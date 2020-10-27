/******************************************************************************
 *
 * [ PSTemplateModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreMessages;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.client.proxies.impl.PSTemplateContentTypeUpdater;
import com.percussion.extension.IPSExtension;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#TEMPLATE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 */
public class PSTemplateModelProxy extends PSTestModelProxy
      implements PSTemplateContentTypeUpdater.UpdaterClient 
{

   /**
    * Creates new proxy.
    */
   public PSTemplateModelProxy()
   {
      super(PSObjectTypes.TEMPLATE);
      m_objectSecondaryType = PSObjectTypes.TemplateSubTypes.SHARED;
      try
      {
         loadFromRepository();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
     
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * rename(com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void rename(IPSReference ref, String name, Object data)
   {
      // we may not know about the data object if it hasn't been persisted
      PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) m_repositoryMap.get(ref);
      if (template != null)
      {
         template.setName(name);
         try
         {
            PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error saving to repository file "
               + ms_repository, e);
         }
      }
      renameLocal(ref, name, data);
      
   }
   
   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      ((PSReference) ref).setName(name);
      if (data != null)
      {
         ((PSUiAssemblyTemplate) data).setName(name);
      }
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      if(sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty.");
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if(results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for(int i = 0; i < sourceObjects.length; i++)
      {
         if(sourceObjects[i] instanceof PSUiAssemblyTemplate)
         {
            try
            {
               PSUiAssemblyTemplate template = (PSUiAssemblyTemplate)clone(sourceObjects[i]);
               template.setGUID(
                  PSCoreUtils.dummyGuid(PSTypeEnum.TEMPLATE));
               
               String name;
               if (names == null || StringUtils.isBlank(names[i]))
               {
                  name = PSCoreMessages.getString("common.prefix.copyof") //$NON-NLS-1$
                     + template.getName();
               }
               else
                  name = names[i];
               template.setName(name);
               results.add(template);
               refs[i] = objectToReference(template);
               m_lockHelper.getLock(refs[i]);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }            
         }
         else
         {
            throw new IllegalArgumentException(
                  "sourceObjects must be instances of PSAssemblyTemplate");
         }
      }
      return refs;
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names, List results)
   {
      if (objType == null
         || !objType.getPrimaryType().equals(m_objectPrimaryType)
         || objType.getSecondaryType().equals(TemplateSubTypes.OTHER))
         throw new IllegalArgumentException("objType is invalid.");
      if(names == null)
         throw new IllegalArgumentException("names cannot be null.");
      if(results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for(String name : names)
      {
         PSUiAssemblyTemplate template = createNewObject(name, -1);
         com.percussion.client.proxies.impl.PSTemplateModelProxy
               .configureTemplate(template, objType);
         refs[++idx] = objectToReference(template);
         m_lockHelper.getLock(refs[idx]);
         results.add(template);
      }
      return refs;
   }

   /**
    * Load the existing objects from the repository.
    * @throws PSProxyTestException  
    */
   @SuppressWarnings("unchecked")
   private void loadFromRepository() throws PSProxyTestException 
   {
      m_repositoryMap.clear();
      if (!ms_repository.exists())
      {
         // If repository does not exist
         // create a few to start with
         createLocalTemplates();
         createSharedTemplates();
         createGlobalTemplates();
         createVariantTemplates();
         
         // and save to repository
         PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = 
            (PSTemplateMap)PSProxyTestUtil.loadRepository(ms_repository);
      }
   }

   private void createVariantTemplates()
   {
      {
         final PSUiAssemblyTemplate temp = createNewObject("Variant 1", 151);
         temp.setAssemblyUrl("../rxs_Test_cas/testGeneric.html");
         temp.setDescription("Renders the test body field");
         temp.setOutputFormat(OutputFormat.Page);
         temp.setStyleSheetPath("TestGeneric.xsl");
         temp.setSlotGuids((Collections.singleton(loadSlot().getGUID())));
         assert temp.isVariant();
         m_repositoryMap.put(objectToReference(temp), temp);
      }

      {
         final PSUiAssemblyTemplate temp = createNewObject("Variant 2", 152);
         temp.setAssemblyUrl("../rxs_Test_cas/testCallout.html");
         temp.setDescription("Renders the test callout field.");
         temp.setOutputFormat(OutputFormat.Snippet);
         temp.setStyleSheetPath("TestCallout.xsl");
         assert temp.isVariant();
         m_repositoryMap.put(objectToReference(temp), temp);
      }
   }

   /**
    * Creates a couple of global templates (id=300, 310) and adds them to the 
    * repository.
    */
   private void createGlobalTemplates()
   {
      {
         final PSUiAssemblyTemplate temp = createNewObject("gtemplate1", 300);
         temp.setLabel("Global template 1");
         temp.setPublishWhen(PublishWhen.Default);
         temp.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);
         temp.setAssemblyUrl("../rxassembly");
         temp.setDescription("A global template.");
         temp.setOutputFormat(OutputFormat.Global);
         temp.setStyleSheetPath("global1.xsl");
         temp.setTemplateType(TemplateType.Shared);
         {
            final List<PSTemplateBinding> bindings = new ArrayList<PSTemplateBinding>();
            bindings.add(new PSTemplateBinding(10, "var10", "$user"));
            bindings.add(new PSTemplateBinding(5,  "var5", "$rx"));
            bindings.add(new PSTemplateBinding(15, "var15", "$sys.template"));
            temp.getBindings().addAll(bindings);
         }
         m_repositoryMap.put(objectToReference(temp), temp);
      }

      {
         final PSUiAssemblyTemplate temp = createNewObject("gtemplate2", 310);
         temp.setLabel("Global template 2");
         temp.setPublishWhen(PublishWhen.Default);
         temp.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);
         temp.setAssemblyUrl("../rxassembly");
         temp.setDescription("A second global template.");
         temp.setOutputFormat(OutputFormat.Global);
         temp.setStyleSheetPath("global2.xsl");
         temp.setTemplateType(TemplateType.Shared);
         m_repositoryMap.put(objectToReference(temp), temp);
      }
   }

   private void createLocalTemplates()
   {
      {
         final PSUiAssemblyTemplate temp = createNewObject("Test_Body", 100);
         temp.setPublishWhen(PublishWhen.Default);
         temp.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);
         temp.setAssemblyUrl("../rxs_Test_cas/testGeneric.html");
         temp.setDescription("Renders the test body field");
         temp.setOutputFormat(OutputFormat.Page);
         temp.setStyleSheetPath("TestGeneric.xsl");
         temp.setTemplateType(TemplateType.Local);
         m_repositoryMap.put(objectToReference(temp), temp);
      }

      {
         final PSUiAssemblyTemplate temp = createNewObject("Test_Snippet", 110);
         temp.setPublishWhen(PublishWhen.Never);
         temp.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);
         temp.setAssemblyUrl("../rxs_Test_cas/testCallout.html");
         temp.setDescription("Renders the test callout field.");
         temp.setOutputFormat(OutputFormat.Snippet);
         temp.setStyleSheetPath("TestCallout.xsl");
         temp.setTemplateType(TemplateType.Local);
         m_repositoryMap.put(objectToReference(temp), temp);
      }
   }

   private void createSharedTemplates()
   {      
      PSUiAssemblyTemplate temp = createNewObject("P_CI_Generic", 200);
      temp.setPublishWhen(PublishWhen.Default);
      temp.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);
      temp.setAssemblyUrl("../rxs_Shared_cas/s_shared.html?shared_variantid=6");
      temp.setDescription("Renders the shared body field");
      temp.setOutputFormat(OutputFormat.Page);
      temp.setStyleSheetPath("p_CIGeneric.xsl");
      temp.setTemplateType(TemplateType.Shared);
      temp.setSlotGuids((Collections.singleton(loadSlot().getGUID())));
      m_repositoryMap.put(objectToReference(temp), temp);
      
      temp = createNewObject("S_Callout", 210);
      temp.setPublishWhen(PublishWhen.Never);      
      temp.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);
      temp.setAssemblyUrl("../rxs_Shared_cas/p_shared.html?shared_variantid=1000");
      temp.setDescription("Renders the shared callout field.");
      temp.setOutputFormat(OutputFormat.Snippet);
      temp.setStyleSheetPath("s_callout.xsl");
      temp.setTemplateType(TemplateType.Shared);
      m_repositoryMap.put(objectToReference(temp), temp);
   }

   private IPSTemplateSlot loadSlot()
   {
      try
      {
         IPSCmsModel slotModel = PSCoreFactory.getInstance().getModel(PSObjectTypes.SLOT);
         IPSReference slotRef = slotModel.getReference(new PSGuid(1L, PSTypeEnum.SLOT, 2));
         IPSTemplateSlot slot = (IPSTemplateSlot) slotModel.load(slotRef, false, false);
         return slot;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * createNewObject(java.lang.String)
    */
   private PSUiAssemblyTemplate createNewObject(String name, int id)
   {
     if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
            
      IPSGuid guid = id == -1 
         ? PSCoreUtils.dummyGuid(PSTypeEnum.TEMPLATE)
         : new PSGuid(1L, PSTypeEnum.TEMPLATE, id);
      PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      
      template.setGUID(guid);
      template.setName(name);         
      template.setDescription(PSCoreMessages.getString(
         "common.prefix.description") //$NON-NLS-1$
         + " " + name); //$NON-NLS-1$
      template.setAssemblyUrl("foo.html");
      template.setOutputFormat(OutputFormat.Page);
      template.setStyleSheetPath("foostylesheet.xsl");
      template.setVersion(0);

      return template;      
   }
   
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
         throws PSMultiOperationException, PSModelException
   {
      new PSTemplateContentTypeUpdater().save(this, refs, data, releaseLock);
   }

   public void doSave(IPSReference[] refs, Object[] data, boolean releaseLock)
   throws PSMultiOperationException
{
   try
   {
      super.save(refs, data, releaseLock);

      // check the types against the object and see if they need to be changed
      for (int i = 0; i < data.length; i++)
      {
         IPSReference testRef = PSReferenceFactory.getInstance()
               .getReference(
                     data[i],
                     (IPSPrimaryObjectType) refs[i].getObjectType()
                           .getPrimaryType());
         if (!testRef.getObjectType().equals(refs[i].getObjectType()))
         {
            ((PSReference) refs[i]).setObjectType(testRef.getObjectType());
         }
      }
   }
   catch (PSModelException e)
   {
      // should never happen
      throw new RuntimeException(e);
   }
}

   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryFile()
    */
   @Override
   protected File getRepositoryFile()
   {
      return ms_repository;
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryMap()
    */
   @Override
   protected IPSRepositoryMap getRepositoryMap()
   {
      return m_repositoryMap;
   }
   
   /**
    * Name of the repository file.
    */
   public static final String REPOSITORY_XML = "template_repository.xml";

   /**
    * Repository file name this test proxy uses. The file will be created
    * in the root directory for the workbench if one does not exist. It will use
    * the existing one if one exists.
    */
   static private File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all object from the repository. Filled during initialization of the
    * proxy.
    */
   private PSTemplateMap m_repositoryMap = new PSTemplateMap();
}

/******************************************************************************
 *
 * [ PSContentTypeModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreMessages;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.objectstore.PSUiItemDefinition;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#CONTENT_TYPE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @since 03-Sep-2005 4:39:27 PM
 */
public class PSContentTypeModelProxy extends PSComponentTestModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#CONTENT_TYPE} and for main type and <code>null</code>
    * sub type since this object type foes not have any sub types.
    */
   public PSContentTypeModelProxy() 
   {
      super(PSObjectTypes.CONTENT_TYPE);
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
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */

   @Override
   public IPSReference[] create(
      final PSObjectType objType, final Collection<String> names,
      final List<Object> results)
   {
      if (objType == null
         || !objType.getPrimaryType().equals(m_objectPrimaryType))
         throw new IllegalArgumentException("objType is invalid."); //$NON-NLS-1$
      if(names == null)
         throw new IllegalArgumentException("names cannot be null."); //$NON-NLS-1$
      if(results == null)
         throw new IllegalArgumentException("results cannot be null."); //$NON-NLS-1$
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for(String name : names)
      {
         
         PSUiItemDefinition contentType = createNewObject(name, -1);         
         refs[++idx] = objectToReference(contentType);
         results.add(contentType);
         m_lockHelper.getLock(refs[idx]);
        
      }
      return refs;
   }

   /* 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @Override
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      if(sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty."); //$NON-NLS-1$
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if(results == null)
         throw new IllegalArgumentException("results cannot be null."); //$NON-NLS-1$
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for(int i = 0; i < sourceObjects.length; i++)
      {
         if(sourceObjects[i] instanceof PSUiItemDefinition)
         {
            try
            {
               PSUiItemDefinition contentType = (PSUiItemDefinition)clone(sourceObjects[i]);
               contentType.setTypeId(
                  (int)PSCoreUtils.dummyGuid(PSTypeEnum.NODEDEF).longValue());
               
               String name;
               if (names == null || StringUtils.isBlank(names[i]))
               {
                  name = PSCoreMessages.getString("common.prefix.copyof") //$NON-NLS-1$
                        + contentType.getName();
               }
               else
                  name = names[i];
               contentType.setName(name); 
               contentType.setLabel(contentType.getName()); 
               results.add(contentType);
               refs[i] = objectToReference(contentType);
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
                  "sourceObjects must be instances of PSUiItemDefinition"); //$NON-NLS-1$
         }
      }
      return refs;
   }
   
   /* 
    * @see com.percussion.client.proxies.impl.PSKeywordModelProxy#rename(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @SuppressWarnings("unused") //exception
   @Override
   public void rename(IPSReference ref, String name, Object data) 
      throws PSModelException
   {
      
      // we may not know about the data object if it hasn't been persisted
      PSUiItemDefinition cType = (PSUiItemDefinition) m_repositoryMap.get(ref);
      if (cType != null)
      {
         cType.setName(name);
         try
         {
            saveRepository(m_repositoryMap, ms_repository);
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
         ((PSUiItemDefinition) data).setName(name);
      }
   }

   /**
    * Load the existing objects from the repository.
    * @throws PSProxyTestException  
    */
   private void loadFromRepository() throws PSProxyTestException 
   {
      m_repositoryMap.clear();
      if (!ms_repository.exists())
      {
         final int briefIdx = 0;
         final int fileIdx = 1;
         String[] names = new String[]{
               "Brief", "File", "Generic", "Image", "Press_Release"};
         int[] ids = new int[] {10, 20, 30, 40, 50};
         final List<IPSReference> refs = new ArrayList<>();
         // If repository does not exist
         // create a few to start with
         for (int i = 0; i < names.length; i++)
         {
            PSUiItemDefinition cType = createNewObject(names[i], ids[i]);           
            final IPSReference ref = objectToReference(cType);
            refs.add(ref);
            m_repositoryMap.put(ref, cType);
         }

         // and save to repository
         saveRepository(m_repositoryMap, ms_repository);
         
         try
         {
            final IPSCmsModel templateModel = 
               PSCoreFactory.getInstance().getModel(PSObjectTypes.TEMPLATE);
            final Collection<IPSReference> templateRefs = templateModel.catalog();
            
            final IPSContentTypeModel model = (IPSContentTypeModel) getModel();
            final Map<IPSReference, Collection<IPSReference>> associations =
                  new HashMap<>();
            associations.put(refs.get(briefIdx), templateRefs);
            associations.put(refs.get(fileIdx), templateRefs);
            model.setTemplateAssociations(associations);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         m_repositoryMap = 
            (PSRepositoryMap)loadRepository(ms_repository);
      }
   }   
   
   /**
    * Convienence to return a <code>PSItemDefinition</code> object for
    * testing purposes only. A dummy guid will be generated instead
    * of a real one from the database. The value will be used as the
    * label and in the description.
    * @param name @param name for which to create a new entry, not 
    *    <code>null</code> or empty.
    * 
    * @return new item definition object , never <code>null</code>.
    */   
   private PSUiItemDefinition createNewObject(String name, int id)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty"); //$NON-NLS-1$
      
      IPSGuid guid = id == -1 
      ? PSCoreUtils.dummyGuid(PSTypeEnum.NODEDEF)
         : new PSGuid(0L, PSTypeEnum.NODEDEF, id);
      String appname = name + "_app";
      String desc = PSCoreMessages.getString("common.prefix.description") //$NON-NLS-1$
      + " " + name; //$NON-NLS-1$
      String editorUrl = "../sys_" + name + "/" +  name + ".html";
      PSContentType typedef = 
         new PSContentType((int)guid.longValue(), name, name,
            desc, editorUrl, false, -1);
      Element elem = getDefaultEditorElem(guid);
      PSContentEditor editor = null;
      try
      {
         editor = new PSContentEditor(elem,null,null);
      }
      catch (PSUnknownNodeTypeException ignore)
      {
         //This should not happen as we are creating the editor
         //from the default template
         ignore.printStackTrace();
      }
      if (name.equalsIgnoreCase("Press_Release"))
      {
         List<Integer> workflows = new ArrayList<>();
         workflows.add(4);
         PSWorkflowInfo info = new PSWorkflowInfo(PSWorkflowInfo.TYPE_INCLUSIONARY,
               workflows);
         editor.setWorkflowInfo(info);
      }
      return new PSUiItemDefinition(appname, typedef, editor);
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
   public static final String REPOSITORY_XML = "contenttype_repository.xml";

   /**
    * Repository file name this test proxy uses. The file will be created
    * in the root directory for the workbench if one does not exist. It will use
    * the existing one if one exists.
    */
    private static File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all object from the repository. Filled during initialization of the
    * proxy.
    */
   private PSRepositoryMap m_repositoryMap = 
      new PSRepositoryMap();

   @Override
   protected Object newComponentInstance()
   {
      return createNewObject("dummy", -1);
   }

   /**
    * Reads the default editor xml file sys_Default.xml and returns the
    * PSXContentEditor element. May be <code>null</code>.
    * @return Document of sys_Templates.xsl file
    */
   private Element getDefaultEditorElem(IPSGuid guid)
   {
      Element elem = null;
      InputStream is = null;
      try
      {
         Class clazz = getClass();
         is = clazz.getResourceAsStream("sys_Default.xml");
         InputSource source = new InputSource(is);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(source, false);
         NodeList nl = doc.getElementsByTagName(PSContentEditor.XML_NODE_NAME);
         if(nl != null && nl.getLength() > 0)
         {
            elem = (Element) nl.item(0);
            elem.setAttribute("contentType",Integer.toString((int)guid.longValue()));
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }      
      finally
      {
         if(is != null)
            try
            {
               is.close();
            }
            catch (IOException ignore)
            {
               //Ignore
            }
      }
      return elem;
   }   
   
   
   
}

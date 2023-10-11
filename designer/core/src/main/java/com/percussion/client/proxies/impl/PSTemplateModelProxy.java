/******************************************************************************
 *
 * [ PSTemplateModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.*;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSTemplateModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.client.proxies.PSObjectFactory;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.client.proxies.PSUiAssemblyTemplateConverter;
import com.percussion.extension.IPSExtension;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assemblydesign.AssemblyDesignSOAPStub;
import com.percussion.webservices.assemblydesign.FindAssemblyTemplatesRequest;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#TEMPLATE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @since 03-Sep-2005 4:39:27 PM
 */
public class PSTemplateModelProxy extends PSCmsModelProxy implements
   PSTemplateContentTypeUpdater.UpdaterClient
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#TEMPLATE} and for main type and <code>null</code>
    * sub type since this object type does not have any sub types.
    */
   public PSTemplateModelProxy()
   {
      super(PSObjectTypes.TEMPLATE);
      PSTransformerFactory.getInstance().register(
         PSUiAssemblyTemplateConverter.class, PSUiAssemblyTemplate.class,
         PSAssemblyTemplate.class);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(com.percussion.client.PSObjectType,
    * java.util.Collection, java.util.List)
    */
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results) throws PSMultiOperationException, PSModelException
   {
      if (objType == null)
      {
         throw new IllegalArgumentException("objType must not be null");
      }
      if (!objType.getPrimaryType().equals(PSObjectTypes.TEMPLATE))
      {
         throw new IllegalArgumentException(
               "Unrecognized object type:" + objType);
      }
      if (names == null || names.isEmpty())
      {
         throw new IllegalArgumentException("names must not be null or empty"); //$NON-NLS-1$
      }
      if (results == null)
      {
         throw new IllegalArgumentException("results must not be null"); //$NON-NLS-1$
      }
      results.clear();
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               AssemblyDesignSOAPStub binding =
                     (AssemblyDesignSOAPStub) getSoapBinding(METHOD.CREATE);
               Object createdObjects = binding.createAssemblyTemplates(names
                  .toArray(new String[0]));
      
               Object[] result = (Object[]) PSProxyUtils.convert(
                  PSUiAssemblyTemplate[].class, createdObjects);
               for (final Object obj : result)
               {
                  final PSUiAssemblyTemplate template = 
                     (PSUiAssemblyTemplate) obj;
                  configureTemplate(template, objType);
                  results.add(template);
               }
      
               IPSReference[] refs = PSObjectFactory.objectToReference(result,
                  (IPSPrimaryObjectType) objType.getPrimaryType(), true);
               configureReferenceSecurity(refs);
      
               return refs;
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
         } while (redo);
      }
      catch (SecurityException | ServiceException | MalformedURLException e)
      {
         ex = e;
      }



      if (ex != null)
         processAndThrowException(names.size(), ex);
      
      // will never get here
      return new IPSReference[0];
   }

   // see base
   @Override
   protected void postCreate(Object[] sourceObjects, List<Object> results,
         Object[] refs) throws PSModelException
   {
      final IPSTemplateModel templateModel = (IPSTemplateModel)
            PSCoreFactory.getInstance().getModel(PSObjectTypes.TEMPLATE);
      for (int i = 0; i < sourceObjects.length; i++)
      {
         final PSUiAssemblyTemplate template =
               (PSUiAssemblyTemplate) results.get(i);
         try
         {
            final PSReference ref = (PSReference)
                  PSObjectFactory.objectToReference(sourceObjects[i],
                        m_objectPrimaryType, true);
            template.setNewContentTypes(new HashSet<>(
                  templateModel.getContentTypes(ref, false)));
         }
         catch (PSModelException e)
         {
            results.set(i, null);
            refs[i] = e;
         }
      }
   }

   /**
    * Sets template properties according to the object type.
    */
   public static void configureTemplate(PSUiAssemblyTemplate template,
         PSObjectType objType)
   {
      if (objType.getSecondaryType().equals(TemplateSubTypes.VARIANT))
      {
         // now it is by default
         if (!template.isVariant())
         {
            throw new IllegalStateException(
                  "A template is supposed to be legacy template by default");
         }
      }
      else if (objType.getSecondaryType().equals(TemplateSubTypes.GLOBAL))
      {
         template.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);
         template.setOutputFormat(OutputFormat.Global);
         assert !template.isVariant();
      }
      else
      {
         assert ms_types.get(objType.getSecondaryType()) != null;
         template.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);
         template.setPublishWhen(PublishWhen.Default);      
         template.setTemplateType(ms_types.get(objType.getSecondaryType()));
         assert !template.isVariant();
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               AssemblyDesignSOAPStub bindingObj = 
                  (AssemblyDesignSOAPStub) getSoapBinding(METHOD.CATALOG);
               FindAssemblyTemplatesRequest request = 
                  new FindAssemblyTemplatesRequest();
               // Catalog global templates
               request.setGlobalFilter(true);
               PSObjectSummary[] results = bindingObj
                  .findAssemblyTemplates(request);
               PSObjectType oType = PSObjectTypeFactory.getType(
                  PSObjectTypes.TEMPLATE, TemplateSubTypes.GLOBAL);
               Collection<IPSReference> resColl = 
                  objectSummaryArrayToReferenceCollection(results, oType);
      
               // Catalog legacy templates
               request.setGlobalFilter(false);
               request.setLegacyFilter(true);
               results = bindingObj
                  .findAssemblyTemplates(request);
               oType = PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                  TemplateSubTypes.VARIANT);
               Collection<IPSReference> resLegacy = 
                  objectSummaryArrayToReferenceCollection(results, oType);
               resColl.addAll(resLegacy);
      
               // Catalog local templates
               request.setGlobalFilter(false);
               request.setLegacyFilter(false);
               request.setTemplateType(
                  com.percussion.webservices.assembly.data.TemplateType.local);
               results = bindingObj
                  .findAssemblyTemplates(request);
               oType = PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                  TemplateSubTypes.LOCAL);
               Collection<IPSReference> resLocal = 
                  objectSummaryArrayToReferenceCollection(results, oType);
               resColl.addAll(resLocal);
      
               // Catalog shared templates
               request.setGlobalFilter(false);
               request.setLegacyFilter(false);
               request.setTemplateType(
                  com.percussion.webservices.assembly.data.TemplateType.shared);
               results = bindingObj
                  .findAssemblyTemplates(request);
               oType = PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                  TemplateSubTypes.SHARED);
               Collection<IPSReference> resShared = 
                  objectSummaryArrayToReferenceCollection(results, oType);
               resColl.addAll(resShared);
      
               return resColl;
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(ex);
      
      // will never get here
      return new ArrayList<IPSReference>();
   }

   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      new PSTemplateContentTypeUpdater().save(this, refs, data, releaseLock);
   }

   public void doSave(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      super.save(refs, data, releaseLock);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#getSoapBinding(com.percussion.client.proxies.IPSCmsModelProxy.METHOD)
    */
   @SuppressWarnings("unused")
   @Override
   protected Stub getSoapBinding(METHOD method) throws MalformedURLException,
      ServiceException
   {
      return PSProxyUtils.getAssemblyDesignStub();
   }

   /**
    * Mapping between template object subtypes and template types.
    */
   static private final Map<TemplateSubTypes, TemplateType> ms_types;

   static
   {
      final Map<TemplateSubTypes, TemplateType> types =
            new HashMap<TemplateSubTypes, TemplateType>(); 
      types.put(TemplateSubTypes.LOCAL, TemplateType.Local);
      types.put(TemplateSubTypes.SHARED, TemplateType.Shared);
      ms_types = Collections.unmodifiableMap(types);
   }
}

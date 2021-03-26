/******************************************************************************
 *
 * [ PSContentTypeModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.models.PSLockException;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.client.objectstore.PSUiItemDefinition;
import com.percussion.client.proxies.IPSCmsModelProxy.METHOD;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.util.PSRemoteRequester;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.PSContentTemplateDesc;
import com.percussion.webservices.contentdesign.ContentDesignSOAPStub;
import com.percussion.webservices.contentdesign.LoadAssociatedTemplatesRequest;
import com.percussion.webservices.contentdesign.SaveAssociatedTemplatesRequest;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementes the interface.
 * 
 * @see IPSContentTypeModel
 * 
 */
public class PSContentTypeModel extends PSCmsModel implements
   IPSContentTypeModel
{
   /**
    * Ctor required by framework. See
    * {@link com.percussion.client.models.IPSCmsModel} for details.
    */
   public PSContentTypeModel(String name, String description,
      IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   /*
    * @see com.percussion.client.models.IPSContentTypeModel#getControls()
    */
   public List<PSControlMeta> getControls() throws PSModelException
   {
      List<PSControlMeta> ctrlList = new ArrayList<PSControlMeta>();
      PSRemoteRequester rr = PSCoreFactory.getInstance().getRemoteRequester();
      Document controlXML;
      try
      {
         controlXML = rr.getDocument(RESOURCE_NAME, new HashMap());
         if (controlXML != null)
         {
            try
            {
               NodeList controlNodes = controlXML
                  .getElementsByTagName(PSControlMeta.XML_NODE_NAME);
               for (int i = 0; controlNodes != null
                  && i < controlNodes.getLength(); i++)
               {
                  ctrlList
                     .add(new PSControlMeta((Element) controlNodes.item(i)));
               }
            }
            catch (PSUnknownNodeTypeException e)
            {
               throw new PSModelException(e);
            }
         }
      }
      catch (IOException e)
      {
         throw new PSModelException(e);
      }
      catch (SAXException e)
      {
         throw new PSModelException(e);
      }
      return ctrlList;
   }

   /*
    * @see com.percussion.client.models.IPSContentTypeModel#getCEFieldCatalog()
    */
   public PSContentEditorFieldCataloger getCEFieldCatalog(boolean force,
      boolean forDisplayFormat)
      throws PSModelException
   {
      int catalogIndex = forDisplayFormat ? 1 : 0;
      if (m_fieldCat[catalogIndex] == null || force)
      {
         PSRemoteRequester appReq = PSCoreFactory.getInstance()
            .getRemoteRequester();

         try
         {
            int options = PSRemoteCataloger.FLAG_INCLUDE_HIDDEN
            | PSRemoteCataloger.FLAG_CTYPE_EXCLUDE_HIDDENFROMMENU;
            if(forDisplayFormat)
               options = PSRemoteCataloger.FLAG_INCLUDE_RESULTONLY;
            options |= PSRemoteCataloger.FLAG_EXCLUDE_CHOICES;
            PSRemoteCataloger remCatlg = new PSRemoteCataloger(appReq);
            m_fieldCat[catalogIndex] = new PSContentEditorFieldCataloger(
               remCatlg, null, options);
            
            
         }
         catch (PSCmsException cmsEx)
         {
            throw new PSModelException(cmsEx);
         }
      }

      return m_fieldCat[catalogIndex];
   }

   //see interface
   public Collection<IPSReference> getUseableContentTypes(boolean force)
      throws PSModelException
   {
      Collection<IPSReference> ctypes = catalog(force);
      for (Iterator<IPSReference> iter = ctypes.iterator(); iter.hasNext(); )
      {
         IPSReference ref = iter.next();
         if (ref.getName().equalsIgnoreCase("folder"))
         {
            iter.remove();
            break;
         }
      }
      return ctypes;
   }

   //see interface
   public Map<IPSReference, Collection<IPSReference>> getWorkflowAssociations(
         Collection<IPSReference> workflowFilter, boolean force)
      throws PSModelException
   {
      //todo ph: - add a web service to get this list, it would be MUCH more 
      // efficient
      try
      {
         Map<IPSReference, Collection<IPSReference>> results = 
            new HashMap<IPSReference, Collection<IPSReference>>();
         Collection<IPSReference> typeRefs = catalog();
         List<IPSReference> orderedTypeRefs = new ArrayList<IPSReference>();
         orderedTypeRefs.addAll(typeRefs);
         if (force)
         {
            PSCoreFactory.getInstance().getModel(PSObjectTypes.WORKFLOW)
                  .flush(null);
         }

         //we load all types in one shot so the getWorkflowAssociations call
         // below does not cause a seperate server request for each type
         load(orderedTypeRefs
               .toArray(new IPSReference[orderedTypeRefs.size()]), false, false);

         for (IPSReference ref : orderedTypeRefs)
         {
            Collection<IPSReference> linkedWorkflows = getWorkflowAssociations(
                  ref, false);

            for (IPSReference wf : linkedWorkflows)
            {
               if (!workflowFilter.contains(wf))
                  continue;
               Collection<IPSReference> linkedTypes = results.get(wf);
               if (linkedTypes == null)
               {
                  linkedTypes = new ArrayList<IPSReference>();
                  results.put(wf, linkedTypes);
               }
               linkedTypes.add(ref);
            }
         }
         
         return results; 
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException(e);
      }
   }

   //see interface
   @SuppressWarnings("unchecked")  //workflow ids
   public Collection<IPSReference> getWorkflowAssociations(
         IPSReference ctypeRef, boolean force)
      throws PSModelException
   {
      PSItemDefinition def;
      try
      {
         if (force)
            flush(ctypeRef);
         def = (PSItemDefinition) load(ctypeRef, false, false);
      }
      catch (Exception e)
      {
         throw new PSModelException(e);
      }
      Set<IPSReference> wfRefs = new HashSet<IPSReference>();

      IPSCmsModel wfModel = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.WORKFLOW);
      int defaultId = def.getContentEditor().getWorkflowId();
      
      IPSReference tmp = null;
      if (defaultId >= 0)
      {
         tmp = wfModel.getReference(new PSDesignGuid(PSTypeEnum.WORKFLOW,
               defaultId));
      }
      
      if (tmp != null)
         wfRefs.add(tmp);
      
      PSWorkflowInfo workflowInfo = def.getContentEditor()
            .getWorkflowInfo();
      if (workflowInfo != null)
      {
         // todo - OK for release - will need to be fixed when guids fully
         // embraced
         List<Integer> wfIds = workflowInfo.getWorkflowIds();
         for (Integer id : wfIds)
         {
            tmp = wfModel.getReference(new PSDesignGuid(PSTypeEnum.WORKFLOW,
                  id));
            if (tmp != null)
               wfRefs.add(tmp);
         }
      }
      return wfRefs;
   }

   
   // see interface for details
   public Map<IPSReference, Collection<IPSReference>> getTemplateAssociations(
         Collection<IPSReference> contentTypeFilter, boolean force,
         boolean lock) 
      throws PSModelException, PSLockException
   {
      return getTemplateAssociations(contentTypeFilter, force, lock, true);
   }

   /**
    * The key is a handle to a content type. The value is a set of handles to
    * linked templates. If the type has been cataloged but doesn't have any
    * links, an empty set will be placed in the cache. Never <code>null</code>.
    * 
    * @see #m_fullCache
    */
   final Map<IPSReference, Collection<IPSReference>> m_cache = 
      new HashMap<IPSReference, Collection<IPSReference>>();

   /**
    * A flag to indicate whether a query of all ctypes has been performed and
    * cached.
    */
   boolean m_fullCache = false;

   /**
    * Just like {@link #getTemplateAssociations(Collection, boolean, boolean)},
    * except it allows the caller to optionally include unpersisted links. 
    */
   private Map<IPSReference, Collection<IPSReference>> getTemplateAssociations(
         Collection<IPSReference> contentTypeFilter, boolean force,
         boolean lock, boolean includeUnpersistedLinks) 
      throws PSModelException, PSLockException
   {
      IPSCmsModel templateModel = getTemplateModel();

      // init node update if not already done.
      getUpdater();
      
      Exception ex = null;
      Map<IPSReference, Collection<IPSReference>> result = null;
      try
      {
         result = loadTemplateAssociations(contentTypeFilter, force, lock);
         
         if (includeUnpersistedLinks)
         {
            //need to find any local links that haven't been persisted yet
            Collection<IPSReference> templateRefs = templateModel.catalog();
            for (IPSReference templateRef : templateRefs)
            {
               if (templateRef.isPersisted())
                  continue;
               
               PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) 
                  templateModel.load(templateRef, false, false);
               Set<IPSReference> ctypes = template.getNewContentTypes();
               if (ctypes != null)
               {
                  for (IPSReference ctypeRef : ctypes)
                  {
                     if (contentTypeFilter != null
                           && !contentTypeFilter.contains(ctypeRef))
                     {
                        continue;
                     }
                     Collection<IPSReference> links = result.get(ctypeRef);
                     if (links == null)
                     {
                        links = new ArrayList<IPSReference>();
                        result.put(ctypeRef, links);
                     }
                     links.add(templateRef);
                  }
               }
            }

            addNewContentTypeTemplates(contentTypeFilter, result);
         }
      }
      catch (PSModelException e)
      {
         throw e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (PSNotAuthorizedFault e)
      {
         ex = e;
      }
      catch (PSErrorResultsFault e)
      {
         ex = PSProxyUtils.createClientException(e, "getTemplateAssociations", 
               "Template Associations",
               contentTypeFilter == null ? "" : contentTypeFilter.toString());

         if (ex instanceof PSModelException)
            throw (PSModelException) ex;
         
         throw (PSLockException) ex;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         if (e instanceof PSLockFault)
         {
            ex = PSProxyUtils.convertFault((PSLockFault) e, 
               "saveTemplateAssociations", PSObjectTypes.TEMPLATE.toString(), 
               null);
         }
         else
            ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (Exception e)
      {
         ex = e;
      }
      if (ex != null)
      {
         throw new PSModelException(ex);
      }
      return result;
   }

   /**
    * Adds to the result templates stored on new content types.
    * @param contentTypeFilter the content type filter passed to
    * {@link #getTemplateAssociations(Collection, boolean, boolean)}.
    * @param result the map to put the template associations to.
    */
   private void addNewContentTypeTemplates(
         Collection<IPSReference> contentTypeFilter,
         Map<IPSReference, Collection<IPSReference>> result)
         throws PSModelException, Exception
   {
      final Collection<IPSReference> contentTypeRefs = catalog();
      for (final IPSReference contentTypeRef : contentTypeRefs)
      {
         if (contentTypeRef.isPersisted())
         {
            continue;
         }
         if (contentTypeFilter != null
               && !contentTypeFilter.contains(contentTypeRef))
         {
            continue;
         }
         final PSUiItemDefinition contentType =
               (PSUiItemDefinition) load(contentTypeRef, false, false);
         if (contentType.areNewTemplatesSpecified())
         {
            result.put(contentTypeRef, contentType.getNewTemplates());
         }
      }
   }

   /**
    * Load content type template associations from the cache or server.
    * 
    * @param contentTypeFilter collection of all content types for which the
    * associations are to be loaded. If <code>null</code> all the content
    * types are used.
    * @param force <code>true</code> to forceload the associations from
    * server, <code>false</code> to get from the previously cached ones.
    * @param lock <code>true</code> to load the associations for for editing,
    * <code>false</code> otherwise.
    * @return A map of content type id to set of templateids, never
    * <code>null</code> may by empty.
    * @throws ServiceException
    * @throws RemoteException
    * @throws MalformedURLException
    * @throws PSErrorResultsFault
    * @throws PSNotAuthorizedFault
    * @throws PSContractViolationFault
    * @throws PSModelException 
    * @throws Exception
    */
   private Map<IPSReference, Collection<IPSReference>> loadTemplateAssociations(
      Collection<IPSReference> contentTypeFilter, boolean force, boolean lock)
      throws PSContractViolationFault, PSNotAuthorizedFault, 
      PSErrorResultsFault, MalformedURLException, RemoteException, 
      ServiceException, PSModelException, Exception
   {
      if(lock)
         force = true;

      if(force)
      {
         //flush and reload
         if (contentTypeFilter == null)
            loadTemplateAssociations(null, lock);
         else
         {
            for (IPSReference ref : contentTypeFilter)
            {
               loadTemplateAssociations(ref, lock);
            }
         }
      }
      
      //make sure the cache has everything needed if not forced
      Collection<IPSReference> ctypeSource = contentTypeFilter;
      if (contentTypeFilter == null || contentTypeFilter.size() > 1)
      {
         if (!m_fullCache)
         {
            loadTemplateAssociations(null, lock);
         }
         if (contentTypeFilter == null)
            ctypeSource = m_cache.keySet();
      }
      else
      {
         for (IPSReference ref : contentTypeFilter)
         {
            if (m_cache.get(ref) == null)
               loadTemplateAssociations(ref, lock);
         }
      }
      
      //make a copy of the cache
      Map<IPSReference, Collection<IPSReference>> results = 
         new HashMap<IPSReference, Collection<IPSReference>>();
      for (IPSReference ctype : ctypeSource)
      {
         Collection<IPSReference> templateRefs = m_cache.get(ctype);
         if (templateRefs != null && !templateRefs.isEmpty())
         {
            Collection<IPSReference> copy = new ArrayList<IPSReference>();
            copy.addAll(templateRefs);
            results.put(ctype, copy);
         }
      }
      
      // 
      return results;
   }

   /**
    * Method to load or refresh the content type template associations for
    * specified content type into cache. The cache is cleared for the specified
    * type before the cataloged nodes are added.
    * 
    * @param ctypeRef The reference to the content type for which associations
    * are to be loaded, may be <code>null</code> to load them for all types. 
    * 
    * @param lock <code>true</code> to load the associations for editing,
    * <code>false</code> otherwise.
    * @throws MalformedURLException
    * @throws ServiceException
    * @throws PSContractViolationFault
    * @throws PSNotAuthorizedFault
    * @throws PSErrorResultsFault
    * @throws RemoteException
    * @throws PSModelException 
    * @throws Exception
    */
   private void loadTemplateAssociations(IPSReference ctypeRef, boolean lock)
      throws MalformedURLException, ServiceException, PSContractViolationFault, 
      PSNotAuthorizedFault, PSErrorResultsFault, RemoteException, 
      PSModelException, Exception
   {
      if (ctypeRef != null && !ctypeRef.isPersisted())
      {
         return;
      }
      
      final Set<IPSGuid> missingTemplates = new HashSet<IPSGuid>();
      final Set<IPSGuid> missingContentTypes = new HashSet<IPSGuid>();

      loadTemplateAssociationsFromServer(ctypeRef, lock,
            missingTemplates, missingContentTypes);
      
      // if some can't be loaded, reload the models
      if (!missingTemplates.isEmpty() || !missingContentTypes.isEmpty())
      {
         missingTemplates.clear();
         missingContentTypes.clear();
         getTemplateModel().catalog(true);
         getContentTypeModel().catalog(true);
         loadTemplateAssociationsFromServer(ctypeRef, lock,
               missingTemplates, missingContentTypes);
      }
      maybeReportMissingData(missingTemplates, missingContentTypes);
   }

   /**
    * Throws an exception, reporting missing data,
    * if <code>missingTemplates</code> or <code>missingContentTypes</code>
    * are not empty. 
    * @param missingTemplates templates, which can't be loaded.
    * Not <code>null</code>.
    * @param missingContentTypes content types, which can't be loaded.
    * Not <code>null</code>.
    * @throws PSModelException if missing data is found in the provided
    * collections. 
    */
   private void maybeReportMissingData(final Set<IPSGuid> missingTemplates,
         final Set<IPSGuid> missingContentTypes) throws PSModelException
   {
      assert missingTemplates != null;
      assert missingContentTypes != null;

      if (!missingTemplates.isEmpty())
      {
         throw new PSModelException(PSErrorCodes.INACCESSIBLE_TEMPLATES,
               idsToString(missingTemplates));
      }
      if (!missingContentTypes.isEmpty())
      {
         throw new PSModelException(PSErrorCodes.INACCESSIBLE_CONTENT_TYPES,
               idsToString(missingContentTypes));
      }
   }

   /**
    * Returns a string, listing the comma-separated ids.
    * @param ids the ids to convert to a string.
    * Not <code>null</code> or empty.
    * @return string, which lists the provided ids separated by commas.
    * Never <code>null</code> or empty.
    */
   private String idsToString(final Set<IPSGuid> ids)
   {
      assert ids != null && !ids.isEmpty();
      final String separator = ", ";
      final StringBuffer result = new StringBuffer();
      for (final IPSGuid id : ids)
      {
         result.append(id.toString());
         result.append(" (");
         result.append(id.longValue());
         result.append(")" + separator);
      }

      // remove the last added separator
      result.setLength(result.length() - separator.length());
      return result.toString();
   }

   /**
    * Actually loads template associations data from the server into the model
    * cache.
    * See {@link #loadTemplateAssociations(IPSReference, boolean)} for details. 
    * @param missingTemplates a collection to populate with ids of
    * the templates, which can't be loaded.
    * Not <code>null</code>, initially empty.
    * @param missingContentTypes a collection to populate with ids of
    * the content types, which can't be loaded.
    * Not <code>null</code>, initially empty.
    */
   private void loadTemplateAssociationsFromServer(final IPSReference ctypeRef,
         final boolean lock, final Set<IPSGuid> missingTemplates,
         final Set<IPSGuid> missingContentTypes)
         throws MalformedURLException, ServiceException, RemoteException,
         PSErrorResultsFault, PSContractViolationFault, PSNotAuthorizedFault,
         PSModelException, Exception
   {
      assert missingTemplates != null && missingTemplates.isEmpty();
      assert missingContentTypes != null && missingContentTypes.isEmpty();

      clearTemplateAssociationsCache(ctypeRef);

      boolean redo;
      do
      {
         redo = false;
         try
         {
            final PSContentTemplateDesc[] ctd =
                  loadContentTemplateDescriptions(ctypeRef, lock);

            for (PSContentTemplateDesc desc : ctd)
            {
               final IPSReference contentTypeRef;
               {
                  final Long ctypeId = desc.getContentTypeId();
                  final PSDesignGuid guid =
                        new PSDesignGuid(PSTypeEnum.NODEDEF, ctypeId);
                  contentTypeRef = getContentTypeModel().getReference(guid);
                  if (contentTypeRef == null)
                  {
                     missingContentTypes.add(guid);
                     continue;
                  }
               }

               final IPSReference templateRef;
               {
                  final Long templateId = desc.getTemplateId();
                  final PSDesignGuid guid =
                        new PSDesignGuid(PSTypeEnum.TEMPLATE, templateId);
                  templateRef = getTemplateModel().getReference(guid);
                  if (templateRef == null)
                  {
                     missingTemplates.add(guid);
                     continue;
                  }
               }

               cacheTemplateAssociation(contentTypeRef, templateRef);
            }
            if (ctypeRef == null)
               m_fullCache = true;
            else if (ctd.length == 0)
               m_cache.put(ctypeRef, Collections.<IPSReference>emptySet());
         }
         catch (PSInvalidSessionFault e)
         {
            PSCoreFactory.getInstance().reconnect();
            redo = true;
         }
      } while (redo);
   }

   /**
    * Removes content type/template associations for the provided content type
    * from the cache. 
    * @param ctypeRef the content type reference to remove from the cache.
    * If <code>null</code>, the whole cache is emptied.
    */
   private void clearTemplateAssociationsCache(IPSReference ctypeRef)
   {
      if (ctypeRef == null)
      {
         m_cache.clear();
         m_fullCache = false;
      }
      else
      {
         //fixme don't use map, key may change
         m_cache.remove(ctypeRef);
      }
   }
   
   @Override
   public void flush(final IPSReference ref)
   {
      super.flush(ref);
      clearTemplateAssociationsCache(ref);
      m_fullCache = false;
   }

   /**
    * Loads content type/template descriptions from server.
    * @param ctypeRef
    * @param lock
    * @see #loadTemplateAssociations(IPSReference, boolean)
    */
   private PSContentTemplateDesc[] loadContentTemplateDescriptions(
         final IPSReference ctypeRef, final boolean lock)
         throws MalformedURLException, ServiceException, RemoteException,
         PSErrorResultsFault, PSInvalidSessionFault, PSContractViolationFault,
         PSNotAuthorizedFault
   {
      ContentDesignSOAPStub binding = PSProxyUtils.getContentDesignStub();
      LoadAssociatedTemplatesRequest req = new LoadAssociatedTemplatesRequest();
      req.setContentTypeId(ctypeRef ==
            null  ? -1 : ctypeRef.getId().longValue());
      req.setLock(lock);
      return binding.loadAssociatedTemplates(req);
   }

   /**
    * Adds the specified content type/template association
    * to the associations cache.
    * @param contentTypeRef content type reference. Not <code>null</code>.
    * @param templateRef associated template reference. Not <code>null</code>. 
    */
   private void cacheTemplateAssociation(final IPSReference contentTypeRef,
         final IPSReference templateRef)
   {
      assert contentTypeRef != null;
      assert templateRef != null;

      Collection<IPSReference> templateRefColl = m_cache.get(contentTypeRef);
      if (null == templateRefColl)
      {
         templateRefColl = new ArrayList<IPSReference>();
         m_cache.put(contentTypeRef, templateRefColl);
      }
      templateRefColl.add(templateRef);
   }

   /**
    * Saves template associations optionally releasing lock. 
    * 
    * @param releaseLock If you will no longer be editing the data, then
    * <code>true</code> should be supplied so the object will be available for
    * someone else to edit.
    * 
    * @see com.percussion.client.models.IPSContentTypeModel#
    * setTemplateAssociations(java.util.Map)
    * @throws PSMultiOperationException when errors have occurred on any of
    * the attempts to save the associations. Will contain <code>null</code>
    * for successes and the <code>Exception</code> where a failure occurred.
    * The number of entries will match the number of entries in the
    * associations map.
    */
   public void setTemplateAssociations(
         Map<IPSReference, Collection<IPSReference>> associations,
         boolean releaseLock) throws PSMultiOperationException
   {
      // init node update if not already done.
      getUpdater();
      
      List<Object> results = new ArrayList<Object>();
      List<IPSReference> details = new ArrayList<IPSReference>();
      boolean hasErrors = false;
      
      //remove unpersisted refs
      Map<IPSReference, Collection<IPSReference>> validAssociations = 
         new HashMap<IPSReference, Collection<IPSReference>>();
      for (IPSReference ctype : associations.keySet())
      {
         if (!ctype.isPersisted())
            continue;
         Collection<IPSReference> templateRefs = associations.get(ctype);
         Collection<IPSReference> validTemplateRefs = new ArrayList<IPSReference>();
         for (IPSReference templateRef : templateRefs)
         {
            if (templateRef.isPersisted())
               validTemplateRefs.add(templateRef);
         }
         validAssociations.put(ctype, validTemplateRefs);
      }
      
      Collection<IPSReference> additions = new ArrayList<IPSReference>();
      Collection<IPSReference> deletions = new ArrayList<IPSReference>();
      Exception ex = null;
      Collection<IPSReference> tRefColl = null; 
      
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            ContentDesignSOAPStub binding = 
               PSProxyUtils.getContentDesignStub();
            SaveAssociatedTemplatesRequest req = 
               new SaveAssociatedTemplatesRequest();
            for (IPSReference cRef : validAssociations.keySet())
            {
               try
               {
                  
                  details.add(cRef);
                  tRefColl = validAssociations.get(cRef);
                  req.setContentTypeId(cRef.getId().longValue());
                  req.setTemplateId(refCollectionToLongArray(tRefColl));
                  req.setRelease(releaseLock);
                  binding.saveAssociatedTemplates(req);
                  
                  //must be done before cache is updated
                  computeChanges(cRef, tRefColl, deletions, additions);
                  
                  //update the cache
                  Collection<IPSReference> tmp = new ArrayList<IPSReference>();
                  tmp.addAll(validAssociations.get(cRef));
                  m_cache.put(cRef, tmp);
                  
                  if (!additions.isEmpty())
                  {
                     PSModelChangedEvent evt = new PSModelChangedEvent(cRef, 
                        additions, PSModelChangedEvent.ModelEvents.LINKS_ADDED);
                     notifyListeners(evt);
                  }
                  if (!deletions.isEmpty())
                  {
                     PSModelChangedEvent evt = new PSModelChangedEvent(cRef, 
                        deletions, 
                        PSModelChangedEvent.ModelEvents.LINKS_DELETED);
                     notifyListeners(evt);
                  }
                  results.add(null);
                  
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
               catch (PSContractViolationFault e)
               {
                  ex = PSProxyUtils.convertFault(e);
               }
               catch (PSNotAuthorizedFault e)
               {
                  ex = PSProxyUtils.convertFault(e);
               }
               catch (PSErrorsFault e)
               {
                  ex = (Exception) PSProxyUtils.extractMultiOperationException(
                     new IPSReference[]{cRef}, METHOD.SAVE, e);
               }
               catch (RemoteException e)
               {
                  if (e instanceof PSLockFault)
                  {
                     ex = PSProxyUtils.convertFault((PSLockFault) e, 
                        "saveTemplateAssociations", 
                        PSObjectTypes.CONTENT_TYPE.toString(), 
                        null);
                  }
                  else
                     ex = e;
               }
               if(ex != null)
               {
                  results.add(ex);
                  hasErrors = true;
                  ex = null;
               }
            }
         } while (redo);
      }
      catch(MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (ServiceException e)
      {
         throw new RuntimeException(e);         
      }
      if(hasErrors)
      {
         throw new PSMultiOperationException(
            results.toArray(new Object[0]),
            details.toArray(new IPSReference[0]));
      }      
      
   }
   
   // see base
   public void setTemplateAssociations(
      Map<IPSReference, Collection<IPSReference>> associations)
      throws PSMultiOperationException
   {
      setTemplateAssociations(associations, true);
   }

   /**
    * Compares the supplied templates against the known set and stores the diffs
    * in the supplied map.
    * 
    * @param ctypeRef The owning content type. Assumed not <code>null</code>.
    * 
    * @param templateRefs The templates to be linked. Assumed not
    * <code>null</code>.
    * 
    * @param deletions First cleared, than, any deletions are added to this set.
    * Assumed not <code>null</code>.
    * 
    * @param additions First cleared, than, any additions are added to this set.
    * Assumed not <code>null</code>.
    */
   private void computeChanges(IPSReference ctypeRef,
         Collection<IPSReference> templateRefs,
         Collection<IPSReference> deletions, 
         Collection<IPSReference> additions)
   {
      deletions.clear();
      additions.clear();

      Collection<IPSReference> tmp = m_cache.get(ctypeRef);
      if (tmp == null)
         tmp = Collections.<IPSReference>emptySet();
      deletions.addAll(tmp);
      deletions.removeAll(templateRefs);
      
      additions.addAll(templateRefs);
      additions.removeAll(tmp);
   }

   /**
    * Helper method to convert the reference collection to long array of ids.
    * 
    * @param tRefColl reference collection, assumed not <code>null</code>.
    * @return converted array of longs, never <code>null</code>, may be
    * empty.
    */
   private long[] refCollectionToLongArray(Collection<IPSReference> tRefColl)
   {
      long[] templateIds = new long[tRefColl.size()];
      IPSReference[] tRefArray = tRefColl.toArray(new IPSReference[tRefColl
            .size()]);
      for (int i = 0; i < tRefArray.length; i++)
      {
         templateIds[i] = tRefArray[i].getId().longValue();
      }
      return templateIds;
   }
   
   /**
    * Returns the template model. Never <code>null</code>.
    * @see PSCoreFactory#getModel(Enum)
    */
   private IPSCmsModel getTemplateModel() throws PSModelException
   {
      return PSCoreFactory.getInstance().getModel(PSObjectTypes.TEMPLATE);
   }

   /**
    * Returns the content type model. Never <code>null</code>.
    * @see PSCoreFactory#getModel(Enum)
    */
   private IPSCmsModel getContentTypeModel() throws PSModelException
   {
      return PSCoreFactory.getInstance().getModel(PSObjectTypes.CONTENT_TYPE);
   }
   
   /**
    * Cache of content editor field editors.
    * [0] = For Search and View
    * [1] = For Display Formats
    * 
    */
   private PSContentEditorFieldCataloger[] m_fieldCat = 
      new PSContentEditorFieldCataloger[2];

   private static final String RESOURCE_NAME = 
      "sys_psxContentEditorCataloger/getControls.xml";
   
   /**
    * Gets the single instance of the template deletion tracker. The instance
    * is created the first time the method is called.
    * 
    * @return Never <code>null</code>.
    */
   private synchronized NodeUpdater getUpdater()
   {
      if (m_nodeUpdater == null)
         m_nodeUpdater = new NodeUpdater();
      return m_nodeUpdater;
   }

   /**
    * Tracks deleted templates and removes entries from the cache. Lazily
    * initialized via the {@link #getUpdater()} method, then never changed.
    */
   private NodeUpdater m_nodeUpdater;
   
   /**
    * This class registers itself as a listener on the template model so it can
    * be informed of template object deletes. When this happens, if the deleted
    * template is linked to any tracked content type, this association is 
    * removed from the cache
    */
   private class NodeUpdater implements IPSModelListener
   {
      /**
       * Only ctor. Registers itself w/ the template model.
       */
      public NodeUpdater()
      {
         try
         {
            PSCoreFactory.getInstance().getModel(PSObjectTypes.TEMPLATE)
               .addListener(this,
                     PSModelChangedEvent.ModelEvents.DELETED.getFlag());
         }
         catch (PSModelException e)
         {
            //should never happen
            throw new RuntimeException(e);
         }
      }

      /**
       * Checks for deletes of templates and removes entries from the cache as
       * necessary.
       */
      public void modelChanged(PSModelChangedEvent event)
      {
         if (event.getEventType() == PSModelChangedEvent.ModelEvents.DELETED)
         {
            for (IPSReference delRef : event.getSource())
            {
               for (Map.Entry<IPSReference, Collection<IPSReference>> entry : 
                  m_cache.entrySet())
               {
                  final Collection<IPSReference> validRefs =
                        new ArrayList<IPSReference>();
                  for (IPSReference test : entry.getValue())
                  {
                     if (!delRef.referencesSameObject(test))
                     {
                        validRefs.add(test);
                     }
                  }
                  entry.setValue(validRefs);
               }
            }
         }
      }
   }   
}

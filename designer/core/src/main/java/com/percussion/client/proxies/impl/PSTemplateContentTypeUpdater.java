/******************************************************************************
 *
 * [ PSTemplateContentTypeUpdater.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.error.PSClientException;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.PSLockException;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Updates content type/templates associations based on value of
 * {@link PSUiAssemblyTemplate#getNewContentTypes()}
 * Extracting this logic into separate class helps to share functionality
 * between test and real proxies. 
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateContentTypeUpdater
{

   /**
    * Calls proxy's save method passing the specified parameters, saves the 
    * template/content type associations. Correctly handles the multi-operation
    * exception.
    * @param client proxy to call save method on.
    */
   public void save(UpdaterClient client, IPSReference[] refs, Object[] data,
         boolean releaseLock)
         throws PSMultiOperationException, PSModelException
   {
      if (data == null || data.length == 0)
         return;
      assert refs.length == data.length;
      PSMultiOperationException multiException = null;
      try
      {
         client.doSave(refs, data, releaseLock);
      }
      catch (PSMultiOperationException e)
      {
         multiException = e;
      }
      for (int i = 0; i < data.length; i++)
      {
         final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) data[i];
         if (template != null && template.areNewContentTypesSpecified())
         {
            if (multiException != null && multiException.getResults()[i] != null)
            {
               // saving of this ref failed
               continue;
            }
            try
            {
               saveNewContentTypes(template, refs[i]);
            }
            catch (PSClientException e)
            {
               if (multiException == null)
               {
                  multiException =
                     new PSMultiOperationException(new Object[refs.length], refs);
               }
               multiException.getResults()[i] = e;
            }
         }
      }
      if (multiException != null)
      {
         throw multiException;
      }
   }

   /**
    * Saves template-content type association.
    * @param template the template to save. Assumed not <code>null</code>.
    * If associations are successfully saved the method sets
    * <code>newContentType</code> property of the template to <code>null</code>.
    * @param ref the template reference. Not <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with the server.
    * @throws PSLockException If the contenttype/template set is already locked.
    * @throws PSMultiOperationException if any errors while setting associations.
    */
   private void saveNewContentTypes(final PSUiAssemblyTemplate template,
         final IPSReference ref) throws PSModelException, PSLockException,
         PSMultiOperationException
   {
      assert ref != null;

      final Set<IPSReference> newContentTypes =
            new HashSet<IPSReference>(template.getNewContentTypes());
      final Map<IPSReference, Collection<IPSReference>> associations =
            updateExistingAssociations(ref, newContentTypes);
      newContentTypes.removeAll(associations.keySet());

      // add content types which did not have associations before
      for (final IPSReference contentTypeRef : newContentTypes)
      {
         associations.put(contentTypeRef, Collections.singleton(ref));
      }

      // lock the associations
      m_contentTypeModel.getTemplateAssociations(
            associations.keySet(), false, true);
      
      m_contentTypeModel.setTemplateAssociations(associations);
      template.setNewContentTypes(null);
   }

   /**
    * @return associations of content types which had associations before. 
    * @param ref the reference of the template being saved.
    * @param newContentTypes the new content types for the template being saved.
    * @throws PSModelException on failure.
    * @throws PSLockException if any of the needed data is locked.
    * Should not happen because here data is accessed read-only.
    */
   private Map<IPSReference, Collection<IPSReference>> updateExistingAssociations(
         final IPSReference ref, final Set<IPSReference> newContentTypes)
         throws PSModelException, PSLockException
   {
      // contains only content types with associations which need to be updated
      final Map<IPSReference, Collection<IPSReference>> newAssociations =
            new HashMap<IPSReference, Collection<IPSReference>>();

      // get all the content types, not just the associated ones because
      // we need to handle removed associations
      final Map<IPSReference, Collection<IPSReference>> associations =
            m_contentTypeModel.getTemplateAssociations(null, false, false);
      
      for (final IPSReference contentTypeRef : associations.keySet())
      {
         final Set<IPSReference> associatedTemplates =
               new HashSet<IPSReference>(associations.get(contentTypeRef));
         removeNotPersistedRefs(associatedTemplates);
         if (newContentTypes.contains(contentTypeRef))
         {
            associatedTemplates.add(ref);
            newAssociations.put(contentTypeRef, associatedTemplates);
         }
         else
         {
            if (associatedTemplates.remove(ref))
            {
               newAssociations.put(contentTypeRef, associatedTemplates);
            }
         }
      }
      return newAssociations;
   }

   /**
    * Removes not persisted references from the provided collection.
    * @param refs updateable collection to remove not persusted references from.
    */
   private void removeNotPersistedRefs(Set<IPSReference> refs)
   {
      for (Iterator i = refs.iterator(); i.hasNext();)
      {
         final IPSReference ref = (IPSReference) i.next();
         if (!ref.isPersisted())
         {
            i.remove();
         }
      }
   }

   /**
    * Returns value {@link #m_contentTypeModel} is initialized with.
    */
   private IPSContentTypeModel initContentTypeModel()
   {
      try
      {
         return (IPSContentTypeModel) 
               PSCoreFactory.getInstance().getModel(PSObjectTypes.CONTENT_TYPE);
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }
   
   /**
    * A way to call actual proxy "save" operation.
    */
   public interface UpdaterClient
   {
      /**
       * @see PSCmsModelProxy#save(IPSReference[], Object[], boolean)
       */
      public void doSave(IPSReference[] refs, Object[] data, boolean releaseLock)
         throws PSMultiOperationException, PSModelException;
   }

   /**
    * Content model used to manipulate the associations.
    */
   IPSContentTypeModel m_contentTypeModel = initContentTypeModel();
}

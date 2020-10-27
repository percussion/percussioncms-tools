/******************************************************************************
 *
 * [ PSTemplateModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.IPSTemplateModel;
import com.percussion.client.models.PSLockException;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Assembly template model.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateModel extends PSCmsModel implements IPSTemplateModel
{
   /**
    * The constructor. Required by the model framework.
    */
   public PSTemplateModel(String name, String description,
         IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   /**
    * Returns the content types associated with the template.
    * @param templateRef the template to return content types for.
    * Never <code>null</code>. 
    * @param force whether to force data loading.
    * @return collection of the content types.
    */
   public Set<IPSReference> getContentTypes(
         final IPSReference templateRef, final boolean force)
         throws PSModelException
   {
      // return content types list attached to the template if exists
      try
      {
         final PSUiAssemblyTemplate template =
               (PSUiAssemblyTemplate) load(templateRef, false, false);
         if (template.areNewContentTypesSpecified())
         {
            return template.getNewContentTypes();
         }
      }
      catch (Exception e)
      {
         throw new PSModelException(e);
      }

      // load the associations from content types
      try
      {
         final Map<IPSReference, Collection<IPSReference>> associations = 
               getContentTypeModel().getTemplateAssociations(null, force, false);
         final Set<IPSReference> results = new HashSet<IPSReference>();
         for (IPSReference contentTypeRef : associations.keySet())
         {
            if (associations.get(contentTypeRef).contains(templateRef))
            {
               results.add(contentTypeRef);
            }
         }
         return results;
      }
      catch (PSLockException e)
      {
         //will never happen because we aren't locking
         throw new RuntimeException("Should never happen.");
      }
   }
   
   /**
    * Convenience method to retrieve content type model.
    * 
    * @return Never <code>null</code>.
    */
   protected IPSContentTypeModel getContentTypeModel() throws PSModelException
   {
      return (IPSContentTypeModel) PSCoreFactory.getInstance().getModel(
            PSObjectTypes.CONTENT_TYPE);
   }
}

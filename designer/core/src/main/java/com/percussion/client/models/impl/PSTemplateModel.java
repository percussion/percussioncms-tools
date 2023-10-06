/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
         final Set<IPSReference> results = new HashSet<>();
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

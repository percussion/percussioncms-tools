/******************************************************************************
 *
 * [ PSTemplateAssociateContentTypeNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSContentTypeModel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Handler for the folder that contains the content types that the template
 * ancestor is used by.
 * 
 * @author paulhoward
 */
public class PSTemplateAssociateContentTypeNodeHandler extends
      PSLinkNodeHandler
{
   /**
    * The only ctor, signature required by framework. Parameters passed to 
    * base class.
    */
   public PSTemplateAssociateContentTypeNodeHandler(Properties props,
         String iconPath, PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   //see base class method for details
   @Override
   protected Collection<IPSReference> doDeleteAssociations(
         IPSReference templateRef, Collection<IPSReference> ctypeRefs)
      throws Exception
   {
      if (null == templateRef)
      {
         throw new IllegalArgumentException("parentTemplateRef cannot be null");  
      }
      if (null == ctypeRefs || ctypeRefs.isEmpty())
         return Collections.emptySet();
      
      IPSContentTypeModel model = 
         (IPSContentTypeModel) getModel(PSObjectTypes.CONTENT_TYPE);
      assert (templateRef.getObjectType().equals(PSObjectTypeFactory
            .getType(PSObjectTypes.TEMPLATE,
                  PSObjectTypes.TemplateSubTypes.SHARED)));
      
      Map<IPSReference, Collection<IPSReference>> associations = model
            .getTemplateAssociations(ctypeRefs, false, true);

      Map<IPSReference, Collection<IPSReference>> results = 
         new HashMap<IPSReference, Collection<IPSReference>>();

      for (IPSReference ctypeRef : associations.keySet())
      {
         assert (ctypeRef.getObjectType().equals(PSObjectTypeFactory
               .getType(PSObjectTypes.CONTENT_TYPE)));
         Collection<IPSReference> associatedTemplateRefs = associations.get(ctypeRef);
         associatedTemplateRefs.remove(templateRef);
         results.put(ctypeRef, associatedTemplateRefs);
      }
      model.setTemplateAssociations(results);
      return ctypeRefs;
   }

   /**
    * @inheritDoc Adds the supplied content types as using this template. If the
    * content type is already present, it is skipped.
    */
   @Override
   protected Collection<IPSReference> doSaveAssociations(IPSReference templateRef,
         Collection<IPSReference> ctypeRefs)
      throws Exception
   {
      if (null == templateRef)
      {
         throw new IllegalArgumentException("parentTemplateRef cannot be null");  
      }
      if (null == ctypeRefs || ctypeRefs.isEmpty())
         return Collections.emptyList();
      
      if (PSContentTypeAssociateTemplateNodeHandler
            .processContentTypeTemplateAssociations(Collections
                  .singleton(templateRef), ctypeRefs))
      {
         return ctypeRefs;
      }
      return Collections.emptySet();
   }
}

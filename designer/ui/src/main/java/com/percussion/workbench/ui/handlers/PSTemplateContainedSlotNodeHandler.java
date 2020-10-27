/******************************************************************************
 *
 * [ PSTemplateContainedSlotNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.objectstore.IPSUiAssemblyTemplate;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workbench.ui.PSModelTracker;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * Handler for the folder that contains the slots that an ancestor template is
 * associated with.
 * 
 * @author paulhoward
 */
public class PSTemplateContainedSlotNodeHandler extends PSLinkNodeHandler
{
   /**
    * The only ctor, signature required by framework. Parameters passed to 
    * base class.
    */
   public PSTemplateContainedSlotNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   //see base class method for details
   @Override
   protected Collection<IPSReference> doDeleteAssociations(
         IPSReference templateRef, Collection<IPSReference> slotRefs)
      throws Exception
   {
      if (null == templateRef)
      {
         throw new IllegalArgumentException("templateRef cannot be null");  
      }
      if (slotRefs == null || slotRefs.isEmpty())
         return Collections.emptySet();
      
      assert (templateRef.getObjectType().getPrimaryType()
            .equals(PSObjectTypes.TEMPLATE));
      
      boolean success = false;
      try
      {
         IPSUiAssemblyTemplate templateData = (IPSUiAssemblyTemplate) 
            PSModelTracker.getInstance().load(templateRef, true);
         Set<IPSGuid> slotGuids = templateData.getSlotGuids(); 
         for (IPSReference slotRef : slotRefs)
         {
            slotGuids.remove(slotRef.getId());
         }
         templateData.setSlotGuids(slotGuids);
         PSModelTracker.getInstance().save(templateRef);
         success = true;
         return slotRefs;
      }
      finally
      {
         if (!success)
            PSModelTracker.getInstance().releaseLock(templateRef);
      }
   }

   /**
    * @inheritDoc
    * Adds the supplied slots as being contained by this template. If the slot
    * is already present, it is skipped.
    */
   @Override
   protected Collection<IPSReference> doSaveAssociations(
         IPSReference templateRef, Collection<IPSReference> slotRefs)
      throws Exception
   {
      if (null == templateRef)
      {
         throw new IllegalArgumentException("templateRef cannot be null");  
      }
      if (slotRefs == null || slotRefs.isEmpty())
         return Collections.emptyList();
      
      assert (templateRef.getObjectType().getPrimaryType()
            .equals(PSObjectTypes.TEMPLATE));
      
      boolean success = false;
      try
      {
         IPSUiAssemblyTemplate templateData = (IPSUiAssemblyTemplate) 
            PSModelTracker.getInstance().load(templateRef, true);
         Set<IPSGuid> slotsToAdd = templateData.getSlotGuids(); 
         for (IPSReference slotRef : slotRefs)
         {
            slotsToAdd.add(slotRef.getId());
         }
         templateData.setSlotGuids(slotsToAdd);
         PSModelTracker.getInstance().save(templateRef);
         success = true;
         return slotRefs;
      }
      finally
      {
         if (!success)
            PSModelTracker.getInstance().releaseLock(templateRef);
      }
   }
}

/*******************************************************************************
 *
 * [ PSSlotCategoryManager.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.views.hierarchy.categories;

import com.percussion.client.PSModelException;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.workbench.ui.model.IPSHomeNodeManager;

import java.util.HashMap;
import java.util.Map;

/**
 * This class knows how to match a slot to one of the nodes in the 
 * assembly_viewHierarchyDef.xsd definition file. The structure is as follows:
 * <pre>
 * Slots
 *    - Navigation
 *    - System
 * </pre>
 *
 * @author paulhoward
 */
public class PSSlotCategoryManager implements IPSHomeNodeManager
{
   //see interface
   public boolean isHomeNode(Map<String, String> props, Object data)
   {
      // TODO Auto-generated method stub
      if ( null == data)
      {
         throw new IllegalArgumentException(
               "data cannot be null and must be a PSTemplateSlot");  
      }
      PSTemplateSlot slot = (PSTemplateSlot) data;
      
      if (props == null)
         props = new HashMap<String,String>();
      
      String cat = props.get("category");
      if (cat == null)
         cat = "";
//      try
//      {
         if (cat.equals("navigation"))
         {
            //todo - determine if a nav slot
//            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
//                  PSObjectTypes.CONFIGURATION_FILE);
//            Collection<IPSReference> configs = model.catalog(false);
//            IPSReference navConfigRef = null;
//            for (IPSReference ref : configs)
//            {
//               if (ref
//                     .getObjectType()
//                     .getSecondaryType()
//                     .equals(
//                           PSObjectTypes.ConfigurationFileSubTypes.NAVIGATION_PROPERTIES))
//               {
//                  navConfigRef = ref;
//               }
//            }
//            
//            if (navConfigRef == null)
//            {
//               //shouldn't happen
//               throw new RuntimeException(
//                     "Couldn't find navigation configuration.");
//            }

            return false;
         }
         else if (cat.equals("system"))
         {
            return slot.isSystemSlot();
         }

         return true;
//      }
//      catch (PSModelException e)
//      {
//         // todo - better exception?
//         throw new RuntimeException(e);
//      }
   }

   //see interface
   public void modifyForHomeNode(Map<String, String> props, Object data) throws PSModelException
   {
   // TODO Auto-generated method stub

   }

}

/******************************************************************************
 *
 * [ PSHierarchyNodeDelegate.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import com.percussion.client.PSObjectType;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.utils.guid.IPSGuid;

/**
 * This is a placeholder for another object for a leaf within the hierarchy. It
 * contains enough information to recreate the object.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSHierarchyNodeDelegate 
{
   /**
    * @param name
    * @param objectType
    * @param id The unique identifier for the object for which this instance is
    *           the delegate for storage of the hierarchy information.
    */
   public PSHierarchyNodeDelegate(String name, PSObjectType objectType,
      IPSGuid id)
   {
      super();
   }

   /**
    * @param source
    * @param objectType
    * @param id
    */
   public PSHierarchyNodeDelegate(PSHierarchyNode source,
      PSObjectType objectType, IPSGuid id)
   {
      super();
   }

}

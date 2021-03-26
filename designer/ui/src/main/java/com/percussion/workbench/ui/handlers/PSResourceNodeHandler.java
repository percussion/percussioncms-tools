/******************************************************************************
 *
 * [ PSResourceNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;
import org.eclipse.swt.dnd.DragSourceListener;

import java.util.Collection;
import java.util.Properties;

/**
 * This class prevents dragging of the root node (which is the application).
 *
 * @author paulhoward
 */
public class PSResourceNodeHandler extends PSFileNodeHandler
{
   /**
    * Ctor required by framework. See base for param descriptions.
    */
   public PSResourceNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * Extended to prevent dragging of application node.
    *
    * @author paulhoward
    */
   protected class ResourceDragHandler extends FileDragHandler
   {
      /**
       * If any nodes are the root, dragging is disallowed.
       * 
       * @return <code>true</code> if all nodes are not an application node,
       * <code>false</code> otherwise.
       */
      @Override
      protected boolean canStartDrag(Collection<PSUiReference> nodes)
      {
         for (PSUiReference node : nodes)
         {
            if (((IPSHierarchyNodeRef) node.getReference()).getParent() == null)
               return false;
         }
         return true;
      }
   }

   //see base class method for details
   @Override
   public DragSourceListener getDragHandler()
   {
      if (ms_dragHandler == null)
         ms_dragHandler = new ResourceDragHandler(); 
      return ms_dragHandler;
   }

   /**
    * The handler that manages the drag operations. Created lazily by the
    * {@link #getDragHandler()} method, then never modified.
    */
   private static DragSourceListener ms_dragHandler;
}

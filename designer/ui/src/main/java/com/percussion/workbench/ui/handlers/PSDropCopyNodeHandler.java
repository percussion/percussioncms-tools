/******************************************************************************
 *
 * [ PSDropCopyNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.model.IPSDropHandler;
import org.eclipse.swt.dnd.DND;

import java.util.Properties;

/**
 * This DND handler only accepts drops as a copy, not move or link.
 *
 * @author paulhoward
 */
public class PSDropCopyNodeHandler extends PSDropNodeHandler
{
   /**
    * Ctor required by framework.
    * 
    * @param props May be <code>null</code>.
    * 
    * @param iconPath May be <code>null</code> or empty.
    */
   public PSDropCopyNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   //see base class
   @Override
   public IPSDropHandler getDropHandler()
   {
      if (ms_handler == null)
         ms_handler = new CopyOnlyHandler(); 
      return ms_handler;
   }

   /**
    * Overridden to prevent move or reference link. Only copy is allowed.
    *
    * @author paulhoward
    */
   protected class CopyOnlyHandler extends NodeDropHandler
   {
      // see class description
      @Override
      public int getValidDndOperation(int desiredOp)
      {
         return (desiredOp == DND.DROP_MOVE || desiredOp == DND.DROP_LINK) 
               ? DND.DROP_COPY : desiredOp;
      }
   }

   /**
    * The handler that manages the drop operations. Created lazily by the
    * {@link #getDropHandler()} method, then never modified.
    */
   private static IPSDropHandler ms_handler;
}

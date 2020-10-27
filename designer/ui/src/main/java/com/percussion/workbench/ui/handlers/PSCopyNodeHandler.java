/******************************************************************************
 *
 * [ PSCopyNodeHandler.java ]
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
 * This node handler provides a custom drop handler that only supports copy, not
 * move.
 *
 * @author paulhoward
 */
public class PSCopyNodeHandler extends PSDeclarativeNodeHandler
{
   /**
    * Required by framework. See base for param description.
    */
   public PSCopyNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * @inheritDoc 
    * A move request is converted to a copy.
    */
   @Override
   public IPSDropHandler getDropHandler()
   {
      if (m_dropHandler == null) 
      {
         m_dropHandler = new NodeDropHandler()
         {
            //see base class method for details
            @Override
            public int getValidDndOperation(int desiredOp)
            {
               return desiredOp == DND.DROP_MOVE ? DND.DROP_COPY : desiredOp;
            }
            
         };
      }
      return m_dropHandler;
   }
   
   /**
    * A drop handler that only supports copy, not move. Lazily created, then
    * never modified.
    */
   private IPSDropHandler m_dropHandler;
}

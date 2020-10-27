/******************************************************************************
 *
 * [ PSDropNodeHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.PSObjectType;
import org.eclipse.swt.dnd.DragSourceListener;

import java.util.Properties;

/**
 * This DND handler only accepts drops, no drag op is supported.
 *
 * @author paulhoward
 */
public class PSDropNodeHandler extends PSDeclarativeNodeHandler
{
   /**
    * Ctor required by framework.
    * 
    * @param props May be <code>null</code>.
    * 
    * @param iconPath May be <code>null</code> or empty.
    */
   public PSDropNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * These nodes don't support being dragged, only dropped upon.
    * 
    * @return Always <code>null</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public DragSourceListener getDragHandler()
   {
      return null;
   }
}

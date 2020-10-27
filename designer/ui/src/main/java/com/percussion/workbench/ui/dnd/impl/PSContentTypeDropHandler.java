/*******************************************************************************
 *
 * [ PSContentTypeDropHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.dnd.impl;

import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

/**
 * @version 6.0
 * @author Paul Howard
 */
public class PSContentTypeDropHandler extends ViewerDropAdapter
{
   /**
    * 
    *
    */
   public PSContentTypeDropHandler()
   {
      super(null);
   }

   //see base class
   public boolean performDrop(Object source)
   {
      return false;
   }

   //see base class
   public boolean validateDrop(Object source, int operations, 
         TransferData transferData)
   {
      return false;
   }
}
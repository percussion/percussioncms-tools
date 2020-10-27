/******************************************************************************
*
* [ IPSTableLayoutListener.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.layouts;

public interface IPSTableLayoutListener
{
   /**
    * Called when the table layout has just resisized one or
    * more table columns.
    * @param event never <code>null</code>.
    */
   public void columnsResized(PSTableLayoutEvent event);
}

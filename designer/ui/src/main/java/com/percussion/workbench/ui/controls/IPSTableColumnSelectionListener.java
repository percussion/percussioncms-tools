/******************************************************************************
*
* [ IPSTableColumnSelectionListener.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

/**
 * A listener used with <code>PSSortableTable</code> to listen for when
 * a column is selected.
 */
public interface IPSTableColumnSelectionListener
{
   
   /**
    * Called when a column selection was made in a table
    * @param event the table event, never <code>null</code>.
    */
   public void columnSelected(PSTableEvent event);

}

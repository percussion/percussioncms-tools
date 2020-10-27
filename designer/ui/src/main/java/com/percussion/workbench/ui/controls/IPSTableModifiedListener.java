/******************************************************************************
*
* [ IPSTableModifiedListener.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.controls;

/**
 * Used with a sortable table to indicate when one of its cells
 * is modified.
 */
public interface IPSTableModifiedListener
{
   /**
    * Is called when a cell within the table is modified.
    * @param event
    */
   public void tableModified(PSTableEvent event);
}

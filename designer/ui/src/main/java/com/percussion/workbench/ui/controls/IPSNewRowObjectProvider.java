/******************************************************************************
*
* [ IPSNewRowObjectProvider.java ]
*
* COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

/**
 * This interface is used with <code>PSSortableTableComposite</code> so that
 * the table can create a new object for a table row when the 
 * <code>doInsert</code> method is called.
 */
public interface IPSNewRowObjectProvider
{
   
   /**
    * This must return a new instance of an object that is the underlying 
    * object that the table row represents.
    * @return a new object instance, cannot be <code>null</code>.
    */
   public Object newInstance();
   
   /**
    * This method should be implemented to determine if the object passed
    * in is considered empty. Empty usually means that all the columns
    * in the table row that this object backs are blank.
    * @param obj the object to be checked, never <code>null</code>.
    * @return <code>true</code> if this object is considered empty.
    */
   public boolean isEmpty(Object obj);
   
}

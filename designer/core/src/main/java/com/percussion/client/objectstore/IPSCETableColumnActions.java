/******************************************************************************
*
* [ IPSCETableColumnActions.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.design.objectstore.PSField;

import java.util.Map;

/**
 * This interface is implemented by objects that need to 
 * tell the content editor table handler what action should
 * occur on a <code>PSField</code> objects associated table
 * column. Currently there are two available actions 
 * <b>ALTER</b> and <b>DELETE</b>.
 */
public interface IPSCETableColumnActions
{
   /**
    * Adds a field/action mapping for the CE table handler to
    * process.
    * @param field the field whose associated column the action should
    * be proccessed on. Cannot be <code>null</code>.
    * @param action the action type
    */
   public void addColumnAction(PSField field, int action);
   
   /**
    * Sets the field/action mappings for the CE table handler to
    * process. Replaces all existing mappings.
    * @param actions the map of field/action pairs. If <code>null</code>
    * then the underlying map will be cleared. 
    */
   public void setColumnActions(Map<PSField, Integer> actions);
   
   /**
    * Removes an existing field/action mapping
    * @param field Cannot be <code>null</code>.
    */
   public void removeColumnAction(PSField field);
   
   /**
    * Retrieves all the field/action mappings for the CE table handler.
    * @return never <code>null</code>, may be empty.
    */
   public Map<PSField, Integer> getColumnActions();
   
   /**
    * Action that tells the handler that the column associated
    * with the specified field should be altered.
    */
   public static final int COLUMN_ACTION_ALTER = 1;
   
   /**
    * Action that tells the handler that the column associated
    * with the specified field should be removed.
    */
   public static final int COLUMN_ACTION_DELETE = 2;
   

}

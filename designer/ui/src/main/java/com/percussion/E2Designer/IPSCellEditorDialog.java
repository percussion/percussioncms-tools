/*[ IPSCellEditorDialog.java ]*************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.awt.event.ActionListener;

/**
 * This interface should be implemented by dialog classes that can act as an
 * editor of table cell data.
 */
public interface IPSCellEditorDialog
{
   /**
    * Return the value to be set in the cell when the dialog is dismissed.
    *
    * @return the value that should be set in the cell once the dialog is
    * dismissed, may be <code>null</code> in case CANCEL button was pressed.
    */
   public Object getData();

   /**
    * The current data in the dialog should be set using the specified
    * <code>value</code>.
    *
    * @param data the current value to display when the dialog appears,
    * may not be <code>null</code>.
    */
   public void setData(Object value);

   /**
    * Set whether the dialog should be visible.
    *
    * @param visible <code>true</code> if the dialog should be made visible,
    * <code>false</code> otherwise
    */
   public void setVisible(boolean visible);

   /**
    * Add a listener for handling OK button pressed event.
    *
    * @param listener the listener to call when the OK button is pressed, may
    * not be <code>null</code>
    */
   public void addOkListener(ActionListener listener);

   /**
    * Add a listener for handling CANCEL button pressed event.
    *
    * @param listener the listener to call when the CANCEL button is pressed,
    * may not be <code>null</code>
    */
    public void addCancelListener(ActionListener listener);

   /**
    * Remove the listener from handling OK button pressed event.
    *
    * @param listener the listener to remove, may not be <code>null</code>
    */
   public void removeOkListener(ActionListener listener);

   /**
    * Remove the listener from handling CANCEL button pressed event.
    *
    * @param listener the listener to remove, may not be <code>null</code>
    */
   public void removeCancelListener(ActionListener listener);
}

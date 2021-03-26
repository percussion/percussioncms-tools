/*[ UTCheckBoxCellEditor.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;


import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.EventObject;


/**
 * Provides a cell editor based on a JCheckBox, backed by a Boolean value
 * in the table model.  Allows listeners to be added to both the cell for
 * editing events, as well as to the checkbox itself, for events triggered
 * off of the checking and unchecking of the checkbox while the cell remains
 * in edit mode. 
 */
public class UTCheckBoxCellEditor extends UTCheckBoxCellRenderer
   implements TableCellEditor
{
   private ChangeEvent m_changeEvent = new ChangeEvent(this);
   private int currentRow, currentColumn;
   private JTable currentTable;

   public UTCheckBoxCellEditor() {
   }

   public void cancelCellEditing() {
   }

   /**
    * Get the current state of the checkbox
    */
   public Object getCellEditorValue() {
      return getCheckBox().isSelected() ?
                        Boolean.TRUE :
                        Boolean.FALSE;
   }

   /**
    * Return a checkbox to edit the data value
    */
   public Component getTableCellEditorComponent(
                                 JTable table,
                                 Object value,
                                 boolean isSelected,
                                 int row,
                                 int column)
   {
      if (value == null || value.toString().trim().equals(""))
         value = Boolean.FALSE;

      currentRow = row;
      currentColumn = column;
      currentTable = table;

      // toggle the state of the data so activating
      //  the editor looks like a toggle
      Boolean antiValue =
            ((Boolean)value).booleanValue() ?
                        Boolean.FALSE:Boolean.TRUE;

      JCheckBox cb = getCheckBox();
      cb.setSelected( ((Boolean)value)/*antiValue*/.booleanValue());

      // add the listeners to the checkbox
      addActionListeners(cb);

      return getTableCellRendererComponent(
                        table,value /*antiValue*/,isSelected,
                        true,row,column);
   }

   /**
    * Allow all cells to be edited
    */
   public boolean isCellEditable(EventObject anEvent) {
         return true;
   }

   /**
    * Always select the cell being edited
    */
   public boolean shouldSelectCell(EventObject anEvent) {
         return true;
   }

   /**
    * Always allow the edit to stop
    */
   public boolean stopCellEditing() {
      // save the contents into the model
      fireEditingStopped();
      return true;
   }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
   protected void fireEditingStopped()
   {
      // Guaranteed to return a non-null array
      Object[] listeners = m_listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2)
      {
       if (listeners[i]==CellEditorListener.class)
          ((CellEditorListener)listeners[i+1]).editingStopped(m_changeEvent);
      }
   }


    public void addCellEditorListener(CellEditorListener l)
   {
     m_listenerList.add(CellEditorListener.class, l);
   }

   // implements javax.swing.CellEditor
   public void removeCellEditorListener(CellEditorListener l)
   {
     m_listenerList.remove(CellEditorListener.class, l);
   }


   /**
    * Add an action listener to list of listneners.  All action
    * listeners will be added to each checkbox returned by
    * getTableCellEditorComponent
    *
    * @param l the ActionListener to add
    */
   public void addCheckBoxListener(ActionListener l)
   {
     m_listenerList.add(ActionListener.class, l);
   }

   /**
    * Remove an action listener from the list of listeners.
    *
    * @param l the ActionListener to add
    */
   public void removeCheckBoxListener(ActionListener l)
   {
     m_listenerList.remove(ActionListener.class, l);
   }

   /**
    * Adds any action listeners to the checkbox
    *
    * @param cb the checkbox
    */
   private void addActionListeners(JCheckBox cb)
   {

       // Guaranteed to return a non-null array
       Object[] listeners = m_listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length-2; i>=0; i-=2)
       {
          if (listeners[i]==ActionListener.class)
            cb.addActionListener((ActionListener)listeners[i+1]);
       }
   }

   /**
    * The list of all listeners registered with this object
    */
   protected EventListenerList     m_listenerList = new EventListenerList();

}


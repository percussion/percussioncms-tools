/******************************************************************************
 *
 * [ UTRadioButtonCellEditor.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
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
public class UTRadioButtonCellEditor extends UTRadioButtonCellRenderer
   implements TableCellEditor
{
   /**
    * Default ctor
    */
   public UTRadioButtonCellEditor()
   {
   }

   public void cancelCellEditing()
   {
   }

   /**
    * Get the current state of the checkbox
    */
   public Object getCellEditorValue()
   {
      return getRadioButton().isSelected() ? Boolean.TRUE : Boolean.FALSE;
   }

   /**
    * Return a checkbox to edit the data value
    */
   public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column)
   {
      if (value == null || value.toString().trim().equals(""))
         value = Boolean.FALSE;


      JRadioButton rb = getRadioButton();
      rb.setSelected(((Boolean) value).booleanValue());

      // add the listeners to the checkbox
      removeActionListeners(rb);
      addActionListeners(rb);
      
      // select the row
      table.setRowSelectionInterval(row, row);

      return getTableCellRendererComponent(table, value,
         isSelected, true, row, column);
   }

   /**
    * Only can edit non-selected
    */
   public boolean isCellEditable(EventObject anEvent)
   {
      if (anEvent == null);
      return true;
   }

   /**
    * Only select the cell if editable
    */
   public boolean shouldSelectCell(EventObject anEvent)
   {
      if (anEvent == null);
      return true;
   }

   /**
    * Always allow the edit to stop
    */
   public boolean stopCellEditing()
   {
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
      for (int i = listeners.length - 2; i >= 0; i -= 2)
      {
         if (listeners[i] == CellEditorListener.class)
            ((CellEditorListener) listeners[i + 1])
               .editingStopped(m_changeEvent);
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
   public void addRadioButtonListener(ActionListener l)
   {
      m_listenerList.add(ActionListener.class, l);
   }

   /**
    * Remove an action listener from the list of listeners.
    *
    * @param l the ActionListener to add
    */
   public void removeRadioButtonListener(ActionListener l)
   {
      m_listenerList.remove(ActionListener.class, l);
   }

   /**
    * Adds any action listeners to the radio button
    *
    * @param rb the radio button
    */
   private void addActionListeners(JRadioButton rb)
   {

      // Guaranteed to return a non-null array
      Object[] listeners = m_listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length - 2; i >= 0; i -= 2)
      {
         if (listeners[i] == ActionListener.class)
            rb.addActionListener((ActionListener) listeners[i + 1]);
      }
   }
   
   /**
    * Removes aall action listeners from the radio button
    *
    * @param rb the radio button
    */
   private void removeActionListeners(JRadioButton rb)
   {
      ActionListener[] listeners = rb.getActionListeners();
      for (int i = 0; i < listeners.length; i++)
      {
         rb.removeActionListener(listeners[i]);
      }
   }   

   /**
    * The list of all listeners registered with this object
    */
   protected EventListenerList m_listenerList = new EventListenerList();

   /**
    * Cached change event.
    */
   private ChangeEvent m_changeEvent = new ChangeEvent(this);
}

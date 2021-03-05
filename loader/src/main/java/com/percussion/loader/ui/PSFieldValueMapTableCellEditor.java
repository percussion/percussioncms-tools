/*[ PSFieldValueMapTableCellEditor.java ]**************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSEntry;
import com.percussion.loader.extractor.PSVariableEvaluator;
import com.percussion.loader.objectstore.PSFieldProperty;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;


/**
 * Specialized cell editor to be used with the <code>PSFieldValueMapTableModel
 * </code>. Editor components include JComboBox, JTextField, and JTextArea.
 * The comboBoxes are dynamically loaded with their choices as they are dependent
 * on the selection of the other columns.
 *
 * The Field column will only show field choices that have yet to be selected.
 * The value type column will depend on the selection in the field column.
 * If a field has choices and a pre-defined value type, then that value type
 * will be displayed and made not editable. Otherwise a list of value types will
 * be available.
 * The value depends on both the field and value type selections. If the
 * field has choices, then they will be displayed. If the value type is
 * an extractor variable, then a list of variables will be presented. Anything
 * else will display a text field.
 */
public class PSFieldValueMapTableCellEditor extends AbstractCellEditor
   implements TableCellEditor, CellEditorListener, KeyListener
{

   /**
    * Constructs a new cell editor
    */
   public PSFieldValueMapTableCellEditor()
   {
      addCellEditorListener(this);
      m_comboEditor.addKeyListener(this);
      m_textEditor.addKeyListener(this);
   }

   // implements TableCellEditor interface method
   public Component getTableCellEditorComponent(
      JTable table,
      Object value,
      boolean isSelected,
      int row,
      int column)
   {
      m_model =
         (PSFieldValueMapTableModel)table.getModel();
      m_currentRow = row;
      m_currentColumn = column;

      switch(column)
      {
         case PSFieldValueMapTableModel.COL_FIELD:
            return getFieldComponent(value);
         case PSFieldValueMapTableModel.COL_VALUE_TYPE:
            return getValueTypeComponent(value);
         case PSFieldValueMapTableModel.COL_VALUE:
            return getValueComponent(value);

      }
      return null;
   }

   // implements CellEditor interface method
   public Object getCellEditorValue()
   {
      if(m_currentEditor == m_comboEditor)
      {
         return m_comboEditor.getSelectedItem();
      }
      if(m_currentEditor == m_textEditor)
      {
         return m_textEditor.getText();
      }
      if(m_currentEditor == m_textNoEdit)
      {
         return m_textNoEdit.getText();
      }
      return null;
   }

   /**
    * Returns field name editor component loaded with all
    * available field names
    *
    * @param value the current selected value for this editor.
    *
    * @return the editor component, never <code>null</code>.
    */
   private Component getFieldComponent(Object value)
   {
      Iterator it = m_model.getAvailableFields();
      m_currentEditor = m_comboEditor;
      m_comboEditor.removeAllItems();
      while(it.hasNext())
         m_comboEditor.addItem(it.next());
      if(null != value)
         m_comboEditor.setSelectedItem(value);
      return m_comboEditor;
   }

   /**
    * Returns value type editor component loaded with either a list
    * of value types or an unchangeable value type.
    *
    * @param value the current selected value for this editor, it may be
    *    <code>null</code>.
    *
    * @return the editor component, never <code>null</code>.
    */
   private Component getValueTypeComponent(Object value)
   {
      PSContentField field =
         (PSContentField)m_model.getValueAt(m_currentRow,
             PSFieldValueMapTableModel.COL_FIELD);
      if(null != field)
      {
         if(field.hasChoices())
         {
            m_currentEditor = m_textNoEdit;
            m_textNoEdit.setText(field.getChoicesValueType());
            m_textNoEdit.setEditable(false);
            return m_textNoEdit;
         }
         else
         {
            m_currentEditor = m_comboEditor;
            m_comboEditor.removeAllItems();
            for(int i = 0; i < ms_valueTypes.length; i++)
               m_comboEditor.addItem(ms_valueTypes[i]);
            if(m_model.isXPathTypeAllowed())
               m_comboEditor.addItem(PSFieldProperty.VALUE_TYPE_XPATH);
            if(null != value)
               m_comboEditor.setSelectedItem(value);
            return m_comboEditor;
         }
      }
      return null;
   }

   /**
    * Returns value editor component loaded with either a list of choices,
    * a list of variables or a text field.
    *
    * @param value the current selected value for this editor.
    *
    * @return the editor component, never <code>null</code>.
    *
    * @todo Add component for multi line XPath queries that
    * pops up a dialog with a text area to enter multi line
    * queries.
    */
   private Component getValueComponent(Object value)
   {
      PSContentField field =
         (PSContentField)m_model.getValueAt(m_currentRow,
             PSFieldValueMapTableModel.COL_FIELD);

      String valueType =
         (String)m_model.getValueAt(m_currentRow,
             PSFieldValueMapTableModel.COL_VALUE_TYPE);

      if(null != field && null != valueType && valueType.trim().length() > 0)
      {
         if(field.hasChoices())
         {
            m_currentEditor = m_comboEditor;
            m_comboEditor.removeAllItems();
            Iterator choices = field.getChoices();
            while(choices.hasNext())
            {
               PSEntry entry = (PSEntry)choices.next();
               m_comboEditor.addItem(entry.getValue());
            }
            if(null != value)
               m_comboEditor.setSelectedItem(value);
            return m_comboEditor;
         }
         else if(valueType.equals(PSFieldProperty.VALUE_TYPE_VARIABLE))
         {
            m_currentEditor = m_comboEditor;
            m_comboEditor.removeAllItems();
            for(int i = 0; i < ms_variables.length; i++)
               m_comboEditor.addItem(ms_variables[i]);
            if(null != value)
               m_comboEditor.setSelectedItem(value);
            return m_comboEditor;
         }
         else
         {
            m_currentEditor = m_textEditor;
            if(null == value)
            {
               m_textEditor.setText("");
            }
            else
            {
               m_textEditor.setText((String)value);
            }
            return m_textEditor;
         }
      }
      return null;
   }

   /**
    * Checks whether a cell is editable or not. In general the cell is
    * editable if the event is a mouse double-click
    *
    * @param event the event, assumed not to be <code>null</code> as this
    * method is called by Swing.
    *
    * @return <code>true</code> if the cell is editable, otherwise <code>
    * false</code>
    */
   public boolean isCellEditable(EventObject event)
   {
      if (event instanceof MouseEvent)
      {
         MouseEvent e = (MouseEvent)event;
         return e.getClickCount() >= 2;
      }
      return false;
   }

   // implements the CellEditorListener interface
   public void editingCanceled(ChangeEvent event)
   {
      // Empty implementation
   }

   /**
    * Implements the <code>CellEditorListener<code> interface method.
    * When editing is stopped the table model is updated with the new
    * value from the editor.
    *
    * @param event <code>ChangeEvent</code> caught by this listener.
    * Never <code>null</code>.
    */
   public void editingStopped(ChangeEvent event)
   {
     if(null != getCellEditorValue())
        m_model.setValueAt(m_currentRow, m_currentColumn, getCellEditorValue());
   }

   // implements KeyListener interface method
   public void keyPressed(KeyEvent event)
   {
      Iterator it = m_keyListeners.iterator();
      while(it.hasNext())
      {
         KeyListener listener = (KeyListener)it.next();
         listener.keyPressed(event);
      }
   }

   // implements KeyListener interface method
   public void keyReleased(KeyEvent event)
   {
      Iterator it = m_keyListeners.iterator();
      while(it.hasNext())
      {
         KeyListener listener = (KeyListener)it.next();
         listener.keyReleased(event);
      }
   }

   // implements KeyListener interface method
   public void keyTyped(KeyEvent event)
   {
      Iterator it = m_keyListeners.iterator();
      while(it.hasNext())
      {
         KeyListener listener = (KeyListener)it.next();
         listener.keyTyped(event);
      }
   }

   /**
    * Adds a key listener to this cell editor
    * @param listener the <code>KeyListener</code>,
    * may not be <code>null</code>.
    */
   public void addKeyListener(KeyListener listener)
   {
      if(null == listener)
         throw new IllegalArgumentException("KeyListener cannot be null.");
      m_keyListeners.add(listener);
   }

   /**
    * Combo box editor component. Never <code>null</code>.
    */
   private JComboBox m_comboEditor = new JComboBox();

   /**
    * No Edit text field component. Never <code>null</code>.
    */
   private JTextField m_textNoEdit = new JTextField();

   /**
    * Text editor component. Never <code>null</code>.
    */
   private JTextField m_textEditor = new JTextField();

   /**
    * Holds a reference to the current editor component. Set and modified in
    * {@link #getTableCellEditorComponent(JTable,Object,boolean,int,int).
    * Never <code>null</code> after the first setting.
    */
   private Component m_currentEditor;

   /**
    * Current row index
    */
   private int m_currentRow = 0;

   /**
    * Current column index
    */
   private int m_currentColumn = 0;

   /**
    * Reference to the table model that this cell editor
    * is in. Set in
    * {@link #getTableCellEditorComponent(JTable,Object,boolean,int,int).
    * Never <code>null</code> after the first setting.
    */
   private PSFieldValueMapTableModel m_model;

   /**
    * Keylistener list, never <code>null</code>, may be empty.
    */
   private List m_keyListeners = new ArrayList();


   /**
    * Array of allowed variables. Never <code>null</code>.
    * Always contains at least one value.
    */
   private static final String[] ms_variables =
      PSVariableEvaluator.getAllowedVariables();

   /**
    * Array of all value type except for XPath type.
    * Never <code>null</code>. Always contains at least one value.
    */
   private static final String[] ms_valueTypes = {
      PSFieldProperty.VALUE_TYPE_DATE,
      PSFieldProperty.VALUE_TYPE_LITERAL, 
      PSFieldProperty.VALUE_TYPE_NUMBER, 
      PSFieldProperty.VALUE_TYPE_VARIABLE};
}
/*[ UTParameterPanel.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSTextLiteral;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The parameter panels provides an editor for function parameters in a table
 * form.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTParameterPanel extends JPanel implements ListSelectionListener
{
   /**
    * Construct a new parameter panel. Use the given cell editor on the 
    * contained table.
    * 
    * @param editor A table cell editor of class <code>ValueSelectorCellEditor</code>
    * @param description_inside Whether the description text pane should be
    * placed in the panel with the properties or externally to allow use from
    * other dialog components.
    */
   public UTParameterPanel(
      ValueSelectorCellEditor editor,
      boolean description_inside)
   {
      m_editor = editor;
      initPanel(description_inside);
   }

   /**
   * Append a parameter to this table.
   *
   * @param name   parameter name
   * @param value parameter value
   * @param description parameter description
    */
   //////////////////////////////////////////////////////////////////////////////
   public void appendParameter(String name, Object value, String description)
   {
      Vector row = new Vector(3);
      row.addElement(name);
      row.addElement(value);
      row.addElement(description);

      m_table.appendRow(row);
   }

   /**
    * If any editors are open, they are closed as if Enter had been performed.
    * This is useful when user is closing a dialog using the Ok button.
    *
    * @return <code>true</code> if not editing or the editor was successfully
    * stopped. <code>false</code> is returned if the editor doesn't stop (probably
    * due to validation errors).
   **/
   public boolean stopEditing()
   {
      if (m_tableView.isEditing())
      {
         TableCellEditor editor = m_tableView.getCellEditor();
         if (null != editor)
            return editor.stopCellEditing();
      }
      return true;
   }

   /**
   * Set the contents for the indexed row.
   *
   * @param name   parameter name
   * @param value parameter value
   * @param description parameter description
   * @param index   the row index
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setParameterAt(
      String name,
      Object value,
      String description,
      int index)
   {
      m_table.setValueAt(name, index, UTParameterPanelTableModel.PARAMETER);
      m_table.setValueAt(value, index, UTParameterPanelTableModel.VALUE);
      m_table.setValueAt(
         description,
         index,
         UTParameterPanelTableModel.DESCRIPTION);
   }

   /**
   * Clear the contents of this paramter table.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void clear()
   {
      for (int i = 0; i < m_table.getColumnCount(); i++)
         m_table.clearColumn(i);
      ListSelectionModel lsm = m_tableView.getSelectionModel();
      lsm.clearSelection();
      m_tableView.editingCanceled(new ChangeEvent(this));
   }

   /**
   * Get number of parameter rows
   *
   * @return int number of rows
    */
   //////////////////////////////////////////////////////////////////////////////
   public int getRowCount()
   {
      return m_table.getRowCount();
   }

   /**
    * Set the text in the description field. This method allows containing
    * controls to display text in the field.
    * 
    * @param text New text to be displayed, may be <code>null</code>
    */
   public void setDescriptionText(String text)
   {
      if (text == null)
      {
         m_description.setText("");
      }
      else
      {
         m_description.setText(text);
      }
   }

   /**
   * Create the parameter table.
   *
   * @return   JScrollPane      the table view, a scrollable pane
   */
   //////////////////////////////////////////////////////////////////////////////
   private JScrollPane createTableView()
   {
      // add row selection listener to update description
      m_tableView.getSelectionModel().addListSelectionListener(this);

      // define the cell renderers
      UTTextFieldCellRenderer renderer = new UTTextFieldCellRenderer();
      m_tableView.setDefaultRenderer(
         m_tableView
            .getColumn(m_table.getResources().getString("value"))
            .getClass(),
         renderer);

      // define the cell editors
      m_tableView.getColumn(
         m_table.getResources().getString("value")).setCellEditor(
         m_editor);
      m_tableView
         .getColumn(m_table.getResources().getString("parameter"))
         .setCellEditor(new UTReadOnlyTableCellEditor());

      // remove columns not shown
      m_tableView.removeColumn(
         m_tableView.getColumn(
            m_table.getResources().getString("description")));

      JScrollPane pane = new JScrollPane(m_tableView);
      pane.setPreferredSize(TABLE_SIZE);
      return pane;
   }

   /**
   * Start cell editor.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void onEdit()
   {
      // get and save the original value in the cell, in case user cancelled
      int iCol = m_tableView.getEditingColumn();
      int iRow = m_tableView.getEditingRow();
      Object origValue = m_table.getValueAt(iRow, iCol);

      m_dialog =
         new ValueSelectorDialog(
            E2Designer.getApp().getMainFrame(),
            m_vDataTypes,
            m_defaultType);
      m_dialog.setVisible(true);

      m_tableView.getCellEditor(iRow, iCol).cancelCellEditing();

      Object newValue = m_dialog.getObject();
      if (newValue == null)
         m_table.setValueAt(origValue, iRow, iCol);
      else
         m_table.setValueAt(newValue, iRow, iCol);
   }

   /**
    * Returns the parameters.
    *
    * @return   PSExtensionParamValue[]   the parameters
    */
   public PSExtensionParamValue[] getParameters()
   {
      IPSReplacementValue[] values = getReplacementValues();
      PSExtensionParamValue params[] = new PSExtensionParamValue[values.length];
      
      cleanupParamValues(values);  
      
      for (int i = 0; i < values.length; i++)
      {
         params[i] = new PSExtensionParamValue(values[i]);
      }

      return params;
   }

   /**
    * Returns the parameter values.
    *
    * @return array of paramater values, never <code>null</code>, may be empty
    */
   public IPSReplacementValue[] getReplacementValues()
   {
      /* End the cell editing mode before trying to grab params. If the
         user is currently editing, the contents of the editor would not be
         recognized. */
      if (m_tableView.isEditing())
         m_tableView.getCellEditor().stopCellEditing();
      int count = 0;
      for (count = 0; count < m_table.getRowCount(); count++)
      {
         String strName =
            (String) m_table.getValueAt(
               count,
               UTParameterPanelTableModel.PARAMETER);
         if (strName.equals(""))
            break;
      }

      IPSReplacementValue params[] = new IPSReplacementValue[count];
      for (int i = 0; i < count; i++)
      {
         Object possible =
            m_table.getValueAt(i, UTParameterPanelTableModel.VALUE);

         if (possible instanceof IPSReplacementValue)
            params[i] = (IPSReplacementValue) possible;
      }
      return params;
   }
   

   /**
    * Check for nulls in the list and replace those with empty text literal
    * replacement values.
    * 
    * @param values an array of values, can be <code>null</code>
    */
   private void cleanupParamValues(IPSReplacementValue[] values)
   {
      if (values == null) return;
      
      boolean foundnulls = false;
      
      // Check for null
      for (int i = 0; i < values.length; i++)
      {
         if (values[i] == null)
         {
            foundnulls = true;
            break;
         }
      }
      
      if (! foundnulls) return;
      
      // Walk the resulting array and substitute any remaining nulls. Any
      // remaining nulls will be interior array elements
      for (int i = 0; i < values.length; i++)
      {
         if (values[i] == null)
         {
            values[i] = new PSTextLiteral("");
         }
      }
   }   

   /**
   * Create parameter description panel
   *
   * @return   JPanel   the parameter description panel
   */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createParameterDescriptionPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      JScrollPane sp = new JScrollPane(m_description);

      panel.setBorder(
         new TitledBorder(getResources().getString("description")));
      panel.add(sp);

      m_description.setEditable(false);
      // Copy colors from parent to make the text box blend in
      m_description.setBackground(panel.getBackground());
      m_description.setForeground(panel.getForeground());
      m_description.setLineWrap(true);
      m_description.setWrapStyleWord(true);
      m_description.setRows(4);

      return panel;
   }

   /**
    * Initialize the panel, creating the appropriate borders and layouts.
    * 
    * @param description_inside Whether the parameter description panel 
    * should be placed inside or outside the main parameter panel.
    */
   private void initPanel(boolean description_inside)
   {
      if (description_inside)
      {
         this.setLayout(new BorderLayout());
         this.setBorder(
            new TitledBorder(getResources().getString("parameters")));
         this.add(createTableView(), "Center");
         this.add(createParameterDescriptionPanel(), "South");
      }
      else
      {
         JPanel ppanel = new JPanel();
         ppanel.setLayout(new BorderLayout());
         ppanel.setBorder(
            new TitledBorder(getResources().getString("parameters")));
         ppanel.add(createTableView(), "Center");
         this.setLayout(new BorderLayout());
         this.add(ppanel, "Center");
         this.add(createParameterDescriptionPanel(), "South");
      }
   }

   /**
   * Returns the table model for the panel.
   */
   //////////////////////////////////////////////////////////////////////////////
   public UTParameterPanelTableModel getTableModel()
   {
      return m_table;
   }

   /**
   * Added for testing reasons only.
   */
   //////////////////////////////////////////////////////////////////////////////
   private ResourceBundle m_res = null;
   protected ResourceBundle getResources()
   {
      try
      {
         if (m_res == null)
            m_res =
               ResourceBundle.getBundle(
                  getClass().getName() + "Resources",
                  Locale.getDefault());
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }

      return m_res;
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementations for ListSelectionListener
   public void valueChanged(ListSelectionEvent e)
   {
      String desc = null;
      int iRow = m_tableView.getSelectedRow();
      if (iRow >= 0)
         desc =
            (String) m_table.getValueAt(
               iRow,
               UTParameterPanelTableModel.DESCRIPTION);
      else
         desc = "";
      m_description.setText(desc);
   }

   //////////////////////////////////////////////////////////////////////////////
   /**
     * set the Vector to be used for constructing the ValueSelectorDialog for the Value column
     */
   public void setDataTypesForValueSelector(Vector v)
   {
      m_vDataTypes = v;
   }

   //////////////////////////////////////////////////////////////////////////////
   /**
    * the parameter table
    */
   private final static Dimension TABLE_SIZE = new Dimension(400, 200);
   private UTParameterPanelTableModel m_table =
      new UTParameterPanelTableModel();
   private JTable m_tableView = new JTable(m_table);
   private ValueSelectorCellEditor m_editor;
   /**
    * the parameter editor dialog
    */
   private ValueSelectorDialog m_dialog = null;
   /**
    * the parameter description
    */
   JTextArea m_description = new JTextArea();

   /**
   * the Vector of context sensitive data types for Value Selector Dialog
   */
   private Vector m_vDataTypes = null;
   private IDataTypeInfo m_defaultType = null;
}

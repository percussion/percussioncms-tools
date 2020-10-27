/******************************************************************************
 *
 * [ PSDatasourceBasePanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTReadOnlyTableCellEditor;
import com.percussion.UTComponents.UTFixedButton;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

/**
 * Base class to provide a panel with a table and add, edit, and delete buttons,
 * and a simple facade to the table model using string arrays.
 */
public abstract class TableEditorBasePanel extends JPanel
{
   /**
    * Called by the derived class during construction, initializes the panel
    * components and data.
    */
   protected void initPanel()
   {
      setLayout(new BorderLayout());
      setBorder(new EmptyBorder(10, 10, 10, 10));
      
      // create table and button panel
      add(createTablePanel(), BorderLayout.CENTER);
      add(createCommandPanel(), BorderLayout.SOUTH);
      
      // manage button state
      m_table.getSelectionModel().addListSelectionListener(
         new ListSelectionListener() {

         public void valueChanged(ListSelectionEvent e)
         {
            if (e == null);
            updateButtonState();
         }});
      updateButtonState();
   }

   /**
    * Creates command button panel
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      panel.add(buttonPanel, BorderLayout.EAST);
      
      m_addButton = createButton("add");
      m_editButton = createButton("edit");
      m_deleteButton = createButton("delete");
      
      m_addButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e)
         {
            if (e == null);
            onAdd();
         }});
      
      m_editButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e)
         {
            if (e == null);
            onEdit();
         }});
      
      m_deleteButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e)
         {
            if (e == null);
            onDelete();
         }});
      
      buttonPanel.add(m_addButton);
      buttonPanel.add(Box.createHorizontalStrut(5));
      buttonPanel.add(m_editButton);
      buttonPanel.add(Box.createHorizontalStrut(5));
      buttonPanel.add(m_deleteButton);
      
      return panel;
   }
   
   /**
    * Handles the add buton click, as well as the double-click on the empty
    * row.
    */
   protected void onAdd()
   {
      int newRow = m_table.getRowCount() - 1;
      
      Object[] rowData = doAdd();
      if (rowData == null)
         return;

      m_modified = true;
      m_tableModel.insertRow(newRow, rowData);
   }
   
   /**
    * Handles the edit buton click, as well as the double-click on a populated
    * row.
    */
   protected void onEdit()
   {
      int onRow = m_table.getSelectedRow();
      
      if (!isEditable(onRow))
         return;
      
      Object[] rowData = doEdit(onRow);
      if (rowData == null)
         return;
      
      m_modified = true;
      for (int i = 0; i < rowData.length; i++)
      {
         m_tableModel.setValueAt(rowData[i], onRow, i);
      }
   }
   
   /**
    * Refreshes the table from the model
    */
   protected void refreshTable()
   {
      Object[][] tableData = getTableData();
      
      for (int i = 0; i < tableData.length; i++)
      {
         Object[] rowData = tableData[i];
         for (int j = 0; j < rowData.length; j++)
         {
            m_tableModel.setValueAt(rowData[j], i, j);
         }
      }
      updateButtonState();
   }

   /**
    * Handles the delete buton click, as well as typing the delete key with a 
    * populated row selected.
    */   
   protected void onDelete()
   {
      int onRow = m_table.getSelectedRow();
      
      if (!(isEditable(onRow) && canDelete(onRow)))
         return;
      
      boolean deleted = doDelete(onRow);
      
      if (!deleted)
         return;
      
      m_modified = true;      
      m_tableModel.removeRow(onRow);      
   }
   
   /**
    * Determines if the current row may be deleted.  Returns <code>true</code>
    * by default.  Derived classes should override this method if they required
    * conditional behavior.
    * 
    * @param onRow The row to check.
    * 
    * @return <code>true</code> if it can be deleted, <code>false</code> if not.
    */
   protected boolean canDelete(int onRow)
   {
      if (onRow > 0);
      return true;
   }

   /**
    * Determine if the specified row is editable.  The last row of the table is
    * always empty and may not be edited.  Derived classes should override this
    * method if they require additional behavior, but should also check 
    * <code>super.isEditable()</code> as well.
    * 
    * @param onRow The row to check.
    * 
    * @return <code>true</code> if it is editable, <code>false</code> if not.
    */
   protected boolean isEditable(int onRow)
   {
      return (onRow >= 0 && onRow != m_table.getRowCount() - 1);
   }
   
   /**
    * Accessor for the current model data.
    * 
    * @return A two dimensional array of objects, where the first dimension is 
    * the row index and the second is the column index, never <code>null</code>.
    */
   protected Object[][] getModelData()
   {
      Object[][] data = 
         new String[m_tableModel.getRowCount()] [m_tableModel.getColumnCount()];
      
      // don't include the last row
      for (int i = 0; i < data.length - 1; i++)
      {
         for (int j = 0; j < data[i].length; j++)
         {
            data[i][j] = m_tableModel.getValueAt(i, j);
         }
      }
      
      return data;
   }
   
   /**
    * Get the number of rows currently populated in the table.
    * 
    * @return the number of populated rows.
    */
   protected int getRowCount()
   {
      // don't include the last row
      return m_tableModel.getRowCount() - 1;
   }
   
   /**
    * See {@link JTable#getSelectedRow()}
    * 
    * @return The selected row, or -1 if no row is selected.
    */
   protected int getSelectedRow()
   {
      return m_table.getSelectedRow();
   }
   
   /**
    * Determine if the data shown by this panel has been modified
    * 
    * @return <code>true</code> if it has been modified, <code>false</code> if
    * not.
    */
   public boolean isModified()
   {
      return m_modified;
   }
   
   /**
    * Sets the modified flag. See {@link #isModified()} for details.
    * 
    * @param modified The flag value to set.
    */
   public void setModified(boolean modified)
   {
      m_modified = modified;
   }
   
   /**
    * Adds the listener to the table model to be notified of model change
    * events.
    * 
    * @param listener The listener, may not be <code>null</code>.
    */
   public void addTableModelListener(TableModelListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
      
      m_tableModel.addTableModelListener(listener);
   }
   
   /**
    * Method implemented by derived classes to handle an edit action.
    * 
    * @param selectedRow The currently selected row.
    * 
    * @return The edited row, <code>null</code> if no edit was performed, must
    * have the same length as the result of {@link #getColumnNames()}.
    */
   protected abstract Object[] doEdit(int selectedRow);

   /**
    * Method implemented by derived classes to handle an add action.
    * 
    * @return The add row, <code>null</code> if no add was performed, must
    * have the same length as the result of {@link #getColumnNames()}.
    */
   protected abstract Object[] doAdd();
   
   /**
    * Method implemented by derived classes to handle a delete action.
    * 
    * @param selectedRow The currently selected row.
    * 
    * @return <code>true</code> if the row was deleted, <code>false</code> if
    * it was not. 
    */
   protected abstract boolean doDelete(int selectedRow);

   /**
    * Creates a button using the supplied key, setting the label and mnemonic.
    * 
    * @param key The key for the button, assumed not <code>null</code> or empty.
    * 
    * @return The button, never <code>null</code>.
    */
   private JButton createButton(String key)
   {
      String fullKey = getFullKey(key);
      JButton button = new UTFixedButton(ms_res.getString(fullKey));
      String mnemonic = ms_res.getString(fullKey + ".mn");
      if (!StringUtils.isBlank(mnemonic))
         button.setMnemonic(mnemonic.charAt(0));
      
      return button;
   }

   /**
    * Creates the table panel.
    * 
    * @return the panel, never <code>null</code>.
    */
   private JPanel createTablePanel()
   {
      JPanel tablePanel = new JPanel(new BorderLayout());
      tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
      m_tableModel = new DefaultTableModel(getTableData(), 
         getColumnNames()); 
      
      // add empty row at end
      m_tableModel.addRow((Object[])null);
      
      JTable table = new JTable(m_tableModel) {

         private static final long serialVersionUID = 1L;

         @Override
         public TableCellEditor getCellEditor(int row, int column)
         {
            if (isEditable(row))
               return super.getCellEditor(row, column);
            else
               return new UTReadOnlyTableCellEditor();
         }

         @Override
         public TableCellRenderer getCellRenderer(int row, int column)
         {
            if (isEditable(row))
               return super.getCellRenderer(row, column);
            else
               return super.getDefaultRenderer(getColumnClass(column));
         }};
         
      table.getSelectionModel().setSelectionMode(
         ListSelectionModel.SINGLE_SELECTION);
      
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      table.setIntercellSpacing(new Dimension(0, 0));
      table.setRowSelectionAllowed(true);
      table.setColumnSelectionAllowed(false);
      table.setRequestFocusEnabled(false);
      table.getTableHeader().setReorderingAllowed(false);
      
      /**
       * Set default read only editor unless derived class supplies one, set
       * renderer only if derived class supplies one, otherwise accept default 
       */
      TableCellEditor defEditor = new UTReadOnlyTableCellEditor();
      for (int i = 0; i < getColumnNames().length; i++)
      {
         TableColumn col = table.getColumnModel().getColumn(i);
         
         TableCellEditor editor = getTableCellEditor(i); 
         col.setCellEditor(editor == null ? defEditor : editor);
         
         TableCellRenderer renderer = getTableCellRenderer(i);
         if (renderer != null)
            col.setCellRenderer(renderer);
      }
      
      m_table = table;
      
      JScrollPane scrollPane = new JScrollPane(table);      
      tablePanel.add(scrollPane, BorderLayout.CENTER);
      
      // need to handle delete key, so table must request focus when a row is
      // selected, and then add a key listener.  
      m_table.getSelectionModel().addListSelectionListener(
         new ListSelectionListener() {

         public void valueChanged(ListSelectionEvent e)
         {
            if (e == null);
            m_table.requestFocusInWindow();
         }});
      
      m_table.addKeyListener(new KeyAdapter() {

         @Override
         public void keyReleased(KeyEvent e)
         {
            if (e.getKeyCode() == KeyEvent.VK_DELETE)
               onDelete();
            else
               super.keyReleased(e);
         }
         
      });
      
      // handle doubleclick
      m_table.addMouseListener(new MouseAdapter() {

         @Override
         public void mouseClicked(MouseEvent e)
         {
            if (e.getClickCount() == 2)
            {
               int onRow = m_table.getSelectedRow();
               if (isEditable(onRow))
                  onEdit();
               else
                  onAdd();
            }
            else
               super.mouseClicked(e);
         }
      });

      
      return tablePanel;
   }
   
   /**
    * Allows derived classes to override the default cell renderer.  Base class
    * returns <code>null</code> to use the default.
    * 
    * @param colIndex The column index.
    * 
    * @return The renderer, may be <code>null</code>.
    */
   protected TableCellRenderer getTableCellRenderer(int colIndex)
   {
      if (colIndex > 0);
      return null;
   }

   /**
    * Allows derived classes to override the default cell editor.  Base class
    * returns <code>null</code> to use the default.
    * 
    * @param colIndex The column index.
    * 
    * @return The editor, may be <code>null</code>.
    */
   protected TableCellEditor getTableCellEditor(int colIndex)
   {
      if (colIndex > 0);
      return null;
   }

   /**
    * Handles enabling and disabling the add, edit, and delete buttons based
    * on the currently selected row.
    */
   protected void updateButtonState()
   {
      int onRow = m_table.getSelectedRow();
      if (!isEditable(onRow))
      {
         m_editButton.setEnabled(false);
         m_deleteButton.setEnabled(false);
      }
      else
      {
         m_editButton.setEnabled(true);
         m_deleteButton.setEnabled(canDelete(onRow));
      }
   }
   
   /**
    * Gets the "full" key.  This is {@link #getKeyPrefix()} 
    * <code>+ key</code>
    *  
    * @param key The key, assumed not <code>null</code> or empty.
    * 
    * @return The full key, never <code>null</code> or empty.
    */
   private String getFullKey(String key)
   {
      return getKeyPrefix() + key;
   }
   
   /**
    * Get the string from the resource bundle, using 
    * {@link #getFullKey(String) getFullKey(key)} as the key.
    * 
    * @param key The portion of the key following the key prefix, may not be
    * <code>null</code> or empty.
    * 
    * @return The matching value from the bundle.
    */
   public String getString(String key)
   {
      if (StringUtils.isBlank(key))
         throw new IllegalArgumentException("key may not be null or empty");
      
      return ms_res.getString(getFullKey(key));
   }
   
   /**
    * Get the key prefix to use in {@link #getFullKey(String)}.  See 
    * {@link #getString(String)} for other details.
    * 
    * @return The key prefix, may not be <code>null</code> or empty.
    */
   protected abstract String getKeyPrefix();
   
   /**
    * Derived classes override this to provide the column names.
    * 
    * @return The array, never <code>null</code> or empty.
    */
   protected abstract String[] getColumnNames();

   /**
    * Derived classes overide this to provide the table data.  
    * See {@link #getModelData()} for details on the data.  The number of 
    * columns must match the length of the array returned by 
    * {@link #getColumnNames()}.
    * 
    * @return The table data array, never <code>null</code>.
    */
   protected abstract Object[][] getTableData();

   /**
    * The add button, never <code>null</code> after ctor.
    */
   private JButton m_addButton;

   /**
    * The edit button, never <code>null</code> after ctor.
    */
   private JButton m_editButton;
   
   /**
    * The delete button, never <code>null</code> after ctor.
    */   
   private JButton m_deleteButton;

   /**
    * The table, never <code>null</code> after ctor.
    */
   private JTable m_table;
   
   /**
    * The table model, never <code>null</code> after ctor.
    */
   private DefaultTableModel m_tableModel;
   
   /**
    * Flag to indicate if the table data has been modified.
    */
   private boolean m_modified = false;
   
   /**
    * Resource bundle to use, never <code>null</code>.
    */
   private static ResourceBundle ms_res = PSServerAdminApplet.getResources();
   
}


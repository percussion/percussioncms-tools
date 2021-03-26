/******************************************************************************
 *
 * [ PropertiesPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSPropertySet;
import com.percussion.guitools.PSTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * A panel that allows editing and displaying properties.
 */
public class PropertiesPanel extends JPanel
{
   /**
    * Creates a new properties table for the supplied properties.
    *
    * @param properties the properties with which the table will be initialized,
    *    not <code>null</code>, may be empty.
    * @param minRows the minimum number of rows to display in the properties
    *    table, all integer values are allowed.
    * @param title the title used to create a titled border around the table,
    *    <code>null</code> if no border is needed, if empty is supplied, the 
    *    border will not show a title.
    * @param preferredSize the preferred size to be used for the panels scroll
    *    pane, may be <code>null</code> if which case the default is used.
    */
   public PropertiesPanel(PSPropertySet properties, int minRows, String title, 
      Dimension preferredSize)
   {
      m_minRows = minRows;

      initPanel(properties, title, preferredSize);
   }

   /**
    * Initializes the user interface, which is basically one table for the
    * specified parameters in the constructor.
    *
    * See {@link #PropertiesPanel(PSPropertySet, int, String, Dimension)} for
    *    the parameter descriptions.
    */
   private void initPanel(PSPropertySet properties, String title, 
      Dimension preferredSize)
   {
      // initialize property type map
      if (ms_propertyTypes == null)
      {
         ms_propertyTypes = new HashMap<Integer, PropertyType>();
         for (int i=0; i<PSProperty.TYPE_ENUM.length; i++)
         {
            ms_propertyTypes.put(new Integer(i), new PropertyType(i,
               ms_res.getString(PSProperty.TYPE_ENUM[i])));
         }
      }

      m_cellRenderer = new PropertiesTableCellRenderer();
      m_cellEditor = new PropertiesTableCellEditor();
      m_cellEditor.setPropertyTypes(ms_propertyTypes.values().iterator());

      m_table = new PropertiesTable();
      m_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      
      setProperties(properties);

      KeyStroke enterRelease = KeyStroke.getKeyStroke(
         KeyEvent.VK_ENTER, 0, true);
      m_table.getInputMap().put(enterRelease, "noAction");
      AbstractAction noAction = new AbstractAction()
      {
         public void actionPerformed(ActionEvent e)
         {
            /**
             * Executes no action, implemented to avoid sending the event up
             * to its container.
             * This is important so that dialogs using this panel will not 
             * receive this action and perform the default action.
             */
         }
      };
      m_table.getActionMap().put("noAction", noAction);

      JScrollPane scrollPane = new JScrollPane(m_table);
      add(scrollPane);
      if (preferredSize != null)
         scrollPane.setPreferredSize(preferredSize);

      if (title != null)
         setBorder(BorderFactory.createTitledBorder(title));
   }

   /**
    * Sets the new properties. All existing properties will be thrown away and
    * the new propeties wil be used instead.
    *
    * @param properties the new properties to set, may be <code>null</code> or 
    *    empty.
    */
   public void setProperties(PSPropertySet properties)
   {
      if (properties == null)
         properties = new PSPropertySet();
         
      m_table.setModel(new PropertiesTableModel(properties.iterator()));
         
      setColumnVisibility(m_isHidden);
   }
   
   /**
    * Hide all columns specified in the supplied array. Columns not specified
    * remain unchanged.
    *
    * @param columns an array of column indices to hide, not <code>null</code>.
    *    Ignores invalid column indices.
    */
   public void hideColumns(int[] columns)
   {
      if (columns == null)
         throw new IllegalArgumentException("columns cannot be null");

      for (int i=0; i<columns.length; i++)
      {
         int column = columns[i];
         if (column >= COL_PROPS_MININDEX && column <= COL_PROPS_MAXINDEX)
            m_isHidden[column] = true;
      }
         
      setColumnVisibility(m_isHidden);
   }

   /**
    * Makes all columns specified in the supplied array visible. Columns
    * not specified remain unchanged.
    *
    * @param columns an array of column indices to make visible,
    *    not <code>null</code>. Ignores invalid column indices.
    */
   public void showColumns(int[] columns)
   {
      if (columns == null)
         throw new IllegalArgumentException("columns cannot be null");

      for (int i=0; i<columns.length; i++)
      {
         int column = columns[i];
         if (column >= COL_PROPS_MININDEX && column <= COL_PROPS_MAXINDEX)
            m_isHidden[column] = false;
      }
      
      setColumnVisibility(m_isHidden);
   }
   
   /**
    * Sets the column visibility based on the supplied status array.
    * 
    * @param hidden a <code>boolean</code> array that specifies whether or not
    *    a column with the same index is hidden, assumed not <code>null</code>.
    */
   private void setColumnVisibility(boolean[] hidden)
   {
      for (int i=0; i<hidden.length; i++)
      {
         TableColumn column = null;
         try
         {
            switch (i)
            {
               case COL_PROPS_NAME:
                  column = m_table.getColumn(ms_res.getString("column.name"));
                  break;
               case COL_PROPS_VALUE:
                  column = m_table.getColumn(ms_res.getString("column.value"));
                  break;
               case COL_PROPS_LOCKED:
                  column = m_table.getColumn(ms_res.getString("column.locked"));
                  break;
               case COL_PROPS_TYPE:
                  column = m_table.getColumn(ms_res.getString("column.type"));
                  break;
               case COL_PROPS_DESCRIPTION:
                  column = m_table.getColumn(ms_res.getString("column.description"));
                  break;
            }
         }
         catch (IllegalArgumentException e)
         {
            // this is thrown by the table if a column is hidden, ignore
         }
   
         if (hidden[i] && column != null)
            m_table.removeColumn(column);
         else if (!hidden[i] && column == null)
            m_table.addColumn(column);
      }
   }

   /**
    * Makes all columns specified in the supplied array editable. Columns
    * not specified remain unchanged. By default all columns are editable.
    *
    * @param columns an array of column indices to make editable,
    *    not <code>null</code>. Ignores invalid column indices.
    */
   public void setEditable(int[] columns)
   {
      if (columns == null)
         throw new IllegalArgumentException("columns cannot be null");

      for (int i=0; i<columns.length; i++)
      {
         int column = columns[i];
         if (column >= COL_PROPS_MININDEX && column <= COL_PROPS_MAXINDEX)
            m_isEditable[column] = true;
      }
   }

   /**
    * Makes all columns specified in the supplied array read-only. Columns
    * not specified remain unchanged. By default all columns are editable.
    *
    * @param columns an array of column indices to make read-only,
    *    not <code>null</code>. Ignores invalid column indices.
    */
   public void setReadOnly(int[] columns)
   {
      if (columns == null)
         throw new IllegalArgumentException("columns cannot be null");

      for (int i=0; i<columns.length; i++)
      {
         int column = columns[i];
         if (column >= COL_PROPS_MININDEX && column <= COL_PROPS_MAXINDEX)
            m_isEditable[column] = false;
      }
   }

   /**
    * Get the properties.
    *
    * @return a list of <code>PSProperty</code> objects, never
    *    <code>null</code>, may be empty.
    */
   public PSPropertySet getProperties()
   {
      Iterator data = ((PropertiesTableModel) m_table.getModel()).getData();

      PSPropertySet properties = new PSPropertySet();
      while (data.hasNext())
         properties.add(data.next());

      return properties;
   }

   /**
    * Class to define property types with an identifier and a display text.
    */
   private class PropertyType
   {
      /**
       * Constructs this object with supplied parameters.
       *
       * @param type the property data type, assumed to be one of the allowed
       *    types of properties, see {@link PSProperty} for allowed values.
       * @param typeString the display string of type, not  <code>null</code> 
       *    or empty.
       */
      public PropertyType(int type, String typeString)
      {
         if (typeString == null || typeString.trim().length() == 0)
            throw new IllegalArgumentException(
               "tyepString cannot be null or empty");
         
         m_type = type;
         m_typeString = typeString;
      }

      /**
       * Gets the type.
       *
       * @return the type, see {@link PSProperty} for possibles values.
       */
      public int getType()
      {
         return m_type;
      }

      /**
       * Gets display string of this property type.
       *
       * @return the property type display string, never
       *    <code>null</code> or empty.
       */
      public String toString()
      {
         return m_typeString;
      }

      /**
       * The display string of property type, initialized in the ctor, never
       *    <code>null</code>, empty or modified after that.
       */
      private String m_typeString = null;

      /**
       * The type of the property, initialized in the ctor, never modified
       * after that.
       */
      private int m_type = -1;
   }

   /**
    * The table model to support the display and edit functionality of
    * properties.
    */
   private class PropertiesTableModel extends PSTableModel
   {
      /**
       * Constructs this model with supplied parameters.
       *
       * @param properties the list of <code>PSProperty</code> objects, 
       *    not <code>null</code> and of correct type.
       */
      public PropertiesTableModel(Iterator properties)
      {
         setData(properties);

         if (getDataVector().size() < m_minRows)
            setNumRows(m_minRows);
      }

      /**
       * Gets the properties set on this table model.
       *
       * @return the list of <code>PSProperty</code> objects, never
       *    <code>null</code>, may be empty.
       */
      public Iterator getData()
      {
         List<Object> props = new ArrayList<Object>();

         Iterator properties = getDataVector().iterator();
         while (properties.hasNext())
         {
            Object prop = ((Vector) properties.next()).get(0);
            if (prop != null)
               props.add(prop);
         }

         return props.iterator();
      }
      
      /**
       * Sets the properties based on the supplied iterator.
       * 
       * @param properties the properties to set, not <code>null</code>, 
       *    may be empty, must be of type <code>PSProperty</code>.
       */
      public void setData(Iterator properties)
      {
         if (properties == null)
            throw new IllegalArgumentException("properties cannot be null");
         
         Vector<Vector<Object>> data = new Vector<Vector<Object>>();
         while (properties.hasNext())
         {
            Vector<Object> rowVector = new Vector<Object>();
            Object property = properties.next();
            if (!(property instanceof PSProperty))
               throw new IllegalArgumentException(
                  "properties must be of type PSProperty");
            
            rowVector.add(property);
            data.add(rowVector);
         }

         setDataVector(data, ms_propColumns);
      }

      /**
       * Checks whether the supplied cell is editable or not. Any cell is
       * editable if the supplied row represents a <code>PSProperty</code>
       * object (property is defined), otherwise only the 'Name' column is
       * editable.
       *
       * @param row the row index of value to get, must be >= 0 and less than
       *    {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less
       *    than {@link #getColumnCount() columncount} of this model.
       * @return <code>true</code> if the cell is editable, otherwise
       *    <code>false</code>.
       * @throws IndexOutOfBoundsException if the supplied row or colmun 
       *    index is invalid.
       */
      public boolean isCellEditable(int row, int col)
      {
         checkRow(row);
         checkColumn(col);

         PSProperty prop = getProperty(row);
         if (col == COL_PROPS_NAME || prop != null)
            return m_isEditable[col];

         return false;
      }

      /**
       * Finds whether data rows can be removed.
       *
       * @return always <code>true</code>.
       */
      public boolean allowRemove()
      {
         return true;
      }

      /**
       * Gets the value at specified row and column.
       *
       * @param row the row index of value to get, must be >= 0 and less than
       *    {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than
       *    {@link #getColumnCount() columncount} of this model.
       * @return the value of cell, may be <code>null</code>
       * @throws IndexOutOfBoundsException if the supplied row or colmun 
       *    index is invalid.
       */
      public Object getValueAt(int row, int col)
      {
         checkRow(row);
         checkColumn(col);

         Object data = null;

         PSProperty prop = getProperty(row);
         if (prop != null)
         {
            switch (col)
            {
               case COL_PROPS_NAME:
                  data = prop.getName();
                  break;
               case COL_PROPS_VALUE:
                  data = prop.getValue();
                  break;
               case COL_PROPS_LOCKED:
                  data = prop.isLocked() ? Boolean.TRUE : Boolean.FALSE;
                  break;
               case COL_PROPS_TYPE:
                  data = ms_propertyTypes.get(new Integer(prop.getType()));
                  break;
               case COL_PROPS_DESCRIPTION:
                  data = prop.getDescription();
                  break;
            }
         }

         return data;
      }

      /**
       * Sets the value at the specified cell. Updates only if the cell is
       * editable. See {@link #isCellEditable(int, int) } for description of
       * which cell is editable.
       *
       * @param value value to assign to cell, may be <code>null</code>
       * @param row the row index of value to get, must be >= 0 and less than
       *    {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than
       *    {@link #getColumnCount() columncount} of this model.
       * @throws IndexOutOfBoundsException if the supplied row or colmun 
       *    index is invalid.
       * @throws IllegalStateException if one tries to set a value on a 
       *    readonly column.
       */
      public void setValueAt(Object value, int row, int col)
      {
         checkRow(row);
         checkColumn(col);

         if (!isCellEditable(row, col))
            throw new IllegalStateException(
               "Can not set a value on the cell that is not editable");

         PSProperty prop = getProperty(row);
         if (prop == null)
         {
            if (col == COL_PROPS_NAME)
            {
               if (!validateValue(value, col))
                  return;

               Vector<PSProperty> data = new Vector<PSProperty>();
               data.add(new PSProperty(value.toString()));
               getDataVector().setElementAt(data, row);
            }
            else
               throw new IllegalStateException(
                  "Name must be set before setting any other value");
         }
         else
         {
            if (!validateValue(value, col))
               return;

            switch (col)
            {
               case COL_PROPS_NAME:
                  prop.setName(value.toString());
                  break;
               case COL_PROPS_VALUE:
                  prop.setValue(value);
                  break;
               case COL_PROPS_LOCKED:
                  prop.setLock(((Boolean)value).booleanValue());
                  break;
               case COL_PROPS_TYPE:
                  PropertyType type = (PropertyType) value;
                  prop.setType(type.getType());
                  if (type.getType() == PSProperty.TYPE_BOOLEAN &&
                     !(prop.getValue() instanceof Boolean))
                  {
                     if (prop.getValue() != null)
                        prop.setValue(Boolean.valueOf(prop.getValue().toString()));
                     else
                        prop.setValue(Boolean.FALSE);
                  }
                  else if (prop.getValue() instanceof Boolean &&
                     type.getType() != PSProperty.TYPE_BOOLEAN)
                  {
                     prop.setValue(prop.getValue().toString());
                  }
                  break;
               case COL_PROPS_DESCRIPTION:
                  if (value == null)
                     prop.setDescription("");
                  else
                     prop.setDescription(value.toString());
                  break;
            }
         }

         fireTableRowsUpdated(row, row);
      }

      /**
       * Validates the new value for the supplied property and column.
       * If invalid, a message is displayed to the user.
       *
       * @param value the new value to be validated, may be <code>null</code>.
       * @param col the column this validation is for, assumed to be valid.
       * @return <code>true</code> if valid, <code>false</code> otherwise.
       */
      private boolean validateValue(Object value, int col)
      {
         String errorMessage = null;

         switch (col)
         {
            case COL_PROPS_NAME:
               if (value == null || value.toString().trim().length() == 0)
                  errorMessage = ms_res.getString("error.name");
               break;

            case COL_PROPS_VALUE:
               if (value == null)
                  errorMessage = ms_res.getString("error.value");
               break;

            case COL_PROPS_LOCKED:
               if (value == null || !(value instanceof Boolean))
                  errorMessage = ms_res.getString("error.locked");
               break;

            case COL_PROPS_TYPE:
               if (value == null || !(value instanceof PropertyType))
                  errorMessage = ms_res.getString("error.type");
               break;
         }

         if (errorMessage != null)
         {
            PSDlgUtil.showErrorDialog(errorMessage, ms_res.getString("error.title"));

            return false;
         }

         return true;
      }

      /**
       * Gets the property represented by this row.
       *
       * @param row the index, assumed to be less than row count of this model
       *    and >= 0.
       * @return the property, may be <code>null</code> if the row does not
       *    represent a property.
       */
      private PSProperty getProperty(int row)
      {
         return (PSProperty)((Vector) getDataVector().get(row)).get(0);
      }
   }

   /**
    * The table cell renderer for the properties table. In general it provides
    * a label for the data that represents a <code>String</code>, a check-box
    * for a <code>Boolean</code>.
    */
   private class PropertiesTableCellRenderer extends DefaultTableCellRenderer
   {
      public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
      {
         Component renderer = null;
         if (value instanceof Boolean)
         {
            JCheckBox checkbox = new JCheckBox();
            renderer = checkbox;
            checkbox.setHorizontalAlignment(SwingConstants.CENTER);
            checkbox.setSelected(((Boolean) value).booleanValue());

            if (isSelected)
               renderer.setBackground(table.getSelectionBackground());
            else
               renderer.setBackground(table.getBackground());
         }

         // use the default if not defined yet
         if (renderer == null)
         {
            renderer = super.getTableCellRendererComponent(table, value,
               isSelected, hasFocus, row, column);
         }

         return renderer;
      }
   }

   /**
    * Class to represent the properties table. This uses the cell renderers and
    * editors supplied by this panel.
    */
   private class PropertiesTable extends JTable
   {
      /**
       * Constructs a table with the Default table model
       */
      public PropertiesTable()
      {
      }

      /**
       * Constructs a table using the supplied table model.
       *
       * @param model the table model to be used for this table.
       *    Should not be <code>null</code>.
       */
      public PropertiesTable(TableModel model)
      {
         super(model);
      }

      // see base class for description
      public TableCellEditor getCellEditor(int row, int column)
      {
         return m_cellEditor;
      }

      // see base class for description
      public TableCellRenderer getCellRenderer(int row, int column)
      {
         return m_cellRenderer;
      }
   }

   /**
    * The table cell editor that supports editing of all columns of the
    * properties table.
    */
   private class PropertiesTableCellEditor extends AbstractCellEditor
      implements TableCellEditor, ItemListener, ActionListener
   {
      /**
       * Constructs this editor and initializes all its editor components.
       */
      public PropertiesTableCellEditor()
      {
         m_textFieldEditor = new JTextField();

         m_comboEditor = new JComboBox();
         m_comboEditor.addItemListener(this);

         m_checkEditor = new JCheckBox();
         m_checkEditor.addActionListener(this);
         m_checkEditor.setHorizontalAlignment(SwingConstants.CENTER);
         m_checkEditor.setBackground(
            UIManager.getColor("Table.selectionBackground"));

         AbstractAction fileChooserAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
               String path = "";
               Object data = getValue(FILE_PATH_KEY);
               if (data != null)
                  path = data.toString();

               JFileChooser fileChooser = new JFileChooser(path);
               fileChooser.setMultiSelectionEnabled(false);
               fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
               fileChooser.showOpenDialog(PropertiesPanel.this);

               File selected = fileChooser.getSelectedFile();
               if (selected != null)
                  putValue(FILE_PATH_KEY,
                     fileChooser.getSelectedFile().toString());

               stopCellEditing();
            }
         };
         fileChooserAction.putValue(AbstractAction.NAME,
            ms_res.getString("button.path"));
         fileChooserAction.putValue(FILE_PATH_KEY, "");
         m_fileEditor = new JButton(fileChooserAction);

         m_descriptionEditor = new JTextField();
      }

      /**
       * Sets the property data types to show in the combo-box editor.
       *
       * @param types the types to show as a list of <code>String</code> 
       *    objects, may not be <code>null</code>, can be empty.
       */
      public void setPropertyTypes(Iterator types)
      {
         if (types == null)
            throw new IllegalArgumentException("types may not be null");

         m_comboEditor.removeAllItems();
         while (types.hasNext())
            m_comboEditor.addItem(types.next());
      }

      /**
       * Action method for item selection change event for combo-box editors.
       * In general it stops cell editing.
       *
       * @param e the selection change event, assumed not to be
       *    <code>null</code> as this method is called by Swing.
       */
      public void itemStateChanged(ItemEvent e)
      {
         stopCellEditing();
      }

      /**
       * Action method to stop cell editing when the editors are done with
       * their actions.
       *
       * @param e the action event, assumed not to be <code>null</code> as this
       *    method is called by Swing.
       */
      public void actionPerformed(ActionEvent e)
      {
         stopCellEditing();
      }

      /**
       * Checks whether a cell is editable or not. In general the cell is
       * editable if the event is a mouse double-click, but if the cell
       * renderer component is <code>JCheckBox</code> or <code>JButton</code>,
       * it allows editing for a single-click.
       *
       * @param event the event, assumed not to be <code>null</code> as this
       *    method is called by Swing.
       * @return <code>true</code> if the cell is editable, otherwise
       *    <code>false</code>
       */
      public boolean isCellEditable(EventObject event)
      {
         if (event instanceof MouseEvent)
         {
            MouseEvent e = (MouseEvent) event;
            int clickCount = 2;
            if (e.getSource() instanceof JTable)
            {
               JTable source = (JTable)e.getSource();
               int row = source.rowAtPoint(e.getPoint());
               int col = source.columnAtPoint(e.getPoint());
               TableCellRenderer renderer = source.getCellRenderer(row, col);
               Component comp = renderer.getTableCellRendererComponent(source,
                  source.getValueAt(row, col), source.isCellSelected(row, col),
                  source.hasFocus(), row, col);

               if (comp instanceof JCheckBox || comp instanceof JButton)
                  clickCount = 1;
            }

            return e.getClickCount() >= clickCount;
         }

         return false;
      }

      //implements interface method
      public Object getCellEditorValue()
      {
         Object data = null;

         if (m_curEditorComponent == m_textFieldEditor)
            data = m_textFieldEditor.getText();
         else if (m_curEditorComponent == m_checkEditor)
            data = m_checkEditor.isSelected() ? Boolean.TRUE : Boolean.FALSE;
         else if (m_curEditorComponent == m_comboEditor)
            data = m_comboEditor.getSelectedItem();
         else if (m_curEditorComponent == m_fileEditor)
            data = m_fileEditor.getAction().getValue("filePath");

         return data;
      }

      /**
       * Implements interface method to show different editor components based
       * on the cell and set its state from the cell value. See the interface
       * for description about parameters and return value. The following
       * explains the editors used.
       * <pre>
       *    PropertiesTable
       *       Name  - JTextField
       *       Value - a JCheckBox for type <code>PSProperty.TYPE_BOOLEAN</code>,
       *          a JFileChooser for type <code>PSProperty.TYPE_FILE</code>,
       *          a JTextField for all other types.
       *       Locked - JCheckBox
       *       Type - JComboBox with available property value types.
       * </pre>
       */
      public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected, int row, int column)
      {
         // erase any previous text
         m_textFieldEditor.setText("");
         m_comboEditor.setSelectedItem(null);
         m_descriptionEditor.setText("");

         PropertiesTableModel model = (PropertiesTableModel) table.getModel();

         String columnName = table.getColumnName(column);
         if (columnName.equals(ms_res.getString("column.name")))
         {
            m_curEditorComponent = m_textFieldEditor;
            if (value != null)
               m_textFieldEditor.setText(value.toString());
         }
         else if (columnName.equals(ms_res.getString("column.value")))
         {
            int type = PSProperty.TYPE_STRING;
            PropertyType propType =
               (PropertyType) model.getValueAt(row, COL_PROPS_TYPE);
            if (propType != null)
               type = propType.getType();

            if (type == PSProperty.TYPE_BOOLEAN)
            {
               m_curEditorComponent = m_checkEditor;
               if (value != null)
                  m_checkEditor.setSelected(((Boolean) value).booleanValue());
            }
            else if (type == PSProperty.TYPE_FILE)
            {
               m_curEditorComponent = m_fileEditor;
               if (value != null)
                  m_fileEditor.getAction().putValue("filePath", value);
            }
            else
            {
               m_curEditorComponent = m_textFieldEditor;
               if (value != null)
                  m_textFieldEditor.setText(value.toString());
            }
         }
         else if (columnName.equals(ms_res.getString("column.locked")))
         {
            m_curEditorComponent = m_checkEditor;
            if (value != null)
               m_checkEditor.setSelected(((Boolean)value).booleanValue());
         }
         else if (columnName.equals(ms_res.getString("column.type")))
         {
            m_curEditorComponent = m_comboEditor;

            if (value != null)
               m_comboEditor.setSelectedItem(value);
         }
         else if (columnName.equals(ms_res.getString("column.description")))
         {
            m_curEditorComponent = m_descriptionEditor;

            if (value != null)
               m_descriptionEditor.setText(value.toString());
         }

         return m_curEditorComponent;
      }

      /**
       * The text field editor to edit string values, initialized in the
       * constructor and never <code>null</code> after that. Its display text
       * is set as per the cell value that this is used for.
       */
      private JTextField m_textFieldEditor = null;

      /**
       * The combo-box editor to edit property types, initialized in the
       * constructor and never <code>null</code> after that.
       */
      private JComboBox m_comboEditor = null;

      /**
       * The editor to edit <code>Boolean</code> values, initialized in the
       * constructor and never <code>null</code> after that. State is modified
       * as per cell value being edited.
       */
      private JCheckBox m_checkEditor = null;

      /**
       * The editor to edit <code>File</code> values, initialized in the
       * constructor and never <code>null</code> after that. State is modified
       * as per cell value being edited.
       */
      private JButton m_fileEditor = null;

      /**
       * The text field editor to edit the property description, initialized
       * in the constructor and never <code>null</code> after that. Its
       * display text is set as per the cell value that this is used for.
       */
      private JTextField m_descriptionEditor = null;

      /**
       * The component that is used to edit the current cell being edited is
       * stored in this and is used later to give back the editor value when
       * editing is stopped.
       */
      private Component m_curEditorComponent = null;
   }

   // test code
   public static void main(String[] arg)
   {
      try
      {
         UIManager.setLookAndFeel((LookAndFeel) Class.forName(
            UIManager.getSystemLookAndFeelClassName()).newInstance());

         JFrame f = new JFrame("Test");
         Container contentPane = f.getContentPane();

         PSPropertySet properties = new PSPropertySet();
         properties.add(new PSProperty("mayHaveInlineLinks",
            PSProperty.TYPE_BOOLEAN, Boolean.FALSE, false, null));
         properties.add(new PSProperty("cleanupBrokenLinks",
            PSProperty.TYPE_BOOLEAN, Boolean.FALSE, false, null));

         PropertiesPanel panel = new PropertiesPanel(properties,
            properties.size(), "Title", null);

//         int[] hidden = { 2, 3 };
         //panel.hideColumns(hidden);

//         int[] readOnly = { 0 };
         //panel.setReadOnly(readOnly);

         contentPane.add(panel, BorderLayout.CENTER);
         f.addWindowListener(new WindowAdapter()
         {
            public void windowClosing(WindowEvent e)
            {
               System.exit(0);
            }
         });

         f.pack();
         f.setVisible(true);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Resource bundle for this class. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res = null;
   static
   {
      ms_res = ResourceBundle.getBundle(PropertiesPanel.class.getName() +
         "Resources", Locale.getDefault());
   }

   /**
    * The map of allowed property data types, with key being the type (<code>
    * Integer</code>) and value being <code>PropertyType</code> object that
    * encapsulates its display text. Initialized in {#initPanel()},
    * never <code>null</code> or changed after that.
    */
   private static Map<Integer, PropertyType> ms_propertyTypes = null;

   /**
    * The minimum number of rows to display for the table. Set in constructor,
    * never changed after that.
    */
   private int m_minRows = -1;

   /**
    * The index for the 'Name' column in the table model.
    */
   public static final int COL_PROPS_NAME = 0;

   /**
    * The index for the 'Value' column in the table model.
    */
   public static final int COL_PROPS_VALUE = 1;

   /**
    * The index for the 'Locked' column in the table model.
    */
   public static final int COL_PROPS_LOCKED = 2;

   /**
    * The index for the 'Type' column in the table model.
    */
   public static final int COL_PROPS_TYPE = 3;

   /**
    * The index for the 'Description' column in the table model.
    */
   public static final int COL_PROPS_DESCRIPTION = 4;

   /**
    * The minimum column index in the table model.
    */
   public static final int COL_PROPS_MININDEX = COL_PROPS_NAME;

   /**
    * The maximum column index in the table model.
    */
   public static final int COL_PROPS_MAXINDEX = COL_PROPS_DESCRIPTION;

   /**
    * The list of column names for properties table model in the order
    * specified by the column index constants. Initialized in
    * {#initPanel()}, never <code>null</code> or changed after that.
    */
   private static Vector<String> ms_propColumns;
   static
   {
      String[] headers = new String[COL_PROPS_MAXINDEX+1];
      headers[COL_PROPS_NAME] = ms_res.getString("column.name");
      headers[COL_PROPS_VALUE] = ms_res.getString("column.value");
      headers[COL_PROPS_LOCKED] = ms_res.getString("column.locked");
      headers[COL_PROPS_TYPE] = ms_res.getString("column.type");
      headers[COL_PROPS_DESCRIPTION] = ms_res.getString("column.description");

      ms_propColumns = new Vector<String>(COL_PROPS_MAXINDEX+1);
      for (int i=0; i<headers.length; i++)
         ms_propColumns.add(headers[i]);
   }

   /**
    * An array of boolean to specify which column is editable and which is not.
    * The COL_PROPS_xxx values are the indices into the array.
    */
   private static boolean[] m_isEditable = new boolean[COL_PROPS_MAXINDEX+1];
   static
   {
      m_isEditable[COL_PROPS_NAME] = true;
      m_isEditable[COL_PROPS_VALUE] = true;
      m_isEditable[COL_PROPS_LOCKED] = true;
      m_isEditable[COL_PROPS_TYPE] = true;
      m_isEditable[COL_PROPS_DESCRIPTION] = true;
   };

   /**
    * A array of columns that will be hidden from the tbale view.
    * The COL_PROPS_xxx values are the indices into the array.
    */
   private static boolean[] m_isHidden = new boolean[COL_PROPS_MAXINDEX+1];
   {
      m_isHidden[COL_PROPS_NAME] = false;
      m_isHidden[COL_PROPS_VALUE] = false;
      m_isHidden[COL_PROPS_LOCKED] = false;
      m_isHidden[COL_PROPS_TYPE] = false;
      m_isHidden[COL_PROPS_DESCRIPTION] = false;
   };

   /**
    * The key used to identify the file path value within the file chooser
    * action.
    */
   private static final String FILE_PATH_KEY = "filePath";

   /**
    * The properties table, initialized while constructed, never
    * <code>null</code> after that.
    */
   private PropertiesTable m_table = null;

   /**
    * The cell renderer used for the properties table, initialized while
    * constructed, never <code>null</code> after that.
    */
   private PropertiesTableCellRenderer m_cellRenderer = null;

   /**
    * The cell editor used for the properties table, initialized while
    * constructed, never <code>null</code> after that.
    */
   private PropertiesTableCellEditor m_cellEditor = null;
}

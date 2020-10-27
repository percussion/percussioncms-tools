/*[ UTPropertiesPanel.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.E2Designer.admin.AdminNameValue;
import com.percussion.E2Designer.admin.PSServerAdminApplet;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.util.PSStringOperation;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The UTPropertiesPanel provides the panel containing the table with 2 columns
 * and a text area. This panel is useful for editing properties(attributes).
 * Text Area is used for editing value of a property.
 */
public class UTPropertiesPanel extends JPanel
{
   /**
    * Constructor for Properties Panel. If it can not find resource file or
    * required properties in that file sets <code>m_bInitialized</code> to
    * <code>false</code> and does not create panel.
    **/
   public UTPropertiesPanel()
   {
      if(getResources() != null)
      {
         try {
            createAttributesPanel();
         }
         catch(MissingResourceException e)
         {
            String property = e.getLocalizedMessage().substring(
               e.getLocalizedMessage().lastIndexOf(' ')+1);
            PSDlgUtil.showErrorDialog(
               "Could not find value for '" + property + "' in \n" +
               "com.percussion.E2Designer.UTPropertiesPanelResources",
               UIManager.getString("OptionPane.messageDialogTitle", null));
            m_bInitialized = false;
         }
      }
      else
         m_bInitialized = false;
   }

   /**
    * Gets Resource Bundle required for this panel.
    *
    * @return Resource Bundle, May be <code>null</code>.
    **/
   private ResourceBundle getResources()
   {
      String resourceName =
         "com.percussion.E2Designer.UTPropertiesPanelResources";
      try {
         ms_dlgResource = ResourceBundle.getBundle(resourceName,
            Locale.getDefault() );
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
         PSDlgUtil.showErrorDialog("Could not find " + resourceName, "Error");
      }

      return ms_dlgResource;
   }

   /**
    *   *   Creates the panel for editing attributes(properties).
    *
    */
   private void createAttributesPanel()
   {
      JPanel attribute_table_panel = createAttributesTablePanel();
      attribute_table_panel.setAlignmentX(LEFT_ALIGNMENT);
      JPanel attribute_value_panel = createAttributeValuePanel();
      attribute_value_panel.setAlignmentX(LEFT_ALIGNMENT);

      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.add(Box.createVerticalStrut(10));
      this.add(createTable());
      this.add(Box.createVerticalStrut(10));
      this.add(attribute_value_panel);
   }

   /**
    * Creates the panel with table to edit attributes.
    *
    * @return The panel with table, never <code>null</code>.
    */
   private JPanel  createAttributesTablePanel()
   {
      JPanel attribute_panel = new JPanel();
      attribute_panel.setLayout(
         new BoxLayout(attribute_panel, BoxLayout.Y_AXIS));
      JLabel label = new JLabel(ms_dlgResource.getString("propertiesTitle"),
         SwingConstants.LEFT);
      label.setAlignmentX(LEFT_ALIGNMENT);

      attribute_panel.add(label);

      attribute_panel.add(createTable());

      return attribute_panel;
   }

   /**
    * Creates table for attributes and sets initial settings and listeners.
    *
    * @return The scrolling pane with table, never <code>null</code>.
    **/
   private JScrollPane createTable()
   {
      m_columnHeaders = new Vector<String>(2);
      String colName = ms_dlgResource.getString("colName");
      String colValue = ms_dlgResource.getString("colValue");
      m_columnHeaders.add(colName);
      m_columnHeaders.add(colValue);

      Vector temp = new Vector();
      m_tableModel = new AttributeTableModel( temp, m_columnHeaders );
      m_table = new JTable(m_tableModel);

      m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_table.setCellSelectionEnabled(true);
      m_table.getTableHeader().setReorderingAllowed(false);

      //add the table selection listeners
      CellSelectionListener listener = new CellSelectionListener();
      m_table.getSelectionModel().addListSelectionListener(listener);
      m_table.getColumnModel().getSelectionModel().addListSelectionListener(
         listener);

      //create and set pop-up menu to table
      final JPopupMenu popupMenu = new JPopupMenu();
      Action clearAction =
         new AbstractAction(ms_dlgResource.getString("clearMenu"))
         {
            public void actionPerformed(ActionEvent e)
            {
               onClearTableCell();
               popupMenu.setVisible(false);
            }
         };
      popupMenu.add(clearAction);

      //Add Mouse Listener for clear action
      m_table.addMouseListener(new MouseAdapter()
      {
         public void mouseReleased(MouseEvent event)
         {
            if(event.isPopupTrigger())
               popupMenu.show(m_table, event.getX(), event.getY());
         }
      });

      //Add Key Listener for delete action
      m_table.addKeyListener( new KeyAdapter()
      {
         public void keyReleased(KeyEvent event)
         {
            if (event.getKeyCode() == KeyEvent.VK_DELETE)
               onDeleteRow();
            else if(event.getKeyCode() == KeyEvent.VK_ESCAPE)
               popupMenu.setVisible(false);
         }
      });

      JScrollPane pane = new JScrollPane (m_table);
      pane.setPreferredSize(new Dimension (80, 125));
      pane.setAlignmentX(LEFT_ALIGNMENT);
      return pane;

   }

   /**
    * Deletes all selected rows and adds empty rows if number of rows in table
    * are less than {@link #MIN_NUM_ROWS}.
    **/
   private void onDeleteRow()
   {
      // delete all selected rows
      int[] indices = m_table.getSelectedRows();

      for (int i=indices.length-1; i>=0; i--)
         m_tableModel.removeRow(indices[i]);

      //Adds empty rows at end to have minimum number of rows in table.
      if(m_tableModel.getRowCount() < MIN_NUM_ROWS)
         m_tableModel.setRowCount(MIN_NUM_ROWS);
   }

   /**
    * Clears the selected cell value. If the selected cell belongs to 'Value'
    * column removes text from text area too.
    **/
   private void onClearTableCell()
   {
      int row = m_table.getSelectedRow();
      int col = m_table.getSelectedColumn();

      if(row >= 0 && col >= 0)
         m_table.setValueAt(null, row, col);

      //If it is 'Value' column clear the text field also
      if(m_table.getColumnName(col).equals(
         ms_dlgResource.getString("colValue")))
         m_valueText.setText("");
   }

   /**
    * Clears the table from the old data.
    */
   public void clearTable()
   {
      Vector tableData = m_tableModel.getDataVector();
      if (tableData != null)
      {
         for (int i = tableData.size(); i > 0; i--)
            m_tableModel.removeRow(0);

         //Adds empty rows at end to have minimum number of rows in table.
         if(m_tableModel.getRowCount() < MIN_NUM_ROWS)
            m_tableModel.setRowCount(MIN_NUM_ROWS);
      }
   }

   /**
    *   *   Creates the panel with text area to edit attribute value.
    *
    * @return The panel, never <code>null</code>.
    **/
   private JPanel  createAttributeValuePanel()
   {
      JPanel attribute_panel = new JPanel();
      attribute_panel.setLayout(
         new BoxLayout(attribute_panel, BoxLayout.Y_AXIS));
      JLabel label = new JLabel(ms_dlgResource.getString("editValueTitle"),
         SwingConstants.LEFT);
      label.setAlignmentX(LEFT_ALIGNMENT);

      attribute_panel.add(label);

      m_valueText = new JTextArea (10,50);
      m_valueText.setLineWrap(true);
      m_valueText.setWrapStyleWord(true);
      m_valueText.setBorder(new EtchedBorder( EtchedBorder.LOWERED ));
      m_valueText.addFocusListener(new TextFocusListener());

      JScrollPane pane = new JScrollPane (m_valueText);
      pane.setAlignmentX(LEFT_ALIGNMENT);
      attribute_panel.add(pane);

      return attribute_panel;
   }

   /**
    * Accessor function to check whether panel is initialized or not.
    *
    * @return Initialized flag.
    **/
   public boolean isInitialized()
   {
      return m_bInitialized;
   }

   /**
    * Sets column editors which overrides the default behavior of columns.
    * If <code>ALLOW_UNDEFINED_ATTRIBUTES</code> is set makes the drop-list
    * editor of attribute name column as editable.
    **/
   private void setEditors()
   {
      //Even though editor for both columns is combo box,
      //set for each inidividual column instead of table
      //because the values to add to combo box are different.
      m_nameEditor = new JComboBox();

      // if we are an applet, we won't be able to read system properties
      if (PSServerAdminApplet.isApplet())
      {
         m_nameEditor.setEditable(false);
      }
      else
      {
         if(System.getProperty(ALLOW_UNDEFINED_ATTRIBUTES) == null)
            m_nameEditor.setEditable(true);
         else
            m_nameEditor.setEditable(false);
      }
 
      DefaultCellEditor defNameEditor = new CustomCellEditor(m_nameEditor);
      defNameEditor.setClickCountToStart(2);
      m_table.getColumn(ms_dlgResource.getString("colName")).setCellEditor(
         defNameEditor);

      m_valueEditor = new JComboBox();

      DefaultCellEditor editor = new CustomCellEditor(m_valueEditor);
      editor.setClickCountToStart(2);
      m_table.getColumn(ms_dlgResource.getString("colValue")).setCellEditor(
         editor);

      m_table.addFocusListener(new TextFocusListener());

   }

   /**
    * Gets editor component of "Name" column.
    *
    * @return the editor component, may be <code>null</code>.
    **/
   public Component getNameColumnCellEditor()
   {
      TableCellEditor editor =
         m_table.getColumn(ms_dlgResource.getString("colName")).getCellEditor();

      if(editor == null)
         return null;
      else
         return ((DefaultCellEditor)editor).getComponent();
   }

   /**
    * Adds cataloged property names to "Name" column drop-list editor if it is
    * set and keeps the name value mappings.
    *
    * @param propertyNameValues, may be <code>null</code>. If it is
    * <code>null</code> assumed no property name values found by cataloging. 
    **/
   private void setCatalogedProperties(List propertyNameValues)
   {
      if(m_nameEditor == null)
         return;

      m_nameValues = new HashMap<AdminNameValue, List>();
      m_nameEditor.removeAllItems();

      if(propertyNameValues != null)
      {
         Iterator iter =   propertyNameValues.iterator();
         while(iter.hasNext())
         {
            AdminNameValue adminProp = (AdminNameValue)iter.next();
            String category = (String) adminProp.getName();
            m_nameEditor.addItem(category);
            m_nameValues.put(adminProp,adminProp.getValues());
         }
      }
      
      /*if the property drop list has no entries, add a single pseudo entry 
      label*/
      if(m_nameEditor.getItemCount() == 0)
         m_nameEditor.addItem(ms_dlgResource.getString("pseudoProp"));

      if(ms_recatalogString  == null)
         ms_recatalogString = ms_dlgResource.getString("recatalog");
      m_nameEditor.addItem(ms_recatalogString);

      //Add an empty string for clearing the cell value when it is selected
      m_nameEditor.addItem("");
   }

   /**
    * Sets property panel with cataloged property name values and sets editor
    * and listeners for editing properties.Should be called if the
    * defined/default properties need to be set to the table for editing.
    *
    * @param propertyNameValues the property name values, may be
    * <code>null</code>.
    * @param listener the item listener, may be <code>null</code>. If the caller
    * wants to specify it's own listener in addition to default, this should not
    * be <code>null</code>.
    **/
   public void setPropertiesAndEditors(List propertyNameValues,
      ItemListener listener)
   {
      setEditors();
      setCatalogedProperties(propertyNameValues);
      setPropertyEditorListener(listener);
   }

   /**
    * Resets property panel editor with re-cataloged property name values.
    * Saves the editing cell's current value to set back after re-cataloging.
    *
    * @param propertyNameValues the property name values, may be
    * <code>null</code>.
    * @param listener the item listener, may be <code>null</code>. If the caller
    * wants to specify it's own listener in addition to default, this should not
    * be <code>null</code>.
    **/
   public void resetCatalogedProperties(List propertyNameValues,
      ItemListener listener)
   {
      /* In case of Re-catalog, we have to remember the current value in the
       * cell to set it back when table gets focus from editor.
       */
      m_attrCellValue = (String)m_table.getValueAt(
         m_table.getEditingRow(), m_table.getEditingColumn());
      m_isCatalog = true;

      /* Remove item listeners before setting re-cataloged properties
       * because adding an item fires the listener which should not occur.
       */
      removeNameColumnItemListener();
      setCatalogedProperties(propertyNameValues);
      setPropertyEditorListener(listener);
   }

   /**
    * Adds a default item listener to drop-list editor of name-column to
    * validate the name entered and sets the value of corresponding
    * "Value" cell of selected cell to <code>null</code> because "Value" depends
    * on attribute name. Sets passed in listener also to "Name" column editor
    * if it is not <code>null</code>.
    * <br>
    * If the editor is not initialized,it simply returns without doing anything.
    *
    * @param listener the item listener which should be set to name column, may
    * be <code>null</code>.
    **/
   private void setPropertyEditorListener(ItemListener listener)
   {
      if(m_nameEditor == null)
         return;

      //set default item listener
      ItemListener defListener = new EditorItemListener();
      setNameColumnItemListener(defListener);

      //add item listener for cataloging role specific
      //attribute/property values
      if(listener != null)
         setNameColumnItemListener(listener);
   }

   /**
    * Sets listener to name column editor. If the editor is not initialized, it
    * simply returns without doing anything.
    *
    * @param listener the item listener to set, assumed not <code>null</code>.
    **/
   private void setNameColumnItemListener(ItemListener listener)
   {
      if(m_nameEditor == null)
         return;

      m_nameEditor.addItemListener(listener);
      if(m_nameEditorlisteners == null)
         m_nameEditorlisteners = new ArrayList<ItemListener>();

      m_nameEditorlisteners.add(listener);
   }

   /**
    * Removes all listeners of name column editor. If the editor is not
    * initialized, it simply returns without doing anything.
    **/
   private void removeNameColumnItemListener()
   {
      if(m_nameEditor == null)
         return;

      if(m_nameEditorlisteners != null)
      {
         Iterator iter = m_nameEditorlisteners.iterator();
         while(iter.hasNext())
         {
            m_nameEditor.removeItemListener((ItemListener)iter.next());
            iter.remove();
         }
      }
   }

   /**
    * Sets Table data with passed in attributes.
    *
    * @param attribs The map of attribute values, must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>attribs</code> is
    * <code>null</code>.
    **/
   public void setTableData(Map attribs)
   {
      if(attribs == null)
         throw new IllegalArgumentException(
            "Data to set to Table can not be null");

      m_tableModel.setRowCount(attribs.entrySet().size());
      Iterator attribsIterator = attribs.entrySet().iterator();
      int row = 0;
      while(attribsIterator.hasNext())
      {
         Map.Entry entry = (Map.Entry)attribsIterator.next();
         m_tableModel.setValueAt(entry.getKey(), row, 0);
         m_tableModel.setValueAt(entry.getValue(), row, 1);
         row++;
      }

      if(m_tableModel.getRowCount() < MIN_NUM_ROWS)
         m_tableModel.setRowCount(MIN_NUM_ROWS);
   }

   /**
    * Sets Table data with passed in attributes list.
    *
    * @param attribs The list of attribute name and values, must not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>attribs</code> is
    * <code>null</code>.
    **/
   public void setTableDataFromList(PSAttributeList attribs)
   {
      if(attribs == null)
         throw new IllegalArgumentException(
            "Data to set to Table can not be null");

      m_tableModel.setRowCount(attribs.size());

      Iterator attribsIterator = attribs.iterator();
      int row = 0;
      while(attribsIterator.hasNext())
      {
         PSAttribute attrib = (PSAttribute)attribsIterator.next();
         m_tableModel.setValueAt(attrib.getName(), row, 0);

         String value = PSStringOperation.append(attrib.getValues(),
            ms_dlgResource.getString("propValueSeperator"));

         m_tableModel.setValueAt(value, row, 1);
         row++;
      }

      if(m_tableModel.getRowCount() < MIN_NUM_ROWS)
         m_tableModel.setRowCount(MIN_NUM_ROWS);
   }


   /**
    * Returns Table data as map.
    *
    * @return Table data as map, never <code>null</code>.
    * @throws ValidationException if data is not valid.
    **/
   public Map getTableData()
      throws ValidationException
   {
      StringBuffer error = new StringBuffer();
      if(validateData(error))
         return convertToHash(m_tableModel.getDataVector());
      else
      {
         throw new ValidationException(error.toString());
      }
   }

   /**
    * Returns Table data as object of {@link PSAttributeList}.
    *
    * @return Table data, never <code>null</code>.
    * @throws ValidationException if data is not valid.
    **/
   public PSAttributeList getTableDataAsList()
      throws ValidationException
   {
      StringBuffer error = new StringBuffer();
      if(validateData(error))
         return convertToAttributeList(m_tableModel.getDataVector());
      else
      {
         throw new ValidationException(error.toString());
      }
   }

   /**
    * Validates data in table. If the attribute name is one of the
    * pre-defined/allowed attribute names, it's value is checked against one of
    * valid values. If the attribute name is <code>null</code> or empty it is
    * ignored. The passed in <code>message</code> is stored with error messages
    * if it finds invalid attribute values. If it finds multiple attributes as
    * invalid, the <code>message</code> lists all invalid attribute values and
    * specifies their corresponding valid values.
    *
    * @param message the string buffer to hold error message while validating,
    * assumed not <code>null</code>.
    **/
   private boolean validateData(StringBuffer message)
   {
      Vector data = m_tableModel.getDataVector();
      if(data == null)
         return true;

      boolean valid = true;
      for(int i=0; i < data.size(); i++)
      {
         Vector row_data = (Vector)data.get(i);

         if(row_data.get(0) != null)
         {
            String key = ((String)row_data.get(0)).trim();
            String value = (String)row_data.get(1);
            if(value == null)
               value = "";
            if(key.length() != 0)
            {
               Map.Entry entry =
                  (Map.Entry)getIgnoreKeyCaseEntry(m_nameValues, key);

               if( entry != null && !isAllowEdit(m_nameValues, key))
               {
                  List values = (ArrayList)entry.getValue();
                  if(values != null && !values.contains(value))
                  {
                     Object[] params = new Object[2];
                     params[0] = key;
                     params[1] = values.toString();
                     message.append(MessageFormat.format(
                        ms_dlgResource.getString("invalidAttrValue"), params));
                     message.append("\n");

                     valid = false;
                  }
               }
            }
         }
      }
      return valid;
   }

   /**
    * Sets value of cell in text area if the column is "Value" column and makes
    * it editable if it doesn't find drop-list editor for cell, otherwsie makes
    * disabled. If the column is "Name" column, it wipes off text in text area
    * and makes disabled.
    *
    * @param   row   The row of selected cell.
    * @param   col   The column of selected cell.
    *
    **/
   private void setValue(int row, int col)
   {
      if(m_table.getColumnName(col).equals(
         ms_dlgResource.getString("colValue")))
      {
         String text = (String)m_table.getValueAt(row, col);
         if(text != null)
         {
            text = PSStringOperation.replaceChar(text,
               ms_dlgResource.getString("propValueSeperator").charAt(0), '\n');
         }
         m_valueText.setText(text);

         if(m_table.getColumn(m_table.getColumnName(col)).getCellEditor() ==
            null)
         {
            m_valueText.setEnabled(true);
            m_valueText.setEditable(true);
            m_valueText.requestFocus();
         }
         else
            m_valueText.setEnabled(false);
      }
      else
      {
         m_valueText.setText("");
         m_valueText.setEnabled(false);
      }
   }

   /**
    * Sets drop-list  or <code>null</code> as editor to "Value" column based on
    * the attribute selected in the row of selected cell. If the attribute
    * selected has values, it shows drop-list of values otherwise makes the cell
    * non-editable and users can enter data in text area for the cell.
    * The attributes names are not case-sensitive.
    *
    * @param row the row of selected cell.
    * @param col the column of selected cell.
    **/
   private void setValueEditor(int row, int col)
   {  
      if(m_valueEditor == null)
         return;

      if(m_table.getColumnName(col).equals(
         ms_dlgResource.getString("colValue")))
      {
         String attrName = (String)m_tableModel.getValueAt(row, col-1);
         ArrayList values = null;

         if(attrName != null)
         {
            Map.Entry entry =
               (Map.Entry)getIgnoreKeyCaseEntry(m_nameValues, attrName);
            if(entry != null)
               values = (ArrayList)entry.getValue();
         }

         if(values == null || values.size() == 0)
         {
            m_table.getColumn(m_table.getColumnName(col)).setCellEditor(null);
            return;
         }

         /* To make a value empty, have a single empty string.
          * If values has other values that aren't empty, the empty value
          * is ignored.
          */
         int index = values.indexOf("");
         if(values.size() > 1 && index != -1)
            values.remove(index);

         m_valueEditor.removeAllItems();
         for(int i=0; i < values.size(); i++)
            m_valueEditor.addItem(values.get(i));

         //Add an empty string to clear the cell when it is selected
         if(values.indexOf("") == -1)
            m_valueEditor.addItem("");

         /*if the empty string is the only item, then this attributes does not
         have values defined, so insert a pseudo entry label */
         if(values.size() == 1 && m_valueEditor.getItemAt(0) instanceof String)
         {  
            if(((String)m_valueEditor.getItemAt(0)).trim().equals(""))
            {
               m_valueEditor.removeItemAt(0);
               m_valueEditor.addItem(ms_dlgResource.getString("pseudoVal"));
            }
         }
         m_valueEditor.setEditable(isAllowEdit(m_nameValues, attrName));
         DefaultCellEditor editor = new CustomCellEditor(m_valueEditor);
         editor.setClickCountToStart(2);
         m_table.getColumn(ms_dlgResource.getString("colValue")).setCellEditor(
            editor);

      }
   }

   /**
    * Gets an entry of key, value pair from provided map, by ignoring the case
    * of key while checking. Returns <code>null</code> if it can not find key.
    *
    * @param map the map to get value from, assumed not <code>null</code>.
    * @param key the key for which value is queried, assumed not
    * <code>null</code>.
    *
    * @return the entry of key, value pair. May be <code>null</code>.
    **/
   private Object getIgnoreKeyCaseEntry(Map map, String key)
   {
      Iterator iter = map.entrySet().iterator();
      while(iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         String name = ((AdminNameValue) entry.getKey()).getName();
         if( name.equalsIgnoreCase(key) )
            return entry;
      }

      return null;
   }

   /**
    * Check if users are allowed to enter additional values. 
    * @param map Map property names and values, assumed not <code>null</code> 
    * @param key name of the object to check, assumed not <code>null</code>
    * @return <code>true</code> if users are allowed to enter their own 
    * value, <code>false</code> if not. If key not found return 
    * <code>false</code>.  
    */
   private boolean isAllowEdit(Map map,String key)
   {
      Iterator iter = map.entrySet().iterator();
      while(iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         String name = ((AdminNameValue) entry.getKey()).getName();
         if(name.equalsIgnoreCase(key))
         { 
            boolean limitToList = ((AdminNameValue)
               entry.getKey()).getLimitToList();
            return !limitToList;
         }

      }
      
     return false;
   }


   /**
    * Function to convert <code>Vector</code> data to <code>Map</code>.
    * If data is <code>null</code>, returns <code>null</code>. Expects each
    * element in data is a <code>Vector</code> of 2 elements, otherwise throws
    * IllegalArgumentException.
    * <br>
    * If key element in row data is <code>null</code> or an empty string,
    * it is not added to the map. If the value is <code>null</code>, it is
    * converted to an empty string.
    *
    * @param  data The <code>Vector</code> data which need to be converted to
    * <code>Map</code>, may be <code>null</code>.
    *
    * @return <code>Map</code> of data in <code>Vector</code> as key and value
    * pairs, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if element in data is not of type
    * <code>Vector</code> or size of element <code>Vector</code> not equal to 2.
    **/
   public Map convertToHash(Vector data)
   {
      if(data == null)
         return null;

      Map<String, String> table = new HashMap<String, String>();

      for(int i=0; i < data.size(); i++)
      {
         Object obj = data.get(i);

         if(!(obj instanceof Vector))
            throw new IllegalArgumentException(
               "can not convert to hash, row data is not a vector");

         Vector row_data = (Vector)obj;
         if(row_data.size() != 2)
            throw new IllegalArgumentException(
               "can not convert to hash, number of columns are more than 2");

         if(row_data.get(0) != null)
         {
            String key = (String)row_data.get(0);
            String value = (String)row_data.get(1);
            if(key.length() != 0)
            {
               Map.Entry entry = (Map.Entry)getIgnoreKeyCaseEntry(table, key);
               if( entry != null)
                  table.remove(entry.getKey());

               if(value == null)
                  value = "";
               table.put(key, value);
            }
         }
      }
      return table;
   }

   /**
    * Function to convert <code>Map</code> data to <code>Vector</code> of
    * <code>Vectors</code>. If <code>map</code> is <code>null</code>, returns
    * <code>null</code>.
    *
    * @param  map The Map which need to be converted to Vector of Vectors.
    * May be <code>null</code>.
    *
    * @return <code>Vector</code> of data  which has key, value pair of
    * <code>map</code> as an element in the form of <code>Vector</code>, may be
    * <code>null</code>.
    **/
   public Vector convertToVector(Map map)
   {
      if(map == null)
         return null;

      Vector<Vector<Object>> data = new Vector<Vector<Object>>();

      Iterator mapIterator = map.entrySet().iterator();
      while(mapIterator.hasNext())
      {
         Vector<Object> row_data = new Vector<Object>(2);
         Map.Entry entry = (Map.Entry)mapIterator.next();

         //each pair is one row
         row_data.add(entry.getKey());
         row_data.add(entry.getValue());

         data.add(row_data);
      }

      return data;
   }

   /**
    * Function to convert Vector data to <code>PSAttributeList</code>.
    * If data is <code>null</code>,  returns <code>null</code>. Expects each
    * element in data is a Vector of 2 elements, otherwise throws
    * IllegalArgumentException.
    * <br>
    * If key element in row data is <code>null</code> or an empty string,
    * it is not added to the list. If the value is <code>null</code>, it is
    * converted to an empty string.
    *
    * @param  data The vector data which need to be converted to attribute list.
    * May be <code>null</code>.
    *
    * @return attribute list of name and values, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if element in data is not of type
    * <code>Vector</code> or size of element is not equal to 2.
    **/
   public PSAttributeList convertToAttributeList(Vector data)
   {
      if(data == null)
         return null;

      PSAttributeList attrList = new PSAttributeList();

      for(int i=0; i < data.size(); i++)
      {
         Object obj = data.get(i);

         if(!(obj instanceof Vector))
            throw new IllegalArgumentException(
               "can not convert to list, row data is not a vector");

         Vector row_data = (Vector)obj;
         if(row_data.size() != 2)
            throw new IllegalArgumentException(
               "can not convert to list, number of columns are more than 2");

         if(row_data.get(0) != null)
         {
            String key = (String)row_data.get(0);
            String value = (String)row_data.get(1);
            if(value == null)
               value = "";

            if(key.length() != 0)
            {
               List values = PSStringOperation.getSplittedList(value,
                  ms_dlgResource.getString("propValueSeperator").charAt(0));

               attrList.setAttribute(key, values);
            }
         }
      }

      return attrList;
   }

   /**
    * Gets the table
    * @return a valid <code>JTable</code>, never <code>null</code>
    */
   public JTable getTable()
   {
      return m_table;
   }

   /**
    * Inner class to override <code>DefaultTableModel</code> for handling table
    * editing and setting minimum number of rows.
    **/
   private class AttributeTableModel extends DefaultTableModel
   {
      /**
       * Constructs a <code>AttributeTableModel</code> with as many columns
       * as there are elements in <code>columnNames</code>
       * and rows as number of elements in <code>data</code>.
       * Each column's name will be taken from the <code>columnNames</code>
       * vector.
       *
       * @param data Data of table, must not be <code>null</code>.
       * @param columnNames The names of the new columns. If this is
       *                    <code>null</code> then the model has no columns.
       *
       * @see DefaultTableModel
       **/
      public AttributeTableModel(Vector data, Vector columnNames)
      {
         super(data, columnNames);
      }

      /**
       * Returns <code>true</code> if the column name of cell is not "Value".
       * Returns <code>false</code> if the column name of cell is "Value" and
       * no editor(<code>null</code>) is set to the column.
       *
       * @param   row   The row whose value to be queried.
       * @param   col   The column whose value to be queried.
       *
       * @return   <code>true</code> if the cell is editable,
       * otherwise <code>false<code>.
       **/
      public boolean isCellEditable(int row, int col)
      {
         if(row < 0 || col < 0)
            return false;

         if(getColumnName(col).equals(ms_dlgResource.getString("colValue")))
         {
            if(m_table.getColumn(getColumnName(col)).getCellEditor() == null)
               return false;
            return true;
         }
         return true;
      }

      /**
       * Calls <code>super</code>'s <code>setDataVector</code> to replace
       * the data vector of the model. Sets Minimum  number of rows of model.
       *
       * @param   newData     The new data vector.Must not be <code>null</code>.
       * @param   columnNames The names of the columns. If this is
       *                    <code>null</code> then the model has no columns.
       *
       * @see DefaultTableModel#setDataVector
       **/
      public void setDataVector(Vector newData, Vector columnNames)
      {
         super.setDataVector(newData, columnNames);
         if(newData.size() < MIN_NUM_ROWS)
            setRowCount(MIN_NUM_ROWS);
      }
   }

   /**
    * Inner class to implement focus listener for text area to update
    * table with text in text area when it is losing focus or set cell
    * with selected item of editor(Combo box) when it gets focus.
    **/
   private class TextFocusListener extends FocusAdapter
   {
      /**
       * Updates selected cell with text in text area if column of
       * the selected cell is "Value" column.
       *
       * @see FocusListener#focusLost
       */
      public void focusLost(FocusEvent event)
      {
         if(event.getSource() == m_valueText)
         {
            int col = m_table.getSelectedColumn();
            if(col > 0 && m_table.getColumnName(col).equals(
               ms_dlgResource.getString("colValue")))
            {
               String text = PSStringOperation.replace(m_valueText.getText(),
                  ms_dlgResource.getString("propValueSeperator"),
                  ms_dlgResource.getString("escPropValueSeperator"));

               text = text.replace('\n', ms_dlgResource.getString(
                  "propValueSeperator").charAt(0));

               m_table.setValueAt(text, m_table.getSelectedRow(), col);
            }
         }
      }

      /**
       * Updates text area with selected cell's value if column of
       * the selected cell is "Value" column.
       *
       * @see FocusListener#focusGained
       */
      public void focusGained(FocusEvent event)
      {
         if(event.getSource() == m_table)
         {
            int col = m_table.getSelectedColumn();
            int row = m_table.getSelectedRow();
            if(row >= 0 && col >= 0)
            {
               if(col > 0 && m_table.getColumnName(col).equals(
                 ms_dlgResource.getString("colValue")))
               {
                  //Sets the value in editable text area if the selected cell
                  //column is "Value" column
                  setValue(row, col);
               }
               else //col=0, focus to attribute name column
               {
                  /* When 'Re-catalog' is selected in editor, the current value
                   * in cell is saved. So the saved value before re-cataloging
                   * is set after it got focus.
                   */
                  if(m_isCatalog)
                  {
                     m_table.setValueAt(m_attrCellValue, row,col);
                     m_isCatalog = false;
                  }
               }
            }
         }
      }

   }

   /**
    * Inner class to implement ListSelectionListener for handling table
    * row selection changes.
    *
    * @see ListSelectionListener
    */
   private class CellSelectionListener implements ListSelectionListener
   {
      /**
       * Implements cell selection value changed. Adds a new row if selected
       * row is last row in the table and sets value of selected cell to text
       * area if column of the cell is "Value" Column.
       *
       * @see ListSelectionListener#valueChanged
       **/
      public void valueChanged (ListSelectionEvent e)
      {
         if(e.getValueIsAdjusting())
            return;

         int row = m_table.getSelectedRow();
         int col = m_table.getSelectedColumn();

         //if the selected row is the last row in the table add an empy row
         //to the table
         int count = m_table.getRowCount();
         if(row == count-1)
         {
            //adds an empty row
            m_tableModel.setRowCount(count+1);
         }

         //Sets the value in editable text area if the selected cell column
         //is "Value" column
         if(row >= 0 && col >= 0)
         {
            setValueEditor(row, col);
            setValue(row, col);
         }
      }
   }

   /**
    * Default Item Listener for drop-list editors.
    */
   private class EditorItemListener implements ItemListener
   {

      /**
       * Clears the editing cell value if the selected value is an empty string.
       * <br>
       * For 'Name' column editor, validates the name entered and clears the
       * value of corresponding "Value" cell of selected cell if attribute name
       * is changed to any other one of the pre-defined attribute names because
       * the value does not pertain to this new name any more.
       *
       * @see ItemListener#itemStateChanged itemStateChanged
       */
      public void itemStateChanged(ItemEvent event)
      {
         if(event.getStateChange() == ItemEvent.SELECTED)
         {
            String name = (String)event.getItem();

            if(name != null && name.trim().indexOf(' ') != -1 )
            {
               Object[] propName = new Object[1];
               propName[0] = name;
               
               final String message;
               if(name.equals(ms_dlgResource.getString("pseudoProp")))
                  message = ms_dlgResource.getString("notProperty");
               else
                  message = ms_dlgResource.getString("invalidProperty");

               PSDlgUtil.showErrorDialog(
                     Util.cropErrorMessage(
                           MessageFormat.format(message, propName)),
                     ms_dlgResource.getString("error"));

               m_nameEditor.setSelectedItem("");
            }

            int row = m_table.getEditingRow();
            int col = m_table.getEditingColumn();

            if(row >= 0 && col >= 0)
            {
               /*Set attribute value to null, if the attribute name is changed
                *to any other one of the pre-defined attribute names
                */
               String oldAttrName =  (String)m_tableModel.getValueAt(row, col);
               if( oldAttrName == null || !oldAttrName.equalsIgnoreCase(name))
               {
                  if(m_nameValues.containsKey(name))
                     m_tableModel.setValueAt(null, row, col+1);
               }
            }
         }
      }
   }

   /**
    * Cell editor for table cells. Activates cell editing only if the event
    * causing the cell editing is a mouse event.
    **/
   private class CustomCellEditor extends DefaultCellEditor
   {

      /**
       * Constructor for cell editor.
       *
       * @param comp the combo box which should be used as editor,
       * assumed not <code>null</code>.
       **/
      public CustomCellEditor(JComboBox comp)
      {
         super(comp);
      }

      /**
       * Returns <code>false</code> if the <code>event</code> is not a
       * <code>MouseEvent</code>, otherwise delegates to super and returns.
       *
       * @return   <code>true</code> if the cell is editable,
       * otherwise <code>false<code>.
       *
       * @see DefaultCellEditor#isCellEditable(EventObject)
       **/
      public boolean isCellEditable(EventObject event)
      {
         if (event instanceof MouseEvent)
            return super.isCellEditable(event);
         return true;
      }
   }

   /**
    * Collection of item listeners on name column editor, gets initialized when
    * the first listener is added.
    **/
   private Collection<ItemListener> m_nameEditorlisteners = null;

   /** Column header names, gets initialized in <code>createTable()</code>. **/
   private Vector<String> m_columnHeaders;

   /** Minimum number of rows in table. **/
   public static final int MIN_NUM_ROWS = 6;

   /**
    * Table to display role attributes, gets initialized in
    * <code>createTable()</code>.
    */
   private JTable m_table;

   /**
    * Text area for editing value of an attribute, gets initialized in
    * <code>createAttributeValuePanel()</code>.
    */
   private JTextArea m_valueText;

   /**
    * TableModel for attributes table, gets initialized in
    * <code>createTable()</code>.
    */
   private DefaultTableModel m_tableModel;

   /**
    * Flag to indicate this panel's initialization. Initialized to
    * <code>true</code> and set to <code>false</code> in constructor, if it can
    * not find resource file or property for initialization.
    **/
   private boolean m_bInitialized = true;

   /** Dialog resource strings, initialized in <code>getResources()</code>. **/
   private static ResourceBundle ms_dlgResource = null;

   /**
    * The combo box editor for "Name" column. Initialized in
    * <code>setEditors()</code> and never <code>null</code> after that.
    **/
   private JComboBox m_nameEditor = null;

   /**
    * The combo box editor for "Value" column. Initialized in
    * <code>setEditors()</code>. The values to set in this depends on
    * attribute selected in "Name" column of the row.
    **/
   private JComboBox m_valueEditor = null;

   /**
    * Map of attribute name and values with name as key and list of values as
    * data. Initialized in <code>setEditors()</code> and never
    * <code>null</code> after that.
    **/
   private Map<AdminNameValue, List> m_nameValues = null;

   /**
    * The system property to check to make attribute name editor editable.
    **/
   private static final String ALLOW_UNDEFINED_ATTRIBUTES =
      "com.percussion.AllowUndefinedAttributes";

   /**
    * The string to represent 'Re-catalog' action, initialized in
    * <code>setEditors()</code> and never <code>null</code> after that.
    **/
   public static String ms_recatalogString = null;

   /**
    * The variable to hold value in selected cell of 'Name' column. This is
    * used for remembering the value when 'Re-catalog' is selected in editor to
    * set back this value when cell gets focus after re-cataloging. Initialized
    * in {@link #resetCatalogedProperties(Map, ItemListener) }
    **/
   private String m_attrCellValue = null;

   /**
    * The flag to specify whether 'Re-catalog' is selected or not. Initialized
    * to <code>false</code> and set to <code>true</code> whenever 'Re-catalog'
    * is selected. It will be re-set to <code>false</code> after setting value
    * back in the selected cell.
    **/
   private boolean m_isCatalog = false;

}

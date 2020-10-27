/******************************************************************************
 *
 * [ DirectorySetEditorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSReference;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;
import com.percussion.guitools.PSPropertyPanel;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An editor dialog used to create new or edit existing directory service 
 * directory sets.
 */
public class DirectorySetEditorDialog extends DirectoryServiceDialog 
   implements KeyListener
{
   /**
    * Convenience constructor the calls {@link #DirectorySetEditorDialog(Frame, 
    * DirectoryServiceData, PSDirectorySet)}. See that constructor for 
    * parameter description. 
    */
   public DirectorySetEditorDialog(Frame parent, DirectoryServiceData data)
   {
      this(parent, data, new PSDirectorySet(
         DirectorySetsPanel.getDefaultDirectorySetName(), 
            PSDirectorySet.DEFAULT_OBJECT_ATTRIBUTE_NAME));
   }
   
   /**
    * Create a new directory set editor for the supplied directory set.
    * 
    * @param parent the parent frame, may be <code>null</code>.
    * @param data the current directory service data, not <code>null</code>.
    * @param directorySet the directory set for which to create the editor,
    *    not <code>null</code>.
    */
   public DirectorySetEditorDialog(Frame parent, DirectoryServiceData data, 
      PSDirectorySet directorySet)
   {
      super(parent, data);
      
      setTitle(getResources().getString("dlg.title"));
         
      initDialog();
      initData(directorySet);
   }
   
   /**
    * Initializes the dialog from the supplied data.
    * 
    * @param directorySet the directory set from which to initialize the
    *    dialog, not <code>null</code>.
    */
   public void initData(PSDirectorySet directorySet)
   {
      if (directorySet == null)
         throw new IllegalArgumentException("directorySet cannot be null");
         
      m_currentName = directorySet.getName();
      
      m_name.setText(directorySet.getName());
      
      // initialize directory selector
      initDirectorySelector();
      
      // initialize directory table
      Iterator references = directorySet.iterator();
      while (references.hasNext())
      {
         PSReference reference = (PSReference) references.next();
         PSDirectory directory = null;
         Iterator directories = m_data.getDirectories().iterator();
         while (directories.hasNext())
         {
            directory = (PSDirectory) directories.next();
            if (directory.getName().equals(reference.getName()))
               break;
            else
               directory = null;
         }

         if (directory == null)
            continue;
            
         Object[] rowData =
         {
            directory.getName(),
            DirectoriesPanel.getCatalogDisplayString(
               directory.isShallowCatalogOption() ? 
                  PSDirectory.CATALOG_SHALLOW : PSDirectory.CATALOG_DEEP),
            directory.getProviderUrl()
         };
       
         DefaultTableModel model = (DefaultTableModel) m_directories.getModel();
         model.addRow(rowData);
      }
      addEmptyRow();
      
      // initialize required attributes table
      DefaultTableModel model = 
         (DefaultTableModel) m_requiredAttributes.getModel();
      for (int i=0; i<PSDirectorySet.REQUIRED_ATTRIBUTE_NAMES_ENUM.length; i++)
      {
         String attributeKey = PSDirectorySet.REQUIRED_ATTRIBUTE_NAMES_ENUM[i];
         Object[] rowData =
         {
            attributeKey,
            directorySet.getRequiredAttributeName(attributeKey)
         };
            
         model.addRow(rowData);
      }
   }
   
   /**
    * Initialize the directory selector cell editor.
    */
   private void initDirectorySelector()
   {
      m_directorySelector.removeAllItems();
      
      m_directorySelector.addItem("");
      Iterator directories = m_data.getDirectories().iterator();
      while (directories.hasNext())
      {
         PSDirectory directory = (PSDirectory) directories.next();
         m_directorySelector.addItem(directory.getName());
      }
      directories = m_newData.getDirectories().iterator();
      while (directories.hasNext())
      {
         PSDirectory directory = (PSDirectory) directories.next();
         m_directorySelector.addItem(directory.getName());
      }
      m_directorySelector.addItem(
         getResources().getString("ctrl.directories.name.new"));
   }
   
   /**
    * Add a new directory though the directory editor dialog.
    * 
    * @return the newly added directory or <code>null</code> if none was added.
    */
   private PSDirectory addDirectory()
   {
      DirectoryEditorDialog editor = new DirectoryEditorDialog(
         (Frame) getParent(), m_data);
      editor.setVisible(true);

      if (editor.isOk())
      {
         PSDirectory directory = editor.getDirectory();
         m_newData.addDirectory(directory);
         m_newData.addAll(editor.getNewData());
         
         return directory;
      }
      
      return null;
   }
   
   /**
    * Add a new directory to the directories table.
    * 
    * @param row the row index of the supplied cell editor source.
    * @param source the cell editor which provides the selected directory to
    *    be added, assumed not <code>null</code>.
    */
   private void onAdd(int row, JComboBox source)
   {
      String selectedItem = source.getSelectedItem().toString();
      
      if (selectedItem.equalsIgnoreCase(
         getResources().getString("ctrl.directories.name.new")))
      {
         PSDirectory directory = addDirectory();
         if (directory != null)
         {
            int index = source.getItemCount()-1;
            source.insertItemAt(directory.getName(), index);
            source.setSelectedIndex(index);

            updateDirectory(directory, row);
            addEmptyRow();
         }
      }
      else
      {
         String currentValue = (String) m_directories.getValueAt(row, 0);
         if (currentValue != null && currentValue.equals(selectedItem))
            return;
         
         PSDirectory directory = m_data.getDirectory(selectedItem);
         updateDirectory(directory, row);
         
         addEmptyRow();
      }
   }
   
   /**
    * Edit the directory in the directories table.
    * 
    * @param row the row index of the supplied cell editor source.
    * @param source the cell editor which provides the selected directory to
    *    be edited, assumed not <code>null</code>.
    */
   private void onEdit(int row, JComboBox source)
   {
      String selectedItem = source.getSelectedItem().toString();
      if (selectedItem.equalsIgnoreCase(
         getResources().getString("ctrl.directories.name.new")))
      {
         PSDirectory directory = addDirectory();
         if (directory != null)
         {
            int index = source.getItemCount()-1;
            source.insertItemAt(directory.getName(), index);
            source.setSelectedIndex(index);

            updateDirectory(directory, row);
         }
      }
      else
      {
         PSDirectory directory = m_data.getDirectory(selectedItem);
         if (directory == null)
         {
            Iterator directories = m_newData.getDirectories().iterator();
            while (directories.hasNext())
            {
               directory = (PSDirectory) directories.next();
               if (directory.getName().equals(selectedItem))
                  break;
               else
                  directory = null;
            }
         }
         
         if (directory != null)
            updateDirectory(directory, row);
      }
   }
   
   /**
    * Update the directory table for the supplied directory and row.
    * 
    * @param directory the directory information with which the supplied row
    *    will be updated, assumed not <code>null</code>.
    * @param row the row index at which to update the directory.
    */
   private void updateDirectory(PSDirectory directory, int row)
   {
      Object[] rowData =
      {
         directory.getName(),
         DirectoriesPanel.getCatalogDisplayString(
            directory.isShallowCatalogOption() ? 
               PSDirectory.CATALOG_SHALLOW : PSDirectory.CATALOG_DEEP),
         directory.getProviderUrl()
      };
       
      DefaultTableModel model = (DefaultTableModel) m_directories.getModel();
      model.removeRow(row);
      model.insertRow(row, rowData);
   }
   
   /**
    * Delete a directory from the directories table.
    * 
    * @param source the cell editor which provides the selected directory to
    *    be deleted, assumed not <code>null</code>.
    */
   private void onDeleteDirectory(JComboBox source)
   {
      DefaultTableModel model = (DefaultTableModel) m_directories.getModel();
      int row = m_directories.rowAtPoint(
         new Point(source.getX(), source.getY()));
         
      // never delete the last row
      if (!(row == model.getRowCount()-1))
         model.removeRow(row);
   }
  
   /**
    * Add an empty row to the directories table.
    */
   private void addEmptyRow()
   {
      DefaultTableModel model = (DefaultTableModel) m_directories.getModel();
      Object[] emptyRow = { "", "", "" };
      model.addRow(emptyRow);
   }
   
   /**
    * @return a directory set object built from all editor controls,
    *    never <code>null</code>.
    */
   public PSDirectorySet getDirectorySet()
   {
      String name = m_name.getText();
      
      DefaultTableModel model = 
         (DefaultTableModel) m_requiredAttributes.getModel();
         
      Map<String, String> attributeNames = new HashMap<String, String>();
      for (int i=0; i<model.getRowCount(); i++)
      {
         String key = (String) model.getValueAt(i, 0);
         String value = (String) model.getValueAt(i, 1);
         
         attributeNames.put(key, value);
      }
      
      PSDirectorySet directorySet =  new PSDirectorySet(name, 
            attributeNames.get(PSDirectorySet.OBJECT_ATTRIBUTE_KEY));
         
      String emailAttributeName = 
            attributeNames.get(PSDirectorySet.EMAIL_ATTRIBUTE_KEY);
      if (emailAttributeName != null && emailAttributeName.trim().length() == 0)
         emailAttributeName = null;
      directorySet.setEmailAttributeName(emailAttributeName);
      
      String roleAttributeName = 
            attributeNames.get(PSDirectorySet.ROLE_ATTRIBUTE_KEY);
      if (roleAttributeName != null && roleAttributeName.trim().length() == 0)
         roleAttributeName = null;
      directorySet.setRoleAttributeName(roleAttributeName);
         
      model = (DefaultTableModel) m_directories.getModel();
      for (int i=0; i<model.getRowCount(); i++)
      {
         String directoryName = (String) model.getValueAt(i, 0);
         if (directoryName.trim().length() > 0)
         {
            PSReference reference = new PSReference(
               directoryName, PSDirectory.class.getName());
            directorySet.add(reference);
         }
      }
         
      return directorySet;
   }
   
   /**
    * Overrides super class to validate the name uniqueness.
    */
   @Override
   public void onOk()
   {
      stopTableEditor(m_directories);
      stopTableEditor(m_requiredAttributes);

      if (!activateValidation())
         return;
       
      // name must be unique  
      String name = m_name.getText();
      if (!m_currentName.equals(name) && 
         m_data.getDirectorySets().contains(name))
      {
         JOptionPane.showMessageDialog(null, 
            getResources().getString("error.msg.notunique"), 
            getResources().getString("error.title"), JOptionPane.ERROR_MESSAGE);
            
         return; 
      }
      
      // must have at least one directory selected
      boolean foundValidDirectory = false;
      DefaultTableModel model = (DefaultTableModel) m_directories.getModel();
      for (int i=0; i<model.getRowCount(); i++)
      {
         String directory = (String) model.getValueAt(i, 0);
         if (directory != null && directory.trim().length() > 0)
         {
            foundValidDirectory = true;
            break;
         }
      }
      if (!foundValidDirectory)
      {
         JOptionPane.showMessageDialog(null, 
            getResources().getString("error.msg.nodirectory"), 
            getResources().getString("error.title"), JOptionPane.ERROR_MESSAGE);
               
         return; 
      }
      
      model = (DefaultTableModel) m_requiredAttributes.getModel();
      for (int i=0; i<model.getRowCount(); i++)
      {
         String key = (String) model.getValueAt(i, 0);
         if (key.equals(PSDirectorySet.OBJECT_ATTRIBUTE_KEY))
         {
            String value = (String) model.getValueAt(i, 1);
            if (StringUtils.isBlank(value))
            {
               String message = MessageFormat.format(
                  getResources().getString("error.msg.norequiredattributename"),
                  new Object[] { key });
                  
               JOptionPane.showMessageDialog(null, message, 
                  getResources().getString("error.title"), 
                  JOptionPane.ERROR_MESSAGE);
            
               return; 
            }
         }
      }
      
      super.onOk();
   }
   
   /**
    * @see KeyListener#keyPressed(KeyEvent)
    */
   public void keyPressed(@SuppressWarnings("unused") KeyEvent event)
   {
      // noop
   }

   /**
    * @see KeyListener#keyReleased(KeyEvent)
    */
   public void keyReleased(KeyEvent event)
   {
      Object source = event.getSource();
      switch (event.getKeyCode())
      {
         case KeyEvent.VK_DELETE:
            if (source instanceof DirectoriesTable)
            {
               DirectoriesTable table = (DirectoriesTable) source;
               DefaultCellEditor editor = 
                  (DefaultCellEditor) table.getDefaultEditor(
                     table.getColumnClass(0));
               editor.stopCellEditing();
               onDeleteDirectory((JComboBox) editor.getComponent());
            }
            else if (source instanceof RequiredAttributesTable)
            {
               RequiredAttributesTable table = (RequiredAttributesTable) source;
               table.setValueAt("", table.getSelectedRow(), 1);
            }
            break;
      }
   }

   /**
    * @see KeyListener#keyTyped(KeyEvent)
    */
   public void keyTyped(@SuppressWarnings("unused") KeyEvent event)
   {
      // noop
   }
   
   /**
    * Initializes the dialogs UI.
    */
   private void initDialog()
   {
      JPanel panel = new JPanel(new BorderLayout(20, 10));
      panel.setBorder((new EmptyBorder (5, 5, 5, 5)));
      getContentPane().add(panel);

      panel.add(createPropertyPanel(), BorderLayout.CENTER);
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true),
                   BorderLayout.EAST);
      panel.add(cmdPanel, BorderLayout.SOUTH);

      initValidationFramework();
      
      setResizable(true);
      pack();
      center();
   }
   
   /**
    * @return the new created property panel, never <code>null</code>.
    */
   private JPanel createPropertyPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(createGeneralPropertiesPanel());
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());
      panel.add(createDirectoriesPanel());
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());
      panel.add(createRequiredAttributesPanel());

      return panel;
   }
   
   /**
    * @return the new created general property panel, never <code>null</code>.
    */
   private JPanel createGeneralPropertiesPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      m_name.setToolTipText(getResources().getString("ctrl.name.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.name"),
         new JComponent[] { m_name });

      return panel;
   }
   
   /**
    * @return the new created directories panel, never <code>null</code>.
    */
   private JPanel createDirectoriesPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new TitledBorder(getResources().getString(
         "border.directories")));
         
      String[] columnNames = 
      {
         getResources().getString("ctrl.directories.name"),
         getResources().getString("ctrl.directories.catalog"),
         getResources().getString("ctrl.directories.providerurl")
      };
      panel.add(createDirectoriesTablePanel(columnNames, 0));
      
      return panel;
   }
   
   /**
    * @return the new created required attributes panel, never <code>null</code>.
    */
   private JPanel createRequiredAttributesPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new TitledBorder(getResources().getString(
         "border.requiredattributes")));
         
      String[] columnNames = 
      {
         getResources().getString("ctrl.attributes.name"),
         getResources().getString("ctrl.attributes.value")
      };
      panel.add(createRequiredAttributesTablePanel(columnNames, 0));
         
      return panel;
   }
   
   /**
    * Creates the directories table panel.
    * 
    * @param columnNames an array of <code>String</code> objects with all
    *    column names in the order from left to right, assumed not 
    *    <code>null</code> or empty.
    * @param rows the number of empty rows to be initialized.
    * @return the table panel, never <code>null</code>.
    */
   private JPanel createDirectoriesTablePanel(Object[] columnNames, int rows)
   {
      m_directories = new DirectoriesTable(
         new DefaultTableModel(columnNames, rows));
      m_directories.setCellSelectionEnabled(false);
      m_directories.setColumnSelectionAllowed(false);
      m_directories.setRowSelectionAllowed(true);
      m_directories.addKeyListener(this);

      DefaultCellEditor editor = new DefaultCellEditor(m_directorySelector);
      editor.setClickCountToStart(2);      
      m_directories.setDefaultEditor(m_directories.getColumnClass(0), editor);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JScrollPane(m_directories), BorderLayout.CENTER);
      panel.setPreferredSize(new Dimension(400, 200));
      
      return panel;
   }
   
   /**
    * Creates the required attributes table panel.
    * 
    * @param columnNames an array of <code>String</code> objects with all
    *    column names in the order from left to right, assumed not 
    *    <code>null</code> or empty.
    * @param rows the number of empty rows to be initialized.
    * @return the table panel, never <code>null</code>.
    */
   private JPanel createRequiredAttributesTablePanel(Object[] columnNames, 
      int rows)
   {
      m_requiredAttributes = new RequiredAttributesTable(
         new DefaultTableModel(columnNames, rows));
      m_requiredAttributes.setCellSelectionEnabled(false);
      m_requiredAttributes.setColumnSelectionAllowed(false);
      m_requiredAttributes.setRowSelectionAllowed(true);
      m_requiredAttributes.addKeyListener(this);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JScrollPane(m_requiredAttributes), BorderLayout.CENTER);
      panel.setPreferredSize(new Dimension(400, 100));
      
      return panel;
   }

   /**
    * Initialize the validation framework for this dialog.
    */
   private void initValidationFramework()
   {
      List<JComponent> comps = new ArrayList<JComponent>();
      List<ValidationConstraint> validations = new ArrayList<ValidationConstraint>();
      StringConstraint nonEmpty = new StringConstraint();

      // name: cannot be empty
      comps.add(m_name);
      validations.add(nonEmpty);

      Component[] components = new Component[comps.size()];
      comps.toArray(components);
      
      ValidationConstraint[] constraints = 
         new ValidationConstraint[validations.size()];
      validations.toArray(constraints);
      
      setValidationFramework(components, constraints);
   }
   
   /**
    * The directories table.
    */
   private class DirectoriesTable extends JTable
   {
      /**
       * @see JTable#JTable(TableModel) for description.
       */
      public DirectoriesTable(TableModel model)
      {
         super(model);
      }
      
      /**
       * Overridden to make only the first column editable.
       * @see JTable#isCellEditable(int, int) for details.
       */
      @Override
      public boolean isCellEditable(int row, int column)
      {
         if (column > 0)
            return false;
            
         return super.isCellEditable(row, column);
      }
      
      /**
       * Overridden to update the table after an edit action.
       * @see JTable#editingStopped(ChangeEvent) for details.
       */
      @Override
      public void editingStopped(ChangeEvent event)
      {
         DefaultCellEditor editor = (DefaultCellEditor) event.getSource();
         JComboBox selector = (JComboBox) editor.getComponent();
         String selectedItem = selector.getSelectedItem().toString();
         if (selectedItem.equals(""))
            onDeleteDirectory(selector);
         else
         {
            int row = m_directories.rowAtPoint(
               new Point(selector.getX(), selector.getY()));
               
            String catalogValue = m_directories.getValueAt(row, 1).toString();
            if (catalogValue != null && catalogValue.trim().length() > 0)
               onEdit(row, selector);
            
            onAdd(row, selector);
         }
         
         super.editingStopped(event);
      }
   }
   
   /**
    * The required attributes table.
    */
   private class RequiredAttributesTable extends JTable
   {
      /**
       * @see JTable#JTable(TableModel) for description.
       */
      public RequiredAttributesTable(TableModel model)
      {
         super(model);
      }
      
      /**
       * Overridden to make only the second column editable.
       * @see JTable#isCellEditable(int, int) for details.
       */
      @Override
      public boolean isCellEditable(int row, int column)
      {
         if (column == 0)
            return false;
            
         return super.isCellEditable(row, column);
      }
   }
   
   /**
    * The directory set name at initialization time. Used to validate the
    * name for uniqueness. Never <code>null</code> or changed.
    */
   private String m_currentName = null;
   
   /**
    * The directory set name, it's value cannot be empty and must be unique 
    * across all other directory sets in this server.
    */
   private JTextField m_name = new JTextField();
   
   /**
    * The directories table, initialized in 
    * {@link #createDirectoriesTablePanel(Object[], int)}, never 
    * <code>null</code> after that.
    */
   private JTable m_directories = null;
   
   /**
    * The directory selector cell editor, initialized in 
    * {@link #initDirectorySelector()}.
    */
   private JComboBox m_directorySelector = new JComboBox();
   
   /**
    * The required attributes table, initialized in 
    * {@link #createRequiredAttributesTablePanel(Object[], int)}, never 
    * <code>null</code> after that.
    */
   private JTable m_requiredAttributes = null;
}

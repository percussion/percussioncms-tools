/******************************************************************************
 *
 * [ DirectoriesPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSServerConfiguration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * This panel provides the UI to view and configure directories used in 
 * directory services.
 */
public class DirectoriesPanel extends DirectoryServiceBasePanel
{
   /**
    * Constructs the panel for the supplied parameters.
    * 
    * @param parent the parent frame, not <code>null</code>.
    */
   public DirectoriesPanel(Frame parent)
   {
      super(parent);
      
      initPanel();
      initData();
   }
   
   /**
    * @see com.percussion.E2Designer.admin.ITabDataHelper#saveTabData()
    */
   public boolean saveTabData()
   {
      if (isModified())
      {
         PSServerConfiguration config = getConfig();
         config.removeAllDirectories();
         
         Iterator directories = m_data.getDirectories().iterator();
         while (directories.hasNext())
         {
            PSDirectory directory = 
               (PSDirectory) directories.next();
               
            config.addDirectory(directory);
         }
         
         setModified(false);

         return true;
      }
      
      return false;
   }

   /**
    * @see com.percussion.E2Designer.admin.ITabDataHelper#validateTabData()
    */
   public boolean validateTabData()
   {
      Iterator directories = m_data.getDirectories().iterator();
      while (directories.hasNext())
      {           
         PSDirectory directory = (PSDirectory) directories.next();
         
         PSReference ref = directory.getAuthenticationRef();
         PSAuthentication authentication = m_data.getAuthentication(
            ref.getName());
         if (authentication == null)
         {
            Object[] args = 
            {
               ref.getName(),
               directory.getName()
            };
            
            String message = MessageFormat.format(getResources().getString(
               "dir.error.missingauthentication"), args);
            
            JOptionPane.showMessageDialog(getParent(), message, 
               getResources().getString("dir.error.title"), 
               JOptionPane.ERROR_MESSAGE);
                  
            return false;
         }
      }
      
      return true;
   }

   /* (non-Javadoc)
    * @see ChangeListener#stateChanged(ChangeEvent)
    */
   public void stateChanged(ChangeEvent event)
   {
      boolean needUpdate = false;
      
      Object source = event.getSource();
      if (source instanceof DirectoryServiceData)
      {
         Iterator directories = 
            ((DirectoryServiceData) source).getDirectories().iterator();
         while (directories.hasNext())
         {
            Object directory = directories.next();
            if (!m_data.getDirectories().contains(directory))
            {
               m_data.addDirectory((PSDirectory) directory);
               needUpdate = true;
               setModified(true);
            }
         }
         
         if (needUpdate)
            initData();
      }
   }
   
   /**
    * @return the default directory name used in the editor dialog to create
    *    new directory entries, is unique across one edit session, 
    *    never <code>null</code> or empty.
    */
   public static String getDefaultDirectoryName()
   {
      return getResources().getString("dir.dir.defaultname") + 
         ms_directoryNameSuffix++;
   }
   
   /**
    * Get the catalog display string.
    * 
    * @param catalog the catalog option for which to get the display  string, 
    *    not <code>null</code>.
    * @return the display string for the supplied catalog option, never 
    *    <code>null</code>.
    */
   public static String getCatalogDisplayString(String catalog)
   {
      return getResources().getString("dir.dir.catalog." + catalog);
   }
   
   /**
    * Opens the directory editor to create a new directory. Depending 
    * on the dialog return value the directory table will be updated 
    * for the newly added directory.
    */
   protected void onAdd()
   {
      DirectoryEditorDialog editor = new DirectoryEditorDialog(m_parent,
         m_data);
      editor.show();
      
      if (editor.isOk())
      {
         PSDirectory directory = editor.getDirectory();
         dataChanged(editor.getNewData());
         
         m_data.addDirectory(directory);
      
         m_model.addRow(getRowData(directory));
         setModified(true);
      }
   }
   
   /**
    * Opens the directory editor for the currently selected directory.
    * Depending on the dialog return value, the directory table will be
    * updated for the edited directory.
    */
   protected void onEdit()
   {
      int row = m_table.getSelectedRow();
      String name = (String) m_model.getValueAt(row, 0);
      PSDirectory directory = m_data.getDirectory(name);
      
      DirectoryServicePanel containerPanel = getContainerPanel();
      if (containerPanel == null)
         throw new IllegalStateException("container panel must be set");
         
      DirectoryEditorDialog editor = new DirectoryEditorDialog(m_parent,
         m_data, directory);
      editor.show();
      
      if (editor.isOk())
      {
         m_data.removeDirectory(directory);
         
         directory = editor.getDirectory();
         dataChanged(editor.getNewData());
         
         m_data.addDirectory(directory);

         m_model.removeRow(row);
         m_model.insertRow(row, getRowData(directory));
         setModified(true);
      }
   }
   
   /**
    * Get the row data for the supplied directory.
    * 
    * @param directory the directory for which to get the row data,
    *    assumed not <code>null</code>.
    * @return the row data for the supplied directory as an array of
    *    <code>Object</code>, never <code>null</code>.
    */
   private Object[] getRowData(PSDirectory directory)
   {
      Object[] rowData =
      {
         directory.getName(),
         directory.isDeepCatalogOption()
            ? getResources().getString("dir.dir.catalog.deep")
            : getResources().getString("dir.dir.catalog.shallow"),
         directory.getProviderUrl()
      };
      
      return rowData;
   }
   
   /**
    * Pops up a dialog to ask the user for a confirmation to delete the 
    * currently selected directory. Depending on the dialog return value,
    * the directory table will be updated for the deleted directory.
    */
   protected void onDelete()
   {
      String names = "";
      int[] rows = m_table.getSelectedRows();
      for (int i=0; i<rows.length; i++)
      {
         String name = (String) m_model.getValueAt(rows[i], 0);
         names += name;
         if (i < rows.length-1)
            names += ", ";
      }
      
      Object[] args = { names };
      String message = MessageFormat.format(
         getResources().getString("dir.dir.delete.message"), args);
         
      int choice = JOptionPane.showConfirmDialog(null, message, 
         getResources().getString("dir.dir.delete.title"), 
            JOptionPane.YES_NO_OPTION);
      if (choice == JOptionPane.YES_OPTION)
      {
         m_table.clearSelection();
         for (int i=rows.length-1; i>=0; i--)
         {
            m_data.removeDirectory((String) m_model.getValueAt(rows[i], 0));
            m_model.removeRow(rows[i]);
         }
         
         setModified(true);
      }
   }
   
   /**
    * Initializes the panel with all UI components supplied.
    */
   protected void initPanel()
   {
      setLayout(new BorderLayout());
      
      String[] columnNames = 
      {
         getResources().getString("dir.dir.name"),
         getResources().getString("dir.dir.catalog"),
         getResources().getString("dir.dir.providerurl")
      };
      
      add(createTablePanel(columnNames, 0), BorderLayout.CENTER);
      add(createCommandPanel(), BorderLayout.SOUTH);
      
      super.initPanel();
   }
   
   /**
    * See {@link BasePanel#initData()} for description.
    */
   protected void initData()
   {
      super.initData();
      
      Iterator directories = m_data.getDirectories().iterator();
      while (directories.hasNext())
      {           
         PSDirectory directory = (PSDirectory) directories.next();
            
         Object[] rowData =
         {
            directory.getName(),
            directory.isDeepCatalogOption() ? 
               getResources().getString("dir.dir.catalog.deep") : 
               getResources().getString("dir.dir.catalog.shallow"),
            directory.getProviderUrl()
         };
            
         m_model.addRow(rowData);
      }
   }
   
   /**
    * The default directory name suffix. Will be incremented in each
    * call to {@link getDefaultDirectoryName()}.
    */
   private static int ms_directoryNameSuffix = 0;
}

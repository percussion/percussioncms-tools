/******************************************************************************
 *
 * [ DirectorySetsPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSServerConfiguration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * This panel provides the UI to view and configure directory sets used in 
 * directory services.
 */
public class DirectorySetsPanel extends DirectoryServiceBasePanel
{
   /**
    * Constructs the panel for the supplied parameters.
    * 
    * @param parent the parent frame, not <code>null</code>.
    */
   public DirectorySetsPanel(Frame parent)
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
         config.removeAllDirectorySets();
         
         Iterator directorySets = m_data.getDirectorySets().iterator();
         while (directorySets.hasNext())
         {
            PSDirectorySet directorySet = 
               (PSDirectorySet) directorySets.next();
               
            config.addDirectorySet(directorySet);
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
      Iterator directorySets = m_data.getDirectorySets().iterator();
      while (directorySets.hasNext())
      {           
         PSDirectorySet directorySet = (PSDirectorySet) directorySets.next();
         for (int i=0; i<directorySet.size(); i++)
         {
            PSReference ref = (PSReference) directorySet.get(i);
            
            PSDirectory test = m_data.getDirectory(ref.getName());
            if (test == null)
            {
               Object[] args = 
               {
                  ref.getName(),
                  directorySet.getName()
               };
            
               String message = MessageFormat.format(getResources().getString(
                  "dir.error.missingdirectory"), args);
            
               JOptionPane.showMessageDialog(getParent(), message, 
                  getResources().getString("dir.error.title"), 
                  JOptionPane.ERROR_MESSAGE);
                  
               return false;
            }
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
         Iterator directorySets = 
            ((DirectoryServiceData) source).getDirectorySets().iterator();
         while (directorySets.hasNext())
         {
            Object directorySet = directorySets.next();
            if (!m_data.getDirectorySets().contains(directorySet))
            {
               m_data.addDirectorySet((PSDirectorySet) directorySet);
               needUpdate = true;
               setModified(true);
            }
         }
         
         if (needUpdate)
            initData();
      }
   }
   
   /**
    * @return the default directory set name, is unique across one edit
    *    session, never <code>null</code> or empty.
    */
   public static String getDefaultDirectorySetName()
   {
      return getResources().getString("dir.dirsets.defaultname") + 
         ms_directorySetNameSuffix++;
   }
   
   /**
    * Opens the directory set editor to create a new directory set. Depending 
    * on the dialog return value the directory set table will be updated 
    * for the newly added directory set.
    */
   protected void onAdd()
   {
      DirectorySetEditorDialog editor = new DirectorySetEditorDialog(m_parent, 
         m_data);
      editor.show();
      
      if (editor.isOk())
      {
         PSDirectorySet directorySet = editor.getDirectorySet();
         dataChanged(editor.getNewData());
         
         m_data.addDirectorySet(directorySet);
      
         m_model.addRow(getRowData(directorySet));
         setModified(true);
      }
   }
   
   /**
    * Get the directories string for the supplied directory set.
    * 
    * @param directorySet teh directory set for which to get the directories 
    *    string, assumed not <code>null</code>.
    * @return a string with all directory names of the directory set as a coma
    *    separated list, never <code>null</code>, may be empty.
    */
   private String getDirectoriesString(PSDirectorySet directorySet)
   {
      StringBuffer directoriesString = new StringBuffer();
      
      Iterator directories = directorySet.iterator();
      while (directories.hasNext())
      {
         PSReference directory = (PSReference) directories.next();
         directoriesString.append(directory.getName());
         if (directories.hasNext())
            directoriesString.append(", ");             
      }
      
      return directoriesString.toString();
   }
   
   /**
    * Opens the directory set editor for the currently selected directory set.
    * Depending on the dialog return value, the directory set table will be
    * updated for the edited directory set.
    */
   protected void onEdit()
   {
      int row = m_table.getSelectedRow();
      String name = (String) m_model.getValueAt(row, 0);
      PSDirectorySet directorySet = m_data.getDirectorySet(name);
      
      DirectorySetEditorDialog editor = new DirectorySetEditorDialog(
         m_parent, m_data, directorySet);
      editor.show();
      
      if (editor.isOk())
      {
         m_data.removeDirectorySet(directorySet);
         
         directorySet = editor.getDirectorySet();
         dataChanged(editor.getNewData());
         
         m_data.addDirectorySet(directorySet);
         
         m_model.removeRow(row);
         m_model.insertRow(row, getRowData(directorySet));
         setModified(true);
      }
   }
   
   /**
    * Pops up a dialog to ask the user for a confirmation to delete the 
    * currently selected directory set. Depending on the dialog return value,
    * the directory set table will be updated for the deleted directory set.
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
         getResources().getString("dir.dirsets.delete.message"), args);
         
      int choice = JOptionPane.showConfirmDialog(null, message, 
         getResources().getString("dir.dirsets.delete.title"), 
            JOptionPane.YES_NO_OPTION);
      if (choice == JOptionPane.YES_OPTION)
      {
         m_table.clearSelection();
         for (int i=rows.length-1; i>=0; i--)
         {
            m_data.removeDirectorySet(
               (String) m_model.getValueAt(rows[i], 0));
            m_model.removeRow(rows[i]);
         }
         
         setModified(true);
      }
   }
   
   /**
    * Get the row data for the supplied directory set.
    * 
    * @param directorySet the directory set for which to get the row data,
    *    assumed not <code>null</code>.
    * @return the row data for the supplied directory set as an array of
    *    <code>Object</code>, never <code>null</code>.
    */
   private Object[] getRowData(PSDirectorySet directorySet)
   {
      Object[] rowData =
      {
         directorySet.getName(),
         getDirectoriesString(directorySet)
      };
      
      return rowData;
   }
   
   /**
    * Initializes the panel with all UI components supplied.
    */
   protected void initPanel()
   {
      setLayout(new BorderLayout());
      
      String[] columnNames = 
      {
         getResources().getString("dir.dirsets.name"),
         getResources().getString("dir.dirsets.directories")
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
      
      Iterator directorySets = m_data.getDirectorySets().iterator();
      while (directorySets.hasNext())
      {           
         PSDirectorySet directorySet = (PSDirectorySet) directorySets.next();
            
         Object[] rowData =
         {
            directorySet.getName(),
            getDirectoriesString(directorySet)
         };
            
         m_model.addRow(rowData);
      }
   }
   
   /**
    * The default directory set name suffix. Will be incremented in each
    * call to {@link getDefaultDirectorySetName()}.
    */
   private static int ms_directorySetNameSuffix = 0;
}

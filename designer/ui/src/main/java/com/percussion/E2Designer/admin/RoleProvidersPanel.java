/******************************************************************************
 *
 * [ RoleProvidersPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.design.objectstore.PSServerConfiguration;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * This panel provides the UI to view and configure role providers used for
 * directory services.
 */
public class RoleProvidersPanel extends DirectoryServiceBasePanel
{
   /**
    * Java serial version id
    */
   private static final long serialVersionUID = 1L;

   /**
    * Constructs the panel for the supplied parameters.
    * 
    * @param parent the parent frame, not <code>null</code>.
    */
   public RoleProvidersPanel(Frame parent)
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
         config.removeAllRoleProviders();
         
         Iterator providers = m_data.getRoleProviders().iterator();
         while (providers.hasNext())
         {
            PSRoleProvider provider = (PSRoleProvider) providers.next();
               
            config.addRoleProvider(provider);
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
      Iterator roleProviders = m_data.getRoleProviders().iterator();
      while (roleProviders.hasNext())
      {           
         PSRoleProvider roleProvider = (PSRoleProvider) roleProviders.next();
         if (!roleProvider.isDirectoryRoleProvider() && 
            !roleProvider.isBothProvider())
            continue;

         /*
          * Validate that the directory set used for this role provider is 
          * still available.
          */
         PSReference ref = roleProvider.getDirectoryRef();
         PSDirectorySet directorySet = m_data.getDirectorySet(
            ref.getName());
         if (directorySet == null)
         {
            Object[] args = 
            {
               ref.getName(),
               roleProvider.getName()
            };
            
            String message = MessageFormat.format(getResources().getString(
               "dir.error.missingdirectoryset"), args);
            
            JOptionPane.showMessageDialog(getParent(), message, 
               getResources().getString("dir.error.title"), 
               JOptionPane.ERROR_MESSAGE);
                  
            return false;
         }

         /*
          * Since the directory set is used as role provider it must specify 
          * the role attribute name.
          */
         if (directorySet.getRequiredAttributeName(
            PSDirectorySet.ROLE_ATTRIBUTE_KEY) == null)
         {
            Object[] args = 
            {
               directorySet.getName(),
               roleProvider.getName()
            };
         
            String message = MessageFormat.format(getResources().getString(
               "dir.error.missingroleattribute"), args);
         
            JOptionPane.showMessageDialog(getParent(), message, 
               getResources().getString("dir.error.title"), 
               JOptionPane.ERROR_MESSAGE);
               
            return false;
         }
      }
      
      return true;
   }
   
   /**
    * @return the default role provider name, is unique across one edit
    *    session, never <code>null</code> or empty.
    */
   public static String getDefaultRoleProviderName()
   {
      return getResources().getString("dir.role.defaultname") + 
         ms_roleProviderNameSuffix++;
   }
   
   /**
    * Opens the role provider editor to create a new role provider. Depending 
    * on the dialog return value the role provider table will be updated 
    * for the newly added role provider.
    */
   protected void onAdd()
   {
      RoleProviderEditorDialog editor = 
         new RoleProviderEditorDialog(m_parent, m_data);
      editor.setVisible(true);
      
      if (editor.isOk())
      {
         PSRoleProvider provider = editor.getRoleProvider();
         dataChanged(editor.getNewData());
         
         m_data.addRoleProvider(provider);
      
         m_model.addRow(getRowData(provider));
         setModified(true);
      }
   }
   
   /**
    * Opens the role provider editor for the currently selected role provider.
    * Depending on the dialog return value, the role provider table will be
    * updated for the edited role provider.
    */
   protected void onEdit()
   {
      int row = m_table.getSelectedRow();
      String name = (String) m_model.getValueAt(row, 0);
      PSRoleProvider provider = m_data.getRoleProvider(name);
      
      DirectoryServicePanel containerPanel = getContainerPanel();
      if (containerPanel == null)
         throw new IllegalStateException("container panel must be set");

      RoleProviderEditorDialog editor = new RoleProviderEditorDialog(
         m_parent, m_data, provider);
      editor.setVisible(true);
      
      if (editor.isOk())
      {
         m_data.removeRoleProvider(provider);
         
         provider = editor.getRoleProvider();
         dataChanged(editor.getNewData());
         
         m_data.addRoleProvider(provider);
         
         m_model.removeRow(row);
         m_model.insertRow(row, getRowData(provider));
         setModified(true);
      }
   }
   
   /**
    * Get the row data for the supplied role provider.
    * 
    * @param provider the role provider for which to get the row data,
    *    assumed not <code>null</code>.
    * @return the row data for the supplied role provider as an array of
    *    <code>Object</code>, never <code>null</code>.
    */
   private Object[] getRowData(PSRoleProvider provider)
   {
      Object[] rowData =
      {
         provider.getName(),
         getRoleProviderTypeDisplayString(provider)
      };
      
      return rowData;
   }
   
   /**
    * Pops up a dialog to ask the user for a confirmation to delete the 
    * currently selected role provider. Depending on the dialog return value,
    * the role provider table will be updated for the deleted role provider.
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
         getResources().getString("dir.role.delete.message"), args);
         
      int choice = JOptionPane.showConfirmDialog(null, message, 
         getResources().getString("dir.role.delete.title"), 
            JOptionPane.YES_NO_OPTION);
      if (choice == JOptionPane.YES_OPTION)
      {
         m_table.clearSelection();
         for (int i=rows.length-1; i>=0; i--)
         {
            m_data.removeRoleProvider(
               (String) m_model.getValueAt(rows[i], 0));
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
         getResources().getString("dir.role.name"),
         getResources().getString("dir.role.type")
      };
      
      add(createTablePanel(columnNames, 0), BorderLayout.CENTER);
      add(createCommandPanel(), BorderLayout.SOUTH);

      super.initPanel();
   }
   
   /**
    * See {@link DirectoryServiceBasePanel#initData()} for description.
    */
   protected void initData()
   {
      super.initData();
      
      Iterator roleProviders = m_data.getRoleProviders().iterator();
      while (roleProviders.hasNext())
      {           
         PSRoleProvider roleProvider = (PSRoleProvider) roleProviders.next();
         
         Object[] rowData =
         {
            roleProvider.getName(),
            getRoleProviderTypeDisplayString(roleProvider)
         };
            
         m_model.addRow(rowData);
      }
   }
   
   /**
    * Get the role provider type display string.
    * 
    * @param roleProvider the role provider for which to get the type display
    *    string, assumed not <code>null</code>.
    * @return the role provider display string, never <code>null</code>.
    */
   private String getRoleProviderTypeDisplayString(PSRoleProvider roleProvider)
   {
      String roleProviderType = "";
      if (roleProvider.isDirectoryRoleProvider())
         roleProviderType = getResources().getString(
            "dir.role.type.directory");
      else if (roleProvider.isBackendRoleProvider())
         roleProviderType = getResources().getString(
            "dir.role.type.backend");
      if (roleProvider.isBothProvider())
         roleProviderType = getResources().getString(
            "dir.role.type.both");
            
      return roleProviderType;            
   }
   
   /**
    * The default role provider name suffix. Will be incremented in each
    * call to {@link #getDefaultRoleProviderName()}.
    */
   private static int ms_roleProviderNameSuffix = 0;
}

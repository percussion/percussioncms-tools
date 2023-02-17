/******************************************************************************
 *
 * [ AuthenticationsPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSServerConfiguration;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * This panel provides the UI to view and configure authentications used for
 * directory services.
 */
public class AuthenticationsPanel extends DirectoryServiceBasePanel
{
   /**
    * Constructs the panel for the supplied parameters.
    * 
    * @param parent the parent frame, not <code>null</code>.
    */
   public AuthenticationsPanel(Frame parent)
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
         config.removeAllAuthentications();
         
         Iterator authentications = m_data.getAuthentications().iterator();
         while (authentications.hasNext())
         {
            PSAuthentication authentication = 
               (PSAuthentication) authentications.next();
               authentication.setEncryptPwd(false);
            config.addAuthentication(authentication);
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
         Iterator authentications = 
            ((DirectoryServiceData) source).getAuthentications().iterator();
         while (authentications.hasNext())
         {
            Object authentication = authentications.next();
            if (!m_data.getAuthentications().contains(authentication))
            {
               m_data.addAuthentication((PSAuthentication) authentication);
               needUpdate = true;
               setModified(true);
            }
         }
         
         if (needUpdate)
            initData();
      }
   }
   
   /**
    * @return the default authentication name, is unique across one edit
    *    session, never <code>null</code> or empty.
    */
   public static String getDefaultAuthenticationName()
   {
      return getResources().getString("dir.auth.defaultname") + 
         ms_authenticationNameSuffix++;
   }
   
   /**
    * Get the schema display string.
    * 
    * @param scheme the scheme for which to get the display  string, not 
    *    <code>null</code>.
    * @return the dispaly string for the supplied scheme, never 
    *    <code>null</code>.
    */
   public static String getSchemaDisplayString(String scheme)
   {
      return getResources().getString("dir.auth.schema." + scheme);
   }
   
   /**
    * Opens the authentication editor to create a new authentication. Depending 
    * on the dialog return value the authentication table will be updated 
    * for the newly added authentication.
    */
   protected void onAdd()
   {
      AuthenticationEditorDialog editor = new AuthenticationEditorDialog(
         m_parent, m_data);
      editor.show();
      
      if (editor.isOk())
      {
         PSAuthentication authentication = editor.getAuthentication();
         authentication.setEncryptPwd(false);
         m_data.addAuthentication(authentication);
         
         m_model.addRow(getRowData(authentication));
         setModified(true);
      }
   }
   
   /**
    * Get the row data for the supplied authentication.
    * 
    * @param authentication the authentication for which to get the row data,
    *    assumed not <code>null</code>.
    * @return the row data for the supplied authentication as an array of
    *    <code>Object</code>, never <code>null</code>.
    */
   private Object[] getRowData(PSAuthentication authentication)
   {
      Object[] rowData =
      {
         authentication.getName(),
         getSchemaDisplayString(authentication.getScheme()),
         authentication.getUser()
      };
      
      return rowData;
   }
   
   /**
    * Opens the authentication editor for the currently selected authentication.
    * Depending on the dialog return value, the authentication table will be
    * updated for the edited authentication.
    */
   protected void onEdit()
   {
      int row = m_table.getSelectedRow();
      String name = (String) m_model.getValueAt(row, 0);
      PSAuthentication authentication = m_data.getAuthentication(name);
      String orgPwd = authentication.getPassword();
      AuthenticationEditorDialog editor = new AuthenticationEditorDialog(
         m_parent, m_data, authentication);
      editor.show();
      
      if (editor.isOk())
      {
         // remove current authentication
         m_data.removeAuthentication(authentication);
         
         authentication = editor.getAuthentication();
         String currPwd = authentication.getPassword();
         //If for some reason password load failed and thus pwd is set to empty, we should leave the original password
         if(StringUtils.isEmpty(currPwd)){
            authentication.setPassword(orgPwd);
            // If password is changed by User to a new string, then that will be a plain text and thus need to encrypt it,
            // so we change the flag PasswordEncrypted to false, so that server can encrypt it.
         }else if(!authentication.getPassword().equals(orgPwd)){
            authentication.setPasswordEncrypted(false);
         }
         
         // add new authentication
         m_data.addAuthentication(authentication);
         
         m_model.removeRow(row);
         m_model.insertRow(row, getRowData(authentication));
         setModified(true);
      }
   }
   
   /**
    * Pops up a dialog to ask the user for a confirmation to delete the 
    * currently selected authentication. Depending on the dialog return value,
    * the authentication table will be updated for the deleted authentication.
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
         getResources().getString("dir.auth.delete.message"), args);
         
      int choice = JOptionPane.showConfirmDialog(null, message, 
         getResources().getString("dir.auth.delete.title"), 
            JOptionPane.YES_NO_OPTION);
      if (choice == JOptionPane.YES_OPTION)
      {
         m_table.clearSelection();
         for (int i=rows.length-1; i>=0; i--)
         {
            m_data.removeAuthentication(
               (String) m_model.getValueAt(rows[i], 0));
            m_model.removeRow(rows[i]);
         }
         
         setModified(true);
      }
   }
   
   /**
    * Initializes the panel with all UI components.
    */
   protected void initPanel()
   {
      setLayout(new BorderLayout());
      
      String[] columnNames = 
      {
         getResources().getString("dir.auth.name"),
         getResources().getString("dir.auth.schema"),
         getResources().getString("dir.auth.user")
      };
      
      add(createTablePanel(columnNames, 0), BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(createCommandPanel(), BorderLayout.EAST);
      add(bottomPanel, BorderLayout.SOUTH);
      
      super.initPanel();
   }
   
   /**
    * See {@link BasePanel#initData()} for description.
    */
   protected void initData()
   {
      super.initData();
      
      Iterator authentications = m_data.getAuthentications().iterator();
      while (authentications.hasNext())
         addRow((PSAuthentication) authentications.next());
   }
   
   /**
    * Add a new table row for the supplied authentication.
    * 
    * @param authentication the authentication for which to add a new row,
    *    assumed not <code>null</code>.
    */
   private void addRow(PSAuthentication authentication)
   {
      Object[] rowData =
      {
         authentication.getName(),
         getSchemaDisplayString(authentication.getScheme()),
         authentication.getUser()
      };
            
      m_model.addRow(rowData);
   }
   
   /**
    * The default authentication name suffix. Will be incremented in each
    * call to {@link getDefaultAuthenticationName()}.
    */
   private static int ms_authenticationNameSuffix = 0;
}

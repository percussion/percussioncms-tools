/******************************************************************************
 *
 * [ AppSecDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.EditableListBox.EditableListBox;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataEncryptor;
import com.percussion.design.objectstore.PSDatabaseComponentCollection;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSRole;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

/** A dialog that handles the settings of application security.  This dialog
 * consists of 2 tabs: User Authroization and Roles
 *
 */
@SuppressWarnings(value={"unchecked"})
public class AppSecDialog
   extends PSEditorDialog
   implements ListSelectionListener
{
   /** A basic constructor that creates a tabbed pane of 3 tabs and 3 buttons.
    *
    */

   public AppSecDialog()
   {
   }

   public AppSecDialog(PSApplication app)
      throws
         PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSLockedException
   {
      super();

      m_data = app;
      PSObjectStore os = E2Designer.getApp().getMainFrame().getObjectStore();
      m_serverConfig = os.getServerConfiguration();

      m_roleConfig = os.getRoleConfiguration(false, false, false);

      m_encryptorPanel = new DataEncryptorPanel(m_data.getDataEncryptor());

      m_tabPane = new JTabbedPane();
      // Note: If the order of tabs is changed, update the array in subclassHelpId
      m_tabPane.addTab(
         getResources().getString("authorization"),
         new UATab(this));
      setMnemonicForTabIndex(m_tabPane, getResources(), "authorization", 0);
      
      m_tabPane.addTab(getResources().getString("encrypt"), m_encryptorPanel);
      setMnemonicForTabIndex(m_tabPane, getResources(), "encrypt", 1);

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(m_tabPane, "Center");
      panel.add(createCommandPanel(), "South");

      getRootPane().setDefaultButton(m_closeButton);

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);
      setResizable(true);
      this.setSize(DIALOG_SIZE);
      center();
   }

   /** Overriding PSDialog.onOk() method implementation.
    */
   @Override
   public void onOk()
   {
      // updating groups for ACL panel
      if (m_comboMultiGroup
         .getSelectedItem()
         .toString()
         .equals(getResources().getString("max")))
         m_acl.setAccessForMultiMembershipMaximum();
      else if (
         m_comboMultiGroup.getSelectedItem().toString().equals(
            getResources().getString("mergemax")))
         m_acl.setAccessForMultiMembershipMergedMaximum();
      else if (
         m_comboMultiGroup.getSelectedItem().toString().equals(
            getResources().getString("mergemin")))
         m_acl.setAccessForMultiMembershipMergedMinimum();
      else if (
         m_comboMultiGroup.getSelectedItem().toString().equals(
            getResources().getString("min")))
         m_acl.setAccessForMultiMembershipMinimum();

      updateEntry(m_ugList.getList().getSelectedRow());

      // if the "overridden" checkbox is checked, the encryptor is saved; else
      // the encryptor is removed from the appication.
      PSDataEncryptor encryptor = m_encryptorPanel.getEncryptor();
      if (m_encryptorPanel.isDefaultOverridden())
         m_data.setDataEncryptor(encryptor);
      else
         m_data.setDataEncryptor(null);

      if (null == encryptor)
      {
         m_tabPane.setSelectedIndex(3);
      }
      else
      {
         save();
         if (!validateUserACLs())
         {
            int userSelection =
               JOptionPane.showConfirmDialog(
                  this,
                  getResources().getString("errorreadmodify"),
                  E2Designer.getResources().getString("ApplicationErr"),
                  JOptionPane.YES_NO_OPTION);

            // brings user to the ACL tab once error occurs
            if (0 != m_tabPane.getSelectedIndex())
               m_tabPane.setSelectedIndex(0);

            if (userSelection == JOptionPane.NO_OPTION)
               return;
         }

         dispose();
      }
   }

   /**
    * Overrides parent class, PSDialog, onCancel() method. Which is used by the
    * windowClosing() method as well. This way, all events are redirected to the
    * action of the Close button.
    */
   @Override
   public void onCancel()
   {
      onOk();
   }

   /**
    * Validates that the current user's or the default user's ACL settings
    * "READ, UPDATE and MODIFY_ACL" are checked.
    *
    * @returns <code>true</code> if the "READ,, UPDATE and MODIFY_ACL" ACL
    *    settings are checked for the current or default user,
    *    <code>false</code> otherwise.
    */
   private boolean validateUserACLs()
   {
      try
      {
         PSObjectStore os = E2Designer.getApp().getMainFrame().getObjectStore();
         String currentUser = os.getUserConfiguration().getUserName();

         // check the current user
         PSAclEntry defaultEntry = null;
         for (int i = 0; i < m_entries.size(); i++)
         {
            PSAclEntry entry = (PSAclEntry) m_entries.get(i);
            if (hasReadUpdateAndModifyAclAccess(entry, currentUser))
               return true;

            if (entry
               .getName()
               .equalsIgnoreCase(getResources().getString("default")))
               defaultEntry = entry;
         }
         // check the default user
         return hasReadUpdateAndModifyAclAccess(defaultEntry, null);
      }
      catch (PSServerException e)
      {
         JOptionPane.showMessageDialog(
            this,
            e.getLocalizedMessage(),
            E2Designer.getResources().getString("ServerErr"),
            JOptionPane.ERROR_MESSAGE);
      }
      catch (PSAuthorizationException e)
      {
         JOptionPane.showMessageDialog(
            this,
            e.getLocalizedMessage(),
            E2Designer.getResources().getString("AuthErr"),
            JOptionPane.ERROR_MESSAGE);
      }
      catch (PSAuthenticationFailedException e)
      {
         PSDlgUtil.showError(
            e,
            false,
            E2Designer.getResources().getString("ExceptionTitle"));
      }

      return false;
   }

   /**
    * Checks is the provided ACL entry has "READ, UPDATE and MODIFY_ACL"
    * checked for the user provided.
    *
    * @param entry the ACL entry to check, may be <code>null</code>.
    * @param user the user to check the suplied entry for, <code>null</code> to
    *    check for evry entry.
    * @return <code>true</code> if READ and UPDATE and MODIFY_ACL settings
    *    are checked, <code>false</code> otherwise.
    */
   private boolean hasReadUpdateAndModifyAclAccess(
      PSAclEntry entry,
      String user)
   {
      if (entry == null)
         return false;

      if (user != null)
      {
         if (entry.isUser())
         {
            if (!entry.getName().equalsIgnoreCase(user))
               return false;
         }
         else if (entry.isRole())
         {
            Iterator members = PSIteratorUtils.emptyIterator();
            if (!isMember(members, user))
               return false;
         }
         else if (entry.isGroup())
         {
            Iterator members = PSIteratorUtils.emptyIterator();
            if (!isMember(members, user))
               return false;
         }
      }

      int accessLevel = entry.getAccessLevel();
      if ((PSAclEntry.AACE_DESIGN_READ
         != (PSAclEntry.AACE_DESIGN_READ & accessLevel))
         || (PSAclEntry.AACE_DESIGN_UPDATE
            != (PSAclEntry.AACE_DESIGN_UPDATE & accessLevel))
         || (PSAclEntry.AACE_DESIGN_MODIFY_ACL
            != (PSAclEntry.AACE_DESIGN_MODIFY_ACL & accessLevel)))
      {
         return false;
      }

      return true;
   }

   /**
    * Test if the supplied user is a member in the provided members list. The
    * test is made case insensitive.
    *
    * @param members the list of members to check against, assumed not
    *    <code>null</code>, assumed to be a list of String objects.
    * @param user the user to check the membership for, assumed not
    *    <code>null</code>.
    * @return <code>true</code> if the provided user is a member in the
    *    provided list, <code>false</code> otherwise.
    */
   private boolean isMember(Iterator members, String user)
   {
      while (members.hasNext())
      {
         String member = (String) members.next();
         if (member.equalsIgnoreCase(user))
            return true;
      }

      return false;
   }

   /** Used to determine which tab is currently visible for Help to display the
    * correct topic
    *
    * @returns int The current visible tab pane index.
    */
   public int getVisibleTabIndex()
   {
      return m_tabPane.getSelectedIndex();
   }

   /** Checks to see the application configuration setting for allowing login pass
    * thru.
    */
   public boolean getLoginPassThru()
   {
      return m_data.isBeLoginPassthruEnabled();
   }

   protected PSApplication getApp()
   {
      return m_data;
   }


   /**
    * Appends a string that corresponds to the active tab type.
    **/
   @Override
   protected String subclassHelpId(String helpId)
   {
      // NOTE: The order of strings in this array must match the actual tab order
      String[] tabType = { "ACL", "BECredentials", "Encryption" };
      return helpId + "_" + tabType[getVisibleTabIndex()];
   }

   /**
    * User Authorization tab panel
    */
   class UATab extends JPanel
   {
      UATab(AppSecDialog parent)
      {
         super();
         m_parent = parent;
         JPanel top = new JPanel();
         JPanel bottom = new JPanel();
         m_acl = m_data.getAcl();
         try
         {
            m_entries = m_acl.getEntries();
            if (m_entries == null)
               m_entries =
                  new PSCollection("com.percussion.design.objectstore.PSAclEntry");

         }
         catch (ClassNotFoundException e)
         {
            e.printStackTrace();
         }

         //setup bottom panel
         bottom.setLayout(new GridLayout(2, 1));
         
         String labelStr = m_parent.getResources().getString("MultiGroup");
         char mn         = 
                  m_parent.getResources().getString("MultiGroup.mn").charAt(0);
         m_lMultiGroup =   new JLabel(labelStr);
         m_lMultiGroup.setDisplayedMnemonic(mn);
         m_lMultiGroup.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
         
         
         m_comboMultiGroup = new PSComboBox();
         bottom.add(m_lMultiGroup);
         bottom.add(m_comboMultiGroup);
         m_lMultiGroup.setLabelFor(m_comboMultiGroup);
         
         //setup top panel
         top.setLayout(new BorderLayout());

         JPanel box = new JPanel();
         box.setLayout(new GridLayout(14, 1));

         m_cbAllowDataAccess =
            new JCheckBox(m_parent.getResources().getString("DataAccess"));
         m_cbAllowDataAccess.setMnemonic(
               m_parent.getResources().getString("DataAccess.mn").charAt(0));
         m_cbDataQuery =
            new JCheckBox(m_parent.getResources().getString("DataQuery"));
         m_cbDataUpdate =
            new JCheckBox(m_parent.getResources().getString("DataUpdate"));
         m_cbDataCreate =
            new JCheckBox(m_parent.getResources().getString("DataCreate"));
         m_cbDataDelete =
            new JCheckBox(m_parent.getResources().getString("DataDelete"));
         
         
         m_cbAllowDesignAccess =
            new JCheckBox(m_parent.getResources().getString("DesignAccess"));
         m_cbAllowDesignAccess.setMnemonic(
               m_parent.getResources().getString("DesignAccess.mn").charAt(0));
         m_cbDesignRead =
            new JCheckBox(m_parent.getResources().getString("DesignRead"));
         m_cbDesignUpdate =
            new JCheckBox(m_parent.getResources().getString("DesignUpdate"));
         m_cbDesignDelete =
            new JCheckBox(m_parent.getResources().getString("DesignDelete"));
         m_cbDesignModify =
            new JCheckBox(m_parent.getResources().getString("DesignModify"));
         
         labelStr = m_parent.getResources().getString("SecurityProvider");
         mn = m_parent.getResources().getString("SecurityProvider.mn").charAt(0); 

         JPanel allow = new JPanel(new BorderLayout());
         allow.setBorder(new EmptyBorder(0, 0, 0, 0));
         allow.add(m_cbAllowDataAccess, "West");
         box.add(allow);

         JPanel DataQuery = new JPanel(new BorderLayout());
         DataQuery.setBorder(new EmptyBorder(0, 10, 0, 0));
         DataQuery.add(m_cbDataQuery, "Center");
         box.add(DataQuery);

         JPanel DataUpdate = new JPanel(new BorderLayout());
         DataUpdate.setBorder(new EmptyBorder(0, 10, 0, 0));
         DataUpdate.add(m_cbDataUpdate, "Center");
         box.add(DataUpdate);

         JPanel DataCreate = new JPanel(new BorderLayout());
         DataCreate.setBorder(new EmptyBorder(0, 10, 0, 0));
         DataCreate.add(m_cbDataCreate, "Center");
         box.add(DataCreate);

         JPanel DataDelete = new JPanel(new BorderLayout());
         DataDelete.setBorder(new EmptyBorder(0, 10, 0, 0));
         DataDelete.add(m_cbDataDelete, "Center");
         box.add(DataDelete);

         JPanel fake = new JPanel(new BorderLayout());
         fake.setBorder(new EmptyBorder(5, 0, 0, 0));
         box.add(fake);

         JPanel designAccess = new JPanel();
         designAccess.setLayout(new BorderLayout());
         designAccess.setBorder(new EmptyBorder(0, 0, 0, 0));
         designAccess.add(m_cbAllowDesignAccess, "West");
         box.add(designAccess);

         JPanel DesignRead = new JPanel(new BorderLayout());
         DesignRead.setBorder(new EmptyBorder(0, 10, 0, 0));
         DesignRead.add(m_cbDesignRead, "Center");
         box.add(DesignRead);

         JPanel DesignUpdate = new JPanel(new BorderLayout());
         DesignUpdate.setBorder(new EmptyBorder(0, 10, 0, 0));
         DesignUpdate.add(m_cbDesignUpdate, "Center");
         box.add(DesignUpdate);

         JPanel DesignDelete = new JPanel(new BorderLayout());
         DesignDelete.add(m_cbDesignDelete, "Center");
         DesignDelete.setBorder(new EmptyBorder(0, 10, 0, 0));
         box.add(DesignDelete);

         JPanel DesignModify = new JPanel(new BorderLayout());
         DesignModify.add(m_cbDesignModify, "Center");
         DesignModify.setBorder(new EmptyBorder(0, 10, 0, 0));
         box.add(DesignModify);

         //add listners
         m_cbAllowDataAccess.addActionListener(new ActionListener()
         {
            public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
            {
               setControls();
            }
         });

         m_cbAllowDesignAccess.addActionListener(new ActionListener()
         {
            public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
            {
               setControls();
            }
         });

         //read from data
         m_comboMultiGroup.addItem(
            m_parent.getResources().getString("mergemax"));
         m_comboMultiGroup.addItem(
            m_parent.getResources().getString("mergemin"));
         m_comboMultiGroup.addItem(m_parent.getResources().getString("max"));
         m_comboMultiGroup.addItem(m_parent.getResources().getString("min"));
         if (m_acl.isAccessForMultiMembershipMaximum())
            m_comboMultiGroup.setSelectedItem(
               m_parent.getResources().getString("max"));
         else if (m_acl.isAccessForMultiMembershipMergedMaximum())
            m_comboMultiGroup.setSelectedItem(
               m_parent.getResources().getString("mergemax"));
         else if (m_acl.isAccessForMultiMembershipMergedMinimum())
            m_comboMultiGroup.setSelectedItem(
               m_parent.getResources().getString("mergemin"));
         else if (m_acl.isAccessForMultiMembershipMinimum())
            m_comboMultiGroup.setSelectedItem(
               m_parent.getResources().getString("min"));

         m_dialog =
            new RoleMemberPropertyDialog(
               AppSecDialog.this,
               null,
               m_serverConfig);
         m_ugList =
            new EditableListBox(
               m_parent.getResources().getString("usersandgroups"),
               null,
               null,
               null,
               EditableListBox.BROWSEBOXEDIT,
               EditableListBox.BROWSEBUTTON);

         for (int iEntry = 0; iEntry < m_entries.size(); ++iEntry)
         {
            PSAclEntry entry = (PSAclEntry) m_entries.get(iEntry);
            m_ugList.addRowValue(new RoleMemberData(entry));
         }

         ButtonListener bl = new ButtonListener();

         m_buttonAdd = m_ugList.getLeftButton();
         m_buttonAdd.setActionCommand("unused");
         m_buttonAdd.addActionListener(bl);

         m_buttonDelete = m_ugList.getRightButton();
         m_buttonDelete.addActionListener(bl);

         m_buttonBrowse = m_ugList.getCellBrowseButton();
         m_buttonBrowse.setActionCommand("unused");
         m_buttonBrowse.addActionListener(bl);
         m_ugList.addListSelectionListener(m_parent);

         String unspecifiedInstance =
            SecurityProviderMetaData.NAME_FOR_ANY_PROVIDER;

         String[] types =
            SecurityProviderMetaData
               .getInstance()
               .getSecurityProvidersByDisplayName(
               false);

         m_vproviderInstances.add(unspecifiedInstance);
         for (int i = 0; i < types.length; ++i)
            m_vproviderInstances.add(
               unspecifiedInstance + "(" + types[i] + ")");

         PSCollection securityProviders =
            m_serverConfig.getSecurityProviderInstances();
         for (int i = 0; i < securityProviders.size(); i++)
         {
            PSSecurityProviderInstance inst =
               (PSSecurityProviderInstance) securityProviders.get(i);
            m_vproviderInstances.add(
               inst.getName()
                  + "("
                  + SecurityProviderMetaData.getInstance().getDisplayNameForId(
                     inst.getType())
                  + ")");
         }

         JPanel box2 = new JPanel();
         box2.setLayout(new BorderLayout());
         box2.add(m_ugList, "Center");
         box2.setBorder(new EmptyBorder(5, 5, 5, 10));
         box.setBorder(new EmptyBorder(5, 10, 5, 0));
         top.add(box2, "Center");
         top.add(box, "East");

         setLayout(new BorderLayout());

         setBorder(new EmptyBorder(5, 5, 5, 5));

         setControls();
         add(top, "Center");
         add(bottom, "South");
         pack();
      }

      /**
       * Inner class to implement ActionListener interface for handling button events.
       */
      class ButtonListener implements ActionListener
      {
         public void actionPerformed(ActionEvent e)
         {
            JButton button = (JButton) e.getSource();
            if (button == m_buttonAdd)
               onAdd();
            else if (button == m_buttonDelete)
               onDelete();
            else if (button == m_buttonBrowse)
               onEdit();
         }
      }

      private AppSecDialog m_parent = null;
   }

   private void setControls()
   {
      m_cbDataQuery.setEnabled(m_cbAllowDataAccess.isSelected());
      m_cbDataUpdate.setEnabled(m_cbAllowDataAccess.isSelected());
      m_cbDataCreate.setEnabled(m_cbAllowDataAccess.isSelected());
      m_cbDataDelete.setEnabled(m_cbAllowDataAccess.isSelected());
      m_cbDesignRead.setEnabled(m_cbAllowDesignAccess.isSelected());
      m_cbDesignUpdate.setEnabled(m_cbAllowDesignAccess.isSelected());
      m_cbDesignDelete.setEnabled(m_cbAllowDesignAccess.isSelected());
      m_cbDesignModify.setEnabled(m_cbAllowDesignAccess.isSelected());

      //undo selections if they unchecked the main check boxes
      if (!m_cbAllowDataAccess.isSelected())
      {
         m_cbDataQuery.setSelected(false);
         m_cbDataUpdate.setSelected(false);
         m_cbDataCreate.setSelected(false);
         m_cbDataDelete.setSelected(false);
      }

      if (!m_cbAllowDesignAccess.isSelected())
      {
         m_cbDesignRead.setSelected(false);
         m_cbDesignUpdate.setSelected(false);
         m_cbDesignDelete.setSelected(false);
         m_cbDesignModify.setSelected(false);
      }
   }

   /** A ListSelectionListener added to the existing listener in EditableListBox for
    * additional features to update the JLabels on the right side of the JPanel.
    */

   public void valueChanged(ListSelectionEvent e)
   {
      //       System.out.println("Selected" + new Integer(getSelectionIndex()).toString());
      if (e.getValueIsAdjusting())
         return;

      updateEntry(getSelectionIndex());
   }

   public int getSelectionIndex()
   {
      return m_ugList.getSelectionModel().getMinSelectionIndex();
   }

   /**
    * Saves all application security settings from this dialog.
    */
   private void save()
   {
      try
      {
         if (m_iCurrent == -1 || !(m_iCurrent < m_entries.size()))
            return;

         PSAclEntry curentry = (PSAclEntry) m_entries.get(m_iCurrent);
         if (m_ugList.getRowValue(m_iCurrent) != null)
            curentry.setName(m_ugList.getRowValue(m_iCurrent).toString());

         curentry.setAccessLevel(getCurrentAccessLevelSettings());

         // replacing the acl entry took at the beginning of the method with 
         // the new entry.
         m_entries.set(m_iCurrent, curentry);
      }
      catch (IllegalArgumentException ex)
      {
         System.out.println(ex.getLocalizedMessage());
      }
   }

   private int getCurrentAccessLevelSettings()
   {
      int iAccess = PSAclEntry.AACE_NO_ACCESS;
      if (m_cbDataQuery.isSelected())
         iAccess |= PSAclEntry.AACE_DATA_QUERY;
      if (m_cbDataUpdate.isSelected())
         iAccess |= PSAclEntry.AACE_DATA_UPDATE;
      if (m_cbDataCreate.isSelected())
         iAccess |= PSAclEntry.AACE_DATA_CREATE;
      if (m_cbDataDelete.isSelected())
         iAccess |= PSAclEntry.AACE_DATA_DELETE;
      if (m_cbDesignRead.isSelected())
         iAccess |= PSAclEntry.AACE_DESIGN_READ;
      if (m_cbDesignUpdate.isSelected())
         iAccess |= PSAclEntry.AACE_DESIGN_UPDATE;
      if (m_cbDesignDelete.isSelected())
         iAccess |= PSAclEntry.AACE_DESIGN_DELETE;
      if (m_cbDesignModify.isSelected())
         iAccess |= PSAclEntry.AACE_DESIGN_MODIFY_ACL;

      return iAccess;
   }

   private void loadCurrent()
   {
      if (m_iCurrent == -1 || !(m_iCurrent < m_entries.size()))
         return;

      PSAclEntry entry = (PSAclEntry) m_entries.get(m_iCurrent);

      //      System.out.println("Loading Current entry" + new Integer(m_iCurrent).toString());

      //now load new entry
      m_cbAllowDataAccess.setSelected(false);
      m_cbAllowDesignAccess.setSelected(false);
      m_cbDataQuery.setSelected(false);
      m_cbDataUpdate.setSelected(false);
      m_cbDataCreate.setSelected(false);
      m_cbDataDelete.setSelected(false);
      m_cbDesignRead.setSelected(false);
      m_cbDesignUpdate.setSelected(false);
      m_cbDesignDelete.setSelected(false);
      m_cbDesignModify.setSelected(false);

      int iAccess = entry.getAccessLevel();
      if ((iAccess & PSAclEntry.AACE_DATA_QUERY) == PSAclEntry.AACE_DATA_QUERY)
      {
         m_cbDataQuery.setSelected(true);
         m_cbAllowDataAccess.setSelected(true);
      }

      if ((iAccess & PSAclEntry.AACE_DATA_UPDATE)
         == PSAclEntry.AACE_DATA_UPDATE)
      {
         m_cbDataUpdate.setSelected(true);
         m_cbAllowDataAccess.setSelected(true);
      }

      if ((iAccess & PSAclEntry.AACE_DATA_CREATE)
         == PSAclEntry.AACE_DATA_CREATE)
      {
         m_cbDataCreate.setSelected(true);
         m_cbAllowDataAccess.setSelected(true);
      }

      if ((iAccess & PSAclEntry.AACE_DATA_DELETE)
         == PSAclEntry.AACE_DATA_DELETE)
      {
         m_cbDataDelete.setSelected(true);
         m_cbAllowDataAccess.setSelected(true);
      }

      if ((iAccess & PSAclEntry.AACE_DESIGN_READ)
         == PSAclEntry.AACE_DESIGN_READ)
      {
         m_cbDesignRead.setSelected(true);
         m_cbAllowDesignAccess.setSelected(true);
      }

      if ((iAccess & PSAclEntry.AACE_DESIGN_UPDATE)
         == PSAclEntry.AACE_DESIGN_UPDATE)
      {
         m_cbDesignUpdate.setSelected(true);
         m_cbAllowDesignAccess.setSelected(true);
      }

      if ((iAccess & PSAclEntry.AACE_DESIGN_DELETE)
         == PSAclEntry.AACE_DESIGN_DELETE)
      {
         m_cbDesignDelete.setSelected(true);
         m_cbAllowDesignAccess.setSelected(true);
      }

      if ((iAccess & PSAclEntry.AACE_DESIGN_MODIFY_ACL)
         == PSAclEntry.AACE_DESIGN_MODIFY_ACL)
      {
         m_cbDesignModify.setSelected(true);
         m_cbAllowDesignAccess.setSelected(true);
      }
      setControls();
   }

   private void updateEntry(int index)
   {
      try
      {
         //if the index is out of range this is a new one
         if (index >= m_entries.size())
         {
            if (m_ugList.getRowValue(index) != null)
            {
               PSAclEntry newentry =
                  new PSAclEntry(
                     m_ugList.getRowValue(index).toString(),
                     PSAclEntry.AACE_NO_ACCESS);
               m_entries.add(newentry);
            }
         }

         // if the entry has been edited
         if (index != -1
            && m_entries != null
            && index < m_entries.size()
            && index != m_iCurrent)
         {
            //            System.out.println("After Got");
            //save current entry
            if (m_iCurrent != -1 && m_iCurrent < m_entries.size())
            {
               //               System.out.println("Saving Current entry" + new Integer(m_iCurrent).toString());

               save();
            }

            m_iCurrent = index;
            loadCurrent();
         }
      }
      catch (IllegalArgumentException ex)
      {
         System.out.println(ex.getLocalizedMessage());
      }
   }

   /**
    * Handler for delete ACL button clicked. Also removes the ACL from the Collection of
    * application ACLS.  It warns user if the ACL entry to be deleted is the
    * user&apos;s own.
    */
   public void onDelete()
   {
      if (m_iCurrent >= m_entries.size())
         return;

      PSAclEntry entry = (PSAclEntry) m_entries.get(m_iCurrent);

      if (getUserName().equals(entry.getName()))
      {
         // if they don't want to delete themselves, exit now
         if (JOptionPane.YES_OPTION
            != JOptionPane.showConfirmDialog(
               this,
               getResources().getString("warningremoveself"),
               E2Designer.getResources().getString("ConfirmOperation"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE))
         {
            return;
         }
      }

      m_entries.remove(entry);
      m_ugList.deleteRows();

      // this is a fix for bug id GBOD-4BBVQS. When we delete, we need to
      // refresh the current entry to keep its access info rather than
      // inherit the info from the last guy deleted.
      int size = m_entries.size();
      if (size > 0)
      {
         if (m_iCurrent >= size)
            m_iCurrent = size - 1; // in case we zapped off the end

         loadCurrent();
         m_ugList.getList().setRowSelectionInterval(m_iCurrent, m_iCurrent);
      }
   }

   /**
    * Current user name.
    */
   private String getUserName()
   {
      return E2Designer.getDesignerConnection().getUserName();
   }

   /**
    * Handler for add ACL button clicked.
    */
   private void onAdd()
   {
      try
      {
         m_dialog = new RoleMemberPropertyDialog(this, null, m_serverConfig);
         m_dialog.setTitle(getResources().getString("newacltitle"));
         m_dialog.includeRoles(getRoles());
         m_dialog.setVisible(true);
         if (m_dialog.isModified())
         {
            int iAccess = getCurrentAccessLevelSettings();
            RoleMemberData data = m_dialog.getData();
            m_ugList.getCellEditor().cancelCellEditing();
            Vector v = null;
            if (!(data.getMemberType())
               .equals(RoleMemberPropertyDialog.FILTER))
            {
               v = m_dialog.getMemberNames();
               for (int i = 0; i < v.size(); i++)
               {
                  RoleMemberData memData = new RoleMemberData();
                  memData.copyFrom(data);

                  memData.setName((String) v.get(i));

                  OSAclEntry entry = new OSAclEntry();
                  entry.copyFrom(memData);
                  entry.setAccessLevel(iAccess);

                  m_entries.add(entry);
                  m_ugList.addRowValue(new RoleMemberData(entry));
               }
            }
         }
      }
      catch (IllegalArgumentException ex)
      {
         System.out.println(ex.getLocalizedMessage());
      }
   }

   /**
    * Handler for editor browse button clicked for the ACL List.
    */
   private void onEdit()
   {
      try
      {
         if (m_iCurrent == -1 || m_iCurrent >= m_entries.size())
            return;

         m_ugList.getCellEditor().stopCellEditing();
         save();
         // get currently selected Role and pass in for editing
         OSAclEntry data =
            new OSAclEntry((PSAclEntry) m_entries.get(m_iCurrent));
         if (data == null)
            return;

         RoleMemberData memdata = new RoleMemberData();
         memdata.setName(data.getName());
         if (data.isUser())
         {
            memdata.setMemberType(memdata.getResources().getString("user"));
         }
         else if (data.isGroup())
         {
            memdata.setMemberType(memdata.getResources().getString("group"));
         }
         else
         {
            memdata.setMemberType(memdata.getResources().getString("role"));
         }

         m_dialog = new RoleMemberPropertyDialog(this, memdata, m_serverConfig);
         m_dialog.setTitle(getResources().getString("editacltitle"));
         m_dialog.includeRoles(getRoles());
         m_dialog.setVisible(true);
         if (m_dialog.isModified())
         {
            // in this case we allow only one member to be edited
            memdata = m_dialog.getData();
            data.copyFrom(memdata);
            m_ugList.setRowValue(memdata, m_iCurrent);
            m_entries.set(m_iCurrent, data);
            loadCurrent();
         }
      }
      catch (IllegalArgumentException ex)
      {
         System.out.println(ex.getLocalizedMessage());
      }
   }

   private Vector getRoles()
   {
      Vector roles = new Vector();

      PSDatabaseComponentCollection serverRoles = m_roleConfig.getRoles();
      for (int iRole = 0; iRole < serverRoles.size(); ++iRole)
      {
         Object role = serverRoles.get(iRole);
         if (role instanceof PSRole)
         {
            roles.add(((PSRole) role).getName());
         }
      }

      return (roles);
   }

   public boolean onEdit(@SuppressWarnings("unused") UIFigure figure,
         final Object data)
   {
      if (data instanceof PSApplication)
      {
         try
         {
            AppSecDialog appSecDialog = new AppSecDialog((PSApplication) data);
            appSecDialog.setVisible(true);
            return (true);
         }
         catch (Exception e)
         {
            PSDlgUtil.showError(e, true,
                  E2Designer.getResources().getString("ServerConnErr"));
         }
      }
      return (false);
   }

   /** Creates the panel containing the command buttons, Close and Help.
    */
   private JPanel createCommandPanel()
   {
      m_closeButton = new UTFixedButton(getResources().getString("close"));
      m_closeButton.setMnemonic(getResources().getString("close.mn").charAt(0));
      m_closeButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            AppSecDialog.this.onOk();
         }
      });

      m_helpButton = new UTFixedButton(getResources().getString("help"));
      m_helpButton.setMnemonic(getResources().getString("help.mn").charAt(0));
      m_helpButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            AppSecDialog.this.onHelp();
         }
      });

      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.setBorder(new EmptyBorder(4, 0, 0, 4));
      panel.add(m_closeButton);
      panel.add(Box.createHorizontalStrut(6));
      panel.add(m_helpButton);
      panel.add(Box.createHorizontalGlue());

      JPanel jp = new JPanel(new BorderLayout());
      jp.add(panel, BorderLayout.EAST);
      return jp;
   }

   //private JButton     m_okButton, m_cancelButton, m_helpButton;
   private JTabbedPane m_tabPane;

   private DataEncryptorPanel m_encryptorPanel = null;

   private PSApplication m_data = null;
   private PSServerConfiguration m_serverConfig = null;
   private PSRoleConfiguration m_roleConfig = null;

   private UTFixedButton m_closeButton, m_helpButton;
   private EditableListBox m_ugList = null;
   private JCheckBox m_cbAllowDataAccess = null;
   private JCheckBox m_cbAllowDesignAccess = null;
   private JCheckBox m_cbDataQuery = null;
   private JCheckBox m_cbDataUpdate = null;
   private JCheckBox m_cbDataCreate = null;
   private JCheckBox m_cbDataDelete = null;
   private JCheckBox m_cbDesignRead = null;
   private JCheckBox m_cbDesignUpdate = null;
   private JCheckBox m_cbDesignDelete = null;
   private JCheckBox m_cbDesignModify = null;
   private PSComboBox m_comboMultiGroup = null;
   private JLabel m_lMultiGroup = null;
   private JButton m_buttonBrowse, m_buttonDelete, m_buttonAdd;
   private PSAcl m_acl = null;
   private PSCollection m_entries = null;
   private int m_iCurrent = -1;
   private final static Dimension DIALOG_SIZE = new Dimension(410, 480);
   private RoleMemberPropertyDialog m_dialog = null;
   private Vector m_vproviderInstances = new Vector();
}

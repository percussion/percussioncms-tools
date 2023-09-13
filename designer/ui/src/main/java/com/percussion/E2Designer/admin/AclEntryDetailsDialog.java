/*[ AclEntryDetailsDialog.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.UTFixedComboBox;
import com.percussion.E2Designer.Util;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

public class AclEntryDetailsDialog extends JDialog
{
/**
 * Constructor for AclEntryDetails dialog. Can be constructed either editing a PSAclEntry
 * or creating a new entry.
 *
 *   @param   parent The parent frame. Can be <code>null</code>.
 *   @param   bEditing <code>true</code> if editing an entry.
 *   @param   entry the entry for which the properties are to be set or edited.
 *   @param   config the ServerConfiguration object to get the security provider instances from.
 *
 */
   public AclEntryDetailsDialog(Frame parent, boolean bEditing, PSAclEntry entry,
      ServerConfiguration config)
   {
      super(parent, true);
      m_bEditing = bEditing;
      m_entry = entry;
      initDialog();
   }

   /**
    *   Initializes the dialog. Sets the size and title, creates the controls,
    * initializes the listeners and centers the dialog.
    */
   private void initDialog()
   {
      if(m_bEditing)
         this.setTitle(sm_res.getString("titleEditAcl"));
      else
         this.setTitle(sm_res.getString("titleNewAcl"));
      this.center();

      createControls();
      initControls();
      initListeners();
      this.pack();
   }

   /**
    * Creates the entry controls.
    *
    * @return the created panel
    */
   private JPanel createEntryPanel()
   {
      JPanel p1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      JLabel labelEntryName = new JLabel(sm_res.getString("entryName"));
      labelEntryName.setDisplayedMnemonic(sm_res.getString("entryName.mn").charAt(0));
      p1.add(labelEntryName);
      m_comboBoxEntryName = new UTFixedComboBox();
      labelEntryName.setLabelFor(m_comboBoxEntryName);
      
      m_comboBoxEntryName.setEditable(true);
      m_comboBoxEntryName.addItem(sm_res.getString("entryNameDefault"));
      m_comboBoxEntryName.addItem(sm_res.getString("entryNameAnonymous"));
      p1.add(m_comboBoxEntryName);

      JPanel p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      JLabel labelEntryType = new JLabel(sm_res.getString("entryType"));
      labelEntryType.setDisplayedMnemonic(sm_res.getString("entryType.mn").charAt(0));
      p2.add(labelEntryType);
      m_comboBoxEntryType = new UTFixedComboBox();
      labelEntryType.setLabelFor(m_comboBoxEntryType);
      m_comboBoxEntryType.addItem(sm_res.getString("entryTypeGroup"));
      m_comboBoxEntryType.addItem(sm_res.getString("entryTypeRole"));
      m_comboBoxEntryType.addItem(sm_res.getString("entryTypeUser"));
      m_panelEntryProperties.add(m_comboBoxEntryType);
      p2.add(m_comboBoxEntryType);

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new TitledBorder(sm_res.getString("entry")));
      panel.add(p1, "North");
      panel.add(p2, "South");

      JPanel retPanel = new JPanel(new BorderLayout());
      retPanel.add(panel, BorderLayout.WEST);
      return retPanel;
   }

   /**
    * Creates the check box controls.
    *
    * @return JPanel the created panel
    */
   private JPanel createCheckBoxPanel()
   {
      Box box = new Box(SwingConstants.VERTICAL);
      m_cbAllowDesignAccess = new JCheckBox(
         sm_res.getString("allowDesignAccess"));
      box.add(m_cbAllowDesignAccess);

      m_cbAllowAdminAccess = new JCheckBox(sm_res.getString("allowAdminAccess"));
      box.add(m_cbAllowAdminAccess);

      m_cbAllowDataAccess = new JCheckBox(sm_res.getString("allowDataAccess"));
      box.add(m_cbAllowDataAccess);

      m_cbCanCreateApps = new JCheckBox(sm_res.getString("canCreateApps"));
      box.add(m_cbCanCreateApps);

      m_cbCanDeleteApps = new JCheckBox(sm_res.getString("canDeleteApps"));
      box.add(m_cbCanDeleteApps);

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new TitledBorder(sm_res.getString("access")));
      panel.add(box, "Center");
      return panel;
   }

   /**
    * Creates the command controls.
    *
    * @return JPanel the created panel
    */
   private JPanel createCommandPanel()
   {
      m_buttonOK = new UTFixedButton(sm_res.getString("ok"));
      m_buttonOK.setMnemonic(sm_res.getString("ok").charAt(0));
      m_buttonCancel = new UTFixedButton(sm_res.getString("cancel"));
      m_buttonCancel.setMnemonic(sm_res.getString("cancel").charAt(0));
      
      JPanel panel = new JPanel(new FlowLayout());
      panel.add(m_buttonOK);
      panel.add(m_buttonCancel);
      
      JPanel retPanel = new JPanel(new BorderLayout());
      retPanel.add(panel, BorderLayout.EAST);
      return panel;
   }

   /**
    * Creates the controls in the dialog.
    *
    */
   private void createControls()
   {
      m_panelCommand = createCommandPanel();

      m_panelEntryProperties = new JPanel();
      m_panelEntryProperties.setLayout(new BoxLayout(m_panelEntryProperties, 
         BoxLayout.Y_AXIS));
      m_panelEntryProperties.add(createEntryPanel());
      
      m_panelEntryProperties.add(createCheckBoxPanel());

      getContentPane().setLayout(new BorderLayout());
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(m_panelEntryProperties, "Center");
      JPanel bottomPanel  = new JPanel(new BorderLayout());
      bottomPanel.add(m_panelCommand, BorderLayout.EAST);
      panel.add(bottomPanel, "South");
      getContentPane().add(panel);
   }

   /**
    * Initializes the controls in the dialog with the member values of the
    * PSAclEntry object
    * that was passed in for editing.
    */
   private void initControls()
   {
      if(m_bEditing)
      {
         m_comboBoxEntryName.setSelectedItem(m_entry.getName());

         if(m_entry.isGroup())
            m_comboBoxEntryType.setSelectedItem(
               sm_res.getString("entryTypeGroup"));
         else if(m_entry.isRole())
            m_comboBoxEntryType.setSelectedItem(
               sm_res.getString("entryTypeRole"));
         else if(m_entry.isUser())
            m_comboBoxEntryType.setSelectedItem(
               sm_res.getString("entryTypeUser"));

         int iAccess = m_entry.getAccessLevel();

         if((iAccess & PSAclEntry.SACE_ACCESS_DESIGN)
            == PSAclEntry.SACE_ACCESS_DESIGN)
         {
            m_cbAllowDesignAccess.setSelected(true);
         }

         if((iAccess & PSAclEntry.SACE_ACCESS_DATA)
            == PSAclEntry.SACE_ACCESS_DATA)
         {
            m_cbAllowDataAccess.setSelected(true);
         }

         if((iAccess & PSAclEntry.SACE_CREATE_APPLICATIONS)
            == PSAclEntry.SACE_CREATE_APPLICATIONS)
         {
            m_cbCanCreateApps.setSelected(true);
         }

         if((iAccess & PSAclEntry.SACE_DELETE_APPLICATIONS)
            == PSAclEntry.SACE_DELETE_APPLICATIONS)
         {
            m_cbCanDeleteApps.setSelected(true);
         }

         if((iAccess & PSAclEntry.SACE_ADMINISTER_SERVER)
            == PSAclEntry.SACE_ADMINISTER_SERVER)
         {
            m_cbAllowAdminAccess.setSelected(true);
         }
      }
      else
      {
         m_comboBoxEntryName.setSelectedItem( "" );
         m_comboBoxEntryType.setSelectedItem( sm_res.getString("entryTypeUser"));
         m_cbAllowDesignAccess.setSelected(true);
         m_cbAllowAdminAccess.setSelected(true);
         m_cbAllowDataAccess.setSelected(true);
         m_cbCanCreateApps.setSelected(true);
         m_cbCanDeleteApps.setSelected(true);
      }
   }

   /**
    * Initializes the listeners for the OK and Cancel buttons.
    *
    */
   private void initListeners()
   {
      ButtonListener buttonListener = new ButtonListener( );
      m_buttonOK.addActionListener(buttonListener);
      m_buttonCancel.addActionListener(buttonListener);

      this.addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
         {
            onCancelButtonClicked();
         }
      });


   }

   /**
    * Handler for OK button clicked. Saves data from the controls to the
    * PSAclEntry object passed in to the Dialog constructor.
    */
   private void onOKButtonClicked()
   {
      int iAccess = 0;

      if(m_cbAllowDesignAccess.isSelected())
         iAccess |= PSAclEntry.SACE_ACCESS_DESIGN;
      if(m_cbAllowDataAccess.isSelected())
         iAccess |= PSAclEntry.SACE_ACCESS_DATA;
      if(m_cbCanCreateApps.isSelected())
         iAccess |= PSAclEntry.SACE_CREATE_APPLICATIONS;
      if(m_cbCanDeleteApps.isSelected())
         iAccess |= PSAclEntry.SACE_DELETE_APPLICATIONS;
      if(m_cbAllowAdminAccess.isSelected())
         iAccess |= PSAclEntry.SACE_ADMINISTER_SERVER;

      try
      {
         m_entry.setName((String) m_comboBoxEntryName.getSelectedItem());
         m_entry.setAccessLevel(iAccess);
      }
      catch(IllegalArgumentException e)
      {
         if(E2Designer.getApp() != null)
            PSDlgUtil.showError(e);
         else
            JOptionPane.showMessageDialog(this,
               Util.cropErrorMessage(e.getLocalizedMessage()),
               sm_res.getString("error"), JOptionPane.ERROR_MESSAGE);
         return;
      }

      String strType = ((String)m_comboBoxEntryType.getSelectedItem());

      if(strType.equals(sm_res.getString("entryTypeGroup")))
         m_entry.setGroup();
      else if(strType.equals(sm_res.getString("entryTypeRole")))
         m_entry.setRole();
      else if(strType.equals(sm_res.getString("entryTypeUser")))
         m_entry.setUser();

      setVisible(false);
   }

   /**
    * Handler for Cancel button clicked. In case of new Entry being added,
    * sets the PSAclEntry object passed in to the Dialog constructor to
    * <code>null</code>.
    */
   private void onCancelButtonClicked()
   {
      if(!m_bEditing)
         m_entry = null;
      dispose();
   }

   /**
    * Centers the dialog on the screen.
    */
   public void center()
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation(( screenSize.width - size.width ) / 2,
         ( screenSize.height - size.height ) / 2 );
   }

   /**
    * Returns the modified PSAclEntry object for outside use. Will be
    * <code>null</code> in case the user clicked Cancel and we were creating the
    * properties of a new PSAclEntry.
    * In case we were editing an existing PSAclEntry object, and the user
    * clicked Cancel, the returned object is the same as that was passed in.
    */
   public PSAclEntry getEntry()
   {
      return m_entry;
   }

   /**
    * Inner class to implement ActionListener interface for handling button
    * events.
    */
   class ButtonListener implements ActionListener
   {
      public void actionPerformed( ActionEvent e )
      {
         JButton button = (JButton)e.getSource( );
         if(button == m_buttonOK)
            onOKButtonClicked( );
         else if(button == m_buttonCancel)
            onCancelButtonClicked( );
      }
   }

   //{{DECLARE_CONTROLS
   JPanel m_panelEntryProperties;
   JPanel m_panelCommand;
   JCheckBox m_cbAllowDesignAccess;
   JCheckBox m_cbAllowAdminAccess;
   JCheckBox m_cbAllowDataAccess;
   JCheckBox m_cbCanCreateApps;
   JCheckBox m_cbCanDeleteApps;
   UTFixedComboBox m_comboBoxEntryName;
   UTFixedComboBox m_comboBoxEntryType;
   UTFixedButton m_buttonOK;
   UTFixedButton m_buttonCancel;
   //}}

   private static ResourceBundle sm_res = PSServerAdminApplet.getResources();
   private boolean m_bEditing = false;
   private PSAclEntry m_entry = null;


   /**
    * The main program for testing.
    */
   public static void main( String [] astrArgs )
   {
      try
      {
         String strLnFClass = UIManager.getCrossPlatformLookAndFeelClassName();
         LookAndFeel lnf = (LookAndFeel) Class.forName(
            strLnFClass).newInstance();
         UIManager.setLookAndFeel( lnf );

         PSAclEntry entry= new PSAclEntry("test", PSAclEntry.ACE_TYPE_GROUP);
         AclEntryDetailsDialog acl = new AclEntryDetailsDialog(
            null, false, entry, null);
         acl.setVisible(true);
      }
      catch (Exception e)
      {
         System.out.println(e);
      }
   }
}

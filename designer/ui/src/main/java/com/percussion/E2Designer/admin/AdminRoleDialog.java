/******************************************************************************
 *
 * [ AdminRoleDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.UTPropertiesPanel;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.E2Designer.Util;
import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.E2Designer.ValidationException;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSDatabaseComponentCollection;
import com.percussion.design.objectstore.PSRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Dialog for adding or editing a role.
 **/
public class AdminRoleDialog extends PSDialog implements ItemListener
{
   /**
    * Constructor for creating new/edit role dialog.
    *
    * @param frame The parent frame of this dialog. May be <code>null</code>.
    * @param role The role object.
    * To add a new role set this to <code>null</code>.
    * @param the collection of roles, may be <code>null</code>. Used for
    * validation of role name to avoid duplicate name when adding a new role.
    **/
   public AdminRoleDialog(JFrame frame, PSRole role,
      PSDatabaseComponentCollection roleColl)
   {
      super(frame);

      if(m_bInitialized = initDialog())
      {
         m_role = role;
         m_roleColl = roleColl;

         try
         {
            if(role == null)
               setTitle(ms_dlgResource.getString("newTitle"));
            else
            {
               String[] rolename = new String[1];
               rolename[0] = role.getName();
               setTitle(MessageFormat.format(
                  ms_dlgResource.getString("editTitle"), rolename));

               m_roleNameField.setText(role.getName());
               m_roleNameField.setEnabled(false);
               PSAttributeList attribs = role.getAttributes();
               if(attribs == null)
                  attribs = new PSAttributeList();
               m_propertiesPanel.setTableDataFromList(attribs);
            }

            Map propertyNameValues =
               SecurityRolePanel.getRolePropertyNameValues();
            List valueList =
               SecurityRolePanel.catalogAdminProperty(propertyNameValues,
               m_roleNameField.getText(),SecurityRolePanel.ROLE_LOOKUP_TYPE);
            m_propertiesPanel.setPropertiesAndEditors(valueList, this);
         }
         catch(MissingResourceException e)
         {
            String property = e.getLocalizedMessage().substring(
               e.getLocalizedMessage().lastIndexOf(' ')+1);
            JOptionPane.showMessageDialog(null,
               "Could not find value for '" + property + "' in \n" +
               getResourceName(), "Error", JOptionPane.ERROR_MESSAGE);
            m_bInitialized = false;
         }
      }
   }

   /**
    * Creates dialog framework.
    *
    * @return <code>true</code> to indicate initialization is successful,
    * Otherwise <code>false</code>.
    **/
   private boolean initDialog()
   {
      ms_dlgResource = getResources();

      if(ms_dlgResource == null)
      {
         JOptionPane.showMessageDialog(null,
            "Could not find " + getResourceName(),
            "Error", JOptionPane.ERROR_MESSAGE);
         return false;
      }

      JPanel panel = new JPanel();
      getContentPane().add(panel);

      m_propertiesPanel = new UTPropertiesPanel();
      if(!m_propertiesPanel.isInitialized())
         return false;

      try {
         m_propertiesPanel.setBorder(createGroupBorder(
            ms_dlgResource.getString("propertiesBorder")));

         JPanel command_panel = createRoleCommandPanel();
         
         JPanel bottomPanel = new JPanel(new BorderLayout());
         bottomPanel.add(createCommandPanel(), BorderLayout.EAST);
         
         panel.setLayout(new BorderLayout(0,5));
         panel.setBorder((new EmptyBorder (5,10,5,10)));
         panel.add(command_panel, BorderLayout.NORTH);
         panel.add(m_propertiesPanel, BorderLayout.CENTER);       
         panel.add(bottomPanel, BorderLayout.SOUTH);

      }
      catch(MissingResourceException e)
      {
         String property = e.getLocalizedMessage().substring(
            e.getLocalizedMessage().lastIndexOf(' ')+1);
         JOptionPane.showMessageDialog(null,
            "Could not find value for '" + property + "' in \n" +
            getResourceName(), "Error", JOptionPane.ERROR_MESSAGE);
         return false;
      }

      pack();
      setResizable(true);
      center();
      return true;
   }

   /**
    * Creates Role Details and command panel.
    *
    * @return Panel with controls, never <code>null</code>.
    **/
   private JPanel createRoleCommandPanel()
   {
      m_roleNameField = new JTextField(20);
      m_roleNameField.setMinimumSize(new Dimension(100, 25));
      m_roleNameField.setPreferredSize(new Dimension(100, 25));
      m_roleNameField.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
      m_roleNameField.setAlignmentX(LEFT_ALIGNMENT);
      m_roleNameField.setAlignmentY(CENTER_ALIGNMENT);

      JLabel label1 = new JLabel(ms_dlgResource.getString("Name"),
         SwingConstants.RIGHT);
      label1.setAlignmentX(RIGHT_ALIGNMENT);
      label1.setAlignmentY(CENTER_ALIGNMENT);

      JPanel role_info_panel = new JPanel();
      role_info_panel.setLayout(
         new BoxLayout(role_info_panel, BoxLayout.X_AXIS));
      role_info_panel.setAlignmentX(LEFT_ALIGNMENT);
      role_info_panel.add(Box.createHorizontalStrut(10));
      role_info_panel.add(label1);
      role_info_panel.add(Box.createHorizontalStrut(5));
      role_info_panel.add(m_roleNameField);

      JPanel panel = new JPanel(new BorderLayout(30, 0));
      panel.add(role_info_panel, BorderLayout.CENTER);

      return  panel;
   }

   /**
    *   *   Creates the command panel with OK, Cancel and Help buttons.
    *
    * @return The command panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      UTStandardCommandPanel commandPanel =
         new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
        public void onOk()
        {
            AdminRoleDialog.this.onOk();
        }

      };

      getRootPane().setDefaultButton(commandPanel.getOkButton());

      return commandPanel;
   }

   /**
    * Re-catalogs attributes/properties when re-catalog item is selected in
    * editor drop-list.
    *
    * @see ItemListener#itemStateChanged  itemStateChanged
    **/
   public void itemStateChanged(ItemEvent event)
   {
      if(event.getStateChange() == ItemEvent.SELECTED)
      {
         String item = (String)event.getItem();
         JComboBox editor = (JComboBox)event.getSource();

         if(item.equals(m_propertiesPanel.ms_recatalogString))
         {
            Map propertyNameValues =
               SecurityRolePanel.catalogRolePropertyNameValues(true);
            List valueList =
               SecurityRolePanel.catalogAdminProperty(propertyNameValues,
               m_roleNameField.getText(),SecurityRolePanel.ROLE_LOOKUP_TYPE);
            m_propertiesPanel.resetCatalogedProperties(valueList, this);
         }
      }
   }

   /**
    * @see <code>PSDialog</code>
    */
   protected String subclassHelpId(String helpId)
   {
      if(m_role != null)
         helpId += "_edit";
      return helpId;
   }

   /**
    * Action method for OK button.
    * Validates role name and saves data in role.
    * Modified flag is set to <code>true</code>.
    **/
   public void onOk()
   {
      stopTableEditor(m_propertiesPanel.getTable());
      
      if(m_role == null)
      {
         String roleName = m_roleNameField.getText().trim();

         String errorMsg = "";
         if(roleName.length() == 0)
         {
            errorMsg = ms_dlgResource.getString("emptyName");
         }
         else if(roleName.length() > PSRole.MAX_ROLE_NAME_LEN)
         {
            errorMsg = ms_dlgResource.getString("longName");
         }
         else if(roleName.indexOf('&') != -1)
         {
            errorMsg = ms_dlgResource.getString("illegalCharacterInName");
         }
         else
         {
            //Validate for duplicate names
            if(m_roleColl != null)
            {
               Iterator iter = m_roleColl.iterator();
               while(iter.hasNext())
               {
                  String name = ((PSRole)iter.next()).getName();
                  if(name.equalsIgnoreCase(roleName))
                  {
                     errorMsg = ms_dlgResource.getString("duplicateName");
                     break;
                  }
               }
            }
         }

         if(errorMsg.length() > 0)
         {
            JOptionPane.showMessageDialog(null, Util.cropErrorMessage(errorMsg),
               ms_dlgResource.getString("error"), JOptionPane.ERROR_MESSAGE);
            return;
         }

         m_role = new PSRole(roleName);
      }

      //Update role with attributes
      PSAttributeList roleAttribs = m_role.getAttributes();
      try
      {
         PSAttributeList attribs = m_propertiesPanel.getTableDataAsList();
         m_components = new Component[]{m_propertiesPanel.getTable()};
         m_validationConstraints = new ValidationConstraint[] {
            new RolePropertiesConstraint()
         };
         setValidationFramework( m_components, m_validationConstraints);
         if (!activateValidation())
            return;
         SecurityRolePanel.addRemoveAttributes(roleAttribs, attribs);
      }
      catch(ValidationException e)
      {
         JOptionPane.showMessageDialog(null,
            Util.cropErrorMessage(e.getMessage()),
            ms_dlgResource.getString("error"), JOptionPane.ERROR_MESSAGE);
         return;
      }

      m_bModified = true;
      setVisible(false);
      dispose();
   }

   /**
    * Accessor function to check whether role is modified or not.
    *
    * @return The modified flag.
    **/
   public boolean isModified()
   {
      return m_bModified;
   }

   /**
    * Gets the role.
    *
    * @return Role, may be <code>null</code> if it is called before
    * <code>onOk()</code> is invoked in case of adding a new role.
    **/
   public PSRole getRole()
   {
      return m_role;
   }



   /**
    * The PropertiesPanel containing the table with 2 columns
    * and a text area. This panel is useful for editing properties.
    * Text Area is used for editing value of a property.
    * Initialized in <code>initDialog()</code>.
    **/
   private UTPropertiesPanel m_propertiesPanel;

   /** The role object, gets initialized in constructor. **/
   private PSRole m_role;

   /**
    * The collection of existing roles, gets initialized in constructor.
    * When new role is getting created, this collection is used for validation
    * of role name for not to have duplicate role name.
   **/
   private PSDatabaseComponentCollection m_roleColl;

   /**
    * The flag to indicate whether role is modified or not, initialized to
    * <code>false</code> and set to <code>true</code> when OK button is clicked.
    **/
   private boolean m_bModified = false;

   /** Text field for entering role name, gets initialized in
     * <code>createRoleCommandPanel()</code>, disabled if the dialog is
     * invoked for editing role.
     **/
   private JTextField  m_roleNameField;

   /** Dialog resource strings, initialized in constructor. **/
   private static ResourceBundle ms_dlgResource = null;

   private Component[] m_components  = null;
   private ValidationConstraint[]  m_validationConstraints = null;
}

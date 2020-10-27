/******************************************************************************
 *
 * [ AdminMemberDialog.java ]
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
import com.percussion.E2Designer.ValidationException;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSDatabaseComponentCollection;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSRelativeSubject;
import com.percussion.design.objectstore.PSRole;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.PSSubject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Dialog for adding new member or for editing member's global or role
 * specific properties.
 **/
public class AdminMemberDialog extends PSDialog
                               implements ItemListener
{
   /**
    * Constructor for Edit Member Properties Dialog. It sets dialog in edit
    * mode.
    *
    * @param frame The parent frame of this dialog. May be <code>null</code>.
    * @param roleMember The role member object which needs to be edited.
    * May not be <code>null</code>.
    * @param config Server configuration. May not be <code>null</code>.
    * @param roleSubjects Map of role name and role subjects of the member.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if server configuration is
    * <code>null</code> or role member is <code>null</code> or
    * role subjects of the member is <code>null</code>.
    **/
   public AdminMemberDialog(JFrame frame, UIRoleMember roleMember,
      ServerConfiguration config, Map roleSubjects)
   {
      super(frame);

      if(roleMember == null)
         throw new IllegalArgumentException(
            "Member to be edited can not be null");

      if(config == null)
         throw new IllegalArgumentException(
            "Server Configuration may not be null");

      if(roleSubjects == null || roleSubjects.isEmpty())
         throw new IllegalArgumentException(
            "Role Subjects may not be null. " +
            "Member should have at least one role subject");

      if( m_bInitialized = initDialog(true) )
      {
         try {
            String[] membername = new String[1];
            membername[0] = roleMember.getMemberName();
            setTitle(MessageFormat.format(
               ms_dlgResource.getString("editTitle"), membername));

            m_memberNameField.setText(roleMember.getMemberName());
            m_bEditing = true;

            if(roleMember.getType().equals(ms_res.getString("user_type")))
               m_userRadio.setSelected(true);
            else
               m_groupRadio.setSelected(true);
         }
         catch(MissingResourceException e)
         {
            String property = e.getLocalizedMessage().substring(
               e.getLocalizedMessage().lastIndexOf(' ')+1);
            JOptionPane.showMessageDialog(null,
               "Could not find value for '" + property + "' in \n" +
               PSServerAdminApplet.getResourceName(),
               "Error", JOptionPane.ERROR_MESSAGE);
            m_bInitialized = false;
         }

         if(roleMember.getMemberAttributes() == null)
            m_globalAttributes = new PSAttributeList();
         else
            m_globalAttributes = roleMember.getMemberAttributes();

         m_roleSpecAttributes = new HashMap();
         Iterator roleSubjectIter = roleSubjects.entrySet().iterator();
         while(roleSubjectIter.hasNext())
         {
            Map.Entry entry = (Map.Entry)roleSubjectIter.next();

            String roleName = (String)entry.getKey();
            PSAttributeList roleMemberAttrs =
               ((PSRelativeSubject)entry.getValue()).getAttributes();

            if(roleMemberAttrs != null)
               m_roleSpecAttributes.put(roleName, roleMemberAttrs);
            else
               m_roleSpecAttributes.put(roleName, new PSAttributeList());
            m_roleCombo.addItem(roleName);
         }

         initSettings();
      }
   }

   /**
    * Constructor for New Member Dialog. It sets dialog in new mode.
    *
    * @param frame The parent frame of this dialog. May be <code>null</code>.
    * @param config Server configuration. May not be <code>null</code>.
    * @param roleConfig a role configuration, never  <code>null</code>
    * @param role The role object to which member is getting added.
    * May not be <code>null</code>.
    * @param roleMembers a list of the role members. Never  <code>null</code>
    * may be empty.
    *
    * @throws IllegalArgumentException if server configuration is
    * <code>null</code> or role of the member is <code>null</code>.
    **/
   public AdminMemberDialog(JFrame frame, ServerConfiguration config,
      PSRoleConfiguration roleConfig, PSRole role, List roleMembers)
   {
      super(frame);

      if(config == null)
         throw new IllegalArgumentException(
            "Server Configuration may not be null");

      if(role == null)
         throw new IllegalArgumentException("Member must have a role.");

      if(  m_bInitialized = initDialog(false) )
      {
         try 
         {
            setTitle(ms_dlgResource.getString("newTitle"));
            m_bEditing = false;
            m_roleConfig = roleConfig;
         }
         catch(MissingResourceException e)
         {
            String property = e.getLocalizedMessage().substring(
               e.getLocalizedMessage().lastIndexOf(' ')+1);
            JOptionPane.showMessageDialog(null,
               "Could not find value for '" + property + "' in \n" +
               PSServerAdminApplet.getResourceName(),
               "Error", JOptionPane.ERROR_MESSAGE);
            m_bInitialized = false;
         }

         m_roleMembers = roleMembers;
         m_globalAttributes = new PSAttributeList();
         m_roleSpecAttributes = new HashMap();
         m_roleSpecAttributes.put(role.getName(), new PSAttributeList());

         m_roleCombo.addItem(role.getName());
         initSettings();
      }
   }

   /**
    *  Sets initial settings.
    **/
   private void initSettings()
   {
      m_attrTypeCombo.addItemListener(this);
      m_roleCombo.addItemListener(this);
      m_roleCombo.setEnabled(false);
      m_propertiesPanel.setTableDataFromList(m_globalAttributes);
      Map propertyNameValues =
         SecurityRolePanel.getSubjectPropertyNameValues();
      List valueList =
         SecurityRolePanel.catalogAdminProperty(propertyNameValues,
            m_memberNameField.getText(),SecurityRolePanel.SUBJECT_LOOKUP_TYPE);
      m_propertiesPanel.setPropertiesAndEditors(valueList,this);
   }

   /**
    * Checks to see wheter or not a new member that is about to be added to
    * the role already exists in the role.
    * @return <code>true</code> if match found, otherwise <code>false</code>
    */
   private boolean isExistingMember()
   {
      boolean isMatch = false;
      for (int i = 0; i < m_roleMembers.size(); i++)
      {
         UIRoleMember member = (UIRoleMember )m_roleMembers.get(i);
         isMatch = foundMatch(member);
         if(isMatch)
            break;
      }
      return isMatch;
   }

   /**
    * Checks to see if the data that user provided for a creation of a new
    * member match the data of the member passed in.
    * @param member an existing member to check against, assumed not to be
    * <code>null</code>
    * @return <code>true</code> if data match, otherwise <code>false</code>
    */
   private boolean foundMatch(UIRoleMember member)
   {
      boolean isMatch = false;
      String newUserType = new String ();
      String memberName = member.getMemberName();
      String userType = member.getType();

      if(m_userRadio.isSelected())
         newUserType = ms_res.getString("user_type");
      else
         newUserType = ms_res.getString("group_type");

      if (memberName.equals(m_memberNameField.getText()) &&
         userType.equals(newUserType))
            isMatch = true;

      return isMatch;
   }


   /**
    * Checks for the same global subject as the subject that is about to be
    * created. If match is found that subject will be returned.
    *
    * @return a matching subject, <code>null</code> if match not found.
    */
   private PSSubject isExistingGlobalSubject()
   {
      PSSubject matchingSubject = null;
      //do this only if adding a new subject
      if(!m_bEditing)
      {
         //get all the global subject from this role configuration
         Iterator iter = m_roleConfig.getSubjects();
         while(iter.hasNext())
         {
            PSGlobalSubject subject = (PSGlobalSubject)iter.next();
            UIRoleMember tempMember = new UIRoleMember(
               subject.makeRelativeSubject(), m_roleConfig);
            if(foundMatch(tempMember))
            {
               matchingSubject  = subject;
               break;
            }
         }
      }
      return matchingSubject;
   }

   /**
    * Creates dialog framework.
    *
    * @param edit The boolean flag to indicate mode of member.
    * If <code>true</code>, member is in edit mode and sets the control states
    * accordingly.
    *
    * @return <code>true</code> to indicate initialization is successful,
    * Otherwise <code>false</code>.
    **/
   private boolean initDialog(boolean edit)
   {
      if( (ms_dlgResource = getResources()) == null)
      {
         JOptionPane.showMessageDialog(null,
            "Could not find " + getResourceName(),
            "Error", JOptionPane.ERROR_MESSAGE);
         return false;
      }

      if((ms_res = PSServerAdminApplet.getResources()) == null)
         return false;

      JPanel panel = new JPanel();
      getContentPane().add(panel);

      try {
         JPanel member_info_command_panel = createMemberCommandPanel(edit);
         member_info_command_panel.setBorder(createGroupBorder(
                 ms_dlgResource.getString("memberBorder")));
         
         m_propertiesPanel = new UTPropertiesPanel();
         if(!m_propertiesPanel.isInitialized())
            return false;

         JPanel attributes_panel = new JPanel();
         attributes_panel.setLayout(
            new BoxLayout(attributes_panel, BoxLayout.Y_AXIS));
         attributes_panel.setBorder(createGroupBorder(
            ms_dlgResource.getString("propertiesBorder")));
         attributes_panel.add(Box.createVerticalStrut(10));
         attributes_panel.add(createAttributeSettingsPanel());
         attributes_panel.add(Box.createVerticalStrut(20));
         attributes_panel.add(m_propertiesPanel);

         JPanel bottomPanel = new JPanel(new BorderLayout());
         bottomPanel.add(createCommandPanel(), BorderLayout.EAST);
         
         panel.setLayout(new BorderLayout(0,5));
         panel.setBorder((new EmptyBorder (5,10,5,10)));
         panel.add(member_info_command_panel, BorderLayout.NORTH);
         panel.add(attributes_panel, BorderLayout.CENTER);
         panel.add(bottomPanel, BorderLayout.SOUTH);
      }
      catch(MissingResourceException e)
      {
         String property = e.getLocalizedMessage().substring(
            e.getLocalizedMessage().lastIndexOf(' ')+1);
         JOptionPane.showMessageDialog(null,
            "Could not find value for '" + property + "' in \n" +
            getResourceName(),
            "Error", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      pack();
      setResizable(true);
      center();

      return true;
   }

   /**
    * Creates member details and command panel.
    *
    * @param edit The boolean flag to indicate mode of member.
    * If <code>true</code> member is in edit mode and sets the control states
    * accordingly.
    *
    * @return Panel with controls, never <code>null</code>.
    **/
   private JPanel createMemberCommandPanel(boolean edit)
   {
      m_memberNameField = new JTextField(20);
      m_memberNameField.setMinimumSize(new Dimension(150, 25));
      m_memberNameField.setPreferredSize(new Dimension(150, 25));
      m_memberNameField.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
      m_memberNameField.setAlignmentX(LEFT_ALIGNMENT);
      m_memberNameField.addFocusListener(new FocusLostGainedListener());
      m_memberNameField.addKeyListener(new KeyTextListener());

      JLabel label1 = new JLabel(ms_dlgResource.getString("memberName"),
         SwingConstants.RIGHT);
      label1.setDisplayedMnemonic(
              ms_dlgResource.getString("memberName.mn").charAt(0));
      label1.setLabelFor(m_memberNameField);
      label1.setMinimumSize(new Dimension(100, 25));
      label1.setPreferredSize(new Dimension(100, 25));
      label1.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
      label1.setAlignmentX(RIGHT_ALIGNMENT);

      Box label_box = Box.createVerticalBox();
      label_box.add(label1);

      Box value_box = Box.createVerticalBox();
      value_box.add(m_memberNameField);

      RadioListener radioListener = new RadioListener();
      m_userRadio = new JRadioButton(ms_dlgResource.getString("userRadio"));
      m_userRadio.setSelected(true);
      m_userRadio.addActionListener(radioListener);
      m_groupRadio = new JRadioButton(ms_dlgResource.getString("groupRadio"));
      m_groupRadio.addActionListener(radioListener);

      ButtonGroup memberTypeGroup = new ButtonGroup();
      memberTypeGroup.add(m_userRadio);
      memberTypeGroup.add(m_groupRadio);

      JPanel radio_box = new JPanel();
      radio_box.setLayout(new BoxLayout(radio_box,BoxLayout.Y_AXIS));
      radio_box.setBorder(createGroupBorder(
         ms_dlgResource.getString("typeBorderTitle")));
      radio_box.add(m_userRadio);
      radio_box.add(m_groupRadio);

      JPanel member_info_panel = new JPanel(new BorderLayout());
      JPanel member_name_panel = new JPanel(new FlowLayout());
      member_name_panel.add(label_box);
      member_name_panel.add(value_box);
      member_info_panel.add(member_name_panel, BorderLayout.WEST);
      member_info_panel.add(radio_box, BorderLayout.EAST);

      JPanel member_info_command_panel = new JPanel();
      member_info_command_panel.add(member_info_panel, BorderLayout.WEST);

      if (edit)
      {
         m_memberNameField.setEnabled(false);
         m_userRadio.setEnabled(false);
         m_groupRadio.setEnabled(false);
      }
      
      JPanel retPanel = new JPanel(new BorderLayout());
      retPanel.add(member_info_command_panel, BorderLayout.WEST);
      return retPanel;
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
            AdminMemberDialog.this.onOk();
        }

      };

      getRootPane().setDefaultButton(commandPanel.getOkButton());

      return commandPanel;
   }

   /**
    *   *   Creates the panel for editing global and role specific attributes
    * of member.
    *
    * @return The panel with controls, never <code>null</code>.
    */
   private JPanel createAttributeSettingsPanel()
   {
      m_attrTypeCombo = new JComboBox();
      m_attrTypeCombo.addItem( ms_dlgResource.getString("attributeTypeGlobal"));
      m_attrTypeCombo.addItem( ms_dlgResource.getString("attributeTypeRole"));
      m_roleCombo = new JComboBox();

      JPanel attribute_settings_panel = new JPanel();
      attribute_settings_panel.setLayout(
         new BoxLayout(attribute_settings_panel, BoxLayout.X_AXIS));
      attribute_settings_panel.setAlignmentX(LEFT_ALIGNMENT);
      JLabel viewCmbLabel = new JLabel(ms_dlgResource.getString("viewCombo"),
              SwingConstants.RIGHT);
      viewCmbLabel.setDisplayedMnemonic(
              ms_dlgResource.getString("viewCombo.mn").charAt(0));
      viewCmbLabel.setLabelFor(m_attrTypeCombo);
      attribute_settings_panel.add(viewCmbLabel);
      attribute_settings_panel.add(Box.createHorizontalStrut(5));
      attribute_settings_panel.add(m_attrTypeCombo);
      attribute_settings_panel.add(Box.createRigidArea(new Dimension(30,20)));
      JLabel roleLabel = new JLabel(
              ms_dlgResource.getString("roleCombo"), SwingConstants.RIGHT);
      roleLabel.setDisplayedMnemonic(
              ms_dlgResource.getString("roleCombo.mn").charAt(0));
      roleLabel.setLabelFor(m_roleCombo);
      attribute_settings_panel.add(roleLabel);
      attribute_settings_panel.add(Box.createHorizontalStrut(5));
      attribute_settings_panel.add(m_roleCombo);

      return attribute_settings_panel;
   }

   /**
    * @see <code>PSDialog</code>
    */
   protected String subclassHelpId(String helpId)
   {
      if(m_bEditing)
         helpId += "_edit";
      return helpId;
   }

   /**
    * Action method for OK button.
    * Saves data in role member data.
    * Modified flag is set to <code>true</code>.
    **/
   public void onOk()
   {
      stopTableEditor(m_propertiesPanel.getTable());
      
      m_data = new AdminRoleMemberData();
      if(!m_bEditing)
      {
         if(m_memberNameField.getText().length() == 0)
         {
            JOptionPane.showMessageDialog(null,
               Util.cropErrorMessage(
               ms_dlgResource.getString("mustEnterMember")),
               ms_dlgResource.getString("error"), JOptionPane.ERROR_MESSAGE);
            return;
         }
         //if a newly added member already exist pop-up a message box
         if (isExistingMember())
         {
            JOptionPane.showMessageDialog(null,
               Util.cropErrorMessage(
               ms_dlgResource.getString("memberExist")),
               ms_dlgResource.getString("error"), JOptionPane.ERROR_MESSAGE);
            return;
         }
         PSSubject subject = isExistingGlobalSubject ();
         if(subject != null)
         {

            if(globalSubjectAttributesChanged(subject))
            {
               int option = JOptionPane.showConfirmDialog(null,
                  Util.cropErrorMessage(
                  ms_dlgResource.getString("globalAttributesChanged")),
                  ms_dlgResource.getString("globalAttr"),
                  JOptionPane.YES_NO_OPTION);
               if(option == JOptionPane.NO_OPTION)
                  resetPropertiesPanel();
            }
         }
      }
      m_data.setName(m_memberNameField.getText());

      if(m_userRadio.isSelected())
         m_data.setMemberType(ms_res.getString("user_type"));
      else if(m_groupRadio.isSelected())
         m_data.setMemberType(ms_res.getString("group_type"));

      //Update attributes in table
      try {
         if(m_roleCombo.isEnabled())
         {
            m_roleSpecAttributes.put(m_roleCombo.getSelectedItem(),
               m_propertiesPanel.getTableDataAsList());
         }
         else
            m_globalAttributes = m_propertiesPanel.getTableDataAsList();
      }
      catch(ValidationException e)
      {
         JOptionPane.showMessageDialog(null,
            Util.cropErrorMessage(e.getMessage()),
            ms_dlgResource.getString("error"), JOptionPane.ERROR_MESSAGE);
         return;
      }

      m_data.setGlobalAttributes(m_globalAttributes);
      m_data.setRoleSpecAttributes(m_roleSpecAttributes);

      m_bModified = true;
      setVisible(false);
      dispose();
   }

   /**
    * Checks for changes made to global subject attributes
    * @param subject, a global subject to check attributes for,
    * assumed not to be <code>null</code>
    * @return <code>true<code> if the global attributes have been changed,
    * otherwise <code>false</code>
    */
   private boolean globalSubjectAttributesChanged(PSSubject subject)
   {
      boolean hasChanged = false;
      try
      {
         PSDatabaseComponentCollection oldAttributes = subject.getAttributes();
         PSDatabaseComponentCollection newAttributes = null;
         if(((String)m_attrTypeCombo.getSelectedItem()).equals("Global"))
            newAttributes = m_propertiesPanel.getTableDataAsList();
         else
            newAttributes = m_globalAttributes;

         if(oldAttributes.size() != newAttributes.size())
            hasChanged = true;
         else
         {
            for (int i = 0; i < oldAttributes.size(); i++)
            {
               PSAttribute oldAttrib =(PSAttribute) oldAttributes.get(i);
               PSAttribute newAttr = null;
               for(int l = 0; l < newAttributes.size(); l++)
               {
                  PSAttribute newAtt = (PSAttribute)newAttributes.get(l);
                  if(newAtt.getName().equals(oldAttrib.getName()))
                  {
                     newAttr = newAtt;
                     break;
                  }
               }
               if(newAttr != null)
               {
                  List oldAttribValues = oldAttrib.getValues();
                  List newAttribValues = newAttr.getValues();
                  if(oldAttribValues.size() != newAttribValues.size())
                     hasChanged = true;
                  else
                  {
                     for(int k = 0; k < newAttribValues.size(); k++)
                     {
                        String newValue = (String)newAttribValues.get(k);
                        if(!oldAttribValues.contains(newValue))
                        {
                           hasChanged = true;
                           break;
                        }
                     }
                  }
               }
               else
               {
                  hasChanged = true;
                  break;
               }
            }
         }
      }
      catch(ValidationException e)
      {
         e.printStackTrace();
      }
      return hasChanged;
   }

   /**
    * Accessor function to check whether member is modified or not.
    *
    * @return The modified flag.
    **/
   public boolean isModified()
   {
      return m_bModified;
   }

   /**
    * Accessor function to get role member data.
    *
    * @return The role member data, may be <code>null</code>.
    **/
   public AdminRoleMemberData getData()
   {
      return m_data;
   }

   /**
    * Implementation for selection changes of "View" and "Role" Combo boxes
    * and for selection of re-catalog item in properties editor.
    *
    * @see ItemListener#itemStateChanged
    **/
   public void itemStateChanged(ItemEvent event)
   {
      try {
         itemStateChangeAction(event);
      }
      catch(ValidationException e)
      {
         JOptionPane.showMessageDialog(null,
            Util.cropErrorMessage(e.getMessage()),
            ms_dlgResource.getString("error"), JOptionPane.ERROR_MESSAGE);

         /* As this exception happens when getting data from table, that is
          * when item gets deselected, so we have to keep the same item as
          * selected in drop-list.
          */
         //Remove and add item listener to prevent event firing by selecting.
         JComboBox source = (JComboBox)event.getSource();
         source.removeItemListener(this);
         source.setSelectedItem(event.getItem());

         /* Add listener in seperate thread which will delay and allows current
          * thread finishes selecting item
          */
         Thread listener = new AddListener(source, this);
         listener.start();
      }
   }

   /**
    * Utility method for item state change action in drop-lists.
    *
    * @param event the item event, assumed not to be <code>null</code>.
    *
    * @throws ValidationException if data in table fails validation.
    **/
   private void itemStateChangeAction(ItemEvent event)
      throws ValidationException
   {
      Object obj = event.getSource();

      if(obj == m_attrTypeCombo)
      {
         String item = (String)event.getItem();

         if(event.getStateChange() == ItemEvent.DESELECTED)
         {
            if(item.equals(ms_dlgResource.getString("attributeTypeGlobal")))
            {
               m_globalAttributes = m_propertiesPanel.getTableDataAsList();
            }
            else
            {
               m_roleSpecAttributes.put(m_roleCombo.getSelectedItem(),
                  m_propertiesPanel.getTableDataAsList());
            }
         }
         else
         {
            //If "Global" is selected shows global attributes in table and
            // disable role combo box.
            if(item.equals(ms_dlgResource.getString("attributeTypeGlobal")))
            {
               m_roleCombo.setEnabled(false);
               m_propertiesPanel.setTableDataFromList(m_globalAttributes);
            }
            //Shows selected role attributes
            else
            {
               m_roleCombo.setEnabled(true);
               PSAttributeList roleAttrs = (PSAttributeList)
                  m_roleSpecAttributes.get(m_roleCombo.getSelectedItem());
               m_propertiesPanel.setTableDataFromList(roleAttrs);
            }
         }
      }
      else if(obj == m_roleCombo)
      {
         if(event.getStateChange() == ItemEvent.DESELECTED)
         {
            m_roleSpecAttributes.put(event.getItem(),
               m_propertiesPanel.getTableDataAsList());
         }
         else
         {
            PSAttributeList roleAttrs = (PSAttributeList)
               m_roleSpecAttributes.get(event.getItem());
            m_propertiesPanel.setTableDataFromList(roleAttrs);
         }
      }
      else
      {
         //implementation for re-catalog action in attribute name editor
         if(event.getStateChange() == ItemEvent.SELECTED)
         {
            String item = (String)event.getItem();
            JComboBox editor = (JComboBox)obj;

            if(item.equals(m_propertiesPanel.ms_recatalogString))
            {
               Map propertyNameValues =
               SecurityRolePanel.catalogSubjectPropertyNameValues(true);
               List valueList =
                  SecurityRolePanel.catalogAdminProperty(propertyNameValues,
                  m_memberNameField.getText(),
                  SecurityRolePanel.SUBJECT_LOOKUP_TYPE);
               m_propertiesPanel.resetCatalogedProperties(valueList,this);
            }
         }
      }
   }

   /**
    * Inner class to handle focus event in the member name text field
    */
    private class FocusLostGainedListener extends FocusAdapter
    {
      public void focusLost(FocusEvent event)
      {
         if(event.getSource() == m_memberNameField)
         {
            resetPropertiesPanel();
         }
      }
      public void focusGained(FocusEvent event)
      {
         if(event.getSource() == m_memberNameField)
         {
            if(m_propertiesPanel.getTable().getCellEditor() != null)
               m_propertiesPanel.getTable().getCellEditor().stopCellEditing();

            resetPropertiesPanel();
         }
      }
    }

   /**
    * Inner class to handle key events for the member name field
    */
   private class KeyTextListener extends KeyAdapter
   {
      public void keyTyped(KeyEvent event)
      {
         if(event.getSource() == m_memberNameField)
            m_propertiesPanel.clearTable();
      }
   }

   /**
    * Inner class to handle action events on radio buttons
    */
   private class RadioListener implements ActionListener
   {
      public void actionPerformed(ActionEvent event)
      {
         if (event.getSource() instanceof JRadioButton)
         {
            JRadioButton button = (JRadioButton)event.getSource();
            if(button.isSelected())
               resetPropertiesPanel();
         }
      }
   }

   /**
    * Resets properties panel depending on the supplied member name,
    * provider and the user type. When adding a new member to a role, and the
    * member belongs to other roles as well, the member global attributes will
    * be shown in the propreties table, if such a member is found.
    */
   private void resetPropertiesPanel()
   {
      PSSubject subject = isExistingGlobalSubject();
      if (subject != null)
      {
         PSAttributeList list = subject.getAttributes();
         String attribType = (String)m_attrTypeCombo.getSelectedItem();
         if(attribType.equals("Global"))
            m_propertiesPanel.setTableDataFromList(list);
      }
      else
         m_propertiesPanel.clearTable();
    }

   /**
    * Utility class for adding item listener to combo box with some delay.
    **/
   private class AddListener extends Thread
   {
      /**
       * Constructor for this thread.
       *
       * @param source drop-list to which listener should be added, assumed
       * not to be <code>null</code>.
       * @param listener item listener to add, assumed not to be
       * <code>null</code>.
       **/
      public AddListener(JComboBox source, ItemListener listener)
      {
         m_source = source;
         m_listener = listener;
      }

      /**
       * Adds listener with delay of '250' milliseconds.
       **/
      public void run()
      {
         try {
            Thread.currentThread().sleep(250);
         }
         catch(InterruptedException e){}
         m_source.addItemListener(m_listener);
      }

      /**
       * The drop-list to which listener should be added, gets initialized in
       * constructor.
       */
      private JComboBox m_source;

      /**
       * The listener to be added to drop-list, gets initialized in
       * constructor.
       */
      private ItemListener m_listener;
   }

   /**
    * Global attributes of member, gets initialized in the constructor.
    * If the member is in edit mode, attributes are copied from member global
    * attributes, else new object is created.
    **/
   private PSAttributeList m_globalAttributes;

   /**
    * Role specific attributes of member, gets initialized in the constructor.
    * If the member is in edit mode, attributes are copied from member's
    * attributes for each role, else new object is created.
    **/
   private HashMap m_roleSpecAttributes;

   /**
    * The role member data, which encapsulates member subject, global attributes
    * and role specific attributes, initially set to <code>null</code> and new
    * object with data is created when OK button is clicked.
    **/
   private AdminRoleMemberData m_data = null;

   /**
    * The flag to indicate whether data is modified or not, initialized to
    * <code>false</code> and set to <code>true</code> when OK button is clicked.
    **/
   private boolean m_bModified = false;

   /**
    * The flag to indicate whether member is new or in edit mode, set to
    * <code>true</code> for editing, otherwise <code>false</code>,
    * gets initialized in the constructor.
    **/
   private boolean m_bEditing;

   /**
    * Text field for entering member name, gets initialized in
    * <code>createMemberCommandPanel()</code>. Disabled if the dialog is
    * invoked for editing member.
    **/
   private JTextField  m_memberNameField;

   /**
    * Radio button to select member type as User, gets initialized in
    * <code>createMemberCommandPanel()</code>,
    * disabled if the dialog is invoked for editing member.
    **/
   private JRadioButton m_userRadio;

   /**
    * Radio button to select member type as Group, gets initialized in
    * <code>createMemberCommandPanel()</code>,
    * disabled if the dialog is invoked for editing member.
    **/
   private JRadioButton m_groupRadio;

   /**
    * Combo box to hold attribute types of member("Global","Role"),
    * gets initialized in <code>createAttributeSettingsPanel()</code>.
    **/
   private JComboBox m_attrTypeCombo;

   /**
    * Combo box to hold roles of the member, disabled if "Global" is
    * selected in <code>m_attrTypeCombo</code>
    * gets initialized in <code>createAttributeSettingsPanel()</code>.
    **/
   private JComboBox m_roleCombo;

   /**
    * The PropertiesPanel containing the table with 2 columns
    * and a text area. This panel is useful for editing properties.
    * Text Area is used for editing value of a property.
    * Initialized in <code>initDialog()</code>.
    **/
   private UTPropertiesPanel m_propertiesPanel;

   /** A list of the role members, initialized in the constructor */
   private List m_roleMembers = null;

   /** The role configuration, gets initialized in the constructor*/
   private PSRoleConfiguration m_roleConfig;

   /** Dialog resource strings, initialized in <code>initDialog()</code>. **/
   private static ResourceBundle ms_dlgResource = null;

   /** Server resource strings, initialized in <code>initDialog()</code>. **/
   private static ResourceBundle ms_res = null;

}

/******************************************************************************
 *
 * [ ModifyMemberListDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.CatalogCatalogers;
import com.percussion.E2Designer.CatalogerMetaData;
import com.percussion.E2Designer.PSComboBox;
import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.UTFixedButton;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.E2Designer.Util;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSRelativeSubject;
import com.percussion.design.objectstore.PSRole;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.security.IPSSecurityProviderMetaData;
import com.percussion.security.PSBackEndTableProviderMetaData;
import com.percussion.util.PSStringFilter;
import com.percussion.util.PSStringOperation;
import com.percussion.xml.PSXmlTreeWalker;
import com.percussion.guitools.PSPropertyPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Dialog for modifying the member list of a role. Provides facility to catalog
 * members of different security provider and instances with filters.
 **/
public class ModifyMemberListDialog extends PSDialog
                                    implements ActionListener
{
   /**
    * Constructor for initializing data and set action commands for all buttons.
    *
    * @param frame the parent frame of the dialog. May be <code>null</code>
    * @param role the role of member list which need to be modified
    * May not be <code>null</code>.
    * @param config Server configuration. May not be <code>null</code>.
    * @param roleConfig Role configuration. May not be <code>null</code>.
    * @param members list of members of the role, may be <code>null</code> or
    * empty.
    *
    **/
   public ModifyMemberListDialog(JFrame frame,
                                PSRole role,
                                ServerConfiguration config,
                                PSRoleConfiguration roleConfig,
                                Collection members)
   {
      super(frame);

      if(role == null)
         throw new IllegalArgumentException("Members must have a role.");

      if(config == null)
         throw new IllegalArgumentException(
            "Server Configuration may not be null");

      if(roleConfig == null)
         throw new IllegalArgumentException(
            "Role Configuration may not be null");

      if( m_bInitialized = initDialog() )
      {
         try {
            String[] rolename = new String[1];
            rolename[0] = role.getName();
            setTitle(MessageFormat.format(
               ms_dlgResource.getString("title"), rolename));
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

      if (m_bInitialized)
      {
         m_role = role;
         m_roleConfig = roleConfig;

         m_config = config;
         m_conn = config.getServerConnection().getConnection();
         if(m_conn == null)
            throw new IllegalArgumentException(
               "Designer connection may not be null.");

         // collect the catalogers
         List<CatalogerMetaData> catalogerList = CatalogCatalogers.getCatalog(
            m_conn, true);

         // add the security providers into combo box in sorted order
         CatalogerMetaData[] catalogers = catalogerList.toArray(
            new CatalogerMetaData[catalogerList.size()]);
         Arrays.sort(catalogers);
         int backendTableProvider = 0;
         for (int i=0; i<catalogers.length; i++)
         {
            if (catalogers[i].getDisplayName().indexOf(
               PSBackEndTableProviderMetaData.SP_FULLNAME) >= 0)
            {
               backendTableProvider = i;
            }
            m_providerCombo.addItem(catalogers[i]);
         }
         // default to the backend security provider 
         m_providerCombo.setSelectedIndex(backendTableProvider);

         m_memberList.setCellRenderer(new MemberListCellRenderer());
         m_roleMemberList.setCellRenderer(new MemberListCellRenderer());

         DefaultListModel roleMemberModel =
            (DefaultListModel) m_roleMemberList.getModel();

         if (members != null)
         {
            Iterator memIterator = members.iterator();
            while (memIterator.hasNext())
            {
               UIRoleMember member = (UIRoleMember) memIterator.next();
               insertMember(roleMemberModel, member);
            }
         }

         m_catalogButton.setActionCommand("Catalog");
         m_catalogButton.setMnemonic('t');
         m_catalogButton.addActionListener(this);
         m_displayButton.setActionCommand("Display");
         m_displayButton.addActionListener(this);
         m_newButton.setActionCommand("New");
         m_newButton.setMnemonic('N');
         m_newButton.addActionListener(this);
         m_removeButton.setActionCommand("Remove");
         m_removeButton.setMnemonic('R');
         m_removeButton.addActionListener(this);
         m_addButton.setActionCommand("Add");
         m_addButton.setMnemonic('A');
         
         m_addButton.addActionListener(this);
         m_lookupFilterButton.setActionCommand("filter");
         m_lookupFilterButton.addActionListener(this);
         m_displayFilterButton.setActionCommand("filter");
         m_displayFilterButton.addActionListener(this);

         // add the listener after its data has been populated
         m_providerCombo.addItemListener(new ItemListener() {
            //Listener to change 'Type' panel radio buttons state based on
            //the selected provider instance.
            public void itemStateChanged(ItemEvent e) {
               if (e.getStateChange() == ItemEvent.SELECTED) {
                  changeTypePanelState((CatalogerMetaData) e.getItem());
               }
            }
         });
         changeTypePanelState(
            (CatalogerMetaData) m_providerCombo.getSelectedItem());
      }
   }

   /**
    * Creates dialog framework.
    * 
    * @return <code>true</code> to indicate initialization is successful,
    *         Otherwise <code>false</code>.
    */
   private boolean initDialog()
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

      try 
      {
         JPanel panel = new JPanel();
         getContentPane().add(panel);
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.setBorder((new EmptyBorder (5,10,5,10)));

         JPanel catalog_panel = createCatalogPanel();
         JPanel display_filter_panel = createDisplayFilterPanel();
         JPanel member_list_panel = createMemberListPanel();

         panel.add(catalog_panel);
         panel.add(Box.createVerticalStrut(10));
         panel.add(Box.createVerticalGlue());
         panel.add(display_filter_panel);
         panel.add(Box.createVerticalGlue());
         panel.add(Box.createVerticalStrut(10));
         panel.add(member_list_panel);
         panel.add(Box.createVerticalStrut(10));
         panel.add(Box.createVerticalGlue());
         
         JPanel cmdPanel = new JPanel(new BorderLayout());
         cmdPanel.add(createCommandPanel(), BorderLayout.EAST);
         panel.add(cmdPanel);
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
    * Creates panel with the catalog filter fields.
    * 
    * @return panel with controls, never <code>null</code>
    */
   private JPanel createCatalogPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();
      
      m_providerCombo = new PSComboBox();
      panel.addPropertyRow(ms_dlgResource.getString("provider"),
         new JComponent[] { m_providerCombo });

      m_lookupFilter = new JTextField("%");
      m_lookupFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
      m_lookupFilter.setToolTipText(ms_dlgResource.getString("filterTip"));
      
      m_lookupFilterButton = new UTFixedButton("...", 25, 25);
      m_lookupFilterButton.setToolTipText(
         ms_dlgResource.getString("filterButtonTip"));
      
      JPanel lookupFilterPanel = new JPanel();
      lookupFilterPanel.setLayout(new BoxLayout(lookupFilterPanel, 
         BoxLayout.X_AXIS));
      lookupFilterPanel.add(m_lookupFilter);
      lookupFilterPanel.add(m_lookupFilterButton);
      
      panel.addPropertyRow(ms_dlgResource.getString("catalogFilter"),
         new JComponent[] { lookupFilterPanel });

      m_catalogButton = new UTFixedButton(
         ms_dlgResource.getString("catalogButton"));
      panel.addControlsRow(new JPanel(), m_catalogButton);

      m_usersRadio = new JRadioButton(ms_dlgResource.getString("userFilter"));
      m_groupsRadio = new JRadioButton(ms_dlgResource.getString("groupFilter"));
      m_bothRadio = new JRadioButton(ms_dlgResource.getString("bothFilter"));
      m_bothRadio.setSelected(true);

      ButtonGroup group = new ButtonGroup();
      group.add(m_usersRadio);
      group.add(m_groupsRadio);
      group.add(m_bothRadio);

      JPanel radio_panel = new JPanel();
      radio_panel.setLayout(new BoxLayout(radio_panel,BoxLayout.Y_AXIS));
      radio_panel.setBorder(createGroupBorder(
         ms_dlgResource.getString("typeBorderTitle")));
      radio_panel.add(m_usersRadio);
      radio_panel.add(m_groupsRadio);
      radio_panel.add(m_bothRadio);

      JPanel catalog_panel = new JPanel();
      catalog_panel.setLayout(new BoxLayout(catalog_panel,BoxLayout.X_AXIS));
      catalog_panel.setBorder(createGroupBorder(
         ms_dlgResource.getString("parametersBorder")));

      catalog_panel.add(Box.createHorizontalGlue());
      catalog_panel.add(panel);
      catalog_panel.add(Box.createHorizontalStrut(20));
      catalog_panel.add(Box.createHorizontalGlue());
      catalog_panel.add(radio_panel);

      return catalog_panel;
   }

   /**
    * Changes the state of radio buttons in 'Type' panel used for cataloging
    * criteria based on the specifed security provider instance. If the
    * instance does not support groups, then all options in panel were disabled
    * with 'Users' option selected, otherwise all are enabled.
    *
    * @param data the cataloger meta data, assumed not to
    * be <code>null</code> .
    */
   private void changeTypePanelState(CatalogerMetaData data)
   {
      if( !getSupportedObjectTypes(data).contains(
         IPSSecurityProviderMetaData.OBJECT_TYPE_GROUP) )
      {
         m_usersRadio.setSelected(true);
         m_usersRadio.setEnabled(false);
         m_groupsRadio.setEnabled(false);
         m_bothRadio.setEnabled(false);
      }
      else
      {
         m_usersRadio.setEnabled(true);
         m_groupsRadio.setEnabled(true);
         m_bothRadio.setEnabled(true);
      }
   }

   /**
    * Sets the cataloging parameters to the cataloging params of the previous
    * cataloging (within the same session of the admin client).
    * @param catalogParam, a catalogProps that holds all the params, if gets
    * here never<code>null</code>
    */
   public void setCatalogParams(CatalogParameters catalogParam)
   {
      CatalogerMetaData meta = catalogParam.getCatalogProvider();
      m_providerCombo.setSelectedItem(meta);
      m_displayFilter.setText(catalogParam.getDisplayText());
      m_lookupFilter.setText(catalogParam.getLookupText());
      String objectType = catalogParam.getCatalogObjectType();
      if(objectType.equals(ms_res.getString("user_type")))
         m_usersRadio.setSelected(true);
      else if (objectType.equals(ms_res.getString("group_type")))
         m_groupsRadio.setSelected(true);
      else if(objectType.equals(ms_res.getString("user_type") + "," +
         ms_res.getString("group_type")))
            m_bothRadio.setSelected(true);

      onCatalog();
   }

   /**
    *   Creates the command panel with OK, Cancel and Help buttons.
    *
    * @return the command panel, never <code>null</code>
    */
   private JPanel createCommandPanel()
   {
      UTStandardCommandPanel commandPanel =
         new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
         public void onOk()
         {
            ModifyMemberListDialog.this.onOk();
         }

      };

      getRootPane().setDefaultButton(commandPanel.getOkButton());

      return commandPanel;
   }

   /**
    *   Creates the panel with display filter fields.
    *
    * @return the panel with display filter fields, never <code>null</code>.
    */
   private JPanel createDisplayFilterPanel()
   {
      JPanel displayFilterPanel = new JPanel();
      displayFilterPanel.setLayout(new BoxLayout(displayFilterPanel, 
         BoxLayout.X_AXIS));

      m_displayFilter = new JTextField("%", 25);
      m_displayFilter.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
      m_displayFilter.setToolTipText(ms_dlgResource.getString("filterTip"));

      m_displayFilterButton = new UTFixedButton("...", 27, 25);
      m_displayFilterButton.setToolTipText(ms_dlgResource.getString(
         "filterButtonTip"));

      m_displayButton = new UTFixedButton(new ImageIcon (getClass().getResource(
         ms_dlgResource.getString("displayButton"))), 27, 25);
      m_displayButton.setToolTipText(ms_dlgResource.getString(
         "displayButtonTip"));

      displayFilterPanel.add(m_displayFilter);
      displayFilterPanel.add(m_displayFilterButton);
      displayFilterPanel.add(Box.createHorizontalStrut(2));
      displayFilterPanel.add(m_displayButton);

      PSPropertyPanel panel = new PSPropertyPanel();
      
      panel.addPropertyRow(ms_dlgResource.getString("displayFilter"),
         new JComponent[] { displayFilterPanel });

      return panel;
   }

   /**
    *   Creates the panel with cataloged member list, role member list and
    * buttons to move from one list to other and a button to add a new member.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createMemberListPanel()
   {
      JPanel catalog_list_panel = new JPanel();
      catalog_list_panel.setLayout(
         new BoxLayout(catalog_list_panel, BoxLayout.Y_AXIS));
      catalog_list_panel.setAlignmentY(CENTER_ALIGNMENT);

      JLabel label = new JLabel(ms_dlgResource.getString("catalogListTitle"),
         SwingConstants.LEFT);
      label.setAlignmentX(LEFT_ALIGNMENT);

      catalog_list_panel.add(label);

      m_memberList = new JList(new DefaultListModel());
      m_memberList.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent event)
         {
            boolean enabled = true;
            int[] indices = m_memberList.getSelectedIndices();
            for (int i=0; enabled && i<indices.length; i++)
            {
               int index = indices[i];
               Object obj = m_memberList.getModel().getElementAt(index);
               if (getRoleMembersList().contains(obj))
                  enabled = false;
            }
            
            m_addButton.setEnabled(enabled);
         }
      });
      JScrollPane m_memberListScroller = new JScrollPane(m_memberList);
      m_memberListScroller.setPreferredSize(new Dimension(200,200));
      m_memberListScroller.setMinimumSize(new Dimension(150, 100));
      m_memberListScroller.setMaximumSize(new Dimension(200, Short.MAX_VALUE));
      m_memberListScroller.setAlignmentX(LEFT_ALIGNMENT);

      catalog_list_panel.add(m_memberListScroller);

      JPanel list_button_panel = new JPanel();
      list_button_panel.setLayout(
         new BoxLayout(list_button_panel, BoxLayout.Y_AXIS));
      list_button_panel.setAlignmentY(CENTER_ALIGNMENT);

      m_newButton = new UTFixedButton(
         ms_dlgResource.getString("newButton"),100,25);
      m_newButton.setAlignmentX(CENTER_ALIGNMENT);
      m_removeButton = new UTFixedButton(
         ms_dlgResource.getString("removeButton"),100,25);
      m_removeButton.setAlignmentX(CENTER_ALIGNMENT);
      m_removeButton.setEnabled(false);
      m_addButton = new UTFixedButton(
         ms_dlgResource.getString("addButton"),100,25);
      m_addButton.setAlignmentX(CENTER_ALIGNMENT);
      m_addButton.setEnabled(false);
      list_button_panel.add(m_addButton);
      list_button_panel.add(Box.createVerticalStrut(10));
      list_button_panel.add(m_removeButton);
      list_button_panel.add(Box.createVerticalStrut(10));
      list_button_panel.add(m_newButton);

      JPanel rolemember_list_panel = new JPanel();
      rolemember_list_panel.setLayout(
         new BoxLayout(rolemember_list_panel, BoxLayout.Y_AXIS));
      rolemember_list_panel.setAlignmentY(CENTER_ALIGNMENT);

      JLabel label1 = new JLabel(
         ms_dlgResource.getString("roleMemberListTitle"), SwingConstants.LEFT);
      label1.setAlignmentX(LEFT_ALIGNMENT);

      rolemember_list_panel.add(label1);

      m_roleMemberList = new JList(new DefaultListModel());
      JScrollPane m_roleMemberListScroller = new JScrollPane(m_roleMemberList);
      m_roleMemberListScroller.setPreferredSize(new Dimension(200,200));
      m_roleMemberListScroller.setMinimumSize(new Dimension(150,100));
      m_roleMemberListScroller.setMaximumSize(
         new Dimension(200,Short.MAX_VALUE));
      m_roleMemberListScroller.setAlignmentX(LEFT_ALIGNMENT);

      m_roleMemberList.addKeyListener( new KeyAdapter()
      {
         public void keyPressed (KeyEvent e)
         {
            if (e.getKeyCode() == KeyEvent.VK_DELETE)
               onRemove();
         }
      });
      
      m_roleMemberList.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent event)
         {
            m_removeButton.setEnabled(event.getFirstIndex() >= 0);
         }
      });

      rolemember_list_panel.add(m_roleMemberListScroller);

      JPanel member_list_panel = new JPanel();
      member_list_panel.setLayout(
         new BoxLayout(member_list_panel, BoxLayout.X_AXIS));
      member_list_panel.add(catalog_list_panel);
      member_list_panel.add(Box.createHorizontalStrut(10));
      member_list_panel.add(Box.createHorizontalGlue());
      member_list_panel.add(list_button_panel);
      member_list_panel.add(Box.createHorizontalGlue());
      member_list_panel.add(Box.createHorizontalStrut(10));
      member_list_panel.add(rolemember_list_panel);

      return member_list_panel;
   }

   /**
    * Performs the actions based on event's action command.
    *
    * @see ActionListener#actionPerformed
    **/
   public void actionPerformed(ActionEvent event)
   {
      String command = event.getActionCommand();

      if(command.equals("Catalog"))
         onCatalog();
      else if(command.equals("Display"))
         onDisplay();
      else if(command.equals("New"))
         onNew();
      else if(command.equals("Remove"))
         onRemove();
      else if(command.equals("Add"))
         onAdd();
      else if(command.equals("filter"))
         showFilterDialog(event);
   }

   /**
    * Shows Filter dialog with the text in edit mode if any of the filter
    * buttons are clicked and updates the filter fields with modified text.
    *
    * The semicolons in text are treated as seperators and each string seperated
    * by semicolons are displayed as seperate line in editor. If the semicolon
    * to be treated as part of pattern, it should be escaped by semicolon.
    *
    * @param event the action event. assumed not <code>null</code>.
    **/
   private void showFilterDialog(ActionEvent event)
   {
      Object obj = event.getSource();
      if(obj == m_lookupFilterButton)
      {
         FilterEditorDialog editor = new FilterEditorDialog(
            AppletMainDialog.getMainframe(),
            ms_dlgResource.getString("catalogFilterTitle"),
            m_lookupFilter.getText());

         editor.setVisible(true);
         if(editor.isModified())
            m_lookupFilter.setText(editor.getFilterText());

      }
      else  if(obj == m_displayFilterButton)
      {
         FilterEditorDialog editor = new FilterEditorDialog(
            AppletMainDialog.getMainframe(),
            ms_dlgResource.getString("displayFilterTitle"),
            m_displayFilter.getText());
         editor.setVisible(true);
         if(editor.isModified())
            m_displayFilter.setText(editor.getFilterText());
      }
   }

   /**
    * Catalogs members from server based on Pcataloger, filter and member
    * type selection. If the cataloged members are already members of role, they
    * won't be displayed in catalog list. If the display filter field has data,
    * that is also included in filtering to display members in cataloged members
    * list.
    **/
   public void onCatalog()
   {
      m_bIsCatalog = true;

      m_catalogProviderInstance = 
         (CatalogerMetaData) m_providerCombo.getSelectedItem();

      m_lookupFilterText = m_lookupFilter.getText();

      m_catalogMembers = catalogMembers();

      onDisplay();
   }

   /**
    * Gets filter pattern as an array of strings.
    *
    * @param text the text from which array of patterns need to get, may not be
    * <code>null</code>.
    * @return array of filter pattern strings, never <code>null</code>,
    * may be empty.
    *
    **/
   public String[] getFilterPattern(String text)
   {
      if(text == null)
         throw new IllegalArgumentException(
            "the text to get filter pattern may not be null");

      // replaces ';' with '\n' and ';;' with ';'
      text = PSStringOperation.replaceChar(text,
         ms_dlgResource.getString("filterSeperator").charAt(0), '\n');

      StringTokenizer st = new StringTokenizer(text,"\n");
      Vector tokens = new Vector();
      while(st.hasMoreTokens())
         tokens.addElement(st.nextToken());

      String[] pattern = new String[tokens.size()];
      tokens.copyInto(pattern);
      return pattern;
   }

   /**
    * Catalogs server to get the list of supported object types for the supplied
    * instance name if they are not yet cached. Logs the error to console if an
    * exception happens in the catalog process.
    *
    * @param data the cataloger to get supported object types, assumed
    * not to be <code>null</code>.
    *
    * @return the list of supported object types for the instance, never <code>
    * null</code>, may be empty.
    */
   private List getSupportedObjectTypes(CatalogerMetaData data)
   {
      if(m_instanceObjectTypesMap.get(data) != null)
         return (List)m_instanceObjectTypesMap.get(data);

      try
      {
         //initialize the cataloger only if needed
         if(m_psCataloger == null)
            m_psCataloger   = new PSCataloger(m_conn);
      }
      catch(IllegalArgumentException e)
      {
         //should not come here
      }

      List list = new ArrayList();
      Properties  catalogProps = new Properties( );

      try
      {
         Document xmlDoc = null;
         catalogProps.put("RequestCategory", "security");
         catalogProps.put("RequestType", "ObjectTypes");
         catalogProps.put("CatalogerName", data.getName());
         catalogProps.put("CatalogerType", data.getType());

         xmlDoc = m_psCataloger.catalog( catalogProps );
         PSXmlTreeWalker tree = new PSXmlTreeWalker(xmlDoc);

         Element objectTypeElement;

         while (
            (objectTypeElement = tree.getNextElement("ObjectType", true, true))
            != null)
         {
            String type = objectTypeElement.getAttribute("type");

            if (type != null)
               list.add(type);
         }
      }
      catch (Exception e)
      {
         System.out.println(e.getLocalizedMessage());
         e.printStackTrace();
      }

      m_instanceObjectTypesMap.put(data, list);

      return list;
   }

   /**
    * Gets the members for the currently selected Security Provider Instance,
    * filter and member type and shows error messages when an exception happens.
    *
    * @return collection of members, never <code>null</code>, may be empty.
    */
   private Collection catalogMembers()
   {
      try
      {
         //initialize the cataloger only if needed
         if(m_psCataloger == null)
            m_psCataloger   = new PSCataloger(m_conn);
      }
      catch (IllegalArgumentException e)
      {
         //should not come here
      }

      Vector list = new Vector();
      Properties  catalogProps = new Properties( );

      if (m_usersRadio.isSelected())
         m_objectType = ms_res.getString("user_type");
      else if (m_groupsRadio.isSelected())
         m_objectType = ms_res.getString("group_type");
      else if (m_bothRadio.isSelected())
         m_objectType = ms_res.getString("user_type") + "," +
            ms_res.getString("group_type");

      try
      {
         Document xmlDoc = null;
         catalogProps.put("RequestCategory", "security");
         catalogProps.put("RequestType", "Object");
         catalogProps.put("CatalogerName", m_catalogProviderInstance.getName());
         catalogProps.put("CatalogerType", m_catalogProviderInstance.getType());
         catalogProps.put("ObjectType", m_objectType);

         String filter = FILTER_MATCH_ANY;
         if (m_lookupFilter.getText().trim().length() != 0)
            filter = m_lookupFilter.getText().trim();

         catalogProps.put("Filter", filter);

         xmlDoc = m_psCataloger.catalog(catalogProps);
         PSXmlTreeWalker tree = new PSXmlTreeWalker(xmlDoc);

         Element objectElement;

         while ((objectElement = tree.getNextElement("Object", true, true))
            != null)
         {
            String name = tree.getElementData("name", false);
            String type = objectElement.getAttribute("type");

            if (name != null && type != null)
            {
               int userType = 0;
               if (type.equals(ms_res.getString("user_type")))
                  userType = PSRelativeSubject.SUBJECT_TYPE_USER;
               else if (type.equals(ms_res.getString("group_type")))
                  userType =PSRelativeSubject.SUBJECT_TYPE_GROUP;

               PSRelativeSubject memSubject = new PSRelativeSubject(name, 
                  userType, null);
               list.addElement(new UIRoleMember(memSubject, m_roleConfig));
            }
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
         JOptionPane.showMessageDialog(null,
            Util.cropErrorMessage(
            ms_res.getString("ioerror") + e.getLocalizedMessage()),
            ms_dlgResource.getString("error"), JOptionPane.ERROR_MESSAGE);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         JOptionPane.showMessageDialog(null,
         Util.cropErrorMessage(e.getLocalizedMessage()),
         ms_dlgResource.getString("error"), JOptionPane.ERROR_MESSAGE);
      }

      return list;
   }

   /**
    * Filters the members of cataloged list based on display filter and displays
    * the list in cataloged members list and sets tooltip for the list with
    * cataloging parameters and display filter. If there are no cataloged
    * members, it does nothing.
    **/

   public void onDisplay()
   {
      if (m_catalogMembers == null)
         return;

      m_displayFilterText = m_displayFilter.getText();

      Collection displayMembers = new Vector();
      if (m_displayFilter.getText().trim().length() == 0)
         displayMembers = m_catalogMembers;
      else 
      {
         String[] filterPattern = getFilterPattern(m_displayFilter.getText());
         PSStringFilter filter = new PSStringFilter(
            filterPattern, FILTER_MATCH_ONE, FILTER_MATCH_ANY.charAt(0), false);

         Iterator memIterator = m_catalogMembers.iterator();
         while (memIterator.hasNext())
         {
            UIRoleMember member = (UIRoleMember) memIterator.next();

            if (filter.accept( member.getMemberName()))
               displayMembers.add(member);
         }
      }

      DefaultListModel memberModel = (DefaultListModel) m_memberList.getModel();
      memberModel.clear();

      Iterator memIterator = displayMembers.iterator();
      while (memIterator.hasNext())
      {
         UIRoleMember member = (UIRoleMember) memIterator.next();
         insertMember(memberModel, member);
      }

      String tooltipText = m_catalogProviderInstance.getDisplayName();

      if (m_lookupFilterText.length() > 0)
      {
         tooltipText += " & " + ms_dlgResource.getString("catalogFilterTip") +
             m_lookupFilterText;
      }

      if (m_displayFilterText.length() > 0)
      {
         tooltipText += " & " + ms_dlgResource.getString("displayFilterTip") +
            m_displayFilterText;
      }
      
      m_memberList.setToolTipText(tooltipText);
   }

   /**
    * Closes the dialog upon setting the data as modified and save the role
    * members.
    **/
   public void onOk()
   {
      m_bModified = true;
      m_roleMembers = getRoleMembersList();

      /*If cataloging was performed save the cataloging properties.*/
      if (m_bIsCatalog)
      {
         m_catalogParameters = new CatalogParameters(m_catalogProviderInstance, 
            m_objectType, m_lookupFilterText, m_displayFilterText);

      }
      setVisible(false);
      dispose();
   }

   /**
    * Accessor function to check whether member list is modified or not.
    *
    * @return The modified  flag.
    **/
   public boolean isModified()
   {
      return  m_bModified;
   }

   /**
    * Accessor function to get role member list.
    *
    * @return collection of role members. May be <code>null</code> or empty.
    **/
   public Collection getRoleMembers()
   {
      return m_roleMembers;
   }

   /**
    * Inserts a new member keeping alphabetical order
    * @param listModel, a model of a list that this member is about to be
    * added to, assumed not to be <code>null</code>
    * @param member a member to be added to the list, assumed not to be
    * <code>null</code>
    */
    private void insertMember(DefaultListModel listModel,  UIRoleMember member)
    {
      int iCount = 0;
      iCount = listModel.getSize();
      String memberName = member.getMemberName();

      Collator c = Collator.getInstance();
      c.setStrength(Collator.SECONDARY);
      int i = 0;
      if(iCount > 0)
      {
         for(i = 0; i < iCount; i++)
         {
            UIRoleMember tempMember =  (UIRoleMember)listModel.getElementAt(i);
            String temMemberName = tempMember.getMemberName();
            if (c.compare(memberName, temMemberName) < 0)
               break;
         }
      }
      if(i >= iCount)
         listModel.addElement(member);
      else
         listModel.insertElementAt(member, i);
    }

   /**
    * Gets all members in role member list box.
    *
    * @return collection of role members. Never <code>null</code>, May be empty.
    **/
   private Collection getRoleMembersList()
   {
      //get role members from list box
      DefaultListModel roleMemberModel =
         (DefaultListModel)m_roleMemberList.getModel();
      Vector roleMembers = new Vector(roleMemberModel.capacity());

      for(Enumeration e =  roleMemberModel.elements();
         e.hasMoreElements(); )
      {
         roleMembers.add(e.nextElement());
      }

      return roleMembers;
   }

   /**
    * Shows New Member Dialog. Called when NEW button is clicked. Updates role
    * member list if new member is added and is not duplicate of any existing
    * member.
    **/
   public void onNew()
   {
      Vector roleMembers = (Vector)getRoleMembersList();

      AdminMemberDialog memberDialog =
         new AdminMemberDialog(AppletMainDialog.getMainframe(), m_config,
         m_roleConfig, m_role, roleMembers);
      memberDialog.setVisible(true);
      if(memberDialog.isModified())
      {
         AdminRoleMemberData data = memberDialog.getData();

         int userType = 0;
         if (data.isUser())
            userType = PSRelativeSubject.SUBJECT_TYPE_USER;
         else if (data.isGroup())
            userType =PSRelativeSubject.SUBJECT_TYPE_GROUP;

         PSAttributeList attrs =
            (PSAttributeList)data.getAttributes(m_role.getName());
         PSAttributeList globalAttrs =
            (PSAttributeList)data.getGlobalAttributes();

         PSRelativeSubject subject =
            new PSRelativeSubject(data.getName(), userType, attrs);

         UIRoleMember roleMember = new UIRoleMember(subject, m_roleConfig);

         if(!roleMembers.contains(roleMember))
         {
            DefaultListModel roleMemberModel =
               (DefaultListModel)m_roleMemberList.getModel();

            insertMember(roleMemberModel,roleMember);
            roleMember.setMemberAttributes(globalAttrs);

         }
      }
   }

   /**
    * Removes selected members from role member list and if their provider
    * instance is same as cataloged member's list, it will be moved to that
    * list.
    */
   public void onRemove()
   {
      DefaultListModel roleMemberModel =
         (DefaultListModel) m_roleMemberList.getModel();

      int[] sel_indices = m_roleMemberList.getSelectedIndices();
      for (int i=sel_indices.length-1; i>=0; i--)
      {
         UIRoleMember member = (UIRoleMember) roleMemberModel.getElementAt(
            sel_indices[i]);

         roleMemberModel.removeElement(member);
      }
      
      m_roleMemberList.clearSelection();
      m_removeButton.setEnabled(false);
      onDisplay();
   }

   /**
    * Adds the selected members from cataloged member list to role member list.
    */
   public void onAdd()
   {
      DefaultListModel roleMemberModel =
         (DefaultListModel)m_roleMemberList.getModel();
      DefaultListModel memberModel =
        (DefaultListModel)m_memberList.getModel();

      int[] sel_indices = m_memberList.getSelectedIndices();
      for(int i = sel_indices.length-1; i >= 0; i--)
      {
         UIRoleMember member = 
            (UIRoleMember) memberModel.getElementAt(sel_indices[i]);
            
         if (!roleMemberModel.contains(member))
            insertMember(roleMemberModel, member);
      }
      
      m_memberList.clearSelection();
      m_addButton.setEnabled(false);
   }

   /**
    * Gets the object that holds cataloging parameters.
    * @return catatlog properties, can be <code>null</code>
    */
   public CatalogParameters getCatalogParameters()
   {
      return m_catalogParameters;
   }

   /**
    * Custom Cell Renderer for displaying members in List.
    **/
   private class MemberListCellRenderer extends DefaultListCellRenderer
   {
      /**
       * Gets cell renderer component for cell in the list. Sets an icon,
       * text to the cell and tooltip if the source list is role member list
       *
       * @see ListCellRenderer#getListCellRendererComponent
       **/
      public Component getListCellRendererComponent(JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus)
      {
         if (value instanceof UIRoleMember)
         {
            UIRoleMember member = (UIRoleMember) value;

            if (member.getType().equals(ms_res.getString("user_type")))
               setIcon (new ImageIcon (
                  getClass().getResource(ms_res.getString("gif_user_member"))));
            else if(member.getType().equals(ms_res.getString("group_type")))
               setIcon (new ImageIcon (
                  getClass().getResource(ms_res.getString("gif_group_member"))));

            setText(member.getMemberName());
         }
         else
            setText(value.toString());

         if (isSelected) 
         {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
         }
         else 
         {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
         }
            
         if (list.equals(m_memberList))
         {
            Collection members = getRoleMembersList();
            if (members.contains(value))
               setBackground(Color.green);
         }
         
         return this;
      }
   }

   /**
    * Combo box which will hold security provider and instance information,
    * in the form of "provider/instance", gets initialized in
    * <code>createCatalogCommandPanel()</code>.
    */
   private PSComboBox m_providerCombo;

   /**
    * Text Field to enter filter for cataloging, gets initialized in
    * <code>createCatalogCommandPanel()</code>.
    */
   private JTextField m_lookupFilter;

   /**
    * String varible to hold current filter used for cataloging, is set in
    * <code>onCatalog()</code> whenever catalog button is clicked.
    */
   private String m_lookupFilterText;

   /**
    * Button to perform catalog action, gets initialized in
    * <code>createCatalogCommandPanel()</code>.
    */
   private UTFixedButton m_catalogButton;

   /**
    * Button to open an editor dialog for catalog filter, gets initialized in
    * <code>createCatalogCommandPanel()</code>.
    */
   private UTFixedButton m_lookupFilterButton;

   /**
    * Radio button to select user member type for cataloging, gets initialized
    * in <code>createCatalogCommandPanel()</code>.
    */
   private JRadioButton m_usersRadio;

   /**
    * Radio button to select group member type for cataloging, gets initialized
    * in <code>createCatalogCommandPanel()</code>.
    */
   private JRadioButton m_groupsRadio;

   /**
    * Radio button to select both types for cataloging, gets initialized
    * in <code>createCatalogCommandPanel()</code>.
    */
   private JRadioButton m_bothRadio;

   /**
    * Varible to hold current filter used for displaying cataloged
    * members, is set in {@link #onDisplay() onDisplay}.
    */
   private String m_displayFilterText;

   /**
    * Text Field to enter filter for display, gets initialized in
    * <code>createDisplayFilterPanel()</code>.
    */
   private JTextField m_displayFilter;

   /**
    * Button to open an editor dialog for display filter, gets initialized in
    * <code>createDisplayFilterPanel()</code>.
    */
   private UTFixedButton m_displayFilterButton;

   /**
    * Button to perform displaying of cataloged members with display filter,
    * gets initialized in <code>createDisplayFilterPanel()</code>.
    */
   private UTFixedButton m_displayButton;

   /**
    * List to hold cataloged members, gets initialized in
    * <code>createMemberListPanel()</code>.
    */
   private JList m_memberList;

   /**
    * Button to show new member dialog to add a new member to role, gets
    * initialized in <code>createMemberListPanel()</code>.
    */
   private UTFixedButton m_newButton;

   /**
    * Button to remove members from role member list and move to cataloged
    * members list, if the instance of member is same as cataloged members list,
    * gets initialized in <code>createMemberListPanel()</code>.
    */
   private UTFixedButton m_removeButton;

   /**
    * Button to move members from cataloged member list to role members list,
    * gets initialized in <code>createMemberListPanel()</code>.
    */
   private UTFixedButton m_addButton;

   /**
    * List to hold role members, gets initialized in
    * <code>createMemberListPanel()</code>.
    */
   private JList m_roleMemberList;

   /**
    * Data Modified flag. Initialized to <code>false</code> and set to
    * <code>true</code> in {@link #onOk() onOk}.
    */
   private boolean m_bModified = false;

   /** The role of member list, gets initialized in constructor. */
   private PSRole m_role;

   /** The list of role members, gets initialized in {@link #onOk() onOk}. */
   private Collection m_roleMembers;

   /** The server configuration, gets initialized in the constructor. */
   private ServerConfiguration m_config;

   /** The role configuration, gets initialized in the constructor*/
   private PSRoleConfiguration m_roleConfig;

   /**The designer connection to server, gets initialized in the constructor. */
    private PSDesignerConnection m_conn = null;

   /**
    * The cataloger object, gets initialized in <code>catalogMembers()</code>.
    */
   private PSCataloger m_psCataloger = null;


   /**
    * Current catalog provider instance used for cataloging members,
    * gets initialized in {@link #onCatalog() onCatalog}.
    */
   private CatalogerMetaData m_catalogProviderInstance;

   /**
    * Currently cached cataloged members,
    * gets initialized in {@link #onCatalog() onCatalog}.
    */
   private Collection m_catalogMembers;

   /**
    * Caches the supported object types for each security provider instance with
    * instance name (<code>String</code>) as key and <code>List</code> of
    * supported object types (<code>String</code>) for the instance as value.
    */
   private Map m_instanceObjectTypesMap = new HashMap();

   /**
    * A flag that indicates wheter or not Catalog button was pressed
    * gets set to <code>true</code>, when cataloging is performed.
    */
    private boolean m_bIsCatalog = false;

   /**
    * A string that represents a type for which cataloging is done.
    * Currently we support three types user 'Users', 'Groups' and 'Both', gets
    * initialized in <code>catalogMembers()</code>.
    */
    private String m_objectType = null;

   /** A variable that holds cataloging parameters, gets initialized in
    *  {@link #onOk() onOK}
    */
    private CatalogParameters m_catalogParameters = null;

   /**
    * The character to use in filter pattern to match any
    * single character in the filtered text.
    **/
   public final static char FILTER_MATCH_ONE = '_';

   /**
    * The character to use in  filter pattern to
    * match 0 or more characters in the filtered text.
    **/
   public final static String FILTER_MATCH_ANY = "%";

  /** Dialog resource strings, initialized in <code>initDialog()</code>. **/
   private static ResourceBundle ms_dlgResource = null;

   /**
    * Server resource strings.
    * Initialized in <code>initDialog()</code>.
    **/
   private static ResourceBundle ms_res = null;

}


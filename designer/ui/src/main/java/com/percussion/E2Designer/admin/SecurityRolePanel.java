/******************************************************************************
 *
 * [ SecurityRolePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.PSComboBox;
import com.percussion.E2Designer.UTReadOnlyTableCellEditor;
import com.percussion.E2Designer.Util;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSDatabaseComponentCollection;
import com.percussion.design.objectstore.PSRelativeSubject;
import com.percussion.design.objectstore.PSRole;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.util.PSStringOperation;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The sub-tab on the &quot;Security&quot; tab of the admin client.  Allows
 * the administrator to add, delete, and edit Roles and Role Members.
 */
public class SecurityRolePanel extends JPanel implements ITabDataHelper
{
   /**
    * Constructor for creating panel.
    * @param config a Server Configuration, may not be <code>null</code>.
    * @param roleConf a roleConf, may not be <code>null</code>.
    * @throws if either server or role configuration is   <code>null</code>.
    */
   public SecurityRolePanel(ServerConfiguration config, PSRoleConfiguration roleConf)
   {
      super();
      if(config == null)
         throw new IllegalArgumentException(
            "Server Configuration must not be null");
      if (roleConf == null)
          throw new IllegalArgumentException(
            "Role Configuration must not be null");

      m_config = config;

      try
      {
         m_roleConfig = roleConf;
         m_roles = m_roleConfig.getRoles();
      }

      catch (Exception se)
      {
         se.printStackTrace();
         JOptionPane.showMessageDialog(null, Util.cropErrorMessage(
            se.getLocalizedMessage()), ms_res.getString("error"),
            JOptionPane.ERROR_MESSAGE);
      }

      initPanel();
      initData();
   }

   //see the interface for description
   public boolean saveTabData()
   {
      if (mb_needSave)
      {
         try
         {
            AppletMainDialog.getServerObjectStore().saveRoleConfiguration(
               m_roleConfig, false);

            //after saving the role config, reset the flag back to false
            mb_needSave = false;

            if(!AppletMainDialog.getOkFlag())
            {
               m_roleConfig = AppletMainDialog.getServerObjectStore(
                  ).getRoleConfiguration(true,false,false);
               m_roles = m_roleConfig.getRoles();
               rebuildTree();
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, Util.cropErrorMessage(
               e.getLocalizedMessage()), ms_res.getString("error"),
               JOptionPane.ERROR_MESSAGE);
         }
      }
      return true;
   }

   //see the interface for description
   public boolean validateTabData()
   {
      return true;
   }

   /**
    * Rebuilds the tree with the current role configuration.
    */
   private void rebuildTree()
   {
      DefaultMutableTreeNode root =
         (DefaultMutableTreeNode)m_treeModel.getRoot();
      clearTree(root);
      buildTree(root, m_roles);
   }

   /**
    * Performs basic initialization of this panel.
    */
   private void initPanel()
   {
      setLayout(new BorderLayout(0, 5));
      setBorder (new EmptyBorder (4,10,4,10));
      add(createViewPanel(), "Center");
      add(createViewCombo(), "North");
      add(createCommandPanel(), "South");
   }

   /**
    * Initializes controls with data.
    */
   private void initData()
   {
      m_viewCombo.setSelectedIndex(0);

      //set the default tree the role tree
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(
         ms_res.getString(ROLES));

      m_treeModel.setRoot(root);
      buildTree(root, m_roles);

      ms_serverRoot = m_config.getServerConfiguration().getRequestRoot();
   }

   /**
    * Creates the view panel with the combo box, the tree and the control panel.
    *
    * @return a newlly created panel, never <code>null</code>
    */
    private JPanel createViewPanel()
   {
      JPanel viewPanel = new JPanel();
      viewPanel.setLayout( new BoxLayout( viewPanel, BoxLayout.X_AXIS ));

      viewPanel.add(createTreePane(), "Center");
      viewPanel.add(Box.createHorizontalStrut( 10 ));
      viewPanel.add(createPropertiesPanel(), "East");

      return viewPanel;
   }

   /**
    * Creates a panel with the tree view.
    * @return a newlly created scroll panel, never <code>null</code>
    */
   private JScrollPane createTreePane()
   {
      // initialize tree
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(
         ms_res.getString(ROLES));
      m_treeModel = new DefaultTreeModel(root);
      m_tree = new JTree(m_treeModel);

      FocusListener focusList = new FocusAdapter()
      {
         public void focusGained (FocusEvent e)
         {
            mb_needSave = true;
         }
      };
      m_tree.addFocusListener(focusList);

      //add the treeCellRenderer
      TreeCellRenderer renderer = new TreeCellRenderer();
      ToolTipManager.sharedInstance().registerComponent(m_tree);
      m_tree.setCellRenderer(renderer);

      //add tree selection listener
      TreeSelection sel = new TreeSelection();
      m_tree.addTreeSelectionListener(sel);


      //add mouse listener for double click
      MouseListener mListener = new MouseAdapter()
      {
         public void mouseClicked (MouseEvent event)
         {
            int selRow = m_tree.getRowForLocation (event.getX(), event.getY());
            if (selRow > 0)
               if(event.getClickCount() == 2)
                  onEdit();
         }
      };
      m_tree.addMouseListener(mListener);

      KeyListener keyListener = new KeyAdapter()
      {
         public void keyPressed (KeyEvent e)
         {
            if (e.getKeyCode() == e.VK_DELETE)
               onDelete();
         }
      };

      m_tree.addKeyListener(keyListener);

      JScrollPane pane = new JScrollPane(m_tree);
      pane.setPreferredSize(new Dimension(350, 5000));
      return pane;
   }

   /**
    * Creates the member/role view combo box.
    * @return a newlly created panel, never <code>null</code>
    */
   private JPanel createViewCombo()
   {
      JPanel panel = new JPanel();

      JLabel labelView = new JLabel (ms_res.getString("view"));
      labelView.setHorizontalAlignment(SwingConstants.RIGHT);

      panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
      panel.setBorder(new EmptyBorder(15,25,4,2));
      panel.add(labelView );
      panel.add(Box.createHorizontalStrut(2));

      Object[] temp = { ms_res.getString("cbRoles"),
         ms_res.getString("cbMembers")};
      m_viewCombo = new PSComboBox( temp);
      m_viewCombo.setSize(new Dimension (60,20));

      //add the combo box item listener
      m_viewCombo.addItemListener( new ItemListener()
      {
         public void itemStateChanged( ItemEvent e )
         {
           onItemStateChanged();
         }
      } );

      panel.add(m_viewCombo);
      panel.add(Box.createHorizontalStrut (240));
      return panel;
   }

   /**
    * Creates the panel to the right, the properties panel.
    * @return a newlly created panel, never <code>null</code>
    */
   private JPanel createPropertiesPanel()
   {
      JPanel propPanel = new JPanel();
      propPanel.setLayout(new BoxLayout( propPanel, BoxLayout.Y_AXIS ));

      JLabel label = new JLabel();
      label.setText(ms_res.getString("cmsProp"));

      JLabel valLabel = new JLabel (ms_res.getString("value"));
      valLabel.setHorizontalAlignment(SwingConstants.LEFT);

      m_cmsPanel = new JPanel();
      m_cmsPanel.setLayout(new BoxLayout( m_cmsPanel, BoxLayout.Y_AXIS ));
      JPanel labelPan = new JPanel ();

      labelPan.add( label);

      labelPan.add(Box.createHorizontalStrut(20));
      m_cmsPanel.add(labelPan);

      //create the table
      m_cmsPanel.add(createTable());

      JPanel valuePanel = new JPanel ();
      valuePanel.setLayout(new BoxLayout( valuePanel, BoxLayout.Y_AXIS ));
      JPanel valLabelPan = new JPanel ();
      valLabelPan.add(valLabel);
      valLabelPan.add(Box.createHorizontalStrut(186));
      valuePanel.add(valLabelPan);

      //create the value text area
      m_valueText = new JTextArea (5,20);
      m_valueText.setLineWrap(true);
      m_valueText.setWrapStyleWord(true);
      m_valueText.setEnabled(false);

      JScrollPane pane = new JScrollPane (m_valueText);

      valuePanel.add(pane);
      propPanel.add(m_cmsPanel, "North");
      propPanel.add(valuePanel);
      return propPanel;
   }

   /**
    * Creates the CMS properties table.
    *
    * @return Scroll pane with table, never <code>null</code>.
    */
   private JScrollPane createTable ()
   {

      String [] tableHeaders =
         {ms_res.getString("colName"), ms_res.getString("colValue")};
      Object[][] temp = new Object[1][NUM_COLUMNS];

      m_tableModel = new DefaultTableModel( temp, tableHeaders );
      m_table = new JTable(m_tableModel);

      m_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      m_table.setIntercellSpacing(new Dimension(0,0));
      m_table.setRowSelectionAllowed(true);
      m_table.setColumnSelectionAllowed(false);
      m_table.setShowVerticalLines(true);
      m_table.setShowGrid(true);
      m_table.setRequestFocusEnabled(false);
      m_table.getTableHeader().setReorderingAllowed(false);
      m_table.getColumn(ms_res.getString("colName")).setPreferredWidth( 40 );
      m_table.getColumn(ms_res.getString("colValue")).setPreferredWidth( 40 );

      UTReadOnlyTableCellEditor editor = new UTReadOnlyTableCellEditor();
      for(int i =0; i < m_table.getColumnCount(); i++)
         m_table.getColumnModel().getColumn(i).setCellEditor(editor);

      //add the table selection listener
      m_table.getSelectionModel().addListSelectionListener(
         new TableSelectionListener());

      JScrollPane pane = new JScrollPane (m_table);
      pane.setPreferredSize(new Dimension (205, 137));
      return pane;

   }

   /**
    * Creates the bottom buttons, the command panel.
    *
    * @return The panel, never <code>null</code>.
    */
    private JPanel createCommandPanel()
    {

      m_roleButton = new UTFixedButton(
         ms_res.getString("addrole"), new Dimension (90 , 25));

      //add the action listener to the role button
      m_roleButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onAddRole();
         }
      });

      m_memberButton = new UTFixedButton(
         ms_res.getString("addmember"), new Dimension (120 , 25));

      //add the action listener to the member button
      m_memberButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onAddMember();

         }
      });

      m_editButton = new UTFixedButton(
         ms_res.getString("edit"), new Dimension (60 , 25));
      //add the actin listener to the edit button
      m_editButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onEdit();
         }
      });

      m_deleteButton = new UTFixedButton(
         ms_res.getString("delete"), new Dimension (70 , 25));
      //add the actin listener to the edit button
      m_deleteButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onDelete();
         }
      });

      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.add(m_roleButton);
      panel.add(Box.createHorizontalStrut(4));
      panel.add(m_memberButton);
      panel.add(Box.createHorizontalStrut(4));
      panel.add(m_editButton);
      panel.add(Box.createHorizontalStrut(4));
      panel.add(m_deleteButton);
      panel.add(Box.createHorizontalGlue());
      panel.setBorder(new EmptyBorder(4,0,0,0));

      return panel;
   }

   /**
    * Performs the action whenever a different view is selected. This will
    * update the view tree. Possible views are roles by members and members
    * by role.
    */
   private void onItemStateChanged()
   {
      String view = (String)m_viewCombo.getSelectedItem();
      DefaultMutableTreeNode root = null;

      if (view.equals(ms_res.getString("cbMembers")))
         root = new DefaultMutableTreeNode (
            ms_res.getString("members"));

      if (view.equals(ms_res.getString("cbRoles")))
         root = new DefaultMutableTreeNode (
            ms_res.getString(ROLES));

      m_treeModel.setRoot(root);
      buildTree (root, m_roles);
   }

   /**
    * Performs the action fired by the &quot;Add Member&quot; button.
    * If a role node is selected a new member is added to that role, if a member
    * is selected a new member is added to the role that the selected member
    * has.  In the Roles by Members view, if the selected member has more then
    * one role a message box pops-up instructing the user to select the role that
    * the new member will be added to.  In the case the selected member has only
    * one role a new member is automatically added to the same role.
    */
   private void onAddMember()
   {
      String view = (String)m_viewCombo.getSelectedItem();
      DefaultMutableTreeNode selNode = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if(selNode == null)
         return;
      PSRole addingRole = null;

      if (selNode.getUserObject() instanceof UIRole)
      {
          UIRole addingRoleUI = (UIRole)selNode.getUserObject();
          addingRole = addingRoleUI.getRole();
      }
      //the tree is the role by member tree, root node name is 'Members'
      else if (selNode.getUserObject() instanceof UIRoleMember)
      {
         DefaultMutableTreeNode roleNode = null;

         //if this is the leaf the role node will be a parent node
         if (selNode.isLeaf())
            roleNode = (DefaultMutableTreeNode)selNode.getParent();
         else
            roleNode = (DefaultMutableTreeNode)selNode.getChildAt(0);

         if (roleNode.getUserObject() instanceof UIRole)
         {
            UIRole roleUI = (UIRole)roleNode.getUserObject();
            addingRole = roleUI.getRole();
         }
      }
      ModifyMemberListDialog memberDialog =
         new ModifyMemberListDialog(AppletMainDialog.getMainframe(), addingRole,
            m_config, m_roleConfig, getRoleMembers(addingRole));

      if(m_catalogParameters != null)
         memberDialog.setCatalogParams(m_catalogParameters);
      memberDialog.setVisible(true);

      /*Now get the cataloging properties and store it for the next time the
      ModifyMemberList dialog is accessed.*/
      m_catalogParameters = memberDialog.getCatalogParameters();

      if(memberDialog.isModified())
      {
         Vector roleMembers = (Vector)memberDialog.getRoleMembers();

         if(roleMembers != null )
            addRemoveMembers(roleMembers, addingRole);

         rebuildTree();
         mb_needSave = true;

         //if the view is Members by Role set the role to selected mode
         if(view.equals(ms_res.getString("cbRoles")))
            setSelected(addingRole);
      }
      m_tree.requestFocus();
   }

   /**
    * Adds or removes the members from the tree
    * @param roleMembers a modified list of the role members, assumed not to
    * be code<null>/code.
    * @param addingRole a role being modifed, assumed not code<null>/code
    */
   private void addRemoveMembers(List roleMembers, PSRole addingRole)
   {
      for (int i = 0; i < roleMembers.size(); i++)
      {
         PSSubject tempSubj = ((UIRoleMember)roleMembers.get(i)).getSubject();

         if(!addingRole.containsCorrespondingSubject(tempSubj))
         {
            for(int m = 0; m < m_roles.size(); m++)
            {
               PSRole role = (PSRole)m_roles.get(m);
               if(role.getName().equals(addingRole.getName()))
               {
                  if(!role.containsCorrespondingSubject(tempSubj))
                     role.getSubjects().add(tempSubj);
               }
            }
         }
      }

      for (int l = addingRole.getSubjects().size()-1; l >=0; l--)
      {
         boolean match = false;
         PSSubject subject = (PSSubject)addingRole.getSubjects().get(l);
         for (int m = 0; m < roleMembers.size(); m++)
         {
            PSSubject remSubject =
               ((UIRoleMember)roleMembers.get(m)).getSubject();

            if (subject.isMatch(remSubject))
            {
               match = true;
               break;
            }
         }
         if (!match)
         {
            for(int m = 0; m < m_roles.size(); m++)
            {
               PSRole role = (PSRole)m_roles.get(m);
               if(role.getName().equals(addingRole.getName()))
                  removeSubjectFromRole(role,subject);

            }
         }
      }
   }

   /**
    * Adds and remove attributes comparing two attribute lists, the original
    * atrribute list with a new attribute list.
    * @param oldAttributes an original attribute list, never <code>null</code>,
    * but it can be empty.
    * @param newAttributes a new attribute list, never <code>null</code>,
    * but it can be empty.
    */
   public static void addRemoveAttributes(PSAttributeList oldAttributes,
      PSAttributeList newAttributes)
   {
      Iterator newIter = newAttributes.iterator();

      while(newIter.hasNext())
      {
         PSAttribute newAttribute = (PSAttribute)newIter.next();

         oldAttributes.setAttribute(newAttribute.getName(),
            newAttribute.getValues());
      }

      for(int i = oldAttributes.size()-1; i >=0; i--)
      {
         PSAttribute oldAttribute = (PSAttribute)oldAttributes.get(i);
         if(newAttributes.getAttribute(oldAttribute.getName()) == null)
            oldAttributes.remove(oldAttribute);

      }
   }

   /**
    * Performs the action fired by the &quot;Add Roles&quot; button.
    * Adds a new role.
    */
   private void onAddRole()
   {
      String view = (String)m_viewCombo.getSelectedItem();

      AdminRoleDialog roleDialog = new AdminRoleDialog(
         AppletMainDialog.getMainframe(), null, m_roles);

      roleDialog.setVisible(true);

      if(roleDialog.isModified())
      {
         PSRole role = roleDialog.getRole();
         if(role == null)
            throw new IllegalArgumentException(("can't add role, role is null"));

         m_roles.add(role);
         mb_needSave = true;

         DefaultMutableTreeNode root =
            (DefaultMutableTreeNode )m_treeModel.getRoot();

         DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
            new UIRole (role));

         if(root.getUserObject().toString().equals(ms_res.getString(ROLES)))
            insertNode(root, roleNode );
         else
            m_viewCombo.setSelectedItem(ms_res.getString("cbRoles"));

         setSelected(role);
      }
   }

   /**
    * Sets the node of the role passed in as a selected node
    * @param role a role to set to a selected mode, assumed not to be
    * <code>null</code>
    */
   private void setSelected(PSRole role)
   {
      DefaultMutableTreeNode root =
         (DefaultMutableTreeNode )m_treeModel.getRoot();
      Enumeration e = root.children();
      while (e.hasMoreElements())
      {
         DefaultMutableTreeNode node =
            (DefaultMutableTreeNode)e.nextElement();
         UIRole uiRole = (UIRole)node.getUserObject();
         if (uiRole.getRole().getName().equals(role.getName()))
         {
            TreePath path =  new TreePath (node.getPath());
            m_tree.setSelectionPath(path);
            m_tree.expandPath(path);
            m_tree.requestFocus();
         }
      }
   }

   /**
    * Gets cached role property name values. If they are not cached, they are
    * cataloged and cached.
    *
    * @return map of role property name values, never <code>null</code>.
    **/
   public static Map getRolePropertyNameValues()
   {
      if(ms_rolePropertyNameValues == null)
         ms_rolePropertyNameValues = catalogRolePropertyNameValues(true);
      return  ms_rolePropertyNameValues;
   }

   /**
    * Gets cached subject property name values. If they are not cached, they are
    * cataloged and cached.
    *
    * @return map of subject property name values, never <code>null</code>.
    **/
   public static Map getSubjectPropertyNameValues()
   {
      if(ms_subjectPropertyNameValues == null)
         ms_subjectPropertyNameValues = catalogSubjectPropertyNameValues(true);
      return  ms_subjectPropertyNameValues;
   }

   /**
    * Catalogs role property name values and removes invalid property names.
    *
    * @param showErrorMsg flag to show error messages to user if error happens
    * while cataloging.
    **/
   public static Map catalogRolePropertyNameValues(boolean showErrorMsg)
   {
      ms_rolePropertyNameValues = AdminPropertyCataloger.catalogProperties(
         ROLE_LOOKUP_TYPE, ms_serverRoot, showErrorMsg);
      removeInvalidPropertyNames(ms_rolePropertyNameValues, showErrorMsg);
      return ms_rolePropertyNameValues;
   }

   /**
    * Catalogs subject property name values and removes invalid property names.
    *
    * @param showErrorMsg flag to show error messages to user if error happens
    * while cataloging.
    **/
   public static Map catalogSubjectPropertyNameValues(boolean showErrorMsg)
   {
      ms_subjectPropertyNameValues = AdminPropertyCataloger.catalogProperties(
         SUBJECT_LOOKUP_TYPE, ms_serverRoot, showErrorMsg);
      removeInvalidPropertyNames(ms_subjectPropertyNameValues, showErrorMsg);
      return ms_subjectPropertyNameValues;
   }
   /**
    * Catalogs for values and builds the list if the property was provided with
    * url for values. Ignores the url if the property has values already.
    *
    * @param catalogedProperties map of property name and values that the list
    * will be created on, can not be <code>null</code>. The key of the map will
    * be a <code>String</code> of the property name and the values of the map
    * are list of <code>AdminPropertyValues AdminProperties</code>.
    * @param type of the catalogAdminProperty, can not be <code>null</code>,
    * can not be empty.
    * @param the name of object that this property belongs to, can be <code>
    * null</code>.
    * @return List of <code>AdminNameValue</code>, never <code>null</code>,
    * can be empty.
    */
   public static List catalogAdminProperty(Map catalogedProperties,
      String objectName, String type)
   {
      if( catalogedProperties == null)
         throw new IllegalArgumentException("catalogedProperties map can not be"
         + "null");

      if( type == null || type.length() < 1)
         throw new IllegalArgumentException("type can not be null or empty");

      List valueList = new ArrayList();
      Iterator iter = catalogedProperties.entrySet().iterator();
      while(iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         String category =  (String)entry.getKey();

         if(category.equalsIgnoreCase(ATTRIBUTE_NAME))
         {
            ArrayList attrNames = (ArrayList)entry.getValue();
            if( attrNames != null )
            {
               for(int i=0; i < attrNames.size(); i++)
               {
                  String name = ((AdminPropertyValues)
                     attrNames.get(i)).getPropertyName();
                  String catalogUrl = ((AdminPropertyValues)
                     attrNames.get(i)).getCatalogUrl();
                  boolean limitToList = ((AdminPropertyValues)
                     attrNames.get(i)).getLimitToList();
                  List tmpvalueList = new ArrayList();
                  //Gets the values from the url provided
                  if(catalogUrl != null && catalogUrl.length() != 0 &&
                     !catalogedProperties.containsKey(name))
                  {
                     List tmpvalueList2 = AdminPropertyCataloger.catalogNameValue
                        (type,objectName,catalogUrl,name,ms_serverRoot);
                     valueList.add(new AdminNameValue(
                        name,limitToList,tmpvalueList2));
                  }
                  else
                  {
                     List tmpValueList2 = null;
                     List attrNames2 = (List)catalogedProperties.get( name );
                     if(attrNames2 != null)
                     {
                        tmpValueList2 = new ArrayList();
                        for(int j=0; j < attrNames2.size(); j++)
                        {
                           tmpValueList2.add(((AdminPropertyValues)
                              attrNames2.get(j)).getPropertyName());
                        }
                     }
                     valueList.add(new
                           AdminNameValue(name,limitToList,tmpValueList2));
                  }
               }
            }
         }
      }
      return valueList;
   }


   /**
    * Removes invalid property names from property name values. It shows message
    * to user while removing if the <code>showMsg</code> flag is set to
    * <code>true</code>. By default it logs a message to console.
    *
    * @param  map the map of property name values, assumed not <code>null</code>
    * @param  showMsg flag to show messages to user while removing invalid
    * property names.
    **/
   private static void removeInvalidPropertyNames(Map map, boolean showMsg)
   {
      List propertyNames = null;

      Iterator iter = map.entrySet().iterator();
      while(iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         if( ((String)entry.getKey()).equalsIgnoreCase(ATTRIBUTE_NAME) )
         {
            propertyNames = (ArrayList)entry.getValue();
            break;
         }
      }

      if(propertyNames != null)
      {
         Iterator propIter = propertyNames.iterator();
         while(propIter.hasNext())
         {
            String name =
               ((AdminPropertyValues)propIter.next()).getPropertyName();
            if(name.indexOf(' ') != -1)
            {
               System.out.println("Ignoring invalid property " + name);
               if(showMsg)
               {
                  JOptionPane.showMessageDialog(null,
                     ms_res.getString("ignoreProperty") + " '" + name + "'");
               }
               propIter.remove();
            }
         }
      }

   }

   /**
    * Performs the action fired by the &quot;Delete&quot; button.
    * If the selected node is a role, a message box pops-up for confirmation
    * as it deletes role and role members, otherwise deletes the member from the
    * role.
    */
   private void onDelete()
   {
      int selOption = JOptionPane.showConfirmDialog(
         this, ms_res.getString("deleteRoleMsg"),
         ms_res.getString("confirmDelete"), JOptionPane.YES_NO_OPTION,
         JOptionPane.WARNING_MESSAGE);

      if (selOption == JOptionPane.YES_OPTION)
      {
         deleteFromTree();
         //clear the table from the old data
         clearTable();
         mb_needSave = true;
         m_tree.setSelectionRow(0);
         m_tree.requestFocus();
      }
   }

   /**
    * Deletes the selected nodes from the tree.  If the view is Members by Role
    * and member is being deleted, that member will be removed from the role
    * under which it is categorized. If the role is being deleted the entire role
    * will be deleted.
    *
    * If the view is Roles by Member and a role is selected when delete button
    * is hit we do not delete the selected role, we are removing the member from
    * that role. If a member is selected we are removing that member from all
    * the roles to which it belongs.  Multi-selection is supported.
    */
    private void deleteFromTree()
    {
      DefaultMutableTreeNode root =
         (DefaultMutableTreeNode)m_treeModel.getRoot();

      TreePath [] selPaths = m_tree.getSelectionPaths();
      for (int i = 0; i < selPaths.length; i ++)
      {
         TreePath selPath = selPaths[i];
         DefaultMutableTreeNode selNode = (DefaultMutableTreeNode)
            selPath.getLastPathComponent();

         if (selNode.getUserObject() instanceof UIRole)
         {
            UIRole selRole = (UIRole)selNode.getUserObject();
            PSRole role = selRole.getRole();

            if(root.getUserObject().toString().equals(
               ms_res.getString(ROLES)))
            {
               m_roles.remove(role);
            }
            else
            {
               DefaultMutableTreeNode parentNode =
                  (DefaultMutableTreeNode)selNode.getParent();

               UIRoleMember member = (UIRoleMember)parentNode.getUserObject();
               removeSubjectFromRole(role, member.getSubject());
            }
         }
         else if (selNode.getUserObject() instanceof UIRoleMember)
         {

            PSSubject subject =
               ((UIRoleMember)selNode.getUserObject()).getSubject();

            if(root.getUserObject().toString().equals(
               ms_res.getString(ROLES)))
            {
               DefaultMutableTreeNode parentNode =
                  (DefaultMutableTreeNode)selNode.getParent();
               PSRole parentRole =
                  ((UIRole)parentNode.getUserObject()).getRole();
               parentRole.getSubjects().remove(subject);
            }
            else
            {
              Enumeration e = selNode.children();
              while (e.hasMoreElements())
              {

                  DefaultMutableTreeNode roleNode =
                     (DefaultMutableTreeNode)e.nextElement();
                  UIRole role =(UIRole) roleNode.getUserObject();
                  removeSubjectFromRole(role.getRole(), subject);
              }
            }
         }
         m_treeModel.removeNodeFromParent(selNode);
      }
    }

   /**
    * Performs the action fired by the &quot;Edit&quot; button.
    * If the selected node is a role, Edit Role Properties dialog will
    * pop-up, else if it is a member, Edit member properties dialog will
    * pop-up and updates properties of role/member respectively.
    */
   private void onEdit()
   {
      DefaultMutableTreeNode root =
         (DefaultMutableTreeNode)m_treeModel.getRoot();

      DefaultMutableTreeNode selNode =
         (DefaultMutableTreeNode)m_tree.getLastSelectedPathComponent();

      if (selNode.getUserObject() instanceof UIRole)
      {
         UIRole uiRole = (UIRole)selNode.getUserObject();

         AdminRoleDialog roleDialog = new AdminRoleDialog(
            AppletMainDialog.getMainframe(), uiRole.getRole(), null);

         roleDialog.setVisible(true);

         if(roleDialog.isModified())
         {
            updateTable(selNode);
            mb_needSave = true;
         }
      }
      else if (selNode.getUserObject() instanceof UIRoleMember)
      {
         UIRoleMember member = (UIRoleMember)selNode.getUserObject();
         HashMap memberRoleSubjects = (HashMap)getMemberRoleSubjects(member);

         AdminMemberDialog memberDialog = new AdminMemberDialog(
            AppletMainDialog.getMainframe(), member, m_config,
            memberRoleSubjects);

         memberDialog.setVisible(true);
         if(memberDialog.isModified())
         {
            AdminRoleMemberData data = memberDialog.getData();

            //update all role subjects of this member
            Iterator iterator = memberRoleSubjects.keySet().iterator();
            while(iterator.hasNext())
            {
               String roleName = (String)iterator.next();
               PSSubject subject = (PSSubject)memberRoleSubjects.get(roleName);

               addRemoveAttributes(subject.getAttributes(),
                  data.getAttributes(roleName));

               member.setRoleSpecAttributes(roleName,
                  data.getAttributes(roleName));
            }
            member.setMemberAttributes(data.getGlobalAttributes());
            updateTable(selNode);
            mb_needSave = true;
         }
      }
      m_tree.requestFocus();
   }

   /**
    * Removes a subject from a role
    * @param role a role to remove the subject from, assumed not to be
    * <code>null</code>
    * @param subject a subject to be removed, assumed not to be
    * <code>null</code>
    */
   private void removeSubjectFromRole (PSRole role, PSSubject subject)
   {
      PSDatabaseComponentCollection subjColl = role.getSubjects();
      for (int k = 0; k < subjColl.size(); k++)
      {
         PSSubject tempSubj = (PSSubject)subjColl.get(k);
         if(tempSubj.isMatch(subject))
            subjColl.remove(tempSubj);
      }
   }

   /**
    * Gets all role subjects of the member.
    *
    * @param member role member, assumed not <code>null</code>.
    *
    * @return return map of role name and role subjects of the member,
    * never <code>null</code> or empty.
    **/
   private Map getMemberRoleSubjects(UIRoleMember member)
   {
      HashMap memberRoleSubjects = new HashMap();

      for(int i=0; i < m_roles.size(); i++)
      {
         PSRole role = (PSRole)m_roles.get(i);
         PSSubject roleSubj = null;


         for(int j=0; j < role.getSubjects().size(); j++)
         {
            PSSubject temp =(PSSubject)role.getSubjects().get(j);
            if (temp.isMatch(member.getSubject()))
            {
              roleSubj = temp;
              break;
            }
         }

         if(roleSubj != null)
            memberRoleSubjects.put(role.getName(), roleSubj);
      }
      return memberRoleSubjects;
   }

   /**
    * Removes the nodes from the tree for which the root node is passed in.
    * @param root a rootNode of the tree to be cleared from the old nodes,
    * assumed not to be <CODE>null</CODE>.
    */
   private void clearTree(DefaultMutableTreeNode root)
   {
      for (int i = m_treeModel.getChildCount(root)-1; i >= 0; i--)
      {
         m_treeModel.removeNodeFromParent(
            (DefaultMutableTreeNode)m_treeModel.getChild(root, 0));
      }
   }

   /**
    * Clears the table from the old data.
    */
   private void clearTable()
   {
      Vector tableData = m_tableModel.getDataVector();
      if (tableData != null)
      {
         for (int i = tableData.size(); i > 0; i--)
            m_tableModel.removeRow(0);
      }
   }

   /**
    * An utility method that adds empty rows to the table.
    * @param row the number of the empty rows to be inserted, can be
    * <CODE>null</CODE>
    */
   private void addEmptyRows(int row)
   {
      for (int i = 0; i < row; i++)
      {
         Object [] emptyObj = {""};
         m_tableModel.addRow(emptyObj);
      }
   }

   /**
    * Updates the title of the attributes table depending on the view, which can
    * be Members by Role and Roles by Member and on the selected node that can
    * be a role or a member
    *
    * @param a selected node, assumed not to be <code>null</code>
    */

    /* TODO:make label a member variable, get rid of the panel that label is
     * a component of
     *
     */
    private void updateLabel (DefaultMutableTreeNode node)
    {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
      for (int i = 0; i < m_cmsPanel.getComponentCount(); i ++)
      {
         if (m_cmsPanel.getComponent(i) instanceof JPanel)
         {
            JPanel panel =  (JPanel)m_cmsPanel.getComponent(i);
            for (int k = 0; k < panel.getComponentCount(); k++)
            {
               if (!(panel.getComponent(k) instanceof JLabel))
               {
                  panel.remove(panel.getComponent(k));
                  break;
               }
               if(panel.getComponent(k) instanceof JLabel)
               {
                  JLabel lab = (JLabel)panel.getComponent(k);
                  if (node.isRoot())
                  {
                     lab.setText(ms_res.getString("cmsProp"));
                     panel.add(Box.createHorizontalStrut(160));
                  }

                  if(root.getUserObject().toString().equals(ROLES))
                  {
                     if(node.getUserObject() instanceof UIRole)
                     {
                        lab.setText(ms_res.getString("roleAttr"));
                        panel.add(Box.createHorizontalStrut(130));
                     }
                     else if(node.getUserObject() instanceof UIRoleMember)
                     {
                        lab.setText(ms_res.getString("subRole"));
                        panel.add(Box.createHorizontalStrut(23));
                     }
                  }
                  else
                  {
                     if (node.getUserObject() instanceof UIRole)
                     {
                        lab.setText(ms_res.getString("subRolAtt"));
                        panel.add(Box.createHorizontalStrut(87));
                     }
                     else if (node.getUserObject() instanceof UIRoleMember)
                     {
                        lab.setText(ms_res.getString("globAttr"));
                        panel.add(Box.createHorizontalStrut(77));
                     }
                  }
               }
            }
         }
      }
    }

   /**
    * Updates the CMS table attributes for the selected node
    * When a role node is selected the role attributes will be displayed and
    * when a member node is selected the attributes of that member will be
    * displayed.  There are two types of the members' attributes one type is
    * the global and the other one is the role specific.  Global attributes
    * are coming from the server configuration and the role specific are
    * coming from the role that this member belongs to.
    * @param node a selected node never <CODE>null</CODE>    */

   private void updateTable (DefaultMutableTreeNode node)
   {
      clearTable();
      m_valueText.setText("");
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
      int rowCount = 0;
      if (node.getUserObject() instanceof UIRole)
      {
         UIRole roleUI = (UIRole)node.getUserObject();
         PSRole role = roleUI.getRole();
         String roleName = role.getName();

         if (!node.isLeaf()|| ((String)root.getUserObject()).equals(
            ms_res.getString(ROLES)))
         {
            PSAttributeList attrib = role.getAttributes();
            rowCount = displayTableData(attrib);
         }
         else
         {

            /*Here we want to display subject's role specific attributes*/
            DefaultMutableTreeNode parentNode =
               (DefaultMutableTreeNode)node.getParent();
               if (parentNode.getUserObject() instanceof UIRoleMember)
               {
                  UIRoleMember member = (UIRoleMember)parentNode.getUserObject();
                  PSAttributeList roleMemberAttribs =
                     member.getRoleSpecAttributes(role.getName());

                  rowCount += displayTableData(roleMemberAttribs);
               }
         }
      }
      else if (node.getUserObject() instanceof UIRoleMember)
      {
         UIRoleMember member = (UIRoleMember)node.getUserObject();
         //get subject global attributes
         PSAttributeList attribList = member.getMemberAttributes();
         //get subject role specific attributes
         PSAttributeList roleMemberAttribs = member.getMemberRoleAttributes();

         if (node.isLeaf())
         {
            //dispay subject global and role specific attributes
            rowCount = displayTableData(attribList);
            rowCount += displayTableData(roleMemberAttribs);
         }
         else
            //display only subject global role specific attributes
            rowCount = displayTableData(attribList);

      }
      if(NUM_ROWS >= (NUM_ROWS - rowCount))
         addEmptyRows(NUM_ROWS-rowCount);
   }

   /**
    * Populates the CMS table with the attributes depending on the selected
    * node.
    * @param attrib Map of the attributes, assumed not to be <CODE>null</CODE>
    * might be empty.
    * @return Number of the inserted rows in the table.
    */
   private int displayTableData (PSAttributeList attrib)
   {
      int rowCount = 0;
      Iterator iter = attrib.iterator();


      while (iter.hasNext())
      {
         Vector attribs = new Vector();
         PSAttribute attribute = (PSAttribute)iter.next();
         String attribName = attribute.getName();
         List attribValues = attribute.getValues();
         String values = new String ();

         for (int i = 0; i < attribValues.size(); i++)
         {
            String val = (String)attribValues.get(i);
            if (i != (attribValues.size()-1))
               values += val + ";";
            else
              values += val;
         }

         attribs.add(attribName);
         attribs.add(values);

         m_tableModel.addRow(attribs);
         if (rowCount == NUM_ROWS )
            addEmptyRows(1);
         rowCount++;
      }
      return rowCount;
   }

   /**
    * Gets all the members of the role.
    * @param role Role to get the members for, assumed not to be
    * <CODE>null</CODE>.
    * @return A collection of the members of the role, never <CODE>null</CODE>,
    * may be empty.
    */
    private Collection getRoleMembers(PSRole role)
    {
      Vector members = new Vector();
      for (int i = 0; i < role.getSubjects().size(); i ++)
      {
         UIRoleMember member = new UIRoleMember((
            PSRelativeSubject)role.getSubjects().get(i), m_roleConfig);
         members.add(member);
      }
      return members;
    }

   /**
    * Builds the tree which can be the role tree or the members tree depending
    * on the root node passed in.  When nodes are inserted for the Roles tree
    * the checking for the existing node is not done since every role has the
    * unique name.
    * @param root Root node to build this tree for, assumed not to be
    * <CODE>null</CODE>.
    * @param list List of all the roles, assumed not to be <CODE>null</CODE>.
    */
   private void buildTree(DefaultMutableTreeNode root,
      PSDatabaseComponentCollection list)
   {
      if(list == null)
         return;

      for (int i = 0; i < list.size(); i ++)
      {
         PSRole role = (PSRole)list.get(i);
         UIRole roleUI = new UIRole (role);
         DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(roleUI);

         if(((String)root.getUserObject()).equals(ms_res.getString(ROLES)))
            insertNode(root, roleNode);

         PSDatabaseComponentCollection roleSubjects = role.getSubjects();

         for (int k = 0; k < roleSubjects.size(); k ++)
         {
            DefaultMutableTreeNode memberNode = null;
            PSRelativeSubject roleSubj = (PSRelativeSubject)roleSubjects.get(k);
            UIRoleMember member = new UIRoleMember(roleSubj, m_roleConfig);

            if(((String)root.getUserObject()).equals(ms_res.getString(ROLES)))
            {
               memberNode = new DefaultMutableTreeNode (member);
               insertNode(roleNode, memberNode);
            }
            else
            {
               DefaultMutableTreeNode parent = getMatchingChild(root, member);

               if(parent == null)
               {
                  parent = new DefaultMutableTreeNode (member);
                  insertNode(root, parent);
               }

               UIRole uiRole = new UIRole (role);
               //now build the roleChild node
               DefaultMutableTreeNode roleChild =
                  new DefaultMutableTreeNode(uiRole);

               insertNode(parent, roleChild);

               ((UIRoleMember)parent.getUserObject()).setRoleSpecAttributes(
                  role.getName(), roleSubj.getAttributes());
            }
         }
      }
      m_tree.setSelectionRow(0);
      m_tree.requestFocus();
   }

   /**
    * Inserts a new node keeping alphabetical order.
    * @param parent a parent node at which child node will be inserted,
    * assumed not to be<code>null</code>
    * @param child a child node to be inserted at the parent node,
    * assumed not to be<code>null</code>
    */
   private void insertNode(DefaultMutableTreeNode parent,
      DefaultMutableTreeNode child)
   {
      int iCount = m_treeModel.getChildCount(parent);
      String childName = child.getUserObject().toString();

      Collator c = Collator.getInstance();
      c.setStrength(Collator.SECONDARY);
      int i = 0;
      if(iCount > 0)
      {
         for(i = 0; i < iCount; i++)
         {
            DefaultMutableTreeNode tempNode =
               (DefaultMutableTreeNode)parent.getChildAt(i);
            String tempNodeName = tempNode.getUserObject().toString();
            if (c.compare(childName, tempNodeName) < 0)
               break;
         }
      }
      if(i >= iCount)
         m_treeModel.insertNodeInto(child,parent,iCount);
      else
         m_treeModel.insertNodeInto(child, parent, i);
    }

   /**
    * Method to get child node of the parent which has user object same as
    * passed in object.
    *
    * @param parent the parent node, assumed not to be <code>null</code>.
    * @param obj the object which should be matched against child node's user
    * object, assumed not to be <code>null</code>.
    *
    * @return matching child node if found, otherwise <code>null</code>.
    **/
   private DefaultMutableTreeNode getMatchingChild(
      DefaultMutableTreeNode parent, Object obj)
   {
      int count = m_treeModel.getChildCount(parent);

      if(count == 0)
         return null;

      for(int i = 0; i < count; i++)
      {
         DefaultMutableTreeNode child =
            (DefaultMutableTreeNode)parent.getChildAt(i);

         //Make sure both objects are of same class instances
         if( obj.getClass().getName().equals(
            child.getUserObject().getClass().getName()) )
         {
            if(child.getUserObject().equals(obj))
               return child;
         }
      }
      return null;
   }

   /**
    * An inner class implementing TreeSelectionListener interface for handling
    * tree selection changes.
    */
   private class TreeSelection implements TreeSelectionListener
   {
      /**
       * Implementation to display properties of selected node
       * in a table, and disable or enable the buttons.
       * @see TreeSelectionListener#valueChanged valueChanged
       */
      public void valueChanged (TreeSelectionEvent event)
      {
         DefaultMutableTreeNode node = null;
         //if multi-selection gray out all buttons, but Delete
         if(m_tree.getSelectionCount() > 1)
         {
            clearTable();
            addEmptyRows(NUM_ROWS);

            m_memberButton.setEnabled(false);
            m_roleButton.setEnabled(false);
            m_editButton.setEnabled(false);
         }
         else
         {
            TreePath nodePath = event.getPath();
            //get the selected node
            node =
               (DefaultMutableTreeNode)m_tree.getLastSelectedPathComponent();
            if (node == null)
               return;

            m_valueText.setText("");

            m_roleButton.setEnabled(true);

            if (node.isRoot())
            {
               m_memberButton.setEnabled(false);
               m_deleteButton.setEnabled(false);
               m_editButton.setEnabled(false);
               //clear the table
               clearTable();
               addEmptyRows(NUM_ROWS);
               m_tree.expandPath(new TreePath(node.getPath()));
            }
            else
            {
               m_memberButton.setEnabled(true);
               m_deleteButton.setEnabled(true);
               m_editButton.setEnabled(true);
               /*update the table properties */
               updateTable(node);
            }
            updateLabel(node);
         }
      }
   }

   /**
    * Inner class to implement ListSelectionListener for handling table
    * selection changes.
    */
   private class TableSelectionListener implements ListSelectionListener
   {
      /**
       * Implements row selection in a table. Updates text area with data
       * in "Value" column.
       *
       * @see ListSelectionListener#valueChanged valueChanged
       */
      public void valueChanged (ListSelectionEvent e)
      {
         m_valueText.setText("");
         int row = m_table.getSelectedRow();
         if (row >= 0)
         {
            /*if the selected row is the last row in the table add an empy row
            to the table*/
            if(row == m_table.getRowCount()-1)
            {
               Object [] obj = {""};
               m_tableModel.addRow(obj);
            }
            String text = (String)m_table.getValueAt(row, 1);
            if(text != null)
            {
               String separator = ";";
               text = PSStringOperation.replaceChar(
                  text, separator.charAt(0), '\n');
               m_valueText.setText(text);
            }
         }
      }
   }

   /**
    * An inner class to implement tree cell rendering.  If a member is a group
    * the icon should be a group user icon and if a member is a user the icon is
    * a user icon.  When a mouse is dragged over a member a tool tip should show
    * the security provided instance if any.
    */
   private class TreeCellRenderer extends DefaultTreeCellRenderer
   {
      /**
       * Overridden method to get customized renderer for displaying icon and
       * setting tool tip for each cell.
       *
       * @see #getTreeCellRendererComponent
       **/
      public Component getTreeCellRendererComponent(JTree tree, Object value,
         boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
      {
         super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
            row, hasFocus);

         DefaultMutableTreeNode root = (DefaultMutableTreeNode)
            m_treeModel.getRoot();
         String rootName = (String)root.getUserObject();
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

         if (node.getUserObject() instanceof UIRoleMember)
         {
            UIRoleMember roleMember = (UIRoleMember)node.getUserObject();
            if (roleMember.getType().equalsIgnoreCase("user"))
            {
               setIcon (new ImageIcon (
                  getClass().getResource(ms_res.getString("gif_user_member"))));
            }
            else if(roleMember.getType().equalsIgnoreCase("group"))
            {
               setIcon (new ImageIcon ( getClass().getResource(
                  ms_res.getString("gif_group_member"))));
            }
         }
         else
         {
            if (node.getUserObject() instanceof UIRole)
               setIcon (null);
            setToolTipText(null);
         }
         return this;
      }
   }

   /** The default number of column this panel table will use. */
   private static final int NUM_COLUMNS = 2;

   /** The default number of rows this panel table will use. */
   private static final int NUM_ROWS = 7;

   /**The default table model, gets initialized in <code>createTable</code>. */
   private DefaultTableModel m_tableModel = null;

   /**A CMS properties table, gets initialized in <code>createTable<code>. */
   private JTable m_table = null;

   /**The title of the attributes table*/
   private JPanel m_cmsPanel = null;

   /**
    * A combo box which will hold two possible tree views, gets initialized in
    * <code>createViewCombo</code>.
    */
   private PSComboBox m_viewCombo = null;

   /**
    * Buttons to modify roles and members, gets initialized in
    * <code>createCommandPanel</code>.
    **/
   private UTFixedButton m_editButton, m_memberButton, m_roleButton;

   /**
    * Button to delete roles and members, gets initialized in
    * <code>createCommandPanel</code>.
    **/
   private UTFixedButton m_deleteButton;

   /** Value text area, gets initialized in <code>createPropertiesPanel</code>.*/
   private JTextArea m_valueText = null;

   /** The tree view, gets initialized in <code>createTreePanel</code>. */
   private  JTree m_tree = null;

   /**
    * The default tree model, gets initialized in <code>createTreePanel</code>.
    */
   private DefaultTreeModel m_treeModel = null;

   /** The server configuration, gets initialized in the constructor. */
   private ServerConfiguration m_config;

   /** The role configuration, gets initialized in the constructor*/
   private PSRoleConfiguration m_roleConfig;

   /** The collection of the roles, gets initialized in the constructor. */
   private PSDatabaseComponentCollection m_roles;

   /** A variable that holds cataloging properties, gets initialized in
    * <code>onAddMember()</code>
    */
   private CatalogParameters m_catalogParameters = null;

   /**
    * Map to hold cataloged property name and values for 'role' lookup type.
    * Initially set to <code>null</code> and set to cataloged map in
    * <code>initData</code>. Used for displaying default/allowed property
    * names or values in <code>AdminRoleDialog</code>.
    **/
   private static Map ms_rolePropertyNameValues = null;

   /**
    * Map to hold cataloged property name and values for 'subject' lookup type.
    * Initially set to <code>null</code> and set to cataloged map in
    * <code>initData</code>. Used for displaying default/allowed property
    * names or values in <code>AdminMemberDialog</code>.
    **/
   private static Map ms_subjectPropertyNameValues = null;

   /**A flag that indicates if the Roles tab has been accessed and needs to
    * save role configuration. If <code>true</code> tab has been accessed and
    * it needs to save role configuration, if <code>false</code> no need for
    * saving role configuration.
    */
    private boolean mb_needSave = false;

   /**
    * The collection of the mapped pairs where a key is the role object and
    * the value is a collection of the roles members, gets initialized in the
    * <code>mapMembers</code>.
    */
   Collection m_allMembers;
   static ResourceBundle ms_res = null;
   static
   {
      try
      {
         ms_res = ResourceBundle.getBundle(
         "com.percussion.E2Designer.admin.PSServerAdminResources",
         Locale.getDefault() );
      }
       catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
   }

   /**
    * Constant to indicate user member type.
    **/
   public static   final String USER = "user" ;

   /**
    * Constant to indicate group member type.
    **/
   public static   final String GROUP = "group";

   /**
    * Constant to indicate 'role' lookup type for cataloging properties.
    **/
   public static final String ROLE_LOOKUP_TYPE = "role";

   /**
    * Constant to indicate 'subject' lookup type for cataloging properties.
    **/
   public static final String SUBJECT_LOOKUP_TYPE = "subject";

   /**
    * The server's request root, gets initialized in <code>initData</code> and
    * never <code>null</code> after that.
    **/
   public static String ms_serverRoot = null;

   /**
    * Constant to indicate that the name is an attribute name whose category
    * is equal to this.
    **/
   public final static String ATTRIBUTE_NAME   = "SYS_ATTRIBUTENAME";

   /**
    * Constant for the resource key of the "Roles" root tree node.
    */
   private static final String ROLES = "rolesNode";
}

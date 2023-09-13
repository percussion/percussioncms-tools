/******************************************************************************
 *
 * [ PSDeploymentMenuHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSAboutDialog;
import com.percussion.guitools.PSResources;
import com.percussion.guitools.ResourceHelper;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.util.PSFormatVersion;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

/**
 * The handler that creates and handles the actions of the main menu bar of the
 * main frame and the pop-up menu of the servers tree in the browser pane of the
 * frame.
 */
public class PSDeploymentMenuHandler implements MenuListener, ActionListener
{
   /**
    * Constructs the menu handler with supplied parameters. Creates the main
    * menu bar for the frame and pop-up menu for the tree.
    *
    * @param frame the main frame of the application to which the main menu is
    * set, may not be <code>null</code>.
    * @param tree the tree on which the main menu actions are dependent and for
    * which the pop-up menu need to be set, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public PSDeploymentMenuHandler(PSMainFrame frame, PSServersTree tree)
   {
      if(frame == null)
         throw new IllegalArgumentException("frame may not be null.");
      if(tree == null)
         throw new IllegalArgumentException(" may not be null.");

      m_frame = frame;
      m_res = PSDeploymentClient.getResources();
      m_frame.setJMenuBar(createMainMenu());

      m_tree = tree;
      m_tree.addMouseListener(new TreeMenuHandler(this));
   }

   /**
    * Creates main menu bar of the main frame. Sets action command on each
    * menu and its menu items.
    *
    * @return the menu bar, never <code>null</code>.
    */
   private JMenuBar createMainMenu()
   {
      JMenuBar menuBar = new JMenuBar();

      //Action Menu items
      String menuStr = MENU_ACTION;
      JMenu menuAction = createMenu( m_res.getString(menuStr),
         ResourceHelper.getMnemonic( m_res, menuStr ), menuStr,
         this);
      menuBar.add(menuAction);

      menuStr = MENU_ACTION_REGISTER;
      JMenuItem menuActionReg = createMenuItem(menuStr, this);
      menuAction.add(menuActionReg);

      menuStr = MENU_ACTION_CREATE_ARCHIVE;
      JMenuItem menuActionCreate = createMenuItem(menuStr, this);
      menuAction.add(menuActionCreate);

      menuStr = MENU_ACTION_INSTALL_ARCHIVE;
      JMenuItem menuActionInstall = createMenuItem(menuStr, this);
      menuAction.add(menuActionInstall);

      menuAction.addSeparator();

      menuStr = MENU_ACTION_EXIT;
      JMenuItem menuActionExit = createMenuItem(menuStr, this);
      menuAction.add(menuActionExit);

      //Edit Menu items
      menuStr = MENU_EDIT;
      JMenu menuEdit = createMenu( m_res.getString(menuStr),
         ResourceHelper.getMnemonic( m_res, menuStr ), menuStr, this);
      menuBar.add(menuEdit);

      menuStr = MENU_EDIT_REGISTRATION;
      JMenuItem menuEditReg = createMenuItem(menuStr, this);
      menuEdit.add(menuEditReg);

      menuStr = MENU_EDIT_POLICY_SETTINGS;
      JMenuItem menuEditPolicySettings = createMenuItem(menuStr, this);
      menuEdit.add(menuEditPolicySettings);

      menuStr = MENU_EDIT_ID_TYPES;
      JMenuItem menuEditIDTypes = createMenuItem(menuStr, this);
      menuEdit.add(menuEditIDTypes);

      menuStr = MENU_EDIT_TRANSFORMS;
      JMenuItem menuEditTransforms = createMenuItem(menuStr, this);
      menuEdit.add(menuEditTransforms);

      //Help Menu items
      menuStr = MENU_HELP;
      JMenu menuHelp = createMenu( m_res.getString(menuStr),
         ResourceHelper.getMnemonic( m_res, menuStr ), null, null );
      menuBar.add( menuHelp );

      menuStr = MENU_HELP_ABOUT;
      JMenuItem menuHelpAbout = createMenuItem(menuStr, this);
      menuHelp.add(menuHelpAbout);

      menuStr = MENU_MSM_HELP;
      JMenuItem menuMultiServerMgrHelp = createMenuItem(menuStr, this);
      menuHelp.add(menuMultiServerMgrHelp);

      return menuBar;
   }

   /**
    * Creates the top-level menu in the menu bar based on the supplied
    * parameters.
    *
    * @param label the label to display for the menu, assumed not to be <code>
    * null</code> or empty.
    * @param mnemonic the character that is set to access menu items by using
    * ALT key combos, ignored if it is '0'.
    * @param actionCommand the action command to set, ignored if <code>
    * null</code> or empty.
    * @param listener the listener to this menu action, ignored if <code>
    * null</code>
    *
    * @return the menu, never <code>null</code>
    */
   private JMenu createMenu(String label, char mnemonic,
      String actionCommand, MenuListener listener)
   {
      JMenu menu = new JMenu( label );

      menu.setMnemonic( mnemonic );
      if(actionCommand != null && actionCommand.trim().length() > 0)
         menu.setActionCommand(actionCommand);
      if(listener != null)
         menu.addMenuListener(listener);

      return menu;
   }

   /**
    * Gets the label, mnemonic, accelerator, tool tip, icon values for this menu
    * item from the resources for the supplied resource string and creates the
    * menu item. Uses resource string key as the action command and its name.
    *
    * @param resourceStr the resource string to get the values from resources,
    * assumed not to be <code>null</code> or empty and the resource string
    * exists in the main resource bundle.
    * @param listener the action listener to this menu item, assumed not to be
    * <code>null</code>
    *
    * @return the menu item, never <code>null</code>
    */
   private JMenuItem createMenuItem(String resourceStr, ActionListener listener)
   {
      return createMenuItem(
         m_res.getString(resourceStr),
         ResourceHelper.getMnemonic( m_res, resourceStr ),
         ResourceHelper.getAccelKey( m_res, resourceStr ),
         ResourceHelper.getToolTipText( m_res, resourceStr ),
         null,
         resourceStr,
         resourceStr,
         listener);
   }

   /**
    * Creates the menu items with the specified parameters.
    *
    * @param label the label to be used for menu item, may not be <code>
    * null</code> or empty.
    * @param mnemonic the character that is set to access menu items by using
    * ALT key combos, ignored if it is '0'.
    * @param accelKey keystroke describing the accelerator key for this menu,
    * ignored if it is <code>null</code>
    * @param toolTip the tool tip to be set on menu item, ignored if it is
    * <code>null</code> or empty.
    * @param icon The icon to set, ignored if it is <code>null</code>
    * @param name the name of the component to recognize it, ignored if it is
    * <code>null</code> or empty.
    * @param actionCommand the action command to set, ignored if it is <code>
    * null</code> or empty.
    * @param listener the listener to this menu action, ignored if it is <code>
    * null</code>
    *
    * @return the menu item, never <code>null</code>
    *
    * @throws IllegalArgumentException if label is <code>null</code> or empty.
    */
   public static JMenuItem createMenuItem(String label, char mnemonic,
      KeyStroke accelKey, String toolTip, ImageIcon icon, String name,
      String actionCommand, ActionListener listener)
   {
      if(label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty.");

      JMenuItem menuItem = new JMenuItem( label );
      if(0 != mnemonic)
         menuItem.setMnemonic(mnemonic);

      if(null != accelKey)
         menuItem.setAccelerator( accelKey );

      if (null != toolTip && toolTip.length() > 0)
         menuItem.setToolTipText( toolTip );

      if(null != icon)
         menuItem.setIcon(icon);

      if (null != name && name.length() > 0)
         menuItem.setName(name);

      if (null != actionCommand && actionCommand.length() > 0)
         menuItem.setActionCommand(actionCommand);

      if(null != listener)
         menuItem.addActionListener(listener);

      return menuItem;
   }

   /**
    * The action method for menu selected action. Checks the action command of
    * the item and acts depending on it. Ignores if the source of this event is
    * not an instance of <code>JMenu</code>.
    *
    * @param e the menu event generated when a menu is clicked, assumed not to
    * be <code>null</code> as Swing model calls this method with an event when
    * a menu selected action occurs.
    */
   public void menuSelected(MenuEvent e)
   {
      Object source = e.getSource();

      if(source instanceof JMenu)
      {
         JMenu menu = (JMenu)source;
         String actionCommand = menu.getActionCommand();

         if(MENU_ACTION.equals(actionCommand))
            onAction(source);
         else if(MENU_EDIT.equals(actionCommand))
            onEdit(source);
      }
   }

   //nothing to implement
   public void menuDeselected(MenuEvent e)
   {
   }

   //nothing to implement
   public void menuCanceled(MenuEvent e)
   {
   }

   /**
    * The action method for menu item actions. Checks the action command of the
    * item and acts depending on it. Ignores if the source of this event is not
    * an instance of <code>JMenuItem</code>.
    *
    * @param e the action event generated when a menu is clicked, assumed not to
    * be <code>null</code> as Swing model calls this method with an event when
    * a menu selected action occurs.
    */
   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if(source instanceof JMenuItem)
      {
         JMenuItem menuItem = (JMenuItem)source;
         String actionCommand = menuItem.getActionCommand();

         if(MENU_ACTION_REGISTER.equals(actionCommand))
            onActionRegister();
         else if(MENU_ACTION_CREATE_ARCHIVE.equals(actionCommand))
            onActionCreate();
         else if(MENU_ACTION_INSTALL_ARCHIVE.equals(actionCommand))
            onActionInstall();
         else if(MENU_ACTION_EXIT.equals(actionCommand))
            onActionExit();
         else if(MENU_EDIT_REGISTRATION.equals(actionCommand))
            onEditRegister();
         else if(MENU_EDIT_POLICY_SETTINGS.equals(actionCommand))
            onEditPolicySettings();
         else if(MENU_EDIT_ID_TYPES.equals(actionCommand))
            onEditIDTypes();
         else if(MENU_EDIT_TRANSFORMS.equals(actionCommand))
            onEditTransforms();
         else if(MENU_HELP_ABOUT.equals(actionCommand))
            onHelpAbout();
         else if(MENU_DELETE.equals(actionCommand))
            onDelete();
         else if(MENU_CONNECT.equals(actionCommand))
            onConnect();
         else if(MENU_DISCONNECT.equals(actionCommand))
            onDisconnect();
         else if(MENU_MSM_HELP.equals(actionCommand))
            onHelp();
      }
   }

   /**
    * Action method for 'Multi-Server Manager Help' under 'Help' menu.
    * Displays a dialog containing help documentation.
    */
   private void onHelp()
   {
      String id = "PSDeploymentMenuHandler";
      PSJavaHelp.launchHelp(id);
   }

   /**
    * Action method for <code>Action</code> menu selection. Disables the
    * <code>Create Archive</code> and <code>Install Archive</code> menu items if
    * there are no registered servers and vice-versa.
    *
    * @param source the 'Action' menu object, assumed not to be <code>null
    * </code> and an instance of <code>JMenu</code>.
    */
   private void onAction(Object source)
   {
      JPopupMenu popup = ((JMenu)source).getPopupMenu();
      MenuElement[] elements = popup.getSubElements();
      for(int i=0; i<elements.length; i++)
      {
         String name = elements[i].getComponent().getName();
         if(MENU_ACTION_CREATE_ARCHIVE.equals(name) ||
            MENU_ACTION_INSTALL_ARCHIVE.equals(name))
         {
            JMenuItem menuItem = (JMenuItem)elements[i].getComponent();
            menuItem.setEnabled(m_tree.doesServersExist());
         }
      }
   }

   /**
    * Action method for 'Register Server' menu. Displays <code>
    * PSServerRegistrationDialog</code> for registering a server.
    */
   private void onActionRegister()
   {
      PSServerRegistrationDialog dlg = new PSServerRegistrationDialog(m_frame,
         m_tree.getRegisteredServerNames(), m_frame.getConnectionHandler());
      dlg.setVisible(true);
   }

   /**
    * Action method for 'Create Archive' menu. Constructs the <code>
    * PSDeploymentExportWizardManager</code> with the list of registered servers
    * and runs the wizard manager to initiate the wizard.
    *
    * @throws IllegalStateException if there are no registered servers.
    */
   private void onActionCreate()
   {
      if(m_tree.doesServersExist())
      {
         PSDeploymentExportExecutionPlan exportPlan =
            new PSDeploymentExportExecutionPlan(m_frame,
            m_tree.getRegisteredServers());
         PSDeploymentExportWizardManager manager =
            new PSDeploymentExportWizardManager(m_frame, exportPlan);
         manager.runWizard();
      }
      else
         throw new IllegalStateException(
            "There are no registered servers to create the archive.");
   }

   /**
    * Action method for 'Install Archive' menu. Constructs the <code>
    * DeploymentImportWizardManager</code> with the list of registered servers
    * and runs the wizard manager to initiate the wizard.
    *
    * @throws IllegalStateException if there are no registered servers.
    */
   private void onActionInstall()
   {
      if(m_tree.doesServersExist())
      {
         int option = JOptionPane.showConfirmDialog(
            PSDeploymentClient.getFrame(),
            ErrorDialogs.cropErrorMessage(
            PSDeploymentClient.getResources().getString("installWarning")),
            PSDeploymentClient.getResources().getString("warningTitle"),
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

         if(option == JOptionPane.NO_OPTION)
            return;

         PSDeploymentImportExecutionPlan importPlan =
            new PSDeploymentImportExecutionPlan(m_frame,
            m_tree.getRegisteredServers(), null);
         PSDeploymentImportWizardManager manager =
            new PSDeploymentImportWizardManager(m_frame, importPlan);
         manager.runWizard();
      }
      else
         throw new IllegalStateException(
            "There are no registered servers to install an archive.");
   }

   /**
    * Calls the {@link PSMainFrame#actionExit() exit} method on the main frame.
    * See the above link for the description of actions on exit.
    */
   private void onActionExit()
   {
      m_frame.actionExit();
   }

   /**
    * Action method for 'Edit' menu. This menu has menu items for editing all
    * properties of a registered server. So the menu items need to be disabled
    * if a server node is not selected in the servers tree that exists in the
    * browser(left) pane of the main frame.
    *
    * @param source the 'Edit' menu object, assumed not to be <code>null
    * </code> and an instance of <code>JMenu</code>
    */
   private void onEdit(Object source)
   {
      boolean enable = false;
      if(m_tree.getSelectedNodeObject() instanceof PSDeploymentServer)
         enable = true;

      JMenu menuEdit = (JMenu)source;
      JPopupMenu popup = menuEdit.getPopupMenu();
      MenuElement[] elements = popup.getSubElements();
      for(int i=0; i<elements.length; i++)
      {
         if(elements[i].getComponent() instanceof JMenuItem)
         {
            JMenuItem menuItem = (JMenuItem)elements[i].getComponent();
            menuItem.setEnabled(enable);
         }
      }
   }

   /**
    * Action method for 'Registration' menu item in 'Edit' menu or the pop-up
    * menu. Displays <code>PSServerRegistrationDialog</code> in edit mode.
    *
    * @throws IllegalStateException if there is no selection or selected node is
    * not a server node in the tree.
    */
   private void onEditRegister()
   {
      checkSelectedNode();

      PSServerRegistrationDialog dlg = new PSServerRegistrationDialog(m_frame,
         (PSDeploymentServer)m_tree.getSelectedNodeObject(),
         m_frame.getConnectionHandler(), false,
         m_tree.getRegisteredServerNames());
      dlg.setVisible(true);

   }

   /**
    * Action method for 'Policy Settings' menu item in 'Edit' menu or the pop-up
    * menu. Displays the <code>ServerPolicySettingsDialog</code> to edit the
    * policy settings.
    *
    * @throws IllegalStateException if there is no selection or selected node is
    * not a server node in the tree.
    */
   private void onEditPolicySettings()
   {
      checkSelectedNode();
      PSDeploymentServer server =
         (PSDeploymentServer)m_tree.getSelectedNodeObject();

      if(!PSDeploymentClient.getConnectionHandler().connectToServer(server))
      {
         ErrorDialogs.showErrorMessage(m_frame, m_res.getString("notConnected"),
            m_res.getString("errorTitle"));
         return;
      }
      try
      {
         PSDeploymentPolicySettingsDialog dlg =
               new PSDeploymentPolicySettingsDialog(m_frame, server);
         dlg.setVisible(true);
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(m_frame, e.getLocalizedMessage(),
            m_res.getString("errorTitle"));
         return;
      }
   }

   /**
    * Action method for 'Identify ID Types' menu item in 'Edit' menu or the
    * pop-up menu. Displays the <code>PSIDTypesDialog</code> to edit/set the
    * types on the literal ids found in all server objects.
    *
    * @throws IllegalStateException if there is no selection or selected node is
    * not a server node in the tree.
    */
   private void onEditIDTypes()
   {
      checkSelectedNode();
      PSDeploymentServer server =
         (PSDeploymentServer)m_tree.getSelectedNodeObject();

      if(!PSDeploymentClient.getConnectionHandler().connectToServer(server))
      {
         ErrorDialogs.showErrorMessage(m_frame, m_res.getString("notConnected"),
            m_res.getString("errorTitle"));
         return;
      }
      
      try {
         m_frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         Iterator idTypes = server.getDeploymentManager().getIdTypes(null);
         if(idTypes.hasNext())
         {
            PSIDTypesDialog dlg = new PSIDTypesDialog(m_frame, server,
               server.getServerName(), idTypes, false, false);
            m_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            dlg.setVisible(true);
         }
         else
         {
            ErrorDialogs.showErrorMessage(m_frame, m_res.getString("noIDTypes"),
               m_res.getString("errorTitle"));
         }
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(m_frame, e.getLocalizedMessage(),
            m_res.getString("errorTitle"));
      }
      finally
      {
         m_frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
   }

   /**
    * Action method for 'Transforms' menu item in 'Edit' menu or the pop-up
    * menu. Displays the <code>TransformsDialog</code> to map the external dbms
    * credentials and the objects that have ids in the selected server to the
    * target server. User can choose the target server in the dialog. Since the
    * objects with ids exist in repository, it is actually a repository to
    * repository mapping. So the list of target servers provided to the dialog
    * are the registered servers that are not using the same repository of the
    * selected server.
    *
    * @throws IllegalStateException if there is no selection or selected node is
    * not a server node in the tree.
    *
    */
   private void onEditTransforms()
   {
      checkSelectedNode();
      PSDeploymentServer server =
         (PSDeploymentServer)m_tree.getSelectedNodeObject();

      if(!PSDeploymentClient.getConnectionHandler().connectToServer(server))
      {
         ErrorDialogs.showErrorMessage(m_frame, m_res.getString("notConnected"),
            m_res.getString("errorTitle"));
         return;
      }
      
      if (!server.isServerLicensed())
      {
         ErrorDialogs.showErrorMessage(m_frame, m_res.getString(
            "notLicensedServer"), m_res.getString("error"));
         return;
      }

      List servers = m_tree.getRegisteredServers();
      servers.remove(server);
      if(servers.isEmpty())
      {
         ErrorDialogs.showErrorMessage(m_frame,
            m_res.getString("noTargetServerstoTransform"),
            m_res.getString("errorTitle"));
         return;
      }

      PSTransformsDialog dlg = new PSTransformsDialog(m_frame, server,
         servers);
      dlg.setVisible(true);
   }

   /**
    * Action method for 'About' menu item in 'Help' menu. Displays the <code>
    * AboutDialog</code> that displays the information about the product.
    */
   private void onHelpAbout()
   {
      PSFormatVersion version = new PSFormatVersion("com.percussion.deployer.ui");
      
      PSAboutDialog dlg = new PSAboutDialog(m_frame, 
         m_res.getString("aboutTitle"), version.getVersionString());
      dlg.setVisible(true);
   }

   /**
    * Action method for 'Delete' menu item in the pop-up menu. Displays a
    * confirmation dialog to delete and if user chose to delete, then it deletes
    * the selected server node.
    *
    * @throws IllegalStateException if there is no selection or selected node is
    * not a server node in the tree.
    */
   private void onDelete()
   {
      checkSelectedNode();
      PSDeploymentServer server =
         (PSDeploymentServer)m_tree.getSelectedNodeObject();
      int option = JOptionPane.showConfirmDialog(m_frame,
         ErrorDialogs.cropErrorMessage( MessageFormat.format(
         m_res.getString("deleteServerMsg"),new String[]{server.toString()})),
         m_res.getString("deleteServerTitle"),
         JOptionPane.YES_NO_OPTION);
      if(option == JOptionPane.YES_OPTION)
      {
         onDisconnect();
         m_tree.removeSelectedServerNode();
      }
   }

   /**
    * Action method for 'Connect' menu item in the pop-up menu. Displays a
    * dialog to connect to the server if the credentials are not saved with the
    * server registration, otherwise directly makes the connection with the
    * saved credentials. Ignores the action if the server is already connected.
    *
    * @throws IllegalStateException if there is no selection or selected node is
    * not a server node in the tree.
    *
    */
   private void onConnect()
   {
      checkSelectedNode();

      m_frame.getConnectionHandler().connectToServer(
         (PSDeploymentServer)m_tree.getSelectedNodeObject());
   }

   /**
    * Action method for 'Disconnect' menu item in the pop-up menu. Ignores the
    * action if the server is already disconnected.
    *
    * @throws IllegalStateException if there is no selection or selected node is
    * not a server node in the tree.
    */
   private void onDisconnect()
   {
      checkSelectedNode();
      try
      {
         m_tree.disconnectFromServer(m_tree.getSelectedNode());
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(m_frame, e.getLocalizedMessage(),
            m_res.getString("errorTitle"));
         return;
      }

   }

   /**
    * Checks that the tree has selection and the selected node represents a
    * server node.
    *
    * @throws IllegalStateException if there is no selection or selected node is
    * not a server node in the tree.
    */
   private void checkSelectedNode()
   {
      if(!(m_tree.getSelectedNodeObject() instanceof PSDeploymentServer))
         throw new IllegalStateException(
            "No selected node or selected node is not a server node");
   }

   /**
    * The menu handler which creates the pop-up menu for the server node in the
    * tree and sets the action listener on the menu items.
    */
   private class TreeMenuHandler extends MouseAdapter
   {
      /**
       * Constructs this menu handler. Sets the supplied listener on the menu
       * items.
       *
       * @param listener the action listener that need to be set on the menu
       * items, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if <code>listener</code> is <code>null
       * </code>
       */
      public TreeMenuHandler(ActionListener listener)
      {
         if(listener == null)
            throw new IllegalArgumentException("listener may not be null.");

         createServerNodeMenu(listener);
         createSourceNodeMenu(listener);
         createTargetNodeMenu(listener);
      }

      /**
       * Creates the pop-up menu with menu items for a server node menu. The
       * menu items are {@link #MENU_CONNECT Connect},
       * {@link #MENU_EDIT_REGISTRATION Registration},
       * {@link #MENU_ACTION_CREATE_ARCHIVE Create Archive},
       * {@link #MENU_ACTION_INSTALL_ARCHIVE Create Archive},
       * {@link #MENU_EDIT_POLICY_SETTINGS Policy Settings},
       * {@link #MENU_EDIT_ID_TYPES Identify ID Types}, {@link
       * #MENU_EDIT_TRANSFORMS Transforms} and {@link #MENU_DELETE Delete}. The
       * 'Connect' menu item will be toggled between 'Connect' and {@link
       * #MENU_DISCONNECT Disconnect} based on the status of the server
       * connection.
       * 
       * @param listener the action listener that need to be set on the menu
       *            items, assumed not to be <code>null</code>
       */
      private void createServerNodeMenu(ActionListener listener)
      {
         JMenuItem menuConnectDisconnect = PSDeploymentMenuHandler.createMenuItem(
            m_res.getString(MENU_CONNECT), '0', null, null, null, MENU_CONNECT,
            MENU_CONNECT, listener);
         m_serverMenu.add( menuConnectDisconnect );

         JMenuItem menuEditReg = PSDeploymentMenuHandler.createMenuItem(
            m_res.getString(MENU_EDIT_REGISTRATION), '0', null, null, null,
            MENU_EDIT_REGISTRATION,
            MENU_EDIT_REGISTRATION, listener);
         m_serverMenu.add( menuEditReg );
         
         JMenuItem menuActionCreate = PSDeploymentMenuHandler.createMenuItem(
               m_res.getString(MENU_ACTION_CREATE_ARCHIVE), '0', null, null, null,
               MENU_ACTION_CREATE_ARCHIVE,
               MENU_ACTION_CREATE_ARCHIVE, listener);
         m_serverMenu.add( menuActionCreate );
         
         JMenuItem menuActionInstall = PSDeploymentMenuHandler.createMenuItem(
               m_res.getString(MENU_ACTION_INSTALL_ARCHIVE), '0', null, null, null,
               MENU_ACTION_INSTALL_ARCHIVE,
               MENU_ACTION_INSTALL_ARCHIVE, listener);
         m_serverMenu.add( menuActionInstall );

         JMenuItem menuEditPolicy = PSDeploymentMenuHandler.createMenuItem(
            m_res.getString(MENU_EDIT_POLICY_SETTINGS), '0', null, null, null,
            MENU_EDIT_POLICY_SETTINGS,
            MENU_EDIT_POLICY_SETTINGS, listener);
         m_serverMenu.add( menuEditPolicy );

         JMenuItem menuEditIDTypes = PSDeploymentMenuHandler.createMenuItem(
            m_res.getString(MENU_EDIT_ID_TYPES), '0', null, null, null,
            MENU_EDIT_ID_TYPES,
            MENU_EDIT_ID_TYPES, listener);
         m_serverMenu.add( menuEditIDTypes );

         JMenuItem menuEditTransforms = PSDeploymentMenuHandler.createMenuItem(
            m_res.getString(MENU_EDIT_TRANSFORMS), '0', null, null, null,
            MENU_EDIT_TRANSFORMS,
            MENU_EDIT_TRANSFORMS, listener);
         m_serverMenu.add( menuEditTransforms );

         JMenuItem menuDelete = PSDeploymentMenuHandler.createMenuItem(
            m_res.getString(MENU_DELETE), '0', null, null, null,
            MENU_DELETE,
            MENU_DELETE, listener);
         m_serverMenu.add( menuDelete );
      }

      /**
       * Creates the pop-up menu with menu items for a source node menu. The menu
       * items are {@link #MENU_ACTION_CREATE_ARCHIVE Create Archive}
       *
       * @param listener the action listener that need to be set on the menu
       * items, assumed not to be <code>null</code>
       */
      private void createSourceNodeMenu(ActionListener listener)
      {
         JMenuItem menuActionCreate = PSDeploymentMenuHandler.createMenuItem(
               m_res.getString(MENU_ACTION_CREATE_ARCHIVE), '0', null, null, null,
               MENU_ACTION_CREATE_ARCHIVE,
               MENU_ACTION_CREATE_ARCHIVE, listener);
         m_sourceMenu.add( menuActionCreate );
      }
      
      /**
       * Creates the pop-up menu with menu items for a target node menu. The menu
       * items are {@link #MENU_ACTION_INSTALL_ARCHIVE Create Archive}
       *
       * @param listener the action listener that need to be set on the menu
       * items, assumed not to be <code>null</code>
       */
      private void createTargetNodeMenu(ActionListener listener)
      {
         JMenuItem menuActionInstall = PSDeploymentMenuHandler.createMenuItem(
               m_res.getString(MENU_ACTION_INSTALL_ARCHIVE), '0', null, null, null,
               MENU_ACTION_INSTALL_ARCHIVE,
               MENU_ACTION_INSTALL_ARCHIVE, listener);
         m_targetMenu.add( menuActionInstall );
      }
      
      /**
       * Action method for a mouse click on the tree.
       * If the mouse action is a pop-up(right mouse button click) action and
       * the tree node on which the mouse action occurred represents a server
       * (<code>PSDeploymentServer</code>) object, then makes the node as
       * selected and shows the server pop-up menu. The 'Connect' menu item will
       * be toggled between 'Connect' and 'Disconnect' based on the server
       * connection status. Disables the menus
       * If the the node is a Source node it will show Source pop-up menu
       * <br>
       * @param event the mouse event generated for this action, assumed
       * not to be <code>null</code> as Swing model calls this method with
       * an event when mouse pressed action occurs.
       *
       * @throws IllegalStateException if 'Connect' menu item is not found in
       * the server pop-up menu.
       */
      public void mouseReleased(MouseEvent event)
      {
         if( event.getClickCount() == 1 &&
            (event.getModifiers() & InputEvent.BUTTON3_MASK) ==
            InputEvent.BUTTON3_MASK &&
            event.getSource() instanceof JTree )
         {
            JTree source = (JTree)event.getSource();
            TreePath path = source.getClosestPathForLocation(
               event.getX(), event.getY());
            DefaultMutableTreeNode node =
               (DefaultMutableTreeNode)path.getLastPathComponent();
            source.setSelectionPath(path);
            if(node.getUserObject() instanceof PSDeploymentServer)
            {
               PSDeploymentServer depServer =
                  (PSDeploymentServer)node.getUserObject();

               //get the menu string to represent.
               String menuStr = MENU_CONNECT;
               if(depServer.isConnected())
                  menuStr = MENU_DISCONNECT;

               //get the connect menu item from the pop-up menu to change
               //disable/enable all menus that needs server to be connected based
               //on connection status.
               JMenuItem connectMenu = null;
               Component[] comps = m_serverMenu.getComponents();
               for(int i=0; i<comps.length; i++)
               {
                  String name = comps[i].getName();
                  if(MENU_CONNECT.equals(name) )
                  {
                     connectMenu = (JMenuItem)comps[i];
                  }
               }

               if(connectMenu == null)
                  throw new IllegalStateException(
                     "Connect/Disconnect menu not found");

               //change the label and action
               connectMenu.setText(m_res.getString(menuStr));
               connectMenu.setActionCommand(menuStr);

               //display the pop-up menu
               m_serverMenu.show(event.getComponent(),
                  event.getX(), event.getY());
            }
//            // If node is a Root folder
//            if(node.toString() == PSDeploymentClient.getResources().getString(
//            "serverGroups"))
//            {
//               PSMainFrame.removeDeployProperty(PSMainFrame.LAST_SELECTED_SERVER);
//            }
            // If node is a Source folder
            if(node.toString() == PSServersTree.ms_source)
            {
               //display the pop-up menu
               m_sourceMenu.show(event.getComponent(),
                  event.getX(), event.getY());
            }
            // If node is a Target folder
            if(node.toString() == PSServersTree.ms_target)
            {
               //display the pop-up menu
               m_targetMenu.show(event.getComponent(),
                  event.getX(), event.getY());
            }
         }
      }

      /**
       * The popup menu that need to be shown when user right-clicks on a node
       * that is representing a server. Never <code>null</code> after
       * initialization. The menu items are added in <code>createServerNodeMenu
       * </code> method and the 'Connect' menu is toggled between 'Connect' and
       * 'Disconnect' based on the server connection status.
       */
      private JPopupMenu m_serverMenu = new JPopupMenu();
      
      /**
       * The popup menu that need to be shown when user right-clicks on a node
       * that is representing a Source folder. Never <code>null</code> after
       * initialization. The menu items are added in <code>createSourcerNodeMenu
       * </code> method 
       */
      private JPopupMenu m_sourceMenu = new JPopupMenu();
      
      /**
       * The popup menu that need to be shown when user right-clicks on a node
       * that is representing a Target folder. Never <code>null</code> after
       * initialization. The menu items are added in <code>createSourcerNodeMenu
       * </code> method 
       */
      private JPopupMenu m_targetMenu = new JPopupMenu();
   }

   /**
    * The servers tree on which the main menu actions are dependent and for
    * which the pop-up menu is set. Initialized during construction and never
    * <code>null</code> after that. The tree structure will be modified as user
    * adds/deletes the registered servers.
    */
   private PSServersTree m_tree;

   /**
    * The main frame of the application to which the main menu bar need to be
    * set and should be used as parent for the dialogs that are created in this
    * menu handler action methods. Initialized during construction and never
    * <code>null</code> or modified after that.
    */
   private PSMainFrame m_frame;

   /**
    * The resource bundle that holds resource strings used by this frame. Never
    * <code>null</code> or modified after initialization.
    */
   private PSResources m_res;

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * menu 'Action'.
    */
   public static final String MENU_ACTION = "menuAction";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Register Server' in 'Action' menu.
    */
   public static final String MENU_ACTION_REGISTER = "menuActionRegister";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Create Archive' in 'Action' menu.
    */
   public static final String MENU_ACTION_CREATE_ARCHIVE =
      "menuActionCreateArchive";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Install Archive' in 'Action' menu.
    */
   public static final String MENU_ACTION_INSTALL_ARCHIVE =
      "menuActionInstallArchive";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Exit' in 'Action' menu.
    */
   public static final String MENU_ACTION_EXIT = "menuActionExit";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * menu 'Edit'.
    */
   public static final String MENU_EDIT = "menuEdit";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Registration' in 'Edit' menu or in the pop-up menu of tree.
    */
   public static final String MENU_EDIT_REGISTRATION = "menuEditRegister";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Policy Settings' in 'Edit' menu or in the pop-up menu of tree.
    */
   public static final String MENU_EDIT_POLICY_SETTINGS = "menuEditSettings";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Identify ID Types' in 'Edit' menu or in the pop-up menu of tree.
    */
   public static final String MENU_EDIT_ID_TYPES = "menuEditIDTypes";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Transforms' in 'Edit' menu or in the pop-up menu of tree.
    */
   public static final String MENU_EDIT_TRANSFORMS = "menuEditTransforms";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * menu 'Help'.
    */
   public static final String MENU_HELP = "menuHelp";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'About' in 'Help' menu.
    */
   public static final String MENU_HELP_ABOUT = "menuHelpAbout";

   /**
    * The constant to use for getting the related resources(label), the
    * component name and action command of the pop-up menu item 'Delete' for the
    * tree.
    */
   public static final String MENU_DELETE = "menuDelete";

   /**
    * The constant to use for getting the related resources(label), the
    * component name and action command of the pop-up menu item 'Connect' for
    * the tree.
    */
   public static final String MENU_CONNECT = "menuConnect";

   /**
    * The constant to use for getting the related resources(label) and action
    * command of the pop-up menu item 'Disconnect' for the tree.
    */
   public static final String MENU_DISCONNECT = "menuDisconnect";

   /**
   * The constant to use for getting the related resources(label, mnemonic,
   * accelerator key, image), the component name and action command of the menu
   * item 'Multi-Server Manager Help' in 'Help' menu.
    */
   public static final String MENU_MSM_HELP = "multiServerMgrHelp";

}

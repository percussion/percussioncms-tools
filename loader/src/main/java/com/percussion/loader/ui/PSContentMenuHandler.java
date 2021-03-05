/*[ PSContentMenuHandler.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;


import com.percussion.guitools.PSResources;
import com.percussion.guitools.ResourceHelper;
import com.percussion.tools.help.PSJavaHelp;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * The handler that creates and handles the actions of the main menu bar of the
 * main frame.
 */
public class PSContentMenuHandler
{
   /**
    * Constructs the menu handler with supplied parameters. Creates the main
    * menu bar for the frame.
    *
    * @param frame the main frame of the application to which the main menu is
    * set, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public PSContentMenuHandler(PSMainFrame frame)
   {
      if(frame == null)
         throw new IllegalArgumentException("frame may not be null.");

      m_frame = frame;
      m_res = PSContentLoaderResources.getResources();
      m_frame.setJMenuBar(createMainMenu());
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

      //File Menu items
      String menuStr = MENU_FILE;
      JMenu menuFile = createMenu( m_res.getString(menuStr),
         ResourceHelper.getMnemonic( m_res, menuStr ), menuStr);
      menuBar.add(menuFile);

      menuStr = MENU_FILE_NEW;
      JMenuItem menuFileNew = createMenuItem(menuStr,
         new ImageIcon(getClass().getResource(m_res.getString("gif_newFile"))),
         m_frame);
      menuFile.add(menuFileNew);

      menuStr = MENU_FILE_OPEN;
      JMenuItem menuFileOpen = createMenuItem(menuStr,
         new ImageIcon(getClass().getResource(m_res.getString("gif_openFile"))),
         m_frame);
       menuFile.add(menuFileOpen);

      menuStr = MENU_FILE_SAVEAS;
      JMenuItem menuActionSaveAs = createMenuItem(menuStr, null, m_frame);
      menuFile.add(menuActionSaveAs);
      
      menuFile.addSeparator();
      
      menuStr = MENU_FILE_SAVE;
      JMenuItem menuActionSave = createMenuItem(menuStr,
         new ImageIcon(getClass().getResource(m_res.getString("gif_saveFile"))),
         m_frame);
      menuFile.add(menuActionSave);

      menuFile.addSeparator();
      
      menuStr = MENU_FILE_EXIT;
      JMenuItem menuActionExit = createMenuItem(menuStr, null, m_frame);
      menuFile.add(menuActionExit);

      //View Menu items
      CheckBoxMenuItemListener cbl = new CheckBoxMenuItemListener();

      menuStr = MENU_VIEW;
      JMenu menuView = createMenu( m_res.getString(menuStr),
         ResourceHelper.getMnemonic( m_res, menuStr ), menuStr);
      menuBar.add(menuView);

      menuStr = MENU_VIEW_TOOLBAR;
      JCheckBoxMenuItem menuToolbar = new JCheckBoxMenuItem(
         m_res.getString(menuStr));
      menuToolbar.setActionCommand(MENU_VIEW_TOOLBAR);
      menuToolbar.addItemListener(cbl);
      menuToolbar.setMnemonic(ResourceHelper.getMnemonic(m_res, menuStr));
      menuToolbar.setSelected(true);
      menuView.add(menuToolbar);

      menuStr = MENU_VIEW_STATBAR;
      JCheckBoxMenuItem menuViewStatBar = new JCheckBoxMenuItem(
         m_res.getString(menuStr));
      menuViewStatBar.setActionCommand(MENU_VIEW_STATBAR);
      menuViewStatBar.setMnemonic(ResourceHelper.getMnemonic(m_res, menuStr));
      menuViewStatBar.addItemListener(cbl);
      menuViewStatBar.setSelected(true);
      menuView.add(menuViewStatBar);

      menuStr = MENU_VIEW_METADATA;
      JCheckBoxMenuItem menuViewMetaData = new JCheckBoxMenuItem(
         m_res.getString(menuStr));
      menuViewMetaData.setActionCommand(MENU_VIEW_METADATA);
      menuViewMetaData.setMnemonic(ResourceHelper.getMnemonic(m_res, menuStr));
      menuViewMetaData.addItemListener(cbl);
      menuViewMetaData.setSelected(true);
      menuView.add(menuViewMetaData);

      menuStr = MENU_VIEW_STATUS;
      JCheckBoxMenuItem menuViewStatus = new JCheckBoxMenuItem(
         m_res.getString(menuStr));
      menuViewStatus.setActionCommand(MENU_VIEW_STATUS);
      menuViewStatus.setMnemonic(ResourceHelper.getMnemonic(m_res, menuStr));
      menuViewStatus.addItemListener(cbl);
      menuViewStatus.setSelected(true);
      menuView.add(menuViewStatus);

      menuStr = MENU_VIEW_LOG;
      JCheckBoxMenuItem menuViewLog = new JCheckBoxMenuItem(
         m_res.getString(menuStr));
      menuViewLog.setActionCommand(MENU_VIEW_LOG);
      menuViewLog.setMnemonic(ResourceHelper.getMnemonic(m_res, menuStr));
      menuViewLog.addItemListener(cbl);
      menuViewLog.setSelected(true);
      menuView.add(menuViewLog);

      menuStr = MENU_ACTIONS;
      JMenu menuActions = createMenu( m_res.getString(menuStr),
         ResourceHelper.getMnemonic( m_res, menuStr ), menuStr);
      menuBar.add(menuActions);

      menuStr = MENU_ACTIONS_SCAN;
      JMenuItem menuActionsScan = createMenuItem(menuStr,
         new ImageIcon(getClass().getResource(m_res.getString("gif_scan"))),
         m_frame);
      menuActions.add(menuActionsScan);

      menuStr = MENU_ACTIONS_UPLOAD;
      JMenuItem menuActionsUpload = createMenuItem(menuStr,
         new ImageIcon(getClass().getResource(m_res.getString("gif_upload"))),
         m_frame);
      menuActions.add(menuActionsUpload);
      
      menuStr = MENU_ACTIONS_RELOAD;
      JMenuItem menuActionReload = createMenuItem(menuStr, null, m_frame);
      menuActions.add(menuActionReload);
     
      menuStr = MENU_ACTIONS_RELOADASNEW;
      JMenuItem menuActionReloadAsNew = createMenuItem(menuStr, null, m_frame);
      menuActions.add(menuActionReloadAsNew);

      menuStr = MENU_TOOLS;
      JMenu menuTools = createMenu( m_res.getString(menuStr),
         ResourceHelper.getMnemonic( m_res, menuStr ), menuStr);
      menuBar.add(menuTools);

      menuStr = MENU_TOOLS_DESC;
      JMenuItem menuToolsDesc = createMenuItem(menuStr, m_frame);
      menuTools.add(menuToolsDesc);

      menuStr = MENU_TOOLS_PREF;
      JMenuItem menuToolsPref = createMenuItem(menuStr, m_frame);
      menuTools.add(menuToolsPref);

      //Help Menu items
      menuStr = MENU_HELP;
      JMenu menuHelp = createMenu( m_res.getString(menuStr),
         ResourceHelper.getMnemonic( m_res, menuStr ), null);
      menuBar.add( menuHelp );

      menuStr = MENU_HELP_ABOUT;
      JMenuItem menuHelpAbout = createMenuItem(menuStr,
         new ImageIcon(getClass().getResource(m_res.getString("gif_empty"))),
         m_frame);
      menuHelp.add(menuHelpAbout);

      menuStr = MENU_CONTENT_LOADER_HELP;
      JMenuItem menuRhythmyxContentLoaderHelp = createMenuItem(menuStr,
         new ImageIcon(getClass().getResource(m_res.getString("gif_help"))),
         m_frame);
      menuHelp.add(menuRhythmyxContentLoaderHelp);

      return menuBar;
   }

   /**
    * Handles selection or deselection of menu items in the 'View' menu.
    * If an item is selected then it's removed from the view and if deselected
    * it's visible again e.g if 'Toolbars' is selected then it's removed from
    * the view.
    */
   private class CheckBoxMenuItemListener implements ItemListener
   {
      /**
       * Called by the Swing subsystem internally.
       * @param e never <code>null</code>.
       */
      public void itemStateChanged(ItemEvent e)
      {
         Object source = e.getItemSelectable();
         if (source instanceof JCheckBoxMenuItem)
         {
            JCheckBoxMenuItem c = (JCheckBoxMenuItem)source;
            boolean isSelected = c.isSelected();
            m_frame.reArrange(c.getActionCommand(), isSelected);
         }
      }
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
   private JMenu createMenu(String label, char mnemonic, String actionCommand)
   {
      JMenu menu = new JMenu( label );

      menu.setMnemonic( mnemonic );
      if(actionCommand != null && actionCommand.trim().length() > 0)
         menu.setActionCommand(actionCommand);
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
    * Gets the label, mnemonic, accelerator and tool tip values for this menu
    * item from the resources for the supplied resource string and creates the
    * menu item. Uses resource string key as the action command and its name.
    *
    * @param resourceStr the resource string to get the values from resources,
    *    assumed not to be <code>null</code> or empty and the resource string
    *    exists in the main resource bundle.
    * @param icon the icon to use for this menu item, may be <code>null</code>
    *    if not used.
    * @param listener the action listener to this menu item, assumed not to be
    *    <code>null</code>
    *
    * @return the menu item, never <code>null</code>
    */
   private JMenuItem createMenuItem(String resourceStr, ImageIcon icon,
      ActionListener listener)
   {
      return createMenuItem(
         m_res.getString(resourceStr),
         ResourceHelper.getMnemonic( m_res, resourceStr ),
         ResourceHelper.getAccelKey( m_res, resourceStr ),
         ResourceHelper.getToolTipText( m_res, resourceStr ),
         icon,
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
    * Action method for 'Content Loader Help' under 'Help' menu.
    * Displays a dialog containing help documentation.
    */
   private void onHelp()
   {
      String id = "PSDeploymentMenuHandler";
      PSJavaHelp.launchHelp(id);
   }

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
    * menu 'File'.
    */
   public static final String MENU_FILE = "menuFile";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'New' in 'File' menu or the icon signifying 'New' button in the
    * toolbar.
    */
   public static final String MENU_FILE_NEW = "menuFileNew";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Open' in 'File' menu the icon signifying 'Open' button in the
    * toolbar.
    */
   public static final String MENU_FILE_OPEN = "menuFileOpen";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Load Status' in 'File' menu.
    */
   public static final String MENU_FILE_LOADSTAT = "menuFileLoadStat";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Save' in 'File' menu or the icon signifying 'Save' button in the
    * toolbar.
    */
   public static final String MENU_FILE_SAVE = "menuFileSave";

   /**
    * The component name for SaveAs menu item.
    */
   public static final String MENU_FILE_SAVEAS = "menuFileSaveAs";

   /**
    * The component name for Exit menu item.
    */
   public static final String MENU_FILE_EXIT = "menuFileExit";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * menu 'View'.
    */
   public static final String MENU_VIEW = "menuView";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Toolbar' in 'View' menu.
    */
   public static final String MENU_VIEW_TOOLBAR = "menuViewToolbar";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Status Bar' in 'View' menu.
    */
   public static final String MENU_VIEW_STATBAR = "menuViewStatBar";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Log' in 'View' menu.
    */
   public static final String MENU_VIEW_LOG = "menuViewLog";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'MetaData' in 'View' menu.
    */
   public static final String MENU_VIEW_METADATA = "menuViewMetaData";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Status' in 'View' menu.
    */
   public static final String MENU_VIEW_STATUS = "menuViewStatus";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Scan' in 'Actions' menu.
    */
   public static final String MENU_ACTIONS_SCAN = "menuActionsScan";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Upload' in 'Actions' menu.
    */
   public static final String MENU_ACTIONS_UPLOAD = "menuActionsUpload";

   /**
    * The key for the "Reload" text in the action pull down menu
    */
   public static final String MENU_ACTIONS_RELOAD = "menuActionsReload";

   /**
    * The key for the "Reload As New Item" text in the action pull down menu
    */
   public static final String MENU_ACTIONS_RELOADASNEW = 
      "menuActionsReloadAsNew";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of 'View'
    * menu.
    */
   public static final String MENU_ACTIONS = "menuActions";


   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * 'Tools'.
    */
   public static final String MENU_TOOLS = "menuTools";


   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Descriptor Setup...' in 'Tools' menu.
    */
   public static final String MENU_TOOLS_DESC = "menuToolsDesc";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'Preferences...' in 'Tools' menu.
    */
   public static final String MENU_TOOLS_PREF = "menuToolsPrefs";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * menu 'Help'.
    */
   public static final String MENU_HELP = "menuHelp";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'ContentLoader Help' in 'Help' menu.
    */
   public static final String MENU_CONTENT_LOADER_HELP =
      "menuContentLoaderHelp";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the menu
    * item 'About' in 'Help' menu.
    */
   public static final String MENU_HELP_ABOUT = "menuHelpAbout";
}

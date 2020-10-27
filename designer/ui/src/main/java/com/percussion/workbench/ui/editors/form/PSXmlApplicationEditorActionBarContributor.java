/******************************************************************************
 *
 * [ PSXmlApplicationEditorActionBarContributor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.Debug;
import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.E2DesignerResources;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.ResourceHelper;
import com.percussion.E2Designer.UIFigureFrame;
import com.percussion.E2Designer.UIMainFrame;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSXmlApplicationModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorActionBarContributor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import static com.percussion.workbench.ui.editors.form.PSXmlAppStatusAction.XMLAPP_DISABLE;
import static com.percussion.workbench.ui.editors.form.PSXmlAppStatusAction.XMLAPP_ENABLE;

/**
 * <p>Provides menu, toolbar buttons.</p>
 * <p>Supports a concept of dynamic menus. When a legacy UI editor
 * {@link UIFigureFrame} tab
 * is selected, it is queried as to whether it wants to add any menu
 * items to certain menus in the frame. If it does, it provides them and the
 * menu item(s) and toolbar button(s) are created and added to the appropriate
 * object. When the tab folder selection changes,
 * all dynamically added items are removed and
 * the next frame gets a chance to add its items. The frame must
 * implement {@link com.percussion.E2Designer.IDynamicActions}
 * to support this functionality.
 * </p>
 * 
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationEditorActionBarContributor
      extends EditorActionBarContributor
{
   /*
    * Note, this class contains disabled menus which were shown to debug and
    * test integration with legacy actions.
    */
   public PSXmlApplicationEditorActionBarContributor()
   {
      createActions();
      if (E2Designer.getApp() == null)
      {
         throw new IllegalStateException("Legacy subsystem is not initialized!");
      }
   }
   
   @Override
   public void init(IActionBars bars, IWorkbenchPage page)
   {
      super.init(bars, page);
      m_editorHelper.init(bars);
   }

   @Override
   public void dispose()
   {
      m_editorHelper.dispose();
      super.dispose();
   }

   /**
    * Connects to the editor returned by {@link #getActiveEditor()}.
    */
   private void connectToEditor()
   {
      getTabFolder().addSelectionListener(m_editorFolderSelectionListener);
      doContributeToMenu();
   }

   /**
    * Disconnects from the editor returned by {@link #getActiveEditor()}.
    */
   private void disconnectFromEditor()
   {
      getTabFolder().removeSelectionListener(m_editorFolderSelectionListener);
   }

   /**
    * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
    */
   @Override
   public void contributeToMenu(IMenuManager menuManager)
   {
      m_menuManager = menuManager;
      doContributeToMenu();
   }
   
   @Override
   public void contributeToStatusLine(IStatusLineManager statusLineManager)
   {
      getMainFrame().setStatusLineManager(statusLineManager);
   }

   @Override
   public void contributeToToolBar(final IToolBarManager toolBarManager)
   {
      m_toolBarManager = toolBarManager;
      toggleStartStop();
   }

   /**
    * Method which actually contributes to the menu.
    * The editor menu is dynamic because it depends on the current page, so
    * the menu needs to be regenerated any time editor or editor page is changed.  
    */
   private void doContributeToMenu()
   {
      removeExistingDynamicMenuContribution();
      disableChildSupportedActions();
//      maybeAddStaticMenuContribution();
      
      clearGlobalActionHandlers();
      
      applyTextEditorContributions();
      addDynamicMenuContribution();

      toggleStartStop();

      getActionBars().updateActionBars();
      updateMenu();
   }

   /**
    * Reset global action handlers, so they can be provided by this contributor.
    *
    */
   private void clearGlobalActionHandlers()
   {
      getActionBars().clearGlobalActionHandlers();
      getActionBars().updateActionBars();
   }
   
   private void applyTextEditorContributions()
   {
      if (getActiveEditor() == null)
      {
         return;
      }
      final TextEditor textEditor;
      if (getActiveEditor().getEditorControl() instanceof TextEditor)
      {
         textEditor = (TextEditor) getActiveEditor().getEditorControl();
      }
      else
      {
         textEditor = null;
      }
      m_editorHelper.activateActionBars(textEditor);
   }

   /**
    * Toggles the start/stop menu and toolbar button according to the application
    * status.
    */
   private void toggleStartStop()
   {
      if (m_activeEditor == null)
      {
         return;
      }

      // toolbar
      if (isToolBarInitialized())
      {
         m_toolBarManager.remove(XMLAPP_ENABLE);
         m_toolBarManager.remove(XMLAPP_DISABLE);
         m_toolBarManager.update(true);
         try
         {
            final String id = isApplicationEnabled() ? XMLAPP_DISABLE : XMLAPP_ENABLE;
            m_toolBarManager.add(getAnyAction(id));
         }
         catch (PSModelException e)
         {
            PSDlgUtil.showError(e);
         }
         m_toolBarManager.update(true);
      }

      // menu
      // this was visible only for testing legacy actions
      /*
      final IMenuManager appMenu = m_menuManager.findMenuUsingPath(MENU_LEGACY + "/" + MENU_APP);
      assert appMenu != null : "Application menu expected to be already created";
      appMenu.find(XMLAPP_ENABLE).setVisible(!isApplicationEnabled());
      appMenu.find(XMLAPP_DISABLE).setVisible(isApplicationEnabled());
      appMenu.markDirty();
      */
   }
   
   /**
    * Returns <code>true</code> if toolbar field is assigned and initialized. 
    */
   private boolean isToolBarInitialized()
   {
      return m_toolBarManager != null;
   }

   /**
    * Returns <code>true</code> if the application is enabled.
    */
   private boolean isApplicationEnabled() throws PSModelException
   {
      return getApplicationRef() != null
            && getAppModel().isAppRunningOnServer(getApplicationRef());
   }

   /**
    * Application model.
    */
   private IPSXmlApplicationModel getAppModel() throws PSModelException
   {
      return (IPSXmlApplicationModel) PSCoreFactory.getInstance().getModel(
            PSObjectTypes.XML_APPLICATION);
   }

   /**
    * Walks list of actions that are only supported by child windows and
    * disables all of them.
    */
   private void disableChildSupportedActions()
   {
      for (final IAction action : m_childSupportedActions.values())
      {
         action.setEnabled(false);
      }
   }

   private void updateMenu()
   {
      getMenuManager().update(true);
   }

   /**
    * Creates static menu contributions if they are not created yet.
    * Note, right now call to this method is commented out.
    */
   @SuppressWarnings("unused")
   private void maybeAddStaticMenuContribution()
   {
      if (staticMenuNotExistYet())
      {
         createLegacyMenu();
      }
   }

   /**
    * Creates legacy menu 
    */
   private void createLegacyMenu()
   {
      final MenuManager legacyMenu = createMenuManager(MENU_LEGACY);
      try
      {
         getMenuManager().insertBefore(IWorkbenchActionConstants.M_EDIT, legacyMenu);
      }
      catch (IllegalArgumentException e)
      {
         getMenuManager().add(legacyMenu);
      }
      
      createLegacyMenuItems();
   }

   /**
    * Returns <code>true</code> if static menus are not created yet.
    */
   private boolean staticMenuNotExistYet()
   {
      return getMenuManager().findMenuUsingPath(MENU_LEGACY) == null;
   }

   /**
    * Creates contributions regenerated for each editor on a tab.
    *
    */
   private void addDynamicMenuContribution()
   {
      m_actionListeners.clear();
      if (getActiveEditor() == null)
      {
         return;
      }
      if (getActiveEditor().getEditorControl() instanceof UIFigureFrame)
      {
         // ask window if they have any dynamic actions
         final UIFigureFrame frame =
            (UIFigureFrame) getActiveEditor().getEditorControl();
         if (frame.hasActionItems(MENU_INSERT))
         {
            final IAction[] actions = frame.getActionItems(MENU_INSERT);
            
            Debug.assertTrue(actions != null && actions.length > 0, E2Designer.getResources(),
                         "BadActionArray", null);
            if (actions.length > 0)
            {
               final MenuManager insertMenu = createInsertMenuManager();
               for (final IAction action : actions)
               {
                  insertMenu.add(action);
               }
            }
         }

         // adding all the InternalFrame's actionListeners
         for (final Action listener : frame.getDynamicActionListeners())
         {
            final IAction action = m_childSupportedActions.get(listener.getId()); 
            assert action != null; 
            m_actionListeners.put(listener.getId(), listener);
            action.setEnabled(true);
            maybeRetargetGlobalAction(action);
         }
      }
   }

   /**
    * If this action corresponds to globally defined action - retarget global
    * action.
    */
   private void maybeRetargetGlobalAction(IAction action)
   {
      if (GLOBAL_ACTIONS.keySet().contains(action.getId()))
      {
         getActionBars().setGlobalActionHandler(
               GLOBAL_ACTIONS.get(action.getId()), action);
      }
   }

   /**
    * Creates and contributes "Insert" menu root.  
    */
   private MenuManager createInsertMenuManager()
   {
      final MenuManager insertMenu = createMenuManager(MENU_INSERT);
      
      m_menus.add(insertMenu);
      try
      {
         getMenuManager().insertAfter(IWorkbenchActionConstants.M_EDIT, insertMenu);
      }
      catch (IllegalArgumentException e)
      {
         getMenuManager().add(insertMenu);
      }
      return insertMenu;
   }

   private void removeExistingDynamicMenuContribution()
   {
      for (final MenuManager menu : m_menus)
      {
         getMenuManager().remove(menu);
         menu.dispose();
      }
      m_menus.clear();
   }
   
   /**
    * Main menu items. The following algorithm is used to create keys to find
    * menu strings in the resource bundle. Main menu items have
    * a key name of the form 'menu<MenuName>'. Sub menus have a key name
    * of the form 'menu<MenuName><SubMenuName>'.
    * <p>
    * Optional accelerator keys, icons and mnemonic characters can be supplied for
    * each item in the resource bundle.
    *
    * @returns the newly created menubar
    */
   private void createLegacyMenuItems()
   {
      createApplicationMenu();
      createEditMenu();
      createInsertMenu();
//      createToolsMenu();
   }

//   private void createToolsMenu()
//   {
//      final IMenuManager toolsMenu = createMenuManager(MENU_TOOLS);
//      getLegacyMenu().add(toolsMenu);
//
//      // only add debugging menu item if feature is supported on server
//      if (FeatureSet.getFeatureSet().isFeatureSupported("trace", 1))
//      {
//         toolsMenu.add(getAnyAction(PSXmlAppDebuggingAction.XMLAPP_DEBUGGING));
//      }
//
//      toolsMenu.add(getAnyAction(MENU_TOOLS_OPTIONS));
//   }

   private void createInsertMenu()
   {
      // there are currently no non-dynamic items on this menu
   }

   private void createEditMenu()
   {
      final IMenuManager editMenu = createMenuManager(MENU_EDIT);
      getLegacyMenu().add(editMenu);
      
      editMenu.add(getAnyAction(MENU_EDIT_CUT));
      editMenu.add(getAnyAction(MENU_EDIT_COPY));
      editMenu.add(getAnyAction(MENU_EDIT_PASTE));

      editMenu.add(new Separator());
      editMenu.add(getAnyAction(MENU_EDIT_CLEAR));
      editMenu.add(getAnyAction(MENU_EDIT_SELECTALL));
      editMenu.add(getAnyAction(MENU_EDIT_DESELECTALL));
   }

   private void createApplicationMenu()
   {
      final IMenuManager appMenu = createMenuManager(MENU_APP);
      getLegacyMenu().add(appMenu);
      
      appMenu.add(new Separator());
      appMenu.add(getAnyAction(MENU_APP_SAVE));
      appMenu.add(getAnyAction(MENU_APP_SAVEAS));

      appMenu.add(new Separator());
      appMenu.add(getAnyAction(PSXmlAppImportAction.XMLAPP_IMPORT));
      appMenu.add(getAnyAction(PSXmlAppExportAction.XMLAPP_EXPORT));
      
      appMenu.add(new Separator());
      appMenu.add(getAnyAction(MENU_APP_PAGE_SETUP));
      appMenu.add(getAnyAction(MENU_APP_PRINT));

      appMenu.add(new Separator());
      appMenu.add(getAnyAction(MENU_APP_PROPERTIES));
      appMenu.add(getAnyAction(XMLAPP_ENABLE));
      appMenu.add(getAnyAction(XMLAPP_DISABLE));

      appMenu.add(new Separator());
   }

   /**
    * Returns existing legacy menu root.
    */
   private IMenuManager getLegacyMenu()
   {
      final IMenuManager legacyMenu = getMenuManager().findMenuUsingPath(MENU_LEGACY);
      assert legacyMenu != null;
      return legacyMenu;
   }
   
   /**
    * This method creates all of the actions that are supported by the main
    * frame directly, and all the actions that are supported for UI
    * consistency. Actions in the latter category are actually performed by
    * the child windows of the main frame.
    * <p>
    * By default, all actions not directly supported by the main frame are
    * grayed out unless a child window has registered a listener for the action.
    * <p>
    * The following algorithm is used for naming resource keys:
    * <ul>
    * <li> Main menu items have a key name of the form menu<MenuName>. </li>
    * <li> Sub menus have a key name of the form menu<MenuName><SubMenuName>. </li>
    * <li> Mnemonic chars have a key of the form mn_<menuitem name> </li>
    * <li> Accelerator keys have a key of the form ks_<menuitem name> </li>
    * <li> Icon image file names have a key of the form gif_<menuitem name> </li>
    * <li> Tool tip text has a key of the form tt_<menuitem name> </li>
    * </ul>
    * For each action, there may be an entry for each of the attributes.
    * <p>
    * When adding new actions, up to 4 steps are required:
    * <ul>
    * <li> Add the desired attributes to the resource bundle </li>
    * <li> Create the action in this method </li>
    * <li> Optionally add the item in the desired position in the menu bar
    *       by calling getAnyAction(strActionName). (Override the defaults if
    *       desired> </li>
    * <li> Optionally add the item in the desired position in the tool bar (/li>
    * </ul>
    *
    * @throws MissingResourceException if image files needed for icons can't
    * be found
    */
   private void createActions()
   {
      //*******************************
      // Frame-independent supported actions
      //*******************************
//      initializeSupportedAction(PSXmlAppImportAction.XMLAPP_IMPORT, new PSXmlAppImportAction());
      initializeSupportedAction(MENU_APP_PAGE_SETUP,
            new Action()
            {
               @Override
               public void run()
               {
                  SwingUtilities.invokeLater(new Runnable()
                  {
                     public void run()
                     {
                        getMainFrame().actionPageSetup();
                     }
                  });
               }
            });

      initializeSupportedAction(MENU_APP_PRINT,
            new Action()
            {
               @Override
               public void run()
               {
                  getMainFrame().actionPrint(getActiveEditor());
               }
            });
      initializeSupportedAction(MENU_TOOLS_OPTIONS,
            new Action()
            {
               @Override
               public void run()
               {
                  SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              getMainFrame().actionEditOptions();
                           }
                        });
               }
            }
      );
      initializeSupportedAction(MENU_HELP_HELP,
            new Action()
            {
               @Override
               public void run()
               {
                  SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              getMainFrame().actionHelp();
                           }
                        });
               }
            }
      );

      initializeSupportedAction(MENU_HELP_HELP,
            new Action()
            {
               @Override
               public void run()
               {
                  SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              getMainFrame().actionHelp();
                           }
                        });
               }
            }
      );
      initializeSupportedAction(MENU_HELP_START_TUTORIAL,
            new Action()
            {
               @Override
               public void run()
               {
                  SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              getMainFrame().actionHelpStartTutorial();
                           }
                        });
               }
            }
      );
      
      initializeSupportedAction(MENU_HELP_ABOUT,
            new Action()
            {
               @Override
               public void run()
               {
                  SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              getMainFrame().actionAbout();
                           }
                        });
               }
            }
      );
      
      initializeSupportedAction(XMLAPP_DISABLE, createToggleStatusAction());
      initializeSupportedAction(XMLAPP_ENABLE, createToggleStatusAction());
      createChildWindowSupportedActions();
   }

   private Action createToggleStatusAction()
   {
      return new Action()
      {
         @Override
         public void run()
         {
            new PSXmlAppStatusAction(null).toggleStatus(getApplicationRef());
            // a workaround to wait until Swing thread from "toggleStatus" comes
            // back. Othewise the button image is not updated whatever I do
            // (Andriy)
            try
            {
               Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
               // ignore
            }
            toggleStartStop();
         }
      };
   }

   /**
    * Actions provided by editor frames.
    */
   private void createChildWindowSupportedActions()
   {
      for (final String id : CHILD_SUPPORTED_ACTIONS)
      {
         final IAction action = new Action()
         {
            @Override
            public void run()
            {
               IAction a = m_actionListeners.get(id);
               Debug.assertTrue(a != null, "Missing action " + id);
               a.run();
            }
         };
         initActionData(id, action);
         m_childSupportedActions.put(id, action);
      }
   }

   /**
    * Initializes action with the resources for specified id and registers it
    * is supported actions list.
    */
   private void initializeSupportedAction(final String id, final IAction action)
   {
      initActionData(id, action);
      m_supportedActions.put(id, action);
   }

   /**
    * Initializes action with the resources for specified id.
    */
   private void initActionData(final String id, final IAction action)
   {
      action.setId(id);
      action.setText(getActionName(id));
      action.setImageDescriptor(ResourceHelper.getIcon2(getResources(), id));
      action.setAccelerator(ResourceHelper.getAccelKey2(getResources(), id));
      action.setToolTipText(
            ResourceHelper.getToolTipText(getResources(), id));
   }
   
   /**
    * Searches both the supported and child supported action lists, returning
    * the action if it is found on either list.
    * <code>null</code> is returned if it is not found.
    */
   private IAction getAnyAction(final String actionId)
   {
      IAction action = m_supportedActions.get(actionId);
      if (action == null)
      {
         action = m_childSupportedActions.get(actionId);
      }
      Debug.assertTrue(action != null, getResources(), "ActionMissing",
            new String[]{actionId});
      return action;
   }

   /**
    * Main {@link UIMainFrame}.
    */
   private UIMainFrame getMainFrame()
   {
      return E2Designer.getApp().getMainFrame();
   }

   /**
    * Returns action name for the specified id.
    */
   private String getActionName(final String id)
   {
      return ResourceHelper.getWithMnemonic(
            getResources().getString(id), getResources(), id);
   }

   /**
    * Creates new menu by specified id.
    *
    * @param menuId The name of a dynamic menu item.
    * @return the dynamic menu item.
    */
   private MenuManager createMenuManager(String menuId)
   {
      final String nameWithMnemonic = ResourceHelper.getWithMnemonic(
            getResources().getString(menuId),
            getResources(), menuId);
      return new MenuManager(nameWithMnemonic, menuId);
   }

   /**
    * The editor to contribute to.
    */
   private PSXmlApplicationEditor getActiveEditor()
   {
      return m_activeEditor;
   }

   @Override
   public void setActiveEditor(IEditorPart targetEditor)
   {
      assert targetEditor instanceof PSXmlApplicationEditor;
      if (getActiveEditor() != null)
      {
         disconnectFromEditor();
      }
      m_activeEditor = (PSXmlApplicationEditor) targetEditor;
      connectToEditor();
   }

   /**
    * Application reference.
    */
   private PSReference getApplicationRef()
   {
      return getActiveEditor().getApplicationRef();
   }

   /**
    * Editor tab folder.
    */
   private CTabFolder getTabFolder()
   {
      return getActiveEditor().getTabFolder();
   }

   /**
    * Menu manager to contribute to.
    */
   public IMenuManager getMenuManager()
   {
      return m_menuManager;
   }
   
   /**
    * Convenience method to access designer resources.
    */
   private static E2DesignerResources getResources()
   {
      return E2Designer.getResources();
   }
   
   /**
    * Creates specification which global actions should be retargeted to
    * the editor actions.
    * @see #GLOBAL_ACTIONS
    */
   private static Map<String, String> createGlobalActions()
   {
      final Map<String, String> actions = new HashMap<String, String>();
      actions.put(MENU_EDIT_CUT, ActionFactory.CUT.getId());
      actions.put(MENU_EDIT_COPY, ActionFactory.COPY.getId());
      actions.put(MENU_EDIT_PASTE, ActionFactory.PASTE.getId());
      actions.put(MENU_EDIT_CLEAR, ActionFactory.DELETE.getId());
      actions.put(MENU_EDIT_SELECTALL, ActionFactory.SELECT_ALL.getId());
      return Collections.unmodifiableMap(actions);
   }

   /**
    * @see #getMenuManager()
    */
   private IMenuManager m_menuManager;
   
   /**
    * Current toolbar manager.
    */
   private IToolBarManager m_toolBarManager;

   /**
    * Current active editor.
    */
   private PSXmlApplicationEditor m_activeEditor;

   /**
    * created and added menus.
    */
   private List<MenuManager> m_menus = new ArrayList<MenuManager>();
   
   /**
    * Listener tracking editor page change. 
    */
   private final SelectionAdapter m_editorFolderSelectionListener =
      new SelectionAdapter()
      {
         @Override
         public void widgetSelected(
               @SuppressWarnings("unused") final SelectionEvent e)
         {
            doContributeToMenu();
         }
      };
      
   /**
    * Static actions.
    */
   private Map<String, IAction> m_supportedActions = new HashMap<String, IAction>();
   
   /**
    * Actions for implementations provided by a child window.
    */
   private Map<String, IAction> m_childSupportedActions = new HashMap<String, IAction>();
   
   /**
    * Actions provided by editor frame.
    */
   private Map<String, IAction> m_actionListeners = new HashMap<String, IAction>();

   /*================ MENUS ================*/
   /**
    * Main legacy UI menu item id.
    */
   public static final String MENU_LEGACY = "menuLegacy";

   /**
    * These strings represent menu items that are always present that may be
    * supported by child frames. When a child frame is activated, if it wants
    * to handle the action, it should register a listener for the specified
    * action using addActionListener(...). All added listeners will automatically
    * be removed when the frame loses activation. Therefore, registration must
    * be repeated each time the window is activated.
    * <p>
    * The child frame is responsible for [un]graying of each menu item it
    * registers a handler for. To [en/dis]able the actions, use getActions()
    * and set the enabled property. This will automatically gray/ungray all
    * ui components associated with that action.
    */
   public static final String MENU_APP = "menuApp";
   private static final String MENU_APP_PAGE_SETUP = "menuAppPageSetup";
   private static final String MENU_APP_PRINT = "menuAppPrint";
   public static final String MENU_APP_PROPERTIES = "menuAppProperties";
   public static final String MENU_APP_SAVE = "menuAppSave";
   public static final String MENU_APP_SAVEAS = "menuAppSaveAs";
   public static final String MENU_APP_SECURITY = "menuAppSecurity";
   private static final String MENU_EDIT = "menuEdit";
   public static final String MENU_EDIT_CLEAR = "menuEditClear";
   public static final String MENU_EDIT_COPY = "menuEditCopy";
   public static final String MENU_EDIT_CUT = "menuEditCut";
   public static final String MENU_EDIT_DESELECTALL = "menuEditDeselectAll";
   public static final String MENU_EDIT_PASTE = "menuEditPaste";
   public static final String MENU_EDIT_SELECTALL = "menuEditSelectAll";

   /*
    * Andriy: help menu is removed, so we may remove these constants and rest of
    * associated Help code after Eclipse help is integrated.
    */
   private static final String MENU_HELP_ABOUT = "menuHelpAbout";
   private static final String MENU_HELP_HELP = "menuHelpHelp";
   private static final String MENU_HELP_START_TUTORIAL = "menuHelpStartTutorial";

//   private static final String MENU_TOOLS = "menuTools";
   private static final String MENU_TOOLS_OPTIONS = "menuToolsOptions";
   public static final String MENU_VIEW = "menuView";
   
   /**
    * Andriy: disabled menu, so probably need to remove this constant and
    * the associated code.
    */
   public static final String MENU_VIEW_PROPERTIES = "menuViewProperties";

   /**
    * Actions supported by editors.
    */
   private static final String[] CHILD_SUPPORTED_ACTIONS =
   {
      MENU_APP_SAVE,
      MENU_APP_SAVEAS,
      PSXmlAppExportAction.XMLAPP_EXPORT,
      MENU_APP_SECURITY,
      MENU_APP_PROPERTIES,

      MENU_EDIT_CUT,
      MENU_EDIT_COPY,
      MENU_EDIT_PASTE,
      MENU_EDIT_CLEAR,
      MENU_EDIT_SELECTALL,
      MENU_EDIT_DESELECTALL,

      PSXmlAppDebuggingAction.XMLAPP_DEBUGGING,

      MENU_VIEW_PROPERTIES,
   };
   
   /**
    * XML editor actions for global actions to retarget to.
    * Keys - application editor action id, values - id of the corresponding
    * global action. 
    */
   private static final Map<String, String> GLOBAL_ACTIONS =
         createGlobalActions(); 

   /*
    * These strings represent the names of the main menu items that support
    * dynamic actions. When a child frame is activated, it will be queried
    * as to whether it wants to add any actions for the supplied main menu.
    * If the action has an icon, a button will be created and added to a
    * toolbar.
    */
   /**
    * Insert menu item. 
    */
   public static final String MENU_INSERT = "menuInsert";
   
   /**
    * Contributes actions for embedded source editor.
    */
   private final PSTextEditorActionBarContributorHelper m_editorHelper =
         new PSTextEditorActionBarContributorHelper();
}

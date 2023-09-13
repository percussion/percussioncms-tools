/******************************************************************************
 *
 * [ PSPackagerMainFrame.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.PSAboutDialog;
import com.percussion.packager.ui.data.PSPackageDescriptorMetaInfo;
import com.percussion.packager.ui.data.PSProgressStatus;
import com.percussion.packager.ui.data.PSServerRegistration;
import com.percussion.packager.ui.managers.PSServerConnectionManager;
import com.percussion.packager.ui.model.IPSPackagerClientModelListener;
import com.percussion.packager.ui.model.PSPackagerClientModel;
import com.percussion.packager.ui.model.PSPackagerClientModel.ChangeEventTypes;
import com.percussion.packagerhelp.PSEclHelpManager;
import com.percussion.util.PSFormatVersion;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The main frame for the package builder tool.
 * @author erikserating
 */
public class PSPackagerMainFrame extends JFrame implements IPSPackagerClientModelListener
{
   
   
   /**
    * @throws HeadlessException
    */
   public PSPackagerMainFrame() throws HeadlessException
   {
      m_model = new PSPackagerClientModel();
      m_model.addPackagerClientModelListener(this);
      init();
      
   }
   
   /**
    * Updates the packager title string to reflect current server
    * connection status.
    */
   public void refreshTitle()
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      setTitle(connMgr.getConnectionTitleString(
         getResourceString("title")));
      
      refreshRecentMenuItems();
   }
   
   /**
    * Get the packager client model.
    * @return the model, never <code>null</code>.
    */
   public PSPackagerClientModel getModel()
   {
      return m_model;
   }
   
   /**
    * Initialize and layout the packager main frame.
    */
   private void init()
   {       
      initMenus();
      refreshTitle();
      
      URL imageFile = getClass().getResource(getResourceString("title.image"));
      if (imageFile != null)
      {
         ImageIcon icon = new ImageIcon(imageFile);
         setIconImage(icon.getImage());
      }      
      MigLayout layout = new MigLayout(
         "fill",
         "[]",
         "[]");
      m_mainPanel = new JPanel();
      getContentPane().setLayout(layout);
      m_cardLayout = new SpecialCardLayout(); 
      m_mainPanel.setLayout(m_cardLayout);
      
      
      m_selectionPanel = new PSPackageSelectionPanel();
      m_selectionPanel.addListSelectionListener(
         new ListSelectionListener()
         {

            public void valueChanged(@SuppressWarnings("unused")
               ListSelectionEvent event)
            {               
               handleMenuItemState();
            }
            
         });
      m_tabbedPanel = createTabbedPanel();
            
      m_mainPanel.add(m_selectionPanel, "SELECTION");
      m_mainPanel.add(m_tabbedPanel, "TABS");
      getContentPane().add(m_toolBar, "north, hidemode 3");
      getContentPane().add(m_mainPanel,  "grow");
      setSize(850, 650);
      handleToolBarButtons();
      handleMenuItemState();
      addF1KeyListeners(this);      
      PSUiUtils.center(this);
      
   }
   
   /**
    * Create the tabbed panel and all its pages.
    * @return the panel , never <code>null</code>.
    */
   private JTabbedPane createTabbedPanel()
   {
      JTabbedPane panel = new JTabbedPane();
      
      panel.setTabPlacement(JTabbedPane.BOTTOM);
      m_generalPage = new PSGeneralPage();
      panel.addTab(getResourceString("tab.general"), (JPanel)m_generalPage);
      panel.setMnemonicAt(0, getResourceString("tab.general.m").charAt(0));
      
      m_selectionPage = new PSSelectionPage();
      panel.addTab(getResourceString("tab.selection"),
         (JPanel)m_selectionPage);
      panel.setMnemonicAt(1, getResourceString("tab.selection.m").charAt(0));
      
      m_dependsPage = new PSDependenciesPage();
      panel.addTab(getResourceString("tab.dependencies"),
         (JPanel)m_dependsPage);
      panel.setMnemonicAt(2, getResourceString("tab.dependencies.m").charAt(0));
      panel.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent event)
         {
            if(m_ignoreTabSelection)
            {
               m_ignoreTabSelection = false;
               return;
            }
            JTabbedPane pane = (JTabbedPane)event.getSource();
            int tabIndex = pane.getSelectedIndex();            
            IPSPage page = (IPSPage)pane.getComponentAt(m_lastTabIndex);
            List<String> errors = page.validateData();
            if(errors != null && !errors.isEmpty())
            {
               m_ignoreTabSelection = true;
               pane.setSelectedIndex(m_lastTabIndex);
               //show errors               
               displayValidationErrors(errors);
               
               if (m_lastTabIndex == 0)
               {
                  ((PSGeneralPage)m_generalPage).focusAndSelectPackageName();
               }
               
               if(m_lastTabIndex == 1)
               {
                  ((PSSelectionPage)m_selectionPage)
                     .focusAndSelectFilterTextField();
               }
                  
               return;
            }
            updateData();
            if(tabIndex == 2)
               handleDependenciesPageDisplay();
            else
               loadData();
            
            m_lastTabIndex = tabIndex;
            
         }
      
      });
      return panel;
   }
   
   /**
    * Helper method to invoke dependency calculation and
    * page reload when switching to the dependency page.
    */
   private void handleDependenciesPageDisplay()
   {
      Runnable r = new Runnable()
      {
         public void run()
         {
            m_model.calculateDependencies(true);
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  loadData();
               }
            });
            
         }
         
      };
      Thread t = new Thread(r);
      t.start();
      
   }
   
   /**
    * Displays validation errors in a dialog as a bulleted list.
    * @param errors may be <code>null</code> or empty in which
    * case no dialog will appear.
    */
   private void displayValidationErrors(List<String> errors)
   {
      if(errors == null || errors.isEmpty())
         return;
      StringBuilder msg = new StringBuilder();
      boolean isFirst = true;
      boolean useBullets = errors.size() > 1;
      for(String error : errors)
      {
         if(!isFirst)
            msg.append("\n");
         if(useBullets)
         {
            msg.append('\u2022');
            msg.append(' ');
         }         
         msg.append(error);
         isFirst= false;
         
      }
      PSPackagerClient.getErrorDialog().showErrorMessage(
         msg.toString(), getResourceString("title.validation.errors"));
   }
   
   /**
    * Initialize all menus.
    */
   private void initMenus()
   {
      m_menuBar = new JMenuBar();
      m_packageMenu = createPackageMenu();
      m_menuBar.add(m_packageMenu);
      m_serverMenu = createServerMenu();
      m_menuBar.add(m_serverMenu);      
      m_menuBar.add(createHelpMenu());
      setJMenuBar(m_menuBar);
      m_toolBar = createToolbar();
      
   }
   
   /**
    * Switches the card layout from the home page view to
    * the tabbed editor view.
    * @param isHome
    */
   private void switchCards(boolean isHome)
   {
      if(isHome)
      {
         m_menuBar.setVisible(true);
         m_toolBar.setVisible(false);
         m_cardLayout.first(m_mainPanel);
         m_homePage = true;
      }
      else
      {
         m_menuBar.setVisible(false);
         m_toolBar.setVisible(true);
         m_cardLayout.last(m_mainPanel);
         m_homePage = false;
      }
   }
   
   /**
    * Dynamically adds/removes recent connection menu items from
    * the server menu to keep them in synch with the actual recent
    * connection list. Also updates the more menu.
    */
   private void refreshRecentMenuItems()
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      List<String> recentConns = connMgr.getRecentConnections();
      for(JMenuItem ml : m_recentMenuItems)
      {
         m_serverMenu.remove(ml);
      }
      m_recentMenuItems.clear();
      int idx = 0;
      for(String recent : recentConns)
      {
         if(StringUtils.isBlank(recent))
            break;
         ActionListener al = new RecentConnActionListener(recent);
         JMenuItem item = PSUiUtils.createMenuItem(recent, (char)0,
            null, "Connect to " + recent,
            null, null, null, al);
         m_recentMenuItems.add(item);
         m_serverMenu.insert(item, idx++);
      }
      
      //update more connections menu
      m_connectionMoreMenu.removeAll();
      Iterator<PSServerRegistration> it = connMgr.getServers().getServers();
      boolean hasItems = false;
      while(it.hasNext())
      {
         String hostport = it.next().toString();
         if(!recentConns.contains(hostport))
         {
            ActionListener al = new RecentConnActionListener(hostport);
            JMenuItem item = PSUiUtils.createMenuItem(hostport, (char)0,
               null, getResourceString("text.connect.to") + " " + hostport,
               null, null, null, al);
            m_connectionMoreMenu.add(item);
            hasItems = true;
         }
      }
      m_connectionMoreMenu.setVisible(hasItems);     
      
   }
   
   /**
    * Call update data methods for all pages.
    */
   private void updateData()
   {
      m_generalPage.update(m_model);
      m_selectionPage.update(m_model);
      m_dependsPage.update(m_model);
   }
   
   /**
    * Call load data methods for all pages.
    */
   private void loadData()
   {
      m_generalPage.load(m_model);
      m_selectionPage.load(m_model);
      m_dependsPage.load(m_model);
   }
   
   /**
    * Auto connects to latest recent connection or
    * displays server connection manager if no connections
    * found.
    */
   public void initialServerConnect()
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      List<String> recent = connMgr.getRecentConnections();
      if(recent.isEmpty())
      {
         onOpenServerManager();
      }
      else
      {
         PSServerRegistration server = 
            connMgr.getServerByHostPortString(recent.get(0));
         try
         {
            m_model.connect(server);
         }
         catch (Exception e)
         {
            PSPackagerClient.getErrorDialog().showError(e, true,
               PSResourceUtils.getCommonResourceString("errorTitle")); 
         }
      }
   }      
   
   /**
    * Check if model is dirty and ask to save if it is dirty.
    */
   private boolean checkIfDirtyShouldSave()
   {
      if(m_model.isDirty())
      {
         int confirm = 
            JOptionPane.showConfirmDialog(this,
               getResourceString("msg.confirm.save.descriptor"),
               getResourceString("title.confirm.save.descriptor"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE);
         if(confirm == JOptionPane.YES_OPTION)
         {            
            return true;
         }
         else
         {
            m_model.setAsClean();
            return false;
         }
      }
      return false;
   }   
   
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.packager.ui.model.IPSPackagerClientModelListener#
    *      modelChanged(com.percussion.packager.ui.model.PSPackagerClientModel.ChangeEventTypes)
    */
   public void modelChanged(final ChangeEventTypes type, final Object extra)
   {
      if (type == ChangeEventTypes.BUILD_DESCRIPTOR)
      {

      }
      if (type == ChangeEventTypes.DELETE_DESCRIPTOR)
      {
         m_selectionPanel.refreshTable();
         m_selectionPanel.handleButtonState();
      }
      if (type == ChangeEventTypes.DIRTY_STATE_CHANGE)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               handleToolBarButtons();
            }
         });
      }
      if (type == ChangeEventTypes.EDIT_DESCRIPTOR)
      {
         try
         {
            SwingUtilities.invokeAndWait(new Runnable()
            {
               public void run()
               {                  
                  boolean reloadPages = extra == null ?
                     true : 
                     ((Boolean)extra).booleanValue();
                  if(reloadPages)
                     loadData();
                  m_tabbedPanel.setSelectedComponent((JPanel)m_generalPage);
                  ((PSSelectionPage)m_selectionPage).setSelectedOnly(false);
                  ((PSSelectionPage)m_selectionPage).clearFilterField();
                  switchCards(false);
               }
            });
         }
         catch (Exception e)
         {
            PSPackagerClient.getErrorDialog().showError(e, true,
               PSResourceUtils.getCommonResourceString("errorTitle")); 
         }             
         
      }
      if (type == ChangeEventTypes.FORCE_PAGE_LOAD)
      {
         try
         {
            SwingUtilities.invokeAndWait(new Runnable()
            {
               public void run()
               {               
                  loadData();              
               }
            });
         }
         catch (Exception e)
         {
            PSPackagerClient.getErrorDialog().showError(e, true,
               PSResourceUtils.getCommonResourceString("errorTitle"));
         }
         
      }      
      if (type == ChangeEventTypes.ERROR)
      {          
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               Object e = extra;
               if(e instanceof String)
               {
                  PSPackagerClient.getErrorDialog().showErrorMessage(
                     e.toString(), 
                     PSResourceUtils.getCommonResourceString("errorTitle"));
               }
               else if(e instanceof Exception)
               {
                  PSPackagerClient.getErrorDialog().showError(
                     (Exception)e, true,
                     PSResourceUtils.getCommonResourceString("errorTitle")); 
               }
               setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }            
         });
      }
      if (type == ChangeEventTypes.NEW_DESCRIPTOR)
      {
         ((PSSelectionPage)m_selectionPage).clearSelectedOnlyCheckBox();
         loadData();
         m_tabbedPanel.setSelectedComponent((JPanel)m_generalPage); 
         ((PSSelectionPage)m_selectionPage).setSelectedOnly(false);
         switchCards(false);  
         ((PSGeneralPage)m_generalPage).focusAndSelectPackageName();
      }
      if (type == ChangeEventTypes.SAVE_DESCRIPTOR)
      {
         
      }
      if (type == ChangeEventTypes.SERVER_CONNECT)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               refreshTitle();
               m_selectionPanel.refreshTable();
               m_selectionPanel.handleButtonState();
               handleMenuItemState();
            }            
         });         
      }
      if (type == ChangeEventTypes.SERVER_DISCONNECT)
      {
         refreshTitle();
         m_selectionPanel.clearTable();
         m_selectionPanel.handleButtonState();
         handleMenuItemState();
      }
      if (type == ChangeEventTypes.PROGRESS_UPDATE)
      {
         handleProgress((PSProgressStatus)extra);
      }
      if (type == ChangeEventTypes.WARNING)
      {          
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               Object[] obj = (Object[])extra;
               if(obj != null)
               {
                  String key = (String)obj[0];
                  Object[] args = (Object[])obj[1];
                  String msg = MessageFormat.format(getResourceString(key),
                     args);
                  JOptionPane.showMessageDialog(PSPackagerClient.getFrame(),
                     msg, PSResourceUtils.getCommonResourceString("warningTitle"),
                     JOptionPane.WARNING_MESSAGE);
                  
               }
                            
            }            
         });
      }
      if (type == ChangeEventTypes.INFO)
      {          
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               
               if(extra != null)
               {
                  String msg = null;
                  if(extra instanceof String)
                  {
                     msg = (String)extra;
                  }
                  else
                  {
                     Object[] obj = (Object[])extra;
                     String key = (String)obj[0];
                     Object[] args = (Object[])obj[1];
                     msg = MessageFormat.format(getResourceString(key),
                        args);
                  }
                  
                  JOptionPane.showMessageDialog(PSPackagerClient.getFrame(),
                     msg, PSResourceUtils.getCommonResourceString("infoTitle"),
                     JOptionPane.INFORMATION_MESSAGE);
                  
               }
                            
            }            
         });
      }
   }
   
   /**
    * Handle tool bar buttons enabled state.
    */
   private void handleToolBarButtons()
   {
      m_saveButton.setEnabled(m_model.isDirty());
      m_buildButton.setEnabled(
         !m_model.isDirty() && !m_model.isDescriptorNew());
   }
   
   /**
    * Handle enable/disabled state of menu items.
    */
   private void handleMenuItemState()
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSDeploymentServerConnection conn = connMgr.getConnection();
      
      m_disconnectMenuItem.setEnabled(conn != null && conn.isConnected());
      
      boolean hasSelection = 
         m_selectionPanel.getSelectedDescriptorInfo() != null;
      
      boolean isConnected = connMgr.getConnection() != null &&
         connMgr.getConnection().isConnected();
      m_newMenuItem.setEnabled(isConnected);
      m_editMenuItem.setEnabled(hasSelection);
      m_deleteMenuItem.setEnabled(hasSelection);
      m_buildMenuItem.setEnabled(hasSelection);
      m_exportMenuItem.setEnabled(hasSelection);
      
   }
   
   /**
    * Create the server menu.
    * @return menu, never <code>null</code>.
    */
   private JMenu createServerMenu()
   {
      JMenu servermenu = new JMenu(getResourceString("menu.servers"));
      servermenu.setMnemonic(getResourceString("menu.servers.m").charAt(0));
            
      
      m_connectionMoreMenu = 
         new JMenu(getResourceString("menu.more.connections"));
      servermenu.add(m_connectionMoreMenu);
      servermenu.addSeparator();
      ActionListener disconnectListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            try
            {
               m_model.disconnect();
            }
            catch (PSDeployException de)
            {
               PSPackagerClient.getErrorDialog().showError(de, true,
                  PSResourceUtils.getCommonResourceString("errorTitle")); 
            }               
         }         
      };
      m_disconnectMenuItem = createMenuItem("menu.item.disconnect",
         disconnectListener);
      servermenu.add(m_disconnectMenuItem);
      servermenu.addSeparator();
      ActionListener smListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onOpenServerManager();            
         }         
      };
      
      servermenu.add(createMenuItem("menu.item.connmanager", smListener));
      
      
              
      return servermenu;
   }
   
   /** 
    * Helper method to create the package menu.
    */
   private JMenu createPackageMenu()
   {
      JMenu menu = new JMenu(getResourceString("menu.package"));
      menu.setMnemonic(getResourceString("menu.package.m").charAt(0));
      
      ActionListener newListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            try
            {
               m_selectionPanel.onNew(m_model);
            }
            catch (PSDeployException de)
            {
               PSPackagerClient.getErrorDialog().showError(
                 de, true,
                 PSResourceUtils.getCommonResourceString("errorTitle")); 
            }      
         }         
      };
      m_newMenuItem = createMenuItem("menu.item.new", newListener);
      menu.add(m_newMenuItem);
      
      ActionListener editListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {            
               PSPackageDescriptorMetaInfo info = 
                  m_selectionPanel.getSelectedDescriptorInfo();
               if(info != null)
                  m_selectionPanel.onEdit(info, m_model);               
         }         
      };      
      m_editMenuItem = createMenuItem("menu.item.edit", editListener);
      menu.add(m_editMenuItem);
      
      ActionListener deleteListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            try
            {
               PSPackageDescriptorMetaInfo info = 
                  m_selectionPanel.getSelectedDescriptorInfo();
               if(info != null)
                  m_selectionPanel.onDelete(info, m_model);
            }
            catch (PSDeployException de)
            {
               PSPackagerClient.getErrorDialog().showError(
                 de, true,
                 PSResourceUtils.getCommonResourceString("errorTitle")); 
            }      
         }         
      };
      m_deleteMenuItem = createMenuItem("menu.item.delete", deleteListener);
      menu.add(m_deleteMenuItem);
      
      ActionListener buildListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {            
               PSPackageDescriptorMetaInfo info = 
                  m_selectionPanel.getSelectedDescriptorInfo();
               if(info != null)
                  m_selectionPanel.onBuild(info, m_model);               
         }         
      };      
      m_buildMenuItem = createMenuItem("menu.item.build", buildListener);
      menu.add(m_buildMenuItem);
      
      ActionListener exportListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {            
               PSPackageDescriptorMetaInfo info = 
                  m_selectionPanel.getSelectedDescriptorInfo();
               if(info != null)
                  m_selectionPanel.onExport(info, m_model);               
         }         
      };      
      m_exportMenuItem = createMenuItem("menu.item.export", exportListener);
      menu.add(m_exportMenuItem);
      
      menu.addSeparator();
      ActionListener exitListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            actionExit();      
         }         
      };
      menu.add(createMenuItem("menu.item.exit", exitListener));
      
      return menu;
   }
   
   /**
    * 
    * @return
    */
   private JMenu createHelpMenu()
   {
      JMenu helpmenu = new JMenu(getResourceString("menu.help"));
      helpmenu.setMnemonic(getResourceString("menu.help.m").charAt(0));
      JMenuItem helpMenuItem = createMenuItem("menu.item.helpcontents", null);
      helpMenuItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") 
               ActionEvent event)
         {            
            onHelp();
         }
         
      });
      helpmenu.add(helpMenuItem);
      JMenuItem aboutMenuItem = createMenuItem("menu.item.about", null);
      aboutMenuItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") 
               ActionEvent event)
         {            
            onAbout();
         }
         
      });
      helpmenu.add(aboutMenuItem);
      return helpmenu;
   }
   
   /**
    * Create the toolbar and all its buttons.
    * @return the toolbar, never <code>null</code>.
    */
   private JToolBar createToolbar()
   {
      JToolBar toolbar = new JToolBar();
      toolbar.setFloatable(false);
      m_saveButton = new ToolBarButton(
         getClass().getResource(getResourceString("button.save.image")));
      m_saveButton.setToolTipText(getResourceString("button.save.tt"));
      toolbar.add(m_saveButton);
      m_saveButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
            ActionEvent event)
         {            
            final boolean valid = validateCurrentPage();
            Runnable r = new Runnable()
            {
               public void run()
               {
                  try
                  {
                     if (valid)
                     {
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {
                              updateData();                           
                           }
                        });
                                            
                        m_model.saveDescriptor();
                        
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {
                              m_selectionPanel.refreshTable();
                              PSPackageDescriptorMetaInfo info = m_model.getDescriptorMetaInfo();
                              if(info != null)
                                 m_selectionPanel.select(info);
                              m_selectionPanel.handleButtonState();                            
                           }
                        });
                        
                     }                     
                  }
                  catch (final Exception e)
                  {
                     SwingUtilities.invokeLater(new Runnable()
                     {
                        public void run()
                        {
                           PSPackagerClient.getErrorDialog().showError(
                              e, true,
                              PSResourceUtils.getCommonResourceString("errorTitle"));                              
                        }
                     });
                  }
               }};
            
               Thread t = new Thread(r);
               t.start();
         }
      });
      m_buildButton = new ToolBarButton(
         getClass().getResource(getResourceString("button.build.image")));
      m_buildButton.setToolTipText(getResourceString("button.build.tt"));
      toolbar.add(m_buildButton);
      m_buildButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
            ActionEvent event)
         {           
            //Ask for target dir, quit if cancel
            PSTargetDirectoryDialog dialog = 
               new PSTargetDirectoryDialog(PSPackagerMainFrame.this, null, false);
            if(dialog.showTargetDirectoryDialog())
            {
               m_model.setLocalPkgDir(dialog.getPath());
            }
            else
            {
               return;
            }
            
            final PSPackageDescriptorMetaInfo info = 
               m_selectionPanel.getSelectedDescriptorInfo();
            if(info == null)
               return;
            final boolean shouldSave = checkIfDirtyShouldSave();
            final boolean valid = validateCurrentPage();
            
            
            Runnable r = new Runnable()
            {
               public void run()
               {
                  try
                  {
                     if (shouldSave && valid)
                     {
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {
                              updateData();                           
                           }
                        });
                                            
                        m_model.saveDescriptor();
                     }
                     m_model.build(info);                     
                     
                  }
                  catch (final Exception e)
                  {
                     SwingUtilities.invokeLater(new Runnable()
                     {
                        public void run()
                        {
                           PSPackagerClient.getErrorDialog().showError(
                              e, true,
                              PSResourceUtils.getCommonResourceString("errorTitle"));                              
                        }
                     });
                  }                  
               }
            };
            Thread t = new Thread(r);
            t.start();
         }
      });      
      m_homeButton = new ToolBarButton(
         getClass().getResource(getResourceString("button.home.image")));
      m_homeButton.setToolTipText(getResourceString("button.home.tt"));
      toolbar.add(m_homeButton);
      
      m_cancelButton = new ToolBarButton(
         getClass().getResource(getResourceString("button.cancel.image")));
      m_cancelButton.setToolTipText(getResourceString("button.cancel.tt"));
      toolbar.add(m_cancelButton);

      ActionListener homeCancelActionListener = new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent event)
         {
            final boolean isNewAndClean = m_model.isDescriptorNew()
                     && !m_model.isDirty();
            final boolean shouldSave = checkIfDirtyShouldSave();
            final boolean valid = isNewAndClean || !shouldSave 
               ? false : validateCurrentPage();
            if(shouldSave && !valid)
               return;
            if (valid)
            {
               updateData();
            }
            else
            {
               loadData();
            }
            Runnable r = new Runnable()
            {
               public void run()
               {
                  try
                  {
                     if (shouldSave && valid)
                     {
                        m_model.saveDescriptor();
                     }
                     SwingUtilities.invokeAndWait(new Runnable()
                     {
                        public void run()
                        {
                           refreshTitle();
                           m_selectionPanel.refreshTable();
                           PSPackageDescriptorMetaInfo info = m_model
                                    .getDescriptorMetaInfo();
                           if (info != null)
                              m_selectionPanel.select(info);
                           m_selectionPanel.handleButtonState();
                           switchCards(true);
                           m_selectionPanel.requestFocusInWindow();
                        }

                     });

                  }
                  catch (final Exception e)
                  {
                     SwingUtilities.invokeLater(new Runnable()
                     {
                        public void run()
                        {
                           PSPackagerClient
                                    .getErrorDialog()
                                    .showError(
                                             e,
                                             true,
                                             PSResourceUtils
                                                      .getCommonResourceString("errorTitle"));
                        }
                     });
                  }
               }
            };
            Thread t = new Thread(r);
            t.start();
         }
      };
      m_homeButton.addActionListener(homeCancelActionListener);  
      m_cancelButton.addActionListener(homeCancelActionListener);
      
      m_helpButton = new ToolBarButton(
         getClass().getResource(getResourceString("button.help.image")));
      m_helpButton.setToolTipText(getResourceString("button.help.tt"));
      toolbar.add(m_helpButton);
      m_helpButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
            ActionEvent event)
         {
            onHelp();
         }
      });      
      toolbar.setVisible(false);
      return toolbar;
   }
   
   /**
    * Validate current tabbed page.
    * @return <code>true</code> if validation passed.
    */
   private boolean validateCurrentPage()
   {
      IPSPage page = (IPSPage)m_tabbedPanel.getSelectedComponent();
      List<String> errors = page.validateData();
      if(errors != null && !errors.isEmpty())
      {
         displayValidationErrors(errors);
         requestFocusInWindow();

         return false;
      }
      return true;
   }
   
   /**
    * Helper method to create a menu item from the resource bundle
    * info of the specified key and with the action listener being this
    * object.
    * @return the menu item.
    */
   private JMenuItem createMenuItem(String resourcename, ActionListener listener)
   {
      String name = getResourceString(resourcename);
      String accel = getResourceString(resourcename + ".acc");
      String mnemonic = getResourceString(resourcename + ".m");
      String tooltip = getResourceString(resourcename + ".tt"); 
      JMenuItem item = new JMenuItem(name);
      item.setToolTipText(tooltip);
      if(mnemonic.length() == 1)
         item.setMnemonic(mnemonic.charAt(0));
      if(!accel.equals(resourcename + ".acc"))
         item.setAccelerator(KeyStroke.getKeyStroke(accel));
      if(listener != null)
         item.addActionListener(listener);
      return item;   
   }
   
   /**
    * Handles launching and updating the progress monitor
    * based on the passed in progress status.
    * @param status the progress status object, assumed not <code>
    * null</code>.
    */
   private void handleProgress(final PSProgressStatus status)
   {
      SwingUtilities.invokeLater(new Runnable()
      {

         public void run()
         {
            final PSPackagerMainFrame frame =  PSPackagerClient.getFrame();
            if(status.getType() == PSProgressStatus.TYPE.START)
            {
               frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               if(m_progressBox != null)
                  m_progressBox.close();
               
                  m_progressBox = new PSProgressBox(
                     frame,
                     status.getMessage(), status.isEnableCancel());
                  m_progressBox.addCancelListener(new PSProgressBox.CancelListener()
                     {
                        public void cancelled()
                        {
                           frame.getModel().cancelCurrentOperation();                           
                        }                     
                     }
                           
                  );
                  m_progressBox.open();
            }
            else if(status.getType() == PSProgressStatus.TYPE.UPDATE)
            {
               if(m_progressBox != null)
               {                  
                  m_progressBox.setNote(status.getNote());                  
               }
            }
            else if(status.getType() == PSProgressStatus.TYPE.END)
            {
               if(m_progressBox != null)
               {
                  m_progressBox.close();
                  m_progressBox = null;
               }
               frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
         }
      });
      
   }
   
   /**
    * Launch server manager dialog.
    */
   private void onOpenServerManager()
   {
      PSServerManagerDialog dialog = new PSServerManagerDialog(this);
      dialog.setVisible(true);
   } 
   
   /** 
    * Action taken when about menu item is clicked.
    */
   private void onAbout()
   {
      PSFormatVersion version = new PSFormatVersion("com.percussion.packager.ui");
      
      PSAboutDialog dlg = new PSAboutDialog(this, 
         PSResourceUtils.getCommonResourceString("aboutTitle"),
         version.getVersionString());
      dlg.setVisible(true);
   }
   
   /** 
    * Action taken when help menu item is clicked.
    */
   private void onHelp()
   {
      if (m_homePage)
      {
        PSEclHelpManager.launchHelp(null,"/com.percussion.doc.help.packagebuilder/toc.xml");
      }
      else
      {
         int currentTab = m_tabbedPanel.getSelectedIndex();
         String className = 
            m_tabbedPanel.getComponentAt(currentTab).getClass().getName();
         PSEclHelpManager.launchHelp(null,"/com.percussion.doc.help.packagebuilder/toc.xml");
      }

   }
   
   private void addF1KeyListeners(Component comp)
   {
      //Add self
      comp.addKeyListener(m_f1KeyListener);
      
      //Add children
      if(comp instanceof Container)
      {
         Component[] children = ((Container)comp).getComponents();
         for(Component child : children)
         {
            addF1KeyListeners(child);
         }
      }
   }
   
   /**
    * Gets a resource string from this classes resource bundle.
    * @param key cannot be <code>null</code>.
    * @return the resource string or the key if not found.
    */
   private String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(
         PSPackagerMainFrame.class, key);
   }

   /**
    * Processes the window close event as exit action.
    * 
    * @param e the window event, assumed not to be <code>null</code> as this 
    * method will be called by <code>Swing</code> model when user clicks on the
    * close button of this frame.
    */
   @Override
   protected void processWindowEvent( WindowEvent e )
   {      
      boolean ok = true;
      if (e.getID( ) == WindowEvent.WINDOW_CLOSING)
      {
         ok = actionExit();        
      }
      if(ok)
         super.processWindowEvent( e );
   }
   
   
   
   /**
    * Implements the actions to be done on exit from this application. The
    * actions are:
    * <ol>
    * </ol>
    */
   public boolean actionExit()
   {

      final boolean isNewAndClean = m_model.isDescriptorNew()
               && !m_model.isDirty();
      final boolean shouldSave = checkIfDirtyShouldSave();
      final boolean valid = isNewAndClean || !shouldSave
               ? false
               : validateCurrentPage();
      if (shouldSave && !valid)
         return false;
      if (valid)
      {
         updateData();
      }      
      Runnable r = new Runnable()
      {
         public void run()
         {
            try
            {
               if (shouldSave && valid)
               {
                  m_model.saveDescriptor();
               }
               SwingUtilities.invokeAndWait(new Runnable()
               {
                  public void run()
                  {
                     try
                     {
                        getModel().disconnect();
                     }
                     catch (PSDeployException e)
                     {
                        PSPackagerClient.getErrorDialog().showError(e, true,
                                 PSResourceUtils.getCommonResourceString("errorTitle"));
                     }

                     System.exit(0);
                  }

               });

            }
            catch (final Exception e)
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     PSPackagerClient.getErrorDialog().showError(
                        e, true,
                        PSResourceUtils.getCommonResourceString("errorTitle"));
                  }
               });
            }
         }
      };
      Thread t = new Thread(r);
      t.start();
      
      return true;
    
   }
   
   /**
    * Class to handle actions when the dynamically generated recent connection
    * menu items are clicked.
    * 
    * @author erikserating
    * 
    */
   class RecentConnActionListener implements ActionListener
   {

      public RecentConnActionListener(String hostport)
      {
         mi_hostport = hostport;
      }
      
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(
       * java.awt.event.ActionEvent)
       */
      public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
      {
        
         PSServerConnectionManager connMgr = 
            PSServerConnectionManager.getInstance();
         PSServerRegistration server = 
            connMgr.getServerByHostPortString(mi_hostport);
         if(server != null)
         {
            try
            {
               getModel().connect(server);
            }
            catch (Exception ex)
            {
               PSPackagerClient.getErrorDialog().showError(ex,
                  false, PSResourceUtils.getCommonResourceString("errorTitle"));
            }
            
         }
      }
      
      private String mi_hostport;
      
   }
   
   /**
    * Toolbar button class.
    * @author erikserating
    *
    */
    class ToolBarButton extends JButton
    {      

      public ToolBarButton(Icon icon)
      {
        super(icon);
        setMargin(new Insets(0, 0, 0, 0));
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
      }
      
      public ToolBarButton(URL imageFile)
      {
        this(new ImageIcon(imageFile));
      }     
    } 
    
    /**
     * Extend the CardLayout to force minimum size.
     */
    class SpecialCardLayout extends CardLayout
    {

      /* (non-Javadoc)
       * @see java.awt.CardLayout#minimumLayoutSize(java.awt.Container)
       */
      @Override
      public Dimension minimumLayoutSize(Container parent)
      {
         return new Dimension(400, 100);
      }
       
    }
    
    /**
     * Handle F1 key press to bring up help.
     */
    class F1KeyListener implements KeyListener
    {

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
       */
      public void keyPressed(KeyEvent e)
      {
         if(e.getKeyCode() == 112)
            onHelp();         
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
       */
      public void keyReleased(KeyEvent e)
      {
         // no-op         
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
       */
      public void keyTyped(KeyEvent e)
      {
         // no-op         
      }
       
    }
  
   
   /**
    * Card layout for this frame. Initialized in
    * {@link #init()}, never <code>null</code> after that.
    */
   private CardLayout m_cardLayout;
   
   /**
    * The menu bar for the main frame. Initialized in
    * {@link #initMenus()}, never <code>null</code> after that.
    */
   private JMenuBar m_menuBar;
   
   /**
    * The tool bar for the main frame. Initialized in
    * {@link #initMenus()}, never <code>null</code> after that.
    */
   private JToolBar m_toolBar;
   
   /**
    * The progress box used to show status of long operations.
    */
   private PSProgressBox m_progressBox;
   
   /**
    * The server menu.
    */
   private JMenu m_serverMenu;
   
   /**
    * The package menu.
    */
   private JMenu m_packageMenu;
   
   private JPanel m_mainPanel;
   
   /**
    * The connections more menu.
    */
   private JMenu m_connectionMoreMenu;
   
   private ToolBarButton m_saveButton;
   
   private ToolBarButton m_buildButton;
   
   private ToolBarButton m_homeButton;
   
   private ToolBarButton m_cancelButton;
   
   private ToolBarButton m_helpButton;
   
   /**
    * List of all recent connection menu items. Never <code>null</code>,
    * may be empty.
    */
   private List<JMenuItem> m_recentMenuItems = new ArrayList<JMenuItem>();
   
   private JMenuItem m_disconnectMenuItem;
   
   private JMenuItem m_newMenuItem;
   
   private JMenuItem m_editMenuItem;
   
   private JMenuItem m_deleteMenuItem;
   
   private JMenuItem m_buildMenuItem;
   
   private JMenuItem m_exportMenuItem;
   
   private PSPackageSelectionPanel m_selectionPanel;
   
   private JTabbedPane m_tabbedPanel;
   
   private IPSPage m_generalPage;
   
   private IPSPage m_selectionPage;
   
   private IPSPage m_dependsPage;
   
   /**
    * The last page index the the tabbed pane was on.
    */
   private int m_lastTabIndex = 0;
   
   /**
    * Flag to indicate that tab selection should be ignored by listener.
    */
   private boolean m_ignoreTabSelection = false;
   
   /**
    * The packager client model that represens this UI's data and
    * state.
    */
   private PSPackagerClientModel m_model;
   
   /**
    * The F1 Key Listener.
    */
   private F1KeyListener m_f1KeyListener = new F1KeyListener();

   /**
    * Current card
    */
   private boolean m_homePage = true;
   
}

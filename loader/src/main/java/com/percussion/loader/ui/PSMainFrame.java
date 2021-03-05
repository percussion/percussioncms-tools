/******************************************************************************
 *
 * [ PSMainFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.ui;

import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSAboutDialog;
import com.percussion.guitools.PSResources;
import com.percussion.guitools.ResourceHelper;
import com.percussion.loader.IPSContentTree;
import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.IPSProgressListener;
import com.percussion.loader.IPSStatusListener;
import com.percussion.loader.PSContentLoaderApp;
import com.percussion.loader.PSContentLoaderMgr;
import com.percussion.loader.PSContentSelectorMgr;
import com.percussion.loader.PSContentStatus;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderRepositoryHandler;
import com.percussion.loader.PSLogDispatcher;
import com.percussion.loader.PSLogMessage;
import com.percussion.loader.PSProcessMgr;
import com.percussion.loader.PSProgressEvent;
import com.percussion.loader.PSStatusEvent;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.objectstore.PSLogDef;
import com.percussion.loader.util.BrowserLauncher;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.util.PSFormatVersion;
import com.percussion.util.PSObjectStoreUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;



/**
 * The main frame for the content loader.
 */
public class PSMainFrame extends JFrame implements
   ActionListener, IPSProgressListener, IPSStatusListener
{
   /**
    * 
    */
   private static final long serialVersionUID = -5528757627591683974L;

   /**
    * Initializes the frame
    */
   private PSMainFrame()
   {
      m_res = PSContentLoaderResources.getResources();
      init();
   }

   /**
    * Gets the singleton instance of this class.
    *
    * @return singleton instance of this class, never <code>null</code>.
    */
   public static PSMainFrame getFrame()
   {
      if (ms_frame == null)
         ms_frame = new PSMainFrame();
      ms_frame.pack();
      PSContentDialog.center(ms_frame);
      return ms_frame;
   }

   /**
    * Gets the singleton instance of this class performs no manipulations
    * to the frame.
    *
    * @return singleton instance of this class, never <code>null</code>.
    */
   public static PSMainFrame getFrameNoAction()
   {
      if (ms_frame == null)
         ms_frame = new PSMainFrame();
      return ms_frame;
   }

   /**
    * Accessor to get the current valid PSLoaderDescriptor
    * configuration object READ ONLY.
    *
    * @return PSLoaderDescriptor may be <code>null</code>.
    */
   public PSLoaderDescriptor getDescriptor()
   {
      return m_descriptor;
   }

   /**
    * Intializes the frame's framework.
    * @TODO read all the properties from file like conversion pattern and threshold.
    */
   private void init()
   {

      // Set the icon
      ImageIcon ic = new ImageIcon(getClass().getResource(
          m_res.getString("gif_main")));     
      setIconImage(ic.getImage());
      
      setTitle(m_res.getString("title"));

      // initialize log panel
      Logger logger = Logger.getRootLogger();
      PSLogDispatcher appender =
         (PSLogDispatcher) logger.getAppender(LOG_DISPATCHER);
      m_logPanel = new PSLogPanel(appender);

      m_treePanel = new PSContentTreePanel(this);
      m_statusPanel = new PSStatusView();
      m_metaPanel = new PSMetaDataView();

      // The meta view listeners to tree view selections
      // and updates it's view on what the tree has selected
      m_treePanel.addContentTreeListener(m_metaPanel);
      m_treePanel.addContentTreeModelListener(m_metaPanel);

      //Arrange all the split panes
      m_rightSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
         m_statusPanel, m_metaPanel);
      m_rightSP.setOneTouchExpandable(true);
      m_rightSP.setDividerSize(DEVIDER_SIZE);
      m_topSP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
         new JScrollPane(m_treePanel), m_rightSP);
      m_topSP.setOneTouchExpandable(true);
      m_topSP.setDividerSize(DEVIDER_SIZE);

      m_mainSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_topSP,
         m_logPanel);
      m_mainSP.setOneTouchExpandable(true);
      m_mainSP.setDividerSize(DEVIDER_SIZE);

      m_contentPane  = (JPanel) getContentPane();
      m_contentPane.setLayout(new BorderLayout());

      m_statusBar.setText(" ");
      new PSContentMenuHandler(this);
      initToolbar();

      m_contentPane.add(m_toolBar, BorderLayout.NORTH);
      m_contentPane.add(m_mainSP, BorderLayout.CENTER);
      m_contentPane.add(m_statusBar, BorderLayout.SOUTH);
   }

   /**
    * Adds buttons to the toolbar.
    */
   private void initToolbar()
   {
      ImageIcon ic = new ImageIcon(getClass().getResource(
         m_res.getString("gif_newFile")));
      JButton btn = new JButton();
      btn.setActionCommand(PSContentMenuHandler.MENU_FILE_NEW);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res,
         PSContentMenuHandler.MENU_FILE_NEW));
      btn.addActionListener(this);
      btn.setIcon(ic);
      m_toolBar.add(btn);

      ic = new ImageIcon(getClass().getResource(
         m_res.getString("gif_openFile")));
      btn = new JButton();
      btn.setActionCommand(PSContentMenuHandler.MENU_FILE_OPEN);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res,
         PSContentMenuHandler.MENU_FILE_OPEN));
      btn.addActionListener(this);
      btn.setIcon(ic);
      m_toolBar.add(btn);

      ic = new ImageIcon(getClass().getResource(
         m_res.getString("gif_saveFile")));
      btn = new JButton();
      btn.setActionCommand(PSContentMenuHandler.MENU_FILE_SAVE);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res,
         PSContentMenuHandler.MENU_FILE_SAVE));
      btn.setIcon(ic);
      m_toolBar.add(btn);
      m_toolBar.addSeparator();
      btn.addActionListener(this);

      ic = new ImageIcon(getClass().getResource(m_res.getString("gif_scan")));
      btn = new JButton();
      btn.setActionCommand(PSContentMenuHandler.MENU_ACTIONS_SCAN);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res,
         PSContentMenuHandler.MENU_ACTIONS_SCAN));
      btn.addActionListener(this);
      btn.setIcon(ic);
      m_toolBar.add(btn);

      ic = new ImageIcon(getClass().getResource(m_res.getString("gif_upload")));
      btn = new JButton();
      btn.setActionCommand(PSContentMenuHandler.MENU_ACTIONS_UPLOAD);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res,
         PSContentMenuHandler.MENU_ACTIONS_UPLOAD));
      btn.addActionListener(this);
      btn.setIcon(ic);
      m_toolBar.add(btn);
      m_toolBar.addSeparator();

      ic = new ImageIcon(getClass().getResource(m_res.getString("gif_previous")));
      btn = new JButton();
      btn.setActionCommand(m_previous);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res, m_previous));
      btn.addActionListener(this);
      btn.setIcon(ic);
      m_toolBar.add(btn);

      ic = new ImageIcon(getClass().getResource(m_res.getString("gif_next")));
      btn = new JButton();
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res, m_next));
      btn.setActionCommand(m_next);
      btn.addActionListener(this);
      btn.setIcon(ic);
      m_toolBar.add(btn);

      ic = new ImageIcon(getClass().getResource(m_res.getString("gif_start")));
      btn = new JButton();
      btn.setActionCommand(m_start);
      btn.addActionListener(this);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res, m_start));
      btn.setIcon(ic);
      m_toolBar.add(btn);

      ic = new ImageIcon(getClass().getResource(m_res.getString("gif_stop")));
      btn = new JButton();
      btn.setActionCommand(m_stop);
      btn.addActionListener(this);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res, m_stop));
      btn.setIcon(ic);
      m_toolBar.add(btn);

      ic = new ImageIcon(getClass().getResource(m_res.getString("gif_reset")));
      btn = new JButton();
      btn.setActionCommand(m_reset);
      btn.addActionListener(this);
      btn.setToolTipText(ResourceHelper.getToolTipText(m_res, m_reset));
      btn.setIcon(ic);
      m_toolBar.add(btn);

      updateToolbar(m_previous, m_statusPanel.hasPrevious());
      updateToolbar(m_next, m_statusPanel.hasNext());
   }

   /**
    * Rearranges the view based on which components have been selected or
    * deselected in the 'View' menu. See {@link ContentMenuHandler}.
    *
    * @param actionCommand specifies which menu item has been selected. Never
    * <code>null</code>.
    * @param isSelected <code>true</code> if the menu item has been selected or
    * else <code>false</code>.
    *
    * @throws IllegalArgumentException if <code>actionCommand</code> is <code>
    * null</code> or empty.
    */
   public void reArrange(String actionCommand, boolean isSelected)
   {
      if (actionCommand == null || actionCommand.length() == 0)
         throw new IllegalArgumentException(
            "Action command has to be supplied");

      if (actionCommand.equals(PSContentMenuHandler.MENU_VIEW_TOOLBAR))
      {
         if (isSelected)
            m_contentPane.add(m_toolBar, BorderLayout.NORTH);
         else
            remove(m_toolBar);
      }

      if (actionCommand.equals(PSContentMenuHandler.MENU_VIEW_STATBAR))
      {
         if (isSelected)
            m_contentPane.add(m_statusBar, BorderLayout.SOUTH);
         else
            remove(m_statusBar);
      }

      if (actionCommand.equals(PSContentMenuHandler.MENU_VIEW_LOG))
      {
         if (isSelected)
         {
            m_mainSP.setBottomComponent(m_logPanel);
            m_mainSP.setOneTouchExpandable(true);
            m_mainSP.setDividerSize(DEVIDER_SIZE);
         }
         else
         {
            m_mainSP.setBottomComponent(null);
            m_mainSP.setOneTouchExpandable(false);
            m_mainSP.setDividerSize(DEVIDER_SIZE_MIN);
         }
      }

      if (actionCommand.equals(PSContentMenuHandler.MENU_VIEW_METADATA))
      {
         if (isSelected)
         {
            m_rightSP.setBottomComponent(m_metaPanel);
            m_rightSP.setOneTouchExpandable(true);
            m_rightSP.setDividerSize(DEVIDER_SIZE);

            m_topSP.setRightComponent(m_rightSP);
            m_topSP.setOneTouchExpandable(true);
            m_topSP.setDividerSize(DEVIDER_SIZE);
         }
         else
         {
            if (m_rightSP.getTopComponent() == null)
            {
               m_topSP.setRightComponent(null);
               m_topSP.setOneTouchExpandable(false);
               m_topSP.setDividerSize(DEVIDER_SIZE_MIN);
            }

            m_rightSP.setBottomComponent(null);
            m_rightSP.setOneTouchExpandable(false);
            m_rightSP.setDividerSize(DEVIDER_SIZE_MIN);
         }
      }

      if (actionCommand.equals(PSContentMenuHandler.MENU_VIEW_STATUS))
      {
         if (isSelected)
         {
            m_rightSP.setTopComponent(m_statusPanel);
            m_rightSP.setOneTouchExpandable(true);
            m_rightSP.setDividerSize(DEVIDER_SIZE);

            m_topSP.setRightComponent(m_rightSP);
            m_topSP.setOneTouchExpandable(true);
            m_topSP.setDividerSize(DEVIDER_SIZE);
         }
         else
         {
            if (m_rightSP.getBottomComponent() == null)
            {
               m_topSP.setRightComponent(null);
               m_topSP.setOneTouchExpandable(false);
               m_topSP.setDividerSize(DEVIDER_SIZE_MIN);
            }

            m_rightSP.setTopComponent(null);
            m_rightSP.setOneTouchExpandable(false);
            m_rightSP.setDividerSize(DEVIDER_SIZE_MIN);
         }
      }
      pack();
      validate();
   }

   /**
    * Implements the ActionListener interface. All actions for menu selection
    * except for 'View' (see @link #reArrange(actionCommand, isSelected)
    * reArrange(String, boolean)) are performed here. Called by Swing subsystem.
    * @TODO implement all the actions.
    * @param e Never <code>null</code>.
    */
   public void actionPerformed(ActionEvent e)
   {
      Object o = e.getSource();
      JMenuItem item;
      JButton but;
      String c = e.getActionCommand();

      if (o instanceof JMenuItem)
      {
         item = (JMenuItem)o;
      }
      else if (o instanceof JButton)
      {
         but = (JButton)o;
      }

      if (c.equals(PSContentMenuHandler.MENU_FILE_NEW))
      {
         onNew();
      }
      else if (c.equals(PSContentMenuHandler.MENU_FILE_OPEN))
      {
         onOpen();
      }
      else if (c.equals(PSContentMenuHandler.MENU_FILE_SAVE))
      {
         onSave();
      }
      else if (c.equals(PSContentMenuHandler.MENU_FILE_SAVEAS))
      {
         onSaveAs();
      }
      else if (c.equals(PSContentMenuHandler.MENU_FILE_EXIT))
      {
         System.exit(0);
      }
      else if (c.equals(PSContentMenuHandler.MENU_ACTIONS_SCAN))
      {
         onScan();
      }
      else if (c.equals(PSContentMenuHandler.MENU_ACTIONS_UPLOAD))
      {
         onUpload();
      }
      else if (c.equals(PSContentMenuHandler.MENU_ACTIONS_RELOAD))
      {
         onUpload(PSContentLoaderMgr.UPLOAD_AS_MODIFIED, null);
      }
      else if (c.equals(PSContentMenuHandler.MENU_ACTIONS_RELOADASNEW))
      {
         onUpload(PSContentLoaderMgr.UPLOAD_AS_NEW, null);
      }
      else if (c.equals(PSContentMenuHandler.MENU_TOOLS_DESC))
      {
         if (m_descriptor == null)
         {
            ErrorDialogs.showErrorDialog(this,
               PSContentLoaderResources.getResourceString(
               m_res, "nodescriptorpathmessage"),
               PSContentLoaderResources.getResourceString(
               m_res, "nodescriptorpathtitle"),
               JOptionPane.ERROR_MESSAGE);
         }
         else
         {
            loaderDescriptorDialog(m_descriptor);
         }
      }
      else if (c.equals(PSContentMenuHandler.MENU_TOOLS_PREF))
      {
         PSUserPreferenceDialog prefDlg = new PSUserPreferenceDialog(this);
         prefDlg.setVisible(true);
      }

      else if (c.equals(PSContentMenuHandler.MENU_CONTENT_LOADER_HELP))
      {
         onHelp();
      }
      else if (c.equals(PSContentMenuHandler.MENU_HELP_ABOUT))
      {
         onHelpAbout();
      }
      else if (c.equals(m_previous))
      {
         m_statusPanel.setSingleStep(true);

         if (m_statusPanel.hasPrevious())
         {
            int nState = m_statusPanel.getState();
            m_statusPanel.setState(--nState);
            m_statusPanel.setAction(PSStatusView.ACTION_START);

            updateToolbar(m_previous, m_statusPanel.hasPrevious());
            updateToolbar(m_next, m_statusPanel.hasNext());

            if (nState == PSStatusView.SCAN)
               onScan();
            else if (nState == PSStatusView.UPLOAD)
               onUpload();
         }
      }
      else if (c.equals(m_next))
      {
         m_statusPanel.setSingleStep(true);

         if (m_statusPanel.hasNext())
         {
            int nState = m_statusPanel.getState();
            m_statusPanel.setState(++nState);
            m_statusPanel.setAction(PSStatusView.ACTION_START);

            updateToolbar(m_previous, m_statusPanel.hasPrevious());
            updateToolbar(m_next, m_statusPanel.hasNext());

            if (nState == PSStatusView.SCAN)
               onScan();
            else if (nState == PSStatusView.UPLOAD)
               onUpload();
         }
      }
      else if (c.equals(m_reset))
      {
         m_statusPanel.reset();
         m_logPanel.clearAll();
         m_treePanel.cleanup();
         m_metaPanel.cleanup();

         if (m_csMgr != null)
            m_csMgr = null;

         if (m_loadMgr != null)
            m_loadMgr = null;

         if (m_tree != null)
            m_tree = null;

         updateToolbar(m_previous, m_statusPanel.hasPrevious());
         updateToolbar(m_next, m_statusPanel.hasNext());

         invalidate();
         repaint();
      }
      else if (c.equals(m_start))
      {
         m_statusPanel.setSingleStep(false);
         m_statusPanel.reset();

         Runnable r = new Runnable()
         {
            public void run()
            {
               // Threshold
               if (m_descriptor == null)
               {
                  Logger.getRootLogger().info(
                     m_res.getString("error.msg.descmissing"));
                  return;
               }

               if (shouldStop())
                  return;

               // Synchronous mode
               setSync(true);

               if (shouldStop())
                  return;

               // Scan
               onScan();

               // Show tree
               try
               {
                  m_treePanel.update(
                        m_descriptor,
                        m_tree);
               }
               catch(PSLoaderException e)
               {
                  ErrorDialogs.showErrorDialog(PSMainFrame.this, e.getMessage(),
                     m_res.getString("error.title.loadexception"),
                     JOptionPane.ERROR_MESSAGE);
               }
               if (shouldStop())
                  return;

               // Upload
               onUpload();

               if (shouldStop())
                  return;

               setSync(false);
            }
         };


         m_syncThread = new Thread(r);
         m_syncThread.setPriority(Thread.MIN_PRIORITY);
         m_syncThread.start();
      }
      else if (c.equals(m_stop))
      {
         onStop();
      }
      else if (c.equals(HIGHLIGHT_TREE_CMD))
      {
         PSLogMessage m = (PSLogMessage) o;
         PSItemContext itemC = m.getItemContext();

         if (itemC != null)
         {
            m_treePanel.highlightNode(itemC);
         }
      }
      else if (c.equals(HIGHLIGHT_TREECLEAR_CMD))
      {
         m_treePanel.clearTreeSelection();
      }
      else if (c.equals(HIGHLIGHT_LIST_CMD))
      {
         PSItemContext itemC = (PSItemContext) o;

         if (itemC != null)
         {
            m_logPanel.highlightNode(itemC);
         }
      }
      else if (c.equals(HIGHLIGHT_LISTCLEAR_CMD))
      {
         m_logPanel.clearListSelection();
      }
      else if (c.equals(PSContentTreePanel.PREVIEW_ITEM_FROM_TREE_CMD))
      {
         onPreviewItemFromTree();
      }
      else if (c.equals(PSContentTreePanel.UPLOAD_ALL_FROM_TREE_CMD))
      {
         onUpload();
      }
      else if (c.equals(PSContentTreePanel.UPLOAD_ITEM_FROM_TREE_CMD))
      {
         onUploadItemFromTree();
      }
   }

   /**
    * Save current status, the tree and descriptor
    */
   private void onSave()
   {
      if (m_descriptor == null)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            m_res, "nodescriptorpathmessage"),
            PSContentLoaderResources.getResourceString(
            m_res, "nodescriptorpathtitle"),
            JOptionPane.ERROR_MESSAGE);

         return;
      }

      try
      {
         PSLoaderRepositoryHandler repHandler = new
               PSLoaderRepositoryHandler(m_descriptor.getPath());
         if (m_csMgr != null && !m_csMgr.isAborted())
         {
            IPSContentTree tree = m_csMgr.getContentTree();
            PSContentStatus status = new PSContentStatus(tree,
                  m_descriptor);
            repHandler.saveStatus(status);
         }
      }
      catch(PSLoaderException f)
      {
         ErrorDialogs.showErrorDialog(this, f.getMessage(),
               m_res.getString("error.title.loadexception"),
               JOptionPane.ERROR_MESSAGE);
      }
   }

   /**
    * Action method for 'About' menu item in 'Help' menu. Displays the <code>
    * AboutDialog</code> that displays the information about the product.
    */
   private void onHelpAbout()
   {
      PSObjectStoreUtil objStoreUtil = null;
      String serverName = "Unknown";
      String serverVersion = "Unknown";

      // get the server name and server version
      try
      {
         Properties conProps = new Properties();

         // get connection info
         PSConnectionDef conInfo = null;
         if (m_descriptor != null)
            conInfo = m_descriptor.getConnectionDef();
         else
            conInfo = PSContentLoaderApp.getConfig().getConnectionDef();

         conProps.setProperty(PSObjectStoreUtil.PROPERTY_LOGIN_ID,
            conInfo.getUser());
         conProps.setProperty(PSObjectStoreUtil.PROPERTY_LOGIN_PW,
            conInfo.getPassword());
         conProps.setProperty(PSObjectStoreUtil.PROPERTY_HOST,
            conInfo.getServerName());
         conProps.setProperty(PSObjectStoreUtil.PROPERTY_PORT,
            conInfo.getPort());

         objStoreUtil = new PSObjectStoreUtil(conProps);
         serverName = objStoreUtil.getServer();
         serverVersion = objStoreUtil.getServerVersion().getVersionString();
      }
      catch(Exception e)
      {
         System.out.println("Caught exception: " + e.toString());
         e.printStackTrace();
      }
      finally
      {
         if (objStoreUtil != null)
         {
            try {
               objStoreUtil.close();
            }
            catch (Exception ex){}
         }
      }

      // display the help about dialog
      String pkgName = getClass().getPackage().getName();
      pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));

      ResourceBundle resource = ResourceBundle.getBundle(
               pkgName + ".About", Locale.getDefault());

      String[] versions = new String[3];
      PSFormatVersion version = new PSFormatVersion(pkgName);
      versions[0] = version.getVersionString();
      versions[1] = MessageFormat.format(resource.getString("servername"),
         new Object[] {serverName});
      versions[2] = MessageFormat.format(resource.getString("serverversion"),
         new Object[] {serverVersion});
      String otherCopyRight = resource.getString("othercopyrights");

      PSAboutDialog dlg = new PSAboutDialog(this,
         resource.getString("title"), versions, otherCopyRight);

      dlg.setVisible(true);
   }

   /**
    * Action method for 'Content Loader Help' under 'Help' menu.
    * Displays a dialog containing help documentation.
    */
   private void onHelp()
   {
      String id = "PSMainFrame";
      PSJavaHelp.launchHelp(id);
   }

   /**
    * Accessor to read <code>m_bStop</code>.
    *
    * @return boolean <code>true</code> to stop otherwise
    *    don't stop.
    */
   public boolean shouldStop()
   {
      return m_bStop;
   }

   protected void onPreviewItemFromTree()
   {
      IPSContentTreeNode n = m_treePanel.getSelectedNode();

      // Threshold
      if (n == null)
         return;

      PSItemContext t = n.getItemContext();

      try
      {
         BrowserLauncher.openURL(t.getResourceId());
      }
      catch (java.io.IOException e)
      {
         Logger.getRootLogger().info(e.getMessage());
      }
   }

   protected void onUploadItemFromTree()
   {
      IPSContentTreeNode n = m_treePanel.getSelectedNode();

      if (n == null)
         return; // do nothing if nothing has been selected

      List<IPSContentTreeNode> nodes = new ArrayList<IPSContentTreeNode>();
      nodes.add(n);
      onUpload(PSContentLoaderMgr.UPLOAD_AS_MODIFIED, nodes);
   }

   /**
    * Convenience method to initialize the loader mgr
    *
    * @param uploadMethod The upload method that will be set to the
    *    loader manager. Assume it is one of the
    *    <code>PSContentLoaderMgr.UPLOAD_AS_XXX</code> values.
    *
    * @param nodeList the to be uploaded node list, a list of
    *    <code>IPSContentTreeNode</code>. It may be <code>null</code> if wants
    *    to upload all nodes in the current content tree.
    */
   private void initLoaderMgr(int uploadMethod, List<IPSContentTreeNode> nodeList)
   {
      try
      {
         // Init the status view and progress panel
         m_statusPanel.setState(PSStatusView.UPLOAD);
         m_statusPanel.setAction(PSStatusView.ACTION_START);

         // load the tree
         m_loadMgr = new PSContentLoaderMgr(m_descriptor, m_tree,
            m_csMgr.hasErrorOccured());
         m_loadMgr.addStatusListener(m_logPanel);
         m_loadMgr.addStatusListener(m_statusPanel);
         m_loadMgr.addProgressListener(m_statusPanel.getProgressPanel());
         m_loadMgr.addStatusListener(this);
         m_loadMgr.setPriority(Thread.MIN_PRIORITY);
         m_loadMgr.setUploadMethod(uploadMethod);
         if (nodeList != null)
            m_loadMgr.setUploadedNodes(nodeList);
      }
      catch (PSLoaderException ignore)
      {
         Logger.getRootLogger().info(ignore.getMessage());
      }
   }

   /**
    * Convenience method to initialize the selection mgr
    */
   private void initSelectorMgr()
      throws PSLoaderException
   {
      PSLoaderRepositoryHandler repositoryHandler =
            new PSLoaderRepositoryHandler(m_descriptor.getPath());
      PSContentStatus status = null;
      try
      {
         status = repositoryHandler.getStatus();
      }
      catch (PSLoaderException e)
      {
         Logger.getRootLogger().warn(e.getMessage());

         // inform user that the previous status is ignored
         ErrorDialogs.showErrorDialog(this,
            e.getLocalizedMessage(),
            PSContentLoaderResources.getResourceString(
            m_res, "warning.title.fail2load.status"),
            JOptionPane.WARNING_MESSAGE);
      }

      // Init the status view and progress panel
      m_statusPanel.setState(PSStatusView.SCAN);
      m_statusPanel.setAction(PSStatusView.ACTION_START);

      m_csMgr = new PSContentSelectorMgr(m_descriptor, status);
      m_csMgr.addStatusListener(m_logPanel);
      m_csMgr.addStatusListener(m_statusPanel);
      m_csMgr.addProgressListener(m_statusPanel.getProgressPanel());
      m_csMgr.addStatusListener(this);
      m_csMgr.setPriority(Thread.MIN_PRIORITY);

   }

   /**
    * Based on the user preferences defined in {@link PSUserPreferences}, a new
    * descriptor dialog is brought up or an existing descriptor's path is used
    * by the application.
    *
    * @param descriptor, loader descriptor encapsulating descriptor information,
    * may not be <code>null</code> if .
    */
   public void initDescriptor(PSLoaderDescriptor descriptor)
   {
      PSUserPreferences preferences = PSUserPreferences.deserialize();
      String strDescPath = "";

      if (descriptor == null)
      {
         if (preferences.isNewDescriptor())
         {
            onNew();
         }
         else if (preferences.isLastDescriptor())
         {
            strDescPath = preferences.getLastDescPath();
         }
         try
         {
            if (strDescPath != null && strDescPath.length() != 0)
               m_descriptor = PSContentLoaderApp.loadDescriptor(strDescPath);
         }
         catch (PSLoaderException e)
         {
            Logger.getRootLogger().info(
               m_res.getString("error.msg.descmissing"));
            ErrorDialogs.showErrorDialog(this,
               m_res.getString("error.msg.descmissing") + e.getErrorArguments()[1],
               m_res.getString("error.title.descmissing"),
               JOptionPane.ERROR_MESSAGE);
            return;
         }
      }
      else
         m_descriptor = descriptor;

      setDescriptorOnStatusBar();
   }

   /**
    * Set the current descriptor name on the status bar.
    */
   private void setDescriptorOnStatusBar()
   {
      if (m_descriptor == null)
         m_statusBar.setText(" ");
      else
         m_statusBar.setText(m_res.getString("descriptor.name") +
            m_descriptor.getName());
   }
   /**
    * Opens the new loader descriptor.
    */
   private void onNew()
   {
      loaderDescriptorDialog(null);
   }

   /**
    * Load the supplied descriptor for editing or creating a new one.
    *
    * @param descriptor The to be load descriptor, it may be <code>null</code>
    *    if creating a new descriptor.
    */
   private void loaderDescriptorDialog(PSLoaderDescriptor descriptor)
   {
      try
      {
         m_descriptorDlg = new PSContentDescriptorDialog(this,
               PSContentLoaderApp.getConfig(), descriptor);
      }
      catch (PSLoaderException e)
      {
         String errorMsg = MessageFormat.format(
            m_res.getString("descriptor.loader.error"),
            new Object[] {e.getLocalizedMessage()});

         ErrorDialogs.showErrorDialog(this,
            errorMsg,
            m_res.getString("error.title.loadexception"),
            JOptionPane.ERROR_MESSAGE);

         return;
      }

      m_descriptorDlg.setVisible(true);
      // Refresh the descriptor
      if (m_descriptorDlg.getDescriptor() != null)
         m_descriptor = m_descriptorDlg.getDescriptor();

      setDescriptorOnStatusBar();
   }


   /**
    * The class is used for "SaveAs" the current descriptor. The filter will
    * not accept files, but directories or user specified directory. It will
    * create the user specified directory if the directory does not exist.
    * The "selected directory" will always exist after pressing the "SaveAs"
    * (or "Approve") button.
    */
   private class SaveAsFileChooser extends JFileChooser
   {
      /**
       * 
       */
      private static final long serialVersionUID = 326409903095450168L;

      /**
       * Default constructor
       */
      SaveAsFileChooser()
      {
         super();

         setMultiSelectionEnabled(false);

         // Has to use FILES_AND_DIRECTORIES. Set to DIRECTORY_ONLY and without
         // customized filter does not work correctly, it will not accept
         // user specified directory name which not exist yet.
         setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
         setFileFilter( new FileFilter()
         {
            // Not accept file; accept anything else which parent is a directory
            public boolean accept(File currFile)
            {
               File parent = currFile.getParentFile();
               return (! currFile.isFile()) && parent.isDirectory();
            }

            // directories only, no extension
            public String getDescription()
            {
               return null;
            }
         });
      }

      /**
       * Override the {@link javax.swing.JFileChooser#approveSelection()}
       * Always create the "selected" directory if it does not exist
       */
      public void approveSelection()
      {
         File file = getSelectedFile();
         System.out.println("approveSelection(): " + file.getAbsolutePath());

         if (! file.exists())
         {
            file.mkdir();
            rescanCurrentDirectory();
         }

         super.approveSelection();
      }
   }

   /**
    * Save current descriptor to the user specified descriptor, which will
    * become current descriptor afterwards.
    */
   private void onSaveAs()
   {
      SaveAsFileChooser chooser = null;

      File currDescFile = null;
      if (m_descriptor != null)
      {
         String path = m_descriptor.getPath();
         currDescFile = new File(path);
         chooser = new SaveAsFileChooser();
         chooser.setSelectedFile(currDescFile);
      }
      else // no current descriptor
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            m_res, "nodescriptorpathmessage"),
            PSContentLoaderResources.getResourceString(
            m_res, "nodescriptorpathtitle"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
      int returnVal = chooser.showDialog(this,
         m_res.getString("buttonFileSaveAs"));
      if (returnVal != JFileChooser.CANCEL_OPTION)
      {
         File file = chooser.getSelectedFile();
         if (file.equals(currDescFile))
            return;  // same with the current descriptor, do nothing

         try
         {
            String newPath = file.getAbsolutePath();
            PSLoaderRepositoryHandler repository =
               new PSLoaderRepositoryHandler(newPath);

            // reset the log file
            PSLogDef logDef = m_descriptor.getLogDef();
            String logPath = PSContentDescriptorDialog.getLogFilePath(newPath);
            logDef.setAppenderParam(PSLogDef.FILE_APPENDER, PSLogDef.FILE,
               logPath);
            m_descriptor.setLogDef(logDef);

            // save new descriptor to file system
            repository.saveDescriptor(m_descriptor);

            // resset the current descriptor, must use the repository, which
            // will set other properties for the new descriptor
            m_descriptor = repository.getDescriptor();

            PSUserPreferences.saveLastDescPath(m_descriptor.getPath());

            editDescriptor(m_descriptor.getPath());
         }
         catch(Exception e)
         {
            Logger.getRootLogger().error(e.getMessage());
            ErrorDialogs.showErrorDialog(this, e.getMessage(),
             m_res.getString("error.title.loadexception"),
             JOptionPane.ERROR_MESSAGE);
         }
      }

      setDescriptorOnStatusBar();
   }

   /**
    * Opens an existing descriptor.
    */
   private void onOpen()
   {
      JFileChooser chooser = new JFileChooser(System.getProperty(
         "user.dir"));
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      if (m_descriptor != null)
      {
         String lastPath = m_descriptor.getPath();
         if (lastPath != null && lastPath.length() != 0)
            chooser.setSelectedFile(new File(lastPath));
      }
      chooser.setFileFilter( new FileFilter()
      {
         // accept any directory or files ending with specified
         // extension.
         public boolean accept(File path)
         {
            if ((path.isFile() &&
                 path.getAbsolutePath().endsWith(DESC_EXT))
                 || path.isDirectory())
            {
               return true;
            }
            return false;
         }

         public String getDescription()
         {
            return m_res.getString("desc.file.type");
         }
      });

      int returnVal = chooser.showOpenDialog(null);
      if (returnVal == JFileChooser.OPEN_DIALOG)
      {
         File file = chooser.getSelectedFile();
         // save the descriptor path
         String descFilePath = file.getAbsolutePath();
         String descDir = getDescriptorDir(descFilePath);
         if (descDir == null)
         {
            String errorMsg = MessageFormat.format(
               m_res.getString("error.msg.wrongdesc"),
               new Object[] {descFilePath});
            //Logger.getRootLogger().info(errorMsg);
            ErrorDialogs.showErrorDialog(this,
               errorMsg,
               m_res.getString("error.title.wrongdesc"),
               JOptionPane.ERROR_MESSAGE);
            return;
         }
         try
         {
            setCurrentDesciptor(descDir);
            PSUserPreferences.saveLastDescPath(descDir);
         }
         catch(PSLoaderException e)
         {
            Logger.getRootLogger().error(e.getMessage());
            ErrorDialogs.showErrorDialog(this, e.getMessage(),
             m_res.getString("error.title.loadexception"),
             JOptionPane.ERROR_MESSAGE);
         }
      }

      setDescriptorOnStatusBar();
   }

   /**
    * Reset log4j configuration according to the descriptor that is
    * retrieved from the given loader repository
    *
    * @param repository The repository, assume not <code>null</code>.
    */
   private void resetLog4j(PSLoaderRepositoryHandler repository)
      throws PSLoaderException
   {
      PSLoaderDescriptor descriptor = repository.getDescriptor();

      DOMConfigurator.configure(descriptor.getLogDef().toXml(
         repository.getDescriptorDoc()));

      m_logPanel.updateAppender();
   }

   /**
    * Set the current descriptor to the specified descriptor.
    *
    * @param descDir The descriptor path of the to be edited descriptor,
    *    assume it is not <code>null</code> or empty.
    *
    * @return The loader repository handle for the current descirptor, never
    *    <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs
    */
   private PSLoaderRepositoryHandler setCurrentDesciptor(String descDir)
      throws PSLoaderException
   {
      PSLoaderRepositoryHandler repository = new
         PSLoaderRepositoryHandler(descDir);
      m_descriptor = repository.getDescriptor();

      resetLog4j(repository);

      return repository;
   }

   /**
    * Set the current descriptor to the specified descriptor and bring up
    * the descriptor editor
    *
    * @param descDir The descriptor path of the to be edited descriptor,
    *    assume it is not <code>null</code> or empty.
    */
   private void editDescriptor(String descDir)
   {
      try
      {
         PSLoaderRepositoryHandler repository = setCurrentDesciptor(descDir);

         // bring up the dialog for editing
         m_descriptorDlg = new PSContentDescriptorDialog(this,
            PSContentLoaderApp.getConfig(), m_descriptor);
         m_descriptorDlg.setVisible(true);

         // Refresh the descriptor
         if (m_descriptorDlg.getDescriptor() != null)
         {
            m_descriptor = m_descriptorDlg.getDescriptor();

            // reset log4j, user may have changed its setting
            resetLog4j(repository);
         }
      }
      catch(PSLoaderException e)
      {
         Logger.getRootLogger().error(e.getMessage());
         ErrorDialogs.showErrorDialog(this, e.getMessage(),
          m_res.getString("error.title.loadexception"),
          JOptionPane.ERROR_MESSAGE);
      }
   }

   /**
    * Get the descriptor directory from a descriptor file path. The descriptor
    * directory has to be the same name as the descriptor file (without the
    * ".xml" extension). For example, .../XXX/XXX.xml
    *
    * @param descFilePath the specified descriptor file path, assumed not
    *    <code>null</code>.
    *
    * @return the descriptor directory for a valid descriptor file path;
    *    otherwise, return <code>null</code> if the descriptor file path is
    *    invalid.
    */
   private String getDescriptorDir(String descFilePath)
   {
      String descDir = null;
      File descFile = new File(descFilePath);
      File dirFile = descFile.getParentFile();

      String dirName = dirFile.getName();
      String descName = descFile.getName();

      int index = descName.lastIndexOf(".xml");
      if (index > 0)
      {
         descName = descName.substring(0, index);
         if (dirName.equals(descName))
            descDir = dirFile.getAbsolutePath();
      }

      return descDir;
   }

   /**
    * Stop method
    */
   private void onStop()

   {
      m_bStop = true;
      if (m_csMgr != null)
      {
         if (m_csMgr.isAlive())
         {
            m_csMgr.abort();
         }
      }

      if (m_loadMgr != null)
      {
         if (m_loadMgr.isAlive())
         {
            m_loadMgr.abort();
         }
      }

      setSync(false);
   }

   /**
    * Convenient method, calls (@link onUpload(int)
    * onUpload(PSContentLoaderMgr.UPLOAD_AS_NEEDED, null)}
    */
   private void onUpload()
   {
      onUpload(PSContentLoaderMgr.UPLOAD_AS_NEEDED, null);
   }

   /**
    * Upload specified node list. If the node list is <code>null</code>, then
    * upload all nodes in the current content tree.
    *
    * @param uploadMethod  The upload method that will be used for the upload
    *    operation. Assume it is one of the
    *    <code>PSContentLoaderMgr.UPLOAD_AS_XXX</code> values
    *
    * @param nodeList A list of <code>IPSContentTreeNode</code>. It may be
    *    <code>null</code>,
    */
   public void onUpload(int uploadMethod, List<IPSContentTreeNode> nodeList)
   {
      try
      {
         if (m_descriptor == null)
         {
            ErrorDialogs.showErrorDialog(this,
               PSContentLoaderResources.getResourceString(
               m_res, "nodescriptorpathmessage"),
               PSContentLoaderResources.getResourceString(
               m_res, "nodescriptorpathtitle"),
               JOptionPane.ERROR_MESSAGE);
            return;
         }

         if (m_tree == null)
         {
            ErrorDialogs.showErrorDialog(this,
               PSContentLoaderResources.getResourceString(
               m_res, "emptytreemessage"),
               PSContentLoaderResources.getResourceString(
               m_res, "emptytreemessagetitle"),
               JOptionPane.ERROR_MESSAGE);

            return;
         }


         initLoaderMgr(uploadMethod, nodeList);

         if (!isSync())
         {
            m_loadMgr.start();
         }
         else
         {
            m_loadMgr.run();
         }
      }
      catch (Exception e)
      {
         Logger.getRootLogger().info(e.getMessage());
      }
   }

   /**
    * Method to perform a scan and all associated ui behaviors.
    */
   private void onScan()
   {
      try
      {
         if (m_descriptor == null)
         {
            ErrorDialogs.showErrorDialog(this,
               PSContentLoaderResources.getResourceString(
               m_res, "nodescriptorpathmessage"),
               PSContentLoaderResources.getResourceString(
               m_res, "nodescriptorpathtitle"),
               JOptionPane.ERROR_MESSAGE);
            return;
         }

         initSelectorMgr();
         String path = m_descriptor.getPath();
         DOMConfigurator.configure(m_descriptor.getLogDef().toXml(
            PSContentLoaderApp.getRepositoryHandler(path).getDescriptorDoc()));
         m_logPanel.updateAppender();
         if (!isSync())
         {
            m_csMgr.start();
         }
         else
         {
            // Waiting to finish
            m_csMgr.run();
            m_tree = m_csMgr.getContentTree();
         }
      }
      catch (Exception e)
      {
         Logger.getRootLogger().error(e.getMessage());
      }
   }

   /**
    * Updates the toolbar action to the supplied status.
    *
    * @param action the action for which to update the toolbar, assumed not
    *    <code>null</code>.
    * @param enable <code>true</code> to enable, <code>false</code> to disable
    *    the addressed action button.
    */
   private void updateToolbar(String action, boolean enable)
   {
      Component[] components = m_toolBar.getComponents();
      for (int i=0; i<components.length; i++)
      {
         Component component = components[i];
         if (component instanceof JButton)
         {
            JButton button = (JButton) component;
            if (button.getActionCommand().equals(action))
            {
               button.setEnabled(enable);
               break;
            }
         }
      }
   }

   //Overridden so we can exit when window is closed
   protected void processWindowEvent(WindowEvent e)
   {
      super.processWindowEvent(e);
      if (e.getID() == WindowEvent.WINDOW_CLOSING)
      {
          System.exit(0);
      }
   }

   // implements IPSProgressListener
   public void progressChanged(PSProgressEvent event)
   {
      // no-op
   }

   // implements IPSStatusListener
   public void statusChanged(PSStatusEvent event)
   {
      try
      {
         if (event.getStatus() == PSStatusEvent.STATUS_ABORTED)
         {
            PSProcessMgr mgr = null;
            if ( event.getSource() == m_csMgr)
               mgr = m_csMgr;
            else if ( event.getSource() == m_loadMgr)
               mgr = m_loadMgr;

            if (mgr != null)
            {
               String title = PSContentLoaderResources.getResourceString(
                     m_res, "error.title.processAborted");
               String errorMsg = title;
               if (mgr.getRunException() != null)
               {
                  errorMsg = MessageFormat.format(
                     m_res.getString("error.msg.processAborted"),
                     new Object[] {mgr.getRunException().getMessage()});
               }

               DisplayErrorMessage showError = new DisplayErrorMessage(this,
                  errorMsg, title);
               SwingUtilities.invokeLater(showError);
            }
         }
         else if (event.getStatus() == PSStatusEvent.STATUS_COMPLETED
            && event.getProcessId() == PSStatusEvent.PROCESS_SCANNING)
         {
            // Load the tree panel
            Runnable r = new Runnable()
            {
               public void run()
               {
                  // if we are IN sync mode than onScan is responsible for
                  // filling out the m_tree.
                  if (!isSync())
                  {
                     if (m_csMgr.getContentTree() != null)
                     {
                        m_tree = m_csMgr.getContentTree();

                        try
                        {
                           m_treePanel.update(m_descriptor, m_tree);
                           // Redraw the tree and re-arrange the top panel
                           m_topSP.updateUI();
                        }
                        catch (Exception ignore)
                        {
                           Logger.getRootLogger().info(ignore.getMessage());
                           onStop();
                        }
                     }
                  }
               }
            };
            SwingUtilities.invokeLater(r);
         }
         else if (event.getStatus() == PSStatusEvent.STATUS_COMPLETED
            && event.getProcessId() == PSStatusEvent.PROCESS_LOADING_CONTENTS)
         {
            // After finished the last step of the uploading process,
            // Update the content tree, some or all of the nodes may have
            // changed its status.
            Runnable r = new Runnable()
            {
               public void run()
               {
                  m_treePanel.updateSelectedNode();
                  m_topSP.updateUI();
               }
            };
            SwingUtilities.invokeLater(r);
         }
      }
      catch (Exception e)
      {
        Logger.getRootLogger().info(e.getMessage());
      }
   }

   /**
    * This class is used to display error messages in a thread that may not be
    * the main or dispatch thread.
    */
   private class DisplayErrorMessage implements Runnable
   {
      /**
       * Creates an instance of this class from given parameters
       */
      private DisplayErrorMessage(Component parent, String message,
                                  String title)
      {
         m_parent = parent;
         m_message = message;
         m_title = title;
      }

      public void run()
      {
         ErrorDialogs.showErrorDialog(m_parent, m_message, m_title,
               JOptionPane.ERROR_MESSAGE);

      }

      private Component m_parent;
      private String m_message;
      private String m_title;
   }

  /**
   * Setter for <code>m_bSync</code>
   *
   * @param b boolean if <code>true</code> operations
   *  are performed synchronously otherwise asynchronously.
   */
   protected void setSync(boolean b)
   {
      m_bSync = b;
   }

   /**
    * Getter for <code>m_bSync</code>
    *
    * @return boolean if <code>true</code> operations
    *  are performed synchronously otherwise asynchronously.
    */
   protected boolean isSync()
   {
      return m_bSync;
   }

   /**
    * Content loader descriptor dialog for setting up variables for migration
    * for a migration process.
    */
   private PSContentDescriptorDialog m_descriptorDlg;

   /**
    * The split pane devider size if one touch expandable is enabled.
    */
   private static final int DEVIDER_SIZE = 8;

   /**
    * The split pane devider size if one touch expandable is disabled.
    */
   private static final int DEVIDER_SIZE_MIN = 0;

   /**
    * The contentPane object for this frame. Initialised in <code>init()</code>.
    * Never <code>null</code> or modified after initialization.
    */
   private JPanel m_contentPane ;

   /**
    * Toolbar for this frame. Never <code>null</code>.
    */
   private JToolBar m_toolBar = new JToolBar();

   /**
    * Status bar for this frame. Never <code>null</code>.
    */
   private JLabel m_statusBar = new JLabel();

   /**
    * Main split pane holding <code>m_topSP</code> and <code>m_logPanel</code>.
    * Initialised in the <code>init()</code>, never <code>null</code>, only
    * modified when user selects a menu item in the 'View' menu. See (see @link
    * #reArrange(actionCommand, isSelected) reArrange(String, boolean)).
    */
   private JSplitPane m_mainSP;

   /**
    * Top split pane holding <code>m_treePanel</code> and <code>m_rightSP
    * </code>.
    * Initialised in the <code>init()</code>, never <code>null</code>, only
    * modified when user selects a menu item in the 'View' menu. See (see @link
    * #reArrange(actionCommand, isSelected) reArrange(String, boolean)).
    */
   private JSplitPane m_topSP;

   /**
    * Top split pane holding <code>m_statusPanel</code> and <code>m_metaPanel
    * </code>.
    * Initialised in the <code>init()</code>, never <code>null</code>, only
    * modified when user selects a menu item in the 'View' menu. See (see @link
    * #reArrange(actionCommand, isSelected) reArrange(String, boolean)).
    */
   private JSplitPane m_rightSP;

   /**
    * The resource bundle that holds resource strings used by this frame. Never
    * <code>null</code> or modified after initialization.
    */
   private PSResources m_res;

   /**
    * Panel holding the tree. Initialised in the <code>init()</code>, never
    * <code>null</code> or modified after that.
    */
   private PSContentTreePanel m_treePanel;

   /**
    * Panel holding the status view for the frame. Initialised in the <code>
    * init()</code>, never <code>null</code> or modified after that.
    */
   private PSStatusView m_statusPanel = null;

   /**
    * Panel holding the meta data view for the frame. Initialised in the <code>
    * init()</code>, never <code>null</code> or modified after that.
    */
   private PSMetaDataView m_metaPanel = null;

   /**
    * The log panel. Initialized in {#link init()}, never <code>null</code> or
    * modified after that.
    */
   private PSLogPanel m_logPanel;

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * tool bar item indicating previous button.
    */
   public static final String m_previous = "previousToolBtn";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * tool bar item indicating previous button.
    */
   public static final String m_next = "nextToolBtn";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * tool bar item indicating stop button.
    */
   public static final String m_stop = "stopToolBtn";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * tool bar item indicating start button.
    */
   public static final String m_start = "startToolBtn";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * tool bar item indicating reset button.
    */
   public static final String m_reset = "resetToolBtn";

   /**
    * The log appender name used in the user interface logging panel.
    */
   public static final String LOG_DISPATCHER = "logDispatcher";

   /**
    * Singleton instance. Intialised in {@link #getFrame()}, never <code>null
    * </code> or modified after that.
    */
   private static PSMainFrame ms_frame;

   /**
    * Command name constant to highlight a tree node.
    */
   public static final String HIGHLIGHT_TREE_CMD = "Highlight Tree";

   /**
    * Command name constant to clear a tree node highlight.
    */
   public static final String HIGHLIGHT_TREECLEAR_CMD = "Highlight Tree Clear";

   /**
    * Command id constant to highlight a tree node.
    */
   public static final int HIGHLIGHT_TREE = 4008;

   /**
    * Command name constant to highlight a tree node list.
    */
   public static final String HIGHLIGHT_LIST_CMD = "Highlight List";

   /**
    * Command name constant to clear a tree node list highlight.
    */
   public static final String HIGHLIGHT_LISTCLEAR_CMD = "Highlight List Clear";

   /**
    * Command id constant to highlight a tree node list.
    */
   public static final int HIGHLIGHT_LIST = 4009;

   /**
    * The file extension for the descriptor
    */
   private static final String DESC_EXT = ".xml";

   /**
    * Private PSContentSelectionMgr. Initialized in <code>onScan</code>, may be
    * <code>null</code>.
    */
   private PSContentSelectorMgr m_csMgr = null;

   /**
    * Private PSContentLoaderMgr. Initialized in <code>onUpload</code>, may be
    * <code>null</code>.
    */
   private PSContentLoaderMgr m_loadMgr = null;

   /**
    * Private IPSContentTree the results of a scan represents a dependency
    * model of the source. Initialized in statusChanged event when scanning
    * process completes.
    */
   private IPSContentTree m_tree = null;

   /**
    * PSLoaderDescriptor represents the current defined descriptor
    * for content selection, loading etc.. Initialized in ctor
    */
   private PSLoaderDescriptor m_descriptor = null;

   /**
    * Processing occurs synchronously or asynchronously based on
    * this attribute. Initialized on definition, set be <code>setSync</code>
    * and read by <code>isSync</code>.
    */
   private boolean m_bSync = false;

   /**
    * Synchronously thread used to run whole process without interruption.
    * May be <code>null</code>.
    */
   private Thread m_syncThread = null;

   /**
    * boolean attribute checked by <code>m_syncThread</code> run method
    * as to whether or not to stop. Initialized in definition to
    * <code>false</code>. Set via <code>onStop</code>
    */
   private boolean m_bStop = false;
}

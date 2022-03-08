/******************************************************************************
 *
 * [ PSMainFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.error.PSDeployException;
import com.percussion.guitools.PSResources;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The main frame for the deployment client. Initializes the frame with a split
 * pane and adds menus to the frame. 
 */
public class PSMainFrame extends JFrame implements TreeSelectionListener
{   
   /**
    * Intializes the frame with browser(left) and view(right) panels.
    * 
    * @param deploymentServers the map of registererd servers with repository 
    * info(<code>OSDbmsInfo</code>) as key and <code>List</code> of <code>
    * PSDeploymentServer</code>s using that repository as value, may not be 
    * <code>null</code>, can be empty. The value <code>List</code> can not be
    * empty.
    * 
    * @throws IllegalArgumentException if deploymentServers is <code>null</code>
    * or the list of servers for a repository is empty.
    */
   public PSMainFrame(Map deploymentServers)
   {
      if(deploymentServers == null)
         throw new IllegalArgumentException(
            "deploymentServers may not be null.");
            
      Iterator entries = deploymentServers.entrySet().iterator();
      while(entries.hasNext())
      {        
         Map.Entry entry = (Map.Entry)entries.next();
         
         Object key = entry.getKey();
         if( !(key instanceof OSDbmsInfo) )
            throw new IllegalArgumentException(
               "The key of the 'deploymentServers' map must be an instance of" +
               " OSDbmsInfo"
               );               
         OSDbmsInfo dbmsInfo = (OSDbmsInfo)key;
         
         Object value = entry.getValue();
         if( !(value instanceof List) )
            throw new IllegalArgumentException(
               "The value of the 'servers' map must be an instance of List that"
               + " holds the PSDeploymentServer objects");
         
          try {
            ((List)value).toArray(new PSDeploymentServer[0]);
         }
         catch(ArrayStoreException e)
         {
            throw new IllegalArgumentException(
               "The list of servers must be instances of PSDeploymentServer");
         }
      }
            
      m_res = PSDeploymentClient.getResources();      
      
      init(deploymentServers);
   }
   
   /**
    * Initializes frame with title, icon, menu and sets actions for menu items. 
    * Creates the tree from the deployment servers and adds itself as selection 
    * listener to the tree. Initializes the handlers required for menu handling
    * and for the server connections. Creates the left and right panels and sets
    * the total size of this frame as 80% of the screen size and the left 
    * (browser) pane's width as 1/3 of frame width.
    * 
    * @param deploymentServers the map of registererd servers with repository 
    * info(<code>OSDbmsInfo</code>) as key and list of <code>PSDeploymentServer
    * </code>s using that repository as value, assumed not <code>null</code>.
    */
   private void init(Map deploymentServers)
   {   
      setTitle( m_res.getString("title") );
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      URL imageFile = getClass().getResource(m_res.getString("gif_main"));
      if (imageFile != null)
      {
         ImageIcon icon = new ImageIcon(imageFile);
         setIconImage(icon.getImage());
      }           
      PSServersTree tree = new PSServersTree(m_res.getString("serverGroups"), 
         deploymentServers);
      tree.addTreeSelectionListener(this);

      //construct menu handler which handles all menu actions.
      m_menuHandler = new PSDeploymentMenuHandler(this, tree);
      
      //construct connection handler
      m_connHandler = new PSConnectionHandler(tree);
      
      createPanels(tree);
      
      Dimension Size = new Dimension( );
      Point Pos = new Point( );      
      Dimension screenSize = new Dimension( 
         Toolkit.getDefaultToolkit( ).getScreenSize( ) );
      Size.setSize( (screenSize.width * 4 / 5), screenSize.height * 4 / 5 );
      Pos.setLocation( (screenSize.width / 10), screenSize.height / 10 );
            
      setSize( Size );
      setLocation( Pos );
   }
   
   /**
    * Creates a split panel with the browser panel as left pane and view panel 
    * as right pane. 
    * 
    * @param tree the registered servers tree that need to be added to the 
    * browser panel, assumed not to be <code>null</code>
    */
   private void createPanels(PSServersTree tree)
   {
      getContentPane( ).setLayout( new BorderLayout( ) );

      JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);   

      m_browserPane = new PSBrowserPanel(tree);
      m_browserPane.setMinimumSize(new Dimension(0,0));
      
      m_viewPane = new PSViewPanel();
      m_viewPane.setMinimumSize(new Dimension(0,0));      
      split.setLeftComponent(m_browserPane);
      split.setRightComponent(m_viewPane);
      getContentPane( ).add( split, BorderLayout.CENTER );      
   }
   
   /**
    * Processes the window close event as exit action.
    * 
    * @param e the window event, assumed not to be <code>null</code> as this 
    * method will be called by <code>Swing</code> model when user clicks on the
    * close button of this frame.
    */
   protected void processWindowEvent( WindowEvent e )
   {
      super.processWindowEvent( e );
      if (e.getID( ) == WindowEvent.WINDOW_CLOSING)
      {
         actionExit();        
      }
   }   
   
   /**
    * Implements the actions to be done on exit from this application. The 
    * actions are:
    * <ol>
    * <li>Disconnects from the connected servers</li>
    * <li>Saves the current registered servers</li>
    * <li>Saves the user properties</li>
    * <li>Exits from the application</li>
    * </ol>
    */
   public void actionExit()
   {
      try {
         Iterator servers = 
            m_browserPane.getServersTree().getRegisteredServers().iterator();
         while(servers.hasNext())
         {
            PSDeploymentServer server = (PSDeploymentServer)servers.next();
            if(server.isConnected())
               server.disconnect();
         }
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showErrorMessage(
            e.getLocalizedMessage(),
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
      m_browserPane.save();
      PSDeploymentClient.saveUserProperties();
      System.exit(0);
   }
   
   /**
    * Gets the list of registered servers.
    * 
    * @return the list of <code>PSDeploymentServer</code>s, never <code>null
    * </code>, may be empty.
    */
   public List getRegisteredServers() 
   {
      return m_browserPane.getServersTree().getRegisteredServers();
   }
   
   /**
    * Gets the connection handler to handle making server connections.
    * 
    * @return the connection handler, never <code>null</code>
    */
   public PSConnectionHandler getConnectionHandler()
   {
      return m_connHandler;
   }

   //Methods generated from implementation of interface TreeSelectionListener
   /**
    * Checks the currently selected node in the event source tree and updates
    * the view in the panel correspondingly. 
    * 
    * @param e the event that characterizes the change, assumed not to be <code>
    * null</code> as this method will be called by <code>Swing</code> model when
    * a tree selection changes.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      Object source = e.getSource();
      if(source instanceof PSServersTree)
      {
         TreePath selPath = e.getPath();
         DefaultMutableTreeNode selNode = 
            (DefaultMutableTreeNode)selPath.getLastPathComponent();
            
         int viewType = PSViewPanel.TYPE_VIEW_NOTHING;
         Object data = null;          
         
         if(!selNode.isRoot())
         {
            if(selNode.getUserObject() instanceof OSDbmsInfo)
            {
               viewType = PSViewPanel.TYPE_VIEW_REPOSITORY;
            }
            else if(selNode.getUserObject() instanceof PSDeploymentServer)
            {
               viewType = PSViewPanel.TYPE_VIEW_SERVER;
            }
            else {
               String nodeString = selNode.getUserObject().toString();
               if(nodeString.equals(PSServersTree.ms_source) ||
                  nodeString.equals(PSServersTree.ms_target))
               {
                  selNode = (DefaultMutableTreeNode)selPath.getParentPath().
                     getLastPathComponent();
                  viewType = PSViewPanel.TYPE_VIEW_SERVER;
               }
               else if(nodeString.equals(PSServersTree.ms_descriptors) ||
                  nodeString.equals(PSServersTree.ms_archives) ||
                  nodeString.equals(PSServersTree.ms_packages))
               {
                  selNode = (DefaultMutableTreeNode)selPath.getParentPath().
                     getParentPath().getLastPathComponent();
                  if(nodeString.equals(PSServersTree.ms_descriptors))
                     viewType = PSViewPanel.TYPE_VIEW_DESCRIPTORS;
                  else if(nodeString.equals(PSServersTree.ms_archives))
                     viewType = PSViewPanel.TYPE_VIEW_ARCHIVES;                  
                  else
                     viewType = PSViewPanel.TYPE_VIEW_PACKAGES;
               }
            }
            //get data object for the node
            data = selNode.getUserObject();
         }
         m_viewPane.updateView(viewType, data);
      }
   }
   
   /**
    * Gets the deploy property value of the specified key. See {@link 
    * #putDeployProperty(String, Object) } for more info on properties.
    * 
    * @param key the property key, may not be <code>null</code> or empty.
    * 
    * @return the property value, may be <code>null</code> if the property is
    * not stored.
    * 
    * @throws IllegalArgumentException if key is not valid.
    */
   public static Object getDeployProperty(String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty.");
      
      return ms_properties.get(key);
   }
   
   /**
    * Stores the deploy property value with the specified key. It will overwrite
    * any previous property stored with the specified key. 
    * 
    * @param key the property key, may not be <code>null</code> or empty.
    * @param value the property value, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public static void putDeployProperty(String key, Object value)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty.");
         
      if(value == null)
         throw new IllegalArgumentException("value may not be null.");
         
      ms_properties.put(key, value);
   }
   
   /**
    * Removes the deploy property value with the specified key. 
    * 
    * @param key the property key, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public static void removeDeployProperty(String key)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key may not be null or empty.");
        
      ms_properties.remove(key);
   }
   
   /**
    * The resource bundle that holds resource strings used by this frame. Never
    * <code>null</code> or modified after initialization.
    */
   private PSResources m_res;
   
   /**
    * The left pane that contains the tree of registered servers. Never <code>
    * null</code> or modified after initialization.
    */
    private PSBrowserPanel m_browserPane;
   
   /**
    * The view panel that displays the contents of the selected node in the 
    * servers tree that exists in the left(browser) pane of this frame. Never
    * <code>null</code> or modified after initialization.
    */
   private PSViewPanel m_viewPane;
   
   /**
    * The handler which creates main menu bar and pop-up menu for the registered
    * servers tree and sets actions on it. Initialized in <code>init()</code> 
    * and never <code>null</code> or modified after that.
    */
   private PSDeploymentMenuHandler m_menuHandler;
   
   /**
    * The handler that creates connections to the server and handles all actions
    * that need to be taken while registering or editing or connecting, 
    * initialized in the constructor and never <code>null</code> or modified 
    * after that.
    */
   private PSConnectionHandler m_connHandler;
   
   /**
    * The property to define the deployment server that is used last time the
    * export (create archive) wizard was run.
    */
   public static final String LAST_EXPORT_SERVER = "lastExportServer";
   
   /**
    * The property to define the deployment server that is used last time the
    * import (install archive) wizard was run.
    */
   public static final String LAST_IMPORT_SERVER = "lastImportServer";   
   
   /**
    * The property to define the deployment server that is used last time the
    * import (install archive) wizard was run.
    */
   public static final String LAST_SELECTED_SERVER = "lastSelectedServer";   
   
   /**
    * The properties that are used by this application, initialized to an empty 
    * map and gets modified through calls to <code>putDeployProperty(String, 
    * Object)</code>, never <code>null</code>.
    */
   private static Map ms_properties = new HashMap();
}

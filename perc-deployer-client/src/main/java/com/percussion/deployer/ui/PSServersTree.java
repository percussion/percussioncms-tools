/******************************************************************************
 *
 * [ PSServersTree.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.PSResources;
import com.percussion.util.PSStringComparator;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * The tree which represents the registered servers grouped with
 * repository(dbms) info.
 */
public class PSServersTree extends JTree 
   implements TreeWillExpandListener, IPSViewDataChangeListener
{
   /**
    * Builds the tree with root node representing <code>rootName</code> and
    * builds its children with the repository info as parent node and servers
    * using that repository as child nodes. The server node is a dynamic node
    * and its children are loaded dynamically when the server node is expanded.
    * Adds the will expand listener to the tree. Creates a connection handler to
    * display the connection dialog when a server node that is not connected is
    * expanded.
    *
    * @param rootName the root of the tree, may not be <code>null</code> or
    * empty.
    * @param dbServers the map of registererd servers with repository info
    * (<code>OSDbmsInfo</code>) as key and list of <code>PSDeploymentServer
    * </code>s using that repository as value, may not be <code>null</code>, can
    * be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSServersTree(String rootName, Map dbServers)
   {
      if(rootName == null || rootName.trim().length() == 0)
         throw new IllegalArgumentException(
            "rootName may not be null or empty.");

      if(dbServers == null)
         throw new IllegalArgumentException("dbServers may not be null.");

      DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootName);
      setModel( new DefaultTreeModel(rootNode) );
      getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      Iterator iter = dbServers.entrySet().iterator();
      while(iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         OSDbmsInfo info = (OSDbmsInfo)entry.getKey();
         DefaultMutableTreeNode dbmsNode = new DefaultMutableTreeNode(info);
         rootNode.add(dbmsNode);

         List servers = (List)entry.getValue();
         Iterator servIter = servers.iterator();
         while(servIter.hasNext())
         {
            PSDeploymentServer server = (PSDeploymentServer)servIter.next();
            server.addDataChangeListener(this);            
            ServerNode serverNode = new ServerNode(server);
            dbmsNode.add(serverNode);
         }
      }
      addTreeWillExpandListener(this);
      setCellRenderer(new ServerTreeCellRenderer());
      expandRow(0);
   }

   /**
    * Adds the supplied server node to the tree with the supplied repository
    * info as its parent node. If the repository info node does not exist in
    * this tree, it adds a new repository node under the root and the server
    * node is added to the repository node. Makes path to the added node
    * visible.
    *
    * @param info the repository info that represents a repository node in the
    * tree, may not be <code>null</code>. If a repository node matching this
    * database information does not exist, it will create this node.
    * @param server the server object which need to be added as a node to the
    * repository node matching the supplied repository info.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws IllegalStateException if user objects of immediate children of
    * root are not instances of <code>OSDbmsInfo</code> objects.
    */
   public void addServerNode(OSDbmsInfo info, PSDeploymentServer server)
   {
      if(info == null)
         throw new IllegalArgumentException("info may not be null.");

      if(server == null)
         throw new IllegalArgumentException("server may not be null.");

      DefaultTreeModel model = (DefaultTreeModel)getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

      DefaultMutableTreeNode dbNode = null;
      int count = root.getChildCount();
      for(int i=0; i<count; i++)
      {
         DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(i);
         if(child.getUserObject() instanceof OSDbmsInfo)
         {
            OSDbmsInfo nodeInfo = (OSDbmsInfo)child.getUserObject();
            if(nodeInfo.isSameDb(info))
            {
               dbNode = child;
               break;
            }
         }
         else
            throw new IllegalStateException(
               "The children of root must be OSDbmsInfo objects");
      }

      if(dbNode == null)
      {
         dbNode = new DefaultMutableTreeNode(info, true);
         insertNode(root, dbNode);
      }
      ServerNode serverNode = new ServerNode(server);
      //add this as a listener for its data changes
      server.addDataChangeListener(this);
      insertNode(dbNode, serverNode);

      scrollPathToVisible(new TreePath(serverNode.getPath()));
   }

   /**
    * Moves the server node that represents <code>server</code> from the
    * repository node that represents the <code>originalRep</code> to the
    * repository node that represents the <code>newRep</code>. If the new
    * repository node does not exist, it creates the new repository node.
    *
    * @param originalRep the original repository of the server, may not be
    * <code>null</code> and a repository node corresponding to this must exist.
    * @param newRep the new repository of the server, may not be <code>null
    * </code>, node corresponding to this may or may not exist. if not it
    * creates a new node.
    * @param server the server node corresponding to which needs to be moved,
    * may not be <code>null</code> and must exist under original repository
    * node.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    * @throws IllegalStateException if original repository node do not exist or
    * server node does not exist under original repository node.
    */
   public void moveServerNode(OSDbmsInfo originalRep, OSDbmsInfo newRep,
      PSDeploymentServer server)
   {
      if(originalRep == null)
         throw new IllegalArgumentException("originalRep may not be null.");

      if(newRep == null)
         throw new IllegalArgumentException("newRep may not be null.");

      if(server == null)
         throw new IllegalArgumentException("server may not be null");

      DefaultMutableTreeNode originalRepNode =
         getMatchingRepositoryNode(originalRep);

      if(originalRepNode == null)
         throw new IllegalStateException(
            "The repository node corresponding to originalRep does not exist");

      int count = originalRepNode.getChildCount();

      DefaultTreeModel model = (DefaultTreeModel)getModel();

      DefaultMutableTreeNode serverNode = null;
      for(int i=0; i<count; i++)
      {
         DefaultMutableTreeNode child =
            (DefaultMutableTreeNode)originalRepNode.getChildAt(i);
         if(child.getUserObject() instanceof PSDeploymentServer)
         {
            PSDeploymentServer depServer =
               (PSDeploymentServer)child.getUserObject();
            if(depServer.getServerName().equals(server.getServerName()))
            {
               serverNode = child;
               model.removeNodeFromParent(child);
               if(originalRepNode.getChildCount() == 0)
                  model.removeNodeFromParent(originalRepNode);
               break;
            }
         }
         else
            throw new IllegalStateException(
               "The children of repository must be PSDeploymentServer objects");
      }

      if(serverNode == null)
         throw new IllegalStateException(
            "server node does not exist under original repository node");

      addServerNode(newRep, server);
   }

   /**
    * Inserts a new node keeping alphabetical order. Uses <code>toString()
    * </code> on node's user object for comparison. Assumes the user object on
    * the node is not <code>null</code>.
    *
    * @param parent a parent node at which child node will be inserted,
    * assumed not to be<code>null</code>
    *
    * @param child a child node to be inserted at the parent node,
    * assumed not to be<code>null</code>
    */
   private void insertNode(DefaultMutableTreeNode parent,
      DefaultMutableTreeNode child)
   {
      DefaultTreeModel model = (DefaultTreeModel)getModel();

      int iCount = model.getChildCount(parent);
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
      model.insertNodeInto(child, parent, i);
   }

   /**
    * Action method that is called before a tree node is expanded. Worker method
    * for a server node to expand. If the node that needs to be expanded is a
    * server node (<code>ServerNode</code>), checks the status of server
    * connection and prompts or tries to connect to the server based on the
    * {@link PSServerRegistration#PSServerRegistration(String, int, String, String, boolean)
    * registration} details saved with this server if the server is not
    * connected. Once the server is connected, it displays the child nodes of
    * that server node. Please refer to <code>loadChildren()</code> of the
    * <code>ServerNode</code> to know about the children of a server node.
    *
    * @param event the tree expansion event, assumed not to be <code>null</code>
    * as Swing model calls this method with an event when tree expansion event
    * occurs.
    *
    * @throws ExpandVetoException if connection to server is not successful.
    */
   public void treeWillExpand(TreeExpansionEvent event)
      throws ExpandVetoException
   {
      DefaultMutableTreeNode node =
         (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
      if(node.getUserObject() instanceof PSDeploymentServer)
      {
         PSDeploymentServer server = (PSDeploymentServer)node.getUserObject();
         
         if(!PSDeploymentClient.getConnectionHandler().connectToServer(server))
            throw new ExpandVetoException(event, "Unable to connect to server");        
      }
   }

   //nothing to implement
   public void treeWillCollapse(TreeExpansionEvent event)
      throws ExpandVetoException
   {
   }

   /**
     * Checks that whether any registered server exists with the supplied
    * repository.
    *
    * @param dbmsInfo dbms/repository info that need to be checked, may not be
    * <code>null</code>
    *
    * @return <code>true</code> if it exists, otherwise <code>false</code>
    *
    * @throws IllegalArgumentException if dbmsInfo is <code>null</code>
    * @throws IllegalStateException if user objects of immediate children of
    * root are not instances of <code>OSDbmsInfo</code> objects.
    */
   public boolean repositoryExists(PSDbmsInfo dbmsInfo)
   {
      DefaultTreeModel model = (DefaultTreeModel)getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

      int count = root.getChildCount();
      for(int i=0; i<count; i++)
      {
         DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(i);
         if(child.getUserObject() instanceof OSDbmsInfo)
         {
            OSDbmsInfo nodeInfo = (OSDbmsInfo)child.getUserObject();
            if(nodeInfo.isSameDb(dbmsInfo))
               return true;
         }
         else
            throw new IllegalStateException(
               "The children of root must be OSDbmsInfo objects");
      }

      return false;
   }

   /**
    * Gets the <code>OSDbmsInfo</code> object matching the <code>PSDbmsInfo
    * </code> object. This is required to find out whether any server is
    * registered with the supplied repository/dbmsInfo. Checks {@link
    * PSDbmsInfo#isSameDb(
    * PSDbmsInfo) } for a match on each
    * repository node.
    *
    * @param dbmsInfo dbms/repository info that need to be checked, may not be
    * <code>null</code>
    *
    * @return the matching repository, may be <code>null</code> if not found.
    *
    * @throws IllegalArgumentException if dbmsInfo is <code>null</code>
    * @throws IllegalStateException if user objects of immediate children of
    * root are not instances of <code>OSDbmsInfo</code> objects.
    */
   public OSDbmsInfo getMatchingRepository(PSDbmsInfo dbmsInfo)
   {
      if(dbmsInfo == null)
         throw new IllegalArgumentException("dbmsInfo may not be null");

      DefaultMutableTreeNode matchingNode = getMatchingRepositoryNode(dbmsInfo);
      if(matchingNode != null)
         return (OSDbmsInfo)matchingNode.getUserObject();

      return null;
   }


   /**
    * Gets the repository node representing the <code>PSDbmsInfo</code> object.
    * Checks {@link
    * PSDbmsInfo#isSameDb(
    * PSDbmsInfo) } for a match on each
    * repository node.
    *
    * @param dbmsInfo dbms/repository info that need to be checked, may not be
    * <code>null</code>
    *
    * @return the matching repository, may be <code>null</code> if not found.
    *
    * @throws IllegalArgumentException if dbmsInfo is <code>null</code>
    * @throws IllegalStateException if user objects of immediate children of
    * root are not instances of <code>OSDbmsInfo</code> objects.
    */
   private DefaultMutableTreeNode getMatchingRepositoryNode(PSDbmsInfo dbmsInfo)
   {
      if(dbmsInfo == null)
         throw new IllegalArgumentException("dbmsInfo may not be null");

      DefaultTreeModel model = (DefaultTreeModel)getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

      int count = root.getChildCount();
      for(int i=0; i<count; i++)
      {
         DefaultMutableTreeNode child =
            (DefaultMutableTreeNode)root.getChildAt(i);
         if(child.getUserObject() instanceof OSDbmsInfo)
         {
            OSDbmsInfo nodeInfo = (OSDbmsInfo)child.getUserObject();
            if(nodeInfo.isSameDb(dbmsInfo))
               return child;
         }
         else
            throw new IllegalStateException(
               "The children of root must be OSDbmsInfo objects");
      }

      return null;
   }

   /**
    * Gets the map of registered servers with <code>OSDbmsInfo</code> as key
    * and the list of <code>PSDeploymentServer</code>s using that
    * repository/database as value. The keys of map are sorted by the repository
    * alias name and the list of servers are sorted by server name and the
    * sorting is case insensitive and alphabetically ascending.
    *
    * @return the map, never <code>null</code>, may be empty.
    */
   public Map getRegisteredServerMap()
   {
      Map dbmsServerMap = new TreeMap(new PSStringComparator(
               PSStringComparator.SORT_CASE_SENSITIVE_ASC));

      DefaultTreeModel model = (DefaultTreeModel)getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

      int count = root.getChildCount();
      for(int i=0; i<count; i++)
      {
         DefaultMutableTreeNode child =
            (DefaultMutableTreeNode)root.getChildAt(i);
         if(child.getUserObject() instanceof OSDbmsInfo)
         {
            OSDbmsInfo nodeInfo = (OSDbmsInfo)child.getUserObject();
            int serverCount = child.getChildCount();
            if(serverCount == 0)
               throw new IllegalStateException(
                  "Invalid repository node, missing child server nodes.");
            List servers = new ArrayList();
            for(int j=0; j<serverCount; j++)
            {
               DefaultMutableTreeNode serverNode =
                  (DefaultMutableTreeNode)child.getChildAt(j);
               servers.add(serverNode.getUserObject());
            }
            dbmsServerMap.put(nodeInfo, servers);
         }
         else
            throw new IllegalStateException(
               "The children of root must be OSDbmsInfo objects");
      }

      return dbmsServerMap;
   }

   /**
    * Gets the list of registered server names.
    *
    * @return the list of <code>String</code>s never <code>null</code>, may be
    * empty.
    *
    * @throws IllegalStateException if a repository node does not have children
    * (server nodes) or children of root node do not represent repository nodes.
    */
   public List getRegisteredServerNames()
   {
      List serverNames = new ArrayList();
      Iterator regServers = getRegisteredServers().iterator();
      while(regServers.hasNext())
      {
         serverNames.add(
            ((PSDeploymentServer)regServers.next()).getServerName() );
      }

      return serverNames;
   }

   /**
    * Gets the list of registered servers (<code>PSDeploymentServer</code>).
    *
    * @return the list, never <code>null</code>, may be empty.
    *
    * @throws IllegalStateException if a repository node does not have children
    * (server nodes) or children of root node do not represent repository nodes.
    */
   public List getRegisteredServers()
   {
      List servers = new ArrayList();
      Iterator regServers = getRegisteredServerMap().values().iterator();
      while(regServers.hasNext())
         servers.addAll((List)regServers.next());

      return servers;
   }

   /**
    * Gets the existing repository alias names.
    *
    * @return the list of <code>String</code>s never <code>null</code>, may be
    * empty.
    */
   public List getExistingRepositoryAliases()
   {
      DefaultTreeModel model = (DefaultTreeModel)getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

      List existAliases = new ArrayList();

      int count = root.getChildCount();
      for(int i=0; i<count; i++)
      {
         DefaultMutableTreeNode child =
            (DefaultMutableTreeNode)root.getChildAt(i);
         if(child.getUserObject() instanceof OSDbmsInfo)
         {
            OSDbmsInfo nodeInfo = (OSDbmsInfo)child.getUserObject();
            existAliases.add(nodeInfo.getRepositoryAlias());
         }
         else
            throw new IllegalStateException(
               "The children of root must be OSDbmsInfo objects");
      }

      return existAliases;
   }

   /**
    * Finds out whether any registered servers exist or not.
    *
    * @return <code>true</code> if exists, otherwise <code>false</code>
    */
   public boolean doesServersExist()
   {
      DefaultTreeModel model = (DefaultTreeModel)getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

      return root.getChildCount() != 0;
   }

   /**
    * Gets the selected node's user object.
    *
    * @return the user object, may be <code>null</code> if there is no
    * selection. If there is a selected node, this will be an instance of <code>
    * String</code> or <code>OSDbmsInfo</code> or <code>PSDeploymentServer
    * </code>.
    */
   public Object getSelectedNodeObject()
   {
      DefaultMutableTreeNode treeNode =
         (DefaultMutableTreeNode)getSelectedNode();

      if(treeNode != null)
         return treeNode.getUserObject();

      return null;
   }

    /**
     * Gets the selected node in the tree.
     *
     * @return the node, may be <code>null</code> if there is no selection. If
     * there is a selected node, this will be an instance of <code>
     * DefaultMutableTreeNode</code>
     */
   public TreeNode getSelectedNode()
   {
      TreePath path = getSelectionPath();
      if(path != null)
      {
         DefaultMutableTreeNode selNode =
            (DefaultMutableTreeNode)path.getLastPathComponent();
         return selNode;
      }

      return null;
   }

   /**
    * Removes the selected node if the selected node is a server node. If all
    * server nodes are deleted from its parent (repository node), then that node
    * will also be removed. Does nothing if there is no selection or selected
    * node is not a server node.
    */
   public void removeSelectedServerNode()
   {
      TreePath path = getSelectionPath();
      if(path != null)
      {
         DefaultMutableTreeNode selNode =
            (DefaultMutableTreeNode)path.getLastPathComponent();
         if(selNode instanceof ServerNode)
         {
            TreePath parentPath = path.getParentPath();
            DefaultMutableTreeNode parentNode =
               (DefaultMutableTreeNode)parentPath.getLastPathComponent();

            DefaultTreeModel model = (DefaultTreeModel)getModel();
            model.removeNodeFromParent(selNode);

            //no more registered servers under this repository
            if(parentNode.getChildCount() == 0)
               model.removeNodeFromParent(parentNode);
         }
      }
   }

   /**
    * Disconnects from server represented by the supplied node.
    *
    * @param node the node that represents a server, may not be <code>null
    * </code> and must be instanceof <code>ServerNode</code>
    *
    * @throws IllegalArgumentException if node is not a valid server node.
    */
   public void disconnectFromServer(TreeNode node) throws PSDeployException
   {
      if(node instanceof ServerNode)
      {
         ServerNode serverNode = (ServerNode)node;
         PSDeploymentServer server =
            (PSDeploymentServer)serverNode.getUserObject();
         if(server.isConnected())
            server.disconnect();
      }
      else
         throw new IllegalArgumentException(
            "serverNode is not a node representing a deployment server");
   }
   
   //implements IPSViewDataChangeListener interface method to refresh server node
   //children
   public void dataChanged(final Object data)
   {     
      if(data instanceof PSDeploymentServer)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {  
               PSDeploymentServer server = (PSDeploymentServer)data;
               TreeNode node = getSelectedNode();
               ServerNode serverNode = (ServerNode)getMatchingServerNode(server);
      
               boolean isServerNodeChildrenSelected = false;            
               String selNodeId = null;      
               if(node != null && serverNode != null)
               {
                  selNodeId = getSelectedNodeObject().toString();
                  if(node.getParent() == serverNode || 
                     node.getParent().getParent() == serverNode)
                  {
                     isServerNodeChildrenSelected = true;            
                  }
               }
                  
               if(serverNode != null)
               {                  
                  serverNode.removeAllChildren();
                  serverNode.setLoadedChildren(false);      
                  DefaultTreeModel model = (DefaultTreeModel)getModel();
                  DefaultMutableTreeNode selNode = null;
                                    
                  if(server.isConnected())
                  {
                     serverNode.loadChildren();         
                     if(isServerNodeChildrenSelected)
                     {
                        for (int i = 0; i < serverNode.getChildCount() && 
                           selNode == null; i++) 
                        {
                           DefaultMutableTreeNode childNode = 
                              (DefaultMutableTreeNode) serverNode.getChildAt(i);
                           if(childNode.toString().equals(selNodeId))
                              selNode = childNode;
                           else
                           {
                              for (int j = 0; j < childNode.getChildCount() && 
                                 selNode == null; j++) 
                              {
                                 DefaultMutableTreeNode grandChildNode = 
                                    (DefaultMutableTreeNode)childNode.
                                    getChildAt(j);

                                 if(grandChildNode.toString().equals(selNodeId))
                                    selNode = grandChildNode;
                              }
                           }
                        }
                     }
                  }

                  model.nodeStructureChanged(serverNode);                                                            
                  if(selNode != null)  
                  {
                     TreePath path = new TreePath(selNode.getPath());
                     setSelectionPath(path);                  
                  }
                  else {
                     TreePath path = new TreePath(serverNode.getPath());
                     if(server.isConnected())
                        expandPath(path);
                     else
                     {
                        collapsePath(path);   
                        model.nodeStructureChanged(serverNode);                         
                     }
                     setSelectionPath(path);                                                   
                  }

               }
            }              
         });   
      }
      else
         throw new IllegalArgumentException(
            "data must be an instance of PSDeploymentServer");
   }
   
      /**
    * Gets the tree node that represents supplied server.
    * 
    * @param server the server that is user object of the tree node, assumed to 
    * be not <code>null</code>
    * 
    * @return the node, may be <code>null</code> if it did not find a matching
    * node.
    */
   private TreeNode getMatchingServerNode(PSDeploymentServer server)
   {
      DefaultTreeModel model = (DefaultTreeModel)getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

      int count = root.getChildCount();
      for(int i=0; i<count; i++)
      {
         DefaultMutableTreeNode child =
            (DefaultMutableTreeNode)root.getChildAt(i);
         if(child.getUserObject() instanceof OSDbmsInfo)
         {
            OSDbmsInfo nodeInfo = (OSDbmsInfo)child.getUserObject();
            int serverCount = child.getChildCount();
            for(int j=0; j<serverCount; j++)
            {
               DefaultMutableTreeNode serverNode =
                  (DefaultMutableTreeNode)child.getChildAt(j);
               PSDeploymentServer depServer =
                  (PSDeploymentServer)serverNode.getUserObject();
               if(depServer == server)
                  return serverNode;
            }
         }
      }
      return null;
   }



   /**
    * The node to represent a server node. Useful to dynamically load children
    * of this node.
    */
   private class ServerNode extends DynamicUtilTreeNode
   {
      /**
       * Constructs a node that represents a server with no children, but allows
       * children. This node can dynamically load the children.
       *
       * @param server the user object of the node, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if server is <code>null</code>
       */
      public ServerNode(PSDeploymentServer server)
      {
         super(server, new Vector());

         if(server == null)
            throw new IllegalArgumentException("server may not be null.");
      }

      /**
       * Overridden to load the children dynamically. If the children are
       * already loaded, it simply returns. Adds a 'Source' node with
       * 'Descriptors' child node as a child node of this node if the server has
       * descriptors. Adds a 'Target' node with 'Archives' and 'Packages' child
       * nodes as a child node of this node if the server has deployed packages.
       */
      protected void loadChildren()
      {
         PSDeploymentServer server = (PSDeploymentServer)getUserObject();

         if(loadedChildren || !server.isConnected())
            return;

         int i = 0;

         try {
            if(server.getDescriptors(false).getResults().hasNext())
            {
               DefaultMutableTreeNode sourceNode =
                  new DefaultMutableTreeNode(ms_source);
               DefaultMutableTreeNode descriptorsNode =
                  new DefaultMutableTreeNode(ms_descriptors);
               sourceNode.add(descriptorsNode);
               insert(sourceNode, i++);
            }
            
            if(server.getArchives(false).getResults().hasNext() ||
               server.getPackages(false).getResults().hasNext())            
            {
               DefaultMutableTreeNode targetNode =
                  new DefaultMutableTreeNode(ms_target);
               DefaultMutableTreeNode archivesNode =
                  new DefaultMutableTreeNode(ms_archives);
               DefaultMutableTreeNode packagesNode =
                  new DefaultMutableTreeNode(ms_packages);
               targetNode.add(archivesNode);
               targetNode.add(packagesNode);
               insert(targetNode, i++);
            }
         }
         catch(PSDeployException e)
         {
            PSDeploymentClient.getErrorDialog().showErrorMessage(
               e.getLocalizedMessage(), ms_res.getString("errorTitle"));
         }
         finally
         {
            setLoadedChildren(true);
         }
      }

      /**
       * Sets the children loaded flag. Call this method with <code>false</code>
       * when the server represented by this node is disconnected to avoid the
       * same children to be displayed when the server is reconnected.
       *
       * @param flag if <code>true</code> the children are already loaded,
       * otherwise children are not loaded.
       */
      public void setLoadedChildren(boolean flag)
      {
         loadedChildren = flag;
      }

   }

   /**
    * The renderer to use with this tree to display a different icon based on
    * the node type.
    */
   private class ServerTreeCellRenderer extends DefaultTreeCellRenderer
   {
      public Component getTreeCellRendererComponent(JTree tree, Object value,
         boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
      {
         super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
            row, hasFocus);

         DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
         Object userObj = node.getUserObject();

         String iconSource = null;
         if(userObj instanceof OSDbmsInfo) //repository node
            iconSource = "gif_repository";
         else if(userObj instanceof PSDeploymentServer) //server node
            iconSource = "gif_server";
         else {
            String nodeString = userObj.toString();
            if(nodeString.equals(PSServersTree.ms_descriptors))
               iconSource = "gif_descriptors";
            else if(nodeString.equals(PSServersTree.ms_archives))
               iconSource = "gif_archives";
            else if(nodeString.equals(PSServersTree.ms_packages))
               iconSource = "gif_packages";
         }

         if(iconSource != null)
         {
            setIcon( PSDeploymentClient.getImageLoader().getImage(
               ms_res.getString(iconSource) ) );
         }

         return this;
      }
   }

   /**
    * The resource string to represent the 'Source' node.
    */
   public static final String ms_source;

   /**
    * The resource string to represent the 'Target' node.
    */
   public static final String ms_target;

   /**
    * The resource string to represent the 'Descriptors' node.
    */
   public static final String ms_descriptors;

   /**
    * The resource string to represent the 'Archives' node.
    */
   public static final String ms_archives;

   /**
    * The resource string to represent the 'Packages' node.
    */
   public static final String ms_packages;

   /**
    * The resource bundle to use, for tree labels, icons or error messages,
    * initialized statically.
    */
   private static final PSResources ms_res;

   //static block to intialize all resource strings
   static {
      ms_res = PSDeploymentClient.getResources();
      ms_source = ms_res.getString("source");
      ms_target = ms_res.getString("target");
      ms_descriptors = ms_res.getString("descriptors");
      ms_archives = ms_res.getString("archives");
      ms_packages = ms_res.getString("packages");
   }

}

/******************************************************************************
 *
 * [ PSUserDependencyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSUserDependency;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Dialog to set user dependencies on a dependency.
 */
public class PSUserDependencyDialog extends PSDialog
   implements ListSelectionListener, TreeSelectionListener, 
   TreeWillExpandListener
{
   /**
    * Constructs this dialog.
    * 
    * @param parent the parent dialog of this dialog, may be <code>null</code>.
    * @param deploymentServer the deployment server on which the user 
    * dependencies need to be set for the supplied dependency, may not be <code>
    * null</code> and must be connected.
    * @param parentDep the parent dependency to which the user dependencies need
    * to be added, may not be <code>null</code> and must support user 
    * dependencies.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSUserDependencyDialog(Dialog parent, 
      PSDeploymentServer deploymentServer, PSDependency parentDep)
   {
      super(parent);
      
      if(deploymentServer == null)
         throw new IllegalArgumentException("deploymentServer may not be null");
         
      if(!deploymentServer.isConnected())
         throw new IllegalArgumentException(
            "deploymentServer must be connected");
      
      if(parentDep == null)
         throw new IllegalArgumentException("parentDep may not be null");
      
      if(!parentDep.supportsUserDependencies())
         throw new IllegalArgumentException(
            "parentDep must support user dependencies");
            
      m_server = deploymentServer;
      m_dependency = parentDep;
      
      initDialog();
   }
   
  /**
    * Creates the dialog framework with border layout keeping the description 
    * panel on north, controls panel on center and command panel on south.
    */
   protected void initDialog()
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BorderLayout(10, 20));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      setTitle( MessageFormat.format(getResourceString("title"), 
         new String[] {m_dependency.getDisplayName()}) );
      
      JPanel descPanel = new JPanel();
      descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
      int steps = 2;
      try 
      {
         steps = Integer.parseInt(getResourceString("descStepCount"));
      }
      catch (NumberFormatException ex) 
      {
         //uses the default  
      }

      for (int i = 1; i <= steps; i++) 
      {
         descPanel.add(new JLabel(getResourceString("descStep" + i), 
            SwingConstants.LEFT));
      }
      panel.add(descPanel, BorderLayout.NORTH);

      //controls panel in the center of the dialog.
      JPanel centerPanel = new JPanel();
      centerPanel.setBorder(
                  BorderFactory.createTitledBorder(
                       BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));   
      centerPanel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(centerPanel, BorderLayout.CENTER);
      
      //the left panel with available elements tree
      centerPanel.add(Box.createHorizontalGlue());
      centerPanel.add(Box.createHorizontalStrut(20));
      centerPanel.add(createFileSystemPanel());
      centerPanel.add(Box.createHorizontalStrut(20));
      centerPanel.add(Box.createHorizontalGlue());
      
      //the panel with add and remove buttons
      JPanel addRemovePanel = new JPanel();      
      addRemovePanel.setLayout(new BoxLayout(addRemovePanel, BoxLayout.Y_AXIS));
      addRemovePanel.add(Box.createVerticalGlue());
      m_addButton = new UTFixedButton(
         getResourceString("add"), new Dimension(100,24));
      m_addButton.setMnemonic(getResourceString("add.mn").charAt(0));
      m_addButton.setEnabled(false);
      m_addButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            onAdd();
         }
      });
      addRemovePanel.add(m_addButton);
            
      m_removeButton = new UTFixedButton(getResourceString("remove"), 
         new Dimension(100,24));
      m_removeButton.setMnemonic(getResourceString("remove.mn").charAt(0));
      addRemovePanel.add(Box.createVerticalStrut(10));      
      m_removeButton.setEnabled(false);         
      m_removeButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            onRemove();
         }
      });
      addRemovePanel.add(m_removeButton);
      addRemovePanel.add(Box.createVerticalGlue());
      centerPanel.add(addRemovePanel);
      centerPanel.add(Box.createHorizontalStrut(20));
      centerPanel.add(Box.createHorizontalGlue());

      
      //the panel with user dependencies to set on the dependency
      centerPanel.add(createUserDependenciesPanel());
      centerPanel.add(Box.createHorizontalStrut(20));
      centerPanel.add(Box.createHorizontalGlue());
      
      panel.add(createCommandPanel(SwingConstants.HORIZONTAL, true), 
         BorderLayout.SOUTH);
      
      pack();      
      center();    
      setResizable(true);
   }
   
   /**
    * Creates the panel with the 'Rhythmyx File System' label and tree enclosed 
    * in a scrollpane. Adds this object as a 'TreeWillExpandListener' to the 
    * tree.
    * 
    * @return the panel, never <code>null</code>
    */
   private JPanel createFileSystemPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel(getResourceString("fileSystem"), 
         SwingConstants.LEFT), BorderLayout.NORTH);

      DefaultMutableTreeNode rootNode = new JTree.DynamicUtilTreeNode(
         getResourceString("rxroot"), new Vector());
      try {
         addChildren(rootNode, m_server.getServerFileSystem(null));
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(getOwner(), e.getLocalizedMessage(), 
            getResourceString("errorTitle"));
      }
      DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
      
      m_fileSystemTree = new JTree(treeModel);
      m_fileSystemTree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
      m_fileSystemTree.addTreeWillExpandListener(this);
      m_fileSystemTree.addTreeSelectionListener(this);
      m_fileSystemTree.setCellRenderer(new DefaultTreeCellRenderer(){
         public Component getTreeCellRendererComponent(JTree tree,
            Object value, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
         {
            super.getTreeCellRendererComponent(tree, value, selected, expanded,
               leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if(node.isLeaf() && node.getUserObject() instanceof PSCatalogResult)
            {
               String filePath = 
                  ((PSCatalogResult)node.getUserObject()).getID();
               if(doesUserDepListContain(filePath))
                  setForeground(Color.cyan);
            }            

            return this;
         }
      });
      
      JScrollPane treePane = new JScrollPane(m_fileSystemTree);
      treePane.setPreferredSize(new Dimension(200, 200));
      treePane.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(treePane, BorderLayout.CENTER);
      
      panel.setAlignmentX(LEFT_ALIGNMENT);      
      return panel;
   }
   
   /**
    * Creates the panel with 'User Dependency Files' label and a list box 
    * enclosed in scroll pane. 
    * 
    * @return the panel, never <code>null</code>
    */
   private JPanel createUserDependenciesPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel(getResourceString("userDeps"), 
         SwingConstants.LEFT), BorderLayout.NORTH);
      
      DefaultListModel model = new DefaultListModel();
      Iterator userDeps = null;
      try {
         if(m_dependency.getDependencies() == null)
            m_server.getDeploymentManager().loadDependencies(m_dependency);
         userDeps = m_dependency.getDependencies(PSDependency.TYPE_USER);
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(getOwner(), e.getLocalizedMessage(), 
            getResourceString("errorTitle"));
      }

      if(userDeps != null)
      {
         while(userDeps.hasNext())
         {
            PSUserDependency userDep = (PSUserDependency)userDeps.next();
            model.addElement(userDep.getPath().toString());
         }
      }
      m_userDepsList = new JList(model);
      m_userDepsList.setSelectionMode(
         ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      m_userDepsList.getSelectionModel().addListSelectionListener(this);
      
      JScrollPane listPane = new JScrollPane(m_userDepsList);
      listPane.setPreferredSize(new Dimension(150, 200));
      listPane.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(listPane, BorderLayout.CENTER);
      
      panel.setAlignmentX(LEFT_ALIGNMENT);      
      return panel;
   }
   
   /**
    * Action method for tree expansion event. If the expanded node is a node 
    * that allows children, but not loaded with children (any directory that is
    * not yet expanded), catalogs the directories or files under that directory
    * from server and adds the children. Displays an error message if an 
    * exception happens in getting child data for the node.
    *
    * @param event the tree expansion event, assumed not to be <code>null</code>
    * as Swing model calls this method with an event when tree expansion event
    * occurs.
    */
   public void treeWillExpand(TreeExpansionEvent event)
   {
      DefaultMutableTreeNode node = 
         (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
         
      try {
         if(node.getAllowsChildren() && node.getChildCount() == 0)
         {            
            if(node.getUserObject() instanceof PSCatalogResult)
            {
               String id = ((PSCatalogResult)node.getUserObject()).getID();               
               //remove separator at the end
               String directory = id.substring(0, id.length() - 
                  IPSDeployConstants.CAT_FILE_SEP.length()); 
               addChildren(node, m_server.getServerFileSystem(directory));
            }
         }
      }
      catch(PSDeployException e)
      {
         //display error message
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(), 
            getResourceString("errorTitle"));
      }

   }
   
   /**
    * Creates nodes for the supplied children and adds them as child nodes to 
    * the supplied node. If the children represent a directory path, then it is
    * created as a node that allows children.
    * 
    * @param node the node to which the children should be added, assumed not to 
    * be <code>null</code>
    * @param files the result set containing file paths as set of <code>
    * PSCatalogResult</code>s, assumed not to be <code>null</code>
    */
   private void addChildren(DefaultMutableTreeNode node, 
      PSCatalogResultSet files)
   {
      Iterator results = files.getResults();
      while(results.hasNext())
      {
         PSCatalogResult result = (PSCatalogResult)results.next();
         Vector children = null;
         if(result.getID().endsWith(IPSDeployConstants.CAT_FILE_SEP))
            children = new Vector();                     
         node.add( new JTree.DynamicUtilTreeNode(result, children) );
      }
   }

   
   //nothing to implement
   public void treeWillCollapse(TreeExpansionEvent event)
   {
   }
   
   /**
    * Called when selection changes in Rhythmyx file system tree. 
    * 
    * @param e the event that characterizes the change, assumed not to be <code>
    * null</code> as this method will be called by <code>Swing</code> model when
    * a tree selection changes.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      Object source = e.getSource();
      if(source == m_fileSystemTree)
      {
         updateAddButtonState();
      }
   }
   
   /**
    * Updates the enabled state of the add button based on the tree selection.
    * This button is enabled only if the selected nodes represents files that 
    * have not already been added.
    */
   private void updateAddButtonState()
   {
      boolean canAdd = true;
      TreePath[] selPaths = m_fileSystemTree.getSelectionPaths();
      if (selPaths == null)
      {
         canAdd = false;
      }
      else
      {
         for (int i = 0; i < selPaths.length && canAdd; i++)
         {
            DefaultMutableTreeNode selNode = 
               (DefaultMutableTreeNode)selPaths[i].getLastPathComponent();
            if( !selNode.isLeaf() ) //file node is a leaf node.
            {
               canAdd = false;
            }
            else 
            {
               String filePath = 
                  ((PSCatalogResult)selNode.getUserObject()).getID();
               if(doesUserDepListContain(filePath))
               {
                  canAdd = false;
               }
            }
         }
      }
      
      m_addButton.setEnabled(canAdd);
   }

   /**
    * Called when selection changes in User Dependnecies list. 'Remove' button 
    * is enabled only if there is selection in the list. 
    * 
    * @param e the event that characterizes the change, assumed not to be <code>
    * null</code> as this method will be called by <code>Swing</code> model when
    * selection changes in list.
    */
   public void valueChanged(ListSelectionEvent e)
   {
      int[] selIndices = m_userDepsList.getSelectedIndices();
      if(selIndices.length == 0)
         m_removeButton.setEnabled(false);
      else 
         m_removeButton.setEnabled(true);
   }
   

   /**
    * Adds the selected nodes in the file system tree to 'User Dependencies' 
    * list if the node is a file. If the node is already added to the list, it 
    * does nothing. 
    */  
   private void onAdd()
   {
      TreePath[] selPaths = m_fileSystemTree.getSelectionPaths();
      if (selPaths != null)
      {
         DefaultTreeModel treeModel = 
            (DefaultTreeModel) m_fileSystemTree.getModel();
         for (int i = 0; i < selPaths.length; i++)
         {
            DefaultMutableTreeNode node = 
               (DefaultMutableTreeNode)selPaths[i].getLastPathComponent();
            if(node.isLeaf() && node.getUserObject() instanceof PSCatalogResult)
            {
               String filePath = 
                  ((PSCatalogResult)node.getUserObject()).getID();
               if(!doesUserDepListContain(filePath))
               {
                  ((DefaultListModel)m_userDepsList.getModel()).addElement(
                     filePath);
                  
                  //refresh the node
                  treeModel.nodeChanged(node);
               }
            }
         }
         
         updateAddButtonState();
      }
   }
   
   /**
    * Checks whether 'User Dependencies' list contains supplied file path.
    * 
    * @param filePath the file path to check, assumed not <code>null</code> or
    * empty.
    * 
    * @return <code>true</code> if it contains, otherwise <code>false</code>
    */
   private boolean doesUserDepListContain(String filePath)
   {
      DefaultListModel listModel = (DefaultListModel)m_userDepsList.getModel();      
      return listModel.contains(filePath);
   }
   
   /**
    * Called when user removes's an object from the 'User Dependencies' list. 
    * Removes the selected elements from the list.
    */
   private void onRemove()
   {
      DefaultListModel model = 
         (DefaultListModel)m_userDepsList.getModel();
      int[] selIndices = m_userDepsList.getSelectedIndices();
      for (int i = selIndices.length-1; i >= 0 ; i--) 
         model.removeElementAt(selIndices[i]);
      
      // now refresh tree nodes
      DefaultTreeModel treeModel = 
         (DefaultTreeModel)m_fileSystemTree.getModel();
      int count = m_fileSystemTree.getRowCount();
      for(int i = 0; i <count; i++)
      {
         TreePath path = m_fileSystemTree.getPathForRow(i);
         DefaultMutableTreeNode visNode = 
            (DefaultMutableTreeNode)path.getLastPathComponent();
         if(visNode.isLeaf())
            treeModel.nodeChanged(visNode);   
      }
      
      updateAddButtonState();
   }
   
   /**
    * Creates the user dependencies for all the files that are selected by the 
    * user in this dialog.
    */
   public void onOk()
   {
      // create map of current user deps
      Map curUserDeps = new HashMap();
      Iterator deps = m_dependency.getDependencies(PSDependency.TYPE_USER);
      while (deps.hasNext())
      {
         PSDependency dep = (PSDependency) deps.next();
         PSUserDependency userDep = (PSUserDependency) dep;
         curUserDeps.put(userDep.getPath().getPath(), dep);
      }
      
      try 
      {
         // add any new user dependencies and save them, remove existing ones
         // still in the list from the map.
         DefaultListModel model = 
            (DefaultListModel)m_userDepsList.getModel();
         for (int i = 0; i < model.size(); i++) 
         {
            PSUserDependency userDep;
            String userDepPath = (String)model.getElementAt(i);
            userDep = (PSUserDependency) curUserDeps.remove(userDepPath);
            if(userDep == null)
            {
               userDep = m_dependency.addUserDependency(new File(userDepPath));

               //save only the if m_dependency is not a custom type element.               
               if( !m_dependency.getObjectType().equals(
                  IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM) )
               {
                  m_server.getDeploymentManager().saveUserDependency(userDep);            
               }
            }
         }
         
         // now remove and delete any current user dependencies not listed
         Iterator removeDeps = curUserDeps.values().iterator();
         while (removeDeps.hasNext())
         {
            PSUserDependency removeDep = (PSUserDependency) removeDeps.next();
            m_dependency.removeUserDependency(removeDep.getPath());
            m_server.getDeploymentManager().deleteUserDependency(removeDep);
         }
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(), 
            getResourceString("errorTitle"));
         return;
      }
      super.onOk();
   }
   
   /**
    * The deployment server on which the user dependencies are saved, 
    * initialized in the constructor and never <code>null</code> or modified 
    * after that. Used to get the file structure under rhythmyx server root.
    */   
   private PSDeploymentServer m_server;

   /**
    * The dependency for which the user dependencies need to be set/edited, 
    * initialized in the constructor and never <code>null</code> after that. The
    * user dependencies are modified in <code>onOk()</code>.
    */   
   private PSDependency m_dependency;
   
   /**
    * The tree which displays the available deployable elements of the 
    * deployment server, initialized in <code>createFileSystemPanel()</code> and
    * never <code>null</code> after that. The tree nodes gets added as user 
    * expands the tree.
    */
   private JTree m_fileSystemTree;
   
   /**
    * The list box which displays all the deployable elements that are to be 
    * packaged, initialized in <code>createUserDependenciesPanel()</code> and 
    * never <code>null</code> after that. The elements will be added to the list
    * as user adds or in <code>init()</code> if the descriptor supplied to this 
    * dialog has elements.
    */
   private JList m_userDepsList;   
   
   /**
    * The button used to add the selected deployable element in the <code>
    * m_fileSystemTree</code> tree to <code>m_userDepsList</code> list, 
    * initialized in <code>initDialog()</code> and never <code>null</code> after
    * that. This will be disabled if there is no selection in the tree or if the
    * selected element is not a deployable element or the selected deployable
    * element is already added to the list.
    */ 
   private JButton m_addButton;
   
   /**
    * The button used to remove the selected elements in the <code>
    * m_userDepsList</code> list, initialized in <code>initDialog()</code> 
    * and never <code>null</code> after that. This will be disabled if there is 
    * no selection in the list.
    */
   private JButton m_removeButton;

}

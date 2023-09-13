/******************************************************************************
 *
 * [ PSSelectionPage.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.packager.ui.model.PSPackagerClientModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Dialog to set user dependencies on a dependency.
 */
public class PSFileResourcesDialog extends JDialog
   implements ListSelectionListener, TreeSelectionListener, 
   TreeWillExpandListener
{
   /**
    * Constructs this dialog.
    */
   public PSFileResourcesDialog()
   {      
      initDialog();
   }
   
  /**
    * Creates the dialog framework with border layout keeping the description 
    * panel on north, controls panel on center and command panel on south.
    */
   protected void initDialog()
   {    
      URL imageFile = getClass().getResource(getResourceString("title.image"));
      
      if (imageFile != null)
      {
         ImageIcon icon = new ImageIcon(imageFile);
         setIconImage(icon.getImage());
      }  
      MigLayout layout = new MigLayout(
            "wrap 3",
            "[center][center][center]",
            "[center][center]");
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(layout);
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      setTitle(getResourceString("title"));
      //File System
      panel.add(createFileSystemPanel());
      //Center Buttons
      panel.add(getAddRemovePanel());
      //the panel with user dependencies to set on the dependency
      panel.add(createUserDependenciesPanel());
      //Bottom buttons
      panel.add(getCommandPanel(), "span");
      
      pack();      
      center();    
      setResizable(true);
   }
   /**
    * Creates add remove button panel
    * 
    * @return the panel, never <code>null</code>
    */
   private JPanel getAddRemovePanel()
{
      //the panel with add and remove buttons
      JPanel addRemovePanel = new JPanel();
      MigLayout layout = new MigLayout(
            "wrap 1",
            "[]",
            "[bottom][top]");
      
      addRemovePanel.setLayout(layout);
      m_addButton = new JButton(
         getResourceString("add"));
      m_addButton.setSize(new Dimension(100,24));
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
            
      m_removeButton = new JButton(getResourceString("remove"));
      m_removeButton.setSize(new Dimension(100,24));
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
      
   return addRemovePanel;
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
      UIManager.put("Tree.rowHeight",new Integer(18)); 
      
      MigLayout layout = new MigLayout(
            "wrap 1",
            "[center]",
            "[center]");
      JPanel panel = new JPanel();
      panel.setLayout(layout);
      panel.add(new JLabel(getResourceString("fileSystem")));

      DefaultMutableTreeNode rootNode = new JTree.DynamicUtilTreeNode(
         getResourceString("rxroot"), new Vector());
      try {
         addChildren(rootNode, packagerModel.getServerFileSystem(null));
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
      
      DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer(){
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
                     setForeground(Color.gray);
               }            

               return this;
            }
         };
      
      //Tree Icons
      URL leafFile =       getClass().getResource(
            getResourceString("image.file"));
      URL folderOpenFile = getClass().getResource(
            getResourceString("image.openFolder"));
      URL folderClosedFile = getClass().getResource(
            getResourceString("image.closedFolder"));
      if (leafFile != null && 
            folderOpenFile != null && 
            folderClosedFile != null) 
      {
         
          ImageIcon leafIcon = new ImageIcon(leafFile);
          ImageIcon folderOpenIcon = new ImageIcon(folderOpenFile);
          ImageIcon folderClosedIcon = new ImageIcon(folderClosedFile);
          renderer.setLeafIcon(leafIcon);
          renderer.setOpenIcon(folderOpenIcon);
          renderer.setClosedIcon(folderClosedIcon);
          m_fileSystemTree.setCellRenderer(renderer);
      }
      
      JScrollPane treePane = new JScrollPane(m_fileSystemTree);
      treePane.setPreferredSize(new Dimension(250, 200));
      panel.add(treePane);
            
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
      MigLayout layout = new MigLayout(
            "wrap 1",
            "[center]",
            "[center]");
      JPanel panel = new JPanel();
      panel.setLayout(layout);
      panel.add(new JLabel(getResourceString("userDeps")));
      
      DefaultListModel model = new DefaultListModel();
      //Load List
      ArrayList<String> userDeps = 
         (ArrayList<String>)packagerModel.getFileResources();
      
      if(userDeps != null)
      {
         for(String path:userDeps)
         {
            model.addElement(path);
         }
      }
      m_userDepsList = new JList(model);
      m_userDepsList.setSelectionMode(
         ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      m_userDepsList.getSelectionModel().addListSelectionListener(this);
      
      JScrollPane listPane = new JScrollPane(m_userDepsList);
      listPane.setPreferredSize(new Dimension(250, 200));
      panel.add(listPane);
          
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
               addChildren(node, packagerModel.getServerFileSystem(directory));
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
            if(!selNode.isLeaf()) //node is a dir node.
            {
               break;
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
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      expandAndSelectAllSelectedDirectories();
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
                  //Set Dirty
                  packagerModel.setAsDirty();
               }
            }            
         }
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         updateAddButtonState();
      }
   }
   
   /**
    * Expand all selected directories and select all children.
    */
   private void expandAndSelectAllSelectedDirectories()
   {
      TreePath[] selPaths = m_fileSystemTree.getSelectionPaths();
      List<TreePath> paths = new ArrayList<TreePath>();
      if (selPaths != null)
      {
         DefaultTreeModel treeModel = 
            (DefaultTreeModel) m_fileSystemTree.getModel();
         for (int i = 0; i < selPaths.length; i++)
         {
            DefaultMutableTreeNode node = 
               (DefaultMutableTreeNode)selPaths[i].getLastPathComponent();
            if(!node.isLeaf())
            {
               expand(node, paths);
            }
            else
            {
               paths.add(selPaths[i]);               
            }
         }
         m_fileSystemTree.setSelectionPaths(
            (TreePath[])paths.toArray(new TreePath[]{}));
      }
   }
   
   /**
    * Expand folder and all sub-folders recursively.
    * @param node folder node to start expansion on. Assumed
    * not <code>null</code>.
    * @param paths list to store paths of all children, assumed
    * not <code>null</code>.
    */
   private void expand(DefaultMutableTreeNode node, List<TreePath> paths)
   {
      JTree tree = m_fileSystemTree;
      if(!node.isLeaf())
      {
         TreePath path = new TreePath(node.getPath());
         if(!tree.isExpanded(path));
         {
            tree.expandPath(path);
         }
         Enumeration children = node.children();
         while(children.hasMoreElements())
         {
            DefaultMutableTreeNode child = 
               (DefaultMutableTreeNode)children.nextElement();
            expand(child, paths);
         }
      }
      else
      {
         paths.add(new TreePath(node.getPath()));
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
    * Creates the standard command panel with 'OK' and 'Cancel' buttons.
    * Assigns the default actions to the buttons. Sets the 'OK' button as
    * default to the dialog.
    *
    * @return the panel, never <code>null</code>
    *
    */
   protected JPanel getCommandPanel()
   {
      MigLayout layout = new MigLayout(
            "wrap 2",
            "[center]",
            "[center]");
     Dimension buttonSize = new Dimension(100,20);
      
     JPanel mainPanel = new JPanel();
     mainPanel.setLayout(layout);

     //OK Button
     JButton okButton = new JButton(getResourceString("button.ok"));
     okButton.setMnemonic((int) getResourceString("button.ok.m").charAt(0));
     okButton.setSize(buttonSize);
     okButton.addActionListener(new ActionListener()
     {
        public void actionPerformed(@SuppressWarnings("unused")
        ActionEvent e)
        {
           onOk();
        }
     });
     
     //Cancel Button
     JButton cancelButton = new JButton(getResourceString("button.cancel"));
     cancelButton.setMnemonic(
           (int) getResourceString("button.cancel.m").charAt(0));
     cancelButton.setSize(buttonSize);
     cancelButton.addActionListener(new ActionListener()
     {
        public void actionPerformed(@SuppressWarnings("unused")
        ActionEvent e)
        {
           onCancel();
        }
     });
     
     mainPanel.add(okButton);
     mainPanel.add(cancelButton);
      return mainPanel;
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
      //Set Dirty
      packagerModel.setAsDirty();
      
      updateAddButtonState();
   }
   
   /**
    * Creates the user dependencies for all the files that are selected by the 
    * user in this dialog.
    */
   public void onOk()
   {
      // Add file paths to Model.
      List<String> paths = packagerModel.getFileResources();
      
      //Clear before adding
      paths.clear();
      //Add Elements
      for(int i = 0; i < m_userDepsList.getModel().getSize(); i++) 
      {
         paths.add(m_userDepsList.getModel().getElementAt(i).toString());
      } 
      setVisible(false);
      dispose();
   }

   /**
    * Centers the dialog on the screen, based on its current size.
    */
   public void center()
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation(( screenSize.width - size.width ) / 2,
            ( screenSize.height - size.height ) / 2 );
   }
   
   /**
    * The action performed by the Cancel/Close button.
    */
   private void onCancel()
   {
      setVisible(false);
      dispose();
   }
   
   /**
    * Get resource text
    * 
    * @param key
    * @return text
    */
   private String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(this.getClass(), key);
   }
   
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
   
   /**
    * Call to get model
    */
   private PSPackagerClientModel packagerModel = 
      PSPackagerClient.getFrame().getModel();

}

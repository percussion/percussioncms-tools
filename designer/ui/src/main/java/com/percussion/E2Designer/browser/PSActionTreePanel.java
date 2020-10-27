/*******************************************************************************
 *
 * [ PSActionTreePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSChildActions;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSMenuChild;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.guitools.ErrorDialogs;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class PSActionTreePanel extends JPanel implements KeyListener,
   ItemListener
{
   /**
    * Consructs the panel.
    */
   public PSActionTreePanel(PSDbComponentCollection dbCollect)
   {
      m_dbCollect = dbCollect;
      populateIdMap();
      populateTreeList();
      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      setLayout(new BorderLayout());
      Border b = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      setBorder(b);
      JPanel treePane = new JPanel();
      treePane.setLayout(new BorderLayout());
      b = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder()
         , b);
      treePane.setBorder(b);
      PSTreeObject rootUserObj = new PSTreeObject(ms_res.getString("root.name"),
         new JPanel());
      // read the default path.
      m_rootNode = new
         DefaultMutableTreeNode(rootUserObj);

      m_tree = new JTree(m_rootNode);
      DefaultTreeCellRenderer renderer =
         (DefaultTreeCellRenderer) m_tree.getCellRenderer();
      renderer.setLeafIcon(ms_leafIcon);

      createTree();
      m_tree.addTreeSelectionListener(new FormatTreeListener());
      m_tree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.SINGLE_TREE_SELECTION);
      m_tree.setEditable(false);
      JScrollPane scrollPane = new JScrollPane(m_tree);
      treePane.add(scrollPane, BorderLayout.CENTER);

      JPanel btnPane = new JPanel();
      btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));
      JButton newButton = new UTFixedButton(ms_res.getString("menu.new"));
      newButton.setMnemonic(ms_res.getString("menu.new.mn").charAt(0));

      newButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            addNode(null);
            m_tree.requestFocus();
         }
      });
      m_deleteButton.setMnemonic(ms_res.getString("menu.delete.mn").charAt(0));
      m_deleteButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            remove();
         }
      });
      btnPane.add(Box.createHorizontalGlue());
      btnPane.add(newButton);
      btnPane.add(Box.createHorizontalStrut(20));
      btnPane.add(m_deleteButton);
      btnPane.add(Box.createHorizontalGlue());

      btnPane.setBorder(BorderFactory.createEmptyBorder(10, 0,0,0));
      treePane.add(btnPane, BorderLayout.SOUTH);

      add(treePane, BorderLayout.CENTER);

      //Create the popup menu.
      addPopupMenu();
      m_tree.setSelectionRow(0);
      m_tree.setShowsRootHandles(true);
      m_tree.setRequestFocusEnabled(true);
   }

   private void populateIdMap()
   {
      Iterator itr = m_dbCollect.iterator();
      PSAction action = null;
      PSKey key = null;
      String id = null;
      while(itr.hasNext())
      {
         action = (PSAction)itr.next();
         key = action.getLocator();
         id = key.getPart(key.getDefinition()[0]);
         m_idActionsMap.put(id, action);
      }
   }

   /**
    * Adds popup menu to the panel. Menu gets displayed on right click of the
    * mouse in the tree area.
    */
   public void addPopupMenu()
   {
      m_popup = new JPopupMenu();
      JMenuItem menuItem = new JMenuItem(ms_res.getString("menu.new"));
      menuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_N, ActionEvent.CTRL_MASK));
      menuItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            addNode(null);
         }
      });
      m_popup.add(menuItem);
      menuItem = new JMenuItem(ms_res.getString("menu.copy"));
      menuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_C, ActionEvent.CTRL_MASK));
      menuItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            copy();
         }
      });
      m_popup.add(menuItem);
      menuItem = new JMenuItem(ms_res.getString("menu.paste"));
      menuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_V, ActionEvent.CTRL_MASK));
      menuItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            paste();
         }
      });
      m_popup.add(menuItem);

      menuItem = new JMenuItem(ms_res.getString("menu.delete"));
      menuItem.setAccelerator(KeyStroke.getKeyStroke("Del"));

      menuItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            remove();
         }
      });
      m_popup.add(menuItem);
      MouseListener popupListener = new PopupListener();
      m_tree.addMouseListener(popupListener);
   }


   public void itemStateChanged(ItemEvent e)
   {
      {
         if (m_tree != null)
         {
            TreePath path = m_tree.getSelectionPath();
            if (path != null)
            {
               DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                  path.getLastPathComponent();
               if (node != null)
               {
                  PSTreeObject usr = (PSTreeObject)node.getUserObject();
                  JPanel pane = (JPanel)usr.getUIObject();
                  if ((DefaultMutableTreeNode)m_tree.getModel().getRoot() != node)
                  {
                     Component c =  pane.getComponent(0);
                     if (c instanceof JTabbedPane)
                     {
                        JTabbedPane tab = (JTabbedPane)c;
                        DisplayFormatColumnPanel colPane =
                              (DisplayFormatColumnPanel)tab.getComponentAt(
                              tab.indexOfTab(ms_res.getString("tabname.columns")));
                        if (e.getStateChange() == ItemEvent.SELECTED)
                           colPane.enableCategoriesTable(false);
                        else
                           colPane.enableCategoriesTable(true);
                     }
                  }
               }
            }
         }
      }
   }


   /**
    * Sets the state of popup menu items and 'New' and 'Delete' buttons in the
    * panel to enabled or disabled based on the node selected in the tree.
    * If the root node is selected then 'New' button is enabled, 'Delete' button
    * is disabled 'New' menu item is enabled, 'Copy', 'Paste' and 'Delete' items
    * are disabled in the popup menu. If any of the child nodes is selected
    * then just the opposite of what's described above happens.
    */
   private void setPopupMenuBtnState()
   {
      TreePath currentSelection = m_tree.getSelectionPath();
      MenuElement[] menuArr = m_popup.getSubElements();
      JMenuItem newItem = (JMenuItem)menuArr[0].getComponent();
      JMenuItem copyItem = (JMenuItem)menuArr[1].getComponent();
      JMenuItem pasteItem = (JMenuItem)menuArr[2].getComponent();
      JMenuItem deleteItem = (JMenuItem)menuArr[3].getComponent();
      if (currentSelection != null)
      {
         DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) currentSelection.getLastPathComponent();
         if (node.isRoot())
         {
            newItem.setEnabled(true);
            deleteItem.setEnabled(false);
            copyItem.setEnabled(false);
            pasteItem.setEnabled(m_clipboard != null);
         }
         else
         {
            newItem.setEnabled(false);
            deleteItem.setEnabled(true);
            pasteItem.setEnabled(m_clipboard != null && !node.isLeaf());
            
            copyItem.setEnabled(true);
            if (node != null)
            {
               // cannot copy an item that has not saved into database.
               PSAction actionNode = 
                (PSAction)((PSTreeObject) node.getUserObject()).getDataObject();
               if (actionNode != null && (! actionNode.isAssigned()))
                  copyItem.setEnabled(false);
            }
         }
      }
      else
      {
         newItem.setEnabled(false);
         deleteItem.setEnabled(false);
         copyItem.setEnabled(false);
         pasteItem.setEnabled(false);
      }
   }

   /**
    * Returns the current selected tree node
    * @return current selected tree node. May be <code>null</code>.
    */
   public DefaultMutableTreeNode getSelectedTreeNode()
   {

      TreePath currentSelection = m_tree.getSelectionPath();
      DefaultMutableTreeNode node = null;
      if(null != currentSelection)
         node =
            (DefaultMutableTreeNode) currentSelection.getLastPathComponent();

      return node;
   }

   /**
    * Implements the mouse adapter interface for bringing up the popup menu on
    * right click.
    */
   class PopupListener extends MouseAdapter
   {
      /**
       * MouseAdapter interface implementation.
       *
       * @param e, never <code>null</code>, supplied by Swing event handling
       * mechanism for mouse.
       */
      public void mousePressed(MouseEvent e)
      {
      }

      /**
       * MouseAdapter interface implementation. Calls {@link#maybeShowPopup(
       * MouseEvent)}, which brings up the popup menu.
       *
       * @param e, never <code>null</code>, supplied by Swing event handling
       * mechanism for mouse events.
       */
      public void mouseReleased(MouseEvent e)
      {
         maybeShowPopup(e);
      }

      /**
       * Shows the popup menu.
       *
       * @param e, mouse event may not be <code>null</code>.
       */
      private void maybeShowPopup(MouseEvent e)
      {
         if (e.isPopupTrigger())
            m_popup.show(e.getComponent(), e.getX(), e.getY());
      }
   }

   /**
    * Adds a new action node to the tree. The new node has a default
    * name 'New Node<n>', where n ranges from 1 to 2147483647. Adds user object
    * to the tree node. User object contains the node name and the editors
    * corresponding to the node.
    *
    * @param userObj May be <code>null</code>, if it is, a new action is
    *    created.
    */
   private void addNode(PSTreeObject userObj)
   {
      ActionsNode node = null;
      ActionsNode childNode = null;
      PSTreeObject childTreeObj = null;
      PSAction action = null;
      // a node is leaf if it has no children and if it has a parent other than
      // rootnode
      boolean isLeaf = false;
      if (userObj == null)
      {
         //means a new format has to be created
         String name = getNewActionName();
         action = new PSAction(name, name, "MENUITEM", "", "SERVER", 0);
         m_dbCollect.add(action);
         userObj = new PSTreeObject(name);
         userObj.setDataObject(action);
         node = new ActionsNode(userObj, true);
         ((DefaultTreeModel)m_tree.getModel()).insertNodeInto(node, m_rootNode,
               m_rootNode.getChildCount());
         TreePath path = new TreePath(node.getPath());
         m_tree.scrollPathToVisible(path);
         m_tree.setSelectionPath(path);
      }
      else
      {
         action =  (PSAction)userObj.getDataObject();
         if (action.isMenuItem())
            isLeaf = true;
         else
            isLeaf = false;
         node = new ActionsNode(userObj, isLeaf);
         m_rootNode.add(node);
         if (userObj.hasChildren())
         {
            List list = userObj.getChildren();
            Iterator itr = list.listIterator();
            while(itr.hasNext())
            {
               childTreeObj = (PSTreeObject)itr.next();
               action =  (PSAction)childTreeObj.getDataObject();
               childNode = new ActionsNode(childTreeObj, true);
               node.add(childNode);
            }
         }
      }
   }

   public JTree getTree()
   {
      return m_tree;
   }


   /**
    * Copies the currently selected action node to the local clipboard.
    */
   private void copy()
   {
      TreePath path = m_tree.getSelectionPath();
      if (path != null)
      {
         DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) path.getLastPathComponent();
         if (node != null)
            m_clipboard =
               (PSAction) ((PSTreeObject) node.getUserObject()).getDataObject();
      }
   }

   /**
    * Pastes the action currently in the local clipboard into the current
    * selection and resets the clopboard to <code>null</code>.
    */
   private void paste()
   {
      if (m_clipboard != null)
      {
         TreePath path = m_tree.getSelectionPath();
         if (path != null)
         {
            DefaultMutableTreeNode node =
               (DefaultMutableTreeNode) path.getLastPathComponent();

            if (node.isRoot())
            {
               String name = getNewActionName();

               PSAction clone = (PSAction) m_clipboard.clone();
               clone.setName(name);
               PSTreeObject userObj = new PSTreeObject(name);
               userObj.setDataObject(clone);

               ActionsNode newNode = new ActionsNode(userObj, true);
               ((DefaultTreeModel) m_tree.getModel()).insertNodeInto(newNode,
                  node, node.getChildCount());

               m_dbCollect.add(clone);

               path = new TreePath(newNode.getPath());
               m_tree.scrollPathToVisible(path);
               m_tree.setSelectionPath(path);
            }
            else
            {
               PSTreeObject userObj = new PSTreeObject(m_clipboard.getLabel());
               userObj.setDataObject(m_clipboard);

               PSTreeObject target = (PSTreeObject) node.getUserObject();
               target.addChildren(userObj);

               PSAction targetAction = (PSAction) target.getDataObject();
               targetAction.getChildren().add(m_clipboard);

               ActionsNode newNode = new ActionsNode(userObj, true);
               ((DefaultTreeModel) m_tree.getModel()).insertNodeInto(newNode,
                  node, node.getChildCount());
            }
         }

         m_clipboard = null;
      }
   }

   /**
    * Removes the selected node. Root node cannot be removed.  If the node
    * represents a menu child, it is only removed as a child of its parent.
    * If the node represents an action, the node is removed, the action is
    * deleted, and all child nodes representing the same action are removed
    * from their parent nodes.
    */
   private void remove()
   {
      TreePath currentSelection = m_tree.getSelectionPath();

      int i = m_tree.getRowForPath(currentSelection);

      m_tree.requestFocus();

      if (currentSelection != null)
      {
         DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
            (currentSelection.getLastPathComponent());

         // if action is not a child of the root, then it's just being removed
         // as a child of another action, otherwise, it's being deleted.
         if (isChildNode(currentNode))
         {
            // Prompt with the are you sure you want to remove message
            String message = ErrorDialogs.cropErrorMessage(MessageFormat.format(
               ms_res.getString("removeChildNode"),
               new Object[] {currentNode.toString(), currentNode.getParent().toString()}));

            int option = PSDlgUtil.showConfirmDialog(
               message,
               ms_res.getString("removeChildNodeTitle"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE);

            if(option == JOptionPane.NO_OPTION)
            {
               m_tree.setSelectionRow(i);
               return;
            }

            //about to delete, select a previous node
            if (i != 0)
                m_tree.setSelectionRow(i - 1);

            m_tree.requestFocus();
            // get the parent node, remove the node from the parent, and remove
            // the menu child action from the parent action
            removeFromParent(currentNode);

            return;
         }
         else
         {
            // Prompt with the are you sure you want to delete mesage
            String message = ErrorDialogs.cropErrorMessage(MessageFormat.format(
               ms_res.getString("deleteNode"),
               new Object[] {currentNode.toString()}));

            int option = PSDlgUtil.showConfirmDialog(
               message,
               ms_res.getString("deleteNodeTitle"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE);

            if(option == JOptionPane.NO_OPTION)
            {
               m_tree.setSelectionRow(i);
               return;
            }


            //about to delete, select a previous node
            if (i != 0)
                m_tree.setSelectionRow(i - 1);

            m_tree.requestFocus();

            PSAction action = (PSAction)(
               (PSTreeObject)currentNode.getUserObject()).getDataObject();

            // get all nodes with the same action as this node
            List<DefaultMutableTreeNode> matchList = new ArrayList<DefaultMutableTreeNode>();
            getMatchingNodes(m_rootNode, currentNode, matchList);

            // remove them from their parent nodes, and the corresonding menu
            // child from the parent action object
            Iterator matches = matchList.iterator();
            while (matches.hasNext())
            {
               DefaultMutableTreeNode match =
                  (DefaultMutableTreeNode)matches.next();
               removeFromParent(match);
            }

            // remove the selected node and delete its action component
            ((DefaultTreeModel)m_tree.getModel()).removeNodeFromParent(
               currentNode);
            m_dbCollect.remove(action);
         }
      }
   }

   /**
    * Get all nodes whose action data object is the same instance as that of the
    * supplied node.  Adds all such nodes to the supplied <code>matches</code>
    * list.  Recurses into all child nodes of the supplied <code>start</code>
    * node.  Does not include the supplied <code>node</code> in the results.
    *
    * @param start Recursively checks all child nodes of this node.  Does not
    * check the data object of this node (athough it may have done so on a
    * previous call while recursing).  Assumed not <code>null</code>.
    * @param node The node to match on, assumed not <code>null</code>.
    * @param matches The list to which the matching nodes are added, assumed not
    * <code>null</code>, may be empty.
    */
   private void getMatchingNodes(DefaultMutableTreeNode start,
      DefaultMutableTreeNode node, List<DefaultMutableTreeNode> matches)
   {
      PSAction action = (PSAction)(
         (PSTreeObject)node.getUserObject()).getDataObject();
      for (int i = 0; i < start.getChildCount(); i++)
      {
         DefaultMutableTreeNode test =
            (DefaultMutableTreeNode)start.getChildAt(i);

         // don't return the node we are matching on
         if (test == node)
            continue;
         Object obj = ((PSTreeObject)test.getUserObject()).getDataObject();
         if (action == obj)
            matches.add(test);
         getMatchingNodes(test, node, matches);
      }
   }

   /**
    * Removes the supplied node from its parent node, and removes the node's
    * action from its parent's child action list.
    *
    * @param node The node to remove, assumed not <code>null</code>.
    */
   private void removeFromParent(DefaultMutableTreeNode node)
   {
      if (isChildNode(node))
      {
         DefaultMutableTreeNode parent =
            (DefaultMutableTreeNode)(node.getParent());
         PSAction childAction = (PSAction)(
            (PSTreeObject)node.getUserObject()).getDataObject();
         PSAction parentAction = (PSAction)(
            (PSTreeObject)parent.getUserObject()).getDataObject();
         parentAction.getChildren().removeAction(childAction);
         ((DefaultTreeModel)m_tree.getModel()).removeNodeFromParent(
            node);
      }
   }

   /**
    * Determine if the supplied node is a child of a node other than the root
    * node.  This means that it represents a <code>PSMenuChild</code>, not a
    * <code>PSAction</code>.
    *
    * @param node The node to check, assumed not <code>null</code>.
    *
    * @return <code>true</code> if it is a child action node, <code>false</code>
    * otherwise.
    */
   private boolean isChildNode(DefaultMutableTreeNode node)
   {
      DefaultMutableTreeNode parent =
         (DefaultMutableTreeNode)(node.getParent());
      return (parent != null && !parent.isRoot());
   }

   /**
    * Gets a new action name, constructed from a default name with a number
    * appended on the end to ensure uniqueness.  Will use the lowest available
    * number that provides a unique name.
    *
    * @return The name, never <code>null</code> or empty.
    */
   private String getNewActionName()
   {
      String base =  ms_res.getString("default.nodename");
      base = base.replace(' ', '_');

      String actionName = base;
      boolean isUnique = false;
      while (!isUnique)
      {
         actionName = base + ++m_newNodeSuffix;
         isUnique = !nameExists(actionName);
      }

      return actionName;
   }

   /**
    * Determines if the specified internal action name has been used in the
    * collection.
    *
    * @param actionName The name to check, assumed not <code>null</code> or
    * empty.
    *
    * @return <code>true</code> if an action with that name exists in the
    * collection, <code>false</code> if not.
    */
   private boolean nameExists(String actionName)
   {
      boolean found = false;
      Iterator itr = m_dbCollect.iterator();
      while (itr.hasNext() && !found)
      {
         PSAction action = (PSAction)itr.next();
         if(actionName.equalsIgnoreCase(action.getName()))
            found = true;
      }
      return found;
   }

   /**
    * Listener responding to the selection of the tree node. Invokes {@link#
    * setPopupMenuBtnState()} on node selection.
    */
   public class FormatTreeListener implements TreeSelectionListener
   {

      /**
       * Interface implementation.
       *
       * @param e, never <code>null</code>, provided by Swing event handling
       * model.
       */
      public void valueChanged(TreeSelectionEvent e)
      {
         setPopupMenuBtnState();
         // new/delete button at bottom of tree
         setButtonState();
      }
   }

   /**
    * Manages the enabling/disabling of the buttons below the tree used for
    * adding/removing tree nodes (actions). Must be called whenever current
    * node is changed.
    */
   private void setButtonState()
   {
      TreePath currentSelection = m_tree.getSelectionPath();
      if (currentSelection != null)
      {
         DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) currentSelection.getLastPathComponent();
         if (node.isRoot())
         {
            m_deleteButton.getModel().setEnabled(false);
         }
         else
         {
            m_deleteButton.getModel().setEnabled(true);
         }
      }
   }

   /**
    * Adds a {@java.swing.event.TreeSelectionListener} to the tree.
    *
    * @param s, assumed to be not <code>null</code>
    */
   public void addTreeSelectionListener(TreeSelectionListener s)
   {
      m_tree.addTreeSelectionListener(s);
   }

   private void populateTreeList()
   {
      Iterator itr = m_dbCollect.iterator();
      PSAction action = null;
      PSAction childAction = null;
      PSChildActions childActions = null;
      PSMenuChild menuChild = null;
      PSTreeObject userObj = null;
      PSTreeObject childUserObj = null;
      m_leafList.clear();
      m_folderList.clear();
      while(itr.hasNext())
      {
         action = (PSAction) itr.next();
         userObj = new PSTreeObject(action.getLabel());
         if (action.isMenuItem())
         {
//            key = action.getLocator();
//            id = key.getPart(key.getDefinition()[0]);
//            userObj.setDataObject(m_idActionsMap.get(id));
            userObj.setDataObject(action);
            m_leafList.add(userObj);
         }
         else
         {
            userObj.setDataObject(action);
            childActions = action.getChildren();
            Iterator childItr = childActions.iterator();
            while (childItr.hasNext())
            {
               menuChild = (PSMenuChild)childItr.next();
               childAction = (PSAction)m_idActionsMap.get(
                     menuChild.getChildActionId());
               if (childAction != null)
               {
                  childUserObj = new PSTreeObject(childAction.getLabel());
                  childUserObj.setDataObject(childAction);
                  childUserObj.setParent(userObj);
                  userObj.addChildren(childUserObj);
               }
            }
            m_folderList.add(userObj);
         }
      }
   }

   /**
    * Adds child nodes to the tree.
    *
    * @param root, root node containing the child nodes, may not be <code>null
    * </code>
    */
   private void createTree()
   {
      Collections.sort(m_folderList);
      Collections.sort(m_leafList);
      int size = m_folderList.size();
      PSTreeObject usrObj = null;
      for (int k = 0; k < size; k++)
      {
         usrObj = (PSTreeObject)m_folderList.get(k);
         addNode(usrObj);
      }
      size = m_leafList.size();
      for (int k = 0; k < size; k++)
      {
         usrObj = (PSTreeObject)m_leafList.get(k);
         addNode(usrObj);
      }
   }

  /**
   * Refreshes the Action tree
   */
   public void refreshTree()
   {
     DefaultTreeModel model = ((DefaultTreeModel)m_tree.getModel());
     DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
     // Remove all child nodes
     root.removeAllChildren();
     // Re-populate the tree lists so they sort correctly
     populateTreeList();
     // Re-create the tree
     createTree();
     // Ask the model to reload the new tree
     model.reload();
   }

   /**
    * Gets the tree model.
    *
    * @return, tree model, never <code>null</code>.
    */
   public TreeModel getModel()
   {
      return m_tree.getModel();
   }

   //An empty implementation for the {@link java.awt.event.KeyListener}
   //interface
   public void keyTyped(KeyEvent e)
   {
   }

   /**
    * Implementation for the accelerator keys for popup menu items.
    *
    * @param e, never <code>null</code>, provided by Swing event handling
    * model.
    */
   public void keyPressed(KeyEvent e)
   {
      int mod = e.getModifiers();
      int code = e.getKeyCode();
      if ((mod == KeyEvent.CTRL_MASK) && (code == KeyEvent.VK_N))
      {
         addNode(null);
      }
      if (code == KeyEvent.VK_DELETE)
      {
         remove();
      }
      if ((mod == KeyEvent.CTRL_MASK) && (code == KeyEvent.VK_C))
      {
         copy();
      }
      if ((mod == KeyEvent.CTRL_MASK) && (code == KeyEvent.VK_V))
      {
         paste();
      }
      m_tree.requestFocus();
   }

   //An empty implementation for the {@link java.awt.event.KeyListener}
   //interface
   public void keyReleased(KeyEvent e)
   {
   }


   private class ActionsNode extends DefaultMutableTreeNode
   {
      public ActionsNode(Object userObj, boolean isLeaf)
      {
         super(userObj);
         m_isLeaf = isLeaf;
      }

      public boolean isLeaf()
      {
         return m_isLeaf;
      }

      private boolean m_isLeaf;
   }

   private Map<String, PSAction> m_idActionsMap = new HashMap<String, PSAction>();
   private DefaultMutableTreeNode m_rootNode;
   private List<PSTreeObject> m_folderList = new ArrayList<PSTreeObject>();
   private List<PSTreeObject> m_leafList = new ArrayList<PSTreeObject>();

   /**
    *
    */
   private PSDbComponentCollection m_dbCollect;

   /**
    * Tree showing display formats, Initialized in {@link#init()}, never <code>
    * null</code> or modified after that.
    */
   private JTree m_tree;

   /**
    * Each new node added has a default name 'New Node<n>', where n is
    * represented by this variable, incremented every time a node is added in
    * {@link#add()}.
    */
   private int m_newNodeSuffix = 0;

   /**
    * Pop up menu containing 'New', 'Copy', 'Paste' and 'Delete' as the menu
    * items, initialized in {@link#addPopupMenu()}, never <code>null</code> or
    * modified after that.
    */
   private JPopupMenu m_popup;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;

   /**
    * The local clipboard used for copy paste operations. Initialized in each
    * call to {@link copy()}, consumed in each call to {@link paste()}.
    */
   private PSAction m_clipboard = null;

   /**
    * The button used to remove actions from the tree. Never <code>null</code>.
    * We keep a reference so we can change its enabled state depending on
    * which node is current.
    */
   private JButton m_deleteButton =
         new UTFixedButton(ms_res.getString("menu.delete"));

   //test code
   public static void main(String[] arg)
   {
      JFrame f = new JFrame("BoxLayoutDemo");
//      Container contentPane = f.getContentPane();
    //  TreePanel ac = new TreePanel();
     // contentPane.add(ac, BorderLayout.CENTER);
      f.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      f.pack();
      f.setVisible(true);
   }

   /**
    * The image icon used for all leaf nodes.
    */
   private static ImageIcon ms_leafIcon = null;
   static
   {
      ms_res = ResourceBundle.getBundle(PSActionTreePanel.class.getName()
            + "Resources", Locale.getDefault());

      ms_leafIcon = new ImageIcon(PSActionTreePanel.class.getResource(
         ms_res.getString("icon.leaf")));
   }
}

/*******************************************************************************
 *
 * [ TreePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
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
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A specific panel holding a tree for all defined Display Formats.
 */
public class TreePanel extends JPanel implements KeyListener, ItemListener
{
   /**
    * Consructs the panel.
    */
   public TreePanel(PSDbComponentCollection dbCollect)
   {
      m_dbCollect = dbCollect;
      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());

      if(null == ms_treeLeafIcon)
      {
         ms_treeLeafIcon =  new ImageIcon(
            getClass().getResource(ms_res.getString("icon.tree.leaf")));
      }

      setLayout(new BorderLayout());
      Border b = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      setBorder(b);
      JPanel treePane = new JPanel();
      treePane.setLayout(new BorderLayout());
      b = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder()
         , b);
      treePane.setBorder(b);
      PSTreeObject rootUserObj = new PSTreeObject(ms_res.getString("root.name"),
         new ParentPanel());
      // read the default path.
      DefaultMutableTreeNode top = new
         DefaultMutableTreeNode(rootUserObj);

      m_tree = new JTree(top);
      createTree();
      m_tree.addTreeSelectionListener(new FormatTreeListener());
      m_tree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.SINGLE_TREE_SELECTION);
      m_tree.setEditable(false);
      DefaultTreeCellRenderer renderer =
         (DefaultTreeCellRenderer)m_tree.getCellRenderer();
      renderer.setLeafIcon(ms_treeLeafIcon);
      JScrollPane scrollPane = new JScrollPane(m_tree);
      treePane.add(scrollPane, BorderLayout.CENTER);

      m_tree.addKeyListener(this);
      JPanel btnPane = new JPanel();
      btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));
      m_new = new UTFixedButton(ms_res.getString("btn.label.new"));
      m_new.setMnemonic(ms_res.getString("btn.label.new.mn").charAt(0));
      m_new.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            addNode(null);
            m_tree.setSelectionRow(m_tree.getRowCount() -1);
            m_tree.requestFocus();
         }
      });
      m_remove = new UTFixedButton(ms_res.getString("btn.label.delete"));
      m_remove.setMnemonic(ms_res.getString("btn.label.delete.mn").charAt(0));
      m_remove.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            remove();
         }
      });
      btnPane.add(Box.createHorizontalGlue());
      btnPane.add(m_new);
      btnPane.add(Box.createHorizontalStrut(20));
      btnPane.add(m_remove);
      btnPane.add(Box.createHorizontalGlue());

      btnPane.setBorder(BorderFactory.createEmptyBorder(10, 0,0,0));

      treePane.add(btnPane, BorderLayout.SOUTH);
      add(treePane, BorderLayout.CENTER);

      //Create the popup menu.
      addPopupMenu();

      // set selected row:
      if(m_tree.getRowCount() > 0)
         m_tree.setSelectionRow(1);

      m_tree.setRequestFocusEnabled(true);
   }

   /**
    * Adds popup menu to the panel. Menu gets displayed on right click of the
    * mouse in the tree area.
    */
   public void addPopupMenu()
   {
      m_popup = new JPopupMenu();
      JMenuItem menuItem = new JMenuItem(ms_res.getString("btn.label.new"));
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

      /**
       * @todo implement when time.
       *
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
*/
      menuItem = new JMenuItem(ms_res.getString("btn.label.delete"));
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
    * Gets the PSDisplayFormat associated with the currently selected node, if
    * there is one.
    *
    * @return May be <code>null</code> if the current node doesn't contain
    *    one or there is no selection.
    */
   public PSDisplayFormat getSelectedObject()
   {
      if (m_tree == null)
         return null;

      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if (node == null)
         return null;

      if (node.getUserObject() instanceof PSDisplayFormat)
      {
         return (PSDisplayFormat) node.getUserObject();
      }

      return null;
   }

   /**
    * Returns the current selected tree node
    * @return current selected tree node. May be <code>null</code>.
    */
   public DefaultMutableTreeNode getSelectedNode()
   {

      TreePath currentSelection = m_tree.getSelectionPath();
      DefaultMutableTreeNode node = null;
      if(null != currentSelection)
         node =
            (DefaultMutableTreeNode) currentSelection.getLastPathComponent();

      return node;
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
//      JMenuItem copyItem = (JMenuItem)menuArr[1].getComponent();
//      JMenuItem pasteItem = (JMenuItem)menuArr[2].getComponent();
      JMenuItem deleteItem = (JMenuItem)menuArr[1].getComponent();
      if (currentSelection != null)
      {
         Object node = currentSelection.getLastPathComponent();
         Object root = m_tree.getModel().getRoot();
         if (node == root)
         {
            m_new.setEnabled(true);
            m_remove.setEnabled(false);
            newItem.setEnabled(true);
            deleteItem.setEnabled(false);
        //    copyItem.setEnabled(false);
        //    pasteItem.setEnabled(false);
         }
         else
         {
            m_remove.setEnabled(true);
            newItem.setEnabled(false);
            deleteItem.setEnabled(true);
        //    copyItem.setEnabled(true);
        //    pasteItem.setEnabled(true);
         }
      }
      else
      {
         m_remove.setEnabled(false);
         newItem.setEnabled(false);
         deleteItem.setEnabled(false);
      //   copyItem.setEnabled(false);
       //  pasteItem.setEnabled(false);
      }
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
    * Adds a new Display Format node to the tree. The new node has a default
    * name 'New Node<n>', where n ranges from 1 to 2147483647. Adds user object
    * to the tree node. User object contains the node name and the editors
    * corresponding to the node.
    */
   private void addNode(PSDisplayFormat df)
   {
      String name = null;
      DefaultMutableTreeNode node = null;
      PSDisplayFormat dispFor = df;
      try
      {
         if (dispFor == null)
         {
            //means a new format has to be created
            name =  ms_res.getString("default.nodename") + ++m_newNodeSuffix;
            dispFor = new PSDisplayFormat();
            dispFor.setDisplayName(name);
            m_dbCollect.add(dispFor);

         }
         else
            name = df.getDisplayName();

         node = createChild(name);
         PSTreeObject treeObj = new PSTreeObject(name);
         treeObj.setDataObject(dispFor);
         node.setUserObject(treeObj);

      }
      catch(PSCmsException e)
      {
      }
   }

   /**
    * @todo copy operation
    *
    * Copies a given Display format node except the root.
    *
    */
   private void copy()
   {

   }

   /**
    * @todo paste operation
    *
    * Pastes copied Display format node.
    *
    */
   private void paste()
   {

   }

   /**
    * Removes the selected node. Root node cannot be removed.
    */
   private void remove()
   {
      TreePath currentSelection = m_tree.getSelectionPath();
      int i = m_tree.getRowForPath(currentSelection);

      if (i != 0)
         m_tree.setSelectionRow(i - 1);
      m_tree.requestFocus();
      if (currentSelection != null)
      {
         DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
            (currentSelection.getLastPathComponent());
          // Prompt with the are you sure you want to delete mesage
          int option = PSDlgUtil.showConfirmDialog(
               MessageFormat.format(
                     ms_res.getString("deleteNode"),new Object[] {currentNode.toString()} ),
                     ms_res.getString("deleteNodeTitle"),
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE);
          if(option == JOptionPane.NO_OPTION)
          {
            m_tree.setSelectionRow(i);
            return;
          }

         PSDisplayFormat dispObj =
            (PSDisplayFormat)(
            (PSTreeObject)currentNode.getUserObject()).getDataObject();
         MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
         if (parent != null)
         {
            ((DefaultTreeModel)m_tree.getModel()).removeNodeFromParent(
               currentNode);
            m_dbCollect.remove(dispObj);
            return;
         }
      }
   }

   /**
    * This method refreshes the selected node.  It resets the data for the
    * current node.
    */
   void refreshSelectedNode()
   {
      TreePath selPath = m_tree.getSelectionPath();
      if(selPath == null || selPath.getParentPath() == null)
         return;

      DefaultMutableTreeNode dmt = (DefaultMutableTreeNode)
         selPath.getLastPathComponent();

      PSTreeObject usr = (PSTreeObject)dmt.getUserObject();
      PSDisplayFormat dformat = (PSDisplayFormat)usr.getDataObject();

      if(dformat != null)
         usr.setName(dformat.getDisplayName());

      ((DefaultTreeModel)getModel()).reload(dmt);
   }

   /**
    * Adds a child node to the 'Display Format' node.
    *
    * @param child, name of the child node, may not be <code>null</code> or
    * empty.
    *
    * @return the added child node, never <code>null</code>
    */
   private DefaultMutableTreeNode createChild(String child)
   {
      if (child == null || child.length() == 0)
         throw new IllegalArgumentException("Child node name cannot be null");
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
         m_tree.getModel().getRoot();
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
      if (parent == null)
         parent = (DefaultMutableTreeNode)m_tree.getModel().getRoot();
      ((DefaultTreeModel)m_tree.getModel()).insertNodeInto(childNode, parent,
         parent.getChildCount());
      m_tree.scrollPathToVisible(new TreePath(childNode.getPath()));
      return childNode;
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

   /**
    * Adds child nodes to the tree.
    *
    * @param root, root node containing the child nodes, may not be <code>null
    * </code>
    */
   private void createTree()
   {
      Iterator itr = m_dbCollect.iterator();
      while(itr.hasNext())
         addNode((PSDisplayFormat)itr.next());
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

   /**
    *
    */
   private PSDbComponentCollection m_dbCollect;

   /**
    * 'New' button to add display format child nodes. Initialized in {@link#init
    * ()}, never <code>null</code> or modified after that.
    */
   private JButton m_new;

   /**
    * 'Delete' button to remove display format child nodes. Initialized in
    * {@link#init()}, never <code>null</code> or modified after that.
    */
   private JButton m_remove;

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
   private int m_newNodeSuffix;

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
    * The image icon used for the tree leaf. Initialized in {@link init()}.
    * Never <code>null</code> after that.
    */
   private static ImageIcon ms_treeLeafIcon;
}

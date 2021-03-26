/******************************************************************************
 *
 * [ SearchTreePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchCollection;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Panel holding tree for all defined search cms layer objects.
 */
public class SearchTreePanel extends JPanel
   implements IPSDbComponentUpdater, ActionListener
{
   /**
    * Consructs the panel.
    *
    * @param dlg The parent dialog. Never <code>null</code>.
    */
   public SearchTreePanel(SearchViewDialog dlg)
   {
      if (dlg == null)
         throw new IllegalArgumentException(
            "parent dialog must not be null");

      m_parentFrame = dlg;

      init();

      // Add the specified tree selection listener
      m_tree.addTreeSelectionListener(dlg);
   }

   /**
    * Does nothing.
    */
   public void onDataPersisted()
   {}

   /**
    * Initializes the panel.
    */
   private void init()
   {

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

      // init tree with empty list
      setupTreeModel(new PSDbComponentCollection(PSSearch.class));

      m_model = new DefaultTreeModel(m_rootNode);
      m_tree = new JTree(m_model);

      m_tree.setRootVisible(false);
      m_tree.setShowsRootHandles(true);

      m_tree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.SINGLE_TREE_SELECTION);
      m_tree.setEditable(false);
      m_tree.setCellRenderer(new SearchTreeRenderer());
      SearchTreeRenderer renderer =
         (SearchTreeRenderer)m_tree.getCellRenderer();
      renderer.setLeafIcon(ms_treeLeafIcon);
      m_tree.addTreeSelectionListener(new TreeSelectionListener()
      {
         public void valueChanged(TreeSelectionEvent evt)
         {
            //manage buttons below the tree
            setButtonState();
         }
      });

      MouseListener pop = new SearchContextMenu();
      m_tree.addMouseListener(pop);

      JScrollPane scrollPane = new JScrollPane(m_tree);
      treePane.add(scrollPane, BorderLayout.CENTER);

      JPanel btnPane = new JPanel();
      btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));

      m_newButton.addActionListener(m_parentFrame);
      m_newButton.setMnemonic(ms_res.getString("btn.label.new.mn").charAt(0));
      m_deleteButton.addActionListener(m_parentFrame);
      m_deleteButton.setMnemonic(
                            ms_res.getString("btn.label.delete.mn").charAt(0));
      btnPane.add(Box.createHorizontalGlue());
      btnPane.add(m_newButton);
      btnPane.add(Box.createHorizontalStrut(20));
      btnPane.add(m_deleteButton);
      btnPane.add(Box.createHorizontalGlue());

      btnPane.setBorder(BorderFactory.createEmptyBorder(10, 0,0,0));

      add(treePane, BorderLayout.CENTER);
      add(btnPane, BorderLayout.SOUTH);
      m_tree.setSelectionRow(0);
      m_tree.setRequestFocusEnabled(true);
   }

   /**
    * Get the root node.
    *
    * @return root node, never <code>null</code>.
    *
    */
   public DefaultMutableTreeNode getRoot()
   {
      return m_rootNode;
   }

   /**
    * Calls {@link #refreshNode(TreeNode)} passing the currently selected node,
    * if a node is not selected nothing will occur.
    */
   public void refreshSelectedNode()
   {
      TreeNode selNode = (DefaultMutableTreeNode)
         m_tree.getSelectionPath().getLastPathComponent();

      if(selNode != null)
         refreshNode(selNode);
   }

   /**
    * Convenience method that calls the
    * {@link DefaultTreeModel#nodeChanged(TreeNode)
    * DefaultTreeModel.nodeChanged(node)} with the supplied
    * <code>TreeNode</code>
    *
    * @param node the node to be changed, if <code>null</code> nothing will
    * occur.
    */
   public void refreshNode(TreeNode node)
   {
      if(node != null)
         ((DefaultTreeModel)m_tree.getModel()).nodeChanged(node);
   }

   /**
    * Given a user object, find it in the tree, and select
    * it.
    *
    * @param userObj The user object, never <code>null</code>
    */
   public void setSelectedObject(Object userObj)
   {
      if (userObj == null)
         throw new IllegalArgumentException(
            "userObj must not be null");

      selectTreeNodeWithUserObject(
         (DefaultMutableTreeNode) m_model.getRoot(), userObj);
   }

   /**
    * Manages the enabling/disabling of the buttons below the tree used for
    * adding/removing tree nodes (searches). Must be called whenever current
    * node is changed.
    */
   private void setButtonState()
   {
      TreePath currentSelection = m_tree.getSelectionPath();
      if (currentSelection != null)
      {
         DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) currentSelection.getLastPathComponent();
         if (node == m_rootNode ||
             node == m_standardViewsNode||
             node == m_customViewsNode ||
             node == m_standardSearchesNode ||
             node == m_customSearchesNode)
         {
            m_deleteButton.getModel().setEnabled(false);
         }
         else
         {
            m_deleteButton.getModel().setEnabled(true);
         }
         m_newButton.getModel().setEnabled(node != m_rootNode);
      }
   }

   /**
    * Recursively checks the tree for this search object.
    *
    * @param n TreeNode to start searching from. Never <code>null</code>
    *
    * @param userObj Used to match on the node's user object. Never 
    * <code>null</code>
    *
    */
   private void selectTreeNodeWithUserObject(
      DefaultMutableTreeNode n,
      Object userObj)
   {
      if (n == null || userObj == null)
         throw new IllegalArgumentException(
            "node and userObj must not be null");

      Object obj = n.getUserObject();

      if (obj != null)
      {
         // Break condition
         if (obj.equals(userObj))
         {
            m_tree.setSelectionPath(new TreePath(n.getPath()));
            return;
         }

         // Check all the children
         Enumeration children = n.children();

         while (children.hasMoreElements())
         {
            selectTreeNodeWithUserObject(
               (DefaultMutableTreeNode)children.nextElement(), userObj);
         }
      }
   }

   // see interface for description
   public void actionPerformed(ActionEvent e)
   {
      String strCmd = e.getActionCommand();

      try
      {
         Method m = getClass().getDeclaredMethod("on" + strCmd, (Class[]) null);
         m.invoke(this, (Object[]) null);
      }
      catch (Exception ignoreAndContinue)
      {}
   }

   /**
    * Gets the currently selected search object. If
    * a search object is not selected, <code>null</code>
    * is returned.
    *
    * @return search object, may be <code>null</code> if
    *    a node search node is selected.
    */
   public PSSearch getSelectedObject()
   {
      if (m_tree == null)
         return null;

      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if (node == null)
         return null;

      if (node.getUserObject() instanceof PSSearch)
      {
         return (PSSearch) node.getUserObject();
      }

      return null;
   }

   // see interface for description
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      return true;
   }

   // see interface for description
   public boolean onUpdateData(
      IPSDbComponent comp, boolean bDirection, boolean isQuiet)
   {
      // Threshold
      if (comp == null)
         throw new IllegalArgumentException(
            "comp must not be null");

      // Threshold - we're expecting a PSSearch cms object collection
      if (!(comp instanceof PSSearchCollection))
         return true; // we're just not interested but no error has happened

      PSDbComponentCollection searches = (PSDbComponentCollection) comp;

      if (bDirection)
      {
         // 'view' to object direction
         // Currently we do nothing here ...
      }
      else
      {
         // update the view from the object
         setupTreeModel(searches);
      }

      return true;
   }

   /**
    * @return <code>true</code> if the selected node is the
    * standard view node or if the selected node's parent is standard view node,
    * otherwise <code>false</code>.
    */
   public boolean isStandardViews()
   {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if (node == null)
         return false;

      if (node == m_standardViewsNode)
         return true;

      if (node.getUserObject() instanceof PSSearch)
      {
         return (((PSSearch) node.getUserObject()).isStandardView());
      }

      return false;
   }

   /**
    * @return <code>true</code> if the selected node is the
    * custom view node or if the selected node's parent is the custom view
    * node, otherwise, <code>false</code>.
    */
   public boolean isCustomViews()
   {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if (node == null)
         return false;

      if (node == m_customViewsNode)
         return true;

      if (node.getUserObject() instanceof PSSearch)
      {
         return (((PSSearch) node.getUserObject()).isCustomView());
      }

      return false;
   }

   /**
    * @return <code>true</code> if the selected node is the
    * standard searches node or if the selected node's parent is the standard
    * searches  node, otherwise, <code>false</code>.
    */
   public boolean isStandardSearches()
   {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if (node == null)
         return false;

      if (node == m_standardSearchesNode)
         return true;

      if (node.getUserObject() instanceof PSSearch)
      {
         return (((PSSearch) node.getUserObject()).isStandardSearch());
      }

      return false;
   }

   /**
    * @return <code>true</code> if the selected node is the
    * custom searche node or if the selected node's parent is the custom
    * searche node, otherwise, <code>false</code>.
    */
   public boolean isCustomSearches()
   {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if (node == null)
         return false;

      if (node == m_customSearchesNode)
         return true;

      if (node.getUserObject() instanceof PSSearch)
      {
         return (((PSSearch) node.getUserObject()).isCustomSearch());
      }

      return false;
   }

   /**
    * Loads the tree from a component collection of searches.  Searches marked
    * for delete are not added to the tree.  If FTS is not enabled on the 
    * server, searches for which {@link PSSearch#useExternalSearch()} returns 
    * <code>true</code> will be disabled as there is no safe way to allow the 
    * user to edit them, and a message is displayed to notify the user the first 
    * time such a search is added to the tree. 
    *
    * @param searches Collection of searches. Never <code>null</code>.
    */
   public void setupTreeModel(PSDbComponentCollection searches)
   {

      // Threshold
      if (searches == null)
         throw new IllegalArgumentException(
            "searches must not be null");


      // Save information about expanded rows
      List<Integer> expandedRows = new ArrayList<Integer>();
      Object selection = null;

      if(null != m_tree)
      {
         selection = getSelectedObject();
         for(int i = 0; i<m_tree.getRowCount(); i++)
         {
            if(m_tree.isExpanded(i))
               expandedRows.add(new Integer(i));
         }
      }

      // threshold - first time setting up the tree
      if (searches.size() < 1 && m_tree == null)
      {
         // setup the static tree nodes
         m_rootNode =
               new DefaultMutableTreeNode(ms_res.getString("tree.root.label"));
         m_rootViewNode =
               new DefaultMutableTreeNode(ms_res.getString("tree.view.label"));
         m_rootSearchNode =
               new DefaultMutableTreeNode(ms_res.getString("tree.search.label"));
         m_rootNode.add(m_rootViewNode);
         m_rootNode.add(m_rootSearchNode);
      }

      // Clear out existing nodes
      if (m_standardViewsNode != null)
         m_standardViewsNode.removeAllChildren();
      else
      {
         m_standardViewsNode =
               new DefaultMutableTreeNode(ms_res.getString(
                  "tree.node.standardviews"));
         // make it a folder even if it is empty
         m_standardViewsNode.setAllowsChildren(true);         
      }

      if (m_customViewsNode != null)
         m_customViewsNode.removeAllChildren();
      else
      {
         m_customViewsNode =
               new DefaultMutableTreeNode(ms_res.getString(
                  "tree.node.customviews"));
         m_customViewsNode.setAllowsChildren(true);
      }

      if (m_standardSearchesNode != null)
         m_standardSearchesNode.removeAllChildren();
      else
      {
         m_standardSearchesNode =
               new DefaultMutableTreeNode(ms_res.getString(
                  "tree.node.standardsearches"));
         m_standardSearchesNode.setAllowsChildren(true);
      }

      if (m_customSearchesNode != null)
         m_customSearchesNode.removeAllChildren();
      else
      {
         m_customSearchesNode =
               new DefaultMutableTreeNode(ms_res.getString(
                  "tree.node.customsearches"));
         m_customSearchesNode.setAllowsChildren(true);
      }

      // Add the 'static' nodes to the tree
      m_rootViewNode.add(m_customViewsNode);
      m_rootViewNode.add(m_standardViewsNode);
      m_rootSearchNode.add(m_customSearchesNode);
      m_rootSearchNode.add(m_standardSearchesNode);

      Iterator iter = sortCollection(searches);
      DefaultMutableTreeNode node = null;
      while (iter.hasNext())
      {
         PSSearch s = (PSSearch)iter.next();
         
         // skip if deleted
         if (s.getState() == IPSDbComponent.DBSTATE_MARKEDFORDELETE)
            continue;
         
         // warn user if any searches will be disabled
         if (!FeatureSet.isFTSearchEnabled() && s.useExternalSearch())
         {
            // warn user only once, as this only occurs on initial load
            if (!m_warnedFTSDisable)
            {
               PSDlgUtil.showErrorDialog( 
                  ms_res.getString("search.disabled.msg"), 
                  ms_res.getString("search.disabled.title"));
               m_warnedFTSDisable = true;
            }
         }
         
         node = new DefaultMutableTreeNode(s);
         node.setAllowsChildren(false);         

         if (s.isCustomView())
         {
            m_customViewsNode.add(node);
         }
         else if(s.isStandardView())
         {
            m_standardViewsNode.add(node);
         }
         else if (s.isCustomSearch())
         {
            m_customSearchesNode.add(node);
         }
         else if (s.isStandardSearch())
         {
            m_standardSearchesNode.add(node);
         }
      }
      
      m_model = new DefaultTreeModel(m_rootNode);


      if (m_tree != null)
      {
         m_tree.setModel(m_model);
         // Make sure expanded rows get expanded
         Iterator it = expandedRows.iterator();
         while(it.hasNext())
            m_tree.expandRow(((Integer)it.next()).intValue());
         if(null != m_rootNode && null != selection)
            selectTreeNodeWithUserObject(m_rootNode, selection);
      }

   }

   /**
    * Sorts the collection by display name.
    *
    * @param searches the collection on which sort will occur.  Assumed not
    * <code>null</code>.
    *
    * @return an <code>Iterator</code> of sorted <code>PSSearch</code> objects.
    * Never <code>null</code>, may be empty.
    *
    * @todo This is not called at this time, but will be called when there is
    * time to address refreshing the tree.
    */
   private Iterator sortCollection(PSDbComponentCollection searches)
   {
      Iterator iter = searches.iterator();

      List<Object> sortedList = new ArrayList<Object>();
      while (iter.hasNext())
         sortedList.add(iter.next());

      Collections.sort(sortedList, new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            PSSearch sObj1 = (PSSearch)o1;
            PSSearch sObj2 = (PSSearch)o2;

            return sObj1.getDisplayName().compareTo(sObj2.getDisplayName());
         }
      });
      return sortedList.iterator();
   }

   /**
    * Inner class renderer for the search tree.
    */
   class SearchTreeRenderer extends DefaultTreeCellRenderer
   {
      /**
       * see base class for description
       */
      public Component getTreeCellRendererComponent(
         JTree tree,
         Object value,
         boolean sel,
         boolean expanded,
         boolean leaf,
         int row,
         @SuppressWarnings("hiding") boolean hasFocus)
      {

         super.getTreeCellRendererComponent(
            tree, value, sel,
            expanded, leaf, row,
            hasFocus);

         if (value instanceof DefaultMutableTreeNode)
         {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;

            if (n == null)
               return this;

            Object nodeInfo = n.getUserObject();

            if (nodeInfo instanceof IPSDbComponent)
            {
               IPSDbComponent psn = (IPSDbComponent) nodeInfo;

               try
               {
                  PSSearch search = (PSSearch) psn;
                  setText(search.getDisplayName());
                  
                  // show external searches as disabled if FTS engine is not
                  // available
                  boolean enabled;
                  if (!FeatureSet.isFTSearchEnabled() && 
                     search.useExternalSearch())
                  {
                     enabled = false;
                  }
                  else
                  {
                     enabled = true;
                  }
                  
                  setEnabled(enabled);
                  // disabled icon looks like crap
                  setDisabledIcon(getLeafIcon());
               }
               catch (Exception ignore)
               {}
            }
            
            // force folder icon if can have children, even if no children            
            if (leaf && n.getAllowsChildren())
            {
               if (tree.isEnabled())
               {
                  if (expanded)
                     setIcon(getOpenIcon());
                  else
                     setIcon(getClosedIcon());
               }
               else
               {
                  if (expanded)
                     setDisabledIcon(getOpenIcon());
                  else
                     setDisabledIcon(getClosedIcon());
               }  
            }
         }
         
         return this;
      }
   }

   /**
    * Popup menu for tree.
    */
   class SearchContextMenu extends MouseAdapter
   {
      public void mousePressed(MouseEvent e)
      {
         showPopup(e);
      }

      public void mouseReleased(MouseEvent e)
      {
         showPopup(e);
      }

      private void showPopup(MouseEvent e)
      {
         if (e.isPopupTrigger())
         {
            int nRow = m_tree.getRowForLocation(e.getX(), e.getY());
            m_tree.setSelectionRow(nRow);

            // Initialize the menu based on the node
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
               m_tree.getLastSelectedPathComponent();

            // threshold
            if (node == null)
               return;

            // threshold
            if (node == m_rootNode)
               return;

            setupPopupMenu(node);
            m_popupMenu.show(e.getComponent(),
               e.getX(), e.getY());
         }
      }
   }

   /**
    * Customize the popup content tree menu based on the
    * node selected.
    *
    * @param node The node used to generate the context menu actions. May be 
    * <code>null</code> in which case the method simply returns.
    */
   private void setupPopupMenu(DefaultMutableTreeNode node)
   {
      // Threshold
      if (node == null)
         return;

      m_popupMenu.removeAll();

      // Add the new item entry
      JMenuItem newItem = new JMenuItem("New");
      newItem.addActionListener(m_parentFrame);
      m_popupMenu.add(newItem);

      // Add the delete item entry
      JMenuItem removeItem = new JMenuItem("Delete");
      removeItem.addActionListener(m_parentFrame);
      m_popupMenu.add(removeItem);

      if (node != m_standardViewsNode && node != m_customViewsNode)
      {
         newItem.setEnabled(false);
      }
      else
      {
         removeItem.setEnabled(false);
      }
   }

   /**
    * Tree showing search objects, Initialized in {@link #init()}, never <code>
    * null</code> or modified after that.
    */
   private JTree m_tree;

   /**
    * Tree model, Initialized in {@link #init()}, never <code>
    * null</code> modified in {@link #onUpdateData}.
    */
   private DefaultTreeModel m_model;

   /**
    * Resource bundle for this class. Initialized in a static initializer.
    * Never <code>null</code> or modified after that. 
    */
   private static ResourceBundle ms_res;

   /**
    * The image icon used for the tree leaf. Initialized in {@link #init()}.
    * Never <code>null</code> after that.
    */
   private static ImageIcon ms_treeLeafIcon;

   /**
    * Tree root node. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>.
    */
   private DefaultMutableTreeNode m_rootNode;

   /**
    * Tree root node for Views. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>.
    */
   private DefaultMutableTreeNode m_rootViewNode;

   /**
    * Tree root node for Searches. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>.
    */
   private DefaultMutableTreeNode m_rootSearchNode;

   /**
    * Standard Views node. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>.
    */
   private DefaultMutableTreeNode m_standardViewsNode;

   /**
    * Custom Views node. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>.
    */
   private DefaultMutableTreeNode m_customViewsNode;

   /**
    * System Searches node. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>.
    */
   private DefaultMutableTreeNode m_standardSearchesNode;

   /**
    * Custom searches node. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>.
    */
   private DefaultMutableTreeNode m_customSearchesNode;

   /**
    * Context menu for new, delete actions. Initialized in definition.
    * Never <code>null</code>.
    */
   private JPopupMenu m_popupMenu = new JPopupMenu();

   /**
    * Parent frame to notify for new and delete events.
    * Never <code>null</code> passed in ctor.
    */
   private SearchViewDialog m_parentFrame = null;


   /**
    * The button used to remove searches from the tree. Never <code>null</code>.
    * We keep a reference so we can change its enabled state depending on
    * which node is current. Should be enabled for all leafs.
    */
   private JButton m_deleteButton =
         new UTFixedButton(ms_res.getString("btn.label.delete"));

   /**
    * Flag to indicate if we've warned the user about external search disabling.
    * Initially <code>false</code>, set to <code>true</code> the first time the
    * user is warned.  See {@link #setupTreeModel(PSDbComponentCollection)} for 
    * more info.
    */
   private boolean m_warnedFTSDisable = false;
   
   /**
    * The button used to add searches to the tree. Never <code>null</code>.
    * We keep a reference so we can change its enabled state depending on
    * which node is current. It should be enabled for all nodes except the
    * root.
    */
   private JButton m_newButton =
         new UTFixedButton(ms_res.getString("btn.label.new"));

   static
   {
      ms_res = ResourceBundle.getBundle(SearchTreePanel.class.getName()
            + "Resources", Locale.getDefault());
   }
}

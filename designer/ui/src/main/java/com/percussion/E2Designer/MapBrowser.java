/******************************************************************************
 *
 * [ MapBrowser.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.ImageListControl.ImageListControl;
import com.percussion.ImageListControl.ImageListItem;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.util.PSCollection;
import com.percussion.util.PSStringComparator;
import com.percussion.xml.PSDtdAttribute;
import com.percussion.xml.PSDtdDataElement;
import com.percussion.xml.PSDtdElement;
import com.percussion.xml.PSDtdElementEntry;
import com.percussion.xml.PSDtdNodeList;
import com.percussion.xml.PSDtdTree;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;


/**
 * The mapper browser panel.
 */
public class MapBrowser extends JPanel implements ListSelectionListener,
   ActionListener
{
   /**
    * Construct the mapper browser panel. Use this constuctor if no selection
    * control is necessary.
    */
   public MapBrowser()
   {
      initPanel();
   }

   /**
    * Construct the mapper browser panel. Use this constructor to create a
    * selection control as well.
    *
    * @param selections  a vector of selectable objects
    * @param udfSet the valid UDFs
    * @param bIsQuery map browser for query (true) or update (false) pipe
    */
   public MapBrowser(Vector selections, PSUdfSet udfSet, Boolean bIsQuery)
   {
      m_selectionItems = selections;
      m_udfSet = udfSet;
      initPanel();
      m_bIsQuery = bIsQuery;
   }

   /**
    * Initialize the panel.
    */
   public void initPanel()
   {
      // initialize the panel itself
      setLayout(new BorderLayout());

      // create the context menus for Udf tree;
      // adds "add/modify", "delete" and "refresh"
      m_menuItemAddmod.addActionListener(this);
      m_menuItemDelete.addActionListener(this);
      m_menuItemRefresh.addActionListener(this);

      // initialize tree
      DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
      m_treeModel = new DefaultTreeModel(root);
      m_tree = new MapBrowserTree(m_treeModel);
      ToolTipManager.sharedInstance().registerComponent(m_tree);
      m_tree.getSelectionModel().setSelectionMode(
         DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
      m_tree.addMouseListener(new UdfTreeNodeSelector());
      JScrollPane pane = new JScrollPane(m_tree);
      pane.setPreferredSize(new Dimension(100, 300));

      // create the selection view
      if (m_selectionItems != null)
      {
         m_selectionView = new ImageListControl(m_selectionItems);
         m_selectionView.setPreferredSize(new Dimension(150, 120));
         JList list = m_selectionView.getListData();
         list.addListSelectionListener(this);
         list.setSelectedIndex(0);
         
         // valueChanged will take care of initializing the tree
         add(m_selectionView, "North");
      }
      add(pane, "Center");
   }

   /**
    * Implementation of ActionListener.
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getActionCommand().equals(m_menuItemAddmod.getText()))
      {
         onUdfAddMod();
         revalidate();
      }
      else if (e.getActionCommand().equals(m_menuItemDelete.getText()))
      {
         onUdfDelete();
         revalidate();
      }
      else if (e.getActionCommand().equals(m_menuItemRefresh.getText()))
      {
         onUdfRefresh();
         revalidate();
      }
   }

   /**
    * Handles the "Add/Modify" action from the popup menu to open the
    * CreateUdfDialog.
    */
   public void onUdfAddMod()
   {
      try
      {
         Object root = m_treeModel.getRoot();
         TreePath path = m_tree.getSelectionModel().getSelectionPath();
         Object strUdfName = path.getLastPathComponent();

         CreateUdfDialog udfDialog = new CreateUdfDialog(
            (JDialog) getUltimateParent(this), m_udfSet, E2Designer.getApp()
               .getMainFrame());
         udfDialog.center();
         udfDialog.onEdit(strUdfName.toString());
         udfDialog.setVisible(true);

         // Adding new Udfs to PSUdfSet's application udfs
         PSCollection newUdfs = udfDialog.getNewUdfs();
         if (null != newUdfs)
         {
            for (int i = 0; i < newUdfs.size(); i++)
               m_udfSet.addApplicationUdf((IPSExtensionDef) newUdfs.get(i));

            newUdfs = null;
         }

         PSCollection appUdfs = m_udfSet.getUdfs(OSUdfConstants.UDF_APP);

         // repopulate tree with new leaves
         MapBrowserTreeNode leaf = null;

         // clearing tree of all nodes
         DefaultMutableTreeNode nodeAppBranch = null;
         Enumeration enumBranch = ((DefaultMutableTreeNode) root).children();
         while (enumBranch.hasMoreElements())
         {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumBranch
               .nextElement();
            if (node.toString().equals(
               E2Designer.getResources().getString("MapUdfApplication")))
            {
               nodeAppBranch = node;
               break;
            }
         }

         for (int i = m_treeModel.getChildCount(nodeAppBranch) - 1; i >= 0; i--)
            m_treeModel.removeNodeFromParent((MapBrowserTreeNode) m_treeModel
               .getChild(nodeAppBranch, 0));

         // set the new UDFs into the tree
         for (int i = 0; i < appUdfs.size(); i++)
         {
            try
            {
               IPSExtensionDef exit = (IPSExtensionDef) appUdfs.get(i);
               String strName = exit.getRef().getExtensionName();
               leaf = new MapBrowserTreeNode(strName, MapBrowserTreeNode.UDF,
                  "", exit, OSUdfConstants.UDF_APP, m_bIsQuery);
               m_treeModel.insertNodeInto(leaf, nodeAppBranch, i);
            }
            catch (Exception e)
            {
               // make a note of the problem and continue w/ processing
               e.printStackTrace();
            }
         }
      }
      catch (IllegalStateException e)
      { 
         /* ignore, a msg has already been displayed to the user */
      }
   }

   /**
    * Handles the "Delete" action from the popup menu to remove the selected
    * udf.
    */
   public void onUdfDelete()
   {
      TreePath path = m_tree.getSelectionModel().getSelectionPath();
      Object objRemove = path.getLastPathComponent();
      IPSExtensionDef def = ((MapBrowserTreeNode) objRemove).getUdfExit();
      String strName = def.getRef().getExtensionName();

      if (!m_udfSet.isAppUdfUsed(strName))
      {
         // remove references of the app udf in the PSUdfSet
         m_udfSet.removeApplicationUdf(strName);

         // remove the stored app udf in the server
         PSObjectStore store = E2Designer.getApp().getMainFrame()
            .getObjectStore();
         try
         {
            store.removeExtension(def.getRef());
         }
         catch (PSNotFoundException e)
         {
            // ignore; if the app udf is still in memory, not saved yet to
            // the server, we will get a PSNotFoundException from removing.
         }
         catch (Exception e)
         {
            // catching all other exceptions here; simply display error message
            PSDlgUtil.showErrorDialog(e.getLocalizedMessage(), E2Designer
               .getResources().getString("ServerErr"));
            e.printStackTrace();
         }

         // remove app udf display from udf tree branch
         DefaultMutableTreeNode nodeAppBranch = null;
         Object root = m_treeModel.getRoot();
         Enumeration enumBranch = ((DefaultMutableTreeNode) root).children();
         while (enumBranch.hasMoreElements())
         {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumBranch
               .nextElement();
            if (node.toString().equals(
               E2Designer.getResources().getString("MapUdfApplication")))
            {
               nodeAppBranch = node;
               break;
            }
         }

         for (int i = m_treeModel.getChildCount(nodeAppBranch) - 1; i >= 0; i--)
         {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_treeModel
               .getChild(nodeAppBranch, i);
            // both should be strings
            if (node.getUserObject().toString().equals(strName))
            {
               m_treeModel.removeNodeFromParent(node);
               break;
            }
         }
      }
      else
      {
         PSDlgUtil.showErrorDialog(sm_res.getString("errorUdfCannotRemove"),
            sm_res.getString("error"));
      }
   }

   /**
    * Handles the "Refresh" action from the popup menu to refresh udf tree from
    * server
    */
   private void onUdfRefresh()
   {
      PSCollection colAppUdfs = m_udfSet.getUdfs(OSUdfConstants.UDF_APP);
      PSCollection colGlobalUdfs = m_udfSet.getUdfs(OSUdfConstants.UDF_GLOBAL,
         true);
      
      // Now create the UDF tree
      makeTree(colGlobalUdfs, colAppUdfs);
   }

   /**
    * Searches for the encompassing JDialog. Done recursively.
    */
   private Component getUltimateParent(Component item)
   {
      Component parent = item.getParent();
      if (!(parent instanceof JDialog))
      {
         return getUltimateParent(parent);
      }
      else
      {
         return parent;
      }
   }

   /**
    * Show the context menu according to event source and row selection.
    *
    * @param e the mouse event, assumed not <code>null</code>.
    */
   private void showContextMenu(MouseEvent e)
   {
      int x = e.getX();
      int y = e.getY();

      TreePath tpSelectedPath = m_tree.getPathForLocation(x, y);
      m_tree.setSelectionPath(tpSelectedPath);

      if (e.isPopupTrigger())
      {
         String rootName = m_tree.getModel().getRoot().toString();
         if (rootName.equals(E2Designer.getResources().getString(
            "predefinedUdfs")))
         {
            if (tpSelectedPath != null)
            {
               if (tpSelectedPath.getLastPathComponent().toString().equals(
                  E2Designer.getResources().getString("predefinedUdfs")))
               {
                  m_contextMenu.removeAll();
                  m_contextMenu.add(m_menuItemRefresh);
                  m_contextMenu.show(m_tree, e.getX(), e.getY());
               }
               else
               {
                  Object[] pathList = tpSelectedPath.getPath();

                  if (pathList.length > 1
                     && pathList[1].toString().equals(
                        E2Designer.getResources()
                           .getString("MapUdfApplication")))
                  {
                     m_contextMenu.removeAll();
                     
                     // means that it's "UDF/Application/"
                     if (pathList.length > 2)
                     {
                        m_contextMenu.add(m_menuItemAddmod);
                        m_contextMenu.add(m_menuItemDelete);
                     }
                     else
                     {
                        m_contextMenu.add(m_menuItemAddmod);
                     }
                     m_contextMenu.show(m_tree, e.getX(), e.getY());
                  }
               }
            }
         }
      }
   }    

   /**
    * Initialize the tree with elements of provided type.
    *
    * @param elements the tree elements, assumed not <code>null</code>.
    * @param type the element type.
    */
   private void initTree(Object elements, int type)
   {
      m_treeModel = (DefaultTreeModel) m_tree.getModel();

      if (type == MapBrowserTreeNode.UDF)
         initUdfTree((PSUdfSet) elements);
      else if (type == MapBrowserTreeNode.CGI)
         initTree((Vector) elements, type, E2Designer.getResources().getString(
            "cgiVariables"));
      else if (type == MapBrowserTreeNode.USER_CONTEXT)
         initTree((Vector) elements, type, E2Designer.getResources().getString(
            "userContext"));
   }

   /**
    * Initialize the UDF tree with the provided UDF set.
    * 
    * @param udfSet the udf set with all the relevant info for all kinds of
    *    udfs.
    */
   private void initUdfTree(PSUdfSet udfSet)
   {
      m_udfSet.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            // the source will always be the new UdfExit
            IPSExtensionDef exit = (IPSExtensionDef) e.getSource();
            String strSourceName = exit.getRef().getExtensionName();

            // loop thru the existing ApplicationUdfs and PredefinedUdfs, if
            // any matches the name of this "exit" udf, return and do nothing
            PSCollection appUdfs = m_udfSet.getUdfs(OSUdfConstants.UDF_APP);
            for (int i = 0; i < appUdfs.size(); i++)
            {
               IPSExtensionDef appDef = (IPSExtensionDef) appUdfs.get(i);
               String strAppName = appDef.getRef().getExtensionName();
               if (strAppName.equals(strSourceName))
                  return;
            }

            // construct a node leaf from the exit
            MapBrowserTreeNode leaf = new MapBrowserTreeNode(strSourceName,
               MapBrowserTreeNode.UDF, "", exit, OSUdfConstants.UDF_APP,
               m_bIsQuery);

            DefaultTreeModel tModel = MapBrowser.this.m_treeModel;

            // add leaf to the MapBrowserTree
            if (null != leaf)
            {
               TreeNode node = ((DefaultMutableTreeNode) tModel.getRoot())
                  .getFirstChild();
               tModel.insertNodeInto(leaf, (MutableTreeNode) node, 0);
            }
         }
      });

      PSCollection colAppUdfs = udfSet.getUdfs(OSUdfConstants.UDF_APP);
      PSCollection colGlobalUdfs = udfSet.getUdfs(OSUdfConstants.UDF_GLOBAL);

      // Now create the UDF tree
      makeTree(colGlobalUdfs, colAppUdfs);
   }

   /**
    * Creates the UDFs tree.
    * 
    * @param colGlobalUdfs a collection of golbal UDFs guaranted not to be
    *    <code>null</code>.
    * @param colAppUdfs a collection of application specific UDFs guaranted
    *    not to be <code>null</code>.
    */
   private void makeTree(PSCollection colGlobalUdfs, PSCollection colAppUdfs)
   {
      // create root first
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(E2Designer
         .getResources().getString("predefinedUdfs"));

      m_treeModel.setRoot(root);

      String strAppUdf = E2Designer.getResources().getString(
         "MapUdfApplication");
      String strGlobalUdf = E2Designer.getResources().getString("MapUdfGlobal");

      m_appNode = new JTree.DynamicUtilTreeNode(root, new Vector());
      m_appNode.setUserObject(strAppUdf);

      m_globalNode = new JTree.DynamicUtilTreeNode(root, new Vector());
      m_globalNode.setUserObject(strGlobalUdf);

      m_treeModel.insertNodeInto(m_appNode, root, 0);
      m_treeModel.insertNodeInto(m_globalNode, root, 1);

      /* Clear vectors before any elements is added to them */
      m_udfAppNodes.clear();
      m_udfNodes.clear();

      /*
       * Get all categories (currently none) of the app UDFs and insert a node
       * for each of them
       */
      Iterator categories = getCategories(colAppUdfs);
      while (categories.hasNext())
         makeNode(m_appNode, (String) categories.next(), m_udfAppNodes);

      for (int i = 0; i < colAppUdfs.size(); i++)
      {
         IPSExtensionDef exit = (IPSExtensionDef) colAppUdfs.get(i);
         addUdf(exit, OSUdfConstants.UDF_APP);
      }

      /* Get all categories of global UDFs and insert a node for each of them */
      Iterator category = getCategories(colGlobalUdfs);
      while (category.hasNext())
         makeNode(m_globalNode, (String) category.next(), m_udfNodes);
      for (int i = 0; i < colGlobalUdfs.size(); i++)
      {
         IPSExtensionDef exit = (IPSExtensionDef) colGlobalUdfs.get(i);
         addUdf(exit, OSUdfConstants.UDF_GLOBAL);
      }

      // expands root node
      m_tree.expandRow(0);
   }

   /**
    * Inserts a global UDF at an appropriate node depending on UDF's category
    * 
    * @param exit a UDF that will be inserted, guaranted not to be 
    *    <code>null</code>.
    * @param iUdfType a type of UDF.
    */
   private void addUdf(IPSExtensionDef exit, int iUdfType)
   {
      String exitPath = null;
      String path = null;
      String exitCategory = null;

      if (iUdfType == OSUdfConstants.UDF_APP)
      {
         path = E2Designer.getResources().getString("predefinedUdfs") + "/"
            + E2Designer.getResources().getString("MapUdfApplication");
         exitCategory = exit.getRef().getCategory();
         /*
          * Since appUdfs do not have category string attrib set up their path
          * will be the same as the app root path
          */
         if (exitCategory == null || exitCategory.trim().length() == 0)
            exitPath = path;

         addUdf(exitPath, m_udfAppNodes, exit, m_appNode, iUdfType);
      }

      if (iUdfType == OSUdfConstants.UDF_GLOBAL)
      {
         path = E2Designer.getResources().getString("predefinedUdfs") + "/"
            + E2Designer.getResources().getString("MapUdfGlobal");
         exitCategory = exit.getRef().getCategory();
         exitPath = path + "/" + exitCategory;

         addUdf(exitPath, m_udfNodes, exit, m_globalNode, iUdfType);

      }
   }
   
   /**
    * Add the provided udf to the provided categories.
    * 
    * @param path the root path to which this should add the udf, assumed not
    * <code>null</code>.
    * @param nodes a vector of category folder nodesto which this will add the
    * provided udf, assumed not <code>null</code>, may be empty.
    * @param exit the udf to add, assumed not <code>null</code>.
    * @param root the root node which contains the provided catagories, assumed
    * not <code>null</code>.
    * @param iUdfType the udf type, assumes its one of OSUdfConstants.UDF_APP or
    * OSUdfConstants.UDF_GLOBAL.
    */
   private void addUdf(String path, Vector nodes, IPSExtensionDef exit,
      DefaultMutableTreeNode root, int iUdfType)
   {
      MapBrowserTreeNode leaf = new MapBrowserTreeNode(exit.getRef()
         .getExtensionName(), MapBrowserTreeNode.UDF, "", exit, iUdfType,
         m_bIsQuery);

      // for appUdf that do not have categorystring attrib set
      if (nodes.size() == 0 && iUdfType == OSUdfConstants.UDF_APP)
      {
         m_treeModel.insertNodeInto(leaf, root, m_treeModel
            .getChildCount(root));
         return;
      }

      /*
       * if a UDF is deprecated, but categorystring attrib is set to an empty
       * string
       */
      if (!exit.isDeprecated() && iUdfType == OSUdfConstants.UDF_GLOBAL)
      {
         if (exit.getRef().getCategory().trim().length() == 0)
         {
            m_treeModel.insertNodeInto(leaf, root, m_treeModel
               .getChildCount(root));
         }
         else
         {
            // loop through a vector of nodes
            for (int k = 0; k < nodes.size(); k++)
            {
               DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes
                  .get(k);
               
               // get the node path
               Object[] obj = node.getUserObjectPath();
               String nodePath = new String();
               
               // transfer it into a string
               for (int i = 0; i < obj.length; i++)
               {
                  String str = (String) obj[i];
                  if (i == obj.length - 1)
                     nodePath += str;
                  else
                     nodePath += str + "/";
               }
               
               /*
                * if a node's path and udf's path match, insert this UDF at
                * that node
                */
               if (path.equals(nodePath))
                  m_treeModel.insertNodeInto(leaf, node, m_treeModel
                     .getChildCount(node));
            }
         }
      }
   }

   /**
    * Inserts the child node at the parent node
    * 
    * @param parent a parent node that a child node will be inserted at assumed
    *    not to be <code>null</code>.
    * @param child a child node to be inserted, assumed not to be 
    *    <code>null</code>.
    * @param exits a vector of inserted nodes, assumed not to be 
    *    <code>null</code>.
    */
   private void insertNode(DefaultMutableTreeNode parent,
      DefaultMutableTreeNode child, Vector<DefaultMutableTreeNode> exits)
   {
      m_treeModel.insertNodeInto(child, parent, m_treeModel
         .getChildCount(parent));
      exits.addElement(child);
   }

   /**
    * Creates nodes that represents UDFs categories. Since the category can be
    * multilevel category this method calls itself until it reaches the last
    * level(inclusive) of the extension's categorystring attribute. The category
    * string is expected as root/level1/level2/..., delimiter can only be "/"
    * 
    * @param parent a parent node that a newly created node will be inserted at
    * can not be <CODE>null</CODE>
    * @param category a string that represents category, asssumed not to be
    * <code>null</code>
    * @param exits a vector that will hold udfs categories, assumed not to be
    * <CODE>null</CODE>
    */
   private void makeNode(DefaultMutableTreeNode parent, String category,
      Vector<DefaultMutableTreeNode> exits)
   {
      if (parent == null)
         throw new IllegalArgumentException("Parent node can not be null");

      boolean found = false;
      // if a category is single-level
      if (category.indexOf("/") == -1)
      {
         DefaultMutableTreeNode node = null;
         DefaultMutableTreeNode child = new DefaultMutableTreeNode(category);
         child.setUserObject(category);

         for (int i = 0; i < m_treeModel.getChildCount(parent)
            && found == false; i++)
         {
            node = (DefaultMutableTreeNode) m_treeModel.getChild(parent, i);
            if (node == null)
               continue;
            String nodeName = (String) node.getUserObject();
            if (nodeName.equals(category))
               found = true;
         }

         if (!found)
         {
            /*
             * if not found we want to insert it and we want to store it in a
             * vector of nodes
             */
            insertNode(parent, child, exits);
         }
         else
            m_treeModel.insertNodeInto(node, parent, m_treeModel
               .getChildCount(parent));
      }
      
      // if a category is multilevel category
      if (category.indexOf("/") != -1)
      {
         // get a first level of the category, and make a node using that string
         String newCategory = category.substring(0, category.indexOf("/"));
         // get a remaining categorysting which will be passed
         int separator = category.indexOf("/") + 1;
         String childCategory = category.substring(separator);

         // create a node
         DefaultMutableTreeNode child = new DefaultMutableTreeNode(newCategory);
         child.setUserObject(newCategory);
         DefaultMutableTreeNode node = null;

         for (int i = 0; i < m_treeModel.getChildCount(parent)
            && found == false; i++)
         {
            node = (DefaultMutableTreeNode) m_treeModel.getChild(parent, i);
            if (node == null)
               continue;
            String nodeName = (String) node.getUserObject();
            if (nodeName.equals(newCategory))
               found = true;
         }
         if (!found)
         {
            // if not found insert a newly created node
            insertNode(parent, child, exits);
            makeNode(child, childCategory, exits);
         }

         /* if found just pass that node as a parent */
         else
            makeNode(node, childCategory, exits);
      }
   }

   /**
    * Sorts the provided collection of exits ascending and casesensitive across
    * their category string. Returns a sorted collection of all the global UDFs
    * categories
    * 
    * @param udfCollection The <code>List</code> of any collection of
    *    IPSExtensionDef's udfs, it can not be <code>null</code>.
    * @return an iterator of udfs' categories collection can not be 
    *    <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Iterator getCategories(List udfCollection)
   {
      if (udfCollection == null)
         throw new IllegalArgumentException("UDF collection can not be null");

      // use treemap to get unique categories' types and to sort them
      TreeMap categories = new TreeMap(new PSStringComparator(
         PSStringComparator.SORT_CASE_INSENSITIVE_ASC));

      for (int i = 0; i < udfCollection.size(); i++)
      {
         IPSExtensionDef exit = (IPSExtensionDef) udfCollection.get(i);
         String category = exit.getRef().getCategory();

         if (category.trim().length() != 0 && (!exit.isDeprecated()))
            categories.put(exit.getRef().getCategory(), exit.getRef()
               .getCategory());
      }
      return categories.values().iterator();
   }

   /**
    * Initialize the tree with the provided IDataTypeInfo elements.
    *
    * @param elements the tree elements
    * @param type
    * @param rootName
    */
   private void initTree(Vector elements, int type, String rootName)
   {
      MapBrowserTreeNode root = new MapBrowserTreeNode(rootName, type,
         rootName, m_bIsQuery);
      m_treeModel.setRoot(root);

      MapBrowserTreeNode leaf = null;
      for (int i = 0, n = elements.size(); i < n; i++)
      {
         leaf = new MapBrowserTreeNode(elements.elementAt(i), type,
            (String) elements.elementAt(i), m_bIsQuery);
         m_treeModel.insertNodeInto(leaf, root, i);
      }

      m_tree.expandRow(0);
   }

   /**
    * Initialize the tree.
    */
   private void initTree(OSBackendTable table)
   {
      m_treeModel = (DefaultTreeModel) m_tree.getModel();
      MapBrowserTreeNode root = new MapBrowserTreeNode(table.getAlias(),
         MapBrowserTreeNode.BACKEND, table.getAlias(), m_bIsQuery);
      m_treeModel.setRoot(root);

      MapBrowserTreeNode leaf = null;
      Vector columns = table.getColumns();
      for (int i = 0, n = columns.size(); i < n; i++)
      {
         String column = (String) columns.elementAt(i);
         leaf = new MapBrowserTreeNode(column, MapBrowserTreeNode.BACKEND,
            table.getAlias() + "." + column, m_bIsQuery);
         m_treeModel.insertNodeInto(leaf, root, i);
      }

      m_tree.expandRow(0);
   }

   /**
    * @param subList node list to get the parent name
    * 
    * @return the name of the list parent, or invalid if not parent is found or
    *    list is null
    */
   private String getListParent(PSDtdNodeList subList)
   {
      String csRet = new String("invalid");
      if (subList != null)
      {
         // get the parent element
         PSDtdElementEntry entry = subList.getParentElement();
         if (entry != null)
         {
            // get the entry
            PSDtdElement elem = entry.getElement();
            if (elem != null)
            {
               // get the name
               csRet = elem.getName();
            }
         }
         else
         {
            Object obj = null;
            int limit = subList.getNumberOfNodes();
            PSDtdElementEntry element;
            PSDtdElementEntry parent = null;
            PSDtdElement elem = null;
            for (int count = 0; count < limit; count++)
            {
               // mia
               obj = subList.getNode(count);
               if (obj instanceof PSDtdElementEntry)
               {
                  element = (PSDtdElementEntry) obj;
                  if (element != null)
                  {
                     parent = element.getParentElement();
                     if (parent != null)
                     {
                        elem = parent.getElement();
                        if (elem != null)
                        {
                           csRet = elem.getName();
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
      
      return csRet; // return the name or invalid
   }

   /**
    * This is the main routine for the parser, based on the object will call the
    * proper routines ( or itself ).
    * 
    * @param obj the entry to work o.n
    * 
    * @param folder the tree folder to put the elements into.
    * 
    * @param rootName the root name entry.
    * 
    * @param name the parents name ( used differently based on content).
    * 
    * @param subName the childs name ( used differently based on content ).
    * 
    * @param usedNames Contains the set of element names that have already been
    * processed. Used by this method to track recursive loops. When first
    * called, it should be an empty map. If the supplied object is an element
    * whose name appears in this map, the method processes the element, but
    * doesn't recurse on it.
    */
   @SuppressWarnings("unchecked")
   private void processElement(Object obj, MapBrowserTreeNode folder,
      String rootName, String name, String subName, Set usedNames)
   {
      if (obj == null) // if null do nothing
      {
         return;
      }
      else if (obj instanceof PSDtdNodeList) // if a list
      {
         // process it
         obj = processList(folder, (PSDtdNodeList) obj, rootName, name,
            usedNames);
         // if the return object is a list also
         if (obj instanceof PSDtdNodeList)
         {
            // get the name, and overwrite it
            name = getListParent((PSDtdNodeList) obj);
         }
         // call itself
         processElement(obj, folder, rootName, name, subName, usedNames);
      }
      else if (obj instanceof PSDtdElementEntry) // element entry
      {
         Object original = obj; // preserve this object
         // insert the element into the tree
         obj = insertElementEntry(folder, (PSDtdElementEntry) obj, rootName);

         PSDtdElementEntry node = (PSDtdElementEntry) original; //
         if (node != null)
         {
            if (obj instanceof PSDtdNodeList) // if return is a list?
            {
               // find the node on the tree
               MapBrowserTreeNode newFolder = findNode(folder, node);
               if (newFolder != null)
                  folder = newFolder; // found patch it
            }
            PSDtdElement el = node.getElement(); // get the element
            if (el != null)
            {
               name = el.getName(); // get the name and patch it
            }
         }

         // check for recursion and stop it
         String elemName = ((PSDtdElementEntry) original).getElement()
            .getName();
         if (usedNames.contains(elemName))
            return;
         else
            usedNames.add(elemName);

         // call itself
         processElement(obj, folder, rootName, name, subName, usedNames);
      }
   }

   /**
    * process the list, if is an PSDtdNodeList.OPTIONLIST process it else store
    * to latter process
    * 
    * @param folder tree to insert elements into
    * 
    * @param subList the element list
    * 
    * @param rootName the root name ( used differently based on content )
    * 
    * @param name the folder ( item ) name (used differently based on content )
    * 
    * @param usedNames See same parameter for {@link #processElement(Object, 
    *    MapBrowserTreeNode, String, String, String, Set)} for a description.
    * 
    * @return the content to be processed
    */
   @SuppressWarnings("unchecked")
   private Object processList(MapBrowserTreeNode folder, PSDtdNodeList subList,
      String rootName, String name, Set usedNames)
   {
      MapBrowserTreeNode lastNode = null;
      String subName = new String();
      String path = new String();
      int limit = subList.getNumberOfNodes();
      
      // walk trough all the nodes
      for (int count = 0; count < limit; count++)
      {
         Object obj = subList.getNode(count);
         
         // is it an entry? (#PCDATA)
         if (obj instanceof PSDtdDataElement)
         {
            // if processing the first element and we have more to process
            if (count == 0 && limit > 1)
            {
               path = rootName + "/" + name;
               // THIS USES an numeric entry
               lastNode = new MapBrowserTreeNode(2101, "#PCDATA",
                  MapBrowserTreeNode.XML, path, m_bIsQuery);
               lastNode.setNodeType(MapBrowserTreeNode.NODETYPECHILD);
               lastNode.setNodeReadOnly();
               if (folder == null)
               {
                  folder = (MapBrowserTreeNode) m_treeModel.getRoot();
               }
               m_treeModel.insertNodeInto(lastNode, folder, m_treeModel
                  .getChildCount(folder));
            }
         }

         // if it is an entry
         if (obj instanceof PSDtdElementEntry)
         {
            PSDtdElementEntry element = (PSDtdElementEntry) obj;
            PSDtdElement elem = element.getElement();
            if (elem != null)
            {
               subName = elem.getName();
               path = rootName + "/" + name;
               PSDtdElementEntry node = element.getParentElement();
               if (node != null)
               {
                  MapBrowserTreeNode newFolder = findNode(folder, node);
                  if (newFolder != null)
                  {
                     folder = newFolder;
                     String newpath = folder.getPathString();
                     path = newpath + "/" + subName;
                  }
               }

               if (findInChildren(folder, element) == null)
               {
                  lastNode = new MapBrowserTreeNode(element, subName,
                     MapBrowserTreeNode.XML, path, m_bIsQuery);
                  lastNode.setNodeType(MapBrowserTreeNode.NODETYPECHILD);
                  m_treeModel.insertNodeInto(lastNode, folder, m_treeModel
                     .getChildCount(folder));
               }
            }

            obj = elem.getContent();
            processAttributes(lastNode, elem, path);
         }

         if (obj instanceof PSDtdElementEntry)
            processElement(obj, lastNode, rootName, "", subName, usedNames);

         // if is a list
         if (obj instanceof PSDtdNodeList)
         {
            PSDtdNodeList list = (PSDtdNodeList) obj;
            int type = list.getType();

            if (type == PSDtdNodeList.OPTIONLIST)
            {
               processList(lastNode, (PSDtdNodeList) obj,
                  rootName + "/" + name, subName, usedNames);
            }
            else if (type == PSDtdNodeList.SEQUENCELIST)
            {
               processList(lastNode, (PSDtdNodeList) obj,
                  rootName + "/" + name, subName, usedNames);
            }
         }
      }
      
      return null;
   }

   /**
    * process an elemententry.
    * 
    * @param treeNode the tree item to insert under.
    * 
    * @param element the element to insert.
    * 
    * @param rootName the rootName element ( used differently based on content ).
    * 
    * @return the content to be processed.
    */
   private Object insertElementEntry(MapBrowserTreeNode treeNode,
      PSDtdElementEntry element, String rootName)
   {
      MapBrowserTreeNode lastNode = null;
      String path = new String();
      Object obj = null;

      PSDtdElement elem = element.getElement();
      if (elem != null)
      {
         String name = elem.getName(); // get the name
         path = rootName + "/" + name; // make the path
         PSDtdElementEntry node = element.getParentElement();
         if (node != null)
         {
            MapBrowserTreeNode newFolder = findNode(treeNode, node);
            if (newFolder != null)
            {
               treeNode = newFolder; // patch the folder
               String newpath = treeNode.getPathString(); // get the path
               String tmp = new String();
               // see if path contains the root
               if (newpath.indexOf(rootName) == -1)
               {
                  // no get the full path
                  tmp += rootName + "/" + newpath + "/" + name;
                  tmp = newpath + "/" + name; // generate a sub path

               }
               else
               {
                  tmp = newpath + "/" + name; // generate a sub path
               }
               path = tmp; // patch the path
            }
         }
         
         // insert it
         lastNode = new MapBrowserTreeNode(element, name,
            MapBrowserTreeNode.XML, path, m_bIsQuery);
         lastNode.setNodeType(MapBrowserTreeNode.NODETYPECHILD);
         m_treeModel.insertNodeInto(lastNode, treeNode, m_treeModel
            .getChildCount(treeNode));
         
         processAttributes(lastNode, elem, path);

         obj = elem.getContent();// get the content
      }
      
      return obj; // and return it
   }

   /**
    * checks if element contains attributes if so process
    * 
    * @param folder the tree item to insert under
    * 
    * @param el element to check for attributes
    * 
    * @param path the path to insert under
    */
   private void processAttributes(MapBrowserTreeNode folder, PSDtdElement el,
      String path)
   {
      if (el != null)
      {
         // has attributes?
         int limit = el.getNumAttributes();
         if (limit > 0)
         {
            // yes
            PSDtdAttribute attrib = null;
            String csattribName = new String();
            MapBrowserTreeNode lastNode = null;
            // walk trough all the elements
            for (int count = 0; count < limit; count++)
            {
               attrib = el.getAttribute(count);
               csattribName = attrib.getName();
               String newPath = path + "/" + csattribName;
               // insert it, see the number
               lastNode = new MapBrowserTreeNode(ATTRIBUTE_ICON, csattribName,
                  MapBrowserTreeNode.XML, newPath, m_bIsQuery);
               lastNode.setNodeReadOnly();
               if (folder == null)
               {
                  folder = (MapBrowserTreeNode) m_treeModel.getRoot();
               }
               m_treeModel.insertNodeInto(lastNode, folder, m_treeModel
                  .getChildCount(folder));
            }
         }
      }
   }

   /**
    * Finds the node on the tree starting at the provided source containing 
    * the supplied entry.
    * 
    * @param source the node in which to search for the supplied entry, may
    *    be <code>null</code> in which case we start at the root.
    * @param entry the element to be found, may be <code>null</code>.
    * @return the tree element or <code>null</code> if not found.
    */
   private MapBrowserTreeNode findNode(MapBrowserTreeNode source, 
      PSDtdElementEntry entry)
   {
      MapBrowserTreeNode retNode = null;

      if (entry != null)
      {
         if (source == null)
            source = (MapBrowserTreeNode)m_treeModel.getRoot();
         
         PSDtdElementEntry test = source.getElement();
         if (test.equals(entry))
            retNode = source;
         else
            retNode = findInChildren(source, entry);
      }
      
      return retNode;
   }

   private boolean elementsAreEqual(PSDtdElementEntry source,
      PSDtdElementEntry target)
   {
      boolean bEqual = false;
      if (source != null && target != null)
      {
         PSDtdElement elemSrc = source.getElement();
         if (elemSrc != null)
         {
            String srcName = elemSrc.getName();
            PSDtdElement elemTrg = target.getElement();
            if (elemTrg != null)
            {
               String trgName = elemTrg.getName();
               if (srcName.equals(trgName))
               {
                  Object sourceParent = source.getParentElement();
                  Object targetParent = target.getParentElement();
                  if (sourceParent == null && targetParent == null)
                  {
                     bEqual = true;
                  }
                  else
                  {
                     srcName = "";
                     trgName = "";
                     if (sourceParent != null)
                     {
                        if (sourceParent instanceof PSDtdElementEntry)
                        {
                           PSDtdElementEntry entry = (PSDtdElementEntry) sourceParent;
                           elemTrg = entry.getElement();
                           if (elemTrg != null)
                           {
                              srcName = elemTrg.getName();
                           }
                        }
                        else if (sourceParent instanceof PSDtdNodeList)
                        {
                           PSDtdNodeList subList = (PSDtdNodeList) sourceParent;
                           srcName = getListParent(subList);
                        }
                     }
                     if (targetParent != null)
                     {
                        if (targetParent instanceof PSDtdElementEntry)
                        {
                           PSDtdElementEntry entry = (PSDtdElementEntry) targetParent;
                           elemTrg = entry.getElement();
                           if (elemTrg != null)
                           {
                              trgName = elemTrg.getName();
                           }
                        }
                        else if (targetParent instanceof PSDtdNodeList)
                        {
                           PSDtdNodeList subList = (PSDtdNodeList) targetParent;
                           trgName = getListParent(subList);
                        }
                     }
                     if (trgName.equals(srcName))
                     {
                        bEqual = true;
                     }
                  }
               }
            }
         }
      }

      return bEqual;
   }

   /**
    * search all the tree subnodes for the entry
    * 
    * @param node the starting search entry
    * 
    * @param parent the element to be found
    */
   private MapBrowserTreeNode findInChildren(MapBrowserTreeNode node,
      PSDtdElementEntry parent)
   {
      if (node != null)
      {
         int count = node.getChildCount(); // get the children count
         if (count > 0) // has children
         {
            Enumeration v = node.children();
            while (v.hasMoreElements())
            {
               node = (MapBrowserTreeNode) v.nextElement();
               PSDtdElementEntry entry = node.getElement();
               if (entry != null) // are equal?
               {
                  if (elementsAreEqual(entry, parent))
                  {
                     return (node); // found it
                  }
               }
               // see if this node has children of the own
               if (node != null && node.getChildCount() > 0)
               {
                  node = findInChildren(node, parent); // find it
                  if (node != null)
                  {
                     return (node); // found it
                  }
               }
            }
            // see if the last entry has children
            if (node != null && node.getChildCount() > 0)
            {
               node = findInChildren(node, parent);// find it
               if (node != null)
               {
                  return (node); // found it
               }
            }
         }
         else
         {
            // single element
            PSDtdElementEntry entry = node.getElement();
            if (entry != null)
            {
               if (elementsAreEqual(entry, parent))
               {
                  return (node); // found it
               }
            }
         }
      }
      return (null); // was not found
   }

   /**
    * Initialize the tree.
    */
   public void initTree(OSPageDatatank page)
   {
      m_treeModel = (DefaultTreeModel) m_tree.getModel();

      if (!page.isUsingDTD())
      {
         m_tree.setDTDRepeatAttributesReadOnly();
         Vector columns = page.getColumns();
         parseColumns(columns, MapBrowserTreeNode.FORMS);
      }
      else if (page.isUsingDTD())
      {
         m_DTDtree = page.getTree();

         if (m_DTDtree == null || m_DTDtree != null
            && m_DTDtree.getRoot() == null)
         {
            m_tree.setDTDRepeatAttributesReadOnly();
            Vector columns = page.getColumns();
            parseColumns(columns, MapBrowserTreeNode.XML);
            return;
         }

         MapBrowserTreeNode root = null;
         MapBrowserTreeNode folder = null;
         PSDtdElementEntry elemRoot = null;

         elemRoot = m_DTDtree.getRoot();

         String name, path;

         PSDtdElement el = elemRoot.getElement();

         String rootName = el.getName();
         root = new MapBrowserTreeNode(elemRoot, rootName,
            MapBrowserTreeNode.XML, rootName, m_bIsQuery);
         root.setNodeType(MapBrowserTreeNode.NODETYPEROOT);
         m_treeModel.setRoot(root);

         Object obj = el.getContent();
         Object obj2 = null;
         PSDtdElementEntry ee;

         // perform checks for root element attributes first, then
         // process/insert them
         if (obj != null)
         {
            path = rootName;
            processAttributes(root, el, path);
         }

         Set usedNames = new HashSet();
         if (obj instanceof PSDtdNodeList)
         {
            PSDtdNodeList nodeList = (PSDtdNodeList) obj;
            int limit = nodeList.getNumberOfNodes();

            if (nodeList.getType() == PSDtdNodeList.SEQUENCELIST)
            {
               for (int count = 0; count < limit; count++)
               {
                  ee = (PSDtdElementEntry) nodeList.getNode(count);
                  el = ee.getElement();
                  name = el.getName();
                  path = rootName + "/" + name;
                  boolean bAdd = true;
                  MapBrowserTreeNode fd = findNode(root, ee);
                  if (fd != null)
                  {
                     // found it see if is empty
                     int t = m_treeModel.getChildCount(fd);
                     if (t == 0)
                     {
                        bAdd = false;
                     }
                  }
                  if (bAdd)
                  {
                     obj2 = el.getContent();

                     folder = new MapBrowserTreeNode(ee, name,
                        MapBrowserTreeNode.XML, path, m_bIsQuery);
                     folder.setNodeType(MapBrowserTreeNode.NODETYPEFOLDER);
                     m_treeModel.insertNodeInto(folder, root, m_treeModel
                        .getChildCount(root));
                     if (obj2 != null)
                     {
                        processAttributes(folder, el, path);
                        processElement(obj2, folder, rootName, name, "",
                           usedNames);
                     }
                  }

               }
            }// if node list
            else
            {
               Object objNode = null;
               nodeList = (PSDtdNodeList) obj;
               limit = nodeList.getNumberOfNodes();
               for (int count = 0; count < limit; count++)
               {
                  objNode = nodeList.getNode(count);
                  if (objNode instanceof PSDtdDataElement)
                     continue;

                  if (objNode instanceof PSDtdElementEntry)
                  {
                     ee = (PSDtdElementEntry) objNode;
                     el = ee.getElement();
                     name = el.getName();
                     path = rootName + "/" + name;
                     boolean bAdd = true;
                     MapBrowserTreeNode fd = findNode(root, ee);
                     if (fd != null)
                     {
                        // found it see if is empty
                        int t = m_treeModel.getChildCount(fd);
                        if (t == 0)
                        {
                           bAdd = false;
                        }
                     }
                     if (bAdd)
                     {
                        obj2 = el.getContent();
                        folder = new MapBrowserTreeNode(ee, name,
                           MapBrowserTreeNode.XML, path, m_bIsQuery);
                        folder.setNodeType(MapBrowserTreeNode.NODETYPEFOLDER);
                        m_treeModel.insertNodeInto(folder, root, m_treeModel
                           .getChildCount(root));
                        if (obj2 != null)
                        {
                           processAttributes(folder, el, path);
                           processElement(obj2, folder, rootName, name, "",
                              usedNames);
                           // if( obj2 instanceof PSDtdDataElement )
                           // folder.setNodeReadOnly();
                        }
                     }
                  }
               }// for
            } // else
         }
         else if (obj instanceof PSDtdElementEntry)
         {
            processElement(obj, root, rootName, "", "", usedNames);
         }

      }
      m_tree.expandRow(0);

      // *** added code to fix bug (PHOD-4AGL29)
      // expand using breadthFirstEnumeration until the next node will cause
      // the tree to go "out of bounds"
      MapBrowserTreeNode treeRoot = (MapBrowserTreeNode) m_treeModel.getRoot();
      Enumeration e = treeRoot.breadthFirstEnumeration();
      while (e.hasMoreElements())
      {
         // if no more space is available to expand, stop now
         if (!isTreeWithinBounds())
            return;

         TreeNode trNode = (TreeNode) e.nextElement();
         if (!trNode.isLeaf())
         {
            TreePath thpath = new TreePath(m_treeModel.getPathToRoot(trNode));

            if (m_tree.isCollapsed(thpath))
               m_tree.expandPath(thpath);
         }
      }
   }

   /**
    * Re-creates the XML tree that is embedded in the entries in elementList.
    * Each call to this method processes all elements at a given depth. It
    * calls this method recursively to process nodes at greater depths.
    * 
    * @param originalList a list of xml 'columns', using fully qualified
    *    names. The vector contents are destroyed by this method.
    * @param type the type of the node to create
    */
   @SuppressWarnings("unchecked")
   private void parseColumns(Vector originalList, int type)
   {
      // make a local copy, so the original remains untouched
      Vector elementList = new Vector(originalList);

      int nEffectiveCount = elementList.size();
      String strTemp = null;
      String strLeft = null;
      String strBaseElement = new String();
      Vector children = null;
      Hashtable treeNodes = new Hashtable();
      int nLoc = 0;
      while (nEffectiveCount > 0)
      {
         nEffectiveCount = 0;
         for (int i = 0; i < elementList.size(); i++)
         {
            strTemp = (String) elementList.elementAt(i);
            if (null == strTemp || strTemp.equals(""))
               continue;

            nEffectiveCount++;

            nLoc = strTemp.indexOf('/');
            if (nLoc != -1)
            {
               strLeft = strTemp.substring(0, nLoc);
               strTemp = strTemp.substring(nLoc + 1);
            }
            else
            {
               strLeft = strTemp;
               strTemp = "";
            }
            // This is the base element for the DTD
            if (strBaseElement.equals(""))
               strBaseElement = strLeft;

            elementList.setElementAt(strTemp, i);

            children = (Vector) treeNodes.get(strLeft);
            if (null == children)
            {
               treeNodes.put(strLeft, new Vector());
               children = (Vector) treeNodes.get(strLeft);
            }

            nLoc = strTemp.indexOf('/');
            if (-1 == nLoc)
               strLeft = strTemp;
            else
               strLeft = strTemp.substring(0, nLoc);
            if (!strLeft.equals("") && -1 == children.indexOf(strLeft))
               children.addElement(strLeft);
         }
      }
      buildTree(null, type, "", strBaseElement, treeNodes);
   }

   /**
    * Re-constructs a tree from a hashtable that contains vectors with children.
    * Each entry in the hashtable is a vector that contains the children for
    * that entry's key. <p/> This method builds the tree recursively. Each call
    * handles all the nodes at a particular depth.
    * 
    * @param folder the parent folder at a given level. Should be null for the
    * root level (which is what it should be when it is first called).
    * 
    * @param type the type for the node. This is passed directly when
    * constructing the tree nodes.
    * 
    * @param path the path of the supplied folder. If the folder is null, the
    * path should be the empty string (not null).
    * 
    * @param root the name of the current root node
    * 
    * @param nodeNames contains all the elements in the tree, with the keys
    * being the names of all the parent nodes and their values being a vector
    * containing all the children for that node. The root should appear once as
    * a key. All parent nodes should appear twice, once as a key and once as a
    * child in one of the children vectors. Leafs will appear once in one of the
    * children vectors.
    */
   private void buildTree(MapBrowserTreeNode folder, int type, String path,
      String root, Hashtable nodeNames)
   {
      MapBrowserTreeNode node = null;
      if (path == null || path.trim().length() == 0)
         path = root;
      else
         path = path + "/" + root;

      node = new MapBrowserTreeNode(root, type, path, m_bIsQuery);

      if (folder == null)
      {
         m_treeModel.setRoot(node);
      }
      else
      {
         m_treeModel.insertNodeInto(node, folder, m_treeModel
            .getChildCount(folder));
      }

      Object o = nodeNames.get(root);
      if (o instanceof Vector)
      {
         Vector v = (Vector) o;
         for (int i = 0; i < v.size(); i++)
         {
            String s = (String) v.elementAt(i);
            if (!nodeNames.containsKey(s))
            {
               /*
                * code below seems to deal with children of the current node
                * that have no entry in the nodeNames map. Not sure that this is
                * possible, but code should handle it properly if it happens.
                */
               String childPath;
               if (folder != null)
               {
                  childPath = path + "/" + s;
               }
               else
               {
                  folder = node;
                  childPath = s;
               }

               node = new MapBrowserTreeNode(s, type, childPath, m_bIsQuery);
               m_treeModel.insertNodeInto(node, folder, m_treeModel
                  .getChildCount(folder));
            }
            else
            {
               // if we are on the root, don't want to include it in the path
               if (folder == null)
                  buildTree(node, type, "", s, nodeNames);
               else
               {
                  buildTree(node, type, path, s, nodeNames);
               }
            }
         }
      }
   }

   public void updateDirtyFlag()
   {
      // need to check if the DTDtree exists, it does not exist if dataset is
      // update. TODO: this is not correct, under unified model a tree should
      // always exist.
      if (null != m_DTDtree)
         m_DTDtree.setTreeDirty(getTree().isDTDDirty());
   }

   // temp routine until DTD copy can be fixed
   public void restore()
   {
      if (!m_tree.getDTDRepeatAttributesReadOnly() && m_tree.isDTDDirty())
      {
         m_DTDtree.setTreeDirty(false);
         MapBrowserTreeNode node = (MapBrowserTreeNode) m_treeModel.getRoot();
         notifyAllChildren(node);
      }
   }

   @SuppressWarnings("unchecked")
   private void notifyAll(MapBrowserTreeNode node)
   {
      if (visitedNodesMap.put(node.getPathString(), node) != null)
         return;

      int count = node.getChildCount();
      if (count > 0)
      {
         Enumeration v = node.children();
         while (v.hasMoreElements())
         {
            node = (MapBrowserTreeNode) v.nextElement();
            node.restoreOriginalDTDRepeat();
            if (node.getChildCount() > 0)
            {
               notifyAll(node);
            }
         }
         if (node.getChildCount() > 0)
         {
            notifyAll(node);
         }
      }
      else
      {
         node.restoreOriginalDTDRepeat();
      }
   }

   // temp routine until DTD copy can be fixed
   private void notifyAllChildren(MapBrowserTreeNode node)
   {
      if (visitedNodesMap == null)
      {
         visitedNodesMap = new HashMap();
      }
      else
      {
         visitedNodesMap.clear();
      }
      notifyAll(node);
   }

   /**
    * Checks the current tree height and compares with the tree-viewport height
    * to see if the tree is still within the height bounds.
    *
    * @returns boolean <CODE>true</CODE> = the tree has not expanded beyond the
    * bounds. <CODE>false</CODE> = tree has expanded beyond bounds.
    */
   private boolean isTreeWithinBounds()
   {
      // limit 320
      int treeHeight = m_tree.getPreferredScrollableViewportSize().height;
      int rowHeight = m_tree.getRowHeight();
      int rowCount = m_tree.getRowCount();

      return (treeHeight > rowHeight * rowCount) ? true : false;
   }

   @SuppressWarnings("unused")
   public void valueChanged(ListSelectionEvent event)
   {
      JList list = m_selectionView.getListData();
      ImageListItem item = (ImageListItem) list.getSelectedValue();
      Object data = item.getData();
      if (data instanceof OSBackendTable)
         initTree((OSBackendTable) data);
      else if (data instanceof OSPageDatatank)
         initTree((OSPageDatatank) data);
      else
      {
         if (item.getText().equals(
            E2Designer.getResources().getString("cgiVariables")))
            initTree(data, MapBrowserTreeNode.CGI);
         else if (item.getText().equals(
            E2Designer.getResources().getString("userContext")))
            initTree(data, MapBrowserTreeNode.USER_CONTEXT);
         else if (item.getText().equals(
            E2Designer.getResources().getString("predefinedUdfs")))
            initTree(data, MapBrowserTreeNode.UDF);
      }
   }

   /**
    * Get the selected item in the current list
    */
   public MapBrowserTreeNode getSelectedNode()
   {
      TreePath path = m_tree.getSelectionPath();
      if (path != null)
      {
         MapBrowserTreeNode selection = (MapBrowserTreeNode) path
            .getLastPathComponent();
         if (selection != null && selection.isLeaf())
         {
            return (new MapBrowserTreeNode(selection));
         }
      }

      return (null);
   }

   /**
    * @return The member <CODE>MapBrowserTree</CODE> reference used in this
    * instance of <CODE>MapBrowser</CODE>.
    */
   public MapBrowserTree getTree()
   {
      return (m_tree);
   }


   /**
    * Inner class that handles the tree node selection pop-up menu display. On
    * the right click of the mouse, it will pop-up the udf context menu.
    * Currently, only Application Udfs will allow add/modify and delete
    * functionalities. All other types of Udfs will ignore the right click.
    */
   private class UdfTreeNodeSelector extends MouseAdapter
   {
      /**
       * Simply passes the <CODE>MouseEvent</CODE> object to our pop-up menu
       * display code.
       * 
       * @param e The event generated on the tree.
       */
      @Override
      public void mouseReleased(MouseEvent e)
      {
         showContextMenu(e);
      }
   }

   /**
    * a vector of all items in the selection view
    */
   private Vector m_selectionItems = null;

   /**
    * the selection view
    */
   private ImageListControl m_selectionView = null;

   /**
    * the selection tree
    */
   private PSDtdTree m_DTDtree = null;

   protected MapBrowserTree m_tree = null;

   /**
    * the tree model
    */
   private DefaultTreeModel m_treeModel = null;

   private Boolean m_bIsQuery = Boolean.FALSE;

   /**
    * the popup menu and its items
    */
   private JPopupMenu m_contextMenu = new JPopupMenu();

   private JMenuItem m_menuItemAddmod = new JMenuItem(sm_res
      .getString("addmod"));

   private JMenuItem m_menuItemDelete = new JMenuItem(sm_res
      .getString("delete"));

   private JMenuItem m_menuItemRefresh = new JMenuItem(sm_res
      .getString("refresh"));

   /**
    * The reference to PSUdfSet in the MapperPropertyDialog.
    */
   private PSUdfSet m_udfSet = null;

   /**
    * The resource bundle of MapperPropertyDialog
    */
   private static ResourceBundle sm_res = null;
   static
   {
      try
      {
         if (sm_res == null)
            sm_res = ResourceBundle.getBundle(
               "com.percussion.E2Designer.MapperPropertyDialogResources",
               Locale.getDefault());
      }
      catch (MissingResourceException e)
      {
         e.printStackTrace();
      }
   }

   private HashMap visitedNodesMap = null;

   /**
    * A collection of inserted nodes. Gets populated in
    * {@link #makeNode(DefaultMutableTreeNode, String, Vector)}.
    */
   private Vector<DefaultMutableTreeNode> m_udfNodes = 
      new Vector<DefaultMutableTreeNode>();

   /**
    * A collection of app inserted nodes. Gets populated in
    * {@link #makeNode(DefaultMutableTreeNode, String, Vector)}.
    * Currently empty, since app UDFs do not have categories
    */
   private Vector<DefaultMutableTreeNode> m_udfAppNodes = 
      new Vector<DefaultMutableTreeNode>();

   /**
    * The global node, gets initialized in
    * {@link #makeNode(DefaultMutableTreeNode, String, Vector)}.
    */
   private JTree.DynamicUtilTreeNode m_globalNode = null;

   /**
    * The app node, gets initialized in
    * {@link #makeNode(DefaultMutableTreeNode, String, Vector)}.
    */
   private JTree.DynamicUtilTreeNode m_appNode = null;

   static final int ATTRIBUTE_ICON = 2000;
}



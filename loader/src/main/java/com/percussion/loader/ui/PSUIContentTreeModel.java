/*[ PSUIContentTreeModel.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.IPSContentTree;
import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.PSConfigurationException;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSFileSearchRoot;
import com.percussion.loader.objectstore.PSListSelectorDef;
import com.percussion.loader.objectstore.PSProperty;
import com.percussion.loader.objectstore.PSSearchRoot;
import com.percussion.loader.selector.PSStructTreeModel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * A class used as the data model for <code>PSContentTreePanel</code>
 * tree. This class encasulates a PSContentTreeModel, the data tree returned,
 * by a content selection.
 */
public class PSUIContentTreeModel extends DefaultTreeModel
{
   /**
    * see base class for description. This constructor
    * allows the model to be created with just a string
    * description (e.g. a root url or name). In our selection case,
    * we allow the model to be created with just a descriptive
    * String without any other content.
    *
    * @param def the objectstore defintion of the selection that
    *    is desired. Never <code>null</code>.
    *
    * @param mode a IPSContentTree the data model returned via a content
    *    selection. May be <code>null</code>. If not <code>null</code> an
    *    attempt to "structuralize" the data will take place.
    *
    * @throws IllegalArgumentException if <code>def</code> is
    *    <code>null</code>.
    *
    * @throws PSConfigurationException if <code>m_model</code> cannot be
    *    constructed properly. See <code>PSStructTreeModel</code> for details
    */
   public PSUIContentTreeModel(PSContentSelectorDef def, IPSContentTree model)
      throws PSConfigurationException
   {
      super(new DefaultMutableTreeNode(PSContentTreePanel.ROOTNODE_STRING));

      if (def == null)
         throw new IllegalArgumentException("def must not be null");

      ArrayList l = new ArrayList();
      Iterator  iter = def.getSearchRoots();
      if (iter.hasNext())
      {
         while (iter.hasNext())
         {
            PSSearchRoot psRoot = (PSSearchRoot) iter.next();
            String strVal = psRoot.getProperty(
                  PSFileSearchRoot.XML_SEARCHROOT_NAME).getValue();

            l.add(strVal);
         }
         init(l.iterator(), model);
      }
      else  // If there are no search roots at this point,
      {     // then it may be the tree derived from a list import.
         try
         {
            // Attempt to parse configuration for a list importing
            // which has no search roots but the path to the xml file.
            PSProperty psProp = PSLoaderUtils.getProperty(
               PSListSelectorDef.CONTENT_LIST, def.getProperties());

            Iterator roots = PSLoaderUtils.getListRoots(psProp.getValue());
            init(roots, model);
         }
         catch (PSLoaderException e)
         {
            throw new PSConfigurationException
               (IPSLoaderErrors.UNEXPECTED_ERROR,
               new Object []
               {
                  e.getMessage()
               });
         }
      }
   }

   /**
    * Initializes this data model based on a String of url's as
    * root urls and a IPSContentTree of data, assumed to be a
    * dependency structure of a scanned data source.
    *
    * @param roots A iterator of String. Never <code>null</code>.
    *
    * @param model A IPSContentTree model of data to import from
    *    dependency to structural data. May be <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameters are invalid.
    *
    * @throws PSConfigurationException if any roots are invalid or if
    *    there is less than one root specified.
    */
   private void init(Iterator roots, IPSContentTree model)
      throws PSConfigurationException
   {
      if (root == null)
         throw new IllegalArgumentException(
            "roots must not be null");

      try
      {
         PSStructTreeModel sModel = new PSStructTreeModel(roots);
         // If a model to import has been supplied
         // load it.
         if (model != null)
            sModel.importDependencyModel(model);

         Iterator rootNodes = sModel.getRoots();
         while (rootNodes.hasNext())
         {
            IPSContentTreeNode root = (IPSContentTreeNode) rootNodes.next();

            String resId = root.getItemContext().getResourceId();
            DefaultMutableTreeNode aServerNode =
               new DefaultMutableTreeNode(sModel.getServer(root));

            loadModel(aServerNode, root);
         }
      }
      catch (Exception e)
      {
         throw new PSConfigurationException
            (IPSLoaderErrors.UNEXPECTED_ERROR,
            new Object []
            {
               e.getMessage()
            });
      }
   }

   /**
    * Loads a model based on a node within that model.
    *
    * @param aServer a TreeNode to be the acting parent for
    *    all nodes loaded. Never <code>null</code>.
    *
    * @param aRoot IPSContentTreeNode node to load and all its
    *    children will be loaded. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>aServer</code> if
    *    <code>null</code> or <code>aRoot</code> is <code>null</code>.
    */
   private void loadModel(DefaultMutableTreeNode aServer,
      IPSContentTreeNode aRoot)
   {
      if (aServer == null || aRoot == null)
         throw new IllegalArgumentException(
            "aServer and aRoot both must not be null");

      DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
      loadPSNode(aRoot, aServer);
      root.add(aServer);
   }

   /**
    * Recursively loads a IPSContentTreeNode and all its children, loading
    * them into this objects data model as DefaultMutableTreeNode with user
    * objects as their IPSContentTreeNode counterparts.
    *
    * @param node IPSContentTreeNode Never <code>null</code>.
    *
    * @param aParent DefaultMutableTreeNode to be the parent in this
    *    relationship. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>node</code> or
    *    <code>aParent</code> is <code>null</code>.
    */
   private void loadPSNode(IPSContentTreeNode node,
      DefaultMutableTreeNode aParent)
   {
      if (node == null || aParent == null)
         throw new IllegalArgumentException(
            "node and aParent both must not be null");

      PSItemContext itemC = node.getItemContext();
      DefaultMutableTreeNode n = new DefaultMutableTreeNode(node);
      aParent.add(n);

      // Remember the nodes for quick lookups later
      if (itemC != null)
      {
         m_data.put(itemC.getResourceId(), n);
      }

      if (!node.hasChildren())
         return;

      // grouping children in 2 list, a list of nodes that contains children,
      // a list of leaf nodes (which does not contains child node).
      // Each list is sorted in alphabetical order.
      Iterator children = node.getChildren();
      ArrayList hasChildList = new ArrayList();
      ArrayList noChildList = new ArrayList();
      IPSContentTreeNode childNode = null;
      while (children.hasNext())
      {
         childNode = (IPSContentTreeNode) children.next();
         if (childNode.hasChildren())
            insertChildNode(hasChildList, childNode);
         else
            insertChildNode(noChildList, childNode);
      }

      // Recursively load children, do the leaf nodes later if there is any
      for (int i=0; i < hasChildList.size(); i++)
         loadPSNode((IPSContentTreeNode) hasChildList.get(i), n);

      for (int i=0; i < noChildList.size(); i++)
         loadPSNode((IPSContentTreeNode) noChildList.get(i), n);
   }

   /**
    * Insert a node into a list in alphabetical order.
    *
    * @param childList The list contains nodes in alphabetical order. Assume
    *    it is not <code>null</code>.
    *
    * @param newNode The to be inserted node, assume not <code>null</code>.
    */
   private void insertChildNode(ArrayList childList, IPSContentTreeNode newNode)
   {
      String newURL = newNode.getItemContext().getResourceId();
      IPSContentTreeNode node = null;
      String currURL = null;
      boolean inserted = false;
      for (int i=0; (! inserted) && (i < childList.size()); i++)
      {
         node = (IPSContentTreeNode) childList.get(i);
         currURL = node.getItemContext().getResourceId();
         if (currURL.compareToIgnoreCase(newURL) > 0)   // currURL > newNode
         {
            inserted = true;
            childList.add(i, newNode);
         }
      }
      if (! inserted)
         childList.add(newNode);
   }

   /**
    * Retreives a tree node contained by this model that encapsulates
    * <code>item</code>. A match is determined by the item's resource id.
    *
    * @param item PSItemContext to match. Never <code>null</code>.
    *
    * @return DefaultMutableTreeNode from the tree. <code>null</code> if
    *    one could not be found.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public DefaultMutableTreeNode getTreeNodeFromItem(PSItemContext item)
   {
      if (item == null)
         throw new IllegalArgumentException(
            "item must not be null");

      String strKey = item.getResourceId();

      DefaultMutableTreeNode node =
         (DefaultMutableTreeNode) m_data.get(strKey);

      if (node != null)
         return node;

      strKey += "/";
      return (DefaultMutableTreeNode) m_data.get(strKey);
   }

   /**
    * Notify's TreeModelListeners of a node change.
    *
    * @param n IPSContentTreeNode that has changed. Never <code>null</code>.
    *
    * @param p TreePath of node. Never <code>null</code>.
    */
   public void onSendStructureEvent(IPSContentTreeNode n, TreePath p)
   {
      if (n == null || p == null)
         throw new IllegalArgumentException("n and p must not be null");

      fireTreeStructureChanged(n, p.getPath(),null,null);
   }

   /**
    * Transient hash of (resourceid, node) used for fast lookup
    * when attempting to find a node for a given item context.
    * Never <code>null</code>, loaded when data model is created.
    */
   private transient Hashtable m_data = new Hashtable();
}
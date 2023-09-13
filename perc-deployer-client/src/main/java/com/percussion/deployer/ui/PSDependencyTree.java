/******************************************************************************
 *
 * [ PSDependencyTree.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.IPSDependencyTreeCtxListener;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyContext;
import com.percussion.deployer.objectstore.PSDependencyTreeContext;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSDeployableObject;
import com.percussion.guitools.PSLabel;
import com.percussion.guitools.PSResources;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * This class to represent the deployable element/package with its dependencies 
 * and ancestors in a tree format. Uses check-box to render a dependency node 
 * that can be included (shared, user-defined dependencies and ancestors) with 
 * the package. Uses {@link DefaultTreeModel DefaultTreeModel}
 * as model of this tree and supports dynamic loading of children. 
 */
public class PSDependencyTree extends JTree 
   implements IPSDependencyTreeCtxListener
{
   /**
    * Constructs the tree with the supplied deployable element.
    * 
    * @param element the root element of the tree, may not be <code>null</code>
    * @param isEdit if <code>true</code> the tree shows the includable nodes
    * (check-box nodes) as enabled, otherwise as disabled.
    * @param treeContext The tree context to use to manage cross-package 
    * dependency selection, may not be <code>null</code> and must contain a
    * dependency context for the supplied element.
    * 
    * @throws IllegalArgumentException if element is <code>null</code>
    */
   public PSDependencyTree(PSDeployableElement element, boolean isEdit, 
      PSDependencyTreeContext treeContext) 
   {
      if(element == null)
         throw new IllegalArgumentException("element may not be null.");
      
      if (treeContext == null)
         throw new IllegalArgumentException("treeContext may not be null");
      
      m_rootElement = element;
      m_isEdit = isEdit;
      m_treeCtx = treeContext;
      
      getSelectionModel().setSelectionMode( 
         TreeSelectionModel.SINGLE_TREE_SELECTION );      
      
      // add to the tree context, but don't recurse as that will happen 
      // automatically as the tree is built.
      m_treeCtx.addPackage(element); 
      PSDependencyNode rootNode = new PSDependencyNode(element, 
         m_treeCtx.getDependencyCtx(element), m_isEdit);
      setModel( new DefaultTreeModel(rootNode) );      
      setCellRenderer(new DependencyCellRenderer());
      
      expandDependencies(rootNode);
      
      // must add after building the tree to avoid recursion issues
      m_treeCtx.addCtxChangeListener(this);
   }
   
   /**
    * The tree node used to represent a <code>PSDependency</code> object and 
    * supports dynamic loading of children.
    */
   private class PSDependencyNode extends DynamicUtilTreeNode
   {
      /**
       * Constructs the node with no children, but allows children. Allows 
       * dynamic loading of children.
       * 
       * @param dependency the user object of this node, may not be <code>null
       * </code>
       * @param ctx The context representing the supplied dependency across
       * all trees, used to manage cross-pacakge selection, may not be 
       * <code>null</code>.  
       * @param isEdit <code>true</code> if the node may be editable, 
       * <code>false</code> if we are in read-only mode.
       * 
       * @throws IllegalArgumentException if any dependency is <code>null</code>
       */   
      public PSDependencyNode(PSDependency dependency, PSDependencyContext ctx,
         boolean isEdit)
      {
         super(dependency, new Vector());
         
         if(dependency == null)
            throw new IllegalArgumentException("dependency may not be null.");
         if (ctx == null)
            throw new IllegalArgumentException("ctx may not be null");
         
         mi_ctx = ctx;
         mi_isEdit = isEdit;
      }
      
      /**
       * Gets the leaf child nodes (children, grand children ... that are leafs) 
       * of this node. If this node is leaf, this will be added to the list and
       * returned. 
       * 
       * @return the list of leaf child nodes, never <code>null</code> or empty.
       */
      public List getLeafChildren() 
      {
         DefaultMutableTreeNode node = this;
      
         List children = new ArrayList();
         getLeafChildren(node, children, false);
         
         return children;
      } 
      

      /**
       * Get the tree context for this node's dependency.
       * 
       * @return The context, never <code>null</code>.
       */
      public PSDependencyContext getDependencyCtx()
      {
         return mi_ctx;
      }
      
      /**
       * Determines if node is read-only or not.  
       * 
       * @return <code>true</code> if it is editable, <code>false</code> if
       * read-only.
       */
      public boolean isEdit()
      {
         return mi_isEdit;
      }
      
      /**
       * Recursive worker method for {@link #getLeafChildren()} to get the leaf
       * nodes of the supplied node.
       * 
       * @param node the root node to check, assumed not to be <code>null</code>
       * @param children the list of child leaf nodes that gets updated, assumed
       * not to be <code>null</code>
       * @param collapsed <code>true</code> if the state of the tree from the
       * supplied node downward should be collapsed, <code>false</code> 
       * otherwise.
       */
      private void getLeafChildren(DefaultMutableTreeNode node, List children, 
         boolean collapsed)
      {
         // A node is leaf if it does not have any children loaded. Treat node
         // with children as a leaf if it should not autoexpand
         boolean dontExpand = false;
         if (node instanceof PSDependencyNode)
         {
            PSDependency userObj = 
               (PSDependency)((PSDependencyNode)node).getUserObject();
            if (!userObj.shouldAutoExpand())
               dontExpand = true;
         }
         
         // expand all nodes with children even if not auto-expanding in order
         // to update the "multi" state throughout the archive.
         if(node.isLeaf() || node.getChildCount() == 0)
         {
            if (!collapsed)
               children.add(node);
         }
         else
         {
            for(Enumeration e = node.children(); e.hasMoreElements(); )
            {
               getLeafChildren(
                  (DefaultMutableTreeNode)e.nextElement(), children, 
                  collapsed || dontExpand);
            }
            
            // if not auto-expanding add as leaf node
            if (dontExpand && !collapsed)
            {
               children.add(node);
            }
         }
      } 
      
      /**
       * Loads the children if the children are already not loaded. Gets the 
       * dependencies and ancestors of the dependency represented by this node
       * and adds them as child nodes. If the dependency represented by this 
       * node is a deployable element, but not root element of the tree it does
       * not load children because each deployable element should be expanded in
       * its own tree.
       */
      protected void loadChildren()
      {      
         if(loadedChildren)
            return;         
   
         PSDependency dep = (PSDependency)getUserObject();
         if( dep instanceof PSDeployableElement && isRoot() ||
            dep instanceof PSDeployableObject)
         {
            loadedChildren = loadDependenciesAndAncestors(dep);
         }
      }  
      
      /**
       * Creates nodes for dependencies and ancestors of the supplied dependency
       * if they are already loaded with them and adds them as children to this
       * node. All dependency nodes are grouped by their type. Does not add the
       * ancestor that is parent of this node as the ancestor of this 
       * dependency. If the tree is constructed in edit mode, it does not create
       * child nodes, unless supplied dependency loaded with its child 
       * dependencies.
       * 
       * @param dependency the dependency represented by this node, assumed not
       * to be <code>null</code> 
       * 
       * @return <code>true</code> if this node is loaded with children, 
       * otherwise <code>false</code>.
       */
      private boolean loadDependenciesAndAncestors(PSDependency dependency)
      {   
         Iterator dependencies = dependency.getDependencies();
         
         //If the tree is in edit mode and dependencies are not yet loaded, 
         //it is not yet ready to create child nodes, so return false.
         if(mi_isEdit && dependencies == null)
            return false;
         
         // set this now so we don't loop back into here as a result of a 
         // context listener event
         loadedChildren = true;
            
         int i=0;                        
         if(dependencies != null && dependencies.hasNext())
         {
            DefaultMutableTreeNode depNode = new DefaultMutableTreeNode(
               ms_dependencies, true);
            addDependencyNodes(
               depNode, dependency, PSDependency.TYPE_LOCAL, mi_isEdit);
            addDependencyNodes(
               depNode, dependency, PSDependency.TYPE_SHARED, mi_isEdit);
            addDependencyNodes(
               depNode, dependency, PSDependency.TYPE_SYSTEM, mi_isEdit);
            addDependencyNodes(
               depNode, dependency, PSDependency.TYPE_SERVER, mi_isEdit);                                    
            if(dependency.supportsUserDependencies())
            {
               addDependencyNodes(
                  depNode, dependency, PSDependency.TYPE_USER, mi_isEdit);
            }                                                
            insert(depNode, i++);
         }
         
         //In case of edit we want to show ancestors only when ancestors are
         //expanded
         DefaultMutableTreeNode ancesNode = 
            new DynamicUtilTreeNode(ms_ancestors, new Vector());
         if(m_isEdit) 
         {
            insert(ancesNode, i++);               
         }
         else
         {            
            PSDependency parent = getParentDependency(this);         
            addAncestorNodes(parent, dependency, ancesNode);
            if(ancesNode.getChildCount() > 0)
               insert(ancesNode, i++);
         }    
         
         return true;
      }
      
      /**
       * The current context for this dependency to use to manage state across
       * packages.  Never <code>null</code> or modified after construction.
       */
      private PSDependencyContext mi_ctx;    
      
      /**
       * Flag to indicate if in read only mode or not.  <code>true</code> if
       * node may be editable, <code>false</code> if in read-only mode.
       */
      private boolean mi_isEdit;    
   }   
   
   /**
    * Gets the child dependencies of the supplied type of the supplied 
    * dependency, creates a group node based on specified <code>depType
    * </code> and adds them as child nodes to the group node. Adds the group 
    * node as child node of the parent.
    * 
    * @param parent the parent of the dependency group, may not be 
    * <code>null</code>
    * @param dependency the dependency to get child dependencies, may not
    * to be <code>null</code>
    * @param depType the dependency type, must be one of the <code>
    * PSDependency.TYPE_XXX</code> values
    * @param isEdit <code>true</code> to make the nodes editable, 
    * <code>false</code> if not.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void addDependencyNodes(DefaultMutableTreeNode parent, 
      PSDependency dependency, int depType, boolean isEdit)
   {
      if(parent == null)
         throw new IllegalArgumentException("parent may not be null");
      if(dependency == null)
         throw new IllegalArgumentException("dependency may not be null");
      if(!PSDependency.validateType(depType))
         throw new IllegalArgumentException("depType is not a valid type");
         
      String depTypeName = "";
      switch(depType)
      {
         case PSDependency.TYPE_LOCAL:
            depTypeName=ms_local;
            break;
            
         case PSDependency.TYPE_SHARED:
            depTypeName=ms_shared;
            break;            
            
         case PSDependency.TYPE_SERVER:
            depTypeName=ms_server;
            break;
            
         case PSDependency.TYPE_SYSTEM:
            depTypeName=ms_system;
            break;       
            
         case PSDependency.TYPE_USER:
            depTypeName=ms_user;
            break;         
      }
      
      Iterator deps = dependency.getDependencies(depType);
      if(deps != null && deps.hasNext())
      {
         DefaultMutableTreeNode node = new DefaultMutableTreeNode(
            depTypeName, true);
         parent.add(node);
         while(deps.hasNext())
         {
            PSDependency dep = (PSDependency)deps.next();
            node.add(new PSDependencyNode(dep, m_treeCtx.addDependency(dep, 
               m_rootElement), isEdit));
         }  
      }
   }
   
   /**
    * Gets the ancestors of the supplied dependency and adds them as child nodes
    * of the supplied ancestor node. Does not add supplied parent dependency as
    * ancestor node.
    * 
    * @param parent the parent dependency in the tree for the supplied 
    * dependency, may be <code>null</code>
    * @param dependency the dependency to get the ancestors, may not be <code>
    * null</code>
    * @param ancestorNode the parent node for the ancestors, may not be <code>
    * null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void addAncestorNodes(PSDependency parent, PSDependency dependency, 
      DefaultMutableTreeNode ancestorNode)
   {
      if(dependency == null)
         throw new IllegalArgumentException("dependency may not be null.");
         
      if(ancestorNode == null)
         throw new IllegalArgumentException("ancestorNode may not be null.");
                  
      Iterator ancestors = dependency.getAncestors();
      if(ancestors != null && ancestors.hasNext())
      {
         while(ancestors.hasNext())
         {
            PSDependency ancestor = (PSDependency)ancestors.next();
            if(parent == null || !ancestor.getKey().equals(parent.getKey()))
            {
               ancestorNode.add(new PSDependencyNode(ancestor, 
                  m_treeCtx.addDependency(ancestor, m_rootElement), false));
            }
               
         }
      }                  
   }
   
   /**
    * Gets the parent dependency of the supplied dependency node. Traverses up  
    * the tree to find a node that represents a <code>PSDependency</code> object.
    * Should be called to eleminate the ancestor which is actually the parent
    * dependency of this node that is shown in the tree to avoid circular 
    * loops.
    * 
    * @param node the dependency node to find the parent dependency, may not
    * to be <code>null</code>
    * @return the parent dependency, may be <code>null</code> if it can not
    * find a dependency node in the parent path.
    * 
    * @throws IllegalArgumentException if node is <code>null</code>
    */
   public PSDependency getParentDependency(DefaultMutableTreeNode node)
   {
      if(node == null)
         throw new IllegalArgumentException("node may not be null.");
         
      PSDependency parent = null;
      while(parent == null && node.getParent() != null)
      {
         node = (DefaultMutableTreeNode)node.getParent();
         if(node.getUserObject() instanceof PSDependency)
            parent = (PSDependency)node.getUserObject();
      }
      
      return parent;
   }
   
   /**
    * Gets the parent dependency node of the supplied node. Traverses up  
    * the tree to find a node that represents a <code>PSDependency</code> object.
    * Should be called to eliminate the ancestor which is actually the parent
    * dependency of this node that is shown in the tree to avoid circular 
    * loops.
    * 
    * @param node the dependency node to find the parent dependency node, may 
    * not be <code>null</code>
    * 
    * @return the parent dependency node, may be <code>null</code> if it can not
    * find a dependency node in the parent path.
    * 
    * @throws IllegalArgumentException if node is <code>null</code>
    */
   public DefaultMutableTreeNode getParentDependencyNode(
      DefaultMutableTreeNode node)
   {
      if(node == null)
         throw new IllegalArgumentException("node may not be null.");
         
      DefaultMutableTreeNode parentNode = null;
      while(parentNode == null && node.getParent() != null)
      {
         node = (DefaultMutableTreeNode)node.getParent();
         if(node.getUserObject() instanceof PSDependency)
            parentNode = node;
      }
      
      return parentNode;
   }
   
   /**
    * Expands the dependencies of the supplied node.
    * 
    * @param node the node representing a dependency, may not be <code>null
    * </code>
    * 
    * @throws IllegalArgumentException if the supplied node is <code>null</code>
    * or does not represent a dependency.
    */
   public void expandDependencies(DefaultMutableTreeNode node)
   {
      if(node instanceof PSDependencyNode)
      {
         // expand to this node
         expandPath(new TreePath(node.getPath()) );

         // create all child nodes and expand all children that should display
         Iterator leafNodes = 
            ((PSDependencyNode)node).getLeafChildren().iterator();
         while(leafNodes.hasNext())
         {
            DefaultMutableTreeNode childNode = 
               (DefaultMutableTreeNode)leafNodes.next();            
            makeVisible( new TreePath(childNode.getPath()) );               
         }
      }
      else
         throw new IllegalArgumentException(
            "node may not be null and must represent a dependency");
   }
   
   /**
    * Gets the list of dependency nodes that match the supplied dependency key. 
    * The matching is based on case-sensitive comparison of the dependency key.
    * 
    * @param depKey The dependency key to check for, may not be 
    * <code>null</code> or empty.
    * 
    * @return the list of matching <code>DefaultMutableTreeNode</code>s, never
    * <code>null</code>, may be empty.
    */
   private List getMatchingDependencyNodes(String depKey)
   {
      if (depKey == null || depKey.trim().length() == 0)
         throw new IllegalArgumentException("depKey may not be null or empty");

         
      DefaultTreeModel model = (DefaultTreeModel)getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
      
      List matchingNodes = new ArrayList();
      getMatchingDependencyNodes(depKey, root, matchingNodes);      
      return matchingNodes;
   }

   
   /**
    * Recursive worker method for {@link 
    * #getMatchingDependencyNodes(String)}. 
    * 
    * @param depKey the dependency key to check for, assumed not to be 
    * <code>null</code> or empty.
    * @param root the root to search for, assumed not to be <code>null</code>.
    * @param matchingNodes the list of matching nodes that get updated, assumed 
    * not to be <code>null</code>.
    */
   private void getMatchingDependencyNodes(String depKey, 
      DefaultMutableTreeNode root, List matchingNodes)
   {
      int childCount = root.getChildCount();
      for(int i=0; i<childCount; i++)
      {
         DefaultMutableTreeNode child = 
            (DefaultMutableTreeNode)root.getChildAt(i);
         if(child instanceof PSDependencyNode)
         {
            PSDependency userObj = (PSDependency)
               ((PSDependencyNode)child).getUserObject();
            if(userObj.getKey().equals(depKey))
            {
               matchingNodes.add(child);
            }
         }
         getMatchingDependencyNodes(depKey, child, matchingNodes);
      }
   }
   
   /**
    * Determine if the supplied node is an editable dependency node.
    * 
    * @param node The node to check, may not be <code>null</code>.
    *  
    * @return <code>true</code> if it is editable, <code>false</code>
    * if not.
    */
   public boolean isEditableDependencyNode(DefaultMutableTreeNode node)   
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null");
      
      boolean isEditable = false;
      if (node instanceof PSDependencyNode)
      {
         isEditable = ((PSDependencyNode)node).isEdit();
      }
      
      return isEditable;
   }
   
   /**
    * Removes all user dependency nodes of the supplied node if they exist.
    * 
    * @param parent the parent dependency node, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if parent is <code>null</code>
    */
   public void removeUserDependencies(DefaultMutableTreeNode parent)
   {
      if(parent == null)
         throw new IllegalArgumentException("parent may not be null.");
         
      int count = parent.getChildCount();
      
      for (int i = 0; i < count; i++) 
      {
         DefaultMutableTreeNode child = 
            (DefaultMutableTreeNode)parent.getChildAt(i);
         if(child.getUserObject().toString().equals(ms_user))
         {
            if (child instanceof PSDependencyNode)
            {
               PSDependency dep = (PSDependency)child.getUserObject();
               m_treeCtx.removeDependency(dep, false);  // would never be local 
            }
             
            parent.remove(child);            
         }
      }
   }

   // see IPSDependencyTreeCtxListener interface
   public void ctxChanged(PSDependencyContext ctx)
   {
      // need to update any nodes affected.
      Iterator nodes = getMatchingDependencyNodes(ctx.getKey()).iterator();
      while (nodes.hasNext())
      {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodes.next();
         ((DefaultTreeModel)getModel()).nodeChanged(node);
      }      
   }   
   
   // see IPSDependencyTreeCtxListener interface
   public boolean listensForChanges(PSDeployableElement pkg)
   {
      return (pkg == m_rootElement);
   }
   
   /**
    * Get the root element supplied during construction.
    * 
    * @return The root element, never <code>null</code>.
    */
   public PSDeployableElement getRootElement()
   {
      return m_rootElement;
   }
   
   /**
    * The renderer used with this dependency tree to show a check-box for 
    * shared or user-defined dependency and ancestor nodes and a default 
    * renderer (label) for other nodes.
    */
   private class DependencyCellRenderer implements TreeCellRenderer
   {
      public Component getTreeCellRendererComponent(JTree tree, Object value,
         boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
      {  
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
         
         if (node.getUserObject() instanceof PSDependency && !node.isRoot())
         {
            return checkRenderer.getTreeCellRendererComponent(tree, value,
               sel, expanded, leaf, row, hasFocus);
         }
         else
         {
            return defaultRenderer.getTreeCellRendererComponent(tree, value,
               sel, expanded, leaf, row, hasFocus);        
         } 
      }    
         
      /**
       * The renderer to use with includable nodes, initialized to a renderer
       * object and never <code>null</code> or modified after that.
       */
      private CheckTreeNodeRenderer checkRenderer = new CheckTreeNodeRenderer();
      
      /**
       * The renderer to use with any nodes in the tree that are not includable
       * nodes, initialized to a renderer object and never <code>null</code> or
       * modified after that.
       */
      private DefaultTreeCellRenderer defaultRenderer = 
         new DefaultTreeCellRenderer();
    }
   
   /**
    * The renderer that shows a check-box for the tree node. The check-box is 
    * enabled if the tree is in edit mode, otherwise it is disabled. If the node
    * represents a dependency object, then it updates the check-box state as 
    * checked if the dependency is included, otherwise as unchecked.
    */ 
   private class CheckTreeNodeRenderer extends JPanel
      implements TreeCellRenderer
   {
      /**
       * Constructs a panel for rendering with a check-box and a label 
       * horizontally placed.
       */
      public CheckTreeNodeRenderer()
      {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         add(m_check);
         add(m_iconLabel);
         add(Box.createHorizontalStrut(5));
         add(m_label);
      }
      
      //implements interface method
      public Component getTreeCellRendererComponent(JTree tree, Object value,
               boolean isSelected, boolean expanded,
               boolean leaf, int row, boolean hasFocus)
      {
         CheckTreeNodeRenderer nodeRenderer = new CheckTreeNodeRenderer();
      
         PSDependencyNode  node = (PSDependencyNode)value;
         if(node.getUserObject() instanceof PSDependency)
         {
            PSDependency element = (PSDependency)node.getUserObject();
            PSDependencyContext ctx = node.getDependencyCtx();
            
            if(node.isEdit())
               nodeRenderer.setCheckEnabled(ctx.canBeSelected());
            else
               nodeRenderer.setCheckEnabled(false);
         
            nodeRenderer.setCheckSelected(element.isIncluded());
            nodeRenderer.setLabelText(element.toString());
                        
            if(isSelected)
            {                           
               nodeRenderer.setBackground(
                  UIManager.getColor("Tree.selectionBackground"));
               nodeRenderer.setForeground(
                  UIManager.getColor("Tree.selectionForeground"));            
            }
            else
            {  
               nodeRenderer.setBackground(
                  UIManager.getColor("Tree.textBackground"));
               nodeRenderer.setForeground(
                  UIManager.getColor("Tree.textForeground"));
            }
            
            // only show multi icons if whole tree is in edit mode
            if(ctx.isMulti() && m_isEdit)
            {
               nodeRenderer.setIcon(
                  PSDeploymentClient.getImageLoader().getImage(
                     PSDeploymentClient.getResources().getString(
                        "gif_showMulti")));
            }            
         }         
         return nodeRenderer;
      }
      
      /**
       * Sets the icon to display.
       * 
       * @param icon The icon, may be <code>null</code> to display no icon.
       */
      private void setIcon(ImageIcon icon)
      {
         m_iconLabel.setIcon(icon);
      }

      /**
       * Sets the specified foreground to this panel and to the components in 
       * the panel.
       * 
       * @param fg the foreground color to set, may not be <code>null</code>
       * 
       * @throws IllegalArgumentException if fg is <code>null</code>
       */
      public void setForeground(Color fg)
      {
         if(fg == null)
            throw new IllegalArgumentException("fg may not be null.");
            
         super.setForeground(fg);
         /* we have to check the components for not null because super's 
          * initialization invokes this method before this object is completely 
          * initialized.
          */
         if(m_label != null)  
            m_label.setForeground(fg);
         if(m_check != null)
            m_check.setForeground(fg);
      }

      /**
       * Sets the specified background to this panel and to the components in 
       * the panel.
       * 
       * @param bg the background color to set, may not be <code>null</code>
       * 
       * @throws IllegalArgumentException if bg is <code>null</code>
       */      
      public void setBackground(Color bg)
      {
         if(bg == null)
            throw new IllegalArgumentException("bg may not be null.");
            
         super.setBackground(bg);
         
         /* we have to check the components for not null because super's 
          * initialization invokes this method before this object is completely 
          * initialized.
          */
         if(m_check != null)
            m_check.setBackground(bg);
            
         //label inherits the container's background, so don't need to set.
      }
      
      /**
       * Sets enabled state of the renderer component's checkbox.
       * 
       * @param enabled <code>true</code> to enable the checkbox, 
       * <code>false</code> to disable it.
       */
      public void setCheckEnabled(boolean enabled)
      {
         m_check.setEnabled(enabled);
      }
      
      /**
       * Sets selected state of the renderer component's checkbox.
       * 
       * @param selected <code>true</code> to select the checkbox, 
       * <code>false</code> to deselect it.
       */
      public void setCheckSelected(boolean selected)
      {
         m_check.setSelected(selected);
      }
      
      /**
       * Set the text of the renderer component's label
       * 
       * @param text The text, may not be <code>null</code> or empty. 
       */      
      public void setLabelText(String text)
      {
         if (text == null || text.trim().length() == 0)
            throw new IllegalArgumentException("text may not be null or empty");

         m_label.setText(text);
      }
      
      /**
       * The check-box to show for a dependency node, initialized to a check-box
       * without label and never <code>null</code> after that. The state or 
       * selection is set when the node renderer component was requested for a
       * particular node.
       */
      private JCheckBox m_check = new JCheckBox();
      
      /**
       * The label to show dependency node display name, initialized to an empty
       * label and never <code>null</code> after that. The text is set when the 
       * node renderer component was requested for a particular node.
       */
      private JLabel m_label = new PSLabel();
      
      /**
       * Label to use to display the image icon if one is provided.  Inialized
       * to an empty label, and never <code>null</code> or modified after that.
       * The icon may be when the node renderer component is requested for a 
       * particular node.
       */
      private JLabel m_iconLabel = new JLabel();
   }
   
   /**
    * The flag to indicate that this tree is created in 'Edit' mode or in 'View'
    * mode. <code>true</code> indicates edit mode whereas <code>false</code> 
    * indicates view mode. Initialized in the constructor and never modified 
    * after that.
    */
   private boolean m_isEdit;
   
   /**
    * The resource string to represent 'Dependencies' node.
    */
   public static final String ms_dependencies;
   
   /**
    * The resource string to represent 'Ancestors' node.
    */
   public static final String ms_ancestors;
   
   /**
    * The resource string to represent 'Local' dependencies node.
    */
   private static final String ms_local;
   
   /**
    * The resource string to represent 'Shared' dependencies node.
    */
   private static final String ms_shared;
   
   /**
    * The resource string to represent 'System' dependencies node.
    */
   private static final String ms_system;
   
   /**
    * The resource string to represent 'Server' dependencies node.
    */
   private static final String ms_server;
   
   /**
    * The resource string to represent 'User Defined' dependencies node.
    */
   private static final String ms_user;
   
   /**
    * Tree context used by this tree to manage cross-pacakge dependency 
    * selection.  Initialized during construction, never <code>null</code> or 
    * modified after that.
    */
   private PSDependencyTreeContext m_treeCtx = null;
   
   /**
    * The root element of the tree, supplied during construction, never 
    * <code>null</code> or modifie after that.
    */
   private PSDeployableElement m_rootElement;   
   
   //static block to intialize all resource strings
   static {
      PSResources res = PSDeploymentClient.getResources();   
      ms_dependencies = res.getString("dependencies");
      ms_ancestors = res.getString("ancestors");
      ms_local = res.getString("local");
      ms_shared = res.getString("shared");
      ms_system = res.getString("system");
      ms_server = res.getString("server");      
      ms_user = res.getString("user");                       
   }
}

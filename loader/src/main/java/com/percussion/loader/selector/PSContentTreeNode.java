/*[ PSContentTreeNode.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.PSItemContext;

import java.util.Iterator;
import java.util.Vector;

/**
 * Default implementation of an IPSContentTreeNode.
 * {@link #PSContentTreeModel} is a tree model of
 * these nodes.
 *
 * @see {@link #IPSContentTreeNode}
*/
public class PSContentTreeNode implements IPSContentTreeNode
{
   /**
    * Default constructor
    */
   public PSContentTreeNode()
   {
   }

   /**
    * Public Accessors
    */

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public boolean isRoot()
   {
      return (! getParents().hasNext());
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public IPSContentTreeNode getRoot()
   {
      /**
       * Recursive break out point if this
       * node is the root node.
       */
      if (isRoot())
         return this;

      /**
       * Walks the tree recursively to
       * find the root.
       */
      IPSContentTreeNode firstParent = (IPSContentTreeNode)getParents().next();
      return firstParent.getRoot();
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public void addChild(IPSContentTreeNode child)
   {
      if (child == null)
         throw new IllegalArgumentException(
            "Attempting to add a null child node.");


      /**
       * Add to the vector of children.
       */
      if (!m_vChildren.contains(child))
      {
         //Set the parent of this node to 'this' node
         // NOTE: parent and child may be the SAME node. This can happen when
         // inline link contains anchor link within the same page
         child.addParent(this);
         m_vChildren.addElement(child);
      }
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public IPSContentTreeNode remove(IPSContentTreeNode child)
   {
      int nIndex = -1;

      if (child == null)
         throw new IllegalArgumentException(
            "Attempting to remove a null child node.");

      nIndex = m_vChildren.indexOf(child);

      if (nIndex < 0)
         return null;

      /**
       * Remove parent from the child & remove the child from the child list
       */
      child.removeParent(this);
      m_vChildren.removeElementAt(nIndex);

      return child;
   }

   // see base class, IPSContentTreeNode, for descrption
   public void removeParent(IPSContentTreeNode parent)
   {
      int index = m_parents.indexOf(parent);
      if (index >= 0)
         m_parents.removeElementAt(index);
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public Iterator getChildren()
   {
      return m_vChildren.iterator();
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public boolean hasChildren()
   {
      return !m_vChildren.isEmpty();
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public Iterator getParents()
   {
      return m_parents.iterator();
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public void addParent(IPSContentTreeNode parent)
   {
      if (!m_parents.contains(parent))
      {
         m_parents.addElement(parent);
      }
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public void terminateBranch(IPSContentTreeNode existing)
   {
      /**
       * <code>true</code> if we have found this node elsewhere
       * in the tree. Prevents circularity.
      */
      m_bTerminated = true;
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public boolean isBranchTerminated()
   {
      return m_bTerminated;
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public IPSContentTreeNode getBranchTerminatingNode()
   {
      IPSContentTreeNode node = null;

      for (int i=0; i<m_vChildren.size(); i++)
      {
         node = (IPSContentTreeNode) m_vChildren.elementAt(i);

         /**
          * Break out routine for recusion
          */
         if (node.isBranchTerminated())
            return node;

         return node.getBranchTerminatingNode();
      }

      return null;
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public void exclude(boolean exclude)
   {
      m_bExclude = exclude;
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public boolean isExcluded()
   {
      return m_bExclude;
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public void setItemContext(PSItemContext item)
      throws IllegalArgumentException
   {
      /**
       * Cannot be null.
       */
      if (item == null)
         throw new IllegalArgumentException(
            "Item context node may not be null.");

      m_itemContext = item;
   }

   /**
    * see {@link com.percussion.loader.IPSContentTreeNode}
    * for description.
    */
   public PSItemContext getItemContext()
   {
      if (m_itemContext == null)
         throw new IllegalStateException(
            "Item context for this node is null.");

      return m_itemContext;
   }

   public String toString()
   {
      return getItemContext().getResourceId();
   }

   /**
    * Determines whether a given node is one of the parents.
    *
    * @param possibleParent The to be tested node, it may not be
    *    <code>null</code>
    *
    * @return <code>true</code> if the URL of one of the parents equals to
    *    the URL of the specified node; otherwise return <code>false</code>.
    */
   boolean isParent(PSContentTreeNode possibleParent)
   {
      if (possibleParent == null)
         throw new IllegalArgumentException("possibleParent may not be null");

      PSItemContext ppItem = possibleParent.getItemContext();
      Iterator parents = m_parents.iterator();
      while (parents.hasNext())
      {
         PSContentTreeNode parentNode = (PSContentTreeNode) parents.next();
         PSItemContext pItem = parentNode.getItemContext();

         if (pItem.getResourceId().equals(ppItem.getResourceId()))
            return true;
      }
      return false;
   }

   /**
    * Private Attributes
    */

   /**
    * See {@link #PSItemContext} for general class description.
    * This object represents the application specific content accessible
    * through this node.
    */
   private PSItemContext m_itemContext = null;

   /**
    * If this node is <code>true</code> the node will be
    * excluded. A value of <code>true</code> will include
    * the node. Default value is set to <code>false</code>.
    */
   private boolean m_bExclude = false;

   /**
    * Vector of children nodes of this element
    */
   private Vector m_vChildren = new Vector();

   /**
    * Terminates a tree branch. This is done if <code>this</code>
    * node is already part of another branch.
    */
   private boolean m_bTerminated = false;

   /**
    * Parent node
    */
   private Vector m_parents = new Vector();
}

/*[ IPSContentTree.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.util.Iterator;
import java.util.List;

/**
 * This interface specifies the data model used for the content item tree.
 */
public interface IPSContentTree extends java.io.Serializable
{
   /**
    * Get the child with the supplied index in the provided parent.
    *
    * @param parent the parent node from which to get the child, not
    *    <code>null</code>.
    * @param index the index of the child node to get from the supplied parent,
    *    must be >= 0.
    * @return the child node from teh supplied parent at the specified index,
    *    <code>null</code> if not found.
    */
   public IPSContentTreeNode getChild(IPSContentTreeNode parent, int index);

   /**
    * Get the number of children from the supplied parent node.
    *
    * @param parent the parent node to get the number of children for, not
    *    <code>null</code>.
    * @return the number of children found in the supplied parent, always >= 0.
    */
   public int getChildCount(IPSContentTreeNode parent);

   /**
    * Lookup the index for the supplied child node within the provided parent.
    *
    * @param parent teh parent node to lookup the inde from, not
    *    <code>null</code>.
    * @param child the child node to lookup the index for, not <code>null</code>.
    * @return the index of the supplied child withi the provided parent, -1 if
    *    not found.
    */
   public int getIndexOfChild(IPSContentTreeNode parent,
      IPSContentTreeNode child);

   /**
    * Get the root nodes of this tree.
    *
    * @return an iterator of nodes of this tree,
    *    <code>null</code> if not set yet.
    */
   public Iterator getRoots();

   /**
    * Test if the supplied node is a leaf.
    *
    * @param node the node to test, not <code>null</code>.
    * @return <code>true</code> if the supplied node is a leaf,
    *    <code>false</code> otherwise.
    */
   public boolean isLeaf(IPSContentTreeNode node);

   /**
    * Get all nodes of this tree.
    *
    * @return A list of <code>IPSContentTreeNode</code> objects. It can never
    *    be <code>null</code>, but may be empty.
    */
   public List getNodes();
}

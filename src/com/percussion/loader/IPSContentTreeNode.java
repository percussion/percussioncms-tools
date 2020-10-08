/*[ IPSContentTreeNode.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.util.Iterator;

/**
 * The source scanner in the Content Loader will return a tree reflecting
 * the source (e.g. a web site or a file system). This interface specifies 
 * all functionality needed for a content tree node. 
 */
public interface IPSContentTreeNode extends java.io.Serializable 
{
   /**
    * Get the root node for this. If this node is the root, it will return
    * itself.
    * 
    * @return the root node, never <code>null</code>.
    */
   public IPSContentTreeNode getRoot();
   
   /**
    * Adds a new child to this node. The parent of the supplied child will be
    * reset to this node.
    * 
    * @param child the new child to add, not <code>null</code>.
    * @throws IllegalArgumentException if the supplied child is 
    *    <code>null</code>.
    */
   public void addChild(IPSContentTreeNode child);
   
   /**
    * Removes the supplied child from this node. The parent of the removed
    * node will be reset to <code>null</code>.
    * 
    * @param child the child that needs to be removed from this node, not
    *    <code>null</code>.
    * @return the removed child node or <code>null</code> if not found.
    */
   public IPSContentTreeNode remove(IPSContentTreeNode child);
   
   /**
    * Get all children of this node.
    * 
    * @return an iterator of <code>IPSContentTreeNode</code> objects, never
    *    <code>null</code>, might be empty.
    */
   public Iterator getChildren();
   
   /**
    * Does this node have children?
    * 
    * @return <code>true</code> if this node has children, <code>false</code>
    *    otherwise.
    */
   public boolean hasChildren();
   
   /**
    * Get the parents of this node.
    * 
    * @return an iterator of <code>IPSContentTreeNode</code> objects, never
    *    <code>null</code>, might be empty.
    */
   public Iterator getParents();
   
   /**
    * Removes a specified parent from current parent list.
    * 
    * @param parent The to be removed parent node, it may not be 
    *    <code>null</code>
    */
   public void removeParent(IPSContentTreeNode parent);
   
   /**
    * Determines if the current node is one of the roots.
    * 
    * @return <code>true</code> if the node does not have a parent; otherwise
    *    return <code>false</code>.
    */
   public boolean isRoot();
   
   /**
    * Add a parent to this node.
    * 
    * @param parent a new parent for this node, may not be <code>null</code>.
    */
   public void addParent(IPSContentTreeNode parent);
   
   /**
    * This method terminates a tree branch. This is done if a reference is found
    * to an object that is already part of another branch.
    * 
    * @param existing a reference to the existing reference found in another 
    *    branch. Set this to <code>null</code> to de-terminate the branch.
    */
   public void terminateBranch(IPSContentTreeNode existing);
   
   /**
    * Is this branch terminated?
    * 
    * @return <code>true</code> if terminated, <code>false</code> otherwise.
    */
   public boolean isBranchTerminated();
   
   /**
    * Get terminal node.
    * 
    * @return the branch terminating node, may be <code>null</code> if this 
    *    branch is not terminated.
    */
   public IPSContentTreeNode getBranchTerminatingNode();
   
   /**
    * Exclude or include this node from the upload process.
    * 
    * @param exclude <code>true</code> to exclude this node from the upload 
    *    process, <code>false</code> otherwise.
    */
   public void exclude(boolean exclude);
   
   /**
    * Is this node excluded from the upload process?
    * 
    * @return <code>true</code> if this node is excluded from the upload
    *    process, <code>false</code> otherwise.
    */
   public boolean isExcluded();
   
   /**
    * Sets the item context for this node.
    * 
    * @param item the item context, not <code>null</code>.
    */
   public void setItemContext(PSItemContext item);
   
   /**
    * Gets the item context of thsi node.
    * 
    * @return the item context, never <code>null</code>.
    * @throws IllegalStateException if this method is called before the {@link
    *    #setItemContext(PSItemContext)} method has been called. 
    */
   public PSItemContext getItemContext();
}

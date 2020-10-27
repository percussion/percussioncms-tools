/******************************************************************************
 *
 * [ IPSHierarchyNodeRef.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.models.IPSHierarchyManager;

import java.util.Collection;

/**
 * This class enforces a model of relationships between nodes. Each node can
 * have 0 or 1 parent and 0 or more children. A node has a name, an optional
 * type and a set of 0 or more properties. A node can be a leaf or container;
 * leaves cannot have children.
 * 
 * @author paulhoward
 * @version 6.0
 */
public interface IPSHierarchyNodeRef extends IPSReference
{
   /**
    * A flag that indicates whether this node can ever contain children.
    * 
    * @return <code>true</code> if this is a container (it does not mean that
    * the container contains children at this time), <code>false</code>
    * if this node can never contain children. If an attempt is made to add
    * children to such a node, an exception will be thrown.
    */
   public boolean isContainer();
   
   /**
    * Returns a path created by walking up the ancestor chain until there is no
    * parent. The node parts are separated by '/' and a leading slash is added.
    * 
    * @return The path will be of the form '/a/b/c', where the last part of the
    * path is equal to {@link #getName()}.
    */
   public String getPath();

   /**
    * 
    * @return The closest ancestor of this node, or <code>null</code> if this
    * is a root node.
    */
   public IPSHierarchyNodeRef getParent();

   /**
    * The manager set by the {@link #setManager(IPSHierarchyManager)}
    * method, or <code>null</code> if one was never set.
    * 
    * @return <code>null</code> until properly initialized, then never 
    * <code>null</code>. 
    */
   public IPSHierarchyManager getManager();
   
   /**
    * All node processing is delegated to the manager. Therefore, the manager
    * must be set before many methods will work. This is expected to be set
    * shortly after construction.
    * 
    * @param mgr Never <code>null</code>.
    */
   public void setManager(IPSHierarchyManager mgr);
   
   /**
    * Returns all of the descendents that have this node as their parent.
    * 
    * @return Never <code>null</code>, may be empty. If this is a leaf node,
    * then the list will always be empty.
    * 
    * @throws PSModelException If any problems communicating with server.
    */
   public Collection<IPSHierarchyNodeRef> getChildren()
      throws PSModelException;
}

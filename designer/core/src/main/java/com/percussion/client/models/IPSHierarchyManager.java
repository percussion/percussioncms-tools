/******************************************************************************
 *
 * [ IPSHierarchyManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.List;

/**
 * This interface provides a set of methods for managing a model represented as
 * a tree. The relationships between the nodes is managed. Each tree has an
 * associated name that can be obtained with the {@link #getTreeName()} method.
 * 
 * @version 1.0
 * @author Paul Howard
 */
public interface IPSHierarchyManager
{
   /**
    * Add new container nodes to the tree, attaching them to the supplied
    * parent. Container children are persisted immediately. Leaves are not
    * persisted.
    * {@link IPSCmsModel#save(com.percussion.client.IPSReference, boolean) save}
    * must be called to persist the newly added children.
    * 
    * @param targetParent The supplied children will be added to this node.
    * Supply <code>null</code> to add to the root.
    * 
    * @param type The sub-type to create. Never <code>null</code>. Must be
    * one of the types returned by {@link IPSCmsModel#getObjectTypes()}.
    * 
    * @param names The monikers for the newly created children. If
    * <code>null</code> or empty, returns immediately. The name must be unique
    * among all children of the parent, case-insensitive or a
    * {@link com.percussion.client.PSDuplicateNameException} will be returned
    * for that entry. The list is unmodified by this call.
    * 
    * @return Never <code>null</code>. Pass this node to the
    * {@link IPSCmsModel#load(com.percussion.client.IPSReference, boolean, 
    * boolean) load} method to retrieve the associated object and pass it to the
    * {@link IPSCmsModel#save(com.percussion.client.IPSReference, boolean) save}
    * method to persist it.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed in
    * the add operation, this exception will contain information about the
    * entire operation, including what was successful and what failed. A failure
    * could occur because the caller doesn't have sufficient privileges 
    * ({@link com.percussion.security.PSAuthorizationException})
    * or because of a duplicate name 
    * ({@link com.percussion.client.PSDuplicateNameException}).
    */
   public IPSHierarchyNodeRef[] createChildren(
         IPSHierarchyNodeRef targetParent, PSObjectType type, List<String> names)
         throws PSMultiOperationException, PSModelException;

   /**
    * Similar to
    * {@link #createChildren(IPSHierarchyNodeRef, PSObjectType, List)}, except
    * the new nodes are clones of existing nodes. All created objects are
    * persisted immediately.
    * 
    * @param targetParent This becomes the parent of all created children. May
    * be <code>null</code> to add to root. Not all models support adding
    * children to the root.
    * 
    * @param sources The objects to clone. Never <code>null</code> and no
    * <code>null</code> entries.
    * 
    * @param names Optional new names for the clones. If an entry is
    * <code>null</code> or empty, the original name is used, otherwise, the
    * new name is used. May be <code>null</code>, otherwise, its length must
    * equal the length of <code>sources</code>.
    * 
    * @return Handles to the newly created objects, never <code>null</code> or
    * empty.
    * 
    * @throws PSMultiOperationException If any problems communicating with the
    * server or creating the objects. Any successes are in the exception.
    */
   public IPSHierarchyNodeRef[] cloneChildren(
         IPSHierarchyNodeRef targetParent, IPSHierarchyNodeRef[] sources,
         String[] names)
      throws PSMultiOperationException, PSModelException;   
   
   /**
    * Remove the supplied children from the hierarchy. If the children do not
    * belong to this tree, an attempt is made to retrieve their tree and remove
    * them from it.
    * <p>
    * Changes are persisted immediately.
    * 
    * @param children The nodes to remove. If <code>null</code>, returns
    * immediately. Each entry must be non-<code>null</code>. If a node is
    * not known by this tree, an exception is returned for that node. Folders
    * are processed recursively. The nodes don't have to have the same parent.
    * <p>
    * This is a <code>List</code> so that ordering is known in case an
    * exception is thrown. The list is unmodified by this call.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed in
    * the remove operation, this exception will contain information about the
    * entire operation, including what was successful and what failed. A failure
    * could occur because some node is locked or because the caller doesn't have
    * sufficient privileges. Nodes that were successfully deleted will be
    * represented in the exception with <code>null</code> entries. Nodes that
    * are not succesfull will contain a collection of exceptions for each leaf
    * under that node that could not be deleted.
    */
   public void removeChildren(List<IPSHierarchyNodeRef> children)
         throws PSMultiOperationException, PSModelException;

   /**
    * Moves nodes from one parent to another, recursively. Changes are persisted
    * immediately.
    * 
    * @param sourceChildren The children to move. All nodes must have the same
    * parent. If <code>null</code>, returns immediately. All entries must be
    * non-<code>null</code>. The supplied children are modified to point to
    * their new parent. A file locked for editing cannot be moved.
    * <p>
    * This is a <code>List</code> so that ordering is known in case an
    * exception is thrown. The list is unmodified by this call.
    * 
    * @param targetParent The supplied children will be added to this node.
    * Supply <code>null</code> to add to the root. If this is the parent of
    * the children, returns immediately. If this node is a child of any node in
    * <code>sourceChildren</code>, an exception is thrown. This node can be
    * located in a different tree.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed in
    * the move, this exception will contain information about the entire
    * operation, including what was successful and what failed. Successful nodes
    * will have a <code>null</code> entry, failed nodes will have an
    * exception. You can retrieve the exception for each error condition. If the
    * file is locked, a {@link PSLockException} is used.
    * 
    * @throws PSModelException If all children don't have the same parent or the
    * target parent is a leaf or a descendent of a source child.
    */
   public void moveChildren(List<IPSHierarchyNodeRef> sourceChildren,
         IPSHierarchyNodeRef targetParent) throws PSMultiOperationException,
         PSModelException;

   /**
    * Convenience method that calls
    * {@link #getChildren(IPSHierarchyNodeRef, boolean) getChildren(parent,
    * <code>false</code>)}.
    */
   public Collection<IPSHierarchyNodeRef> getChildren(IPSHierarchyNodeRef parent)
         throws PSModelException;

   /**
    * Catalogs objects in the tree and scans them looking for a matching id. If
    * one is found, it is returned. Otherwise, <code>null</code> is returned.
    * The scan should be performed in a breadth-first manner to minimize
    * cataloging calls.
    * 
    * @param id The id of the persisted object. Never <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating w/ server.
    */
   public IPSHierarchyNodeRef getReference(IPSGuid id) throws PSModelException;

   /**
    * Retrieves all children of the supplied node, optionally flushing the cache
    * and going to the server.
    * 
    * @param parent If <code>parent</code> is a leaf, an empty collection is
    * returned. If <code>null</code>, the roots of the tree fragment with
    * which this manager is associated will be returned. 
    * 
    * @param forceRefresh If <code>true</code>, the cache for the
    * <code>parent</code> node is flushed before performing the catalog.
    * 
    * @return Never <code>null</code>, may be empty.
    * 
    * @throws PSModelException If any problems communicating with server.
    */
   public Collection<IPSHierarchyNodeRef> getChildren(
         IPSHierarchyNodeRef parent, boolean forceRefresh)
         throws PSModelException;

   /**
    * Retrieves the single parent of this node, if there is one.
    * 
    * @param child Never <code>null</code>.
    * 
    * @return May be <code>null</code> if <code>child</code> is the root
    * node.
    */
   public IPSHierarchyNodeRef getParent(IPSHierarchyNodeRef child);

   /**
    * Convenience method that calls {@link IPSHierarchyNodeRef#getPath()
    * node.getPath()}.
    * 
    * @param node Never <code>null</code>.
    */
   public String getPath(IPSHierarchyNodeRef node);

   /**
    * Allows retrieval of a node if the entire path is known. A path is a node
    * identifier of the form /a/b/c, where a, b and c are node names as returned
    * by the <code>getName</code> method of <code>IPSReference</code>.
    * 
    * @param path A fully qualified path of the proper form. The path is
    * case-insensitive.
    * 
    * @return The node that is identified by the supplied path, or
    * <code>null</code> if the node cannot be found.
    * 
    * @throws PSModelException If any problems communicating with the server.
    */
   public IPSHierarchyNodeRef getByPath(String path) throws PSModelException;
   
   /**
    * The name of the tree that this manager represents.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getTreeName();
}

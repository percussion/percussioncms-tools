/******************************************************************************
 *
 * [ IPSHierarchyChangeListener.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.model;

import com.percussion.workbench.ui.PSUiReference;

/**
 * Classes interested in changes to the declarative hierarchy model can
 * implement this interface and register it with the
 * {@link com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor}
 * class to get notifications of changes.
 * 
 * @author paulhoward
 */
public interface IPSHierarchyChangeListener
{
   /**
    * These are the types of modifications, as reported by the
    * {@link IPSHierarchyChangeListener#changeOccurred(HierarchyChangeType, 
    * PSUiReference[], PSUiReference[]) changeOccurred} method.
    * 
    * @author paulhoward
    */
   public enum HierarchyChangeType
   {
      NODE_CREATED,
      /**
       * The path reported by the node included w/ this message will not be the
       * original path if this message is part of a move operation that results
       * in a NODE_DELETED followed by a NODE_CREATED message.
       */
      NODE_DELETED,
      NODE_MODIFIED,
      NODE_CHILDREN_MODIFIED,
      
      /**
       * A node was moved between 2 different folders, from a folder to no 
       * folder or from no folder to a folder.
       */
      NODE_MOVED,
      
      /**
       * This message is sent after the model receives a message that the server
       * has been disconnected. The receiver should clear all nodes and requery
       * the root node to get a 'placeholder' node that indicates the system is
       * disconnected. When it reconnects, a {@link #MODEL_ACTIVATED}
       * notification will be sent and the listener should remove all nodes and
       * requery the root again.
       * 
       * @see #MODEL_ACTIVATED
       */
      MODEL_DEACTIVATED,
      
      /**
       * This message is sent when the model receives a message that it has
       * connected to a server.
       * 
       * @see #MODEL_DEACTIVATED
       */
      MODEL_ACTIVATED
   }
   
   /**
    * Notifies the listener of a change in the model. The type of change and who
    * was change is included in the parameters.
    * 
    * @param type Never <code>null</code>.
    * 
    * @param nodes The nodes that were modified in some way. If the
    * <code>type</code> was <code>NODE_DELETED</code>, the entries should
    * not be used except for cleanup. The order only matters for create events
    * where the parent of the nodes doesn't have a sort order defined. If the
    * type is one of the MODEL_xxx values, this will be <code>null</code>,
    * otherwise it is never <code>null</code> or empty.
    * 
    * @param sourceParents The original parent of each node, <code>null</code>
    * if there was no parent. The positions correlate with the
    * <code>nodes</code> array. Always <code>null</code> unless the
    * <code>type</code> is {@link HierarchyChangeType#NODE_MOVED}, in which
    * case it is never <code>null</code> or empty. An entry may be
    * <code>null</code>. If present, its length must equal the length of
    * <code>nodes</code>.
    */
   public void changeOccurred(HierarchyChangeType type, PSUiReference[] nodes,
         PSUiReference[] sourceParents);
}

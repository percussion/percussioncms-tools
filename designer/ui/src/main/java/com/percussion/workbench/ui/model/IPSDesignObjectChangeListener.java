/******************************************************************************
 *
 * [ IPSDesignObjectChangeListener.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.model;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.PSUiReference;

import java.util.List;

/**
 * This interface is provided so that nodes in a tree can be used to add newly
 * created core model objects. It makes it easy for the New wizard to add
 * created objects to the current view's tree.
 * 
 * @author paulhoward
 */
public interface IPSDesignObjectChangeListener
{
   /**
    * When a new object needs to be added to the tree. Typically, this happens
    * when the New wizard creates one on a node that accepts the object type
    * being created.
    * 
    * @param parent The node to which the newly created node will be added. If
    * this node does not accept nodes of the type created, or <code>null</code> 
    * is supplied, the new child will be added to its home node. If this node
    * does not currently exist in the tree, no children will be added and an
    * empty result will be returned.
    * 
    * @param children The new core model objects to add. Never <code>null</code>.
    * <code>null</code> entries not allowed. If empty, returns immediately.
    * 
    * @return The nodes that were created for the supplied children or
    * <code>null</code> if a problem occurs. The latter can happen when adding
    * children to user defined folders. In that case, 1 or more of the
    * associated placeholders failed during creation meaning that object will
    * end up appearing in the home node, not the desired sub-folder. The failure
    * will be logged.
    */
   public List<PSUiReference> addChildren(PSUiReference parent,
         IPSReference[] children);
}

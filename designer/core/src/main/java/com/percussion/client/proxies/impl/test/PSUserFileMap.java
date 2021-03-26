/******************************************************************************
 *
 * [ PSUserFileMap.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class that encapsulates the slot map that is used as slot repository
 * in the test slot proxy
 * {@link com.percussion.client.proxies.impl.test.PSSlotModelProxy}. This is
 * class is required mainly to get a better control on serialization and
 * deserialization using the class
 * {@link com.percussion.xml.serialization.PSObjectSerializer} and has no other
 * use.
 * 
 */
public class PSUserFileMap extends PSRepositoryMap
{
   /**
    * Add a hierarchy node
    * @param obj
    */
   public void addHierarchyNode(PSHierarchyNode obj)
   {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj may not be null");
      }
      put(getReference(obj), obj);
   }
   
   /**
    * Get templates
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<PSHierarchyNode> getHierarchyNodes()
   {
      return m_values;
   }      

   /**
    * Must be called after betwixt has finished adding objects.
    */
   synchronized void finish()
   {
      processNodes(m_temp, null);
      if (!m_temp.isEmpty())
      {
         throw new IllegalStateException(
               "Mismatched set of nodes in repository.");
      }
      m_temp = null;
   }

   /**
    * Supplies the full path of the node. We assume that no tree has the 
    * exact same path as another tree (in this test code.)
    */
   protected String getQName(IPSReference ref)
   {
      return ref.getId().toString();
   }

   /**
    * Recursively walks the supplied list of nodes from the top down to build
    * the in-memory repository w/ properly linked nodes.
    * 
    * @param sourceNodes Assumed not <code>null</code>.
    * 
    * @param parent Should be <code>null</code> the first time.
    */
   private void processNodes(List<PSHierarchyNode> sourceNodes,
         PSHierarchyNodeRef parent)
   {
      List<PSHierarchyNode> children = getChildren(m_temp, parent);
      for (PSHierarchyNode child : children)
      {
         PSHierarchyNodeRef handle = (PSHierarchyNodeRef) getReference(child);
         handle.setParent(parent);
         put(handle, child);
         if (child.getType() == PSHierarchyNode.NodeType.FOLDER)
            processNodes(m_temp, handle);
      }
   }
   
   /**
    * Scan <code>sourceNodes</code> and extract each one that matches the 
    * supplied parent id and return it in the result.
    * 
    * @param sourceNodes The set of nodes to search. Any node whose parent
    * matches the supplied parent id will be removed from this list and added to
    * the returned list. Assumed not <code>null</code>.
    * 
    * @param parent Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   private List<PSHierarchyNode> getChildren(List<PSHierarchyNode> sourceNodes,
         IPSHierarchyNodeRef parent)
   {
      IPSGuid parentId = parent == null ? null : parent.getId();
      List<PSHierarchyNode> children = new ArrayList<PSHierarchyNode>();
      for (Iterator<PSHierarchyNode> iter = sourceNodes.iterator(); iter
            .hasNext();)
      {
         PSHierarchyNode child = iter.next();
         IPSGuid childParentId = child.getParentId(); 
         if ((childParentId == null && parentId == null)
               || (childParentId != null && parentId != null && childParentId
                     .equals(parentId)))
         {
            children.add(child);
            iter.remove();
         }
      }
      return children;
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSRepositoryMap#getType()
    */
   @Override
   public PSObjectTypes getType()
   {
      return PSObjectTypes.USER_FILE;
   }
   
   /**
    * A temporary storage location while the nodes are being loaded. It is 
    * set to <code>null</code> in the {@link #finish()} method upon successful
    * completion.
    */
   private List<PSHierarchyNode> m_temp = new ArrayList<PSHierarchyNode>();
}

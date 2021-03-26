/******************************************************************************
 *
 * [ PSHierarchyNodeRefHiddenParent.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.PSModelException;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.services.ui.data.PSHierarchyNode;

/**
 * This class extends {@link com.percussion.client.impl.PSHierarchyNodeRef} to
 * support hidden paraent ref which is required in proxy layer.
 */
@SuppressWarnings("serial")
public class PSHierarchyNodeRefHiddenParent extends PSHierarchyNodeRef
{
   /**
    * This ctor invokes the base class ctor with <code>null</code> as the
    * parent node ref and stores a reference to the first argument as hidden
    * parent node ref.
    * 
    * @param parent must not be <code>null</code>
    * @param data must not ve <code>null</code>.
    * @throws PSModelException
    */
   public PSHierarchyNodeRefHiddenParent(IPSHierarchyNodeRef parent,
      PSHierarchyNode data) throws PSModelException
   {
      super(null, data);
      m_hiddenParent = parent;
   }
   
   /**
    * This ctor invokes the base class ctor and stores a reference to the first
    * argument as hidden parent node ref.
    * 
    * @param parent must not be <code>null</code>
    * @param data must not ve <code>null</code>.
    */
   public PSHierarchyNodeRefHiddenParent(IPSHierarchyNodeRef parent,
      IPSHierarchyNodeRef data)
   {
      super(data);
      m_hiddenParent = parent;
   }

   /**
    * Get the hidden parent noderef supplied while construction.
    * 
    * @return never <code>null</code>.
    */
   public IPSHierarchyNodeRef getHiddenParent()
   {
      return m_hiddenParent;
   }

   /**
    * Refernce to the hidden parent node ref.
    * 
    * @see #getHiddenParent()
    */
   private IPSHierarchyNodeRef m_hiddenParent = null;

}

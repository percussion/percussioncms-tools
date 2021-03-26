/*******************************************************************************
 *
 * [ PSDatabaseTypeHierarchyModelProxy.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.DataBaseObjectSubTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.proxies.PSUninitializedConnectionException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Provides hierarchy management services for the object type
 * {@link PSObjectTypes#DB_TYPE} in test mode. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSDatabaseTypeHierarchyModelProxy
 * 
 * @version 6.0
 */
public class PSDatabaseTypeHierarchyModelProxy extends
   com.percussion.client.proxies.impl.PSDatabaseTypeHierarchyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#DB_TYPE}.
    * 
    * @throws PSUninitializedConnectionException
    */
   public PSDatabaseTypeHierarchyModelProxy()
      throws PSUninitializedConnectionException
   {
      super();
      m_flatProxy = new PSDatabaseTypeModelProxy();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.IPSHierarchyModelProxy#getChildren(com.percussion.client.proxies.IPSHierarchyModelProxy.NodeId)
    */
   public IPSHierarchyNodeRef[] getChildren(NodeId parent)
      throws PSModelException
   {
      Collection<IPSHierarchyNodeRef> result = new ArrayList<IPSHierarchyNodeRef>();
      Collection<IPSReference> refColl = null;
      if (parent.isNameBased())
      {
         if (parent.getTreeName().equals(ROOT_TREE_NAME))
         {
            refColl = m_flatProxy.catalog();
         }
      }
      else
      {
         refColl = m_flatProxy.catalog(parent.getNodeRef());
      }
      for (IPSReference ref : refColl)
      {
         result.add(new PSHierarchyNodeRef(
               parent.getNodeRef(), ref.getName(), ref.getObjectType(),
               ref.getId(), isContainer(ref)));
      }
      return result.toArray(new IPSHierarchyNodeRef[0]);
   }

   /**
    * Indicates whether provided reference is a container.
    */
   private boolean isContainer(IPSReference ref)
   {
      final Enum secondaryType = ref.getObjectType().getSecondaryType();
      return
            !secondaryType.equals(DataBaseObjectSubTypes.TABLE)
            && !secondaryType.equals(DataBaseObjectSubTypes.VIEW);
   }
}

/*******************************************************************************
 *
 * [ PSDatabaseTypeHierarchyModelProxy.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.DataBaseObjectSubTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides hierarchy management services for the object type
 * {@link PSObjectTypes#DB_TYPE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSHierarchyModelProxy
 * 
 * @version 6.0
 */
public class PSDatabaseTypeHierarchyModelProxy extends PSHierarchyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#DB_TYPE}.
    */
   public PSDatabaseTypeHierarchyModelProxy()
   {
      super(PSObjectTypes.DB_TYPE);
      m_flatProxy = new PSDatabaseTypeModelProxy();
   }

   // see interface
   @SuppressWarnings("unused")
   public IPSHierarchyNodeRef[] createChildrenFrom(@SuppressWarnings("unused")
   NodeId targetParent, IPSHierarchyNodeRef[] children,
      @SuppressWarnings("unused")
      Object[] results) throws PSMultiOperationException
   {
      throw new UnsupportedOperationException(
         "cloneChildren not supported by this object");
   }

   public IPSReference[] addChildren(@SuppressWarnings("unused")
   IPSReference targetParent, @SuppressWarnings("unused")
   Object[] children)
   {
      throw new UnsupportedOperationException(
         "addChildren not supported by this object");
   }

   public void removeChildren(@SuppressWarnings("unused")
   IPSReference targetParent, @SuppressWarnings(
   {
      "unused", "unused"
   })
   IPSReference[] children)
   {
      throw new UnsupportedOperationException(
         "addChildren not supported by this object");
   }

   public IPSReference[] moveChildren(@SuppressWarnings("unused")
   IPSReference sourceParent, @SuppressWarnings("unused")
   IPSReference[] sourceChildren, @SuppressWarnings("unused")
   IPSReference targetParent)
   {
      throw new UnsupportedOperationException(
         "addChildren not supported by this object");
   }

   public IPSReference[] getChildren(@SuppressWarnings("unused")
   IPSReference[] parent)
   {
      return new IPSReference[0];
   }

   @SuppressWarnings("unused")
   public IPSHierarchyNodeRef[] createChildren(NodeId targetParent,
         PSObjectType type, List<String> names, Object[] results)
   {
      throw new UnsupportedOperationException(
         "addChildren not supported by this object");
   }

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
         final PSHierarchyNodeRef href =
               new PSHierarchyNodeRef(parent.getNodeRef(), ref.getName(),
                     ref.getObjectType(), ref.getId(), isContainer(ref));
         href.setLabelKey(ref.getLabelKey());
         result.add(href);
      }
      return result.toArray(new IPSHierarchyNodeRef[0]);
   }

   /**
    * Indicates whether provided reference is a container.
    */
   private boolean isContainer(IPSReference ref)
   {
      final Enum secondaryType = ref.getObjectType().getSecondaryType();
      return !secondaryType.equals(DataBaseObjectSubTypes.TABLE)
         && !secondaryType.equals(DataBaseObjectSubTypes.VIEW);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.IPSHierarchyModelProxy#getRoots()
    */
   public Collection<String> getRoots()
   {
      Collection<String> c = new ArrayList<String>(1);
      c.add(ROOT_TREE_NAME);
      return c;
   }

   /**
    * Flat model proxy for cataloging
    */
   protected PSDatabaseTypeModelProxy m_flatProxy = null;

   static public final String ROOT_TREE_NAME = "DATA_SOURCES";
}

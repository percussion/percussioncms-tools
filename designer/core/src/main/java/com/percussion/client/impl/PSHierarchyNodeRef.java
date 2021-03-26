/******************************************************************************
 *
 * [ PSHierarchyNodeRef.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class enforces a model of relationships between nodes. Each node can
 * have 0 or 1 parent and 0 or more children. This class adds tracking of the
 * parent and child nodes to the base class. A node can be a leaf or container;
 * leaves cannot have children.
 * 
 * @author paulhoward
 * @version 6.0
 */
public class PSHierarchyNodeRef extends PSReference implements
      IPSHierarchyNodeRef
{
   /**
    * See {@link PSReference} for description of most of the params.
    * 
    * @param parent The closest ancestor of this node. May be <code>null</code>
    * if this is the root node.
    * 
    * @param isContainer Indicates that this node can have children.
    * 
    * @throws PSModelException If a model for <code>objectType</code> cannot
    * be found.
    */
   public PSHierarchyNodeRef(IPSHierarchyNodeRef parent, String name,
         PSObjectType objectType, IPSGuid id, boolean isContainer)
      throws PSModelException
   {
      super(name, null, null, objectType, id);
      m_isContainer = isContainer;
      m_parent = parent;
   }

   /**
    * This is provided so this class may be extended. Derived classes can use
    * this ctor to 'recreate' the object as the derived type.
    * 
    * @param node
    */
   protected PSHierarchyNodeRef(IPSHierarchyNodeRef node)
   {
      super(node);
      m_isContainer = node.isContainer();
      m_parent = node.getParent();
   }

   public PSHierarchyNodeRef(IPSHierarchyNodeRef parent, PSHierarchyNode data)
         throws PSModelException 
   {
      this(parent, data.getName(), new PSObjectType(PSObjectTypes.USER_FILE,
            data.getType() == PSHierarchyNode.NodeType.FOLDER
                  ? PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER
                  : PSObjectTypes.UserFileSubTypes.PLACEHOLDER),
            data.getGUID(), data.getType() == PSHierarchyNode.NodeType.FOLDER);
   }

   /**
    * @inheritDoc Overridden to compare the paths rather than names of the 2
    * nodes if there is no id.
    * 
    * @return <code>true</code> if the 2 paths are equal, case-insensitive,
    * <code>false</code> otherwise.d
    */
   @Override
   public boolean referencesSameObject(IPSReference other)
   {
      if (other == null || !(other instanceof IPSHierarchyNodeRef))
         return false;
      
      IPSGuid id1 = getId();
      IPSGuid id2 = other.getId();
      
      if (id1 != null && id2 != null)
         return id1.equals(id2);
      
      return getPath().equalsIgnoreCase(((IPSHierarchyNodeRef)other).getPath());
   }

   /**
    * The name uniquely identifies this node among its siblings,
    * case-insensitive, but is not validated by this class.
    * 
    * @param name See {@link PSCoreUtils#isValidHierarchyName(String)} for
    * rules. If not valid, an {@link IllegalArgumentException} is thrown.
    */
   public void setName(String name)
   {
      if (!PSCoreUtils.isValidHierarchyName(name))
      {
         throw new IllegalArgumentException("name is not valid: '" + name + "'");  
      }
      super.setName(name.trim(), true);
   }
   
   //see interface
   public void setManager(IPSHierarchyManager mgr)
   {
      if ( null == mgr)
      {
         throw new IllegalArgumentException("mgr cannot be null");  
      }
      m_manager = mgr;
   }
   
   //see interface
   public IPSHierarchyManager getManager()
   {
      return m_manager;
   }
   
   //see interface
   public String getPath()
   {
      List<IPSHierarchyNodeRef> nodes = new ArrayList<IPSHierarchyNodeRef>();
      nodes.add(this);
      IPSHierarchyNodeRef node = this;
      while (true)
      {
         node = node.getParent();
         if (node == null)
            break;
         nodes.add(node);
      }
      StringBuffer path = new StringBuffer(200);
      Collections.reverse(nodes);
      for (IPSHierarchyNodeRef ref : nodes)
      {
         path.append("/");
         path.append(ref.getName());
      }
      return path.toString();
   }

   //see interface
   public IPSHierarchyNodeRef getParent()
   {
      return m_parent;
   }
   
   /**
    * @see #getParent()
    */
   public void setParent(IPSHierarchyNodeRef parent)
   {
      m_parent = parent;
   }

   //see interface
   public Collection<IPSHierarchyNodeRef> getChildren()
      throws PSModelException
   {
      if (m_manager == null)
      {
         throw new IllegalStateException("Manager must be set first.");
      }
      return m_manager.getChildren(this);
   }

   //see interface
   public boolean isContainer()
   {
      return m_isContainer;
   }

   @Override
   public boolean equals(Object o)
   {
      if (o == null)
         return false;
      if (!(o instanceof PSHierarchyNodeRef))
         return false;
      if (o == this)
         return true;

      PSHierarchyNodeRef rhs = (PSHierarchyNodeRef) o;
      
      return new EqualsBuilder()
            .appendSuper(super.equals(o))
            .append(m_parent, rhs.m_parent)
            .append(m_manager, rhs.m_manager)
            .append(m_isContainer, rhs.m_isContainer)
            .isEquals();
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(m_parent)
            .append(m_isContainer)
            .toHashCode();
   }

   /**
    * Flag that indicates whether this node can ever have children.
    */
   private final boolean m_isContainer;

   /**
    * Set by the {@link #setManager(IPSHierarchyManager)} method, then
    * never <code>null</code>. Used to manage node child relationships.
    */
   private IPSHierarchyManager m_manager;

   /**
    * The one and only parent of this node, if there is one. All nodes have a
    * parent except the root.
    */
   private IPSHierarchyNodeRef m_parent;

   /**
    * For serialization. 
    */
   private static final long serialVersionUID = 1L;
}

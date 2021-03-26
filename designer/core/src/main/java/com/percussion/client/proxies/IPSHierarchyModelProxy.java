/******************************************************************************
 *
 * [ IPSHierarchyModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * Proxy interface providing hierarchy management services for objects
 * represented as a relationship tree. This interface is a broker between the ui
 * model layer and the web services layer. The model layer should never need to
 * access anything from web services layer directly. All the services required
 * from hierarchy management are available in this interface. Understands the
 * languages of model layer as well as webservices layer.
 */
public interface IPSHierarchyModelProxy
{
   /**
    * A small class to carry either a name or an node ref.
    */
   public class NodeId
   {
      /**
       * Make this id based on a tree fragment name.
       * 
       * @param treeName Never <code>null</code> or empty.
       */
      public NodeId(String treeName)
      {
         if (StringUtils.isBlank(treeName))
         {
            throw new IllegalArgumentException(
                  "treeName may not be null or empty");
         }
         m_treeName = treeName;
         m_ref = null;
      }
      
      /**
       * Make this id based on an existing node.
       * 
       * @param ref Never <code>null</code>.
       */
      public NodeId(IPSHierarchyNodeRef ref)
      {
         if (null == ref)
         {
            throw new IllegalArgumentException("ref cannot be null");  
         }
         m_ref = ref;
         m_treeName = null;
      }
      
      /**
       * Use {@link #isNameBased()} to determine whether this will return a
       * non-<code>null</code> value. 
       * 
       * @return The name supplied in the ctor, or <code>null</code> if that
       * ctor was not used.
       */
      public String getTreeName()
      {
         return m_treeName;
      }
      
      /**
       * Use {@link #isNameBased()} to determine whether this will return a
       * non-<code>null</code> value. 
       * 
       * @return The ref supplied in the ctor, or <code>null</code> if that
       * ctor was not used.
       */
      public IPSHierarchyNodeRef getNodeRef()
      {
         return m_ref;
      }
      
      /**
       * Distinguishes what type of id this represents.
       * 
       * @return <code>true</code> if {@link #getTreeName()} returns a valid
       * name, <code>false</code> if {@link #getNodeRef()} returns 
       * non-<code>null</code>.
       */
      public boolean isNameBased()
      {
         return m_treeName != null;
      }

      /**
       * Either <code>null</code> or non-empty.
       */
      private final String m_treeName;
      
      /**
       * If <code>m_treeName</code> is <code>null</code>, this is never 
       * <code>null</code>, otherwise it is always <code>null</code>.
       */
      private final IPSHierarchyNodeRef m_ref;
   }
   
   /**
    * See {@link com.percussion.client.models.IPSHierarchyManager#
    * createChildren(IPSHierarchyNodeRef, PSObjectType, List)} for details. 
    *  
    * @param names Never <code>null</code> or empty.
    * 
    * @param results The generated data objects for each node reference are set
    * on this array at the corresponding index. If a particular reference
    * results in an exception, that result will be set to <code>null</code>.
    * Some nodes will return <code>null</code> because they don't have 
    * associated data.
    * 
    * @throws PSMultiOperationException If any problems creating any individual
    * node.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   public IPSHierarchyNodeRef[] createChildren(NodeId targetParent,
         PSObjectType type, List<String> names, Object[] results)
      throws PSMultiOperationException, PSModelException;
   
   /**
    * Similar to {@link #createChildren(NodeId, PSObjectType, List, Object[])},
    * except clones are made of existing objects. The processing is not
    * recursive. Processing stops at the first error. Folders are persisted
    * immediately, files are not.
    * 
    * @param targetParent The node to receive the cloned children. Never
    * <code>null</code>.
    * 
    * @param children Never <code>null</code> or empty and no
    * <code>null</code> entries. All children must be of the same object type.
    * 
    * @param names Optional new names for the clones. If an entry is
    * <code>null</code> or empty, the original name is used, otherwise, the
    * new name is used. May be <code>null</code>, otherwise, its length must
    * equal the length of <code>sources</code>.
    * 
    * @param results The created objects are returned here. <code>null</code> is
    * returned for folders.
    * 
    * @return The references for the top level created children. Never
    * <code>null</code> or empty.
    * 
    * @throws PSMultiOperationException If any problems creating any individual
    * node.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   public IPSHierarchyNodeRef[] createChildrenFrom(NodeId targetParent,
         IPSHierarchyNodeRef[] children, String[] names, Object[] results)
      throws PSMultiOperationException, PSModelException;

   /**
    * See {@link com.percussion.client.models.IPSHierarchyManager#
    * removeChildren(List)} for details.
    */
   public void removeChildren(IPSHierarchyNodeRef[] children)
      throws PSMultiOperationException, PSModelException;


   /**
    * See {@link com.percussion.client.models.IPSHierarchyManager#
    * moveChildren(List, IPSHierarchyNodeRef)} for details. 
    */
   public void moveChildren(IPSHierarchyNodeRef[] sourceChildren,
         NodeId targetParent)
      throws PSMultiOperationException, PSModelException;
   
   /**
    * See {@link com.percussion.client.models.IPSHierarchyManager#
    * getChildren(IPSHierarchyNodeRef)} for details. The only difference is that
    * <code>parent</code> cannot be <code>null</code>. 
    */
   public IPSHierarchyNodeRef[] getChildren(NodeId parent)
      throws PSModelException;

   /**
    * This method will return all of the tree names known to this manager. These
    * can then be used to get the root of a tree fragment using the
    * {@link #getChildren(NodeId)} method.
    * 
    * @return Never <code>null</code>, may be empty. An empty list does not
    * necessarily mean there are no roots, just that they can't be determined at
    * this time.
    * 
    * @throws PSModelException If any problems while communicating with the
    * server.
    */
   public Collection<String> getRoots() 
      throws PSModelException;
}

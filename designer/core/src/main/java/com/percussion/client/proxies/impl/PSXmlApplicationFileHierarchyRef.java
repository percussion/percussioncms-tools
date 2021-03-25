/******************************************************************************
 *
 * [ PSXmlApplicationFileHierarchyRef.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import static com.percussion.client.proxies.impl.PSResourceFileModelProxy.OBJECT_TYPE_FILE;
import static com.percussion.client.proxies.impl.PSResourceFileModelProxy.OBJECT_TYPE_FOLDER;

/**
 * Resource file hierarchy tree root. Stores tree-specific data.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationFileHierarchyRef extends PSHierarchyNodeRef
{
   private static final long serialVersionUID = 1L;

   public PSXmlApplicationFileHierarchyRef(IPSHierarchyNodeRef parent, String name,
         boolean isContainer) throws PSModelException
   {
      super(parent, name, isContainer ? OBJECT_TYPE_FOLDER : OBJECT_TYPE_FILE,
            null, isContainer);
   }

   public PSXmlApplicationFileHierarchyRef(IPSHierarchyNodeRef parent, String name,
      PSObjectType objType, boolean isContainer) throws PSModelException
   {
      super(parent, name, objType, null, isContainer);
   }

   /**
    * Convenience getter to eliminate casts.
    */
   private PSXmlApplicationFileHierarchyRef getParent2()
   {
      return (PSXmlApplicationFileHierarchyRef) getParent();
   }

   /**
    * Application corresponding to the tree.
    */
   public PSApplication getApplication()
   {
      return getParent2().getApplication();
   }

   /**
    * Returns file path of the file corresponding to the current node.
    * The file path is relative to application request folder.
    */
   public String getFilePath()
   {
      if (getParent() == null)
      {
         return ""; 
      }
      return StringUtils.isEmpty(getParent2().getFilePath()) ? getName()
      : getParent2().getFilePath() + "/" + getName();
   }
   
   /**
    * Returns <code>true</code> if the tree the node is in locked because at
    * least one node in the tree is locked.
    */
   public boolean isTreeLocked()
   {
      return getParent2().isTreeLocked();
   }

   /**
    * Returns <code>true</code> if this particular node is locked.
    */
   @Override
   public boolean isLocked()
   {
      return m_locked;
   }

   /**
    * Marks the tree as locked because of this node.
    * The tree will be locked as long as all the nodes are unlocked.
    * This is only for client-level tracking and does not affect actual locking
    * status on server. 
    */
   public void lock()
   {
      m_locked = true;
   }
   
   /**
    * Marks the node as unlocked.
    * The whole tree still can be locked if some other node is locked. 
    */
   public void unlock()
   {
      m_locked = false;
      setLock(this, false);
   }

   /**
    * Notifies the tree that lock status for the specified node is changed.
    * @param ref node status is changed for
    * @param locked lock status of the node
    */
   protected void setLock(PSXmlApplicationFileHierarchyRef ref, boolean locked)
   {
      getParent2().setLock(ref, locked);
   }

   /**
    * Throws {@link UnsupportedOperationException} because application files are
    * identified by application/file names, not by ids.
    */
   @Override
   public void setId(@SuppressWarnings("unused") IPSGuid id)
   {
      throw new UnsupportedOperationException(
            "Application files are identified by application/file names, n" +
            "ot by ids.");
   }
   /**
    * Set the hash of the page
    * @param hash
    */
   public void setHash(String hash) {
      this.hash = hash;
   }

   /**
    * Get the hash of the page
    * @return The hash of the page, may be null
    */
   public String getHash() {
      return this.hash;
   }

   /**
    * Indicates whether current node is locked.
    */
   private boolean m_locked;

   /**
    * hash of reference
    */
   private String hash = null;
}

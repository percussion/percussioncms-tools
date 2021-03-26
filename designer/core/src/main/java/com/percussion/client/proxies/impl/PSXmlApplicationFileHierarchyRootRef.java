/******************************************************************************
 *
 * [ PSXmlApplicationFileHierarchyRootRef.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashSet;
import java.util.Set;

/**
 * Resource file hierarchy tree root. Stores tree-specific data.
 * Read-only except hierarchy manager property.
 * Throws {@link UnsupportedOperationException} on any attempt
 * of modification after it is created.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationFileHierarchyRootRef extends PSXmlApplicationFileHierarchyRef
{
   private static final long serialVersionUID = 1L;

   public PSXmlApplicationFileHierarchyRootRef(final PSApplication application)
         throws PSModelException
   {
      super(null, application.getName(), true);
      setDescription(APPLICATION_FOLDER_DESCRIPTION);
      setReadOnly(true);
      setPersisted();
      m_application = application;
      isInitialized = true;
   }

   public PSXmlApplicationFileHierarchyRootRef(final PSApplication application,
      PSObjectType objType, boolean isContainer) throws PSModelException
   {
      super(null, application.getName(), objType, isContainer);
      setDescription(APPLICATION_FOLDER_DESCRIPTION);
      setReadOnly(true);
      setPersisted();
      m_application = application;
      isInitialized = false;
   }

   /**
    * Application corresponding to the tree.
    */
   @Override
   public PSApplication getApplication()
   {
      return m_application;
   }
   
   @Override
   public void setName(String name)
   {
      insureNotInitialized();
      super.setName(name);
   }

   /**
    * Throws {@link UnsupportedOperationException} if the object is already
    * initialized.
    */
   private void insureNotInitialized()
   {
      if (isInitialized)
      {
         throw new UnsupportedOperationException("The object is read-only after it is initialized");
      }
   }

   @Override
   public void setDescription(String desc)
   {
      insureNotInitialized();
      super.setDescription(desc);
   }

   @Override
   public void setId(IPSGuid id)
   {
      insureNotInitialized();
      super.setId(id);
   }

   @Override
   public void setLabelKey(String key)
   {
      insureNotInitialized();
      super.setLabelKey(key);
   }

   @Override
   public void setObjectType(PSObjectType type) throws PSModelException
   {
      insureNotInitialized();
      super.setObjectType(type);
   }

   @Override
   public void setPersisted()
   {
      insureNotInitialized();
      super.setPersisted();
   }

   @Override
   public void setReadOnly(boolean readOnly)
   {
      insureNotInitialized();
      super.setReadOnly(readOnly);
   }

   /**
    * Allows to set hierarchy manager only once.
    * 
    * @see PSHierarchyNodeRef#setManager(IPSHierarchyManager)
    */
   @Override
   public void setManager(IPSHierarchyManager mgr)
   {
      if (getManager() != null)
      {
         insureNotInitialized();
      }
      super.setManager(mgr);
   }

   @Override
   public boolean isTreeLocked()
   {
      return !m_lockedNodes.isEmpty();
   }

   @Override
   protected void setLock(PSXmlApplicationFileHierarchyRef ref, boolean locked)
   {
      if (locked)
      {
         m_lockedNodes.add(ref);
      }
      else
      {
         m_lockedNodes.remove(ref);
      }
   }

   /**
    * Set of locked nodes in the tree.
    */
   private final Set<PSXmlApplicationFileHierarchyRef> m_lockedNodes =
         new HashSet<PSXmlApplicationFileHierarchyRef>();
   
   /**
    * @see #getApplication()
    */
   final PSApplication m_application;

   /**
    * Description for the folder corresponding to an application.
    */
   private static final String APPLICATION_FOLDER_DESCRIPTION =
         "Virtual folder corresponding to an application";

   /**
    * Flag which is set to <code>true</code> in constructor after all
    * initialization is completed. 
    */
   private final boolean isInitialized;
}

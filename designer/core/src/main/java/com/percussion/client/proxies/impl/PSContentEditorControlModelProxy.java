/******************************************************************************
 *
 * [ PSContentEditorControlModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.proxies.IPSHierarchyModelProxy.NodeId;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#CONTENT_EDITOR_CONTROLS}. Uses base class
 * implementation whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSContentEditorControlModelProxy extends PSLegacyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#CONTENT_EDITOR_CONTROLS} and for primary type.
    */
   public PSContentEditorControlModelProxy() 
   {
      super(PSObjectTypes.CONTENT_EDITOR_CONTROLS);
      m_xmlHierarchyModelProxy = new PSXmlApplicationFileHierarchyModelProxy(
         PSObjectTypes.CONTENT_EDITOR_CONTROLS);
   }

   public Collection<IPSReference> catalog() throws PSModelException
   {
      try
      {
         PSApplication appSysResources = getObjectStore().getApplication(
            SYS_RESOURCES, false, false);
         PSObjectType objType = new PSObjectType(
            PSObjectTypes.CONTENT_EDITOR_CONTROLS,
            PSObjectTypes.ContentEditorControlSubTypes.SYSTEM);
         PSHierarchyNodeRef sysRoot = new PSXmlApplicationFileHierarchyRootRef(
            appSysResources, objType, false);
         IPSHierarchyNodeRef sysStylesheets;
         try
         {
            sysStylesheets = loadChildByName(sysRoot, "stylesheets");
         }
         catch (PSMultiOperationException e)
         {
            throw new PSModelException(e);
         }
         PSXmlApplicationFileHierarchyRef sysTemplates = new PSXmlApplicationFileHierarchyRef(
            sysStylesheets, SYS_TEMPLATES, objType, false);
         sysTemplates.setPersisted();
         
         PSApplication appRxResources = getObjectStore().getApplication(
            RX_RESOURCES, false, false);
         objType = new PSObjectType(PSObjectTypes.CONTENT_EDITOR_CONTROLS,
            PSObjectTypes.ContentEditorControlSubTypes.USER);
         PSHierarchyNodeRef rxRoot = new PSXmlApplicationFileHierarchyRootRef(
            appRxResources, objType, false);
         IPSHierarchyNodeRef rxStylesheets;
         try
         {
            rxStylesheets = loadChildByName(rxRoot, "stylesheets");
         }
         catch (PSMultiOperationException e)
         {
            throw new PSModelException(e);
         }
         PSXmlApplicationFileHierarchyRef rxTemplates = new PSXmlApplicationFileHierarchyRef(
            rxStylesheets, RX_TEMPLATES, objType, false);
         rxTemplates.setPersisted();

         Collection<IPSReference> refColl = new ArrayList<IPSReference>(2);
         refColl.add(sysTemplates);
         refColl.add(rxTemplates);

         return refColl;
      }
      catch (PSServerException e)
      {
         throw new PSModelException(e);
      }
      catch (PSAuthorizationException e)
      {
         throw new PSModelException(e);
      }
      catch (PSAuthenticationFailedException e)
      {
         throw new PSModelException(e);
      }
      catch (PSLockedException e)
      {
         throw new PSModelException(e);
      }
      catch (PSNotFoundException e)
      {
         throw new PSModelException(e);
      }
      catch (PSModelException e)
      {
         throw new PSModelException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#load(com.percussion.client.IPSReference[],
    * boolean, boolean)
    */
   public Object[] load(IPSReference[] reference, boolean lockForEdit,
      boolean overrideLock) throws PSMultiOperationException, PSModelException
   {
      if (reference == null || reference.length == 0)
      {
         throw new IllegalArgumentException(
            "references must not be null or empty");
      }
      return m_xmlHierarchyModelProxy.m_modelProxy.load(reference, lockForEdit,
         overrideLock);
   }

   /**
    * @param parent
    * @throws PSMultiOperationException
    */
   private IPSHierarchyNodeRef loadChildByName(IPSHierarchyNodeRef parent,
      String name) throws PSMultiOperationException
   {
      IPSHierarchyNodeRef stylesheets = null;
      try
      {
         IPSHierarchyNodeRef[] children = m_xmlHierarchyModelProxy
            .getChildren(new NodeId(parent));
         for (int i = 0; i < children.length; i++)
         {
            IPSHierarchyNodeRef ref = children[i];
            if (ref.getName().equals(name))
            {
               stylesheets = ref;
               break;
            }
         }
      }
      catch (PSModelException e)
      {
         throw new PSMultiOperationException(e, name);
      }
      return stylesheets;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#save(com.percussion.client.IPSReference[],
    * java.lang.Object[], boolean)
    */
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      m_xmlHierarchyModelProxy.m_modelProxy.save(refs, data, releaseLock);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#releaseLock(com.percussion.client.IPSReference[])
    */
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException, PSModelException
   {
      m_xmlHierarchyModelProxy.m_modelProxy.releaseLock(references);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#isLocked(com.percussion.client.IPSReference)
    */
   public boolean isLocked(IPSReference ref) throws PSModelException
   {
      return m_xmlHierarchyModelProxy.m_modelProxy.isLocked(ref);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#create(int,
    * java.util.List)
    */
   @SuppressWarnings("unused")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results)
   {
      throw new UnsupportedOperationException(
         "Create is not supported for this type of object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#create(java.lang.Object[],
    * java.util.List)
    */
   @SuppressWarnings("unused")
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List results)
   {
      throw new UnsupportedOperationException(
         "Create is not supported for this type of object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#delete(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   public void delete(IPSReference[] reference)
   {
      throw new UnsupportedOperationException(
         "Delete is not supported for this type of object");
   }

   /**
    * Operation not supported.
    * 
    * @throws UnsupportedOperationException Always.
    */
   @SuppressWarnings("unused")
   public void rename(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException(
         "Rename is not supported for this type of object");
   }

   private static final String SYS_RESOURCES = "sys_resources";

   private static final String RX_RESOURCES = "rx_resources";

   private static final String SYS_TEMPLATES = "sys_Templates.xsl";

   private static final String RX_TEMPLATES = "rx_Templates.xsl";

   private PSXmlApplicationFileHierarchyModelProxy m_xmlHierarchyModelProxy = null;
}

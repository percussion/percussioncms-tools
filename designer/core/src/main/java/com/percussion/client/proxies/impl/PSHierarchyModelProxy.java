/******************************************************************************
 *
 * [ PSHierarchyModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.proxies.IPSHierarchyModelProxy;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.uidesign.MoveChildrenRequest;
import com.percussion.webservices.uidesign.RemoveChildrenRequest;
import com.percussion.webservices.uidesign.UiDesignSOAPStub;
import org.apache.axis.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.ServiceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for classes implementing the interface
 * {@link com.percussion.client.proxies.IPSHierarchyModelProxy}. All object
 * model independent functionality is implemeneted in this class.
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public abstract class PSHierarchyModelProxy implements IPSHierarchyModelProxy
{
   public PSHierarchyModelProxy(IPSPrimaryObjectType primaryType) 
   {
      m_primaryType = primaryType;
   }

   /**
    * Default implementation just throws an unsupported op exception.
    */
   @SuppressWarnings("unused")
   public IPSHierarchyNodeRef[] createChildrenFrom(NodeId targetParent,
         IPSHierarchyNodeRef[] children, String[] names, Object[] results)
      throws PSMultiOperationException, PSModelException
   {
      throw new UnsupportedOperationException("cloning children not supported");
   }

   @SuppressWarnings("unchecked")
   public void removeChildren(IPSHierarchyNodeRef[] children)
      throws PSMultiOperationException, PSModelException
   {
      if (children == null || children.length == 0)
         return;
      Map<Long, List<IPSHierarchyNodeRef>> parentChildrenMap = 
         new HashMap<Long, List<IPSHierarchyNodeRef>>();
      for (IPSHierarchyNodeRef nodeRef : children)
      {
         IPSHierarchyNodeRef parentRef = nodeRef.getParent();
         if (parentRef == null)
         {
            parentRef = ((PSHierarchyNodeRefHiddenParent) nodeRef)
               .getHiddenParent();
         }
         if (parentRef == null)
         {
            throw new RuntimeException(
               "Both visible and hidden parent refs must not be null");
         }
         Long parentId = parentRef.getId().longValue();
         List localChidren = parentChildrenMap.get(parentId);
         if (localChidren == null)
         {
            localChidren = new ArrayList<IPSHierarchyNodeRef>();
            parentChildrenMap.put(parentId, localChidren);
         }
         localChidren.add(nodeRef);
      }
      Iterator parentIdIter = parentChildrenMap.keySet().iterator();
      while (parentIdIter.hasNext())
      {
         Long pId = (Long) parentIdIter.next();
         removeChildren(pId, parentChildrenMap.get(pId));
      }
   }

   /**
    * Helper method that removes the children of the supplied parent.
    * 
    * @param parent assumed not <code>null</code>
    * @param children assumed not <code>null</code> or empty
    * @throws PSMultiOperationException
    */
   @SuppressWarnings("unused")
   private void removeChildren(long parent, List<IPSHierarchyNodeRef> children)
      throws PSMultiOperationException, PSModelException
   {
      Throwable ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               Stub binding = getDesignBinding();
               RemoveChildrenRequest request = new RemoveChildrenRequest();
               request.setParentId(parent);
               long[] ids = new long[children.size()];
               for (int i = 0; i < children.size(); i++)
                  ids[i] = children.get(i).getId().longValue();
               request.setId(ids);
               Method removeChildren = binding.getClass().getMethod(
                  "removeChildren",
                  new Class[]
                  {
                     RemoveChildrenRequest.class
                  });
               removeChildren.invoke(binding, new Object[]
               {
                  request
               });
            }
            catch (InvocationTargetException e)
            {
               try
               {
                  if (e.getTargetException() instanceof PSInvalidSessionFault)
                  {
                     PSCoreFactory.getInstance().reconnect();
                     redo = true;
                  }
                  else
                     ex = e.getTargetException();
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (NoSuchMethodException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (IllegalAccessException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      
      if (ex != null)
         PSProxyUtils.processAndThrowException(1, ex, ms_log);
   }

   @SuppressWarnings("unchecked")
   public void moveChildren(IPSHierarchyNodeRef[] sourceChildren, NodeId target)
      throws PSMultiOperationException, PSModelException
   {
      if (sourceChildren == null || sourceChildren.length == 0)
         return;
      Map<Long, List<IPSHierarchyNodeRef>> parentChildrenMap = 
         new HashMap<Long, List<IPSHierarchyNodeRef>>();
      for (IPSHierarchyNodeRef nodeRef : sourceChildren)
      {
         IPSHierarchyNodeRef parentRef =null;
         if (nodeRef instanceof PSHierarchyNodeRefHiddenParent)
         {
            parentRef = ((PSHierarchyNodeRefHiddenParent) nodeRef)
               .getHiddenParent();
         }
         else
         {
            parentRef = nodeRef.getParent();
         }
         if (parentRef == null)
         {
            ms_log.error("Parent ref for " + nodeRef.getName()
               + "cannot be null");
            throw new RuntimeException(
               "Both visible and hidden parent refs must not be null");
         }
         Long parentId =parentRef.getId().longValue();
         List localChidren = parentChildrenMap.get(parentId);
         if (localChidren == null)
         {
            localChidren = new ArrayList<IPSHierarchyNodeRef>();
            parentChildrenMap.put(parentId, localChidren);
         }
         localChidren.add(nodeRef);
      }
      Iterator parentIdIter = parentChildrenMap.keySet().iterator();
      while (parentIdIter.hasNext())
      {
         Long pId = (Long) parentIdIter.next();
         IPSHierarchyNodeRef nodeRef = null;
         if(target.isNameBased())
         {
            nodeRef = getRootNode(target.getTreeName());
         }
         else
         {
            nodeRef = target.getNodeRef();
         }
         PSMultiOperationException ex = null;
         boolean[] success = new boolean[sourceChildren.length];
         for (int i = 0; i < success.length; i++)
            success[i] = true;
         //deal with partial sucecss
         try
         {
            moveChildren(parentChildrenMap.get(pId), pId, nodeRef.getId()
               .longValue());
         }
         catch (PSMultiOperationException e)
         {
            ex = e;
            Object[] results = e.getResults();
            for (int i = 0; i < results.length; i++)
            {
               if(results[i] instanceof Exception)
                  success[i] = false;
            }
         }
         //repoint parent of moved nodes
         for (int i = 0; i < success.length; i++)
         {
            if (success[i])
            {
               PSHierarchyNodeRef child = (PSHierarchyNodeRef) sourceChildren[i];
               if (child.getObjectType().getPrimaryType() == PSObjectTypes.USER_FILE
                  && child.getObjectType().getSecondaryType() == PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER)
               {
                  if (target.isNameBased())
                  {
                     sourceChildren[i] = new PSHierarchyNodeRefHiddenParent(
                        getRootNode(target.getTreeName()), sourceChildren[i]);
                  }
               }
               else
               ((PSHierarchyNodeRef) sourceChildren[i]).setParent(target
                  .getNodeRef());
            }
         }
         if (ex != null)
            throw ex;
      }
   }
   /**
    * Must be overridden by the derived class to return the root node for the
    * given root node name. Returns <code>null</code> if not overridden.
    * 
    * @param name must not be <code>null</code> or empty.
    * @return root node <code>null</code> if not found one.
    * @throws PSModelException
    */
   @SuppressWarnings("unused")
   public IPSHierarchyNodeRef getRootNode(@SuppressWarnings("unused")
   String name) throws PSModelException
   {
      return null;
   }

   private void moveChildren(List<IPSHierarchyNodeRef> sourceChildren,
      long srcParentId, long tgtParentId) 
      throws PSMultiOperationException, PSModelException
   {
      Throwable ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               Stub binding = getDesignBinding();
               MoveChildrenRequest request = new MoveChildrenRequest();
               request.setSourceId(srcParentId);
               request.setTargetId(tgtParentId);
               long[] ids = new long[sourceChildren.size()];
               for (int i = 0; i < sourceChildren.size(); i++)
                  ids[i] = sourceChildren.get(i).getId().longValue();
               request.setId(ids);
               Method moveChildren = binding.getClass().getMethod("moveChildren",
                  new Class[]
                  {
                     MoveChildrenRequest.class
                  });
               moveChildren.invoke(binding, new Object[]
               {
                  request
               });
            }
            catch (InvocationTargetException e)
            {
               try
               {
                  if (e.getTargetException() instanceof PSInvalidSessionFault)
                  {
                     PSCoreFactory.getInstance().reconnect();
                     redo = true;
                  }
                  else
                     ex = e.getTargetException();
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (NoSuchMethodException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (IllegalAccessException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      
      if (ex != null)
         PSProxyUtils.processAndThrowException(1, ex, ms_log);
   }

   /**
    * Object primary type. Set by constructor.
    */
   public IPSPrimaryObjectType getPrimaryType()
   {
      return m_primaryType;
   }

   /**
    * Get design SOAP binding stub for the proxy. Derived class must build one.
    * 
    * @return never <code>null</code>.
    */
   @SuppressWarnings({"unused","unused"})
   protected UiDesignSOAPStub getDesignBinding() throws MalformedURLException,
      ServiceException
   {
      throw new UnsupportedOperationException(
         "Base class must implement this method");
   }

   /**
    * Get the object store. Just a helper method that delegates call to
    * {@link PSProxyUtils#getObjectStore()}
    * 
    * @see PSProxyUtils#getObjectStore()
    */
   protected PSObjectStore getObjectStore()
   {
      return PSProxyUtils.getObjectStore();
   }

   /**
    * Primary type of the hierarchy model proxy.
    */
   private final IPSPrimaryObjectType m_primaryType;

   /**
    * Logger instance for this class.
    */
   private static Log ms_log = LogFactory.getLog(PSHierarchyModelProxy.class);
}

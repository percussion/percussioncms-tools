/******************************************************************************
 *
 * [ PSUserFileHierarchyModelProxy.java ]
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
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import com.percussion.webservices.ui.data.PSHierarchyNode;
import com.percussion.webservices.uidesign.*;
import org.apache.commons.beanutils.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Provides hierarchy management services for the object type
 * {@link PSObjectTypes#USER_FILE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSHierarchyModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSUserFileHierarchyModelProxy extends PSHierarchyModelProxy
{
   /**
    * Default ctor. Invokes base class ctor
    * {@link PSHierarchyModelProxy#PSHierarchyModelProxy(IPSPrimaryObjectType)}
    * with {@link PSObjectTypes#USER_FILE} as the argument.
    */
   public PSUserFileHierarchyModelProxy() 
   {
      super(PSObjectTypes.USER_FILE);
   }

   /**
    * Override base class version.
    * 
    * @see com.percussion.client.proxies.IPSHierarchyModelProxy#createChildren(NodeId,
    * PSObjectType, List, Object[])
    */
   public IPSHierarchyNodeRef[] createChildren(NodeId targetParent,
      PSObjectType type, List<String> names, Object[] results)
      throws PSMultiOperationException, PSModelException
   {
      if (targetParent == null)
      {
         throw new IllegalArgumentException("targetParent cannot be nullL"); //$NON-NLS-1$
      }

      if (names.size() < results.length)
      {
         throw new IllegalArgumentException("Results Array to small"); //$NON-NLS-1$
      }

      if (names == null || names.size() == 0)
      {
         return (new IPSHierarchyNodeRef[0]);
      }
      IPSHierarchyNodeRef parentRef;
      boolean isParentHiddenRoot = targetParent.isNameBased();
      if (isParentHiddenRoot)
      {
         try
         {
            parentRef = getRootNode(targetParent.getTreeName());
            if (parentRef == null)
            {
               throw new IllegalArgumentException("unknown tree name: " //$NON-NLS-1$
                  + targetParent.getTreeName());
            }
         }
         catch (PSModelException e)
         {
            // shouldn't happen
            throw new RuntimeException(e);
         }
      }
      else
      {
         parentRef = targetParent.getNodeRef();
      }
      Exception ex = null;
      IPSHierarchyNodeRef[] resArray = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               CreateHierarchyNodesRequestType wsType = 
                  CreateHierarchyNodesRequestType.folder;
               if (type.getSecondaryType() == 
                  PSObjectTypes.UserFileSubTypes.PLACEHOLDER)
               {
                  wsType = CreateHierarchyNodesRequestType.placeholder;
               }
               UiDesignSOAPStub binding = getDesignBinding();
               CreateHierarchyNodesRequest request = 
                  new CreateHierarchyNodesRequest();
               request.setName(names.toArray(new String[0]));
               CreateHierarchyNodesRequestType[] types = 
                  new CreateHierarchyNodesRequestType[names.size()];
               long[] parentIds = new long[names.size()];
               long pid = PSProxyUtils.getDesignId(parentRef.getId());
               for (int i = 0; i < parentIds.length; i++)
               {
                  types[i] = wsType;
                  parentIds[i] = pid;
               }
               request.setType(types);
               request.setParentId(parentIds);
               PSHierarchyNode[] result = binding.createHierarchyNodes(request);
               Object[] objServer = (Object[]) PSProxyUtils.convert(
                  com.percussion.services.ui.data.PSHierarchyNode[].class, 
                  result);
               resArray = new IPSHierarchyNodeRef[objServer.length];
               for (int i = 0; i < objServer.length; i++)
               {
                  com.percussion.services.ui.data.PSHierarchyNode node = 
                     (com.percussion.services.ui.data.PSHierarchyNode) objServer[i];
                  PSHierarchyNodeRef ref = null;
                  if (isParentHiddenRoot)
                     ref = new PSHierarchyNodeRefHiddenParent(parentRef, node);
                  else
                     ref = new PSHierarchyNodeRef(parentRef, node);
                  ref.setLockSessionId(PSCoreFactory.getInstance()
                     .getClientSessionId());
                  ref.setLockUserName(
                     PSCoreFactory.getInstance().getConnectionInfo().getUserid());
                  resArray[i] = ref;
                  results[i] = objServer[i];
               }
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (PSNotAuthorizedFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      catch (PSTransformationException e)
      {
         ex = e;
      }
      catch (PSModelException e)
      {
         ex = e;
      }
      
      if (ex != null)
         PSProxyUtils.processAndThrowException(names.size(), ex, ms_log);
      
      return resArray;
   }

   /**
    * Finds a node by a specific name whose parent is <code>null</code>.
    * 
    * @param treeName The base name of the node to find. Assumed not
    * <code>null</code> or empty. A case-insensitive compare is done.
    * 
    * @return May be <code>null</code> if a matching node is not found.
    * 
    * @throws PSModelException If any problems constructing the noderef.
    */
   @Override
   public IPSHierarchyNodeRef getRootNode(String treeName)
      throws PSModelException
   {
      IPSHierarchyNodeRef result = null;
      com.percussion.services.ui.data.PSHierarchyNode item = m_rootNodeMap
         .get(treeName.toLowerCase());
      if (item != null)
      {
         PSObjectType type = com.percussion.client.proxies.impl.PSUserFileModelProxy.USERFILE_PLACEHOLDER;
         if (item.getType() == com.percussion.services.ui.data.PSHierarchyNode.NodeType.FOLDER)
            type = com.percussion.client.proxies.impl.PSUserFileModelProxy.USERFILE_FOLDER;
         result = new PSHierarchyNodeRef(null, item.getName(), type, item
            .getGUID(), type.getSecondaryType().equals(
            PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER));
         result.setManager(PSCoreFactory.getInstance().getModel(
            PSObjectTypes.USER_FILE).getHierarchyManager(treeName));
      }
      return result;
   }

   /**
    * Overide the base method.
    * 
    * @see com.percussion.client.proxies.IPSHierarchyModelProxy#getRoots()
    */
   public Collection<String> getRoots() throws PSModelException
   {
      if (m_rootNodeMap == null)
      {
         m_rootNodeMap = 
            new HashMap<String, com.percussion.services.ui.data.PSHierarchyNode>();
         Exception ex = null;
         try
         {
            boolean redo = false;
            do
            {
               redo = false;
               try
               {
                  UiDesignSOAPStub binding = getDesignBinding();
                  GetChildrenRequest getRequest = new GetChildrenRequest();
                  getRequest.setId(new PSDesignGuid(PSTypeEnum.HIERARCHY_NODE, 0)
                     .longValue());
                  long[] rootIds = binding.getChildren(getRequest);
                  LoadHierarchyNodesRequest loadRequest = null;
                  loadRequest = new LoadHierarchyNodesRequest();
                  loadRequest.setId(rootIds);
                  loadRequest.setLock(false);
                  loadRequest.setOverrideLock(false);
                  PSHierarchyNode[] nodes = binding.loadHierarchyNodes(
                     loadRequest);
                  Object[] converted = (Object[]) PSProxyUtils.convert(
                     com.percussion.services.ui.data.PSHierarchyNode[].class, 
                     nodes);
                  for (Object obj : converted)
                  {
                     com.percussion.services.ui.data.PSHierarchyNode node = 
                        (com.percussion.services.ui.data.PSHierarchyNode) obj;
                     m_rootNodeMap.put(node.getName().toLowerCase(), node);
                  }
               }
               catch (PSInvalidSessionFault e)
               {
                  try
                  {
                     PSCoreFactory.getInstance().reconnect();
                     redo = true;
                  }
                  catch (Exception e1)
                  {
                     ex = e1;
                  }
               }
            } while (redo);
         }
         catch (MalformedURLException e)
         {
            ex = e;
         }
         catch (ServiceException e)
         {
            ex = e;
         }
         catch (PSContractViolationFault e)
         {
            ex = e;
         }
         catch (RemoteException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            ex = e;
         }
         
         if (ex != null)
            PSProxyUtils.processAndThrowException(ex, ms_log);
      }
      
      return m_rootNodeMap.keySet();
   }

   /**
    * Override the base class version.
    * 
    * @see com.percussion.client.proxies.IPSHierarchyModelProxy#getChildren(NodeId)
    */
   public IPSHierarchyNodeRef[] getChildren(NodeId parent)
      throws PSModelException
   {
      UiDesignSOAPStub binding;
      PSHierarchyNode[] nodes = null;
      IPSHierarchyNodeRef[] result = null;
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               binding = getDesignBinding();
               GetChildrenRequest getRequest = new GetChildrenRequest();
               IPSHierarchyNodeRef parentRef = parent.getNodeRef();
               boolean hiddenRoot = (parentRef == null);
               if (hiddenRoot)
               {
                  parentRef = new PSHierarchyNodeRef(null, 
                     m_rootNodeMap.get(parent.getTreeName().toLowerCase()));
               }
               Long id = parentRef.getId().longValue();
               getRequest.setId(id);
               long[] childIds = binding.getChildren(getRequest);
               if (childIds.length == 0)
               {
                  return new IPSHierarchyNodeRef[0];
               }
               LoadHierarchyNodesRequest loadRequest = null;
               loadRequest = new LoadHierarchyNodesRequest();
               loadRequest.setId(childIds);
               loadRequest.setLock(false);
               loadRequest.setOverrideLock(false);
               nodes = binding.loadHierarchyNodes(loadRequest);
               result = new IPSHierarchyNodeRef[nodes.length];
               PSTransformerFactory factory = 
                  PSTransformerFactory.getInstance();
               Converter converter = factory.getConverter(
                  PSHierarchyNode.class);
               for (int i = 0; i < nodes.length; i++)
               {
                  com.percussion.services.ui.data.PSHierarchyNode converted = 
                     (com.percussion.services.ui.data.PSHierarchyNode) converter
                        .convert(
                           com.percussion.services.ui.data.PSHierarchyNode.class, 
                           nodes[i]);
                  if (hiddenRoot)
                  {
                     result[i] = new PSHierarchyNodeRefHiddenParent(parentRef,
                        converted);
                  }
                  else
                  {
                     result[i] = new PSHierarchyNodeRef(parentRef, converted);
                  }
                  ((PSHierarchyNodeRef) result[i]).setPersisted();
               }
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         PSProxyUtils.processAndThrowException(ex, ms_log);
      
      return result;
   }

   /**
    * @see com.percussion.client.models.IPSUserFileModel#getDescendentPlaceholders(String)
    */
   public Collection<com.percussion.services.ui.data.PSHierarchyNode> getDescendents(
      String path) throws PSModelException
   {
      UiDesignSOAPStub binding;
      PSHierarchyNode[] nodes = null;
      Collection<com.percussion.services.ui.data.PSHierarchyNode> result = 
         new ArrayList<com.percussion.services.ui.data.PSHierarchyNode>();
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               binding = getDesignBinding();
               FindHierarchyNodesRequest findRequest = 
                  new FindHierarchyNodesRequest();
               findRequest.setPath(path);
               com.percussion.webservices.common.PSObjectSummary[] summaries = 
                  binding.findHierarchyNodes(findRequest);
               if (summaries == null || summaries.length == 0)
                  return result;
      
               long[] childIds = new long[summaries.length];
               for (int i = 0; i < summaries.length; i++)
                  childIds[i] = summaries[i].getId();
      
               LoadHierarchyNodesRequest loadRequest = null;
               loadRequest = new LoadHierarchyNodesRequest();
               loadRequest.setId(childIds);
               loadRequest.setLock(false);
               loadRequest.setOverrideLock(false);
               nodes = binding.loadHierarchyNodes(loadRequest);
               PSTransformerFactory factory = 
                  PSTransformerFactory.getInstance();
               Converter converter = 
                  factory.getConverter(PSHierarchyNode.class);
               for (int i = 0; i < nodes.length; i++)
               {
                  com.percussion.services.ui.data.PSHierarchyNode converted = 
                     (com.percussion.services.ui.data.PSHierarchyNode) converter
                        .convert(
                           com.percussion.services.ui.data.PSHierarchyNode.class, 
                           nodes[i]);
                  result.add(converted);
               }
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         PSProxyUtils.processAndThrowException(ex, ms_log);

      return result;
   }

   /**
    * Get binding for the assembly design SOAP port.
    * 
    * @return the new binding, never <code>null</code>.
    * @throws ServiceException
    * @throws MalformedURLException
    */
   @Override
   protected UiDesignSOAPStub getDesignBinding() throws MalformedURLException,
      ServiceException
   {
      return PSProxyUtils.getUiDesignStub();
   }

   /**
    * Root node map initially <code>null</code> constructed and cached when
    * {@link #getRoots()} is called for the first time. This cache is not
    * cleared or dirtied during the life time of this class assuming the root
    * nodes are not added during that time.
    * 
    * @see #getRoots()
    */
   private Map<String, com.percussion.services.ui.data.PSHierarchyNode> 
      m_rootNodeMap = null;

   /**
    * Logger instance for this class.
    */
   private static Logger ms_log = LogManager.getLogger(
      PSUserFileHierarchyModelProxy.class);
}

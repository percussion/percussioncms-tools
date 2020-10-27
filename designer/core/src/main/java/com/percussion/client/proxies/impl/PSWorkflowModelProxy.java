/******************************************************************************
 *
 * [ PSWorkflowModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.common.PSObjectSummaryLocked;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.system.LoadWorkflowsRequest;
import com.percussion.webservices.system.PSWorkflow;
import com.percussion.webservices.system.PSWorkflowRole;
import com.percussion.webservices.system.SystemSOAPStub;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides cataloging read-only load services for the object type
 * {@link PSObjectTypes#WORKFLOW}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSWorkflowModelProxy extends PSReadOnlyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#WORKFLOW} and for main type and
    * <code>null</code> sub type since this object type does not have any sub
    * types.
    */
   public PSWorkflowModelProxy()
   {
      super(PSObjectTypes.WORKFLOW);
   }

   /**
    * Get the {@link IPSGuid}s for the roles that is member of the workflow
    * with supplied workflow reference. If the supplied workflow reference is
    * <code>null</code> all workflows are catalogged from the server and a set
    * of all workflow roles is returned.
    * 
    * @param wfRef Reference of the workflow whose member roles are asked for.
    * If <code>null</code> all workflows are cataloged and and set of all
    * roles from all workflows is returned.
    * @return set of workflow roles as explained above. Never <code>null</code>
    * may be empty.
    * @throws PSModelException 
    */
   public Collection<String> getWorkflowRoles(IPSReference wfRef)
      throws PSModelException
   {
      Set<String> result = new HashSet<String>();
      try
      {
         Collection<IPSReference> wfRefs = null;
         if (wfRef == null)
         {
            wfRefs = catalog();
         }
         else
         {
            wfRefs = new ArrayList<IPSReference>(1);
            wfRefs.add(wfRef);
         }
         PSWorkflow[] wfs = (PSWorkflow[]) load(wfRefs
            .toArray(new IPSReference[0]), false, false);
         for (PSWorkflow wf : wfs)
         {
            PSWorkflowRole[] wfRoles = wf.getRoles();
            for (PSWorkflowRole wfRole : wfRoles)
               result.add(wfRole.getName());
         }
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException(e);
      }
      return result;
   }

   /**
    * Does not support loading locked objects. This is a read-only model.
    * 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#load(IPSReference[],
    * boolean, boolean)
    */
   @SuppressWarnings("unused")
   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException, PSModelException
   {
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               SystemSOAPStub binding = 
                  (SystemSOAPStub) getSoapBinding(METHOD.LOAD);
               LoadWorkflowsRequest req = new LoadWorkflowsRequest();
               com.percussion.webservices.system.PSWorkflow[] wfs = 
                  binding.loadWorkflows(req);
               return wfs;
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
      
      if (ex != null)
         processAndThrowException(reference.length, ex);
      
      // will never get here
      return new Object[0];
   }

   /**
    * Does not perform validation on reference names.
    * 
    * @see PSCmsModelProxy#objectSummaryArrayToReferenceCollection(
    *    PSObjectSummary[], PSObjectType)
    */
   @Override
   protected Collection<IPSReference> objectSummaryArrayToReferenceCollection(
      PSObjectSummary[] results, PSObjectType objType) throws PSModelException
   {
      if (results == null)
      {
         throw new IllegalArgumentException("results must not be null");
      }
      if (objType == null)
      {
         throw new IllegalArgumentException("objType must not be null");
      }
      Collection<IPSReference> coll = new ArrayList<IPSReference>();
      if (results != null && results.length > 0)
      {
         for (PSObjectSummary result : results)
         {
            // We override to avoid name validation for workflows
            PSReference ref = new PSReference(){
               @Override
               public void setName(String name)
               {
                  if (name == null || name.length() == 0)
                  {
                     throw new IllegalArgumentException(
                        "name must not be null or empty");
                  }
                  m_name = name;
               }
            };
            ref.setDescription(result.getDescription());
            ref.setId(new PSDesignGuid(result.getId()));
            ref.setLabelKey(result.getLabel());
            ref.setName(result.getName());
            ref.setObjectType(objType);
            ref.setPersisted();
            PSObjectSummaryLocked lockInfo = result.getLocked();
            if (lockInfo != null)
            {
               ref.setLockUserName(lockInfo.getLocker());
               ref.setLockSessionId(lockInfo.getSession());
            }
            int[] permissions = result.getPermissions();
            if (permissions == null)
               permissions = new int[0];
            ref.setPermissions(permissions);
            coll.add(ref);
         }
      }
      return coll;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#getSoapBinding(com.percussion.client.proxies.IPSCmsModelProxy.METHOD)
    */
   @Override
   protected Stub getSoapBinding(METHOD method) throws MalformedURLException,
      ServiceException
   {
      if (method == METHOD.LOAD)
         return PSProxyUtils.getSystemPublicStub();
      return PSProxyUtils.getSystemDesignStub();
   }
}

/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.client.proxies.impl;

import com.percussion.client.*;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.common.PSObjectSummaryLocked;
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
 * @since  03-Sep-2005 4:39:27 PM
 */
public class PSWorkflowModelProxy extends PSReadOnlyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#WORKFLOW} and for main type and
    * <code>null</code> subtype since this object type does not have any subtypes.
    */
   public PSWorkflowModelProxy()
   {
      super(PSObjectTypes.WORKFLOW);
   }

   /**
    * Get the {@link IPSGuid}s for the roles that is member of the workflow
    * with supplied workflow reference. If the supplied workflow reference is
    * <code>null</code> all workflows are cataloged from the server and a set
    * of all workflow roles is returned.
    * 
    * @param wfRef Reference of the workflow whose member roles are asked for.
    * If <code>null</code> all workflows are cataloged and set of all
    * roles from all workflows is returned.
    * @return set of workflow roles as explained above. Never <code>null</code>
    * may be empty.
    * @throws PSModelException When an exception occurs.
    */
   public Collection<String> getWorkflowRoles(IPSReference wfRef)
      throws PSModelException
   {
      Set<String> result = new HashSet<>();
      try
      {
         Collection<IPSReference> wfRefs;
         if (wfRef == null)
         {
            wfRefs = catalog();
         }
         else
         {
            wfRefs = new ArrayList<>(1);
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
         boolean redo;
         do
         {
            redo = false;
            try
            {
               SystemSOAPStub binding = 
                  (SystemSOAPStub) getSoapBinding(METHOD.LOAD);
               LoadWorkflowsRequest req = new LoadWorkflowsRequest();
               return binding.loadWorkflows(req);
            }
            catch (RemoteException e)
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
      catch (MalformedURLException | ServiceException e)
      {
         ex = e;
      }

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
      Collection<IPSReference> coll = new ArrayList<>();
      if (results.length > 0)
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

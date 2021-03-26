/******************************************************************************
 *
 * [ PSWorkflowModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.client.proxies.impl.PSReadOnlyModelProxy;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PSWorkflowModelProxy extends PSReadOnlyModelProxy
{

   public PSWorkflowModelProxy() throws PSUninitializedConnectionException
   {
      super(PSObjectTypes.WORKFLOW); 
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      List<IPSReference> workflows = new ArrayList<IPSReference>();
      for (int i = 0; i < 5; i++)
      {
         String cname = "Workflow" + i; //$NON-NLS-1$
         String clabel = "Workflow " + i; //$NON-NLS-1$
         String cdesc = "Workflow " + i + " Desc"; //$NON-NLS-1$ //$NON-NLS-2$
         //guids for workflows should not have the hostid set because they are 
         // still legacy
         IPSReference ref = new PSReference(cname, clabel, cdesc,
            new PSObjectType(PSObjectTypes.WORKFLOW), new PSGuid(0L,
               PSTypeEnum.WORKFLOW, i));
         workflows.add(ref);
      }
      return workflows;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSReadOnlyModelProxy#load(com.percussion.client.IPSReference[],
    * boolean, boolean)
    */
   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException
   {
      return new Object[reference.length];
   }

   /**
    * 
    * @param wfRef
    * @return
    * @throws PSModelException
    */
   public Collection<IPSReference> getWorkflowRoles(IPSReference wfRef)
      throws PSModelException
   {
      PSRoleModelProxy roleProxy = new PSRoleModelProxy();
      return roleProxy.catalog();
   }
}

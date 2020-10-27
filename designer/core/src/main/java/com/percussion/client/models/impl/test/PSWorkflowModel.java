/******************************************************************************
 *
 * [ PSWorkflowModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl.test;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.proxies.impl.test.PSWorkflowModelProxy;

import java.util.Collection;

/**
 * 
 */
public class PSWorkflowModel extends
   com.percussion.client.models.impl.PSWorkflowModel
{

   /**
    * @param name
    * @param description
    * @param supportedType
    */
   public PSWorkflowModel(String name, String description,
      IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.models.impl.PSWorkflowModel#getWorkflowRoles(com.percussion.client.IPSReference)
    */
   @Override
   public Collection<IPSReference> getWorkflowRoles(IPSReference wfRef)
      throws PSModelException
   {
      PSWorkflowModelProxy proxy;
      proxy = new PSWorkflowModelProxy();
      return proxy.getWorkflowRoles(wfRef);
   }
}

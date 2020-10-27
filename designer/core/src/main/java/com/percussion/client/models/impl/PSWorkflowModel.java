/******************************************************************************
 *
 * [ PSWorkflowModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.models.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSWorkflowModel;
import com.percussion.client.proxies.impl.PSWorkflowModelProxy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * See interface.
 * 
 * @author ram
 */
public class PSWorkflowModel extends PSCmsModel implements IPSWorkflowModel
{

   /**
    * The only ctor. See 3 param ctor for description.
    */
   public PSWorkflowModel(String name, String description,
      IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   // see interface
   public Collection<IPSReference> getWorkflowRoles(IPSReference wfRef)
      throws PSModelException
   {
      PSWorkflowModelProxy proxy = null;
      Set<IPSReference> result = new HashSet<IPSReference>();
      IPSCmsModel cmsModel = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.ROLE);
      Collection<IPSReference> allRoles = cmsModel.catalog(true);

      proxy = new PSWorkflowModelProxy();
      Collection<String> wfRoles = proxy.getWorkflowRoles(wfRef);
      for (String roleName : wfRoles)
      {
         for (IPSReference ref : allRoles)
         {
            if (ref.getName().equals(roleName))
            {
               result.add(ref);
               break;
            }
         }
      }
      return result;
   }
}

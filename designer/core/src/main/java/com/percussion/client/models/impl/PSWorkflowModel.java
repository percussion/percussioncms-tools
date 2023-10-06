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
      PSWorkflowModelProxy proxy;
      Set<IPSReference> result = new HashSet<>();
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

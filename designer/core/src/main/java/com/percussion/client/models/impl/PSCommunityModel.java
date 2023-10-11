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
import com.percussion.client.models.IPSCommunityModel;
import com.percussion.client.proxies.impl.PSCommunityModelProxy;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * See interface.
 * 
 * @author ram
 */
public class PSCommunityModel extends PSCmsModel implements IPSCommunityModel
{

   /**
    * The only ctor. See 3 param ctor for description.
    */
   public PSCommunityModel(String name, String description,
      IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   // see interface
   public Collection<IPSReference> getCommunityRoles(IPSReference commRef)
      throws PSModelException
   {

      PSCommunityModelProxy proxy = new PSCommunityModelProxy();
      Set<IPSReference> result = new HashSet<>();
      IPSCmsModel roleModel = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.ROLE);
      Collection<IPSReference> allRoles = roleModel.catalog(true);

      Collection<IPSGuid> commGuids = proxy.getCommunityRoleIds(commRef);
      for (IPSGuid guid : commGuids)
      {
         for (IPSReference ref : allRoles)
         {
            if (ref.getId().equals(guid))
            {
               result.add(ref);
               break;
            }
         }
      }
      return result;
   }

}

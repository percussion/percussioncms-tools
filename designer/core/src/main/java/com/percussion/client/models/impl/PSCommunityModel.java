/******************************************************************************
 *
 * [ PSCommunityModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
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

      PSCommunityModelProxy proxy = null;
      Set<IPSReference> result = new HashSet<IPSReference>();
      IPSCmsModel roleModel = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.ROLE);
      Collection<IPSReference> allRoles = roleModel.catalog(true);

      proxy = new PSCommunityModelProxy();
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

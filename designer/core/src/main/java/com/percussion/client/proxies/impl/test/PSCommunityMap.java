/******************************************************************************
 *
 * [ PSCommunityMap.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSObjectTypes;
import com.percussion.services.security.data.PSCommunity;

import java.util.List;

/**
 * Helper class that encapsulates the community map that is used as the community repository
 * in the test community proxy
 * {@link com.percussion.client.proxies.impl.test.PSCommunityModelProxy}. This is
 * class is required mainly to get a better control on serialization and
 * deserialization using the class
 * {@link com.percussion.xml.serialization.PSObjectSerializer} and has no other
 * use.
 * 
 */
public class PSCommunityMap extends PSRepositoryMap
{  
    
   /**
    * Add a community
    * @param obj
    */
   public void addCommunity(PSCommunity obj)
   {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj may not be null");
      }
      put(getReference(obj), obj);
   }
   
   /**
    * Get communities
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<PSCommunity> getCommunities()
   {
      return m_values;
   }
   
   public PSObjectTypes getType()
   {
      return PSObjectTypes.COMMUNITY;
   }

  
}

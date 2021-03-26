/*******************************************************************************
 *
 * [ PSItemFilterMap.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSObjectTypes;
import com.percussion.services.filter.data.PSItemFilter;

import java.util.List;

/**
 * Helper class that encapsulates the slot map that is used as slot repository
 * in the test slot proxy
 * {@link com.percussion.client.proxies.impl.test.PSItemFilterModelProxy}. This
 * is class is required mainly to get a better control on serialization and
 * deserialization using the class
 * {@link com.percussion.xml.serialization.PSObjectSerializer} and has no other
 * use.
 * 
 */
public class PSItemFilterMap extends PSRepositoryMap
{
   /**
    * Add a filter
    * @param obj
    */
   public void addFilter(PSItemFilter obj)
   {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj may not be null");
      }
      put(getReference(obj), obj);
   }
   
   /**
    * Get filters
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<PSItemFilter> getFilters()
   {
      return m_values;
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSRepositoryMap#getType()
    */
   @Override
   public PSObjectTypes getType()
   {
      return PSObjectTypes.ITEM_FILTER;
   }
}

/******************************************************************************
 *
 * [ PSKeywordMap.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSObjectTypes;
import com.percussion.services.content.data.PSKeyword;

import java.util.List;

/**
 * Helper class that encapsulates the keyword map that is used as keyword repository
 * in the test keyword proxy
 * {@link com.percussion.client.proxies.impl.test.PSKeywordModelProxy}. This is
 * class is required mainly to get a better control on serialization and
 * deserialization using the class
 * {@link com.percussion.xml.serialization.PSObjectSerializer} and has no other
 * use.
 * 
 */
public class PSKeywordMap extends PSRepositoryMap
{   
   /**
    * Add a keyword
    * @param obj
    */
   public void addKeyword(PSKeyword obj)
   {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj may not be null");
      }
      put(getReference(obj), obj);
   }
   
   /**
    * Get keywords
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<PSKeyword> getKeywords()
   {
      return m_values;
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSRepositoryMap#getType()
    */
   @Override
   public PSObjectTypes getType()
   {
      return PSObjectTypes.KEYWORD;
   }

  
}

/******************************************************************************
 *
 * [ PSLocaleMap.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSObjectTypes;
import com.percussion.i18n.PSLocale;

import java.util.List;

/**
 * 
 */
public class PSLocaleMap extends PSRepositoryMap
{
   /**
    * Add a locale
    * @param obj
    */
   public void addLocale(PSLocale obj)
   {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj may not be null");
      }
      put(getReference(obj), obj);
   }
   
   /**
    * Get locales
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<PSLocale> getLocales()
   {
      return m_values;
   }

   public PSObjectTypes getType()
   {
      return PSObjectTypes.LOCALE;
   }

}

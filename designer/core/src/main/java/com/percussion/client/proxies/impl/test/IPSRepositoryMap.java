/******************************************************************************
*
* [ IPSRepositoryMap.java ]
*
* COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;

import java.util.Collection;
import java.util.Iterator;

public interface IPSRepositoryMap
{
   /**
    * @param key
    */
   public void remove(IPSReference key);

   /**
    * @param key
    * @return
    */
   public Object get(IPSReference key);

   /**
    * @return
    */
   public Collection values();

   /**
    * @param key
    * @param obj
    */
   public void put(IPSReference key, Object obj);

   /**
    * 
    */
   public void clear();

   /**
    * @return
    */
   public Iterator getAll();
   
   
}

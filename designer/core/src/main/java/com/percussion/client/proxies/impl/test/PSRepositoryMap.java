/******************************************************************************
 *
 * [ PSRepositoryMap.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Repository map base class that implements the required
 * <code>IPSRepositoryMap</code> methods.
 *
 */
public class PSRepositoryMap implements IPSRepositoryMap
{ 
     
   /**
    * @param key
    */
   public void remove(IPSReference key)
   {
      int i = getIndex(key);
      if(i < m_index.size())
      {
         m_index.remove(i);
         m_values.remove(i);         
      }
   }

   /**
    * @param key The handle for the design object of interest. Never
    * <code>null</code>.
    * 
    * @return The data object associated with the supplied key from the
    * in-memory cache, or <code>null</code> if there is no object in the
    * repository by that id.
    */
   @IPSXmlSerialization(suppress=true)
   public Object get(IPSReference key)
   {
      if (null == key)
      {
         throw new IllegalArgumentException("key cannot be null");  
      }
      
      int i = getIndex(key);
      if (i < m_index.size())
      {
         return m_values.get(i);
      }
      return null;
   }

   /**
    * Scans {@link #m_index} looking for a match to the supplied key. If found,
    * the index value is returned.
    * 
    * @param key Assumed not <code>null</code>.
    * 
    * @return A value &lt; <code>m_index.size()</code> if found, otherwise it
    * will be &gt;=.
    */
   private int getIndex(IPSReference key)
   {
      int i = 0;
      for (; i < m_index.size(); i++)
      {
         if (m_index.get(i).referencesSameObject(key))
            break;
      }
      return i;
   }
   
   /**
    * @return Never <code>null</code>.
    */
   public Collection values()
   {
      return m_values;
   }

   /**
    * @param key The handle to the supplied object. Never <code>null</code>.
    * 
    * @param obj The data to be stored in the in-memory repository. May be
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void put(IPSReference key, Object obj)
   {
      int i = getIndex(key);
      if (i < m_index.size())
      {
         m_values.remove(i);
         m_values.add(i, obj);
      }
      else
      {
         m_index.add(key);
         m_values.add(obj);
      }      
   }

   /**
    * 
    */
   public void clear()
   {
      m_index.clear();
      m_values.clear();
   }

   /**
    * @return Never <code>null</code>.
    */
   @IPSXmlSerialization(suppress=true)
   public Iterator getAll()
   {
      return m_values.iterator();
   }   
   
   /**
    * Only needed by proxies that use betwixt and a subclass
    * of this class. {@link #getType()} must be implemented
    * in the subclass for this to work.
    * 
    * @param obj 
    * @return Never <code>null</code>.
    */
   @IPSXmlSerialization(suppress=true)
   public IPSReference getReference(Object obj)
   {
      if(getType() == null)
         throw new RuntimeException(
            "The getType() method must be implemented for getReference() to" +
            " work.");
      PSReferenceFactory factory = PSReferenceFactory.getInstance();
      return factory.getReference(obj, getType());
   }   
   

   /**
    * Must be implemented by proxies that need to call 
    * {@link #getReference(Object)}. These are proxies that
    * use betwixt for serialization.
    * 
    * @return This class always returns <code>null</code>.
    */
   @IPSXmlSerialization(suppress=true)
   public PSObjectTypes getType()
   {
      return null;
   }
   
  
   /*
    * Lists are needed instead of a Map as we may manipulate the key
    * object in which case the hash will become invalid
    */
   protected List<IPSReference> m_index = new ArrayList<IPSReference>();
   protected List m_values = new ArrayList();
}

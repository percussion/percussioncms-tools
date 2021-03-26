/******************************************************************************
 *
 * [ PSMultiOperationException.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to report multiple errors and positive results from an
 * operation that takes multiple instances of a type. Generally, the same
 * operation is performed on all instances and the proper result or an error
 * condition is noted for each one. If at least 1 exception occurs, this
 * exception is thrown and the mixture of results and exceptions is available to
 * the caller.
 * <p>
 * It is guaranteed that there is at least 1 entry that has an exception class.
 * There may be no entries of the desired result class.
 * 
 * @author paulhoward
 * @version 6.0
 */
public class PSMultiOperationException extends Exception
{
   /**
    * Convenience ctor that calls
    * {@link #PSMultiOperationException(Object[], Object[])}
    * this(Object[] &#123; result &#125;, null); 
    */
   public PSMultiOperationException(Object result)
   {
      this(new Object[] { result }, null);
   }
   
   /**
    * Convenience ctor that calls
    * {@link #PSMultiOperationException(Object[], Object[]) this(result, null)}.
    */
    public PSMultiOperationException(Object result[])
   {
      this(result, null);
   }
      
   /**
    * Convenience ctor that calls
    * {@link #PSMultiOperationException(Object[], Object[])} 
    * this(new Object[] &#123; result &#125;
    * , new Object[] &#123; detail &#125;).
    */
   public PSMultiOperationException(Object result, Object detail)
   {
      this(new Object[] { result }, new Object[] { detail});
   }

   /**
    * Instantiate with multiple results.
    * 
    * @param results An array whose length is equal to the number of instances
    * being operated upon. Each entry should be either the object that would
    * have been returned if everything had been successful or an
    * <code>Exception</code>. At least 1 entry must be of the latter type.
    * @param details An array whose length is equal to the number of instances
    * being operated upon or <code>null</code>. Each entry must be either an
    * instance of <code>String</code> or <code>IPSReference</code> or 
    * <code>null</code>. These objects are used to give further detail about
    * the object that had the corresponding error.
    */
   public PSMultiOperationException(Object[] results, Object[] details)
   {
      if ( null == results || results.length == 0)
      {
         throw new IllegalArgumentException("results cannot be null or empty");  
      }
      
      if (null != details && details.length != results.length)
         throw new IllegalArgumentException(
            "If details not null then its length must be the same as results");
      
      m_results = results;
      m_details = new ArrayList<Object>(results.length);
      if(details != null)
      {
         int count = 0;
         for(Object obj : details)
            setDetail(count++, obj);
      }
   }
   
   /**
    * Adds a detail for the specified exception
    * @param index the index of the exception, must be within the bounds
    * of the results array or an <code>ArrayOutOfBoundsException</code> will
    * be thrown.
    * @param obj the detail object. Can be <code>null</code>, but if not
    * then must be an instance of either <code>IPSReference</code> or
    * <code>String</code>.
    */
   public void setDetail(int index, Object obj)
   {
      if(obj != null && !(obj instanceof String || obj instanceof IPSReference))
         throw new IllegalArgumentException("Exception detail object must be an"
            + "instance of String or IPSReference or null.");
      if(index < 0 || index >= m_results.length)
         throw new ArrayIndexOutOfBoundsException("Index of " + index);
      m_details.add(index, obj);
   }
   
   /**
    * Returns an exception detail object
    * @param index
    * @return Returns the detail object supplied in the ctor or set by calling the 
    * {@link #setDetail(int, Object)} method at the requested index.
    * Either a <code>IPSReference</code> or <code>String</code>
    * object, may be <code>null</code>.
    */
   public Object getDetail(int index)
   {
      if(index < 0 || index >= m_results.length)
         throw new ArrayIndexOutOfBoundsException("Index of " + index);
      return m_details.get(index);   
   }
   
   /**
    * Returns the exception detail objects
    * @return Array of objects containing either <code>IPSReference</code>
    * or <code>String</code> object, may be <code>null</code>. If not 
    * <code>null</code> then the length will be equal to the length
    * of the array returned from {@link #getResults()}.
    */
   public Object[] getDetails()
   {
      Object[] details = m_details.toArray();
      return details.length == 0 ? null : m_details.toArray();
   }
   
   /**
    * Each entry is either an object whose class is determined by the called
    * method or an <code>Exception</code>. The length of the returned array
    * is equal to the length of the array supplied to the called method.
    * 
    * @return Never <code>null</code> or empty. Whether elements can be
    * <code>null</code> depends on context.
    */
   public Object[] getResults()
   {
      return m_results;
   }

   /**
    * Returns the first exception in details array as the cause of this exception.
    * @return the first exception in the details array.
    * Returns <code>null</code> if there is no exception. Normally null should
    * not be returned. Because constructor does not insure that at least
    * one exception exists in details data, and to make sure this method does
    * not break existing code we don't throw an exception.  
    */
   @Override
   public Throwable getCause()
   {
      assert super.getCause() == null;

      for (final Object result : getResults())
      {
         if (result instanceof Throwable)
         {
            return (Throwable) result;
         }
      }
      return null;
   }



   /**
    * Stores the data supplied in the ctor. Never <code>null</code> or empty.
    */
   private Object[] m_results;
   
   /**
    * Initialized in the ctor 
    * {@link #PSMultiOperationException(Object[], Object[])}
    * then never <code>null</code>, may be empty.
    */
   private List<Object> m_details;
   
   /**
    * Support serialization.
    */
   private static final long serialVersionUID = -4031170264594583839L;
}

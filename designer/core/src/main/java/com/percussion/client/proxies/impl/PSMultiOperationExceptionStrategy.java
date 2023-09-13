/*******************************************************************************
 *
 * [ PSMultiOperationExceptionStrategy.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.PSModelException;
import com.percussion.client.models.PSLockException;
import com.percussion.conn.PSServerException;
import com.percussion.error.PSNotLockedException;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;

/**
 * <p>Base class for transforming operations throwing checked exceptions.
 * Catches any transformation checked exception from subclasses, and rethrows
 * it wrapping in {@link FunctorException}. Converts lower-level checked
 * exceptions to the client exceptions. 
 * Lets unchecked exceptions through</p>
 * <p>Example of definition:
 * <pre>
 *    final PSMultiOperationExceptionStrategy transformer =
 *          new PSMultiOperationExceptionStrategy()
 *    {
 *       protected Object doTransform(Object obj) throws Exception
 *       {
 *          ... // operation implementation 
 *       }
 *    }
 * <pre>
 * </p>
 * <p>Example of usage: 
 * </pre>
 *    try
 *    {
 *       Object b = transformer.transform(a); 
 *    }
 *    catch (FunctorException e)
 *    {
 *       System.out.println("Operation threw checked exception: " + e.getCause());
 *    }
 * </pre>
 * </p>
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSMultiOperationExceptionStrategy implements Transformer
{
   /**
    * Calls {@link #doTransform(Object)} and processes checked exceptions
    * according to the class contract.
    * Lets unchecked exceptions through.
    */
   public Object transform(Object obj)
   {
      try
      {
         return doTransform(obj);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (final Exception e)
      {
         final Exception e2;
         if (e instanceof PSServerException)
         {
            e2 = new PSModelException(e);
         }
         else if (e instanceof PSNotLockedException)
         {
            e2 = constructLockException((PSNotLockedException) e);
         }
         else
         {
            e2 = e;
         }
         return passExceptionUp(e2);
      }
   }

   /**
    * The overwriting class must overwrite this method to handle this particular
    * exception. It is recommended the implementation will wrap it into
    * {@link PSLockException}. 
    * @return the wrapped exception. Must not be <code>null</code>.
    */
   protected abstract PSLockException constructLockException(PSNotLockedException e);

   /**
    * Rethrows the exception wrapping it into {@link FunctorException}.
    */
   Object passExceptionUp(final Exception e)
   {
      throw new FunctorException(e);
   }
   
   /**
    * Overwrite this method to perform processing which throws checked exceptions.
    * @param obj passed from {@link #transform(Object)}
    * @return result of transformation.
    * @throws Exception any checked exceptions to be wrapped in
    * {@link FunctorException}. 
    */
   protected abstract Object doTransform(Object obj) throws Exception;
}

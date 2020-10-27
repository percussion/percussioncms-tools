/******************************************************************************
 *
 * [ PSMultiOperationStrategy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;

/**
 * <p>Strategy for handling multiple operations as specified in
 * {@link PSMultiOperationException}. Implemented as transformer
 * decorator adding validation for multiple runs of the wrapped
 * transformer. 
 * If the decorated transformer completes successfully the decorator returns
 * its result.
 * If the transformer throws {@link FunctorException} then it returns the
 * exception wrapped in {@link FunctorException}. If there is no root cause
 * the decorator returns {@link FunctorException} itself.</p>
 * <p>Consider using separate transformer such as
 * {@link com.percussion.client.proxies.impl.PSMultiOperationExceptionStrategy}
 * to simplify implementation of operations throwing checked exceptions.</p>
 * <p>Usage example:
 * <pre>
 *    final Object[] inData = ...
 *    // operation to perform over inData elements
 *    final Transformer operation = ...;
 *    final PSMultiOperationStrategy transformer =
 *          new PSMultiOperationStrategy(operation);
 *    final List results = TransformedList.decorate(new ArrayList(), transformer);
 *    
 *    // run the actual transformation
 *    results.addAll(Arrays.asList(inData));
 *    // return results or through an exception
 *    return transformer.validateResult(results.toArray());
 * </pre>
 * </p>
 *
 * @author Andriy Palamarchuk
 */
public class PSMultiOperationStrategy implements Transformer
{
   public PSMultiOperationStrategy(final Transformer transformer)
   {
      assert transformer != null;
      m_transformer = transformer;
   }
   
   /**
    * Calls decorated transformer, catches and handles {@link FunctorException}
    * according to the class contract.
    * As a result {@link FunctorException} is never thrown by this
    * method implementation. See class description for details.
    * Passes through any other exceptions.
    * @param obj data to pass to the decorated transformer. 
    */
   public Object transform(Object obj)
   {
      try
      {
         return m_transformer.transform(obj);
      }
      catch (FunctorException e)
      {
         m_multiOperationFailed = true;
         return e.getCause() == null ? e : e.getCause();
      }
   }

   /**
    * Called to evaluate all the transformation this decorator performed.
    * If any of the transformations failed as indicated by method
    * {@link #isMultiOperationFailed()} the method throws 
    * {@link PSMultiOperationException}. Otherwise the method returns
    * its result parameter.
    * @param results data to construct PSMultiOperationException with or return
    * @return the result parameter.
    * @throws PSMultiOperationException constructed with provided result if
    * {@link #isMultiOperationFailed()} returns <code>true</code>.
    */
   public Object[] validateResult(Object[] results)
         throws PSMultiOperationException
   {
      if (m_multiOperationFailed)
      {
         throw new PSMultiOperationException(results);
      }
      return results;
   }

   /**
    * Returns <code>true</code> if any of the operations run
    * by this transformer threw an exception.
    */
   public boolean isMultiOperationFailed()
   {
      return m_multiOperationFailed;
   }

   /**
    * @see #isMultiOperationFailed()
    */
   private boolean m_multiOperationFailed;

   /**
    * Transformer to be called by the decorator.
    */
   private final Transformer m_transformer;
}


/******************************************************************************
 *
 * [ PSMultiOperationExceptionStrategyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.PSModelException;
import com.percussion.client.models.PSLockException;
import com.percussion.conn.IPSConnectionErrors;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSNotLockedException;
import junit.framework.TestCase;
import org.apache.commons.collections.FunctorException;

public class PSMultiOperationExceptionStrategyTest extends TestCase
{
   public void testTransform()
   {
      // unchecked exception
      {
         final Exception uncheckedException = new RuntimeException();
         try
         {
            new TestPSCatchExceptionTransformer()
            {
               @Override
               protected Object doTransform(
                     @SuppressWarnings("unused") Object obj) throws Exception
               {
                  throw uncheckedException;
               }
            }.transform(null);
            fail();
         }
         catch (Exception e)
         {
            assertSame(e, uncheckedException);
         }
      }

      // checked exception
      {
         final Exception checkedException = new Exception();
         try
         {
            new TestPSCatchExceptionTransformer()
            {
               @Override
               protected Object doTransform(
                     @SuppressWarnings("unused") Object obj) throws Exception
               {
                  throw checkedException;
               }
            }.transform(null);
            fail();
         }
         catch (FunctorException e)
         {
            assertSame(e.getCause(), checkedException);
         }
      }
      
      // exceptions mapping
      checkExceptionMapped(
            new PSServerException(IPSConnectionErrors.NULL_SOCKET)
            {
               private static final long serialVersionUID = 1L;

               @Override
               public String getLocalizedMessage()
               {
                  // overwritten to suppress logging messages
                  return "dummy message";
               }
            },
            PSModelException.class);
      checkExceptionMapped(
            new PSNotLockedException(IPSConnectionErrors.NULL_SOCKET)
            {
               private static final long serialVersionUID = 1L;

               @Override
               public String getLocalizedMessage()
               {
                  // overwritten to suppress logging messages
                  return "dummy message";
               }
            },
            PSLockException.class);
   }

   /**
    * Makes sure the lower-level exception is mapped to client exception.
    */
   private void checkExceptionMapped(final Exception checkedException,
         final Class exceptionClassMappedTo)
   {
      final Object result = new PSMultiOperationExceptionStrategy()
      {
         @Override
         protected Object doTransform(
               @SuppressWarnings("unused") Object obj) throws Exception
         {
            throw checkedException;
         }
         
         @SuppressWarnings("unchecked")
         @Override
         Object passExceptionUp(Exception e)
         {
            // do not throw the exception, return a marker instead
            assertTrue(exceptionClassMappedTo.toString() +
                  " is not superclass of " + e.getClass(),
                  exceptionClassMappedTo.isAssignableFrom(e.getClass()));
            return Boolean.TRUE;
         }

         @Override
         protected PSLockException constructLockException(
               @SuppressWarnings("unused") PSNotLockedException e)
         {
            return new PSLockException("operation", null, "object name");
         }
      }.transform(null);
      assertEquals("Was called", Boolean.TRUE, result);
   }
   
   /**
    * Convenience implementation for tests.
    */
   private abstract static class TestPSCatchExceptionTransformer extends PSMultiOperationExceptionStrategy
   {

      @Override
      protected PSLockException constructLockException(
            @SuppressWarnings("unused") PSNotLockedException e)
      {
         throw new AssertionError();
      }
   }
}

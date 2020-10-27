/******************************************************************************
 *
 * [ PSMultiOperationExceptionTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.impl.PSReference;
import junit.framework.TestCase;

public class PSMultiOperationExceptionTest extends TestCase
{
   /**
    * Tests constructor parameter validation.
    */
   public void testConstructor()
   {
      final Object o = new Object();
      new PSMultiOperationException(new Object[] {o}, null);
      new PSMultiOperationException(new Object[] {o}, new Object[] {null});
      new PSMultiOperationException(new Object[] {o}, new Object[] {"detail"});
      new PSMultiOperationException(new Object[] {o},
            new Object[] {new PSReference()});

      assertNewFails(null, new Object[] {"detail"});
      assertNewFails(new Object[] {}, new Object[] {});
      assertNewFails(new Object[] {o}, new Object[] {o});
   }
   
   public void testGetCause()
   {
      final Object o = new Object();
      final Exception e1 = new Exception();
      final Exception e2 = new Exception();
      
      assertEquals(null, create(o).getCause());
      assertEquals(e1, create(o, e1, e2).getCause());
      assertEquals(e2, create(e2, o, e1).getCause());
   }

   /**
    * Asserts that
    * {@link PSMultiOperationException#PSMultiOperationException(Object[],
    * Object[])} fails with <code>IllegalArgumentException</code>.
    * @param results the results parameter to the exception constructor.
    * @param details the details parameter to the exception constructor.
    */
   private void assertNewFails(final Object[] results, final Object[] details)
   {
      try
      {
         create(results, details);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }

   /**
    * Creates new exception with the specified parameters.
    * Convenience method to call the exception constructor.
    */
   private PSMultiOperationException create(final Object[] results,
         final Object[] details)
   {
      return new PSMultiOperationException(results, details);
   }

   /**
    * Creates new exception with the specified parameters.
    * Convenience method to call
    * {@link PSMultiOperationException#PSMultiOperationException(Object[])}.
    */
   private PSMultiOperationException create(final Object... results)
   {
      return new PSMultiOperationException(results);
   }
}

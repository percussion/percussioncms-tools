/******************************************************************************
 *
 * [ PSMultiOperationStrategyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import junit.framework.TestCase;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;

public class PSMultiOperationStrategyTest extends TestCase
{
   public void testTransform()
   {
      final CallOtherTransformer transformer = new CallOtherTransformer();
      final PSMultiOperationStrategy decorator =
         new PSMultiOperationStrategy(transformer);
      assertFalse(decorator.isMultiOperationFailed());
      
      // successful transformation
      {
         final Object o = new Object();
         transformer.m_transformer = TransformerUtils.constantTransformer(o);
         assertEquals(o, decorator.transform(null));
         assertFalse(decorator.isMultiOperationFailed());
      }
      
      // transformation failed with nested exception
      {
         final RuntimeException e = new RuntimeException();
         transformer.m_transformer = new Transformer()
         {
            @SuppressWarnings("unused")
            public Object transform(Object obj)
            {
               throw new FunctorException(e);
            }
         };
         assertEquals(e, decorator.transform(null));
         assertTrue(decorator.isMultiOperationFailed());
      }
      
      // transformation failed without nested exception
      {
         final FunctorException functorException = new FunctorException();
         transformer.m_transformer = new Transformer()
         {
            @SuppressWarnings("unused")
            public Object transform(Object arg0)
            {
               throw functorException;
            }
         };
         assertEquals(functorException, decorator.transform(null));
         assertTrue(decorator.isMultiOperationFailed());
      }
   }
   
   public void testValidateResults() throws PSMultiOperationException
   {
      final CallOtherTransformer transformer = new CallOtherTransformer();
      final PSMultiOperationStrategy decorator =
         new PSMultiOperationStrategy(transformer);
      final Object[] resultArray = new Object[] {new Object(), new Object()};
      
      // initial state - everything is Ok
      assertFalse(decorator.isMultiOperationFailed());
      assertSame(resultArray, decorator.validateResult(resultArray));
      // makes sure that method which returns array is called
      assertSame(resultArray[0], decorator.validateResult(resultArray)[0]);
      
      // successful transformation
      {
         final Object o = new Object();
         transformer.m_transformer = TransformerUtils.constantTransformer(o);
         decorator.transform(null);
         assertFalse(decorator.isMultiOperationFailed());
         assertSame(resultArray, decorator.validateResult(resultArray));
         assertSame(resultArray[0], decorator.validateResult(resultArray)[0]);
      }

      // unsuccessful transformation
      {
         transformer.m_transformer = TransformerUtils.exceptionTransformer();
         decorator.transform(null);

         try
         {
            decorator.validateResult(resultArray);
            fail();
         }
         catch (PSMultiOperationException e)
         {
            assertSame(resultArray, e.getResults());
         }
      }
   }

   /**
    * Calls provided transformer. Used for testing.
    */
   private static class CallOtherTransformer implements Transformer
   {
      /**
       * Calls {@link #m_transformer}.
       */
      public Object transform(Object obj)
      {
         return m_transformer.transform(obj);
      }

      /**
       * Transformer to be called by {@link #transform(Object)}.
       */
      private Transformer m_transformer;
   }
}

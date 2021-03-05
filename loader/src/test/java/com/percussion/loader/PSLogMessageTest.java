/******************************************************************************
 *
 * [ PSLogMessageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader;

import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;
import static com.percussion.loader.PSLogMessage.LEVEL_INFO;
import static com.percussion.loader.PSLogMessage.LEVEL_ERROR;

import junit.framework.TestCase;

public class PSLogMessageTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSLogMessage message =
            new PSLogMessage(MESSAGE_CODE1, new Object[] {}, LEVEL_INFO);
      assertFalse(message.equals(new Object()));
      assertEqualsWithHash(message,
            new PSLogMessage(MESSAGE_CODE1, new Object[] {}, LEVEL_INFO));
      
      assertFalse(message.equals(
            new PSLogMessage(MESSAGE_CODE2, new Object[] {}, LEVEL_INFO)));
      assertFalse(message.equals(
            new PSLogMessage(MESSAGE_CODE1, new Object[] {}, LEVEL_ERROR)));
   }

   /**
    * Sample message code.
    */
   private static final int MESSAGE_CODE1 = 10;

   /**
    * Sample message code.
    */
   private static final int MESSAGE_CODE2 = 133;
}

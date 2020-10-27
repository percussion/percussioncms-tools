/******************************************************************************
 *
 * [ PSObjectTypeFactoryTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import junit.framework.TestCase;

public class PSObjectTypeFactoryTest extends TestCase
{
   /**
    * Ctor required by base class.
    */
   public PSObjectTypeFactoryTest(String name)
   {
      super(name);
   }

   /**
    * Creates some different types and verifies that the returned type is the
    * same instance each time.
    */
   public void testGetType()
   {
      PSObjectType t1, t2, t3;

      //contract validation
      try
      {
         t1 = PSObjectTypeFactory.getType((Enum) null);
         fail("Contract violation not caught");
      }
      catch (IllegalArgumentException success)
      {}
      
      try
      {
         t1 = PSObjectTypeFactory.getType((String) null);
         fail("Contract violation not caught");
      }
      catch (IllegalArgumentException success)
      {}
      
      
      t1 = PSObjectTypeFactory.getType(PSObjectTypes.SLOT);
      t2 = PSObjectTypeFactory.getType(PSObjectTypes.SLOT);
      assertTrue(t1 == t2);
      
      t3 = PSObjectTypeFactory.getType(t1.toSerial());
      assertTrue(t1 == t3);
      
      t1 = PSObjectTypeFactory.getType(PSObjectTypes.CONFIGURATION_FILE,
            PSObjectTypes.ConfigurationFileSubTypes.LOGGER_PROPERTIES);
      t2 = PSObjectTypeFactory.getType(PSObjectTypes.CONFIGURATION_FILE,
            PSObjectTypes.ConfigurationFileSubTypes.LOGGER_PROPERTIES);
      t3 = PSObjectTypeFactory.getType(PSObjectTypes.CONFIGURATION_FILE,
            PSObjectTypes.ConfigurationFileSubTypes.NAVIGATION_PROPERTIES);
      assertTrue(t1 == t2);
      assertFalse(t1 == t3);
      t2 = PSObjectTypeFactory.getType(PSObjectTypes.CONFIGURATION_FILE,
            PSObjectTypes.ConfigurationFileSubTypes.LOGGER_PROPERTIES);
      assertTrue(t1 == t2);
   }
   
}

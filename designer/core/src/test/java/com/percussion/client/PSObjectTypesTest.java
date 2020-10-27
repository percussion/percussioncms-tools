/******************************************************************************
 *
 * [ PSObjectTypesTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import junit.framework.TestCase;

/**
 * Runs basic consistency checks against the enumeration class. 
 *
 * @author paulhoward
 */
public class PSObjectTypesTest extends TestCase
{
   /**
    * Walks thru all types and verifies that for every sub type, the primary
    * type considers it an allowed type.
    */
   public void testSubTypeConsistency()
   {
      for (PSObjectTypes type : PSObjectTypes.values())
      {
         int count = 0;
         if (type.hasSubTypes())
         {
            for (Enum subType : type.getSubTypeValues())
            {
               assertTrue(type.isAllowedType(subType));
               count++;
            }
         }
         if (count == 0)
            count++;
         assertEquals(count, type.getTypes().size());
      }
   }
   
   /**
    * Verify that types with sub types are indicating they have them.
    */
   public void testSubTypePresence()
   {
      for (PSObjectTypes type : PSObjectTypes.values())
      {
         assertEquals(type.hasSubTypes(), type.getSubTypeValues().length > 0);
      }
   }
   
   /**
    * Performs a couple of random negative checks.
    */
   public void testSubTypeFailures()
   {
      assertFalse(PSObjectTypes.AUTO_TRANSLATION_SET
            .isAllowedType(PSObjectTypes.FileSubTypes.FILE));
      assertFalse(PSObjectTypes.AUTO_TRANSLATION_SET
            .isAllowedType(PSObjectTypes.AUTO_TRANSLATION_SET));
   }
   
   /**
    * Verifies that the sub types for <code>LOCAL_FILE</code> are correct.
    */
   public void testSubTypeAccuracy()
   {
      for (Enum subType : PSObjectTypes.LOCAL_FILE.getSubTypeValues())
      {
         if ((subType.equals(PSObjectTypes.FileSubTypes.FILE)
               || (subType == PSObjectTypes.FileSubTypes.FOLDER)))
         {
            continue;
         }
         assertTrue(false);
      }
   }
}

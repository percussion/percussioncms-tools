/******************************************************************************
 *
 * [ PSMappingElementTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import junit.framework.TestCase;

import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

/**
 * Test case for {@link PSMappingElement}.
 *
 * @author Andriy Palamarchuk
 */
public class PSMappingElementTest extends TestCase
{
   /**
    * Tests no-parameter constructor.
    */
   public void testNoParamCreate()
   {
      final PSMappingElement e = new PSMappingElement();
      assertNull(e.getId());
      assertNull(e.getName());
      assertNull(e.getType());
      assertFalse(e.hasParent());
      assertNull(e.getParentId());
      assertNull(e.getParentName());
      assertNull(e.getParentType());
   }
   
   /**
    * Tests main constructor
    */
   public void testCreate()
   {
      final PSMappingElement e = new PSMappingElement(TYPE, ID, NAME);
      assertEquals(TYPE, e.getType());
      assertEquals(ID, e.getId());
      assertEquals(NAME, e.getName());
   }
   
   /**
    * Main constructor parameter validation.
    */
   public void testCreateFailure()
   {
      try {
         new PSMappingElement(null, ID, NAME);
         fail();
      }
      catch (IllegalArgumentException success) {}
      try {
         new PSMappingElement("   ", ID, NAME);
         fail();
      }
      catch (IllegalArgumentException success) {}
      try {
         new PSMappingElement(TYPE, null, NAME);
         fail();
      }
      catch (IllegalArgumentException success) {}
      try {
         new PSMappingElement(TYPE, "  ", NAME);
         fail();
      }
      catch (IllegalArgumentException success) {}
      try {
         new PSMappingElement(TYPE, ID, null);
         fail();
      }
      catch (IllegalArgumentException success) {}
      try {
         new PSMappingElement(TYPE, ID, "  ");
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
   
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSMappingElement element = new PSMappingElement(TYPE, ID, NAME);
      {
         final PSMappingElement e2 = new PSMappingElement(TYPE, ID, NAME);
         assertEqualsWithHash(element, e2);
      }
      {
         final PSMappingElement e2 = new PSMappingElement(OTHER_STR, ID, NAME);
         assertFalse(element.equals(e2));
      }
      {
         final PSMappingElement e2 = new PSMappingElement(TYPE, OTHER_STR, NAME);
         assertFalse(element.equals(e2));
      }
      {
         final PSMappingElement e2 = new PSMappingElement(TYPE, ID, OTHER_STR);
         assertFalse(element.equals(e2));
      }

      element.setParent(PARENT_TYPE, PARENT_ID, PARENT_NAME);
      final PSMappingElement e2 = new PSMappingElement(TYPE, ID, NAME);
      
      e2.setParent(PARENT_TYPE, PARENT_ID, PARENT_NAME);
      assertEqualsWithHash(element, e2);

      e2.setParent(OTHER_STR, PARENT_ID, PARENT_NAME);
      assertFalse(element.equals(e2));

      e2.setParent(PARENT_TYPE, OTHER_STR, PARENT_NAME);
      assertFalse(element.equals(e2));

      e2.setParent(PARENT_TYPE, PARENT_ID, OTHER_STR);
      assertFalse(element.equals(e2));
}

   /**
    * Sample type string.
    */
   private static final String TYPE = "Type";

   /**
    * Sample ID.
    */
   private static final String ID = "Id!";
   
   /**
    * Sample name.
    */
   private static final String NAME = "Name";

   /**
    * Sample parent type string.
    */
   private static final String PARENT_TYPE = "Parent Type";

   /**
    * Sample parent ID.
    */
   private static final String PARENT_ID = "Parent Id!";
   
   /**
    * Sample parent name.
    */
   private static final String PARENT_NAME = "Parent Name";

   /**
    * Sample string.
    */
   private static final String OTHER_STR = "Other String";
}

/******************************************************************************
 *
 * [ PSObjectTypeTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test cases for the {@link PSObjectType} class.
 *
 * @author paulhoward
 */
public class PSObjectTypeTest extends TestCase
{
   public PSObjectTypeTest(String name)
   {
      super(name);
   }

   public static TestSuite suite()
   {
      return new TestSuite(PSObjectTypeTest.class);
   }

   /**
    * Create new instances using various combinations. Include negative tests
    * too.
    */
   public void testCtors()
   {
      //basic ctors
      new PSObjectType(PSObjectTypes.AUTO_TRANSLATION_SET);
      new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
            PSObjectTypes.FileSubTypes.FOLDER);
      
      try
      {
         //this should fail because it's not an Enum
         new PSObjectType(new IPSPrimaryObjectType()
         {
            private static final long serialVersionUID = 1L;

            public boolean hasSubTypes()
            {
               return false;
            }

            @SuppressWarnings("unused")
            public boolean isAllowedType(Enum subType)
            {
               return false;
            }
            
            public Set<PSObjectType> getTypes()
            {
               return null;
            }
            
            public String name()
            {
               return null;
            }

            public boolean isFileType()
            {
               return false;
            }
            
            public boolean supportsAcls()
            {
               return false;
            }
         });
         assertTrue(false);
      }
      catch (IllegalArgumentException e)
      {
         //successful
      }
      
      try
      {
         //invalid sub type
         new PSObjectType(PSObjectTypes.AUTO_TRANSLATION_SET, 
               PSObjectTypes.FileSubTypes.FILE);
         assertTrue(false);
      }
      catch (IllegalArgumentException e)
      {
         //successful
      }
   }
   
   /**
    * Check positive and negative cases.
    */
   public void testEquals()
   {
      //types w/o sub types
      PSObjectType type4 = new PSObjectType(PSObjectTypes.AUTO_TRANSLATION_SET);
      PSObjectType type5 = new PSObjectType(PSObjectTypes.AUTO_TRANSLATION_SET);
      PSObjectType type6 = new PSObjectType(PSObjectTypes.LOCALE);
      assertTrue(type4.equals(type5));
      assertFalse(type4.equals(type6));
      
      //types w/ differing sub types
      PSObjectType type1 = new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
            PSObjectTypes.FileSubTypes.FOLDER);
      PSObjectType type2 = new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
            PSObjectTypes.FileSubTypes.FOLDER);
      PSObjectType type3 = new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
            PSObjectTypes.FileSubTypes.FILE);
      assertTrue(type1.equals(type2));
      assertFalse(type1.equals(type3));
   }
   
   /**
    * Verify that objects that are equal give same hash code.
    */
   public void testHash()
   {
      //types w/o sub types
      PSObjectType type4 = new PSObjectType(PSObjectTypes.AUTO_TRANSLATION_SET);
      PSObjectType type5 = new PSObjectType(PSObjectTypes.AUTO_TRANSLATION_SET);
      assertFalse(type4 == type5);
      assertTrue(type4.hashCode() == type5.hashCode());
      
      //types w/ sub types
      PSObjectType type1 = new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
            PSObjectTypes.FileSubTypes.FOLDER);
      PSObjectType type2 = new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
            PSObjectTypes.FileSubTypes.FOLDER);
      assertFalse(type1 == type2);
      assertTrue(type1.hashCode() == type2.hashCode());
   }
   
   /**
    * Tests that serialization works and that de-serialization works with
    * different problem strings.
    */
   public void testSerialization()
   {
      //proper use
      PSObjectType type1 = new PSObjectType(PSObjectTypes.AUTO_TRANSLATION_SET);
      String serialized = type1.toSerial();
      PSObjectType type2 = new PSObjectType(serialized);
      assertTrue(type1.equals(type2));

      type1 = new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
            PSObjectTypes.FileSubTypes.FOLDER);
      serialized = type1.toSerial();
      type2 = new PSObjectType(serialized);
      assertTrue(type1.equals(type2));
      
      // invalid use, but valid forms
      String[] serialForms = 
      {
            "  com.percussion.client.PSObjectTypes$1 :  RESOURCE_FILE   ",
            "com.percussion.client.PSObjectTypes$1:RESOURCE_FILE:  com.percussion.client.PSObjectTypes$FileSubTypes : FILE ",
      };
      for (String form : serialForms)
      {
         new PSObjectType(form);
      }

      // invalid use, invalid forms
      String[] serialForms2 = 
      {
            "com.percussion.client.PSObjectTypes$1",
            "com.percussion.client.PSObjectTypes$1:",
            "com.percussion.client.PSObjectTypes$1:abc",
            "percussion.client.PSObjectTypes$1:RESOURCE_FILE",

            "com.percussion.client.PSObjectTypes$1:RESOURCE_FILE: ",
            "com.percussion.client.PSObjectTypes$1:RESOURCE_FILE:com.percussion.client.PSObjectTypes$FileSubTypes",
            "com.percussion.client.PSObjectTypes$1:RESOURCE_FILE:com.percussion.client.PSObjectTypes$FileSubTypes:",
            "com.percussion.client.PSObjectTypes$1:RESOURCE_FILE:percussion.client.PSObjectTypes$FileSubTypes:FILE",
            "com.percussion.client.PSObjectTypes$1:RESOURCE_FILE:com.percussion.client.PSObjectTypes$FileSubTypes:abc",
      };
      for (String form : serialForms2)
      {
         try
         {
            new PSObjectType(form);
            assertTrue(false);
         }
         catch (Exception e)
         {
            //expect failure
         }
      }
      
      //mixed known and unknown
      type1 = new PSObjectType(TestType.CONTENT_TYPE2);
      serialized = type1.toSerial();
      type2 = new PSObjectType(serialized);
      assertTrue(type1.equals(type2));
      
      type1 = new PSObjectType(TestType.MY_RESOURCE_FILE,
            TestType.MyFileSubTypes.MYFILE);
      serialized = type1.toSerial();
      type2 = new PSObjectType(serialized);
      assertTrue(type1.equals(type2));

      type1 = new PSObjectType(TestType.RESOURCE_FILE2,
            PSObjectTypes.FileSubTypes.FOLDER);
      serialized = type1.toSerial();
      type2 = new PSObjectType(serialized);
      assertTrue(type1.equals(type2));
   }
   
   /**
    * Used for testing serialization of an unknown object type.
    *
    * @author paulhoward
    */
   enum TestType implements IPSPrimaryObjectType
   {
      /**
       * No sub types.
       */
      CONTENT_TYPE2,

      /**
       * Local sub types.
       */
      MY_RESOURCE_FILE
      {
         // see base class method for details
         @Override
         protected Enum[] getSubTypeValues()
         {
            return MyFileSubTypes.values();
         }
      },

      /**
       * Sub types from {@link PSObjectTypes}.
       */
      RESOURCE_FILE2
      {
         // see base class method for details
         @Override
         protected Enum[] getSubTypeValues()
         {
            return PSObjectTypes.FileSubTypes.values();
         }
      };

      //see base class method for details
      public boolean isFileType()
      {
         return false;
      }
      
//    see base class method for details
      public boolean supportsAcls()
      {
         return false;
      }

      // see interface for details
      public boolean hasSubTypes()
      {
         return getSubTypeValues().length > 0;
      }

      // see interface for details
      public Set<PSObjectType> getTypes()
      {
         Set<PSObjectType> results = new HashSet<PSObjectType>();
         if (hasSubTypes())
         {
            for (Enum subType : getSubTypeValues())
            {
               results.add(new PSObjectType(this, subType));
            }
         }
         else
            results.add(new PSObjectType(this, null));
         return Collections.unmodifiableSet(results);
      }

      // see interface for details
      public boolean isAllowedType(Enum subType)
      {
         for (Enum existingType : getSubTypeValues())
         {
            if (existingType == subType)
               return true;
         }
         return false;
      }

      /**
       * Default behavior is to return an empty array. Types with sub-types must
       * override this method and return all the sub type values. All other methods
       * in the interface are implemented based on this method.
       * 
       * @return Never <code>null</code>, default implementation is always
       * empty.
       */
      protected Enum[] getSubTypeValues()
      {
         return new Enum[0];
      }

      /**
       * Defines the sub types for {@link #MY_RESOURCE_FILE}.
       */
      enum MyFileSubTypes
      {
         MYFILE, 
         MYFOLDER
      }      
   }
}

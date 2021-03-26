/******************************************************************************
 *
 * [ PSReferenceTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.impl;

import com.percussion.client.IPSReference.LabelKeyComparator;
import com.percussion.client.IPSReference.NameKeyComparator;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import junit.framework.TestCase;
import org.apache.commons.lang.RandomStringUtils;

public class PSReferenceTest extends TestCase
{
   /**
    * Creates new object, waits a few seconds and validates the age.
    */
   public void testAge()
      throws Exception
   {
      PSReference r = new PSReference("test", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), null);
      Thread.sleep(5000);
      assertTrue(r.getAge() >= 4 && r.getAge() <= 6);
      r.refresh();
      assertTrue(r.getAge() < 2);
   }
   
   /**
    * Creates various conditions of equality and ids and names being equal or
    * not and verifies the method behaves correctly.
    */
   public void testReferencesSameObject()
      throws PSModelException
   {
      PSReference r1 = new PSReference("test", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), new PSGuid(0, PSTypeEnum.SLOT, 10));
      PSReference r2 = new PSReference("test", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), new PSGuid(0, PSTypeEnum.SLOT, 10));
      PSReference r3 = new PSReference("test", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), new PSGuid(0, PSTypeEnum.SLOT, 11));
      PSReference r4 = new PSReference("test2", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), new PSGuid(0, PSTypeEnum.SLOT, 10));
      PSReference r5 = new PSReference("test2", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), new PSGuid(0, PSTypeEnum.SLOT, 11));
      PSReference r6 = new PSReference("test", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), null);
      PSReference r7 = new PSReference("test", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), null);
      PSReference r8 = new PSReference("test2", "key", null, new PSObjectType(
            PSObjectTypes.SLOT), null);
      
      assertTrue(r1.referencesSameObject(r2));
      assertFalse(r1.referencesSameObject(r3));
      assertTrue(r1.referencesSameObject(r4));
      assertFalse(r1.referencesSameObject(r5));
      assertFalse(r1.referencesSameObject(r6));
      assertTrue(r6.referencesSameObject(r7));
      assertFalse(r6.referencesSameObject(r8));
   }
   
   /**
    * Tests {@link PSReference#getName()} and
    * {@link PSReference#setLabelKey(String)}.
    */
   public void testGetSetName()
   {
      final PSReference r = new PSReference();

      // everything is ok
      final String name = "name";
      r.setName(name);
      assertEquals(name, r.getName());

      // name is empty
      checkIllegalName(r, null);
      checkIllegalName(r, "");
      checkIllegalName(r, " ");

      // fixme uncomment when the check is enabled
      // name with spaces
      final String nameWithSpaces = "name with spaces";
//      try
//      {
//         r.setName(nameWithSpaces);
//         fail();
//      }
//      catch (IllegalArgumentException success)
//      {
//      }

      try
      {
         r.setName(nameWithSpaces, false);
         fail();
      }
      catch (IllegalArgumentException success)
      {
      }
      assertEquals(name, r.getName());
      r.setName(nameWithSpaces, true);
      assertEquals(nameWithSpaces, r.getName());
   }

   /**
    * Makes sure this name is illegal.
    */
   private void checkIllegalName(final PSReference r, final String name)
   {
      try
      {
         r.setName(name);
         fail();
      }
      catch (IllegalArgumentException success)
      {
      }

      try
      {
         r.setName(name, true);
         fail();
      }
      catch (IllegalArgumentException success)
      {
      }
   }

   /**
    * Tests {@link PSReference#getLabelKey()} and
    * {@link PSReference#setLabelKey(String)}.
    */
   public void testGetSetLabelKey()
   {
      final PSReference r = new PSReference();

      r.setName(NAME);
      assertEquals(NAME, r.getLabelKey());

      r.setLabelKey(LABEL_KEY);
      assertEquals(LABEL_KEY, r.getLabelKey());

      r.setLabelKey(null);
      assertEquals(NAME, r.getLabelKey());

      r.setLabelKey("");
      assertEquals(NAME, r.getLabelKey());

      r.setLabelKey(" ");
      assertEquals(NAME, r.getLabelKey());

      r.setLabelKey(LABEL_KEY);
      assertEquals(LABEL_KEY, r.getLabelKey());
   }

   public void testToString() throws PSModelException
   {
      final long SITE_ID = 11111;
      final long UUID = 5432;
      final String DESCRIPTION = "DESCRIPTION " + RandomStringUtils.random(2);
      final IPSGuid id = new PSGuid(SITE_ID, PSTypeEnum.INTERNAL, UUID);
      final PSObjectType objectType = new PSObjectType(
            PSObjectTypes.RESOURCE_FILE, PSObjectTypes.FileSubTypes.FILE);

      final PSReference r = new PSReference(NAME, LABEL_KEY, DESCRIPTION,
            objectType, id);
      assertTrue(r.toString().contains(r.getClass().getName()));
      assertTrue(r.toString().contains(NAME));
      assertFalse(r.toString().contains(LABEL_KEY));
      assertFalse(r.toString().contains(DESCRIPTION));
      assertTrue(r.toString().contains(objectType.toString()));
      assertTrue(r.toString().contains(id.toString()));
   }
   
   public void testLabelKeyComparator()
   {
      final PSReference r0 = new PSReference();
      r0.setName("name0");
      
      final PSReference r1 = new PSReference();
      r1.setName("name0");
      
      final LabelKeyComparator cmp = new LabelKeyComparator();
      assertEquals(0, cmp.compare(r0, r1));
      
      r1.setName("name1");
      assertTrue(cmp.compare(r0, r1) < 0);
      
      r0.setLabelKey("b");
      r1.setLabelKey("a");
      assertTrue(cmp.compare(r0, r1) > 0);
   }

   public void testNameKeyComparator()
   {
      final PSReference r0 = new PSReference();
      r0.setName("name0");
      
      final PSReference r1 = new PSReference();
      r1.setName("name0");
      
      final NameKeyComparator cmp = new NameKeyComparator();
      assertEquals(0, cmp.compare(r0, r1));
      
      r1.setName("name1");
      assertTrue(cmp.compare(r0, r1) < 0);
      
      r0.setName("b");
      r1.setName("a");
      assertTrue(cmp.compare(r0, r1) > 0);
   }
   
   /**
    * Sample name value.
    */
   private static final String NAME = "NAME"
         + RandomStringUtils.randomAlphabetic(2);

   /**
    * Sample label key value.
    */
   private static final String LABEL_KEY = "LABEL KEY "
         + RandomStringUtils.random(2);
}

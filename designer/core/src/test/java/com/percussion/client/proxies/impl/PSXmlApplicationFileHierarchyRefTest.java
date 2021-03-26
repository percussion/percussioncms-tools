/******************************************************************************
 *
 * [ PSXmlApplicationFileHierarchyRefTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.PSModelException;
import com.percussion.design.objectstore.PSApplication;
import junit.framework.TestCase;

public class PSXmlApplicationFileHierarchyRefTest extends TestCase
{
   public void testGetApplication() throws PSModelException
   {
      final PSApplication application = new PSApplication("name") {};
      final PSXmlApplicationFileHierarchyRef ref1 =
            new PSTestXmlApplicationFileHierarchyRef(null, "name", true)
      {
         private static final long serialVersionUID = 1L;

         @Override
         public PSApplication getApplication()
         {
            return application;
         }
      };
      final PSXmlApplicationFileHierarchyRef ref2 =
            new PSTestXmlApplicationFileHierarchyRef(ref1, "name2", true);
      assertEquals(application, ref2.getApplication());
   }
   
   public void testGetFilePath() throws PSModelException
   {
      final PSXmlApplicationFileHierarchyRef ref1 =
            new PSTestXmlApplicationFileHierarchyRef(null, "name", true);
      assertEquals("", ref1.getFilePath());

      final PSTestXmlApplicationFileHierarchyRef ref2 =
            new PSTestXmlApplicationFileHierarchyRef(ref1, "dir2", true);
      assertEquals("dir2", ref2.getFilePath());

      final PSXmlApplicationFileHierarchyRef ref3 =
            new PSTestXmlApplicationFileHierarchyRef(ref2, "dir3", true);
      assertEquals("dir2/dir3", ref3.getFilePath());
   }
   
   public void testLocking() throws PSModelException
   {
      final PSXmlApplicationFileHierarchyRef ref1 =
            new PSTestXmlApplicationFileHierarchyRef(null, "name", true)
      {
         private static final long serialVersionUID = 1L;

         @Override
         protected void setLock(
               @SuppressWarnings("unused") PSXmlApplicationFileHierarchyRef ref,
               boolean locked)
         {
            assertEquals(expectedLockValue, locked);
            lockedWasCalled = true;
         }
      };
      final PSXmlApplicationFileHierarchyRef ref2 =
            new PSTestXmlApplicationFileHierarchyRef(ref1, "name2", true);
      
      expectedLockValue = true;
      lockedWasCalled = false;
      ref2.lock();
      assertTrue(ref2.isLocked());
      assertTrue(lockedWasCalled);

      lockedWasCalled = false;
      expectedLockValue = false;
      ref2.unlock();
      assertFalse(ref2.isLocked());
      assertTrue(lockedWasCalled);
   }
   
   public void testSetId() throws PSModelException
   {
      final PSXmlApplicationFileHierarchyRef ref1 =
            new PSTestXmlApplicationFileHierarchyRef(null, "name", true);
      try
      {
         ref1.setId(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }
   
   /**
    * Flag indicating whether setLock method was called.
    */
   private boolean lockedWasCalled;
   private boolean expectedLockValue;
}

/******************************************************************************
 *
 * [ PSXmlApplicationFileHierarchyRootRefTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.design.objectstore.PSApplication;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.jmock.cglib.CGLIBCoreMock;

public class PSXmlApplicationFileHierarchyRootRefTest extends TestCase
{
   public void testConstructor() throws PSModelException
   {
      final PSApplication application = new PSApplication(NAME) {};
      final PSXmlApplicationFileHierarchyRootRef ref =
            new PSTestXmlApplicationFileHierarchyRootRef(application);
      assertNull(ref.getParent());
      assertEquals(NAME, ref.getName());
      assertEquals(PSResourceFileModelProxy.OBJECT_TYPE_FOLDER, ref.getObjectType());
      assertTrue(ref.isContainer());
      assertTrue(ref.isReadOnly());
      assertTrue(ref.isPersisted());
      assertTrue(StringUtils.isNotBlank(ref.getDescription()));
   }
   
   public void testUnmodifiable() throws PSModelException
   {
      final PSApplication application = new PSApplication(NAME) {};
      final PSXmlApplicationFileHierarchyRootRef ref =
            new PSTestXmlApplicationFileHierarchyRootRef(application);
      // this is a virtual node and no changes are allowed
      try {
         ref.setDescription(NAME);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try {
         ref.setId(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}
      try {
         ref.setLabelKey("sss");
         fail();
      }
      catch (UnsupportedOperationException success) {}
      try {
         ref.setName(NAME);
         fail();
      }
      catch (UnsupportedOperationException success) {}
      try {
         final PSObjectType type = new PSObjectType(
               PSObjectTypes.RESOURCE_FILE, PSObjectTypes.FileSubTypes.FOLDER);
         ref.setObjectType(type);
         fail();
      }
      catch (UnsupportedOperationException success) {}
      try {
         ref.setPersisted();
         fail();
      }
      catch (UnsupportedOperationException success) {}
      try {
         ref.setReadOnly(true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      // hierarchy manager can be set only once
      final IPSHierarchyManager manager =
         (IPSHierarchyManager) new CGLIBCoreMock(IPSHierarchyManager.class).proxy();
      ref.setManager(manager);
      try {
         ref.setManager(manager);
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }
   
   public void testLocking() throws PSModelException
   {
      final PSApplication application = new PSApplication(NAME) {};
      final PSXmlApplicationFileHierarchyRootRef ref =
            new PSTestXmlApplicationFileHierarchyRootRef(application);
      final PSXmlApplicationFileHierarchyRef ref1 =
            new PSTestXmlApplicationFileHierarchyRef(ref, "name", true);

      assertFalse(ref.isTreeLocked());
      assertFalse(ref1.isTreeLocked());
      ref.lock();
      assertTrue(ref.isTreeLocked());
      assertTrue(ref1.isTreeLocked());
      ref1.lock();
      assertTrue(ref.isTreeLocked());
      assertTrue(ref1.isTreeLocked());
      ref.unlock();
      assertTrue(ref.isTreeLocked());
      assertTrue(ref1.isTreeLocked());
      ref1.unlock();
      assertFalse(ref.isTreeLocked());
      assertFalse(ref1.isTreeLocked());
   }

   /**
    * Disables model validation, so the tests can run with testing proxies.
    * Otherwise it fails because there is no testing proxy for application files.
    *
    * @author Andriy Palamarchuk 
    */
   private static class PSTestXmlApplicationFileHierarchyRootRef
         extends PSXmlApplicationFileHierarchyRootRef
   {

      public PSTestXmlApplicationFileHierarchyRootRef(PSApplication application)
            throws PSModelException
      {
         super(application);
      }

      /**
       * Always returns <code>null</code>.
       */
      @Override
      protected IPSCmsModel getModel(@SuppressWarnings("unused") Enum type)
      {
         return null;
      }
   }

   /**
    * Sample name.
    */
   private static final String NAME = "NAME";
}

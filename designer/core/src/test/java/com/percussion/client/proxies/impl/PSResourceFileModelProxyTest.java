/******************************************************************************
 *
 * [ PSResourceFileModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.conn.IPSConnectionErrors;
import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSMimeContentAdapter;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.security.PSAuthenticationFailedException;
import org.apache.commons.lang.RandomStringUtils;
import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class PSResourceFileModelProxyTest extends MockObjectTestCase

{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSCoreFactory.getInstance().setClientSessionId(CLIENT_SESSION_ID);
   }

   public void testConstructor()
   {
      final PSResourceFileModelProxy proxy = new PSTestResourceFileModelProxy();
      assertEquals(PSObjectTypes.RESOURCE_FILE, proxy.getPrimaryType());
   }

   public void testCatalog() throws PSModelException
   {
      // unsupported because model represents hierarchy
      // see the method definition for details
      try
      {
         new PSTestResourceFileModelProxy().catalog();
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }

   public void testCreate()
      throws Exception
   {
      // unsupported because model represents hierarchy
      // see the method definition for details
      try
      {
         // create new
         new PSTestResourceFileModelProxy().create((PSObjectType) null, null,
               null);
         fail();
      }
      catch (UnsupportedOperationException success) {}
      try
      {
         // create clones
         new PSTestResourceFileModelProxy().create((IPSReference[]) null,
               null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }
   
   public void testRename() throws PSModelException, PSModelException
   {
      final PSResourceFileModelProxy proxy = new PSTestResourceFileModelProxy();
      final PSApplication application = new PSApplication(APPLICATION_NAME1) {};

      final IPSHierarchyNodeRef rootRef =
            new PSXmlApplicationFileHierarchyRootRef(application);
      final PSXmlApplicationFileHierarchyRef ref =
            new PSXmlApplicationFileHierarchyRef(rootRef, NAME1, true);
      
      // everything is Ok - renaming directory
      ref.setName(NAME1);
      m_mockObjectStore.expects(once()).method("renameApplicationFile")
            .with(eq(application),new PSApplicationFileConstraint(NAME1),
                  eq(NAME2), eq(false));
      assertFalse(ref.isTreeLocked());
      proxy.rename(ref, NAME2, null);
      assertEquals(NAME2, ref.getName());
      assertTrue(ref.isTreeLocked());
      ref.unlock();
      assertFalse(ref.isTreeLocked());

      // everything is Ok - renaming file with content
      ref.setName(NAME1);
      final PSMimeContentAdapter content = createDummyContent();
      content.setName("dir1");
      m_mockObjectStore.expects(once()).method("renameApplicationFile")
            .with(eq(application),new PSApplicationFileConstraint(NAME1),
                  eq(NAME2), eq(false));

      assertFalse(ref.isTreeLocked());
      proxy.rename(ref, NAME2, content);
      assertEquals(NAME2, ref.getName());
      assertEquals(NAME2, content.getName());
      assertTrue(ref.isTreeLocked());
   }

   /**
    * Creates sample content object.
    */
   private PSMimeContentAdapter createDummyContent()
   {
      return new PSMimeContentAdapter(new ByteArrayInputStream(new byte[0]),
            "mimeType", "encoding", "encoding", 0);
   }

   public void testLoad() throws PSModelException, PSMultiOperationException
   {
      final PSResourceFileModelProxy proxy = new PSTestResourceFileModelProxy()
      {
         @Override
         @SuppressWarnings("unused")
         public boolean isLocked(IPSReference ref)
         {
            return true;
         }
      };
      
      final PSXmlApplicationFileHierarchyRef[] refs =
            new PSXmlApplicationFileHierarchyRef[2];
      final PSApplication application1 =
            new PSApplication(APPLICATION_NAME1) {};
      {
         // reference to file in a subdirectory
         final IPSHierarchyNodeRef rootRef =
               new PSXmlApplicationFileHierarchyRootRef(application1);
         final IPSHierarchyNodeRef ref1 =
               new PSXmlApplicationFileHierarchyRef(rootRef, "dir1", true);
         refs[0] = new PSXmlApplicationFileHierarchyRef(ref1, NAME1, false);
      }

      final PSApplication application2 = new PSApplication(APPLICATION_NAME2) {};
      assertFalse(application1.equals(application2));
      {
         // reference to folder
         final IPSHierarchyNodeRef rootRef2 =
               new PSXmlApplicationFileHierarchyRootRef(application2);
         refs[1] = new PSXmlApplicationFileHierarchyRef(rootRef2, NAME2, true);
      }
      final PSMimeContentAdapter content1 = createDummyContent();
      final InputStream content1Content = content1.getContent();
      content1.setContent(content1Content);

      final PSApplicationFile file1 = new PSApplicationFile(new File("File 1"))
      {
         private static final long serialVersionUID = 1L;

         @Override
         public IPSMimeContent getContent()
         {
            return content1;
         }
      };
      final PSMimeContentAdapter content2 = createDummyContent();
      final PSApplicationFile file2 = new PSApplicationFile(new File("File 2"))
      {
         private static final long serialVersionUID = 1L;

         @Override
         public IPSMimeContent getContent()
         {
            return content2;
         }
      };
      assertFalse(file1.equals(file2));
      
      // everything is Ok
      m_mockObjectStore.expects(once()).method("loadApplicationFile")
            .with(eq(application1), new PSApplicationFileConstraint("dir1/" + NAME1)) 
            .will(returnValue(file1));
      {
         final Object[] results = proxy.load(refs, false, false);
         assertEquals(refs.length, results.length);
         assertSame(content1Content,
               ((com.percussion.services.system.data.PSMimeContentAdapter)
                     results[0]).getContent());
         // second ref is a folder
         assertSame(null, results[1]);
         assertFalse(refs[0].isTreeLocked());
         assertFalse(refs[1].isTreeLocked());
      }
      m_mockObjectStore.verify();
      
      // one of loads fails with an exception
      m_mockObjectStore.expects(once()).method("extendApplicationLock")
            .with(eq(application1), eq(true)); 
      m_mockObjectStore.expects(once()).method("loadApplicationFile")
            .with(eq(application1), new PSApplicationFileConstraint("dir1/" + NAME1)) 
            .will(throwException(EXCEPTION));
      assertFalse(refs[0].isTreeLocked());
      assertFalse(refs[1].isTreeLocked());
      try
      {
         proxy.load(refs, true, true);
         fail();
      }
      catch (PSMultiOperationException e)
      {
         final Object[] results = e.getResults();
         assertEquals(refs.length, results.length);
         
         assertEquals(EXCEPTION, results[0]);
         assertNull(results[1]);
      }
      assertFalse(refs[0].isTreeLocked());
      assertFalse(refs[1].isTreeLocked());
   }
   
   public void testDelete() throws PSModelException, PSMultiOperationException
   {
      final PSResourceFileModelProxy proxy = new PSTestResourceFileModelProxy();
      
      final PSXmlApplicationFileHierarchyRef[] refs = new PSXmlApplicationFileHierarchyRef[2];
      final PSApplication application1 = new PSApplication(APPLICATION_NAME1) {};
      {
         // reference to file in a subdirectory
         final IPSHierarchyNodeRef rootRef =
               new PSXmlApplicationFileHierarchyRootRef(application1);
         final IPSHierarchyNodeRef ref1 =
              new PSXmlApplicationFileHierarchyRef(rootRef, "dir1", true);
         refs[0] =
               new PSXmlApplicationFileHierarchyRef(ref1, NAME1, false);
      }

      final PSApplication application2 = new PSApplication(APPLICATION_NAME2) {};
      assertFalse(application1.equals(application2));
      {
         // reference to folder
         final PSXmlApplicationFileHierarchyRootRef rootRef2 =
               new PSXmlApplicationFileHierarchyRootRef(application2);
         // some other node in this tree is locked
         rootRef2.lock();
         refs[1] = new PSXmlApplicationFileHierarchyRef(rootRef2, NAME2, true);
      }
      
      // everything is Ok
      m_mockObjectStore.expects(once()).method("removeApplicationFile")
            .with(eq(application1),
                  new PSApplicationFileConstraint("dir1/" + NAME1),
                  eq(true)); 
      m_mockObjectStore.expects(once()).method("removeApplicationFile")
            .with(eq(application2),
                  new PSApplicationFileConstraint(NAME2),
                  eq(false));
      refs[0].lock();
      assertTrue(refs[0].isTreeLocked());
      proxy.delete(refs);
      assertFalse(refs[0].isTreeLocked());
      
      // one of deletions fails with an exception
      m_mockObjectStore.expects(once()).method("removeApplicationFile")
            .with(eq(application1),
                  new PSApplicationFileConstraint("dir1/" + NAME1),
                  eq(true)); 
      m_mockObjectStore.expects(once()).method("removeApplicationFile")
            .with(eq(application2),
                  new PSApplicationFileConstraint(NAME2),
                  eq(false))
            .will(throwException(EXCEPTION));
      try
      {
         proxy.delete(refs);
         fail();
      }
      catch (PSMultiOperationException e)
      {
         final Object[] results = e.getResults();
         assertEquals(refs.length, results.length);
         assertNull(results[0]);
         assertEquals(EXCEPTION, results[1]);
      }
   }
   
   public void testSave() throws PSMultiOperationException, PSModelException
   {
      final PSResourceFileModelProxy proxy = new PSTestResourceFileModelProxy();
      
      final PSXmlApplicationFileHierarchyRef[] refs = new PSXmlApplicationFileHierarchyRef[2]; 
      final Object[] data = new Object[2];

      final PSApplication application1 = new PSApplication(APPLICATION_NAME1) {};

      data[0] = new com.percussion.services.system.data.PSMimeContentAdapter();
      data[1] = null;
      
      {
         // reference to file in a subdirectory
         final IPSHierarchyNodeRef rootRef =
               new PSXmlApplicationFileHierarchyRootRef(application1);
         final IPSHierarchyNodeRef ref1 =
            new PSXmlApplicationFileHierarchyRef(rootRef, "dir1", true);
         refs[0] = new PSXmlApplicationFileHierarchyRef(ref1, NAME1, false);
      }

      final PSApplication application2 = new PSApplication(APPLICATION_NAME2) {};
      assertFalse(application1.equals(application2));
      {
         // reference to folder
         final PSXmlApplicationFileHierarchyRef rootRef2 =
               new PSXmlApplicationFileHierarchyRootRef(application2);
         // some other node in this tree is locked
         rootRef2.lock();
         refs[1] = new PSXmlApplicationFileHierarchyRef(rootRef2, NAME2, true);
      }
      
      // everything is Ok
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application1),
                  new PSApplicationFileConstraint("dir1/" + NAME1),
                  eq(true),
                  eq(true)); 
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application2),
                  new PSApplicationFileConstraint(NAME2),
                  eq(true),
                  eq(false));
      for (final IPSHierarchyNodeRef ref : refs)
      {
         assertFalse(ref.isPersisted());
      }
      refs[0].lock();
      proxy.save(refs, data, true);
      assertFalse(refs[0].isTreeLocked());
      for (final IPSHierarchyNodeRef ref : refs)
      {
         assertTrue(ref.isPersisted());
      }
      
      // one of savings fails with an exception
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application1),
                  new PSApplicationFileConstraint("dir1/" + NAME1),
                  eq(true),
                  eq(false)); 
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application2),
                  new PSApplicationFileConstraint(NAME2),
                  eq(true),
                  eq(false))
            .will(throwException(EXCEPTION));
      try
      {
         proxy.save(refs, data, false);
         fail();
      }
      catch (PSMultiOperationException e)
      {
         final Object[] results = e.getResults();
         assertEquals(refs.length, results.length);
         assertNull(results[0]);
         assertEquals(EXCEPTION, results[1]);
      }
   }
   
   public void testIsLocked() throws PSModelException
   {
      final PSApplication application1 = new PSApplication(APPLICATION_NAME1) {};
      final PSXmlApplicationFileHierarchyRef rootRef =
         new PSXmlApplicationFileHierarchyRootRef(application1);
      final PSXmlApplicationFileHierarchyRef ref =
            new PSXmlApplicationFileHierarchyRef(rootRef, "dir1", true);

      final PSResourceFileModelProxy proxy = new PSTestResourceFileModelProxy()
      {
         @Override
         @SuppressWarnings("unused")
         boolean checkApplicationLock(PSXmlApplicationFileHierarchyRef fileRef)
         {
            return true;
         }
      };
      assertFalse(ref.isLocked());
      assertFalse(proxy.isLocked(ref));
      
      rootRef.lock();
      assertTrue(ref.isTreeLocked());
      assertFalse(ref.isLocked());
      assertFalse(proxy.isLocked(ref));

      ref.lock();
      assertTrue(ref.isTreeLocked());
      assertTrue(ref.isLocked());
      assertTrue(proxy.isLocked(ref));
   }
   
   public void testReleaseLock() throws PSModelException, PSMultiOperationException
   {
      final PSXmlApplicationFileHierarchyRef[] refs = new PSXmlApplicationFileHierarchyRef[2];
      final PSApplication application1 = new PSApplication(APPLICATION_NAME1) {};
      {
         final IPSHierarchyNodeRef rootRef =
               new PSXmlApplicationFileHierarchyRootRef(application1);
         final IPSHierarchyNodeRef ref1 =
            new PSXmlApplicationFileHierarchyRef(rootRef, "dir1", true);
         refs[0] = new PSXmlApplicationFileHierarchyRef(ref1, NAME1, false);
      }

      final PSApplication application2 = new PSApplication(APPLICATION_NAME2) {};
      assertFalse(application1.equals(application2));
      {
         final PSXmlApplicationFileHierarchyRootRef rootRef2 =
               new PSXmlApplicationFileHierarchyRootRef(application2);
         // some other node in this tree is locked
         rootRef2.lock();
         refs[1] = new PSXmlApplicationFileHierarchyRef(rootRef2, NAME2, true);
      }
      
      final PSResourceFileModelProxy proxy = new PSTestResourceFileModelProxy();

      // everything is Ok
      m_mockObjectStore.expects(once()).method("releaseApplicationLock")
            .with(eq(application1)); 
      
      refs[0].lock();
      refs[1].lock();
      proxy.releaseLock(refs);
      assertFalse(refs[0].isLocked());
      assertFalse(refs[1].isLocked());
      assertFalse(refs[0].isTreeLocked());
      assertTrue(refs[1].isTreeLocked());
      
      // one of calls fails with an exception
      m_mockObjectStore.expects(once()).method("releaseApplicationLock")
            .with(eq(application1))
            .will(throwException(EXCEPTION));
      refs[0].lock();
      refs[1].lock();
      try
      {
         proxy.releaseLock(refs);
         fail();
      }
      catch (PSMultiOperationException e)
      {
         final Object[] results = e.getResults();
         assertEquals(refs.length, results.length);
         assertEquals(EXCEPTION, results[0]);
         assertNull(results[1]);
         
         assertTrue(refs[0].isLocked());
         assertFalse(refs[1].isLocked());
         assertTrue(refs[0].isTreeLocked());
         assertTrue(refs[1].isTreeLocked());

      }
   }
   
   /**
    * Sample application name.
    */
   private static final String APPLICATION_NAME1 = "Application Name 1 " + RandomStringUtils.random(2);

   /**
    * Sample application name.
    */
   private static final String APPLICATION_NAME2 = "Application Name 2 " + RandomStringUtils.random(2);

   /**
    * Sample name.
    */
   private static final String NAME1 = "Name 1"; 

   /**
    * Sample name.
    */
   private static final String NAME2 = "Name 2"; 

   /**
    * Sample exception.
    */
   private final PSAuthenticationFailedException EXCEPTION =
         new PSAuthenticationFailedException(IPSConnectionErrors.NULL_SOCKET,
               new Object[0])
   {
      private static final long serialVersionUID = 1L;

      @Override
      public String getLocalizedMessage()
      {
         // overwritten to suppress logging messages
         return "dummy message";
      }
   };

   /**
    * Store used by all {@link PSTestResourceFileModelProxy}.
    */
   private Mock m_mockObjectStore = new Mock(PSObjectStore.class);

   /**
    * Class for testing which replaces {@link PSLegacyModelProxy#getObjectStore()}
    * initialization.
    */
   private class PSTestResourceFileModelProxy extends PSResourceFileModelProxy
   {
      public PSTestResourceFileModelProxy()
      {
         super();
      }

      @Override
      protected PSObjectStore getObjectStore()
      {
         return (PSObjectStore) m_mockObjectStore.proxy();
      }
   }

   /**
    * For unit tests session identification.
    */
   private static final String CLIENT_SESSION_ID = "WORKBENCH_UNIT_TEST";
}

/******************************************************************************
 *
 * [ PSResourceFileHierarchyModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.IPSHierarchyModelProxy.NodeId;
import com.percussion.content.PSMimeContentAdapter;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.security.PSAuthorizationException;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.RandomStringUtils;
import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.percussion.client.proxies.impl.PSXmlApplicationFileModelProxy.OBJECT_TYPE_FILE;
import static com.percussion.client.proxies.impl.PSXmlApplicationFileModelProxy.OBJECT_TYPE_FOLDER;

@Category(IntegrationTest.class)
public class PSResourceFileHierarchyModelProxyTest extends MockObjectTestCase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSCoreFactory.getInstance().setClientSessionId(CLIENT_SESSION_ID);
   }

   public void testConstructor()
   {
      assertEquals(PSObjectTypes.RESOURCE_FILE, createProxy().getPrimaryType());
   }

   private TestPSResourceFileHierarchyModelProxy createProxy()
   {
      return new TestPSResourceFileHierarchyModelProxy();
   }
   
   public void testCreateChildrenFailure_DuplicateNameException()
         throws PSModelException
   {
      final PSApplication application = new PSApplication(APPLICATION_NAME) {};
      final IPSHierarchyNodeRef rootRef =
            new PSXmlApplicationFileHierarchyRootRef(application);
      final PSXmlApplicationFileHierarchyRef ref1 =
         new PSXmlApplicationFileHierarchyRef(rootRef, NAME, true);

      final List<String> names = new ArrayList<String>();
      names.add(NAME1);
      names.add(NAME2);

      // second name already exists
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application),
                  new PSApplicationFileConstraint(NAME + "/" + NAME1),
                  eq(true),
                  eq(true));
      try
      {
         final PSResourceFileHierarchyModelProxy proxy =
            new TestPSResourceFileHierarchyModelProxy()
            {
               @Override
               public IPSHierarchyNodeRef[] getChildren(NodeId parent)
                  throws PSModelException
               {
                  assertEquals(ref1, parent.getNodeRef());
                  return new IPSHierarchyNodeRef[] {
                        new PSXmlApplicationFileHierarchyRef(rootRef, NAME2, true)};
               }
            };
         final Object[] results = new Object[2];
         proxy.createChildren(new NodeId(ref1), OBJECT_TYPE_FOLDER, names, results);
         fail();
      }
      catch (PSMultiOperationException success) {
         assertEquals(2, success.getResults().length);
         assertTrue(success.getResults()[0] instanceof PSXmlApplicationFileHierarchyRef);
         assertTrue(success.getResults()[1] instanceof PSDuplicateNameException);
      }
      
      // first name is duplicated in the list, differs by case
      names.add(NAME1.toUpperCase());
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application),
                  new PSApplicationFileConstraint(NAME + "/" + NAME1),
                  eq(true),
                  eq(true));
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application),
                  new PSApplicationFileConstraint(NAME + "/" + NAME2),
                  eq(true),
                  eq(true));
      try
      {
         final PSResourceFileHierarchyModelProxy proxy =
            new TestPSResourceFileHierarchyModelProxy()
            {
               @Override
               public IPSHierarchyNodeRef[] getChildren(NodeId parent)
               {
                  assertEquals(ref1, parent.getNodeRef());
                  return new IPSHierarchyNodeRef[0];
               }
            };
         final Object[] results = new Object[3];
         proxy.createChildren(new NodeId(ref1), OBJECT_TYPE_FOLDER, names, results);
         fail();
      }
      catch (PSMultiOperationException success) {
         assertEquals(3, success.getResults().length);
         assertTrue(success.getResults()[0] instanceof PSXmlApplicationFileHierarchyRef);
         assertTrue(success.getResults()[1] instanceof PSXmlApplicationFileHierarchyRef);
         assertTrue(success.getResults()[2] instanceof PSDuplicateNameException);
      }
   }

   public void testCreateChildrenFailure_GetChildrenFailure()
         throws PSModelException
   {
      final PSApplication application = new PSApplication(APPLICATION_NAME) {};
      final IPSHierarchyNodeRef rootRef =
            new PSXmlApplicationFileHierarchyRootRef(application);
      final PSXmlApplicationFileHierarchyRef ref1 =
         new PSXmlApplicationFileHierarchyRef(rootRef, NAME, true);

      final List<String> names = new ArrayList<String>();
      names.add(NAME1);
      names.add(NAME2);

      final PSModelException modelException = new PSModelException(new Exception())
      {
         private static final long serialVersionUID = 1L;

         @Override
         public String getLocalizedMessage()
         {
            return "message";
         }
      };
      final PSResourceFileHierarchyModelProxy proxy =
         new TestPSResourceFileHierarchyModelProxy()
         {
            @Override
            @SuppressWarnings("unused")
            public IPSHierarchyNodeRef[] getChildren(NodeId parent)
               throws PSModelException
            {
               throw modelException;
            }
         };
      try
      {
         proxy.createChildren(new NodeId(ref1), OBJECT_TYPE_FOLDER, names, new Object[2]);
         fail();
      }
      catch (PSMultiOperationException success) {
         assertEquals(2, success.getResults().length);
         assertSame(modelException, success.getResults()[0]);
         assertSame(modelException, success.getResults()[1]);
      }
   }

   public void testCreateChildren()
         throws PSMultiOperationException, PSModelException
   {
      final PSApplication application = new PSApplication(APPLICATION_NAME) {};
      final IPSHierarchyNodeRef rootRef =
            new PSXmlApplicationFileHierarchyRootRef(application);
      final PSXmlApplicationFileHierarchyRef ref1 =
         new PSXmlApplicationFileHierarchyRef(rootRef, NAME, true);

      final PSResourceFileHierarchyModelProxy proxy =
         new TestPSResourceFileHierarchyModelProxy()
         {
            @Override
            @SuppressWarnings("unused")
            public IPSHierarchyNodeRef[] getChildren(NodeId parent)
            {
               return new IPSHierarchyNodeRef[0];
            }
         };

      final List<String> names = new ArrayList<String>();
      names.add(NAME1);
      names.add(NAME2);

      // create folders
      checkCreateChildFolders(application, ref1, proxy, names, true);
      ref1.lock();
      checkCreateChildFolders(application, ref1, proxy, names, false);

      // create files
      {
         final Object[] results = new Object[2];
         checkCreateResultRefs(
               proxy.createChildren(new NodeId(ref1), OBJECT_TYPE_FILE, names, results), false);
         assertEquals(NAME1, ((PSMimeContentAdapter) results[0]).getName());
         assertEquals(NAME2, ((PSMimeContentAdapter) results[1]).getName());
      }
   }

   /**
    * Validates child folders creation for given lock mode. 
    */
   private void checkCreateChildFolders(final PSApplication application,
         final PSXmlApplicationFileHierarchyRef ref1,
         final PSResourceFileHierarchyModelProxy proxy, final List<String> names,
         final boolean releaseLock) throws PSMultiOperationException
   {
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application),
                  new PSApplicationFileConstraint(NAME + "/" + NAME1),
                  eq(true),
                  eq(releaseLock));
      m_mockObjectStore.expects(once()).method("saveApplicationFile")
            .with(eq(application),
                  new PSApplicationFileConstraint(NAME + "/" + NAME2),
                  eq(true),
                  eq(releaseLock));
      final Object[] results = new Object[2];
      checkCreateResultRefs(
            proxy.createChildren(new NodeId(ref1), OBJECT_TYPE_FOLDER, names, results), true);
      assertNull(results[0]);
      assertNull(results[1]);
   }

   private void checkCreateResultRefs(final IPSHierarchyNodeRef[] resultRefs,
         boolean isContainer)
   {
      for (final IPSHierarchyNodeRef ref : resultRefs)
      {
         // files are persisted, folders are not
         assertEquals(isContainer, ref.isContainer());
         assertEquals(isContainer, ref.isPersisted());
         assertNotNull(((PSXmlApplicationFileHierarchyRef) ref).getApplication());
      }
   }
   
   public void testGetRoots()
   {
      assertEquals(1, createProxy().getRoots().size());
      assertEquals("RESOURCE_FILE_TREE", createProxy().getRoots().iterator().next());
   }

   public void testGetRootNodes()
   {
      // non-existing root
      try
      {
         createProxy().getChildren(new NodeId("Non-Existing File"));
         fail();
      }
      catch (PSModelException success) {}

      // normal situation
      //todo finish
      // TODO finish
//      final PSApplication application = new PSApplication(APPLICATION_NAME) {};
//      m_mockObjectStore.expects(once()).method("getApplication")
//            .with(eq(APPLICATION_NAME), eq(false), eq(false))
//            .will(returnValue(application));
//      final IPSHierarchyNodeRef[] rootNodes = createProxy().getRootNodes(ROOT_NAME);
//
//      assertEquals(1, rootNodes.length);
//      final IPSHierarchyNodeRef rootRef = rootNodes[0];
//      
//      assertTrue(checkRefIs(rootRef, APPLICATION_NAME, true, true, true));
//      assertNull(rootRef.getParent());
//      assertTrue(StringUtils.isNotBlank(rootRef.getDescription()));
//      m_mockObjectStore.verify();
   }

   private boolean checkRefIs(final IPSHierarchyNodeRef ref, final String name,
         final boolean isContainer, final boolean isPersisted,
         final boolean isReadOnly)
   {
      if (!name.equals(ref.getName()))
      {
         return false;
      }
      assertNotNull(ref.getParentModel());
      assertNull(((PSHierarchyNodeRef) ref).getManager());

      assertEquals(isContainer, ref.isContainer()); 
      assertEquals(isPersisted, ref.isPersisted());
      assertEquals(isReadOnly, ref.isReadOnly());

      assertEquals(ref.getObjectType(),
            ref.isContainer() ? OBJECT_TYPE_FOLDER : OBJECT_TYPE_FILE);
      return true;
   }

   public void testGetChildren() throws PSModelException
   {
      // parent - unexpected primary type
      {
         final PSObjectType objectType = new PSObjectType(PSObjectTypes.ROLE);
         final IPSHierarchyNodeRef reference =
               new PSHierarchyNodeRef(null, NAME, objectType, null, false);
         try
         {
            createProxy().getChildren(new NodeId(reference));
            fail();
         }
         catch (IllegalArgumentException success) {}
      }

      // parent - file
      {
         final IPSHierarchyNodeRef reference =
               new PSHierarchyNodeRef(null, NAME, OBJECT_TYPE_FILE, null, false);
         try
         {
            createProxy().getChildren(new NodeId(reference));
            fail();
         }
         catch (IllegalArgumentException success) {}
      }

      final PSApplication application = new PSApplication(APPLICATION_NAME) {};
      final IPSHierarchyNodeRef rootRef =
            new PSXmlApplicationFileHierarchyRootRef(application);

      // parent - root
      {
         m_mockObjectStore.expects(once()).method("getApplicationFiles").with(
               eq(application), NULL).will(returnValue(getSampleFiles()));
         checkNodesFromSampleFiles(rootRef, createProxy().getChildren(new NodeId(rootRef)));
      }

      // parent - folder
      final IPSHierarchyNodeRef dir1Ref =
         new PSXmlApplicationFileHierarchyRef(rootRef, "dir1", true);
      {
         final PSXmlApplicationFileHierarchyRef ref =
            new PSXmlApplicationFileHierarchyRef(dir1Ref, NAME, true);
         m_mockObjectStore.expects(once()).method("getApplicationFiles")
               .with(eq(application),
                     new PSApplicationFileConstraint("dir1/" + NAME))
               .will(returnValue(getSampleFiles()));
         checkNodesFromSampleFiles(ref, createProxy().getChildren(new NodeId(ref)));
      }
   }

   /**
    * Checks nodes generated from application files returned by
    * {@link #getSampleFiles()}.
    * @param parent expected parent of the nodes
    * @param refs the nodes to validate.
    */
   private void checkNodesFromSampleFiles(final IPSHierarchyNodeRef parent,
         final IPSHierarchyNodeRef[] refs)
   {
      assertEquals(getSampleFiles().size(), refs.length);
      for (final IPSHierarchyNodeRef ref : refs)
      {
         assertEquals(parent, ref.getParent());
         assertNotNull(((PSXmlApplicationFileHierarchyRef) ref).getApplication());
         assertTrue(ref.toString(),
               checkRefIs(ref, DIR_NAME, true, true, false)
               || checkRefIs(ref, FILE_NAME, false, true, false));
      }
   }

   /**
    * Generates sample files - one directory named {@link #DIR_NAME},
    * another - file named {@link #FILE_NAME}.
    */
   private List<PSApplicationFile> getSampleFiles()
   {
      final List<PSApplicationFile> applicationFiles = new ArrayList<PSApplicationFile>();
      applicationFiles.add(new PSApplicationFile(new File(DIR_NAME), true));
      applicationFiles.add(new PSApplicationFile(new File(FILE_NAME), false));
      return applicationFiles;
   }
   
   public void testRemoveChildren() throws PSModelException, PSMultiOperationException
   {
      final PSResourceFileHierarchyModelProxy proxy =
         createProxy();
      final Mock mockModelProxy = new Mock(IPSCmsModelProxy.class);
      proxy.m_modelProxy = (IPSCmsModelProxy) mockModelProxy.proxy();
      final PSXmlApplicationFileHierarchyRef[] refs = new PSXmlApplicationFileHierarchyRef[1]; 
      refs[0] = new PSXmlApplicationFileHierarchyRef(null, NAME, true);

      // normal call
      mockModelProxy.expects(once()).method("delete")
            .with(same(refs));
      proxy.removeChildren(refs);
      
      // failure
      final PSMultiOperationException e = new PSMultiOperationException(refs);
      mockModelProxy.expects(once()).method("delete")
            .with(same(refs))
            .will(throwException(e));
      try
      {
         proxy.removeChildren(refs);
         fail();
      }
      catch (PSMultiOperationException success) {
         assertSame(e, success);
      }
      mockModelProxy.verify();
   }
   
   public void testMoveChildren() throws PSModelException, PSMultiOperationException
   {
      final PSApplication application1 = new PSApplication(APPLICATION_NAME) {};
      final IPSHierarchyNodeRef rootRef1 =
            new PSXmlApplicationFileHierarchyRootRef(application1);
      final IPSHierarchyNodeRef ref1 =
         new PSXmlApplicationFileHierarchyRef(rootRef1, "dir1", true);

      final PSXmlApplicationFileHierarchyRef ref1_1 =
         new PSXmlApplicationFileHierarchyRef(ref1, "dir1_1", true);
      final PSXmlApplicationFileHierarchyRef ref1_2 =
         new PSXmlApplicationFileHierarchyRef(ref1, "file1_2", false);
      final PSXmlApplicationFileHierarchyRef ref1_3 =
         new PSXmlApplicationFileHierarchyRef(ref1, "dir1_3", true);
      
      final PSXmlApplicationFileHierarchyRef ref1_1_1 =
         new PSXmlApplicationFileHierarchyRef(ref1_1, "file1_1_1", false);
      final PSXmlApplicationFileHierarchyRef ref1_1_2 =
         new PSXmlApplicationFileHierarchyRef(ref1_1, "dir1_1_2", true);

      final PSApplication application2 = new PSApplication(APPLICATION_NAME2) {};
      final PSXmlApplicationFileHierarchyRef rootRef2 =
            new PSXmlApplicationFileHierarchyRootRef(application2);
      final PSXmlApplicationFileHierarchyRef ref2 =
         new PSXmlApplicationFileHierarchyRef(rootRef2, "dir2", true);

      // move ref1_1, ref1_2 to ref2 - everything is Ok
      assertEquals(ref1, ref1_1.getParent());
      assertEquals(application1, ref1_1.getApplication());
      assertEquals(ref1, ref1_2.getParent());
      assertEquals(application1, ref1_2.getApplication());
      assertEquals(ref1, ref1_3.getParent());
      assertEquals(application1, ref1_3.getApplication());
      assertEquals(application1, ref1_1_1.getApplication());
      assertEquals(application1, ref1_1_2.getApplication());
      
      final IPSHierarchyNodeRef[] children =
         new IPSHierarchyNodeRef[] {ref1_1, ref1_2}; 

      m_mockObjectStore.expects(once()).method("moveApplicationFile")
            .with(new Constraint[] {
                  eq(application1),
                  new PSApplicationFileConstraint(ref1.getName() + "/" + ref1_1.getName()),
                  eq(application2),
                  new PSApplicationFileConstraint(ref2.getName() + "/" + ref1_1.getName()),
                  eq(true)});
      m_mockObjectStore.expects(once()).method("moveApplicationFile")
            .with(new Constraint[] {
                  eq(application1),
                  new PSApplicationFileConstraint(ref1.getName() + "/" + ref1_2.getName()),
                  eq(application2),
                  new PSApplicationFileConstraint(ref2.getName() + "/" + ref1_2.getName()),
                  eq(true)});
      
      {
         final PSResourceFileHierarchyModelProxy proxy =
            new TestPSResourceFileHierarchyModelProxy()
            {
               @Override
               @SuppressWarnings("unused")
               public IPSHierarchyNodeRef[] getChildren(NodeId parent)
               {
                  return new IPSHierarchyNodeRef[0];
               }
            };
         proxy.moveChildren(children, new NodeId(ref2));
      }
      
      
      assertEquals(ref2, ref1_1.getParent());
      assertEquals(application2, ref1_1.getApplication());

      assertEquals(ref2, ref1_2.getParent());
      assertEquals(application2, ref1_2.getApplication());

      assertEquals(ref1, ref1_3.getParent());
      assertEquals(application1, ref1_3.getApplication());

      assertEquals(application2, ref1_1_1.getApplication());
      assertEquals(application2, ref1_1_2.getApplication());

      // move ref1_1, ref1_2 to ref2 - ref1_1 fails with exception
      final Exception exception1_1 = new PSAuthorizationException(0, new Object[0])
      {
         private static final long serialVersionUID = 1L;

         @Override
         public String getMessage()
         {
            return "dummy message";
         }
      };
      m_mockObjectStore.expects(once()).method("moveApplicationFile")
            .with(new Constraint[] {
                  eq(application2),
                  new PSApplicationFileConstraint(ref2.getName() + "/" + ref1_1.getName()),
                  eq(application1),
                  new PSApplicationFileConstraint(ref1.getName() + "/" + ref1_1.getName()),
                  eq(true)})
            .will(throwException(exception1_1));
      m_mockObjectStore.expects(once()).method("moveApplicationFile")
            .with(new Constraint[] {
                  eq(application2),
                  new PSApplicationFileConstraint(ref2.getName() + "/" + ref1_2.getName()),
                  eq(application1),
                  new PSApplicationFileConstraint(ref1.getName() + "/" + ref1_2.getName()),
                  eq(true)});
      try
      {
         final PSResourceFileHierarchyModelProxy proxy =
            new TestPSResourceFileHierarchyModelProxy()
            {
               @Override
               @SuppressWarnings("unused")
               public IPSHierarchyNodeRef[] getChildren(NodeId parent)
               {
                  return new IPSHierarchyNodeRef[0];
               }
            };
         proxy.moveChildren(children, new NodeId(ref1));
         fail();
      }
      catch (PSMultiOperationException e)
      {
         assertEquals(exception1_1, e.getResults()[0]);
         assertNull(e.getResults()[1]);
      }

      assertEquals(ref2, ref1_1.getParent());
      assertEquals(application2, ref1_1.getApplication());

      assertEquals(ref1, ref1_2.getParent());
      assertEquals(application1, ref1_2.getApplication());

      assertEquals(ref1, ref1_3.getParent());
      assertEquals(application1, ref1_3.getApplication());

      assertEquals(application2, ref1_1_1.getApplication());
      assertEquals(application2, ref1_1_2.getApplication());
      
      // move ref1_1 back when same name already exists
      assertEquals(ref2, ref1_1.getParent());
      try
      {
         final PSResourceFileHierarchyModelProxy proxy =
            new TestPSResourceFileHierarchyModelProxy()
            {
               @Override
               
               public IPSHierarchyNodeRef[] getChildren(
                     @SuppressWarnings("unused") NodeId parent)
               throws PSModelException
               {
                  return new IPSHierarchyNodeRef[] {
                        new PSXmlApplicationFileHierarchyRef(ref1, "DIR1_1", true)
                        };
               }
            };
         proxy.moveChildren(new IPSHierarchyNodeRef[] {ref1_1}, new NodeId(ref1));
         fail();
      }
      catch (PSMultiOperationException e)
      {
         assertTrue(e.getResults()[0] instanceof PSDuplicateNameException);
      }
      
      // move ref1_1 back when it is already locked
      assertEquals(ref2, ref1_1.getParent());
      ref1_1.lock();
      try
      {
         final PSResourceFileHierarchyModelProxy proxy =
            new TestPSResourceFileHierarchyModelProxy()
            {
               @Override
               @SuppressWarnings("unused")
               public IPSHierarchyNodeRef[] getChildren(NodeId parent)
               {
                  return new IPSHierarchyNodeRef[0];
               }
            };
         proxy.moveChildren(new IPSHierarchyNodeRef[] {ref1_1}, new NodeId(ref1));
         fail();
      }
      catch (PSMultiOperationException e)
      {
         assertTrue(e.getResults()[0] instanceof PSLockException);
      }
   }

   /**
    * Class for testing which replaces {@link PSHierarchyModelProxy#getObjectStore()}
    * initialization.
    */
   private class TestPSResourceFileHierarchyModelProxy extends PSResourceFileHierarchyModelProxy
   {

      public TestPSResourceFileHierarchyModelProxy()
      {
         super();
      }

      @Override
      protected IPSCmsModelProxy createModelProxy()
      {
         return null;
      }
   }

   /**
    * Sample name.
    */
   private static final String NAME = "Name"; 

   /**
    * Sample name.
    */
   private static final String NAME1 = "Name 1"; 

   /**
    * Sample name.
    */
   private static final String NAME2 = "Name 2"; 

   /**
    * Sample application name.
    */
   private static final String APPLICATION_NAME = "Application Name "
         + RandomStringUtils.random(2);

   /**
    * Sample application name.
    */
   private static final String APPLICATION_NAME2 = "Application Name2 "
         + RandomStringUtils.random(2);

   /**
    * Sample file name.
    */
   private static final String FILE_NAME = "File " + RandomStringUtils.random(2);

   /**
    * Sample folder name.
    */
   private static final String DIR_NAME = "Directory " + RandomStringUtils.random(2);

   /**
    * For unit tests session identification.
    */
   static private final String CLIENT_SESSION_ID = "WORKBENCH_UNIT_TEST";

   /**
    * Store used by all {@link TestPSResourceFileHierarchyModelProxy}.
    */
   private Mock m_mockObjectStore = new Mock(PSObjectStore.class);
}

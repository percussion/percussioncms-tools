/******************************************************************************
 *
 * [ PSLocalFileSystemHierarchyModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.IPSHierarchyModelProxy.NodeId;
import com.percussion.services.system.data.PSMimeContentAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.percussion.client.proxies.impl.PSLocalFileSystemHierarchyModelProxy.ROOT_NAME;
import static com.percussion.client.proxies.impl.PSLocalFileSystemHierarchyNodeRef.OBJECT_TYPE_FILE;
import static com.percussion.client.proxies.impl.PSLocalFileSystemHierarchyNodeRef.OBJECT_TYPE_FOLDER;

/**
 * @author Andriy Palamarchuk
 */
public class PSLocalFileSystemHierarchyModelProxyTest extends MockObjectTestCase
{
   @Override
   protected void setUp() throws Exception
   {
      m_file0 = createTempFile();
      m_file1 = createTempFile();
      assertEquals(m_file0.getParentFile(), m_file1.getParentFile());
      m_files = new File[] {m_file0, m_file1};
      for (final File file : m_files)
      {
         checkContent(new FileInputStream(file));
      }
      m_references = new PSLocalFileSystemHierarchyNodeRef[] {
            createRef(m_file0), createRef(m_file1)};

      m_dir = createTempFile();
      m_dir.delete();
      m_dir.mkdirs();
      super.setUp();
   }

   @Override
   protected void tearDown() throws Exception
   {
      m_file0.delete();
      m_file1.delete();
      FileUtils.deleteDirectory(m_file0);
      FileUtils.deleteDirectory(m_file1);
      FileUtils.deleteDirectory(m_dir);
      super.tearDown();

      assertFalse(m_file0.exists());
      assertFalse(m_file1.exists());
      assertFalse(m_dir.exists());
   }

   public void testCreateChildren_dirs()
         throws PSMultiOperationException, IOException
   {
      final PSLocalFileSystemHierarchyNodeRef parent =
         new PSLocalFileSystemHierarchyNodeRef(m_file0.getParentFile(), true);
      
      final Object[] results = new Object[getNames().size()];

      // ref-based
      final IPSHierarchyNodeRef[] children =
         createProxy().createChildren(new NodeId(parent), OBJECT_TYPE_FOLDER,
               getNames(), results);
      for (int i = 0; i < getNames().size(); i++)
      {
         final PSLocalFileSystemHierarchyNodeRef child =
               (PSLocalFileSystemHierarchyNodeRef) children[i];
         assertEquals(getNames().get(i), child.getName());
         assertTrue(child.isContainer());
         assertTrue(child.getFile().exists());
         assertTrue(child.getFile().isDirectory());
         assertNull(results[i]);
         FileUtils.deleteDirectory(child.getFile());
      }
      
      // name-based
      try
      {
         createProxy().createChildren(
               new NodeId(ROOT_NAME), OBJECT_TYPE_FOLDER, getNames(), results);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }

   public void testCreateChildren_files()
         throws PSMultiOperationException
   {
      final PSLocalFileSystemHierarchyNodeRef parent =
         new PSLocalFileSystemHierarchyNodeRef(m_file0.getParentFile(), true);

      final Object[] results = new Object[getNames().size()];

      // ref-based
      final IPSHierarchyNodeRef[] children = createProxy().createChildren(
            new NodeId(parent), OBJECT_TYPE_FILE, getNames(), results);
      for (int i = 0; i < getNames().size(); i++)
      {
         final PSLocalFileSystemHierarchyNodeRef child = (PSLocalFileSystemHierarchyNodeRef) children[i];
         assertEquals(getNames().get(i), child.getName());
         assertFalse(child.isContainer());
         assertFalse(child.getFile().exists());
         final PSMimeContentAdapter content = (PSMimeContentAdapter) results[i];
         assertEquals(child.getFile().getAbsolutePath(), content.getName());
      }

      // save, now files exist
      createModelProxy().save(children, results, true);
      for (int i = 0; i < getNames().size(); i++)
      {
         final PSLocalFileSystemHierarchyNodeRef child = (PSLocalFileSystemHierarchyNodeRef) children[i];
         assertTrue(child.getFile().exists());
         assertEquals(0, child.getFile().length());
         child.getFile().delete();
      }

      // name-based
      try
      {
         createProxy().createChildren(new NodeId(ROOT_NAME),
               OBJECT_TYPE_FILE, getNames(), results);
         fail();
      }
      catch (IllegalArgumentException success){}
   }
   
   public void testCreateChildren_duplicate()
   {
      m_file1.delete();

      final PSLocalFileSystemHierarchyNodeRef parent =
         new PSLocalFileSystemHierarchyNodeRef(m_file1.getParentFile(), true);

      final List<String> names = new ArrayList<String>();
      names.add(m_file0.getName());
      names.add(m_file1.getName());
      final Object[] results = new Object[names.size()];

      Object[] exceptionResults = null;
      try
      {
         createProxy().createChildren(
               new NodeId(parent), OBJECT_TYPE_FILE, names, results);
         fail();
      }
      catch (PSMultiOperationException e)
      {
         exceptionResults = e.getResults();
      }
      assertTrue(exceptionResults[0] instanceof Exception);
      assertTrue(exceptionResults[1] instanceof IPSReference);
      assertNull(results[0]);
      assertTrue(results[1] instanceof PSMimeContentAdapter);
   }
   
   /**
    * Creates list of names.
    */
   private List<String> getNames()
   {
      final String name0 = "Name0";
      final String name1 = "Name1";
      final String name2 = "Name2";
      final List<String> names = new ArrayList<String>();
      names.add(name0);
      names.add(name1);
      names.add(name2);
      return names;
   }

   public void testRemoveChildren()
      throws PSMultiOperationException, PSModelException
   {
      final PSLocalFileSystemHierarchyModelProxy proxy = createProxy();
      final Mock mockModelProxy = new Mock(IPSCmsModelProxy.class);
      proxy.m_modelProxy = (IPSCmsModelProxy) mockModelProxy.proxy();

      mockModelProxy.expects(once()).method("delete").with(same(m_references));
      proxy.removeChildren(m_references);
      
      mockModelProxy.verify();
   }
   
   public void testMoveChildren() throws IOException, PSMultiOperationException
   {
      final File moved0 = new File(m_dir, m_file0.getName()); 
      final File moved1 = new File(m_dir, m_file1.getName());
      assertFalse(moved0.exists());
      assertFalse(moved1.exists());
      
      moved0.mkdirs();
      
      final PSLocalFileSystemHierarchyNodeRef parent =
            new PSLocalFileSystemHierarchyNodeRef(m_dir, true);
      try
      {
         createProxy().moveChildren(m_references, new NodeId(parent));
         fail();
      }
      catch (PSMultiOperationException e)
      {
         assertTrue(e.getResults()[0] instanceof Exception);
         assertNull(e.getResults()[1]);
      }
      assertTrue(m_file0.exists());
      assertFalse(m_file1.exists());
      assertTrue(moved0.exists());
      assertTrue(moved1.exists());
      checkContent(new FileInputStream(moved1));
      
      // name-based
      try
      {
         createProxy().moveChildren(m_references, new NodeId(ROOT_NAME));
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
   
   public void testCreateChildrenFrom() throws IOException, PSMultiOperationException
   {
      m_file0.delete();
      m_file0.mkdirs();
      m_references[0] = createRef(m_file0);
      
      final File copied0 = new File(m_dir, m_file0.getName()); 
      final File copied1 = new File(m_dir, m_file1.getName());
      assertFalse(copied0.exists());
      assertFalse(copied1.exists());
      
      final PSLocalFileSystemHierarchyNodeRef parent =
            new PSLocalFileSystemHierarchyNodeRef(m_dir, true);
      final Object[] results = new Object[m_references.length];
      final IPSHierarchyNodeRef[] refs = createProxy().createChildrenFrom(
            new NodeId(parent), m_references, null, results);

      assertTrue(m_file0.exists());
      assertTrue(m_file1.exists());
      assertTrue(copied0.exists());
      assertFalse(copied1.exists());
      assertNull(results[0]);
      final PSMimeContentAdapter result1 = (PSMimeContentAdapter) results[1];
      checkContent(result1.getContent());

      final PSLocalFileSystemHierarchyNodeRef ref0 =
         (PSLocalFileSystemHierarchyNodeRef) refs[1];
      assertEquals(copied1, ref0.getFile());
      
      // trying second time
      try
      {
         createProxy().createChildrenFrom(new NodeId(parent), m_references,
               null, results);
         fail();
      }
      catch (PSMultiOperationException e)
      {
         assertTrue(e.getResults()[0] instanceof Exception);
         assertTrue(e.getResults()[0] instanceof Exception);
      }
      
      // name-based
      try
      {
         createProxy().moveChildren(m_references, new NodeId(ROOT_NAME));
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
   
   public void testGetRootNodes() throws PSModelException
   {
      // normal situation
      assertTrue(createProxy().getChildren(new NodeId(ROOT_NAME)).length > 0);

      // non-existing root
      try
      {
         createProxy().getChildren(new NodeId("Non-Existing File"));
         fail();
      }
      catch (PSModelException success) {}
   }
   
   public void testGetChildren() throws IOException, PSModelException
   {
      final File file = File.createTempFile("test", null);
      // normal
      try
      {
         final IPSHierarchyNodeRef[] children = createProxy().getChildren(
               new NodeId(new PSLocalFileSystemHierarchyNodeRef(
                     file.getParentFile(), true)));
         boolean fileFound = false;
         for (final IPSHierarchyNodeRef ref : children)
         {
            if (ref.getName().equals(file.getName()))
            {
               fileFound = true;
               break;
            }
         }
         assertTrue(fileFound);
      }
      finally
      {
         file.delete();
      }

      // trying to pass null
      try
      {
         createProxy().getChildren((NodeId) null);
         fail();
      }
      catch (Exception success) {}
}
   
   public void testGetRoots()
   {
      assertEquals(1, createProxy().getRoots().size());
      assertEquals(ROOT_NAME, createProxy().getRoots().iterator().next());
   }

   /**
    * Creates a proxy.
    */
   private PSLocalFileSystemHierarchyModelProxy createProxy()
   {
      return new PSLocalFileSystemHierarchyModelProxy();
   }
   
   /**
    * Creates model proxy corresponding to this hierarchy model proxy.
    */
   private PSLocalFileSystemModelProxy createModelProxy()
   {
      return new PSLocalFileSystemModelProxy();
   }

   /**
    * Creates temporary file and writes to it string {@link #FILE_CONTENT}. 
    */
   private File createTempFile() throws IOException
   {
      final File file = File.createTempFile("test", null);
      FileUtils.writeStringToFile(file, FILE_CONTENT, StandardCharsets.UTF_8);
      return file;
   }

   /**
    * Makes sure content equals to {@link #FILE_CONTENT}.
    */
   private void checkContent(final InputStream content) throws IOException
   {
      try
      {
         assertEquals(FILE_CONTENT, IOUtils.toString(content,StandardCharsets.UTF_8));
      }
      finally
      {
         content.close();
      }
   }

   /**
    * Creates reference to the specified file.
    */
   private PSLocalFileSystemHierarchyNodeRef createRef(final File file)
   {
      return new PSLocalFileSystemHierarchyNodeRef(file, file.isDirectory());
   }
   
   private static final String FILE_CONTENT = "File Content";
   
   /**
    * Sample file. Part of fixture.
    */
   private File m_file0;

   /**
    * Sample file. Part of fixture.
    */
   private File m_file1;

   /**
    * Sample directory. Part of fixture.
    */
   private File m_dir;

   /**
    * All sample files.
    */
   private File[] m_files;
   
   /**
    * References corresponding to sample files.
    */
   private PSLocalFileSystemHierarchyNodeRef[] m_references;
}

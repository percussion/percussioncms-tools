/******************************************************************************
 *
 * [ PSLocalFileSystemModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.system.data.PSMimeContentAdapter;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PSLocalFileSystemModelProxyTest extends TestCase
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
      super.setUp();
   }

   @Override
   protected void tearDown() throws Exception
   {
      m_file0.delete();
      m_file1.delete();
      super.tearDown();

      assertFalse(m_file0.exists());
      assertFalse(m_file1.exists());
   }

   public void testBasics() throws PSMultiOperationException
   {
      final PSLocalFileSystemModelProxy proxy = 
         new PSLocalFileSystemModelProxy();
      assertFalse(proxy.isLocked(null));
      
      // operations which do nothing
      proxy.releaseLock(null);
      proxy.saveAcl(null, null, true);
      proxy.releaseAclLock(null);
   }
   
   public void testCreate()
   {
      try
      {
         createProxy().create((IPSReference[]) null, null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         createProxy().create((PSObjectType) null, null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }
   
   public void testSave_dirs() throws PSMultiOperationException
   {
      m_file0.delete();
      m_file1.delete();

      // non-existing directories
      final IPSReference[] dirRefs = new IPSReference[m_files.length];
      for (int i = 0; i < m_files.length; i++)
      {
         dirRefs[i] = new PSLocalFileSystemHierarchyNodeRef(m_files[i], true);
      }
      final Object[] data = new Object[m_files.length];
      createProxy().save(dirRefs, data, true);
      assertTrue(m_file0.exists());
      assertTrue(m_file1.exists());
      assertTrue(m_file0.isDirectory());
      assertTrue(m_file1.isDirectory());

      // existing directories
      createProxy().save(dirRefs, data, true);
      assertTrue(m_file0.exists());
      assertTrue(m_file1.exists());
      assertTrue(m_file0.isDirectory());
      assertTrue(m_file1.isDirectory());
   }
   
   public void testSave_files() throws PSMultiOperationException, IOException
   {
      // two instances of data because data stream is exausted when it read
      final Object[] data0 = loadData();
      final Object[] data1 = loadData();
      m_file0.delete();
      m_file1.delete();

      // non-existing files
      createProxy().save(m_references, data0, true);
      checkContent(new FileInputStream(m_file0));
      checkContent(new FileInputStream(m_file1));
      
      // existing files
      createProxy().save(m_references, data1, true);
      checkContent(new FileInputStream(m_file0));
      checkContent(new FileInputStream(m_file1));
   }

   /**
    * Loads objects for {@link #m_references}.
    */
   private Object[] loadData() throws PSMultiOperationException
   {
      return createProxy().load(m_references, false, false);
   }

   public void testDelete() throws PSMultiOperationException
   {
      assertTrue(m_file0.exists());
      assertTrue(m_file1.exists());
      
      // delete one of the files
      createProxy().delete(m_references);
      assertFalse(m_file0.exists());
      assertFalse(m_file1.exists());
      
      // attempt to delete non-existing files
      createProxy().delete(m_references);
   }
   
   public void testCatalog()
   {
      try
      {
         createProxy().catalog();
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }
   
   public void testLoad() throws IOException, PSMultiOperationException
   {
      final Object[] loaded = loadData();
      assertEquals(m_references.length, loaded.length);
      for (int i = 0; i < loaded.length; i++)
      {
         final PSMimeContentAdapter content = (PSMimeContentAdapter) loaded[i];
         checkContent(content.getContent());
         assertEquals(m_files[i].getAbsolutePath(), content.getName());
      }
   }

   public void testRename_fileWithContent()
         throws IOException, PSMultiOperationException, PSModelException
   {
      final PSLocalFileSystemHierarchyNodeRef ref = m_references[0];
      final PSMimeContentAdapter content = (PSMimeContentAdapter) createProxy()
            .load(new IPSReference[] { ref }, false, false)[0];
      assertEquals(m_file0.getAbsolutePath(), content.getName());

      checkRenameFile0To1(ref, content);
      assertEquals(m_file1.getAbsolutePath(), content.getName());
   }

   public void testRename_fileNoContent()
         throws IOException, PSModelException
   {
      final PSLocalFileSystemHierarchyNodeRef ref = m_references[0];
      checkRenameFile0To1(ref, null);
   }

   /**
    * Insures that file 0 is correctly renamed to file 1.
    */
   private void checkRenameFile0To1(final PSLocalFileSystemHierarchyNodeRef ref,
         final PSMimeContentAdapter content)
         throws IOException, FileNotFoundException, PSModelException
   {
      // target file already exists
      try
      {
         createProxy().rename(ref, m_file1.getName(), content);
         fail();
      }
      catch (PSModelException e)
      {
         assertEquals(PSErrorCodes.DUPLICATE_NAME.getCodeAsInt(), e.getErrorCode());
      }
      assertEquals(m_file0.getName(), ref.getName());
      
      m_file1.delete();
      assertFalse(m_file1.exists());
      
      // normal
      createProxy().rename(ref, m_file1.getName(), content);
      
      assertFalse(m_file0.exists());
      assertTrue(m_file1.exists());
      checkContent(new FileInputStream(m_file1));

      assertEquals(m_file1.getName(), ref.getName());
   }

   public void testRenameLocal_fileWithContent()
      throws PSMultiOperationException
   {
      final PSLocalFileSystemHierarchyNodeRef ref = m_references[0];
      final PSMimeContentAdapter content = (PSMimeContentAdapter) createProxy()
            .load(new IPSReference[] { ref }, false, false)[0];
      assertEquals(m_file0.getAbsolutePath(), content.getName());

      createProxy().renameLocal(ref, m_file1.getName(), content);
      assertEquals(m_file1.getName(), ref.getName());
      assertEquals(m_file1.getAbsolutePath(), content.getName());
   }
   
   public void testRenameLocal_fileNoContent()
   {
      final PSLocalFileSystemHierarchyNodeRef ref = m_references[0];
      createProxy().renameLocal(ref, m_file1.getName(), null);
      assertTrue(m_file0.exists());
      assertEquals(m_file1.getName(), ref.getName());
   }


   /**
    * Makes sure content equals to {@link #FILE_CONTENT}.
    */
   private void checkContent(final InputStream content) throws IOException
   {
      try
      {
         assertEquals(FILE_CONTENT, IOUtils.toString(content));
      }
      finally
      {
         content.close();
      }
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
    * Creates reference to the specified file.
    */
   private PSLocalFileSystemHierarchyNodeRef createRef(final File file)
   {
      return new PSLocalFileSystemHierarchyNodeRef(file, file.isDirectory());
   }
   
   /**
    * Creates a proxy.
    */
   private PSLocalFileSystemModelProxy createProxy()
   {
      return new PSLocalFileSystemModelProxy();
   }
   
   public void testLoadAcls()
   {
      final IPSAcl[] acls = createProxy().loadAcl(m_references, true);
      assertEquals(2, acls.length);
      assertNull(acls[0]);
      assertNull(acls[1]);
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
    * All sample files.
    */
   private File[] m_files;
   
   /**
    * References corresponding to sample files.
    */
   private PSLocalFileSystemHierarchyNodeRef[] m_references;
}

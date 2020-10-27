/******************************************************************************
 *
 * [ PSLocalFileSystemHierarchyNodeRefTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Andriy Palamarchuk
 */
public class PSLocalFileSystemHierarchyNodeRefTest extends TestCase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      m_file = createTempFile();
   }

   @Override
   protected void tearDown() throws Exception
   {
      m_file.delete();
      super.tearDown();
   }
   
   public void testBasics()
   {
      assertEquals(m_file, createRef().getFile());
      assertNull(createRef().getId());
      assertTrue(createRef().isReadOnly());
      assertTrue(createRef().isPersisted());
      assertTrue(createRef().getAge() > 0);
   }
   
   public void testGetName()
   {
      // normal
      assertEquals(m_file.getName(), createRef().getName());
      assertEquals(m_file.getName(), createRef().getLocalName());
      
      // root - had problems, see getName() implementation for details
      final File rootFile = File.listRoots()[0];
      assertTrue(StringUtils.isNotBlank(createRef(rootFile, true).getName()));
   }
   
   public void testGetLabelKey()
   {
      assertEquals(m_file.getName(), createRef().getLabelKey());
      assertEquals(m_file.getName(), createRef().getLocalLabelKey());
   }

   public void testEqualsHash_ReferencesSameObject() throws IOException
   {
      final String name0 = m_file.getAbsolutePath();
      
      assertEquals(createRef(name0), createRef(name0));
      assertEquals(createRef(name0).hashCode(), createRef(name0).hashCode());
      assertEquals(createRef(name0).hashCode(), new File(name0).hashCode());
      assertTrue(createRef(name0).referencesSameObject(createRef(name0)));

      final File file1 = createTempFile();
      file1.delete();
      final String name1 = file1.getAbsolutePath();
      assertFalse(createRef(name0).equals(createRef(name1)));
   }
   
   public void testToString()
   {
      final String name = m_file.getAbsolutePath();
      assertTrue(createRef(name).toString().contains(name));
   }
   
   public void testIsContainer()
   {
      // file
      assertFalse(createRef().isContainer());

      // folder
      assertTrue(createRef(m_file.getParentFile(), true).isContainer());
      
      // root folder
      // used to try to access A: and request to insert a disk
      assertTrue(createRef(File.listRoots()[0], true).isContainer());
   }

   /**
    * Creates reference to the specified file.
    */
   private PSLocalFileSystemHierarchyNodeRef createRef(final File file,
         final boolean directory)
   {
      return new PSLocalFileSystemHierarchyNodeRef(file, directory);
   }
   
   /**
    * Creates reference to {@link #m_file}.
    */
   private PSLocalFileSystemHierarchyNodeRef createRef()
   {
      return createRef(m_file, false);
   }

   /**
    * Creates reference to the file with the specified name.
    */
   private PSLocalFileSystemHierarchyNodeRef createRef(final String name)
   {
      return createRef(new File(name), false);
   }

   public void testGetDescription()
   {
      // file
      assertTrue(createRef().getDescription().contains("File"));
      assertTrue(createRef().getDescription().contains(m_file.getName()));
      
      // folder
      final File dir = m_file.getParentFile();
      assertTrue(createRef(dir, true).getDescription().contains("Directory"));
      assertTrue(createRef(dir, true).getDescription().contains(dir.getName()));
   }
   
   public void testGetPath()
   {
      assertTrue(createRef().getPath().endsWith(m_file.getName()));
      assertTrue(createRef().getPath().startsWith("/"));
   }
   
   public void testGetParent()
   {
      assertEquals(createRef(m_file.getParentFile(), true), createRef().getParent());
      assertNull(createRef(File.listRoots()[0], true).getParent());
   }
   
   public void testGetChildren() throws PSModelException
   {
      assertEquals(0, createRef().getChildren().size());
      assertTrue(createRef().getParent().getChildren().size() > 0);
   }
   
   public void testGetObjectType()
   {
      assertEquals(PSObjectTypes.LOCAL_FILE, createRef().getObjectType().getPrimaryType());
      assertEquals(PSObjectTypes.FileSubTypes.FILE, createRef().getObjectType().getSecondaryType());
      
      assertEquals(PSObjectTypes.LOCAL_FILE,
            createRef().getParent().getObjectType().getPrimaryType());
      assertEquals(PSObjectTypes.FileSubTypes.FOLDER,
            createRef().getParent().getObjectType().getSecondaryType());
   }
   
   /**
    * Creates temporary file.
    */
   private File createTempFile() throws IOException
   {
      return File.createTempFile("test", null);
   }

   /**
    * An empty temporary file creates in the fixture for the test.
    * Removed on teardown.
    */
   private File m_file;
}

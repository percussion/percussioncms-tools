/******************************************************************************
 *
 * [ PSFileEditorHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class PSFileEditorHelperTest extends TestCase
{
   public void testCreateTempFile() throws IOException
   {
      final PSFileEditorHelper helper = new PSFileEditorHelper();
      // normal case
      {
         final File file = helper.createTempFile("abcd.123");
         assertTrue(file.exists());
         assertTrue(file.getName().startsWith("abcd"));
         assertTrue(file.getName().endsWith(".123"));
         file.delete();
      }
      
      // short name
      {
         final File file = helper.createTempFile("f.123");
         assertTrue(file.getName().startsWith("f"));
         assertTrue(file.getName().endsWith(".123"));
         assertTrue(file.getName().length() > "123.123".length());
         file.delete();
      }

      // no name
      {
         final File file = helper.createTempFile(".123");
         assertTrue(file.getName().endsWith(".123"));
         file.delete();
      }
      
      // more than 1 dot
      {
         final File file = helper.createTempFile("abcd.123.456");
         assertTrue(file.getName().startsWith("abcd"));
         assertTrue(file.getName().endsWith(".456"));
         file.delete();
      }
      
      // no extension
      {
         final File file = helper.createTempFile("abcd");
         assertTrue(file.getName().startsWith("abcd"));
         assertTrue(file.getName().endsWith(".tmp"));
         file.delete();
      }
   }
}

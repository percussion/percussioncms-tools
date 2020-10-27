/******************************************************************************
 *
 * [ PSResourceLoaderTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class PSResourceLoaderTest extends TestCase
{
   public void testLoadMimeTypes() throws FileNotFoundException, IOException
   {
      final List<String> types = PSResourceLoader.loadMimeTypes();
      assertEquals(types.size(), new HashSet<String>(types).size());
   }
}

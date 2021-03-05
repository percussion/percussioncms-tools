/* *****************************************************************************
 *
 * [ PSLoaderRepositoryHandlerTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader;

import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSContentSelectorDefTest;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.objectstore.PSLoaderDescriptorTest;
import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test class for the <code>PSLoaderRepositoryHandler</code> class.
 */
public class PSLoaderRepositoryHandlerTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSLoaderRepositoryHandlerTest(String name)
   {
      super(name);
   }

   /**
    * Tests save and get PSLoaderDescriptor from PSLoaderRepositoryHandler
    *
    * @throws Exception if there are any errors.
    */
   public void testSaveGetDescriptor() throws Exception
   {
      // make the temp directory
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      String tmpPath = tmpDir.getAbsolutePath(); 
      String path = tmpPath + "/" + DESCRIPTOR_NAME;
      File pathDir = new File(path);
      if (pathDir.exists()) // cleanup old stuff if needed
         pathDir.delete();
      pathDir.mkdirs();

      // copy the PSLoaderDescriptorTest.xml -> pathDir
      InputStream in = PSLoaderDescriptorTest.class
            .getResourceAsStream(DESCRIPTOR_XML);
      File descPath = new File(path + "/" + DESCRIPTOR_XML);
      FileOutputStream out = new FileOutputStream(descPath);
      IOTools.copyStream(in, out);
      in.close();
      out.close();
      
      // load the descriptor
      in = new FileInputStream(descPath); 
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);      
      PSLoaderDescriptor src = new PSLoaderDescriptor(path);
      src.fromXml(doc.getDocumentElement());
      
      // modify the descriptor
      PSContentSelectorDef slct =
         PSContentSelectorDefTest.getContentSelectorDef();
      src.setContentSelectorDef(slct);

      // save the descriptor
      PSLoaderRepositoryHandler handler;
      handler = new PSLoaderRepositoryHandler(path);
      handler.saveDescriptor(src);

      // load the saved descriptor
      PSLoaderDescriptor tgt = handler.getDescriptor();

      // TODO: fix "src" == "tgt", then use assertTrue(src.equals(tgt));
      assertTrue(src.getConnectionDef().equals(tgt.getConnectionDef()));
      assertTrue(src.getContentSelectorDef().equals(tgt.getContentSelectorDef()));
      assertTrue(src.getLoaderDef().equals(tgt.getLoaderDef()));
      assertTrue(src.getAllExtractorDefs().size() == tgt.getAllExtractorDefs()
            .size());
      
      // cleanup
      pathDir.delete();
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSLoaderRepositoryHandlerTest("testSaveGetDescriptor"));
      return suite;
   }
   
   /**
    * The descriptor name for this test. 
    */
   private String DESCRIPTOR_NAME = "PSLoaderDescriptorTest";

   /**
    * The XML file name of the tested descriptor. 
    */
   private String DESCRIPTOR_XML = DESCRIPTOR_NAME + ".xml";
}

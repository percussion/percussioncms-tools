/*[ PSContentSelectorDefTest.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSContentSelectorDef</code> and
 * <code>PSFileSelectorDef</code> class.
 */
public class PSContentSelectorDefTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSContentSelectorDefTest(String name)
   {
      super(name);
   }
   
   /**
    * Tests all Xml functions for PSContentSelectorDef class
    *
    * @throws Exception if there are any errors.
    */
   public void testContentSelectorXml() throws Exception
   {
      PSContentSelectorDef src = getContentSelectorDef();

      // create the XML from the "src" object
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);

      //System.out.println(PSXmlDocumentBuilder.toString(srcEl));

      // create the target object, tgt, from the XML of the "src"
      PSContentSelectorDef tgt = new PSContentSelectorDef(srcEl);

      // "src" == "tgt"
      assertTrue(src.equals(tgt));
   }

   /**
    * Creates an instance of PSContentSelectorDef class
    * 
    * @return PSContentSelectorDef object, never <code>null</code>.
    */
   public static PSContentSelectorDef getContentSelectorDef()
   {
       PSContentSelectorDef src = new PSContentSelectorDef("s1",
         "com.percussion.loader.PSContentSelector");
      PSProperty prop = new PSProperty("p1", "v1");
      src.addProperty(prop);
      PSSearchRoot sr1 = PSSearchRootTest.getSearchRoot("src1");
      PSSearchRoot sr2 = PSSearchRootTest.getSearchRoot("src2");
      src.addSearchRoot(sr1);
      src.addSearchRoot(sr2);

      return src;     
   }

   /**
    * Creates a file selector
    * 
    * @param searchRoot The search root value, assume not <code>null</code>
    *    or empty.
    *    
    * @return The created file selector, never <code>null</code>.
    * 
    * @throws Exception if any error occurs.
    */
   public static PSFileSelectorDef createFileSelector(String searchRoot) 
      throws Exception
   {
      PSContentSelectorDef cs = new PSContentSelectorDef("File Selector", 
         PSFileSelectorDef.PLUGIN_CLASS);
      
      // prepare the search root object
      PSSearchRoot fsRoot = new PSSearchRoot("File Search Root");

      // add the search root property
      PSProperty prop = new PSProperty(PSFileSearchRoot.XML_SEARCHROOT_NAME,
         searchRoot);
      fsRoot.addProperty(prop);
      
      // add the recurse property
      prop = new PSProperty(PSFileSearchRoot.XML_RECURSE_NAME, 
         PSLoaderComponent.XML_TRUE);
      fsRoot.addProperty(prop);
      
      cs.addSearchRoot(fsRoot);
      
      return new PSFileSelectorDef(cs);
   }
   
   /**
    * Tests all Xml functions for PSFileSelectorDef class, including to and 
    * from a XML file.
    *
    * @throws Exception if there are any errors.
    */
   public void testXmlForFileSelector() throws Exception
   {
      PSFileSelectorDef src = createFileSelector(
         "file:/C:/inetpub/wwwroot/FileSelectorTest/");

      File file = new File("PSXFileSelector.xml");
      saveComponentToFile(file, src);
      
      PSFileSelectorDef tgt = (PSFileSelectorDef) getComponentFromFile(file);
      
      // "src" == "tgt"
      assertTrue(src.equals(tgt));            
   }

   /**
    * Get a selector definition (as a loader component) object from a 
    * specified file.
    * 
    * @param docFile The file contains a component.
    * 
    * @return The retrieved component, never <code>null</code>
    * 
    * @throws Exception if any error occurs.
    */   
   private static PSLoaderComponent getComponentFromFile(File docFile)
      throws Exception
   {
      Document doc = getDocumentFromFile(docFile);
      
      Element compEl = doc.getDocumentElement();
      PSContentSelectorDef comp = null;
      
      try
      {
         if (compEl.getNodeName().equals(PSContentSelectorDef.XML_NODE_NAME))
         {
            comp = new PSContentSelectorDef(compEl);
            if (comp.getPlugInClass().equals(PSFileSelectorDef.PLUGIN_CLASS))
               comp = new PSFileSelectorDef(comp );
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      
      return comp;
   }
   
   /**
    * Get the document from the specified file.
    *
    * @param docFile The <code>File</code> object that the document is
    * retrieved from, may not  be <code>null</code>.
    *
    * @return The retrieved document, never <code>null</code>.
    *
    * @throws Exception if any error occurs.
    */
   public static Document getDocumentFromFile(File docFile) throws Exception
   {
      if(docFile == null)
         throw new IllegalArgumentException("docFile may not be null.");
         
      if (!docFile.exists())
      {
         throw new Exception("docFile not exist");
      }

      FileInputStream in = null;
      Document respDoc = null;
      try
      {
         in = new FileInputStream(docFile);
         return PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch (Exception e)
      {
         throw e;
      }
      finally
      {
         if (in != null)
            try {in.close();} catch(IOException ex){}
      }

   }
   /**
    * Save a loader component to a file.
    *
    * @param compFile The file need to saved to, assume not <code>null</code>.
    * @param comp The to be saved component, assume not <code>null</code>
    *
    * @throws Exception if an error occures.
    */
   public static void saveComponentToFile(File compFile, PSLoaderComponent comp) 
      throws Exception
   {
      FileOutputStream out = null;
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element compEl = comp.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, compEl);
         out = new FileOutputStream(compFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (Exception e)
      {
         throw e;
      }
      finally
      {
         if (out != null)
            try {out.close();} catch(IOException ex){}
      }
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSContentSelectorDefTest("testXmlForFileSelector"));
      suite.addTest(new PSContentSelectorDefTest("testContentSelectorXml"));
      return suite;
   }

}

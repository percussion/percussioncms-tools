/*[ PSSearchRootTest.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSSearchRootTest</code> class.
 */
public class PSSearchRootTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSSearchRootTest(String name)
   {
      super(name);
   }

   /**
    * Tests equals for <code>PSSearchRoot</code>.
    *
    * @throws Exception if there are any errors.
    */
   public void testEqualSearchRoot() throws Exception
   {
      // test generic class
      PSSearchRoot src1 = getSearchRoot("src1");
      PSSearchRoot src11 = getSearchRoot("src1");
      PSSearchRoot src2 = getSearchRoot("src2");
      
      assertTrue(src1.equals(src11));
      assertTrue(! src1.equals(src2));
   }
   
   /**
    * Tests toXml and fromXml for <code>PSFileSearchRoot</code>.
    *
    * @throws Exception if there are any errors.
    */
   public void testFileSearchRootXml() throws Exception
   {
      PSFileSearchRoot src = new PSFileSearchRoot("src1", ".", false);
      src.addFilter(new PSFilter("default", "*"));
      src.addFilter(new PSFilter("image", "*.gif"));
   
      
      
      // create the XML from the "src" object
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);

      //System.out.println(PSXmlDocumentBuilder.toString(srcEl));
      
      // create the target object, tgt, from the XML of the "src"
      PSFileSearchRoot tgt = new PSFileSearchRoot(srcEl);

      // "src" == "tgt"
      assertTrue(src.equals(tgt));
   }

   /**
    * Tests all Xml functions.
    *
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSSearchRoot src = getSearchRoot("src1");
      
      // create the XML from the "src" object
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);

      //System.out.println(PSXmlDocumentBuilder.toString(srcEl));
      
      // create the target object, tgt, from the XML of the "src"
      PSSearchRoot tgt = new PSSearchRoot(srcEl);

      // "src" == "tgt"
      assertTrue(src.equals(tgt));
   }

   public static PSSearchRoot getSearchRoot(String base)
   {
      // create the source object, src
      PSSearchRoot src = new PSSearchRoot(base + "sr1");
      PSProperty p1 = new PSProperty(base + "p1", "v1");
      PSProperty p2 = new PSProperty("p2", "v2");
      src.addProperty(p1);
      src.addProperty(p2);
      PSFilter f1 = new PSFilter(base + "f1", "fv1");
      PSFilter f2 = new PSFilter("f2", "fv1");
      src.addFilter(f1);
      src.addFilter(f2);
      
      return src;
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSSearchRootTest("testEqualSearchRoot"));
      suite.addTest(new PSSearchRootTest("testFileSearchRootXml"));
      suite.addTest(new PSSearchRootTest("testXml"));
      return suite;
   }

}

/*[ PSLoaderDefTest.java ]*****************************************************
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
 * Unit test class for the <code>PSLoaderDef</code> class.
 */
public class PSLoaderDefTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSLoaderDefTest(String name)
   {
      super(name);
   }
   
   public static void main(String args[]) 
   {
      junit.textui.TestRunner.run(PSLoaderDefTest.class);
   }

   /**
    * Basic test to test out some xml objectstore classes 
    */
   public void testDefXml() throws Exception
   {
      PSLoaderDef aDef = createLoaderDef();
      PSLoaderDef aDifferentDef = createLoaderDef();
      aDifferentDef.addProperty(new PSProperty("make me", "different"));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element aEl = aDef.toXml(doc); // to Xml

      // Just to see
      //System.out.println(
      //"TO XML: \n" + PSXmlDocumentBuilder.toString(aEl));

      PSLoaderDef tgtDef = new PSLoaderDef(aEl); // From XML
     
      // Just to see
      //System.out.println("FROM XML: \n" 
        // + PSXmlDocumentBuilder.toString(aEl));

      assertTrue(aDef.equals(tgtDef)); // equals works?
      assertTrue(!aDef.equals(aDifferentDef)); // equal works?   
   }

   /**
    * Creates a PSLoaderDef for testing. 
    * 
    * @return a PSLoaderDef, never <code>null</code>.
    */
   public PSLoaderDef 
      createLoaderDef()
   {
      PSLoaderDef wf = new PSLoaderDef(
         "somename", "com.percussion.bla.blahh");
  
      wf.addProperty(new PSProperty("somename", "somevalue"));
      wf.addProperty(new PSProperty("foo", "bar"));
      wf.addProperty(new PSProperty("hello", "world"));
      wf.addProperty(new PSProperty("hello", "Ben!"));

      return wf;     
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(
         new PSLoaderDefTest("testDefXml"));
      return suite;
   }

}

/******************************************************************************
 *
 * [ PSLoaderDescriptorTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSLoaderDescriptor</code>
 */
public class PSLoaderDescriptorTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSLoaderDescriptorTest(String name)
   {
      super(name);
   }

   /**
    * Tests all Xml functions for PSLoaderDescriptor class
    *
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSContentSelectorDef slct = 
         PSContentSelectorDefTest.getContentSelectorDef();

      InputStream in = getClass().getResourceAsStream(
            "PSLoaderDescriptorTest.xml");
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);      
      PSLoaderDescriptor src = new PSLoaderDescriptor(doc.getDocumentElement());
      src.setContentSelectorDef(slct);

      // create the XML from the "src" object
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc2);

      // create the target object, tgt, from the XML of the "src"
      PSLoaderDescriptor tgt = new PSLoaderDescriptor(srcEl);

      // "src" == "tgt"
      assertTrue(src.equals(tgt));
   }


   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSLoaderDescriptorTest("testXml"));
      return suite;
   }

}

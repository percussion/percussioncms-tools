/* *****************************************************************************
 *
 * [ PSErrorHandlingDefTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSErrorHandlingDef</code> class.
 */
public class PSErrorHandlingDefTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSErrorHandlingDefTest(String name)
   {
      super(name);
   }
   
   public static void main(String args[]) 
   {
      junit.textui.TestRunner.run(PSErrorHandlingDefTest.class);
   }

   /**
    * Basic test to test out some xml objectstore classes 
    */
   public void testDefXml() throws Exception
   {
      /**
       * test out the PSErrorHandlingDef class
       */
      PSErrorHandlingDef aDef = createDef();
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element aEl = aDef.toXml(doc); // to Xml

      // Just to see
      //System.out.println(
      //"TO XML: \n" + PSXmlDocumentBuilder.toString(aEl));

      PSErrorHandlingDef tgtDef = new PSErrorHandlingDef(aEl); // From XML
     
      // Just to see
      //System.out.println("FROM XML: \n" 
      //   + PSXmlDocumentBuilder.toString(aEl));

      assertTrue(aDef.equals(tgtDef)); // equals works?
      
      tgtDef.addProperty(new PSProperty("not", "equals"));
      assertTrue(!aDef.equals(tgtDef)); // equals works?
   
      assertTrue(aDef.getEmailOnError() == true);
      assertTrue(aDef.getEmailOnSuccess() == false);
   
   }

   /**
    * Creates a PSErrorHandlingDef for testing. 
    * 
    * @return a PSErrorHandlingDef, never <code>null</code>.
    */
   public PSErrorHandlingDef 
      createDef()
   {
      PSErrorHandlingDef aDef = new PSErrorHandlingDef();
      aDef.addProperty(new PSProperty(
         PSErrorHandlingDef.EMAIL_ON_ERROR_PROP,
         PSLoaderComponent.XML_TRUE));
      
      aDef.addProperty(new PSProperty(
         PSErrorHandlingDef.EMAIL_ON_SUCCESS_PROP,
         PSLoaderComponent.XML_FALSE));
      
      aDef.addProperty(new PSProperty(
         PSErrorHandlingDef.STOP_ON_ERROR_PROP,
         PSLoaderComponent.XML_FALSE));
      
      PSEmailDef email = new PSEmailDef("pan", false, "ben@perc.com",
            "admin@perc.com");
      email.addRecipient("marting@percussion.comCC", true);
      //email.addRecipient("ben@percussion.comCC", true);
      //email.addRecipient("ben@percussion.com", false);
      aDef.setEmail(email);
     
      return aDef;     
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(
         new PSErrorHandlingDefTest("testDefXml"));
      return suite;
   }

}

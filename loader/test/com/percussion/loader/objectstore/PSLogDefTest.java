/*[ PSLogDefTest.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit test class for the <code>PSWorkflowDef</code> and
 * <code>PSMimeTypeDef</code> class.
 */
public class PSLogDefTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSLogDefTest(String name)
   {
      super(name);
   }
   
   public static void main(String args[]) 
   {
      junit.textui.TestRunner.run(PSLogDefTest.class);
   }

   /**
    * Basic test to test out some xml objectstore classes 
    */
   public void testDefXml() throws Exception
   {
      /**
       * test out the PSLogDefTest class
       */
      PSLogDef aDef = createDef();
   
      System.out.println("Default Log Level: " + 
         aDef.getDefaultLogLevel(null));

      System.out.println("Set Log Level ");
      aDef.setDefaultLogLevel(PSLogDef.ERROR);

      System.out.println("Default Log Level: " + 
         aDef.getDefaultLogLevel(null));

      System.out.println("Is File " + aDef.isFile());
      System.out.println("Setting file on ");
      aDef.setFile(true);
      System.out.println("Is File " + aDef.isFile());
      System.out.println("Setting File Off ");
      aDef.setFile(false);
      System.out.println("Is File " + aDef.isFile());

      
      System.out.println("Is Console " + aDef.isConsole());
      System.out.println("Setting Console On ");
      aDef.setConsole(true);
      System.out.println("Is Console " + aDef.isConsole());
      System.out.println("Setting console off ");
      aDef.setConsole(false);
      System.out.println("Is Console " + aDef.isConsole());


   }

   /**
    * Creates a PSLogDef for testing. 
    * 
    * @return a PSLogDef, may be <code>null</code>.
    */
   public PSLogDef 
      createDef()
   {
      Document d = null;
      InputStream inStream = null;
      PSLogDef wf = null;

      try
      {
         inStream = new FileInputStream(
         "loader/src/com/percussion/loader/objectstore/PSLoaderDescriptorTest.xml");

         d = PSXmlDocumentBuilder.createXmlDocument(
            inStream, false);

         //NodeList list = d.getElementsByTagNameNS("*",
          //  PSLogDef.XML_NODE_NAME);
         NodeList list = d.getElementsByTagName(PSLogDef.XML_NODE_NAME);

         System.out.println(list);
         Element s = (Element) list.item(0);

         System.out.println("Testing");
         System.out.println(PSXmlDocumentBuilder.toString(s));

         wf = new PSLogDef(s);         
      }
      catch (Exception e)
      {
         e.printStackTrace(System.out);
         return null;
      }

      return wf;     
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(
         new PSLogDefTest("testDefXml"));
      return suite;
   }

}

/*[ PSWorkflowMimeDefTest.java ]************************************************
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
 * Unit test class for the <code>PSWorkflowDef</code> and
 * <code>PSMimeTypeDef</code> class.
 */
public class PSWorkflowMimeDefTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSWorkflowMimeDefTest(String name)
   {
      super(name);
   }

   public static void main(String args[])
   {
      junit.textui.TestRunner.run(PSWorkflowMimeDefTest.class);
   }

   /**
    * Basic test to test out some xml objectstore classes
    */
   public void testWFDefXml() throws Exception
   {
      /**
       * test out the work flow definition
       */
      PSWorkflowDef wfDef = createWorkflowDef();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element wfEl = wfDef.toXml(doc); // to Xml

      // Just to see
      System.out.println("TO XML: \n" + PSXmlDocumentBuilder.toString(wfEl));

      PSWorkflowDef tgtDef = new PSWorkflowDef(wfEl); // From XML

      // Just to see
      System.out.println("\nFrom XML: \n" +PSXmlDocumentBuilder.toString(wfEl));

      assertTrue(wfDef.equals(tgtDef));
   }

   /**
    * Basic test to test out some xml objectstore classes
    */
   public void testMTDefXml() throws Exception
   {
      /**
       * test out the work flow definition
       */
      PSMimeTypeDef wfDef = createMimeTypeDef();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element wfEl = wfDef.toXml(doc); // to Xml

      // Just to see
      //System.out.println("TO XML: \n" + PSXmlDocumentBuilder.toString(wfEl));

      PSMimeTypeDef tgtDef = new PSMimeTypeDef(wfEl); // From XML

      // Just to see
      //System.out.println(PSXmlDocumentBuilder.toString(wfEl));

      assertTrue(wfDef.equals(tgtDef));
   }

   /**
    * Creates a PSWorkflowDef for testing.
    *
    * @return a PSWorkflowDef, never <code>null</code>.
    */
   public PSWorkflowDef
      createWorkflowDef()
   {
      PSWorkflowDef wf = new PSWorkflowDef("attNameVal");
      
      wf.addTransition(new PSTransitionDef("insert", "insertTrigger"),
         PSWorkflowDef.TRANS_INSERT);
      wf.addTransition(new PSTransitionDef("insert2", 
         "insertTrigger2"), PSWorkflowDef.TRANS_INSERT);
      wf.addTransition(new PSTransitionDef("preupdate", 
         "preupdateTrigger"), PSWorkflowDef.TRANS_PREUPDATE);
      wf.addTransition(new PSTransitionDef("postupdate", 
         "postupdateTrigger"), PSWorkflowDef.TRANS_POSTUPDATE);

      return wf;
   }

   /**
    * Creates a PSMimeTypeDef for testing.
    *
    * @return a PSMimeTypeDef, never <code>null</code>.
    */
   public PSMimeTypeDef
      createMimeTypeDef()
   {
      PSMimeTypeDef mt = new PSMimeTypeDef(
         "attNameVal", new PSExtensionDef("someext"));

      return mt;
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(
         new PSWorkflowMimeDefTest("testWFDefXml"));
      suite.addTest(
         new PSWorkflowMimeDefTest("testMTDefXml"));
      return suite;
   }

}

/*[ PSExtractorDefTest.java ]**************************************************
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
public class PSExtractorDefTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSExtractorDefTest(String name)
   {
      super(name);
   }
   
   public static void main(String args[]) 
   {
      junit.textui.TestRunner.run(PSExtractorDefTest.class);
   }

   /**
    * Basic test to test out some xml objectstore classes 
    */
   public void testDefXml() throws Exception
   {
      PSExtractorDef aDef = createExtractorDef();
     
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element aEl = aDef.toXml(doc); // to Xml

      // Just to see
      System.out.println(
      "TO XML: \n" + PSXmlDocumentBuilder.toString(aEl));

      PSExtractorDef tgtDef = new PSExtractorDef(aEl); // From XML
     
      // Just to see
      System.out.println("FROM XML: \n" 
         + PSXmlDocumentBuilder.toString(tgtDef.toXml(doc)));

      assertTrue(aDef.equals(tgtDef));
   }

   /**
    * Creates a PSExtractorDef for testing. 
    * 
    * @return a PSExtractorDef, never <code>null</code>.
    */
   public PSExtractorDef 
      createExtractorDef()
   {
      PSExtractorDef extrDef = new PSExtractorDef("anExt",
         PSExtractorDef.TYPE_STATIC, "com.percussion.someclass"); 
     
      extrDef.addFilter(new PSFilter("somefilter", "\\d"));
      extrDef.addFilter(new PSFilter("somefilter1", "\\d\\d"));
      extrDef.addMimeType(new PSMimeTypeDef("image/gif", 
         new PSExtensionDef("gif")));
      extrDef.addMimeType(new PSMimeTypeDef("application/x-javascript", 
         new PSExtensionDef("js")));
      
      extrDef.addProperty(new PSProperty("prop1", "prop1value"));
      extrDef.addProperty(new PSProperty("prop2", "extrDef"));
      extrDef.addProperty(new PSProperty("prop3", "prop3value"));

      extrDef.addTransformaton(new PSTransformationDef(
         "trans1", "com.percussion.someclass1"), true);

      extrDef.addTransformaton(new PSTransformationDef(
         "trans2", "com.percussion.someclass2"), true);

      extrDef.addTransformaton(new PSFieldTransformationDef(
         "trans1", "com.percussion.someclass1", "localField1"), false);

      extrDef.addTransformaton(new PSFieldTransformationDef(
         "trans2", "com.percussion.someclass2", "localField2"), false);

      extrDef.addFieldProperty(new PSFieldProperty("sys_title", "$filename", 
         "Variable"));
      extrDef.addFieldProperty(new PSFieldProperty("body", "$content", "Variable"));

      PSTransitionDef trans = new PSTransitionDef("Submit", "Submit");
      PSTransitionDef trans1 = new PSTransitionDef("Approve", "Approve");
      PSTransitionDef trans2 = new PSTransitionDef("Public", "Public");
      PSWorkflowDef workflow = new PSWorkflowDef("Article");
      workflow.addTransition(trans1, PSWorkflowDef.TRANS_INSERT);
      workflow.addTransition(trans2, PSWorkflowDef.TRANS_INSERT);
      extrDef.setWorkflowDef(workflow);     
      
      return extrDef;
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(
         new PSExtractorDefTest("testDefXml"));
      return suite;
   }

}

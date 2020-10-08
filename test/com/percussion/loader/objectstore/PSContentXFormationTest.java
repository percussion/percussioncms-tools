/*[ PSContentXFormationTest.java ]*********************************************
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
 * Unit test class for the <code>PSContentSelectorDef</code> and
 * <code>PSFileSelectorDef</code> class.
 */
public class PSContentXFormationTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSContentXFormationTest(String name)
   {
      super(name);
   }
   
   public static void main(String args[]) 
   {
      junit.textui.TestRunner.run(PSContentXFormationTest.class);
   }

   /**
    * Basic test to create a {@link #PSTransformationDef}
    * and test it's xml representation. 
    */
   public void testTransformationDefXml() throws Exception
   {
      PSTransformationDef src = getTransformationDef();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);
      
      System.out.println(PSXmlDocumentBuilder.toString(srcEl));
      
      System.out.println("Entering Constructor(node)\n");
      PSTransformationDef tgt = new PSTransformationDef(srcEl);
      System.out.println("Finished Constructor(node)\n");
      
      doc = PSXmlDocumentBuilder.createXmlDocument();
      
      srcEl = tgt.toXml(doc);
      System.out.println("Finished TOXML");

      System.out.println(PSXmlDocumentBuilder.toString(srcEl));
      assertTrue(src.equals(tgt));
   }

   /**
    * Creates a PSTransformationDef object with some basic
    * parameters.
    * 
    * @returns a new {@link #PSTransformationDef} never <code>null</code>.
    */
   public PSTransformationDef getTransformationDef()
   {
      PSTransformationDef src = 
         new PSTransformationDef("transformation", 
         "com.percussion.loader.ATransformer");
      PSParamDef param = 
         new PSParamDef("param1", "java.lang.String", 
         "This is some description");
      PSParamDef param1 = 
         new PSParamDef("param2", "java.lang.String", 
         "This is some description 1");

      src.addParameter(param);
      src.addParameter(param1);

      return src;     
   }
 
   /**
    *   collect all tests into a TestSuite and return it
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSContentXFormationTest("testTransformationDefXml"));
      return suite;
   }

}

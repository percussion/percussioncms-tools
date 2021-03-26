/*[ PSTransformationBucketTest.java ]*******************************************
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
 * Unit test class for the <code>PSItemTransformationsDef</code> and
 * <code>PSFieldTransformationsDef</code> class.
 */
public class PSTransformationBucketTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSTransformationBucketTest(String name)
   {
      super(name);
   }

   public static void main(String args[])
   {
      junit.textui.TestRunner.run(PSTransformationBucketTest.class);
   }

   /**
    * Basic test to test out some PSTransformationBucket
    * derived classes.
    */
   public void testTransformationDefXml() throws Exception
   {
      PSFieldTransformationsDef fieldX =
         (PSFieldTransformationsDef) getTransformationBucketDef(false);
      PSItemTransformationsDef itemX =
         (PSItemTransformationsDef) getTransformationBucketDef(true);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = fieldX.toXml(doc);
      Element srcElItem = itemX.toXml(doc);

      PSFieldTransformationsDef tgt = new PSFieldTransformationsDef(srcEl);
      PSItemTransformationsDef tgtItems =
         new PSItemTransformationsDef(srcElItem);

      doc = PSXmlDocumentBuilder.createXmlDocument();

      srcEl = tgt.toXml(doc);
      srcElItem = tgtItems.toXml(doc);

      System.out.println(PSXmlDocumentBuilder.toString(srcEl));

      assertTrue(fieldX.equals(tgt));
      assertTrue(itemX.equals(tgtItems));
   }

   /**
    * Creates a PSTransformationBucket.
    *
    * @param bItems if <code>true</code>
    *    PSItemTransformationsDef is returned, otherwise
    *    a PSFieldTransformationsDef is returned.
    *
    * @return a PSTransformationBucket, never <code>null</code>.
    */
   public PSTransformationBucket
      getTransformationBucketDef(boolean bItems)
   {
      PSTransformationBucket bucket = null;

      /**
       * Create a dummy xml doc
       */
      if (!bItems)
      {
         bucket = new PSFieldTransformationsDef();
         bucket.addTransformation(getTransformationDef(0, bItems));
         bucket.addTransformation(getTransformationDef(1, bItems));
         bucket.addTransformation(getTransformationDef(2, bItems));
      }
      else
      {
         bucket = new PSItemTransformationsDef();
         bucket.addTransformation(getTransformationDef(10, bItems));
         bucket.addTransformation(getTransformationDef(11, bItems));
         bucket.addTransformation(getTransformationDef(12, bItems));
      }

      return bucket;
   }

   /**
    * Creates a PSTransformationDef with some default parameters.
    *
    * @param i int to use to construct name of the transformation.
    *
    * @return PSTransformationDef Never <code>null</code>
    */
   public PSTransformationDef getTransformationDef(int i, boolean isItem)
   {
      PSTransformationDef src;
      if (isItem)
         src = new PSTransformationDef(
            "transformation" + i, "com.percussion.loader.ATransformer");
      else
         src = new PSFieldTransformationDef("transformation" + i,
            "com.percussion.loader.ATransformer", "field" + i);

      PSParamDef param =
         new PSParamDef(
         "param1", "java.lang.String", "This is some description");
      PSParamDef param1 =
         new PSParamDef(
         "param2", "java.lang.String", "This is some description 1");

      src.addParameter(param);
      src.addParameter(param1);

      return src;
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(
         new PSTransformationBucketTest("testTransformationDefXml"));
      return suite;
   }

}

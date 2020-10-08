/**[ PSStructuralModelTest ]****************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.IPSContentSelector;
import com.percussion.loader.IPSContentTree;
import com.percussion.loader.PSPluginFactory;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSFileSearchRoot;
import com.percussion.loader.objectstore.PSProperty;
import com.percussion.loader.objectstore.PSSearchRoot;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;


/**
 * This is a container for all the selector tests.
 *
 */
public class PSStructuralModelTest extends TestCase
{
   public PSStructuralModelTest(String name)
   {
      super(name);
   }

   public static void main(String args[])
   {
      junit.textui.TestRunner.run(PSStructuralModelTest.class);
   }

   protected void setUp()
   {
      BasicConfigurator.configure();
   }

   /**
    * Tests a structural model based on a file crawl.
    */
   public void testWebContentSelector()
   {
      try
      {
         PSContentSelectorDef csDef =
            new PSContentSelectorDef(
            "testing",
            "com.percussion.loader.selector.PSFileSelector");
         PSSearchRoot psRoot = new PSSearchRoot("testcrawl");
         psRoot.addProperty(
            new PSProperty(
            PSFileSearchRoot.XML_SEARCHROOT_NAME,
            "C:/FileSelectorTest"));

         csDef.addSearchRoot(psRoot);

         PSPluginFactory pFactory = PSPluginFactory.getInstance();
         IPSContentSelector cs = pFactory.newContentSelector(csDef);
         // do a scan
         IPSContentTree tree = cs.scan();

         // Display
         System.out.println("Dependency Tree =========> ");
         PSSelectorTest.writeOut(tree, System.out);
         System.out.println("Dependency Tree =========. ");

         // Construct roots
         ArrayList v = new ArrayList();
         v.add("C:/FileSelectorTest");

         // Build a structural model of the dependency model
         PSStructTreeModel m = new PSStructTreeModel(v.iterator());
         m.importDependencyModel(tree);
         tree = new PSDefaultContentTree(m);

         // Display
         System.out.println("Structural Tree =========> ");
         PSSelectorTest.writeOut(tree, System.out);
         System.out.println("Structural Tree =========. ");
      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }

   /**
    *  Tests a structural model based on a List import
    */
   /*public void testImportSelector()
   {
      try
      {
         PSContentSelectorDef csDef =
            new PSContentSelectorDef(
            "testing",
            "com.percussion.loader.selector.PSListContentSelector");

         csDef.addProperty(
            new PSProperty(
            PSListSelectorDef.CONTENT_LIST,
            "./rxconfig/ContentLoader/listimport.xml"));

         PSPluginFactory pFactory = PSPluginFactory.getInstance();
         // This calls configure on the plugin ...
         IPSContentSelector cs = pFactory.newContentSelector(csDef);
         // do a scan
         IPSContentTree tree = cs.scan();
         System.out.println("Tree ===> ");
         PSSelectorTest.writeOut(tree, System.out);

         // Construct roots
         // Build a structural model of the dependency model
         PSStructTreeModel m = new PSStructTreeModel(
            PSLoaderUtils.getListRoots("./rxconfig/ContentLoader/listimport.xml"));
         m.importDependencyModel(tree);
         tree = new PSDefaultContentTree(m);

         // Display
         System.out.println("Structural Tree =========> ");
         PSSelectorTest.writeOut(tree, System.out);
         System.out.println("Structural Tree =========. ");

      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }*/

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSStructuralModelTest("testWebContentSelector"));
      //suite.addTest(new PSStructuralModelTest("testImportSelector"));
      return suite;
   }
}

/*[ PSSelectorImportTest.java ]************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.IPSContentSelector;
import com.percussion.loader.IPSContentTree;
import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSPluginFactory;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSListSelectorDef;
import com.percussion.loader.objectstore.PSProperty;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.PrintStream;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.Document;


/**
 * Tests the list importing selector. See ListContentSelector.dtd
 * for details of importing format.
 *
 */
public class PSSelectorImportTest extends TestCase
{
   public PSSelectorImportTest(String name)
   {
      super(name);
   }

   public static void main(String args[])
   {
      junit.textui.TestRunner.run(PSSelectorImportTest.class);
   }

   protected void setUp()
   {
      BasicConfigurator.configure();
   }

   public void testImportSelector()
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

         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         System.out.println(PSXmlDocumentBuilder.toString(csDef.toXml(doc)));
         PSPluginFactory pFactory = PSPluginFactory.getInstance();
         // This calls configure on the plugin ...
         IPSContentSelector cs = pFactory.newContentSelector(csDef);
         // do a scan
         IPSContentTree tree = cs.scan();
         System.out.println("Tree ===> ");
         writeOut(tree, System.out);
         System.out.println("<=== Tree Out ");

         // Test out the substitute
         System.out.println("Root url's: ===>" );
         PSSelectorTest.writeOutRootsFromEachNode(tree, csDef, System.out);
         System.out.println("<=== Root url's" );
      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }

   /**
    * Convenience method to write out a IPSContentTree
    *
    * @param tree a IPSContentTree to print. Never <code>null</code>
    *
    * @param out a Stream to write to. Never <code>null</code>
    *
    * @throws IllegalArgumentException if any invalid parameters
    *    are found.
    */
   private void writeOut(IPSContentTree tree, PrintStream out)
   {
      if (tree == null || out == null)
         throw new IllegalArgumentException(
            "both tree and out must not be null");

      Iterator iter = tree.getRoots();

      while (iter.hasNext())
      {
         System.out.print("Root:");
         writeNode((IPSContentTreeNode) iter.next(), out, "");
      }
   }

   /**
    * Convenience method to write out a tree node and all of its children
    *
    * @param tree a IPSContentTreeNode to print. Never <code>null</code>
    *
    * @param out a Stream to write to. Never <code>null</code>
    *
    * @param strTab a String to append when printing. Never <code>null</code>
    *    may be empty.
    *
    * @throws IllegalArgumentException if any invalid parameters
    *    are found.
    */
   private void writeNode(IPSContentTreeNode node, PrintStream out,
      String strTab)
   {
      if (node == null ||out == null || strTab == null)
         throw new IllegalArgumentException(
            "strTab, node and out must not be null");

      // Print the node
      PSItemContext ic = node.getItemContext();
      out.println(strTab + ic.getResourceId());

      if (!node.hasChildren())
         return;

      Iterator children = node.getChildren();

      while (children.hasNext())
         writeNode((IPSContentTreeNode) children.next(), out, strTab + "\t");
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSSelectorImportTest("testImportSelector"));
      return suite;
   }
}

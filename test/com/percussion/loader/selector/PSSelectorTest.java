/**[ PSSelectorTest ]**********************************************************
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
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.PSPluginFactory;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSFileSearchRoot;
import com.percussion.loader.objectstore.PSProperty;
import com.percussion.loader.objectstore.PSSearchRoot;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.PrintStream;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.Document;


/**
 * This is a container for all the selector tests.
 *
 */
public class PSSelectorTest extends TestCase implements IPSCrawlListener
{
   public PSSelectorTest(String name)
   {
      super(name);
   }

   public static void main(String args[])
   {
      junit.textui.TestRunner.run(PSSelectorTest.class);
   }

   protected void setUp()
   {
      BasicConfigurator.configure();
   }

   public void testWebContentSelector()
   {
      try
      {
         PSContentSelectorDef csDef =
            new PSContentSelectorDef(
            "testing",
            /*"com.percussion.loader.selector.PSWebContentSelector"*/
            "com.percussion.loader.selector.PSFileSelector");
         PSSearchRoot psRoot = new PSSearchRoot("testcrawl");
         psRoot.addProperty(
            new PSProperty(
            PSFileSearchRoot.XML_SEARCHROOT_NAME,
         "C:/FileSelectorTest"));
         //"C:/inetpub/wwwroot/testcrawl/"));

         PSSearchRoot psRoot1 = new PSSearchRoot("testcrawl1");
         psRoot1.addProperty(
            new PSProperty(
            PSFileSearchRoot.XML_SEARCHROOT_NAME,
            "C:/inetpub/wwwroot/testcrawl2/"));

         // Test multiple roots
         csDef.addSearchRoot(psRoot);
         //csDef.addSearchRoot(psRoot1);

         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         System.out.println(PSXmlDocumentBuilder.toString(csDef.toXml(doc)));
         PSPluginFactory pFactory = PSPluginFactory.getInstance();
         // This calls configure on the plugin ...
         IPSContentSelector cs = pFactory.newContentSelector(csDef);
         // do a scan
         IPSContentTree tree = cs.scan();
         System.out.println("Tree =========> ");
         writeOut(tree, System.out);
         System.out.println("Tree =========. ");

         // Test out the substitute
         //writeOutRootsFromEachNode(tree, csDef, System.out);

      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }

   /**
    * Convenience method to write out the roots from each IPSContentTreeNode
    *
    * @param tree a IPSContentTree to print. Never <code>null</code>
    *
    * @param def a PSContentSelectorDef if not <code>null</code> it will
    *    print out the true root url for a given node. May be <code>null
    *    </code>.
    *
    * @param out a Stream to write to. Never <code>null</code>
    *
    * @throws IllegalArgumentException if any invalid parameters
    *    are found.
    */
   public static void writeOutRootsFromEachNode(IPSContentTree tree,
      PSContentSelectorDef def, PrintStream out)
   {
      if (tree == null || out == null)
         throw new IllegalArgumentException(
            "both tree and out must not be null");

      Iterator iter = tree.getRoots();

      while (iter.hasNext())
      {
         writeNodeRoot(((IPSContentTreeNode) iter.next()), def, out, "");
      }
   }

   /**
    * Convenience method to write out the root
    * from a tree node and all of its children
    *
    * @param tree a IPSContentTreeNode to print. Never <code>null</code>
    *
    * @param def a PSContentSelectorDef if not <code>null</code> it will
    *    print out the true root url for a given node. May be <code>null
    *    </code>.
    *
    * @param out a Stream to write to. Never <code>null</code>
    *
    * @param strTab a String to append when printing. Never <code>null</code>
    *    may be empty.
    *
    * @throws IllegalArgumentException if any invalid parameters
    *    are found.
    */
   public static void writeNodeRoot(IPSContentTreeNode node,
      PSContentSelectorDef def, PrintStream out,
      String strTab)
   {
      if (node == null ||out == null || strTab == null)
         throw new IllegalArgumentException(
            "strTab, node and out must not be null");

      // Print the node
      if (def != null)
         out.println(strTab + "Root url: " +
            PSLoaderUtils.getRootUrlFromDesc(def,
            node.getItemContext().getResourceId()));

      out.println(strTab + "Node: " + node.toString());
      out.println(strTab + "Root: " + node.getRoot().toString());

      if (!node.hasChildren())
         return;

      Iterator children = node.getChildren();

      while (children.hasNext())
         writeNodeRoot((IPSContentTreeNode) children.next(),
            def, out, strTab + "\t");
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
   public static void writeOut(IPSContentTree tree, PrintStream out)
   {
      if (tree == null || out == null)
         throw new IllegalArgumentException(
            "both tree and out must not be null");

      Iterator iter = tree.getRoots();

      while (iter.hasNext())
      {
         System.out.print("Root: ");
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
   public static void writeNode(IPSContentTreeNode node, PrintStream out,
      String strTab)
   {
      if (node == null ||out == null || strTab == null)
         throw new IllegalArgumentException(
            "strTab, node and out must not be null");

      // Print the node
      PSItemContext ic = node.getItemContext();
      Object obj = ic.getDataObject();

      if (obj != null)
      {
         try
         {
            websphinx.Link l = (websphinx.Link) obj;
            out.print(strTab +
               websphinx.LinkEvent.eventName[l.getStatus()] + " ");
         }
         catch (Exception ignored)
         {
         }
       }

      out.println(strTab + ic.getResourceId() + " Terminated: " + node.isBranchTerminated());

      out.println("Mimetype: " + ic.getResourceMimeType());
      out.println("Extension: " + ic.getResourceExtension());

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
      suite.addTest(new PSSelectorTest("testWebContentSelector"));
      //suite.addTest(new PSSelectorTest("testCrawler"));
      return suite;
   }

   /**
    * Notify that the crawler started.
    *
    * @param event a {@link #PSCrawlEvent}
    */
   public void started(PSCrawlEvent event)
   {
      System.out.println("started");
   }

   /**
    * Notify that the crawler ran out of links to crawl
    *
    * @param event a {@link #PSCrawlEvent}
    */
   public void stopped(PSCrawlEvent event)
   {
      System.out.println("stopped");
   }

   /**
    * Notify that the crawler's state was cleared.
    *
    * @param event a {@link #PSCrawlEvent}
    */
   public void cleared(PSCrawlEvent event)
   {
   }

   /**
    * Notify that the crawler timed out.
    *
    * @param event a {@link #PSCrawlEvent}
    */
   public void timedOut(PSCrawlEvent event)
   {
   }

   /**
    * Notify that the crawler was paused.
    *
    * @param event a {@link #PSCrawlEvent}
    */
   public void paused(PSCrawlEvent event)
   {
   }

   /**
    * A node was visited.
    *
    * @param event a {@link #PSCrawlEvent}
    */
   public void visited(PSCrawlEvent event)
   {
      System.out.println("visited: " +
         ((websphinx.Link) event.getItemContext().getDataObject()));
   }
}

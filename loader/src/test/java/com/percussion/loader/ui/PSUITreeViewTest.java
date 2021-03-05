/*[ PSUITreeViewTest.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.IPSContentSelector;
import com.percussion.loader.IPSContentTree;
import com.percussion.loader.PSPluginFactory;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSListSelectorDef;
import com.percussion.loader.objectstore.PSProperty;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A way to test the tree view gui. 
 */
public class PSUITreeViewTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSUITreeViewTest(String name)
   {
      super(name);
   }
   
   public static void main(String args[]) 
   {
      junit.textui.TestRunner.run(PSUITreeViewTest.class);
   }

   /**
    * Basic test to test out some xml objectstore classes 
    */
   public void test() throws Exception
   {      
      /*PSContentSelectorDef csDef = 
         new PSContentSelectorDef(
         "testing",             
         "com.percussion.loader.selector.PSFileSelector");
      PSSearchRoot psRoot = new PSSearchRoot("testcrawl");
      psRoot.addProperty(
         new PSProperty(
         PSFileSearchRoot.XML_SEARCHROOT_NAME, 
         "C:/inetpub/wwwroot/testcrawl/"));

      PSSearchRoot psRoot1 = new PSSearchRoot("testcrawl2");
      psRoot1.addProperty(
         new PSProperty(
         PSFileSearchRoot.XML_SEARCHROOT_NAME, 
         "C:/inetpub/wwwroot/testcrawl2/"));
            
      csDef.addSearchRoot(psRoot);
      csDef.addSearchRoot(psRoot1);
*/
      PSContentSelectorDef csDef = 
         new PSContentSelectorDef(
         "testing", 
         "com.percussion.loader.selector.PSListContentSelector");
         
      csDef.addProperty(
         new PSProperty(
         PSListSelectorDef.CONTENT_LIST, 
         "./rxconfig/ContentLoader/listimport.xml"));
      
       
      // get some content
      IPSContentTree tree = getContentTree(csDef);             
      
      // What's in it
      //SelectorTests.writeOut(tree, System.out);

      launchGui(csDef, tree);
   }

   private void launchGui(PSContentSelectorDef def, IPSContentTree tree)
   {
      /*// Lauch the gui
      JFrame      frame       = new JFrame("ContentTree");
      Container   contentPane = frame.getContentPane();
      contentPane.setLayout(new BorderLayout());
      
      PSContentTreePanel p = new PSContentTreePanel();
      p.init(def, tree);
      p.setPreferredSize(new Dimension(300, 150));      
      contentPane.add(p, BorderLayout.CENTER);

      frame.addWindowListener(new WindowAdapter() 
      {
         public void windowClosing(WindowEvent e) 
         {
            System.exit(0);
         }
      });

      frame.pack();
      frame.setVisible(true);*/
   }
  
   public IPSContentTree getContentTree(PSContentSelectorDef csDef)
   {
      try
      {           
         PSPluginFactory pFactory = PSPluginFactory.getInstance();
         // This calls configure on the plugin ...
         IPSContentSelector cs = pFactory.newContentSelector(csDef);                               
         // do a scan
         IPSContentTree tree = cs.scan();
         return tree;
      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
      
      return null;
   }
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(
         new PSUITreeViewTest("test"));
      
      return suite;
   }

}
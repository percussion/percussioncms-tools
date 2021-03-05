/******************************************************************************
 *
 * [ PSContentSelectorMgrTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSContentSelectorDefTest;
import com.percussion.loader.objectstore.PSFileSearchRoot;
import com.percussion.loader.objectstore.PSFileSelectorDef;
import com.percussion.loader.objectstore.PSLoaderComponent;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.objectstore.PSProperty;
import com.percussion.loader.objectstore.PSSearchRoot;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

/**
 * Unit test class for the <code>PSContentSelectorMgr</code> and
 * <code>PSProcessMgr</code> classes
 */
public class PSContentSelectorMgrTest extends TestCase 
   implements IPSProgressListener
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSContentSelectorMgrTest(String name)
   {
      super(name);
   }

   /**
    * Invoked upon firing a progress event.
    * 
    * @param e The progress event
    */
   public void progressChanged(PSProgressEvent e)
   {
      if ( e.getCounter() > 0 )
         System.out.println("progressChanged counter = " + e.getCounter());
      else
         System.out.println("progressChanged percent = " + e.getPercent());
   }

   /**
    * Invokes upon firing a state event. The source of the event may be
    * the selector manager or a plugin object.
    * 
    * @param e The state event.
    */
   public void statusChanged(PSStatusEvent e)
   {
      if ( e.getProcessId() == PSStatusEvent.PROCESS_MANAGER )
         System.out.println("Manager statusChanged status = " + e.getStatus());
      else 
         System.out.println("job process id (" + e.getProcessId() + 
            ") statusChanged status = " + e.getStatus());
   }

   public static PSFileSelectorDef createFileSelector() throws Exception
   {
      PSContentSelectorDef cs = new PSContentSelectorDef("File Selector", 
         PSFileSelectorDef.PLUGIN_CLASS);
      
      PSSearchRoot fsRoot = new PSSearchRoot("File Search Root");
      PSProperty prop = new PSProperty(PSFileSearchRoot.XML_SEARCHROOT_NAME,
         "file:/C:/inetpub/wwwroot/FileSelectorTest/");
      fsRoot.addProperty(prop);
      prop = new PSProperty(PSFileSearchRoot.XML_RECURSE_NAME, 
         PSLoaderComponent.XML_TRUE);
      fsRoot.addProperty(prop);
      cs.addSearchRoot(fsRoot);
      
      return new PSFileSelectorDef(cs);
   }
      
   /**
    * Tests generate content tree from PSContentSelectorMgr; and 
    * serialize the content tree by PSLoaderRepositoryHandler
    *
    * @throws Exception if there are any errors.
    */
   public void doAll() throws Exception
   {
      // preparation before generate content tree      
      PSFileSelectorDef fsDef = PSContentSelectorDefTest.createFileSelector(
         "file:/C:/inetpub/wwwroot/FileSelectorTest/");

      PSLoaderDescriptor desc;
      desc = new PSLoaderDescriptor("c:\\temp\\testContentTree");
      desc.setContentSelectorDef(fsDef);
      PSLoaderDef loaderDef = new PSLoaderDef("Rhythmyx loader", 
         "com.percussion.loader.PSContentLoader");
      desc.setLoaderDef(loaderDef);
      
      PSContentSelectorMgr mgr = new PSContentSelectorMgr(desc, null);
      mgr.addStatusListener(this);
      mgr.addProgressListener(this);

      // generate content tree
      mgr.run();
      
      IPSContentTree tree = mgr.getContentTree();
      
      // serialize the tree
      PSLoaderRepositoryHandler handler = 
         new PSLoaderRepositoryHandler("c:\\temp\\testContentTree");
      PSContentStatus status = new PSContentStatus(tree, desc);
      handler.saveStatus(status);
      
      status = handler.getStatus();
   }

   /**
    * A placeholder for the actual test.
    * 
    * @throws Exception if error occurs
    */
   public void testAll() throws Exception
   {
      // doAll();
   }
   
   /**
    * Take care the log4j with basic configuration
    */
   protected void setUp() 
   {
      BasicConfigurator.configure();  
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSContentSelectorMgrTest("testAll"));
      return suite;
   }

}
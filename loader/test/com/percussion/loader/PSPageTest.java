/*[ PSPageTest.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import websphinx.Link;

/**
 * Tests out PSPage
 */
public class PSPageTest extends TestCase
{
   public PSPageTest(String name)
   {
      super(name);
   }
   
   public static void main(String args[]) 
   {
      junit.textui.TestRunner.run(PSPageTest.class);
   }
   
   protected void setUp() 
   {

   }
   
   public void test() 
   {
      try
      {
         //String strUrl = "file://localhost/c:/e2/loaderTemp/testpage.htm";
         File f = new File("./testpage.htm");
          java.io.FileReader fr = new java.io.FileReader(f);
          char [] buf = new char[(int) f.length()];

          while (fr.read(buf) > 0)
          {}
          
         
         //PSPage page = new PSPage(
           // new websphinx.Link(strUrl));
         String strBase = "file://localhost/c:/e2/loaderTemp/";

         PSPage page = new PSPage(strBase, 
            new String(buf));

         //System.out.println("\n\nBody:");
         //System.out.println(page.getBody());
         System.out.println("\n\nPage:");
         System.out.println(page.getContent());
         
         System.out.println("Page isParsed: " + page.isParsed());
         System.out.println("Links length: " + page.getLinks().length);
         
         String strRtn = page.getContent();

         for (int i=0; i<page.getLinks().length; i++)
         {
            Link l = page.getLinks()[i];

            System.out.println(
               "Link:" + l.toDescription());

            //System.out.println(
              // "Link alt tag " + l.getHTMLAttribute("ALT", ""));
   
            //System.out.println(
              // "Link tag " + l.toText());
                    
            // Test            
            strRtn = page.substituteLink(               
              strRtn, l, "<a href=\"http://www.msn.com\">");
            
         }
         
         System.out.println("After: \n" + strRtn);
      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSPageTest("test"));
      return suite;
   }
}

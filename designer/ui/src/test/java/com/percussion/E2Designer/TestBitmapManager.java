/*[ TestBitmapManager.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import java.util.MissingResourceException;

/**
 * Test harness for the BitmapManager class. Simple tests to verify the caching
 * is working correctly. 
 * <p><b>NOTE:</b><p>
 * Testing is performed in the setup method, which the test framework is not
 * designed to handle. The output of these tests will appear in the console 
 * window instead of the gui.
 */
public class TestBitmapManager extends TestCase
{
   // constructor
   /**
    *
    * @param strTest name of the method that should be executed as a test
    */
   public TestBitmapManager(String strTest)
   {
      super(strTest);
   }

   // Test cases
   /**
    * Test that the bitmap manager's caching is working correctly.   Images that
    * have the same name (case sensitive compare) should have the same reference.
    */
   public void testImageCaching()
   {
      String strMsg = new String("BitmapMgr failed caching test");

      assertTrue(strMsg, m_aIcons[0] == m_aIcons[1]);
      assertTrue(strMsg, m_aIcons[0] == m_aIcons[8]);
      assertTrue(strMsg, m_aIcons[5] == m_aIcons[9]);
   }

   /**
    * Verify that different images have different references.
    */
   public void testDifferentImages()
   {
      String strMsg = new String("BitmapMgr returns same reference for different "
         + "images.");

      assertTrue(strMsg, m_aIcons[1] != m_aIcons[2]);
      assertTrue(strMsg, m_aIcons[2] != m_aIcons[3]);
      assertTrue(strMsg, m_aIcons[3] != m_aIcons[4]);
      assertTrue(strMsg, m_aIcons[4] != m_aIcons[5]);
      assertTrue(strMsg, m_aIcons[5] != m_aIcons[6]);
      assertTrue(strMsg, m_aIcons[6] != m_aIcons[7]);
      assertTrue(strMsg, m_aIcons[7] != m_aIcons[8]);
      assertTrue(strMsg, m_aIcons[8] != m_aIcons[9]);
      assertTrue(strMsg, m_aIcons[9] != m_aIcons[10]);
      assertTrue(strMsg, m_aIcons[10] != m_aIcons[11]);
      assertTrue(strMsg, m_aIcons[11] != m_aIcons[12]);
   }


   /**
    * Verify that same named images in different directories have different
    * references.
    */
   public void testSameImageDiffDir()
   {
      String strMsg = new String("BitmapMgr used same reference for same named "
         + "image in different directories.");

      assertTrue(strMsg, m_aIcons[0] != m_aIcons[10]);
   }


   /**
    * Initialization needed for tests. Load all the images. Assertions are 
    * thrown in this method, but the test framework isn't expecting them so
    * they are not shown in the visual interface, only in the console window.
    * <p>
    * Note: no de-initialization is required
    */
   protected void setUp()
   {
      boolean bGotException = false;
      boolean bUnexpectedFailure = false;
      try
      {
         // get all the images
         BitmapManager Mgr = BitmapManager.getBitmapManager();
         m_aIcons = new ImageIcon[m_astrFilename.length];
         for (int i=0; i < m_aIcons.length; i++)
            m_aIcons[i] = Mgr.getImage(m_astrFilename[i]);
      }
      catch (MissingResourceException e)
      {
         // we expect one of these for ee
         if (!e.getKey().equals("ee"))
            assertTrue("Unexpected MissingResourceException thrown " 
               + "for file " + e.getKey(), false);
         else
            bGotException = true;
      }
      catch (Exception e)
      {
         assertTrue("Unknown exception caught...", false);
      }

      assertTrue("Didn't get MissingResourceException that was expected", 
         bGotException);

   }



   /**
    *  Collect all tests into a TestSuite and return the suite.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      
       suite.addTest(new TestBitmapManager("testImageCaching"));
      suite.addTest(new TestBitmapManager("testDifferentImages"));
      suite.addTest(new TestBitmapManager("testSameImageDiffDir"));
      return suite;
   }

   // test variables
   /**
    * These are the names of the test image files. The order and case IS 
    * important.
    */
   private String [] m_astrFilename = 
   {
      "test.gif",
      "test.gif",
      "Test.gif",
      "test2.gif",
      "test3.gif",
      "test4.gif",
      "test5.gif",
      "test6.gif",
      "test.gif",
      "test4.gif",
      "images\\test.gif",
      "images\\test7.gif",
      "c:\\test8.gif",
      "ee"
   };

   /**
    * This variable stores the references to all the loaded images.
    */
   private ImageIcon [] m_aIcons = null;

}   

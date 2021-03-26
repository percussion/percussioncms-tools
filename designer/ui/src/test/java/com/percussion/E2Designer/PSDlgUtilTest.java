/******************************************************************************
 *
 * [ PSDlgUtilTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.workbench.ui.PSUiTestBase;

public class PSDlgUtilTest extends PSUiTestBase
{
   public void testFailWhenCalledFromUnitTest()
   {
      try
      {
         PSDlgUtil.showConfirmDialog(MESSAGE, "", 0, 0);
         fail();
      }
      catch (AssertionError success)
      {
         assertTrue(success.getMessage().contains(MESSAGE));
      }
      
      try
      {
         PSDlgUtil.showError(new Exception(MESSAGE));
         fail();
      }
      catch (AssertionError success)
      {
         assertTrue(success.getMessage().contains(MESSAGE));
      }

      try
      {
         PSDlgUtil.showError(new Exception(MESSAGE), false, "");
         fail();
      }
      catch (AssertionError success)
      {
         assertTrue(success.getMessage().contains(MESSAGE));
      }

      try
      {
         PSDlgUtil.showErrorDialog(MESSAGE, "");
         fail();
      }
      catch (AssertionError success)
      {
         assertTrue(success.getMessage().contains(MESSAGE));
      }
      
      try
      {
         PSDlgUtil.showInfoDialog(MESSAGE, "");
         fail();
      }
      catch (AssertionError success)
      {
         assertTrue(success.getMessage().contains(MESSAGE));
      }
      
      try
      {
         PSDlgUtil.showMessageDialog(MESSAGE);
         fail();
      }
      catch (AssertionError success)
      {
         assertTrue(success.getMessage().contains(MESSAGE));
      }
      
      try
      {
         PSDlgUtil.showWarningDialog(MESSAGE, "");
         fail();
      }
      catch (AssertionError success)
      {
         assertTrue(success.getMessage().contains(MESSAGE));
      }
   }

   /**
    * Sample message.
    */
   private static final String MESSAGE = "Message !";
}

/*[ Debug.java ]***************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * A class to aid in debugging. 
 */
public class Debug
{
   // 
   /**
    * Allows dynamic enabling of debug support. 
    *
    * @param bEnabled if <code>true</code> then debug support is turned on,
    * otherwise it is turned off.
    */
   public static void setEnabled(boolean bEnabled)
   {
      ms_bDebugEnabled = bEnabled;
   }
   
   /**
    * @returns <code>true</code> if debugging is currently enabled. If debugging
    * is enabled, assert(...) will output messages on assertion failures, otherwise
    * it is a no-op.
    */
   public static boolean isEnabled()
   {
      return ms_bDebugEnabled;
   }

   /**
    * Allows the user to set the behavior of the assert() method. If bThrow is
    * <code>true</code>, then assert will throw an exception if the condition
    * fails, otherwise it will just return. In the future, we can extend this
    * to allow the user to decide what to do.
    */
   public static void setThrowException(boolean bThrow)
   {
      ms_bThrowException = bThrow;
   }

   public static void assertTrue(boolean bCondition, String strMessage)
   {
      if (ms_bDebugEnabled && !bCondition)
      {
         outputMessage(strMessage);
         if (ms_bThrowException)
            throw new AssertionFailedError(strMessage);
      }
   }

   /**
    * Like the other method, but looks up the message in the supplied resource
    * bundle, using the supplied key. If params is not null, then the message
    * is formated using the supplied params.
    *
    * @param bCondition if <code>true</code>, the assertion fails and if debugging
    * is enabled, a msg is output to the user or a file
    *
    * @param rb the bundle containing the resource for the supplied key
    *
    * @param strResourceKey the key to use when looking up the message text from
    * the supplied resource bundle
    *
    * @param params an array of strings to use when formatting the text obtained
    * from the resource bundle
    */
   public static void assertTrue(boolean bCondition,
         @SuppressWarnings("unused") ResourceBundle rb, 
         String strResourceKey, Object[] params )
   {
      if (ms_bDebugEnabled && !bCondition)
      {
         String strFormat = E2Designer.getResources().getString( strResourceKey );
         String strMsg = null;
         if ( null != params )
            strMsg = MessageFormat.format( strFormat, params );
         else
            strMsg = strFormat;
         outputMessage( strMsg );
         if (ms_bThrowException)
            throw new AssertionFailedError(strMsg);
      }
   }

   /**
    * Writes the message to 1 or more places: screen, stdout, file.
    */
   public static void outputMessage(String strMessage)
   {
      System.out.println( "Assertion failed: " + strMessage );   
      // don't show anything thru UI until this dnd bug gets fixed
//      PSDlgUtil.showErrorDialog(strMessage, E2Designer.getResources()
//            .getString( "AssertionFailed" ));
   }


   // private data
   private static boolean ms_bDebugEnabled = false;
   private static boolean ms_bThrowException = false;
}   

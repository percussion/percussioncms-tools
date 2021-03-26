/******************************************************************************
 *
 * [ PSCoreMessages.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PSCoreMessages
{
   
   private PSCoreMessages()
   {
   }

   /**
    * 
    * @param key the message key
    * @return the string message from the bundle resource
    */
   public static String getString(String key)
   {
      // TODO Auto-generated method stub
      try
      {
         return RESOURCE_BUNDLE.getString(key);
      }
      catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }
   
   /**
    * 
    * @param key key the message key
    * @param args array of strings to bind to the message,
    * replaces {0} placeholders.
    * @return the string message from the bundle resource
    */
   public static String getString(String key, Object[] args)
   {
      try
      {
         String msg = RESOURCE_BUNDLE.getString(key);
         return MessageFormat.format(msg, args);
      }
      catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }
   
   private static final String BUNDLE_NAME = 
      "com.percussion.client.pscore_messages"; //$NON-NLS-1$

   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
      .getBundle(BUNDLE_NAME);

}

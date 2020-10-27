/******************************************************************************
 *
 * [ PSMessages.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Accessor class to retrieve messages from the psmessages.properties
 * resource bundle. Located under this package. 
 */
public class PSMessages
{
      private PSMessages()
      {
      }

      /**
       * 
       * @param key the message key
       * @return the string message from the bundle resource if found, or the
       * supplied key surrounded by '!' as !key!.
       */
      public static String getString(String key)
      {
         try
         {
            return RESOURCE_BUNDLE.getString(key);
         }
         catch (MissingResourceException e)
         {
            return key;
         }
      }
      
      /**
       * 
       * @param key key the message key
       * @param args array of strings to bind to the message,
       * replaces {0} placeholders.
       * @return the string message from the bundle resource
       */
      public static String getString(String key, Object... args)
      {
         try
         {
            String msg = RESOURCE_BUNDLE.getString(key);
            return MessageFormat.format(msg, args);
         }
         catch (MissingResourceException e)
         {
            return key;
         }
      }
      
      /**
       * Verifies the existance of the string resource for
       * the specified key.
       * @param key the resource key, may be <code>null</code> or
       * empty in which case <code>false</code> will be returned.
       * @return <code>true</code> if the resource exists,
       * else <code>false</code>.
       */
      public static boolean stringExists(String key)
      {
         if(StringUtils.isBlank(key))
            return false;
         try
         {
            RESOURCE_BUNDLE.getString(key);
            return true;
         }
         catch (MissingResourceException e)
         {
            return false;
         }
      }
      
      private static final String BUNDLE_NAME = 
         "com.percussion.workbench.ui.psmessages"; //$NON-NLS-1$

      private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
         .getBundle(BUNDLE_NAME);

}

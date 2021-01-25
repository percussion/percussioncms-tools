/******************************************************************************
 *
 * [ PSResourceUtils.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author erikserating
 *
 */
public class PSResourceUtils
{

   /**
    * Gets resource file name for this class.
    * @param clazz the class to get the resources name for. 
    * Cannot be <code>null</code>.
    * @return resource file name, never <code>null</code> or empty.
    **/
   @SuppressWarnings("unchecked")
   public static String getResourceName(Class clazz)
   {
      if(clazz == null)
         throw new IllegalArgumentException("clazz cannot be null.");
      return clazz.getName() + "Resources";
   }

   /**
    * Gets the common resource string identified by the specified key.  If the
    * resource cannot be found, the key itself is returned. This is the common
    * resource bundle for the <code>PSUiUtils</code> class.
    * 
    * @param key identifies the resource to be fetched; may not be <code>null
    * </code> or empty.
    *
    * @return String value of the resource identified by <code>key</code>, or
    * <code>key</code> itself.
    */
   public static String getCommonResourceString(String key)
   {
      return getResourceString(PSResourceUtils.class, key);
   }

   /**
    * Gets the resource string identified by the specified key.  If the
    * resource cannot be found, the key itself is returned.
    * @param clazz the class to get the resources for. Cannot be <code>null</code>.
    * @param key identifies the resource to be fetched; may not be <code>null
    * </code> or empty.
    *
    * @return String value of the resource identified by <code>key</code>, or
    * <code>key</code> itself.
    *
    * @throws IllegalArgumentException if key is <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public static String getResourceString(Class clazz, String key)
   {
      if(clazz == null)
         throw new IllegalArgumentException("clazz cannot be null.");
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");
   
      String resourceValue = key;
      try
      {
         if (getResources(clazz) != null)
            resourceValue = 
               PSResourceUtils.ms_resBundles.get(clazz.getName()).getString( key );
      } catch (MissingResourceException ignore)
      {
         
      }
      return resourceValue;
   }

   /**
    * Utility method for any class to retrieve its ResourceBundle.
    *
    * @return ResourceBundle, may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public static ResourceBundle getResources(Class clazz)
   {
      try
      {
         if ( null == PSResourceUtils.ms_resBundles.get(clazz.getName()))
            PSResourceUtils.ms_resBundles.put(clazz.getName(), 
               ResourceBundle.getBundle(
                  PSResourceUtils.getResourceName(clazz), Locale.getDefault()));
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
      return PSResourceUtils.ms_resBundles.get(clazz.getName());
   }

   /**
    * Cache of all resource bundles.
    */
   private static Map<String, ResourceBundle> ms_resBundles =
      new HashMap<String, ResourceBundle>();

}

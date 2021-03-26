/******************************************************************************
 *
 * [ ResourceHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import org.eclipse.jface.resource.ImageDescriptor;

import javax.swing.*;
import java.awt.*;
import java.util.MissingResourceException;

import static org.apache.commons.lang.StringUtils.isBlank;


/**
 * This is a class that aids in retrieving different kinds of resources from a
 * resource bundle. It has static methods to get mnemonics, accelerator keys, 
 * icons, etc.
 * <p>
 * To use this class, resources must use the following key naming convention:
 * <ul>
 * <li> mn_<base_resource_key> for mnemonics </li>
 * <li> ks_<base_resource_key> for accel keys </li>
 * <li> gif_<base_resource_key> for icon file names </li>
 * <li> tt_<base_resource_key> for tooltip text </li>
 * <li> pt_<base_resource_key> for Points (can be used for cursor hotspot) </li>
 * </ul>
 * This allows all resources associated with the same UI object to be accessed
 * with the 'same' key from the caller's point of view.
 */
public class ResourceHelper
{
   /**
    * Returns the character that is the mnemonic for the for the supplied 
    * action, or 0 if the action does not have a mnemonic.
    */
   public static char getMnemonic(PSResources rb, String strBaseKeyName)   
   {
      try
      {
         return rb.getCharacter("mn_" + strBaseKeyName);
      } 
      catch (MissingResourceException e)
      {
         return 0;
      } 
   }
   
   /**
    * Returns the provided string with the mnemonic character marked with "&". 
    * The string is unchanged if no mnemonic character exists.
    * Note, mnemonic character is case-insensitive.
    * @throws IllegalArgumentException if the specified mnemonic character
    * does not occur in the string.
    */
   public static String getWithMnemonic(final String str, PSResources rb, String strBaseKeyName)
   {
      final char c = getMnemonic(rb, strBaseKeyName);
      if (c == 0)
      {
         return str;
      }
      final int idx = str.toLowerCase().indexOf(Character.toLowerCase(c));
      if (idx == -1)
      {
         throw new IllegalArgumentException(
               "Mnemonic character '" + c + "'does not exist in string \"" + str
               + "\". Key: '" + strBaseKeyName + "'.");
      }
      else
      {
         return str.substring(0, idx) + '&' + str.substring(idx);
      }
   }

   /**
    * Checks the supplied resource bundle for an accelerator key by the
    * supplied name. If one is found it is returned, otherwise null is 
    * returned.
    * @deprecated used with legacy Sing UI only.
    * New actions should use {@link #getAccelKey2(PSResources, String)}.
    */
   public static KeyStroke getAccelKey(PSResources rb, String strBaseKeyName)
   {
      try
      {
         return rb.getKeyStroke("ks_" + strBaseKeyName);
      } 
      catch (MissingResourceException e)
      {
         return null;
      }
   }

   /**
    * Ideally standard Eclipse approach should be used instead.
    */
   public static int getAccelKey2(PSResources rb, String strBaseKeyName)
   {
      try
      {
         return (Integer) rb.getObject("ks_" + strBaseKeyName);
      } 
      catch (MissingResourceException e)
      {
         return 0;
      }
   }
   /**
    * Checks the supplied resource bundle for a tool tip string by the
    * supplied name. If a non-empty one is found it is returned, otherwise null is 
    * returned.
    */
   public static String getToolTipText(PSResources rb, String strBaseKeyName)
   {
      try
      {
         String strTip = rb.getString("tt_" + strBaseKeyName);
         return isBlank(strTip) ? null : strTip;
      } 
      catch (MissingResourceException e)
      {
         return null;
      } 
   }

   /**
    * Checks the supplied resource bundle for an icon filename whose key is
    * gif_<strBaseKeyName>. If a non-empty one is found, the icon is loaded and it is 
    * returned, otherwise null is returned.
    *
    * @para rb the resource bundle to search for the filename, using strBaseKeyName
    * as the key
    *
    * @param strBaseKeyName must be a valid string
    *
    * @throws MissingResourceException If the icon filename is present in the
    * resource bundle, but the file cannot be found or loaded.
    */
   public static ImageIcon getIcon(PSResources rb, String strBaseKeyName)
   {
      String strFilename = null;
      try
      {
         strFilename = rb.getString("gif_" + strBaseKeyName);
      } 
      catch (MissingResourceException e)
      {
         return null;
      } 

      return isBlank(strFilename) ? null
            : BitmapManager.getBitmapManager().getImage(strFilename);
   }

   /**
    * Checks the supplied resource bundle for an icon filename whose key is
    * gif_<strBaseKeyName>. If a non-empty one is found, the icon is loaded and it is 
    * returned, otherwise null is returned.
    *
    * @para rb the resource bundle to search for the filename, using strBaseKeyName
    * as the key
    *
    * @param strBaseKeyName must be a valid string
    *
    * @throws MissingResourceException If the icon filename is present in the
    * resource bundle, but the file cannot be found or loaded.
    */
   public static ImageDescriptor getIcon2(PSResources rb, String strBaseKeyName)
   {
      String strFilename = null;
      try
      {
         strFilename = rb.getString("gif_" + strBaseKeyName);
      } 
      catch (MissingResourceException e)
      {
         return null;
      } 

      return isBlank(strFilename) ? null
            : ImageDescriptor.createFromFile(ResourceHelper.class, strFilename); 
   }

   /**
    * @returns the point found in the supplied resource bundle under the supplied
    * key name. If one is not found, a point of 0,0 is returned. If debugging
    * is enabled and the resource isn't found, an assertion is issued.
    */
   public static Point getPoint(PSResources rb, String strBaseKeyName)
   {
      Point pt = null;
      try
      {
         pt = (Point) rb.getObject("pt_" + strBaseKeyName);
      } 
      catch (MissingResourceException e)
      {
//         final String [] astrParams = 
//         {
//            strBaseKeyName
//         };
//         Debug.assert( false, E2Designer.getResources(), "PointNotFound", astrParams );
         pt = new Point();
      }
      catch (ClassCastException e)
      {
         final String [] astrParams = 
         {
            strBaseKeyName
         };
         Debug.assertTrue( false, E2Designer.getResources(), "WrongObjForPt", astrParams );
         pt = new Point();
      }
      return pt;
   }
}




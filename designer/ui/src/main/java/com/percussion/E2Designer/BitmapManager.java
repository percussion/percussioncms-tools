/*[ BitmapManager.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.MissingResourceException;

/**
 * Maintains a list of bitmaps that are shared by multiple objects. Rather
 * than each object of the same class keeping a copy of the image it needs,
 * it uses the bitmap manager to get a reference to a single instance of
 * the image. Any class that uses an imutable image that will possibly be
 * used by more than 1 object should use this class.
 * <p>
 * There is a single instance of the class. Use getBitmapManager() to
 * obtain it.
 */
public class BitmapManager
{
   // constructors
   /**
    * Default constructor. Use getBitmapManager() to get the singleton instance
    * of the manager.
    */
   private BitmapManager( )
   {
   }

   /**
    * Returns a singleton instance of the BitmapManager class. Creates it
    * if it hasn't been created.
    */
   public static BitmapManager getBitmapManager( )
   {
      if (null == theManager)
         theManager = new BitmapManager( );
      return theManager;
   }

   // operations

   /**
    * Returns an instance of the image specified by strFilename. It checks
    * in the local cache for a image with the same name (case sensitive). If
    * one is found, it is returned, otherwise the image is loaded and returned.
    *
    * @param  strFilename - name of the bitmap resource. 
    *
    * @throws MissingResourceException if the image file cannot be found. The
    * filename is returned as part of the detail message and as the key.
    */
   public ImageIcon getImage(String strFilename)
   {
      boolean bInCache = true;

      // check if image in cache
      ImageIcon Img = (ImageIcon) Map.get(strFilename);
      E2DesignerResources rb = E2Designer.getResources();
      if (null == Img)
      {
         URL IconURL = getClass().getResource(strFilename);
         if (null == IconURL)
         {
            throw new MissingResourceException(rb.getString("LoadIconFail"),
                  "IconURL", strFilename);
         }
         Img = new ImageIcon(IconURL);
         bInCache = false;
      }
      
      //first lets wait
      while (MediaTracker.LOADING == Img.getImageLoadStatus())

      //now check the status
      if (MediaTracker.COMPLETE != Img.getImageLoadStatus())
      {
         String astrParam [] =
         {
            strFilename
         };
         throw new MissingResourceException(MessageFormat.format( 
               rb.getString("FileDesc"), astrParam ), "ImageIcon", strFilename);
      }
      // add it to the cache if not already present
      if (!bInCache)
         Map.put(strFilename, Img);
   
      return Img;
   }
   
   // variables
   /**
    * The only instance of the manager. Created the first time the mgr is
    * requested.
    */
   private static BitmapManager theManager = null;
   /**
    * Estimate of the # of different images used by designer. We can do a little
    * research on this number when the program nears completion.
    */
   private HashMap Map = new HashMap(60);
}




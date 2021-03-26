/*[ BrightenImageFilter.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.RGBImageFilter;

/**
 * A derivation of RGBImageFilter that bleaches an image.<p>
 *
 * Extent of the bleaching effect is controlled by the only 
 * constructor argument: an integer representing the percentage 
 * of bleaching.  The percentage of bleaching may also be 
 * controlled after instantiation by invoking the 
 * void percent(int) method.<p>
 *
 * @version 1.0, Apr 1 1996
 * @author  David Geary
 * @see     RGBImageFilter
 */
public class BrightenImageFilter extends RGBImageFilter {
    private int redpercent;
    private int greenpercent;
    private int bluepercent;
    private int redlimit;
    private int greenlimit;
    private int bluelimit;
    private int redlowest;
    private int greenlowest;
    private int bluelowest;

    public BrightenImageFilter(int redpercent, int greenpercent, int bluepercent,
                        int redlimit, int greenlimit, int bluelimit,
                        int redlowest, int greenlowest, int bluelowest) 
   {
        this.redpercent = redpercent;
        this.greenpercent = greenpercent;
        this.bluepercent = bluepercent;
        this.redlimit = redlimit;
        this.greenlimit = greenlimit;
        this.bluelimit = bluelimit;
        this.redlowest = redlowest;
        this.greenlowest = greenlowest;
        this.bluelowest = bluelowest;
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
        DirectColorModel cm = 
            (DirectColorModel)ColorModel.getRGBdefault();

        int    alpha = cm.getAlpha(rgb);
        int    red   = cm.getRed  (rgb);
        int    green = cm.getGreen(rgb);
        int    blue  = cm.getBlue (rgb);

      
      if(red > redlimit && green > greenlimit && blue > bluelimit)
      {
           red = redlowest;
           green = greenlowest;
           blue = bluelowest;
      }
      else
      {
         if(redpercent > 0)
         {
              double redpercentMultiplier = (double)redpercent/100;
              red   = Math.min((int)
                      (red - (red * redpercentMultiplier)), 255);
         }   

         if(greenpercent > 0)
         {
              double greenpercentMultiplier = (double)greenpercent/100;
         
              green = Math.min((int)
                      (green - (green * greenpercentMultiplier)), 255);
         }   
         
         if(bluepercent > 0)
         {
              double bluepercentMultiplier = (double)bluepercent/100;

              blue  = Math.min((int)
                      (blue - (blue  * bluepercentMultiplier)), 255);
         }
      }
      
        alpha = alpha << 24;
        red   = red   << 16;
        green = green << 8;

        return alpha | red | green | blue;
    }
}

/*[ BlackAndWhiteFilter.java ]*************************************************
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
public class BlackAndWhiteFilter extends RGBImageFilter {
    public BlackAndWhiteFilter() {
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
        DirectColorModel cm = 
            (DirectColorModel)ColorModel.getRGBdefault();

        int    alpha = cm.getAlpha(rgb);
        int    red   = cm.getRed  (rgb);
        int    green = cm.getGreen(rgb);
        int    blue  = cm.getBlue (rgb);
      int    mixed = (red + green + blue) / 3;

      red   = blue = green = mixed;
        alpha = alpha << 24;
        red   = red   << 16;
        green = green << 8;

        return alpha | red | green | blue;
    }
}

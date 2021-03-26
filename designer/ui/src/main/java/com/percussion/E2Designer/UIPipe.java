/*[ UIPipe.java ]**************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


/**
 * Same as its super class, but it paints a tiled image. The image is a pipe
 * with an image at either end (endcaps). The derived classes provide the
 * bitmaps and the extent of tiling for the main pipe.
 */
abstract public class UIPipe extends UIConnectableFigure
{
   public UIPipe( )
   {
      super( null, null, null, 1, "UIPipe" );
   }

   /**
    * Returns the desired width of the main pipe, in pixels (not including
    * the endcaps).
    */
   abstract protected int getPipeImageExtent( );
}


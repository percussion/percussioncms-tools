/*[ IGuiLink.java ]************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

/**
 * This interface was created to allow data objects to keep references to their
 * figure owners.
 */
public interface IGuiLink
{
   /**
    * Sets the figure that owns this object. It should be called when the figure
    * takes ownership of the object. When finished, the figure should call
    * release().
    *
    * @see #release
    */
   public void setFigure( UIFigure figure );

   /**
    * Tells the object implementing this interface that the current owner is
    * no longer valid.
    */
   public void release();

   /**
    * @returns the figure that owns this data object, or null if no owner
    */
   public UIFigure getFigure();
}

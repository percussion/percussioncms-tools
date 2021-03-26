/*[ IRefreshableGuiLink.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

/**
 * This interface is provided to allow data objects to refresh the display of
 * the figure which owns this data object.
 */
public interface IRefreshableGuiLink extends IGuiLink
{
   /**
    * Refreshes the figure display from the data object which the figure is set
    * with. This should be called when data represented by figure is changed.
    */
   public void refreshFigure();
}

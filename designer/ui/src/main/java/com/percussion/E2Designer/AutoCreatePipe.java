/*[ AutoCreatePipe.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.awt.*;

/**
 * A highly specialized class that creates a pipe if a page datatank is dropped
 * on a backend datatank, or vice versa and target datatank does not have any
 * dynamic connections.
 * <p>
 * The object is registered with a UIPipeFrame window.
 *
 * @see UIFigureFrame
 */
public class AutoCreatePipe implements ICustomDropAction
{
   // constructors

   // operations
   public boolean wantsDrop(UICIdentifier ID)
   {
      return(false);
   }
   
   public int customizeDrop( UIFigureFrame frame,
         UIConnectableFigure target,
         UIConnectableFigure source, Point dropLocation )
   {
      return ICustomDropAction.DROP_IGNORED;
   }
}


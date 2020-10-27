/*[ AutoCreateConnector.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.awt.*;

/**
 * A highly specialized class that creates a directed connection when an input/
 * output connector is dragged and dropped onto an output/input connector 
 * (respectively).
 * <p>
 * The object is registered with a UIAppFrame window.
 *
 * @see UIFigureFrame
 */
public class AutoCreateConnector implements ICustomDropAction
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

/*[ ICustomDropAction.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.awt.*;

/**
 * Defines an interface that is used by the figure editing window to allow
 * customization during a drop of one UIConnectableFigure (UIC)onto another, or
 * the UIC onto the frame directly.
 * If the target does not accept a connection from the source, then all
 * objects implementing this interface that are registered with the frame will
 * get called in the order of registration until the list is exhausted or
 * one of the objects returns true.
 *
 * @see UIFigureFrame
 */

public interface ICustomDropAction
{
   /**
    * This method is called to determine if the currently dragging object
    * would be accepted by this drop action. This information is then used
    * to provide proper feedback to the user.
    *
    * @param ID the unique identifier of the object being dropped
    *
    * @returns <code>true</code> if this action will use the object, 
    * <code>false</code> otherwise.
    */
   public boolean wantsDrop(UICIdentifier ID);

   /**
    * This method is called if a UIConnectableFigure is dropped but does not
    * connect. It allows custom actions to occur, such as creating a pipe
    * when a page datatank is dropped onto a backend datatank. To implement
    * this function requires detailed knowledge that the frame does not
    * have.
    *
    * @param frame The frame window which accepted the drop.
    *
    * @param target The object that is under the drop that won't accept a
    * connection from the source object. May be null if the drop is occurring
    * directly on the frame.
    *
    * @param source The object that is being dropped.
    *
    * @param dropLocation where the drop occurred, in coord system of the frame
    *
    * @returns   DROP_IGNORED  if the implementor is not interested in the drop
   *    DROP_ACCEPTED if any actions are taken
   *
   *   DROP_ACCEPTED_AND_ATTACH_OBJ if any actions are taken,and object will handle
   *   the attachment
    *
    */
   public int customizeDrop( UIFigureFrame frame,
         UIConnectableFigure target,
         UIConnectableFigure source, Point dropLocation );

  /*
  *types to indicate the action returned by customizeDrop
  */
  /**
  *drop was canceled
  */
  public static final int DROP_CANCELED = -1;

  /**
  *drop was not accepted
  */
  public static final int DROP_IGNORED                   =0;
  /**
  *drop was accepted
  */
   public static final int DROP_ACCEPTED                  =1;
   /**
   *drop was accepted and handler attached the object
   */
   public static final int DROP_ACCEPTED_AND_ATTACH_OBJ   =2;

}



/*[ UTBrowseButton.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;

/**
 * Creates a small, square button that has 3 dots that is typically used to
 * bring up some sort of browser dialog. The button attempts to maintain a
 * fixed size.
 */
public class UTBrowseButton extends JButton
{
   /**
    * The standard ctor for the browse button object.
    */
   public UTBrowseButton()
   {
      ImageIcon icon = BitmapManager.getBitmapManager().getImage(
         "images/optional.gif" );
      setIcon( icon );
      Dimension d = new Dimension( IUTConstants.FIXED_HEIGHT,
         IUTConstants.FIXED_HEIGHT );
      setSize( d );
      setMaximumSize( d );
      setMinimumSize( d );
      setPreferredSize( d );
      setAlignmentY( CENTER_ALIGNMENT);
   }
}

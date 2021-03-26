/*[ UTFixedHeightComboBox.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;


/**
 * Just like a standard combo box with 3 small enhancements. When used with
 * a Box layout, it will take up all available width, but is limited in height.
 * It changes the default border so it looks better w/ the Windows Look and
 * Feel, and it defaults the the box to editable.
 */
public class UTFixedHeightComboBox extends JComboBox
{
   /**
    * The only ctor. Modifies the default border to a lowered, beveled border
    * and makes the text field editable.
    */
   public UTFixedHeightComboBox()
   {
      super();
      setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED,
         Color.gray, Color.darkGray));
      setEditable( true );
   }

   /**
    * Overridden to return the max size for the control. When used w/ a Box,
    * this provides the behavior of taking up all the width, but maintaining
    * a nice height.
    */
   public Dimension getPreferredSize()
   {
      return getMaximumSize();
   }

   /**
    * Overridden to return the min size of the control, as defined by
    * IUTConstants.MIN_CONTROL_SIZE. When used with the Box layout mgr, this
    * provides the behavior of the control not shrinking beyond a default
    * width, while maintaining a nice height as the container is resized.
    */
   public Dimension getMinimumSize()
   {
      return IUTConstants.MIN_CONTROL_SIZE;
   }

   /**
    * Overridden to return the max size of the control, as defined by
    * IUTConstants.MAX_CONTROL_SIZE. When used with the Box layout mgr, this
    * provides the behavior of taking up all the width, but maintaining a nice
    * height as the container is resized.
    */
   public Dimension getMaximumSize()
   {
      return IUTConstants.MAX_CONTROL_SIZE;
   }
}

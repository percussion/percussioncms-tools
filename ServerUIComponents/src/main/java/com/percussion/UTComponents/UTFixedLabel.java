/*[ UTFixedLabel.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.UTComponents;

import javax.swing.*;
import java.awt.*;


/**
 * Just like a standard label, with a slight change in the resizing
 * behavior. The height is maintained at a constant value as defined by
 * IUTConstants.FIXED_HEIGHT.
 */
public class UTFixedLabel extends JLabel
{
   public UTFixedLabel( String label, int position )
   { super( label, position );}

   /**
    * Overridden to return constant height for the control. When used w/ a Box,
    * this provides the behavior of taking enough room for the text in the
    * control, but maintaining a nice height.
    */
   public Dimension getPreferredSize()
   {
      return new Dimension( super.getPreferredSize().width,
         getMaximumSize().height );
   }

   /**
    * Overridden to return the min size of the control, which is the underlying
    * minimum size for width, and a constant height, as defined by
    * IUTConstants.FIXED_HEIGHT.
    */
   public Dimension getMinimumSize()
   {
      return new Dimension( super.getMinimumSize().width,
         IUTConstants.MIN_CONTROL_SIZE.height );
   }

   /**
    * Overridden to return the max size of the control, which is the underlying
    * maximum size for width, and a constant height, as defined by
    * IUTConstants.FIXED_HEIGHT.
    */
   public Dimension getMaximumSize()
   {
      return new Dimension( super.getMaximumSize().width,
         IUTConstants.MAX_CONTROL_SIZE.height );
   }
}

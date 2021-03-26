/*[ IUTConstants.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.awt.*;

/**
 * A simple interface that contains constant values useful to the UT...
 * classes (and possibly other UI windows/components). All of our classes
 * should be modified to use these constants so they look and behave
 * consistently.
 */
public interface IUTConstants
{
   /**
    * The standard height, in pixels of single-line, text editing controls
    * such as text fields and combo boxes. We use a fixed height on these
    * controls so that as the container is resized, these guys remain the
    * same height (although functional when taller, they are ugly).
    */
   public static final int FIXED_HEIGHT = 20;

   /**
    * A dimension useful for setting up fixed height controls. Use this
    * values to set the maximum size for the control. When used
    * with a Box layout, the width will take up the available width and not
    * be limited like the UTFixed[ComboBox, TextField] controls are.
    */
   public static final Dimension MAX_CONTROL_SIZE = new Dimension( 10000,
      FIXED_HEIGHT );

   /**
    * A dimension useful for setting up fixed height controls. Use this
    * values to set the minimum size for the control. When used
    * with a Box layout, the width will take up the available width but not
    * get narrower than specified here.
    */
   public static final Dimension MIN_CONTROL_SIZE = new Dimension( 40,
      FIXED_HEIGHT );

} 

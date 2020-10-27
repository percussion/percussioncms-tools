/*[ UTFixedButton.java ]*******************************************************
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
 * A fixed sized JButton.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTFixedButton extends JButton
{
   /**
    * The size of button used by default by this class.
    */
   public static final Dimension STANDARD_BUTTON_SIZE = new Dimension(80, 24);

   /**
   * Construct a new fixed button with the standard size.
   *
   * @param string      the button name
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(String name)
   {
     super(name);
    setPreferredSize(STANDARD_BUTTON_SIZE);
   }

   /**
   * Construct a new fixed button of passed size.
   *
   * @param string      the button name
   * @param size         the button size
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(String name, Dimension size)
   {
     super(name);
    setPreferredSize(size);
   }

   /**
   * Construct a new fixed button of passed width/height
   *
   * @param string      the button name
   * @param width         the button width
   * @param height      the button height
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(String name, int width, int height)
   {
     super(name);
    setPreferredSize(new Dimension(width, height));
   }
   /**
   * Construct a new fixed button with the standard size.
   *
   * @param icon      the button icon
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(ImageIcon icon)
   {
     super(icon);
    setPreferredSize(STANDARD_BUTTON_SIZE);
   }

   /**
   * Construct a new fixed button of passed size.
   *
   * @param icon      the button icon
   * @param size         the button size
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(ImageIcon icon, Dimension size)
   {
     super(icon);
    setPreferredSize(size);
   }

   /**
   * Construct a new fixed button of passed width/height
   *
   * @param icon         the button icon
   * @param width         the button width
   * @param height      the button height
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(ImageIcon icon, int width, int height)
   {
     super(icon);
    setPreferredSize(new Dimension(width, height));
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Dimension getMinimumSize()
   {
      return getPreferredSize();
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Dimension getMaximumSize()
   {
      return getPreferredSize();
   }
}

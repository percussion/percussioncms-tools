/*[ UTFixedPasswordField.java ]************************************************
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
 * A fixed sized JPasswordField.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTFixedPasswordField extends JPasswordField
{
   public UTFixedPasswordField()
   {
      this( "" );
   }

   /**
   * Construct a new fixed password field with the standard size.
   *
   * @param string      the text field name
   */
   public UTFixedPasswordField(String name)
   {
      super(name);
    setPreferredSize(STANDARD_PASSWORDFIELD_SIZE);
   }

   /**
   * Construct a new fixed password field of passed size.
   *
   * @param string      the text field name
   * @param size         the text field size
   */
   public UTFixedPasswordField(String name, Dimension size)
   {
      super(name);
    setPreferredSize(size);
   }

   /**
   * Construct a new fixed password field of passed width/height.
   *
   * @param name      the text field name
   * @param width         the text field width
   * @param height      the text field height
   */
   public UTFixedPasswordField(String name, int width, int height)
   {
      super(name);
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

  //////////////////////////////////////////////////////////////////////////////
   /**
   * the standard text field size
    */
  private static final Dimension STANDARD_PASSWORDFIELD_SIZE = new Dimension(200, 20);
}
 

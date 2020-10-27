/*[ StatusBar.java ]***********************************************************
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
 * Initially, this will be a simple class that allows any object in the mainframe
 * to display a message. Once the text is set, it will remain until it is cleared
 * or a different string is set. It provides a very simple mechanism to give
 * short messages to the end user.
 * <p>
 * The status bar is composed of a panel with a label.
 * <p>
 * In the future, additional things could be added to the panel.
 */
class StatusBar extends JPanel
{
   public StatusBar(String strMsg)
   {
      try
      {
         setBorder(BorderFactory.createCompoundBorder(
            // border size, in pixels: top, left, bot, right, chosen empirically
            BorderFactory.createEmptyBorder(2, 3, 0, 3),
            BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
         ((FlowLayout) getLayout()).setAlignment(FlowLayout.LEFT);
         add(m_label);
      } 
      catch (ClassCastException e)
      {
         Debug.assertTrue( false, E2Designer.getResources(), "LayoutMgrChanged", null );
      }
   }
   /**
    * Sets the text of the status bar. If strText is empty or null, the current
    * message is cleared and the default message is displayed (Ready). 
    * The screen is repainted after the message has been updated.
    */
   public void setMessage(String strText)
   {
      if ( null == strText || 0 == strText.trim().length())
         clearMessage();
      else
         m_label.setText( strText );
   }

   /**
    * Clears the current message (if there is one), sets the default message
    * and repaints the screen.
    */
   public void clearMessage()
   {
      m_label.setText( m_strDefaultMsg );
   }

   /**
    * Sets the text that will appear whenever the message is cleared (either
    * explicitly or implicitly). By default, the text is 'Ready'.
    */
   public void setDefaultMessage(String strText)
   {
      m_strDefaultMsg = strText;
   }
   
   // private storage
   private String m_strDefaultMsg = E2Designer.getResources().getString("StatusBarDefault");
   private JLabel m_label = new JLabel( m_strDefaultMsg );
}   

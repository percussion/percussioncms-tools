/*[ StatusBar.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import javax.swing.*;
import java.awt.*;

/**
 * This class implements a status bar used to inform the user of whats going on
 * while during longer operations like loging into th eserver.
 */
////////////////////////////////////////////////////////////////////////////////
public class StatusBar extends JPanel
{
   /**
    * Construct the status bar with its initial display text.
    *
   * @param statusText      text to display
    */
  //////////////////////////////////////////////////////////////////////////////
   public StatusBar(String statusText)
   {
      try
      {
         initPanel(statusText);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   /**
    * Set new status text.
    *
   * @param statusText      text to display
    */
  //////////////////////////////////////////////////////////////////////////////
  public void setStatusText(String statusText)
  {
     m_statusText.getAccessibleContext().setAccessibleName("Status is "+statusText);
     m_statusText.setText(statusText);
  }

   /**
    * Create and initialize GUI elements used for the status bar.
    *
   * @param statusText      initial text to display
    */
   //////////////////////////////////////////////////////////////////////////////
   private   void initPanel(String statusText) throws Exception
   {
     BorderLayout layout = new BorderLayout();
      
      
      this.setLayout(layout);

      m_statusText = new JTextField(statusText);
      m_statusText.setBackground(Color.lightGray);
      m_statusText.setFocusable(true);
      
      m_statusText.getAccessibleContext().setAccessibleName("Status is "+statusText);
      this.add(m_statusText,BorderLayout.CENTER);
   }

  //////////////////////////////////////////////////////////////////////////////
   /**
    * the status text currently displayed
    */
  private JTextField m_statusText = null;
}

 

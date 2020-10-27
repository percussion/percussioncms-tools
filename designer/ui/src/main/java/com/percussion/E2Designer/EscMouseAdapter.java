/*[ EscMouseAdapter.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
 
/**
*  This class is used as a mouse adapter that knows if the esc key was pressed between
*  a mouse pressed and mouse released
*/
package com.percussion.E2Designer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EscMouseAdapter extends MouseAdapter
{
   public void mousePressed(MouseEvent e)
   {
      m_bEscPressed = false;
      final MouseEvent me = e;
      e.getComponent().requestFocus();
      e.getComponent().addKeyListener(new KeyAdapter()
      {
         public void keyReleased(KeyEvent e)
         {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE && !m_bEscPressed)
            {
               m_bEscPressed = true;            
               mouseReleased(me);
               m_bEscPressed = false;
            }
         }
      });
   }   
   
   public static boolean wasEscPressed()
   {
      return(m_bEscPressed);
      
   }

   private static boolean m_bEscPressed = false;
}   

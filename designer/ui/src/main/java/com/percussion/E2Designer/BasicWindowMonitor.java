/*[ BasicWindowMonitor.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This is the helper class that let the window that contains the bean
 * shutting down upon user press the window close button
 */
public class BasicWindowMonitor extends WindowAdapter 
{
   public void windowClosing( WindowEvent e )
   {
      Window w = e.getWindow( );
      w.setVisible( false );
      w.dispose( );
      System.exit( 0 );
   }
}




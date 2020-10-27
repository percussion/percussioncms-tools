/*[ PSServerAdminFrame.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import javax.swing.*;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * PSServerAdminFrame initializes /stops the main frame while running this as
 * a standalone application.
 */
////////////////////////////////////////////////////////////////////////////////
public class PSServerAdminFrame extends JFrame implements AppletStub,
                                                                                       AppletContext
{
   /**
    * The Constuctor sets the aplications main fram size and title. It adds a new
   * window listener to watch for closing events. Then it performs the same
   * initialization as a browser.
    *
    * @param   applet      the applet
   * @param x               x-size
   * @param   y               y-size
    */
   //////////////////////////////////////////////////////////////////////////////
   public PSServerAdminFrame(JApplet applet, int x, int y)
   {
     super(getResources().getString("titlelogin"));
      ImageIcon icon = new ImageIcon(getClass().getResource(getResources().getString("gif_main")));
      if (null != icon)
      this.setIconImage( icon.getImage( ) ); // adding Rhythmyx icon to title bar

    this.addWindowListener(new WindowAdapter()
       {
          public void windowClosing(WindowEvent e)
           {
              System.exit(0);
           }
       }
    );

    this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
       this.getContentPane().add(applet);
    applet.setStub(this);
    applet.init();
    applet.start();
    this.setSize(1000, 1000);
     this.pack();
     this.center();
    this.setResizable(false);
   }

      /**
    * Centers the dialog on the screen, based on its current size.
    */
   public void center()
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation(( screenSize.width - size.width ) / 2, 
            ( screenSize.height - size.height ) / 2 );
   }


   /**
   * The Server Admin resources
   */
  //////////////////////////////////////////////////////////////////////////////
  private static ResourceBundle sm_res = null;
  public static ResourceBundle getResources()
  {
      try
    {
      if (sm_res == null)
          sm_res = ResourceBundle.getBundle("com.percussion.E2Designer.admin.PSServerAdminResources",
                                                           Locale.getDefault());
    }
    catch (MissingResourceException e)
      {
         System.out.println(e);
      }

       return sm_res;
    }


   /**
    * Minimal implementation for AppletStub.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public boolean isActive() { return false; }
  public URL getDocumentBase() { return null; }
  public URL getCodeBase() { return null; }
  public String getParameter(String name) { return ""; }
  public AppletContext getAppletContext() { return this; }
  public void appletResize(int width, int height) {}

   /**
    * Minimal implementation for AppletContext.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public AudioClip getAudioClip(URL url) { return null; }
  public Image getImage(URL url) { return null; }
  public Applet getApplet(String name) { return null; }
  public Enumeration getApplets() { return null; }
  public void showDocument(URL url) {}
  public void showDocument(URL url, String taget) {}
  public void showStatus(String status) {}
  
   /* (non-Javadoc)
    * @see java.applet.AppletContext#getStream(java.lang.String)
    */
   public InputStream getStream(String key)
   {
      // TODO - Implement for JDK 1.4
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.applet.AppletContext#getStreamKeys()
    */
   public Iterator getStreamKeys()
   {
      // TODO - Implement for JDK 1.4
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.applet.AppletContext#setStream(java.lang.String, java.io.InputStream)
    */
   public void setStream(String key, InputStream stream) throws IOException
   {
      // TODO - Implement for JDK 1.4
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

}


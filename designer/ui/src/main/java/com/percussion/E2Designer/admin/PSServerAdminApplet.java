/******************************************************************************
 *
 * [ PSServerAdminApplet.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.tools.help.PSJavaHelp;
import org.apache.log4j.BasicConfigurator;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * PSServerAdminApplet provides the init, start and stop functions which will
 * be used from any applet container such as browsers. It tries to start with
 * the system look and feel. If this failes it comes up with th edefault look
 * and feel.
 */
////////////////////////////////////////////////////////////////////////////////
public class PSServerAdminApplet extends JApplet
{
   /**
    * Initialize the applet. This function will be called automatically by its
    * container.
    */
   //////////////////////////////////////////////////////////////////////////////
   @Override
   public void init()
   {
      try
      {
         // we need the applets frame as an anchor for certain view operations
         m_frame = getFrame(this);
         this.setSize(ProjectConstants.APPLET_SIZE);

         //Get the JavaHelp helpset file and attach the protocol based on its
         //location.
         if(m_applet)
         {
            String helpFile = getParameter(HELPSETFILE);
            m_helpSetURL = PSJavaHelp.getHelpSetURL(helpFile, true, 
               getCodeBase().toString());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

  /**
    * The main function, which allows the applet to be started as an application
    * as well.
    *
    * @param   args      no arguments implemented
    */
   //////////////////////////////////////////////////////////////////////////////
   public static void main(String[] args)
   {
      BasicConfigurator.configure();
      // indicate we are running as an application
      m_applet = false;

      sm_frame = new PSServerAdminFrame(new PSServerAdminApplet(),
                                        ProjectConstants.APPLICATION_WIDTH,
                                        ProjectConstants.APPLICATION_HEIGHT);
      sm_frame.setVisible(true);
   }

   /**
    * Start the applet. This is called each time the container page comes into
    * scope. It creates all GUI elements, which is the login panel in our
    * case.
    * NOTE: this can't be don in the init because its not finding the resource
    *          bundle while not completly initialized!
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   @Override
   public void start()
   {
      // we must set the look and feel each time we start the applet
      String strLookAndFeel = new String(UIManager.getSystemLookAndFeelClassName());
      try
        {
           UIManager.setLookAndFeel(strLookAndFeel);
      }
      catch (UnsupportedLookAndFeelException e)
      {
         // if this fails we still want to proceed
         System.err.println("Warning: UnsupportedLookAndFeel: " + strLookAndFeel);
      }
      catch (Exception e)
      {
         // if this fails we still want to proceed
         System.err.println("Error loading " + strLookAndFeel + ": " + e);
      }

      try
      {
         String server = null;
         String port = null;
         String protocol = "http";
         if (m_applet)
         {
            URL url = this.getDocumentBase();
            server = url.getHost();
            protocol = url.getProtocol();

            if (url.getPort() >= 0)
            {
               Integer iPort = new Integer(url.getPort());
               port = iPort.toString();
            }
         }

         // create the login panel, which is always the first panel shown
         m_loginPanel = new LoginPanel(m_frame, m_applet, server, protocol, port);

         this.getContentPane().add(m_loginPanel);
         this.getRootPane().setDefaultButton(m_loginPanel.getLoginButton());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Stops the applet. This is called each time the containing page goes out of
    * scope.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   @Override
   public void stop()
   {
      // nothing to do
   }

   /**
    * The browser asks to destroy this applet. So do the cleanup here.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   @Override
   public void destroy()
   {
      // let the login panel handle this
      m_loginPanel.onDestroy();
   }

   /**
    * Returns information about this applet (similar to the about dialog).
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   @Override
   public String getAppletInfo()
   {
      return "";
   }

   /**
    * Find the applets parent frame.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
  public static Frame getFrame(JApplet applet)
  {
    Component parent = applet;
    Frame frame = null;
    while (parent != null)
    {
      if (parent instanceof Frame)
      {
        frame = (Frame) parent;
        break;
      }
      parent = parent.getParent();
    }

    return frame;
  }

  /**
   * @returns Frame The static instance of the main applet frame.
   */
  public static Frame getFrame()
  {
    return sm_frame;
  }

   /**
    * The Server Admin resources
    */
   private static ResourceBundle m_res = null;
   public static ResourceBundle getResources()
   {
      if (m_res == null)
          m_res = ResourceBundle.getBundle(
            getResourceName(),
            Locale.getDefault());
       return m_res;
    }

   /**
    * Gets resource file name.
    *
    * @return resource file name, never <code>null</code> or empty.
    */
   public static String getResourceName()
   {
      return "com.percussion.E2Designer.admin.PSServerAdminResources";
   }

   /**
    * Are we running as an applet or as an application?
    *
    * @returns <code>true</code> if we were started as an applet;
    *    <code>false</code> if we were started as an application
    */
   public static boolean isApplet()
   {
      return m_applet;
   }

   /**
    * the applets parent frame
    */
   private Frame m_frame = null;

  /**
   * static instance of the admin frame.
   */
  private static Frame sm_frame = null;

   /**
    * the login panel, containing all GUI elements for the login dialog
    */
  private LoginPanel m_loginPanel = null;

  /*
   * status whether we were started as an applet or application.
   */
   private static boolean m_applet = true;

   /**
    * The help set file url for this applet, gets initialized when the applet
    * is initialized.
    */
   public static String m_helpSetURL = null;

   /**
    * The property which defines the helpset file to be used to display help for
    * this application using JavaHelp viewer.
    */
   public static final String HELPSETFILE = "helpset_file";
}


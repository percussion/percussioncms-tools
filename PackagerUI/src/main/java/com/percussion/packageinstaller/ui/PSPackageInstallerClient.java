/******************************************************************************
 *
 * [ PSPackageInstallerClient.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packageinstaller.ui;

import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.packager.ui.data.PSServerRegistration;
import org.jvnet.substance.skin.SubstanceModerateLookAndFeel;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSPackageInstallerClient
{
   /**
    * @param args
    */
   public static void main(String[] args)
   {
      
      if (Array.getLength(args) == 0)
      {

         try
         {

            if (ms_frame != null)
               return; // Can only have one local instance of the client.



            // Make sure we run in the Swing Ui Thread
            SwingUtilities.invokeLater(new Runnable()
            {

               public void run()
               {
                  try {
                     UIManager.setLookAndFeel(new SubstanceModerateLookAndFeel());
                  } catch (UnsupportedLookAndFeelException e) {
                     e.printStackTrace();
                  }
                  ms_frame = new PSPackageInstallerFrame();
                  ms_errDlg = new ErrorDialogs(ms_frame);

                  ms_frame.setVisible(true);
               }

            });

         }
         catch (Throwable e)
         {
            e.printStackTrace();
            // we're on our way out, so notify user
            System.out.println("Unexpected exception in main");
            ErrorDialogs.FatalError(e.getLocalizedMessage());
         }

      }
      else
      {
         m_console = true;
         parseArgs(args);

         // Verify required args
         List<String> missingArgs = new ArrayList<String>();
         missingArgs = verifyReqArgs();

         for (String missingArg:missingArgs)
         {
            System.out.print("Missing required Arguments: " + missingArg + "\n");
            System.exit(3);
         }
         PSPackageInstallerConsole installer =  new PSPackageInstallerConsole();

            installer.doConsoleInstall(m_package);   
      }
   }
   
   
   /**
    * Parse command line and sets variables
    * 
    * @param args - command line arguments
    */
   private static void parseArgs(String[] args)
   {
      String opt = "";
      
      int i = 0;  
      while (i < args.length) { 
         if (m_package_flag.equalsIgnoreCase(args[i]))
         {
            i++;
            if (i < args.length)
            {
               m_package = args[i];
            }
         }
         else if (m_host_flag.equalsIgnoreCase(args[i]))
         {
            i++;
            if (i < args.length)
            {
               m_host = args[i];
            }
         }
         else if(m_port_flag.equalsIgnoreCase(args[i]))
         {
            i++;
            if (i < args.length)
            {
               m_port = args[i];
            }
         }
         else if(m_user_flag.equalsIgnoreCase(args[i]))
         {
            i++;
            if (i < args.length)
            {
               m_user = args[i];
            }
         }
         else if(m_password_flag.equalsIgnoreCase(args[i]))
         {
            i++;
            if (i < args.length)
            {
               m_password = args[i];
            }
         }
         else if(m_usessl_flag.equalsIgnoreCase(args[i]))
         {
            i++;
            if (i < args.length)
            {
               m_usessl = Boolean.valueOf(args[i]);
            }
         }
         else if(m_acceptwarnings_flag.equalsIgnoreCase(args[i]))
         {
            i++;
            if (i < args.length)
            {
               m_acceptwarnings = Boolean.valueOf(args[i]);
            }
         }
         else
         {
            // Show Usage
            showUsage();
            System.exit(3);
         }
         i++;
      } 
   }
   
   /**
    * Checks for missing arguments
    * 
    * @return List of missing required arguments
    */
   private static List<String> verifyReqArgs()
   {
      List<String> missingArgs = new ArrayList<String>();

      if (m_package == "")
      {
         missingArgs.add(m_package_flag);
      }
      if (m_host == "")
      {
         missingArgs.add(m_host_flag);
      }
      if (m_port == "")
      {
         missingArgs.add(m_port_flag);
      }
      if (m_user == "")
      {
         missingArgs.add(m_user_flag);
      }
      if (m_password == "")
      {
         missingArgs.add(m_password_flag);
      }
      return missingArgs;
   }
   
   /**
    * Shows command line usage
    */
   private static void showUsage()
   {
      System.out.println("Usage:");
      System.out.println("  " + m_package_flag + " [full path to .ppkg file - required]");
      System.out.println("  " + m_host_flag + " [host name or IP - required]");
      System.out.println("  " + m_port_flag + " [port - required]");
      System.out.println("  " + m_user_flag + " [username - required]");
      System.out.println("  " + m_password_flag + " [password - required]");
      System.out.println("  " + m_usessl_flag + " [true or false - optional - defaults to false]");
      System.out.println("  " + m_acceptwarnings_flag + " [true or false if you want to accept" );
      System.out.println("  " + "  warnings and continue install - optional - defaults to false]");

   }
   
   public static PSServerRegistration getServerReg()
   {
      String password = PSDeploymentServerConnection.encryptPwd(
            m_user, m_password);
      
      PSServerRegistration server = new PSServerRegistration(
            m_host,
            Integer.parseInt(m_port.trim()),
            m_user,
            password,
            true,
            m_usessl);
         return server;
   }
   
   public static boolean isConsoleMode()
   {
      return m_console;
   }
   
   public static boolean acceptWarnings()
   {
      return m_acceptwarnings;
   }
   
   public static ErrorDialogs getErrorDialog()
   {
      return ms_errDlg;
   }
   
   /**
    * The frame for this client.
    */
   private static PSPackageInstallerFrame ms_frame;
   
   /**
    * The object that can be used to display the error dialogs with in this
    * application. Initialized in <code>main()</code> method after constructing
    * the main frame and never <code>null</code> or modified after that.
    */
   @SuppressWarnings("unused")
   private static ErrorDialogs ms_errDlg;
   
   /**
    * Command line flags
    */
   static private String m_package_flag = "-package";
   static private String m_host_flag = "-host";
   static private String m_port_flag = "-port";
   static private String m_user_flag = "-user";
   static private String m_password_flag = "-password";
   static private String m_usessl_flag = "-usessl";
   static private String m_acceptwarnings_flag = "-acceptwarnings";
   
   /**
    * Variables set by command line arg values
    */
   static private String m_package = "";
   static private String m_host = "";
   static private String m_port = "";
   static private String m_user = "";
   static private String m_password = "";
   static private Boolean m_usessl = false;
   static private Boolean m_acceptwarnings = false;
   static private Boolean m_console = false;

   
   
   
}

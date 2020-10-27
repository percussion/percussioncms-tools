/******************************************************************************
 *
 * [ RhythmyxWorkbench.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * The application is a wrapper which launches the workbench with a workspace
 * based on the current user name.  This will allow each user to have a separate
 * workspace for the workbench.
 */
public class RhythmyxWorkbench
{
   /**
    * The main entry point for the workbench.  Launches eclipse with the 
    * appropriate arguments including the dynamic workspace setting.  The
    * workspace will be by default: 
    * 
    * <RxRoot>/rxconfig/Workbench/users/<OS User name>/workspaces/workspace
    *
    * @param astrArgs The command line arguments.  One optional argument, 
    * -workspace, may be specified followed by a workspace name, resulting in
    * a path of:
    * 
    * <RxRoot>/rxconfig/Workbench/users/<OS User name>/workspaces/<workspace name>
    * 
    * which overrides the default workspace.  If remote debugging options are
    * provided through the use of RhythmyxWorkbench.lax, the actual debug port
    * will be the value provided plus one, due to the inability of the 
    * InstallAnywhere launcher to pass java args down to child processes.
    */
   public static void main( String [] astrArgs )
   {
      String javaLauncher = "JRE\\bin\\javaw.exe";
      String workspace = "rxconfig\\Workbench\\users\\"
         + System.getProperty("user.name") + "\\workspaces\\";
      String workspaceName = "workspace";
      String additionalArgs = "";
     
      if (astrArgs.length == 2)
      {
         String arg = astrArgs[0];
         if (arg.equals("-workspace"))
            workspaceName = astrArgs[1];
      }
      
      workspace += workspaceName;
                
      FileInputStream fin = null;
      
      try
      {
         //Look for and read in any java arguments from RhythmyxWorkbench.lax
         File laxFile = new File("RhythmyxWorkbench.lax");
         
         if (laxFile.exists())
         {
            fin = new FileInputStream(laxFile);
            Properties props = new Properties();
            props.load(fin);
            
            additionalArgs = props.getProperty(ARGS_PROP_NAME, "");
         }
         
         int dbgPort = -1;
         String dbgAddress = "";
         String[] argSplit = additionalArgs.split(",");
         for (int i=0; i < argSplit.length; i++)
         {
            String arg = argSplit[i];
            if (arg.startsWith("address="))
            {
               dbgAddress = arg;
               
               String port = dbgAddress.substring(dbgAddress.indexOf("=") + 1);
               
               try
               {
                  dbgPort = Integer.parseInt(port);
               }
               catch (NumberFormatException ex)
               {
                  System.out.println("Error parsing debug port: " + port);
               }
               
               break;
            }
         }
          
         if (dbgPort != -1)
         {
            String addressStr = "address=" + (dbgPort + 1);
            additionalArgs = additionalArgs.replaceFirst(dbgAddress,
                  addressStr);
         }
         
         String launcherJar = LAUNCHER_PACKAGE_NAME 
              + "_1.0.1.R33x_v20070828.jar";
         String launcherDir = LAUNCHER_PACKAGE_NAME
              + ".win32.win32.x86_1.0.2.R331_v20071019";
         String launcherDll = "eclipse_1021.dll";
         
         // Search for the eclipse launcher files in case version has changed
         File pluginsDir = new File(".\\eclipse\\plugins");
         File[] plugins = pluginsDir.listFiles();
         for (File plugin : plugins)
         {
            String name = plugin.getName();
            if (!name.startsWith(LAUNCHER_PACKAGE_NAME))
               continue;
            
            if (plugin.isFile())
               launcherJar = name;
            else if (plugin.isDirectory())
            {
               launcherDir = plugin.getName();
               
               String[] files = plugin.list();
               for (String file : files)
               {
                  if (file.endsWith(".dll"))
                  {
                     launcherDll = file;
                     break;
                  }
               }
            }
         }
         
         String vmargs = DEFAULT_JVM_ARGS + " ";
         if (additionalArgs.trim().length() > 0)
            vmargs += additionalArgs + " ";
         vmargs += "-jar eclipse\\plugins\\" + launcherJar;
         
         String command = javaLauncher + " "
              + vmargs + " "
              + "-os win32 -ws win32 -arch x86 -showsplash "
              + "-launcher eclipse\\eclipse.exe "
              + "-name Eclipse "
              + "--launcher.library "
              + "eclipse\\plugins\\" + launcherDir + "\\" + launcherDll + " "
              + "-startup eclipse\\plugins\\" + launcherJar + " "
              + "-exitdata 172c_78c "
              + "-data " + workspace + " "
              + "-product " + PRODUCT_NAME + " "
              + "-vm " + javaLauncher + " "
              + "-vmargs " + vmargs;
               
         Process proc = Runtime.getRuntime().exec(command);
         
         new PSStreamGobbler(proc.getInputStream()).start();
         new PSStreamGobbler(proc.getErrorStream()).start();
      }
      catch(Exception e)
      {
         E2Designer.showErrorMessage(e.getMessage(), "RhythmyxWorkbench Error");
      }
   }

   /**
    * Empties stream passed into it in a separate thread line by line and
    * prints the line just read to the console.
    * To launch the gobbler call {@link #start()}.
    * The idea is borrowed from
    * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html. 
    */
   private static class PSStreamGobbler extends Thread
   {
      /**
       * Creates new stream gobbler.
       * @param in the stream to copy to the console.
       * Assumed not <code>null</code>.
       */
      private PSStreamGobbler(InputStream in)
      {
         m_in = in;
      }

      /**
       * Reads the stream provided in the constructor and prints it line-by-line
       * to the console. The method finishes when the stream is empty.
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run()
      {
         try
         {
            final BufferedReader reader =
                  new BufferedReader(new InputStreamReader(m_in));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
               System.out.println(line);
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      /**
       * The stream to read.
       */
      private final InputStream m_in;
   }
   
   /**
    * The .lax property used to specify additional java arguments.
    */
   private static final String ARGS_PROP_NAME =
      "lax.nl.java.option.additional";
   
   /**
    * The name of the Rhythmyx Workbench eclipse product.
    */
   private static final String PRODUCT_NAME = 
      "com.percussion.workbench.ui.RhythmyxWorkbenchProduct";
   
   /**
    * The default jvm arguments used as part of the launch command.
    */
   private static final String DEFAULT_JVM_ARGS = 
      "-Xms128m -Xmx384m -XX:MaxPermSize=256m";
   
   /**
    * The name of the eclipse launcher package.
    */
   private static final String LAUNCHER_PACKAGE_NAME =
      "org.eclipse.equinox.launcher";
}  



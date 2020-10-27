/*[ PSContentLoaderApp.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.loader.objectstore.PSContentLoaderConfig;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.ui.PSConnectionInfo;
import com.percussion.loader.ui.PSMainFrame;
import com.percussion.loader.ui.PSUserPreferences;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.UIManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The singleton class to start the Rhythmyx content loader.
 */
public class PSContentLoaderApp
{
   /**
    * No external constructor
    */
   private PSContentLoaderApp()
   {
   }

   /**
    * Gets the singleton instance of this class.
    *
    * @return The instance, never <code>null</code>.
    */
   public static PSContentLoaderApp getInstance()
   {
      if (ms_contentLoaderApp == null)
         ms_contentLoaderApp = new PSContentLoaderApp();

      return ms_contentLoaderApp;
   }

   /**
    * Starts the application.
    */
   public static void main(String[] args)
   {
      try
      {
         PSLoaderDescriptor descriptor = null;
         PSConnectionInfo con = null;
         PSContentLoaderApp loaderApp = null;
         try
         {
            BasicConfigurator.configure();  // init default log4j
            loaderApp = PSContentLoaderApp.getInstance();
            loaderApp.initFromArgs(args);
            descriptor = loaderApp.getDefaultDescriptor();
            con = PSConnectionInfo.getConnectionInfo();
         }
         catch (PSLoaderException e)
         {
            Logger.getLogger(PSContentLoaderApp.class.getName()).error(
               e.getMessage());
            if (!loaderApp.isInteractive())
               return;
         }

         if (descriptor != null)
         {
            PSConnectionDef connDef = descriptor.getConnectionDef();
            try
            {
               con.setProtocol(connDef.getServerProtocol());
               con.setServer(connDef.getServerName());
               con.setPort(connDef.getPort());
               con.setUser(connDef.getUser());
               con.setPassword(connDef.getPassword());
            }
            catch (PSLoaderException e)
            {
               /**
                * The default descriptor does not use a rhythmyx content loader.
                * We must obtain the connection info from the user preferences
                * which is used for catalogging, etc.
                */
               Logger.getLogger(PSContentLoaderApp.class.getName()).error(
                  e.getMessage());
            }
         }
         else
         {
            /**
             * If no descriptor is available we use the default connection
             * info for catalogging etc.
             */
            // todo...
         }

         if (loaderApp.isInteractive())
         {
            ms_help = PSJavaHelp.getInstance();
            ms_help.setHelpSet(PSJavaHelp.getHelpSetURL(HELPSET_FILE),
               "com.percussion.loader.ui.helptopicmapping");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            PSMainFrame.getFrame().setVisible(true);
            PSMainFrame.getFrame().initDescriptor(descriptor);
         }
         else
         {
            loaderApp.silentProcess(descriptor);
         }
      }
      catch(Exception e)
      {
         Logger.getLogger(PSContentLoaderApp.class.getName())
            .info(e.getMessage());
      }
   }

   /**
    * Get configuration information.
    *
    * @return The configuration object, never <code>null</code>.
    */
   public static PSContentLoaderConfig getConfig()
   {
      return m_config;
   }

   /**
    * Get descriptor information.
    *
    * @return The descriptor object, never <code>null</code>.
    */
   public static PSLoaderDescriptor getDescriptor(String path)
   {
      PSLoaderDescriptor a = null;
      try
      {
         a = loadDescriptor(path);
      }
      catch(Exception e)
      {
         Logger.getLogger(PSContentLoaderApp.class.getName())
            .info(e.getMessage());
      }
      return a;
   }

   /**
    * Silently processing the content loading operation. Save the status
    * after the operation is successfully finished.
    *
    * @param desc The descriptor definition, may not <code>null</code>
    *
    * @throws PSLoaderException if any error occurs.
    */
   public void silentProcess(PSLoaderDescriptor desc) throws PSLoaderException
   {
      if (desc == null)
         throw new IllegalArgumentException("desc may not be null");

      PSLoaderRepositoryHandler repositoryHandler;
      repositoryHandler = new PSLoaderRepositoryHandler(desc.getPath());
      PSContentStatus status = repositoryHandler.getStatus();
      IPSContentTree tree = (status != null) ? status.getContentTree() : null;

      // get the tree from selector
      PSContentSelectorMgr selectorMgr = new PSContentSelectorMgr(desc, status);
      selectorMgr.run();

      if (! selectorMgr.isAborted())
      {
         tree = selectorMgr.getContentTree();

         // load the tree
         PSContentLoaderMgr loaderMgr = new PSContentLoaderMgr(desc, tree,
            selectorMgr.hasErrorOccured());
         loaderMgr.run();

         if (loaderMgr.isAborted())
         {
            if (loaderMgr.getRunException() != null)
               throw loaderMgr.getRunException();
         }
      }
      else
      {
         if (selectorMgr.getRunException() != null)
            throw selectorMgr.getRunException();
      }
   }

   /**
    * Initialization from the command line arguments. The options are:
    * [-descriptor <descriptor directory>] [-server <name>] [-port <#>] [-p <path>]
    * [-noui] [-d]
    *
    * @param args The command line arguments.
    *
    * @throws PSLoaderException if any error occurs.
    */
   public void initFromArgs(String[] args) throws PSLoaderException
   {
      Document cfgDoc = loadConfig();
      Document descDoc = null;
      PSLoaderException loaderEx = null;
      String defaultDescpath = "";

      for (int i=0; i < args.length; i++)
      {
         if (DESCRIPTOR.equals(args[i]) && ((i + 1) < args.length))
            defaultDescpath = args[++i];
         else if (SERVER.equals(args[i]) && ((i + 1) < args.length))
            m_server = args[++i];
         else if (PORT.equals(args[i]) && ((i + 1) < args.length))
            m_port = Integer.parseInt(args[++i]);
         else if (PREVIEW_PATH.equals(args[i]) && ((i + 1) < args.length))
            m_previewPath = args[++i];
         else if (NONINTERACTIVE.equals(args[i]))
            m_isUIMode = false;
         else if (DEBUG_LOG.equals(args[i]))
            m_isDebug = true;
         else
         {
            BufferedReader conReader = new BufferedReader(
               new InputStreamReader(System.in));
            loaderEx = new PSLoaderException(IPSLoaderErrors.UNKNOWN_ARG, args[i]);
            System.out.println(loaderEx.getLocalizedMessage());
            System.out.println("Press ENTER to continue...");
            try
            {
               conReader.readLine();
               System.exit(1);
            }
            catch (java.io.IOException e){}
         }
      }
      if (!m_isUIMode)
         m_defaultDescriptor = loadDescriptor(defaultDescpath);
      // update the default descriptor if specified
      if (m_defaultDescriptor != null)
      {
         if ( m_server != null )
            m_defaultDescriptor.getConnectionDef().setServerName(m_server);
         if ( m_port != -1 )
            m_defaultDescriptor.getConnectionDef().setPortInt(m_port);

         descDoc = getRepositoryHandler(
         m_defaultDescriptor.getPath()).getDescriptorDoc();
         DOMConfigurator.configure(m_defaultDescriptor.getLogDef().toXml(
            descDoc));
      }
      else
      {
         PSUserPreferences preferences = PSUserPreferences.deserialize();
         String strDescPath = "";
         strDescPath = preferences.getLastDescPath();
         if (defaultDescpath != null && defaultDescpath.length() != 0)
            strDescPath = defaultDescpath;
         if (strDescPath.length() != 0)
         {
            try
            {

               m_defaultDescriptor = loadDescriptor(strDescPath);
               descDoc = getRepositoryHandler(strDescPath).getDescriptorDoc();
            }
            catch(PSLoaderException e)
            {
               Logger.getLogger(PSContentLoaderApp.class.getName()).warn(
                  "Fail to load descriptor, \"" + strDescPath + "\". " +
                  e.getMessage());
               
               DOMConfigurator.configure(m_config.getLogDef().toXml(cfgDoc ));
               throw e;
            }
         }
         if ( m_defaultDescriptor != null)
            //init default log4j from the desc information
            DOMConfigurator.configure(
               m_defaultDescriptor.getLogDef().toXml(descDoc));
         else
            //init default log4j from the configuration information
            DOMConfigurator.configure(m_config.getLogDef().toXml(cfgDoc ));
      }
   }

   /**
    * Load a specified descriptor and its related files.
    *
    * @param descDir The directory of the descriptor. The descriptor file must
    *    be the same name as the directory and with "xml" extension.
    *
    * @throws PSLoaderException if any error occurs.
    */
   public static PSLoaderDescriptor loadDescriptor(String descDir)
         throws PSLoaderException
   {
      return getRepositoryHandler(descDir).getDescriptor();
   }

   /**
    * Gets the repository handler from a descriptor directory.
    *
    * @param descDir The directory of the descriptor, never <code>null</code>
    * or empty.
    *
    * @return {@link PSLoaderRepositoryHandler} object, never <code>null</code>.
    *
    * @throws PSLoaderException if the descriptor file does not exist or the
    * file name is wrong.
    *
    * @throws IllegalArgumentException if the supplied argument is invalid.
    */
   public static PSLoaderRepositoryHandler getRepositoryHandler(String descDir)
         throws PSLoaderException
   {
      if (descDir == null || descDir.length() == 0)
         throw new IllegalArgumentException("descDir is never null or empty");

      File descDirFile = new File(descDir);
      // validate the file path of the descriptor
      if (!descDirFile.exists())
         throw new PSLoaderException(IPSLoaderErrors.INVALID_DESCRIPTOR_PATH,
         descDir);

      // make sure the path is in the format of  .../XXX/XXX.xml
      int ind = descDir.lastIndexOf(File.separator);
      String descName = descDir.substring(ind + 1);
      String descFileName = descName +
         PSLoaderRepositoryHandler.DESCRIPTOR_EXTESION;
      String descFullPath = descDir + File.separator + descFileName;
      File descFile = new File(descFullPath);
      if (!descFile.exists())
         throw new PSLoaderException(IPSLoaderErrors.DESCRIPTOR_FILE_NOT_EXIST,
         new String[]{descFileName, descFullPath});

      PSLoaderRepositoryHandler repositoryHandler;
      repositoryHandler = new PSLoaderRepositoryHandler(descDir);
      return repositoryHandler;
   }

   /**
    * Loads the content configuration information from the disk.
    *
    * @return The content loader configuration xml document, never <code>null
    * </code>
    *
   * @throws PSLoaderException if any error occurs.
    */
   private static Document loadConfig() throws PSLoaderException
   {
      Document doc = null;
      if (CONFIG_FILE.exists())
      {
         FileInputStream in = null;

         try
         {
            in = new FileInputStream(CONFIG_FILE);
            doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            Element configEl = doc.getDocumentElement();
            m_config = new PSContentLoaderConfig(configEl);
         }
         catch (Exception ex)
         {
            throw new PSLoaderException(IPSLoaderErrors.LOAD_CONFIG_ERROR,
               ex.toString());
         }
         finally
         {
            if (in != null)
               try {in.close();} catch(IOException e){}
         }
      }
      else
      {
         throw new PSLoaderException(IPSLoaderErrors.CONFIG_FILE_NOT_EXIST,
            CONFIG_FILE.getPath());
      }
      return doc;
   }

   /**
    * Get the override server name in descriptor, specified in the command line
    * arguments.
    *
    * @return The override server name if specified in the command line
    *    arguments, <code>null</code> if not specified.
    */
   public String getOverrideServer()
   {
      return m_server;
   }

   /**
    * Get the override port number in descriptor, specified in the command line
    * arguments.
    *
    * @return The override port number if specified in command line arguments,
    *    <code>-1</code> if not specified.
    */
   public int getOverridePort()
   {
      return m_port;
   }

   /**
    * Get preview path, overrides the Content Loader specified in the supplied
    * descriptor with the Preview Loader.
    *
    * @return The preview path if specified in the command line arguments;
    *    <code>null</code> if not specified.
    */
   public String getPreviewPath()
   {
      return m_previewPath;
   }

   /**
    * Determines whether to turn on extra debug information. Default to
    * <code>false</code> as in non-debug mode, unless specified from the
    * command line argument.
    *
    * @return <code>true</code> if turn on extra debug information;
    *    <code>false</code> otherwise.
    */
   public boolean isDebugOn()
   {
      return m_isDebug;
   }

   /**
    * Get the default descriptor specified in the command line arguments.
    *
    * @return The default descriptor, <code>null</code> if not specified in the
    *    command line arguments.
    *
    * @throws PSLoaderException if any error occurs.
    */
   public PSLoaderDescriptor getDefaultDescriptor() throws PSLoaderException
   {
      return m_defaultDescriptor;
   }

   /**
    * Determines whether is interactive mode.
    *
    * @return <code>true</code> if is in interactive mode; <code>false</code>
    *    otherwise.
    */
   public boolean isInteractive()
   {
      return m_isUIMode;
   }

   // private constant for various argument flags
   final private static String DESCRIPTOR = "-descriptor";
   final private static String SERVER = "-server";
   final private static String PORT = "-port";
   final private static String NONINTERACTIVE = "-noui";
   final private static String DEBUG_LOG = "-d";
   final private static String PREVIEW_PATH = "-p";


   /**
    * The default directory that contains the default file for loader
    * configuration information.
    */
   final private static File CONFIG_DIR = new File(
      "." + File.separator + "rxconfig", "ContentConnector");

   /**
    * The default file that contains loader configuration information.
    */
   final private static File CONFIG_FILE = new File(CONFIG_DIR,
      "contentconnector.xml");

   /**
    * The configuration information, initialized by <code>loadConfig()</code>,
    * never <code>null</code> after that.
    */
   private static PSContentLoaderConfig m_config;

   /**
    * Determines ui or non-ui mode. <code>true</code> is in
    * ui mode; <code>false</code> otherwise. Default to
    * <code>true</code> as ui mode.
    */
   private boolean m_isUIMode= true;

   /**
    * The computer name for the Rhythmyx content services interface that will
    * be used when submitting the items. Overrides the server in the descriptor.
    * Set by <code>initFromArgs()</code> if specified from the command line
    * arguments; <code>null</code> otherwise.
    */
   private String  m_server = null;

   /**
    * The port for the Rhythmyx server content web services interface.
    * Overrides the port provided in the descriptor. Set by
    * <code>initFromArgs()</code> if specified from the command line arguments;
    * <code>-1</code> otherwise.
    */
   private int m_port = -1;

   /**
    * Determines whether to turn on extra debug information. Set to
    * <code>true</code> by <code>initFromArgs()</code> if specified from the
    * command line arguments; <code>false</code> otherwise.
    */
   private boolean m_isDebug = false;

   /**
    * Overrides the Content Loader specified in the supplied descriptor with
    * the Preview Loader. Set by <code>initFromArgs()</code> if specified from
    * the command line arguments; <code>null</code> otherwise.
    */
   private String m_previewPath = null;

   /**
    * The default descriptor, which could be specified from the command line
    * arguments. Set by <code>loadDescriptor()</code> if specified;
    * <code>null</code> otherwise.
    */
   private PSLoaderDescriptor m_defaultDescriptor = null;

   /**
    * Singleton instance of this class, set by first call to
    * {@link #getInstance()}, never <code>null</code> or modified after that.
    */
   private static PSContentLoaderApp ms_contentLoaderApp = null;

   /**
    * The singleton instance of javahelp used to launch help, initialized in
    * <code>main()</code> method and never <code>null</code> or modified after
    * that.
    */
   private static PSJavaHelp ms_help;

   /**
    * The path of help set file relative to the rhythmyx root.
    */
   public static final String HELPSET_FILE =
      "Docs/Rhythmyx/Enterprise_Content_Connector/Rhythmyx_Enterprise_Content_Connector.hs";
}
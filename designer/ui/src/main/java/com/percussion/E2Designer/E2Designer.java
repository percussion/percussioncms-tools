/******************************************************************************
 *
 * [ E2Designer.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer;

import com.percussion.client.PSCoreFactory;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSFormatVersion;
import com.percussion.util.PSProperties;
import com.percussion.util.PSRemoteRequester;
import com.percussion.workbench.ui.legacy.PSLegacyInitialzer;
import org.w3c.dom.Document;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * The application does a few things:
 * <ul>
 * <li> Run the login dialog and validate server and user. </li>
 * <li> Creates the main window and displays it. </li>
 * <li> Creates the browser and displays it. </li>
 * <li> Loads the resource bundle for the main application (browser has its own
 *       resource bundle). </li>
 * <li> Creates the user config object. No static code should try to use the
 *       configurator. It won't be available until after the user login. If the
 *      the configurator can't be created, the program will exit.
 * </ul>
 */
public class E2Designer
{
   // system wide constants
   /**
    * The default port to use when connecting to the E2 server.
   **/
   public static final String DEFAULT_PORT = "9992";

   /**
    * The default SSL port to use when connecting to the E2 server.
   **/
   public static final String DEFAULT_SSL_PORT = "9443";

   public static final String PROPERTY_FILENAME = "designer.properties";

   /**
    * Constant for the name of the feature in the featureset for all features
    * added with the initial implementation of Content Explorer.
    */
   public static final String CX_FEATURE = "ContentExplorer";

  // todoph: move propsRoot resource here and add methods to access it

   // constructors
   /**
    * Creates a new designer application. Assumes the passed in params are
    * valid.
    *
    * @throws PSServerException if the strServer can't be found
    * @throws PSAuthorizationException if the specified uid/pw can't be
    * authenticated on the specified server
    * @throws PSAuthenticationFailedException If the login to the server failed
    * the verification of the credentials used.
    */
   public E2Designer(@SuppressWarnings("unused") String strServer,
         @SuppressWarnings("unused") String strUID,
         @SuppressWarnings("unused") String strPW,
         @SuppressWarnings("unused") String strPort,
         @SuppressWarnings("unused") String protocol)
      throws   PSServerException,
               PSAuthorizationException,
               PSAuthenticationFailedException
   {
      // load the resource bundle and create the main frame
      UIMainFrame frame = new UIMainFrame( ms_conn,
         ms_propsLogin.getProperty( PSDesignerConnection.PROPERTY_HOST ));
      setMainFrame(frame);
      frame.setVisible(true);

   }
   
   /**
    * Creates a new designer application initialized with the provided main
    * frame.
    * Used for compatibility with legacy application only.
    */
   public E2Designer(UIMainFrame frame)
   {
      setMainFrame(frame);
   }

   /**
    * @return the main frame instance for this program
    */
   public UIMainFrame getMainFrame()
   {
      return m_mainFrame;
   }
   
   /**
    * Assigns main frame.
    */
   private void setMainFrame(UIMainFrame mainFrame)
   {
      m_mainFrame = mainFrame;
   }

   public static E2Designer getApp()
   {
      if (ms_theApp == null)
      {
         PSLegacyInitialzer.initializeLegacySystems();
      }
      return ms_theApp;
   }

   /**
    * The main entry point for the workbench. Performs the login and init
    * functionality. The following params are supported (either / or - can be
    * used to indicate a param). If an optional param that requires a value
    * doesn't have one, it is ignored.<p>
    * <p>Usage:
    * <p>java com.percussion.E2Designer.E2Designer
    *                         [-uid user [/pw pass] [/s server] [-port port]]
    * <ul>
    *    <li>-uid : User id to login to the Rx server. If present with a value,
    *       autologin is enabled. </li>
    *    <li>/pw : Used for autologin. User's password.</li>
    *    <li>/s : Used for autologin. The computer name where the Rx server is
    *       running. If not supplied, 'localhost' is used.</li>
    *    <li>-port : Used for autologin. The port the Rx server is listening
    *       on. If not supplied, 9992 is used.</li>
    * </ul>
    * For example:<p>
    *    java com.percussion.E2Designer.E2Designer -uid admin1 /pw demo
    *
    * @param astrArgs The command line arguments. See Java doc for more details.
    */
   public static void main( String [] astrArgs )
   {
      try
      {
         // turn on debugging during development
         // Todoph - use command line param to enable debugging
         Debug.setEnabled(false);

         // Set our default look and feel
         String strLnFClass = UIManager.getSystemLookAndFeelClassName();
         LookAndFeel lnf =
               (LookAndFeel) Class.forName(strLnFClass).newInstance();
         UIManager.setLookAndFeel( lnf );

         // have the user log in
         // temporary defaults
         String strServer = "E2";
         String strUID = ""; //now being persisted
         String strPW = "";
         String strPort = "";
         String strProtocol = "";

         JFrame f = new JFrame();   // temp frame for dialog only
         LoginDialog dlgLogin = new LoginDialog( f, strServer, strUID );

         // creating an entry in the task bar, so it is easier to get to the
         // login dialog
         f.setTitle(dlgLogin.getTitle());
         f.setLocation(10000,10000); // workaround: make the JFrame invisible
         E2DesignerResources rb = E2Designer.getResources();
         String strKey = "MainIconFilename";
         ImageIcon icon = ResourceHelper.getIcon( rb, strKey );
         if (null != icon)
            f.setIconImage( icon.getImage( ) ); // adding Rx icon to title bar
         else
            PSDlgUtil.showErrorDialog(
                               rb.getString( "LoadIconFail" ) + ": MainIcon",
                                     rb.getString("OpErrorTitle"));

         f.setVisible(true);
         boolean bConn = false;
         boolean bCancelled = false;
         Properties args = processArgs( astrArgs );
         String uid = args.getProperty( "uid", null );
         if ( null != uid && uid.trim().length() > 0 )
         {
            strServer = args.getProperty( "srv", null );
            if ( null == strServer || strServer.trim().length() == 0 )
               strServer = "localhost";
            ms_propsLogin.setProperty(
                  PSDesignerConnection.PROPERTY_HOST, strServer );

            strUID = args.getProperty( "uid", null );
            // guaranteed to be present
            ms_propsLogin.setProperty(
                  PSDesignerConnection.PROPERTY_LOGIN_ID, strUID );

            strPW = args.getProperty( "pw", null );
            if ( null == strPW )
               strPW = "";
            ms_propsLogin.setProperty(
                  PSDesignerConnection.PROPERTY_LOGIN_PW, strPW );

            strProtocol =
               args.getProperty(PSDesignerConnection.PROPERTY_PROTOCOL, "http");
            ms_propsLogin.setProperty(PSDesignerConnection.PROPERTY_PROTOCOL,
               strProtocol);

            strPort = args.getProperty( "port", null );

            if ( null == strPort || strPort.trim().length() == 0 )
            {
               if (strProtocol.trim().compareToIgnoreCase("https")==0)
                  strPort = DEFAULT_SSL_PORT;
               else
                  strPort = DEFAULT_PORT;
            }

            ms_propsLogin.setProperty(
                  PSDesignerConnection.PROPERTY_PORT, strPort );
            setSSLProp(strProtocol, ms_propsLogin);
            
            try
            {
               ms_conn = new PSDesignerConnection( ms_propsLogin );
               System.out.println( "server = " + strServer );
               bConn = true;
            }
            catch ( Exception e )
            {
               System.out.println( "Failed to connect: " +
                     e.getLocalizedMessage());
            }
         }
         else
         {
            // keep trying to login until they are successful or cancel
            do
            {
               dlgLogin.setVisible(true);
               if ( dlgLogin.isCancelled())
               {
                  bCancelled = true;
                  break;
               }


               strServer = dlgLogin.getServerName();
               strUID = dlgLogin.getUserName();
               strPW = dlgLogin.getPasswordString();
               strPort = dlgLogin.getPort();
               strProtocol = dlgLogin.getProtocol();

               // create an object store to use when creating/loading apps
               // save the login info because it can't be retrieved from the os
               ms_propsLogin.setProperty(
                     PSDesignerConnection.PROPERTY_HOST, strServer );
               ms_propsLogin.setProperty(
                     PSDesignerConnection.PROPERTY_LOGIN_ID, strUID );
               ms_propsLogin.setProperty(
                     PSDesignerConnection.PROPERTY_LOGIN_PW, strPW );
               ms_propsLogin.setProperty(
                     PSDesignerConnection.PROPERTY_PORT, strPort );

               ms_propsLogin.setProperty(PSDesignerConnection.PROPERTY_LOCALE, 
                  PSI18nUtils.DEFAULT_LANG );

               ms_propsLogin.setProperty(
                     PSDesignerConnection.PROPERTY_PROTOCOL, strProtocol);
               setSSLProp(strProtocol, ms_propsLogin);

               try
               {
                  ms_conn = new PSDesignerConnection( ms_propsLogin );
                  System.out.println( "server = " + strServer );
                  //System.out.println( "userid = " + strUID );
                  bConn = true;
               }
               catch (IllegalArgumentException e)
               {
                  Debug.assertTrue( false, E2Designer.getResources( ),
                        "ChangedPropKeys", null );
               }
               catch (PSServerException e)
               {
                  Object[] astrParams =
                  {
                     e.getLocalizedMessage()
                  };
                  showErrorMessage(MessageFormat.format(
                        getResources().getString( "ServerException" ),
                        astrParams ), null );
               }
               catch (PSAuthorizationException e)
               {
                  /* the server is throwing this exception for both
                     authentication and author..., so show the error returned
                     rather than a nice message.
                     TODOph: Use the nice message below when the server has
                     fixed this in the PSDesignerConnection */
                  showErrorMessage(e.getLocalizedMessage(), null );
               }
               catch (PSAuthenticationFailedException e)
               {
                  showErrorMessage(e.getLocalizedMessage(),
                     getResources().getString( "AuthenticationFailedTitle" ));
               }
            } while ( !bConn );
         }

         // load internationalization for Designer:
         String i18nErrMsg = null;
         ms_i18nKeyValue =
            PSI18NTranslationKeyValues.getInstance();
         try
         {
            ms_i18nKeyValue.load(
               new PSRemoteRequester(getLoginProperties()));
         }
         catch (Exception e)
         {
            // this feature may not be supported - error will be handled after
            // the feature set is loaded.
            i18nErrMsg = e.getLocalizedMessage();
         }

         // get the client version
         PSFormatVersion clientVersion =
               new PSFormatVersion("com.percussion.E2Designer");

         // if user cancelled dialog, then no connection was made;
         if ( null != ms_conn )
         {
            // Check the server version, if it doesn't match ours properly, exit
            PSFormatVersion serverVersion = ms_conn.getServerVersion();
            if (serverVersion == null ||
                  !ms_conn.checkVersionCompatibility(
                     clientVersion))
            {
               String strServerVersion;
               String msg;
               String interfaceVersion;
               if (serverVersion != null)
               {
                  strServerVersion = serverVersion.getVersionString();
                  interfaceVersion = new Integer(
                     serverVersion.getInterfaceVersion()).toString();
               }
               else
               {
                  strServerVersion = getResources().getString(
                     "UnknownServerVersion");
                  interfaceVersion = "0";
               }

               msg = "InterfaceMismatch";
               String msgPattern = getResources().getString( msg );
               Object[] params = { strServerVersion, interfaceVersion};

               showErrorMessage(MessageFormat.format( msgPattern, params ),
                     getResources().getString( "VersionMismatchTitle" ));
               bCancelled = true;
            }
         }
         else
            bCancelled = true;

         // clean up
         dlgLogin.dispose();
         f.dispose();

         if ( bCancelled )
            System.exit(0);     

         //add the server to server list if not already in
          String strServers = E2Designer.getDesignerProperties().getProperty(
               LoginDialog.ALL_SERVERS);

          boolean bFound = false;
          if(strServers != null)
          {
            StringTokenizer tokens =
                  new StringTokenizer(strServers, UserConfig.DELIMITER);

            while(tokens.hasMoreTokens())
            {
               if(tokens.nextToken().equals(strServer))
               {
                  bFound = true;
                  break;
               }
            }
         }

         if(strServers == null)
            strServers = new String("");

         if(!bFound && strServer != null && strServer.trim().length() != 0)
            strServers += strServer + UserConfig.DELIMITER;

         E2Designer.getDesignerProperties().setProperty(
               LoginDialog.ALL_SERVERS, strServers);

         if(strServer != null)
         {
            E2Designer.getDesignerProperties().setProperty(
                  LoginDialog.LAST_SERVER, strServer);
         }

         if (strProtocol!=null
             && strProtocol.trim().compareToIgnoreCase("https")==0)
         {
            E2Designer.getDesignerProperties().setProperty(
               LoginDialog.LAST_SSL_PORT, strPort);
         }
         else
         {
            E2Designer.getDesignerProperties().setProperty(
               LoginDialog.LAST_PORT, strPort);
         }

         E2Designer.getDesignerProperties().setProperty(
               LoginDialog.LAST_PROTOCOL, strProtocol);

         E2Designer.getDesignerProperties().setProperty(
               LoginDialog.LAST_USER, strUID);

         ms_theApp =
            new E2Designer(strServer, strUID, strPW, strPort, strProtocol);

         // since Designer successfully started, LoginDialog can go away now
         dlgLogin.dispose();

         OSLoadSaveHelper.createSaveLog();

         /* check on feature support - if there is error text, then there
          * was a problem loading the featureset from the server.
          */
         String featureSupportErr = FeatureSet.getFeatureSet().getErrorText();
         if (featureSupportErr != null)
         {
            // log the message
            System.out.println(
               "Failed to load server feature support file.  The error was: " +
               featureSupportErr);

            // Display a warning
            PSDlgUtil.showWarningDialog(getResources().getString("featureSetWarning"),
                  getResources().getString("Warning"));

         }
         else
         {
            // make sure we loaded i18n resources if required
            if(FeatureSet.isFeatureSupported(CX_FEATURE))
            {
               if (i18nErrMsg != null)
               {
                  PSDlgUtil.showWarningDialog(
                     getResources().getString("i18nResourcesNotLoaded"),
                     getResources().getString("Warning"));
               }
            }
         }

         try
         {
            if (makeDBLookupRequest(DBLOOKUP_UPPER_RESOURCE))
            {
               if (makeDBLookupRequest(DBLOOKUP_LOWER_RESOURCE))
                  ms_isDBCaseSensitive = false;
            }
         }
         catch (Exception ex)
         {
            System.out.println(ex.getLocalizedMessage());
            showErrorMessage(ex.getLocalizedMessage(), null);
         }
      }
      catch(MissingResourceException e)
      {
         Object[] astrParams =
         {
            e.getLocalizedMessage(),
            e.getKey()
         };
         FatalError(MessageFormat.format(
            getResources().getString("MissingResourceExceptionFormat"),
            astrParams ));
      }
      catch (Throwable e)
      {
         // we're on our way out, so notify user
         System.out.println( "Unexpected exception in main" );
         e.printStackTrace();
         FatalError(e.getLocalizedMessage());
      }
   }

   /**
    * This is a very simple cmd line argument processor. It converts the
    * supplied array of strings into a property list. Params may be specified
    * using - or /. Params have optional values. For every parameter found,
    * an entry is added to the properties file; the key is the param name, the
    * value is the following string, if there is one and it's not a param,
    * otherwise, the empty string is set as the value.
    *
    * @param args An array of 0 or more. If <code>null</code>, returns
    *    immediately.
    *
    * @return A valid object with 0 or more properties set. Never <code>null
    *    </code>.
    */
   private static Properties processArgs( String [] args )
   {
      Properties props = new Properties();
      if ( null == args )
         return props;

      String param = null;
      for ( int i = 0; i < args.length; i++ )
      {
         String p = args[i];
         if ( p.charAt(0) == '/' || p.charAt(0) == '-' )
         {
            if ( null != param )
            {
               props.setProperty( param, "" );
               param = null;
            }
            param = p.substring(1);
         }
         else if ( null != param )
         {
            props.setProperty( param, args[i] );
            param = null;
         }
         // else skip value w/o param
      }
      // was the last element a param?
      if ( null != param )
         props.setProperty( param, "" );
      return props;
   }

   /**
    * Set the <code>useSSL</code> prop in the supplied login properties.
    * 
    * @param strProtocol The protocol, assumed not <code>null</code> or empty,
    * and to be either "http" or "https", case-insensitive.
    * @param loginProps The properties to which the <code>useSSL</code>
    * property value is added, assumed not <code>null</code>.
    */
   private static void setSSLProp(String strProtocol, Properties loginProps)
   {
      loginProps.setProperty("useSSL", 
         (strProtocol.trim().compareToIgnoreCase("https")==0) ? "true" : 
            "false");
   }
   
   /**
    * Returns the resource bundle containing the internationalized strings
    * for the application. The bundle is loaded the first time the method
    * is called. If the resouce bundle can't be found, a dialog is displayed
    * to the user and an exception is thrown to terminate the program.
    * <p>
    * The bundle is loaded here rather than using a static block so we don't
    * have to worry about order of execution of static code.
    * <p>
    * Initially, we are loading the resource bundle based on the default
    * locale. We could potentially override this in the future and allow
    * users to specify their desired locale. This will be a little difficult
    * because we want to have this available for static code which gets executed
    * before main is called.
    */
   public static E2DesignerResources getResources()
   {
      if (null == ms_ResBundle)
      {
         final String strResBaseName = "com.percussion.E2Designer.E2DesignerResources";
         try
         {
            // load the string resources for the application
            ms_ResBundle = (E2DesignerResources)ResourceBundle.getBundle(strResBaseName);
         }
         catch(MissingResourceException e)
         {
            Object[] astrParams =
            {
               e.getLocalizedMessage(),
               e.getKey()
            };
            FatalError(MessageFormat.format( ms_ResBundle.getString( "MissingResources" ),
                  astrParams ));
         }
      }
      return ms_ResBundle;
   }


   // private implementation

   /**
    * This method prints a message to the screen, indicating that the error
    * will terminate the program. After the user clicks OK, the program is
    * terminated.
    *
    * @param strMsg this text is shown in the dialog. It should describe the
    * problem that caused the error.
    */
   public static void FatalError(String strMsg)
   {
      ResourceBundle rb = getResources();
      String strFormat = null;
      String strFatalDlgTitle = null;
      if (null != rb)
      {
         try
         {
            strFormat = rb.getString("FatalException");
            strFatalDlgTitle = rb.getString("FatalDlgTitle");
         }
         catch (MissingResourceException e)
         {
            // ignore this, the strings will be set below
         }
      }
      if (null == strFormat)
         strFormat = "A fatal exception has occurred with the following message:\n {0}. "
            + "The program cannot continue.";
      if (null == strFatalDlgTitle)
         strFatalDlgTitle = "Fatal Error";

      Object[] astrParams =
      {
         strMsg
      };

      showErrorMessage(MessageFormat.format(strFormat, astrParams), strFatalDlgTitle );
      System.out.println( "Fatal error, program terminating" );
      System.exit(-1);
   }

   /**
    * Displays the supplied message to the user w/ the supplied title, or a default
    * title if null.
    *
    * @param msg The text to display.
    *
    * @param title The title for the dialog. If null, a default title is used.
   **/
   public static void showErrorMessage(String msg,String title )
   {
      if ( null == title )
      {
         title = getResources().getString("ExceptionTitle");
      }
      PSDlgUtil.showErrorDialog(Util.cropErrorMessage(msg), title);
   }

   /**
    * Get the designer properties.
    *
    * @return     the designer properties
    */
   public static PSProperties getDesignerProperties()
   {
      if ( null == ms_designerProperties )
      {
         File file = null;
         try
         {
            file = PSProperties.getConfig(ENTRY_NAME, PROPERTY_FILENAME,
               new File(ms_rhythmyxDir, DESIGNER_DIR).getAbsolutePath());
            //if(!file.exists())
              // file.createNewFile();
            ms_designerProperties = new PSProperties ();

         }
         catch (Exception e)
         {
            e.printStackTrace();
            /*We do not want an error message here. If file not found
            designer properties will need to be entered manuly. When saved
            the file will be sent to the right location.*/
            System.out.println("System could not find: " + file.getAbsolutePath());
         }
      }
      return(ms_designerProperties);
   }

   /**
    * Saves the designer properties file and closes the save log.  Prints a
    * message to <code>System.err</code> if the designer properties is missing,
    * read-only, or if an <code>IOException</code> occurs.
    */
   public void saveDesignerProperties()
   {
      if(ms_designerProperties != null)
      {
         File file = null;
         try
         {
            file = PSProperties.getConfig(ENTRY_NAME, PROPERTY_FILENAME,
               new File(ms_rhythmyxDir, DESIGNER_DIR).getAbsolutePath());
            if (file != null && file.canWrite())
               ms_designerProperties.store(new FileOutputStream(
                  file.getAbsolutePath()), null);
            else
               System.err.println(
                  "Designer properties file is missing or read-only: " +
                  file.getAbsolutePath() );

            OSLoadSaveHelper.closeSaveLog();
         }
         catch(IOException e)
         {
            e.printStackTrace();
         }
      }
   }

   /**
    * Creates an integer ID from 1 and increments by 1 for each new applcation
    * created.
    */
  public static int createAppId()
   {
    return ++ms_appNum;
   }

   /**
    * Determine whether the database is case-sensitive or not.
    *
    * @return <code>true</code> if the database is case-sensitive, otherwise
    * <code>false</code>
    */
   public static boolean isDBCaseSensitive()
   {
      return ms_isDBCaseSensitive;
   }

   /**
    * The connection is one of the first things created inside main(). If not
    * successfully created, the program will exit. It should be valid by the time
    * nearly any class needs it.
    *
    * @return A connection that was valid when created, or null if it hasn't been
    * created yet.
   **/
   public static PSDesignerConnection getDesignerConnection()
   {
      return PSCoreFactory.getInstance().getDesignerConnection();
   }


   /**
    * @return The properties object that contains the user credentials and server
    * connection info that was used to connect to the server for this session.
   **/
   public static Properties getLoginProperties()
   {
      // update current jsessionid
      ms_propsLogin.setProperty(PSDesignerConnection.PROPERTY_JSESSION_ID, 
         ms_conn.getJSessionId());
      return ms_propsLogin;
   }

   /**
    * Makes a request to the specified resource in "sys_psxCms" app and
    * parses the response document.
    *
    * @param resourceName the name of the resource in "sys_psxCms" app,
    * assumed not <code>null</code> and non-empty
    *
    * @return <code>true</code> if the response document has an integer value
    * for <code>ATTR_CASE_SENSITVE</code> attribute, otherwise false.
    *
    * @throws IOException if an I/O error occurs
    */
   private static boolean makeDBLookupRequest(String resourceName)
      throws IOException
   {
      boolean ret = false;

      ApplicationRequestor app = new ApplicationRequestor(
         ms_conn, CatalogHelper.getRequestRoot());
      Document respDoc = app.makeRequest("sys_psxCms", resourceName);

      if ((respDoc != null) && (respDoc.getDocumentElement() != null))
      {
         String strCaseSensitive =
            respDoc.getDocumentElement().getAttribute(ATTR_CASE_SENSITVE);
         if ((strCaseSensitive != null) &&
            (strCaseSensitive.trim().length() > 0))
         {
            try
            {
               Integer.parseInt(strCaseSensitive);
               ret = true;
            }
            catch(NumberFormatException ex)
            {
            }
         }
      }
      return ret;
   }

   // Variables
   /**
    * This is static so it can be loaded if needed by any static methods or
    * variables. It is assigned a value the first time someone tries to access it.
    */
   private static E2DesignerResources ms_ResBundle = null;

   /**
    * The singleton instance of the main frame window.
    */
   private UIMainFrame m_mainFrame;

   /**
    * The singleton instance of the application.
    */
   public static E2Designer ms_theApp = null;
   private static PSProperties   ms_designerProperties = null;
   private static int ms_appNum = 0;
   public static Properties ms_propsLogin = new Properties( );
   public static PSDesignerConnection ms_conn = null;

   /**
    * Initialized in the init, never <code>null</code>
    * after that and invariant.
    */
   private static PSI18NTranslationKeyValues ms_i18nKeyValue = null;

   /**
    * Constant for the name of the entry that reperesents workbench's name/value
    * pair.
    */
   public static final String ENTRY_NAME = "designer_config_base_dir";

   /**
    * Stores the case sensitivity of the database. If <code>true</code> then
    * the database is case-sensitive, otherwise not.
    * Defaults to <code>true</code>. Set to <code>false</code> in the
    * <code>main()</code> method if the database is not case-sensitive.
    * Never modified after that.
    */
   private static boolean ms_isDBCaseSensitive = true;

   /**
    * Name of the resource in "sys_psxCms" app which performs a query similar to:
    * SELECT NEXTNR FROM NEXTNUMBER WHERE UPPER(KEYNAME) = UPPER('PSX_PROPERTIES')
    * This should return single row irrespective of the case-sensitivity
    * of the database.
    */
   private static final String DBLOOKUP_UPPER_RESOURCE = "DBLookupUpper.xml";

   /**
    * Name of the resource in "sys_psxCms" app which performs a query similar to:
    * SELECT NEXTNR FROM NEXTNUMBER WHERE KEYNAME = 'psx_properties'
    * This query will return a row only if the database is case-insensitive.
    */
   private static final String DBLOOKUP_LOWER_RESOURCE = "DBLookupLower.xml";

   /**
    * attribute of the root element of the response document obtained by making
    * a request to either <code>DBLOOKUP_LOWER_URL</code> or
    * <code>DBLOOKUP_UPPER_URL</code> resource
    */
   private static final String  ATTR_CASE_SENSITVE = "caseSensitive";

   /** The workbench configs, this is the default for the case not
    *  init.properties exists
    *  Constant for the directory containing workbench configs.
    *  Assumed to be relative to the Rx directory.
    */
    public static final String DESIGNER_DIR = "rxconfig/Workbench";

   /**
    * Rhythmyx directory. By default this is a current directory.
    * This is a hack to make legacy code run.
    */
   public static File ms_rhythmyxDir = new File(".");
}



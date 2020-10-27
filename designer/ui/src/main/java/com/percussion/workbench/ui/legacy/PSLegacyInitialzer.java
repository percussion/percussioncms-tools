/******************************************************************************
 *
 * [ PSLegacyInitialzer.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.legacy;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.E2DesignerResources;
import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.LoginDialog;
import com.percussion.E2Designer.OSLoadSaveHelper;
import com.percussion.E2Designer.UIContentEditorHandler;
import com.percussion.E2Designer.UIMainFrame;
import com.percussion.E2Designer.browser.BrowserFrame;
import com.percussion.client.PSCoreFactory;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.util.PSRemoteRequester;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import javax.swing.*;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Performs the same initialization as E2Designer and sets up and launches
 * UI component for testing.  
 * Swing implementation.
 *
 * @author Andriy Palamarchuk
 */
public class PSLegacyInitialzer
{
   public static E2DesignerResources getResources()
   {
      final String strResBaseName =
            "com.percussion.E2Designer.E2DesignerResources";
      try
      {
         // load the string resources for the application
         return (E2DesignerResources)ResourceBundle.getBundle(strResBaseName);
      }
      catch(MissingResourceException e)
      {
         throw new AssertionError(e);
      }
   }

   /**
    * Initializes legacy subsystems if they are not initialized yet.
    */
   public static synchronized void initializeLegacySystems()
   {
      if (!ms_initializeRequired || E2Designer.ms_theApp != null)
      {
         return;
      }
      main(null);
      getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            UIContentEditorHandler.initSwtResources();
         }
      });
   }

   public static void main(String[] astrArgs)
   {
         E2Designer.ms_rhythmyxDir = PSWorkbenchPlugin.getDefault().getStateLocation().toFile();
         try
         {
            BasicConfigurator.configure();
            configureLookAndFeel();

            final PSDesignerConnection ms_conn = PSCoreFactory.getInstance().getDesignerConnection();
            if (ms_conn == null)
            {
               throw new RuntimeException("Connect to the server first!");
            }
            E2Designer.ms_conn = ms_conn;
               
            final Properties propsLogin = new Properties();
            E2Designer.ms_propsLogin = propsLogin;
            propsLogin.setProperty(
                  PSDesignerConnection.PROPERTY_HOST, ms_conn.getServer());
            propsLogin.setProperty(
                  PSDesignerConnection.PROPERTY_LOGIN_ID, ms_conn.getUserName());
//            ms_propsLogin.setProperty(
//                  PSDesignerConnection.PROPERTY_LOGIN_PW, ms_conn.get);
            propsLogin.setProperty(PSDesignerConnection.PROPERTY_PROTOCOL,
                  ms_conn.getProtocol());
            propsLogin.setProperty(
                     PSDesignerConnection.PROPERTY_PORT, Integer.toString(ms_conn.getPort()));
            propsLogin.setProperty("useSSL", "false"); 

            // load internationalization for Designer:
            final PSI18NTranslationKeyValues ms_i18nKeyValue =
               PSI18NTranslationKeyValues.getInstance();
            propsLogin.setProperty(PSDesignerConnection.PROPERTY_JSESSION_ID, 
                  ms_conn.getJSessionId());
            try
            {
               ms_i18nKeyValue.load(
                  new PSRemoteRequester(propsLogin));
            }
            catch (Exception e)
            {
               // this feature may not be supported - error will be handled after
               // the feature set is loaded.
//               i18nErrMsg = e.getLocalizedMessage();
            }
            setupPortAndProtocol(ms_conn.getUserName(), Integer.toString(ms_conn.getPort()),
                  ms_conn.getProtocol());

            final String host =
               propsLogin.getProperty(PSDesignerConnection.PROPERTY_HOST);
            final UIMainFrame mainFrame = new UIMainFrame(ms_conn, host);
            E2Designer.ms_theApp = new E2Designer(mainFrame);
            BrowserFrame.getBrowser(ms_conn, mainFrame.getObjectStore(), host);

            OSLoadSaveHelper.createSaveLog();

            /* check on feature support - if there is error text, then there
             * was a problem loading the featureset from the server.
             */
            final String featureSupportErr = FeatureSet.getFeatureSet().getErrorText();
            assert featureSupportErr == null;
         }
         catch(MissingResourceException e)
         {
            throw new AssertionError(e);
         }
         catch (Throwable e)
         {
            // we're on our way out, so notify user
            System.out.println( "Unexpected exception in main" );
            e.printStackTrace();
         }
      }

   private static void configureLookAndFeel() throws InstantiationException, IllegalAccessException, ClassNotFoundException, UnsupportedLookAndFeelException
   {
      final String strLnFClass = UIManager.getSystemLookAndFeelClassName();
      final LookAndFeel lnf =
            (LookAndFeel) Class.forName(strLnFClass).newInstance();
      UIManager.setLookAndFeel(lnf);
   }

   /**
    * @param strUID
    * @param strPort
    * @param strProtocol
    */
   private static void setupPortAndProtocol(final String strUID, final String strPort, final String strProtocol)
   {
      if (strProtocol!=null
          && strProtocol.trim().equalsIgnoreCase("https"))
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
   }

   /**
    * Current workbench display.
    * @return current workbench display. Never <code>null</code>.
    * Fails if the workbench has not been created yet.
    */
   private static Display getDisplay()
   {
      return PlatformUI.getWorkbench().getDisplay();
   }
   
   public static final String CX_FEATURE = "ContentExplorer";
   
   public static boolean ms_initializeRequired;
}

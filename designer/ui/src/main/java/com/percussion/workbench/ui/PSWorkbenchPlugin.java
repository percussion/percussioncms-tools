/******************************************************************************
 *
 * [ PSWorkbenchPlugin.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.PSXmlApplicationConverter;
import com.percussion.client.IPSCoreListener;
import com.percussion.client.IPSinvalidConnectionListener;
import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSLogonStateChangedEvent;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.error.PSClientException;
import com.percussion.client.preferences.PSRhythmyxPreferences;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.client.proxies.PSXmlApplicationConverterProvider;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.services.security.IPSAcl;
import com.percussion.util.IOTools;
import com.percussion.utils.xml.PSEntityResolver;
import com.percussion.workbench.config.PSUiConfigManager;
import com.percussion.workbench.ui.legacy.PSLegacyInitialzer;
import com.percussion.workbench.ui.preferences.PSRhythmyxPreferencesPage;
import com.percussion.workbench.ui.preferences.PSSecurityPreferencesPage;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSResourceLoader;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.xml.serialization.PSObjectSerializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The main plugin class for the Rhythmyx workbench.
 */
public class PSWorkbenchPlugin extends AbstractUIPlugin implements
   IPSCoreListener, IPSinvalidConnectionListener
{
   /**
    * The constructor.
    */
   public PSWorkbenchPlugin() 
   {
      ms_plugin = this;
      // allows running the workbench w/o any server connections
      PropertyConfigurator.configureAndWatch("log4j.properties"); //$NON-NLS-1$
      PSLegacyInitialzer.ms_initializeRequired = true;
      // Initialize the singleton with the class registry from this package
      PSObjectSerializer.getInstance().registerBeanClasses(
         PSWorkbenchPlugin.class);
   }

   /**
    * This method is called upon plug-in activation
    */
   @Override
   public void start(BundleContext context) throws Exception
   {
      super.start(context);
      ResourceBundle bundle = ResourceBundle.getBundle(
            "com.percussion.workbench.ui.PSErrorMessages", Locale.getDefault()); //$NON-NLS-1$
      PSClientException.registerMessageBundle("com.percussion.workbench.ui", //$NON-NLS-1$
            30400, 30999, bundle);
      PSDlgUtil.useSwtIfPossible();
      maybeInitDefaultConfiguration();
      configureEnityResolver();
      configureWorkbenchResourceLoader();
      loadPreferences();
      PSXmlApplicationConverterProvider.getInstance().setConverter(
            new PSXmlApplicationConverter());
      PSCoreFactory.getInstance().setInvalidConnectionListener(this);
      PSCoreFactory.getInstance().addListener(this);
      // Must happen before logon
      PSCoreFactory.getInstance().setClientSessionId(getClientSessionId());
   }

   /**
    * Is called after {@link #start(BundleContext)} in UI thread,
    * after the display is initialized.
    * Runs initialization requiring user interaction. 
    */
   public void uiStart()
   {
      if (getPreferences().isAutoConnectOnOpen())
      {
         ms_log.debug(
               PSMessages.getString("PSWorkbenchPlugin.autoConnectOn.message") +//$NON-NLS-1$
               PSMessages.getString(
                     "PSWorkbenchPlugin.loggingOntoLastServer.message"));       //$NON-NLS-1$
         maybeLogon(PSUiConfigManager.getInstance().getUserConnections()
               .getDefaultConnection());
      }
      PSCoreFactory.getInstance().setClientSessionId(getClientSessionId());

      // For testing
      try
      {
         PSCoreFactory.getInstance().finishTestingSetup();
      }
      catch (PSUninitializedConnectionException e)
      {
         //probably workbench is not initialized
         //we can excuse this here
      }
   }
   
   /**
    * Logs in to the server with the provided connection information.
    * @param connection the connection to use to connect to the server.
    * The method does nothing if is <code>null</code>. 
    */
   public void maybeLogon(final PSConnectionInfo connection)
   {
      if (connection != null)
      {
         final Display display = Display.getCurrent(); 
         final Thread t = new Thread(new Runnable()
         {
            public void run()
            {
               try
               {
                  PSCoreFactory.getInstance().logon(connection);
               }
               catch (PSUninitializedConnectionException ignore)
               {
                  //probably workbench is not initialized
                  //we can excuse this here
               }
               catch (final Exception e)
               {
                  display.asyncExec(new Runnable()
                  {
                     public void run()
                     {
                        PSWorkbenchPlugin.handleException(
                              null, null, null, e);
                     }
                  });
               }
            }
         });
         t.start();
         PSConnectingNotification.getInstance().open(connection);
      }
   }

   /**
    * Initializes {@link PSResourceLoader}.
    */
   private void configureWorkbenchResourceLoader()
   {
      PSResourceLoader.setRootDir(getRxConfigDir().getParentFile());
   }

   /**
    * Get the id for the client session. Looks up in the preference store first.
    * If one does not exist it will create one, save and return the value.
    * 
    * @return client session id as described above, never <code>null</code> or
    *         empty.
    */
   private String getClientSessionId()
   {
      String clientid = getPreferenceStore().getString(PREFERENCE_KEY_CLIENTID);
      if (StringUtils.isEmpty(clientid))
      {
         // The last session of workbench was closed properly
         // Create a new client sessionid and persist.
         clientid = PSCoreUtils.dummyGuid().longValue() + StringUtils.EMPTY;
         getPreferenceStore().setValue(PREFERENCE_KEY_CLIENTID, clientid);
         savePluginPreferences();
      }
      else
      {
         // use the previously stored one since the closing last session was not
         // normal.
      }
      return clientid;
   }

   /**
    * Set the client session id to empty in the preference store.
    */
   private void clearClientSessionId()
   {
      getPreferenceStore().setValue(PREFERENCE_KEY_CLIENTID, StringUtils.EMPTY);
      savePluginPreferences();
   }


   /**
    * Initializes the FeatureSet for this client. This will create a mock
    * featureset if we are using the test proxies.
    */
   private void initFeatureSet()
   {
      PSCoreFactory factory = PSCoreFactory.getInstance();
      if (factory.isLocalMode())
      {
         FeatureSet.createMockFeatureSet();
      }
      else
      {
         if (factory.getDesignerConnection() != null)
         {
            PSObjectStore os = new PSObjectStore(factory
                  .getDesignerConnection());
            FeatureSet.createFeatureSet(os);
         }
      }
   }

   /**
    * Creates default Workbench configuration if it does not exist yet.
    */
   private void maybeInitDefaultConfiguration()
   {
      if (!isUserConfigurationInitialized())
      {
         try
         {
            IOTools.copyToDir(loadDefaultConfigDir(), getStateLocation()
                  .toFile());
         }
         catch (Exception e)
         {
            ms_log.error("Failed to initialize default configuration", e); //$NON-NLS-1$
         }
      }
   }

   /**
    * Finds default rxconfig configuration directory template in plug-in
    * resources and returns its location in the file system.
    * 
    * @throws IOException if loading failed.
    */
   private File loadDefaultConfigDir() throws IOException
   {
      final File dir = findDirectory("default-config"); //$NON-NLS-1$
      return new File(dir, RXCONFIG_DIR);
   }

   /**
    * Returns <code>true</code> if the plug-in configuration is initialized.
    */
   private boolean isUserConfigurationInitialized()
   {
      return getRxConfigDir().exists();
   }

   /**
    * Returns location of main configuration directory in local file system.
    */
   private File getRxConfigDir()
   {
      return getStateLocation().append(RXCONFIG_DIR).toFile();
   }

   /**
    * Initializes entity resolver.
    */
   private void configureEnityResolver()
   {
      try
      {
         PSEntityResolver.setResolutionHome(loadDtdDirectory().getParentFile());
      }
      catch (Exception e)
      {
         ms_log.error("Failed to resolve DTD plugin subdirectory", e); //$NON-NLS-1$
      }
   }

   /**
    * Loads DTD directory from plug-in resources and returns its location in the
    * file system.
    */
   private File loadDtdDirectory() throws IOException
   {
      return findDirectory("DTD"); //$NON-NLS-1$
   }

   /**
    * This method is called when the plug-in is stopped
    */
   @Override
   public void stop(BundleContext context) throws Exception
   {
      super.stop(context);
      ms_plugin = null;
      // Proper shut down of the workbench, clear the client sessionid
      clearClientSessionId();
      // override the eclipse default for the known options
      PlatformUI.getPreferenceStore().setValue(
         IWorkbenchPreferenceConstants.CLOSE_EDITORS_ON_EXIT, true);
      savePluginPreferences();
   }

   /**
    * Finds specified directory from plug-in resources and returns its location
    * in the file system.
    */
   public File findDirectory(String dirpath) throws IOException
   {
      final Path path = new Path(dirpath);
      final URL url = Platform.find(getBundle(), path);
      final URL localUrl = Platform.asLocalURL(url);
      return new File(localUrl.getFile());
   }

   /**
    * Get the eclipse project associated with the current connection.
    * 
    * @return Returns the project, can be <code>null</code> if no connection
    *         is established.
    */
   public IProject getProject()
   {
      return m_project;
   }

   /**
    * Returns the shared instance.
    */
   public static PSWorkbenchPlugin getDefault()
   {
      return ms_plugin;
   }

   /**
    * Convenience method that calls
    * {@link #log(String, Throwable) log(msg, <code>null</code>)}.
    * 
    * @param msg If <code>null</code> or empty, returns immediately.
    */
   public void log(String msg)
   {
      log(msg, null);
   }

   /**
    * Convenience method that builds a {@link Status} object from the supplied
    * params and logs the resulting object to the main logging mechanism as an
    * INFO level message, with a plugin specific code of 0.
    * 
    * @param msg If <code>null</code> or empty, returns immediately.
    * 
    * @param e May be <code>null</code>.
    */
   public void log(String msg, Throwable e)
   {
      if (StringUtils.isEmpty(msg))
         return;
      getLog().log(
            new Status(Status.INFO, getBundle().getSymbolicName(), 0, msg, e));
   }

   /**
    * Pops up a dialog just like the error dialog, except a warning icon is
    * displayed instead. See that
    * {@link #handleException(String, String, String, Throwable)} for a
    * description of the params.
    */
   public static void displayWarning(String context, String title,
         String message, Throwable... errors)
   {
      // todo - implement to doc w/ error dialog
      handleException(context, title, message, errors);
   }

   /**
    * Displays an error dialog and logs the exception information when multiple
    * errors have occurred in a single operation.
    * <p>
    * This method is designed to handle failures from methods that operate on
    * more than 1 object at a time. Generally, these methods use the
    * {@link com.percussion.client.PSMultiOperationException}. The results in
    * such an exception can be passed directly to this method.
    * 
    * @param context This text is written with the log message. It is not
    *           presented in the dialog shown to the user. Therefore, it does
    *           not need to be localized. It should be a brief (couple of words)
    *           string that identifies what was happening when the error
    *           occurred, e.g. "object delete". If not supplied, "&lt;Unknown
    *           context&gt;" is used.
    * 
    * @param title This is displayed as the title of the error dialog. If not
    *           provided, a default is used.
    * 
    * @param message This is displayed as the main message of the dialog. If not
    *           provided, a default is used.
    * 
    * @param objs An array of objects that must contain 1 or more
    *           {@link Throwable}s. Non throwables are skipped. The resulting
    *           message indicates how many successes vs failures there were.
    *           Never <code>null</code>, must contain at least 1 throwable.
    */
   public static void handleException(
         String context, String title, String message, Object[] objs)
   {
      handleException(context, title, message, objs, false);
   }

   /**
    * Displays an error dialog and logs the exception information when multiple
    * errors have occurred in a single operation.
    * <p>
    * This method is designed to handle failures from methods that operate on
    * more than 1 object at a time. Generally, these methods use the
    * {@link com.percussion.client.PSMultiOperationException}. The results in
    * such an exception can be passed directly to this method.
    * 
    * @param context This text is written with the log message. It is not
    *           presented in the dialog shown to the user. Therefore, it does
    *           not need to be localized. It should be a brief (couple of words)
    *           string that identifies what was happening when the error
    *           occurred, e.g. "object delete". If not supplied, "&lt;Unknown
    *           context&gt;" is used.
    * 
    * @param title This is displayed as the title of the error dialog. If not
    *           provided, a default is used.
    * 
    * @param message This is displayed as the main message of the dialog. If not
    *           provided, a default is used.
    * 
    * @param objs An array of objects that must contain 1 or more
    *           {@link Throwable}s. Non throwables are skipped. The resulting
    *           indicates how many successes vs failures there were
    *           (non-throwables are considered success.) Never <code>null</code>,
    *           must contain at least 1 throwable.
    * 
    * @param isWarning flag that if <code>true</code> indicates that the
    *           message being displayed is a warning.
    */
   @SuppressWarnings("unused") //$NON-NLS-1$
   public static void handleException(String context, String title, //$NON-NLS-1$
         String message, Object[] objs, boolean isWarning)
   {
      if (null == objs)
      {
         throw new IllegalArgumentException("objs cannot be null"); //$NON-NLS-1$
      }      
      final Collection<Throwable> errors = new ArrayList<Throwable>();
      for (Object o : objs)
      {
         if (o instanceof Throwable)
         {
            errors.add((Throwable) o);
         }
      }

      if (errors.isEmpty())
      {
         throw new IllegalArgumentException(
            "Supplied array must contain at least 1 Throwable."); //$NON-NLS-1$
      }
      
      MultiStatus status = new MultiStatus(getPluginId(), IStatus.ERROR,
         StringUtils.defaultString(message, "Error"), null);
      
      for (Throwable t : errors)
      {
         IStatus child = new Status(IStatus.ERROR, getPluginId(), 0, "", t); //$NON-NLS-1$
         status.add(child);
      }
      getDefault().getLog().log(status);
      new PSErrorDialog(PSUiUtils.getShell(), title, message, errors).open();
      

      
   }

   /**
    * The fully qualified name for this plugin.
    */
   public static String getPluginId()
   {
      return getDefault() == null && runsUnderUnitTest()
            ? "pluginId" : getDefault().getBundle().getSymbolicName();
   }

   /**
    * Returns <code>true</code> if the code runs under unit test.
    */
   private static boolean runsUnderUnitTest()
   {
      return ExceptionUtils.getFullStackTrace(new Exception()).contains("junit");
   }

   /**
    * Displays an error dialog and logs the exception information when a single
    * error has occurred.
    * 
    * @param context If <code>null</code> or empty, "&lt;Unknown context&gt;"
    *           is used.
    * 
    * @param title If <code>null</code> or empty, a default is supplied.
    * 
    * @param message If <code>null</code> or empty, the text from the
    *           exception is used. If that is empty, the base name of the class
    *           is used.
    * 
    * @param t Never <code>null</code>. todo - doc
    */
   public static void handleException(String context, String title,
         String message, Throwable t)
   {      
         handleException(context, title, message, new Throwable[] {t});      
   }
   

   /**
    * Access method for Rhythmyx preferences object that is initialized in the
    * {@link #start(BundleContext)} method and updated every time preferences
    * are saved using {@link #savePreferences(PSRhythmyxPreferences)}.
    * 
    * @return preferences loaded from the preference store, never
    *         <code>null</code>.
    */
   public PSRhythmyxPreferences getPreferences()
   {
      return m_preferences;
   }

   /**
    * Access method for default ACL object that is initialized in the
    * {@link #start(BundleContext)} method and updated every time preferences
    * are saved using {@link #savePreferences(PSRhythmyxPreferences)}.
    * 
    * @return preferences loaded from the preference store, never
    *         <code>null</code>.
    */
   public IPSAcl getDefaultAcl()
   {
      return m_defaultAcl;
   }

   /**
    * Load Rhythmyx general preferences from preference store. Error message is
    * logged and default preferences will be created if load fails for any
    * reason.
    */
   private void loadPreferences()
   {
      String str = getPreferenceStore().getString(
            PSRhythmyxPreferencesPage.getPreferenceName());
      if (str != null && str.length() > 0)
      {
         XStream xs = new XStream(new DomDriver());
         xs.allowTypesByWildcard(new String[] {
                 "com.percussion.**"
         });
         try
         {
            Object obj = xs.fromXML(str);
            m_preferences = (PSRhythmyxPreferences) obj;
         }
         catch (Throwable e)
         {
            //Can happen due to a bug in XStream which is fixed in a SNAPSHOT jar.
            m_preferences = null;
         }
      }
      if (m_preferences == null)
      {
         ms_log
            .info(PSMessages
               .getString("PSWorkbenchPlugin.couldNotLoadPreferences.error.message")); //$NON-NLS-1$
         ms_log.info(PSMessages
            .getString("PSWorkbenchPlugin.loadingDefaultPrefs.info.message")); //$NON-NLS-1$
         m_preferences = new PSRhythmyxPreferences();
      }
      
      if (PSResourceLoader
            .checkUpgradeResourceFileExists(ms_upgradeResourceFile))
      {
         m_preferences.setShowLegacyInterfacesForExtns(true);
         savePreferences(m_preferences);
      }
   }

   /**
    * Load default ACL from preference store. A default (for default) will be
    * created if load fails for any reason.
    */
   private void loadDefaultAcl()
   {
      String str = getPreferenceStore().getString(
         PSSecurityPreferencesPage.getPreferenceName());
      if (str != null && str.length() > 0)
      {
         XStream xs = new XStream(new DomDriver());
         xs.allowTypesByWildcard(new String[] {
                 "com.percussion.**"
         });
         try
         {
            Object obj = xs.fromXML(str);
            m_defaultAcl = (IPSAcl) obj;
         }
         catch (Throwable e)
         {
            //Can happen due to a bug in XStream which is fixed in a SNAPSHOT jar.
            m_defaultAcl = null;
         }
      }
      if (m_defaultAcl == null)
      {
         ms_log
            .info(PSMessages
               .getString("PSWorkbenchPlugin.couldNotLoadDefaultAclError.info.message")); //$NON-NLS-1$
         ms_log.info(PSMessages
            .getString("PSWorkbenchPlugin.usingNewDefaultAcl.info.message")); //$NON-NLS-1$
         m_defaultAcl = PSSecurityUtils.createNewAcl();
      }
   }
   
   /**
    * Save preferences to preference store. Typically called by preference
    * editor user interface. If save is successful the local reference to the
    * references is also updated.
    * 
    * @param prefs preference object to save to store, must not be
    * <code>null</code>.
    */
   public void savePreferences(PSRhythmyxPreferences prefs)
   {
      if (prefs == null)
      {
         throw new IllegalArgumentException("prefs must not be null"); //$NON-NLS-1$
      }
      savePreferenceStoreObject(PSRhythmyxPreferencesPage.getPreferenceName(),
         prefs);
      m_preferences = prefs;
   }

   /**
    * Save default ACL to preference store. Typically called by preference
    * editor user interface. If save is successful the local reference to the
    * default ACL is also updated.
    * 
    * @param defaultAcl default ACL object to save to store, must not be
    *           <code>null</code>.
    */
   public void saveDefaultAcl(IPSAcl defaultAcl)
   {
      if (defaultAcl == null)
      {
         throw new IllegalArgumentException("defaultAcl must not be null"); //$NON-NLS-1$
      }
      savePreferenceStoreObject(PSSecurityPreferencesPage.getPreferenceName(),
         defaultAcl);
      m_defaultAcl = defaultAcl;
   }
   
   /**
    * Save the supplied object with supplied key in the workbench preference
    * store. Uses Xstream to serialze the object to string.
    * 
    * @param prefKey preference key must not be <code>null</code> or empty.
    * @param obj the object to serialize string an dthen save to store, must not
    * be <code>null</code>.
    */
   public void savePreferenceStoreObject(String prefKey, Object obj)
   {
      if (prefKey == null || prefKey.length() == 0)
      {
         throw new IllegalArgumentException("prefKey must not be null or empty");
      }
      if (obj == null)
      {
         throw new IllegalArgumentException("obj to serialize must not be null");
      }
      XStream xs = new XStream(new DomDriver());
      xs.allowTypesByWildcard(new String[] {
              "com.percussion.**"
      });
      String str = xs.toXML(obj);
      getPreferenceStore().setValue(prefKey, str);
   }

   /**
    * Returns an image descriptor for the image file at the given plug-in
    * relative path.
    * 
    * @param path the path
    * @return the image descriptor
    */
   public static ImageDescriptor getImageDescriptor(String path)
   {
      return AbstractUIPlugin.imageDescriptorFromPlugin(getPluginId(), path);
   }

   /**
    * A static instance is kept in support of {@link #getDefault()}. Set in
    * ctor, then never changed.
    */
   private static PSWorkbenchPlugin ms_plugin;

   /**
    * Configuration directory name.
    */
   private static final String RXCONFIG_DIR = "rxconfig"; //$NON-NLS-1$

   /**
    * Reference to Rhythmyx preferences object.
    * 
    * @see #getPreferences()
    */
   private PSRhythmyxPreferences m_preferences = null;

   /**
    * Reference to default ACL configured as preference.
    * 
    * @see #getPreferences()
    */
   private IPSAcl m_defaultAcl = null;

   /**
    * Workspace project associated with the current connection. Initially
    * <code>null</code>, this is created by the connection listener invoked
    * by the core factory.
    */
   private IProject m_project;

   /**
    * Preference key to store and retrieve the client session id.
    */
   private static final String PREFERENCE_KEY_CLIENTID = "com.percussion.workbench.ui.clientid"; //$NON-NLS-1$

   /**
    * Logger for this class. Never <code>null</code>.
    */
   private static final Logger ms_log = LogManager
         .getLogger(PSWorkbenchPlugin.class);

   /**
    * A resource file, if this exists, WB treats this as an upgraded 
    * installation and not a fresh installation.
    */
   private static final String ms_upgradeResourceFile = "psUpgrade";
   
   public void logonStateChanged(PSLogonStateChangedEvent event)
   {
      if (event.getEventType() == PSLogonStateChangedEvent.LogonStateEvents.LOGON)
      {
         getWorkbench().getDisplay().asyncExec(new Runnable()
         {
            public void run()
            {
               onLoggedIn();
            }
         });
      }
   }

   /**
    * Is called when the workbench is logged in.
    * Is called in the UI thread.
    */
   private void onLoggedIn()
   {
      loadDefaultAcl();
      initFeatureSet();

      // Create or lookup associated workspace project
      IWorkspace ws = ResourcesPlugin.getWorkspace();
      IWorkspaceRoot root = ws.getRoot();
      PSConnectionInfo info = PSCoreFactory.getInstance()
            .getConnectionInfo();
      if (info != null)
      {
         String projectName = "Rx_" + info.getName() + "_" //$NON-NLS-1$ //$NON-NLS-2$
               + info.getPort();
         
         // Remove any "bad" characters - i.e. chars not allowed in filenames
         projectName = projectName.replaceAll(
               "[\\p{Space}|\\\\/:?\\*\"><!-]", "_");

         m_project = findProject(root, projectName);
         IProgressMonitor monitor = new NullProgressMonitor();
         try
         {
            if (!m_project.exists())
               m_project.create(monitor);
            else
               m_project.refreshLocal(IResource.DEPTH_INFINITE, null);
               
            if (!m_project.isOpen())
               m_project.open(monitor);
         }
         catch (CoreException e)
         {
            log("Problem creating project " + projectName, e); //$NON-NLS-1$
         }
      }
   }

   /**
    * Find the project with supplied name ignoring the case.
    * 
    * @param root workspace root, assumed not <code>null</code>.
    * @param projectName name of the project to find, assumed not
    * <code>null</code> or empty.
    * @return matching project if found or new one with the supplied name Never
    * <code>null</code>.
    */
   private IProject findProject(IWorkspaceRoot root, String projectName)
   {
      IProject[] projects = root.getProjects();
      for (IProject project : projects)
      {
         if (project.getName().equalsIgnoreCase(projectName))
            return project;
      }
      return root.getProject(projectName);
   }


   /**
    * {@inheritDoc}
    * Can be called from a non-UI event thread.
    */
   public PSConnectionInfo correctConnection(final PSConnectionInfo lastConn,
      final Object error, final List<String> locales) 
   {
      final IWorkbench wb;
      try
      {
         wb = getWorkbench();
      }
      catch (IllegalStateException e)
      {
         // Workbench is not initialized, cancel further attempts
         return null;
      }

      final PSConnectionInfo[] connectionContainer = new PSConnectionInfo[1];
      getWorkbench().getDisplay().syncExec(new Runnable()
      {
         public void run()
         {
            final PSConnectionDialogManager connMgr =
                  new PSConnectionDialogManager(wb);
            connectionContainer[0] = connMgr.connect(lastConn, error, 
                  "PSWorkbenchPlugin.connectDialog.closeButton.label", locales);
            connMgr.dispose();
         }
      });
      
      final PSConnectionInfo connection = connectionContainer[0];

      if (connection != null)
      {
         // open the connection progress notification window
         getWorkbench().getDisplay().asyncExec(new Runnable()
         {
            public void run()
            {
               PSConnectingNotification.getInstance().open(connection);
            }
         });
      }
      return connection;
   }
}

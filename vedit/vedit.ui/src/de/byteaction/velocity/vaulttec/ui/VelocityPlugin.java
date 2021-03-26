package de.byteaction.velocity.vaulttec.ui;

import de.byteaction.velocity.editor.compare.CompareDialog;
import de.byteaction.velocity.preferences.GeneralPreferencePage;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityEditorEnvironment;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class VelocityPlugin extends AbstractUIPlugin
{
    /**
     * The id of the Velocity plugin (value
     * <code>"de.byteaction.velocity.vaulttec.ui"</code>).
     */
    public static final String    PLUGIN_ID       = "de.byteaction.velocity.vaulttec.ui";
    public static boolean         isBrowserSupported;
    /** The shared instance. */
    private static VelocityPlugin fPlugin;
    private static final String   RESOURCE_NAME   = PLUGIN_ID + ".messages";
    public static String          dirPath;
    private ResourceBundle        fResourceBundle;

    public boolean isAutoCompletionEnabled()
    {
        return getDefault().getPreferenceStore().getBoolean(GeneralPreferencePage.P_Completion);
    }

    public boolean isUppercaseEnabled()
    {
        return getDefault().getPreferenceStore().getBoolean(GeneralPreferencePage.P_CASE);
    }
   
    public VelocityPlugin()
    {
        fPlugin = this;
        isBrowserSupported = true;
        Browser br = null;
        try
        {
            br = new Browser(getActiveWorkbenchShell(), SWT.BOLD);
        }
        catch (SWTError e)
        {
            isBrowserSupported = false;
        }
        if (br != null) br = null;
        try
        {
            fResourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
        }
        catch (Exception e)
        {
            log(e);
            fResourceBundle = null;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPluginPreferences()
     */
    @Override
   protected void initializeDefaultPluginPreferences()
    {
        IPreferenceStore aStore = getPreferenceStore();
        aStore.setDefault(IPreferencesConstants.EDITOR_SHOW_SEGMENTS, false);
        aStore.setDefault(IPreferencesConstants.VELOCITY_COUNTER_NAME, "velocityCount");
        aStore.setDefault(IPreferencesConstants.VELOCITY_USER_DIRECTIVES, "");
        VelocityColorProvider.initializeDefaults(aStore);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent anEvent)
    {
        String prop = anEvent.getProperty();
        VelocityEditorEnvironment.getColorProvider().handlePreferenceStoreChanged(anEvent);
        if (prop.equals(IPreferencesConstants.VELOCITY_USER_DIRECTIVES) || prop.equals(IPreferencesConstants.LIBRARY_LIST))
        {
            VelocityEditorEnvironment.createVelocityParser();
        }
    }

    /**
     * Returns Velocity user directives.
     */
    public static List getVelocityUserDirectives()
    {
        IPreferenceStore store = getDefault().getPreferenceStore();
        String directives = store.getString(IPreferencesConstants.VELOCITY_USER_DIRECTIVES);
        StringTokenizer st = new StringTokenizer(directives, ",\n\r");
        ArrayList list = new ArrayList();
        while (st.hasMoreElements())
        {
            list.add(st.nextElement());
        }
        return list;
    }

    /**
     * Returns the shared instance.
     */
    public static VelocityPlugin getDefault()
    {
        return fPlugin;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle()
    {
        return fResourceBundle;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static Shell getActiveWorkbenchShell()
    {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        return ((window != null) ? window.getShell() : null);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static String getPluginId()
    {
        return getDefault().getBundle().getSymbolicName();
    }

    /**
     * Get the installation directory as a url. The url can be used to load
     * file from the installation directory
     * 
     * @return the installation url, never <code>null</code>
     */
    public static URL getInstallURL()
    {
        return getDefault().getBundle().getEntry("/");
    }

    /**
     * Get the installation path
     * @return the installation path, never <code>null</code> or empty
     */
    public static String getInstallPath()
    {
        URL url = VelocityPlugin.getDefault().getBundle().getEntry("/");
        File rval = null;
        try
        {
            URL loc = Platform.resolve(url);
            rval = new File(loc.toURI());
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rval.getAbsolutePath();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aStatus
     *            DOCUMENT ME!
     */
    public static void log(IStatus aStatus)
    {
        getDefault().getLog().log(aStatus);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aThrowable
     *            DOCUMENT ME!
     */
    public static void log(Throwable aThrowable)
    {
        log(new Status(IStatus.ERROR, getPluginId(), IStatus.OK, getMessage("VelocityPlugin.internal_error"), aThrowable));
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aMessage
     *            DOCUMENT ME!
     */
    public static void logErrorMessage(String aMessage)
    {
        log(new Status(IStatus.ERROR, getPluginId(), IStatus.OK, aMessage, null));
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aMessage
     *            DOCUMENT ME!
     * @param aStatus
     *            DOCUMENT ME!
     */
    public static void logErrorStatus(String aMessage, IStatus aStatus)
    {
        if (aStatus == null)
        {
            logErrorMessage(aMessage);
        } else
        {
            MultiStatus multi = new MultiStatus(getPluginId(), IStatus.OK, aMessage, null);
            multi.add(aStatus);
            log(multi);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static boolean isDebug()
    {
        return getDefault().isDebugging();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param anOption
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static boolean isDebug(String anOption)
    {
        boolean debug;
        if (isDebug())
        {
            String value = Platform.getDebugOption(anOption);
            debug = (((value != null) && value.equalsIgnoreCase("true")) ? true : false);
        } else
        {
            debug = false;
        }
        return debug;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'aKey' if not
     * found.
     */
    public static String getMessage(String aKey)
    {
        String bundleString;
        ResourceBundle bundle = getDefault().getResourceBundle();
        if (bundle != null)
        {
            try
            {
                bundleString = bundle.getString(aKey);
            }
            catch (MissingResourceException e)
            {
                log(e);
                bundleString = "!" + aKey + "!";
            }
        } else
        {
            bundleString = "!" + aKey + "!";
        }
        return bundleString;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aKey
     *            DOCUMENT ME!
     * @param anArg
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static String getFormattedMessage(String aKey, String anArg)
    {
        return getFormattedMessage(aKey, new String[] { anArg });
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aKey
     *            DOCUMENT ME!
     * @param anArgs
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static String getFormattedMessage(String aKey, Object[] anArgs)
    {
        return MessageFormat.format(getMessage(aKey), anArgs);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param key
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = getDefault().getResourceBundle();
        try
        {
            return bundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            return key;
        }
    }

    public int openCompareDialog(final CompareEditorInput input, String error)
    {
        int i = 0;
        if (compareResultOK(input))
        {
            CompareDialog dialog = new CompareDialog(VelocityPlugin.getActiveWorkbenchShell(), input, error);
            i = dialog.open();
        }
        return i;
    }

    private boolean compareResultOK(CompareEditorInput input)
    {
        final Shell shell = VelocityPlugin.getActiveWorkbenchShell();
        try
        {
            // run operation in separate thread and make it canceable
            PlatformUI.getWorkbench().getProgressService().run(true, true, input);
            String message = input.getMessage();
            if (message != null)
            {
                MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), message); //$NON-NLS-1$
                return false;
            }
            if (input.getCompareResult() == null)
            {
                MessageDialog.openInformation(shell, Utilities.getString("CompareUIPlugin.dialogTitle"), Utilities.getString("CompareUIPlugin.noDifferences")); //$NON-NLS-2$ //$NON-NLS-1$
                return false;
            }
            return true;
        }
        catch (InterruptedException x)
        {
            // cancelled by user
        }
        catch (InvocationTargetException x)
        {
            MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), x.getTargetException().getMessage()); //$NON-NLS-1$
        }
        return false;
    }
}
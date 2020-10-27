/******************************************************************************
*
* [ PSHelpManager.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.help;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PSHelpManager
{

   /**
    * Construct new Help manager
    * @param helpProvider cannot be <code>null</code>.
    * @param rootControl cannot be <code>null</code>.
    */
   public PSHelpManager(final IPSHelpProvider helpProvider, final Control rootControl)
   {
      if(helpProvider == null)
         throw new IllegalArgumentException("helpProvider cannot be null."); //$NON-NLS-1$
      if(rootControl == null)
         throw new IllegalArgumentException("rootControl cannot be null."); //$NON-NLS-1$
      m_helpProvider = helpProvider;
      m_helpListener = new WorkbenchHelpListener();
      registerControls(rootControl);
   }
   
   /**
    * Construct new Help manager
    * @param helpProvider cannot be <code>null</code>.
    * @param tabFolder cannot be <code>null</code>.
    */
   public PSHelpManager(final IPSHelpProvider helpProvider, final CTabFolder tabFolder)
   {
      if(helpProvider == null)
         throw new IllegalArgumentException("helpProvider cannot be null."); //$NON-NLS-1$
      if(tabFolder == null)
         throw new IllegalArgumentException("tabFolder cannot be null."); //$NON-NLS-1$
      m_helpProvider = helpProvider;
      m_helpListener = new WorkbenchHelpListener();
      registerControls(tabFolder);
      
   }
   
   /**
    * Construct new Help manager
    * @param helpProvider cannot be <code>null</code>.
    * @param action cannot be <code>null</code>.
    */
   public PSHelpManager(final IPSHelpProvider helpProvider, Action action)
   {
      if(helpProvider == null)
         throw new IllegalArgumentException("helpProvider cannot be null."); //$NON-NLS-1$
      if(action == null)
         throw new IllegalArgumentException("action cannot be null."); //$NON-NLS-1$
      m_helpProvider = helpProvider;
      m_helpListener = new WorkbenchHelpListener();
      action.setHelpListener(m_helpListener);      
   }
   
   /**
    * Ctor to initialize a help provider without registering
    * any controls.
    * @param helpProvider cannot be <code>null</code>.
    */
   public PSHelpManager(final IPSHelpProvider helpProvider)
   {
      if(helpProvider == null)
         throw new IllegalArgumentException("helpProvider cannot be null."); //$NON-NLS-1$
      m_helpProvider = helpProvider;
      m_helpListener = new WorkbenchHelpListener();
   }
   
   /**
    * Registers all controls, recursing down to get all children.
    * A help listener is added for any non Composite derived controls.
    * @param control cannot be <code>null</code>.
    */
   public void registerControls(final Control control)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null."); //$NON-NLS-1$
      if(control instanceof Tree)
      {
         Tree tree = (Tree)control; 
         tree.addHelpListener(m_helpListener);
         for(Control child : tree.getChildren())
         {
            registerControls(child);
         }
      }
      else if(!(control instanceof PSSortableTable) && control instanceof Composite )
      {
         for(Control c : ((Composite)control).getChildren())
         {
            registerControls(c);
         }
         control.addHelpListener(m_helpListener);
      }      
      else
      {
         control.addHelpListener(m_helpListener);
      }
      
   }
   
   /**
    * Registers all controls in a tab folder, recursing down to get all children.
    * A help listener is added for any non Composite derived controls.
    * @param tabFolder cannot be <code>null</code>.
    */
   public void registerControls(final CTabFolder tabFolder )
   {
      if(tabFolder == null)
         throw new IllegalArgumentException("tabFolder cannot be null."); //$NON-NLS-1$
      for(CTabItem item : tabFolder.getItems())
      {
         registerControls(item.getControl());
      }
   }
   
   /**
    * Displays the help using the key provided by the
    * help provider. No help will be displayed if the key or
    * key mapping can't be located.
    * @param source the control who has focus. Never
    * <code>null</code>.
    */
   private void displayHelp(Object source)
   {
      if(!(source instanceof Control))
         source = null;         
      invokeHelp(m_helpProvider.getHelpKey((Control)source));
   }
   
   /**
    * Allows help to be invoked from a legacy app (Swing) by
    * dispatching the help request call into the SWT display 
    * thread when it is ready.
    * @param key may be <code>null</code> or empty.
    */
   public static void displayHelpFromLegacy(final String key)
   {
      IWorkbench workbench = PSWorkbenchPlugin.getDefault().getWorkbench();
      final Display display = workbench.getDisplay();
      display.asyncExec(new Runnable()
         {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
               invokeHelp(key);               
            }
         
         });
      
   }
   
   /**
    * Displays the help using the key provided by.
    *  No help will be displayed if the key or
    * key mapping can't be located.
    * @param key the help mapping keys. May be 
    * <code>null</code>, or empty.
    */
   private static void invokeHelp(String key)
   {
      if(StringUtils.isBlank(key))
         return;
      //    fixme - remove this debug sysout line
      System.out.println("Help key: " + key); //$NON-NLS-1$
      String path = getHelpPath(key);
      if(StringUtils.isBlank(path))
      {
         PSUiUtils.log(PSMessages.getString("PSHelpManager.message.noKeyFound") + //$NON-NLS-1$
            key);
         if(!key.equals(DEFAULT_HELP_KEY))
            invokeHelp(DEFAULT_HELP_KEY);
      }
      else
      {
         String helpPath = "/" + HELP_PLUGIN_ID + "/" + path; //$NON-NLS-1$ //$NON-NLS-2$
         getHelpSystem().displayHelpResource(helpPath);
      }
   }
   
   
   /**
    * Gets the help path for the specified key from the help key
    * mapping file.
    * @param key cannot be <code>null</code> or empty.
    * @return the help path if found, else <code>null</code>.
    */
   private static String getHelpPath(String key)
   {
      return getKeyMappings().getProperty(key);
   }
   
   /**
    * Retrieves the key mappings from the key mappings properties file
    * and caches it in a static context for this class.
    * @return the key mappings properties, never <code>null</code>,
    * may be empty.
    */
   protected static Properties getKeyMappings()
   {
      if(ms_keyMappings == null)
      {
         PSWorkbenchPlugin plugin = PSWorkbenchPlugin.getDefault();
         Properties mappings = new Properties();
         InputStream is = null;
         try
         {
            
            File defaultConfigDir = plugin.findDirectory(DEFAULT_CONFIG);
            File mappingsFile = new File(defaultConfigDir, MAPPINGS_FILE_PATH );
            is = new FileInputStream(mappingsFile);
            mappings.load(is);
            ms_keyMappings = mappings;
         }
         catch (IOException e)
         {            
            IWorkbench workbench = PSWorkbenchPlugin.getDefault().getWorkbench();
            @SuppressWarnings("unused")
            final Display display = workbench.getDisplay();
            //fixme uncomment this code when the ctx help file is available, also log it
//            display.asyncExec(new Runnable()
//            {
//               @SuppressWarnings("synthetic-access")
//               public void run()
//               {
//                  new PSErrorDialog(PSUiUtils.getShell(),
//                     PSMessages.getString("PSHelpManager.error.noMappingFile")).open(); //$NON-NLS-1$               
//               }
//            
//            });
            
            ms_keyMappings = mappings;
            return mappings;
         }
         finally
         {
            try
            {
               if(is != null)
                  is.close();
            }
            catch(IOException ignore){}
         }
      }
      return ms_keyMappings;
   }
   
   /**
    * A method to allow the preloading of the help mapping
    * keys to occur in a separate thread.
    */
   public static void preloadKeyMappings()
   {
      Thread thread = new Thread(new Runnable()
         {
            public void run()
            {
               getKeyMappings();               
            }         
         });
      thread.start();
   }
   
   /**
    * Helper method to retrieve the workbench's help
    * system.
    * @return the help system. Never <code>null</code>
    */
   public static IWorkbenchHelpSystem getHelpSystem()
   {
      PSWorkbenchPlugin plugin = PSWorkbenchPlugin.getDefault();
      IWorkbench workbench = plugin.getWorkbench();
      return workbench.getHelpSystem();
   }
   
   /**
    * Help listener that will invoke the display help method
    * of the manager.
    */
   class WorkbenchHelpListener implements HelpListener
   {

      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public void helpRequested(HelpEvent e)
      {
         displayHelp(e.getSource());         
      }
      
   }
   
   /**
    * The help provider, initialized in the ctor,
    * never <Code>null</code> after that.
    */
   private IPSHelpProvider m_helpProvider;
   
   /**
    * Cache of key mappings, initialized the first time
    * that {@link #getKeyMappings()} is called. Never
    * <code>null</code> after that, may be empty.
    */
   private static Properties ms_keyMappings;
   
   /**
    * The help listener for this instance of the
    * help manager. Initialized in the ctor,
    * never <Code>null</code> after that.
    */
   private HelpListener m_helpListener;
   
   /**
    * The help plug-in's id
    */
   public static final String HELP_PLUGIN_ID = "com.percussion.doc.help"; //$NON-NLS-1$
   
   /**
    * The help plugin directory
    */
   public static final String HELP_PLUGIN_DIR = "com.percussion.doc.workbench"; //$NON-NLS-1$
   
   private static final String DEFAULT_CONFIG = "default-config"; //$NON-NLS-1$
   private static final String MAPPINGS_FILE_PATH = 
      "/rxconfig/Workbench/WorkbenchHelpMappings.properties"; //$NON-NLS-1$
   
   private static final String DEFAULT_HELP_KEY = "default"; //$NON-NLS-1$
   

}

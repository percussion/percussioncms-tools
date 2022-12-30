/******************************************************************************
 *
 * [ UIMainFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSVersionConflictException;
import com.percussion.error.PSNonUniqueException;
import com.percussion.error.PSNotFoundException;
import com.percussion.error.PSNotLockedException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.PSEditorRegistry;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditor;
import com.percussion.workbench.ui.legacy.AwtSwtModalDialogBridge;
import org.apache.commons.collections.map.ReferenceIdentityMap;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;


/**
 * The main frame window for the application. Provides menu, toolbar buttons,
 * and status bar. The toolbar and status bar's visibility can be toggled by
 * the user. The frame maintains a list of actions that are currently in the
 * menu or toolbar.
 * <p>
 * Some std menu items are supported by the main frame, but others will be grayed
 * unless the currently active window enables them by supplying an action
 * handler. These include such items as 'Save' and editing operations such as
 * 'Cut', 'Copy' and 'Paste'. When a window loses activation, all menu items it
 * had previously set handlers for will be disabled.
 * <p>
 * <b>Attention!</b> When legacy UI is moved to Eclipse plugin this frame is not
 * shown anymore 
 * and is used only as utility class. Due to usage of global variables in
 * E2Designer logic it is impossible to use legacy UI in Eclipse plug-in as-is
 * because Eclipse can have more than one set of UI elements for different windows
 * but legacy UI assumes that there is one global set of UI elements and accesses
 * these elements through global classes like this one.
 * To handle this problem we decided to limit usage of legacy UI to one set of
 * UI elements. If user attemts to open legacy UI e.g. in other Eclipse window
 * we will fail the operation with notification that the operation is not 
 * supported.
 * Before using legacy UI call method {@link #validatePlatform(IWorkbenchSite)}
 * to run the validation.
 * <p>See SWT/AWT integration conventions about how to integrate UI</p>
 * </p>Classes embedded in SWT pages and as result which should use SWT dialogs 
 * to pop-up messages etc.:
 * <table>
 * <tr><td>UIAppFrame</td></tr>
 * <tr><td>UIPipeFrame</td></tr>
 * </table>
 * </p>    
 */
public class UIMainFrame extends JFrame implements IConnectionSource
{
   /**
    * The class logger.
    */
   private final static Logger ms_log = LogManager.getLogger(UIMainFrame.class);

   // constructors
   /**
    * Initializes the main window and connects to the object store.
    * The passed in params are stored for later use because they cannot be
    * extracted back from the object store.
    *
    * @param server the name of the E2 server that we are connecting to,
    * must be a non-empty string
    *
    * @param conn A valid connection to the E2Server we are connecting to
    *
    * @throws PSServerException if strServer can't be found
    * @throws PSAuthorizationException If the conn doesn't have access to the
    * object store.
    * @throws PSAuthenticationFailedException If the login to the server failed
    * the verification of the credentials used.
    */
   public UIMainFrame(PSDesignerConnection conn, String server)
      throws PSServerException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (null == conn || null == server || 0 == server.length())
         throw new IllegalArgumentException(
            "Connection or server was null/empty");

      // create the user config object
      UserConfig.createConfig( getObjectStore() );

      SecurityProviderMetaData.initialize( this );
      FeatureSet.createFeatureSet(getObjectStore());
      /* set this so the objectstore has backwards compatibility support when
       * connecting to servers older than this wb
       */
      PSComponent.setFeatureSet(getObjectStore().getSupportedFeatureSet());

      // delegate most of the work to an initialization method
      init();

      // initialize the AWT/SWT bridge for the current display
      getDisplay().syncExec(new Runnable()
      {
         public void run()
         {
            m_awtSwtBridge = new AwtSwtModalDialogBridge(
               getWorkbench().getActiveWorkbenchWindow().getShell());
         }
      });
   }

   /**
    * Loads help. UI handling is so complicated because in case of error
    * PSJavaHepl shows modal Swing dialog we don't have access to, so we
    * have to lock UI during the whole call. 
    */
   @SuppressWarnings("unused")
   private void loadHelp(final String helpSetURL)
   {
      final AwtSwtModalDialogBridge bridge =
         new AwtSwtModalDialogBridge(
               getWorkbench().getActiveWorkbenchWindow().getShell());
      // Andriy: another hack - we asynchroniously delay help loading
      // to eliminate following situation:
      // if this code is called during loading application editor
      // and modal Swing error dialog pops up, Swing portion of the application
      // editor is left locked up after it is dismissed.
      // This affects only to the first open application editor.
      // I suspect that this happens because the editor performs initialization
      // of Swing components while the modal dialog is shown it can't correctly
      // handle dismissal of the dialog. 
      new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               sleep(DateUtils.MILLIS_PER_SECOND * 6);
            }
            catch (InterruptedException e) {}
            SwingUtilities.invokeLater(new Runnable()
                  {
                     public void run()
                     {
                        try
                        {
                           lockUiAndLoadHelp(helpSetURL, bridge);
                        }
                        catch (Exception e)
                        {
                           PSDlgUtil.showError(e);
                        }
                     }
                  });
         }
      }.start();
   }

   /**
    * Uses provided bridge class to lock UI while loading help.
    */
   private void lockUiAndLoadHelp(final String helpSetURL,
         final AwtSwtModalDialogBridge bridge) throws InvocationTargetException
   {
      bridge.lockSWTFor(new Runnable()
      {
         public void run()
         {
            PSJavaHelp.getInstance().setHelpSet(helpSetURL,
                  "com.percussion.E2Designer.helptopicmapping");
         }
      });
   }

   // properties
   public String getE2Server( )
   {
      return( getLoginProperties().getProperty( PSDesignerConnection.PROPERTY_HOST ));
   }

   /**
    * Returns a properties object that contains the server, port, userid and pw
    * that was used to login. To retrieve the properties, use the keys defined
    * in PSDesignerConnection (e.g. PROPERTY_HOST for the server).
    *
    * @return the main frames Properties object that contains the login values.
    * Do not modify the returned object, it should be treated read/only.
    *
    * TODOph: This should return a read/only object. Create a ReadOnlyProps
    * object and return that (this class must be defined).
    **/
   public Properties getLoginProperties( )
   {
      return E2Designer.getLoginProperties();
   }

   /**
    * Use this method to get the object store. This makes sure we are using the
    * same session ID for one E2Designer instance.
    *
    * @return PSObjectStore the object store
    */
   public PSObjectStore getObjectStore( )
   {
      return PSProxyUtils.getObjectStore();
   }

   /**
    * Tries to open an application named strAppName on the server that this
    * window was created on. If the application cannot be opened, a message
    * is displayed to the user with the reason.
    * @return {@link UIAppFrame} with the application.
    */
   private UIAppFrame openApplication(
         final PSXmlApplicationEditor xmlApplicationEditor)
   {
      m_parentShell = xmlApplicationEditor.getSite().getShell();

      // the object won't initialize if the store can't be created
      Debug.assertTrue(null != getObjectStore(), getResources(), "OSNotInit",
            null);

      final UIAppFrame appFrame = createApplication(
            xmlApplicationEditor.getApplication(),
            xmlApplicationEditor.isReadOnly(), "", xmlApplicationEditor);
      if (!xmlApplicationEditor.getReference().isPersisted())
      {
         appFrame.setNewApplication();
      }
      return appFrame;
   }

   /**
    * Eclipse-specific way to load the application.
    * @param editor the frame editor.
    * @return {@link UIAppFrame} with the application.
    */
   public UIAppFrame loadApplication(final PSXmlApplicationEditor editor)
   {
      final IPSReference ref = editor.getApplicationRef(); 
      if (m_applicationsToOpen.containsKey(ref))
      {
         final OSApplication app =
               (OSApplication) m_applicationsToOpen.remove(ref);
         final ApplicationImportExport impExp =
               (ApplicationImportExport) m_applicationsToOpenData.remove(ref);
         assert app != null;
         assert impExp != null;
         /* We want to create the app after the files are added to the application
         so they are available to the application objects. */
         final File origLocation = app.setAppFileLocation(impExp.getAppDir());
         final UIAppFrame appFrame =
               createApplication(app, editor.isReadOnly(), "", editor);
         appFrame.setNewApplication();
         app.setAppFileLocation(origLocation);
         return appFrame;
      }
      else
      {
         return openApplication(editor);
      }
   }

   /*
    * Get the application frame for the provided name. Will return null if not
    * found.
    *
    * @param appName the application name we are looking for
    * @return UIAppFrame the application frame if we found it, null otherwise
    */
   public UIAppFrame getApplicationFrame(String appName)
   {
      return getApplicationEditor(appName).getAppFrame();
   }

   public PSXmlApplicationEditor getApplicationEditor(final String appName)
   {
      if (m_page == null)
      {
         return null;
      }
      final IEditorReference[] refs = m_page.getEditorReferences();
      for (final IEditorReference reference : refs)
      {
         if (reference.getEditor(false) instanceof PSXmlApplicationEditor)
         {
            final PSXmlApplicationEditor editor =
               (PSXmlApplicationEditor) reference.getEditor(false);
            if (appName.equals(editor.getApplication().getName()))
            {
               return editor;
            }
         }
      }
      return null;
   }

   /**
    * Sets the cursor to the wait cursor for the entire application. Increments an internal
    * counter so that multiple calls to this method are handled correctly.
    * Each call to this method must be matched by a call to <code>clearWaitCursor
    * </code>.
    * <em>NOTE:</em>  Be sure that all possible paths have a clearWaitCursor,
    * including all error paths, or the program will be unaccessible.
    **/
   public synchronized void setWaitCursor()
   {
      m_waitCursorCount++;
      if ( 1 == m_waitCursorCount )
      {
         // Setting the frame cursor AND glass pane cursor in this order
         // works around the Win32 problem where you have to move the mouse 1
         // pixel to get the Cursor to change.
         Component glassPane = this.getGlassPane();

         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         glassPane.setVisible(true);
         // Force glass pane to get focus so that we consume KeyEvents
         glassPane.requestFocus();
         glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
   }


   /**
    * Decrements the wait cursor counter, setting the cursor back to the default
    * cursor when the count reaches 0.
    *
    * @see #setWaitCursor
    **/
   public synchronized void clearWaitCursor()
   {
      if ( m_waitCursorCount > 0 )
         m_waitCursorCount--;

      if ( 0 == m_waitCursorCount )
      {
         Component glassPane = this.getGlassPane();

         glassPane.setCursor(Cursor.getDefaultCursor());
         glassPane.setVisible(false);
         this.requestFocus();
         this.setCursor(Cursor.getDefaultCursor());
      }

   }

   /*
    * According to the current status, this function enables and starts or stops
    * and disables the application.
    * The main flag we are relying on is the "enabled" flag.
    *
    * @param app the application
    * @param releaseLock  whether or not to release the application lock
    * @param the frame which caused this action, this will be used to update menu
    *        and toolbar, maybe null
    */
   public void toggleApplicationStatus(PSApplication app, boolean releaseLock,
         UIAppFrame frame)
   {
      UIAppFrame localFrame = frame;
      PSApplication localApp = app;
      String oldRevision = "";

      if (localApp == null)
      throw new IllegalArgumentException();

      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

         // get the application frame if none was provided
         if (localFrame == null)
         {
            localFrame = getApplicationFrame(localApp.getName());
            /* the frame may still be null if this method was called outside the
            context of an open app */
            if (localFrame != null)
            {
               // use its application
               localApp = (PSApplication) localFrame.getData();
               if (localApp == null)
               throw new IllegalArgumentException();
            }
         }

         // validate only if we try to start the application
         boolean validate = !localApp.isEnabled();
         localApp.setEnabled(!localApp.isEnabled());
         if (localApp.getRevisionHistory() != null)
         oldRevision = localApp.getRevisionHistory().getLatestVersion();
         if (localFrame != null)
         {
            // use the frames save if it exists or if it was provided
            if (!localFrame.saveApp(false, validate))
            {
               localApp.setEnabled(!localApp.isEnabled());

               String newRevision = "";
               if (localApp.getRevisionHistory() != null)
               newRevision = localApp.getRevisionHistory().getLatestVersion();
               if (!oldRevision.equals(newRevision))
               {
                  // if the version changed, we know that saving was o.k. but starting
                  // failed. in this case we disable the application again
                  localFrame.saveApp(false, false);
               }
            }
         }
         else
         {
            getObjectStore().saveApplication(localApp, releaseLock, validate,
                  false);
         }
      }
      catch (PSVersionConflictException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (PSNotLockedException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (PSAuthorizationException e)
      {
         Object[] astrParams =
         {
            e.getLocalizedMessage(),
         };

         MessageDialog.openError(m_parentShell,
               MessageFormat.format(getResources().getString("AuthException"), astrParams),
               MessageFormat.format(getResources().getString("AuthException"), astrParams));
      }
      catch (PSServerException e)
      {
         Object[] astrParams =
         {
            localApp.isEnabled() ? getResources().getString("Start") : getResources().getString("Stop"),
            localApp.getName(),
            e.getLocalizedMessage(),
         };

         PSDlgUtil.showErrorDialog(
               MessageFormat.format(getResources().getString("StartStopApplicationException"), astrParams),
               getResources().getString("ApplicationErr"));
      }
      catch (PSNonUniqueException e)
      {
         Object[] astrParams =
         {
            localApp.getName(),
            e.getLocalizedMessage(),
         };

         PSDlgUtil.showErrorDialog(
               MessageFormat.format(getResources().getString("NotUniqueApplicationException"), astrParams),
               getResources().getString("ApplicationErr"));
      }   catch (Exception e)
      {
         PSDlgUtil.showErrorDialog( e.getLocalizedMessage(),
                 getResources().getString("ApplicationErr"));
         localApp.setEnabled(!localApp.isEnabled());
         String newRevision = "";
         if (localApp.getRevisionHistory() != null)
            newRevision = localApp.getRevisionHistory().getLatestVersion();
         if (!oldRevision.equals(newRevision))
         {
            try
            {
               // if the version changed, we know that saving was o.k. but starting
               // failed. in this case we disable the application again
               getObjectStore().saveApplication(localApp, releaseLock, false,
                       false);
            }
            catch (Exception e1)
            {
               PSDlgUtil.showErrorDialog(e1.getLocalizedMessage(),
                       getResources().getString("ServerErr"));
            }
         }
      }
      finally
      {
         setCursor(Cursor.getDefaultCursor());
      }
   }

   // action item handlers
   /**
    * Initialize the provided application. This function should only be called to
    * set up default settings after creating a new application.
    *
    * @param app the application
    * @param name the application name
    */
   public static void initApplication(PSApplication app, String name)
   {
      try
      {
         // set the applications name, we also default the application root the
         // the same as the applictaion name
         app.setName(name);
         app.setRequestRoot(name);

         // disable the application
         app.setEnabled(false);

         // Set the request default types
         app.setRequestTypeHtmlParamName(getResources().getString("DefaultActionField"));
         app.setRequestTypeValueQuery(getResources().getString("DefaultQueryActionName"));
         app.setRequestTypeValueUpdate(getResources().getString("DefaultUpdateActionName"));
         app.setRequestTypeValueInsert(getResources().getString("DefaultInsertActionName"));
         app.setRequestTypeValueDelete(getResources().getString("DefaultDeleteActionName"));

         // setup anonymous ACL entry
         PSAcl acl = app.getAcl();
         PSAclEntry anonymous = null;
         PSAclEntry defaultEntry = null;
         PSCollection entries = acl.getEntries();
         if (entries != null)
         {
            for (int i=0, n=entries.size(); i<n; i++)
            {
               PSAclEntry entry = (PSAclEntry) entries.get(i);
               if (entry.getName().equals(entry.ANONYMOUS_USER_NAME))
               {
                  anonymous = entry;
               }
               else if (entry.getName().equals(entry.DEFAULT_USER_NAME))
               {
                  defaultEntry = entry;
               }
            }
         }

         if (anonymous != null)
         {
            // allow data access query/update by default
            anonymous.setAccessLevel(PSAclEntry.AACE_DATA_CREATE |
            PSAclEntry.AACE_DATA_DELETE |
            PSAclEntry.AACE_DATA_QUERY |
            PSAclEntry.AACE_DATA_UPDATE);
         }
         if (defaultEntry != null)
         {
            // allow data access AND design access query/update by default
            defaultEntry.setAccessLevel(PSAclEntry.AACE_DATA_CREATE |
            PSAclEntry.AACE_DATA_DELETE |
            PSAclEntry.AACE_DATA_QUERY |
            PSAclEntry.AACE_DATA_UPDATE |
            PSAclEntry.AACE_DESIGN_DELETE |
            PSAclEntry.AACE_DESIGN_MODIFY_ACL |
            PSAclEntry.AACE_DESIGN_READ |
            PSAclEntry.AACE_DESIGN_UPDATE);
         }

         // disable application
         app.setEnabled(false);
      }
      catch (Exception e)
      {
         ms_log.error("Failed to initialize the application", e);
      }
   }

   private static E2DesignerResources getResources()
   {
      return E2Designer.getResources();
   }

   /**
    * Launches the page setup dialog
    */
   public void actionPageSetup()
   {
      final PrinterJob job = PrinterJob.getPrinterJob();
      if (job != null)
      {
         if (m_pf == null)
         {
            m_pf = job.defaultPage();
         }
         try
         {
            final Runnable printTask = new Runnable()
            {
               public void run()
               {
                  m_pf = job.pageDialog(m_pf);
               }
            };
            lockSWTFor(printTask);
         }
         catch (Exception e)
         {
            PSDlgUtil.showError(e);
         }
      }
   }

   private void doImportApplication(final IPSReference ref,
         final ApplicationImportExport impExp) throws Exception
   {
      final Runnable importTask = new Runnable()
      {
         public void run()
         {
            try
            {
               if (!impExp.importApplication(ref))
               {
                  return;
               }
               final OSApplication app =
                     (OSApplication) getAppModel().load(ref, true, false);
               m_applicationsToOpen.put(ref, app);
               m_applicationsToOpenData.put(ref, impExp);

               launchOpenApp(ref);
            }
            catch (Exception e)
            {
               PSDlgUtil.showError(e);
            }
         }
      };

      try
      {
         lockSWTFor(importTask);
      }
      catch (InvocationTargetException e)
      {
         PSDlgUtil.showError(e);
      }
   }
   
   /**
    * Launches imported application opening routine in SWT thread. 
    */
   private void launchOpenApp(final IPSReference ref)
   {
      final Runnable openTask = new Runnable()
      {
         public void run()
         {
            openImportedApp(ref);
         }
      };
      getDisplay().asyncExec(openTask);
   }

   /**
    * Opens imported application in the editor.
    */
   private final void openImportedApp(final IPSReference ref)
   {
      try
      {
         openEditorFor(ref);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Main call for application import.
    * Requests user for application data to import.
    * Note, the import process runs asynchroniously, so application is not
    * imported yet when the call returns.
    * @param ref the application to load data into. Not <code>null</code>.
    */
   public void importApp(final IPSReference ref)
   // uses chain of methods to make jumping from thread
   // to thread more comprehensible
   {
      assert ref != null;
      // the object won't initialize if the store can't be created
      Debug.assertTrue(null != getObjectStore(), getResources(), "OSNotInit",
            null);
      getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            try
            {
               if (!lockApplication(ref))
               {
                  return;
               }
               launchImport(ref);
            }
            catch (Exception e)
            {
               PSDlgUtil.showError(e);
            }
         }
      });
   }

   /**
    * Launches next step of import process in the AWT event thread.
    * @param ref the application to load.
    */
   protected void launchImport(final IPSReference ref)
   {
      SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  try
                  {
                     final ApplicationImportExport impExp =
                        new ApplicationImportExport(
                              new JFileChooser(System.getProperty("user.dir")));
                     doImportApplication(ref, impExp);
                  }
                  catch (Exception e)
                  {
                     PSDlgUtil.showError(e);
                  }
               }
            });
   }

   /**
    * Locks application for editing. Correctly handles locking.
    * @param ref the application reference. Never <code>null</code>.
    * @return <code>true</code> if application was successfully locked for
    * editing.
    */
   private boolean lockApplication(IPSReference ref)
   {
      try
      {
         try
         {
            getAppModel().load(ref, true, false);
            return true;
         }
         catch (PSLockException e)
         {
            return PSXmlApplicationEditor.handleLockException(ref, e) != null;
         }
      }
      catch (Exception e)
      {
         return false;
      }
   }

   /**
    * Opens editor for the specified reference.
    */
   private void openEditorFor(final IPSReference ref)
   {
      getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            try
            {
               PSEditorRegistry.getInstance().findEditorFactory(ref.getObjectType())
                     .openEditor(getActivePage(), ref);
            }
            catch (Exception e)
            {
               PSDlgUtil.showError(e);
            }
         }
      });
   }

   /**
    * Current workbench page.
    */
   private IWorkbenchPage getActivePage()
   {
      return m_site.getWorkbenchWindow().getActivePage();
   }

   public void actionPrint(final PSXmlApplicationEditor editor)
   {
      final JInternalFrame frame = (JInternalFrame) editor.getEditorControl();

      if (frame instanceof Bookable)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               try
               {
                  printFrame(frame);
               }
               catch (Exception e)
               {
                  PSDlgUtil.showError(e);
               }
            }
         });
      }
   }


   /**
    * Opens the dialog that is currently hightlighted.
    */
   public void actionViewProperties()
   {
      JInternalFrame[] frames = m_contentPane.getAllFrames();
      for (int i = 0; i < frames.length; i++)
      {
         if (frames[i].isSelected())
         {
            //System.out.println(frames[i].getTitle());
            if (frames[i] instanceof UIAppFrame)
            {
               Vector components = ((UIAppFrame)frames[i]).getSelected();

               if (1 == components.size())
               {
                  ((UIFigure)components.get(0)).onEdit(((UIAppFrame)frames[i]).getData());

               }
               else
               return;
            }
            else if (frames[i] instanceof UIPipeFrame)
            {
               Vector components = ((UIPipeFrame)frames[i]).getSelected();

               if (1 == components.size())
               {
                  ((UIFigure)components.get(0)).onEdit(((UIPipeFrame)frames[i]).getData());
               }
               else
               return;
            }
         }
      }
   }

   /**
    * Launches the Options dialog.
    */
   public void actionEditOptions()
   {
      if(m_optionsDialog == null)
      {
         m_optionsDialog = new OptionsPropertyDialog(this);
      }
      m_optionsDialog.setVisible(true);
   }


   public void deleteOptionsDialog()
   {
      if(m_optionsDialog != null)
      {
         m_optionsDialog=null;
      }
   }

   /**
    * Launches the Javahelp viewer to display help.
    */
   public void actionHelp()
   {
      PSJavaHelp.launchHelp( "workbench" );
   }

   /**
    * Displays the About dialog.
    */
   public void actionAbout()
   {
      if(m_dialogAbout == null)
      {
         m_dialogAbout = new AboutDialog(this);
      }
      m_dialogAbout.setVisible(true);
   }

   /**
    * Delete the passed in application from the object store.
    *
    * @param appName the name of the application which will be deleted
    * @return boolean false if the user cancelled the delete, true if deleted
    */
   //////////////////////////////////////////////////////////////////////////////
   public boolean deleteApplication(String appName)
   {
      try
      {
         // first check to see if the provided app exists
         boolean exists = true;
         try
         {
            getObjectStore().getApplication(appName, false);
         }
         catch(PSNotFoundException e)
         {
            exists = false;
         }

         if (exists)
         {
            // get confirmation from the user to remove the existing application
            final int userChoice = PSDlgUtil.showConfirmDialog(
                  getResources().getString("CONFIRM_REPLACE"),
                  getResources().getString("WARNING"),
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
            if (userChoice == JOptionPane.YES_OPTION)
            {
               getObjectStore().removeApplication(appName);
            }
            else
            {
               return false;
            }
         }
      }
      catch(PSServerException ex)
      {
         PSDlgUtil.showError(ex);
      }
      catch(PSAuthorizationException ex)
      {
         // exception is caused because the user does not have write access to this
         // application. Must return false to let caller know that delete has
         // failed. (This will lead to a bug in UIAppFrame)
         PSDlgUtil.showErrorDialog(
               getResources().getString("CannotDeleteApp"),
               getResources().getString("AuthErr"));
         // force application delete cancellation.
         return false;
      }
      catch (PSAuthenticationFailedException e)
      {
         PSDlgUtil.showError(e, false, getResources().getString("ExceptionTitle"));
      }
      catch(PSLockedException ex)
      {
         PSDlgUtil.showError(ex);
      }

      return true;
   }

   /**
    * Start the First Tutorial lesson.
    */
   /**
    * Start the First Tutorial lesson.
    * TODOph: get file names from properties file
    */
   public void actionHelpStartTutorial()
   {
      @SuppressWarnings("unused") StartTutorialDialog dlg = new StartTutorialDialog(E2Designer.getApp().getMainFrame());

      throw new AssertionError("Implement");
//      if(dlg.wasOKPressed())
//      {
//         String strImportFromFileName = null;
//         switch ( dlg.getLessonSelected())
//         {
//            case 1:
//            createApplication(dlg.getTutorialName(), null);
//            throw new AssertionError("implement");
//            //return;
//
//            case 2:
//            strImportFromFileName = "StartOfLessonTwo.xml";
//            break;
//
//            case 3:
//            strImportFromFileName = "StartOfLessonThree.xml";
//            break;
//
//            case 4:
//            strImportFromFileName = "StartOfLessonFour.xml";
//            break;
//
//            default:
//            break;
//         }
//
//         String strRootDir = E2Designer.getApp().getDesignerProperties().getProperty(
//         E2Designer.getApp().getResources().getString("propsRoot"));
//         if(strRootDir == null)
//         strRootDir = "";
//         else
//         strRootDir += File.separator;
//
//         strRootDir += "Tutorial" + File.separator + "LessonFiles";
         //import our start from app
//         UIAppFrame app = importApp(strRootDir, strImportFromFileName);
//         app.setApplicationName( dlg.getTutorialName());
//      }
   }


   /**
    * All actions that are not directly supported by the main frame get routed
    * through this handler. This handler dispatches them to the listeners registered
    * for the actions.
    */
   public void actionChildSupported( ActionEvent e )
   {
      String strActionName = ((Component)e.getSource()).getName();
      ActionListener l = m_actionListenersList.get(strActionName);
      Debug.assertTrue(l != null, "Missing listener for action " + strActionName);
      l.actionPerformed(e);
   }

   /**
    * Creates a new application using defaults from the server that was passed
    * in when this window was created.
    * If the server can't be contacted or the user is not authorized to create
    * applications on this server, the appropriate message is displayed to the
    * user.
    *
    * @returns <code>true</code> if the server is successfully contacted and
    * the application is successfully created. <code>false</code> otherwise.
    */
   public boolean createNewApplication( )
   {
      return false;
   }

   /**
    * Sets the text displayed in the status bar. If the supplied string is null,
    * the message is cleared (restoring the default msg).
    */
   public void setStatusMessage(final String strMsg)
   {
      getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            m_statusLineManager.setMessage(strMsg);
         }
      });
   }

   /**
    * Clears the message displayed in the status bar (restoring the default
    * message).
    */
   public void clearMessage()
   {
      setStatusMessage(null);
   }


   // private implementation

   /**
    * These are configuration entry keys.
    */

   public static final String RECENT_APPS = "RecentApplications";

   /**
    * Does all the basic initialization for the main window.
    * The resource bundle for the application must be available before this
    * method is called. It will try to obtain the bundle from the main app.
    * <p>
    * The configurator should be available before this method is called.
    * If it is not, default values will be used.
    * <p>
    * The border layout is used for the main window area, with the top being
    * allocated to the toolbar and the bottom being allocated to the status
    * bar. The center is used for the application windows.
    */
   private void init( )
   {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints gc = new GridBagConstraints();
      
      Container cp = getContentPane();
      cp.setLayout( gridbag );

      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      m_contentPane = new JDesktopPane( );
      m_contentPane.setBackground( new Color( 140, 140, 140 ));   // medium dk gray
      m_contentPane.putClientProperty("JDesktopPane.dragMode", "outline");

      gc.gridx = 0;
      gc.gridy = 1;
      gc.fill = GridBagConstraints.BOTH;
      gc.weightx = 1.0;
      gc.weighty = 1.0;
      m_contentPane.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
   }

   public class WindowListDialog extends PSDialog
   {
      /** Constructor which takes the JFrame object.
       *
       */
      public WindowListDialog( JFrame frame, Object[] data)
      {
         super( frame, E2Designer.getResources( ).getString("WindowListTitle"));

         m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
         {
            @Override
            public void onOk()
            {
               m_bOk = true;
               dispose();
            }
         };

         m_List = new JList(data);

         getRootPane().setDefaultButton(m_commandPanel.getOkButton());

         JPanel panel = new JPanel(new BorderLayout());
         panel.setBorder(new EmptyBorder(5, 5, 5, 5));
         JPanel listpanel = new JPanel(new BorderLayout());
         listpanel.setBorder(new EmptyBorder(5,5,5,5));
         JScrollPane sPane = new JScrollPane(m_List);
         listpanel.add(sPane, BorderLayout.CENTER);
         panel.add(listpanel, BorderLayout.CENTER);
         panel.add(m_commandPanel, BorderLayout.EAST);

         getContentPane().setLayout(new BorderLayout());
         getContentPane().add(panel);
         this.setSize(W_DIALOG_SIZE);
         center();
         setVisible(true);
      }

      public boolean wasOKPressed()
      {
         return(m_bOk);
      }

      public String getSelectedValue()
      {
         if(m_List.getSelectedValue() != null)
         return(m_List.getSelectedValue().toString());

         return(null);
      }


      private UTStandardCommandPanel m_commandPanel = null;
      private JList m_List = null;
      private boolean m_bOk = false;
      private final Dimension W_DIALOG_SIZE = new Dimension(300, 200);
   }

   /**
    * This function will create a new application window and add it to the frame.
    * If readonly is true, due to lock by self or someone else.
    */
   public UIAppFrame createApplication(OSApplication App, boolean readOnly,
         String locker, final PSXmlApplicationEditor xmlApplicationEditor)
   {
      UIAppFrame f = new UIAppFrame(App, getObjectStore(), readOnly,
            xmlApplicationEditor);
      if (readOnly)
      {
         final Object[] params =
         {
            locker
         };

         final String strReadOnly =
            MessageFormat.format(getResources().getString("ApplicationLocked"), params);
         f.setTitle(App.getName() + strReadOnly);
      }

      return f;
   }

   /**
    * Get the current page format
    */
   public PageFormat getPageFormat()
   {
      return m_pf;
   }

   /**
    * Insures that legacy UI is used only on one page.
    * @param site site used to initialize the workbench part containing
    * legacy UI.
    * @throws PartInitException if different window is discovered. 
    */
   public void validatePlatform(IWorkbenchSite site) throws PartInitException
   {
      if (m_page == null)
      {
         m_site = site;
         m_page = site.getPage();
      }
      if (!m_page.equals(site.getPage()))
      {
         throw new PartInitException(
               PSMessages.getString("UIMainFrame.many.windows.failure")); //$NON-NLS-1$
      }
   }
   
   /**
    * Registers provided dialog for locking UI.
    * @see AwtSwtModalDialogBridge
    */
   public void registerDialog(final JDialog dialog)
   {
      m_awtSwtBridge.registerModalSwingDialog(dialog);
   }

   /**
    * Locks SWT UI for the following Swing/AWT task.
    * Must be called from the Swing event thread.
    * @see AwtSwtModalDialogBridge#lockSWTFor(Runnable)
    */
   public void lockSWTFor(final Runnable task) throws InvocationTargetException
   {
      assert SwingUtilities.isEventDispatchThread() :
         "This method must be called from AWT/Swing event thread!";

      m_awtSwtBridge.lockSWTFor(task);
   }

   /**
    * Current display.
    */
   private Display getDisplay()
   {
      return getWorkbench().getDisplay();
   }

   /**
    * Current workbench.
    */
   private IWorkbench getWorkbench()
   {
      return PlatformUI.getWorkbench();
   }

   /**
    * Assigns current status line manager.
    * @param statusLineManager
    */
   public void setStatusLineManager(IStatusLineManager statusLineManager)
   {
      m_statusLineManager = statusLineManager;
   }

   /* ################### IConnectionSource interface ##################### */

   /**
    * This is not a proper implementation of this interface. We need to
    * verify the connection when requested and attempt to re-connect.
    * See {@link IConnectionSource#getDesignerConnection(boolean)
    * getDesignerConnection} for more details.
    */
   public PSDesignerConnection getDesignerConnection(
         @SuppressWarnings("unused") boolean verifyConnection)
   {
      return E2Designer.getDesignerConnection();
   }
   
   /**
    * Prints specified page
    */
   private void printFrame(final JInternalFrame frame)
   {
      final PrinterJob job = PrinterJob.getPrinterJob();
      if (m_pf == null)
      {
         m_pf = job.defaultPage();
      }

      try
      {
         final Runnable printTask = new Runnable()
         {
            public void run()
            {
               printFrameInPrintJob(frame, job);
            }
         };
         lockSWTFor(printTask);
      }
      catch (InvocationTargetException e)
      {
         PSDlgUtil.showError(e);
      }
   }


   /**
    * Prints frame in the provided print job.
    */
   private void printFrameInPrintJob(final JInternalFrame frame, final PrinterJob job)
   {
      final Book book = new Book();
      ((Bookable)frame).appendPrintPages(book);
      // Pass the book to the PrinterJob
      job.setPageable(book);

      // Put up the dialog box
      if (job.printDialog())
      {
         // Print the job if the user didn't cancel printing
         try
         {
            job.print();
         }
         catch (PrinterException e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   /**
    * Convenience method to access application model.
    * @return Never <code>null</code>.
    */
   private IPSCmsModel getAppModel() throws PSModelException
   {
      return PSCoreFactory.getInstance().getModel(PSObjectTypes.XML_APPLICATION);
   }
   

   // private variables
   private IStatusLineManager m_statusLineManager;

   /**
    * This object contains the server name, userid and pw, as needed to create
    * an object store instance.
    */
   // listeners that want action events not supported by main frame
   private Hashtable<String, ActionListener> m_actionListenersList = new Hashtable<String, ActionListener>();

   // keep the content pane around since it will be used frequently
   private JDesktopPane m_contentPane;

   private PageFormat m_pf;

   /** The About Rhythmyx Dialog that has a link to the Percussion website.
    */
   private AboutDialog m_dialogAbout = null;

   /** The Options Dialog launches by the Tools menu selection.
    */
   private OptionsPropertyDialog m_optionsDialog = null;

   /**
    * The reference to the help object. Keep a single reference so the object's
    * class does not get unloaded.
    **/
   protected PSHelp m_help = PSHelp.getInstance();
   
   /**
    * Keeps a count of the number of times setWaitCursor has been called. This
    * allows multiple, nested calls w/o restoring the cursor on the first call
    * to clear.
    **/
   private int m_waitCursorCount = 0;
   
   /**
    * Shell used to show dialogs.
    */
   Shell m_parentShell;
   
   /**
    * The page associated with legacy UI.
    */
   private IWorkbenchPage m_page;

   /**
    * First site provided to {@link #validatePlatform(IWorkbenchSite)}
    * method.
    */
   private IWorkbenchSite m_site;
   
   /**
    * Manages modal Swing dialogs, initialized while constructed, never 
    * <code>null</code> or changed after that.
    */
   private AwtSwtModalDialogBridge m_awtSwtBridge;
   
   /**
    * Identity map storing mappings between just created applications. 
    * Keys are weak, values are hard references.
    * Keys are {@link PSReference}s, values are applications.
    * Andriy: hackish. Wonder if there is more Eclipsy way to do this :-)
    */
   private final ReferenceIdentityMap m_applicationsToOpen =
      new ReferenceIdentityMap(ReferenceIdentityMap.WEAK, 
         ReferenceIdentityMap.HARD);
   
   /**
    * Stores {@link ApplicationImportExport} data for same keys as
    * {@link #m_applicationsToOpen}.
    */
   private final ReferenceIdentityMap m_applicationsToOpenData =
      new ReferenceIdentityMap(ReferenceIdentityMap.WEAK, 
         ReferenceIdentityMap.HARD);
}


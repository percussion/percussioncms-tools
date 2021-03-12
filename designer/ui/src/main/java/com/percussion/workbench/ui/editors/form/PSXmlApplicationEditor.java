/******************************************************************************
 *
 * [ PSXmlApplicationEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.ICustomDropSourceData.DropAction;
import com.percussion.E2Designer.OSApplication;
import com.percussion.E2Designer.OSResultPage;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.PSWidgetCloseStrategy;
import com.percussion.E2Designer.UIAppFrame;
import com.percussion.E2Designer.UIFigureFrame;
import com.percussion.E2Designer.UIMainFrame;
import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalStateException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import com.percussion.workbench.ui.util.PSFileEditorHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderAdapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Provides a multi-tabbed UI for modifying an xml application and its
 * associated files. The first tab is the main application. Other tabs are 
 * opened as needed for app resources and app files.
 * 
 * @version 6.0
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationEditor extends PSMultiPageEditorBase
   implements IPSHelpProvider
{
   private final static Logger ms_log =
         LogManager.getLogger(PSXmlApplicationEditor.class);

   @Override
   public boolean isValidReference(IPSReference ref)
   {
      return ref.getObjectType().getPrimaryType().equals(
            PSObjectTypes.XML_APPLICATION);
   }

   @Override
   protected void createPages()
   {
      m_display = getContainer().getDisplay();
      m_tabFolder = (CTabFolder) getContainer().getParent();
      getTabFolder().addCTabFolder2Listener(new AppTabFolderListener());
      showCloseButtonOnTabs();
      // super requires at least 1 page, so give it a dummy one
      addPage(0, new Composite(getContainer(), SWT.NONE));
      m_page0IsDummy = true;
   }

   /* (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#dispose()
    */
   @Override
   public void dispose()
   {
      disposeParts();
      
      Disposer disposer = new Disposer(this);
      disposer.start();
   }
   
   /**
    * The disposer thread first waits until the <code>doSave</code> method has 
    * finished. After that it calls <code>doDispose</code> to cleanup the 
    * editor.
    */
   private class Disposer extends Thread
   {
      /**
       * Constructs a new disposer for the supplied editor.
       * 
       * @param editor the editor to dispose, not <code>null</code>.
       */
      public Disposer(PSXmlApplicationEditor editor)
      {
         if (editor == null)
            throw new IllegalArgumentException("editor cannot be null");
         
         m_editor = editor;
      }
      
      /*(non-Javadoc)
       * @see java.lang.Thread#run()
       */
      @Override
      public void run()
      {
         while (m_doSave)
         {
            try
            {
               Thread.sleep(100);
            }
            catch (InterruptedException ie)
            {
               // continue
            }
         }

         m_editor.doDispose();
      }
      
      /**
       * The editor to dispose after the save, never <code>null</code>.
       */
      final private PSXmlApplicationEditor m_editor;
   }
   
   /**
    * Do the actual dispose.
    */
   private void doDispose()
   {
      try
      {
         super.dispose();
      }
      catch (NullPointerException e)
      {
         /*
          * Work around a bug in JFaceResources.getResources(final Display 
          * toQuery). The problem occurrs if the method is called with a null 
          * toQuery parameter.
          */
      }
   }

   @Override
   protected void initHelpManager()
   {
      m_helpManager = new PSHelpManager(this, m_tabFolder);
   }

   public void loadControlValues(
         @SuppressWarnings("unused") Object designObject)
   {
      // note, designObject is not used because the class gets application
      // directly from PSEditorBase.getDesignerObject()
      try
      {
         getMainFrame().validatePlatform(getSite());
      }
      catch (PartInitException e)
      {
         PSDlgUtil.showError(e);
      }
      
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               m_appFrame = getMainFrame().loadApplication(
                     PSXmlApplicationEditor.this);
               if (m_appFrame == null)
               {
                  PSDlgUtil.showErrorDialog(
                        getFailedLoadAppMessage(getApplicationRef().getName()),
                        PSMessages.getString("common.error.title"));
               }
               launchAddApplicationPage();
            }
            catch (Exception e)
            {
               PSDlgUtil.showError(e);
            }
         }
      });
   }
   
   /**
    * Asynchroniously adds application page.
    * Is used to run this functionality in SWT event thread.
    */
   private void launchAddApplicationPage()
   {
      getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            try
            {
               // remove a dummy page
               assert m_page0IsDummy : "Due to the fact app editor uses " +
                     "legacy logic data should be loaded only once";
               m_page0IsDummy = false;
               removePage(0);
               addFrameAsPage(m_appFrame,
                     PSMessages.getString("PSXmlApplicationEditor.mainFrameName"));      //$NON-NLS-1$
               scrollAppFrameToBeginning();
            }
            catch (Exception e)
            {
               PSDlgUtil.showError(e);
            }
         }
      });
   }

   /**
    * Scrolls the application diagram to show upper left corner.
    * This has to be called explicitely because improvements in SWT/AWT event
    * threading handling made application diagram to be shifted when it has any
    * content.
    * Is called during opening, after all other initialization is done and
    * {@link #m_appFrame} is initialized.
    */
   private void scrollAppFrameToBeginning()
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               ((JScrollPane) m_appFrame.getContentPane()).getViewport()
                     .setViewPosition(new Point(0, 0));
            }
            catch (Exception e)
            {
               PSDlgUtil.showError(e);
            }
         }
      });
   }

   /**
    * Currently does nothing, all the update action occurs in
    * {@link #doSave(IProgressMonitor)}
    */
   public void updateDesignerObject(
         @SuppressWarnings("unused") Object designObject,
         @SuppressWarnings("unused") Object control)
   {
   }

   @SuppressWarnings("deprecation")//$NON-NLS-1$
   private void showCloseButtonOnTabs()
   {
      // Andriy: a dirty hack to show close buttons on tabs.
      // Relies on backwards compatibility behavior of CTabFolder which 
      // shows close buttons for folders when this listener is added  
      getTabFolder().addCTabFolderListener(new CTabFolderAdapter());
   }

   /* (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#doSave(
    *    org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public void doSave(IProgressMonitor pMonitor)
   {
      m_doSave = true;
      PSModelTracker tracker = PSModelTracker.getInstance();
      try
      {
         if (isReadOnly())
         {
            doSaveAs();
            m_doSave = false;
            return;
         }
         
         Object[] args = new Object[]{m_reference.getName()};
         pMonitor.beginTask(
            PSMessages.getString("PSEditorBase.progress.message.saving", args),
            IProgressMonitor.UNKNOWN);
         
         // save new apps through proxy to update cache and notify listeners
         if (!m_reference.isPersisted())
            tracker.save(m_reference, false, true);
         
         // prepare the application frame for save
         flushEditors(pMonitor);
         applyUpdatesOnSave();
         for (final URL resource : m_loadedResources.keySet())
         {
            ms_log.info("Loading to server file " + 
               m_loadedResources.get(resource));
            
            getApplication().saveAppFile(new File(resource.getFile()),
               new BufferedInputStream(
                  m_loadedResources.get(resource).getContents()));
         }

         // save the application frame
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               try
               {
                  final boolean saved = m_appFrame.saveApp(true, false);
                  
                  if (saved)
                     launchCleanUpClosedFiles();
               }
               catch (Exception e)
               {
                  PSDlgUtil.showError(e);
               }
               finally
               {
                  m_doSave = false;
               }
            }
         });
         
         setClean();
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
         m_doSave = false;
      }
      finally
      {
         pMonitor.done();
      }
   }

   /**
    * Removes files which are not used anymore. Called only after the files are
    * saved to the server, so we do not need to keep a copy locally.
    */
   private void cleanUpClosedFiles()
   {
      removeClosedLoadedResources();
      removeClosedUpdateOnSave();
   }

   /**
    * Removes closed resources previously registered with
    * {@link #updateOnSave(URL, URL, OSResultPage)}.
    */
   private void removeClosedUpdateOnSave()
   {
      for (Iterator<URL> i = m_updateOnSave.keySet().iterator(); i.hasNext();)
      {
         final URL url = i.next();
         if (!getOpenResources().contains(url))
         {
            m_updateOnSaveStylesheets.remove(url);
            i.remove();
         }
      }
   }

   /**
    * Removes closed resources previously open in editors.
    */
   private void removeClosedLoadedResources()
   {
      for (Iterator<URL> i = m_loadedResources.keySet().iterator(); i.hasNext();)
      {
         final URL url = i.next();
         if (!getOpenResources().contains(url))
         {
            IProgressMonitor pm = new NullProgressMonitor();
            try
            {
               m_loadedResources.get(url).delete(true, true, pm);
            }
            catch (CoreException e)
            {
               PSWorkbenchPlugin.getDefault().log("Problem deleting resource",
                     e);
            }
            i.remove();
         }
      }
   }

   /**
    * Set of resources currently open in editors.
    */
   private Set<URL> getOpenResources()
   {
      final Set<URL> openResources = new HashSet<URL>();
      for (final CTabItem item : getTabFolder().getItems())
      {
         if (!item.isDisposed())
         {
            final URL resource = (URL) item.getData(RESOURCE_TAB_PROPERTY);
            if (resource != null)
            {
               openResources.add(resource);
            }
         }
      }

      return openResources;
   }

   /**
    * Applies the updates registered with
    * {@link #updateOnSave(URL, URL, OSResultPage)}.
    * This method is called <b>after</b> main application is saved,
    * and <b>before</b> application files are saved.  
    */
   private void applyUpdatesOnSave() throws IOException, PSException,
         IllegalStateException, CoreException
   {
      for (final URL url : m_updateOnSave.keySet())
      {
         generateStylesheetForSource(url);
      }
   }

   /**
    * Regenerates stylesheet for the provided source url. Removes local data for
    * the stylesheet, closes stylesheet editor.
    */
   public void generateStylesheetForSource(final URL sourceUrl)
         throws IOException, PSException, IllegalStateException, CoreException
   {
      final URL stylesheetUrl = m_updateOnSaveStylesheets.get(sourceUrl);
      assert stylesheetUrl != null;
      closeEditorForResource(stylesheetUrl);
      try
      {
         final IFile file = getFileForResource(sourceUrl, null);
         final OSResultPage page = m_updateOnSave.get(sourceUrl);
         page.startReplace();
         final boolean result = page.prepareSourceForDrop(DropAction.UPDATE,
               file.getFullPath().toFile().getAbsolutePath(), null);
         if (!result)
         {
            throw new IOException("Failed to update page " + page);
         }
         assert StringUtils.isNotBlank(page.getTempFilePath());
         registerStreamAsLoadedResource(stylesheetUrl,
                 new ByteArrayInputStream(page.getLastSplitXSLContent().getBytes(StandardCharsets.UTF_8)));
      }
      finally
      {
      }
   }

   /**
    * Creates a file containing the provided stream and registers this file
    * as loaded resource.
    * @param url the resource url. Never <code>null</code>.
    * @param in the data input stream.
    */
   public void registerStreamAsLoadedResource(URL url, InputStream in)
         throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSNotLockedException,
         PSValidationException, PSIllegalStateException, CoreException
   {
      m_loadedResources.put(url, getFileForResource(url, in));
   }
   
   /**
    * Data from editors are saved to buffers.
    */
   public void flushEditors(final IProgressMonitor monitor)
   {
      m_display.syncExec(new Runnable()
      {
         public void run()
         {
            for (final CTabItem item : getTabFolder().getItems())
            {
               final Object editor = item.getData(EDITOR_TAB_PROPERTY);
               if (editor instanceof ISaveablePart)
                  ((ISaveablePart) editor).doSave(monitor);
            }
         }
     });
   }

   @Override
   public void doSaveAs()
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            m_appFrame.saveAsApp(false, false);
            notifyApplicationChange();
         }
      });
   }
   
   private void notifyApplicationChange()
   {
      m_display.asyncExec(new Runnable()
      {
         public void run()
         {
            firePropertyChange(PROP_DIRTY);
         }
      });
   }

   /**
    * Adds provided frame as a tab.
    * 
    * @param frame the frame to add
    * @param tabText text shown on the added tab.
    */
   public void addFrameAsPage(final JInternalFrame frame, final String tabText)
   {      
      m_display.syncExec(new Runnable()
      {
         public void run()
         {
            final Composite mainTabControl = new Composite(getContainer(),
                  SWT.EMBEDDED);
            final Frame awtContainer = SWT_AWT.new_Frame(mainTabControl);
            {
               final JPanel container = new PSFrameProviderPanel()
               {
                  public JInternalFrame getFrame()
                  {
                     return frame;
                  }
               };
               container.setLayout(new BorderLayout());
               container.add(frame.getRootPane());
               addChangeListenerToFrame(frame);
               awtContainer.add(container);
            }
            final int tabItemIdx = addPage(mainTabControl);
            setPageText(tabItemIdx, tabText);

            // whenever legacy code changes title of the frame change page name
            frame.addPropertyChangeListener(JInternalFrame.TITLE_PROPERTY,
                  new PropertyChangeListener()
                  {
                     @SuppressWarnings("unused")
                     public void propertyChange(PropertyChangeEvent evt)
                     {
                        getDisplay().asyncExec(new Runnable()
                        {
                           public void run()
                           {
                              setPageText(tabItemIdx, frame.getTitle());
                           }
                        });
                     }
                  });
            final CTabItem tabItem = getTabFolder().getItem(tabItemIdx);
            tabItem.setData(EDITOR_TAB_PROPERTY, frame);
            selectPage(tabItemIdx);
            m_helpManager.registerControls(tabItem.getControl());
         }
      });
   }

   /**
    * Registers a listener with the frame to mark the editor as dirty if the
    * frame content changes.
    * @param frame the frame to watch updates for. If <code>null</code> the
    * method does nothing.
    */
   private void addChangeListenerToFrame(final JInternalFrame frame)
   {
      if (frame instanceof UIFigureFrame)
      {
         UIFigureFrame figureFrame = (UIFigureFrame) frame;
         figureFrame.getTheGlassPane().addMouseListener(new MouseAdapter()
         {
            @Override
            @SuppressWarnings("unused")
            public void mousePressed(MouseEvent e)
            {
               markEditorDirty();
            }
         });
      }
   }

   /**
    * Marks editor dirty. Can be called from any thread.
    * Takes into account whether the editor is read-only.
    */
   public void markEditorDirty()
   {
      if (isReadOnly())
      {
         return;
      }
      getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            setDirty();
         }
      });
   }

   /**
    * Closes editor for the specified resource.
    */
   private void closeEditorForResource(final URL url)
   {
      if (getTabForResource(url) != null)
      {
         removePage(getTabFolder().indexOf(getTabForResource(url)));
      }
      m_loadedResources.remove(url);
      m_updateOnSave.remove(url);
   }
   
   /**
    * Opens editor for the specified application resource.
    * 
    * @throws CoreException
    * @throws IllegalStateException
    */
   public void openEditorForResource(final URL url) throws PSServerException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSNotLockedException, PSValidationException,
         PSIllegalStateException, IllegalStateException, CoreException
   {
      if (showEditorIfResourceIsAlreadyOpen(url))
      {
         return;
      }
      
      final IFile file = getFileForResource(url, null);
      if (!file.exists())
      {
         final Object[] args = new Object[]
         {file.toString()};
         PSDlgUtil.showErrorDialog(PSMessages.getString(
               "common.error.fileNotFound", args), //$NON-NLS-1$
               E2Designer.getResources().getString("error")); //$NON-NLS-1$
         return;
      }

      m_display.syncExec(new Runnable()
            {
               public void run()
               {
                  doOpenEditor(url, file);
               }
            });
   }
   
   /**
    * Calls actual SWT code to open the editor for the provided file.
    */
   private void doOpenEditor(final URL url, final IFile file)
   {
      try
      {
         addEditorPage(m_fileEditorHelper.createEditorPartToEdit(file),
               new FileEditorInput(file), new File(url.getFile()).getName(),
               url);
      }
      catch (CoreException e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Triggers page switch hooks.
    */
   public void notifyPageSwitched()
   {
      m_display.syncExec(new Runnable()
      {
         public void run()
         {
            final Event event = new Event();
            event.item = getTabFolder().getSelection();
            getTabFolder().notifyListeners(SWT.Selection, event);
         }
      });
   }

   /**
    * Returns local file for the specified resource. Loads the resource from
    * server if necessary.
    * 
    * @throws CoreException
    * @throws IllegalStateException
    */
   private IFile getFileForResource(final URL url, InputStream content)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSNotLockedException,
         PSValidationException,
         PSIllegalStateException, IllegalStateException, CoreException
   {
      IProject proj = PSWorkbenchPlugin.getDefault().getProject();
      if (m_loadedResources.containsKey(url))
      {
         return m_loadedResources.get(url);
      }
      final String fileName = url.getFile();
      if (content == null)
      {
         final PSApplicationFile appFile = getObjectStore()
               .loadApplicationFile(getApplication(),
                     createAppFileFromFileName(fileName));
         content = appFile.getContent().getContent();
      }
      IPath path = new Path(fileName);
      final IFile localFile = proj.getFile(path);
      IProgressMonitor pm = new NullProgressMonitor();
      if (localFile.exists())
      {
         localFile.setContents(content, true, true, pm);
      }
      else
      {
         localFile.create(content, true, pm);
      }
      m_loadedResources.put(url, localFile);
      return localFile;
   }

   /**
    * Creates {@link PSApplicationFile application file} from file name.
    */
   private PSApplicationFile createAppFileFromFileName(final String fileName)
   {
      return new PSApplicationFile(new File(fileName));
   }
   
   /**
    * Shows the tab with the editor for given resource if it is already open.
    * 
    * @param url the resource
    * @return <code>true</code> if the resource is already open
    */
   private boolean showEditorIfResourceIsAlreadyOpen(final URL url)
   {
      final boolean[] resultContainer =
      {false};
      m_display.syncExec(new Runnable()
            {
               public void run()
               {
                  final boolean alreadyOpen = getTabForResource(url) != null;
                  resultContainer[0] = alreadyOpen;
                  if (alreadyOpen)
                  {
                     selectPage(getTabForResource(url));
                  }
               }
           });
      return resultContainer[0];
   }

   /**
    * Finds editor for the provided resource URL. The method must run in the SWT
    * thread.
    */
   private CTabItem getTabForResource(final URL url)
   {
      for (final CTabItem item : getTabFolder().getItems())
      {
         if (url.equals(item.getData(RESOURCE_TAB_PROPERTY)))
         {
            return item;
         }
      }
      return null;
   }

   /**
    * Adds provided editor as a tab.
    * 
    * @param editor the editor to add
    * @param tabText text shown on the added tab.
    * @param resource object to store in {@link #RESOURCE_TAB_PROPERTY} tab
    * property. Can be <code>null</code>.
    * @throws PartInitException if editor adding fails.
    */
   private void addEditorPage(final IEditorPart editor,
         final IEditorInput input, final String tabText, final URL resource)
         throws PartInitException
   {
      editor.addPropertyListener(new IPropertyListener()
      {
         public void propertyChanged(final Object source, final int propId)
         {
            if (propId == IEditorPart.PROP_DIRTY
                  && source instanceof IEditorPart
                  && !isReadOnly())
            {
               if (((IEditorPart) source).isDirty())
               {
                  setDirty();
               }
            }
         }
      });
      final int tabItemIdx = addPage(editor, input);
      setPageText(tabItemIdx, tabText);
      final CTabItem tabItem = getTabFolder().getItems()[tabItemIdx];
      tabItem.setData(EDITOR_TAB_PROPERTY, editor);
      if (resource != null)
      {
         tabItem.setData(RESOURCE_TAB_PROPERTY, resource);
      }
      selectPage(tabItemIdx);
   }
   
   /**
    * Opens the tab with specified control. Note, the control should be added
    * with one of the <code>add...</code> methods of this class.
    * 
    * @throws IllegalArgumentException if the tab was not found or the specified
    * control is <code>null</code>. 
    */
   public void showTabWithControl(final JInternalFrame frame)
   {
      if (frame == null)
      {
         throw new IllegalArgumentException("The control should not be null");
      }
      m_display.syncExec(new Runnable()
            {
               public void run()
               {
                  if (findFrameItem(frame) == null)
                  {
                     throw new IllegalArgumentException(PSMessages.getString(
                           "PSXmlApplicationEditor.frameNotFound"));            //$NON-NLS-1$
                  }
                  selectPage(findFrameItem(frame));
               }
           });
   }
   
   /**
    * Closes tab with the specified control. Note, the control should be added
    * with one of the <code>add...</code> methods of this class.
    * @param frame the control to find. Should not be <code>null</code>. 
    * 
    * @throws IllegalArgumentException if the tab was not found or the specified
    * control is <code>null</code>. 
    */
   public void closeTabWithControl(final JInternalFrame frame)
   {
      if (frame == null)
      {
         throw new IllegalArgumentException("The control should not be null");
      }
      m_display.syncExec(new Runnable()
            {
               public void run()
               {
                  if (findFrameItem(frame) == null)
                  {
                     throw new IllegalArgumentException(PSMessages.getString(
                           "PSXmlApplicationEditor.frameNotFound"));            //$NON-NLS-1$
                  }
                  final int tabIdx = getTabFolder().indexOf(findFrameItem(frame));
                  removePage(tabIdx);
               }
           });
   }

   /**
    * Finds tab corresponding to the specified frame.
    * @param frame the frame to find item for.
    * Assumed not <code>null</code>.
    * @return the tab correspondit to the frame or <code>null</code> if no such
    * tab is found.
    */
   private CTabItem findFrameItem(final JInternalFrame frame)
   {
      for (final CTabItem item : getTabFolder().getItems())
      {
         if (item.getData(EDITOR_TAB_PROPERTY) == frame)
         {
            return item;
         }
      }
      return null;
   }

   /**
    * Selects the specified page.
    */
   private void selectPage(int tabItemIdx)
   {
      getTabFolder().setSelection(tabItemIdx);
      notifyPageSwitched();
   }

   /**
    * Selects page on the specified tab.
    */
   private void selectPage(final CTabItem item)
   {
      getTabFolder().setSelection(item);
      notifyPageSwitched();
   }

   /**
    * Main {@link UIMainFrame}.
    */
   private UIMainFrame getMainFrame()
   {
      return E2Designer.getApp().getMainFrame();
   }

   /**
    * Generates message about failure to load the application
    * 
    * @param name the application name
    */
   private String getFailedLoadAppMessage(final String name)
   {
      return PSMessages.getString(
            "PSXmlApplicationEditor.failed.load.application", //$NON-NLS-1$
            new Object[]
            {name});
   }

   private PSObjectStore getObjectStore()
   {
      return getMainFrame().getObjectStore();
   }

   /**
    * Returns application the editor edits.
    */
   public OSApplication getApplication()
   {
      return (OSApplication) getDesignerObject();
   }

   /**
    * Sets application. Note, this method leaves application-related data like
    * open files intact. Now it is used to initially set the application or for
    * the "save as" functionality.
    */
   public void setApplication(OSApplication application)
   {
      m_data = application;
      if (getApplicationRef() != null &&
            !StringUtils.equals(getApplicationRef().getName(), application.getName()))
      {
         m_reference = null;
      }
      final Runnable updateUITask = new Runnable()
      {
         public void run()
         {
            setPartName(getApplication().getName());
            firePropertyChange(PROP_DIRTY);
         }
      };
      if (m_display == null)
      {
         // UI not initialized yet
         updateUITask.run();
      }
      else
      {
         m_display.asyncExec(updateUITask);
      }
   }

   /**
    * Returns legacy UI frame displaying application diagram.
    */
   public UIAppFrame getAppFrame()
   {
      return m_appFrame;
   }
   
   /**
    * Returns {@link CTabItem} index in the folder.
    */
   private int getItemIdx(final CTabItem item)
   {
      int idx = 0;
      for (final CTabItem curItem : getTabFolder().getItems())
      {
         if (item == curItem)
         {
            return idx;
         }
         idx++;
      }
      throw new IllegalArgumentException(PSMessages
            .getString("PSXmlApplicationEditor.tabNotFound")); //$NON-NLS-1$
   }

   /**
    * Tab folder containing all the application editor tabs.
    */
   CTabFolder getTabFolder()
   {
      return m_tabFolder;
   }
   
   /**
    * Returns editor control of the currently active page. Is
    * {@link IEditorPart} or {@link JInternalFrame}.
    */
   public Object getEditorControl()
   {
      return getTabFolder().getSelection().getData(EDITOR_TAB_PROPERTY);
   }

   private abstract class PSFrameProviderPanel extends JPanel
         implements
            PSFrameProvider
   {
   }

   private class AppTabFolderListener extends CTabFolder2Adapter
   {

      @Override
      public void close(CTabFolderEvent event)
      {
         if (event.item instanceof CTabItem)
         {
            final CTabItem item = (CTabItem) event.item;
            final int itemIdx = getItemIdx(item);
            if (itemIdx == 0)
            {
               // attempt to close first tab containing app diagram closes the
               // editor
               event.doit = false;
               getSite().getPage().closeEditor(PSXmlApplicationEditor.this,
                     true);
            }
            else if (item.getData(EDITOR_TAB_PROPERTY) instanceof PSWidgetCloseStrategy)
            {
               final PSWidgetCloseStrategy closeStrategy = (PSWidgetCloseStrategy) item
                     .getData(EDITOR_TAB_PROPERTY);
               // rely on MultiPageEditorPart.removePage(int) to actually close
               event.doit = false;
               if (closeStrategy.onClose())
               {
                  removePage(itemIdx);
               }
            }
         }
      }
   }
   
   /**
    * Base class implementation just returns name of the class. 
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(@SuppressWarnings("unused") Control control)
   {      
      int pageIndex = getActivePage();
      String postfix = pageIndex == 0 ? "main" : "pipe" ;
      return getClass().getName() + "_" + postfix;
   }
   
   /**
    * Generates stylesheet for the given stylesheet source on save.
    */
   public void updateOnSave(final URL sourceUrl, final URL stylesheetUrl,
         final OSResultPage dropData)
   {
      assert sourceUrl != null && dropData != null;
      assert !m_updateOnSave.containsKey(sourceUrl)
            || m_updateOnSave.get(sourceUrl).equals(dropData);
      m_updateOnSave.put(sourceUrl, dropData);
      m_updateOnSaveStylesheets.put(sourceUrl, stylesheetUrl);
   }
   
   public Display getDisplay()
   {
      return m_display;
   }
   
   /**
    * The reference to the application returned by {@link #getApplication()}
    * Can be <code>null</code> if application is imported.
    */
   public PSReference getApplicationRef()
   {
      return (PSReference) getReference();
   }
   
   /**
    * Set the reference to the object that this editor edits.
    * 
    * @param ref the new reference to the application to be edited, not 
    *    <code>null</code>.
    */
   public void setApplicationRef(PSReference ref)
   {
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");
      
      m_reference = ref;
   }
   
   
   /* (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#load(
    *    com.percussion.client.IPSReference)
    */
   @Override
   protected void load(IPSReference ref) throws Exception
   {
      try
      {
         super.load(ref);
      }
      catch (PSAuthorizationException e)
      {
         PSModelTracker tracker = PSModelTracker.getInstance();
         
         if (PSWorkbenchPlugin.getDefault().getPreferences()
            .isShowWarningForReadOnlyObjects())
         {
            String title = PSMessages.getString(
               "PSEditorBase.noUpdatePermission.title");
            String msg = PSMessages.getString(
               "PSEditorBase.noUpdatePermission.message");
            MessageDialog.openInformation(getSite().getShell(), title, msg);
         }
         
         m_data = tracker.load(ref, false, false, true);
         m_isReadOnlyMode = true;
      }
   }

   /**
    * Runs {@link #cleanUpClosedFiles()} in SWT event thread. 
    */
   private void launchCleanUpClosedFiles()
   {
      m_display.asyncExec(new Runnable()
      {
         public void run()
         {
            try
            {
               cleanUpClosedFiles();
               assert getApplicationRef().isPersisted();
            }
            catch (Exception e)
            {
               PSDlgUtil.showError(e);
            }
         }
      });
   }

   /**
    * Tab property name pointing to the actual editor control stored in the tab.
    */
   private static final String EDITOR_TAB_PROPERTY = PSXmlApplicationEditor.class
         .getName()
         + " - tab frame:"; //$NON-NLS-1$

   /**
    * Tab property name pointing to the resource open in the tab. The property
    * can contain <code>null</code> if there is no resource associated with
    * the tab.
    */
   private static final String RESOURCE_TAB_PROPERTY = PSXmlApplicationEditor.class
         .getName()
         + " - resource:"; //$NON-NLS-1$

   /**
    * Parent display.
    */
   private Display m_display;

  /**
    * Main application frame.
    */
   private UIAppFrame m_appFrame;
   
   /**
    * Tab folder containing all the application editor tabs.
    */
   private CTabFolder m_tabFolder;
   
   /**
    * Map containing resources already loaded from application.
    */
   private Map<URL, IFile> m_loadedResources = new HashMap<URL, IFile>();
   
   /**
    * Drop data should be applied on save.
    */
   private Map<URL, OSResultPage> m_updateOnSave = new HashMap<URL, OSResultPage>();

   /**
    * Update on save stylesheets. Keys - stylesheet source resource URLs, values -
    * stylesheet resource URLs.
    */
   private Map<URL, URL> m_updateOnSaveStylesheets = new HashMap<URL, URL>();
   
   /**
    * Provides some file editing functionality.
    */
   private final PSFileEditorHelper m_fileEditorHelper = new PSFileEditorHelper();
   
   /**
    * Flag used to make sure that dummy page is added only once.
    */
   private boolean m_page0IsDummy;
   
   /**
    * There is no way for us to find out what option the user has choosen
    * while a dirty editor was closed. This flag is set to <code>true</code>
    * as long as the <code>doSave</code> method is active. It is needed to 
    * wait in the disposer thread until the save is done.
    * <p>
    * Note: we cannot use <code>SwingUtilities.invokeAndWait</code> in the 
    * <code>doSave</code> method because that will lockup the UI in certain 
    * cases.
    */
   private boolean m_doSave = false;
}

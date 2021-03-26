/******************************************************************************
 *
 * [ PSFileEditorTracker.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.model;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSCompletionProvider;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is used to manage design objects that are edited as file content.
 * To do this, this class copies the data from the server, placing it in a 
 * temporary file on the local file system, then opening an editor on this
 * local content. When the editor is finished, this class then reverses the
 * process.
 *
 * @author paulhoward
 */
public class PSFileEditorTracker
{
   /**
    * Used to retrieve the single instance of this class.
    * 
    * @return Never <code>null</code>.
    */
   synchronized public static PSFileEditorTracker getInstance()
   {
      if (ms_instance == null)
      {
         try
         {
            ms_instance = new PSFileEditorTracker();
         }
         catch (PSModelException e)
         {
            throw new RuntimeException(e);
         }
      }
      return ms_instance;
   }
   
   /**
    * Returns <code>true</code> if {@link #getInstance()} already was called,
    * and the singleton instance.
    * Normally is not needed.
    * Can be used e.g. in cleanup code to avoid initializing the instance if
    * it is not initialized yet.
    */
   public static boolean isInitialized()
   {
      return ms_instance != null;
   }
   
   /**
    * Adds the supplied ref to the set of objects managed by this class. If
    * errors occur during data transfer, messages are displayed to the user.
    * 
    * @param ref Never <code>null</code>. If it is already registered, an
    * exception is thrown.
    * 
    * @param file A resource that will be used as the transfer mechanism for
    * getting data from the object to the editor and back. Never
    * <code>null</code>. If it refers to an existing file, the contents are
    * cleared before the object's data is transferred, otherwise, a file is
    * created.
    * 
    * @param page The page that contains the editor being tracked. If this is
    * provided, this class will track the lifecycle of the editor and listen for
    * changes to the project for the file associated with the ref and copy the
    * content from the file to the object and save it when appropriate. It will
    * also perform proper cleanup when the editor is closed. If not provided,
    * the caller is responsible for calling {@link #save(IPSReference, boolean)}
    * or {@link #unregister(IPSReference)} appropriately.
    * 
    * @throws IllegalStateException If ref is already registered.
    */
   public boolean register(final IPSReference ref, IFile file,
         IWorkbenchPage page)
   {
      if (null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      if (null == file)
      {
         throw new IllegalArgumentException("file cannot be null");  
      }
      
      boolean successful = false;
      PSMimeContentAdapter content = null;
      try
      {
         Info data = new Info();
         data.m_content = file;
         data.m_ref = ref;
         data.m_page = page;


         ms_logger.debug("PSFileEditorTracker: registering "
               + ref.getName() + (page == null ? " system" : " internal"));

         //the order of these 2 is important for cleanup in case of error
         content = loadContent(ref);
         m_registrations.add(data);
         
         if (!file.exists())
         {
            String[] parts = file.getFullPath().segments();
            IFolder folder = file.getProject().getFolder(parts[1]);
            if (!folder.exists())
               folder.create(true, true, null);
            for (int i=2; i < parts.length-1; i++)
            {
               folder = folder.getFolder(parts[i]);
               if (!folder.exists())
                  folder.create(true, true, null);
            }
            file.create(content.getContent(), true, null);
         }
         else
         {
            IProgressMonitor monitor = null;
            if (data.m_page instanceof IViewSite)
            {
               IStatusLineManager statusLine = ((IViewSite) data.m_page)
                     .getActionBars().getStatusLineManager();
               monitor = statusLine.getProgressMonitor();
            }
            file.setContents(content.getContent(), true, true, monitor);
         }
         
         if (page != null)
         {
            data.m_partListener = new IPartListener()
            {
               /**
                * Unregisters the ref.
                * @param part Never <code>null</code>.
                */
               public void partClosed(IWorkbenchPart part)
               {
                  if (!ref.getName().equalsIgnoreCase(part.getTitle()))
                  {
                     return;
                  }
                  
                  try
                  {
                     unregister(ref);
                  }
                  catch (Exception e)
                  {
                     PSUiUtils.handleExceptionSync(
                        "Saving IFile content on editor close", null, null, e);
                  }
               }

               /**
                * See IPartListener interface. Not used.
                */
               @SuppressWarnings("unused")
               public void partActivated(IWorkbenchPart part)
               {}

               /**
                * See IPartListener interface. Not used.
                */
               @SuppressWarnings("unused")
               public void partBroughtToTop(IWorkbenchPart part)
               {}

               /**
                * See IPartListener interface. Not used.
                */
               @SuppressWarnings("unused")
               public void partDeactivated(IWorkbenchPart part)
               {}

               /**
                * See IPartListener interface. Not used.
                */
               @SuppressWarnings("unused")
               public void partOpened(IWorkbenchPart part)
               {}
            };
            page.addPartListener(data.m_partListener);
            
            
            IWorkspace ws = ResourcesPlugin.getWorkspace();
            data.m_resourceListener = new IResourceChangeListener()
            {
               /**
                * Copy the data from the file to the ref's data and save it.
                */
               public void resourceChanged(IResourceChangeEvent event)
               {
                  assert(event.getType() == IResourceChangeEvent.POST_CHANGE);
                  
                  try
                  {
                     Collection<IPath> affectedPaths = new ArrayList<>();
                     IResourceDelta[] d = event.getDelta().getAffectedChildren(
                           IResourceDelta.CHANGED);
                     if (d.length == 0)
                        return;
                     getAffectedRefs(d, affectedPaths);
                     for (IPath affectedPath : affectedPaths)
                     {
                        Info info = getData(ref);
                        if (null!=info && info.m_content.getFullPath().equals(affectedPath))
                        {
                           save(info.m_ref, false);
                        }
                     }
                  }
                  catch (Exception e)
                  {
                     PSUiUtils.handleExceptionSync("Saving IFile content", null,
                           null, e);
                  }
               }

               private void getAffectedRefs(
                     IResourceDelta[] deltas, Collection<IPath> results)
               {
                  for (IResourceDelta delta : deltas)
                  {
                     if (delta.getAffectedChildren(
                           IResourceDelta.CHANGED).length == 0)
                     {
                        results.add(delta.getFullPath());
                     }
                     else
                     {
                        getAffectedRefs(delta
                              .getAffectedChildren(IResourceDelta.CHANGED),
                              results);
                     }
                  }
               }
            };
               
            ws.addResourceChangeListener(data.m_resourceListener,
                  IResourceChangeEvent.POST_CHANGE);
         }
         successful = true;
      }
      catch (CoreException e)
      {
         PSUiUtils.handleExceptionSync("Writing local file", null, null, e);
      }
      catch (ClassCastException e)
      {
         String title = PSMessages
               .getString("PSFileEditorTracker.unsupportedDataType.title");
         String msg = PSMessages
               .getString("PSFileEditorTracker.unsupportedDataType.message");
         MessageDialog.openError(PSUiUtils.getShell(), title, msg);
      }
      catch (Exception e)
      {
         PSUiUtils.handleExceptionSync("Reading object file content", null,
               null, e);
      }
      finally
      {
         if (!successful && content != null)
         {
            try
            {
               unregister(ref);
            }
            catch (Exception e)
            {
               //ignore and log
               ms_logger.error("Failed to unregister after exception.", e);
            }
         }
      }
      return successful;
   }

   /**
    * Loads the data for the supplied reference from its model. If the data
    * is locked in another session by this user, the user is asked if they
    * want to override the lock.
    * 
    * @param ref Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws Exception If any problems loading it.
    */
   private PSMimeContentAdapter loadContent(IPSReference ref)
      throws Exception
   {
      try
      {
         return (PSMimeContentAdapter) PSModelTracker
               .getInstance().load(ref, true, false, true);
      }
      catch (Exception e)
      {
         if (e instanceof PSLockedException)
         {
            if (PSCoreFactory.getInstance().getLockHelper()
                  .isLockedToMeElsewhere(ref))
            {
               String title = PSMessages
                  .getString("PSFileEditorTracker.lockedInAnotherSession.title");
               String msg = PSMessages
                  .getString("PSFileEditorTracker.lockedInAnotherSession.message");
               if (MessageDialog.openQuestion(PSUiUtils.getShell(), title, msg))
               {
                  return (PSMimeContentAdapter) PSModelTracker
                     .getInstance().load(ref, true, true, true);
               }
            }
         }
         throw e;
      }
      
   }

   /**
    * A convenience method that calls
    * {@link #register(IPSReference, IFile, IWorkbenchPage) 
    * register(ref, file, null)}.
    */
   public void register(IPSReference ref, IFile file)
      throws Exception
   {
      register(ref, file, null);
   }
   
   /**
    * Releases the lock on the ref and removes it from the list of tracked
    * objects.
    * 
    * @param ref Never <code>null</code>. If the ref is not registered, returns
    * silently.
    * 
    * @throws Exception If any problems communicating with the server.
    */
   public void unregister(IPSReference ref)
      throws Exception
   {
      if (null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      ms_logger.debug("PSFileEditorTracker: Unregistering " + ref.getName());
      Info data = getData(ref);
      if (data == null)
         return;
      
      if (data.m_page != null)
      {
         //these may not have been set if an error occurred
         if (data.m_resourceListener != null)
         {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                  data.m_resourceListener);
         }
         if (data.m_partListener != null)
         {
            data.m_page.removePartListener(data.m_partListener);
         }
      }
      assert(data.m_content != null);
      if (data.m_content.exists())
         data.m_content.delete(true, null);
      m_registrations.remove(data);
      PSModelTracker.getInstance().releaseLock(ref, true);
   }

   /**
    * Opens the given editor on the selected file. If an editor is already open,
    * it is brought to the top. If conditions are such that an editor is not
    * allowed to be opened (e.g. trying to open an external editor while the
    * content is open in an internal editor) a message is displayed to the user.
    * 
    * @param page The container fof the created editor. Never <code>null</code>.
    * 
    * @param desc The editor descriptor, or <code>null</code> for a default
    * editor, or the system editor if an internal editor is not available.
    * 
    * @param file A handle to the content, never <code>null</code>. It is
    * assumed to exist.
    * 
    * @param ref The design object handle that owns the content.
    * 
    * @return <code>true</code> if the editor was open or activated,
    * <code>false</code> otherwise.
    */
   public boolean openEditor(IWorkbenchPage page, IEditorDescriptor desc,
         IFile file, IPSReference ref)
   {
      if (null == file)
      {
         throw new IllegalArgumentException("file cannot be null");  
      }

      IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
      if (desc == null)
      {
         desc = registry.findEditor("org.eclipse.ui.DefaultTextEditor");
         if (desc == null)
         {
            desc = registry
                  .findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
         }
      }
      
      String editorId = desc.getId();
      boolean systemEditor = desc.isOpenExternal();
      
      if ((!systemEditor && isRegisteredForEdit(ref, true)) 
            || (systemEditor && isRegisteredForEdit(ref, false)))
      {
         String title = PSMessages
            .getString("PSFileEditorTracker.warning.cantOpenEditor.title");
         String msg = PSMessages
            .getString("PSFileEditorTracker.warning.cantOpenEditor.message");
         MessageDialog.openInformation(PSUiUtils.getShell(), title, msg);
         return false;
      }
      
      if (page.findEditor(new FileEditorInput(file)) == null)
      {
         if (!register(ref, file, systemEditor ? null : page))
         {
            return false;
         }
      }
      boolean result = false;
      try
      {
         // refactoring alert. See comments for m_completionProvider
         m_completionProvider.attachVelocityEditorCompletionData(file);
         IEditorInput editorInput = new FileEditorInput(file);
         page.openEditor(editorInput, editorId);
         // only remember the default editor if the open succeeds
         IDE.setDefaultEditor(file, editorId);
         result = true;
      }
      catch (PartInitException e)
      {
         PSWorkbenchPlugin.handleException("Opening editor", null, null, e);
      }
      catch (CoreException e)
      {
         PSWorkbenchPlugin.handleException("Opening editor", null, null, e);
      }
      finally
      {
         if (!result)
         {
            try
            {
               unregister(ref);
            }
            catch (Exception e)
            {
               //ignore and log
               String msg = PSMessages
                  .getString("PSFileEditorTracker.error.unregisterFailed.message");
               PSWorkbenchPlugin.handleException(
                     "Unregistering file after error", null, msg, e);
            }
         }
      }
      return result;
   }

   /**
    * Checks if the supplied reference is currently open for editing in an
    * external editor.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @param withPage If <code>true</code>, check for external editors, otherwise,
    * check for internal editors.
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isRegisteredForEdit(IPSReference ref, boolean withPage)
   {
      if (null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      Collection<PSPair<IPSReference, IFile>> openEditors =
         PSFileEditorTracker.getInstance().getRegisteredReferences(!withPage);
      for (PSPair<IPSReference, IFile> p : openEditors)
      {
         if (p.getFirst().equals(ref))
            return true;
      }
      return false;
   }

   /**
    * Attempts to find a matching 'instance' node from the view model that wraps
    * the supplied ref. If one is found, {@link #getFileResource(PSUiReference)}
    * is called with that node. It is preferable that the other form of this
    * method is called.
    * 
    * @throws RuntimeException If the node is not found.
    */
   public IFile getFileResource(IPSReference ref)
   {
      PSUiReference editNode = null;
      Collection<PSUiReference> nodes = PSDesignObjectHierarchy.getInstance().getNodes(ref);
      for (PSUiReference node : nodes)
      {
         if (!node.isReference())
            editNode = node;
      }
      
      if (editNode == null)
      {
         throw new RuntimeException("Couldn't find matching node.");
      }
      return getFileResource(editNode);
   }
   
   /**
    * Creates an Eclipse resource that matches the path of the supplied node.
    * The name of the node is used rather than the display label because many
    * labels don't include the file extension.
    * 
    * @param node Never <code>null</code>.
    * 
    * @return The returned file is not checked in any way, including whether 
    * the ancestor folders exist or the file exists. Never <code>null</code>.
    */
   public IFile getFileResource(PSUiReference node)
   {
      IProject project = PSWorkbenchPlugin.getDefault().getProject();
      //the display label of an object may not be the actual name, so correct it
      String path = node.getPath();
      path = path.substring(0, path.lastIndexOf("/")+1);
      path += node.getName();
      
      return project.getFile(path);
   }
   
   /**
    * Finds the registration data for the supplied ref.
    *  
    * @param ref Assumed not <code>null</code>.
    * 
    * @return If not registered, <code>null</code> is returned.
    */
   private Info getData(IPSReference ref)
   {
      for (Info info : m_registrations)
      {
         if (info.m_ref.equals(ref))
            return info;
      }
      return null;
   }

   /**
    * Copies the content from the local file associated with the supplied reference
    * to the data object associated with the supplied ref and saves it. If the
    * flag is <code>true</code>, then the {@link #unregister(IPSReference)}
    * method is called as well.
    * 
    * @param ref Never <code>null</code>. If not registered, an exception is 
    * thrown.
    * 
    * @param unregister <code>true</code> to call {@link #unregister(IPSReference)},
    * false if you want to continue tracking this object.
    * 
    * @throws IllegalStateException If ref is not registered.
    * @throws Exception If any problems w/ server.
    */
   public void save(IPSReference ref, boolean unregister)
      throws Exception
   {
      Info data = getData(ref);
      if (data == null)
      {
         throw new IllegalStateException("Supplied ref is not registered: "
               + ref.getName());
      }
      
      ms_logger.debug("PSFileEditorTracker: Saving " + ref.getName());
      InputStream is = null;
      try
      {
         is = data.m_content.getContents(true);
         if (is == null)
         {
            //shouldn't happen, but doc is unclear
            throw new RuntimeException("No content found for " + ref.getName());
         }
         
         PSMimeContentAdapter target = (PSMimeContentAdapter) 
               PSModelTracker.getInstance().load(ref, true);
         target.setContent(is);
         PSModelTracker.getInstance().save(ref, false, true);
         
         if (unregister)
            unregister(ref);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException e)
            {
               //ignore
            }
         }
      }
   }
   
   /**
    * Returns all currently registered objects that were registered with a
    * workbench page.
    * 
    * @param withPage A flag to control which registrations to return. If
    * <code>true</code>, then those objects registered with a page are
    * returned, otherwise, those w/o a page are returned.
    * 
    * @return Never <code>null</code>. The caller takes ownership of the
    * returned set. A list is returned for the convenience of the caller, the
    * entries are in no particular order.
    */
   public List<PSPair<IPSReference, IFile>> getRegisteredReferences(
         boolean withPage)
   {
      List<PSPair<IPSReference, IFile>> results = 
         new ArrayList<PSPair<IPSReference, IFile>>();
      for (Info info : m_registrations)
      {
         if ((withPage && info.m_page != null)
               || (!withPage && info.m_page == null))
         {
            results.add(new PSPair<IPSReference, IFile>(info.m_ref,
                  info.m_content));
         }
      }
      return results;
   }

   /**
    * Private to implement singelton pattern. Use {@link #getInstance()}.
    * @throws PSModelException when data loading from model failed.
    */
   private PSFileEditorTracker() throws PSModelException
   {
      //@todo ph: this should be done lazily
      m_completionProvider = new PSCompletionProvider();
   }

   /**
    * A structure to group information into a single unit for storing in a 
    * collection. See {@link #m_registrations}.
    */
   private class Info
   {
      public IPSReference m_ref;
      public IWorkbenchPage m_page;
      public IFile m_content;
      public IPartListener m_partListener;
      public IResourceChangeListener m_resourceListener;
   }
   
   /**
    * Contains all registered objects. Never <code>null</code>. We don't use a
    * map because the name of the ref could change on the fly which would screw
    * up the hash map.
    */
   private Collection<Info> m_registrations = new ArrayList<Info>();
   
   /**
    * The only instance. Created lazily in {@link #getInstance()}.
    */
   private static PSFileEditorTracker ms_instance;
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static final Logger ms_logger = LogManager.getLogger(PSFileEditorTracker.class);
   
   /**
    * Manages variable, method, field completions. Initialized in constructor.
    * Never <code>null</code> after that.
    *
    * <bAttention:</b> (Andriy) ideally file editor tracker should not know
    * about velocity completions. I ended up with calling it here
    * because Eclipse does not provide standard extension point or listener API
    * to be notified right before an editor is open. 
    * Trouble of implementing generic solution (e.g. adding listeners to file
    * tracker) is not worth the effort in this particular case.
    * Refactor if we need to handle more situations like this. 
    */
   private final PSCompletionProvider m_completionProvider;
}

/******************************************************************************
 *
 * [ PSUiUtils.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Various workbench ui helper methods
 * @author erikserating
 *
 */
public class PSUiUtils
{
   /**
    * Gets the index for a list of <code>IPSReference</code> objects.
    * The index for the reference whose guid matches the passed in guid
    * is returned. 
    * @param guid the guid to find in the list, cannot be <code>null</code>
    * @return the index for the reference containing the specified guid, or
    * -1 if not found.
    */
   public static final int getReferenceIndexByGuid(
      final List<IPSReference> list, IPSGuid guid)
   {
      if(list == null)
         throw new IllegalArgumentException("list cannot be null.");
      if(guid == null)
         throw new IllegalArgumentException("name cannot be null.");
      int count = -1;
      for(IPSReference ref : list)
      {
         ++count;
         if(ref == null)
            continue;
         if(guid.equals(ref.getId()))
            return count;
      }
      return -1;
   }
   
   /**
    * Gets the index for a list of <code>IPSReference</code> objects.
    * The index for the reference whose guid contains the passed in id
    * is returned. 
    * @param id the id to find in the list
    * @return the index for the reference containing the specified id, or
    * -1 if not found.
    */
   public static final int getReferenceIndexById(
      final List<IPSReference> list, long id)
   {
      if(list == null)
         throw new IllegalArgumentException("list cannot be null.");      
      int count = -1;
      for(IPSReference ref : list)
      {
         ++count;
         if(ref == null)
            continue;
         IPSGuid guid = ref.getId();
         if(guid == null)
            continue;
         if(guid.longValue() == id)
            return count;
      }
      return -1;
   }
   
   /**
    * Gets the reference by guid for a list of <code>IPSReference</code> 
    * objects.
    * The reference whose guid matches the passed in guid
    * is returned. 
    * @param guid the guid to find in the list, cannot be <code>null</code>
    * @return the reference containing the specified guid, or
    * <code>null</code> if not found.
    */
   public static final IPSReference getReferenceByGuid(
      final Collection<IPSReference> coll, IPSGuid guid)
   {
      if(coll == null)
         throw new IllegalArgumentException("coll cannot be null.");
      if(guid == null)
         throw new IllegalArgumentException("name cannot be null.");
      
      for(IPSReference ref : coll)
      {
         if(ref == null)
            continue;
         if(guid != null && guid.equals(ref.getId()))
            return ref;
      }
      return null;
   }
   
   /**
    * Gets the reference by the id for a list of <code>IPSReference</code> 
    * objects.
    * The reference whose guid contains the passed in id
    * is returned. 
    * @param id the id to find in the list
    * @return the reference containing the specified id, or
    * <code>null</code> if not found.
    */
   public static final IPSReference getReferenceById(
      final Collection<IPSReference> coll, long id)
   {
      if(coll == null)
         throw new IllegalArgumentException("coll cannot be null.");      
      
      for(IPSReference ref : coll)
      {
         if(ref == null)
            continue;
         IPSGuid guid = ref.getId();
         if(guid == null)
            continue;
         if(guid.longValue() == id)
            return ref;
      }
      return null;
   }
   
   /**
    * Gets the index for a list of <code>IPSReference</code> objects.
    * The index for the reference whose name matches the passed in name
    * is returned. 
    * @param name the name to find in the list, cannot be <code>null</code> or
    * empty.
    * @return the index for the reference containing the specified name, or
    * -1 if not found.
    */
   public static final int getReferenceIndexByName(
      final List<IPSReference> list, String name)
   {
      if(list == null)
         throw new IllegalArgumentException("list cannot be null.");
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty.");
      int count = -1;
      for(IPSReference ref : list)
      {
         ++count;
         if(ref == null)
            continue;
         if(name.equals(ref.getName()))
            return count;
      }
      return -1;
   }
   
   /**
    * Gets the reference by name for a list of <code>IPSReference</code> 
    * objects.
    * The reference whose name matches the passed in name
    * is returned. 
    * @param name the name to find in the list, cannot be <code>null</code> or
    * empty.
    * @return the reference containing the specified name, or
    * <code>null</code> if not found.
    */
   public static final IPSReference getReferenceByName(
      final Collection<IPSReference> coll, String name)
   {
      if(coll == null)
         throw new IllegalArgumentException("coll cannot be null.");
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty.");
      
      for(IPSReference ref : coll)
      {
         if(ref == null)
            continue;
         if(name.equals(ref.getName()))
            return ref;
      }
      return null;
   }
   
   /**
    * Returns a collection of <code>IPSGuid</code> objects from a collection
    * of <code>IPSReference</code> object.
    * @param refs cannot be <code>null</code>, may be empty.
    * @return collection of <code>IPSGuid</code> object,
    * never <code>null</code>, may be empty.
    */
   public static final Collection<IPSGuid> guidCollectionFromRefCollection(
      Collection<IPSReference> refs)
   {
      if(refs == null)
         throw new IllegalArgumentException("refs cannot be null");
      Collection<IPSGuid> results = new ArrayList<IPSGuid>(refs.size());   
      for(IPSReference ref : refs)
         results.add(ref.getId());
      return results;
   }
   
   /**
    * Determines the table cell that the passed in event occurred on. 
    * @param table the table where the event occured, cannot be 
    * <code>null</code>.
    * @param x 
    * @param y
    * @return an <code>int</code> array with the row and column index values or
    * <code>null</code> if it could not be found.
    * <p>
    * <pre>
    * Array values:
    *    [0] = row index
    *    [1] = column index
    * </pre>
    * </p>
    */
   public static final int[] getTableCellIndex(
      final Table table, final int x, final int y)
   {
      if(table == null)
         throw new IllegalArgumentException("table cannot be null.");
      int row = -1;
      Point pt = new Point(x, y);
      for(TableItem item : table.getItems())
      {
         ++row;         
         int colCount = table.getColumnCount();
         for(int col = 0; col < colCount; col++)
         {
            Rectangle bounds = item.getBounds(col);
            if(bounds.contains(pt))
               return new int[]{row, col};
         }
      }
      return null;
   }
   
   /**
    * Determines the table cell that the passed in event occurred on. 
    * @param table the table where the event occured, cannot be 
    * <code>null</code>.
    * @param e the event that was captured, cannot be <code>null</code>.
    * @return an <code>int</code> array with the row and column index values or
    * <code>null</code> if it could not be found.
    * <p>
    * <pre>
    * Array values:
    *    [0] = row index
    *    [1] = column index
    * </pre>
    * </p>
    */
   public static final int[] getTableCellIndex(
      final Table table, final Event e)
   {
      return getTableCellIndex(table, e.x, e.y);
   }  
   
   /**
    * Determines if the specified name already exists for the object
    * type represented by the passed in <code>IPSCmsModel</code>
    * @param name the name to check, cannot be <code>null</code>.
    * @param model <code>IPSCmsModel</code> representing the object type to
    *  be checked, cannot be <code>null</code>.
    * @return <code>true</code> if name already exists.
    */
   public static final boolean doesObjectNameExist(
      final String name, final IPSCmsModel model)
   {
      if(name == null)
         throw new IllegalArgumentException("name cannot be null.");
      if(model == null)
         throw new IllegalArgumentException("model cannot be null.");
    
      try
      {
         Collection<IPSReference> refs = model.catalog(true);
         for(IPSReference ref : refs)
         {
            if(name.equals(ref.getName()))
               return true;
         }
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }     
      return false;
   }
   
   /**
    * Return the <code>Image</code> to be used when 
    * displaying an error. Do not call dispose on the returned image as
    * the dispose call will be handled by the system.
    * 
    * @return image  the error image
    */
   public static Image getErrorImage(Shell shell) {
       return getSWTImage(shell, SWT.ICON_ERROR);
   }

   /**
    * Return the <code>Image</code> to be used when 
    * displaying a warning. Do not call dispose on the returned image as
    * the dispose call will be handled by the system.
    * 
    * @return image  the warning image
    */
   public static Image getWarningImage(Shell shell) {
       return getSWTImage(shell,SWT.ICON_WARNING);
   }

   /**
    * Return the <code>Image</code> to be used when 
    * displaying information. Do not call dispose on the returned image as
    * the dispose call will be handled by the system.
    * 
    * @return image  the information image
    */
   public static Image getInfoImage(Shell shell) {
       return getSWTImage(shell,SWT.ICON_INFORMATION);
   }

   /**
    * Return the <code>Image</code> to be used when 
    * displaying a question. Do not call dispose on the returned image as
    * the dispose call will be handled by the system.
    * 
    * @return image  the question image
    */
   public static Image getQuestionImage(Shell shell) {
       return getSWTImage(shell,SWT.ICON_QUESTION);
   }

   /**
    * Get an <code>Image</code> from the provide SWT image
    * constant. Do not call dispose on the returned image as
    * the dispose call will be handled by the system.
    * 
    * @param imageID the SWT image constant
    * @return image  the image
    */
   public static Image getSWTImage(final Shell shell, final int imageID) {
       
       final Display display;
       
       if (shell == null) {
           display = Display.getCurrent();
       } else {
           display = shell.getDisplay();
       }

       final Image[] image = new Image[1];
       display.syncExec(new Runnable() {
           public void run() {
               image[0] = display.getSystemImage(imageID);
           }
       });

       return image[0];

   }
   
   /**
    * Scales an image to the specified width and height
    * @param shell the SWT <code>Shell</code>, cannot  be <code>null</code>.
    * @param image the image to modify, cannot be <code>null</code>.
    * @param width the width to rescale to. 
    * @param height the height to rescale to.
    * @return the modified <code>Image</code>.
    */
   public static Image scaleImage(
      Shell shell, Image image, int width, int height)
   {
      ImageData data = image.getImageData().scaledTo(width, height);
      Image scaledImage = new Image(shell.getDisplay(), data);
      return scaledImage;
   }
   
   /**
    * Returns an Eclipse platform shared image if found. This
    * image should not be disposed by the client. 
    * @param imageName the image key, these are part of the 
    * <code>ISharedImages</code> interface. Cannot be 
    * <code>null</code> or empty.
    * @return the image or <code>null</code> if not found.
    * @see #getSharedImageDescriptor(String)
    */
   public static Image getSharedImage(String imageName)
   {
      if(StringUtils.isBlank(imageName))
         throw new IllegalArgumentException("imageName cannot be null or empty.");
      ISharedImages mgr = 
         PSWorkbenchPlugin.getDefault().getWorkbench().getSharedImages();
      return mgr.getImage(imageName);
   }
   
   /**
    * Returns an Eclipse platform shared image descriptor, if found.
    * 
    * @param imageName the image key, these are part of the
    * <code>ISharedImages</code> interface. Cannot be <code>null</code> or
    * empty.
    * @return The descriptor or <code>null</code> if not found.
    * @see #getSharedImage(String)
    */
   public static ImageDescriptor getSharedImageDescriptor(String imageName)
   {
      if(StringUtils.isBlank(imageName))
         throw new IllegalArgumentException("imageName cannot be null or empty.");
      ISharedImages mgr = 
         PSWorkbenchPlugin.getDefault().getWorkbench().getSharedImages();
      return mgr.getImageDescriptor(imageName);
   }
   
   /**
    * Convenience method that just prepends the supplied path with "icons/" and
    * calls {@link #getImageDescriptor(String)}.
    * 
    * @param imagepath The path of the image relative to the plugin's icon
    * directory. Never <code>null</code> or empty.
    */
   public static ImageDescriptor getImageDescriptorFromIconsFolder(
         String imagepath)
   {
      if (StringUtils.isBlank(imagepath))
      {
         throw new IllegalArgumentException("imagePath cannot be null or empty");  
      }
      
      return getImageDescriptor("icons/" + imagepath);
   }
   
   /**
    * Returns a descriptor of an image that is located at the provided path.
    * 
    * @param imagePath the path to the image, never <code>null</code> or
    * empty.
    * @return the image descriptor or <code>null</code> if the image was not
    * found.
    */
   public static ImageDescriptor getImageDescriptor(String imagePath)
   {
      if (StringUtils.isBlank(imagePath))
      {
         throw new IllegalArgumentException("imagePath cannot be null or empty");  
      }
      
      Path path = new Path(imagePath);

      if (PSWorkbenchPlugin.getDefault() == null)
      {
         // happens during unit tests
         return ImageDescriptor.getMissingImageDescriptor();
      }
      final URL url = PSWorkbenchPlugin.getDefault().find(path);
      if(url != null)
      {
         return ImageDescriptor.createFromURL(url);
      }
      return null;
   }
   
   
   /**
    * Attempts to retrieve the shell of the active window if it exists,
    * and if not then gets the shell from the plugin's array of
    * windows and then returns the root shell.
    * @return a shell for this plugin, never <code>null</code>.
    */
   public static Shell getShell()
   {
      IWorkbench workbench = PSWorkbenchPlugin.getDefault().getWorkbench();
      final Display display = workbench.getDisplay();
      final Shell[] shell = new Shell[1];
      display.syncExec(new Runnable()
         {

            public void run()
            {
               Shell theshell = display.getActiveShell();
               if(theshell != null)
               {
                  shell[0] = theshell;
                  return;
               }
               Shell[] allShells = display.getShells();
               if(allShells.length > 0)
               {
                  shell[0] = getRootShell(allShells[0]);
                  return;
               }
               // if all else fails then create a new shell
               shell[0] = new Shell(display);
               return;
               
            }
         
         });
      return shell[0];
      
   }
   
   
   /**
    * Returns the root shell of the shell passed in or itself if
    * it is the root shell.
    * @param shell cannot be <code>null</code>
    * @return the root shell, never <code>null</code>.
    */
   public static Shell getRootShell(final Shell shell)
   {
      if(shell.getParent() == null)
         return shell;
      return getRootShell(shell.getParent().getShell());
   }
   
   /**
    * @return the plugin's log if it exists else a new log
    * is created.
    */
   public static ILog getLog()
   {
      return PSWorkbenchPlugin.getDefault().getLog();
   }
   
   /**
    * Convenience method that calls
    * {@link #log(String, Throwable) log(msg, <code>null</code>)}.
    * 
    * @param msg If <code>null</code> or empty, returns immediately.
    */
   public static void log(String msg)
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
   public static void log(String msg, Throwable e)
   {
      if (StringUtils.isEmpty(msg))
         return;
      PSWorkbenchPlugin.getDefault().log(msg, e);
   }
   
   /**
    * Opens the specified view if it exists
    * @param viewId the id for the view, cannot be <code>null</code>
    * or empty.
    * @throws ExecutionException upon an error.
    */
   public static void openView(final String viewId) 
      throws ExecutionException 
   {
      if(StringUtils.isBlank(viewId))
         throw new IllegalArgumentException("viewId cannot be null or empty.");
      final IWorkbenchWindow activeWorkbenchWindow = PlatformUI
            .getWorkbench().getActiveWorkbenchWindow();
      if (activeWorkbenchWindow == null) {
         return;
      }

      final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
      if (activePage == null) {
         return;
      }

      try {
         activePage.showView(viewId);
      } catch (PartInitException e) {
         throw new ExecutionException("Part could not be initialized", e); //$NON-NLS-1$
      }
   }
   
   /**
    * Finds and returns the specified view if possible.
    * @param viewId the id for the view, cannot be <code>null</code>
    * or empty.
    * @return the <code>IViewPart</code> or <code>null</code> if
    * not found.
    */
   public static IViewPart findView(final String viewId)
   {
      if(StringUtils.isBlank(viewId))
         throw new IllegalArgumentException("viewId cannot be null or empty.");
      final IWorkbenchWindow activeWorkbenchWindow = PlatformUI
            .getWorkbench().getActiveWorkbenchWindow();
      if (activeWorkbenchWindow == null) {
         return null;
      }

      final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
      if (activePage == null) {
         return null;
      }      
      return activePage.findView(viewId);
   }
   
   /**
    * Determines if the passed in workbench part is visible.
    * @param part the part in question, cannot be <code>null</code>.
    * @return <code>true</code> if the part is visible.
    */
   public static boolean isPartVisible(final IWorkbenchPart part)
   {
      if(part == null)
         throw new IllegalArgumentException("part cannot be null.");
      final IWorkbenchWindow activeWorkbenchWindow = PlatformUI
            .getWorkbench().getActiveWorkbenchWindow();
      if (activeWorkbenchWindow == null) {
         return false;
      }

      final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
      if (activePage == null) {
         return false;
      }      
      return activePage.isPartVisible(part);
   }
   
   /**
    * Brings specified workbench part to the top.
    * @param part the part in question, cannot be <code>null</code>.
    */
   public static void bringToTop(final IWorkbenchPart part)
   {
      if(part == null)
         throw new IllegalArgumentException("part cannot be null.");
      final IWorkbenchWindow activeWorkbenchWindow = PlatformUI
            .getWorkbench().getActiveWorkbenchWindow();
      if (activeWorkbenchWindow == null) {
         return ;
      }

      final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
      if (activePage == null)      {
         return ;
      }
      activePage.bringToTop(part);
   }
   
   /**
    * Retrieves the plugin's global dialog settings object and then
    * returns the section that matches the name of the class passed
    * in. If the section does not exist, it is created. The global dialog
    * settings object actually gets persisted when the workbench closes.
    * @param clazz the class that have its classname used for the name
    * of the section key. Cannot be <code>null</code>.
    * @return the <code>IDialogSettings</code> object (section) that
    * matches the class name passed in. Never <code>null</code>, but
    * may be empty.
    */
   public static IDialogSettings getDialogSettings(
         @SuppressWarnings("unchecked") Class clazz)
   {
      if(clazz == null)
         throw new IllegalArgumentException("clazz cannot be null.");
      IDialogSettings globalSettings = 
         PSWorkbenchPlugin.getDefault().getDialogSettings();
      IDialogSettings settings = 
         globalSettings.getSection(clazz.getName());
      if(settings == null)
         settings = globalSettings.addNewSection(clazz.getName());
      return settings;
   }

   /**
    * Tests the supplied <code>name</code> for the following:
    * <ol>
    *    <li>non-blank</li>
    *    <li>doesn't match any name in the existingNames set</li>
    *    <li>validates for characters based on the supplied flag</li>
    * <ol>
    *
    * 
    * @param name The text to test. May be anything.
    * 
    * @param existingNames If lower-cased <code>name</code> matches any entry
    * in this set, case-sensitive, it is considered a validation failure.
    * 
    * @param hierarchyName If <code>true</code>, the
    * {@link PSCoreUtils#isValidHierarchyName(String)} method is called,
    * otherwise, {@link PSCoreUtils#isValidObjectName(String)} is called.
    * 
    * @return Either <code>null</code>, indicating the supplied name is
    * valid, or an internationalized string containing an appropriate message
    * describing the problem.
    */
   public static String validateObjectName(String name, 
         Collection<String> existingNames, boolean hierarchyName)
   {
      String key = null;
      
      if (StringUtils.isBlank(name))
      {
         key = "common.validation.emptyName";
      }
      else if (!hierarchyName && !PSCoreUtils.isValidObjectName(name))
      {
         key = "common.validation.noSpaceInName";
      }
      else if ((hierarchyName && !PSCoreUtils.isValidHierarchyName(name)))
      {
         key = "common.validation.invalidCharacters";
      }
      else if (existingNames.contains(name.toLowerCase()))
      {
         key = "common.validation.duplicateName";
      }
      
      return key == null ? null : PSMessages.getString(key);
   }
   
   /**
    * Checks the supplied set and its content for nulls.
    * 
    * @param objects If <code>null</code>, will fail validation.
    * 
    * @param throwEx If <code>true</code>, an
    * <code>IllegalArgumentException</code> is thrown if <code>nodes</code>
    * is <code>null</code> or any of its entries are <code>null</code>.
    * 
    * @return If <code>throwEx</code> is <code>false</code>,
    * <code>true</code> is returned if an exception would have been thrown.
    */
   public static boolean isValidCollection(Collection<? extends Object> objects, 
         boolean throwEx)
   {
      if (objects == null)
      {
         if (throwEx)
         {
            throw new IllegalArgumentException("set cannot be null");
         }
         return true;
      }
      
      for (Object o : objects)
      {
         if (o == null)
         {
            if (throwEx)
            {
               throw new IllegalArgumentException("set entries cannot be null");
            }
            return true;
         }
      }
      return false;
   }

   /**
    * Checks that the supplied array is not <code>null</code> and that none of
    * its entries are <code>null</code>. 
    * 
    * @param objects If <code>null</code>, will fail validation.
    * 
    * @param throwEx If <code>true</code>, an
    * <code>IllegalArgumentException</code> is thrown if <code>objects</code>
    * is <code>null</code> or any of its entries are <code>null</code>.
    * 
    * @return If <code>throwEx</code> is <code>false</code>,
    * <code>true</code> is returned if the array is valid as previously 
    * described.
    */
   public static boolean isValidArray(Object[] objects, boolean throwEx)
   {
      if (objects == null)
      {
         if (throwEx)
         {
            throw new IllegalArgumentException("array cannot be null");
         }
         return false;
      }
      
      for (Object o : objects)
      {
         if (o == null)
         {
            if (throwEx)
            {
               throw new IllegalArgumentException("array entries cannot be null");
            }
            return false;
         }
      }
      return true;
   }
   
   /**
    * Get the image with specified path (like 'icons/community16.gif') from the
    * image registry. Loads if not already loaded. If the image with specified
    * path does not exist it returns the default which is with the path
    * "icons/unknown16.gif".
    * 
    * @param key icon path, if <code>null</code>, the default is returned.
    * @return the image as explained above or <code>null</code> if one does
    * not exist and default does not exist.
    */
   static public Image getImage(String key)
   {
      String UNKNOWN_ICON_PATH = "icons/unknown16.gif";
      if(StringUtils.isEmpty(key))
         key = UNKNOWN_ICON_PATH;

      Image icon = JFaceResources.getImage(key);
      if (icon == null)
      {
         // register 1st time
         ImageDescriptor desc = PSWorkbenchPlugin.getImageDescriptor(key);
         if (desc == null)
         {
            if (key.equals(UNKNOWN_ICON_PATH))
               return icon;
            icon = JFaceResources.getImage(UNKNOWN_ICON_PATH);
            if (icon == null)
            {
               icon = PSWorkbenchPlugin.getImageDescriptor(UNKNOWN_ICON_PATH)
                  .createImage(true);
            }
         }
         else
            icon = desc.createImage(true);
         JFaceResources.getImageRegistry().put(key, icon);
      }
      return icon;
   }

   /**
    * Calls the workbench to display the exception. Can be called from any
    * thread. Does not return until the user has dismissed the dialog.
    * 
    * @param ctx The context where the op failed. Passed to the exception
    * handler which includes it in the log. May be <code>null</code> or empty.
    * 
    * @param titleKey The key used to retrieve the title text. May be
    * <code>null</code> or empty to use a default title.
    * 
    * @param msgKey The key used to retrieve the message text. May be
    * <code>null</code> or empty to use a default title.
    * 
    * @param e Never <code>null</code>.
    */
   public static void handleExceptionSync(final String ctx,
         final String titleKey, final String msgKey, final Throwable e)
   {
      Display.getDefault().syncExec(new Runnable()
      {
         @SuppressWarnings("synthetic-access")
         public void run()
         {
            if (e instanceof PSMultiOperationException
                  && StringUtils.isBlank(titleKey)
                  && StringUtils.isBlank(msgKey))
            {
               PSWorkbenchPlugin.handleException(ctx, null, null,e);
               return;
            }
            String title = null;
            if (!StringUtils.isBlank(titleKey))
            {
               title = PSMessages.getString(titleKey);
            }

            String msg = null;
            if (!StringUtils.isBlank(msgKey))
            {
               msg = PSMessages.getString(msgKey);
            }
            PSWorkbenchPlugin.handleException(ctx, title, msg, e);
         }
      }); 
   }

   /**
    * Calls the workbench to display the exception. Can be called from any
    * thread. Does not return until the user has dismissed the dialog.
    * 
    * @param titleKey The key used to retrieve the title text. May be
    * <code>null</code> or empty to use a default warning title.
    * 
    * @param msgKey The key used to retrieve the message text. Never
    * <code>null</code> or empty.
    */
   public static void displayWarningMessage(String titleKey,
         final String msgKey)
   {
      if (StringUtils.isBlank(msgKey))
      {
         throw new IllegalArgumentException("msgKey cannot be null or empty");  
      }
      
      if (StringUtils.isBlank(titleKey))
      {
         titleKey = "common.warning.title";
      }

      final String titleKeyf = titleKey;
      Display.getDefault().syncExec(new Runnable()
      {
         /**
          * Displays a warning dialog in the UI thread.
          */
         public void run()
         {
            String title = PSMessages.getString(titleKeyf);
            String msg = PSMessages.getString(msgKey);
            MessageDialog.openWarning(PSUiUtils.getShell(), title, msg);
         }
      }); 
   }

   /**
    * Queues the supplied runnable for execution at the next reasonable point
    * in the UI thread. While it is executing, an hour glass is shown. This 
    * method returns before the supplied code has finished executing. 
    * <p>
    * Any widgets referenced in <code>r</code> may have been disposed by the
    * time it runs, so the code should take care.
    * 
    * @param r The code to execute. Never <code>null</code>.
    */
   public static void asyncExecWithBusy(final Runnable r)
   {
      if (null == r)
      {
         throw new IllegalArgumentException("runnable cannot be null");  
      }
      Display disp = Display.getCurrent();
      if (disp == null)
         disp = Display.getDefault();
      assert (disp != null);
      final Display d = disp;
      disp.asyncExec(new Runnable()
      {
         public void run()
         {
            BusyIndicator.showWhile(d, new Runnable()
            {
               public void run()
               {
                  r.run();
               }
            });
         }
      });
   }
   
   /**
    * This class only provides utility methods. It is not meant to be
    * instantiated.
    */
   private PSUiUtils()
   {}
}

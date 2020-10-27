/******************************************************************************
 *
 * [ PSOpenWithMenu.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.FileSubTypes;
import com.percussion.client.models.IPSLocalFileSystemModel;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.editors.form.PSExternalFileEditorInput;
import com.percussion.workbench.ui.model.PSFileEditorTracker;
import com.percussion.workbench.ui.util.PSFileEditorHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

/**
 * A menu for opening files in the workbench.
 * <p>
 * This is a nearly identical copy of the same named class in the
 * org.eclipse.ui.actions package. It has been cleaned up to conform to our
 * standards and to work with references. The main difference is that before the
 * editor opens, the content of the opened resource is copied to the location
 * where the file claims to be.
 * <p>
 * A <code>PSOpenWithMenu</code> is used to populate a menu with "Open With"
 * actions. One action is added for each editor which is applicable to the
 * selected file. If the user selects one of these items, the corresponding
 * editor is opened on the file.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class PSOpenWithMenu extends ContributionItem 
{
    /**
     * The id of this action.
     */
    public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".OpenWithMenu";//$NON-NLS-1$

   /**
    * Constructs a new instance of <code>PSOpenWithMenu</code>.
    * 
    * @param page The page where the editor is opened if an item within the
    * menu is selected.
    * 
    * @param node The selected design object. Never <code>null</code> and it
    * must contain a valid reference.
    */
   public PSOpenWithMenu(IWorkbenchPage page, PSUiReference node)
   {
      super(ID);
      if (node.getReference() == null)
      {
         throw new IllegalArgumentException(
               "m_node must wrap a valid design object");
      }
      m_page = page;
      m_node = node;
   }

   /**
    * Returns an image to show for the corresponding editor descriptor.
    * 
    * @param editorDesc the editor descriptor, or null for the system editor
    * @return the image or null
    */
   private Image getImage(IEditorDescriptor editorDesc)
   {
      ImageDescriptor imageDesc = getImageDescriptor(editorDesc);
      if (imageDesc == null)
      {
         return null;
      }
      Image image = m_imageCache.get(imageDesc);
      if (image == null)
      {
         image = imageDesc.createImage();
         m_imageCache.put(imageDesc, image);
      }
      return image;
   }

   /**
    * Returns the image descriptor for the given editor descriptor, or null if
    * it has no image.
    */
   private ImageDescriptor getImageDescriptor(IEditorDescriptor editorDesc)
   {
      ImageDescriptor imageDesc = null;
      if (editorDesc == null)
      {
         imageDesc = getRegistry().getImageDescriptor(getFileResource().getName());
         // TODO - OK for release: is this case valid, and if so, what are the
         // implications for content-type editor bindings?
      }
      else
      {
         imageDesc = editorDesc.getImageDescriptor();
      }
      if (imageDesc == null)
      {
         if (editorDesc.getId().equals(
               IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID))
            imageDesc = getRegistry()
                  .getSystemExternalEditorImageDescriptor(getFileResource()
                        .getName());
      }
      return imageDesc;
   }

   /**
    * Builds the file based on the node supplied in the ctor and caches it.
    * 
    * @return Never <code>null</code>.
    */
   synchronized private IFile getFileResource()
   {
      if (m_file == null)
      {
         m_file = PSFileEditorTracker.getInstance().getFileResource(m_node);
      }
      return m_file;
   }
   
   /**
    * Creates the menu item for the editor descriptor.
    * 
    * @param menu the menu to add the item to
    * @param descriptor the editor descriptor, or null for the system editor
    * @param preferredEditor the descriptor of the preferred editor, or
    * <code>null</code>
    */
   private void createMenuItem(Menu menu, final IEditorDescriptor descriptor,
         final IEditorDescriptor preferredEditor)
   {
      // XXX: Would be better to use bold here, but SWT does not support it.
      final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
      boolean isPreferred = preferredEditor != null
            && descriptor.getId().equals(preferredEditor.getId());
      menuItem.setSelection(isPreferred);
      menuItem.setText(descriptor.getLabel());
      Image image = getImage(descriptor);
      if (image != null)
      {
         menuItem.setImage(image);
      }
      Listener listener = new Listener()
      {
         public void handleEvent(Event event)
         {
            switch (event.type)
            {
               case SWT.Selection:
                  if (menuItem.getSelection())
                  {
                     final IPSReference ref = m_node.getReference();
                     if (isLocal(ref))
                     {
                        try
                        {
                           final File file = getLocalFileModel().getFile(ref);
                           if (file == null)
                           {
                                    return;
                           }
                           m_page.openEditor(new PSExternalFileEditorInput(file),
                                 descriptor.getId());
                        }
                        catch (PSModelException e)
                        {
                           PSDlgUtil.showError(e);
                        }
                        catch (PartInitException e)
                        {
                           PSDlgUtil.showError(e);
                        }
                     }
                     else
                     {
                        PSFileEditorTracker.getInstance().openEditor(m_page,
                              descriptor, getFileResource(), m_node.getReference());
                     }
                  }
                  break;
            }
         }
      };
      menuItem.addListener(SWT.Selection, listener);
   }

   /*
    * (non-Javadoc) Fills the menu with perspective items.
    */
   @Override
   @SuppressWarnings("unused")
   public void fill(Menu menu, int index)
   {
      final IPSReference ref = m_node.getReference();
      if (isLocal(ref))
      {
         fillForLocal(menu, ref);
      }
      else
      {
         IFile file = getFileResource();
         fillInEditors(menu, file.getName(), IDE.getContentType(file),
               IDE.getDefaultEditor(file));
         createDefaultMenuItem(menu, file);
      }
   }

   /**
    * Fills menu for local files.
    * @param menu menu to populate. Never <code>null</code>.
    * @param ref reference to a local file. Never <code>null</code>.
    */
   private void fillForLocal(Menu menu, final IPSReference ref)
   {
      try
      {
         final File file = getLocalFileModel().getFile(ref);
         if (file == null)
         {
            return;
         }
         final IContentType contentType =
               new PSFileEditorHelper().getContentType(file);
         final IEditorDescriptor defaultEditor =
               getRegistry().getDefaultEditor(file.getName(), contentType);
         fillInEditors(menu, file.getName(), contentType, defaultEditor);
         createDefaultMenuItem(menu, file, defaultEditor);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.getDefault().handleException(null, null, null, e);
      }
   }

   /**
    * Convenience method to access file model.
    */
   private IPSLocalFileSystemModel getLocalFileModel() throws PSModelException
   {
      return (IPSLocalFileSystemModel) PSCoreFactory.getInstance().getModel(
            PSObjectTypes.LOCAL_FILE);
   }


   /**
    * Lists editors in the menu.
    * @param menu menu to populate. Not <code>null</code>.
    * @param fileName file name to get editor for. Not <code>null</code>, empty.
    * @param preferredEditor preferred editor. Can be <code>null</code>.
    */
   private void fillInEditors(Menu menu, final String fileName,
         final IContentType contentType, IEditorDescriptor preferredEditor)
   {
      IEditorDescriptor defaultEditor =
            getRegistry().findEditor("org.eclipse.ui.DefaultTextEditor"); 

      IEditorDescriptor[] editors = getRegistry().getEditors(fileName, contentType);
      Collections.sort(Arrays.asList(editors), comparer);

      boolean defaultFound = false;

      // Check that we don't add it twice. This is possible
      // if the same editor goes to two mappings.
      List<IEditorDescriptor> alreadyMapped = new ArrayList<IEditorDescriptor>();

      for (int i = 0; i < editors.length; i++)
      {
         IEditorDescriptor editor = editors[i];
         if (!alreadyMapped.contains(editor))
         {
            createMenuItem(menu, editor, preferredEditor);
            if (defaultEditor != null
                  && editor.getId().equals(defaultEditor.getId()))
               defaultFound = true;
            alreadyMapped.add(editor);
         }
      }

      // Only add a separator if there is something to separate
      if (editors.length > 0)
         new MenuItem(menu, SWT.SEPARATOR);

      // Add default editor. Check it if it is saved as the preference.
      if (!defaultFound && defaultEditor != null)
      {
         createMenuItem(menu, defaultEditor, preferredEditor);
      }

      // Add system editor (should never be null)
      IEditorDescriptor descriptor = getRegistry()
            .findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
      createMenuItem(menu, descriptor, preferredEditor);

      // Add system in-place editor (can be null)
      descriptor = getRegistry()
            .findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
      if (descriptor != null)
      {
         createMenuItem(menu, descriptor, preferredEditor);
      }
   }

   /*
    * (non-Javadoc) Returns whether this menu is dynamic.
    */
   @Override
   public boolean isDynamic()
   {
      return true;
   }

   /**
    * Creates the menu item for clearing the current selection.
    * 
    * @param menu the menu to add the item to
    * @param file the file being edited
    */
   private void createDefaultMenuItem(Menu menu, final IFile file)
   {
      final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
      menuItem.setSelection(IDE.getDefaultEditor(file) == null);
      menuItem.setText(PSMessages
            .getString("PSOpenWithMenu.defaultEntry.label"));

      Listener listener = new Listener()
      {
         public void handleEvent(Event event)
         {
            switch (event.type)
            {
               case SWT.Selection:
                  if (menuItem.getSelection())
                  {
                     IDE.setDefaultEditor(file, null);
                     try
                     {
                        IEditorDescriptor desc = IDE.getEditorDescriptor(file);
                        PSFileEditorTracker.getInstance().openEditor(m_page,
                              desc, file, m_node.getReference());
                     }
                     catch (PartInitException e)
                     {
                        PSWorkbenchPlugin.handleException("Opening editor",
                              null, null, e);
                     }
                  }
                  break;
            }
         }
      };

      menuItem.addListener(SWT.Selection, listener);
   }

   /**
    * Creates the menu item for clearing the current selection.
    * 
    * @param menu the menu to add the item to. Not <code>null</code>.
    * @param file the file being edited. Must be existing file.
    * Not <code>null</code>.
    * @param defaultEditor editor to edit the file. Can be <code>null</code> if
    * no editor exists.
    */
   private void createDefaultMenuItem(Menu menu, final File file,
         final IEditorDescriptor defaultEditor)
   {
      final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
      menuItem.setSelection(defaultEditor == null);
      menuItem.setText(PSMessages
            .getString("PSOpenWithMenu.defaultEntry.label"));

      Listener listener = new Listener()
      {
         public void handleEvent(Event event)
         {
            switch (event.type)
            {
               case SWT.Selection:
                  if (menuItem.getSelection())
                  {
                     try
                     {
                        m_page.openEditor(new PSExternalFileEditorInput(file),
                              defaultEditor.getId());
                     }
                     catch (PartInitException e)
                     {
                        PSWorkbenchPlugin.handleException("Opening editor",
                              null, null, e);
                     }
                  }
                  break;
            }
         }
      };

      menuItem.addListener(SWT.Selection, listener);
   }
   
   /**
    * Indicates whether provided reference points to local file.
    * @param ref to check whether it is a local file reference.
    * Can be <code>null</code>.
    */
   private boolean isLocal(final IPSReference ref)
   {
      return ref != null && ref.getObjectType().equals(
            new PSObjectType(PSObjectTypes.LOCAL_FILE, FileSubTypes.FILE));
   }

   /**
    * Convenience method to get workbench editor registry.
    * Never <code>null</code>.
    */
   private IEditorRegistry getRegistry()
   {
      return PlatformUI.getWorkbench().getEditorRegistry();
   }

   /*
    * Compares the labels from two IEditorDescriptor objects 
    */
   private static final Comparator<IEditorDescriptor> 
      comparer = new Comparator<IEditorDescriptor>()
   {
      private Collator collator = Collator.getInstance();

      public int compare(IEditorDescriptor descriptor1,
            IEditorDescriptor descriptor2)
      {
         String s1 = descriptor1.getLabel();
         String s2 = descriptor2.getLabel();
         return collator.compare(s1, s2);
      }
   };

   /**
    * Used to open the editor. Never <code>null</code> after ctor.
    */
   private final IWorkbenchPage m_page;
   
   /**
    * The handle to the file content to be edited. Lazily created when needed,
    * then never <code>null</code>.
    */
   private IFile m_file;
   
   /**
    * The m_node which the menu will act upon. Never <code>null</code> after
    * ctor.
    */
   private final PSUiReference m_node;

   /**
    * 
    */
   private static Hashtable<ImageDescriptor, Image> m_imageCache = 
      new Hashtable<ImageDescriptor, Image>(11);
}

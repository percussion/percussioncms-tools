/******************************************************************************
 *
 * [ PSEditorRegistry.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.FileSubTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSLocalFileSystemModel;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.workbench.ui.editors.form.PSExternalFileEditorInput;
import com.percussion.workbench.ui.model.PSFileEditorTracker;
import com.percussion.workbench.ui.util.PSFileEditorHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class hides the details of the mapping between a design object and the
 * editor used to modify it. Currently, this class contains the mappings. At a
 * later time, this may be moved to the plugin file for more flexibility.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSEditorRegistry
{
   /**
    * Obtain the single instance of this class.
    *
    * @return Never <code>null</code>.
    */
   static synchronized public PSEditorRegistry getInstance()
   {
      if (null == ms_instance)
         ms_instance = new PSEditorRegistry();
      return ms_instance;
   }
   
   /**
    * Locates the factory that knows how to create a UI for the supplied
    * object type. This can then be used to actually open an editor for a 
    * specific instance of the type.
    * 
    * @param type Never <code>null</code>. If the type is not recognized,
    * <code>null</code> is returned.
    */
   public IPSEditorFactory findEditorFactory(PSObjectType type)
   {
      if ( null == type)
      {
         throw new IllegalArgumentException("type cannot be null");   //$NON-NLS-1$
      }
      return m_factories.get(type);
   }

   /**
    * Creates an editor input for the specified reference.
    * Note, this method creates editor input only for non-file object types.  
    * @param ref the reference to generate editor input for.
    * Not <code>null</code>.
    * @return the editor input. <code>null</code> if can't find editor factory
    * for the reference object type does not exist.
    */
   public IEditorInput createEditorInputForRef(IPSReference ref)
   /*
    * Andriy: this method is not used for file processing because
    * files editor logic does not work with files directly. 
    */
   {
      if (ref == null)
      {
         throw new IllegalArgumentException("Reference should not be null"); //$NON-NLS-1$
      }
      final PSObjectType objectType = ref.getObjectType();
      if (findEditorFactory(objectType) == null)
      {
         return null;
      }
      if (!(findEditorFactory(objectType) instanceof EditorFactory))
      {
         throw new IllegalArgumentException("Does not process file references."); //$NON-NLS-1$
      }
      final EditorFactory factory =
            (EditorFactory) findEditorFactory(objectType);
      final IEditorDescriptor editordesc =
            getRegistry().findEditor(factory.m_editorId);
      if (editordesc == null)
      {
         return null;

      }
      ImageDescriptor image = editordesc.getImageDescriptor();
      if (image == null)
      {
         image = ImageDescriptor.getMissingImageDescriptor();
      }
      return new PSEditorInput(ref, ref.getName(), ref.getDescription(), 
            image,  null, ref.isPersisted());
   }

   /**
    * Workbench editor registry. Never <code>null</code>. 
    * @see IWorkbench#getEditorRegistry()
    */
   private IEditorRegistry getRegistry()
   {
      return PSWorkbenchPlugin.getDefault().getWorkbench().getEditorRegistry();
   }

   /**
    * Internal class that knows how to launch the default Eclipse editor for any
    * file type.
    * 
    * @author paulhoward
    */
   private class FileEditorFactory implements IPSEditorFactory
   {
      public void openEditor(IWorkbenchPage page, IPSReference reference)
      {
         if (reference.getObjectType().equals(
               new PSObjectType(PSObjectTypes.LOCAL_FILE, FileSubTypes.FILE)))
         {
            openLocalFile(page, reference);
         }
         else
         {
            // open workspace file
            PSFileEditorTracker tracker = PSFileEditorTracker.getInstance();
            IFile file = tracker.getFileResource(reference);
            IEditorDescriptor desc = IDE.getDefaultEditor(file);
            tracker.openEditor(page, desc, file, reference);
         }
      }

      /**
       * Opens file from local filesystem.
       * @param page current workbench page. Not <code>null</code>.
       * @param reference {@link PSObjectTypes#LOCAL_FILE} reference to get file
       * from. Not <code>null</code>.
       */
      private void openLocalFile(IWorkbenchPage page, IPSReference reference)
      {
         try
         {
            final File file = getLocalFileModel().getFile(reference);
            if (file == null)
            {
                  return;
            }
            page.openEditor(
                  new PSExternalFileEditorInput(file), getEditorId(file));
         }
         catch (Exception e)
         {
            PSDlgUtil.showError(e);
         }
      }

      /**
       * Finds editor ID for the specified file.
       * @param file existing file to find editor for. Never <code>null</code>.
       * @return the string editor id. Never <code>null</code>. 
       */
      private String getEditorId(final File file)
      {
         final IContentType contentType =
               new PSFileEditorHelper().getContentType(file);
         final IEditorDescriptor editorDescriptor =
               getRegistry().getDefaultEditor(file.getName(), contentType);
         return editorDescriptor == null
               ? EditorsUI.DEFAULT_TEXT_EDITOR_ID
               : editorDescriptor.getId();
      }

      /**
       * Convenience method to access {@link IPSLocalFileSystemModel}.
       * @return the model. Never <code>null</code>.
       * @throws PSModelException passes the exception from underlying
       * core factory.
       */
      private IPSLocalFileSystemModel getLocalFileModel()
            throws PSModelException
      {
         return (IPSLocalFileSystemModel) PSCoreFactory.getInstance()
               .getModel(PSObjectTypes.LOCAL_FILE);
      }
   }
   
   /**
    * Internal class used to represent an editor factory based on a supplied
    * editor id.
    */
   private class EditorFactory implements IPSEditorFactory
   {
      /**
       * Create the factory.
       * 
       * @param editorId Assumed not <code>null</code> or empty.
       */
      public EditorFactory(String editorId)
      {
         m_editorId = editorId;
      }
      
      //see interface
      public void openEditor(IWorkbenchPage page, IPSReference ref)
         throws PartInitException
      {
         IEditorInput eInput = createEditorInputForRef(ref);
         page.openEditor(eInput, m_editorId);
      }

      /**
       * The text string that represents the editor id as specified in the
       * plugin.xml file.
       */
      private final String m_editorId;
   }
   
   /**
    * Editor factory that is specialized for search and view editors.
    * It checks to see if FTS is disabled and if so will display a
    * message and not allow an FTS search or view to be edited.
    */
   private class SearchViewEditorFactory extends EditorFactory
   {
      /**
       * Create the factory.
       * 
       * @param editorId Assumed not <code>null</code> or empty.
       */
      public SearchViewEditorFactory(String editorId)
      {
         super(editorId);
      }

      /* @see com.percussion.workbench.ui.PSEditorRegistry.EditorFactory#openEditor(
       * org.eclipse.ui.IWorkbenchPage, com.percussion.client.IPSReference)
       */
      @Override
      public void openEditor(IWorkbenchPage page, IPSReference ref)
         throws PartInitException
      {
         if(isEditAllowed(page, ref))
            super.openEditor(page, ref);
      }
      
      /**
       * Calculates whether or not edit is allowed for this seach or view.
       * Editing will not be allowed if the is an FTS type search or view
       * and FTS is disabled.
       * @param page the workbench page, assumed not <code>null</code>
       * @param ref the reference for the object to be edited,
       * assumed not <code>null</code>.
       * @return <code>true</code> if editing is allowed.
       */
      private boolean isEditAllowed(IWorkbenchPage page, IPSReference ref)
      {
         if(FeatureSet.isFTSearchEnabled())
            return true;
         try
         {
            // load the search object so we can get the engine type prop
            IPSCmsModel model = 
               PSCoreFactory.getInstance().getModel(ref);
            PSSearch def = 
               (PSSearch)model.load(ref, false, false);
            if(PSSearch.SEARCH_ENGINE_TYPE_EXTERNAL.equals(def.getProperty(
                     PSSearch.PROP_SEARCH_ENGINE_TYPE)))
            {
               String mode = 
                  ref.getObjectType().getPrimaryType() == 
                    PSObjectTypes.UI_SEARCH
                    ? PSMessages.getString("PSEditorRegistry.search.label") //$NON-NLS-1$
                    : PSMessages.getString("PSEditorRegistry.view.label"); //$NON-NLS-1$ 
               // display message
               Object[] args = new Object[]{mode};
               String title = 
                  PSMessages.getString(
                     "PSEditorRegistry.editingDisabled.title", args); //$NON-NLS-1$
               String msg = 
                  PSMessages.getString(
                     "PSEditorRegistry.editingDisabled.message", args); //$NON-NLS-1$
               MessageDialog.openInformation(
                  page.getWorkbenchWindow().getShell(), title, msg);
               return false;
            }
         }
         catch (Exception e)
         {
            PSWorkbenchPlugin.handleException(null, null, null, e);
         }
         return true;
      }
      
      
   }
   
   /**
    * This is a singleton object. Use {@link #getInstance()}.
    */
   private PSEditorRegistry()
   {
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSAutoTranslationEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.AUTO_TRANSLATION_SET));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSSlotEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.SLOT));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSKeywordEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.KEYWORD));
      createEditorFactory(
         "com.percussion.workbench.ui.editors.form.PSExtensionEditor", //$NON-NLS-1$
         PSObjectTypeFactory.getType(PSObjectTypes.EXTENSION));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSViewEditor", //$NON-NLS-1$
            PSObjectTypes.UI_VIEW);
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSXmlApplicationEditor", //$NON-NLS-1$
            PSObjectTypes.XML_APPLICATION);
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSContentTypeEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSSystemDefEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSSharedDefEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.SHARED_FIELDS));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSLocaleEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.LOCALE));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSItemFilterEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.ITEM_FILTER));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSCommunityEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.COMMUNITY));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSTemplateEditor", //$NON-NLS-1$
            PSObjectTypes.TEMPLATE);
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSDisplayFormatEditor", //$NON-NLS-1$
            PSObjectTypeFactory.getType(PSObjectTypes.UI_DISPLAY_FORMAT));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSSearchEditor", //$NON-NLS-1$
            PSObjectTypes.UI_SEARCH);
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSActionMenuEditor", //$NON-NLS-1$
            PSObjectTypes.UI_ACTION_MENU);
      createEditorFactory(PSObjectTypeFactory.getType(
            PSObjectTypes.CONFIGURATION_FILE,
            PSObjectTypes.ConfigurationFileSubTypes.NAVIGATION_PROPERTIES));
      createEditorFactory(
            "com.percussion.workbench.ui.editors.form.PSRelationshipTypeEditor", //$NON-NLS-1$
            PSObjectTypes.RELATIONSHIP_TYPE);

      //file based types
      createEditorFactory(PSObjectTypeFactory.getType(PSObjectTypes.LOCAL_FILE,
            FileSubTypes.FILE));
      createEditorFactory(PSObjectTypes.CONFIGURATION_FILE);
      createEditorFactory(PSObjectTypes.LEGACY_CONFIGURATION);
      createEditorFactory(PSObjectTypes.CONTENT_EDITOR_CONTROLS);
      createEditorFactory(PSObjectTypeFactory.getType(
            PSObjectTypes.RESOURCE_FILE, FileSubTypes.FILE));
      createEditorFactory(PSObjectTypes.CONTENT_EDITOR_CONTROLS);
   }

   /**
    * A convenience method that calls
    * {@link #createEditorFactory(String, PSObjectType)} for each sub-type
    * supported by the supplied type. If the supplied type has no sub-types,
    * just the primary is registered.
    * 
    * @param primary Assumed not <code>null</code>.
    */
   private void createEditorFactory(
         final String editorId, final PSObjectTypes primary)
   {
      for (final PSObjectType objectType : primary.getTypes())
      {
         createEditorFactory(editorId, objectType);
      }
   }

   /**
    * Creates a new factory instance for editing non-file-based objects and
    * stores it in the local cache.
    * 
    * @param editorId The fully qualified name of the editor, as defined in
    * plugin.xml.
    * 
    * @param objectType The design object that is to be edited by this editor.
    */
   private void createEditorFactory(final String editorId,
         final PSObjectType objectType)
   {
      Enum type = objectType.getPrimaryType();
      IPSEditorFactory factory = null;
      if(type == PSObjectTypes.UI_SEARCH || type == PSObjectTypes.UI_VIEW)
      {
         factory = new SearchViewEditorFactory(editorId);
      }
      else
      {
         factory = new EditorFactory(editorId);
      }
      m_factories.put(objectType, factory);
   }

   /**
    * A convenience method that calls {@link #createEditorFactory(PSObjectType)}
    * for each sub-type supported by the supplied type. If the supplied type 
    * has no sub-types, just the primary is registered.
    * 
    * @param primary Assumed not <code>null</code>.
    */
   private void createEditorFactory(final IPSPrimaryObjectType primary)
   {
      for (final PSObjectType objectType : primary.getTypes())
      {
         createEditorFactory(objectType);
      }
   }

   /**
    * Creates a new factory instance for editing file-based objects and
    * stores it in the local cache.
    * 
    * @param objectType The design object that is to be edited by this editor.
    * Assumed not <code>null</code>.
    */
   private void createEditorFactory(PSObjectType objectType)
   {
      IPSEditorFactory factory = new FileEditorFactory();
      m_factories.put(objectType, factory);
   }

   /**
    * The single instance of this class. Initialized on first call to
    * {@link #getInstance()}, then never changed.
    */
   private static PSEditorRegistry ms_instance;
   
   /**
    * Initialized in ctor, then never changed. Contains a mapping between the
    * known object types and the code that can open an editor for an instance 
    * of that type.
    */
   final private Map<PSObjectType, IPSEditorFactory> m_factories = 
      new HashMap<PSObjectType, IPSEditorFactory>();
}

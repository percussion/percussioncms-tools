/******************************************************************************
 *
 * [ PSTemplateSourcePageHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSExtensionModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSCompletionProvider;
import com.percussion.workbench.ui.util.PSFileEditorHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.percussion.workbench.ui.IPSUiConstants.HALF;

/**
 * Helps to manage source page shown on the template editor "Source" tab.
 * Depending on template configuration shows appropriate source editor or empty
 * page for configurations which should not have editable source.
 * 
 * @author Andriy Palamarchuk
 */
public class PSTemplateSourcePageHelper
{
   /**
    * Creates source new page helper.
    * @param multipageEditor the editor this class manages source page for.
    * @throws PSModelException when data loading from the model failed. 
    */
   public PSTemplateSourcePageHelper(
         final PSMultiPageEditorBase multipageEditor)
         throws PSModelException
   {
      m_multipageEditor = multipageEditor;
      insertNoSourceControlPage();
      m_completionProvider = new PSCompletionProvider();
   }

   /**
    * Initialize controls with the template values.
    * @param template the template to load data from. Never <code>null</code>.
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      if (template == null)
      {
         throw new IllegalArgumentException("Template should not be null.");
      }
      m_template = template;
      m_lastAssembler = template.getAssembler();
      clearSources();
      setSource(generateTemplateFileName(), m_template.getTemplate());
   }

   /**
    * Reinitializes page reflecting template state.
    * Must be called only after {@link #loadControlValues(PSUiAssemblyTemplate)}
    * initialized template field. 
    */
   public void reinitializePage()
   {
      setSource(generateTemplateFileName(), ""); //$NON-NLS-1$
      m_lastAssembler = m_template.getAssembler();
   }

   /**
    * Generates file name for the template with the file extension according to
    * the declared extension.
    */
   public String generateTemplateFileName()
   {
      final String suffix = retrieveSourceSuffix();
      return suffix == null ? null : m_template.getName() + suffix;
   }

   /**
    * Loads source file suffix for the template assembler. 
    */
   private String retrieveSourceSuffix()
   {
      try
      {
         final String assembler = m_template.getAssembler();
         return m_extensionModel.getAssemblerSourceExt(assembler);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }

   /**
    * Loads the template with the provided data.
    * @param template the template being updated with data
    */
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      if (m_ignoreUpdate)
      {
         return;
      }
      template.setTemplate(null);
      if (m_currentEditor != null)
      {
         m_currentEditor.doSave(new NullProgressMonitor());
         final IFile file = m_sources.get(m_currentFileExt);
         final StringWriter writer = new StringWriter();
         try
         {
            IPath loc = file.getRawLocation();
            File f = loc.toFile();
            final Reader reader = new InputStreamReader(new FileInputStream(f), "UTF8");
            try
            {
               IOUtils.copy(reader, writer);
            }
            finally
            {
               reader.close();
            }
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         template.setTemplate(writer.toString());
      }
   }

   /**
    * Clears all sources associations.
    */
   void clearSources()
   {
      m_sources.clear();
      m_currentFileExt = null;
      m_currentEditor = null;
   }
   
   /**
    * Convenience method to call {@link #setSource(String, String, boolean)} as
    * setSource(String, String, false)
    */
   public void setSource(String filename, final String source)
   {
      setSource(filename, source, false);   
   }

   /**
    * Loads specified source into editor. If no source is associated with the
    * output format source format the provided source is associated.
    * 
    * @param filename The filename to be used
    * @param source the template source
    * @param force set source even if already set,show no warning if source already
    * exists. When forcing the caller should handle the display of a warning message.
    */
   public void setSource(String filename, final String source, boolean force)
   {
      IProject project = PSWorkbenchPlugin.getDefault().getProject();

      final String fileExt = retrieveSourceSuffix();
      assert fileExt == null || fileExt.startsWith("."); //$NON-NLS-1$
      if (!force && StringUtils.equals(fileExt, m_currentFileExt))
      {
         return;
      }
      if(!force)
         maybeWarnSourceIsLost();
      m_currentFileExt = fileExt;

      // copy source to file if it is not already there
      if (fileExt != null && (force || !m_sources.containsKey(fileExt)))
      {
         try
         {
            final IFile file = writeSourceToFile(project, source, filename);
            m_completionProvider.attachVelocityEditorCompletionData(file);
            m_sources.put(fileExt, file);
         }
         catch (IOException e)
         {
            PSDlgUtil.showError(e);
            return;
         }
         catch (CoreException e)
         {
            PSDlgUtil.showError(e);
            return;
         }
      }

      try
      {
         resetPage();
      }
      catch (PartInitException e)
      {
         PSDlgUtil.showError(e);
         return;
      }
      catch (CoreException e)
      {
         PSDlgUtil.showError(e);
         return;
      }
   }

   /**
    * Shows a warning pop-up if current editor has non-empty source.
    */
   private void maybeWarnSourceIsLost()
   {
      if (m_currentEditor != null)
      {
         m_currentEditor.doSave(new NullProgressMonitor());
         final IFile file = m_sources.get(m_currentFileExt);
         final File f = file.getRawLocation().toFile();
         if (f.length() > 0)
         {
            final String assembler =
               m_lastAssembler.substring(m_lastAssembler.lastIndexOf('/') + 1);
            MessageDialog.openWarning(
                  m_multipageEditor.getContainer().getShell(),
                  PSMessages.getString("common.warning.title"),                 //$NON-NLS-1$
                  PSMessages.getString(
                        "PSTemplateSourcePageHelper.warning.sourceLost",        //$NON-NLS-1$
                        assembler));
         }
      }
   }

   /**
    * Writes the provided source string to a temporary file and returns the
    * file.
    * 
    * @param project Eclipse project for the workbench. Never <code>null</code>.
    * @param source can be <code>null</code>
    * @param name
    * @throws CoreException
    * @throws UnsupportedEncodingException
    */
   private IFile writeSourceToFile(IProject project, final String source,
         String name) throws CoreException, UnsupportedEncodingException
   {
      IProgressMonitor def = new NullProgressMonitor();
      IFolder folder = project.getFolder("templates"); //$NON-NLS-1$
      if (!folder.exists())
      {
         folder.create(true, true, def);
      }
      
      IFile file = null;
      
      // Remove any existing matching file to avoid case sensitivity collisions
      // on windows.
      for(IResource m : folder.members())
      {
         if (m instanceof IFile)
         {
            IFile filem = (IFile) m;
            if (filem.getName().equalsIgnoreCase(name))
            {
               filem.delete(true, def);
               break;
            }
         }
      }
 
      final InputStream is = new ByteArrayInputStream(
            StringUtils.defaultString(source).getBytes("UTF8")); //$NON-NLS-1$

      file = folder.getFile(name);
      file.create(is, true, def);
      file.setCharset("UTF8", def);

      return file;
   }

   /**
    * Recreates the page control.
    */
   void resetPage() throws PartInitException, CoreException
   {
      removePage();
      if (hasSource())
      {
         insertEditorPage();
      }
      else
      {
         insertNoSourceControlPage();
      }
   }

   /**
    * Creates and inserts a page with source editor.
    */
   private void insertEditorPage() throws PartInitException, CoreException
   {
      assert !pageExists();
      final IFile file = m_sources.get(m_currentFileExt);
      assert file.exists();

      m_currentEditor = m_fileEditorHelper.createEditorPartToEdit(file);
      m_currentEditor.addPropertyListener(new IPropertyListener()
      {
         public void propertyChanged(
               @SuppressWarnings("unused")final Object source, final int propId)
         {
            if (propId == IEditorPart.PROP_DIRTY && m_currentEditor != null
                  && m_currentEditor.isDirty())
            {
               m_multipageEditor.setDirty();
            }
         }
      });
      m_ignoreUpdate = true;
      try
      {
         m_multipageEditor.addPage(PAGE_IDX, m_currentEditor,
               new FileEditorInput(file));
      }
      finally
      {
         m_ignoreUpdate = false;
      }
      configurePage();
   }

   /**
    * Removes the current page created by the helper.
    */
   private void removePage()
   {
      assert pageExists();
      if (m_currentEditor != null)
      {
         m_currentEditor.doSave(new NullProgressMonitor());
      }
      m_multipageEditor.unregisterControl(getPageControl());
      m_multipageEditor.removePage(PAGE_IDX);
      m_currentEditor = null;
      assert !pageExists();
   }

   /**
    * Inserts a page with a control displaying information that there is no
    * source for this kind of output format. The control presents empty page
    * with a message explaining why the source editor is not available.
    */
   void insertNoSourceControlPage()
   {
      assert !pageExists();
      final Composite control = new Composite(getPageContainer(), SWT.NONE);
      control.setLayout(new FormLayout());

      final Label label = new Label(control, SWT.CENTER);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.right = new FormAttachment(100);
      formData.top = new FormAttachment(HALF);

      label.setText(
            PSMessages.getString("PSTemplateSourcePageHelper.label.noSource")); //$NON-NLS-1$
      label.setLayoutData(formData);

      m_multipageEditor.addPage(PAGE_IDX, control);
      configurePage();
   }

   /**
    * Configures newly creates page.
    */
   private void configurePage()
   {
      m_multipageEditor.setPageText(PAGE_IDX, TAB_LABEL);
      m_multipageEditor.registerControl(TAB_LABEL, getPageControl(), null);
      assert pageExists();
   }

   /**
    * Default value for the extension model used on this page.
    */
   IPSExtensionModel initializeExtensionModel()
   {
      try
      {
         return (IPSExtensionModel) getCoreFactory().getModel(
               PSObjectTypes.EXTENSION);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }

   /**
    * Returns <code>true</code> if the template has source
    * under the configuration set up in the template editor. 
    */
   protected boolean hasSource()
   {
      return m_currentFileExt != null;
   }

   /**
    * Indicates whether the page served by this editor exists.
    */
   private boolean pageExists()
   {
      return StringUtils.equals(m_multipageEditor.getPageText(PAGE_IDX),
            TAB_LABEL);
   }

   /**
    * The singleton core factory instance.
    */
   private PSCoreFactory getCoreFactory()
   {
      return PSCoreFactory.getInstance();
   }

   /**
    * The page control. Must be called only when page exists.
    * @return the page control 
    */
   public Control getPageControl()
   {
      assert pageExists();
      return m_multipageEditor.getControl(PAGE_IDX);
   }

   /**
    * The multipage editor page parent control.
    */
   private Composite getPageContainer()
   {
      return m_multipageEditor.getContainer();
   }

   /**
    * Current editor. Can be <code>null</code>.
    * @return the editor part
    */
   public IEditorPart getCurrentEditor()
   {
      return m_currentEditor;
   }

   /**
    * Label of the source tab this helper manages.
    */
   public static final String TAB_LABEL = "Source"; //$NON-NLS-1$

   /**
    * Tab index this helper manages.
    */
   static final int PAGE_IDX = 0;
   
   /**
    * When <code>true</code> update call is ignored.
    * Used internally to block updates when resetting the source editor.
    * Otherwise source editor "save" method is called while it is not completely
    * initialized yet causing at least one kind of editor (XMLBuddy) to crash.
    */
   private boolean m_ignoreUpdate;

   /**
    * Map of template file extensions identifying source format
    * to template source. Have to use map to store
    * source because user potentially can keep more than one source in memory
    * due to switching output formats.
    */
   final Map<String, IFile> m_sources = new HashMap<String, IFile>();

   /**
    * File extension identifying current template format.
    * File extension is stored with the leading dot.
    * If <code>null</code> then no source editor is provided to edit the source.
    */
   String m_currentFileExt;

   /**
    * Current editor.
    */
   private IEditorPart m_currentEditor;

   /**
    * Provides some file editing functionality.
    */
   private final PSFileEditorHelper m_fileEditorHelper = new PSFileEditorHelper();

   /**
    * Multipage editor showing the page.
    */
   private final PSMultiPageEditorBase m_multipageEditor;

   /**
    * Template loaded last. Is initialized in
    * {@link #loadControlValues(PSUiAssemblyTemplate)}.
    */
   private PSUiAssemblyTemplate m_template;
   
   /**
    * Manages variable, method, field completions. Initialized in constructor.
    * Never <code>null</code> after that.
    */
   private final PSCompletionProvider m_completionProvider;

   /**
    * Model used to catalog assemblers.
    */
   IPSExtensionModel m_extensionModel = initializeExtensionModel();
   
   /**
    * Last selected assembler. Used to generate a warning when switching
    * assemblers.
    */
   private String m_lastAssembler;
}

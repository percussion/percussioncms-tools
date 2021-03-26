/******************************************************************************
 *
 * [ PSImportWizardBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectInfoExtractor;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.util.PSIgnoreCaseStringComparator;
import com.percussion.utils.exceptions.PSExceptionHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.workbench.ui.PSEditorRegistry;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for design objects import wizards.
 * 
 * @author Andriy Palamarchuk
 */
public abstract class PSImportWizardBase extends Wizard
      implements IImportWizard
{
   // see interface
   public void init(IWorkbench workbench,
         @SuppressWarnings("unused") IStructuredSelection selection)
   {
      m_workbench = workbench;
   
      setWindowTitle(getMessage("title"));
      setDefaultPageImageDescriptor(
            PSUiUtils.getImageDescriptorFromIconsFolder(getPageImage()));
   }

   // see base class
   @Override
   public void addPages()
   {
      super.addPages();
      m_mainPage = new PSFileImportPage(getFileExtension());
      addPage(m_mainPage);
   }

   /**
    * Performs actual import when user clicks on "Finish" button.
    * Reports all the errors to the user. 
    * @return always <code>true</code>.
    * @see org.eclipse.jface.wizard.IWizard#performFinish()
    */
   @Override
   public boolean performFinish()
   {
      final Shell shell = m_workbench.getActiveWorkbenchWindow().getShell();
      final List<File> files = m_mainPage.getSelectedFiles();
      
      // Start import *after* import wizard dialog is dismissed.
      // Otherwise progress dialog is not shown because there are other
      // modal windows.
      shell.getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            try
            {
               PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
                     new IRunnableWithProgress()
                     {
                        public void run(IProgressMonitor monitor)
                        {
                           doImport(monitor, shell, files);
                        }
                     });
            }
            catch (Exception e)
            {
               new PSErrorDialog(getShell(), null, e).open();
            }
   
         }
      });
      return true;
   }

   /**
    * Import given files. First load the files. Then lock the objects if they
    * exist. Last, update/create the objects.
    * 
    * @param monitor progress monitor. Assumed not <code>null</code>.
    * @param shell the parent window shell. Assumed not <code>null</code>.
    * @param files the file array of selected files.
    * Assumed not <code>null</code>.
    */
   private void doImport(IProgressMonitor monitor, final Shell shell,
         final List<File> files)
   {
      final Map<IPSReference, Object> objMap =
            new HashMap<IPSReference, Object>();
      final List<Throwable> problems = new ArrayList<Throwable>();
      final List<Object> detail = new ArrayList<Object>();
      try
      {
         final IPSCmsModel model = getModel();
         final Collection<IPSReference> trefs = model.catalog();
         final Map<String, IPSReference> refmap =
               new HashMap<String, IPSReference>();
         for (IPSReference r : trefs)
         {
            refmap.put(r.getName(), r);
         }
   
         // subtasks: read each object from file (size), save each one (size)
         int count = files.size() * 2;
         monitor.beginTask(getMessage("progress.beginTask"), count);
   
         
         for (final File file : files)
         {
            try
            {
               monitor.subTask(getMessage("progress.subTaskLoad", file));
               monitor.worked(1);
               loadFileToObject(objMap, refmap, file, shell);
               if (monitor.isCanceled())
               {
                  handleCancel(objMap.keySet(), null, shell);
                  return;
               }
            }
            catch (Exception e)
            {
               if(e instanceof PSModelException && 
                     ((PSModelException)e).getDetail() != null)
               {
                  problems.add(e);
               }
               else
               {
                  problems.add(PSExceptionHelper.findRootCause(e, true));
               }
               detail.add(null);
            }
         }
   
         // Commit the loaded data
         for (IPSReference ref : objMap.keySet())
         {
            try
            {
               monitor.subTask(getMessage("progress.subTaskStore", ref.getName()));
               monitor.worked(1);
               getTracker().save(ref);            
               if (monitor.isCanceled())
               {
                  handleCancel(objMap.keySet(), ref, shell);
                  return;
               }
            }
            catch (Exception e)
            {
               if(e instanceof PSModelException && 
                     ((PSModelException)e).getDetail() != null)
               {
                  problems.add(e);
               }
               else
               {
                  problems.add(PSExceptionHelper.findRootCause(e, true));
               }
               detail.add(ref);
               try
               {
                  getTracker().releaseLock(ref);
               }
               catch (Exception ignore)
               {
               }
            }
         }
         
      }
      catch (final PSModelException e)
      {
         getDisplay().asyncExec(new Runnable() {
            public void run()
            {
               PSWorkbenchPlugin.handleException(null, null, null, e);
            }});
      }
      finally
      {
         reportProblems(problems, detail);
         monitor.done();
      }
   }

   /**
    * Shows user provided errors.
    * @param problems the exceptions to show to the user.
    * Assumed not <code>null</code>.
    * @param details error details about the error that occurred.
    * Must have the same number of entries as the problems list. 
    */
   private void reportProblems(final List<Throwable> problems,
         final List<Object> details)
   {
     
      if (!problems.isEmpty())
      {
         final PSMultiOperationException mex = 
            new PSMultiOperationException(problems.toArray(new Object[0]),
                  details.toArray(new Object[0]));
         getDisplay().asyncExec(new Runnable()
         {
            public void run()
            {
               PSWorkbenchPlugin.handleException(
                     getMessage("progress.beginTask"), null, null, mex);
            }});
      }
   }

   /**
    * Is called when user cancels an operation.
    * Goes through the object map and releases references which were not
    * persisted.
    * Notifies user about objects which were skipped.
    * @param refs the object references. Assumed not <code>null</code>.
    * @param lastProcessedRef last reference for which processing is complete.
    * <code>null</code> if there is no reference with completed processing.
    */
   private void handleCancel(Collection<IPSReference> refs,
         final IPSReference lastProcessedRef, final Shell shell)
   {
      for (final IPSReference ref : refs)
      {
         if (!ref.isPersisted())
         {
            try
            {
               getTracker().releaseLock(ref);
            }
            catch (Exception ignore)
            {
            }
         }
      }

      // notify about skipped references
      if (lastProcessedRef == null)
      {
         getDisplay().asyncExec(new Runnable()
         {
            public void run()
            {
               new PSErrorDialog(shell, getMessage("error.nothingImported")).open();
            }
         });
      }
      else
      {
         final List<String> names = new ArrayList<String>();
         boolean afterLast = false;
         for (final IPSReference ref : refs)
         {
            if (afterLast)
            {
               names.add(ref.getName());
            }
            else if (ref.equals(lastProcessedRef))
            {
               afterLast = true;
            }
            else
            {
               // skip this one
            }
         }
         if (!names.isEmpty())
         {
            Collections.sort(names, new PSIgnoreCaseStringComparator());
            final String namesStr = StringUtils.join(names.iterator(), ", ");
            getDisplay().asyncExec(new Runnable()
                  {
                     public void run()
                     {
                        new PSErrorDialog(shell,
                              getMessage("error.notImported", namesStr)).open();
                     }
                  });
         }
      }
   }

   /**
    * Updates design object from the specified file.
    * Creates a new object if necessary.
    * @param objMap a map containing the loaded design objects indexed by their
    * references. The objects loaded/created by this method are added to the map.
    * Assumed not <code>null</code>.
    * @param refmap A read-only map containing all existing references
    * for the current object type keyed by their names. 
    * Assumed not <code>null</code>.
    * @param file file to load to the object. Assumed not <code>null</code>.
    * @param shell parent shell for pop-up dialogs.
    * Assumed not <code>null</code>.
    * @throws Exception on loading failure.
    */
   private void loadFileToObject(final Map<IPSReference, Object> objMap,
         final Map<String, IPSReference> refmap, final File file,
         final Shell shell)
         throws Exception
   {
      final Reader reader =
            new InputStreamReader(new FileInputStream(file), "UTF8");
      final String content;
      try
      {
         content = IOUtils.toString(reader);
      }
      finally
      {
         reader.close();
      }
      final String name = FilenameUtils.getBaseName(file.getName());
      final IPSReference ref;
      if (refmap.get(name) == null)
      {
         ref = createNewLoadToObject(content, name);
      }
      else
      {
         ref = refmap.get(name);
         if (!checkForOpenEditor(shell, ref))
         {
            return;
         }
      }
      final Object designObject = getTracker().load(ref, true);
      objMap.put(ref, designObject);
      fromXml(designObject, content);
      updateAccessPrivileges(designObject);
      getTracker().propertyChanged(ref, null);
   }

   /**
    * Checks whether an editor is already open for the specified design object.
    * If the editor is dirty asks user whether to overwrite the changes.
    *
    * @param shell the shell to use for the confirmation dialog.
    * Assumed not <code>null</code>.
    * @param ref the object to search an open editor for.
    * Assumed not <code>null</code>.
    * @return <code>true</code> if the object can be imported,
    * <code>false</code> if the object should be skipped.
    */
   private boolean checkForOpenEditor(final Shell shell, final IPSReference ref)
   {
      final boolean[] resultContainer = new boolean[] {true};
      getDisplay().syncExec(new Runnable()
      {
         public void run()
         {
            final IEditorPart openEditor = findRefOpenEditor(ref);
            if (openEditor != null && openEditor.isDirty())
            {
               resultContainer[0] = MessageDialog.openQuestion(shell,
                     getMessage("confirmation.overwriteUnsavedChanges.title"),
                     getMessage("confirmation.overwriteUnsavedChanges.message",
                           ref.getName()));
            }
         }
      });
      return resultContainer[0];
   }

   /**
    * Unserializes design object from its XML presentation.
    * <p>
    * The default implementation attempts to cast the supplied object to a
    * {@link IPSCatalogItem} and use its methods to load the supplied xml.
    * 
    * @param o the object to convert from XML. Not <code>null</code>. In the
    * default impl, must be an instance of <code>IPSCatalogItem</code>.
    * Derived classes may not have this requirement.
    * 
    * @param xml xml document to convert to design object. Not blank.
    * 
    * @throws IOException if there is a problem unserializing the object.
    * @throws SAXException if there is an issue parsing XML.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by the class.
    * @throws PSInvalidXmlException if the XML element node is not valid. 
    */
   @SuppressWarnings("unused")
   protected void fromXml(final Object o, final String xml)
         throws IOException, SAXException, PSUnknownNodeTypeException,
         PSInvalidXmlException 
   {
      if (o == null)
      {
         throw new IllegalArgumentException("Object must be specified");
      }
      if (StringUtils.isBlank(xml))
      {
         throw new IllegalArgumentException("Content must be specified");
      }
      if (o instanceof IPSCatalogItem)
      {
         final IPSCatalogItem item = (IPSCatalogItem) o;
         final IPSGuid guid = item.getGUID();
         item.fromXML(xml);
         item.setGUID(guid);
      }
      else
      {
         throw new IllegalArgumentException(
               "Unexpected object type for object " + o);
      }
   }

   /**
    * Creates new object 
    * @param xml xml document to parse. Not blank.
    * @param name name for new object. Not blank.
    * @return reference for the newly created object. Not <code>null</code>.
    */
   private IPSReference createNewLoadToObject(final String xml, final String name)
         throws IOException, SAXException, PSDuplicateNameException,
         PSInvalidXmlException, PSUnknownNodeTypeException
   {
      if (StringUtils.isBlank(xml))
      {
         throw new IllegalArgumentException("Specify valid XML document to parse");
      }
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("Specify valid object name");
      }

      final PSObjectType objectType = findObjectType(xml);
      return getTracker().create(objectType, name, null);
   }

   /**
    * Object type of the object serialized in the document.
    * 
    * @param xml the document. Not blank.
    * @return object type of the object serialized in the document. Never null.
    * @throws IOException on I/O failure.
    * @throws SAXException on XML parsing failure.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by the class.
    * @throws PSInvalidXmlException if the XML element node is not valid. 
    */
   protected PSObjectType findObjectType(final String xml)
         throws IOException, SAXException, PSUnknownNodeTypeException,
         PSInvalidXmlException
   {
      if (StringUtils.isBlank(xml))
      {
         throw new IllegalArgumentException(
               "The document to parse must be specified");
      }
      // load the object to figure out the type
      final Object o = createTemp();
      fromXml(o, xml);
      final PSObjectType objectType =
            PSObjectInfoExtractor.getObjectType(o, getPrimaryType());
      return objectType;
   }

   /**
    * Subclasses should override this method if they need
    * to correct access privileges of the design object.
    * Default implementation does nothing except it validates the parameter.
    * 
    * @param o the object being imported. Should not be <code>null</code>.
    * @throws PSModelException 
    * @throws NotOwnerException 
    */
   @SuppressWarnings("unused")
   protected void updateAccessPrivileges(final Object o)
         throws PSModelException, NotOwnerException
   {
      if (o == null)
      {
         throw new IllegalArgumentException("Design object should be specified");
      }
   }

   /**
    * Finds editor holding object referenced by ref.
    * @param ref reference to the object to search open editor for.
    * Assumed not <code>null</code>.
    * @return editor holding the specified object or <code>null</code> if
    * there is no such editor.
    * @throws IllegalArgumentException if an editor input can't be created
    * for the passed reference.
    */
   private IEditorPart findRefOpenEditor(IPSReference ref)
   {
      final IEditorInput editorInput =
            PSEditorRegistry.getInstance().createEditorInputForRef(ref);
      assert editorInput != null
            : "Should be able to create editor input for " + ref; 
      return getActivePage().findEditor(editorInput);
   }

   /**
    * Provides current workbench page. Must be called from SWT event thread. 
    * @return Active workbench page or <code>null</code> if none
    * exist.
    */
   private IWorkbenchPage getActivePage()
   {
      return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
   }

   /**
    * Retrieves a message for the specified key.
    * 
    * @param key the part of the key after class name to retrieve message for.
    * Not blank.
    * @return the string message from the bundle resource if found, or the
    * supplied key surrounded by '!' as !key!.
    */
   private String getMessage(final String key)
   /* 
    * Private to making sure all the keys are accounted to.
    * Feel free to make it protected.
    */ 
   {
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("Key must be specified");
      }
      return PSMessages.getString(getMessagePrefix() + key);
   }

   /**
    * Retrieves a message for the specified key.
    * 
    * @param key the part of the key after class name to retrieve message for.
    * Not blank.
    * @param args array of strings to bind to the message, replaces {0}
    * placeholders.
    * @return the string message from the bundle resource. if found, or the
    * supplied key surrounded by '!' as !key!.
    */
   private String getMessage(final String key, Object... args)
   /* 
    * Private to making sure all the keys are accounted to.
    * Feel free to make it protected.
    */ 
   {
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("Key must be specified");
      }
      return PSMessages.getString(getMessagePrefix() + key, args);
   }

   /**
    * Current display.
    * @return current display. Never <code>null</code>.
    */
   private Display getDisplay()
   {
      return PlatformUI.getWorkbench().getDisplay();
   }

   /**
    * Model for the object type handled by the wizard. 
    * @return model for the object type handled by the wizard.
    * Not <code>null</code>.
    * @throws PSModelException if model loading fails.
    */
   protected IPSCmsModel getModel() throws PSModelException
   {
      return PSCoreFactory.getInstance().getModel(getPrimaryType());
   }

   /**
    * Convenience method to access model tracker.
    * @return model tracker. Never <code>null</code>.
    */
   protected PSModelTracker getTracker()
   {
      return PSModelTracker.getInstance();
   }

   /**
    * File extension for files the design objects are saved in.
    * @return file extension design objects are exported/imported.
    * Never <code>null</code> or empty.
    */
   protected abstract String getFileExtension();

   /**
    * Primary object type for objects imported by this wizard.
    * @return primary object type for objects in the wizard.
    * Not <code>null</code>.
    */
   protected abstract PSObjectTypes getPrimaryType();

   /**
    * Name of the page image file from icons folder. 
    * @return the image file name. Never <code>null</code> or empty.
    */
   protected abstract String getPageImage();

   /**
    * Prefix for message strings. It is
    * @return prefix string prepended to message keys to retrieve a message.
    * @see #getMessage(String)
    * @see #getMessage(String, Object[])
    */
   protected abstract String getMessagePrefix();
   
   /**
    * Creates a new temporary object. 
    * @return temporary object. Never <code>null</code>.
    */
   protected abstract Object createTemp();

   /**
    * Selects files to import.
    * The only page of the wizard. Initialized in {@link #addPages()}.
    * Never <code>null</code> after that.
    */
   protected PSFileImportPage m_mainPage;

   /**
    * Stores workbench passed to {@link #init(IWorkbench, IStructuredSelection)}.
    */
   protected IWorkbench m_workbench;
}

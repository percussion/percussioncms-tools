/*******************************************************************************
 *
 * [ PSExportWizardBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.util.PSIgnoreCaseStringComparator;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for design objects export wizards.
 * 
 * @author Andriy Palamarchuk
 * @author Doug Rand
 */
public abstract class PSExportWizardBase extends Wizard implements IExportWizard
{
   // see base type
   public void init(@SuppressWarnings("unused") final IWorkbench workbench,
         final IStructuredSelection selection)
   {
      setWindowTitle(getMessage("title"));
      setDefaultPageImageDescriptor(
            PSUiUtils.getImageDescriptorFromIconsFolder(getPageImage()));
      
      m_selection = selection;
   }

   // see base type
   @Override
   public void addPages()
   {
      super.addPages();
      try
      {
         m_mainPage = new PSDesignObjectExportPage(
               getPrimaryType(), m_selection);
         addPage(m_mainPage);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
   }

   /**
    * Performs actual export when user clicks on "Finish" button.
    * Reports all the errors to the user.
    * @return always <code>true</code>.
    * @see org.eclipse.jface.wizard.Wizard#performFinish()
    */
   @Override
   public boolean performFinish()
   {
      final String exportDir = m_mainPage.getExportDir();
      final List<IPSReference> selectedRefs = m_mainPage.getSelectedObjects();
      final Shell shell = getShell();

      // start import *after* import wizard dialog is dismissed
      // otherwise progress dialog is not shown
      getDisplay().asyncExec(new Runnable()
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
                           doExport(monitor, exportDir, selectedRefs, shell);
                        }
                     });
            }
            catch (Exception e)
            {
               new PSErrorDialog(shell, null, e).open();
            }
         }
      });
      return true;
   }

   /**
    * Do the actual export work and monitor the progress of loading and storing
    * the design objects.
    * 
    * @param monitor progress monitor for this operation.
    * Assumed not <code>null</code>.
    * @param exportToDir the directory to export to.
    * Assumed exists, not <code>null</code>.
    * @param shell shell to use to display error dialogs.
    * Assumed not <code>null</code>.
    */
   private void doExport(IProgressMonitor monitor, String exportToDir,
         final List<IPSReference> selectedRefs, final Shell shell)
   {
      final List<Throwable> problems = new ArrayList<Throwable>();
      final List<Object> detail = new ArrayList<Object>();
      try
      {
         final IPSCmsModel model =
               PSCoreFactory.getInstance().getModel(getPrimaryType());
         // subtasks: load all objects (1), save each one (size)
         final int workunits = 1  + selectedRefs.size(); // 
         monitor.beginTask(getMessage("progress.beginTask"), workunits);

         monitor.subTask(getMessage("progress.subTaskLoad"));
         final Object[] loaded = model.load(
               selectedRefs.toArray(new IPSReference[0]), false, false);
         monitor.worked(1);

         // Write the files
         int i = 0;
         for (final Object o : loaded)
         {
            final IPSReference ref = selectedRefs.get(i);
            try
            {
               monitor.subTask(getMessage("progress.subTaskSave", ref.getName()));
               monitor.worked(1);
               final File tfile = new File(new File(exportToDir),
                     ref.getName() + getFileExtension());
               final Writer writer =
                     new OutputStreamWriter(new FileOutputStream(tfile), "UTF8");
               try
               {
                  writer.write(toXml(o));
               }
               finally
               {
                  writer.close();
               }
            }
            catch (Exception e)
            {
               problems.add(e);
               detail.add(ref);
            }
            if (monitor.isCanceled())
            {
               handleCancel(selectedRefs, ref, shell);
               break;
            }
            i++;
         }
         reportProblems(problems, detail);
      }
      catch (final Exception e)
      {
         PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
         {
            public void run()
            {
               new PSErrorDialog(getShell(), null, e).open();
            }
         });
      }
      finally
      {
         monitor.done();
      }
   }

   /**
    * Is called when user cancels an operation.
    * Notifies user about objects which were skipped.
    * @param refs the references being exported. Assumed not <code>null</code>.
    * @param lastProcessedRef last reference for which processing is complete.
    * Assumed not <code>null</code>.
    */
   private void handleCancel(Collection<IPSReference> refs,
         final IPSReference lastProcessedRef, final Shell shell)
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
               new PSErrorDialog(shell, getMessage("error.notExported",
                     namesStr)).open();
            }
         });
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
    * Converts a design object to XML.
    * 
    * @param o the object to convert to XML. Assumed not <code>null</code>.
    * Must be an instance of {@link IPSCatalogItem} for the default
    * implementation. Derived classes may not have such a requirement.
    * 
    * @return String presentation of the object.
    * @throws IOException if there is a problem serializing the object.
    * @throws SAXException if there is an issue converting the object to XML.
    */
   protected String toXml(final Object o) throws IOException, SAXException
   {
      if (o instanceof IPSCatalogItem)
      {
         return ((IPSCatalogItem) o).toXML(); 
      }
      else
      {
         throw new IllegalArgumentException("Unexpected type. Object: " + o);
      }
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
    * Retrieves a message for the specified key.
    * 
    * @param key the part of the key after class name to retrieve message for.
    * @return the string message from the bundle resource if found, or the
    *         supplied key surrounded by '!' as !key!.
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
    * @param args array of strings to bind to the message, replaces {0}
    *           placeholders.
    * @return the string message from the bundle resource if found, or the
    *         supplied key surrounded by '!' as !key!.
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
    * File extension for files the design objects are saved in.
    * @return file extension design objects are exported/imported.
    * Never <code>null</code> or empty.
    */
   protected abstract String getFileExtension();

   /**
    * Name of the page image file from icons folder. 
    * @return the image file name. Never <code>null</code> or empty.
    */
   protected abstract String getPageImage();

   /**
    * Primary object type for objects imported by this wizard.
    * @return primary object type for objects in the wizard.
    * Not <code>null</code>.
    */
   protected abstract PSObjectTypes getPrimaryType();

   /**
    * Prefix for message strings. It is
    * @return prefix string prepended to message keys to retrieve a message.
    * @see #getMessage(String)
    * @see #getMessage(String, Object[])
    */
   protected abstract String getMessagePrefix();

   /**
    * Selection passed to {@link #init(IWorkbench, IStructuredSelection)}.
    */
   private IStructuredSelection m_selection;

   /**
    * Selects design objects to export.
    * The only page of the wizard. Initialized in {@link #addPages()}.
    * Never <code>null</code> after that.
    */
   protected PSDesignObjectExportPage m_mainPage;
}

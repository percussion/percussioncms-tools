/*******************************************************************************
 *
 * [ PSFileImportPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.workbench.ui.editors.wizards;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.eclipse.ui.PlatformUI.getPreferenceStore;

/**
 * Page of import wizard to import files.
 * Allows user to find files with predefined extension in directory hierarchy.
 * Selection of files to import is limited to the specified file extension.
 *
 * @author Andriy Palamarchuk
 */
public class PSFileImportPage extends WizardPage
{
   /**
    * Creates new page.
    * @param fileExtension file extension to search for. Not blank.
    * Should be specified with leading '.', for example:
    * <code>".template"</code>, <code>".contentType"</code>.
    */
   public PSFileImportPage(final String fileExtension)
   {
      super("fileImportPage");
      if (StringUtils.isEmpty(fileExtension))
      {
         throw new IllegalArgumentException("File extension should not be blank");
      }
      if (!fileExtension.startsWith("."))
      {
         throw new IllegalArgumentException("File extension \"" + fileExtension
               + "\" must be specified with a leading dot.");
      }
      m_fileExtension = fileExtension; 
      setPageComplete(false);
      setDescription(getMessage("error.sourceNotEmpty"));
   }

   // see base
   public void createControl(Composite parent)
   {
      initializeDialogUnits(parent);

      final Composite container = new Composite(parent, SWT.NONE);
      setControl(container);

      container.setLayout(new GridLayout());
      container.setLayoutData(new GridData(GridData.FILL_BOTH
            | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

      createFilesRoot(container);
      createFilesList(container);
      initData();
      Dialog.applyDialogFont(container);
   }

   /**
    * Initializes controls with data.
    */
   private void initData()
   {
      final String dir = getPreferenceStore().getString(getDirPreferenceKey());
      m_directoryPathField.setText(StringUtils.isBlank(dir) ? "" : dir);
      updateFilesList();
   }

   /**
    * Create the area where you select the root directory to search for files.
    * 
    * @param container the container to create this control in.
    * Assumed not <code>null</code>.
    */
   private void createFilesRoot(Composite container)
   {
      // file specification group
      final Composite fileGroup = new Composite(container, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 3;
      layout.makeColumnsEqualWidth = false;
      layout.marginWidth = 0;
      fileGroup.setLayout(layout);
      fileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      {
         final Label dirLabel = new Label(fileGroup, SWT.NONE);
         dirLabel.setText(getMessage("label.fromDirectory"));
      }

      // root directory entry field
      m_directoryPathField = new Text(fileGroup, SWT.BORDER);
      m_directoryPathField.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

      // browse button
      m_browseButton = new Button(fileGroup, SWT.PUSH);
      m_browseButton.setText(getMessage("label.browse"));
      setButtonLayoutData(m_browseButton);

      m_browseButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            handleLocationDirectoryButtonPressed();
         }
      });

      m_directoryPathField.addTraverseListener(new TraverseListener()
      {
         public void keyTraversed(TraverseEvent e)
         {
            if (e.detail == SWT.TRAVERSE_RETURN)
            {
               e.doit = false;
               updateFilesList();
            }
         }

      });

      m_directoryPathField.addFocusListener(new FocusAdapter()
      {
         @Override
         public void focusLost(
               @SuppressWarnings("unused") FocusEvent e)
         {
            updateFilesList();
         }

      });
   }

   /**
    * Create the checkbox list for the found files.
    * 
    * @param container the container to create this control in.
    * Assumed not <code>null</code>.
    */
   private void createFilesList(final Composite container)
   {
      final Label title = new Label(container, SWT.NONE);
      title.setText(getMessage("label.fileList"));

      final Composite listComposite = new Composite(container, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      layout.marginWidth = 0;
      layout.makeColumnsEqualWidth = false;
      listComposite.setLayout(layout);

      listComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
            | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

      m_filesList = new CheckboxTreeViewer(listComposite, SWT.BORDER);
      GridData listData = new GridData(GridData.GRAB_HORIZONTAL
            | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
      listData.heightHint = getShell().getDisplay().getBounds().height / 3;
      m_filesList.getControl().setLayoutData(listData);

      m_filesList.setContentProvider(new ITreeContentProvider()
      {
         @SuppressWarnings("unused")
         public Object[] getChildren(Object parentElement)
         {
            return null;
         }

         @SuppressWarnings("unused")
         public Object[] getElements(Object inputElement)
         {
            return m_selectedFiles;
         }

         @SuppressWarnings("unused")
         public boolean hasChildren(Object element)
         {
            return false;
         }

         @SuppressWarnings("unused")
         public Object getParent(Object element)
         {
            return null;
         }

         public void dispose()
         {
         }

         @SuppressWarnings("unused")
         public void inputChanged(Viewer viewer, Object oldInput,
               Object newInput)
         {
         }
      });

      m_filesList.setLabelProvider(new LabelProvider()
      {
         @Override
         public String getText(Object element)
         {
            return getFileLabel((File) element);
         }
         
         /**
          * Generates file label.
          * @param file file to generate label for.
          * Assumed not <code>null</code>.
          * @return the file label.
          */
         private String getFileLabel(File file)
         {
            final String label;
            if (file.isDirectory())
            {
               label = file.getName();
            }
            else
            {
               // strip file extension
               label = file.getName().substring(
                     0, file.getName().length() - m_fileExtension.length());
            }
            final File parent = file.getParentFile(); 
            if (parent == null || (StringUtils.isNotBlank(m_lastPath)
                  && new File(m_lastPath).equals(parent)))
            {
               return label;
            }
            else
            {
               return getFileLabel(parent) + File.separatorChar + label;
            }
         }
      });

      m_filesList.setInput(this);
      createSelectionButtons(listComposite);

   }

   /**
    * Create the selection buttons in the listComposite.
    * 
    * @param listComposite the selection buttons container.
    * Assumed not <code>null</code>. 
    */
   private void createSelectionButtons(Composite listComposite)
   {
      Composite buttonsComposite = new Composite(listComposite, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      buttonsComposite.setLayout(layout);

      buttonsComposite.setLayoutData(new GridData(
            GridData.VERTICAL_ALIGN_BEGINNING));

      Button selectAll = new Button(buttonsComposite, SWT.PUSH);
      selectAll.setText(getMessage("label.selectAll"));
      selectAll.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            m_filesList.setCheckedElements(m_selectedFiles);
         }
      });

      setButtonLayoutData(selectAll);

      Button deselectAll = new Button(buttonsComposite, SWT.PUSH);
      deselectAll.setText(getMessage("label.deselectAll"));
      deselectAll.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            m_filesList.setCheckedElements(new Object[0]);
         }
      });
      setButtonLayoutData(deselectAll);

      Button refresh = new Button(buttonsComposite, SWT.PUSH);
      refresh.setText(getMessage("label.refresh"));
      refresh.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            updateFilesList();
         }
      });
      setButtonLayoutData(refresh);
   }

   /**
    * Is called when the browse button has been selected.
    * Shows dialog to select a location.
    */
   private void handleLocationDirectoryButtonPressed()
   {

      final DirectoryDialog dialog = new DirectoryDialog(m_directoryPathField
            .getShell());
      dialog.setText(getMessage("sourceDirectory.title"));
      dialog.setMessage(getMessage("sourceDirectory.message"));

      final String dirName = m_directoryPathField.getText().trim();

      if (StringUtils.isNotBlank(dirName))
      {
         final File path = new File(dirName);
         if (path.exists())
         {
            dialog.setFilterPath(new Path(dirName).toOSString());
         }
      }

      final String selectedDirectory = dialog.open();
      if (selectedDirectory != null)
      {
         m_directoryPathField.setText(selectedDirectory);
         updateFilesList();
      }
   }

   /**
    * Update the list of files based on currently selected root directory.
    * Returns right away if path did not change since last call.
    */
   private void updateFilesList()
   {
      final String path = m_directoryPathField.getText().trim();
      if (path.equals(m_lastPath))// Do not select the same path again
      {
         return;
      }

      m_lastPath = path;

      // on an empty path empty selectedFiles
      if (StringUtils.isBlank(path))
      {
         m_selectedFiles = new File[0];
         m_filesList.refresh(true);
         m_filesList.setCheckedElements(m_selectedFiles);
         setPageComplete(m_selectedFiles.length > 0);
         return;
      }
      
      final File directory = new File(path);

      // remember path in preferences
      if (directory.isDirectory())
      {
         getPreferenceStore().setValue(getDirPreferenceKey(), path);
      }

      try
      {
         getContainer().run(true, true, new IRunnableWithProgress()
         {
            public void run(IProgressMonitor monitor)
            {
               monitor.beginTask(getMessage("label.searching"), 100);
               m_selectedFiles = new File[0];
               Collection<File> files = new ArrayList<File>();
               monitor.worked(10);
               if (directory.isDirectory())
               {

                  if (!collectFilesFromDirectory(files, directory, monitor))
                  {
                     return;
                  }
                  Iterator filesIterator = files.iterator();
                  m_selectedFiles = new File[files.size()];
                  int index = 0;
                  monitor.worked(50);
                  monitor.subTask("label.processingSearchResult");
                  while (filesIterator.hasNext())
                  {
                     m_selectedFiles[index] = (File) filesIterator.next();
                     index++;
                  }
               }
               else
               {
                  monitor.worked(60);
               }
               monitor.done();
            }

         });
      }
      catch (InvocationTargetException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      catch (InterruptedException e)
      {
         // Nothing to do if the user interrupts.
      }

      m_filesList.refresh(true);
      m_filesList.setCheckedElements(m_selectedFiles);
      setPageComplete(m_selectedFiles.length > 0);
   }

   /**
    * Collect the list of files with extension {@link #m_fileExtension} that are
    * in the specified directory.
    * 
    * @param files collection to add collected files to.
    * Assumed not <code>null</code>, modifiable collection.
    * @param directory directory to scan for files.
    * Assumed not <code>null</code>, existing. 
    * @param monitor The monitor to report progress to.
    * Assumed not <code>null</code>.
    * @return boolean <code>true</code> if the operation was completed.
    */
   private boolean collectFilesFromDirectory(Collection<File> files,
         File directory, IProgressMonitor monitor)
   {

      if (monitor.isCanceled())
      {
         return false;
      }
      monitor.subTask(getMessage("label.checkingDir", directory.getPath()));
      final File[] contents = directory.listFiles();

      for (final File file : contents)
      {
         if (file.isFile() && file.getName().endsWith(m_fileExtension))
         {
            files.add(file);
         }
      }

      return true;
   }

   /**
    * Returns files selected for import.
    * @return files selected by user. Never <code>null</code>.
    */
   public List<File> getSelectedFiles()
   {
      final List<File> selected = new ArrayList<File>();
      for (final Object file : m_filesList.getCheckedElements())
      {
         selected.add((File) file);
      }
      return selected;
   }

   /**
    * Generates key for user preferences for last visited directory.
    * Preferences key is specific to file extension.
    * @return user preferences key for last visited directory.
    * Never <code>null</code>.
    */
   private String getDirPreferenceKey()
   {
      return "PSFileImportPage.dir" + m_fileExtension;
   }

   /**
    * Retrieves a message for the specified key.
    * 
    * @param key the part of the key after class name to retrieve message for.
    * @return the string message from the bundle resource if found, or the
    *         supplied key surrounded by '!' as !key!.
    */
   private String getMessage(final String key)
   {
      return PSMessages.getString("PSFileImportPage." + key);
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
   {
      return PSMessages.getString("PSFileImportPage." + key, args);
   }

   /**
    * File extension to search for. Never blank, has leading period.
    */
   private final String m_fileExtension;


   /**
    * Field to enter directory under which to search for files.
    * Not <code>null</code> after UI initialization.
    */
   private Text m_directoryPathField;

   /**
    * Shows found files. 
    * Not <code>null</code> after UI initialization.
    */
   private CheckboxTreeViewer m_filesList;

   /**
    * Files selected in {@link #m_filesList}. Never <code>null</code>.
    * Can be empty.
    */
   private File[] m_selectedFiles = new File[0];

   /**
    * Button to call directory selection dialog.
    * Not <code>null</code> after UI initialization.
    */
   private Button m_browseButton;

   /**
    * The last selected path to mimize searches.
    * Saves value of {@link #m_directoryPathField} during found files list
    * update. Is <code>null</code> before is initialized.
    * Can be blank if the field is blank.  
    */ 
   private String m_lastPath;
}

/*******************************************************************************
 *
 * [ PSDesignObjectExportPage.java ]
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
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.util.PSReferenceComparator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.ui.PlatformUI.getPreferenceStore;

/**
 * Page of export wizard to select design objects to export.
 * Allows user to specifiy design objects to export and directory to export to.
 *
 * @author Andriy Palamarchuk
 */
public class PSDesignObjectExportPage extends WizardPage
{
   /**
    * Creates new page.
    *
    * @param objectType type of design objects to select.
    * Should be one of {@link PSObjectTypes} values, not <code>null</code>
    * @param selection the current object selection.
    * Must not be <code>null</code>.
    * @throws PSModelException on underlying model operation failure.
    */
   public PSDesignObjectExportPage(final Enum objectType,
         final IStructuredSelection selection) throws PSModelException
   {
      super("designObjectExportPage");
      if (objectType == null)
      {
         throw new IllegalArgumentException("Object type must be specified");
      }
      if (!ArrayUtils.contains(PSObjectTypes.values(), objectType))
      {
         throw new IllegalArgumentException(
               "Unrecognized object type " + objectType);
      }
      if (selection == null)
      {
         throw new IllegalArgumentException("Selection must be specified");
      }
      m_objectType = objectType;
      m_selection = selection;
      setPageComplete(false);
      setDescription(getMessage("label.pageDescription"));
      m_references = loadRefs().toArray(new IPSReference[0]);
   }

   /**
    * Loads references of object type {@link #m_objectType} ordered by name.
    *
    * @return list of references. Never <code>null</code>.
    * @throws PSModelException on model failure.
    */
   @SuppressWarnings("unchecked")
   private List<IPSReference> loadRefs() throws PSModelException
   {
      final List[] refsContainer = new List[1];
      final Exception[] exceptionContainer = new Exception[1];
      BusyIndicator.showWhile(null, new Runnable()
      {
         public void run()
         {
            try
            {
               final IPSCmsModel model =
                  PSCoreFactory.getInstance().getModel(m_objectType);
               final List<IPSReference> refs =
                  new ArrayList<IPSReference>(
                     (model instanceof IPSContentTypeModel) ?
                        ((IPSContentTypeModel)model).
                        getUseableContentTypes(false) :
                           model.catalog());
               Collections.<IPSReference>sort(refs, new PSReferenceComparator());
               refsContainer[0] = refs;
            }
            catch (PSModelException e)
            {
               exceptionContainer[0] = e;
            }
            catch (RuntimeException e)
            {
               exceptionContainer[0] = e;
            }
         }
      });

      if (exceptionContainer[0] == null)
      {
         // Ok
      }
      else if (exceptionContainer[0] instanceof PSModelException)
      {
         throw (PSModelException) exceptionContainer[0];
      }
      else if (exceptionContainer[0] instanceof RuntimeException)
      {
         throw (RuntimeException) exceptionContainer[0];
      }
      else
      {
         throw new RuntimeException("Unknown exception", exceptionContainer[0]);
      }
      return refsContainer[0];
   }

   // see base
   public void createControl(Composite parent)
   {
      initializeDialogUnits(parent);

      final Composite container = new Composite(parent, SWT.NONE);
      setControl(container);

      container.setLayout(new GridLayout());
      container.setLayoutData(
            new GridData(GridData.FILL, GridData.FILL, true, true));

      createPathPane(container);
      createObjectListPane(container);
      initData();
      Dialog.applyDialogFont(container);
   }

   /**
    * Initializes controls with data.
    */
   private void initData()
   {
      final String dir = getPreferenceStore().getString(getDirPreferenceKey());
      m_exportDirField.setText(StringUtils.isBlank(dir) ? "" : dir);
   }

   /**
    * Create the area where you select the directory to export to.
    * 
    * @param container the container to create this control in.
    * Assumed not <code>null</code>.
    */
   private void createPathPane(Composite container)
   {
      final Composite pathGroup = new Composite(container, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 3;
      layout.makeColumnsEqualWidth = false;
      layout.marginWidth = 0;
      pathGroup.setLayout(layout);
      pathGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      {
         final Label dirLabel = new Label(pathGroup, SWT.NONE);
         dirLabel.setText(getMessage("label.toDirectory"));
      }

      // root directory entry field
      m_exportDirField = new Text(pathGroup, SWT.BORDER);
      m_exportDirField.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
      m_exportDirField.addModifyListener(new ModifyListener()
      {
         public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
         {
            checkPageComplete();
         }
      });

      // browse button
      m_browseButton = new Button(pathGroup, SWT.PUSH);
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
   }

   /**
    * Notifies wizard whether this page is complete.
    */
   private void checkPageComplete()
   {
      setPageComplete(isValidExportDir()
            && m_objectList.getCheckedElements().length > 0);
      if (!isValidExportDir())
      {
         setErrorMessage(getMessage("message.enterDestination"));
      }
      else if (m_objectList.getCheckedElements().length == 0)
      {
         setErrorMessage(getMessage("message.specifyObjects"));
      }
      else
      {
         setErrorMessage(null);
      }
   }

   /**
    * Returns <code>true</code> if valid export directory directory is specified. 
    * @return <code>true</code> if user specified existing file which is a
    * directory.
    */
   private boolean isValidExportDir()
   {
      return StringUtils.isNotBlank(getExportDir())
            && new File(getExportDir()).isDirectory();
   }

   /**
    * Is called when the browse button has been selected.
    * Shows dialog to select a location.
    */
   private void handleLocationDirectoryButtonPressed()
   {

      final DirectoryDialog dialog = new DirectoryDialog(m_exportDirField
            .getShell());
      dialog.setText(getMessage("destinationDirectory.title"));
      dialog.setMessage(getMessage("destinationDirectory.message"));

      final String dirName = getExportDir();

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
         m_exportDirField.setText(selectedDirectory);
      }
   }

   /**
    * Create the checkbox list for design objects to select from.
    * 
    * @param container the container to create this control in.
    * Assumed not <code>null</code>.
    */
   private void createObjectListPane(final Composite container)
   {
      final Label title = new Label(container, SWT.NONE);
      title.setText(getMessage("label.objectList"));

      final Composite listComposite = new Composite(container, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      layout.marginWidth = 0;
      layout.makeColumnsEqualWidth = false;
      listComposite.setLayout(layout);

      listComposite.setLayoutData(
          new GridData(GridData.FILL, SWT.FILL, true, true));

      m_objectList = new CheckboxTreeViewer(listComposite, SWT.BORDER);
      {
         final GridData gridData =
               new GridData(GridData.FILL, SWT.FILL, true, true);
         gridData.heightHint = getShell().getDisplay().getBounds().height / 3;
         m_objectList.getControl().setLayoutData(gridData);
      }

      m_objectList.setContentProvider(new ITreeContentProvider()
      {
         // see interface
         @SuppressWarnings("unused")
         public Object[] getChildren(Object parentElement)
         {
            return null;
         }

         // see interface
         @SuppressWarnings("unused")
         public Object[] getElements(Object inputElement)
         {
            return m_references;
         }

         // see interface
         @SuppressWarnings("unused")
         public boolean hasChildren(Object element)
         {
            return false;
         }

         // see interface
         @SuppressWarnings("unused")
         public Object getParent(Object element)
         {
            return null;
         }

         // see interface
         public void dispose()
         {
         }

         // see interface
         @SuppressWarnings("unused")
         public void inputChanged(Viewer viewer, Object oldInput,
               Object newInput)
         {
         }
      });

      m_objectList.setLabelProvider(new LabelProvider()
      {
         @Override
         public String getText(Object element)
         {
            return ((IPSReference) element).getName();
         }
      });

      m_objectList.setInput(this);
      makeInitialSelection();
      m_objectList.addSelectionChangedListener(new ISelectionChangedListener()
      {
         @SuppressWarnings("unused")
         public void selectionChanged(SelectionChangedEvent event)
         {
            checkPageComplete();
         }
      });
      createSelectionButtons(listComposite);
   }

   /**
    * Makes initial selection of listed objects.
    * Selects those objects which were selected in a view. 
    */
   private void makeInitialSelection()
   {
      final List<IPSReference> refs = new ArrayList<IPSReference>();
      for (final Object selected : m_selection.toList())
      {
         if (selected instanceof PSUiReference)
         {
            final PSUiReference uiref = (PSUiReference) selected;
            if (uiref.getReference() != null)
            {
               if (m_objectType.equals(
                     uiref.getReference().getObjectType().getPrimaryType()))
               {
                  refs.add(uiref.getReference());
               }
            }
         }
      }

      m_objectList.setCheckedElements(refs.toArray());
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

      final Button selectAll = new Button(buttonsComposite, SWT.PUSH);
      selectAll.setText(getMessage("label.selectAll"));
      selectAll.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            m_objectList.setCheckedElements(m_references);
            checkPageComplete();
         }
      });

      setButtonLayoutData(selectAll);

      final Button deselectAll = new Button(buttonsComposite, SWT.PUSH);
      deselectAll.setText(getMessage("label.deselectAll"));
      deselectAll.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            m_objectList.setCheckedElements(new Object[0]);
            checkPageComplete();
         }
      });
      setButtonLayoutData(deselectAll);
   }
   
   /**
    * Returns references selected for export.
    * @return references selected by user. Can be empty, never <code>null</code>.
    */
   public List<IPSReference> getSelectedObjects()
   {
      // remember path in preferences
      if (new File(getExportDir()).isDirectory())
      {
         getPreferenceStore().setValue(getDirPreferenceKey(), getExportDir());
      }

      final List<IPSReference> selected = new ArrayList<IPSReference>();
      for (final Object ref : m_objectList.getCheckedElements())
      {
         selected.add((IPSReference) ref);
      }
      return selected;
   }

   /**
    * Directory to export to.
    * @return direstory user selected for exporting to.
    * Can be empty, never <code>null</code>.
    */
   public String getExportDir()
   {
      return m_exportDirField.getText().trim();
   }

   /**
    * Generates key for user preferences for last visited directory.
    * Preferences key is specific to an object type.
    * @return user preferences key for last visited directory.
    * Never <code>null</code>.
    */
   private String getDirPreferenceKey()
   {
      return "PSDesignObjectExportPage.dir." + m_objectType.name();
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
      return PSMessages.getString("PSDesignObjectExportPage." + key);
   }

   /**
    * Object type of design objects to select.
    * Never <code>null</code>.
    */
   private final Enum m_objectType;
   
   /**
    * Selection the current object selection. Not <code>null</code>.
    */
   private final IStructuredSelection m_selection;

   /**
    * The references to show for selecting.
    * Never <code>null</code>. Can be empty.
    */
   private final IPSReference[] m_references;

   /**
    * Field to enter directory to export design objects to.
    * Not <code>null</code> after UI initialization.
    */
   private Text m_exportDirField;

   /**
    * Button to call directory selection dialog.
    * Not <code>null</code> after UI initialization.
    */
   private Button m_browseButton;

   /**
    * Lists design objects to select from. 
    * Not <code>null</code> after UI initialization.
    */
   private CheckboxTreeViewer m_objectList;
}

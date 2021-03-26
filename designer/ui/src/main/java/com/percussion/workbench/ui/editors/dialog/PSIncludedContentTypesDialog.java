/******************************************************************************
*
* [ PSIncludedContentTypesDialog.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.client.IPSReference;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog that allows the association of a content type(s) with a template.
 * This dialog will only return content type/template pairs that have
 * both values. A warning will be displayed if not all templates have
 * an associated content type.
 */
public class PSIncludedContentTypesDialog extends PSDialog
   implements IPSUiConstants
{  
   
   /**
    * Create the dialog
    * @param parentShell the parent shell
    * @param templates collection of templates, cannot be <code>null</code>,
    * may be empty.
    * @param allCTypes collection of all content types, cannot be 
    * <code>null</code>, may be empty.
    * @param associations map of associated content types, cannot be 
    * <code>null</code>, may be empty.
    */
   public PSIncludedContentTypesDialog(Shell parentShell,
      Collection<IPSReference> templates, Collection<IPSReference> allCTypes,
      Map<IPSReference, Collection<IPSReference>> associations)
   {
      super(parentShell);
      setBlockOnOpen(true);
      if(templates == null)
         throw new IllegalArgumentException("templates cannot be null."); //$NON-NLS-1$
      if(allCTypes == null)
         throw new IllegalArgumentException("cTypes cannot be null."); //$NON-NLS-1$
      if(associations == null)
         throw new IllegalArgumentException("associations cannot be null."); //$NON-NLS-1$
      m_templates = templates;
      m_cTypes = allCTypes;
      m_associated = associations;
     
   }

   /**
    * Create contents of the dialog
    * @param parent
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FormLayout());

      m_templateLabel = new Label(container, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 20);
      formData.left = new FormAttachment(0, 5);
      m_templateLabel.setLayoutData(formData);
      m_templateLabel.setText(PSMessages.getString(
         "PSIncludedContentTypesDialog.template.label")); //$NON-NLS-1$

      m_templateListViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
      m_templateListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent e)
         {
            loadSlushControl(e.getSelection());
         }
      });
      m_templateListViewer.setContentProvider(new PSDefaultContentProvider());
      m_templateListViewer.setLabelProvider(new PSReferenceLabelProvider());
      m_templateList = m_templateListViewer.getList();
      final FormData formData_1 = new FormData();
      formData_1.height = 100;
      formData_1.right = new FormAttachment(100, -5);
      formData_1.top = new FormAttachment(m_templateLabel, 0, SWT.BOTTOM);
      formData_1.left = new FormAttachment(m_templateLabel, 0, SWT.LEFT);
      m_templateList.setLayoutData(formData_1);
      m_templateListViewer.setInput(m_templates);

      m_slush = new PSSlushBucket(container, SWT.NONE, 
         PSMessages.getString(
            "PSIncludedContentTypesDialog.availableContentType.label"), //$NON-NLS-1$
         PSMessages.getString(
            "PSIncludedContentTypesDialog.includedContentTypes.label"),
         new PSReferenceLabelProvider()); //$NON-NLS-1$
      final FormData formData_2 = new FormData();
      formData_2.height = 215;
      formData_2.right = new FormAttachment(m_templateList, 0, SWT.RIGHT);
      formData_2.top = new FormAttachment(m_templateList, 15, SWT.DEFAULT);
      formData_2.left = new FormAttachment(m_templateList, 0, SWT.LEFT);
      m_slush.setLayoutData(formData_2);
      
      m_slush.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings({"synthetic-access","unchecked"}) //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               PSSlushBucket control = (PSSlushBucket)e.getSource();
               StructuredSelection sel = 
                  (StructuredSelection)m_templateListViewer.getSelection();
               IPSReference template = (IPSReference)sel.getFirstElement();
               java.util.List selections = control.getSelections();
               m_selections.put(template, selections);
            }
            
         });

      m_filterLabel = new Label(container, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_slush, 
         5 + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_slush, 0, SWT.LEFT);
      m_filterLabel.setLayoutData(formData_3);
      m_filterLabel.setText(PSMessages.getString(
         "PSIncludedContentTypesDialog.filter.label")); //$NON-NLS-1$

      m_filterText = new Text(container, SWT.BORDER);
      m_filterText.addModifyListener(new ModifyListener() {
         public void modifyText(ModifyEvent e)
         {
            m_slush.filterAvailableList(((Text)e.getSource()).getText());
         }
      });
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(0, 220);
      formData_4.top = new FormAttachment(m_filterLabel, 
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_4.left = new FormAttachment(m_filterLabel, 0, SWT.RIGHT);
      m_filterText.setLayoutData(formData_4);
      // Create associations filter
      m_assocFilter = new AssociatedCTypeFilter(m_associated);
      
      m_includeOnlyAssociatedButton = new Button(container, SWT.CHECK);
      m_includeOnlyAssociatedButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e)
         {
            if(((Button)e.getSource()).getSelection())
            {
               m_slush.addAvailableListFilter(m_assocFilter);
            }
            else
            {
               m_slush.removeAvailableListFilter(m_assocFilter);
            }
         }
      });
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_filterText, 5, SWT.DEFAULT);
      formData_5.left = new FormAttachment(m_filterLabel, 0, SWT.LEFT);
      m_includeOnlyAssociatedButton.setLayoutData(formData_5);
      m_includeOnlyAssociatedButton.setText(PSMessages.getString(
         "PSIncludedContentTypesDialog.includeOnlyAssociated.label")); //$NON-NLS-1$
      // Select the first entry
      if(!m_templates.isEmpty())
      {
         m_templateList.select(0);
         loadSlushControl(m_templateListViewer.getSelection());
      }
         
      return container;
   }
   
   /**
    * Returns all valid content type/ template pairs. A valid pair is
    * one that has both values.
    * @return collection of valid pairs, never <code>null</code>, 
    * may be empty.
    */
   public Collection<PSPair<IPSGuid, IPSGuid>> getValues()
   {
      return m_finalSelections;
   }

   /**
    * Create contents of the button bar
    * @param parent
    */
   @Override
   protected void createButtonsForButtonBar(Composite parent)
   {
      createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
         true);
      createButton(parent, IDialogConstants.CANCEL_ID,
         IDialogConstants.CANCEL_LABEL, false);
   }   
   
   
   /* 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   @Override
   protected void okPressed()
   {
      m_finalSelections.clear();
      java.util.List<IPSReference> templates = 
         (java.util.List<IPSReference>)m_templateListViewer.getInput();
      boolean warn = false;
      for(IPSReference temp : templates)
      {
         Collection<IPSReference> cTypes = m_selections.get(temp);
         if(cTypes != null && !cTypes.isEmpty())
         {
            for(IPSReference cType : cTypes)
               m_finalSelections.add(
                  new PSPair(cType.getId(), temp.getId()));
         }
         else
         {
            warn = true;
         }
      }
      if(warn)
      {
         MessageBox msgBox = new MessageBox(
            PSIncludedContentTypesDialog.this.getShell(),
            SWT.YES | SWT.NO | SWT.ICON_WARNING);
         msgBox.setText(PSMessages.getString(
            "PSIncludedContentTypesDialog.warning.title")); //$NON-NLS-1$
         msgBox.setMessage(PSMessages.getString(
            "PSIncludedContentTypesDialog.warning.missingAssociations")); //$NON-NLS-1$
         int status = msgBox.open();
         if(status == SWT.NO)
            return;
      }
      super.okPressed();
   }
   
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   protected void loadSlushControl(ISelection selection)
   {
      StructuredSelection sel = (StructuredSelection)selection;
      IPSReference ref = (IPSReference)sel.getFirstElement();      
      Collection<IPSReference> refs = m_selections.get(ref);      
      m_slush.setValues((java.util.List)m_cTypes, (java.util.List)refs);
         
   }

   /**
    * Return the initial size of the dialog
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(500, 500);
   }
   
   /* 
    * @see org.eclipse.jface.window.Window#configureShell(
    * org.eclipse.swt.widgets.Shell)
    */
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString(
         "PSIncludedContentTypesDialog.title")); //$NON-NLS-1$
   }
   
   /**
    * Filter that will only allow associated content types for the
    * selected template.
    */
   class AssociatedCTypeFilter extends ViewerFilter
   {
      
      AssociatedCTypeFilter(
         Map<IPSReference, Collection<IPSReference>> associated)
      {
         mi_associated = associated;
      }
      
      /* 
       * @see org.eclipse.jface.viewers.ViewerFilter#select(
       * org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
       */
      @Override
      public boolean select(@SuppressWarnings("unused") Viewer viewer,
         @SuppressWarnings("unused") Object parentElement, Object element)
      {
         IPSReference cType = (IPSReference)element;
         StructuredSelection selection = 
            (StructuredSelection)m_templateListViewer.getSelection();
         if(selection.isEmpty())
            return true;
         IPSReference template = (IPSReference)selection.getFirstElement();
         Collection<IPSReference> temps = mi_associated.get(cType);
         if(temps != null && temps.contains(template))
         {
            return true;
         }
         return false;
      }
      
      /**
       * local reference to the associations map
       */
      private Map<IPSReference, Collection<IPSReference>> mi_associated;
      
      
   }
   
   //Controls
   private Button m_includeOnlyAssociatedButton;
   private Text m_filterText;
   private Label m_filterLabel;
   protected PSSlushBucket m_slush;
   private List m_templateList;
   private ListViewer m_templateListViewer;   
   private Label m_templateLabel;
   
   /**
    * Templates passed in. Initialized in ctor, never <code>
    * null</code> after that.
    */
   private Collection<IPSReference> m_templates;
   
   /**
    * Collection of all existing contenttypes. Initialized
    * in ctor. Never <code>null</code> after that.
    */
   private Collection<IPSReference> m_cTypes;
   
   /**
    * Map of all associated content types. Initialized
    * in ctor. Never <code>null</code> after that.
    */
   private  Map<IPSReference, Collection<IPSReference>> m_associated;
   
   /**
    * Map of templates and their selected content types.
    */
   private Map<IPSReference, Collection<IPSReference>> m_selections =
      new HashMap<IPSReference, Collection<IPSReference>>();
   
   /**
    * The final selection of content type to template pairs that is
    * extracted from the ongoing selection list.
    */
   private Collection<PSPair<IPSGuid, IPSGuid>> m_finalSelections =
      new ArrayList<PSPair<IPSGuid, IPSGuid>>();
   
   /**
    * The associations filter, initialized in {@link #createDialogArea(Composite)}
    * and never <code>null</code> after that.
    */
   protected AssociatedCTypeFilter m_assocFilter;
   

}

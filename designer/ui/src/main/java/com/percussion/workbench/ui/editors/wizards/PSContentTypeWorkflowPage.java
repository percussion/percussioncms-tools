/******************************************************************************
 *
 * [ PSContentTypeWorkflowPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSSecurityUtils;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValueRequiredValidator;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Workflow page of Content Type wizard
 */
public class PSContentTypeWorkflowPage extends PSWizardPageBase
      implements
         IPSUiConstants,
         ISelectionChangedListener
{
   /**
    * Constructor
    *
    */
   public PSContentTypeWorkflowPage() {
      super(
            PSMessages.getString("PSContentTypePropertiesPage.page.name"), //$NON-NLS-1$
            PSMessages.getString("PSContentTypePropertiesPage.page.title"), //$NON-NLS-1$
            null); 
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {
      final Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      List<IPSReference> selected = new ArrayList<IPSReference>();
      List<IPSReference> available = new ArrayList<IPSReference>();
      IPSControlValueValidator required = new PSControlValueRequiredValidator();
      m_slushComp = new WorkflowSlushBucketComposite(
            comp, SWT.NONE,
            PSMessages.getString("PSContentTypeWorkflowPage.label.availableworkflows"), //$NON-NLS-1$
            PSMessages.getString("PSContentTypeWorkflowPage.label.allowedworkflows"), //$NON-NLS-1$
            available, selected, new PSReferenceLabelProvider());
      final FormData formData_1 = new FormData();
      formData_1.left = new FormAttachment(0, 0);
      formData_1.right = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      m_slushComp.setLayoutData(formData_1);
      registerControl(PSMessages.getString("PSContentTypeWorkflowPage.label.allowedworkflows"), //$NON-NLS-1$
            m_slushComp, null);

      final Composite filterComposite = new Composite(comp, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.left = new FormAttachment(0, 0);
      formData_3.right = new FormAttachment(100, 0);
      formData_3.top = new FormAttachment(m_slushComp,
            15 + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,
            SWT.BOTTOM);
      filterComposite.setLayoutData(formData_3);
      filterComposite.setLayout(new FormLayout());

      final Label m_filterLabel = new Label(filterComposite, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(20,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      formData.left = new FormAttachment(0,0);
      m_filterLabel.setLayoutData(formData);
      m_filterLabel.setText(PSMessages.getString("PSContentTypeWorkflowPage.label.filter")); //$NON-NLS-1$

      m_filterText = new Text(filterComposite, SWT.BORDER);
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(40,0);
      formData_2.top = new FormAttachment(
            m_filterLabel, 
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_2.left = new FormAttachment(
            m_filterLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_filterText.setLayoutData(formData_2);
      m_filterText.addModifyListener(new ModifyListener() {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
         {
            m_slushComp.filterAvailableList(m_filterText.getText());
         }
      });

      final Label m_defualtWorkflowLabel = new Label(filterComposite, SWT.RIGHT);
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(m_filterLabel,0,SWT.TOP);
      formData_4.left = new FormAttachment(m_filterText, LABEL_HSPACE_OFFSET * 2);
      formData_4.right = new FormAttachment(60,0);
      m_defualtWorkflowLabel.setLayoutData(formData_4);
      m_defualtWorkflowLabel.setText(PSMessages.getString("PSContentTypeWorkflowPage.label.defaultworkflow")); //$NON-NLS-1$

      m_defaultWfCombo = new ComboViewer(filterComposite,
            SWT.BORDER | SWT.READ_ONLY);
      final Combo combo = m_defaultWfCombo.getCombo();
      final FormData formData_5 = new FormData();
      formData_5.right = new FormAttachment(100,0);
      formData_5.top = new FormAttachment(
            m_filterLabel, 
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_5.left = new FormAttachment(
            m_defualtWorkflowLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      combo.setLayoutData(formData_5);
      m_defaultWfCombo.setContentProvider(new PSDefaultContentProvider());
      m_defaultWfCombo.setLabelProvider(new PSReferenceLabelProvider());
      List<IPSReference> defWfList = new ArrayList<IPSReference>();
      defWfList.addAll(m_workflows);
      m_defaultWfCombo.setInput(defWfList);
      m_defaultWfCombo.addSelectionChangedListener(this);
      registerControl(PSMessages.getString("PSContentTypeWorkflowPage.label.defaultworkflow"), //$NON-NLS-1$
            m_defaultWfCombo, new IPSControlValueValidator[]
            {required});
      setControl(comp);
   }
   
   /**
    * Gets the list of workflows based on the selected communities.
    * @return List of IPSReference of workflows.
    */
   public List<IPSReference> getCommunityWorkflows()
   {
      IWizardPage prevPage = getPreviousPage();
      if (!(prevPage instanceof PSContentTypePropertiesPage))
         throw new IllegalStateException(
               "The prev page must be of PSContentTypePropertiesPage"); //$NON-NLS-1$
      List<IPSReference> cList =
         ((PSContentTypePropertiesPage)prevPage).getSelectedCommunities();
      List<IPSReference> availWorkflows = new ArrayList<IPSReference>();
      if (!cList.isEmpty())
      {
         try
         {
            availWorkflows.addAll(PSSecurityUtils
                  .getObjectsByCommunityVisibility(cList,
                        PSTypeEnum.WORKFLOW));
         }
         catch (PSModelException e1)
         {
            PSWorkbenchPlugin.handleException(
                  "Contenttype Workflow Wizard Page",
                  PSMessages.getString(
                     "PSContentTypePropertiesPage.error.title.communityworkflows"), //$NON-NLS-1$ //$NON-NLS-2$
                  PSMessages.getString(
                     "PSContentTypePropertiesPage.error.message.communityworkflows"),e1); //$NON-NLS-1$
         }
      }
      return availWorkflows;
   }   
   
   /**
    * Update the workflows
    * 
    * @param selected the slush bucket values
    */
   public void updateWorkflows(List<IPSReference> available,
         List<IPSReference> selected, IPSReference defaultWf)
   {
      m_workflows.clear();
      m_workflows.addAll(available);
      Collections.sort(m_workflows, new Comparator<IPSReference>()
      {
         //see base class method for details
         public int compare(IPSReference ref1, IPSReference ref2)
         {
            return ref1.getName().compareToIgnoreCase(ref2.getName());
         }
      });
      m_slushComp.setWorkflows(m_workflows, selected, defaultWf);
   }

   /**
    * Gets the selected workflows.
    * 
    * @return reference of default workflow may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public List<IPSReference> getSelectedWorkflows()
   {
      return m_slushComp.getSelections();
   }

   /**
    * Gets the default workflow.
    * @return reference of default workflow may be <code>null</code>.
    */
   public IPSReference getDefaultWorkflow()
   {
      IStructuredSelection sel = (IStructuredSelection) m_defaultWfCombo.getSelection();
      IPSReference defRef = (IPSReference) sel.getFirstElement();
      return defRef;
   }

   
   /*
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    *      updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSItemDefinition contenttype = (PSItemDefinition) designObject;
      PSContentEditor editor = contenttype.getContentEditor();
      if (control == m_slushComp)
      {
         List<Integer> wfList = new ArrayList<Integer>();
         for(Object obj : m_slushComp.getSelections())
            wfList.add(new Integer((int)((IPSReference)obj).getId().longValue()));
         PSWorkflowInfo wfinfo = new PSWorkflowInfo(
               PSWorkflowInfo.TYPE_INCLUSIONARY, wfList);
         editor.setWorkflowInfo(wfinfo);
      }
      else if (control == m_defaultWfCombo.getCombo())
      {
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         IPSReference selDef = (IPSReference) m_defaultWfCombo
               .getElementAt(index);
         editor.setWorkflowId((int)selDef.getId().longValue());
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
    */
   public void selectionChanged(SelectionChangedEvent event)
   {
      if(event.getSource()==m_defaultWfCombo)
      {
         m_slushComp.defaultSelected();
      }
   }

   /**
    * Inner class that extends the PSSlushBucket control to add additional 
    * functionality like the changes made to the slush bucket affect
    * the default work flow combo box and vice versa.
    */
   private class WorkflowSlushBucketComposite extends PSSlushBucket 
   implements IPSUiConstants 
   {

      WorkflowSlushBucketComposite(Composite parent, int style,
            String availLabel, String selectLabel,
            java.util.List availableItems, java.util.List selections,
            ILabelProvider labelProvider) 
      {
         super(parent, style, availLabel, selectLabel, availableItems,
               selections, labelProvider);
      }
      
      @SuppressWarnings({"synthetic-access","unchecked"}) //$NON-NLS-1$ //$NON-NLS-2$
      public void defaultSelected()
      {
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         IPSReference selDef = (IPSReference) m_defaultWfCombo
               .getElementAt(index);
         List to = (List) getSelections();
         if (!to.contains(selDef))
         {
            List from = (List) getAvailable();
            from.remove(selDef);
            to.add(selDef);
            refreshLists();
            updateButtonStatus();
         }
      }
      
      @Override
      protected void onUnSelect()
      {
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         IPSReference selDef = (IPSReference) m_defaultWfCombo
               .getElementAt(index);
         StructuredSelection selection = getSelection();
         if (!selection.toList().contains(selDef))
         {
            super.onUnSelect();
            return;
         }
         PSContentTypePropertiesPage
               .showDefaultWorkflowInvalidWarning(getShell());
         super.onUnSelect();
         List to = getSelections();
         if (to.isEmpty())
         {
            m_defaultWfCombo.getCombo().deselectAll();
         }
         else
         {
            IPSReference fref = (IPSReference) to.get(0);
            int findex = m_workflows.indexOf(fref);
            m_defaultWfCombo.getCombo().select(findex);
         }
      }

      @Override
      protected void onUnSelectAll()
      {
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         if (index != -1)
         {
            PSContentTypePropertiesPage
                  .showDefaultWorkflowInvalidWarning(getShell());
            m_defaultWfCombo.getCombo().deselectAll();
         }
         super.onUnSelectAll();
      }

      @SuppressWarnings("synthetic-access")
      public void setWorkflows(List<IPSReference> available, List<IPSReference> selected, IPSReference defaultWf)
      {
         if(available != null)
         {
            //Filter selected from available
            List<IPSReference> avList = new ArrayList<IPSReference>(available);
            avList.removeAll(selected);
            setAvailable(avList);
            m_defaultWfCombo.setInput(available);
            m_defaultWfCombo.getCombo().select(available.indexOf(defaultWf));
         }
         if(selected != null)
         {
            setSelections(selected);
         }
      }
   }

   /**
    * Contains the list of all workflows visible to the currently selected
    * communities. Values set by the
    * {@link #updateWorkflows(List, List, IPSReference)} method, always in
    * ascending alpha order. Never <code>null</code>, may be empty.
    */
   private List<IPSReference> m_workflows = new ArrayList<IPSReference>();

   /**
    * Controls
    */
   private WorkflowSlushBucketComposite m_slushComp;
   private Text m_filterText;
   private ComboViewer m_defaultWfCombo;

}

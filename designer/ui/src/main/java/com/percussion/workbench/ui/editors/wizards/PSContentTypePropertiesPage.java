/******************************************************************************
 *
 * [ PSContentTypePropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSSelectCommunitiesComposite;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The property page of the Content Type wizard. 
 */
public class PSContentTypePropertiesPage extends PSWizardPageBase
      implements IPSUiConstants
{
   /**
    * Ctor for Content type properties page of content type wizard.
    */
   public PSContentTypePropertiesPage() 
   {
      super(
            PSMessages.getString("PSContentTypePropertiesPage.page.name"), //$NON-NLS-1$
            PSMessages.getString("PSContentTypePropertiesPage.page.title"), //$NON-NLS-1$
            null);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   @SuppressWarnings(
   {"unchecked", "synthetic-access"})//$NON-NLS-1$ //$NON-NLS-2$
   public void createControl(Composite parent)
   {
      final Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      PSControlValidatorFactory vFactory = PSControlValidatorFactory
            .getInstance();
      IPSControlValueValidator required = vFactory.getRequiredValidator();
      m_commonComp = new PSContentTypeNameLabelDesc(comp, SWT.NONE,
            PSMessages.getString("common.label.contentType"), //$NON-NLS-1$
            WIZARD_LABEL_NUMERATOR, PSNameLabelDesc.SHOW_ALL, this);
      final FormData formData_14 = new FormData();
      formData_14.left = new FormAttachment(0, 0);
      formData_14.right = new FormAttachment(100, 0);
      formData_14.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      m_commonComp.setLayoutData(formData_14);
      m_commonComp.getLabelText().setTextLimit(255);
      m_communitiesControl = new PSSelectCommunitiesComposite(comp, SWT.NONE);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0);
         formData.right = new FormAttachment(100);
         formData.top = new FormAttachment(m_commonComp, TEXT_VSPACE_OFFSET, SWT.BOTTOM);
         formData.bottom = new FormAttachment(100);
         m_communitiesControl.setLayoutData(formData);
      }
      registerControl(m_communitiesControl.getVisibleLabel(),
            m_communitiesControl.getSlushControl(),
            new IPSControlValueValidator[]
            {required}, PSControlInfo.TYPE_COMMUNITY);
      m_communitiesControl.getSlushControl().addSelectionListener(
            new SelectionAdapter()
            {
               @Override
               public void widgetSelected(
                     @SuppressWarnings("unused") SelectionEvent e)
               {
                  IWizardPage nextPage = getNextPage();
                  if (!(nextPage instanceof PSContentTypeWorkflowPage))
                     throw new IllegalStateException(
                           "The next page must be of PSContentTypeWorkflowPage"); //$NON-NLS-1$
                  List cList = m_communitiesControl.getSlushControl()
                        .getSelections();
                  List<IPSReference> availWorkflows = new ArrayList();
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
                                 "PSContentTypePropertiesPage..error.title.communityworkflows"), //$NON-NLS-1$ //$NON-NLS-2$
                              PSMessages.getString(
                                 "PSContentTypePropertiesPage..error.message.communityworkflows"),e1); //$NON-NLS-1$
                     }
                  }
                  List<IPSReference> selected = ((PSContentTypeWorkflowPage) nextPage)
                        .getSelectedWorkflows();
                  if (!selected.isEmpty())
                  {
                     List<IPSReference> temp = new ArrayList(selected);
                     temp.removeAll(availWorkflows);
                     selected.removeAll(temp);
                  }
                  IPSReference ref = ((PSContentTypeWorkflowPage) nextPage)
                        .getDefaultWorkflow();
                  if (ref != null && !selected.contains(ref))
                  {
                     showDefaultWorkflowInvalidWarning(getShell());
                     ref = null;
                  }
                  ((PSContentTypeWorkflowPage) nextPage).updateWorkflows(
                        availWorkflows, selected, ref);
               }
            });
      setControl(comp);
   }

   /**
    * Pops up a warning that the currently selected default workflow is no 
    * longer valid.
    */
   static void showDefaultWorkflowInvalidWarning(Shell parent)
   {
      MessageDialog.openWarning(parent,
         PSMessages.getString(
            "common.error.defaultWorkflowInvalid.title"), //$NON-NLS-1$
         PSMessages.getString(
            "common.error.defaultWorkflowInvalid.message")); //$NON-NLS-1$
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSItemDefinition contenttype = (PSItemDefinition) designObject;
      
      if (control == m_commonComp.getNameText())
      {
         String name = ((Text) m_commonComp.getNameText()).getText();
         contenttype.setName(name);
         PSContentEditorPipe pipe = (PSContentEditorPipe) contenttype
            .getContentEditor().getPipe();
         PSContentEditorMapper ceMapper = pipe.getMapper();
         PSFieldSet parentFieldSet = ceMapper.getFieldSet();
         PSDisplayMapper parentMapper = ceMapper.getUIDefinition().getDisplayMapper();
         parentFieldSet.setName(CONTENTTYPE_TABLE_PREFIX + name);
         parentMapper.setFieldSetRef(CONTENTTYPE_TABLE_PREFIX + name);
         
         // Enable related content for new c types.
         contenttype.getContentEditor().enableRelatedContent(true);
      }
      else if (control == m_commonComp.getDescriptionText())
      {
         contenttype
               .setDescription(m_commonComp.getDescriptionText().getText());
      }
      else if (control == m_commonComp.getLabelText())
      {
         contenttype.setLabel(m_commonComp.getLabelText().getText());
      }
   }

   /**
    * Gets the list of selected communities.
    * 
    * @return list of communities selected in this page. May be empty but never
    *         <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public List<IPSReference> getSelectedCommunities()
   {
      List<IPSReference> cList = new ArrayList<IPSReference>();
      cList.addAll(m_communitiesControl.getSlushControl().getSelections());
      return cList;
   }

   /**
    * Adds a validator to prevent characters, invalid for content type names.    
    */
   private final class PSContentTypeNameLabelDesc extends PSNameLabelDesc
   {
      private PSContentTypeNameLabelDesc(Composite parent, int style,
            String prefix, int num, int options,
            IPSDesignerObjectUpdater editor)
      {
         super(parent, style, prefix, num, options, editor);
      }

      @Override
      protected IPSControlValueValidator[] getNameTextValidators(IPSCmsModel model)
      {
         if (model == null)
         {
            throw new IllegalArgumentException("Model should not be null");
         }
         return new IPSControlValueValidator[]{
            getValidatorFactory().getRequiredValidator(),
            getValidatorFactory().getDuplicateNameValidator(model),
            getValidatorFactory().getNoWhitespaceValidator(),
            getValidatorFactory().getContentTypeNameValidator()};
      }
   }

   /**
    * Common composite for name label and description
    */
   private PSNameLabelDesc m_commonComp;
   
   /**
    * Communities selection control.
    */
   private PSSelectCommunitiesComposite m_communitiesControl;
   
   /**
    * Constant to append infront of the fieldset name. Content editor tables
    * will be created with this name.
    */
   private static final String CONTENTTYPE_TABLE_PREFIX = "CT_";  //$NON-NLS-1$
}

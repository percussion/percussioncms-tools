/******************************************************************************
 *
 * [ PSTemplateSlotsPage ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.controls.PSSlotsControl;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.WIZARD_TOP_OFFSET;

/**
 * Allows user to specify associated slots. 
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateSlotsPage extends PSWizardPageBase
{
   public PSTemplateSlotsPage(){
      super(getMessage("PSTemplateWizard.title"));   //$NON-NLS-1$
      setTitle(getMessage("PSTemplateWizard.screenTitle")); //$NON-NLS-1$
   }

   public void createControl(Composite parent)
   {
      final Composite container = new Composite(parent, SWT.NULL);
      container.setLayout(new FormLayout());
      setControl(container);
      
      m_slotsControl = createSlotsControl(container);
      registerControl("PSSlotsControl.label.containedSlots",
            m_slotsControl.getSelectionControl(), null);
   }

   /**
    * Creates control to select slots.
    */
   private PSSlotsControl createSlotsControl(final Composite container)
   {
      final PSSlotsControl slotsControl = new PSSlotsControl(container, SWT.NONE);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      formData.bottom = new FormAttachment(100, -LABEL_VSPACE_OFFSET);
      // do not allow the control to grow too big
      formData.height = getShell().getSize().y / 3 * 2;
      slotsControl.setLayoutData(formData);
      
      slotsControl.loadControlValues(new PSUiAssemblyTemplate());
      return slotsControl;
   }

   public void updateDesignerObject(Object designObject, Object control)
   {
      final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) designObject;
      if (control.equals(m_slotsControl.getSelectionControl()))
      {
         m_slotsControl.updateTemplate(template);
      }
      else
      {
         throw new IllegalArgumentException("Unrecognized control: " + control); //$NON-NLS-1$
      }
   }
   
   @Override
   public IWizardPage getNextPage()
   {
      assert super.getNextPage() instanceof PSTemplateContentTypesPage;
      if (((PSTemplateWizard) getWizard()).skipContentTypesPage())
      {
         return super.getNextPage().getNextPage();
      }
      return super.getNextPage();
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpHintKey(com.percussion.workbench.ui.util.PSControlInfo)
    */
   @Override
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      if(m_helpHintKeyHelper == null)
      {
         m_helpHintKeyHelper = new PSHelpHintKeyHelper(new String[]
         {            
            "PSSlotsControl.label.containedSlots",
               "contained_slots"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   private PSSlotsControl m_slotsControl;
}

/******************************************************************************
 *
 * [ PSTemplateContentTypesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.client.objectstore.PSUiTemplateSlot;
import com.percussion.workbench.ui.controls.PSContentTypesControl;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.WIZARD_TOP_OFFSET;

/**
 * Allows user to pick content types for the template.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateContentTypesPage extends PSWizardPageBase
{
   public PSTemplateContentTypesPage()
   {
      super(getMessage("PSTemplateWizard.title"));   //$NON-NLS-1$
      setTitle(getMessage("PSTemplateWizard.screenTitle")); //$NON-NLS-1$
   }

   public void createControl(Composite parent)
   {
      final Composite container = new Composite(parent, SWT.NULL);
      container.setLayout(new FormLayout());
      setControl(container);
      
      m_contentTypesControl = createContentTypesControl(container);
      
      registerControl("PSContentTypesControl.label.associatedContentTypes",
            m_contentTypesControl, null);
   }

   /**
    * Creates control to select content types.
    */
   private PSContentTypesControl createContentTypesControl(
         final Composite container)
   {
      final PSContentTypesControl contentTypesControl =
            new PSContentTypesControl(container, SWT.NONE);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      formData.bottom = new FormAttachment(100, -LABEL_VSPACE_OFFSET);
      // do not allow the control to grow too big
      formData.height = getShell().getSize().y / 3 * 2;
      contentTypesControl.setLayoutData(formData);

      contentTypesControl.loadControlValues(new PSUiTemplateSlot());
      return contentTypesControl;
   }
   
   // see base
   public void updateDesignerObject(Object designObject, Object control)
   {
      if (skipContentTypesPage())
      {
         return;
      }
      final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) designObject;
      if (control.equals(m_contentTypesControl))
      {
         m_contentTypesControl.updateTemplate(template);
      }
      else
      {
         throw new IllegalArgumentException("Unrecognized control: " + control); //$NON-NLS-1$
      }
   }
   
   /**
    * Convenience method to access {@link PSTemplateWizard#skipContentTypesPage()}.
    */
   boolean skipContentTypesPage()
   {
      return ((PSTemplateWizard) getWizard()).skipContentTypesPage();
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
            "PSContentTypesControl.label.associatedContentTypes",
               "associated_content_types"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   private PSContentTypesControl m_contentTypesControl;
}

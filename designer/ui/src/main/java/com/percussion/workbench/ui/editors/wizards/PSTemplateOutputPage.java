/******************************************************************************
 *
 * [ PSTemplateOutputPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import com.percussion.workbench.ui.controls.PSTemplateOutputComposite;
import com.percussion.workbench.ui.editors.wizards.PSTemplateTypePage.TypeButton;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import com.percussion.workbench.ui.validators.PSControlValueRequiredValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.List;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.WIZARD_TOP_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.WIZARD_VALUE_NUMERATOR;

/**
 * Shows template output format.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateOutputPage extends PSWizardPageBase
// wireframe WB-1226
{
   /**
    * Initializes the page.
    */
   public PSTemplateOutputPage()
   {
      super(getMessage("PSTemplateWizard.title"));   //$NON-NLS-1$
      setTitle(getMessage("PSTemplateWizard.screenTitle"));  //$NON-NLS-1$
   }

   // see base
   public void createControl(Composite parent)
   {
      final Composite container = new Composite(parent, SWT.NONE);
      container.setLayout(new FormLayout());
      setControl(container);
      
      m_outputControl = createOutputControl(container);

      registerControl("PSTemplateOutputComposite.label.assembler",
            m_outputControl.getAssemblerCombo(),
            new IPSControlValueValidator[] {getRequiredValidator()});
   }

   /**
    * Creates the main page control.
    */
   private PSTemplateOutputComposite createOutputControl(Composite container)
   {
      final PSTemplateOutputComposite outputControl =
            new PSTemplateOutputComposite(container, SWT.NULL, false, false,
                  false, true, false);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      formData.left = FORM_LEFT_SIDE;
      formData.right = FORM_RIGHT_SIDE;
      outputControl.setLayoutData(formData);
      return outputControl;
   }

   /**
    * Currently selected assembler.
    */
   public String getSelectedAssembler()
   {
      return m_outputControl.getSelectedAssembler();
   }
   
   /**
    * The "required" validator.
    */
   private PSControlValueRequiredValidator getRequiredValidator()
   {
      return PSControlValidatorFactory.getInstance().getRequiredValidator();
   }

   // see base
   public void updateDesignerObject(Object designObject, Object control)
   {
      if (skipOutputFormatPage())
      {
         return;
      }
      if (control.equals(m_outputControl.getAssemblerCombo()))
      {
         final PSUiAssemblyTemplate template =
               (PSUiAssemblyTemplate) designObject;
         m_outputControl.updateTemplate(template);
         if (template.getOutputFormat().equals(OutputFormat.Snippet))
         {
            template.setPublishWhen(PublishWhen.Never);
         }
         else if (template.getOutputFormat().equals(OutputFormat.Binary))
         {
            template.setMimeType(IPSMimeContentTypes.MIME_TYPE_OCTET_STREAM);
         }
      }
      else
      {
         throw new IllegalArgumentException("Unrecognized control: " + control); //$NON-NLS-1$
      }
   }

   /**
    * Convenience method to access {@link PSTemplateWizard#skipOutputFormatPage()}.
    */
   boolean skipOutputFormatPage()
   {
      return ((PSTemplateWizard) getWizard()).skipOutputFormatPage();
   }

   /**
    * Indicates whether the page should show configuration to pick assembler for
    * Global template. This configuration is as following - only assembler combo
    * is visible, only Velocity assembler and unrecognized assemblers are shown.
    */
   private boolean isGlobalTemplate()
   {
      return getTypePage().getTypeRadioSelection().equals(TypeButton.GLOBAL_FORMAT);
   }
   
   /**
    * The type page of this wizard.
    */
   private PSTemplateTypePage getTypePage()
   {
      return (PSTemplateTypePage) getPreviousPage();
   }

   @Override
   public void setVisible(final boolean visible)
   {
      if (visible)
      {
         m_outputControl.configureUiBeforeDisplaying(isGlobalTemplate());
      }
      super.setVisible(visible);
   }
   
   /**
    * The output format UI control - the only UI element on the page.
    */
   public PSTemplateOutputComposite getOutputControl()
   {
      return m_outputControl;
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
            "PSTemplateOutputComposite.label.assembler",
               "assembler"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   /**
    * Most to the left form attachment. 
    */
   private static final FormAttachment FORM_LEFT_SIDE =
         new FormAttachment(0, COMMON_BORDER_OFFSET);

   /**
    * Most to the right form attachment. 
    */
   private static final FormAttachment FORM_RIGHT_SIDE =
         new FormAttachment(WIZARD_VALUE_NUMERATOR, -COMMON_BORDER_OFFSET);

   /**
    * Composite to display global template selection.
    */
   PSRadioAndCheckBoxes m_globalTemplateRadio;

   /**
    * Templates with {@link OutputFormat#Global} type. Sorted by label.
    */
   final List<IPSReference> m_globalTemplateRefs = new ArrayList<IPSReference>();

   /**
    * The template output UI.
    */
   private PSTemplateOutputComposite m_outputControl;
}

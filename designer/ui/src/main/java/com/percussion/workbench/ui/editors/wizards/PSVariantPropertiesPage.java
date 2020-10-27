/******************************************************************************
 *
 * [ PSVariantPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSTemplateOutputComposite;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import static com.percussion.workbench.ui.IPSUiConstants.COMBO_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.ONE_FORTH;
import static com.percussion.workbench.ui.IPSUiConstants.TEXT_VSPACE_OFFSET;

/**
 * Main variant wizard page. 
 *
 * @author Andriy Palamarchuk
 */
public class PSVariantPropertiesPage extends PSNameLabelDescPageBase
{
// WB-1235

   public PSVariantPropertiesPage()
   {
      
      super(getMessage("PSVariantWizard.title"), //$NON-NLS-1$
         getMessage("PSVariantPropertiesPage.title"), //$NON-NLS-1$
         null);
      
   }

   @Override
   protected void fillUpContainer(final Composite container)
   {
      final Label contentTypeLabel = createAlignedLabel(container, CONTENT_TYPE_LABEL,
            new FormAttachment(m_nameLabelDesc,
               TEXT_VSPACE_OFFSET + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET));
      m_contentTypeCombo = createContentTypeCombo(container, contentTypeLabel);
      m_contentTypeHelper.setCombo(m_contentTypeCombo);
      
      m_outputControl = createOutputControl(container, m_contentTypeCombo);
      
      registerControl("common.label.contentType", m_contentTypeCombo, null);
      registerControl("PSTemplateOutputComposite.label.output",
            m_outputControl.getFormatRadio(), null);
   }

   /**
    * Creates UI to select output format.
    */
   private PSTemplateOutputComposite createOutputControl(
         final Composite container, final Control topControl)
   {
      final PSTemplateOutputComposite outputControl =
            new PSTemplateOutputComposite(container, 0, false, true, false, false, true);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.right = new FormAttachment(100);
      formData.top = new FormAttachment(topControl, COMBO_VSPACE_OFFSET, SWT.BOTTOM);
      outputControl.setLayoutData(formData);
      return outputControl;
   }

   /**
    * Creates content type combo.
    */
   private Combo createContentTypeCombo(final Composite container, final Label contentTypeLabel)
   {
      final Combo combo = new Combo(container, SWT.READ_ONLY);
      combo.setItems(
            m_contentTypeHelper.getContentTypeNames().toArray(new String[0]));
      combo.select(0);
      
      final FormData formData = new FormData();
      formData.left = new FormAttachment(contentTypeLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData.right = new FormAttachment(100 - ONE_FORTH);
      formData.top = new FormAttachment(contentTypeLabel, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      combo.setLayoutData(formData);
      
      return combo;
   }

   // see base
   public void updateDesignerObject(Object designObject, Object control)
   {
      final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) designObject;
      if (control.equals(m_nameLabelDesc.getNameText()))
      {
         template.setName(((Text) m_nameLabelDesc.getNameText()).getText());
      }
      else if (control.equals(m_nameLabelDesc.getLabelText()))
      {
         template.setLabel(m_nameLabelDesc.getLabelText().getText());
      }
      else if (control.equals(m_nameLabelDesc.getDescriptionText()))
      {
         template.setDescription(m_nameLabelDesc.getDescriptionText().getText());
      }
      else if (control.equals(m_contentTypeCombo))
      {
         m_contentTypeHelper.updateTemplate(template);
      }
      else if (control.equals(m_outputControl.getFormatRadio()))
      {
         m_outputControl.updateTemplate(template);
         configurePublishWhen(template);
      }
      else
      {
         throw new IllegalArgumentException("Unrecognized control: " + control);
      }
   }

   private void configurePublishWhen(final PSUiAssemblyTemplate template)
   {
      if (template.getOutputFormat().equals(OutputFormat.Snippet))
      {
         template.setPublishWhen(PublishWhen.Never);
      }
      else
      {
         template.setPublishWhen(PublishWhen.Default);
      }
   }

   @Override
   protected String getNamePrefix()
   {
      return getMessage("common.label.variant");   //$NON-NLS-1$
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
            PSNameLabelDesc.DESC_TEXT_KEY,
               "description",
            PSNameLabelDesc.LABEL_TEXT_KEY,
               "label",
            PSNameLabelDesc.NAME_TEXT_KEY,
               "variant_name",
            "common.label.contentType",
               "content_type",
            "PSTemplateOutputComposite.label.output",
               "output"
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
    * Text of content type label.
    */
   private static final String CONTENT_TYPE_LABEL =
         getMessage("common.label.contentType");

   /**
    * Drop list of content types.
    */
   Combo m_contentTypeCombo;
   
   /**
    * Format selection composite.
    */
   private PSTemplateOutputComposite m_outputControl;

   /**
    * Manages content type UI.
    */
   private final PSContentTypeHelper m_contentTypeHelper = new PSContentTypeHelper();
}

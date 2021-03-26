/******************************************************************************
 *
 * [ PSTemplateVariantPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.controls.PSTemplateOutputWithPublishComposite;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import static com.percussion.workbench.ui.IPSUiConstants.LABEL_VSPACE_OFFSET;

/**
 * Propertied page for old 5.x style templates (variants).
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateVariantPage extends PSTemplatePropertiesPageBase
// WB-1230
{
   /**
    * Creates new page.
    */
   public PSTemplateVariantPage(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style, editor);

      // left pane
      m_outputComposite = createOutputControl(m_leftPane);
      createPublishWhenControl(m_leftPane);
      m_outputComposite.setPublishWhenHelper(m_publishWhenHelper);

      // right pane
      initActiveAssemblyRadio(m_rightPane);
      final Label stylesheetLabel =
            createUpperAttachedLabel(m_rightPane, m_activeAssemblyHelper.getRadio(),
                  STYLESHEET_LABEL);
      m_stylesheetText = createStylesheetText(m_rightPane, stylesheetLabel);
      m_editor.registerControl("PSTemplateVariantPage.label.stylesheet",
         m_stylesheetText, null);

      final Label urlLabel =
         createUpperAttachedLabel(m_rightPane, m_stylesheetText, URL_LABEL);
      m_urlText = createUrlText(m_rightPane, urlLabel);
      m_editor.registerControl("PSTemplateVariantPage.label.url", m_urlText, null);
      
      createLocationPane();
   }

   /**
    * Initializes {@link #m_outputComposite}.
    */
   private PSTemplateOutputWithPublishComposite createOutputControl(
         Composite container)
   {
      final PSTemplateOutputWithPublishComposite control =
         new PSTemplateOutputWithPublishComposite(
            container, SWT.NONE, true, false, false, false, true);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      control.setLayoutData(formData);
      if (control.getAssemblerCombo() != null)
      {
         m_editor.registerControl("PSTemplateOutputComposite.label.assembler",
               control.getAssemblerCombo(), null);
      }
      m_editor.registerControl("PSTemplateOutputComposite.label.output",
            control.getFormatRadio(), null);
      return control;
   }

   /**
    * Creates location control.
    */
   private void createLocationPane()
   {
      final Composite locationPane = m_locationHelper.initUI(m_rightPane);
      final FormData formData = attachToUpperFormData(m_urlText, LABEL_VSPACE_OFFSET);
      formData.right = new FormAttachment(100);
      locationPane.setLayoutData(formData);
      m_editor.registerControl("PSLocationHelper.label.prefix",
            m_locationHelper.getPrefixText(), null);
      m_editor.registerControl("PSLocationHelper.label.suffix",
            m_locationHelper.getSuffixText(), null);
   }

   /**
    * Creates and lays out text control to edit URL.
    */
   private Text createUrlText(Composite container, Control previousControl)
   {
      final Text text = new Text(container, SWT.BORDER);
      final FormData formData = attachToUpperFormData(previousControl, 0);
      formData.right = new FormAttachment(100, 0);
      text.setLayoutData(formData);
      return text;
   }

   /**
    * Creates and lays out text control to edit stylesheet.
    */
   private Text createStylesheetText(Composite container, Control previousControl)
   {
      final Text text = new Text(container, SWT.BORDER);
      final FormData formData = attachToUpperFormData(previousControl, 0);
      formData.right = new FormAttachment(100, 0);
      text.setLayoutData(formData);
      return text;
   }
   
   /**
    * Creates label attached to upper control. 
    */
   private Label createUpperAttachedLabel(Composite container, Control previousControl, final String labelText)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(labelText + ':');
      final FormData formData =
         attachToUpperFormData(previousControl, LABEL_VSPACE_OFFSET);
      label.setLayoutData(formData);
      return label;
   }

   /**
    * Initializes {@link #m_activeAssemblyHelper}.
    */
   private void initActiveAssemblyRadio(final Composite container)
   {
      m_activeAssemblyHelper.initRadio(container);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      m_activeAssemblyHelper.getRadio().setLayoutData(formData);
      m_editor.registerControl("PSTemplateGeneralPage.label.activeAssemblyFormat",
            m_activeAssemblyHelper.getRadio(), null);
   }

   /**
    * Creates publish when radio.
    */
   private void createPublishWhenControl(final Composite container)
   {
      m_publishWhenHelper.initRadio(container);
      m_publishWhenHelper.getRadio().setLayoutData(
            attachToUpperFormData(m_outputComposite, LABEL_VSPACE_OFFSET));
      m_editor.registerControl("PSPublishWhenHelper.label.publish",
            m_publishWhenHelper.getRadio(), null);
   }

   @Override
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      super.loadControlValues(template);
      m_publishWhenHelper.loadControlValues(template);
      m_outputComposite.loadControlValues(template);
      m_activeAssemblyHelper.loadControlValues(template);
      m_locationHelper.loadControlValues(template);
      
      m_stylesheetText.setText(
            StringUtils.defaultString(template.getStyleSheetPath()));
      m_urlText.setText(StringUtils.defaultString(template.getAssemblyUrl()));
   }

   @Override
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      super.updateTemplate(template);
      m_outputComposite.updateTemplate(template);
      m_publishWhenHelper.updateTemplate(template);
      m_activeAssemblyHelper.updateTemplate(template);
      m_locationHelper.updateTemplate(template);
      
      template.setStyleSheetPath(
            StringUtils.isBlank(m_stylesheetText.getText())
               ? null
               : m_stylesheetText.getText());
      template.setAssemblyUrl(m_urlText.getText());
   }

   @Override
   protected String getNamePrefix()
   {
      return getMessage("common.label.variant"); //$NON-NLS-1$
   }
   
   /**
    * Text for stylesheet label.
    */
   private static final String STYLESHEET_LABEL =
         getMessage("PSTemplateVariantPage.label.stylesheet"); //$NON-NLS-1$

   /**
    * Text for URL label.
    */
   private static final String URL_LABEL =
         getMessage("PSTemplateVariantPage.label.url"); //$NON-NLS-1$

   /**
    * Manages the publishing UI.
    */
   final PSPublishWhenHelper m_publishWhenHelper = new PSPublishWhenHelper();

   /**
    * Manages the active assembly format UI.
    */
   final PSActiveAssemblyHelper m_activeAssemblyHelper = new PSActiveAssemblyHelper();
   
   /**
    * Manages the location prefix/suffix UI.
    */
   final PSLocationHelper m_locationHelper = new PSLocationHelper();
   
   /**
    * Allows user to specify assembler, output format.
    */
   final PSTemplateOutputWithPublishComposite m_outputComposite;
   
   /**
    * Field to enter stylesheet.
    */
   final Text m_stylesheetText;
   
   /**
    * Field to enter URL.
    */
   final Text m_urlText;
}

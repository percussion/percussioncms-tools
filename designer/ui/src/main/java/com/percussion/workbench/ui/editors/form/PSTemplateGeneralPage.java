/******************************************************************************
 *
 * [ PSTemplateGeneralPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSTemplateOutputComposite.FormatChoice;
import com.percussion.workbench.ui.controls.PSTemplateOutputWithPublishComposite;
import com.percussion.workbench.ui.editors.common.PSEditorUtil;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import com.percussion.workbench.ui.editors.dialog.PSTdGeneratorDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import static com.percussion.extension.IPSExtension.BINARY_ASSEMBLER;
import static com.percussion.workbench.ui.IPSUiConstants.COMBO_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.ONE_THIRD;

/**
 * The General tab of the Template editor for shared or local templates.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateGeneralPage extends PSTemplatePropertiesPageBase
// WB-1202
{
   /**
    * Creates new General page for the template editor.
    */
   PSTemplateGeneralPage(final Composite parent, final int style, PSEditorBase editor)
   {
      super(parent, style, editor);
      m_outputComposite = createOutputControl(m_leftPane);
      createPublishWhenControl(m_leftPane);
      m_outputComposite.setPublishWhenHelper(m_publishWhenHelper);
      
      initActiveAssemblyRadio(m_rightPane);
      m_mimeTypeLabel = createMimeTypeLabel(m_rightPane, m_activeAssemblyHelper.getRadio());
      m_mimeTypeHelper.setCombo(createMimeTypeCombo(m_rightPane, m_mimeTypeLabel));
      m_outputComposite.getFormatRadio().addSelectionListener(new SelectionAdapter()
            {
         @Override
         @SuppressWarnings("unused")                              //$NON-NLS-1$
         public void widgetDefaultSelected(SelectionEvent e)
         {
            outputFormatSelectionChanged();
         }

         @Override
         @SuppressWarnings("unused")                              //$NON-NLS-1$
         public void widgetSelected(SelectionEvent e)
         {
            outputFormatSelectionChanged();
         }
      });

      m_outputComposite.getAssemblerCombo().addSelectionListener(
         new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(
               @SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
            {
               handleBinaryAssemblerSelection();
            }
            
         });
      m_characterSetLabel = createCharacterSetLabel(m_rightPane, m_mimeTypeHelper.getCombo());
      m_charSetHelper.setCombo(createCharacterSetCombo(m_rightPane, m_characterSetLabel));
      Composite locationPane = createLocationPane();
      m_regenSourceButton = createRegenSourceButton(m_rightPane, locationPane);
      pack();
   }
   
   /**
    * Handles clearing and disabling or enableing of the mime type and
    * charset fields if a binary assembler is selected.
    */
   private void handleBinaryAssemblerSelection()
   {
      String selected = m_outputComposite.getSelectedAssembler();
      PSUiAssemblyTemplate template = (PSUiAssemblyTemplate)m_editor.m_data;

      if((selected == null) || (selected.equals(BINARY_ASSEMBLER)) )
      {
         m_charSetHelper.clearSelectionAndDisable();
         m_mimeTypeHelper.clearSelectionAndDisable();
         m_mimeTypeLabel.setEnabled(false);
         m_characterSetLabel.setEnabled(false);
         if (template != null) 
         {
            template.setMimeType(null);
            template.setCharset(null);
         }
      }
      else
      {
         m_charSetHelper.getCombo().setEnabled(true);
         m_mimeTypeHelper.getCombo().setEnabled(true);
         m_mimeTypeLabel.setEnabled(true);
         m_characterSetLabel.setEnabled(true);
         // Set defaults and reload if necessary
         if (template != null)
         {
            if(template.getMimeType() == null)
            {
               template.setMimeType(DEFAULT_MIME_TYPE);
               m_mimeTypeHelper.loadControlValues(template);
            }
            if(template.getCharset() == null)
            {
               template.setCharset(DEFAULT_CHARACTER_SET);
               m_charSetHelper.loadControlValues(template);
            }            
         }
      }
   }

   /**
    * Creates publish when radio.
    */
   private void createPublishWhenControl(final Composite container)
   {
      m_publishWhenHelper.initRadio(container);
      m_publishWhenHelper.getRadio().setLayoutData(
            attachToUpperFormData(m_outputComposite, LABEL_VSPACE_OFFSET));
      m_editor.registerControl("PSPublishWhenHelper.label.publish", //$NON-NLS-1$
            m_publishWhenHelper.getRadio(), null);
   }

   /**
    * Creates location control.
    */
   private Composite createLocationPane()
   {
      final Composite locationPane = m_locationHelper.initUI(m_rightPane);
      final FormData formData = attachToUpperFormData(m_charSetHelper.getCombo(), LABEL_VSPACE_OFFSET);
      formData.right = new FormAttachment(100);
      locationPane.setLayoutData(formData);
      m_editor.registerControl("PSLocationHelper.label.prefix", //$NON-NLS-1$
            m_locationHelper.getPrefixText(), null);
      m_editor.registerControl("PSLocationHelper.label.suffix", //$NON-NLS-1$
            m_locationHelper.getSuffixText(), null);
      return locationPane;
   }
   
   /**
    * Creates a combo to select character set. 
    */
   private Combo createCharacterSetCombo(Composite container, Control previousControl)
   {
      final Combo combo = new Combo(container, SWT.READ_ONLY);
      final FormData formData = attachToUpperFormData(previousControl, 0);
      formData.right = new FormAttachment(100 - ONE_THIRD);
      combo.setLayoutData(formData);
      m_editor.registerControl("PSCharSetHelper.label.characterSet", combo, null); //$NON-NLS-1$
      return combo;
   }

   /**
    * Creates a label for character set combo.
    */
   private Label createCharacterSetLabel(Composite container, Control previousControl)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(PSCharSetHelper.LABEL + ':');
      label.setLayoutData(
            attachToUpperFormData(previousControl, COMBO_VSPACE_OFFSET));
      return label;
   }

   /**
    * Creates drop list to select character set.
    */
   private Combo createMimeTypeCombo(Composite container, Control previousControl)
   {
      final Combo combo = new Combo(container, SWT.READ_ONLY);
      final FormData formData = attachToUpperFormData(previousControl, 0);
      formData.right = new FormAttachment(100 - ONE_THIRD);
      combo.setLayoutData(formData);
      m_editor.registerControl("PSMimeTypeHelper.label.mimeType", combo, null); //$NON-NLS-1$
      return combo;
   }

   /**
   /**
    * Creates label for mime type drop list.
    */
   private Label createMimeTypeLabel(final Composite container,
         final Control previousControl)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(PSMimeTypeHelper.LABEL + ':');
      label.setLayoutData(
            attachToUpperFormData(previousControl, LABEL_VSPACE_OFFSET));
      return label;
   }
   
   private Button createRegenSourceButton(final Composite container,
         final Control previousControl)
   {
      final Button button = new Button(container, SWT.PUSH);
      button.setText(PSMessages.getString("PSTemplateGeneralPage.regenerateSourceButton.label")); //$NON-NLS-1$
      button.setVisible(false);
      setRegenSourceButtonState();
      FormData formData = new FormData();
      formData.top = new FormAttachment(previousControl, LABEL_VSPACE_OFFSET * 4, SWT.BOTTOM);
      formData.left = new FormAttachment(previousControl, 0, SWT.LEFT);
      button.setLayoutData(formData);
      button.addSelectionListener(new SelectionAdapter()
      {

         /* (non-Javadoc)
          * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
          * org.eclipse.swt.events.SelectionEvent)
          */
         @Override
         public void widgetSelected(@SuppressWarnings("unused")
         SelectionEvent e)
         {
            PSTemplateEditor editor = (PSTemplateEditor) m_editor;
            PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) editor
                     .getDesignerObject();
            PSTdGeneratorDialog dialog = new PSTdGeneratorDialog(container
                     .getShell(), template);
            int ret = dialog.open();
            if (ret == PSDialog.OK)
            {
               String msg = 
                  PSMessages.getString("PSTemplateGeneralPage.modifySchema.warning");  //$NON-NLS-1$
              
               if (MessageDialog.openConfirm(container.getShell(),
                  PSMessages.getString("PSTemplateGeneralPage.updateSchemaDialog.title"), //$NON-NLS-1$
                  msg))
               {
                  String newSource = dialog.getSource();
                  if (StringUtils.isNotBlank(newSource))
                  {                     
                     PSTemplateSourcePageHelper sourcePage = editor
                              .getSourcePage();
                     if (sourcePage != null)
                     {
                        sourcePage.setSource(sourcePage
                                 .generateTemplateFileName(), newSource, true);
                        sourcePage.updateTemplate(template);
                        String datasource = dialog.getDatasource();
                        if (StringUtils.isNotBlank(datasource))
                        {
                           PSEditorUtil.updateTemplateDbBindings(datasource,
                                    template);
                           editor.getBindingsPage().loadControlValues(template);
                        }
                     }
                  }
               }
            }
         }
         
      });
      
      return button;
   }
   
   /**
    * Toggles the regen source button enabled/disabled based on
    * the state of the template. The regen button is disabled if
    * the db.resource binding does not exist.
    */
   public void setRegenSourceButtonState()
   {
      if(m_regenSourceButton == null)
         return;
      m_regenSourceButton.setEnabled(true);
      PSTemplateEditor editor = (PSTemplateEditor) m_editor;
      PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) editor
               .getDesignerObject();
      if(template.getBindings().size() == 0 && 
         StringUtils.isBlank(template.getTemplate()))
         return;
      String datasource = 
         PSTdGeneratorDialog.getDatasourceFromBindings(template);
      if(StringUtils.isBlank(datasource))
         m_regenSourceButton.setEnabled(false);
   }
   /**
    * Is called whenever output format selection changes.
    * Contains actual logic to manager the publishing when control.
    */
   private void outputFormatSelectionChanged()
   {
      if (m_outputComposite.getSelectedFormatChoice().equals(FormatChoice.BINARY))
      {
         m_mimeTypeHelper.setMimeType(IPSMimeContentTypes.MIME_TYPE_OCTET_STREAM);     //$NON-NLS-1$
      }
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
      m_editor.registerControl("PSTemplateGeneralPage.label.activeAssemblyFormat", //$NON-NLS-1$
            m_activeAssemblyHelper.getRadio(), null);

   }

   /**
    * Initializes {@link #m_outputComposite}.
    */
   private PSTemplateOutputWithPublishComposite createOutputControl(final Composite container)
   {
      final PSTemplateOutputWithPublishComposite control =
            new PSTemplateOutputWithPublishComposite(
                  container, SWT.NONE, true, false, true, true, false);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      control.setLayoutData(formData);
      m_editor.registerControl("PSTemplateOutputComposite.label.assembler", //$NON-NLS-1$
            control.getAssemblerCombo(), null);
      m_editor.registerControl("PSTemplateOutputComposite.label.output", //$NON-NLS-1$
            control.getFormatRadio(), null);
      control.getAssemblerCombo().addSelectionListener(
               new SelectionAdapter()
               {

                  /* (non-Javadoc)
                   * @see org.eclipse.swt.events.SelectionAdapter#
                   * widgetSelected(org.eclipse.swt.events.SelectionEvent)
                   */
                  @Override
                  public void widgetSelected(@SuppressWarnings("unused")
                  SelectionEvent e)
                  {
                     setRegenButtonVisibility();
                  }
                  
               });
      return control;
   }

   /**
    * Load controls with the template values.
    */
   @Override
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      super.loadControlValues(template);
      setRegenButtonVisibility();
      m_publishWhenHelper.loadControlValues(template);
      m_outputComposite.loadControlValues(template);
      m_activeAssemblyHelper.loadControlValues(template);
      m_mimeTypeHelper.loadControlValues(template);
      m_charSetHelper.loadControlValues(template);
      m_locationHelper.loadControlValues(template);
      handleBinaryAssemblerSelection();
   }

   @Override
   public void updateTemplate(final PSUiAssemblyTemplate template)
   {
      super.updateTemplate(template);
      m_outputComposite.updateTemplate(template);
      m_publishWhenHelper.updateTemplate(template);
      m_activeAssemblyHelper.updateTemplate(template);
      m_mimeTypeHelper.updateTemplate(template);
      m_charSetHelper.updateTemplate(template);
      m_locationHelper.updateTemplate(template);
   }
   
   /**
    * Sets the regeneration button's visibility based on if
    * the assembler is of type database assembler.
    */
   private void setRegenButtonVisibility()
   {
      if(m_editor == null || m_regenSourceButton == null)
         return;
      PSUiAssemblyTemplate template = 
         (PSUiAssemblyTemplate)m_editor.getDesignerObject();
      if(template == null || template.getAssembler() == null)
         return;
      m_regenSourceButton.setVisible(
         template.getAssembler().endsWith("/databaseAssembler")); //$NON-NLS-1$
   }

   // see base
   @Override
   protected String getNamePrefix()
   {
      return getMessage("PSTemplateGeneralPage.label.name"); //$NON-NLS-1$
   }

   /**
    * Output format control. 
    */
   public PSTemplateOutputWithPublishComposite getOutputComposite()
   {
      return m_outputComposite;
   }
   
   /** 
    * Regenerate source button, initialized in 
    * {@link #createRegenSourceButton(Composite, Control)}.
    * Never <code>null</code> after that, may not be set as visible.
    */
   private Button m_regenSourceButton;
   
   /**
    * Allows user to specify assembler, output format.
    */
   final PSTemplateOutputWithPublishComposite m_outputComposite;
   
   /**
    * Manages the mime types dropdown.
    */
   final PSMimeTypeHelper m_mimeTypeHelper = new PSMimeTypeHelper();
   
   /**
    * Manages the char sets dropdown.
    */
   final PSCharSetHelper m_charSetHelper = new PSCharSetHelper();
   
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
    * Mime type label, initialized in ctor, never <code>null</code> after that.
    */
   final private Label m_mimeTypeLabel;
   
   /**
    * Char set label, initialized in ctor, never <code>null</code> after that.
    */
   final private Label m_characterSetLabel;
   
   /**
    * Default mime type value.
    */
   private static final String DEFAULT_MIME_TYPE = "text/html"; //$NON-NLS-1$

   /**
    * Default character set.
    */
   private static final String DEFAULT_CHARACTER_SET = "UTF-8"; //$NON-NLS-1$
}

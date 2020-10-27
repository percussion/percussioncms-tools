/******************************************************************************
 *
 * [ PSTemplatePropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSSelectCommunitiesComposite;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValueFileExistsValidator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.WIZARD_LABEL_NUMERATOR;
import static com.percussion.workbench.ui.IPSUiConstants.WIZARD_TOP_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.WIZARD_VALUE_NUMERATOR;
/**
 * General template properties page of the template wizard.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplatePropertiesPage extends PSWizardPageBase
//wireframe WB-1201
{
   public PSTemplatePropertiesPage(){
      super(getMessage("PSTemplateWizard.title"));   //$NON-NLS-1$
      setTitle(getMessage("PSTemplateWizard.screenTitle"));  //$NON-NLS-1$
   }

   // see base
   public void createControl(Composite parent){
      final Composite container = new Composite(parent, SWT.NULL);
      container.setLayout(new FormLayout());
      setControl(container);
      
      m_nameDesc = createNameLabelDescComposite(container);
      
      final Label sourceLabel = createSourceLabel(container);
      m_sourceButton = createSourceButton(container, sourceLabel);
      m_sourceText = createSourceText(container, sourceLabel);
      m_communitiesControl = createCommunitiesControl(container);

      registerControl("PSTemplatePropertiesPage.label.source", m_sourceText,
            new IPSControlValueValidator[] {new PSControlValueFileExistsValidator()});
      registerControl("PSSelectCommunitiesComposite.label.visible.communities",
               m_communitiesControl.getSlushControl(), null,
               PSControlInfo.TYPE_COMMUNITY);
   }
   
   /**
    * 
    */
   @Override
   public IWizardPage getNextPage()
   {
      PSTemplateWizard wizard = (PSTemplateWizard)getWizard();
      if (wizard.isDbPubType() && !isSourcePathDefined())
      {
         return super.getNextPage();
      }      
      return super.getNextPage().getNextPage();
   }

   /**
    * Creates and lays out the control to select communities.
    */
   private PSSelectCommunitiesComposite createCommunitiesControl(final Composite container)
   {
      final PSSelectCommunitiesComposite communitiesControl =
            new PSSelectCommunitiesComposite(container, SWT.NONE);
      {
         final FormData formData = new FormData();
         formData.left = FORM_LEFT_SIDE;
         formData.right = FORM_RIGHT_SIDE;
         formData.top = new FormAttachment(m_sourceButton, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData.bottom = new FormAttachment(100);
         communitiesControl.setLayoutData(formData);
      }
      return communitiesControl;
   }

   /**
    * Creates source path text control. 
    */
   private Text createSourceText(final Composite container, final Label sourceLabel)
   {
      final Text sourceText = new Text(container, SWT.BORDER);
      sourceText.setTextLimit(Text.LIMIT);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(sourceLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         formData.right = new FormAttachment(m_sourceButton, -LABEL_HSPACE_OFFSET, SWT.LEFT);
         formData.top = new FormAttachment(sourceLabel, 0, SWT.TOP);
         sourceText.setLayoutData(formData);
      }
      return sourceText;
   }

   /**
    * Creates button to load template source.
    */
   private Button createSourceButton(final Composite container, final Label sourceLabel)
   {
      final Button sourceButton = new Button(container, SWT.PUSH);
      sourceButton.setText(getMessage("PSTemplatePropertiesPage.button.source")); //$NON-NLS-1$
      {
         final FormData formData = new FormData();
         sourceButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
         formData.right = FORM_RIGHT_SIDE;
         formData.top = new FormAttachment(sourceLabel, 0, SWT.TOP);
         sourceButton.setLayoutData(formData);
      }
      sourceButton.addSelectionListener(new SelectionListener()
            {
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  locateSource();
               }

               public void widgetDefaultSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  locateSource();
               }
            });
      return sourceButton;
   }

   /**
    * Helps user to locate the source.
    */
   private void locateSource()
   {
      final FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
      dlg.setText(getMessage("PSTemplatePropertiesPage.title.openSourceDialog")); //$NON-NLS-1$
      final String src = dlg.open();
      if (StringUtils.isNotBlank(src))
      {
         m_sourceText.setText(src);
      }
   }

   /**
    * Creates label near source controls.
    */
   private Label createSourceLabel(final Composite container)
   {
      final Label label = new Label(container, SWT.WRAP);
      label.setText(getMessage("PSTemplatePropertiesPage.label.source") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
      label.setAlignment(SWT.RIGHT);
      {
         final FormData formData = new FormData();
         formData.left = FORM_LEFT_SIDE;
         formData.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, LABEL_HSPACE_OFFSET);
         formData.top = new FormAttachment(
               m_nameDesc, TEXT_VSPACE_OFFSET, SWT.BOTTOM);
         label.setLayoutData(formData);
      }
      return label;
   }

   /**
    * Creates composite handlign name, label and description fields.
    */
   private PSNameLabelDesc createNameLabelDescComposite(final Composite container)
   {
      final PSNameLabelDesc composite =
            new PSNameLabelDesc(container, SWT.NONE,
               getMessage("PSTemplatePropertiesPage.label.name"),  //$NON-NLS-1$
               WIZARD_LABEL_NUMERATOR, 
               PSNameLabelDesc.SHOW_ALL, this);
      final FormData formData = new FormData();
      formData.left = FORM_LEFT_SIDE;
      formData.right = FORM_RIGHT_SIDE;
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      composite.setLayoutData(formData);
      return composite;
   }

   // see base
   public void updateDesignerObject(Object designObject, Object control)
   {
      final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) designObject;
      if (control.equals(getNameText()))
      {
         template.setName(getNameText().getText());
      }
      else if (control.equals(getLabelText()))
      {
         if (StringUtils.isNotBlank(getLabelText().getText()))
         {
            template.setLabel(getLabelText().getText());
         }
      }
      else if (control.equals(getDescriptionText()))
      {
         template.setDescription(getDescriptionText().getText());
      }
      else if (control.equals(m_sourceText))
      {
         if (!isSourcePathDefined())
         {
            template.setTemplate(null);
            return;
         }
         final File file = new File(m_sourceText.getText());
         try
         {
            template.setTemplate(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
         }
         catch (IOException e)
         {
            PSDlgUtil.showError(e);
         }
      }      
      else
      {
         // 
      }
   }
   
   /**
    * Indicates that a source path has been defined that points
    * to an table schema xml file.
    * @return
    */
   public boolean isSourcePathDefined()
   {
      return StringUtils.isNotBlank(m_sourceText.getText());
   }

   /**
    * Convenience method to get description text control.
    */
   private Text getDescriptionText()
   {
      return m_nameDesc.getDescriptionText();
   }

   /**
    * Convenience method to get label text control.
    */
   private Text getLabelText()
   {
      return m_nameDesc.getLabelText();
   }
   
   /**
    * Convenience method to access name control.
    */
   Text getNameText()
   {
      return (Text) m_nameDesc.getNameText();
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
               "template_name",
            "PSTemplatePropertiesPage.label.source",
               "source",
            "PSSelectCommunitiesComposite.label.visible.communities",
               "visible_in_these_communities"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   // see base
   @Override
   public void setVisible(boolean visible)
   {
      if (visible)
      {
         m_sourceButton.setEnabled(enableSourceControls());
         m_sourceText.setEnabled(enableSourceControls());
         if (!enableSourceControls())
         {
            m_sourceText.setText("");
         }
      }
      super.setVisible(visible);
   }

   /**
    * Indicates whether source controls should be enabled.
    * @return <code>true</code> when source controls should be enabled.
    */
   private boolean enableSourceControls()
   {
      return ((PSTemplateWizard) getWizard()).hasSource();
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
    * Composite managing name and description data.
    */
   PSNameLabelDesc m_nameDesc;

   /**
    * Path to the content template. Will be used to populate the velocity editor.  
    */
   Text m_sourceText;
   
   /**
    * The button to open file dialog to specify the source file.
    */
   Button m_sourceButton;
   
   /**
    * Communities selection control.
    */
   PSSelectCommunitiesComposite m_communitiesControl;
}

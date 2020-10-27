/******************************************************************************
 *
 * [ PSTemplateTypePage.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import static com.percussion.extension.IPSExtension.DATABASE_ASSEMBLER;
import static com.percussion.extension.IPSExtension.VELOCITY_ASSEMBLER;

/**
 * Provides user with selection of template type (wireframe WB-1225).
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateTypePage extends PSWizardPageBase implements IPSUiConstants 
{
   public PSTemplateTypePage()
   {
      super(getMessage("PSTemplateWizard.title"));     //$NON-NLS-1$
      setTitle(getMessage("PSTemplateWizard.screenTitle")); //$NON-NLS-1$
   }

   public void createControl(Composite parent)
   {
      final Composite container = new Composite(parent, SWT.NULL);
      container.setLayout(new FormLayout());
      setControl(container);

      m_typeRadio = createRadioComposite(container);
      m_typeRadio.addEntry(getMessage("PSTemplateTypePage.localType"), //$NON-NLS-1$
            getMessage("common.label.contentType") + ':', //$NON-NLS-1$
            m_contentTypeHelper.getContentTypeNames(), null, true);
      m_contentTypeHelper.setCombo(getContentTypeCombo());


      m_typeRadio.addEntry(getMessage("PSTemplateTypePage.sharedType")); //$NON-NLS-1$
      m_typeRadio.addEntry(getMessage("PSTemplateTypePage.globalOutputFormat")); //$NON-NLS-1$
      m_typeRadio.addEntry(getMessage("PSTemplateTypePage.databaseOutputFormat")); //$NON-NLS-1$
      m_typeRadio.layoutControls();

      registerControl("PSTemplateTypePage.typeRadioGroupTitle",
         m_typeRadio, null); //$NON-NLS-1$
      registerControl("PSTemplateTypePage.localType",
         m_typeRadio, null); //$NON-NLS-1$
      
      makeInitialSelection();
   }

   private PSRadioAndCheckBoxes createRadioComposite(final Composite container)
   {
      final PSRadioAndCheckBoxes radio =
            new PSRadioAndCheckBoxes(container,
                  getMessage("PSTemplateTypePage.typeRadioGroupTitle"), //$NON-NLS-1$
                  SWT.SEPARATOR | SWT.VERTICAL | SWT.RADIO);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
         formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
         formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
         radio.setLayoutData(formData);
      }
      return radio;
   }

   private void makeInitialSelection()
   {
      m_typeRadio.setSelection(TypeButton.SHARED.ordinal());
      if (m_contentTypeHelper.getContentTypeNames().isEmpty())
      {
         m_typeRadio.setEnabledButtons(false, TypeButton.LOCAL.ordinal());
      }
      else
      {
         getContentTypeCombo().select(0);
      }
   }
   
   /**
    * Returns content type combo box.
    */
   Combo getContentTypeCombo()
   {
      return (Combo) m_typeRadio.getNestedControl(TypeButton.LOCAL.ordinal());
   }

   // see super
   public void updateDesignerObject(Object designObject, Object control)
   {
      final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) designObject;
      if (control.equals(m_typeRadio))
      {
         updateTemplateForTypeRadioButtons(template);
         template.setAssemblyUrl(ASSEMBLY_URL);
         template.setMimeType(MIME_TYPE);
         template.setCharset(CHARACTER_SET);
      }
      else
      {
         throw new IllegalArgumentException("Unrecognized control: " + control); //$NON-NLS-1$
      }
      
   }

   /**
    * Reflects state of type radio buttons in the provided template.
    */
   @SuppressWarnings("unused")
   private void updateTemplateForTypeRadioButtons(final PSUiAssemblyTemplate template)
   {
      switch (getTypeRadioSelection())
      {
         case LOCAL:
            template.setTemplateType(TemplateType.Local);
            template.setOutputFormat(NON_GLOBAL_OUTPUT_TYPE);
            break;
         case SHARED:
            template.setTemplateType(TemplateType.Shared);
            template.setOutputFormat(NON_GLOBAL_OUTPUT_TYPE);
            break;
         case GLOBAL_FORMAT:
            template.setTemplateType(TemplateType.Shared);
            template.setOutputFormat(OutputFormat.Global);
            template.setAssembler(VELOCITY_ASSEMBLER);
            break;
         case DATABASE_FORMAT:
            template.setTemplateType(TemplateType.Shared);
            template.setOutputFormat(OutputFormat.Database);
            template.setAssembler(DATABASE_ASSEMBLER);
            break;
         default:
            throw new IllegalArgumentException(
                  "Unrecognized radio button index: " + getTypeRadioSelection()); //$NON-NLS-1$
      }

      if (getTypeRadioSelection().equals(TypeButton.LOCAL))
      {
         m_contentTypeHelper.updateTemplate(template);
      }
      else
      {
         template.setNewContentTypes(null);
      }
   }

   public TypeButton getTypeRadioSelection()
   {
      return TypeButton.valueOf(m_typeRadio.getSelectedIndex());
   }
   
   @Override
   public IWizardPage getNextPage()
   {
      if (skipOutputFormatPage())
      {
         return super.getNextPage().getNextPage();
      }
      return super.getNextPage();
   }
   
   /**
    * Returns next page as {@link PSTemplateOutputPage}. 
    */
   private PSTemplateOutputPage getOutputFormatPage()
   {
      return (PSTemplateOutputPage) super.getNextPage();
   }

   /**
    * Indicates whether it is necessary to skip next Output Format page.
    */
   public boolean skipOutputFormatPage()
   {
      return
         getTypeRadioSelection().equals(TypeButton.DATABASE_FORMAT)
         || (getTypeRadioSelection().equals(TypeButton.GLOBAL_FORMAT)
               && !getOutputFormatPage().getOutputControl().hasUnknownAssemblers());
   }

   /**
    * Indicates whether it is necessary to skip next Content Types page.
    */
   public boolean skipContentTypesPage()
   {
      return getTypeRadioSelection().equals(TypeButton.LOCAL)
            || getTypeRadioSelection().equals(TypeButton.GLOBAL_FORMAT);
   }
   
   /**
    * Indicates that the type selected is for database publishing.
    * @return
    */
   public boolean isDatabaseTypeSelected()
   {
      return
      getTypeRadioSelection().equals(TypeButton.DATABASE_FORMAT);
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
           "PSTemplateTypePage.typeRadioGroupTitle",
               "type",
            "PSTemplateTypePage.localType",
               "content_type"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   

   /**
    * Any output format indicating that the template is not global.
    */
   static final OutputFormat NON_GLOBAL_OUTPUT_TYPE = OutputFormat.Snippet;

   /**
    * Local type button index in the group of radio buttons.
    */
   public enum TypeButton {
      LOCAL(TemplateSubTypes.LOCAL),
      SHARED(TemplateSubTypes.SHARED),
      GLOBAL_FORMAT(TemplateSubTypes.GLOBAL),
      DATABASE_FORMAT(TemplateSubTypes.SHARED);
   
      /**
       * New type button selection.
       * @param subType template object subtype associated with the selection.
       */
      private TypeButton(TemplateSubTypes subType)
      {
         mi_subType = subType;
      }
      
      private static TypeButton valueOf(int ordinal)
      {
         for (TypeButton t : values())
         {
            if (t.ordinal() == ordinal)
            {
               return t;
            }
         }
         throw new IndexOutOfBoundsException(
               "Index " + ordinal + " was not found in enum " + TypeButton.class); //$NON-NLS-1$ //$NON-NLS-2$
      }

      /**
       * Template object subtype associated with the selection
       */
      public TemplateSubTypes getSubType()
      {
         return mi_subType;
      }
      
      private final TemplateSubTypes mi_subType;
   }
   
   /**
    * Default assembly url assigned to the template.
    */
   private static final String ASSEMBLY_URL = "../assembler/render";

   /**
    * Default mime type value.
    */
   private static final String MIME_TYPE = "text/html";

   /**
    * Default character set.
    */
   private static final String CHARACTER_SET = "UTF-8";
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   /**
    * Main radio button composite.
    */
   PSRadioAndCheckBoxes m_typeRadio;
   
   /**
    * Manages content type UI.
    */
   final PSContentTypeHelper m_contentTypeHelper = new PSContentTypeHelper();
}

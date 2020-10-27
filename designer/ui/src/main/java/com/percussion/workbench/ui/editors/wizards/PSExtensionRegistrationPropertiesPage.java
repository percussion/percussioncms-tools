/******************************************************************************
 *
 * [ PSExtensionRegistrationPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSExtensionModel.Handlers;
import com.percussion.client.models.IPSExtensionModel.Interfaces;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSMuttBoxControl;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.common.PSJexlExtensionContextFilter;
import com.percussion.workbench.ui.editors.form.PSExtensionEditor;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValueRequiredValidator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

import static com.percussion.workbench.ui.IPSUiConstants.COMBO_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.ONE_FORTH;
import static com.percussion.workbench.ui.IPSUiConstants.TEXT_VSPACE_OFFSET;

/**
 * Main page of the extension registration wizard.
 *
 * @author Andriy Palamarchuk
 */
public class PSExtensionRegistrationPropertiesPage extends PSNameLabelDescPageBase
{
   /**
    * Creates new page. 
    */
   public PSExtensionRegistrationPropertiesPage()
   {
      super(PSMessages.getString("PSExtensionRegistrationWizard.title"), //$NON-NLS-1$
         PSMessages.getString("PSExtensionRegistrationPropertiesPage.title"), //$NON-NLS-1$
         null);              
      
   }

   /**
    * Creates rest of the page controls.
    * @see PSNameLabelDescPageBase#fillUpContainer(Composite)
    */
   @Override
   protected void fillUpContainer(Composite container)
   {
      try
      {
         m_contexts.addAll(PSExtensionEditor.loadContexts());
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      catch (PSMultiOperationException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      
            
      final Label contextLabel = createAlignedLabel(container, CONTEXT_LABEL,
            new FormAttachment(m_nameLabelDesc,
                  COMBO_VSPACE_OFFSET + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET));
      m_contextCombo = createContextCombo(container, contextLabel);
      
      final Label handlerLabel = createAlignedLabel(container, HANDLER_LABEL,
         new FormAttachment(m_contextCombo,
            TEXT_VSPACE_OFFSET + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET));

      m_handlerCombo = createHandlerCombo(container, handlerLabel);
      m_handlerCombo.addSelectionListener(new SelectionAdapter()
         {
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               handlerSelectionChanged();
            }
         });
      
      final Label typeLabel = createAlignedLabel(container, SUPPORTED_INTERFACES_LABEL,
            new FormAttachment(m_handlerCombo,
               COMBO_VSPACE_OFFSET + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET));
      m_supportedInterfaces = createSupportedInterfacesControl(container, typeLabel);
      
      
      final Label classNameLabel = createAlignedLabel(container, CLASSNAME_LABEL,
         new FormAttachment(m_supportedInterfaces,
            COMBO_VSPACE_OFFSET + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET));
      
      m_classNameText = createClassNameText(container, classNameLabel);
      
      handlerSelectionChanged();
      
      registerControl("PSExtensionRegistrationPropertiesPage.label.handler",
         m_handlerCombo, null);
      registerControl("PSExtensionRegistrationPropertiesPage.label.context",
         m_contextCombo,
               new IPSControlValueValidator[] {new PSControlValueRequiredValidator()});
      registerControl("PSExtensionRegistrationPropertiesPage.supportedInterfaces.label",
         m_supportedInterfaces, 
         new IPSControlValueValidator[] {new PSControlValueRequiredValidator()});
      registerControl("PSExtensionRegistrationPropertiesPage.className.label",
         m_classNameText, 
         new IPSControlValueValidator[] {new PSControlValueRequiredValidator()});
      m_nameLabelDesc.getDescriptionText().setTextLimit(Text.LIMIT);
   }

   /**
    * Creates combo box to select context.
    */
   private Combo createContextCombo(Composite container, Label contextLabel)
   {
      m_contextComboViewer = new ComboViewer(container, SWT.BORDER);
      m_contextComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_contextComboViewer.addFilter(m_contextFilter);
      m_contextComboViewer.setInput(m_contexts);
     
      final Combo combo = m_contextComboViewer.getCombo();
      combo.setItems(m_contexts.toArray(new String[0]));
      int dflt = m_contexts.indexOf("global/percussion/user/");
      if(dflt == -1)
         dflt = 0;
      combo.select(dflt);
      
      final FormData formData = new FormData();
      formData.left = new FormAttachment(contextLabel, 
         LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData.right = new FormAttachment(100 - ONE_FORTH);
      formData.top = new FormAttachment(contextLabel, 
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      combo.setLayoutData(formData);

      return combo;
   }
   
   private Text createClassNameText(Composite container, Label label)
   {
      Text text = new Text(container, SWT.BORDER);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(label, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData.right = new FormAttachment(100 - ONE_FORTH);
      formData.top = new FormAttachment(label, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      text.setLayoutData(formData);
      return text;
   }

   /**
    * Creates supported interfaces control
    */
   private PSMuttBoxControl createSupportedInterfacesControl(Composite container, Label typeLabel)
   {
      PSMuttBoxControl control = new PSMuttBoxControl(
         container, null, PSMuttBoxControl.TYPE_DROP);
      
      final FormData formData = new FormData();
      formData.left = new FormAttachment(typeLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(typeLabel, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      control.setLayoutData(formData);
      // load supported interfaces choices
      control.setChoices(Interfaces.getClassNames());
      control.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               PSMuttBoxControl thecontrol = (PSMuttBoxControl)e.getSource();
               PSExtensionEditor.handleInterfaceSelection(
                  thecontrol.getSelections(),
                  m_contextComboViewer, m_contextFilter);
            }
            
         });
      return control;
   }
   
   /**
    * Called when handler selection is changed.
    */
   void handlerSelectionChanged()
   {
      boolean enable = getSelectedHandler().equals(Handlers.JAVA);
      m_supportedInterfaces.setEnabled(enable);
      m_classNameText.setEnabled(enable);      
   }
  

   /**
    * Currently selected handler.
    */
   Handlers getSelectedHandler()
   {
      final int idx = m_handlerCombo.getSelectionIndex();
      return Handlers.values()[idx];
   }

   /**
    * Creates combo box to select handler.
    */
   private Combo createHandlerCombo(Composite container, Label handlerLabel)
   {
      final Combo combo = new Combo(container, SWT.READ_ONLY);
      combo.setItems(Handlers.getNames().toArray(new String[0]));
      combo.select(0);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(handlerLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData.right = new FormAttachment(100 - ONE_FORTH);
      formData.top = new FormAttachment(handlerLabel, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      combo.setLayoutData(formData);
      
      return combo;
   }   

   /**
    * Returns object name generated from the page data for the wizard.
    * @see PSWizardBase#findObjectName()
    */
   public String findObjectName()
   {
      final PSExtensionDef extension = new PSExtensionDef();
      extension.setExtensionRef(new PSExtensionRef("Java/context/name")); //$NON-NLS-1$
      updateDesignerObject(extension, m_nameLabelDesc.getNameText());
      updateDesignerObject(extension, m_contextCombo);
      updateDesignerObject(extension, m_handlerCombo);
      return extension.getRef().getFQN();
   }

   public void updateDesignerObject(Object designObject, Object control)
   {
      final PSExtensionDef extension = (PSExtensionDef) designObject;
      boolean isJava = getSelectedHandler().equals(Handlers.JAVA);
      if (control.equals(m_nameLabelDesc.getNameText()))
      {
         final PSExtensionRef ref = extension.getRef();
         extension.setExtensionRef(new PSExtensionRef(
               ref.getCategory(),
               ref.getHandlerName(),
               ref.getContext(),
               ((Text) m_nameLabelDesc.getNameText()).getText()));
      }
      else if (control.equals(m_nameLabelDesc.getDescriptionText()))
      {
         extension.setInitParameter(IPSExtensionDef.INIT_PARAM_DESCRIPTION,
               m_nameLabelDesc.getDescriptionText().getText());
      }
      else if (control.equals(m_contextCombo))
      {
         final PSExtensionRef ref = extension.getRef();
         extension.setExtensionRef(new PSExtensionRef(
               ref.getCategory(),
               ref.getHandlerName(),
               getSelectedContext(),
               ref.getExtensionName()));
      }
      else if (control.equals(m_handlerCombo))
      {
         final PSExtensionRef ref = extension.getRef();
         extension.setExtensionRef(new PSExtensionRef(
               ref.getCategory(),
               getSelectedHandler().getName(),
               ref.getContext(),
               ref.getExtensionName()));
      }
      else if (control.equals(m_supportedInterfaces))
      {
         if(isJava)
         {
            extension.setInterfaces(m_supportedInterfaces.getSelections());
         }
         else
         {
            List<String> requiredInterface = new ArrayList<String>();
            requiredInterface.add(Interfaces.UDF_PROCESSOR.getClassName());
            extension.setInterfaces(requiredInterface);
         }
      }
      else if (control == m_classNameText)
      {         
         if(isJava)
            extension.setInitParameter(IPSExtensionDef.INIT_PARAM_CLASSNAME, 
               m_classNameText.getText());
      }
      else
      {
         throw new IllegalArgumentException("Unrecognized control: " + control);
      }
   }
   
   

   /**
    * The context user selected.
    */
   private String getSelectedContext()
   {
      int index = m_contextCombo.getSelectionIndex();
      boolean isReadOnly = 
         (m_contextCombo.getStyle() & SWT.READ_ONLY) != 0;
      if(index != -1)        
         return m_contexts.get(index);
      if(!isReadOnly && StringUtils.isNotBlank(m_contextCombo.getText()))
         return m_contextCombo.getText();
      return ""; //$NON-NLS-1$
   }
   
   /**
    * Default value for the extension model used on this page.
    */
   IPSCmsModel initializeExtensionModel()
   {
      try
      {
         return getCoreFactory().getModel(PSObjectTypes.EXTENSION);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }

   /**
    * The singleton core factory instance.
    */
   private PSCoreFactory getCoreFactory()
   {
      return PSCoreFactory.getInstance();
   }
   
   // see base
   @Override
   protected int getNameDescOptions()
   {
      return PSNameLabelDesc.SHOW_NAME | PSNameLabelDesc.SHOW_DESC;
   }

   @Override
   protected String getNamePrefix()
   {
      return getMessage("PSExtensionRegistrationPropertiesPage.label.name"); //$NON-NLS-1$
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
               "extension_name",
            "PSExtensionRegistrationPropertiesPage.label.context",
               "context",
            "PSExtensionRegistrationPropertiesPage.label.handler",
               "handler",
            "PSExtensionRegistrationPropertiesPage.supportedInterfaces.label",
               "supported_interfaces",
            "PSExtensionRegistrationPropertiesPage.className.label",
               "class name"               
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
    * Text for label near handler dropdown.
    */
   private static final String CONTEXT_LABEL =
         getMessage("PSExtensionRegistrationPropertiesPage.label.context"); //$NON-NLS-1$

   /**
    * Text for label near handler dropdown.
    */
   private static final String HANDLER_LABEL =
         getMessage("PSExtensionRegistrationPropertiesPage.label.handler"); //$NON-NLS-1$
   
   /**
    * Text for label near handler dropdown.
    */
   private static final String SUPPORTED_INTERFACES_LABEL = PSMessages.getString(
      "PSExtensionRegistrationPropertiesPage.supportedInterfaces.label"); //$NON-NLS-1$
   
   /**
    * Text for classname label 
    */
   private static final String CLASSNAME_LABEL = PSMessages.getString(
      "PSExtensionRegistrationPropertiesPage.className.label"); //$NON-NLS-1$

   /**
    * Model used to catalog extensions.
    */
   IPSCmsModel m_extensionModel = initializeExtensionModel();
   
   /**
    * Dropdown list of extension contexts.
    */
   Combo m_contextCombo;
   
   /**
    * Context viewer
    */
   ComboViewer m_contextComboViewer;
   
   /** 
    * Filter used to restrict context combo to values allowed for
    * Jexl expression extensions.
    */
   PSJexlExtensionContextFilter m_contextFilter = 
      new PSJexlExtensionContextFilter();

   /**
    * Dropdown list of handlers.
    */
   Combo m_handlerCombo;
   
   /**
    * Class name text for Java type extension
    */
   Text m_classNameText;

   /**
    * Supported interfaces for the extension
    */
   PSMuttBoxControl m_supportedInterfaces;
         
   /**
    * List of available contexts.
    */
   private final List<String> m_contexts = new ArrayList<String>();
}

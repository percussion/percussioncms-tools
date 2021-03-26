/******************************************************************************
 *
 * [ PSSlotPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSElipseButtonComboComposite;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.dialog.PSExtensionParamsDialog;
import com.percussion.workbench.ui.editors.form.PSSlotEditor;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:44:00 PM
 */
public class PSSlotPropertiesPage extends PSWizardPageBase 
   implements IPSUiConstants {

   public PSSlotPropertiesPage()
   {         
      super(PSMessages.getString("PSSlotPropertiesPage.page.name"), //$NON-NLS-1$
         PSMessages.getString("PSSlotPropertiesPage.page.title"), null);  //$NON-NLS-2$
   }
   
   /* 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(
    * org.eclipse.swt.widgets.Composite)
    */
   @SuppressWarnings({"unchecked", "synthetic-access"})
   public void createControl(Composite parent)
   {
      
      final Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      
      //This composite takes care of registering its own controls
      m_commonComp = new PSNameLabelDesc(comp, SWT.NONE, 
         PSMessages.getString("PSSlotEditor.slotName.label"), //$NON-NLS-1$
         WIZARD_LABEL_NUMERATOR, 
         PSNameLabelDesc.SHOW_ALL, this);
      final FormData formData_14 = new FormData();
      formData_14.left = new FormAttachment(0, 0);
      formData_14.right = 
         new FormAttachment(WIZARD_VALUE_NUMERATOR, -COMMON_BORDER_OFFSET);
      formData_14.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      m_commonComp.setLayoutData(formData_14);      
      
      final Label contentFinderLabel = new Label(comp, SWT.WRAP);
      contentFinderLabel.setAlignment(SWT.RIGHT);
      final FormData formData_8 = new FormData();
      formData_8.left = new FormAttachment(0, 0);
      formData_8.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      formData_8.top = 
         new FormAttachment(
            m_commonComp, 15 + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      
      contentFinderLabel.setLayoutData(formData_8);
      contentFinderLabel.setText(PSMessages.getString(
         "PSSlotEditor.contentFinder.label")); //$NON-NLS-1$
      
      final PSElipseButtonComboComposite elipseButtonComboComposite = 
         new PSElipseButtonComboComposite(comp, SWT.READ_ONLY);
      final FormData formData = new FormData();
      formData.right = 
         new FormAttachment(WIZARD_VALUE_NUMERATOR, -COMMON_BORDER_OFFSET);
      formData.top = 
         new FormAttachment(
            contentFinderLabel, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData.left = new FormAttachment(contentFinderLabel, LABEL_HSPACE_OFFSET);
      elipseButtonComboComposite.setLayoutData(formData);      
      
      m_contentFinderComboViewer = elipseButtonComboComposite.getComboViewer();
      m_contentFinderCombo = m_contentFinderComboViewer.getCombo();
      m_contentFinderCombo.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               handleBrowseButtonEnable();               
            }
         
         });
      m_browseButton = elipseButtonComboComposite.getButton();
      m_contentFinderComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_contentFinderComboViewer.setLabelProvider(new PSAbstractLabelProvider()
         {

            public String getText(Object element)
            {
               if(element instanceof PSExtensionDef)
               {
               PSExtensionDef def = (PSExtensionDef)element;
               return StringUtils.defaultString(def.getRef().getExtensionName());
               }
               else if(element instanceof String)
               {
                  return (String)element;
               }
               return "";
            }
         
         });          
      loadContentFinderControl();
      registerControl(
         "PSSlotEditor.contentFinder.label", m_contentFinderCombo, null);      
        
      /**
       * A selectionlistener is added to the content finder combo
       * so the finder arguments are cleared when a new selection is
       * made.
       */
      m_contentFinderCombo.addSelectionListener(new SelectionAdapter()
         {

            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               m_finderArgs.clear();               
            }
         
         });
      
      /*
       * A selection listener is added to launch the extension params
       * dialog if a selection exists in the content finder combo
       */   
      m_browseButton.addSelectionListener(new SelectionAdapter()
         {

         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
         {
            if(m_contentFinderCombo.getSelectionIndex() < 1)
               return;               
            
            IStructuredSelection selection = 
               (IStructuredSelection)m_contentFinderComboViewer.getSelection();
            IPSExtensionDef def = (IPSExtensionDef)selection.getFirstElement();
            
            PSExtensionParamsDialog dialog = 
               new PSExtensionParamsDialog(comp.getShell(), def, new HashMap(),
                  PSExtensionParamsDialog.MODE_TEXT_LITERAL_ONLY);
                           
            if( dialog.open() == Dialog.OK)
            {
              m_finderArgs.clear();
              for(PSPair<String, IPSReplacementValue> pair : dialog.getParamValues())
              {
                 if(pair.getSecond() == null)
                    continue;
                 m_finderArgs.put(StringUtils.defaultString(pair.getFirst()),
                    StringUtils.defaultString(pair.getSecond().getValueText()));
              }              
               
            }
         } 
         
         });     
      handleBrowseButtonEnable();
      setControl(comp);
   }   
   
   /**
    * load values into the content finder combo control
    */
   @SuppressWarnings("unchecked")
   private void loadContentFinderControl()
   {
      List defs = new ArrayList();
      defs.add(PSMessages.getString("PSSlotEditor.contentFinder.none.choice")); //$NON-NLS-1$
      defs.addAll(PSSlotEditor.getSlotFinderExtensions());
      m_contentFinderComboViewer.setInput(defs);
      // find sys_RelationshipContentFinder default
      ILabelProvider lProvider = 
         (ILabelProvider)m_contentFinderComboViewer.getLabelProvider();
      int sel = 0;
      for(int i = 1; i < defs.size(); i++)
      {
         if(lProvider.getText(defs.get(i)).equalsIgnoreCase(
            "sys_RelationshipContentFinder"))
         {
            sel = i; 
            break;
         }
      }
      m_contentFinderCombo.select(sel); // set default
   }  

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings({"unchecked", "synthetic-access"})
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSTemplateSlot slot = (PSTemplateSlot)designObject;
      if(control == m_commonComp.getNameText())
      {
         slot.setName(((Text)m_commonComp.getNameText()).getText().trim());         
      }
      else if(control == m_commonComp.getLabelText())
      {
         slot.setLabel(m_commonComp.getLabelText().getText().trim());
      }
      else if (control == m_commonComp.getDescriptionText())
      {
         slot.setDescription(
            m_commonComp.getDescriptionText().getText().trim());
      }      
      else if (control == m_contentFinderCombo)
      {
         String finderName = null;
         int selection = m_contentFinderCombo.getSelectionIndex();
         if(selection > 0)
         {
            List data = 
               (List)m_contentFinderComboViewer.getInput();
            finderName = ((PSExtensionDef)data.get(selection)).getRef().toString();
         }
         else
         {
            m_finderArgs.clear();
         }
         slot.setFinderName(finderName);
         slot.setFinderArguments(m_finderArgs);
      }
      
   }
   
   /**
    * Helper method to handle the setting of the browse
    * button to enabled or diabled
    */
   private void handleBrowseButtonEnable()
   {
      boolean enable = false;
      if(m_contentFinderCombo.getSelectionIndex() > 0)
         enable = true;
      m_browseButton.setEnabled(enable);
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
               "slot_name",
            "PSSlotEditor.contentFinder.label",
               "content_finder"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;  
   
   
   private Combo m_contentFinderCombo;
   private ComboViewer m_contentFinderComboViewer;
   private PSNameLabelDesc m_commonComp;
   private Button m_browseButton;
   private Map<String, String> m_finderArgs = new HashMap<String, String>();

}

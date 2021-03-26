/******************************************************************************
 *
 * [ PSRelationshipTypePropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:58 PM
 */
public class PSRelationshipTypePropertiesPage extends PSWizardPageBase
   implements IPSUiConstants {

   public PSRelationshipTypePropertiesPage()
   {
      super(PSMessages.getString("PSRelationshipTypePropertiesPage.page.name"), //$NON-NLS-1$
            PSMessages.getString("PSRelationshipTypePropertiesPage.page.title"), null);  //$NON-NLS-2$
   
      initCategoryStore();
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
      
      // This composite takes care of registering its own controls
      m_commonComp = new PSNameLabelDesc(comp, SWT.NONE, 
         PSMessages.getString("PSRelationshipTypeEditor.label.relTypeName"), //$NON-NLS-1$
         WIZARD_LABEL_NUMERATOR, 
         PSNameLabelDesc.SHOW_ALL, this);
      final FormData formData_14 = new FormData();
      formData_14.left = new FormAttachment(0, 0);
      formData_14.right = 
         new FormAttachment(WIZARD_VALUE_NUMERATOR, -COMMON_BORDER_OFFSET);
      formData_14.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      m_commonComp.setLayoutData(formData_14);
            
      final Label categoryLabel = new Label(comp, SWT.WRAP);
      categoryLabel.setAlignment(SWT.RIGHT);
      final FormData formData_8 = new FormData();
      formData_8.left = new FormAttachment(0, 0);
      formData_8.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      formData_8.top = 
         new FormAttachment(
            m_commonComp, 15 + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      
      categoryLabel.setLayoutData(formData_8);
      categoryLabel.setText(PSMessages.getString(
         "PSRelationshipTypeEditor.label.category")); //$NON-NLS-1$
      
      m_categoryCombo = createCategoryCombo(comp, categoryLabel);
      
      loadCategoryControl();
      registerControl(
         "PSRelationshipTypeEditor.label.category", m_categoryCombo, null);      
           
      setControl(comp);
   }

   /**
    * Creates combo for relationship types categories.
    */
   private Combo createCategoryCombo(final Composite comp, final Label categoryLabel)
   {
      final Combo combo = new Combo(comp, SWT.READ_ONLY);
      final FormData formData = new FormData();
      formData.right = 
         new FormAttachment(WIZARD_VALUE_NUMERATOR, -COMMON_BORDER_OFFSET);
      formData.top = 
         new FormAttachment(
               categoryLabel, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData.left = new FormAttachment(categoryLabel, LABEL_HSPACE_OFFSET);
      combo.setLayoutData(formData);
      return combo;
   }   
   
   /**
    * load values into the category combo control
    */
   private void loadCategoryControl()
   {
      m_categoryCombo.setItems(m_categoryNames);
      m_categoryCombo.select(0); // set default to Active Assembly
   }  

   /**
    * initialize the category store with the appropriate values
    */
   private void initCategoryStore()
   {
      Object[] categories = PSRelationshipConfig.CATEGORY_ENUM;
      
      m_categoryEntries = new HashMap<String, String>();
      m_categoryNames = new String[categories.length];
      
      for (int i = 0; i < categories.length; i++)
      {
         PSEntry category = (PSEntry) categories[i];
         String name = category.getLabel().getText();
         m_categoryEntries.put(name, category.getValue());
         m_categoryNames[i] = name;
      }
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings({"unchecked", "synthetic-access"})
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSRelationshipConfig relType = (PSRelationshipConfig)designObject;
      if(control == m_commonComp.getNameText())
      {
         relType.setName(((Text)m_commonComp.getNameText()).getText().trim());         
      }
      else if(control == m_commonComp.getLabelText())
      {
         relType.setLabel(m_commonComp.getLabelText().getText().trim());
      }
      else if (control == m_commonComp.getDescriptionText())
      {
         relType.setDescription(
            m_commonComp.getDescriptionText().getText().trim());
      }      
      else if (control == m_categoryCombo)
      {
         int selection = m_categoryCombo.getSelectionIndex();
         String category = m_categoryEntries.get(m_categoryNames[selection]);
         relType.setCategory(category);
      }
      
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
               "relationship_type_name",
            "PSRelationshipTypeEditor.label.category",
               "category"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;  
   
   // Controls
   private Combo m_categoryCombo;
   private PSNameLabelDesc m_commonComp;
   private Map<String, String> m_categoryEntries;
   private String[] m_categoryNames;
}

/******************************************************************************
 *
 * [ PSFieldPropertiesComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.objectstore.IPSCETableColumnActions;
import com.percussion.cms.objectstore.PSFieldDefinition;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldValidationRules;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSSearchProperties;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.utils.PSContentTypeUtils;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSDefaultValueControl;
import com.percussion.workbench.ui.controls.PSMnemonicControl;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

public class PSFieldPropertiesComposite extends Composite
      implements
         IPSUiConstants,
         SelectionListener,
         IPSDesignerObjectUpdater
{

   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public PSFieldPropertiesComposite(PSCEFieldsCommonComposite parent,
         int style, int editorType, PSEditorBase editor)
   {
      super(parent, style);
      if (!PSContentEditorDefinition.isValidEditorType(editorType))
         throw new IllegalArgumentException("Invalid editor type"); //$NON-NLS-1$
      m_editorType = editorType;
      m_parentComp = parent;
      m_editor = editor;
      setLayout(new FormLayout());

      m_fieldComp = new Composite(this, SWT.NONE);
      m_fieldComp.setLayout(new FormLayout());
      final FormData formData_fieldComp = new FormData();
      formData_fieldComp.top = new FormAttachment(0, 0);
      formData_fieldComp.left = new FormAttachment(0, 0);
      formData_fieldComp.right = new FormAttachment(100, 0);
      m_fieldComp.setLayoutData(formData_fieldComp);

      m_fieldSetComp = new Composite(this, SWT.NONE);
      m_fieldSetComp.setLayout(new FormLayout());
      final FormData formData_fieldSetComp = new FormData();
      formData_fieldSetComp.top = new FormAttachment(m_fieldComp, 0,
            SWT.BOTTOM);
      formData_fieldSetComp.left = new FormAttachment(0, 0);
      formData_fieldSetComp.right = new FormAttachment(100, 0);
      m_fieldSetComp.setLayoutData(formData_fieldSetComp);

      final Label mainLabel = new Label(m_fieldComp, SWT.WRAP);
      final FormData formData_0 = new FormData();
      formData_0.top = new FormAttachment(0, 0);
      formData_0.left = new FormAttachment(0, 0);
      mainLabel.setLayoutData(formData_0);
      mainLabel.setText(PSMessages
            .getString("PSFieldPropertiesComposite.label.fieldproperties")); //$NON-NLS-1$

      final Label label = 
         new Label(m_fieldComp, SWT.SEPARATOR | SWT.HORIZONTAL);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(mainLabel, 0, SWT.CENTER);
      formData.left = new FormAttachment(mainLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      label.setLayoutData(formData);

      m_dataTypeAndFormat = new PSFieldDataTypeAndFormatComposite(m_fieldComp,
            SWT.NONE, true);
      final FormData formData_7 = new FormData();
      formData_7.left = new FormAttachment(mainLabel, LABEL_HSPACE_OFFSET,
            SWT.LEFT);
      formData_7.top = new FormAttachment(mainLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_7.right = new FormAttachment(100, 0);
      
      m_dataTypeAndFormat.setLayoutData(formData_7);
      m_dataTypeAndFormat.getDataTypeCombo().addSelectionListener(this);
      m_dataTypeAndFormat.getDataFormatCombo().addSelectionListener(this);
      m_dataTypeAndFormat.getDataFormatCombo().addFocusListener(
            new FocusAdapter()
            {
               @Override
               public void focusLost(@SuppressWarnings("unused") FocusEvent e)
               {
                  updateDataFormat();
                  m_editor.updateDesignerObject(m_editor.getDesignerObject(),
                        m_parentComp.getTableComp());
                  m_editor.setDirty();
               }
            });

      final Label defaultValueLabel = new Label(m_fieldComp, SWT.WRAP);
      final FormData formData_11 = new FormData();
      formData_11.top = 
         new FormAttachment(m_dataTypeAndFormat, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_11.left = new FormAttachment(m_dataTypeAndFormat, 0, SWT.LEFT);
      defaultValueLabel.setLayoutData(formData_11);
      defaultValueLabel.setText(PSMessages
            .getString("PSFieldPropertiesComposite.label.defaultvalue")); //$NON-NLS-1$

      m_defaultValueControl = new PSDefaultValueControl(m_fieldComp, SWT.NONE,
            PSDefaultValueControl.FUNCTION_SELECTOR
                  | PSDefaultValueControl.OTHERVALUE_SELECTOR);

      final FormData formData_12 = new FormData();
      formData_12.top = new FormAttachment(defaultValueLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_12.left = new FormAttachment(defaultValueLabel,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_12.right = new FormAttachment(100, 0);

      m_defaultValueControl.setLayoutData(formData_12);
      
      m_mimeTypeComposite = new PSMimeTypeComposite(m_fieldComp,SWT.NONE,true);
      final FormData formData_13 = new FormData();
      formData_13.left = new FormAttachment(mainLabel, LABEL_HSPACE_OFFSET,
            SWT.LEFT);
      formData_13.top = 
         new FormAttachment(defaultValueLabel, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_13.right = new FormAttachment(100, 0);
      m_mimeTypeComposite.setLayoutData(formData_13);
      
      m_enableSearchingForButton = new Button(m_fieldComp, SWT.CHECK);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_mimeTypeComposite,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_mimeTypeComposite, 0, SWT.LEFT);
      formData_3.right = new FormAttachment(50,0);
      m_enableSearchingForButton.setLayoutData(formData_3);
      m_enableSearchingForButton
            .setText(PSMessages
                  .getString("PSFieldPropertiesComposite.label.enablesearchingforfield")); //$NON-NLS-1$
      m_enableSearchingForButton.addSelectionListener(this);

      m_required = new Button(m_fieldComp, SWT.CHECK);
      final FormData formData_3a = new FormData();
      formData_3a.top = new FormAttachment(m_enableSearchingForButton,
            0, SWT.TOP);
      formData_3a.left = new FormAttachment(50, 0);
      formData_3a.right = new FormAttachment(100, 0);
      
      m_required.setLayoutData(formData_3a);
      m_required.setText("Required");
      m_required.addSelectionListener(this);
      
      final Label displayLabel = new Label(m_fieldComp, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_enableSearchingForButton,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_5.left = new FormAttachment(defaultValueLabel, 0, SWT.LEFT);
      displayLabel.setLayoutData(formData_5);
      displayLabel.setText(PSMessages
            .getString("PSFieldPropertiesComposite.label.display")); //$NON-NLS-1$

      final Label label_1 = new Label(m_fieldComp, SWT.SEPARATOR
            | SWT.HORIZONTAL);
      final FormData formData_6 = new FormData();
      formData_6.right = new FormAttachment(100, 0);
      formData_6.top = new FormAttachment(displayLabel, 0, SWT.CENTER);
      formData_6.left = new FormAttachment(displayLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      label_1.setLayoutData(formData_6);

      m_mnemonicControl = new PSMnemonicControl(m_fieldComp, SWT.NONE);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(displayLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_1.left = new FormAttachment(displayLabel, LABEL_HSPACE_OFFSET,
            SWT.LEFT);
      m_mnemonicControl.setLayoutData(formData_1);
      m_mnemonicControl.getMnemonicCombo().addSelectionListener(this);
      m_showInPreviewButton = new Button(m_fieldComp, SWT.CHECK);
      final FormData formData_15 = new FormData();
      formData_15.top = new FormAttachment(m_mnemonicControl, 0, SWT.TOP);
      formData_15.left = new FormAttachment(50, 0);
      m_showInPreviewButton.setLayoutData(formData_15);
      m_showInPreviewButton.setText(PSMessages
            .getString("PSFieldPropertiesComposite.label.showinpreview")); //$NON-NLS-1$
      m_showInPreviewButton.addSelectionListener(this);

      m_validationButton = new Button(m_fieldComp, SWT.NONE);
      final FormData formData_16 = new FormData();
      formData_16.top = new FormAttachment(m_mnemonicControl,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_16.right = new FormAttachment(100, -LABEL_HSPACE_OFFSET);
      m_validationButton.setLayoutData(formData_16);
      m_validationButton.setText(PSMessages
            .getString("PSFieldPropertiesComposite.label.button.validation")); //$NON-NLS-1$
      m_validationButton.addSelectionListener(this);

      m_allPropsButton = new Button(m_fieldComp, SWT.NONE);
      final FormData formData_17 = new FormData();
      formData_17.top = new FormAttachment(m_validationButton, 0, SWT.TOP);
      formData_17.right = new FormAttachment(m_validationButton,
            -LABEL_HSPACE_OFFSET, SWT.LEFT);
      m_allPropsButton.setLayoutData(formData_17);
      m_allPropsButton.setText(PSMessages
            .getString("PSFieldPropertiesComposite.label.button.allprops")); //$NON-NLS-1$
      m_allPropsButton.addSelectionListener(this);

      if (m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
      {
         final Label fieldSetLabel = new Label(m_fieldSetComp, SWT.NONE);
         final FormData formData_18 = new FormData();
         formData_18.top = new FormAttachment(m_allPropsButton,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_18.left = new FormAttachment(mainLabel, 0, SWT.LEFT);
         fieldSetLabel.setLayoutData(formData_18);
         fieldSetLabel.setText(PSMessages
               .getString("PSFieldPropertiesComposite.label.fieldset")); //$NON-NLS-1$

         final Label labelSep = new Label(m_fieldSetComp, SWT.SEPARATOR
               | SWT.HORIZONTAL);
         final FormData formData_19 = new FormData();
         formData_19.right = new FormAttachment(100, 0);
         formData_19.top = new FormAttachment(fieldSetLabel, 0, SWT.CENTER);
         formData_19.left = new FormAttachment(fieldSetLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         labelSep.setLayoutData(formData_19);

         final Label fieldSetNameLabel = new Label(m_fieldSetComp, SWT.NONE);
         final FormData formData_20 = new FormData();
         formData_20.top = new FormAttachment(fieldSetLabel,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_20.left = new FormAttachment(fieldSetLabel,
               LABEL_HSPACE_OFFSET, SWT.LEFT);
         fieldSetNameLabel.setLayoutData(formData_20);
         fieldSetNameLabel.setText(PSMessages
               .getString("PSFieldPropertiesComposite.label.fieldsetname")); //$NON-NLS-1$

         m_addFieldSet = new Button(m_fieldSetComp, SWT.NONE);
         final FormData formData_21 = new FormData();
         formData_21.top = new FormAttachment(fieldSetNameLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         formData_21.right = new FormAttachment(100, -LABEL_HSPACE_OFFSET);
         formData_21.width = BUTTON_WIDTH + 25;
         m_addFieldSet.setLayoutData(formData_21);
         m_addFieldSet.setText("Add Field Set"); //$NON-NLS-1$
         m_addFieldSet.addSelectionListener(this);

         m_fieldSetNameText = new Text(m_fieldSetComp, SWT.BORDER);
         final FormData formData_22 = new FormData();
         formData_22.top = new FormAttachment(fieldSetNameLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         formData_22.left = new FormAttachment(fieldSetNameLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         formData_22.right = new FormAttachment(m_addFieldSet,
               -LABEL_HSPACE_OFFSET, SWT.LEFT);
         m_fieldSetNameText.setLayoutData(formData_22);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   @Override
   public void dispose()
   {
      super.dispose();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   @Override
   protected void checkSubclass()
   {
   }

   /*
    * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
    */
   @Override
   public void setVisible(boolean visible)
   {
      m_fieldComp.setVisible(visible);
   }

   /**
    * Loads the control values that are present in this composite.
    * 
    * @param data The input data object PSFieldTableRowDataObject.
    * @param currentRow An int representing the current row.
    */
   @SuppressWarnings(
   {"unchecked", "unchecked"})//$NON-NLS-1$ //$NON-NLS-2$
   public void loadData(PSFieldTableRowDataObject data, int currentRow)
   {
      if (data == null)
         throw new IllegalArgumentException("data must not be null"); //$NON-NLS-1$

      m_currentRow = currentRow;
      m_fieldData = data;
      m_field = data.getField();
      m_mapping = data.getDisplayMapping();
      int type = m_field.getType();
      // For system and shared fields set the data type and storage size non
      // editable.
      if (type == PSField.TYPE_SYSTEM)
      {
         m_dataTypeAndFormat.setDataTypeAndFormatEnabled(false);
      }
      // If the field is shared but you are not editing it part of the shared
      // def then set the data type and storage size non editable.
      else if (type == PSField.TYPE_SHARED
            && m_editorType != PSContentEditorDefinition.SHAREDDEF_EDITOR)
      {
         m_dataTypeAndFormat.setDataTypeAndFormatEnabled(false);
      }
      else
      {
         m_dataTypeAndFormat.setDataTypeAndFormatEnabled(true);
      }

      m_dataTypeAndFormat.setDataTypeAndFormat(m_field.getDataType(), m_field
            .getDataFormat(), m_field.getLocator() != null);
      
      m_defaultValueControl.setValue(data.getField());
      m_mimeTypeComposite.setValues(data.getField(), m_parentComp
            .getSingleDimensionFieldNames());
      // Fill the m_mnemonicControl with the values.

      String mnem = m_mapping.getUISet().getAccessKey();
      m_mnemonicControl.setInput(
            StringUtils.defaultString(data.getLabelText()), StringUtils
                  .defaultString(mnem));

      m_enableSearchingForButton.setSelection(m_field.getSearchProperties()
            .isUserSearchable());
      // Set the required setting
      int occur = m_field.getOccurrenceDimension(null);
      PSFieldValidationRules valrules = m_field.getValidationRules();
      if (valrules == null)
         valrules = new PSFieldValidationRules();
      List<PSRule> rules = new ArrayList<PSRule>();
      CollectionUtils.addAll(rules, valrules.getRules());
      if (occur == PSField.OCCURRENCE_DIMENSION_REQUIRED)
      {
         PSFieldValidationsDialog.addRequiredRule(rules, m_field
               .getSubmitName());
         m_required.setSelection(true);
      }
      else
      {
         m_required.setSelection(false);
      }
      //Enable required check box only if it local field or system field
      //while editing system def or shared field while editing shared def.
      if (m_field.isLocalField()
            || (m_field.isSharedField() && 
                  m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
            || (m_field.isSystemField() && 
                  m_editorType == PSContentEditorDefinition.SYSTEMDEF_EDITOR))
      {
         m_required.setEnabled(true);
      }
      else
      {
         m_required.setEnabled(false);
      }
      
      m_showInPreviewButton.setSelection(m_field.isShowInPreview());
      // Set the tab order
      List<Control> tabList = new ArrayList();
      if (type == PSField.TYPE_LOCAL)
      {
         tabList.add(m_dataTypeAndFormat);
      }
      tabList.add(m_defaultValueControl);
      tabList.add(m_enableSearchingForButton);
      tabList.add(m_mnemonicControl);
      tabList.add(m_showInPreviewButton);
      tabList.add(m_allPropsButton);
      tabList.add(m_validationButton);
      m_fieldComp.setTabList(tabList.toArray(new Control[0]));

   }

   /**
    * Gets all fieldnames that are part of the editor. This compasite can not
    * decide the information and calls the parent composite and return it to the
    * caller.
    * 
    * See
    * <code>PSCEFieldsCommonComposite.validateFieldName(String, String)</code>
    * method to determine whether the field name is valid or not.
    * 
    * @return array of field names, Never <code>null</code>, but may be
    *         empty.
    */
   public String[] getAllFieldNames()
   {
      return m_parentComp.getAllFieldNames();
   }

   /**
    * Updates the mnemonic when the label changes
    * 
    */
   public void updateMnemonic(String label, PSUISet uiSet)
   {
      String mnem = ""; //$NON-NLS-1$
      if (uiSet != null)
         mnem = uiSet.getAccessKey();
      m_mnemonicControl.setInput(StringUtils.defaultString(label), StringUtils
            .defaultString(mnem));
      if (uiSet != null)
         uiSet.setAccessKey(m_mnemonicControl.getMnemonic());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public void widgetSelected(SelectionEvent e)
   {
      if(e.getSource() == m_dataTypeAndFormat.getDataTypeCombo())
      {
         updateDataType();
      }
      else if (e.getSource() == m_dataTypeAndFormat.getDataFormatCombo())
      {
         updateDataFormat();
      }
      else if (e.getSource() == m_enableSearchingForButton)
      {
         PSSearchProperties sprops = m_field.getSearchProperties();
         sprops.setUserSearchable(m_enableSearchingForButton.getSelection());
         m_field.setSearchProperties(sprops);
      }
      else if (e.getSource() == m_showInPreviewButton)
      {
         m_field.setShowInPreview(m_showInPreviewButton.getSelection());
      }
      else if (e.getSource() == m_mnemonicControl.getMnemonicCombo())
      {
         m_mapping.getUISet().setAccessKey(m_mnemonicControl.getMnemonic());
      }
      else if (e.getSource() == m_validationButton)
      {
         if (PSContentEditorDefinition.isLocalEditorType(m_editorType)
               && !PSContentEditorDefinition
                     .checkFieldValidationAllowed(m_fieldData.getField()))
         {
            return;
         }

         PSFieldDefinition fieldDefCopy = PSFieldDefinition
               .deepCopy(m_fieldData.getFieldDefinition());
         PSFieldTableRowDataObject fieldDataCopy = new PSFieldTableRowDataObject(
               fieldDefCopy);
         PSFieldValidationsDialog propDlg = new PSFieldValidationsDialog(
               getShell(), fieldDataCopy, m_editorType, m_parentComp
                     .getSingleDimensionFieldNames());
         if (propDlg.open() == Dialog.OK)
         {
            m_fieldData = propDlg.getFieldRowData();
            PSSortableTable table = m_parentComp.getTableComp();
            List<PSFieldTableRowDataObject> rows = m_parentComp.getTableComp()
                  .getValues();
            rows.set(m_currentRow, m_fieldData);
            table.setValues(rows);
            table.refreshTable();
            loadData(m_fieldData, m_currentRow);
         }
      }
      else if (e.getSource() == m_allPropsButton)
      {
         // Create a deep copy of the rowdata object before passing it to the
         // dialog
         // incase of cancel we need to preserve the original
         PSFieldDefinition fieldDefCopy = PSFieldDefinition
               .deepCopy(m_fieldData.getFieldDefinition());
         PSFieldTableRowDataObject fieldDataCopy = new PSFieldTableRowDataObject(
               fieldDefCopy);
         PSFieldPropertiesDialog propDlg = new PSFieldPropertiesDialog(
               getShell(), fieldDataCopy, m_editorType, getAllFieldNames(),
               m_parentComp.getSingleDimensionFieldNames());
         if (propDlg.open() == Dialog.OK)
         {
            
            PSFieldTableRowDataObject temp = propDlg.getFieldRowData();
            //If user changes the control reset the control dependencies
            if(!m_fieldData.getControlName().equals(temp.getControlName()))
               m_parentComp.setControlDependencies(temp.getFieldDefinition());
            // If the field has a data locator then check whether the data type
            // or format got changed if yes then add it to column modification
            // map.
            if(temp.getField().getLocator()!= null)
            {
               PSField oldF = m_fieldData.getField();
               PSField newF = temp.getField();
               boolean dtChanged = !oldF.getDataType().equals(
                     newF.getDataType());
               boolean dfChanged = false;
               // The data format can be null check for null before comparing
               if (oldF.getDataFormat() != null)
                  dfChanged = !oldF.getDataFormat()
                        .equals(newF.getDataFormat());
               else if (newF.getDataFormat() != null)
                  dfChanged = !newF.getDataFormat()
                        .equals(oldF.getDataFormat());
               // If both or null then no need to compare changed will be false.
               if (dtChanged || dfChanged)
               {
                  m_parentComp.setColumnModificationActions(newF,
                        IPSCETableColumnActions.COLUMN_ACTION_ALTER);
               }
            }
            m_fieldData = temp;
            PSSortableTable table = m_parentComp.getTableComp();
            List<PSFieldTableRowDataObject> rows = m_parentComp.getTableComp()
                  .getValues();
            rows.set(m_currentRow, m_fieldData);
            table.setValues(rows);
            table.refreshTable();
            loadData(m_fieldData, m_currentRow);
         }
      }
      else if (e.getSource() == m_addFieldSet)
      {
         m_parentComp.createSharedFieldSet(m_fieldSetNameText.getText());
         m_fieldSetNameText.setText(""); //$NON-NLS-1$
      }
      else if(e.getSource() == m_required)
      {
         List<PSExtensionRef> fvExits = PSFieldRuleDetailsComposites
               .getExtensions(IPSFieldValidator.class.getName());
         PSContentTypeUtils.setFieldRequiredRule(m_field, m_required
               .getSelection(), fvExits);
      }
      m_editor.updateDesignerObject(m_editor.getDesignerObject(), m_parentComp
            .getTableComp());
      m_editor.setDirty();
   }
   
   /**
    * Provides inner composites with the ability to dirty the main editor by
    * providing direct access to the editor.
    * 
    * @return reference to the parent editor of this composite.
    */
   public PSEditorBase getEditor()
   {
      return m_editor;
   }
   
   /**
    * Updates the data type and as well as the data format. If field has a data
    * locator then sets a column modification action.
    */
   private void updateDataType()
   {
      String dt = m_dataTypeAndFormat.getDataType();
      if (!dt.equals(m_field.getDataType()))
      {
         m_field.setDataType(dt);
         m_field.setDataFormat(m_dataTypeAndFormat.getDataFormat());
         if (m_field.getLocator() != null)
         {
            m_parentComp.setColumnModificationActions(m_field,
                  IPSCETableColumnActions.COLUMN_ACTION_ALTER);
         }
      }
   }

   /**
    * Updates the data format. If field has a data locator then sets a column
    * modification action.
    */
   private void updateDataFormat()
   {
      String newdf = StringUtils.defaultString(m_dataTypeAndFormat
            .getDataFormat());
      String olddf = StringUtils.defaultString(m_field.getDataFormat());
      if (olddf.equals(newdf))
         return;
      m_field.setDataFormat(m_dataTypeAndFormat.getDataFormat());
      if (m_field.getLocator() != null)
      {
         m_parentComp.setColumnModificationActions(m_field,
               IPSCETableColumnActions.COLUMN_ACTION_ALTER);
      }
      PSContentEditorDefinition.setMaxLengthParam(m_fieldData.getControlRef(),
            m_dataTypeAndFormat.getDataFormat());
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    *      updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(
         @SuppressWarnings("unused") Object designObject, //$NON-NLS-1$
         @SuppressWarnings("unused") Object control) //$NON-NLS-1$
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    *      loadControlValues(java.lang.Object)
    */
   public void loadControlValues(@SuppressWarnings("unused") Object designObject) //$NON-NLS-1$
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(
    *      org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(
         @SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
   {

   }

   /*
    * Controls
    */
   private PSDefaultValueControl m_defaultValueControl;

   private PSMimeTypeComposite m_mimeTypeComposite;
   
   private PSField m_field;

   private PSDisplayMapping m_mapping;

   private PSMnemonicControl m_mnemonicControl;

   private Button m_enableSearchingForButton;

   private Button m_showInPreviewButton;

   private Button m_validationButton;

   private Button m_allPropsButton;

   private Button m_addFieldSet;

   private Text m_fieldSetNameText;

   private PSFieldTableRowDataObject m_fieldData;

   private PSCEFieldsCommonComposite m_parentComp;

   private int m_currentRow;

   private int m_editorType;

   private PSEditorBase m_editor;

   private Composite m_fieldComp;

   private Composite m_fieldSetComp;

   private Button m_required;
   
   private PSFieldDataTypeAndFormatComposite m_dataTypeAndFormat;

}

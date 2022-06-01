/******************************************************************************
 *
 * [ PSFieldPropertiesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.PSModelException;
import com.percussion.cms.objectstore.PSFieldDefinition;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldTranslation;
import com.percussion.design.objectstore.PSFieldValidationRules;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSSearchProperties;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSVisibilityRules;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSDefaultValueControl;
import com.percussion.workbench.ui.controls.PSMnemonicControl;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.Iterator;
import java.util.List;

public class PSFieldPropertiesDialog extends PSDialog
      implements
         IPSUiConstants,
         SelectionListener
{

   public PSFieldPropertiesDialog(Shell parentShell,
         PSFieldTableRowDataObject fieldData, int editorType,
         String[] allFieldNames, String[] singleDiemnsionFieldNames) 
   {
      super(parentShell);
      if (fieldData == null)
      {
         throw new IllegalArgumentException("fieldData must not be null"); //$NON-NLS-1$
      }
      if (allFieldNames == null)
      {
         throw new IllegalArgumentException("allFieldNames must not be null"); //$NON-NLS-1$
      }
      if (!PSContentEditorDefinition.isValidEditorType(editorType))
         throw new IllegalArgumentException("Invalid editor type"); //$NON-NLS-1$
      m_fieldData = fieldData;
      m_editorType = editorType;
      m_allFieldNames = allFieldNames;
      m_singleDimensionFieldNames = singleDiemnsionFieldNames;
   }

   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString("PSFieldPropertiesDialog.dialog.title.fieldproperties")); //$NON-NLS-1$
   }

   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite mainComp = new Composite(parent, SWT.NONE);
      mainComp.setLayout(new FormLayout());

      // Main Controls
      final Label fieldNameLabel = new Label(mainComp, SWT.WRAP);
      fieldNameLabel.setAlignment(SWT.RIGHT);
      final FormData formData_0 = new FormData();
      formData_0.top = new FormAttachment(0, 20);
      formData_0.left = new FormAttachment(0, 10);
      formData_0.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      fieldNameLabel.setLayoutData(formData_0);
      fieldNameLabel.setText(PSMessages.getString("PSFieldPropertiesDialog.label.fieldname")); //$NON-NLS-1$

      m_fieldNameText = new Text(mainComp, SWT.BORDER);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, -10);
      formData.top = new FormAttachment(fieldNameLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData.left = new FormAttachment(fieldNameLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      m_fieldNameText.setLayoutData(formData);

      m_dataTypeAndFormat = new PSFieldDataTypeAndFormatComposite(mainComp,
            SWT.NONE, false);
      final FormData formData_7 = new FormData();
      formData_7.left = new FormAttachment(0,0);
      formData_7.right = new FormAttachment(100,0);
      formData_7.top = new FormAttachment(fieldNameLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      m_dataTypeAndFormat.setLayoutData(formData_7);
      
      final Label defaultValueLabel = new Label(mainComp, SWT.WRAP);
      defaultValueLabel.setAlignment(SWT.RIGHT);
      final FormData formData_11 = new FormData();
      formData_11.top = new FormAttachment(m_dataTypeAndFormat,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_11.left = new FormAttachment(fieldNameLabel, 0, SWT.LEFT);
      formData_11.right = new FormAttachment(fieldNameLabel, 0, SWT.RIGHT);
      defaultValueLabel.setLayoutData(formData_11);
      defaultValueLabel.setText(PSMessages.getString("PSFieldPropertiesDialog.label.defaultvalue")); //$NON-NLS-1$

      m_defaultValueControl = new PSDefaultValueControl(mainComp, SWT.NONE,
            PSDefaultValueControl.FUNCTION_SELECTOR
                  | PSDefaultValueControl.OTHERVALUE_SELECTOR);
      final FormData formData_12 = new FormData();
      formData_12.top = new FormAttachment(defaultValueLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_12.left = new FormAttachment(defaultValueLabel,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_12.right = new FormAttachment(100, -10);
      m_defaultValueControl.setLayoutData(formData_12);

      m_mimeTypeComposite = new PSMimeTypeComposite(mainComp,SWT.NONE,false);
      final FormData formData_13 = new FormData();
      formData_13.top = new FormAttachment(m_defaultValueControl,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_13.left = new FormAttachment(0,0);
      formData_13.right = new FormAttachment(100,-10);
      m_mimeTypeComposite.setLayoutData(formData_13);
      
      m_treatDataAsBinaryButton = new Button(mainComp, SWT.CHECK);
      final FormData formData_14 = new FormData();
      formData_14.top = new FormAttachment(m_mimeTypeComposite,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_14.left = new FormAttachment(defaultValueLabel,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_treatDataAsBinaryButton.setLayoutData(formData_14);
      m_treatDataAsBinaryButton.setText(PSMessages.getString("PSFieldPropertiesDialog.label.binary")); //$NON-NLS-1$
      m_treatDataAsBinaryButton.addSelectionListener(this);

      m_treatFieldAsMetadataButton = new Button(mainComp, SWT.CHECK);
      final FormData formData_14_1 = new FormData();
      formData_14_1.top = new FormAttachment(m_treatDataAsBinaryButton,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_14_1.left = new FormAttachment(defaultValueLabel,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_treatFieldAsMetadataButton.setLayoutData(formData_14_1);
      m_treatFieldAsMetadataButton.setText(PSMessages.getString("PSFieldPropertiesDialog.label.metadata")); //$NON-NLS-1$
      m_treatFieldAsMetadataButton.addSelectionListener(this);
 
      m_treatFieldAsExportableButton = new Button(mainComp, SWT.CHECK);
      final FormData formData_14_2 = new FormData();
      formData_14_2.top = new FormAttachment(m_treatFieldAsMetadataButton,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_14_2.left = new FormAttachment(defaultValueLabel,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_treatFieldAsExportableButton.setLayoutData(formData_14_2);
      m_treatFieldAsExportableButton.setText(PSMessages.getString("PSFieldPropertiesDialog.label.export")); //$NON-NLS-1$
 //     m_treatFieldAsExportableButton.setSelection(true);
      m_treatFieldAsExportableButton.addSelectionListener(this);
      
      // Display controls
      final Label displayLabel = new Label(mainComp, SWT.WRAP);
      final FormData formData_15 = new FormData();
      formData_15.top = new FormAttachment(m_treatFieldAsExportableButton,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_15.left = new FormAttachment(0, 10);
      displayLabel.setLayoutData(formData_15);
      displayLabel.setText(PSMessages.getString("PSFieldPropertiesDialog.label.display")); //$NON-NLS-1$
      // Display controls

      final Label displaySepLabel = new Label(mainComp, SWT.SEPARATOR
            | SWT.HORIZONTAL);
      final FormData formData_16 = new FormData();
      formData_16.top = new FormAttachment(displayLabel, 0, SWT.CENTER);
      formData_16.left = new FormAttachment(displayLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      formData_16.right = new FormAttachment(100, -10);
      displaySepLabel.setLayoutData(formData_16);

      final Label labelLabel = new Label(mainComp, SWT.WRAP);
      labelLabel.setAlignment(SWT.RIGHT);
      final FormData formData_17 = new FormData();
      formData_17.right = new FormAttachment(fieldNameLabel, 0, SWT.RIGHT);
      formData_17.top = new FormAttachment(displayLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_17.left = new FormAttachment(fieldNameLabel, 0, SWT.LEFT);
      labelLabel.setLayoutData(formData_17);
      labelLabel.setText(PSMessages.getString("PSFieldPropertiesDialog.label.label")); //$NON-NLS-1$

      m_mnemonicControl = new PSMnemonicControl(mainComp, SWT.NONE);
      final FormData formData_18 = new FormData();
      formData_18.top = new FormAttachment(labelLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_18.right = new FormAttachment(100, -10);
      m_mnemonicControl.setLayoutData(formData_18);

      m_labelText = new Text(mainComp, SWT.BORDER);
      final FormData formData_19 = new FormData();
      formData_19.top = new FormAttachment(labelLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_19.left = new FormAttachment(labelLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      formData_19.right = new FormAttachment(m_mnemonicControl, -2
            * LABEL_HSPACE_OFFSET, SWT.LEFT);
      m_labelText.setLayoutData(formData_19);

      final Label errorLabelLabel = new Label(mainComp, SWT.WRAP);
      errorLabelLabel.setAlignment(SWT.RIGHT);
      final FormData formData_20 = new FormData();
      formData_20.top = new FormAttachment(labelLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_20.left = new FormAttachment(labelLabel, 0, SWT.LEFT);
      formData_20.right = new FormAttachment(labelLabel, 0, SWT.RIGHT);
      errorLabelLabel.setLayoutData(formData_20);
      errorLabelLabel.setText(PSMessages.getString("PSFieldPropertiesDialog.label.errorlabel")); //$NON-NLS-1$

      m_errorLabelText = new Text(mainComp, SWT.BORDER);
      final FormData formData_21 = new FormData();
      formData_21.right = new FormAttachment(100, -10);
      formData_21.top = new FormAttachment(errorLabelLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_21.left = new FormAttachment(errorLabelLabel,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_errorLabelText.setLayoutData(formData_21);

      final Label controlLabel = new Label(mainComp, SWT.WRAP);
      controlLabel.setAlignment(SWT.RIGHT);
      final FormData formData_22 = new FormData();
      formData_22.top = new FormAttachment(errorLabelLabel,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_22.left = new FormAttachment(errorLabelLabel, 0, SWT.LEFT);
      formData_22.right = new FormAttachment(errorLabelLabel, 0, SWT.RIGHT);
      controlLabel.setLayoutData(formData_22);
      controlLabel.setText(PSMessages.getString("PSFieldPropertiesDialog.label.control")); //$NON-NLS-1$

      m_controlButton = new Button(mainComp, SWT.NONE);
      final FormData formData_24b = new FormData();
      formData_24b.height = 21;
      formData_24b.width = 21;
      formData_24b.right = new FormAttachment(100, -10);
      formData_24b.top = new FormAttachment(controlLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_controlButton.setLayoutData(formData_24b);
      m_controlButton.setText("..."); //$NON-NLS-1$
      m_controlButton.addSelectionListener(this);

      m_controlCombo = new CCombo(mainComp, SWT.BORDER);
      final FormData formData_24a = new FormData();
      formData_24a.top = new FormAttachment(controlLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_24a.left = new FormAttachment(controlLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      formData_24a.right = new FormAttachment(m_controlButton,
            -LABEL_HSPACE_OFFSET, SWT.LEFT);
      m_controlCombo.setLayoutData(formData_24a);
      m_controlCombo.addSelectionListener(this);

      m_showInSummaryButton = new Button(mainComp, SWT.CHECK);
      final FormData formData_25 = new FormData();
      formData_25.top = new FormAttachment(controlLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_25.left = new FormAttachment(controlLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      formData_25.right = new FormAttachment(65, 0);
      m_showInSummaryButton.setLayoutData(formData_25);
      m_showInSummaryButton.setText(PSMessages.getString("PSFieldPropertiesDialog.label.showinsummary")); //$NON-NLS-1$

      m_showInPreviewButton = new Button(mainComp, SWT.CHECK);
      final FormData formData_26 = new FormData();
      formData_26.top = new FormAttachment(m_showInSummaryButton, 0, SWT.TOP);
      formData_26.left = new FormAttachment(m_showInSummaryButton, 0, SWT.RIGHT);
      formData_26.right = new FormAttachment(100, -10);
      m_showInPreviewButton.setLayoutData(formData_26);
      m_showInPreviewButton.setText(PSMessages.getString("PSFieldPropertiesDialog.label.sjowinpreview")); //$NON-NLS-1$

      m_showClearFieldButton = new Button(mainComp, SWT.CHECK);
      final FormData formData_27 = new FormData();
      formData_27.top = new FormAttachment(m_showInSummaryButton,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_27.left = new FormAttachment(m_showInSummaryButton, 0, SWT.LEFT);
      formData_27.right = new FormAttachment(100, -10);
      m_showClearFieldButton.setLayoutData(formData_27);
      m_showClearFieldButton
            .setText(PSMessages.getString("PSFieldPropertiesDialog.label.showclearfield")); //$NON-NLS-1$

      // Search Controls
      final Label searchLabel = new Label(mainComp, SWT.WRAP);
      final FormData formData_28 = new FormData();
      formData_28.top = new FormAttachment(m_showClearFieldButton,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_28.left = new FormAttachment(0, 10);
      searchLabel.setLayoutData(formData_28);
      searchLabel.setText(PSMessages.getString("PSFieldPropertiesDialog.label.search")); //$NON-NLS-1$

      final Label searchSepLabel = new Label(mainComp, SWT.SEPARATOR
            | SWT.HORIZONTAL);
      final FormData formData_29 = new FormData();
      formData_29.top = new FormAttachment(searchLabel, 0, SWT.CENTER);
      formData_29.left = new FormAttachment(searchLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      formData_29.right = new FormAttachment(100, -10);
      searchSepLabel.setLayoutData(formData_29);

      m_allowFieldSearchButton = new Button(mainComp, SWT.CHECK);
      final FormData formData_30 = new FormData();
      formData_30.top = new FormAttachment(searchLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_30.left = new FormAttachment(m_showClearFieldButton, 0, SWT.LEFT);
      m_allowFieldSearchButton.setLayoutData(formData_30);
      m_allowFieldSearchButton.setText(PSMessages.getString("PSFieldPropertiesDialog.label.allowfieldsearched")); //$NON-NLS-1$
      m_allowFieldSearchButton.addSelectionListener(this);

      m_includeButton = new Button(mainComp, SWT.CHECK);
      final FormData formData_31 = new FormData();
      formData_31.top = new FormAttachment(m_allowFieldSearchButton,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_31.left = new FormAttachment(m_allowFieldSearchButton, 20,
            SWT.LEFT);
      m_includeButton.setLayoutData(formData_31);
      m_includeButton.setText(PSMessages.getString("PSFieldPropertiesDialog.label.includeinftquery")); //$NON-NLS-1$
      m_includeButton.addSelectionListener(this);

      //
      GridData data = new GridData(GridData.FILL_VERTICAL
            | GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_CENTER);
      mainComp.setLayoutData(data);

      // Load the control values
      loadControlValues();
      //
      mainComp.pack();
      return mainComp;
   }

   /*
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      if(!updateData())
         return;
      super.okPressed();
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent)
   {
      m_readonlyButton = createButton(parent, READONLY_BUTTON_ID,
            READONLY_BUTTON_LABEL, false);
      m_readonlyButton.addSelectionListener(this);
      m_visibilityButton = createButton(parent, VISIBILITY_BUTTON_ID,
            VISIBILITY_BUTTON_LABEL, false);
      m_visibilityButton.addSelectionListener(this);
      m_validationButton = createButton(parent, VALIDATION_BUTTON_ID,
            VALIDATION_BUTTON_LABEL, false);
      m_validationButton.addSelectionListener(this);
      m_transformButton = createButton(parent, TRANSFORM_BUTTON_ID,
            TRANSFORM_BUTTON_LABEL, false);
      m_transformButton.addSelectionListener(this);
      m_okButton = createButton(parent, IDialogConstants.OK_ID,
            IDialogConstants.OK_LABEL, true);
      m_okButton.addSelectionListener(this);

      m_cancelButton = createButton(parent, IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL, false);
      m_cancelButton.addSelectionListener(this);
   }

   @Override
   protected Point getInitialSize()
   {
      return new Point(600, 600);
   }

   public boolean updateData()
   {
      m_field = m_fieldData.getField();
      m_mapping = m_fieldData.getDisplayMapping();
      PSUISet uiSet = m_mapping.getUISet();
      // *******************Main group******************
      // **Fieldname**
      // Validate the field name before setting it, if it is not system def
      if(m_field.getType() != PSField.TYPE_SYSTEM)
      {
         String fieldName = StringUtils
               .defaultString(m_fieldNameText.getText());
         String oldName = m_field.getSubmitName();
         // Validate the field name if new and old names are different.
         if (!oldName.equalsIgnoreCase(fieldName))
         {
            boolean isValid = isValidFieldName(fieldName);
            if (!isValid)
               return false;
         }
         m_field.setSubmitName(m_fieldNameText.getText());
         // **Data type and Storage size**
         m_field.setDataType(m_dataTypeAndFormat.getDataType());
         m_field.setDataFormat(m_dataTypeAndFormat.getDataFormat());
      }
      // **Default value**
      m_field.setDefault(m_defaultValueControl.getValue());
      // **Treat data as binary**
      m_field.setForceBinary(m_treatDataAsBinaryButton.getSelection());
      // **Treat field as metadata**
      int temp = PSField.FIELD_VALUE_TYPE_CONTENT;
      if(m_treatFieldAsMetadataButton.getSelection())
         temp = PSField.FIELD_VALUE_TYPE_META;
      m_field.setFieldValueType(temp);
      // **Treat field as exportable**
      m_field.setExportable(m_treatFieldAsExportableButton.getSelection());
      
      // ******************Display group ******************
      // **Label**
      uiSet.getLabel().setText(m_labelText.getText());
      // **Mnemonic**
      uiSet.setAccessKey(m_mnemonicControl.getMnemonic());
      // **Error Label**
      uiSet.setErrorLabel(new PSDisplayText(m_errorLabelText.getText()));
      // **Control**
      // **Show in Summary**
      m_field.setShowInSummary(m_showInSummaryButton.getSelection());
      // **Show in Preview**
      m_field.setShowInPreview(m_showInPreviewButton.getSelection());
      // **Show clear check box**
      m_field.setClearBinaryParam(m_showClearFieldButton.getSelection());
      // ******************Search group ******************
      // **Allow this field to be searched**
      PSSearchProperties sprops = m_field.getSearchProperties();
      sprops.setUserSearchable(m_allowFieldSearchButton.getSelection());
      sprops.setVisibleToGlobalQuery(m_includeButton.getSelection());
      m_field.setSearchProperties(sprops);
      //Reset the maxlength parameter.
      PSContentEditorDefinition.setMaxLengthParam(m_fieldData.getControlRef(),
            m_dataTypeAndFormat.getDataFormat());
           
      return true;
   }

   /**
    * Validates the supplied string for field name.
    * Checks whether the string is valid name or not then
    * checks whether a field exists with this name or not. 
    * @param fieldName The string to be checked.
    * @return <code>true</code> if it is a valid name otherwise
    * <code>false</code>. 
    */
   private boolean isValidFieldName(String fieldName)
   {
      fieldName = StringUtils.defaultString(fieldName);
      if (m_fieldData.getName().equalsIgnoreCase(fieldName))
      {
         return true;
      }

      String validationError = PSContentEditorDefinition
            .validateFieldName(fieldName);
      if (validationError != null)
      {
         MessageDialog
               .openError(
                     getShell(),
                     PSMessages
                           .getString("PSFieldPropertiesDialog.error.title.invalidfieldname"),
                     validationError);
         return false;
      }
      for (int i = 0; i < m_allFieldNames.length; i++)
      {
         if (fieldName.equalsIgnoreCase(StringUtils.defaultString(
               m_allFieldNames[i]).trim()))
         {
            MessageDialog
                  .openError(
                        getShell(),
                        PSMessages
                              .getString("PSFieldPropertiesDialog.error.title.duplicatefieldname"), //$NON-NLS-1$
                        PSMessages
                              .getString("PSFieldPropertiesDialog.error.message.duplicatefieldname")); //$NON-NLS-1$
            return false;

         }
      }
      return true;
   }

   /**
    * Handles all control selection events.
    */
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_transformButton)
      {
         PSFieldDefinition fieldDefCopy = PSFieldDefinition
               .deepCopy(m_fieldData.getFieldDefinition());
         PSFieldTableRowDataObject fieldDataCopy = new PSFieldTableRowDataObject(
               fieldDefCopy);
         PSFieldTransformationsDialog propDlg = new PSFieldTransformationsDialog(
               getShell(), fieldDataCopy);
         if (propDlg.open() == Dialog.OK)
         {
            PSFieldTranslation inputTrans = fieldDataCopy.getField().getInputTranslation();
            m_fieldData.getField().setInputTranslation(inputTrans);
            PSFieldTranslation outputTrans = fieldDataCopy.getField().getOutputTranslation();
            m_fieldData.getField().setOutputTranslation(outputTrans);
         }
      }
      else if (e.getSource() == m_validationButton)
      {
         if (PSContentEditorDefinition.isLocalEditorType(m_editorType) &&
            !PSContentEditorDefinition.checkFieldValidationAllowed(
               m_fieldData.getField()))
         {
            return;
         }
         PSFieldDefinition fieldDefCopy = PSFieldDefinition.deepCopy(m_fieldData
               .getFieldDefinition());
         PSFieldTableRowDataObject fieldDataCopy = new PSFieldTableRowDataObject(
               fieldDefCopy);
         PSFieldValidationsDialog propDlg = new PSFieldValidationsDialog(
               getShell(), fieldDataCopy, m_editorType, m_singleDimensionFieldNames);
         if(propDlg.open()==Dialog.OK)
         {
            PSFieldValidationRules valRules = fieldDataCopy.getField().getValidationRules();
            m_fieldData.getField().setValidationRules(valRules);
         }
      }
      else if (e.getSource() == m_visibilityButton)
      {
         PSFieldDefinition fieldDefCopy = PSFieldDefinition
               .deepCopy(m_fieldData.getFieldDefinition());
         PSFieldTableRowDataObject fieldDataCopy = new PSFieldTableRowDataObject(
               fieldDefCopy);
         PSFieldVisibilityDialog propDlg = new PSFieldVisibilityDialog(
               getShell(), fieldDataCopy, false);
         if (propDlg.open() == Dialog.OK)
         {
            PSVisibilityRules visRules = fieldDataCopy.getField().getVisibilityRules();
            m_fieldData.getField().setVisibilityRules(visRules);
         }
      }
      else if (e.getSource() == m_readonlyButton)
      {
         PSFieldDefinition fieldDefCopy = PSFieldDefinition
               .deepCopy(m_fieldData.getFieldDefinition());
         PSFieldTableRowDataObject fieldDataCopy = new PSFieldTableRowDataObject(
               fieldDefCopy);
         PSFieldVisibilityDialog propDlg = new PSFieldVisibilityDialog(
               getShell(), fieldDataCopy, true);
         if (propDlg.open() == Dialog.OK)
         {
            Iterator iter = fieldDataCopy.getDisplayMapping().getUISet()
                  .getReadOnlyRules();
            PSCollection rorules = new PSCollection(PSRule.class);
            while (iter.hasNext())
            {
               rorules.add(iter.next());
            }
            m_fieldData.getDisplayMapping().getUISet()
                  .setReadOnlyRules(rorules);
         }
      }
      else if (e.getSource() == m_showClearFieldButton)
      {

      }
      else if (e.getSource() == m_treatDataAsBinaryButton)
      {
         if (m_treatDataAsBinaryButton.getSelection())
         {
            m_showClearFieldButton.setEnabled(true);
            m_showClearFieldButton.setSelection(true);
         }
         else
         {
            m_showClearFieldButton.setEnabled(false);
            m_showClearFieldButton.setSelection(false);
         }
      }
      else if (e.getSource() == m_includeButton)
      {
         if (m_includeButton.getSelection())
            m_allowFieldSearchButton.setSelection(true);
      }
      else if (e.getSource() == m_controlCombo)
      {
         PSControlRef oldCtrlRef = m_fieldData.getControlRef();
         String oldCtrl = m_fieldData.getControlName();
         String newCtrl = m_controlCombo.getText();
         String[] items = m_controlCombo.getItems();
         if (!oldCtrl.equals(newCtrl))
         {
            if (m_field.isLocalField() || 
                  (m_field.isSharedField() && 
                        m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR))
            {
               List<String> dataList = PSContentEditorDefinition
               .getDefaultDataTypeAndFormat(newCtrl);
               String newType = dataList.get(0);
               String newFormat = dataList.get(1);
               if(m_field.getLocator() != null)
               {
                  if (!m_field.getDataType().equals(newType))
                  {
                     if(!MessageDialog.openConfirm(getShell(),PSMessages.getString(
                     "PSFieldDataTypeAndFormatComposite.error.possibledataloss.title"), 
                     PSMessages.getString(
                           "PSFieldDataTypeAndFormatComposite.error.possibledataloss.datatype.message")))
                     {
                        m_controlCombo.select(PSContentEditorDefinition
                              .getItemIndex(items, oldCtrl));
                        return;
                     }
                     else
                     {
                        m_field.setDataType(newType);
                        m_field.setDataFormat(newFormat);
                     }
                  }
                  else if (m_field.getDataType().equals("text")
                        && newType.equals("text"))
                  {
                     // If old and new types are text.
                     // change the format only if the old format is less than
                     // new default format.
                     String oldFormat = m_field.getDataFormat();
                     boolean change = false;
                     if (oldFormat == null)
                        change = true;
                     else if(oldFormat.equals("max"))
                        change = false;
                     else if(newFormat.equals("max"))
                        change = true;
                     else if(!StringUtils.isNumeric(oldFormat))
                        change = true;
                     else if(Integer.parseInt(oldFormat) < Integer
                                 .parseInt(newFormat))
                        change = true;
                     if(change)
                     {
                        m_field.setDataFormat(newFormat);
                     }
                  }
               }
               else
               {
                  m_field.setDataType(newType);
                  m_field.setDataFormat(newFormat);
               }
            }
            m_dataTypeAndFormat.setDataTypeAndFormat(m_field.getDataType(),
                  m_field.getDataFormat(), m_field.getLocator() != null);
            PSContentEditorDefinition.setMaxLengthParam(m_fieldData
                  .getControlRef(), m_fieldData.getField().getDataFormat());
            m_fieldData.setControlName(newCtrl);
            PSContentEditorDefinition.setDefaultInlineLinkProperties(m_field,
                  newCtrl);
            PSContentEditorDefinition.preserveControlProperties(oldCtrlRef,
                  m_fieldData.getControlRef());
         }
      }
      else if (e.getSource() == m_allowFieldSearchButton)
      {
         m_includeButton.setSelection(m_allowFieldSearchButton.getSelection());
      }
      else if (e.getSource() == m_controlButton)
      {
         PSFieldDefinition fieldDefCopy = PSFieldDefinition
               .deepCopy(m_fieldData.getFieldDefinition());
         PSFieldTableRowDataObject fieldDataCopy = new PSFieldTableRowDataObject(
               fieldDefCopy);
         PSControlPropertiesDialog dlg = new PSControlPropertiesDialog(
               getShell(), fieldDataCopy, m_editorType);
         int result = dlg.open();
         if (result == Dialog.OK)
         {
            m_fieldData = fieldDataCopy;
            // Update the control combo
            m_controlCombo.select(PSContentEditorDefinition.getItemIndex(
                  m_controlNames, m_fieldData.getControlName()));
            m_field = m_fieldData.getField();
            m_mapping = m_fieldData.getDisplayMapping();
         }
      }
   }

   public void widgetDefaultSelected(
         SelectionEvent e) //$NON-NLS-1$
   {
   }

   /**
    * Method to load the control values.
    */
   private void loadControlValues()
   {
      m_field = m_fieldData.getField();
      m_mapping = m_fieldData.getDisplayMapping();
      PSUISet uiSet = m_mapping.getUISet();
      // *******************Main group******************
      // **Fieldname**
      m_fieldNameText.setText(m_field.getSubmitName());
      // **Data type and Storage size**
      int type = m_field.getType();
      // For system and shared fields set the data type, storage size,  
      // and field value type (metadata) are non-editable.
      if (type == PSField.TYPE_SYSTEM)
      {
         m_dataTypeAndFormat.setDataTypeAndFormatEnabled(false);
         m_fieldNameText.setEnabled(false);
         m_treatFieldAsMetadataButton.setEnabled(false);
      }
      // If the field is shared but you are not editing it part of the shared
      // def then set the data type, storage size, and field value type (metadata) 
      // as non-editable.
      else if (type == PSField.TYPE_SHARED
            && m_editorType != PSContentEditorDefinition.SHAREDDEF_EDITOR)
      {
         m_dataTypeAndFormat.setDataTypeAndFormatEnabled(false);
         m_fieldNameText.setEnabled(false);
         m_treatFieldAsMetadataButton.setEnabled(false);
      }
      else
      {
         m_dataTypeAndFormat.setDataTypeAndFormatEnabled(true);
      }

      m_dataTypeAndFormat.setDataTypeAndFormat(m_field.getDataType(), m_field
            .getDataFormat(), m_field.getLocator() != null);
      // **Default value**
      m_defaultValueControl.setValue(m_field);
      // **Mime type value**
      m_mimeTypeComposite.setValues(m_field,m_singleDimensionFieldNames);
      
      // **Treat data as binary**
      m_treatDataAsBinaryButton.setSelection(m_field.isForceBinary());

      // **Treat field as metadata**
      if(m_field.getFieldValueType() == PSField.FIELD_VALUE_TYPE_META)
         m_treatFieldAsMetadataButton.setSelection(true);
      else
         m_treatFieldAsMetadataButton.setSelection(false);
      
      m_treatFieldAsExportableButton.setSelection(m_field.isExportable());
         
      // ******************Display group ******************
      // **Label**
      m_labelText.setText(uiSet.getLabel().getText());
      // **Mnemonic**
      String mnem = uiSet.getAccessKey();
      m_mnemonicControl.setInput(StringUtils.defaultString(m_fieldData
            .getLabelText()), StringUtils.defaultString(mnem));
      // **Error Label**
      if (uiSet.getErrorLabel() != null)
         m_errorLabelText.setText(uiSet.getErrorLabel().getText());
      // **Control**
      try
      {
         /* The behavior in the properties dialog is slightly different than
          * in the table. In the table, if the field has not been saved, you
          * can change the control between single and multi types. But in the
          * properties, we act as if the field had been saved and only allow
          * changes w/in the same type. 
          */
         PSControlMeta meta = PSContentEditorDefinition
               .getControl(uiSet.getControl().getName());
         int controlType;
         if (meta.getDimension().equals(PSControlMeta.ARRAY_DIMENSION))
            controlType = PSContentEditorDefinition.ARRAY_DIM_CONTROLS;
         else
            controlType = PSContentEditorDefinition.SINGLE_DIM_CONTROLS;
         m_controlNames = PSContentEditorDefinition
               .getControlNames(controlType);
         m_controlCombo.setItems(m_controlNames);
         m_controlCombo.select(PSContentEditorDefinition.getItemIndex(
               m_controlNames, uiSet.getControl().getName()));
      }
      catch (PSModelException e)
      {
         String title = PSMessages.getString("PSFieldPropertiesDialog.error.title.failedtogetcontrol"); //$NON-NLS-1$
         String msg = PSMessages.getString("PSFieldPropertiesDialog.error.message.failedtogettitle"); //$NON-NLS-1$
         PSWorkbenchPlugin.handleException("Field Properties Dialog",title, msg,e); //$NON-NLS-1$
      }
      // **Show in Summary**
      if (m_editorType == PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
         m_showInSummaryButton.setSelection(m_field.isShowInSummary());
      else
         m_showInSummaryButton.setEnabled(false);
      // **Show in Preview**
      m_showInPreviewButton.setSelection(m_field.isShowInPreview());
      // **Show clear check box**
      boolean isFormatBinary = false;
      if (m_field.getDataType().equals("binary")) //$NON-NLS-1$
         isFormatBinary = true;

      if (m_treatDataAsBinaryButton.getSelection() || isFormatBinary)
      {
         m_showClearFieldButton.setEnabled(true);
         if (m_field.getClearBinaryParam() != null)
            m_showClearFieldButton.setSelection(true);
         else
            m_showClearFieldButton.setSelection(false);
      }
      else
      {
         m_showClearFieldButton.setEnabled(false);
      }
      // ******************Search group ******************
      // **Allow this field to be searched**
      PSSearchProperties sprops = m_field.getSearchProperties();
      m_allowFieldSearchButton.setSelection(sprops.isUserSearchable());
      // **Other search fields**
      if (m_allowFieldSearchButton.getSelection())
      {
         if (sprops.isEnableTransformation())
         {
            m_includeButton.setSelection(true);
            m_includeButton.setEnabled(false);
         }
         else
         {
            m_includeButton.setSelection(sprops.isVisibleToGlobalQuery());
         }
      }
      else
      {
         m_includeButton.setSelection(false);
      }
   }

   public PSFieldTableRowDataObject getFieldRowData()
   {
      return m_fieldData;
   }

   /*
    * Controls
    */
   private PSFieldDataTypeAndFormatComposite m_dataTypeAndFormat;
   private Text m_fieldNameText;
   private PSDefaultValueControl m_defaultValueControl;
   private PSMimeTypeComposite m_mimeTypeComposite;
   private PSField m_field;
   private PSDisplayMapping m_mapping;
   private PSMnemonicControl m_mnemonicControl;
   private Button m_treatDataAsBinaryButton;
   private Button m_treatFieldAsMetadataButton;
   private Button m_treatFieldAsExportableButton;
   private Text m_labelText;
   private Text m_errorLabelText;
   private CCombo m_controlCombo;
   private Button m_controlButton;
   private Button m_showInSummaryButton;
   private Button m_showInPreviewButton;
   private Button m_showClearFieldButton;
   private Button m_allowFieldSearchButton;
   private Button m_includeButton;
   private static int TRANSFORM_BUTTON_ID = 2;
   private static int VALIDATION_BUTTON_ID = 3;
   private static int VISIBILITY_BUTTON_ID = 4;
   private static int READONLY_BUTTON_ID = 5;
   private static String TRANSFORM_BUTTON_LABEL = PSMessages.getString("PSFieldPropertiesDialog.button.label.transforms"); //$NON-NLS-1$
   private static String VALIDATION_BUTTON_LABEL = PSMessages.getString("PSFieldPropertiesDialog.button.label.validation"); //$NON-NLS-1$
   private static String VISIBILITY_BUTTON_LABEL = PSMessages.getString("PSFieldPropertiesDialog.button.label.visibility"); //$NON-NLS-1$
   private static String READONLY_BUTTON_LABEL = PSMessages.getString("PSFieldPropertiesDialog.button.label.readonly"); //$NON-NLS-1$
   private Button m_okButton;
   private Button m_cancelButton;
   private Button m_transformButton;
   private Button m_validationButton;
   private Button m_visibilityButton;
   private Button m_readonlyButton;
   private String[] m_controlNames;

   // Data object
   private PSFieldTableRowDataObject m_fieldData;
   private int m_editorType;
   private String[] m_allFieldNames;
   private String[] m_singleDimensionFieldNames;
}

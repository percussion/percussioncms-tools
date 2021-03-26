/******************************************************************************
 *
 * [ PSFieldSetPropertiesComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSMnemonicControl;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class PSFieldSetPropertiesComposite extends Composite implements
   IPSUiConstants, SelectionListener
{
   public PSFieldSetPropertiesComposite(PSCEFieldsCommonComposite parent,
      int style, int editorType, PSEditorBase editor)
   {
      super(parent, style);
      if (!PSContentEditorDefinition.isValidEditorType(editorType))
         throw new IllegalArgumentException("Invalid editor type");
      m_editor = editor;
      m_parentComp = parent;
      setLayout(new FormLayout());

      final Label mainLabel = new Label(this, SWT.WRAP);
      final FormData formData_0 = new FormData();
      formData_0.top = new FormAttachment(0, 0);
      formData_0.left = new FormAttachment(0, 0);
      mainLabel.setLayoutData(formData_0);
      mainLabel.setText(PSMessages.getString(
         "PSFieldSetPropertiesComposite.label.fieldsetproperties"));

      final Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(mainLabel, 0, SWT.CENTER);
      formData.left = new FormAttachment(mainLabel, LABEL_HSPACE_OFFSET,
         SWT.RIGHT);
      label.setLayoutData(formData);

      m_mnemonicControl = new PSMnemonicControl(this, style);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(mainLabel, LABEL_VSPACE_OFFSET,
         SWT.BOTTOM);
      formData_1.left = new FormAttachment(mainLabel, LABEL_HSPACE_OFFSET,
         SWT.LEFT);
      formData_1.right = new FormAttachment(100, 0);
      m_mnemonicControl.setLayoutData(formData_1);
      m_mnemonicControl.getMnemonicCombo().addSelectionListener(this);

      m_enableSearchingForButton = new Button(this, SWT.CHECK);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_mnemonicControl,
         LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_mnemonicControl, 0, SWT.LEFT);
      m_enableSearchingForButton.setLayoutData(formData_3);
      m_enableSearchingForButton.setText(PSMessages.getString(
         "PSFieldSetPropertiesComposite.checkbox.enablefieldsetsearch"));
      m_enableSearchingForButton.addSelectionListener(this);

      m_allowUserToButton = new Button(this, SWT.CHECK);
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(m_enableSearchingForButton,
         LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_4.left = new FormAttachment(m_mnemonicControl, 0, SWT.LEFT);
      m_allowUserToButton.setLayoutData(formData_4);
      m_allowUserToButton.setText(PSMessages.getString(
         "PSFieldSetPropertiesComposite.checkbox.alloworder"));
      m_allowUserToButton.addSelectionListener(this);

      final Label validationOnWorkflowLabel = new Label(this, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_allowUserToButton,
         LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_5.left = new FormAttachment(m_mnemonicControl, 0, SWT.LEFT);
      validationOnWorkflowLabel.setLayoutData(formData_5);
      validationOnWorkflowLabel.setText(PSMessages.getString(
         "PSFieldSetPropertiesComposite.label.workflowvalidation"));

      final Label label_1 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
      final FormData formData_6 = new FormData();
      formData_6.right = new FormAttachment(100, 0);
      formData_6.top = new FormAttachment(validationOnWorkflowLabel, 0,
         SWT.CENTER);
      formData_6.left = new FormAttachment(validationOnWorkflowLabel,
         LABEL_HSPACE_OFFSET, SWT.RIGHT);
      label_1.setLayoutData(formData_6);

      final Label occurrenceLabel = new Label(this, SWT.WRAP);
      final FormData formData_7 = new FormData();
      formData_7.left = new FormAttachment(validationOnWorkflowLabel,
         LABEL_HSPACE_OFFSET, SWT.LEFT);
      formData_7.top = new FormAttachment(validationOnWorkflowLabel,
         LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      occurrenceLabel.setLayoutData(formData_7);
      occurrenceLabel.setText(PSMessages.getString(
         "PSFieldSetPropertiesComposite.label.occurrence"));

      final ComboViewer occComboViewer = new ComboViewer(this, 
         SWT.BORDER | SWT.READ_ONLY);
      m_occurrenceCombo = occComboViewer.getCombo();
      final FormData formData_8 = new FormData();
      formData_8.top = new FormAttachment(occurrenceLabel,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_8.left = new FormAttachment(occurrenceLabel, LABEL_HSPACE_OFFSET,
         SWT.RIGHT);
      m_occurrenceCombo.setLayoutData(formData_8);
      m_occurrenceCombo.setItems(ms_occurrenceOptions);
      m_occurrenceCombo.addSelectionListener(this);

      final Label countLabel = new Label(this, SWT.WRAP);
      final FormData formData_9 = new FormData();
      formData_9.top = new FormAttachment(occurrenceLabel, 0, SWT.TOP);
      formData_9.left = new FormAttachment(50, 0);
      countLabel.setLayoutData(formData_9);
      countLabel.setText(PSMessages.getString(
         "PSFieldSetPropertiesComposite.label.count"));

      m_countSpinner = new Spinner(this, SWT.BORDER);
      m_countSpinner.setIncrement(1);
      m_countSpinner.setMinimum(1);
      m_countSpinner.setMaximum(Integer.MAX_VALUE);
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(countLabel,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_10.left = new FormAttachment(countLabel, LABEL_HSPACE_OFFSET,
         SWT.RIGHT);
      m_countSpinner.setLayoutData(formData_10);
      m_countSpinner.addSelectionListener(this);
   }

   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_mnemonicControl.getMnemonicCombo())
      {
         m_mapping.getUISet().setAccessKey(m_mnemonicControl.getMnemonic());
      }
      else if (e.getSource() == m_enableSearchingForButton)
      {
         m_fieldSet
            .setUserSearchable(m_enableSearchingForButton.getSelection());
      }
      else if (e.getSource() == m_allowUserToButton)
      {
         m_fieldSet.setSequencingSupported(m_allowUserToButton.getSelection());
      }
      else if (e.getSource() == m_occurrenceCombo)
      {
         if (m_occurrenceCombo.getText().equals(OCCURS_COUNT))
         {
            m_countSpinner.setEnabled(true);
            m_fieldSet.setCount(m_countSpinner.getSelection());
         }
         else
         {
            m_countSpinner.setEnabled(false);
            m_fieldSet.setRepeatability(m_occurrenceCombo.getSelectionIndex());
         }
      }
      else if (e.getSource() == m_countSpinner)
      {
         m_fieldSet.setCount(m_countSpinner.getSelection());
      }
      
      m_editor.updateDesignerObject(m_editor.getDesignerObject(), m_parentComp
         .getTableComp());
      m_editor.setDirty();
   }

   public void widgetDefaultSelected(
      @SuppressWarnings("unused") SelectionEvent e)
   {
      // noop
   }

   public void loadData(PSFieldTableRowDataObject data, 
      @SuppressWarnings("unused") int currentRow)
   {
      if (data == null)
         throw new IllegalArgumentException("data must not be null");

      m_fieldSet = data.getFieldSet();
      m_mapping = data.getDisplayMapping();

      // Set the mnemonic control
      String mnem = m_mapping.getUISet().getAccessKey();
      m_mnemonicControl.setInput(
         StringUtils.defaultString(data.getLabelText()), StringUtils
            .defaultString(mnem));

      // Set the enable searching button
      m_enableSearchingForButton.setSelection(m_fieldSet.isUserSearchable());
      // Set the allow user to order entries button
      m_allowUserToButton.setSelection(m_fieldSet.isSequencingSupported());

      int occurr = m_fieldSet.getRepeatability();
      int index = -1;
      m_countSpinner.setEnabled(false);
      if (occurr == PSFieldSet.REPEATABILITY_ONE_OR_MORE)
         index = m_occurrenceCombo.indexOf(OCCURS_REQUIRED);
      else if (occurr == PSFieldSet.REPEATABILITY_ZERO_OR_MORE)
         index = m_occurrenceCombo.indexOf(OCCURS_OPTIONAL);
      else if (occurr == PSFieldSet.REPEATABILITY_COUNT)
      {
         index = m_occurrenceCombo.indexOf(OCCURS_COUNT);
         m_countSpinner.setEnabled(true);
         m_countSpinner.setSelection(m_fieldSet.getCount());
      }
      m_occurrenceCombo.select(index);
   }

   public void updateData()
   {

   }

   /*
    * Controls
    */
   private Button m_enableSearchingForButton;

   private Button m_allowUserToButton;

   /**
    * User control to specify the count if the occurrence control is set to
    * <code>OCCURS_COUNT</code>. Always >=1, incremented by 1.
    */
   private Spinner m_countSpinner;

   /**
    * User control to specifies the occurrence requirements of a field.
    */
   private Combo m_occurrenceCombo;

   private PSFieldSet m_fieldSet;

   private PSMnemonicControl m_mnemonicControl;

   private PSEditorBase m_editor;

   private PSCEFieldsCommonComposite m_parentComp;

   private PSDisplayMapping m_mapping;

   /**
    * Constant to define optional occurrence setting for field or field set.
    */
   public final static String OCCURS_OPTIONAL = PSMessages.getString(
      "PSFieldSetPropertiesComposite.occurrence.optional");

   /**
    * Constant to define required occurrence setting for field or field set.
    */
   public final static String OCCURS_REQUIRED = PSMessages.getString(
      "PSFieldSetPropertiesComposite.occurrence.required");

   /**
    * Constant to define count occurrence setting for field or field set.
    */
   public final static String OCCURS_COUNT = PSMessages.getString(
      "PSFieldSetPropertiesComposite.occurrence.count");

   /**
    * Static array to hold the occurrence options. The index of this array is
    * the value used in the underlying field set.
    */
   private static String[] ms_occurrenceOptions = 
   { 
      OCCURS_OPTIONAL,
      OCCURS_REQUIRED, 
      OCCURS_COUNT 
   };
}

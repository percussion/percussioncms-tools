/******************************************************************************
 *
 * [ PSFieldValidationsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.E2Designer.PSRuleEditorDialog;
import com.percussion.design.objectstore.*;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.utils.PSContentTypeUtils;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSTableModifiedListener;
import com.percussion.workbench.ui.controls.PSTableEvent;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import com.percussion.workbench.ui.legacy.AwtSwtModalDialogBridge;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Dialog to show the field validation rules in a tabular format and composites
 * to edit the rules.
 * 
 */
public class PSFieldValidationsDialog extends PSDialog
      implements
         IPSUiConstants,
         SelectionListener
{

   /**
    * 
    * @param parentShell Shell object of parent. Must not be <code>null</code>.
    * @param rowData Object of PSFieldTableRowDataObject consisting of input
    *           data.
    * @param editorType Type of editor, must be a valid editor type as defined
    *           in {@link PSContentEditorDefinition#isValidEditorType(int)}.
    * @param fieldNames String array of other field names in the editor to
    *           compare.
    */
   public PSFieldValidationsDialog(Shell parentShell,
         PSFieldTableRowDataObject rowData, int editorType, String[] fieldNames)
   {
      super(parentShell);
      if (parentShell == null)
      {
         throw new IllegalArgumentException("parentShell must not be null"); //$NON-NLS-1$
      }
      if (rowData == null)
      {
         throw new IllegalArgumentException("fieldData must not be null"); //$NON-NLS-1$
      }
      if (!PSContentEditorDefinition.isValidEditorType(editorType))
      {
         throw new IllegalArgumentException("editorType is invalid"); //$NON-NLS-1$
      }
      m_rowData = rowData;
      m_fieldNames = fieldNames;
      m_fieldName = m_rowData.getName();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite mainComp = (Composite) super.createDialogArea(parent);
      mainComp.setLayout(new FormLayout());
      Composite topComp = new Composite(mainComp, SWT.NONE);
      topComp.setLayout(new FormLayout());
      FormData fd1 = new FormData();
      fd1.top = new FormAttachment(0, 20);
      fd1.left = new FormAttachment(0, 10);
      fd1.right = new FormAttachment(100, -10);
      topComp.setLayoutData(fd1);

      // Pre requ button area
      m_preRequisitesBtn = new Button(topComp, SWT.NONE);
      FormData fd2 = new FormData();
      fd2.top = new FormAttachment(0,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      fd2.left = new FormAttachment(0, 0);
      fd2.width = 100;
      m_preRequisitesBtn.setLayoutData(fd2);
      m_preRequisitesBtn.setText(PSMessages
            .getString("PSFieldValidationsDialog.label.button.prerequisites")); //$NON-NLS-1$
      m_preRequisitesBtn.addSelectionListener(this);

      final Label preReqLabel = new Label(topComp, SWT.WRAP);
      final FormData fd3 = new FormData();
      fd3.top = new FormAttachment(m_preRequisitesBtn,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      fd3.left = new FormAttachment(m_preRequisitesBtn, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      preReqLabel.setLayoutData(fd3);
      preReqLabel.setText(PSMessages
            .getString("PSFieldValidationsDialog.label.prerequisitesmessage")); //$NON-NLS-1$

      m_required = new Button(topComp,SWT.CHECK);
      FormData reqFd = new FormData();
      reqFd.top = new FormAttachment(m_preRequisitesBtn,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      reqFd.left = new FormAttachment(m_preRequisitesBtn, 0,SWT.LEFT);
      reqFd.right = new FormAttachment(40,0);
      m_required.setLayoutData(reqFd);
      m_required.setText("Required"); //$NON-NLS-1$
      m_required.addSelectionListener(this);

      m_skipValidation = new Button(topComp,SWT.CHECK);
      FormData skipFd = new FormData();
      skipFd.top = new FormAttachment(m_required,
            0, SWT.TOP);
      skipFd.left = new FormAttachment(40,0);
      skipFd.right = new FormAttachment(100,0);
      m_skipValidation.setLayoutData(skipFd);
      m_skipValidation.setText("Skip Validation if Field is Empty"); //$NON-NLS-1$
      m_skipValidation.addSelectionListener(this);

      
      // Validation table area
      final Label ruleTableLabel = new Label(topComp, SWT.WRAP);
      final FormData fd4 = new FormData();
      fd4.top = new FormAttachment(m_required, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      fd4.left = new FormAttachment(0, 0);
      ruleTableLabel.setLayoutData(fd4);
      ruleTableLabel.setText(PSMessages
            .getString("PSFieldValidationsDialog.label.validationtabletitle")); //$NON-NLS-1$

      m_rulesComp = new ValidationRulesComposite(topComp, SWT.NONE,
            m_fieldName, m_fieldNames);
      final FormData fd5 = new FormData();
      fd5.top = new FormAttachment(ruleTableLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      fd5.left = new FormAttachment(0, 0);
      fd5.right = new FormAttachment(100, 0);
      m_rulesComp.setLayoutData(fd5);
      m_rulesComp.setRuleComposites();

      // validation failure message
      final Label valFaillureLabel = new Label(topComp, SWT.WRAP);
      final FormData fd12 = new FormData();
      fd12.top = new FormAttachment(m_rulesComp, LABEL_VSPACE_OFFSET
            + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      fd12.left = new FormAttachment(0, 0);
      valFaillureLabel.setLayoutData(fd12);
      valFaillureLabel
            .setText(PSMessages
                  .getString("PSFieldValidationsDialog.label.validationfailuremessage")); //$NON-NLS-1$

      m_errorMessage = new Text(topComp, SWT.WRAP | SWT.BORDER);
      final FormData fd13 = new FormData();
      fd13.top = new FormAttachment(m_rulesComp, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      fd13.left = new FormAttachment(valFaillureLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      fd13.right = new FormAttachment(100, 0);
      fd13.height = DESCRIPTION_FIELD_HEIGHT / 2;
      m_errorMessage.setLayoutData(fd13);
      // workflow transition area
      // Grid data
      GridData data = new GridData(GridData.FILL_HORIZONTAL
            | GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
      mainComp.setLayoutData(data);
      loadControlValues();
      return mainComp;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected void createButtonsForButtonBar(Composite parent)
   {
      createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
            true);
      createButton(parent, IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL, false);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.window.Window#getInitialSize()
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(600, 700);
   }

   /*
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      m_rulesComp.checkForRuleChanges();
      if (!updateData())
         return;
      super.okPressed();
   }

   /**
    * Gets the PSFieldTableRowDataObject
    * 
    * @return PSFieldTableRowDataObject
    */
   public PSFieldTableRowDataObject getFieldRowData()
   {
      return m_rowData;
   }

   /**
    * Convenient method to update the data.
    * 
    * @return <code>true</code> if succeeds, otherwise shows the error message
    *         and returns <code>false</code>.
    */
   private boolean updateData()
   {
      PSField field = m_rowData.getField();
      PSFieldValidationRules valrules = new PSFieldValidationRules();
      if(m_applyWhen == null)
         m_applyWhen = new PSApplyWhen();
      PSCollection rulescol = new PSCollection(PSRule.class);
      List<PSRule> rules = m_rulesComp.getRules();
      //If required check box is checked then add required rule
      if (m_required.getSelection())
      {
         addRequiredRule(rules,m_fieldName);
         m_applyWhen.setIfFieldEmpty(true);
      }
      else
      {
         m_applyWhen.setIfFieldEmpty(!m_skipValidation.getSelection());
      }
      if(rules.isEmpty())
      {
         field.setValidationRules(null);
      }
      else
      {
         valrules.setApplyWhen(m_applyWhen);
         rulescol.addAll(rules);

         valrules.setRules(rulescol);

         valrules.setErrorMessage(new PSDisplayText(StringUtils
               .defaultString(m_errorMessage.getText())));
         field.setValidationRules(valrules);
      }
      
      Integer[] occSetsIds = field.getOccurenceSettingsTransitionIds();
      if (occSetsIds.length > 1
            || (occSetsIds.length == 1 && occSetsIds[0] != null))
      {
         MessageDialog
               .openInformation(
                     getShell(),
                     PSMessages
                           .getString("PSFieldValidationsDialog.error.title.unsupportedoccurence"), //$NON-NLS-1$
                     PSMessages
                           .getString("PSFieldValidationsDialog.error.msg.unsupportedoccurence")); //$NON-NLS-1$
      }

      int occur = m_required.getSelection()
            ? PSField.OCCURRENCE_DIMENSION_REQUIRED
            : PSField.OCCURRENCE_DIMENSION_OPTIONAL;
      try
      {
         field.clearOccurrenceSettings();
         field.setOccurrenceDimension(occur, null);
      }
      catch ( PSSystemValidationException e)
      {
         // We can safely ignore this as we are setting a valid value here.
         e.printStackTrace();
      }
      
      return true;
   }

   
   /**
    * Adds the required rule to the supplied list of rules. If supplied rules is
    * <code>null</code> new array list is created and required rule is added.
    * If the required rule already exists in the supplied rules does nothing.
    * 
    * @param rules The rules for which the required rule needs to be added. May
    *           be <code>null</code>.
    * @param fieldName The name of the must not be <code>null</code> or empty.
    * 
    */
   public static void addRequiredRule(List<PSRule> rules, String fieldName)
   {
      if(StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException("fieldName must not be null");
      
      if(rules == null)
         rules = new ArrayList<PSRule>();
      if(PSContentTypeUtils.hasRequiredRule(rules))
         return;

      // Catalog extensions
      List<PSExtensionRef> fvExits = PSFieldRuleDetailsComposites
            .getExtensions(IPSFieldValidator.class.getName());
      
      PSContentTypeUtils.addRequiredRule(rules, fieldName, fvExits);
   }

   /**
    * Convenient method for loading the control values.
    */
   @SuppressWarnings("unchecked")
   private void loadControlValues()
   {
      PSField fld = m_rowData.getField();
      PSFieldValidationRules valrules = fld.getValidationRules();
      List<PSRule> rules = new ArrayList<PSRule>();
      if (valrules != null)
      {
         Iterator iter = valrules.getRules();
         while (iter.hasNext())
         {
            PSRule rule = (PSRule) iter.next();
            if (rule != null)
            {
               rules.add(rule);
            }
         }
         m_applyWhen = valrules.getApplyWhen();
         if (valrules.getErrorMessage() != null)
            m_errorMessage.setText(StringUtils.defaultString(valrules
                  .getErrorMessage().getText()));
      }
      int occur = fld.getOccurrenceDimension(null);

      // If occurence dimension is required or it has a required rule. Then
      // remove required rule if it has and set the required check box to
      // checked and skip validation check box to checked and disabled.
      if (occur == PSField.OCCURRENCE_DIMENSION_REQUIRED || 
            PSContentTypeUtils.hasRequiredRule(rules))
      {
         // Remove required rule if exists
         PSContentTypeUtils.removeRequiredRule(rules);
         m_required.setSelection(true);
         m_skipValidation.setSelection(false);
         m_skipValidation.setEnabled(false);
      }
      else if(rules.isEmpty())
      {
        m_skipValidation.setSelection(true);
        m_skipValidation.setEnabled(false);
      }
      else
      {
         if(m_applyWhen != null)
            m_skipValidation.setSelection(!m_applyWhen.ifFieldEmpty());
      }
      m_rulesComp.setRules(rules);
   }


   /*
    * @see org.eclipse.jface.window.Window
    *      #configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      String dialogTitle = PSMessages
            .getString("PSFieldValidationsDialog.dialog.title.fieldvalidation"); //$NON-NLS-1$
      newShell.setText(dialogTitle);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_preRequisitesBtn)
      {
         final AwtSwtModalDialogBridge bridge = new AwtSwtModalDialogBridge(
               getShell());
         SwingUtilities.invokeLater(new Runnable()
         {
            @SuppressWarnings("synthetic-access")//$NON-NLS-1$
            public void run()
            {
               try
               {
                  PSRuleEditorDialog dlg = new PSRuleEditorDialog((Frame)null);
                  if (m_applyWhen != null)
                  {
                     dlg.onEdit(m_applyWhen.iterator());
                  }
                  bridge.registerModalSwingDialog(dlg);
                  dlg.setVisible(true);
                  PSCollection rules = dlg.getRules();
                  if (rules != null)
                  {
                     if (m_applyWhen == null)
                        m_applyWhen = new PSApplyWhen();
                     m_applyWhen.clear();
                     m_applyWhen.addAll(rules);
                  }
               }
               catch (ClassNotFoundException ex)
               {
                  // this should not happen
                  ex.printStackTrace();
               }
            }
         });

      }
      else if(e.getSource() == m_required)
      {
         if(m_required.getSelection())
         {
            m_skipValidation.setSelection(false);
            m_skipValidation.setEnabled(false);
         }
         else
         {
            m_skipValidation.setSelection(true);
            if(m_rulesComp.getRules().isEmpty())
            {
               m_skipValidation.setEnabled(false);
            }
            else
            {
               m_skipValidation.setEnabled(true);
            }
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(@SuppressWarnings("unused") SelectionEvent e)
   {

   }

   /**
    * A conveneint inner class to create the rule details area. Extends from
    * PSFieldRulesComposite class and adds the additional functionality of
    * setting the rule composites for validation rules.
    * 
    * @author bjoginipally
    * 
    */
   private class ValidationRulesComposite extends PSFieldRulesComposite
         implements
            PSFieldRuleDetailsComposites.IRuleFieldDetails
   {

      ValidationRulesComposite(Composite parent, int style, String fieldName,
            String[] fieldNames)
      {
         super(parent, style, SHOW_BOOLEAN_COLUMN);
         mi_fieldName = fieldName;
         mi_fieldNames = fieldNames;
         m_rulesTable.addTableModifiedListener(new IPSTableModifiedListener()
         {
            public void tableModified(@SuppressWarnings("unused") PSTableEvent event)
            {
               if (m_rulesTable.getValues().isEmpty())
               {
                  m_skipValidation.setEnabled(false);
               }
               else
               {
                  if (!m_required.getSelection())
                     m_skipValidation.setEnabled(true);
               }
            }

         });
      }

      @Override
      String getRuleTypeLabel()
      {
         return PSMessages
               .getString("PSFieldValidationsDialog.label.ruletypevalidation"); //$NON-NLS-1$
      }

      @Override
      List<Composite> getRuleTypeComposites()
      {
         List<Composite> ruleComps = new ArrayList<Composite>();
         m_defaultRuleComp = new PSFieldRuleDetailsComposites.NumberRangeRuleComposite(
                     m_ruleDetailsComp, SWT.NONE, null, this);
         ruleComps.add(m_defaultRuleComp);
         ruleComps.add(new PSFieldRuleDetailsComposites.DateRangeRuleComposite(
               m_ruleDetailsComp, SWT.NONE, null, this));
         ruleComps
               .add(new PSFieldRuleDetailsComposites.StringLengthRuleComposite(
                     m_ruleDetailsComp, SWT.NONE, null, this));
         ruleComps.add(new PSFieldRuleDetailsComposites.RegExRuleComposite(
               m_ruleDetailsComp, SWT.NONE, null, this));
         ruleComps.add(new PSFieldRuleDetailsComposites.JEXLRuleComposite(
               m_ruleDetailsComp, SWT.NONE, null, this));
         ruleComps
               .add(new PSFieldRuleDetailsComposites.ConditionalRuleComposite(
                     m_ruleDetailsComp, SWT.NONE, null, this));
         ruleComps.add(new PSFieldRuleDetailsComposites.ExtnRuleComposite(
               m_ruleDetailsComp, SWT.NONE, null, this, 
               IPSFieldValidator.class.getName()));
         return ruleComps;
      }

      @Override
      Composite getDefaultRuleComposite()
      {
         return m_defaultRuleComp;
      }

      public String getFieldName()
      {
         return mi_fieldName;
      }

      public String[] getFieldNames()
      {
         return mi_fieldNames;
      }

      private String[] mi_fieldNames;

      private String mi_fieldName;

      private Composite m_defaultRuleComp;

   }

   // Data
   private PSFieldTableRowDataObject m_rowData;

   // Controls
   private Button m_preRequisitesBtn;

   private Text m_errorMessage;

   private PSApplyWhen m_applyWhen;

   private ValidationRulesComposite m_rulesComp;

   private String[] m_fieldNames;

   private String m_fieldName;
   
   private Button m_required;
   
   private Button m_skipValidation;

}

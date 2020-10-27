/******************************************************************************
 *
 * [ PSTransformationRulesTab.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldTranslation;
import com.percussion.design.objectstore.PSRule;
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.IPSFieldOutputTransformer;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Composite for handling the input and output transformation rules.
 */
public class PSTransformationRulesTab extends Composite
      implements
         IPSUiConstants
{

   /**
    * Ctor
    * 
    * @param parent Parent of this component must not be <code>null</code>.
    * @param style The SWT styles for this composite.
    * @param rowData Object of PSFieldTableRowDataObject representing the field
    *           in the content editor.
    * @param inputTransform The type of transformation this composite is
    *           handling if <code>true</code>, treated as input
    *           transformation otherwise treated as output transformation.
    */
   public PSTransformationRulesTab(Composite parent, int style,
         PSFieldTableRowDataObject rowData, boolean inputTransform)
   {
      super(parent, style);
      if (parent == null)
      {
         throw new IllegalArgumentException("parent must not be null"); //$NON-NLS-1$
      }
      if (rowData == null)
      {
         throw new IllegalArgumentException("fieldData must not be null"); //$NON-NLS-1$
      }
      m_rowData = rowData;
      m_fieldName = m_rowData.getName();
      m_isInputTransform = inputTransform;
      setLayout(new FormLayout());
      Composite mainComp = new Composite(this, SWT.NONE);
      mainComp.setLayout(new FormLayout());
      FormData fd = new FormData();
      fd.top = new FormAttachment(0, 0);
      fd.left = new FormAttachment(0, 0);
      fd.right = new FormAttachment(100, 0);
      mainComp.setLayoutData(fd);

      Composite topComp = new Composite(mainComp, SWT.NONE);
      topComp.setLayout(new FormLayout());
      FormData fd1 = new FormData();
      fd1.top = new FormAttachment(0, 0);
      fd1.left = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(100, 0);
      topComp.setLayoutData(fd1);

      // Transforms table area
      final Label ruleTableLabel = new Label(topComp, SWT.WRAP);
      final FormData fd4 = new FormData();
      fd4.top = new FormAttachment(0, 0);
      fd4.left = new FormAttachment(0, 0);
      fd4.right = new FormAttachment(100, 0);
      ruleTableLabel.setLayoutData(fd4);
      ruleTableLabel.setText(PSMessages.getString("PSTransformationRulesTab.label.tabletitle.transforms")); //$NON-NLS-1$

      m_rulesComp = new TransformationRulesComposite(topComp, SWT.NONE,
            m_fieldName, null, m_isInputTransform);
      final FormData fd5 = new FormData();
      fd5.top = new FormAttachment(ruleTableLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      fd5.left = new FormAttachment(0, 0);
      fd5.right = new FormAttachment(100, 0);
      m_rulesComp.setLayoutData(fd5);
      m_rulesComp.setRuleComposites();

      // Transformation failure message
      final Label transFaillureLabel = new Label(topComp, SWT.WRAP);
      final FormData fd12 = new FormData();
      fd12.top = new FormAttachment(m_rulesComp, LABEL_VSPACE_OFFSET
            + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      fd12.left = new FormAttachment(0, 0);
      transFaillureLabel.setLayoutData(fd12);
      transFaillureLabel.setText(PSMessages.getString("PSTransformationRulesTab.label.transformfailure")); //$NON-NLS-1$

      m_errorMessage = new Text(topComp, SWT.WRAP | SWT.BORDER);
      final FormData fd13 = new FormData();
      fd13.top = new FormAttachment(m_rulesComp, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      fd13.left = new FormAttachment(transFaillureLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      fd13.right = new FormAttachment(100, 0);
      fd13.height = DESCRIPTION_FIELD_HEIGHT / 2;
      m_errorMessage.setLayoutData(fd13);
      loadControlValues();
   }

   /**
    * Get field name
    * 
    * @return field name;
    */
   public String getFieldName()
   {
      return m_fieldName;
   }

   /**
    * Convenient method to update the data.
    * 
    * @return <code>true</code> if succeeds, otherwise shows the error message
    *         and returns <code>false</code>.
    */
   public boolean updateData()
   {
      PSField field = m_rowData.getField();
      PSExtensionCallSet callSet = new PSExtensionCallSet();
      List<PSRule> rules = m_rulesComp.getRules();
      for (PSRule rule : rules)
      {
         PSExtensionCallSet cset = rule.getExtensionRules();
         Iterator iter = cset.iterator();
         while (iter.hasNext())
         {
            callSet.add(iter.next());
         }
      }
      PSFieldTranslation fTransForm = new PSFieldTranslation(callSet);
      fTransForm.setErrorMessage(new PSDisplayText(StringUtils
            .defaultString(m_errorMessage.getText())));

      if (m_isInputTransform)
         field.setInputTranslation(fTransForm);
      else
         field.setOutputTranslation(fTransForm);
      return true;
   }

   /**
    * Convenient method for loading the control values.
    */
   private void loadControlValues()
   {
      PSField field = m_rowData.getField();
      PSFieldTranslation fTransForm = null;
      if (m_isInputTransform)
         fTransForm = field.getInputTranslation();
      else
         fTransForm = field.getOutputTranslation();
      List<PSRule> rules = new ArrayList<PSRule>();
      if (fTransForm != null)
      {
         if (fTransForm.getErrorMessage() != null)
            m_errorMessage.setText(StringUtils.defaultString(fTransForm
                  .getErrorMessage().getText()));

         PSExtensionCallSet callSet = fTransForm.getTranslations();
         Iterator iter = callSet.iterator();
         while (iter.hasNext())
         {
            PSExtensionCall call = (PSExtensionCall) iter.next();
            PSExtensionCallSet cset = new PSExtensionCallSet();
            cset.add(call);
            PSRule rule = new PSRule(cset);
            if (rule != null)
               rules.add(rule);

         }
         if (fTransForm.getErrorMessage() != null)
            m_errorMessage.setText(StringUtils.defaultString(fTransForm
                  .getErrorMessage().getText()));

      }
      m_rulesComp.setRules(rules);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */@Override
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
   
   /**
    * @return the field rules composite. Never <code>null</code> after
    * the ctor is processed.
    */
   public PSFieldRulesComposite getFieldRulesComposite()
   {
      return m_rulesComp;
   }

   private class TransformationRulesComposite extends PSFieldRulesComposite
         implements
            PSFieldRuleDetailsComposites.IRuleFieldDetails
   {

      TransformationRulesComposite(Composite parent, int style,
            String fieldName, String[] fieldNames, boolean isInputtransform)
      {
         super(parent, style, 0);
         mi_fieldName = fieldName;
         mi_fieldNames = fieldNames;
         mi_inputTransform = isInputtransform;
      }

      @Override
      String getRuleTypeLabel()
      {
         return PSMessages.getString("PSTransformationRulesTab.label.ruletypetransform"); //$NON-NLS-1$
      }

      @Override
      List<Composite> getRuleTypeComposites()
      {
         List<Composite> ruleComps = new ArrayList<Composite>();
         String iface;
         if (mi_inputTransform)
         {
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.LowercaseRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.UpperCaseRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.ProperCaseRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));
            ruleComps.add(new PSFieldRuleDetailsComposites.TrimRuleComposite(
                  m_ruleDetailsComp, SWT.NONE, null, this));
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.NormalizeDateRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.ReplaceRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.SetFieldRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.InputMapRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));
            
            iface = IPSFieldInputTransformer.class.getName();
         }
         else
         {
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.OutputMapRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));
            ruleComps
                  .add(new PSFieldRuleDetailsComposites.FormatDateRuleComposite(
                        m_ruleDetailsComp, SWT.NONE, null, this));

            iface = IPSFieldOutputTransformer.class.getName();
         }
          
         m_defaultRuleComp = new PSFieldRuleDetailsComposites.ExtnRuleComposite(
               m_ruleDetailsComp, SWT.NONE, null, this, iface);
         ruleComps.add(m_defaultRuleComp);
         
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

      private boolean mi_inputTransform;

      private Composite m_defaultRuleComp;

   }

   // Data
   private PSFieldTableRowDataObject m_rowData;

   // Controls
   private PSSortableTable m_rulesTable;

   private Text m_errorMessage;

   private String m_fieldName;

   private boolean m_isInputTransform;

   private TransformationRulesComposite m_rulesComp;
}

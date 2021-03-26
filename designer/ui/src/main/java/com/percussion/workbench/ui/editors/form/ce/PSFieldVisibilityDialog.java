/******************************************************************************
 *
 * [ PSFieldVisibilityDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSVisibilityRules;
import com.percussion.extension.IPSFieldEditabilityRule;
import com.percussion.extension.IPSFieldVisibilityRule;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Dialog for creating field visibility rules.
 * @todo - ph: This is no longer a field visibility dialog, it should be renamed 
 * at a minimum.
 */
public class PSFieldVisibilityDialog extends PSDialog implements IPSUiConstants
{

   /**
    * Ctor
    * 
    * @param parentShell Shell of the parent, must not be <code>null</code>.
    * @param rowData Object of PSFieldTableRowDataObject representing the field
    *           row.
    * @param isReadOnlyRule flag to indicate whether this dialog is used for
    *           visibility rules or readonly rules. If <code>true</code>, the
    *           dialog is used for editing read only rules other wise visibility
    *           rules.
    */
   public PSFieldVisibilityDialog(Shell parentShell,
         PSFieldTableRowDataObject rowData, boolean isReadOnlyRule)
   {
      super(parentShell);
      if (rowData == null)
      {
         throw new IllegalArgumentException("fieldData must not be null"); //$NON-NLS-1$
      }
      m_rowData = rowData;
      m_isReadOnlyRule = isReadOnlyRule;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite mainComp = (Composite) super.createDialogArea(parent);
      mainComp.setLayout(new FormLayout());
      Composite topComp = new Composite(mainComp,SWT.NONE);
      topComp.setLayout(new FormLayout());
      FormData fd1 = new FormData();
      fd1.top = new FormAttachment(0,20);
      fd1.left = new FormAttachment(0,10);
      fd1.right = new FormAttachment(100,-10);
      topComp.setLayoutData(fd1);

      //Validation table area
      final Label ruleTableLabel = new Label(topComp, SWT.WRAP);
      final FormData fd4 = new FormData();
      fd4.top = new FormAttachment(0,0);
      fd4.left = new FormAttachment(0,0);
      ruleTableLabel.setLayoutData(fd4);
      String key = m_isReadOnlyRule 
         ? "PSFieldVisibilityDialog.label.tabletitle.readonly" //$NON-NLS-1$ 
         : "PSFieldVisibilityDialog.label.tabletitle.visibility";  //$NON-NLS-1$
      ruleTableLabel.setText(PSMessages.getString(key)); //$NON-NLS-1$

      m_rulesComp = new VisibilityRulesComposite(topComp,SWT.NONE,m_rowData.getName(), null, m_isReadOnlyRule);
      final FormData fd5 = new FormData();
      fd5.top = new FormAttachment(ruleTableLabel, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      fd5.left = new FormAttachment(0,0);
      fd5.right = new FormAttachment(100,0);
      m_rulesComp.setLayoutData(fd5);
      m_rulesComp.setRuleComposites();
      
      //Grid data
      GridData data = new GridData(GridData.FILL_HORIZONTAL
            | GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
      mainComp.setLayoutData(data);
      loadControlValues();
      return mainComp;
   }

   /*
    *  (non-Javadoc)
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
    *  (non-Javadoc)
    * @see org.eclipse.jface.window.Window#getInitialSize()
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(600, 600);
   }

   /* 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      m_rulesComp.checkForRuleChanges();
      if(!updateData())
         return;
      super.okPressed();
   }
   
   /**
    * Convenient method to update the data.
    * @return <code>true</code> if succeeds, otherwise shows the error message
    * and returns <code>false</code>.
    */
   private boolean updateData()
   {
      List<PSRule> rules = m_rulesComp.getRules();
      if(m_isReadOnlyRule)
      {
         PSDisplayMapping dm = m_rowData.getDisplayMapping();
         PSCollection rorules = new PSCollection(PSRule.class);
         rorules.addAll(rules);
         dm.getUISet().setReadOnlyRules(rorules);
      }
      else
      {
         PSField field = m_rowData.getField();
         PSVisibilityRules visrules = new PSVisibilityRules();
         visrules.addAll(rules);
         field.setVisibilityRules(visrules);
      }
      return true;
   }

   /**
    * Convenient method for loading the control values.
    */
   private void loadControlValues()
   {
      List<PSRule> rules = new ArrayList<PSRule>();
      Iterator iter = null;
      if(m_isReadOnlyRule)
      {
         PSDisplayMapping dm = m_rowData.getDisplayMapping();
         iter = dm.getUISet().getReadOnlyRules();
      }
      else
      {
         PSField fld = m_rowData.getField();
         PSVisibilityRules visrules = fld.getVisibilityRules();
         if (visrules != null)
         {
            iter = visrules.iterator();
         }
      }
      while (iter!=null && iter.hasNext())
      {
         PSRule rule = (PSRule) iter.next();
         if (rule != null)
         {
            rules.add(rule);
         }
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
      String key = m_isReadOnlyRule 
         ? "PSFieldVisibilityDialog.dialog.title.readonly" //$NON-NLS-1$ 
         : "PSFieldVisibilityDialog.dialog.title.visibility";  //$NON-NLS-1$
      String dialogTitle = PSMessages.getString(key);
      newShell.setText(dialogTitle);
   }

   
   private class VisibilityRulesComposite extends PSFieldRulesComposite implements PSFieldRuleDetailsComposites.IRuleFieldDetails
   {

      VisibilityRulesComposite(Composite parent, int style, String fieldName,
            String[] fieldNames, boolean isReadOnlyRule)
      {
         super(parent, style, SHOW_BOOLEAN_COLUMN);
         mi_fieldName = fieldName;
         mi_fieldNames = fieldNames;
         mi_isReadOnlyRule = isReadOnlyRule;
      }

      @Override
      String getRuleTypeLabel()
      {
         return PSMessages.getString("PSFieldVisibilityDialog.label.ruletype"); //$NON-NLS-1$
      }

      @Override
      List<Composite> getRuleTypeComposites()
      {
         List<Composite> ruleComps = new ArrayList<Composite>();
         String extensionInterfaceName;
         if(mi_isReadOnlyRule)
         {
            ruleComps
            .add(new PSFieldRuleDetailsComposites.AlwaysReadOnlyRuleComposite(
                  m_ruleDetailsComp, SWT.NONE, null, this));
            extensionInterfaceName = IPSFieldEditabilityRule.class.getName();
         }
         else
         {
            extensionInterfaceName = IPSFieldVisibilityRule.class.getName();
         }
         ruleComps
               .add(new PSFieldRuleDetailsComposites.CreateOnlyRuleComposite(
                     m_ruleDetailsComp, SWT.NONE, null, this));
         ruleComps
               .add(new PSFieldRuleDetailsComposites.ModifyOnlyRuleComposite(
                     m_ruleDetailsComp, SWT.NONE, null, this));
         ruleComps.add(new PSFieldRuleDetailsComposites.JEXLRuleComposite(
               m_ruleDetailsComp, SWT.NONE, null, this));
         ruleComps
               .add(new PSFieldRuleDetailsComposites.ConditionalRuleComposite(
                     m_ruleDetailsComp, SWT.NONE, null, this));
         m_defaultRuleComp = new PSFieldRuleDetailsComposites.ExtnRuleComposite(
               m_ruleDetailsComp, SWT.NONE, null, this, 
               extensionInterfaceName);
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

      private Composite m_defaultRuleComp;
      
      private boolean mi_isReadOnlyRule;

   }
   
   //Data
   private PSFieldTableRowDataObject m_rowData;
   //Controls
   private VisibilityRulesComposite m_rulesComp;

   /**
    * Flag to indicate whether the this dialog class is used for visibility
    * rules or read only rules.
    */
   private boolean m_isReadOnlyRule;
}

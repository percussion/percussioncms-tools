/******************************************************************************
 *
 * [ PSFieldRulesComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSRule;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.form.ce.PSFieldRuleDetailsComposites.IRuleComposite;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
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
import org.eclipse.swt.widgets.TableItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract class to render field rules. The users of the derived classes
 * should call setRuleTypeComposites() method after constrcutiong the objects.
 */
public abstract class PSFieldRulesComposite extends Composite
      implements
         IPSUiConstants,
         SelectionListener,
         ISelectionChangedListener
{
   PSFieldRulesComposite(Composite parent, int style, int options)
   {
      super(parent, style);
      m_options = options;
      setLayout(new FormLayout());
      createControls();
   }

   /**
    * The label of the ryle types combobox is set by the name returned by this
    * method.
    * 
    * @return String label if <code>null</code> or empty no label is shown.
    */
   abstract String getRuleTypeLabel();

   /**
    * Displays the rule type composites based on the list supplied. These
    * composites must implement PSFieldRuleDetailsComposites.IRuleComposite
    * otherwise IllegalArgumentException is thrown from the setRuleComposites()
    * method. The created composites should have m_
    * 
    * @return List of different rule Composite objects that
    */
   abstract List<Composite> getRuleTypeComposites();

   /**
    * The composite returned by this method is set as the default rule type. If
    * <code>null</code> or does not exist in the composites returned by
    * getRuleTypeComposites() method then the default rule composite is set to
    * the first one in the list.
    */
   abstract Composite getDefaultRuleComposite();

   /**
    * A convenient method to create
    * 
    */
   private void createControls()
   {
      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         public String getColumnText(Object element, int columnIndex)
         {
            RuleRow rule = (RuleRow) element;
            switch (columnIndex)
            {
               case 0 :
                  return StringUtils.defaultString(rule.getRuleText());
               case 1 :
                  return StringUtils.defaultString(rule.getOperator());
            }
            return ""; // should never get here //$NON-NLS-1$
         }
      };
      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
      {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public Object newInstance()
         {
            RuleRow newRow = new RuleRow();
            setRuleData(null);
            m_applyButton.setVisible(false);
            m_addButton.setVisible(true);            
            return newRow;
         }

         public boolean isEmpty(Object obj)
         {
            if (!(obj instanceof RuleRow))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of RuleRow."); //$NON-NLS-1$
            RuleRow rule = (RuleRow) obj;
            return rule.isEmpty();
         }
      };

      m_rulesTable = new RulesSortableTable(this, labelProvider, newRowProvider,
            SWT.NONE, PSSortableTable.SHOW_ALL | PSSortableTable.INSERT_ALLOWED
                  | PSSortableTable.DELETE_ALLOWED);
      m_rulesTable.setCellModifier(new CellModifier(m_rulesTable));
      final FormData fd5 = new FormData();
      fd5.left = new FormAttachment(0, 0);
      fd5.right = new FormAttachment(100, 0);
      fd5.top = new FormAttachment(0, 0);
      fd5.height = 2 * DESCRIPTION_FIELD_HEIGHT;
      m_rulesTable.setLayoutData(fd5);

      TextCellEditor ceditor = new TextCellEditor(m_rulesTable, SWT.READ_ONLY);
      m_rulesTable.addColumn(PSMessages.getString("PSFieldRulesComposite.label.column.rule"), PSSortableTable.NONE, //$NON-NLS-1$
            new ColumnWeightData(6, 100), ceditor, SWT.LEFT);

      PSComboBoxCellEditor beditor = new PSComboBoxCellEditor(m_rulesTable,
            ms_operator);
      if ((m_options & SHOW_BOOLEAN_COLUMN) != 0)
         m_rulesTable.addColumn(PSMessages.getString("PSFieldRulesComposite.label.column.boolean"), PSSortableTable.NONE, //$NON-NLS-1$
               new ColumnWeightData(1, 20), beditor, SWT.LEFT);

      m_rulesTable.addSelectionListener(this);
      m_rulesTable.getDeleteButton().addSelectionListener(this);

      // Rule details area
      final Label ruleDetailsLabel = new Label(this, SWT.WRAP);
      final FormData fd6 = new FormData();
      fd6.top = new FormAttachment(m_rulesTable, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      fd6.left = new FormAttachment(0, 0);
      ruleDetailsLabel.setLayoutData(fd6);
      ruleDetailsLabel.setText(PSMessages.getString("PSFieldRulesComposite.label.ruledetails")); //$NON-NLS-1$

      m_ruleDetailsComp = new Composite(this, SWT.BORDER);
      final FormData fd7 = new FormData();
      fd7.top = new FormAttachment(ruleDetailsLabel,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      fd7.left = new FormAttachment(0, 0);
      fd7.right = new FormAttachment(100, 0);
      fd7.height = 200;
      m_ruleDetailsComp.setLayout(new FormLayout());
      m_ruleDetailsComp.setLayoutData(fd7);

      m_ruleTypeLabel = new Label(m_ruleDetailsComp, SWT.WRAP | SWT.RIGHT);
      final FormData fd8 = new FormData();
      fd8.top = new FormAttachment(0, LABEL_VSPACE_OFFSET);
      fd8.right = new FormAttachment(20, 0);
      m_ruleTypeLabel.setLayoutData(fd8);

      m_ruleTypeCombo = new ComboViewer(m_ruleDetailsComp, SWT.BORDER
            | SWT.READ_ONLY);

      Combo rcombo = m_ruleTypeCombo.getCombo();
      final FormData fd9 = new FormData();
      fd9.top = new FormAttachment(m_ruleTypeLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      fd9.left = new FormAttachment(m_ruleTypeLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      rcombo.setLayoutData(fd9);

      m_ruleTypeCombo.setContentProvider(new PSDefaultContentProvider());
      m_ruleTypeCombo.setLabelProvider(new PSAbstractLabelProvider()
      {

         public String getText(Object element)
         {
            if (element instanceof IRuleComposite)
               return ((IRuleComposite) element).getDisplayName();
            else
               return ""; //$NON-NLS-1$
         }
      });

      m_ruleTypeCombo.addSelectionChangedListener(this);

      m_addButton = new Button(m_ruleDetailsComp, SWT.NONE);
      final FormData fd11 = new FormData();
      fd11.bottom = new FormAttachment(100, -BUTTON_VSPACE_OFFSET);
      fd11.right = new FormAttachment(100, -BUTTON_VSPACE_OFFSET);
      fd11.width = BUTTON_WIDTH;
      m_addButton.setLayoutData(fd11);
      m_addButton.setText(PSMessages.getString("PSFieldRulesComposite.label.button.add")); //$NON-NLS-1$
      m_addButton.addSelectionListener(this);

      m_applyButton = new Button(m_ruleDetailsComp, SWT.NONE);
      m_applyButton.setLayoutData(fd11);
      m_applyButton.setText(PSMessages.getString("PSFieldRulesComposite.label.button.apply")); //$NON-NLS-1$
      m_applyButton.setVisible(false);
      m_applyButton.addSelectionListener(this);

      m_jexlEditorButton = new Button(m_ruleDetailsComp, SWT.NONE);
      final FormData fd11a = new FormData();
      fd11a.bottom = new FormAttachment(m_addButton, -BUTTON_VSPACE_OFFSET,
            SWT.TOP);
      fd11a.right = new FormAttachment(100, -BUTTON_VSPACE_OFFSET);
      fd11a.width = BUTTON_WIDTH;
      m_jexlEditorButton.setLayoutData(fd11a);
      m_jexlEditorButton.setText(PSMessages.getString("PSFieldRulesComposite.label.button.editor")); //$NON-NLS-1$
      m_jexlEditorButton.setVisible(false);
      m_jexlEditorButton.addSelectionListener(this);
      m_ruleTypeLabel.setText(StringUtils.defaultString(getRuleTypeLabel()));
   }

   /**
    * Returns the list of rules selected.
    * 
    * @return List of selected rules may be empty but never <code>null</code>.
    */
   public @SuppressWarnings("unchecked") //$NON-NLS-1$
   List<PSRule> getRules()
   {
      List<PSRule> rules = new ArrayList<PSRule>();
      List<RuleRow> rows = m_rulesTable.getValues();
      for (RuleRow row : rows)
      {
         if (!row.isEmpty())
         {
            rules.add(row.mi_rule);
         }
      }
      return rules;
   }

   /**
    * Clears the existsing rules data and sets the supplied rules.
    */
   public void setRules(List<PSRule> rules)
   {
      List<RuleRow> input = new ArrayList<RuleRow>();
      for (PSRule rule : rules)
      {
         input.add(new RuleRow(rule));
      }
      m_rulesTable.setValues(input);
      m_rulesTable.getTable().select(input.size());
      m_rulesTable.refreshTable();
   }

   /**
    * Sets the rule details composites.
    */
   public void setRuleComposites()
   {
      List<Composite> comps = getRuleTypeComposites();
      for (Composite comp : comps)
      {
         if (!(comp instanceof IRuleComposite))
            throw new IllegalArgumentException(
                  "All composites must implement IRuleComposite interface"); //$NON-NLS-1$
      }
      m_ruleTypeCombo.setInput(comps);
      final FormData fd = new FormData();
      fd.top = new FormAttachment(m_ruleTypeLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      fd.bottom = new FormAttachment(100, 0);
      fd.right = new FormAttachment(75, 0);
      fd.left = new FormAttachment(0, 0);
      for (Composite comp : comps)
      {
         comp.setLayoutData(fd);
      }
      showComposite(getDefaultRuleComposite());
   }
   
   /**
    * Checks to see if the current rule composite is dirty and
    * if so then prompts the user as to whether they want to 
    * save these changes or not. This should be called when ok 
    * is pressed on the enclosing dialog or when selecting another
    * row in the rule table.
    */
   public void checkForRuleChanges()
   {
      if(isLastSelectedRuleRowDirty())
      {
         boolean isApply = m_applyButton.getVisible();
         String applyMsg = "There are rule changes that have not yet been applied.\nDo you want to apply the rule changes?";
         String addMsg = "There are new rule changes that have not yet been added.\nDo you want to add the new rule changes?";
         boolean response = 
            MessageDialog.openQuestion(getShell(), "Rule Changes Exists",
               isApply ? applyMsg : addMsg);
         if(response)
         {
            if(!isApply)
               onAdd(m_lastSelectedRow);
            else
               onApply(m_lastSelectedRow);
         }
      }
   }

   /**
    * 
    * 
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void clearCompositesData()
   {
      List<Composite> rc = (List<Composite>) m_ruleTypeCombo.getInput();
      for (Composite c : rc)
      {
         ((IRuleComposite) c).clearData();
      }
   }

   /**
    * A conveneint method to show the composite at the supplied index and hide
    * the rest. Hides all if the index is out of range.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void showComposite(int index)
   {
      List<Composite> ruleComps = (List<Composite>) m_ruleTypeCombo.getInput();
      Composite comp = null;
      if (index >= 0 && index < ruleComps.size())
         comp = ruleComps.get(index);
      showComposite(comp);
   }

   /**
    * A conveneint method to show the supplied composite and hide the rest.
    * Hides all composites if <code>null</code> or does not exist in the rule
    * types combo
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void showComposite(Composite comp)
   {
      List<Composite> ruleComps = (List<Composite>) m_ruleTypeCombo.getInput();
      for (Composite c : ruleComps)
         c.setVisible(false);
      if (comp != null && ruleComps.indexOf(comp) != -1)
      {
         comp.setVisible(true);
         m_ruleTypeCombo.getCombo().select(ruleComps.indexOf(comp));
      }
   }

   /**
    * Gets the rule from the composite
    * 
    * @return PSRule may be <code>null</code>, incase of <code>null</code>
    *         the IRuleComposite has to show appropriate error message.
    */
   private PSRule getRuleData()
   {
      IStructuredSelection sel = (IStructuredSelection) m_ruleTypeCombo
            .getSelection();
      IRuleComposite comp = (IRuleComposite) sel.getFirstElement();
      return comp.getRuleData();
   }
   
   /**
    * Determines if the current rule composite is dirty.
    * @return <code>true</code> if the rule composite is dirty.
    */
   private boolean isLastSelectedRuleRowDirty()
   {
      IStructuredSelection sel = (IStructuredSelection) m_ruleTypeCombo
      .getSelection();
      IRuleComposite comp = (IRuleComposite) sel.getFirstElement();
      if(comp == null)
         return false;
      // Clear the rule error occurred flag
      PSFieldRuleDetailsComposites.clearErrorOccurredFlag();
      //Suppress error dialogs from popping up
      PSFieldRuleDetailsComposites.setSuppressErrors(true);
      // Attempt to create the rule from the composite
      PSRule compRule = comp.getRuleData();
      // Turn on error dialogs
      PSFieldRuleDetailsComposites.setSuppressErrors(false);
      boolean errorOccurred = PSFieldRuleDetailsComposites.ruleErrorOccurred();
      // Be a good citizen and reset the error occurred flag
      PSFieldRuleDetailsComposites.clearErrorOccurredFlag();
      // If an error occurred then there is no point asking to save the
      // invalid rule so return false
      if(errorOccurred)
         return false;
      PSRule lastRule = null;
      RuleRow lastRow = getRuleRow(m_lastSelectedRowIndex);
      if(lastRow != null)
         lastRule = lastRow.mi_rule;

      if(comp != null && compRule != null && m_addButton.getVisible() 
         && lastRule == null || 
            (lastRule != null && !lastRule.equals(compRule)))
      {
         return true;
      }         
      return false;
   }
   
   /**
    * Sets the rules composites data.
    * 
    * @param rule object of PSRule, may be <code>null</code>.
    */
   public void setRuleData(PSRule rule)
   {
      clearCompositesData();
      m_ruleDetailsComp.setVisible(true);
      if (rule == null)
      {
         Composite comp = getDefaultRuleComposite();
         ((IRuleComposite) comp).setRuleData(null);
         showComposite(comp);
         return;
      }

      Composite comp = getRuleComposite(rule);
      if (comp == null)
      {
         // This should not happen but incase happens
         MessageDialog
               .openInformation(getShell(), PSMessages.getString("PSFieldRulesComposite.error.title.noruledetails"), //$NON-NLS-1$
                     PSMessages.getString("PSFieldRulesComposite.error.msg.noruledetails")); //$NON-NLS-1$
         m_ruleDetailsComp.setVisible(false);
      }
      else
      {
         showComposite(comp);
         ((IRuleComposite) comp).setRuleData(rule);
      }
   }

   private Composite getRuleComposite(PSRule rule)
   {
      Composite comp = null;
      if (rule.isExtensionSetRule())
      {
         // Find the composite
         PSExtensionCallSet callSet = rule.getExtensionRules();
         Iterator iter = callSet.iterator();
         if (iter.hasNext())
         {
            PSExtensionCall call = (PSExtensionCall) iter.next();
            String extName = call.getExtensionRef().getExtensionName();
            comp = getCompositeByExtensionName(extName);
         }
         // If comp is null find the Extension composite
         if (comp == null)
         {
            comp = getCompositeByExtensionName(PSFieldRuleDetailsComposites.RULE_EXTENSION);
         }
      }
      else
      {
         PSCollection conds = rule.getConditionalRulesCollection();
         if (isCreateOnlyConditionalRule(conds))
            comp = getCompositeByExtensionName(PSFieldRuleDetailsComposites.RULE_CREATE_ONLY);
         else if (isModifyOnlyConditionalRule(conds))
            comp = getCompositeByExtensionName(PSFieldRuleDetailsComposites.RULE_MODIFY_ONLY);
         else if(isAlwaysReadOnlyConditionalRule(conds))
            comp = getCompositeByExtensionName(PSFieldRuleDetailsComposites.ALWAYS_RULE_READ_ONLY);
         else
            comp = getCompositeByExtensionName(PSFieldRuleDetailsComposites.RULE_CONDITIONAL);
      }
      return comp;
   }

   /**
    * Convenient method to check whether the supplied collection of conditionals
    * yields to Create Only rule or not.
    * 
    * @param conds PSCollection of conditionals.
    * @return <code>true</code> if it is Modify only rule otherwise
    *         <code>false</code>.
    */
   private boolean isCreateOnlyConditionalRule(PSCollection conds)
   {
      if (!checkCommonFactors(conds))
         return false;
      String op = StringUtils.defaultString(((PSConditional) conds.get(0))
            .getOperator());
      if (!op.equals(PSConditional.OPTYPE_ISNULL))
         return false;
      return true;
   }

   /**
    * Convenient method to check whether the supplied collection of conditionals
    * yields to Modify Only rule or not.
    * 
    * @param conds PSCollection of conditionasl.
    * @return <code>true</code> if it is Modify only rule otherwise
    *         <code>false</code>.
    */
   private boolean isModifyOnlyConditionalRule(PSCollection conds)
   {
      if (!checkCommonFactors(conds))
         return false;
      String op = StringUtils.defaultString(((PSConditional) conds.get(0))
            .getOperator());
      if (!op.equals(PSConditional.OPTYPE_ISNOTNULL))
         return false;
      return true;

   }
   
   /**
    * Convenient method to check whether the supplied collection of conditionals
    * yields to Always Read Only rule or not.
    * 
    * @param conds PSCollection of conditionals.
    * @return <code>true</code> if it is Always Read only rule otherwise
    *         <code>false</code>.
    */
   private boolean isAlwaysReadOnlyConditionalRule(PSCollection conds)
   {
      if (conds.size() != 1)
         return false;
      PSConditional cond = (PSConditional) conds.get(0);
      String variable = cond.getVariable() == null ? "" : StringUtils
            .defaultString(cond.getVariable().getValueText());
      String value = cond.getValue() == null ? "" : StringUtils
            .defaultString(cond.getValue().getValueText());
      String op = StringUtils.defaultString(((PSConditional) conds.get(0))
            .getOperator());
      if (variable.equals("1") && value.equals("1")
            && op.equals(PSConditional.OPTYPE_EQUALS))
         return true;

      return false;

   }

   /**
    * Checks the common factors for Create or Modify only conditionals. Checks
    * whether the name is equal to current field name and value is empty or not.
    * If yes then returns <code>true</code>, otherwise <code>false</code>.
    * 
    * @param conds PSCollection Object of conditionals to check.
    * @return <code>true<code> if check succeeds otherwise <code>false</code>.
    */
   private boolean checkCommonFactors(PSCollection conds)
   {
      if (conds.size() != 1)
         return false;
      boolean result = false;
      PSConditional cond = (PSConditional) conds.get(0);
      String variable = cond.getVariable() == null ? "" : StringUtils //$NON-NLS-1$
            .defaultString(cond.getVariable().getValueText());
      String value = cond.getValue() == null ? "" : StringUtils //$NON-NLS-1$
            .defaultString(cond.getValue().getValueText());
      if (variable.equals(PSFieldRuleDetailsComposites.SYS_CONTENTID)
            && StringUtils.isEmpty(value))
         result = true;
      return result;
   }

   /**
    * Convenient method to get the composite by given extension name
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private Composite getCompositeByExtensionName(String extName)
   {
      Composite comp = null;
      if (StringUtils.isEmpty(extName))
         return comp;
      List<Composite> comps = (List<Composite>) m_ruleTypeCombo.getInput();
      // Find the composite
      for (int i = 0; i < comps.size(); i++)
      {
         Composite c = comps.get(i);
         if (((IRuleComposite) c).getExtensionName().equalsIgnoreCase(extName))
         {
            comp = c;
            break;
         }
      }
      return comp;
   }

   /**
    * Cell modifier for the choice list table
    */
   class CellModifier implements ICellModifier
   {

      CellModifier(PSSortableTable comp)
      {
         mi_tableComp = comp;
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(
       *      java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public boolean canModify(@SuppressWarnings("unused") Object element, String property)
      {
         if (m_rulesTable.getColumnIndex(property) == 0)
            return false;
         return true;
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       *      java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public Object getValue(Object element, String property)
      {
         int col = m_rulesTable.getColumnIndex(property);
         RuleRow rule = (RuleRow) element;
         if (rule.isEmpty())
            return ""; //$NON-NLS-1$
         switch (col)
         {
            case 0 :
               return rule.getRuleText();
            case 1 :
               return rule.getOperator();
         }
         return ""; //$NON-NLS-1$
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#modify( java.lang.Object,
       *      java.lang.String, java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public void modify(Object element, String property, Object value)
      {
         int col = m_rulesTable.getColumnIndex(property);
         TableItem item = (TableItem) element;
         RuleRow rule = (RuleRow) item.getData();
         if (rule.isEmpty())
            return;
         String val = (String) value;
         switch (col)
         {
            case 0 :
               break;
            case 1 :
               rule.setOperator(val);
               break;
         }
         mi_tableComp.refreshTable();
      }

      private PSSortableTable mi_tableComp;

   }

   /**
    * Convenient class to hold the Rules table row data
    */
   private class RuleRow
   {
      RuleRow()
      {
         mi_rule = null;
      }

      RuleRow(PSRule rule)
      {
         mi_rule = rule;
      }

      RuleRow(PSExtensionCall ext)
      {
         PSExtensionCallSet extset = new PSExtensionCallSet();
         extset.add(ext);
         mi_rule = new PSRule(extset);
      }

      RuleRow(PSExtensionCallSet extset)
      {
         mi_rule = new PSRule(extset);
      }

      RuleRow(PSCollection cndRules)
      {
         mi_rule = new PSRule(cndRules);
      }

      public void setRule(PSRule rule)
      {
         mi_rule = rule;
      }

      public boolean isEmpty()
      {
         return mi_rule == null ? true : false;
      }

      public String getOperator()
      {
         if (isEmpty())
            return ""; //$NON-NLS-1$
         if (mi_rule.getOperator() == PSRule.BOOLEAN_AND)
            return STRING_AND;
         else
            return STRING_OR;
      }

      public void setOperator(String op)
      {
         if (isEmpty())
            return;
         if (StringUtils.isEmpty(op) || !op.equals(STRING_OR))
            mi_rule.setOperator(PSRule.BOOLEAN_AND);
         else
            mi_rule.setOperator(PSRule.BOOLEAN_OR);
      }

      public String getRuleText()
      {
         if (isEmpty())
            return ""; //$NON-NLS-1$
         Composite comp = getRuleComposite(mi_rule);
         String ruleText = ""; //$NON-NLS-1$
         if (comp == null)
         {
            //This should not happen as if we can't find a specific rule
            //composite we will get either extension rule composite
            //or condtional rule composite.
            //In case happens.
            ruleText = PSMessages.getString("PSFieldRulesComposite.error.nameforrule"); //$NON-NLS-1$
         }
         else
         {
            ruleText = ((IRuleComposite) comp).getRuleDisplayName(mi_rule);
         }

         return ruleText;
      }

      public String getErrorMessage()
      {
         if (isEmpty())
            return ""; //$NON-NLS-1$
         return StringUtils.defaultString(mi_rule.getErrorLabel().getText());
      }

      public void setErrorMessage(String errLabel)
      {
         if (!isEmpty())
            mi_rule.setErrorLabel(new PSDisplayText(errLabel));
      }

      public PSRule mi_rule;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
    */
   public void selectionChanged(SelectionChangedEvent event)
   {
      if (event.getSource() == m_ruleTypeCombo)
      {
         showComposite(m_ruleTypeCombo.getCombo().getSelectionIndex());
      }
   }
   
   /**
    * Helper method to retrieve the currently selected rule
    * row in the rules table.
    * @return the selected rule row or <code>null</code>.
    */
   private RuleRow getCurrentRuleRow()
   {
      RuleRow rr = null;
      IStructuredSelection sel = (IStructuredSelection) m_rulesTable
      .getSelection();
      if(sel != null && !sel.isEmpty())
         rr = (RuleRow)sel.getFirstElement();
      return rr;
   }
   
   private RuleRow getRuleRow(int index)
   {
      if(index < 0)
         return null;
      List values = m_rulesTable.getAllValues();
      if(index > values.size() - 1)
         return null;
      return (RuleRow)values.get(index);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_rulesTable)
      {
         checkForRuleChanges();
         IStructuredSelection sel = (IStructuredSelection) m_rulesTable
               .getSelection();
         if (sel.size() > 1)
         {
            m_ruleDetailsComp.setVisible(false);
            return;
         }
         else
         {
            m_ruleDetailsComp.setVisible(true);
            RuleRow ruleRow = (RuleRow) sel.getFirstElement();
            if (ruleRow == null || ruleRow.mi_rule == null)
            {
               m_addButton.setVisible(true);
               m_applyButton.setVisible(false);
               setRuleData(null);
            }
            else
            {
               m_addButton.setVisible(false);
               m_applyButton.setVisible(true);
               setRuleData(ruleRow.mi_rule);
            }
            m_lastSelectedRow = getCurrentRuleRow();
            m_lastSelectedRowIndex = m_rulesTable.getTable().getSelectionIndex();
         }
      }
      else if (e.getSource() == m_rulesTable.getDeleteButton())
      {
         m_ruleDetailsComp.setVisible(false);
      }
      else if (e.getSource() == m_applyButton)
      {
         onApply(getCurrentRuleRow());
      }
      else if (e.getSource() == m_addButton)
      {
         onAdd(getCurrentRuleRow());
      }
      else if (e.getSource() == m_jexlEditorButton)
      {
         // TODO open JEXL editor
         MessageDialog.openInformation(getShell(), "Coming Soon", //$NON-NLS-1$
               "JEXL editor is coming soon"); //$NON-NLS-1$
      }

   }
   
   @SuppressWarnings("unchecked")
   protected void onAdd(RuleRow row)
   {
      PSRule rule = getRuleData();
      // If null do nothing, getRuleData() of the composite is supposed
      // to show the appropriate error message.
      if (rule != null)
      {
         List<RuleRow> rules = m_rulesTable.getAllValues();
         if (row == null)
         {
            rules.add(new RuleRow(rule));
            m_rulesTable.getTable().select(rules.size());
         }
         else
         {
           row.setRule(rule);
         }
         rules = m_rulesTable.getAllValues();
         m_rulesTable.setValues(rules);
         m_rulesTable.refreshTable();
         m_rulesTable.fireTableModifiedEvent();
         if (row == null)
         {
            m_rulesTable.getTable().select(rules.size() - 1);
         }
         m_addButton.setVisible(false);
         m_applyButton.setVisible(true);
         m_lastSelectedRowIndex = m_rulesTable.getTable().getSelectionIndex();
      }
   }
   
   @SuppressWarnings("unchecked")
   protected void onApply(RuleRow row)
   {
      PSRule rule = getRuleData();
      // If null do nothing, getRuleData() of the composite is supposed
      // to show the appropriate error message.
      if (rule != null && row != null)
      {
         row.setRule(rule);
         List<RuleRow> rules = m_rulesTable.getAllValues();
         m_rulesTable.setValues(rules);
         m_rulesTable.refreshTable();
         
      }   
   }

   /**
    * Extended sortable table to add additional functionality of firing the
    * table modified event incase of adding the new rules from another
    * composite.
    */
   public class RulesSortableTable extends PSSortableTable
   {
      public RulesSortableTable(Composite parent,
            ITableLabelProvider labelProvider,
            IPSNewRowObjectProvider rowObjectProvider, int style, int options)
      {
         super(parent, labelProvider, rowObjectProvider, style, options);
      }

      /**
       * Fires the super class table modified event.
       */
      @Override
      protected void fireTableModifiedEvent()
      {
         super.fireTableModifiedEvent();
      }

      /* @see com.percussion.workbench.ui.controls.PSSortableTable#
       * preInsert()
       */
      @Override
      protected boolean preInsert()
      {
         checkForRuleChanges();
         return true;
      }

      /* @see com.percussion.workbench.ui.controls.PSSortableTable#preDelete()
       */
      @Override
      protected boolean preDelete()
      {
         m_lastSelectedRow = null;
         m_lastSelectedRowIndex = -1;
         return true;
      }

      /* @see com.percussion.workbench.ui.controls.PSSortableTable#
       * postInsert(java.lang.Object)
       */
      @Override
      protected void postInsert(Object newObject)
      {
         m_lastSelectedRow = (RuleRow)newObject;
         m_lastSelectedRowIndex = m_rulesTable.getTable().getSelectionIndex();
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

   protected RulesSortableTable m_rulesTable;

   // Constants
   private static final String STRING_AND = "AND"; //$NON-NLS-1$

   private static final String STRING_OR = "OR"; //$NON-NLS-1$

   private static final String[] ms_operator =
   {STRING_AND, STRING_OR};

   /**
    * Combo box control to represent the types of rules.
    */
   private ComboViewer m_ruleTypeCombo;

   /**
    * Button for adding the new rules.
    */
   private Button m_addButton;

   /**
    * Button for applying changes made to an existsing rule.
    */
   private Button m_applyButton;

   /**
    * Opens JEXL editor. Visible only if the rule type is RULE_JEXL.
    */
   private Button m_jexlEditorButton;

   private Label m_ruleTypeLabel;

   protected Composite m_ruleDetailsComp;

   /**
    * Option to show boolean column in the rules table
    */
   public static final int SHOW_BOOLEAN_COLUMN = 1 << 1;

   private int m_options;
   
   /**
    * Holds the last selected rule table row. May be<code>null</code>
    */
   private RuleRow m_lastSelectedRow;
   
   /**
    * Holds the index of the last selected rule table row. -1 if no selection.
    */
   private int m_lastSelectedRowIndex = -1;

}

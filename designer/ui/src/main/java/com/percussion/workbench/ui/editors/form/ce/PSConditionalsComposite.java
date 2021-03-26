/******************************************************************************
 *
 * [ PSConditionalsComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSRule;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.controls.PSValueSelectorCellEditor;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PSConditionalsComposite extends Composite implements IPSUiConstants
{

   public PSConditionalsComposite(Composite parent, int style)
   {
      super(parent, style);
      setLayout(new FormLayout());
      createControls();
   }

   private void createControls()
   {
      Composite mainComp = new Composite(this,SWT.NONE);
      mainComp.setLayout(new FormLayout());
      FormData fd = new FormData();
      fd.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      fd.top = new FormAttachment(0, 0);
      fd.right = new FormAttachment(100, 0);
      fd.bottom = new FormAttachment(100, 0);
      mainComp.setLayoutData(fd);
      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         @SuppressWarnings("synthetic-access")
         public String getColumnText(Object element, int columnIndex)
         {
            CondRow rowData = (CondRow) element;
            switch (columnIndex)
            {
               case 0:
                  return rowData.variable == null ? "" : rowData.variable
                        .getValueDisplayText();
               case 1:
                  return StringUtils.defaultString(rowData.op);
               case 2:
                  return rowData.value == null ? "" : rowData.value
                        .getValueDisplayText();
               case 3:
                  return StringUtils.defaultString(rowData.bool);
            }
            return ""; // should never get here
         }
      };

      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
      {
         @SuppressWarnings("synthetic-access")
         public Object newInstance()
         {
            CondRow newRow = new CondRow();
            return newRow;
         }

         public boolean isEmpty(Object obj)
         {
            if (!(obj instanceof CondRow))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of CondRow.");
            CondRow cond = (CondRow) obj;
            return cond.isEmpty();
         }
      };

      m_condTable = new PSSortableTable(mainComp, labelProvider, newRowProvider,
            SWT.NONE, PSSortableTable.DELETE_ALLOWED
                  | PSSortableTable.INSERT_ALLOWED);
      FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 0);
      fd1.top = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(100, 0);
      fd1.bottom = new FormAttachment(100, 0);
      m_condTable.setLayoutData(fd1);
      
      final PSValueSelectorCellEditor varCellEditor =
         new PSValueSelectorCellEditor(m_condTable.getTable(),  
               false);
      m_condTable.addColumn(COL_VARIABLE, PSSortableTable.NONE,
         new ColumnWeightData(4,80, true), varCellEditor, SWT.LEFT);      
      
      final ComboBoxCellEditor opCellEditor = new ComboBoxCellEditor(
            m_condTable.getTable(), m_opOptions, SWT.READ_ONLY);
      m_condTable.addColumn(COL_OP, PSSortableTable.NONE,
            new ColumnWeightData(2,25, true), opCellEditor, SWT.LEFT);      

      final PSValueSelectorCellEditor valCellEditor = 
         new PSValueSelectorCellEditor(m_condTable.getTable(), false);

      m_condTable.addColumn(COL_VALUE, PSSortableTable.NONE,
         new ColumnWeightData(4,80, true), valCellEditor, SWT.LEFT);      

      final ComboBoxCellEditor boolCellEditor = new ComboBoxCellEditor(
            m_condTable.getTable(), m_boolOptions, SWT.READ_ONLY);
      m_condTable.addColumn(COL_BOOL, PSSortableTable.NONE,
            new ColumnWeightData(2,25, true), boolCellEditor, SWT.LEFT);      
      m_condTable.setCellModifier(new CellModifier(m_condTable));
   }
   
   /**
    * Sets the conditional rules data for this composite
    *
    */
   public void setRuleData(PSRule rule)
   {
      m_rule = rule;
      m_condData = new ArrayList<CondRow>();
      if(m_rule!=null)
      {
         Iterator iter = m_rule.getConditionalRules();
         while(iter.hasNext())
         {
            PSConditional cond = (PSConditional) iter.next();
            m_condData.add(new CondRow(cond));
         }
      }
      m_condTable.setValues(m_condData);
      m_condTable.refreshTable();
   }
   
   /**
    * Clears the conditionals table
    */
   protected void clearConditionals()
   {
      m_condTable.setValues(new ArrayList());
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
       * java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("unused")   //for both params
      public boolean canModify(Object element, String property)
      {
         return true;
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       * java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public Object getValue(Object element, String property)
      {
         int col = m_condTable.getColumnIndex(property);
         CondRow cond = (CondRow)element;
         switch (col)
         {
            case 0:
               return cond.variable;
            case 1:
               return PSContentEditorDefinition.getItemIndex(m_opOptions,
                     StringUtils.defaultString(cond.op));
            case 2:
               return cond.value;
            case 3:
               return PSContentEditorDefinition.getItemIndex(m_boolOptions,
                     StringUtils.defaultString(cond.bool));
         }
         return "";
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#modify(
       * java.lang.Object, java.lang.String, java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public void modify(Object element, String property, Object value)
      {
         if(value == null)
            return;
         int col = m_condTable.getColumnIndex(property);
         TableItem item = (TableItem)element;
         CondRow cond = (CondRow)item.getData();
         switch(col)
         {
            case 0:
               cond.variable = (IPSReplacementValue)value;
               break;
            case 1:               
               Integer opt = (Integer)value;
               if(opt >= 0 && opt < m_opOptions.length)
                  cond.op = m_opOptions[opt];                  
               break;
            case 2:
               cond.value = (IPSReplacementValue)value;
               break;
            case 3:
               Integer bl = (Integer)value;
               if(bl >= 0 && bl < m_boolOptions.length)
                  cond.bool = m_boolOptions[bl];
               break;
         }
         mi_tableComp.refreshTable();
      }
      private PSSortableTable mi_tableComp;
   }
   
   /**
    * Simple wrapper class for the object represented in each row of the 
    * conditional table.
    */
   private class CondRow
   {
      CondRow()
      {
         
      }
      
      CondRow(PSConditional cond)
      {
         if (cond == null)
         {
            throw new IllegalArgumentException("cond must not be null");
         }
         variable = cond.getVariable();
         value = cond.getValue();
         op = cond.getOperator();
         bool = cond.getBoolean();
      }

      public boolean isEmpty()
      {
         return (StringUtils.isEmpty(variable == null ? "" : variable
               .getValueDisplayText())
               && StringUtils.isEmpty(value == null ? "" : value
                     .getValueDisplayText()));
      }

      private boolean isValidCondition()
      {
         boolean valid = true;
         if (variable != null)
         {
            if (StringUtils.isBlank(op))
            {
               valid = false;
               MessageDialog
                     .openError(getShell(), "Condional validation Error",
                           "Clause is missing operator");
            }
            else if (value == null
                  && !PSConditional.isUnaryOp(op))
            {
               valid = false;
               MessageDialog.openError(getShell(),
                     "Condional validation Error", "Clause is missing value");
            }
         }
         else
         {
            valid = false;
            MessageDialog.openError(getShell(), "Condional validation Error",
                  "Clause is missing variable");
         }
         return valid;
      }
      
      @SuppressWarnings("synthetic-access")
      public PSConditional toConditional()
      {
         if(isEmpty() || !isValidCondition())
            return null;
         
         if(StringUtils.isBlank(bool))
            bool = PSConditional.OPBOOL_AND;
         PSConditional cond = new PSConditional(variable,op,value,bool);
         
         return cond;
      }
      public IPSReplacementValue variable;
      public IPSReplacementValue value;
      public String op;
      public String bool;
   }
   
   @SuppressWarnings("unchecked")
   public PSRule getRuleData()
   {
      m_condTable.refreshTable();
      PSCollection condRules = new PSCollection(PSConditional.class);
      List<CondRow> vals = (List<CondRow>)m_condTable.getValues();
      if(vals.isEmpty())
      {
         MessageDialog.openError(getShell(), "Empty Conditionals",
               "At least one valid condition is required.");
         return null;
      }
      for(CondRow val:vals)
      {
         PSConditional cond = val.toConditional();
         if(cond != null)
            condRules.add(cond);
      }
      if(!condRules.isEmpty())
         return new PSRule(condRules);
         
      return null;
   }

   public void dispose()
   {
      super.dispose();
   }

   protected void checkSubclass()
   {
   }
   
   private List<CondRow> m_condData;
   private PSSortableTable m_condTable;
   private PSRule m_rule;
   private String[] m_opOptions =
   {StringUtils.EMPTY, PSConditional.OPTYPE_EQUALS,
         PSConditional.OPTYPE_NOTEQUALS, PSConditional.OPTYPE_LESSTHAN,
         PSConditional.OPTYPE_LESSTHANOREQUALS,
         PSConditional.OPTYPE_GREATERTHAN,
         PSConditional.OPTYPE_GREATERTHANOREQUALS, PSConditional.OPTYPE_ISNULL,
         PSConditional.OPTYPE_ISNOTNULL, PSConditional.OPTYPE_BETWEEN,
         PSConditional.OPTYPE_NOTBETWEEN, PSConditional.OPTYPE_IN,
         PSConditional.OPTYPE_NOTIN, PSConditional.OPTYPE_LIKE,
         PSConditional.OPTYPE_NOTLIKE};

   private String[] m_boolOptions =
   {StringUtils.EMPTY, PSConditional.OPBOOL_AND, PSConditional.OPBOOL_OR};
   private static final String COL_VARIABLE = "Variable";
   private static final String COL_OP = "Op";
   private static final String COL_VALUE = "Value";
   private static final String COL_BOOL = "Bool";
}

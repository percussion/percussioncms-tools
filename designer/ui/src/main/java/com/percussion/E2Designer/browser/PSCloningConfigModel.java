/******************************************************************************
 *
 * [ PSCloningConfigModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSCloneHandlerConfig;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSProcessCheck;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.util.PSCollection;
import com.percussion.guitools.PSTableModel;
import org.apache.commons.collections.IteratorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * The model to display cloning properties of relationship configuration.
 * Extracted it from {@link PSRelationshipEditorDialog} for easier comprehension.
 */
public class PSCloningConfigModel extends PSTableModel
{
   /**
    * Constructs this model to display all system process checks and enable
    * the ones that also exists in the supplied list of process checks. The
    * conditions for the process checks are taken from the overridden process
    * checks.
    *
    * @param configProcChecks the list of process checks, may not be <code>
    * null</code> can be empty.
    */
   public PSCloningConfigModel(Iterator configProcChecks,
         PSCloneHandlerConfig cloneHandlerConfig)
   {
      final Vector<Vector<Object>> data =
         buildCheckData(configProcChecks, cloneHandlerConfig);
      setDataVector(data, PSRelationshipEditorDialog.ms_cloneColumns);
   }

   /**
    * Builds list of lists where first column - boolean with enabled/disabled,
    * second - rel. type check or if it does not exist corresponding
    * global check, third - the cleaned up list of conditions of the check
    * from the second column. 
    * @param configProcChecks relationship type checks. 
    */
   public static Vector<Vector<Object>> buildCheckData(
         Iterator configProcChecks, PSCloneHandlerConfig cloneHandlerConfig)
   {
      if (configProcChecks == null)
      {
         throw new IllegalArgumentException(
            "configProcChecks may not be null.");
      }

      final Map<String, PSProcessCheck> configChecksMap =
            getConfigChecksMap(configProcChecks);
      final Iterator sysProcessChecks = cloneHandlerConfig.getProcessChecks();
      final Vector<Vector<Object>> data = new Vector<Vector<Object>>();
      while (sysProcessChecks.hasNext())
      {
         Vector<Object> element = new Vector<Object>();
         PSProcessCheck check = (PSProcessCheck) sysProcessChecks.next();
         if (configChecksMap.containsKey(check.getName())
            && PSRelationshipEditorDialog.isProcessCheckEnabled(
               configChecksMap.get(check.getName())))
         {
            element.add(Boolean.TRUE);

            PSProcessCheck temp = configChecksMap.get(check.getName());
            element.add(temp);
            element.add(cleanupConditions(temp.getConditions()));
         }
         else
         {
            element.add(Boolean.FALSE);
            element.add(check);
            element.add(cleanupConditions(check.getConditions()));
         }
         assert element.size() == 3;
         data.add(element);
      }
      return data;
   }

   /**
    * Builds checks map from the checks iterator.
    */
   private static Map<String, PSProcessCheck> getConfigChecksMap(Iterator configProcChecks)
   {
      final Map<String, PSProcessCheck> configChecks =
            new HashMap<String, PSProcessCheck>();

      while(configProcChecks.hasNext())
      {

         PSProcessCheck check = (PSProcessCheck) configProcChecks.next();
         configChecks.put(check.getName(), check);
      }
      return configChecks;
   }

   /**
    * Gets data represented by this model.
    *
    * @return the list of process checks enabled, never <code>null</code>,
    * may be empty.
    */
   public Iterator getData()
   {
      List<PSProcessCheck> processChecks = new ArrayList<PSProcessCheck>();

      for (int i = 0; i < getRowCount(); i++)
      {
         PSProcessCheck check = (PSProcessCheck)getValueAt(i, COL_CLONE_NAME);

         if (check != null)
         {
            processChecks.add(check);
            final Boolean enabled = (Boolean) getValueAt(i, COL_CLONE_ENABLE);
            final List list = (List) getValueAt(i, COL_CLONE_COND);
            updateProcessCheck(check, enabled, list);
         }

      }

      return processChecks.iterator();
   }

   /**
    * Updates process check with the specified enabled flag and list of
    * conditionals.
    */
   public static void updateProcessCheck(final PSProcessCheck check,
         final Boolean enabled, final List conditionalsList)
   {
      // Set the first rule conditional which
      // indicates that this check is enabled or disabled.
      if (!conditionalsList.isEmpty())
      {
         PSRule rule = (PSRule) conditionalsList.get(0);
         final Iterator iterator = rule.getConditionalRules();
         if (iterator.hasNext())
         {
            PSConditional cond = (PSConditional) iterator.next();
            final IPSReplacementValue val = new PSTextLiteral("1");
            try
            {
               cond.setVariable(val);
               if (enabled)
               {
                  cond.setValue(val);
               }
               else
               {
                  cond.setValue(new PSTextLiteral("2"));
               }
            }
            catch (IllegalArgumentException ignore){}
         }
      }
      check.setConditions(conditionalsList.iterator());
   }

   /**
    * Checks for existance of the conditional rule that indicates
    * that a process check is enabled. If no rules exists or the rule
    * is not in the correct format then a default rule is added.
    * @param conds the conditional rules. May be <code>null</code>.
    * @return list of conditonal rules. Never <code>null</code> or
    * empty.
    */
   @SuppressWarnings("unchecked")
   private static List cleanupConditions(Iterator conds)
   {
      List<PSRule> rules = IteratorUtils.toList(conds); 

      try
      {
         PSConditional conditional =
            new PSConditional(
               new PSTextLiteral("1"), "=", new PSTextLiteral("1"));

         if(rules.isEmpty())
         {
            PSCollection coll = new PSCollection(PSConditional.class);
            coll.add(conditional);
            PSRule rule = new PSRule(coll);
            rules.add(rule);

         }
         else
         {
            PSRule rule = (PSRule)rules.get(0);
            PSCollection coll = rule.getConditionalRulesCollection();
            if(coll.isEmpty())
            {
               coll.add(conditional);
            }
            else
            {
               // Insert a new default conditional if it is not in
               // the proper format.
               PSConditional cond = (PSConditional)coll.get(0);
               IPSReplacementValue val = cond.getValue();
               IPSReplacementValue var = cond.getVariable();
               String op = cond.getOperator();
               if(!op.equals("=")
                  || (!(val instanceof PSTextLiteral)
                     && !(val instanceof PSNumericLiteral))
                  || (!(var instanceof PSTextLiteral)
                     && !(var instanceof PSNumericLiteral)))
               {
                   coll.insertElementAt(conditional, 0);
               }

            }

         }
      }
      catch(IllegalArgumentException ignore){}

      return rules;
   }


   /**
    * Checks whether the supplied cell is editable or not. Overridden to make
    * only <code>COL_CLONE_ENABLE</code> and <code>COL_CLONE_COND</code> are
    * editable. These columns are editable only if the row of column
    * represents a data row (process check).
    *
    * @param row the row index of value to get, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index of value to get, must be >= 0 and less than
    * {@link #getColumnCount() columncount} of this model.
    *
    * @return <code>true</code> if the cell is editable, otherwise <code>
    * false</code>
    */
   public boolean isCellEditable(int row, int col)
   {
      return col != COL_CLONE_NAME && getValueAt(row ,col) != null;
   }

   //implements IPSTableModel interface method.
   public String getDescription(int row)
   {
      String description = "";
      Object obj = getValueAt(row, COL_CLONE_NAME);
      if(obj instanceof PSProcessCheck)
      {
         PSProcessCheck check = (PSProcessCheck)obj;
         description = check.getDescription();
      }

      return description;
   }

   /* The following represents indices of columns of cloning table model */
   public static final int COL_CLONE_ENABLE = 0;
   public static final int COL_CLONE_NAME = 1;
   public static final int COL_CLONE_COND = 2;
}

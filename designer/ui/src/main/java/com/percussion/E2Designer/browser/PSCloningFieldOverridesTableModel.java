/******************************************************************************
 *
 * [ PSCloningFieldOverridesTableModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSCloneOverrideField;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSRule;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.util.PSCollection;
import com.percussion.guitools.PSTableModel;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * The model to manage cloning field overrides.
 * Extracted it from {@link PSRelationshipEditorDialog} for easier comprehension.
 */
public class PSCloningFieldOverridesTableModel extends PSTableModel
{
   /**
    * Constructs this model with supplied parameters.
    *
    * @param cloneOverrides the list of clone overrides to edit/view, may not be
    * <code>null</code>.
    */
   public PSCloningFieldOverridesTableModel(Iterator cloneOverrides)
   {
      if(cloneOverrides == null)
         throw new IllegalArgumentException("cloneOverrides may not be null.");

      Vector<Vector<Object>> data = buildCloningFieldOverridesData(cloneOverrides);
      setDataVector(data, getColumnNames());

      //manages to have minimum number of rows in model.
      if (data.size() < PSRelationshipEditorDialog.MIN_ROWS)
      {
         setNumRows(PSRelationshipEditorDialog.MIN_ROWS);
      }
   }

   /**
    * Builds list of lists where first column - field override name,
    * second - extension call, third - list of condition rules
    * from the second column.
    * Note, it is public static only as a hack to share functionality with
    * new editor.
    */
   public static Vector<Vector<Object>> buildCloningFieldOverridesData(
         final Iterator cloneOverrides)
   {
      Vector<Vector<Object>> data = new Vector<Vector<Object>>();
      while(cloneOverrides.hasNext())
      {
         Object obj = cloneOverrides.next();

         if (!(obj instanceof PSCloneOverrideField))
         {
            throw new IllegalArgumentException(
               "all elements in effects must be instances of" +
               " PSCloneOverrideField");
         }

         final Vector<Object> element = new Vector<Object>();
         data.add(element);

         final PSCloneOverrideField fieldOverride = (PSCloneOverrideField) obj;
         String name = fieldOverride.getName();

         element.add(StringUtils.isBlank(name) ? "" : name);

         final IPSReplacementValue val = fieldOverride.getReplacementValue();
         if (!(val instanceof PSExtensionCall))
         {
            throw new IllegalArgumentException(
               "Only IPSReplacementValue(s) of type UDF are supported");
         }

         element.add(new OSExtensionCall((PSExtensionCall)val));

         element.add(fieldOverride.getRules());
         assert element.size() == 3;
      }
      return data;
   }

   /**
    * Gets the cloneOverrides set on this table model.
    *
    * @return the list of cloneOverrides,
    * never <code>null</code>, may be empty.
    */
   public Iterator getData()
   {
      List<PSCloneOverrideField> cloneOverrides =
            new ArrayList<PSCloneOverrideField>();

      for (int i = 0; i < getRowCount(); i++)
      {
         String field = (String)getValueAt(i, COL_FIELD);
         Object obj = getValueAt(i, COL_UDF);
         Object conds = getValueAt(i, COL_COND);

         if(obj instanceof PSExtensionCall)
         {
            PSCloneOverrideField cloneOverride
               = new PSCloneOverrideField(field, (IPSReplacementValue)obj);

            if (conds instanceof PSCollection)
               cloneOverride.setRules((PSCollection) conds);

            cloneOverrides.add(cloneOverride);
         }
      }

      return cloneOverrides.iterator();
   }

   /**
    * Sets the value at the specified cell. Overriden to set empty list of
    * conditions in the column <code>COL_COND</code> for that row if it
    * is not already set, when an extension is chosen in the column <code>
    * COL_UDF</code>. Useful to render a 'conditions' button when an
    * extension chosen in a new row. See super's description for parameter
    * description.
    */
   @Override
   public void setValueAt(Object value, int row, int col)
   {
      if (value != null)
      {
         if (col == COL_UDF)
         {
            List conds = (List) getValueAt(row, COL_COND);
            if (conds == null)
            {
               super.setValueAt(new PSCollection(PSRule.class), row, 
                  COL_COND);
            }
         }
      }
      super.setValueAt(value, row, col);
   }

   /**
    * Checks whether the supplied cell is editable or not. Overridden to make
    * the <code>COL_COND</code> is editable only if the extension/effect
    * in that row is set.
    *
    * @param row the row index of value to get, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index of value to get, must be >= 0 and less than
    * {@link #getColumnCount() columncount} of this model.
    *
    * @return <code>true</code> if the cell is editable, otherwise <code>
    * false</code>
    */
   @Override
   public boolean isCellEditable(int row, int col)
   {
      if (col == COL_COND)
      {
         if (getValueAt(row, COL_UDF) == null)
            return false;
      }

      return true;
   }

   /**
    * Gets the column names of the model based on type of extensions it is
    * representing.
    *
    * @return the list of column names, never <code>null</code> or empty.
    */
   private Vector<String> getColumnNames()
   {
      return PSRelationshipEditorDialog.ms_cloneFieldOverridesColumns;
   }

   //implements interface method.
   @Override
   public String getDescription(int row)
   {
      Object obj = getValueAt(row, COL_UDF);
      return getExtensionDescription(obj);
   }

   /**
    * Retrieves field override description for the provided field override.
    */
   public static String getExtensionDescription(Object obj)
   {
      if (obj instanceof OSExtensionCall)
      {
         OSExtensionCall call = (OSExtensionCall)obj;
         IPSExtensionDef def = call.getExtensionDef();
         final String description = def.getInitParameter(
            IPSExtensionDef.INIT_PARAM_DESCRIPTION);
         return StringUtils.defaultString(description);
      }

      return "";
   }

   /**
    * Always returns true.
    */
   @Override
   public boolean allowRemove()
   {
      return true;
   }

   /**
    * Always returns true.
    */
   @Override
   public boolean allowMove()
   {
      return true;
   }

   /**
    * Field column index.
    */
   public static final int COL_FIELD = 0;

   /**
    * UDF column index.
    */
   public static final int COL_UDF = 1;

   /**
    * Conditional column index.
    */
   public static final int COL_COND = 2;

}

/*[ PSRuleEditorTableModel.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSConditionalSet;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSRule;
import com.percussion.error.PSTableCellValidationError;
import com.percussion.util.PSCollection;
import com.percussion.guitools.PSTableModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;



/**
 * This is a table model that represents a set of PSRule objects
 * in a JTable.
 */

public class PSRuleEditorTableModel extends PSTableModel
{

   /**
    * Construct the table.
    * @param rules collection of PSRule objects. May be <code>null</code>
    * or empty.
    */
   public PSRuleEditorTableModel(PSCollection rules)
   {
      createTable(createHeaders());
      if(null != rules)
         loadFromRules(rules);
   }

   /**
    * Derived classes may wish to override this to return a collection
    * object for different rules.
    *
    * @return A new collection to hold rules when saving from the model
    * to a collection. Never <code>null</code>.
    *
    * @throws ClassNotFoundException if the collection for this object cannot
    * be created.
    */
   protected static PSCollection createNewCollection()
         throws ClassNotFoundException
   {
      return new PSCollection( "com.percussion.design.objectstore.PSRule" );
   }

   /**
    * Gets table resources and caches them into a static context for
    * this class.
    * @return a the resource bundle for this class. Should not be
    * <code>null</code>.
    */
   public static ResourceBundle getResources()
   {
      try
      {
         if (ms_resources == null)
            ms_resources =
               ResourceBundle.getBundle(ms_bundle_loc + "Resources",
                                        Locale.getDefault() );
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }

      return ms_resources;
   }

   /**
    * The table headers. Derived classes can override this method to add
    * additional headers to the right side of the table.
    * This model does not support adding columns
    * to the left side of the table or in-between these columns.
    *
    * @return A vector containing all of the names for the column headers.
    * Never <code>null</code>, may be empty.
    */
   protected Vector createHeaders()
   {
      Vector headers = new Vector( 3 );
      /* the order of these columns must match the order of the public static
         column indexes, <...>_COL_INDEX. */
      headers.addElement(TYPE_COL_NAME);
      headers.addElement(RULE_COL_NAME);
      headers.addElement(OPERATOR_COL_NAME);

      return headers;
   }

   /**
    * Load table from a collection of rules.
    * <p>If derived classes have added columns to the right side of this table,
    * they should override <code>createRow</code> to append the values to the
    * row.
    *
    * @param rules the rules, Each object in the supplied collection
    * must be a PSRule. May be <code>null</code> or empty.
    */
   public void loadFromRules(PSCollection rules)
   {


       if (rules != null)
       {
          // remove all rows
          getDataVector().clear();
          for (int i=0; i<rules.size(); i++)
          {
             PSRule rule = (PSRule) rules.get(i);
             appendRow(createRow(rule,(i==rules.size()-1)));
           }
          // make sure we have the min rows
          // showing
          setNumRows(MIN_ROWS);
        }


   }

   /**
    * Adds all the elements for the rule to the editor. This is broken
    * out of the load method so derived classes can add/override the columns
    * that get added and/or the order they get added in. If a derived class wants
    * to add an extra column, it can just call this method, then add its row.
    *
    * @param rule The rule object that will be read to get the
    * element values for the row. Can not be <code>null</code>.
    *
    * @param isLastRow If this is the last row being added, this flag should be <code>
    * true</code>. In this case, an empty BOOL operator is set in the table.
    *
    * @return A vector containing all the elements for the row. Never
    * <code>null</code>, may be empty.
   **/
   protected Vector createRow( PSRule rule, boolean isLastRow )
   {
      Vector row = new Vector( 3 );
      if(rule.isExtensionSetRule())
      {
         row.addElement(RULE_TYPE_EXTENSION);
         PSExtensionCall extCall =
            (PSExtensionCall)rule.getExtensionRules().get(0);
         row.addElement(new OSExtensionCall(extCall));

      }
      else
      {
         row.addElement(RULE_TYPE_CONDITIONAL);
         row.addElement(
            new PSConditionalSet(
               rule.getConditionalRulesCollection()));
      }


      int op = rule.getOperator();

      if(isLastRow)
      {
        row.addElement(null);
      }
      else if(op == PSRule.BOOLEAN_AND)
      {
        row.addElement(BOOLEAN_AND_STRING);
      }
      else if(op == PSRule.BOOLEAN_OR)
      {
         row.addElement(BOOLEAN_OR_STRING);
      }

      return row;
   }


   /**
    * Attempts to create a new rule object. If successful, the object is
    * returned. If it fails, null is returned.
    * @param row the row index
    *
    * @return A new PSRule if successful, else <code>null</code> is returned.
    */
   protected PSRule createRule( int row )
   {
       PSRule rule = null;


       String type = (String) getValueAt(row, TYPE_COL_INDEX);
       String op = (String) getValueAt(row, OPERATOR_COL_INDEX);

       if(null == type)
          return rule;

       if(type.equals(RULE_TYPE_EXTENSION))
       {

          OSExtensionCall call =
             (OSExtensionCall)getValueAt(row, RULE_COL_INDEX);
          PSExtensionCallSet callSet = new PSExtensionCallSet();
          callSet.add(call);

          rule = new PSRule(callSet);

       }
       else
       {
          rule =
             new PSRule((PSCollection) getValueAt(row, RULE_COL_INDEX));
       }
       if(null != op && op.equals(BOOLEAN_AND_STRING))
       {
          rule.setOperator(PSRule.BOOLEAN_AND);
       }
       else if(null != op && op.equals(BOOLEAN_OR_STRING))
       {
          rule.setOperator(PSRule.BOOLEAN_OR);
       }


       return rule;

   }

   /**
    * Checks all entries in the table. If any entry is found that is not valid,
    * a PSTableCellValidationError object is returned that indicates where and what the
    * problem is. Rows are checked from top to bottom. If there are no errors,
    * <code>null</code> is returned.
    *
    * @return If any errors occur, a ValidationContext is returned describing the
    * error. If no errors are found, <code>null</code> is returned.
   **/
   public PSTableCellValidationError validate()
   {
      PSTableCellValidationError error = null;
      int rows = getRowCount();
      for ( int i=0; i < rows; ++i )
      {
         error = validateRow(i);
         if ( null != error )
            break;
      }
      return error;
   }


   /**
    * Validates the string representation of data in the model in the supplied
    * row number (the types of the objects in each cell are not validated).
    * The following rules are validated:
    * <UL>
    *    <LI>If type exists then rule field must have a valid conditional
    *    or extension</LI>
    * </UL>
    *
    * @param row The index of the row to validate. 0 is the first row.
    *
    * @return PSTableCellValidationError if validation fails, else
    * <code>null</code>.
   **/
   public PSTableCellValidationError validateRow( int row )
   {
      boolean bError = false;
      Object obj = getValueAt(row, TYPE_COL_INDEX);
      if(null == obj)
         return null;
      boolean hasType = obj.toString().trim().length() > 0;
      Object rule =  getValueAt(row, RULE_COL_INDEX);

      String error = null;
      int col = -1;

      if(hasType)
      {
         if(rule == null)
         {
           error = getResources().getString("missingRule");
           col = RULE_COL_INDEX;
         }

      }

      return null == error ? null : new PSTableCellValidationError( error, row, col );

   }

   /**
    * Gets this table model data.
    *
    * @return the iterator over zero or more elements each representing a row
    * data, never <code>null</code>. The elements can be any <code>Object</code>
    * as defined by the implementor.
    */
   public Iterator getData()
   {
      List data = new ArrayList();
      int rows = getActualRowCount();
      Object obj = null;
      for(int i=0; i<rows; i++)
      {
        obj = getData(i);
        if(null != obj)
           data.add(obj);
      }

      return data.iterator();

   }

   /**
    * Gets the object representing supplied row.
    *
    * @param row the row index, must be >= 0 and less than row count of
    * this model.
    *
    * @return the data, may be <code>null</code>.
    *
    * @throws IndexOutOfBoundsException if row index is not valid.
    */
   public Object getData(int row)
   {
     return createRule(row);
   }

   /**
    * Append provided row at table end.
    *
    * @param row the row index
    */
   public void appendRow(Vector row)
   {
      if(row.size() != getColumnCount())
          throw new IllegalArgumentException(
             "The row size is not valid for this table.");

      addRow(row);
      fireTableDataChanged();
   }

   /**
    * Create an empty table with the number of column as header entries.
    *
    * @param headers a vector of table headers; cannot be <code>null</code>
    * @throws IllegalArgumentException if headers is <code>null</code>
    */
   public void createTable(Vector headers)
   {
      if (null == headers)
         throw new IllegalArgumentException("headers Vector cannot be null");

      Vector tableData = new Vector();
      Vector row = new Vector();
      for(int i=0; i<headers.size(); i++)
      {
          row.addElement(new Vector());
      }
      setDataVector(tableData, headers);
      setNumRows(MIN_ROWS);
   }

   /**
    * Returns the actual number of rows in the table
    * ignoring blank rows.
    * @return the actual number of non blank rows in the table.
    */
   public int getActualRowCount()
   {
      int totalRows = getRowCount();
      int rowCount = 0;
      for(int i = 0; i<totalRows; i++)
      {
         if(null != ((String) getValueAt(i, TYPE_COL_INDEX)))
            rowCount++;
      }
      return rowCount;

   }

   /**
    * Indicates whether or not we can remove table rows. This overriden method
    * always allows removing.
    * @return <code>true</code> if allowed to remove rows,
    * else <code>false</code>.
    */
   public boolean allowRemove()
   {
     return true;
   }

   /**
    * Indicates whether or not we can move table rows. This overriden method
    * always allows moving.
    * @return <code>true</code> if allowed to move rows,
    * else <code>false</code>.
    */
   public boolean allowMove()
   {
     return true;
   }

   /**
    * The resource bundle. Initialized in {@link getResources()}, never
    * <code>null</code> after that.
    */
   private static ResourceBundle ms_resources = null;

   /**
    * The bundle location
    */
   private final static String ms_bundle_loc =
      "com.percussion.E2Designer.PSRuleEditorTableModel";
   /**
    * Index of the "type" column
    */
   public final static int TYPE_COL_INDEX = 0;
   /**
    * Index of the "rule" column
    */
   public final static int RULE_COL_INDEX = 1;
   /**
    * Index of the "operator" column
    */
   public final static int OPERATOR_COL_INDEX = 2;
   /**
    * Header name for the "type" column
    */
   public final static String TYPE_COL_NAME = getResources().getString("type");
   /**
    * Header name for the "rule" column
    */
   public final static String RULE_COL_NAME = getResources().getString("rule");
   /**
    * Header name for the "operator" column
    */
   public final static String OPERATOR_COL_NAME =
      getResources().getString("operator");
   /**
    * Extension rule type
    */
   public final static String RULE_TYPE_EXTENSION =
      getResources().getString("Extension");
   /**
    * Conditional rule type
    */
   public final static String RULE_TYPE_CONDITIONAL =
      getResources().getString("Conditional");
   /**
    * Boolean "AND" as string
    */
   public final static String BOOLEAN_AND_STRING = getResources().getString("and");
   /**
    * Boolean "OR" as string
    */
   public final static String BOOLEAN_OR_STRING = getResources().getString("or");
   /**
    * The minimum number of rows to display for the table.
    */
   private static final int MIN_ROWS = 12;



}
/*[ ConditionalTableModel.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.util.PSCollection;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The table model used for the conditional table editor. It has 3 or 4 columns,
 * depending on the show boolean flag. The 4 columns are:
 * <UL>
 *    <LI>Variable - type IPSReplacementValue</LI>
 *    <LI>Operator - type String</LI>
 *    <LI>Value - type IPSReplacementValue</LI>
 *    <LI>Bool (optionally shown) - type String</LI>
 * </UL>
 * Data is exchanged between the model and OS... classes via a PSCollection.
 * The passed in collection is a collection of PSConditional objects. Column
 * re-ordering is not supported.
 *
 * @see PSConditional for more details on the 4 column types.
 */
////////////////////////////////////////////////////////////////////////////////
public class ConditionalTableModel extends UTTableModel
{
   /**
    * These 4 variables can be used as column indexes for the 4 possible columns
    * supported by this model. BOOL is only applicable if <code>isShowingBoolColumn
    * </code> returns <code>true</code>.
    */
   public final static int VARIABLE_COL_INDEX = 0;
   public final static int OPERATOR_COL_INDEX = 1;
   public final static int VALUE_COL_INDEX = 2;
   public final static int BOOL_COL_INDEX = 3;

   /**
    * These 4 strings are the identifiers for the 4 columns in this model. BOOL
    * is only applicable if <code>isShowingBoolColumn</code> returns <code>true
    * </code>.
    */
   public final static String VARIABLE_COL_NAME = getResources().getString("variable");
   public final static String OPERATOR_COL_NAME = getResources().getString("operator");
   public final static String VALUE_COL_NAME = getResources().getString("value");
   public final static String BOOL_COL_NAME = getResources().getString("bool");


   /**
    * Construct the table.
    *
    * @param bShowBool Flag used to determine whether the table model should
    * show the boolean operation. If it is <code>true</code>, the model will
    * have 4 columns, allowing programatic changing of the boolean, otherwise,
    * the model forces the condition to be AND.
    */
   public ConditionalTableModel( boolean bShowBool )
   {
      m_bShowBool = bShowBool;
      createTable( createHeaders());
   }

   /**
    * Creates a conditional model w/ 4 columns, showing the boolean.
    */
   public ConditionalTableModel()
   {
      this( true );
   }

   /**
    * @return <code>true</code> if the 4th (bool) column is present in the model.
    */
   public boolean isShowingBoolColumn()
   {
      return m_bShowBool;
   }

   /**
    * Load table from a collection of conditionals.
    * <p>If derived classes have added columns to the right side of this table,
    * they should override <code>createRow</code> to append the values to the
    * row.
    *
    * @param conditionals   the conditions, Each object in the supplied collection
    * must be a PSConditional.
    */
   public void loadFromConditionals(PSCollection conditionals)
   {
      try
      {
         if (conditionals != null)
         {
            for (int i=0; i<conditionals.size(); i++)
            {
               PSConditional conditional = (PSConditional) conditionals.get(i);
               appendRow( createRow( conditional, i==conditionals.size()-1));
            }
         }
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Adds all the elements for the conditional to the editor. This is broken
    * out of the load method so derived classes can add/override the columns
    * that get added and/or the order they get added in. If a derived class wants
    * to add an extra column, it can just call this method, then add its row.
    *
    * @param conditional The conditional object that will be read to get the
    * element values for the row.
    *
    * @param bLastRow If this is the last row being added, this flag should be <code>
    * true</code>. In this case, an empty BOOL operator is set in the table.
    *
    * @return A vector containing all the elements for the row.
    */
   protected Vector createRow( PSConditional conditional, boolean bLastRow )
   {
      Vector row = new Vector( m_bShowBool ? 4 : 3 );
      row.addElement(conditional.getVariable());
      row.addElement(conditional.getOperator());
      row.addElement(conditional.getValue());
      if ( m_bShowBool )
      {
         if ( bLastRow )
            row.addElement("");
         else
            row.addElement(conditional.getBoolean());
      }
      return row;
   }

   /**
    * Save the table to the provided collection of conditionals. Any existing
    * conditions in the provided collection will be deleted first. It is assumed
    * that the table has passed validation using the validation method in this
    * class. If any invalid rows exist, they will be skipped w/ no indication.
    * A row w/ no variable is skipped. For all other rows, an attempt is made to
    * create a new conditional object. If this fails, the row is skipped.
    *
    * @param conditionals   the conditional set we are saving to. Any conditionals
    * already in the set are removed before adding the new ones. If the set is
    * null, a new set is created.
    *
    * @return If a collection is passed in, it is returned, otherwise, the newly
    * created collection is returned. If there are no conditionals, null is
    * returned.
    *
    * @throws ClassNotFoundException if conditionals is null and the attempt to
    * create a new one fails (TODOph: make a better exception for this case)
    */
   public PSCollection saveToConditionals(PSCollection conditionals)
         throws ClassNotFoundException
   {
      if ( null == conditionals )
         conditionals = createNewCollection();

      try
      {
         // clear existing stuff first
         conditionals.clear();

         // create new entries from the table
         int rows = getRowCount();
         for (int i=0; i<rows; i++)
         {
            // omit empty or incomplete rows
            if (0 == getValueAt(i, VARIABLE_COL_INDEX).toString().trim().length())
               continue;

            PSConditional conditional = createConditional( i );
            if ( null != conditional )
               conditionals.add(conditional);
         }
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
      return 0 == conditionals.size() ? null : conditionals;
   }


   /**
    * @return a new PSCollection for PSConditional objects. Derived classes may
    * wish to override this to return a collection object for different conditionals.
    *
    * @return A new collection to hold conditionals when saving from the model
    * to a collection.
    *
    * @throws ClassNotFoundException if the collection for this object cannot
    * be created.
    */
   protected PSCollection createNewCollection()
         throws ClassNotFoundException
   {
      return new PSCollection( "com.percussion.design.objectstore.PSConditional" );
   }

   /**
    * Checks all entries in the table. If any entry is found that is not valid,
    * a ConditionalValidationError object is returned that indicates where and what the
    * problem is. Rows are checked from top to bottom. If there are no errors,
    * null is returned.
    *
    * @return If any errors occur, a ValidationContext is returned describing the
    * error. If no errors are found, null is returned.
    */
   public ConditionalValidationError validate()
   {
      ConditionalValidationError error = null;
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
    *                                  <LI>If the variable has a value, there must be an operator and a value
    *                                                                  (if the operator is binary)</LI>
    *                                  <LI>If variable and value are present, the operator must be present.</LI>
    * </UL>
    *
    * @param row The index of the row to validate. 0 is the first row.
    */
   public ConditionalValidationError validateRow( int row )
   {
      boolean bError = false;

      boolean bVariable = getValueAt(row, VARIABLE_COL_INDEX).toString().trim().length() > 0;
      String op = getValueAt(row, OPERATOR_COL_INDEX).toString();
      boolean bOp = op.length() > 0;
      // bUnary is false if bOp is false
      boolean bUnary = bOp ? PSConditional.isUnaryOp(op) : false;
      Object value = getValueAt(row, VALUE_COL_INDEX);
      boolean bValue = null != value ? value.toString().trim().length() > 0 : false;

      String error = null;
      int col = -1;
      if ( bVariable )
      {
         if ( !bOp )
         {
            error = getResources().getString( "MissingOp" );
            col = OPERATOR_COL_INDEX;
         }
         else if ( !bUnary && !bValue )
         {
            error = getResources().getString( "OpRequiresVal" );
            col = VALUE_COL_INDEX;
         }
      }
      else if ( bValue && bOp && !bUnary )
      {
         error = getResources().getString( "MissingVar" );
         col = VARIABLE_COL_INDEX;
      }

      return null == error ? null : new ConditionalValidationError( error, row, col );
   }

   /**
    * Attempts to create a new conditional object. If successful, the object is
    * returned. If it fails, null is returned. The objects in the table for the
    * variable and value must be IPSReplacementValue objects or null will be
    * returned.
    *
    * @return A new PSConditional if successful, else null is returned.
    */
   protected PSConditional createConditional( int row )
   {
      PSConditional conditional = null;
      try
      {
         Object value = getValueAt(row, VALUE_COL_INDEX);
         String op = (String) getValueAt(row, OPERATOR_COL_INDEX);
         boolean bUnary = PSConditional.isUnaryOp(op);
         conditional = new PSConditional(
            (IPSReplacementValue) getValueAt(row, VARIABLE_COL_INDEX), op,
                  bUnary ? null : (value instanceof IPSReplacementValue ?
                     (IPSReplacementValue) value : null ));
         String bool = "";
         if ( m_bShowBool )
            bool = (String) getValueAt(row, BOOL_COL_INDEX);
         // if we don't set the bool, it will default to AND
         if ( m_bShowBool )
         {
            if (bool.equals(PSConditional.OPBOOL_AND) ||
                  bool.equals(PSConditional.OPBOOL_OR))
            {
               conditional.setBoolean(bool);
            }
         }

         return conditional;
      }
      // ignore expected exceptions, they are indicated by the return value
      catch ( IllegalArgumentException e )
      {
      }
      catch ( ClassCastException e )
      {
      }

      return conditional;
   }

   /**
    * Get table resources.
    */
   private static ResourceBundle ms_res = null;
   public static ResourceBundle getResources()
   {
      try
      {
         if (ms_res == null)
            ms_res = ResourceBundle.getBundle(
               "com.percussion.E2Designer.ConditionalTableModel" + "Resources",
                  Locale.getDefault() );
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }

      return ms_res;
   }

   /**
    * The table headers. Derived classes can override this method to add additional
    * headers to the right side of the table. This model does not support adding columns
    * to the left side of the table or in-between these columns.
    * <p><em>Note:</em> This method is called from the constructor, so any base
    * classes that override it must treat it like a static method.
    *
    * @return A vector containing all of the names for the column headers.
    */
   protected Vector createHeaders()
   {
      Vector headers = new Vector( m_bShowBool ? 4 : 3 );
      /* the order of these columns must match the order of the public static
         column indexes, <...>_COL_INDEX. */
      headers.addElement(VARIABLE_COL_NAME);
      headers.addElement(OPERATOR_COL_NAME);
      headers.addElement(VALUE_COL_NAME);
      if ( m_bShowBool )
         headers.addElement(BOOL_COL_NAME);
      return headers;
   }

   /**
    * Should the BOOL column be shown or not. By default it is shown.
    */
   private boolean m_bShowBool = true;
}


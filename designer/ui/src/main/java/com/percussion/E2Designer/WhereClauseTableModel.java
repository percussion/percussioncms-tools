/******************************************************************************
 *
 * [ WhereClauseTableModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.util.PSCollection;

import java.util.Vector;

/**
 * Adds an Omit-when-null column to the right side of the table and manages PSWhereClause
 * conditional objects. The new column has boolean data. No additional validation
 * constraints are added to the conditional table model.
 *
 * @see PSWhereClause
**/
public class WhereClauseTableModel extends ConditionalTableModel
{
   /**
    * The column index of the "omit when null" column added by this table model.
   **/
   public static final int OMIT_COL_INDEX = 4;

   /**
    * This string is the identifier for the omit column in this model.
   **/
   public static final String OMIT_COL_NAME = getResources().getString( "omit" );

   /**
    * Constructs a 5 column table model. The 5th column has data type Boolean.
   **/
   public WhereClauseTableModel()
   {
      // must show the BOOL column
      super( true );
   }

   /**
    * Adds the "omit" data to the row.
    *
    * @throws ClassCastException if conditional is not a PSWhereClause
   **/
   protected Vector createRow( PSConditional conditional, boolean bLastRow )
   {
      PSWhereClause clause = (PSWhereClause) conditional;

      Vector row = super.createRow( conditional, bLastRow );
      row.addElement(clause.isOmittedWhenNull());
      return row;
   }

   /**
    * @return a new PSCollection for PSWhereClause objects. Derived classes may
    * wish to override this to return a collection object for different conditionals.
    *
    * @return A new collection to hold conditionals when saving from the model
    * to a collection.
    *
    * @throws ClassNotFoundException if the collection for this object cannot
    * be created.
   **/
   protected PSCollection createNewCollection()
         throws ClassNotFoundException
   {
      return new PSCollection( "com.percussion.design.objectstore.PSWhereClause" );
   }


   /**
    * Attempts to create a new where clause conditional object. If successful, the object is
    * returned. If it fails, null is returned. The objects in the table for the
    * variable and value must be IPSReplacementValue objects or null will be
    * returned.
    *
    * @return A new PSConditional if successful, else null is returned.
   **/
   protected PSConditional createConditional( int row )
   {
      PSConditional conditional = null;
      try
      {
            Object value = getValueAt(row, VALUE_COL_INDEX);
            String op = (String) getValueAt(row, OPERATOR_COL_INDEX);
            boolean bUnary = PSConditional.isUnaryOp(op);
            Object omit = getValueAt( row, OMIT_COL_INDEX );
            boolean bOmit = omit instanceof Boolean ? ((Boolean) omit).booleanValue() : false;
            conditional = new PSWhereClause(
                     (IPSReplacementValue) getValueAt( row, VARIABLE_COL_INDEX ),
                     op,
                     bUnary ? null : (value instanceof IPSReplacementValue ? (IPSReplacementValue) value : null ),
                     bOmit );

            String bool = (String) getValueAt( row, BOOL_COL_INDEX );
            if (bool.equals(PSConditional.OPBOOL_AND) ||
                  bool.equals(PSConditional.OPBOOL_OR))
            {
               conditional.setBoolean(bool);
            }
      }
      // ignore all expected exceptions, the error is indicated by the return value
      catch ( IllegalArgumentException e )
      {   e.printStackTrace();   }
      catch ( ClassCastException e )
      {   e.printStackTrace();   }

      return conditional;
   }

   /**
    * Adds the header for the "omit when null" column.
    *
    * @return A vector containing the names for all headers in this table model.
   **/
   protected Vector createHeaders()
   {
      Vector headers = super.createHeaders();
      headers.addElement(getResources().getString("omit"));
      return headers;
   }

}

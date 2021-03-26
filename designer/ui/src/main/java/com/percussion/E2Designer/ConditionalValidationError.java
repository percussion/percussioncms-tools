/*[ ConditionalValidationError.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

/**
 * Used to return error information about an invalid entry in the conditional
 * table model. A string describing the problem and the row/col# of the cell
 * containing the error. This info can be used to hilite/activate the offending
 * cell to aid the user. It is a wrapper around a data structure.
**/
public class ConditionalValidationError
{

   public ConditionalValidationError( String errorText, int row, int col )
   {
      m_errorText = errorText;
      m_errorRow = row;
      m_errorCol = col;
   }

   public int getErrorRow()
   {
      return m_errorRow;
   }

   public int getErrorCol()
   {
      return m_errorCol;
   }

   public String getErrorText()
   {
      return m_errorText;
   }

   private int m_errorRow = -1;
   private int m_errorCol = -1;
   private String m_errorText = null;
}

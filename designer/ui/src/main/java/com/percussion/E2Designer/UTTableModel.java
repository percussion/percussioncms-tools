/*[ UTTableModel.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Provides a table model based on the AbstractTableModel. The data is kept in
 * vector of columns which are vectors of cells.
 * Valid cell types are: String, IPSReplacementValue.
 */
public class UTTableModel extends AbstractTableModel
{
   /**
    * Construct the table with the given table headers and an initial number of
    * columns of the passed header vector.
    *
    * @param headers      a vector of table headers
    */
   public UTTableModel()
   {
      super();
   }

   /**
    * Construct a table with the specified headers and data. The first index in
    * the <code>Object[][]</code> array is the row index and the second is the
    * column index.
    *
    * @param data two dimension array of rows then columns
    * @param headers values to use for the column headers
    * @throws IllegalArgumentException if data or headers is <code>null</code>
    */
   public UTTableModel(Object[][] data, Object[] headers)
   {
      if (null == data || null == headers)
         throw new IllegalArgumentException(
               "Cannot provide null values to this constructor");

      m_headers = new Vector( headers.length );
      m_table = new Vector( headers.length );

      for (int col=0; col < headers.length; col++)
      {
         m_headers.addElement( headers[col] );

         Vector column = new Vector( data.length );
         for (int row=0; row < data.length; row++)
         {
            column.addElement( data[row][col] );
         }
         m_table.addElement( column );
      }
   }


   /**
    * Construct a table with the specified headers and data
    *
    * @param data Vector of column Vectors; cannot be <code>null</code>
    * @param headers values to use for the column headers; cannot be <code>null
    * </code>
    * @throws IllegalArgumentException if data or headers is <code>null</code>
    */
   public UTTableModel(Vector data, Vector headers)
   {
      if (null == data || null == headers)
         throw new IllegalArgumentException(
               "Cannot provide null values to this constructor");
      m_headers = new Vector( headers );
      m_table = new Vector( data );
   }

   /**
    * Construct an empty table with the specified headers
    *
    * @param headers values to use for the column headers
    * @throws IllegalArgumentException if headers is <code>null</code>
    */
   public UTTableModel(Vector headers)
   {
      if (null == headers)
         throw new IllegalArgumentException("headers Vector cannot be null");

      createTable( headers );
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

      m_headers = new Vector( headers.size() );
      m_table = new Vector( headers.size() );

      for (int i = 0; i < headers.size(); i++)
      {
         // initialize headers
         m_headers.addElement( headers.elementAt( i ) );

         // initialize an empty table
         Vector col = new Vector();
         m_table.addElement( col );
      }
   }

  /**
   * Log table to screen
   *
   * @param table   the table to save
   */
   public void log()
   {
     System.out.println("");
     System.out.println("");
     for (int i=0; i<getColumnCount(); i++)
       System.out.print("column " + i + "\t");
      System.out.println("--model start-----------------------------------------------------------------");

    for (int j=0; j<getRowCount(); j++)
    {
        for (int i=0; i<getColumnCount(); i++)
      {
            System.out.print(((Vector) m_table.elementAt(i)).elementAt(j).toString() + "\t");
      }
        System.out.println("");
    }
    System.out.println("--model end-------------------------------------------------------------------");
  }

   /**
   * Get the number of rows.
   *
   * @return int   the current number of rows
    */
   public int getRowCount()
   {
      if (getColumnCount() > 0)
          return getColumn(0).size();
      return 0;
   }

   /**
   * Get the column count.
   *
   * @return int   the actual number of columns
    */
   public int getColumnCount()
   {
     return m_table.size();
   }

   /**
   * Get indexed column.
   *
   * @param    column the column index
   * @return Vector   the column of provided index
    */
   public Vector getColumn(int column) throws IllegalArgumentException
   {
     if ((column < 0) || (column > getColumnCount()))
       throw new IllegalArgumentException("The table column index is out of bounds.");

     return (Vector) m_table.elementAt(column);
   }

   /**
   * Get the cell value identified by row/column index.
   *
   * @param    row         the row index
   * @param      column   the column index
   * @return   Object   the value at row/column index
    */
   public Object getValueAt(int row, int column) throws IllegalArgumentException
   {
     if ((column < 0) || (column > getColumnCount()))
       throw new IllegalArgumentException("The table column index is out of bounds.");

     if ((row < 0) || (row > getRowCount()))
       throw new IllegalArgumentException("The passed row has an invalid size.");

    return getColumn(column).elementAt(row);
   }

   /**
    * Clear all entries of indexed column.
    *
    * @param      column   the column index
    * @throws IllegalArgumentException if column is less than 0 or greater than
    * column count
    */
   public void clearColumn(int column) throws IllegalArgumentException
   {
     if ((column < 0) || (column > getColumnCount()))
       throw new IllegalArgumentException("The table column index is out of bounds.");

     for (int i=0; i<getColumn(column).size(); i++)
       getColumn(column).setElementAt("", i);

    fireTableDataChanged();
   }

   /**
    * Clears all entries of indexed row.
    *
    * @param row the row index
    * @throws IllegalArgumentException if row is less than 0 or greater than
    * row count
    */
   public void clearRow(int row) throws IllegalArgumentException
   {
      if (row < 0 || row > getRowCount())
         throw new IllegalArgumentException("Row index is out of bounds");

      for (int col=0; col<getColumnCount(); col++)
      {
         getColumn(col).setElementAt("", row);
      }

      fireTableDataChanged();
   }

   /**
    * Clears all rows from the table.
    */
   public void clearTableEntries()
   {
      createTable(m_headers);
   }

   /**
    * Check whether the indexed row is empty or not.
    *
    * @param   row   the row index
    * @return true, if row is empty (all cells contains empty strings)
    */
   public boolean isRowEmpty(int index)
   {
        if ((index < 0) || (index >= getRowCount()))
          throw new IllegalArgumentException("Index out of bounds.");

        for (int i=0; i<getColumnCount(); i++)
      {
          Object cell = getColumn(i).elementAt(index);
         String strCell = null;
          if (cell instanceof IPSReplacementValue)
            strCell = ((IPSReplacementValue) cell).getValueDisplayText();
         else
            strCell = cell.toString();

          if (!strCell.trim().equals(""))
            return false;
      }

      return true;
   }

   /**
    * Checks whether any cell is empty in the specified row index.
    *
    * @param index the row index, must be >= 0 and < rowcount of the table.
    *
    * @return <code>true</code> if any cell is empty(having <code>null</code>
    * value or empty string), otherwise <code>false</code>
    *
    * @throws IllegalArgumentException if <code>index</code> is less than 0 or
    * greater than or equal to row count of the table.
    */
   public boolean isAnyCellEmpty(int index)
   {
      if ((index < 0) || (index >= getRowCount()))
         throw new IllegalArgumentException("Index out of bounds.");

      for (int i=0; i<getColumnCount(); i++)
      {
         Object cell = getColumn(i).elementAt(index);
         String strCell = null;

         if (cell instanceof IPSReplacementValue)
            strCell = ((IPSReplacementValue) cell).getValueDisplayText();
         else if(cell != null)
            strCell = cell.toString();

         if (strCell == null || strCell.trim().equals(""))
            return true;
      }
      return false;
   }

   /**
   * Check wether the indexed row is empty or not.
   *
   * @param   row   the row index
   * @param boolean   true, if row is empty (contains empty strings)
    */
   public boolean hasEmptyRow() throws IllegalArgumentException
   {
     for (int i=0; i<getRowCount(); i++)
    {
         if (isRowEmpty(i))
         return true;
    }

    return false;
   }

   /**
   * Remove empty rows.
   *
    */
   public void removeEmptyRows() throws IllegalArgumentException
   {
      for (int i=0; i<getRowCount(); i++)
       if (isRowEmpty(i))
         deleteRow(i);
   }

   /**
   * Append provided row at table end.
   *
   * @param      column   the column index
    */
   public void appendRow(Vector row) throws IllegalArgumentException
   {
     if (row.size() != getColumnCount())
       throw new IllegalArgumentException("The row size is not valid for this table.");

     for (int i=0; i<getColumnCount(); i++)
    {
       getColumn(i).addElement(row.elementAt(i));
    }

    fireTableDataChanged();
   }                

   /**
   * Append empty row at table end.
   *
    */
   public void appendRow()
   {
     for (int i=0; i<getColumnCount(); i++)
    {
       getColumn(i).addElement("");
    }

    fireTableDataChanged();
   }

   /**
    * Append the specified number of empty row at table end.
    *
    */
   public void appendRow(int number)
   {
     for (int j=0; j<number; j++)
     {
        for (int i=0; i<getColumnCount(); i++)
       {
          getColumn(i).addElement("");
       }
    }

    fireTableDataChanged();
   }

   /**
    * Sets the min number of rows.
    * @param rows the number of rows, can not be less then 0
    */
    public void setMinRows(int rows)
    {
      if(rows < 0)
         throw new IllegalArgumentException(
            "Number of rows must be greater then 0");

      if(rows > getRowCount())
         appendRow(rows - getRowCount());
    }

   /**
    * Moves the range of the rows to the specifed destination.
    * @param startRange an index of the leading selected row can not be less
    * then 0 and greater then row count.
    * @param endRange an index of the last selected row can not be less then
    * zero and greater then row count.
    * @param destination an index where to insert the range of the rows can not
    * be less then zero or greater then the number of rows
    */
   public void moveRows(int startRange, int endRange, int destination)
   {

      if(destination < 0 ||destination > getRowCount())
         throw new IllegalArgumentException(
         "The destination index is not a valid row index ");

      if(startRange < 0 || startRange > getRowCount())
         throw new IllegalArgumentException(
            "Leading row index is not a valid row index");

      if(endRange < 0 || endRange > getRowCount())
         throw new IllegalArgumentException(
            "The index of last selected row is not a valid index ");

      //moving  one row down
      if((destination - startRange) == 1)
      {
         moveOneRowDown(startRange, endRange);
         return;
      }

      for(int k = 0; k < getRowCount(); k++)
      {
         Vector data = new Vector();

         for(int m = 0; m < getColumnCount(); m++)
         {
            if(k >= startRange && k <= endRange)
            {
               data.addElement(getValueAt(k,m));

               if(data.size() == getColumnCount())
               {
                  //moving up
                  if((startRange > destination) && (data.size() != 0))
                  {
                     deleteRow(k);
                     insertRow(data,destination);
                     startRange++;
                     destination++;
                  }
                  //moving down
                  else if ((startRange < destination) && data.size() != 0)
                  {
                     //just insert rows we will clean up later
                     insertRow(data, destination);
                     destination++;
                  }
               }
            }
         }
      }
      //if we moved down delete already moved rows from the old location
      if(startRange < destination )
      {
         for (int j =0; j < getRowCount(); j++)
            if(j >= startRange && j <= endRange)
              deleteRow(startRange);
      }
   }

   /**
    * Moves selected range of rows one row down
    * @param start a leading row index, if gets here a valid row index
    * @param end an  index of the last selected row, if gets here a valid row
    * index.
    */
    private void moveOneRowDown(int start, int end)
    {
      Vector rowData = new Vector();
      for(int i = 0; i < getColumnCount(); i++)
            rowData.addElement(getValueAt((end+1), i));
          if(rowData.size() != 0)
          {
            deleteRow(end+1);
            insertRow(rowData, start);
          }

    }

   /**
   * Insert empty row above the given position.
   *
   * @param      pos   the row position
    */
   public void insertRow(int pos) throws IllegalArgumentException
   {
     if ((pos < 0) || (pos > getRowCount()))
       throw new IllegalArgumentException("The passed row has an invalid size.");

     for (int i=0; i<getColumnCount(); i++)
    {
       getColumn(i).insertElementAt("", pos);
    }

    fireTableDataChanged();
   }

   /**
   * Insert provided row above the given position.
   *
   * @param      pos   the row position
    */
   public void insertRow(Vector rowData, int pos) throws IllegalArgumentException
   {
     if ((pos < 0) || (pos > getRowCount()))
       throw new IllegalArgumentException("The passed row has an invalid size.");

    if (rowData.size() != getColumnCount())
       throw new IllegalArgumentException("The row size is not valid for this table.");

     for (int i=0; i<getColumnCount(); i++)
    {
       getColumn(i).insertElementAt(rowData.elementAt(i), pos);
    }

    fireTableDataChanged();
   }

   /**
   * Delete row of the given position.
   *
   * @param      pos   the row position
    */
   public void deleteRow(int pos) throws IllegalArgumentException
   {
     if ((pos < 0) || (pos > getRowCount()))
       throw new IllegalArgumentException("The passed row has an invalid size.");

     for (int i=0; i<getColumnCount(); i++)
    {
       getColumn(i).removeElementAt(pos);
    }

    fireTableDataChanged();
   }

   /**
   * Get the table data
   *
   * @return   Vector   the table data
    */
   public Vector getTable()
   {
    return m_table;
   }

   /**
   * Get the column name
   *
   * @param column   the column index
   * @return String   the column name
   */
   public String getColumnName(int column) throws IllegalArgumentException
  {
     if ((column < 0) || (column > getColumnCount()))
       throw new IllegalArgumentException("The table column index is out of bounds.");

    return (String) m_headers.elementAt(column);
  }

  /**
   * Set the column name
   *
   * @param String   the column name
   * @param column   the column index
   */
   public void setColumnName(String name, int column) throws IllegalArgumentException
  {
     if ((column < 0) || (column > getColumnCount()))
       throw new IllegalArgumentException("The table column index is out of bounds.");

    m_headers.setElementAt(name, column);
  }

   /**
   * Get the column class
   *
   * @param column   the column index
   * @return Class      the column class
   */
   public Class getColumnClass(int column) throws IllegalArgumentException
  {
   if ((column < 0) || (column > getColumnCount()))
      throw new IllegalArgumentException("The table column index is out of bounds.");

   return getColumn(column).getClass();
  }

   /**
   * Enable cell editor for all cells.
   *
   * @param row            the row index
   * @param col            the column index
   * @return boolean   true if editable
   */
   public boolean isCellEditable(int row, int col)
  {
   return ( -1 == m_readOnlyStartRow || row < m_readOnlyStartRow || ( -1 != m_readOnlyEndRow
         && row > m_readOnlyEndRow ));
  }

   /**
    * A very simple mechanism to partially write protect the table. isCellEditable
    * will return <code>false</code> for any row in the supplied range,
    * inclusive.
    *
    * @param startRow The first row in the range. Rows start at 1. If -1 is
    * supplied, no rows will be set read only.
    *
    * @param endRow The last row in the range, inclusive. If -1 is passed in,
    * this means to the end of the table.
    *
    * @throws IllegalArgumentException if startRow is <= 0 if it is not -1,
    * or endRow is less than startRow, except if it is -1.
   **/
   public void setReadOnly( int startRow, int endRow )
   {
      if ( (startRow <= 0 && startRow != -1) || ( startRow > endRow && endRow != -1 ))
         throw new IllegalArgumentException();

      m_readOnlyStartRow = startRow;
      m_readOnlyEndRow = endRow;
   }


   /**
    * Checks all rows/cols in the table for the presence of any data, even if
    * rows containing data are preceded by blank rows.
    *
    * @return <code>true</code> if there is currently any data in the table,
    * <code>false</code> otherwise.
   **/
   public boolean hasData()
   {
      boolean bHasData = false;
      int rows = getRowCount();
      for ( int i = 0; i < rows && !bHasData; ++i )
      {
         if ( !isRowEmpty(i))
            bHasData = true;
      }
      return bHasData;
   }

   /**
   * Set the idexed table entry with the given value.
   *
   * @param value         the new value
   * @param row            the row index
   * @param col            the column index
   */
    public void setValueAt(Object value, int row, int column) throws IllegalArgumentException
  {
     if ((column < 0) || (column >= getColumnCount()))
       throw new IllegalArgumentException("The table column " + new Integer(column).toString() + "index is out of bounds.");

     if ((row < 0) || (row >= getRowCount()))
       throw new IllegalArgumentException("The passed row " + new Integer(row).toString() + "has an invalid size.");

    getColumn(column).setElementAt(value, row);
   }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * the table headers
   */
  protected Vector m_headers = null;
  /**
   * the table
   */
   private Vector m_table = null;

   /**
    * These variables indicate the range of rows that are read only in this
    * model. If m_readOnlyStartRow == -1, no rows are r/o. If m_readOnlyEndRow
    * is -1, it signifies the last in the model. The following rules should always
    * hold (OR'd):
    * <UL>
    * <LI>m_readOnlyStartRow = -1</LI>
    * <LI>m_readOnlyStartRow > 0 && m_readOnlyendRow == -1</LI>
    * <LI>m_readOnlyEndRow >= m_readOnlyStartRow</LI>
    * </UL>
   **/
   private int m_readOnlyStartRow = -1;
   private int m_readOnlyEndRow = -1;

}


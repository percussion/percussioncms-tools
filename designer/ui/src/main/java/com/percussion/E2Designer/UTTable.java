/*[ UTTable.java ]*************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.io.Serializable;
import java.util.Vector;

/**
 * Generic 2D table providing a vector of columns containing a vector of rows.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTTable implements Serializable
{
  /**
   * Construct an empty table with the number of columns provided.
   *
   * @params columns the number of table columns
   */
   //////////////////////////////////////////////////////////////////////////////
   public UTTable(int columns)
   {
      create(columns);
   }

  /**
   * Construct a table of provided info.
   *
   * @param table the table
   */
   //////////////////////////////////////////////////////////////////////////////
   public UTTable(Vector table)
   {
      create(table.size());
        copy(table);
   }

  /**
   * Create a new table.
   *
   */
   //////////////////////////////////////////////////////////////////////////////
   private void create(int columns)
   {
     m_table = new Vector(columns);
    for (int i=0; i<columns; i++)
       m_table.addElement(new Vector(1));
   }

  /**
   * Log table to screen
   *
   */
   //////////////////////////////////////////////////////////////////////////////
   public void log()
   {
     System.out.println("");
     System.out.println("");
     for (int i=0; i<getColumnCount(); i++)
       System.out.print("column " + i + "\t");
    System.out.println("");
      System.out.println("--table start-----------------------------------------------------------------");

    for (int j=0; j<getRowCount(); j++)
    {
       Vector row = getRow(j);
        for (int i=0; i<getColumnCount(); i++)
      {
            System.out.print(row.elementAt(i).toString() + "\t");
      }
       System.out.println("");
    }
       System.out.println("--table end-------------------------------------------------------------------");
  }

  /**
   * Copies all the cell data in the passed in table into this table. All existing
   * data in this table is lost.
   *
   * @param table a vector of table columns, the number of cols must match the
   * number of cols in this table
   *
   * @throws   IllegalArgumentException   if table is not balanced (all cols 
   * having same # of rows) or if the number of cols in table does not match 
   * number of cols in this table.
   */
   //////////////////////////////////////////////////////////////////////////////
   public void copy(Vector table) throws IllegalArgumentException
   {
      if ( table.size() != m_table.size())
         throw new IllegalArgumentException();
      // remove all existing columns
      m_table.clear();

     // save the row size from the first column, so we are able to check if the
    // provided table is balanced
     int rowSize = ((Vector) table.elementAt(0)).size();
     for (int col=0; col<table.size(); col++)
    {
       if (((Vector) table.elementAt(col)).size() != rowSize)
       {
         clear();
         throw new IllegalArgumentException("Table is not balanced");
      }

      Vector column =  (Vector)table.elementAt(col);
      m_table.addElement(table.elementAt(col));
    }
   }

  /**
   * Get the addressed row
   *
   * @return Vector      the row
   */
   //////////////////////////////////////////////////////////////////////////////
   public Vector getRow(int index)
   {
     if (m_table.isEmpty())
       return null;

    Vector row = new Vector(getColumnCount());
    for (int i=0; i<getColumnCount(); i++)
       row.addElement(((Vector) m_table.elementAt(i)).elementAt(index));

     return row;
  }

  /**
   * Get number of rows.
   *
   * @return int      the number of rows
   */
   //////////////////////////////////////////////////////////////////////////////
   public int getRowCount()
   {
     if (m_table.isEmpty())
       return 0;

     return ((Vector) m_table.elementAt(0)).size();
  }

  /**
   * Get number of columns.
   *
   * @return int      the number of columns
   */
   //////////////////////////////////////////////////////////////////////////////
   public int getColumnCount()
   {
     return m_table.size();
  }

  /**
   * Get table cell.
   *
   * @param    row         row index
   * @param    column      column index
   * @return Object      the contents of the addressed table cell
   */
   //////////////////////////////////////////////////////////////////////////////
   public Object getCell(int row, int column)
   {
     return ((Vector) m_table.elementAt(column)).elementAt(row);
  }

  /**
   * Clear the table.
   *
   */
   //////////////////////////////////////////////////////////////////////////////
   public void clear()
   {
     for (int col=0; col<m_table.size(); col++)
    {
       Vector column = (Vector) m_table.elementAt(col);
      if (column != null)
          column.clear();
    }
  }

  /**
   * Add the provided row to the table end.
   *
   * @param row      the row vector
   */
   //////////////////////////////////////////////////////////////////////////////
   public void addRow(Vector row)
   {
     if (row.size() != getColumnCount())
       throw new IllegalArgumentException("Invalid row for this table");

    for (int i=0; i<row.size(); i++)
    {
       Vector column = (Vector) m_table.elementAt(i);
      column.addElement(row.elementAt(i));
    }
  }

  /**
   * Remove indexed table row.
   *
   * @param index   the index of row to be removed
   */
   //////////////////////////////////////////////////////////////////////////////
   public void removeRow(int index)
   {
    for (int col=0; col<getColumnCount(); col++)
    {
       Vector column = (Vector) m_table.elementAt(col);
      column.removeElementAt(index);
    }
  }

   //////////////////////////////////////////////////////////////////////////////
  /**
   * the table
   */
   private Vector m_table = null;
}


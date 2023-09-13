/******************************************************************************
 *
 * [ PSCatalogTableModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultColumn;
import com.percussion.deployer.catalog.PSCatalogResultSet;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The table model which can construct the table from catalog resultset. Uses 
 * the column metadata for the names of the columns and supported classes for 
 * those columns in the table and results as rows.
 */
public class PSCatalogTableModel  extends AbstractTableModel
{
   /**
    * Constructs the table model from the supplied catalog result set.
    * 
    * @param catalogResultSet the resultset that provides the data for this 
    * table model, may not be <code>null</code> and must have column metadata.
    * 
    * @throws IllegalArgumentException if <code>catalogResultSet</code> is 
    * invalid.
    */
   public PSCatalogTableModel(PSCatalogResultSet catalogResultSet)
   {
      if(catalogResultSet == null)
         throw new IllegalArgumentException("catalogResultSet may not be null");
      
      if(catalogResultSet.getColumns() == null)
         throw new IllegalArgumentException(
            "catalogResultSet must have columns");       
      
      setTableData(catalogResultSet);
   }
   
   /**
    * Sets the column names and data rows from this result set.
    * 
    * @param catalogResultSet the resultset that provides the data for this 
    * table model, may not be <code>null</code> and must have column metadata.
    * 
    * @throws IllegalArgumentException if <code>catalogResultSet</code> is 
    * invalid.
    */
   public void setTableData(PSCatalogResultSet catalogResultSet)
   {
      if(catalogResultSet == null)
         throw new IllegalArgumentException("catalogResultSet may not be null");
      
      if(catalogResultSet.getColumns() == null)
         throw new IllegalArgumentException(
            "catalogResultSet must have columns");   
            
      m_columns = catalogResultSet.getColumns();
      
      m_data.clear();
      Iterator iter = catalogResultSet.getResults();
      while(iter.hasNext())
         m_data.add(iter.next());
         
      fireTableChanged(new TableModelEvent(this));
   }
   
   /**
    * Gets the number of columns in this table model.
    * 
    * @return the number of columns, never <code>0</code>
    */
   public int getColumnCount() 
   {
        return m_columns.length;
   }
   
   /**
    * Gets the number of data rows in this table model.
    * 
    * @return the number of rows, may be <code>0</code>
    */
   public int getRowCount()
   {
      return m_data.size();
   }
   
   /**
    * Gets the column name for the supplied column index.
    * 
    * @param col the column index of name to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @return the column name, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if column index is invalid.
    */
   public String getColumnName(int col)
   {
      checkColumn(col);       
      return m_columns[col].getName();
   }
   
   /**
    * Gets the class of the column data which will be used by <code>JTable
    * </code> to provide a default renderer/editor to the column data.
    * 
    * @param col the column index of name to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @return the column class, never <code>null</code>
    * 
    * @throws IllegalArgumentException if column index is invalid.
    */
   public Class getColumnClass(int col)
   {
      checkColumn(col);            
      return m_columns[col].getColumnClass();
   }

   /**
    * Gets the value at specified row and column.
    * 
    * @param row the row index of value to get, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index of value to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public Object getValueAt(int row, int col)
   {
      checkRow(row);
      checkColumn(col); 
            
      PSCatalogResult data = (PSCatalogResult)m_data.get(row);
      Object[] dataColumns = data.getColumns();
      return dataColumns[col];
   }
   
   /**
    * Gets the id of the supplied cataloged result row.
    * 
    * @param row the row index of id to get, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * 
    * @return the id string, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if row index is invalid.
    */
   public String getID(int row)
   {
      checkRow(row);
      
      return ((PSCatalogResult)m_data.get(row)).getID();
   }
   
   /**
    * Gets the display name of the supplied cataloged result row.
    * 
    * @param row the row index of id to get, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * 
    * @return the display name, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if row index is invalid.
    */
   public String getDisplayName(int row)
   {
      checkRow(row);
      
      return ((PSCatalogResult)m_data.get(row)).getDisplayText();
   }
   
   /**
    * Checks that the supplied row exists in this model.
    * 
    * @param row the row index to check, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * 
    * @throws IllegalArgumentException if row index is invalid.
    */
   private void checkRow(int row)
   {
      if(row < 0 || row >= getRowCount())
         throw new IllegalArgumentException("row must be between 0 and " + 
            (getRowCount()-1) + "inclusive");   
   }
   
    /**
     * Checks that the supplied column exists in this model.
     * 
     * @param col the column index to check, must be >= 0 and less than 
     * {@link #getRowCount() rowcount} of this model.
     * 
     * @throws IllegalArgumentException if column index is invalid.
     */
   private void checkColumn(int col)
   {
      if(col < 0 || col >= getColumnCount())
         throw new IllegalArgumentException("col must be between 0 and " + 
            (getColumnCount()-1) + "inclusive");     
   }

   /**
    * The array of <code>PSCatalogResultColumn</code>s that represents the table
    * structure, initialized in the constructor and may be modified by call to 
    * <code>setTableData(PSCatalogResultSet)</code>. Never <code>null</code> 
    * after that.
    */  
   private PSCatalogResultColumn[] m_columns = null;
   
   /**
    * The list of <code>PSCatalogResult</code>s that represents the data rows in
    * the table, initialized to empty list and gets updated in <code>
    * setTableData(PSCatalogResultSet)</code>. Never <code>null</code>.
    */
   private List m_data = new ArrayList();
}

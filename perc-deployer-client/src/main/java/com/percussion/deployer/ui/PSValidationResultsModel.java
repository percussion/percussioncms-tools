/******************************************************************************
 *
 * [ PSValidationResultsModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSValidationResult;
import com.percussion.deployer.objectstore.PSValidationResults;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Table model for validation results.
 */
public class PSValidationResultsModel extends AbstractTableModel
{
   /**
    * Table model for validation results.
    * @param results may be <code>null</code>.
    * @param allowEdit if <code>true</code> the 'Actions' column in table is
    * editable for the rows that allows skipping.
    */
   public PSValidationResultsModel(PSValidationResults results,
    boolean allowEdit)
   {
      m_allowEdit = allowEdit;
      getResources();
      loadColumnNames();
      setData(results);
   }

   /**
    * Sets data for this model.
    * @param results may be <code>null</code>.
    */
   public void setData(PSValidationResults results)
   {
      m_list = new ArrayList();
      if(results != null)
      {
         Iterator iter = results.getResults();
         while(iter.hasNext())
            m_list.add(iter.next());
      }
      m_filteredList = new ArrayList(m_list.size());
      clearFilters();
   }
   
   /**
    * Filters on the message column with the supplied message.  Match is on the
    * column value, case-sensitive. 
    * 
    * @param message The message to match, may not be <code>null</code> or 
    * empty. Only rows with this value in the message column will be available
    * in the model.
    */
   public void applyMessageValueFilter(String message)
   {
      if (message == null || message.trim().length() == 0)
         throw new IllegalArgumentException("message may not be null or empty");
      
      m_filteredList.clear();
      Iterator results = m_list.iterator();
      while (results.hasNext())
      {
         PSValidationResult result = (PSValidationResult) results.next();
         if (result.getMessage().equals(message))
            m_filteredList.add(result);
      }
      
      fireTableDataChanged();
   }
   
   /**
    * Filters on the error column based on the supplied value.
    * 
    * @param isError <code>true</code> to make only error rows available in the
    * model, <code>false</code> to make only warning rows available.
    */
   public void applyErrorFilter(boolean isError)
   {
      m_filteredList.clear();
      Iterator results = m_list.iterator();
      while (results.hasNext())
      {
         PSValidationResult result = (PSValidationResult) results.next();
         if (result.isError() ^ !isError)
            m_filteredList.add(result);
      }
      
      fireTableDataChanged();
   }
   
   /**
    * Clears all current filters.
    */
   public void clearFilters()
   {
      m_filteredList.clear();
      m_filteredList.addAll(m_list);
      fireTableDataChanged();
   }
   
   /**
    * Determines if any data is shown.
    * 
    * @return <code>true</code> if some rows match the current filter, 
    * <code>false</code> if the current filter hides all data.
    */
   public boolean hasVisibleData()
   {
      return !m_filteredList.isEmpty();
   }

   /**
    * Load column names from the resource properties file.
    */
   private void loadColumnNames()
   {
      if(ms_colNames.isEmpty())
      {
         ms_actionColumn = getResourceString("action");
         ms_colNames.add(getResourceString("dependency"));
         ms_colNames.add(getResourceString("type"));
         ms_colNames.add(getResourceString("include"));
         ms_colNames.add(getResourceString("message"));
         ms_colNames.add(getResourceString("warnErr"));
         ms_colNames.add(getResourceString("action"));
      }
   }

   /**
    * Gets resource file name for this class.
    *
    * @return resource file name, never <code>null</code> or empty.
    */
   protected String getResourceName()
   {
      return getClass().getName() + "Resources";
   }

   /**
    * Utility method to retrieve model's dialog box's ResourceBundle.
    *
    * @return ResourceBundle, may be <code>null</code>.
    */
   protected ResourceBundle getResources()
   {
      try
      {
         if ( null == ms_res )
            ms_res = ResourceBundle.getBundle( getResourceName(),
                       Locale.getDefault() );
      }
      catch(MissingResourceException mre)
      {
         System.err.println(this.getClass());
      }
      return ms_res;
   }

   /**
    * Gets the value for a key in the resource bundle.
    *
    * @param name key for the string whoes value is required. May not to be
    * <code>null</code>.
    * @return value of the key supplied, never <code>null</code>, if the value
    * doesn't exist key itself is returned.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   private String getResourceString(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");
      return PSDeploymentResources.getResourceString(ms_res, name);
   }

   /**
    * Gets the number of columns in the table.
    * @return number of columns
    */
   public int getColumnCount()
   {
      return ms_colNames.size();
   }

   /**
    * Gets the number of rows in the table. A minimum of <code>MIN_ROWS</code>
    * are returned.
    * 
    * @return number of rows.  The value returned is affected by any changes to
    * the filters applied.
    */
   public int getRowCount()
   {
      if(m_filteredList.size() < MIN_ROWS)
         return MIN_ROWS;
      else
         return m_filteredList.size();
   }

   /**
    * Gets the name of the column specified by its column number.
    * @param col number of the column whoes name is being sought, must be >= 0
    * and less than {@link #getRowCount() rowcount}
    * @return  name of the the column. Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if column index is invalid.
    */
   public String getColumnName(int col)
   {
      checkColumn(col);
      return (String)ms_colNames.get(col);
   }

   /**
    * Gets the data at the specified row and column number.
    * @param row row number, must be >= 0 and less than
    * {@link #getRowCount() rowcount}
    * @param col column number, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    * @return data at <code>row</code> and <code>col</code>, may be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if column or row index is invalid.
    */
   public Object getValueAt(int row, int col)
   {
      checkRow(row);
      checkColumn(col);

      if(row < m_filteredList.size())
      {
         PSValidationResult result = (PSValidationResult)m_filteredList.get(
            row);

         switch(col)
         {
            case 0:
               return result.getDependency().getDisplayIdentifier();

            case 1:
               return result.getDependency().getDependencyTypeName();

            case 2:
               if (result.getDependency().isIncluded())
                  return getResourceString("depIncluded");
               else
                  return getResourceString("nodepIncluded");

            case 3:
               return result.getMessage();

            case 4:
               if (result.isError())
                  return getResourceString("error");
               else
                  return getResourceString("warning");

            case 5:
               if(result.allowSkip())
               {
                  if(m_allowEdit)
                  {
                     if(result.isSkip())
                        return Boolean.TRUE;
                     else
                        return Boolean.FALSE;
                  }
                  else
                  {
                     if(result.isSkip())
                        return getResourceString("skip");
                     else
                        return getResourceString("install");
                  }
               }
               else
                  return getResourceString("none");
         }
      }
      return null;
   }

   /**
    * Specifies whether a cell is editable or not.
    * Checks if the column is 'Actions' column, <code>m_allowEdit</code> is
    * <code>true</code> and (@link PSValidationResult#allowSkip()) returns
    * <code>true</code>, then only allows editing.
    * @param row row number of the cell, must be >= 0 and less than
    * {@link #getRowCount() rowcount}
    * @param col column number of the cell, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    * @return <code>true</code> if editable else <code>false</code>.
    *
    * @throws IllegalArgumentException if column or row index is invalid.
    */
   public boolean isCellEditable(int row, int col)
   {
      checkRow(row);
      checkColumn(col);

      String colName = getColumnName(col);

      if(row < m_filteredList.size())
      {
         PSValidationResult result = (PSValidationResult)m_filteredList.get(
            row);

         if (colName.equals(ms_actionColumn) && m_allowEdit &&
             result.allowSkip())
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Checks if the cell is editable or not by calling
    * <code>isCellEditable(int, int)</code> and if the <code>value</code> is
    * an instance of <code>Boolean</code> then only allows the  data at the
    * specified cell location to be edited.
    * @param value data to be edited, assumed not to be <code>null</code>.
    * @param row row number of the cell, must be >= 0 and less than
    * {@link #getRowCount() rowcount}
    * @param col column number of the cell, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    *
    * @throws IllegalStateException if the cell is not editable.
    * @throws IllegalArgumentException if <code>value</code> is
    * <code>null</code>.
    */
   public void setValueAt(Object value, int row, int col)
   {
      if (value == null)
         throw new IllegalArgumentException(
            "Value to be updated cannot be null");

      if(isCellEditable(row, col) && value instanceof Boolean)
      {
         PSValidationResult result = (PSValidationResult)m_filteredList.get(
            row);
         result.skipInstall(((Boolean)value).booleanValue());
      }
      else
         throw new IllegalStateException(
               "requested cell is not editable, can not update");
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
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private  static ResourceBundle ms_res;

   /**
    * Set in the constructor, specifies whether to allow editing of the
    * 'Actions' column. <code>true</code> if allowed else <code>false</code>.
    * Not modified after construction.
    */
   private boolean m_allowEdit = false;

   /**
    * List holding <code>PSValidationResult</code> objects.
    * Initialised in <code>setData()</code>.
    * Never <code>null</code>. Everytime <code>setData()</code> is called, it is
    * initialised to reflect new data for the model.
    */
   private List m_list;
   
   /**
    * List of filtered results.  Holds some subset of the contents of 
    * {@link #m_list}.  List is intialized in 
    * {@link #setData(PSValidationResults)} to hold all results, and is modfied
    * when any of the filter methods are called.
    */
   private List m_filteredList;

   /**
    * Represents editable 'Actions' column. Initialised in
    * <code>loadColumnNames()</code>, never <code>null</code> after that.
    */
   public static String ms_actionColumn;

   /**
    * Minimum number of rows to be dispalyed.
    */
   public static final int MIN_ROWS = 17;

   /**
    * List holding column names in the same order as they appear in the table.
    * Names are set in <code>loadColumnNames()</code>, Even if it is not loaded
    * properly the keys will be used as column names.  Never <code>null</code>.
    * or modified.
    */
   private static List ms_colNames = new ArrayList();
}

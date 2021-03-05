/*[ PSTwoColumnModel.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.util.PSMapPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;

/**
 * A generic table model for a two column table. The model assumes the first
 * and second column as a name-value pair. However that doesn't limit it to just
 * that, it can be used in any way, the internal representation of a row is an
 * object {@link PSMapPair} which has two fields corresponding to two columns.
 * <code>getName()</code> and <code>getValue()</code> gets the first and second
 * column repectively for a row.
 */
public class PSTwoColumnModel extends AbstractTableModel
{
   /**
    * Creates the table model. Loads resource bundle, column names and
    * intializes <code>m_list</code>. Sets <code>m_modelName</code> to DEFAULT.
    */
   public PSTwoColumnModel()
   {
      this(null, null, DEFAULT_ROWS);
   }

   /**
    * Constructs the table model with the number of ros supplied.
    *
    * @param rows the number of rows to create, must be >= 1.
    * @throws IllegalArgumentException if the number of rows is invalid.
    */
   public PSTwoColumnModel(int rows)
   {
      this(null, null, rows);
   }

   /**
    * Creates the table model. Loads resource bundle, column names and
    * intializes <code>m_list</code>.
    *
    * @param modelName model name to be able to load column names properly. If
    * it is <code>null</code> or empty default is assumed.
    */
   public PSTwoColumnModel(String modelName)
   {
      this(modelName, null, DEFAULT_ROWS);
   }

   /**
    * Constructs the table model with the providedd column names and the number
    * of rows supplied.
    *
    * @param columnNames the column names to display, must be an array of
    *    <code>DEFAULT_COLUMNS</code> or <code>null</code>.
    * @param rows the number of rows to create, must be >= 1.
    * @throws IllegalArgumentException if the number of column names or rows is
    *    invalid.
    */
   public PSTwoColumnModel(String[] columnNames, int rows)
   {
      this(null, columnNames, rows);
   }

   /**
    * Constructs the table model for the supplied name and with the provided
    * number of rows.
    *
    * @param modelName the model name, defaults to <code>DEFAULT</code> if
    *    <code>null</code> or empty.
    * @param columnNames the column names to display, must be an array of
    *    <code>DEFAULT_COLUMNS</code> or <code>null</code>.
    * @param rows the number of rows to create, must be >= 1.
    * @throws IllegalArgumentException if the number of rows is invalid.
    */
   public PSTwoColumnModel(String modelName, String[] columnNames, int rows)
   {
      if (columnNames != null && columnNames.length != DEFAULT_COLUMNS)
         throw new IllegalArgumentException(
            "must provide an array of " + DEFAULT_COLUMNS);

      if (rows < 1)
         throw new IllegalArgumentException("rows must be >= 1");

      if (modelName == null || modelName.length() == 0)
         m_modelName = DEFAULT;
      else
         m_modelName = modelName;

      m_rowCount = rows;

      getResources();
      loadColumnNames(columnNames);
      init();
   }

   /**
    * Initializes <code>m_list</code> with {@link PSNVPair} objects, with name
    * and value strings in the object intialised to empty string.
    */
   private void init()
   {
      for(int k = 0; k < m_rowCount; k++)
      {
         m_list.add(new PSMapPair("", ""));
      }
   }

   /**
    * Resets the model with new data supplied. First the existing entries in
    * <code>m_list</code> are reset to an empty string and then initialized
    * with the supplied data.
    *
    * @param list represents the data with which the model will be
    * initialized, may be <code>null</code>. It can contain <code>String
    * </code> objects only.
    */
   private void reset(List list)
   {
      for(int k = 0; k < m_rowCount; k++)
      {
         m_list.set(k, new PSMapPair("", ""));
      }
      //FB:  RpC_REPEATED_CONDITIONAL_TEST NC 1-17-16
      if (list != null && list.size() != 0)
      {
         int k = list.size();
         for (int m = 0; m < k; m++)
         {
            m_list.set(m, list.get(m));
         }
      }
   }

   /**
    * Sets data for this model.
    *
    * @param list may be <code>null</code>.
    */
   public void setData(List list)
   {
      if (list == null || list != m_list)
         reset(list);
      fireTableDataChanged();
   }

   /**
    * Gets the list of {@link PSNVPair} objects.
    *
    * @return list may be empty, but never <code>null</code>.
    */
   public List getList()
    {
       return m_list;
    }

   /**
    * Load column names from the resource properties file.
    *
    * @param columnNames the columns names, may be <code>null</code> in
    *    which case the predefined names of the model name will be used.
    */
   private void loadColumnNames(String[] columnNames)
   {
      if (columnNames == null)
      {
         m_columnNames = new String[DEFAULT_COLUMNS];
         if (m_modelName.equals(FILTER))
         {
            m_columnNames[0] = getResourceString("column.name");
            m_columnNames[1] = getResourceString("column.filter");
         }
         else if (m_modelName.equals(EXTENSION))
         {
            m_columnNames[0] = getResourceString("column.name");
            m_columnNames[1] = getResourceString("column.extensions");
         }
         else if (m_modelName.equals(DEFAULT))
         {
            m_columnNames[0] = getResourceString("column.name");
            m_columnNames[1] = getResourceString("column.value");
         }
      }
      else
         m_columnNames = columnNames;
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
      return PSContentLoaderResources.getResourceString(ms_res, name);
   }

   /**
    * Gets the number of columns in the table.
    * @return number of columns
    */
   public int getColumnCount()
   {
      return DEFAULT_COLUMNS;
   }

   /**
    * Gets the number of rows in the table.
    * @return number of rows.
    */
   public int getRowCount()
   {
      return m_list.size();
   }

   /**
    * Gets the name of the column specified by its column number.
    * @param col number of the column whoes name is being sought, must be >= 0
    * and less than {@link #getColumnCount() colcount}
    * @return  name of the the column. Never <code>null</code> or empty.
    *
    * @throws IndexOutOfBoundsException if column index is invalid.
    */
   public String getColumnName(int col)
   {
      checkColumn(col);
      return m_columnNames[col];
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
    * @throws IndexOutOfBoundsException if column or row index is invalid.
    */
   public Object getValueAt(int row, int col)
   {
      checkRow(row);
      checkColumn(col);

      if(row < m_list.size())
      {
         PSMapPair nv = (PSMapPair)m_list.get(row);
         switch(col)
         {
            case 0:
               return nv.getKey();

            case 1:
               return nv.getValue();
         }
      }
      return null;
   }

   /**
    * Specifies whether a cell is editable or not.
    * @param row row number of the cell, must be >= 0 and less than
    * {@link #getRowCount() rowcount}
    * @param col column number of the cell, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    * @return <code>true</code> if editable else <code>false</code>.
    *
    * @throws IndexOutOfBoundsException if column or row index is invalid.
    */
   public boolean isCellEditable(int row, int col)
   {
      checkRow(row);
      checkColumn(col);
      if (m_modelName.equals(DEFAULT) && col == 0)
         return false;
      return true;
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
    * @throws IndexOutOfBoundsException if <code>value</code> is
    * <code>null</code>.
    */
   public void setValueAt(Object value, int row, int col)
   {
      if (value == null)
         throw new IllegalArgumentException(
            "Value to be updated cannot be null");

      if(isCellEditable(row, col))
      {
         // gem: does not work!!!
         PSMapPair nv = (PSMapPair)m_list.get(row);
         String colName = getColumnName(col);
         if (nv == null)
         {
            PSMapPair nvObj = new PSMapPair("", "");
            m_list.add(row, nvObj);

            if (col == 0)
               nvObj.setKey((String)value);
            else
               nvObj.setValue((String)value);
         }
         else
         {
            if (col == 0)
               nv.setKey((String)value);
            else
               nv.setValue((String)value);
         }
      }
      fireTableDataChanged();
   }

   /**
    * Checks that the supplied row exists in this model.
    *
    * @param row the row index to check, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    *
    * @throws IndexOutOfBoundsException if row index is invalid.
    */
   private void checkRow(int row)
   {
     if(row < 0 || row >= getRowCount())
        throw new IndexOutOfBoundsException("row must be between 0 and " +
           (getRowCount()-1) + "inclusive");
   }

   /**
   * Checks that the supplied column exists in this model.
   *
   * @param col the column index to check, must be >= 0 and less than
   * {@link #getRowCount() rowcount} of this model.
   *
   * @throws IndexOutOfBoundsException if column index is invalid.
   */
   private void checkColumn(int col)
   {
     if(col < 0 || col >= getColumnCount())
        throw new IndexOutOfBoundsException("col must be between 0 and " +
           (getColumnCount()-1) + "inclusive");
   }

   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private  static ResourceBundle ms_res;

   /**
    * List holding <code>PSMapPair</code> objects. Initialized in
    * <code>setData()</code>. Never <code>null</code>. Everytime <code>setData()
    * </code> is called, it is Initialized to reflect new data for the model.
    */
   private List m_list = new ArrayList();

   /**
    * The default number of columns displayed.
    */
   public static final int DEFAULT_COLUMNS = 2;

   /**
    * The number of rows for which this model is initialized. Initialized in
    * constructor, defaults to <code>DEFAULT_ROWS</code>.
    */
   private int m_rowCount = DEFAULT_ROWS;

   /**
    * Maximum default number of rows to be displayed.
    */
   public static final int DEFAULT_ROWS = 100;

   /**
    * An array of column names as displayed.
    */
   private String[] m_columnNames = null;

   /**
    * Name of the model for loading the column names properly. Initialized in
    * the ctor. Never <code>null</code> or empty or modified after that.
    * Default value is <code>DEFAULT</code>.
    */
   private String m_modelName;

   /**
    * Model name when it is an extensions table with two editable column - 'Name'
    * and 'Extensions'.
    */
   public static final String EXTENSION = "extension";

   /**
    * Model name when it is a filters table with two editable column - 'Name'
    * and 'Filter'.
    */
   public static final String FILTER = "filter";

   /**
    * Model name when it is a default table with two editable column - 'Name'
    * and 'Value'.
    */
   public static final String DEFAULT = "default";
}

/*[ PSSingleColumnModel.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for 'Transition' table. This is a single column editable
 * table. Each row is rendered as a combo box containing a list of work flow
 * actions.
 */
public class PSSingleColumnModel extends AbstractTableModel
{
   /**
    * Creates the table model. Loads resource bundle, column names and
    * intialises <code>m_list</code>. Sets <code>m_modelName</code> to DEFAULT.
    */
   public PSSingleColumnModel()
   {
      m_modelName = DEFAULT;
      getResources();
      loadColumnNames();
      init();
   }

   /**
    * Creates the table model. Loads resource bundle, column names and
    * intialises <code>m_list</code>.
    *
    * @param modelName model name to be able to load column names properly. If
    * it is <code>null</code> or empty default is assumed.
    *
    */
   public PSSingleColumnModel(String modelName)
   {
      if (modelName == null || modelName.length() == 0)
         m_modelName = DEFAULT;
      else
         m_modelName = modelName;
      getResources();
      loadColumnNames();
      init();
   }

   /**
    * Initializes <code>m_list</code> to a list of empty strings.
    */
   private void init()
   {
      for(int k = 0; k < MIN_ROWS; k++)
      {
         m_list.add(k, "");
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
      if ( null == ms_res )
         ms_res = ResourceBundle.getBundle( getResourceName(),
               Locale.getDefault() );

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
    * Resets the model with new data supplied. First the existing entries in
    * <code>m_list</code> are reset to an empty string and then initialized
    * with the supplied data.
    *
    * @param list represents the data with which the model will be
    * initialized, may be <code>null</code>. It can contain <code>String
    * </code> objects only.
    */
   public void reset(List list)
   {
      for(int k = 0; k < MIN_ROWS; k++)
      {
         m_list.set(k, "");
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
    * @param list may be <code>null</code>.
    */
   public void setData(List list)
   {
      if (list == null || list != m_list)
         reset(list);
      fireTableDataChanged();
   }

   /**
    * Load column names from the resource properties file.
    */
   private void loadColumnNames()
   {
      if(ms_colTransitionNames.isEmpty() && m_modelName.equals(TRANSITION))
         ms_colTransitionNames.add(getResourceString("column.transition"));
      else if (ms_colExtensionsNames.isEmpty() && m_modelName.equals(EXTENSIONS))
         ms_colExtensionsNames.add(getResourceString("column.extensions"));
   }

   /**
    * Gets the data for the model as a list.
    *
    * @return list of strings, never <code>null</code> or empty. It is
    * initialized with empty strings.
    */
   public List getData()
   {
      return m_list;
   }

   /**
    * Gets the number of columns in the table.
    * @return number of columns
    */
   public int getColumnCount()
   {
      return 1;
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
    * @param col number of the column whoes name is being sought, must be
    * >= 0 and less than {@link #getRowCount() rowcount}
    * @return  name of the the column. Never <code>null</code> or empty.
    *
    * @throws IndexOutOfBoundsException if column index is invalid.
    */
   public String getColumnName(int col)
   {
      checkColumn(col);
      if (m_modelName.equals(TRANSITION))
         return (String)ms_colTransitionNames.get(col);
      else if (m_modelName.equals(EXTENSIONS))
         return (String)ms_colExtensionsNames.get(col);
      else
         return " ";
   }

   /**
    * Gets the data at the specified row and column number. The objects are
    * of type <code>String</code> always.
    *
    * @param row row number, must be >= 0 and less than
    * {@link #getRowCount() rowcount}
    *
    * @param col column number, must be >= 0 and less than
    * {@link #getColumnCount() colCount} of this model.
    *
    * @return data at <code>row</code> and <code>col</code>, Never <code>null
    * </code>, may be empty.
    *
    * @throws IndexOutOfBoundsException if column or row index is invalid.
    */
   public Object getValueAt(int row, int col)
   {
      checkRow(row);
      checkColumn(col);
      return (String)m_list.get(row);
   }

   /**
    * Specifies whether a cell is editable or not.
    * @param row row number of the cell, must be >= 0 and less than
    * {@link #getRowCount() rowcount}
    * @param col column number of the cell, must be >= 0 and less than
    * {@link #getColumnCount() colcount} of this model.
    * @return <code>true</code> if editable else <code>false</code>.
    *
    * @throws IndexOutOfBoundsException if column or row index is invalid.
    */
   public boolean isCellEditable(int row, int col)
   {
      checkRow(row);
      checkColumn(col);
      return true;
   }

   /**
    * Checks if the cell is editable or not by calling
    * <code>isCellEditable(int, int)</code> then only allows the  data at the
    * specified cell location to be edited.
    *
    * @param value data to be edited, assumed not to be <code>null</code>.
    *
    * @param row row number of the cell, must be >= 0 and less than
    * {@link #getRowCount() rowcount}
    *
    * @param col column number of the cell, must be >= 0 and less than
    * {@link #getColumnCount() colcount} of this model.
    *
    * @throws IndexOutOfBoundsException <code>row</code> or <code>col</code>
    * are out of range.
    * @throws IllegalArgumentException if <code>value</code> is
    * <code>null</code>.
    */
   public void setValueAt(Object value, int row, int col)
   {
      if (value == null)
         throw new IllegalArgumentException(
               "Value to be updated cannot be null");

      if(isCellEditable(row, col))
      {
         m_list.set(row, (String)value);
         fireTableCellUpdated(row, col);
      }
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
    * {@link getColumnCount() colcount} of this model.
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
    * List holding empty strings. Initialised to empty strings <code>init()
    * </code>. Never <code>null</code>. Everytime <code>setData()</code>
    * is called, it is initialized to reflect new data for the model.
    */
   private List m_list = new ArrayList();

   /**
    * Maximum number of rows supported by the model.
    */
   public static final int MIN_ROWS = 100;

   /**
    * List holding column name for transition table in the same order as they
    * appear in the table.
    * Names are set in <code>loadColumnNames()</code>, Even if it is not loaded
    * properly the keys will be used as column names.  Never <code>null</code>.
    * or modified.
    */
   private static List ms_colTransitionNames = new ArrayList();

   /**
    * List holding column name for extensions table in the same order as they
    * appear in the table.
    * Names are set in <code>loadColumnNames()</code>, Even if it is not loaded
    * properly the keys will be used as column names.  Never <code>null</code>.
    * or modified.
    */
   private static List ms_colExtensionsNames = new ArrayList();
   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;

   /**
    * Name of the model for loading the column names properly. Initialised in
    * the ctor. Never <code>null</code> or empty or modified after that.
    * Default value is <code>DEFAULT</code>.
    */
   private String m_modelName;

   /**
    * Model name when it is an extensions table with singlr editable column -
    * 'Extensions'.
    */
   public static final String EXTENSIONS = "extensions";

   /**
    * Model name when it is a filters table with Single editable column -
    * 'Transition'.
    */
   public static final String TRANSITION = "transition";

   /**
    * Model name when it is a default table with single editable column with no
    * column name specified.
    */
   public static final String DEFAULT = " ";
}

/******************************************************************************
 *
 * [ ResultPagerTableModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSSortedColumn;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.util.PSCollection;
import com.percussion.guitools.IPSCreateModelItem;
import com.percussion.guitools.PSEditTableModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author DougRand
 *
 * This is the specific model subclass for the results pager. This model 
 * implements two true columns plus one dummy column for use as a row tab.
 */
public class ResultPagerTableModel extends PSEditTableModel
   implements IPSCreateModelItem
{
   /**
    * Define column positions for this model. Modifying these values will
    * rearrange the columns in the UI
    */
   public final static int COLUMN_ROW_SELECT_TAB = 0;
   public final static int COLUMN_BACKEND_COLUMN_NAME = 1;
   public final static int COLUMN_SORT_ORDER = 2;

   /**
    * This is the expected count of entries in the column array. This
    * must be maintained with the enumerated column positions above.
    */
   public final static int COLUMN_ARRAY_LENGTH = 3;

   /**
    * This value is initialized as part of the constructor. These names are
    * passed back and will be used by the table as column headers. Note that
    * the above enumerated values are indecies into this array, and the length
    * of the array absolutely must match or there is something wrong. See 
    * argument checking on the constructor.
    */
   private String m_colNames[];
   
   /**
    * This specifies a default table object to use when adding rows to the
    * model. This is set in the constructor and is
    * never modified afterward, but may be null for a dummy
    * table model.
    */
   private PSBackEndColumn m_defaultTableColumn = null;
   
   
   /**
    * This is set on creation with a list of all of the PSBackEndColumn
    * objects known by the given dataset. The key into this hashmap is the
    * formatted column name as created by formatColumnName. Entries
    * are passed on creation.
    */
   private Map m_knownTables = new HashMap();

   /**
    * Create a dummy table model. This table model only serves to act
    * as a placeholder and may not be used actively. Replace this table
    * model before displaying the table. 
    * 
    * @param colNames The header names for the table, which must not
    * be <code>null</code> or zero length
    */
   public ResultPagerTableModel(String[] colNames)
   {
      super(new PSCollection(PSSortedColumn.class)); // Dummy
      
      if (colNames == null || colNames.length == 0)
      {
         throw new IllegalArgumentException("colNames may not be null or empty");
      }
      
      m_colNames = colNames;
   }

   /**
    * Create a new ResultPagerTableModel.
    * @param rows The initial data for the model, which must not be 
    * <code>null</code> but may be empty.
    * @param colNames The header names for the table, which must not
    * be <code>null</code> or zero length
    * @param knownColumns A collection of known column objects of type
    * PSBackEndColumn or a subclass. Must not be <code>null</code> and
    * may not be zero length.
    */
   public ResultPagerTableModel(PSCollection rows, String[] colNames, 
                                 PSCollection columns)
   {
      super(rows);
      
      if (rows == null)
      {
         throw new IllegalArgumentException("rows may not be null");
      }
      
      if (colNames == null || colNames.length == 0)
      {
         throw new IllegalArgumentException("colNames may not be null or empty");
      }
      
      if (columns == null || columns.size() == 0)
      {
         throw new IllegalArgumentException("columns may not be null or empty");
      }
      
      if (colNames.length != COLUMN_ARRAY_LENGTH)
      {
         throw new IllegalArgumentException(
            "The length of the column names array must be "
               + COLUMN_ARRAY_LENGTH);
      }

      m_colNames = colNames;
      
      m_defaultTableColumn = (PSBackEndColumn) columns.iterator().next();
      
      // Create hashmap
      for (Iterator iter = columns.iterator(); iter.hasNext();)
      {
         PSBackEndColumn element = (PSBackEndColumn) iter.next();
         String key = formatColumnName(element);
         m_knownTables.put(key, element);
      }
   }

   /**
    * This method returns the specific class for the given column.
    * This drives a number of things including the default table
    * display format in JTable. Several classes are defined for
    * correct built-in behavior, and custom renderers can be added
    * to extend this behavior. 
    * 
    * @see javax.swing.table.TableModel#getColumnClass(int)
    */
   public Class getColumnClass(int arg0)
   {
      // Note that for this first column, the value in the table is a String,
      // but the editor actually returns a PSSortedColumn, which is quite 
      // convenient
      if (arg0 == COLUMN_BACKEND_COLUMN_NAME)
      {
         return String.class;
      }
      else if (arg0 == COLUMN_SORT_ORDER)
      {
         return Boolean.class;
      }
      else
      {
         // Any other columns are treated as strings. This can be extended if
         // more information should be shown in the table
         return String.class;
      }
   }

   /**
    * @see javax.swing.table.TableModel#getColumnCount()
    */
   public int getColumnCount()
   {
      // Fixed count for this model
      return 3;
   }

   /**
    * This returns the appropriate column name from the array
    * passed in the constructor. Note that there will be a runtime
    * exception if there is a disconnect between the model&apos;s 
    * declared count of columns, and the column name array.
    * 
    * @see javax.swing.table.TableModel#getColumnName(int)
    */
   public String getColumnName(int column)
   {
      // This method is called by the JTable implementation 
      // to obtain header names
      return m_colNames[column];
   }

   /**
    * This method extracts the information in the model into a 
    * form suitable for displaying in the table. The types of 
    * these values must be synchronized between this method
    * in the getColumnClass method. 
    * 
    * @see javax.swing.table.TableModel#getValueAt(int, int)
    */
   public Object getValueAt(int row, int column)
   {
      PSSortedColumn col = (PSSortedColumn) m_rows.get(row);

      if (column == COLUMN_BACKEND_COLUMN_NAME)
      {
         return formatColumnName(col);
      }
      else if (column == COLUMN_SORT_ORDER)
      {
         return Boolean.valueOf(col.isAscending());
      }
      else
      {
         // Show nothing in the row tab column
         return "";
      }
   }

   /**
    * Take a column and format it for both friendly display and
    * later parsing. 
    * 
    * @param col the column object, which may not be <code>null</code>
    * @return a formatted string that shows the column in a user 
    * readable fashion.
    */
   public static String formatColumnName(PSBackEndColumn col)
   {      
      return col.getValueDisplayText();
   }

   /**
    * For this model, all columns with the exception of the special
    * row selection column are editable.
    * 
    * @see javax.swing.table.TableModel#isCellEditable(int, int)
    */
   public boolean isCellEditable(int row, int column)
   {
      // XXX Auto-generated method stub
      return column != COLUMN_ROW_SELECT_TAB;
   }

   /**
    * This method translates from the value contained in the given
    * table element and the model. An unexpected benefit is that
    * for the backend column table column, the values passed back
    * from the table are actually a PSSortedColumn. While the value
    * cannot be used directly, it can be copied from. For the other
    * currently supported column, the Boolean value is adequate.
    * 
    * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
    */
   public void setValueAt(Object arg0, int row, int column)
   {
      PSSortedColumn col = (PSSortedColumn) m_rows.get(row);
      
      try
      {
         if (column == COLUMN_BACKEND_COLUMN_NAME)
         {
            processColumnData(col, (String) arg0);
         }
         else if (column == COLUMN_SORT_ORDER)
         {
            Boolean value = (Boolean) arg0;
            col.setAscending(value.booleanValue());
         }
      }
      catch (PSIllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.guitools.IPSCreateModelItem#createInstance()
    */
   public Object createInstance() throws InstantiationException
   {
      return new PSSortedColumn(m_defaultTableColumn, true);
   }
   
   /**
    * Process the new value from the combo box. The original data came 
    * from Rhythmyx via the formatColumnName method, and so should not
    * require any validation
    * 
    * @param col the column to update, may not be <code>null</code>
    * @param string the new value to use, may not be <code>null</code>
    */
   private void processColumnData(PSSortedColumn col, String string) 
   throws PSIllegalArgumentException
   {
      if (col == null)
      {
         throw new IllegalArgumentException("col may not be null");
      }
      
      if (string == null)
      {
         throw new IllegalArgumentException("string may not be null");
      }

      PSBackEndColumn column = (PSBackEndColumn) m_knownTables.get(string);
      
      if (column == null)
      {
         throw new IllegalArgumentException("string must be a known column");
      }

      // Copy lookup information into cell
      col.setTable(column.getTable());
      col.setAlias(column.getAlias());
      col.setColumn(column.getColumn());
   }


}

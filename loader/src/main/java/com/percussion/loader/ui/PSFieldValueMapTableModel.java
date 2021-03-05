/*[ PSFieldValueMapTableModel.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.objectstore.PSFieldProperty;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class PSFieldValueMapTableModel extends AbstractTableModel
{

   /**
    * Contructs a new <code>PSFieldValueMapTableModel</code>
    *
    * @param table the table that owns this model. Never <code>null</code>.
    * @param allowsXpath if set to <code>true</code>, the Xpath value type
    * will appear as a selection in the value type drop down lists.
    */
   public PSFieldValueMapTableModel(PSFieldValueMapTable table,
      boolean allowsXpath)
   {
     if(null == table)
        throw new IllegalArgumentException("Table cannot be null.");

     m_table = table;
     setXPathTypeAllowed(allowsXpath);
     if (null == ms_res)
        ms_res = ResourceBundle.getBundle(
           getClass().getName() + "Resources", Locale.getDefault() );
     addMinimumRows();
   }

   /**
    * Loads initial catalog data into the table model
    * @param fields iterator of {@link com.percussion.loader.ui.PSContentField}
    * objects. May not be <code>null</code>.
    */
   public void load(Iterator fields)
   {
      if(null == fields)
         throw new IllegalArgumentException(
            "ContentField iterator cannot be null");
      m_fieldList.clear();
      while(fields.hasNext())
         m_fieldList.add(fields.next());

   }

   /**
    * Validates this table using the following rules:
    * <ul>
    *    <li>All rows must have a value type</li>
    *    <li>All rows except literal must have a value</li>
    *    <li>If variable then must start with $</li>
    *    <li>Number value type must be a valid number</li>
    * </ul>
    * @return <code>true</code> if all data in the table is valid.
    */
   public boolean validate()
   {
      Iterator it = m_data.iterator();

      if(m_table.isEditing())
         m_table.stopCellEditing();

      ValueMapRow row = null;
      int index = 1;
      try
      {
         while(it.hasNext())
         {
            row = (ValueMapRow)it.next();
            if(row.getField() != null)
            {
               String fieldName =
                  ((PSContentField)row.getField()).getFieldName();

               // All rows must have a value type
               if(row.getValueType() == null
                  || row.getValueType().trim().length() == 0)
                  throw new RowValidationException(
                     index, fieldName, VALIDATION_ERROR_NO_VALUE_TYPE);

               // All rows except literal must have a value
               if(!row.getValueType().equalsIgnoreCase(
                  PSFieldProperty.VALUE_TYPE_LITERAL)
                  && (row.getValue() == null
                     || row.getValue().trim().length() == 0))
                  throw new RowValidationException(
                     index, fieldName, VALIDATION_ERROR_NO_VALUE);

               // If variable then must start with $
               if(row.getValueType().equalsIgnoreCase(
                  PSFieldProperty.VALUE_TYPE_VARIABLE)
                  && !row.getValue().trim().startsWith("$"))
                  throw new RowValidationException(
                     index, fieldName, VALIDATION_ERROR_NOT_A_VARIABLE);

               // If number then it must be a number
               if(row.getValueType().equalsIgnoreCase(
                  PSFieldProperty.VALUE_TYPE_NUMBER))
               {
                  try
                  {
                     Integer.parseInt(row.getValue());
                  }
                  catch(NumberFormatException nfe)
                  {
                     throw new RowValidationException(
                        index, fieldName, VALIDATION_ERROR_NOT_A_NUMBER);
                  }
               }
           }
         }
         index++;
       }
       catch(RowValidationException ve)
       {
          String message = "";
          Object[] args = {ve.getFieldName(), String.valueOf(ve.getRowIndex())};
          switch(ve.getProblemId())
          {
             case VALIDATION_ERROR_NO_VALUE:
                message = ms_res.getString("error.validation.no.value");
                break;

             case VALIDATION_ERROR_NO_VALUE_TYPE:
                message = ms_res.getString("error.validation.no.value.type");
                break;

             case VALIDATION_ERROR_NOT_A_NUMBER:
                message = ms_res.getString("error.validation.not.a.number");
                break;

             case VALIDATION_ERROR_NOT_A_VARIABLE:
                message = ms_res.getString("error.validation.not.a.variable");
                break;
          }

          ErrorDialogs.showErrorDialog(m_table,
             MessageFormat.format(message, args),
             ms_res.getString("error.title.validationexception"),
             JOptionPane.ERROR_MESSAGE);
          return false;
       }

      return true;
   }

   /**
    * Returns data from the table model
    *
    * @return iterator of <code>PSFieldProperty</code> objects
    * from this table model. Never <code>null</code>, may be empty.
    */
   public Iterator getData()
   {
      List data = new ArrayList();
      Iterator it = m_data.iterator();
      while(it.hasNext())
      {
          ValueMapRow row = (ValueMapRow)it.next();
          if(null != row.getField())
             data.add(new PSFieldProperty(
                row.getField().getFieldName(),
                row.getValue(),
                row.getValueType()));
      }
      return data.iterator();
   }

   /**
    * Returns data from the table model as a List
    *
    * @return List of <code>PSFieldProperty</code> objects
    * from this table model. Never <code>null</code>, may be empty.
    */
   public List getDataList()
   {
      List data = new ArrayList();
      Iterator it = getData();
      while(it.hasNext())
         data.add(it.next());
      return data;
   }

   /**
    * Sets the data for this table model.
    * @param data iterator of <code>PSFieldProperty</code> objects.
    * May not be <code>null</code>.
    */
   public void setData(Iterator data)
   {
      if(null == data)
         throw new IllegalArgumentException(
            "Data iterator cannot be null");
      m_data.clear();
      while(data.hasNext())
      {
         PSFieldProperty prop = (PSFieldProperty)data.next();
         PSContentField field = findContentField(prop.getName());
         if(null != field)
            m_data.add(
               new ValueMapRow(field, prop.getValueType(), prop.getValue()));

      }

      addMinimumRows();
      fireTableDataChanged();
   }

   /**
    * Adds empty rows so table contains the minimum rows
    * required as defined in MIN_ROWS.
    */
   private void addMinimumRows()
   {
      for(int i = m_data.size(); i < MIN_ROWS; i++)
         m_data.add(new ValueMapRow());
   }

   /**
    * Removes row in the table
    *
    * @param row the row index of the row to be removed
    */
   public void removeRow(int row)
   {
      if(row < 0 || row >= getRowCount())
         return;
      m_data.remove(row);
      addMinimumRows();
      fireTableDataChanged();
   }

   /**
    * Returns boolean value indicating that the table cell specified is
    * editable.
    *
    * @param row the table cell row coordinate
    *
    * @param col the table cell column coordinate
    *
    * @return <code>true</code> if editable, else <code>false</code>
    */
   public boolean isCellEditable(int row, int col)
   {
      return true;
   }


   // implements TableModel interface method
   public Object getValueAt(int row, int col)
   {
      ValueMapRow theRow = (ValueMapRow)m_data.get(row);
      switch(col)
      {
         case COL_FIELD:
            return theRow.getField();
         case COL_VALUE_TYPE:
            return theRow.getValueType();
         case COL_VALUE:
            return theRow.getValue();
      }
      return null;
   }

   // implements TableModel interface method
   public void setValueAt(int row, int col, Object value)
   {
      ValueMapRow theRow = (ValueMapRow)m_data.get(row);
      switch(col)
      {
         case COL_FIELD:
            theRow.setField((PSContentField)value);
            break;
         case COL_VALUE_TYPE:
            theRow.setValueType((String)value);
            break;
         case COL_VALUE:
            theRow.setValue((String)value);
            break;
      }
   }

   // implements TableModel interface method
   public int getRowCount()
   {
      return m_data.size();
   }

   // implements TableModel interface method
   public int getColumnCount()
   {
      return 3;
   }

   // implements TableModel interface method
   public String getColumnName(int col)
   {
      switch(col)
      {
         case COL_FIELD:
            return ms_res.getString("field.label.field");
         case COL_VALUE_TYPE:
            return ms_res.getString("field.label.valueType");
         case COL_VALUE:
            return ms_res.getString("field.label.value");
      }
      return "";
   }


   /**
    * Sets xpath value type allowed flag. Default is <code>false</code>.
    *
    * @param allowed, if set to <code>true</code>, the Xpath value type
    * will appear as a selection in the value type drop down lists.
    */
   public void setXPathTypeAllowed(boolean allowed)
   {
      m_xpathTypeAllowed = allowed;
   }

   /**
    * Returns flag indicating that XPath value types
    * are allowed.
    * @return <code>true</code> if XPath value type is allowed.
    */
   public boolean isXPathTypeAllowed()
   {
      return m_xpathTypeAllowed;
   }

   /**
    * Returns an iterator of fields that have not yet been selected
    * @return iterator of <code>PSContentField</code> objects. Never
    * <code>null</code>.
    */
   public Iterator getAvailableFields()
   {
      List fields = new ArrayList();
      Iterator it = m_fieldList.iterator();
      while(it.hasNext())
      {
         PSContentField contentField = (PSContentField)it.next();
         if(!dataContainsField(contentField))
            fields.add(contentField);
      }
      return fields.iterator();
   }

   /**
    * Determines if a content field with the same name is contained in
    * the data list.
    *
    * @param field the content field to search for. May be
    * <code>null</code>.
    *
    * @return <code>true</code> if the field is contained within the
    * data list.
    */
   private boolean dataContainsField(PSContentField field)
   {
      if(null == field)
         return false;
      Iterator it = m_data.iterator();
      while(it.hasNext())
      {
         ValueMapRow row = (ValueMapRow)it.next();
         if(null != row && null != row.getField() &&
            row.getField().getFieldName().equals(field.getFieldName()))
            return true;
      }
      return false;
   }

   /**
    * Finds the <code>PSContentField</code> with the specified name from
    * the field list and returns it.
    *
    * @param name the name of the content field to retrieve. May not
    * be <code>null</code>.
    *
    * @return the <code>PSContentField</code> object if found, else
    * returns <code>null</code>
    */
   private PSContentField findContentField(String name)
   {
      if(null == name)
         throw new IllegalArgumentException(
            "The content field name cannot be null");

      Iterator it = m_fieldList.iterator();
      while(it.hasNext())
      {
         PSContentField field = (PSContentField)it.next();
         if(field.getFieldName().equals(name))
            return field;
      }
      return null;

   }

   /**
    * Inner class to represent a row in the field value map table
    */
   class ValueMapRow
   {

      /**
       * Constructs an empty row
       */
      public ValueMapRow()
      {
          this(null, "", "");
      }

      /**
       * Constructs a new row.
       *
       * @param field the <code>PSContentField</code> object for this row. May
       * be <code>null</code>.
       *
       * @param valueType the value type string for this row. Never
       * <code>null</code>, may be empty.
       *
       * @param value the value string for this row. Never
       * <code>null</code>, may be empty.
       */
      public ValueMapRow(PSContentField field, String valueType, String value)
      {
         setField(field);
         setValueType(valueType);
         setValue(value);
      }

      /**
       * Returns the content field for this row.
       * @return the <code>PSContentField</code> for this row,
       * May be <code>null</code>.
       */
      public PSContentField getField()
      {
         return m_field;
      }

      /**
       * Sets the content field for this row.
       * @param field the <code>PSContentField</code> for this row,
       * May be <code>null</code>.
       */
      public void setField(PSContentField field)
      {
         m_field = field;
      }

      /**
       * Returns the value type for this row.
       * @return the value type string. Never <code>null</code>, may be
       * empty.
       */
      public String getValueType()
      {
         return m_valueType;
      }

      /**
       * Sets the value type for this row
       * @param value the value. Never <code>null</code> may be empty.
       */
      public void setValueType(String valueType)
      {
         if(null == valueType)
            throw new IllegalArgumentException("Value type cannot be null.");

         m_valueType = valueType;
      }

      /**
       * Returns the value for this row.
       * @return the value string. Never <code>null</code>, may be
       * empty.
       */
      public String getValue()
      {
         return m_value;
      }

      /**
       * Sets the value for this row
       * @param value the value. Never <code>null</code> may be empty.
       */
      public void setValue(String value)
      {
         if(null == value)
            throw new IllegalArgumentException("Value cannot be null.");

         m_value = value;
      }

      /**
       * The content field for this row. May be <code>null</code>.
       */
      private PSContentField m_field;

      /**
       * The value type for this row. Never <code>null</code>, may be empty.
       */
      private String m_valueType = "";

      /**
       * The value type for this row. Never <code>null</code>, may be empty.
       */
      private String m_value = "";
   }

   /**
    * Inner class to handle a row validation exception
    */
   class RowValidationException extends Exception
   {
      RowValidationException(int rowIndex, String fieldName, int problemId)
      {
         m_rowIndex = rowIndex;
         m_fieldName = fieldName;
         m_problemId = problemId;
      }

      public int getRowIndex()
      {
         return m_rowIndex;
      }

      public String getFieldName()
      {
         return m_fieldName;
      }

      public int getProblemId()
      {
         return m_problemId;
      }

      private int m_rowIndex;
      private String m_fieldName;
      private int m_problemId;


   }

   /**
    * Field list passed in. Never <code>null</code>
    * after that.
    */
   private List m_fieldList = new ArrayList();

   /**
    * List of data for this table model. Never <code>null</code>.
    */
   private List m_data = new ArrayList();

   /**
    * Reference to the table taht owns this model. Set in Ctor.
    * Never <code>null</code> after that.
    */
   private PSFieldValueMapTable m_table;

   /**
    * Flag indicating that the Xpath value type should appear as a selection
    * in a value type drop down list. Defaults to <code>false</code>.
    */
   private boolean m_xpathTypeAllowed = false;

   /**
    * Resource bundle for this class. Initialized once in {@link #init()},
    * never <code>null</code> after that.
    */
   protected static ResourceBundle ms_res;

   /**
    *  Validation error - No Value Type defined
    */
   public static final int VALIDATION_ERROR_NO_VALUE_TYPE = 1;

   /**
    *  Validation error - No Value defined
    */
   public static final int VALIDATION_ERROR_NO_VALUE = 2;

   /**
    *  Validation error - Not a Number
    */
   public static final int VALIDATION_ERROR_NOT_A_NUMBER = 3;

   /**
    *  Validation error - Not a Variable
    */
   public static final int VALIDATION_ERROR_NOT_A_VARIABLE = 4;

   /**
    * Index of the field column
    */
   public static final int COL_FIELD = 0;

   /**
    * Index of the value type column
    */
   public static final int COL_VALUE_TYPE = 1;

   /**
    * Index of the value column
    */
   public static final int COL_VALUE = 2;

   /**
    * Minimum rows this table model can have.
    */
   public static final int MIN_ROWS = 30;
}
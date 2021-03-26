/*[ ParameterNameValueTable.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

/**
 * A table with two columns:  param name and value.
 */
public class ParameterNameValueTable extends UTJTable
{
   /**
    * Initializes an empty <code>ParameterNameValueTable</code> object with
    * the specified number of rows.
    *
    * @param numberOfRows minimum (and default) number of rows in the table
    */
   public ParameterNameValueTable(int numberOfRows)
   {
      setMinRows( numberOfRows );
      setNameEditor( new DefaultCellEditor( new JTextField() ) );
      setValueEditor( new DialogMenuCellEditor() );

      // enter key should accept edits in the table, not close dialog
      addKeyListener(new KeyAdapter()
      {
         public void keyReleased (KeyEvent e)
         {
            if (e.getKeyCode() == KeyEvent.VK_ENTER)
               if (isEditing())
                  getCellEditor().stopCellEditing();
         }
      });
   }


   /**
    * @return new UTTableModel with the correct column headers
    */
   protected TableModel createDefaultDataModel()
   {
      return new UTTableModel( new Vector( Arrays.asList( HEADERS ) ) );
   }


   /**
    * Removes the first selected row from the table and adds an empty row to
    * the end of the table.
    */
   public void removeSelectedRows()
   {
      int selectedRow = getSelectedRow();
      if (selectedRow > -1)
      {
         // removing rows in the process of being edited is bad idea
         if (isEditing())
            getCellEditor().stopCellEditing();

         UTTableModel model = (UTTableModel) getModel();
         model.deleteRow(selectedRow);
         model.appendRow();
      }
   }


   /**
    * Sets the model used for the parameter name/value table, and updates all
    * the table's UI settings. Called externally to allow editing the current
    * CE control's settings. Called internally whenever the CE control is
    * changed.
    *
    * @param model a two-column UTTableModel with column names "Param name" and
    *              "Value", or <code>null</code> to create a new, empty model.
    * @throws IllegalArgumentException if model is invalid
    */
   public void setModel(TableModel model)
   {
      if (null == model)
      {
         // create a new, empty model
         model = createDefaultDataModel();
      }
      // else validate the model
      else if (model.getColumnCount() != 2 ||
            !(model.getColumnName( 0 ).equals( HEADERS[0] )) ||
            !(model.getColumnName( 1 ).equals( HEADERS[1] )) ||
            !(model instanceof UTTableModel) )
      {
         throw new IllegalArgumentException( "An invalid model was provided" );
      }

      // make sure we've got the minimum number of rows
      while (model.getRowCount() < getMinRows())
      {
         ((UTTableModel)model).appendRow();
      }

      super.setModel( model );     
      createDefaultColumnsFromModel(); // prevents selection column from 
                                       // applying itself twice
      
      configureBehavior();
   }


   /**
    * Adds a dialog to the value columns action button
    * @param dialog the dialog; cannot be <code>null</code>
    * @param label the label to use for the menu item that will cause
    * the dialog to be displayed; cannot be <code>null</code> or empty
    * 
    * @throws IllegalArgumentException if any parameter is <code>null</code> or
    * <code>label</code> is empty.
    */
   public void addValueDialog(PSDialogAPI dialog, String label)
   {
      if (null == dialog)
         throw new IllegalArgumentException("dialog cannot be null");
      if (null == label || label.trim().length() == 0)
         throw new IllegalArgumentException("label cannot be null or empty");

      m_valueEditor.addDialog( dialog, label );
   }


   /**
    * Sets the selection mode to a single row, sets the cell editors for each
    * column, and enables the selection column.  This method should be called 
    * whenever the model is changed.
    */
   protected void configureBehavior()
   {
      setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      // need to set cell editors before enabling selectionColumn
      getColumn( HEADERS[0] ).setCellEditor( m_nameEditor );
      getColumn( HEADERS[1] ).setCellEditor( m_valueEditor );
         
      setSelectionColumn( true );
   }


   /**
    * Converts the table model into a PSCollection of PSParam objects by
    * assigning the name column to the label and the value column to the value.
    * String values are converted to PSTextLiteral because PSParam requires
    * an IPSReplacementValue.  Rows that do not have both a name and a value
    * are excluded.
    *
    * @return PSCollection of PSParam objects; never <code>null</code>; may
    * be empty
    */
   public PSCollection getParameters()
   {
      PSCollection parameters = new PSCollection( PSParam.class );
      TableModel model = getModel();
      for (int row = 0; row < model.getRowCount(); row++)
      {
         // is there a name and value for this row?
         String label = null;
         if (model.getValueAt( row, 0 ) != null)
            label = model.getValueAt( row, 0 ).toString();

         Object valueObj = model.getValueAt( row, 1 );
         IPSReplacementValue value = convert( valueObj );

         if (label != null && label.trim().length() > 0 && value != null)
         {
            PSParam param = new PSParam( label, value );
            parameters.add( param );
         }
      }
      return parameters;
   }

   
   /**
    * Converts the supplied object (usually from a table cell) into an
    * <code>IPSReplacementValue</code>.  Strings become
    * <code>PSTextLiteral</code>s.
    * 
    * @param valueObj the object to convert; if <code>null</code> method will
    * return null.
    * 
    * @return valueObj converted to IPSReplacementValue, or
    * <code>null</code> if valueObj cannot be converted, or if valueObj is
    * <code>null</code>
    */ 
   protected IPSReplacementValue convert(Object valueObj)
   {
      IPSReplacementValue value = null;
      
      if (valueObj instanceof String)
      {
         // only counts if the string isn't empty
         String valueText = (String) valueObj;
         if (valueText.trim().length() != 0)
            value = new PSTextLiteral( valueText );
      }
      else if (valueObj instanceof IPSReplacementValue)
         value = (IPSReplacementValue) valueObj;
      
      return value;
   }

   /**
    * Sets the table with a new model created from the PSParam objects obtained
    * through the specified Iterator.
    * @param parameters Iterator of PSParam objects to be assigned to the table.
    * If <code>null</code>, table will be empty.
    */
   public void setParameters(Iterator parameters)
   {
      Vector names = new Vector();
      Vector values = new Vector();
      Vector columns = new Vector( HEADERS.length );
      columns.add( names );
      columns.add( values );
      Vector columnNames = new Vector( HEADERS.length );
      columnNames.add( HEADERS[0] );
      columnNames.add( HEADERS[1] );

      if (parameters != null)
         while (parameters.hasNext())
         {
            PSParam param = (PSParam) parameters.next();
            names.add( extractName( param ) );
            values.add( extractValue( param ) );
         }
      setModel( new UTTableModel( columns, columnNames ) );
   }

   /**
    * Creates a representation for the supplied parameter suitable for assigning
    * to the name column by returning the String obtained by 
    * {@link PSParam#getName()}.
    * 
    * @param parameter a representation of this <code>PSParam</code>'s name
    * will be returned; assumed not <code>null</code>
    * @return A not <code>null</code> or empty String
    */ 
   protected Object extractName(PSParam parameter)
   {
      return parameter.getName();
   }
   
   
   /**
    * Returns the value of parameter, unless that value is a PSTextLiteral, in
    * which case it is converted into a String.
    * 
    * @param parameter a representation of this <code>PSParam</code>'s value
    * will be returned; assumed not <code>null</code>
    * @return String (possibly empty, never <code>null</code>) or 
    * IPSReplacementValue (never <code>null</code>)
    */ 
   protected Object extractValue(PSParam parameter)
   {
      IPSReplacementValue value = parameter.getValue();
      if (value instanceof PSTextLiteral)
         return value.getValueText();
      else
         return value;
   }

   /**
    * Adds the specified parameter as a new row at end of the table.
    * @param parameter contains the name and value to assign; may not be
    * <code>null</code>
    * @throws IllegalArgumentException if parameter is <code>null</code>
    */   
   public void addParameter(PSParam parameter)
   {
      if (null == parameter)
         throw new IllegalArgumentException("parameter may not be null");
      Vector row = new Vector();
      row.add( parameter.getName() );
      row.add( parameter.getValue() );
      ((UTTableModel)getModel()).appendRow( row );    
   }

   /**
    * Sets the editor that will be used with the name (first) column.
    * @param nameEditor editor to use; cannot be <code>null</code>
    * @throws IllegalArgumentException if nameEditor is <code>null</code>
    */ 
   protected void setNameEditor(TableCellEditor nameEditor)
   {
      if (null == nameEditor)
         throw new IllegalArgumentException("TableCellEditor cannot be null");
      m_nameEditor = nameEditor;
   }


   /**
    * Sets the editor that will be used with the value (second) column.
    * @param valueEditor editor to use; cannot be <code>null</code>
    * @throws IllegalArgumentException if nameEditor is <code>null</code>
    */ 
   protected void setValueEditor(DialogMenuCellEditor valueEditor)
   {
      if (null == valueEditor)
         throw new IllegalArgumentException("valueEditor cannot be null");
      m_valueEditor = valueEditor;
   }


   /**
    * Edits the name (first) column.  Set in {@link #setNameEditor}. Never
    * <code>null</code> after construction.
    */
   private TableCellEditor m_nameEditor;

   /**
    * Edits the value (second) column.  Set in {@link #setValueEditor}. Never
    * <code>null</code> after construction.
    */
   private DialogMenuCellEditor m_valueEditor;

   /**
    * Column identifiers (headers)
    */
   protected static final String[] HEADERS = {"Param name", "Value"};
}

/*[ ControlParameterTable.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSControlParameter;
import com.percussion.design.objectstore.PSDisplayTextLiteral;
import com.percussion.design.objectstore.PSParam;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A table used to manage the parameters for a content editor control.  It
 * consists of two columns:  param name and value.  The param name column is
 * edited by a combo box which lists the valid parameters for the active 
 * content editor control.  The value column is edited with a text field, 
 * unless the parameter specifies a choice list, in which case a combo box is
 * used.  The value column also contains a button which displays a pop-up menu
 * to select one of three dialogs: url request link, function properties, or 
 * value selector.
 */ 
public class ControlParameterTable extends ParameterNameValueTable
{
   /**
    * Initializes an empty <code>ControlParameterTable</code> object with
    * the specified number of rows.  The cell editor for the name column is
    * assigned to a combo box.  The cell editor for the value column is
    * assigned to a derived DialogMenuCellEditor that knows to display a combo
    * box when the control has a choice list.
    *
    * @param numberOfRows minimum (and default) number of rows in the table
    */
   public ControlParameterTable(int numberOfRows)
   {
      super( numberOfRows );
      initNameEditor();
      setValueEditor( new ValueCellEditor() );
   }
   
   
   /**
    * Creates a combo box and assigns it as the cell editor for the param name
    * column.
    */ 
   private void initNameEditor()
   {
      m_paramNameCombo = new PSComboBox();
      m_paramNameCellEditor = new DefaultCellEditor( m_paramNameCombo );
      setNameEditor( m_paramNameCellEditor );
   }


   /**
    * Sets the name column to a custom cell renderer that will display the
    * control parameter description as a tool tip, in addition to the
    * behavior of the super class.  This method should be called whenever the
    * model is changed.
    */
   protected void configureBehavior()
   {    
      DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
      {
         /** 
          * If the value assigned is a <code>PSControlParameter</code>, sets 
          * the tool tip for the cell to its description.
          */
         protected void setValue(Object value)
         {
            super.setValue( value );
            if (value instanceof PSControlParameter)
            {
               PSControlParameter parameter = (PSControlParameter) value;
               setToolTipText( parameter.getDescription() );
            }
            else
            {
               setToolTipText( null );
            }
         }
      };
      getColumn( HEADERS[0] ).setCellRenderer(renderer);
      super.configureBehavior();
   }


   /**
    * Sets the combo box of parameter names that is used by the parameter table
    * cell editor.
    *
    * @param names a not <code>null</code> array of objects to display as the
    *              combo box list.
    * @throws IllegalArgumentException if names is <code>null</code>
    */
   public void setParameterNames(Object[] names)
   {
      if (null == names)
         throw new IllegalArgumentException( "names array is null" );

      m_paramNameCombo.removeAllItems();
      for (int i = 0; i < names.length; i++)
      {
         m_paramNameCombo.addItem( names[i] );
      }
   }

   
   /**
    * Gets the object that represents the specified parameter.
    * 
    * @param parameterName the name of the parameter to return (match is
    * case-sensitive); cannot be <code>null</code> or empty.
    * @return PSParam if parameter is defined; <code>null</code> otherwise.
    * @throws IllegalArgumentException if parameterName is <code>null</code>
    * or empty.
    */ 
   public PSParam getParameter(String parameterName)
   {
      if (null == parameterName || 0 == parameterName.trim().length())
         throw new IllegalArgumentException(
               "parameterName cannot be null or empty");
      
      PSParam foundParam = null;
      for (Iterator iter = getParameters().iterator(); iter.hasNext();)
      {
         PSParam param = (PSParam) iter.next();
         if (param.getName().equals( parameterName ))
         {
            foundParam = param;
            break;
         }        
      }
      return foundParam;
   }

   /**
    * Inserts the specified parameter as the first row of the table if the
    * supplied parameter's name is found in the combo box of parameter names
    * used as the name column editor.
    * 
    * @param parameter the object to add to the table; may not be
    * <code>null</code>
    * @throws IllegalArgumentException if parameter is <code>null</code>
    */ 
   public void insertParameter(PSParam parameter)
   {
      if (null == parameter)
         throw new IllegalArgumentException("parameter may not be null");
      Object controlParam = extractName( parameter );
      if (controlParam != null)
      {
         Vector row = new Vector();
         row.add( controlParam );
         row.add( extractValue( parameter ) );
         ((UTTableModel)getModel()).insertRow( row, 0 );
      }
   }


   /**
    * Creates a representation for the supplied parameter suitable for assigning
    * to the name column by searching the control parameters combo box.
    * 
    * @param parameter a representation of this <code>PSParam</code>'s name
    * will be returned; assumed not <code>null</code>
    * @return Object from the parameter names combo box (usually a <code>
    * PSControlParameter</code>) whose string representation matches the name
    * of the supplied parameter, or <code>null</code> if no match.
    */ 
   protected Object extractName(PSParam parameter)
   {
      String paramName = parameter.getName();
      for (int i = 0; i < m_paramNameCombo.getModel().getSize(); i++)
      {
         Object controlParam = m_paramNameCombo.getModel().getElementAt( i );
         if (paramName.equals( controlParam.toString() ))
            return controlParam;
      }
      return null; // didn't find one
   }


   /**
    * If the valueObj is a PSControlParameter.Entry, it is converted into a 
    * PSDisplayTextLiteral.  Otherwise, delegates to the super-class.
    * 
    * @param valueObj object to be converted; if <code>null</code> method will
    * return null.
    * @return valueObj converted to IPSReplacementValue, or
    * <code>null</code> if valueObj cannot be converted, or if valueObj is
    * <code>null</code>
    */ 
   protected IPSReplacementValue convert(Object valueObj)
   {
      if (valueObj instanceof PSControlParameter.Entry)
      {
         PSControlParameter.Entry entry = (PSControlParameter.Entry) valueObj;
         return new PSDisplayTextLiteral(entry.getDisplayValue(),
            entry.getInternalName());
      }
      else return super.convert( valueObj );
   }
   
   
   /**
    * Displays the list of parameter names available for the currently selected
    * CE control. Set in {@link #initNameEditor}, and never <code>null</code>
    * after that.
    */
   private PSComboBox m_paramNameCombo;

   /**
    * Edits the name column of the parameter table with list of parameters
    * for the selected control. Set in {@link #initNameEditor}, and never
    * <code>null</code> after that.
    */
   private TableCellEditor m_paramNameCellEditor;
   
   /**
    * When the value column of a control parameter with a choice set is 
    * edited, use a combo box of those choices instead of the standard
    * editor.
    */ 
   public class ValueCellEditor extends DialogMenuCellEditor
   {
      public ValueCellEditor()
      {
         m_choicesCombo = new PSComboBox();
      }
      
      /**
       * Edits the cell with a combo box if the name column has defined a
       * choice list.  Otherwise, the editor defined by the super class is
       * used.
       * 
       * See interface for description of parameters
       */ 
      public Component getTableCellEditorComponent(JTable table, Object value,
                                                   boolean isSelected,
                                                   int row, int column)
      {
         // check the PSControlParameter in the name column for choices
         Object o = getValueAt(row, 0);
         if (o instanceof PSControlParameter)
         {
            PSControlParameter parameter = (PSControlParameter) o;
            if (parameter.getChoiceList() != null &&
                parameter.getChoiceList().size() != 0)
            {
               setChoices( parameter.getChoiceList() );
               selectCurrentValue( value );
               m_isChoices = true;
               return m_choicesCombo;
            }
         }
         
         // use normal editor when name column is not PSControlParameter
         // with choices
         m_isChoices = false;
         return super.getTableCellEditorComponent( table, value, isSelected, 
               row, column );
      }


      /**
       * Searches the choices combo box for a object with the same string
       * representation as the supplied <code>value</code>, and selects that
       * object.  If no match is found, the combo box selection is not changed.
       * 
       * @param value provides value to match against the combo box; assumed
       * not <code>null</code>
       */ 
      private void selectCurrentValue(Object value)
      {
         String matchValue = value.toString();
         for (int i = 0; i < m_choicesCombo.getModel().getSize(); i++)
         {
            Object o = m_choicesCombo.getModel().getElementAt( i );
            if (o.toString().equals( matchValue ))
            {
               m_choicesCombo.setSelectedIndex( i );
               break;
            }
         }        
      }
      
      
      /**
       * Gets the value from the editor to assign back to the table cell.  If
       * we are editing using the choices combo box, return the selected item.
       * Otherwise, delegate to the super class.
       *
       * @return the currently selected Object in the choices combo box, or
       * the super class result.  Never <code>null</code>.
       */
      public Object getCellEditorValue()
      {
         if (m_isChoices)
            // m_choicesCombo is never empty when used as the editor
            return m_choicesCombo.getSelectedItem();
         else
            return super.getCellEditorValue();
      }


      /**
       * Sets the combo box of control parameter choices.
       *
       * @param choices list of objects to display in the combo box.
       * Assumed not <code>null</code> or empty.
       */
      private void setChoices(List choices)
      {
         m_choicesCombo.removeAllItems();
         for (Iterator i = choices.iterator(); i.hasNext();)
         {
            m_choicesCombo.addItem( i.next() );          
         }
      }
      
      /** 
       * Contains the choices from a control parameter. Never <code>null</code>
       * after construction.  Never empty once assigned in <code>
       * getTableCellEditorComponent</code>
       */ 
      private PSComboBox m_choicesCombo;
      
      /** 
       * Remembers whether we are editing a cell with choices or not. Set in
       * <code>getTableCellEditorComponent</code>.
       */ 
      private boolean m_isChoices;
   }
}

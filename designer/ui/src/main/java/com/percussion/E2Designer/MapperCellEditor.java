/*******************************************************************************
 * $Id: MapperCellEditor.java 1.11 2001/06/21 19:22:06Z SyamalaKommuru Release $
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * Version Labels  : $Name: Pre_CEEditorUI RX_40_REL $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 * $Log: MapperCellEditor.java $
 * Revision 1.11  2001/06/21 19:22:06Z  SyamalaKommuru
 * Fix for the bug Rx-00-10-0038, changed this to extend ValueSelectorEditor instead of
 * MapperCell, so that there is consistency in the behavior of editing fields. 
 * Revision 1.10  2000/07/12 14:19:50Z  AlexTeng
 * Fixed a bug that did not set the newly edited UDF param values.
 *
 * Revision 1.9  2000/06/28 00:15:59Z  AlexTeng
 * Added code to support new UDF and extension model.
 *
 * Revision 1.8  2000/03/07 02:34:44Z  candidorodriguez
 * fixed bug Rx-00-03-0005
 * Revision 1.7  2000/02/09 19:18:40Z  candidorodriguez
 * tmp fix to allow rebuild ( using new extensions )
 *
 * Revision 1.6  1999/08/25 04:12:29Z  paulhoward
 * Click count to edit changed to 2 so it wouldn't be so hard to delete
 * rows.
 *
 * Revision 1.5  1999/08/14 19:26:43Z  martingenhart
 * several bugfixes, mapper changes to support CGI, etc.
 * cache all catalogs
 * Revision 1.4  1999/07/30 19:49:26  martingenhart
 * fixed mapper table editors
 * Revision 1.3  1999/06/15 16:02:12  martingenhart
 * fix mapper property dialog
 * Revision 1.2  1999/06/10 22:02:31  martingenhart
 * fix create UDFs
 * Revision 1.1  1999/06/09 23:35:08  martingenhart
 * Initial revision
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSExtensionParamValue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * This is text field editor panel for MapBrowserTable. It provides two sets of
 * editors(ValueSelectorDialog and FormulaPropertyDialog) depending on value
 * to edit.
 * It provides implementation for editing cell data of type
 * <code>OSExtensionCall</code>
 * It extends ValueSelectorEditor for implementation of data editing of type
 * <code>IPSReplacementValue</code>
 */

////////////////////////////////////////////////////////////////////////////////
public class MapperCellEditor extends ValueSelectorCellEditor
{

  /**
   * Construct the cell editor providing a reference to the editor dialog used
   * while the edit button was pressed.
   *
   * @param valueEditor the editor fo rall value types (DT...)
   * @param udfEditor the editor for UDF mappings
   */
  //////////////////////////////////////////////////////////////////////////////
   public MapperCellEditor( ValueSelectorDialog valueEditor,
                            FormulaPropertyDialog udfEditor )
   {
        super(valueEditor, null);

      m_udfEditor = udfEditor;
      m_clickCountToStart = 2;

      initUdfEditor();
   }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public boolean isCellEditable(EventObject event)
   {
        if (event instanceof MouseEvent)
      {
         MouseEvent mouseEvent = (MouseEvent) event;
          if (mouseEvent.getClickCount() >= getClickCountToStart())
         {
            if (mouseEvent.getSource() instanceof MapBrowserTable)
            {
               MapBrowserTable table = (MapBrowserTable) mouseEvent.getSource();
               int row = table.rowAtPoint(mouseEvent.getPoint());
               int col = table.columnAtPoint(mouseEvent.getPoint());
               Object selectedValue = table.getValueAt( row , col);

               if( !(selectedValue instanceof OSExtensionCall) )
                    super.isCellEditable(event);
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Sets editor value.
    *
    * @param value the new value to set
    *    assumed not <code>null</code>.
    * @return <code>true</code> if value is set
    */
   //////////////////////////////////////////////////////////////////////////////
   public boolean setValue(Object value)
   {
      if (value instanceof OSExtensionCall)  //user defined function
      {
          OSExtensionCall call = (OSExtensionCall) value;
         String function = createUdfDisplayText(call);
         super.setValue(value, function);
         return true;
      }
      else // must be a datatype value
      {
         return super.setValue(value);
      }
   }

   /**
   * Get the UDF display text.
   *
   * @param call the call to create the display text from
   * @return String the UDF display text
   *    never <code>null</code>, might be empty
   * @throws IllegalArgumentException If param is null.
   */
   /////////////////////////////////////////////////////////////////////////////
   public static String createUdfDisplayText(OSExtensionCall call)
   {
      if (call == null)
         throw new IllegalArgumentException("call cannot be null");

      PSExtensionParamValue[] params = call.getParamValues();
      String function = call.getExtensionRef().getExtensionName();
      function += "(";
      for (int i=0, n=params.length; i<n; i++)
      {
         if (params[i] == null)
            function += "";
         else
            function += params[i].getValue().getValueDisplayText();
         if (i<n-1)
            function += ", ";
      }
      function += ")";

      return function;
   }

   /**
   * Get UDF call
   *
   * @return exits   the new exit
   */
   /////////////////////////////////////////////////////////////////////////////
   public OSExtensionCall getUdfCall()
   {
      return m_udfEditor.getUdfCall();
   }

   /**
   * Gets the editor dialog of this cell
   *
   * @return JDialog the editor dialog
   */
   /////////////////////////////////////////////////////////////////////////////
   public JDialog getEditorDialog()
   {
      if (getValue() instanceof OSExtensionCall)
         return m_udfEditor;

      return getValueEditor();
   }

   /**
   * UDF Editor is called for editing
   *
   * @return true or false for selection of editing cell
   */
   /////////////////////////////////////////////////////////////////////////////
   // implementations for TableCellEditor
   public boolean shouldSelectCell(EventObject event)
   {
      if (event instanceof MouseEvent)
      {
         MouseEvent mouseEvent = (MouseEvent) event;
         if (mouseEvent.getSource() instanceof MapBrowserTable)
         {
               MapBrowserTable table = (MapBrowserTable) mouseEvent.getSource();
               int row = table.rowAtPoint(mouseEvent.getPoint());
               int col = table.columnAtPoint(mouseEvent.getPoint());
               Object selectedValue = table.getValueAt( row , col);

               if( selectedValue instanceof OSExtensionCall )
               {
                  m_udfEditor.clearSelector();
                    m_udfEditor.onEdit((OSExtensionCall)selectedValue, false);
                  m_udfEditor.center();
                    m_udfEditor.setVisible(true);
                  return true;
               }
               else
                  return super.shouldSelectCell(event);
          }
      }
      return true;
   }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public boolean stopCellEditing()
   {
       if (getValue() instanceof OSExtensionCall)
       {
           fireEditingStopped();
         return true;
       }
       else
         return super.stopCellEditing();
   }

   /**
    * Initialize UDF Editor action listeners
    * catch OK/Cancel actions from formula property dialog and perform the
    * approptiate action
    */
   private void initUdfEditor()
   {
        if (m_udfEditor != null)
      {
         m_udfEditor.addOkListener(new ActionListener()
           {
             public void actionPerformed(ActionEvent event)
            {
                setValue(m_udfEditor.getUdfCall());
                 acceptCellEditing();
            }
           });

          m_udfEditor.addCancelListener(new ActionListener()
         {
                public void actionPerformed(ActionEvent event)
             {
                cancelCellEditing();
            }
           });
      }
   }

   /** The UDF dialog that will be displayed if the cell contains an Extension
    * call.
    */
   private FormulaPropertyDialog m_udfEditor = null;
}

/*[ PSFieldValueMapTable.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * This class is used to display and edit a list of field properties within
 * a table an item extractor.
 */
public class PSFieldValueMapTable extends JTable
{

   /**
    * Constructs a new table. Use {@link #newInstance(boolean)} to
    * create a new instance from outside of this class.
    */
   private PSFieldValueMapTable()
   {
      super();
   }

   /**
    * Overridden Always returns the <code>PSFieldValueMapTableCellEditor</code>
    * table cell editor.
    *
    * @param row the row index of the cell to be edited
    * @param col the column index of the cell to be edited
    * @return the TableCellEditor, Never <code>null</code>.
    */
   public TableCellEditor getCellEditor(int row, int col)
   {
      return m_cellEditor;
   }

   /**
    * Stops table cell editing
    */
   public void stopCellEditing()
   {
      m_cellEditor.stopCellEditing();
   }

   /**
    * Returns a new instance of <code>PSFieldValueMapTable</code> using
    * the <code>PSFieldValueMapTableModel</code> and a key listener
    * added to listen for delete key events.
    *
    * @param allowsXPathType indicates if XPath value type is allowed
    *
    * @return a new instance of <code>PSFieldValueMapTable</code>, Never
    * <code>null</code>.
    */
   public static PSFieldValueMapTable newInstance(boolean allowsXPathType)
   {

     final PSFieldValueMapTable table = new PSFieldValueMapTable();
     final PSFieldValueMapTableModel model =
        new PSFieldValueMapTableModel(table, allowsXPathType);
     table.setModel(model);
     KeyAdapter keyAdapter = ( new KeyAdapter()
     {
        // Removes selected row if delete key is hit
         public void keyReleased(KeyEvent event)
         {
           if(event.getKeyCode() == event.VK_DELETE)
           {
              table.stopCellEditing();
              model.removeRow(table.getSelectedRow());
           }

           if(event.getKeyCode() == event.VK_ENTER)
           {
              table.stopCellEditing();
           }
         }
      });

      table.addKeyListener(keyAdapter);
      PSFieldValueMapTableCellEditor celleditor =
         (PSFieldValueMapTableCellEditor)table.getCellEditor(0,0);
      celleditor.addKeyListener(keyAdapter);

      return table;
   }

   /**
    * Table cell editor for this table. Will always be
    * <code>PSFieldValueMapTableCellEditor</code>, never <code>null</code>.
    */
   private TableCellEditor m_cellEditor = new PSFieldValueMapTableCellEditor();


}
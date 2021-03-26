/*[ UTJTable.java ]************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.guitools.PSJTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * A subclass of JTable that provides useful additional helper methods. JTable
 * has a tendency to put useful methods in aggregated data objects such as 
 * the column collection. Additionally, those objects are not directly
 * accessed, but accessed via model instances. 
 */
////////////////////////////////////////////////////////////////////////////////
public class UTJTable extends PSJTable implements KeyListener
{
   /**
    * Constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTJTable()
   {
      super();
      initTable();
   }

   /**
    * Constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTJTable(int numRows, int numColumns)
   {
      super(numRows, numColumns);
      initTable();
   }

   /**
    * Constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTJTable(Object[][] rowData, Object[] columnNames)
   {
      super(rowData, columnNames);
      initTable();

   }

   /**
    * Constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTJTable(TableModel dm)
   {
      super(dm);
      initTable();
   }

   /**
    * Constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTJTable(TableModel dm, TableColumnModel cm)
   {
      super(dm, cm);
      initTable();
   }

   /**
    * Constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
   {
      super(dm, cm, sm);
      initTable();
   }

   /**
    * Constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTJTable(Vector rowData, Vector columnNames)
   {
      super(rowData, columnNames);
      initTable();
   }

   /**
    * Inner class to handle cell rendering for the first column if that column
    * has no header.
    */
   private class TableCERenderer extends DefaultTableCellRenderer
   {
      /**
       * If the first column does not have header we want to display button
       * looking images in it.
       * @see #getTableCellRendererComponent in the base class
       */
      public Component getTableCellRendererComponent(
         JTable table,
         Object value,
         boolean isSelected,
         boolean hasFocus,
         int row,
         int column)
      {
         if (column == 0)
         {
            ImageIcon icon =
               new ImageIcon(
                  getClass().getResource(
                     E2Designer.getResources().getString("gif_Column")));

            if (hasFocus || isSelected)
            {
               if (hasFocus)
                  setBorder(new LineBorder(Color.yellow));

               icon =
                  new ImageIcon(
                     getClass().getResource(
                        E2Designer.getResources().getString("gif_ColumnSel")));
            }
            else
               setBorder(new EmptyBorder(0, 0, 0, 0));

            setIcon(icon);
         }
         return this;
      }
   }

   /**
    * Creates a column without a header, with a custom cell renderer and
    * sets the column as the first one. This column will be used for selecting
    * the rows.
    * @param createCol a flag if <code>true</code> indicates that this column is
    * to be created if <code>false</code> nothing is done.
    */
   public void setSelectionColumn(boolean createCol)
   {
      if (createCol)
      {
         //create and add a column, by default as the last column
         TableColumn newColumn =
            new TableColumn(0, 18, new TableCERenderer(), null);
         newColumn.setMinWidth(18);
         newColumn.setMaxWidth(18);
         newColumn.setResizable(false);
         newColumn.setHeaderRenderer(new TableCERenderer());
         newColumn.setCellRenderer(new TableCERenderer());
         addColumn(newColumn);
         moveColumn(this.getColumnCount() - 1, 0); // move it to first column

         setRowSelectionAllowed(true);
      }
   }

   /**
   * Initialize the table.
   */
   //////////////////////////////////////////////////////////////////////////////
   private void initTable()
   {
      // specify table behavier
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(
         DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      this.setRowSelectionAllowed(true);
      this.addKeyListener(this);

   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for KeyListener
   public void keyPressed(KeyEvent event)
   {
      if (event.getKeyCode() == KeyEvent.VK_DELETE)
      {
         if (this.getModel() instanceof UTTableModel)
         {
            UTTableModel model = (UTTableModel) this.getModel();
            int deleteCount = 0;
            int[] indices = this.getSelectedRows();
            for (int i = 0, n = indices.length; i < n; i++)
            {
               // delete all selected rows
               model.deleteRow(indices[i]);
               deleteCount++;
            }

            // append as many empty rows as we deleted
            model.appendRow(deleteCount);
         }
      }
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for KeyListener
   public void keyReleased(KeyEvent event)
   {
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for KeyListener
   public void keyTyped(KeyEvent event)
   {
   }

   /**
    * Sets the minimum number of rows for this table
    * @param rows the number representing the min number of rows, if it is
    * less then 1 the initial value will be used.
    */
   public void setMinRows(int rows)
   {
      if (rows < 1)
         return;

      m_minRows = rows;
   }

   /**
    * Gets the min number of rows for this table.
    * @return the min number of rows.
    */
   public int getMinRows()
   {
      return m_minRows;
   }

   /**
    * Sets the editors for the columns in this table.
    * @param editors A Map where the key is a column name as a String, and the
    * value is the control to be set for the named column, this value can not be
    * <code>null</code>. This map can not be <code>null</code>
    */
   public void setColumnEditors(Map editors)
   {
      if (editors == null)
         throw new IllegalArgumentException("Editors map can not be null");

      Iterator iter = editors.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry entry = (Map.Entry) iter.next();
         String key = (String) entry.getKey();
         Object editorObj = entry.getValue();

         if (editorObj == null)
            throw new IllegalArgumentException("Control can not be null");

         //build the editor
         TableCellEditor editor = buildCellEditor(editorObj);

         //set the editor for the column
         if (editor != null)
            this.getColumn(key).setCellEditor(editor);
      }
   }

   /**
    * Creates the cell editor with the control passed in.
    * @param control a control to build the cell editor from, 
    * must not to be <code>null</code>.
    * @return an editor, assumed not to be <code>null</code>.
    */
   private TableCellEditor buildCellEditor(Object control)
   {
      if (control == null)
      {
         throw new IllegalArgumentException("control may not be null");
      }
      
      TableCellEditor editor = null;

      if (control instanceof JTextField)
      {
         JTextField field = (JTextField) control;
         editor = new DefaultCellEditor(field);
      }
      else if (control instanceof JComboBox)
      {
         JComboBox box = (JComboBox) control;
         editor = new DefaultCellEditor(box);
      }
      else if (control instanceof JCheckBox)
      {
         JCheckBox chBox = (JCheckBox) control;
         editor = new DefaultCellEditor(chBox);
      }
      else if (control instanceof JComponent)
      {
         /*    boolean supported = false;
         
         Class[] interfaces = control.getClass().getInterfaces();
         if(interfaces.length > 0)
         {
          for(int i=0; i<interfaces.length; i++)
          {
             if(interfaces[0].getName().equals("IEditorComponent"))
             {
                supported = true;
                break;
             }
          }
         }
         if(!supported)
          throw new UnsupportedOperationException(
             "The editor control should support 'IEditorComponent' interface");*/

         if (!(control instanceof UTEditorComponent))
            throw new UnsupportedOperationException("The editor control should support 'IEditorComponent' interface");

         editor = new UTCellEditor((UTEditorComponent) control);
      }
      return editor;
   }

   /**
    * Representing the min number of rows for this table.
    * The initial value is 1, so that the table will have at least one row.
    * Gets set in {@link #setMinRows(int)}
    */
   int m_minRows = 1;

}

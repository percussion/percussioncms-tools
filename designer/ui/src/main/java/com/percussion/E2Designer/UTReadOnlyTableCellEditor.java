/*[ UTReadOnlyTableCellEditor.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

/**
 * A table cell editor implementation which does not allow any cell editing.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTReadOnlyTableCellEditor extends DefaultCellEditor implements TableCellEditor
{
   public UTReadOnlyTableCellEditor()
  {
     super(new JTextField());
  }

    public Component getTableCellEditorComponent(JTable table,
                                               Object value,
                                                                       boolean isSelected,
                                               int row,
                                               int column)
   {
    table.setRowSelectionInterval(row, row);
    table.setColumnSelectionInterval(column, column);

      return getComponent();
  }

  public void addCellEditorListener(CellEditorListener l) {}
   public void cancelCellEditing() {}
  public Object getCellEditorValue() { return null; }
  public boolean isCellEditable(EventObject anEvent) { return false; }
  public void removeCellEditorListener(CellEditorListener l) {}
  public boolean shouldSelectCell(EventObject anEvent) { return false; }
  public boolean stopCellEditing() { return true; }
}

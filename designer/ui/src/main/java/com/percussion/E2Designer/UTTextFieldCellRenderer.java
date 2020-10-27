/*[ UTTextFieldCellRenderer.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Table cell renderer for a text field, that shows a button to open an
 * additional dialog, if the table cell is in edit mode.
 */
////////////////////////////////////////////////////////////////////////////////
class UTTextFieldCellRenderer extends UTTextFieldEditor implements TableCellRenderer
{
   /**
   * Construct the cell renderer
    */
  //////////////////////////////////////////////////////////////////////////////
  public UTTextFieldCellRenderer()
  {
    super();
  }

  //////////////////////////////////////////////////////////////////////////////
  // implementation for TableCellRenderer
  public Component getTableCellRendererComponent(JTable table,
                                                                        Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row, int column)
  {
     if (value == null)
       return this;

      setValue(value);
    table.setRowSelectionInterval(row, row);
    table.setColumnSelectionInterval(column, column);

    // set foreground / background
    if (isSelected)
    {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    }
    else
    {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }

    return this;
  }
}

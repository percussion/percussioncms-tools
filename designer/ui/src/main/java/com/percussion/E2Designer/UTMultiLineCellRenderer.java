/*[ UTMultiLineCellRenderer.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.Serializable;

/**
 * the cell renderer when JTextArea object is stored within a table cell.
 */
public class UTMultiLineCellRenderer extends JTextArea implements TableCellRenderer, Serializable {

  protected static Border noFocusBorder; 
    
  private Color unselectedForeground; 
  private Color unselectedBackground; 

  public UTMultiLineCellRenderer() {
    super();
    noFocusBorder = new EmptyBorder(1, 2, 1, 2);
    setLineWrap(true);
    setWrapStyleWord(true);
    setOpaque(true);
    setBorder(noFocusBorder);
  }

  public void setForeground(Color c) {
    super.setForeground(c); 
    unselectedForeground = c; 
  }
    
  public void setBackground(Color c) {
    super.setBackground(c); 
    unselectedBackground = c; 
  }

  public void updateUI() {
    super.updateUI(); 
    setForeground(null);
    setBackground(null);
  }
    
  public Component getTableCellRendererComponent(JTable table, Object value,
                   boolean isSelected, boolean hasFocus, 
                   int row, int column) {

    if (isSelected) {
      super.setForeground(table.getSelectionForeground());
      super.setBackground(table.getSelectionBackground());
    }
    else {
      super.setForeground((unselectedForeground != null) ? unselectedForeground 
           : table.getForeground());
      super.setBackground((unselectedBackground != null) ? unselectedBackground 
           : table.getBackground());
    }
   
    setFont(table.getFont());

    if (hasFocus) {
      setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
      if (table.isCellEditable(row, column)) {
   super.setForeground( UIManager.getColor("Table.focusCellForeground") );
   super.setBackground( UIManager.getColor("Table.focusCellBackground") );
      }
    } else {
      setBorder(noFocusBorder);
    }

    setValue(value); 
        
    return this;
  }
    
  protected void setValue(Object value) {
    setText((value == null) ? "" : value.toString());
  }


  public static class UIResource extends UTMultiLineCellRenderer implements javax.swing.plaf.UIResource {
  }

}



/*[ UTCheckBoxCellRenderer.java ]**********************************************
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
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

 /**
  * A simple renderer that displays a Boolean
  * value in a checkbox.  We'll subclass this to
  * provide a cell editor as well...
  */
public class UTCheckBoxCellRenderer implements TableCellRenderer {
    private JCheckBox checkBox = new JCheckBox();
    private Border emptyBorder = new EmptyBorder(1,1,1,1);
    private Border focusBorder =
        new LineBorder(Color.lightGray, 1);

    protected JCheckBox getCheckBox() {
        return checkBox;
    }

    /**
     * Get a Component that can be used to
     * edit the value
     */
    public Component getTableCellRendererComponent(
                        JTable table,
                        Object value,
                        boolean isSelected,
                        boolean hasFocus,
                        int row,
                        int column) {
        // set the state of the checkbox to the
        // state of the data.
        // NOTE: JTable will send a null value through
        //       when trying to get a ToolTip for
        //       the object -- make sure you
        //       don't assume the value is valid!
        checkBox.setSelected((value==null || value.toString().trim().equals(""))?false :
                        ((Boolean)value).booleanValue());
        checkBox.setHorizontalAlignment(
                        SwingConstants.CENTER);
        checkBox.setFont(table.getFont());
        checkBox.setText("");

        // set a border based on whether we have
        //  focus.
/*        if (hasFocus)
            checkBox.setBorder(focusBorder);
        else
            checkBox.setBorder(emptyBorder);      */

        // if selected, show our background
        checkBox.setOpaque(isSelected);

        if (isSelected)
        {
            checkBox.setBackground(table.getSelectionBackground());
        }
        else checkBox.setBackground(table.getBackground());
//        checkBox.setSelected( value != null && ((Boolean)value).booleanValue());

         return checkBox;
    }
}

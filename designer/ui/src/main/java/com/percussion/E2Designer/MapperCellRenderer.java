/*[ MapperCellRenderer.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Table cell renderer for a text field
 * Customizes the value to be set in the table cell
 */
////////////////////////////////////////////////////////////////////////////////
class MapperCellRenderer extends DefaultTableCellRenderer
{
   /////////////////////////////////////////////////////////////////////////////
   // implementation for TableCellRenderer
   public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row, int column)
   {
        if (value == null)
          return this;

      setText(customizeValue(value));

      // set foreground / background
      if (!isSelected)
      {
         setForeground(table.getForeground());
         setBackground(table.getBackground());
      }

      if (hasFocus)
          setBorder(new LineBorder(Color.yellow));
      else
          setBorder(new EmptyBorder(0, 0, 0, 0));

      return this;
   }

   /**
    * Gets the string representation of the provided value object and
    * customizes it according to its type.
    *
    * @param value the value to get the customized string representaion from,
    *    assumed not <code>null</code>.
    * @return the customized string representation of the provided object,
    *    never <code>null</code>, might be empty.
    */
   private String customizeValue(Object value)
   {
      if (value instanceof OSExtensionCall)  //user defined function
      {
          OSExtensionCall call = (OSExtensionCall) value;
         return MapperCellEditor.createUdfDisplayText(call);
      }
      else // must be a datatype value
      {
         return value.toString();
      }
   }
}

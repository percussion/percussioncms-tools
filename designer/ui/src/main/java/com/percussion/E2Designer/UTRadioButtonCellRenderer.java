/******************************************************************************
 *
 * [ UTRadioButtonCellRenderer.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * A simple renderer that displays a Boolean value in a radio button. We'll
 * subclass this to provide a cell editor as well...
 */
public class UTRadioButtonCellRenderer implements TableCellRenderer
{
   private JRadioButton m_radioButton = new JRadioButton();

   /**
    * Get the radio button used by this renderer
    * 
    * @return The button, never <code>null</code>.
    */
   protected JRadioButton getRadioButton()
   {
      return m_radioButton;
   }

   /**
    * Get a Component that can be used to edit the value
    */
   public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
   {
      // set the state of the checkbox to the
      // state of the data.
      // NOTE: JTable will send a null value through
      //       when trying to get a ToolTip for
      //       the object -- make sure you
      //       don't assume the value is valid!
      m_radioButton.setSelected((value == null || 
         value.toString().trim().equals("")) ? false : 
            ((Boolean) value).booleanValue());
      m_radioButton.setHorizontalAlignment(SwingConstants.CENTER);
      m_radioButton.setFont(table.getFont());
      m_radioButton.setText("");

      // if selected, show our background
      m_radioButton.setOpaque(isSelected);

      if (isSelected)
      {
         m_radioButton.setBackground(table.getSelectionBackground());
      }
      else
         m_radioButton.setBackground(table.getBackground());

      return m_radioButton;
   }

}
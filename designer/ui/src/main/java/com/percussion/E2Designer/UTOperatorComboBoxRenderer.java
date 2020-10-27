/*[ UTOperatorComboBoxRenderer.java ]******************************************
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
 * Renderer to display the resource text instead of the internal text.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTOperatorComboBoxRenderer extends DefaultTableCellRenderer
{
  //////////////////////////////////////////////////////////////////////////////
  // implementation for TableCellRenderer
  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row, int column)
  {
    if (value != null && value instanceof String)
      this.setValue(m_translator.translateToDisplay((String) value));

    if (hasFocus)
      setBorder(new LineBorder(Color.yellow));
    else
      setBorder(new EmptyBorder(0, 0, 0, 0));

    return this;
  }

  //////////////////////////////////////////////////////////////////////////////
  UTOperatorComboBox m_translator = new UTOperatorComboBox();
}

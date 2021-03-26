/*[ ConditionalCellRenderer.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Table cell renderer the mapper conditionals.
 */
////////////////////////////////////////////////////////////////////////////////
class ConditionalCellRenderer extends ConditionalCell implements TableCellRenderer
{
   /**
   * Construct the cell editor
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public ConditionalCellRenderer() throws ClassNotFoundException
   {
     super(null);
  }

  //////////////////////////////////////////////////////////////////////////////
  // implementation for TableCellRenderer
  public Component getTableCellRendererComponent(JTable table,
                                                                        Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row, int column)
  {
      // show conditional icon
    if (value != null && value instanceof PSCollection)
    {
       PSCollection conditionals = (PSCollection) value;
      if (conditionals.size() == 0)
         setIcon(m_noCondIcon);
      else
         setIcon(m_condIcon);

      if (hasFocus)
         setBorder(new LineBorder(Color.yellow));
      else
       setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    return this;
  }
  //////////////////////////////////////////////////////////////////////////////
   /*
   * the conditional icons
   */
   ImageIcon m_condIcon = new ImageIcon(getClass().getResource(E2Designer.getResources().getString("gif_Conditionals")));
   ImageIcon m_noCondIcon = new ImageIcon(getClass().getResource(E2Designer.getResources().getString("gif_NoConditionals")));
}


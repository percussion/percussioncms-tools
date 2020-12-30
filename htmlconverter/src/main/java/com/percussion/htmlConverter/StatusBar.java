/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/** 
 * This class provides all functionality to display and maintain a status bar.
 */
public class StatusBar extends JPanel
{
   /**
    * Construct a new status bar.
    *
    * @param text the status text to start with.
    * @param line the line number to display.
    * @param column the column number to display.
    */
   public StatusBar(String text, int line, int column)
   {
      Border b1 = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
      
      m_text = new JLabel(text);
      m_text.setBorder(b1);
      
      m_lineColumn = new JLabel(getLineColumnString(line, column))
      {
         public Dimension getPreferredSize()
         {
            return new Dimension(60, 20);
         }
      };
      m_lineColumn.setHorizontalAlignment((int) JLabel.CENTER_ALIGNMENT);
      m_lineColumn.setBorder(b1);
      
      this.setLayout(new BorderLayout());
      this.add(m_text, "Center");
      this.add(m_lineColumn, "East");
   }
   
   /**
    * Set the status text.
    *
    * @param text the new staus text.
    */
   public void setStatusText(String text)
   {
      if (text != null)
         m_text.setText(text);
   }
   
   /**
    * Set the line and column numbers.
    *
    * @param line the new line to set.
    * @param column the new column to set.
    */
   public void setLineColumnText(int line, int column)
   {
      m_lineColumn.setText(getLineColumnString(line, column));
   }
   
   /**
    * Get the display string for line and column provided.
    *
    * @param line the line number to display.
    * @param column the column number to display.
    */
   protected String getLineColumnString(int line, int column)
   {
      String strLineColumn = "" + line + ":" + column;
      return strLineColumn;
   }
   
   /**
    * The label which displays the status text.
    */
   JLabel m_text = null;
   /**
    * The label which displays line and column numbers (line:column).
    */
   JLabel m_lineColumn = null;
}

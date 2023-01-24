/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

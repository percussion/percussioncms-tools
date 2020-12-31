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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A fixed sized JTextField.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTFixedTextField extends JTextField
{
   /**
   * Construct a new fixed text field with the standard size.
   *
   * @param string      the text field name
   */
   public UTFixedTextField(String name)
   {
      super(name);
    setPreferredSize(STANDARD_TEXTFIELD_SIZE);
   }

   /**
   * Construct a new fixed text field of passed size.
   *
   * @param string      the text field name
   * @param size         the text field size
   */
   public UTFixedTextField(String name, Dimension size)
   {
      super(name);
    setPreferredSize(size);
   }

   /**
   * Construct a new fixed text field of passed width/height.
   *
   * @param string      the text field name
   * @param width         the text field width
   * @param height      the text field height
   */
   public UTFixedTextField(String name, int width, int height)
   {
      super(name);
    setPreferredSize(new Dimension(width, height));
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Dimension getMinimumSize()
   {
      return getPreferredSize();
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Dimension getMaximumSize()
   {
      return getPreferredSize();
   }

  /**
   * Sets number only capability in the text field.
   *
   * @param bNumOnly <CODE>true</CODE> = numbers only, other keys are consumed.
   */
  public void setNumericDataOnly(boolean bNumOnly)
  {
    if (bNumOnly)
    {
      m_klListener = new KeyAdapter()
      {
        public void keyTyped(KeyEvent e)
        {
          m_cKey = e.getKeyChar();

          if (!Character.isDigit(m_cKey))
          {
            e.consume();
            return;
          }
        }
      };

      this.addKeyListener(m_klListener);
    }
    else // removes the non-digit keylistener from this textfield
    {
      if (null != m_klListener)
        this.removeKeyListener(m_klListener);

      m_klListener = null;
    }
  }

  /**
   * @returns boolean <CODE>true</CODE> = field is numbers only.
   */
  public boolean isNumericDataOnly()
  {
    return m_klListener != null;
  }


  /**
   * For testing only.
   */
  public static void main(String[] args)
  {
    JFrame f = new JFrame();
    final UTFixedTextField test = new UTFixedTextField("");
    test.setNumericDataOnly(true);

    JButton toggle = new JButton("Toggle");
    toggle.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent e)
      {
        if (test.isNumericDataOnly())
          test.setNumericDataOnly(false);
        else
          test.setNumericDataOnly(true);
      }
    });
    
    JPanel p = new JPanel(new BorderLayout());
    p.add(test, BorderLayout.NORTH);
    p.add(toggle, BorderLayout.SOUTH);

    f.getContentPane().add(p);

    f.setSize(100, 100);
    f.setVisible(true);
  }

  //////////////////////////////////////////////////////////////////////////////
   /**
    * the standard text field size
    */
  private static final Dimension STANDARD_TEXTFIELD_SIZE = new Dimension(200, 20);
  /**
   * the key listener used to consume non-digit keys.
   */
  private KeyListener m_klListener = null;
  /**
   * Used to prevent over-allocation.
   */
  private char m_cKey = 'a';
}

/******************************************************************************
 *
 * [ UTFixedTextField.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A fixed sized JTextField.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTFixedTextField extends JTextField
{
   public UTFixedTextField()
   {
      this( "" );
   }

   /**
   * Construct a new fixed text field with the standard size.
   *
   * @param name      the text field name
   */
   public UTFixedTextField(String name)
   {
      super(name);
    setPreferredSize(STANDARD_TEXTFIELD_SIZE);
   }

   /**
   * Construct a new fixed text field of passed size.
   *
   * @param name      the text field name
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
   * @param name      the text field name
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
   @Override
   public Dimension getMinimumSize()
   {
      return getPreferredSize();
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   @Override
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
        @Override
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
    return null != m_klListener;
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
       @SuppressWarnings("unused")
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

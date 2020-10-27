/*[ UTCellEditorDialog.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Basic table cell editor dialog.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTCellEditorDialog extends JDialog
{
   /**
   * Construct the dialog.
    */
  //////////////////////////////////////////////////////////////////////////////
  public UTCellEditorDialog(ActionListener listener)
  {
     super();
    this.setModal(true);

    m_ok.addActionListener(listener);
    m_cancel.addActionListener(listener);

      initDialog();
  }

   /**
   * Set the text field.
   *
   * @text   the new text
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setValue(String text)
  {
     m_text.setText(text);
  }

   /**
   * Get the text field contents.
   *
   * @String   the text
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getValue()
  {
     return m_text.getText();
  }

   /**
   * Create the dialogs view/edit panel.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createViewPanel()
  {
     JPanel p1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    p1.add(m_ok);
    p1.add(m_cancel);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(m_text, "Center");
    panel.add(p1, "South");

    return panel;
  }

   /**
   * Initialize the dialogs GUI elements with its data.
   */
  //////////////////////////////////////////////////////////////////////////////
  private void initDialog()
  {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(createViewPanel(), "Center");

     getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel);
    pack();
  }

   /**
   * Added for testing reasons only.
   */
  //////////////////////////////////////////////////////////////////////////////
  private ResourceBundle m_res = null;
  protected ResourceBundle getResources()
  {
      try
    {
      if (m_res == null)
          m_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
                                                           Locale.getDefault() );
    }
    catch (MissingResourceException e)
      {
         System.out.println(e);
      }

       return m_res;
    }

   //////////////////////////////////////////////////////////////////////////////
   /**
   * the OK button
   */
  private UTFixedButton m_ok = new UTFixedButton("OK");
   /**
   * the Cancel button
   */
  private UTFixedButton m_cancel = new UTFixedButton("Cancel");
   /**
   * the text field
   */
  private UTFixedTextField m_text = new UTFixedTextField("");
}

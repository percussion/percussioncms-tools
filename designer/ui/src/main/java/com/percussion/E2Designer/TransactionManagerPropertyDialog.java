/*[ TransactionManagerPropertyDialog.java ]************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * The transaction manager property dialog.
 */
////////////////////////////////////////////////////////////////////////////////
public class TransactionManagerPropertyDialog extends PSEditorDialog
{
   /**
   * Construct the default transaction manager property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public TransactionManagerPropertyDialog()
   {
     super();
     initDialog();
   }

   /**
   * Create radio button panel
   *
   * @return   JPanel      the panel
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createRadioButtonPanel()
  {
     JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    // create radio buttons
    m_rowLevel.setMnemonic(getResources().getString("mn_rowLevel").charAt(0));
    m_all.setMnemonic(getResources().getString("mn_all").charAt(0));

    m_group.add(m_rowLevel);
    m_group.add(m_all);

      m_group.setSelected(m_all.getModel(), true);

    panel.add(m_rowLevel);
    panel.add(m_all);

    return panel;
  }

   /**
   * Create view panel
   *
   * @return   JPanel      the criteria selection view panel
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createViewPanel()
  {
     JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
    {
       // implement onOk action
       public void onOk()
      {
          TransactionManagerPropertyDialog.this.onOk();
      }
    };

    panel.add(createRadioButtonPanel(), "Center");
    panel.add(m_commandPanel, "East");

    return panel;
  }

   /**
   * Initialize the dialogs GUI elements with its data.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  private void initDialog()
  {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(createViewPanel());

     // set the default button
    getRootPane().setDefaultButton(m_commandPanel.getOkButton());

     getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel);
    pack();
  }

   /**
   * Test the dialog.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
     try
    {
         final JFrame frame = new JFrame("Test Dialog");
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         frame.addWindowListener(new BasicWindowMonitor());

         JButton startButton = new JButton("Open Dialog");
         frame.getContentPane().add(startButton);
         startButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
          JDialog dialog = new TransactionManagerPropertyDialog();
               dialog.setVisible(true);
            }
         });

         frame.setSize(640, 480);
         frame.setVisible(true);
    }
    catch (Exception ex)
    {
       ex.printStackTrace();
    }
  }

   /**
   * Added for testing reasons only.
   *
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
   // implementation of IEditor
  public boolean onEdit(UIFigure figure, final Object data)
  {
     try
    {
        if (figure.getData() instanceof OSTransaction)
       {
          m_transaction = (OSTransaction) figure.getData();
        if (m_transaction.isRowLevel())
           m_group.setSelected(m_rowLevel.getModel(), true);
        else
           m_group.setSelected(m_all.getModel(), true);

            this.center();
          this.setVisible(true);
        return true;
       }
       else
          throw new IllegalArgumentException("OSTransaction expected!");
      }
    catch (Exception e)
    {
       e.printStackTrace();
    }

    return false;
  }


/** Handles ok button action. Overrides PSDialog onOk() method implementation.
*/
  public void onOk()
  {
    try
    {
      if (m_group.isSelected(m_rowLevel.getModel()))
        m_transaction.setRowLevel();
      else
        m_transaction.setAllRows();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    dispose();
  }
  

   //////////////////////////////////////////////////////////////////////////////
    private OSTransaction m_transaction = null;
  /**
   * the radion buttons to switch the view
   */
  ButtonGroup m_group = new ButtonGroup();
  JRadioButton m_rowLevel = new JRadioButton(getResources().getString("rowLevel"));
  JRadioButton m_all = new JRadioButton(getResources().getString("all"));
   /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel = null;
}

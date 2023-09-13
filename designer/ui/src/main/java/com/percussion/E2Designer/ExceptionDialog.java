/******************************************************************************
 *
 * [ ExceptionDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class ExceptionDialog extends PSDialog
{
   /** Constructor which takes the Frame object.
    */
   ExceptionDialog(Frame frame, String message, String title)
   {
      super(frame, title);
      initDialog(frame, message);
   }

   /** Constructor which takes the Frame object.
    */
   ExceptionDialog(Dialog dialog, String message, String title)
   {
      super(dialog, title);
      initDialog(dialog, message);
   }

   private void initDialog(Window owner, String message)
   {
      setResizable( true );
      m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
         @Override
         public void onOk()
         {
            m_bOk = true;
            setVisible(false);
            dispose();
         }
      };

      m_commandPanel.getCancelButton().setVisible(false);
      if (m_commandPanel.getHelpButton() != null)
      {
         m_commandPanel.getHelpButton().setVisible(false);
      }

      m_List = new JTextArea(message);
      m_List.setEditable(false);
      m_List.setLineWrap(true);
      m_List.setWrapStyleWord(true);
      if (owner != null)
      {
         m_List.setForeground(owner.getForeground());
         m_List.setBackground(owner.getBackground());
      }

      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      JPanel listpanel = new JPanel(new BorderLayout());
      listpanel.setBorder(new EmptyBorder(5,5,5,5));

      JScrollPane sPane = new JScrollPane(m_List);
      listpanel.add(sPane, "Center");
      panel.add(listpanel, "Center");
      panel.add(m_commandPanel, "South");

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);
      this.setSize(EX_DIALOG_SIZE);
      center();
      pack();
      setVisible(true);
   }

   public boolean wasOKPressed()
   {
      return(m_bOk);
   }

   private UTStandardCommandPanel m_commandPanel = null;
   private JTextArea m_List = null;
   private boolean m_bOk = false;
   private final Dimension EX_DIALOG_SIZE = new Dimension(300, 200);
}

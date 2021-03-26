/*[ FilterEditorDialog.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.UTFixedButton;
import com.percussion.util.PSStringOperation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Editor dialog for entering filters.
 **/
public class FilterEditorDialog extends PSDialog
                                implements ActionListener
{
   /**
    * Constructor for Filter Editor Dialog with the passed in
    * <code>filter</code> set in text area. It replaces 'filter seperator(;)'
    * in <code>filter</code> with new line so that one filter is displayed per
    * line. If 'filter seperator' is part of the filter, then it should be
    * escaped in <code>filter</code> for not to be replaced with new line.
    *
    * Example: Replaces ';' with '\n' and ';;' with ';' in passed in
    * <code>filter</code>.
    *
    * @param frame the parent frame of this dialog. May be <code>null</code>.
    * @param title of the dialog. May be <code>null</code>.
    * @param filter to set in the text area. May be <code>null</code>.
    **/
   public FilterEditorDialog(JFrame frame, String title, String filter)
   {
      super(frame, title);
      try {
         if(m_bInitialized=initDialog())
         {
            if(filter != null && filter.length() > 0)
            {
               // replaces ';' with '\n' and ';;' with ';'
               filter = PSStringOperation.replaceChar(filter,
                  ms_res.getString("filterSeperator").charAt(0), '\n');
            }
         }
      }
      catch(MissingResourceException e)
      {
         String property = e.getLocalizedMessage().substring(
            e.getLocalizedMessage().lastIndexOf(' ')+1);

         JOptionPane.showMessageDialog(null,
            "Could not find value for '" + property + "' in \n" +
            "com.percussion.E2Designer.admin.ModifyMemberListDialogResources",
            "Error", JOptionPane.ERROR_MESSAGE);
         m_bInitialized = false;
      }
      m_valueText.setText(filter);
   }

   /**
    * Gets Resource Bundle required for this dialog.
    *
    * @return Resource Bundle, May be <code>null</code>.
    **/
   private ResourceBundle getDialogResources()
   {
      String resourceName =
         "com.percussion.E2Designer.admin.ModifyMemberListDialogResources";
      try {
         ms_res = ResourceBundle.getBundle(resourceName,Locale.getDefault());
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
         JOptionPane.showMessageDialog(null, "Could not find " + resourceName,
            "Error", JOptionPane.ERROR_MESSAGE);
      }

      return ms_res;
   }

   /**
    * Creates dialog framework.
    *
    * @return <code>true</code> to indicate initialization is successful,
    * Otherwise <code>false</code>.
    **/
   private boolean initDialog()
   {
      ms_res = getDialogResources();
      if(ms_res == null)
         return false;

      JPanel panel = new JPanel(new BorderLayout(20,20));
      getContentPane().add(panel);

      panel.setBorder(new EmptyBorder(5,10,5,10));

      JPanel editor_panel = new JPanel();
      editor_panel.setLayout(new BoxLayout(editor_panel, BoxLayout.Y_AXIS));
      JLabel label = new JLabel(
         ms_res.getString("filterPattern"), SwingConstants.LEFT);
      label.setMinimumSize(new Dimension(300, 15));
      label.setPreferredSize(new Dimension(300, 15));
      label.setMaximumSize(new Dimension(Short.MAX_VALUE,15));
      label.setAlignmentX(LEFT_ALIGNMENT);

      editor_panel.add(label);

      m_valueText = new JTextArea (10,50);
      m_valueText.setLineWrap(true);
      m_valueText.setWrapStyleWord(true);
      m_valueText.setBorder(new EtchedBorder( EtchedBorder.LOWERED ));

      JScrollPane pane = new JScrollPane (m_valueText);
      pane.setAlignmentX(LEFT_ALIGNMENT);
      editor_panel.add(pane);

      m_okButton = new UTFixedButton(ms_res.getString("okButton"));
      m_okButton.addActionListener(this);
      m_cancelButton = new UTFixedButton(ms_res.getString("cancelButton"));
      m_cancelButton.addActionListener(this);

      JPanel command_panel = new JPanel();
      command_panel.setLayout(
         new BoxLayout(command_panel, BoxLayout.Y_AXIS));
      command_panel.add(m_okButton);
      command_panel.add(Box.createVerticalStrut(5));
      command_panel.add(m_cancelButton);

      panel.add(editor_panel, BorderLayout.CENTER);
      panel.add(command_panel, BorderLayout.EAST);

      pack();
      center();
      return true;
   }

   /**
    * Action Method for button actions.
    *
    * @see ActionListener#actionPerformed
    **/
   public void actionPerformed(ActionEvent event)
   {
      Object obj = event.getSource();

      if(obj == m_okButton)
            onOk();
      else if(obj == m_cancelButton)
          setVisible(false);
   }

   /**
    *  Called when OK is clicked. Sets data modified and updates filter text.
    **/
   public void onOk()
   {
      m_bModified = true;
      String seperator =  ";";
      String escSeperator =";;";
      try {
         seperator =  ms_res.getString("filterSeperator");
         escSeperator = ms_res.getString("escFilterSeperator");
      }
      catch(MissingResourceException e)
      {
         escSeperator = seperator + seperator;
      }                                                

      String text = PSStringOperation.replace(
         m_valueText.getText(), seperator, escSeperator);

      m_filterText = text.replace(
         '\n',   ms_res.getString("filterSeperator").charAt(0));
      setVisible(false);
      dispose();
   }

   /**
    * Accessor function to check whether filter text is modified.
    *
    * @return boolean the modified  flag
    **/
   public boolean isModified()
   {
      return m_bModified;
   }

   /**
    * Converts multi line filter text to single line seperating filters with
    * 'filter seperator(;)' and escaping it if it exists in filter text.
    *
    * @return filter text, never <code>null</code>, may be empty.
    **/
   public String getFilterText()
   {
      return m_filterText;
   }

   /** Button for OK Action, gets initialized in <code>initDialog()</code>. */
   private UTFixedButton m_okButton;

   /** Button for Cancel Action,gets initialized in <code>initDialog()</code>.*/
   private UTFixedButton m_cancelButton;

   /**
    * Text Area for entering filter text,
    * gets initialized in <code>initDialog()</code>.
    */
   private JTextArea m_valueText;

   /**
    * Data Modified flag. Initialized to <code>false</code> and set to
    * <code>true</code> in {@link #onOk() onOk}.
    **/
   private boolean m_bModified = false;

   /**
    * Text entered in text area in a single line, converting all new line
    * characters to 'filterSeperator(;)' and escaping 'filterSeperator(;)' if it
    * exists in text. Initially set to empty string and updated when OK
    * button is clicked.
    */
   private String m_filterText = "";

   /** Dialog resource Strings. **/
   private static ResourceBundle ms_res = null;
}

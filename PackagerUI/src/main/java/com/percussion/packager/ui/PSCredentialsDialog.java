/******************************************************************************
 *
 * [ PSCredentialsDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;

/**
 * Dialog to allow input of log-in credentials.
 * @author erikserating
 *
 */
public class PSCredentialsDialog extends JDialog
{   
  
   /**
    * @param f
    */
   public PSCredentialsDialog(Frame f, String hostport)
   {
      super(f);
      init(hostport);
   }
   
   /**
    * Initialize the dialog layout.
    */
   private void init(String hostport)
   {
      setModal(true);
      setResizable(true);
      setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter()
      {
         @Override
         public void windowClosing(@SuppressWarnings("unused")
         WindowEvent e)
         {
            onCancel();
         }
      });

      JPanel panel = new JPanel();
      getContentPane().add(panel);

      MigLayout layout = new MigLayout("insets dialog", "[][fill, grow]",
               "[fill]10[fill][fill][fill, center]");
      panel.setLayout(layout);
      setTitle(getResourceString("title"));
      JLabel msg = new JLabel( // adding the html tags causes the label to
                               // wrap
               "<html>" +
               MessageFormat.format(
                  getResourceString("message"), new Object[]{hostport}) +
               "</html>");

      JLabel usernameLabel = new JLabel(getResourceString("label.username"));
      JLabel passwordLabel = new JLabel(getResourceString("label.password"));
      m_usernameTextField = new JTextField();
      m_passwordTextField = new JPasswordField();

      panel.add(msg, "wrap, span 2, grow, width 300:300:300");
      panel.add(usernameLabel);
      panel.add(m_usernameTextField, "wrap, grow");
      panel.add(passwordLabel);
      panel.add(m_passwordTextField, "wrap, grow");

      JPanel cmdPanel = new JPanel();
      MigLayout cmdLayout = new MigLayout("center", "[][]", "[]");
      cmdPanel.setLayout(cmdLayout);
      JButton okButton = new JButton(getResourceString("button.ok"));
      cmdPanel.add(okButton, "sg 1");
      okButton.addActionListener(new ActionListener()
      {

         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            onOk();
         }
      });
      JButton cancelButton = new JButton(getResourceString("button.cancel"));
      cmdPanel.add(cancelButton, "sg 1");
      cancelButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            onCancel();
         }
      });
      panel.add(cmdPanel, "span 2, align center");

      pack();
      PSUiUtils.center(this);
   }    

   /**
    * The action performed by the Ok button.
    */
   public void onOk()
   {
      m_username = m_usernameTextField.getText();
      m_password = new String(m_passwordTextField.getPassword());
      m_isOk = true;
      setVisible(false);
   }
   
   /**
    * The action performed by the Cancel/Close button.
    */
   public void onCancel()
   {
      m_isOk = false;
      setVisible(false);
   } 
   
   
   /**
    * Finds out whether OK is pressed in the dialog. Should be called after
    * dialog is disposed.
    *
    * @return <code>true</code> if the dialog is disposed by clicking 'OK',
    * otherwise <code>false</code>.
    */
   public boolean isOk()
   {
      return m_isOk;
   }
   
   /**
    * 
    * @param key
    * @return
    */
   private String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(this.getClass(), key);
   }

   /**
    * @return the username
    */
   public String getUsername()
   {
      return m_username;
   }

   /**
    * @return the password
    */
   public String getPassword()
   {
      return m_password;
   }

   /**
    * Text field for username.
    */
   private JTextField m_usernameTextField;
   
   /**
    *  Password field.
    */
   private JPasswordField m_passwordTextField;
   
   /**
    * The username value for this dialog. May be <code>null</code>
    * or empty.
    */
   private String m_username;
   
   /**
    * The password value for this dialog. May be <code>null</code>
    * or empty.
    */
   private String m_password;
   
   /**
    * Indicates dialog status.
    */
   private boolean m_isOk;

   
}

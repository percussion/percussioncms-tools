/******************************************************************************
 *
 * [ PSServerRegistrationDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.UTComponents.PSPasswordField;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.validation.IntegerConstraint;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The dialog presented to user to register a server, edit its registration or
 * connect to the server.
 */
public class PSServerRegistrationDialog extends PSDialog
{
   /**
    * Constructs the dialog and initializes the controls in the panel. Should
    * be called to register a new server.
    *
    * @param parent the parent frame of this dialog, may be <code>null</code>.
    * @param serverNames list of registered server names with port that needs to
    * be checked for unique server name and port combination, may not be <code>
    * null</code>, can be empty.
    * @param handler the handler that handles the connection to the server and
    * the actions to take about the server repository, may not be
    * <code>null</code>
    *
    * @throws IllegalArgumentException is any param is invalid.
    */
   public PSServerRegistrationDialog(Frame parent, List serverNames,
      PSConnectionHandler handler)
   {
      super(parent);

      if(serverNames == null)
         throw new IllegalArgumentException("serverNames may not be null");

      if(handler == null)
         throw new IllegalArgumentException("handler may not be null");

      m_regServers = serverNames;
      m_edit = false;
      m_connHandler = handler;

      initDialog();
   }

   /**
    * Constructs the dialog and initializes the controls in the panel. Should
    * be called to edit a server registration or connect to a server.
    *
    * @param parent the parent frame of this dialog, may be <code>null</code>.
    * @param deploymentServer the server to edit, may not be <code>null</code>
    * @param handler the handler that handles the connection to the server and
    * the actions to take about the server repository, may not be
    * <code>null</code>
    * @param isConnectOnly if <code>true</code>, user will be able to edit only
    * the login credentials, otherwise user may also edit the server
    * registration (includes port).
    * @param serverNames list of registered server names with port that needs to
    * be checked for unique server name and port combination in case of edit,
    * may not be <code>null</code> in case of edit, can be empty.
    *
    * @throws IllegalArgumentException is any param is invalid.
    */
   public PSServerRegistrationDialog(Frame parent,
      PSDeploymentServer deploymentServer, PSConnectionHandler handler,
      boolean isConnectOnly, List serverNames)
   {
      super(parent);

      if(deploymentServer == null)
         throw new IllegalArgumentException("deploymentServer may not be null");

      if(handler == null)
         throw new IllegalArgumentException("handler may not be null");

      m_server = deploymentServer;
      m_connHandler = handler;
      m_edit = true;
      m_connect = isConnectOnly;

      if(!m_connect && serverNames == null)
         throw new IllegalArgumentException(
            "serverNames may not be null in case of edit");
      m_regServers = serverNames;

      initDialog();
   }

   /**
    * Creates dialog framework and sets the control states and updates data in
    * the controls in case of editing a server registration.
    **/
   private void initDialog()
   {
      if(m_connect)
      {
         setTitle(MessageFormat.format(getResourceString("connTitle"),
            new String[]{m_server.getServerName()}));
      }
      else
         setTitle(getResourceString("title"));

      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);

      if(!m_connect)
      {
         PSPropertyPanel serverPanel = new PSPropertyPanel();
         serverPanel.addPropertyRow(getResourceString("server"),
            new JComponent[] { m_serverField }, m_serverField, 
            getResourceString("server.mn").charAt(0), null);
         serverPanel.addPropertyRow(getResourceString("port"),
            new JComponent[] { m_portField }, m_portField, 
            getResourceString("port.mn").charAt(0), null);
         serverPanel.setAlignmentX(LEFT_ALIGNMENT);
         serverPanel.addControlsRow(m_useSSLCheck, null);
         m_useSSLCheck.setText(getResourceString("useSSL"));
         m_useSSLCheck.setMnemonic(getResourceString("useSSL.mn").charAt(0));
         m_useSSLCheck.setPreferredSize(new Dimension(150, 20));
         m_useSSLCheck.setHorizontalAlignment(SwingConstants.RIGHT);
         panel.add(serverPanel);
         panel.add(Box.createVerticalStrut(20));
         panel.add(Box.createVerticalGlue());
      }

      PSPropertyPanel credentialsPanel = new PSPropertyPanel();
      credentialsPanel.setBorder(
         createGroupBorder(getResourceString("loginCredentials")));
      credentialsPanel.addPropertyRow(getResourceString("username"),
         new JComponent[] { m_userField }, m_userField, 
         getResourceString("username.mn").charAt(0), null);
      credentialsPanel.addPropertyRow(getResourceString("password"),
         new JComponent[] { m_passwordField }, m_passwordField, 
         getResourceString("password.mn").charAt(0), null);
      credentialsPanel.addControlsRow(m_saveCredentialsCheck, null);
      m_saveCredentialsCheck.setText(getResourceString("saveCredentials"));
      m_saveCredentialsCheck.setMnemonic(
                           getResourceString("saveCredentials.mn").charAt(0));
      m_saveCredentialsCheck.setPreferredSize(new Dimension(150, 20));
      credentialsPanel.setAlignmentX(LEFT_ALIGNMENT);
      m_saveCredentialsCheck.setHorizontalAlignment(SwingConstants.RIGHT);
      panel.add(credentialsPanel);
      panel.add(Box.createVerticalStrut(20));
      panel.add(Box.createVerticalGlue());

      JPanel commandPanel = new JPanel();
      commandPanel.setLayout(new BorderLayout());
      commandPanel.add( createCommandPanel(SwingConstants.HORIZONTAL,true), 
                        BorderLayout.EAST);
      
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.add(panel, BorderLayout.NORTH);
      mainPanel.add(commandPanel, BorderLayout.SOUTH);
      getContentPane().add(mainPanel);

      
      if(m_edit)
      {
         PSServerRegistration regDetails = m_server.getServerRegistration();
         if(!m_connect)
         {
            m_serverField.setText(regDetails.getServer());
            m_serverField.setEnabled(false);
            m_portField.setText(String.valueOf(regDetails.getPort()));
         }
         m_userField.setText(regDetails.getUserName());   
         m_saveCredentialsCheck.setSelected(regDetails.isSaveCredentials());            
         m_useSSLCheck.setSelected(regDetails.isUseSSL());
         
         //only offer the password if the credentials are saved.
         if(regDetails.isSaveCredentials())
         {
            if(regDetails.getUserName() != null)
               m_passwordField.resetPasswordField(regDetails.getPassword());
            m_encryptedPassword = regDetails.getPassword();
         }
      }

      pack();
      setResizable(true);
      center();
   }

   /**
    * Creates the validation framework and sets it in the parent dialog. Sets
    * the following validations.
    * <ol>
    * <li>server field is not empty and not duplicate of existing servers in
    * case of new registration</li>
    * <li>port field allows only integers > 0 in case of editing port also</li>
    * <li>user field is not empty</li>
    * </ol>
    */
   private void initValidationFramework()
   {
      List components = new ArrayList();
      List constraints = new ArrayList();

      if(!m_edit)
      {
         components.add(m_serverField);
         constraints.add(new StringConstraint());
      }
      if(!m_connect)
      {
         components.add(m_portField);
         constraints.add(new IntegerConstraint(1, Integer.MAX_VALUE));
      }

      components.add(m_userField);
      constraints.add(new StringConstraint());

      setValidationFramework(
         (Component[])components.toArray(new Component[components.size()]),
         (ValidationConstraint[])constraints.toArray(
            new ValidationConstraint[constraints.size()]) );
   }
   
   /**
    * Gets the right help page based on the current mode of the dialog.
    * 
    * @param helpId name of the class, may not be <code>null</code> or
    * empty.
    * 
    * @return help id corresponding to the mode. Never <code>null</code>
    * or empty.
    * 
    * @throws IllegalArgumentException if any helpId is <code>null</code> or
    * empty.
    */
   protected String subclassHelpId( String helpId )
   {
      if(helpId == null || helpId.trim().length() == 0)
         throw new IllegalArgumentException("helpId may not be null or empty.");
      
      if (m_connect)
         helpId = helpId +"_connect";
      else
         helpId = helpId +"_addedit";

      return helpId;
   }


   /**
    * Validates the user input and makes a connection to the server with the
    * user entered values. If it is editing the existing server registration,
    * then it disconnects from the server if it is connected. Displays an error
    * message if an exception happens getting the connection. Creates or updates
    * the deployment server object with the deployment manager who owns the
    * connection to the server. Disposes the dialog at the end.
    */
   public void onOk()
   {
     //if we are editing a server registration and the server is connected,
     //disconnect
      if(m_edit && m_server.isConnected())
      {
         try
         {
            m_server.disconnect();
         }
         catch(PSDeployException e)
         {
            ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
               getResourceString("error"));
            return;
         }
      }
      initValidationFramework();

      if(!activateValidation())
         return;

      String server;
      int port = 1;
      if(m_connect)
      {
         server = m_server.getServerRegistration().getServer();
         port = m_server.getServerRegistration().getPort();
      }
      else {
         server = m_serverField.getText();
         try {
            port = Integer.parseInt(m_portField.getText());
         }
         catch(NumberFormatException e)
         {
            //we should not get here, as it is validated. Any case if it comes
            //display error dialog
            JOptionPane.showMessageDialog(this, e.getLocalizedMessage());
         }

         //Validate that this server and port combination is not registered.
         boolean validateUnique = true;
         String serverIdentifier = server + ":" + port;
         if(m_edit && m_server.getServerName().equals(serverIdentifier))
            validateUnique = false;

         if(validateUnique && m_regServers.contains(serverIdentifier))
         {
            ErrorDialogs.showErrorMessage(this,
               MessageFormat.format(getResourceString("duplicateReg"), 
               new String[]{serverIdentifier}), getResourceString("error"));
            return;
         }
      }

      String userid = m_userField.getText();
      String password = String.valueOf(m_passwordField.getPassword());

      if(!m_edit)
      {
         try
         {
            m_server = m_connHandler.registerServer(this, server, port, userid,
               password, false, m_saveCredentialsCheck.isSelected(), 
               m_useSSLCheck.isSelected());
         }
         catch(PSDeployException e)
         {
            ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
               getResourceString("error"));
            return;
         }
         if(m_server == null)
            return;
      }
      else
      {
         boolean isEncryptedPW = false;
         if(password.equals(m_encryptedPassword))
            isEncryptedPW = true;

         if( !m_connHandler.connectAndUpdateServerRegistration(
            this, m_server, port, userid, password, isEncryptedPW,
            m_saveCredentialsCheck.isSelected(), m_useSSLCheck.isSelected()) )
         {
            return;
         }
      }

      super.onOk();
   }

   /**
    * Gets the deployment server object created/updated in the dialog.
    *
    * @return the deployment server,  may be <code>null</code> if this is called
    * before <code>onOk()</code> in case of registering a new server.
    */
   public PSDeploymentServer getDeploymentServer()
   {
      return m_server;
   }

   /**
    * The flag to indicate the dialog invoked mode. Set to <code>true</code> to
    * edit the server registration/connection and <code>false</code> to register
    * a new server in the respective constructors. Never modified after that.
    */
   private boolean m_edit;

   /**
    * The flag to indicate whether the dialog is invoked to edit the connection
    * details only. <code>true</code> to edit the server connection details
    * only, <code>false</code> to edit server registration(includes port).
    * Initialized to <code>false</code> and is set to <code>true</code> if the
    * dialog is invoked in edit mode and to edit only connection details.
    */
   private boolean m_connect = false;

   /**
    * The text field to enter the 'CMS Server' name, never <code>null</code>
    * after construction.
    */
   private JTextField m_serverField = new JTextField();

   /**
    * The text field to enter the port number, never <code>null</code>
    * after construction.
    */
   private JTextField m_portField = new JTextField();

   /**
    * The check box for setting the flag on server registration to use SSL or 
    * not, never <code>null</code> after construction.
    */
   private JCheckBox m_useSSLCheck = new JCheckBox();
   
   /**
    * The text field to enter the user name, never <code>null</code>
    * after construction.
    */
   private JTextField m_userField = new JTextField();

   /**
    * The text field to enter the password, never <code>null</code>
    * after construction.
    */
   private PSPasswordField m_passwordField = new PSPasswordField();

   /**
    * The check box for setting the flag on server registration to save the
    * credentials or not, never <code>null</code> after construction.
    */
   private JCheckBox m_saveCredentialsCheck = new JCheckBox();

   /**
    * The password that needs to be checked for equality to find out whether
    * user changed the password or not in this dialog in case of edit. This is
    * required to find out the password getting from <code>m_passwordField
    * </code> is encrypted or not. If we are setting, we set the encrypted
    * password and if user changes, it will be clear text. <code>null</code>
    * until it is initialized in the {@link #initDialog()} and never modified 
    * after that.
    */
   private String m_encryptedPassword = null;

   /**
    * The deployment server that is registered, <code>null</code> until it is
    * created in <code>onOK()</code> in case of new registration, otherwise
    * initialized in the constructor and never <code>null</code> after that.
    */
   private PSDeploymentServer m_server = null;

   /**
    * The list of registered servers, <code>null</code> if this dialog is not
    * constructed to register a server or edit server registration, otherwise it
    * will be initialized in the constructor and never <code>null</code> or
    * modified after that.
    */
   private List m_regServers = null;

   /**
    * The handler that creates connections to the server and handles all actions
    * that need to be taken while registering or editing, initialized in the
    * constructor and never <code>null</code> or modified after that.
    */
   private PSConnectionHandler m_connHandler = null;
}

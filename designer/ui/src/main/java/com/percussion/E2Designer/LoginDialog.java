/* *****************************************************************************
 *
 * [ LoginDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.util.PSFormatVersion;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This is a dialog that lets designers to login to a E2 server, providing
 * a server name, a user name, and a password.
 */
public class LoginDialog extends PSDialog /*implements ActionListener*/
{
   /**
    * The name of a key in the designer properties file. If present, the value
    * of this key will be used as the default for the server field.
    */
   public final static String LAST_SERVER = "last_server_opened";

   /**
    * The name of a key in the designer properties file. If present, the value
    * of this key should contain a delimited list of servers that have previously
    * been connected to. The delimiter to use is UserConfig.DELIMITER. The
    * server combobox will be populated with these values.
    */
   public final static String ALL_SERVERS = "last_all_server_opened";

   /**
    * The name of a key in the designer properties file. If present, the value
    * of this key will be used as the default for the port field.
    */
   public final static String LAST_PORT = "last_port_opened";

   /**
    * The name of a key in the designer properties file. If present, the value
    * of this key will be used as the default for the SSL port field.
    */
   public final static String LAST_SSL_PORT = "last_ssl_port_opened";

   /**
    * The name of a key in the designer properties file. If present, the value
    * is a default protocol name, these two are expected: 'http' or 'https'.
    */
   public final static String LAST_PROTOCOL = "last_protocol_used";

   /**
    * The name of a key in the designer properties file. If present, the value
    * is a user name who connected to rhythmyx last.
    */
   public final static String LAST_USER = "last_user_name";

   //
   // CONSTRUCTORS
   //

   /**
    * Create a login dialog from an existing Frame instance
    * @param f java.awt.Frame used to create a new Dialog based on
    * an existing Frame instance, must not be <code>null</code>
   */
   public LoginDialog(Frame f)
   {
      this(f, null, null);
   }

   /**
    * Create a login dialog from an existing java.awt.Dialog's Frame instance
    * @param d java.awt.Dialog used to create a new Dialog based on
    * an existing Frame instance, must not be <code>null</code>
   */
   public LoginDialog(Dialog d)
   {
      this(d, null, null);
   }


   /**
    * If the 2 Strings passed in are <code>null</code>, the serverName field will be empty.  Same
    * for userName field.
    *
    * @param serverName can be <code>null</code>.
    *        userName can be <code>null</code> as well.
    */
   public LoginDialog(Frame f, String serverName, String userName)
   {
      super(f);

      init(serverName, userName);
   }

   /** If the 2 Strings passed in are <code>null</code>, the serverName field will be empty.  Same
    * for userName field.
    *
    * @param serverName can be <code>null</code>.
    *        userName can be <code>null</code> as well.
    */
   public LoginDialog(Dialog d, String serverName, String userName)
   {
      super(d);

      init(serverName, userName);
   }

   //
   // Property Methods
   //

   /** Returns the port number */
   public String getPort() {   return(m_port); }

   /** Returns the server name. */
   public String getServerName() { return m_server; }

   /** Returns the users name. */
   public String getUserName()   { return m_user; }

   /** Returns the password string. */
   public String getPasswordString() { return m_password; }

   /**
    * @return a protocol name, either 'http' or 'https', never <code>null</code>
    * or <code>empty</code>.
    */
   public String getProtocol()
   {
      return m_useSSL.isSelected() ? "https" : "http";
   }

   /**
    * Gets the local host name or the local host IP address
    * @return a local host name or its IP address, if exception caught
    * <code>null</code>
    */
   public static String getLocalHost()
   {
      String localHost = new String();
      try
      {
         localHost = InetAddress.getLocalHost().getHostName();
         if(localHost == null || localHost.trim().length() == 0)
            localHost = InetAddress.getLocalHost().getHostAddress();
      }
      catch (UnknownHostException e)
      {
         e.printStackTrace();
      }
      return localHost;
   }


   /** Handles the action for pressing the OK button.
    */
   public void onOk()
   {
      m_s1 = new StringConstraint();

      m_constraints[0] = m_s1;
      m_components[0] = m_userField;

      setValidationFramework(m_components, m_constraints);

      if (activateValidation())
      {
         m_server   = m_serverField.getEditor().getItem().toString();
         if(m_server == null || m_server.trim().length() == 0)
            m_server = getLocalHost();
         m_user     = m_userField.getText();
         m_password = new String(m_passwordField.getPassword());
         m_port = m_portField.getText();
         setVisible( false );
      }
   }

   /** Handles the exiting action (either by ESCAPE key or Cancel button).
   */
   public void onCancel()
   {
      m_bCancelled = true;
      super.onCancel();
   }

   /**
    * @return <code>true</code> if the dialog was closed by pressing the Cancel
    * button, <code>false</code> otherwise. Can be called after the caller gets
    * control back.
   **/
   public boolean isCancelled()
   {
      return m_bCancelled;
   }

   public void setVisible( boolean bShow )
   {
      if ( bShow )
         m_bCancelled = false;
      super.setVisible( bShow );
   }

   //
   // PRIVATE METHODS
   //   
   /** Constructs all the Labels for this dialog. */
   private JPanel initializeLabels()
   {
      char mn;
      String labelStr = getResources().getString("server");
      m_serverLabel = new JLabel(labelStr, SwingConstants.RIGHT);
      m_serverLabel.setPreferredSize(new Dimension(60, 15));
      mn = getResources().getString("server.mn").charAt(0);
      m_serverLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      m_serverLabel.setDisplayedMnemonic(mn);
      
      labelStr = getResources().getString("user");
      m_userLabel = new JLabel(labelStr, SwingConstants.RIGHT);    
      mn = getResources().getString("user.mn").charAt(0);
      m_userLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      m_userLabel.setDisplayedMnemonic(mn);
      
      labelStr = getResources().getString("password");
      m_passwordLabel = new JLabel(labelStr, SwingConstants.RIGHT);
      mn = getResources().getString("password.mn").charAt(0);
      m_passwordLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      m_passwordLabel.setDisplayedMnemonic(mn);
      
      labelStr = getResources().getString("port");
      m_portLabel = new JLabel(labelStr, SwingConstants.RIGHT);
      mn = getResources().getString("port.mn").charAt(0);
      m_portLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      m_portLabel.setDisplayedMnemonic(mn);
      
      labelStr = getResources().getString("useSSL");
      m_useSSLLabel = new JLabel(labelStr, SwingConstants.RIGHT);
      mn = getResources().getString("useSSL.mn").charAt(0);
      m_useSSLLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      m_useSSLLabel.setDisplayedMnemonic(mn);
      
      JPanel labelPanel = new JPanel(new GridLayout(5,1));
      ((GridLayout)labelPanel.getLayout()).setVgap(10);
      labelPanel.add(m_serverLabel);
      labelPanel.add(m_portLabel);
      labelPanel.add(m_userLabel);
      labelPanel.add(m_passwordLabel);
      labelPanel.add(m_useSSLLabel);

      return labelPanel;
   }

   /** Constructs all Changing data fields. serverName and userName can be <code>null</code>.
    */
   private JPanel initializeFields(String serverName, String userName)
   {
      serverName = E2Designer.getDesignerProperties().getProperty(LAST_SERVER);

      final String lastProtocolName = E2Designer.getDesignerProperties()
         .getProperty(LAST_PROTOCOL, "http");

      final String lastPortName = E2Designer.getDesignerProperties()
         .getProperty(LAST_PORT, "" + E2Designer.DEFAULT_PORT);

      final String lastSSLPortName = E2Designer.getDesignerProperties()
         .getProperty(LAST_SSL_PORT, "" + E2Designer.DEFAULT_SSL_PORT);

      m_serverField = new PSComboBox();
      m_serverLabel.setLabelFor(m_serverField);

      m_serverField.setEditable(true);
      if(serverName == null || serverName.trim().length() == 0)
         serverName = getLocalHost();

      String strServers =
         E2Designer.getDesignerProperties().getProperty(ALL_SERVERS);
      if(strServers != null)
      {
         StringTokenizer tokens =
            new StringTokenizer(strServers, UserConfig.DELIMITER);
         while(tokens.hasMoreTokens())
            m_serverField.addItem(tokens.nextToken());
      }

      if(serverName != null)
         m_serverField.setSelectedItem(serverName);

      if (userName==null || userName.trim().length()<=0)
      {
         userName = E2Designer.getDesignerProperties().getProperty(LAST_USER);
         if (userName==null || userName.trim().length()<=0)
            userName = System.getProperty("user.name");
      }

      if (userName == null)
         m_userField = new JTextField(30);
      else
         m_userField = new JTextField(userName, 30);
      m_userLabel.setLabelFor(m_userField);

      m_userField.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_btnLogin.doClick();
         }
      });

      m_useSSL = new JCheckBox();
      m_useSSLLabel.setLabelFor(m_useSSL);

      m_useSSL.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            //flip the port number
            onUseSSL();
         }
      });

      if (lastProtocolName!=null &&
          lastProtocolName.trim().compareToIgnoreCase("https")==0)
      {
         m_useSSL.setSelected(true);
         m_portField = new JTextField(lastSSLPortName, 10);
      }
      else
      {
         m_useSSL.setSelected(false);
         m_portField = new JTextField(lastPortName, 10);
      }
      
      m_portLabel.setLabelFor(m_portField);
      m_portField.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_btnLogin.doClick();
         }
      });

      m_passwordField = new JPasswordField(30);
      m_passwordField.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_btnLogin.doClick();
         }
      });
      m_passwordLabel.setLabelFor(m_passwordField);
      
      JPanel fieldPanel = new JPanel(new GridLayout(5, 1));
      ((GridLayout)fieldPanel.getLayout()).setVgap(10);
      fieldPanel.add(m_serverField);
      fieldPanel.add(m_portField);
      fieldPanel.add(m_userField);
      fieldPanel.add(m_passwordField);
      fieldPanel.add(m_useSSL);

      return fieldPanel;
   }

   /**
    * Flips last used port number depending on whether useSSL is checked or not.
    */
   private void onUseSSL()
   {
      boolean useSSL = m_useSSL.isSelected();
      String curPort = m_portField.getText();
      Properties e2prop = E2Designer.getDesignerProperties();

      //save port if already entered, so that user can switch back
      if (curPort!=null && curPort.trim().length()>0)
         e2prop.setProperty(useSSL ? LAST_PORT : LAST_SSL_PORT, curPort);

      //get the last one and then flip it
      String lastPort = e2prop.getProperty(useSSL ? LAST_SSL_PORT : LAST_PORT);

      if (lastPort!=null && lastPort.trim().length()>0)
         m_portField.setText(lastPort);
      else
         m_portField.setText(useSSL ?
            E2Designer.DEFAULT_SSL_PORT : E2Designer.DEFAULT_PORT);
   }

   /** Constructs the buttons used in this dialog.
   */
   private JPanel initializeButtons()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      m_btnLogin = new UTFixedButton(getResources().getString("login"));

      m_btnLogin.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
              onOk();
            }
         }
      );

      panel.add(m_btnLogin);

      //make it a default button
      getRootPane().setDefaultButton( m_btnLogin );

      return panel;
   }

   /** Simply used for different constructors to share identical functionalities.
   */
   private void init(String serverName, String userName)
   {
      JPanel mainPanel = new JPanel(new BorderLayout()); 
      JPanel topPanel  = new JPanel(new BorderLayout());
      JPanel labels = initializeLabels();
      labels.setBorder( BorderFactory.createEmptyBorder(4,4,3,2));
      topPanel.add(labels, BorderLayout.WEST);
      JPanel dummyPanel = new JPanel();
      topPanel.add(dummyPanel, BorderLayout.EAST);
      mainPanel.add(topPanel, BorderLayout.CENTER);

      JPanel fields = initializeFields(serverName, userName);
      fields.setBorder( BorderFactory.createEmptyBorder(4,2,3,2));
      topPanel.add(fields, BorderLayout.CENTER);

      JPanel buttonsPanel = initializeButtons();
      buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ));
      topPanel.add(buttonsPanel, BorderLayout.SOUTH);

      PSFormatVersion versionInfo = 
         new PSFormatVersion("com.percussion.E2Designer");
      String [] params =
      {
         E2Designer.getResources().getString( "ProductName" ),
         versionInfo.getVersionString()
      };
      String info = MessageFormat.format(
         E2Designer.getResources().getString( "LoginVersionInfo" ), params);

      JLabel versionLabel = new JLabel( info, JLabel.CENTER );
      versionLabel.setBorder( BorderFactory.createCompoundBorder(
         BorderFactory.createEmptyBorder( 4, 4, 4, 4 ),
         BorderFactory.createBevelBorder( BevelBorder.LOWERED )));
      mainPanel.add(topPanel, BorderLayout.NORTH);
      mainPanel.add(versionLabel, BorderLayout.SOUTH);

      getContentPane().add(mainPanel);
      //for convenience automatically make an appropriate control selected
      final Component selComp;
      String lastUser = E2Designer.getDesignerProperties().getProperty(LAST_USER);
      if (lastUser!=null && lastUser.trim().length()>0)
         selComp = m_passwordField;
      else
         selComp = m_userField;

      addWindowListener( new WindowAdapter() {
            public void windowOpened( WindowEvent e ){
               selComp.requestFocus();
            }
         }
      );

      setResizable(true);
      pack();
      center();
   }

   /** server name combo box UI field
   */
   private PSComboBox m_serverField;

   /** user name UI entry field
   */
   private JTextField m_userField;

   /** port number UI entry field
   */
   private JTextField m_portField;

   /** password UI entry field
   */
   private JPasswordField m_passwordField;

   /** use SSL check box UI field
    */
   private JCheckBox m_useSSL;

   /** Login button instance, created by {@link #initializeButtons()},
    *  never <code>null</code> after that.
   */
   private JButton m_btnLogin;

   /** these hold data entered by user
   */
   private String m_server, m_password, m_user, m_port;

   /** holds standard user entry validation constants
   */
   private StringConstraint m_s1;

   /** validation constaints interface used for user entry validation
   */
   private ValidationConstraint[] m_constraints = new ValidationConstraint[1];

   /** used for the UI field entry validation, @see java.awt.Component
   */
   private Component[] m_components = new Component[1];

   /** set to true if user clicks on "Cancel"
   */
   private boolean m_bCancelled = false;
   
   /* Labels */
   private JLabel m_serverLabel;
   private JLabel m_userLabel;
   private JLabel m_passwordLabel;
   private JLabel m_portLabel;
   private JLabel m_useSSLLabel;
}



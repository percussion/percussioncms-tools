/******************************************************************************
 *
 * [ LoginPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.LoginDialog;
import com.percussion.E2Designer.PSComboBox;
import com.percussion.E2Designer.UTFixedPasswordField;
import com.percussion.E2Designer.UTFixedTextField;
import com.percussion.E2Designer.UTMnemonicLabel;
import com.percussion.E2Designer.UserConfig;
import com.percussion.E2Designer.Util;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.StringTokenizer;


/**
 * The LoginPanel creates the applets/applications first dialog shown to the user.
 * It askes for server name, user Id and password and provides a login button.
 * AppletMainDialog will be started on successful login.
 */
////////////////////////////////////////////////////////////////////////////////
public class LoginPanel extends JPanel
{
   /**
    * The name of the file where various properties are stored, such as the
    * last server name, port #, etc.
   **/
   public static final String PROPERTIES_FILENAME = "admin.properties";

   /**
    * Construct the login applet/application.
    *
    * @param parent the parent frame, never <code>null</code>
    * @param applet <code>true</code> if this was started as an applet,
    * <code>false</code> otherwise.
    * @param server the server to login to, can be <code>null</code>.
    * @param protocol the protocol name, can be <code>null</code> in which case
    * it defaults to 'http'.
    * @param port the servers port to use, can be  <code>null</code>.
    */
   public LoginPanel(Frame parent, boolean applet,
      String server, String protocol, String port)
   {
      try
      {
         m_parent = parent;
         m_applet = applet;

         initPanel();
         initData (server, protocol, port);
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(this,
            Util.cropErrorMessage(e.getMessage()),  m_res.getString("error"),
            JOptionPane.ERROR_MESSAGE);
      }
   }

   /**
    * Create and initialize all GUI elements.
    * @throws Exception creating GUI element failed.
    */
   private  void initPanel() throws Exception
   {
      setLayout(new BorderLayout());
      setBorder(new EmptyBorder(5, 5, 5, 5));

      JPanel p1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      p1.add(new UTMnemonicLabel(m_res, "server", m_server));
      p1.add(m_server);

      JPanel p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      p2.add(new UTMnemonicLabel(m_res, "userId", m_userId));
      p2.add(m_userId);

      JPanel p3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      p3.add(new UTMnemonicLabel(m_res, "password", m_password));
      m_password.enableInputMethods(true);
      p3.add(m_password);

      JPanel p4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      p4.add(new UTMnemonicLabel(m_res, "port", m_port));
      p4.add(m_port);

      JPanel p5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
      p5.add(new UTMnemonicLabel(m_res, "useSSL", m_useSSL));
      p5.add(m_useSSL);
      p5.setBorder(new EmptyBorder(0, 10, 0, 0));
      m_server.setPreferredSize(m_port.getPreferredSize());
      m_server.setEditable(true);

      Box b = new Box(BoxLayout.Y_AXIS);
      b.add(p1);
      b.add(p4);
      b.add(p2);
      b.add(p3);

      if (!m_applet)
      {
         b.add(p5);
      }
      else
      {
          /*If we run as an applet, then we already know what protocol
            was used to connect to the server, hence we don't need to
            show the 'useSSL' checkbox.
          */
      }

      JPanel p6 = new JPanel(new BorderLayout());
      JPanel p7 = new JPanel(new BorderLayout());
      p7.add(createCommandPanel(), BorderLayout.EAST);
      p6.add(p7, BorderLayout.NORTH);
      p6.add(m_statusBar, BorderLayout.SOUTH);
      
      add(b, "Center");
      
      add(p6, "South");
      setBorder(new EtchedBorder(EtchedBorder.LOWERED));
      
      // Widen dialog
      Dimension size = getPreferredSize();
      size.width += 60;
      setPreferredSize(size);
   }

   /**
    * Initializes the login panel with data
    * @param server the server to login to, can be <code>null</code>
    * @param protocol the protocol name, can be <code>null</code> in which case
    * it defaults to 'http'.
    * @param port the servers port to use, can be <code>null</code>
    */
   private void initData(String server, String protocol, String port)
   {
     /*When any entry except the last in the combo box is present in the edit
      control and the tab key is pressed, it causes this event to be activated.
      This makes it impossible to log into any machine except the last one you
      successfully logged into. This looks like a problem in Swing or lower.*/
      //m_server.addActionListener(new LoginHandler());

      m_userId.addActionListener(new LoginHandler());
      m_password.addActionListener(new LoginHandler());
      m_port.addActionListener(new LoginHandler());

      // set defaults
      if (m_applet)
      {
         if (server != null)
         {
            m_server.addItem(server);
            m_server.setSelectedItem(server);
            m_server.setEditable(false);
            m_server.setEnabled(false);
            m_server.setBackground(Color.lightGray);

            if (port != null)
               m_port.setText(port);

            if(protocol != null &&
               protocol.trim().compareToIgnoreCase("https")==0)
            {
               m_useSSL.setSelected(true);

               if (port == null)
                  m_port.setText(DEFAULT_SSL_PORT);
            }
            else
            {
               m_useSSL.setSelected(false);

               if (port == null)
                  m_port.setText(DEFAULT_PORT);
            }

         }
      }
      else
      {
         File file = null;
         try
         {
            file = PSProperties.getConfig(LoginPanel.ENTRY_NAME,LoginPanel.PROPERTIES_FILENAME,  LoginPanel.ADMIN_DIR);

            if(file != null)
               m_adminProps = new PSProperties (file.getAbsolutePath());

            // we use the same key names as the workbench
            String strServer =
               m_adminProps.getProperty( LoginDialog.LAST_SERVER );

            initServerField(strServer);

            String prot = m_adminProps.getProperty(LoginDialog.LAST_PROTOCOL);
            if(prot != null &&
               prot.trim().compareToIgnoreCase("https")==0)
            {
               m_useSSL.setSelected(true);
               String prt = m_adminProps.getProperty(LoginDialog.LAST_SSL_PORT);
               if(prt!=null && prt.trim().length()>0)
                  m_port.setText(prt);
               else
                  m_port.setText(DEFAULT_SSL_PORT);
            }
            else
            {
               m_useSSL.setSelected(false);
               String prt = m_adminProps.getProperty(LoginDialog.LAST_PORT);
               if(prt!=null && prt.trim().length()>0)
                  m_port.setText(prt);
               else
                  m_port.setText(DEFAULT_PORT);
            }

            String user = m_adminProps.getProperty(LoginDialog.LAST_USER);
            if(user!=null && user.trim().length()>0)
               m_userId.setText(user);
            else
               m_userId.setText(System.getProperty("user.name"));

         }
         catch(IOException e) //I don't want to show any dialog here
         {
            System.out.println(
               "Couldn't find properties file: " + file.getPath() );
         }
      }
   }

   /** @return JButton The login button for the applet to give the DefaultButton
    * access.
    */
   public JButton getLoginButton()
   {
      return m_login;
   }

   /**
    * Initializes the server combo box
    * @param strServer the name of the server to login to,
    * can not be <code>null</code>
    */
   private void initServerField(String strServer)
   {
      if(m_adminProps != null) {
         String allServers = m_adminProps.getProperty(LoginDialog.ALL_SERVERS);
         if (allServers != null) {
            StringTokenizer tokens = new StringTokenizer(allServers, ";");
            while (tokens.hasMoreTokens()) {
               m_server.addItem(tokens.nextToken());
            }
         }
      }
      if(strServer == null || strServer.trim().length() == 0)
         strServer = LoginDialog.getLocalHost();

      if (strServer != null)
         m_server.setSelectedItem(strServer);
   }

   /**
    * Create the command panel, which provides th elogin button.
    *
    * @return The panel, never <code>null</code>.
    * 
    * @throws Exception If creating any GUI element failed.
    */
   //////////////////////////////////////////////////////////////////////////////
   private  JPanel createCommandPanel() throws Exception
   {
    m_login.addActionListener(new ActionListener()
    {
      public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
      {
        onOk();
      }
    });

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(m_login);

    return panel;
   }

   /**
    * Implements the ActionListener for the login button. Since we will connect
    * to the server, the action will be handled in a worker thread to give the
    * system the possibility to update the GUIs while waiting for th econnection.
    */
   public void onOk()
   {
      try
      {
         m_login.setCursor(
            m_parent.getCursor().getPredefinedCursor(Cursor.WAIT_CURSOR));
         m_statusBar.setStatusText(m_res.getString("connectingStatus"));


         if(m_userId.getText() == null ||
            m_userId.getText().trim().length() == 0)
         {
            JOptionPane.showMessageDialog(this,
               Util.cropErrorMessage(m_res.getString("missUserId")),
               m_res.getString("error"), JOptionPane.ERROR_MESSAGE);
            return;
         }

         m_login.setEnabled(false);

         //use default port if not specified
         String port = DEFAULT_PORT;
         if(m_port.getText() != null && m_port.getText().trim().length() != 0)
            port = m_port.getText().trim();

         m_connection = new ServerConnection(port);
         String serverName = (String)m_server.getSelectedItem();

         //if the server name not supplied use the local host name
         if(serverName == null || serverName.trim().length() == 0)
            serverName = LoginDialog.getLocalHost();

         String protocol = m_useSSL.isSelected() ? "https" : "http";

         if (protocol == null || protocol.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Protocol cannot be Null",
                    m_res.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return;
         }

         if (serverName == null || serverName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Server cannot be Null",
                    m_res.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return;
         }
         if (m_userId.getText() == null || m_userId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "UserId  cannot be Null",
                    m_res.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return ;
         }
         if (m_password.getText() == null || m_password.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Password  cannot be Null",
                    m_res.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return ;
         }


         if (m_connection.login(serverName, m_userId.getText(),
             m_password.getText(), protocol))
         {

            m_statusBar.setStatusText(m_res.getString("connectedStatus") +
               serverName);
            Object[] params = { protocol, serverName, port};
            m_mainDialog = new AppletMainDialog(
               MessageFormat.format(m_res.getString("title"), params),
               m_connection, m_applet);
            m_mainDialog.addWindowListener(new WindowAdapter()
            {
               public void windowClosed(
                  @SuppressWarnings("unused") WindowEvent event)
               {
                  m_statusBar.setStatusText(
                     m_res.getString("disconnectedStatus"));
                  m_login.setEnabled(true);
                  m_mainDialog = null;
                  if (!m_applet)
                  {
                     // close the main application too
                     System.exit(0);
                  }
               }
            });

            //mainDialog.setIconImage(m_parent.getIconImage());
            m_mainDialog.pack();

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension size = m_mainDialog.getSize();
            m_mainDialog.setLocation((screenSize.width - size.width) / 2,
                                     (screenSize.height - size.height) / 2);

            m_mainDialog.setVisible(true);

            /*effectively making the login dialog disappear after successful
            login*/
            if ( !m_applet )
            {
               updateAdminProperties();
               saveAdminProperties();
               m_parent.dispose();
            }
         }
         else
         {
            m_statusBar.setStatusText(m_res.getString("failedStatus"));
            m_login.setEnabled(true);
         }
      }
      catch ( UserCancelledException e )
      {
         // don't do anything, just finish
         m_connection.logout();
         m_statusBar.setStatusText(m_res.getString("canceledStatus"));
         m_login.setEnabled(true);
      }
      catch (PSAuthenticationFailedException e)
      {
         m_statusBar.setStatusText(m_res.getString("notAuthorizedStatus"));
         m_login.setEnabled(true);
      }
      catch (PSAuthorizationException e)
      {
         m_statusBar.setStatusText(m_res.getString("notAuthorizedStatus"));
         m_login.setEnabled(true);
      }
      catch (Exception e)
      {
         String message = "Connection to Server Failed. Invalid Server Configurations/Credentials";
         if(e.getCause() != null && e.getCause().getMessage() != null){
            message = e.getCause().getLocalizedMessage();
         }
         Util.showStackTraceDialog(e,"Error",message);
         m_connection.logout();
         m_login.setEnabled(true);
      }
      finally
      {
         m_login.setCursor(Cursor.getDefaultCursor());
         m_statusBar.repaint();
      }
   }

   /**
    * The browser is being destroyed. Cleanup the applet.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void onDestroy()
   {
      if (m_mainDialog != null)
      {
         m_mainDialog.close();
         m_mainDialog = null;
      }

      if (m_connection != null)
         m_connection.logout();
   }


   /** Simply accepts <CODE>ENTER</CODE> key press as a "Login" button push.
    */
   public class LoginHandler implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         m_login.doClick();
      }
   }

   /**
    * Updates the admin properties
    */
   private void updateAdminProperties()
   {
      if(m_adminProps != null)
      {
         boolean found = false;

         if ((String)m_server.getSelectedItem() == null)
            return;

         /*set the last accessed server; if the user left the server name
         field blank means he/she accessed the local host, so set the name
         of the local host as the last accessed server*/
         String lastAccessedServer = (String)m_server.getSelectedItem();
         if(lastAccessedServer.trim().length() == 0)
            lastAccessedServer = LoginDialog.getLocalHost();
         m_adminProps.setProperty(LoginDialog.LAST_SERVER, lastAccessedServer);

         String strServers = m_adminProps.getProperty(LoginDialog.ALL_SERVERS);
         if(strServers == null)
            strServers = "";

         StringTokenizer tokens = new StringTokenizer(strServers,
            UserConfig.DELIMITER);

         //add the server to the server list if not already in
         while(tokens.hasMoreTokens())
         {
            if (tokens.nextToken().equals(lastAccessedServer))
            {
               found = true;
               break;
            }
         }

         if(!found && lastAccessedServer != null &&
            lastAccessedServer.trim().length() != 0)
               strServers += lastAccessedServer + UserConfig.DELIMITER;

         m_adminProps.setProperty(LoginDialog.ALL_SERVERS, strServers);

         m_adminProps.setProperty(LoginDialog.LAST_PROTOCOL,
            m_useSSL.isSelected() ? "https" : "http");

         m_adminProps.setProperty(m_useSSL.isSelected() ?
            LoginDialog.LAST_SSL_PORT : LoginDialog.LAST_PORT, m_port.getText());

         m_adminProps.setProperty(LoginDialog.LAST_USER, m_userId.getText());
      }
    }

   /**
    * Saves the admin.properties file
    */
   private void saveAdminProperties()
   {
      if(m_adminProps != null)
      {
         File file = null;
         try
         {
            /*get the admin.properties file where properties were loaded from
             and save changes to it */
            file = PSProperties.getConfig(ENTRY_NAME, PROPERTIES_FILENAME,
               ADMIN_DIR);
            if(file != null)
               m_adminProps.store(new FileOutputStream(
                  file.getAbsolutePath()), null);
         }
         catch(IOException e)
         {
            e.printStackTrace();
         }
      }

   }

   //////////////////////////////////////////////////////////////////////////////
   /**
    * the parent frame
    */
  private Frame m_parent = null;

   /**
    * the E2 server connection
    */
  private ServerConnection m_connection = null;

   /**
    * editable text field for port
    */
  private JTextField m_port = new UTFixedTextField("9992");

   /**
    * Editable combo box for server name
    */
    private PSComboBox m_server = new PSComboBox();

   /**
    * editable text field for user identification
    */
  private JTextField m_userId = new UTFixedTextField("");

  /**
   * use SSL check box
   */
  private JCheckBox  m_useSSL = new JCheckBox();

   /**
    * editable password field for user password
    */
  private JTextField m_password = new UTFixedPasswordField("");

   /**
    * the login button
    */
  private JButton m_login = new UTFixedButton(m_res.getString("login"));

   /**
    * status bar, informing the user about the applets/applications state
    */
  private StatusBar m_statusBar = new StatusBar(m_res.getString("disconnectedStatus"));

  /** Admin properties gets initialized in <code>initPanel</code>*/
  private PSProperties m_adminProps = null;

   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();

  /**
   * status if we were started from an applet or an application.
   */
  private static boolean m_applet = true;

   /**
    * The main application. It is created after a successful login. Its valid
    * until the user quits the application or the browser receives a destroy
    * message, which closes this dialog too.
    */
   private AppletMainDialog m_mainDialog = null;

   /**
    * Constant for the name of the entry that reperesents admin's name/value
    * pair.
    */
   public static final String ENTRY_NAME = "admin_config_base_dir";

    /**
    * Constant for the directory containing admin client configs.
    * Assumed to be relative to the Rx directory.
    */
    public static final String ADMIN_DIR = "../rxconfig/Workbench";

    /**
     * Constant for default port number '9992'.
     */
    public static final String DEFAULT_PORT = "9992";

    /**
     * Constant for default SSL port number '9443'.
     */
    public static final String DEFAULT_SSL_PORT = "9443";
}


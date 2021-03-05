package com.percussion.cx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;

import org.apache.axis.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.percussion.E2Designer.LoginDialog;
import com.percussion.E2Designer.UTFixedPasswordField;
import com.percussion.E2Designer.UTFixedTextField;
import com.percussion.E2Designer.UTMnemonicLabel;
import com.percussion.E2Designer.UserConfig;
import com.percussion.E2Designer.Util;
import com.percussion.E2Designer.admin.AppletMainDialog;
import com.percussion.E2Designer.admin.StatusBar;
import com.percussion.border.PSFocusBorder;
import com.percussion.util.PSProperties;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSNotAuthenticatedFault;

/**
 * The LoginPanel creates the applets/applications first dialog shown to the
 * user. It askes for server name, user Id and password and provides a login
 * button. AppletMainDialog will be started on successful login.
 */
////////////////////////////////////////////////////////////////////////////////
public class PSContentExplorerLoginPanel extends JFrame
{
   static Logger log = Logger.getLogger(PSContentExplorerLoginPanel.class);

   /**
    * The name of the file where various properties are stored, such as the last
    * server name, port #, etc.
    **/
   public static final String PROPERTIES_FILENAME = "admin.properties";

   public PSContentExplorerApplet applet;

   public PSContentExplorerLoginPanel(PSContentExplorerFrame parent, PSContentExplorerApplet applet)
   {
      // blank constructor in case you already have a session

      super(PSContentExplorerHelper.getResources().getString("titlelogin"));

      m_parent = parent;
      this.applet = applet;

      // add the applet as the child, so when the applet ref is called it will
      // be there
      // add the applet as the child, so when the applet ref is called it will
      // be there
      m_res = applet.getResources();
      m_login = new JButton(m_res.getString("login"));
      m_login.setFont(new Font("Arial", Font.BOLD, 18));
      FontMetrics metrics = getFontMetrics(m_login.getFont());
      int width = metrics.stringWidth(m_login.getText());
      int height = metrics.getHeight();
      Dimension newDimension = new Dimension(width + 100, height + 100);
      m_login.setSize(newDimension);
      m_login.setMinimumSize(newDimension);

      m_login.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onOk();
         }
      });

      m_statusBar = new StatusBar(m_res.getString("disconnectedStatus"));
      for (Component comp : m_statusBar.getComponents())
      {
         comp.setFont(new Font("Arial", Font.BOLD, 18));
      }

      this.setMinimumSize(new Dimension(400, 200));
      try
      {
         initData();
         initPanel();
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(e.getMessage()), m_res.getString("error"),
               JOptionPane.ERROR_MESSAGE);
      }

   }

   /**
    * Create and initialize all GUI elements.
    * 
    * @throws Exception creating GUI element failed.
    */
   private void initPanel() throws Exception
   {
      PSFocusBorder focusBorder = new PSFocusBorder(1, Color.RED);

      this.setLayout(new BorderLayout());

      JPanel panel = new JPanel();

      panel.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.weighty = 0;
      c.weightx = 0.2;
      c.gridx = 0;
      c.ipadx = 10;
      c.ipady = 10;
      c.gridy = 0;
      c.insets = new Insets(5, 20, 0, 50); // top padding
      c.anchor = GridBagConstraints.LINE_START;
      c.fill = 0;
      UTMnemonicLabel p1alabel = new UTMnemonicLabel(m_res, "serverurl", m_url);
      p1alabel.setMinimumSize(new Dimension(150, 60));
      p1alabel.setFont(new Font("Arial", Font.BOLD, 18));
      p1alabel.setLabelFor(m_url);
      panel.add(p1alabel, c);
      c.weightx = 1.0;
      c.gridx = 1;
      c.gridy = 0;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(5, 0, 0, 20); // top padding
      m_url.setFont(new Font("Arial", Font.PLAIN, 18));

      FontMetrics metrics = getFontMetrics(m_url.getFont());
      int width = metrics.stringWidth(m_url.getText());
      int height = metrics.getHeight();
      Dimension newDimension = new Dimension(width + 40, height + 10);

      m_url.setMinimumSize(newDimension);
      panel.add(m_url, c);

      UTMnemonicLabel p2label = new UTMnemonicLabel(m_res, "userId", m_userId);
      p2label.setLabelFor(m_userId);
      p2label.setMinimumSize(new Dimension(150, 60));
      p2label.setFont(new Font("Arial", Font.BOLD, 18));
      c.fill = GridBagConstraints.NONE;
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0.2;
      c.insets = new Insets(5, 20, 0, 50); // top padding
      panel.add(p2label, c);
      c.gridx = 1;
      c.gridy = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      m_userId.setMinimumSize(new Dimension(300, 60));
      m_userId.setFont(new Font("Arial", Font.PLAIN, 18));
      c.insets = new Insets(5, 0, 0, 20); // top padding
      panel.add(m_userId, c);

      UTMnemonicLabel p3Label = new UTMnemonicLabel(m_res, "password", m_password);
      p3Label.setLabelFor(m_password);
      p3Label.setMinimumSize(new Dimension(150, 60));
      p3Label.setFont(new Font("Arial", Font.BOLD, 18));
      c.weightx = 0.2;
      c.fill = 0;
      c.gridx = 0;
      c.gridy = 2;
      c.fill = GridBagConstraints.NONE;
      c.insets = new Insets(5, 20, 0, 50); // top padding
      panel.add(p3Label, c);
      c.gridx = 1;
      c.gridy = 2;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(5, 0, 0, 20); // top padding
      panel.add(m_password, c);
      m_password.setMinimumSize(new Dimension(300, 60));
      m_password.setFont(new Font("Arial", Font.PLAIN, 18));

      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 3;
      c.weighty = 1.0;
      c.gridwidth = 2;
      c.anchor = GridBagConstraints.SOUTH;
      c.fill = GridBagConstraints.HORIZONTAL;

      panel.add(Box.createVerticalStrut(30), c);

      c.gridx = 1;
      c.gridy = 4;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0;
      c.weighty = 0;
      c.gridwidth = 2;
      c.anchor = GridBagConstraints.EAST;
      c.insets = new Insets(10, 0, 0, 20); // top padding
      panel.add(m_login, c);

      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 5;
      c.weighty = 0;
      c.gridwidth = 2;
      c.insets = new Insets(10, 0, 0, 0);; // top padding
      c.anchor = GridBagConstraints.SOUTH;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(m_statusBar, c);

      panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

      focusBorder.addToAllNavigable(panel);
      add(panel, BorderLayout.CENTER);

      this.pack();
      this.setMinimumSize(this.getSize());

      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
     this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
     
     m_password.requestFocusInWindow();
   }

   /**
    * Initializes the login panel with data
    * 
    * @param server the server to login to, can be <code>null</code>
    * @param protocol the protocol name, can be <code>null</code> in which case
    *           it defaults to 'http'.
    * @param port the servers port to use, can be <code>null</code>
    */
   private void initData()
   {

      m_userId.addActionListener(new LoginHandler());
      m_password.addActionListener(new LoginHandler());

      File file = null;
      try
      {
         file = new File(PSContentExplorerApplication.getConfigDir(), PROPERTIES_FILENAME);

         if (file.exists())
            m_adminProps = new PSProperties(file.getAbsolutePath());
         else
            m_adminProps = new PSProperties();

         String server = m_parent.getParameter("serverName");

         String prot = m_parent.getParameter("protocol");
         String prt = m_parent.getParameter("port");

         m_url.setEditable(false);
         m_url.setBorder(null);

         String user = m_adminProps.getProperty(LoginDialog.LAST_USER);
         if (user != null && user.trim().length() > 0)
            m_userId.setText(user);
         else
            m_userId.setText(System.getProperty("user.name"));

         if (server == null || prot == null)
         {
            m_url.setText("");
            m_url.setEditable(true);
         }
         else
         {
            String url = prot + "://" + server;

            if (!((prot.equalsIgnoreCase("https") && prt.equals("443"))
                  || (prot.equalsIgnoreCase("http") && prt.equals("80"))))
            {
               url += ":" + prt;
            }
            m_url.setText(url);
         }
      }
      catch (IOException e) // I don't want to show any dialog here
      {
         log.error("Couldn't find properties file: " + file.getPath());
      }

   }

   /**
    * @return JButton The login button for the applet to give the DefaultButton
    *         access.
    */
   public JButton getLoginButton()
   {

      return m_login;
   }

   /**
    * Implements the ActionListener for the login button. Since we will connect
    * to the server, the action will be handled in a worker thread to give the
    * system the possibility to update the GUIs while waiting for th
    * econnection.
    */
   public void onOk()
   {

      this.setCursor(getCursor().getPredefinedCursor(Cursor.WAIT_CURSOR));

      if (m_userId.getText() == null || m_userId.getText().trim().length() == 0)
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(m_res.getString("missUserId")),
               m_res.getString("error"), JOptionPane.ERROR_MESSAGE);
         this.setCursor(getCursor().getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         m_statusBar.setStatusText(m_res.getString("disconnectedStatus"));
         m_userId.requestFocus();
         return;
      }
      
      if (m_password.getText() == null || m_password.getText().trim().length() == 0)
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(m_res.getString("missPassword")),
               m_res.getString("error"), JOptionPane.ERROR_MESSAGE);
         this.setCursor(getCursor().getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         m_statusBar.setStatusText(m_res.getString("disconnectedStatus"));
         m_password.requestFocus();
         return;
      }
      
      m_statusBar.setStatusText(m_res.getString("connectingStatus"));
      m_login.setEnabled(false);

      String protocol = null;
      String host = null;
      String port = null;

      try
      {
         if (StringUtils.isNotEmpty(m_url.getText()))
         {
            URI uri = new URI(m_url.getText());
            protocol = uri.getScheme();
            host = uri.getHost();
            int prt = uri.getPort();
            if (prt == -1)
            {
               port = protocol.equalsIgnoreCase("https") ? "443" : "80";
            }
            else
               port = String.valueOf(prt);

         }

      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException("Cannot login Invalid url " + m_url.getText());
      }

      m_parent.setParameter("serverName", host);
      m_parent.setParameter("protocol", protocol);
      m_parent.setParameter("port", port);

      backgroundLogin();

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

      PSCESessionManager.getInstance().shutdown();
   }

   /**
    * Simply accepts <CODE>ENTER</CODE> key press as a "Login" button push.
    */
   private class LoginHandler implements ActionListener
   {
      public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
      {
         m_login.doClick();
      }
   }

   /**
    * Updates the admin properties
    *
    */
   private void updateAdminProperties()
   {
      if (m_adminProps == null)
      {
    	 m_adminProps = new PSProperties();
      }
      m_adminProps.setProperty(LoginDialog.LAST_USER, m_userId.getText());
      saveAdminProperties();
   }

   private void backgroundLogin()
   {
      SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
      {

         private volatile String errorMessage = "";

         @Override
         protected Boolean doInBackground() throws Exception
         {
            try
            {
               String host = m_parent.getParameter("serverName");
               String protocol = m_parent.getParameter("protocol");
               String port = m_parent.getParameter("port");

               PSCESessionManager.getInstance().login(protocol, host, port, m_userId.getText(), m_password.getText());
               
               m_statusBar.setStatusText(m_res.getString("connectedStatus") + host);

               // get the jsession from the connection and set it back in the
               // applet
               m_parent.setParameter("pssessionid", PSCESessionManager.getInstance().getLoginInfo().getSessionId());
               m_parent.setParameter("serverName", host);
               m_parent.setParameter("protocol", protocol);
               m_parent.setParameter("port", port);
               m_parent.setParameter("userId", m_userId.getText());
               m_parent.setParameter("password", m_password.getText());

            }
            catch (PSNotAuthenticatedFault e)
            {
               errorMessage = "Failed to authenticate with username and password.";
               return false;
            }
            catch (PSContractViolationFault e)
            {
               errorMessage = "Login details are invalid.";
               return false;
            }
            catch (AxisFault f)
            {
               errorMessage = f.getFaultString();
               if (StringUtils.contains(errorMessage, "Connection refused: connect"))
                  errorMessage = "The service is down or port is incorrect.";
               if (StringUtils.contains(errorMessage, "java.net.UnknownHostException:"))
                  errorMessage = errorMessage.replace("java.net.UnknownHostException:", "Unknown Hostname");

               return false;
            }
            catch (Exception e)
            {
               errorMessage = e.getMessage();
               PSContentExplorerApplication.logout();
               return false;
            }
            updateAdminProperties();
            return true;
         }

         // Can safely update the GUI from this method.
         protected void done()
         {

            System.out.println("done");
            boolean status;
            try
            {
               // Retrieve the return value of doInBackground.
               status = get();
               if (status)
               {
                  System.out.println("good");
                  m_parent.initCESession();

               }
               else
               {
                  m_statusBar.setStatusText(m_res.getString("failedStatus"));

                  JOptionPane.showMessageDialog(PSContentExplorerLoginPanel.this, Util.cropErrorMessage(errorMessage),
                        m_res.getString("error"), JOptionPane.ERROR_MESSAGE);
                  m_login.setEnabled(true);
               }

            }
            catch (InterruptedException e)
            {
               // This is thrown if the thread's interrupted.
            }
            catch (ExecutionException e)
            {
               // This is thrown if we throw an exception
               // from doInBackground.
            }

            PSContentExplorerLoginPanel.this.setCursor(getCursor().getDefaultCursor());
            PSContentExplorerLoginPanel.this.repaint();
            m_statusBar.repaint();
         }

      };

      worker.execute();
   }

   /**
    * Saves the admin.properties file
    */
   private void saveAdminProperties()
   {
      if (m_adminProps != null)
      {
         File file = null;
         try
         {
            file = new File(PSContentExplorerApplication.getConfigDir(), PROPERTIES_FILENAME);
            /*
             * get the admin.properties file where properties were loaded from
             * and save changes to it
             */
            m_adminProps.store(new FileOutputStream(file.getAbsolutePath()), null);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

   }

   public PSContentExplorerApplet getApplet()
   {
      return applet;
   }

   public void setApplet(PSContentExplorerApplet applet)
   {
      this.applet = applet;
      m_login.setEnabled(true);
   }

   //////////////////////////////////////////////////////////////////////////////
   /**
    * the parent frame
    */
   private PSContentExplorerFrame m_parent = null;

   /**
    * the E2 server connection
    */
   // private ServerConnection m_connection = null;

   /**
    * editable text field for port
    */
   private JTextField m_url = new JTextField("");

   /**
    * editable text field for user identification
    */
   private JTextField m_userId = new UTFixedTextField("");

   /**
    * editable password field for user password
    */
   private JTextField m_password = new UTFixedPasswordField("");

   /**
    * the login button
    */
   private JButton m_login = null;

   /**
    * status bar, informing the user about the applets/applications state
    */
   private StatusBar m_statusBar = null;

   /** Admin properties gets initialized in <code>initPanel</code> */
   private PSProperties m_adminProps = null;

   /**
    * Resources
    */
   private ResourceBundle m_res = null;

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
    * Constant for the directory containing admin client configs. Assumed to be
    * relative to the Rx directory.
    */
   public static final String ADMIN_DIR = "rxconfig/Administrator";

   /**
    * Constant for default port number '9992'.
    */
   public static final String DEFAULT_PORT = "9992";

   /**
    * Constant for default SSL port number '9443'.
    */
   public static final String DEFAULT_SSL_PORT = "9443";
}
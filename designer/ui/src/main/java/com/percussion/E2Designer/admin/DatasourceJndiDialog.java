/******************************************************************************
 *
 * [ DatasourceJndiDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.IntegerConstraint;
import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.StringConstraint;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.E2Designer.Util;
import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.design.objectstore.PSJdbcDriverConfig;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSJndiDatasourceImpl;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dialog to add/edit a jndi datasource
 */
public class DatasourceJndiDialog extends PSDialog
{
   private static final long serialVersionUID = 1L;

   /**
    * Construct this dialog.
    * 
    * @param f The parent frame, may be <code>null</code>.
    * @param ds The datasource to edit, may be <code>null</code> if adding a
    * new one.
    * @param datasources The list of currently defined datasources, may not be
    * <code>null</code>.
    * @param drivers The list of configured JDBC drivers, may not be 
    * <code>null</code> or empty. 
    */
   public DatasourceJndiDialog(Frame f, IPSJndiDatasource ds, 
      List<IPSJndiDatasource> datasources, List<PSJdbcDriverConfig> drivers)
   {
      super(f);
      
      if (datasources == null)
         throw new IllegalArgumentException("datasources may not be null");
      
      if (drivers == null)
         throw new IllegalArgumentException("drivers may not be null");
      
      m_datasource = ds;
      m_datasources = datasources;
      m_drivers = drivers;
      
      ms_res = getResources();
      
      m_bInitialized = initDialog();
      if (m_bInitialized)
         initData();
   }


   /**
    * Initializes the data models
    */
   private void initData()
   {
      if (m_datasource != null)
      {
         m_name.setText(m_datasource.getName());
         for (PSJdbcDriverConfig config : m_drivers)
         {
            if (m_datasource.getDriverName().equals(config.getDriverName()))
            {
               m_driver.setSelectedItem(config);
               break;
            }
         }
         
         m_server.setText(m_datasource.getServer());
         m_userid.setText(m_datasource.getUserId());
         m_pw.setText(m_datasource.getPassword());
         m_pwConfirm.setText(m_datasource.getPassword());
         m_minConns.setText(String.valueOf(m_datasource.getMinConnections()));
         m_maxConns.setText(String.valueOf(m_datasource.getMaxConnections()));
         m_idleTimeout.setText(String.valueOf(m_datasource.getIdleTimeout()));
      }
      else
      {
         m_name.setText(JDBC_PREFIX);
         m_minConns.setText("5");
         m_maxConns.setText("100");
         m_idleTimeout.setText("59000");
      }
   }
   
   
   /**
    * Set if this datasource config is being referenced by another component and
    * thus should not allow its name to be changed. Defaults to 
    * <code>false</code> if never set.
    * 
    * @param inUse <code>true</code> if it is in use, <code>false</code> if not.
    */
   public void setIsInUse(boolean inUse)
   {
      m_name.setEditable(!inUse);
   }
   
   /**
    * Initializes all UI components.
    * 
    * @return <code>true</code> if the dialog is initialized, <code>false</code>
    * if not.
    */
   private boolean initDialog()
   {
      setTitle(ms_res.getString("title"));

      for (PSJdbcDriverConfig driver : m_drivers)
      {
         m_driver.addItem(driver);
      }
      
      m_driver.setEditable(false);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      getContentPane().add(panel);
      
      PSPropertyPanel propPanel = new PSPropertyPanel();
      propPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
           
      propPanel.addPropertyRow(ms_res.getString("name"), m_name, 
         ms_res.getString("name.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("driver"), m_driver, 
         ms_res.getString("driver.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("server"), m_server, 
         ms_res.getString("server.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("userid"), m_userid, 
         ms_res.getString("userid.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("pw"), m_pw, 
         ms_res.getString("pw.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("pwConf"), m_pwConfirm, 
         ms_res.getString("pwConf.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("minConns"), m_minConns, 
         ms_res.getString("minConns.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("maxConns"), m_maxConns, 
         ms_res.getString("maxConns.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("idleTimeout"), m_idleTimeout, 
         ms_res.getString("idleTimeout.mn").charAt(0));
      
      panel.add(propPanel, BorderLayout.CENTER);
      
      UTStandardCommandPanel commandPanel = new UTStandardCommandPanel(this, "", 
         SwingConstants.HORIZONTAL)
      {
         private static final long serialVersionUID = 1L;

         public void onOk()
        {
            DatasourceJndiDialog.this.onOk();
          }
      };
      getRootPane().setDefaultButton(commandPanel.getOkButton());
      
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
      cmdPanel.add(commandPanel, BorderLayout.EAST);      
      panel.add(cmdPanel, BorderLayout.SOUTH);
      
      initValidation();
      
      pack();
      setResizable(true);
      center();
      
      return true;
   }

   /**
    * Initializes the validation framework.
    */
   private void initValidation()
   {
      List<Component> comps = new ArrayList<Component>();
      List<ValidationConstraint> constraints = 
         new ArrayList<ValidationConstraint>();
      StringConstraint nonEmpty = new StringConstraint();
      IntegerConstraint numeric = new IntegerConstraint(Integer.MAX_VALUE, 0);
      
      comps.add(m_name);
      constraints.add(nonEmpty);
      comps.add(m_driver);
      constraints.add(nonEmpty);
      comps.add(m_server);
      constraints.add(nonEmpty);
      comps.add(m_minConns);
      constraints.add(numeric);
      comps.add(m_maxConns);
      constraints.add(numeric);
      comps.add(m_idleTimeout);
      constraints.add(numeric);

      Component[] c = comps.toArray(new Component[comps.size()]);
      ValidationConstraint[] v = constraints.toArray(
         new ValidationConstraint[constraints.size()]);
      setValidationFramework(c, v);
   }
   
   /**
    * Handle the ok action
    */
   public void onOk()
   {
      if (!activateValidation())
         return;
      
      // check min and max ok
      int minConns = Integer.parseInt(m_minConns.getText());
      int maxConns = Integer.parseInt(m_maxConns.getText());
      int idleTimeout = Integer.parseInt(m_idleTimeout.getText());

      if (maxConns < minConns)
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
            ms_res.getString("invalidConns.msg")), ms_res.getString(
               "invalidConns.title"), JOptionPane.ERROR_MESSAGE);
         return;
      }
      
      // check ds name
      String dsName = m_name.getText().trim();
      if (dsName.equalsIgnoreCase(JDBC_PREFIX))
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
            ms_res.getString("invalidName.msg")), ms_res.getString(
               "invalidName.title"), JOptionPane.ERROR_MESSAGE);
         return;
      }
      
      for (IPSJndiDatasource datasource : m_datasources)
      {
         if (datasource.getName().equalsIgnoreCase(dsName) && 
            datasource != m_datasource)
         {
            JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
               ms_res.getString("duplicateName.msg")), ms_res.getString(
                  "duplicateName.title"), JOptionPane.ERROR_MESSAGE);
            return;
         }
      }
      
      // check password
      String pw = new String(m_pw.getPassword()).trim();
      String pwConfirm = new String(m_pwConfirm.getPassword()).trim();
      if (!pw.equals(pwConfirm))
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
            ms_res.getString("invalidPw.msg")), ms_res.getString(
               "invalidPw.title"), JOptionPane.ERROR_MESSAGE);
         return;
      }      
      
      // get driver
      PSJdbcDriverConfig driver = 
         (PSJdbcDriverConfig) m_driver.getSelectedItem();

      String server = m_server.getText().trim();
      String uid = m_userid.getText().trim();

      
      if (m_datasource == null)
      {
         m_datasource = new PSJndiDatasourceImpl(dsName,
            driver.getDriverName(), driver.getClassName(), 
             server, uid, pw);
      }
      else
      {
         m_datasource.setName(m_name.getText());
         m_datasource.setDriverName(driver.getDriverName());
         m_datasource.setDriverClassName(driver.getClassName());
         m_datasource.setServer(server);
         m_datasource.setUserId(uid);
         m_datasource.setPassword(pw);
      }
      
      m_datasource.setMinConnections(minConns);
      m_datasource.setMaxConnections(maxConns);
      m_datasource.setIdleTimeout(idleTimeout);
      
      m_bModified = true;
      setVisible(false);
      dispose();
   }

   /**
    * Accessor function to check whether role is modified or not.
    *
    * @return The modified flag.
    **/
   public boolean isModified()
   {
      return m_bModified;
   }
   
   /**
    * Accessor for the added/edited datasource.
    * 
    * @return The datasource, may be <code>null</code> if {@link #onOk()} has
    * not been called..
    */
   public IPSJndiDatasource getDatasource()
   {
      return m_datasource;
   }      

   /**
    * The datasource to add/edit, may be <code>null</code> if adding and 
    * {@link #onOk()} has not been called..
    */
   private IPSJndiDatasource m_datasource;
   
   /**
    * List of currently defined datasource, never <code>null</code> or modified
    * after ctor.
    */
   List<IPSJndiDatasource> m_datasources;
   
   /**
    * List of drivers to select from, never <code>null</code> or modified after 
    * ctor.
    */
   private List<PSJdbcDriverConfig> m_drivers;
   
   /**
    * The field to enter the datasource name, never <code>null</code> after
    * construction.
    */
   private JTextField m_name = new JTextField();
   
   /**
    * The combo box used  to select the dirver, never <code>null</code> after
    * construction.
    */   
   private JComboBox<PSJdbcDriverConfig> m_driver = new JComboBox<PSJdbcDriverConfig>();
   
   /**
    * The field to enter the server name, never <code>null</code> after
    * construction.
    */
   private JTextField m_server = new JTextField();
   
   /**
    * The field to enter the user name, never <code>null</code> after
    * construction.
    */
   private JTextField m_userid = new JTextField();
   
   /**
    * The field to enter the password, never <code>null</code> after
    * construction.
    */
   private JPasswordField m_pw = new JPasswordField();
   
   /**
    * The field to confirm the password, never <code>null</code> after
    * construction.
    */
   private JPasswordField m_pwConfirm = new JPasswordField();
   
   /**
    * The field to enter the min connections, never <code>null</code> after
    * construction.
    */
   private JTextField m_minConns = new JTextField();
   
   /**
    * The field to enter the max connections, never <code>null</code> after
    * construction.
    */
   private JTextField m_maxConns = new JTextField();
   
   /**
    * The field to enter the idle timeout, never <code>null</code> after
    * construction.
    */
   private JTextField m_idleTimeout = new JTextField();

   /**
    * The flag to indicate whether role is modified or not, initialized to
    * <code>false</code> and set to <code>true</code> when OK button is clicked.
    **/
   private boolean m_bModified = false;
   
   /** Dialog resource strings, initialized in constructor. **/
   private static ResourceBundle ms_res = null;

   /**
    * Constant for default jdbc prefix.
    */
   private static final String JDBC_PREFIX = "jdbc/";
}


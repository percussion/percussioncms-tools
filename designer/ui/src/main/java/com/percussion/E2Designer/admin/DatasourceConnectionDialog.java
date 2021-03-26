/******************************************************************************
 *
 * [ DatasourceConnectionDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.StringConstraint;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.E2Designer.Util;
import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dialog for editing a datasource connection
 */
public class DatasourceConnectionDialog extends PSDialog
{
   private static final long serialVersionUID = 1L;

   /**
    * Construct this dialog
    * 
    * @param f The parent frame, may be <code>null</code>.
    * @param config The config to edit, may be <code>null</code> if creating
    * a new one.
    * @param configs The list of currently defined configs, may not be 
    * <code>null</code>.
    * @param jndiDatasources List of Jndi datasources to reference, may not
    * be <code>null</code>. 
    */
   public DatasourceConnectionDialog(Frame f, IPSDatasourceConfig config,
                                     List<IPSDatasourceConfig> configs, List<IPSJndiDatasource> jndiDatasources)
   {
      super(f);
      
      if (configs == null)
         throw new IllegalArgumentException("configs may not be null");
      if (jndiDatasources == null)
         throw new IllegalArgumentException("jndiDatasources may not be null");
      
      m_config = new PSDatasourceConfig(config);
      m_configs = configs;
      m_datasources = jndiDatasources;
      
      ms_res = getResources();
      
      m_bInitialized = initDialog();
      if (m_bInitialized && m_config != null)
         initData();      
   }

   /**
    * Initializes the dialog data, assumes a connection has been supplied.
    */
   private void initData()
   {
      m_name.setText(m_config.getName());
      
      for (IPSJndiDatasource ds : m_datasources)
      {
         if (ds.getName().equals(m_config.getDataSource()))
         {
            m_datasource.setSelectedItem(ds);
            break;
         }
      }
      
      m_origin.setText(m_config.getOrigin());
      m_database.setText(m_config.getDatabase());
   }

   /**
    * Init all UI controls.
    * 
    * @return <code>true</code> if the dialog is initialized, <code>false</code>
    * if not.
    */
   private boolean initDialog()
   {
      setTitle(ms_res.getString("title"));
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      getContentPane().add(panel);
      
      m_name = new JTextField();
      m_datasource = new JComboBox<IPSJndiDatasource>();
      m_origin = new JTextField();
      m_database = new JTextField();
      
      for (IPSJndiDatasource datasource : m_datasources)
      {
         m_datasource.addItem(datasource);
      }
      
      PSPropertyPanel propPanel = new PSPropertyPanel();
      propPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
           
      propPanel.addPropertyRow(ms_res.getString("name"), m_name, 
         ms_res.getString("name.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("datasource"), m_datasource, 
         ms_res.getString("datasource.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("database"), m_database, 
         ms_res.getString("database.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("origin"), m_origin, 
         ms_res.getString("origin.mn").charAt(0));
      
      panel.add(propPanel, BorderLayout.CENTER);
      
      UTStandardCommandPanel commandPanel = new UTStandardCommandPanel(this, "", 
         SwingConstants.HORIZONTAL)
      {
         private static final long serialVersionUID = 1L;

         public void onOk()
        {
            DatasourceConnectionDialog.this.onOk();
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
      
      comps.add(m_name);
      constraints.add(nonEmpty);
      comps.add(m_datasource);
      constraints.add(nonEmpty);

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
      
      String name = m_name.getText().trim();
      for (IPSDatasourceConfig config : m_configs)
      {
         if (config.getName().equalsIgnoreCase(name) && config != m_config)
         {
            JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
               ms_res.getString("duplicateName.msg")), ms_res.getString(
                  "duplicateName.title"), JOptionPane.ERROR_MESSAGE);
            return;
         }
      }
      
      String datasource = 
         ((IPSJndiDatasource) m_datasource.getSelectedItem()).getName().trim();
      String origin = m_origin.getText().trim();
      String database = m_database.getText().trim();
      
      if (m_config == null)
      {
         m_config = new PSDatasourceConfig(name, datasource, origin, database);
      }
      else
      {
         m_config.setName(name);
         m_config.setDataSource(datasource);
         m_config.setOrigin(origin);
         m_config.setDatabase(database);
      }
      
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
    * Get the new/edited config.
    * 
    * @return The config, may be <code>null</code> if creating a new config and
    * {@link #onOk()} has not been successfully executed.
    */
   public PSDatasourceConfig getConfig()
   {
      return m_config;
   }

   
   /**
    * The connection to edit/create, <code>null</code> only if creating a new 
    * one
    */
   private PSDatasourceConfig m_config;
   
   /**
    * List of currently defined configs, never <code>null</code> or modified
    * after construction.
    */
   private List<IPSDatasourceConfig> m_configs;
   
   /**
    * JNDI datasources supplied during ctor, never <code>null</code> after that.
    */
   private List<IPSJndiDatasource> m_datasources;
   
   /**
    * Text field to specify the name, never <code>null</code> after ctor.
    */
   private JTextField m_name;

   /**
    * Combo to specify the JNDI datasource, never <code>null</code> after ctor.
    */
   private JComboBox<IPSJndiDatasource> m_datasource;
   
   /**
    * Text field to specify the database, never <code>null</code> after ctor.
    */
   private JTextField m_database;

   
   /**
    * Text field to specify the origin, never <code>null</code> after ctor.
    */
   private JTextField m_origin;   
   
   /**
    * The flag to indicate whether role is modified or not, initialized to
    * <code>false</code> and set to <code>true</code> when OK button is clicked.
    **/
   private boolean m_bModified = false;
   
   /** Dialog resource strings, initialized in constructor. **/
   private static ResourceBundle ms_res = null;
}


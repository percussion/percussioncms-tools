/******************************************************************************
 *
 * [ DatasourceDriverConfigDialog.java ]
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
import com.percussion.design.objectstore.PSJdbcDriverConfig;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Dialog for editing a JDBC driver config
 */
public class DatasourceDriverConfigDialog extends PSDialog
{
   private static final long serialVersionUID = 1L;

   /**
    * Construct this dialog
    * 
    * @param f The parent frame, may be <code>null</code>.
    * @param config The config to edit, may be <code>null</code> if creating
    * a new one.
    * @param configs The current list of configs, may not be <code>null</code>.
    * @param dialects List of dialects, may not be <code>null</code>, must 
    * include the driver specified by the supplied config.
    * @param typeMappings List of type mappings, may not be <code>null</code>, 
    * must include the type mapping specified by the supplied config.
    */
   public DatasourceDriverConfigDialog(Frame f, PSJdbcDriverConfig config, 
      List<PSJdbcDriverConfig> configs, PSHibernateDialectConfig dialects, 
      Set<String> typeMappings)
   {
      super(f);
      
      if (configs == null)
         throw new IllegalArgumentException("configs may not be null");
      if (dialects == null)
         throw new IllegalArgumentException("dialects may not be null");
      if (typeMappings == null)
         throw new IllegalArgumentException("typeMappings may not be null");
      
      m_config = config;
      m_configs = configs;
      m_dialects = dialects;
      m_typeMappings = typeMappings;
      
      ms_res = getResources();
      
      m_bInitialized = initDialog();
      if (m_bInitialized && m_config != null)
         initData();
   }
   
   /**
    * Set if this driver config is being referenced by another component and
    * thus should not allow its name to be changed. Defaults to 
    * <code>false</code> if never set.
    * 
    * @param inUse <code>true</code> if it is in use, <code>false</code> if not.
    */
   public void setIsInUse(boolean inUse)
   {
      m_driver.setEditable(!inUse);
   }

   /**
    * Initializes the dialog data, assumes a config has been supplied.
    */
   private void initData()
   {
      m_driver.setText(m_config.getDriverName());
      m_class.setText(m_config.getClassName());
      m_typeMapping.setSelectedItem(m_config.getContainerTypeMapping());
      m_dialect.setSelectedItem(m_dialects.getDialectClassName(
         m_config.getDriverName()));
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
      
      m_driver = new JTextField();
      m_class = new JTextField();
      m_typeMapping = new JComboBox();
      m_typeMapping.setEditable(true);
      m_dialect = new JComboBox();
      m_dialect.setEditable(true);
      
      List<String> values = new ArrayList<String>(m_typeMappings);
      Collections.sort(values);
      for (String typeMapping : values)
      {
         m_typeMapping.addItem(typeMapping);
      }
      
      Set<String> dialectSet = 
         new HashSet<String>(m_dialects.getDialects().values());
      values.clear();
      values.addAll(dialectSet);
      Collections.sort(values);
      for (String dialect : values)
      {
         m_dialect.addItem(dialect);
      }
      
      PSPropertyPanel propPanel = new PSPropertyPanel();
      propPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
           
      propPanel.addPropertyRow(ms_res.getString("driver"), m_driver, 
         ms_res.getString("driver.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("class"), m_class, 
         ms_res.getString("class.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("typeMapping"), m_typeMapping, 
         ms_res.getString("typeMapping.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("dialect"), m_dialect, 
         ms_res.getString("dialect.mn").charAt(0));
      
      panel.add(propPanel, BorderLayout.CENTER);
      
      UTStandardCommandPanel commandPanel = new UTStandardCommandPanel(this, "", 
         SwingConstants.HORIZONTAL)
      {
         private static final long serialVersionUID = 1L;

         public void onOk()
        {
            DatasourceDriverConfigDialog.this.onOk();
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
      
      comps.add(m_driver);
      constraints.add(nonEmpty);
      comps.add(m_class);
      constraints.add(nonEmpty);
      comps.add(m_typeMapping);
      constraints.add(nonEmpty);
      comps.add(m_dialect);
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
      
      String driver = m_driver.getText().trim();
      for (PSJdbcDriverConfig driverConfig : m_configs)
      {
         if (driverConfig.getDriverName().equalsIgnoreCase(driver) && 
            driverConfig != m_config)
         {
            JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
               ms_res.getString("duplicateName.msg")), ms_res.getString(
                  "duplicateName.title"), JOptionPane.ERROR_MESSAGE);
            return;
         }
      }
      
      String className = m_class.getText().trim();
      String typeMapping = ((String) m_typeMapping.getSelectedItem()).trim(); 
      
      if (m_config == null)
      {
         m_config = new PSJdbcDriverConfig(driver, className, typeMapping);
      }
      else
      {
         m_config.setDriverName(driver);
         m_config.setClassName(className);
         m_config.setContainerTypeMapping(typeMapping);
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
   public PSJdbcDriverConfig getConfig()
   {
      return m_config;
   }
   
   /**
    * Get the currently selected dialect.
    * 
    * @return The dialect, never <code>null</code> or empty.
    */
   public String getDialect()
   {
      return (String)m_dialect.getSelectedItem();
   }
   
   /**
    * The config to edit/create, <code>null</code> only if creating a new config
    */
   private PSJdbcDriverConfig m_config;
   
   /**
    * List of current driver configs, never <code>null</code> after ctor.
    */
   private List<PSJdbcDriverConfig> m_configs;
   
   /**
    * Dialects supplied during ctor, never <code>null</code> after that.
    */
   private PSHibernateDialectConfig m_dialects;
   
   /**
    * Type mappings supplied during ctor, never <code>null</code> after that.
    */
   private Set<String> m_typeMappings;
   
   /**
    * Text field to specify the driver, never <code>null</code> after ctor.
    */
   private JTextField m_driver;

   /**
    * Text field to specify the class, never <code>null</code> after ctor.
    */
   private JTextField m_class;
   
   /**
    * Combo to specify the type mapping, never <code>null</code> after ctor.
    */
   private JComboBox m_typeMapping;
   
   /**
    * Combo to specify the dialect, never <code>null</code> after ctor.
    */
   private JComboBox m_dialect;

   /**
    * The flag to indicate whether role is modified or not, initialized to
    * <code>false</code> and set to <code>true</code> when OK button is clicked.
    **/
   private boolean m_bModified = false;
   
   /** Dialog resource strings, initialized in constructor. **/
   private static ResourceBundle ms_res = null;
}


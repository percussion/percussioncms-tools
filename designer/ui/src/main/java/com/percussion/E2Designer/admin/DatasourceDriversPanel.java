/******************************************************************************
 *
 * [ DatasourceDriversPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSJdbcDriverConfig;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.PSDatasourceResolver;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Panel to enable adding, editing and deleting JDBC driver configurations. 
 */
public class DatasourceDriversPanel extends DatasourceBasePanel
{
   private static final long serialVersionUID = 1L;
   
   /**
    * Construct the panel.
    * 
    * @param parent The parent frame, may be <code>null</code>.
    * @param config The server config, used to get and set driver configs, may
    * not be <code>null</code>.
    * @param dialects The hibernate dialect mappings, may not be 
    * <code>null</code>.
    * @param datasources The JNDI datasources, may not be <code>null</code>.
    * @param resolver The datasource resolver, may not be <code>null</code>.
    */
   public DatasourceDriversPanel(Frame parent, ServerConfiguration config, 
      PSHibernateDialectConfig dialects, List<IPSJndiDatasource> datasources, 
      PSDatasourceResolver resolver)
   {
      super(parent, config, dialects, datasources, resolver);
   }


   /* (non-Javadoc)
    * @see com.percussion.E2Designer.admin.ITabDataHelper#validateTabData()
    */
   public boolean validateTabData()
   {
      // ensure there is at least one driver configured
      if (m_driverConfigs.isEmpty())
      {
         JOptionPane.showMessageDialog( AppletMainDialog.getMainframe(),
            getString("drivers.noDriversDefined"),
            getString( "error" ),
            JOptionPane.ERROR_MESSAGE );

         return false;
      }
      else
         return true;
   }
   
   // see base class
   @Override
   protected String[] getColumnNames()
   {
      if (ms_columnNames == null)
      {
         ms_columnNames = new String[]
         {
            getString("drivers.driver"), 
            getString("drivers.class"),
            getString("drivers.hibernateDialect")};
      }
      return ms_columnNames;
   }
   
   // see base class
   @Override
   protected String[][] getTableData()
   {
      String[][] data = 
         new String[m_driverConfigs.size()][getColumnNames().length];
      for (int i = 0; i < data.length; i++)
      {
         PSJdbcDriverConfig config = m_driverConfigs.get(i);
         data[i] = configToRowData(config);
      }
      
      return data;
   }   

   /**
    * Convert a config to matching row data.
    * 
    * @param config The config, assumed not <code>null</code>.
    * 
    * @return The row data, never <code>null</code>.
    */
   private String[] configToRowData(PSJdbcDriverConfig config)
   {
      String[] data = new String[ms_columnNames.length];
      data[DRIVER_COL] = config.getDriverName();
      data[CLASS_COL] = config.getClassName();
      data[DIALECT_COL] = m_hibernateDialects.getDialectClassName(
         config.getDriverName());
      
      return data;
   }
   
   // see base class
   @Override
   protected String[] doEdit(int selectedRow)
   {
      PSJdbcDriverConfig config = m_driverConfigs.get(selectedRow);
      
      String[] result = null;
      
      // build set of type mappings
      Set<String> typeMappings = getTypeMappings();
      
      DatasourceDriverConfigDialog dlg = new DatasourceDriverConfigDialog(
         m_parent, config, m_driverConfigs, m_hibernateDialects, typeMappings);
      
      // see if being used
      for (IPSJndiDatasource ds : m_datasources)
      {
         if (ds.getDriverName().equals(config.getDriverName()))
         {
            dlg.setIsInUse(true);
            break;
         }
      }
      
      dlg.setVisible(true);
      if (dlg.isModified())
      {
         m_hibernateDialects.setDialect(config.getDriverName(), 
            dlg.getDialect());
         config = dlg.getConfig();
         result = configToRowData(config);
         
         // update any jndi datasources using this config
         for (IPSJndiDatasource datasource : m_datasources)
         {
            if (datasource.getDriverName().equals(config.getDriverName()))
            {
               datasource.setDriverClassName(config.getClassName());
            }
         }
      }
      
      return result;
   }


   // see base class
   @Override
   protected String[] doAdd()
   {
      String[] result = null;
      
      // build set of type mappings
      Set<String> typeMappings = getTypeMappings();
      
      DatasourceDriverConfigDialog dlg = new DatasourceDriverConfigDialog(
         m_parent, null, m_driverConfigs, m_hibernateDialects, typeMappings);
      dlg.setVisible(true);
      if (dlg.isModified())
      {
         PSJdbcDriverConfig config = dlg.getConfig(); 
         m_driverConfigs.add(config);
         m_hibernateDialects.setDialect(config.getDriverName(), 
            dlg.getDialect());
         result = configToRowData(dlg.getConfig());
      }
      
      return result;
   }

   // see base class
   @Override
   protected boolean canDelete(int onRow)
   {
      // can't delete last one, can't delete if any jndi datasources
      // reference the row's driver
      boolean canDelete =  m_driverConfigs.size() > 1;
      
      if (canDelete)
      {
         // check for usage
         for (IPSJndiDatasource ds : m_datasources)
         {
            if (ds.getDriverName().equals(
               m_driverConfigs.get(onRow).getDriverName()))
            {
               canDelete = false;
               break;
            }
         }
      }
      
      return canDelete;
   }

   /**
    * Get the set of type mappings to supply as choices for editing.
    * 
    * @return The mappings, never <code>null</code>.
    */
   private Set<String> getTypeMappings()
   {
      Set<String> typeMappings = new HashSet<String>();
      for (PSJdbcDriverConfig config : m_driverConfigs)
      {
         typeMappings.add(config.getContainerTypeMapping());
      }
      return typeMappings;
   }


   // see base class
   @Override
   protected boolean doDelete(int selectedRow)
   {
      int result = JOptionPane.showConfirmDialog(m_parent,
         getString("drivers.delete.msg"), getString("drivers.delete.title"),
         JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION)
      {
         m_driverConfigs.remove(selectedRow);
         return true;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Array of column names, never <code>null</code> or empty after first call
    * to {@link #getColumnNames()}
    */
   private static String[] ms_columnNames = null;
   
   /**
    * Constant for the index into the row data for the driver column
    */
   private static int DRIVER_COL = 0;
   
   /**
    * Constant for the index into the row data for the class column
    */
   private static int CLASS_COL = 1;
   
   /**
    * Constant for the index into the row data for the dialect column
    */
   private static int DIALECT_COL = 2;
}


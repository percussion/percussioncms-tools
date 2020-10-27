/******************************************************************************
 *
 * [ DatasourceJndiPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceResolver;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel to enable adding, editing and deleting JNDI datasource configurations. 
 */
public class DatasourceJndiPanel extends DatasourceBasePanel
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
   public DatasourceJndiPanel(Frame parent, ServerConfiguration config, 
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
      if (m_datasources.isEmpty())
      {
         JOptionPane.showMessageDialog( AppletMainDialog.getMainframe(),
            getString("jndi.noDatasourceDefined"),
            getString( "error" ),
            JOptionPane.ERROR_MESSAGE );

         return false;
      }
      else
         return true;
   }

   @Override
   protected String[] doEdit(int selectedRow)
   {
      String[] result = null;
      
      IPSJndiDatasource ds = m_datasources.get(selectedRow); 
      DatasourceJndiDialog dlg = new DatasourceJndiDialog(m_parent, ds, 
         m_datasources, m_driverConfigs);
      
      // see if being used
      for (IPSDatasourceConfig config : m_resolver.getDatasourceConfigurations())
      {
         if (config.getDataSource().equals(ds.getName()))
         {
            dlg.setIsInUse(true);
            break;
         }
      }      
      
      
      dlg.setVisible(true);
      if (dlg.isModified())
      {
         ds = dlg.getDatasource();
         result = datasourceToRowData(ds);
      }
      
      return result;
   }

   @Override
   protected String[] doAdd()
   {
      String[] result = null;
      
      DatasourceJndiDialog dlg = new DatasourceJndiDialog(m_parent, null, 
         m_datasources, m_driverConfigs);
      dlg.setVisible(true);
      if (dlg.isModified())
      {
         IPSJndiDatasource ds = dlg.getDatasource();
         m_datasources.add(ds);
         result = datasourceToRowData(ds);
      }
      
      return result;
   }

   @Override
   protected boolean doDelete(int selectedRow)
   {
     int result = JOptionPane.showConfirmDialog(m_parent,
         getString("jndi.delete.msg"), getString("jndi.delete.title"),
         JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION)
      {
         m_datasources.remove(selectedRow);
         return true;
      }
      else
      {
         return false;
      }
   }


   // see base class
   @Override
   protected String[] getColumnNames()
   {
      if (ms_columnNames == null)
      {
         ms_columnNames = new String[]
         {
            getString("jndi.name"), 
            getString("jndi.driver"),
            getString("jndi.server")
         };
      }
      return ms_columnNames;
   }
   
   // see base class
   @Override
   protected String[][] getTableData()
   {
      String[][] data = 
         new String[m_datasources.size()][getColumnNames().length];
      for (int i = 0; i < data.length; i++)
      {
         IPSJndiDatasource ds = m_datasources.get(i);
         data[i] = datasourceToRowData(ds);
      }
      
      return data;
   }   
   
   // see base class
   @Override
   protected boolean canDelete(int onRow)
   {
      // can't delete last one, can't delete if any connections reference
      // the datasource.
      boolean canDelete = m_datasources.size() > 1;
      if (canDelete)
      {
         for (IPSDatasourceConfig config : 
            m_resolver.getDatasourceConfigurations())
         {
            if (config.getDataSource().equals(
               m_datasources.get(onRow).getName()))
            {
               canDelete = false;
               break;
            }
         }
      }
      
      return canDelete;
   }

   /**
    * Convert the supplied datasource to matching row data.
    * 
    * @param ds The datasource, assumed not <code>null</code>.
    * 
    * @return The row data, never <code>null</code>.
    */
   private String[] datasourceToRowData(IPSJndiDatasource ds)
   {
      String[] data = new String[ms_columnNames.length];
      data[NAME_COL] = ds.getName();
      data[DRIVER_COL] = ds.getDriverName();
      data[SERVER_COL] = ds.getServer();
      
      return data;
   }

   /**
    * Array of column names, never <code>null</code> or empty after first call
    * to {@link #getColumnNames()}
    */
   private static String[] ms_columnNames = null;
   
   /**
    * Constant for the name column index.
    */
   private static final int NAME_COL = 0;

   /**
    * Constant for the driver column index.
    */
   private static final int DRIVER_COL = 1;

   /**
    * Constant for the server column index.
    */
   private static final int SERVER_COL = 2;
}


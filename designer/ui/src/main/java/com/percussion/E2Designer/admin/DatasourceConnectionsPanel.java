/******************************************************************************
 *
 * [ DatasourceConnectionsPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTRadioButtonCellEditor;
import com.percussion.E2Designer.UTRadioButtonCellRenderer;
import com.percussion.E2Designer.Util;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import com.percussion.utils.jdbc.PSJdbcUtils;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Panel to enable adding, editing and deleting datasource connection
 * configurations.
 */
public class DatasourceConnectionsPanel extends DatasourceBasePanel
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
   public DatasourceConnectionsPanel(Frame parent, ServerConfiguration config, 
      PSHibernateDialectConfig dialects, List<IPSJndiDatasource> datasources, 
      PSDatasourceResolver resolver)
   {
      super(parent, config, dialects, datasources, resolver);
   }


   @Override
   protected boolean canDelete(int onRow)
   {
      List<IPSDatasourceConfig> configs =
         m_resolver.getDatasourceConfigurations();
      boolean isRepository = m_resolver.getRepositoryDatasource().equals(
         configs.get(onRow).getName()); 
      
      return !isRepository && configs.size() > 1;   
   }


   /* (non-Javadoc)
    * @see com.percussion.E2Designer.admin.ITabDataHelper#validateTabData()
    */
   public boolean validateTabData()
   {
     // ensure there is at least one driver configured
      if (m_resolver.getDatasourceConfigurations().isEmpty())
      {
         JOptionPane.showMessageDialog( AppletMainDialog.getMainframe(),
            getString("conn.noDatasourceDefined"),
            getString( "error" ),
            JOptionPane.ERROR_MESSAGE );

         return false;
      }
      else
         return true;
   }



   @Override
   protected Object[] doEdit(int selectedRow)
   {
      Object[] result = null;
      List<IPSDatasourceConfig> configs =
         m_resolver.getDatasourceConfigurations();
      IPSDatasourceConfig config = configs.get(selectedRow);
      boolean isDefault = config.getName().equals(
         m_resolver.getRepositoryDatasource());
      
      DatasourceConnectionDialog dlg = new DatasourceConnectionDialog(m_parent, 
         config, configs, m_datasources);
      dlg.setVisible(true);
      if (dlg.isModified())
      {
         PSDatasourceConfig ds = dlg.getConfig();
         // default name may change
         if (isDefault)
            m_resolver.setRepositoryDatasource(ds.getName());
         result = datasourceToRowData(ds);
      }
      
      return result;
   }



   @Override
   protected Object[] doAdd()
   {
      Object[] result = null;
      
      DatasourceConnectionDialog dlg = new DatasourceConnectionDialog(m_parent, 
         null, m_resolver.getDatasourceConfigurations(), m_datasources);
      dlg.setVisible(true);
      if (dlg.isModified())
      {
         PSDatasourceConfig ds = dlg.getConfig();
         m_resolver.getDatasourceConfigurations().add(ds);
         result = datasourceToRowData(ds);
      }
      
      return result;
   }



   @Override
   protected boolean doDelete(int selectedRow)
   {
     int result = JOptionPane.showConfirmDialog(m_parent,
         getString("conn.delete.msg"), getString("conn.delete.title"),
         JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION)
      {
         m_resolver.getDatasourceConfigurations().remove(selectedRow);
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
            getString("conn.name"), 
            getString("conn.jndi"),
            getString("conn.database"),
            getString("conn.schema"),
            getString("conn.repository")
         };
      }
      return ms_columnNames;
   }
   
   // see base class
   @Override
   protected Object[][] getTableData()
   {
      List<IPSDatasourceConfig> configs =
         m_resolver.getDatasourceConfigurations();
      
      Object[][] data = 
         new Object[configs.size()][getColumnNames().length];
      for (int i = 0; i < data.length; i++)
      {
         IPSDatasourceConfig config = configs.get(i);
         data[i] = datasourceToRowData(config);
      }
      
      return data;
   }
   
   // see base class
   @Override
   protected TableCellRenderer getTableCellRenderer(int colIndex)
   {
      if (colIndex == REPOSITORY_COL)
      {
         return new UTRadioButtonCellRenderer();
      }
      else
         return null;
   }

   // see base class
   @Override
   protected TableCellEditor getTableCellEditor(int colIndex)
   {
      if (colIndex == REPOSITORY_COL)
      {
         final UTRadioButtonCellEditor editor = new UTRadioButtonCellEditor();
         
         // stop editing on change
         editor.addRadioButtonListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            ActionEvent e)
            {
               editor.stopCellEditing();
            }});
         
         // handle selection change and simulate group behavior
         editor.addCellEditorListener(new CellEditorListener() {

            public void editingStopped(ChangeEvent e)
            {
               if (e.getSource() instanceof UTRadioButtonCellEditor)
               {
                  boolean selected = (Boolean) editor.getCellEditorValue();
                  int row = getSelectedRow();

                  if (isMySqlDataSource(row))
                  {
                     editor.cancelCellEditing();
                     refreshTable();
                     
                     JOptionPane.showMessageDialog(m_parent, 
                           Util.cropErrorMessage(getString(
                                 "conn.mySqlNotSupported.msg")),
                           getString("conn.mySqlNotSupported.title"), 
                           JOptionPane.INFORMATION_MESSAGE);
                                          
                     return;
                  }
                  
                  String selectedName = m_resolver
                     .getDatasourceConfigurations().get(row).getName();
                  boolean wasSelected = selectedName.equals(
                        m_resolver.getRepositoryDatasource());
                  /*
                   * this keeps from having two radio buttons selected while we 
                   * show a confirmation 
                   */
                  refreshTable();
                  
                  if (!wasSelected && selected)
                  {
                     // changing it 
                     int result = JOptionPane.showConfirmDialog(m_parent, 
                        Util.cropErrorMessage(getString(
                           "conn.changeRepository.msg")), 
                           getString("conn.changeRepository.title"), 
                           JOptionPane.OK_CANCEL_OPTION);
                     
                     if (result == JOptionPane.CANCEL_OPTION)
                     {
                        editor.cancelCellEditing();
                     }
                     else 
                     {
                        m_resolver.setRepositoryDatasource(selectedName);
                     }
                  }
                  refreshTable();                  
               }
            }

            public void editingCanceled(ChangeEvent e)
            {
               // noop
               if (e == null);
            }});
         return editor;
      }
      else
      {
         return null;
      }
   }

   /**
    * Determines if the data source of the given row is MySQL or not.
    * @param row the row of the data source in question.
    * @return <code>true</code> if the data source of the specified row is
    *    MySQL database.
    */
   private boolean isMySqlDataSource(int row)
   {
      String jndiName = m_resolver.getDatasourceConfigurations().get(
            row).getDataSource();

      for (IPSJndiDatasource ds : m_datasources)
      {
         if (ds.getName().equalsIgnoreCase(jndiName))
         {
            return ds.getDriverName().equalsIgnoreCase(
                  PSJdbcUtils.MYSQL_DRIVER);
         }
      }
      return false;
   }
   
   /**
    * Convert the supplied datasource to matching row data.
    * 
    * @param config The datasource, assumed not <code>null</code>.
    * 
    * @return The row data, never <code>null</code>.
    */
   private Object[] datasourceToRowData(IPSDatasourceConfig config)
   {
      Object[] data = new Object[ms_columnNames.length];
      data[NAME_COL] = config.getName();
      data[JNDI_COL] = config.getDataSource();
      data[DB_COL] = config.getDatabase();
      data[SCHEMA_COL] = config.getOrigin();
      data[REPOSITORY_COL] = m_resolver.getRepositoryDatasource().equals(
         config.getName()); 
      
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
    * Constant for the jndi column index.
    */
   private static final int JNDI_COL = 1;

   /**
    * Constant for the db column index.
    */
   private static final int DB_COL = 2;
   
   /**
    * Constant for the schema column index.
    */
   private static final int SCHEMA_COL = 3;

   /**
    * Constant for the repository column index.
    */
   private static final int REPOSITORY_COL = 4;
}


/******************************************************************************
 *
 * [ CatalogerConfigurationPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.services.security.data.PSCatalogerConfig;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

/**
 * A panel to add/edit/delete a type of cataloger config.
 */
public class CatalogerConfigurationPanel extends TableEditorBasePanel
{
   private static final long serialVersionUID = 1L;

   /**
    * Construct the dialog
    * 
    * @param parent The parent frame, may not be <code>null</code>.
    * @param configs The list of configurations to modify, may not be 
    * <code>null</code>.  The passed in list is directly modified by this
    * dialog by all user actions.
    * @param type The type of configs contained in the supplied list.
    */
   public CatalogerConfigurationPanel(Frame parent, 
      List<PSCatalogerConfig> configs, PSCatalogerConfig.ConfigTypes type)
   {
      super();
      
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      if (configs == null)
         throw new IllegalArgumentException("configs may not be null");
      
      m_parent = parent;
      m_type = type;
      m_catalogerConfigs = configs;
      
      initPanel();
      Border border = BorderFactory.createCompoundBorder(
         BorderFactory.createTitledBorder(getString("title." + type)), 
         BorderFactory.createEmptyBorder(5, 5, 5, 5));
      setBorder(border);
   }

   @Override
   protected Object[] doEdit(int selectedRow)
   {
      PSCatalogerConfig config = m_catalogerConfigs.get(selectedRow);
      
      String[] result = null;
      
      CatalogerConfigDialog dlg = new CatalogerConfigDialog(m_parent, config, 
         m_type);
      dlg.setVisible(true);
      if (dlg.isModified())
      {
         config = dlg.getConfig();
         m_catalogerConfigs.set(selectedRow, config);
         result = configToRowData(config);
      }

      return result;
   }

   @Override
   protected Object[] doAdd()
   {
      String[] result = null;
      
      CatalogerConfigDialog dlg = new CatalogerConfigDialog(m_parent, null, 
         m_type);
      dlg.setVisible(true);
      if (dlg.isModified())
      {
         PSCatalogerConfig config = dlg.getConfig();
         m_catalogerConfigs.add(config);
         result = configToRowData(config);
      }
      
      return result;
   }
   
   /**
    * Convert the config to the table model row data.
    * 
    * @param config The config to convert, assumed not <code>null</code>.
    * 
    * @return The row data, never <code>null</code>.
    */
   private String[] configToRowData(PSCatalogerConfig config)
   {
      String[] data = new String[getColumnNames().length];
      data[NAME_COL] = config.getName();
      data[DESC_COL] = config.getDescription();
      
      return data;
   }
   
   @Override
   protected boolean doDelete(int selectedRow)
   {
      int result = JOptionPane.showConfirmDialog(m_parent,
         getString("delete.msg"), 
         getString("delete.title"),
         JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION)
      {
         m_catalogerConfigs.remove(selectedRow);
         return true;
      }
      else
      {
         return false;
      }
   }

   @Override
   protected String getKeyPrefix()
   {
      return "catalogerConfigs.";
   }

   @Override
   protected String[] getColumnNames()
   {
      if (ms_columnNames == null)
         ms_columnNames = new String[] {getString("name"), getString("desc")};
      
      return ms_columnNames;
   }

   @Override
   protected Object[][] getTableData()
   {
      String[][] result = 
         new String [m_catalogerConfigs.size()][getColumnNames().length];
      
      for (int i = 0; i < result.length; i++)
      {
         result[i] = configToRowData(m_catalogerConfigs.get(i)); 
      }
      
      return result;
   }
   
   /**
    * Get the list of configurations supplied to this panel during construction,
    * possibly modified by the panel.
    * 
    * @return The list, never <code>null</code>, may be empty.
    */
   public List<PSCatalogerConfig> getConfigs()
   {
      return m_catalogerConfigs;
   }

   /**
    * Cataloger configurations, initialized in the constructor, 
    * never <code>null</code> after that.
    */
   private List<PSCatalogerConfig> m_catalogerConfigs;
   
   /**
    * The parent frame supplied during ctor, never <code>null</code>
    * after that.
    */
   private Frame m_parent;
   
   /**
    * Type of configurations to edit specified during construction, never
    * <code>null</code> or modified after that.
    */
   private PSCatalogerConfig.ConfigTypes m_type; 
   
   /**
    * Array of column names, never <code>null</code> or empty after first call
    * to {@link #getColumnNames()}
    */   
   private static String[] ms_columnNames = null;
   
   /**
    * Constant for index of name column in table model.
    */
   private static final int NAME_COL = 0;

   /**
    * Constant for index of description column in table model.
    */
   private static final int DESC_COL = 1;
}


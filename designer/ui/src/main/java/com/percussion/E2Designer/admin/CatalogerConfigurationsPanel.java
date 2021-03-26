/******************************************************************************
 *
 * [ CatalogerConfigurationsPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.services.security.data.PSCatalogerConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Panel to provide add/edit/delete functionality for both subject and role
 * catalogers
 */
public class CatalogerConfigurationsPanel extends JPanel 
   implements ITabDataHelper
{
   private static final long serialVersionUID = 1L;

   /**
    * Construct the dialog
    * 
    * @param parent The parent frame, may not be <code>null</code>.
    * @param configs The list of configurations to modify, may not be 
    * <code>null</code>.  The passed in list is directly modified by this
    * dialog when {@link #saveTabData()} is called.
    */
   public CatalogerConfigurationsPanel(Frame parent, 
      List<PSCatalogerConfig> configs)
   {
      super(new BorderLayout());
      
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      
      if (configs == null)
         throw new IllegalArgumentException("configs may not be null");
      
      m_parent = parent;
      m_catalogerConfigs = configs;
      
      initDialog();
   }

   /**
    * Initializes the dialog UI and data.
    */
   private void initDialog()
   {
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      String key = "catalogerConfigs.";
      // create the header message
      JLabel label = new JLabel(ms_res.getString(key + "headerText"));
      label.setBorder(new EmptyBorder(5, 5, 10, 5));
      label.setAlignmentX(LEFT_ALIGNMENT);
      add(label, BorderLayout.NORTH);

      add(createConfigsPanel(), BorderLayout.CENTER);
   }

   /**
    * Create the panel containing the sub-panels to edit each type of config.
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createConfigsPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      
      List<PSCatalogerConfig> subConfigs = new ArrayList<PSCatalogerConfig>();
      List<PSCatalogerConfig> roleConfigs = new ArrayList<PSCatalogerConfig>();
      for (PSCatalogerConfig config : m_catalogerConfigs)
      {
         if (config.getConfigType().equals(
            PSCatalogerConfig.ConfigTypes.SUBJECT))
         {
            subConfigs.add(config);
         }
         else
         {
            roleConfigs.add(config);
         }
      }
      
      m_subjectPanel = new CatalogerConfigurationPanel(m_parent, subConfigs, 
         PSCatalogerConfig.ConfigTypes.SUBJECT);
      m_rolePanel = new CatalogerConfigurationPanel(m_parent, roleConfigs, 
         PSCatalogerConfig.ConfigTypes.ROLE);
      
      panel.add(m_subjectPanel);
      panel.add(Box.createVerticalStrut(10));
      panel.add(m_rolePanel);
      
      return panel;
   }

   // see ITabDataHelper
   public boolean saveTabData()
   {
      boolean isModified = false;
      if (m_subjectPanel.isModified())
      {
         m_subjectPanel.setModified(false);
         isModified = true;
      }
      
      if (m_rolePanel.isModified())
      {
         m_rolePanel.setModified(false);
         isModified = true;
      }
      
      if (isModified)
      {
         m_catalogerConfigs.clear();
         m_catalogerConfigs.addAll(m_subjectPanel.getConfigs());
         m_catalogerConfigs.addAll(m_rolePanel.getConfigs());
         
         AppletMainDialog.setRestartRequired();
      }
      
      return isModified;
   }

   // see ITabDataHelper
   public boolean validateTabData()
   {
      return true;
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
   protected Frame m_parent;
   
   /**
    * Panel used to edit the subject cataloger configs, never <code>null</code>
    * after construction.
    */
   private CatalogerConfigurationPanel m_subjectPanel;

   /**
    * Panel used to edit the role cataloger configs, never <code>null</code>
    * after construction.
    */
   private CatalogerConfigurationPanel m_rolePanel;
   
   /**
    * The resource bundle to use, never <code>null</code>.
    */
   private static ResourceBundle ms_res = PSServerAdminApplet.getResources();
}


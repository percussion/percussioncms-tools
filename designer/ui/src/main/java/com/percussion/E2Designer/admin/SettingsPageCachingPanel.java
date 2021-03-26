/******************************************************************************
 *
 * [ SettingsPageCachingPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTFixedTextField;
import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.layout.PSGridBoxLayout;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/** The Page Caching panel in the Settings Tab of the Admin client to set the
 *  server cache settings.
 */
public class SettingsPageCachingPanel extends JPanel implements ITabDataHelper
{

   /**
    * Creates an instance of this panel to set the server cache settings for cms
    * caching and initializes the panel with data.
    *
    * @param config the server configuration, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if config is <code>null</code>.
    */
   public SettingsPageCachingPanel(ServerConfiguration config)
   {
      super();
      if(config == null)
         throw new IllegalArgumentException("config can not be null");

      m_config = config;
      initPanel();
      initData();
   }

   //Implementing ITabDataHelper interface.
   public boolean saveTabData()
   {
      //add code to save data
      boolean enabled = m_cbEnableCaching.isSelected();
      long memUsage = -1;
      long diskUsage = -1;
      long pageSize = -1;
      long agingTime = -1;
      boolean enableFolderCaching = m_cbEnableFolderCaching.isSelected();

      if(!m_cbUnlimitedMaxMemory.isSelected())
      {
         memUsage = Long.parseLong( m_maxMemoryField.getText() );
         memUsage *= MEG_TO_BYTE;
      }
      
      if(!m_cbUnlimitedMaxDisk.isSelected())
      {
         diskUsage = Long.parseLong( m_maxDiskField.getText() );
         diskUsage *= MEG_TO_BYTE;
      }
            
      if(!m_cbUnlimitedMaxPage.isSelected())
      {
         pageSize = Long.parseLong( m_maxPageField.getText() );
         pageSize *= KILO_TO_BYTE;
      }

      if(!m_cbUnlimitedAgingTime.isSelected())
         agingTime = Long.parseLong( m_agingTimeField.getText() );

      PSServerCacheSettings settings = new PSServerCacheSettings(enabled,
            enableFolderCaching, memUsage, diskUsage, pageSize,
            agingTime);
      m_config.getServerConfiguration().setServerCacheSettings(settings);

      return true;
   }

   /**
    * Implementing ITabDataHelper interface for validating this tab data.
    * Validates all text fields if their corresponding 'Unlimited' check boxes
    * are not checked off. If the value is invalid and 'Enable Server Cache'
    * checkbox is not selected (server cache is not enabled), then it sets the
    * value to default value, otherwise it displays error message to the user
    * to correct it.
    *
    * @return <code>true</code> if the validation is successful, otherwise
    * <code>false</code>
    */
   public boolean validateTabData()
   {
      if( !validateMemoryField(m_cbUnlimitedMaxMemory, m_maxMemoryField,
         ms_res.getString("bigMemory"),
         PSServerCacheSettings.DEFAULT_MEM_USAGE) )
         return false;

      if( !validateMemoryField(m_cbUnlimitedMaxDisk, m_maxDiskField,
         ms_res.getString("bigDiskSpace"),
         PSServerCacheSettings.DEFAULT_DISK_USAGE) )
         return false;

      long maxMemory = Long.parseLong( m_maxMemoryField.getText() );
      long maxDisk = Long.parseLong( m_maxDiskField.getText() );

      if(maxMemory == 0 && maxDisk == 0)
      {
         if(m_cbEnableCaching.isSelected())
         {
            displayErrorMessage(m_maxMemoryField,
               ms_res.getString("invalidMemDisk"));
            return false;
         }
         else
         {
            //if the server cache is disbled set the default value to disk space
            //alone because either memory or disk space are allowed to be zero.
            m_maxDiskField.setText( String.valueOf(
               PSServerCacheSettings.DEFAULT_DISK_USAGE/MEG_TO_BYTE));
         }
      }

      //validate page size      
      if(!m_cbUnlimitedMaxPage.isSelected())
      {
         long size = 0;
         try {
            size = Long.parseLong( m_maxPageField.getText() );
            if(size <= 0)
            {
               if(m_cbEnableCaching.isSelected())
               {
                  displayErrorMessage(m_maxPageField,
                     ms_res.getString("invalidPageSize"));
                  return false;
               }
               else
               {
                  m_maxPageField.setText( String.valueOf(
                     PSServerCacheSettings.DEFAULT_PAGE_SIZE/KILO_TO_BYTE ));
               }
            }
         }
         catch(NumberFormatException e)
         {
            if(m_cbEnableCaching.isSelected())
            {
               displayErrorMessage(
                  m_maxPageField, ms_res.getString("invalidValue"));
               return false;
            }
            else
            {
               m_maxPageField.setText( String.valueOf(
                  PSServerCacheSettings.DEFAULT_PAGE_SIZE/KILO_TO_BYTE ) );
            }
         }        
      }

      //validate aging time
      if(!m_cbUnlimitedAgingTime.isSelected())
      {
         long time = 0;
         try {
            time = Long.parseLong( m_agingTimeField.getText() );
         }
         catch(NumberFormatException e)
         {
            if(m_cbEnableCaching.isSelected())
            {
               displayErrorMessage(
                  m_agingTimeField, ms_res.getString("invalidValue"));
               return false;
            }
            else
            {
               m_agingTimeField.setText( String.valueOf(
                  PSServerCacheSettings.DEFAULT_AGING_TIME ) );
               return true;
            }
         }
         if(time <= 0)
         {
            if(m_cbEnableCaching.isSelected())
            {
               displayErrorMessage(m_agingTimeField,
                  ms_res.getString("invalidTime"));
               return false;
            }
            else
            {
               m_agingTimeField.setText( String.valueOf(
                  PSServerCacheSettings.DEFAULT_AGING_TIME ));
            }
         }
      }
      return true;
   }

   /**
    * Worker method for validating data in memory fields. Validates the data and
    * displays an error message to the user for invalid field if 'Enable Server
    * Cache' checkbox is selected, otherwise sets default value to the field.
    *
    * @param unlimitedBox The checkbox to check whether the value in the field
    * needs to be validated or not. If this is checked, then the value is not
    * validated as it will be ignored and takes '-1' to signify 'unlimited'
    * value. Assumed not to be <code>null</code>
    * @param field The text field in which data entered should be validated,
    * assumed not to be <code>null</code>
    * @param errorMessage the error message to be displayed in case the value is
    * too big, assumed not to be <code>null</code> or empty.
    * @param defValue The default value to use if the value in field is invalid
    * and 'Enable Server Cache' checkbox is not selected.
    *
    * @return <code>true</code> if the data in <code>field</code> is valid,
    * otherwise <code>false</code>
    */
   private boolean validateMemoryField(JCheckBox unlimitedBox, JTextField field,
      String errorMessage, long defValue)
   {
      if(!unlimitedBox.isSelected())
      {
         long memInMB = 0;
         try {
            memInMB = Long.parseLong( field.getText() );
         }
         catch(NumberFormatException e)
         {
            if(m_cbEnableCaching.isSelected())
            {
               displayErrorMessage(field, ms_res.getString("invalidValue"));
               return false;
            }
            else
            {
               field.setText( String.valueOf( defValue/MEG_TO_BYTE ));
               return true;
            }
         }
         if(m_cbEnableCaching.isSelected())
         {
            if(memInMB < 0)
            {
               displayErrorMessage(field, ms_res.getString("noNegative"));
               return false;
            }

            if(memInMB*MEG_TO_BYTE > Long.MAX_VALUE)
            {
               displayErrorMessage(field, errorMessage);
               return false;
            }
         }
         else
         {
            if(memInMB < 0 || memInMB*MEG_TO_BYTE > Long.MAX_VALUE)
               field.setText( String.valueOf( defValue/MEG_TO_BYTE ));
         }
      }
      return true;
   }

   /**
    * Displays error message to the user and sets focus on the provided
    * textfield.
    *
    * @param field the text field to get focus, assumed not to be
    * <code>null</code>.
    * @param errorMessage the error message to display, assumed not to be
    * <code>null</code> and empty.
    */
   private void displayErrorMessage(JTextField field, String errorMessage)
   {
      JOptionPane.showMessageDialog( PSServerAdminApplet.getFrame(),
                                     errorMessage,
                                     ms_res.getString("error"),
                                     JOptionPane.ERROR_MESSAGE );
      field.requestFocus();
   }

   /**
    * Initializes the panel with all controls and sets listeners on controls.
    */
   private void initPanel()
   {
      // set this panel with border layout to contain all component in NORTH 
      // region to prevent undesired "spread" of components
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      
      JPanel outerPanel = new JPanel();
      outerPanel.setLayout( new BoxLayout(outerPanel, BoxLayout.Y_AXIS) );
      add(outerPanel, BorderLayout.NORTH);
      
      
      // enable cache cb panel
      JPanel cachePanel = new JPanel(  );
      cachePanel.setLayout( new BoxLayout(cachePanel, BoxLayout.X_AXIS) );
      m_cbEnableCaching = new JCheckBox(ms_res.getString("enablePageCaching"));
      m_cbEnableCaching.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            updateSettingsPanel(m_cbEnableCaching.isSelected());
         }
      });
      cachePanel.add(Box.createHorizontalStrut(15) );
      cachePanel.add(m_cbEnableCaching);
      cachePanel.add(Box.createHorizontalGlue());
      
      // setting panel
      JPanel settingsPanel = createPageCacheSettingsPanel();
      
      // create group for page cache settings
      JPanel pageCachePanel = new JPanel();
      pageCachePanel.setLayout(new BoxLayout(pageCachePanel, BoxLayout.Y_AXIS));
      pageCachePanel.setBorder(new TitledBorder(new EtchedBorder(
         EtchedBorder.LOWERED), ms_res.getString("pageCaching")));
      
      pageCachePanel.add(cachePanel);
      pageCachePanel.add(Box.createVerticalStrut(10));
      pageCachePanel.add(settingsPanel);
      pageCachePanel.add(Box.createVerticalStrut(10));
      
      outerPanel.add(pageCachePanel);
      outerPanel.add(Box.createVerticalStrut(10));
      
      
      // folder cache panel      
      JPanel cacheFolderPanel = new JPanel();
      m_cbEnableFolderCaching = new JCheckBox(ms_res
            .getString("folderCacheEnable"));
      cacheFolderPanel.setLayout(new BoxLayout(cacheFolderPanel,
            BoxLayout.X_AXIS));
      cacheFolderPanel.add( m_cbEnableFolderCaching);
      cacheFolderPanel.add(Box.createHorizontalGlue());

      outerPanel.add(cacheFolderPanel);
      outerPanel.add(Box.createVerticalGlue());
   }

   /**
    * Creates the panel with page cache settings controls
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createPageCacheSettingsPanel()
   {
      JPanel settingsPanel = new JPanel();
      settingsPanel.setLayout(new PSGridBoxLayout(settingsPanel, 4, 5, 10, 10));

      settingsPanel.add(Box.createHorizontalGlue());
      JLabel maxMemoryLabel =
         new JLabel(ms_res.getString("maxMemory"), SwingConstants.RIGHT);
      maxMemoryLabel.setAlignmentX(RIGHT_ALIGNMENT);
      settingsPanel.add( maxMemoryLabel );
      m_maxMemoryField = new UTFixedTextField("", FIELD_SIZE);

      settingsPanel.add( m_maxMemoryField );
      m_cbUnlimitedMaxMemory = new JCheckBox(ms_res.getString("unlimited"));
      m_cbUnlimitedMaxMemory.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_maxMemoryField.setEnabled(!m_cbUnlimitedMaxMemory.isSelected());
         }
      });
      settingsPanel.add( m_cbUnlimitedMaxMemory );
      settingsPanel.add(Box.createHorizontalGlue());

      settingsPanel.add(Box.createHorizontalGlue());
      JLabel maxDiskLabel =
         new JLabel(ms_res.getString("maxDisk"), SwingConstants.RIGHT);
      maxDiskLabel.setAlignmentX(RIGHT_ALIGNMENT);
      settingsPanel.add( maxDiskLabel );
      m_maxDiskField = new UTFixedTextField("", FIELD_SIZE);
      settingsPanel.add( m_maxDiskField );
      m_cbUnlimitedMaxDisk = new JCheckBox(ms_res.getString("unlimited"));
      m_cbUnlimitedMaxDisk.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_maxDiskField.setEnabled(!m_cbUnlimitedMaxDisk.isSelected());
         }
      });
      settingsPanel.add( m_cbUnlimitedMaxDisk );
      settingsPanel.add(Box.createHorizontalGlue());
      
      settingsPanel.add(Box.createHorizontalGlue());
      JLabel maxPageLabel =
         new JLabel(ms_res.getString("maxPage"), SwingConstants.RIGHT);
      maxPageLabel.setAlignmentX(RIGHT_ALIGNMENT);
      settingsPanel.add( maxPageLabel );
      m_maxPageField = new UTFixedTextField("", FIELD_SIZE);
      settingsPanel.add( m_maxPageField );
      m_cbUnlimitedMaxPage = new JCheckBox(ms_res.getString("unlimited"));
      m_cbUnlimitedMaxPage.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_maxPageField.setEnabled(!m_cbUnlimitedMaxPage.isSelected());
         }
      });
      settingsPanel.add( m_cbUnlimitedMaxPage );
      settingsPanel.add(Box.createHorizontalGlue());

      settingsPanel.add(Box.createHorizontalGlue());
      JLabel agingTimeLabel =
         new JLabel(ms_res.getString("agingTime"), SwingConstants.RIGHT);
      agingTimeLabel.setAlignmentX(RIGHT_ALIGNMENT);
      settingsPanel.add( agingTimeLabel );
      m_agingTimeField = new UTFixedTextField("", FIELD_SIZE);

      settingsPanel.add( m_agingTimeField );
      m_cbUnlimitedAgingTime = new JCheckBox(ms_res.getString("unlimited"));
      m_cbUnlimitedAgingTime.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_agingTimeField.setEnabled(!m_cbUnlimitedAgingTime.isSelected());
         }
      });
      settingsPanel.add( m_cbUnlimitedAgingTime );
      settingsPanel.add(Box.createHorizontalGlue());
      
      return settingsPanel;
   }

   /**
    * Enables/disables the controls to set server cache settings. The text
    * fields are enabled if <code>enable</code> is <code>true</code> and their
    * corresponding 'Unlimited' check boxes are not checked off.
    *
    * @param enable if <code>true</code> enables the controls, otherwise
    * disables.
    */
   private void updateSettingsPanel(boolean enable)
   {
      if(enable)
      {
         m_maxMemoryField.setEnabled(!m_cbUnlimitedMaxMemory.isSelected());
         m_maxDiskField.setEnabled(!m_cbUnlimitedMaxDisk.isSelected());
         m_maxPageField.setEnabled(!m_cbUnlimitedMaxPage.isSelected());         
         m_agingTimeField.setEnabled(!m_cbUnlimitedAgingTime.isSelected());
      }
      else
      {
         m_maxMemoryField.setEnabled(enable);
         m_maxDiskField.setEnabled(enable);
         m_maxPageField.setEnabled(enable);         
         m_agingTimeField.setEnabled(enable);
      }
      m_cbUnlimitedMaxMemory.setEnabled(enable);
      m_cbUnlimitedMaxDisk.setEnabled(enable);
      m_cbUnlimitedMaxPage.setEnabled(enable);      
      m_cbUnlimitedAgingTime.setEnabled(enable);
   }

   /**
    * Initializes all controls with their corresponding data.
    */
   private void initData()
   {
      PSServerCacheSettings settings =
         m_config.getServerConfiguration().getServerCacheSettings();

      m_cbEnableFolderCaching.setSelected(settings.isFolderCacheEnabled());
      
      boolean enable = settings.isEnabled();
      m_cbEnableCaching.setSelected(enable);
      updateSettingsPanel(enable);

      long maxMemory = settings.getMaxMemoryUsage();
      if( maxMemory == -1)
      {
         m_maxMemoryField.setEnabled(false);
         m_maxMemoryField.setText( String.valueOf(
            PSServerCacheSettings.DEFAULT_MEM_USAGE/MEG_TO_BYTE) );
         m_cbUnlimitedMaxMemory.setSelected(true);
      }
      else
         m_maxMemoryField.setText( String.valueOf(maxMemory/MEG_TO_BYTE) );

      long maxDisk = settings.getMaxDiskUsage();
      if( maxDisk == -1)
      {
         m_maxDiskField.setEnabled(false);
         m_maxDiskField.setText( String.valueOf(
            PSServerCacheSettings.DEFAULT_DISK_USAGE/MEG_TO_BYTE) );
         m_cbUnlimitedMaxDisk.setSelected(true);
      }
      else
         m_maxDiskField.setText( String.valueOf(maxDisk/MEG_TO_BYTE) );
         
      long maxPage = settings.getMaxPageSize();
      if( maxPage == -1)
      {
         m_maxPageField.setEnabled(false);
         m_maxPageField.setText( String.valueOf(
            PSServerCacheSettings.DEFAULT_PAGE_SIZE/KILO_TO_BYTE) );
         m_cbUnlimitedMaxPage.setSelected(true);
      }
      else
         m_maxPageField.setText( String.valueOf(maxPage/KILO_TO_BYTE) );         

      long agingTime = settings.getAgingTime();
      if( agingTime == -1)
      {
         m_agingTimeField.setEnabled(false);
         m_agingTimeField.setText( String.valueOf(
            PSServerCacheSettings.DEFAULT_AGING_TIME) );
         m_cbUnlimitedAgingTime.setSelected(true);
      }
      else
         m_agingTimeField.setText( String.valueOf(agingTime) );

   }

   /**
    * The server configuration object, gets initialized in the constructor and
    * never <code>null</code> after that.
    */
   private ServerConfiguration m_config = null;

   /**
    * The checkbox to set folder caching enabled/disabled, gets initialized in
    * <code>initPanel()</code> and never <code>null</code> or modified after
    * that.
    */
   private JCheckBox m_cbEnableFolderCaching;
   
   /**
    * The checkbox to set caching enabled/disabled, gets initialized in
    * <code>initPanel()</code> and never <code>null</code> or modified after
    * that.
    */
   private JCheckBox m_cbEnableCaching;

   /**
    * The checkbox to set unlimited maximum memory, gets initialized in
    * <code>initPanel()</code> and never <code>null</code> or modified after
    * that.
    */
   private JCheckBox m_cbUnlimitedMaxMemory;

   /**
    * The checkbox to set unlimited maximum disk space, gets initialized in
    * <code>initPanel()</code> and never <code>null</code> or modified after
    * that.
    */
   private JCheckBox m_cbUnlimitedMaxDisk;
   
   /**
    * The checkbox to set unlimited maximum page size, gets initialized in
    * <code>initPanel()</code> and never <code>null</code> or modified after
    * that.
    */
   private JCheckBox m_cbUnlimitedMaxPage;

   /**
    * The checkbox to set unlimited aging time, gets initialized in <code>
    * initPanel()</code> and never <code>null</code> or modified after that.
    */
   private JCheckBox m_cbUnlimitedAgingTime;

   /**
    * The text field to enter the maximum memory, gets initialized in <code>
    * initPanel()</code> and never <code>null</code> or modified after that.
    */
   private UTFixedTextField m_maxMemoryField;

   /**
    * The text field to enter the maximum disk space, gets initialized in <code>
    * initPanel()</code> and never <code>null</code> or modified after that.
    */
   private UTFixedTextField m_maxDiskField;
   
   /**
    * The text field to enter the maximum page size, gets initialized in <code>
    * initPanel()</code> and never <code>null</code> or modified after that.
    */
   private UTFixedTextField m_maxPageField;   

   /**
    * The text field to enter the aging time, gets initialized in <code>
    * initPanel()</code> and never <code>null</code> or modified after that.
    */
   private UTFixedTextField m_agingTimeField;

  /**
   * Resource bundle to get resource strings for this panel.
   */
  private static ResourceBundle ms_res = PSServerAdminApplet.getResources();

   /**
    * The dimension required for the text fields in this panel.
    */
   private static final Dimension FIELD_SIZE = new Dimension(100, 22);

   /**
    * The constant to use for converting mega bytes to bytes or bytes to mega
    * bytes.
    */
   private static final long MEG_TO_BYTE = 1024*1024;
   
   /**
    * The constant to use for converting kilo bytes to bytes or bytes to kilo
    * bytes.
    */
   private static final long KILO_TO_BYTE = 1024;   
}


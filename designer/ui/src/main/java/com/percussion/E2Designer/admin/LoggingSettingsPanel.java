/*[ LoggingSettingsPanel.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.PSComboBox;
import com.percussion.E2Designer.UTFixedTextField;
import com.percussion.E2Designer.UTMnemonicLabel;
import com.percussion.design.objectstore.PSLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;


/**
 * The applets main dialog is implemented as a tabbed dialog. One Tab of this
 * dialog is the "Logging" Tab which itself contains several Tabs. This class
 * implements the GUI elements and its functionality for the "Logging" "Settings" Tab.
 */
public class LoggingSettingsPanel extends JPanel implements ITabDataHelper
{
   /**
    * Construct the GUI elements and initialize them with actual data.
    *
    * @param   serverConfig      the actual server configuration
    */
   public LoggingSettingsPanel(ServerConfiguration serverConfig)
   {
      try
      {
         m_serverConf = serverConfig;
         if(m_serverConf != null)
            m_iLogDays = m_serverConf.getRunningLogDays();

         this.setLayout(new BorderLayout());
         this.setBorder(new EmptyBorder(5, 5, 5, 5));

         // LogLevelChooser is not relevant in Rhythmyx v1.0
         //this.add("North", createLogLevelChooserPanel());
         this.add(createLogOptionsPanel(), BorderLayout.CENTER);
         //this.add("South", createCommandPanel());

         this.add(createAutoDeletePanel(), BorderLayout.SOUTH);
         initAutoDeletePanel();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }

   }

   /**
    * Create the panel and implement their listeners.
    *
    */
    private   JPanel createAutoDeletePanel()
   {
    // create a panel with the checkbox
      JPanel checkBoxPanel = new JPanel();
      checkBoxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      m_cbAutoDelete = new JCheckBox(m_res.getString("autoDelete"));
      m_cbAutoDelete.addChangeListener(new ChangeListener(){
         public void stateChanged(ChangeEvent e)
         {
            m_bModified = true;
        if (m_cbAutoDelete.isSelected())
        {
          m_keepLastLabel.setEnabled(true);
          m_tfNumberOfDays.setEnabled(true);
          m_daysLogLabel.setEnabled(true);
        }
        else
        {
          m_keepLastLabel.setEnabled(false);
          m_tfNumberOfDays.setEnabled(false);
          m_daysLogLabel.setEnabled(false);
        }
         }
      });
      checkBoxPanel.add(m_cbAutoDelete);

      // create a panel with a label and a Textfield
      JPanel logPanel = new JPanel();
      logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.X_AXIS));
      m_tfNumberOfDays = new UTFixedTextField("", new Dimension(50, 22));
      m_tfNumberOfDays.addKeyListener(new KeyAdapter()
      {
         public void KeyPressed(KeyEvent e)
         {
            if (!m_bModified)
               LoggingSettingsChanged();
         }
      });
      m_tfNumberOfDays.addFocusListener(new FocusListener()
      {
         public void focusLost(FocusEvent e)
         {
            String text = m_tfNumberOfDays.getText();

            if (text.equals(""))
            {
               m_tfNumberOfDays.setText(new Integer(LOGDAYS_DEFAULT).toString());
               return;
            }
        
            for (int i = 0; i < text.length(); i++)
            {
               if (!Character.isDigit(text.charAt(i)))
               {
                  m_tfNumberOfDays.setText(new Integer(LOGDAYS_DEFAULT).toString());
                  return;
               }
            }
         }

         public void focusGained(FocusEvent e) {}
      });

      logPanel.add(Box.createHorizontalStrut(3));
      logPanel.add(m_keepLastLabel);
      logPanel.add(Box.createHorizontalStrut(6));
      logPanel.add(m_tfNumberOfDays);
      logPanel.add(Box.createHorizontalStrut(6));
      logPanel.add(m_daysLogLabel);
      logPanel.add(Box.createHorizontalGlue());
    
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                            m_res.getString("autoDelete")));

      panel.add(checkBoxPanel);
      panel.add(logPanel);

      
      JPanel outerPanel = new JPanel(new BorderLayout());
      outerPanel.setBorder(new EmptyBorder(5, 10, 0, 10));
      outerPanel.add(panel, BorderLayout.CENTER);

      return outerPanel;
   }


   /**
    * initializes the panel based on the number of days from the ServerConfiguration
    * object.
    */
   private void initAutoDeletePanel()
   {
      m_tfNumberOfDays.setText(""+m_iLogDays);
      if(m_iLogDays > 0)
      {
         m_cbAutoDelete.setSelected(true);
      }
    else
    {
      m_daysLogLabel.setEnabled(false);
      m_keepLastLabel.setEnabled(false);
      m_tfNumberOfDays.setEnabled(false);
    }

   }

/** Saves the tab data.
   * Saves the number of days the log is to be kept. If set to zero, auto delete
   * is off.
  *
   * @returns boolean <CODE>true</CODE> = data is ok; 
  * <CODE>false</CODE> otherwise.
   */
   public boolean saveTabData()
   {
      if (!m_bModified)
         return false;

      System.out.println("saving LoggingSettings tab data");
      m_logConf.setErrorLoggingEnabled(m_errors.isSelected());
      m_logConf.setServerStartStopLoggingEnabled(m_serverStartStop.isSelected());
      m_logConf.setAppStartStopLoggingEnabled(m_applicationStartStop.isSelected());
      m_logConf.setAppStatisticsLoggingEnabled(m_applicationStatistics.isSelected());
      m_logConf.setBasicUserActivityLoggingEnabled(m_basicUserActivity.isSelected());
      m_logConf.setDetailedUserActivityLoggingEnabled(m_detailedUserActivity.isSelected());
      m_logConf.setFullUserActivityLoggingEnabled(m_fullUserActivity.isSelected());

      m_serverConf.setLogConfiguration(m_logConf);
      
      if(m_cbAutoDelete.isSelected())
      {
          String strDays = m_tfNumberOfDays.getText().trim();
         m_iLogDays = Integer.parseInt(strDays);
         m_serverConf.setRunningLogDays(m_iLogDays);
      }
      else
      {
         m_serverConf.setRunningLogDays(0);
      }
   ///// todo: set m_bModified to false once Sun fixes the bug with JTextField events.
   //    m_bModified = false;
      return true;
   }

   /** Does not need validation. Thus does nothing.
    */
   public boolean validateTabData()
   {
      boolean bValid = true;
      String strDays = m_tfNumberOfDays.getText().trim();
      try
      {
         Integer.parseInt(strDays);
      }
      catch(NumberFormatException e)
      {
         bValid = false;
         JOptionPane.showMessageDialog(PSServerAdminApplet.getFrame(),
                                                 e.getMessage(),
                                     m_res.getString("error"),
                                     JOptionPane.ERROR_MESSAGE);

         m_tfNumberOfDays.requestFocus();

      }

      return bValid;
   }
   
   /**
    * Create and initialize the log level chooser and implement its action
    * listener.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private   JPanel createLogLevelChooserPanel() throws Exception
   {
        m_logLevel.addActionListener(new LoggingSettingActionListener());

      m_logLevel.setPreferredSize(LOG_LEVEL_CHOOSER_SIZE);
    
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      panel.add(new UTMnemonicLabel(m_res, "logLevel", m_logLevel));
      panel.add(m_logLevel);

      return panel;
   }


   /**
    * Create and initialize the log settings option panel and implement the
    * action listeners.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private   JPanel createLogOptionsPanel() throws Exception
  {
     JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                            m_res.getString("keepLog")));

      JPanel innerPanel = new JPanel(new BorderLayout());
      innerPanel.setBorder(new EmptyBorder(0, 10, 0, 5));
      // get servers log configuration
      m_logConf = m_serverConf.getLogConfiguration();

      Box b = new Box(BoxLayout.Y_AXIS);
      b.add(m_errors);
      b.add(m_serverStartStop);
      b.add(m_applicationStartStop);
      b.add(m_applicationStatistics);
      b.add(m_basicUserActivity);
      b.add(m_detailedUserActivity);
      b.add(m_fullUserActivity);

      initComponents();
      initComponentListeners();

      innerPanel.add(b, BorderLayout.CENTER);
      panel.add(innerPanel, BorderLayout.CENTER);
      
      JPanel outerPanel = new JPanel(new BorderLayout());
      outerPanel.setBorder(new EmptyBorder(5, 10, 0, 10));
      outerPanel.add(panel, BorderLayout.CENTER);

      return outerPanel;
   }


   /**
    * Initializes the checkboxes' data.
    */
   private void initComponents()
   {
      m_errors.setSelected(m_logConf.isErrorLoggingEnabled());
      m_serverStartStop.setSelected(m_logConf.isServerStartStopLoggingEnabled());
      m_applicationStartStop.setSelected(m_logConf.isAppStartStopLoggingEnabled());
      m_applicationStatistics.setSelected(m_logConf.isAppStatisticsLoggingEnabled());
      m_basicUserActivity.setSelected(m_logConf.isBasicUserActivityLoggingEnabled());
      m_detailedUserActivity.setSelected(m_logConf.isDetailedUserActivityLoggingEnabled());
      m_fullUserActivity.setSelected(m_logConf.isFullUserActivityLoggingEnabled());
   }


   /**
    * Initializes the multiple component action listeners.
    */
   private void initComponentListeners()
   {
      m_errors.addActionListener(new LoggingSettingActionListener());
      m_serverStartStop.addActionListener(new LoggingSettingActionListener());
      m_applicationStartStop.addActionListener(new LoggingSettingActionListener());
      m_applicationStatistics.addActionListener(new LoggingSettingActionListener());
      m_basicUserActivity.addActionListener(new LoggingSettingActionListener());
      m_detailedUserActivity.addActionListener(new LoggingSettingActionListener());
      m_fullUserActivity.addActionListener(new LoggingSettingActionListener());
   }


   /**
    * Enables the "Apply" button if any log setting changed since initializing
    * the panel or the last "Apply" command.
    */
    ///////////////////////////////////////////////////////////////////////////
   private void LoggingSettingsChanged()
   {
      m_bModified = true;
      //m_applyButton.setEnabled(m_bModified);
   }


   /**
    * The action listener that multiple components of the panel will use.
    */
   private class LoggingSettingActionListener implements ActionListener
   {
      public void actionPerformed(ActionEvent event)
        {
          LoggingSettingsChanged();
       }
   }


  //////////////////////////////////////////////////////////////////////////////
   /**
    * the servers configuration we are working with
    */
  private ServerConfiguration m_serverConf = null;
   /**
    * the servers log configuration
    */
   private PSLogger m_logConf = null;

   /**
    * the log level setting
    */
  private String LOG_LEVEL_OPTIONS[] =
  {
      m_res.getString("logLevel1"),
      m_res.getString("logLevel2"),
      m_res.getString("logLevel3")
  };
  private PSComboBox m_logLevel = new PSComboBox(LOG_LEVEL_OPTIONS);
   /**
    * the log error events flag
    */
  private JCheckBox m_errors = new JCheckBox(m_res.getString("logErrors"));
   /**
    * the log server start/stop events flag
    */
   private JCheckBox m_serverStartStop = new JCheckBox(m_res.getString("serverStartStop"));
   /**
    * the log application start/stop events flag
    */
  private JCheckBox m_applicationStartStop = new JCheckBox(m_res.getString("applicationStartStop"));
   /**
    * the log sapplication statistics events flag
    */
  private JCheckBox m_applicationStatistics = new JCheckBox(m_res.getString("applicationStatistics"));
   /**
    * the log basic user events flag
    */
  private JCheckBox m_basicUserActivity = new JCheckBox(m_res.getString("userBasic"));
   /**
    * the log detailed user events flag
    */
  private JCheckBox m_detailedUserActivity = new JCheckBox(m_res.getString("userDetailed"));
   /**
    * the log full user events flag
    */
  private JCheckBox m_fullUserActivity = new JCheckBox(m_res.getString("userFull"));

  /**
   * The labels for days log field.
   */
  private JLabel m_keepLastLabel = new JLabel(m_res.getString("keepLast"));
   private JLabel m_daysLogLabel = new JLabel(m_res.getString("daysOfLog"));

   /**
    * the apply button
    */
  //private JButton m_applyButton = new UTFixedButton(m_res.getString("apply"));
   /**
   * the preferred details chooser size
    */
  private static final Dimension LOG_LEVEL_CHOOSER_SIZE = new Dimension(400, 20);

   /**
    * The number of days to save the running log for. If set to 0, the 
    * auto delete feature is off.
    */
   private int m_iLogDays = 0;

   ///// m_bModified should be set to "false" but since the text fields do not generate
   ///// KeyTyped events, or even FocusGained events m_bModified never gets set to "true"
   ///// and data is not saved. WORKAROUND: always save this tab. Set m_bModified to true.
   ///// todo: set m_bModified to false once Sun fixes this bug.
  private boolean m_bModified = true;

   /**
    * The text field for number of days.
    */
   private UTFixedTextField m_tfNumberOfDays = null;

   private JCheckBox m_cbAutoDelete = null;

   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();

  /**
   * Default log days.
   */
  private static int LOGDAYS_DEFAULT = 7;
}

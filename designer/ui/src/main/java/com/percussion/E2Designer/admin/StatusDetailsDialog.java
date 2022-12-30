/*[ StatusDetailsDialog.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTFixedTextField;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ResourceBundle;

/**
 * This class implements the dialog to display the server or application
 * detailed (statistic) data.
 */
////////////////////////////////////////////////////////////////////////////////
public class StatusDetailsDialog extends JDialog implements WindowListener
{
   /**
    * Construct the details dialog with its initial data. Use this constructor to
   * construct the server details.
    *
   * @param parent         the paren frame
   * @param titel            the dialogs title
   * @param console         the server console used to access the data
    */
   //////////////////////////////////////////////////////////////////////////////
   public StatusDetailsDialog(Frame parent, String title,
                                          ServerConsole console)
   {
    this(parent, title, console, null);
    /*
      super(parent, title, true);
      try
      {
       m_parent = parent;
       m_console = console;
      m_application = null;

         initDialog(apps);
      }
      catch (Exception ex)
      {
         m_refreshTimer.stop();
         ex.printStackTrace();
      }
    */
   }

   /**
    * Construct the details dialog with its initial data. Use this constructor to
   * construct the application details.
    *
   * @param parent         the paren frame
   * @param titel            the dialogs title
   * @param console         the server console used to access the data
   * @param application   the application to initialize the data for
    */
   //////////////////////////////////////////////////////////////////////////////
   public StatusDetailsDialog(Frame parent, String title,
                                          ServerConsole console, String application)
   {
      super(parent, title, true);
      try
      {
       m_parent = parent;
       m_console = console;
      m_application = application;

      // initialize the update interval field
      Integer updateInterval = new Integer(ProjectConstants.DETAILS_UPDATE_INTERVAL);
        m_intervalField.setText(updateInterval.toString());

      // start timer thread to refresh data from server
      m_refreshTimer = new Timer(updateInterval.intValue(), new ActionListener()
      {
          public void actionPerformed(ActionEvent event)
          {
          refreshData(m_application);
        }
      });

         //initDialog(apps);
      initDialog();
      }
      catch (Exception ex)
      {
         m_refreshTimer.stop();
         ex.printStackTrace();
      }
   }

   /**
    * Refresh the statistics data from the passed application. Pass in null to
   * get the servers statistic data.
    *
   * @param parent         the application, null for the server
    */
   //////////////////////////////////////////////////////////////////////////////
   private void refreshData(String application)
   {
     try
    {
      Statistics statistics = null;
      if (application != null)
         statistics = new Statistics(m_console, application);
      else
         statistics = new Statistics(m_console);

      m_uptimeField.setText(statistics.getUptime());

      m_processedField.setText(statistics.getEventsProcessed());
      m_failedField.setText(statistics.getEventsFailed());
      m_pendingField.setText(statistics.getEventsPending());

      m_minimumField.setText(statistics.getMinimumEventTime());
      m_maximumField.setText(statistics.getMaximumEventTime());
      m_averageField.setText(statistics.getAverageEventTime());
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
   }

   /**
    * Initialize the dialogs GUI elements and start the interval timer for
   * refreshing the data from the server. It implements the timer action listener
   * as well.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   protected void initDialog() throws Exception
   {
     // we have to stop the refresh timer ourselfs
     this.addWindowListener(this);
    
    /*
     // add server and all applications to selectable items
    m_serverChooser.addItem("Server");
      for (int i=0; i<apps.size(); i++)
    {
      // only list "Active" applications
      //if (!((PSApplication)apps.elementAt(i)).isActive())
         m_serverChooser.addItem(apps.elementAt(i));
    }
    // set current selection
    if (m_application == null)
       m_serverChooser.setSelectedItem("Server");
    else
       m_serverChooser.setSelectedItem(m_application);
    */

      BorderLayout layout = new BorderLayout();
    this.getContentPane().setLayout(layout);
    this.getContentPane().add(createDetailsPanel());
        
      m_refreshTimer.start();

    // add data
        refreshData(m_application);
        this.pack();
        this.setResizable(true);
    }

   /**
    * Create the event statistics panel.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private   JPanel createDetailsPanel() throws Exception
   {
     JPanel panel = new JPanel();
      BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
      panel.setLayout(layout);
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));

      //panel.add(createServerPanel());
      panel.add(createUpdateIntervalPanel());
      panel.add(createUptimePanel());
      panel.add(createCounterPanel());
      panel.add(createTimerPanel());
    panel.add(createCommandPanel());
    panel.revalidate();

    return panel;
   }

   /**
    * Create the server/application chooser panel to change the dialogs data
   * source.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
  /*
   private   JPanel createServerPanel() throws Exception
   {
     m_serverChooser.setPreferredSize(DETAILS_CHOOSER_SIZE);
    m_serverChooser.addActionListener(new ActionListener()
    {
       public void actionPerformed(ActionEvent event)
      {
        m_application = (String) m_serverChooser.getSelectedItem();
         if (m_application == "Server")
           m_application = null;
          
        refreshData(m_application);
      }
    });
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(new JLabel(m_res.getString("detailsChooser")));
    panel.add(m_serverChooser);

    return panel;
   }
  */

   /**
    * Create the uptime panel.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private   JPanel createUptimePanel()
   {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(new JLabel(m_res.getString("uptime")));
    m_uptimeField.setEnabled(false);
    m_uptimeField.setBackground(Color.lightGray);
    m_uptimeField.setHorizontalAlignment(SwingConstants.RIGHT);
    panel.add(m_uptimeField);

    return panel;
   }

   /**
    * Create the statistic counter panel.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private   JPanel createCounterPanel()
   {
    JPanel panel = new JPanel();
    BoxLayout box = new BoxLayout(panel, BoxLayout.X_AXIS);
    panel.setLayout(box);
    panel.setBorder(new TitledBorder(m_res.getString("statisticCounters")));

    m_processedField.setHorizontalAlignment(SwingConstants.RIGHT);
    JPanel b1 = new JPanel(new GridLayout(2, 1));
    b1.setBorder(new EmptyBorder(5, 5, 5, 5));
    b1.add(new JLabel(m_res.getString("processed")));
    b1.add(m_processedField);
    m_processedField.setEnabled(false);
    m_processedField.setBackground(Color.lightGray);

    m_failedField.setHorizontalAlignment(SwingConstants.RIGHT);
    JPanel b2 = new JPanel(new GridLayout(2, 1));
    b2.setBorder(new EmptyBorder(5, 5, 5, 5));
    b2.add(new JLabel(m_res.getString("failed")));
    b2.add(m_failedField);
    m_failedField.setEnabled(false);
    m_failedField.setBackground(Color.lightGray);

    m_pendingField.setHorizontalAlignment(SwingConstants.RIGHT);
    JPanel b3 = new JPanel(new GridLayout(2, 1));
    b3.setBorder(new EmptyBorder(5, 5, 5, 5));
    b3.add(new JLabel(m_res.getString("pending")));
    b3.add(m_pendingField);
    m_pendingField.setEnabled(false);
    m_pendingField.setBackground(Color.lightGray);

    panel.add(b1, "West");
    panel.add(b2, "Center");
    panel.add(b3, "East");

    return panel;
   }

   /**
    * Create the statistic timer panel.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private   JPanel createTimerPanel()
   {
    JPanel panel = new JPanel();
    BoxLayout box = new BoxLayout(panel, BoxLayout.X_AXIS);
    panel.setLayout(box);
    panel.setBorder(new TitledBorder(m_res.getString("statisticTimers")));

    m_minimumField.setHorizontalAlignment(SwingConstants.RIGHT);
    JPanel b1 = new JPanel(new GridLayout(2, 1));
    b1.setBorder(new EmptyBorder(5, 5, 5, 5));
    b1.add(new JLabel(m_res.getString("minimum")));
    b1.add(m_minimumField);
    m_minimumField.setEnabled(false);
    m_minimumField.setBackground(Color.lightGray);

    m_maximumField.setHorizontalAlignment(SwingConstants.RIGHT);
    JPanel b2 = new JPanel(new GridLayout(2, 1));
    b2.setBorder(new EmptyBorder(5, 5, 5, 5));
    b2.add(new JLabel(m_res.getString("maximum")));
    b2.add(m_maximumField);
    m_maximumField.setEnabled(false);
    m_maximumField.setBackground(Color.lightGray);

    m_averageField.setHorizontalAlignment(SwingConstants.RIGHT);
    JPanel b3 = new JPanel(new GridLayout(2, 1));
    b3.setBorder(new EmptyBorder(5, 5, 5, 5));
    b3.add(new JLabel(m_res.getString("average")));
    b3.add(m_averageField);
    m_averageField.setEnabled(false);
    m_averageField.setBackground(Color.lightGray);

    panel.add(b1, "West");
    panel.add(b2, "Center");
    panel.add(b3, "East");

    return panel;
   }

   /**
    * Create the update inerval panel.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private   JPanel createUpdateIntervalPanel()
   {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(new JLabel(m_res.getString("interval")));
    m_intervalField.setHorizontalAlignment(SwingConstants.RIGHT);
    m_intervalField.addActionListener(new ActionListener()
     {
        public void actionPerformed(ActionEvent event)
        {
         Integer newInterval = new Integer(m_intervalField.getText());
        m_refreshTimer.setDelay(newInterval.intValue());
       }
      });

    panel.add(m_intervalField);

    return panel;
   }

   /**
    * Crette the command panel and implement its action listeners.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private   JPanel createCommandPanel() throws Exception
   {
     JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

     m_closeButton.addActionListener(new ActionListener()
     {
        public void actionPerformed(ActionEvent event)
        {
          m_refreshTimer.stop();
         dispose();
       }
      });

      panel.add(m_closeButton);

    return panel;
   }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for window listener
   public void windowActivated(WindowEvent e) {}
   public void windowClosed(WindowEvent e) {}
   public void windowClosing(WindowEvent e)
  {
     this.removeWindowListener(this);
    m_refreshTimer.stop();
  }
   public void windowDeactivated(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowOpened(WindowEvent e) {}

  //////////////////////////////////////////////////////////////////////////////
   /**
    * the parent frame
    */
  private Frame m_parent = null;
   /**
    * the user server remote console
    */
   private ServerConsole m_console= null;
   /**
    * the data source application, specify null to access the server data
    */
  private String m_application = null;
   /**
    * the data source chooser
    */
  //private JComboBox m_serverChooser = new JComboBox();
   /**
    * the uptime display text field
    */
  private JTextField m_uptimeField = new UTFixedTextField("",STATISTIC_FIELD_SIZE);
   /**
    * the number of events processed display text field
    */
  private JTextField m_processedField = new UTFixedTextField("", STATISTIC_FIELD_SIZE);
   /**
    * the number of events failed display text field
    */
  private JTextField m_failedField = new UTFixedTextField("", STATISTIC_FIELD_SIZE);
   /**
    * the number of events pending display text field
    */
  private JTextField m_pendingField = new UTFixedTextField("", STATISTIC_FIELD_SIZE);
   /**
    * the minimal event processing time display text field
    */
  private JTextField m_minimumField = new UTFixedTextField("", STATISTIC_FIELD_SIZE);
   /**
    * the maximal event processing time display text field
    */
  private JTextField m_maximumField = new UTFixedTextField("", STATISTIC_FIELD_SIZE);
   /**
    * the average event processing time display text field
    */
  private JTextField m_averageField = new UTFixedTextField("", STATISTIC_FIELD_SIZE);
   /**
    * the refresh data interval time edit field
    */
  private JTextField m_intervalField = new UTFixedTextField("", STATISTIC_FIELD_SIZE);
   /**
    * the Cancel button
    */
  private JButton m_closeButton = new UTFixedButton(m_res.getString("close"));
   /**
    * the refresh timer
    */
  private Timer m_refreshTimer = null;
   /**
   * the statistic field size
   */
  private final static Dimension STATISTIC_FIELD_SIZE = new Dimension(60, 20);
   /**
   * the dialog size
   */
  private final static Dimension DIALOG_SIZE = new Dimension(400, 350);
   /**
   * the preferred details chooser size
    */
  private static final Dimension DETAILS_CHOOSER_SIZE = new Dimension(200, 20);
   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();
}


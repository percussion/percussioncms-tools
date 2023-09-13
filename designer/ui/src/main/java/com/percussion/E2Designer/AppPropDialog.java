/******************************************************************************
 *
 * [ AppPropDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer;

import com.percussion.E2Designer.browser.BrowserFrame;
import com.percussion.E2Designer.browser.BrowserTree;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSLogger;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;

/**
 * This dialog displays and allows user to set the various basic settings in an
 * application. It contains 4 tabs: Basic, Performance, Request types, and
 * Logging.
 */
public class AppPropDialog extends PSEditorDialog implements ActionListener,
                                                       ChangeListener
{
   private static final long serialVersionUID = 1L;

   /**
     * This constructor is only present to create a new instance from the
     * class name. This is required for the IEditor interface implementation.
     * It should never be called directly by any code.
    **/
    public AppPropDialog()
    {}

   /**
    * Basic constructor.
    * 
    * @param parent The parent frame, may be <code>null</code>.
    * @param app The PSApplication associated to the selected AppFrame.
    * @throws PSServerException
    * @throws PSAuthorizationException
    * @throws PSAuthenticationFailedException
    */
   public AppPropDialog(JInternalFrame parent, PSApplication app) throws
                                     PSServerException,
                                     PSAuthorizationException,
                                     PSAuthenticationFailedException
     {
        super();
      m_parent = parent;
      init( app);
       setVisible(true);
    }

   private void init( PSApplication app)
          throws PSServerException,
               PSAuthorizationException,
               PSAuthenticationFailedException
   {

       PSObjectStore store = E2Designer.getApp().getMainFrame().getObjectStore();

       // saving a copy of server configuration.
      m_config = store.getServerConfiguration();
       m_app = app; // the application associated with this properties dialog.

      //Save the original name. In case, user changes the application name
      //We have to update the browser tree with new name. So to check the tree
      //we need the original name
      m_original = app.getName();

       initUI();

      getContentPane().add(m_dialogPanel);

       setSize(DIALOG_SIZE);
       pack();
       center();
       setResizable(true);
   }

//
// PUBLIC METHODS
//

   public void onOk()
   {
      if (m_tabPane.getSelectedComponent() == null)
         m_selectedTab = m_basicTab;
      else
         m_selectedTab = m_tabPane.getSelectedComponent();

      // save all tabs, not just the selected
      try
      {
         m_saveFlag = true;
         saveBasicInfo();
         savePerformanceInfo();
         saveRequestTypeInfo();
         saveLoggingInfo();

         // update the AppFrame title and Browser Tree with
         // the new Application name
         if(!m_original.equals(m_app.getName()))
         {
            JInternalFrame appFrame = m_parent;

            appFrame.setTitle(m_app.getName());
            if(appFrame instanceof UIAppFrame)
            {
               UIAppFrame app = (UIAppFrame)appFrame;
               app.refreshEditingWindowsTitle(m_app.getName());

               if(!app.isNewApplication())
               {
                  //Refresh tree with new name
                  BrowserTree appTree = BrowserFrame.getBrowser().getAppTree();
                  appTree.refreshAppName(m_original, m_app.getName());
               }

            }
            appFrame.repaint();


         }

         // TODO: should we update all visible PipeFrames associated to
         // this AppFrame?
         // All opened PipeFrame titles will have to update as well.

         dispose();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
  }

/** Implementing ChangeListener interface.  This handles the m_app data saves
  * whenever a new tab has been selected.
*/
  public void stateChanged(ChangeEvent e)
  {
    if (e.getSource() == m_enableUser)
    {
      if (m_enableUser.isSelected()) //&& m_enableUser.isEnabled())
      {
        m_sessionTime.setEnabled(true);
        m_sessionTimeLabel.setEnabled(true);
      }
      else
      {
        m_sessionTime.setEnabled(false);
        m_sessionTimeLabel.setEnabled(false);
      }
    }
    else
    {
      // if no tab has been selected yet
      if (m_tabPane.getSelectedComponent() != null)
      {
        // if new tab selected; m_selectedTab = old tab
        if ((((JTabbedPane)e.getSource()).getSelectedComponent() != m_selectedTab) && m_saveFlag)
        {
          try {
            if (m_selectedTab == m_basicTab)
              saveBasicInfo();
            else if (m_selectedTab == m_performanceTab)
              savePerformanceInfo();
            else if (m_selectedTab == m_requestTypesTab)
              saveRequestTypeInfo();
            else
              saveLoggingInfo();

            m_selectedTab = ((JTabbedPane)e.getSource()).getSelectedComponent();
          }
          catch (Exception exc)
          {
            m_saveFlag = false;
            m_tabPane.setSelectedIndex(0);
            m_saveFlag = true;
            exc.printStackTrace();
          }
        }
      }
      else
      {
        m_selectedTab = m_tabPane.getSelectedComponent();
      }
    }
  }

/** Implementing ActionListener interface.
*/
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals("ok"))
    {
      onOk();
    }
    else if (e.getActionCommand().equals("cancel"))
    {
      onCancel();
    }
    else if (e.getActionCommand().equals("help"))
    {
      onHelp();
    }
    else if (e.getActionCommand().equals("maxThread"))// && (e.getSource() == m_maxThreadBox))
    {
      if (((JCheckBox)e.getSource()).isSelected())
      {
        m_maxThread.setEnabled(false);
        m_maxThreadLabel.setEnabled(false);
      }
      else
      {
        m_maxThread.setEnabled(true);
        m_maxThreadLabel.setEnabled(true);
      }
    }
    else if (e.getActionCommand().equals("maxReqQueue"))// && (e.getSource() == m_maxRequestInQueueBox))
    {
      if (((JCheckBox)e.getSource()).isSelected())
      {
        m_maxReq.setEnabled(false);
        m_maxReqLabel.setEnabled(false);
      }
      else
      {
        m_maxReq.setEnabled(true);
        m_maxReqLabel.setEnabled(true);
      }
    }
    else if (e.getActionCommand().equals("maxReqTime"))// && (e.getSource() == m_maxRequestTimeBox))
    {
      if (((JCheckBox)e.getSource()).isSelected())
      {
        m_reqTime.setEnabled(false);
        m_reqTimeLabel.setEnabled(false);
      }
      else
      {
        m_reqTime.setEnabled(true);
        m_reqTimeLabel.setEnabled(true);
      }
    }
  }

   /**
    * Implements the onEdit method of the IEditor interface.
    */
   public boolean onEdit(UIFigure figure, final Object data)
   {
      if (data instanceof PSApplication)
      {
         try
         {
           init((PSApplication) data);
           m_selectedTab = m_tabPane.getSelectedComponent();
           setVisible(true);
           return true;
         }
         catch (Exception e)
         {
            e.printStackTrace();

            JOptionPane.showMessageDialog(this, MessageFormat.format(E2Designer
               .getResources().getString("ServerConnException"), e
               .getLocalizedMessage()), E2Designer.getResources().getString(
               "ServerConnErr"), JOptionPane.ERROR_MESSAGE);
         }
      }

      return false;
   }



 /**
    * Used to determine which tab is currently visible for Help to display the
    * correct topic
    * 
    * @return The current visible tab pane index.
    */
  public int getVisibleTabIndex()
  {
    return m_tabPane.getSelectedIndex();
  }

   /**
    * Appends a string that corresponds to the active tab type.
   **/
   protected String subclassHelpId( String helpId )
   {
      // NOTE: The order of strings in this array must match the actual tab order
      String [] tabType = { "Basic", "Performance", "RequestTypes", "Logging" };
      return helpId + "_" + tabType[getVisibleTabIndex()];
   }

//
// PRIVATE METHODS
//

/** 
 * Saving the data provided from the basic tab data fields.
 * 
 * @throws Exception If there are any errors. 
 */
  private void saveBasicInfo() throws Exception
  {
    try
    {
      if (-1 != m_appName.getText().indexOf("\\"))
        throw new IllegalArgumentException();

      m_app.setName(m_appName.getText());
      m_app.setRequestRoot(m_appRoot.getText());
      m_app.setDescription(m_appDesc.getText());
    }
    catch (IllegalArgumentException exc)
    {
      JOptionPane.showMessageDialog(this, getResources().getString(
            "basicerror")
            + " " + getResources().getString("appnameerror"), getResources()
            .getString("errordialog"), JOptionPane.ERROR_MESSAGE);

      m_appName.requestFocus();

      throw exc;
    }
  }

/**
 * Saving the data provided from the performance tab data fields.
 */
  private void savePerformanceInfo()
  {
    if (m_maxThreadBox.isSelected())
      m_app.setMaxThreads(-1);
    else
      m_app.setMaxThreads(m_maxThread.getData().intValue());

    if (m_maxRequestTimeBox.isSelected())
      m_app.setMaxRequestTime(-1);
    else
      m_app.setMaxRequestTime(m_reqTime.getData().intValue());

    if (m_maxRequestInQueueBox.isSelected())
      m_app.setMaxRequestsInQueue(-1);
    else
      m_app.setMaxRequestsInQueue(m_maxReq.getData().intValue());

    m_app.setUserSessionEnabled(m_enableUser.isSelected());
    m_app.setUserSessionTimeout(m_sessionTime.getData().intValue());
  }

/** Saving the data provided from the request type tab data fields.
*/
  private void saveRequestTypeInfo()
  {
    try
    {
      m_app.setRequestTypeHtmlParamName(m_html.getText());
      //m_app.setRequestTypeValueQuery(m_query.getText());
      m_app.setRequestTypeValueUpdate(m_update.getText());
      m_app.setRequestTypeValueInsert(m_insert.getText());
      m_app.setRequestTypeValueDelete(m_delete.getText());
    }
    catch (IllegalArgumentException e)
    {
      System.out.println("Problems saving info from Request Type tab: " + e.getLocalizedMessage());
    }
  }

/** Saving the data provided from the logging tab data fields.
*/
  private void saveLoggingInfo()
  {
    if (m_appLogger == null)
    {
      m_appLogger = new PSLogger();
    }

    m_appLogger.setErrorLoggingEnabled(m_error.isSelected());
    m_appLogger.setServerStartStopLoggingEnabled(m_serverUpDown.isSelected());
    m_appLogger.setAppStartStopLoggingEnabled(m_appUpDown.isSelected());
    m_appLogger.setAppStatisticsLoggingEnabled(m_appStatDown.isSelected());
    m_appLogger.setBasicUserActivityLoggingEnabled(m_basicActivity.isSelected());
    m_appLogger.setFullUserActivityLoggingEnabled(m_fullActivity.isSelected());
    m_appLogger.setDetailedUserActivityLoggingEnabled(m_detailActivity.isSelected());
    m_appLogger.setMultipleHandlerLoggingEnabled(m_multi.isSelected());

    m_app.setLogger(m_appLogger);
  }

/** Initialization method for calling all four tab panel creation and putting
  * everything together. */
  private void initUI()
  {
    // creating the 4 tab panels.
    initBasicTab();
    initPerformanceTab();
    initRequestTypeTab();
    initLoggingTab();

      m_tabPane = new JTabbedPane();
      // Note: If the order of tabs is changed, update the array in subclassHelpId
      m_tabPane.addTab(getResources().getString("basic"), m_basicTab);
      setMnemonicForTabIndex(m_tabPane, getResources(), "basic", 0);
      m_tabPane.addTab(getResources().getString("perf"), m_performanceTab);
      setMnemonicForTabIndex(m_tabPane, getResources(), "perf", 1);
      m_tabPane.addTab(getResources().getString("req"), m_requestTypesTab);
      setMnemonicForTabIndex(m_tabPane, getResources(), "req", 2);
      m_tabPane.addTab(getResources().getString("log"), m_loggingTab);
      setMnemonicForTabIndex(m_tabPane, getResources(), "log", 3);
      m_tabPane.addChangeListener(this);

    // setting up buttons
    m_ok = new JButton(getResources().getString("ok"));
    m_ok.setPreferredSize(new Dimension(80, 25));
    m_ok.setActionCommand("ok");
    m_ok.setMnemonic(getResources().getString("ok.mn").charAt(0));
    m_ok.addActionListener(this);

    m_cancel = new JButton(getResources().getString("cancel"));
    m_cancel.setPreferredSize(new Dimension(80, 25));
    m_cancel.setActionCommand("cancel");
    m_cancel.setMnemonic(getResources().getString("cancel.mn").charAt(0));
    m_cancel.addActionListener(this);

    m_help = new JButton(getResources().getString("help"));
    m_help.setPreferredSize(new Dimension(80, 25));
    m_help.setActionCommand("help");
    m_help.setMnemonic(getResources().getString("help.mn").charAt(0));
    m_help.addActionListener(this);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(m_ok);
    buttonPanel.add(Box.createHorizontalStrut(3));
    buttonPanel.add(m_cancel);
    buttonPanel.add(Box.createHorizontalStrut(3));
    buttonPanel.add(m_help);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.setBorder(new EmptyBorder(5,200,5,2));
    JPanel cmdPanel = new JPanel(new BorderLayout());
    cmdPanel.add(buttonPanel, BorderLayout.EAST);
    // putting everything together
    m_dialogPanel = new JPanel(new BorderLayout());
    m_dialogPanel.add(m_tabPane, BorderLayout.CENTER);
    m_dialogPanel.add(cmdPanel, BorderLayout.SOUTH);

    getRootPane().setDefaultButton(m_ok);
  }

/** Initialization method for the Basic tab panel. */
  private void initBasicTab()
  {
    m_basicTab = new JPanel();

    // creating labels
    String labelStr = getResources().getString("appName");
    char mn         = getResources().getString("appName.mn").charAt(0);
    JLabel name = new JLabel(labelStr, SwingConstants.RIGHT);
    name.setDisplayedMnemonic(mn);
    name.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
    name.setPreferredSize(new Dimension(100, 18));

    labelStr    = getResources().getString("appRoot");
    mn          = getResources().getString("appRoot.mn").charAt(0);
    JLabel root = new JLabel(labelStr, SwingConstants.RIGHT);
    root.setDisplayedMnemonic(mn);
    root.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
    
    root.setPreferredSize(new Dimension(100, 18));

    JLabel desc = new JLabel("Description", SwingConstants.LEFT);
    desc.setPreferredSize(new Dimension(420, 18));

    // initializing data fields
    m_appName = new JTextField(m_app.getName(), 20);
    name.setLabelFor(m_appName);
    m_appOriginalName = new String(m_appName.getText());
    m_appName.addKeyListener(new KeyAdapter()
    {
      public void keyReleased(KeyEvent event)
      {
        updateApplicationRoot();
      }
    });
    m_appName.setPreferredSize(new Dimension(200, 20));
    m_appName.setMaximumSize(m_appName.getPreferredSize());

    m_appRoot = new JTextField(m_app.getRequestRoot(), 20);
    root.setLabelFor(m_appRoot);
    m_appRoot.setPreferredSize(new Dimension(200, 20));
    m_appRoot.setMaximumSize(m_appRoot.getPreferredSize());

    m_appDesc = new JTextArea(m_app.getDescription(), 20, 5);
    m_appDesc.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.gray, Color.darkGray));
    m_appDesc.setLineWrap(true);
    m_appDesc.setWrapStyleWord(true);
    m_appDesc.setPreferredSize(new Dimension(300, 200));
    m_appDesc.setMaximumSize(m_appDesc.getPreferredSize());

    // setting up panel layout
    JPanel namePanel = new JPanel();
    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
    namePanel.add(Box.createHorizontalGlue());
    namePanel.add(name);
    namePanel.add(Box.createHorizontalStrut(3));
    namePanel.add(m_appName);
    namePanel.add(Box.createHorizontalGlue());

    JPanel rootPanel = new JPanel();
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));
    rootPanel.add(Box.createHorizontalGlue());
    rootPanel.add(root);
    rootPanel.add(Box.createHorizontalStrut(3));
    rootPanel.add(m_appRoot);
    rootPanel.add(Box.createHorizontalGlue());

    JPanel descPanel = new JPanel();
    descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.X_AXIS));
    descPanel.add(Box.createHorizontalGlue());
    descPanel.add(desc);
    descPanel.add(Box.createHorizontalGlue());

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.add(Box.createVerticalGlue());
    topPanel.add(namePanel);
    topPanel.add(Box.createVerticalStrut(5));
    topPanel.add(rootPanel);
    topPanel.add(Box.createVerticalStrut(5));
    topPanel.add(descPanel);
    topPanel.setBorder(new EmptyBorder(0,0,4,0));

    m_basicTab.setLayout(new BorderLayout());
    m_basicTab.add(topPanel, BorderLayout.NORTH);
    m_basicTab.add(m_appDesc, BorderLayout.CENTER);
    m_basicTab.setBorder(new EmptyBorder(3,3,3,3));
  }

   /**
   * Update the application root while changeing its name. If the root is empty
   * or equal to the name, the root is updated to the same name as the name.
    */
  //////////////////////////////////////////////////////////////////////////////
  private void updateApplicationRoot()
  {
    String appRoot = m_appRoot.getText();
    if (appRoot.equals("") || appRoot.equals(m_appOriginalName))
    {
      m_appRoot.setText(m_appName.getText());
      m_appOriginalName = new String(m_appName.getText());
    }
  }

/** Initialization method for the Performance tab panel. */
  private void initPerformanceTab()
  {
    m_performanceTab = new JPanel();

    // creating labels
    m_maxThreadLabel = new JLabel(getResources().getString("maxthread"), SwingConstants.RIGHT);
    m_maxThreadLabel.setPreferredSize(new Dimension(150, 18));

    m_reqTimeLabel = new JLabel(getResources().getString("reqtime"), SwingConstants.RIGHT);
    m_reqTimeLabel.setPreferredSize(new Dimension(150, 18));

    m_maxReqLabel = new JLabel(getResources().getString("maxreq"), SwingConstants.RIGHT);
    m_maxReqLabel.setPreferredSize(new Dimension(150, 18));

    m_sessionTimeLabel = new JLabel(getResources().getString("sessiontime"), SwingConstants.RIGHT);
    m_sessionTimeLabel.setPreferredSize(new Dimension(150, 18));

    // initializing UTSpinTextFields and JCheckBox

    m_maxThreadBox = new JCheckBox(getResources().getString("maxthreadbox"));
    m_maxThreadBox.setMnemonic(
                getResources().getString("maxthreadbox.mn").charAt(0));    
    m_maxThreadBox.setPreferredSize(new Dimension(170, 20));
    m_maxThreadBox.setVerticalAlignment(SwingConstants.TOP);
    m_maxThreadBox.setActionCommand("maxThread");
    m_maxThreadBox.addActionListener(this);

    m_maxRequestInQueueBox = new JCheckBox(getResources().getString("maxreqbox"));
    m_maxRequestInQueueBox.setMnemonic(
          getResources().getString("maxreqbox.mn").charAt(0)); 
    m_maxRequestInQueueBox.setPreferredSize(new Dimension(170, 20));
    m_maxRequestInQueueBox.setVerticalAlignment(SwingConstants.TOP);
    m_maxRequestInQueueBox.setActionCommand("maxReqQueue");
    m_maxRequestInQueueBox.addActionListener(this);

    m_maxRequestTimeBox = new JCheckBox(getResources().getString("reqtimebox"));
    m_maxRequestTimeBox.setMnemonic(
          getResources().getString("reqtimebox.mn").charAt(0)); 
    m_maxRequestTimeBox.setPreferredSize(new Dimension(170, 20));
    m_maxRequestTimeBox.setVerticalAlignment(SwingConstants.TOP);
    m_maxRequestTimeBox.setActionCommand("maxReqTime");
    m_maxRequestTimeBox.addActionListener(this);

    // setting up Max Thread field and buttons
    m_maxThread = new UTSpinTextField("", new Integer(10),
                                      new Integer(1),
                                      new Integer(m_app.getMaxThreads()));
    // if getMaxThreadPerApp == -1; that means infinite threads allowed.
    if (0 >= m_app.getMaxThreads())
    {
      m_maxThread.setValue(1);
      m_maxThreadBox.setSelected(true);
      m_maxThreadLabel.setEnabled(false);
      m_maxThread.setEnabled(false);
    }
    else
    {
      m_maxThread.setValue(m_app.getMaxThreads());
      m_maxThreadBox.setSelected(false);
      m_maxThreadLabel.setEnabled(true);
      m_maxThread.setEnabled(true);
    }
    
    /*
    // consume in KeyEvent does not work!  an existing Java 1.2 bug...
    m_maxThread.setIntOnlyOption();
    m_maxThread.setDirectEditingAllowed(true);
    */

    // setting up Req time field and buttons

    m_reqTime = new UTSpinTextField("", new Integer(10),
                                    new Integer(1),
                                    new Integer(m_app.getMaxRequestTime()));

    if (0 >= m_app.getMaxRequestTime())     // if not new app
    {
      m_reqTime.setValue(1);
      m_maxRequestTimeBox.setSelected(true);
      m_reqTimeLabel.setEnabled(false);
      m_reqTime.setEnabled(false);
    }
    else
    {
      m_reqTime.setValue(m_app.getMaxRequestTime());
      m_maxRequestTimeBox.setSelected(false);
      m_reqTimeLabel.setEnabled(true);
      m_reqTime.setEnabled(true);
    }
    // setting up max requests field and buttons

    m_maxReq = new UTSpinTextField("", new Integer(10),
                                   new Integer(1),
                                   new Integer(m_app.getMaxRequestsInQueue()));

    if (0 >= m_app.getMaxRequestsInQueue())     // if not new app
    {
      m_maxReq.setValue(1);
      m_maxRequestInQueueBox.setSelected(true);
      m_maxReqLabel.setEnabled(false);
      m_maxReq.setEnabled(false);
    }
    else
    {
      m_maxReq.setValue(m_app.getMaxRequestsInQueue());
      m_maxRequestInQueueBox.setSelected(false);
      m_maxReqLabel.setEnabled(true);
      m_maxReq.setEnabled(true);
    }
    // setting up session timeout field and buttons
    System.out.println("User session timeout: " + m_app.getUserSessionTimeout());
    m_sessionTime = new UTSpinTextField("",
                                        new Integer(10),
                                        new Integer(1),
                                        new Integer(Integer.MAX_VALUE));
    if (0 >= m_app.getUserSessionTimeout())
      m_sessionTime.setValue(1);
    else
      m_sessionTime.setValue(m_app.getUserSessionTimeout());

    // setting up Enable User Check Box
    m_enableUser = new JCheckBox(getResources().getString("enableuser"));
    if (!(m_config.isUserSessionEnabled()))
    {
      m_enableUser.setSelected(false);
      m_enableUser.setEnabled(false);
      m_sessionTime.setEnabled(false);
      m_sessionTimeLabel.setEnabled(false);
    }
    else
    {
      if (m_app.isUserSessionEnabled())
      {
        m_enableUser.setSelected(true);
        m_sessionTime.setEnabled(true);
        m_sessionTimeLabel.setEnabled(true);
      }
      else
      {
        m_enableUser.setSelected(false);
        m_sessionTime.setEnabled(false);
        m_sessionTimeLabel.setEnabled(false);
      }
      m_enableUser.setEnabled(true);
    }
    m_enableUser.setPreferredSize(new Dimension(100, 20));
    m_enableUser.addChangeListener(this);
    // *** add field data later

    // creating small panels
    JPanel maxThreadPanel = new JPanel();
    maxThreadPanel.setLayout(new BoxLayout(maxThreadPanel, BoxLayout.X_AXIS));
    maxThreadPanel.add(Box.createHorizontalGlue());
    maxThreadPanel.add(m_maxThreadLabel);
    maxThreadPanel.add(Box.createHorizontalStrut(2));
    maxThreadPanel.add(m_maxThread);
    maxThreadPanel.add(Box.createHorizontalStrut(2));
    maxThreadPanel.add(m_maxThreadBox);
    maxThreadPanel.add(Box.createHorizontalGlue());

    JPanel reqTimePanel = new JPanel();
    reqTimePanel.setLayout(new BoxLayout(reqTimePanel, BoxLayout.X_AXIS));
    reqTimePanel.add(Box.createHorizontalGlue());
    reqTimePanel.add(m_reqTimeLabel);
    reqTimePanel.add(Box.createHorizontalStrut(2));
    reqTimePanel.add(m_reqTime);
    reqTimePanel.add(Box.createHorizontalStrut(2));
    reqTimePanel.add(m_maxRequestTimeBox);
    reqTimePanel.add(Box.createHorizontalGlue());

    JPanel maxReqPanel = new JPanel();
    maxReqPanel.setLayout(new BoxLayout(maxReqPanel, BoxLayout.X_AXIS));
    maxReqPanel.add(Box.createHorizontalGlue());
    maxReqPanel.add(m_maxReqLabel);
    maxReqPanel.add(Box.createHorizontalStrut(2));
    maxReqPanel.add(m_maxReq);
    maxReqPanel.add(Box.createHorizontalStrut(2));
    maxReqPanel.add(m_maxRequestInQueueBox);
    maxReqPanel.add(Box.createHorizontalGlue());

    // combining top "thread" components
    JPanel threadPanel = new JPanel();
    threadPanel.setLayout(new BoxLayout(threadPanel, BoxLayout.Y_AXIS));
    threadPanel.add(Box.createVerticalGlue());
    threadPanel.add(maxThreadPanel);
    //threadPanel.add(Box.createVerticalStrut(0));
    threadPanel.add(reqTimePanel);
    //threadPanel.add(Box.createVerticalStrut(0));
    threadPanel.add(maxReqPanel);
    threadPanel.add(Box.createVerticalGlue());

    // creating bottom User session components
    JPanel sessionTimePanel = new JPanel();
    sessionTimePanel.setLayout(new BoxLayout(sessionTimePanel, BoxLayout.X_AXIS));
    sessionTimePanel.add(Box.createHorizontalGlue());
    sessionTimePanel.add(m_sessionTimeLabel);
    sessionTimePanel.add(Box.createHorizontalStrut(2));
    sessionTimePanel.add(m_sessionTime);
    sessionTimePanel.add(Box.createHorizontalGlue());

    JPanel userSessionPanel = new JPanel();
    userSessionPanel.setLayout(new BoxLayout(userSessionPanel, BoxLayout.Y_AXIS));
    userSessionPanel.add(Box.createVerticalGlue());
    userSessionPanel.add(m_enableUser);
    userSessionPanel.add(sessionTimePanel);
    userSessionPanel.add(Box.createVerticalGlue());
    userSessionPanel.setBorder(new TitledBorder(
                                   new EtchedBorder(EtchedBorder.LOWERED),
                                   getResources().getString("user")
                               ));

    m_performanceTab.setLayout(new BoxLayout(m_performanceTab, BoxLayout.Y_AXIS));
    m_performanceTab.add(Box.createVerticalGlue());
    m_performanceTab.add(threadPanel);
    m_performanceTab.add(Box.createVerticalStrut(5));
    m_performanceTab.add(userSessionPanel);
   userSessionPanel.setVisible(false);
    m_performanceTab.add(Box.createVerticalGlue());
    m_performanceTab.setBorder(new EmptyBorder(3,3,3,3));
  }

/** Initialization method for the Request Type tab panel. */
  private void initRequestTypeTab()
  {
    m_requestTypesTab = new JPanel();

    // initializing data fields
    if (m_app.getRequestTypeHtmlParamName().equals(""))
      m_html = new JTextField( E2Designer.getResources().getString( "DefaultActionField" ), 20);
    else
      m_html = new JTextField(m_app.getRequestTypeHtmlParamName(), 20);
    m_html.setPreferredSize(new Dimension(150, 20));
    m_html.setMaximumSize(m_html.getPreferredSize());

/*   /////// Query is not supported for V1 ////////
    if (m_app.getRequestTypeValueQuery().equals(""))
      m_query = new JTextField( E2Designer.getResources().getString( "DefaultInsertActionName" ), 20);
    else
      m_query = new JTextField(m_app.getRequestTypeValueQuery(), 20);
    m_query.setPreferredSize(new Dimension(80, 20));
    m_query.setMaximumSize(m_query.getPreferredSize());*/

    if (m_app.getRequestTypeValueUpdate().equals(""))
      m_update = new JTextField( E2Designer.getResources().getString( "DefaultUpdateActionName" ), 20);
    else
      m_update = new JTextField(m_app.getRequestTypeValueUpdate(), 20);
    m_update.setPreferredSize(new Dimension(80, 20));
    m_update.setMaximumSize(m_update.getPreferredSize());

    if (m_app.getRequestTypeValueInsert().equals(""))
      m_insert = new JTextField(E2Designer.getResources().getString( "DefaultInsertActionName" ), 20);
    else
      m_insert = new JTextField(m_app.getRequestTypeValueInsert(), 20);
    m_insert.setPreferredSize(new Dimension(80, 20));
    m_insert.setMaximumSize(m_insert.getPreferredSize());

    if (m_app.getRequestTypeValueDelete().equals(""))
      m_delete = new JTextField( E2Designer.getResources().getString( "DefaultDeleteActionName" ), 20);
    else
      m_delete = new JTextField(m_app.getRequestTypeValueDelete(), 20);
    m_delete.setPreferredSize(new Dimension(80, 20));
    m_delete.setMaximumSize(m_delete.getPreferredSize());
    
    // creating labels
    String labelStr    = getResources().getString("query");
    char   mn          = getResources().getString("query.mn").charAt(0);
    JLabel query = new JLabel(labelStr, SwingConstants.LEFT);
    query.setDisplayedMnemonic(mn);
    query.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
    query.setPreferredSize(new Dimension(80, 18));

    labelStr    = getResources().getString("update");
    mn          = getResources().getString("update.mn").charAt(0);
    JLabel update = new JLabel(labelStr, SwingConstants.LEFT);
    update.setDisplayedMnemonic(mn);
    update.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
    update.setPreferredSize(new Dimension(80, 18));

    labelStr    = getResources().getString("insert");
    mn          = getResources().getString("insert.mn").charAt(0);
    JLabel insert = new JLabel(labelStr, SwingConstants.LEFT);
    insert.setDisplayedMnemonic(mn);
    insert.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
    insert.setPreferredSize(new Dimension(80, 18));
    
    labelStr    = getResources().getString("delete");
    mn          = getResources().getString("delete.mn").charAt(0);
    JLabel delete = new JLabel(labelStr, SwingConstants.LEFT);
    delete.setDisplayedMnemonic(mn);
    delete.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
    delete.setPreferredSize(new Dimension(80, 18));

    
    JLabel values = new JLabel(getResources().getString("values"), SwingConstants.LEFT);
    values.setPreferredSize(new Dimension(170, 18));

    labelStr    = getResources().getString("html");
    mn          = getResources().getString("html.mn").charAt(0);
    JLabel html = new JLabel(labelStr);
    html.setDisplayedMnemonic(mn);
    html.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
    html.setPreferredSize(new Dimension(150, 18));

    // putting the left side together (HTML Parameter name)
    JPanel htmlPanel = new JPanel();
    htmlPanel.setLayout(new BoxLayout(htmlPanel, BoxLayout.Y_AXIS));
    htmlPanel.add(Box.createVerticalGlue());
    htmlPanel.add(html);
    htmlPanel.add(Box.createVerticalStrut(1));
    htmlPanel.add(m_html);
    htmlPanel.add(Box.createVerticalGlue());
    htmlPanel.setBorder(new EmptyBorder(0,4,0,0));

    // set labels for...
    update.setLabelFor(m_update);
    insert.setLabelFor(m_insert);
    delete.setLabelFor(m_delete);
    html.setLabelFor(m_html);
    // putting the right side together (all the Values)

/*   /////// Query is not supported for V1 ////////
    JPanel queryPanel = new JPanel();
    queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.X_AXIS));
    queryPanel.add(Box.createHorizontalGlue());
    queryPanel.add(m_query);
    //queryPanel.add(Box.createHorizontalGlue());
    queryPanel.add(Box.createHorizontalStrut(3));
    queryPanel.add(query);
    queryPanel.add(Box.createHorizontalGlue());
    //queryPanel.setPreferredSize(new Dimension(170, 20));*/

    JPanel updatePanel = new JPanel();
    updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.X_AXIS));
    updatePanel.add(Box.createHorizontalGlue());
    updatePanel.add(m_update);
    //updatePanel.add(Box.createHorizontalGlue());
    updatePanel.add(Box.createHorizontalStrut(3));
    updatePanel.add(update);
    updatePanel.add(Box.createHorizontalGlue());
    //updatePanel.setPreferredSize(new Dimension(170, 20));

    JPanel insertPanel = new JPanel();
    insertPanel.setLayout(new BoxLayout(insertPanel, BoxLayout.X_AXIS));
    insertPanel.add(Box.createHorizontalGlue());
    insertPanel.add(m_insert);
    //insertPanel.add(Box.createHorizontalGlue());
    insertPanel.add(Box.createHorizontalStrut(3));
    insertPanel.add(insert);
    insertPanel.add(Box.createHorizontalGlue());
    //insertPanel.setPreferredSize(new Dimension(170, 20));

    JPanel deletePanel = new JPanel();
    deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.X_AXIS));
    deletePanel.add(Box.createHorizontalGlue());
    deletePanel.add(m_delete);
    //deletePanel.add(Box.createHorizontalGlue());
    deletePanel.add(Box.createHorizontalStrut(3));
    deletePanel.add(delete);
    deletePanel.add(Box.createHorizontalGlue());
    //deletePanel.setPreferredSize(new Dimension(170, 20));

    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.X_AXIS));
    valuePanel.add(Box.createHorizontalGlue());
    valuePanel.add(values);
    valuePanel.add(Box.createHorizontalGlue());

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    rightPanel.add(Box.createVerticalGlue());
    rightPanel.add(valuePanel);
/*   /////// Query is not supported for V1 ////////
    rightPanel.add(Box.createVerticalStrut(1));
    rightPanel.add(queryPanel);   */
    rightPanel.add(Box.createVerticalStrut(3));
    rightPanel.add(updatePanel);
    rightPanel.add(Box.createVerticalStrut(3));
    rightPanel.add(insertPanel);
    rightPanel.add(Box.createVerticalStrut(3));
    rightPanel.add(deletePanel);
    rightPanel.add(Box.createVerticalGlue());

    m_requestTypesTab = new JPanel();
    m_requestTypesTab.setLayout(new BoxLayout(m_requestTypesTab, BoxLayout.X_AXIS));
    //m_requestTypesTab.add(Box.createHorizontalGlue());
    m_requestTypesTab.add(htmlPanel);
    m_requestTypesTab.add(Box.createHorizontalStrut(6));
    m_requestTypesTab.add(rightPanel);
    //m_requestTypesTab.add(Box.createHorizontalGlue());
    m_requestTypesTab.setBorder(new EmptyBorder(3,3,3,3));
  }

/** Initialization method for the Logging tab panel. */
  private void initLoggingTab()
  {
    m_loggingTab = new JPanel();

    // initializing data fields
    m_error = new JCheckBox(getResources().getString("error"));
    m_error.setMnemonic(getResources().getString("error.mn").charAt(0));
    m_serverUpDown = new JCheckBox(getResources().getString("serverupdown"));
    m_serverUpDown.setMnemonic(
                      getResources().getString("serverupdown.mn").charAt(0));
    m_appUpDown = new JCheckBox(getResources().getString("appupdown"));
    m_appUpDown.setMnemonic(getResources().getString("appupdown.mn").charAt(0));
    m_appStatDown = new JCheckBox(getResources().getString("appstatdown"));
    m_appStatDown.setMnemonic(
                         getResources().getString("appstatdown.mn").charAt(0));
    m_basicActivity = new JCheckBox(getResources().getString("useractivity"));
    m_basicActivity.setMnemonic(
                         getResources().getString("useractivity.mn").charAt(0));
    m_fullActivity = new JCheckBox(getResources().getString("fullactivity"));
    m_fullActivity.setMnemonic(
                         getResources().getString("fullactivity.mn").charAt(0));
    m_detailActivity = new JCheckBox(getResources().getString("detail"));
    m_detailActivity.setMnemonic(
                               getResources().getString("detail.mn").charAt(0));
    m_multi = new JCheckBox(getResources().getString("multi"));
    m_multi.setMnemonic(getResources().getString("multi.mn").charAt(0));

    // *** get server config. log data here, then set into checkboxes...
    Object[] checkBoxArray = new Object[MAX_BOXES];
    checkBoxArray[0] = m_error;
    checkBoxArray[1] = m_serverUpDown;
    checkBoxArray[2] = m_appUpDown;
    checkBoxArray[3] = m_appStatDown;
    checkBoxArray[4] = m_basicActivity;
    checkBoxArray[5] = m_fullActivity;
    checkBoxArray[6] = m_detailActivity;
    checkBoxArray[7] = m_multi;

    m_appLogger = m_app.getLogger();
    m_serverLogger = m_config.getLogger();

    // ***** temporary implementation for Log actions... *****
    boolean[] appLogArray = new boolean[MAX_BOXES];

    // if this is a new application, app logger will be null.  So all the Log
    // checkboxes will not be checked.  If this is not a new application,
    // check the appropriate check boxes.
    if (m_appLogger != null)
    {
      appLogArray[0] = m_appLogger.isErrorLoggingEnabled();
      appLogArray[1] = m_appLogger.isServerStartStopLoggingEnabled();
      appLogArray[2] = m_appLogger.isAppStartStopLoggingEnabled();
      appLogArray[3] = m_appLogger.isAppStatisticsLoggingEnabled();
      appLogArray[4] = m_appLogger.isBasicUserActivityLoggingEnabled();
      appLogArray[5] = m_appLogger.isFullUserActivityLoggingEnabled();
      appLogArray[6] = m_appLogger.isDetailedUserActivityLoggingEnabled();
      appLogArray[7] = m_appLogger.isMultipleHandlerLoggingEnabled();
    }
    else
    {
      for (int i = 0; i < appLogArray.length; i++)
        appLogArray[i] = false;
    }

    boolean[] serverLogArray = new boolean[MAX_BOXES];
    serverLogArray[0] = m_serverLogger.isErrorLoggingEnabled();
    serverLogArray[1] = m_serverLogger.isServerStartStopLoggingEnabled();
    serverLogArray[2] = m_serverLogger.isAppStartStopLoggingEnabled();
    serverLogArray[3] = m_serverLogger.isAppStatisticsLoggingEnabled();
    serverLogArray[4] = m_serverLogger.isBasicUserActivityLoggingEnabled();
    serverLogArray[5] = m_serverLogger.isFullUserActivityLoggingEnabled();
    serverLogArray[6] = m_serverLogger.isDetailedUserActivityLoggingEnabled();
    serverLogArray[7] = m_serverLogger.isMultipleHandlerLoggingEnabled();


    for (int i = 0; i < serverLogArray.length; i++)
    {
      if (serverLogArray[i] == true)
      {
        ((JCheckBox)checkBoxArray[i]).setSelected(true);
        ((JCheckBox)checkBoxArray[i]).setEnabled(false);
      }
      else
      {
        if (appLogArray[i] == true)
          ((JCheckBox)checkBoxArray[i]).setSelected(true);
      }
    }
    // ***** finished temporary implementation... *****

    // creating tab panel
    m_loggingTab = new JPanel();
    m_loggingTab.setLayout(new BoxLayout(m_loggingTab, BoxLayout.Y_AXIS));
    //m_loggingTab.add(Box.createVerticalGlue());
    m_loggingTab.add(m_error);
    m_loggingTab.add(Box.createVerticalStrut(0));
    m_loggingTab.add(m_serverUpDown);
    m_loggingTab.add(Box.createVerticalStrut(0));
    m_loggingTab.add(m_appUpDown);
    m_loggingTab.add(Box.createVerticalStrut(0));
    m_loggingTab.add(m_appStatDown);
    m_loggingTab.add(Box.createVerticalStrut(0));
    m_loggingTab.add(m_basicActivity);
    m_loggingTab.add(Box.createVerticalStrut(0));
    m_loggingTab.add(m_fullActivity);
    m_loggingTab.add(Box.createVerticalStrut(0));
    m_loggingTab.add(m_detailActivity);
    m_loggingTab.add(Box.createVerticalStrut(0));
    m_loggingTab.add(m_multi);
    //m_loggingTab.add(Box.createVerticalGlue());
    m_loggingTab.setBorder(new CompoundBorder(
                             new EmptyBorder(3,3,3,3),
                             new CompoundBorder(
                               new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                              getResources().getString("logact")),
                               new EmptyBorder(0,3,3,0)
                             )
                            )
                           );

  }

//
// MEMBER VARIABLES
//

   //private PSDesignerConnection m_conn = null;
   private PSServerConfiguration m_config = null;
   private PSApplication         m_app    = null;
   private PSLogger              m_serverLogger = null;
   private PSLogger              m_appLogger    = null;

   private JPanel m_basicTab, m_performanceTab, m_requestTypesTab, m_loggingTab,
                 m_dialogPanel;

   private JTabbedPane m_tabPane;

   private JLabel m_sessionTimeLabel, m_maxThreadLabel, m_maxReqLabel,
                 m_reqTimeLabel;

   private JButton m_ok, m_cancel, m_help;

   private String m_appOriginalName = null;
   private JTextField m_appName, m_appRoot, m_html, m_update, m_insert,
                     m_delete;

   private JCheckBox m_enableUser, m_error, m_serverUpDown, m_appUpDown,
                    m_appStatDown, m_basicActivity, m_detailActivity,
                    m_multi, m_maxThreadBox, m_maxRequestInQueueBox,
                    m_maxRequestTimeBox, m_fullActivity;

   private JInternalFrame m_parent;
   private UTSpinTextField m_maxThread, m_reqTime, m_maxReq, m_sessionTime;

   private JTextArea m_appDesc;
   private Component m_selectedTab;
   private boolean   m_saveFlag = true;

   private static final int MAX_BOXES = 8;
   private static final Dimension DIALOG_SIZE = new Dimension(500, 325);
   /*
   * original application name
   */
   private String m_original = null;
}



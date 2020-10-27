/******************************************************************************
 *
 * [ AppletMainDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.SecurityProviderMetaData;
import com.percussion.E2Designer.Util;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.util.PSProperties;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * After a successful log-on to an E2 server, this class provides the main
 * container for the server admin remote console.
 */
public class AppletMainDialog extends JFrame
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Construct a tabbed dialog as the main dialog for an E2 server admin remote
    * console.
    *
    * @param title the dialog title to be displayed
    * @param connection a valid E2 server connection
    * @param applet the connection type, true for applet, false for application
    * @throws PSServerException 
    * @throws PSIllegalArgumentException 
    * @throws PSLockedException 
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationFailedException 
    * @throws UserCancelledException 
    */
   public AppletMainDialog(String title, ServerConnection connection,
      boolean applet) throws PSServerException, PSIllegalArgumentException,
      PSLockedException, PSAuthorizationException,
      PSAuthenticationFailedException, UserCancelledException
   {
      super(title);
      ms_connection = connection;
      ms_objectStore = new PSObjectStore( ms_connection.getConnection() );

      // initialize the security cataloging stuff
      m_singletons.add(
         SecurityProviderMetaData.initialize( getServerConnection()));
      m_singletons.add( FeatureSet.createFeatureSet(ms_objectStore) );

      initializeProperties(applet);
      
      /*
       * reset ok flag in case launched as an applet and logging backin after OK
       * was pressed
       */
      ms_isOk = false;
      
      /* Keeps the singleton instance of help class to load the map or helpset
       * files once only.
       */
      String helpSetURL = null;
      PSJavaHelp help = PSJavaHelp.getInstance();
      if(applet)
         helpSetURL = PSServerAdminApplet.m_helpSetURL;
      else
      {
         String helpFile = ms_props.getProperty(
            PSServerAdminApplet.HELPSETFILE);
         //Attach the protocol based on the file location.
         helpSetURL = PSJavaHelp.getHelpSetURL(helpFile);
      }

      if(helpSetURL != null && helpSetURL.trim().length() != 0)
      {
         help.setHelpSet( helpSetURL,
            "com.percussion.E2Designer.admin.helptopicmapping" );
      }
      else
      {
         System.out.println(
            "HelpSet URL is not available. So help is not activated.");
         PSDlgUtil.showErrorDialog(
            E2Designer.getResources().getString( "noHelpSet" ),
            E2Designer.getResources().getString( "invalidHelpSetTitle" ));
      }
      
      m_singletons.add( help );

      m_timerListener = new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if(e.getSource() == m_timer)
            {
               m_timer.stop();
            }
            else if(e.getSource() == m_lockTimer)
            {
               extendServerConfigurationLock();
               m_lockTimer.restart();
            }
         }
      };
      
      m_timer = new Timer(2000, m_timerListener);

      initDialog();
      
      initLockTimer(m_serverConfiguration.getUserSessionTimeout());

      this.setResizable(true);
      ms_mainframe = this;

      ImageIcon icon = new ImageIcon(getClass().getResource(
         m_res.getString("gif_main")));
      
      if (null != icon)
         setIconImage( icon.getImage( ) );
   }

   /**
    * Creates or updates the lock timer so that the lock timeout is either the
    * {@link #LOCK_EXTEND_MINS} or the current session timeout, which ever is 
    * shorter.
    * 
    * @param sessionTimeout The current user session timeout in mins. 
    */
   private void initLockTimer(int sessionTimeout)
   {
      // get session timeout in seconds, minus 5 minutes if possible
      int longBuffer = 5*60;
      int shortBuffer = 15;
      if (sessionTimeout > longBuffer)
         sessionTimeout -= longBuffer;
      else
      {
         if (sessionTimeout > shortBuffer)
            sessionTimeout -= shortBuffer;
      }
      
      // locktimeout in seconds
      int lockTimeout = LOCK_EXTEND_MINS * 60;
      
      if (lockTimeout > sessionTimeout)
         lockTimeout = sessionTimeout;
      
      if (m_lockTimer == null)
      {
         // timer delay in in milliseconds
         m_lockTimer = new Timer(lockTimeout * 1000, m_timerListener);
         m_lockTimer.start();
      }
      else
      {
         int curDelay = m_lockTimer.getDelay();
         if (curDelay != lockTimeout)
         {
            m_lockTimer.stop();
            m_lockTimer.setDelay(lockTimeout * 1000);
            m_lockTimer.setInitialDelay(lockTimeout * 1000);
            m_lockTimer.start();
         }
      }
   }

   /**
    * Return the preferred size.
    *
    * @return Dimension the preferred size
    */
   @Override
   public Dimension getPreferredSize()
   {
      return DIALOG_SIZE;
   }

   /**
    * Return the minimum size.
    *
    * @return Dimension the minimum size
    */
   @Override
   public Dimension getMinimumSize()
   {
      return DIALOG_SIZE;
   }

   /**
    * Return the maximum size.
    *
    * @return Dimension the maximum size
    */
   @Override
   public Dimension getMaximumSize()
   {
      return DIALOG_SIZE;
   }

   /**
    * All components contained by this frame were moved to the top left corner
    * of the screed after an iconify/deiconify sequence (running as applet
    * only).
    * To fix this we expand  the frame to the full screen size and then size
    * it back to the original size.
    *
    * @param event the window event
    */
   @Override
   public void processWindowEvent(WindowEvent event)
   {
      if (event.getID() == WindowEvent.WINDOW_DEICONIFIED)
      {
         // store the current size, maximize it to the screen size and the
         // resize it back to the stored current size.
         this.setVisible(false);
         Dimension dim = this.getSize();
         this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
         this.setSize(dim);
         this.setVisible(true);
      }

      super.processWindowEvent(event);
   }

  /**
   * Initialize the admin properties only if this was started as an
   * application. Applets do not have access to any files on the server until
   * it is connected and accesed through an object store handler.
   *
   * @param applet the connection type, true for applet, false for application
   */
  private static void initializeProperties(boolean applet)
  {
    PSProperties props = null;

    if (!applet)
    {
      try
      {
         File file = PSProperties.getConfig(LoginPanel.ENTRY_NAME,
         LoginPanel.PROPERTIES_FILENAME, LoginPanel.ADMIN_DIR);

         props = new PSProperties (file.getAbsolutePath());

      }
      catch ( FileNotFoundException e )
      {
         // ignore, but log msg
         System.out.println( "Couldn't find properties: " + 
            e.getLocalizedMessage());
      }
      catch ( IOException ioe )
      {
         // ignore, but log msg
         System.out.println( "Couldn't load properties: " + 
            ioe.getLocalizedMessage());
      }
      if ( null != props )
         ms_props = props;
      else
      {
         ms_props = new PSProperties();
      }
    }
  }

   /**
   * Renews the lock for the application every LOCK_EXTEND_MINS minutes
   */
   public void extendServerConfigurationLock()
   {
      if(m_serverConfiguration != null)
      {
         try
         {
            PSObjectStore os = ms_objectStore;
            os.extendServerConfigurationLock(
               m_serverConfiguration.getServerConfiguration(), 
               LOCK_EXTEND_MINS);
         }
         catch (PSServerException e)
         {
            JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
               E2Designer.getResources().getString("ServerErr"),
               JOptionPane.ERROR_MESSAGE);
         }
         catch (PSAuthorizationException e)
         {
            JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
               E2Designer.getResources().getString("AuthErr"),
               JOptionPane.ERROR_MESSAGE);
         }
         catch (PSAuthenticationFailedException e)
         {
            JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
               E2Designer.getResources().getString("AuthErr"),
               JOptionPane.ERROR_MESSAGE);
         }
         catch (PSLockedException e)
         {
            JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
               E2Designer.getResources().getString("AuthErr"),
               JOptionPane.ERROR_MESSAGE);
         }
      }
   }

/**
 * @return JButton A reference to the apply button.
 */
  public JButton getApplyButton()
  {
   return m_buttonApply;
  }

   /**
    * Returns the flag that indicates that the OK button was pressed.
    * 
    * @return <code>true</code> if OK was pressed, otherwise
    *         <code>false</code>
    */
   public static boolean getOkFlag()
   {
      return ms_isOk;
   }

   /**
    * Initialize the dialog with all GUI elements and data.
    * @throws PSLockedException 
    * @throws PSServerException 
    * @throws PSIllegalArgumentException 
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationFailedException 
    * @throws UserCancelledException 
    */
   void initDialog( )
      throws PSLockedException, PSServerException,
                   PSIllegalArgumentException, PSAuthorizationException,
                   PSAuthenticationFailedException, UserCancelledException
   {
      // get servers remote console
      m_console = new ServerConsole(ms_connection);
      String locker = "";

      try
      {
         loadData(true, false);
      }
      catch ( PSLockedException e )
      {
         try
         {
            int option = JOptionPane.NO_OPTION;
            int errCode = e.getErrorCode();
            if ((errCode == IPSObjectStoreErrors.LOCK_ALREADY_HELD) ||
               (errCode == IPSObjectStoreErrors.LOCK_ALREADY_HELD_SAME_USER))
            {
               JTextArea textBox = new JTextArea();
               textBox.setWrapStyleWord( true );
               textBox.setLineWrap( true );
               textBox.setMaximumSize(new Dimension(2000, 2000));
               textBox.setEditable( false );
               /* don't set the preferred size of the textBox as that will 
                * disabled the scroll pane's scroll bar from working. Instead,
                * set the preferred size on the scroll pane.
                */
               JScrollPane pane = new JScrollPane( textBox );

               if (errCode == IPSObjectStoreErrors.LOCK_ALREADY_HELD)
               {
                  // lock is currently held by some other user
                  // show message that the admin client will open in readonly
                  // mode if the user presses OK. If user presses CANCEL, the
                  // admin client will not open.
                  locker = "";
                  String mins = "";
                  Object[] errArgs = e.getErrorArguments();
                  if ((errArgs != null) && (errArgs.length > 1) &&
                     (errArgs[1] != null))
                  {
                     locker = errArgs[1].toString();
                  }
                  if ((errArgs != null) && (errArgs.length > 2) &&
                     (errArgs[2] != null))
                  {
                     mins = errArgs[2].toString();
                  }

                  MessageFormat titleFromat = new MessageFormat(
                     m_res.getString("readOnlyTitleFormat"));
                  String [] titleParams = {"", locker};
                  String msgTitle = titleFromat.format(titleParams);

                  MessageFormat msgFromat = new MessageFormat(
                     m_res.getString("LockAlreadyHeld"));
                  String [] msgParams = {locker, mins};
                  String msgText = msgFromat.format(msgParams);

                  textBox.setText(msgText);
                  pane.setPreferredSize(new Dimension(300, 150));

                  option = JOptionPane.showConfirmDialog(this, pane, msgTitle,
                     JOptionPane.OK_CANCEL_OPTION );

                  if (option == JOptionPane.OK_OPTION)
                  {
                     // if the user presses OK button then the admin client
                     // should open in read only mode
                     option = JOptionPane.NO_OPTION;
                  }
               }
               else if (errCode ==
                  IPSObjectStoreErrors.LOCK_ALREADY_HELD_SAME_USER)
               {
                  textBox.setText(m_res.getString("OverrideLock"));
                  
                  /* Why change the height from 150 to 170?
                   * The newer version of the JRE (1.6 vs 1.5) modified how they
                   * render text slightly. Now instead of a 1 pixel border 
                   * between the left edge of the text area and the left edge
                   * of the text, there are 2 pixels. This was enough to change
                   * the wrapping and cause the text to render beyond the bottom
                   * of the visible area. Due to another bug, this text could
                   * never been seen.
                   */
                  pane.setPreferredSize(new Dimension(400, 170));

                  option = JOptionPane.showConfirmDialog(this, pane,
                  m_res.getString( "OverrideLockTitle" ),
                  JOptionPane.YES_NO_CANCEL_OPTION );
            }
            }
            switch ( option )
            {
               case JOptionPane.YES_OPTION:
               {
                  // override the lock
                  loadData(true, true);
                  break;
               }
               case JOptionPane.NO_OPTION:
               {
                  // open it read only
                  loadData(false, false);
                  locker = "";
                  Object[] errArgs = e.getErrorArguments();
                  if ((errArgs != null) && (errArgs.length > 1) &&
                     (errArgs[1] != null))
                  {
                     locker = errArgs[1].toString();
                  }

                  break;
               }
               case JOptionPane.CANCEL_OPTION:
               {
                  // user decided not to open it
                  throw new UserCancelledException();
               }
            }
         }
         catch ( PSLockedException ex )
         {
            /* 
             * This can only rarely happen. If lock expires and someone else 
             * grabbed it between the time the dialog appeared and the user 
             * said yes, we would get this. 
             */
            JTextArea textBox = new JTextArea(
               m_res.getString("OverrideLockFailed"));
            textBox.setWrapStyleWord( true );
            textBox.setLineWrap( true );
            textBox.setPreferredSize( new Dimension( 250, 150 ));
            // arbitrary huge size
            textBox.setMaximumSize( new Dimension( 2000, 2000 ));
            textBox.setEditable( false );
            JScrollPane pane = new JScrollPane( textBox );
            JOptionPane.showMessageDialog( this, pane,
               m_res.getString( "OverrideLockFailedTitle" ),
               JOptionPane.WARNING_MESSAGE );
            throw ex;
         }
      }

      // create and add the tabs to tabbed pane
      m_tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      int tabIx = 0;
      m_tabbedPane.addTab(m_res.getString("status"), 
            new StatusPropertyPanel(this, this, m_console));
      setMnemonicForTabIndex("status", tabIx++);
      
      m_tabbedPane.addTab(m_res.getString("settings"), 
            new SettingsPropertyPanel(this, m_serverConfiguration));
      setMnemonicForTabIndex("settings", tabIx++);

      m_tabbedPane.addTab(m_res.getString("datasources"), 
         new DatasourceConfigurationPanel(this, m_serverConfiguration, 
            m_jndiDatasources, m_datasourceResolver, m_hibernateDialects));
      setMnemonicForTabIndex("datasources", tabIx++);

      
      m_tabbedPane.addTab(m_res.getString("logging"), 
            new LoggingPropertyPanel(this, m_console, m_serverConfiguration));
      setMnemonicForTabIndex("logging", tabIx++);
      if (isDirectoryServicesSupported())
      {
         DirectoryServiceData data = new DirectoryServiceData(
            m_serverConfiguration.getServerConfiguration());
         data.setCatalogerConfigs(m_catalogerConfigs);
         m_tabbedPane.addTab(m_res.getString("dir"), 
            new DirectoryServicePanel(this, data, m_serverConfiguration));
         setMnemonicForTabIndex("dir", tabIx++);
      }
      m_tabbedPane.addTab(m_res.getString("security"), 
            new SecurityPropertyPanel(this, m_serverConfiguration, 
               m_roleConfiguration));
      setMnemonicForTabIndex("security", tabIx++);
      
      if (FeatureSet.isFeatureSupported(FeatureSet.FEATURE_FTS))
      {         
         m_tabbedPane.addTab(m_res.getString("search"), 
               new SearchPropertyPanel(this, m_serverConfiguration)); 
         setMnemonicForTabIndex("search", tabIx++);
      }
      m_tabbedPane.addTab(m_res.getString("monitor"), 
            new MonitorPropertyPanel(this, m_console));
      setMnemonicForTabIndex("monitor", tabIx++);
      
      if (FeatureSet.isFeatureSupported(E2Designer.CX_FEATURE, 1))
         m_tabbedPane.addTab( m_res.getString("contentExplorer"),
            new ContentExplorerPanel( this, m_serverConfiguration));
      setMnemonicForTabIndex("contentExplorer", tabIx++);
      m_tabbedPane.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            onTabChanged(e);
         }
      });

      addWindowListener(new WindowAdapter()
      {
         @Override
         public void windowClosing(@SuppressWarnings("unused") WindowEvent e)
         {
            onClose();
            return;
         }
      });

      // add tabbed pane to dialog
      getContentPane().add(m_tabbedPane, "Center");
      getContentPane().add(createCommandPanel(), "South");
      getRootPane().setDefaultButton(m_buttonOk);
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      if ( m_serverConfiguration.isReadOnly())
      {
         MessageFormat form = new MessageFormat(
            m_res.getString("readOnlyTitleFormat"));
         String [] params =
         {
            getTitle(),
            locker
         };
         makeReadOnly(form.format(params));
      }
   }

   /**
     * @param resId the resource id from the bundle
     * @param tabIx is the tab index on which a mnemonic has to be set
     */
    private void setMnemonicForTabIndex(String resId, int tabIx)
    {
        char mnemonic;
        String tabName = m_res.getString(resId);
        mnemonic = m_res.getString(resId+".tab.mn").charAt(0);
        int ix = tabName.indexOf(mnemonic);
        char upperMnemonic = (""+mnemonic).toUpperCase().charAt(0);
        m_tabbedPane.setMnemonicAt(tabIx, upperMnemonic);
        m_tabbedPane.setDisplayedMnemonicIndexAt(tabIx, ix);
    }

/**
    * Switches the admin client to read only mode by disabling the apply and
    * ok buttons.
    *
    * @param title the title of the admin client dialog, assumed not
    * <code>null</code> and non-emppty
    */
   private void makeReadOnly(String title)
   {
      setTitle(title);
         m_buttonOk.setEnabled(false);
         m_buttonApply.setEnabled(false);
      }

   /**
    * Get the first <code>ITabDataHelper</code> object found starting with
    * the currently selected tab component.
    * 
    * @return the first <code>ITabDataHelper</code> object found, 
    *    <code>null</code> if none is found.
    */
   private ITabDataHelper getDataHelperPanel()
   {
      Component tab = m_tabbedPane.getSelectedComponent();
      if (tab instanceof ITabDataHelper)
         return (ITabDataHelper) tab;
      else if (tab instanceof ITabPaneRetriever)
      {
         JTabbedPane pain = ((ITabPaneRetriever) tab).getTabbedPane();
         Component innerTab = pain.getSelectedComponent();
         if (innerTab instanceof ITabDataHelper)
         {
            return (ITabDataHelper) innerTab;
         }
         
         return null;
      }
      else
         return null;
   }

   /**
    * Saves role configuration if the 'roles' inner tab in 'security' tab is not
    * yet saved.
    */
   private void saveRoleConfiguration()
   {
      Component comp = m_tabbedPane.getComponentAt(
         m_tabbedPane.indexOfTab(m_res.getString("security")) );
      if(comp instanceof ITabPaneRetriever)
      {
         JTabbedPane innerTabPane = ((ITabPaneRetriever)comp).getTabbedPane();
         comp = innerTabPane.getComponentAt(
            innerTabPane.indexOfTab(m_res.getString("roles")) );
         if(comp instanceof ITabDataHelper)
         {
            ((ITabDataHelper)comp).saveTabData();
         }
      }
   }


   /** 
    * The procedure performed by switching the top tabs.
    * 
    * @param e The event, never <code>null</code>. 
    */
   private void onTabChanged(@SuppressWarnings("unused") ChangeEvent e)
   {
      // remember the previous inner/outer tab indices
      int iPrevInnerIndex = -1;
      int iPrevOuterIndex = m_curOuterIndex;
      m_curOuterIndex = m_tabbedPane.getSelectedIndex();

      ITabDataHelper helper = null;
      JTabbedPane innerPane = null;
      
      /* getting the previous inner/outer tab panel and saving the appropriate
      data */
      if (m_tabbedPane.getComponentAt(iPrevOuterIndex)
         instanceof ITabDataHelper)
      {
         helper = (ITabDataHelper)m_tabbedPane.getComponentAt(iPrevOuterIndex); 

      }
      else if (m_tabbedPane.getComponentAt(iPrevOuterIndex)
         instanceof ITabPaneRetriever)
      {
         innerPane = ((ITabPaneRetriever)m_tabbedPane.getComponentAt(
            iPrevOuterIndex)).getTabbedPane();

         iPrevInnerIndex = innerPane.getSelectedIndex();

         if (innerPane.getComponentAt(iPrevInnerIndex) 
            instanceof ITabDataHelper)
         {
            helper = (ITabDataHelper)innerPane.getComponentAt(
               iPrevInnerIndex);
         }
      }
      
      if (helper == null)
         return;
      
      if (!helper.validateTabData())
      {
         /*
          * Since validation failed, the tab panes are forced to remain 
          * the same.
          */
         m_tabbedPane.setSelectedIndex(iPrevOuterIndex);
         if (innerPane != null && iPrevInnerIndex != -1)
            innerPane.setSelectedIndex(iPrevInnerIndex);
         return;
      }
      
      helper.saveTabData();      
   }

   /**
    * Saves data and closes Main Dialog
    */
   private void saveValData()
   {
      onApply();
      stopTimers();
      dispose();
   }

   /**
    * The action performed by pressing the Ok button.
    * Sets the ServerConfiguration back into PSObjectStore by using
    * saveServerConfiguration(). If the current tab contains savable data it
    * validates it. Saves all config data, and closes Main Dialog.
    */
   private void onOk()
   {
      ms_isOk = true;
      
      ITabDataHelper dataHelper = getDataHelperPanel();
      if (dataHelper == null)
         saveValData();
      else if (dataHelper.validateTabData())
         saveValData();

      return;
   }

   /**
    * Force this dialog to close itself. It will ask the user to save or
    * discard his changes. Since the java forces this there will be no option
    * to cancel.
    */
   public void close()
   {
      if ( m_serverConfiguration.isReadOnly())
      {
         dispose();
         return;
      }

      int op = JOptionPane.showConfirmDialog(this, m_res
         .getString("cancelwarning"), m_res.getString("warning"),
         JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (op == JOptionPane.YES_OPTION)
      {
         // saves the server config and removes the lock.
         onOk();
      }
      else if (JOptionPane.NO_OPTION == op)
      {
         try
         {
            ms_objectStore.releaseServerConfigurationLock(
               m_serverConfiguration.getServerConfiguration());
         }
         catch (Exception e)
         {
            JOptionPane.showMessageDialog(this, Util.cropErrorMessage(e
               .getMessage()), m_res.getString("error"),
               JOptionPane.ERROR_MESSAGE);
         }
         stopTimers();
         dispose();
      }
   }

   /**
    * If the config is read/only, just shuts down, otherwise, it asks the user 
    * what they want to do.
    * Yes = does onApply, then dispose.<BR>
    * No = simply dispose.<BR>
    * Cancel = does nothing, dialog remains.
    */
   private void onClose()
   {
      if ((m_timer == null) || m_timer.isRunning())
         return;

      if ( m_serverConfiguration.isReadOnly())
      {
         dispose();
         return;
      }

      int op = JOptionPane.showConfirmDialog(this,
         m_res.getString("cancelwarning"),
         m_res.getString("warning"),
         JOptionPane.YES_NO_CANCEL_OPTION,
         JOptionPane.WARNING_MESSAGE);

      if (JOptionPane.YES_OPTION == op) //it should behave just like OK clicked
      {
         onOk();
      }
      else if (JOptionPane.NO_OPTION == op)
      {
         try
         {
            ms_objectStore.releaseServerConfigurationLock(
               m_serverConfiguration.getServerConfiguration());
         }
         catch (Exception e)
         {
            JOptionPane.showMessageDialog(this,
               Util.cropErrorMessage(e.getMessage()),
               m_res.getString("error"),
               JOptionPane.ERROR_MESSAGE);
         }
         stopTimers();
         dispose();
      }
      else if (JOptionPane.CANCEL_OPTION == op)
         m_timer.start();
   }

/** Small method to stop timers and remove all pointer references.
*/
  private void stopTimers()
  {
      if (null != m_lockTimer)
      {
         m_lockTimer.stop();
         m_lockTimer = null;
      }

      if (m_timer != null)
      {
         m_timer.stop();
         m_timer.removeActionListener(m_timerListener);
         m_timer = null;
         m_timerListener = null;
      }
  }


   /** 
    * The action performed by pressing the apply button.  Sets the panel&apos;s
    * data into the ServerConfiguration object.
    */
   private void onApply()
   {
      saveData();
   }

   /**
    * Saves the server configuration data. If <code>mb_isOk</code> is
    * <code>true</code> (either the OK button was clicked or, CLOSE button
    * clicked then OK selected on the subsequent dialog box) then the server
    * configuration lock is released after saving the data, otherwise the data
    * will be saved without removing the lock
    *
    * @return <code>true</code> if the server configuration data was saved
    * successfully, <code>false</code> otherwise
    */
   private boolean saveData()
   {
      boolean saved = false;
      try
      {
         /* validates and saves currently visible tab panel
          data into the server config.*/
         ITabDataHelper dataHelper = getDataHelperPanel();
         if (dataHelper != null)
         {
            if (!dataHelper.validateTabData())
               return false;
         
            dataHelper.saveTabData();
         }
         
         // save all datasource definitions to the server
         ms_objectStore.saveHibernateDialectConfig(m_hibernateDialects);
         ms_objectStore.saveJndiDatasources(m_jndiDatasources);
         ms_objectStore.saveDatsourceConfigs(m_datasourceResolver);
         
         // save cataloger configs
         ms_objectStore.saveCatalogerConfigs(m_catalogerConfigs);
         
         /* Saves Role configuration to the server and regets if the role
            configuration is not yet saved. This is required because the role
            configuration should be saved to server only on apply/ok, not on tab
            change.
         */
         saveRoleConfiguration();

         // save the current server configuration and release the server
         // configuration lock. If mb_isOk is false then it will be reacquired
         PSServerConfiguration savedSrvConf =
            m_serverConfiguration.getServerConfiguration();

         // lock should always be removed when saving the configuration
         // if the user modifies the Server ACL in such a way that he locks
         // himself out then he will not be able to release the lock and no
         // other user will be able to acquire the lock till it expires
         ms_objectStore.saveServerConfiguration(savedSrvConf, true);

         // data was saved successfully
         saved = true;
         
         // reset lock timer if necessary
         initLockTimer(savedSrvConf.getUserSessionTimeout());
         
         // notify of restart required
         if (ms_restartRequired)
         {
            JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
               m_res.getString("restartRequired.msg")), m_res.getString(
                  "restartRequired.title"), JOptionPane.WARNING_MESSAGE);
            ms_restartRequired = false;
         }
         

         /* In case of apply, re-get the server configuration as server
          * deletes the group providers which are not used by any security
          * providers.
          */
         if (!ms_isOk)
         {
            PSServerConfiguration srvConf = null;
            String errMsg = null;
            // try to get the server configuration by locking
            try
            {
               srvConf = ms_objectStore.getServerConfiguration(true, false);
            }
            catch (PSAuthorizationException ae)
            {
               // if the user made Server ACL changes such that he locked
               // himself out then this excpetion will be thrown
               errMsg = m_res.getString("readOnlyMode");
            }
            catch (PSLockedException le)
            {
               // in the small time frame between saving the configuration
               // (when the lock is released) and reacquiring the lock, the
               // lock may have been acquired by someone else (very unlikely)
               // In such a case, display message to the user and make the
               // admin client read only.
               MessageFormat msgFromat = new MessageFormat(
                  m_res.getString("LockAcquireFailed"));
               String [] msgParams = {le.getLocalizedMessage()};
               errMsg = msgFromat.format(msgParams);
            }
            finally
            {
               if (errMsg != null)
               {
                  JOptionPane.showMessageDialog(this, errMsg,
                     m_res.getString("readOnlyModeTitle"),
                     JOptionPane.INFORMATION_MESSAGE);

                  // use the saved server configuration
                  // the admin client will run in read-only mode
                  srvConf = savedSrvConf;
                  m_serverConfiguration.setReadOnly(true);

                  MessageFormat titleFromat = new MessageFormat(
                        m_res.getString("readOnlyModeTitleFormat"));
                  String [] titleParams = {getTitle()};
                  makeReadOnly(titleFromat.format(titleParams));
               }
            }

            m_serverConfiguration.setServerConfiguration(srvConf);
         }
      }

      catch (PSServerException se)
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
            se.getMessage()), m_res.getString("error"),
            JOptionPane.ERROR_MESSAGE);
      }
      catch (PSAuthorizationException ae)
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
            ae.getMessage()), m_res.getString("error"),
            JOptionPane.ERROR_MESSAGE);
      }
      catch (PSAuthenticationFailedException e)
      {
         JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
            E2Designer.getResources().getString("AuthErr"),
            JOptionPane.ERROR_MESSAGE );
      }
      catch (PSLockedException le)
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
            le.getMessage()), m_res.getString("error"),
            JOptionPane.ERROR_MESSAGE);
      }
      return saved;

   }

   /**
    * Displays  help for current tab in the panel using JavaHelp viewer.
    */
   private void onHelp()
   {
      PSJavaHelp.launchHelp( "admin." + getCurrentTabForHelp() );
   }


/** 
 * A local utility method get the visible tab/sub-tab class name for help.
 * 
 * @return The string, never <code>null</code>
 */
  private String getCurrentTabForHelp()
  {
    // check first for "status" panel
    int iSelected = m_tabbedPane.getSelectedIndex();
    if (m_tabbedPane.getTitleAt(iSelected).equals(m_res.getString("status")))
    {
      String fullname = m_tabbedPane.getSelectedComponent().getClass().getName();
         return fullname.substring(fullname.lastIndexOf(".") + 1);
    }

    // if not "status" panel, go get the classname of the sub-tab panel
    Component subTabPane = ((ITabPaneRetriever) m_tabbedPane
         .getSelectedComponent()).getTabbedPane();
      String fullname = ((JTabbedPane) subTabPane).getSelectedComponent()
         .getClass().getName();
      
    return fullname.substring(fullname.lastIndexOf(".") + 1);
  }

   /**
    * Create and return the command panel. This panel is always visible and
    * handles the OK, close, apply and help commands.
    *
    * @return the command panel.
    */
   private JPanel createCommandPanel()
   {
      // create the panel
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      buttonPanel.setBorder(new EmptyBorder(6,0,10,10));
      buttonPanel.add(Box.createHorizontalGlue());

      // initialize and add the OK command
      m_buttonOk = new UTFixedButton(m_res.getString("ok"));
      m_buttonOk.setMnemonic(m_res.getString("ok.mn").charAt(0));
      m_buttonOk.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onOk();
         }
      });
      buttonPanel.add(m_buttonOk);
      buttonPanel.add(Box.createHorizontalStrut(6));

      // initialize and add the close command
      m_buttonClose = new UTFixedButton(m_res.getString("close"));
      m_buttonClose.setMnemonic(m_res.getString("close.mn").charAt(0));
      m_buttonClose.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onClose();
         }
      });
      buttonPanel.add(m_buttonClose);
      buttonPanel.add(Box.createHorizontalStrut(6));

      // initialize and add the apply command
      m_buttonApply = new UTFixedButton(m_res.getString("apply"));
      m_buttonApply.setMnemonic(m_res.getString("apply.mn").charAt(0));
      m_buttonApply.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            // saves server config without removing the lock on the config.
            onApply();
         }
      });
      buttonPanel.add(m_buttonApply);
      buttonPanel.add(Box.createHorizontalStrut(6));

      // initialize and add the help command
      m_buttonHelp = new UTFixedButton(m_res.getString("help"));
      m_buttonHelp.setMnemonic(m_res.getString("help.mn").charAt(0));
      m_buttonHelp.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onHelp();
         }
      });
      buttonPanel.add(m_buttonHelp);

      return buttonPanel;
   }
   
   /**
    * Loads all required data from the server
    * 
    * @param locked <code>true</code> to lock the data for editing, 
    * <code>false</code> to get it read only.
    * @param overrideLock <code>true</code> to override any existing locks, 
    * <code>false</code> otherwise.
    * @throws PSAuthorizationException If the user is not authorized.
    * @throws PSAuthenticationFailedException If the user cannot be 
    * authenticated.
    * @throws PSIllegalArgumentException If any old code throws this exception.
    * @throws PSLockedException If the attempt to acquire the lock fails.
    * @throws PSServerException If there are any other errors.
    */
   private void loadData(boolean locked, boolean overrideLock)
      throws PSAuthorizationException, PSAuthenticationFailedException,
      PSIllegalArgumentException, PSServerException, PSLockedException
   {
      m_serverConfiguration = new ServerConfiguration(ms_connection, locked,
         overrideLock);
      m_roleConfiguration = ms_objectStore.getRoleConfiguration(locked,
         overrideLock, false);
      m_jndiDatasources = ms_objectStore.getJndiDatasources(locked);
      m_hibernateDialects = ms_objectStore.getHibernateDialectConfig(locked);
      m_datasourceResolver = ms_objectStore.getDatasourceConfigs(locked);
      m_catalogerConfigs = ms_objectStore.getCatalogerConfigs(locked);
   }

   /**
    * @return a ServerConnection object that connects to the e2 server, null if
    * it hasn't been initialized yet.
    */
   public static ServerConnection getServerConnection()
   {
      return ms_connection;
   }

   /**
    * @return a PSObjectStore object created by the designer connection object
    * used by the admin applet. This object store may be <CODE>null</CODE> when
    * you call for it, because it is a static method and static member variable
    * and may not be available before run-time.
    */
   public static PSObjectStore getServerObjectStore()
   {
      return ms_objectStore;
   }
   
   /**
    * @return <code>true</code> if the feature set 'DirectoryServices' is
    *    supported by the accessed server, <code>false</code> otherwise.
    */
   public static boolean isDirectoryServicesSupported()
   {
      return FeatureSet.getFeatureSet().isFeatureSupported(
         DIRECTORY_SERVICES_FEATURE);
   }
   
   /**
    * Sets a flag to indicate a server restart is required after saving the
    * current changes.  Once set, it is only cleared when changes are saved to
    * the server.
    */
   public static void setRestartRequired()
   {
      ms_restartRequired = true;
   }

   /**
    * A pointer to this frame for use as a parent when creating dialogs. It is
    * not valid until this object has completed its initialization in the ctor.
    * 
    * @return The mainframe, <code>null</code> if the applet has not been
    * initialized. 
    */
   public static JFrame getMainframe()
   { return ms_mainframe; }

   /**
    * A pointer to this frame for use as a parent when creating dialogs. It is
    * not valid until this object has completed its initialization in the ctor.
   **/
   private static JFrame ms_mainframe = null;
   /**
    * the tabbed pane
    */
   private JTabbedPane m_tabbedPane = null;

   /**
    * the update button
    */
   private JButton m_buttonOk = null;

   /**
    * the cancel button
    */
   private JButton m_buttonClose = null;

   /**
    * the apply button
    */
   private JButton m_buttonApply = null;

   /**
    * the help button
    */
   private JButton m_buttonHelp = null;

   /**
    * the current outer tab index
    */
   private int m_curOuterIndex = 0;

   /**
    * the current inner tab index
    */
   //private int m_curInnerIndex = 0;

   /**
    * an E2 server connection
    */
   private static ServerConnection ms_connection = null;
   /**
    * an E2 server console
    */
   private ServerConsole m_console = null;
   /**
    * an E2 server configuration
    */
   private ServerConfiguration m_serverConfiguration = null;

   /** A role configuration, gets initialized in the <code>initDialog</code>*/
   private PSRoleConfiguration m_roleConfiguration = null;
   
   /**
    * Jndi datasources, initialized in {@link #initDialog()}, 
    * never <code>null</code> after that.
    */
   private List<IPSJndiDatasource> m_jndiDatasources;
   
   /**
    * Datsource resolver, initialized in {@link #initDialog()}, 
    * never <code>null</code> after that.
    */
   private PSDatasourceResolver m_datasourceResolver;
   
   /**
    * Hibernate dialects, initialized in {@link #initDialog()}, 
    * never <code>null</code> after that.
    */
   private PSHibernateDialectConfig m_hibernateDialects;

   /**
    * Cataloger configurations, initialized in {@link #initDialog()}, 
    * never <code>null</code> after that.
    */
   private List<PSCatalogerConfig> m_catalogerConfigs;

   /**
    * an E2 server object store interface object. This object may be
    * <CODE>null</CODE> when you call for it.
    */
   private static PSObjectStore ms_objectStore = null;

   /**
    * Timer to delay possible closing of the applet for 2 seconds after first
    * initialized, never <code>null</code> after construction.
    */
   private Timer m_timer = null;
   
   /**
    * Handles timer events for shutdown and lock refresh, never
    * <code>null</code> after construction.
    */
   private ActionListener m_timerListener = null;

   /**
    * Timer to handle extending config lock, never <code>null</code> after 
    * construction
    */
   private Timer m_lockTimer = null;
   

   /**
    * the minutes the lock is to be extended for must be less than the 30 mins
    * that the server grants the lock for
    */   
   private static final int LOCK_EXTEND_MINS = 25;

   /**
    * the dialog size
    */
   private final static Dimension DIALOG_SIZE = new Dimension(650, 510);
   /**
    * Resources
    */
   private static ResourceBundle m_res = PSServerAdminApplet.getResources();

   /**
    * The user properties were we keep the settings like last loged on server
    * to reset them as default on the next session. This file can only be used
    * if we run as an application.
    **/
   private static PSProperties ms_props = null;

     /**
    * A place to store objects that implement the singleton pattern. We store
    * them in the mainframe so the garbage collector will never unload them.
    */
   private List<Object> m_singletons = new ArrayList<Object>(2);

   /**A flag that indicates that the OK button was pressed.
    * It will be used by <code>SecurityRolePanel</code> when saving and
    * re-getting role config.
    */
   private static boolean ms_isOk = false;
 
   /**
    * Flag to indicate if saving changes will require a server restart.  
    */
   private static boolean ms_restartRequired = false;

   /**
    * Name of the Group Providers feature. Used to check whether this feature is
    * supported by server or not.
    */
   public static final String GROUP_PROVIDERS_FEATURE = "GroupProviders";
   
   /**
    * Name of the GDirectory Services feature. Used to check whether this 
    * feature is supported by the server or not.
    */
   public static final String DIRECTORY_SERVICES_FEATURE = "DirectoryServices";

   /**
    * Name of the CMS Caching feature. Used to check whether this feature is
    * supported by server or not.
    */
   public static final String CMS_CACHING_FEATURE = "CmsCaching";
}


/*[ LoggingPropertyPanel.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * The applets main dialog is implemented as a tabbed dialog. One Tab of this
 * dialog is the "Logging" Tab which itself contains several Tabs. This class
 * implements the "Logging" main Tab.
 */
////////////////////////////////////////////////////////////////////////////////
public class LoggingPropertyPanel extends JPanel implements ITabPaneRetriever
{
   /**
    * Construct the GUI elements and initialize them with actual data.
    *
    * @param   parent         the parent frame
    * @param serverConsole the server's remote console object
   * @param serverConfig   the servers configuration
    */
   //////////////////////////////////////////////////////////////////////////////
   public LoggingPropertyPanel(Frame parent, ServerConsole serverConsole, ServerConfiguration serverConfig)
   {
      try
      {
      m_parent = parent;
      m_serverConsole = serverConsole;
         m_serverConfig = serverConfig;

         this.setLayout(new BorderLayout());
      this.setBorder(new EmptyBorder(10, 10, 10, 10));

       // create and add the tabs to tabbed pane
        m_tabPane = new JTabbedPane(JTabbedPane.BOTTOM);

      m_tabPane.addTab(m_res.getString("loggingView"), new LoggingViewPanel(m_serverConsole));
      m_tabPane.addTab(m_res.getString("loggingSettings"), new LoggingSettingsPanel(m_serverConfig));
      setMnemonicsInTheTab();
      m_tabPane.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent e)
        {
          onTabChanged();
        }
      });
       // add tabbed pane to dialog
         this.add(m_tabPane);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

//
// PUBLIC METHODS
//

/** Implementing ITabPaneRetriever interface.
*/
  public JTabbedPane getTabbedPane()
  {
    return m_tabPane;
  }

//
// PRIVATE METHODS
//

/** Handles the action when tab has been changed.
*/
  private void onTabChanged()
  {
    int iPrevInnerIndex = m_curIndex;
    m_curIndex = m_tabPane.getSelectedIndex();

    if (m_tabPane.getComponentAt(iPrevInnerIndex) instanceof ITabDataHelper)
    { 
      if (!((ITabDataHelper)m_tabPane.getComponentAt(iPrevInnerIndex)).validateTabData())
      {
        // since validation failed, the tab panes are forced to remain the
        // same.
        m_tabPane.setSelectedIndex(iPrevInnerIndex);
        return;
      }
      else
      {
        if (!((ITabDataHelper)m_tabPane.getComponentAt(iPrevInnerIndex)).saveTabData())
        {
          // if code gets here, the data in panel at saveTabData() did not 
          // change. Then it simply does nothing and returns.            
          return;  
        }
        else
        {
          // TODO: must access apply button somehow...

          //m_buttonApply.setEnabled(true);
        }
      }
    }
  }
  
  /**
   * Private method that sets mnemonics for all the controls within the tab 
   *
   */
  private void setMnemonicsInTheTab()
  {
      int m = getMnemonicForControl("loggingView");
      m_tabPane.setMnemonicAt(0, (int)getMnemonicForControl("loggingView"));
      m_tabPane.setDisplayedMnemonicIndexAt(0, 0);
      m_tabPane.setMnemonicAt(1, (int)getMnemonicForControl("loggingSettings"));
      m_tabPane.setDisplayedMnemonicIndexAt(1, 0);
  }
  /**
   * @param resId the resource id from the bundle, prepend status and append mn
   * @return return the integer value of the char ( upper case )
   */
  private int getMnemonicForControl(String resId)
  {
      char mnemonic;
      String cmdName = m_res.getString(resId);
      mnemonic = m_res.getString("logging."+resId+".mn").charAt(0);
      return (int)(""+mnemonic).toUpperCase().charAt(0);
  }

  
  
  //////////////////////////////////////////////////////////////////////////////
   /**
    * the parent frame
    */
  private Frame m_parent = null;
   /**
    * the server configuration
    */
   private ServerConfiguration m_serverConfig = null;

   /**
    * the server remote console
    */
  private ServerConsole m_serverConsole = null;
  /**
    * the tabbed pane that is selected
    */
  private JTabbedPane m_tabPane = null;
  /**
   * The counter for the currently selected index.
   */
  private int m_curIndex = 0;
   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();
}


/*[ MonitorPropertyPanel.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * The applets main dialog is implemented as a tabbed dialog. One Tab of this
 * dialog is the "Monitor" Tab which itself contains other Tabs. This class
 * implements the "Monitor" main Tab.
 */
////////////////////////////////////////////////////////////////////////////////
public class MonitorPropertyPanel extends JPanel implements ITabPaneRetriever
{
   /**
    * Construct the GUI elements and initialize them with actual data.
    *
    * @param   parent         the parent frame
   * @param serverConf   the servers configuration
    */
   //////////////////////////////////////////////////////////////////////////////
   public MonitorPropertyPanel(Frame parent, ServerConsole serverConsole)
   {
      try
      {
      m_parent = parent;
      m_serverConsole = serverConsole;

         this.setLayout(new BorderLayout());
      this.setBorder(new EmptyBorder(10, 10, 10, 10));

       // create and add the tabs to tabbed pane
        m_tabPane = new JTabbedPane(JTabbedPane.BOTTOM);

      m_tabPane.addTab(m_res.getString("console"), new MonitorConsolePanel(m_serverConsole));
 
       // add tabbed pane to dialog
         this.add(m_tabPane);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

  /** Implementation of ITabPaneRetriever interface.
  */
  public JTabbedPane getTabbedPane()
  {
    return m_tabPane;
  }

  //////////////////////////////////////////////////////////////////////////////
   /**
    * the parent frame
    */
  private Frame m_parent = null;
  /**
   *
   */
  private JTabbedPane m_tabPane = null;
   /**
    * the server configuration
    */
  private ServerConsole m_serverConsole = null;
   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();
}


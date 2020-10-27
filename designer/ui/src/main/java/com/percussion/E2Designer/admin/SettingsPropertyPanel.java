/******************************************************************************
 *
 * [ SettingsPropertyPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.FeatureSet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * The applets main dialog is implemented as a tabbed dialog. One Tab of this
 * dialog is the "Settings" Tab which itself contains several Tabs. This class
 * implements the "Settings" main Tab.
 */
////////////////////////////////////////////////////////////////////////////////
public class SettingsPropertyPanel extends JPanel implements ITabPaneRetriever
{
   /**
    * Construct the GUI elements and initialize them with actual data.
    * 
    * @param parent the parent frame
    * @param serverConf the servers configuration
    */
   public SettingsPropertyPanel(@SuppressWarnings("unused") Frame parent, 
      ServerConfiguration serverConf)
   {
      try
      {
         m_serverConf = serverConf;

         this.setLayout(new BorderLayout());
         this.setBorder(new EmptyBorder(10, 10, 10, 10));

         // create and add the tabs to tabbed pane
         m_tabPane = new JTabbedPane(JTabbedPane.BOTTOM);

         m_tabPane.addTab(m_res.getString("perf"),
            new SettingsPerformancePanel(m_serverConf));
         setMnemonicForTabIndex("perf", 0);

         if (FeatureSet.getFeatureSet().isFeatureSupported(
            AppletMainDialog.CMS_CACHING_FEATURE))
         {
            m_tabPane.addTab(m_res.getString("cache"),
               new SettingsPageCachingPanel(m_serverConf));
            setMnemonicForTabIndex("cache", 1);
         }

         m_tabPane.addChangeListener(new ChangeListener()
         {
            public void stateChanged(@SuppressWarnings("unused") ChangeEvent e)
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
          System.out.println("Inner tab updated...");
          //m_buttonApply.setEnabled(true);
        }
      }
    }
  }

  /**
   * @param resId the resource id from the bundle, assumed not <code>null</code> 
   * or empty.
   * @param tabIx is the tab index on which a mnemonic has to be set, assumed to 
   * be in the range 0 to m_tabPane.getTabCount()-1, inclusive.
   */
  private void setMnemonicForTabIndex(String resId, int tabIx)
  {
      char mnemonic;
      String tabName = m_res.getString(resId);
      mnemonic = m_res.getString("settings."+ resId+ ".mn").charAt(0);
      int ix = tabName.indexOf(mnemonic);
      char upperMnemonic = (""+mnemonic).toUpperCase().charAt(0);
      m_tabPane.setMnemonicAt(tabIx, upperMnemonic);
      m_tabPane.setDisplayedMnemonicIndexAt(tabIx, ix);
  }

//
// MEMEBR VARIABLES
//

   /**
   * The Tab Pane in this panel.
   */
  private JTabbedPane m_tabPane = null;
  /**
   * The counter for the currently selected index.
   */
  private int m_curIndex = 0;
   /**
    * the server configuration.
    */
  private ServerConfiguration m_serverConf = null;
   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();
}


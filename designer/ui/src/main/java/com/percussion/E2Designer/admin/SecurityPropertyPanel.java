/*[ SecurityPropertyPanel.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSRoleConfiguration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ResourceBundle;
/** 
 * The Security tab on the admin client.
 */
public class SecurityPropertyPanel extends JPanel implements ITabPaneRetriever
{
   private static final long serialVersionUID = 1L;

/**
   * Constructs a tabed security panel.
   * @param parent never <code>null</code>.
   * @param serverConf a server configuration, may not be  <code>null</code>.
   * @param roleConf a role configuration, may not be  <code>null</code>.
   */

   public SecurityPropertyPanel(Frame parent, ServerConfiguration serverConf,
         PSRoleConfiguration roleConf)
   {
      try
      {
         m_parent = parent;
         m_serverConf = serverConf;

         this.setLayout(new BorderLayout());
         this.setBorder(new EmptyBorder(10, 10, 10, 10));

         int tabIx = 0;
          // create and add the tabs to tabbed pane
         m_tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

          m_tabbedPane.addTab(sm_res.getString("provider"),
            new SecurityProviderSummaryPanel(m_serverConf));
         setMnemonicForTabIndex("provider", tabIx++);
         
         m_tabbedPane.addTab(sm_res.getString("roles"),
            new SecurityRolePanel(m_serverConf, roleConf));
         setMnemonicForTabIndex("roles", tabIx++);
         
         m_tabbedPane.addTab(sm_res.getString("serveracl"),
            new ServerAclSummaryPanel(m_parent, m_serverConf));
         setMnemonicForTabIndex("serveracl", tabIx++);
         
         m_tabbedPane.addChangeListener(new ChangeListener()
         {
            public void stateChanged(@SuppressWarnings("unused") ChangeEvent e)
            {
               onTabChanged();
            }
         });

          // add tabbed pane to dialog
         this.add(m_tabbedPane);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * @param resId the resource id from the bundle
    * @param tabIx is the tab index on which a mnemonic has to be set
    */
   private void setMnemonicForTabIndex(String resId, int tabIx)
   {
       char mnemonic;
       String tabName = sm_res.getString(resId);
       mnemonic = sm_res.getString("security."+ resId+ ".mn").charAt(0);
       int ix = tabName.indexOf(mnemonic);
       char upperMnemonic = (""+mnemonic).toUpperCase().charAt(0);
       m_tabbedPane.setMnemonicAt(tabIx, upperMnemonic);
       m_tabbedPane.setDisplayedMnemonicIndexAt(tabIx, ix);
   }

   
/** Implementing ITabPaneRetriever interface.
*/
   public JTabbedPane getTabbedPane()
   {
      return m_tabbedPane;
   }

//
// PRIVATE METHODS
//  

/** Handles the action when tab has been changed.
*/
  private void onTabChanged()
  {
    int iPrevInnerIndex = m_curIndex;
    m_curIndex = m_tabbedPane.getSelectedIndex();

    if (m_tabbedPane.getComponentAt(iPrevInnerIndex) instanceof ITabDataHelper)
    { 
      if (!((ITabDataHelper)m_tabbedPane.getComponentAt(iPrevInnerIndex)).validateTabData())
      {
        // since validation failed, the tab panes are forced to remain the
        // same.
        m_tabbedPane.setSelectedIndex(iPrevInnerIndex);
        return;
      }
      else
      {
        if (!((ITabDataHelper)m_tabbedPane.getComponentAt(iPrevInnerIndex)).saveTabData())
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
      
//
// MEMBER VARIABLES
//
  private Frame m_parent = null;
  private ServerConfiguration m_serverConf = null;
   private JTabbedPane m_tabbedPane = null;
   private int m_curIndex = 0;

  private static ResourceBundle sm_res = PSServerAdminApplet.getResources();
}


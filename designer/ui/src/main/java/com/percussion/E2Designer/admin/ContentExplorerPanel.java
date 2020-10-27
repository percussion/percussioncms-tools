/*[ ContentExplorerPanel.java ]**********************************************
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


/** The javaPlugin tab on the admin client.
*/
public class ContentExplorerPanel extends JPanel implements ITabPaneRetriever
{

   /**
    * Ctor.
    * @param parent parent frame, may be <code>null</code>.
    * @param serverConf never <code>null</code>.
    */
   public ContentExplorerPanel(Frame parent, ServerConfiguration serverConf)
   {
      if (serverConf==null)
         throw new IllegalArgumentException("serverConf may not be null");

      try
      {
         m_parent = parent;
         m_serverConf = serverConf;

         this.setLayout(new BorderLayout());
         this.setBorder(new EmptyBorder(10, 10, 10, 10));

         // create and add the tabs to tabbed pane
         m_tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
         m_tabbedPane.addTab(ms_res.getString("javaPlugin"),
            new ContentExplorerPluginPanel(serverConf));
         setMnemonicsInTheTab();            
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
    * Private method that sets mnemonics for all the controls within the tab 
    *
    */
   private void setMnemonicsInTheTab()
   {
       char mn = ms_res.getString("contentExplorer.javaPlugin.mn").charAt(0);
       String name = ms_res.getString("javaPlugin");
       m_tabbedPane.setMnemonicAt(0, mn);      
       m_tabbedPane.setDisplayedMnemonicIndexAt(0, name.indexOf(mn));
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

   /**
    * Handles the action when tab has been changed.
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
            if (((ITabDataHelper)m_tabbedPane.getComponentAt(iPrevInnerIndex)).saveTabData())
            {
               int iPaneIndex = m_tabbedPane.indexOfTab( ms_res.getString("javaPlugin") );
               if ( iPrevInnerIndex == iPaneIndex )
               {
                  //noop
               }
            }
         }
      }
   }

   /**
    * Parent frame. Initialized in Ctor, may be <code>null</code>.
    */
   @SuppressWarnings("unused")
   private Frame m_parent = null;
   /**
    * Initialized in Ctor, never <code>null</code> after that.
    */
   @SuppressWarnings("unused")
   private ServerConfiguration m_serverConf = null;
   /**
    * Initialized in Ctor, never <code>null</code> after that.
    */
   private JTabbedPane m_tabbedPane = null;
   /**
    * Tracks tab index.
    */
   private int m_curIndex = 0;

   /**
    * Resources.
    */
   private static ResourceBundle ms_res = PSServerAdminApplet.getResources();
}


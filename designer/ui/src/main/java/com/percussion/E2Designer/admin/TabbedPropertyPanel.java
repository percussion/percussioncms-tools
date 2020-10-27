/*[ TabbedPropertyPanel.java ]**************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
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

/**
 * All tabs present in the main applet dialog should extend this class as it 
 * provides support for a multi-tab interface in each tab of the main panel.
 * As tabs are changed, the current tab is validated and saved.
 * <p>This class was created when the full text search tab was added, but
 * all other top level tab components were not updated to use this one.
 */
public abstract class TabbedPropertyPanel extends JPanel 
   implements ITabPaneRetriever
{
   /**
    * Basic ctor. Derived classes must call {@link #addTab(String, Component)
    * addTab} at least once before being shown.
    */
   public TabbedPropertyPanel()
   {
      setLayout(new BorderLayout());
      setBorder(new EmptyBorder(10, 10, 10, 10));

      m_tabPane.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            onTabChanged();
         }
      });
      add(m_tabPane);
   }
  
   /**
    * Must be called by derived class to add at least 1 tab.
    * 
    * @param name The name displayed for the tab, never <code>null</code> or
    * empty.
    * 
    * @param c This component is used to when creating the tab. Never 
    * <code>null</code>.
    */
   protected void addTab(String name, Component c)
   {
      if (null == name || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }
      if (null == c)
      {
         throw new IllegalArgumentException("component cannot be null");
      }
      m_tabPane.addTab(name, c);
   }

   // see interface for description
   public JTabbedPane getTabbedPane()
   {
      return m_tabPane;
   }

   /** 
    * As tabs are changed, we want to validate data in the current tab so
    * we don't end up w/ lots of tabs that may have invalid data at the
    * end (when the user applies his changes).
    * <p>Validates and saves the data as tabs change.
    */
   private void onTabChanged()
   {
      int iPrevInnerIndex = m_curIndex;
      m_curIndex = m_tabPane.getSelectedIndex();

      if (m_tabPane.getComponentAt(iPrevInnerIndex) instanceof ITabDataHelper)
      { 
         if (!((ITabDataHelper)
               m_tabPane.getComponentAt(iPrevInnerIndex)).validateTabData())
         {
            // since validation failed, the tab panes are forced to remain the
            // same.
            m_tabPane.setSelectedIndex(iPrevInnerIndex);
            return;
         }
         else
         {
            if (!((ITabDataHelper)
                  m_tabPane.getComponentAt(iPrevInnerIndex)).saveTabData())
            {
               // if code gets here, the data in panel at saveTabData() did not 
               // change. Then it simply does nothing and returns.            
               return;  
            }
            else
            {
               m_dataChanged = true;
            }
         }
      }
   }

   /**
    * We track changes to the config here so that our parent can later 
    * determine whether any changes have been made. Defaults to <code>false
    * </code>.
    * <em>Note</em>: This is not implemented properly yet in all classes, but
    * all new tabs should conform to this model. The interface needs to be 
    * modified to make this data accessible to the parent.
    */
   private boolean m_dataChanged = false;
     
   /**
    * The container for all sub-tabs. Never <code>null</code>. Displays tabs
    * at bottom.
    */
   private JTabbedPane m_tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
   
   /**
    * The counter for the currently selected index.
    */
   private int m_curIndex = 0;
}

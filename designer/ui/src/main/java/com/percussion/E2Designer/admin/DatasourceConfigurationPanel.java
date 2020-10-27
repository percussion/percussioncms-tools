/******************************************************************************
 *
 * [ DatasourceConfigurationPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.PSDatasourceResolver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Panel that contains the tabs for editing datasource configurations.
 */
public class DatasourceConfigurationPanel extends JPanel 
   implements ITabPaneRetriever
{
   private static final long serialVersionUID = 1L;
   
   /**
    * Construct the panel
    * 
    * @param parent The parent fame, used to make this dialog modal to that
    * frame, may be <code>null</code> if there is no parent frame.
    * @param serverConfig The server configuration used to edit the JDBC driver
    * configurations, may not be <code>null</code>.
    * @param jndiDatasources The list of JNDI datasources to edit, may not be
    * <code>null</code>.  Modifications to this list by this dialoed affect 
    * the contents of the callers list.
    * @param datasourceResolver The resolver containing the list of datasource
    * configurations to edit, may not be <code>null</code>.
    * @param hibernateDialectConfig The hibernate configuration to edit, may
    * not be <code>null</code>.
    */
   public DatasourceConfigurationPanel(Frame parent,
      ServerConfiguration serverConfig, List<IPSJndiDatasource> jndiDatasources,
      PSDatasourceResolver datasourceResolver,
      PSHibernateDialectConfig hibernateDialectConfig)
   {
      if (serverConfig == null)
         throw new IllegalArgumentException("serverConfig may not be null");
      if (jndiDatasources == null)
         throw new IllegalArgumentException("jndiDatasources may not be null");
      if (datasourceResolver == null)
         throw new IllegalArgumentException(
            "datasourceConfigs may not be null");
      if (hibernateDialectConfig == null)
         throw new IllegalArgumentException(
            "hibernateDialectConfig may not be null");
      
      m_parent = parent;
      m_serverConfig = serverConfig;
      m_jndiDatasources = jndiDatasources;
      m_datasourceResolver = datasourceResolver;
      m_hibernateDialects = hibernateDialectConfig;
      
      initPanel();
   }

   /**
    * Initializes this panels ui components, including all sub tabs.
    */
   private void initPanel()
   {
      setLayout(new BorderLayout());
      setBorder(new EmptyBorder(10, 10, 10, 10));

      int tabIx = 0;
      String key = "datasources.";
      // create the header message
      JLabel label = new JLabel(ms_res.getString(key + "headerText"));
      label.setBorder(new EmptyBorder(0, 5, 10, 5));
      add(label, BorderLayout.NORTH);
      
       // create and add the tabs to tabbed pane
      m_tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
      String tabKey;
      
      tabKey = key + "drivers";
      m_tabbedPane.addTab(ms_res.getString(tabKey),
         new DatasourceDriversPanel(m_parent, m_serverConfig, 
            m_hibernateDialects, m_jndiDatasources, m_datasourceResolver));
      setMnemonicForTabIndex(tabKey, tabIx++);

      tabKey = key + "jndi";
      m_tabbedPane.addTab(ms_res.getString(tabKey),
         new DatasourceJndiPanel(m_parent, m_serverConfig, 
            m_hibernateDialects, m_jndiDatasources, m_datasourceResolver));
      setMnemonicForTabIndex(tabKey, tabIx++);
      
      tabKey = key + "connections";
      m_tabbedPane.addTab(ms_res.getString(tabKey),
         new DatasourceConnectionsPanel(m_parent, m_serverConfig, 
            m_hibernateDialects, m_jndiDatasources, m_datasourceResolver));
      setMnemonicForTabIndex(tabKey, tabIx++);

      // create listener to handle button state of other panels when data
      // on one panel changes.
      TableModelListener tml = new TableModelListener() {

         public void tableChanged(TableModelEvent e)
         {
            if (e == null);
            for (int i = 0; i < m_tabbedPane.getTabCount(); i++)
            {
               if (i != m_tabbedPane.getSelectedIndex())
               {
                  ((DatasourceBasePanel) m_tabbedPane.getComponentAt(i))
                     .updateButtonState();
               }
            }
         }};
         
      // add the listener to all panels
      for (int i = 0; i < m_tabbedPane.getTabCount(); i++)
      {
         ((DatasourceBasePanel) m_tabbedPane.getComponentAt(i))
            .addTableModelListener(tml);
      }
   
      // add listener to handle validation on tab switching
      m_tabbedPane.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            if (e == null);
            onTabChanged();
         }
      });
      
      add(m_tabbedPane, BorderLayout.CENTER);
   }

   // See ITabPaneRetriever
   public JTabbedPane getTabbedPane()
   {
      return m_tabbedPane;
   }
   
   /**
    * Sets the mnemonic for the specified tab index.
    * 
    * @param resId the resource id from the bundle, assumed not 
    * <code>null</code> or empty.
    * @param tabIx is the tab index on which a mnemonic has to be set
    */
   private void setMnemonicForTabIndex(String resId, int tabIx)
   {
       char mnemonic;
       String tabName = ms_res.getString(resId);
       mnemonic = ms_res.getString(resId + ".mn").charAt(0);
       int ix = tabName.indexOf(mnemonic);
       char upperMnemonic = (""+mnemonic).toUpperCase().charAt(0);
       m_tabbedPane.setMnemonicAt(tabIx, (int)upperMnemonic);
       m_tabbedPane.setDisplayedMnemonicIndexAt(tabIx, ix);
   }
   
   /** Handles the action when tab has been changed.
   */
     private void onTabChanged()
     {
       int iPrevInnerIndex = m_curIndex;
       m_curIndex = m_tabbedPane.getSelectedIndex();

       if (m_tabbedPane.getComponentAt(iPrevInnerIndex) instanceof ITabDataHelper)
       { 
         if (!((ITabDataHelper)m_tabbedPane.getComponentAt(
            iPrevInnerIndex)).validateTabData())
         {
           // since validation failed, the tab panes are forced to remain the
           // same.
           m_tabbedPane.setSelectedIndex(iPrevInnerIndex);           
         }
         else
         {
            ((ITabDataHelper)m_tabbedPane.getComponentAt(
              iPrevInnerIndex)).saveTabData();
         }
       }
     }   

   /**
    * The parent frame, never <code>null</code> or modified after ctor.
    */
   private Frame m_parent;
   
   /**
    * The server config containing the JDBC drivers to configure, never 
    * <code>null</code> after ctor.
    */
   private ServerConfiguration m_serverConfig;

   /**
    * The hibernate dialects, used to configure the drivers, never 
    * <code>null</code> after ctor.
    */
   private PSHibernateDialectConfig m_hibernateDialects;
   
   /**
    * The list of datasources to configure, never <code>null</code> after ctor.
    */
   private List<IPSJndiDatasource> m_jndiDatasources;
   
   
   /**
    * The datasource resolver to configure, never <code>null</code> after ctor.
    */
   private PSDatasourceResolver m_datasourceResolver;

   /**
    * Contains the sub-tabs of this dialog, created during construction, never
    * <code>null</code> after that.
    */
   private JTabbedPane m_tabbedPane;
   
   /**
    * The resource bundle to use, never <code>null</code>.
    */
   private static ResourceBundle ms_res = PSServerAdminApplet.getResources();
   
   /**
    * The currently selected tab, ininitialized during construction, maintained
    * to reference the index of the currently selected tab.
    */
   private int m_curIndex = 0;
}
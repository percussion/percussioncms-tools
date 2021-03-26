/******************************************************************************
 *
 * [ DirectoryServicePanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.IPSGroupProviderInstance;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.PSSecurityProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides a tabbed panel that allows a user to display and configure all
 * directory services.
 */
public class DirectoryServicePanel extends DirectoryServiceBasePanel 
   implements ITabDataHelper, ITabPaneRetriever
{
  
   /**
    * Constructs the new directory panel for the supplied parameters. It will
    * provide a tabbed pane to configure directory sevices.
    * 
    * @param parent the parent frame, not <code>null</code>.
    * @param data the directory service data to initialize the panel with,
    *    not <code>null</code>. This object is used to store the
    *    data before it is saved to the server configuration and to transfer
    *    information between the various directory service panels.
    * @param config the server configuration to which the UI component is
    *    saved, not <code>null</code>.
    */
   public DirectoryServicePanel(Frame parent, DirectoryServiceData data,
      ServerConfiguration config)
   {
      super(parent, data, config);
            
      initPanel();
   }
   
   /**
    * @see com.percussion.E2Designer.admin.ITabDataHelper#saveTabData()
    */
   public boolean saveTabData()
   {
      boolean result = false;
      for (int i=0; i<m_tabbedPane.getTabCount(); i++)
      {
         ITabDataHelper panel = (ITabDataHelper) m_tabbedPane.getComponent(i);
         if (panel.saveTabData())
            result = true;
      }
      
      PSServerConfiguration config = getConfig();
      Iterator groupProviders = m_data.getGroupProviders().iterator();
      while (groupProviders.hasNext())
      {
         config.setGroupProviderInstance(
            (IPSGroupProviderInstance) groupProviders.next());
      }
      
      setModified(false);
      
      return result;
   }

   /**
    * @see com.percussion.E2Designer.admin.ITabDataHelper#validateTabData()
    */
   public boolean validateTabData()
   {
      /*
       * Make sure that all directory sets and role providers specified in 
       * any security provider are still avaliable.
       */
      List errors = new ArrayList();
      PSServerConfiguration config = getConfig();
      Iterator securityProviders = 
         config.getSecurityProviderInstances().iterator();
      while (securityProviders.hasNext())
      {
         PSSecurityProviderInstance securityProvider = 
            (PSSecurityProviderInstance) securityProviders.next();
         if (securityProvider.getType() == 
            PSSecurityProvider.SP_TYPE_DIRCONN)
         {
            PSProvider testee = securityProvider.getDirectoryProvider();
            if (testee != null)
            {
               String name = testee.getReference().getName();
               if (m_data.getDirectorySet(name) == null)
               {
                  Object[] args = 
                  {
                     securityProvider.getName(),
                     name
                  };
                  
                  String error = MessageFormat.format(
                     getResources().getString(
                        "dir.sec.error.missingdirectoryset"), args);
                  errors.add(error);
               }
            }
         }
      }
      if (!errors.isEmpty())
      {
         StringBuffer errorString = new StringBuffer();
         for (int i=0; i<errors.size(); i++)
         {
            errorString.append("\n   - ");
            errorString.append(errors.get(i).toString());
         }
         
         Object[] args = 
         {
            errorString.toString()
         };
         
         String message = MessageFormat.format(getResources().getString(
            "dir.sec.error"), args);
         
         JOptionPane.showMessageDialog(getParent(), message, 
            getResources().getString("dir.error.title"), 
            JOptionPane.ERROR_MESSAGE);
               
         return false;
      }

      /*
       * Validate all directory service sub-tabs.
       */
      for (int i=0; i<m_tabbedPane.getTabCount(); i++)
      {
         ITabDataHelper panel = (ITabDataHelper) m_tabbedPane.getComponent(i);
         if (!panel.validateTabData())
            return false;
      }
      
      return true;
   }
   
   /**
    * Not required/supported for the directory services main tab.
    */
   protected void initData()
   {
      throw new UnsupportedOperationException(
         "initData is not supported");
   }
   
   /* @see com.percussion.E2Designer.admin.ITabPaneRetriever#getTabbedPane()
    */
   public JTabbedPane getTabbedPane()
   {
      return m_tabbedPane;
   }
   
   /**
    * Initializes the panel with all UI components supplied.
    */
   protected void initPanel()
   {
      setLayout(new BorderLayout());
      setBorder(new EmptyBorder(10, 10, 10, 10));

      // create and add all tabs to tabbed pane
      m_tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
      int tabIx = 0;
      
      m_authenticationsPanel = new AuthenticationsPanel(m_parent);
      m_tabbedPane.addTab(getResources().getString("dir.authentications"),
         m_authenticationsPanel);
      setMnemonicForTabIndex("dir.authentications", tabIx++);
      m_authenticationsPanel.setContainerPanel(this);
         
      m_directoriesPanel = new DirectoriesPanel(m_parent);
      m_tabbedPane.addTab(getResources().getString("dir.directories"),
         m_directoriesPanel);
      setMnemonicForTabIndex("dir.directories", tabIx++);
      
      m_directoriesPanel.setContainerPanel(this);
      m_directoriesPanel.addChangeListener(m_authenticationsPanel);
         
      m_directorySetsPanel = new DirectorySetsPanel(m_parent);
      m_tabbedPane.addTab(getResources().getString("dir.directorysets"),
         m_directorySetsPanel);
      setMnemonicForTabIndex("dir.directorysets", tabIx++);
      
      m_directorySetsPanel.setContainerPanel(this);
      m_directorySetsPanel.addChangeListener(m_authenticationsPanel);
      m_directorySetsPanel.addChangeListener(m_directoriesPanel);
         
      m_roleProvidersPanel = new RoleProvidersPanel(m_parent);
      m_tabbedPane.addTab(getResources().getString("dir.roleproviders"),
         m_roleProvidersPanel);
      setMnemonicForTabIndex("dir.roleproviders", tabIx++);
      
      m_roleProvidersPanel.setContainerPanel(this);
      m_roleProvidersPanel.addChangeListener(m_authenticationsPanel);
      m_roleProvidersPanel.addChangeListener(m_directoriesPanel);
      m_roleProvidersPanel.addChangeListener(m_directorySetsPanel);

      m_catalogerConfigsPanel = new CatalogerConfigurationsPanel(m_parent, 
         m_data.getCatalogerConfigs());
      m_tabbedPane.addTab(getResources().getString("dir.catalogerconfigs"),
         m_catalogerConfigsPanel);
      setMnemonicForTabIndex("dir.catalogerconfigs", tabIx++);
      
      m_tabbedPane.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent event)
         {
            Object source = event.getSource();
            if (source instanceof JTabbedPane)
            {
               JTabbedPane pane = (JTabbedPane) source;
               Component comp = pane.getSelectedComponent();
               if (comp instanceof DirectoryServiceBasePanel)
               {
                  DirectoryServiceBasePanel panel = 
                     (DirectoryServiceBasePanel) comp;
                  panel.initData();
               }
            }
         }
      });

      // and add tabbed pane to the panel
      add(m_tabbedPane);
   }
   
   /**
    * @param resId the resource id from the bundle
    * @param tabIx is the tab index on which a mnemonic has to be set
    */
   private void setMnemonicForTabIndex(String resId, int tabIx)
   {
       char mnemonic;
       String tabName = getResources().getString(resId);
       mnemonic = getResources().getString(resId+ ".mn").charAt(0);
       int ix = tabName.indexOf(mnemonic);
       char upperMnemonic = (""+mnemonic).toUpperCase().charAt(0);
       m_tabbedPane.setMnemonicAt(tabIx, (int)upperMnemonic);
       m_tabbedPane.setDisplayedMnemonicIndexAt(tabIx, ix);
   }

   
   /**
    * This panel has no Add button, does nothing for this action.
    */
   protected void onAdd()
   {
      // noop
   }

   /**
    * This panel has no Delete button, does nothing for this action.
    */
   protected void onDelete()
   {
      // noop
   }

   /**
    * This panel has no Edit button, does nothing for this action.
    */
   protected void onEdit()
   {
      // noop
   }
   
   /**
    * The tabbed pane provided through this panel, initialized in constructor,
    * never <code>null</code> or changed after that.
    */
   private JTabbedPane m_tabbedPane = null;
   
   /**
    * The panel used to add, remove and edit authentications. Initialized in
    * {@link #initPanel()}, never <code>null</code> or changed after that.
    */
   private AuthenticationsPanel m_authenticationsPanel = null;
   
   /**
    * The panel used to add, remove and edit directories. Initialized in
    * {@link #initPanel()}, never <code>null</code> or changed after that.
    */
   private DirectoriesPanel m_directoriesPanel = null;
   
   /**
    * The panel used to add, remove and edit directory sets. Initialized in
    * {@link #initPanel()}, never <code>null</code> or changed after that.
    */
   private DirectorySetsPanel m_directorySetsPanel = null;
   
   /**
    * The panel used to add, remove and edit role providers. Initialized in
    * {@link #initPanel()}, never <code>null</code> or changed after that.
    */
   private RoleProvidersPanel m_roleProvidersPanel = null;
   
   /**
    * Panel used to modify the cataloger configs, initialized in
    * {@link #initPanel()}, never <code>null</code> or changed after that.
    */
   private CatalogerConfigurationsPanel m_catalogerConfigsPanel = null;
}

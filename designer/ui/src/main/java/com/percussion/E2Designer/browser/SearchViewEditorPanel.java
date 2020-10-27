/* *****************************************************************************
 *
 * [ SearchViewEditorPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.FeatureSet;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchCollection;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * NOTE: While reviewing please keep in mind that no data objects have been
 * used as of yet so ctor and other places may have to be modified but that
 * shouldn't affect the ui in general, hopefully.
 */

/**
 * Panel holding the all editing controls to provide an editor for
 * a given search/view cms object.
 */
public class SearchViewEditorPanel extends JPanel
   implements IPSDbComponentUpdater
{
   /**
    * Consructs the panel.
    *
    * @param parent the parent container, not <code>null</code>.
    * @param searches a collection of searches, not <code>null</code> or,
    *    may be empty.
    */
   public SearchViewEditorPanel(SearchViewDialog parent,
      PSSearchCollection searches)
   {
      if (parent == null)
         throw new IllegalArgumentException(
            "parent frame must not be null");

      m_parentFrame = parent;
      init(searches);
      m_tabPane.addChangeListener(m_parentFrame);
   }

   /**
    * This method will remove all tabs from this panel.
    */
   public void clearTabs()
   {
      onActivateTabs(null);
   }

   /**
    * Calls same method on all child panels.
    */
   public void onDataPersisted()
   {
      m_generalPanel.onDataPersisted();
      m_urlPanel.onDataPersisted();
      m_queryPanel.onDataPersisted();
      m_searchQueryPanel.onDataPersisted();
      m_communitiesPanel.onDataPersisted();
      m_propertiesPanel.onDataPersisted();
   }

   /**
    * Initializes the panel.
    *
    * @param searches a collection of searches, assumed not <code>null</code>,
    *    may be empty.
    */
   private void init(PSSearchCollection searches)
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());

      setLayout(new BorderLayout());
      Border b = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      setBorder(b);

      m_tabPane = new JTabbedPane();
      m_disabledPanel = createDisabledPanel();
      
      // General Panel
      m_generalPanel = new SearchViewGeneralPanel(m_parentFrame);
      // URL panel
      m_urlPanel = new SearchViewURLPanel(searches);
      // Communities panel
      m_communitiesPanel = new SearchViewCommunitiesPanel();
      // View Queries panel
      m_queryPanel = new SearchViewQueriesPanel(m_parentFrame);
      // Search Queries panel
      m_searchQueryPanel = new SearchQueryPanel(m_parentFrame);
      // Properties panel
      m_propertiesPanel = new SearchViewPropertiesPanel(m_parentFrame);
      // handles these tabs layout
      onActivateTabs(null);      
   }

   /**
    * Creates a panel containing only the disabled search message
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createDisabledPanel()
   {
      JPanel msgPanel = new JPanel();
      msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
      JTextArea msgArea = new JTextArea(ms_res.getString(
         "search.disabled.msg"));
      msgArea.setBackground(getBackground());
      msgArea.setEditable(false);
      msgArea.setFont((new JLabel()).getFont());
      msgArea.setLineWrap(true);
      msgArea.setWrapStyleWord(true);
      msgArea.setRows(3);
      msgArea.setMaximumSize(new Dimension(500, 200));

      msgPanel.add(Box.createVerticalGlue());      
      msgPanel.add(msgArea);
      msgPanel.add(Box.createVerticalGlue());

      return msgPanel;
   }

   /**
    * Convenience method used internally to handle swapping
    * panes based on whether or not the supplied search object
    * is a custom application or not.
    *
    * @param search object may be null to default the
    *    tabs (i.e. show no tabs).
    */
   private void onActivateTabs(PSSearch search)
   {
      // Threshold - if the search object is null
      // just show empty
      if (search == null)
      {
         m_tabPane.removeAll();
         setCurComp(m_tabPane);
      }      
      else if (!FeatureSet.isFTSearchEnabled() && search.useExternalSearch())
      {
         // handle disabled search
         setCurComp(m_disabledPanel);         
      }
      else 
      {
         int currentSelected = m_tabPane.getSelectedIndex();
         
         // is this a custom app
         //    if yes is the url panel loaded?
         //       if url panel is not loaded, load it
         // if no it's not a custom app
         //    is the url panel loaded
         //       if it is, unload it and load the query panel
         // we need to load the url panel if it's not loaded:
         if (search.isCustomView())
         {
            enableCustomViewTabs();
         }
         else if(search.isStandardView())
         {
            enableStandardViewTabs();
         }
         else if(search.isCustomSearch())
         {
            enableCustomSearchTabs();
         }
         else if(search.isStandardSearch())
         {
            enableStandardSearchTabs();
         }
         
         if (currentSelected >= 0 && currentSelected < m_tabPane.getTabCount())
            m_tabPane.setSelectedIndex(currentSelected);
         
         setCurComp(m_tabPane);
      }
   }

   /**
    * Sets the current component to be held by the right hand panel.
    * 
    * @param comp The component to set, assumed not <code>null</code>.  If
    * this is not the current component held by that panel, the current 
    * component is removed and this component added to the panel.
    */
   private void setCurComp(JComponent comp)
   {
      if (comp != m_curComp)
      {
         if (m_curComp != null)
            remove(m_curComp);

         add(comp, BorderLayout.CENTER);
         m_curComp = comp;
      }
      
      updateUI();
   }

   /**
    * Enables the custom tab components.
    */
   private void enableCustomViewTabs()
   {
      m_enabledTabPanelType = CUSTOM_VIEW_TAB_PANEL;

      // remove unnecessary tabs:
      m_tabPane.remove(m_queryPanel);
      m_tabPane.remove(m_searchQueryPanel);

      // add only if they don't exist:
      if(m_tabPane.indexOfComponent(m_generalPanel) < 0)
      {
         m_tabPane.add(m_generalPanel, 0);
         m_tabPane.setTitleAt(0, ms_res.getString("tabname.general"));
         setMnemonicForTabIndex("tabname.general", 0);
      }

      if(m_tabPane.indexOfComponent(m_communitiesPanel) < 0)
      {
         m_tabPane.add(m_communitiesPanel, 1);
         m_tabPane.setTitleAt(1, ms_res.getString("tabname.communities"));
         setMnemonicForTabIndex("tabname.communities", 1);
      }

      if(m_tabPane.indexOfComponent(m_urlPanel) < 0)
      {
         m_tabPane.add(m_urlPanel, 2);
         m_tabPane.setTitleAt(2, ms_res.getString("tabname.url"));
         setMnemonicForTabIndex("tabname.url", 2);
      }

      if(m_tabPane.indexOfComponent(m_propertiesPanel) < 0)
      {
         m_tabPane.add(m_propertiesPanel, 3);
         m_tabPane.setTitleAt(3, ms_res.getString("tabname.properties"));
         setMnemonicForTabIndex("tabname.properties", 3);
      }
   }

   /**
    * Enables the standard tab components.
    */
   private void enableStandardViewTabs()
   {
      m_enabledTabPanelType = STANDARD_VIEW_TAB_PANEL;

      // remove unnecessary tabs:
      m_tabPane.remove(m_urlPanel);
      m_tabPane.remove(m_searchQueryPanel);

      // add only if they don't exist:
      if(m_tabPane.indexOfComponent(m_generalPanel) < 0)
      {
         m_tabPane.add(m_generalPanel, 0);
         m_tabPane.setTitleAt(0, ms_res.getString("tabname.general"));
         setMnemonicForTabIndex("tabname.general", 0);
      }

      if(m_tabPane.indexOfComponent(m_communitiesPanel) < 0)
      {
         m_tabPane.add(m_communitiesPanel, 1);
         m_tabPane.setTitleAt(1, ms_res.getString("tabname.communities"));
         setMnemonicForTabIndex("tabname.communities", 1);
      }

      if(m_tabPane.indexOfComponent(m_queryPanel) < 0)
      {
         m_tabPane.add(m_queryPanel, 2);
         m_tabPane.setTitleAt(2, ms_res.getString("tabname.queries"));
         setMnemonicForTabIndex("tabname.queries", 2);
      }

      if(m_tabPane.indexOfComponent(m_propertiesPanel) < 0)
      {
         m_tabPane.add(m_propertiesPanel, 3);
         m_tabPane.setTitleAt(3, ms_res.getString("tabname.properties"));
         setMnemonicForTabIndex("tabname.properties", 3);
      }
   }

   /**
    * Enables the standard tab components.
    */
   private void enableStandardSearchTabs()
   {
      m_enabledTabPanelType = STANDARD_SEARCH_TAB_PANEL;

      // remove unnecessary tabs:
      m_tabPane.remove(m_urlPanel);
      m_tabPane.remove(m_queryPanel);

      // add only if they don't exist:
      if(m_tabPane.indexOfComponent(m_generalPanel) < 0)
      {
         m_tabPane.add(m_generalPanel, 0);
         m_tabPane.setTitleAt(0, ms_res.getString("tabname.general"));
         setMnemonicForTabIndex("tabname.general", 0);
      }

      if(m_tabPane.indexOfComponent(m_communitiesPanel) < 0)
      {
         m_tabPane.add(m_communitiesPanel, 1);
         m_tabPane.setTitleAt(1, ms_res.getString("tabname.communities"));
         setMnemonicForTabIndex("tabname.communities", 1);
      }

      if(m_tabPane.indexOfComponent(m_searchQueryPanel) < 0)
      {
         m_tabPane.add(m_searchQueryPanel, 2);
         m_tabPane.setTitleAt(2, ms_res.getString("tabname.searchquery"));
         setMnemonicForTabIndex("tabname.searchquery", 2);
      }

      if(m_tabPane.indexOfComponent(m_propertiesPanel) < 0)
      {
         m_tabPane.add(m_propertiesPanel, 3);
         m_tabPane.setTitleAt(3, ms_res.getString("tabname.properties"));
         setMnemonicForTabIndex("tabname.properties", 3);
      }
   }

   /**
    * Enables the standard tab components.
    */
   private void enableCustomSearchTabs()
   {
      m_enabledTabPanelType = CUSTOM_SEARCH_TAB_PANEL;

      // remove unnecessary tabs:
      m_tabPane.remove(m_urlPanel);
      m_tabPane.remove(m_queryPanel);

      // add only if they don't exist:
      if(m_tabPane.indexOfComponent(m_generalPanel) < 0)
      {
         m_tabPane.add(m_generalPanel, 0);
         m_tabPane.setTitleAt(0, ms_res.getString("tabname.general"));
         setMnemonicForTabIndex("tabname.general", 0);
      }

      if(m_tabPane.indexOfComponent(m_communitiesPanel) < 0)
      {
         m_tabPane.add(m_communitiesPanel, 1);
         m_tabPane.setTitleAt(1, ms_res.getString("tabname.communities"));
         setMnemonicForTabIndex("tabname.communities", 1);
      }

      if(m_tabPane.indexOfComponent(m_searchQueryPanel) < 0)
      {
         m_tabPane.add(m_searchQueryPanel, 2);
         m_tabPane.setTitleAt(2, ms_res.getString("tabname.searchquery"));
         setMnemonicForTabIndex("tabname.searchquery", 2);
      }

      if(m_tabPane.indexOfComponent(m_propertiesPanel) < 0)
      {
         m_tabPane.add(m_propertiesPanel, 3);
         m_tabPane.setTitleAt(3, ms_res.getString("tabname.properties"));
         setMnemonicForTabIndex("tabname.properties", 3);
      }
   }

   /**
    * Returns enabled tab panel type
    * @return short indicating enabled tab panel type
    */
   public short getEnabledTabPanelType()
   {
      return m_enabledTabPanelType;
   }

   /**
    * If validation fails, the tab that failed will be made the current tab.
    * See interface for more details.
    */
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp must not be null");

      // Threshold - we're only interested in a PSSearch cms object
      // other objects passed we just ignore
      if (!(comp instanceof PSSearch))
         return true;

      PSSearch search = (PSSearch) comp;

      if (!m_generalPanel.onValidateData(search, isQuiet))
      {
         m_tabPane.setSelectedComponent(m_generalPanel);
         return false;
      }

      if (search.isStandardView())
      {
         if (!m_queryPanel.onValidateData(search, isQuiet))
         {
            m_tabPane.setSelectedComponent(m_queryPanel);
            return false;
         }
      }
      else if(search.isCustomView())
      {
         if (!m_urlPanel.onValidateData(search, isQuiet))
         {
            m_tabPane.setSelectedComponent(m_urlPanel);
            return false;
         }
      }
      else if(search.isStandardSearch() || search.isCustomSearch())
      {
         if (!m_searchQueryPanel.onValidateData(search, isQuiet))
         {
            m_tabPane.setSelectedComponent(m_searchQueryPanel);
            return false;
         }
      }

      if (!m_communitiesPanel.onValidateData(search, isQuiet))
      {
         m_tabPane.setSelectedComponent(m_communitiesPanel);
         return false;
      }
      if (!m_propertiesPanel.onValidateData(search, isQuiet))
      {
         m_tabPane.setSelectedComponent(m_propertiesPanel);
         return false;
      }

      return true;
   }

   // see interface for description
   public boolean onUpdateData(
      IPSDbComponent comp, boolean isSaving, boolean isQuiet)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp must not be null");

      // Threshold - we're only interested in a PSSearch cms object
      // other objects passed we just ignore
      if (!(comp instanceof PSSearch))
         return true;

      PSSearch search = (PSSearch) comp;

      // handles any swapping of panels based on the
      // selected search
      if (!isSaving)
         onActivateTabs(search);

      // update all the panels with the 'actively' selected
      // search object
      
      // save properties first in case another panel uses properties to store 
      // its data
      if (!m_propertiesPanel.onUpdateData(search, isSaving, isQuiet))
      {
         m_tabPane.setSelectedComponent(m_propertiesPanel);
         return false;
      }

      if (!m_generalPanel.onUpdateData(search, isSaving, isQuiet))
      {
         m_tabPane.setSelectedComponent(m_generalPanel);
         return false;
      }

      if (search.isStandardView())
      {
         if (!m_queryPanel.onUpdateData(search, isSaving, isQuiet))
         {
            m_tabPane.setSelectedComponent(m_queryPanel);
            return false;
         }
      }
      else if (search.isCustomView())
      {
         if (!m_urlPanel.onUpdateData(search, isSaving, isQuiet))
         {
            m_tabPane.setSelectedComponent(m_urlPanel);
            return false;
         }
      }
      else if (search.isStandardSearch() || search.isCustomSearch())
      {
         if (!m_searchQueryPanel.onUpdateData(search, isSaving, isQuiet))
         {
            m_tabPane.setSelectedComponent(m_searchQueryPanel);
            return false;
         }
      }

      if (!m_communitiesPanel.onUpdateData(search, isSaving, isQuiet))
      {
         m_tabPane.setSelectedComponent(m_communitiesPanel);
         return false;
      }


      return true;
   }

   /**
    * Get the selected tab.
    *
    * @return integer selected tab id.
    */
   public int getSelectedTab()
   {
      return m_tabPane.getSelectedIndex();
   }

    /**
    * Get the communities panel.
    *
    * @return <code>SearchViewCommunitiesPanel</code> communities panel, never <code>null</code>.
    */
   public SearchViewCommunitiesPanel getCommunitiesPanel()
   {
      return m_communitiesPanel;
   }

   /**
    * Get the general panel.
    *
    * @return <code>SearchViewGeneralPanel</code> general panel, never <code>null</code>.
    */
   public SearchViewGeneralPanel getGeneralPanel()
   {
      return m_generalPanel;
   }
   /**
    * A service method to set mnemonics on a tab panel
    * This method can be used if the convention of resourcename.mn is used 
    * for adding mnemonics in the resource bundle
    * @param resId the resource id from the bundle cannot be <code>null</code>
    * @param tabIx is the tab index on which a mnemonic has to be set and it
    * cannot be <code>null</code>
    */
   private void setMnemonicForTabIndex(String resId, int tabIx)
   {
       char mnemonic;
       String tabName = ms_res.getString(resId);
       mnemonic = ms_res.getString(resId+".mn").charAt(0);
       int ix = tabName.indexOf(mnemonic);
       char upperMnemonic = (""+mnemonic).toUpperCase().charAt(0);
       m_tabPane.setMnemonicAt(tabIx, (int)upperMnemonic);
       m_tabPane.setDisplayedMnemonicIndexAt(tabIx, ix);
   }

   
   /**
    * Panel intitialized in {@link #init(PSSearchCollection)}, never modified 
    * after that, never <code>null</code>.
    */
   private SearchViewGeneralPanel m_generalPanel;

   /**
    * All panels implement {@link #IPSDbComponentUpdater} to provide
    * visualization/modification of the particular db component that
    * is active.
    */

   /**
    * Panel intitialized in {@link #init(PSSearchCollection)}, never modified 
    * after that, never <code>null</code>.
    */
   private SearchViewCommunitiesPanel m_communitiesPanel;

   /**
    * Panel intitialized in {@link #init(PSSearchCollection)}, never modified 
    * after that, never <code>null</code>.
    */
   private SearchViewQueriesPanel m_queryPanel;

   /**
    * Panel intitialized in {@link #init(PSSearchCollection)}, never modified 
    * after that, never <code>null</code>.
    */
   private SearchViewPropertiesPanel m_propertiesPanel;

   /**
    * Panel intitialized in {@link #init(PSSearchCollection)}, never modified 
    * after that, never <code>null</code>.
    */
   private SearchViewURLPanel m_urlPanel;

   /**
    * Panel intitialized in {@link #init(PSSearchCollection)}, never modified 
    * after that, never <code>null</code>.
    */
   private SearchQueryPanel m_searchQueryPanel;

   /**
    * Resource bundle for this class. Initialized in 
    * {@link #init(PSSearchCollection)}.  Never <code>null</code> or modified
    * after that.
    */
   private static ResourceBundle ms_res;

   /**
    * Tabbed pane containing all panels used to edit a search/view. Initialized
    * in {@link #init(PSSearchCollection)}, never <code>null</code>
    */
   private JTabbedPane m_tabPane = null;
   
   /**
    * Panel to display message if searches are disabled.  Inialized in 
    * {@link #init(PSSearchCollection)}, never <code>null</code> or modified
    * after that.
    */
   private JPanel m_disabledPanel;
   
   /**
    * Holds the currently displayed component, either the {@link #m_tabPane} or 
    * the {@link #m_disabledPanel}.  Modified by 
    * {@link #setCurComp(JComponent)}, never <code>null</code> after first
    * call to that method.
    */
   private Component m_curComp = null;

   /**
    * Parent frame to notify for events.
    * Never <code>null</code> passed in ctor.
    */
   private SearchViewDialog m_parentFrame = null;

   /**
    * Indicates which type of tab panel is currently enabled. Modified by any
    * of the <code>enableXXXSearchTabs()</code> methods.
    */
   private short m_enabledTabPanelType = 0;

   /**
    * Standard view tab panel type
    */
   public static final short STANDARD_VIEW_TAB_PANEL = 1;

   /**
    * Custom view tab panel type
    */
   public static final short CUSTOM_VIEW_TAB_PANEL = 2;

   /**
    * Standard search tab panel type
    */
   public static final short STANDARD_SEARCH_TAB_PANEL = 3;

   /**
    * Custom search tab panel type
    */
   public static final short CUSTOM_SEARCH_TAB_PANEL = 4;
   
}

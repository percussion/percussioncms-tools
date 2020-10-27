/* *****************************************************************************
 *
 * [ SearchQueryPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.search.ui.PSSearchAdvancedPanel;
import com.percussion.search.ui.PSSearchFieldEditor;
import com.percussion.search.ui.PSSearchSimplePanel;
import com.percussion.util.PSRemoteRequester;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.UTComponents.UTFixedHeightTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * A panel with one main component. A tree that represents the content
 * selected by a <code>IPSContentSelector</code>.
 */
public class SearchQueryPanel extends JPanel implements
   IPSDbComponentUpdater, ActionListener
{
   /**
    * Default constructor with parent frame for central event
    * notification if needed.
    *
    * @param parentFrame. Never <code>null</code>.
    * @throws IllegalArgumentException if <code>parentFrame</code> is <code>null</code>.
    */
   public SearchQueryPanel(SearchViewDialog parentFrame)
   {
      if (parentFrame == null)
         throw new IllegalArgumentException("parent frame must not be null");

      m_parentFrame = parentFrame;
      init();
   }

   /**
    * Does nothing.
    */
   public void onDataPersisted()
   {}

   /**
    * Initialize the panel and all contained components.
    */
   private void init()
   {
      // N.B. may be reentrant
      removeAll();

      // 'this' panel Y_AXIS layout
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

      // create custom URL field
      // Third row
      m_customUrlPanel = new  JPanel();
      m_customUrlPanel.setLayout(new BoxLayout(m_customUrlPanel, 
         BoxLayout.X_AXIS));
      m_customUrl = new UTFixedHeightTextField();
      String labelText = ms_res.getString("customurl");
      char mn = ms_res.getString("customurl.mn").charAt(0);
      JLabel label = new JLabel(labelText);
      label.setDisplayedMnemonic(mn);
      label.setDisplayedMnemonicIndex(labelText.indexOf(mn));
      m_customUrlPanel.add(label);
      m_customUrlPanel.add(m_customUrl);
      label.setLabelFor(m_customUrlPanel);
      m_customUrlPanel.setBorder(new EmptyBorder(5, 0, 5, 0));      
      add(m_customUrlPanel);      

      E2Designer workbench = E2Designer.getApp();
      if (workbench == null) // not possible
         throw new IllegalStateException("workbench must not be null");

      PSSearchConfig searchCfg = null;
      try
      {
         PSServerConfiguration cfg = workbench.getMainFrame().getObjectStore()
               .getServerConfiguration();
         searchCfg = cfg.getSearchConfig();
      }
      catch (Exception e)
      {
         throw new IllegalStateException(e.toString()); // not possible
      }
      
      m_searchSimplePanel = new PSSearchSimplePanel(m_isEngineEnabled,
         DisplayFormatCataloger.getAllDisplayFormats(), 
         searchCfg.getMaxSearchResult());
      m_searchAdvancedPanel = new PSSearchAdvancedPanel(m_searchSimplePanel,
            m_isEngineEnabled, E2Designer.isDBCaseSensitive(), 
            searchCfg.isSynonymExpansionRequired());
      add(m_searchSimplePanel);
      add(Box.createVerticalStrut(5));

      if (m_searchPanel != null && m_searchPanel.hasFields())
      {
         add(new JScrollPane(m_searchPanel));
         add(Box.createRigidArea(new Dimension(0, 5)));
      }
      
      add(Box.createVerticalGlue());

      // create customize button and handle all its actions
      // Fourth row
      m_customizePanel = new  JPanel();
      m_customizePanel.setLayout(new BoxLayout(m_customizePanel, BoxLayout.X_AXIS));
      m_customizeButton = new UTFixedButton(ms_res.getString("customize"));
      //this cannot change w/o changing the name of the handler in m_parentFrame
      m_customizeButton.setActionCommand("Customize");
      m_customizeButton.addActionListener(m_parentFrame);
      m_customizeButton.setPreferredSize(SearchViewDialog.BTN_SIZE);
      m_customizeButton.setMnemonic(ms_res.getString("customize.mn").charAt(0));
      m_userCustomizableCheckBox = new JCheckBox(
         ms_res.getString("usercustomizable"));
      m_userCustomizableCheckBox.removeActionListener(this);
      m_userCustomizableCheckBox.addActionListener(this);
      m_userCustomizableCheckBox.setFocusPainted(false);
      m_userCustomizableCheckBox.setMnemonic(
                           ms_res.getString("usercustomizable.mn").charAt(0));  
      m_customizePanel.add(m_userCustomizableCheckBox);
      m_customizePanel.add(Box.createHorizontalStrut(5));
      m_customizePanel.add(m_customizeButton);
      m_customizePanel.add(Box.createHorizontalGlue());

      add(m_customizePanel);
      invalidate();
      validate();
   }

   // see interface for description
   public void actionPerformed(ActionEvent e)
   {
      String strCmd = e.getActionCommand();
      strCmd = strCmd.replace(' ', '_');
      try
      {
         Method m = getClass().getDeclaredMethod("on" + strCmd, (Class[]) null);
         m.invoke(this, (Object[]) null);
      }
      catch (Exception ignore)
      {
        ignore.printStackTrace(System.out);
      }
   }

   /**
    * Event handler for user customizable checkbox
    */
   public void onEnd_User_Customizable()
   {
      //nothing to do for now
   }

   /**
    * Must have a valid number for max results.
    */
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      // Validate the number
      if(!m_searchSimplePanel.onValidateData(isQuiet)
            || !m_searchAdvancedPanel.onValidateData(isQuiet))
      {
         return false;
      }
      PSSearch search = (PSSearch)comp;
      String customUrl = m_customUrl.getText();
      if(search.isCustomSearch() && (customUrl==null || customUrl.trim().length() < 1))
      {
         PSDlgUtil.showErrorDialog(
            ms_res.getString("error.msg.invalidcustomurl"),
            ms_res.getString("error.title.invalidcustomurl"));
         m_customUrl.requestFocus();
         return false;
      }
      return true;
   }

   // see interface for description
   public boolean onUpdateData(
      IPSDbComponent comp, boolean bDirection, boolean isQuiet)
   {
      // Threshold
      if (comp == null)
         throw new IllegalArgumentException(
            "comp must not be null");

      // Threshold - we're expecting a PSSearch cms object
      if (!(comp instanceof PSSearch))
         return true; // we're just not interested but no error has happened

      PSSearch search = (PSSearch) comp;
      m_isEngineEnabled = FeatureSet.isFTSearchEnabled() && 
         search.useExternalSearch();
      if (bDirection)
      {
         // 'view' to object direction
         // Validate the data
         if (!onValidateData(search, isQuiet))
            return false;

         // Save the user customizable option
         if (m_userCustomizableCheckBox.isSelected())
            search.setUserCustomizable(true);
         else
            search.setUserCustomizable(false);

         String url = m_customUrl.getText();
         if(url!=null && url.length() > 0)
            search.setUrl(url);

         // Save the fields
         search.setFields(m_searchPanel.getFields());
      }
      else
      {
         // object to 'view' direction

         // Fill in Search criteria object values then proceed with
         // other controls
         m_searchPanel = setupSearchCriteria(search.getFields());
         
         // now initialize the panel
         init();
         
         if(!search.isCustomSearch())
            remove(m_customUrlPanel);

         if(search.isCustomSearch())
            m_customizePanel.remove(m_userCustomizableCheckBox);

         if(search.isUserCustomizable())
         {
            m_userCustomizableCheckBox.setSelected(true);
         }
         else
         {
            m_userCustomizableCheckBox.setSelected(false);
         }
         if(search.getUrl() != null)
            m_customUrl.setText(search.getUrl());

      }
      m_searchSimplePanel.updateData(bDirection,search);
      m_searchAdvancedPanel.updateData(bDirection, search);
      return true;
   }

   /**
    * Lays out the panels per this collection of search fields.
    *
    * @param c Never <code>null</code>.
    *
    * @return panel to add, never <code>null</code>
    */
   private PSSearchFieldEditor setupSearchCriteria(Iterator c)
   {
      if (c == null)
         throw new IllegalArgumentException("collection must not be null");

      return new PSSearchFieldEditor(c,
         new PSRemoteRequester(E2Designer.getLoginProperties()),
         m_parentFrame.getCEFieldCatalog());
   }

   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private static ResourceBundle ms_res = null;

   // UI Components
   private UTFixedButton m_customizeButton;
   protected JCheckBox m_userCustomizableCheckBox;
   protected UTFixedHeightTextField m_customUrl = null;
   protected JPanel m_customUrlPanel = null;
   protected JPanel m_customizePanel = null;

   /**
    * Search criteria panel, contstructed at runtime for specific
    * component to search field relationships. May be <code>null</code>, 
    * modified by calls to 
    * {@link #onUpdateData(IPSDbComponent, boolean, boolean)}.
    */
   private PSSearchFieldEditor m_searchPanel = null;

   /**
    * Parent frame to notify for events.
    * Never <code>null</code> passed in ctor.
    */
   private SearchViewDialog m_parentFrame = null;
   
   /**
    * It is a panel that handles all the components that need to be displayed
    * when search is in simple format. 
    * Never <code>null</code> after construction.
    */
   private PSSearchSimplePanel m_searchSimplePanel = null;
   
   /**
    * Contains the 'advanced' controls for the search interface. The panel is
    * created by another class. Never <code>null</code> after construction.
    */
   private PSSearchAdvancedPanel m_searchAdvancedPanel;

   /**
    * Flag to indicate whether search engine is available or not.
    */
   private boolean m_isEngineEnabled;

   static
   {
      ms_res = ResourceBundle.getBundle(SearchQueryPanel.class.getName()
            + "Resources", Locale.getDefault());
   }
}

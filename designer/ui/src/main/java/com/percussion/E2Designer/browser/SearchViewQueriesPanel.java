/*******************************************************************************
 *
 * [ SearchViewQueriesPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.search.ui.ApplicationDataComboModel;
import com.percussion.search.ui.PSSearchFieldEditor;
import com.percussion.util.PSRemoteRequester;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
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
public class SearchViewQueriesPanel extends JPanel implements
   IPSDbComponentUpdater, ActionListener
{
   /**
    * Default constructor with parent frame for central event
    * notification if needed.
    *
    * @param parentFrame. Never <code>null</code>.
    */
   public SearchViewQueriesPanel(SearchViewDialog parentFrame)
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

      Dimension rowDim = new Dimension(0,0);

      PSPropertyPanel propertyPanel = new PSPropertyPanel();

      // First row
      if (null == m_displayFormatCombo)
      {
         m_displayFormatCombo = new JComboBox(
            ApplicationDataComboModel.createApplicationDataComboModel(
               DisplayFormatCataloger.getAllDisplayFormats()));
      }

      propertyPanel.addPropertyRow(ms_res.getString("displayformat"), 
            new JComponent[] {m_displayFormatCombo}, m_displayFormatCombo,
            ms_res.getString("displayformat.mn").charAt(0), null);

      rowDim.height = m_displayFormatCombo.getPreferredSize().height;

      // Second row
      JPanel p = new  JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
      m_unlimitedCheckBox = new JCheckBox(ms_res.getString("unlimited"));
      m_unlimitedCheckBox.setMnemonic(ms_res.getString("unlimited.mn").charAt(0));
      m_unlimitedCheckBox.removeActionListener(this);
      m_unlimitedCheckBox.addActionListener(this);
      m_unlimitedCheckBox.setFocusPainted(false);
      m_maximumText = new UTFixedHeightTextField();
      p.add(m_maximumText);
      p.add(Box.createHorizontalStrut(5));
      p.add(m_unlimitedCheckBox);
      p.add(Box.createHorizontalGlue());
      JComponent[] panelComp = {p};
      propertyPanel.addPropertyRow(ms_res.getString("maximumresults"), 
                  panelComp, m_maximumText, 
                  ms_res.getString("maximumresults.mn").charAt(0), null);
      p.setPreferredSize(rowDim);
      p.setMinimumSize(rowDim);
      p.setMaximumSize(new Dimension(120, 30));

      if (E2Designer.isDBCaseSensitive())
      {
         propertyPanel.addPropertyRow(
            ms_res.getString("caseSensitive"), 
            new JComponent[] {m_caseSensitiveCheckBox},
            m_caseSensitiveCheckBox, 
            ms_res.getString("caseSensitive.mn").charAt(0), null);
      }

      add(propertyPanel);
      add(Box.createRigidArea(new Dimension(0, 5)));

      if (m_ftQuery != null)
      {         
         PSPropertyPanel queryPanel = new PSPropertyPanel();
         JScrollPane queryPane = new JScrollPane(m_ftQuery);
         queryPane.setPreferredSize(new Dimension(300, 100));
         queryPanel.addPropertyRow(ms_res.getString("query") + ":", 
               new JComponent[] {queryPane}, m_ftQuery,
               ms_res.getString("query.mn").charAt(0), null);
         add(queryPanel);
         add(Box.createVerticalStrut(5));
      }

      if (m_searchPanel != null && m_searchPanel.hasFields())
      {
         add(new JScrollPane(m_searchPanel));
         add(Box.createRigidArea(new Dimension(0, 5)));
      }
      add(Box.createVerticalGlue());

      // create cancel button and handle all its actions
      m_customizeButton = new UTFixedButton(ms_res.getString("customize"));
      //this cannot change w/o changing the name of the handler in m_parentFrame
      m_customizeButton.setActionCommand("Customize");
      m_customizeButton.addActionListener(m_parentFrame);
      m_customizeButton.setPreferredSize(SearchViewDialog.BTN_SIZE);
      m_customizeButton.setMnemonic(ms_res.getString("customize.mn").charAt(0));
      add(m_customizeButton);
      invalidate();
      validate();
   }

   // see interface for description
   public void actionPerformed(ActionEvent e)
   {
      String strCmd = e.getActionCommand();
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
    * Event handler for unlimited checkbox
    */
   public void onUnlimited()
   {
      if (m_unlimitedCheckBox.isSelected())
      {
         m_maximumText.setEnabled(false);
         m_maximumText.setText("-1");
      }
      else
      {
         m_maximumText.setEnabled(true);
      }
   }

   /**
    * Must have a valid number for max results.
    */
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      // Validate the number
      String strValue = m_maximumText.getText();

      try
      {
         Integer.parseInt(strValue);
      }
      catch (Exception e)
      {
         if(!isQuiet)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.invalidmaxtext"),
               ms_res.getString("error.title.invalidmaxtext"));
         }
         m_maximumText.requestFocus();
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

      if (bDirection)
      {
         // 'view' to object direction
         // Validate the data
         if (!onValidateData(search, isQuiet))
            return false;

         // Save the diplay format id
         ApplicationDataComboModel model = (ApplicationDataComboModel)
            m_displayFormatCombo.getModel();
         String strDisplayId = model.getSelectedId();

         if (strDisplayId == null || strDisplayId.trim().length() == 0)
         {
            m_displayFormatCombo.setSelectedIndex(0);
            model = (ApplicationDataComboModel)
               m_displayFormatCombo.getModel();
            strDisplayId = model.getSelectedId();
         }

         search.setDisplayFormatId(strDisplayId);

         // Save the result set size
         if (m_unlimitedCheckBox.isSelected())
         {
            search.setMaximumNumber(-1);
         }
         else
         {
            String strValue = m_maximumText.getText();
            try
            {
               int nNum = Integer.parseInt(strValue);
               search.setMaximumNumber(nNum);
            }
            catch (Exception e)
            {
               //shouldn't get here if validation was performed
               search.setMaximumNumber(PSSearch.DEFAULT_MAX);
            }
         }

         boolean caseSensitive =
            (m_caseSensitiveCheckBox.isSelected() ? true : false);
         search.setCaseSensitive(caseSensitive);

         // Save the fields
         search.setFields(m_searchPanel.getFields());
         
         // save ft query if specified
         if (m_ftQuery != null)
         {
            String ftq = m_ftQuery.getText();
            // if none specified, remove the property (can't set it to empty)
            if (ftq == null || ftq.trim().length() == 0)          
               search.removeProperty(PSSearch.PROP_FULLTEXTQUERY, null);
            else
               search.setProperty(PSSearch.PROP_FULLTEXTQUERY, ftq);
         }
         
         // if any fields are specified, set advanced mode to true so they will
         // be used and displayed
         if(FeatureSet.isFTSearchEnabled())
         {
            search.setProperty(PSSearch.PROP_SEARCH_MODE, 
               search.getFields().hasNext() ? PSSearch.SEARCH_MODE_ADVANCED : 
               PSSearch.SEARCH_MODE_SIMPLE);
         }

      }
      else
      {
         // object to 'view' direction

         // add FTS query panel if engine is available and not a custom search
         if (FeatureSet.isFTSearchEnabled() && search.useExternalSearch())
         {
            // set up query text area
            m_ftQuery = new JTextArea();
            m_ftQuery.setLineWrap(true);
            m_ftQuery.setWrapStyleWord(true);
         
            // get previous value
            m_ftQuery.setText(search.getProperty(PSSearch.PROP_FULLTEXTQUERY));
         }
         else
            m_ftQuery = null;
         // Fill in Search criteria object values then proceed with
         // other controls
         m_searchPanel = setupSearchCriteria(search.getFields());
         
         // now initialize the panel
         init();

         ((ApplicationDataComboModel) m_displayFormatCombo.getModel()).
            setSelectedId(search.getDisplayFormatId());

         int nMax = search.getMaximumResultSize();

         if (nMax < 1)
         {
            m_maximumText.setText("-1");
            m_maximumText.setEnabled(false);
            m_unlimitedCheckBox.setSelected(true);
         }
         else
         {
            m_unlimitedCheckBox.setSelected(false);
            m_maximumText.setText(Integer.toString(nMax));
         }

         m_caseSensitiveCheckBox.setSelected(search.isCaseSensitive());

         // enabled customize

      }

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
   protected JComboBox m_displayFormatCombo;
   protected UTFixedHeightTextField m_maximumText;
   protected JCheckBox m_unlimitedCheckBox;

   /**
    * Check box for determining if the query should treat text data in
    * case-sensitive manner. Never <code>null</code>. Never modified after
    * initialization.
    */
   private JCheckBox m_caseSensitiveCheckBox = new JCheckBox();

   /**
    * Search criteria panel, contstructed at runtime for specific
    * component to search field relationships. Never <code>null</code>.
    */
   private PSSearchFieldEditor m_searchPanel = null;

   /**
    * Parent frame to notify for events.
    * Never <code>null</code> passed in ctor.
    */
   private SearchViewDialog m_parentFrame = null;
   
   /**
    * The text area to enter the full text search query. May be 
    * <code>null</code>, modified by calls to 
    * {@link #onUpdateData(IPSDbComponent, boolean, boolean)}.
    */
   private JTextArea m_ftQuery = new JTextArea();

   static
   {
      ms_res = ResourceBundle.getBundle(SearchViewQueriesPanel.class.getName()
            + "Resources", Locale.getDefault());
   }
}

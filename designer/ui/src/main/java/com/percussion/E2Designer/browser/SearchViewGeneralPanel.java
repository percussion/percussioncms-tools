/* *****************************************************************************
 *
 * [ SearchViewGeneralPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.search.ui.ApplicationDataComboModel;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * A panel with one main component. A tree that represents the content
 * selected by a <code>IPSContentSelector</code>.
 */
public class SearchViewGeneralPanel extends JPanel
   implements IPSDbComponentUpdater
{
   /**
    * Default constructor with parent frame for central event
    * notification if needed.
    *
    * @param parentFrame. Never <code>null</code>.
    */
   public SearchViewGeneralPanel(SearchViewDialog parentFrame)
   {
      if (parentFrame == null)
         throw new IllegalArgumentException(
            "parent frame must not be null");

      m_parentFrame = parentFrame;
      init();
   }

   /**
    * Disables the internal name field;
    */
   public void onDataPersisted()
   {
      m_internalNameText.setEnabled(false);
   }


   /**
    * Initialize the panel and all contained components.
    */
   private void init()
   {
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

      m_propertyPanel = new PSPropertyPanel();
      m_propertyPanel.setAlignmentX(PSPropertyPanel.RIGHT_ALIGNMENT);
      mainPanel.add(m_propertyPanel);

      // Label text field and label
      m_labelText = new UTFixedHeightTextField();
      m_propertyPanel.addPropertyRow(ms_res.getString("displayname.label"),
               new JComponent[] {m_labelText}, m_labelText,
               ms_res.getString("displayname.label.mn").charAt(0), null);

      // Internal name text field and label
      m_internalNameText = new UTFixedHeightTextField();
      m_propertyPanel.addPropertyRow(ms_res.getString("internalname.label"),
            new JComponent[] {m_internalNameText}, m_internalNameText,
            ms_res.getString("internalname.label.mn").charAt(0), null);

      m_parentCategoryCombo = new JComboBox(
            ApplicationDataComboModel.createApplicationDataComboModel(
               ParentCategoryCataloger.getAllParentCategories()));

      m_propertyPanel.addPropertyRow(ms_res.getString("category.label"),
               new JComponent[] {m_parentCategoryCombo}, m_parentCategoryCombo,
               ms_res.getString("category.label.mn").charAt(0), null);

      if (FeatureSet.isFTSearchEnabled())
      {
         m_modeCombo = new JComboBox();
         List<String> entries = new ArrayList<String>();
         for (int i = 0; i < MODE_MAP.length; i++)
         {
            entries.add(MODE_MAP[i][1]);
         }
         Collections.sort(entries);
         
         Iterator labels = entries.iterator();
         while (labels.hasNext())
            m_modeCombo.addItem(labels.next());
         
         m_modeLabelText = ms_res.getString("mode.label");
         m_propertyPanel.addPropertyRow(m_modeLabelText,
               new JComponent[] {m_modeCombo}, m_modeCombo,
               ms_res.getString("mode.label.mn").charAt(0), null);;
      }

      //Description panel
      JPanel descPanel = new JPanel();
      descPanel.setLayout(new BorderLayout());

      descPanel.setBorder(PSDialog.createGroupBorder(ms_res.getString(
         "description.label")));
      m_desc.setLineWrap(true);
      m_desc.setWrapStyleWord(true);
      m_desc.setEditable(true);
      JScrollPane areaScrollPane = new JScrollPane(m_desc);
      areaScrollPane.setPreferredSize(new Dimension(100, 100));
      descPanel.add(areaScrollPane, BorderLayout.CENTER);

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(mainPanel);
      add(Box.createVerticalStrut(5));
      add(descPanel);
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);
   }

   // see interface for description
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      String strLabel = m_labelText.getText();
      String strName = m_internalNameText.getText();

      if (strLabel.trim().length() == 0)
      {
         if(!isQuiet)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.missingdisplayname"),
               ms_res.getString("error.title.missingdisplayname"));
         }
         m_labelText.requestFocus();
         return false;
      }

      // Validate data length
      if (strLabel.length() > PSSearch.DISPLAYNAME_LENGTH)
      {
         if(!isQuiet)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.dataexceedlength"),
               ms_res.getString("error.title.dataexceedlength"));
         }
         m_labelText.requestFocus();
         return false;
      }

      // is this a unique internal name:
      if (!isUnique((PSSearch)comp, strName))
      {
         if(!isQuiet)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("err.internalnamenotnunique.msg"),
               ms_res.getString("error.title.invalidinternalname"));
         }
         m_internalNameText.requestFocus();
         return false;
      }

      if (strName.trim().length() == 0)
      {
         if(!isQuiet)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.missinginternalname"),
               ms_res.getString("error.title.missinginternalname"));
         }
         m_internalNameText.requestFocus();
         return false;
      }

      // Validate data length
      if (strName.length() > PSSearch.INTERNALNAME_LENGTH)
      {
         if(!isQuiet)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.dataexceedlength"),
               ms_res.getString("error.title.dataexceedlength"));
         }
         m_internalNameText.requestFocus();
         return false;
      }

      // Attempt to tokenize the internal name
      // to detect any spaces
      StringTokenizer st = new StringTokenizer(strName);

      if (st.countTokens() > 1)
      {
         if(!isQuiet)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.nospacesallowedinternalname"),
               ms_res.getString("error.title.invalidinternalname"));
         }
         m_internalNameText.requestFocus();
         return false;
      }

      String description = m_desc.getText();
      if (description.length() > PSSearch.DESCRIPTION_LENGTH)
      {
         if(!isQuiet)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.dataexceedlength"),
               ms_res.getString("error.title.dataexceedlength"));
         }
         m_desc.selectAll();
         m_desc.requestFocus();
         return false;
      }
      return true;
   }

   /**
    * This method determines if the supplied <code>internalName</code> is
    * unique by performing two checks.  The first check is to iterate through
    * the search collection to see if the <code>internalName</code> exists.  If
    * it does not, the second check will not be performed and <code>true</code>
    * will be returned.  If the <code>internalName</code> does exist a check
    * will get the <code>PSSearch</code> from the collection using the
    * <code>internalName</code> and then compare that <code>PSSearch</code>
    * with the supplied loadedSearch (using the == method).  If they are the
    * same object then <code>true</code> is returned, if they are different,
    * meaning that another <code>PSSearch</code> has the same
    * <code>internalName</code>, <code>false</codd> is returned.
    *
    * @param internalName the string for which to check in the
    *  <code>PSSearchCollection</code>.  Assumed not <code>null</code> or empty.
    *
    * @param loadedSearch the PSSearch on which to perform the second check.
    * Assumed not <code>null</code>.
    *
    * @return see method description above.
    */
   private boolean isUnique(PSSearch loadedSearch, String internalName)
   {
      Iterator itr = m_parentFrame.getSearchCollection();
      PSSearch theSearch = null;
      boolean isUnique = true;

      while(itr.hasNext() && isUnique)
      {
         theSearch = (PSSearch)itr.next();
         if(internalName.equalsIgnoreCase(theSearch.getInternalName()) &&
            theSearch != loadedSearch)
         {
            isUnique = false;
         }
      }
      return isUnique;
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
         // Validate the data
         if (!onValidateData(search, isQuiet))
            return false;

         // 'view' to object direction
         ApplicationDataComboModel model = (ApplicationDataComboModel)
            m_parentCategoryCombo.getModel();
         String strId = model.getSelectedId();
         search.setParentCategory(Integer.parseInt(strId));

         search.setDisplayName(m_labelText.getText());
         search.setInternalName(m_internalNameText.getText());
         if (FeatureSet.isFTSearchEnabled() && search.useExternalSearch())
         {
            search.setProperty(PSSearch.PROP_SEARCH_MODE, 
                  getModeValue(m_modeCombo.getSelectedItem().toString()));
         }
         search.setDescription(m_desc.getText());
      }
      else
      {
         // object to 'view' direction
         m_labelText.setText(search.getDisplayName());
         m_internalNameText.setText(search.getInternalName());

         //the internal name field is always disabled once it has been saved.
         m_internalNameText.setEnabled(!search.isPersisted());

         if (FeatureSet.isFTSearchEnabled() && search.useExternalSearch())
         {
            String searchMode = search.getProperty(
               PSSearch.PROP_SEARCH_MODE);
               
            // default mode to simple if not defined
            if (searchMode == null || searchMode.trim().length() == 0)
               searchMode = PSSearch.SEARCH_MODE_SIMPLE;
            m_modeCombo.setSelectedItem(getModeLabel(searchMode));
            
            m_propertyPanel.showRow(m_modeLabelText);
         }
         else if (m_modeLabelText != null)
         {
            m_propertyPanel.hideRow(m_modeLabelText);
         }

         m_desc.setText(search.getDescription());

         ((ApplicationDataComboModel) m_parentCategoryCombo.getModel()).
            setSelectedId(Integer.toString(search.getParentCategory()));
      }

      return true;
   }

   /**
    * Convenience method that calls {@link #translateMode(int, String)
    * translateMode(0, displayLabel)}.
    */
   private String getModeLabel(String modeValue)
   {
      return translateMode(0, modeValue);
   }

   /**
    * Convenience method that calls {@link #translateMode(int, String)
    * translateMode(1, displayLabel)}.
    */
   private String getModeValue(String displayLabel)
   {
      return translateMode(1, displayLabel);
   }
   
   /**
    * Compares text to the text in MODE_MAP at sourceIndex for each entry and
    * returns the text at the other index.
    * 
    * @param sourceIndex either 0 or 1
    * @param text An entry from the map that corresponds to the supplied
    * index. Assumed not <code>null</code>.
    * 
    * @return What was found in the map. A RuntimeException is thrown if a 
    * match is not found.
    */
   private String translateMode(int sourceIndex, String text)
   {
      //algorithm depends on this assumption
      if (MODE_MAP.length != 2)
         throw new IllegalStateException("MODE_MAP must have 2 entries.");
      if (sourceIndex < 0 || sourceIndex >= MODE_MAP.length)
      {
         throw new IllegalArgumentException(
               "sourceIndex must be valid index into MODE_MAP.");
      }
         
      String result = null;
      for (int i=0; i < MODE_MAP.length; i++)
      {
         if (text.equals(MODE_MAP[i][sourceIndex]))
            result = MODE_MAP[i][sourceIndex == 0 ? 1 : 0];
      }
      if (null == result)
      {
         throw new RuntimeException(
               "Supplied value not in map.");
      }
      return result;
   }

   /**
    * The control used for editing text. Never <code>null</code>. Configured
    * in init().
    */
   private JTextArea m_desc = new JTextArea();

   /**
    * Resource bundle for this class. Initialised in the static block.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private static ResourceBundle ms_res = null;

   /**
    * Reference to the parent frame.  Initialized in the ctor.  Never
    * <code>null</code> and invariant.
    */
   private SearchViewDialog m_parentFrame = null;

   /**
    * Panel containing the general properties of this search, Initialized during
    * {@link #init()}, never <code>null</code> or modified after that.  
    */
   private PSPropertyPanel m_propertyPanel;
   
   /**
    * Control for the search mode setting (as droplist). Initialized during
    * {@link #init()}, may be <code>null</code> if full text search is not
    * enabled.
    */
   private JComboBox m_modeCombo = null;

   /**
    * The label text for {@link #m_modeCombo}, Initialized during
    * {@link #init()}, may be <code>null</code> if full text search is not
    * enabled, never empty.
    */
   private String m_modeLabelText;

   /**
    * Component for the label of the search. Initialized in {@link #init()}.
    * Never <code>null</code>.
    */
   private UTFixedHeightTextField m_labelText;

   /**
    * Component for the internal name of the search. Initialized in
    * {@link #init()}. Never <code>null</code>.
    */
   private UTFixedHeightTextField m_internalNameText;

   /**
    * Component for the parent category of the search. Initialized in
    * {@link #init()}. Never <code>null</code>.
    */
   private JComboBox m_parentCategoryCombo;

   /**
    * Contains the map between the <code>PSSearch</code> object constants
    * and their display label. For each entry, the first element is the
    * constant and the 2nd element is the display label.
    */
   private static String[][] MODE_MAP;
   
   static 
   {
      ms_res = ResourceBundle.getBundle(SearchViewGeneralPanel.class.getName() 
            + "Resources", Locale.getDefault());

      MODE_MAP = new String[][]
      {
         { PSSearch.SEARCH_MODE_SIMPLE, ms_res.getString("mode.simple")},
         { PSSearch.SEARCH_MODE_ADVANCED, ms_res.getString("mode.advanced")
         }
      };
   }
}

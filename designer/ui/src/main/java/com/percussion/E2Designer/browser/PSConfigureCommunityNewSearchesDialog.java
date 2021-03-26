/******************************************************************************
 *
 * [ PSConfigureCommunityNewSearchesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchCollection;
import com.percussion.util.PSMapPair;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This feature is being added as part of cross-site linking.
 * <p>This class provides a dialog box for configuring new Content Explorer 
 * and Related Content searches based on community. Before this class was 
 * added, exactly 1 search had the property of being the default CX new search 
 * or the default RC new search (or both). Now, this can be configured based 
 * on the community. 
 * <p>There will always be at least 1 search that is default for new searches,
 * regardless of community. This guarantees we have a search available in case
 * new communities are added or a community is not cofigured. But it will also
 * be possible to specify different defaults for different communities.
 * <p>This configuration is done outside the scope of any particular search.
 * Based on the values entered in this dialog, the appropriate searches are
 * modified to indicate if they are the specified search when a new search
 * is activated.
 */
public class PSConfigureCommunityNewSearchesDialog extends PSDialog
{
   /**
    * Ctor that takes frame instead of dialog.
    */
   public PSConfigureCommunityNewSearchesDialog(Frame parent, 
         PSSearchCollection searches)
   {
      super(parent);
      initDialog(searches);
   }
   
   
   /**
    * Ctor. Takes search collection.
    * 
    * @param searches search collection, must not be <code>null</code> or
    *           empty.
    */
   public PSConfigureCommunityNewSearchesDialog(JDialog parent, 
         PSSearchCollection searches)
   {
      super(parent);
      initDialog(searches);
   }

   private void initDialog(PSSearchCollection searches)
   {
      
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         //This should not happen as it comes from the swing package
         e.printStackTrace();
      }

      if (searches == null || searches.isEmpty())
      {
         throw new IllegalArgumentException("searches must be null or empty");
      }
      //Filter out searches that are not real searches, i.e. views.
      m_searches = getFilteredSearches(searches);

      //Load resources for the dialog if not loaded yet.
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
               Locale.getDefault());

      //Load all communities from the server
      m_allCommunities = CommunitiesCataloger.getAllCommunities();

      //Build the map of community and applicable searches.
      m_communitySearchMap = buildCommunitySearchMap(m_searches);
      m_tableModel = new CommunityNewSearchTableModel();
      m_communityNewSearchTable.setModel(m_tableModel);

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BorderLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
      
      Box tablePanel = new Box(BoxLayout.Y_AXIS);
      tablePanel.add(Box.createVerticalStrut(5));
      JLabel tableLabel = new JLabel(ms_res.getString("table.title"));
      tableLabel.setAlignmentX(0.5f);
      tablePanel.add(tableLabel, BorderLayout.NORTH);
      JScrollPane jsp = new JScrollPane(m_communityNewSearchTable);
      jsp.getViewport()
            .setBackground(m_communityNewSearchTable.getBackground());
      tablePanel.add(jsp, BorderLayout.CENTER);
      mainPanel.add(tablePanel, BorderLayout.CENTER);

      JPanel commandPanel = createCommandPanel(SwingConstants.HORIZONTAL, true);
      commandPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
      mainPanel.add(commandPanel, BorderLayout.SOUTH);

      PSPropertyPanel defaultsPanel = new PSPropertyPanel();
      String title = ms_res.getString("default_new_search_group_title.label");
      defaultsPanel.setBorder(BorderFactory.createTitledBorder(null, title));
      String label = ms_res.getString("label.new_cx_search");
      char mnemonic = ms_res.getString("default_cx_search.mnemonic").charAt(0);
      defaultsPanel.addPropertyRow(label, m_cxNewSearch, mnemonic);
      label = ms_res.getString("label.new_rc_search");
      mnemonic = ms_res.getString("default_rc_search.mnemonic").charAt(0);
      defaultsPanel.addPropertyRow(label, m_rcNewSearch, mnemonic);
      mainPanel.add(defaultsPanel, BorderLayout.NORTH);
      
      // configure the default search controls
      SearchWrapper defaultCxSearch = null;
      SearchWrapper defaultRcSearch = null;      
      Iterator searchIter = m_searches.iterator();
      while (searchIter.hasNext())
      {
         PSSearch search = (PSSearch) searchIter.next(); 
         if (search.isCXNewSearch())
            defaultCxSearch = new SearchWrapper(search);
         if (search.isAADNewSearch())
            defaultRcSearch = new SearchWrapper(search);
      }
      populateComboList(m_cxNewSearch, null, defaultCxSearch, false);
      populateComboList(m_rcNewSearch, null, defaultRcSearch, false);
      
      m_communityNewSearchTable.getColumnModel().getColumn(1).setCellEditor(
            new DefaultCellEditor(m_comboBoxSearches));
      m_communityNewSearchTable.getColumnModel().getColumn(2).setCellEditor(
            new DefaultCellEditor(m_comboBoxSearches));

      for (int i = 0; i < m_communityNewSearchTable.getRowCount(); i++)
      {
         Object obj = m_communityNewSearchTable.getValueAt(i, 0);
         if (obj instanceof PSComparablePair)
         {
            String commId = ((PSComparablePair) obj).getKey().toString();
            searchIter = m_searches.iterator();
            SearchWrapper cxVal = null;
            SearchWrapper rcVal = null;
            while (searchIter.hasNext())
            {
               PSSearch search = (PSSearch) searchIter.next(); 
               if (search.isCXNewSearch(commId))
                  cxVal = new SearchWrapper(search);
               if (search.isAADNewSearch(commId))
                  rcVal = new SearchWrapper(search);
            }
            m_communityNewSearchTable.setValueAt(cxVal == null 
                  ? (Object) getDefaultEntryText() : (Object) cxVal, i, 1);
            m_communityNewSearchTable.setValueAt(rcVal == null 
                  ? (Object) getDefaultEntryText() : (Object) rcVal, i, 2);
         }
      }
      
      try 
      {
         m_originalTableModel = 
               (CommunityNewSearchTableModel) m_tableModel.clone();
      } 
      catch (CloneNotSupportedException e1) 
      {
         // ignore as we know it is supported
      }
      getContentPane().add(mainPanel);
      pack();
      center();
      setSize(600, 300);
      setResizable(true);
   }

   /**
    * Filter the supplied search collection to remove searches that are not
    * standard searches or custom searches.
    * 
    * @param searches Search collection, assumed not <code>null</code> or
    *           empty.
    * @return a new filtered search collection. May be empty but never
    *         <code>null</code>.
    */
   private PSSearchCollection getFilteredSearches(PSSearchCollection searches)
   {
      try 
      {
         PSSearchCollection coll = new PSSearchCollection();
         Iterator searchIter = searches.iterator();
         while (searchIter.hasNext())
         {
            PSSearch search = (PSSearch) searchIter.next();
            if (!search.isStandardSearch() && !search.isCustomSearch())
               continue;
            coll.add(search);
         }
         return coll;
      } 
      catch (ClassNotFoundException e) 
      {
         //should never happen, so just convert to runtime exception
         throw new RuntimeException(e.getLocalizedMessage());
      }
   }

   /**
    * Builds a map of community and applicable searches for that community.
    * 
    * @param searches Search collection, must not be <code>null</code> may be
    *           empty.
    * @return Map of communities to their default searches. The
    *         key in the map is the community id as string and the value is a
    *         <cod>List</code> of {@link PSSearch} objects which will never be
    *         <code>null</code> or empty. Never <code>null</code>, may be
    *         empty.
    */
   public Map buildCommunitySearchMap(PSSearchCollection searches)
   {
      if (searches == null)
         throw new IllegalArgumentException("searches must not be null");

      Map communitySearchMap = new HashMap();
      Iterator searchIter = searches.iterator();
      while (searchIter.hasNext())
      {
         PSSearch search = (PSSearch) searchIter.next();
         Iterator commIter = getCommunities(search);
         while (commIter.hasNext())
         {
            String commid = (String) commIter.next();
            List searchList = (List) communitySearchMap.get(commid);
            if (searchList == null)
            {
               searchList = new ArrayList();
               communitySearchMap.put(commid, searchList);
            }
            searchList.add(new SearchWrapper(search));
         }
      }
      return communitySearchMap;
   }

   /**
    * Helper method to get a list of community ids that the supplied search is
    * valid for. This list is built off of the community property (which is
    * multi-valued) of the search object
    * 
    * @param search Search object, must not be <code>null</code>.
    * @return an iterator of community ids (as string) the search is valid for.
    */
   public static Iterator getCommunities(PSSearch search)
   {
      if ( null == search)
      {
         throw new IllegalArgumentException("search cannot be null");  
      }
      List<String> commList = new ArrayList<String>();
      String comms = search.getProperty(PSSearch.PROP_COMMUNITY);
      if (comms == null)
         return Collections.EMPTY_SET.iterator();
      StringTokenizer tokenizer = new StringTokenizer(comms, ",");
      while (tokenizer.hasMoreTokens())
      {
         String commId = tokenizer.nextToken();
         if (commId != null && commId.length() != 0)
            commList.add(commId);
      }
      return commList.iterator();
   }

   /**
    * Overriding the same method in {@link PSDialog}.
    * Validates the community new searches' configuration and modifies in memory
    * search objects. Does not persist on the server.
    */
   public void onOk()
   {
      if (m_cxNewSearch.getSelectedItem() == null 
            || m_rcNewSearch.getSelectedItem() == null)
      {
         JOptionPane.showMessageDialog(this, ms_res
               .getString("error.msg.must_have_defaults"), ms_res
               .getString("error.title.must_have_defaults"),
               JOptionPane.WARNING_MESSAGE);
         return;
      }
      
      Iterator searches = m_searches.iterator();
      //clear all settings and then reset based on the new settings
      while (searches.hasNext())
      {
         PSSearch search = (PSSearch) searches.next();
         search.clearAADNewSearch();
         search.clearCXNewSearch();
      }
      
      /* 
       * each key is a PSSearch, value is a Collection of String containing 
       * community id
       */
      Map communityCxSearches = new HashMap();
      for (int i = 0; i < m_tableModel.getRowCount(); i++)
      {
         Object objComm = m_tableModel.getValueAt(i, 0);
         String commId = ((PSComparablePair) objComm).getKey().toString();
         
         Object objCx = m_communityNewSearchTable.getValueAt(i, 1);
         processSearch(communityCxSearches, objCx, commId);
      }
      processSearch(communityCxSearches, m_cxNewSearch.getSelectedItem(),
            PSSearch.PROP_COMMUNITY_ALL);
      storeResults(communityCxSearches, new DefaultSearchConfigurator()
      {
         public void setDefaultSearch(PSSearch search, int[] communityIds)
         {
            search.setAsCXNewSearch(communityIds);
         }
      });

      Map communityRcSearches = new HashMap();
      for (int i = 0; i < m_tableModel.getRowCount(); i++)
      {
         Object objComm = m_tableModel.getValueAt(i, 0);
         String commId = ((PSComparablePair) objComm).getKey().toString();
         
         Object objRc = m_communityNewSearchTable.getValueAt(i, 2);
         processSearch(communityRcSearches, objRc, commId);
      }
      processSearch(communityRcSearches, m_rcNewSearch.getSelectedItem(),
            PSSearch.PROP_COMMUNITY_ALL);
      storeResults(communityRcSearches, new DefaultSearchConfigurator()
      {
         public void setDefaultSearch(PSSearch search, int[] communityIds)
         {
            search.setAsAADNewSearch(communityIds);
         }
      });
      super.onOk();
   }

   /**
    * Simple helper class that wraps the method used to set the communities
    * for a type of search, such as cx new search and aad new search.
    *
    * @author paulhoward
    */
   private abstract class DefaultSearchConfigurator
   {
      /**
       * Calls one of setAs[type]NewSearch methods of the <code>PSSearch</code>
       * object, as determined by the implementer.
       * 
       * @param search Assumed not <code>null</code>.
       * @param communityIds Assumed to have a length > 0.
       */
      public abstract void setDefaultSearch(PSSearch search, 
            int[] communityIds);
   }
   
   /**
    * A helper method that takes the searches and their associated community  
    * ids from <code>results</code> and saves them to each search using the
    * supplied configurator.
    * 
    * @param results Assumed not <code>null</code>. Each key is a 
    * <code>PSSearch</code>, each value is a <code>Collection</code> of
    * <code>String</code> containing a community id.
    *  
    * @param dsc Used to set the proper type of search, cx or rc.
    */
   private static void storeResults(Map results, DefaultSearchConfigurator dsc)
   {
      Iterator keys = results.keySet().iterator();
      while (keys.hasNext())
      {
         PSSearch search = (PSSearch) keys.next();
         Collection commIds = (Collection) results.get(search);
         int[] numericCommIds = new int[commIds.size()];
         Iterator commIdsIter = commIds.iterator();
         int index = 0;
         while (commIdsIter.hasNext())
         {
            String commId = commIdsIter.next().toString();
            numericCommIds[index++] = Integer.parseInt(commId);
         }
         dsc.setDefaultSearch(search, numericCommIds);
      }
   }
   
   /**
    * Helper method that checks the type of <code>test</code> and if it is
    * a <code>SearchWrapper</code>, uses it's search as a key into 
    * <code>results</code>. If a <code>Collection</code> is found, the
    * supplied community id is added to it, otherwise a new collection is 
    * created and added to <code>results</code> w/ the community id.
    * 
    * @param results Assumed not <code>null</code>. Each key is a 
    * <code>PSSearch</code>, each value is a <code>Collection</code>.
    *  
    * @param test May be <code>null</code>. The possible search to process.
    * 
    * @param commId Assumed to be a valid community id.
    */
   private static void processSearch(Map results, Object test, String commId)
   {
      if (test instanceof SearchWrapper)
      {
         PSSearch search = ((SearchWrapper) test).getSearch();
         Collection communities;
         if (results.containsKey(search))
         {
            communities = (Collection) results.get(search);
         }
         else
         {
            communities = new ArrayList();
            results.put(search, communities);
         }
         communities.add(commId);
      }
   }
   
   /**
    * Overriding the same method in {@link PSDialog}.
    * Warns user if there were any changes that can be lost.
    */
   public void onCancel()
   {
      boolean changed = !m_tableModel.equals(m_originalTableModel);

      if (changed)
      {
         int choice = JOptionPane.showConfirmDialog(this, ms_res
               .getString("error.msg.unsaveddata"), ms_res
               .getString("error.title.unsaveddata"),
               JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
         if (choice == JOptionPane.YES_OPTION)
         {
            onOk();
            return;
         }
         else if (choice == JOptionPane.CANCEL_OPTION)
            return;
      }
      super.onCancel();
   }

   /**
    * This method does the following:
    * <ol>
    *    <li>adds all searches not assigned to any community to the supplied
    *    list</li>
    *    <li>sorts the list</li>
    *    <li>if <code>addDefaultEntry</code> is <code>true</code>, adds
    *    the value returned by {@link #getDefaultEntryText()} and sets it
    *    as the first item in the combo box list</li>
    *    <li>adds the rest of the searches</li>
    *    <li>if <code>selectedValue</code> is not <code>null</code>, it is
    *    used to set the selection on the combo box</li>
    * <ol>
    *
    * @param control The combo box to prepare. Assumed not <code>null</code>.
    * @param searches The base set of searches to which the searches that
    * are not community specific will be added. May be <code>null</code> or
    * empty.
    * @param selectedValue If not <code>null</code>, this will become the
    * selected value in the combo box.
    * @param addDefaultEntry If <code>true</code>, a special entry is added
    * as the first item in the combo box's list.
    */
   private void populateComboList(JComboBox control,
         List<SearchWrapper> searches, Object selectedValue,
         boolean addDefaultEntry)
   {
      if (null == searches)
         searches = new ArrayList<SearchWrapper>();
      
      //which searches are visible in all communities
      Iterator searchIter = m_searches.iterator();
      while (searchIter.hasNext())
      {
         PSSearch search = (PSSearch) searchIter.next();
         boolean visibleToAllComm = true;
         for (PSMapPair pair : m_allCommunities)
         {
            if (!search.isAllowedForCommunity(pair.getKey().toString()))
            {
               visibleToAllComm = false;
               break;
            }
         }
         if (visibleToAllComm)
            maybeAddSearch(searches, search);
      }
      
      Collections.sort(searches);
      control.removeAllItems();
      if (addDefaultEntry)
         control.addItem(getDefaultEntryText());
      Iterator iter = searches.iterator();
      while (iter.hasNext())
         control.addItem(iter.next());
      if (selectedValue != null)
         control.setSelectedItem(selectedValue);
      else if (addDefaultEntry)
         control.setSelectedItem(getDefaultEntryText());
      else
         control.setSelectedItem(null);
   }

   /**
    * Adds the supplied search to the supplied list if it is not already present,
    * first wrapping it in the appropriate class.
    * 
    * @param searches Assumed not <code>null</code>.
    * @param search Assumed not <code>null</code>.
    */
   private void maybeAddSearch(List<SearchWrapper> searches, PSSearch search)
   {
      for (SearchWrapper wrapper : searches)
      {
         if (wrapper.getSearch().getId() == search.getId())
            return;
      }
      searches.add(new SearchWrapper(search));
   }


   /**
    * Builds the string that indicates that the default search will be used
    * for this community. It has leading white space to make it stand out. 
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getDefaultEntryText()
   {
      return "   " + ms_res.getString("any");
   }
   
   /**
    * This class contains a {@link PSSearch} to override the 
    * <code>toString</code> method to show the search display name and provide 
    * a comparison function for sorting the searches in display name order.
    *
    * @author paulhoward
    */
   private class SearchWrapper implements Comparable<SearchWrapper>
   {
      /**
       * Overridden because default ctor can't handle exception being thrown.
       * 
       * @param search Assumed not <code>null</code>.
       */
      public SearchWrapper(PSSearch search)
      {
         m_search = search;
      }
      
      /**
       * @return The value returned by the <code>equals</code> method of the
       * search supplied in the ctor.
       */
      @Override
      public boolean equals(Object o)
      {
         if (null == o || !(o instanceof SearchWrapper))
            return false;
         return m_search.equals(((SearchWrapper)o).getSearch());
      }
      
      /**
       * @return The hashcode of the search supplied in the ctor.
       */
      @Override
      public int hashCode()
      {
         return m_search.hashCode();
      }
      
      /**
       * @return the search supplied in the ctor.
       */
      public PSSearch getSearch()
      {
         return m_search;
      }
      
      /**
       * Compares the display name of the search provided in ctor w/ the 
       * <code>toString</code> of the supplied object.
       * @param o See <code>Comparable</code> interface description.
       * @return See <code>Comparable</code> interface description.
       */
      public int compareTo(SearchWrapper o) 
      {
         return m_search.getDisplayName().compareTo(o.toString());
      }
      
      /**
       * Provides the display name of the search supplied in the ctor.
       */
      @Override
      public String toString()
      {
         return m_search.getDisplayName();
      }
      
      /**
       *  Set in ctor, then never null or modified.
       */
      private PSSearch m_search;
   }
   
   /**
    * Inner class to represent the table model for the community new searches
    * table. It is very specific to this class' needs.
    */
   class CommunityNewSearchTableModel extends DefaultTableModel 
      implements Cloneable
   {
      /**
       * Number of columns in the table.
       */
      private static final int COLUMNS = 3;

      //see base class
      public Object clone()
         throws CloneNotSupportedException
      {
         //We only care about the data, so that's all we clone
         CommunityNewSearchTableModel result = 
               (CommunityNewSearchTableModel) super.clone();
         Vector clone = new Vector();
         Iterator cols = dataVector.iterator();
         while (cols.hasNext())
         {
            clone.add(((Vector)cols.next()).clone());
         }
         result.dataVector = clone;
         return result;
      }
      
      /**
       * <code>o</code> must be an object of this class and the data in each
       * cell in columns 1 and 2 and all rows must match. <code>null</code>
       * and "" are equivalent.
       */
      public boolean equals(Object o)
      {
         if (!(o instanceof CommunityNewSearchTableModel))
            return false;
         CommunityNewSearchTableModel other = (CommunityNewSearchTableModel)o;
         for (int col = 1; col < COLUMNS; col++)
         {
            for (int row = 0; row < getRowCount(); row++)
            {
               Object val1 = getValueAt(row, col);
               Object val2 = other.getValueAt(row, col);
               if (!(((val1 == null || val1.toString().length() == 0) 
                     && (val2 == null || val2.toString().length() == 0))
                     || val1.equals(val2)))
               {
                  return false;
               }
            }
         }
         return true;
      }
      
      /**
       * Generates code of the object. Overrides {@link Object#hashCode().
       */
      @Override
      public int hashCode()
      {
         throw new UnsupportedOperationException("Not Implemented");
      }

      /**
       * Override base class method to return column count.
       */
      public int getColumnCount()
      {
         return COLUMNS;
      }

      /**
       * Override base class method to return column name.
       */
      public String getColumnName(int column)
      {
         switch (column)
         {
            case 0 :
               return ms_res.getString("col.community");
            case 1 :
               return ms_res.getString("col.new_cx_search");
            case 2 :
               return ms_res.getString("col.new_rc_search");
            default :
               return ms_res.getString("col.unknown");
         }
      }

      /**
       * Override base class method to return row count which is the number of
       * all communities to be displayed.
       */
      public int getRowCount()
      {
         return m_allCommunities.size();
      }

      /**
       * Override base class method to return column value
       */
      public Object getValueAt(int row, int column)
      {
         switch (column)
         {
            //Communities column
            case 0 :
               return m_allCommunities.get(row);
            default :
               break;
         }
         return super.getValueAt(row, column);
      }

      /**
       * Override base class method to return if the cell is editable. The
       * first, i.e. community column is not editable.
       */
      public boolean isCellEditable(int row, int column)
      {
         if (column == 0)
            return false;
         return true;
      }
   }

   /**
    * Collection of all real searches i.e. not including the views. Initialized
    * in the ctor. Never <code>null</code> after that. May be empty.
    */
   private PSSearchCollection m_searches = null;

   /**
    * List of all communities in the system. Includes a special community "Any"
    * as the first entry. Never <code>null</code> or empty.
    */
   private List<PSMapPair> m_allCommunities = null;

   /**
    * Map of community and applicable searches for the community built as
    * described in {@link #buildCommunitySearchMap(PSSearchCollection)}.
    */
   private Map m_communitySearchMap = new HashMap();

   /**
    * Community-new searches configuration table. Use table model
    * {@link CommunityNewSearchTableModel}.
    */
   private JTable m_communityNewSearchTable = new JTable()
   {
      @Override
      public Component prepareEditor(TableCellEditor e, int row, int column)
      {
         if (row != -1 && column != -1)
         {
            PSComparablePair community = (PSComparablePair) 
                  m_communityNewSearchTable.getValueAt(row, 0);
            Object commId = community.getKey();
            List searches = new ArrayList();
            Collection tmp = (Collection) m_communitySearchMap.get(commId);
            if (tmp != null)
               searches.addAll(tmp);
            populateComboList(m_comboBoxSearches, searches, 
                  getValueAt(row, column), true);
         }
         return super.prepareEditor(e, row, column); 
      }
   };

   /**
    * Table model used by {@link #m_communityNewSearchTable}.
    * <p>The ANY community will e the first row. Column #1 will have the 
    * CX new search and column #2 will have the RC new search. 
    */
   private CommunityNewSearchTableModel m_tableModel = null;

   /**
    * Stores a copy of {@link #m_tableModel} after it has been fully 
    * initialized. It is not modified after that.
    */
   private CommunityNewSearchTableModel m_originalTableModel = null;
   
   /**
    * The cell editor for second and third columns of the configuration table.
    * The data for this control is a dynamic list of communities valid for that
    * community.
    */
   private JComboBox m_comboBoxSearches = new JComboBox();

   /**
    * Editing control for the default cx new search property. Never 
    * <code>null</code>.
    */
   private JComboBox m_cxNewSearch = new JComboBox();

   /**
    * Editing control for the default rc new search property. Never 
    * <code>null</code>.
    */
   private JComboBox m_rcNewSearch = new JComboBox();
   
   /**
    * Resource bundle for this class. Initialized in ctor. It's not modified
    * after that. Never <code>null</code>, equivalent to a variable declared
    * final.
    */
   private static ResourceBundle ms_res;
}

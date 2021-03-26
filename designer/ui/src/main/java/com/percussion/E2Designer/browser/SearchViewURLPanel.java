/*******************************************************************************
 *
 * [ SearchViewURLPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchCollection;
import com.percussion.search.ui.ApplicationDataComboModel;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Panel that allows editing of a custom application search/view. The
 * user can specify the url to call in the form of 'app/resource'. They
 * also can provide any user parameters or context specific parameters.
 * Any given user parameter overrides a context parameter specified.
 */
public class SearchViewURLPanel extends JPanel
   implements IPSDbComponentUpdater
{
   /**
    * Default constructor.
    *
    * @param searches a collection of searches, not <code>null</code> or,
    *    may be empty.
    */
   public SearchViewURLPanel(PSSearchCollection searches)
   {
      if (searches == null)
         throw new IllegalArgumentException("searches cannot be null");

      init(searches);
   }

   /**
    * Does nothing.
    */
   public void onDataPersisted()
   {}

   /**
    * Initializes the panel.
    *
    * @param searches a collection of searches, assumed not <code>null</code>,
    *    may be empty.
    */
   private void init(PSSearchCollection searches)
   {
      setLayout(new BorderLayout());
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);

      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

      PSPropertyPanel comboPanel = new PSPropertyPanel();
      m_displayFormatCombo = new JComboBox(
         ApplicationDataComboModel.createApplicationDataComboModel(
            DisplayFormatCataloger.getAllDisplayFormats()));

      comboPanel.addPropertyRow(ms_res.getString("displayformat"), 
               new JComponent[] {m_displayFormatCombo}, m_displayFormatCombo,
               ms_res.getString("displayformat.mn").charAt(0), null);

      topPanel.add(comboPanel);
      topPanel.add(Box.createHorizontalStrut(60));
      topPanel.setBorder(emptyBorder);
      add(topPanel, BorderLayout.NORTH);

      List<String> contextParameters = new ArrayList<String>();
      Iterator searchesIt = searches.iterator();
      while (searchesIt.hasNext())
      {
         PSSearch search = (PSSearch) searchesIt.next();
         String url = search.getUrl();
         if (url != null)
         {
            Iterator params =
               PSSearch.parseParameters(url, null).values().iterator();
            while (params.hasNext())
            {
               String param = (String) params.next();
               if (param != null && param.startsWith("$") &&
                  !contextParameters.contains(param))
                  contextParameters.add(param);
            }
         }
      }

      m_urlPanel = new PSUrlPanel(contextParameters.iterator(), false);

      add(m_urlPanel, BorderLayout.CENTER);
   }

   /**
    * Get the full url string.
    *
    * @return the url string, never <code>null</code>, may be empty.
    */
   public String getUrl()
   {
      return m_urlPanel.getUrl();
   }

   /**
    * Set the url.
    *
    * @param url a url string, may be <code>null</code> or empty, can have
    *    parameters attached.
    */
   public void setUrl(String url)
   {
      m_urlPanel.setUrl(url, null);
   }

   // see interface for description
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      boolean isValid =
         m_urlPanel.isValid(m_urlPanel.getUrl(), PSSearch.CUSTOMURL_LENGTH);
      // Display error to user if url is not valid
      if(!isValid && !isQuiet)
      {

         PSDlgUtil.showErrorDialog(ms_res.getString("err.msg.invalid.url"),
                  ms_res.getString("err.title.invalid.url"));
      }

      return isValid;
   }

   // see interface for description
   public boolean onUpdateData(
      IPSDbComponent comp, boolean isSave, boolean isQuiet)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp must not be null");

      if (!(comp instanceof PSSearch))
         return true;

      PSSearch search = (PSSearch) comp;
      if (isSave)
      {
         if (!onValidateData(search, isQuiet))
            return false;

         ApplicationDataComboModel model =
            (ApplicationDataComboModel) m_displayFormatCombo.getModel();
         String strDisplayId = model.getSelectedId();

         if (strDisplayId == null || strDisplayId.trim().length() == 0)
         {
            m_displayFormatCombo.setSelectedIndex(0);
            model = (ApplicationDataComboModel) m_displayFormatCombo.getModel();
            strDisplayId = model.getSelectedId();
         }

         search.setDisplayFormatId(strDisplayId);
         search.setUrl(m_urlPanel.getUrl());
      }
      else
      {
         ApplicationDataComboModel model =
            (ApplicationDataComboModel) m_displayFormatCombo.getModel();
         model.setSelectedId(search.getDisplayFormatId());

         m_urlPanel.setUrl(search.getUrl(), null);
      }

      return true;
   }

   /**
    * Resource bundle for this class. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
   static
   {
      ms_res = ResourceBundle.getBundle(SearchViewURLPanel.class.getName() +
         "Resources", Locale.getDefault());
   }

   /**
    * Combo with data model containing list of display formats
    */
   private JComboBox m_displayFormatCombo = null;

   /**
    * The url panel. Initialized in the ctor. Never <code>null</code>
    * after that.
    */
   private PSUrlPanel m_urlPanel = null;
}

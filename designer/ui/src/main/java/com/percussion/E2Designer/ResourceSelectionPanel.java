/******************************************************************************
 *
 * [ ResourceSelectionPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Panel to allow selection of an application and query resource for url
 * construction.  It consists of two label/combo box pairs:  application name 
 * and resource name.  When an application is selected, the resources combo box 
 * updates to reflect the query pages in that application.
 */
public class ResourceSelectionPanel extends JPanel
{
   /**
    * Construct a instance of this class.
    */
   public ResourceSelectionPanel()
   {
      init();
      setApplications();
   }
   
   /**
    * Gets the string value of the currently selected object in the application
    * name combo box.
    *
    * @return String value of selected application name; <code>null</code> if
    * no selection, never empty.
    */
   public String getApplicationName()
   {
      Object item = m_applicationNameCombo.getSelectedItem();
      return (null == item || item.toString().trim().length() == 0 ? null : 
         item.toString());
   }


   /**
    * Gets the string value of the currently selected object in the resource
    * name combo box.
    *
    * @return String value of selected resource name; <code>null</code> if
    * no selection, never empty.
    */
   public String getRequestPage()
   {
      Object item = m_resourceNameCombo.getSelectedItem();
      return (null == item || item.toString().trim().length() == 0 ? null : 
         item.toString());
   }

   /**
    * Sets the currently selected item in the application combo to the provided 
    * application name if the specified name is in the list.
    * 
    * @param appName The app to select, if <code>null</code>, the first 
    * item in the list is selected if the list is not empty.
    */
   public void setApplicationName(String appName)
   {
      if (appName == null)
      {
         if (m_applicationNameCombo.getItemCount() > 0)
            m_applicationNameCombo.setSelectedIndex(0);
      }
      else
         m_applicationNameCombo.setSelectedItem(appName);
   }
   
   /**
    * Sets the currently selected item in the request page combo to the provided 
    * request page name if the specified name is in the list.
    * 
    * @param requestPage The page to select, if <code>null</code>, the first 
    * item in the list is selected if the list is not empty.
    */
   public void setRequestPage(String requestPage)
   {
      if (requestPage == null)
      {
         if (m_resourceNameCombo.getItemCount() > 0)
            m_resourceNameCombo.setSelectedIndex(0);
      }
      else
         m_resourceNameCombo.setSelectedItem(requestPage);
   }

   /**
    * Initialize all components on this panel.
    */
   private void init()
   {
      // create labels and controls
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());
            
      m_applicationNameCombo = new PSComboBox();
      m_applicationNameCombo.addActionListener( new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onApplicationComboChanged();
         }
      } );
      
      String labelStr = ms_res.getString("application");
      char mn = ms_res.getString("application.mn").charAt(0);
      JLabel applicationNameLabel = new JLabel(labelStr);
      applicationNameLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      applicationNameLabel.setDisplayedMnemonic(mn);
      applicationNameLabel.setLabelFor( m_applicationNameCombo );
      applicationNameLabel.setHorizontalAlignment( SwingConstants.RIGHT );

      m_resourceNameCombo = new PSComboBox();
      labelStr = ms_res.getString("resource");
      mn = ms_res.getString("resource.mn").charAt(0);
      JLabel resourceNameLabel = new JLabel(labelStr);
      resourceNameLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      resourceNameLabel.setDisplayedMnemonic(mn);
      resourceNameLabel.setLabelFor( m_resourceNameCombo );
      resourceNameLabel.setHorizontalAlignment( SwingConstants.RIGHT );

      // layout labels
      JPanel labelPanel = new JPanel();
      GridLayout layout = new GridLayout( 2, 0 );
      layout.setVgap( 5 );
      labelPanel.setLayout( layout );
      labelPanel.add( applicationNameLabel );
      labelPanel.add( resourceNameLabel );

      // add an extra JPanel to prevent the components from growing vertically
      JPanel labelPanelWrap = new JPanel();
      labelPanelWrap.add( labelPanel );

      // layout controls
      JPanel dataPanel = new JPanel( new GridLayout( 2, 0 ) );
      dataPanel.add( m_applicationNameCombo );
      dataPanel.add( m_resourceNameCombo );

      // add an extra JPanel to prevent the components from growing vertically
      JPanel dataPanelWrap = new JPanel();
      dataPanelWrap.add( dataPanel );

      // tie it all together
      setLayout( new BorderLayout( 5, 0 ) );
      setAlignmentX( LEFT_ALIGNMENT );
      add( labelPanelWrap, BorderLayout.WEST );
      add( dataPanelWrap, BorderLayout.CENTER );
   }
   
   /**
    * Sets the application name combo box with a list of application names
    * obtained from cataloger.
    */
   private void setApplications()
   {
      try
      {
         List<IPSReference> appRefs = PSCoreUtils.catalog(
               PSObjectTypes.XML_APPLICATION, false);
         List<String> apps = new ArrayList<String>();
         m_applicationNameCombo.removeAllItems();

         for (IPSReference ref : appRefs)
         {
            apps.add(ref.getName());
         }

         // sort them
         Collections.sort(apps);

         // add them to the combo box
         for (final String s : apps)
         {
            m_applicationNameCombo.addItem(s);
         }
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Catalogs the request pages in the currently selected application and sets 
    * the resource name combo with those pages.
    */
   private void onApplicationComboChanged()
   {
      List<String> pages = new ArrayList<String>();
      String appName = (String) m_applicationNameCombo.getSelectedItem();
      PSObjectStore os = getObjectStore();
      if (os != null && appName != null)
      {
         PSApplication app = null;
         try
         {
            app = os.getApplication( appName, false );
         } catch (PSServerException e)
         {
            PSDlgUtil.showError(e);
         } catch (PSAuthorizationException e)
         {
            PSDlgUtil.showError(e);
         } catch (PSAuthenticationFailedException e)
         {
            PSDlgUtil.showError(e);
         } catch (PSLockedException e)
         {
            // this shouldn't happen as we didn't ask for a lock
            PSDlgUtil.showError(e);
         } catch (PSNotFoundException e)
         {
            PSDlgUtil.showError(e);
         }
         if (app != null)
         {
            PSCollection dataSets = app.getDataSets();
            if (dataSets != null)
            {
               for (Iterator i = dataSets.iterator(); i.hasNext();)
               {
                  PSDataSet dataSet = (PSDataSet) i.next();
                  // only process this dataSet if it is a query resource
                  if (OSDataset.getType( dataSet ) == OSDataset.DST_QUERY)
                  {
                     PSRequestor requestor = dataSet.getRequestor();
                     if (requestor != null)
                        pages.add( requestor.getRequestPage() );
                  }
               }
               Collections.sort( pages );
            }
         }
      }

      setRequestPages( pages );
   }

   /**
    * Sets the resource name combo box with the String objects in the specified
    * Collection.  Any previous entries in the combo box are removed.
    *
    * @param pages Collection of String; assumed not <code>null</code>
    */
   private void setRequestPages(Collection pages)
   {
      m_resourceNameCombo.removeAllItems();
      for (Iterator i = pages.iterator(); i.hasNext();)
      {
         String s = (String) i.next();
         m_resourceNameCombo.addItem( s );
      }
   }

   /**
    * Gets the reference to the E2Designer's object store.
    *
    * @return PSObjectStore, or <code>null</code> if the E2Designer is not
    * running.
    * 
    * @see UIMainFrame#getObjectStore
    * @todo consider refactoring to have object store supplied by caller
    */
   private PSObjectStore getObjectStore()
   {
      PSCoreFactory factory = PSCoreFactory.getInstance();
      PSObjectStore os = new PSObjectStore(factory.getDesignerConnection());
      return os;
   }
   
   /**
    * Contains the names of all Rhythmyx applications.  Initialized in <code>
    * init()</code> and then never <code>null</code>.
    */
   private JComboBox m_applicationNameCombo;

   /**
    * Contains the names of the request pages within the Rhythmyx application
    * selected in {@link #m_applicationNameCombo}.  Initialized in <code>
    * init()</code> and then never <code>null</code>.
    */
   private JComboBox m_resourceNameCombo;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * Never <code>null</code> or modified after that.
    */
   private static ResourceBundle ms_res;
   
}

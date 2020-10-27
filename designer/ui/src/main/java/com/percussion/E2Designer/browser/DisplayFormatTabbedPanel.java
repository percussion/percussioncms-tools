/*******************************************************************************
 *
 * [ DisplayFormatTabbedPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSDisplayFormatCollection;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Contains all of the Display Format tabs.
 */
public class DisplayFormatTabbedPanel extends JPanel
{
   /**
    * Constructs an instance of this class from a
    * <code>DisplayFormatDialog</code>.
    *
    * @param parentDialog  The parent dialog for this tabbed panel.  This
    * specifies a particular dialog reference not the general reference,
    * because some of the data that is needed is specific to the
    * <code>DisplayFormatDialog</code>.  Must not be <code>null</code>.
    */
   DisplayFormatTabbedPanel(DisplayFormatDialog parentDialog)
   {
      if(parentDialog == null)
         throw new IllegalArgumentException("parentDialog must not be null");

      m_parentDialog = parentDialog;
      init();
   }

   /**
    * Creates a panel containing tabbed pane containing editors for the
    * Display Format.
    */
   private void init()
   {
      if (ms_res == null)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());

      m_tabbedPane = new JTabbedPane();

      //general panel
      m_generalPanel = new DisplayFormatGeneralPanel(this);
      m_tabbedPane.add(ms_res.getString("tabname.general"), m_generalPanel);
      setMnemonicForTabIndex("tabname.general", 0);

      //column panel
      m_columnPanel = new DisplayFormatColumnPanel(this);
      m_tabbedPane.add(ms_res.getString("tabname.columns"), m_columnPanel);
      setMnemonicForTabIndex("tabname.columns", 1);

      // communities panel:
      m_communitiesPanel = new DisplayCommunitiesPanel(this.getCommunityList());
      m_tabbedPane.add(ms_res.getString("tabname.communities"), m_communitiesPanel);
      setMnemonicForTabIndex("tabname.communities", 2);
      
      //properties panel
       m_propertiesPanel = new DisplayFormatPropertiesPanel(
         new String[] {ms_res.getString("col.name"),
            ms_res.getString("col.value")}, Integer.parseInt(ms_res.getString(
            "col.rows")));
      m_tabbedPane.add(ms_res.getString("tabname.properties"), m_propertiesPanel);
      setMnemonicForTabIndex("tabname.properties", 3);
      
      setLayout(new BorderLayout());
      add(m_tabbedPane, BorderLayout.CENTER);
      setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
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
       m_tabbedPane.setMnemonicAt(tabIx, (int)upperMnemonic);
       m_tabbedPane.setDisplayedMnemonicIndexAt(tabIx, ix);
   }

   

   /**
    * Calls the validate method on each panel managed by this class until one
    * fails or all succeed.
    *
    * @return <code>true</code> if all controls in all panels have valid data,
    *    <code>false</code> otherwise.
    */
   public boolean validateData()
   {
      if (!m_generalPanel.validateData())
         return false;

      if(!m_columnPanel.validateData())
         return false;

      if (!m_communitiesPanel.validateData())
         return false;

      if (!m_propertiesPanel.validateData())
         return false;

      return true;
   }

   /**
    * Loads data from the <code>PSDisplayFormat</code> into the tabs.
    *
    * @param model must not be <code>null</code>.
    */
   public void loadData(PSDisplayFormat model)
   {
      if(model == null)
         throw new IllegalArgumentException("displayFormat must not be null");

      m_generalPanel.load(model);
      m_columnPanel.load(model);
      m_communitiesPanel.load(model);
      m_propertiesPanel.load(model);

      m_isDataLoaded = true;
   }

   /**
    * Has the data been loaded into the panels?  This will return
    * after <code>true</code> the first time the
    * {@link #loadData(PSDisplayFormat) loadData(PSDisplayFormat)} is called
    * and will the continue to return <code>true</code>.  Returns
    * <code>false</code> only if called before the data has been loaded.
    *
    * @return  see above.
    */
   public boolean isDataLoaded()
   {
      return m_isDataLoaded;
   }

   /**
    * Attempts to save the data currently present in the tabs to the model.
    *
    * @return <code>true</code> if the save has succeeded, otherwise <code>
    * false</code>.
    */
   public boolean save()
   {
      // save properties first in case other panels store data in properties
      if(!m_propertiesPanel.save())
         return false;
      if(!m_generalPanel.save())
         return false;
      if(!m_columnPanel.save())
         return false;
      if(!m_communitiesPanel.save())
         return false;

       return true;
   }

   /**
    * Returns the selected index for the tabbed panel.
    *
    * @return selected index for the tabbed panel
    */
   public int getSelectedTabIndex()
   {
      return m_tabbedPane.getSelectedIndex();
   }

   /**
    * This is a proxy to {@link DisplayFormatDialog#getCommunityList()
    * DisplayFormatDialog.getCommunityList()}
    */
   List getCommunityList()
   {
      return m_parentDialog.getCommunityList();
   }

   /**
    * This is a proxy to {@link DisplayFormatDialog#getCataloger()
    * DisplayFormatDialog.getCataloger()}
    */
   PSContentEditorFieldCataloger getCataloger()
   {
      return m_parentDialog.getCataloger();
   }

   /**
    * This is a proxy to {@link DisplayFormatDialog#getDisplayFormatCollection()
    * DisplayFormatDialog.getDisplayFormatCollection()}
    */
   PSDisplayFormatCollection getDisplayFormatCollection()
   {
      return m_parentDialog.getDisplayFormatCollection();
   }

   /**
    * This is a proxy to {@link DisplayFormatDialog#getProcessorProxy()
    * DisplayFormatDialog.getProcessorProxy()}
    */
   PSProcessorProxy getProcessorProxy()
   {
      return m_parentDialog.getProcessorProxy();
   }

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;

   /**
    * The general panel used by this class.  Initialized in the
    * <code>init()</code>, never <code>null</code> after that and is invariant.
    */
   private DisplayFormatGeneralPanel m_generalPanel = null;

   /**
    * The column panel used by this class.  Initialized in the
    * <code>init()</code>, never <code>null</code> after that and is invariant.
    */
   private DisplayFormatColumnPanel m_columnPanel = null;

   /**
    * The communities panel used by this class.  Initialized in the
    * <code>init()</code>, never <code>null</code> after that and is invariant.
    */
   private DisplayCommunitiesPanel m_communitiesPanel = null;

   /**
    * The properties panel used by this class.  Initialized in the
    * <code>init()</code>, never <code>null</code> after that and is invariant.
    */
   private DisplayFormatPropertiesPanel m_propertiesPanel = null;

   /**
    * Initialized in the ctor and never <code>null</code> after that, and
    * the reference is invariant.
    * @see #DisplayFormatTabbedPanel(DisplayFormatDialog)
    * DisplayFormatTabbedPanel(DisplayFormatDialog)
    */
   private DisplayFormatDialog m_parentDialog = null;

  /**
    * The tabbed panel used by this class.  Initialized in the
    * <code>init()</code>, never <code>null</code> after that and is invariant.
    */
   private JTabbedPane m_tabbedPane = null;

   /**
    * @see isDataLoaded();
    */
   private boolean m_isDataLoaded = false;

}

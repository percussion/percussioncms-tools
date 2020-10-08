/*[ PSStaticItemExtractorPanel.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import java.awt.BorderLayout;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JTabbedPane;

/**
 * The configuration panel for defining the static extractor definition.
 * It contains a tab-panel with "Location" and "Filters" sub-panels.
 */
public class PSStaticItemExtractorPanel extends PSExtractorConfigPanel
{
   /**
    * Creates an item extractor panel.
    */
   public PSStaticItemExtractorPanel()
   {
      super();
   }

   // Implements PSExtractorConfigPanel#initConfigPanel()
   protected JTabbedPane initConfigPanel()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BorderLayout());

      JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.addTab(getResourceString("tab.location"),
         new PSLocationConfigTabPanel());
      tabbedPane.addTab(getResourceString("tab.filters"), 
         new PSFilterConfigTabPanel());
      
      add(tabbedPane, BorderLayout.CENTER);
      
      return tabbedPane;
   }
   
   /**
    * Gets the resource mapping for the supplied key.
    *
    * @param key, may not be <code>null</code>
    * @return mapping corresponding to the key, never <code>null</code>.
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   private String getResourceString(String key)
   {
      return PSContentLoaderResources.getResourceString(ms_res, key);
   }

   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. Never <code>null</code>
    */
   private static ResourceBundle ms_res;
}

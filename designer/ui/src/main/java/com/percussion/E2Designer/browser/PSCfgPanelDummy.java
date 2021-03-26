/******************************************************************************
 *
 * [ PSCfgPanelDummy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.design.objectstore.PSRelationshipConfig;

/**
  * Dummy panel that is shown as a filler.
  * Extracted it from {@link PSRelationshipEditorDialog} for easier comprehension.
  */
 class PSCfgPanelDummy extends PSCfgPanel
 {
   public PSCfgPanelDummy(String view, PSRelationshipEditorDialog owner)
   {
      super(view, owner);
   }

   /**
    * Sets default layout and creates any default comps. such as description box.
    */
   public void preInitPanel()
   { /*noop*/ }

   /**
    * Adds any default comps at the end, also sets default size and titled border.
    */
   public void postInitPanel()
   { /*noop*/ }

   /* (non-Javadoc)
    * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#initPanel()
    */
   public void initPanel()
   {
      //noop
   }

   /* (non-Javadoc)
    * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#updateViewFromData(com.percussion.design.objectstore.PSRelationshipConfig)
    */
   public void updateViewFromData(PSRelationshipConfig cfg)
   {
      //noop
   }

   /* (non-Javadoc)
    * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#validateViewData()
    */
   public boolean validateViewData()
   {
      return true;
   }

   /* (non-Javadoc)
    * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#updateDataFromView()
    */
   public void updateDataFromView()
   {
      //noop
   }
 }

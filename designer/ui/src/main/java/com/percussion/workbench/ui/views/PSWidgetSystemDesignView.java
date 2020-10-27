/*******************************************************************************
 *
 * [ PSWidgetSystemDesignView.java ]
 * 
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.views;

import com.percussion.workbench.ui.views.hierarchy.PSDeclarativeExplorerView;

/**
 * This view is based on the declarative hierarchy view. It provides the name
 * of an xml file that creates a view showing all the design objects that are
 * more advanced and less frequently needed.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSWidgetSystemDesignView extends PSDeclarativeExplorerView
{
   /**
    * The view id as specified in the plugin.xml.
    */
   public static final String ID = 
      "com.percussion.workbench.ui.views.PSWidgetSystemDesignView";

   //see base class
   @Override protected String getRootName()
   {
      return "widget-system";
   }
}

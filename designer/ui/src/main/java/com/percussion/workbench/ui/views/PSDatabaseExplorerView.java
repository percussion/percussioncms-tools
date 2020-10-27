/******************************************************************************
 *
 * [ PSDatabaseExplorerView.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views;

import com.percussion.workbench.ui.views.hierarchy.PSDeclarativeExplorerView;

/**
 * Shows content of datasources.
 * Is based on the declarative hierarchy view.
 * 
 * @author Andriy Palamarchuk
 */
public class PSDatabaseExplorerView extends PSDeclarativeExplorerView
{
   @Override
   protected String getRootName()
   {
      return "databaseExplorer";
   }

   /**
    * The view id as specified in the plugin.xml.
    */
   public static final String ID = 
      "com.percussion.workbench.ui.views.PSDatabaseExplorerView";
}

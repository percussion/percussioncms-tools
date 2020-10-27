/******************************************************************************
 *
 * [ PSFileExplorerView.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views;

import com.percussion.workbench.ui.views.hierarchy.PSDeclarativeExplorerView;

/**
 * Local file system explorer.
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:43:52 PM
 */
public class PSFileExplorerView extends PSDeclarativeExplorerView
{
   /**
    * The view id as specified in the plugin.xml.
    */
   public static final String ID = PSFileExplorerView.class.getName(); 

   // see base class
   protected String getRootName()
   {
      return "localFileExplorer";
   }
}

/******************************************************************************
 *
 * [ PSLegacyExplorerView.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views;

import com.percussion.workbench.ui.views.hierarchy.PSDeclarativeExplorerView;


/**
 * @version 6.0
 * @created 03-Sep-2005 4:43:54 PM
 */
public class PSLegacyExplorerView extends PSDeclarativeExplorerView
{

   public PSLegacyExplorerView()
   {

   }

   /**
    * Derived classes must override this method.
    */
   protected String getRootName()
   {
      return null;
   }

}

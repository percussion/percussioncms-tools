/* *****************************************************************************
 *
 * [ SearchPropertyPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/


package com.percussion.E2Designer.admin;

import java.awt.*;

/**
 * Simple wrapper that contains the knowledge of what tabs make up the main
 * search tab in the admin client.
 *
 * @author paulhoward
 */
public class SearchPropertyPanel extends TabbedPropertyPanel
{
   /**
    * Basic ctor that just creates the appropriate sub-tabs and adds them
    * to this panel. It is assumed that this method is not called unless
    * the <code>E2Designer.FTS_FEATURE</code> is present.
    * <p>If search is disabled, a single tab will show with a message telling
    * the user she is not licensed.
    * 
    * @param parent Never <code>null</code>. Used as parent for sub-dialogs.
    * 
    * @param data The data to be edited. Only the search related data will
    * be touched. Never <code>null</code>.
    */
   public SearchPropertyPanel(Frame parent, ServerConfiguration data)
   {
      if (null == parent)
      {
         throw new IllegalArgumentException("parent frame cannot be null");
      }
      if (null == data)
      {
         throw new IllegalArgumentException("data cannot be null");
      }
      
      String tabLabel = PSServerAdminApplet.getResources().getString(
            "search.ftstab");
      addTab(tabLabel, new SearchGeneralPanel(parent, data));
   }
}

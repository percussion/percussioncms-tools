/*[ XMLBrowser.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.ImageListControl.ImageListItem;

import javax.swing.*;
import java.util.Vector;

/**
 * The xml browser panel.
 */
////////////////////////////////////////////////////////////////////////////////
public class XMLBrowser
{
   XMLBrowser(OSPageDatatank data)
   {
      Vector<ImageListItem> selections = new Vector<ImageListItem>();
      selections.addElement(new ImageListItem(m_pageIcon,
         data.getSchemaSource().toString(), data));
            
      m_browser = new MapBrowser(selections, null, Boolean.FALSE);
      m_browser.initTree(data);
   }
   
   public MapBrowserTree getTree()
   {
      return(m_browser.getTree());
   }
   
   private MapBrowser m_browser = null;
     private ImageIcon m_pageIcon = new ImageIcon(getClass().getResource(
      E2Designer.getResources().getString("gif_PageDatatank")));
}

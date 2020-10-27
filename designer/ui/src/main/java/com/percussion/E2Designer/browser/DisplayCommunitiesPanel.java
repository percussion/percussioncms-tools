/*[ DisplayCommunitiesPanel.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSDisplayFormat;

import javax.swing.table.DefaultTableModel;
import java.util.List;


/**
 * The panel diaplays the communities that are allowed to use the display format
 * (representing one of the nodes in the tree). For each enabled community one
 * row will be added to the table with the actual community as value. If all
 * communities are allowed, the special value 'All' will be set.
 */
public class DisplayCommunitiesPanel extends CommunitiesPanel
{
   /**
    * Constructs the communities panel.
    */
   public DisplayCommunitiesPanel(List communList)
   {
      super(communList);
   }

   public void load(Object data)
   {
      if (data instanceof PSDisplayFormat)
      {
         m_dsFormat = (PSDisplayFormat)data;
         super.load(m_dsFormat.getProperties());
      }
   }

   public boolean save()
   {
      if(!validateData())
         return false;

      DefaultTableModel model = (DefaultTableModel)getTableModel();
      int rows = model.getRowCount();
      String id = null;
      boolean value = false;

      if (isAllSelected())
      {
         m_dsFormat.addCommunity(PSDisplayFormat.PROP_COMMUNITY_ALL);
         return true;
      }

      m_dsFormat.removeCommunity(PSDisplayFormat.PROP_COMMUNITY_ALL);
      for (int  k = 0; k < rows; k++)
      {
         id = (String) ((PSComparablePair) model.getValueAt(k, 0)).getKey();
         value = ((Boolean)model.getValueAt(k, 1)).booleanValue();
         if (id == null || id.trim().length() == 0)
            continue;
         if (value)
            m_dsFormat.addCommunity(id);
         else
            m_dsFormat.removeCommunity(id);
      }
      return true;
   }

   private PSDisplayFormat m_dsFormat;
}
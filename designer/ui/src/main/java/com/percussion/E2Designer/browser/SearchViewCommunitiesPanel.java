/*[ SearchViewCommunitiesPanel.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSSearch;

import javax.swing.table.TableModel;

/**
 * A panel with one main component. A tree that represents the content
 * selected by a <code>IPSContentSelector</code>.
 */
public class SearchViewCommunitiesPanel extends CommunitiesPanel
   implements IPSDbComponentUpdater
{
   /**
    * Consructs the panel.
    * @param listener object implementing change listener interface
    *    to react to tab changes. Never <code>null</code>
    */
   public SearchViewCommunitiesPanel()
   {
      super(CommunitiesCataloger.getAllCommunities());
   }

   /**
    * Does nothing.
    */
   public void onDataPersisted()
   {}

   // see interface for description
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      super.setValidationQuiet(isQuiet);
      return super.validateData();
   }

   // see interface for description
   public boolean onUpdateData(
      IPSDbComponent comp, boolean bDirection, boolean isQuiet)
   {
      // Threshold
      if (comp == null)
         throw new IllegalArgumentException("comp must not be null");

      // Threshold - we're expecting a PSSearch cms object
      if (!(comp instanceof PSSearch))
         return true; // we're just not interested but no error has happened

      PSSearch search = (PSSearch) comp;

      if (bDirection)
      {
         // Validate the data
         if (!onValidateData(search, isQuiet))
            return false;

         // Threshold if 'all' is checked
         if (isAllSelected())
            search.addCommunity(search.PROP_COMMUNITY_ALL);
         else
            search.removeCommunity(search.PROP_COMMUNITY_ALL);

         // 'view' to object direction
         TableModel model = getTableModel();

         int rowCount = model.getRowCount();

         for (int i=0; i<rowCount; i++)
         {
            String strName = (String)
                  ((PSComparablePair) model.getValueAt(i, 0)).getValue();
            Object obj = model.getValueAt(i, 1);

            // Threshold - ignore case where no name exists.
            if (strName == null || strName.trim().length() == 0)
               continue;

            if (obj instanceof Boolean)
            {
               Boolean bObj = (Boolean) obj;
               String commId = (String)
                  ((PSComparablePair) model.getValueAt(i, 0)).getKey();
               if (bObj.booleanValue())
               {
                  search.addCommunity(commId);
               }
               else
               {
                  search.removeCommunity(commId);
               }
            }
         }
      }
      else
      {
         // object to 'view' direction
         super.load(search.getProperties());
      }

      return true;
   }
}
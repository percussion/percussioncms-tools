/*[ SearchViewPropertiesPanel.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSMultiValuedProperty;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.search.PSCommonSearchUtils;
import com.percussion.search.ui.PSSearchAdvancedPanel;
import com.percussion.guitools.PropertyTablePanel;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A panel with one main component. A tree that represents the content
 * selected by a <code>IPSContentSelector</code>.
 */
public class SearchViewPropertiesPanel extends PropertyTablePanel
   implements IPSDbComponentUpdater
{
   /**
    * Constructor with parent frame for central event
    * notification if needed.
    */
   public SearchViewPropertiesPanel(SearchViewDialog parentFrame)
   {
      super(new String [] {"Name", "Value"}, 0, true);
      //suppress eclipse warning
      if (null == parentFrame);
   }

   /**
    * Does nothing.
    */
   public void onDataPersisted()
   {}

   // see interface for description
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      return super.validateData();
   }

   // see base class for description
   public boolean onUpdateData(
      IPSDbComponent comp, boolean bDirection, boolean isQuiet)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp must not be null");

      // Threshold - we're expecting a PSSearch cms object
      if (!(comp instanceof PSSearch))
         return true; // we're just not interested but no error has happened

      PSSearch search = (PSSearch) comp;

      if (bDirection)
      {
         // 'view' to object direction
         // Validate the data
         if (!onValidateData(search, isQuiet))
            return false;

         // Stop editing if in that mode
         stopExtTableEditing();

         // Get the data
         TableModel model = getTableModel();

         // If we are missing properties in our model
         // that exist in the object they must be removed.
         Collection removeColl = new ArrayList();
         Iterator propsCurrent = search.getProperties();

         // Construct delete list
         while (propsCurrent.hasNext())
         {
            PSMultiValuedProperty p = 
               (PSMultiValuedProperty) propsCurrent.next();
            String strPropName = p.getName();

            // Ignore internal properties
            if (search.getInternalPropertyNames().contains(strPropName))
               continue;
            // Ignore hidden properties
            if (ms_hiddenSearchProps.contains(strPropName))
               continue;
            
            // Vector of vectors - we're interested in row n colvalue 1
            Vector dataVector = ((DefaultTableModel) model).getDataVector();
            boolean bExists = false;

            for (int i=0; i<dataVector.size(); i++)
            {
               String strName = (String)
                  ((Vector)dataVector.elementAt(i)).elementAt(0);

               if (strPropName.equalsIgnoreCase(strName))
               {
                  bExists = true;
                  break;
               }
            }

            if (!bExists)
               removeColl.add(strPropName);
         }

         int rowCount = model.getRowCount();

         for (int i=0; i<rowCount; i++)
         {
            String strName = (String) model.getValueAt(i, NAME_COLUMN);
            String strValue = (String) model.getValueAt(i, VALUE_COLUMN);

            // Threshold - ignore case where no name exists.
            if (strName == null || strName.trim().length() == 0)
               continue;

            // if there's a name there's going to be value validation
            // took care of that.
            search.setProperty(strName.trim(), strValue.trim());
         }

         // Perform delete(s) from collection
         Iterator remIter = removeColl.iterator();

         while (remIter.hasNext())
         {
            search.removeProperty((String) remIter.next(), null);
         }
      }
      else
      {
         // object to 'view' direction
         DefaultTableModel model = (DefaultTableModel) getTableModel();

         // clear out
         clearAllRows();

         Iterator iter = search.getProperties();
         int nCount = 0;

         while (iter.hasNext())
         {
            PSMultiValuedProperty p = (PSMultiValuedProperty) iter.next();

            // If the property is not editable ... continue
            if (!search.isEditableProperty(p.getName()))
               continue;

            // Ignore iternal properties
            if (search.getInternalPropertyNames().contains(p.getName()))
               continue;
            // Ignore hidden properties
            if (ms_hiddenSearchProps.contains(p.getName()))
               continue;
            
            // Increment the row count
            nCount++;

            Iterator values = p.iterator();

            while (values.hasNext())
            {
               String strValue = (String) values.next();
               Vector vAdd = new Vector();
               // set the name in the ui
               vAdd.add(p.getName());
               vAdd.add(strValue);
               model.addRow(vAdd);
            }
         }

         // padding for adding properties (blank rows)
         model.setRowCount(nCount + 1);
      }

      return true;
   }
   /**
    * List of search properties that need to be hidden in the properties tab
    * probably those are edited in other tab.
    */
   static private List ms_hiddenSearchProps = new ArrayList();
   static
   {
      ms_hiddenSearchProps.add(PSCommonSearchUtils.PROP_SYNONYM_EXPANSION);
   }
}
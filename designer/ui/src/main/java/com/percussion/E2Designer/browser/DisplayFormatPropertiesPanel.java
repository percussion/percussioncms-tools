/*[ DisplayFormatPropertiesPanel.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSDFMultiProperty;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.guitools.PropertyTablePanel;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This panel allows designers to add any property to a menu action.
 */
public class DisplayFormatPropertiesPanel extends PropertyTablePanel
   implements IPSPersistableInput
{
   /**
    * Constructs the panel.
    *
    * @param colNames, a string array of column names, may not be <code>null
    * </code> or empty ands should have only two entries spcifying the two
    * column names.
    *
    * @param rows, number of rows in the table
    */
   public DisplayFormatPropertiesPanel(String[] colNames, int rows)
   {
      super(colNames, rows, true);
   }

   public void load(Object data)
   {
      if (data instanceof PSDisplayFormat)
      {

         m_dsFormat = (PSDisplayFormat)data;
         Iterator itr = m_dsFormat.getProperties();
         DefaultTableModel model = (DefaultTableModel)getTableModel();
         model.getDataVector().clear();
         PSDFMultiProperty mp = null;
         String name = null;
         while(itr.hasNext())
         {
            mp = (PSDFMultiProperty)itr.next();
            name = mp.getName();            
            if (ms_hiddenProps.contains(name))            
               continue;
            Iterator valueItr = mp.iterator();
            while (valueItr.hasNext())
            {
               Object[] rowData = new Object[2];

               rowData[NAME_COLUMN] = name;
               rowData[VALUE_COLUMN] = (String)valueItr.next();
               model.addRow(rowData);
            }
         }
         model.setRowCount(model.getRowCount() + 1);
         model.fireTableDataChanged();
      }
   }

   public boolean save()
   {
      stopExtTableEditing();
      if (!validateData())
         return false;

      DefaultTableModel model = (DefaultTableModel)getTableModel();
      int rows = model.getRowCount();
      String name = null;
      String value = null;
      Iterator iter = m_dsFormat.getProperties();
      List currentProps = new ArrayList();
      while (iter.hasNext())
      {
         currentProps.add(
               ((PSDFMultiProperty) iter.next()).getName().toLowerCase());
      }
      for(int k = 0; k < rows; k++)
      {
         name = (String)model.getValueAt(k, NAME_COLUMN);
         value = (String)model.getValueAt(k, VALUE_COLUMN);
         if (name != null && name.trim().length() > 0)
         {
            m_dsFormat.setProperty(name.trim(), value.trim());
            currentProps.remove(name.toLowerCase().trim());
         }
      }
      iter = currentProps.iterator();
      // remove all properties that are no longer applicable
      // except hidden ones
      String propName = "";
      while (iter.hasNext())
      {
         propName = (String)iter.next();
         if(ms_hiddenProps.contains(propName))
            continue;

         m_dsFormat.removeProperty(propName);
      }

      return true;
   }

   private PSDisplayFormat m_dsFormat;

   /**
    * List of properties that are not editable directly by he end user and so
    * should not be displayed by this panel.  Never <code>null</code>, filled
    * in by a static initializer.
    */
   private static List ms_hiddenProps = new ArrayList();
   
   static
   {
      ms_hiddenProps.add(PSDisplayFormat.PROP_COMMUNITY);
      ms_hiddenProps.add(PSDisplayFormat.PROP_SORT_COLUMN);
      ms_hiddenProps.add(PSDisplayFormat.PROP_SORT_DIRECTION);
   }
}

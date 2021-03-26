/******************************************************************************
 *
 * [ DatasourceComboBox.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.util.PSStringComparator;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Combo box that automatically populates itself with cataloged datasource data,
 * using appropriate display names and sorting the repository datasource first
 * and other following sorted alpha ascending case-insensitive. Combo is
 * initially set as non-editable. Use {@link #getSelectedDatasource()} to
 * retrieve the selected datasource name, and
 * {@link #setSelectedDatasource(String)} to set the initial selection.
 */
public class DatasourceComboBox extends UTFixedComboBox
{
   private static final long serialVersionUID = 1L;

   /**
    * Calls {@link #DatasourceComboBox(Dimension) 
    * this((PSDesignerConnection)null)}.
    */
   public DatasourceComboBox()
   {
      this((PSDesignerConnection)null);
   }
   
   /**
    * Create the combo with a default size and populate it with data.  
    * 
    * @param conn The connection to use, may be <code>null</code> to use the
    * one returned by {@link E2Designer#getDesignerConnection()}.
    */
   public DatasourceComboBox(PSDesignerConnection conn)
   {
      this(new Dimension(200, 20), conn);
   }

   /**
    * Calls {@link #DatasourceComboBox(Dimension, PSDesignerConnection) 
    * this(preferredSize, null)}
    */
   public DatasourceComboBox(Dimension preferredSize)
   {
      this(preferredSize, null);
   }
   
   /**
    * Create the combo and populate it with data specifying a preferred size.
    * 
    * @param preferredSize The size, may not be <code>null</code>.
    * @param conn The connection to use, may be <code>null</code> to use the
    * one returned by {@link E2Designer#getDesignerConnection()}.
    */
   public DatasourceComboBox(Dimension preferredSize, PSDesignerConnection conn)
   {
      super(preferredSize);
      
      setEditable(false);
      List<String> datasources = CatalogDatasources.getCatalog(conn, false);
      List<DatasourceData> comboData = new ArrayList<DatasourceData>(
         datasources.size());
      for (String datasource : datasources)
      {
         String dsName = CatalogDatasources.getDisplayName(datasource);
         DatasourceData dataObj = new DatasourceData();
         dataObj.m_name = datasource;
         dataObj.m_displayName = dsName;
         comboData.add(dataObj);
      }

      // Sort with repository first, followed by others asc case-insensitive
      Comparator<DatasourceData> comparator = new Comparator<DatasourceData>() 
      {
         public int compare(DatasourceData data1, DatasourceData data2)
         {
            if (CatalogDatasources.isRepository(data1.m_name))
               return -1;
            else if (CatalogDatasources.isRepository(data2.m_name))
               return 1;
            else 
            {
               return m_stringComp.compare(data1.m_displayName, 
                  data2.m_displayName);
            }
         }
         private PSStringComparator m_stringComp = new PSStringComparator(
            PSStringComparator.SORT_CASE_INSENSITIVE_ASC);   
      };
         
      Collections.sort(comboData, comparator);

      for (DatasourceData data : comboData)
      {
         addItem(data);
      }
      
      if (getItemCount() > 0)
         setSelectedIndex(0);
   }
   
   /**
    * Get the selected datasource name.
    * 
    * @return The name, empty for the repository, may be <code>null</code> if 
    * one is not selected.
    */
   public String getSelectedDatasource()
   {
      String datasource = null;
      DatasourceData data = (DatasourceData) getSelectedItem();
      if (data != null)
      {
         datasource = data.m_name;
      
         // handle repository
         if (CatalogDatasources.isRepository(datasource))
            datasource = "";
      }
      
      return datasource;
   }

   /**
    * Sets the selection to the entry whose internal name matches the supplied
    * name.
    *  
    * @param dsName The name to match, may be <code>null</code> or empty
    * to indicate the repository.
    */
   public void setSelectedDatasource(String dsName)
   {
      for (int i = 0; i < getItemCount(); i++)
      {
         DatasourceData data = (DatasourceData) getItemAt(i);
         if ((StringUtils.isBlank(dsName) && 
            CatalogDatasources.isRepository(data.m_name)) || 
            data.m_name.equals(dsName))
         {
            setSelectedIndex(i);
            break;
         }
      }
   }  
   
   /**
    * Simple data object to provide a display name for datasources in the
    * combo box.
    */
   private class DatasourceData
   {
      public String toString()
      {
         return m_displayName;
      }
      private String m_name;
      private String m_displayName;
   }
 
}


/******************************************************************************
 *
 * [ CatalogExtendedBackendcolumns.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.catalog.PSCataloger;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
* this class catalogs the extended attributes for SQL columns. such as type ( int,
* char, varchar, etc).
*/
public class CatalogExtendedBackendcolumns
{
  /**
  * private constructor
  */
  private CatalogExtendedBackendcolumns()
  {
  }

  /**
    * returns a vector containing the extended types for fields such as char,
    * int, etc, they are called extended because the regular cataloger returns
    * only the field names
    * 
    * @param osTable a valid OSBackendTable. If null, an
    * IllegalArgumentException is thrown.
    * 
    * @return The catalog, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if osTable is null
    */

   public static Vector getCatalog(OSBackendTable osTable, boolean forceCatalog)
   {

      Vector<ExtendedBackendColumnData> catalog = null;
      String table;
      String strDatasource;

      if (osTable == null)
         throw new IllegalArgumentException();

      table = osTable.getTable();

      strDatasource = osTable.getDataSource();


      String key = strDatasource + ":" + table;
      try
      {
         // try to get the vector from the map
         catalog = m_catalogMap.get(key);
         // if force then will recatalog
         if (forceCatalog && catalog != null)
            m_catalogMap.remove(key);

         // if not found or force catalog
         if (catalog == null || forceCatalog)
         {
            String scName;
            String scType;
            String scJDBCType;

            catalog = new Vector<ExtendedBackendColumnData>();
            Properties properties = new Properties();

            // create the property array
            properties.put("RequestCategory", "data");
            properties.put("Datasource", strDatasource);
            properties.put("TableName", table);

            // schema set to blank
            properties.put("RequestType", "Column"); // request column data
            // construct the cataloger
            PSCataloger cataloger = new PSCataloger(E2Designer.getDesignerConnection());
            Document columns = cataloger.catalog(properties);
            PSCatalogResultsWalker column = new PSCatalogResultsWalker(columns);
            // right now add only the column name, type and jdbc type
            while (column.nextResultObject("Column"))
            {
               scName = column.getResultData("name");
               if (scName != null)
               {
                  scType = column.getResultData("backEndDataType");
                  if (scType != null)
                  {
                     scJDBCType = column.getResultData("jdbcDataType");
                     if (scJDBCType != null)
                     {
                        ExtendedBackendColumnData col = 
                           new ExtendedBackendColumnData(scName, scType, 
                              scJDBCType);
                        catalog.add(col);
                     }
                  }
               }
            }
            // add into the list
            m_catalogMap.put(key, catalog);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return catalog;
   }
  /**
  * hashmap to contain the cataloged tables
  */
    private static Map<String, Vector<ExtendedBackendColumnData>> 
       m_catalogMap = new HashMap<String, Vector<ExtendedBackendColumnData>>();

}

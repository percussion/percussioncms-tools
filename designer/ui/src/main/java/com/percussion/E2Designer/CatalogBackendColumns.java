/*[ CatalogBackendColumns.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

/**
 * Catalogs and caches all columns of backend tables.
 */
////////////////////////////////////////////////////////////////////////////////
public class CatalogBackendColumns implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private CatalogBackendColumns()
   {
   }
   
   /**
    * Catalogs for the columns of supplied table. If available cached data will
    * be returned unless a new cataloging is forced. If any problems(exceptions)
    * occur, a message is displayed to the designer.
    *
    * @param   osTable the backend table, may not be <code>null</code>
    * @param forceCatalog if <code>true</code> catalog is performed always,
    * otherwise only if cached data is not available.
    *
    * @return Vector a vector of all cataloged elements, may be
    * <code>null</code> if any exception occurs.
    *
    * @throws IllegalArgumentException if <code>osTable</code> is
    * <code>null</code>
    */
   public static Vector getCatalog(OSBackendTable osTable, boolean forceCatalog)
   {
      if(osTable == null)
         throw new IllegalArgumentException("osTable can not be null");
         
      Vector catalog = null;

      String datasource = osTable.getDataSource();
      String table = osTable.getTable();
      String key = datasource + table;

      try
      {
         // get the catalog and remove it from the map if it exits and
         // cataloging is forced
         catalog = (Vector) m_catalogMap.get(key);
         if (forceCatalog && catalog != null)
            m_catalogMap.remove(key);

         if (catalog == null || forceCatalog)
         {
            SqlCataloger cat = new SqlCataloger(datasource, table);

            catalog = cat.getCatalog();
            if(!catalog.isEmpty())
               m_catalogMap.put(key, catalog);
         }
      }
      catch ( IOException ioe )
      {
         SqlCataloger.handleException( ioe );
      }
      catch ( PSAuthorizationException ae )
      {
         SqlCataloger.handleException( ae );
      }
      catch (PSAuthenticationFailedException e)
      {
         PSDlgUtil.showError(
            e, false, E2Designer.getResources().getString("ExceptionTitle"));
      }
      catch ( PSServerException se )
      {
         SqlCataloger.handleException( se );
      }

      return catalog;
   }

  /*
   * a map of cataloged backend tables
   */
   private static HashMap m_catalogMap = new HashMap();
}

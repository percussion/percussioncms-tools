/*[ CatalogIndices.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

/**
 * Catalogs and caches all owners for a give db.
 */
////////////////////////////////////////////////////////////////////////////////
public class CatalogIndices implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private CatalogIndices()
   {
   }

   /**
    * Same as its overloaded brother, except you can pass in a table rather than
    * all the parameters individually.
   **/
   public static Vector getCatalog( OSBackendTable table, boolean forceCatalog )
   {
      return getCatalog( table.getDataSource(), table.getTable(), forceCatalog );
   }

   /**
    * Get the catalog of all owners on a specified datasource.
    * If available cached data will be returned unless a new cataloging is forced
    * by the supplied flag. If errors occur during the cataloging, a message is
    * displayed to the user.
    *
    * @param datasource the datasource to catalog, may be <code>null</code> or 
    * empty to reference the repository.
    * @param table the table within the datasource
    * @param forceCatalog If <code>true</code> the cache is flushed and a catalog
    * is performed.
    *
    * @return A vector containing the unique keys. Each unique key is a vector
    * that contains the names of all the columns that make up the key. If any
    * problems occur, a message is displayed to the designer and null is returned.
    */
   public static Vector getCatalog( String datasource, String table, 
      boolean forceCatalog)
   {
      Vector catalog = null;
      String key = datasource + table;
      try
      {
         // get the catalog and remove it from the map if it exists and cataloging
         // is forced
         catalog = (Vector) m_catalogMap.get(key);
         if (forceCatalog && catalog != null)
            m_catalogMap.remove(key);

         if (catalog == null || forceCatalog)
         {
            catalog = new Vector(10);
            String reqType = "UniqueKey";
            SqlCataloger cat = new SqlCataloger( reqType, datasource, table );
            PSCatalogResultsWalker walker = cat.getWalker();
            Node                curNode;
            String elementDataType = "name";
            while (walker.getNextElement( reqType, true ) != null)
            {
               curNode = walker.getCurrent();
               Vector vColumnNames = new Vector();
               while (walker.getNextElement(elementDataType, true) != null)
               {
                  String strColName = (String)walker.getElementData(elementDataType, false);
                  vColumnNames.add(strColName);
               }
               catalog.add(vColumnNames);
               walker.setCurrent(curNode);
            }
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
      catch ( PSAuthenticationFailedException ae )
      {
         SqlCataloger.handleException( ae );
      }
      catch ( PSServerException se )
      {
         SqlCataloger.handleException( se );
      }

      return catalog.size() > 0 ? catalog : null;
   }

   //////////////////////////////////////////////////////////////////////////////
   /*
    * a map of cataloged backend tables
    */
   private static HashMap m_catalogMap = new HashMap();
}



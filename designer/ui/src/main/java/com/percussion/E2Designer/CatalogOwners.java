/*[ CatalogOwners.java ]*******************************************************
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
 * Catalogs and caches all owners for a give db.
 */
////////////////////////////////////////////////////////////////////////////////
public class CatalogOwners implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private CatalogOwners()
   {
   }

   /**
    * Get the catalog of all owners on a specified driver/server/db triplet.
    * If available cached data will be returned unless a new cataloging is forced
    * by the supplied flag. If errors occur during the cataloging, a message is
    * displayed to the user.
    *
    * @param   driver the driver to catalog
    * @param server the server on the specified driver
    * @param database the db on the specified server
    * @param forceCatalog If <code>true</code> the cache is flushed and a catalog
    * is performed.
    *
    * @return A vector of the names of all owners on the specified database,
    * sorted in ascending order.  If any problems occur, a message is displayed to
    * the designer and null is returned.
    */
   //////////////////////////////////////////////////////////////////////////////
   public static Vector getCatalog( String driver, String server, String database,
         boolean forceCatalog)
   {
      Vector catalog = null;
      String key = driver + server + database;
      try
      {
         // get the catalog and remove it from the map if it exists and cataloging
         // is forced
         catalog = (Vector) m_catalogMap.get(key);
         if (forceCatalog && catalog != null)
            m_catalogMap.remove(key);

         if (catalog == null || forceCatalog)
         {
            SqlCataloger cat = new SqlCataloger( driver, server, database );
            catalog = cat.getCatalog();
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

      return catalog;
   }

   //////////////////////////////////////////////////////////////////////////////
   /*
    * a map of cataloged backend tables
    */
   private static HashMap m_catalogMap = new HashMap();
}



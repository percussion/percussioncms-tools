/*[ CatalogObjectTypes.java ]**************************************************
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
public class CatalogObjectTypes implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private CatalogObjectTypes()
   {
   }

   /**
    * Get the catalog of all object types supported by a specified datasource.
    * If available cached data will be returned unless a new cataloging is forced
    * by the supplied flag. If errors occur during the cataloging, a message is
    * displayed to the user.
    *
    * @param datasource the datasource to catalog, may be <code>null</code> or 
    * empty to reference the repository.
    * @param forceCatalog If <code>true</code> the cache is flushed and a catalog
    * is performed.
    *
    * @return A vector containing the names of all the supported types. These
    * type names can be used when cataloging the types themselves.  If any
    * problems occur, a message is displayed to the designer and null is returned.
    */
   //////////////////////////////////////////////////////////////////////////////
   public static Vector getCatalog( String datasource, boolean forceCatalog)
   {
      Vector catalog = null;
      String key = datasource;
      try
      {
         // get the catalog and remove it from the map if it exists and cataloging
         // is forced
         catalog = (Vector) m_catalogMap.get(key);
         if (forceCatalog && catalog != null)
            m_catalogMap.remove(key);

         if (catalog == null || forceCatalog)
         {
            SqlCataloger cat = new SqlCataloger( datasource, true );
            catalog = cat.getCatalog( "type", true );
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



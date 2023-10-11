/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.client.catalogers;

import com.percussion.client.PSCoreFactory;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Catalogs and caches all owners for a give db.
 */
// //////////////////////////////////////////////////////////////////////////////
public class PSCatalogDatabaseCategories implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private PSCatalogDatabaseCategories()
   {
   }

   /**
    * Convenience method that calls
    * {@link #getCatalog(PSDesignerConnection, String, boolean)  getCatalog(null, datasource
    * forceCatalog)}
    */
   public static Vector<String> getCatalog(String datasource, boolean forceCatalog)
   {
      return getCatalog(null, datasource, forceCatalog);
   }

   /**
    * Get the catalog of all object types supported by a specified datasource.
    * If available cached data will be returned unless a new cataloging is
    * forced by the supplied flag. If errors occur during the cataloging, a
    * message is displayed to the user.
    * 
    * @param conn a valid designer connection to use to catalog from server,
    * must not be <code>null</code>.
    * 
    * @param datasource the datasource to catalog, may be <code>null</code> or
    * empty to reference the repository.
    * @param forceCatalog If <code>true</code> the cache is flushed and a
    * catalog is performed.
    * 
    * @return A vector containing the names of all the supported types. These
    * type names can be used when cataloging the types themselves. If any
    * problems occur, a message is displayed to the designer and null is
    * returned.
    */
   // ////////////////////////////////////////////////////////////////////////////
   public static Vector<String> getCatalog(PSDesignerConnection conn,
      String datasource, boolean forceCatalog)
   {
      if (datasource == null || datasource.length() == 0)
      {
         throw new IllegalArgumentException(
            "datasource must not be null or empty");
      }
      if (conn == null)
      {
         conn = PSCoreFactory.getInstance().getDesignerConnection();
      }
      Vector<String> catalog = null;
      String key = datasource;
      try
      {
         // get the catalog and remove it from the map if it exists and
         // cataloging
         // is forced
         catalog = m_catalogMap.get(key);
         if (forceCatalog && catalog != null)
            m_catalogMap.remove(key);

         if (catalog == null || forceCatalog)
         {
            PSSqlCataloger cat = new PSSqlCataloger(datasource, true);
            cat.setConnectionInfo(conn);
            catalog = cat.getCatalog("type", true);
            m_catalogMap.put(key, catalog);
         }
      }
      catch (IOException | PSAuthenticationFailedException | PSServerException | PSAuthorizationException ioe)
      {
         PSSqlCataloger.handleException(ioe);
      }

      return catalog;
   }

   // ////////////////////////////////////////////////////////////////////////////
   /*
    * a map of cataloged backend tables
    */
   private static Map<String, Vector<String>> m_catalogMap = 
      new HashMap<>();
}

/*******************************************************************************
 *
 * [ PSCatalogDatabaseTables.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client.catalogers;

import com.percussion.client.PSCoreFactory;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs tables from the server and caches the results.
 */
public class PSCatalogDatabaseTables
{
   /**
    * Private ctor to enforce static use.
    */
   private PSCatalogDatabaseTables()
   {

   }

   /**
    * Convenience method that calls
    * {@link #getCatalog(PSDesignerConnection, String, String, boolean)  
    * getCatalog(<code>null</code>, datasource, tableType, forceCatalog)}
    */
   public static List<String> getCatalog(String datasource, String tableType,
      boolean forceCatalog)
   {
      return getCatalog(null, datasource, tableType, forceCatalog);
   }

   /**
    * Catalog tables from the server using the specified datasource.
    * 
    * @param conn a valid designer connection, must not be <code>null</code>
    * 
    * @param datasource The name of the datasource to use, may be
    * <code>null</code> or empty to specify the repository datasource.
    * @param tableType table type name which will be used as filter for
    * cataloging. If not <code>null</code> the value of the "type" attribute
    * of the "Table" element must match with this to be included in the
    * cataloged result.
    * @param forceCatalog <code>true</code> to force a request to the server,
    * <code>false</code> to use cached data if tables have already been
    * cataloged for the specified datasource.
    * 
    * @return A list of table names, never <code>null</code>, may be empty.
    */
   public static List<String> getCatalog(PSDesignerConnection conn,
      String datasource, String tableType, boolean forceCatalog)
   {
      if (conn == null)
      {
         conn = PSCoreFactory.getInstance().getDesignerConnection();
      }
      if (ms_cachedTables.get(datasource) == null || forceCatalog)
      {
         
         PSSqlCataloger cataloger = new PSSqlCataloger(datasource);
         if (tableType != null)
            cataloger.setFilter("@type", tableType);
         cataloger.setConnectionInfo(conn);
         try
         {
            ms_cachedTables.put(datasource, 
               new ArrayList<String>(cataloger.getCatalog()));
         }
         catch (IOException ioe)
         {
            PSSqlCataloger.handleException(ioe);
         }
         catch (PSAuthorizationException ae)
         {
            PSSqlCataloger.handleException(ae);
         }
         catch (PSAuthenticationFailedException ae)
         {
            PSSqlCataloger.handleException(ae);
         }
         catch (PSServerException se)
         {
            PSSqlCataloger.handleException(se);
         }
      }

      return ms_cachedTables.get(datasource);
   }
      
   /**
    * Results of previous catalog attempt, <code>null</code> until first call
    * to {@link #getCatalog(String, String, boolean)}, never <code>null</code>
    * after that.
    */
   private static Map<String, ArrayList<String>> ms_cachedTables = 
      new HashMap<String, ArrayList<String>>();
}

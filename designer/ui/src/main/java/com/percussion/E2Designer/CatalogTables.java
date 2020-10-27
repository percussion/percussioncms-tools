/******************************************************************************
 *
 * [ CatalogTables.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Catalogs tables from the server and caches the results.
 */
public class CatalogTables
{
   /**
    * Private ctor to enforce static use.
    */
   private CatalogTables()
   {

   }

   /**
    * Catalog tables from the server using the specified datasource.  
    * 
    * @param datasource The name of the datasource to use, may be 
    * <code>null</code> or empty to specify the repository datasource.
    * @param forceCatalog <code>true</code> to force a request to the server,
    * <code>false</code> to use cached data if tables have already been
    * cataloged for the specified datasource.
    * 
    * @return A list of table names, never <code>null</code>, may be empty.
    */
   public static List<String> getCatalog(String datasource, 
      boolean forceCatalog)
   {
      if (ms_cachedTables == null || forceCatalog)
      {
         ms_cachedTables = new ArrayList<String>();
         
         SqlCataloger cataloger = new SqlCataloger(datasource);
         try
         {
            ms_cachedTables.addAll(cataloger.getCatalog());
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
      }
      
      return ms_cachedTables;
   }

   /**
    * Results of previous catalog attempt, <code>null</code> until first call
    * to {@link #getCatalog(String, boolean)}, never <code>null</code> after 
    * that.
    */
   private static List<String> ms_cachedTables = null;
}

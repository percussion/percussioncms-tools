/******************************************************************************
 *
 * [ CatalogDatabaseFunctions.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.function.PSDatabaseFunctionCatalogHandler;
import com.percussion.extension.PSDatabaseFunctionDef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Catalogs and caches database function definitions available on the
 * Rhythmyx server. The functions are catalged once for every driver type
 * and then cached internally. For subsequent request, the functions can be
 * either be cataloged or returned from the cache (based on the parameters
 * specified by the caller).
 *
 * @see getDatabaseFunctions(PSDesignerConnection, String , boolean)
 */
public class CatalogDatabaseFunctions implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private CatalogDatabaseFunctions()
   {
   }

   /**
    * Returns an iterator over a list of database function definition
    * (<code>PSDatabaseFunctionDef</code>) objects.
    *
    * @param connection The connection to the server, if <code>null</code> will
    * obtain the connection using the
    * <code>com.percussion.E2Designer.getApp().getMainFrame().getDesignerConnection()</code>
    * method.
    *
    * @param datasource name of the datasource for which to catalog the database
    * functions, may not be <code>null</code> or empty
    *
    * @param forceCatalog if <code>true</code> then ignores the cached data
    * and performs a fresh catalog (the cached data is updated with the result
    * of this catalog), otherwise uses the cached data if possible. If database
    * function defitions have not been cached for the specified driver,
    * catalogging of database functions is performed and the cached data update
    * with the results of this catalog.
    *
    * @return an iterator over a list of database function definition
    * (<code>PSDatabaseFunctionDef</code>) objects, never <code>null</code>,
    * may be empty
    *
    * @throws IllegalArgumentException if <code>driver</code> is
    * <code>null</code> or empty
    */
   public static Iterator getDatabaseFunctionDefs(
      PSDesignerConnection connection, String datasource, boolean forceCatalog)
   {
      if ((datasource == null))
         throw new IllegalArgumentException("driver may not be null");

      // if not force catalog and has cached data for this driver, then
      // return data from the cache
      List<PSDatabaseFunctionDef> catalog =
            (List<PSDatabaseFunctionDef>)ms_dbFuncsMap.get(datasource);
      if ((!forceCatalog) && (catalog != null))
         return catalog.iterator();

      // need to catalog
      try
      {
         if (connection == null)
         {
            connection = E2Designer.getDesignerConnection();
         }

         // if force then remove existing cached data from map if it exits
         ms_dbFuncsMap.remove(datasource);

         PSDatabaseFunctionDef [] defs =
            PSDatabaseFunctionCatalogHandler.getDatabaseFunctions(
               new PSCataloger(connection), datasource);

         catalog = new ArrayList<PSDatabaseFunctionDef>();
         for ( int i = 0; i < defs.length; ++i )
            catalog.add(defs[i]);

         ms_dbFuncsMap.put(datasource, catalog);
      }
      catch (Exception ex)
      {
         handleException(ex);
      }

      return catalog.iterator();
   }

   /**
    * A common method to print message to user and call stack to log. The method
    * returns after the user dismisses the dialog.
    *
    * @param ex The exception or error that occurred, assumed not
    * <code>null</code>
    */
   private static void handleException(Exception ex)
   {
      PSDlgUtil.showErrorDialog(ex.toString(),
            E2Designer.getResources().getString("CatalogerExceptionTitle"));
      System.out.println( "Server exit cataloger failure: " + ex);
      ex.printStackTrace();
   }

   /**
   * Map to cache the cataloged database functions, the datasource name
   * (<code>String</code>) is used as the key and the list
   * (<code>java.util.List</code>) of database function definitions
   * (<code>PSDatabaseFunctionDef</code>) objects as the value.
   * Never <code>null</code>, initialized to empty map, modified in the
   * <code>getDatabaseFunctions()</code> method.
   */
   private static Map<String, List<PSDatabaseFunctionDef>> ms_dbFuncsMap = 
      new HashMap<String, List<PSDatabaseFunctionDef>>();
}

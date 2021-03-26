/******************************************************************************
 *
 * [ PSCatalogDatasources.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.catalogers;

import com.percussion.client.PSCoreFactory;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs active datasources from the server and caches the results.
 */
public class PSCatalogDatasources
{
   /**
    * Private ctor to force static use
    */
   private PSCatalogDatasources()
   {

   }

   /**
    * Convenience method that calls
    * {@link #getCatalog(PSDesignerConnection, boolean)  getCatalog(null,
    * forceCatalog)}
    */
   public static List<String> getCatalog(boolean forceCatalog)
   {
      return getCatalog(null, forceCatalog);
   }
   
   /**
    * Convenience method to call {@link #getCatalog(
    * PSDesignerConnection, boolean, boolean)}
    * as getCatalog(PSDesignerConnection, boolean, false).
    */
   public static List<String> getCatalog(PSDesignerConnection conn,
            boolean forceCatalog)
   {
      return getCatalog(conn, forceCatalog, false);
   }

   /**
    * Catalog the names of all active datsources from the server.
    * 
    * @param conn The connection to use, may be <code>null</code> to use the
    * one returned by {@link PSCoreFactory#getDesignerConnection()}.
    * @param forceCatalog <code>true</code> to force a request to the server,
    * <code>false</code> to use cached data if datasources have already been
    * cataloged.
    * @param includeDbPubOnlySources flag that indicates that database publishing
    * only sources should also be included in the returned results.
    * @return A list of datasource names, never <code>null</code> or empty
    * unless an error occurs. Call {@link #isRepository(String)} to determine if
    * a returned name represents the CMS Repository connection.
    */
   public static List<String> getCatalog(PSDesignerConnection conn,
      boolean forceCatalog, boolean includeDbPubOnlySources)
   {
      List<Map<String, String>> sources = 
         getCatalogAllInfo(conn, forceCatalog, includeDbPubOnlySources);
      List<String> names = new ArrayList<String>();
      for(Map<String, String> source : sources)
      {
         names.add(source.get(DATASOURCE_NAME));   
      }      
      return names;
   }
   
   /**
    * Catalog all info for all active datsources from the server.
    * @param conn The connection to use, may be <code>null</code> to use the
    * one returned by {@link PSCoreFactory#getDesignerConnection()}.
    * @param forceCatalog <code>true</code> to force a request to the server,
    * <code>false</code> to use cached data if datasources have already been
    * cataloged.
    * @param includeDbPubOnlySources flag that indicates that database publishing
    * only sources should also be included in the returned results.
    * @return  A list of datasource info as a Map, never <code>null</code> or empty
    * unless an error occurs. 
    */
   @SuppressWarnings("unchecked")
   public static List<Map<String, String>> getCatalogAllInfo(
            PSDesignerConnection conn,
            boolean forceCatalog, boolean includeDbPubOnlySources)
   {
      if (conn == null)
      {
         conn = PSCoreFactory.getInstance().getDesignerConnection();
      }
      if (ms_datasourceCache == null || forceCatalog)
      {
         ms_datasourceCache = new ArrayList<Map<String, String>>();
         ms_repositoryDatasource = "";

         PSSqlCataloger cataloger = new PSSqlCataloger();
         if(includeDbPubOnlySources)
            cataloger.addProperty("IncludeDbPubSources", "true");
         if (conn != null)
            cataloger.setConnectionInfo(conn);
         try
         {
            PSCatalogResultsWalker walker = cataloger.getWalker();
            while (walker.nextResultObject("datasource"))
            {
               Map<String, String> info = new HashMap<String, String>();
               info.put(DATASOURCE_NAME, walker.getResultData(DATASOURCE_NAME));
               info.put(JNDI_DATASOURCE_NAME, 
                  walker.getResultData(JNDI_DATASOURCE_NAME));
               info.put(JDBC_URL, walker.getResultData(JDBC_URL));
               info.put(DATABASE, walker.getResultData(DATABASE));
               info.put(IS_REPOSITORY, walker.getResultData(IS_REPOSITORY));
               info.put(ORIGIN, walker.getResultData(ORIGIN));
               info.put(DRIVER, walker.getResultData(DRIVER));
               info.put(IS_DB_PUBONLY, walker.getResultData(IS_DB_PUBONLY));
               ms_datasourceCache.add(UnmodifiableMap.decorate(info));
               if (isRepository(walker))
                  ms_repositoryDatasource = info.get(DATASOURCE_NAME);
            }
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
      List<Map<String, String>> results = 
         new ArrayList<Map<String, String>>();
      for(Map<String, String> entry : ms_datasourceCache)
      {
         String isPubOnly = entry.get("isDbPubOnly");
         if(!includeDbPubOnlySources && isPubOnly.equalsIgnoreCase("yes"))
            continue;
         results.add(entry);
      }
      return results;
   }
   
   /**
    * Retrieve datasource info map by name
    * @param name the name of the datasource to retrieve, cannot be
    * <code>null</code> or empty.
    * @param forceCatalog <code>true</code> to force a request to the server,
    * <code>false</code> to use cached data if datasources have already been
    * cataloged.
    * @return the source map or <code>null</code> if not found.
    */
   public static Map<String, String> getDataSourceByName(String name, boolean forceCatalog)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty.");
      List<Map<String, String>> sources = 
         getCatalogAllInfo(null, forceCatalog, true);
      for(Map<String, String> source : sources)
      {
         if(name.equalsIgnoreCase(source.get(DATASOURCE_NAME)))
         {
            return source;   
         }
      }      
      return null;
   }

   /**
    * Determines if the specified datasource name represents a connection to the
    * CMS Repository. This method will use cached data if available, otherwise
    * it will re-catalog datasource information from the server.
    * 
    * @param dsName The name of the datasource, may not be <code>null</code>
    * or empty.
    * 
    * @return <code>true</code> if it is the repository, <code>false</code>
    * if not.
    */
   public static boolean isRepository(String dsName)
   {
      if (StringUtils.isBlank(dsName))
         throw new IllegalArgumentException("dsName may not be null or empty");

      return dsName.equals(ms_repositoryDatasource);
   }

   /**
    * Determine if the walker is positioned on the repository dataset.
    * 
    * @param walker The walker to use, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it is the repository, <code>false</code>
    * if not.
    */
   public static boolean isRepository(PSCatalogResultsWalker walker)
   {
      return "yes".equals(walker.getResultData(IS_REPOSITORY));
   }

   /**
    * Gets the display name of the datasource name.
    * 
    * @param dsName The name, may not be <code>null</code> or empty.
    * 
    * @return The display name. For datasources other than the repository, it is
    * the supplied name. For the repository datasource, it is a generic
    * identifier such as "<CMS Repository>".
    */
   public static String getDisplayName(String dsName)
   {
      if (StringUtils.isBlank(dsName))
         throw new IllegalArgumentException("dsName may not be null or empty");

      if (dsName.equalsIgnoreCase(ms_repositoryDatasource))
         dsName = REPOSITORY_LABEL;

      return dsName;
   }

   /**
    * Map of datasource info, <code>null</code> until first call to
    * {@link #getCatalog(boolean)}, never <code>null</code> after that.
    */
   private static List<Map<String, String>> ms_datasourceCache = null;

   /**
    * Name of the repository datasource, set by each call to
    * {@link #getCatalog(boolean)} that does not use the cached data.
    */
   private static String ms_repositoryDatasource;

   /**
    * Constant for the display name to use for the repository datasource.
    */
   public static final String REPOSITORY_LABEL = "<CMS Repository>";
   
   /* Xml Element name Constants */
   public static final String DATASOURCE_NAME = "name";
   public static final String JNDI_DATASOURCE_NAME = "jndiDatasource"; 
   public static final String JDBC_URL = "jdbcUrl"; 
   public static final String DATABASE = "database"; 
   public static final String ORIGIN = "origin"; 
   public static final String IS_REPOSITORY = "isRepository"; 
   public static final String DRIVER = "driver";
   public static final String IS_DB_PUBONLY = "isDbPubOnly";
   
}

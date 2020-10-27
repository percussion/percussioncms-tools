/******************************************************************************
 *
 * [ CatalogDatasources.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Catalogs active datasources from the server and caches the results.
 */
public class CatalogDatasources
{
   /**
    * Private ctor to force static use
    */
   private CatalogDatasources()
   {

   }

   /**
    * Convenience method that calls 
    * {@link #getCatalog(PSDesignerConnection, boolean) 
    * getCatalog(null, forceCatalog)}
    */
   public static List<String> getCatalog(boolean forceCatalog)
   {
      return getCatalog(null, forceCatalog);
   }
   
   /**
    * Catalog all active datsources from the server.
    * 
    * @param conn The connection to use, may be <code>null</code> to use the
    * one returned by {@link E2Designer#getDesignerConnection()}.
    * @param forceCatalog <code>true</code> to force a request to the server,
    * <code>false</code> to use cached data if datasources have already been
    * cataloged.
    * 
    * @return A list of datasource names, never <code>null</code> or empty
    * unless an error occurs. Call {@link #isRepository(String)} to determine if
    * a returned name represents the CMS Repository connection.
    */
   public static List<String> getCatalog(PSDesignerConnection conn, 
      boolean forceCatalog)
   {
      if (ms_datasourceCache == null || forceCatalog)
      {
         ms_datasourceCache = new ArrayList<String>();
         ms_repositoryDatasource = "";
         
         SqlCataloger cataloger = new SqlCataloger();
         if (conn != null)
            cataloger.setConnectionInfo(conn);
         try
         {
            PSCatalogResultsWalker walker = cataloger.getWalker(); 
            while(walker.nextResultObject("datasource"))
            {
               String name = walker.getResultData("name");
               ms_datasourceCache.add(name);
               if (isRepository(walker))
                  ms_repositoryDatasource = name;
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
      }
      
      return new ArrayList<String>(ms_datasourceCache);
   }
   
   /**
    * Determines if the specified datasource name represents a connection to the
    * CMS Repository.  This method will use cached data if available, otherwise
    * it will re-catalog datasource information from the server.
    * 
    * @param dsName The name of the datasource, may not be <code>null</code> or 
    * empty.
    * 
    * @return <code>true</code> if it is the repository, <code>false</code> if 
    * not.
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
    * @return <code>true</code> if it is the repository, <code>false</code> if
    * not.
    */
   public static boolean isRepository(PSCatalogResultsWalker walker)
   {
      return "yes".equals(walker.getResultData("isRepository"));
   }   

   /**
    * Gets the display name of the datasource name.
    * 
    * @param dsName The name, may not be <code>null</code> or empty.
    * 
    * @return The display name.  For datasources other than the repository, it
    * is the supplied name.  For the repository datasource, it is a generic
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
    * List of datasource names, <code>null</code> until first call to 
    * {@link #getCatalog(boolean)}, never <code>null</code> after that.
    */
   private static List<String> ms_datasourceCache = null;
   
   /**
    * Name of the repository datasource, set by each call to 
    * {@link #getCatalog(boolean)} that does not use the cached data.
    */
   private static String ms_repositoryDatasource;
   
   /**
    * Constant for the display name to use for the repository datasource.
    */
   public static final String REPOSITORY_LABEL = "<CMS Repository>";
}
/******************************************************************************
 *
 * [ CatalogMimeTypes.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Catalogs supported mime types from the server.
 */
public class CatalogMimeTypes
{
   /**
    * Private ctor to enforce static use.
    */
   private CatalogMimeTypes()
   {
      
   }
   
   /**
    * Convenience method that calls
    * {@link #getCatalog(PSDesignerConnection, boolean) getCatalog(conn, false)}
    */
   public static List<String> getCatalog(PSDesignerConnection conn)
   {
      return getCatalog(conn, false);
   }
   
   /**
    * Catalogs and caches the mime types from the server.
    * @param conn The connection to use, may be <code>null</code> to use the
    * one returned by {@link E2Designer#getDesignerConnection()}.
    * @param forceCatalog <code>true</code> to force a request to the server,
    * <code>false</code> to use cached data if mime types have already been
    * cataloged.
    * 
    * @return The mime type names, never <code>null</code> or empty unless an
    * error occurs.
    */
   public static List<String> getCatalog(PSDesignerConnection conn, 
      boolean forceCatalog)
   {
      if (ms_mimeTypeCache == null || forceCatalog)
      {
         ms_mimeTypeCache = new ArrayList<String>();
         
         PSCataloger cataloger = new PSCataloger(conn == null ? 
            E2Designer.getDesignerConnection() : conn);
         try
         {
            Properties catalogProps = new Properties();
            catalogProps.put("RequestCategory", "system");
            catalogProps.put("RequestType", "MimeType");

            Document doc = cataloger.catalog(catalogProps);
            PSCatalogResultsWalker walker = new PSCatalogResultsWalker(doc);
            while (walker.nextResultObject("mimetype"))
            {
               ms_mimeTypeCache.add(walker.getResultData("name"));
            }
         }
         catch (IOException e)
         {
            SqlCataloger.handleException( e );
         }
         catch (PSServerException e)
         {
            SqlCataloger.handleException( e );
         }
         catch (PSAuthenticationFailedException e)
         {
            SqlCataloger.handleException( e );
         }
         catch (PSAuthorizationException e)
         {
            SqlCataloger.handleException( e );
         }         
      }
      
      return new ArrayList<String>(ms_mimeTypeCache);
   }
   
   /**
    * List of mime types, <code>null</code> until first call to 
    * {@link #getCatalog(PSDesignerConnection, boolean)}, 
    * never <code>null</code> after that.
    */
   private static List<String> ms_mimeTypeCache = null;   
}


/******************************************************************************
 *
 * [ CatalogCatalogers.java ]
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
import com.percussion.design.catalog.PSCataloger;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Catalogs directory and subject catalogers from the server.
 */
public class CatalogCatalogers
{
   /**
    * Private ctor to enforce static use.
    */
   private CatalogCatalogers()
   {
      
   }
   
   /**
    * Convenience method that calls
    * {@link #getCatalog(PSDesignerConnection, boolean) getCatalog(conn, false)}
    */
   public static List<CatalogerMetaData> getCatalog(PSDesignerConnection conn)
   {
      return getCatalog(conn, false);
   }
   
   /**
    * Catalogs and caches the subject and directory catalogers from the 
    * server.
    * @param conn The connection to use, may be <code>null</code> to use the
    * one returned by {@link E2Designer#getDesignerConnection()}.
    * @param forceCatalog <code>true</code> to force a request to the server,
    * <code>false</code> to use cached data if datasources have already been
    * cataloged.
    * 
    * @return The list cataloger meta data objects, never <code>null</code> or
    * empty unless an error occurs
    */
   public static List<CatalogerMetaData> getCatalog(PSDesignerConnection conn, 
      boolean forceCatalog)
   {
      if (ms_catalogerCache == null || forceCatalog)
      {
         ms_catalogerCache = new ArrayList<CatalogerMetaData>();
         
         PSCataloger cataloger = new PSCataloger(conn == null ? 
            E2Designer.getDesignerConnection() : conn);
         try
         {
            Properties catalogProps = new Properties();
            catalogProps.put("RequestCategory", "security");
            catalogProps.put("RequestType", "Cataloger");

            Document doc = cataloger.catalog(catalogProps);
            PSCatalogResultsWalker walker = new PSCatalogResultsWalker(doc);
            while(walker.nextResultObject("cataloger"))
            {
               String name = walker.getResultData("catalogerName");
               String type = walker.getResultData("catalogerType");
               String fullName = walker.getResultData("fullName");
               ms_catalogerCache.add(new CatalogerMetaData(name, type, 
                  fullName));
            }
         }
         catch ( IOException e )
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
      
      return new ArrayList<CatalogerMetaData>(ms_catalogerCache);
   }
   
   /**
    * List of catalogers, <code>null</code> until first call to 
    * {@link #getCatalog(PSDesignerConnection, boolean)}, 
    * never <code>null</code> after that.
    */
   private static List<CatalogerMetaData> ms_catalogerCache = null;   
}


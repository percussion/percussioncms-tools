/******************************************************************************
 *
 * [ CatalogServerExits.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSContentFactory;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.exit.PSExtensionCatalogHandler;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionDefFactory;
import com.percussion.extension.PSExtensionDefFactory;
import com.percussion.extension.PSExtensionException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * Catalogs and caches all server exits available on this Rhythmyx server. It
 * can also be used to catalog any input stream that contains an XML representation
 * of exits.
 */
////////////////////////////////////////////////////////////////////////////////
public class CatalogServerExits implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private CatalogServerExits()
   {
   }

   /**
    * This method takes an XML file that contains extension definitions, parses
    * the contents and returns a vector of IPSExtensionDef
    * objects, 1 for each definition in the stream.
    *
    * @param src The XML document that contains extension definitions.
    *
    * @param   bForce If <code>true</code>, the file will be read and parsed even
    * if a cached catalog is present, otherwise the cached catalog is returned,
    * if there is one.
    *
    * @return A vector containing 1 IPSExtensionDef object for each entry in
    * the XML doc. The order of the returned values is indeterminate.
   **/
   public static Vector getCatalog( File src, boolean bForce )
   {
      Vector catalog = null;

      // try to get the vector from the map
      catalog = ms_catalogMap.get(src.getPath());
      if ( null != catalog && !bForce )
         return catalog;

      // we really need to do the work
      Document xmlDoc = null;
      try
      {
         IPSMimeContent content = PSContentFactory.loadXmlFile(src);
         Reader in = PSContentFactory.getReader(content);
         xmlDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch ( IOException e )
      {
         handleException( e );
      }
      catch ( SAXException sax )
      {
         handleException( sax );
      }
      catalog = doCatalog( xmlDoc );
      if ( null != catalog )
         ms_catalogMap.put( src.getPath(), catalog );
      return catalog;
   }


   public static Vector getCatalog(PSDesignerConnection connection,
      String scHandlerName, boolean forceCatalog)
   {
      return getCatalog(connection, scHandlerName, null, null, forceCatalog, false);
   }

   /**
    * Get the catalog. If available cached data will be returned unless a
    * new cataloging is forced. If any errors occur, a message is displayed to
    * the user and logged.
    *
    * @param connection The connection to the server. <CODE>null</CODE> will
    * default the connection to E2Designer.
    *
    * @param scHandlerName A specific handler name, or <CODE>null</CODE> to
    * catalog all handlers.
    *
    * @param context A specific extension context, or <code>null</code> to
    * catalog all contexts.
    *
    * @param ifacePattern A search pattern using SQL syntax. All extensions
    * that implement an interface that matches this pattern will be returned.
    * If <code>null</code>, this will not be considered in the criteria.
    *
    * @param   forceCatalog true to force a new cataloging
    * @return Vector a vector of all cataloged elements or null if no elements or
    * an exception occurred
    *
    * @param includeDeprecated If <code>true</code>, exits that have been
    * deprecated will be included in the results, if <code>false</code>,
    * they will be excluded.
    */
   //////////////////////////////////////////////////////////////////////////////
   public static Vector getCatalog(PSDesignerConnection connection,
      String scHandlerName, String context, String ifacePattern,
      boolean forceCatalog, boolean includeDeprecated)
   {
      Vector<Object> catalog = null;

      // try to get the vector from the map
      String mapKey = scHandlerName + "/" + ifacePattern + "/" +
         (includeDeprecated ? "true" : "false");
      catalog = ms_catalogMap.get(mapKey);
      if ( null != catalog && !forceCatalog )
         return catalog;

      // we need to really catalog
      try
      {
         if( connection == null )
         {
            connection = E2Designer.getDesignerConnection();
         }

         // make sure that this is  a valid request
         if( !CatalogExtensionCatalogHandler.isCatalogHandlerInstaled(
            connection,scHandlerName) )
         {
            return(null); // no return
         }

         // if force then remove existing from map if it exits
         if (forceCatalog && catalog != null)
            ms_catalogMap.remove(mapKey);


         IPSExtensionDef [] defs = PSExtensionCatalogHandler.getCatalog(
            new PSCataloger( connection ), scHandlerName, context,
            ifacePattern );

         catalog = new Vector();
         for ( int i = 0; i < defs.length; ++i )
         {
            // skip deprecated exits unless we've been told to include them
            if (!includeDeprecated && defs[i].isDeprecated())
            {
               continue;
            }
            catalog.add( defs[i] );
         }

         if ( null != catalog )
            ms_catalogMap.put(mapKey, catalog );
      }
      catch ( IllegalArgumentException iae )
      {
         handleException( iae );
      }
      catch (IOException ioe)
      {
         handleException( ioe );
      }
      catch ( PSAuthorizationException ae )
      {
         handleException( ae );
      }
      catch ( PSServerException se )
      {
         handleException( se );
      }
      catch ( PSAuthenticationFailedException afe )
      {
         handleException( afe );
      }

      return catalog;
   }

   /**
    * Does the real work of walking the doc and creating the defs.
    *
    * @param src The document that contains the XML representation of the defs.
    * If null, null is returned.
    *
    * @return A vector w/ 1 or more extension defs, or null or there are no defs.
   **/
   private static Vector doCatalog( Document src )
   {
      if ( null == src )
         return null;
      Vector<IPSExtensionDef> catalog = new Vector<IPSExtensionDef>(20);

      try
      {
         PSXmlTreeWalker   tree = new PSXmlTreeWalker( src );

         final int flag1 = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         final int flag2 = flag1 | PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         Element begin = tree.getNextElement(flag2);
         if ( null != begin )
         {
            IPSExtensionDefFactory factory = new PSExtensionDefFactory();
            for ( Element e = begin; e != null; e = tree.getNextElement( flag1 ))
               catalog.add( factory.fromXml( e ));
         }
      }
      catch ( PSExtensionException ee )
      {
         handleException( ee );
      }

      return catalog.size() > 0 ? catalog : null;
   }


   /**
    * A common method to print message to user and call stack to log. The method
    * returns after the user dismisses the dialog.
    *
    * @param failure The exception or error that occurred.
   **/
   private static void handleException( Throwable failure )
   {
      PSDlgUtil.showErrorDialog(failure.toString(),
            E2Designer.getResources().getString("CatalogerExceptionTitle"));
      System.out.println( "Server exit cataloger failure: " + failure );
      failure.printStackTrace();
   }

   /**
   * hashmap to contain the cataloged java exits
   */
   private static Map<String, Vector> ms_catalogMap = new HashMap<String, Vector>();
}

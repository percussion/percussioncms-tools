/*[ CatalogWorkflowContentTypes.java ]*****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSContentType;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Obtains the list of workflow content types.
 */
public class CatalogWorkflowContentTypes extends CatalogHelper
{
   /**
    * Do not construct new instances. Use the static functions.
    */
   private CatalogWorkflowContentTypes()
   {
   }


   /**
    * A helper function for {@link #getCatalog(PSDesignerConnection, String,
    * boolean)} that obtains the connection and request root from the
    * static E2Designer object.
    *
    * @param bForceCatalog if <code>true</code> the catalog must be queried.
    *                      if <code>false</code> a cached catalog may be used.
    *
    * @return possibily empty List of PSContentType objects
    */
   public static List getCatalog( boolean bForceCatalog )
   {
      PSDesignerConnection connection = E2Designer.getDesignerConnection();
      return getCatalog(connection, getRequestRoot(), bForceCatalog);
   }


   /**
    * Gets a List of PSContentType objects from the catalog.
    *
    * @param connection valid connection to the server that contains the
    *                   catalog application
    * @param requestRoot the URL root that the server is listening on. For
    *                    example "Rhythmyx".  Should not include leading or
    *                    trailing slashes
    * @param bForceCatalog if <code>true</code> the catalog must be queried.
    *                      if <code>false</code> a cached catalog may be used.
    * @return possibily empty List of PSContentType objects
    */
   public static List getCatalog(PSDesignerConnection connection,
                                 String requestRoot, boolean bForceCatalog )
   {
      if ( null == connection )
         throw new IllegalArgumentException( "connection cannot be null" );
      if ( null == requestRoot )
         throw new IllegalArgumentException( "requestRoot cannot be null" );

      List result;
      PSContentType[] contentTypes = null;

      if (!bForceCatalog && m_cachedContentTypes != null)
         return m_cachedContentTypes;

      try
      {
         ApplicationRequestor app =
               new ApplicationRequestor( connection, requestRoot );
         Document resultXML = app.makeRequest( APPLICATION_NAME, RESOURCE_NAME );
         if ( resultXML != null )
         {
            // parse the result XML into PSContentType objects
            try
            {
               NodeList controlNodes = resultXML.getElementsByTagName(
                  PSContentType.XML_NODE_NAME );
               int length = controlNodes.getLength();
               contentTypes = new PSContentType[length];
               for ( int i = 0; i < length; i++ )
               {
                  contentTypes[i] =
                        new PSContentType( (Element) controlNodes.item( i ) );
               }
            }
            catch ( PSUnknownNodeTypeException e )
            {
               // shouldn't happen as we should always have a valid structure
               handleException( e );
            }
         }
      }
      catch ( IOException e )
      {
         handleException( e );
      }

      if ( null == contentTypes )
         result = new ArrayList();
      else
         result = Arrays.asList( contentTypes );

      m_cachedContentTypes = result;
      return result;
   }


   /**
    * A helper function for {@link #updateCatalog(PSDesignerConnection, String,
    * PSContentType)} that obtains the connection and request root from the
    * static E2Designer object.
    *
    * @param contentType the entry that will be created or updated
    * @return List of PSContentType objects reflecting the update
    */
   public static List updateCatalog( PSContentType contentType )
   {
      PSDesignerConnection connection = E2Designer.getDesignerConnection();
      return updateCatalog( connection, getRequestRoot(), contentType );
   }


   /**
    * Updates the entry in the catalog for the specified contentType.  If the
    * specified contentType does not exist in the catalog, it is created.
    * The catalog is requeried (using getCatalog) after the update.
    *
    * @param connection valid connection to the server that contains the
    *                   catalog application
    * @param requestRoot the URL root that the server is listening on. For
    *                    example "Rhythmyx".  Should not include leading or
    *                    trailing slashes
    * @param contentType the entry that will be created or updated
    *
    * @return List of PSContentType objects reflecting the update
    */
   public static List updateCatalog( PSDesignerConnection connection,
                                     String requestRoot,
                                     PSContentType contentType )
   {
      if ( null == connection )
         throw new IllegalArgumentException( "connection cannot be null" );
      if ( null == requestRoot )
         throw new IllegalArgumentException( "requestRoot cannot be null" );
      if ( null == contentType )
         throw new IllegalArgumentException( "contentType cannot be null" );


      // build parameters
      HashMap params = new HashMap();
      if ( contentType.getDbId() != PSContentType.NOT_ASSIGNED )
         params.put( "contenttypeid", String.valueOf( contentType.getDbId() ) );
      params.put( "contenttypename", contentType.getName() );
      params.put( "contenttypedesc", contentType.getDescription() );
      params.put( "contenttypenewurl", contentType.getNewURL() );
      params.put( "contenttypequeryurl", contentType.getQueryURL() );

      // set DBActionType = UPDATE
      E2DesignerResources designerRsrc = E2Designer.getResources();
      params.put( designerRsrc.getString( "DefaultActionField" ),
         designerRsrc.getString( "DefaultUpdateActionName" ) );

      try
      {
         ApplicationRequestor app =
               new ApplicationRequestor( connection, requestRoot );
         app.makeRequest( APPLICATION_NAME, UPDATE_RESOURCE_NAME, params );
      }
      catch ( IOException e )
      {
         handleException( e );
      }

      return getCatalog(connection, requestRoot, true);
   }


   /**
    * Name of the Rhythmyx application responsible for cataloging this object
    */
   private static final String APPLICATION_NAME = "sys_psxContentEditorCataloger";

   /**
    * Name of the resource within {@link #APPLICATION_NAME} responsible for
    * cataloging this object
    */
   private static final String RESOURCE_NAME = "getAllItemContentTypes.xml";

   /**
    * Name of the resource within {@link #APPLICATION_NAME} responsible for
    * updating the catalog for this object
    */
   private static final String UPDATE_RESOURCE_NAME = "updatecontenttype";

   /**
    * Holds a cache of the catalog.  Will be <code>null</code> until assigned
    * in <code>getCatalog</code>.
    */
   private static List m_cachedContentTypes = null;

   // DEBUG
   public static void main( String[] args )
   {
      java.util.Properties props = new java.util.Properties();
      props.put( PSDesignerConnection.PROPERTY_LOGIN_ID, "admin1" );
      props.put( PSDesignerConnection.PROPERTY_LOGIN_PW, "demo" );
      props.put( PSDesignerConnection.PROPERTY_PORT, "9992" );
      props.put( PSDesignerConnection.PROPERTY_HOST, "localhost" );

      try
      {
         PSDesignerConnection conn = new PSDesignerConnection( props );
         PSObjectStore objectStore = new PSObjectStore( conn );
         String reqRoot = objectStore.getServerConfiguration().getRequestRoot();

         List types =
               CatalogWorkflowContentTypes.getCatalog( conn, reqRoot, false );
         for ( Iterator i = types.iterator(); i.hasNext(); )
         {
            PSContentType type = (PSContentType) i.next();
            System.out.println( type.getId() + "=" + type.toString() );
         }

         PSContentType contentType = new PSContentType( "JAMES_TEST" );
         contentType.setDescription("Funky + Fresh = Good times!");
         updateCatalog( conn, reqRoot, contentType );

         contentType = new PSContentType( "SCHULTZ_TEST" );
         contentType.setId( 99 );

         types = updateCatalog( conn, reqRoot, contentType );
         for ( Iterator i = types.iterator(); i.hasNext(); )
         {
            PSContentType type = (PSContentType) i.next();
            System.out.println( type.getId() + "=" + type.toString() );
         }
      }
      catch ( PSServerException e )
      {
         handleException( e );
      }
      catch ( PSAuthorizationException e )
      {
         handleException( e );
      }
      catch ( PSAuthenticationFailedException e )
      {
         handleException( e );
      }
      catch ( IllegalArgumentException e )
      {
         handleException( e );
      }

   }
}

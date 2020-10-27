/*[ PSCatalogTransitionActionTriggers.java ]*****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to catalog list of transition action triggers.
 */
public class PSCatalogTransitionActionTriggers extends CatalogHelper
{
   /**
    * Do not construct new instances. Use the static functions.
    */
   private PSCatalogTransitionActionTriggers()
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
    * @return possibily empty List of String objects
    */
   public static List getCatalog( boolean bForceCatalog )
   {
      PSDesignerConnection connection = E2Designer.getDesignerConnection();
      return getCatalog(connection, getRequestRoot(), bForceCatalog);
   }


   /**
    * Gets a List of unique transition action trigger strings from the catalog.
    *
    * @param connection valid connection to the server that runs the
    *                   catalog application
    * @param requestRoot the URL root that the server is listening on. For
    *                    example "Rhythmyx".  Should not include leading or
    *                    trailing slashes
    * @param bForceCatalog if <code>true</code> the catalog must be queried.
    *                      if <code>false</code> a cached catalog may be used.
    * @return possibily empty List of String objects. Never <code>null</code>.
    */
   public static List getCatalog(PSDesignerConnection connection,
                                 String requestRoot, boolean bForceCatalog )
   {
      if ( null == connection )
         throw new IllegalArgumentException( "connection cannot be null" );
      if ( null == requestRoot )
         throw new IllegalArgumentException( "requestRoot cannot be null" );


      List transitions = new ArrayList();

      if (!bForceCatalog && m_cachedTransitions != null)
         return m_cachedTransitions;

      try
      {
         ApplicationRequestor app =
               new ApplicationRequestor( connection, requestRoot );
         Document resultXML = app.makeRequest( APPLICATION_NAME, RESOURCE_NAME );
         if ( resultXML != null )
         {
         // parse the result XML into String objects

            NodeList controlNodes = resultXML.getElementsByTagName(
               XML_NODE_TRANSITION );
            int length = controlNodes.getLength();
            String trans = null;
            for ( int i = 0; i < length; i++ )
            {
               trans = new String(((Element) controlNodes.item( i )).
                        getAttribute(XML_ATTRIB_ACTION_TRIGGER));
               if(!transitions.contains(trans))
                  transitions.add(trans); //only add unique triggers
            }
          }


      }
      catch ( IOException e )
      {
         handleException( e );
      }

      m_cachedTransitions = transitions;
      return transitions;
   }

   // TEST CODE
   public static void main( String[] args )
   {
      java.util.Properties props = new java.util.Properties();
      props.put( PSDesignerConnection.PROPERTY_LOGIN_ID, "admin1" );
      props.put( PSDesignerConnection.PROPERTY_LOGIN_PW, "demo" );
      props.put( PSDesignerConnection.PROPERTY_PORT, "9995" );
      props.put( PSDesignerConnection.PROPERTY_HOST, "localhost" );

      try
      {
         PSDesignerConnection conn = new PSDesignerConnection( props );
         PSObjectStore objectStore = new PSObjectStore( conn );
         String reqRoot = objectStore.getServerConfiguration().getRequestRoot();

         List triggers =
               PSCatalogTransitionActionTriggers.getCatalog( conn, reqRoot, false );
         for ( Iterator i = triggers.iterator(); i.hasNext(); )
         {
            String trigger = (String) i.next();
            System.out.println( "Action trigger: "+trigger );
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

   /**
    * Name of the Rhythmyx application responsible for cataloging this object
    */
   private static final String APPLICATION_NAME = "sys_wfLookups";

   /**
    * Name of the resource within {@link #APPLICATION_NAME} responsible for
    * cataloging this object
    */
   private static final String RESOURCE_NAME = "getAllTransitionNames.xml";

   /**
    * Name of the transition node
    */
   private static final String XML_NODE_TRANSITION = "transition";

   /**
    * Name of the action trigger attribute
    */
   private static final String XML_ATTRIB_ACTION_TRIGGER = "actiontrigger";

   /**
    * Holds a cache of the catalog.  Will be <code>null</code> until assigned
    * in <code>getCatalog</code>.
    */
   private static List m_cachedTransitions = null;


}

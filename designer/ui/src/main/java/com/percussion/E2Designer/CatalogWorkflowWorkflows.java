/*[ CatalogWorkflowWorkflows.java ]********************************************
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
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSWorkflow;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Obtains the list of workflows.
 */
public class CatalogWorkflowWorkflows extends CatalogHelper
{
   /**
    * Do not construct new instances. Use the static functions.
    */
   private CatalogWorkflowWorkflows()
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
    * @return possibily empty List of PSGlobalLookup objects
    */
   public static List getCatalog( boolean bForceCatalog )
   {
      PSDesignerConnection connection = E2Designer.getDesignerConnection();
      return getCatalog( connection, getRequestRoot(), bForceCatalog );
   }


   /**
    * Returns the list of global lookup choices.
    *
    * @param bForceCatalog <CODE>true</CODE> will force cataloging
    * and a <CODE>false</CODE> will simply use cached data. This method must
    * always poll to see if any changes have been made. If none are detected,
    * the cached data will be returned unless the flag is true.
    * @return A valid List of zero-or-more PSGlobalLookup objects
    * @todo implement caching
    */
   public static List getCatalog( PSDesignerConnection connection,
                                  String requestRoot,
                                  boolean bForceCatalog )
   {
      List result;
      PSWorkflow[] workflows = null;

      try
      {
         ApplicationRequestor app =
               new ApplicationRequestor( connection, requestRoot );
         Document resultXML =
               app.makeRequest( APPLICATION_NAME, RESOURCE_NAME );
         if ( resultXML != null )
         {
            try
            {
               NodeList nodes = resultXML.getElementsByTagName(
                     PSWorkflow.XML_NODE_NAME );
               int length = nodes.getLength();
               workflows = new PSWorkflow[length];
               for ( int i = 0; i < length; i++ )
               {
                  workflows[i] = new PSWorkflow( (Element) nodes.item( i ) );
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

      if ( null == workflows )
         result = new ArrayList();
      else
         result = Arrays.asList( workflows );
      return result;
   }


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
               CatalogWorkflowWorkflows.getCatalog( conn, reqRoot, false );

         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = doc.createElement( "getWorkflows" );
         doc.appendChild(root);

         for ( Iterator i = types.iterator(); i.hasNext(); )
         {
            PSWorkflow workflow = (PSWorkflow) i.next();
            System.out.println( workflow );
            System.out.println( "id = " + workflow.getId() );
            System.out.println( "desc = " + workflow.getDescription() );

            root.appendChild(workflow.toXml(doc));
         }
         System.out.println(PSXmlDocumentBuilder.toString(doc));

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
   private static final String APPLICATION_NAME = "sys_psxWorkflowCataloger";

   /**
    * Name of the resource within {@link #APPLICATION_NAME} responsible for
    * cataloging this object
    */
   private static final String RESOURCE_NAME = "getWorkflows.xml";
}

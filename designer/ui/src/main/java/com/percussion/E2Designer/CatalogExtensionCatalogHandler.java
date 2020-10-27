/*[ CatalogExtensionCatalogHandler.java ]**************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.exit.PSExtensionHandlerCatalogHandler;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionRef;

import java.util.HashMap;
import java.util.Vector;

/**
*returns the name of the installed exit handlers
*/
public class CatalogExtensionCatalogHandler
{
   private CatalogExtensionCatalogHandler()
   {

   }

   public static Vector getCatalog(PSDesignerConnection connection,
      boolean forceCatalog )
   {
      return getCatalog( connection, false, forceCatalog );
   }

   /**
    * The first time this method is called, it queries the server for a list
    * of all installed extension handlers. The list is cached and returned
    * on successive calls that don't have the forceCatalog flag set.
    *
    * @param connection A valid connection to a Rx server.
    *
    * @param scriptableOnly If <code>true</code>, only handlers that are
    * scriptable are returned. Otherwise, all handlers are returned.
    *
    * @param forceCatalog if <code>true</code> recatalog the data, else
    * use catched data
    *
    * @return A collection containing 0 or more names of the installed
    * extensions as PSExtensionRefs.
    */
   public static Vector getCatalog( PSDesignerConnection connection,
      boolean scriptableOnly, boolean forceCatalog )
   {
      String key = scriptableOnly ? "s" : "ns";
      Vector catalog = (Vector) m_data.get( key );
      if ( null == catalog )
         catalog = new Vector(5);

      if ( catalog.size() == 0 || forceCatalog )
      {
         try
         {
            catalog.clear();
            IPSExtensionDef [] defs = PSExtensionHandlerCatalogHandler.getCatalog(
               new PSCataloger( connection));

            for ( int i = 0; i < defs.length; ++i )
            {
               String scriptableValue = defs[i].getInitParameter(
                  IPSExtensionDef.INIT_PARAM_SCRIPTABLE );
               if ( !scriptableOnly || ( null != scriptableValue &&
                  scriptableValue.equalsIgnoreCase( "yes" )))
               {
                  catalog.add( defs[i].getRef());
               }
            }
         }
         catch (Exception e)
         {
              e.printStackTrace();
         }
       }
       m_data.put( key, catalog );
       return( catalog );
   }

   /**
   *checks if the extension is installed on the server
   *
   *@param connection the server to query
   *
   *@param Key the name to search for
   *
   *@return <code> true </code> if extension is installed <code> false </code> if not
   *
   */

   public static boolean isCatalogHandlerInstaled(
      PSDesignerConnection connection,String Key)
   {
      boolean bFound=false;
      Vector vc=getCatalog(connection,false);
      if( vc != null )
      {
         int limit=vc.size();
         for(int count=0;count<limit;count++)
         {
             if(((PSExtensionRef) vc.get(count)).getExtensionName().equals(Key))
             {
               bFound=true;
               break;
             }
         }
      }
       return bFound;
   }
  
   /**
    * The name of the standard extension handler for Java extensions.
    */
   public static final String JAVA_EXTENSION_HANDLER_NAME = "Java";
   /**
    * The name of the standard extension handler that processes Java script
    * extensions.
    */
   public static final String JAVA_SCRIPT_EXTENSION_HANDLER_NAME = "JavaScript";

   private static HashMap  m_data = new HashMap();
}

/*[ CatalogUserContext.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.io.Serializable;
import java.util.Vector;

/**
 * Catalogs and caches all user context information on this Rhythmyx server.
 */
////////////////////////////////////////////////////////////////////////////////
public class CatalogUserContext implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private CatalogUserContext()
   {
   }

   /**
    * Get a list of all user context entries. <p/>
    * If available cached data will be returned unless a new cataloging is forced.
    *
    * @param   forceCatalog If <code>true</code>, any cached data will be discarded
    * and the server will be re-queried for the catalog entries.
    *
    * @return A vector of all cataloged elements.
    */
   //////////////////////////////////////////////////////////////////////////////
   public static Vector getCatalog(boolean forceCatalog)
   {
      if (forceCatalog || m_catalog == null)
      {
         m_catalog = new Vector();
/*
         // TODOph: finish this
         try
         {
            if (m_catalog == null || forceCatalog)
            {
               Properties properties = new Properties();
               properties.put("RequestCategory", "security"      );
               properties.put("RequestType",     "Attributes"    );
               properties.put("InstanceName",         "???"     );


               PSCataloger cataloger   = new PSCataloger(E2Designer.getApp().getMainFrame().getDesignerConnection());
               Document attributesDoc = cataloger.catalog(properties);
               PSCatalogResultsWalker attributes   = new PSCatalogResultsWalker(attributesDoc);
               while (attributes.nextResultObject("Attributes"))
                  m_catalog.addElement(attributes.getElementData("name", false));
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
*/
         /* These shouldn't be internationalized except thru the server because the
            server must interpret the string */
         m_catalog.add( "SessionId" );
         m_catalog.add( "SessionCreateTime" );
         m_catalog.add( "User/Name" );
         m_catalog.add( "User/SecurityProvider" );
         m_catalog.add( "User/SecurityProviderTypeId" );
         m_catalog.add( "User/SecurityProviderInstance" );
         // this entry should be replaced with a list of cataloged entries (from the security cataloger)
         m_catalog.add( "User/Attributes/FullName" );
         m_catalog.add( "User/SessionObject/" );
         m_catalog.add( "Roles/RoleName" );
         m_catalog.add( "Roles/RoleNameInClause" );
         m_catalog.add( "DataAccessRights/query" );
         m_catalog.add( "DataAccessRights/insert" );
         m_catalog.add( "DataAccessRights/update" );
         m_catalog.add( "DataAccessRights/delete" );
         m_catalog.add( "DesignAccessRights/modifyACL" );
         m_catalog.add( "DesignAccessRights/readDesign" );
         m_catalog.add( "DesignAccessRights/updateDesign" );
         m_catalog.add( "DesignAccessRights/deleteDesign" );
         m_catalog.add( "ServerAccessRights/dataAccess" );
         m_catalog.add( "ServerAccessRights/designAccess" );
         m_catalog.add( "ServerAccessRights/createApplications" );
         m_catalog.add( "ServerAccessRights/deleteApplications" );
         m_catalog.add( "ServerAccessRights/administerServer" );
      }

      return m_catalog;
   }

   //////////////////////////////////////////////////////////////////////////////
   private static Vector m_catalog = null;
}

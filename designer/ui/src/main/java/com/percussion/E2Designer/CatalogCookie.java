/*[ CatalogCookie.java ]*******************************************************
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
public class CatalogCookie implements Serializable
{
  /**
   * Do not construct new instances. Use the static function.
   */
  private CatalogCookie()
  {
  }
  
   /**
   * Get the catalog. If available cached data will be returned unless a
   * new cataloging is forced.
   *
   * @param type name or fullName
   * @param   forceCatalog true to force a new cataloging
   * @return Vector a vector of all cataloged elements
    */
  //////////////////////////////////////////////////////////////////////////////
  public static Vector getCatalog(boolean forceCatalog)
   {
      // TODOph: enable when the server supports it
      if ( null == m_catalog )
         m_catalog = new Vector(0);
      return m_catalog;
/*
      try
      {
         if (m_catalog == null || forceCatalog)
         {
            Properties properties = new Properties();
            properties.put("RequestCategory", "data"      );
            properties.put("RequestType",     "Column"    );
            properties.put("DriverName",         "psxml"     );
            properties.put("TableName",           "PSXCookie" );


            PSCataloger cataloger   = new PSCataloger(E2Designer.getApp().getMainFrame().getDesignerConnection());
            Document columns = cataloger.catalog(properties);
            PSCatalogResultsWalker column   = new PSCatalogResultsWalker(columns);
            while (column.nextResultObject("Column"))
               m_catalog.addElement(column.getElementData("name", false));
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return m_catalog;
*/
   }

  //////////////////////////////////////////////////////////////////////////////
  private static Vector m_catalog = null;
}

/******************************************************************************
 *
 * [ CatalogCgiVariables.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.catalog.PSCataloger;
import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;

/**
 * Catalogs and caches all CGI variables available on this Rhythmyx server.
 */
////////////////////////////////////////////////////////////////////////////////
public class CatalogCgiVariables implements Serializable
{
  /**
   * Do not construct new instances. Use the static function.
   */
  private CatalogCgiVariables()
  {
  }

   /**
   * Get the catalog. If available cached data will be returned unless a
   * new cataloging is forced.
   *
   * @param   forceCatalog true to force a new cataloging
   * @return Vector a vector of all cataloged elements
    */
  //////////////////////////////////////////////////////////////////////////////
  public static Vector getCatalog(boolean forceCatalog)
  {
     try
    {
        if (m_catalog == null || forceCatalog)
        {
        Properties properties = new Properties();
          properties.put("RequestCategory", "file"      );
          properties.put("RequestType",     "Column"    );
        properties.put("DriverName",         "psxml"     );
          properties.put("TableName",           "PSXCgiVar" );

        m_catalog = new Vector();

          PSCataloger cataloger= new PSCataloger(E2Designer.getDesignerConnection());
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
  }

  //////////////////////////////////////////////////////////////////////////////
   /*
   * the table fields
    */
  private static Vector m_catalog = null;
}

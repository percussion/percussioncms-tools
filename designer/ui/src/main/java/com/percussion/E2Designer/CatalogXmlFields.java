/******************************************************************************
 *
 * [ CatalogXmlFields.java ]
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

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

/**
 * Catalogs and caches all fields of XML files.
 */
////////////////////////////////////////////////////////////////////////////////
public class CatalogXmlFields implements Serializable
{
  /**
   * Do not construct new instances. Use the static function.
   */
  private CatalogXmlFields()
  {
  }

   /**
   * Get the catalog. If available cached data will be returned unless a
   * new cataloging is forced.
   *
   * @param   file the XML file to catalog
   * @return Vector a vector of all cataloged elements
    */
  //////////////////////////////////////////////////////////////////////////////
  public static Vector getCatalog(File file, boolean forceCatalog)
  {
    Vector catalog = null;

     try
    {
      // get the catalog and remove it from the map if it exits and cataloging
      // is forced
      catalog = (Vector) m_catalogMap.get(file);
      if (forceCatalog && catalog != null)
        m_catalogMap.remove(file);

        if (catalog == null || forceCatalog)
        {
        String fileDir = file.getParent();
        String fileName = file.getName();

        Properties properties = new Properties();
          properties.put("RequestCategory", "file"    );
        properties.put("RequestType",     "Column"  );
          properties.put("DriverName",      "psxml"   );
          properties.put("ServerName",      ""        );
          properties.put("DatabaseName",    fileDir   );
          properties.put("SchemaName",      ""        );
          properties.put("TableName",       fileName  );

        catalog = new Vector();

          PSCataloger cataloger   = new PSCataloger(E2Designer.getDesignerConnection());
          Document columns = cataloger.catalog(properties);
          PSCatalogResultsWalker column   = new PSCatalogResultsWalker(columns);
          while (column.nextResultObject("Column"))
          catalog.addElement(column.getElementData("name", false));

        m_catalogMap.put(file, catalog);
      }
    }
    catch (Exception e)
    {
       System.out.println(e);
    }

      return catalog;
  }

  //////////////////////////////////////////////////////////////////////////////
  /*
   * a map of cataloged XML files
   */
  private static HashMap m_catalogMap = new HashMap();
}


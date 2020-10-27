/*[ ObjectTypeCataloger.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.design.catalog.PSCatalogResultsWalker;

import javax.swing.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Creates the correct PSCataloger for db object types; for example: Tables,
 * System tables, Views.
 */
public class ObjectTypeCataloger extends SQLTabCataloger
{
   /**
    * Creates a new object type cataloger for the supplied driver, server,
    * database, schema and table.
    *
    * @param strDatasource name of the datasource which it is desired to catalog
    * 
    * @throws IllegalArgumentException if strDriver, strServer are null or empty
    */
   public ObjectTypeCataloger( String strDatasource)
   {
      super( SQLHierarchyConstraints.NT_DBOBJ_TYPE, strDatasource);

      m_Properties.put( "RequestType",      "TableTypes" );
   }

  public String getBaseKey( )
  {
    return "TableType";    // Even though this says TableType, it is more generic
  }

  /**
    * Returns a default entry to support those DBMS's that don't have table 
   * types, such as Informix. The default entry has an internal name of "" and a
   * generic name for display.
   **/
   public CatalogEntryEx getDefaultEntry()
   {
      ResourceBundle res = null;
      try
      {
         res = ResourceBundle.getBundle(
            "com.percussion.E2Designer.browser.CatalogerResources",
            Locale.getDefault());
      }
      catch ( MissingResourceException mre )
      {
         // don't fail, we'll use a local string below
         mre.printStackTrace();
      }

      CatalogEntryEx entry = new CatalogEntryEx();
      entry.setInternalName( "" );
      entry.setDisplayName( null == res ? "DB objects" :
         res.getString( "DefaultTableTypeEntry" ));
      entry.setIcon(getIcon());
      entry.setData( null );
      entry.setType(m_Type);
      return entry;
   }
  
   /**
    * For object types, the internal name always matches the display name,
    * so null is always returned.
    *
    * @returns always returns null
    */
   protected String getDisplayName( PSCatalogResultsWalker walker )
   {
      return null;
   }

   /**
    *
    * @returns type of table
    */
   protected String getInternalName( PSCatalogResultsWalker walker )
   {
      return walker.getResultData("type");
  }

   protected ImageIcon getIcon()
   {
//      ResourceBundle res = BrowserFrame.getBrowser().getResources();
//      return (new ImageIcon(getClass().getResource(res.getString("gif_object_type"))));
      return null;
   }


   /**
    * There is no extra data for an object type node.
    *
    * @returns always returns null
    */
   protected Object getData( PSCatalogResultsWalker walker )
   {
      return null;
   }

}


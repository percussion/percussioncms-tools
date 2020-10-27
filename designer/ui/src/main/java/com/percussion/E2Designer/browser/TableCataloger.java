/*[ TableCataloger.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.design.catalog.PSCatalogResultsWalker;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Creates the correct PSCataloger for databases and sets it.
 */
public class TableCataloger extends SQLTabCataloger
{
   /**
    * Creates a new table cataloger for the supplied driver and server and
    * database and schema.
    *
    * @param strDatasource name of the datasource which it is desired to catalog
    * @param strType name of the types of tables to return. The supported types
    * can be obtained using an ObjectTypeCataloger object. Must be a supported
    * type or an error will be returned when the cataloger is iterated.
    *
    * @throws IllegalArgumentException if strDriver, strServer or strType are
    * null or empty
    */
   public TableCataloger( String strDatasource, String strType, 
      String strFilter )
   {
      super( SQLHierarchyConstraints.NT_DBOBJ, strDatasource);

      m_Properties.put( "RequestType",      getBaseKey());

    // do not set "TableType" property if strType is null, so Informix can
    // catalog correctly
    if ( null != strType )
        m_Properties.put( "TableType",           strType     );
      
      if(strFilter != null && strFilter.length() > 0)
         m_Properties.put("Filter",      strFilter);
      
      m_strType = strType;
   }

  public String getBaseKey( )
  {
    return "Table";
  }

   /**
    * For a table node, the internal name always matches the display name,
    * so null is always returned.
    *
    * @returns always returns null
    */
   protected String getDisplayName( PSCatalogResultsWalker walker )
   {
      return null;
   }

   protected ImageIcon getIcon()
   {

      ResourceBundle res = BrowserFrame.getBrowser().getResources();
      ImageIcon icon = null;
      if(m_strType.equals("VIEW"))
         icon = new ImageIcon(getClass().getResource(res.getString("gif_view")));
      else
         icon = new ImageIcon(getClass().getResource(res.getString("gif_table")));
      return icon;
   }

   /**
    * Returns the text string that should be used as the internal name used when
    * communicating with the E2 server. The string may be empty.
    *
    * @returns text for internal name. If empty, the name is empty.
    */
   protected String getInternalName( PSCatalogResultsWalker walker )
   {
      return walker.getResultData( "name" );
   }

   /**
    * There is no extra data for a table node.
    *
    * @returns <code>null</code>.
    */
   protected Object getData( PSCatalogResultsWalker walker )
   {
      return null;
   }

   private String m_strType = null;

}


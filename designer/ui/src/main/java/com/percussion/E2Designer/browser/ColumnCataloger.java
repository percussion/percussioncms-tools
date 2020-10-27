/*[ ColumnCataloger.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;


import com.percussion.design.catalog.PSCatalogResultsWalker;

import javax.swing.*;

/**
 * Creates the correct PSCataloger for databases and sets it.
 */
public class ColumnCataloger extends SQLTabCataloger
{
   /**
    * Creates a new column cataloger for the supplied driver, server,
    * database, schema and table.
    *
    * @param strDatasource name of the datasource which it is desired to catalog
    * @param strTable name of the table
    *
    * @throws IllegalArgumentException if strDriver, strServer or strTable are
    * null or empty
    */
   public ColumnCataloger( String strDatasource, String strTable )
   {
      super( SQLHierarchyConstraints.NT_COLUMN, strDatasource);

      m_Properties.put( "RequestType",      getBaseKey());
      m_Properties.put( "TableName",           strTable    );

   }

  public String getBaseKey( )
  {
    return "Column";
  }

   /**
    * For a column, the internal name always matches the display name,
    * so null is always returned.
    *
    * @returns always returns null
    */
   protected String getDisplayName( PSCatalogResultsWalker walker )
   {
      return null;
   }

   /**
    * Returns the text string that should be used as the internal name used when
    * communicating with the E2 server. The string may be empty.
    *
    * @returns text for internal name. If empty, the name is empty.
    */
   protected String getInternalName( PSCatalogResultsWalker walker )
   {
      return walker.getResultData("name");
   }

   protected ImageIcon getIcon()
   {
      return null;
   }


   /**
    * There is no extra data for a column node. In the future, specific
    * column schema information such as data type, size, etc. could be
    * returned thru this object.
    *
    * @returns always returns null
    */
   protected Object getData( PSCatalogResultsWalker walker )
   {
      return null;
   }

}



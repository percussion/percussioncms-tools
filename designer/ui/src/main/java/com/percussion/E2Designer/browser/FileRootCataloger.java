/******************************************************************************
 *
 * [ FileRootCataloger.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.design.catalog.PSCatalogResultsWalker;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

/**
 * Creates the correct PSCataloger for the path root (example c:\, d:\).
 * Designed for the FILE tab tree.
 */
public class FileRootCataloger extends FileTabCataloger
{
   /**
    * Creates a new path root cataloger for the supplied driver and server and
    * database and schema.
    *
    * @param strDriver (short) name of the driver which it is desired to catalog.
    * This should be the proper name for the FILE driver.
    * @param strServer name of the server containing the file system that is
    * being cataloged. For V1, any passed in value will be ignored.
    *
    * @throws IllegalArgumentException if strDriver is null or empty
    */
   public FileRootCataloger( String strDriver, String strServer )
   {
      super( FileHierarchyConstraints.NT_PATHROOT );

      if (StringUtils.isEmpty(strDriver))              // do we throw excep if server is null/empty ?????
      {
         throw new IllegalArgumentException();
      }
      // set the Properties object that will be used by the base class to
      // get the xmlDoc
      m_Properties = new Properties( );
      m_Properties.put( "RequestCategory",  "data"    );
      m_Properties.put( "RequestType",      "Database");     //guess
      m_Properties.put( "DriverName",           strDriver );
      m_Properties.put( "ServerName",           "" );
      m_Properties.put( "DatabaseName",           strServer );
   }

  public String getBaseKey( )
  {
    return "Database";
  }


   /**
    * Returns the text string that should be used as the internal name used when
    * communicating with the E2 server. The string may be empty or null.
    *
    * @returns text for internal name. If empty, the name is empty.
    */
   protected String getInternalName( PSCatalogResultsWalker walker  )
   {
    return walker.getResultData("name"); // return the internal name
   }

  /**
    * Returns the type of the cataloger.
    *
    * @returns an integer corresponding to the Type of the cataloger.
    */
   protected int getType( PSCatalogResultsWalker walker )
  {
    // this is the only type that can be returned by this cataloger
    return FileHierarchyConstraints.NT_PATHROOT;
  }

}



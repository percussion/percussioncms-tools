/******************************************************************************
 *
 * [ FileCataloger.java ]
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
 * Creates the correct PSCataloger for any part of the tree except the root.
 * Designed for the FILE tab tree.
 */
public class FileCataloger extends FileTabCataloger
{
   /**
    * Creates a new path root cataloger for the supplied driver, server and path.
    *
    * @param strDriver (short) name of the driver which it is desired to catalog.
    * This should be the proper name for the FILE driver.
    * @param strServer name of the server containing the file system that is
    * being cataloged. For V1, any passed in value will be ignored.
    * @param strPath the path of the directory that is desired to be cataloged.
    * The complete path name should be included. For example, on an NT system,
    * "c:\winnt\system32" would be a valid path.
    *
    * @throws IllegalArgumentException if strDriver is null or empty
    */
   public FileCataloger(String strPath )
   {
      super( FileHierarchyConstraints.NT_FILESYSTEM_ENTRY );

      if (StringUtils.isEmpty(strPath)) // do we throw excep if server is null/empty ?????
      {
         throw new IllegalArgumentException();
      }
      // set the Properties object that will be used by the base class to
      // get the xmlDoc
      m_Properties = new Properties( );
      m_Properties.put( "RequestCategory",  "file"    );
      m_Properties.put( "RequestType",      "Table"    );     //guess
      m_Properties.put( "DriverName",           "psxml" );
      m_Properties.put( "ServerName",           "" );
      m_Properties.put( "DatabaseName",             strPath   );
      m_Properties.put( "TableType",             "DIRECTORY"   );
   }

  @Override
public String getBaseKey( )
  {
    return "Table";
  }

   /**
    * Returns the text string that should be used as the internal name used when
    * communicating with the E2 server. The string may be empty or null.
    *
    * @returns text for internal name. If empty, the name is empty.
    */
   protected String getInternalName( PSCatalogResultsWalker walker  )
   {
    return walker.getResultData("name"); // guessing cataloger will return the internal name
   }


  /**
    * Returns the type of the cataloger.
    *
    * @returns an integer corresponding to the Type of the cataloger.
   *
    * @throws IllegalArgumentException if the type is not of type File or Directory
    */
   protected int getType( PSCatalogResultsWalker walker )
  {
      return FileHierarchyConstraints.NT_DIRECTORY;
  }


}


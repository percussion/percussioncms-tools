/******************************************************************************
 *
 * [ DatasourceCataloger.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.CatalogDatasources;
import com.percussion.design.catalog.PSCatalogResultsWalker;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Provides browser catalog support for datasources.  Results are sorted with
 * the repository datasource first followed by other datasources alpha sorted
 * ascending case-insensitive. 
 */
public class DatasourceCataloger extends SQLTabCataloger
{
   /**
    * Construct the cataloger
    */
   public DatasourceCataloger()
   {
      super(SQLHierarchyConstraints.NT_DATASOURCE_OBJ, null);
      m_Properties.put( "RequestType", "Datasource");
   }

   // see base class
   public String getBaseKey()
   {
      return "datasource";
   }

   /**
    * Returns a generic string (i.e. "CMS Respository") for the repository
    * datasource, otherwise returns <code>null</code>.  See base class for
    * more info.
    */
   protected String getDisplayName(PSCatalogResultsWalker walker)
   {
      String name = walker.getResultData("name");
      return CatalogDatasources.isRepository(walker) ? 
         CatalogDatasources.REPOSITORY_LABEL : name;
   }

   // see base class
   protected String getInternalName(PSCatalogResultsWalker walker)
   {
      String name = walker.getResultData("name");
      return CatalogDatasources.isRepository(walker) ? "" : name;
   }

   /**
    * See base class for more info.
    * 
    * @return <code>null</code> always.
    */
   protected Object getData(PSCatalogResultsWalker walker)
   {
      return null;
   }

   // see base class
   protected ImageIcon getIcon()
   {
      ResourceBundle res = BrowserFrame.getBrowser().getResources();
      return (new ImageIcon(getClass().getResource(res.getString(
         "gif_database"))));
   }


}

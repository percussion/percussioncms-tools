/******************************************************************************
 *
 * [ CatalogContentEditorFields.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.client.PSCoreFactory;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.util.PSRemoteRequester;
import com.percussion.workbench.ui.PSWorkbenchPlugin;

import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
 * Catalogs and caches all CE fields.    
 */
public class CatalogContentEditorFields
{
  /**
   * Do not construct new instances. Use the static function.
   */
  private CatalogContentEditorFields()
  {
  }

  /**
   * Get the catalog. If available cached data will be returned unless a
   * new cataloging is forced.
   *
   * @param forceCatalog true to force a new cataloging
   * @return Vector a alpha ordered vector of Strings, each of which
   * represents one CE field formatted as follows: scopeName/fieldName,
   * where scope is 'system', 'shared' or 'local'.
   */
  public static Vector getCatalog(boolean forceCatalog)
  {
    try
    {
      if (m_catalog == null || forceCatalog)
      {
        m_catalog = new Vector();
         
        Properties props = new Properties();
         
        PSRemoteRequester appReq = 
           PSCoreFactory.getInstance().getRemoteRequester();
        
        PSRemoteCataloger remCatlg = new PSRemoteCataloger(appReq);
        
      PSContentEditorFieldCataloger fieldCatalogger =
         new PSContentEditorFieldCataloger(remCatlg, null, 
            IPSFieldCataloger.FLAG_INCLUDE_ALL);
        
        Iterator fields[] = {  
          fieldCatalogger.getSystemMap().keySet().iterator(),
          fieldCatalogger.getSharedMap().keySet().iterator(),
          fieldCatalogger.getLocalMap().keySet().iterator()
        };
        
        String scopeNames[] = {
           PSContentEditorFieldCataloger.SYSTEM,
           PSContentEditorFieldCataloger.SHARED,
           PSContentEditorFieldCataloger.LOCAL
        };
        
        for (int i = 0; i < fields.length; i++)
        {
           Vector catalogEntries = new Vector();
           
           String scopeName = scopeNames[i];
           
           Iterator it = fields[i];  
           while (it.hasNext())
           {
              String fieldName = (String)it.next();
              
              //format a new entry as scopeName/fieldName.
              catalogEntries.add(scopeName + "/" + fieldName);
           }
           
           //sort them up
           Collections.sort(catalogEntries);
           
           //add already sorted
           m_catalog.addAll(catalogEntries);
        }

        if (m_catalog.size() < 1)
        {
          //this means that there is something wrong with this app
          throw new Exception("no CE fields found!");
        }
      }
    }
    catch (Exception e)
    {
       PSWorkbenchPlugin.handleException("Content editor field cataloger",
               "Content editor field cataloger error",
               "Error occurred while cataloging content editor fields", e);
    }
    return m_catalog;
  }
  
  /**
   * The cataloged and alpha sorted CE fields formatted as scopeName/fieldName,
   * where scope is 'system', 'shared' or 'local'.
   */
  private static Vector m_catalog = null; 
}

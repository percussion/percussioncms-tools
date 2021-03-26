/******************************************************************************
 *
 * [ CatalogContentItemStatus.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;


import com.percussion.client.PSCoreFactory;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSRemoteRequester;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Catalogs and caches all relevant to content item status tables
 * and columns. The catalog returned is a alpha ordered collection
 * of Strings formatted as TABLENAME.COLUMNNAME. The catalogging
 * is dynamic; it queries a designated app/resource, which is 
 * expected to have a DTD, where each DOM element represents a single
 * table and each attribute represents a single column.
 * Only names element and attribute names are used,
 * values are completely ignored.    
 */
public class CatalogContentItemStatus implements Serializable
{
  /**
   * Do not construct new instances. Use the static function.
   */
  private CatalogContentItemStatus()
  {
  }

   /**
   * Get the catalog. If available cached data will be returned unless a
   * new cataloging is forced.
   *
   * @param forceCatalog true to force a new cataloging
   * @return Vector a alpha sorted vector of Strings, each of which
   * is formatted as uppercased TABLENAME.COLUMNNAME.
    */
  //////////////////////////////////////////////////////////////////////////////
  public static Vector getCatalog(boolean forceCatalog)
  {
    try
    {
      if (m_catalog == null || forceCatalog)
      {
        m_catalog = new Vector<String>();
         
        PSRemoteRequester appReq = 
           PSCoreFactory.getInstance().getRemoteRequester();
        
        PSRemoteCataloger remCatlg = new PSRemoteCataloger(appReq);
        
        IPSRemoteRequester req = remCatlg.getRemoteRequester();
        
        Map<String, String> params = new HashMap<String, String>();
        
        //use the content id of 'Folders', because it must always be there.
        params.put(IPSHtmlParameters.SYS_CONTENTID, "1");
        
        Document doc = req.getDocument(ms_queryResource, params);
        
        if (doc==null)
        {
           //this means that there is something wrong with this app
           throw new Exception(ms_queryResource + " returned null doc.");
        }
        
        /* The DTD of the doc. is structured to have each element as a table
         * name and each attribute is the column name. Here we don't care about
         * actual values of those elements and attributes, all we need is the names.
         */
         
        PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
        
        Element el = walker.getNextElement(true); 
        
        while(el != null)
        {
           String elname = el.getNodeName();
                       
           if (el.hasAttributes())
           {
              NamedNodeMap attrs = el.getAttributes();
              // Get number of attributes in the element
              int numAttrs = attrs.getLength();
             
              // Process each attribute
              for (int i=0; i<numAttrs; i++)
              {
                 Attr attr = (Attr)attrs.item(i);
             
                 // Get attribute name
                 String attrName = attr.getNodeName();
                 
                 //construct uppercased table.col entries. 
                 m_catalog.add(elname.toUpperCase() + "." + attrName.toUpperCase());
             }
           }
           
           el = walker.getNextElement(true);
        }
        
        if (m_catalog.size() < 1)
        {
          //this means that there is something wrong with this app
          throw new Exception(ms_queryResource + " returned empty doc.");
        }
        
        //sort them up
        Collections.sort(m_catalog);
      }
    }
    catch (Exception e)
    {
       handleException(e);
    }

    return m_catalog;
  }
  
  /**
   * A common method to print message to user and call stack to log. The method
   * returns after the user dismisses the dialog.
   *
   * @param ex The exception or error that occurred, assumed not
   * <code>null</code>
   */
  private static void handleException(Exception ex)
  {
     PSDlgUtil.showErrorDialog(
           ex.toString(),
           E2Designer.getResources().getString("CatalogerExceptionTitle"));
     System.out.println( "Server exit cataloger failure: " + ex);
     ex.printStackTrace();
  }

  /**
   * The cataloged and alpha sorted TABLENAME.COLUMNNAME strings.
   */
  private static Vector<String> m_catalog = null; 
  
  /**
   * The resource to query.
   */
  private static final String ms_queryResource =
     "sys_psxObjectSupport/getContentItemStatus.xml";
   
}

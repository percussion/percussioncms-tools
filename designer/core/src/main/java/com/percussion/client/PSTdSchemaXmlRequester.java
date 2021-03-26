/******************************************************************************
 *
 * [ PSTdSchemaXmlRequestor.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client;

import com.percussion.util.PSRemoteRequester;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * A helper class to make a request to the server to create a table definition
 * schema based on the passed in arguments. This ends up calling the
 * <code>PSTdSchemaXmlServlet</code> that is running on the server and does 
 * the actual work of creating the table defintion xml.
 * @author erikserating
 *
 */
public class PSTdSchemaXmlRequester
{

   /**
    * Private ctor to inhibit instantiation
    */
   private PSTdSchemaXmlRequester()
   {
      
   }
   
   /**
    * Requests that a table definition schema be created for the datasource 
    * and tables specified from the server.
    * @param datasource the name of the datasource connection. Can not
    * be <code>null</code> or empty.
    * @param tables a list of the names of all tables to be included in the
    * schema. Cannot be <code>null</code> or empty.
    * @param allowSchemaChanges flag indicating that schema changes should be
    * allowed in the created schema.
    * @return the schema xml as a string or <code>null</code> if it was not
    * created.
    * @throws SAXException  
    * @throws IOException 
    */
   @SuppressWarnings("unchecked")
   public static String getSchema(
      String datasource, List<String> tables, boolean allowSchemaChanges) 
      throws IOException, SAXException
   {
      PSRemoteRequester req = 
         PSCoreFactory.getInstance().getRemoteRequester();
      StringBuilder query = new StringBuilder();
      query.append("?");
      query.append("datasource=");
      query.append(datasource);
      query.append("&allowchanges=");
      query.append(Boolean.toString(allowSchemaChanges));
      for(String table : tables)
      {
         query.append("&");
         query.append("tables=");
         query.append(table);
      }
      Document doc = req.getDocument(RESOURCE + query, new HashMap());
      if(doc != null)
      {
         return PSXmlDocumentBuilder.toString(doc);
      }
      else
      {
         return null;
      }
      
   }
   
   /**
    * The resource path.
    */
   private static final String RESOURCE = "servlet/tdschemaxml"; 

}

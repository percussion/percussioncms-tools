/*[ DisplayFormatCataloger.java ]****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.ApplicationRequestor;
import com.percussion.E2Designer.CatalogHelper;
import com.percussion.E2Designer.E2Designer;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Catalogs all the communities.
 */
public class DisplayFormatCataloger  extends CatalogHelper{

   /**
    * Use the static method, no new instances need to be created.
    */
   private DisplayFormatCataloger()
   {
   }

   /**
    * Returns a map of (display id, display name). 
    */
   public static Map getAllDisplayFormats()
   {
      Map result = new HashMap();
      
      try
      {
         ApplicationRequestor app =
            new ApplicationRequestor(E2Designer.getDesignerConnection(),
            getRequestRoot());

         Document resultXML = app.makeRequest(APPLICATION_NAME, RESOURCE_NAME);
         
         if (resultXML != null)
         {
            NodeList nodes = resultXML.getElementsByTagName("PSXDisplayFormat");
            int length = nodes.getLength();
            Node name = null;
            
            for(int i=0; i<length; i++)
            {
               name = nodes.item(i);
               Element displayEl = (Element) name;

               NodeList children = displayEl.getChildNodes();

               // Get the display name and display id 
               // add it to the map
               String strName = "";
               String strId = "";
               
               int nChildSize = children.getLength();               
            
               for (int j=0; j<nChildSize; j++)
               {
                  Node aChild = children.item(j);
                  
                  if (aChild.getNodeName().equalsIgnoreCase
                     (PSDisplayFormat.XML_NODE_DISPLAYNAME))
                     strName = PSXmlTreeWalker.getElementData(
                        (Element) aChild);
                  else if (aChild.getNodeName().equalsIgnoreCase
                     ("PSXKey"))
                  {
                     NodeList keyElements = aChild.getChildNodes();                       
                     
                     // Key elements 
                     for (int k=0;k<keyElements.getLength();k++)
                     {
                        Node aKeyChild = keyElements.item(k);
                        
                        if (aKeyChild.getNodeName().equalsIgnoreCase(
                           "DISPLAYID"))
                        {
                           strId = PSXmlTreeWalker.getElementData(
                              (Element) aKeyChild);   
                           break;
                        }
                     }                                      
                  }                                    
               }  

               // If we have data add the entry
               if (strName.trim().length() > 0 && strId.trim().length() > 0)
               {
                  result.put(strId, strName);
               }                                        
            }
         }
      }
      catch (IOException e)
      {
         handleException(e);
      }
      
      return result;
   }

   /**
    * Name of the Rhythmyx application responsible for cataloging this object
    */
   private static final String APPLICATION_NAME = "sys_DisplayFormats";

   /**
    * Name of the resource within {@link #APPLICATION_NAME} responsible for
    * cataloging this object
    */
   private static final String RESOURCE_NAME = "getDisplayFormats.html";
   

}
/* *****************************************************************************
 *
 * [ CommunitiesCataloger.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.ApplicationRequestor;
import com.percussion.E2Designer.CatalogHelper;
import com.percussion.E2Designer.E2Designer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Catalogs all the communities.
 */
public class CommunitiesCataloger  extends CatalogHelper{

   /**
    * Use the static method, no new instances need to be created.
    */
   private CommunitiesCataloger()
   {
   }

   /**
    * Performs a request to the server to get all communities currently defined
    * on the server. A list of PSComparablePair objects is created, one for
    * each community. The community id is the key and the community name is
    * the value.
    *
    * @return A list of 0 or more PSComparablePair objects, each containing
    *    a commnunity name and id, each as <code>String</code>. Never 
    *    <code>null</code>, may be empty.
    */
   public static List getAllCommunities()
   {
      List result = new ArrayList();
      try
      {
         ApplicationRequestor app =
            new ApplicationRequestor( E2Designer.getDesignerConnection(),
            getRequestRoot());
         Document resultXML = app.makeRequest(APPLICATION_NAME, RESOURCE_NAME);
         if (resultXML != null)
         {
            NodeList nodes = resultXML.getElementsByTagName(
               LIST);
            int length = nodes.getLength();
            Node node = null;
            Node listNode;
            String name;
            String id;
            for(int k = 0; k < length; k++)
            {
               listNode = nodes.item(k);
               node = ((Element)listNode).getElementsByTagName(
                  COMMUNITYNODENAME).item(0);
               name = node.getFirstChild().getNodeValue();

               node = ((Element)listNode).getElementsByTagName(
                  COMMUNITYID_NODENAME).item(0);
               id = node.getFirstChild().getNodeValue();
               result.add(new PSComparablePair(id, name));
            }
         }
      }
      catch ( IOException e )
      {
         handleException( e );
      }
      return result;
   }

   /**
    * Name of the Rhythmyx application responsible for cataloging this object
    */
   private static final String APPLICATION_NAME = "sys_cmpCommunities";

   /**
    * Name of the resource within {@link #APPLICATION_NAME} responsible for
    * cataloging this object
    */
   private static final String RESOURCE_NAME = "communities.xml";

   /**
    * Name of the community node.
    */
   private static final String COMMUNITYNODENAME = "communityname";

   /**
    * The element name that contains the numeric id of the community.
    */
   private static final String COMMUNITYID_NODENAME = "communityid";

   private static final String LIST  = "list";

}
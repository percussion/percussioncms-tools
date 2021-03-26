/**[ PSLocaleCataloger.java ]********************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.cx.catalogers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.percussion.cms.PSCmsException;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * Catalogs all locales in the CMS by querying the
 * ../sys_i18nSupport/languagelookup.xml app.
 * @author RammohanVangapalli
 */
public class PSLocaleCataloger
{
   /**
    * Default constructor. Does nothing. Must be followed by call to fromXml()
    * method. This is useful only to build an object in the fly means the state
    * information might not come from the Rhythmyx server.
    */
   public PSLocaleCataloger()
   {
      super();
   }

   /**
    * Constructor meant to be used in the context of an applet. This may not work
    * in other contexts since there is no way of supplying credentials for logging
    * in.
    * @param urlBase the document or code base for the applet.
    * @throws PSCmsException if request to server to get the data fails for
    * any reason.
    */
   public PSLocaleCataloger(URL urlBase)
      throws PSCmsException
   {
      try
      {
         URL url = new URL(urlBase, "../sys_i18nSupport/languagelookup.xml");
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            url.openStream(), false);
         fromXml(doc.getDocumentElement());
      }
      catch(Exception e)
      {
         throw new PSCmsException(
            IPSContentExplorerErrors.CATALOG_ERROR,
            e.getMessage());
      }
   }
   
   /*
    * Implementation of the interface method
    */
   public void fromXml(Element elemSrc) throws PSUnknownNodeTypeException
   {
      m_locales.clear();
      
      NodeList nl = elemSrc.getElementsByTagName(PSEntry.XML_NODE_NAME);
      Element elem = null;
      PSEntry entry = null;
      for(int i=0; i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         entry = new PSEntry(elem, null,null);
         m_locales.add(entry);
      }
   }
   
   /**
    * Get the list of locale entries.
    * @return iterator of {@link PSEntry} object representing each locale 
    * in the system. Never <code>null</code>.
    */
   public Iterator getLocales()
   {
      return m_locales.iterator();
   }
   
   /**
    * Collection of {@link PSEntry} objects. Each objects represents a locale 
    * in the CMS. Filled in the constructor.
    */
   private Collection m_locales = new ArrayList();
}

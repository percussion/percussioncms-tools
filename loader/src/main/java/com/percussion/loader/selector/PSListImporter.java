/*[ PSListImporter.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;


import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.IPSLogCodes;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLogMessage;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import websphinx.Form;
import websphinx.Link;

/**
 * This class is similar with PSFileCrawler, but it is implemented to crawl 
 * a list of predefined links, which is specified in an XML file.
 */
public class PSListImporter 
   extends PSCommonCrawler 
{
   /**
    * Constructs this object from the following parameters.
    * Tests for the existence of the file denoted by <code>
    * strPath</code> path.
    *
    * @param strPath The file path of the file to load
    *    data from. Expecting an xml file conforming to
    *    the ListContentSelector.dtd. Never <code>null</code>
    *    or empty.
    * 
    * @param calcChecksum Indicate whether the checksum will be calculated
    *    by the file crawler. <code>true</code> the checksum will be 
    *    calculated; <code>false</code> the checksum will not be calculated.
    *
    * @throws FileNotFoundException if file does not exist.
    *
    * @throws SecurityException If a security manager exists
    *    and its SecurityManager.checkRead(java.io.
    *    FileDescriptor) method denies read access to the file
    */
   public PSListImporter(String strPath, boolean calcChecksum)
      throws FileNotFoundException
   {
      super(calcChecksum);
      
      if (strPath == null || strPath.trim().length() < 1)
         throw new IllegalArgumentException(
            "strPath must not be null or empty.");

      m_strPath = strPath;

      File f = new File(m_strPath);
      if (!f.exists())
         throw new FileNotFoundException(m_strPath + " must exist");
   }

   // see IPSSelector.isValidLink(Link, Object) for description
   public boolean isValidLink(Link link, Object ctx)
   {
      if (link instanceof Form)
      {
         return false;
      }
      else
      {
         return (m_linkNodeMap.get(link.getResourceId()) != null);
      }
   }
   
   /**
    * Runnable interface impl. so this class can run within a
    * <code>Thread</code>
    *
    * @throws IllegalStateException if <code>m_strPath</code> is not
    *    a valid readable file.
    *
    * @throws PSLoaderException if any xml is invalid.
    */
   public void run()
   {
      FileInputStream in = null;
      
      sendStatusEvent(PSCrawlEvent.STARTED);
      
      try
      {
         initRun();
         
         // load the links from XML
         in = new FileInputStream(m_strPath);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         NodeList list = doc.getElementsByTagName(XML_NODE_SERVER);
         if (list.getLength() < 1)
            throw new IllegalStateException(
               "Invalid xml missing: " + XML_NODE_SERVER);
         for (int i=0; i<list.getLength(); i++)
         {
            try
            {
               loadServer((Element) list.item(i));
            }
            catch (PSLoaderException e)
            {
               e.printStackTrace();
               Logger.getLogger(getClass().getName()).error(e.toString());
            }
         }
         
         // make depenedencies for the loaded links
         makeDependencies();   
      }
      catch (Exception e)
      {
         e.printStackTrace();
         Logger.getLogger(getClass().getName()).error(e.toString());
      }
      finally
      {
         if (in != null) {
            try {
               in.close();
            }
            catch (Exception e) {} // ignore if any
         }
      }

      sendStatusEvent(PSCrawlEvent.STOPPED);
   }

   /**
    * Interprets a <code>XML_NODE_SERVER</code> element
    * and imports its data.
    *
    * @param server Element containing the <code>XML_NODE_SERVER</code>
    *    xml. May be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>server</code> is <code>
    *    null</code>
    *
    * @throws PSLoaderException if any xml is invalid.
    */
   private void loadServer(Element server)
      throws PSLoaderException
   {
      if (server == null)
         throw new IllegalArgumentException("server must not be null.");

      NodeList list = server.getElementsByTagName(XML_NODE_ROOT);

      if (list.getLength() < 1)
         throw new PSLoaderException(
            IPSLoaderErrors.MISSING_PROPERTY, XML_NODE_ROOT);

      for (int i=0; i<list.getLength(); i++)
      {
         loadRoot((Element) list.item(i));
      }
   }

   /**
    * Interprets a <code>XML_NODE_ROOT</code> element
    * and imports its data.
    *
    * @param server Element containing the <code>XML_NODE_ROOT</code>
    *    xml. May be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>root</code> is <code>
    *    null</code>
    *
    * @throws PSLoaderException if any xml is invalid.
    */
   protected void loadRoot(Element root)
      throws PSLoaderException
   {
      if (root == null)
         throw new IllegalArgumentException("root must not be null.");

      String rootAttr = root.getAttribute(PSListImporter.XML_ATTR_ROOT);
      if (rootAttr == null || rootAttr.trim().length() == 0)
         throw new PSLoaderException(IPSLoaderErrors.ROOT_ATTRIB_MISSING);
      URL rootUrl = null;  
      try 
      {
         rootUrl = new URL(rootAttr);
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR, 
            e.toString());
      }
      
         
      PSXmlTreeWalker tree = new PSXmlTreeWalker(root);

      Element elLinks = tree.getNextElement(XML_NODE_LINKS);
      Element aLink = tree.getNextElement(XML_NODE_LINK);

      while (aLink != null)
      {
         loadLink(aLink, rootUrl);
         aLink = tree.getNextElement(XML_NODE_LINK,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * Interprets a <code>XML_NODE_LINK</code> element
    * and imports its data.
    *
    * @param link Element containing the <code>XML_NODE_LINK</code>
    *    xml. Assume not <code>null</code>.
    *
    * @param rootUrl The root url for the specified link, assume not 
    *    <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>link</code> is <code>
    *    null</code>
    *
    * @throws PSLoaderException if any xml is invalid.
    */
   private void loadLink(Element link, URL rootUrl)
      throws PSLoaderException
   {
      if (link == null)
         throw new IllegalArgumentException("link must not be null.");

      NodeList list = link.getElementsByTagName(XML_NODE_URL);

      if (list.getLength() < 1)
         throw new PSLoaderException(
            IPSLoaderErrors.MISSING_PROPERTY, XML_NODE_URL);

      Element urlEl = (Element) list.item(0);
      String strUrl = PSXmlTreeWalker.getElementData(urlEl);
      Link l = null;

      try
      {
         l = new Link(strUrl);
      }
      catch (MalformedURLException ex)
      {
         PSLogMessage msg = new PSLogMessage(
            IPSLogCodes.LOG_SELECTORERROR,
            new Object []
            {
               strUrl,
               ex.toString()
            },
            null,
            PSLogMessage.LEVEL_ERROR);
         
         // Recursively called, so return and keep going
         // as opposed to stopping for one bad url.
         return;
      }
      addLinksToMap(strUrl, l, rootUrl);
   }

   /**
    * Adds a mapping with url string as key and {@link WebSphinx.Link} object
    * as value.
    *
    * @param url, may be <code>null</code> or empty in which case method does
    * not do anything, it silently returns.
    *
    * @param link, may be <code>null</code> in which case method does
    * not do anything, it silently returns.
    * 
    * @param rootUrl the root URL, assume not <code>null</code> or empty.
    */
   private void addLinksToMap(String url, Link link, URL rootUrl)
   {
      if (link == null)
      {
         Logger.getLogger(PSListImporter.class.getName()).error(
         "Link is not present");
         return;
      }
      if(url == null || url.length() == 0)
      {
         Logger.getLogger(getClass().getName()).error(
         "Url - which is the key is not present");
         return;
      }
      m_linkNodeMap.put(url, new LinkSource(link, rootUrl));
   }

   /**
    * Make the dependency tree from the loaded links
    */
   private void makeDependencies()
   {
      Iterator itor = m_linkNodeMap.values().iterator();
      while (itor.hasNext() && (!shouldStop()))
      {
         LinkSource linkSrc = (LinkSource)itor.next();
         try
         {
            processLink(linkSrc.m_currLink, linkSrc.m_rootUrl, null);
         }
         catch (Exception e)
         {
            e.printStackTrace();
            Logger.getLogger(getClass().getName()).error(
               "Unexpected error when processing: " + linkSrc.toString());
         }
      }
   }

   /**
    * Container class to hold a <code>Link</code> and its root URL
    */
   private class LinkSource
   {
      /**
       * Constructs an instance with a link and root url
       * 
       * @param currLink The link, assume not <code>null</code>.
       * @param rootUrl The root URL, assume not <code>null</code> or empty.
       */
      LinkSource(Link currLink, URL rootUrl)
      {
         m_currLink = currLink;
         m_rootUrl = rootUrl;
      }
      
      public String toString()
      {
         return "link: " + m_currLink.toString() + 
            ", rootUrl: " + m_rootUrl.toString();
      }
      
      private Link m_currLink;
      private URL m_rootUrl;
   }
   
   /**
    * The path of the file to import. Initialized in ctor, never <code>null
    * </code> or empty.
    */
   private String m_strPath;

   /**
    * Vector of listeners to the import process. Initialized in
    * definition, never <code>null</code>
    */
   //private Vector m_vCrawlListeners = new Vector();

   /**
    * Contains a mappping from string url links to {@link WebSphinx.Link} objects.
    */
   private Map m_linkNodeMap = new HashMap();

   // Public xml name constants
   public static final String XML_NODE_SERVERS = "Servers";
   public static final String XML_NODE_SERVER = "Server";
   public static final String XML_NODE_LINKS = "Links";
   public static final String XML_NODE_LINK = "Link";
   public static final String XML_NODE_URL = "Url";
   public static final String XML_NODE_ROOTS = "Roots";
   public static final String XML_NODE_ROOT = "Root";
   public static final String XML_ATTR_ROOT = "url";
   public static final String XML_ATTR_NAME = "name";
   public static final String XML_ATTR_PORT = "port";
   public static final String XML_ATTR_FULL = "full";
   public static final String XML_ATTR_PROTOCOL = "protocol";
}
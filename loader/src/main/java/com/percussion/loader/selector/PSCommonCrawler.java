/******************************************************************************
 *
 * [ PSCommonCrawler.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.selector;


import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import websphinx.Link;


/**
 * Utility class, contains common methods that are used for both file and list
 * crawlers.
 */
public abstract class PSCommonCrawler
   implements IPSSelector, Runnable
{
   /**
    * Constructs an instance of this class.
    * 
    * @param calcChecksum Indicate whether the checksum will be calculated
    *    by the file crawler. <code>true</code> the checksum will be 
    *    calculated; <code>false</code> the checksum will not be calculated.
    */
   protected PSCommonCrawler(boolean calcChecksum)
   {
      m_calcChecksum = calcChecksum;
   }

   // IPSSelector interface implementation

   // @see {@link #IPSSelector} for description.
   public void addCrawlListener(IPSCrawlListener aListener)
   {
      if (aListener == null)
         throw new IllegalArgumentException(
            "Attempting to add null crawl listener.");

      if (!m_vCrawlListeners.contains(aListener))
      {
         m_vCrawlListeners.addElement(aListener);
      }
   }

   // @see {@link #IPSSelector} for description.
   public void removeCrawlListener(IPSCrawlListener aListener)
   {
      if (aListener == null)
         throw new IllegalArgumentException(
            "Attempting to remove null crawl listener.");

      m_vCrawlListeners.removeElement(aListener);
   }

   // @see {@link #IPSSelector} for description.
   public void start()
   {
      run();
   }

   // @see {@link #IPSSelector} for description.
   public void stop()
   {
      m_stop = true;
   }

   // @see {@link #IPSSelector} for description.
   public void clear()
   {
   }

   // @see {@link #IPSSelector} for description.
   public void pause()
   {
   }


   /**
    * @return The tree contains the nodes that has been crawled, never
    *    <code>null</code>, but may be empty.
    */
   public PSContentTreeModel getContentTree()
   {
      return m_treeModel;
   }


   /**
    * Initialize the crawler for each fresh crawling or scanning.
    */
   protected void initRun()
   {
      m_totalVisitedPages = 0;
      m_stop = false;
      m_treeModel = new PSContentTreeModel();
   }

   /**
    * Determines whether to stop current process.
    *
    * @return <code>true</code> if need to stop the process.
    */
   protected boolean shouldStop()
   {
      return m_stop;
   }

   /**
    * Processing the specified link, create node if not exist.
    *
    * @param link The to be processed link, it may not be <code>null</code>.
    *
    * @param rootUrl The root URL of the link, it may not be <code>null</code>.
    *
    * @param ctx the context of the crawler, it may be <code>null</code>.
    *
    * @throws MalformedURLException if fail to create an URL.
    * @throws PSLoaderException if an other eror occurs.
    */
   protected void processLink(Link link, URL rootUrl, Object ctx)
      throws PSLoaderException, MalformedURLException
   {
      if (link == null)
         throw new IllegalArgumentException("link may not be null");
      if (rootUrl == null)
         throw new IllegalArgumentException("rootUrl may not be null");

      PageData pageData = getPageData(link.getURL());
      addNode(link, pageData, rootUrl.toString());
   }

    /**
     * @return number of pages has been visited.
     */
    public int getPagesVisited()
    {
        return m_totalVisitedPages;
    }

   /**
    * Sends events to listeners about the status of the crawler.
    *
    * @param status an status id defined on {@link #PSCrawlEvent}.
    *
    */
   protected void sendStatusEvent(int status)
   {
      PSCrawlEvent e = new PSCrawlEvent(status, null, null);

      for (int i=0; i<m_vCrawlListeners.size(); i++)
      {
         IPSCrawlListener listener =
            (IPSCrawlListener) m_vCrawlListeners.elementAt(i);

         switch (status)
         {
            case PSCrawlEvent.STOPPED:
               listener.stopped(e);
               break;

            case PSCrawlEvent.STARTED:
               listener.started(e);
               break;

            case PSCrawlEvent.CLEARED:
               listener.cleared(e);
               break;

            case PSCrawlEvent.PAUSED:
               listener.paused(e);
               break;

            case PSCrawlEvent.TIMEDOUT:
               listener.timedOut(e);
               break;

            default:
               break;
         }
      }
   }


   /**
    * Get page data from the specified URL.
    *
    * @param url The URL used to retrieve the page data from, assume not
    *    <code>null</code>.
    */
   private PageData getPageData(URL url)
      throws PSLoaderException
  {
      InputStream in = null;
      byte[] rawData = null;
      String contentType = null;
      long checkSum = -1;

      try
      {
         URLConnection conn = url.openConnection();
         conn.connect();
         if (m_calcChecksum)
         {
            in = conn.getInputStream();
            rawData = PSLoaderUtils.getRawData(in);
            checkSum = PSLoaderUtils.calcChecksum(rawData);
         }
         contentType = conn.getContentType();

      }
      catch (Exception e)
      {
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
                                     e.toString());
      }
      finally
      {
         if (in != null)
         {
            try {
               in.close();
            }
            catch (Exception e){}
         }
      }
      return new PageData(rawData, contentType, checkSum);
   }

   /**
    * Send an event to the registered crawler listeners.
    *
    * @param itemCtx The item context that used to create the event, assume it
    *    is not <code>null</code>.
    */
   private void sendVisitedEvent(PSItemContext itemCtx)
   {
      PSCrawlEvent evt =
         new PSCrawlEvent(PSCrawlEvent.RETRIEVING, itemCtx, null);

      /**
       * Send the event to all the listeners
       */
      for (int i=0; i<m_vCrawlListeners.size(); i++)
      {
         IPSCrawlListener aListener =
            (IPSCrawlListener)m_vCrawlListeners.elementAt(i);
         aListener.visited(evt);
      }
   }

   /**
    * Creates an item context from the specified parameters.
    *
    * @param link The link that contains the resource id of the item context,
    *    assume not <code>null</code>.
    *
    * @param pageData the page data that contains the checksun of the created
    *    item context. Assume not <code>null</code>.
    *
    * @param rootResourceId The resource id of the root. It may not be
    *    <code>null</code> or empty.
    *
    * @return The created item context, never <code>null</code>.
    */
   private PSItemContext createItemContext(Link link, PageData pageData,
      String rootResourceId)
   {
      PSItemContext itemCtx = new PSItemContext(
         link.getResourceId(),
         null,
         pageData.m_contentType);

      itemCtx.setStatus(PSItemContext.STATUS_NEW);
      itemCtx.setDataObject(link);
      itemCtx.setRootResourceId(rootResourceId);
      if (m_calcChecksum)
      {
         itemCtx.setResourceDataLength(pageData.m_rawData.length);
         itemCtx.setChecksum(pageData.m_checkSum);
      }

      sendVisitedEvent(itemCtx);

      return itemCtx;
   }

   /**
    * Add a node for the specified parameters if the corresponding node does
    * not exist in the tree model.
    *
    * @param file The file that is used to create or identify the tree node.
    *    assume not <code>null</code>.
    *
    * @param pageData The page data of the file, assume not <code>null</code>.
    *
    * @param rootResourceId The resource id of the root. It may not be
    *    <code>null</code> or empty.
    *
    *
    * @return The created or existing tree node which corresponding to the
    *    <code>file</code>. Never <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs
    */
   private PSContentTreeNode addNode(Link link, PageData pageData,
      String rootResourceId)
      throws PSLoaderException
   {
      m_totalVisitedPages++;
      PSContentTreeNode node = m_treeModel.findNode(link);
      if (node != null)
         return node;

      PSItemContext itemCtx = createItemContext(link, pageData, rootResourceId);

      return m_treeModel.addNode(link, itemCtx, null);
   }

   /**
    * Add a child node to the specified parent node. The child node will be
    * created if does not exist.
    *
    * @param parentNode The parent node, assume not <code>null</code>.
    *
    * @param childLink The child like which contains the resource id of the
    *    child node, assume not <code>null</code>. If there is no such node
    *    in the tree model, then a node will be created from this child link.
    *
    * @param params The search parameters used to create a node if needed,
    *    assume not <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs
    */
   private void addChildNode(PSContentTreeNode parentNode, Link childLink,
      String rootResourceId)
      throws PSLoaderException
   {
      PSContentTreeNode childNode = m_treeModel.findNode(childLink);
      PSItemContext childItem = null;
      if (childNode == null)
      {
         PageData data = getPageData(childLink.getURL());
         childItem = createItemContext(childLink, data, rootResourceId);
      }
      else
         childItem = childNode.getItemContext();

      m_treeModel.addNode(childLink, childItem, parentNode);
   }

   /**
    * See getTotalCrawlerPages() for description.
    */
   private int m_totalVisitedPages = 0;

   /**
    * The tree model contains scanned nodes, never <code>null</code>, but
    * may be empty.
    */
   private PSContentTreeModel m_treeModel = new PSContentTreeModel();

   /**
    * Vector of listeners to the import process. Initialized in
    * definition, never <code>null</code>
    */
   private Vector m_vCrawlListeners = new Vector();

   /**
    * Determines whether stop() has been called.
    */
   private boolean m_stop = false;

   /**
    * Indicate whether the checksum will be calculated by the file crawler. 
    * <code>true</code> the checksum will be calculated; <code>false</code> 
    * the checksum will not be calculated. Initialized by the constructor.
    */
   private boolean m_calcChecksum;
   
   /**
    * Inner class contains scanned page data
    */
   private class PageData
   {
      /**
       * Construct page data from raw data, content type and check sum,
       * which are retrieved from a file.
       */
      PageData(byte[] rawData, String contentType, long checkSum)
      {
         m_rawData = rawData;
         m_contentType = contentType;
         m_checkSum = checkSum;
      }

      byte[] m_rawData;
      String m_contentType;
      long m_checkSum;
   }

}

/*[ PSFileSelector.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.IPSContentSelector;
import com.percussion.loader.IPSContentTree;
import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.IPSProgressListener;
import com.percussion.loader.IPSStatusListener;
import com.percussion.loader.PSConfigurationException;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSProgressEvent;
import com.percussion.loader.PSStatusEvent;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSFileSearchRoot;
import com.percussion.loader.objectstore.PSSearchRoot;
import com.percussion.loader.ui.IPSUIPlugin;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSFileSelectorEditorPanel;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Element;

import websphinx.Link;

/**
 * This class handles scanning/retreiving content from file system.
 *
 * @see com.percussion.loader.selector.PSWebContentSelector
 */
public class PSFileSelector 
   implements IPSContentSelector, IPSCrawlListener, IPSUIPlugin
{
   /**
    * IPSPlugin interface implementation
    */
   public PSConfigPanel getConfigurationUI()
   {
      return new PSFileSelectorEditorPanel();
   }
   
   /**
    * IPSPlugin interface implementation
    */

    // @see {@link com.percussion.loader.IPSPlugin} for description
   public void configure(Element config)
      throws PSConfigurationException
   {
      try
      {
         PSContentSelectorDef def = new PSContentSelectorDef(config);
         initializeCrawler(def.getSearchRoots(), def.calcChecksum());
      }
      catch (Exception e)
      {
         throw new PSConfigurationException(
            IPSLoaderErrors.INIT_PLUGIN_ERROR,
            new Object []
            {
               PSWebContentSelector.class.getName(),
               "Error attempting to configure plugin."
            });
      }
   }

   /**
    * IPSContentSelector interface implementation
    */

   // @see {@link com.percussion.loader.IPSContentSelector} for description
   public IPSContentTree scan()
      throws PSScanException
   {
      if (m_crawler == null)
         throw new IllegalStateException("m_crawler must not be null.");

      /**
       * Add crawl listeners to 'listen' to the scan process
       */
      m_crawler.addCrawlListener(this);

      /**
       * Start the crawler processing. The crawler implements Runnable
       * and may be run in its own Thread.
       */
      m_crawler.start();

      return new PSDefaultContentTree(m_crawler.getContentTree());
   }

   // @see {@link com.percussion.loader.IPSContentSelector} for description
   public InputStream retrieve(IPSContentTreeNode node)
      throws IOException
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null");
         
      PSItemContext item = node.getItemContext();

      if (item == null)
         throw new IllegalArgumentException(
            "Descriptor did not originate from this plugin.");

      String resId = item.getResourceId();
      Link link = new Link(resId);

      URLConnection conn = link.getURL().openConnection();
      conn.connect();
      return conn.getInputStream();
   }

   // @see {@link com.percussion.loader.IPSContentSelector} for description
   public void addProgressListener(IPSProgressListener listener)
   {
      if (!m_vProgressListeners.contains(listener))
         m_vProgressListeners.addElement(listener);
   }

   // @see {@link com.percussion.loader.IPSContentSelector} for description
   public void removeProgressListener(IPSProgressListener listener)
   {
      m_vProgressListeners.removeElement(listener);
   }

   // @see {@link com.percussion.loader.IPSContentSelector} for description
   public void abort()
   {
      if (m_crawler == null)
         throw new IllegalStateException("crawler must not be null.");

      m_crawler.stop();
   }

   /**
    * Updates the progress listeners with the current number of crawled files
    *
    * @param strResId a string resourceId. May be <code>null</code>.
    */
   private void sendProgressEvent(String resId)
   {
     PSProgressEvent evt = new PSProgressEvent(
         this,
         m_crawler.getPagesVisited(),
         resId);

      for (int i=0; i<m_vProgressListeners.size(); i++)
      {
         Object obj = m_vProgressListeners.elementAt(i);

         if (obj instanceof IPSProgressListener)
            ((IPSProgressListener) obj).progressChanged(evt);
      }
   }

   /**
    * Updates the progress listeners with a status of the selector.
    *
    * @param evt The to be send event, assume not <code>null</code>.
    *
    * @throws IllegalArgumentException if evt is <code>null</code>.
    */
   private void sendStatusEvent(PSCrawlEvent evt)
   {
      PSStatusEvent eStatus = null;

      /**
       * Translate the crawler's status into a higher level
       * status representation.
       */
      switch (evt.getStatus())
      {
         case PSCrawlEvent.STARTED:
            eStatus = new PSStatusEvent(this, PSStatusEvent.STATUS_STARTED,
               PSStatusEvent.PROCESS_SCANNING);
            break;
         case PSCrawlEvent.STOPPED:
            eStatus = new PSStatusEvent(this, PSStatusEvent.STATUS_COMPLETED,
               PSStatusEvent.PROCESS_SCANNING);
            break;
         case PSCrawlEvent.PAUSED:
         case PSCrawlEvent.CLEARED:
            eStatus = new PSStatusEvent(this, PSStatusEvent.STATUS_ABORTED,
               PSStatusEvent.PROCESS_SCANNING);
            break;
      }

      if (eStatus == null)
         return;

      /**
       * Send the event to all the listeners
       */
     for (int i=0; i<m_vProgressListeners.size(); i++)
      {
         Object obj = m_vProgressListeners.elementAt(i);

         if (obj instanceof IPSStatusListener)
            ((IPSStatusListener) obj).statusChanged(eStatus);

      }
   }

   /**
    * Helper method to initialize the crawler with some default properties.
    *
    * @param searchRoots Iterator over a collection
    *    of {@link com.percussion.loader.objectstore.PSSearchRoot}, assume
    *    it is not <code>null</code>.
    * 
    * @param calcChecksum Indicate whether the selector should calculate
    *    the checksum for the scanned files. <code>true</code> if the 
    *    checksum should be calculated; <code>false</code> otherwise.
    * 
    * @throws PSConfigurationException if an error occurs the initialization.
    */
   private void initializeCrawler(Iterator searchRoots, boolean calcChecksum)
      throws PSConfigurationException
   {
      m_crawler = new PSFileCrawler(calcChecksum);
      
      PSSearchRoot aRoot = null;
      PSFileSearchRoot fileSearchRoot = null;
      Element rootEl = null;

      try 
      {
         /**
          * Load all the search roots
          */
         while (searchRoots.hasNext())
         {
            aRoot = (PSSearchRoot) searchRoots.next();
            rootEl = aRoot.toXml(PSXmlDocumentBuilder.createXmlDocument());
            fileSearchRoot = new PSFileSearchRoot(rootEl);
            m_crawler.addSearchRoot(fileSearchRoot);
         }
      }
      catch (Exception e)
      {
         throw new PSConfigurationException(IPSLoaderErrors.UNEXPECTED_ERROR, 
            e.toString());
      }
   }

   /**
    * IPSCrawlListener interface implementation
    */

   // see {@link #IPSCrawlListener} for description.
   public void visited(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      PSItemContext itemC = event.getItemContext();

      if (itemC != null)
         sendProgressEvent(itemC.getResourceId());
   }

   // see {@link #IPSCrawlListener} for description.
   public void started(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   // see {@link #IPSCrawlListener} for description.
   public void stopped(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   // see {@link #IPSCrawlListener} for description.
   public void cleared(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   // see {@link #IPSCrawlListener} for description.
   public void timedOut(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   // see {@link #IPSCrawlListener} for description.
   public void paused(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   /**
    * Attributes
    */

   /**
    * Container of {@link #com.percussion.loader.IPSProgressListener}
    * listening to the selection process, initialized in definition,
    * never <code>null</code>, but may be empty.
    */
   protected Vector m_vProgressListeners = new Vector();

   /**
    * File crawler, intialized in {@link #initializeCrawler} called
    * from {@link #configure}, Never <code>null</code> after that.
    */
   private PSFileCrawler m_crawler = null;
   
}

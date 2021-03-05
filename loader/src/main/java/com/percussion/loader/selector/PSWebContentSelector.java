/******************************************************************************
 *
 * [ PSWebContentSelector.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
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
import com.percussion.loader.objectstore.PSFilter;
import com.percussion.loader.objectstore.PSLoaderComponent;
import com.percussion.loader.objectstore.PSNameValuePair;
import com.percussion.loader.objectstore.PSProperty;
import com.percussion.loader.objectstore.PSSearchRoot;
import com.percussion.loader.ui.IPSUIPlugin;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSFileSelectorEditorPanel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Element;

import websphinx.DownloadParameters;
import websphinx.Link;

/**
 * This class is equivalent with PSFileSelector, but it is implemented by using 
 * PSCawler. There are a lot of problems with the crawler, such as uses a lot 
 * of memory, threads, complicated interface, ...  This class should not be 
 * used but as a reference only.
 * <p>
 * This class is the main entry point for scanning/retreiving
 * content from data sources. This class uses a {@link #PSCrawler}
 * to scan a web site or file system and represents the content in a
 * {@link #PSContentTreeModel}.
 *
 * @see #PSContentTreeModel
 * @see #PSCrawler
 */
public class PSWebContentSelector
   implements IPSContentSelector, IPSCrawlListener, IPSUIPlugin
{
   /**
    * Default constructor
    */
   public PSWebContentSelector()
   {
   }

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

   /**
    * @see {@link com.percussion.loader.IPSPlugin} for description
    */
   public void configure(Element config)
      throws PSConfigurationException
   {
      try
      {
         PSContentSelectorDef def = new PSContentSelectorDef(config);
         Iterator iter = def.getSearchRoots();
         initializeCrawler(def.getSearchRoots(), def.getProperties());
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

   /**
    * @see {@link com.percussion.loader.IPSContentSelector} for description
    */
   public IPSContentTree scan()
      throws PSScanException
   {
      if (m_crawler == null)
         throw new IllegalStateException("Crawler must not be null.");

      PSContentTreeModel m = new PSContentTreeModel();

      /**
       * Add crawl listeners to 'listen' to the scan process
       */
      m_crawler.addCrawlListener(m);
      m_crawler.addCrawlListener(this);

      /**
       * Start the crawler processing. The crawler implements Runnable
       * and may be run in its own Thread.
       */
      m_crawler.start();

      return new PSDefaultContentTree(m);
   }

   /**
    * @see {@link com.percussion.loader.IPSContentSelector} for description
    */
   public InputStream retrieve(IPSContentTreeNode descriptor)
      throws IOException
   {
      PSItemContext item = descriptor.getItemContext();

      /**
       * Threshold
       */
      if (item == null)
         throw new
            IllegalArgumentException(
            "Descriptor did not originate from this plugin.");

      String strResId = item.getResourceId();
      Link origin = new Link(strResId);

      URLConnection conn = origin.getURL().openConnection();
      conn.connect();
      return conn.getInputStream();
   }

   /**
    * @see {@link com.percussion.loader.IPSContentSelector} for description
    */
   public void addProgressListener(IPSProgressListener listener)
   {
      if (!m_vProgressListeners.contains(listener))
         m_vProgressListeners.addElement(listener);
   }

   /**
    * @see {@link com.percussion.loader.IPSContentSelector} for description
    */
   public void removeProgressListener(IPSProgressListener listener)
   {
      m_vProgressListeners.removeElement(listener);
   }

   /**
    * @see {@link com.percussion.loader.IPSContentSelector} for description
    */
   public void abort()
   {
      if (m_crawler == null)
         throw new IllegalStateException("crawler must not be null.");

      m_crawler.stop();
      //m_crawler.clear();
   }

   /**
    * Updates the progress listeners with a estimated percent of
    * completion.
    *
    * @param strResId a string resourceId. May be <code>null</code>.
    */
   protected void sendProgressEvent(String strResId)
   {
     PSProgressEvent evt = new PSProgressEvent(
         this,
         m_crawler.getPagesVisited(),
         strResId);

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
    * @param evt {@link #PSCrawlEvent} Never <code>null</code>.
    *
    * @throws IllegalArgumentException if evt is <code>null</code>.
    */
   protected void sendStatusEvent(PSCrawlEvent evt)
   {
      if (evt == null)
         throw new IllegalArgumentException("Event must not be null.");

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

      /**
       * Threshold
       */
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
    * Helper method to initialize the crawler with
    * some default properties.
    *
    * @param searchRoots Iterator over a collection
    *    of {@link com.percussion.loader.objectstore.PSSearchRoot}
    * @param props Iterator a collection of {@link
    *    com.percussion.loader.objectstore.PSLoaderComponent}
    *    objects.
    *
    * @throws PSConfigurationException if an error occurs reading
    *    the configuration.
    */
   private void initializeCrawler(Iterator searchRoots, Iterator props)
      throws PSConfigurationException
   {
      /**
       * Default base most configuration - these
       * will be overwritten below if they are
       * supplied in the properties Iterator.
       */
      m_crawler = new PSCrawler();
      m_crawler.setDomain(websphinx.Crawler.SUBTREE);
      m_crawler.setLinkType(websphinx.Crawler.ALL_LINKS);
      m_crawler.addClassifier(new websphinx.StandardClassifier());
      DownloadParameters dp = DownloadParameters.NO_LIMITS;
      // the program some times running forever, not sure why yet. To reduce
      // the problem happen, use 1 thread for now.
      dp.changeMaxThreads(1);
      m_crawler.setDownloadParameters(dp);

      PSSearchRoot aRoot = null;
      PSProperty prop = null;

      /**
       * Load all the search roots
       */
      while (searchRoots.hasNext())
      {
         aRoot = (PSSearchRoot) searchRoots.next();

         /**
          * Check what types of PSLoaderComponent exist
          * and setup accordingly.
          */
         prop = aRoot.getProperty(PSFileSearchRoot.XML_SEARCHROOT_NAME);

         if (prop != null)
         {
            try
            {
               String strRootVal = prop.getValue();
               java.net.URL url = null;

               if (!strRootVal.startsWith("file://"))
               {
                  url = new java.net.URL(
                     "file", "localhost", "/" + strRootVal);
               }
               else
               {
                  url = new java.net.URL(strRootVal);
               }
               websphinx.Link link = new websphinx.Link(url);
               m_crawler.addRoot(link);
               String recurse =
               aRoot.getProperty(PSFileSearchRoot.XML_RECURSE_NAME).getValue();

               if (recurse != null && recurse.length() != 0 &&
               !recurse.equalsIgnoreCase(PSFileSearchRoot.XML_TRUE))
                  m_crawler.setRecursionDepthForSearch(link, false);
               else
                  m_crawler.setRecursionDepthForSearch(link, true);
            }
            catch (Exception e)
            {
               throw new PSConfigurationException(
                  IPSLoaderErrors.INIT_PLUGIN_ERROR,
                  new Object []
                  {
                     PSWebContentSelector.class.getName(),
                     e.getMessage()
                  });
            }
         }

         /**
          * Register any filters
          */
         Iterator filters = aRoot.getFilters();
         PSFilter f = null;

         while (filters.hasNext())
         {
            f = (PSFilter) filters.next();

            if (f != null)
            {
               PSRegExObjectFilter regFil = new PSRegExObjectFilter();
               regFil.setFilter(f.getValue());

               m_crawler.addObjectFilter(regFil);
            }
         }
      }

      /**
       * Load all the properties
       */
      PSLoaderComponent aComp = null;

      while (props.hasNext())
      {
         aComp = (PSLoaderComponent) props.next();

         /**
          * This is where the user added properties
          * get interpreted
          */
         if (aComp instanceof PSNameValuePair)
         {
            PSNameValuePair pair = (PSNameValuePair) aComp;

            if (pair.getName().equals("DOMAIN"))
            {
               m_crawler.setDomain(new String [] { pair.getValue() });
            }
            /**
             *  ... Whatever user properties become necessary
             *  configure them here ...
             */

            /**
             * @todo recurse property from ui -- don't forget!!
             */
         }
      }
   }

   /**
    * IPSCrawlListener interface implementation
    */

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void visited(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      PSItemContext itemC = event.getItemContext();

      if (itemC != null)
         sendProgressEvent(itemC.getResourceId());
   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void started(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void stopped(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void cleared(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void timedOut(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      sendStatusEvent(event);
   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
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
    * Web crawler, intialized in {@link #initializeCrawler} called
    * from {@link #configure}, Never
    * <code>null</code>.
    */
   private PSCrawler m_crawler = null;
}

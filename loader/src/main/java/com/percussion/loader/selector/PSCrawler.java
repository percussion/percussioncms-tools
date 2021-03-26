/*[ PSCrawler.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.PSItemContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;

import websphinx.CrawlEvent;
import websphinx.Crawler;
import websphinx.Link;
import websphinx.LinkEvent;
import websphinx.Page;

/**
 * NOTE: This class is used by PSWebContentSelector only. It should not be used
 * until we support web crawler, so this is used for future reference only.
 *
 * See {@link #Crawler} for base class descripion.
 * This class extends the functionality of {@link websphinx.Crawler}
 * by adding regular expression filtering using {@link #PSObjectFilter}
 * in {@link #shouldVisit}. This class also provides for the ability
 * to have {@link #PSLinkExtractor} operate on each page as they are
 * scanned in {@link #expand}.
 *
*/
public class PSCrawler extends Crawler implements IPSSelector
{
   /**
    * Construction
    */
   public PSCrawler()
   {
      /**
       * Base class construction
       */
      super();

      /**
       * Storage initialization
       */
      m_vCrawlListeners    = new Vector();
      m_vFilters           = new Vector();
      m_lExFactory         = new PSLinkExtractorFactory();
   }

   /**
    * @see {@link #IPSSelector} for description.
    */
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

   /**
    * @see {@link #IPSSelector} for description.
    */
   public void removeCrawlListener(IPSCrawlListener aListener)
   {
      if (aListener == null)
         throw new IllegalArgumentException(
            "Attempting to remove null crawl listener.");

      m_vCrawlListeners.removeElement(aListener);
   }

   /**
    * @see {@link #IPSSelector} for description.
    */
   public void stop()
   {
      super.stop();
   }

   /**
    * @see {@link #IPSSelector} for description.
    */
   public void start()
   {
      if (getState() == CrawlEvent.STOPPED)
         clear();

      run();

      m_callListenerClear = false;
      clear();
      m_callListenerClear = true;
   }

   /**
    * @see {@link #IPSSelector} for description.
    */
   public void pause()
   {
      super.pause();
   }

   /**
    * @see {@link #IPSSelector} for description.
    */
   public void resume()
   {
      super.run();
   }

   /**
    * @see {@link #IPSSelector} for description.
    */
   public void clear()
   {
      super.clear();
   }

   // see IPSSelector.isValidLink(Link, Object) for description
   public boolean isValidLink(Link link, Object o)
   {
      return true;
   }

   /**
    * Overriden from websphinx.Crawler
    */

    private boolean m_firstTime = true;

   /**
    * Overriding expand to customize the behavior of submitting
    * links back to the fetchQueue
    *
    * @param page websphinx.Page. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if {@link #page} is <code>null</code>.
    */
   public void expand(Page page)
   {
      if (page == null)
         throw new IllegalArgumentException("page must not be null.");

      /**
       * Perform the link extraction necessary.
       * If the content is html the link extraction is
       * already done for us. Otherwise, the content may be
       * eligible for an IPSLinkExtractor
       */
      Link srcLink = page.getOrigin();
      PSItemContext itemContext = createItemContext(srcLink);
      Link[] links = null;
      String strContentType = page.getContentType();

      if (strContentType == null
         || strContentType.startsWith ("text/html")
         || strContentType.startsWith ("content/unknown"))
      {
         links = page.getLinks();
      }
      else if (itemContext != null)
      {
         /**
          * Find the appropriate IPSLinkExtractor and extract.
          * (An example for this case would be if a word document
          * was found and let's say it contained links that needed
          * to be extracted.)
          */

         IPSLinkExtractor linkEx = m_lExFactory.getLinkExtractor(itemContext);

         if (linkEx != null)
         {
            itemContext = linkEx.onItemExtract(itemContext);

            /**
             * At this point the link extractor has pulled out any
             * links and has used a websphinx.Page object to represent
             * the content. That page object is set as the data object
             * of itemContext object.
             */

            if (itemContext.getDataObject() != null)
            {
               try
               {
                  Page p   = (Page) itemContext.getDataObject();
                  links    = p.getLinks();
               }
               catch (Exception ig)
               {
                  links = null;
               }
            }
         }
      }

      if (links != null && links.length > 0)
      {
         // give each link a default priority based on its page
         // and position on page
         float priority =
            (this.getDepthFirst() ? - this.getPagesVisited() :
            this.getPagesVisited());

         float increment = 1.0f/links.length;

         for (int i=0;  i<links.length; ++i)
         {
            Link l = links[i];

            /**
             * Set the default download parameters
             */
            l.setPriority (priority);
            priority += increment;
            l.setDownloadParameters (this.getDownloadParameters());

            /**
             * Perform some basic checks on whether or not
             * we should submit this node to the fetchQueue
             */
            if (visited(l))
            {
               if (l instanceof Link) // don't do this for Form object
                  sendLinkEvent (l, LinkEvent.ALREADY_VISITED);
            }
            else if (!shouldVisit(l))
            {
               sendLinkEvent (l, LinkEvent.SKIPPED);
            }
            else if (page.getDepth() >= this.getMaxDepth())
            {
               sendLinkEvent (l, LinkEvent.TOO_DEEP);
            }
            else
            {
               /**
                * Submitted to the fetch queue for processing
                */
               submit (l);
            }
         }
      }
   }

   /**
    * Helper method to create a {@link com.percussion.loader.PSItemContext}
    * from a webshphinx.Link object. Put any common intialization etc. in
    * this method.
    *
    * @param l {@link websphinx.Link} a link object to be stored as
    *    the data object of the item context object. Never <code>null</code>
    *
    * @return {@link com.percussion.loader.PSItemContext} a node
    *    encapsulating the Link object and any further description
    *    that may be necessary. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if {@link #l} is <code>null</code>.
    */
   public static PSItemContext createItemContext(Link l)
   {
      if (l == null)
         throw new IllegalArgumentException("link must not be null.");

      Page page = l.getPage();
      PSItemContext ps = null;
      String strContentType = "content/unknown";

      if (page != null)
         strContentType = page.getContentType();

      ps = new PSItemContext(
         l.getResourceId(),
         null,
         strContentType);

      ps.setStatus(PSItemContext.STATUS_NEW);
      ps.setDataObject(l);

      return ps;
   }

   /**
    * Tests the websphinx.Link to see if we should visit this
    * node.
    *
    * @param l {@link websphinx.Link} a Link object providing
    *    descriptive information about the content. Never <code>null</code>.
    *
    * @return boolean <code>true</code> to visit the link, otherwise
    *    <code>false</code>
    *
    * @throws IllegalArgumentException if {@link #l} is <code>null</code>.
    *
    */
   public boolean shouldVisit(Link l)
   {
      if (l == null)
         throw new IllegalArgumentException("link must not be null.");

      /**
       * If we crawl with 'file:' url's for each
       * file or directory we get back doc: nodes that
       * we do not need.
       */
      if (l.getURL().toString().indexOf("doc:") >= 0)
         return false;

      File file = new File(l.getFile());
      if (! file.exists())
         return false;

      /**
       * Make sure it is a link under one of the specified roots
       */
       Link[] roots = getRoots();
       String currUrl = l.getResourceId();
       boolean foundRoot = false;
       boolean recurse = true;
       Boolean recursionObj = null;
       for (int i=0; (i < roots.length) && (!foundRoot); i++)
       {
          recursionObj = (Boolean)m_rootRecursionMap.get(roots[i]);
          if (recursionObj != null)
             recurse = recursionObj.booleanValue();
          else
             recurse = true;
         String rootUrl = roots[i].getResourceId();
         if (currUrl.startsWith(rootUrl))
            foundRoot = true;
         if (!recurse)
         {
            if (l.getDepth() > 1)
               foundRoot = false;
            else
               foundRoot = true;
         }
       }
       if (! foundRoot)
         return false;  // skip the link which is not under one of the roots

      /**
       * Check the object filters. If any individual object
       * filter matches then we should NOT visit this node
       */
      PSItemContext aContext = createItemContext(l);

      if (aContext != null)
      {
         for (int i=0; i<m_vFilters.size(); i++)
         {
            PSRegExObjectFilter aFilter =
               (PSRegExObjectFilter) m_vFilters.elementAt(i);

            try
            {
               if (aFilter.accept(aContext))
                  return true;
            }
            catch (MalformedPatternException e)
            {
               /**
                * Ignore for now. Notify the user of invalid regular
                * expression supplied.
                */

               Logger.getInstance(PSCrawler.class.getName()).debug(e);

            }
         }
      }

      return false;
   }

   /**
    * Sets the recursion level for the search root. As of now it is either the
    * root is searched recursively or just one level deep. There is no notion of
    * multiple depths of recursion.
    *
    * @param root, Websphinx.Link object, may be <code>null</code>, if <code>
    * null</code> then the method exits silently.
    *
    * @param recurse, specified values are {@link IPSConstants.BOOLEAN_TRUE} and
    * {@link IPSConstants.BOOLEAN_FALSE}, if it is <code>null</code> or empty
    * when the root has been specified the method returns silently.
    */
   void setRecursionDepthForSearch(Link root, Boolean recurse)
   {
      if (root != null)
      {
         //System.out.println("baseURL = " + root.getResourceId());
         //setBaseURL(root.getURL());
         m_rootRecursionMap.put(root, recurse);
      }
   }

   /**
    * Overriden from {@link websphinx.Crawler}.
    * Convenience method to make any notifications necessary
    * to any listeners.
    *
    * @param id an int that represents the {@link websphinx.CrawlEvent}
    *    id.
    */
   protected void sendCrawlEvent(int id)
   {
      /**
       * Base class event notification
       */
      super.sendCrawlEvent(id);

      PSCrawlEvent evt = new PSCrawlEvent();

      for (int i=0; i<m_vCrawlListeners.size(); i++)
      {
         IPSCrawlListener listen = (IPSCrawlListener)
            m_vCrawlListeners.elementAt(i);

         /**
          * 'Translate' the event from webshpinx id to
          * our PS defines.
          */
         switch (id)
         {
            case CrawlEvent.STARTED:
               evt.setStatus(PSCrawlEvent.STARTED);
               listen.started(evt);
               break;
            case CrawlEvent.STOPPED:
               evt.setStatus(PSCrawlEvent.STOPPED);
               listen.stopped(evt);
               break;
            case CrawlEvent.CLEARED:
               if (m_callListenerClear)
               {
                  evt.setStatus(PSCrawlEvent.CLEARED);
                  listen.cleared(evt);
               }
               break;
            case CrawlEvent.TIMED_OUT:
               evt.setStatus(PSCrawlEvent.TIMEDOUT);
               listen.timedOut(evt);
               break;
            case CrawlEvent.PAUSED:
               evt.setStatus(PSCrawlEvent.PAUSED);
               listen.paused(evt);
               break;
         }
      }
   }

   /**
    * Convienve method to construct a <code>PSCrawlEvent</code>
    * from the following parameters
    *
    * @param l a <code>websphinx.Link</code> that we are visiting.
    *    Never <code>null</code>.
    *
    * @param int a status code.
    *
    * @param ex a Throwable exception that was caught while processing
    *    <code>l</code>. May be <code>null</code>.
    *
    * @throw IllegalArgumentException if any parameters are invalid.
    */
   private PSCrawlEvent createPSEvent(Link l, int id, Throwable ex)
   {
      if (l == null)
         throw new IllegalArgumentException(
            "l must not be null");

      PSItemContext context = createItemContext(l);
      PSCrawlEvent evt = new PSCrawlEvent();
      evt.setItemContext(context);

      /**
       * 'Translate' websphinx event id to our
       * PS event id.
       */
      switch (id)
      {
         case LinkEvent.NONE:
            evt.setStatus(PSCrawlEvent.NONE);
            break;

            /**
             * Link was rejected by shouldVisit()
             */
         case LinkEvent.SKIPPED:
            evt.setStatus(PSCrawlEvent.SKIPPED);
            break;

            /**
             * Link has already been visited during the crawl,
             * so it was skipped.
             */
         case LinkEvent.ALREADY_VISITED:
            evt.setStatus(PSCrawlEvent.ALREADY_VISITED);
            break;

            /**
             * Link was accepted by walk() but exceeds the maximum
             * depth from the start set.
             */
         case LinkEvent.TOO_DEEP:
            evt.setStatus(PSCrawlEvent.TOO_DEEP);
            break;

            /**
             * Link was accepted by walk() and is waiting to be downloaded
             */
         case LinkEvent.QUEUED:
            evt.setStatus(PSCrawlEvent.QUEUED);
            break;

            /**
             * Link is being retrieved
             */
         case LinkEvent.RETRIEVING:
            evt.setStatus(PSCrawlEvent.RETRIEVING);
            break;

            /**
             * An error occurred in retrieving the page.
             * The error can be obtained from getException().
             */
         case LinkEvent.ERROR:
            evt.setStatus(PSCrawlEvent.ERROR);
            break;

            /**
             * Link has been retrieved
             */
         case LinkEvent.DOWNLOADED:
            evt.setStatus(PSCrawlEvent.DOWNLOADED);
            break;

            /**
             * Link has been thoroughly processed by crawler
             */
         case LinkEvent.VISITED:
            evt.setStatus(PSCrawlEvent.VISITED);
            break;
      }

      if (ex != null)
         evt.setException(ex);

      return evt;
   }

   /**
    * Overriden from {@link websphinx.Crawler}. See base class
    * for description.
    */
   protected void sendLinkEvent (Link l, int id, Throwable exception)
   {
      super.sendLinkEvent(l, id, exception);

      PSCrawlEvent evt = createPSEvent(l, id, exception);

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
    * Overriden from {@link websphinx.Crawler}. See base class
    * for description.
    */
   protected void sendLinkEvent(Link l, int id)
   {
      /**
       * Base class event notification
       */
      super.sendLinkEvent(l, id);

      PSCrawlEvent evt = createPSEvent(l, id, null);

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
    * Add a {@link #PSRegExObjectFilter}
    *
    * @param filter {@link #PSRegExObjectFilter} object encapsulates
    *    a regular expression. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if the supplied
    *    {@link #PSRegExObjectFilter} is <code>null</code>
    *
    * @see {@link #shouldVisit}
    */
   protected void addObjectFilter(PSRegExObjectFilter filter)
   {
      if (filter == null)
         throw new IllegalArgumentException("object filter cannot be null.");

        m_vFilters.addElement(filter);
   }

   /**
    * Remove a {@link #PSRegExObjectFilter}
    *
    * @param filter {@link #PSRegExObjectFilter} object encapsulates
    *    a regular expression. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if the supplied
    *    {@link #PSRegExObjectFilter} is <code>null</code>
    *
    * @see {@link #shouldVisit}
    */
   protected void removeObjectFilter(PSRegExObjectFilter filter)
   {
      if (filter == null)
         throw new IllegalArgumentException("object filter cannot be null.");

      m_vFilters.removeElement(filter);
   }

   /**
    * This is used to avoid to call its listener's clear (to cleanup the data
    * of the listener) during call clear(). <code>true</code> if should call
    * its listener's clear.
    */
   private boolean m_callListenerClear = true;

   /**
    * Maps a search root link object {@link WebSphinx.Link} as key to recursion
    * flag as value, specifying if the root will be searched recursively or not.
    * Intialized in {@link setRecursionDepthForSearch(Link, String)}, may be
    * empty, never <code>null</code>
    */
   private Map m_rootRecursionMap = new HashMap();

   /**
    * Attributes
    */

   /**
    * Vector of IPSCrawlListeners. These listeners are sent the crawlers
    * state notification as well as the crawlers link listening.
    */
   private Vector m_vCrawlListeners = null;

   /**
    * Vector of {@ link com.percussion.loader.selector.PSRegExObjectFilter}.
    */
   private Vector m_vFilters = null;

   /**
    * {@link #PSLinkExtractorFactory} that retrieves the proper
    * link extractor per Page based on its content type.
    */
   private PSLinkExtractorFactory m_lExFactory = null;
}

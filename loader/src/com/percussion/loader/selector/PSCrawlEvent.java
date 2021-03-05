/*[ PSCrawlEvent.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.PSItemContext;

/**
 * Event that is passed from an {@link #PSCrawler} to all of that particular 
 * crawlers {@link #IPSCrawlListener} listeners. These events are sent when a
 * {@link #PSCrawler}'s state changes or when an PSItemContext node is scanned. 
 * 
 * @see IPSCrawlListener
 * @see PSCrawler
*/
public class PSCrawlEvent
{  
   /**
    * Crawler started.
    */
   public static final int STARTED = 3000;
    
   /**
    * Crawler ran out of links to crawl
    */
   public static final int STOPPED = 3001;

   /**
    * Crawler's state was cleared.
    */
   public static final int CLEARED = 3002;

   /**
    * Crawler timeout expired.
    */
   public static final int TIMEDOUT = 3003;

   /**
    * Crawler was paused.
    */
   public static final int PAUSED = 3004;

   /**
    * No event occurred on this link yet.
    */
   public static final int NONE = 4000;
    
   /**
    * Node was rejected by shouldVisit()
    */
   public static final int SKIPPED = 4001;

   /**
    * Link has already been visited during the crawl, so it was skipped.
    */
   public static final int ALREADY_VISITED = 4002;

   /**
    * Link was accepted by walk() but exceeds the maximum 
    * depth from the start set.
    */
   public static final int TOO_DEEP = 4003;

   /**
    * Link was accepted by walk() and is waiting to be downloaded
    */
   public static final int QUEUED = 4004;

   /**
    * Link is being retrieved
    */
   public static final int RETRIEVING = 4005;

   /**
    * An error occurred in retrieving the page.
    * The error can be obtained from getException().
    */
   public static final int ERROR = 4006;

   /**
    * Link has been retrieved
    */
   public static final int DOWNLOADED = 4007;

   /**
    * Link has been thoroughly processed by crawler
    */
   public static final int VISITED = 4008;

   /**
    * Default Constructor.
    * 
    */
   public PSCrawlEvent() 
   {
      m_nId          = PSCrawlEvent.NONE;
      m_itemContext  = null;
   }

   /**
    * Make a PSCrawlEvent.
    * 
    * @param id event id, one of PSCrawlEvent codes. If id is invalid
    *    {@link #m_nId} will be set to {@link #PSCrawlEvent.NONE}.
    * 
    * @param itemContext {@link com.percussion.loader.PSItemContext}
    *    May be <code>null</code>.
    * 
    * @param parent explicitly specify the parent of this 
    *    <code>itemContext</code>. May be <code>null</code>.
    */
   public PSCrawlEvent(int id, PSItemContext itemContext, 
      PSItemContext parentContext) 
   {
      switch (id)
      {
         case PSCrawlEvent.STARTED:
         case PSCrawlEvent.STOPPED:
         case PSCrawlEvent.CLEARED:
         case PSCrawlEvent.TIMEDOUT:
         case PSCrawlEvent.PAUSED:
         case PSCrawlEvent.NONE:
         case PSCrawlEvent.SKIPPED:
         case PSCrawlEvent.ALREADY_VISITED:
         case PSCrawlEvent.TOO_DEEP:
         case PSCrawlEvent.QUEUED:
         case PSCrawlEvent.RETRIEVING:
         case PSCrawlEvent.ERROR:
         case PSCrawlEvent.DOWNLOADED:
         case PSCrawlEvent.VISITED:
            m_nId = id;
            break;
         default:
            m_nId = PSCrawlEvent.NONE;
            break;
      }
     
      m_itemContext  = itemContext;
      m_parentContext = parentContext;
   }

   /**
    * Get status id of this event.
    * 
    * @return one of the status codes.
    */
   public int getStatus() 
   { 
      return m_nId; 
   }

   /**
    * Public accessors to set the state {@link #m_nId}.
    * 
    * @param nId an Integer value that corresponds to 
    *    a PSCrawlEvent id.
    */
   public void setStatus(int nId)
   {
      m_nId = nId;
   }
  
   /**
    * Public accessor for {@link m_exception} a Throwable
    * exception that was thrown during the processing of 
    * a item
    * 
    * @return Throwable May be <code>null</code>.
    */
   public Throwable getException()
   {
      return m_exception;
   }
   
   /**
    * Public accessor for {@link m_exception} a Throwable
    * exception that was thrown during the processing of 
    * a item. 
    * 
    * @param e An <code>Exception</code> that was encountered
    *    during processing a Link. Never <code>null</code>.
    * 
    * @throws IllegalArgumentException if e is <code>null</code>
    * 
    */
   public void setException(Throwable e)
   {
      if (e == null)
         throw new IllegalArgumentException(
            "e must not be null");
            
      m_exception = e;
   }

   /**
    * Public accessor for current PSItemContext being 
    * processed.
    * 
    * @return {@link #IPSItemContext} the node that the this event
    *    is working with. May be <code>null</code>.
    */
   public PSItemContext getItemContext()
   {
      return m_itemContext;
   }
   
   /**
    * Public accessor for current parent PSItemContext being 
    * processed.
    * 
    * @return {@link #IPSItemContext} the parent node that the this event
    *    is working with. May be <code>null</code>.
    */
   public PSItemContext getParentItemContext()
   {
      return m_parentContext;
   }

   /**
    * Public accessor for current PSItemContext being 
    * processed.
    * 
    * @param {@link #IPSItemContext} the node that the this event
    *    is working with. May be <code>null</code>.
    */
   public void setItemContext(PSItemContext i)
   {
      m_itemContext = i;
   }

   /**
    * State of the crawler at this point. Initialized in definition, 
    * set in ctor and may be the following values:
    * <p>
    * Possible values are
    * <table border="0">
    * <tr>
    *    <td align="right">
    *       STARTED 
    *    </td>
    *    <td>
    *       This crawler has started
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       STOPPED 
    *    </td>
    *    <td>
    *      The crawler has been stopped and all resources are cleaned up.
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       CLEARED 
    *    </td>
    *    <td>
    *       All resources have been cleared.
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       TIMEDOUT 
    *    </td>
    *    <td>
    *       The crawler has attempted to complete an action which timed out.
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       PAUSED
    *    </td>
    *    <td>
    *       The crawler has stopped but no resources have been cleared.
    *    </td>
    * </tr>
    * <tr>
    *    <td rowspan="2">
    *       Node based properties
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       NONE
    *    </td>
    *    <td>
    *       Empty node event
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       SKIPPED
    *    </td>
    *    <td>
    *       This node was skipped possibly because the crawlers shoulVisit() 
    *       returned <code>false</code>
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       ALREADY_VISITED
    *    </td>
    *    <td>
    *       The node was already processed
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       TOO_DEEP
    *    </td>
    *    <td>
    *       This node exists below the crawlers depth setting
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       QUEUED
    *    </td>
    *    <td>
    *       The node has been queued for processing
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       RETRIEVING
    *    </td>
    *    <td>
    *       Retrieving this node
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       ERROR
    *    </td>
    *    <td>
    *       An error occurred while processing this node
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       DOWNLOADED
    *    </td>
    *    <td>
    *       The node was downloaded
    *    </td>
    * </tr>
    * <tr>
    *    <td align="right">
    *       VISITED
    *    </td>
    *    <td>
    *       This node was visited
    *    </td>
    * </tr>
    */
   private int m_nId = PSCrawlEvent.NONE;
   
   /**
    * The node descriptor at this point of event firing. Initialized
    * in ctor. May be <code>null</code>
    */
   private PSItemContext   m_itemContext;
   
   /**
    * The node parent descriptor at this point of event firing.
    * Explicitly specified by the caller. Initialized in ctor. May be 
    * <code>null</code> 
    */
   private PSItemContext   m_parentContext;
 

   /**
    * The exception that was thrown at the point of firing. Initialized
    * in ctor, may be <code>null</code>
    */
   private Throwable m_exception;
}

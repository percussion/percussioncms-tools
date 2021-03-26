/*[ IPSCrawlListener.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

/**
 * Interface for 'listening' to the state of any crawler as
 * well as the 'scanning'. Implement this interface to be able to 
 * keep track of the crawling process for a given crawler. 
 * For example, an object may want to count the number or web pages scanned, 
 * see pages only over a certain size, or extract all nodes of a certain 
 * type etc.
 * 
*/
public interface IPSCrawlListener
{
   /**
    * Notify that the crawler started.
    * 
    * @param event a {@link #PSCrawlEvent}. Never
    *    <code>null</code>.
    * 
    * @throws IllegalArgumentException if event is <code>null</code>
    */
   public void started(PSCrawlEvent event);

   /**
    * Notify that the crawler ran out of links to crawl
    * 
    * @param event a {@link #PSCrawlEvent}. Never
    *    <code>null</code>.
    * 
    * @throws IllegalArgumentException if event is <code>null</code>
    */
   public void stopped(PSCrawlEvent event);

   /**
    * Notify that the crawler's state was cleared.
    * 
    * @param event a {@link #PSCrawlEvent}. Never
    *    <code>null</code>.
    * 
    * @throws IllegalArgumentException if event is <code>null</code>
    */
   public void cleared(PSCrawlEvent event);

   /**
    * Notify that the crawler timed out. We may set
    * the crawlers timeout for attempting to download
    * a page.
    * 
    * @param event a {@link #PSCrawlEvent}. Never
    *    <code>null</code>.
    * 
    * @throws IllegalArgumentException if event is <code>null</code>
    */
   public void timedOut(PSCrawlEvent event);

   /**
    * Notify that the crawler was paused.
    * 
    * @param event a {@link #PSCrawlEvent}. Never
    *    <code>null</code>.
    * 
    * @throws IllegalArgumentException if event is <code>null</code>
    */
   public void paused(PSCrawlEvent event);

   /**
    * A node was visited. A node may be visited on many occassions
    * and the {@link #PSCrawlEvent} will encapsulate this behavior.
    * For example, a node may be Visited, Already Visited, 
    * downloaded, queued, too deep to reach etc... this information
    * is encapsulated in the event object.
    * 
    * @param event a {@link #PSCrawlEvent}. Never
    *    <code>null</code>.
    * 
    * @throws IllegalArgumentException if event is <code>null</code>
    * 
    * @see {@link PSCrawlEvent}
    */
   public void visited(PSCrawlEvent event);
}

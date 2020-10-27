/*[ IPSSelector.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import websphinx.Link;

/**
 * Interface for a selector behavior. Encapsulated high level
 * behavior such as starting, starting etc. and notification to
 * listeners of these states. This interface should expand or contract
 * based on the scanning sources that may come down the pike later.
 * 
*/
public interface IPSSelector
{
   /**
    * Start to select
    */
   public void start();
   
   /**
    * Stop the selection
    */
   public void stop();
   
   /**
    * Clear out the selection data and resseting for a 
    * clean select.
    */
   public void clear();

   /**
    * Stop without a <code>clear</code>
    */
   public void pause();

   /**
    * Add a listener to the selection process.
    * 
    * @param l An {@link #IPSCrawlListener} to add.
    *    Never <code>null</code>.
    * 
    * @throws IllegalArgumentException if l is <code>null</code>
    */
   public void addCrawlListener(IPSCrawlListener l);
   
   /**
    * Remove a listener to the selection process.
    * 
    * @param l An {@link #IPSCrawlListener} to add.
    *    Never <code>null</code>.
    * 
    * @throws IllegalArgumentException if l is <code>null</code>
    */
   public void removeCrawlListener(IPSCrawlListener l);
   
   /**
    * Validating a given link.
    * 
    * @param link The to be tested link, cannot be <code>null</code>
    * 
    * @param context the context of the current crawler, used to validate
    *    the link. It may be <code>null</code>.
    *     
    * @return <code>true</code> if it is a valid link; otherwise return
    *    <code>false</code>.
    */
   public boolean isValidLink(Link link, Object context);
}

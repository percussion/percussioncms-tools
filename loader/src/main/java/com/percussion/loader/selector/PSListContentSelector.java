/*[ PSListContentSelector.java ]***********************************************
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
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSProgressEvent;
import com.percussion.loader.PSStatusEvent;
import com.percussion.loader.objectstore.PSListSelectorDef;
import com.percussion.loader.ui.IPSUIPlugin;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSListContentEditorPanel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Vector;

import org.w3c.dom.Element;

import websphinx.Link;

/**
 * This class is the main entry point for scanning/retreiving
 * content from a xml file described by ListContentSelector.dtd.
 * This class imports the link structure defined in the supplied file
 * into a PSContentTreeModel.
 *
 * @see #PSContentTreeModel
 */
public class PSListContentSelector implements IPSContentSelector, IPSUIPlugin,
   IPSCrawlListener
{
   /**
    * @see {@link com.percussion.loader.IPSPlugin} for description
    */
   public void configure(Element config)
      throws PSConfigurationException
   {
      try
      {
         m_def = new PSListSelectorDef(config);
      }
      catch (PSLoaderException ex)
      {
         throw new PSConfigurationException(ex);
      }
      catch (Exception e)
      {
         Object [] args = {PSListContentSelector.class.getName(), e.toString()};
         throw new PSConfigurationException(
            IPSLoaderErrors.INIT_PLUGIN_ERROR, args);
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

      try
      {
         if (m_importer == null)
            m_importer = new PSListImporter(m_def.getContentList(), 
               m_def.calcChecksum());
      }
      catch (Exception e)
      {
         Object [] args = {PSListContentSelector.class.getName(), e.toString()};
         throw new PSScanException(
            IPSLoaderErrors.INIT_PLUGIN_ERROR, args);
      }

      m_importer.addCrawlListener(this);
      m_importer.start();

      return new PSDefaultContentTree(m_importer.getContentTree());
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
      m_importer.stop();
   }

   /**
    * Updates the progress listeners with a estimated percent of
    * completion.
    * 
    * @param strResId string resource id. May be <code>null</code>.
    */
   protected void sendProgressEvent(String strResId)
   {
      PSProgressEvent evt = new PSProgressEvent(
         this,
         m_importer.getPagesVisited(),
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
    * IPSPlugin interface implementation
    */
   public PSConfigPanel getConfigurationUI()
   {
      return new PSListContentEditorPanel();
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
    * Web crawler, intialized in {@link #configure}, never
    * <code>null</code> after that.
    */
   private PSListImporter m_importer = null;

   /**
    * The definition of the list selector, intialized in {@link #configure},
    * never <code>null</code> after that.
    */
   private PSListSelectorDef m_def = null;
}

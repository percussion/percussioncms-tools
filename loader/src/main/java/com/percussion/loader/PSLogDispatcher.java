/*[ PSLogDispatcher.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This class is a log4j appender that dispatches received logging events to
 * all registered listeners. 
 */
public class PSLogDispatcher extends AppenderSkeleton
{
   /**
    * @see {@link org.apache.log4j.append#addChangeListener(LoggingEvent)}
    */    
   public void append(LoggingEvent event)
   {
      for (int i=0; i<m_listeners.size(); i++)
      {
         IPSLogListener listener = (IPSLogListener) m_listeners.get(i);
         listener.logReceived(event, this);
      }
   }

   /**
    * @see {@link org.apache.log4j.append#close(int)}
    */    
   public void close()
   {
      // no-op
   }

   /**
    * @see {@link org.apache.log4j.append#requiresLayout()}
    */    
   public boolean requiresLayout()
   {
      return true;
   }

   /**
    * Register to receive logging events.
    * 
    * @param listener the listener that will receive the logging events, not
    *    <code>null</code>.
    * @throws IllegalArgumentException if the supplied listener is 
    *    <code>null</code>.
    */
   public void addLogListener(IPSLogListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener cannot be null");

      m_listeners.add(listener);
   }

   /**
    * Unregister from reveiving logging events.
    * 
    * @param listener the listener to unregister, not <code>null</code>.
    * @throws IllegalArgumentException if the supplied listener is 
    *    <code>null</code>.
    */
   public void removeLogListener(IPSLogListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener cannot be null");

      m_listeners.remove(listener);
   }

   /**
    * A collection of registered listeners. Never <code>null</code>, might be
    * empty.
    */
   private List m_listeners = new Vector();
}

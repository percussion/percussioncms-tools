/*[ IPSLogListener.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.util.EventListener;

import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * All classes interested in logging events dispatched through the 
 * <code>PSLogDispatcher</code> should implement this interface and register
 * with the <code>PSLogDispatcher</code> to receive logging events.
 */
public interface IPSLogListener extends EventListener
{
   /**
    * This method is called for each logging event.
    * 
    * @param event the log event, never <code>null</code>.
    * @param event the log appender who was dispatching this event, 
    *    never <code>null</code>.
    */
   public void logReceived(LoggingEvent event, Appender appender);
}

/*******************************************************************************
 *
 * [ PSLogonStateChangedEvent.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client;

/**
 * A simple wrapper for the LogonStateEvents enum value to identify the 
 * event type that caused the notification.
 * <p>
 * This class is immutable.
 * 
 * @author paulhoward
 * @version 6.0
 */
public class PSLogonStateChangedEvent
{
   /**
    * Used to specify which action caused this event.
    */
   public enum LogonStateEvents
   {
      LOGON,
      LOGOFF
   }
   
   /**
    * The only ctor. This class is immutable.
    * 
    * @param eventType Never <code>null</code>.
    */
   public PSLogonStateChangedEvent(LogonStateEvents eventType)
   {
      assert(eventType != null);
      m_eventType = eventType;
   }

   /**
    * Returns the event type that was used to create this event.
    *  
    * @return Never <code>null</code>.
    */
   public LogonStateEvents getEventType()
   {
      return m_eventType;
   }

   /**
    * The event type that generated this event. Set in ctor, then never changed. 
    */
   private LogonStateEvents m_eventType;
}
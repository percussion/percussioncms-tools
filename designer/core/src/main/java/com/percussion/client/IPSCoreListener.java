/******************************************************************************
 *
 * [ IPSCoreListener.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

/**
 * This interface is used to provide notification of logon/logoff events to
 * interested parties that have registered their interest. See
 * {@link com.percussion.client.PSLogonStateChangedEvent.LogonStateEvents}
 * for details about the events.
 * 
 * @author paulhoward
 * @version 6.0
 */
public interface IPSCoreListener
{
   /**
    * <p>This method will be called after a successful logon or logoff has been
    * completed if that action was initiated by a client. Auto-connect logic 
    * inside the core does not cause this event to be fired.</p>
    *
    * <p>Note, the method can be called from a non-UI thread.</p>
    *  
    * @param event Indicates the type of event that caused this notification;
    * either a logon or logoff.
    */
   public void logonStateChanged(PSLogonStateChangedEvent event);
}

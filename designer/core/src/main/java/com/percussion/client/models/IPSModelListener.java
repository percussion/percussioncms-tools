/*******************************************************************************
 *
 * [ IPSodelListener.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.models;

import com.percussion.client.PSModelChangedEvent;

/**
 * Classes that want to register for notifications for changes in a model must
 * implement this interface and pass it as the listener when registering.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public interface IPSModelListener
{
   /**
    * Whenever a significant event occurs (after successful complete,) this
    * method will be called on registered listeners.
    *  
    * @param event Identifies who caused the event and what the event was. 
    * Never <code>null</code>.
    */
   public void modelChanged(PSModelChangedEvent event);
}

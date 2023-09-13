/******************************************************************************
 *
 * [ IPSDataChangeNotifier.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

/**
 * The interface that needs to be implemented by the classes which need to 
 * inform listeners of changes in its data.
 */
public interface IPSDataChangeNotifier
{
   /**
    * Adds the listener that is interested in the data change to its list of
    * listeners.
    * 
    * @param listener the view listener that is to be notified when a data 
    * change occurs, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>
    */
   public void addDataChangeListener(IPSViewDataChangeListener listener);
   
   /**
    * Removes the listener from the list of data change listeners.
    * 
    * @param listener the view listener that is to be removed, may be <code>
    * null</code>
    */
   public void removeDataChangeListener(IPSViewDataChangeListener listener);
}

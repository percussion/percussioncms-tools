/******************************************************************************
 *
 * [ IPSConfigPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.PSLoaderException;

import org.w3c.dom.Element;

/**
 * An interface that must be implemented from all editor panels written for
 * the configuration dialog.
 */
public interface IPSConfigPanel
{
   /**
    * Loads it's components from the supplied source.
    *
    * @param source object from where this panel should initialize its
    *    components, not <code>null</code>.
    * 
    * @throws PSLoaderException if an error occurs.
    */
   public void load(Element source)
      throws PSLoaderException;

   /**
    * Validates all panel components.
    *
    * @return <code>true</code> if all content is valid, <code>false</code>
    *    otherwise.
    */
   public boolean validateContent();

   /**
    * Saves it's components to the supplied target.
    *
    * @return object which contains the edited data, never <code>null</code>.
    * 
    * @throws PSLoaderException if an error occurs.
    */
   public Element save()
      throws PSLoaderException;
   
   /**
    * Resets the data in the panel.
    */
   public void reset()
      throws PSLoaderException;
   
   /**
    * Get the name of the config panel. It may be the name of the current
    * definition. 
    * 
    * @return The current name. It may be <code>null</code> if it cannot be 
    *    modified.
    */
   public String getName();

   /**
    * Adds the supplied change listener to the list of listeners that will be
    * informed if any component changes in this editor panel.
    *
    * @param listener the listener to be informed with all changes, not
    *    <code>null</code>.
    */
   public void addChangeListener(IPSConfigChangeListener listener);

   /**
    * Removes the supplied change listener.
    *
    * @param listener the listener to be removed frmo receiving configuration
    *    change events, not <code>null</code>.
    */
   public void removeChangeListener(IPSConfigChangeListener listener);

}

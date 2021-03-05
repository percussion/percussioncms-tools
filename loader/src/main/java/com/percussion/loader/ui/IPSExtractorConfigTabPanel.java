/*[ IPSExtractorConfigTabPanel.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.PSLoaderException;

/**
 * An interface that must be implemented for all extractor config tab panels.
 */
public interface IPSExtractorConfigTabPanel
{

   /**
    * Validates the panels content
    *
    * @return <code>true</code> if the content is valid, else
    *    <code>false</code>.
    */
   public boolean validateContent();

   /**
    * Loads data into this extractor's config panel
    *
    * @param config the configuration context, may not be <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public void load(PSExtractorConfigContext config)
      throws PSLoaderException;

   /**
    * Saves data from this extractor's config panel
    *
    * @param config the configuration context, may not be <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public void save(PSExtractorConfigContext config)
      throws  PSLoaderException;

   /**
    * Resets this panel so that all previous user selections
    * will no longer exist.
    *
    * @param config the configuration context, may not be <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public void reset(PSExtractorConfigContext config)
      throws PSLoaderException;

   /**
    * Adds an <code>IPSExtractorConfigChangeListener</code> to this
    * panel's listener list.
    *
    * @param listener an <code>IPSExtractorConfigChangeListener</code>. Can
    * not be <code>null</code>
    */
   public void addChangeListener(IPSExtractorConfigChangeListener listener);

   /**
    * Removes an <code>IPSExtractorConfigChangeListener</code> from this
    * panel's listener list.
    *
    * @param listener an <code>IPSExtractorConfigChangeListener</code>. Can
    * not be <code>null</code>
    */
   public void removeChangeListener(IPSExtractorConfigChangeListener listener);

   /**
    * Returns the current name field value if it exists. It is generally
    * used to update the tree node in the content descriptor tree.
    *
    * @return string representing the extractor panel name. May be
    * <code>null</code>.
    */
   public String getName();

}
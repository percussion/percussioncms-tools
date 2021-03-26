/*[ IPSContentLoader.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;


/**
 * See base interface for a more generic description.
 * Plugins implementing this interface fit into the migration/loader model as
 * the loader of content. Each plugin is responsible for uploading content to
 * the configured target system.
 */
public interface IPSContentLoader extends IPSPlugin
{
   /**
    * Load the supplied item to the target system. This method is responsible
    * to update information data like content id and revision in the supplied
    * item after a successful upload.
    *
    * @param item the item to load, not <code>null</code>.
    * 
    * @throws PSLoaderException if any error occurs.
    */
   public void loadContentItem(PSItemContext item) throws PSLoaderException;

   /**
    * Load the supplied static item to the target system.
    *
    * @param item the item to load, not <code>null</code>.
    * 
    * @throws PSLoaderException if any error occurs.
    */
   public void loadStaticItem(PSItemContext item) throws PSLoaderException;
}

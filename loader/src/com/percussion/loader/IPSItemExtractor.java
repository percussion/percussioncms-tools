/*[ IPSItemExtractor.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.io.InputStream;

import org.w3c.dom.Element;

/**
 * See base interface for a more generic description.
 * Plugins implementing this interface fit into the migration/loader model as 
 * the analyzer of content. Each plugin is responsible for analyzing a resource
 * and determining if it fits the pattern of the content type for which it was
 * written. The analysis could be very simple or very complex.
 * <p>The manager of the loading process gives each plugin an opportunity to 
 * 'view' the resource and make a determination if it contains 1 or more 
 * instances of the content type / variant of interest to this plugin. It is 
 * acceptable for multiple plugins to express interest in the same resource.
 * <p>The plugin should extract all instances of interest in a single pass and
 * return them as seperate items.
 */
public interface IPSItemExtractor extends IPSPlugin
{
   /**
    * This method is called by the loader manager to 'guess' the content 
    * type of the supplied resource. This information may then be provided to
    * a user for correction/amplification. Each plugin will have its own
    * method for making this determination.
    * 
    * @param resource The data to be analyzed, including its meta data. Never 
    *    <code>null</code>.
    * 
    * @return The number of items that would be created if the {@link 
    *    #extractItems(PSItemContext, InputStream)} method was called with 
    *    the supplied resource.
    */
   public int containsInstances( PSItemContext resource );
   
   
   /**
    * Performs an analysis of the supplied resource data and extracts all found
    * instances appropriate for this plugin. Will return as many items as 
    * the count returned by {@link #containsInstances}. If the supplied
    * resource contains no instances, an empty array is returned. If any
    * instances are found, the supplied resource will be the first item in the
    * returned array.
    * <p>The current implementation will only process the first item from the
    * returned array. The rest of the items will not be processed.
    * 
    * @param resource the data that contains the instances, never <code>null
    *    </code>.
    *  
    * @param in  The stream that can be used to get the source data. The caller 
    *    of this method is responsible to close this stream, but not the method 
    *    itself.
    * 
    * @return an array of extracted items, may be empty, never <code>null
    *    </code>.
    *    
    * @throws java.io.IOException if error occurs during retrieving the 
    *    content.
    */
   public PSItemContext[] extractItems(PSItemContext resource, InputStream in)
      throws java.io.IOException;
   
   /**
    * Get the configuration of the object. see 
    * {@link IPSPlugin#configure(Element)}.
    * 
    * @return The config of the object, never <code>null</code>.
    */
   public Element getConfigure();
}

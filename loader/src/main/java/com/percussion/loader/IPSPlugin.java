/*[ IPSPlugin.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import org.w3c.dom.Element;

/**
 * A project known as Site Migration (see spec by Martin Genhart in spec db)
 * includes a client for scanning and analyzing a set of resources to convert
 * them to a set of Rx CMS items and then upload them to a Rhythmyx server.
 * The model for performing all of these operations has many 'hooks' that allow
 * different types of modules (commonly known as plugins) to be used. This 
 * interface is implemented by many of the modules used in this system.
 * <p>This interface provides methods to allow the plugin to be configured.
 * The class that implement this interface must have the default constructor,
 * which is a constructor without parameters.
 */
public interface IPSPlugin
{
   /**
    * Each plugin optionally has a set of properties that are used to change
    * its behavior under different conditions. Each plugin must specify
    * the schema (or dtd) that describes the format of the allowed 
    * configuration. This configuration is loaded by the manager from a known
    * location and passed to the plugin during initialization. This method is
    * always called whether any configuration information is present or not.
    * 
    * @param config the configuration xml for this plugin. If no configuration
    *    was supplied, <code>null</code> is passed in.
    * 
    * @throws PSConfigurationException if the supplied configuration is not
    *    valid for this plugin.
    */
   public void configure(Element config) 
      throws PSConfigurationException;
}

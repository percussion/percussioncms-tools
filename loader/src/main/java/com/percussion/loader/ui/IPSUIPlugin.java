/******************************************************************************
 *
 * [ IPSUIPlugin.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.ui;


/**
 * A project known as Site Migration (see spec by Martin Genhart in spec db)
 * includes a client for scanning and analyzing a set of resources to convert
 * them to a set of Rx CMS items and then upload them to a Rhythmyx server.
 * The model for performing all of these operations has many 'hooks' that allow
 * different types of modules (commonly known as plugins) to be used. This
 * interface is implemented by many of the modules used in this system.
 * <p>This interface provides methods to allow the plugin to edit the
 * configuration appropriate to the plugin.
 */
public interface IPSUIPlugin
{
   /**
    * Returns a panel that will be used in a GUI to allow a user to set the
    * properties for this object. The manager will use the returned panel in a
    * tabbed interface. The manager will persist this data and pass it to the
    * <code>configure</code> method each time it is initialized.
    *
    * @return A panel that contains controls for editing the properties of
    *    this plugin, or <code>null</code> if this plugin has no user editable
    *    properties.
    */
   public PSConfigPanel getConfigurationUI();
}

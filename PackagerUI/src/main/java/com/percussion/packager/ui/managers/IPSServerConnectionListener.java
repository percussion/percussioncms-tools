/******************************************************************************
 *
 * [ IPSServerConnectionListener.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.managers;

/**
 * Connection listener that listens for sserver manager connect and
 * disconnect events.
 * @author erikserating
 *
 */
public interface IPSServerConnectionListener
{
   /**
    * Indicates that a server connection just occurred.
    * @param hostport the host:port string for the connected
    * server.
    */
   public void connected(String hostport);
   
   /**
    * Indicates that a server disconnection just occurred.
    * @param hostport the host:port string for the previously 
    * connected server.
    */
   public void disconnected(String hostport);
}

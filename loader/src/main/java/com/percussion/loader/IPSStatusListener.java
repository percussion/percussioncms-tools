/*[ IPSStatusListener.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.util.EventListener;

/**
 * The listener interface for receiving a status event from a manager or job 
 * object.
 */
public interface IPSStatusListener extends EventListener
{
   /**
    * Invoked after a status event is fired. The status event may be fired by a
    * manager or job object. The manager may be the object that is registered 
    * the listener from. A job object is one of the <code>IPSPlugin</code> 
    * objects, which may be created by the manager object.
    *
    * @param e The fired status event, which contains the changed status of
    *    the source
    */
   void statusChanged(PSStatusEvent e);
}

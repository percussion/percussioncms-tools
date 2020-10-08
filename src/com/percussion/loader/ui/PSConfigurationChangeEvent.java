/*[ PSConfigurationChangeEvent.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import java.util.EventObject;

/**
 * The event object passed aroud to configuration change listeners.
 * @todo implement this
 */
public class PSConfigurationChangeEvent extends EventObject
{
   /**
    * Constructs a new content loader configuration change event.
    * 
    * @param source the source objects causeing this event, never 
    *    <code>null</code>.
    */
   public PSConfigurationChangeEvent(Object source)
   {
      super(source);
   }
}

/******************************************************************************
*
* [ PSTableEvent.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;


/**
 * Event that indicates a table event occurred
 */
public class PSTableEvent extends TypedEvent
{    

   /**
    * ctor
    * @param event
    */
   public PSTableEvent(Event event)
   {
      super(event);      
   }
   
   private static final long serialVersionUID = 8348220538034367940L;

}

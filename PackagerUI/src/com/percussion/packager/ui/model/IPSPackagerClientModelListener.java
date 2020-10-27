/******************************************************************************
 *
 * [ IPSPackagerClientModelListener.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.model;

import com.percussion.packager.ui.model.PSPackagerClientModel.ChangeEventTypes;

/**
 * Listener for packager client model events.
 * @author erikserating
 *
 */
public interface IPSPackagerClientModelListener
{
   /**
    * Indicates that something in the packager client model changed.
    * @param type the type of change the occurred. One of the
    * <code>PSPackagerClientModel.ChangeEventTypes</code> enums.
    * @param extra additional object of info needed for event. May
    * be <code>null</code>.
    */ 
   public void modelChanged(ChangeEventTypes type, Object extra);
}

/******************************************************************************
 *
 * [ IPSAAClientAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions;

import java.util.Map;

/**
 * An aa client action does some action on the Rhythmyx server or
 * retrieves information for the the AA client to use. Actions are expected
 * to be in the com.percussion.content.ui.aa.actions.impl package and must have 
 * the naming convention of PSXXXAction. This naming convention is required
 * as reflection is used to instantiate the action instance. The client
 * must send the descriptive name between the PS and Action so that the
 * action factory can find and instantiate the action.
 */
public interface IPSAAClientAction
{
   
   /**
    * This is where the work of the action is executed.
    * @param params a map of parameters that the action will need
    * to do its job. Usually is just some of the parameters from the
    * clients servlet request. May be <code>null</code> or empty if
    * parameters are not needed.
    * @return an action response object that contains the response data
    * and the response return type. Never <code>null</code>.
    * @throws PSAAClientActionException 
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException; 
   
   
   
   /**
    * Success string constant
    */
   public String SUCCESS = "success";
   
   /**
    * Object id parameter constant
    */
   public String OBJECT_ID_PARAM = "objectId"; 
   
   
   
}

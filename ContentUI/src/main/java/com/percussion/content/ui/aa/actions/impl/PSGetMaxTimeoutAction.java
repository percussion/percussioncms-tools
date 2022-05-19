/******************************************************************************
 *
 * [ PSGetMaxTimeoutAction.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.PSAAClientServlet;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;

import java.util.Map;

/**
 * Retrieves the server's max timeout setting in seconds.
 * Takes no params. Used for keep alive.
 */
public class PSGetMaxTimeoutAction extends PSAAActionBase
{

   // see interface for details
   @SuppressWarnings("unused")
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      Integer timeout = (Integer)params.get(PSAAClientServlet.PARAM_TIMEOUT);
      return new PSActionResponse(String.valueOf(timeout.intValue()),
               PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

}

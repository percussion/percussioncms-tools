/******************************************************************************
 *
 * [ PSTestAction.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Test action used for a client to test a request
 * against the servlet. Also used for a keep alive request.
 *
 */
public class PSTestAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      String mode = (String)getParameter(params, "mode");
      if(StringUtils.isBlank(mode))
      {
         return new PSActionResponse("success",
                  PSActionResponse.RESPONSE_TYPE_PLAIN);
      }
      else if(mode.equals("echoparams"))
      {
         StringBuilder sb = new StringBuilder();
         for(String key : params.keySet())
         {
            sb.append(key);
            sb.append(" = ");
            sb.append((String)getParameter(params, key));
            sb.append("\n");
         }
         return new PSActionResponse(sb.toString(),
                  PSActionResponse.RESPONSE_TYPE_PLAIN);
         
      }
      else if(mode.equals("error"))
      {
         throw new PSAAClientActionException("Simulated exception thrown.");
      }      
      return new PSActionResponse("fail",
               PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

}

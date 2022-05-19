/******************************************************************************
 *
 * [ PSGetServerPropertiesAction.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.server.PSServer;

import java.util.Map;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Retrieve the server properties for Rhythmyx.
 * Returns a JSON object with a property for each corresponding
 * server property.
 */
public class PSGetServerPropertiesAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(@SuppressWarnings("unused") Map<String, Object> params)
            throws PSAAClientActionException
   {
      Properties props = PSServer.getServerProps();
      JSONObject result = new JSONObject();      
      try
      {
         for(Object key : props.keySet())
         {
            String value = (String)props.get(key);
            result.put((String)key, value);
         }
      }
      catch (JSONException e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result.toString(), PSActionResponse.RESPONSE_TYPE_JSON);
   }

}

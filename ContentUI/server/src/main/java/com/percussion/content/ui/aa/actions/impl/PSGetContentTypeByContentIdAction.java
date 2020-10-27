/******************************************************************************
 *
 * [ PSGetContentTypeByContentId.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.util.IPSHtmlParameters;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Returns the content type of the content item id passed in.
 * Expects one parameter sys_contentid which is the content id of
 * the content item in question.
 */
public class PSGetContentTypeByContentIdAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      Object contentId = getParameter(params, IPSHtmlParameters.SYS_CONTENTID);
      JSONObject result = new JSONObject();      
      try
      {
         PSComponentSummary summary = 
            PSAAObjectId.getItemSummary(Integer.parseInt(contentId.toString()));
         result.put(IPSHtmlParameters.SYS_CONTENTTYPEID,
            summary.getContentTypeGUID().getUUID());
      }
      catch (JSONException e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result.toString(), PSActionResponse.RESPONSE_TYPE_JSON);
   }

}

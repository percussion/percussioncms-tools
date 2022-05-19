/******************************************************************************
 *
 * [ PSGetIdByPathAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Returns a JSONObject consisting of id and type for the given path. If the
 * object corresponds to the path is folder then returns type as "folder", if it
 * corresponds to item returns "item".
 * 
 * <pre>
 *   id: &lt;content id/folderid&gt;
 *   type:&lt;item:folder&gt;
 * </pre>
 * 
 */
public class PSGetIdByPathAction extends PSAAActionBase
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      Object path = getParameter(params, "path");
      if (path == null || StringUtils.isBlank(path.toString()))
      {
         throw new PSAAClientActionException("path must not be null or empty.");
      }
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      IPSGuid id = null;
      JSONObject obj = new JSONObject();
      try
      {
         id = cws.getIdByPath(path.toString());
         if (id == null)
         {
            throw new PSAAClientActionException(
                  "No item/folder exists with the supplied path: '"
                        + path.toString() + "'");
         }
         IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary summary = objMgr.loadComponentSummary(id.getUUID());
         obj.append("id", id.getUUID());
         String type = summary.isFolder() ? "folder" : "item";
         obj.append("type", type);
      }
      catch (PSErrorException e)
      {
         throw new PSAAClientActionException(e);
      }
      catch (JSONException e)
      {
         // ignore
      }
      return new PSActionResponse(obj.toString(),
            PSActionResponse.RESPONSE_TYPE_JSON);
   }
}

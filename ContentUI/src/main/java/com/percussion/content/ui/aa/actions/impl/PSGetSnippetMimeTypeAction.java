/******************************************************************************
 *
 * [ PSGetSnippetMimeTypeAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.utils.types.PSPair;

import java.util.Map;

import org.json.JSONObject;

/**
 * Retrieves the mime type of the assembled snippet. Expects an
 * objectid for the snippet.
 */
public class PSGetSnippetMimeTypeAction extends PSAAActionBase
{

   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String result = null;
      try
      {
         Map<String, String[]> assemblyParams = PSActionUtil.getAssemblyParams(
                  objectId, getCurrentUser());
         
         PSPair<IPSAssemblyItem, IPSAssemblyResult> pair = PSActionUtil
                  .assemble(assemblyParams);
         JSONObject obj = new JSONObject();
         obj.append("mimetype", pair.getSecond().getMimeType());
        
         result = obj.toString();
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result, PSActionResponse.RESPONSE_TYPE_JSON);
   }

}

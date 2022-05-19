/******************************************************************************
 *
 * [ PSGetSlotContentAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
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
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.types.PSPair;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Retrieves the assembled html content for the specified
 * slot. Expects an objectid for the snippet.
 */
public class PSGetSlotContentAction extends PSAAActionBase
{

   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String isAAMode = (String)getParameter(params, "isaamode");
      String sys_aamode = (String)getParameter(params,
               IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE);
      String result = null;
      try
      {
         IPSTemplateSlot slot = PSActionUtil.loadSlot(objectId.getSlotId());
         Map<String, String[]> assemblyParams = 
            PSActionUtil.getAssemblyParams(objectId, getCurrentUser());
         PSActionUtil.addAssemblyParam(assemblyParams,
            IPSHtmlParameters.SYS_PART, "slot:" + slot.getName());
         if(StringUtils.isNotBlank(isAAMode))
         {
            if(isAAMode.equalsIgnoreCase("true"))
            {
               PSActionUtil.addAssemblyParam(assemblyParams,
                  IPSHtmlParameters.SYS_COMMAND, 
                  IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY);
               if(StringUtils.isNotBlank(sys_aamode))
               {
                  PSActionUtil.addAssemblyParam(assemblyParams,
                     IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE, 
                     sys_aamode);
               }
            }
         }
         PSPair<IPSAssemblyItem, IPSAssemblyResult> pair = 
            PSActionUtil.assemble(assemblyParams);
         result = new String(pair.getSecond().getResultData(), "UTF8");
         
         int begin = result.indexOf("<div class=\"PsAaSlot\"");
         int end = result.lastIndexOf("</div>");
         
         result =  result.substring(begin, end);
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result,
               PSActionResponse.RESPONSE_TYPE_HTML);
   }

}

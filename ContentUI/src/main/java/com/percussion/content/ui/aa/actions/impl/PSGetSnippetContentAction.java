/******************************************************************************
 *
 * [ PSGetSnippetContentAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.PSInlineLinkField;
import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.types.PSPair;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Retrieves the assembled html content for the specified snippet. Expects an
 * objectid for the snippet.
 */
public class PSGetSnippetContentAction extends PSAAActionBase
{

   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String isAAMode = (String) getParameter(params, "isaamode");
      String sys_aamode = (String) getParameter(params,
               IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE);
      String selectedtext = (String) getParameter(params,
               PSInlineLinkField.RX_SELECTEDTEXT);
      String result = null;
      try
      {
         Map<String, String[]> assemblyParams = PSActionUtil.getAssemblyParams(
                  objectId, getCurrentUser());
         if (StringUtils.isNotBlank(isAAMode))
         {
            if (isAAMode.equalsIgnoreCase("true"))
            {
               PSActionUtil.addAssemblyParam(assemblyParams,
                        IPSHtmlParameters.SYS_COMMAND,
                        IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY);
               if (StringUtils.isNotBlank(sys_aamode))
               {
                  PSActionUtil.addAssemblyParam(assemblyParams,
                           IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE,
                           sys_aamode);
               }
            }
         }
         if (StringUtils.isNotBlank(selectedtext))
         {
            PSActionUtil.addAssemblyParam(assemblyParams,
               PSSingleValueBuilder.INLINE_TEXT, selectedtext);
         }
         PSPair<IPSAssemblyItem, IPSAssemblyResult> pair = PSActionUtil
                  .assemble(assemblyParams);
         result = PSActionUtil.getBodyContent(pair.getSecond());
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result, PSActionResponse.RESPONSE_TYPE_HTML);
   }

}

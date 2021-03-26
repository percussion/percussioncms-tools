/******************************************************************************
 *
 * [ PSGetSnippetPickerSlotContentAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.services.assembly.impl.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.jexl.PSAssemblerUtils;
import com.percussion.services.assembly.jexl.PSDocumentUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Gets the slot content that is used for snippet picker dialog. Builds the slot
 * content by getting the slot items and wrapping them with div tags. This
 * action can be used for getting the slot content as snippets or just titles.
 * If parameter with name "isTitles" exists with a value of true then returns
 * titles.
 */
public class PSGetSnippetPickerSlotContentAction extends PSAAActionBase
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String showTitlesParam = (String) getParameter(params, "isTitles");
      boolean showTitles = true;
      if (StringUtils.isBlank(showTitlesParam)
            || !showTitlesParam.equalsIgnoreCase("true"))
      {
         showTitles = false;
      }
      String output = null;
      try
      {
         Map<String, String[]> assemblyParams = PSActionUtil.getAssemblyParams(
               objectId, getCurrentUser());
         PSPair<IPSAssemblyItem, IPSAssemblyResult> pair = PSActionUtil
               .assemble(assemblyParams);
         IPSTemplateSlot slot = PSActionUtil.loadSlot(objectId.getSlotId());
         PSAssemblerUtils autils = new PSAssemblerUtils();
         List<IPSAssemblyResult> results = autils.assemble(pair.getFirst(),
               slot, null);
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < results.size(); i++)
         {
            IPSAssemblyResult result = results.get(i);
            String cssClass = "PSAASnippetPickerItem";
            if (showTitles)
            {
               cssClass = "PSAASnippetPickerTitle";
            }
            String begin = "<div class=\""
                  + cssClass
                  + "\" rid=\""
                  + result.getParameterValue(
                        IPSHtmlParameters.SYS_RELATIONSHIPID, "") + "\">";
            String end = "</div>";
            sb.append(begin);
            if (showTitles)
            {
               sb.append(result.getNode().getProperty("sys_title").getString());
            }
            else
            {
               sb.append(new PSDocumentUtils().extractBody(result));
            }
            sb.append(end);
         }
         output = sb.toString();
      }
      catch (Throwable e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(output, PSActionResponse.RESPONSE_TYPE_HTML);
   }

}

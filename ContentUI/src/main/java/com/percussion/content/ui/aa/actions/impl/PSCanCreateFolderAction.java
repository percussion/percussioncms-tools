/******************************************************************************
 *
 * [ PSCanCreateFolderAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.content.ui.browse.PSContentBrowser;
import com.percussion.util.IPSHtmlParameters;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Implementation of the "canCreateFolder" action.
 */
public class PSCanCreateFolderAction extends PSAAActionBase
{
   /**
    * @param params the parameter expected in the map is a valid
    * {@link IPSHtmlParameters#SYS_FOLDERID parentFolderId}
    * @return String equivalent of boolean value <code>true</code> if user can
    * create a folder in the folder with given folderid, <code>false</code>
    * otherwise.
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      Object obj = getParameter(params, IPSHtmlParameters.SYS_FOLDERID);
      if (obj == null || StringUtils.isBlank(obj.toString()))
         throw new IllegalArgumentException("folderid must not be ampty");
      String folderid = obj.toString();
      boolean canCreate = false;
      try
      {
         canCreate = PSContentBrowser.canCreateFolder(getRequestContext(),
            folderid);
      }
      catch (PSCmsException e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(Boolean.toString(canCreate),
         PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

}

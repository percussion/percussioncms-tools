/******************************************************************************
 *
 * [ PSCanCreateItemAction.java ]
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
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Implementation of the "canCreateItem" action.
 */
public class PSCanCreateItemAction extends PSAAActionBase
{
   /**
    * @param params parameters expected in the map are a valid
    * {@link com.percussion.util.IPSHtmlParameters#SYS_FOLDERID parentFolderId}
    * and a valid
    * {@link com.percussion.util.IPSHtmlParameters#SYS_CONTENTTYPEID contentTypeId}.
    * @return String equivalent of boolean value <code>true</code> if user can
    * create an item of the given content typeid in the folder with given
    * folderid, <code>false</code> otherwise.
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      Object obj = getParameter(params, IPSHtmlParameters.SYS_FOLDERID);
      if (obj == null || StringUtils.isBlank(obj.toString()))
         throw new IllegalArgumentException("folderid must not be ampty");
      String folderid = obj.toString();

      obj = getParameter(params, IPSHtmlParameters.SYS_CONTENTTYPEID);
      if (obj == null || StringUtils.isBlank(obj.toString()))
         throw new IllegalArgumentException("ctypeid must not be ampty");
      String ctypeid = obj.toString();
      boolean canCreate = false;
      try
      {
         canCreate = PSContentBrowser.canCreateItem(getRequestContext(),
            folderid, ctypeid);
      }
      catch (PSCmsException e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(Boolean.toString(canCreate),
         PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

}

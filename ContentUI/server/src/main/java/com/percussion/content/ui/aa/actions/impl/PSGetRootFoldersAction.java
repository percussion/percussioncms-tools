/******************************************************************************
 *
 * [ PSGetRootFoldersAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.content.ui.browse.PSContentBrowser;

import java.util.Map;

/**
 * Implementation of the get root folders action.
 */
public class PSGetRootFoldersAction extends PSAAActionBase
{
   /**
    * todo document the required and optional parameters in the map.
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      try
      {
         return new PSActionResponse(PSContentBrowser
            .getRootFolders(getRequestContext()),
            PSActionResponse.RESPONSE_TYPE_JSON);
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
   }
}

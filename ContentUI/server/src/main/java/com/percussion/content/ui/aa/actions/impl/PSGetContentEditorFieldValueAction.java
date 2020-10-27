/******************************************************************************
 *
 * [ PSGetContentEditorFieldValueAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.services.assembly.impl.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Action to get the content editor field value. Loads the item using
 * webservices and gets the value of the filed with the name mentioned in object
 * id.
 * 
 */
public class PSGetContentEditorFieldValueAction extends PSAAActionBase
{
   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid cid = guidMgr.makeGuid(new PSLocator(objectId.getContentId()));

      IPSContentWs ctService = PSContentWsLocator.getContentWebservice();

      List<IPSGuid> ids = Collections.singletonList(cid);
      List<PSCoreItem> items;
      IPSFieldValue value = null;
      String fieldValue = "";
      try
      {
         items = ctService.loadItems(ids, false, false, false, false);
         PSItemField field = items.get(0).getFieldByName(
               objectId.getFieldName());
         if (field != null)
         {
            value = field.getValue();
            if (value != null)
            {
               fieldValue = value.getValueAsString();
            }
         }
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }

      return new PSActionResponse(fieldValue,
            PSActionResponse.RESPONSE_TYPE_HTML);
   }

}

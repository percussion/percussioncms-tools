/******************************************************************************
 *
 * [ PSGetItemSortRankAction.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Returns the sort rank of the items relationship.
 * Expects sys_relationshipid parameter.
 * Returns the sort rank result as plain text.
 */
public class PSGetItemSortRankAction extends PSAAActionBase
{

   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      String rid = (String)getParameter(params,
               IPSHtmlParameters.SYS_RELATIONSHIPID);
      if(StringUtils.isBlank(rid))
         throw new PSAAClientActionException("sys_relationshipid is a required parameter.");
      
      try
      {
          PSRelationshipFilter filter = new PSRelationshipFilter();
          filter.setRelationshipId(Integer.parseInt(rid));
          PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
          PSRelationshipSet relationships = processor.getRelationships(filter);
          PSRelationship relationship = null;
          if (!relationships.isEmpty())
              relationship = (PSRelationship) relationships.get(0);

          PSAaRelationship r =
            new PSAaRelationship(relationship);
         
         return new PSActionResponse(String.valueOf(r.getSortRank()),
                  PSActionResponse.RESPONSE_TYPE_PLAIN);
      }      
      catch (PSCmsException e)
      {
         throw new PSAAClientActionException(e);
      }
      
   }

}

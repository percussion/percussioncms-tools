/******************************************************************************
 *
 * [ PSRemoveSnippetAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This action is used to remove snippet/s. It removes active assembly
 * relationships corresponding to the comma separated list of relationship ids
 * specified in the (required) <code>relationshipIds</code> parameter.
 * 
 * <p>
 * Expects the following parameters:
 * </p>
 * <table border="1" cellspacing="0" cellpadding="5"> <thead>
 * <th>Name</th>
 * <th>Allowed Values</th>
 * <th>Details</th>
 * </thead> <tbody>
 * <tr>
 * <td>relationshipIds</td>
 * <td>comma separated list of relationship ids</td>
 * <td>Required</td>
 * </tr>
 * </tbody> </table>
 */
public class PSRemoveSnippetAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      String ridParam = (String) getParameterRqd(params, RELATIONSHIP_IDS);
      String[] rids = ridParam.split(",");
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      for (String rid : rids)
      {
         ids.add(mgr.makeGuid(rid,PSTypeEnum.RELATIONSHIP));
      }
      IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
      try
      {
         service.deleteRelationships(ids);
      }
      catch (PSErrorsException es)
      {
         throw createException(es);
      }
      catch (PSErrorException e)
      {
         throw createException(e);
      }
      
      return new PSActionResponse(SUCCESS, PSActionResponse.RESPONSE_TYPE_PLAIN);
   }
   
   /**
    * The name of the parameter to specify the to be removed relationship ids.
    */
   public static String RELATIONSHIP_IDS = "relationshipIds";
}

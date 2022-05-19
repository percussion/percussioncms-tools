/******************************************************************************
 *
 * [ PSMoveAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cas.PSModifyRelatedContent;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * An action that will move a slot item up or down by one position or move
 * the item to a specified position.
 * <p>
 * Expects the following parameters:
 * </p>
 * <table border="1" cellspacing="0" cellpadding="5">
 * <thead>  
 * <th>Name</th><th>Allowed Values</th><th>Details</th> 
 * </thead>
 * <tbody>
 * <tr>
 * <td>objectId</td><td>The object id string</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>mode</td><td>up, down, reorder</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>index</td><td>The desired position to move to</td>
 * <td>Only required if in reorder mode</td>
 * </tr>
 * </tbody>
 * </table>
 */
public class PSMoveAction extends PSAAActionBase
{

   // see base class for details
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String mode = (String)getParameter(params, "mode");
      String index = (String)getParameter(params, "index");
      
      int idx = -1;      
      
      // Validate mode
      if(StringUtils.isBlank(mode))
         throw new PSAAClientActionException(
                  "Missing required mode parameter.");
      if(!(mode.equals("up") || mode.equals("down") || mode.equals("reorder")))
         throw new PSAAClientActionException(
                  "Invalid mode! Must be 'up', 'down' or 'reorder'.");
      
      // Validate index
      if(mode.equals("reorder"))
      {
         if(StringUtils.isBlank(index))
            throw new PSAAClientActionException(
                     "index parameter required when using reorder mode.");
         try
         {
            idx = Integer.parseInt(index);   
         }
         catch(NumberFormatException nfe)
         {
            throw new PSAAClientActionException(
                     "Invalid format! index must be an integer.");
         }
      }
      IPSRequestContext request = getRequestContext();
      try
      {
         if(mode.equals("up"))
         {
            PSModifyRelatedContent.moveUp(
                     Integer.parseInt(objectId.getRelationshipId()), request);
         }
         else if(mode.equals("down"))
         {
            PSModifyRelatedContent.moveDown(
                     Integer.parseInt(objectId.getRelationshipId()), request);
         }
         else if(mode.equals("reorder"))
         {
            PSModifyRelatedContent.reorder(
                     Integer.parseInt(objectId.getRelationshipId()), idx, request);
         }
      }
      catch(PSException e)
      {
         throw new PSAAClientActionException(e);
      }      
      return new PSActionResponse(SUCCESS, PSActionResponse.RESPONSE_TYPE_PLAIN);
   }   

}

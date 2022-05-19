/******************************************************************************
 *
 * [ PSGetActionLabelsAction.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

/**
 * Given a set of action names (case-insensitive), it looks them up and returns
 * the corresponding label.
 *
 * @author paulhoward
 */
public class PSGetActionLabelsAction extends PSAAActionBase
{
   /**
    * For each supplied name, search for a matching <code>PSAction</code> that
    * has that name (case-insensitive.) If found, add the name and label to the
    * result.
    * 
    * @param params An entry called 'names' whose value is a String[] containing
    * the actions of interest.
    * 
    * @return The value is a <code>Map</code> whose key is the proper-cased
    * name and whose value is the label that has been converted to a JSON
    * string.
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      try
      {
         String[] names = (String[]) params.get("names");
         
         IPSUiDesignWs uiMgr = PSUiWsLocator.getUiDesignWebservice();
         List<IPSCatalogSummary> actionSums = 
            uiMgr.findActions(null, null, null);
         Map<String, IPSCatalogSummary> namesToSums = 
            new HashMap<String, IPSCatalogSummary>();
         for (IPSCatalogSummary sum : actionSums)
            namesToSums.put(sum.getName().toLowerCase(), sum);
         
         Map<String, String> namesToLabels = new HashMap<String, String>();
         for (String name : names)
         {
            IPSCatalogSummary sum = namesToSums.get(name.toLowerCase());
            if (sum == null)
               continue;
            namesToLabels.put(name, sum.getLabel());
         }
         JSONArray result = new JSONArray();
         result.put(namesToLabels);
         return new PSActionResponse(result.toString(),
               PSActionResponse.RESPONSE_TYPE_JSON);
      }
      catch (PSErrorException e)
      {
         throw new PSAAClientActionException(e);
      }
   }
}

/******************************************************************************
 *
 * [ PSGetLocaleCountAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.PSAAClientServlet;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.util.List;
import java.util.Map;

/**
 * Catalogs the locales on the system and returns the number of them.
 */
public class PSGetLocaleCountAction extends PSAAActionBase
{
   // see interface for details
   @SuppressWarnings("unused") //exception
   public PSActionResponse execute(
         @SuppressWarnings("unused") Map<String, Object> params)
         throws PSAAClientActionException
   {
      IPSContentDesignWs cd = PSContentWsLocator.getContentDesignWebservice();
      List locales = cd.findLocales(null, null);
      return new PSActionResponse(String.valueOf(locales.size()),
               PSActionResponse.RESPONSE_TYPE_PLAIN);
   }
}

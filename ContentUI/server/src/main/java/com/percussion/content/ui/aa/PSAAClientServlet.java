/******************************************************************************
 *
 * [ PSAAClientServlet.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa;

import com.percussion.content.ui.aa.actions.IPSAAClientAction;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSAAClientActionFactory;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet that calls a AA client action to provide a specific response
 * for a request made by the Active Assembly client objects.
 */
public class PSAAClientServlet extends HttpServlet
{
   @SuppressWarnings("unchecked")
   @Override
   public void service(HttpServletRequest request, HttpServletResponse response)
      throws IOException
   {
      // Discard if the connection has closed 
      if (response.isCommitted())
         return;
      String actionType = request.getParameter(PARAM_ACTION);
      int sessiontimeout = request.getSession().getMaxInactiveInterval();
      IPSAAClientAction action = null;
      if(StringUtils.isNotEmpty(actionType))
      {        
         action = 
            PSAAClientActionFactory.getInstance().getAction(actionType);         
      }
      if (action != null)
      {
         try
         {
            Map params = new HashMap(request.getParameterMap());
            params.put(PARAM_TIMEOUT, sessiontimeout);
            PSActionResponse aResponse = action.execute(
                  MapUtils.unmodifiableMap(params));
            pushResponse(response, aResponse.getResponseData(),
                  aResponse.getResponseTypeString(), 200);
         }
         catch (PSAAClientActionException e)
         {
            String resp = e.getLocalizedMessage();
            pushResponse(response, resp, "text/plain", 500);
         }
         return;
      }
      // No action specified. Cannot handle the request
      String resp = "Servlet is not meant to handle the request";
      pushResponse(response, resp, "text/plain", 404);
   }

   /**
    * Helper method to help with passing back the response.
    * @param httpResponse the response object, assumed not <code>null</code>.
    * @param resp the response data string, 
    * @param ctype the response content type, assumed not <code>null</code>.
    * @param respCode the response code.
    * @throws IOException
    */
   private void pushResponse(HttpServletResponse httpResponse,
      String resp, String ctype, int respCode) throws IOException
   {
      /* Discard if the connection has closed */
      if (httpResponse.isCommitted())
         return;
     
      httpResponse.setContentType(ctype);
      byte[] respBytes = resp.getBytes("UTF-8");
      httpResponse.setContentLength(respBytes.length);
      httpResponse.setStatus(respCode);
      if(respCode == 500)
         httpResponse.sendError(respCode, resp);
      OutputStream os = httpResponse.getOutputStream();
      os.write(respBytes);
      os.flush();      
   }   
   
   /**
    * Constant for Action parameter.
    */
   public static final String PARAM_ACTION = "action";
   
   /**
    * Constant for timeout parameter
    */
   public static final String PARAM_TIMEOUT = "__timeout";
  
}

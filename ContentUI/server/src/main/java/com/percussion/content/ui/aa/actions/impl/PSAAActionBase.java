/******************************************************************************
 *
 * [ PSAAActionBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.assembly.impl.PSAAObjectId;
import com.percussion.content.ui.aa.actions.IPSAAClientAction;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The base class for AA Client Actions. Contains some common helper methods
 * that most actions will need.
 */
public abstract class PSAAActionBase implements IPSAAClientAction
{

   /**
    * Creates a new <code>IPSRequestContext</code> from the
    * request stored in thread local.
    * @return the request context, never <code>null</code>.
    */
   protected IPSRequestContext getRequestContext()
   {
      PSRequest req = (PSRequest) PSRequestInfo
      .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      return new PSRequestContext(req);
   }

   /**
    * Helper method to retrieve and parse the objectid array string
    * from the passed in parameter map.
    * @param params parameter map passed into the actions execute method.
    * @return the object id object, never <code>null</code>.
    * @throws PSAAClientActionException if no object id string is found or
    * a parsing error occurs.
    */
   protected PSAAObjectId getObjectId(Map<String, Object> params)
      throws PSAAClientActionException
   {
      String objectId = (String)getParameter(params, OBJECT_ID_PARAM);
      if(StringUtils.isNotBlank(objectId))
      {
         try
         {
            return new PSAAObjectId(objectId);
         }
         catch(JSONException e)
         {
            throw new PSAAClientActionException(
                     "Error parsing JSON array string.", e);
         }
      }
      else
      {
         throw new PSAAClientActionException(
                  "Required objectid does not exist.");
      }
   }

   /**
    * Helper method to retrieve and validate an integer type
    * parameter from passed in parameter map.
    * @param params cannot be <code>null</code>.
    * @param paramname the name of the parameter. Cannot be
    * <code>null</code> or empty.
    * @param required flag indicating that this object is required.
    * @return the integeror -1 if not required and no value was found.
    * @throws PSAAClientActionException if no value and required or if
    * an invalid integer.
    */
   protected int getValidatedInt(Map<String, Object> params,
      String paramname, boolean required)
      throws PSAAClientActionException
   {
      if(params == null)
         throw new IllegalArgumentException("params cannot be null.");
      if(StringUtils.isBlank(paramname))
         throw new IllegalArgumentException("paramname cannot be null or empty.");
      String param = (String)getParameter(params, paramname);
      if(StringUtils.isNotBlank(param))
      {
         try
         {
            return Integer.parseInt(param);
         }
         catch(NumberFormatException e)
         {
            throw new PSAAClientActionException(
                     param + " is not a valid integer.");
         }
      }
      else if(required)
      {
         throw new PSAAClientActionException(
                  paramname + " is a required parameter.");
      }
      return -1;
   }

   /**
    * Helper method to help get the one string from a single value
    * string array returned from the request.
    * @param params the map of params, cannot be <code>null</code>.
    * @param key the param name key cannot be <code>null</code> or
    * empty.
    * @return A single string if the param type is a string array with
    * a single value else just return the object.
    */
   protected Object getParameter(Map<String, Object> params, String key)
   {
      if(params == null)
         throw new IllegalArgumentException("params cannot be null.");
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      Object param = params.get(key);
      if(param instanceof String[])
      {
         String[] array = (String[])param;
         if(array.length == 1)
            return array[0];
      }
      return param;

   }

   /**
    * This is the same as {@link #getParameter(Map, String)}
    * except it throws IllegalArgumentException if the specified parameter
    * does not exist.
    */
   protected Object getParameterRqd(Map<String, Object> params, String key)
   {
      Object value = getParameter(params, key);
      if (value == null)
         throw new IllegalArgumentException("Cannot find required parameter, '" + key + "'.");

      return value;
   }   

   /**
    * Creates an {@link IPSGuid} for a specified content id. 
    *
    * @param contentId the specified content id. It must not be less than 0.
    * 
    * @return the created {@link IPSGuid} object, with revision equals 
    *    <code>-1</code>.
    */
   protected IPSGuid getItemGuid(int contentId)
   {
      if (contentId < 0)
         throw new IllegalArgumentException("contentId must be >= 0");
      
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      return mgr.makeGuid(contentId, PSTypeEnum.LEGACY_CONTENT);
   }
   
   /**
    * Returns the latest AA viewable revision for the specified
    * content item.
    * @param objectId the object id in question, never not <code>null</code>.
    * @return the revision, never <code>null</code>.
    */
   protected String getCorrectRevision(PSAAObjectId objectId)
   {
      if (objectId == null)
         throw new IllegalArgumentException("objectId must not be null.");
      
      String currentuser = getCurrentUser();
      PSComponentSummary summary = objectId.getItemSummary(
               Integer.parseInt(objectId.getContentId()));
      return String.valueOf(summary.getAAViewableRevision(currentuser));
   }
   
   /**
    * Returns the current revision for the specified content item.
    * @param objectId the object id in question, never not <code>null</code>.
    * @return the curent revision, never <code>null</code>.
    */
   protected String getCurrentRevision(PSAAObjectId objectId)
   {
      if (objectId == null)
         throw new IllegalArgumentException("objectId must not be null.");
      
      PSComponentSummary summary = objectId.getItemSummary(
               Integer.parseInt(objectId.getContentId()));
      return String.valueOf(summary.getCurrentLocator().getRevision());
   }

   /**
    * Gets the current Rx user for this request.
    * @return never <code>null</code> or empty.
    */
   protected String getCurrentUser()
   {
      IPSRequestContext requestCtx = getRequestContext();
      return requestCtx.getUserName();
   }

   /**
    * Looks up the community of the user making the request.
    * 
    * @return A valid community UUID or -1 if the current user is not operating
    * in a community.
    */
   protected int getCurrentCommunityUuid()
   {
      int commId = -1;
      Object o = getRequestContext().getSessionPrivateObject("sys_community");
      if (o != null)
      {
         if (o instanceof Integer)
            commId = ((Integer) o).intValue();
         else
            commId = Integer.parseInt(o.toString());
      }
      return commId;
   }
   
   /**
    * Creates an exception from the specified ws exception and object id.
    * 
    * @param e the specified ws exception, assumed not <code>null</code>.
    * 
    * @return the created exception, never <code>null</code>.
    */
   protected PSAAClientActionException createException(PSErrorException e)
   {
      if (e == null)
         throw new IllegalArgumentException("e must not be null.");
      
      PSAAClientActionException ex = 
         new PSAAClientActionException(e.getLocalizedMessage());
      return ex;
   }

   /**
    * Creates an exception from the specified ws exception.
    * 
    * @param e the specified ws exception, assumed not <code>null</code>.
    * 
    * @return the created exception, never <code>null</code>.
    */
   protected PSAAClientActionException createException(PSErrorsException e)
   {
      if (e == null)
         throw new IllegalArgumentException("e must not be null.");
      
      Set<Entry<IPSGuid, Object>> errors = e.getErrors().entrySet();
      if (errors.size() == 0)
         throw new RuntimeException("Unexpected empty error.");
      
      Entry<IPSGuid, Object> errEntry = errors.iterator().next();
      Object error = errEntry.getValue();
      if (! (error instanceof PSErrorException))
         throw new RuntimeException("Unknown error: " + error.toString());
      
      return createException((PSErrorException)error);
   }
   
   /**
    * Calculate root url 
    * @return the root url, never <code>null</code> or empty
    * 
    */
   protected String getRoot()
   {
      IPSRequestContext requestCtx = getRequestContext();
      String rawUrl = requestCtx.getRequestFileURL();      
      return rawUrl.substring(0, rawUrl.indexOf("/", 1));
   }

   public static final String PARAM_NAME_PARENT_FOLDER_PATH = "parentFolderPath";

   public static final String PARAM_NAME_CATEGORY = "category";

   public static final String PARAM_CATEGORY_SITES = "sites";

   public static final String PARAM_CATEGORY_FOLDERS = "folders";
}

/******************************************************************************
 *
 * [ PSGetItemTemplatesForSlotAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.services.assembly.impl.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Retrieves a listing of all allowed templates for a particular
 * slot. 
 * Expects an objectId for the slot. Returns a Json Array
 * with a Json object for each template entry.
 * <pre>
 * Each Json object that represents a template has the following
 * parameters:
 * 
 * variantid
 * name
 * description
 * objectid
 * outputformat
 * </pre>
 *
 */
public class PSGetItemTemplatesForSlotAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String results = null;
      try
      {
         Collection<IPSAssemblyTemplate> temps = getAssociatedTemplates(objectId);
         JSONArray array = new JSONArray();
         for(IPSAssemblyTemplate template : temps)
         {
            
            JSONObject obj = new JSONObject();
            String vid = String.valueOf(
                     template.getGUID().getUUID());
            obj.append("variantid", vid);
            obj.append("name",
               String.valueOf(template.getLabel()));
            obj.append("description",
               String.valueOf(template.getDescription()));
            objectId.modifyParam(IPSHtmlParameters.SYS_VARIANTID, vid);
            obj.append("objectid", objectId.toString());
            obj.append("outputformat", template.getOutputFormat().toString());
            array.put(obj);
         }
         results = array.toString();
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(results,
         PSActionResponse.RESPONSE_TYPE_JSON);
   }
   
   /**
    * Retrieves the associated templates for the slot indicated in the
    * object id passed in.
    * @param objectId cannot be <code>null</code> or empty. Must have the
    * slot id defined.
    * @return collection of template object, never <code>null</code>,
    * may be empty.
    * @throws PSAssemblyException if an error occurs when retriving the 
    * slot or templates from the assembly service.
    */
   @SuppressWarnings("unchecked")
   protected static Collection<IPSAssemblyTemplate> getAssociatedTemplates(PSAAObjectId objectId)
      throws PSAssemblyException
   {
      if(objectId == null)
         throw new IllegalArgumentException("objectId cannot be null.");
      if(StringUtils.isBlank(objectId.getSlotId()))
         throw new IllegalArgumentException(
                  "The slotid in the objectId cannot be null or empty.");
      return getAssociatedTemplates(objectId.getItemSummary()
            .getContentTypeId()+"", objectId.getSlotId());
   
   }
   
   /**
    * Retrieves the associated templates for the slot indicated in the object id
    * passed in.
    * 
    * @param ctypeid cannot be <code>null</code> or empty. Must be a valid
    *           content type id.
    * @param slotid cannot be <code>null</code> or empty. Must be a valid
    *           slot id.
    * @return collection of template object, never <code>null</code>, may be
    *         empty.
    * @throws PSAssemblyException if an error occurs when retriving the slot or
    *            templates from the assembly service.
    */
   @SuppressWarnings("unchecked")
   protected static Collection<IPSAssemblyTemplate> getAssociatedTemplates(
         String ctypeid, String slotid) throws PSAssemblyException
   {
      if(StringUtils.isBlank(ctypeid))
         throw new IllegalArgumentException(
                  "The ctypeid cannot be null or empty.");
      if(StringUtils.isBlank(slotid))
         throw new IllegalArgumentException(
                  "The slotid cannot be null or empty.");

      List<IPSAssemblyTemplate> templates = new ArrayList<IPSAssemblyTemplate>();
      IPSAssemblyService aService = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSTemplateSlot slotObj = PSActionUtil.loadSlot(slotid);
      Collection<PSPair<IPSGuid, IPSGuid>> assoc = slotObj
            .getSlotAssociations();

      for (PSPair<IPSGuid, IPSGuid> pair : assoc)
      {
         if (pair.getFirst().equals(
               PSGuidManagerLocator.getGuidMgr().makeGuid(ctypeid,
                     PSTypeEnum.NODEDEF)))
            templates.add(aService.loadTemplate(pair.getSecond(), false));
      }
      Collections.sort(templates, new Comparator()
      {
         public int compare(Object obj1, Object obj2)
         {
            IPSAssemblyTemplate temp1 = (IPSAssemblyTemplate) obj1;
            IPSAssemblyTemplate temp2 = (IPSAssemblyTemplate) obj2;

            return temp1.getName().compareTo(temp2.getName());
         }
      });
      return templates;
   }   
   

}

/******************************************************************************
 *
 * [ PSGetTemplateImagesForContentTypeAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.services.assembly.impl.PSAANodeType;
import com.percussion.services.assembly.impl.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSTemplateImageUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action to get the template image urls. Expects sys_contenttypeid and objectId
 * parameters, throws exception if they are not part of supplied params. Gets
 * the templates for the supplied content type and then returns
 * 
 * JSONArray of JSONObjects with the template details. The JSONObject consists
 * of the following parameters.
 * 
 * <pre>
 *     templateId
 *     templateName
 *     thumbUrl
 *     fullUrl
 * </pre>
 * 
 * If the supplied objectId is of type item or slot then filters the templates
 * by slot.
 */
public class PSGetTemplateImagesForContentTypeAction extends PSAAActionBase
{

   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      Object obj = getParameter(params, IPSHtmlParameters.SYS_CONTENTTYPEID);
      if (obj == null || obj.toString().trim().length() == 0)
      {
         throw new PSAAClientActionException("Parameter '"
               + IPSHtmlParameters.SYS_CONTENTTYPEID
               + "' is required and cannot be empty for this action");
      }
      String cTypeId = obj.toString();
      PSAAObjectId objectId = getObjectId(params);
      String results = null;
      try
      {
         JSONArray array = new JSONArray();
         // Get the templates
         Collection<IPSAssemblyTemplate> templates = 
            new ArrayList<IPSAssemblyTemplate>();

         if (objectId.getNodeType() != null
               && (objectId.getNodeType().equals(
                     "" + PSAANodeType.AA_NODE_TYPE_SLOT.getOrdinal()) || objectId
                     .getNodeType().equals(
                           "" + PSAANodeType.AA_NODE_TYPE_SNIPPET.getOrdinal())))
         {
            templates = PSGetItemTemplatesForSlotAction.getAssociatedTemplates(
                  cTypeId, objectId.getSlotId());
         }
         else
         {
            IPSAssemblyService aService = PSAssemblyServiceLocator
                  .getAssemblyService();
            IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
            IPSGuid ctGuid = mgr.makeGuid(cTypeId, PSTypeEnum.NODEDEF);
            templates = aService.findTemplatesByContentType(ctGuid);
            templates = filterTemplatesBySite(templates,objectId.getSiteId());
            templates = getSortedTemplates(templates);
         }

         Map<String, String> fileNames = PSTemplateImageUtils
               .getImageFileNames();
         String siteid = objectId.getSiteId();
         IPSSiteManager sm = PSSiteManagerLocator.getSiteManager();
         IPSSite site = sm.loadUnmodifiableSite(PSGuidManagerLocator.getGuidMgr()
               .makeGuid(siteid, PSTypeEnum.SITE));
         String siteName = site.getName();
         for (IPSAssemblyTemplate template : templates)
         {
            JSONObject jobj = new JSONObject();
            jobj.append("templateId", template.getGUID().getUUID());
            jobj.append("templateName", template.getLabel());
            jobj.append("thumbUrl", PSTemplateImageUtils.getImageUrl(template
                  .getName(), siteName, true, fileNames));
            jobj.append("fullUrl", PSTemplateImageUtils.getImageUrl(template
                  .getName(), siteName, false, fileNames));
            array.put(jobj);
         }
         results = array.toString();
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(results, PSActionResponse.RESPONSE_TYPE_JSON);
   }

   /**
    * Filters the templates by supplied site.
    * 
    * @param templates If <code>null</code> returns <code>null</code>.
    * @param siteId if <code>null</code> templates are returned unfiltered.
    * @return Filtered templates by site. May be <code>null</code> or empty.
    * @throws PSMissingBeanConfigurationException
    * @throws PSSiteManagerException
    */
   private Collection<IPSAssemblyTemplate> filterTemplatesBySite(
         Collection<IPSAssemblyTemplate> templates, String siteId)
         throws PSSiteManagerException, PSMissingBeanConfigurationException
   {
      if (templates == null)
         return null;
      if (siteId == null)
         return templates;
      IPSSiteManager sm = PSSiteManagerLocator.getSiteManager();
      IPSSite site = sm.loadUnmodifiableSite(PSGuidManagerLocator.getGuidMgr()
            .makeGuid(siteId, PSTypeEnum.SITE));
      Set<IPSAssemblyTemplate> siteTempls = site.getAssociatedTemplates();
      templates.retainAll(siteTempls);
      return templates;
   }

   /**
    * Helper method to sort the supplied templates by outputformat and then by
    * name.
    * 
    * @param templates assumed not <code>null</code>.
    * @return sorted collection of templates by outputformat and by type, may be
    *         empty but never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Collection<IPSAssemblyTemplate> getSortedTemplates(
         Collection<IPSAssemblyTemplate> templates)
   {
      Map<OutputFormat, List<IPSAssemblyTemplate>> temp = 
         new HashMap<OutputFormat, List<IPSAssemblyTemplate>>();
      for (IPSAssemblyTemplate template : templates)
      {
         OutputFormat of = template.getOutputFormat();
         List<IPSAssemblyTemplate> tl = temp.get(of);
         if (tl == null)
         {
            tl = new ArrayList<IPSAssemblyTemplate>();
            temp.put(of, tl);
         }
         tl.add(template);
      }
      List<IPSAssemblyTemplate> sortedTempls = new ArrayList<IPSAssemblyTemplate>();
      List<OutputFormat> ofList = new ArrayList<OutputFormat>();
      ofList.add(OutputFormat.Global);
      ofList.add(OutputFormat.Page);
      ofList.add(OutputFormat.Snippet);
      ofList.add(OutputFormat.Binary);
      ofList.add(OutputFormat.Database);
      for (OutputFormat format : ofList)
      {
         List<IPSAssemblyTemplate> tlist = temp.get(format);
         if(tlist==null)
            continue;
         Collections.sort(tlist, new Comparator()
         {
            public int compare(Object obj1, Object obj2)
            {
               IPSAssemblyTemplate temp1 = (IPSAssemblyTemplate) obj1;
               IPSAssemblyTemplate temp2 = (IPSAssemblyTemplate) obj2;

               return temp1.getLabel().compareTo(temp2.getLabel());
            }
         });
         sortedTempls.addAll(tlist);
         
      }
      return sortedTempls;
   }
}

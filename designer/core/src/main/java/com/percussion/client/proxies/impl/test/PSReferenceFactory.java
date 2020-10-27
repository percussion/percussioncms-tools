/******************************************************************************
 *
 * [ PSReferenceFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.i18n.PSLocale;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.ui.data.PSHierarchyNode;

import java.util.Set;

public class PSReferenceFactory
{

   private PSReferenceFactory(){}
   
   public static PSReferenceFactory getInstance()
   {
      if(ms_instance == null)
      {
         ms_instance = new PSReferenceFactory();
      }
      return ms_instance;
   }
   
   /**
    * Returns the reference for the passed in object
    * @param obj the object to get a reference for
    * @param pType Used to choose the correct method to create the ref. Never
    * <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   public IPSReference getReference(Object obj, IPSPrimaryObjectType pType)
   {
      if (null == pType)
      {
         throw new IllegalArgumentException("pType cannot be null");  
      }
      PSObjectTypes type = (PSObjectTypes) pType;
      if(type == PSObjectTypes.USER_FILE)
         return getUserFileReference(obj, type);
      if(type == PSObjectTypes.AUTO_TRANSLATION_SET)
         return getAutoTransReference(obj, type);
      if(type == PSObjectTypes.COMMUNITY)
         return getCommunityReference(obj, type);
      if(type == PSObjectTypes.CONTENT_TYPE)
         return getContentTypeReference(obj, type);
      if(type == PSObjectTypes.KEYWORD)
         return getKeywordReference(obj, type);
      if(type == PSObjectTypes.LOCALE)
         return getLocaleReference(obj, type);
      if(type == PSObjectTypes.RELATIONSHIP_TYPE)
         return getRelTypeReference(obj, type);
      if(type == PSObjectTypes.SLOT)
         return getSlotReference(obj, type);
      if(type == PSObjectTypes.TEMPLATE)         
        return getTemplateReference(obj);
      if(type == PSObjectTypes.UI_ACTION_MENU)
         return getUiActionMenuReference(obj, type);
      if(type == PSObjectTypes.UI_DISPLAY_FORMAT)
         return getUiDisplayFormatReference(obj, type);
      if(type == PSObjectTypes.UI_SEARCH || type == PSObjectTypes.UI_VIEW)
         return getUiSearchReference(obj, type);
      if(type == PSObjectTypes.ITEM_FILTER)
         return getItemFilterReference(obj, type);
      
      
      throw new RuntimeException(
         "No reference definition defined in PSReferenceFactory!");
   }      
   
   
   private IPSReference getAutoTransReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof Set))
         throw new IllegalArgumentException(
            "Object must be of type Set");
      
      return PSCoreUtils.createReference(
         "AutoTranslations", "AutoTranslations", "AutoTranslations",
         PSObjectTypeFactory.getType(objType), null);
   }
   
   private IPSReference getCommunityReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof PSCommunity))
         throw new IllegalArgumentException(
            "Object must be of type PSCommunity");
      PSCommunity o = (PSCommunity)obj;
      return PSCoreUtils.createReference(
         o.getName(), o.getLabel(), o.getDescription(),
         PSObjectTypeFactory.getType(objType), o.getGUID());
   }
   
   private IPSReference getContentTypeReference(Object obj, PSObjectTypes objType)
   {      
      if(!(obj instanceof PSItemDefinition))
         throw new IllegalArgumentException(
            "Object must be of type PSItemDefinition");
      PSItemDefinition o = (PSItemDefinition)obj;     
      
      return PSCoreUtils.createReference(
         o.getName(), o.getLabel(), o.getDescription(),
         PSObjectTypeFactory.getType(objType), new PSGuid(0L, PSTypeEnum.NODEDEF, o.getTypeId()));
   }
    
   private IPSReference getKeywordReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof PSKeyword))
         throw new IllegalArgumentException(
            "Object must be of type PSKeyword");
      PSKeyword o = (PSKeyword)obj;
      return PSCoreUtils.createReference(
         o.getLabel(), o.getLabel(), o.getDescription(),
         PSObjectTypeFactory.getType(objType), o.getGUID());
   }
   
   private IPSReference getLocaleReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof PSLocale))
         throw new IllegalArgumentException(
            "Object must be of type PSLocale");
      PSLocale o = (PSLocale)obj;
      return PSCoreUtils.createReference(
         o.getName(), o.getLabel(), o.getDescription(),
         PSObjectTypeFactory.getType(objType), o.getGUID());

   }
   
   private IPSReference getRelTypeReference(Object obj, PSObjectTypes objType)
   {
      
      if(!(obj instanceof PSRelationshipConfig))
         throw new IllegalArgumentException(
            "Object must be of type PSRelationshipConfig");
      PSRelationshipConfig o = (PSRelationshipConfig)obj;     
      
      return PSCoreUtils.createReference(
         o.getName(), o.getLabel(), o.getDescription(),
         PSObjectTypeFactory.getType(objType), o.getGUID());
   }
   
   private IPSReference getSlotReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof PSTemplateSlot))
         throw new IllegalArgumentException(
            "Object must be of type PSTemplateSlot");
      PSTemplateSlot o = (PSTemplateSlot)obj;
      return PSCoreUtils.createReference(
         o.getName(), o.getLabel(), o.getDescription(),
         PSObjectTypeFactory.getType(objType), o.getGUID());
   }
   
   private IPSReference getItemFilterReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof PSItemFilter))
         throw new IllegalArgumentException(
            "Object must be of type PSItemFilter");
      PSItemFilter o = (PSItemFilter)obj;
      return PSCoreUtils.createReference(
         o.getName(), o.getLabel(), o.getDescription(),
         PSObjectTypeFactory.getType(objType), o.getGUID());
   }

   private IPSReference getTemplateReference(Object obj)
   {
      if(!(obj instanceof PSAssemblyTemplate))
         throw new IllegalArgumentException(
            "Object must be of type PSAssemblyTemplate");
      PSAssemblyTemplate o = (PSAssemblyTemplate)obj;
      Enum secondary = null;
      if (o.getOutputFormat().equals(OutputFormat.Global))
         secondary = PSObjectTypes.TemplateSubTypes.GLOBAL;
      else if (o.isVariant())
         secondary = PSObjectTypes.TemplateSubTypes.VARIANT;
      else if (o.getTemplateType().equals(TemplateType.Shared))
         secondary = PSObjectTypes.TemplateSubTypes.SHARED;
      else if (o.getTemplateType().equals(TemplateType.Local))
         secondary = PSObjectTypes.TemplateSubTypes.LOCAL;
      else
         secondary = PSObjectTypes.TemplateSubTypes.OTHER;
      
      return PSCoreUtils.createReference(
         o.getName(), o.getName(), o.getDescription(),
         PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE, secondary), o.getGUID());
   }

   private IPSReference getUiDisplayFormatReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof PSDisplayFormat))
         throw new IllegalArgumentException(
            "Object must be of type PSDisplayFormat");
      PSDisplayFormat o = (PSDisplayFormat)obj;
      return PSCoreUtils.createReference(
         o.getInternalName(), o.getDisplayName(), o.getDescription(),
         PSObjectTypeFactory.getType(objType),
         o.getGUID());
   }
   
   private IPSReference getUiSearchReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof PSSearch))
         throw new IllegalArgumentException("Object must be of type PSSearch");
      PSSearch o = (PSSearch)obj;
      Enum subType;
      if (objType == PSObjectTypes.UI_SEARCH)
      {
         subType = o.isCustomSearch() ? PSObjectTypes.SearchSubTypes.CUSTOM
               : PSObjectTypes.SearchSubTypes.STANDARD; 
      }
      else
      {
         assert(objType == PSObjectTypes.UI_VIEW);
         subType = o.isCustomView() ? PSObjectTypes.SearchSubTypes.CUSTOM
               : PSObjectTypes.SearchSubTypes.STANDARD; 
      }
      return PSCoreUtils.createReference(
         o.getInternalName(), o.getDisplayName(), o.getDescription(),
         PSObjectTypeFactory.getType(objType, subType),
         o.getGUID());
   }
   
   private IPSReference getUiActionMenuReference(Object obj, PSObjectTypes objType)
   {
      if(!(obj instanceof PSAction))
         throw new IllegalArgumentException("Object must be of type PSAction");
      PSAction o = (PSAction)obj;
      Enum secondary = null;
      if (o.isMenuItem())
      {
         if (o.isClientAction())
            secondary = PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_SYSTEM;
         else
            secondary = PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_USER;
      }
      else
      {
         if (o.isClientAction())
         {
            if (o.isCascadedMenu())
            {
               secondary = 
                  PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_SYSTEM;
            }
            else
            {
               secondary = 
                  PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_SYSTEM;
            }
         }
         else if (o.isCascadedMenu())
            secondary = PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER;
         else
         {
            assert(o.isDynamicMenu());
            secondary = PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER;
         }
      }
      return PSCoreUtils.createReference(
         o.getName(), o.getLabel(), o.getDescription(),
         PSObjectTypeFactory.getType(objType, secondary),
         o.getGUID());
   }
   
   private IPSReference getUserFileReference(Object obj, PSObjectTypes type)
   {
      try
      {
         if (!(obj instanceof PSHierarchyNode))
            throw new IllegalArgumentException(
                  "Object must be of type PSHierarchyNode");
         PSHierarchyNode node = (PSHierarchyNode) obj;
         //todo - deal w/ parent
         return new PSHierarchyNodeRef(null,
                     node.getName(),
                     new PSObjectType(
                           type,
                           node.getType() == PSHierarchyNode.NodeType.FOLDER 
                                 ? PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER
                                 : PSObjectTypes.UserFileSubTypes.PLACEHOLDER),
                     node.getGUID(),
                     node.getType() == PSHierarchyNode.NodeType.FOLDER);
      }
      catch (PSModelException e)
      {
         //shouldn't happen
         throw new RuntimeException(e);
      }
   }
   
   /**
    * The factory instance
    */
   private static PSReferenceFactory ms_instance; 

}

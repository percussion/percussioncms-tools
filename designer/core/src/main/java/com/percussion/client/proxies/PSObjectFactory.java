/*******************************************************************************
 *
 * [ PSObjectFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.proxies;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.SearchSubTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.PSObjectTypes.UiActionMenuSubTypes;
import com.percussion.client.PSObjectTypes.XmlApplicationSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationType;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.extension.PSExtensionDef;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.services.ui.data.PSHierarchyNode.NodeType;
import com.percussion.utils.guid.IPSGuid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Contains static helper methods for creating references and clones off of the
 * objects.
 */
public class PSObjectFactory
{
   /**
    * Static access only.
    */
   private PSObjectFactory()
   {
   }

   /**
    * Same as {@link #objectToReference(Object, IPSPrimaryObjectType, boolean)}
    * except that this takes an array of objects and return array of references.
    * 
    * @param objects object array to extract references from, must not be
    * <code>null</code>, if empty empty reference array will be returned.
    * @param pType primary object type, must not be <code>null</code>, the
    * secondary object type is guessed where possible, if not possible taken as
    * <code>null</code>.
    * @param setId if <code>true</code> the object id is extracted and set in
    * each reference. Otherwise id field in the reference will not be set.
    * @return reference array, never <code>null</code> will be empty if the
    * supplied object array is empyt.
    */
   static public IPSReference[] objectToReference(Object[] objects,
      IPSPrimaryObjectType pType, boolean setId)
   {
      if (objects == null)
      {
         throw new IllegalArgumentException("objects must not be null");
      }
      if (pType == null)
      {
         throw new IllegalArgumentException("pType must not be null");
      }
      IPSReference[] refs = new IPSReference[objects.length];
      for (int i = 0; i < objects.length; i++)
         refs[i] = objectToReference(objects[i], pType, setId);

      return refs;
   }

   /**
    * Creates an {@link IPSReference} object off of the supplied object and its
    * primary type. The secondary type is computed from the object wherever is
    * applicable.
    * 
    * @param obj source object, must not be <code>null</code>.
    * @param pType object primary type, must not be <code>null</code>, the
    * secondary object type is guessed where possible, if not possible taken as
    * <code>null</code>.
    * @param setId if <code>true</code> the object id is extracted and set in
    * the reference. Otherwise id field in the reference will not be set.
    * @return the created reference with id not set, never <code>null</code>.
    */
   static public IPSReference objectToReference(Object obj,
      IPSPrimaryObjectType pType, boolean setId)
   {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj must not be null");
      }
      if (pType == null)
      {
         throw new IllegalArgumentException("pType must not be null");
      }
      if (pType == PSObjectTypes.AUTO_TRANSLATION_SET)
      {
         return getAutoTransReference(obj, setId);
      }
      else if (pType == PSObjectTypes.EXTENSION)
      {
         return getExtensionReference(obj, setId);
      }
      else if (pType == PSObjectTypes.SHARED_FIELDS)
      {
         return getSharedFieldReference(obj, setId);
      }
      else if (pType == PSObjectTypes.TEMPLATE)
      {
         return getTemplateReference(obj, setId);
      }
      else if (pType == PSObjectTypes.SLOT)
      {
         return getSlotReference(obj, setId);
      }
      else if (pType == PSObjectTypes.UI_ACTION_MENU)
      {
         return getUiActionReference(obj, setId);
      }
      else if (pType == PSObjectTypes.UI_SEARCH)
      {
         return getUiSearchReference(obj, setId);
      }
      else if (pType == PSObjectTypes.UI_VIEW)
      {
         return getUiViewReference(obj, setId);
      }
      else if (pType == PSObjectTypes.USER_FILE)
      {
         return getUserFileReference(obj, setId);
      }
      else if (pType == PSObjectTypes.XML_APPLICATION)
      {
         return getXmlApplicationReference(obj);
      }
      return getObjectReference(obj, pType, null, setId);
   }

   /**
    * Creates a new reference for the supplied object type.
    * 
    * @param o Assumed not <code>null</code>. Must be of instance
    * {@link PSApplication} and the type of the application must be system or
    * user.
    * 
    * @return Never <code>null</code>.
    */
   private static IPSReference getXmlApplicationReference(Object o)
   {
      PSApplication app = (PSApplication) o;
      final XmlApplicationSubTypes sType;
      if (app.getApplicationType() == PSApplicationType.SYSTEM)
      {
         sType = XmlApplicationSubTypes.SYSTEM;
      }
      else if (app.getApplicationType() == PSApplicationType.USER)
      {
         sType = XmlApplicationSubTypes.USER;
      }
      else
      {
         throw new IllegalArgumentException(
               "Unrecognized application type: " + app.getApplicationType());
      }

      PSReference ref = (PSReference) PSCoreUtils.createReference(app
            .getName(), app.getName(), app.getDescription(),
            PSObjectTypeFactory.getType(PSObjectTypes.XML_APPLICATION, sType),
            new PSGuid(0, PSTypeEnum.LEGACY_CHILD, app.getId()));
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   private static IPSReference getUserFileReference(Object obj, boolean setId)
   {
      PSHierarchyNode hNode = ((PSHierarchyNode) obj);
      NodeType type = hNode.getType();
      Enum sType = PSObjectTypes.UserFileSubTypes.PLACEHOLDER;
      if (type == NodeType.FOLDER)
         sType = PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER;

      PSReference ref = (PSReference) getObjectReference(obj,
         PSObjectTypes.USER_FILE, sType, false);
      if (setId)
         ref.setId(hNode.getGUID());
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getAutoTransReference(Object obj, boolean setId)

   {
      if (!(obj instanceof Set))
         throw new IllegalArgumentException("Object must be of type Set");

      PSReference ref = (PSReference) PSCoreUtils.createReference(
         "AutoTranslations", "AutoTranslations", "AutoTranslations",
         PSObjectTypeFactory.getType(PSObjectTypes.AUTO_TRANSLATION_SET), null);
      if (setId)
         ref.setId(PSAutoTranslation.getAutoTranslationsGUID());
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getExtensionReference(Object obj, 
      @SuppressWarnings("unused") boolean setId)
   {
      if (!(obj instanceof PSExtensionDef))
         throw new IllegalArgumentException(
            "Object must be of type PSExtensionDef");
      PSExtensionDef def = (PSExtensionDef) obj;
      PSReference ref = (PSReference) PSCoreUtils.createReference(def.getRef()
         .getFQN(), def.getRef().getExtensionName(), "New Extension",
         PSObjectTypeFactory.getType(PSObjectTypes.EXTENSION), null);
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getSharedFieldReference(Object obj, boolean setId)

   {
      if (!(obj instanceof PSContentEditorSharedDef))
         throw new IllegalArgumentException(
            "Object must be of type PSContentEditorSharedDef");
      PSContentEditorSharedDef def = (PSContentEditorSharedDef) obj;
      String name = ((PSSharedFieldGroup) def.getFieldGroups().next())
         .getFilename();
      PSReference ref = (PSReference) PSCoreUtils.createReference(name, name,
         name, PSObjectTypeFactory.getType(PSObjectTypes.SHARED_FIELDS), null);
      if (setId)
         ref.setId(new PSDesignGuid(PSTypeEnum.CONFIGURATION, Math.abs(name
            .hashCode())));
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getTemplateReference(Object obj, boolean setId)

   {
      if (!(obj instanceof PSAssemblyTemplate))
         throw new IllegalArgumentException(
            "Object must be of type PSAssemblyTemplate");
      PSAssemblyTemplate t = (PSAssemblyTemplate) obj;

      final TemplateSubTypes sType;
      if (t.isVariant())
      {
         sType = TemplateSubTypes.VARIANT;
      }
      else if (t.getOutputFormat() == OutputFormat.Global)
      {
         sType = TemplateSubTypes.GLOBAL;
      }
      else if (t.getTemplateType() == TemplateType.Local)
      {
         sType = TemplateSubTypes.LOCAL;
      }
      else if (t.getTemplateType() == TemplateType.Shared)
      {
         sType = TemplateSubTypes.SHARED;
      }
      else
      {
         throw new IllegalArgumentException(
               "Unrecognized template configuration! Template: " + t);
      }

      PSReference ref = (PSReference) PSCoreUtils.createReference(t.getName(),
         t.getLabel(), t.getDescription(), PSObjectTypeFactory.getType(
            PSObjectTypes.TEMPLATE, sType), null);
      if (setId)
         ref.setId(new PSDesignGuid(t.getGUID()));

      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getSlotReference(Object obj, boolean setId)
   {
      if (!(obj instanceof PSTemplateSlot))
         throw new IllegalArgumentException(
            "Object must be of type PSTemplateSlot");
      
      PSReference ref = (PSReference) getObjectReference(obj,
         PSObjectTypes.SLOT, null, false);
      
      if (setId)
      {
         PSTemplateSlot s = (PSTemplateSlot) obj;
         ref.setId(new PSDesignGuid(s.getGUID()));
      }
      
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getUiActionReference(Object obj, boolean setId)
   {
      if (!(obj instanceof PSAction))
         throw new IllegalArgumentException("Object must be of type PSAction");
      
      PSAction a = (PSAction) obj;

      UiActionMenuSubTypes sType = UiActionMenuSubTypes.MENU_ENTRY_USER;
      if (a.isCascadedMenu() || a.isDynamicMenu())
      {
         if (a.isClientAction())
         {
            if (a.isCascadedMenu())
               sType = UiActionMenuSubTypes.MENU_CASCADING_SYSTEM;
            else
               sType = UiActionMenuSubTypes.MENU_DYNAMIC_SYSTEM;
         }
         else if (a.isCascadedMenu())
            sType = UiActionMenuSubTypes.MENU_CASCADING_USER;
         else
            sType = UiActionMenuSubTypes.MENU_DYNAMIC_USER;
      }
      else if (a.isClientAction())
      {
         sType = UiActionMenuSubTypes.MENU_ENTRY_SYSTEM;
      }

      PSReference ref = (PSReference) PSCoreUtils.createReference(a.getName(),
         a.getLabel(), a.getDescription(), PSObjectTypeFactory.getType(
            PSObjectTypes.UI_ACTION_MENU, sType), null);
      
      if (setId)
         ref.setId(new PSDesignGuid(a.getGUID()));
      
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getUiSearchReference(Object obj, boolean setId)

   {
      if (!(obj instanceof PSSearch))
         throw new IllegalArgumentException("Object must be of type PSSearch");
      PSSearch s = (PSSearch) obj;

      SearchSubTypes sType = SearchSubTypes.CUSTOM;
      if (s.isStandardSearch())
         sType = SearchSubTypes.STANDARD;

      PSReference ref = (PSReference) PSCoreUtils.createReference(s.getName(),
         s.getLabel(), s.getDescription(), PSObjectTypeFactory.getType(
            PSObjectTypes.UI_SEARCH, sType), null);
      if (setId)
         ref.setId(new PSDesignGuid(s.getGUID()));
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getUiViewReference(Object obj, boolean setId)

   {
      if (!(obj instanceof PSSearch))
         throw new IllegalArgumentException("Object must be of type PSSearch");
      PSSearch s = (PSSearch) obj;

      SearchSubTypes sType = SearchSubTypes.CUSTOM;
      if (s.isStandardView())
         sType = SearchSubTypes.STANDARD;

      PSReference ref = (PSReference) PSCoreUtils.createReference(s.getName(), s.getLabel(),
         s.getDescription(), PSObjectTypeFactory.getType(
            PSObjectTypes.UI_VIEW, sType), null);
      if (setId)
         ref.setId(new PSDesignGuid(s.getGUID()));
      return ref;
   }

   /**
    * @see #objectToReference(Object, IPSPrimaryObjectType, boolean)
    */
   static private IPSReference getObjectReference(Object obj,
      IPSPrimaryObjectType pType, Enum secondary, boolean setId)
   {
      Class claz = obj.getClass();
      Method getName = null;
      Method getLabel = null;
      Method getDesc = null;
      Method getId = null;
      try
      {
         if (setId)
         {
            try
            {
               getId = claz.getMethod("getGUID", new Class[] {});
            }
            catch (NoSuchMethodException e)
            {
               // Does not even have a get method throw runtime exception
               throw new RuntimeException(
                  "Object does not implement have getId(). "
                     + "Conversion to reference failed");
            }
         }
         try
         {
            getName = claz.getMethod("getName", new Class[] {});
         }
         catch (NoSuchMethodException e)
         {
            // Does not even have a get method throw runtime exception
            throw new RuntimeException(
               "Object does not implement have getName(). "
                  + "Conversion to reference failed");
         }
         try
         {
            getLabel = claz.getMethod("getLabel", new Class[] {});
         }
         catch (NoSuchMethodException e)
         {
            getLabel = null;
         }
         try
         {
            getDesc = claz.getMethod("getDescription", new Class[] {});
         }
         catch (NoSuchMethodException e)
         {
            getDesc = null;
         }
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      String name, label, desc;
      IPSGuid id = null;
      try
      {
         if (getId != null)
         {
            id = (IPSGuid) getId.invoke(obj, new Object[] {});
            if (id != null)
               id = new PSDesignGuid(id);
         }
         name = (String) getName.invoke(obj, new Object[] {});
         label = name;
         desc = name;
         if (getLabel != null)
         {
            label = (String) getLabel.invoke(obj, new Object[] {});
         }
         if (getDesc != null)
         {
            desc = (String) getDesc.invoke(obj, new Object[] {});
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e);
      }

      return PSCoreUtils.createReference(name, label, desc, PSObjectTypeFactory
         .getType(pType, secondary), id);
   }

   /**
    * Sets the supplied name for the object using reflection. It assumes the
    * method with signature setName(String) exists in the object's class.
    * 
    * @param object object on which the name to be set, must not be
    * <code>null</code>.
    * @param name name to set must not be <code>null</code> or empty.
    */
   public static void setName(Object object, String name)
   {
      if (object == null)
      {
         throw new IllegalArgumentException("object must not be null");
      }
      if (name == null || name.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      Class claz = object.getClass();
      Method setName = null;
      try
      {
         setName = claz.getMethod("setName", new Class[]
         {
            String.class
         });
         setName.invoke(object, new Object[]
         {
            name
         });
      }
      catch (NoSuchMethodException e)
      {
         // Does not even have a gset method throw runtime exception
         throw new RuntimeException(
            "Object does not implement have setName(String)."
               + "New name could not be set");
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e);
      }
   }
}

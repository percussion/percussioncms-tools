/*******************************************************************************
 *
 * [ PSObjectTypeFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client;

import com.percussion.services.catalog.PSTypeEnum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class acts as a centralized source of the {@link PSObjectType} class.
 * It makes sure there is only a single instance for any given type.
 *
 * @author paulhoward
 */
public class PSObjectTypeFactory
{
   /**
    * Attempts to retrieve the requested type from cache. If one is found it is
    * returned. Otherwise one is created and added to the cache and returned.
    * 
    * @param primary Never <code>null</code>.
    * 
    * @param secondary May be <code>null</code> if the primary type does not
    * require it. If the secondary type doesn't match what the primary requires,
    * an IllegalArgumentException will be thrown. 
    */
   static public PSObjectType getType(IPSPrimaryObjectType primary,
         Enum secondary)
   {
      if (null == primary)
      {
         throw new IllegalArgumentException("primary type cannot be null");  
      }

      Set<PSObjectType> existingTypes = m_cache.get(primary);
      if (existingTypes == null)
      {
         existingTypes = new HashSet<PSObjectType>();
         m_cache.put(primary, existingTypes);
      }

      PSObjectType result = null;
      for (PSObjectType type : existingTypes)
      {
         Enum existingSec = type.getSecondaryType(); 
         if (existingSec == null)
         {
            assert(type.getPrimaryType() == primary);
            result = type;
            break;
         }
         else if (existingSec == secondary)
         {
            result = type;
            break;
         }
            
      }
      
      if (null == result)
      {
         result = new PSObjectType(primary, secondary);
         existingTypes.add(result);
      }
      
      return result;
   }

   /**
    * Find an instance based on a string representation of it.
    * 
    * @param serializedForm A value returned by the
    * {@link PSObjectType#toSerial()} method. Never <code>null</code> or
    * empty.
    */
   static public PSObjectType getType(String serializedForm)
   {
      PSObjectType tmp = new PSObjectType(serializedForm);
      return getType((IPSPrimaryObjectType) tmp.getPrimaryType(), tmp
            .getSecondaryType());
   }
   
   /**
    * Convenience method that calls {@link #getType(IPSPrimaryObjectType, Enum)
    * getType((IPSPrimaryObjectType) primary, <code>null</code>)}.
    * 
    * @throws ClassCastException If the type does not implement the required 
    * interface.
    */
   static public PSObjectType getType(Enum primary)
   {
      return getType((IPSPrimaryObjectType) primary, null);      
   }
   
   /**
    * Converts the passed in server type to its equivilent
    * primary object type if there is one.
    * @param type the server type to be converted, cannot be
    * <code>null</code>.
    * @return the primary object type or <code>null</code> if
    * there is none.
    */
   static public Enum convertServerTypeToPrimaryType(
      PSTypeEnum type)
   {
      if(type == null)
         throw new IllegalArgumentException("type cannot be null.");
      Enum primary = ms_typeToPrimaryMap.get(type);
      if(primary == null)
         return null;
      return primary;
   }
   
   /**
    * Converts the passed in primary object type to its equivilent
    * server type if there is one.
    * @param type the primary object type to be converted, cannot be
    * <code>null</code>.
    * @return the primary object type or <code>null</code> if
    * there is none.
    */
   static public PSTypeEnum convertPrimaryTypeToServerType(
      Enum type)
   {
      if(type == null)
         throw new IllegalArgumentException("type cannot be null.");
      PSTypeEnum primary = ms_primaryToTypeMap.get(type);
      if(primary == null)
         return null;
      return primary;
   }
         
   /**
    * Only used for static methods.
    */
   private PSObjectTypeFactory()
   {}

   /**
    * Stores the instances grouped by their primary type. Never
    * <code>null</code>.
    */
   static private Map<IPSPrimaryObjectType, Set<PSObjectType>> m_cache = new 
      HashMap<IPSPrimaryObjectType, Set<PSObjectType>>();
   
   /**
    * Mapping of server types to primary object types
    */
   static private Map<PSTypeEnum, Enum> ms_typeToPrimaryMap = 
      new HashMap<PSTypeEnum, Enum>();
   static
   {
      ms_typeToPrimaryMap.put(PSTypeEnum.AUTO_TRANSLATIONS,
         PSObjectTypes.AUTO_TRANSLATION_SET);
      ms_typeToPrimaryMap.put(PSTypeEnum.ACTION,
         PSObjectTypes.UI_ACTION_MENU);
      ms_typeToPrimaryMap.put(PSTypeEnum.COMMUNITY_DEF,
         PSObjectTypes.COMMUNITY);
      ms_typeToPrimaryMap.put(PSTypeEnum.CONFIGURATION,
         PSObjectTypes.CONFIGURATION_FILE);
      ms_typeToPrimaryMap.put(PSTypeEnum.DISPLAY_FORMAT,
         PSObjectTypes.UI_DISPLAY_FORMAT);
      ms_typeToPrimaryMap.put(PSTypeEnum.EXTENSION,
         PSObjectTypes.EXTENSION);
      ms_typeToPrimaryMap.put(PSTypeEnum.KEYWORD_DEF,
         PSObjectTypes.KEYWORD);
      ms_typeToPrimaryMap.put(PSTypeEnum.ITEM_FILTER,
         PSObjectTypes.ITEM_FILTER);
      ms_typeToPrimaryMap.put(PSTypeEnum.LOCALE,
         PSObjectTypes.LOCALE);
      ms_typeToPrimaryMap.put(PSTypeEnum.NODEDEF,
         PSObjectTypes.CONTENT_TYPE);
      ms_typeToPrimaryMap.put(PSTypeEnum.RELATIONSHIP,
         PSObjectTypes.RELATIONSHIP_TYPE);
      ms_typeToPrimaryMap.put(PSTypeEnum.ROLE,
         PSObjectTypes.ROLE);
      ms_typeToPrimaryMap.put(PSTypeEnum.SEARCH_DEF,
         PSObjectTypes.UI_SEARCH);
      ms_typeToPrimaryMap.put(PSTypeEnum.SITE,
         PSObjectTypes.SITE);
      ms_typeToPrimaryMap.put(PSTypeEnum.SLOT,
         PSObjectTypes.SLOT);
      ms_typeToPrimaryMap.put(PSTypeEnum.TEMPLATE,
         PSObjectTypes.TEMPLATE);
      ms_typeToPrimaryMap.put(PSTypeEnum.VIEW_DEF,
         PSObjectTypes.UI_VIEW);
      ms_typeToPrimaryMap.put(PSTypeEnum.WORKFLOW,
         PSObjectTypes.WORKFLOW);
   }
   
   /**
    * Mapping of server types to primary object types
    */
   static private Map<Enum, PSTypeEnum> ms_primaryToTypeMap = 
      new HashMap<Enum, PSTypeEnum>();
   static
   {
      // create the reverse index of ms_typeToPrimaryMap
      for(PSTypeEnum type : ms_typeToPrimaryMap.keySet())
      {
         ms_primaryToTypeMap.put(ms_typeToPrimaryMap.get(type), type);
      }
   }
}

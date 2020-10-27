/******************************************************************************
 *
 * [ PSUiSearchModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreMessages;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.services.catalog.PSTypeEnum;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSUiSearchModelProxy extends PSComponentTestModelProxy
{

   public PSUiSearchModelProxy()
   {
      this(PSObjectTypes.UI_SEARCH);
   }
   
   /**
    * @param type
    */
   public PSUiSearchModelProxy(PSObjectTypes type)
   {
      super(type);
      try
      {
         loadFromRepository();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
     
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * rename(com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void rename(IPSReference ref, String name, Object data)
   {
      // we may not know about the data object if it hasn't been persisted
      PSSearch def = (PSSearch) m_repositoryMap.get(ref);
      if (def != null)
      {
         def.setInternalName(name);
         try
         {
            PSProxyTestUtil.saveRepository(m_repositoryMap, getRepositoryFile());
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error saving to repository file "
               + getRepositoryFile(), e);
         }
      }
      renameLocal(ref, name, data);      
   }
   
   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      ((PSReference) ref).setName(name);
      if (data != null)
      {
         ((PSSearch) data).setInternalName(name);
      }      
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List results)
   {
      if(sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty.");
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if(results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for(int i = 0; i < sourceObjects.length; i++)
      {
         if(sourceObjects[i] instanceof PSSearch)
         {
            try
            {
               PSSearch def = (PSSearch)((PSSearch)sourceObjects[i]).clone();               
               def.setLocator(PSSearch.createKey(new String[] { String
                     .valueOf(PSCoreUtils.dummyGuid(PSTypeEnum.SEARCH_DEF)
                           .getUUID()) }));
               String name;
               if (names == null || StringUtils.isBlank(names[i]))
               {
                  name = PSCoreMessages.getString("common.prefix.copyof") //$NON-NLS-1$
                     + def.getInternalName();
               }
               else
                  name = names[i];
               def.setInternalName(name);
               results.add(def);
               refs[i] = objectToReference(def);
               m_lockHelper.getLock(refs[i]);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }            
         }
         else
         {
            throw new IllegalArgumentException(
                  "sourceObjects must be instances of PSKeyword");
         }
      }
      return refs;
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(PSObjectType objType,
         Collection<String> names, List results)
   {
      if (objType == null
         || !objType.getPrimaryType().equals(m_objectPrimaryType))
      {
         throw new IllegalArgumentException("objType is invalid.");
      }
      if(names == null)
         throw new IllegalArgumentException("names cannot be null.");
      if(results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for(String name : names)
      {
         PSSearch def = createNewObject(name); 
         Enum secondary = objType.getSecondaryType();
         if (secondary == PSObjectTypes.SearchSubTypes.STANDARD)
         {
            if (objType.getPrimaryType() == PSObjectTypes.UI_SEARCH)
               def.setType(PSSearch.TYPE_STANDARDSEARCH);
            else
               def.setCustom(false);
         }
         else if (secondary == PSObjectTypes.SearchSubTypes.CUSTOM)
         {
            if (objType.getPrimaryType() == PSObjectTypes.UI_SEARCH)
               def.setType(PSSearch.TYPE_CUSTOMSEARCH);
            else
               def.setCustom(true);
         }
         refs[++idx] = objectToReference(def);
         assert(refs[idx].getObjectType().equals(objType));
         m_lockHelper.getLock(refs[idx]);
         results.add(def);
      }
      return refs;
   }
   
   /**
    * Load the existing objects from the repository.
    * @throws PSProxyTestException  
    */
   @SuppressWarnings("unchecked")
   protected void loadFromRepository() throws PSProxyTestException 
   {
      m_repositoryMap.clear();
      if (!getRepositoryFile().exists())
      {
         // If repository does not exist
         // create a few to start with
         for (int i = 0; i < 4; i++)
         {
            String[] names = new String[]{
               "Sample_Custom_Search", "userSearch", "stdSearch1", "stdSearch2"};
            PSSearch def = createNewObject(names[i]);
            switch (i)
            {
               case 0:
                  def.setType(PSSearch.TYPE_CUSTOMSEARCH);
                  break;
               case 1:
                  def.setType(PSSearch.TYPE_USERSEARCH);
                  break;
               case 2:
                  def.setType(PSSearch.TYPE_STANDARDSEARCH);
                  break;
               case 3:
                  def.setType(PSSearch.TYPE_STANDARDSEARCH);
                  break;
            }
            m_repositoryMap.put(objectToReference(def), def);
         }
         // and save to repository
         saveRepository(m_repositoryMap, getRepositoryFile());
      }
      else
      {
         m_repositoryMap = 
            (PSRepositoryMap)loadRepository(getRepositoryFile());
      }
   }
      
   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * createNewObject(java.lang.String)
    */
   protected PSSearch createNewObject(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
            
      int id = new SecureRandom().nextInt() & Integer.MAX_VALUE;
      PSSearch def = null; 
      try
      {
         def = new PSSearch();
         def.setLocator(PSSearch.createKey(new String[] {String.valueOf(id)}));      
         def.setInternalName(name);
         def.setDisplayName(def.getInternalName());           
         def.setDescription(PSCoreMessages.getString("common.prefix.description") //$NON-NLS-1$
            + " " + name); //$NON-NLS-1$
         def.setType(PSSearch.TYPE_VIEW);
      }
      catch(Exception ignore){}
      return def;      
   }
   
   
   
   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryFile()
    */
   @Override
   protected File getRepositoryFile()
   {
      return new File("UiSearch_repository.xml");
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryMap()
    */
   @Override
   protected IPSRepositoryMap getRepositoryMap()
   {
      return m_repositoryMap;
   }
   
   @Override
   protected Object newComponentInstance()
   {
      PSDbComponent comp = null;
      try
      {
         comp =  new PSSearch();
      }
      catch (PSCmsException ignore){}
      return comp;
   }  

   /**
    * Map of all object from the repository. Filled during initialization of the
    * proxy.
    */
   protected PSRepositoryMap m_repositoryMap = 
      new PSRepositoryMap();

  

}

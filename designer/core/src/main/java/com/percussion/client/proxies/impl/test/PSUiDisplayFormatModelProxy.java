/******************************************************************************
 *
 * [ PSUiDisplayFormatModelProxy.java ]
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
import com.percussion.cms.objectstore.PSDisplayFormat;
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
public class PSUiDisplayFormatModelProxy extends PSComponentTestModelProxy
{

   public PSUiDisplayFormatModelProxy()
   {
      this(PSObjectTypes.UI_DISPLAY_FORMAT);
   }
   
   /**
    * @param type
    */
   public PSUiDisplayFormatModelProxy(PSObjectTypes type)
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
      PSDisplayFormat def = (PSDisplayFormat) m_repositoryMap.get(ref);
      if (def != null)
      {
         def.setInternalName(name);
         try
         {
            PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error saving to repository file "
               + ms_repository, e);
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
         ((PSDisplayFormat) data).setInternalName(name);
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
         if(sourceObjects[i] instanceof PSDisplayFormat)
         {
            try
            {
               PSDisplayFormat def = 
                  (PSDisplayFormat) ((PSDisplayFormat) sourceObjects[i]).clone();
               def.setLocator(PSDisplayFormat.createKey(
                     new String[] { String.valueOf(
                           PSCoreUtils.dummyGuid(
                                 PSTypeEnum.DISPLAY_FORMAT).getUUID())}));
                  
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
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names, List<Object> results)
   {
      if (objType == null
         || !objType.getPrimaryType().equals(m_objectPrimaryType))
         throw new IllegalArgumentException("objType is invalid.");
      if(names == null)
         throw new IllegalArgumentException("names cannot be null.");
      if(results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for(String name : names)
      {
         PSDisplayFormat def = createNewObject(name, -1);         
         refs[++idx] = objectToReference(def);
         m_lockHelper.getLock(refs[idx]);
         results.add(def);
      }
      return refs;
   }
   
   /**
    * Load the existing objects from the repository.
    * @throws PSProxyTestException  
    */
   protected void loadFromRepository() throws PSProxyTestException 
   {
      m_repositoryMap.clear();
      if (!ms_repository.exists())
      {
         // If repository does not exist
         // create a few to start with
         for (int i = 0; i < 5; i++)
         {
            String[] names = new String[]{
               "By_Author", "By_Status", "By_Type", "Default", "Extended"};
            long[] ids = new long[]{
               10, 20, 30, 1, 100};
           
            PSDisplayFormat def = createNewObject(names[i], ids[i]);
            m_repositoryMap.put(objectToReference(def), def);
         }
         // and save to repository
         saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = 
            (PSRepositoryMap)loadRepository(ms_repository);
      }
   }   
   
   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * createNewObject(java.lang.String)
    */
   protected PSDisplayFormat createNewObject(String name, long id)
   {
     if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
            
      if (id < 1)
         id = new SecureRandom().nextInt() & Integer.MAX_VALUE;
      PSDisplayFormat def = null; 
      try
      {
         def = new PSDisplayFormat();
         def.setLocator(PSDisplayFormat.createKey(new String[] { String
               .valueOf(id) }));      
         def.setInternalName(name);
         def.setDisplayName(def.getInternalName());           
         def.setDescription(PSCoreMessages.getString("common.prefix.description") //$NON-NLS-1$
            + " " + name); //$NON-NLS-1$
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
      return ms_repository;
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
         comp =  new PSDisplayFormat();
      }
      catch (PSCmsException ignore){}
      return comp;
   }
   
   /**
    * Name of the repository file.
    */
   public static final String REPOSITORY_XML = "UiDisplayFormat_repository.xml";

   /**
    * Repository file name this test proxy uses. The file will be created
    * in the root directory for the workbench if one does not exist. It will use
    * the existing one if one exists.
    */
    private static File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all object from the repository. Filled during initialization of the
    * proxy.
    */
   protected PSRepositoryMap m_repositoryMap = 
      new PSRepositoryMap();

  

}

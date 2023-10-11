/******************************************************************************
 *
 * [ PSKeywordModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreMessages;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PSKeywordModelProxy extends PSTestModelProxy
{

   public PSKeywordModelProxy()
   {
      super(PSObjectTypes.KEYWORD);      
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
    * @see com.percussion.client.proxies.impl.PSKeywordModelProxy#
    * create(com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @Override
   public IPSReference[] create(
      final PSObjectType objType, final Collection<String> names,
      final List<Object> results)
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
         PSKeyword keyword = (PSKeyword)createNewObject(name);
         keyword.setSequence(1);
         refs[++idx] = objectToReference(keyword);
         m_lockHelper.getLock(refs[idx]);
         results.add(keyword);
      }
      return refs;
   }

   /* 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @Override
   @SuppressWarnings("unchecked")
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if(sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty.");
      if(results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for(int i = 0; i < sourceObjects.length; i++)
      {
         if(sourceObjects[i] instanceof PSKeyword)
         {
            try
            {
               PSKeyword keyword = (PSKeyword)clone(sourceObjects[i]);
               keyword.setGUID(
                  PSCoreUtils.dummyGuid(PSTypeEnum.KEYWORD_DEF));
               String name;
               if (names == null || StringUtils.isBlank(names[i]))
               {
                  name = PSCoreMessages.getString("common.prefix.copyof") //$NON-NLS-1$
                     + keyword.getLabel();
               }
               else
                  name = names[i];
               keyword.setLabel(name);
               results.add(keyword);
               refs[i] = objectToReference(keyword);
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
    * @see com.percussion.client.proxies.impl.PSKeywordModelProxy#rename(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void rename(IPSReference ref, String name, Object data) 
   throws PSModelException
   {
      // we may not know about the data object if it hasn't been persisted
      PSKeyword keyword = (PSKeyword) m_repositoryMap.get(ref);
      if (keyword != null)
      {
         keyword.setLabel(name);
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
         ((PSKeyword) data).setLabel(name);
      }      
   }

   /**
    * Load the existing objects from the repository.
    * @throws PSProxyTestException  
    */
   private void loadFromRepository() throws PSProxyTestException 
   {
      m_repositoryMap.clear();
      if (!ms_repository.exists())
      {
         PSKeywordChoice choice1 = null;
         PSKeywordChoice choice2 = null;
         // If repository does not exist
         // create a few to start with
         for (int i = 0; i < 6; i++)
         {
            String[] names = new String[]{"Dog", "Cat", "Foo", "Bar", "Font", "FooBarFooBar"};
            PSKeyword keyword = createNewObject(names[i]);
            keyword.setKeywordType("Test");
            keyword.setSequence(i + 1);
                        
            if (i == 2)
            {
               keyword.setValue("FooVal");
               choice1 = new PSKeywordChoice(keyword);
            }
            
            if (i == 3)
            {
               keyword.setValue("BarVal");
               choice2 = new PSKeywordChoice(keyword);
            }
            
            if (i == 5)
            {
               List<PSKeywordChoice> choices = new ArrayList<>();
               choices.add(choice1);
               choices.add(choice2);
               keyword.setChoices(choices);
            }
            
            m_repositoryMap.put(objectToReference(keyword), keyword);
         }
         // and save to repository
         PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = 
            (PSKeywordMap)PSProxyTestUtil.loadRepository(ms_repository);
      }
   }
   
   
   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * createNewObject(java.lang.String)
    */
   private PSKeyword createNewObject(String name)
   {
     if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
            
      IPSGuid guid = PSCoreUtils.dummyGuid(PSTypeEnum.KEYWORD_DEF);
      PSKeyword keyword = new PSKeyword();
      keyword.setGUID(guid);
      keyword.setLabel(name);           
      keyword.setDescription(PSCoreMessages.getString("common.prefix.description") //$NON-NLS-1$
         + " " + name); //$NON-NLS-1$      
      return keyword;      
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
   
   /**
    * Name of the repository file.
    */
   public static final String REPOSITORY_XML = "keyword_repository.xml";

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
   private PSKeywordMap m_repositoryMap = 
      new PSKeywordMap();   
   

   
  

}

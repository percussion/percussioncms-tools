/******************************************************************************
 *
 * [ PSCommunityModelProxy.java ]
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
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author erikserating
 * 
 */
public class PSCommunityModelProxy extends PSTestModelProxy
{

   /**
    * Default ctor. Invokes base class version with <@link
    * PSObjectTypes#COMMUNITY).
    */
   public PSCommunityModelProxy()
   {
      super(PSObjectTypes.COMMUNITY);
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
    * rename(com.percussion.client.IPSReference, java.lang.String,
    * java.lang.Object)
    */
   @SuppressWarnings("unused") //exception
   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {

      // we may not know about the data object if it hasn't been persisted
      PSCommunity community = (PSCommunity) m_repositoryMap.get(ref);
      if (community != null)
      {
         community.setName(name);
         ((PSReference) ref).setName(name);
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
         ((PSCommunity) data).setName(name);
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
      if (sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty.");
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if (results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for (int i = 0; i < sourceObjects.length; i++)
      {
         if (sourceObjects[i] instanceof PSCommunity)
         {
            try
            {
               PSCommunity community = (PSCommunity) clone(sourceObjects[i]);
               community.setId(PSCoreUtils.dummyGuid(PSTypeEnum.COMMUNITY_DEF)
                  .longValue());
               
               String name;
               if (names == null || StringUtils.isBlank(names[i]))
               {
                  name = PSCoreMessages.getString("common.prefix.copyof") //$NON-NLS-1$
                        + community.getName();
               }
               else
                  name = names[i];
               community.setName(name);
               results.add(community);
               refs[i] = objectToReference(community);
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
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results)
   {
      if (objType == null
         || !objType.getPrimaryType().equals(m_objectPrimaryType))
         throw new IllegalArgumentException("objType is invalid.");
      if (names == null)
         throw new IllegalArgumentException("names cannot be null.");
      if (results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for (String name : names)
      {
         PSCommunity community = createNewObject(name, -1);
         refs[++idx] = objectToReference(community);
         m_lockHelper.getLock(refs[idx]);
         results.add(community);
      }
      return refs;
   }

   /**
    * Load the existing objects from the repository.
    * 
    * @throws PSProxyTestException
    */
   private void loadFromRepository() throws PSProxyTestException
   {
      m_repositoryMap.clear();
      if (!ms_repository.exists())
      {
         // If repository does not exist
         // create a few to start with
         for (int i = 0; i < 3; i++)
         {
            String[] names = new String[]
            {
               "Default", "Enterprise_Investments", "Corporate_Investments"
            };
            int[] ids = new int[]
            {
               10, 100, 200
            };
            PSCommunity community = createNewObject(names[i], ids[i]);

            if (i == 2)
            {
               Collection<IPSGuid> roles = new ArrayList<>();

               roles.add(new PSGuid(PSTypeEnum.ROLE, 10));
               roles.add(new PSGuid(PSTypeEnum.ROLE, 70));
               roles.add(new PSGuid(PSTypeEnum.ROLE, 80));

               community.setRoleAssociations(roles);
            }

            m_repositoryMap.put(objectToReference(community), community);
         }
         // and save to repository
         PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = (PSCommunityMap) PSProxyTestUtil
            .loadRepository(ms_repository);
      }
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * createNewObject(java.lang.String)
    */
   private PSCommunity createNewObject(String name, int id)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      IPSGuid guid = id == -1 ? PSCoreUtils.dummyGuid(PSTypeEnum.COMMUNITY_DEF)
         : new PSGuid(PSTypeEnum.COMMUNITY_DEF, id);
      PSCommunity community = new PSCommunity();
      community.setId(guid.longValue());
      community.setName(name);
      community.setDescription(PSCoreMessages
         .getString("common.prefix.description") //$NON-NLS-1$
         + " " + name); //$NON-NLS-1$      
      return community;
   }

   // see interface
   public Collection<IPSGuid> getCommunityRoleIds(IPSReference commRef)
   {
      Set<IPSGuid> result = new HashSet<>();
      try
      {
         Collection<IPSReference> commRefs = null;
         if (commRef == null)
         {
            commRefs = catalog();
         }
         else
         {
            commRefs = new ArrayList<>(1);
            commRefs.add(commRef);
         }
         Object[] comms = load(commRefs.toArray(new IPSReference[0]), false,
               false);
         for (Object obj : comms)
         {
            PSCommunity comm = (PSCommunity) obj;
            result.addAll(comm.getRoleAssociations());
         }
      }
      catch (PSMultiOperationException | PSModelException e)
      {
         logError(e);
      }
       return result;
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
   public static final String REPOSITORY_XML = "community_repository.xml";

   /**
    * Repository file name this test proxy uses. The file will be created in the
    * root directory for the workbench if one does not exist. It will use the
    * existing one if one exists.
    */
   private static  File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all object from the repository. Filled during initialization of the
    * proxy.
    */
   private PSCommunityMap m_repositoryMap = new PSCommunityMap();
}

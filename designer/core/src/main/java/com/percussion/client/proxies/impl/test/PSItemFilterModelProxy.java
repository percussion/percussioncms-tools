/*******************************************************************************
 *
 * [ PSItemFilterModelProxy.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreMessages;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.objectstore.IPSReferenceable;
import com.percussion.client.proxies.PSProxyException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.filter.data.PSItemFilterRuleDef;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is an implementation of the proxy for offline testing, i.e. when the
 * server is not running. Most of the functionality is simulated here except for
 * user level locking and persistence of the locks.
 */
public class PSItemFilterModelProxy extends PSTestModelProxy
{
   /**
    * Default ctor that invokes the base class version that takes the object
    * type. Loads the objects from the local repository for later use.
    * 
    * @throws PSProxyException
    * 
    * @throws PSProxyException any other error during initialization such as
    * loading repository.
    */
   public PSItemFilterModelProxy() throws PSProxyException
   {
      super(PSObjectTypes.ITEM_FILTER);
      try
      {
         PSXmlSerializationHelper.addType(PSItemFilter.class);
         PSXmlSerializationHelper.addType(
            PSItemFilterMap.class);
         loadFromRepository();
         if (m_repositoryMap == null)
         {
            throw new PSProxyException(PSErrorCodes.RAW,
               "Local repository could not be loaded");
         }
      }
      catch (Exception e)
      {
         throw new PSProxyException(PSErrorCodes.RAW, new Object[]
         {
            "Could not initialize the proxy"
         }, e);
      }
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
         for (int i = 0; i < 8; i++)
         {
            PSItemFilter itemFilter;
            itemFilter = createNewObject(PSObjectTypes.ITEM_FILTER
               .toString()
               + i, i);
            m_repositoryMap.put(objectToReference(itemFilter), itemFilter);
         }
         // and save to repository
         PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = (PSItemFilterMap) PSProxyTestUtil
            .loadRepository(ms_repository);
      }
   }

   /*
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @Override
   @SuppressWarnings("unchecked")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results)
   {
      if (results == null)
         throw new IllegalArgumentException("results cannot be null");
      else
         results.clear();
      IPSReference[] refs = new PSReference[names.size()];
      int i = 0;
      for (String name : names)
      {
         PSItemFilter itemFilter;
         itemFilter = createNewObject(name, -1);
         results.add(itemFilter);
         refs[i] = objectToReference(itemFilter);
         m_lockHelper.getLock(refs[i]);
         i++;
      }
      return refs;
   }

   /*
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List<Object> results)
   {
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if (results == null)
         throw new IllegalArgumentException("results cannot be null");
      else
         results.clear();
      IPSReference[] refs = new PSReference[sourceObjects.length];
      Collection<String> existingNames = new ArrayList<>();
      for (Object o : getRepositoryMap().values())
      {
         existingNames.add(((PSItemFilter) o).getName().toLowerCase());
      }

      for (int i = 0; i < sourceObjects.length; i++)
      {
         PSItemFilter itemFilter;
         try
         {
            itemFilter = (PSItemFilter) clone(sourceObjects[i]);
            itemFilter.setGUID(PSCoreUtils.dummyGuid(PSTypeEnum.SLOT));
            String name;
            if (names == null || StringUtils.isBlank(names[i]))
            {
               name = PSCoreUtils.createCopyName(itemFilter.getName(), -1, 
                  existingNames);
            }
            else
               name = names[i];
            itemFilter.setName(name);
            results.add(itemFilter);
            refs[i] = objectToReference(itemFilter);
            existingNames.add(itemFilter.getName());
            m_lockHelper.getLock(refs[i]);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      int i = 0;
      for (IPSReference ref : refs)
      {
         ((IPSReferenceable) results.get(i++)).setReference(ref);
      }
      return refs;
   }

   /*
    * @see com.percussion.client.proxies.IPSCmsModelProxy#rename(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void rename(IPSReference ref, String name, Object data)
   {
      // we may not know about the data object if it hasn't been persisted
      PSItemFilter itemFilter = (PSItemFilter) m_repositoryMap.get(ref);
      if (itemFilter != null)
      {
         try
         {
            itemFilter.setName(name);
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

   // see interface for details
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      ((PSReference) ref).setName(name);
      if (data != null)
      {
         ((PSItemFilter) data).setName(name);
      }
   }

   /**
    * Helper method to create a new {@link PSItemFilter} object.
    * 
    * @param name name of the slot, assumed not <code>null</code>.
    * @param id
    * @return new item filter object, never <code>null</code>.
    */
   private PSItemFilter createNewObject(String name, int id)
   {
      IPSGuid guid;

      if (id != -1)
      {
         guid = new PSGuid(1L, PSTypeEnum.ITEM_FILTER, id);
      }
      else
      {
         guid = PSCoreUtils.dummyGuid(PSTypeEnum.ITEM_FILTER);
      }

      String desc = PSCoreMessages.getString("common.prefix.description") //$NON-NLS-1$
         + " " + name; //$NON-NLS-1$
      PSItemFilter itemFilter = new PSItemFilter();
      itemFilter.setGUID(guid);
      itemFilter.setName(name);
      itemFilter.setDescription(desc);
      PSItemFilterRuleDef rDef = new PSItemFilterRuleDef();
      // rDef.setRule("Rule1");
      rDef.setParam("param1", "value1");
      rDef.setParam("param2", "value2");
      Set<IPSItemFilterRuleDef> rDefSet = new HashSet<>();
      rDefSet.add(rDef);
      itemFilter.setRuleDefs(rDefSet);

      return itemFilter;
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
   public static final String REPOSITORY_XML = "item_filter_repository.xml";

   /**
    * Item filter repository file name this test proxy uses. The file will be
    * created in the root directory for the workbench if one does not exist. It
    * will use the existing one if one exists.
    */
    private static File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all slots from the repository. Filled during initialization of the
    * proxy.
    */
   private PSItemFilterMap m_repositoryMap = new PSItemFilterMap();

}

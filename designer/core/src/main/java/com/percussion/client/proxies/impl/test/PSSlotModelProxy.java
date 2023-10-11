/******************************************************************************
 *
 * [ PSSlotModelProxy.java ]
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
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.objectstore.IPSReferenceable;
import com.percussion.client.objectstore.PSUiTemplateSlot;
import com.percussion.client.proxies.PSProxyException;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is an implementation of the proxy for offline testing, i.e. when the
 * server is not running. Most of the functionality is simulated here except for
 * user level locking and persistence of the locks.
 */
public class PSSlotModelProxy extends PSTestModelProxy
{
   /**
    * Ctor taking the object type. Loads the objects from the local repository
    * for later use.
    * 
    * @throws PSProxyException any other error during initialization such as
    * loading repository.
    */
   public PSSlotModelProxy() throws PSProxyException
   {
      super(PSObjectTypes.SLOT);
      try
      {
         loadFromRepository();
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
            PSTemplateSlot slot = createNewObject(PSObjectTypes.SLOT
                  .toString()
                  + i, i);
            if (i==2)
            {
               Collection<PSPair<IPSGuid, IPSGuid>> associations = 
                  new ArrayList<>();
               associations.add(new PSPair<>(
                     new PSGuid(0L, PSTypeEnum.NODEDEF, 10),
                     new PSGuid(1L, PSTypeEnum.TEMPLATE, 200)));
               slot.setSlotAssociations(associations);
            }
            if (i==3)
            {
               Collection<PSPair<IPSGuid, IPSGuid>> associations = 
                  new ArrayList<>();
               associations.add(new PSPair<>(
                     new PSGuid(0L, PSTypeEnum.NODEDEF, 30), 
                     new PSGuid(1L, PSTypeEnum.TEMPLATE, 200)));
               slot.setSlotAssociations(associations);

               associations.add(new PSPair<>(
                     new PSGuid(0L, PSTypeEnum.NODEDEF, 30), 
                     new PSGuid(1L, PSTypeEnum.TEMPLATE, 110)));
               slot.setSlotAssociations(associations);
            }
            if (i==4)
            {
               Collection<PSPair<IPSGuid, IPSGuid>> associations = 
                  new ArrayList<>();
               associations.add(new PSPair<>(
                     new PSGuid(0L, PSTypeEnum.NODEDEF, 30), 
                     new PSGuid(1L, PSTypeEnum.TEMPLATE, 100)));
               slot.setSlotAssociations(associations);
            }
            if (i==5)
            {
               slot.setSlottype(1);
            }
            m_repositoryMap.put(objectToReference(slot), slot);
         }
         // and save to repository
         PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = (PSSlotMap) 
            PSProxyTestUtil.loadRepository(ms_repository);
      }
   }

   
   /*
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @Override
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
         PSTemplateSlot slot = createNewObject(name, -1);
         slot.setName(name);
         results.add(slot);
         refs[i] = objectToReference(slot);
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
      Collection<String> existingNames = new ArrayList<String>();
      for (Object o : getRepositoryMap().values())
      {
         existingNames.add(((PSTemplateSlot) o).getName().toLowerCase());
      }

      for (int i = 0; i < sourceObjects.length; i++)
      {
         PSTemplateSlot slot;
         try
         {
            slot = (PSTemplateSlot) clone(sourceObjects[i]);
            slot.setGUID(PSCoreUtils.dummyGuid(PSTypeEnum.SLOT));
            String name;
            if (names == null || StringUtils.isBlank(names[i]))
            {
               name = PSCoreUtils.createCopyName(slot.getName(), -1,
                     existingNames);
            }
            else 
               name = names[i];
            slot.setName(name);
            results.add(slot);
            refs[i] = objectToReference(slot);
            existingNames.add(slot.getName());
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
      PSTemplateSlot slot = (PSTemplateSlot) m_repositoryMap.get(ref);
      if (slot != null)
      {
         slot.setName(name);
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

   // see interface for details
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      ((PSReference) ref).setName(name);
      if (data != null)
      {
         ((PSTemplateSlot) data).setName(name);
      }
   }

   
   /**
    * Helper method to create a new {@link PSTemplateSlot} object.
    * 
    * @param name name of the slot, assumed not <code>null</code>.
    * @return new slot object, never <code>null</code>.
    */
   private PSTemplateSlot createNewObject(String name, int id)

   {
      IPSGuid guid;

      if(id != -1) {
         guid = new PSGuid(1L, PSTypeEnum.SLOT, id);
      }
      else {
         guid = PSCoreUtils.dummyGuid(PSTypeEnum.SLOT);
      }

      String desc = PSCoreMessages.getString("common.prefix.description") //$NON-NLS-1$
         + " " + name; //$NON-NLS-1$
      PSTemplateSlot slot = new PSUiTemplateSlot();
      slot.setGUID(guid);
      slot.setName(name);
      slot.setDescription(desc);
      slot.setRelationshipName(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      slot.setSlottype(0);
      slot.setSystemSlot(false);

      return slot;
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
   public static final String REPOSITORY_XML = "slot_repository.xml";

   /**
    * Slot repository file name this test proxy uses. The file will be created
    * in the root directory for the workbench if one does not exist. It will use
    * the existing one if one exists.
    */
   private static  File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all slots from the repository. Filled during initialization of the
    * proxy.
    */
   private PSSlotMap m_repositoryMap = new PSSlotMap();

}

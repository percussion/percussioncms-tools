/******************************************************************************
 *
 * [ PSSharedFieldsModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.impl.PSReference;
import com.percussion.client.objectstore.PSUiContentEditorDefinitionConverter;
import com.percussion.client.objectstore.PSUiContentEditorSharedDef;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.error.PSException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.util.PSCollection;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.PSContentEditorDefinition;
import com.percussion.webservices.contentdesign.ContentDesignSOAPStub;
import com.percussion.webservices.contentdesign.LoadSharedDefinitionRequest;
import com.percussion.webservices.contentdesign.LoadSharedDefinitionResponse;
import com.percussion.webservices.contentdesign.SaveSharedDefinitionRequest;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#SHARED_FIELDS}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSSharedFieldsModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#SHARED_FIELDS} and for main type and
    * <code>null</code> sub type since this object type does not have any sub
    * types.
    */
   public PSSharedFieldsModelProxy()
   {
      super(PSObjectTypes.SHARED_FIELDS);
      PSTransformerFactory.getInstance().register(
         PSUiContentEditorDefinitionConverter.class,
         PSUiContentEditorSharedDef.class, PSContentEditorSharedDef.class);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Collection<IPSReference> result = new ArrayList<IPSReference>();
      try
      {
         Object[] objects = load(null, false, false);
         PSUiContentEditorSharedDef sDef = (PSUiContentEditorSharedDef) objects[0];
         result = getRefsFromSharedDef(sDef);
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException(e);
      }
      return result;
   }

   @Override
   public boolean isLocked(IPSReference ref)
   {
      return m_locks.containsKey(ref) && m_locks.get(ref).equals(Boolean.TRUE);
   }
   
   /**
    * Parse the shared def and extract all references based on the file names.
    * 
    * @param sDef shared defintiion, assumed not <code>null</code>
    * @return collection of references, never <code>null</code> may be empty.
    */
   private Collection<IPSReference> getRefsFromSharedDef(
      PSContentEditorSharedDef sDef)
   {
      Set<String> names = new HashSet<String>();
      Iterator iter = sDef.getFieldGroups();
      while (iter.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup) iter.next();
         names.add(group.getFilename());
      }
      Collection<IPSReference> result = new ArrayList<IPSReference>();
      for (String name : names)
      {
         IPSReference ref = PSCoreUtils.createReference(name, name, name,
            PSObjectTypeFactory.getType(PSObjectTypes.SHARED_FIELDS), null);
         ((PSReference) ref).setPersisted();
         result.add(ref);
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(@SuppressWarnings("unused")
   PSObjectType objType, Collection<String> names, List<Object> results)
   {
      if (names == null || names.isEmpty())
      {
         throw new IllegalArgumentException("names must not be null or empty");
      }
      for (String name : names)
      {
         if (name == null || name.length() == 0)
         {
            throw new IllegalArgumentException(
               "name for a shared def file must not be null or empty");
         }
      }
      if (results == null)
      {
         throw new IllegalArgumentException("result must not be null");
      }
      if(m_sharedDef==null)
      {
         try
         {
            loadSharedDefFromServer(true, false);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      results.clear();
      IPSReference[] refs = new IPSReference[names.size()];
      int i = 0;
      String ext = ".xml";
      for (String name : names)
      {
         String fileName = name;
         if(!fileName.endsWith(ext))
            fileName += ext;
         if (name.endsWith(ext))
            name = name.substring(0, name.length() - ext.length());

         IPSReference ref = PSCoreUtils.createReference(fileName, fileName,
            fileName, PSObjectTypeFactory.getType(PSObjectTypes.SHARED_FIELDS),
            null);
         m_locks.put(ref, Boolean.TRUE);
         PSProxyUtils.setLockInfo(new IPSReference[]{ref}, false);
         
         refs[i++] = ref;

         PSCollection coll = new PSCollection(PSSharedFieldGroup.class);
         PSSharedFieldGroup group = new PSSharedFieldGroup(name, fileName);
         coll.add(group);
         m_sharedDef.setFieldGroupsByFileName(coll.iterator());
         results.add(new PSUiContentEditorSharedDef(coll));
      }
      return refs;
   }

   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException
   {
      if (reference == null && lock)
      {
         throw new IllegalArgumentException(
            "You cannot load entire shared definition locked");
      }

      PSUiContentEditorSharedDef sharedDef = null;
      if (m_sharedDef != null)
      {
         sharedDef = cloneSharedDef(m_sharedDef);
      }
      else
      {
         Exception ex = null;
         try
         {
            sharedDef = loadSharedDefFromServer(lock, overrideLock);
         }
         catch (PSLockFault e)
         {
            ex = PSProxyUtils.convertFault(e, METHOD.LOAD.toString(), 
               "sharedFieldDefinition", "sharedFields");
         }
         catch (PSNotAuthorizedFault e)
         {
            ex = e;
         }
         catch (RemoteException e)
         {
            ex = e;
         }
         catch (MalformedURLException e)
         {
            ex = e;
         }
         catch (ServiceException e)
         {
            ex = e;
         }
         catch (PSTransformationException e)
         {
            ex = e;
         }
         catch (Exception e)
         {
            ex = e;
         }
         if (ex != null)
         {
            throw new PSMultiOperationException(ex);
         }
      }
      if (reference == null)
      {
         return new Object[]
         {
            sharedDef
         };
      }
      
      PSUiContentEditorSharedDef[] sDefs = 
         new PSUiContentEditorSharedDef[reference.length];
      for (int i = 0; i < reference.length; i++)
      {
         PSCollection groupColl = sharedDef
            .lookupFieldGroupByFileName(reference[i].getName());
         m_locks.put(reference[i], lock);
            
         // Create a shared def with just the group asked for and add to
         // result.
         PSUiContentEditorSharedDef def = new PSUiContentEditorSharedDef();
         def.setFieldGroups(groupColl);
         sDefs[i] = def;
      }
      if(lock)
         PSProxyUtils.setLockInfo(reference, false);
      
      return sDefs;
   }

   /**
    * Helper method to clone the shared def object.
    * 
    * @param sharedDef shared object to clone, assumed not <code>null</code>.
    * @return cloned object, never <code>null</code>.
    */
   private PSUiContentEditorSharedDef cloneSharedDef(
      PSUiContentEditorSharedDef sharedDef)
   {
      if (sharedDef == null)
      {
         throw new IllegalArgumentException("sharedDef must not be null");
      }
      PSUiContentEditorSharedDef sDef = new PSUiContentEditorSharedDef();
      sDef.copyFrom(sharedDef);
      return sDef;
   }

   /**
    * Load the shared definition object from the server.
    * 
    * @see #load(IPSReference[], boolean, boolean) for the latter two parameter
    * description.
    * @param lock
    * @param overrideLock
    * @return shared definition object loaded from server, never
    * <code>null</code>.
    * @throws MalformedURLException
    * @throws ServiceException
    * @throws PSLockFault
    * @throws PSNotAuthorizedFault
    * @throws RemoteException
    * @throws PSTransformationException
    */
   private PSUiContentEditorSharedDef loadSharedDefFromServer(boolean lock,
      boolean overrideLock) throws MalformedURLException, ServiceException,
      PSLockFault, PSNotAuthorizedFault, RemoteException, 
      PSTransformationException, Exception
   {
      boolean redo = false;
      do
      {
         redo = false;
         try
         {
            PSUiContentEditorSharedDef sharedDef = null;
            ContentDesignSOAPStub binding = getSoapBinding(METHOD.LOAD);
            LoadSharedDefinitionRequest request = new LoadSharedDefinitionRequest();
            request.setLock(lock);
            request.setOverrideLock(overrideLock);
            LoadSharedDefinitionResponse response = binding
               .loadSharedDefinition(request);
            sharedDef = (PSUiContentEditorSharedDef) PSProxyUtils.convert(
               PSUiContentEditorSharedDef.class, response
                  .getPSContentEditorDefinition());
            if (lock)
            {
               sharedDef.getFieldGroups();
               // loaded with locking - cache it locally
               m_sharedDef = cloneSharedDef(sharedDef);
            }
            return sharedDef;
         }
         catch (PSInvalidSessionFault e)
         {
            PSCoreFactory.getInstance().reconnect();
            redo = true;
         }
      } while (redo);
      
      // will never happen
      return null;
   }

   @Override
   public void delete(IPSReference[] reference)
      throws PSMultiOperationException
   {
      boolean loadedHere = false;
      if (m_sharedDef == null)
      {
         // was never loaded with lock, load with lock
         load(reference, true, false);
         loadedHere = true;
      }
      for (IPSReference ref : reference)
      {
         PSCollection groups = m_sharedDef.lookupFieldGroupByFileName(ref
            .getName());
         Iterator iter = groups.iterator();
         while (iter.hasNext())
         {
            m_sharedDef.removeFieldGroup((PSSharedFieldGroup) iter.next());
         }
      }
      try
      {
         saveSharedFieldToServer();
      }
      finally
      {
         try
         {
            releaseLock(reference);
         }
         catch (PSMultiOperationException e)
         {
            // Not much can be done here, a stray will result which will be
            // discarded after lock times out anyway.
            logError(e);
         }
      }
      // Delete the lock entries (just for fun!)
      for (IPSReference ref : reference)
         m_locks.remove(ref.getId());
      try
      {
         // The master is still locked and was loaded here, unlock it
         if (m_sharedDef != null && loadedHere)
            releaseLock();
      }
      catch (PSModelException e)
      {
         throw new PSMultiOperationException(e);
      }
   }

   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {
      if (refs == null || refs.length != 1)
      {
         throw new IllegalArgumentException(
            "refs array must not be null and length must be equal to 1");
      }
      if (data == null || data.length != 1)
      {
         throw new IllegalArgumentException(
            "data array must not be null and length must be equal to 1");
      }
      PSUiContentEditorSharedDef sDef = (PSUiContentEditorSharedDef) data[0];
      if (m_sharedDef == null)
      {
         throw new PSMultiOperationException(new PSException(
            "Shared definition was never loaded"));
      }

      m_sharedDef.setFieldGroupsByFileName(sDef.getFieldGroups());

      // Create or alter the tables and save the object
      Object[] result = new Object[data.length];
      boolean errorOccured = false;
      // Create or alter tables.
      Iterator iter = sDef.getFieldGroups();
      while (iter.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup) iter.next();
         try
         {
            PSContentEditorTableHandler th = new PSContentEditorTableHandler(
               group.getFieldSet(), group.getUIDefinition().getDisplayMapper(),
               group.getLocator(),
               PSContentEditorTableHandler.EDITOR_TYPE_SHARED, sDef);
            th.setTableReferences();
            th.createAlterTablesForEditor();
         }
         catch (Exception e)
         {
            result[0] = e;
            errorOccured = true;
         }
      }
      if (errorOccured)
      {
         throw new PSMultiOperationException(result);
      }
      saveSharedFieldToServer();
      final PSReference ref = (PSReference) refs[0];
      ref.setPersisted();
      if (releaseLock)
         releaseLock(refs);
      PSProxyUtils.setLockInfo(refs, releaseLock);
   }

   /**
    * Save the object loaded previously and cached locally to server.
    * 
    * @throws PSMultiOperationException
    */
   private void saveSharedFieldToServer() throws PSMultiOperationException
   {
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               PSContentEditorDefinition def = 
                  (PSContentEditorDefinition) PSProxyUtils.convert(
                     PSContentEditorDefinition.class, m_sharedDef);
               ContentDesignSOAPStub binding = getSoapBinding(METHOD.SAVE);
               SaveSharedDefinitionRequest request = 
                  new SaveSharedDefinitionRequest();
               request.setRelease(false);
               request.setPSContentEditorDefinition(def);
               binding.saveSharedDefinition(request);
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      } catch (PSLockFault e)
      {
         ex = PSProxyUtils.convertFault(e, METHOD.SAVE.toString(), 
            "sharedFieldDefinition", "sharedFields");
      }
      catch (MalformedURLException | ServiceException | RemoteException e)
      {
         ex = e;
      }
       if (ex != null)
      {
         throw new PSMultiOperationException(ex);
      }
   }

   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List<Object> results)
   {
      throw new UnsupportedOperationException(
         "Create is not supported by this object");
   }

   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      boolean loadedHere = false;
      if (m_sharedDef == null)
      {
         // was never loaded with lock, load with lock
         try
         {
            load(new IPSReference[]
            {
               ref
            }, true, false);
         }
         catch (PSMultiOperationException e)
         {
            throw new PSModelException(e);
         }
         loadedHere = true;
      }
      PSCollection groups = m_sharedDef.lookupFieldGroupByFileName(ref
         .getName());
      Iterator iter = groups.iterator();
      while (iter.hasNext())
         ((PSSharedFieldGroup) iter.next()).setFilename(name);

      try
      {
         saveSharedFieldToServer();
         // The master is still locked and was loaded here, unlock it
         if (m_sharedDef != null && loadedHere)
            releaseLock();
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException((Throwable) e.getResults()[0]);
      }
      // Now change the file names in the data
      if (data == null)
         return;
      groups = ((PSUiContentEditorSharedDef) data)
         .lookupFieldGroupByFileName(ref.getName());
      iter = groups.iterator();
      while (iter.hasNext())
         ((PSSharedFieldGroup) iter.next()).setFilename(name);

   }

   @Override
   public void renameLocal(@SuppressWarnings("unused")
   IPSReference ref, @SuppressWarnings("unused")
   String name, @SuppressWarnings("unused")
   Object data)
   {
      throw new UnsupportedOperationException(
         "renameLocal is not supported for this object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#releaseLock(com.percussion.client.IPSReference[])
    */
   @Override
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      if (m_sharedDef == null)
         return;
      // Release the local locks for the references first
      for (IPSReference ref : references)
         m_locks.put(ref, false);
      PSProxyUtils.setLockInfo(references, true);

      // Check if any of the references locked, if so return
      Iterator iter = m_locks.values().iterator();
      while (iter.hasNext())
      {
         Boolean locked = (Boolean) iter.next();
         if (locked)
         {
            return;
         }
      }
      // All local locks are clear, so release the lock on the master.
      try
      {
         releaseLock();
      }
      catch (PSModelException e)
      {
         throw new PSMultiOperationException(e);
      }
      m_sharedDef = null;
   }

   /**
    * Calls the {@link PSCmsModelProxy#releaseLock(IPSReference[])} with the
    * GUID of the master shared definition to lock the master.
    * 
    * @throws PSMultiOperationException
    * @throws PSModelException
    */
   private void releaseLock() throws PSMultiOperationException,
      PSModelException
   {
      super.releaseLock(new IPSReference[]
      {
         // Fields other than guid are dummy and are fine for release lock.
         new PSReference("SharedDef", "SharedDef", "SharedDef",
            PSObjectTypeFactory.getType(PSObjectTypes.SHARED_FIELDS),
            m_masterGuid)
      });
   }
   
   /* (non-Javadoc)
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#flush(com.percussion.client.IPSReference)
    */
   @Override
   public void flush(@SuppressWarnings("unused") IPSReference ref)
   {
      m_locks.clear();
      m_sharedDef = null;
   }
   
   /**
    * Does nothing as shared field does not have ACLs.
    */
   @Override
   @SuppressWarnings("unused")
   public void deleteAcl(IPSReference[] owners)
   {
   }

   /**
    * Returns ACLs indicating full access to the object.
    */
   @Override
   @SuppressWarnings("unused")
   public Object[] loadAcl(IPSReference[] refs, boolean lock)
   {
      final Object[] result = new Object[refs.length];
      for (int i = 0; i < refs.length; i++)
      {
         result[i] = PSSecurityUtils.createNewAcl(); 
      }
      return result;
   }

   /**
    * Does nothing as shared field does not have ACLs.
    */
   @Override
   @SuppressWarnings("unused")
   public void releaseAclLock(Long[] aclIds)
   {
   }

   /**
    * Does nothing as shared field does not have ACLs.
    */
   @Override
   @SuppressWarnings("unused")
   public void saveAcl(IPSReference[] ref, IPSAcl[] acl, boolean releaseLock)
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#getSoapBinding(com.percussion.client.proxies.IPSCmsModelProxy.METHOD)
    */
   @Override
   protected ContentDesignSOAPStub getSoapBinding(@SuppressWarnings("unused")
   METHOD method) throws MalformedURLException, ServiceException
   {
      return PSProxyUtils.getContentDesignStub();
   }

   /**
    * Master shared definition object. Set when a shared definition is loaded
    * with lock. Set to <code>null</code> when local locks for all the shared
    * files are released. Shared file parts are loaded (after cloning) from this
    * object.
    */
   private PSUiContentEditorSharedDef m_sharedDef = null;

   /**
    * Local locks for each shared file. These are required here since the master
    * shared definition is a single object for all shared files.
    */
   private Map<IPSReference, Boolean> m_locks = new HashMap<IPSReference, Boolean>();

   /**
    * Just shortcut for the GUID for the shared definition object.
    */
   private IPSGuid m_masterGuid = new PSDesignGuid(PSTypeEnum.CONFIGURATION,
      PSUiContentEditorSharedDef.SHARED_DEF_ID);
}

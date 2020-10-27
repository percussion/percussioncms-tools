/******************************************************************************
 *
 * [ PSTestModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.impl.PSCmsModelProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class PSTestModelProxy extends PSCmsModelProxy
{

   public PSTestModelProxy(PSObjectTypes type)
   {
      super();
      m_objectPrimaryType = type;
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * catalog(com.percussion.client.IPSReference)
    */
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Collection<IPSReference> results = new ArrayList<IPSReference>();
      Iterator it = getRepositoryMap().getAll();
      while (it.hasNext())
      {
         PSReference pRef = (PSReference) objectToReference(it.next());
         pRef.setPersisted();
         results.add(pRef);
      }
      return results;
   }

   /*
    * gets all the objects of the proxy type and returns them as objects so they
    * can be cast.
    * 
    * @return Collection of Objects
    * 
    * throws PSModelException if anything goes wrong
    */
   @SuppressWarnings("unchecked")
   public Collection<Object> getObjects() throws PSModelException
   {
      return getRepositoryMap().values();
   }

   // see base class
   public void delete(IPSReference[] references)
      throws PSMultiOperationException
   {
      IPSRepositoryMap map = getRepositoryMap();
      File repositoryFile = getRepositoryFile();

      Object[] results = new Object[references.length];
      boolean error = false;
      for (int i = 0; i < references.length; i++)
      {
         IPSReference ref = references[i];
         map.remove(ref);
         m_lockHelper.releaseLock(ref);
      }
      try
      {
         saveRepository(map, repositoryFile);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error saving to repository file "
            + repositoryFile, e);
      }
      if (error)
         throw new PSMultiOperationException(results);
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * load(com.percussion.client.IPSReference[], boolean, boolean)
    */
   @Override
   public Object[] load(IPSReference[] references, boolean lock,
      boolean overrideLock) throws PSMultiOperationException
   {
      IPSRepositoryMap map = getRepositoryMap();

      if (references == null || references.length == 0)
         throw new IllegalArgumentException(
            "reference cannot be null or empty.");
      Object[] results = new Object[references.length];
      boolean error = false;
      for (int i = 0; i < references.length; i++)
      {
         IPSReference ref = references[i];
         // Attempt to get lock if necessary
         if (lock)
         {
            m_lockHelper.getLock(ref);
         }
         // Retrieve the object from the repository
         Object obj = map.get(ref);
         if (obj == null)
         {
            results[i] = new PSModelException(PSErrorCodes.RAW, new Object[]
            {
               "Not found in repository."
            });
            error = true;
            continue;
         }
         try
         {
            obj = clone(obj);
            results[i] = obj;
         }
         catch (Exception e)
         {
            results[i] = e;
            error = true;
         }
      }
      if (error)
         throw new PSMultiOperationException(results);

      return results;
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * releaseLock(com.percussion.client.IPSReference[])
    */
   @Override
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      if (references == null || references.length == 0)
         throw new IllegalArgumentException(
            "reference cannot be null or empty.");
      Object[] results = new Object[references.length];
      boolean error = false;
      for (int i = 0; i < references.length; i++)
      {
         IPSReference ref = references[i];
         m_lockHelper.releaseLock(ref);
      }
      if (error)
         throw new PSMultiOperationException(results);

   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * save(com.percussion.client.IPSReference[], java.lang.Object[], boolean)
    */
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      if (refs.length != data.length)
      {
         throw new IllegalArgumentException(
               "refs and data arrays must be the same length");
      }
      
      IPSRepositoryMap map = getRepositoryMap();
      File repositoryFile = getRepositoryFile();

      Object[] objects = new Object[data.length];
      boolean error = false;
      for (int i = 0; i < data.length; i++)
      {
         if (map.get(refs[i]) != null && !m_lockHelper.hasLock(refs[i]))
         {
            try
            {
               throw new PSLockException("save",
                  m_objectPrimaryType.toString(), refs[i].getName());
            }
            catch (PSLockException e)
            {
               error = true;
               objects[i] = e;
            }
         }
         else
         {
            ((PSReference) refs[i]).setPersisted();
            map.put(refs[i], data[i]);

            objects[i] = null;
            if (releaseLock)
               m_lockHelper.releaseLock(refs[i]);
            else if (!m_lockHelper.hasLock(refs[i]))
               m_lockHelper.getLock(refs[i]);
         }
      }
      if (error)
         throw new PSMultiOperationException(objects);
      try
      {
         saveRepository(map, repositoryFile);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error saving to repository file "
            + repositoryFile, e);
      }

   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * isLocked(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isLocked(IPSReference ref)
   {
      return m_lockHelper.hasLock(ref);
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#isTypeSupported(
    * com.percussion.client.PSObjectType)
    */
   @Override
   public boolean isTypeSupported(PSObjectType type)
   {
      if (type.getPrimaryType() == m_objectPrimaryType)
         return true;
      return false;
   }

   protected void saveRepository(IPSRepositoryMap map, File repositoryFile)
      throws PSProxyTestException
   {
      PSProxyTestUtil.saveRepository(map, repositoryFile);
   }

   /**
    * Creates a reference from the object passed in
    * 
    * @param obj the object, should not be <code>null</code>.
    * @return the <code>IPSReference</code> for the object passed in. Never
    * <code>null</code>.
    */
   protected IPSReference objectToReference(Object obj)
   {
      PSReferenceFactory factory = PSReferenceFactory.getInstance();
      return factory.getReference(obj, m_objectPrimaryType);
   }

   /**
    * @return the repository map used to store this test proxy's objects. Never
    * <code>null</code>.
    */
   protected abstract IPSRepositoryMap getRepositoryMap();

   /**
    * @return the repository file where the map will be persisted to. Never
    * <code>null</code>.
    */
   protected abstract File getRepositoryFile();

   /**
    * The lock helper for this test proxy
    */
   protected PSLockHelper m_lockHelper = new PSLockHelper();
}

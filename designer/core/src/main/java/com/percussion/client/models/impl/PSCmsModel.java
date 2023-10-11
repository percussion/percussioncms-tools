/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.client.models.impl;

import com.percussion.client.*;
import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.models.*;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSProxyException;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedExException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSTypedPrincipal;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.acl.NotOwnerException;
import java.text.MessageFormat;
import java.util.*;

/**
 * This class manages the basics of a model such as the name, description and
 * listener management. All object type specific knowledge is contained in the
 * lower layers that this class depends on to accomplish its work, i.e. the
 * proxies.
 * <p>
 * Although it's main model is a flat model, it does have knowledge of the
 * hierarchical model that is supported via the hierarchy manager. Some
 * operations are disallowed for hierarchical models.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSCmsModel implements IPSCmsModel
{
   /**
    * Create an instance of a model that supports a particular set of design
    * objects.
    * 
    * @param name A unique identifier for this model among all models. Never
    * <code>null</code> or empty.
    * 
    * @param description A brief overview of what this model instance supports.
    * May be <code>null</code> or empty.
    * 
    * @param supportedType The category of design objects supported by this
    * model. If this primary type has sub-types, then any of the sub-types may
    * be processed by this model. The supplied instance must be of type
    * <code>Enum</code>.
    */
   public PSCmsModel(String name, String description,
         IPSPrimaryObjectType supportedType)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }
      if (null == supportedType)
      {
         throw new IllegalArgumentException(
               "primary object type cannot be null");
      }
      if (!(supportedType instanceof Enum))
      {
         throw new IllegalArgumentException(
               "supportedType must be an Enum class");
      }
      m_name = name;

      if (StringUtils.isNotBlank(description))
      {
         m_description = description;
      }
      m_primaryObjectType = supportedType;

      PSCoreFactory.getInstance().addListener(new IPSCoreListener()
      {
         //see base class method for details
         public void logonStateChanged(PSLogonStateChangedEvent event)
         {
            if (event.getEventType() 
                  == PSLogonStateChangedEvent.LogonStateEvents.LOGOFF)
            {
               Collection<IPSReference> lockedRefs = getLockedRefs();
               for (IPSReference ref : lockedRefs)
               {
                  String pattern = 
                     "Received server disconnect while ''{0}:{1} ({2})'' was still locked.";
                  log.warn(MessageFormat.format(pattern, ref
                        .getObjectType(), ref.getName(), ref.getId()));
               }
               
               m_mgrInfos.clear();
            }
            else if (event.getEventType() 
                  == PSLogonStateChangedEvent.LogonStateEvents.LOGON)
            {
               init();
            }
         }
      });
      
      //was there a logon event before we registered our listener?
      if (PSCoreFactory.getInstance().getConnectionInfo() != null)
         init();
   }
   
   /**
    * Performs setup that needs to be done at startup or whenever we connect
    * to a server.
    */
   private void init()
   {
      // set up all manager infos
      if (isHierarchyModel())
      {
         for (String treeName : getHierarchyTreeNames())
         {
            treeName = treeName.toLowerCase();
            m_mgrInfos.put(treeName, new ManagerInfo(treeName));
         }
      }
      else
      {
         m_mgrInfos.put(FLAT_MODEL_TREENAME, new ManagerInfo(FLAT_MODEL_TREENAME));
      }
   }
   
   /**
    * {@inheritDoc}
    * <p>
    * The name supplied in the ctor.
    */
   public String getName()
   {
      return m_name;
   }

   //see interface for details
   public String getDescription(IPSReference ref)
   {
      String description;
      if (isDataCached(ref, false))
      {
         description = PSObjectInfoExtractor.getDescription(
               getDataFromCache(ref, false), (IPSPrimaryObjectType) ref
                     .getObjectType().getPrimaryType());
      }
      else
      {
         description = ref.getDescription();
      }
      return description;
   }

   //see interface for details
   public String getLabelKey(IPSReference ref)
   {
      String label;
      if (isDataCached(ref, false))
      {
         label = PSObjectInfoExtractor.getLabelKey(
               getDataFromCache(ref, false), (IPSPrimaryObjectType) ref
                     .getObjectType().getPrimaryType());
      }
      else
      {
         label = ref.getLabelKey();
      }
      return label;
   }

   //see interface for details
   public String getName(IPSReference ref)
   {
      String name;
      if (isDataCached(ref, false))
      {
         name = PSObjectInfoExtractor.getName(
               getDataFromCache(ref, false), (IPSPrimaryObjectType) ref
                     .getObjectType().getPrimaryType());
      }
      else
      {
         name = ref.getName();
      }
      return name;
   }

   //see interface for details
   public PSObjectType getObjectType(IPSReference ref)
   {
      PSObjectType type;
      if (isDataCached(ref, false))
      {
         type = PSObjectInfoExtractor.getObjectType(
               getDataFromCache(ref, false), (IPSPrimaryObjectType) ref
                     .getObjectType().getPrimaryType());
      }
      else
      {
         type = ref.getObjectType();
      }
      return type;
   }

   // see interface
   @SuppressWarnings("unused")
   public void rename(IPSReference ref, String name)
         throws PSAuthorizationException, PSDuplicateNameException,
         PSLockException, PSModelException
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name cannot be null or empty");  
      }
      if (null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      
      name = name.trim();
      
      try
      {
         getProxy().rename(ref, name,
               getInfo(getTreeName(ref), ref, true).m_data);
      }
      catch (PSModelException e)
      {
         //todo - this should be moved to proxy
         Throwable th = e.getCause();
         if (th instanceof PSAuthenticationFailedExException
               || th instanceof PSAuthorizationException)
         {
            PSException ex = (PSException) th;
            throw new PSAuthorizationException(ex.getErrorCode(), ex
                  .getErrorArguments());
         }
         throw e;
      }
      PSModelChangedEvent event = new PSModelChangedEvent(ref,
            ModelEvents.RENAMED, null);
      notifyListeners(event);
   }

   /**
    * Determines the proper tree name for the supplied ref.
    * 
    * @param ref Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   private String getTreeName(IPSReference ref)
   {
      String name;
      if (isHierarchyModel())
      {
         IPSHierarchyNodeRef nodeRef = (IPSHierarchyNodeRef) ref;
         IPSHierarchyManager mgr = nodeRef.getManager();
         name = mgr.getTreeName();
      }
      else
         name = FLAT_MODEL_TREENAME;
      return name;
   }

   /**
    * {@inheritDoc}
    * <p>
    * The description supplied in the ctor or "" if <code>null</code> was
    * supplied.
    */
   public String getDescription()
   {
      return m_description;
   }

   // see interface
   public IPSReference create(PSObjectType objectType, String name,
      IPSObjectDefaulter defaulter)
         throws PSDuplicateNameException, PSModelException
   {
      try
      {
         List<String> names = new ArrayList<>();
         names.add(name);
         return create(objectType, names, defaulter)[0];
      }
      catch (PSMultiOperationException e)
      {
         Exception e2 = (Exception) e.getResults()[0];
         if (e2 instanceof PSDuplicateNameException)
            throw (PSDuplicateNameException) e2;
         else if (e2 instanceof RuntimeException)
            throw (RuntimeException) e2;
         else
            throw new RuntimeException(e2.getCause());
      }
   }
   
   // see interface
   public IPSReference create(PSObjectType objectType, String name)
         throws PSDuplicateNameException, PSModelException
   {
      return create(objectType, name, null);
   }

   // see interface
   public IPSReference[] create(PSObjectType objectType,
         List<String> names) throws PSMultiOperationException, PSModelException
   {
      return create(objectType, names, null);
   }
   
   // see interface
   public IPSReference[] create(PSObjectType objectType,
         List<String> names, IPSObjectDefaulter defaulter) 
      throws PSMultiOperationException, PSModelException
   {
      checkObjectType(objectType, true);
      
      if (names == null)
      {
         int nameIndex = 1;
         String baseName = objectType.getPrimaryType().toString().toLowerCase();
         baseName = baseName.replace(' ', '_');
         String proposedName = baseName + nameIndex++;
         names = new ArrayList<>();
         //need a unique name
         try
         {
            Collection<IPSReference> c = catalog(false);
            IPSReference[] existingRefs = c.toArray(new IPSReference[c.size()]);
            for (int i=0; i < existingRefs.length; i++)
            {
               if (existingRefs[i].getName().equalsIgnoreCase(proposedName))
               {
                  proposedName = baseName + nameIndex++;
                  i = -1; //it will be incremented at the end of the loop
               }
            }
            names.add(proposedName);
         }
         catch (PSModelException ignore)
         {
            //just choose a name, best we can do
            names.add("object");
         }
      }

      /*
       * Walk thru supplied names, checking against existing ones for dupes.
       */
      Collection<IPSReference> existingRefs;
      try
      {
         existingRefs = catalog(false);
      }
      catch (PSModelException e)
      {
         //if we can't reach the server, do the best we can
         log.info("Catalog failed while creating new objects.", e);
         existingRefs = new ArrayList<>();
      }
      Object[] results = new Object[names.size()];
      Collection<String> validNames = new ArrayList<>();
      int resultIndex = 0;
      for (String name : names)
      {
         if (StringUtils.isBlank(name))
         {
            results[resultIndex] = new IllegalArgumentException(
                  "name cannot be null or empty");
         }
         else
         {
            for (IPSReference existingRef : existingRefs)
            {
               if (existingRef.getName().equalsIgnoreCase(name))
               {
                  results[resultIndex] =
                        new PSDuplicateNameException(name, objectType);
               }
            }
            for (String suppliedName : validNames)
            {
               if (suppliedName.equalsIgnoreCase(name))
               {
                  results[resultIndex] =
                        new PSDuplicateNameException(name, objectType);
               }
            }
         }
         if (results[resultIndex] == null)
            validNames.add(name);
         resultIndex++;
      }

      IPSCmsModelProxy proxy = getProxy();
      List<Object> proxyResults = new ArrayList<>();
      IPSReference[] refs = null;
      if (!validNames.isEmpty())
      {
         refs = proxy.create(objectType, validNames, proxyResults);
   
         //update the data objects before notification
         if (defaulter != null)
         {
            for (Object data : proxyResults)
            {
               if (!(data instanceof Throwable))
                  defaulter.modify(data);
            }
         }
         // add to cache
         objectsCreated(refs, proxyResults);
      }

      // walk thru the results array and set the successful ones
      for (int i = 0, j = 0; i < results.length; i++)
      {
         if (results[i] == null && refs !=null)
            results[i] = refs[j++];
      }

      if (validNames.size() < names.size())
      {
         throw new PSMultiOperationException(results, names.toArray());
      }

      if(refs!=null)
         return Arrays.asList(results).toArray(new IPSReference[refs.length]);
      else
         return Arrays.asList(results).toArray(new IPSReference[results.length]);
   }

   // see interface
   public IPSReference[] create(IPSReference[] source, String[] names)
      throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(source, "source");
      if (source.length == 0)
         return new IPSReference[0];
      isHierarchyModel(true);
      
      PSMultiOperationException ex = null;
      Object[] allData;
      try
      {
         allData = load(source, false, false);
      }
      catch (PSMultiOperationException e)
      {
         ex = e;
         allData = e.getResults();
      }

      Object[] aclData = null;
      boolean supportsAcl = true;
      try
      {
         aclData = loadAcl(source, false);
      }
      catch (PSMultiOperationException e)
      {
         ex = e;
         aclData = e.getResults();
      }
      catch(UnsupportedOperationException e)
      {
         supportsAcl = false;
      }
      List<Object> goodData = new ArrayList<>();
      List<Object> goodAclData = new ArrayList<>();
      List<IPSReference> goodRefs = new ArrayList<>();
      if (supportsAcl)
      {
         //If ACLs are supported then deal with the ACL load errors too
         for (int i = 0; i < allData.length; i++)
         {
            if (!(allData[i] instanceof Throwable)
               && aclData[i] instanceof Throwable)
            {
               allData[i] = aclData[i];
            }
            if(!(allData[i] instanceof Throwable))
            {
               goodData.add(allData[i]);
               goodAclData.add(aclData[i]);
               goodRefs.add(source[i]);
            }
         }
      }
      
      Object[] goodObjects = goodData.toArray(new Object[goodData.size()]);

      IPSCmsModelProxy proxy = getProxy();
      List<Object> proxyResults = new ArrayList<>();
      IPSReference[] refs;
      if (goodObjects.length > 0)
      {
         Object[] exResults;
         try
         {
            refs = proxy.create(goodObjects, names, proxyResults);
         }
         catch (PSMultiOperationException e)
         {
            if(!supportsAcl)
               throw e;
            ex = e;
            exResults = e.getResults();
            List<IPSReference> newRefs = new ArrayList<>();
            for (Object obj : exResults) {
               if (!(obj instanceof Throwable))
                  newRefs.add((IPSReference) obj);
            }
            for (int i = exResults.length-1; i>=0; i--)
            {
               Object obj = exResults[i];
               if (obj instanceof Throwable)
               {
                  goodRefs.remove(i);
                  goodAclData.remove(i);
                  for (int j = allData.length - 1; j >= 0; j--)
                  {
                     if (!(allData[j] instanceof Throwable))
                     {
                        allData[j] = obj;
                     }
                  }
               }
            }
            refs = newRefs.toArray(new IPSReference[0]);
         }
         if(supportsAcl)
         {
            List<IPSReference> newRefs = new ArrayList<>();
            newRefs.addAll(Arrays.asList(refs));

            try
            {
               createAndModifyAcls(goodAclData, refs, goodRefs);
            }
            catch (PSMultiOperationException e)
            {
               exResults = e.getResults();
               for (int i = exResults.length-1; i>=0; i--)
               {
                  Object obj = exResults[i];
                  if (obj instanceof Throwable)
                  {
                     newRefs.remove(i);
                     goodRefs.remove(i);
                     for (int j = allData.length - 1; j >= 0; j--)
                     {
                        if (!(allData[j] instanceof Throwable))
                        {
                           allData[j] = obj;
                        }
                     }
                  }
               }
            }
            refs = newRefs.toArray(new IPSReference[0]);
         }
      }
      else
         refs = new IPSReference[0];

      /*
       * need to verify that names are unique since there could be a conflict w/
       * an object that hasn't been persisted yet
       */
      try
      {
         int i = 0;
         Collection<IPSReference> currentRefs = catalog(false);
         for (IPSReference ref : refs)
         {
            currentRefs.remove(ref);
            Collection<IPSReference> comps = new ArrayList<>();
            comps.addAll(currentRefs);
            comps.addAll(Arrays.asList(refs));
            comps.remove(ref);
            Collection<String> existingNames = new ArrayList<>();
            for (IPSReference existingRef : comps)
            {
               existingNames.add(existingRef.getName());
            }
            
            //don't compare against self
            currentRefs.remove(ref);
            for (IPSReference testRef : comps)
            {
               if (ref.getName().equalsIgnoreCase(testRef.getName()))
               {
                  proxy.renameLocal(ref, 
                     PSCoreUtils.createCopyName(source[i].getName(), -1,
                        existingNames), proxyResults.get(i));
               }
            }
            i++;
         }
      }
      catch (PSModelException e)
      {
         //ignore, catch any issues on the save
      }
      
      // add to cache
      objectsCreated(refs, proxyResults);
      
      if (ex != null)
      {
         //need to throw an exception
         for (int i=0, j=0; i < allData.length; i++)
         {
            if (!(allData[i] instanceof Throwable))
               allData[i] = refs[j++];
         }
         throw new PSMultiOperationException(allData, names);
      }
      return refs;
   }

   /**
    * This method synchronizes the acls for the supplied new object references
    * with the supplied acl data of the old object references provided. If the
    * requesting user does not have full access to any of the updated object 
    * acls, a new entry for that user with full access will be added.
    * 
    * @param oldAclData the acl data for the old object references to which 
    *    the acl data of the new object references will be synchronized, 
    *    assumed not <code>null</code> or empty and of the same size as the 
    *    supplied new and old object references. This method takes ownership
    *    and may modify the supplied list.
    * @param refs a list of references to the new objects for which to 
    *    synchronize the acl data with the supplied acl data, assumed not
    *    <code>null</code> or empty and of the same size as the supplied
    *    old acl data and old object references.
    * @param oldRefs a list of references to the old objects from which to
    *    synchronize the acl data of the supplied new object references, 
    *    assumed not <code>null</code> or empty and of the same size as the 
    *    supplied old acl data and new object references. This method takes 
    *    ownership and may modify the supplied list.
    * @throws PSMultiOperationException if creating acls for the new objects
    *    fails on an individual object basis.
    * @throws PSModelException if creating acls fails for more fatal reasons.
    */
   private void createAndModifyAcls(List<Object> oldAclData,
      IPSReference[] refs, List<IPSReference> oldRefs)
      throws PSMultiOperationException, PSModelException
   {
      PSMultiOperationException ex = null;
      
      /*
       * Loading the acls from the new objects will create the defaults if
       * no acls exist yet.
       */
      List<Object> newAcls = new ArrayList<Object>();
      try
      {
         newAcls = Arrays.asList(loadAcl(refs, true));
      }
      catch (PSMultiOperationException e)
      {
         ex = e;
         
         /*
          * Weed out all failed acls and synchronize the oldRefs and oldAclData
          * to continue with the ones successfully loaded.
          */
         int index = 0;
         for (Object result : e.getResults())
         {
            if (result instanceof IPSAcl)
            {
               newAcls.add(result);
               index++;
            }
            else
            {
               oldRefs.remove(index);
               oldAclData.remove(index);
            }
         }
      }
      
      try
      {
         /*
          * Walk all new acls and synchronize them with the old acls.
          */
         for (int i=0; i<newAcls.size(); i++)
         {
            IPSAcl newAcl = (IPSAcl) newAcls.get(i);
            IPSAcl oldAcl = (IPSAcl) oldAclData.get(i);
   
            /*
             * Walk all old acl entries and add the missing entries to the new acl.
             * We can't update the permissions in the same loop because we may
             * end up with no acl owner, which is not allowed.
             */
            Enumeration entries = oldAcl.entries();
            while (entries.hasMoreElements())
            {
               IPSAclEntry entry = (IPSAclEntry) entries.nextElement();
               IPSAclEntry newEntry = newAcl.findEntry(
                  (IPSTypedPrincipal) entry.getPrincipal());
               if (newEntry == null)
               {
                  newEntry = newAcl.createEntry(
                     (IPSTypedPrincipal) entry.getPrincipal());
                  try
                  {
                     newAcl.addEntry(newAcl.getFirstOwner(), newEntry);
                  }
                  catch (NotOwnerException e)
                  {
                     // should not happen
                  }
               }
               
               /*
                * Find or create the entry for the current user and give him
                * full access.
                */
               boolean hasFullAccess = hasFullAccess(oldRefs.get(i));
               if (!hasFullAccess)
               {
                  IPSAclEntry me = newAcl.findEntry(
                     PSCoreFactory.getInstance().getUserPrincipal());
                  if (me == null)
                  {
                     me = newAcl.createEntry(
                        PSCoreFactory.getInstance().getUserPrincipal());
                     try
                     {
                        newAcl.addEntry(newAcl.getFirstOwner(), me);
                     }
                     catch (NotOwnerException e)
                     {
                        // should not happen
                     }
                  }
                  
                  me.addPermissions(new PSPermissions[]
                  {
                     PSPermissions.READ,
                     PSPermissions.UPDATE,
                     PSPermissions.DELETE,
                     PSPermissions.OWNER
                  });
               }
            }
   
            /*
             * First add all permissions from the originals to the new entries.
             */ 
            entries = oldAcl.entries();
            while (entries.hasMoreElements())
            {
               IPSAclEntry entry = (IPSAclEntry) entries.nextElement();
               IPSAclEntry newEntry = newAcl.findEntry(
                  (IPSTypedPrincipal) entry.getPrincipal());
   
               for (PSPermissions perm : PSPermissions.values())
               {
                  if (entry.checkPermission(perm))
                     newEntry.addPermission(perm);
               }
            }
   
            /*
             * Now remove all permissions not set in the originals from the 
             * new entries.
             */ 
            entries = oldAcl.entries();
            while (entries.hasMoreElements())
            {
               IPSAclEntry entry = (IPSAclEntry) entries.nextElement();
               IPSAclEntry newEntry = newAcl.findEntry(
                  (IPSTypedPrincipal) entry.getPrincipal());
   
               for (PSPermissions perm : PSPermissions.values())
               {
                  if (!entry.checkPermission(perm))
                     newEntry.removePermission(perm);
               }
            }
         }
      }
      catch (SecurityException e)
      {
         /*
          * This should never happen. Wrap it in model exception to make sure
          * all UI actions show an error dialog if it does happen.
          */
         throw new PSModelException(e);
      }
      
      if (ex != null)
         throw ex;
   }

   /**
    * Check if the permission associated with this reference gives all
    * permissions defined in {@link PSPermissions} except
    * {@link PSPermissions#RUNTIME_VISIBLE}
    * 
    * @param reference assumed not <code>null</code> and has user permissions
    * associated with it.
    * @return <code>true</code> if the reference has all four permissions,
    * <code>false</code> otherwise.
    */
   private boolean hasFullAccess(IPSReference reference)
   {
      int[] perms = reference.getPermissions();
      if (perms == null)
         return false;

      for (PSPermissions required : PSPermissions.values())
      {
         if (required.getOrdinal() == PSPermissions.RUNTIME_VISIBLE
            .getOrdinal())
            continue;
         boolean found = false;
         for (int i : perms)
         {
            if(i==required.getOrdinal())
            {
               found = true;
               break;
            }
         }
         if(!found)
            return false;
      }
      return true;
   }

   // see interface
   public Object load(IPSReference ref, boolean lock, boolean overrideLock)
         throws Exception
   {
      try
      {
         Object[] objs = load(new IPSReference[] { ref }, lock, overrideLock);
         return objs[0];
      }
      catch (PSMultiOperationException e)
      {
         Exception ex = (Exception)e.getResults()[0];
         if(ex != null && (ex instanceof PSModelException))
            ((PSModelException)ex).setDetail(ref);
         throw (Exception) e.getResults()[0];
      }
   }

   // see interface
   public Object[] load(IPSReference[] refs, boolean lock, boolean overrideLock)
         throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(refs, "refs");
      if (refs.length == 0)
         return new IPSReference[0];

      boolean exceptionOccurred = false;
      List<Object> results = new ArrayList<>();
      Object[] moreResults;
      try
      {
         List<Object> query = new ArrayList<>();
         for (IPSReference ref : refs)
         {
            if ((lock && isLocked(ref) && isDataCached(ref, false))
                  || (!lock && isDataCached(ref, false)))
            {
               results.add(getDataFromCache(ref, false));
            }
            else
            {
               // add a placeholder for now, put in real object later
               results.add(null);
               query.add(ref);
            }
         }

         IPSCmsModelProxy proxy = getProxy();
         IPSReference[] unloadedRefs = new IPSReference[query.size()];
         query.toArray(unloadedRefs);
         if (log.isDebugEnabled())
         {
            log.debug("Loading " + (refs.length - unloadedRefs.length)
                  + " " + m_primaryObjectType + " from cache. lock=" + lock);
         }
         if (unloadedRefs.length > 0)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Loading " + unloadedRefs.length + " "
                     + m_primaryObjectType + " thru proxy. lock=" + lock);
            }
            moreResults = proxy.load(unloadedRefs, lock, overrideLock);
         }
         else
            moreResults = new Object[0];
         assert (moreResults.length == unloadedRefs.length);
      }
      catch (PSMultiOperationException e)
      {
         moreResults = e.getResults();
         exceptionOccurred = true;
      }

      for (int i = 0, j = 0; i < results.size(); i++)
      {
         Object result = results.get(i);
         if (result != null)
            continue;
         results.set(i, moreResults[j++]);
      }
      Object[] resultArray = results.toArray();

      if (getProxy().getMetaData().isCacheable())
         objectsLoaded(refs, resultArray, false);

      if (lock)
      {
         Collection<IPSReference> successfulRefs = new ArrayList<>();
         int i = 0;
         for (Object o : resultArray)
         {
            if (!(o instanceof Exception))
               successfulRefs.add(refs[i]);
            i++;
         }
         objectsLocked(successfulRefs.toArray(), false);
      }

      if (exceptionOccurred)
         throw new PSMultiOperationException(resultArray, refs);
      return resultArray;
   }

   // see interface
   public IPSReference save(IPSReference ref, boolean releaseLock)
         throws Exception
   {
      try
      {
         IPSReference[] refs = save(new IPSReference[] { ref }, releaseLock);
         return refs[0];
      }
      catch (PSMultiOperationException e)
      {
         throw (Exception) e.getResults()[0];
      }
   }

   // see interface
   public IPSReference[] save(IPSReference[] refs, boolean releaseLock)
         throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(refs, "refs");
      if (refs.length == 0)
         return new IPSReference[0];

      boolean errorOccurred = false;
      List<Object> targets = new ArrayList<>();
      List<IPSReference> targetRefs = new ArrayList<>();
      List<IPSReference> unpersistedRefs = new ArrayList<>();
      Object[] results = new Object[refs.length];
      System.arraycopy(refs, 0, results, 0, refs.length);
      for (int i = 0; i < refs.length; i++)
      {
         if (!refs[i].isPersisted() && getDataFromCache(refs[i], false) != null)
         {
            unpersistedRefs.add(refs[i]);
         }
         if (!isLocked(refs[i]))
         {
            errorOccurred = true;
            results[i] = new PSLockException("save", refs[i].getObjectType()
                  .getPrimaryType().name(), refs[i].getName());
         }
         else
         {
            targetRefs.add(refs[i]);
            targets.add(getDataFromCache(refs[i], false));
         }
      }

      if (targets.isEmpty())
      {
         // none of the supplied refs was locked
         throw new PSMultiOperationException(results, refs);
      }

      try
      {
         getProxy().save(targetRefs.toArray(new IPSReference[targetRefs.size()]),
               targets.toArray(), releaseLock);
         //Also save the ACLs for the newly persisted objects
         if (!unpersistedRefs.isEmpty())
         {
            try
            {
               IPSReference[] newAclRefs = unpersistedRefs
                     .toArray(new IPSReference[unpersistedRefs.size()]);
               loadAcl(newAclRefs, true);
               saveAcl(newAclRefs, true);
            }
            catch (UnsupportedOperationException ignore)
            {
               //this type doesn't support ACLs, so just ignore it
            }
         }
      }
      catch (PSMultiOperationException e)
      {
         Object[] errors = e.getResults();
         int errorsIndex = 0;
         for (int i = 0; i < results.length; i++)
         {
            if (!(results[i] instanceof Throwable))
            {
               if (errors[errorsIndex] != null)
               {
                  results[i] = errors[errorsIndex];
               }
               errorsIndex++;
            }
         }
         errorOccurred = true;
      }

      // fixup the locks
      if (releaseLock)
      {
         objectsUnlocked(results, true, false);
      }

      objectsSaved(results, false);

      if (errorOccurred)
         throw new PSMultiOperationException(results, refs);
      return refs;
   }

   // see interface
   public void delete(IPSReference ref) throws Exception
   {
      try
      {
         delete(new IPSReference[] { ref });
      }
      catch (PSMultiOperationException e)
      {
         throw (Exception) e.getResults()[0];
      }
   }

   // see interface
   public void delete(IPSReference[] refs)
      throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(refs, "refs");
      if (refs.length == 0)
         return;

      if (isHierarchyModel())
      {
         doHierarchicalDelete(refs);
         return;
      }

      try
      {
         getProxy().delete(refs);
         //Delete the ACLs for only the persisted objects
         //Model would not have saved ACLs for non-persisted objects
         List<IPSReference> persistedRefs = new ArrayList<>();
         for (IPSReference ref : refs) {
            if (ref.isPersisted())
               persistedRefs.add(ref);
         }
         // Transaction?? what if the object is deleted but the ACL was locked
         // for someone else.
         //fixme Should this be server responsibilty?
         try
         {
            if(!persistedRefs.isEmpty())
               getProxy().deleteAcl(persistedRefs.toArray(new IPSReference[0]));
         }
         catch(PSMultiOperationException e)
         {
            Object[] obj = e.getResults();
            for (Object o : obj) {
               if (o instanceof Exception)
                  log.error(o);
            }
         }
         objectsDeleted(refs);
      }
      catch (PSMultiOperationException e)
      {
         // clean up the ones that succeeded
         Object[] results = e.getResults();
         List<IPSReference> successes = new ArrayList<IPSReference>();
         for (int i = 0; i < results.length; i++)
         {
            if (results[i] == null)
               successes.add(refs[i]);
         }
         objectsDeleted(successes.toArray(new IPSReference[successes.size()]));
         throw e;
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.client.models.IPSCmsModel#deleteAcl(com.percussion.services.security.IPSAcl)
    */
   public void deleteAcl(IPSReference owner) throws Exception
   {
      try
      {
         deleteAcl(new IPSReference[] { owner });
      }
      catch (PSMultiOperationException e)
      {
         throw (Exception) e.getResults()[0];
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.models.IPSCmsModel#deleteAcl(com.percussion.services.security.IPSAcl[])
    */
   public void deleteAcl(IPSReference[] owners)
      throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(owners, "owners");
      if (owners.length == 0)
         return;

      try
      {
         getProxy().deleteAcl(owners);
         // Do we need events?
         // objectsDeleted(acls);
      }
      catch (PSMultiOperationException e)
      {
         // clean up the ones that succeeded
         Object[] results = e.getResults();
         List<IPSReference> successes = new ArrayList<IPSReference>();
         for (int i = 0; i < results.length; i++)
         {
            if (results[i] == null)
               successes.add(owners[i]);
         }
         // Do we need events?
         // objectsDeleted((IPSReference[]) successes
         // .toArray(new IPSReference[successes.size()]));
         throw e;
      }
   }

   /**
    * Casts the supplied refs to {@link IPSHierarchyNodeRef}s, groups them by
    * their owning manager and passes each group to the manager for deletion.
    * All errors are collected and returned properly, even if the refs span
    * multiple managers.
    * 
    * @param refs Assumed not <code>null</code>.
    * 
    * @throws PSMultiOperationException If any object cannot be deleted for any
    * reason. The results contain <code>null</code>s for successes and the
    * appropriate exception for failures.
    */
   private void doHierarchicalDelete(IPSReference[] refs)
      throws PSMultiOperationException, PSModelException
   {
      Map<IPSHierarchyManager, List<IPSHierarchyNodeRef>> mgrs = 
         new HashMap<>();
      
      //group refs with their mgr
      for (IPSReference ref : refs)
      {
         IPSHierarchyNodeRef node = (IPSHierarchyNodeRef) ref;
         IPSHierarchyManager mgr = getHierarchyManager(node);
         List<IPSHierarchyNodeRef> nodes = mgrs.get(mgr); 
         if (nodes == null)
         {
            nodes = new ArrayList<>();
            mgrs.put(mgr, nodes);
         }
         nodes.add(node);
      }
      
      //do delete for each mgr
      Object[] results = null;
      for (Map.Entry<IPSHierarchyManager, List<IPSHierarchyNodeRef>> entry : mgrs
            .entrySet())
      {
         List<IPSHierarchyNodeRef> values = entry.getValue();
         try
         {
            entry.getKey().removeChildren(values);
         }
         catch (PSMultiOperationException e)
         {
            if (results == null)
               results = new Object[refs.length];
            Object[] partialResults = e.getResults();
            for (int i = 0; i < partialResults.length; i++)
            {
               if (partialResults[i] == null)
                  continue;
               IPSHierarchyNodeRef failedNode = values.get(i);
               for (int j = 0; j < refs.length; j++)
               {
                  if (refs[j].equals(failedNode))
                     results[j] = partialResults[i];
               }
            }
         }
      }
      if (results != null)
         throw new PSMultiOperationException(results, refs);
   }
   
   // see interface
   public void releaseLock(IPSReference ref) throws Exception
   {
      try
      {
         releaseLock(new IPSReference[] { ref });
      }
      catch (PSMultiOperationException e)
      {
         throw (Exception) e.getResults()[0];
      }
   }

   // see interface
   public void releaseLock(IPSReference[] refs)
         throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(refs, "refs");
      if (refs.length == 0)
         return;

      Object[] results = new Object[refs.length];
      System.arraycopy(refs, 0, results, 0, refs.length);
      try
      {
            getProxy().releaseLock(refs);
      }
      catch (PSMultiOperationException e)
      {
         // clean up the ones that succeeded
         Object[] persistedResults = e.getResults();
         for (int i=0; i < persistedResults.length; i++)
         {
            if (persistedResults[i] instanceof Throwable)
               results[i] = persistedResults[i];
         }
         throw new PSMultiOperationException(results, refs);
      }
      finally
      {
         objectsUnlocked(results, false, false);
      }
   }

   // see interface
   public boolean isLockedInThisSession(IPSReference ref)
   {
      return checkLocked(ref, true, false);
   }

   // see interface
   public Collection<IPSReference> getLockedRefs()
   {
      Collection<IPSReference> results = new ArrayList<>();
      for (ManagerInfo mgrInfo : m_mgrInfos.values())
      {
         Collection<DataInfo> cache = mgrInfo.m_cache;
         if (cache == null)
            continue;
         for (DataInfo dInfo : cache)
         {
            if (dInfo.m_locked)
            {
               assert (dInfo.m_ref != null);
               results.add(dInfo.m_ref);
            }
         }
      }
      return results;
   }

   // see interface
   public boolean isLocked(IPSReference ref)
   {
      return checkLocked(ref, false, false);
   }

   // see interface
   public boolean isAclLocked(IPSReference owner)
   {
      return checkLocked(owner, false, true);
   }

   // see interface
   public Set<PSObjectType> getObjectTypes()
   {
      return m_primaryObjectType.getTypes();
   }

   //see interface
   public Collection<IPSReference> catalog() 
      throws PSModelException
   {
      return catalog(false, (PSObjectType[]) null);
   }

   //see interface
   public Collection<IPSReference> catalog(boolean forceRefresh) 
      throws PSModelException
   {
      return catalog(forceRefresh, (PSObjectType[]) null);
   }
   
   //see interface
   public Collection<IPSReference> catalog(final boolean forceRefresh, 
         final PSObjectType filter)
      throws PSModelException
   {
      if (filter != null)
         return catalog(forceRefresh, new PSObjectType[] {filter});
      return catalog(forceRefresh, (PSObjectType[]) null);         
   }
   
   // see interface
   public Collection<IPSReference> catalog(final boolean forceRefresh, 
         final PSObjectType[] filters)
      throws PSModelException
   {
      if (forceRefresh)
         flush(null);
      return doCatalog(null, filters, new IProxyWrapper()
      {
         @SuppressWarnings("unused") //no parents in flat model
         public Collection<IPSReference> catalog(IPSReference parent)
            throws PSModelException
         {
            return getProxy().catalog();
         }

         /**
          * The tree name for a flat model is just the empty string.
          */
         public String getTreeName()
         {
            return FLAT_MODEL_TREENAME;  
         }
      });
   }
   
   /**
    * Simple interface so the cataloger for the hierarchy proxy and the regular
    * proxy can both be passed to the
    * {@link #doCatalog(IPSReference, PSObjectType[], IProxyWrapper)} method.
    */
   interface IProxyWrapper
   {
       Collection<IPSReference> catalog(IPSReference parent)
         throws PSProxyException, PSModelException;
      
       String getTreeName();
   }
   
   /**
    * This method is shared by the flat and hierarchical catalogers.
    * 
    * @param parent May be <code>null</code> to get just the root, or flat
    * level children.
    * 
    * @param filters If not <code>null</code>, the results are limited to
    * objects of this type.
    * 
    * @return Never <code>null</code>, may be empty.
    * 
    * @throws PSModelException If the items can't be obtained from cache and a
    * problem occurs communicating with the server.
    */
   Collection<IPSReference> doCatalog(IPSReference parent,
         final PSObjectType[] filters, IProxyWrapper wrapper)
      throws PSModelException
   {
      if (!isCataloged(wrapper.getTreeName(), parent))
      {
         try
         {
            Collection<IPSReference> catalogedRefs = wrapper.catalog(parent);
            if (!getProxy().getMetaData().isCacheable())
               return catalogedRefs;
            if (parent == null)
            {
               Collection<IPSReference> cachedRefs = getCachedRefs(wrapper
                     .getTreeName(), parent);
               for (IPSReference catalogedEntry : catalogedRefs)
               {
                  boolean found = false;
                  for (IPSReference existingEntry : cachedRefs)
                  {
                     //skip entries that were left because they were open for edit
                     if (existingEntry.equals(catalogedEntry))
                     {
                        found = true;
                        break;
                     }
                  }
                  if (!found)
                     getInfo(wrapper.getTreeName(), catalogedEntry, true);
               }
               getManagerInfo(wrapper.getTreeName()).m_cataloged = true;
            }
            else
            {
               DataInfo info = getInfo(wrapper.getTreeName(), parent, true);
               if (info.m_children == null)
                  info.m_children = new ArrayList<>();
               for (IPSReference catalogedEntry : catalogedRefs)
               {
                  boolean found = false;
                  for (IPSReference existingEntry : info.m_children)
                  {
                     //skip entries that were left because they were open for edit
                     if (existingEntry.equals(catalogedEntry))
                     {
                        found = true;
                        break;
                     }
                  }
                  if (!found)
                  {
                     info.m_children.add(catalogedEntry);
                     //all containers must be in the first level cache as well
                     if (((IPSHierarchyNodeRef)catalogedEntry).isContainer())
                        getInfo(wrapper.getTreeName(), catalogedEntry, true);
                  }
               }
               info.m_cataloged = true;
            }
         }
         catch (PSProxyException e)
         {
            throw new PSModelException(e);
         }
      }

      assert (getProxy().getMetaData().isCacheable());
      Collection<IPSReference> results = getCachedRefs(wrapper.getTreeName(),
            parent);
      if (filters != null)
      {
         for (Iterator<IPSReference> iter = results.iterator(); iter
               .hasNext();)
         {
            PSObjectType refType = iter.next().getObjectType();
            boolean typeFound = false;
            for (PSObjectType type : filters)
            {
               if (refType.equals(type))
               {
                  typeFound = true;
                  break;
               }
            }
            if (!typeFound)
               iter.remove();
         }
      }
      return results;
   }

   /**
    * Checks if the supplied reference has been cataloged since the last flush
    * operation.
    * 
    * @param parent The container to check. May be <code>null</code>.
    * 
    * @return <code>true</code> if it has, <code>false</code> otherwise.
    */
   private boolean isCataloged(String treeName, IPSReference parent)
   {
      if (!getProxy().getMetaData().isCacheable())
         return false;
      
      ManagerInfo mgrInfo = getManagerInfo(treeName);
      if (!isHierarchyModel() || parent == null)
         return mgrInfo.m_cataloged;
      
      for (DataInfo info : mgrInfo.m_cache)
      {
         if (parent.equals(info.m_ref))
         {
            return info.m_cataloged;
         }
      }
      return false;
   }

   // see interface
   public IPSReference getReference(IPSGuid id)
      throws PSModelException
   {
      if (null == id)
      {
         throw new IllegalArgumentException("id cannot be null");  
      }

      isHierarchyModel(true);
      Collection<IPSReference> catalogedRefs = catalog(false);
      for (IPSReference ref : catalogedRefs)
      {
         if (ref.getId() != null && ref.getId().equals(id))
         {
            return ref;
         }
      }
      return null;
   }

   // see reference
   public IPSReference getReference(String name) throws PSModelException
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name cannot be blank");  
      }

      isHierarchyModel(true);
      Collection<IPSReference> catalogedRefs = catalog(false);
      for (IPSReference ref : catalogedRefs)
      {
         if (ref.getName().equals(name))
         {
            return ref;
         }
      }
      return null;
   }

   /**
    * Checks if this model is based on a hierarchy by attempting to load the
    * hierarchy proxy.
    * 
    * @param throwIfTrue If <code>true</code> and this is a hierarchy, an
    * <code>UnsupportedOperationException</code> will be thrown.
    * 
    * @return If <code>throwIfTrue</code> is <code>false</code>,
    * <code>true</code> is returned if this model has a hierarchy manager,
    * <code>false</code> otherwise.
    */
   private boolean isHierarchyModel(boolean throwIfTrue)
   {
      boolean isHierarchy = false;
      
      try
      {
         isHierarchy = null != PSCoreFactory.getInstance()
               .getHierarchyModelProxy((Enum) m_primaryObjectType);
      }
      catch (PSModelException e)
      {
         //shouldn't happen unless system incorrectly configured
         throw new RuntimeException(e);
      }
      
      if (throwIfTrue && isHierarchy)
      {
         throw new UnsupportedOperationException("Must use hierarchy manager.");
      }
      return isHierarchy;
   }

   // see interface
   public void addListener(IPSModelListener listener, int notifications)
   {
      if (null == listener)
      {
         throw new IllegalArgumentException("listener cannot be null");
      }
      ModelEvents[] values = ModelEvents.values();
      int possibleNotifications = 0;
      for (int i = 0; i < values.length; i++)
      {
         possibleNotifications |= values[i].getFlag();
      }
      if ((notifications & possibleNotifications) == 0)
      {
         throw new IllegalArgumentException(
               "at least 1 notification must be supplied");
      }

      m_listeners.put(listener, notifications
              & possibleNotifications);
   }

   // see interface
   public void removeListener(IPSModelListener listener)
   {
      m_listeners.remove(listener);
   }

   // see interface
   public void propertyChanged(IPSReference ref, Map<String, String> hint)
      throws PSLockException
   {
      if (!isLockedInThisSession(ref))
      {
         throw new PSLockException("propChange", ref.getObjectType().toString(),
               ref.getName());
      }
      PSModelChangedEvent event = new PSModelChangedEvent(ref,
            ModelEvents.MODIFIED, hint);
      notifyListeners(event);
   }

   // see interface
   public boolean isHierarchyModel()
   {
      try
      {
         return null != PSCoreFactory.getInstance().getHierarchyModelProxy(
               (Enum) m_primaryObjectType);
      }
      catch (PSModelException e)
      {
         //mis-configuration
         throw new RuntimeException(e);
      }
   }

   //see interface
   public Collection<String> getHierarchyTreeNames()
   {
      try
      {
         if (!isHierarchyModel())
            throw new UnsupportedOperationException("Must be hierarchy model.");
         
         return PSCoreFactory.getInstance().getHierarchyModelProxy(
               (Enum) m_primaryObjectType).getRoots();
      }
      catch (PSModelException e)
      {
         // should never happen as we checked if this was a hierarchy model
         throw new RuntimeException(e);
      }
   }

   // see interface
   public IPSHierarchyManager getHierarchyManager(String treeName)
   {
      ManagerInfo info = getManagerInfo(treeName);
      try
      {
         IPSHierarchyManager mgr = info.m_hierarchyManager;
         if (mgr == null)
         {
            mgr = new PSHierarchyManager(this, treeName);
            info.m_hierarchyManager = mgr;
         }
         return mgr;
      }
      catch (PSModelException e)
      {
         // should not happen unless system is badly configured
         throw new RuntimeException(e);
      }
   }

   // see interface
   public IPSHierarchyManager getHierarchyManager(IPSReference node)
   {
      if ( !(node instanceof IPSHierarchyNodeRef))
      {
         throw new IllegalArgumentException(
               "node cannot be null and must be instanceof IPSHierarchyNodeRef");  
      }
      return ((IPSHierarchyNodeRef) node).getManager();
   }

   // see interface
   public void flush(final IPSReference ref)
   {
      processAllCaches(new CacheProcessor()
      {
         public Object process(ManagerInfo mgrInfo)
         {
            mgrInfo.m_cataloged = false;
            if ((getProxy().getMetaData().isCacheable() ? false : mgrInfo.m_cache.size() != 0)) {
               throw new AssertionError();
            }
            if (mgrInfo.m_cache.isEmpty())
               return null;
            recursiveFlush(mgrInfo.m_cache, ref);

            // flush all proxies
            getProxy().flush(ref);
            
            return null;
         }
      });
   }

   /**
    * This is used to perform a top down scan of the 'tree' to remove all nodes
    * that don't have a descendent that is locked. If it is not a hierarchy,
    * just the root level nodes are processed.
    * 
    * @param node May be <code>null</code> for the first call.
    * 
    * @return <code>false</code> if this path cannot be removed because some
    * descendent has a locked node, <code>true</code> to keep deleting.
    */
   private boolean recursiveFlush(Collection<DataInfo> cache, IPSReference node)
   {
      if (!isHierarchyModel())
      {
         Collection<IPSReference> toRemove = new ArrayList<>();
         for (DataInfo info : cache)
         {
            if (!info.m_locked)
               toRemove.add(info.m_ref);
            else
               info.m_cataloged = false;
         }
         for (IPSReference ref : toRemove)
            removeFromCache(ref);
         return true;
      }
      
      // build list of all children
      Set<IPSReference> children = new HashSet<>();
      for (DataInfo info : cache)
      {
         IPSHierarchyNodeRef parent = ((IPSHierarchyNodeRef) info.m_ref)
               .getParent(); 
         if ((parent == null && node == null) || (parent != null
               && parent.equals(node)))
         {
            children.add(info.m_ref);
         }
      }

      boolean stopFlush = false;
      for (IPSReference ref : children)
      {
         stopFlush = stopFlush || !recursiveFlush(cache, ref);
      }

      if (children.size() == 0)
      {
         if (isRefCached(node) && !getInfo(node).m_locked)
         {
            removeFromCache(node);
            return true;
         }
         else if (isRefCached(node))
            getInfo(node).m_cataloged = false;
         return false;
      }

      if (!stopFlush && null != node && isRefCached(node)
            && !getInfo(node).m_locked)
      {
         removeFromCache(node);
         return true;
      }
      else if (node != null && isRefCached(node))
         getInfo(node).m_cataloged = false;

      return false;
   }

   // see interface
   public Object loadAcl(IPSReference owner, boolean lock)
      throws PSModelException
   {
      try
      {
         Object[] objs = loadAcl(new IPSReference[]
         {
            owner
         }, lock);
         return objs[0];
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException((Exception) e.getResults()[0]);
      }
   }

   // see interface
   public Object[] loadAcl(IPSReference[] owners, boolean lock)
      throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(owners, "owners");
      if (owners.length == 0)
         return new IPSAcl[0];

      boolean exceptionOccurred = false;
      List<Object> results = new ArrayList<>();
      Object[] loadedResults = new Object[0];
      IPSReference[] refsToLoadArray = null;
      try
      {
         List<Object> refsToLoad = new ArrayList<>();
         for (IPSReference owner : owners)
         {
            if ((lock && isAclLocked(owner)) || (!lock && isDataCached(owner, true)))
               results.add(getDataFromCache(owner, true));
            else
            {
               // add a placeholder for now, put in real object later
                  results.add(null);
                  refsToLoad.add(owner);
            }
         }
         
         IPSCmsModelProxy proxy = getProxy();
         if (refsToLoad.size() > 0)
         {
            refsToLoadArray = refsToLoad.toArray(new IPSReference[0]);
            loadedResults = proxy.loadAcl(refsToLoadArray, lock);
         }
         else
            loadedResults = new Object[0];
         assert (loadedResults.length == refsToLoad.size());
      }
      catch (PSMultiOperationException e)
      {
         loadedResults = e.getResults();
         exceptionOccurred = true;
      }

      for (int i = 0, j = 0; i < results.size() && j < loadedResults.length; i++)
      {
         Object result = results.get(i);
         if (result != null)
            continue;
         results.set(i, loadedResults[j++]);
      }
      Object[] resultArray = results.toArray();

      objectsLoaded(owners, resultArray, true);

      if (lock)
      {
         Collection<IPSReference> successfulRefs = new ArrayList<>();
         int i = 0;
         for (Object o : resultArray)
         {
            if (!(o instanceof Exception))
               successfulRefs.add(owners[i]);
            i++;
         }
         objectsLocked(successfulRefs.toArray(), true);
      }

      if (exceptionOccurred)
         throw new PSMultiOperationException(resultArray, owners);
      return resultArray;
   }

   // see interface
   public void releaseAclLock(IPSReference owner) throws PSModelException
   {
      try
      {
         releaseAclLock(new IPSReference[]
         {
            owner
         });
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException((Exception) e.getResults()[0]);
      }
   }

   // see interface
   public void releaseAclLock(IPSReference[] refs)
      throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(refs, "refs");
      if (refs.length == 0)
         return;

      Object[] results = new Object[refs.length];
      System.arraycopy(refs, 0, results, 0, refs.length);
      List<Long> aclIds = null;
      try
      {
         aclIds = new ArrayList<>();
         for (IPSReference ref : refs) {
            //If the object is not persisted yet, dont release the ACL
            if (!ref.isPersisted())
               continue;
            IPSAcl acl = (IPSAcl) getDataFromCache(ref, true);
            // ACL is not in the cache, mostly the ACL is not locked or type
            // does not support ACLs
            if (acl != null)
               aclIds.add(acl.getId());
         }
         if(aclIds.isEmpty())
            return;
         getProxy().releaseAclLock(aclIds.toArray(new Long[0]));
      }
      catch (PSMultiOperationException e)
      {
         // clean up the ones that succeeded
         Object[] persistedResults = e.getResults();
         for (int i=0; i < persistedResults.length; i++)
         {
            if (persistedResults[i] instanceof Throwable)
               results[i] = persistedResults[i];
         }
         throw new PSMultiOperationException(results, refs);
      }
      finally
      {
         objectsUnlocked(results, false, true);
      }
   }

   // see interface
   public void saveAcl(IPSReference owner, boolean releaseLock) throws PSModelException
   {
      try
      {
         saveAcl(new IPSReference[] { owner }, releaseLock);
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException((Exception)e.getResults()[0]);
      }
   }

   // see interface
   public void saveAcl(IPSReference[] owners, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      PSCoreUtils.checkArray(owners, "owners");
      if (owners.length == 0)
         return;

      boolean errorOccurred = false;
      List<IPSReference> targetRefs = new ArrayList<>();
      List<IPSAcl> targets = new ArrayList<>();
      Object[] results = new Object[owners.length];
      System.arraycopy(owners, 0, results, 0, owners.length);
      for (int i = 0; i < owners.length; i++)
      {
         if (!isAclLocked(owners[i]))
         {
            errorOccurred = true;
            results[i] = new PSLockException("saveAcl", owners[i]
               .getObjectType().getPrimaryType().name(), owners[i].getName());
         }
         else
         {
            Object acl = getDataFromCache(owners[i], true);
            if (owners[i].isPersisted() && acl != null)
            {
               targetRefs.add(owners[i]);
               targets.add((IPSAcl) getDataFromCache(owners[i], true));
            }
         }
      }

      if (targets.size() == 0)
      {
         return;
      }

      try
      {
         getProxy().saveAcl(targetRefs.toArray(new IPSReference[0]),
            targets.toArray(new IPSAcl[0]), releaseLock);
      }
      catch (PSMultiOperationException e)
      {
         Object[] errors = e.getResults();
         int errorsIndex = 0;
         for (int i = 0; i < results.length; i++)
         {
            if (!(results[i] instanceof Throwable))
            {
               if (errors[errorsIndex] != null)
               {
                  results[i] = errors[errorsIndex];
               }
               errorsIndex++;
            }
         }
         errorOccurred = true;
      }

      objectsSaved(results, true);

      // fixup the locks
      if (releaseLock)
      {
         objectsUnlocked(results, true, true);
      }

      if (errorOccurred)
         throw new PSMultiOperationException(results, owners);
   }

   /**
    * Sends the <code>event</code> to all listeners registered to receive
    * events of this type.
    */
   void notifyListeners(PSModelChangedEvent event)
   {
      /*
       * make a copy of the listener map to prevent possible concurrent
       * modification exceptions
       */
      Map<IPSModelListener, Integer> listeners = 
         new HashMap<>(m_listeners);
      for (IPSModelListener listener : listeners.keySet())
      {
         Integer notifications = listeners.get(listener);
         if ((notifications & event.getEventType().getFlag()) > 0)
            listener.modelChanged(event);
      }
   }

   /**
    * Performs notification and any other necessary operations when a design
    * object has been locked for editing.
    * 
    * @param refs Assumed not <code>null</code>. <code>null</code> entries
    * or entries of type <code>Throwable</code> will be skipped. All other
    * entries assumed to be <code>IPSReference</code>.
    */
   private void objectsLocked(Object[] refs, boolean isAcl)
   {
      Collection<IPSReference> valid = new ArrayList<IPSReference>();
      for (Object obj : refs)
      {
         if (obj != null && !(obj instanceof Throwable))
         {
            IPSReference ref = (IPSReference) obj;
            addToLockList(ref, isAcl);
            valid.add(ref);
         }
      }
      ModelEvents e = ModelEvents.LOCKED;
      if (isAcl)
         e = ModelEvents.ACL_LOCKED;

      PSModelChangedEvent event = new PSModelChangedEvent(valid
            .toArray(new IPSReference[valid.size()]), e, null);
      notifyListeners(event);
   }

   /**
    * Performs the cleanup and notification operations necessary when a design
    * object has been released from editing.
    * 
    * @param refs Assumed not <code>null</code>. <code>null</code> entries
    * or entries of type <code>Throwable</code> will be skipped. All other
    * entries assumed to be <code>IPSReference</code>.
    * 
    * @params withSave If <code>false</code>, the associated data is removed
    * from the cache, otherwise, it is left in the cache.
    */
   private void objectsUnlocked(Object[] refs, boolean withSave, boolean isAcl)
   {
      Collection<IPSReference> unlockedSet = new ArrayList<>();
      Collection<IPSReference> modifiedSet = new ArrayList<>();
      Collection<IPSReference> neverPersistedSet = new ArrayList<>();
      for (Object obj : refs)
      {
         if ((obj instanceof IPSReference))
         {
            IPSReference ref = (IPSReference) obj;
            if (ref.isPersisted())
            {
               removeFromLockList(ref, isAcl);
               if (!withSave)
                  removeDataFromCache(ref, false);
               modifiedSet.add(ref);
            }
            else if(!isAcl)
            {
               removeFromCache(ref);
               neverPersistedSet.add(ref);
            }
            unlockedSet.add(ref);
         }
      }
      ModelEvents e = ModelEvents.UNLOCKED;
      if(isAcl)
         e = ModelEvents.ACL_UNLOCKED;  

      PSModelChangedEvent event = new PSModelChangedEvent(unlockedSet
            .toArray(new IPSReference[unlockedSet.size()]), e, null);
      notifyListeners(event);

      /*
       *  if the object was unlocked and never persisted, this is equivalent 
       *  to a delete
       */
      if (neverPersistedSet.size() > 0)
      {
         event = new PSModelChangedEvent(neverPersistedSet
               .toArray(new IPSReference[neverPersistedSet.size()]),
               ModelEvents.DELETED, null);
         notifyListeners(event);
      }

      // otherwise notify of possible change in case unlocked w/o save
      if (!isAcl && modifiedSet.size() > 0)
      {
         event = new PSModelChangedEvent(modifiedSet
               .toArray(new IPSReference[modifiedSet.size()]),
               ModelEvents.MODIFIED, null);
         notifyListeners(event);
      }
   }

   /**
    * Performs the caching and notification operations necessary when a design
    * object has been modified.
    * 
    * @param refs the saved references, not <code>null</code>, may be empty. 
    *    <code>null</code> entries or entries of type <code>Throwable</code> 
    *    will be skipped. All other entries must be of type 
    *    <code>IPSReference</code>.
    */
   protected void objectsSaved(Object[] refs, boolean isAcl)
   {
      if (refs == null)
         throw new IllegalArgumentException("refs cannot be null");
      
      Collection<IPSReference> valid = new ArrayList<>();
      for (Object obj : refs)
      {
         if (obj == null || (obj instanceof Throwable))
            continue;
         
         if (!(obj instanceof IPSReference))
            throw new IllegalArgumentException(
               "invalid ref, must be of type IPSReference");
            
         IPSReference ref = (IPSReference) obj;
         valid.add(ref);
      }
      
      if (!valid.isEmpty())
      {
         final ModelEvents e = isAcl ? ModelEvents.ACL_SAVED
            : ModelEvents.SAVED;

         PSModelChangedEvent event = new PSModelChangedEvent(valid
               .toArray(new IPSReference[valid.size()]), e, null);
         notifyListeners(event);
      }
   }

   /**
    * Just like {@link #objectsCreated(IPSReference[], List)}, except the
    * objects are not marked as locked (it's assumed they have already been
    * persisted.)
    */
   void objectsCloned(IPSReference[] refs, List<Object> data)
   {
      if ( null == refs)
      {
         throw new IllegalArgumentException("refs cannot be null");  
      }
      
      if (refs.length == 0)
         return;
      
      Collection<IPSReference> validRefs = new ArrayList<>();
      for (int i = 0; i < refs.length; i++)
      {
         if (refs[i] != null)
         {
            updateCache(refs[i], data.get(i));
            validRefs.add(refs[i]);
         }
      }

      if (validRefs.size() > 0)
      {
         PSModelChangedEvent event = new PSModelChangedEvent(validRefs
               .toArray(new IPSReference[validRefs.size()]),
               ModelEvents.CREATED, null);
         notifyListeners(event);
      }
   }

   /**
    * Performs the caching and notification operations necessary when a design
    * object has been created. If the ref has not been persisted, a lock is
    * activated for it.
    * 
    * @param refs Never <code>null</code>. May contain <code>null</code>
    * entries, which are skipped. If empty, returns immediately.
    * 
    * @param data Assumed not <code>null</code> and no <code>null</code>
    * entries. The order of entries in this list is expected to correlate with
    * the order of entries in <code>refs</code>.
    */
   void objectsCreated(IPSReference[] refs, List<Object> data)
   {
      objectsCloned(refs, data);
      Collection<IPSReference> notPersistedRefs = new ArrayList<>();
      for (IPSReference ref : refs)
      {
         if (!ref.isPersisted())
            notPersistedRefs.add(ref);
      }
         
      objectsLocked(notPersistedRefs.toArray(new IPSReference[notPersistedRefs
            .size()]), false);
   }

   /**
    * Performs all operations necessary when a design object has been loaded,
    * including adding it in the cache.
    * 
    * @param refs Assumed not <code>null</code> and that for every valid entry
    * in <code>data</code>, there is a valid entry at the same index in this
    * array.
    * 
    * @param data Assumed not <code>null</code>. <code>null</code> entries
    * or entries of type <code>Throwable</code> will be skipped. All other
    * entries assumed to be <code>IPSReference</code>.
    * @param isAcl 
    */
   private void objectsLoaded(IPSReference[] refs, Object[] data, boolean isAcl)
   {
      for (int i=0; i < data.length; i++)
      {
         if (data[i] != null && !(data[i] instanceof Throwable))
         {
            if (isAcl)
               getInfo(getTreeName(refs[i]), refs[i], true).m_acl = (IPSAcl) data[i];
            else
               getInfo(getTreeName(refs[i]), refs[i], true).m_data = data[i]; 
         }
      }
   }

   /**
    * Performs the cleanup and notification operations necessary when a design
    * object has been deleted.
    * 
    * @param refs Assumed not <code>null</code>. <code>null</code> entries
    * or entries of type <code>Throwable</code> will be skipped. All other
    * entries assumed to be <code>IPSReference</code>.
    */
   void objectsDeleted(Object[] refs)
   {
      Collection<IPSReference> valid = new ArrayList<>();
      for (Object obj : refs)
      {
         if ( (obj instanceof IPSReference))
         {
            IPSReference ref = (IPSReference) obj;
            removeFromCache(ref);
            valid.add(ref);
         }
      }
      PSModelChangedEvent event = new PSModelChangedEvent(
            valid.toArray(new IPSReference[valid.size()]),
            ModelEvents.DELETED, null);
      notifyListeners(event);
   }

   /**
    * Performs the cleanup and notification operations necessary when a design
    * object has been moved between folders.
    * 
    * @param source The original parent of the entries in <code>refs</code>.
    * May be <code>null</code> if coming from the root.
    * 
    * @param refs Never <code>null</code>. <code>null</code> entries
    * will be skipped.
    * 
    * @param target The new parent of the entries in <code>refs</code>. May be 
    * <code>null</code> to move to the root.
    */
   void objectsMoved(IPSHierarchyNodeRef source, IPSHierarchyNodeRef[] refs, 
         IPSHierarchyNodeRef target)
   {
      if (null == refs)
      {
         throw new IllegalArgumentException("refs cannot be null");  
      }
      
      Collection<IPSReference> valid = new ArrayList<>();
      for (IPSReference ref : refs)
      {
         if (ref != null)
         {
            valid.add(ref);
            moveInCache(source, ref, target);
         }
      }
      
      if (!valid.isEmpty())
      {
         IPSHierarchyNodeRef[] validRefArray = valid
               .toArray(new IPSHierarchyNodeRef[valid.size()]);
         PSModelChangedEvent event = new PSModelChangedEvent(source,
               validRefArray, ModelEvents.CHILDREN_REMOVED);
         notifyListeners(event);

         event = new PSModelChangedEvent(target, validRefArray,
               ModelEvents.CHILDREN_ADDED);
         notifyListeners(event);
      }
   }

   /**
    * Retrieve the proper proxy for this model.
    * 
    * @return Never <code>null</code>.
    */
   IPSCmsModelProxy getProxy()
   {
      try
      {
         return PSCoreFactory.getInstance().getCmsModelProxy(
               (Enum) m_primaryObjectType);
      }
      catch (PSModelException e)
      {
         /*
          * this should never happen until we support arbitrary registrations,
          * and then it should be rare
          */
         throw new RuntimeException(e);
      }
   }

   /**
    * Walks the cache and builds a collection of all refs that have the 
    * supplied ref as its parent.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   synchronized private Collection<IPSReference> getCachedRefs(
         final String treeName, final IPSReference parent)
   {
      ManagerInfo mgrInfo = getManagerInfo(treeName);
      Collection<IPSReference> results = new ArrayList<>();
      
      for (DataInfo info : mgrInfo.m_cache)
      {
         if (!isHierarchyModel())
         {
            results.add(info.m_ref);
         }
         else if (parent == null)
         {
            //either this is flat, or they want the root node
            if (((IPSHierarchyNodeRef) info.m_ref).getParent() == null)
               results.add(info.m_ref);
         }
         else if (parent.equals(info.m_ref))
         {
            if (info.m_children != null)
               results.addAll(info.m_children);
            break;
         }
      }
      return results;
   }

   /**
    * Removes the supplied object from the cache and locked list. If the object
    * is not in the cache, returns immediately. If it has children, they
    * are all removed from the cache recursively.
    * 
    * @param ref Assumed not <code>null</code>.
    */
   synchronized void removeFromCache(final IPSReference ref)
   {
      ManagerInfo mgrInfo = getManagerInfo(getTreeName(ref));
      DataInfo mainInfo = getInfo(mgrInfo.m_treeName, ref, false);

      /* should only happen if client gets a ref, flushes, gets same ref and 
       * changes name
       */
      if (mainInfo == null & !isHierarchyModel())
         return;
     
      //no infos are stored for leaves in a hierarchy model
      if (mainInfo != null)
      {
         //remove all children
         if (mainInfo.m_children != null)
         {
            //make copy of children because it will be modified while we walk it
            Collection<IPSReference> tmp = new ArrayList<>(mainInfo.m_children);
            tmp.addAll(mainInfo.m_children);
            for (IPSReference childRef : tmp)
               removeFromCache(childRef);
         }
      }

      //remove from child list
      if (ref instanceof IPSHierarchyNodeRef)
      {
         IPSHierarchyNodeRef parent = ((IPSHierarchyNodeRef) ref).getParent();
         for (DataInfo info : mgrInfo.m_cache)
         {
            if (info.m_ref.equals(parent) && info.m_children != null)
            {
               info.m_children.remove(ref);
               break;
            }
         }
      }
      
      if (mainInfo != null)
         mgrInfo.m_cache.remove(mainInfo);
   }

   /**
    * Move a reference between 2 different parents in the cache. If
    * <code>source</code> and <code>target</code> are the same, returns
    * immediately.
    * 
    * @param source The original parent of the supplied ref. Assumed not
    * <code>null</code>.
    * @param ref Assumed not <code>null</code>.
    * @param target The new parent of the supplied ref. Assumed not
    * <code>null</code>.
    */
   private void moveInCache(IPSReference source, IPSReference ref,
         IPSReference target)
   {
      if ((source == null && target == null)
            || (source != null && source.equals(target)))
      {
         return;
      }
      
      if (source != null)
      {
         DataInfo sourceInfo = getInfo(source);
         assert (sourceInfo != null);
         assert (sourceInfo.m_children != null);
         sourceInfo.m_children.remove(ref);
      }

      if (target != null)
      {
         DataInfo targetInfo = getInfo(target);
         assert (targetInfo != null);
         assert (targetInfo.m_children != null);
         if (!targetInfo.m_children.contains(ref))
            targetInfo.m_children.add(ref);
      }
   }

   /**
    * Places the supplied reference and the object that it refers to in the
    * cache. It is assumed that this relationship is valid for this pair. If the
    * reference is already in the cache, the supplied data object replaces the
    * existing one. 
    * <p>
    * If this is a hierarchical model, the parent list of children is modified
    * as well.
    * 
    * @param ref Assumed not <code>null</code>.
    * @param data Assumed not <code>null</code>.
    */
   synchronized void updateCache(IPSReference ref, Object data)
   {
      if (!getProxy().getMetaData().isCacheable())
         return;
      DataInfo info = getInfo(getTreeName(ref), ref, true);
      info.m_data = data;
      if ( ref instanceof IPSHierarchyNodeRef)
      {
         IPSHierarchyNodeRef node = (IPSHierarchyNodeRef) ref;
         if (node.getParent() != null)
         {
            DataInfo parentInfo = getInfo(node.getParent());
            if (parentInfo!=null && parentInfo.m_children == null)
               parentInfo.m_children = new ArrayList<>();
            for (IPSReference testRef : parentInfo.m_children)
            {
               if (testRef.equals(ref))
               {
                  parentInfo.m_children.remove(testRef);
                  break;
               }
            }
            parentInfo.m_children.add(ref);
         }
      }
   }

   /**
    * Checks if the supplied reference is currently in the cache.
    * 
    * @param ref Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   synchronized boolean isRefCached(IPSReference ref)
   {
      return getInfo(ref) != null;
   }

   /**
    * Checks if the data associated with the supplied reference is currently in
    * the cache.
    * 
    * @param ref Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   synchronized boolean isDataCached(IPSReference ref, boolean isAcl)
   {
      return getDataFromCache(ref, isAcl) != null;
   }

   /**
    * If <code>ref</code> is in the cache, the associated data object is
    * returned.
    * 
    * @param ref Assumed not <code>null</code>.
    * 
    * @return May be <code>null</code>.
    */
   synchronized Object getDataFromCache(IPSReference ref, boolean isAcl)
   {
      DataInfo info = getInfo(ref);
      if(info == null)
         return null;
      if(isAcl)
         return info.m_acl;
      return info.m_data;
   }

   /**
    * Checks if the supplied type is known by this model, optionally throwing an
    * exception.
    * 
    * @param objectType May be <code>null</code>, which may cause an
    * exception.
    * 
    * @param throwEx If <code>true</code>, an <code>IllegalArgumentException
    * </code>
    * is thrown with an appropriate message if <code>objectType</code> is
    * <code>null</code> or unknown to this model.
    * 
    * @return <code>true</code> if this model manages the supplied type,
    * <code>false</code> if <code>null</code> or type is unknown to this
    * model.
    */
   boolean checkObjectType(PSObjectType objectType, boolean throwEx)
   {
      if (null == objectType)
      {
         if (throwEx)
            throw new IllegalArgumentException("the object type cannot be null");
         return false;
      }

      Enum secondary = objectType.getSecondaryType();
      boolean result = objectType.getPrimaryType().equals(m_primaryObjectType)
            && ((secondary == null) || (m_primaryObjectType
                  .isAllowedType(secondary)));
      if (!result && throwEx)
      {
         throw new IllegalArgumentException("the supplied type ("
               + objectType.toString() + ") is not valid for this model ("
               + m_primaryObjectType.toString() + ")");
      }
      return result;
   }

   /**
    * Performs a check for different types of locking.
    * 
    * @param ref Assumed not <code>null</code>.
    * 
    * @param local If <code>true</code>, only checks if the referenced object
    * is locked in this session of the workbench. Otherwise, checks if anyone
    * has it locked.
    * 
    * @return <code>true</code> if the supplied reference is currently locked,
    * <code>false</code> otherwise.
    */
   synchronized private boolean checkLocked(IPSReference ref, boolean local,
         boolean isAcl)
   {
      boolean locked = false;
      if (local)
      {
         if (isRefCached(ref))
            locked = getInfo(ref).m_locked;
      }
      else
      {
         if (isDataCached(ref, isAcl))
         {
            if(isAcl)
               locked = getInfo(ref).m_aclLocked;
            else
               locked = getInfo(ref).m_locked;
         }
         else if (!isAcl && ref.isPersisted())
         {
            try
            {
               locked = getProxy().isLocked(ref);
            }
            catch (PSModelException e)
            {
               throw new RuntimeException(e);
            }
         }
         else
            // unknown ref
            locked = false;
      }
      return locked;
   }

   /**
    * Removes the supplied reference from the set of locked references
    * (regardless of how many times it was added.) If it is not present, no
    * action is taken.
    * 
    * @param ref Assumed not <code>null</code>.
    */
   synchronized private void removeFromLockList(IPSReference ref, boolean isAcl)
   {
      if (!isRefCached(ref))
         return;
      if(isAcl)
         getInfo(ref).m_aclLocked = false;
      else
         getInfo(ref).m_locked = false;
   }

   /**
    * Removes the data object for the supplied handle. If not in the cache, no
    * action is taken.
    * 
    * @param ref Assumed not <code>null</code>.
    */
   synchronized private void removeDataFromCache(IPSReference ref, boolean isAcl)
   {
      if (!isRefCached(ref))
         return;
      if(isAcl)
         getInfo(ref).m_acl = null;
      else
         getInfo(ref).m_data = null;
         
   }

   /**
    * If <code>ref</code> is cached, it is marked as locked by a client. 
    * Otherwise, returns immediately.
    * 
    * @param ref Assumed not <code>null</code>.
    */
   private synchronized  void addToLockList(IPSReference ref, boolean isAcl)
   {
      if (!isRefCached(ref))
         return;
      if(isAcl)
         getInfo(ref).m_aclLocked = true;
      else
         getInfo(ref).m_locked = true;
   }


   /**
    * A simple interface that allows us to apply an operation to all caches.
    *
    * @author paulhoward
    */
   private interface CacheProcessor
   {
      /**
       * The implementation does the work appropriate to a single cache.
       * 
       * @param mgrInfo Will never be <code>null</code>.
       * 
       * @return Depends on the implementation.
       */
      public Object process(ManagerInfo mgrInfo);
   }
   
   /**
    * Calls {@link CacheProcessor#process(ManagerInfo)} on every manager info
    * known to this model. If the process method returns non-<code>null</code>,
    * the processing is stopped and the object returned by the process method
    * is returned by this method.
    * 
    * @param cp Assumed not <code>null</code>.
    * 
    * @return The object returned by the cp.process method.
    */
   private Object processAllCaches(CacheProcessor cp)
   {
      Object o = null;
      for (ManagerInfo mgrInfo : m_mgrInfos.values())
      {
         o = cp.process(mgrInfo);
         if (o != null)
            return o;
      }
      return o;
   }
   
   /**
    * Walks all caches, calling {@link #getInfo(String, IPSReference, boolean)}
    * on each one until a match is found.
    */
   synchronized private DataInfo getInfo(final IPSReference ref)
   {
      return getInfo(getTreeName(ref), ref, false);
   }

   /**
    * Scans the supplied cache for the supplied reference. If not found and
    * <code>add</code> is <code>true</code>, an empty one is created and added
    * to the cache and returned.
    * 
    * @param treeName Assumed to be non-<code>null</code>.
    * 
    * @param ref Assumed not <code>null</code>.
    * 
    * @param add A flag that controls whether to create a new info if a cached
    * one is not found.
    *  
    * @return Never <code>null</code> if <code>add</code> is <code>true</code>,
    * otherwise, may be <code>null</code>.
    */
   private DataInfo getInfo(final String treeName, final IPSReference ref,
         final boolean add)
   {
      Collection<DataInfo> cache = getManagerInfo(treeName).m_cache;
      DataInfo info = null;
      for (DataInfo testInfo : cache)
      {
         if (testInfo.m_ref.referencesSameObject(ref))
         {
            info = testInfo;
            break;
         }
      }
      if (null == info && add)
      {
         info = new DataInfo(ref);
         cache.add(info);
      }
      return info;
   }
   
   /**
    * Retrieves the structure containing the cache and hierarchy manager for the
    * specified tree.
    * 
    * @param treeName For flat models, use FLAT_MODEL_TREENAME. Otherwise, must
    * be a value returned by the {@link #getHierarchyTreeNames()} method.
    * 
    * @return The actual object from the cache. Changes to it will affect the
    * object in the cache. Never <code>null</code>.
    */
   private ManagerInfo getManagerInfo(String treeName)
   {
      if (StringUtils.isBlank(treeName))
      {
         assert(!isHierarchyModel());
         treeName = FLAT_MODEL_TREENAME;
      }
      else
      {
         assert(isHierarchyModel());
         treeName = treeName.toLowerCase();
         boolean found = false;
         for (String name : getHierarchyTreeNames())
         {
            if (name.equalsIgnoreCase(treeName)) {
               found = true;
               break;
            }
         }
         if (!found)
            throw new IllegalArgumentException("Unknown tree name: " + treeName);
      }
      
      ManagerInfo info = m_mgrInfos .get(treeName);
      if (info == null)
      {
         info = new ManagerInfo(treeName);
         m_mgrInfos.put(treeName, info);
      }
      return info;
   }

   /**
    * A simple class that acts as a structure to group all data that is 
    * tree-dependent.
    *
    * @author paulhoward
    */
   private class ManagerInfo
   {
      public ManagerInfo(String treeName)
      {
         m_treeName = treeName;
      }
      
      /**
       * The name supplied in the ctor. Never <code>null</code>.
       */
      public final String m_treeName;
      
      /**
       */
      /**
       * If a flat model, <code>true</code> means this model has been cataloged,
       * <code>false</code> that it hasn't yet. If a hierarchical model, the
       * flag indicates the same thing, but only for the root node.
       * <p> 
       * This flag is set after {@link #catalog(boolean)} has been called once.
       * Once set, catalogs are generated from the cache. It is cleared when
       * {@link #flush(IPSReference) flush(<code>null</code>)} is called.
       * <p>
       * Defaults to <code>false</code>.
       */
      public boolean m_cataloged;
      
      /**
       * Stores the objects and references as they are cataloged and created. We
       * cannot use a map with IPSReference as the key because the references
       * can change over time. If this is a flat model, then the infos for
       * leaves are stored here. Otherwise, only the infos for containers are
       * stored here. Children of the container are stored in a collection in
       * the info.
       */
      public final Collection<DataInfo> m_cache = new ArrayList<DataInfo>();
      
      /**
       * If this is a hierarchical model, the mgr is lazily loaded, then never
       * changed. If flat, it is always <code>null</code>.
       */
      public IPSHierarchyManager m_hierarchyManager;
   }
   
   /**
    * A little structure to group some related objects for storage in the cache.
    */
   private class DataInfo
   {
      /**
       * Guarantees that ref is set. Set of other params as needed after 
       * construction.
       * 
       * @param ref Never <code>null</code>.
       */
      public DataInfo(IPSReference ref)
      {
         if ( null == ref)
         {
            throw new IllegalArgumentException("ref cannot be null");  
         }
         m_ref = ref;
      }
      
      /**
       * The reference to the associated data object. Never <code>null</code>
       * after construction.
       */
      public IPSReference m_ref = null;
      
      /**
       * If this model supports a hierarchy, then this set contains all the
       * children of {@link #m_ref}. Will be populated when the parent is
       * cataloged. If the parent is a leaf, this node will be 
       * <code>null</code>.
       */
      public Collection<IPSReference> m_children = null;
      
      /**
       * Default value is <code>false</code>.
       * <p>
       * Is this data object locked for editing.
       */
      public boolean m_locked = false;

      /**
       * Default value is <code>false</code>.
       * <p>
       * Is the ACL for this data object locked for editing.
       */
      public boolean m_aclLocked = false;

      /**
       * Default value is <code>null</code>.
       * <p>
       * The data associated with the reference. <code>null</code> until load
       * or create called.
       */
      public Object m_data = null;

      /**
       * Default value is <code>null</code>.
       * <p>
       * The ACL associated with the object with this reference.
       * <code>null</code> until load ACL called.
       */
      public IPSAcl m_acl = null;

      /**
       * Default value is <code>false</code>.
       * <p>
       * If this model supports hierarchy manager, has this node been cataloged
       * yet.
       */
      public boolean m_cataloged = false;
   }
   
   /**
    * The name used as the cache id for a flat model.
    */
   private String FLAT_MODEL_TREENAME = "";

   /**
    * The name assigned to this model during construction. Never
    * <code>null</code> or empty. Set in ctor then never changed.
    */
   private String m_name;

   /**
    * A brief overview of the design objects this model manages. Never
    * <code>null</code> may be empty. Set in ctor then never changed.
    */
   private String m_description = "";

   /**
    * The main category for the design objects managed by this model. Never
    * <code>null</code>. Set in ctor, then never changed. Of class type
    * <code>Enum</code>
    */
   private IPSPrimaryObjectType m_primaryObjectType;

   /**
    * Stores the objects and references as they are cataloged and created.
    * We cannot use a map with IPSReference as the key because the references
    * can change over time. If this is a flat model, then the infos for leaves
    * are stored here. Otherwise, only the infos for containers are stored here.
    * Children of the container are stored in a collection in the info.
    * <p>
    * Their is a cache per tree. The key is the treeName. For a flat model, use
    * {@link #FLAT_MODEL_TREENAME} string as the key.
    */
   Map<String, ManagerInfo> m_mgrInfos = new HashMap<>();

   /**
    * Stores all the notification listeners and what events they want to be
    * notified of. The value is an OR'd set of {@link ModelEvents}.
    * <p>
    * Never <code>null</code>, may be empty.
    */
   private Map<IPSModelListener, Integer> m_listeners = 
      new HashMap<>();
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static final Logger log = LogManager.getLogger(PSCmsModel.class);
}

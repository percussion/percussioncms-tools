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
package com.percussion.client.proxies;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.services.security.IPSAcl;

import java.util.Collection;
import java.util.List;

/**
 * Proxy interface providing CRUD and cataloging services for objects
 * implementing <code>IPSCmsModel</code> interface. This interface is a broker
 * between the ui model layer and the web services layer. The model layer should
 * never need to access anything from web services layer directly. All the
 * services required are available in this interface. Understands the languages
 * of model layer as well as webservices layer.
 * <p>
 * Ctors (and any member variables whose values are calculated at construction
 * time) should not attempt to connect to the server.
 */
public interface IPSCmsModelProxy
{
   /**
    * An aggregation of information about how this proxy behaves that may be
    * useful to users of it.
    * 
    * @author paulhoward
    */
    interface IModelInfo
   {
      /**
       * Higher levels should honor this flag and not cache any returned values
       * if this method returns <code>false</code>.
       * 
       * @return If <code>false</code>, then higher levels should not cache
       * data returned by this proxy, they should always re-query when
       * cataloging.
       */
       boolean isCacheable();
   }

   /**
    * Returns an object that contains information about how this particular
    * model behaves. No connection to the server should be made to get this
    * information.
    * 
    * @return Never <code>null</code>.
    */
    IModelInfo getMetaData();

   /**
    * Change the name of a design object. A name change is persisted immediately
    * by loading the object from the server, setting the new name and saving it.
    * The name in the supplied reference is updated as well.
    * <p>
    * If the object is locked by someone else or the caller does not have
    * sufficient privileges, an exception is thrown.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @param name Never <code>null</code>or empty. No whitespace.
    * 
    * @param data If supplied, the name on this object will be set to the
    * supplied name. It will not take part in the name persistence. If not
    * provided, it is ignored.
    * 
    * @throws PSModelException If the object is locked or the caller doesn't
    * have proper access or any other reason from the server.
    */
    void rename(IPSReference ref, String name, Object data)
      throws PSModelException;

   /**
    * Just like {@link #rename(IPSReference, String, Object)}, except the
    * change is not persisted. Only the supplied ref and object are modified. No
    * exception is thrown because only the supplied objects are used.
    * 
    * @param ref
    * @param name
    * @param data
    * 
    * @see #rename(IPSReference, String, Object)
    */
    void renameLocal(IPSReference ref, String name, Object data);

   /**
    * Catalog all objects of the type this proxy supports. If the objects are
    * Hierarchical use getChildren using the Hierarchical proxy
    * 
    * @return Each entry is an {@link IPSReference}. Never <code>null</code>,
    * may be empty.
    * @throws PSModelException if there is any error from server while
    * cataloging.
    * @throws UnsupportedOperationException if called on a Hierarchical object.
    */
    Collection<IPSReference> catalog() throws PSModelException;

   /**
    * Instantiates a new instance and adds it to the model. To save them
    * permanently, supply the returned reference(s) to the
    * {@link #save(IPSReference[], Object[], boolean) save} method.
    * <p>
    * If the model represents its members in a hierarchy, the
    * <code>addChildren</code> method in the <code>IPSHierarchyManager</code>
    * must be called instead. In that case, this method will throw an
    * <code>UnsupportedOperationException</code>.
    * 
    * @param objType type of the objects to be created, must not be
    * <code>null</code>.
    * 
    * @param names How many new instances to create and what to call them. Never
    * <code>null</code> or empty. Each entry must be non-empty. The names are
    * not checked for duplication.
    * 
    * @param results The generated objects are added to this list, in the same
    * order as the returned references. Never <code>null</code>. It is
    * cleared before any objects are added.
    * 
    * @return An array with <code>names.size()</code> elements, each one non-
    * <code>null</code>. The design objects corresponding to each returned
    * reference are found in the same position in the <code>results</code>
    * list.
    * @throws PSMultiOperationException If one or more of the objects failed the
    * creation, this exception will contain information about the entire
    * operation, including what was successful and what failed. The result entry
    * for success in the exception results will be <code>null</code>. Only
    * those objects that created successfully will possibly be modified.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
    IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results) throws PSMultiOperationException, PSModelException;

   /**
    * Creates clones of supplied objects changing properties that would conflict
    * if it were saved. For example, the name is always changed by prepending
    * "Copy N" onto the name and the id will always be cleared. Other properties may
    * be changed as well, depending on the object.
    * <p>
    * The object is created in memory, but not persisted. Call
    * {@link #save(IPSReference[], Object[], boolean) save} to persist it.
    * <p>
    * If the model represents its members in a hierarchy, this method will throw
    * an <code>UnsupportedOperationException</code>.
    * <p>
    * The ACL is copied. If the current user has full access, the ACL is not
    * modified. If they do not, the current user is added as a new entry to the
    * ACL with full access.
    * 
    * @param sourceObjects The objects to be cloned. Must not be
    * <code>null</code> or empty.
    * 
    * @param names Optional names used instead of the default name described
    * above. May be <code>null</code> and any entry may be <code>null</code>
    * or empty to use the default for the corresponding object.
    * 
    * @param results The generated objects are added to this list, in the same
    * order as the returned references. Never <code>null</code>. It is
    * cleared before any objects are added.
    * 
    * @return Never <code>null</code>, its length will equal
    * <code>sourceObjects</code> length. The design objects corresponding to
    * each returned reference are found in the same position in the
    * <code>results</code> list.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * creation, this exception will contain information about the entire
    * operation, including what was successful and what failed. The result entry
    * for success in the exception results will be {@link IPSReference} and that
    * for a failure will be the {@link Throwable}. The entry in the
    * <code>results</code> for a failure will be <code>null</code>.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
    IPSReference[] create(Object[] sourceObjects, String[] names,
         List<Object> results)
      throws PSMultiOperationException, PSModelException;

   /**
    * Loads objects from the server for the references supplied.
    * 
    * @param refs Array of references previously cataloged. Must not be
    * <code>null</code> or empty. If an id does not reference an existing
    * object (either it's invalid or the object was deleted by someone else,) an
    * exception is thrown.
    * 
    * @param lockForEdit If <code>true</code>, an attempt will be made to
    * lock the object for editing. In order to successfully save an object, it
    * must first be locked. If the object is already locked, an exception is
    * thrown. The exception contains information about who has it locked.
    * 
    * @param overrideLock Generally only used to recover from a crash. If the
    * user owns the lock on an object but in a different session, then the same
    * user can take ownership of the lock by providing <code>true</code>.
    * This could happen if the workbench crashes.
    * 
    * @return Never <code>null</code>. Each entry is an instance of the
    * appropriate object class. The class of the object is identified in the
    * description of the type of the supplied reference.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * load, this exception will contain information about the entire operation,
    * including what was successful and what failed. You can retrieve the new
    * <code>Objects</code>s from this object as well as the exception for
    * each error condition.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   Object[] load(IPSReference[] refs, boolean lockForEdit,
      boolean overrideLock) throws PSMultiOperationException, PSModelException;

   /**
    * Saves the supplied data to persistent storage on the server. The object
    * must have previously been obtained with a call to
    * {@link #create(PSObjectType, Collection, List) create} or
    * {@link #load(IPSReference[], boolean, boolean) load} with
    * <code>true</code> for the second parameter (lock for edit). If the
    * current lock is invalid, the save will fail and the
    * {@link com.percussion.client.models.PSLockException} will appear in the
    * exception results.
    * 
    * @param refs One for each object being saved. Never <code>null</code> or
    * empty.
    * 
    * @param data The object to save. Must have one entry for each entry in the
    * <code>refs</code> param.
    * 
    * @param releaseLock If you will no longer be editing the data, then
    * <code>true</code> should be supplied so the object will be available for
    * someone else to edit.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * save, this exception will contain information about the entire operation,
    * including what was successful and what failed. The result entry for
    * success in the exception results will be <code>null</code>. Only those
    * objects that saved successfully will possibly be modified.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException;

   /**
    * Removes the object with the supplied reference from persistent storage on
    * the server. The deletion of any object may fail because it is locked by
    * someone else or the caller does not have the necessary permissions.
    * 
    * @param refs Reference of the object to be deleted, must not be
    * <code>null</code> or empty.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * deletion, this exception will contain information about the entire
    * operation, including what was successful and what failed. The result entry
    * for success in the exception results will be <code>null</code>.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   void delete(IPSReference[] refs) throws PSMultiOperationException,
      PSModelException;

   /**
    * Removes the object ACLs with the supplied reference from persistent
    * storage on the server. The deletion of any object ACL may fail because it is
    * locked by someone else or the caller does not have the necessary
    * permissions.
    * 
    * @param owners Reference of the objects whose ACLs to be deleted, must not
    * be <code>null</code> or empty.
    * 
    * @throws PSMultiOperationException If one or more of the ACL objects failed
    * the deletion, this exception will contain information about the entire
    * operation, including what was successful and what failed. The result entry
    * for success in the exception results will be <code>null</code>.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   void deleteAcl(IPSReference[] owners)
      throws PSMultiOperationException, PSModelException;

   /**
    * Checks whether the referenced object is locked. The locking information in
    * the supplied reference will be updated so that the caller can further see if
    * it is locked to current user in current session or otherwise.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @return <code>true</code> if it is locked by anyone, including the
    * caller, <code>false</code> otherwise.
    * @throws PSModelException if loading the lock info from server fails for
    * any reason.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   boolean isLocked(IPSReference ref) throws PSModelException;

   /**
    * Release lock for the objects specified by their references.
    * 
    * @param refs Array of references to release lock, must not be
    * <code>null</code> or empty.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * operation, this exception will contain information about the entire
    * operation, including what was successful and what failed. The entries that
    * were successful will have <code>null</code> in the results.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   void releaseLock(IPSReference[] refs)
      throws PSMultiOperationException, PSModelException;

   /**
    * Marks cache entries as dirty except for objects that are locked. This
    * includes references and actual objects that have been loaded but not
    * locked. Locked objects are never flushed. It is up to the implementation
    * to whether anything happens. For example, if an implementation does not
    * cache anything then it is obvious that it does not have to flush. It is
    * rare that anything needs to be cached at the proxy layer.
    * 
    * @param ref If provided, only entries associated with this object are
    * flushed. Otherwise, all catalog and object cache references are flushed,
    * except if the object is locked.
    */
   void flush(IPSReference ref);

   /**
    * Load ACLs for an array of design objects specified. To make changes to an
    * ACL, call this method, make changes to the returned object and call
    * {@link #saveAcl(IPSReference[], IPSAcl[], boolean)}.
    * 
    * @param refs Array of references for which the ACLs have to be loaded. Must
    * not be <code>null</code> or empty.
    * 
    * @param lock A flag that allows making changes to the ACL. The ACL must be
    * locked before changes can be persisted.
    * 
    * @return The current ACLs for the referenced objects. Each entry in the
    * array corresponds to the ACL corresponding the object identified by the
    * array of references supplied. If an object does not have one, then the
    * corresponding entry will be created with default entries.
    * 
    * @throws PSMultiOperationException If one or more of the object ACLs failed
    * to load, this exception will contain information about the entire
    * operation, including what was successful and what failed. You can retrieve
    * the new {@link IPSAcl} s from this object as well as the exception for
    * each error condition.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    * 
    * @throws UnsupportedOperationException If the object type of the refs
    * does not support ACLs.
    */
   Object[] loadAcl(IPSReference[] refs, boolean lock)
      throws PSMultiOperationException, PSModelException;

   /**
    * Save the specified ACL to server.
    * 
    * @param ref object reference array , must not be <code>null</code>.
    * 
    * @param acl ACL object array to save to server, must not be
    * <code>null</code>. One-to-one correspondence is assumed between the
    * object reference entries and ACLs in the array. The resulting user
    * permissions will be set on each reference after save.
    * 
    * @param releaseLock Flag to indicate if the lock to be released after
    * saving.
    * 
    * @throws PSMultiOperationException If one or more of the ACLs failed to
    * save, this exception will contain information about the entire operation,
    * including what was successful and what failed. The result entry for
    * success in the exception results will be <code>null</code>. Only those
    * ACLs that saved successfully will possibly be modified.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   void saveAcl(IPSReference[] ref, IPSAcl[] acl, boolean releaseLock)
      throws PSMultiOperationException, PSModelException;

   /**
    * Release locks for the ACLs specified by their long ids. Please note that
    * the method {@link #loadAcl(IPSReference[], boolean)} takes the reference
    * to the actual object not the ACL itself whereas here it is the ACL id
    * array that is expected. Very similar to
    * {@link #releaseLock(IPSReference[])} except that this takes ACL id array.
    * 
    * @param aclIds long ids of the acls to release, if <code>null</code> or
    * empty returns doing nothing.
    * @throws PSMultiOperationException see {@link #releaseLock(IPSReference[])}
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   void releaseAclLock(Long[] aclIds) throws PSMultiOperationException,
      PSModelException;

   /**
    * Enumeration for the method names/indices in this interface
    */
   enum METHOD
   {
      /**
       * 'catalog' method name of the interface
       */
      CATALOG(1),

      /**
       * 'create' method name of the interface
       */
      CREATE(2),

      /**
       * 'delete' method name of the interface
       */
      DELETE(3),

      /**
       * 'load' method name of the interface
       */
      LOAD(4),

      /**
       * 'releaseLock' method name of the interface
       */
      RELEASELOCK(5),

      /**
       * 'rename' method name of the interface
       */
      RENAME(6),

      /**
       * 'save' method name of the interface
       */
      SAVE(7);

      METHOD(int ord)
      {
         if (ord > Short.MAX_VALUE)
         {
            throw new IllegalArgumentException("Ordinal value too large"); //$NON-NLS-1$
         }
         mi_ordinal = (short) ord;
      }

      /**
       * Ordinal value, initialized in the ctor, and never modified.
       */
      private short mi_ordinal;

      /**
       * Returns the ordinal value for the enumeration. This ordinal is used as
       * part of the {@link com.percussion.utils.guid.IPSGuid} id, and can be
       * used as part of the cataloging process
       * 
       * @return the ordinal
       */
      short getOrdinal()
      {
         return mi_ordinal;
      }
   }
}

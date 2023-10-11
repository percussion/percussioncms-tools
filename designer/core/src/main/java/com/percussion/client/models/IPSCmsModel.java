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
package com.percussion.client.models;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.security.PSAuthorizationException;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A model presents the operations that can be performed on a small, closely
 * related set of objects. Generally, a model represents a single object.
 * However, if the objects are related in a hierarchical manner, it may represent
 * multiple objects that are part of such a graph. Or if objects are generally
 * the same but have different content, such as a set of configuration files.
 * <p>
 * Implementing classes must have constructor with signature
 * <code>IPSCmsModel(String name, String description,
 * IPSPrimaryObjectType supportedType)</code>.
 * See {@link com.percussion.client.models.impl.PSCmsModel#PSCmsModel(String,
 * String, IPSPrimaryObjectType)} for more information.
 * <p>
 * A model has a name, description and a set of object types that it represents.
 * It presents cataloging of the objects, CRUD operations (some models may be
 * read only) and a listener interface that allows callers to register for
 * interesting events that happen to objects in this model.
 * <p>
 * In all the methods that accept an {@link IPSReference}, only those instances
 * that have been returned by this class should be supplied. Arbitrary instances
 * may not work and are not supported.
 * <p>
 * If a hierarchy manager is available, some methods (all signatures) in this
 * interface will behave differently as noted in the following table. The table
 * also shows the hierarchical equivalent method, where applicable.
 * <p>
 * <table>
 * <th>
 * <td>IPSCmsModel method</td>
 * <td>Equivalent IPSHierarchyManager method</td>
 * <td>Description</td>
 * <td></td>
 * </th>
 * <tr>
 * <td>create</td>
 * <td>createChildren</td>
 * <td>create throws <code>UnsupportedOperationException</code></td>
 * </tr>
 * <tr>
 * <td>catalog</td>
 * <td>catalog</td>
 * <td>The model cataloger returns same value as the hierarchy cataloger with a
 * <code>null</code> parent.</td>
 * </tr>
 * <tr>
 * <td>delete</td>
 * <td>removeChildren</td>
 * <td>delete is equivalent to calling
 * removeChildren(((PSHierarchyNode)ref).getParent(), new PSHierarchyNode[]
 * {(PSHierarchyNode)ref}), which deletes the supplied node and all its children
 * recursively. If the supplied node is the root, the supplied node is deleted.
 * </td>
 * </tr>
 * <tr>
 * <td>save</td>
 * <td>n/a</td>
 * <td>works as described, except ancestor nodes are saved as well if they have
 * never been persisted, otherwise, they are not saved </td>
 * </tr>
 * </table>
 * <p>
 * A model caches as much data as possible and always returns results from the
 * cache if available unless this behavior is overridden. Every method that 
 * may use a cache accepts a flag that allows the caller to force an override
 * of the default behavior and query the server, even though the request could
 * have been fulfilled from the cache.
 * <p> 
 * A model listens for logon/logoff events from the core factory. When a logoff
 * event is received, all data caches are flushed. No messages are sent out
 * when this happens. If objects are still locked, messages are logged, but
 * nothing else is done.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public interface IPSCmsModel
{
   /**
    * The unique name for this model as declared in the model's configuration.
    * 
    * @return Never <code>null</code> or empty.
    */
   String getName();

   /**
    * Change the name of a design object. A name change is persisted
    * immediately, even if the object is open for editing. In that case, the
    * object is requested from the server and just the name is changed and
    * re-saved. A name-change notification is then sent.
    * <p>
    * If the object is locked by someone else or the caller does not have
    * sufficient privileges, an exception is thrown.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @param name Never <code>null</code>or empty. Leading and trailing
    * whitespace will be removed. See
    * {@link com.percussion.client.PSCoreUtils#isValidObjectName(String)} and
    * {@link com.percussion.client.PSCoreUtils#isValidHierarchyName(String)} for
    * rules of allowed characters.
    * 
    * @throws PSAuthorizationException If the caller doesn't have sufficient
    * access.
    * @throws PSDuplicateNameException If another object managed by this model
    * has the same name, case-insensitive.
    * @throws PSLockException If the object is locked by someone else.
    * @throws PSModelException If any problems communicating with the server.
    */
   void rename(IPSReference ref, String name)
      throws PSAuthorizationException, PSDuplicateNameException,
      PSLockException, PSModelException;

   /**
    * A short description of this model, as declared in the model's
    * configuration.
    * 
    * @return May be empty, never <code>null</code>.
    */
   String getDescription();

   /**
    * Instantiates a new instance identified by the supplied object type and
    * adds it to the model. To save them permanently, supply the returned
    * reference(s) to the {@link #save(IPSReference, boolean) save} method.
    * <p>
    * If the model represents its members in a hierarchy, the
    * {@link IPSHierarchyManager#createChildren(
    * com.percussion.client.IPSHierarchyNodeRef, PSObjectType, List)
    * createChildren} method in the hierarchy manager must be called instead. In
    * that case, this method will throw an
    * <code>UnsupportedOperationException</code>.
    * 
    * @param objectType The type of object to create. Must be one of the types
    * returned by the {@link #getObjectTypes()} method.
    * 
    * @param names How many new instances to create and what each should be
    * called. The names must be unique among all objects in this model of a
    * given subtype. If <code>null</code> or empty, a single object with an
    * auto-generated name is returned. Each entry must be non-<code>null</code>
    * and non-empty.
    * 
    * @param defaulter If provided, each generated object will be passed through
    * this class before a creation notification
    * @return An array with <code>names.size()</code> elements, each one non-
    * <code>null</code>.
    * 
    * @throws UnsupportedOperationException If this model uses a hierarchy
    * manager, or it does not support creation of new objects. See object type
    * enum to determine which objects support creation.
    * 
    * @throws PSMultiOperationException If one or more of the creations fail.
    * Each entry in the results from the exception will either be the desired
    * object type, a {@link PSDuplicateNameException}, or some other exception
    * in extraordinary circumstances (e.g. OutOfMemory.)
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   IPSReference[] create(PSObjectType objectType, List<String> names,
         IPSObjectDefaulter defaulter)
      throws PSMultiOperationException, PSModelException;

   /**
    * Convenience method that calls
    * {@link #create(PSObjectType, List, IPSObjectDefaulter) create(objectType,
    * names, null)}.
    */
   IPSReference[] create(PSObjectType objectType, List<String> names)
      throws PSMultiOperationException, PSModelException;

   /**
    * Convenience method that calls
    * {@link #create(PSObjectType, List, IPSObjectDefaulter) create(objectType,
    * Collections.singletonList(name), null)}.
    * 
    * @throws PSDuplicateNameException If an object in this model already has
    * the supplied name, case-insensitive.
    * 
    * @throws PSModelException If any problems communicating with the server.
    */
   IPSReference create(PSObjectType objectType, String name)
      throws PSDuplicateNameException, PSModelException;
   
   /**
    * Convenience method that calls
    * {@link #create(PSObjectType, List, IPSObjectDefaulter) create(objectType,
    * Collections.singletonList(name), defaulter)}.
    * 
    * @throws PSDuplicateNameException If an object in this model already has
    * the supplied name, case-insensitive.
    */
   IPSReference create(PSObjectType objectType, String name,
      IPSObjectDefaulter defaulter)
      throws PSDuplicateNameException, PSModelException;

   /**
    * Creates a clone of the object(s) referenced by the supplied param,
    * changing properties that would conflict if it were saved. For example, the
    * name is always changed, by default, prepending "Copy N" onto the name and
    * the id are always cleared. Other properties may be changed as well,
    * depending on the object.
    * <p>
    * The object is created in memory, but not persisted. Call {@link
    * #save(IPSReference, boolean) save} to persist it.
    * <p>
    * The ACL is copied. If the current user has full access, the ACL is not
    * modified. If they do not, the current user is added as a new entry to the
    * ACL with full access.
    * 
    * @param sources The object(s) to be copied. Never <code>null</code> and
    * each entry must be non-<code>null</code> and its type known to this
    * model. If empty, returns immediately.
    * 
    * @param names If provided, the supplied name is used instead of the name as
    * defined above. Any entry can be <code>null</code> or empty to use the
    * default convention and the array can be <code>null</code> to use the
    * default for all names. It is the caller's responsibility to make sure
    * the supplied names do not conflict with existing names, which would cause
    * an exception.
    * 
    * @return A reference to the new object. Call
    * {@link #load(IPSReference,boolean,boolean) load} to retrieve the new
    * object. The name of each object is changed using the following pattern:
    * <p>
    * Copy N <original name>
    * <p>
    * Where N is a number that makes the name unique.
    * 
    * @throws UnsupportedOperationException If this model uses a hierarchy
    * manager, or it does not support creation of new objects. See object type
    * enum to determine which objects support creation.
    * 
    * @throws PSMultiOperationException If the data for the source reference
    * can't be loaded from the server. Each entry in the results is either an
    * exception or the reference of the newly created object.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   IPSReference[] create(IPSReference[] sources, String[] names)
      throws PSMultiOperationException, PSModelException;

   /**
    * Convenience method that calls
    * {@link #load(IPSReference[], boolean,boolean)  load(new IPSReference[]
    * &#123; ref &#125;}, lock, overrideLock)}.
    * 
    * @throws Exception The exception thrown by the referenced load method,
    * extracted from the <code>PSMultiOperationException</code>.
    */
   Object load(IPSReference ref, boolean lock, boolean overrideLock)
      throws Exception;

   /**
    * Objects are retrieved lazily from persistent storage and cached locally.
    * If the <code>locked</code> flag is <code>false</code>, or the object
    * was previously retrieved with a <code>locked</code> flag of
    * <code>true</code> and the lock not released, the cached object will be
    * returned. The first time the object is retrieved with the lock set to
    * <code>true</code>, it will be retrieved from persistent storage, even
    * if a copy is in the cache. This guarantees that the most up-to-date object
    * is returned for editing.
    * 
    * @param refs One or more references previously obtained from this model.
    * Never <code>null</code> and each entry must be valid. If the id does not
    * reference an existing object (either it's invalid or the object was
    * deleted by someone else,) an exception is thrown. If empty, returns
    * immediately.
    * 
    * @param lock If <code>true</code>, an attempt will be made to lock the
    * object for editing. In order to successfully save an object, it must first
    * be locked. If the object is already locked by someone else, an exception
    * is thrown. The exception contains information about who has it locked. If
    * an object is locked for editing, the caller must call
    * {@link #propertyChanged(IPSReference, Map)} after each successful change of
    * data on the object.
    * 
    * @param overrideLock Generally only used to recover from a crash. If the
    * user owns the lock on an object but in a different session, then the same
    * user can take ownership of the lock by providing <code>true</code>.
    * This could happen if the workbench crashes. Ignored if <code>lock</code>
    * is <code>false</code>.
    * 
    * @return Never <code>null</code>. Each entry is an instance of the
    * appropriate object class. The class of the object is identified in the
    * description of the type of the supplied reference.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * load, this exception will contain information about the entire operation,
    * including what was successful and what failed. You can retrieve the new
    * <code>Objects</code>s from this object as well as the exception for
    * each error condition. Generally, the exceptions will be one of the
    * following:
    * <ol>
    * <li>{@link PSLockException} - if trying to lock an object locked by
    * someone else.</li>
    * <li>{@link PSAuthorizationException} - if caller doesn't have sufficient
    * privileges.</li>
    * <li>{@link PSModelException} - general problems such as a failure to
    * reach the server.</li>
    * <ol>
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   Object[] load(IPSReference[] refs, boolean lock, boolean overrideLock)
      throws PSMultiOperationException, PSModelException;

   /**
    * Convenience method that calls
    * {@link #save(IPSReference[],boolean) save(new IPSReference[] 
    * &#123;ref&#125;, releaseLock)}. If the called class throws a
    * <code>PSMultiOperationException</code>, the actual exception is
    * extracted from the class and rethrown by this one.
    * 
    * @throws Exception The exception thrown by the referenced load method,
    * extracted from the <code>PSMultiOperationException</code>.
    */
   IPSReference save(IPSReference ref, boolean releaseLock)
      throws Exception;

   /**
    * Saves the supplied data to persistent storage on the server. The object
    * must have previously been obtained with a call to
    * {@link #load(IPSReference, boolean,boolean) load(IPSReference,
    * <code>true</code>)}. If the current lock is invalid, the save will fail
    * and an exception will be thrown.
    * 
    * @param refs The object to save. Never <code>null</code> and each entry
    * must be valid. If empty, returns immediately.
    * 
    * @param releaseLock If you will no longer be editing the data, then
    * <code>true</code> should be supplied so the object will be available for
    * someone else to edit.
    * 
    * @return Never <code>null</code>. Each entry is an instance of the
    * reference to the saved instance (which may be new if the object had never
    * been persisted before) The underlying object may have been modified as
    * well if it had never been persisted previously.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * save, this exception will contain information about the entire operation,
    * including what was successful and what failed. You can retrieve the
    * exception for each error condition. Successful entries will be
    * <code>IPSReference</code>. Generally, the exceptions will be one of the
    * following:
    * <ol>
    * <li>{@link PSLockException} - if trying to save an object that was not
    * previously locked.</li>
    * <li>{@link PSAuthorizationException} - if caller doesn't have sufficient
    * privileges (rare.)</li>
    * <li>{@link PSModelException} - general problems such as communication
    * errors.</li>
    * <ol>
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   IPSReference[] save(IPSReference[] refs, boolean releaseLock)
      throws PSMultiOperationException, PSModelException;

   /**
    * Convenience method that calls
    * {@link #delete(IPSReference[]) delete(new IPSReference[] 
    * &#123;ref&#125;)}. If the called class throws a
    * <code>PSMultiOperationException</code>, the actual exception is
    * extracted and rethrown by this one.
    * 
    * @throws Exception The exception thrown by the referenced load method,
    * extracted from the <code>PSMultiOperationException</code>.
    */
   void delete(IPSReference ref) throws Exception;

   /**
    * Removes the object from persistent storage. The deletion of any object may
    * fail because it is locked by someone else or the caller does not have the
    * necessary permissions. If the object is not locked by the caller, an
    * attempt will be made to acquire the lock automatically.
    * <p>
    * If this is a hierarchical model, it is assumed that the supplied refs are
    * actually {@link com.percussion.client.IPSHierarchyNodeRef}s and the call
    * is forwarded to the hierarchy manager's
    * {@link IPSHierarchyManager#removeChildren(List)} method.
    * 
    * @param refs Never <code>null</code> and each entry cannot be
    * <code>null</code>. If empty, returns immediately.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * save, this exception will contain information about the entire operation,
    * including what was successful and what failed. You can retrieve the new
    * <code>Objects</code>s from this object as well as the exception for
    * each error condition. Generally, the exceptions will be one of the
    * following:
    * <ol>
    * <li>{@link PSLockException} - if someone else has the object locked </li>
    * <li>{@link PSAuthorizationException} - if caller doesn't have sufficient
    * privileges (rare.)</li>
    * <li>{@link PSModelException} - general problems such as communication
    * errors.</li>
    * <ol>
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   void delete(IPSReference[] refs) throws PSMultiOperationException, 
      PSModelException;

   /**
    * Convenience method that calls {@link #deleteAcl(IPSReference[])}. If the
    * called class throws a <code>PSMultiOperationException</code>, the
    * actual exception is extracted and rethrown by this one.
    * 
    * @throws Exception The exception thrown by the referenced load method,
    * extracted from the <code>PSMultiOperationException</code>.
    */
   void deleteAcl(IPSReference owner) throws Exception;

   /**
    * Removes the ACL objects for the object references from persistent storage. The deletion of any ACL object may
    * fail because it is locked by someone else or the caller does not have the
    * necessary permissions. If the object is not locked by the caller, an
    * attempt will be made to acquire the lock automatically.
    * <p>
    * 
    * @param references to the object whose ACLs to be deleted Never <code>null</code> and each entry cannot be
    * <code>null</code>. If empty, returns immediately.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * save, this exception will contain information about the entire operation,
    * including what was successful and what failed. You can retrieve the new
    * <code>Objects</code>s from this object as well as the exception for
    * each error condition. Generally, the exceptions will be one of the
    * following:
    * <ol>
    * <li>{@link PSLockException} - if someone else has the object locked </li>
    * <li>{@link PSAuthorizationException} - if caller doesn't have sufficient
    * privileges (rare.)</li>
    * <li>{@link PSModelException} - general problems such as communication
    * errors.</li>
    * <ol>
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   void deleteAcl(IPSReference[] references)
      throws PSMultiOperationException, PSModelException;

   /**
    * Convenience method that calls {@link #releaseLock(IPSReference[])
    * releaseLock(new IPSReference[] &#123;ref&#125;)}. If the called class
    * throws a <code>PSMultiOperationException</code>, the actual exception
    * is extracted and rethrown by this one.
    * 
    * @throws Exception The exception thrown by the referenced load method,
    * extracted from the <code>PSMultiOperationException</code>.
    */
   void releaseLock(IPSReference ref) throws Exception;

   /**
    * Unlocks an object without having to save it.
    * 
    * @param refs Never <code>null</code> and each entry must not be
    * <code>null</code>. If empty, returns immediately.
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
    * Is the referenced object locked by me in this workbench session?
    * 
    * @param ref Never <code>null</code>.
    * 
    * @return <code>true</code> if it is locked by the caller in this session
    * of the workbench, <code>false</code> otherwise.
    */
   boolean isLockedInThisSession(IPSReference ref);

   /**
    * Indicates whether this model contains a hierarchy.
    * 
    * @return <code>true</code> if {@link #getHierarchyManager(IPSReference)}
    * can return a valid manager, <code>false</code> otherwise.
    */
   boolean isHierarchyModel();

   /**
    * Checks whether the referenced object is available for editing.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @return <code>true</code> if it is locked by anyone, including the
    * caller, <code>false</code> otherwise.
    */
   boolean isLocked(IPSReference ref);

   /**
    * Returns all references that have been locked and not released during this
    * session.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   Collection<IPSReference> getLockedRefs();
   
   /**
    * Returns a set of enumerations that identify the data objects associated
    * with this model. Each of these may be returned by the
    * {@link #catalog(boolean)} method.
    * <p>
    * Most models represent 1 object type.
    * 
    * @return An unmodifiable set. Never <code>null</code> or empty.
    */
   Set<PSObjectType> getObjectTypes();

   /**
    * Convenience method that calls {@link #catalog(boolean, PSObjectType)
    * catalog(<code>false</code>, (PSObjectType[]) <code>null</code>)}.
    */
   Collection<IPSReference> catalog() throws PSModelException;

   /**
    * Convenience method that calls {@link #catalog(boolean, PSObjectType)
    * catalog(forceRefresh, (PSObjectType[]) <code>null</code>)}.
    */
   Collection<IPSReference> catalog(boolean forceRefresh)
      throws PSModelException;

   /**
    * Convenience method that calls {@link #catalog(boolean, PSObjectType[])
    * catalog(forceRefresh, new PSObjectType[] &#123;filter&#125;)} (handling
    * <code>null</code> properly).
    */
   Collection<IPSReference> catalog(boolean forceRefresh,
         PSObjectType filter)
      throws PSModelException;

   /**
    * A model may present a flat collection of objects or a hierarchical
    * collection of objects. If hierarchical, this method is equivalent to
    * calling getHierarchyManager(IPSReference).{@link 
    * IPSHierarchyManager#getChildren(com.percussion.client.IPSHierarchyNodeRef)
    * getChildren(null)}.
    * <p>
    * All catalogs are executed lazily and cached locally. The returned values
    * come from the local cache unless they are not present or the
    * <code>force</code> flag has been supplied.
    * 
    * @param forceRefresh If <code>true</code>, the server is queried even if
    * the data is available in the local cache. Supplying <code>true</code> is
    * equivalent to calling {@link #flush(IPSReference)} first.
    * 
    * @param filter If provided, all results will be of one of these types. This
    * is useful if the model supports subtypes.
    * 
    * @return Never <code>null</code>, may be empty. Caller takes ownership
    * of the collection.
    * 
    * @throws PSModelException If any problems occur on the server side.
    */
   Collection<IPSReference> catalog(boolean forceRefresh,
         PSObjectType[] filter) throws PSModelException;

   /**
    * Catalogs all objects and scans them looking for a matching id. If one is
    * found, it is returned. Otherwise, <code>null</code> is returned. If this
    * model is hierarchical, this model throws an exception. In that case, the
    * hierarchy manager should be used.
    * 
    * @param id The id of the persisted object. Never <code>null</code>.
    * 
    * @return A matching reference, if found, or <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating w/ server.
    * 
    * @throws UnsupportedOperationException If this model is hierarchical.
    */
   IPSReference getReference(IPSGuid id) throws PSModelException;

   /**
    * Like {@link IPSReference#getName()}, except if the data is cached
    * locally, the name will be retrieved from the data object rather than from
    * the supplied reference.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @return Never <code>null</code> or empty. This value could differ from the
    * value returned by the ref.
    */
   String getName(IPSReference ref);

   /**
    * Like {@link IPSReference#getLabelKey()}, except if the data is cached
    * locally, the label will be retrieved from the data object rather than from
    * the supplied reference.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @return Never <code>null</code> or empty. This value could differ from the
    * value returned by the ref.
    */
   String getLabelKey(IPSReference ref);

   /**
    * Like {@link IPSReference#getDescription()}, except if the data is
    * cached locally, the description will be retrieved from the data object
    * rather than from the supplied reference.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @return Never <code>null</code> or empty. This value could differ from the
    * value returned by the ref.
    */
   String getDescription(IPSReference ref);

   /**
    * Like {@link IPSReference#getObjectType()}, except if the data is cached
    * locally, the type will be retrieved from the data object rather than from
    * the supplied reference.
    * 
    * @param ref Never <code>null</code>. This value could differ from the
    * value returned by the ref.
    * 
    * @return Never <code>null</code> or empty.
    */
   PSObjectType getObjectType(IPSReference ref);
   
   /**
    * Catalogs all objects and scans them looking for a matching name. If one is
    * found, it is returned. Otherwise, <code>null</code> is returned. If this
    * model is hierarchical, this model throws an exception. In that case, the
    * hierarchy manager should be used.
    *
    * @return A matching reference, if found, or <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating w/ server.
    * 
    * @throws UnsupportedOperationException If this model is hierarchical.
    */
   IPSReference getReference(String name) throws PSModelException;

   /**
    * Allows callers to register for various notifications. Notifications can be
    * requested for object creation, object deletion, object save and so on. If
    * this is a hierarchical model, only 1 notification for the container is
    * sent. No notifications are sent for any ancestors of this container.
    * 
    * @param listener Who to notify. Never <code>null</code>. If the listener
    * is already registered, the new notifications replace the existing ones.
    * 
    * @param notifications An OR'd set of the
    * {@link com.percussion.client.PSModelChangedEvent} events. Supply
    * 0xffffffff to get all notifications.
    */
   void addListener(IPSModelListener listener, int notifications);

   /**
    * Removes <code>listener</code> from the notification list so that it
    * receives no further events.
    * 
    * @param listener If <code>null</code> or not registered, returns
    * silently.
    */
   void removeListener(IPSModelListener listener);

   /**
    * This method must be called every time a property in an object is changed.
    * This will cause an event to be sent to all registered listeners.
    * 
    * @param ref Never <code>null</code>.
    * @param hint This value is passed through to the event ctor. May be
    * <code>null</code> or empty.
    * 
    * @throws PSLockException If the object is not locked for editing.
    */
   void propertyChanged(IPSReference ref, Map<String, String> hint)
      throws PSLockException;

   /**
    * If this is a hierarchical model, this method will return all the tree
    * names known to the hierarchy manager.
    * 
    * @return All the names, which may be empty. An empty list does not
    * necessarily mean there are no trees for this hierarchy, just that they
    * cannot be determined at this time. The returned values can be passed to
    * {@link #getHierarchyManager(String)}.
    * 
    * @throws UnsupportedOperationException If this is not a hierarchical model.
    */
   Collection<String> getHierarchyTreeNames();

   /**
    * If the underlying model is organized in a hierarchy, this method will
    * return an object that can be used to manage the relationships between the
    * nodes. The data associated with a node should be managed by the other
    * methods in this interface.
    * 
    * @param treeName If this type supports multiple trees, this must be the
    * name of one of them. Otherwise, <code>null</code> or empty is allowed.
    * 
    * @return A valid manager if this model is represented by a tree, otherwise
    * <code>null</code> is returned.
    */
   IPSHierarchyManager getHierarchyManager(String treeName);

   /**
    * If the underlying model is organized in a hierarchy, this method will
    * return an object that can be used to manage the relationships between the
    * nodes. The data associated with a node should be managed by the other
    * methods in this interface.
    * 
    * @param node Any node from the tree. Never <code>null</code>.
    * 
    * @return A valid manager if this model is represented by a tree, otherwise
    * <code>null</code> is returned.
    */
   IPSHierarchyManager getHierarchyManager(IPSReference node);

   /**
    * Marks cache entries as dirty except for objects that are locked. This
    * includes references and actual objects that have been loaded but not
    * locked. Locked objects and their references are never flushed.
    * 
    * @param ref If provided, only entries associated with this object are
    * flushed. Otherwise, all catalog and object cache references are flushed,
    * except if the object is locked.
    */
   void flush(IPSReference ref);

   /**
    * Convenience method that calls
    * {@link #loadAcl(IPSReference[],boolean) loadAcl(new IPSReference[1] 
    * &#123;owner&#125;, lock)}. If the called class throws a
    * <code>PSMultiOperationException</code>, the actual exception is
    * extracted from the class and rethrown by this one.
    * 
    * @param owner owner of the ACL to load, must not be <code>null</code>
    * @param lock <code>true</code> to lock the object, <code>false</code>
    * otherwise.
    * @return ACL object for the supplied owner, may be <code>null</code> if
    * the object does not an ACL set yet.
    * @throws PSModelException
    */
   Object loadAcl(IPSReference owner, boolean lock)
      throws PSModelException;

   /**
    * To make changes to an ACL, call this method, make changes to the returned
    * object and call {@link #saveAcl(IPSReference, boolean)}.
    * 
    * @param owners The identifier(s) of the object for which you want the ACL.
    * This should be an object returned by one of the
    * {@link #create(PSObjectType, String) create} methods or the
    * {@link #catalog(boolean) catalog} method.
    * 
    * @param lock A flag that allows making changes to the ACL. The ACL must be
    * locked before changes can be persisted.
    * 
    * @return Array of ACL objects for each supplied reference. Each entry is
    * either the existing ACL for the referenced object, or a new default ACL if
    * one does not exist yet.
    * 
    * @throws PSMultiOperationException If one or more of the objects failed the
    * load, this exception will contain information about the entire operation,
    * including what was successful and what failed. You can retrieve the new
    * <code>Object</code>s from this object as well as the exception for each
    * error condition.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    * 
    * @throws UnsupportedOperationException If the object type of the refs
    * does not support ACLs.
    */
   Object[] loadAcl(IPSReference[] owners, boolean lock)
      throws PSMultiOperationException, PSModelException;

   /**
    * Convenience method that calls {@link #releaseAclLock(IPSReference[])
    * releaseAclLock(new IPSReference[] &#123;ref&#125;)}. If the called class
    * throws a <code>PSMultiOperationException</code>, the actual exception
    * is extracted and rethrown by this one.
    * @param owner 
    * 
    * @throws PSModelException The exception thrown by the referenced load method,
    * extracted from the <code>PSMultiOperationException</code>.
    */
   void releaseAclLock(IPSReference owner) throws PSModelException;

   /**
    * Unlocks an Acl without having to save it. The object is retrieved from the
    * server on the next load.
    * 
    * @param owners Never <code>null</code> and each entry must not be
    * <code>null</code>.
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
   void releaseAclLock(IPSReference[] owners)
      throws PSMultiOperationException, PSModelException;

   /**
    * Convenience method that calls
    * {@link #saveAcl(IPSReference[], boolean) saveAcls(new IPSReference[1] 
    * &#123;owner&#125;)}. If the called class throws a
    * <code>PSMultiOperationException</code>, the actual exception is
    * extracted from the class and rethrown by this one.
    * 
    * @param owner Never <code>null</code>.
    * @param releaseLock <code>true</code> to release lock after save,
    * <code>false</code> otherwise.
    * @throws PSModelException
    */
   void saveAcl(IPSReference owner, boolean releaseLock)
      throws PSModelException;

   /**
    * The behavior depends on whether the associated object has ever persisted.
    * If it has, the ACL is immediately persisted to the server. If not, the
    * save is queued until the object is saved, at which point the ACL is
    * immediately saved. In that case, any exceptions would be returned as part
    * of the <code>save</code> method.
    * 
    * <p>
    * All locks are automatically released.
    * 
    * @param owners Never <code>null</code>. Each entry must not be
    * <code>null</code>. If empty, returns immediately.
    * @param releaseLock <code>true</code> to release each lock after save,
    * <code>false</code> otherwise.
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
   void saveAcl(IPSReference[] owners, boolean releaseLock)
      throws PSMultiOperationException, PSModelException;
}
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

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.design.objectstore.PSControlMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Add on interface for the IPSCmsModel to add the content type specific
 * methods.
 * 
 */
public interface IPSContentTypeModel extends IPSCmsModel
{
   /**
    * Utility method to get the list of PSControlMeta objects.
    * @return List of PSControlMeta objects. May be empty, never <code>null</code>.
    * @throws PSModelException in case of error.
    */
   public List<PSControlMeta> getControls() throws PSModelException;
   
   /**
    * Utility method to retrieve the <code>PSContentEditorFieldCataloger</code>.
    * @param force if <code>true</code> then force cache refresh.
    * @param forDisplayFormat if <code>true</code> than options to retrieve the
    * catalog will be set specifically for use with the Display format editor.
    * @return cataloger that contains content editor fields.
    */
   public PSContentEditorFieldCataloger getCEFieldCatalog(boolean force, 
      boolean forDisplayFormat)
      throws PSModelException;

   /**
    * Just like the {@link IPSCmsModel#catalog(boolean)}, except types that are
    * used for internal system implementation are removed from the returned 
    * list.
    * 
    * @param force A flag that can be used to require the query to be processed
    * by calling the server rather than by fulfilling it from a cache. 
    * <code>true</code> forces a server call even if the request could have
    * been processed from the cache. 
    * 
    * @return Never <code>null</code>. Caller takes ownership of the returned
    * set.
    * 
    * @throws PSModelException If any problems communicating with the server.
    */
   public Collection<IPSReference> getUseableContentTypes(boolean force)
      throws PSModelException;
   
   /**
    * Retrieves the content types that have templates associated with them,
    * optionally limiting which content types are returned.
    * <p>
    * Any of the returned references may not be persisted yet. If saved, 
    * unpersisted refs will be removed from the set automatically.
    * 
    * @param contentTypeFilter If provided, only these content types will be
    * cataloged.
    * @param force If <code>false</code>, then data will be returned from the
    * cache if available, otherwise, a new query will be made, even if data is
    * present in the cache. If lock is <code>true</code>, then this value is
    * ignored.
    * @param lock Supply <code>true</code> if you wish to make changes,
    * otherwise, <code>false</code>.
    * 
    * @return The key is the content type ref and the value is a set of template
    * refs that are associated with that content type. Never <code>null</code>.
    * If the association list for a ctype is empty, that ctype will not appear
    * in the results. The caller takes ownership of the returned map. Changes to
    * it do not affect this object.
    * 
    * @throws PSModelException If any problems communicating with the server.
    * 
    * @throws PSLockException If <code>lock</code> is <code>true</code> but
    * the association set is already locked by someone else.
    */
   public Map<IPSReference, Collection<IPSReference>> getTemplateAssociations(
         Collection<IPSReference> contentTypeFilter, boolean force,
         boolean lock)
      throws PSModelException, PSLockException;
   
   /**
    * Saves the associations between templates and content types. The provided
    * list specifies all the associations for each provided content type. The
    * caller should obtain the existing list first using the
    * {@link #getTemplateAssociations(Collection, boolean, boolean) 
    * getTemplateAssociations} method, supplying the lock flag as
    * <code>true</code>. The links for a type are only modified if there is an
    * entry for it. To remove all links for a type, supply an empty set as the
    * value for that type key. The lock is automatically released on success or 
    * failure. Every content type that was locked must be supplied in this
    * call, even if it was not changed or the locks will not release properly.
    * 
    * @param associations Map where the key is the content type ref and the
    * value is a set of template refs that are associated with that content
    * type. If <code>null</code> or empty, nothing is done. This class copies
    * the necessary data from the map. The caller retains ownership of the
    * supplied map.
    * @throws PSMultiOperationException when errors have occurred on any of
    * the attempts to save the associations. Will contain <code>null</code>
    * for successes and the <code>Exception</code> where a failure occurred.
    * The number of entries will match the number of entries in the
    * associations map.
    */
   public void setTemplateAssociations(
         Map<IPSReference, Collection<IPSReference>> associations)
      throws PSMultiOperationException;
   
   /**
    * Determines all associations between content types and workflows and builds
    * a map that is returned, optionally limiting which workflows are included.
    * <p>
    * Any of the returned ctype references may not be persisted yet.
    * 
    * @param workflowFilter If provided, only these workflows will be cataloged.
    * @param force If <code>false</code>, then data will be returned from the
    * cache if available, otherwise, a new query will be made, even if data is
    * present in the cache.
    * 
    * @return The key is the workflow ref and the value is a set of ctype refs
    * that are associated with that workflow. Never <code>null</code>. If the
    * association list for a workflow is empty, that workflow will not appear in
    * the results. The caller takes ownership of the returned map. Changes to it
    * do not affect this object.
    * 
    * @throws PSModelException If any problems communicating with the server.
    */
   public Map<IPSReference, Collection<IPSReference>> getWorkflowAssociations(
         Collection<IPSReference> workflowFilter, boolean force)
      throws PSModelException;
   
   /**
    * Determines all workflows that the supplied content type can enter.
    * <p>
    * If you plan on calling this method with several different types, it may be
    * more efficient to load all these types first (with the <code>force</code>
    * flag you desire), then call this method with the <code>force</code> flag
    * set to <code>false</code>. This preloads the cache.
    * 
    * @param contentType A handle to the def from which to extract the links.
    * Never <code>null</code>.
    * 
    * @param force If <code>false</code>, then data will be returned from the
    * cache if available, otherwise, a new query will be made, even if data is
    * present in the cache.
    * 
    * @return Handles to each associated workflow. May be empty, never
    * <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with the server.
    */
   public Collection<IPSReference> getWorkflowAssociations(
         IPSReference contentType, boolean force)
      throws PSModelException;
}

/******************************************************************************
 *
 * [ PSTransformsHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSArchiveDetail;
import com.percussion.deployer.objectstore.PSDatasourceMap;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.deployer.objectstore.PSDbmsMapping;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.error.PSDeployException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The worker class that handles the getting and saving the transform maps for
 * external dbms credentials and objects with ids for a specific source and
 * target.
 */
public class PSTransformsHandler 
{
   /**
    * Constructs the class and initializes the dbms and id map objects for the 
    * specified source on the specified target.
    * 
    * @param target the deployment server on which maps exists, may not be 
    * <code>null</code> and must be connected.
    * @param sourceName the source server name to which dbms map needs to be 
    * initialized, may not be <code>null</code> or empty.
    * @param sourceRepository the source repository to which id map needs to be
    * initialized, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if an error happens initializing the maps.
    */
   public PSTransformsHandler(PSDeploymentServer target, String sourceName, 
      PSDbmsInfo sourceRepository) 
      throws PSDeployException
   {
      if(target == null)
         throw new IllegalArgumentException("target may not be null.");
      
      if(!target.isConnected())
         throw new IllegalArgumentException("target must be connected.");
         
      if(sourceName == null || sourceName.trim().length() == 0)
         throw new IllegalArgumentException(
            "sourceName may not be null or empty.");
            
      if(sourceRepository == null)
         throw new IllegalArgumentException(
            "sourceRepository may not be null.");

      m_dbmsMap = target.getDeploymentManager().getDbmsMap(sourceName);
      if(target.getRepositoryInfo().isSameDb(sourceRepository))
         m_idMap = null;
      else
         m_idMap = target.getDeploymentManager().getIdMap(
            sourceRepository.getDbmsIdentifier());
         
      m_target = target;
   }
   
   /**
    * Creates a copy of the source transforms handle.  The ID and DBMS maps are
    * deep copied, but the same target server reference is used.
    * 
    * @param source The source transforms handler, never <code>null</code>.
    */
   public PSTransformsHandler(PSTransformsHandler source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      m_dbmsMap = new PSDbmsMap(source.getDbmsMap().getSourceServer());
      Iterator mappings = source.getDbmsMap().getMappings();
      while (mappings.hasNext())
      {
         PSDbmsMapping mapping = (PSDbmsMapping) mappings.next();
         m_dbmsMap.addMapping(new PSDbmsMapping(mapping));
      }         

      m_idMap = null;
      if (source.getIdMap() != null)
      {
         m_idMap = new PSIdMap(source.getIdMap().getSourceServer());
         mappings = source.getIdMap().getMappings();
         while (mappings.hasNext())
         {
            PSIdMapping mapping = (PSIdMapping) mappings.next();
            m_idMap.addMapping(new PSIdMapping(mapping));
         }         
      }
      
      m_target = source.getTarget();
   }
   
   /**
    * Gets the dbms map.
    * 
    * @return the dbms map, never <code>null</code>
    */
   public PSDbmsMap getDbmsMap()
   {
      return m_dbmsMap;
   }
   
   /**
    * Gets the id map.
    * 
    * @return the id map, may be <code>null</code> if the source repository and
    * target repository matches.
    */
   public PSIdMap getIdMap()
   {
      return m_idMap;
   }
   
   /**
    * Checks whether the ids need to be mapped. If the source and target share 
    * the same repository, the ids don't need to be mapped.
    * 
    * @return <code>true</code> if the ids need to be mapped, otherwise <code>
    * false</code>
    */
   public boolean needToMapIds()
   {
      return m_idMap != null;
   }
   
   /**
    * Gets the target server for which this handler works.
    * 
    * @return the target server, never <code>null</code> and is connected.
    */
   public PSDeploymentServer getTarget()
   {
      return m_target;
   }

   /**
    * Gets the list of dbms mappings of the supplied package. Creates new 
    * mappings for the external source credentials in the archive detail that 
    * does not exist in the dbms map and adds them to map.
    * 
    * @param unMappedOnly if <code>true</code> gets the unmapped mappings only, 
    * otherwise gets all the mappings 
    * @param depElement the package for which we have to find the mappings, may 
    * not be <code>null</code>
    * @param archiveDetail the archive detail from which external dbms 
    * credentials list need to be get, may not be <code>null</code>.
    * 
    * @return the list of mappings, never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public Iterator getDBMSMappings(boolean unMappedOnly, 
      PSDeployableElement depElement, PSArchiveDetail archiveDetail)
   {
      if(depElement == null)
         throw new IllegalArgumentException("depElement may not be null.");
         
      if(archiveDetail == null)
         throw new IllegalArgumentException("archiveDetail may not be null.");
         
      Iterator extDbmsList = archiveDetail.getExternalDbmsList(depElement);
      Set<PSDbmsMapping> mappings = new HashSet<PSDbmsMapping>();
      while(extDbmsList.hasNext())
      {
         PSDatasourceMap dsMap = (PSDatasourceMap)extDbmsList.next();
         PSDbmsMapping mapping = m_dbmsMap.getMapping(dsMap.getSrc());
         if(mapping == null)
         {
            mapping = new PSDbmsMapping(dsMap);
            m_dbmsMap.addMapping(mapping);
         }
            
         //Add always if user requested all mappings, otherwise add only if the
         //mapping is not set with target info(unmapped ones)
         if (mapping != null )
         {
            String tgt = mapping.getTargetInfo();
            if (!unMappedOnly || (tgt == null || tgt.length()== 0))
               mappings.add(mapping);
         }
      }
      
      return mappings.iterator();
   }
   
   /**
    * Checks whether id map can be modified.  See {@link #needToMapIds()} for 
    * more info.
    * 
    * @throws IllegalStateException if id map can not be modifyable.
    */
   private void checkModifyIdMap()
   {
      if(!needToMapIds())
         throw new IllegalStateException("Trying to get/add/save the id map," +
            "but the id map does not exist for the current source and target");
   }

   /**
    * Gets the id mappings of the supplied package. Creates new mappings for the
    * the dependecies that supports id mappings and does not have a mapping in
    * the id map and adds them to map.
    * 
    * @param unMappedOnly if <code>true</code> it gets the mappings that are not
    * mapped to target.
    * @param depElement the package for which we have to find the mappings, may 
    * not be <code>null</code>
    * 
    * @return the list of mappings, never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws IllegalStateException if id map can not be modifyable.
    */
   public Iterator getIDMappings(boolean unMappedOnly, 
      PSDeployableElement depElement)
   {
      if(depElement == null)
         throw new IllegalArgumentException("depElement may not be null.");
      
      checkModifyIdMap();
      
      Set<PSIdMapping> mappings = new HashSet<PSIdMapping>();
      
      List<PSDependency> deps = new ArrayList<PSDependency>();
      getSupportedIdMapDependencies(depElement, deps);
      Iterator<PSDependency> idMapDeps = deps.iterator();
      while(idMapDeps.hasNext())
      {
         PSDependency dep = idMapDeps.next();
         String depId = dep.getDependencyId();
         String objType = dep.getObjectType();
         
         PSIdMapping mapping;
         if(!dep.supportsParentId())         
         {
            mapping = m_idMap.getMapping(depId, objType);
            if(mapping == null)
            {
               mapping = new PSIdMapping(
                  depId, dep.getDisplayName(), objType, true);
               m_idMap.addMapping(mapping);                  
            }
         }
         else {
            PSIdMapping parentMapping = m_idMap.getMapping(dep.getParentId(), 
               dep.getParentType());
            if(parentMapping == null) //this should not happen if we go in the order
            {
               PSDependency parent = 
                  getParentDependency(dep, dep.getParentType(), depElement);
               if(parent == null)
               {
                  throw new IllegalStateException(
                     "could not find a parent for a child " + 
                     "that supports parent id");
               }
               parentMapping = new PSIdMapping(parent.getDependencyId(), 
                  parent.getDisplayName(), parent.getObjectType(), true);
               m_idMap.addMapping(parentMapping);         
               if(!unMappedOnly || !parentMapping.isMapped())              
                  mappings.add(parentMapping);            
            }
            mapping = m_idMap.getMapping(
               depId, objType, dep.getParentId(), dep.getParentType());               
            if(mapping == null)
            {
               mapping = new PSIdMapping(
                  depId, dep.getDisplayName(), objType, dep.getParentId(),                   
                  parentMapping.getSourceName(), dep.getParentType(), true);
               m_idMap.addMapping(mapping);               
            }                           
         }
            
         //Add always if user requested all mappings, otherwise adds only  the
         //the mappings that were not mapped.
         if(!unMappedOnly || !mapping.isMapped())
            mappings.add(mapping);
      }
          
      return mappings.iterator();
   }
   
   /**
    * Traverses the child dependencies of the parent recursively to find the 
    * immediate parent of the supplied dependency (<code>dep</code>) whose type
    * is supplied parent type.
    * 
    * @param dep the dependency for which to find the parent, assumed not to be
    * <code>null</code>
    * @param parentType the type of parent dependency, assumed not to be <code>
    * null</code> or empty.
    * @param root the root dependency to search for, assumed not to be 
    * <code>null</code>
    * 
    * @return the parent dependency, may be <code>null</code> if it could not
    * find the parent.
    * @throws IllegalStateException if the found parent does not support id 
    * mapping or reached the dependency in the tree but not found the parent.
    */
   private PSDependency getParentDependency(PSDependency dep, String parentType,
      PSDependency root)
   {   
      /* Reached the actual dependency for which we are finding the parent, that 
       * means we didn't find a parent for the dependency matching the supplied 
       * parent type in the dependency tree, that should not happen so throw
       * exception.
       */
      if(dep == root) 
      {
         throw new IllegalStateException("required parent is not found");
      }
      if(root.getObjectType().equals(parentType) && 
         root.containsDependency(dep))
      {
         if(!root.supportsIDMapping())
            throw new IllegalStateException(
               "Found parent that does not support id mapping");
         return root;
      }
      
      PSDependency parent = null;
      Iterator childDeps = root.getDependencies();
      if(childDeps != null)
      {
         while(childDeps.hasNext() && parent != null)
         {
            parent = getParentDependency(dep, parentType, 
               (PSDependency)childDeps.next());            
         }
      }
      
      return parent; 
   }
   
   /**
    * Checks the supplied dependency and its child dependencies recursively and 
    * gets the list of dependencies that supports id mapping.
    * 
    * @param dependency the dependency to check for, assumed not to be <code>
    * null</code>
    * @param idMapDeps the list of dependencies that support id maps, gets 
    * updated in this method. Assumed not <code>null</code>.
    */
   private void getSupportedIdMapDependencies(PSDependency dependency, 
      List<PSDependency> idMapDeps)
   {
      if(dependency.supportsIDMapping())
         idMapDeps.add(dependency);
      Iterator childDeps = dependency.getDependencies();
      if(childDeps != null)
      {
         while(childDeps.hasNext())
         {
            getSupportedIdMapDependencies((PSDependency)childDeps.next(), 
               idMapDeps);
         }
      }
   }   
   
   /**
    * Saves the dbms map.
    * 
    * @throws PSDeployException if an error happens saving the map.
    */
   public void saveDbmsMappings() throws PSDeployException   
   {
      m_target.getDeploymentManager().saveDbmsMap(m_dbmsMap);      
   }
   
   /**
    * Saves the id map, first removing any undefined mappings (assumes the 
    * caller has validated that all required mappings have been completed).
    * 
    * @throws PSDeployException if an error happens saving the map.
    * @throws IllegalStateException if id map can not be modifyable.
    */
   public void saveIdMappings() throws PSDeployException   
   {
      checkModifyIdMap();
      m_target.getDeploymentManager().saveIdMap(getValidMappings(m_idMap));      
   }   
   
   /**
    * Creates a shallow copy of the supplied id map, copying only those mappings
    * that have a target defined, or that are marked as new.
    *  
    * @param idMap The map to copy, assumed not <code>null</code>.
    * 
    * @return The copy, never <code>null</code>.
    */
   private PSIdMap getValidMappings(PSIdMap idMap)
   {
      PSIdMap copy = new PSIdMap(idMap.getSourceServer());
      Iterator mappings = idMap.getMappings();
      while (mappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)mappings.next();
         if (mapping.isMapped())
            copy.addMapping(mapping);
      }
      
      return copy;
   }

   /**
    * Adds the specified mapping to the id map.
    * 
    * @param mapping the id mapping to add, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if mapping is <code>null</code>
    * @throws IllegalStateException if id map is not modifyable.
    */
   public void addIdMapping(PSIdMapping mapping)
   {   
      checkModifyIdMap();
      if(mapping == null)
         throw new IllegalArgumentException("mapping may not be null.");
         
      m_idMap.addMapping(mapping);
   }
   
   /**
    * Removes the specified mapping from the id map if it exists.
    * 
    * @param mapping the id mapping to remove, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if mapping is <code>null</code>
    * @throws IllegalStateException if id map is not modifyable.
    */
   public void removeIdMapping(PSIdMapping mapping)
   {   
      checkModifyIdMap();
      if(mapping == null)
         throw new IllegalArgumentException("mapping may not be null.");
         
      m_idMap.removeMapping(mapping);
   }

   /**
    * Add the specified mapping to the dbms map.
    * 
    * @param mapping the dbms mapping to add, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if mapping is <code>null</code>
    */   
   public void addDbmsMapping(PSDbmsMapping mapping)
   {   
      if(mapping == null)
         throw new IllegalArgumentException("mapping may not be null.");
         
      m_dbmsMap.addMapping(mapping);
   }
   
   /**
    * Removes the specified mapping from the dbms map if it exists.
    * 
    * @param mapping the dbms mapping to remove, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if mapping is <code>null</code>
    */
   public void removeDbmsMapping(PSDbmsMapping mapping)
   {   
      if(mapping == null)
         throw new IllegalArgumentException("mapping may not be null.");
         
      m_dbmsMap.removeMapping(mapping);
   }
   
   /**
    * Creates and adds the id mappings for the source elements of the specified 
    * type that do not exist in the id map.
    * 
    * @param sourceServer the source server to get the source elements, may not
    * be <code>null</code> and must be connected.
    * @param objectType the object/element type to get the elements, may not be
    * <code>null</code> or empty.
    * 
    * @return the list of new mappings created, never <code>null</code> may be 
    * empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws IllegalStateException if id map can not be modifyable.
    * @throws PSDeployException if an error happens.
    */
   public Iterator createUnmappedSourceMappings(PSDeploymentServer sourceServer, 
      String objectType) throws PSDeployException
   {
      checkModifyIdMap();
      
       if(objectType == null || objectType.trim().length() == 0)
         throw new IllegalArgumentException(
            "objectType may not be null or empty.");
            
      if(sourceServer == null)
         throw new IllegalArgumentException("sourceServer may not be null.");
         
      if(!sourceServer.isConnected())
         throw new IllegalArgumentException("sourceServer must be connected.");
      
      List<PSIdMapping> mappings = new ArrayList<PSIdMapping>();
      List unmappedElements = getUnmappedSourceElements(sourceServer, 
         objectType);
      Iterator iter = unmappedElements.iterator();
      while(iter.hasNext())
      {
         PSMappingElement source = (PSMappingElement)iter.next();
         PSIdMapping mapping = new PSIdMapping(source.getId(), source.getName(), 
            source.getType(), source.getParentId(), source.getParentName(), 
            source.getParentType(), true);
         m_idMap.addMapping(mapping);
         mappings.add(mapping);         
      }
      
      return mappings.iterator();
   }
   
   /**
    * Guess target for the supplied mappings.  First guesses by name and id, 
    * then if no match, guesses by name alone.  Does not guess for any
    * 
    * @param idMappings Mappings to guess, may or may not already have a target
    * set (will only guess unmapped mappings).  May not be <code>null</code>, 
    * may be empty.
    * 
    * @return A List of unmodified mappings that could not be mapped due to 
    * unmapped parents.  A dependency that supports parent ids can only be 
    * mapped if its parent id has already been mapped.  Never <code>null</code>, 
    * may be empty.  
    * 
    * @throws PSDeployException if there are any errors. 
    */
   public List guessTarget(Iterator idMappings) throws PSDeployException
   {
      List<PSIdMapping> unMatchedById = new ArrayList<PSIdMapping>();
      List<PSIdMapping> unMatchedParentList = new ArrayList<PSIdMapping>();
      while (idMappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)idMappings.next();
         if(mapping != null && !mapping.isMapped())
         {
            //guess target by name and id
            guessTarget(mapping, true);
                        
            // save those needing parent, or subsequent guess by
            // name only
            if(!mapping.isMapped())
            {
               unMatchedParentList.add(mapping);
            }
            else if(mapping.isNewObject())
            {
               unMatchedById.add(mapping);
            }
         }
      }
         
      //now guess target by name alone
      Iterator<PSIdMapping> iter = unMatchedById.iterator();
      while(iter.hasNext())
      {
         guessTarget(iter.next(), false); 
      }
          
      return unMatchedParentList;
   }
   
   /**
    * Guesses target for the provided mapping. Gets the unmapped target elements
    * for the supplied mapping and tries to find the element matching source 
    * name and/or id case insensitively. If it finds a matching element, the 
    * target of mapping is set with that element, otherwise the source element 
    * is set as new object in the mapping. If the source element has a parent id
    * and if the parent id is not mapped, it simply returns without guessing 
    * target for the mapping.
    * 
    * @param mapping the mapping that is not mapped, may not be <code>null
    * </code>
    * @param mustMatchById supply <code>true</code> to must match by id also, 
    * otherwise <code>false</code>.
    * 
    * @throws IllegalArgumentException if mapping is <code>null</code>
    * @throws IllegalStateException if id map can not be modifyable.
    * @throws PSDeployException if exception happens cataloging.
    */
   public void guessTarget(PSIdMapping mapping, boolean mustMatchById) 
      throws PSDeployException
   {
      checkModifyIdMap();
      
      if(mapping == null)
         throw new IllegalArgumentException("mapping may not be null.");
         
      if( mapping.getParentType() != null && 
         !m_idMap.isMapped(mapping.getSourceParentId(), mapping.getParentType()))
      {
         return; //simply return because you don't know about parent.
      }
      
      PSMappingElement targetEl = guessTarget(
         mapping.getObjectType(), mapping.getSourceName(), 
         mapping.getSourceId(), mapping.getParentType(), 
         mapping.getSourceParentId(), mustMatchById);
         
      if(targetEl != null)
      {
         mapping.setIsNewObject(false);      
         mapping.setTarget(targetEl.getId(), targetEl.getName(), 
            targetEl.getParentId(), targetEl.getParentName());
      }      
      else
         mapping.setIsNewObject(true);
   }
   
   /**
    * Guesses the target for the supplied source element. If the source element
    * has a parent that should be mapped for guessing the target. Gets the 
    * unmapped target elements for the supplied source element's and its parent
    * and tries to find the element matching source name (case-insensitive) and 
    * id. If it did not find match by id, it matches by name alone.
    * 
    * @param source the source element, may not be <code>null</code>
    * 
    * @return the matching target element, may be <code>null</code> if it did
    * not find a match.
    * 
    * @throws IllegalArgumentException if source is <code>null</code>.
    * @throws IllegalStateException if the id map can not be modifyable or if
    * supplied source element has a parent and the parent is not mapped.
    * @throws PSDeployException if an error happens.
    */
   public PSMappingElement guessTarget(PSMappingElement source) 
      throws PSDeployException
   {
      checkModifyIdMap();
      
      if(source == null)
         throw new IllegalArgumentException("source may not be null.");
         
      if( source.hasParent() &&
         !m_idMap.isMapped(source.getParentId(), source.getParentType()))
      {
         throw new IllegalStateException(
            "The parent element must be mapped if the supplied source element" +
            " has parent to guess the target.");
      }

      return guessTarget(source.getType(), source.getName(), source.getId(),
         source.getParentType(), source.getParentId(), false);
   }
   
   /**
    * Guesses the target element for the supplied source element by matching 
    * names case insensitively. If it finds more than one match, then tries to 
    * get the one matching id, if it didn't find any, gets the first one in the
    * list. If <code>mustMatchById</code> is <code>true</code> it always tries
    * to match by id also.
    * 
    * @param elementType the element type of the source, assumed not to be 
    * <code>null</code> or empty.
    * @param sourceName the name of the source element, assumed not to be 
    * <code>null</code> or empty.
    * @param sourceId the id of the source element, assumed not to be 
    * <code>null</code> or empty.
    * @param parentType the parent type of the source element type, may be 
    * <code>null</code> if source does not have a parent.
    * @param parentId the parent id of the source element, may be <code>null
    * </code> if source does not have a parent.
    * @param mustMatchById supply <code>true</code> to must match by id also, 
    * otherwise <code>false</code>.
    * 
    * @return the target element, may be <code>null</code> if it didn't find a
    * target by matching name case insensitively.
    * 
    * @throws PSDeployException if an error happens getting target elements.
    */
   private PSMappingElement guessTarget(String elementType, String sourceName, 
      String sourceId, String parentType, String parentId, 
      boolean mustMatchById) 
      throws PSDeployException
   {
      List targetElements = getUnmappedTargetElements(elementType, 
         parentType, parentId);      
               
      List<PSMappingElement> matchingElements = 
         new ArrayList<PSMappingElement>();
      //guess by name
      Iterator elements = targetElements.iterator();
      while(elements.hasNext())
      {
         PSMappingElement target = (PSMappingElement)elements.next();
         if( target.getName().equalsIgnoreCase(sourceName) )
            matchingElements.add(target);
      }

      PSMappingElement target = null;      
      if(!matchingElements.isEmpty())
      {
         elements = matchingElements.iterator();

         while(elements.hasNext() && target == null)
         {
            PSMappingElement tg = (PSMappingElement)elements.next();
            if(tg.getId().equals(sourceId))
               target = tg;  
         }
         if(target == null && !mustMatchById)
            target = matchingElements.get(0);
      }
      
      return target;
   }
   
   /**
    * Gets the unmapped target elements for the specified element (object) type
    * and the source parent id (if it has) from the current id mappings.
    * 
    * @param objectType the object/element type to get the elements, may not be
    * <code>null</code> or empty.
    * @param parentType the parent element type, may not be <code>null</code> or
    * empty if the supplied <code>objectType</code> has parent type, otherwise
    * it must be <code>null</code>
    * @param sourceParentId the parent id of the source, may not be <code>null
    * </code> or empty if the <code>parentType</code> is not <code>null</code>
    * 
    * @return the list of target (<code>PSMappingElement</code>) elements, never
    * <code>null</code>, may be empty when all target elements are mapped or no
    * existing target elements or supplied parent id (element) is being added to
    * server.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if an exception happens cataloging target 
    * elements.
    * @throws IllegalStateException if id map does not exist.
    */
   public List getUnmappedTargetElements(String objectType, 
      String parentType, String sourceParentId) 
      throws PSDeployException
   {
      checkModifyIdMap();
      
      if(objectType == null || objectType.trim().length() == 0)
         throw new IllegalArgumentException(
            "objectType may not be null or empty.");

      if(m_target.hasParentType(objectType))
      { 
         if(parentType == null || parentType.trim().length()==0)
         {
            throw new IllegalArgumentException("parentId may not be null or "
               + "empty if objectType supports parent type");
         }
      }
      else {
         if(parentType != null)
         {
            throw new IllegalArgumentException("parentType must be null if " +
               "objectType does not support parent type");
         }
      }
      
      if(parentType != null)
      {
         if(sourceParentId == null || sourceParentId.trim().length() == 0)
         {
            throw new IllegalArgumentException("sourceParentId may not be null"
               + "or empty if objectType supports parent type.");
         }
      }
      else if(sourceParentId != null)
      {
         throw new IllegalArgumentException("sourceParentId must be null if " + 
            "objectType does not support parent type");      
      }
      
      List<PSMappingElement> targetElements = new ArrayList<PSMappingElement>();
            
      String targetParentId = null;
      if(sourceParentId != null)
      {
         PSIdMapping parentMapping = 
            m_idMap.getMapping(sourceParentId, parentType);      
         if(parentMapping == null || !parentMapping.isMapped())
            throw new IllegalArgumentException(
               "sourceParentId must be mapped before mapping child");
                        
         targetParentId = parentMapping.getTargetId();
         //The mapping have been mapped as new object, so this should return 
         //an empty list.
         if(targetParentId == null)
            return targetElements;
      }
 
      Set<String> usedElements = new HashSet<String>();       
      Iterator mappings = m_idMap.getMappings();
      while(mappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)mappings.next();
         if(mapping.getObjectType().equals(objectType) && 
            mapping.getTargetId() != null)
         {         
            usedElements.add(mapping.getTargetId() + 
            (mapping.getParentType() == null ? "" : 
            "-" + mapping.getTargetParentId())
            );         
         }
      }

      Iterator iter = m_target.getElementsByType(objectType);      
      while(iter.hasNext())
      {
         PSMappingElement element = (PSMappingElement)iter.next();     
         String uniqueID = element.getId();
         if(element.hasParent())
            uniqueID += "-" + element.getParentId();
         if( !usedElements.contains(uniqueID) && 
            ( targetParentId == null || 
            targetParentId.equals(element.getParentId()) ) )
         {
            targetElements.add(element);
         }
      }
      return targetElements;
   }
   
   /**
    * Checks whether the supplied type has a parent type or not.
    * 
    * @param type the element type to check, may not be <code>null</code> or 
    * empty.
    * 
    * @return <code>true</code> if the supplied type has parent type, otherwise
    * <code>false</code>
    * 
    * @throws PSDeployException if error happens cataloging parent types.
    */
   public boolean hasParentType(String type) throws PSDeployException
   {
      return m_target.hasParentType(type);
   }   
   
   /**
    * Gets the source elements that are not used to map for the specified  
    * element(object) type from the current id mappings.
    * 
    * @param sourceServer the source server that should be used to catalog 
    * source elements, may not be <code>null</code> and must be connected.
    * @param objectType the object/element type to get the elements, may not be
    * <code>null</code> or empty.
    * 
    * @return the list of source (<code>PSMappingElement</code>) elements, never
    * <code>null</code>, may be empty if all source elements are mapped.
    * 
    * @throws PSDeployException if an exception happens cataloging source
    * elements.
    * @throws IllegalStateException if id map does not exist.
    */
   public List getUnmappedSourceElements(PSDeploymentServer sourceServer, 
      String objectType) throws PSDeployException
   {
      checkModifyIdMap();
      
      if(objectType == null || objectType.trim().length() == 0)
         throw new IllegalArgumentException(
            "objectType may not be null or empty.");
            
      if(sourceServer == null)
         throw new IllegalArgumentException("sourceServer may not be null.");
         
      if(!sourceServer.isConnected())
         throw new IllegalArgumentException("sourceServer must be connected.");
 
      Set<String> usedElements = new HashSet<String>();       
      Iterator mappings = m_idMap.getMappings();
      while(mappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)mappings.next();
         if(mapping.getObjectType().equals(objectType))
         {
            usedElements.add(mapping.getSourceId() + 
               (mapping.getParentType() == null ? "" : 
               "-" + mapping.getSourceParentId()) );
         }
      }
      
      List<PSMappingElement> sourceElements = new ArrayList<PSMappingElement>();
      
      Iterator iter = sourceServer.getElementsByType(objectType);      
      while(iter.hasNext())
      {
         PSMappingElement element = (PSMappingElement)iter.next();       
         String uniqueID = element.getId();
         if(element.hasParent())
            uniqueID += "-" + element.getParentId();
         if(!usedElements.contains(uniqueID))
            sourceElements.add(element);
      }
      
      return sourceElements;
   }
   
   
   /**
    * The dbms map on the target for a source server, initialized in the 
    * constructor and never <code>null</code> after that. May be modified by a
    * call to <code>saveTransforms(Iterator, Iterator)</code>.
    */
   private PSDbmsMap m_dbmsMap;
   
   /**
    * The id map on the target for a source repository, initialized in the 
    * constructor and may be <code>null</code> if target repository matches with
    * source repository. Does not need to map. May be modified by a call to
    * <code>saveTransforms(Iterator, Iterator)</code>.
    */
   private PSIdMap m_idMap;
   
   /**
    * The target deployment server on which the maps exist, initialized in the 
    * constructor and never <code>null</code> or modified after that.
    */
   private PSDeploymentServer m_target;
}

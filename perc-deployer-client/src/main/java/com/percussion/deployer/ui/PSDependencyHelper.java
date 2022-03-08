/******************************************************************************
 *
 * [ PSDependencyHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableObject;
import com.percussion.error.PSDeployException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The helper class to handle loading the dependencies.
 */
public class PSDependencyHelper
{
   /**
    * Gets the list of the <code>PSApplicationIDTypes</code> for the supplied
    * dependency and its all local dependencies recursively that supports id 
    * types. Does not modify the dependency structure of the supplied dependency
    * in any way.
    * 
    * @param dep the dependency for which id types need to get, may not be 
    * <code>null</code>.
    * @param server the deployment server on which this dependency exists, may
    * not be <code>null</code> and must be connected. Used to get id types for
    * the dependencies.
    * @param incompleteOnly supply <code>true</code> to get id types that are 
    * incomplete only or <code>false</code> to get all id types.
    * 
    * @return the list, never <code>null</code>, may be empty if neither the
    * supplied dependency nor its local children supports id types or does not
    * have incomplete id types and asked for incomplete id types only.
    * 
    * @throws PSDeployException if the id types request fails.
    */
   public static Iterator getIDTypes(PSDependency dep, 
      PSDeploymentServer server, boolean incompleteOnly) 
      throws PSDeployException
   {
      if(dep == null)
         throw new IllegalArgumentException("dep may not be null.");
         
      if(server == null)
         throw new IllegalArgumentException("server may not be null.");
         
      if(!server.isConnected())
         throw new IllegalArgumentException("server must be connected.");   

      // since we may be working from a descriptor in which there are 
      // dependencies that have changed since the tree was built, we need to 
      // check id types of any included dependency, or any dependency that has
      // child dependencies loaded, as these are the dependencies that will have 
      // been checked when the descriptor was first created.           
      return getIDTypes(getAllCheckedDepsAndAncestors(dep), server, 
         incompleteOnly);
   }
   
   /**
    * Gets the list of the <code>PSApplicationIDTypes</code> for the supplied
    * list of dependencies and their all local dependencies recursively that 
    * supports id  types. Does not modify the dependency structure of the 
    * supplied dependencies in any way. Please see {@link 
    * #getIDTypes(PSDependency, PSDeploymentServer, boolean) for more 
    * information about the missing parameter descriptions.
    * 
    * @param deps the list of dependencies to get id types, may not be <code>
    * null</code>, may be empty.
    * 
    * @return the list, never <code>null</code>, may be empty if neither the
    * supplied dependencies nor its local children supports id types or does not
    * have incomplete id types and asked for incomplete id types only.
    * 
    * @throws IllegalArgumentException if any parameter is invalid
    * @throws PSDeployException if the id types request fails.
    */
   public static Iterator getIDTypes(Iterator deps, 
      PSDeploymentServer server, boolean incompleteOnly) 
      throws PSDeployException
   {
      if(deps == null)
         throw new IllegalArgumentException("deps may not be null.");
         
      if(server == null)
         throw new IllegalArgumentException("server may not be null.");
         
      if(!server.isConnected())
         throw new IllegalArgumentException("server must be connected.");   

      Set idTypesLocalDeps = new HashSet();         
      while(deps.hasNext())      
      {
         PSDependency dep = (PSDependency)deps.next();
         PSDependency clonedDep = (PSDependency)dep.clone();
         loadAllLocalDependencies(clonedDep, server);
         getSupportedIDTypesDependencies(clonedDep, idTypesLocalDeps);
      }
      
      List idTypes = new ArrayList();
      if(!idTypesLocalDeps.isEmpty())
      {
         Iterator depIDTypes = server.getDeploymentManager().getIdTypes(
            idTypesLocalDeps.iterator());
         while(depIDTypes.hasNext())
         {
            PSApplicationIDTypes types = 
               (PSApplicationIDTypes)depIDTypes.next();
            if(types.getResourceList(incompleteOnly).hasNext())
               idTypes.add(types);
         }
      }

      return idTypes.iterator();
   }
   
   /**
    * Checks recursively the supplied dependency and its local dependencies to
    * get the dependencies that support id types. Assumes the local dependencies
    * are loaded for the supplied dependency and its local children recursively.
    * 
    * @param dep the top-level dependency to check, assumed not to be <code>null
    * </code>
    * @param idTypesLocalDeps The list of local dependencies that support the
    * id types, assumed not <code>null</code>, gets updated in this method.
    */
   private static void getSupportedIDTypesDependencies(PSDependency dep, 
      Set idTypesLocalDeps)
   {  
      if(dep.supportsIdTypes())
         idTypesLocalDeps.add(dep);
             
      Iterator localDeps = dep.getDependencies(PSDependency.TYPE_LOCAL);
      while(localDeps.hasNext())
      {
         PSDependency dependency = (PSDependency)localDeps.next();
         getSupportedIDTypesDependencies(dependency, idTypesLocalDeps);
      }
   }
   
   /**
    * Loads all local dependencies recursively for the supplied dependency if 
    * they are not loaded yet until it reaches the end of the dependency tree 
    * (no more local children for the dependency). During this process this 
    * loads all dependencies of this dependency and each child local dependency,
    * so any dependencies other than local may not be correct if the id types of
    * the dependencies that support id types are not identified yet.
    * 
    * @param dep the dependency to get local dependencies, may not be <code>null
    * </code>
    * @param server the deployment server on which this dependency exists, may
    * not be <code>null</code> and must be connected. 
    */
   public static void loadAllLocalDependencies(PSDependency dep, 
      PSDeploymentServer server) throws PSDeployException
   {
      if(dep == null)
         throw new IllegalArgumentException("dep may not be null.");
         
      if(server == null)
         throw new IllegalArgumentException("server may not be null.");
         
      if(!server.isConnected())
         throw new IllegalArgumentException("server must be connected.");   
         
      if(dep.getDependencies(PSDependency.TYPE_LOCAL) == null)
         server.getDeploymentManager().loadDependencies(dep);
         
      Iterator localDeps = dep.getDependencies(PSDependency.TYPE_LOCAL);
      while(localDeps.hasNext())
      {
         PSDependency dependency = (PSDependency)localDeps.next();
         loadAllLocalDependencies(dependency, server);
      }
   }
   
   /**
    * Gets all loaded dependencies and ancestors of the root element and its
    * child dependencies that are included, or that have children loaded, 
    * recursively.
    * 
    * @param dep the root dependency to recurse, assumed not <code>null</code>.
    *  
    * @return The list of dependencies, never <code>null</code>, may be 
    * empty, will not contain duplicates, will include the root element, but no
    * deployable elements found within the tree.  Will only include those 
    * dependencies marked as include or that have children loaded.
    */
   private static Iterator getAllCheckedDepsAndAncestors(PSDependency dep)
   {
      Set deps = new HashSet();
      deps.add(dep);         
      getAllCheckedDepsAndAncestors(dep, deps);
         
      return deps.iterator();
   }
      
   /**
    * Worker method for {@link #getAllCheckedDepsAndAncestors(PSDependency)}.
    * 
    * @param dep the root dependency to recurse, assumed not to be <code>null
    * </code>
    * @param deps the list of dependencies to which all loaded dependency 
    * objects are to be added, assumed not <code>null</code>.
    */
   private static void getAllCheckedDepsAndAncestors(PSDependency dep, Set deps)
   {
      if(dep instanceof PSDeployableObject && (dep.isIncluded() || 
         dep.getDependencies() != null))
      {
         deps.add(dep);
      }
            
      Iterator dependencies = dep.getDependencies();
      if(dependencies != null)
      {
         while(dependencies.hasNext())
         {
            getAllCheckedDepsAndAncestors(
               (PSDependency)dependencies.next(), deps);
         }
      }
         
      Iterator ancs = dep.getAncestors();
      if(ancs != null)
      {
         while(ancs.hasNext())
         getAllCheckedDepsAndAncestors((PSDependency)ancs.next(), deps);
      }
   }   
}

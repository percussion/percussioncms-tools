/******************************************************************************
 *
 * [ PSDeploymentServer.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.catalog.PSCataloger;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.design.objectstore.PSFeatureSet;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.error.PSException;
import com.percussion.utils.collections.PSIteratorUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
* The class which holds the information for each registered server.
*/
public class PSDeploymentServer implements IPSDataChangeNotifier
{
   /**
    * Constructs this object with its registration details. This object need to
    * be provided with the deployment manager using {@link
    * #createDeploymentManager(
    * PSDeploymentServerConnection)} to fulfill
    * the deployment requests.
    *
    * @param registration The server registration object, may not be <code>null
    * </code>
    * @param repository the repository this server is using, may not be
    * <code>null</code>
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSDeploymentServer(PSServerRegistration registration,
      OSDbmsInfo repository)
   {
      if(registration == null)
         throw new IllegalArgumentException("registration may not be null.");

      if(repository == null)
         throw new IllegalArgumentException("repository may not be null");

      m_serverRegistration = registration;
      m_repository = repository;
   }

   /**
    * Gets the registration object for this server.
    *
    * @return the object, never <code>null</code>
    */
   public PSServerRegistration getServerRegistration()
   {
      return m_serverRegistration;
   }

   /**
    * Gets the string representation of this object. See {@link #getServerName()
    * } for the format of the string.
    *
    * @return the server name, never <code>null</code> or empty.
    */
   public String toString()
   {
      return getServerName();
   }

   /**
    * Returns the boolean indicating whether the Rx server for which this object
    * stores information is licensed for Multi-Server Manager.
    *
    * @return <code>true</code> if the Rx server for which this object stores
    * information is licensed for Multi-Server Manager, <code>false</code>
    * otherwise.
    */
   public boolean isServerLicensed()
   {
      return m_bLicensed;
   }

   /**
    * Sets whether the Rx server for which this object stores information is
    * licensed for Multi-Server Manager.
    *
    * @param licensed <code>true</code> if the Rx server for which this object
    * stores information is licensed for Multi-Server Manager,
    * <code>false</code> otherwise.
    */
   public void setServerLicensed(boolean licensed)
   {
      m_bLicensed = licensed;
   }

   /**
    * Gets the cataloged result set of descriptors. If the descriptors are not
    * cataloged, catalogs the descriptors and caches them.
    *
    * @param forceCatalog if <code>true</code> always catalogs, otherwise
    * catalogs only if the descriptors are not cataloged.
    *
    * @return the descriptors, never <code>null</code> may be empty.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public PSCatalogResultSet getDescriptors(boolean forceCatalog)
      throws PSDeployException
   {
      checkConnected();

      if(m_descriptors == null || forceCatalog)
      {
         try {
            m_descriptors = getSortedSet(m_cataloger.catalog(
               PSCataloger.TYPE_REQ_DESCRIPTORS, null));
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }
      return m_descriptors;
   }

   /**
    * Call this method to inform the object that the descriptors on the server
    * has changed and should update its list by recataloging to the server. This
    * updates the listeners that the data has changed.
    *
    * @throws IllegalStateException if the server is not connected.
    * @throws PSDeployException if an exception happens cataloging.
    */
   public void updateDescriptors()
      throws PSDeployException
   {
      checkConnected();
      getDescriptors(true);
      notifyDataChangeListeners();
   }

   /**
    * Call this method to inform the object that the archives are installed or
    * some archive logs on the server are deleted and should update its list by
    * recataloging to the server. This updates its listeners that the data has
    * changed. When archive are installed since the available elements in this
    * server and server file system will change, this clears the cache of
    * elements by type.
    *
    * @throws IllegalStateException if the server is not connected.
    * @throws PSDeployException if an exception happens cataloging.
    */
   public void updateArchives()
      throws PSDeployException
   {
      checkConnected();
      m_archives = null;
      notifyDataChangeListeners();

      //clear available elements
      m_typeElements = new HashMap();
   }

   /**
    * Call this method to inform the object that the packages are installed or
    * some package logs on the server are deleted and should update its list by
    * recataloging to the server. This updates its listeners that the data has
    * changed.
    *
    * @throws IllegalStateException if the server is not connected.
    * @throws PSDeployException if an exception happens cataloging.
    */
   public void updatePackages()
      throws PSDeployException
   {
      checkConnected();
      m_packages = null;
      notifyDataChangeListeners();
   }

   /**
    * Checks that the server is connected or not.
    *
    * @throws IllegalStateException if the server is not connected.
    */
   private void checkConnected()
   {
      if(!isConnected())
         throw new IllegalStateException(
            "server is not connected to perform the requested operation.");
   }

   /**
    * Converts the exception into a <code>PSDeployException</code> and throws.
    *
    * @param e the exception to convert, assumed not <code>null</code>
    *
    * @throws PSDeployException after converting the exception.
    */
   private void handleException(Exception e) throws PSDeployException
   {
      if(e instanceof PSDeployException)
         throw (PSDeployException)e;
      else if(e instanceof PSException)
         throw new PSDeployException((PSException)e);
      else
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Notifies all view data change listeners registered with the object as data
    * changed.
    */
   private void notifyDataChangeListeners()
   {
      Iterator listeners = m_listeners.iterator();
      while(listeners.hasNext())
      {
         ((IPSViewDataChangeListener)listeners.next()).dataChanged(this);
      }
   }

   /**
    * Gets the cataloged result set of archive logs. If the archive logs are not
    * yet cataloged, catalogs and caches them.
    *
    * @param forceCatalog if <code>true</code> always catalogs, otherwise
    * catalogs only if the archive logs are not cataloged.
    *
    * @return the archive logs, never <code>null</code> may be empty.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public PSCatalogResultSet getArchives(boolean forceCatalog)
      throws PSDeployException
   {
      checkConnected();

      if(m_archives == null || forceCatalog)
      {
        try {
            m_archives = getSortedSet(m_cataloger.catalog(
               PSCataloger.TYPE_REQ_ARCHIVES, null));
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }

      return m_archives;
   }

   /**
    * Gets the cataloged result set of package logs. If the package logs are not
    * yet cataloged, catalogs and caches them.
    *
    * @param forceCatalog if <code>true</code> always catalogs, otherwise
    * catalogs only if the package logs are not cataloged.
    *
    * @return the package logs, never <code>null</code> may be empty.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public PSCatalogResultSet getPackages(boolean forceCatalog)
      throws PSDeployException
   {
      checkConnected();

      if(m_packages == null || forceCatalog)
      {
         try {
            m_packages = getSortedSet(m_cataloger.catalog(
               PSCataloger.TYPE_REQ_PACKAGELOGS, null));
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }

      return m_packages;
   }

   /**
    * Gets the list of directories and files under the supplied directory path
    * from rhythmyx server file system.
    *
    * @param dirPath the directory path relative to rhythmyx root, supply <code>
    * null</code> or empty to get all directories or files in rhythmyx root
    * directory.
    *
    * @return the result set of file structure with file/directory path as id
    * and name as display text, never <code>null</code> may be empty.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public PSCatalogResultSet getServerFileSystem(String dirPath)
      throws PSDeployException
   {
      checkConnected();

      if(dirPath != null && dirPath.trim().length() == 0)
         dirPath = null;

      PSCatalogResultSet result = null;

      try
      {
         Properties props = new Properties();
         if(dirPath != null)
            props.put("directory", dirPath);

         result = getFileSortedSet(m_cataloger.catalog(
            PSCataloger.TYPE_REQ_USER_DEP, props));
      }
      catch(Exception e)
      {
         handleException(e);
      }

      return result;
   }

   /**
    * Gets the list of elements on the server of the specified type. If the
    * elements are not yet cataloged for the supplied type, catalogs and caches
    * them. Uses case-sensitive comparison to check whether the supplied type is
    * already cataloged or not. Uses {@link
    * PSCataloger#TYPE_REQ_TYPE_OBJECTS}
    * for cataloging. See the link for more information. If the supplied type
    * has parent type, then the elements of the type will be all child elements
    * of each element of parent type.
    *
    * @param type the type of elements, may not be <code>null</code> or empty
    * and must be a valid element type.
    *
    * @return the list of <code>PSMappingElement</code>s, never <code>null
    * </code>, may be empty.
    *
    * @throws IllegalArgumentException if type is not valid.
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public Iterator getElementsByType(String type)
      throws PSDeployException
   {
      if(type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      checkConnected();

      getLiteralIDTypes();
      Iterator idTypes = m_idTypes.getResults();
      boolean isValid = false;
      while(idTypes.hasNext())
      {
         PSCatalogResult idType = (PSCatalogResult)idTypes.next();
         if(idType.getID().equals(type))
         {
            isValid = true;
            break;
         }
      }

      if(!isValid)
         throw new IllegalArgumentException("type is not valid.");

      if(m_typeElements.get(type) == null)
      {
         try {
            List elements = new ArrayList();
            if(hasParentType(type))
            {
               String parentType = getParentType(type);
               Iterator iter = getElementsByType(parentType);
               while(iter.hasNext())
               {
                  PSMappingElement parentElement = (PSMappingElement)iter.next();
                  Iterator childElements = getDeploymentManager().
                     getDependencies(type, parentElement.getId());
                  while(childElements.hasNext())
                  {
                     PSDependency element = (PSDependency)childElements.next();
                     PSMappingElement childElement = new PSMappingElement(
                        element.getObjectType(), element.getDependencyId(),
                        element.getDisplayName());
                     childElement.setParent(parentElement.getType(),
                        parentElement.getId(),
                        parentElement.getName());
                     elements.add(childElement);
                  }
               }
            }
            else {
               Properties props = new Properties();
               props.put("type", type);

               Iterator iter = m_cataloger.catalog(
                  PSCataloger.TYPE_REQ_TYPE_OBJECTS, props).getResults();
               while(iter.hasNext())
               {
                  PSCatalogResult element = (PSCatalogResult)iter.next();
                  PSMappingElement mapElement = new PSMappingElement(
                        type, element.getID(), element.getDisplayText());
                  elements.add(mapElement);
               }
            }
            if(!elements.isEmpty())
               Collections.sort(elements);

            m_typeElements.put( type, elements );
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }

      return ((List)m_typeElements.get(type)).iterator();
   }

   /**
    * Finds out whether supplied type has any parent type or not.
    *
    * @param type the type to check, may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if has parent type, otherwise <code>false</code>
    *
    * @throws IllegalArgumentException if type is invalid.
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if an error happens getting parent types.
    */
   public boolean hasParentType(String type) throws PSDeployException
   {
      if(type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty.");

      checkConnected();

      Map parentTypes = getParentTypes();
      return parentTypes.containsKey(type);
   }

   /**
    * Gets the parent type of supplied type.
    *
    * @param type the type, may not be <code>null</code> or empty.
    *
    * @return the parent type, may be <code>null</code> if it does not have any
    * parent type, never empty.
    *
    * @throws IllegalArgumentException if type is invalid.
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if an error happens getting parent types.
    */
   public String getParentType(String type) throws PSDeployException
   {
      if(type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty.");

      checkConnected();

      Map parentTypes = getParentTypes();
      return (String)parentTypes.get(type);
   }

   /**
    * Gets the map of child types to parent types. See {@link
    * PSDeploymentManager#getParentTypes() } for
    * more information.
    *
    * @return the map, never <code>null</code>, may be empty.
    *
    * @throws PSDeployException if an error happens getting parent types.
    */
   private Map getParentTypes() throws PSDeployException
   {
      if(m_parentTypes == null)
         m_parentTypes = getDeploymentManager().getParentTypes();

      return m_parentTypes;
   }

   /**
    * Gets the deployment manager who manages the connection and any other
    * requests to the server.
    *
    * @return the manager, may be <code>null</code> if the server is not
    * connected.
    */
   public PSDeploymentManager getDeploymentManager()
   {
      return m_deploymentManager;
   }

   /**
    * Creates new deployment manager for this server to service the requests.
    * If this server already has a deployment manager, use {@link #disconnect}
    * to disconnect from the old deployment manager and call this to create new
    * deployment manager. Notifies all listeners that are interested in the
    * server status.
    *
    *
    * @param connection the connection to server, may not be
    * <code>null</code> and is a valid connection.
    *
    * @throws IllegalArgumentException if connection is invalid.
    * @throws IllegalStateException if the deployment manager already exists.
    */
   public void createDeploymentManager(PSDeploymentServerConnection connection)
   {
      if(connection == null)
         throw new IllegalArgumentException("connection may not be null.");

      if(!connection.isConnected())
         throw new IllegalArgumentException(
            "connection is not a valid connection.");

      if(m_deploymentManager != null)
         throw new IllegalStateException("Deployment Manager already exists.");

      m_deploymentManager = new PSDeploymentManager(connection);
      m_cataloger = m_deploymentManager.getCataloger();
      notifyDataChangeListeners();
   }

   /**
    * Disconnects from the server and clears the deployment manager and all the
    * cataloged members. Ignores if the server is already disconnected. Notifies
    * all listeners that are interested in the server status.
    */
   public void disconnect() throws PSDeployException
   {
      if(m_deploymentManager != null)
      {
         try
         {
            m_deploymentManager.getConnection().disconnect();
         }
         finally
         {
            m_deploymentManager = null;
            m_descriptors = null;
            m_archives = null;
            m_packages = null;
            m_cmsElementTypes = null;
            m_customElementTypes = null;
            m_idTypes = null;
            m_datasources = null;
            m_typeElements = new HashMap();
            m_parentTypes = null;
            notifyDataChangeListeners();
         }
      }
   }

   /**
    * Checks whether this server is connected or not. If the deployment manager
    * is <code>null</code> the server is not connected.
    *
    * @return <code>true</code> if the server is connected, otherwise <code>
    * false</code>
    */
   public boolean isConnected()
   {
      return m_deploymentManager != null;
   }

   /**
    * Gets the name of the server this is representing in the format
    * 'server:port'.
    *
    * @return the server name, never <code>null</code> or empty.
    */
   public String getServerName()
   {
      return m_serverRegistration.getServer() + ":" +
         m_serverRegistration.getPort();
   }


   /**
    * Gets the repository information used by the server.
    *
    * @return the repository info, never <code>null</code>
    */
   public OSDbmsInfo getRepositoryInfo()
   {
      return m_repository;
   }

   /**
    * Sets the repository information on the server.
    */
   public void setRepositoryInfo(OSDbmsInfo repository)
   {
      if(repository == null)
         throw new IllegalArgumentException("repository may not be null");

      m_repository = repository;
   }

   /**
    * Gets the cataloged result set of cms element types. If the element types
    * are not yet cataloged, catalogs and caches them. The cataloged result set
    * will have <code>PSCatalogResult</code>s for all cms element types with the
    * element type as id and element type name as displaytext. Uses {@link
    * PSCataloger#TYPE_REQ_DEPLOY_TYPES} for
    * cataloging. See the link for more information.
    *
    * @return the cms element types, never <code>null</code>, it may have empty
    * results.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public PSCatalogResultSet getCMSElementTypes()
      throws PSDeployException
   {
      checkConnected();

      if(m_cmsElementTypes == null)
      {
        try {
            m_cmsElementTypes = getSortedSet(m_cataloger.catalog(
               PSCataloger.TYPE_REQ_DEPLOY_TYPES, null));
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }

      return m_cmsElementTypes;
   }

   /**
    * Gets the cataloged result set of custom element types. If the element
    * types are not yet cataloged, catalogs and caches them. The cataloged
    * result set will have <code>PSCatalogResult</code>s for all element types
    * with the element type as id and element type name as displaytext. Uses
    * {@link PSCataloger#TYPE_REQ_CUSTOM_TYPES}
    * for cataloging. See the link for more information.
    *
    * @return the custom element types, never <code>null</code>, it may have
    * empty results.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public PSCatalogResultSet getCustomElementTypes()
      throws PSDeployException
   {
      checkConnected();

      if(m_customElementTypes == null)
      {
        try {
            m_customElementTypes = getSortedSet(m_cataloger.catalog(
               PSCataloger.TYPE_REQ_CUSTOM_TYPES, null));
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }

      return m_customElementTypes;
   }

   /**
    * Gets the display name of the supplied package type.
    *
    * @param type the package/element type, may not be <code>null</code> and
    * must be one of the cms element types or the custom type.
    *
    * @return the display name of the type, never <code>null</code> or empty.
    * @throws IllegalArgumentException if type is invalid.
    * @throws PSDeployException if an error happens getting types.
    */
   public String getPackageTypeDisplayName(String type) throws PSDeployException
   {
      checkConnected();

      if(type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty.");

      Iterator types = getCMSElementTypes().getResults();
      List customType = new ArrayList();
      customType.add(new PSCatalogResult(
         IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM,
         IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM));
      types = PSIteratorUtils.joinedIterator(types, customType.iterator());

      while(types.hasNext())
      {
         PSCatalogResult idType = (PSCatalogResult)types.next();
         if(idType.getID().equals(type))
            return idType.getDisplayText();
      }

      throw new IllegalArgumentException("invalid type");
   }

   /**
    * Gets the cataloged result set of literal id types. If the literal id
    * types are not yet cataloged, catalogs and caches them. The cataloged
    * result set will have <code>PSCatalogResult</code>s for all types
    * with the id type as id and id type name as displaytext. Uses {@link
    * PSCataloger#TYPE_REQ_LITERAL_ID_TYPES}
    * for cataloging. See the link for more information.
    *
    * @return the literal id types, never <code>null</code>, it may have
    * empty results.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public PSCatalogResultSet getLiteralIDTypes() throws PSDeployException
   {
      checkConnected();

      if(m_idTypes == null)
      {
        try {
            m_idTypes = getSortedSet(m_cataloger.catalog(
               PSCataloger.TYPE_REQ_LITERAL_ID_TYPES, null));
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }

      return m_idTypes;

   }

   /**
    * Gets the display name of the supplied type.
    *
    * @param type the id/element type, may not be <code>null</code> or empty and
    * must be one of the valid element types.
    *
    * @return the display name of supplied type, never <code>null</code> or
    * empty.
    * @throws IllegalArgumentException if type is invalid.
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if an error happens getting types.
    */
   public String getTypeDisplayName(String type) throws PSDeployException
   {
      checkConnected();

      if(type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty.");

      Iterator types = getLiteralIDTypes().getResults();
      while(types.hasNext())
      {
         PSCatalogResult idType = (PSCatalogResult)types.next();
         if(idType.getID().equals(type))
            return idType.getDisplayText();
      }

      throw new IllegalArgumentException("invalid type");
   }

   /**
    * Gets the cataloged result set of database drivers. If the drivers are not
    * yet cataloged, catalogs and caches them. The cataloged result set will
    * have <code>PSCatalogResult</code>s for all drivers with the driver name as
    * id and displaytext. Uses {@link
    * PSCataloger#TYPE_REQ_DATASOURCES}
    * for cataloging. See the link for more information.
    *
    * @return the driver, never <code>null</code>, it may have
    * empty results.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   public PSCatalogResultSet getDataSources() throws PSDeployException
   {
      checkConnected();

      if(m_datasources == null)
      {
        try {
            m_datasources = getSortedSet(m_cataloger.catalog(
               PSCataloger.TYPE_REQ_DATASOURCES, null));
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }

      return m_datasources;
   }

   /**
    * Gets the featureset from the server to use to determine if optional 
    * features are supported.  Caches the featureset after the first call.
    *
    * @return The featureset, never <code>null</code>.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if there are any errors.
    */
   public PSFeatureSet getFeatureSet() throws PSDeployException
   {
      checkConnected();
      if (m_featureSet == null)
      {
         m_featureSet = m_deploymentManager.getFeatureSet();
      }
      
      return m_featureSet;
   }
   
   /**
    * Sorts the results of given cataloged result set by the result's
    * displaytext lexicographically.
    *
    * @param set the result set to be sorted, assumed not to be <code>null
    * </code>, modified in this method.
    *
    * @return the set after sorting, never <code>null</code>
    */
   private PSCatalogResultSet getSortedSet(PSCatalogResultSet set)
   {
     
      Iterator results = set.getResults();
      if(results.hasNext())
      {
         List sortedResults = new ArrayList();
         while(results.hasNext())
         {
            sortedResults.add(results.next());
            results.remove();
         }
         Collections.sort(sortedResults, ms_sortComp);
         results = sortedResults.iterator();
         while(results.hasNext())
            set.addResult((PSCatalogResult)results.next());
      }
      return set;
   }
   
   /**
    * Sorts the results of given cataloged File / Directory result set 
    * by the result's DisplayText lexicographically (Folders first then files).
    *
    * @param set the result set to be sorted, assumed not to be <code>null
    * </code>, modified in this method.
    *
    * @return the set after sorting, never <code>null</code>
    */
   private PSCatalogResultSet getFileSortedSet(PSCatalogResultSet set)
   {
     
      Iterator results = set.getResults();
      if(results.hasNext())
      {
         List sortedDirectoryResults = new ArrayList();
         List sortedFileResults = new ArrayList();
         
         //Add to Directory list or File list
         while(results.hasNext())
         {
            PSCatalogResult record = (PSCatalogResult) results.next();
            if (record.getID().endsWith("/"))
            {
               sortedDirectoryResults.add(record);
            }
            else
            {
               sortedFileResults.add(record);
            }
            results.remove();
         }
         
         //Sort 
         Collections.sort(sortedDirectoryResults, ms_sortComp);
         Collections.sort(sortedFileResults, ms_sortComp);
         
         //Add Sorted Directories
         results = sortedDirectoryResults.iterator();
         while(results.hasNext())
            set.addResult((PSCatalogResult)results.next());
         
         //Add Sorted Files
         results = sortedFileResults.iterator();
         while(results.hasNext())
            set.addResult((PSCatalogResult)results.next());
         
      }
      return set;
   }

   //implements interface method
   public void addDataChangeListener(IPSViewDataChangeListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener may not be null.");

      m_listeners.add(listener);
   }

   //implements interface method
   public void removeDataChangeListener(IPSViewDataChangeListener listener)
   {
      m_listeners.remove(listener);
   }

   /**
    * The list of <code>IPSViewDataChangeListener</code>s that are interested in
    * the change of data of this object, initialized to an empty list and
    * listeners are added/removed by calls to {@link
    * #addDataChangeListener(IPSViewDataChangeListener) addDataChangeListener}/
    * {@link #removeDataChangeListener(IPSViewDataChangeListener)
    * removeDataChangeListener} methods.
    */
   private List m_listeners = new ArrayList();

   /**
    * The cataloged result set of the descriptors that exists on this server,
    * <code>null</code> until first call to <code>getDescriptors(boolean)</code>
    * or if the server is not connected.
    */
   private PSCatalogResultSet m_descriptors = null;

   /**
    * The cataloged result set of the archive logs that exists on this server,
    * <code>null</code> until first call to <code>getArchives(boolean)</code>
    * or if the server is not connected.
    */
   private PSCatalogResultSet m_archives = null;

   /**
    * The cataloged result set of the package logs that exists on this server,
    * <code>null</code> until first call to <code>getPackages(boolean)</code>
    * or if the server is not connected.
    */
   private PSCatalogResultSet m_packages = null;

   /**
    * The cataloged result set of the cms element types that are supported by
    * the server, <code>null</code> until first call to <code>
    * getCMSElementTypes()</code> or if the server is not connected.
    */
   private PSCatalogResultSet m_cmsElementTypes = null;

   /**
    * The cataloged result set of the custom element types that are supported by
    * the server, <code>null</code> until first call to <code>
    * getCustomElementTypes()</code> or if the server is not connected.
    */
   private PSCatalogResultSet m_customElementTypes = null;

   /**
    * The cataloged result set of the id types that are supported by the server,
    * <code>null</code> until first call to <code>getLiteralIDTypes()</code> or
    * if the server is not connected.
    */
   private PSCatalogResultSet m_idTypes = null;

   /**
    * The cataloged result set of the datasources that are supported by the
    * server, <code>null</code> until first call to <code>getDataSources()
    * </code> or if the server is not connected.
    */
   private PSCatalogResultSet m_datasources = null;

   /**
    * The map of elements by type, with type as key (<code>String</code>) and
    * the  <code>PSCatalogResultSet</code> with results representing the
    * elements of that type.  Initialized to an empty map and entries get added
    * by calls to {@link #getElementsByType(String) }. Never <code>null</code>.
    */
   private Map m_typeElements = new HashMap();

   /**
    * The map of child-parent element types with key as child type (<code>String
    * </code>), and value as parent type (<code>String</code>). <code>null
    * </code> until first call to <code>getParentTypes()</code>. Reset to <code>
    * null</code> in  <code>disconnect()</code> method.
    */
   private Map m_parentTypes = null;

   /**
    * The deployment manager that will be used to create requests to a server.
    * Created in <code>createDeploymentManager(PSDeploymentServerConnection)
    * </code> and set to <code>null</code> in the <code>disconnect()</code>
    * method.
    */
   private PSDeploymentManager m_deploymentManager = null;

   /**
    * The cataloger used to catalog the data from the server, initialized in
    * <code>createDeploymentManager(PSDeploymentServerConnection)</code> and is
    * valid only if the deployment manager is not <code>null</code>.
    */
   private PSCataloger m_cataloger = null;

   /**
    * The repository info that this server is using, initialized in constructor
    * and never <code>null</code> after that. May be modified through the
    * <code>setRepositoryInfo(PSDbmsInfo)</code> method.
    */
   private OSDbmsInfo m_repository;

   /**
    * The server registration details, initialized in the constructor and never
    * <code>null</code> after that. May be modified using mutator methods of
    * this object.
    */
   private PSServerRegistration m_serverRegistration;
   /**
    * <code>true</code> if the Rx server for which this object stores
    * information is licensed for Multi-Server Manager, <code>false</code>
    * otherwise. Initialized to <code>true</code>, set using the
    * <code>setServerLicensed()</code> method
    */
   private boolean m_bLicensed = true;
   
   /**
    * The featureset from the server to use to determine if optional features
    * are supported.  <code>null</code> until first call to 
    * <code>createDeploymentManager</code>, never <code>null</code> after that.
    */
   private PSFeatureSet m_featureSet = null;
   
   /**
    * Comparator for PSCatalogResult.
    * Compares based on DisplayText and ignores case.
    */
   private static final Comparator<PSCatalogResult> ms_sortComp =
      new Comparator<PSCatalogResult>() {
      public int compare(PSCatalogResult r1, PSCatalogResult r2) {
         return (r1.getDisplayText().compareToIgnoreCase(r2.getDisplayText()));
      }
   };
}

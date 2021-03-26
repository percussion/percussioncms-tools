/******************************************************************************
 *
 * [ DirectoryServiceData.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.IPSGroupProviderInstance;
import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.util.PSCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class serves as data model for all directory service editors. It holds
 * all changes made in directory service editors until they are saved to the
 * server configuration.
 */
@SuppressWarnings(value={"unchecked"})
public class DirectoryServiceData
{
   /**
    * Constructs a new directory service data model with empty collections.
    */
   public DirectoryServiceData()
   {
   }
   
   /**
    * Construct a new directory service data model. All data is cloned from the 
    * supplied server configuration to allow editing all data without saving
    * it to disk.
    * 
    * @param config the server configuration from which to clone all directory 
    *    service data, not <code>null</code>.
    */
   public DirectoryServiceData(PSServerConfiguration config)
   {
      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
   
      // initialize authentications
      Iterator authentications = config.getAuthentications();
      while (authentications.hasNext())
         m_authentications.add(
            ((PSAuthentication) authentications.next()).clone());
      
      // initialize directories
      Iterator directories = config.getDirectories();
      while (directories.hasNext())
         m_directories.add(((PSDirectory) directories.next()).clone());
      
      // initialize directory sets
      Iterator directorySets = config.getDirectorySets();
      while (directorySets.hasNext())
         m_directorySets.add(((PSDirectorySet) directorySets.next()).clone());
      
      // initialize role providers
      Iterator roleProviders = config.getRoleProviders();
      while (roleProviders.hasNext())
         m_roleProviders.add(((PSRoleProvider) roleProviders.next()).clone());
      
      // initialize group providers
      Iterator groupProviders = config.getGroupProviderInstances().iterator();
      while (groupProviders.hasNext())
      {
         m_groupProviders.add((
            (IPSGroupProviderInstance) groupProviders.next()).clone());
      }
   }
   
   /**
    * Add all data from the supplied directory service data to this one.
    * 
    * @param data the directory service data from which to add all data to
    *    this object, not <code>null</code>.
    */
   public void addAll(DirectoryServiceData data)
   {
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
         
      addAllAuthentications(data.getAuthentications());
      addAllDirectories(data.getDirectories());
      addAllDirectorySets(data.getDirectorySets());
      addAllRoleProviders(data.getRoleProviders());
      addAllGroupProviders(data.getGroupProviders());
   }
   
   /**
    * Get the collection of all specified authentications. This includes all
    * authentications in the current server configuration and all newly created
    * authentications since the last save.
    * 
    * @return a collection of <code>PSAuthentication</code> objects with all 
    *    known authentications, never <code>null</code>, may be empty. Changes
    *    to the returned collection will affect this object.
    */
   public Collection getAuthentications()
   {
      return m_authentications;
   }
   
   /**
    * Get the requested authentication.
    * 
    * @param name the name of the authentication we are looking for, not
    *    <code>null</code> or empty. The name comparison is case sensitive.
    * @return the requested authentication, <code>null</code> if not found.
    */
   public PSAuthentication getAuthentication(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
      
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      Iterator authentications = getAuthentications().iterator();
      while (authentications.hasNext())
      {
         PSAuthentication authentication = (PSAuthentication) authentications.next();
         if (authentication.getName().equals(name))
            return authentication;
      }
      
      return null;
   }
   
   /**
    * Get a list of authentication names currently in the directory service 
    * data.
    * 
    * @return a list with all authentication names currently in the directory 
    *    service data, never <code>null</code>, may be empty.
    */
   protected List getAuthenticationNames()
   {
      List names = new ArrayList();
      
      Iterator authentications = getAuthentications().iterator();
      while (authentications.hasNext())
      {
         PSAuthentication authentication = 
            (PSAuthentication) authentications.next();
         
         names.add(authentication.getName());
      }
      
      return names;
   }
   
   /**
    * Add a new authentication.
    * 
    * @param authentication the new authentication to be added to the directory
    *    service data, not <code>null</code>.
    */
   public void addAuthentication(PSAuthentication authentication)
   {
      if (authentication == null)
         throw new IllegalArgumentException("authentication cannot be null");
         
      m_authentications.add(authentication);
   }
   
   /**
    * Add all supplied authentications to this directory service model.
    * 
    * @param authentications the authentications to be added, not 
    *    <code>null</code>, may be empty.
    */
   public void addAllAuthentications(Collection authentications)
   {
      if (authentications == null)
         throw new IllegalArgumentException("authentications cannot be null");
         
      m_authentications.addAll(authentications);
   }
   
   /**
    * Remove the authentication for the supplied name, does nothing if no
    * authentication was found for the provided name.
    * 
    * @param name the name of the authentication to be removed from the 
    *    directory service data, not <code>null</code> or empty. The name 
    *    comparison is case sensitive.
    */
   public void removeAuthentication(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      PSAuthentication authentication = getAuthentication(name);
      if (authentication != null)
         m_authentications.remove(authentication);
   }
   
   /**
    * Remove the supplied authentication.
    * 
    * @param authentication the authentication to be removed from the directory
    *    service data, not <code>null</code>.
    */
   public void removeAuthentication(PSAuthentication authentication)
   {
      if (authentication == null)
         throw new IllegalArgumentException("authentication cannot be null");
         
      m_authentications.remove(authentication);
   }
   
   /**
    * Get the collection of all specified directories. This includes all
    * directories in the current server configuration and all newly created
    * directories since the last save.
    * 
    * @return a collection of <code>PSDirectory</code> objects with all 
    *    known directories, never <code>null</code>, may be empty. Changes
    *    to the returned collection will affect this object.
    */
   public Collection getDirectories()
   {
      return m_directories;
   }
   
   /**
    * Get the requested directory.
    * 
    * @param name the name of the directory we are looking for, not
    *    <code>null</code> or empty. The name comparison is case sensitive.
    * @return the requested directory, <code>null</code> if not found.
    */
   public PSDirectory getDirectory(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
      
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      Iterator directories = getDirectories().iterator();
      while (directories.hasNext())
      {
         PSDirectory directory = (PSDirectory) directories.next();
         if (directory.getName().equals(name))
            return directory;
      }
      
      return null;
   }
   
   /**
    * Get a list of directory names currently in the directory service 
    * data.
    * 
    * @return a list with all directory names currently in the directory 
    *    service data, never <code>null</code>, may be empty.
    */
   protected List getDirectoryNames()
   {
      List names = new ArrayList();
      
      Iterator directories = getDirectories().iterator();
      while (directories.hasNext())
      {
         PSDirectory directory = (PSDirectory) directories.next();
         
         names.add(directory.getName());
      }
      
      return names;
   }
   
   /**
    * Add a new directory.
    * 
    * @param directory the new directory to be added to the directory service 
    *    data, not <code>null</code>.
    */
   public void addDirectory(PSDirectory directory)
   {
      if (directory == null)
         throw new IllegalArgumentException("directory cannot be null");
         
      m_directories.add(directory);
   }
   
   /**
    * Add all supplied directories to this directory service model.
    * 
    * @param directories the directories to be added, not <code>null</code>, 
    *    may be empty.
    */
   public void addAllDirectories(Collection directories)
   {
      if (directories == null)
         throw new IllegalArgumentException("directories cannot be null");
         
      m_directories.addAll(directories);
   }
   
   /**
    * Remove the directory for the supplied name, does nothing if no
    * directory was found for the provided name.
    * 
    * @param name the name of the directory to be removed from the 
    *    directory service data, not <code>null</code> or empty. The name 
    *    comparison is case sensitive.
    */
   public void removeDirectory(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      PSDirectory directory = getDirectory(name);
      if (directory != null)
         m_directories.remove(directory);
   }
   
   /**
    * Remove the supplied directory.
    * 
    * @param directory the directory to be removed from the directory service 
    *    data, not <code>null</code>.
    */
   public void removeDirectory(PSDirectory directory)
   {
      if (directory == null)
         throw new IllegalArgumentException("directory cannot be null");
         
      m_directories.remove(directory);
   }
   
   /**
    * Get the collection of all specified directory sets. This includes all
    * directory sets in the current server configuration and all newly created
    * directory sets since the last save.
    * 
    * @return a collection of <code>PSDirectorySet</code> objects with all 
    *    known directory sets, never <code>null</code>, may be empty. Changes
    *    to the returned collection will affect this object.
    */
   public Collection getDirectorySets()
   {
      return m_directorySets;
   }
   
   /**
    * Get the requested directory set.
    * 
    * @param name the name of the directory set we are looking for, not
    *    <code>null</code> or empty. The name comparison is case sensitive.
    * @return the requested directory set, <code>null</code> if not found.
    */
   public PSDirectorySet getDirectorySet(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
      
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      Iterator directorySets = getDirectorySets().iterator();
      while (directorySets.hasNext())
      {
         PSDirectorySet directorySet = (PSDirectorySet) directorySets.next();
         if (directorySet.getName().equals(name))
            return directorySet;
      }
      
      return null;
   }
   
   /**
    * Get a list of directory set names currently in the directory service 
    * data.
    * 
    * @return a list with all directory set names currently in the directory 
    *    service data, never <code>null</code>, may be empty.
    */
   protected List getDirectorySetNames()
   {
      List names = new ArrayList();
      
      Iterator directorySets = getDirectorySets().iterator();
      while (directorySets.hasNext())
      {
         PSDirectorySet directorySet = (PSDirectorySet) directorySets.next();
         
         names.add(directorySet.getName());
      }
      
      return names;
   }
   
   /**
    * Add a new directory set.
    * 
    * @param directorySet the new directory set to be added to the directory 
    *    service data, not <code>null</code>.
    */
   public void addDirectorySet(PSDirectorySet directorySet)
   {
      if (directorySet == null)
         throw new IllegalArgumentException("directorySet cannot be null");
         
      m_directorySets.add(directorySet);
   }
   
   /**
    * Add all supplied directory sets to this directory service model.
    * 
    * @param directorySets the directory sets to be added, not 
    *    <code>null</code>, may be empty.
    */
   public void addAllDirectorySets(Collection directorySets)
   {
      if (directorySets == null)
         throw new IllegalArgumentException("directorySets cannot be null");
         
      m_directorySets.addAll(directorySets);
   }
   
   /**
    * Remove the directory set for the supplied name, does nothing if no
    * directory set was found for the provided name.
    * 
    * @param name the name of the directory set to be removed from the 
    *    directory service data, not <code>null</code> or empty. The name 
    *    comparison is case sensitive.
    */
   public void removeDirectorySet(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      PSDirectorySet directorySet = getDirectorySet(name);
      if (directorySet != null)
         m_directorySets.remove(directorySet);
   }
   
   /**
    * Remove the supplied directory set.
    * 
    * @param directorySet the directory set to be removed from the directory 
    *    service data, not <code>null</code>.
    */
   public void removeDirectorySet(PSDirectorySet directorySet)
   {
      if (directorySet == null)
         throw new IllegalArgumentException("directorySet cannot be null");
         
      m_directorySets.remove(directorySet);
   }
   
   /**
    * Get the collection of all specified role providers. This includes all
    * role providers in the current server configuration and all newly created
    * role providers since the last save.
    * 
    * @return a collection of <code>PSRoleProvider</code> objects with all 
    *    known role providers, never <code>null</code>, may be empty. Changes
    *    to the returned collection will affect this object.
    */
   public Collection getRoleProviders()
   {
      return m_roleProviders;
   }
   
   /**
    * Get the requested role provider.
    * 
    * @param name the name of the role provider we are looking for, not
    *    <code>null</code> or empty. The name comparison is case sensitive.
    * @return the requested role provider, <code>null</code> if not found.
    */
   public PSRoleProvider getRoleProvider(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
      
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      Iterator roleProviders = getRoleProviders().iterator();
      while (roleProviders.hasNext())
      {
         PSRoleProvider roleProvider = (PSRoleProvider) roleProviders.next();
         if (roleProvider.getName().equals(name))
            return roleProvider;
      }
      
      return null;
   }
   
   /**
    * Get a list of role provider names currently in the directory service 
    * data.
    * 
    * @return a list with all role provider names currently in the directory 
    *    service data, never <code>null</code>, may be empty.
    */
   protected List getRoleProviderNames()
   {
      List names = new ArrayList();
      
      Iterator roleProviders = getRoleProviders().iterator();
      while (roleProviders.hasNext())
      {
         PSRoleProvider roleProvider = (PSRoleProvider) roleProviders.next();
         
         names.add(roleProvider.getName());
      }
      
      return names;
   }
   
   /**
    * Add a new role provider.
    * 
    * @param roleProvider the new role provider to be added to the directory 
    *    service data, not <code>null</code>.
    */
   public void addRoleProvider(PSRoleProvider roleProvider)
   {
      if (roleProvider == null)
         throw new IllegalArgumentException("roleProvider cannot be null");
         
      m_roleProviders.add(roleProvider);
   }
   
   /**
    * Add all supplied role providers to this directory service model.
    * 
    * @param roleProviders the role providers to be added, not 
    *    <code>null</code>, may be empty.
    */
   public void addAllRoleProviders(Collection roleProviders)
   {
      if (roleProviders == null)
         throw new IllegalArgumentException("roleProviders cannot be null");
         
      m_roleProviders.addAll(roleProviders);
   }
   
   /**
    * Remove the supplied role provider.
    * 
    * @param roleProvider the role provider to be removed from the directory 
    *    service data, not <code>null</code>.
    */
   public void removeRoleProvider(PSRoleProvider roleProvider)
   {
      if (roleProvider == null)
         throw new IllegalArgumentException("roleProvider cannot be null");
         
      m_roleProviders.remove(roleProvider);
   }
   
   /**
    * Remove the role provider for the supplied name, does nothing if no
    * role provider was found for the provided name.
    * 
    * @param name the name of the role provider to be removed from the 
    *    directory service data, not <code>null</code> or empty. The name 
    *    comparison is case sensitive.
    */
   public void removeRoleProvider(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
         
      PSRoleProvider roleProvider = getRoleProvider(name);
      if (roleProvider != null)
         m_roleProviders.remove(roleProvider);
   }

   /**
    * Get all group providers in this data.
    * 
    * @return The collection of {@link IPSGroupProviderInstance} objects, never
    * <code>null</code>.
    */
   public Collection getGroupProviders()
   {
      return m_groupProviders;
   }
   
   /**
    * Add all supplied group providers to this directory service model.
    * 
    * @param groupProviders the providers to be added, not <code>null</code>, 
    *    may be empty.
    */   
   public void addAllGroupProviders(Collection groupProviders)
   {
      if (groupProviders == null)
         throw new IllegalArgumentException("groupProviders may not be null");
      
      m_groupProviders.addAll(groupProviders);
   }
   
   /**
    * Set the list of cataloger configs contained by this object, replaces any
    * previously held list.  Use {@link #getCatalogerConfigs()} to obtain a
    * reference to the list held by this object.
    * 
    * @param configs The list to set, may not be <code>null</code>, may be
    * empty.
    */
   public void setCatalogerConfigs(List<PSCatalogerConfig> configs)
   {
      if (configs == null)
         throw new IllegalArgumentException("configs may not be null");
      
      m_catalogerConfigs = configs;
   }
   
   /**
    * Obtain a reference to the list of cataloger configs held by this object.
    * 
    * @return The list, never <code>null</code>, may be empty.
    */
   public List<PSCatalogerConfig> getCatalogerConfigs()
   {
      return m_catalogerConfigs;
   }
   
   /**
    * A collection of <code>PSAuthentication</code> objects known. Initialized
    * during construction, never <code>null</code>, may be empty.
    */
   private Collection m_authentications = new PSCollection(
      PSAuthentication.class);
      
   /**
    * A collection of <code>PSDirectory</code> objects known. Initialized
    * during construction, never <code>null</code>, may be empty.
    */
   private Collection m_directories = new PSCollection(PSDirectory.class);
   
   /**
    * A collection of <code>PSDirectorySet</code> objects known. Initialized
    * during construction, never <code>null</code>, may be empty.
    */
   private Collection m_directorySets = new PSCollection(PSDirectorySet.class);
   
   /**
    * A collection of <code>PSRoleProvider</code> objects known. Initialized
    * during construction, never <code>null</code>, may be empty.
    */
   private Collection m_roleProviders = new PSCollection(PSRoleProvider.class);
   
   /**
    * A collection of <code>IPSGroupProviderInstance</code> objects known. 
    * Initialized during construction, never <code>null</code>, may be empty.
    */
   private Collection m_groupProviders = new PSCollection(
      IPSGroupProviderInstance.class);

   /**
    * List of cataloger configs, never <code>null</code>, may be empty.  
    */
   private List<PSCatalogerConfig> m_catalogerConfigs = 
      new ArrayList<PSCatalogerConfig>();
}

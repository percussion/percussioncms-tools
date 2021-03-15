/******************************************************************************
 *
 * [ PSPackagerClientModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.model;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.catalog.PSCataloger;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.IPSDeployJobControl;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSUserDependency;
import com.percussion.error.PSException;
import com.percussion.packager.ui.PSPackagerClient;
import com.percussion.packager.ui.PSPackagerMainFrame;
import com.percussion.packager.ui.PSResourceUtils;
import com.percussion.packager.ui.data.PSElementCategory;
import com.percussion.packager.ui.data.PSElementFilter;
import com.percussion.packager.ui.data.PSElementNode;
import com.percussion.packager.ui.data.PSPackageDependency;
import com.percussion.packager.ui.data.PSPackageDescriptorMetaInfo;
import com.percussion.packager.ui.data.PSProgressStatus;
import com.percussion.packager.ui.data.PSServerRegistration;
import com.percussion.packager.ui.managers.IPSServerConnectionListener;
import com.percussion.packager.ui.managers.PSServerConnectionManager;
import com.percussion.security.PSAuthenticationFailedException;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is a UI model that represents all data that will be viewed and
 * modified in the packager client UI.
 * @author erikserating
 *
 */
public class PSPackagerClientModel 
{
   
   
   
   public PSPackagerClientModel()
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      connMgr.addServerConnectionListener(new IPSServerConnectionListener()
         {

            public void connected(String hostport)
            {
               //no-op as we want to go through this classes
               //connect method instead.
               
            }

            public void disconnected(String hostport)
            {
                firePackagerClientModelChangeEvent(
                   ChangeEventTypes.SERVER_DISCONNECT);              
            }
         
         });
   }
   
   /**
    * Add a new packager client model listener if it has not
    * already been registered.
    * @param listener cannot be <code>null</code>.
    */
   public void addPackagerClientModelListener(
      IPSPackagerClientModelListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_listeners.contains(listener))
         m_listeners.add(listener);
   }
   
   /**
    * Build the package for the descriptor specified by the passed in
    * info object.
    * @param info the descriptor meta info object. Cannot be
    * <code>null</code>.
    * @throws PSDeployException 
    * @throws PSDeployException
    */
   public void build(PSPackageDescriptorMetaInfo info) throws PSDeployException
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null.");
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSDeploymentManager dm = connMgr.getDeploymentManager();
      dispatchProgress(PSProgressStatus.TYPE.START,
         getResourceString("status.msg.building"), null);
      try
      {

         if(m_cancelCurrentOperation)
            return;
         
         // Need to recalculate depends and save descriptor
         PSExportDescriptor descriptor = save();
         if(m_cancelCurrentOperation)
            return;

         // Send config files to server if we have any
         dispatchProgress(PSProgressStatus.TYPE.UPDATE, null,
            getResourceString("status.note.uploading.config"));
         if(StringUtils.isNotBlank(m_metaInfo.getConfigDefPath()))
         {
            File config = new File(m_metaInfo.getConfigDefPath());
            if(!config.exists() || !config.isFile())
            {
               Object[] args = new Object[]{m_metaInfo.getConfigDefPath()};
               throw new PSDeployException(
                  IPSDeploymentErrors.CONFIG_DOES_NOT_EXIST, args);
            }
            IPSDeployJobControl job = 
               dm.copyConfigToServer(m_metaInfo.getName() + "_configDef", config);
            while(job.getStatus() != 100 && job.getStatus() != -1) // Wait for file upload to complete
            {
               try
               {
                  Thread.sleep(250);
                  if(m_cancelCurrentOperation)
                     job.cancelDeployJob();
               }
               catch (InterruptedException ignore){}
            }
            if(job.getStatus() == -1)
            {
               firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR,
                  job.getStatusMessage());
               return;
            }
         }
         
         if(m_cancelCurrentOperation)
            return;
         if(StringUtils.isNotBlank(m_metaInfo.getLocalConfigPath()))
         {
            File config = new File(m_metaInfo.getLocalConfigPath());
            if(!config.exists() || !config.isFile())
            {
               Object[] args = new Object[]{m_metaInfo.getLocalConfigPath()};
               throw new PSDeployException(
                  IPSDeploymentErrors.CONFIG_DOES_NOT_EXIST, args);
            }
            IPSDeployJobControl job = 
               dm.copyConfigToServer(m_metaInfo.getName() + "_localConfig", config);
            while(job.getStatus() != 100 && job.getStatus() != -1) // Wait for file upload to complete
            {
               try
               {
                  Thread.sleep(250);
                  if(m_cancelCurrentOperation)
                     job.cancelDeployJob();
               }
               catch (InterruptedException ignore){}
            }
            if(job.getStatus() == -1)
            {
               firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR,
                  job.getStatusMessage());
               return;
            }
         }      
         
         if(m_cancelCurrentOperation)
            return;
         // Invoke the export job
          dispatchProgress(PSProgressStatus.TYPE.UPDATE, null,
             getResourceString("status.note.invoking.build"));       
          IPSDeployJobControl exJob = dm.runExportJob(descriptor);
          while(exJob.getStatus() != 100 && exJob.getStatus() != -1) // Wait for export job to complete
          {
             try
             {
                dispatchProgress(PSProgressStatus.TYPE.UPDATE, null,
                   exJob.getStatusMessage());
                Thread.sleep(250);
                if(m_cancelCurrentOperation)
                   exJob.cancelDeployJob();
             }
             catch (InterruptedException ignore){}
          }
          if(exJob.getStatus() == -1)
          {
             firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR,
                exJob.getStatusMessage());
             return;
          }
          
          if(m_cancelCurrentOperation)
             return;
         // Save package to local system
          File targetFile = null;
          if(m_localPkgDir != null)
          {
             dispatchProgress(PSProgressStatus.TYPE.UPDATE, null,
                getResourceString("status.note.copying.pkg"));
             if(!m_localPkgDir.exists())
                m_localPkgDir.mkdirs();
             targetFile = new File(m_localPkgDir, 
                descriptor.getName() + IPSDeployConstants.ARCHIVE_EXTENSION);
             IPSDeployJobControl sJob = 
               dm.copyArchiveFromServer(descriptor.getName(), targetFile) ;
             while(sJob.getStatus() != 100 && sJob.getStatus() != -1) // Wait for  job to complete
             {
                try
                {
                   Thread.sleep(250);
                   if(m_cancelCurrentOperation)
                      sJob.cancelDeployJob();
                }
                catch (InterruptedException ignore){}
             }
             if(sJob.getStatus() == -1)
             {
                firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR,
                   sJob.getStatusMessage());
                return;
             }
          }
         String fName = targetFile == null ? "" : targetFile.getName();
         String confirmMsg = 
            MessageFormat.format(getResourceString("info.build.confirm"), 
               new Object[]{fName, m_localPkgDir.getAbsolutePath()}); 
         firePackagerClientModelChangeEvent(ChangeEventTypes.BUILD_DESCRIPTOR);
         firePackagerClientModelChangeEvent(ChangeEventTypes.INFO, confirmMsg);
      }
      finally
      {
         dispatchProgress(PSProgressStatus.TYPE.END, null, null);
      }
   }
   
   /**
    * Connect to specified Percussion CM Server.
    * @param server cannot be <code>null</code>.
    * @throws Exception 
    */
   public void connect(PSServerRegistration server) 
      throws Exception
   {
      if(server == null)
         throw new IllegalArgumentException("server cannot be null.");
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      
         try
         {
            if(connMgr.getConnection() != null)
               disconnect();
            if(!connMgr.initConnection(server))
               loadDataIsland();
         }catch(PSAuthenticationFailedException e){
            JOptionPane.showMessageDialog(PSPackagerClient.getFrame(), PSResourceUtils.getCommonResourceString("invalidCredentials"));
            server.setCredentials("","");
            this.connect(server);
         }
         catch (Exception e)
         {
            if(e.getMessage().startsWith("An unexpected error has occurred:"))
            {
               throw new PSDeployException(
                  IPSDeploymentErrors.UNABLE_TO_CONNECT_TO_SERVER);
            }
            else
            {
               throw e;
            }
         }         
       
      
   }
   
   /**
    * Create a configuration def shell file based on the selected
    * descriptor.
    * @param info the descriptor meta info, cannot be <code>null</code>. 
    * @throws PSDeployException
    */
   public void createConfigDef(PSPackageDescriptorMetaInfo info) 
      throws PSDeployException
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null");
      if(m_exportDir == null)
         throw new IllegalArgumentException("target dir cannot be null.");
      if(!m_exportDir.exists())
         m_exportDir.mkdirs();      
      File targetFile = new File(m_exportDir, info.getName() + "_configDef.xml");
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSDeploymentManager dm = connMgr.getDeploymentManager();
      dispatchProgress(PSProgressStatus.TYPE.START, 
         getResourceString("status.msg.creating.cfgDef"),
         getResourceString("status.note.please.wait"));
      IPSDeployJobControl job = dm.createConfigDef(info.getName(), targetFile);
      while(job.getStatus() != 100 && job.getStatus() != -1) // Wait for file upload to complete
      {
         try
         {
            Thread.sleep(250);
         }
         catch (InterruptedException ignore){}
      }
      if(job.getStatus() == -1)
      {
         firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR,
            job.getStatusMessage());
         dispatchProgress(PSProgressStatus.TYPE.END, null, null);
         return;
      }
     dispatchProgress(PSProgressStatus.TYPE.END, null, null);
     String confirmMsg = 
        MessageFormat.format(getResourceString("info.configDef.confirm"), 
           new Object[]{targetFile.getName(), m_exportDir.getAbsolutePath()});     
     firePackagerClientModelChangeEvent(ChangeEventTypes.INFO, confirmMsg);
      
   }
   
   /**
    * Create default config based on configuration def.
    * 
    * @param info the descriptor meta info, cannot be <code>null</code>. 
    * @throws PSDeployException
    */
   public void createDefaultConfig(PSPackageDescriptorMetaInfo info) 
      throws PSDeployException
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null");
      if(m_exportDir == null)
         throw new IllegalArgumentException("target dir cannot be null.");
      if(!m_exportDir.exists())
         m_exportDir.mkdirs();      
      File targetFile = new File(m_exportDir, info.getName() + "_defaultConfig.xml");
      File configDef = new File(targetFile.getParent(), info.getName() + "_configDef.xml");
      
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSDeploymentManager dm = connMgr.getDeploymentManager();
      dispatchProgress(PSProgressStatus.TYPE.START, 
         getResourceString("status.msg.creating.defaultCfg"),
         getResourceString("status.note.please.wait"));
      IPSDeployJobControl job = dm.createDefaultConfig(info.getName(), targetFile, configDef);
      while(job.getStatus() != 100 && job.getStatus() != -1) // Wait for file upload to complete
      {
         try
         {
            Thread.sleep(250);
         }
         catch (InterruptedException ignore){}
      }
      if(job.getStatus() == -1)
      {
         firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR,
            job.getStatusMessage());
         dispatchProgress(PSProgressStatus.TYPE.END, null, null);
         return;
      }
     dispatchProgress(PSProgressStatus.TYPE.END, null, null);
     String confirmMsg = 
        MessageFormat.format(getResourceString("info.defaultConfig.confirm"), 
           new Object[]{targetFile.getName(), m_exportDir.getAbsolutePath()});     
     firePackagerClientModelChangeEvent(ChangeEventTypes.INFO, confirmMsg);
      
   }
   
   /**
    * Create the descriptor summary.
    * @param info cannot be <code>null</code>.
    * @throws PSDeployException
    */
   public void createSummary(PSPackageDescriptorMetaInfo info) 
      throws PSDeployException
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null");
      if(m_exportDir == null)
         throw new IllegalArgumentException("target dir cannot be null.");
      if(!m_exportDir.exists())
         m_exportDir.mkdirs();      
      File targetFile = new File(m_exportDir, info.getName() + "_summary.txt");
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSDeploymentManager dm = connMgr.getDeploymentManager();
      dispatchProgress(PSProgressStatus.TYPE.START, 
         getResourceString("status.msg.creating.summary"),
         getResourceString("status.note.please.wait"));
      IPSDeployJobControl job = 
         dm.createDescriptorSummary(info.getName(), targetFile);
      while(job.getStatus() != 100 && job.getStatus() != -1) // Wait for file upload to complete
      {
         try
         {
            Thread.sleep(250);
         }
         catch (InterruptedException ignore){}
      }
      if(job.getStatus() == -1)
      {
         firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR,
            job.getStatusMessage());
         dispatchProgress(PSProgressStatus.TYPE.END, null, null);
         return;
      }
     dispatchProgress(PSProgressStatus.TYPE.END, null, null);
     String confirmMsg = 
        MessageFormat.format(getResourceString("info.summary.confirm"), 
           new Object[]{targetFile.getName(), m_exportDir.getAbsolutePath()});     
     firePackagerClientModelChangeEvent(ChangeEventTypes.INFO, confirmMsg);
   }
   
   /**
    * Delete the descriptor specified by the passed in descriptor
    * meta info object.
    * @param info descriptor meta info object, cannot be <code>null</code>.
    * @throws PSDeployException
    */
   public void deleteDescriptor(PSPackageDescriptorMetaInfo info) 
      throws PSDeployException
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null");
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      connMgr.getDeploymentManager().deleteExportDescriptor(info.getName());
      m_metaInfo = null;
      loadDataIsland();
      firePackagerClientModelChangeEvent(ChangeEventTypes.DELETE_DESCRIPTOR);
   }
   
   /**
    * Disconnect from currently connected server if connected.
    * @throws PSDeployException
    */
   public void disconnect()
      throws PSDeployException
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSDeploymentServerConnection conn = connMgr.getConnection();
      if(conn != null && conn.isConnected())
         connMgr.disconnect();
      m_metaInfo = null;
      m_elements = null;
      m_dependencies = null;
      m_fileResources = null;
      m_filter = null;
      m_packageDepends = null;
      m_descriptors = null;
      m_categories = null;
      m_categoryElements.clear();
      m_dependsToElementsMap = null;
      setAsClean();
      firePackagerClientModelChangeEvent(
         ChangeEventTypes.SERVER_DISCONNECT);
   }
   
   /**
    * Edit the descriptor specified by the passed in info object.
    * @param info the descriptor meta info object. Cannot be
    * <code>null</code>.
    * @throws PSDeployException
    */
   public void editDescriptor(PSPackageDescriptorMetaInfo info) 
      throws PSDeployException
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null.");
      dispatchProgress(PSProgressStatus.TYPE.START,
         getResourceString("status.msg.loading"), null,
         false);
      
      Boolean loadOccured = load(info);
      
      dispatchProgress(PSProgressStatus.TYPE.END, null, null);
      firePackagerClientModelChangeEvent(ChangeEventTypes.EDIT_DESCRIPTOR,
         loadOccured);
   }
   
   /**
    * Notify the model that element selection has changed.
    */
   public void elementSelectionChange()
   {
      m_elementSelectionChanged = true;
      m_lastDependenciesCalculated = null;
   }
   
   /**
    * Load all element categories from the server.
    * @throws PSDeployException
    */
   public List<PSElementCategory> getAllElementCategories(boolean force)
            throws PSDeployException
   {
      PSServerConnectionManager connMgr = PSServerConnectionManager
         .getInstance();
      PSCataloger cat = connMgr.getCataloger();

      try
      {
         if (m_categories == null || force)
         {
            PSCatalogResultSet results = null;
            m_categories = 
               new ArrayList<PSElementCategory>();
            // Get Element Categories
            results = cat.catalog(PSCataloger.TYPE_REQ_DEPLOY_TYPES, null);
            Iterator it = results.getResults();
            while (it.hasNext())
            {
               PSCatalogResult res = (PSCatalogResult) it.next();
               m_categories.add(new PSElementCategory(res.getID(), res
                        .getDisplayText(), false));
            }
            results = cat.catalog(PSCataloger.TYPE_REQ_CUSTOM_TYPES, null);
            it = results.getResults();
            while (it.hasNext())
            {
               PSCatalogResult res = (PSCatalogResult) it.next();
               if(!USER_DEPENDENCY_ID.equals(res.getID()))
                  m_categories.add(new PSElementCategory(res.getID(), res
                     .getDisplayText(), true));
            }
            
            Collections.sort(m_categories,
                     new Comparator<PSElementCategory>()
                     {

                        public int compare(PSElementCategory o1,
                                 PSElementCategory o2)
                        {
                           String s1 = o1.getName().toLowerCase();
                           String s2 = o2.getName().toLowerCase();
                           return s1.compareTo(s2);
                        }

                     });
                     
         }
         return m_categories;
         
      }
      catch (Exception e)
      {
         throw new PSDeployException(
            new PSException("Unexpected Error:", e));
      }
   }
   
   /**
    * Retrieves all elements for a specified category.
    * @param categoryName cannot be <code>null</code>.
    * @param force always go to server for info instead of cache.
    * @return
    * @throws PSDeployException
    */
   public List<PSDeployableElement> getCategoryElements(
      PSElementCategory category, boolean force)
      throws PSDeployException
   {
      if(category == null)
         throw new IllegalArgumentException("category cannot be null.");
      PSServerConnectionManager connMgr = PSServerConnectionManager
      .getInstance();
      PSDeploymentManager dm = connMgr.getDeploymentManager();      
      List<PSDeployableElement> elements = 
         m_categoryElements.get(category.getId());
      if(elements == null || force)
      {
         elements = new ArrayList<PSDeployableElement>();
         String prefix = category.isCustom() ? "Custom/" : "";
         Iterator it = dm.getDeployableElements(prefix + category.getId());
         while(it.hasNext())
         {
            PSDeployableElement el = (PSDeployableElement)it.next();
            elements.add(el);
         }
         Collections.sort(elements, new Comparator<PSDeployableElement>()
            {

               public int compare(PSDeployableElement o1, PSDeployableElement o2)
               {
                  String name1 = StringUtils.defaultString(
                     o1.getDisplayName()).toLowerCase();
                  String name2 = StringUtils.defaultString(
                     o2.getDisplayName()).toLowerCase();
                  return name1.compareTo(name2);
               }            
            });
         m_categoryElements.put(category.getId(), elements);
      }
      return elements;
   }
   
   /**
    * Get the descriptor meta info object to use for updating to and
    * from general fields.
    * @return may be <code>null</code> if no descriptor is was selected. 
    */
   public PSPackageDescriptorMetaInfo getDescriptorMetaInfo()
   {
      return m_metaInfo;
   }
   
   /**
    * Gets the list of descriptors on server. If the descriptors are not
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
   public List<PSPackageDescriptorMetaInfo> getDescriptors(boolean forceCatalog)
      throws PSDeployException
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSDeploymentServerConnection conn = connMgr.getConnection();
      
      checkConnected();
      PSCatalogResultSet results = null;
      if(m_descriptors == null || forceCatalog)
      {
         try
         {
            m_descriptors = new ArrayList<PSPackageDescriptorMetaInfo>();
            results = getSortedSet(connMgr.getCataloger().catalog(
               PSCataloger.TYPE_REQ_DESCRIPTORS, null));
            Iterator it = results.getResults();
            while(it.hasNext())
            {
               PSCatalogResult result = (PSCatalogResult)it.next();
               Object[] columns = result.getColumns();
               PSPackageDescriptorMetaInfo desc = 
                  new PSPackageDescriptorMetaInfo(columns[0].toString());
               desc.setDescription(columns[1].toString());
               desc.setLastModified((Date)columns[2]);
               desc.setVersion(columns[3].toString());
               m_descriptors.add(desc);               
            }
         }
         catch(Exception e)
         {
            handleException(e);
         }
      }
      return m_descriptors;
   }
   
   /**
    * Retrieve a listing of all existing packages on the currently connected
    * server.
    * @return the iterator of all existing packages. May be <code>null</code>
    * if not connected to server.
    */
   public Iterator<PSPackageDependency> getExistingPackages()
   {
      return m_packages.iterator();
   }
   
   /**
    * Retrieve list of file resource paths for this descriptor.
    * @return list of file resource path strings. Never <code>null</code>
    * if not yet initialized.
    */
   public List<String> getFileResources()
   {
      if(m_fileResources == null)
         m_fileResources = new ArrayList<String>();
      return m_fileResources;
   }
   
   /**
    * Returns a list of <code>PSElementNodes</code> that represent
    * the categories and elements in the selection tree that should be
    * visible based on the set filter if one exists. See 
    * {@link #setElementFilter(PSElementFilter)} for details on setting
    * the filter.
    * @return the list of filtered element nodes, never <code>null</code>,
    * may be empty.
    */
   public List<PSElementNode> getFilteredElementNodes()
   {
      List<PSElementNode> results = new ArrayList<PSElementNode>();
      for(PSElementNode cat : m_elements)
      {
         if(!cat.hasChildren())
            continue;
         PSElementNode newCat = new PSElementNode(cat.getName(), true);
         Iterator<PSElementNode> children = cat.getChildren();
         while(children.hasNext())
         {
            PSElementNode child = children.next();
            //Filter out elements from other packages
            PSPackageDependency parentPackage = 
               getParentPackage(child.getDependencyId(), child.getObjectType());
            if(parentPackage != null)
               continue;
            if(m_filter == null)
            {
               newCat.addChild(child);
            }
            else
            {
               if(m_filter.accept(child))
                  newCat.addChild(child);
            }
         }
         if(newCat.hasChildren())
            results.add(newCat);
      }
      return results;
   }
   
   /**
    * Retrieve all package dependencies for this descriptor.
    * @return list of dependencies. May be <code>null</code> or
    * empty if no depends exist.
    */
   public List<PSPackageDependency> getPackageDependencies()
   {
      return m_packageDepends;
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
     PSServerConnectionManager connMgr = 
        PSServerConnectionManager.getInstance();
     
     if(dirPath != null && dirPath.trim().length() == 0)
        dirPath = null;

     PSCatalogResultSet result = null;

     try
     {
        Properties props = new Properties();
        if(dirPath != null)
           props.put("directory", dirPath);

        result = getFileSortedSet(connMgr.getCataloger().catalog(
           PSCataloger.TYPE_REQ_USER_DEP, props));
     }
     catch(Exception e)
     {
        handleException(e);
     }

     return result;
  }

   
   /**
    * Indicate if this descriptor is new and has never been
    * saved.
    * @return <code>true</code> if new.
    */
   public boolean isDescriptorNew()
   {
      return m_isNew;
   }
   
   /**
    * Indicates if this model has "dirty" fields or components
    * since last descriptor save or load.
    * @return <code>true</code> if model is dirty.
    */
   public boolean isDirty()
   {
      return m_dirty;
   }   
   
   /**
    * Create a new descriptor by loading an empty meta info object
    * with name set to new.
    * @throws PSDeployException 
    */
   public void newDescriptor() throws PSDeployException
   {
      m_metaInfo = new PSPackageDescriptorMetaInfo("");      
      m_lastDependenciesCalculated = null;
      m_isNew = true;
      m_filter = null;
      loadElements();
      loadPackageDepends(null);
      loadFileResources(null);
      setAsClean();
      
      firePackagerClientModelChangeEvent(ChangeEventTypes.NEW_DESCRIPTOR);
   }
   
   /**
    * Remove an existing packager client model listener.
    * @param listener cannot be <code>null</code>.
    */
   public void removePackagerClientModelListener(
      IPSPackagerClientModelListener listener)
   {
        if(listener == null)
           throw new IllegalArgumentException("listener cannot be null.");
        if(m_listeners.contains(listener))
           m_listeners.remove(listener);
   }
   
   /**
    * Save the current descriptor. This should be called in another thread
    * if ran from the ui.
    */
   public void saveDescriptor()
   {
      dispatchProgress(PSProgressStatus.TYPE.START,
         getResourceString("status.msg.saving"), null);
      save();
      dispatchProgress(PSProgressStatus.TYPE.END, null, null);
      firePackagerClientModelChangeEvent(ChangeEventTypes.SAVE_DESCRIPTOR);
   }
   
   /**
    * Set model to be in a clean state.
    */
   public void setAsClean()
   {
      m_dirty = false;
      m_elementSelectionChanged = false;
      firePackagerClientModelChangeEvent(ChangeEventTypes.DIRTY_STATE_CHANGE);
   }
   
   /**
    * Set model to be dirty.
    */
   public void setAsDirty()
   {
      m_dirty = true;
      firePackagerClientModelChangeEvent(ChangeEventTypes.DIRTY_STATE_CHANGE);
   }
   
   /**
    * Set the file path of the local package directory.
    * @param dir cannot be <code>null</code>.
    */
   public void setLocalPkgDir(File dir)
   {
      if(dir == null)
         throw new IllegalArgumentException("dir cannot be null.");
      m_localPkgDir = dir;
   }
   
   /**
    * Set the file path of the export directory.
    * @param dir cannot be <code>null</code>.
    */
   public void setLocalExportDir(File dir)
   {
      if(dir == null)
         throw new IllegalArgumentException("dir cannot be null.");
      m_exportDir = dir;
   }
   
   /**
    * Set the element filter to be used for retrieving the elements that
    * will be visible in the element selection component. See
    * {@link #getFilteredElementNodes()} for details.
    * @param filter may be <code>null</code>.
    */
   public void setElementFilter(PSElementFilter filter)
   {
      m_filter = filter;
   }
   
   /**
    * Request model to cancel the current operation if
    * possible. Generally only called via the progress box.
    */
   public void cancelCurrentOperation()
   {
      m_cancelCurrentOperation = true;
      dispatchProgress(PSProgressStatus.TYPE.UPDATE, null,
         getResourceString("status.note.cancelling"));
   }   
         
   /**
    * Calculate dependencies for selected elements. This
    * should be run in a separate thread.
    * @param standAlone flag indicating that calculate depends is not
    * being run as part of any other process such as build and
    * should launch the progress box itself.
    */
   public void calculateDependencies(boolean standAlone)
   {
      final int waitTime = 1000 * 60 * 5;
      if(m_lastDependenciesCalculated != null &&
         (new Date().getTime() - 
            m_lastDependenciesCalculated.getTime()) < waitTime)
         return;
      
      try
      {         
         
         PSServerConnectionManager connMgr = PSServerConnectionManager
                  .getInstance();
         PSDeploymentManager dm = connMgr.getDeploymentManager();
         m_dependencies = new ArrayList<PSDependency>();
         
         if(standAlone)
            dispatchProgress(PSProgressStatus.TYPE.START,
               getResourceString("status.msg.calc.depends"),
               getResourceString("status.note.please.wait"));
         
         // clear implied flags from package depends
         for (PSPackageDependency dep : m_packageDepends)
            dep.setImpliedDependency(false);

         Collection<PSElementNode> elements = getSelectedElements();

         dispatchProgress(PSProgressStatus.TYPE.UPDATE, null,
            getResourceString("status.msg.calc.depends"));

         
         for (PSElementNode node : elements)
         {
            String note = MessageFormat.format(
               getResourceString("status.note.analyzing"),
               new Object[]{node.getName()});
            dispatchProgress(PSProgressStatus.TYPE.UPDATE, null, note);            

            PSDeployableElement element = m_dependsToElementsMap.get(node
                     .getDependencyId()
                     + "_" + node.getObjectType());

            // Add the element itself as a dependency
            PSDependency dependClone = (PSDependency) element.clone();
            if (!dependClone.getObjectType().equals(
                     IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
               dependClone.setDependencies(null);
            m_dependencies.add(dependClone);
            if(m_cancelCurrentOperation)
               return;
            handleChildDepends(dependClone, dm, new ArrayList<String>());
         }
         m_lastDependenciesCalculated = new Date();
      }
      catch (PSDeployException e)
      {
         firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR, e);
      }
      finally
      {
         if(standAlone)
            dispatchProgress(PSProgressStatus.TYPE.END, null,
               null);
      }

   }
   
   /**
    * Checks that the server is connected or not.
    * 
    * @throws IllegalStateException if the server is not connected.
    */
   private void checkConnected()
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSDeploymentServerConnection conn = connMgr.getConnection();
      if(conn == null || !conn.isConnected())
         throw new IllegalStateException(
            "server is not connected to perform the requested operation.");
   }
   
   /**
    * Find an element node by dependencyId and objectType
    * @param dependencyId assumed not <code>null</code> or empty.
    * @param objectType assumed not <code>null</code> or empty
    * @return the element node or <code>null</code> if not found.
    */
   private PSElementNode findElementNode(String dependencyId, String objectType)
   {
      if(m_elements == null)
         return null;
      for(PSElementNode cat : m_elements)
      {
         Iterator<PSElementNode> it = cat.getChildren();
         while(it.hasNext())
         {
            PSElementNode el = it.next();
            
            if(el.getDependencyId().equals(dependencyId) &&
               el.getObjectType().equals(objectType))
               return el;
         }
      }
      return null;
   }
   
   /**
    * Convienience method to call {@link #dispatchProgress(
    * com.percussion.packager.ui.data.PSProgressStatus.TYPE,
    * String, String, boolean) as
    * <code>dispatchProgress(type, msg, note, true)</code>.
    */
   private void dispatchProgress(
            PSProgressStatus.TYPE type, String msg, String note)
   {
      dispatchProgress(type,msg, note, true);
   }
   
   /**
    * Helper method to dispatch progress events.
    * @param type <code>PSProgressStatus.TYPE</code>, assumed
    * not <code>null</code>.
    * @param msg may be <code>null</code>.
    * @param note may be <code>null</code>.
    * @param enableCancel flag indicating that cancellation
    * of progress is allowed. Only affects progress start type.
    */
   private void dispatchProgress(
      PSProgressStatus.TYPE type, String msg, String note, boolean enableCancel)
   {
      PSProgressStatus status = null;
      if(type == PSProgressStatus.TYPE.START)
      {
         m_cancelCurrentOperation = false;
         status = 
            PSProgressStatus.createStartStatus(
               StringUtils.defaultString(msg),
               StringUtils.defaultString(note));
         status.setEnableCancel(enableCancel);
      }
      if(type == PSProgressStatus.TYPE.UPDATE)
      {
         status = PSProgressStatus.createUpdateStatus(
            StringUtils.defaultString(note));
      }
      if(type == PSProgressStatus.TYPE.END)
      {
         status = PSProgressStatus.createEndStatus();
         m_cancelCurrentOperation = false;
      }
      firePackagerClientModelChangeEvent(
         ChangeEventTypes.PROGRESS_UPDATE, status);
   }
   
   
   
   /**
    * Convenience method to call {@link #firePackagerClientModelChangeEvent(
    * com.percussion.packager.ui.model.PSPackagerClientModel.ChangeEventTypes,
    * Object)} as <code>firePackagerClientModelChangeEvent(type, null)</code>.
    */
   private void firePackagerClientModelChangeEvent(ChangeEventTypes type)
   {
      for(IPSPackagerClientModelListener listener : m_listeners)
      {
         listener.modelChanged(type, null);
      }
   }
   
   /**
    * Fire a packager client model change event.
    * @param type the change event, assumed not <code>null</code>.
    */
   private void firePackagerClientModelChangeEvent(ChangeEventTypes type,
      Object extra)
   {
      for(IPSPackagerClientModelListener listener : m_listeners)
      {
         listener.modelChanged(type, extra);
      }
   }
   
   /**
    * Creates the user dependency entry from the list
    * of file resources if any exist.
    * @return the user dependency entry or <code>null</code> if
    * no file resources selected.
    */
   private PSDependency getFileResourcesDependency()
   {
      if(m_fileResources != null && !m_fileResources.isEmpty())
      {
         PSDeployableElement dep = new PSDeployableElement(
            PSDependency.TYPE_SHARED,
            USER_DEPENDENCY_ID,
            IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM,
            IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM,
            "User Dependency",
            false,
            false,
            true,
            false);
         dep.setDependencies(new ArrayList().iterator()); 
         for(String file : m_fileResources)
         {
              File f = new File(file);              
              dep.addUserDependency(f);
         }
         return dep;           
      }
      return null;
   }
   
   /**
    * Retrieves parent package of dependency if one exists.
    * @param dependencyId assumed not <code>null</code>.
    * @param objectType assumed not <code>null</code>.
    * @return the parent package dependency or <code>null</code> if
    * not found.
    */
   private PSPackageDependency getParentPackage(String dependencyId,
      String objectType)
   {
      PSPackageDependency pack = 
         m_dependsToPackageMap.get(dependencyId + "_" + objectType);
      
      if(pack == null || 
         (m_metaInfo != null && 
             pack.getPackageName().equals(m_metaInfo.getName())))
         return null;
      return pack;
   }
   
   /**
    * Retrieves a collection of all selected element nodes.
    * @return collection of element nodes, never <code>null</code>,
    * may be empty.
    */
   private Collection<PSElementNode> getSelectedElements()
   {
      Collection<PSElementNode> results = new ArrayList<PSElementNode>();
      if (m_elements == null)
         return results;
      for(PSElementNode cat : m_elements)
      {
         if(cat.hasChildren())
         {
            Iterator<PSElementNode> children = cat.getChildren();
            while(children.hasNext())
            {
               PSElementNode child = children.next();
               if(child.isSelected())
               {
                  results.add(child);
               }
            }
         }
      }
      return results;
   } 
   
   /**
    * Sorts the results of given cataloged result set by the result's
    * display text lexicographically.
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
    * File / Directory resources which are already included in a package will be
    * filtered out of the returned set.
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
            String recId = record.getID().replaceAll("\\\\", "/");
            PSPackageDependency parentPackage =
               getParentPackage(recId, USER_DEPENDENCY_ID);
            if (parentPackage == null)
            {
               if (recId.endsWith("/"))
               {
                  sortedDirectoryResults.add(record);
               }
               else
               {
                  sortedFileResults.add(record);
               }
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
   
   /**
    * Handles the recursive dependency calculation.
    * @param dependency
    * @param dm
    * @param done
    * @param recurse
    * @throws PSDeployException
    */
   private void handleChildDepends(PSDependency dependency,
      PSDeploymentManager dm, List<String> done)
      throws PSDeployException
   {
      if(m_cancelCurrentOperation)
         return;
      if(dependency.getObjectType().equals(
         IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
      {
         Iterator it = dependency.getDependencies();
         if(it != null && it.hasNext())
         {
            handleChildDepends((PSDependency)it.next(), dm, done);
         }
         return;
      }
      
      String key = dependency.getDependencyId() + " -- " + dependency.getObjectType();
      if(done.contains(key))
         return;
      else
         done.add(key);
      dm.loadDependencies(dependency);
      Iterator children = dependency.getDependencies();
      while(children.hasNext())
      {
         if(m_cancelCurrentOperation)
            return;
         PSDependency child = (PSDependency)children.next();
         PSPackageDependency parentPackage = 
            getParentPackage(child.getDependencyId(), child.getObjectType());
         if(!child.isAssociation())
         {
            if(parentPackage != null)
            {
               PSPackageDependency pack = 
                  m_packageDepends.get(m_packageDepends.indexOf(parentPackage));
               if(child.canBeIncludedExcluded())
                  child.setIsIncluded(false);
               pack.setImpliedDependency(true);
            }
            else 
            {
               if(child.getDependencyType() == PSDependency.TYPE_SHARED)
               {
                  if(child.canBeIncludedExcluded())
                     child.setIsIncluded(true); 
                  handleChildDepends(child, dm, done);
               }
               else if(child.getDependencyType() == PSDependency.TYPE_LOCAL)
               {                 
                  handleChildDepends(child, dm, done);
               }
            }
         }         
      }
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
    * Load data from specified descriptor.
    * @param info 
    * @return <code>true</code> if the load actually occurred.
    * @throws PSDeployException 
    */
   private boolean load(PSPackageDescriptorMetaInfo info) 
      throws PSDeployException
   {
            
      dispatchProgress(PSProgressStatus.TYPE.UPDATE, null, 
         getResourceString("status.note.loading.desc.from.server"));
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSExportDescriptor descriptor = 
         connMgr.getDeploymentManager().getExportDescriptor(info.getName());
      m_isNew = false;
      m_lastDependenciesCalculated = null;
      // Handle missing packages message
      Iterator missing = descriptor.getMissingPackages();
      StringBuilder msg = new StringBuilder();
      boolean foundMissing = false;      
      while(missing.hasNext())
      {
         String miss = (String)missing.next();
         if(foundMissing)
            msg.append("\n");
         msg.append('\u2022');
         msg.append(' ');
         msg.append(miss);
         foundMissing = true;
      }
      if(foundMissing)
      {
         String prefix = "msg.missing.elements";
         Object[] obj = new Object[]{prefix, new Object[]{msg.toString()}};
         firePackagerClientModelChangeEvent(
            ChangeEventTypes.WARNING, obj);
      }
      m_filter = null;
      m_metaInfo = new PSPackageDescriptorMetaInfo(descriptor);
      loadPackageDepends(descriptor);            
      loadElements();
      
      //clear out dependencies before calculating them
      m_fileResources = null;
      
      Iterator<PSDeployableElement> it = descriptor.getPackages();
      while(it.hasNext())
      {
         PSDependency el = it.next();
         if(el.getObjectType().equals(
            IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM) &&
               el.getDependencyId().equals(USER_DEPENDENCY_ID))
         {
            loadFileResources(el);
         }
         else
         {
            if(el.getObjectType().equals(
                  IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
            {
               Iterator<PSDependency> itr = el.getDependencies();
               if(itr != null && itr.hasNext())
               {
                  el = itr.next();
               }               
            }
            PSElementNode node = 
               findElementNode(el.getDependencyId(), el.getObjectType());
            if(node != null)
            {
               node.setSelected(true);
            }
            else
            {
               //Dependency no longer exists? This should never happen.
               
            }           
         }
      }
      if(foundMissing)
      {
         setAsDirty();
      }
      else
      {
         setAsClean();   
      }
      return true;
   }
   
   /**
    * Loads all data needed by the application up front into cache.
    * Called at server connection time.
    * @throws PSDeployException 
    */
   @SuppressWarnings("unchecked")
   private void loadDataIsland() throws PSDeployException
      
   {
      Runnable r = new Runnable()
      {
         public void run()
         {
            try
            {
               m_dependsToElementsMap = new HashMap<String, PSDeployableElement>();
               List<PSElementCategory> cats = getAllElementCategories(true);
               dispatchProgress(PSProgressStatus.TYPE.START, 
                  getResourceString("status.msg.analyzing.server"),
                  getResourceString("status.note.initializing"),
                  false);               
               int count = 1;
               for(PSElementCategory cat : cats)
               {
                  
                  List<PSDeployableElement> els = 
                     getCategoryElements(cat, true);
                  for(PSDeployableElement el : els)
                  { 
                     PSDependency actual = el;
                     if(actual.getObjectType().equals(IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
                     {
                        Iterator it = actual.getDependencies();
                        if(it != null && it.hasNext())
                        {
                           actual = (PSDependency)it.next();
                        }               
                     }
                     m_dependsToElementsMap.put(
                        actual.getDependencyId() + "_" + actual.getObjectType(), el);
                  }
                  String note = MessageFormat.format(
                     getResourceString("status.note.loading.cat"),
                     new Object[]{cat.getName()});
                  dispatchProgress(PSProgressStatus.TYPE.UPDATE, null, note);                                   
                  
               }
               loadDependsToPackageIndex();               
               
               firePackagerClientModelChangeEvent(
                  ChangeEventTypes.SERVER_CONNECT);
            }
            catch (PSDeployException e)
            {
               firePackagerClientModelChangeEvent(
                  ChangeEventTypes.ERROR, e);
            }
            finally
            {
               dispatchProgress(PSProgressStatus.TYPE.END, null, null);
            }
         }         
      }; 
      Thread t = new Thread(r);
      t.start();
   }
   
   /**
    * 
    * @throws PSDeployException
    */
   private void loadDependsToPackageIndex() throws PSDeployException
   {
      dispatchProgress(PSProgressStatus.TYPE.UPDATE, null,
               getResourceString("status.note.loading.dep.to.pkg.map"));               
            
            PSDeploymentManager dm = PSServerConnectionManager.getInstance().
               getDeploymentManager();
            Iterator<Map<String, String>> indexIt = 
               dm.getDependencyToPackageNameIndex();
            m_dependsToPackageMap = new HashMap<String, PSPackageDependency>();
            m_packages = new TreeSet<PSPackageDependency>(
               new Comparator<PSPackageDependency>()
               {
                  public int compare(PSPackageDependency p1,
                           PSPackageDependency p2)
                  {
                     String name1 = p1.getPackageName();
                     String name2 = p2.getPackageName();
                     return name1.compareTo(name2);
                  }                     
               });
            while(indexIt.hasNext())
            {
               Map<String, String> entry = indexIt.next();
               String dId = entry.get("dependencyId");
               String type = entry.get("objectType");
               String pack = entry.get("package");
               String version = entry.get("version");
               PSPackageDependency depend = new PSPackageDependency(pack);
               depend.setPackageVersion(version);
               m_dependsToPackageMap.put(dId + "_" + type, depend);
               m_packages.add(depend);
            }
            for(PSPackageDescriptorMetaInfo info : getDescriptors(true))
            {
               PSPackageDependency depend = 
                  new PSPackageDependency(info.getName());
               depend.setPackageVersion(info.getVersion());
               m_packages.add(depend);
            }
   }
   
   /**
    * Helper method to load elements list.
    * @throws PSDeployException
    */
   private void loadElements() throws PSDeployException
   {
      m_elements = new ArrayList<PSElementNode>();
      for(PSElementCategory cat : getAllElementCategories(false))
      {
         PSElementNode catNode = new PSElementNode(cat.getName(), true);
         for(PSDependency el : getCategoryElements(cat, false))
         {            
            if(el.getObjectType().equals(IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM))
            {
               Iterator it = el.getDependencies();
               if(it != null && it.hasNext())
               {
                  el = (PSDependency)it.next();
               }               
            }
            PSElementNode elNode = new PSElementNode(el.getDisplayName(),
               false, el.getDependencyId(), el.getObjectType()); 
            catNode.addChild(elNode);            
         }
         m_elements.add(catNode);
      }
   }
   
   /**
    * Loads the file resource paths from the passed in user dependency
    * if specified. If not it just set file resources to an empty List.
    * @param userDepend may be <code>null</code>.
    */
   private void loadFileResources(PSDependency userDepend)
   {
      m_fileResources = new ArrayList<String>();
      if(userDepend != null)
      {
         Iterator it = userDepend.getDependencies();
         while(it.hasNext())
         {
            PSUserDependency dep = (PSUserDependency)it.next();
            m_fileResources.add(dep.getPath().getPath());
         } 
         Collections.sort(m_fileResources);
      }
   }
   
   /**
    * Handle loading of package dependencies and setting
    * their state to match loaded descriptor.
    * @param desc may be <code>null</code> when called for
    * a new descriptor.
    */
   private void loadPackageDepends(PSExportDescriptor desc)
   {
      m_packageDepends = new ArrayList<PSPackageDependency>();
      Map<String, Boolean> index = new HashMap<String, Boolean>();
      if (desc != null)
      {
         List<Map<String, String>> pkgDepList = desc.getPkgDepList();
         if (pkgDepList != null)
         {
            for (Map<String, String> temp : pkgDepList)
            {
               String name = temp.get(PSDescriptor.XML_PKG_DEP_NAME);
               Boolean implied = Boolean.valueOf(temp
                  .get(PSDescriptor.XML_PKG_DEP_IMPLIED));
               index.put(name, implied);
            }
         }
      }
      for(PSPackageDependency pack : m_packages)
      {
         PSPackageDependency dep = (PSPackageDependency) pack.clone();
         m_packageDepends.add(dep);
         Boolean implied = index.get(dep.getPackageName());
         if(implied == null)
         {
            dep.setSelected(false);
            continue;
         }
         dep.setSelected(!implied);
         dep.setImpliedDependency(implied);         
      }
   }
   
   /**
    * Create and save the descriptor to the server.
    * 
    * @throws PSDeployException
    */
   private PSExportDescriptor save()
   {

      try
      {
         PSServerConnectionManager connMgr = PSServerConnectionManager
                  .getInstance();
         // Create descriptor id if one does not exist yet.
         if (StringUtils.isBlank(m_metaInfo.getId()))
         {
            m_metaInfo.setId(connMgr.getDeploymentManager()
                     .createDescriptorGuid());
         }
         PSExportDescriptor descriptor = m_metaInfo.toExportDescriptor();
         calculateDependencies(false);
         List<PSDependency> depends = new ArrayList<PSDependency>(
                  m_dependencies);
         if(m_cancelCurrentOperation)
            return descriptor;
         PSDependency userDepend = getFileResourcesDependency();
         if (userDepend != null)
            depends.add(userDepend);
         if(m_cancelCurrentOperation)
            return descriptor;
         descriptor.setPackages(depends.iterator());
         for(PSPackageDependency pDep : m_packageDepends)
         {   
            if(pDep.isImpliedDependency() || pDep.isSelected())
            {
               descriptor.setPkgDep(pDep.getPackageName(),
                  pDep.getPackageVersion(), pDep.isImpliedDependency());
            }
         }
         if(m_cancelCurrentOperation)
            return descriptor;
         dispatchProgress(PSProgressStatus.TYPE.UPDATE, null,
            getResourceString("status.note.saving.desc.to.server"));
         connMgr.getDeploymentManager().saveExportDescriptor(descriptor);
         // Add the package to the cache of all packages
         PSPackageDependency pkg = new PSPackageDependency(descriptor.getName());
         pkg.setPackageVersion(descriptor.getVersion());         
         loadDependsToPackageIndex();
         m_isNew = false;
         setAsClean();
         
         return descriptor;
      }
      catch (Exception e)
      {
         firePackagerClientModelChangeEvent(ChangeEventTypes.ERROR, e);
      }
      return null;
   }
   
   /**
    * Get resource string based on passed in key.
    * @param key assumed not <code>nul</code>.
    * @return the resource string or key if not found.
    */
   private static String getResourceString(String key)
   {
      return 
         PSResourceUtils.getResourceString(PSPackagerClientModel.class, key);
   }
   
   /**
    * Enumeration of change event types.
    */
   public static enum ChangeEventTypes
   {
      BUILD_DESCRIPTOR,
      CONFIRM,
      DELETE_DESCRIPTOR,
      DIRTY_STATE_CHANGE,
      EDIT_DESCRIPTOR,
      ERROR,
      FORCE_PAGE_LOAD,
      INFO,
      NEW_DESCRIPTOR,
      PROGRESS_UPDATE,
      SAVE_DESCRIPTOR,
      SERVER_CONNECT,
      SERVER_DISCONNECT,
      WARNING
   }
   
   /**
    * Constant used as the id for all user dependencies.
    */
   private static final String USER_DEPENDENCY_ID = "sys_UserDependency";
   
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
   
   /**
    * The currently selected package descriptor meta information.
    */
   private PSPackageDescriptorMetaInfo m_metaInfo;
   
   /**
    * List of all categories and elements and their selection
    * state.
    */
   private List<PSElementNode> m_elements;
   
   /**
    * List of all dependencies for the descriptor. Calculated and
    * set in {@link #calculateDependencies()}.
    */
   private List<PSDependency> m_dependencies;
   
   /**
    * List of all dependent packages.
    */
   private List<PSPackageDependency> m_packageDepends;
   
   /**
    * The element filter to be used by the selection tree.
    */
   private PSElementFilter m_filter;
   
   /**
    * The list of descriptors that exists on this server,
    * <code>null</code> until first call to <code>getDescriptors(boolean)</code>
    * or if the server is not connected.
    */
   private List<PSPackageDescriptorMetaInfo> m_descriptors = null;
   
   /**
    * List of all registered packager client model listeners.
    * Never <code>null</code>, may be empty.
    */
   private List<IPSPackagerClientModelListener> m_listeners = 
      new ArrayList<IPSPackagerClientModelListener>();
   
   /**
    * Flag indicating if the model is dirty. Modified in
    * {@link #setAsDirty()} and {@link #setAsClean()}.
    */
   private boolean m_dirty;
   
   /**
    * Flag indicating element selection changed.
    */
   private boolean m_elementSelectionChanged;
   
   /**
    * Cached list of all element categories as <code>PSPair</code> objects,
    * where the first value is the element type and the second value
    * is the element type display name.
    */
   private List<PSElementCategory> m_categories;
   
   /**
    * Cached map of deployable elements for categories. Never
    * <code>null</code>, but may be empty.
    */
   private Map<String, List<PSDeployableElement>> m_categoryElements = 
      new HashMap<String, List<PSDeployableElement>>();
   
   /**
    * List of all file resource paths for the current descriptor.
    */
   private List<String> m_fileResources;
   
   /**
    * Cache of all dependency to packages. The key is
    * the dependencyId concatenated to the objecttype with
    * an underscore separator. Initialized in {@link #loadDataIsland()}.
    * May be <code>null</code> if no connection to server.
    */
   private Map<String, PSPackageDependency> m_dependsToPackageMap;
   
   /**
    * Cache of all dependency to deployable elements.The key is
    * the dependencyId concatenated to the objecttype with
    * an underscore separator.
    * Initialized in {@link #createDependsToElementIndex()}.
    * May be <code>null</code> if no connection to server.
    */
   private Map<String, PSDeployableElement> m_dependsToElementsMap;
   
   /**
    * Cache of all packages existing on this system. 
    * Initialized in {@link #loadDataIsland()}.
    * May be <code>null</code> if no connection to server.
    */
   private Set<PSPackageDependency> m_packages;
   
   /**
    * Flag indicating that the current descriptor is new and has never been
    * saved.
    */
   private boolean m_isNew;
   
   /**
    * Indicates the last time a dependency calculation occurred.
    */
   private Date m_lastDependenciesCalculated;
   
   /**
    * Flag indicating that the current operation should be canceled
    * if possible.
    */
   private boolean m_cancelCurrentOperation;
   
   /**
    * The local package directory where the built package will be placed.
    */
   private File m_localPkgDir;
   
   /**
    * The local export directory where the config def or
    * summary export will be placed.
    */
   private File m_exportDir;

   
}

/******************************************************************************
 *
 * [ PSLocalObjectStore.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies;

import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSMimeContentAdapter;
import com.percussion.design.objectstore.IPSConfig;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSFeatureSet;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUserConfiguration;
import com.percussion.error.PSIllegalStateException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionDefFactory;
import com.percussion.extension.PSExtensionRef;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.tablefactory.PSJdbcTableSchemaCollection;
import com.percussion.util.IOTools;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Local store storing data on local filesystem instead of the server. Note,
 * this class implements only part of methods of the superclass. Unimplemented
 * methods are still overwritten and throw
 * {@link java.lang.UnsupportedOperationException}. Exception handling is
 * sloppy as this file is hacked together for debugging.
 * 
 * @author Andriy Palamarchuk
 */
public class PSLocalObjectStore extends
   com.percussion.design.objectstore.PSObjectStore
{

   /**
    * Default ctor. Initializes the object store by reading the application
    * files. Creates sample object store files and directories from ZIP file
    * from the archive if does not already exist. The directory structure 
    * must full fill the object store requirement, for example,
    * <p> 
    * unitTest
    * &nbsp;&nbsp;  objectstore
    * &nbsp;&nbsp;&nbsp;&nbsp;      sys_resources.xml
    * &nbsp;&nbsp;&nbsp;&nbsp;      rx_resources.xml
    * &nbsp;&nbsp;  sys_resources
    * &nbsp;&nbsp;&nbsp;&nbsp;      css
    * &nbsp;&nbsp;&nbsp;&nbsp;      ewebeditpro
    * &nbsp;&nbsp;  rx_resources
    * &nbsp;&nbsp;&nbsp;&nbsp;      css
    * &nbsp;&nbsp;&nbsp;&nbsp;      html
    * <p>
    * The zip file named "unitTest.zip" is assumed to be part of the jar file 
    * and has the directory structure above.
    */
   public PSLocalObjectStore()
   {
      if (!osDir.exists())
      {
         osDir = createObjectStoreDir();
         if (osDir == null)
         {
            throw new RuntimeException("Object store directory cannot be null");
         }
      }
      File[] files = osDir.listFiles();
      for (File file : files)
      {
         if (!file.getName().toLowerCase().endsWith("xml"))
         {
            // log warning - not an xml file
            continue;
         }
         InputStream is = null;
         Exception ex = null;
         try
         {
            is = new FileInputStream(file);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(is, false);
            PSApplication app = new PSApplication(doc);
            ms_applicationMap.put(app.getName(), app);
         }
         catch (FileNotFoundException e)
         {
            ex = e;
         }
         catch (IOException e)
         {
            ex = e;
         }
         catch (SAXException e)
         {
            ex = e;
         }
         catch (PSUnknownDocTypeException e)
         {
            ex = e;
         }
         catch (PSUnknownNodeTypeException e)
         {
            ex = e;
         }
         finally
         {
            IOUtils.closeQuietly(is);
         }
         if (ex != null)
         {
            // log error for file
            ex.printStackTrace();
         }
      }
   }

   /**
    * Helper method to extract the files from the zip files "unitTest.zip" in
    * the archive and to the local files system. See
    * {@link #PSLocalObjectStore() ctor} for details of the directory structure
    * within the zip file.
    * 
    * @return file object that refers to the object store directory.
    * <code>null</code> if creation of the directory structure fails for any
    * reason.
    */
   private File createObjectStoreDir()
   {
      try
      {
         ZipInputStream in = new ZipInputStream(this.getClass()
            .getResourceAsStream(LOCAL_OBJECT_STORE_ZIP));

         // Get the first entry
         ZipEntry entry = null;
         while ((entry = in.getNextEntry()) != null)
         {
            if (entry.isDirectory())
            {
               File file = new File(entry.getName());
               file.mkdirs();
            }
            else
            {
               OutputStream out = new FileOutputStream(entry.getName());
               IOUtils.copy(in, out);
               out.flush();
               out.close();
            }
         }
         in.close();
         return new File(m_localDir, "objectstore");
      }
      catch (IOException e)
      {
         //log
         e.printStackTrace();
      }
      return null;
   }

   @Override
   public PSApplication createApplication()
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSApplication createApplication(String name)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void extendApplicationLock(PSApplication app, boolean overrideSameUser)
   {
//      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void extendApplicationLock(PSApplication app, int minutes,
      boolean overrideSameUser)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void extendApplicationLock(PSApplication app, int minutes)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void extendApplicationLock(PSApplication app)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void extendServerConfigurationLock(PSServerConfiguration config,
      int minutes, boolean overrideSameUser)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void extendServerConfigurationLock(PSServerConfiguration config,
      int minutes)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void extendServerConfigurationLock(PSServerConfiguration config)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * If the application exists in the sample object store
    * (see {@link #PSLocalObjectStore()}) returns it.
    * Otherwise creates new application for which name is the same as root
    * directory.
    */
   @SuppressWarnings("unused")
   @Override
   public PSApplication getApplication(String appName, boolean lockApp,
      boolean overrideSameUser)
   {
      return ms_applicationMap.containsKey(appName)
            ? ms_applicationMap.get(appName) : new PSApplication(appName) {}; 
   }

   @Override
   @SuppressWarnings("unused")
   public PSApplication getApplication(String appName, boolean lockApp)
   {
      return getApplication(appName, lockApp, false);
   }

   @Override
   public Collection<PSApplicationFile> getApplicationFiles(PSApplication app,
      PSApplicationFile appFile)
   {
      final File dirToList = getFile(app, appFile);
      insureExists(dirToList);
      if (!dirToList.isDirectory())
      {
         throw new IllegalArgumentException("Expected directory: " + dirToList);
      }
      final Collection<PSApplicationFile> files = new ArrayList<PSApplicationFile>();
      for (final File file : dirToList.listFiles())
      {
         final String name = file.getName();
         final File relFile = appFile == null ? new File(name) : new File(
            appFile.getFileName(), name);
         files.add(new PSApplicationFile(relFile, file.isDirectory()));
      }
      return files;
   }

   /**
    * File specified by application and application file.
    * 
    * @param appFile can be <code>null</code>. In this case file for root
    * directory is returned.
    */
   private File getFile(PSApplication app, PSApplicationFile appFile)
   {
      final File appDir = new File(m_localDir, app.getRequestRoot());
      return appFile == null ? appDir : new File(appDir, appFile.getFileName()
         .getPath());
   }

   @SuppressWarnings("unused")
   @Override
   public List<String> getApplicationFiles(String appRoot)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public InputStream getApplicationLog(PSApplication app, Date startTime,
      Date endTime)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public InputStream getApplicationLog(String appName, Date startTime,
      Date endTime)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Enumeration getApplicationSummaries(Properties info,
         boolean showHiddenApps, String appName)
   {
      boolean all = StringUtils.isBlank(appName);
      Enumeration available = getApplicationSummaries(info, showHiddenApps);
      Vector<Properties> summaries = new Vector<Properties>();
      while (available.hasMoreElements())
      {
         Properties appProps = (Properties) available.nextElement();
         if (all || appProps.getProperty("name").equalsIgnoreCase(appName))
         {
            summaries.add(appProps);
         }
      }
      return summaries.elements();
   }
   
   @Override
   public Enumeration getApplicationSummaries(Properties info,
      boolean showHiddenApps)
   {
      Vector<Properties> summaries = new Vector<Properties>();
      Enumeration all = getApplicationSummaries(info);
      while (all.hasMoreElements())
      {
         Properties appProps = (Properties) all.nextElement();
         if (!showHiddenApps
            && appProps.getProperty("isHidden").equals(Boolean.TRUE.toString()))
         {
            continue;
         }
         summaries.add(appProps);
      }
      return summaries.elements();
   }

   @Override
   public Enumeration getApplicationSummaries(
         @SuppressWarnings("unused") Properties info)
   {
      Vector<Properties> summaries = new Vector<Properties>();

      for (PSApplication app : ms_applicationMap.values())
      {
         Properties appProps = new Properties();
         appProps.setProperty("id", Integer.toString(app.getId()));
         appProps.setProperty("name", app.getName());
         appProps.setProperty("description", app.getDescription());
         appProps.setProperty("appType", app.getApplicationType().name());
         appProps.setProperty("isEnabled", Boolean.valueOf(app.isEnabled())
            .toString());
         appProps.setProperty("isEmpty", Boolean.valueOf(app.isEmpty())
            .toString());
         appProps.setProperty("isHidden", Boolean.valueOf(app.isHidden())
            .toString());
         summaries.add(appProps);
      }
      return summaries.elements();
   }

   @SuppressWarnings("unused")
   @Override
   public List<PSCatalogerConfig> getCatalogerConfigs(boolean lockConfig)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public Document getCharacterSetMap()
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSConnectionDetail getConnectionDetail(String datasource)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSContentEditorSharedDef getContentEditorSharedDef(boolean lock,
      boolean overrideSameUser)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSContentEditorSystemDef getContentEditorSystemDef(boolean lock,
      boolean overrideSameUser)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSDatasourceResolver getDatasourceConfigs(boolean lockConfig)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSHibernateDialectConfig getHibernateDialectConfig(boolean lockConfig)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public List<IPSJndiDatasource> getJndiDatasources(boolean lockConfig)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSRoleConfiguration getRoleConfiguration(boolean lock,
      boolean overrideSameUser, boolean overrideDifferentUser)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public IPSConfig getRxConfiguration(String name, boolean lock,
      boolean overrideSameUser, boolean overrideDifferentUser)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSServerConfiguration getServerConfiguration()
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSServerConfiguration getServerConfiguration(boolean lockConfig,
      boolean overrideSameUser)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSServerConfiguration getServerConfiguration(boolean lockConfig)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSFeatureSet getSupportedFeatureSet()
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public PSUserConfiguration getUserConfiguration()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public PSApplicationFile loadApplicationFile(PSApplication app,
      PSApplicationFile loadFile)
   {
      final File file = getFile(app, loadFile);
      insureExists(file);
      if (file.isFile())
      {
         IPSMimeContent content = null;
         try
         {
            InputStream is = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOTools.copyStream(is, bos);
            String test = bos.toString("UTF8");

            content = new PSMimeContentAdapter(new ByteArrayInputStream(test
               .getBytes("UTF8")), null, null, null, -1);
         }
         catch (FileNotFoundException e)
         {
            throw new RuntimeException(e);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
         return new PSApplicationFile(content, loadFile.getFileName());
      }
      return new PSApplicationFile(loadFile.getFileName(), loadFile.isFolder());
   }

   @SuppressWarnings("unused")
   @Override
   public IPSExtensionDef loadExtensionDef(PSExtensionRef ref)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void moveApplicationFile(PSApplication app,
      PSApplicationFile appFile, PSApplication targetApp,
      PSApplicationFile targetAppFile,
      @SuppressWarnings("unused") boolean releaseLock)
   {
      final File file = getFile(app, appFile);
      final File targetFile = getFile(targetApp, targetAppFile);
      insureExists(file);
      insureExists(targetFile.getParentFile());
      if (!file.renameTo(targetFile))
      {
         throw new IllegalArgumentException("Was not able to rename file "
            + file + " to " + targetFile);
      }
   }

   /**
    * If the specified file does not exist throws
    * {@link IllegalArgumentException}.
    */
   private void insureExists(final File file)
   {
      if (!file.exists())
      {
         throw new IllegalArgumentException("File " + file + " does not exist");
      }
   }

   @SuppressWarnings("unused")
   @Override
   public void releaseApplicationLock(PSApplication app)
   {
      // do nothing
   }

   @SuppressWarnings("unused")
   @Override
   public void releaseServerConfigurationLock(PSServerConfiguration config)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void removeApplication(PSApplication app)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void removeApplication(String appName)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removeApplicationFile(PSApplication app,
      PSApplicationFile removeFile,
      @SuppressWarnings("unused") boolean releaseLock)
   {
      final File file = getFile(app, removeFile);
      insureExists(file);
      if (file.equals(getFile(app, null)))
      {
         throw new IllegalArgumentException(
            "Can't remove virtual file root node corresponding"
               + " to application: " + file);
      }
      IOTools.deleteFile(file);
   }

   @SuppressWarnings("unused")
   @Override
   public void removeExtension(PSExtensionRef ref)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void removeUserConfiguration()
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void renameApplication(PSApplication app, String newName)
   {
      throw new UnsupportedOperationException(
            "Rename application not supported in test mode.");
   }

   @SuppressWarnings("unused")
   @Override
   public void renameApplication(String curName, String newName)
   {
      throw new UnsupportedOperationException(
            "Rename application not supported in test mode.");
   }

   @SuppressWarnings("unused")
   @Override
   public void saveApplication(PSApplication app, boolean releaseLock,
      boolean validate, boolean createNewApp)
   {
      throw new UnsupportedOperationException(
            "Save application not supported in test mode.");
   }

   @Override
   public void saveApplicationFile(PSApplication app,
      PSApplicationFile appFile, boolean overwriteIfExists,
      @SuppressWarnings("unused") boolean releaseLock)
   {
      final File file = getFile(app, appFile);
      insureExists(file.getParentFile());
      if (!overwriteIfExists && file.exists())
      {
         throw new IllegalArgumentException("File " + file
            + " should not exist");
      }
      if (appFile.isFolder())
      {
         file.mkdir();
      }
      else
      {
         try
         {
            final FileOutputStream out = new FileOutputStream(file, false);
            try
            {
               IOTools.copyStream(appFile.getContent().getContent(), out);
            }
            finally
            {
               out.close();
            }
         }
         catch (IOException e)
         {
            throw new IllegalArgumentException(e);
         }
         catch (PSIllegalStateException e)
         {
            throw new IllegalArgumentException(e);
         }
      }
   }

   @SuppressWarnings("unused")
   @Override
   public void saveCatalogerConfigs(List<PSCatalogerConfig> configs)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveDatsourceConfigs(PSDatasourceResolver configs)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveExtension(IPSExtensionDefFactory factory,
      IPSExtensionDef def, Iterator resources, boolean overwriteIfExists)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveHibernateDialectConfig(PSHibernateDialectConfig config)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveJndiDatasources(List<IPSJndiDatasource> datasources)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveRoleConfiguration(PSRoleConfiguration config,
      boolean releaseLock)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveRxConfiguration(String rxConfigName, IPSConfig rxConfig,
      boolean releaseLock)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveServerConfiguration(PSServerConfiguration config,
      boolean releaseLock)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveServerConfiguration(PSServerConfiguration config)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public Document saveTableDefinitions(PSTableLocator dbms,
      PSJdbcTableSchemaCollection defs)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void saveUserConfiguration(PSUserConfiguration config)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   @Override
   public void setClientGeneratedSessionId(String id)
   {
      throw new UnsupportedOperationException();
   }
   
   public File getLocalDir()
   {
      return m_localDir;
   }
   
   public void setLocalDir(File lDir)
   {
      m_localDir = lDir;
   }

   /**
    * Local data directory.
    */
   File m_localDir = new File("localObjectStore"); //$NON-NLS-1$

   /**
    * object store directory. This will be a subdirectory of the local data
    * directory above.
    */
   File osDir = new File(m_localDir, "objectstore"); //$NON-NLS-1$
   
   /**
    * name of the zip file that contains the object store files and is shipped
    * part of the jar file.
    */
   private static final String LOCAL_OBJECT_STORE_ZIP = "localObjectStore.zip";

   /**
    * Map of names and Applications in the local object store. Initilized in the
    * ctor, never <code>null</code> may be empty. The key is the name of the
    * application and the value is the application itself.
    */
   static Map<String, PSApplication> ms_applicationMap =
         new HashMap<String, PSApplication>();
}

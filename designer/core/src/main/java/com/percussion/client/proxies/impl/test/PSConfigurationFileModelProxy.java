/******************************************************************************
 *
 * [ PSConfigurationFileModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.PSLockException;
import com.percussion.services.system.data.PSConfigurationTypes;
import com.percussion.services.system.data.PSMimeContentAdapter;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class PSConfigurationFileModelProxy extends
   com.percussion.client.proxies.impl.PSConfigurationFileModelProxy
{
   /**
    * Default ctor for testing.
    */
   public PSConfigurationFileModelProxy()
   {
      super();
      try
      {
         init();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSConfigurationFileModelProxy#load(com.percussion.client.IPSReference[],
    * boolean, boolean)
    */
   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException
   {
      Object[] result = new Object[reference.length];
      boolean errorOccured = false;
      for (int i = 0; i < reference.length; i++)
      {
         try
         {
            PSMimeContentAdapter ada = loadAdapter(reference[i]);
            result[i] = ada;
            if (lock)
            {
               m_lockHelper.getLock(reference[i]);
            }
         }
         catch (IOException e)
         {
            logError(e);
            result[i] = e;
            errorOccured = true;
         }
      }
      if (errorOccured)
      {
         throw new PSMultiOperationException(result);
      }
      return result;
   }

   /**
    * @param ref
    * @return
    * @throws IOException
    */
   private PSMimeContentAdapter loadAdapter(IPSReference ref)
      throws IOException
   {
      InputStream is = null;
      PSMimeContentAdapter ca = null;
      ByteArrayInputStream bis = null;

      try
      {
         File file = m_fileMap.get(ref.getName());
         if (!file.exists())
         {
            throw new IOException("File does not exist: "
               + file.getAbsolutePath());
         }
         is = new FileInputStream(file);
         byte[] bytes = IOUtils.toByteArray(is);
         bis = new ByteArrayInputStream(bytes);
         ca = new PSMimeContentAdapter();
         ca.setContent(bis);
         ca.setContentLength(bytes.length);
         ca.setName(ref.getName());
         ca.setGUID(ref.getId());
         ca.setMimeType(getMimeType(ref.getObjectType()));
      }
      finally
      {
         IOUtils.closeQuietly(is);
         IOUtils.closeQuietly(bis);
      }
      return ca;

   }

   /**
    * Return appropriate mime type for the object type specified.
    * 
    * @param objectType type to get the mime type for, the primary type is
    * assumed to be {@link PSObjectTypes#CONFIGURATION_FILE}. The secondary
    * type is used to resolve the mime type.
    * @return mime type string.
    */
   private String getMimeType(PSObjectType objectType)
   {
      String mimeType = "text/xml";
      if (objectType.getSecondaryType() == PSObjectTypes.ConfigurationFileSubTypes.SERVER_PAGE_TAG_PROPERTIES)
         mimeType = "text/plain";

      return mimeType;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSConfigurationFileModelProxy#save(com.percussion.client.IPSReference[],
    * java.lang.Object[], boolean)
    */
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {
      Object[] objects = new Object[data.length];
      boolean errorOccured = false;
      for (int i = 0; i < data.length; i++)
      {
         if (!m_lockHelper.hasLock(refs[i]))
         {
            try
            {
               throw new PSLockException("save", refs[i].toString(), refs[i]
                  .getName());
            }
            catch (PSLockException e)
            {
               errorOccured = true;
               objects[i] = e;
            }
         }
         else
         {
            objects[i] = null;
            FileOutputStream fos = null;
            try
            {
               PSMimeContentAdapter ada = (PSMimeContentAdapter) data[i];
               InputStream is = ada.getContent();
               fos = new FileOutputStream(m_fileMap.get(refs[i].getName()));
               IOUtils.copy(is, fos);
               fos.flush();
            }
            catch (IOException e)
            {
               errorOccured = true;
               objects[i] = e;
            }
            finally
            {
               IOUtils.closeQuietly(fos);
            }
            ((PSReference) refs[i]).setPersisted();

            if (releaseLock)
               m_lockHelper.releaseLock(refs[i]);
            else if (!m_lockHelper.hasLock(refs[i]))
               m_lockHelper.getLock(refs[i]);
         }
      }
      if (errorOccured)
         throw new PSMultiOperationException(objects);
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * isLocked(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isLocked(IPSReference ref)
   {
      return m_lockHelper.hasLock(ref);
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * releaseLock(com.percussion.client.IPSReference[])
    */
   @Override
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      if (references == null || references.length == 0)
         throw new IllegalArgumentException(
            "reference cannot be null or empty.");
      Object[] results = new Object[references.length];
      boolean error = false;
      for (int i = 0; i < references.length; i++)
      {
         IPSReference ref = references[i];
         m_lockHelper.releaseLock(ref);
      }
      if (error)
         throw new PSMultiOperationException(results);

   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#isTypeSupported(
    * com.percussion.client.PSObjectType)
    */
   @Override
   public boolean isTypeSupported(PSObjectType type)
   {
      if (type.getPrimaryType() == m_objectPrimaryType)
         return true;
      return false;
   }

   /**
    * Initializes the file map for each of the configuration files. This method
    * assumes the files are packaged part of jar. The names (with appprporiate
    * case) are hard coded.
    * 
    * @see #importFile(String)
    * 
    * @throws IOException if initialization fails for any reason.
    */
   private void init() throws IOException
   {
      m_fileMap.put(PSConfigurationTypes.AUTH_TYPES.name(),
         importFile(PSConfigurationTypes.AUTH_TYPES.getFileName()));
      m_fileMap.put(PSConfigurationTypes.LOG_CONFIG.name(),
         importFile(PSConfigurationTypes.LOG_CONFIG.getFileName()));
      m_fileMap.put(PSConfigurationTypes.NAV_CONFIG.name(),
         importFile(PSConfigurationTypes.NAV_CONFIG.getFileName()));
      m_fileMap.put(PSConfigurationTypes.SERVER_PAGE_TAGS.name(),
         importFile(PSConfigurationTypes.SERVER_PAGE_TAGS.getFileName()));
      m_fileMap.put(PSConfigurationTypes.TIDY_CONFIG.name(),
         importFile(PSConfigurationTypes.TIDY_CONFIG.getFileName()));
      m_fileMap.put(PSConfigurationTypes.WF_CONFIG.name(),
         importFile(PSConfigurationTypes.WF_CONFIG.getFileName()));
      m_fileMap.put(PSConfigurationTypes.THUMBNAIL_CONFIG.name(),
         importFile(PSConfigurationTypes.THUMBNAIL_CONFIG.getFileName()));
      m_fileMap.put(PSConfigurationTypes.SYSTEM_VELOCITY_MACROS.name(),
         importFile(PSConfigurationTypes.SYSTEM_VELOCITY_MACROS.getFileName()));
      m_fileMap.put(PSConfigurationTypes.USER_VELOCITY_MACROS.name(),
         importFile(PSConfigurationTypes.USER_VELOCITY_MACROS.getFileName()));
   }

   /**
    * Instantiate the {@link File} object with the name specified and return it.
    * It assumes the file exists in the current directory. I does not exist
    * loads from the jar file to the current directory and then returns.
    * 
    * @param name name of the file to instantiate and return. Assumed not
    * <code>null</code> or empty.
    * @return the file object as explained above.
    * @throws IOException if it cannot import the file from remote directory.
    */
   private static File importFile(String name) throws IOException
   {
      File tgt = new File(name);
      if (tgt.exists())
         return tgt;
      InputStream is = null;
      FileOutputStream fos = null;
      try
      {
         is = PSConfigurationFileModelProxy.class.getResourceAsStream(name);
         fos = new FileOutputStream(tgt);
         IOUtils.copy(is, fos);
         fos.flush();
      }
      catch (IOException e)
      {
         e.printStackTrace();
         throw e;
      }
      finally
      {
         IOUtils.closeQuietly(is);
         IOUtils.closeQuietly(fos);
      }
      return tgt;
   }

   /**
    * The lock helper for this test proxy
    */
   private PSLockHelper m_lockHelper = new PSLockHelper();

   /**
    * Map of the config files internal names to their {@link File} objects.
    */
   private Map<String, File> m_fileMap = new HashMap<String, File>();

}

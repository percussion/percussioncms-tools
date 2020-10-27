/******************************************************************************
 *
 * [ PSLocaleModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreMessages;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.i18n.PSLocale;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

/**
 * Test proxy.
 */
public class PSLocaleModelProxy extends PSTestModelProxy
{
   /**
    * Default ctor. Loads the sample locale objects from local repository.
    * Creates if repository does not already exist.
    */
   public PSLocaleModelProxy()
   {
      super(PSObjectTypes.LOCALE);
      try
      {
         loadFromRepository();
         PSXmlSerializationHelper.addType(PSLocale.class);
         PSXmlSerializationHelper.addType(PSLocaleMap.class);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * @throws PSProxyTestException
    * 
    */
   private void loadFromRepository() throws PSProxyTestException
   {
      m_repositoryMap.clear();
      if (!ms_repository.exists())
      {
         // If repository does not exist
         // create a few to start with
         for (int i = 0; i < 3; i++)
         {
            String[] names = new String[]
            {
               "en-us", "fr-fr", "ja-jp"
            };
            int[] ids = new int[]
            {
               10, 100, 200
            };
            PSLocale locale = createNewObject(names[i], ids[i]);
            m_repositoryMap.put(objectToReference(locale), locale);
         }
         // and save to repository
         PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = (PSLocaleMap) PSProxyTestUtil
            .loadRepository(ms_repository);
      }
   }

   /**
    * Helper method to create a new {@link PSLocale} object with a giben name
    * and index.
    * 
    * @param name language string, assumed not <code>null</code>.
    * @param i
    * @return new Locale, never <code>null</code>.
    */
   private PSLocale createNewObject(String name, int i)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      PSLocale locale = new PSLocale();
      locale.setLocaleId(i);
      locale.setLanguageString(name);
      locale.setDisplayName(name);
      locale.setDescription(PSCoreMessages
         .getString("common.prefix.description") //$NON-NLS-1$
         + " " + name); //$NON-NLS-1$      
      return locale;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#rename(com.percussion.client.IPSReference,
    * java.lang.String, java.lang.Object)
    */
   @Override
   public void rename(IPSReference ref, String name, Object data)
   {

      // we may not know about the data object if it hasn't been persisted
      PSLocale locale = (PSLocale) m_repositoryMap.get(ref);
      if (locale != null)
      {
         locale.setLanguageString(name);
         ((PSReference) ref).setName(name);
         try
         {
            PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error saving to repository file "
               + ms_repository, e);
         }
      }
      renameLocal(ref, name, data);
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      ((PSReference) ref).setName(name);
      if (data != null)
      {
         ((PSLocale) data).setLanguageString(name);
      }
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List results)
   {
      if (sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty.");
      if (names != null)
      {
         throw new IllegalArgumentException(
               "this proxy does not support supplied names");
      }
      if (results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for (int i = 0; i < sourceObjects.length; i++)
      {
         if (sourceObjects[i] instanceof PSLocale)
         {
            try
            {
               PSLocale locale = (PSLocale) clone(sourceObjects[i]);
               locale.setLanguageString(locale.getLanguageString() + i);
               locale.setDisplayName(PSCoreMessages
                  .getString("common.prefix.copyof") //$NON-NLS-1$
                  + locale.getName());
               results.add(locale);
               refs[i] = objectToReference(locale);
               m_lockHelper.getLock(refs[i]);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }
         else
         {
            throw new IllegalArgumentException(
               "sourceObjects must be instances of PSKeyword");
         }
      }
      return refs;
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results)
   {
      if (objType == null
         || !objType.getPrimaryType().equals(m_objectPrimaryType))
         throw new IllegalArgumentException("objType is invalid.");
      if (names == null)
         throw new IllegalArgumentException("names cannot be null.");
      if (results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for (String name : names)
      {
         SecureRandom rand = new SecureRandom();
         int uuid = rand.nextInt() & Integer.MAX_VALUE;
         PSLocale locale = createNewObject(name, uuid);
         refs[++idx] = objectToReference(locale);
         m_lockHelper.getLock(refs[idx]);
         results.add(locale);
      }
      return refs;
   }

   /**
    * @param type
    */
   public PSLocaleModelProxy(PSObjectTypes type)
   {
      super(type);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.test.PSTestModelProxy#getRepositoryMap()
    */
   @Override
   protected IPSRepositoryMap getRepositoryMap()
   {
      return m_repositoryMap;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.test.PSTestModelProxy#getRepositoryFile()
    */
   @Override
   protected File getRepositoryFile()
   {
      return ms_repository;
   }

   /**
    * Name of the repository file.
    */
   public static final String REPOSITORY_XML = "locale_repository.xml";

   /**
    * Repository file name this test proxy uses. The file will be created in the
    * root directory for the workbench if one does not exist. It will use the
    * existing one if one exists.
    */
   static private File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all object from the repository. Filled during initialization of the
    * proxy.
    */
   private PSLocaleMap m_repositoryMap = new PSLocaleMap();

}

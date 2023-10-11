/******************************************************************************
 *
 * [ PSRelationshipTypeModelProxy.java ]
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
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#RELATIONSHIP_TYPE}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @since 03-Sep-2005 4:39:27 PM
 */
public class PSRelationshipTypeModelProxy extends PSComponentTestModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#RELATIONSHIP_TYPE} and for main type and
    * <code>null</code> sub type since this object type does not have any sub
    * types.    
    */
   public PSRelationshipTypeModelProxy()
   {
      super(PSObjectTypes.RELATIONSHIP_TYPE);
      try
      {
         loadFromRepository();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /* 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @Override
   public IPSReference[] create(
      final PSObjectType objType, final Collection<String> names,
      final List<Object> results)
   {
      if (objType == null
         || !objType.getPrimaryType().equals(m_objectPrimaryType))
         throw new IllegalArgumentException("objType is invalid."); //$NON-NLS-1$
      if(names == null)
         throw new IllegalArgumentException("names cannot be null."); //$NON-NLS-1$
      if(results == null)
         throw new IllegalArgumentException("results cannot be null."); //$NON-NLS-1$
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for(String name : names)
      {         
         PSRelationshipConfig config = createNewObject(name, -1);         
         refs[++idx] = objectToReference(config);
         results.add(config);
         m_lockHelper.getLock(refs[idx]);
        
      }
      return refs;
   }

   /* 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @Override
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      if(sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty."); //$NON-NLS-1$
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if(results == null)
         throw new IllegalArgumentException("results cannot be null."); //$NON-NLS-1$
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for(int i = 0; i < sourceObjects.length; i++)
      {
         if(sourceObjects[i] instanceof PSRelationshipConfig)
         {
            try
            {
               PSRelationshipConfig config = 
                  (PSRelationshipConfig)clone(sourceObjects[i]);
               
               String name;
               if (names == null || StringUtils.isBlank(names[i]))
               {
                  name = PSCoreMessages.getString("common.prefix.copyof") //$NON-NLS-1$
                        + config.getName();
               }
               else
                  name = names[i];
               config.setName(name); 
               config.setLabel(config.getName()); 
               results.add(config);
               refs[i] = objectToReference(config);
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
                  "sourceObjects must be instances of PSRelationshipConfig"); //$NON-NLS-1$
         }
      }
      return refs;
   }
   
   /* 
    * @see com.percussion.client.proxies.impl.PSKeywordModelProxy#rename(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void rename(IPSReference ref, String name, Object data) 
   {
      // we may not know about the data object if it hasn't been persisted
      PSRelationshipConfig config = (PSRelationshipConfig) m_repositoryMap.get(ref);
      if (config != null)
      {
         config.setName(name);
         try
         {
            saveRepository(m_repositoryMap, ms_repository);
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
         ((PSRelationshipConfig) data).setName(name);
      }      
   }

   /**
    * Load the existing objects from the repository.
    * @throws PSProxyTestException  
    */
   @SuppressWarnings("unchecked")
   private void loadFromRepository() throws PSProxyTestException 
   {
      m_repositoryMap.clear();
      if (!ms_repository.exists())
      {
         createRelTypes();
         
         // and save to repository
         saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = 
            (PSRepositoryMap)loadRepository(ms_repository);
      }
   }   
   
   private void createRelTypes()
   {
      for(PSRelationshipConfig config : getTestConfigs())
      {
         m_repositoryMap.put(objectToReference(config), config);
      }
      
   }

   /**
    * Convenience to return a <code>PSRelationshipConfig</code> object for
    * testing purposes only. A dummy guid will be generated instead
    * of a real one from the database. The value will be used as the
    * label and in the description.
    * @param name @param name for which to create a new entry, not 
    *    <code>null</code> or empty.
    */   
   @SuppressWarnings("unused")
   private PSRelationshipConfig createNewObject(String name,  int id)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty"); //$NON-NLS-1$
      PSRelationshipConfig config = null;
      PSRelationshipConfig starterConfig = getTestConfigs()[0];
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         config = 
            new PSRelationshipConfig(starterConfig.toXml(doc), null, null);
         config.setName(name);
         config.setLabel(name);
         config.setDescription(
            PSCoreMessages.getString("common.prefix.description") + " " + name);
         config.setType(PSRelationshipConfig.RS_TYPE_USER);
      }
      catch (PSUnknownNodeTypeException e)
      {
         e.printStackTrace();
      }
      
      return config;      
   }
   
   /**
    * Retrieves the test configs from the xml file.
    * @return never <code>null</code> may be empty.
    */
   private PSRelationshipConfig[] getTestConfigs()
   {
      if(m_testData != null)
         return m_testData;
      Element root = getXmlData().getDocumentElement();
      NodeList nl = root.getElementsByTagName("PSXRelationshipConfig");
      int len = nl.getLength();
      m_testData = new PSRelationshipConfig[len];
      try
      {
         for(int i = 0; i < len; i++)
         {
            m_testData[i] = new PSRelationshipConfig((Element)nl.item(i), null, null);
            m_testData[i].setId(i+1);
         }
      }
      catch (PSUnknownNodeTypeException e)
      {
         e.printStackTrace();
      }
      return m_testData;
   }
   
   /**
    * Returns the xml document containing the test relationship config
    * starter data.
    * @return dom document, may be <code>null</code> if failed to load.
    */
   private Document getXmlData()
   {
      Class clazz = getClass();
      InputStream is = null;
      Document doc = null;
      try
      {
         is = clazz.getResourceAsStream("relationshipConfigs.xml");
         InputSource source = new InputSource(is);
         doc = PSXmlDocumentBuilder.createXmlDocument(source, false);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (SAXException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      finally
      {
         if(is != null)
            try
            {
               is.close();
            }
            catch (IOException ignore){}
      }
      return doc;
      
   }
  
   
   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryFile()
    */
   @Override
   protected File getRepositoryFile()
   {
      return ms_repository;
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryMap()
    */
   @Override
   protected IPSRepositoryMap getRepositoryMap()
   {
      return m_repositoryMap;
   }
   
   @Override
   protected Object newComponentInstance()
   {
      return createNewObject("dummy", -1);
   }
   
   
   /**
    * Name of the repository file.
    */
   public static final String REPOSITORY_XML = "relType_repository.xml";

   /**
    * Repository file name this test proxy uses. The file will be created
    * in the root directory for the workbench if one does not exist. It will use
    * the existing one if one exists.
    */
   static private File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all object from the repository. Filled during initialization of the
    * proxy.
    */
   private PSRepositoryMap m_repositoryMap = 
      new PSRepositoryMap();
   
   private PSRelationshipConfig[] m_testData;

  

   
}

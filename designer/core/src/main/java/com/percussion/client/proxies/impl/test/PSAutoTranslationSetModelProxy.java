/******************************************************************************
 *
 * [ PSAutoTranslationSetModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.*;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.PSLockException;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;

public class PSAutoTranslationSetModelProxy extends PSTestModelProxy
{

   public PSAutoTranslationSetModelProxy()
   {
      super(PSObjectTypes.AUTO_TRANSLATION_SET);
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
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * catalog(com.percussion.client.IPSReference)
    */
   @SuppressWarnings("unused") //exception
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Collection<IPSReference> results = new ArrayList<>();

      if (!m_set.isEmpty())
      {
         PSReference pRef = (PSReference) objectToReference(new HashSet<>());
         pRef.setPersisted();
         results.add(pRef);
      }
      return results;
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * load(com.percussion.client.IPSReference[], boolean, boolean)
    */
   @SuppressWarnings("unchecked") //overrideLock
   @Override
   public Object[] load(IPSReference[] references, boolean lock,
         @SuppressWarnings("unused") boolean overrideLock) 
      throws PSMultiOperationException
   {

      if (references == null || references.length == 0)
         throw new IllegalArgumentException(
            "reference cannot be null or empty.");
      Object[] results = new Object[references.length];
      boolean error = false;
      if (!m_set.isEmpty() && references.length > 0)
      {
         IPSReference ref = references[0];
         // Attempt to get lock if necessary
         if (lock)
         {
            m_lockHelper.getLock(ref);
         }
         // Retrieve the object from the repository
         Object obj = m_set;
         if (obj == null)
         {
            results[0] = new PSModelException(PSErrorCodes.RAW, new Object[]
            {
               "Not found in repository."
            });
            error = true;
         }
         try
         {
            obj = new HashSet(m_set);
            results[0] = obj;
         }
         catch (Exception e)
         {
            results[0] = e;
            error = true;
         }
      }
      if (error)
         throw new PSMultiOperationException(results);

      return results;
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * save(com.percussion.client.IPSReference[], java.lang.Object[], boolean)
    */
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {

      Object[] objects = new Object[data.length];
      boolean error = false;
      if (refs.length > 0)
      {
         if (!m_lockHelper.hasLock(refs[0]))
         {
            try
            {
               throw new PSLockException("save",
                  m_objectPrimaryType.toString(), refs[0].getName());
            }
            catch (PSLockException e)
            {
               error = true;
               objects[0] = e;
            }
         }
         else
         {
            ((PSReference) refs[0]).setPersisted();
            m_set = ((Set) data[0]);

            objects[0] = null;
            if (releaseLock)
               m_lockHelper.releaseLock(refs[0]);
            else if (!m_lockHelper.hasLock(refs[0]))
               m_lockHelper.getLock(refs[0]);
         }
      }
      if (error)
         throw new PSMultiOperationException(objects);
      try
      {
         saveSet();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error saving to repository file "
            + ms_repository.getAbsolutePath(), e);
      }

   }

   /*
    * @see com.percussion.client.proxies.impl.test.PSTestModelProxy#delete(
    * com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   @Override
   public void delete(IPSReference[] references)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List<Object> results)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#rename(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @SuppressWarnings("unused")
   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @SuppressWarnings("unused")
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Load the existing objects from the repository.
    * 
    * @throws PSProxyTestException
    */
   @SuppressWarnings("unchecked")
   private void loadFromRepository() throws PSProxyTestException
   {

      if (!ms_repository.exists())
      {
         // Create example auto translation set
         m_set = new HashSet();

         PSAutoTranslation at = new PSAutoTranslation();
         at.setContentTypeId(1108101562408L);
         at.setCommunityId(10);
         at.setWorkflowId(1198295875588L);
         at.setLocale("en-us");
         m_set.add(at);

         at = new PSAutoTranslation();
         at.setContentTypeId(1108101562378L);
         at.setCommunityId(100);
         at.setWorkflowId(1198295875584L);
         at.setLocale("fr-fr");
         m_set.add(at);

         at = new PSAutoTranslation();
         at.setContentTypeId(1108101562388L);
         at.setCommunityId(200);
         at.setWorkflowId(1198295875588L);
         at.setLocale("en-us");
         m_set.add(at);

         // and save to repository
         saveSet();
      }
      else
      {
         loadSet();
      }
   }

   @SuppressWarnings("unchecked")
   private void saveSet() throws PSProxyTestException
   {

      try(FileWriter writer = new FileWriter(ms_repository))
      {
         writer.write(PSXmlDocumentBuilder.toString(toXml(m_set)));
         writer.flush();
      }
      catch (Exception e)
      {
         throw new PSProxyTestException(e);
      }

   }

   private void loadSet() throws PSProxyTestException
   {
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
                 Files.newInputStream(ms_repository.toPath()), false);
         m_set = fromXml(doc.getDocumentElement());
      }
      catch (Exception e)
      {
         throw new PSProxyTestException(e);
      }

   }

   private Element toXml(Set<PSAutoTranslation> set)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement(ELEM_SET);
      doc.appendChild(root);
      for (PSAutoTranslation trans : set)
      {
         Element node = doc.createElement(ELEM_AUTO_TRANS);
         node.setAttribute(ATTR_CONTENTTYPEID, String.valueOf(trans
            .getContentTypeId()));
         node.setAttribute(ATTR_COMMUNITYID, String.valueOf(trans
            .getCommunityId()));
         node.setAttribute(ATTR_WORKFLOWID, String.valueOf(trans
            .getWorkflowId()));
         node.setAttribute(ATTR_LOCALE, trans.getLocale());
         String version = trans.getVersion() != null ? String.valueOf(trans
            .getVersion()) : "";
         node.setAttribute(ATTR_VERSION, version);
         root.appendChild(node);

      }
      return root;
   }

   private Set<PSAutoTranslation> fromXml(Element root)
   {
      Set<PSAutoTranslation> set = new HashSet<>();
      NodeList nl = root.getElementsByTagName(ELEM_AUTO_TRANS);
      int len = nl.getLength();
      for (int i = 0; i < len; i++)
      {
         Element node = (Element) nl.item(i);
         PSAutoTranslation trans = new PSAutoTranslation();
         trans
            .setCommunityId(Long.parseLong(node.getAttribute(ATTR_COMMUNITYID)));
         trans.setContentTypeId(Long.parseLong(node
            .getAttribute(ATTR_CONTENTTYPEID)));
         trans.setLocale(node.getAttribute(ATTR_LOCALE));
         try
         {
            trans.setVersion(Integer.valueOf(node.getAttribute(ATTR_VERSION)));
         }
         catch (NumberFormatException ignore)
         {

         }
         trans.setWorkflowId(Long.parseLong(node.getAttribute(ATTR_WORKFLOWID)));
         set.add(trans);
      }
      return set;
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
      return null;
   }

   /**
    * Name of the repository file.
    */
   public static final String REPOSITORY_XML = "autoTrans_repository.xml";

   /**
    * Repository file name this test proxy uses. The file will be created in the
    * root directory for the workbench if one does not exist. It will use the
    * existing one if one exists.
    */
    private static File ms_repository = new File(REPOSITORY_XML);

   /**
    * Set of auto translation objects
    */
   private Set m_set;

   private static final String ELEM_SET = "Set";

   private static final String ELEM_AUTO_TRANS = "PSAutoTranslation";

   private static final String ATTR_CONTENTTYPEID = "contenttypeid";

   private static final String ATTR_WORKFLOWID = "workflowid";

   private static final String ATTR_VERSION = "version";

   private static final String ATTR_COMMUNITYID = "communityid";

   private static final String ATTR_LOCALE = "locale";

}

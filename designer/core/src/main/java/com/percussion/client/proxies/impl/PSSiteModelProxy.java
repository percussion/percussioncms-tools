/******************************************************************************
 *
 * [ PSSiteModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.util.PSRemoteRequester;
import com.percussion.xml.serialization.PSObjectSerializer;
import com.percussion.xml.serialization.PSObjectSerializerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Provides cataloging read-only load services for the object type
 * {@link com.percussion.client.PSObjectTypes#SITE}. Uses base class
 * implementation whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 */

public class PSSiteModelProxy extends PSLegacyModelProxy
{
   /**
    * Default ctor. Invokes base class version with object type
    * {@link PSObjectTypes#SITE}.
    * 
    */
   public PSSiteModelProxy()
   {
      super(PSObjectTypes.SITE);
      PSXmlSerializationHelper.addType(PSObjectSummary.class);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#catalog()
    */
   public Collection<IPSReference> catalog() throws PSModelException
   {
      List<IPSReference> siteList = new ArrayList<IPSReference>();
      PSCoreFactory factory = PSCoreFactory.getInstance();
      PSRemoteRequester remoteRequester = factory.getRemoteRequester();
      Exception ex = null;
      try
      {
         Document doc = remoteRequester.getDocument(LOOKUP_RESOURCE,
            new HashMap());
         PSObjectSummary summary = null;
         NodeList nl = doc.getElementsByTagName("object-summary"); //$NON-NLS-1$
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element elem = (Element) nl.item(i);
            summary = (PSObjectSummary) PSObjectSerializer.getInstance()
               .fromXml(elem);
            //fixme remove when summary deserialization works
            if (summary.getId() == 0)
               continue;
            //
            siteList.add(summaryToReference(summary));
         }
      }
      catch (IOException e)
      {
         ex = e;
      }
      catch (SAXException e)
      {
         ex = e;
      }
      catch (PSObjectSerializerException e)
      {
         ex = e;
      }
      if (ex != null)
      {
         throw new PSModelException(ex);
      }
      return siteList;
   }

   /**
    * Create a reference object from object summary object.  Does not perform
    * validation on reference names.
    * 
    * @param s summary object, assumed not <code>null</code>.
    * @return reference object, never <code>null</code>.
    */
   private IPSReference summaryToReference(PSObjectSummary s)
   {
      // We override to avoid name validation for sites
      PSReference ref = new PSReference()
      {
         @Override
         public void setName(String name)
         {
            if (name == null || name.length() == 0)
            {
               throw new IllegalArgumentException(
                  "name must not be null or empty");
            }
            m_name = name;
         }
      };
      ref.setDescription(s.getDescription());
      ref.setName(s.getName());
      ref.setId(new PSDesignGuid(s.getGUID()));
      try
      {
         ref.setObjectType(PSObjectTypeFactory.getType(PSObjectTypes.SITE));
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
      ref.setLabelKey(s.getLabel());
      // Sites are read only->always persisted
      ref.setPersisted();
      return ref;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#create(com.percussion.client.PSObjectType,
    * java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unused")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results)
   {
      throw new UnsupportedOperationException(DOES_NOT_SUPPORT_THIS_METHOD);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#load(com.percussion.client.IPSReference[],
    * boolean, boolean)
    */
   @SuppressWarnings("unused")
   public Object[] load(IPSReference[] reference, boolean lockForEdit,
      boolean overrideLock) throws PSMultiOperationException
   {
      return new Object[reference.length];
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#delete(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   public void delete(IPSReference[] reference)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException(DOES_NOT_SUPPORT_THIS_METHOD);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#save(com.percussion.client.IPSReference[],
    * java.lang.Object[], boolean)
    */
   @SuppressWarnings("unused")
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException(DOES_NOT_SUPPORT_THIS_METHOD);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#rename(com.percussion.client.IPSReference,
    * java.lang.String, java.lang.Object)
    */
   @SuppressWarnings("unused")
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      throw new UnsupportedOperationException(DOES_NOT_SUPPORT_THIS_METHOD);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#releaseLock(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      /*
       * The current workbench does not allow editing sites. Because of that
       * this is a no-op.
       */
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#isLocked(com.percussion.client.IPSReference)
    */
   @SuppressWarnings("unused")
   public boolean isLocked(IPSReference ref)
   {
      /*
       * The current workbench does not allow editing sites. Because of that
       * we assume sites are never locked.
       */
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#create(java.lang.Object[],
    * java.util.List)
    */
   @SuppressWarnings("unused")
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List results)
   {
      throw new UnsupportedOperationException(DOES_NOT_SUPPORT_THIS_METHOD);
   }
   
   @Override
   public Object[] loadAcl(IPSReference[] ref, boolean lock)
      throws PSModelException, PSMultiOperationException
   {
      // We need IPSCmsModelProxy for site that does not exist, can we borrow
      // from workflow object type?
      IPSCmsModelProxy proxy = PSCoreFactory.getInstance().getCmsModelProxy(
         PSObjectTypes.WORKFLOW);
      return proxy.loadAcl(ref, lock);
   }

   @Override
   public void saveAcl(IPSReference[] ref, IPSAcl[] acl, boolean releaseLock)
      throws PSModelException, PSMultiOperationException
   {
      // We need IPSCmsModelProxy for site that does not exist, can we borrow
      // from workflow object type?
      IPSCmsModelProxy proxy = PSCoreFactory.getInstance().getCmsModelProxy(
         PSObjectTypes.WORKFLOW);
      proxy.saveAcl(ref, acl, releaseLock);
   }

   /**
    * String constant for the message used repeatedly.
    */
   private static final String DOES_NOT_SUPPORT_THIS_METHOD = "Does not support this method"; //$NON-NLS-1$

   /**
    * Resource to get the XML document of the list of object summaries fo the
    * sites from the server.
    */
   private static final String LOOKUP_RESOURCE = "sitelist"; //$NON-NLS-1$
}

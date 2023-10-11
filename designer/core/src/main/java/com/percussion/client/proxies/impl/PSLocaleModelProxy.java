/******************************************************************************
 *
 * [ PSLocaleModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSObjectFactory;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.webservices.content.PSLocale;
import com.percussion.webservices.contentdesign.ContentDesignSOAPStub;
import com.percussion.webservices.contentdesign.CreateLocalesRequest;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.transformation.PSTransformationException;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#LOCALE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSLocaleModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#LOCALE} and for main type and <code>null</code> subtype
    * since this object type does not have any subtypes.
    */
   public PSLocaleModelProxy()
   {
      super(PSObjectTypes.LOCALE);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(com.percussion.client.PSObjectType,
    * java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results) throws PSMultiOperationException, PSModelException
   {
      if (names == null || names.isEmpty())
      {
         throw new IllegalArgumentException("names must not be null or empty");
      }
      if (objType == null)
      {
         throw new IllegalArgumentException("objType must not be null");
      }
      if (results == null)
      {
         throw new IllegalArgumentException("results must not be null");
      }
      results.clear();
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               ContentDesignSOAPStub binding = 
                  (ContentDesignSOAPStub) getSoapBinding(METHOD.CREATE);
      
               CreateLocalesRequest request = new CreateLocalesRequest();
               request.setCode(names.toArray(new String[0]));
               request.setLabel(names.toArray(new String[0]));
               PSLocale[] locales = binding.createLocales(request);
               com.percussion.i18n.PSLocale[] converted = 
                  (com.percussion.i18n.PSLocale[]) PSProxyUtils.convert(
                     com.percussion.i18n.PSLocale[].class, locales);
               for (int i = 0; i < converted.length; i++)
                  results.add(converted[i]);
      
               IPSReference[] refs = PSObjectFactory.objectToReference(converted,
                  (IPSPrimaryObjectType) objType.getPrimaryType(), true);
               PSProxyUtils.setLockInfo(refs, false);
               return refs;
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (SecurityException | ServiceException | RemoteException e)
      {
         ex = e;
      } catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }

       if (ex != null)
         processAndThrowException(names.size(), ex);
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(com.percussion.client.IPSReference,
    * java.lang.String, java.lang.Object)
    */
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      if (name == null || name.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      if (ref == null && data == null)
      {
         throw new IllegalArgumentException(
            "ref and data both must not be null");
      }
      if (ref != null)
      {
         ((PSReference) ref).setName(name);
      }
      if (data == null)
         return;
      ((com.percussion.i18n.PSLocale) data).setLanguageString(name);
   }

   /**
    * Overrides base class method to return the SOAP stub appropriate for this
    * type object.
    * 
    * @see PSCmsModelProxy#getSoapBinding(IPSCmsModelProxy.METHOD)
    * 
    */
   @Override
   protected Stub getSoapBinding(@SuppressWarnings("unused")
   METHOD method) throws MalformedURLException, ServiceException
   {
      return PSProxyUtils.getContentDesignStub();
   }
}

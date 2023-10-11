/******************************************************************************
 *
 * [ PSAutoTranslationSetModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.*;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.security.PSPermissions;
import com.percussion.webservices.contentdesign.ContentDesignSOAPStub;
import com.percussion.webservices.contentdesign.LoadTranslationSettingsRequest;
import com.percussion.webservices.contentdesign.SaveTranslationSettingsRequest;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#AUTO_TRANSLATION_SET}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @since 03-Sep-2005 4:39:27 PM
 */
public class PSAutoTranslationSetModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#AUTO_TRANSLATION_SET} and for main type and
    * <code>null</code> sub type since this object type does not have any sub
    * types.
    */
   public PSAutoTranslationSetModelProxy()
   {
      super(PSObjectTypes.AUTO_TRANSLATION_SET);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog()
   {
      IPSReference ref = PSCoreUtils.createReference(AUTO_TRANSLATIONS,
         AUTO_TRANSLATIONS, AUTO_TRANSLATIONS, PSObjectTypeFactory
            .getType(PSObjectTypes.AUTO_TRANSLATION_SET), PSAutoTranslation
            .getAutoTranslationsGUID());
      ((PSReference) ref).setPersisted();
      /*
       * Because we did not actually catalog the reference (instead we created
       * one for a virtual design object), we need to set the ACL's manually.
       */
      ((PSReference) ref).setPermissions(new int[] { 
         PSPermissions.READ.getOrdinal(),
         PSPermissions.UPDATE.getOrdinal(),
         PSPermissions.DELETE.getOrdinal(),
         PSPermissions.OWNER.getOrdinal() });
      
      Collection<IPSReference> coll = new HashSet<IPSReference>(1);
      coll.add(ref);
      return coll;
   }

   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException, PSModelException
   {
      if (reference == null || reference.length != 1)
      {
         throw new IllegalArgumentException(
            "reference array must not be null and must be of size 1");
      }
      assert reference[0].getObjectType().getPrimaryType() 
            == PSObjectTypes.AUTO_TRANSLATION_SET;
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
                  (ContentDesignSOAPStub) getSoapBinding(METHOD.LOAD);
               LoadTranslationSettingsRequest req = 
                  new LoadTranslationSettingsRequest(lock, overrideLock);
               com.percussion.services.content.data.PSAutoTranslation[] result = 
                  (com.percussion.services.content.data.PSAutoTranslation[]) 
                  PSProxyUtils.convert(
                     com.percussion.services.content.data.PSAutoTranslation[].class,
                     binding.loadTranslationSettings(req));
               Set<PSAutoTranslation> set = new HashSet<PSAutoTranslation>();
               for (PSAutoTranslation trans : result)
                  set.add(trans);
               
               if(lock)
                  PSProxyUtils.setLockInfo(reference, false);
      
               return new Object[] { set };
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
      catch (ServiceException | MalformedURLException | RemoteException e)
      {
         ex = e;
      }


      if (ex != null)
         processAndThrowException(reference.length, ex);
      
      // will never get here
      return null;
   }

   /* (non-Javadoc)
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#save(
    *    com.percussion.client.IPSReference[], java.lang.Object[], boolean)
    */
   @SuppressWarnings({"unchecked"})
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock) 
      throws PSMultiOperationException, PSModelException
   {
      if (refs == null || refs.length != 1)
      {
         throw new IllegalArgumentException(
            "reference array must not be null and must be of size 1");
      }
      if (data == null || data.length != 1)
      {
         throw new IllegalArgumentException(
            "data array must not be null and must be of size 1");
      }
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               com.percussion.webservices.content.PSAutoTranslation[] converted = 
                  (com.percussion.webservices.content.PSAutoTranslation[]) 
                  PSProxyUtils.convert(
                     com.percussion.webservices.content.PSAutoTranslation[].class,
                     ((Set) data[0]).toArray(new PSAutoTranslation[0]));
               ContentDesignSOAPStub binding = 
                  (ContentDesignSOAPStub) getSoapBinding(METHOD.SAVE);
               SaveTranslationSettingsRequest req = 
                  new SaveTranslationSettingsRequest();
               req.setPSAutoTranslation(converted);
               req.setRelease(releaseLock);
               binding.saveTranslationSettings(req);
               if(releaseLock)
                  PSProxyUtils.setLockInfo(refs, true);
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
      } catch (MalformedURLException | ServiceException | RemoteException e)
      {
         ex = e;
      }


      if (ex != null)
         processAndThrowException(refs.length, ex);
   }

   @Override
   @SuppressWarnings("unused") 
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void delete(@SuppressWarnings("unused") IPSReference[] reference)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   @SuppressWarnings("unused") 
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   @SuppressWarnings("unused") 
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   @SuppressWarnings("unused") 
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Overrides base class method to return the SOAP stub appropriate for this
    * type object.
    * 
    * @see PSCmsModelProxy#getSoapBinding(IPSCmsModelProxy.METHOD)
    * 
    */
   @Override
   @SuppressWarnings("unused") 
   protected Stub getSoapBinding(METHOD method) throws MalformedURLException,
      ServiceException
   {
      return PSProxyUtils.getContentDesignStub();
   }

   /**
    * Name constant of the only auto translations object.
    */
   private static final String AUTO_TRANSLATIONS = "TranslationSettings";
}

/******************************************************************************
 *
 * [ PSContentTypeSystemConfigModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
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
import com.percussion.client.PSObjectTypes;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.webservices.content.PSContentEditorDefinition;
import com.percussion.webservices.contentdesign.ContentDesignSOAPStub;
import com.percussion.webservices.contentdesign.LoadSystemDefinitionRequest;
import com.percussion.webservices.contentdesign.LoadSystemDefinitionResponse;
import com.percussion.webservices.contentdesign.SaveSystemDefinitionRequest;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.transformation.PSTransformationException;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#CONTENT_TYPE_SYSTEM_CONFIG}. Uses base class
 * implementation whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSContentTypeSystemConfigModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#CONTENT_TYPE_SYSTEM_CONFIG} and for main type and
    * <code>null</code> sub type since this object type foes not have any sub
    * types.
    */
   public PSContentTypeSystemConfigModelProxy()
   {
      super(PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG);
   }

   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results)
   {
      throw new UnsupportedOperationException(
         "create is not supported for this object");
   }

   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List results)
   {
      throw new UnsupportedOperationException(
         "Create is not supported for this object");
   }

   @SuppressWarnings("unused")
   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      throw new UnsupportedOperationException(
         "rename is not supported for this object");
   }

   @SuppressWarnings("unused")
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException(
         "renameLocal is not supported for this object");
   }

   @SuppressWarnings("unused")
   @Override
   public void delete(IPSReference[] reference)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException(
         "delete is not supported for this object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog()
   {
      Collection<IPSReference> results = new ArrayList<IPSReference>();

      IPSReference pRef = PSProxyUtils.getSystemDefReference();
      results.add(pRef);

      return results;
   }

   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException, PSModelException
   {
      if (reference == null || reference.length != 1)
      {
         throw new IllegalArgumentException(
            "reference[] must not be null and must be of size 1");
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
               ContentDesignSOAPStub binding = getSoapBinding(METHOD.LOAD);
               LoadSystemDefinitionRequest request = 
                  new LoadSystemDefinitionRequest();
               request.setLock(lock);
               request.setOverrideLock(overrideLock);
               LoadSystemDefinitionResponse response = binding
                  .loadSystemDefinition(request);
               PSContentEditorSystemDef sysDef = 
                  (PSContentEditorSystemDef) PSProxyUtils.convert(
                     PSContentEditorSystemDef.class, 
                     response.getPSContentEditorDefinition());
               if(lock)
                  PSProxyUtils.setLockInfo(reference, false);
               return new Object[]
               {
                  sysDef
               };
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
      catch (PSLockFault e)
      {
         ex = PSProxyUtils.convertFault(e, METHOD.LOAD.toString(), 
            reference[0].getObjectType().getPrimaryType().toString(), 
            reference[0].getName());
      }
      catch (PSNotAuthorizedFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSTransformationException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(reference.length, ex);
      
      // will never get here
      return null;
   }

   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      if (refs == null || refs.length != 1)
      {
         throw new IllegalArgumentException(
            "refs[] must not be null and must be of size 1");
      }
      if (data == null || data.length != 1)
      {
         throw new IllegalArgumentException(
            "data[] must not be null and must be of size 1");
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
               PSContentEditorDefinition def = 
                  (PSContentEditorDefinition) PSProxyUtils.convert(
                     PSContentEditorDefinition.class, data[0]);
               ContentDesignSOAPStub binding = getSoapBinding(METHOD.SAVE);
               SaveSystemDefinitionRequest request = 
                  new SaveSystemDefinitionRequest();
               request.setRelease(releaseLock);
               request.setPSContentEditorDefinition(def);
               binding.saveSystemDefinition(request);
               if (releaseLock)
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
      }
      catch (PSTransformationException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (PSLockFault e)
      {
         ex = PSProxyUtils.convertFault(e, METHOD.SAVE.toString(), 
            refs[0].getObjectType().getPrimaryType().toString(), 
            refs[0].getName());
      }
      catch (PSNotAuthorizedFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(refs.length, ex);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#getSoapBinding(com.percussion.client.proxies.IPSCmsModelProxy.METHOD)
    */
   @SuppressWarnings("unused")
   @Override
   protected ContentDesignSOAPStub getSoapBinding(METHOD method)
      throws MalformedURLException, ServiceException
   {
      return PSProxyUtils.getContentDesignStub();
   }
}

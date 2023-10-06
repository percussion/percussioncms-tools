/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.client.proxies.impl;

import com.percussion.client.*;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.system.data.PSConfigurationTypes;
import com.percussion.webservices.faults.*;
import com.percussion.webservices.system.PSMimeContentAdapter;
import com.percussion.webservices.systemdesign.LoadConfigurationRequest;
import com.percussion.webservices.systemdesign.LoadConfigurationResponse;
import com.percussion.webservices.systemdesign.SaveConfigurationRequest;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#CONFIGURATION_FILE}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * 
 * @since 03-Sep-2005 4:39:27 PM
 */
public class PSConfigurationFileModelProxy extends PSCmsModelProxy
{
   /**
    * Default ctor for testing.
    * 
    */
   public PSConfigurationFileModelProxy()
   {
      super(PSObjectTypes.CONFIGURATION_FILE);
   }

   /**
    * Default ctor for testing.
    * 
    * @param type primary object type, must not be <code>null</code>
    * 
    */
   public PSConfigurationFileModelProxy(PSObjectTypes type)
   {
      super(type);
   }

   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException, PSModelException
   {
      LoadConfigurationRequest loadReq = new LoadConfigurationRequest();
      SystemDesignSOAPStub binding;
      try
      {
         binding = (SystemDesignSOAPStub) getSoapBinding(METHOD.LOAD);
      }
      catch (MalformedURLException | ServiceException e)
      {
         throw new RuntimeException(e);
      }

      loadReq.setLock(lock);
      loadReq.setOverrideLock(overrideLock);
      Object[] resultObj = new Object[reference.length];
      boolean errorOccurred = false;
      for (int i = 0; i < reference.length; i++)
      {
         IPSReference ref = reference[i];
         loadReq.setName(ref.getLabelKey());
         Exception ex = null;
         try
         {
            boolean redo;
            do
            {
               redo = false;
               try
               {
                  LoadConfigurationResponse resp = binding.loadConfiguration(
                     loadReq);
                  PSMimeContentAdapter content = resp.getPSMimeContentAdapter();
                  com.percussion.services.system.data.PSMimeContentAdapter result = 
                     (com.percussion.services.system.data.PSMimeContentAdapter) PSProxyUtils.convert(
                        com.percussion.services.system.data.PSMimeContentAdapter.class,
                        content);
                  resultObj[i] = result;
                  if (lock)
                  {
                     PSProxyUtils.setLockInfo(new IPSReference[]
                     {
                        ref
                     }, false);
                  }
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
         } catch (PSLockFault e)
         {
            ex = PSProxyUtils.convertFault(e, METHOD.LOAD.toString(), 
               ref.getObjectType().getPrimaryType().toString(), ref.getName());
         } catch (RemoteException e)
         {
            ex = e;
         }

         if (ex != null)
         {
            logError(ex);
            errorOccurred = true;
            resultObj[i] = ex;
         }
      }
      
      if (errorOccurred)
      {
         processAndThrowException(resultObj.length, 
            new PSMultiOperationException(resultObj, reference));
      }
      
      return resultObj;
   }

   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      PSConfigurationTypes[] array = PSConfigurationTypes.values();
      List<IPSReference> refs = new ArrayList<>(array.length);
      for (PSConfigurationTypes type : array) {
         PSReference ref = new PSReference();
         ref.setName(type.getFileName());
         //if you change this, the load method must be modified
         ref.setLabelKey(type.name());
         ref.setObjectType(serverToClientType(type));
         ref.setDescription(type.getDescription());
         ref.setId(new PSDesignGuid(PSTypeEnum.CONFIGURATION, type.getId()));
         refs.add(ref);
      }
      return refs;
   }

   /**
    * Translate the server side configuration type to local type.
    * 
    * @param type server type to translate, assumed not <code>null</code>.
    * @return Client side object type for the server side equivalent, never
    * <code>null</code>. Returns
    * PSObjectTypes.ConfigurationFileSubTypes.OTHER if an equivalent is not
    * found.
    */
   private PSObjectType serverToClientType(PSConfigurationTypes type)
   {
      IPSPrimaryObjectType pType = PSObjectTypes.CONFIGURATION_FILE;
      Enum<PSObjectTypes.ConfigurationFileSubTypes> sType;
      switch (type)
      {
         case LOG_CONFIG:
            sType = PSObjectTypes.ConfigurationFileSubTypes.LOGGER_PROPERTIES;
            break;
         case NAV_CONFIG:
            sType = PSObjectTypes.ConfigurationFileSubTypes.NAVIGATION_PROPERTIES;
            break;
         case SERVER_PAGE_TAGS:
            sType = PSObjectTypes.ConfigurationFileSubTypes.SERVER_PAGE_TAG_PROPERTIES;
            break;
         case TIDY_CONFIG:
            sType = PSObjectTypes.ConfigurationFileSubTypes.TIDY_PROPERTIES;
            break;
         case WF_CONFIG:
            sType = PSObjectTypes.ConfigurationFileSubTypes.WORKFLOW_PROPERTIES;
            break;
         case THUMBNAIL_CONFIG:
            sType = PSObjectTypes.ConfigurationFileSubTypes.ADD_THUMBNAIL_URL_PROPERTIES;
            break;
         case SYSTEM_VELOCITY_MACROS:
            sType = PSObjectTypes.ConfigurationFileSubTypes.SYSTEM_VELOCITY_MACROS;
            break;
         case USER_VELOCITY_MACROS:
            sType = PSObjectTypes.ConfigurationFileSubTypes.USER_VELOCITY_MACROS;
            break;
         case AUTH_TYPES:
         default:
            sType = PSObjectTypes.ConfigurationFileSubTypes.OTHER;
      }
      return new PSObjectType(pType, sType);
   }

   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      SaveConfigurationRequest saveReq = new SaveConfigurationRequest();
      SystemDesignSOAPStub binding;
      try
      {
         binding = (SystemDesignSOAPStub) getSoapBinding(METHOD.LOAD);
      }
      catch (MalformedURLException | ServiceException e)
      {
         throw new RuntimeException(e);
      }
      saveReq.setRelease(releaseLock);
      Object[] resultObj = new Object[data.length];
      boolean errorOccurred = false;
      for (int i = 0; i < data.length; i++)
      {
         Object obj = data[i];
         Exception ex = null;
         try
         {
            boolean redo;
            do
            {
               redo = false;
               try
               {
                  PSMimeContentAdapter objClient = 
                     (PSMimeContentAdapter) PSProxyUtils.convert(
                        PSMimeContentAdapter.class, obj);
                  saveReq.setPSMimeContentAdapter(objClient);
                  binding.saveConfiguration(saveReq);
                  resultObj[i] = data[i];
                  if (releaseLock)
                  {
                     PSProxyUtils.setLockInfo(new IPSReference[]
                     {
                        refs[i]
                     }, false);
                  }
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
         } catch (PSLockFault e)
         {
            ex = PSProxyUtils.convertFault(e, METHOD.SAVE.toString(), 
               refs[i].getObjectType().getPrimaryType().toString(), 
               refs[i].getName());
         } catch (RemoteException e)
         {
            ex = e;
         }

         if (ex != null)
         {
            logError(ex);
            errorOccurred = true;
            resultObj[i] = ex;
         }
      }
      
      if (!errorOccurred || resultObj.length > 1)
      {
         Collection<IPSReference> successful = new ArrayList<>();
         for (int i=0; i < resultObj.length; i++)
         {
            if (!(resultObj[i] instanceof Throwable))
               successful.add(refs[i]);
         }
         PSProxyUtils.setLockInfo(successful.toArray(
                 new IPSReference[0]), true);
      }
      
      if (errorOccurred)
      {
         processAndThrowException(resultObj.length, 
            new PSMultiOperationException(resultObj, refs));
      }
   }

   /**
    * Override to provide correct SOAP binding
    * 
    * @param method
    * @return never <code>null</code>
    * @throws MalformedURLException
    * @throws ServiceException
    */
   @Override
   @SuppressWarnings("unused")
   protected Stub getSoapBinding(IPSCmsModelProxy.METHOD method)
      throws MalformedURLException, ServiceException
   {
      return PSProxyUtils.getSystemDesignStub();
   }

   @SuppressWarnings("unused")
   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      throw new UnsupportedOperationException(
         "rename() is not supported by this proxy");
   }

   @SuppressWarnings("unused")
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException(
         "renameLocal() is not supported by this proxy");
   }

   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results)
   {
      throw new UnsupportedOperationException(
         "create() is not supported by this proxy");
   }

   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List<Object> results)
   {
      throw new UnsupportedOperationException(
         "create() is not supported by this proxy");
   }

   @SuppressWarnings("unused")
   @Override
   public void delete(IPSReference[] reference)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException(
         "delete() is not supported by this proxy");
   }
}

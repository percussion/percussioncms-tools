/******************************************************************************
 *
 * [ PSUiSearchModelProxy.java ]
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
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.PSObjectFactory;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.webservices.ui.LoadSearchesRequest;
import com.percussion.webservices.ui.UiSOAPStub;
import com.percussion.webservices.ui.data.PSSearchDef;
import com.percussion.webservices.ui.data.PSSearchDefType;
import com.percussion.webservices.uidesign.CreateSearchesRequest;
import com.percussion.webservices.uidesign.CreateSearchesRequestType;
import com.percussion.webservices.uidesign.FindSearchesRequest;
import com.percussion.webservices.uidesign.UiDesignSOAPStub;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#UI_SEARCH}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSUiSearchModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class Invokes base class
    * {@link PSCmsModelProxy#PSCmsModelProxy(IPSPrimaryObjectType) version} with
    * the object type {@link PSObjectTypes#UI_SEARCH} and for primary type.
    */
   public PSUiSearchModelProxy()
   {
      super(PSObjectTypes.UI_SEARCH);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               UiSOAPStub bindingObj = 
                  (UiSOAPStub) getSoapBinding(METHOD.CATALOG);
               LoadSearchesRequest req = new LoadSearchesRequest();
               PSSearchDef[] allSearches = bindingObj.loadSearches(req);
      
               List<IPSReference> result = new ArrayList<IPSReference>(
                  allSearches.length);
               for (PSSearchDef search : allSearches)
               {
                  result.add(searchToReference(search));
               }
               addPermissions(result);
               return result;
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
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(ex);
      
      // will never get here
      return new ArrayList<IPSReference>();
   }

   /**
    * Add permissions field of the {@link IPSReference} object. To do this, it
    * catalogs all action summaries and then sets permissions for each reference
    * in the collection from corresponding catalog summary object. Please note
    * that this one loads all summaries irrespective of how many entries are in
    * the supplied collection.
    * 
    * @param targetRefs reference on which the permissions are to be set,
    * assumed not <code>null</code>. Each entry in the collection is assumed
    * to be castable to {@link PSReference}.
    * @throws PSModelException for any error finding searches.
    */
   private void addPermissions(List<IPSReference> targetRefs) 
      throws PSModelException
   {
      FindSearchesRequest request = new FindSearchesRequest();
      PSObjectSummary[] summaries = new PSObjectSummary[0];
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               UiDesignSOAPStub binding = PSProxyUtils.getUiDesignStub();
               summaries = binding.findSearches(request);
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
      
      if (ex != null)
         processAndThrowException(ex);
      
      PSProxyUtils.copyPermissions(targetRefs, summaries);
   }

   /**
    * Helper method to convert a webservice search def object to a reference.
    * 
    * @param search assumed not <code>null</code>
    * @return reference, never <code>null</code>
    */
   private IPSReference searchToReference(PSSearchDef search)
   {
      String name = search.getName();
      String label = search.getLabel();
      String desc = search.getDescription();
      long id = search.getId();
      Enum secType = PSObjectTypes.SearchSubTypes.STANDARD;
      if (search.getType().equals(PSSearchDefType.customSearch))
         secType = PSObjectTypes.SearchSubTypes.CUSTOM;
      PSReference ref = (PSReference) PSCoreUtils.createReference(name, label,
         desc, PSObjectTypeFactory.getType(m_objectPrimaryType, secType),
         new PSDesignGuid(PSTypeEnum.SEARCH_DEF, id));
      ref.setPersisted();
      return ref;
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
      List results) throws PSMultiOperationException, PSModelException
   {
      if (objType == null)
      {
         throw new IllegalArgumentException("objType must not be null");
      }
      if (names == null || names.size() == 0)
      {
         throw new IllegalArgumentException("names must not be null or empty"); //$NON-NLS-1$
      }
      if (results == null)
      {
         throw new IllegalArgumentException("results must not be null"); //$NON-NLS-1$
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
               CreateSearchesRequestType type = CreateSearchesRequestType.standard;
               if(objType.getSecondaryType()==PSObjectTypes.SearchSubTypes.CUSTOM)
                  type = CreateSearchesRequestType.custom;
      
               UiDesignSOAPStub bindingObj = (UiDesignSOAPStub) getSoapBinding(METHOD.CREATE);
               CreateSearchesRequest req = new CreateSearchesRequest();
               req.setName(names.toArray(new String[0]));
               CreateSearchesRequestType[] types = new CreateSearchesRequestType[names
                  .size()];
               for (int i = 0; i < types.length; i++)
                  types[i] = type;
               req.setType(types);
               Object createdObjects = bindingObj.createSearches(req);
               Object[] result = (Object[]) PSProxyUtils.convert(PSSearch[].class,
                  createdObjects);
               for (int i = 0; i < result.length; i++)
                  results.add(result[i]);
      
               IPSReference[] refs =  PSObjectFactory.objectToReference(result,
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
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (PSTransformationException e)
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
      catch (PSNotAuthorizedFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(names.size(), ex);
      
      // will never get here
      return new IPSReference[0];
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#getSoapBinding(com.percussion.client.proxies.IPSCmsModelProxy.METHOD)
    */
   @Override
   protected Stub getSoapBinding(METHOD method) throws MalformedURLException,
      ServiceException
   {
      if (method == METHOD.CATALOG)
         return PSProxyUtils.getUiStub();
      return PSProxyUtils.getUiDesignStub();
   }
}

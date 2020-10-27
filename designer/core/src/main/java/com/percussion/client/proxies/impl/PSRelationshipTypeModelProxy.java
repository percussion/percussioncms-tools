/******************************************************************************
 *
 * [ PSRelationshipTypeModelProxy.java ]
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
import com.percussion.client.proxies.PSObjectFactory;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.system.RelationshipCategory;
import com.percussion.webservices.systemdesign.CreateRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;
import com.percussion.webservices.transformation.PSTransformationException;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
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
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSRelationshipTypeModelProxy extends PSCmsModelProxy
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
               SystemDesignSOAPStub binding = 
                  (SystemDesignSOAPStub) getSoapBinding(METHOD.CREATE);
      
               CreateRelationshipTypesRequest request = 
                  new CreateRelationshipTypesRequest();
               request.setName(names.toArray(new String[0]));
               // AA by default
               request.setCategory(new RelationshipCategory[]
               {
                  RelationshipCategory.ActiveAssembly
               });
               com.percussion.webservices.system.PSRelationshipConfig[] relTypes = 
                  binding.createRelationshipTypes(request);
      
               Object[] converted = (Object[]) PSProxyUtils.convert(
                  PSRelationshipConfig[].class, relTypes);
      
               for (int i = 0; i < converted.length; i++)
                  results.add(converted[i]);
      
               IPSReference[] refs = PSObjectFactory.objectToReference(
                  converted,
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
   protected Stub getSoapBinding(@SuppressWarnings("unused")
   METHOD method) throws MalformedURLException, ServiceException
   {
      return PSProxyUtils.getSystemDesignStub();
   }
}

/******************************************************************************
 *
 * [ PSUserFileModelProxy.java ]
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
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSObjectFactory;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.webservices.ui.data.PSHierarchyNode;
import com.percussion.webservices.uidesign.CreateHierarchyNodesRequest;
import com.percussion.webservices.uidesign.CreateHierarchyNodesRequestType;
import com.percussion.webservices.uidesign.UiDesignSOAPStub;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#USER_FILE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSUserFileModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#USER_FILE} and for main type and supplied sub typefor
    * the object.
    */
   public PSUserFileModelProxy()
   {
      super(PSObjectTypes.USER_FILE);
   }

   /**
    * Is this object type supproted by this proxy? Overides the defalut method
    * in the base class.
    * 
    * @see PSCmsModelProxy#isTypeSupported(PSObjectType)
    */
   @Override
   public boolean isTypeSupported(PSObjectType type)
   {
      return type.equals(USERFILE_FOLDER) || type.equals(USERFILE_PLACEHOLDER);

   }

   /**
    * Override base class method.
    * 
    * @see PSCmsModelProxy#create(PSObjectType, Collection, List)
    */
   @Override
   @SuppressWarnings("unchecked")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results) throws PSMultiOperationException, PSModelException
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
               UiDesignSOAPStub binding = 
                  (UiDesignSOAPStub) getSoapBinding(METHOD.CREATE);
      
               CreateHierarchyNodesRequest request = 
                  new CreateHierarchyNodesRequest();
               request.setName((String[]) names.toArray());
               CreateHierarchyNodesRequestType type = null;
               if (objType.getSecondaryType() == 
                  PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER)
               {
                  type = CreateHierarchyNodesRequestType.folder;
               }
               else if (objType.getSecondaryType() == 
                  PSObjectTypes.UserFileSubTypes.PLACEHOLDER)
               {
                  type = CreateHierarchyNodesRequestType.placeholder;
               }
               request.setType(new CreateHierarchyNodesRequestType[]
               {
                  type
               });
               request.setParentId(new long[]
               {
                  0
               });
               PSHierarchyNode[] nodes = binding.createHierarchyNodes(request);
               Object[] converted = (Object[]) PSProxyUtils.convert(
                  com.percussion.services.ui.data.PSHierarchyNode[].class, 
                  nodes);
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
      catch (SecurityException  | ServiceException | RemoteException |
             MalformedURLException | IllegalArgumentException e)
      {
         ex = e;
      }

       if (ex != null)
         processAndThrowException(names.size(), ex);
      
      // will never get here
      return new IPSReference[0];
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
      return PSProxyUtils.getUiDesignStub();
   }

   /**
    * Object type - workbench folder. Singleton instance.
    */
   public static final PSObjectType USERFILE_FOLDER = new PSObjectType(
      PSObjectTypes.USER_FILE, PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER);

   /**
    * Object type - placeholder. Singleton instance.
    */
   public static final PSObjectType USERFILE_PLACEHOLDER = new PSObjectType(
      PSObjectTypes.USER_FILE, PSObjectTypes.UserFileSubTypes.PLACEHOLDER);
}

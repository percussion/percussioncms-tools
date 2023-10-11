/******************************************************************************
 *
 * [ PSUiMenuActionModelProxy.java ]
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
import com.percussion.cms.objectstore.PSAction;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.webservices.ui.LoadActionsRequest;
import com.percussion.webservices.ui.UiSOAPStub;
import com.percussion.webservices.ui.data.ActionType;
import com.percussion.webservices.uidesign.CreateActionsRequest;
import com.percussion.webservices.uidesign.CreateActionsRequestType;
import com.percussion.webservices.uidesign.FindActionsRequest;
import com.percussion.webservices.uidesign.UiDesignSOAPStub;
import org.apache.axis.client.Stub;
import org.apache.commons.lang.StringUtils;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#UI_ACTION_MENU}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSUiMenuActionModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class
    * {@link PSCmsModelProxy#PSCmsModelProxy(IPSPrimaryObjectType) version} with
    * the object type {@link PSObjectTypes#UI_ACTION_MENU} and for primary type.
    */
   public PSUiMenuActionModelProxy()
   {
      super(PSObjectTypes.UI_ACTION_MENU);
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
               LoadActionsRequest req = new LoadActionsRequest();
               com.percussion.webservices.ui.data.PSAction[] allActions = 
                  bindingObj.loadActions(req);
      
               List<IPSReference> result = new ArrayList<IPSReference>(
                  allActions.length);
               for (com.percussion.webservices.ui.data.PSAction action : allActions)
               {
                  result.add(actionToReference(action));
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
      catch (MalformedURLException | ServiceException | RemoteException e)
      {
         ex = e;
      }


      
      if (ex != null)
         processAndThrowException(ex);
      
      // will never get here
      return new ArrayList<>();
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
    * @throws PSModelException for any error finding actions.
    */
   private void addPermissions(List<IPSReference> targetRefs) 
      throws PSModelException
   {
      FindActionsRequest request = new FindActionsRequest();
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
               summaries = binding.findActions(request);
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
    * Helper method to convert a webservice action object to a reference.
    * 
    * @param action assumed not <code>null</code>
    * @return reference, never <code>null</code>
    */
   private IPSReference actionToReference(
      com.percussion.webservices.ui.data.PSAction action)
   {
      String name = action.getName();
      String label = action.getLabel();
      String desc = action.getDescription();
      long id = action.getId();
      Enum secType = null;
      ActionType type = action.getType();
      if (type.equals(ActionType.item))
      {
         // This is a menu entry, it can be a client action(system) or server
         // (user)
         if (action.isClientAction())
         {
            secType = PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_SYSTEM;
         }
         else
            secType = PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_USER;
      }
      else
      {
         if (action.isClientAction())
         {
            if (StringUtils.isBlank(action.getCommand().getUrl()))
               secType = PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_SYSTEM;
            else
               secType = PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_SYSTEM;
         }
         else if (StringUtils.isBlank(action.getCommand().getUrl()))
            secType = PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER;
         else
            secType = PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER;
      }
      PSReference ref = (PSReference) PSCoreUtils.createReference(name, label,
         desc, PSObjectTypeFactory.getType(m_objectPrimaryType, secType),
         new PSDesignGuid(PSTypeEnum.ACTION, id));
      ref.setPersisted();
      return ref;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(com.percussion.client.PSObjectType,
    * java.util.Collection, java.util.List)
    */
   @Override
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
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               UiDesignSOAPStub bindingObj = 
                  (UiDesignSOAPStub) getSoapBinding(METHOD.CREATE);
               CreateActionsRequest req = new CreateActionsRequest();
               req.setName(names.toArray(new String[0]));
               CreateActionsRequestType[] reqType = 
                  new CreateActionsRequestType[names.size()];
               CreateActionsRequestType type = CreateActionsRequestType.cascading;
               if (objType.getSecondaryType() == 
                  PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_USER)
               {
                  type = CreateActionsRequestType.item;
               }
               else if (objType.getSecondaryType() == 
                  PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER)
               {
                  type = CreateActionsRequestType.cascading;
               }
               else if (objType.getSecondaryType() == 
                  PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER)
               {
                  type = CreateActionsRequestType.dynamic;
               }
               else
               {
                  // Default is user type entry
                  type = CreateActionsRequestType.item;
               }
               for (int i = 0; i < reqType.length; i++)
               {
                  reqType[i] = type;
               }
      
               req.setType(reqType);
               Object createdObjects = bindingObj.createActions(req);
               Object[] result = (Object[]) PSProxyUtils.convert(
                  PSAction[].class, createdObjects);
               for (int i = 0; i < result.length; i++)
                  results.add(result[i]);
      
               IPSReference[] refs = PSObjectFactory.objectToReference(result,
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
      catch (ServiceException | MalformedURLException | IllegalArgumentException | SecurityException | RemoteException e)
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

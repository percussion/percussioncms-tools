/******************************************************************************
 *
 * [ PSUiViewModelProxy.java ]
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
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.ui.LoadViewsRequest;
import com.percussion.webservices.ui.UiSOAPStub;
import com.percussion.webservices.ui.data.PSViewDef;
import com.percussion.webservices.ui.data.PSViewDefType;
import com.percussion.webservices.uidesign.FindViewsRequest;
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
 * {@link PSObjectTypes#UI_VIEW}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSUiViewModelProxy extends PSCmsModelProxy
{
   /**
    * Default ctor. Invokes base class
    * {@link PSCmsModelProxy#PSCmsModelProxy(IPSPrimaryObjectType) version} with
    * {@link PSObjectTypes#UI_VIEW} for primary type.
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy
    */
   public PSUiViewModelProxy()
   {
      super(PSObjectTypes.UI_VIEW);
   }

   /**
    * Sets the custom property on all supplied searches if the object type is
    * custom.
    */
   @Override
   protected void postCreate(PSObjectType objType, Object[] results)
   {
      if (objType.getSecondaryType() == PSObjectTypes.SearchSubTypes.CUSTOM)
      {
         for (Object o : results)
         {
            ((PSSearch) o).setCustom(true);
         }
      }
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
               UiSOAPStub bindingObj = (UiSOAPStub) getSoapBinding(METHOD.CATALOG);
               LoadViewsRequest req = new LoadViewsRequest();
               PSViewDef[] allViews = bindingObj.loadViews(req);
      
               List<IPSReference> result = new ArrayList<IPSReference>(
                  allViews.length);
               for (PSViewDef view : allViews)
               {
                  result.add(viewToReference(view));
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
    * @param targetRef reference on which the permissions are to be set, assumed
    * not <code>null</code>. Each entry in the collection is assumed to be
    * castable to {@link PSReference}.
    * @throws PSModelException for any error finding views.
    */
   private void addPermissions(List<IPSReference> targetRef) 
      throws PSModelException
   {
      FindViewsRequest request = new FindViewsRequest();
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
               summaries = binding.findViews(request);
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
      
      PSProxyUtils.copyPermissions(targetRef, summaries);
   }

   /**
    * Helper method to convert a webservice view def object to a reference.
    * 
    * @param view assumed not <code>null</code>
    * @return reference, never <code>null</code>
    */
   private IPSReference viewToReference(PSViewDef view)
   {
      String name = view.getName();
      String label = view.getLabel();
      String desc = view.getDescription();
      long id = view.getId();
      Enum secType = PSObjectTypes.SearchSubTypes.STANDARD;
      if (view.getType().equals(PSViewDefType.customView))
         secType = PSObjectTypes.SearchSubTypes.CUSTOM;
      PSReference ref = (PSReference) PSCoreUtils.createReference(name, label,
         desc, PSObjectTypeFactory.getType(m_objectPrimaryType, secType),
         new PSDesignGuid(PSTypeEnum.VIEW_DEF, id));
      ref.setPersisted();
      return ref;
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

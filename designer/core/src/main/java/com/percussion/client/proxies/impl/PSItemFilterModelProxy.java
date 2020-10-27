/******************************************************************************
 *
 * [ PSItemFilterModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.proxies.PSItemFilterConverter;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#ITEM_FILTER}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSItemFilterModelProxy extends PSCmsModelProxy
{
   /**
    * Default ctor. Calls
    * {@link PSCmsModelProxy#PSCmsModelProxy(IPSPrimaryObjectType)} with
    * {@link PSObjectTypes#ITEM_FILTER} as the argument.
    */
   public PSItemFilterModelProxy()
   {
      super(PSObjectTypes.ITEM_FILTER);
      PSTransformerFactory.getInstance().register(
         PSItemFilterConverter.class, PSItemFilter.class,
         com.percussion.webservices.system.PSItemFilter.class);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#getSoapBinding(com.percussion.client.proxies.IPSCmsModelProxy.METHOD)
    */
   @Override
   protected Stub getSoapBinding(
         @SuppressWarnings("unused") METHOD method) throws MalformedURLException,
      ServiceException
   {
      return PSProxyUtils.getSystemDesignStub();
   }
}

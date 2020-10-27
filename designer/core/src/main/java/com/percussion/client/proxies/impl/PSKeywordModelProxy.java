/******************************************************************************
 *
 * [ PSKeywordModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.PSObjectTypes;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSProxyUtils;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#KEYWORD}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSKeywordModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#KEYWORD} and for main type and <code>null</code>
    * sub type since this object type does not have any sub types.
    */
   public PSKeywordModelProxy()
   {
      super(PSObjectTypes.KEYWORD);
   }

   /**
    * Overrides base class method to return the SOAP stub appropriate for this
    * type object.
    * 
    * @see PSCmsModelProxy#getSoapBinding(IPSCmsModelProxy.METHOD)
    * 
    */
   @Override
   protected Stub getSoapBinding(
         @SuppressWarnings("unused") METHOD method) throws MalformedURLException,
      ServiceException
   {
      return PSProxyUtils.getContentDesignStub();
   }
}

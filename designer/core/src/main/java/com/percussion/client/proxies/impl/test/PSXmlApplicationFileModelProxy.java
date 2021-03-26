/******************************************************************************
 *
 * [ PSXmlApplicationFileModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.proxies.PSUninitializedConnectionException;

/**
 * 
 */
public class PSXmlApplicationFileModelProxy extends
   com.percussion.client.proxies.impl.PSXmlApplicationFileModelProxy
{

   /**
    * @throws PSUninitializedConnectionException
    */
   public PSXmlApplicationFileModelProxy()
      throws PSUninitializedConnectionException
   {
      super();
   }

   /**
    * @param primaryType
    * @throws PSUninitializedConnectionException
    */
   public PSXmlApplicationFileModelProxy(IPSPrimaryObjectType primaryType)
      throws PSUninitializedConnectionException
   {
      super(primaryType);
   }

}

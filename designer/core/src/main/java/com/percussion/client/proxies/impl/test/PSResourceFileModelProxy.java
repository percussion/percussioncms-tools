/******************************************************************************
 *
 * [ PSResourceFileModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.client.proxies.impl.PSXmlApplicationFileModelProxy;

/**
 * Test implementation working on local data, not with the server. 
 *
 * @author Andriy Palamarchuk
 */
public class PSResourceFileModelProxy
      extends PSXmlApplicationFileModelProxy
{

   // see base class
   public PSResourceFileModelProxy() throws PSUninitializedConnectionException
   {
      super();
   }
}

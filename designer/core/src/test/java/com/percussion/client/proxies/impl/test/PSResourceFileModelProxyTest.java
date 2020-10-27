/******************************************************************************
 *
 * [ PSResourceFileModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.client.proxies.PSUnsupportedObjectTypeException;
import junit.framework.TestCase;

public class PSResourceFileModelProxyTest extends TestCase
{
   public void testBasics() throws PSUninitializedConnectionException,
      PSUnsupportedObjectTypeException
   {
      final PSResourceFileModelProxy proxy = new PSResourceFileModelProxy();
   }
}

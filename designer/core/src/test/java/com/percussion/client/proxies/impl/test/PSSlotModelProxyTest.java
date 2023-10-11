/******************************************************************************
 *
 * [ PSSlotModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSModelException;
import com.percussion.client.proxies.PSProxyException;
import junit.framework.TestCase;

public class PSSlotModelProxyTest extends TestCase
{
   public void testBasics() throws PSProxyException, PSModelException
   {
      final PSSlotModelProxy proxy = new PSSlotModelProxy();
       assertFalse(proxy.catalog().isEmpty());
   }
}

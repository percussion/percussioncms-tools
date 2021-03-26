/******************************************************************************
 *
 * [ PSResourceFileHierarchyModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSResourceFileHierarchyModelProxyTest extends TestCase
{
   public void testBasics() throws PSUninitializedConnectionException
   {
      final PSResourceFileHierarchyModelProxy proxy =
            new PSResourceFileHierarchyModelProxy();
   }
}

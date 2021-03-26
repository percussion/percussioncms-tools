/******************************************************************************
 *
 * [ PSExtensionModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import junit.framework.TestCase;

import java.io.File;
import java.util.Collection;

/**
 * @author Andriy Palamarchuk
 */
public class PSExtensionModelProxyTest extends TestCase
{
   public void testPersistence() throws PSUninitializedConnectionException,
      PSModelException, PSMultiOperationException
   {
      getExtensionFile().delete();
      assertFalse(getExtensionFile().exists());

      // initial load, save
      final Collection<IPSReference> allRecords; 
      {
         final PSExtensionModelProxy proxy = new PSExtensionModelProxy();
         allRecords = proxy.catalog();
         assertTrue(getExtensionFile().exists());
         
         proxy.delete(new IPSReference[] {allRecords.iterator().next()});
         assertEquals(allRecords.size() - 1, proxy.catalog().size());
      }
      
      // reset repository
      {
         getExtensionFile().delete();
         assertFalse(getExtensionFile().exists());
         final PSExtensionModelProxy proxy = new PSExtensionModelProxy();
         assertEquals(allRecords.size(), proxy.catalog().size());
         assertTrue(getExtensionFile().exists());
      }
   }

   /**
    * Convenience method to access extension file.
    */
   private File getExtensionFile()
   {
      return PSExtensionModelProxy.ms_localExtensionFile;
   }
}

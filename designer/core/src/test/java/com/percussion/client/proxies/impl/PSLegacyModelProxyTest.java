/******************************************************************************
 *
 * [ PSLegacyModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.conn.PSDesignerConnection;
import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PSLegacyModelProxyTest extends MockObjectTestCase
{
   private final Mock mockConnection = new Mock(PSDesignerConnection.class);
   
   /**
    * Nothing happens during deleteAcl.
    */
   public void testDeleteAcl()
   {
      final PSLegacyModelProxy proxy =
         new TestPSLegacyModelProxy2(PSObjectTypes.RESOURCE_FILE);

      proxy.deleteAcl(null);
      
      final IPSReference[] owners = new IPSReference[1];
      proxy.deleteAcl(owners);
      
      final Mock mockRef = new Mock(IPSReference.class);
      owners[0] = (IPSReference) mockRef.proxy();
      proxy.deleteAcl(owners);
      mockRef.verify();
   }
   
   public void testCheckInstanceType()
   {
      final Object[] objects = new Object[] {new File("ss"), new ArrayList()};
      final PSLegacyModelProxy proxy =
         new TestPSLegacyModelProxy2(PSObjectTypes.RESOURCE_FILE);
      proxy.checkInstanceType(objects, Object.class);
      try
      {
         proxy.checkInstanceType(objects, File.class);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
   
   /**
    * Handles connection.
    *
    * @author Andriy Palamarchuk
    */
   private class TestPSLegacyModelProxy2 extends TestPSLegacyModelProxy
   {

      public TestPSLegacyModelProxy2(IPSPrimaryObjectType primaryType)
      {
         super(primaryType);
      }

      @Override
      protected synchronized PSDesignerConnection getConnection()
      {
         return (PSDesignerConnection) mockConnection.proxy();
      }
   }

   /**
    * Dummy PSLegacyModelProxy implementation.
    */
   private static class TestPSLegacyModelProxy extends PSLegacyModelProxy
   {
      public TestPSLegacyModelProxy(IPSPrimaryObjectType primaryType)
      {
         super(primaryType);
      }

      public Collection<IPSReference> catalog()
      {
         throw new AssertionError();
      }

      @SuppressWarnings("unused")
      public IPSReference[] create(PSObjectType objType,
            Collection<String> names, List results)
      {
         throw new AssertionError();
      }

      @SuppressWarnings("unused")
      public IPSReference[] create(Object[] sourceObjects, String[] names, 
            List results)
      {
         throw new AssertionError();
      }

      @SuppressWarnings("unused")
      public Object[] load(IPSReference[] reference, boolean lock,
            boolean overrideLock) throws PSMultiOperationException
      {
         throw new AssertionError();
      }

      @SuppressWarnings("unused")
      public void delete(IPSReference[] reference)
         throws PSMultiOperationException
      {
         throw new AssertionError();
      }

      @SuppressWarnings("unused")
      public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
         throws PSMultiOperationException
      {
         throw new AssertionError();
      }

      @SuppressWarnings("unused")
      public void rename(IPSReference ref, String name, Object data)
         throws PSModelException
      {
         throw new AssertionError();
      }

      @Override
      @SuppressWarnings("unused")
      public void renameLocal(IPSReference ref, String name, Object data)
      {
         throw new AssertionError();
      }

      @SuppressWarnings("unused")
      public void releaseLock(IPSReference[] references)
         throws PSMultiOperationException
      {
         throw new AssertionError();
      }

      @SuppressWarnings("unused")
      public boolean isLocked(IPSReference ref)
      {
         throw new AssertionError();
      }
   }
}

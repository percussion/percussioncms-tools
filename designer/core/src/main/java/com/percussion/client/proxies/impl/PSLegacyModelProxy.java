/******************************************************************************
 *
 * [ PSLegacyModelProxy.java ]
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
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.services.security.IPSAcl;

/**
 * This provides CRUD and catalogging services for all legacy objects such as
 * Rhythmyx applications, extensions etc.
 * <p>
 * Creation of an object of this proxy assumes the required connection has been
 * initialized in {@link com.percussion.client.PSCoreFactory}.
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public abstract class PSLegacyModelProxy implements IPSCmsModelProxy
{
   /**
    * Ctor usable in test proxies.
    */
   protected PSLegacyModelProxy()
   {
      m_primaryType = null;
   }

   /**
    * Get the object store. Just a helper method that delegates call to
    * {@link PSProxyUtils#getObjectStore()}
    * 
    * @see PSProxyUtils#getObjectStore()
    */
   protected PSObjectStore getObjectStore()
   {
      return PSProxyUtils.getObjectStore();
   }

   /**
    * Ctor taking the object type. Assumes the designer conenction has already
    * been initialized.
    * 
    * @param primaryType object's primary type for the proxy, must not be
    * <code>null</code>.
    */
   public PSLegacyModelProxy(IPSPrimaryObjectType primaryType)
   {
      m_primaryType = primaryType;
   }

   /**
    * The default implementation returns a meta data object that conforms to the
    * following table: <table>
    * <th>
    * <td>Method</td>
    * <td>Return</td>
    * </th>
    * <tr>
    * <td>isCacheable</td>
    * <td>true</td>
    * </tr>
    * </table>
    */
   public IModelInfo getMetaData()
   {
      return new IModelInfo()
      {
         /**
          * @return Always <code>true</code>.
          */
         public boolean isCacheable()
         {
            return true;
         }
      };
   }

   /**
    * Convenience method that calls {@link PSCoreFactory#getInstance()}#getDesignerConnection().
    * 
    * @return Never <code>null</code>.
    */
   synchronized protected PSDesignerConnection getConnection()
   {
      return PSCoreFactory.getInstance().getDesignerConnection();
   }

   /**
    * Does nothing. Subclass must override to do type specific cleanup.
    * 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#flush(com.percussion.client.IPSReference)
    */
   @SuppressWarnings("unused")
   public void flush(IPSReference ref)
   {
      // does nothing
   }

   /**
    * Legacy objects do not support ACLs.
    * @return default ACL set.
    */
   @SuppressWarnings("unused")
   public Object[] loadAcl(IPSReference[] refs, boolean lock)
         throws PSModelException, PSMultiOperationException
   {
      final Object[] result = new Object[refs.length];
      for (int i = 0; i < refs.length; i++)
      {
         result[i] = PSSecurityUtils.createNewAcl(); 
      }
      return result;
   }

   /**
    * Legacy objects do not support ACLs.
    * Does nothing.
    */
   @SuppressWarnings("unused")
   public void saveAcl(IPSReference[] ref, IPSAcl[] acl, boolean releaseLock)
         throws PSModelException, PSMultiOperationException
   {
   }

   /**
    * Legacy objects do not support ACLs.
    * Does nothing.
    */
   @SuppressWarnings("unused")
   public void deleteAcl(IPSReference[] owners)
   {
   }

   /**
    * Legacy objects do not support ACLs.
    * Does nothing.
    */
   @SuppressWarnings("unused")
   public void releaseAclLock(Long[] aclIds) throws PSMultiOperationException
   {
   }

   /**
    * The default behavior throws an exception.
    * 
    * @throws UnsupportedOperationException Always.
    */
   @SuppressWarnings("unused")
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException("local rename not supported.");
   }

   /**
    * Checks objects' java instance type and throws an exception if one or more
    * of the array of objects is/are not instance(s) of expected java type.
    * 
    * @param sourceObjects Array of the objects to validate the type, assumed
    * not <code>null</code> or empty.
    * 
    * @param expectedClass expected classs of objects to validate.
    * 
    * @throws IllegalArgumentException if the java type of any object in the
    * array is not expected.
    */
   @SuppressWarnings("unchecked")
   protected void checkInstanceType(final Object[] sourceObjects,
      final Class expectedClass) throws IllegalArgumentException
   {
      for (final Object o : sourceObjects)
      {
         if (!expectedClass.isAssignableFrom(o.getClass()))
         {
            throw new IllegalArgumentException(
               "sourceObjects must be instances of PSApplication");
         }
      }
   }

   /**
    * Object primary type. Set by constructor.
    */
   public IPSPrimaryObjectType getPrimaryType()
   {
      return m_primaryType;
   }

   /**
    * Get the CMS model this proxy is associated with.
    * 
    * @return the associated CMS model, never <code>null</code>.
    */
   protected IPSCmsModel getModel()
   {
      try
      {
         return PSCoreFactory.getInstance().getModel((Enum) m_primaryType);
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e.getLocalizedMessage());
      }
   }

   /**
    * The primary object type for which this proxy is instantiated.
    */
   private IPSPrimaryObjectType m_primaryType;
}

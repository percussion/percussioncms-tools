/******************************************************************************
*
* [ PSReadOnlyModelProxy.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;

public abstract class PSReadOnlyModelProxy extends PSCmsModelProxy
{
 
   public PSReadOnlyModelProxy(PSObjectTypes objectType)
   {
      super();
      m_objectPrimaryType = objectType;
   }

   /**
    * Overriden to throw an <code>UnsupportedOperationException</code>, but
    * can be overriden by subclass if needed.
    * 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#load(IPSReference[],
    * boolean, boolean)
    */
   @SuppressWarnings("unused")
   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException, PSModelException
   {
      throw new UnsupportedOperationException();
   }
   
   /**
    * Overriden to always throw an <code>UnsupportedOperationException</code>
    */
   @SuppressWarnings("unused")
   @Override
   public final void delete(IPSReference[] reference)
      throws PSMultiOperationException, PSModelException
   {
      throw new UnsupportedOperationException();
   }
   
   /**
    * Overriden to always throw an <code>UnsupportedOperationException</code>
    */
   @SuppressWarnings("unused")
   @Override
   public final void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Overriden to always throw an <code>UnsupportedOperationException</code>
    */
   @SuppressWarnings("unused")
   @Override
   public final void releaseLock(IPSReference[] references)
      throws PSMultiOperationException, PSModelException
   {
      throw new UnsupportedOperationException();
   }
   
   /**
    * Overriden to always throw an <code>UnsupportedOperationException</code>
    */
   @SuppressWarnings("unused")
   @Override
   public final void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Overriden to always throw an <code>UnsupportedOperationException</code>
    */
   @SuppressWarnings("unused")
   @Override
   public final void renameLocal(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException();
   }

}

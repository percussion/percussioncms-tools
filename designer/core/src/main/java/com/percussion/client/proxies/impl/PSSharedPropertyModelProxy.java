/******************************************************************************
 *
 * [ PSSharedPropertyModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;

import java.util.Collection;
import java.util.List;

/**
 * TODO At this point this is a place holder only. I see a need for something
 * like this to be used by other proxies if not required by the user.
 * 
 * @version 6.0
 * @since 03-Sep-2005 4:39:27 PM
 */
@SuppressWarnings("unused")
public class PSSharedPropertyModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor
    */
   public PSSharedPropertyModelProxy()
   {
      super(null);
   }

   @Override
   public IPSReference[] create(PSObjectType objType,
         Collection<String> names, List<Object> results)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Object[] load(IPSReference[] reference, boolean lock,
         boolean overrideLock) throws PSMultiOperationException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void delete(IPSReference[] reference) throws PSMultiOperationException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      // TODO Auto-generated method stub
      
   }   
}

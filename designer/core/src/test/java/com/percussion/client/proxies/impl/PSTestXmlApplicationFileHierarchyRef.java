/******************************************************************************
 *
 * [ PSTestXmlApplicationFileHierarchyRef.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.PSModelException;
import com.percussion.client.models.IPSCmsModel;

/**
 * Disables model validation, so the tests can run with testing proxies.
 * Otherwise it fails because there is no testing proxy for application files.
 * Used for testing purposes only
 *
 * @author Andriy Palamarchuk 
 */
class PSTestXmlApplicationFileHierarchyRef
      extends PSXmlApplicationFileHierarchyRef
{
   public PSTestXmlApplicationFileHierarchyRef(
         IPSHierarchyNodeRef parent, String name, boolean isContainer)
         throws PSModelException
   {
      super(parent, name, isContainer);
   }

   /**
    * Always returns <code>null</code>.
    * @see com.percussion.client.impl.PSReference#getModel(java.lang.Enum)
    */
   @Override
   protected IPSCmsModel getModel(@SuppressWarnings("unused") Enum type)
   {
      return null;
   }
}
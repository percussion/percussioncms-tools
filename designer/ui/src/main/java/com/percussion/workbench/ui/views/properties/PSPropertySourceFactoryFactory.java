/******************************************************************************
 *
 * [ PSPropertySourceFactoryFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.properties;

import com.percussion.client.PSObjectType;

/**
 * Singleton class that can create property factories from an object type.
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:43:58 PM
 */
public class PSPropertySourceFactoryFactory
{

   public IPSPropertySourceFactory m_IPSPropertySourceFactory;

   public PSPropertySourceFactoryFactory()
   {}

   /**
    * @param objectType
    */
   public IPSPropertySourceFactory createFactory(PSObjectType objectType)
   {
      return null;
   }

   public PSPropertySourceFactoryFactory getInstance()
   {
      return null;
   }

}

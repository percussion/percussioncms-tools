/******************************************************************************
 *
 * [ PSResourceFileModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSObjectTypes;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#RESOURCE_FILE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSResourceFileModelProxy extends PSXmlApplicationFileModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#RESOURCE_FILE} and for primary type.
    */
   public PSResourceFileModelProxy()
   {
      super(PSObjectTypes.RESOURCE_FILE);
   }

   /**
    * Ctor. Invokes base class version
    * @param primaryType  primary object type, must not be <code>null</code>.
    */
   public PSResourceFileModelProxy(IPSPrimaryObjectType primaryType)
   {
      super(primaryType);
   }
}

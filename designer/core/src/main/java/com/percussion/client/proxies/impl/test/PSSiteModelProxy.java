/******************************************************************************
 *
 * [ PSSiteModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test proxy. Over rides the base class
 * {@link com.percussion.client.proxies.impl.PSSiteModelProxy#catalog()} method
 * to return an in-memory generated references.
 */
public class PSSiteModelProxy extends
   com.percussion.client.proxies.impl.PSSiteModelProxy
{
   /**
    * Default ctor. Just invokes base version.
    * 
    * @throws PSUninitializedConnectionException
    */
   public PSSiteModelProxy() throws PSUninitializedConnectionException 
   {
      super();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSSiteModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      List<IPSReference> refs = new ArrayList<IPSReference>(5);
      // ids 10, 20, 30, 40 and 50
      for (int i = 0; i < 5; i++)
      {
         String name = "Site_" + i; //$NON-NLS-1$
         refs.add(PSCoreUtils.createReference(name, name + "_label", name //$NON-NLS-1$
            + "_desc", PSObjectTypeFactory.getType(PSObjectTypes.SITE), //$NON-NLS-1$
            new PSDesignGuid(PSTypeEnum.SITE, i * 10 + 10)));
      }
      return refs;
   }
}

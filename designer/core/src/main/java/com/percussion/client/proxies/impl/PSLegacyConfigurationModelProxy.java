/******************************************************************************
 *
 * [ PSLegacyConfigurationModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.system.data.PSConfigurationTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#LEGACY_CONFIGURATION}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSLegacyConfigurationModelProxy extends
   PSConfigurationFileModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#LEGACY_CONFIGURATION} and for main type and
    * {@link PSObjectTypes.LegacyConfigurationSubTypes#AUTH_TYPES} sub type for
    * the object.
    */
   public PSLegacyConfigurationModelProxy()
   {
      super(PSObjectTypes.LEGACY_CONFIGURATION);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSConfigurationFileModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      List<IPSReference> refs = new ArrayList<IPSReference>(1);
      PSConfigurationTypes type = PSConfigurationTypes.AUTH_TYPES;
      String name = type.name();
      PSReference ref = new PSReference();
      ref.setName(name);
      ref.setLabelKey(name);
      ref.setObjectType(new PSObjectType(PSObjectTypes.LEGACY_CONFIGURATION,
         PSObjectTypes.LegacyConfigurationSubTypes.AUTH_TYPES));
      ref.setDescription(type.getDescription());
      ref.setId(new PSDesignGuid(PSTypeEnum.CONFIGURATION, type.getId()));
      // What about the lockinfo??? TODO
      refs.add(ref);
      return refs;
   }
}

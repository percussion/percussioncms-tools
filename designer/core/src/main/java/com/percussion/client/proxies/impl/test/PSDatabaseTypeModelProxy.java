/*******************************************************************************
 *
 * [ PSDatabaseTypeModelProxy.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.DataBaseObjectSubTypes;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Provides cataloging services for the object type
 * {@link PSObjectTypes#DB_TYPE} in test mode. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSDatabaseTypeModelProxy
 * 
 * @version 6.0
 */
public class PSDatabaseTypeModelProxy extends
   com.percussion.client.proxies.impl.PSDatabaseTypeModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#DB_TYPE} and for primary type
    * 
    * @throws PSUninitializedConnectionException
    */
   public PSDatabaseTypeModelProxy() throws PSUninitializedConnectionException 
   {
      super();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#catalog(com.percussion.client.IPSReference)
    */
   @SuppressWarnings("unused") //exception
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Collection<IPSReference> result = new ArrayList<IPSReference>();
      String[] dsNames = new String[]
      {
         "rxdefault"
      };
      for (String name : dsNames)
      {
         IPSReference ref = PSCoreUtils.createReference(name, name, name,
            PSObjectTypeFactory.getType(PSObjectTypes.DB_TYPE,
               DataBaseObjectSubTypes.DATASOURCE), new PSDesignGuid(
               PSTypeEnum.HIERARCHY_NODE, name.hashCode()));
         result.add(ref);
      }
      return result;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Collection<IPSReference> catalog(IPSHierarchyNodeRef ref)
   {
      if (ref == null)
      {
         throw new IllegalArgumentException("ref must not be null");
      }
      Collection<IPSReference> result = new ArrayList<IPSReference>();
      PSObjectType oType = ref.getObjectType();
      Enum sType = oType.getSecondaryType();
      final Enum childType;
      Collection<String> returned = new ArrayList<String>();
      if (sType == DataBaseObjectSubTypes.DATASOURCE)
      {
         childType = DataBaseObjectSubTypes.CATEGORY;
         returned.add("SYSTEM TABLE");
         returned.add("TABLE");
         returned.add("VIEW");
      }
      else if (sType == DataBaseObjectSubTypes.CATEGORY)
      {
         if (ref.getName().equals("SYSTEM TABLE"))
         {
            childType = DataBaseObjectSubTypes.TABLE;
            returned.add("syscolumns");
            returned.add("syscomments");
            returned.add("sysdepends");
            returned.add("sysfilegroups");
         }
         else if (ref.getName().equals("TABLE"))
         {
            childType = DataBaseObjectSubTypes.TABLE;
            returned.add("CONTENTADHOCUSERS");
            returned.add("CONTENTAPPROVALS");
            returned.add("NOTIFICATIONS");
            returned.add("RXCOMMUNITY");
            returned.add("PSX_TEMPLATE");
         }
         else if (ref.getName().equals("VIEW"))
         {
            childType = DataBaseObjectSubTypes.VIEW;
            returned.add("sysconstraints");
            returned.add("syssegments");
         }
         else
         {
            throw new AssertionError("Unknown category: " + ref.getName());
         }
      }
      else
      {
         throw new AssertionError("Unknown type: " + sType);
      }
      if (returned != null)
      {
         for (String name : returned)
         {
            IPSReference r = PSCoreUtils.createReference(name, name, name,
               PSObjectTypeFactory.getType(PSObjectTypes.DB_TYPE, childType),
               new PSDesignGuid(PSTypeEnum.HIERARCHY_NODE, Math.abs(name
                  .hashCode())));
            result.add(r);
         }
      }
      return result;
   }
}

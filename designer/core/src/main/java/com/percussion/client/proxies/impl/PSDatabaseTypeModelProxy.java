/******************************************************************************
 *
 * [ PSDatabaseTypeModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.DataBaseObjectSubTypes;
import com.percussion.client.catalogers.PSCatalogDatabaseCategories;
import com.percussion.client.catalogers.PSCatalogDatabaseTables;
import com.percussion.client.catalogers.PSCatalogDatasources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides cataloging services for the object type
 * {@link PSObjectTypes#DB_TYPE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 */
public class PSDatabaseTypeModelProxy extends PSLegacyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#DB_TYPE} and for primary type
    */
   public PSDatabaseTypeModelProxy() 
   {
      super(PSObjectTypes.DB_TYPE);
   }

   @SuppressWarnings("unused") //exception
   public Collection<IPSReference> catalog() 
      throws PSModelException
   {
      final Collection<IPSReference> result = new ArrayList<IPSReference>();
      final PSObjectType dsType = PSObjectTypeFactory.getType(
            PSObjectTypes.DB_TYPE, DataBaseObjectSubTypes.DATASOURCE);
      for (String name : PSCatalogDatasources.getCatalog(true))
      {
         result.add(PSCoreUtils.createReference(name,
               PSCatalogDatasources.getDisplayName(name),
               name, dsType, null));
      }
      return result;
   }

   //see interface
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
      Enum childType = null;
      Collection<String> returned = null;
      if (sType == DataBaseObjectSubTypes.DATASOURCE)
      {
         childType = DataBaseObjectSubTypes.CATEGORY;
         returned = PSCatalogDatabaseCategories.getCatalog(ref.getName(), true);
      }
      else if (sType == DataBaseObjectSubTypes.CATEGORY)
      {
         childType = ref.getName().equals("VIEW")
               ? DataBaseObjectSubTypes.VIEW
               : DataBaseObjectSubTypes.TABLE;
         returned = PSCatalogDatabaseTables.getCatalog(ref.getParent()
            .getName(), ref.getName(), true);
      }
      if (returned != null)
      {
         for (String name : returned)
         {
            IPSReference r = PSCoreUtils.createReference(name, name, name,
               PSObjectTypeFactory.getType(PSObjectTypes.DB_TYPE, childType),
               null);
            result.add(r);
         }
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#create(int,
    * java.util.List)
    */
   @SuppressWarnings("unused")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results)
   {
      throw new UnsupportedOperationException(
         "Create not supported by this object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#create(java.lang.Object[],
    * java.util.List)
    */
   @SuppressWarnings("unused")
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List results)
   {
      throw new UnsupportedOperationException(
         "Create not supported by this object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#load(com.percussion.client.IPSReference[],
    * boolean, boolean)
    */
   @SuppressWarnings("unused")
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException
   {
      Object[] result = new Object[reference.length];
      for (int i = 0; i < reference.length; i++)
         result[i] = null;
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#delete(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   public void delete(IPSReference[] reference)
   {
      throw new UnsupportedOperationException(
         "delete not supported by this object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#save(java.lang.Object[],
    * boolean)
    */
   @SuppressWarnings("unused")
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
   {
      throw new UnsupportedOperationException(
         "Save not supported by this object");
   }

   /**
    * Unsupported operation.
    * 
    * @throws UnsupportedOperationException Always.
    */
   @SuppressWarnings("unused")
   public void rename(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException(
         "Rename not supported by this object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#releaseLock(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   public void releaseLock(IPSReference[] references)
   {
      throw new UnsupportedOperationException(
         "releaseLock not supported by this object");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#isLocked(com.percussion.client.IPSReference)
    */
   @SuppressWarnings("unused")
   public boolean isLocked(IPSReference ref)
   {
      throw new UnsupportedOperationException(
         "isLocked not supported by this object");
   }
}

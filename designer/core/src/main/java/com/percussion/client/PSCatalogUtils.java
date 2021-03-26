/******************************************************************************
*
* [ PSCatalogUtils.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client;

import com.percussion.client.models.IPSCmsModel;
import com.percussion.extension.PSExtensionDef;
import com.percussion.services.filter.IPSItemFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class with catalog utility methods.
 */
public class PSCatalogUtils
{
   /**
    * Catalogs all the extensions, that implement the supplied interface.
    * 
    * @return List of PSExtensionDef objects, may be empty but never
    *         <code>null</code>
    * @throws Exception upon error
    */
   public static List<PSExtensionDef> catalogExtensions(String interfaceName)
         throws Exception
   {
      if (interfaceName == null || interfaceName.length() == 0)
      {
         throw new IllegalArgumentException("interfaceName must not be null or empty");
      }
      
      List<PSExtensionDef> exts = new ArrayList<PSExtensionDef>();
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.EXTENSION);
      List<IPSReference> all = PSCoreUtils.catalog(PSObjectTypes.EXTENSION,
            false);
      for (IPSReference ref : all)
      {
         PSExtensionDef def = (PSExtensionDef) model.load(ref, false, false);
         if (def.implementsInterface(interfaceName))
            exts.add(def);
      }
      return exts;
   }
   
   /**
    * Catalogs possible parent filters for a given filter.
    * 
    * @param curFilter Object of IPSItemFilter for which parents needs to be
    *           cataloged.
    * @return List of IPSItemFilter objects, may be empty but never
    *         <code>null</code>
    * @throws Exception upon error
    */
   public static List<IPSItemFilter> catalogItemFilterParents(IPSItemFilter curFilter)
         throws Exception
   {
      if (curFilter == null)
      {
         throw new IllegalArgumentException("curFilter must not be null");
      }
      List<IPSItemFilter> filters = new ArrayList<IPSItemFilter>();
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.ITEM_FILTER);
      List<IPSReference> all = PSCoreUtils.catalog(PSObjectTypes.ITEM_FILTER,
            false);
      for (IPSReference ref : all)
      {
         IPSItemFilter filter = (IPSItemFilter) model.load(ref, false, false);
         if(!(curFilter.equals(filter) || isAncesterFilter(curFilter,filter)))
            filters.add(filter);
      }
      return filters;
   }
   
   /**
    * Checks whether the filter2 is ancester of filter1.
    * 
    * @param filter1 Object of IPSItemFilter, assumed not <code>null</code>.
    * @param filter2 Object of IPSItemFilter, assumed not <code>null</code>.
    * @return <code>true</code> if the filter2 is ancester of filter1 or
    *         <code>false</code>
    */
   private static boolean isAncesterFilter(IPSItemFilter filter1,
         IPSItemFilter filter2)
   {
      if (filter2.getParentFilter() == null)
         return false;
      else if (filter2.getParentFilter().equals(filter1))
      {
         return true;
      }
      else
         return isAncesterFilter(filter1, filter2.getParentFilter());
   }
}

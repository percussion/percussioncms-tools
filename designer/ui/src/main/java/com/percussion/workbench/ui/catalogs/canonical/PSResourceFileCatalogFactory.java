/*******************************************************************************
 *
 * [ PSResourceFileCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

/**
 * Creates a cataloger that can retrieve applications whose type is RESOURCE.
 * It then retrieves all the app files associated with each application.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSResourceFileCatalogFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps One optional property may be supplied: 'type' The
    * allowed values are 'roots', and 'members'. If the property is not
    * supplied, 'roots' is assumed. All values are case-sensitive.
    */
   public PSResourceFileCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      // TODO Auto-generated constructor stub
   }

   // see interface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      // TODO Auto-generated method stub
      return null;
   }

}

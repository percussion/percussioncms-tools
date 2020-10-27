/*******************************************************************************
 *
 * [ PSViewCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs.uielements;

import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

/**
 * Creates a cataloger that retrieves views designed for use by the Content
 * Explorer based on provided filter criteria. The filters differentiate
 * categories and custom versus standard.
 * <p>
 * This class also knows how to match a view to one of the nodes in the 
 * uielements_viewHierarchyDef.xsd definition file. The structure is as follows:
 * <pre>
 * Views
 *    - Standard
 *    - Custom
 * </pre>
 *
 * @version 6.0
 * @author Paul Howard
 */
public class PSViewCatalogFactory extends PSSearchCatalogFactory
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    */
   public PSViewCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
   }

   //overridden to create the proper view Cataloger
   @Override
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new SearchCataloger(parent, PSObjectTypes.UI_VIEW);
   }  
}
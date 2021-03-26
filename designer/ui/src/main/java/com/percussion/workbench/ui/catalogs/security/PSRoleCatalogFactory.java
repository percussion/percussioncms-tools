/******************************************************************************
 *
 * [ PSRoleCatalogFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.security;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSCommunityModel;
import com.percussion.client.models.IPSWorkflowModel;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creates a cataloger that can retrieve roles, filtered by type of use.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSRoleCatalogFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps One optional property may be supplied: 'category' The
    * allowed values are 'community', 'functional' or 'unassigned'. All values
    * are case-sensitive.
    */
   public PSRoleCatalogFactory(InheritedProperties contextProps,
      PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);

   }

   // see interface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         // see interface for details
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            Collection<IPSReference> refs = new ArrayList<IPSReference>();
            String category = PSRoleCatalogFactory.this
               .getContextProperty(CATEGORY_PROPNAME);
            if (category.equalsIgnoreCase(CATEGORY_COMMUNITY))
               refs = getCommunityRoles(force);
            else if (category.equalsIgnoreCase(CATEGORY_WORKFLOW))
               refs = getWorkflowRoles(force);
            else
            {
               refs = getUnassignedRoles(force);
            }

            return createNodes(refs);
         }

         @SuppressWarnings("unused")
         private Collection<IPSReference> getCommunityRoles(boolean force)
            throws PSModelException
         {
            IPSCommunityModel commModel = (IPSCommunityModel) PSCoreFactory
               .getInstance().getModel(PSObjectTypes.COMMUNITY);
            return commModel.getCommunityRoles(null);
         }

         @SuppressWarnings("unused")
         private Collection<IPSReference> getWorkflowRoles(boolean force)
            throws PSModelException
         {
            IPSWorkflowModel wfModel = (IPSWorkflowModel) PSCoreFactory
               .getInstance().getModel(PSObjectTypes.WORKFLOW);
            return wfModel.getWorkflowRoles(null);
         }

         @SuppressWarnings("unused")
         private Collection<IPSReference> getUnassignedRoles(boolean force)
            throws PSModelException
         {
            IPSCmsModel roleModel = getModel(PSObjectTypes.ROLE);
            Collection<IPSReference> allRoles = roleModel.catalog(force);
            allRoles.removeAll(getCommunityRoles(force));
            allRoles.removeAll(getWorkflowRoles(force));
            return allRoles;
         }

         protected static final String CATEGORY_PROPNAME = "category";

         protected static final String CATEGORY_COMMUNITY = "community";

         protected static final String CATEGORY_WORKFLOW = "workflow";

      };
   }
}

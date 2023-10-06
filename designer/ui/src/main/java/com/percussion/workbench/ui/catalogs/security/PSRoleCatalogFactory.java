/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            Collection<IPSReference> refs;
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

         private static final String CATEGORY_PROPNAME = "category";

         private static final String CATEGORY_COMMUNITY = "community";

         private static final String CATEGORY_WORKFLOW = "workflow";

      };
   }
}

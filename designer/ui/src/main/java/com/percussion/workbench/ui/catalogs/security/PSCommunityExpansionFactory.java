/******************************************************************************
 *
 * [ PSCommunityExpansionFactory.java ]
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
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefinitionException;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Creates a cataloger that retrieves the roles that are associated with a 
 * specified community.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSCommunityExpansionFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps is not used.
    */
   public PSCommunityExpansionFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
   }

   //see interface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         //see interface
         public List<PSUiReference> getEntries(boolean force) 
            throws PSModelException
         {
            // fixme Auto-generated method stub
            Collection<IPSReference> refs = new ArrayList<IPSReference>();
            IPSCmsModel communityModel = getCommunityModel();
            PSUiReference communityUiRef = getAncestorNode(PSObjectTypes.COMMUNITY);
            IPSReference communityRef = communityUiRef.getReference();
            
            if (communityRef == null)
            {
               String[] args = { PSObjectTypes.COMMUNITY.name(),
                     getParent().getPath() };
               Exception e = new PSHierarchyDefinitionException(PSMessages
                     .getString("common.error.badmodel", args));
               throw new PSModelException(e);
            }
            
            try
            {
               PSCommunity community = 
                  (PSCommunity) communityModel.load(communityRef, false, false);
               
               refs = getRoles(community);
            }
            catch (Exception e)
            {
               if (e instanceof PSModelException)
                  throw (PSModelException) e;
               else
                  throw new PSModelException(e);
            }
                        
            return createNodes(refs);
         }
         
         /**
          * Finds all role names for the given community.
          * 
          * @param community the community object as a PSCommunity.
          * @return Never <code>null</code>, may be empty.
          * 
          * @throws PSModelException If any problems communicating w/ server.
          */
         private Collection<IPSReference> getRoles(
               PSCommunity community)
            throws PSModelException
         {
            Collection<IPSGuid> roleIds = community.getRoleAssociations();
            IPSCmsModel roleModel = getModel(PSObjectTypes.ROLE);
            Collection<IPSReference> roleRefs = roleModel.catalog(false);
            
            for (Iterator<IPSReference> iter = roleRefs.iterator(); iter
            .hasNext();)
            {
               if (!roleIds.contains(((PSReference) iter.next()).getId()))
                  iter.remove();
            }
            
            return roleRefs;
         }

         /**
          * Simple method to wrap the exception that should never happen.
          * 
          * @return Never <code>null</code>.
          */
         private IPSCmsModel getCommunityModel()
         {
            try
            {
               return PSCoreFactory.getInstance().getModel(
                     PSObjectTypes.COMMUNITY);
            }
            catch (PSModelException e)
            {
               // shouldn't happen
               throw new RuntimeException(e);
            }
         }
         
                  
      };
   }
          
}

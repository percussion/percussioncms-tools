/******************************************************************************
 *
 * [ PSActionMenuExpansionFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.uielements;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSChildActions;
import com.percussion.cms.objectstore.PSMenuChild;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a cataloger that retrieves the menu entries for a particular menu.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSActionMenuExpansionFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps Not used. 
    */
   public PSActionMenuExpansionFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
   }

   //see interface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         //see interface for details
         @SuppressWarnings("unchecked")   //interaction w/ cms objectstore
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            try
            {
               IPSCmsModel menuModel = getModel(PSObjectTypes.UI_ACTION_MENU);

               if (force)
                  menuModel.flush(getParent().getReference());
               PSAction menu = (PSAction) menuModel.load(getParent()
                     .getReference(), false, false);

               PSChildActions children = menu.getChildren();
               Collection<IPSReference> refs = new ArrayList<IPSReference>();
               for (Iterator<PSMenuChild> iter = children.iterator(); iter
                     .hasNext();)
               {
                  IPSReference childRef = menuModel
                        .getReference(new PSDesignGuid(PSTypeEnum.ACTION, Long
                              .parseLong(iter.next().getChildActionId())));

                  if (childRef == null)
                  {
                     // fixme log it
                  }
                  else
                     refs.add(childRef);
               }
               return createNodes(refs);
            }
            catch (Exception e)
            {
               throw new PSModelException(e);
            }
         }
      };
   }
}

/******************************************************************************
 *
 * [ PSXmlApplicationsCatalogFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.legacy;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
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
 * Creates a cataloger that filters by app type and whether the app is located
 * in a folder.
 * 
 * @version 1.0
 * @created 03-Sep-2005 4:43:43 PM
 *///fixme - needs testing when the proxy and view are complete
public class PSXmlApplicationsCatalogFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps One property must be supplied: 'type' The allowed
    * values are 'system' or 'uncategorized'. All values are case-insensitive.
    */
   public PSXmlApplicationsCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      validatePropertyValue(CATEGORY_PROPNAME, new String[] {
            CATEGORY_UNCATEGORIZED, CATEGORY_SYSTEM }, false);
   }   
    
   //see interface
   public IPSCatalog createCatalog(final PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         //see interface for details
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            Collection<IPSReference> refs = new ArrayList<IPSReference>();
            String category = PSXmlApplicationsCatalogFactory.this
                  .getContextProperty(CATEGORY_PROPNAME);
            if (category.equalsIgnoreCase(CATEGORY_UNCATEGORIZED))
               refs = getUncategorizedApps(force);
            else
            {
               assert(category.equalsIgnoreCase(CATEGORY_SYSTEM));
               refs = getSystemApps(force);
            }
            
            return createNodes(refs);
         }

         /**
          * Gets all apps with the SYSTEM app type.
          * 
          * @param force If <code>true</code>, the request will be sent to
          * the server, even if it could have been processed from the cache.
          * 
          * @return Never <code>null</code>.
          * 
          * @throws PSModelException If any problems communicating with the
          * server.
          */
         private Collection<IPSReference> getSystemApps(boolean force)
            throws PSModelException
         {
            IPSCmsModel model = getModel(PSObjectTypes.XML_APPLICATION);
            Collection<IPSReference> results = model.catalog(force,
                  PSObjectTypeFactory.getType(PSObjectTypes.XML_APPLICATION,
                        PSObjectTypes.XmlApplicationSubTypes.SYSTEM));
            return results;
         }

         /**
          * Gets all apps that don't have the SYSTEM app type.
          * 
          * @param force If <code>true</code>, the request will be sent to
          * the server, even if it could have been processed from the cache.
          * 
          * @return Never <code>null</code>.
          * 
          * @throws PSModelException If any problems communicating with the
          * server.
          */
         private Collection<IPSReference> getUncategorizedApps(boolean force)
            throws PSModelException
         {
            Collection<IPSReference> results = getUncategorizedRefs(
                  "xmlApplications", force, PSObjectTypes.XML_APPLICATION,
                  new PSObjectType[] { PSObjectTypeFactory.getType(
                        PSObjectTypes.XML_APPLICATION,
                        PSObjectTypes.XmlApplicationSubTypes.USER) });
            
            //remove content types
            for (Iterator<IPSReference> i = results.iterator(); i.hasNext();)
            {
               if (i.next().getName().startsWith("psx_ce"))
                  i.remove();
            }
            return results;
         }
      };
   }

   /**
    * The name of the property that controls how the set of apps are filtered
    * before being returned. See the other <code>CATEGORY_xxx</code> values.
    */
   private static String CATEGORY_PROPNAME =  "category";
   
   /**
    * One of the values for the {@link #CATEGORY_PROPNAME} property. Causes all
    * apps that are not system or located under a folder to be returned.
    */
   private static String CATEGORY_UNCATEGORIZED =  "uncategorized";
   
   /**
    * One of the values for the {@link #CATEGORY_PROPNAME} property. Causes all
    * apps that have the system type set to be returned.
    */
   private static String CATEGORY_SYSTEM =  "system";
}

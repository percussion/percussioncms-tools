/******************************************************************************
 *
 * [ PSRelationshipTypeCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Gets the relationship types based on supplied properties. Relationships are
 * categorized either as system or user.
 *
 * @author paulhoward
 */
public class PSRelationshipTypeCatalogFactory extends PSCatalogFactoryBase
{

   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details. 
    * <table>
    * <th>
    *    <td>Property Name</td>
    *    <td>Allowed Values</td>
    *    <td>Description</td>
    * </th>
    * <tr>
    *    <td>type</td>
    *    <td>system, user</td>
    *    <td>Specifies which types of relationship configs are desired.
    *    If not supplied, all relationship types are returned.</td>
    * </tr>
    * </table>
    * 
    * @param contextProps One optional property may be supplied.
    */
   public PSRelationshipTypeCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      validatePropertyValue(REL_TYPE_PROPNAME, new String[]
      {
         REL_TYPE_SYSTEM, REL_TYPE_USER
      }, true);
   }

   //see interface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         // see interface for details
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            Collection<IPSReference> refs = new ArrayList<IPSReference>();
            String relTypeCategory = PSRelationshipTypeCatalogFactory.this
               .getContextProperty(REL_TYPE_PROPNAME);

            IPSCmsModel model = getModel(PSObjectTypes.RELATIONSHIP_TYPE);
            if (StringUtils.isBlank(relTypeCategory))
            {
               // the relTypeCategory property was not supplied,
               // return all relationship types
               refs = model.catalog(force);
            }
            else
            {
               if (relTypeCategory.equalsIgnoreCase(REL_TYPE_SYSTEM))
                  refs = getRelationshipTypes(model, force, true);
               else 
                  refs = getRelationshipTypes(model, force, false);
            }
            return createNodes(refs);
         }

         /**
          * Load all relationships and only return refs to those that match the
          * <code>system</code> flag.
          * 
          * @param model Relationship type model. Assumed not <code>null</code>.
          * @param force A flag to override the cache.
          * @param system A flag to indicate which type of relationships to 
          * return.
          * 
          * @return Never <code>null</code>.
          * @throws PSModelException If any problems communicating with the 
          * server or loading the objects to check the type.
          */
         private Collection<IPSReference> getRelationshipTypes(
               IPSCmsModel model, boolean force, boolean system)
            throws PSModelException
         {
            try
            {
               final Collection<IPSReference> refs = model.catalog(force);
               Object[] data = model.load(
                     refs.toArray(new IPSReference[refs.size()]), false, false);
               assert data.length == refs.size();
               final List<IPSReference> selectedRefs =
                     new ArrayList<IPSReference>();
               Iterator<IPSReference> iter = refs.iterator();
               for (Object o : data)
               {
                  assert iter.hasNext();
                  final IPSReference ref = iter.next();
                  final PSRelationshipConfig cfg = (PSRelationshipConfig) o; 
                  if (cfg.isSystem() == system)
                  {
                     selectedRefs.add(ref);
                  }
               }
               return selectedRefs;
            }
            catch (PSMultiOperationException e)
            {
               //shouldn't happen since we are loading read-only
               throw new PSModelException(e);
            }
         }
      };
   }

   /**
    * One of the values for the {@link #REL_TYPE_PROPNAME} property.
    */
   private static String REL_TYPE_USER = "user";

   /**
    * One of the values for the {@link #REL_TYPE_PROPNAME} property.
    */
   private static String REL_TYPE_SYSTEM = "system";

   /**
    * The name of the property that controls the type of relationship to return.
    * The value is one of the <code>REL_TYPE_xxx</code> values.
    */
   private static String REL_TYPE_PROPNAME = "type";
}

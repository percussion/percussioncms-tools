/*******************************************************************************
 *
 * [ PSSlotCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSUserFileModel;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Creates a cataloger that contains slot definitions. Accepts parameters to
 * differentiate between system and user slots.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSSlotCatalogFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details. * <table>
    * <th>
    * <td>Property Name</td>
    * <td>Allowed Values</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>category</td>
    * <td>uncategorized</td>
    * <td>Where in the tree the nodes are intended. 'uncategorized' says to
    * return all nodes that don't have a folder parent. This parameter is
    * optional. If not provided, all known slots are returned.</td>
    * </tr>
    * </table>
    * 
    * @param contextProps One optional property may be supplied.
    */
   public PSSlotCatalogFactory(InheritedProperties contextProps,
      PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      validatePropertyValue(CATEGORY_PROPNAME, new String[]
      {
         CAT_UNCATEGORIZED
      }, true);
   }

   // see interface
   public IPSCatalog createCatalog(final PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         // see interface for details
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            Collection<IPSReference> refs = new ArrayList<IPSReference>();
            String category = PSSlotCatalogFactory.this
               .getContextProperty(CATEGORY_PROPNAME);

            if (StringUtils.isBlank(category))
            {
               // the category property was not supplied, return all slots
               refs = getModel(PSObjectTypes.SLOT).catalog(force);
            }
            else
            {
               refs = getUncategorizedSlots(force);
            }

            return createNodes(refs);
         }

         /**
          * Builds the list of slot refs that should appear in the slots tree
          * outside of any other nodes.
          * 
          * @param force See {@link #createCatalog(PSUiReference)}.
          * 
          * @return Never <code>null</code>, may be empty.
          */
         private Collection<IPSReference> getUncategorizedSlots(boolean force)
            throws PSModelException
         {
            IPSUserFileModel folderModel = getFolderModel();
            Collection<PSHierarchyNode> placeholders = folderModel
               .getDescendentPlaceholders(getTreeName());
            Set<String> ids = new HashSet<String>();
            for (PSHierarchyNode node : placeholders)
            {
               String id = node.getProperty("guid");
               if (!StringUtils.isBlank(id))
                  ids.add(id);
            }

            IPSCmsModel slotModel = getModel(PSObjectTypes.SLOT);
            Collection<IPSReference> slots = slotModel.catalog(force);
            for (Iterator<IPSReference> iter = slots.iterator(); iter.hasNext();)
            {
               if (ids.contains(iter.next().getId().toString()))
                  iter.remove();
            }

            return slots;
         }

         /**
          * Looks up the tree name using the property name "userPathRootName".
          * 
          * @return Never <code>null</code> or empty. If no property is
          * present, "slots" is returned.
          */
         private String getTreeName()
         {
            String treeName = PSSlotCatalogFactory.this
               .getContextProperty(USER_PATH_ROOTNAME_PROPNAME);
            if (StringUtils.isBlank(treeName))
               treeName = "slots";
            return treeName;
         }
      };
   }

   /**
    * The name of the property that controls where in the tree the node should
    * appear.
    */
   private static String CATEGORY_PROPNAME = "category";

   /**
    * One of the values for the {@link #CATEGORY_PROPNAME} property.
    */
   private static String CAT_UNCATEGORIZED = "uncategorized";

   /**
    * The name of the property that controls tree name under which to look for
    * nodes.
    */
   private static String USER_PATH_ROOTNAME_PROPNAME = "userPathRootName";
}

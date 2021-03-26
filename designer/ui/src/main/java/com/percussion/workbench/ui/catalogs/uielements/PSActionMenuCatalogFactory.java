/*******************************************************************************
 *
 * [ PSActionMenuCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs.uielements;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSAction;
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
 * Creates a cataloger that retrieves action menus and items based on provided
 * filter criteria. The filters differentiate menus and items and system vs 
 * user items. For menus, cascading vs dynamic is also differentiated.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSActionMenuCatalogFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps Two required properties must be supplied:
    * {@link #ACTION_TYPE_PROPNAME} and {@link #ENTRY_TYPE_PROPNAME}. The
    * allowed values for the former are the <code>AT_xxx</code> values. The
    * allowed values for the 2nd param are the <code>ET_xxx</code> values. All
    * values are case-sensitive.
    */
   public PSActionMenuCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      validatePropertyValue(ACTION_TYPE_PROPNAME, new String[] {
            AT_MENU_CASCADE, AT_MENU_DYNAMIC, AT_MENU_ENTRY }, false);
      validatePropertyValue(ENTRY_TYPE_PROPNAME, new String[] { ET_SYSTEM,
            ET_USER }, false);
   }

   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         //see interface for details
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            Collection<IPSReference> refs = new ArrayList<IPSReference>();
            String category = PSActionMenuCatalogFactory.this
                  .getContextProperty(ACTION_TYPE_PROPNAME);
            String entryType = PSActionMenuCatalogFactory.this
                  .getContextProperty(ENTRY_TYPE_PROPNAME);

            Enum secondary = null;
            if (category.equalsIgnoreCase(AT_MENU_CASCADE))
            {
               if (entryType.equalsIgnoreCase(ET_SYSTEM))
               {
                  secondary = 
                     PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_SYSTEM;
               }
               else
               {
                  secondary = 
                     PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER;
               }
            }
            else if (category.equalsIgnoreCase(AT_MENU_DYNAMIC))
            {
               if (entryType.equalsIgnoreCase(ET_SYSTEM))
               {
                  secondary = 
                     PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_SYSTEM;
               }
               else
               {
                  secondary = 
                     PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER;
               }
            }
            else
            {
               assert(category.equalsIgnoreCase(AT_MENU_ENTRY));
               if (entryType.equalsIgnoreCase(ET_SYSTEM))
               {
                  secondary = 
                     PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_SYSTEM;
               }
               else if (entryType.equalsIgnoreCase(ET_USER))
               {
                  secondary = PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_USER;
               }
            }
            
            refs = getModel(PSObjectTypes.UI_ACTION_MENU).catalog(
                  force,
                  secondary == null ? null : PSObjectTypeFactory.getType(
                        PSObjectTypes.UI_ACTION_MENU, secondary));
            return createNodes(refs);
         }
      };
   }
   
   /**
    * Simple interface to generalize a filtering mechanism.
    *
    * @author paulhoward
    */
   interface IActionFilter
   {
      /**
       * Looks at the properties of the supplied action to determine whether it
       * is appropriate for this filter.
       * 
       * @param data Assumed not <code>null</code>.
       * 
       * @return <code>true</code> if the supplied action meets the criteria of
       * this filter, <code>false</code> otherwise.
       */
      public boolean accept(PSAction data);
   }
   
   /**
    * One of the values for the {@link #ENTRY_TYPE_PROPNAME} property.
    */
   private static final String ET_SYSTEM = "system";

   /**
    * One of the values for the {@link #ENTRY_TYPE_PROPNAME} property.
    */
   private static final String ET_USER = "user";
   
   /**
    * The name of the property that controls whether system or user menu entries
    * will be returned. Only applicable if {@link #ACTION_TYPE_PROPNAME} =
    * {@link #AT_MENU_ENTRY}.
    */
   private static final String ENTRY_TYPE_PROPNAME = "menuEntryType";
   
   /**
    * One of the values for the {@link #ACTION_TYPE_PROPNAME} property.
    */
   private static final String AT_MENU_CASCADE = "cascading";
   
   /**
    * One of the values for the {@link #ACTION_TYPE_PROPNAME} property.
    */
   private static final String AT_MENU_DYNAMIC = "dynamic";

   /**
    * One of the values for the {@link #ACTION_TYPE_PROPNAME} property.
    */
   private static final String AT_MENU_ENTRY = "entries";
   
   /**
    * The name of the property that controls whether menus or menu entries will
    * be returned.
    */
   private static final String ACTION_TYPE_PROPNAME = "menuObjectType";
}

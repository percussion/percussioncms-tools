/******************************************************************************
 *
 * [ PSSharedFieldsExpansionFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.handlers.PSIconNodeHandler;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefinitionException;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a cataloger that retrieves the field groups within a shared def
 * file.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSSharedFieldsExpansionFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps Not used.
    */
   public PSSharedFieldsExpansionFactory(InheritedProperties contextProps,
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
         @SuppressWarnings("unchecked") // integration w/ legacy code
         public List<PSUiReference> getEntries(
               @SuppressWarnings("unused") boolean force)
            throws PSModelException
         {
            IPSReference sharedFileRef = getAncestorNode(
                  PSObjectTypes.SHARED_FIELDS).getReference();
            if (sharedFileRef == null)
            {
               Object[] args = { PSObjectTypes.SHARED_FIELDS.name(),
                     getParent().getPath() };
               Exception e = new PSHierarchyDefinitionException(PSMessages
                     .getString("common.error.badmodel", args));
               throw new PSModelException(e);
            }
            
            List<PSUiReference> nodes = new ArrayList<PSUiReference>();
            IPSCmsModel model = getModel(PSObjectTypes.SHARED_FIELDS);

            try
            {
               PSContentEditorSharedDef fieldData = 
                  (PSContentEditorSharedDef) model.load(sharedFileRef, false, false);

               for (Iterator<PSSharedFieldGroup> iter = fieldData
                     .getFieldGroups(); iter.hasNext();)
               {
                  PSUiReference node = new PSUiReference(getParent(), iter
                        .next().getName(), null, null, null, false);
                  IPSDeclarativeNodeHandler handler = new PSIconNodeHandler(
                        null, "icons/fieldGroup16.gif", null);
                  node.setHandler(handler);
                  nodes.add(node);
               }
               Collections.sort(nodes, new Comparator()
               {
                  /**
                   * @inheritDoc
                   * Sorts by name.
                   */
                  public int compare(Object o1, Object o2)
                  {
                     return ((PSUiReference) o1).getName().compareTo(
                           ((PSUiReference) o2).getName());
                  }
               });
               return nodes;
            }
            catch (Exception e)
            {
               if (e instanceof PSModelException)
                  throw (PSModelException) e;
               else
                  throw new PSModelException(e);
            }
         }
      };
   }
}

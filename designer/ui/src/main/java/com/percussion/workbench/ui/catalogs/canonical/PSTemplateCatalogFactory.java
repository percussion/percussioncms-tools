/******************************************************************************
 *
 * [ PSTemplateCatalogFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.PSLockException;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Accepts a property to control whether new style (v6+) or old style templates
 * (referred to as variants in the old model) are returned.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSTemplateCatalogFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details. 
    * <table>
    *    <th>
    *       <td>Property Name</td>
    *       <td>Allowed Values</td>
    *       <td>Description</td>
    *    </th>
    *    <tr>
    *       <td>outputType</td>
    *       <td>Global, Non-global</td>
    *       <td>Defaults to Non-global</td>
    *    </tr>
    *    <tr>
    *       <td>templateType</td>
    *       <td>Local, Shared, Variant</td>
    *       <td>Local means it is 'owned' by a content type. Shared means it is 
    *       not owned by any type but used by 0 or more. Defaults to shared.
    *       </td>
    *    </tr>
    *    <tr>
    *       <td>category</td>
    *       <td>uncategorized, contentTypeOwner</td>
    *       <td>Where in the tree the nodes are intended. 'uncategorized' says
    *       to return all nodes that don't have a folder parent. 
    *       contentTypeOwner says to return nodes for content types that own 
    *       a template.</td>
    *    </tr>
    *    <tr>
    *       <td>userPathRootName</td>
    *       <td>one of the root names in the USER_FILE type</td>
    *       <td>The name of the 'tree' in the USER_FILE model if user created 
    *       folder structures are allowed. If not provided, defaults to 
    *       templates.</td>
    *    </tr>
    * </table> 
    * Not all combinations are allowed.
    * 
    * @param contextProps Multiple properties allowed. All values are
    * case-insensitive.
    */
   public PSTemplateCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      
      validatePropertyValue(CATEGORY_PROPNAME, new String[] {
            CATEGORY_UNCATEGORIZED, CATEGORY_CONTENT_TYPE_OWNER }, true);

      validatePropertyValue(OUTPUT_TYPE_PROPNAME, new String[] {
            OUTPUT_TYPE_GLOBAL, OUTPUT_TYPE_NON_GLOBAL }, true);

      validatePropertyValue(TEMPLATE_TYPE_PROPNAME, new String[] {
            TEMPLATE_TYPE_LOCAL, TEMPLATE_TYPE_SHARED, TEMPLATE_TYPE_VARIANT }, 
            true);
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
            String category = PSTemplateCatalogFactory.this
                  .getContextProperty(CATEGORY_PROPNAME);
            if (category.equalsIgnoreCase(CATEGORY_UNCATEGORIZED))
               refs = getUncategorizedTemplates(force);
            else if (category.equalsIgnoreCase(CATEGORY_CONTENT_TYPE_OWNER))
               refs = getLocalTemplateContentType(parent, force);
            else
            {
               String outputType = PSTemplateCatalogFactory.this
                     .getContextProperty(OUTPUT_TYPE_PROPNAME);
               if (outputType.equalsIgnoreCase(OUTPUT_TYPE_GLOBAL))
                  refs = getGlobalTemplates(force);
               else
               {
                  String templateType = PSTemplateCatalogFactory.this
                     .getContextProperty(TEMPLATE_TYPE_PROPNAME);
                  if (templateType.equalsIgnoreCase(TEMPLATE_TYPE_LOCAL))
                     refs = getLocalTemplates(force);
                  else if (templateType.equalsIgnoreCase(TEMPLATE_TYPE_VARIANT))
                     refs = getVariants(force);
                  else
                  {
                     throw new UnsupportedOperationException(
                           "Unsupported combination of properties.");
                  }
               }
            }
            
            return createNodes(refs);
         }

         /**
          * Builds the list of template refs that should appear in the 'Local' 
          * node of the templates tree.
          * 
          * @param force See {@link #createCatalog(PSUiReference)}.
          * 
          * @return Never <code>null</code>, may be empty.
          */
         private Collection<IPSReference> getLocalTemplates(boolean force)
            throws PSModelException
         {
            IPSCmsModel templateModel = getModel(PSObjectTypes.TEMPLATE);
            Collection<IPSReference> localTemplates = templateModel.catalog(
                  force, PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                        PSObjectTypes.TemplateSubTypes.LOCAL));
            return localTemplates;
         }


         /**
          * Builds the list of template refs that should appear in the 'XSL
          * Variants' node of the tree.
          * 
          * @param force See {@link #createCatalog(PSUiReference)}.
          * 
          * @return Never <code>null</code>, may be empty.
          */
         private Collection<IPSReference> getVariants(boolean force)
            throws PSModelException
         {
            IPSCmsModel templateModel = getModel(PSObjectTypes.TEMPLATE);
            Collection<IPSReference> localTemplates = templateModel.catalog(
                  force, PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                        PSObjectTypes.TemplateSubTypes.VARIANT));
            return localTemplates;
         }

         /**
          * Builds the list of template refs that should appear in the 'Global' 
          * node of the templates tree.
          * 
          * @param force See {@link #createCatalog(PSUiReference)}.
          * 
          * @return Never <code>null</code>, may be empty.
          */
         private Collection<IPSReference> getGlobalTemplates(boolean force)
            throws PSModelException
         {
            return getModel(PSObjectTypes.TEMPLATE).catalog(force, 
                  new PSObjectType[] {
                  PSObjectTypeFactory.getType(
                        PSObjectTypes.TEMPLATE,
                        PSObjectTypes.TemplateSubTypes.GLOBAL)});
         }

         /**
          * Finds all associations between local templates and content types
          * and returns the content type that owns the supplied template.
          * 
          * @param localTemplateNode The node that contains the local template
          * for which the ctype owner is desired. Assumed not <code>null</code>.
          * 
          * @param force If true, the catalog is sent to the server rather than
          * using cached data if available.
          * 
          * @return Never <code>null</code>.
          * 
          * @throws PSModelException If any problems communicating with the 
          * server.
          */
         private Collection<IPSReference> getLocalTemplateContentType(
               PSUiReference localTemplateNode, boolean force)
            throws PSModelException
         {
            try
            {
               IPSContentTypeModel ctModel = getContentTypeModel();
               Map<IPSReference, Collection<IPSReference>> associations = ctModel
                     .getTemplateAssociations(null, force, false);
               Collection<IPSReference> results = new ArrayList<IPSReference>();
               for (IPSReference ctypeRef : associations.keySet())
               {
                  for (IPSReference templateRef : associations.get(ctypeRef))
                  {
                     if (templateRef.getId().equals(localTemplateNode.getId()))
                        results.add(ctypeRef);
                  }
               }
               assert(results.size() < 2);
               return results;
            }
            catch (PSLockException e)
            {
               //will never happen because we aren't locking
               throw new RuntimeException("Should never happen.");
            }
         }
         
         /**
          * Builds the list of template refs that should appear in the 'Shared' 
          * node of the templates tree.
          * 
          * @param force See {@link #createCatalog(PSUiReference)}.
          * 
          * @return Never <code>null</code>, may be empty.
          */
         private Collection<IPSReference> getUncategorizedTemplates(
               boolean force)
            throws PSModelException
         {
            String templateType = PSTemplateCatalogFactory.this
               .getContextProperty(TEMPLATE_TYPE_PROPNAME);
            if (templateType.equalsIgnoreCase(TEMPLATE_TYPE_SHARED))
            {
               return getUncategorizedRefs(getTreeName(), force,
                  PSObjectTypes.TEMPLATE, new PSObjectType[] {
                        PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                              PSObjectTypes.TemplateSubTypes.SHARED),
                        PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                              PSObjectTypes.TemplateSubTypes.OTHER) });
            }
            else if (templateType.equalsIgnoreCase(TEMPLATE_TYPE_VARIANT))
            {
               return getUncategorizedRefs(getTreeName(), force,
                  PSObjectTypes.TEMPLATE, new PSObjectType[] {
                        PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                              PSObjectTypes.TemplateSubTypes.VARIANT) });
            }
            throw new UnsupportedOperationException(
            "category=uncategorized only works w/ templateType=shared");
         }

         /**
          * Looks up the tree name using the property name "userPathRootName".
          * 
          * @return Never <code>null</code> or empty. If no property is present,
          * "templates" is returned.
          */
         private String getTreeName()
         {
            String treeName = PSTemplateCatalogFactory.this
               .getContextProperty(USER_PATH_ROOTNAME_PROPNAME);
            if (StringUtils.isBlank(treeName))
               treeName = "templates";
            return treeName;
         }
      };
   }

   /**
    * The name of the property that controls whether to get templates that don't
    * appear under some other node or content types that have local templates.
    */
   private static String USER_PATH_ROOTNAME_PROPNAME =  "userPathRootName";

   /**
    * The name of the property that controls whether to get templates that don't
    * appear under some other node or content types that have local templates.
    */
   private static String CATEGORY_PROPNAME =  "category";
   
   /**
    * One of the values for the {@link #CATEGORY_PROPNAME} property.
    */
   private static String CATEGORY_UNCATEGORIZED =  "uncategorized";
   
   /**
    * One of the values for the {@link #CATEGORY_PROPNAME} property.
    */
   private static String CATEGORY_CONTENT_TYPE_OWNER =  "contentTypeOwner";
   
   /**
    * The name of the property that controls whether to get global templates
    * or non-global templates.
    */
   private static String OUTPUT_TYPE_PROPNAME =  "outputType";
   
   /**
    * One of the values for the {@link #OUTPUT_TYPE_PROPNAME} property.
    */
   private static String OUTPUT_TYPE_GLOBAL =  "global";
   
   /**
    * One of the values for the {@link #OUTPUT_TYPE_PROPNAME} property.
    */
   private static String OUTPUT_TYPE_NON_GLOBAL =  "non-global";
   
   /**
    * The name of the property that controls whether to get local or shared
    * templates.
    */
   private static String TEMPLATE_TYPE_PROPNAME =  "templateType";
   
   /**
    * One of the values for the {@link #TEMPLATE_TYPE_PROPNAME} property.
    */
   private static String TEMPLATE_TYPE_LOCAL =  "local";
   
   /**
    * One of the values for the {@link #TEMPLATE_TYPE_PROPNAME} property.
    */
   private static String TEMPLATE_TYPE_SHARED =  "shared";

   /**
    * One of the values for the {@link #TEMPLATE_TYPE_PROPNAME} property.
    */
   private static String TEMPLATE_TYPE_VARIANT =  "variant";
}

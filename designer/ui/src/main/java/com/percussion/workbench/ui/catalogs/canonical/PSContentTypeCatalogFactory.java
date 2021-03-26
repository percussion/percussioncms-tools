/******************************************************************************
 *
 * [ PSContentTypeCatalogFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.IPSUserFileModel;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Gets the content types based on supplied filters. Guarantees that each
 * content type instance will only appear under a single category.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSContentTypeCatalogFactory extends PSCatalogFactoryBase
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
    * <td>contentTypeCategory</td>
    * <td>uncategorized, navigation</td>
    * <td>Where in the tree the nodes are intended. 'uncategorized' says to
    * return all nodes that don't have a folder parent. This parameter is
    * optional. If not provided, all known content types are returned.</td>
    * </tr>
    * </table>
    * 
    * @param contextProps One optional property may be supplied.
    */
   public PSContentTypeCatalogFactory(InheritedProperties contextProps,
      PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      validateProps();
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
            String contentTypeCategory = PSContentTypeCatalogFactory.this
               .getContextProperty(CONTENT_TYPE_CATEGORY_PROPNAME);

            if (StringUtils.isBlank(contentTypeCategory))
            {
               // the contentTypeCategory property was not supplied,
               // return all content types
               refs = getAllContentTypes(force);
            }
            else
            {
               if (contentTypeCategory
                  .equalsIgnoreCase(CONTENT_TYPE_CATEGORY_UNCATEGORIZED))
                  refs = getUncategorizedContentTypes(force);
               else if (contentTypeCategory
                  .equalsIgnoreCase(CONTENT_TYPE_CATEGORY_NAVIGATION))
                  refs = getNavigationContentTypes(force);
            }
            return createNodes(refs);
         }

         /**
          * Builds the list of content type refs that should appear in the
          * 'Navigation' node of the content types tree.
          * 
          * @param force See {@link #createCatalog(PSUiReference)}.
          * 
          * @return Never <code>null</code>, may be empty.
          */
         private Collection<IPSReference> getNavigationContentTypes(
            boolean force) throws PSModelException
         {
            Collection<IPSReference> navTypes = new ArrayList<IPSReference>();

            // get reference to navigation.properties
            IPSCmsModel configModel = getConfigModel();
            Collection<IPSReference> configs = configModel.catalog(force);
            IPSReference navConfigRef = null;
            for (IPSReference ref : configs)
            {
               if (ref.getObjectType().getSecondaryType().equals(
                  PSObjectTypes.ConfigurationFileSubTypes.NAVIGATION_PROPERTIES))
               {
                  navConfigRef = ref;
                  break;
               }
            }

            if (navConfigRef == null)
            {
               //some systems don't have managed nav
               return navTypes;
            }
            try
            {
               // get the contents
               PSMimeContentAdapter navContent = (PSMimeContentAdapter) 
                     configModel.load(navConfigRef, false, false);

               // load as properties
               Properties navProps = new Properties();
               InputStream navStream = navContent.getContent();
               navProps.load(navStream);
               navStream.reset();

               Set<String> navTypeNames = new HashSet<String>();
               String navimage = navProps
                  .getProperty("navimage.content_type");

               if (!StringUtils.isBlank(navimage))
                  navTypeNames.add(navimage.toLowerCase());

               String navon = navProps.getProperty("navon.content_type");

               if (!StringUtils.isBlank(navon))
                  navTypeNames.add(navon.toLowerCase());

               String navtree = navProps.getProperty("navtree.content_type");

               if (!StringUtils.isBlank(navtree))
                  navTypeNames.add(navtree.toLowerCase());

               // load all content types for comparison
               Collection<IPSReference> contentTypes = 
                  getAllContentTypes(force);

               for (IPSReference type : contentTypes)
               {
                  if (navTypeNames.contains(type.getName().toLowerCase()))
                     navTypes.add(type);
               }
            }
            catch (Exception e)
            {
               if (e instanceof PSModelException)
                  throw (PSModelException) e;
               throw new PSModelException(e);
            }

            return navTypes;
         }

         /**
          * Builds the list of content type refs that should appear in the
          * content types tree outside of any other nodes.
          * 
          * @param force See {@link #createCatalog(PSUiReference)}.
          * 
          * @return Never <code>null</code>, may be empty.
          */
         private Collection<IPSReference> getUncategorizedContentTypes(
            boolean force) throws PSModelException
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

            // get Navigation id's
            Collection<IPSReference> navigationTypes = 
               getNavigationContentTypes(force);
            for (IPSReference navigationType : navigationTypes)
            {
               String typeId = navigationType.getId().toString();
               if (!StringUtils.isBlank(typeId))
                  ids.add(typeId);
            }

            Collection<IPSReference> contentTypes = getAllContentTypes(force);
            for (Iterator<IPSReference> iter = contentTypes.iterator(); iter
               .hasNext();)
            {
               if (ids.contains(iter.next().getId().toString()))
                  iter.remove();
            }
            return contentTypes;
         }

         /**
          * Builds the list of all content type refs that should appear in the
          * content types tree. By default, "Folder" content types will not be
          * returned. A special property, see {@link #SHOW_FOLDERS_PROPNAME},
          * may be provided to display these types.
          * 
          * @param force See {@link #createCatalog(PSUiReference)}.
          * 
          * @return Never <code>null</code>, may be empty.
          */
         private Collection<IPSReference> getAllContentTypes(boolean force)
            throws PSModelException
         {
            IPSContentTypeModel contentModel = getContentTypeModel();
            Collection<IPSReference> contentTypes;
            if (StringUtils.isBlank(PSContentTypeCatalogFactory.this
                  .getContextProperty(SHOW_FOLDERS_PROPNAME)))
            {
               contentTypes = contentModel.getUseableContentTypes(force);
            }
            else
               contentTypes = contentModel.catalog(force);
            return contentTypes;
         }

         /**
          * Simple method to wrap the exception that should never happen.
          * 
          * @return Never <code>null</code>.
          */
         private IPSCmsModel getConfigModel()
         {
            try
            {
               return PSCoreFactory.getInstance().getModel(
                  PSObjectTypes.CONFIGURATION_FILE);
            }
            catch (PSModelException e)
            {
               // shouldn't happen
               throw new RuntimeException(e);
            }
         }

         /**
          * Looks up the tree name using the property name "userPathRootName".
          * 
          * @return Never <code>null</code> or empty. If no property is
          * present, "slots" is returned.
          */
         private String getTreeName()
         {
            String treeName = PSContentTypeCatalogFactory.this
               .getContextProperty("userPathRootName");
            if (StringUtils.isBlank(treeName))
               treeName = "contentTypes";
            return treeName;
         }
      };
   }

   /**
    * Validates context properties supplied in ctor
    */
   private void validateProps()
   {
      validatePropertyValue(CONTENT_TYPE_CATEGORY_PROPNAME, new String[]
      {
         CONTENT_TYPE_CATEGORY_UNCATEGORIZED, CONTENT_TYPE_CATEGORY_NAVIGATION
      }, true);
   }

   /**
    * The name of the property that controls where in the tree the nodes are
    * intended.
    */
   private static final String CONTENT_TYPE_CATEGORY_PROPNAME = 
      "contentTypeCategory";

   /**
    * One of the values for the {@link #CONTENT_TYPE_CATEGORY_PROPNAME}
    * property.
    */
   private static String CONTENT_TYPE_CATEGORY_UNCATEGORIZED = "uncategorized";

   /**
    * One of the values for the {@link #CONTENT_TYPE_CATEGORY_PROPNAME}
    * property.
    */
   private static String CONTENT_TYPE_CATEGORY_NAVIGATION = "navigation";

   /**
    * The name of the property that controls whether or not "Folder" content
    * types will be displayed.
    */
   private static String SHOW_FOLDERS_PROPNAME = "showFolders";

}

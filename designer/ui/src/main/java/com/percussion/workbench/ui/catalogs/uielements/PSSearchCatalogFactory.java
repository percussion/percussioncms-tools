/*******************************************************************************
 *
 * [ PSSearchCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs.uielements;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiErrorCodes;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * Creates a cataloger that retrieves searches designed for use by the Content
 * Explorer based on provided filter criteria. The filters differentiate custom
 * versus standard searches.
 * <p>
 * The user can determine which type the node is by looking for the
 * {@link #FILTER_TYPE_PROPNAME} in the node. It's value is equal to the value
 * supplied when the catalog was performed. For example, if the
 * {@link #FT_STANDARD} value was supplied, all returned nodes would be standard
 * searches and the nodes would have the <code>FILTER_TYPE_PROPNAME</code>
 * present with the value <code>FT_STANDARD</code>.
 * <p>
 * This class knows how to match a search to one of the nodes in the 
 * uielements_viewHierarchyDef.xsd definition file. The structure is as follows:
 * <pre>
 * Searches
 *    - Standard
 *    - Custom
 * </pre>
 *
 * @version 6.0
 * @author Paul Howard
 */
public class PSSearchCatalogFactory extends PSCatalogFactoryBase 
{
   /**
    * The name of the property that controls which type of searches will be 
    * returned.
    */
   public static final String FILTER_TYPE_PROPNAME = "filterType";

   /**
    * One of the values for the {@link #FILTER_TYPE_PROPNAME} property.
    */
   public static final String FT_STANDARD = "standard";

   /**
    * One of the values for the {@link #FILTER_TYPE_PROPNAME} property.
    */
   public static final String FT_CUSTOM = "url";
   
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps One optional property may be supplied:
    * {@link #FILTER_TYPE_PROPNAME}. The allowed values are the FT_xxx constants.
    * If not provided, all searches will be returned. All values are
    * case-sensitive.
    */
   public PSSearchCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      validatePropertyValue(FILTER_TYPE_PROPNAME, new String[] { FT_STANDARD,
            FT_CUSTOM }, true);
   }

   //see interface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new SearchCataloger(parent, PSObjectTypes.UI_SEARCH);
   }
   
   /**
    * Builds an exception stating that a needed property was not found with the
    * right values and then throws it.
    */
   protected void throwException(String badValue)
      throws PSModelException
   {
      StringBuffer buf = new StringBuffer();
      buf.append(FT_STANDARD);
      buf.append(", ");
      buf.append(FT_CUSTOM);
      String[] args = {FILTER_TYPE_PROPNAME, badValue,
            buf.toString() };
      throw new PSModelException(
            PSUiErrorCodes.MISSING_PROPERTY_WITH_VALUES, args);      
   }

   /**
    * Designed to be used by searches and views.
    *
    * @author paulhoward
    */
   protected class SearchCataloger extends BaseCataloger
   {

      /**
       * Only ctor.
       * 
       * @param parent The node that will receive the cataloged children. Never
       * <code>null</code>.
       * 
       * @param modelType Which model to use to catalog the objects. Never
       * <code>null</code>.
       */
      public SearchCataloger(PSUiReference parent, PSObjectTypes modelType)
      {
         super(parent);
         if (null == modelType)
         {
            throw new IllegalArgumentException("modelType cannot be null");  
         }
         
         m_modelType = modelType;
      }
      
      //see interface for details
      public List<PSUiReference> getEntries(boolean force)
         throws PSModelException
      {
         String category = PSSearchCatalogFactory.this
               .getContextProperty(FILTER_TYPE_PROPNAME);
         
         IPSCmsModel model = getModel(m_modelType);
         Collection<IPSReference> refs; //= model.catalog(force);
         PSObjectType filterType = null;
         if (!StringUtils.isBlank(category))
         {
            if (category.equalsIgnoreCase(FT_CUSTOM))
            {
               filterType = PSObjectTypeFactory.getType(m_modelType,
                     PSObjectTypes.SearchSubTypes.CUSTOM);
            }
            else if (category.equalsIgnoreCase(FT_STANDARD))
            {
               filterType = PSObjectTypeFactory.getType(m_modelType,
                     PSObjectTypes.SearchSubTypes.STANDARD);
            }
            else 
               assert(false);
         }
         refs = model.catalog(force, filterType);
         return createNodes(refs);
      }

      /**
       * See ctor for details.
       */
      private final PSObjectTypes m_modelType;
   }
}

/*******************************************************************************
 *
 * [ PSKeywordExpansionFactory.java ]
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
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
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
import java.util.List;

/**
 * Creates a cataloger that retrieves the choices for a particular keyword.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSKeywordExpansionFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps Not used. 
    */
   public PSKeywordExpansionFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);;
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
            List<PSUiReference> nodes = new ArrayList<PSUiReference>();
            IPSCmsModel keywordModel = getModel(PSObjectTypes.KEYWORD);
            IPSReference keywordRef = getAncestorNode(PSObjectTypes.KEYWORD).getReference();
           
            if (keywordRef == null)
            {
               String[] args = { PSObjectTypes.KEYWORD.name(),
                     getParent().getPath() };
               Exception e = new PSHierarchyDefinitionException(PSMessages
                     .getString("common.error.badmodel", args));
               throw new PSModelException(e);
            }
            
            try
            {
               PSKeyword keyword = 
                  (PSKeyword) keywordModel.load(keywordRef, false, false);
               
               nodes = getChoices(keyword);
            }
            catch (Exception e)
            {
               nodes = new ArrayList<PSUiReference>();
               nodes.add(createErrorNode(e));
            }
            
            return nodes;
         }

         /**
          * Builds a list of nodes containing a given keyword's choices.
          * 
          * @param keyword the keyword object, assumed not <code>null</code>.
          * @return a list of PSUiReferences to the keyword's choices.
          */
         private List<PSUiReference> getChoices(
               PSKeyword keyword)
            throws PSModelException
         {
            List<PSUiReference> nodes = new ArrayList<PSUiReference>();
            List<PSKeywordChoice> choices = keyword.getChoices();
                                    
            for (PSKeywordChoice choice : choices)
            {
               PSUiReference node = new PSUiReference(getParent(), choice
                     .getLabel(), choice.getDescription(), choice, null, false);
               IPSDeclarativeNodeHandler handler = new PSIconNodeHandler(
                     null, "icons/keywordChoice16.gif", null);
               node.setHandler(handler);
               nodes.add(node);
            }
            
            return nodes;
         }
      };
   }
}

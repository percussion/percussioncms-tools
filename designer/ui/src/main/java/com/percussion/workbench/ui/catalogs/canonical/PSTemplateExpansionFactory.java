/******************************************************************************
 *
 * [ PSTemplateExpansionFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiErrorCodes;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefinitionException;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a cataloger that contains slot or content type associations based on
 * the supplied property.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSTemplateExpansionFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps One optional property may be supplied: 'linkType'. The
    * allowed values are defined by the <code>xxx_ASSOCIATION</code>
    * constants; if not provided, {@link #SLOT_ASSOCIATION} is used. All names
    * and values are case-insensitive.
    */
   public PSTemplateExpansionFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      
      validatePropertyValue(LINK_TYPE_PROPNAME, new String[] {
            SLOT_ASSOCIATION, CONTENT_TYPE_ASSOCIATION }, true);
   }
   
   //see interface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         //see interface
         public List<PSUiReference> getEntries(boolean force) 
            throws PSModelException
         {
            Collection<IPSReference> refs = new ArrayList<IPSReference>();
            String association = PSTemplateExpansionFactory.this
                  .getContextProperty(LINK_TYPE_PROPNAME);
            PSUiReference templateAncestor = 
               getAncestorNode(PSObjectTypes.TEMPLATE);
            if (templateAncestor == null)
            {
               Object[] args = { getParent().getPath(),
                     PSMessages.getString(PSObjectTypes.TEMPLATE.name()) }; 
               throw new PSModelException(PSUiErrorCodes.ANCESTOR_NOT_FOUND,
                     args);
            }
            if (StringUtils.isBlank(association)
                  || association.equalsIgnoreCase(SLOT_ASSOCIATION))
            {
               refs = getSlotAssociations(templateAncestor, force);
            }
            else
            {
               refs = getContentTypeAssociations(templateAncestor, force);
               //register for changes
               getUpdater(PSTemplateExpansionFactory.this).setType(
                     templateAncestor.getReference(), getParent());
            }

            return createNodes(refs);
         }
         
         /**
          * Finds all content types that use the supplied template.
          * 
          * @param templateNode Assumed not <code>null</code>.
          * 
          * @param force If <code>true</code>, flushes the cache before loading
          * the template to get the slots.
          * 
          * @return Never <code>null</code>, may be empty.
          * 
          * @throws PSModelException If any problems communicating w/ server.
          */
         private Collection<IPSReference> getContentTypeAssociations(
               PSUiReference templateNode, boolean force)
            throws PSModelException
         {
            return getTemplateModel().getContentTypes(
                  templateNode.getReference(), force);
         }

         /**
          * Finds all slots that are present in the supplied template
          * definition.
          * 
          * @param templateNode The template ancestor node. Assumed not
          * <code>null</code>.
          * 
          * @param force If <code>true</code>, flushes the cache before
          * loading the template to get the slots.
          * 
          * @return Never <code>null</code>, may be empty.
          * 
          * @throws PSModelException If any problems communicating w/ server.
          */
         private Collection<IPSReference> getSlotAssociations(
               PSUiReference templateNode, boolean force)
            throws PSModelException
         {
            IPSCmsModel templateModel = getModel(PSObjectTypes.TEMPLATE);
            IPSReference templateRef = templateNode.getReference();
            if (templateRef == null)
            {
               Object[] args = { PSObjectTypes.TEMPLATE.name(),
                     getParent().getPath() };
               Exception e = new PSHierarchyDefinitionException(
                     PSMessages.getString("common.error.badmodel", args));
               throw new PSModelException(e);
            }
            PSUiAssemblyTemplate templateData;
            try
            {
               if (force)
                  templateModel.flush(templateRef);
               templateData = (PSUiAssemblyTemplate) templateModel
                     .load(templateRef, false, false);
            }
            catch (Exception e)
            {
               if (e instanceof PSModelException)
                  throw (PSModelException) e;
               throw new PSModelException(e);
            }
            Collection<IPSGuid> containedSlots = templateData.getSlotGuids();
            Collection<IPSReference> results = new ArrayList<IPSReference>();
            IPSCmsModel slotModel = getModel(PSObjectTypes.SLOT);
            for (IPSGuid slot : containedSlots)
            {
               results.add(slotModel.getReference(slot));
            }
            return results;
         }
      };
   }
   
   /**
    * This class registers itself as a listener on the content type model so it
    * can be informed of ctype/template link changes. When this happens, if the
    * changes affect any tracked template, the new/deleted content type is
    * added/removed from the proper folder.
    * <p>
    * Types are added for tracking by calling the
    * {@link #setType(IPSReference, PSUiReference)} method.
    * 
    * @author paulhoward
    */
   private class NodeUpdater implements IPSModelListener
   {
      /**
       * Only ctor. Registers itself w/ the template model.
       */
      public NodeUpdater()
      {
         try
         {
            PSCoreFactory.getInstance().getModel(PSObjectTypes.CONTENT_TYPE)
                  .addListener(this,
                     PSModelChangedEvent.ModelEvents.LINKS_ADDED.getFlag()
                     | PSModelChangedEvent.ModelEvents.LINKS_DELETED.getFlag());
         }
         catch (PSModelException e)
         {
            //should never happen
            throw new RuntimeException(e);
         }
      }
      
      /**
       * Add a new content type to track. If it is already being tracked, the
       * supplied parent replaces the previously registered one. Any tracked
       * type will automatically be updated if a new template is creasted that
       * links to that type.
       * 
       * @param templateRef A content type that is in the tree. Assumed not
       * <code>null</code>.
       * 
       * @param ctypeLinksParent The node that contains the linked 
       * template children. Assumed not <code>null</code>.
       */
      public void setType(IPSReference templateRef, PSUiReference ctypeLinksParent)
      {
         PSPair<IPSReference, PSUiReference> info = getInfo(templateRef);
         if (info == null)
         {
            info = new PSPair<IPSReference, PSUiReference>();
            m_nodeInfos.add(info);
         }
         info.setFirst(templateRef);
         info.setSecond(ctypeLinksParent);
      }

      /**
       * Updates the design object model if any ctype/template links are added
       * or deleted. See interface for more details.
       */
      public void modelChanged(PSModelChangedEvent event)
      {
         for (IPSReference templateRef : event.getSource())
         {
            PSUiReference parentNode = getParent(templateRef);
            if (null == parentNode)
               continue;
            if (event.getEventType() == ModelEvents.LINKS_ADDED)
            {
               PSDesignObjectHierarchy.getInstance().addChildren(parentNode,
                  new IPSReference[] {event.getLinkOwner()}, false, false);
            }
            else if (event.getEventType() == ModelEvents.LINKS_DELETED)
            {
               Collection<PSUiReference> possibles = 
                  PSDesignObjectHierarchy.getInstance().getNodes(event.getLinkOwner());
               for (PSUiReference node : possibles)
               {
                  if (node.getParentNode().equals(parentNode))
                  {
                     PSDesignObjectHierarchy.getInstance().deleteChildren(
                           Collections.singletonList(node));
                  }
               }
            }
            else
               assert(false);
         }
      }
      
      /**
       * Scans the {@link #m_nodeInfos} set for a matching content type,
       * returning the associated node if found.
       * 
       * @param templateRef The handle of a template. Assumed not
       * <code>null</code>.
       * 
       * @return A valid node that is the parent node for linked content types,
       * if the supplied template has been registered. Otherwise,
       * <code>null</code> is returned.
       */
      private PSUiReference getParent(IPSReference templateRef)
      {
         PSPair<IPSReference, PSUiReference> info = getInfo(templateRef);
         return info == null ? null : info.getSecond();
      }
      
      /**
       * Walks the local list of pairs, looking for one whose type matches the
       * supplied type.
       * 
       * @param template Assumed never <code>null</code>.
       * 
       * @return The matching pair, or <code>null</code> if not found.
       */
      private PSPair<IPSReference, PSUiReference> getInfo(IPSReference template)
      {
         PSPair<IPSReference, PSUiReference> result = null;
         Iterator<PSPair<IPSReference, PSUiReference>> iter = m_nodeInfos
               .iterator();
         while (iter.hasNext())
         {
            PSPair<IPSReference, PSUiReference> info = iter.next();
            /* Make this list self cleaning. This can happen if someone converts
             * a local template to a shared template.
             */
            if (info.getFirst().getObjectType().equals(
                  PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                        PSObjectTypes.TemplateSubTypes.SHARED)))
            {
               iter.remove();
               continue;
            }
            if (!info.getFirst().equals(template))
               continue;
            result = info;
            break;
         }
         return result;
      }
      
      /**
       * First is the template ref. Second is the parent node containing the
       * content type links.
       * 
       * DONT use map - key may change
       */
      private List<PSPair<IPSReference, PSUiReference>> m_nodeInfos = 
         new ArrayList<PSPair<IPSReference, PSUiReference>>(); 
   }

   /**
    * Gets the single instance of the template creation tracker. The instance
    * is created the first time the method is called.
    * 
    * @return Never <code>null</code>.
    */
   private static NodeUpdater getUpdater(PSTemplateExpansionFactory factory)
   {
      if (ms_nodeUpdater == null)
         ms_nodeUpdater = factory.new NodeUpdater();
      return ms_nodeUpdater;
   }

   /**
    * This guy tracks newly created templates and adds children to the
    * appropriate 'Allowed Templates...' node. Lazily initialized via the
    * {@link #getUpdater(PSTemplateExpansionFactory)} method, then never
    * changed.
    */
   private static NodeUpdater ms_nodeUpdater;

   /**
    * The name of the property that controls whether we return associated
    * slots or associated content types.
    */
   private static String LINK_TYPE_PROPNAME = "linkType";
   
   /**
    * One of the values for the {@link #LINK_TYPE_PROPNAME} property that 
    * specifies to return slots.
    */
   private static String SLOT_ASSOCIATION = "containedSlots";
   
   /**
    * One of the values for the {@link #LINK_TYPE_PROPNAME} property that 
    * specifies to return content types.
    */
   private static String CONTENT_TYPE_ASSOCIATION = "contentType";
}

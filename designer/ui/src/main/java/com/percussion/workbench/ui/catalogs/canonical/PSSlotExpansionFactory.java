/*******************************************************************************
 *
 * [ PSSlotExpansionFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSMessages;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Creates a cataloger that retrieves the templates that are allowed within
 * a specified slot.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSSlotExpansionFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps One optional property may be supplied: 'linkType'. The
    * allowed values are defined by the <code>xxx_ASSOCIATION</code>
    * constants; if not provided, {@link #CONTENT_TYPE_ASSOCIATION} is used. All
    * names and values are case-insensitive.
    */
   public PSSlotExpansionFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);

      validatePropertyValue(LINK_TYPE_PROPNAME, new String[] {
            TEMPLATE_ASSOCIATION, CONTENT_TYPE_ASSOCIATION }, true);
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
            IPSCmsModel slotModel = getModel(PSObjectTypes.SLOT);

            IPSReference slotRef = getAncestorNode(PSObjectTypes.SLOT)
                  .getReference();
            
            if (slotRef == null)
            {
               Object[] args = 
               { 
                  PSObjectTypes.SLOT.name(),
                  getParent().getPath()
               };
               Exception e = new PSHierarchyDefinitionException(
                  PSMessages.getString("common.error.badmodel", args));
               
               throw new PSModelException(e);
            }
            
            try
            {
               IPSTemplateSlot slotData = 
                  (IPSTemplateSlot) slotModel.load(slotRef, false, false);

               String association = 
                  PSSlotExpansionFactory.this.getContextProperty(
                     LINK_TYPE_PROPNAME);
               if (StringUtils.isBlank(association) || 
                  association.equalsIgnoreCase(TEMPLATE_ASSOCIATION))
               {
                  refs = getAllowedTemplates(slotData, getAncestorNode(
                        PSObjectTypes.CONTENT_TYPE).getReference());
                  
                  // register allowed templates
                  getUpdater(PSSlotExpansionFactory.this).setAllowedTemplates(
                     getParent(), refs);
               }
               else
               {
                  refs = getContentTypeAssociations(slotData);
               }
   
               return createNodes(refs);
            }
            catch (Exception e)
            {
               if (e instanceof PSModelException)
                  throw (PSModelException) e;
               else
                  throw new PSModelException(e);
            }
         }

         /**
          * Builds a list of refs to a given slot's allowed content types.
          * 
          * @param slotData Assumed not <code>null</code>.
          * 
          * @return A collection of refs to the slot's allowed content types.
          * 
          * @throws PSModelException If any problems communicating with the 
          * server.
          */
         private Collection<IPSReference> getContentTypeAssociations(
            IPSTemplateSlot slotData)
            throws PSModelException
         {
            //there may be multiples of the same type, so use set to dedupe them
            Set<IPSGuid> guids = new HashSet<IPSGuid>();
            Collection<PSPair<IPSGuid, IPSGuid>> associations =
               slotData.getSlotAssociations();
            for (PSPair<IPSGuid, IPSGuid> association : associations)
            {
               guids.add(association.getFirst());
            }

            Collection<IPSReference> ctypeRefs = new ArrayList<IPSReference>();
            IPSCmsModel ctypeModel = getModel(PSObjectTypes.CONTENT_TYPE);
            for (IPSGuid ctypeGuid : guids)
            {
               IPSReference ref = ctypeModel.getReference(ctypeGuid);
               if(ref == null)
                  continue;
               ctypeRefs.add(ctypeModel.getReference(ctypeGuid));
            }
            
            return ctypeRefs;
         }

         /**
          * Builds a list of refs to a given slot's allowed templates, filtered
          * by the supplied content type.
          * 
          * @param slotData the slot which contains the allowed templates,
          * assumed not <code>null</code>.
          * 
          * @param ctypeFilter Only get allowed templates whose entry
          * includes the supplied content type. Assumed not <code>null</code>.
          * 
          * @return A collection of refs to the slot's allowed templates,
          *    never <code>null</code>, may be empty.
          * 
          * @throws PSModelException If any problems communicating with the 
          * server.
          */
         private Collection<IPSReference> getAllowedTemplates(
            IPSTemplateSlot slotData, IPSReference ctypeFilter)
            throws PSModelException
         {
            //there may be multiples of the same type, so use set to dedupe them
            Set<IPSGuid> guids = new HashSet<IPSGuid>();
            Collection<PSPair<IPSGuid, IPSGuid>> associations =
               slotData.getSlotAssociations();
            for (PSPair<IPSGuid, IPSGuid> association : associations)
            {
               IPSGuid ctypeGuid = association.getFirst();
               if (ctypeGuid.equals(ctypeFilter.getId()))
                  guids.add(association.getSecond());
            }

            Collection<IPSReference> templateRefs = new ArrayList<IPSReference>();
            IPSCmsModel templateModel = getModel(PSObjectTypes.TEMPLATE);
            for (IPSGuid templateGuid : guids)
            {
               IPSReference ref = templateModel.getReference(templateGuid);
               if (ref != null)
                  templateRefs.add(ref);
            }
            
            return templateRefs;
         }
      };
   }

   /**
    * Gets the single instance of the template deletion tracker. The instance
    * is created the first time the method is called.
    * 
    * @return the template deletion tracker, never <code>null</code>.
    */
   private static NodeUpdater getUpdater(PSSlotExpansionFactory factory)
   {
      if (ms_nodeUpdater == null)
         ms_nodeUpdater = factory.new NodeUpdater();
      
      return ms_nodeUpdater;
   }
   
   /**
    * Updates slot nodes if associated templates are deleted.
    */
   private class NodeUpdater implements IPSModelListener
   {
      /**
       * The only constructor registers itself with the template model as 
       * delete listener.
       */
      public NodeUpdater()
      {
         try
         {
            PSCoreFactory.getInstance().getModel(
               PSObjectTypes.TEMPLATE).addListener(this, 
                  PSModelChangedEvent.ModelEvents.DELETED.getFlag());
         }
         catch (PSModelException e)
         {
            // should never happen
            throw new RuntimeException(e);
         }
      }
      
      /**
       * Set the allowed templates for the supplied parent.
       * 
       * @param parent the parent node which contains the supplied allowed
       *    templates, not <code>null</code>.
       * @param allowedTemplates the allowed templates, not <code>null</code>,
       *    may be empty.
       */
      public void setAllowedTemplates(PSUiReference parent, 
         Collection<IPSReference> allowedTemplates)
      {
         if (parent == null)
            throw new IllegalArgumentException("parent cannot be null");
         
         if (allowedTemplates == null)
            throw new IllegalArgumentException(
               "allowedTemplates cannot be null");
         
         PSPair<PSUiReference, Collection<IPSReference>> updatePair = null;
         for (PSPair<PSUiReference, Collection<IPSReference>> pair : 
            m_allowedTemplates)
         {
            if (parent.equals(pair.getFirst()))
            {
               updatePair = pair;
               break;
            }
         }
         
         if (updatePair == null)
         {
            // create and add a new pair
            updatePair = new PSPair<PSUiReference, Collection<IPSReference>>(
               parent, new ArrayList<IPSReference>(allowedTemplates));
            m_allowedTemplates.add(updatePair);
         }
         else
         {
            // set allowed templates on an existing pair
            Collection<IPSReference> updateTemplates = updatePair.getSecond();
            updateTemplates.clear();
            updateTemplates.addAll(allowedTemplates);
         }
      }

      /**
       * Handles template delete events. If a templates is deleted this
       * walkes the registered allowed template and removes the appropriate 
       * nodes when no more allowed templates exist.
       */
      public void modelChanged(PSModelChangedEvent event)
      {
         if (event.getEventType() == PSModelChangedEvent.ModelEvents.DELETED)
         {
            for (PSPair<PSUiReference, Collection<IPSReference>> pair : 
               m_allowedTemplates)
            {
               if (pair.getSecond().isEmpty())
                  continue;
               
               PSUiReference parent = pair.getFirst();
               for (IPSReference templateRef : event.getSource())
               {
                  Iterator allowedTemplates = pair.getSecond().iterator();
                  while (allowedTemplates.hasNext())
                  {
                     if (templateRef.equals(allowedTemplates.next()))
                     {
                        allowedTemplates.remove();
                        break;
                     }
                  }
               }
               
               if (pair.getSecond().isEmpty())
               {
                  Collection<PSUiReference> ctypeNodes = 
                     new ArrayList<PSUiReference>();
                  ctypeNodes.add(parent.getParentNode());
                  PSDesignObjectHierarchy.getInstance().deleteChildren(
                     ctypeNodes);
               }
            }
         }
      }
      
      /**
       * The registered allowed templates, never <code>null</code>, may be 
       * empty. A list of pairs in which the first element is the 
       * <code>Allowed Templates</code> folder and the second element is a 
       * list of references to allowed templates.
       */
      private List<PSPair<PSUiReference, Collection<IPSReference>>> m_allowedTemplates = 
         new ArrayList<PSPair<PSUiReference, Collection<IPSReference>>>();
   }

   /**
    * This guy tracks deleted templates and removes children from the
    * appropriate 'Allowed Templates...' node. Lazily initialized via the
    * {@link #getUpdater(PSSlotExpansionFactory)} method, then never
    * <code>null</code> or changed.
    */
   private static NodeUpdater ms_nodeUpdater;

   /**
    * The name of the property that controls whether we return associated
    * slots or associated content types.
    */
   private static String LINK_TYPE_PROPNAME = "linkType";
   
   /**
    * One of the values for the {@link #LINK_TYPE_PROPNAME} property that 
    * specifies to return the templates for the parent content type.
    */
   private static String TEMPLATE_ASSOCIATION = "template";
   
   /**
    * One of the values for the {@link #LINK_TYPE_PROPNAME} property that 
    * specifies to return content types.
    */
   private static String CONTENT_TYPE_ASSOCIATION = "contentType";
}

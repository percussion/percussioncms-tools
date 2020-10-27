/*******************************************************************************
 *
 * [ PSContentTypeExpansionFactory.java ]
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
import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.models.PSLockException;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Creates a cataloger that varies depending on the value of the
 * {@link #EXPANSION_TYPE_PROPNAME} entry in the context property map supplied
 * in the ctor.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSContentTypeExpansionFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps One property must be supplied:
    * {@link #EXPANSION_TYPE_PROPNAME}. The allowed values are one of the
    * ET_xxx values.
    */
   public PSContentTypeExpansionFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      validatePropertyValue(EXPANSION_TYPE_PROPNAME, new String[] {
            ET_WORKFLOW, ET_LINKED_SHARED_TEMPLATES,
            ET_LINKED_LOCAL_TEMPLATES, ET_LINKED_VARIANT_TEMPLATES }, false);
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
            IPSContentTypeModel contentModel = getContentTypeModel();
            PSUiReference contentUiRef = 
               getAncestorNode(PSObjectTypes.CONTENT_TYPE);
            IPSReference contentRef = contentUiRef.getReference();
            
            if (contentRef == null)
            {
               String[] args = { PSObjectTypes.CONTENT_TYPE.name(),
                     getParent().getPath() };
               Exception e = new PSHierarchyDefinitionException(PSMessages
                     .getString("common.error.badmodel", (Object[]) args));
               throw new PSModelException(e);
            }
            
            final Collection<IPSReference> refs;
            try
            {
               String expansionType = PSContentTypeExpansionFactory.this.
                  getContextProperty(EXPANSION_TYPE_PROPNAME);
               
               if (expansionType.equalsIgnoreCase(ET_WORKFLOW))
               {
                  refs = contentModel.getWorkflowAssociations(contentRef, force);
               }
               else
               {
                  refs = getTemplates(contentUiRef, force);
                  //register for changes
                  getUpdater(PSContentTypeExpansionFactory.this).setType(
                        contentRef, getParent());
               }
            }
            catch (Exception e)
            {
               if (e instanceof PSModelException)
                  throw (PSModelException) e;
               throw new PSModelException(e);
            }
                        
            List<PSUiReference> results = PSDesignObjectHierarchy
                  .getInstance().addChildren(getParent(),
                  refs.toArray(new IPSReference[refs.size()]), false, false);
            return results;
         }
         
         /**
          * Finds all templates that use the supplied content type
          * 
          * @param contentType Only templates that are linked to this type are
          * returned. Assumed not <code>null</code>.
          * 
          * @param force Passed through to the model when cataloging.
          * 
          * @return Never <code>null</code>, may be empty.
          * 
          * @throws PSModelException If any problems communicating w/ server.
          */
         private Collection<IPSReference> getTemplates(
               PSUiReference contentType, boolean force)
            throws PSModelException
         {
            try
            {
               Map<IPSReference, Collection<IPSReference>> associations = 
                  getContentTypeModel().getTemplateAssociations(
                           Collections.singletonList(contentType
                                 .getReference()), force, false);
               Collection<IPSReference> results = associations.get(contentType
                     .getReference());
               
               if (results == null)
                  results = new ArrayList<IPSReference>();
               return results;
            }
            catch (PSLockException e)
            {
               //will never happen because we aren't locking
               throw new RuntimeException("Should never happen.");
            }
         }
      };
   }
   
   /**
    * This class registers itself as a listener on the template model so it can
    * be informed of template object creates. When this happens, if the new
    * template is linked to any tracked content type, this new template is added
    * as a child to the correct folder.
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
            PSCoreFactory.getInstance().getModel(PSObjectTypes.TEMPLATE)
               .addListener(this,
                     PSModelChangedEvent.ModelEvents.CREATED.getFlag());
            
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
       * @param ctype A content type that is in the tree. Assumed not
       * <code>null</code>.
       * 
       * @param templateLinksParent The node that contains the linked 
       * template children. Assumed not <code>null</code>.
       */
      public void setType(IPSReference ctype, PSUiReference templateLinksParent)
      {
         PSPair<IPSReference, PSUiReference> info = getInfo(ctype);
         if (info == null)
         {
            info = new PSPair<IPSReference, PSUiReference>();
            m_nodeInfos.add(info);
         }
         info.setFirst(ctype);
         info.setSecond(templateLinksParent);
      }

      /**
       * Checks if any registered content types match the ctypes associated 
       * with the template in the supplied event and adds a node if one or more
       * are found. 
       * See interface for more details.
       * todo - if ctype links are cloned when a template is cloned, we need to
       * listen to saves as well
       */
      public void modelChanged(PSModelChangedEvent event)
      {
         if (event.getEventType() == PSModelChangedEvent.ModelEvents.CREATED)
         {
            //key is the parent node, value is the set of new children for that node
            Map<PSUiReference, Collection<IPSReference>> newChildren = 
               new HashMap<PSUiReference, Collection<IPSReference>>();
            for (IPSReference newRef : event.getSource())
            {
               try
               {
                  IPSCmsModel templateModel = PSCoreFactory.getInstance()
                        .getModel(PSObjectTypes.TEMPLATE);
                  PSUiAssemblyTemplate createdTemplate = (PSUiAssemblyTemplate) 
                        templateModel.load(newRef, false, false);
                  
                  if (createdTemplate.areNewContentTypesSpecified())
                  {
                     final Set<IPSReference> linkedCTypes =
                           createdTemplate.getNewContentTypes();

                     for (IPSReference ref : linkedCTypes)
                     {
                        PSUiReference parent = getParent(ref); 
                        if (parent == null)
                           continue;
                        Collection<IPSReference> childRefs = newChildren.get(parent);
                        if (childRefs == null)
                        {
                           childRefs = new ArrayList<IPSReference>();
                           newChildren.put(parent, childRefs);
                        }
                        childRefs.add(newRef);
                     }
                  }
               }
               catch (PSModelException e)
               {
                  // should never happen, getting from cache
                  throw new RuntimeException(e);
               }
               catch (Exception e)
               {
                  // should never happen, getting from cache
                  throw new RuntimeException(e);
               }
            }
            for (PSUiReference parent : newChildren.keySet())
            {
               Collection<IPSReference> refs = newChildren.get(parent);
               PSDesignObjectHierarchy.getInstance().addChildren(parent,
                     refs.toArray(new IPSReference[refs.size()]), false, false);
            }
         }
         else if (event.getEventType() 
                  == PSModelChangedEvent.ModelEvents.LINKS_ADDED
               || event.getEventType() 
                  == PSModelChangedEvent.ModelEvents.LINKS_DELETED)
         {
            PSUiReference parentNode = getParent(event.getLinkOwner());
            if (null != parentNode)
            {
               if (event.getEventType() == ModelEvents.LINKS_ADDED)
               {
                  PSDesignObjectHierarchy.getInstance().addChildren(parentNode,
                     event.getSource(), false, false);
               }
               else if (event.getEventType() == ModelEvents.LINKS_DELETED)
               {
                  for (IPSReference templateRef : event.getSource())
                  {
                     Collection<PSUiReference> possibles = 
                        PSDesignObjectHierarchy.getInstance().getNodes(templateRef);
                     Collection<PSUiReference> validNodes = 
                        new ArrayList<PSUiReference>(); 
                     for (PSUiReference node : possibles)
                     {
                        if (node.getParentNode().equals(parentNode))
                        {
                           validNodes.add(node);
                        }
                     }
                     PSDesignObjectHierarchy.getInstance().deleteChildren(
                           validNodes);
                  }
               }
            }
         }
         else
            //wrong event type
            assert(false);
      }
      
      /**
       * Scans the {@link #m_nodeInfos} set for a matching content type,
       * returning the associated node if found.
       * 
       * @param ctype The id of a content type. Assumed not <code>null</code>.
       * 
       * @return A valid node that is the parent node for linked templates, if
       * the supplied content type has been registered. Otherwise,
       * <code>null</code> is returned.
       */
      private PSUiReference getParent(IPSReference ctype)
      {
         PSPair<IPSReference, PSUiReference> info = getInfo(ctype);
         return info == null ? null : info.getSecond();
      }
      
      /**
       * Walks the local list of pairs, looking for one whose type matches the
       * supplied type.
       * 
       * @param ctype Assumed never <code>null</code>.
       * 
       * @return The matching pair, or <code>null</code> if not found.
       */
      private PSPair<IPSReference, PSUiReference> getInfo(IPSReference ctype)
      {
         PSPair<IPSReference, PSUiReference> result = null;
         for (PSPair<IPSReference, PSUiReference> info : m_nodeInfos)
         {
            if (!info.getFirst().equals(ctype))
               continue;
            result = info;
            break;
         }
         return result;
      }
      
      /**
       * First is the content type ref. Second is the parent node containing the
       * template links.
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
   private static NodeUpdater getUpdater(PSContentTypeExpansionFactory factory)
   {
      if (ms_nodeUpdater == null)
         ms_nodeUpdater = factory.new NodeUpdater();
      return ms_nodeUpdater;
   }

   /**
    * This guy tracks newly created templates and adds children to the
    * appropriate 'Allowed Templates...' node. Lazily initialized via the
    * {@link #getUpdater(PSContentTypeExpansionFactory)} method, then never
    * changed.
    */
   private static NodeUpdater ms_nodeUpdater;
   
   /**
    * The name of the property that controls whether we return workflow names,
    * linked templates, or children.
    */
   private static String EXPANSION_TYPE_PROPNAME = "expansionType";
   
   /**
    * One of the values for the {@link #EXPANSION_TYPE_PROPNAME} property that 
    * specifies to return workflow names.
    */
   private static String ET_WORKFLOW = "workflow";
   
   /**
    * One of the values for the {@link #EXPANSION_TYPE_PROPNAME} property that 
    * specifies to return linked templates of template type shared.
    */
   private static String ET_LINKED_SHARED_TEMPLATES = "linkedSharedTemplates";
           
   
   /**
    * One of the values for the {@link #EXPANSION_TYPE_PROPNAME} property that 
    * specifies to return linked templates of template type local.
    */
   private static String ET_LINKED_LOCAL_TEMPLATES = "linkedLocalTemplates";
   
   /**
    * One of the values for the {@link #EXPANSION_TYPE_PROPNAME} property that 
    * specifies to return linked templates of template type variant.
    */
   private static String ET_LINKED_VARIANT_TEMPLATES = "linkedXslVariants";
}
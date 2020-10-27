/******************************************************************************
 *
 * [ PSWorkflowExpansionFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefinitionException;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a cataloger that retrieves all content types that allow the ancestor
 * workflow as an allowed workflow.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSWorkflowExpansionFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps No properties are supported.
    */
   public PSWorkflowExpansionFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
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
            PSUiReference wfNode = 
               getAncestorNode(PSObjectTypes.WORKFLOW);
            IPSReference wfRef = wfNode.getReference();
            
            if (wfRef == null)
            {
               String[] args = { PSObjectTypes.WORKFLOW.name(),
                     getParent().getPath() };
               Exception e = new PSHierarchyDefinitionException(PSMessages
                     .getString("common.error.badmodel", (Object[]) args));
               throw new PSModelException(e);
            }
            
            final Collection<IPSReference> refs;
            try
            {
               refs = getAllowedContentTypes(wfRef, force);
               getUpdater(PSWorkflowExpansionFactory.this).setType(wfRef,
                     getParent());
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
          * Finds all allowed content types names for the given workflow handle.
          * 
          * @param wfRef A handle to the parent workflow. Assumed not
          * <code>null</code>.
          * @return Never <code>null</code>, may be empty.
          * 
          * @throws PSModelException If any problems communicating w/ server.
          */
         private Collection<IPSReference> getAllowedContentTypes(
               IPSReference wfRef, boolean force)
            throws PSModelException
         {
            IPSContentTypeModel ctypeModel = 
               (IPSContentTypeModel) getModel(PSObjectTypes.CONTENT_TYPE);
            Map<IPSReference, Collection<IPSReference>> links = ctypeModel
                  .getWorkflowAssociations(Collections.singletonList(wfRef),
                        force);
            Collection<IPSReference> results = links.get(wfRef);
            if (results == null)
               results = Collections.emptySet();
            return results;
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
            PSCoreFactory.getInstance().getModel(PSObjectTypes.CONTENT_TYPE)
               .addListener(this,
                     PSModelChangedEvent.ModelEvents.CREATED.getFlag()
                     | PSModelChangedEvent.ModelEvents.MODIFIED.getFlag());
         }
         catch (PSModelException e)
         {
            //should never happen
            throw new RuntimeException(e);
         }
      }
      
      /**
       * Add a new workflow to track. If it is already being tracked, the
       * supplied parent replaces the previously registered one. Any tracked
       * type will automatically be updated if a new content type is creasted
       * that links to that workflow.
       * 
       * @param workflowRef A workflow that is in the tree. Assumed not
       * <code>null</code>.
       * 
       * @param ctypeLinksParent The node that contains the linked content type
       * children. Assumed not <code>null</code>.
       */
      public void setType(IPSReference workflowRef,
            PSUiReference ctypeLinksParent)
      {
         PSPair<IPSReference, PSUiReference> info = getInfo(workflowRef);
         if (info == null)
         {
            info = new PSPair<IPSReference, PSUiReference>();
            m_nodeInfos.add(info);
         }
         info.setFirst(workflowRef);
         info.setSecond(ctypeLinksParent);
      }

      /**
       * Checks if any registered workflow matches the workflows associated 
       * with the modified ctype in the supplied event and adds/removes nodes
       * as appropriate.
       * See interface for more details.
       * todo - if ctype links are cloned when a template is cloned, we need to
       * listen to saves as well
       */
      public void modelChanged(final PSModelChangedEvent event)
      {
         assert (event.getEventType() == PSModelChangedEvent.ModelEvents.CREATED
               || event.getEventType() 
                  == PSModelChangedEvent.ModelEvents.MODIFIED);
  
         //fixme
         PSUiUtils.asyncExecWithBusy(new Runnable()
         {
            public void run()
            {
               //key is the parent node, value is the set of new children for 
               // that node
               Map<PSUiReference, Collection<IPSReference>> addChildren = 
                  new HashMap<PSUiReference, Collection<IPSReference>>();
               Collection<PSUiReference> removeChildren = 
                  new ArrayList<PSUiReference>();
               Collection<IPSReference> ctypes = 
                  Arrays.asList(event.getSource());
               for (IPSReference ctypeRef : event.getSource())
               {
                  try
                  {
                     IPSContentTypeModel ctypeModel = (IPSContentTypeModel) 
                        PSCoreFactory.getInstance().getModel(
                              PSObjectTypes.CONTENT_TYPE);
                     
                     Collection<IPSReference> workflows = 
                        ctypeModel.getWorkflowAssociations(ctypeRef, false);
                     
                     for (IPSReference wfRef : workflows)
                     {
                        PSUiReference parentNode = getParent(wfRef);
                        if (null == parentNode || !parentNode.isCataloged())
                           continue;
                        
                        List<PSUiReference> children = 
                           PSDesignObjectHierarchy.getInstance().getChildren(
                                 parentNode, true);
                        Collection<IPSReference> toAdd = addChildren.get(wfRef);
                        if (toAdd == null)
                        {
                           toAdd = new ArrayList<IPSReference>();
                           addChildren.put(parentNode, toAdd);
                        }
                        Collection<IPSReference> existingChildRefs = 
                           new ArrayList<IPSReference>();
                        for (PSUiReference child : children)
                        {
                           existingChildRefs.add(child.getReference());
                        }
                        toAdd.addAll(ctypes);
                        toAdd.removeAll(existingChildRefs);
                     }
      
                     for (PSPair<IPSReference, PSUiReference> entry 
                           : m_nodeInfos)
                     {
                        if (workflows.contains(entry.getFirst()))
                           continue;
                        
                        List<PSUiReference> children = 
                           PSDesignObjectHierarchy.getInstance().getChildren(
                                 entry.getSecond(), true);
      
                        PSUiReference toRemove = null;
                        for (PSUiReference node : children)
                        {
                           if (ctypes.contains(node.getReference()))
                           {
                              toRemove = node;
                              break;
                           }
                        }
                        if (toRemove != null)
                           removeChildren.add(toRemove);
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
               for (PSUiReference parent : addChildren.keySet())
               {
                  Collection<IPSReference> refs = addChildren.get(parent);
                  PSDesignObjectHierarchy.getInstance().addChildren(parent,
                        refs.toArray(new IPSReference[refs.size()]), false,
                        false);
               }
               PSDesignObjectHierarchy.getInstance().deleteChildren(
                     removeChildren);
            }
         });
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
       * First is the workflow ref. Second is the parent node containing the
       * content type links.
       * 
       * DONT use map - key may change
       */
      private List<PSPair<IPSReference, PSUiReference>> m_nodeInfos = 
         new ArrayList<PSPair<IPSReference, PSUiReference>>(); 
   }

   /**
    * Gets the single instance of the ctype creation tracker. The instance
    * is created the first time the method is called.
    * 
    * @return Never <code>null</code>.
    */
   private static NodeUpdater getUpdater(PSWorkflowExpansionFactory factory)
   {
      if (ms_nodeUpdater == null)
         ms_nodeUpdater = factory.new NodeUpdater();
      return ms_nodeUpdater;
   }

   /**
    * This guy tracks newly created templates and adds children to the
    * appropriate 'Allowed Templates...' node. Lazily initialized via the
    * {@link #getUpdater(PSWorkflowExpansionFactory)} method, then never
    * changed.
    */
   private static NodeUpdater ms_nodeUpdater;
}

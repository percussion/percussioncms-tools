/******************************************************************************
 *
 * [ PSCommunityVisibilityCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.security;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

import java.util.Collection;
import java.util.List;

/**
 * Creates a cataloger that can retrieve all communities. It also listens for
 * create changes in the model and updates accordingly. All entries it creates
 * are references. 
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSCommunityVisibilityCatalogFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps No properties are supported.
    */
   public PSCommunityVisibilityCatalogFactory(
         InheritedProperties contextProps, PSHierarchyDefProcessor proc,
         Catalog type)
   {
      super(contextProps, proc, type);
   }

   //see inteface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      getUpdater(this).setType(parent);
      return new BaseCataloger(parent)
      {
         // see interface for details
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            IPSCmsModel commModel = getModel(PSObjectTypes.COMMUNITY);
            Collection<IPSReference> refs = commModel.catalog(force);
            return PSDesignObjectHierarchy.getInstance().addChildren(
                  getParent(), refs.toArray(new IPSReference[refs.size()]),
                  false, false);

         }
      };
   }
   
   /**
    * This class registers itself as a listener on the community model so it can
    * be informed of community creations. When this happens, a wrapper node is
    * created and added to the folder that had been previously set.
    * <p>
    * The parent folder is set using the {@link #setType(PSUiReference)} method.
    * 
    * @author paulhoward
    */
   private class NodeUpdater implements IPSModelListener
   {
      /**
       * Only ctor. Registers itself w/ the community model.
       */
      public NodeUpdater()
      {
         try
         {
            PSCoreFactory.getInstance().getModel(PSObjectTypes.COMMUNITY)
                  .addListener(this,
                     PSModelChangedEvent.ModelEvents.CREATED.getFlag());
         }
         catch (PSModelException e)
         {
            //should never happen
            throw new RuntimeException(e);
         }
      }
      
      /**
       * Set the folder that will be the parent of any newly created community.
       * The supplied parent replaces the previously registered one.
       * 
       * @param parent The folder to receive newly created communities.
       * <code>null</code> is allowed.
       */
      public void setType(PSUiReference parent)
      {
         m_parent = parent;
      }

      /**
       * Updates the design object model if any ctype/template links are added
       * or deleted. See interface for more details.
       */
      public void modelChanged(PSModelChangedEvent event)
      {
         assert(event.getEventType() == PSModelChangedEvent.ModelEvents.CREATED);
         PSDesignObjectHierarchy.getInstance()
               .addChildren(m_parent, event.getSource(), false, false);
      }
      
      /**
       * The folder that will receive any newly created community nodes.
       * Modified by the {@link #setType(PSUiReference)} method.
       */
      private PSUiReference m_parent; 
   }

   /**
    * Gets the single instance of the template creation tracker. The instance
    * is created the first time the method is called.
    * 
    * @return Never <code>null</code>.
    */
   private static NodeUpdater getUpdater(
         PSCommunityVisibilityCatalogFactory factory)
   {
      if (ms_nodeUpdater == null)
         ms_nodeUpdater = factory.new NodeUpdater();
      return ms_nodeUpdater;
   }

   /**
    * This guy tracks newly created communities and adds children to the
    * appropriate 'Communities' node. Lazily initialized via the
    * {@link #getUpdater(PSCommunityVisibilityCatalogFactory)} method, then
    * never changed.
    */
   private static NodeUpdater ms_nodeUpdater;
}

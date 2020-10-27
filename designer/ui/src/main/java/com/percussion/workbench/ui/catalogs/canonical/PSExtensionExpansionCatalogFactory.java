/*******************************************************************************
 *
 * [ PSExtensionExpansionCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSModelListener;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

/**
 * Adds functionality for object tracking.
 *
 * @author Andriy Palamarchuk
 */
public class PSExtensionExpansionCatalogFactory
      extends PSExtensionCatalogFactory
{
   /**
    * See {@link PSExtensionCatalogFactory#PSExtensionCatalogFactory(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    */
   public PSExtensionExpansionCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
   }
   
   @Override
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      ms_nodeUpdater.setType(parent);
      return super.createCatalog(parent);
   }

   /**
    * This class registers itself as a listener on the extension model so it can
    * be informed of extension creations. When this happens, a wrapper node is
    * created and added to the folder that had been previously set.
    * <p>
    * The parent folder is set using the {@link #setType(PSUiReference)} method.
    * 
    * @author Andriy Palamarchuk
    */
   private static class NodeUpdater implements IPSModelListener
   {
      /**
       * Only ctor. Registers itself w/ the extension model.
       */
      public NodeUpdater()
      {
         try
         {
            PSCoreFactory.getInstance().getModel(PSObjectTypes.EXTENSION)
                  .addListener(this, ModelEvents.CREATED.getFlag());
         }
         catch (PSModelException e)
         {
            //should never happen
            throw new RuntimeException(e);
         }
      }

      /**
       * Set the folder that will be the parent of any newly created extension.
       * The supplied parent replaces the previously registered one.
       * 
       * @param parent The folder to receive newly created extensions.
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
         assert event.getEventType().equals(ModelEvents.CREATED);
         PSDesignObjectHierarchy.getInstance()
               .addChildren(m_parent, event.getSource(), false, true);
      }
      
      /**
       * The folder that will receive any newly created extension nodes.
       * Modified by the {@link #setType(PSUiReference)} method.
       */
      private PSUiReference m_parent; 
   }

   /**
    * This guy tracks newly created extensions and adds children to the
    * appropriate 'Extensions' node.
    */
   private static final NodeUpdater ms_nodeUpdater = new NodeUpdater();
}
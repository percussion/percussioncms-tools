/******************************************************************************
 *
 * [ PSCommunityVisibilityView.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.models.IPSModelListener;
import com.percussion.workbench.ui.views.hierarchy.PSDeclarativeExplorerView;
import com.percussion.workbench.ui.views.hierarchy.PSDeclarativeHierarchyContentProvider;
import org.eclipse.swt.widgets.Display;

import java.text.MessageFormat;

/**
 * Provides a container for the community_viewHierarchyDef.xml definition. Its
 * purpose is to show the communities with their object visibility. It provides
 * a way to view and edit multiple ACLs simultaneously.
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:43:45 PM
 */
public class PSCommunityVisibilityView extends PSDeclarativeExplorerView
{
   /**
    * The view id as specified in the plugin.xml.
    */
   public static final String ID = 
      "com.percussion.workbench.ui.views.PSCommunityVisibilityView";
   
   @Override
   protected String getRootName()
   {
      return "community";
   }

   /**
    * The only ctor. Adds a listener to the core factory for acl save events
    * so the view can be kept up to date.
    */
   public PSCommunityVisibilityView()
   {
      m_provider = new PSCommunityVisibilityContentProvider();
      PSCoreFactory.getInstance().addListener(m_provider, 
            PSModelChangedEvent.ModelEvents.ACL_SAVED.getFlag());
   }
   
   /**
    * Adds a listener for ACL changes, which is the main concern of this view.
    *
    * @author paulhoward
    */
   private class PSCommunityVisibilityContentProvider extends
         PSDeclarativeHierarchyContentProvider implements IPSModelListener
   {
      /**
       * Only ctor.
       */
      public PSCommunityVisibilityContentProvider()
      {
         super(getRootName());
      }

      /**
       * From interface. Refreshes the entire tree to pick up changes in
       * multiple nodes.
       */
      public void modelChanged(PSModelChangedEvent event)
      {
         assert (event.getEventType() 
               == PSModelChangedEvent.ModelEvents.ACL_SAVED);
         
         if (isIgnoreChangeEvents())
            return;
         final Display display = Display.getDefault();
         if (getLogger().isDebugEnabled())
         {
            String message = 
               "Change occurred for community visibility view: {0} for {1}"; 
            StringBuffer nameBuf = new StringBuffer();
            for (IPSReference ref : event.getSource())
               nameBuf.append(ref.getName() + ", ");
            getLogger().debug(MessageFormat.format(message, 
                  event.getEventType().name(), nameBuf.toString()));
         }
         
         display.asyncExec(new Runnable()
         {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
               //todo - may need to be smarter about what is refreshed
               getViewer().refresh();   
            }
         });            
      }
   }

   //see base class method for details
   @Override
   protected PSDeclarativeHierarchyContentProvider getContentProvider()
   {
      return m_provider;
   }
   
   /**
    * Created in ctor, then never <code>null</code> or modified. This is the
    * provided for this class, provided to the base class when needed.
    */
   private PSCommunityVisibilityContentProvider m_provider;
}

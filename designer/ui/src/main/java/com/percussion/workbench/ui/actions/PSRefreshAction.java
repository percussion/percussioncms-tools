/******************************************************************************
 *
 * [ PSRefreshAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Since the data that forms the underlying models can be modified by clients
 * other than ourselves, a refresh mechanism is necessary. The declarative
 * hierarchy model is obtained and refresh is called on it.
 * <p>
 * The refresh action will be available as long as 1 or more
 * {@link com.percussion.workbench.ui.PSUiReference} objects are selected and
 * only that type is in the selection set.
 */
class PSRefreshAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".RefreshAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSRefreshAction(ISelectionProvider provider)
   {
      super(PSMessages.getString("PSRefreshAction.action.label"), provider);
      setToolTipText(PSMessages.getString("PSRefreshAction.action.tooltip"));
      setId(PSRefreshAction.ID);
   }

   /**
    * For every {@link PSUiReference} object in the selection, the
    * <code>refresh</code> method is called on the design object model.
    */
   public void run()
   {
      if (!isEnabled())
         return;
      List sel = getStructuredSelection().toList();
      Collection<PSUiReference> toRefresh = new ArrayList<PSUiReference>();
      for (Object o : sel)
      {
         if (!(o instanceof PSUiReference))
            continue;
         PSUiReference node = (PSUiReference) o;
         if (!sel.contains(node.getParentNode()))
            toRefresh.add(node);
      }
      for (PSUiReference node : toRefresh)
      {
         PSDesignObjectHierarchy.getInstance().refresh(node);
      }
   }

   /**
    * Checks that every element in the selection is a {@link PSUiReference}.
    * 
    * @return <code>true</code> If there is at least 1 element and the 
    * aforementioned check is valided, otherwise <code>false</code>.
    */
   protected boolean updateSelection(IStructuredSelection selection)
   {
      if (!super.updateSelection(selection))
         return false;

      if (selection.size() == 0)
         return false;

      for (Object o : selection.toList())
      {
         if (!(o instanceof PSUiReference))
         {
            return false;
         }
      }
      
      return true;
   }
}

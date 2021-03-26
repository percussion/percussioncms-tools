/******************************************************************************
 *
 * [ PSDeleteAction.java ]
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
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Removes deletable nodes from the tree by delegating to the node's delete
 * handler. If no delete handler is present, a delete is not allowed.
 *
 * @author paulhoward
 */
public class PSDeleteAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".DeleteAction"; //$NON-NLS-1$

   /**
    * Create the action.
    * 
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSDeleteAction(ISelectionProvider provider)
   {
      super(PSMessages.getString("PSDeleteAction.action.label"), provider);
      setToolTipText(PSMessages.getString("PSDeleteAction.action.tooltip"));
      //for global action support
      setActionDefinitionId("org.eclipse.ui.edit.delete");
      setId(PSDeleteAction.ID);
   }

   @Override
   protected boolean updateSelection(IStructuredSelection selection)
   {
      if (!super.updateSelection(selection))
         return false;

      if (selection.size() == 0)
         return false;
      
      //all elements must be design objects and have delete handlers
      for (Object o : selection.toArray())
      {
         if (!(o instanceof PSUiReference))
         {
            return false;
         }
         PSUiReference node = (PSUiReference) o;
         IPSDeclarativeNodeHandler handler = node.getHandler();
         if (handler != null)
         {
            return handler.supportsDelete(node) && 
               node.getReference() != null && !node.getReference().isLocked();
         }
      }
      
      return false;
   }

   @Override
   public void run()
   {
      if (!isEnabled())
         return;

      IStructuredSelection ssel = getStructuredSelection();
      List objects = ssel.toList();
      if (objects.size() == 0)
         return;
      
      final Map<IPSDeclarativeNodeHandler, Collection<PSUiReference>> toDelete = 
         new HashMap<IPSDeclarativeNodeHandler, Collection<PSUiReference>>();
      boolean instanceObject = false;
      for (Object o : objects)
      {
         if (!(o instanceof PSUiReference))
         {
            //shouldn't happen
            PSWorkbenchPlugin.getDefault().log(
                  "Skipping non-PSUiReference during delete: "
                        + o.getClass().getName());
            continue;
         }
         PSUiReference node = (PSUiReference) o;
         IPSDeclarativeNodeHandler handler = node.getHandler();
         if (handler != null)
         {
            Collection<PSUiReference> nodes = toDelete.get(handler);
            if (nodes == null)
            {
               nodes = new ArrayList<PSUiReference>();
               toDelete.put(handler, nodes);
            }
            nodes.add(node);
            if (!node.isReference())
               instanceObject = true;
         }
         else
         {
            //shouldn't happen
            PSWorkbenchPlugin.getDefault().log(
                  "Skipping node that doesn't have delete handler: "
                        + node.getDisplayLabel());
         }
      }
      if (instanceObject)
      {
         //warn the user
         final String title = 
            PSMessages.getString("PSDeleteAction.confirmDelete.title");
         final String msg = objects.size() == 1
               ? PSMessages.getString("PSDeleteAction.confirmDelete1.message")
               : PSMessages.getString("PSDeleteAction.confirmDeleteN.message");   
         if (!MessageDialog.openConfirm(PSUiUtils.getShell(), title, msg))
            return;
      }
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
      {
         public void run()
         {
            for (Map.Entry<IPSDeclarativeNodeHandler, Collection<PSUiReference>> 
               entry : toDelete.entrySet())
            {
               entry.getKey().handleDelete(entry.getValue());
            }
         }
      });
   }
}

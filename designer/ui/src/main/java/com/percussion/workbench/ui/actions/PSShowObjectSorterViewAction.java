/******************************************************************************
*
* [ PSShowObjectSorterViewAction.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.views.PSObjectSorterView;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;

import java.util.ArrayList;
import java.util.List;

public class PSShowObjectSorterViewAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId() + 
      ".ShowObjectSorterView"; //$NON-NLS-1$

   public PSShowObjectSorterViewAction(ISelectionProvider provider)
   {
      super(PSMessages.getString(
            "PSShowObjectSorterViewAction.showObjectSorter.label"), provider); //$NON-NLS-1$
      setToolTipText(PSMessages.getString(
            "PSShowObjectSorterViewAction.showInView.label")); //$NON-NLS-1$
      setId(ID);      
   }

   /* 
    * @see org.eclipse.ui.actions.BaseSelectionListenerAction#
    * updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
    */
   @Override
   protected boolean updateSelection(IStructuredSelection sel)
   {
      // as documented by super class
      if (!super.updateSelection(sel))
         return false;
      
      if (sel.size() == 0)
         return false;
      
      boolean isOk = false;
      for (Object o : sel.toArray())
      {
         if (o instanceof PSUiReference)
         {
            PSObjectType type = ((PSUiReference)o).getObjectType();
            if(type != null && type.getPrimaryType() 
               == PSObjectTypes.LOCAL_FILE)
               return false;
            isOk = true;
         }         
      }
      return isOk;
   }

   /* 
    * @see org.eclipse.jface.action.Action#run()
    */
   @Override
   public void run()
   {
      IStructuredSelection ssel = getStructuredSelection();
      List objects = ssel.toList();
      if (objects.size() == 0)
         return;
      
      // Get list of all valid selections
      List<PSUiReference> refs = new ArrayList<PSUiReference>();
      for(Object obj : objects)
      {
         if(obj instanceof PSUiReference)
            refs.add((PSUiReference)obj);
      }
      // open view
      try
      {
         PSUiUtils.openView(PSObjectSorterView.ID);
         IViewPart view = PSUiUtils.findView(PSObjectSorterView.ID);
         ((PSObjectSorterView)view).load(refs);
         
      }
      catch (ExecutionException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
     
      
   }
   
  

   
   

}

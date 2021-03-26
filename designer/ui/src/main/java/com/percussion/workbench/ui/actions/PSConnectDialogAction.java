/******************************************************************************
 *
 * [ PSConnectDialogAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author RammohanVangapalli
 */
public class PSConnectDialogAction extends PSAction implements
   IWorkbenchWindowActionDelegate
{
   /**
    * Launches the connection editing dialog, then logs on if a valid connection
    * is returned.
    */
   @Override
   public void run()
   {
      if (!PSDisconnectAction.checkCanDisconnect())
         return;
      
      PSConnectionInfo lastConn = PSCoreFactory.getInstance()
         .getConnectionInfo();
      PSWorkbenchPlugin plugin = PSWorkbenchPlugin.getDefault();
      if (plugin == null)
      {
         // This should never happen
         throw new RuntimeException(
            "Workbench plugin must be initialized to execute this action");
      }
      PSWorkbenchPlugin.getDefault().maybeLogon(
            plugin.correctConnection(lastConn, null, null));
   } 

   /**
    * No op.
    */
   public void dispose()
   {}

   /**
    * No op.
    */
   public void init(@SuppressWarnings("unused") IWorkbenchWindow window)
   {}

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
    */
   public void run(@SuppressWarnings("unused") IAction action)
   {
      run();
   }

   /**
    * No op.
    */
   @SuppressWarnings("unused")
   public void selectionChanged(IAction action, ISelection selection)
   {}
}

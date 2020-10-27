/******************************************************************************
 *
 * [ PSDisconnectAction.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import java.util.Collection;

/**
 * Calls the core plugin to initiate the server logoff sequence.
 *
 * @author paulhoward
 */
public class PSDisconnectAction extends PSAction implements
   IWorkbenchWindowActionDelegate
{
   /**
    * Calls the core factory to logoff after validating that it is safe to do
    * so. It is safe if no objects are currently locked for editing.
    */
   @Override
   public void run()
   {
      if (checkCanDisconnect())
         PSCoreFactory.getInstance().logoff();
   }
   
   /**
    * Checks to see if the current state allows the user to disconnect, and if
    * not, displays a message indicating the reason.  
    * 
    * @return <code>true</code> if the user can disconnect, <code>false</code>
    * if not.
    */
   static boolean checkCanDisconnect()
   {
      Collection<IPSReference> lockedRefs = 
         PSCoreFactory.getInstance().getLockedRefs();
      if (lockedRefs.size() > 0)
      {
         String title = PSMessages.getString(
         "PSDisconnectAction.warning.editorsStillOpen.title");
         String msg = PSMessages.getString(
         "PSDisconnectAction.warning.editorsStillOpen.message");
         MessageDialog.openInformation(PSUiUtils.getShell(), title, msg);
         
         return false;
      }
      
      return true;
   }
   
   //see base class method for details
   @Override
   public boolean isEnabled()
   {
      // fixme Auto-generated method stub
      return super.isEnabled();
   }

   /**
    * No op.
    */
   public void dispose()
   {}

   /**
    * No op.
    */
   public void init(
         @SuppressWarnings("unused") IWorkbenchWindow window)
   {}

   /**
    * Not implemented by this class. Does nothing.
    */
   public void selectionChanged(
         @SuppressWarnings("unused") IAction action, 
         @SuppressWarnings("unused") ISelection selection)
   {
      //We don't care about selection
   }
   
   /**
    * Calls the run() method.
    */
   public void run(@SuppressWarnings("unused") IAction action)
   {
      run();
   }
}

/******************************************************************************
 *
 * [ PSReleaseLockAction.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.model.PSFileEditorTracker;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * This action allows the user to release the lock on a file-like object that
 * is being edited in an external editor w/o saving it.
 *
 * @author paulhoward
 */
public class PSReleaseLockAction extends PSExternalEditingAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".ReleaseLockAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSReleaseLockAction(ISelectionProvider provider)
   {
      super(PSMessages.getString("PSReleaseLockAction.action.label"), provider);
      setToolTipText(PSMessages
            .getString("PSReleaseLockAction.action.tooltip"));
      setId(ID);
            
   }

   protected void processRef(IPSReference ref)
      throws Exception
   {
      PSFileEditorTracker.getInstance().unregister(ref);
   }

   //see base class method for details
   @Override
   protected boolean queryProceed()
   {
      // fixme Auto-generated method stub
      String title = PSMessages
            .getString("PSReleaseLockAction.warning.aboutToLoseData.title");
      String msg = PSMessages
            .getString("PSReleaseLockAction.warning.aboutToLoseData.message");
      return MessageDialog.openQuestion(PSUiUtils.getShell(), title, msg);
   }

}

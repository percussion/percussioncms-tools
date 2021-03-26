/******************************************************************************
 *
 * [ PSFinishedEditingAction.java ]
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
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * This action allows the user to save the object and release the lock on a
 * file-like object that is being edited in an external editor.
 * 
 * @author paulhoward
 */
public class PSFinishedEditingAction extends PSExternalEditingAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".FinishedEditingAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    * @param provider Supplied to base class ctor.
    */
   public PSFinishedEditingAction(ISelectionProvider provider)
   {
      super(PSMessages.getString("PSFinishedEditingAction.action.label"),
            provider);
      setToolTipText(PSMessages
            .getString("PSFinishedEditingAction.action.tooltip"));
      setId(ID);           
   }

   //see base class
   protected void processRef(IPSReference ref)
      throws Exception
   {
      PSFileEditorTracker.getInstance().save(ref, true);
   }
}

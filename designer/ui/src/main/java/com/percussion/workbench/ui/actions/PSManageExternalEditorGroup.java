/******************************************************************************
 *
 * [ PSManageExternalEditorGroup.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;

/**
 * This group contains the actions associated with managing content that is 
 * being edited in an external editor.
 *
 * @author paulhoward
 */
public class PSManageExternalEditorGroup extends ActionGroup
{
   /**
    * Create the group.
    * 
    * @param provider The source of the selections for actions managed by this
    * group. Never <code>null</code>.
    */
   public PSManageExternalEditorGroup(ISelectionProvider provider)
   {
      if (null == provider)
      {
         throw new IllegalArgumentException("provider cannot be null");  
      }
      m_finishedEditingAction = new PSFinishedEditingAction(provider);
      m_releaseLockAction = new PSReleaseLockAction(provider);
   }

   /*
    * (non-Javadoc) Method declared in ActionGroup
    */
   public void fillActionBars(IActionBars actionBar)
   {
      super.fillActionBars(actionBar);
//      setGlobalActionHandlers(actionBar);
   }

   //see base class
   public void fillContextMenu(IMenuManager menu)
   {
      super.fillContextMenu(menu);
      IStructuredSelection selection = (IStructuredSelection) getContext()
         .getSelection();
      m_finishedEditingAction.selectionChanged(selection);
      m_releaseLockAction.selectionChanged(selection);
      menu.add(m_finishedEditingAction);
      menu.add(m_releaseLockAction);
   }

   /**
    * Allows user to create new folders in declarative views. Never
    * <code>null</code> after ctor.
    */
   private PSExternalEditingAction m_finishedEditingAction;
   
   /**
    * Allows user to release the lock on a file being edited external to Eclipse
    * w/o saving it. Never <code>null</code> after ctor.
    */
   private PSExternalEditingAction m_releaseLockAction;
}

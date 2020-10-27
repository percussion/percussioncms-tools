/******************************************************************************
*
* [ PSEditorActionBase.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public abstract class PSEditorActionBase implements IEditorActionDelegate
{
   /**
    * Holds the current active editor
    */
   protected IEditorPart m_activeEditor = null;
   
   public void setActiveEditor(IAction action, IEditorPart targetEditor)
   {
      m_activeEditor = targetEditor;
   }
   
   public void selectionChanged(IAction action, ISelection selection)
   {
      // Ignore
   }
}

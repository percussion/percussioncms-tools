/******************************************************************************
 *
 * [ PSXmlApplicationActionGroup.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.editors.form.PSXmlAppDebuggingAction;
import com.percussion.workbench.ui.editors.form.PSXmlAppExportAction;
import com.percussion.workbench.ui.editors.form.PSXmlAppImportAction;
import com.percussion.workbench.ui.editors.form.PSXmlAppStatusAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Adds XML application-specific actions
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationActionGroup extends ActionGroup
{
   /**
    * Creates a new <code>PSXmlApplicationActionGroup</code>.
    * The group requires that the
    * selection provided by the part's selection provider is of type <code>
    * org.eclipse.jface.viewers.IStructuredSelection</code>.
    * 
    * @param site the site that owns this action group
    */
   public PSXmlApplicationActionGroup(IWorkbenchSite site)
   {
      m_statusAction = new PSXmlAppStatusAction(site);
      m_exportAction = new PSXmlAppExportAction(site);
      m_importAction = new PSXmlAppImportAction(site);
      m_debuggingAction = new PSXmlAppDebuggingAction(site);

      final ISelection selection = site.getSelectionProvider().getSelection();
      if (selection instanceof IStructuredSelection)
      {
         m_exportAction.selectionChanged((IStructuredSelection) selection);
         m_importAction.selectionChanged((IStructuredSelection) selection);
         m_debuggingAction.selectionChanged((IStructuredSelection) selection);
      }
   }
   
   @Override
   public void fillContextMenu(IMenuManager menu)
   {
      final IPSReference selectedRef = getSelectedRef();
      if (selectedRef == null)
      {
         return;
      }

      final IStructuredSelection selection =
         (IStructuredSelection) getContext().getSelection();
      
      m_statusAction.selectionChanged(selection);
      menu.add(m_statusAction);
      
      m_exportAction.selectionChanged(selection);
      menu.add(m_exportAction);
      
      m_importAction.selectionChanged(selection);
      menu.add(m_importAction);
      
      m_debuggingAction.selectionChanged(selection);
      menu.add(m_debuggingAction);
   }

   /**
    * Reference selected in the tree.
    * @return <code>null</code> if no app reference is selected.
    */
   private IPSReference getSelectedRef()
   {
      if (!(getContext().getSelection() instanceof IStructuredSelection))
      {
         return null;
      }
      final IStructuredSelection selection =
         (IStructuredSelection) getContext().getSelection();
      if (selection.size() != 1)
      {
         return null;
      }

      final Object o = selection.getFirstElement();
      if (!(o instanceof PSUiReference))
      {
         return null;
      }
      
      final PSUiReference node = (PSUiReference) o;
      if (node.getReference() == null || node.isFolder())
      {
         return null;
      }
      
      if (!(node.getObjectType().getPrimaryType().equals(
            PSObjectTypes.XML_APPLICATION)))
      {
         return null;
      }
      return node.getReference();
   }

   /**
    * Shows Tracing Debugging dialog.
    */
   private final PSXmlAppDebuggingAction m_debuggingAction;
   
   /**
    * Imports application
    */
   private final PSXmlAppImportAction m_importAction;
   
   /**
    * Application export action.
    */
   private PSXmlAppExportAction m_exportAction;

   /**
    * Enables/disables the application.
    */
   private PSXmlAppStatusAction m_statusAction;
}

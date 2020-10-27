/******************************************************************************
 *
 * [ PSRefactorActionGroup.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSUiReference;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;

import java.util.Iterator;

/**
 * This is the action group for refactor actions,
 * including global action handlers for copy, paste and delete.
 * 
 * @since 2.0
 */
public class PSRefactorActionGroup extends ActionGroup
{
   /**
    * The only ctor.
    * 
    * @param site Never <code>null</code>. Used to get a shell to use as the
    * parent of dialogs shown to the user.
    * 
    * @param viewer Never <code>null</code>. Used by the rename action for
    * inline editing.
    */
   public PSRefactorActionGroup(IViewSite site, StructuredViewer viewer)
   {
      m_site = site;
      m_viewer = viewer;
      makeActions();
      m_site.getActionBars().setGlobalActionHandler(
            ActionFactory.RENAME.getId(), m_renameAction);
      m_site.getActionBars().setGlobalActionHandler(
            ActionFactory.COPY.getId(), m_copyAction);
      m_site.getActionBars().setGlobalActionHandler(
            ActionFactory.PASTE.getId(), m_pasteAction);
      m_site.getActionBars().setGlobalActionHandler(
            ActionFactory.DELETE.getId(), m_deleteAction);
   }

   @Override
   public void dispose()
   {
      if (clipboard != null)
      {
         clipboard.dispose();
         clipboard = null;
      }
      super.dispose();
   }

   //see base class
   @Override
   public void fillContextMenu(IMenuManager menu)
   {
      IStructuredSelection selection = (IStructuredSelection) getContext()
            .getSelection();

      m_copyAction.selectionChanged(selection);
      menu.add(m_copyAction);
      m_pasteAction.selectionChanged(selection);
      menu.add(m_pasteAction);

      m_deleteAction.selectionChanged(selection);
      menu.add(m_deleteAction);
      if(m_viewer instanceof TreeViewer)
      {
         m_renameAction.selectionChanged(selection);
         menu.add(m_renameAction);
      }
      
      ISelection sel = m_site.getSelectionProvider().getSelection();
      if (!(sel instanceof IStructuredSelection))
         return;
      
      IStructuredSelection ssel = (IStructuredSelection) sel;
      PSUiReference node = (PSUiReference) ssel.getFirstElement();
      if (node == null)
         return;
      boolean allSameType = isSelectedSameObjectType(ssel);
      if(allSameType && node.getName().equals("Searches"))
      {
         m_assignNewSearchesAction.selectionChanged(selection);
         menu.add(m_assignNewSearchesAction);
      }
      
      if (node.getReference() == null)
         return;
      
      Enum objType = node.getReference().getObjectType().getPrimaryType();
      if (allSameType && objType.equals(PSObjectTypes.TEMPLATE))
      {
         m_addSlotsAction.selectionChanged(selection);
         menu.add(m_addSlotsAction);
      }
     
      if (allSameType && objType.equals(PSObjectTypes.CONTENT_TYPE))
      {
         m_addForCTAction.selectionChanged(selection);
         menu.add(m_addForCTAction);
         m_addWorkflowsAction.selectionChanged(selection);
         menu.add(m_addWorkflowsAction);
         m_enableDisableAction.selectionChanged(selection);
         menu.add(m_enableDisableAction);
      }
      
   }
   
   /**
    * Utility method to determine if all selected objects are of the
    * same object type.
    * @param sel the selection, assumed not <code>null</code>.
    * @return <code>true</code> if all selected objects are of
    * the same type.
    */
   private boolean isSelectedSameObjectType(IStructuredSelection sel)
   {
      Iterator it = sel.iterator();
      PSObjectType firstObjectType = null;
      while(it.hasNext())
      {
         PSUiReference node = (PSUiReference)it.next();
         PSObjectType type = node.getObjectType();
         if(firstObjectType == null)
         {
            firstObjectType = type;
            continue;
         }
         if(!firstObjectType.equals(type))
            return false;
      }
      return true;
   }

   //see base class
   @Override
   public void fillActionBars(@SuppressWarnings("unused") IActionBars actionBars)
   {
//      textActionHandler = new TextActionHandler(actionBars); // hooks handlers
//      textActionHandler.setCopyAction(m_copyAction);
//      textActionHandler.setPasteAction(m_pasteAction);
//      textActionHandler.setDeleteAction(m_deleteAction);
//      m_renameAction.setTextActionHandler(textActionHandler);
//
//      actionBars.setGlobalActionHandler(ActionFactory.MOVE.getId(),
//            moveAction);
   }

   /**
    * If the DEL or F2 keys are pressed, the delete or rename actions are run,
    * respectively.
    */
   public void handleKeyPressed(KeyEvent event)
   {
      if (event.character == SWT.DEL && event.stateMask == 0)
      {
         if (m_deleteAction.isEnabled())
         {
            m_deleteAction.run();
         }

         // Swallow the event.
         event.doit = false;

      }
      else if (event.keyCode == SWT.F2 && event.stateMask == 0)
      {
         if (m_renameAction.isEnabled())
         {
            m_renameAction.run();
         }

         // Swallow the event.
         event.doit = false;
      }
   }

   /**
    * Creates all the actions managed by this group and assigns them to the
    * instance members.
    */
   protected void makeActions()
   {
      Shell shell = m_site.getShell();
      clipboard = new Clipboard(shell.getDisplay());
      
      m_pasteAction = new PSPasteAction(m_viewer, clipboard);
      ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
      m_pasteAction.setDisabledImageDescriptor(images
            .getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
      m_pasteAction.setImageDescriptor(images
            .getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
      m_pasteAction.setHoverImageDescriptor(
            images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));

      m_copyAction = new PSCopyAction(m_viewer, clipboard, m_pasteAction);
      m_copyAction.setDisabledImageDescriptor(images
            .getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
      m_copyAction.setImageDescriptor(images
            .getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
      m_copyAction.setHoverImageDescriptor(
            images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

      m_renameAction = new PSRenameAction(m_viewer, m_pasteAction);

      m_deleteAction = new PSDeleteAction(m_viewer);
      m_deleteAction.setDisabledImageDescriptor(images
            .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
      m_deleteAction.setImageDescriptor(images
            .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
      m_deleteAction.setHoverImageDescriptor(
            images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
      
      //TODO - pf - these don't belong here, these are not 'refactor' actions
      m_addSlotsAction = new PSAddSlotsAction(shell, m_site, m_viewer);
      m_addForCTAction = new PSAddForCTAction(shell, m_site, m_viewer);
      m_addWorkflowsAction = new PSAddWorkflowsAction(shell, m_site);
      m_enableDisableAction = new PSEnableDisableContentTypeAction(m_viewer);
      m_assignNewSearchesAction = 
         new PSAssignNewSearchesByCommunityAction(m_viewer);
   }

   //see base class
   @Override
   public void updateActionBars()
   {
      IStructuredSelection selection = (IStructuredSelection) getContext()
            .getSelection();

      m_copyAction.selectionChanged(selection);
      m_pasteAction.selectionChanged(selection);
      m_deleteAction.selectionChanged(selection);
      m_renameAction.selectionChanged(selection);
      m_addSlotsAction.selectionChanged(selection);
      m_addForCTAction.selectionChanged(selection);
      m_addWorkflowsAction.selectionChanged(selection);
      m_enableDisableAction.selectionChanged(selection);
      m_assignNewSearchesAction.selectionChanged(selection);
   }

   private Clipboard clipboard;

   private PSCopyAction m_copyAction;

   private PSDeleteAction m_deleteAction;

   private PSPasteAction m_pasteAction;

   private PSRenameAction m_renameAction;
   
   private PSAddSlotsAction m_addSlotsAction;
   
   private PSAddForCTAction m_addForCTAction;
   
   private PSAddWorkflowsAction m_addWorkflowsAction;

   private PSEnableDisableContentTypeAction m_enableDisableAction;
   
   private final IViewSite m_site;

   private final StructuredViewer m_viewer;

   private PSAssignNewSearchesByCommunityAction m_assignNewSearchesAction;

}

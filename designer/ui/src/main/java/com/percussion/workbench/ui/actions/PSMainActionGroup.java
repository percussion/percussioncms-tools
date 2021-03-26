/******************************************************************************
 *
 * [ PSMainActionGroup.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.PSFileEditorTracker;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import java.util.List;

/**
 * This group contains all the actions used by objects in the tree.
 */
public class PSMainActionGroup extends ActionGroup
{
   /**
    * Constructs the main action group.
    * 
    * @param site Never <code>null</code>.
    * 
    *  @param viewer The control for which the actions are being created. Never
    *  <code>null</code>.
    */
   public PSMainActionGroup(IViewSite site, StructuredViewer viewer)
   {
      if (null == site)
      {
         throw new IllegalArgumentException("site cannot be null");  
      }
      if (null == viewer)
      {
         throw new IllegalArgumentException("viewer cannot be null");  
      }
      m_site = site;
      m_viewer = viewer;
      makeActions();
      makeSubGroups();
   }


   /**
    * Makes the actions contained directly in this action group.
    */
   protected void makeActions()
   {
      m_refreshAction = new PSRefreshAction(m_viewer);
      m_securityAction = new PSSecurityAction(m_viewer);
   }

   /**
    * Makes the sub action groups.
    */
   protected void makeSubGroups()
   {
      m_newGroup = new PSNewActionGroup(m_site.getPage(), m_viewer);
      m_openGroup = new PSOpenEditorActionGroup(m_site, m_viewer);
      m_refactorGroup = new PSRefactorActionGroup(m_site, m_viewer);
      m_manageExternalEditorGroup = new PSManageExternalEditorGroup(m_viewer);
      m_xmlAppGroup = new PSXmlApplicationActionGroup(m_site);
   }

   /**
    * If the selection is a folder, its open state is toggled. Otherwise, if
    * the open strategy is double-click, the editor for the selected object
    * is opened, otherwise, nothing is done.
    * 
    * @param event Info about the gesture.
    */
   public void handleDoubleClick(DoubleClickEvent event)
   {
      StructuredViewer viewer = (StructuredViewer) event.getViewer();
      Object element = ((IStructuredSelection) event.getSelection())
            .getFirstElement();
      if (!(element instanceof PSUiReference))
         return;
      PSUiReference node = (PSUiReference) element;
      if (node.isFolder() && (viewer instanceof TreeViewer))
      {
         ((TreeViewer)viewer).setExpandedState(element, 
            !((TreeViewer)viewer).getExpandedState(element));
      }
   }
   
   /**
    * If the open action is enabled, it is executed.
    * 
    * @param event Not used.
    */
   public void handleOpen(OpenEvent event)
   {
      System.out.println("handleOpen");
      BaseSelectionListenerAction openAction = 
         ((BaseSelectionListenerAction) m_openGroup.getOpenAction());
      openAction.selectionChanged((IStructuredSelection) event.getSelection());
      if (openAction != null && openAction.isEnabled())
      {
         openAction.run();
         return;
      }
   }
   
   /**
    * Fills the context menu with the actions contained in this group
    * and its subgroups.
    * 
    * @param menu the context menu. All actions/groups controlled by this group
    * are added to this menu. Never <code>null</code>.
    */
   public void fillContextMenu(IMenuManager menu)
   {
      if (null == menu)
      {
         throw new IllegalArgumentException("menu cannot be null");  
      }
      IStructuredSelection selection = (IStructuredSelection) getContext()
            .getSelection();

      ActionContext ctx = new ActionContext(selection);

      m_newGroup.setContext(ctx);
      m_newGroup.fillContextMenu(menu);
      m_openGroup.setContext(ctx);
      m_openGroup.fillContextMenu(menu);
      menu.add(new Separator());

      m_refactorGroup.setContext(ctx);
      m_refactorGroup.fillContextMenu(menu);
      menu.add(new Separator());

      m_xmlAppGroup.setContext(ctx);
      m_xmlAppGroup.fillContextMenu(menu);
      menu.add(new Separator());

      m_refreshAction.selectionChanged(selection);
      menu.add(m_refreshAction);
      if (externalEditingActionsVisible(selection))
      {
         m_manageExternalEditorGroup.setContext(ctx);
         m_manageExternalEditorGroup.fillContextMenu(menu);
      }

      menu.add(new Separator());

      menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$

      menu.add(new Separator());
      m_securityAction.selectionChanged(selection);
      menu.add(m_securityAction);
   }

   /**
    * Determines if the supplied selection contains only nodes that are
    * currently being edited in an external editor.
    * 
    * @param ss Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if all nodes in <code>ss</code> are being
    * edited, <code>false</code> otherwise.
    */
   private boolean externalEditingActionsVisible(IStructuredSelection ss)
   {
      if (ss.size() == 0)
         return false;

      List<PSPair<IPSReference, IFile>> currentlyOpen = PSFileEditorTracker
            .getInstance().getRegisteredReferences(false);

      if (currentlyOpen.isEmpty())
         return false;
      
      for (Object o : ss.toList())
      {
         if (!(o instanceof PSUiReference))
         {
            return false;
         }
         PSUiReference node = (PSUiReference) o;
         boolean found = false;
         for (PSPair<IPSReference, IFile> p : currentlyOpen)
         {
            if (node.getReference().equals(p.getFirst()))
               found = true;
         }
         if (!found)
            return false;
      }

      return true;
   }

   /**
    * Adds the actions in this group and its subgroups to the action bars.
    */
   public void fillActionBars(IActionBars actionBars)
   {
      m_openGroup.fillActionBars(actionBars);
      m_refactorGroup.fillActionBars(actionBars);
      m_xmlAppGroup.fillActionBars(actionBars);
   }

   /**
    * Updates the actions which were added to the action bars,
    * delegating to the subgroups as necessary.
    */
   public void updateActionBars()
   {
      m_openGroup.updateActionBars();
      m_refactorGroup.updateActionBars();
      m_xmlAppGroup.updateActionBars();
   }

   /**
    * Extends the superclass implementation to dispose the 
    * actions in this group and its subgroups.
    */
   public void dispose()
   {
      m_openGroup.dispose();
      m_refactorGroup.dispose();
      m_xmlAppGroup.dispose();
      super.dispose();
   }

   /**
    * Contains the actions for creating new objects. Created in
    * {@link #makeSubGroups()}, then never changed or <code>null</code>.
    */
   private PSNewActionGroup m_newGroup;

   /**
    * Contains the actions for opening objects in an editor. Created in
    * {@link #makeSubGroups()}, then never changed or <code>null</code>.
    */
   private PSOpenEditorActionGroup m_openGroup;
   
   /**
    * Contains the editing actions such as copy/paste. Created in
    * {@link #makeSubGroups()}, then never changed or <code>null</code>.
    */
   private PSRefactorActionGroup m_refactorGroup;
   
   /**
    * Allows user to flush the cache in the viewer. Created in
    * {@link #makeActions()}, then never changed or <code>null</code>.
    */
   private PSRefreshAction m_refreshAction;
   
   /**
    * Allows user to add/remove ACLs on a design object. Created in
    * {@link #makeActions()}, then never changed or <code>null</code>.
    */
   private PSSecurityAction m_securityAction;

   /**
    * XML application-specific action group.
    */
   private PSXmlApplicationActionGroup m_xmlAppGroup;

   /**
    * The control using these actions. Never <code>null</code> after ctor.
    */
   private final StructuredViewer m_viewer;
   
   /**
    * The site containing the control. Never <code>null</code> after ctor.
    */
   private final IViewSite m_site;

   /**
    * Contains the actions used to manage objects being edited external to
    * Eclipse.
    */
   private PSManageExternalEditorGroup m_manageExternalEditorGroup;
}

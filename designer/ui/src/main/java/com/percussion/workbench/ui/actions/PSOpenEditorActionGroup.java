/******************************************************************************
 *
 * [ PSOpenEditorActionGroup.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.PSFileEditorTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Action group that adds the actions opening a new editor to the 
 * context menu and the action bar's navigate menu.
 */
public class PSOpenEditorActionGroup extends ActionGroup
{
   /**
    * Creates a new <code>OpenActionGroup</code>. The group requires that the
    * selection provided by the part's selection provider is of type <code>
    * org.eclipse.jface.viewers.IStructuredSelection</code>.
    * 
    * @param site the site that owns this action group
    * @param provider The source of the selections for actions managed by this
    * group. Never <code>null</code>.
    */
   public PSOpenEditorActionGroup(IWorkbenchSite site, 
         ISelectionProvider provider)
   {
      if (null == provider)
      {
         throw new IllegalArgumentException("provider cannot be null");  
      }
      m_site = site;
      m_openAction = new PSOpenAction(m_site, provider);
      m_objectSorterAction = new PSShowObjectSorterViewAction(provider);
//      m_openAction.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);
      ISelection selection = m_site.getSelectionProvider().getSelection();
      if (selection instanceof IStructuredSelection)
      {
         m_openAction.selectionChanged((IStructuredSelection)selection);
         m_objectSorterAction.selectionChanged(
            (IStructuredSelection)selection);
      }
   }

   /**
    * Returns the open action managed by this action group.
    * 
    * @return the open action. Returns <code>null</code> if the group doesn't
    * provide any open action
    */
   public IAction getOpenAction()
   {
      return m_openAction;
   }
   
   /**
    * Returns the show object sorter action managed by this action group.
    * 
    * @return The 'show object sorter' action. Never <code>null</code>.
    */
   public IAction getShowObjectSorterAction()
   {
      return m_objectSorterAction;
   }

   /*
    * (non-Javadoc) Method declared in ActionGroup
    */
   public void fillActionBars(IActionBars actionBar)
   {
      super.fillActionBars(actionBar);
      setGlobalActionHandlers(actionBar);
   }

   /*
    * (non-Javadoc) Method declared in ActionGroup
    */
   public void fillContextMenu(IMenuManager menu)
   {
      IStructuredSelection selection = (IStructuredSelection) getContext()
            .getSelection();
      super.fillContextMenu(menu);
      m_openAction.selectionChanged(selection);
      menu.add(m_openAction);
      addOpenWithMenu(menu);
      m_objectSorterAction.selectionChanged(selection);
      menu.add(m_objectSorterAction);
   }

   @SuppressWarnings("unused")
   private void setGlobalActionHandlers(IActionBars actionBars)
   {
      actionBars.setGlobalActionHandler(
            "com.percussion.workbench.ui.actions.Open", m_openAction);
   }

   @SuppressWarnings("unused")
   private void appendToGroup(IMenuManager menu, IAction action)
   {
      if (action.isEnabled())
         menu.appendToGroup("group.open"/*IContextMenuConstants.GROUP_OPEN*/, action);
   }

   /**
    * Checks if the menu should be shown, and if it should, builds it and adds
    * it to the supplied menu.
    * 
    * @param menu Receives the generated menu entry. Assumed not
    * <code>null</code>.
    */
   private void addOpenWithMenu(IMenuManager menu)
   {
      ISelection selection = getContext().getSelection();
      if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
         return;
      IStructuredSelection ss = (IStructuredSelection) selection;
      if (ss.size() != 1)
         return;

      Object o = ss.getFirstElement();
      if (!(o instanceof PSUiReference))
         return;
      
      PSUiReference node = (PSUiReference) o;
      if (node.getReference() == null || node.isFolder())
         return;
      
      if (!((IPSPrimaryObjectType) node.getObjectType().getPrimaryType())
            .isFileType())
      {
         return;
      }
      
      String label = PSMessages
            .getString("PSOpenEditorActionGroup.openWith.label");
      if (PSFileEditorTracker.getInstance().isRegisteredForEdit(
            node.getReference(), true)
            || PSFileEditorTracker.getInstance().isRegisteredForEdit(
                  node.getReference(), false))
      {
         // todo - OK for release - I would like to add the menu, but disable it
         // instead, but I didn't have time to figure out how to disable a
         // sub-menu
         return;
      }

      IMenuManager submenu = new MenuManager(label);
      submenu.add(new PSOpenWithMenu(m_site.getPage(), node));
      menu.add(submenu);
   }

   /**
    * Used when creating the various open menus. Never <code>null</code> after
    * ctor.
    */
   private final IWorkbenchSite m_site;

   /**
    * The basic Open action. Never <code>null</code> after ctor. 
    */
   private final PSOpenAction m_openAction;
   
   /**
    * The action for the object sorter. Never <code>null</code> after ctor.
    */
   private final PSShowObjectSorterViewAction m_objectSorterAction;
}

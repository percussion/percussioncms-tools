/******************************************************************************
 *
 * [ PSNewWizardMenu.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;


import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BaseNewWizardMenu;

import java.util.List;

/**
 * This class extends the base by adding entries at the top of the menu that
 * are only applicable to the current selection.
 *
 * @author paulhoward
 */
public class PSNewWizardMenu extends BaseNewWizardMenu
{
   /**
    * Needed for base class. See base class for further description of params.
    * 
    * @param page Context for the actions. Never <code>null</code>.
    * 
    * @param id Supplied to super ctor. May be <code>null</code>.
    * 
    * @param provider Passed to the actions created by this menu as the source
    * of the selections to be processed.
    */
   public PSNewWizardMenu(IWorkbenchPage page, String id, 
         ISelectionProvider provider)
   {
      super(page.getWorkbenchWindow(), id);
      IWorkbenchWindow window = page.getWorkbenchWindow();
      m_newFolderAction = new PSNewFolderAction(provider);
      m_newFileAction = new PSNewFileAction(page, provider);
      m_newObjectAction = new PSNewRxResourceAction(provider, window);
   }
   
   /**
    * Similar to the other ctor. Should only be used if a page is not available.
    * An attempt will be made lazily to find a page to use. If not found, an
    * exception will occur at that time. See other ctor for param description.
    * 
    * @param win Never <code>null</code>.
    */
   public PSNewWizardMenu(IWorkbenchWindow win, String id, 
         ISelectionProvider provider)
   {
      super(win, id);
      m_newFolderAction = new PSNewFolderAction(provider);
      m_newFileAction = new PSNewFileAction(null, provider);
      m_newObjectAction = new PSNewRxResourceAction(provider, win);
   }

   /**
    * Overridden to add the context sensitive entries.
    */
   @Override
   @SuppressWarnings("unchecked")   //old style in api
   protected void addItems(List list)
   {
      assert(list != null);
      boolean needSep = false;
      if (m_newFolderAction.isEnabled())
      {
         needSep = true;
         list.add(new ActionContributionItem(m_newFolderAction));
      }
      if (m_newFileAction.isEnabled())
      {
         needSep = true;
         list.add(new ActionContributionItem(m_newFileAction));
      }
      if (m_newObjectAction.isEnabled())
      {
         needSep = true;
         list.add(new ActionContributionItem(m_newObjectAction));
      }
      if (needSep)
         list.add(new Separator());
      
      super.addItems(list);
   }

   /**
    * Allows user to create new files in certain declarative views. Never
    * <code>null</code> after ctor.
    */
   private final PSNewFileAction m_newFileAction;

   /**
    * Allows user to create new design objects where appropriate. Never
    * <code>null</code> after ctor.
    */
   private final PSNewRxResourceAction m_newObjectAction;

   /**
    * Allows user to create new folders in declarative views. Never
    * <code>null</code> after ctor.
    */
   private PSNewFolderAction m_newFolderAction;
}

/******************************************************************************
 *
 * [ PSNewActionGroup.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.workbench.ui.PSMessages;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;

/**
 * This group contains the actions associated with creating new objects within
 * the CMS system.
 *
 * @author paulhoward
 */
public class PSNewActionGroup extends ActionGroup
{
   /**
    * Only ctor.
    * 
    * @param page Never <code>null</code>.
    */
   public PSNewActionGroup(IWorkbenchPage page, ISelectionProvider provider)
   {
      if (null == page)
      {
         throw new IllegalArgumentException("site cannot be null");  
      }
      m_newSubMenu = new PSNewWizardMenu(page, null, provider);
   }

   /*
    * (non-Javadoc) Method declared in ActionGroup
    */
   public void fillActionBars(IActionBars actionBar)
   {
      super.fillActionBars(actionBar);
   }

   //see base class
   public void fillContextMenu(IMenuManager menu)
   {
      super.fillContextMenu(menu);
      
      MenuManager newMenu = new MenuManager(PSMessages
            .getString("PSNewActionGroup.action.label"),
            ActionFactory.NEW.getId());
      menu.add(newMenu);
      newMenu.add(m_newSubMenu);
   }

   /**
    * The sub menu for the 'New' menu entry. Never <code>null</code> after ctor.
    */
   private final PSNewWizardMenu m_newSubMenu;
}

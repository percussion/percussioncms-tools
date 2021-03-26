/*[ IDynamicActions.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import org.eclipse.jface.action.IAction;


/**
 * This interface is used by the main frame to build or extend drop down menus
 * dynamically. All modeless windows that are displayed in the main frame must
 * implement this interface. Whenever a window becomes active, it is queried to
 * determine if it wants to add any items to the dropdown list. If no items are
 * available, and there are no items already in the dropdown, the main menu
 * item will be removed from the main menu while this window is active.
 * <p>
 * A menu item will always be added for each action. If the action has an 
 * icon, a toolbar button will also be created.
 */
public interface IDynamicActions
{
   /**
    * Returns an array of action items with attached listeners for the main menu
    * item with the passed in id.
    *
    * @param strMenuId the internal name of the main menu action which is being
    * dynamically created or extended.
    *
    * @returns The array of actions for the supplied action ID. If no actions
    * are present (the caller didn't check by first calling hasActionItems()), 
    * an empty array is returned.
    */
   public IAction[] getActionItems(String strActionId);

   /**
    * Returns <code>true</code> if this object wants to add any menu items to the main menu
    * action with the passed in id.
    *
    * @param strMenuId the internal name of the main menu action which is being
    * dynamically created or extended.
    */
   public boolean hasActionItems(String strActionId);
}


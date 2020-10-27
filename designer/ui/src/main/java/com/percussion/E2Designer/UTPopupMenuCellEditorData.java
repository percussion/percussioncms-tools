/*[ UTPopupMenuCellEditorData.java ]********************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;

/**
 * This class represents data required for constructing the popup menu cell
 * editor (<code>UTPopupMenuCellEditor</code>).
 *
 * @see UTPopupMenuCellEditor
 */
public class UTPopupMenuCellEditorData
{
   /**
    * Constructs the cell editor data from the specified menu item and the
    * dialog to display when the menu item is selected.
    *
    * @param menuItem the menu item to display when the edit button is pressed,
    * may not be <code>null</code>
    *
    * @param cellDlg the dialog to display if the user selects the menu item
    * <code>menuItem</code> from the popup menu displayed when the edit
    * button is pressed.
    *
    * @throws IllegalArgumentException if <code>menuItem</code> or
    * <code>cellDlg</code> is displayed.
    */
   public UTPopupMenuCellEditorData(
      JMenuItem menuItem, IPSCellEditorDialog cellDlg)
   {
      if (menuItem == null)
         throw new IllegalArgumentException("menu item may not be null");

      if (cellDlg == null)
         throw new IllegalArgumentException("cell Dlg may not be null");

      m_menuItem = menuItem;
      m_cellDlg = cellDlg;
   }

   /**
    * Returns the menu item to display in the popup menu when the user clicks
    * on the edit button.
    *
    * @return the menu item to display in the popup menu, never
    * <code>null</code>
    */
   public JMenuItem getMenuItem()
   {
      return m_menuItem;
   }

   /**
    * Returns the dialog to display if the user selects the menu item returned
    * by <code>getMenuItem()</code> method.
    *
    * @return the dialog to display when the menu item is selected, never
    * <code>null</code>
    */
   public IPSCellEditorDialog getCellDialog()
   {
      return m_cellDlg;
   }

   /**
    * the popup menu item to display when the button is clicked in the
    * table cell, initialized in the ctor, never <code>null</code> or
    * modified after initialization
    */
   private JMenuItem m_menuItem;

   /**
    * the dialog to display when the popup menu item is selected by the user,
    * initialized in the ctor, never <code>null</code> or
    * modified after initialization
    */
   private IPSCellEditorDialog m_cellDlg;

}

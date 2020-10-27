/*[ UTPopupMenuCellEditor.java ]***********************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSTextLiteral;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

/**
 * This class represents a table cell editor which displays a text field
 * with a button. When the button is clicked, it displays a popup menu.
 */
public class UTPopupMenuCellEditor extends UTTextFieldCellEditor
   implements TableCellEditor, ActionListener, KeyListener
{
   /**
    * Constructs the cell editor from the specified cell editor data objects.
    *
    * @param cellEditorData the cell editor data objects, may not be
    * <code>null</code> or empty array
    *
    * @throws IllegalArgumentException if <code>cellEditorData</code> is
    * <code>null</code> or empty array
    */
   public UTPopupMenuCellEditor(UTPopupMenuCellEditorData[] cellEditorData)
   {
      if ((cellEditorData == null) || (cellEditorData.length == 0))
         throw new IllegalArgumentException("Invalid cell editor data");

      m_menu = new JPopupMenu();

      for (int i = 0; i < cellEditorData.length; i++)
      {
         JMenuItem menuItem = cellEditorData[i].getMenuItem();
         IPSCellEditorDialog cellDlg = cellEditorData[i].getCellDialog();
         if ((menuItem == null) || (cellDlg == null))
            throw new IllegalArgumentException("Invalid cell editor data");
         m_menu.add(menuItem);
      }

      m_cellEditorData = cellEditorData;
      addActionListeners();
   }

   /**
    * Create the panels.
    */
   protected void initPanel()
   {
      m_displayPanel = new JPanel();
      m_displayPanel.setLayout(new BoxLayout(m_displayPanel, BoxLayout.X_AXIS));
      m_displayPanel.add(m_text);

      m_editPanel = new JPanel();
      m_editPanel.setLayout(new BoxLayout(m_editPanel, BoxLayout.X_AXIS));
      m_editPanel.add(m_text);
      m_editPanel.add(m_edit);

      // set the icon for the edit button
      m_edit.setIcon(new ImageIcon(getClass().getResource(
         E2Designer.getResources().getString("gif_right"))));

      // add the display panel by default
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      add(m_displayPanel);
      m_isDisplayMode = true;
   }

   /**
    * Adds action listeners for the cell browse (...) button, clicking
    * ENTER on cell text field, or selecting a popup menu item.
    */
   private void addActionListeners()
   {
      // perform action for cell browse (...) button
      addEditListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            // make sure the editor's default type is in sync with ours
            m_menu.show(m_edit, m_edit.getWidth(), m_edit.getHeight());
         }
      });

      // perform action for ENTER on cell text field
      addTextListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            stopCellEditing();
         }
      });

      for (int i = 0; i < m_cellEditorData.length; i++)
      {
         JMenuItem menuItem = m_cellEditorData[i].getMenuItem();
         final IPSCellEditorDialog cellDlg =
            m_cellEditorData[i].getCellDialog();

         menuItem.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent event)
            {
               Object obj = getCellEditorValue();
               if ((obj == null) || (obj.toString().trim().length() < 1))
                  obj = new PSTextLiteral("");
               cellDlg.setData(obj);
               cellDlg.setVisible(true);
            }
         });

         cellDlg.addOkListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent event)
            {
               setValue(cellDlg.getData());
               acceptCellEditing();
            }
         });

         cellDlg.addCancelListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent event)
            {
               cancelCellEditing();
            }
         });
      }
   }

   /**
    * See {@link UTTextFieldEditor#setValue(Object)} for details.
    */
   public boolean setValue(Object value)
   {
      if (value instanceof PSFunctionCall)
         m_text.setEditable(false);
      else
         m_text.setEditable(true);

      return super.setValue(value);
   }

   /**
    * An array of cell editor data objects. Each object contains a popup
    * menu item and a dialog to display when the menu item is selected,
    * initialized in the ctor, never <code>null</code> or modified after
    * initialization
    */
   private UTPopupMenuCellEditorData[] m_cellEditorData;

   /**
    * The popup menu to display when the edit button in the cell is pressed.
    * This menu includes all the menu items specified in the cell editor data
    * in the ctor, initialized in the ctor, never <code>null</code> or modified
    * after initialization;
    */
   private JPopupMenu m_menu;
}

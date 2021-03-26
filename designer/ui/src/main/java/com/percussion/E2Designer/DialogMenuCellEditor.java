/*[ DialogMenuCellEditor.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides an table cell editor with a text field and a button to
 * display a menu of dialogs.
 */
public class DialogMenuCellEditor extends AbstractCellEditor
      implements TableCellEditor
{
   /**
    * Initializes a newly created <code>DialogMenuCellEditor</code>
    * object.
    */
   public DialogMenuCellEditor()
   {
      createEditPanel();
      m_dialogMenu = new JPopupMenu();
   }

   /**
    * Holds the value generated by a dialog triggered from the menu button.
    * If <code>null</code>, the value contained in the m_editorComponent
    * should be used.
    */
   private Object m_valueFromDialog;

   /**
    * Gets the value from the editor to assign back to the table cell.  If a
    * value was generated by a sub-dialog, that value is returned; otherwise,
    * the text from m_editorComponent is returned.
    *
    * @return either a String (possibly empty) or a IPSReplacementValue;
    * never <code>null</code>
    */
   public Object getCellEditorValue()
   {
      if (null == m_valueFromDialog)
         return m_editorComponent.getText();
      else
         return m_valueFromDialog;
   }


   // use our component (text and button) instead of super's
   // see interface for description of parameters
   public Component getTableCellEditorComponent(JTable table, Object value,
                                                boolean isSelected,
                                                int row, int column)
   {
      // set the value from the table cell to the value we are editing
      setValue( value );
      table.repaint(); // seems like overkill, but prevents the cell from
                       // having a spotty update (just repainting the editPanel
                       // isn't enough)
      return m_editPanel;
   }


   /**
    * Creates the component used to edit the cell:  a text field and a button.
    * The button is used to select a dialog that will supply the value for
    * this cell.
    */
   private void createEditPanel()
   {
      m_menuBtn = new UTFixedButton((new ImageIcon(getClass().getResource(
            E2Designer.getResources().getString("gif_Browser")))),
            new Dimension(20, 500))
      {
         /** Don't need to tab onto the menu button */
         public boolean isFocusTraversable()
         {
            return false;
         }
      };
      m_menuBtn.setVisible(false); // only visible when dialogs are registered
      m_menuBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onMenuBtn( e.getSource() );
         }
      });


      m_editorComponent = new JTextField();
      m_editorComponent.addFocusListener( new FocusAdapter()
      {
         /** Selects all of the text within the editor (if editable) */
         public void focusGained(FocusEvent e)
         {
            if (m_editorComponent.isEditable()) m_editorComponent.selectAll();
         }
      } );
      m_editorComponent.addKeyListener( new KeyAdapter()
      {
         /**
          * when the backspace or delete key is pressed and we are not
          * editable (because cell contains a dialog value), clear the
          * dialog value and text and enable edits
          */
         public void keyReleased (KeyEvent e)
         {
            if (!m_editorComponent.isEditable())
               if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                   e.getKeyCode() == KeyEvent.VK_DELETE)
               {
                  setValue( null );
               }
         }
      } );

      m_editPanel = new JPanel();
      m_editPanel.setLayout( new BoxLayout(m_editPanel, BoxLayout.X_AXIS) );
      m_editPanel.add( m_editorComponent );
      m_editPanel.add( m_menuBtn );
   }

   /**
    * Loads the specified dialog, and if it returns a value, assigns that value
    * to this editor.
    *
    * @param dialog the PSDialog to display; assumed not <code>null</code>
    */
   private void processDialog(PSDialogAPI dialog)
   {
      if (m_valueFromDialog != null)
         dialog.setData( m_valueFromDialog );
      else
         dialog.reset();
      
      dialog.setVisible(true);

      /* dialog is modal, so we can assume once we are here, dialog is closed
       * now, try to get value from dialog (null means cancelled or no input)
       */
      Object result = dialog.getData();

      if (result != null)
      {
         setValue( result );
         stopCellEditing();
      }
   }

   /**
    * Processes a selection from the dialog pop-up menu, by processing the
    * dialog associated with that menu selection.
    * @param menuLabel label of the selected menu item; matched against our
    * HashMap of dialogs
    */
   private void onDialogMenuItem(String menuLabel)
   {
      processDialog( (PSDialogAPI) m_dialogsLabel.get(menuLabel) );
   }


   /**
    * Processes the menu button within the cell.  One of three behaviors is
    * possible:
    * <ol>
    * <li>If the cell has a dialog-generated value, show the dialog that can 
    * edit it.  The dialogs are consulted according to the order they were 
    * {@link #addDialog added}, and the first dialog to claim the value is 
    * displayed.  If no dialog claims the value, nothing will happen.
    * <li>If the cell has no dialog-generated value (the cell may have a 
    * manually-entered String value) and only one dialog is registered, show it.
    * <li>If the cell has no dialog-generated value (the cell may have a 
    * manually-entered String value) and multiple dialogs have been registered,
    * show a pop-up menu to select which dialog to use.
    * </ol>
    * This method will not be called when no dialogs have been registered, as
    * the menu button is not displayed in that case.
    *
    * @param eventSource the JButton object that was pressed; used to position
    * the pop-up menu; assumed not <code>null</code>
    */
   private void onMenuBtn(Object eventSource)
   {
      if (null != m_valueFromDialog)
      {
         // once we have a value, only that dialog can be used to edit
         for (Iterator iter = m_dialogs.iterator(); iter.hasNext();)
         {
            PSDialogAPI dialog = (PSDialogAPI) iter.next();
            if (dialog.isValidModel( m_valueFromDialog ))
            {
               processDialog( dialog ); 
               break;
            }
         }        
      }
      else if (m_dialogsLabel.size() == 1)
      {
         // with only one dialog registered, show it
         processDialog((PSDialogAPI) m_dialogsLabel.values().iterator().next());
      }
      else if (m_dialogsLabel.size() > 1)
      {
         // with multiple dialogs registered, show the menu
         m_dialogMenu.show((JButton) eventSource, 0, 0);
      }
   }


   /**
    * Adds the specified dialog to the collection of those supported by this
    * editor.  A menu item with <code>menuLabel</code> will be created and
    * added to bottom of the pop-up menu (the order the dialogs are added using
    * this method determines the ordering of the menu). 
    * <p>
    * When a cell with an object is edited, each dialog is consulted to see if
    * it can edit that object.  The order the dialogs are added using this 
    * method determines the order in which the dialogs are consulted.  If more
    * than one dialog can edit a given object type, the dialog with the most 
    * specific model requirements should be added first, as the first dialog 
    * that claims the object is displayed.  
    * 
    * @param dialog the dialog that will be displayed when an object of 
    * <code>type</code> is edited, or the menu item is selected, not <code>null
    * </code>.
    * @param menuLabel the label to use for the menu item that will cause
    * the dialog to be displayed; cannot be <code>null</code> or empty
    * 
    * @throws IllegalArgumentException if any parameter is <code>null</code> or
    * <code>menuLabel</code> is empty.
    */
   public void addDialog(PSDialogAPI dialog, String menuLabel)
   {
      if (null == dialog)
         throw new IllegalArgumentException("dialog may not be null");
      if (null == menuLabel || menuLabel.trim().length() == 0)
         throw new IllegalArgumentException("menuLabel cannot be null or empty");

      m_menuBtn.setVisible( true );
      m_dialogsLabel.put( menuLabel, dialog );
      m_dialogs.add( dialog );
      m_dialogMenu.add( menuLabel ).addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onDialogMenuItem( e.getActionCommand() );
         }
      });
   }


   /** Contains all the controls used in editing a cell */
   private JPanel m_editPanel;

   /**
    * The menu displayed when the menu button is pressed (and more than one
    * dialog has been registered).  Set in constructor and never
    * <code>null</code> after that.
    */
   private JPopupMenu m_dialogMenu;

   /**
    * The button that displays the dialog pop-up menu. Set in {@link
    * #createEditPanel} and never <code>null</code> after that.
    */
   private JButton m_menuBtn;

   /** 
    * Maps a <code>String</code> to a <code>PSDialogAPI</code>.  Populated by 
    * <code>addDialog()</code>.  Used to determine which dialog to display when
    * a menu item is selected from the pop-up.  Never <code>null</code>. Empty 
    * if no dialogs have been added.
    */
   private Map m_dialogsLabel = new HashMap();

   /** 
    * Orders <code>PSDialogAPI</code> objects.  Populated by <code>addDialog()
    * </code>.  Processed sequentially in <code>onMenuBtn</code> to determine 
    * which dialog should be used to edit an arbitrary object.  
    * Never <code>null</code>.  Empty if no dialogs have been added.
    */
   private List m_dialogs = new ArrayList();

   /**
    * Used for entering data directly into the cell (without using a dialog).
    * Set in {@link #createEditPanel} and never <code>null</code> after that.
    */
   private JTextField m_editorComponent;


   /**
    * Assigns the value this editor is editing.  Strings are edited using
    * our JTextField.  Other types are currently not editable (they can be
    * replaced by dialog choices, but not by text).
    *
    * @param value the value to be edited; if <code>null</code>, treated like
    * an empty string.
    */
   private void setValue(Object value)
   {
      if (value instanceof String)
      {
         m_valueFromDialog = null;
         m_editorComponent.setEditable( true );
         m_editorComponent.setText( value.toString() );
      }
      else if (value != null)
      {
         // don't allow editing of dialog values without user explictly
         // clearing them first (by pressing backspace or delete key)
         m_valueFromDialog = value;
         m_editorComponent.setEditable( false );
         m_editorComponent.setText( value.toString() );
      }
      else // value must be null
      {
         m_valueFromDialog = null;
         m_editorComponent.setEditable( true );
         m_editorComponent.setText( "" );
      }
   }
}

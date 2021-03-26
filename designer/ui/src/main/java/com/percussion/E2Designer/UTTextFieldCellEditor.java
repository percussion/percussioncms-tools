/*[ UTTextFieldCellEditor.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;

/**
 * The text field editor panel provides two components: an editable text field
 * and a button which opens the dialog provided in the constructor.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTTextFieldCellEditor extends UTTextFieldEditor implements TableCellEditor,
                                                      ActionListener,
                                                      KeyListener
{
   /**
   * Construct the default cell editor
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTTextFieldCellEditor()
   {
   super();
   init();

   }

   /**
   * Construct the cell editor providing a reference to the editor dialog used
   * while the edit button was pressed.
   *
   * @param editor the editor dialog
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTTextFieldCellEditor(PSDialog editor)
   {
   super(editor);
   init();
   }

   public UTTextFieldCellEditor(PSDialog editor, IDataTypeInfo defaultType)
   {
   super(editor, defaultType);
   init();
   }

   /**
   * Construct the cell editor providing a reference to the file chooser used
   * while the edit button was pressed.
   *
   * @param chooser the file chooser
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTTextFieldCellEditor(JFileChooser chooser)
   {
   super(chooser);
   chooser.addActionListener(this);
   init();
   }

   /**
   * Common class initialization.
    */
  //////////////////////////////////////////////////////////////////////////////
  private void init()
  {
   addTextKeyListener(this);
  }

   /**
   * Returns the number of clicks nessecary to start the editor.
    */
  //////////////////////////////////////////////////////////////////////////////
  public int getClickCountToStart()
  {
   return m_clickCountToStart;
  }

  /**
   * Sets the number of clicks nessecary to start the editor.
    */
  //////////////////////////////////////////////////////////////////////////////
  public void setClickCountToStart(int clicks)
  {
   m_clickCountToStart = clicks;
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public Component getTableCellEditorComponent(JTable table,
                                    Object value,
                                                                      boolean isSelected,
                                    int row,
                                    int column)
   {
      if (value == null)
      return this;

   setValue(value);
   table.setRowSelectionInterval(row, row);
   table.setColumnSelectionInterval(column, column);

   // save the original in case the edit session is canceled
   m_original = getValue();

   return this;
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public Object getCellEditorValue()
  {
   return getValue();
  }

 //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor

   /**
    * If editing via the mouse, don't start editing until the click count is
    * high enough. For all other event types, begin editing immediately.
    *
    * @return <code>true</code> if editing is currently allowed, <code>false</code>
    * otherwise
   **/
   public boolean isCellEditable(EventObject event)
  {
      if ( event instanceof MouseEvent
            && ((MouseEvent) event).getClickCount() < getClickCountToStart())
      {
         return false;
      }
      bEditorWasCalled=false;
      setEditMode();
      return true;
}

  /**
   * This flag is returned by <code>shouldSelectCell</code>. It is used to
   * indicate <code>false</code> when a stop editing event has failed.
  **/
  private boolean m_allowSelect = true;
   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public boolean shouldSelectCell(EventObject event)
  {
   return m_allowSelect;
//     return true;
  }

   /*
   * Accept the changes from the editor dialog.
   */
   //////////////////////////////////////////////////////////////////////////////
   public void acceptCellEditing()
  {
   // overwrite the original and fire stopped to accept the changes and
      // stop the edit mode
    m_original = getValue();
    bEditorWasCalled=true;
    fireEditingStopped();
  }
     //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
  public boolean stopCellEditing()
  {
   // if this flag is true then  acceptCellEditing() was called ( the dialog was up )
   // else it will be false
   if( bEditorWasCalled == false )
   {
     // get the original object
     String sOriginalValue = m_original.toString();

      Object newObject = getValue();
      // see if this object was a non-empty string
      if( newObject instanceof String && newObject != null &&
                           ((String)newObject).length() > 0)
      {
         // it is, so set the new text and we are done
          m_allowSelect = setValue(this.getText());
      }
      else if ((newObject instanceof IPSReplacementValue) &&
               (((IPSReplacementValue)newObject).getValueText().equals(this.getText()))
               && sOriginalValue.equals(this.getText()))
      {
         // no change, keep old object
         m_allowSelect = true;
      }
      else if((newObject == null ||
               (newObject instanceof String &&
               ((String)newObject).length() == 0)) &&
               this.getText().length() == 0)
      {
         // leaving field empty, was empty before, do nothing
         m_allowSelect = true;
      }
      else
      {
         // construct a tmp object to set the string
         // create a new object of the proper type
         Object tmpObject=createObject(this.getText(),newObject,m_dataTypes,this);
         if( tmpObject != null ) // the object was created?
         {
            setValue(tmpObject); // yes overwrite the object
            m_allowSelect = true;  // set the flag to overwrite the original object
         }
         else
         {
            m_allowSelect = false ;
         }
      }
   }
   else
   {
     // if we are here acceptCellEditing() was called
      m_allowSelect = setValue(this.getText());
   }
   if ( m_allowSelect )
      acceptCellEditing();

   bEditorWasCalled=false;
   return m_allowSelect;
  }
    //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public void cancelCellEditing()
  {
   m_allowSelect = true;
   bEditorWasCalled=false;
   fireEditingCanceled();
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public void addCellEditorListener(CellEditorListener listener)
  {
     m_listeners.addElement(listener);
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public void removeCellEditorListener(CellEditorListener listener)
  {
     m_listeners.removeElement(listener);
  }

  /**
   * Inform all listeners that editing was stopped.
   */
   //////////////////////////////////////////////////////////////////////////////
  protected void fireEditingStopped()
  {
     setValue(m_original);
      setDisplayMode();

    ChangeEvent event = new ChangeEvent(this);
    for (int i=0; i<m_listeners.size(); i++)
       ((CellEditorListener) m_listeners.elementAt(i)).editingStopped(event);
  }

  /**
   * Inform all listeners that editing was canceled.
   */
   //////////////////////////////////////////////////////////////////////////////
  protected void fireEditingCanceled()
  {
     setValue(m_original);
      setDisplayMode();

    ChangeEvent event = new ChangeEvent(this);
    for (int i=0; i<m_listeners.size(); i++)
       ((CellEditorListener) m_listeners.elementAt(i)).editingCanceled(event);
  }

  //////////////////////////////////////////////////////////////////////////////
  // implementation for ActionListener
  public void actionPerformed(ActionEvent event)
  {
    if (event.getSource() instanceof JFileChooser)
    {
      if (event.getActionCommand() == JFileChooser.CANCEL_SELECTION)
        cancelCellEditing();
      else if (event.getActionCommand() == JFileChooser.APPROVE_SELECTION)
      {
        JFileChooser chooser = (JFileChooser) event.getSource();
        setValue(chooser.getSelectedFile());
        acceptCellEditing();
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  // implements KeyListener
  public void keyPressed(KeyEvent event)
  {
  }

  //////////////////////////////////////////////////////////////////////////////
  // implements KeyListener
  public void keyReleased(KeyEvent event)
  {
  }

  //////////////////////////////////////////////////////////////////////////////
  // implements KeyListener
  public void keyTyped(KeyEvent event)
  {
    if (event.getKeyChar() == '\n' /* Enter */)
    {
     setValue(this.getText());

     setValue(this.getText());
      acceptCellEditing();
    }
    else if (event.getKeyChar() == '\u001b' /* Escape */)
    {
      cancelCellEditing();
    }
  }

   //////////////////////////////////////////////////////////////////////////////
  protected transient Object m_original = new Object();
  protected transient Vector m_listeners = new Vector();

  protected int m_clickCountToStart = 1;
  private   boolean bEditorWasCalled=false;
  /**
   * the list of datatypes that may be selected for this editor
   */
  protected Vector m_dataTypes = new Vector();
}

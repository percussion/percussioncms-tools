/*[ ConditionalCellEditor.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;

/**
 * The cell editor used to ceate or edit mapper conditionals.
 */
////////////////////////////////////////////////////////////////////////////////
public class ConditionalCellEditor extends ConditionalCell implements TableCellEditor
{
   /**
   * Construct the cell editor
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public ConditionalCellEditor(ConditionalPropertyDialog editor) throws ClassNotFoundException
   {
     super(editor);
    m_editor = editor;
    m_editor.addOkListener(new ActionListener()
    {
       public void actionPerformed(ActionEvent event)
      {
        stopCellEditing();
      }
    });
    m_editor.addCancelListener(new ActionListener()
    {
       public void actionPerformed(ActionEvent event)
      {
        cancelCellEditing();
      }
    });
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

    if (table.getModel() instanceof MapperTableModel)
    {
       MapperTableModel model = (MapperTableModel) table.getModel();
      setBackendTank(model.getBackendTank());
      setPageTank(model.getPageTank());
    }

      setValue(value);

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
   public boolean isCellEditable(EventObject event)
  {
     if (event instanceof MouseEvent)
    {
       if (((MouseEvent) event).getClickCount() >= getClickCountToStart())
      {
          setEditMode();
         return true;
      }
    }

    return false;
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public boolean shouldSelectCell(EventObject event)
  {
     return true;
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public boolean stopCellEditing()
  {
     fireEditingStopped();
    return true;
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementations for TableCellEditor
   public void cancelCellEditing()
  {
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
      setDisplayMode();
    ChangeEvent event = new ChangeEvent(this);
    for (int i=0; i<m_listeners.size(); i++)
       ((CellEditorListener) m_listeners.elementAt(i)).editingCanceled(event);
  }

   //////////////////////////////////////////////////////////////////////////////
  protected transient Vector m_listeners = new Vector();

  protected int m_clickCountToStart = 1;

    private ConditionalPropertyDialog m_editor = null;
}

/******************************************************************************
 *
 * [ ConditionalPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The FunctionSelectorDialog shows all available Java Script functions and
 * allows adding / deleting scripts.
 */
////////////////////////////////////////////////////////////////////////////////
public class ConditionalPropertyDialog extends PSDialog
{
   /**
   * Construct the default mapper property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public ConditionalPropertyDialog()
   {
        super();
        initDialog();

   }

   /**
    * Handles key released event.
    */
   public void addKeyEnterHandler()
   {
      addKeyListener(new KeyAdapter()
      {
         public void keyReleased (KeyEvent e)
         {
            if(e.getKeyCode () ==  e.VK_ENTER)
               if(m_table.isEditing())
                  m_table.getCellEditor().stopCellEditing();
         }
      });
   }

   /**
    * Create the WHERE clause selector table to edit the query keys.
    *
    * @param      table               the table model used
    * @return   JScrollPane      the table view, a scrollable pane
    */
   //////////////////////////////////////////////////////////////////////////////
   private JScrollPane createTableView()
   {
      // the helper initializes all valid types for the value selector dialog
      ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(m_backendTank,
      m_pageTank);

      m_variableDialog = new ValueSelectorDialog(this, h.getDataTypes(), null);
      m_variableEditor = new ValueSelectorCellEditor(m_variableDialog);
      m_variableEditor.setClickCountToStart(1);

      m_valueDialog = new ValueSelectorDialog(this, h.getDataTypes(), null);
      m_valueEditor = new ValueSelectorCellEditor(m_valueDialog, null);
      m_valueEditor.setClickCountToStart(1);

      // define the cell editors
      m_table.getColumn( ConditionalTableModel.VARIABLE_COL_NAME ).setCellEditor(m_variableEditor);
      m_table.getColumn( ConditionalTableModel.VARIABLE_COL_NAME ).setPreferredWidth(150);
      m_table.getColumn( ConditionalTableModel.OPERATOR_COL_NAME ).setCellEditor(new DefaultCellEditor(new UTOperatorComboBox()));
      m_table.getColumn( ConditionalTableModel.OPERATOR_COL_NAME ).setPreferredWidth(50);
      m_table.getColumn( ConditionalTableModel.VALUE_COL_NAME ).setCellEditor(m_valueEditor);
      m_table.getColumn( ConditionalTableModel.VALUE_COL_NAME ).setPreferredWidth(150);
      m_table.getColumn( ConditionalTableModel.BOOL_COL_NAME ).setCellEditor(new DefaultCellEditor(new UTBooleanComboBox()));
      m_table.getColumn( ConditionalTableModel.BOOL_COL_NAME ).setPreferredWidth(40);

      // set operator cell renderer
      m_table.getColumn( ConditionalTableModel.OPERATOR_COL_NAME ).setCellRenderer(new UTOperatorComboBoxRenderer());

      JScrollPane pane = new JScrollPane(m_table);
      pane.setPreferredSize(new Dimension(380, 200));
      return pane;
   }

   /**
    * Create command panel.
   *
   * @return   JPanel      the guess command panel
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createCommandPanel()
  {
    m_commandPanel = new UTStandardCommandPanel(
                                           this, "", SwingConstants.HORIZONTAL)
    {
       // implement onOk action
       public void onOk()
      {
          ConditionalPropertyDialog.this.onOk();
      }

       public void onCancel()
      {
          ConditionalPropertyDialog.this.onCancel();
      }
    };

      JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(m_commandPanel, BorderLayout.EAST);

    return panel;
  }

   /**
    * Returns a reference to this dialog's table. Never <code>null</code>.
    */
   public JTable getTable()
   {
      return this.m_table;
   }

   /**
    * Initialize the dialogs GUI elements with its data.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private void initDialog()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(createTableView(), BorderLayout.CENTER);
      panel.add(createCommandPanel(), BorderLayout.SOUTH);

      // set the default button
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);
      setResizable(true);
      pack();
      /* Fix for Memory Leak:  Pack() was leaking a Notify on the window, and
       * this removes it without appearing to cause any problems
       */
      removeNotify();
   }

   /**
   * Initialize a new edit session. The provided tanks are used to initialize
   * the editors combobox with the valid choices.
   *
   * @param conditionals the conditionals to be edited
   * @param backendTank the backend tank used
   * @param pageTank the paga tank used
   */
  //////////////////////////////////////////////////////////////////////////////
  public void onEdit(Object conditionals,
                     OSBackendDatatank backendTank,
                              OSPageDatatank pageTank)
  {
     if (conditionals instanceof PSCollection)
      {
       // refresh the value selector dialog combo boxes
       ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(backendTank,
                                                                  pageTank);
         Vector types = h.getDataTypes();
         m_variableDialog.refresh(h.getDataTypes());
       m_valueDialog.refresh(h.getDataTypes());

       // remove existing rows first
        m_conditionals = (PSCollection) conditionals;
      while (m_model.getRowCount() > 0)
            m_model.deleteRow(0);

        m_model.loadFromConditionals(m_conditionals);
       m_model.appendRow(24 - m_table.getRowCount());
    }
  }

   /**
    * Returns collection of conditionals
    *
    * @return collection of conditionals
    */
    public PSCollection getConditionals()
    {
       return m_conditionals;
    }

/** Overrides PSDialog onOk() method implementation.
*/
   public void onOk()
   {
      try
      {
         m_modified = true;

         stopTableEditor(m_table);

         ConditionalValidationError error = m_model.validate();
         if ( null != error )
         {
            JOptionPane.showMessageDialog(this, error.getErrorText(),
            getResources().getString( "ValidationErrorTitle" ), JOptionPane.WARNING_MESSAGE);
            m_table.getSelectionModel().clearSelection();
            m_table.editCellAt( error.getErrorRow(), error.getErrorCol());
            m_table.getEditorComponent().requestFocus();

            return;
         }
         m_conditionals = m_model.saveToConditionals(m_conditionals);
         fireOk();
         dispose();
      }
      catch ( ClassNotFoundException e )
      {
         PSDlgUtil.showError( e );
      }
  }

   /** Overrides PSDialog onCancel() method implementation.
    */
  public void onCancel()
  {
    m_modified = true;
    fireCancel();
    dispose();
  }

   /**
    *   Add a new OK listener.
   *
    * @ listener the new listener
    */
   //////////////////////////////////////////////////////////////////////////////
   public void addOkListener(ActionListener listener)
  {
     m_okListeners.addElement(listener);
  }

   /**
    *   Remove the provided OK listener.
   *
    * @ listener the listener to be removed
    */
   //////////////////////////////////////////////////////////////////////////////
   public void removeOkListener(ActionListener listener)
  {
     m_okListeners.removeElement(listener);
  }

   /**
    *   Free resources so this dialog will be garbage collected properly.  Should
    * be called when this dialog is no longer needed.  Currently this removes
    * all listeners.  Fix for Memory Leak where circular references that
    * contain listeners are not always GC'd
    */
   public void freeResources()
   {
      m_okListeners.clear();
      m_cancelListeners.clear();
   }

  /**
   * Inform all OK listeners that the OK button was pressed.
   */
   //////////////////////////////////////////////////////////////////////////////
  protected void fireOk()
  {
    ActionEvent event = new ActionEvent(this, 0, "OK");
    for (int i=0; i<m_okListeners.size(); i++)
       ((ActionListener) m_okListeners.elementAt(i)).actionPerformed(event);
  }

   /**
    *   Add a new Cancel listener.
   *
    * @ listener the new listener
    */
   //////////////////////////////////////////////////////////////////////////////
   public void addCancelListener(ActionListener listener)
  {
     m_cancelListeners.addElement(listener);
  }

   /**
    *   Remove the provided Cancel listener.
   *
    * @ listener the listener to be removed
    */
   //////////////////////////////////////////////////////////////////////////////
   public void removeCancelListener(ActionListener listener)
  {
     m_cancelListeners.removeElement(listener);
  }

  /**
   * Inform all Cancel listeners that the Cancel button was pressed.
   */
   //////////////////////////////////////////////////////////////////////////////
  protected void fireCancel()
  {
      ActionEvent event = new ActionEvent(this, 0, "Cancel");
      for (int i=0; i<m_cancelListeners.size(); i++)
         ((ActionListener) m_cancelListeners.elementAt(i)).actionPerformed(event);
   }



   /**
    * Added for testing reasons only.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private ResourceBundle m_res = null;
   protected ResourceBundle getResources()
   {
      try
      {
         if (m_res == null)
            m_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
                                                             Locale.getDefault() );
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }

      return m_res;
   }

   //////////////////////////////////////////////////////////////////////////////
   private PSCollection m_conditionals = null;
   /**
    * the conditional table
    */
   ConditionalTableModel m_model = new ConditionalTableModel();
   UTJTable m_table = new UTJTable(m_model);
   /**
    * The table cell editors
    */
   ValueSelectorCellEditor m_variableEditor = null;
   ValueSelectorCellEditor m_valueEditor = null;
   /**
    * The dialogs associated with the ConditionalCellEditor.
    */
   ValueSelectorDialog m_variableDialog = null;
   ValueSelectorDialog m_valueDialog = null;
   /**
    * the data tanks
    */
    OSBackendDatatank m_backendTank = null;
    OSPageDatatank m_pageTank = null;
   /**
    * this flag will be set if any data within this dialog was modified
    */
   private boolean m_modified = false;
   /**
    * the standard command panel
    */
   private UTStandardCommandPanel m_commandPanel = null;

   protected transient Vector m_okListeners = new Vector();
   protected transient Vector m_cancelListeners = new Vector();
}


/******************************************************************************
 *
 * [ PSRuleEditorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSConditionalSet;
import com.percussion.design.objectstore.PSRule;
import com.percussion.error.PSTableCellValidationError;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.util.PSCollection;
import com.percussion.UTComponents.UTEditorComponent;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.UTStandardCommandPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Iterator;

/**
 * Dialog that allows editing of a set of {@link PSRule} objects.
 */
public class PSRuleEditorDialog extends PSDialog
{
   /**
    * Default Constructor. Creates an empty set of rules and initializes
    * the dialog framework.
    * @param parent the parent dialog of the dialog, may be <code>null</code>
    * @throws ClassNotFoundException if the class type passed to
    * PSCollection is not found.
    */
   public PSRuleEditorDialog(Dialog parent) throws ClassNotFoundException
   {
       super(parent);
       m_rules = PSRuleEditorTableModel.createNewCollection();
       m_udfSet = new PSUdfSet("ALL", E2Designer.getDesignerConnection());
       initDialog();
   }

   /**
    * Creates an empty set of rules and initializes the dialog framework.
    * @param parent the parent frame of the dialog, may be <code>null</code>
    * @throws ClassNotFoundException if the class type passed to
    * PSCollection is not found.
    */
   public PSRuleEditorDialog(JFrame parent) throws ClassNotFoundException
   {
       super(parent);
       m_rules = PSRuleEditorTableModel.createNewCollection();
       m_udfSet = new PSUdfSet("ALL", E2Designer.getDesignerConnection());
       initDialog();
   }

   /**
    * Default Constructor. Creates an empty set of rules and initializes
    * the dialog framework.
    * @param frm the parent dialog of the dialog, may be <code>null</code>
    * @throws ClassNotFoundException if the class type passed to
    * PSCollection is not found.
    */
   public PSRuleEditorDialog(Frame frm) throws ClassNotFoundException
   {
       super(frm);
       m_rules = PSRuleEditorTableModel.createNewCollection();
       m_udfSet = new PSUdfSet("ALL",
           E2Designer.getDesignerConnection());
       initDialog();
   }

   /**
    * Create the Rule selector table. Defines cell editors for the type, rule
    * and operator column.
    *
    * @return JScrollPane the table view, a scrollable pane.
    * Never <code>null</code>.
    */
   private JScrollPane createTableView()
   {
      JComboBox typeCombo = new JComboBox();
      typeCombo.addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent event)
         {
            if (event.getStateChange() == event.DESELECTED)
            {
               mi_previousSelected = event.getItem().toString();
            }
            else
            {
               String test = event.getItem().toString();
               if (!test.equals(mi_previousSelected))
               {
                  int column = m_table.getColumn(
                     PSRuleEditorTableModel.RULE_COL_NAME).getModelIndex();
                  int row = m_table.getEditingRow();
                  if (row >= 0 && row < m_table.getRowCount())
                     m_table.getModel().setValueAt(null, row, column);
               }
            }
         }
         
         private String mi_previousSelected = null;
      });

      typeCombo.addItem(getResourceString(
         PSRuleEditorTableModel.RULE_TYPE_CONDITIONAL));
      typeCombo.addItem(getResourceString(
         PSRuleEditorTableModel.RULE_TYPE_EXTENSION));
      typeCombo.setSelectedIndex(0);

      // define the cell editors
      m_table.getColumn( PSRuleEditorTableModel.TYPE_COL_NAME ).setCellEditor(
         new DefaultCellEditor(typeCombo));
      m_table.getColumn( PSRuleEditorTableModel.TYPE_COL_NAME ).
         setPreferredWidth(80);
      m_table.getColumn( PSRuleEditorTableModel.RULE_COL_NAME ).setCellEditor(
         new PSRuleEditorTableCellEditor(m_udfSet));
      m_table.getColumn( PSRuleEditorTableModel.RULE_COL_NAME ).
         setPreferredWidth(300);
      m_table.getColumn( PSRuleEditorTableModel.OPERATOR_COL_NAME ).
         setCellEditor(
         new DefaultCellEditor(new UTBooleanComboBox()));
      m_table.getColumn( PSRuleEditorTableModel.OPERATOR_COL_NAME ).
         setPreferredWidth(40);

      JScrollPane pane = new JScrollPane(m_table);
      pane.setPreferredSize(new Dimension(420, 210));
      return pane;
   }

   /**
    * Create command panel. This contains the ok, cancel and help
    * buttons.
    *
    * @return JPanel the command panel. Never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      m_commandPanel =
          new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
         // implement onOk action
         public void onOk()
         {
            PSRuleEditorDialog.this.onOk();
         }

         public void onCancel()
         {
            PSRuleEditorDialog.this.onCancel();
         }
      };

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(m_commandPanel, BorderLayout.EAST);
      // set the default button
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      return panel;
  }

   /**
    * Initialize the dialogs framework laying out the necesary components.
    */
   private void initDialog()
   {
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         //This should not happen as it comes from the swing package
         e.printStackTrace();
      }
      JPanel panel = new JPanel(new BorderLayout());
      JPanel bottomPanel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
      bottomPanel.add(createCommandPanel(), "East");
      panel.add(createTableView(), "Center");
      panel.add(bottomPanel, "South");

      // Handle key events
      m_table.addKeyListener(new KeyAdapter()
      {
          public void keyReleased(KeyEvent event)
          {


             int[] rows = m_table.getSelectedRows();
             if(event.getKeyCode() == KeyEvent.VK_DELETE)
             {
                if(m_model.allowRemove())
                {
                    m_model.removeRows(rows);
                    m_table.setRowSelectionInterval(rows[0], rows[rows.length-1]);
                }
             }
             else if(m_model.allowMove())
             {
                if(event.isControlDown() &&
                      event.getKeyCode() == KeyEvent.VK_U && rows[0] != 0)
                {
                     //destination is before the first index
                     int dest = rows[0]-1;
                     m_model.moveRow(rows[0], rows[rows.length-1], dest);
                     m_table.setRowSelectionInterval(dest, dest+rows.length-1);
                }
                else if(event.isControlDown() &&
                          event.getKeyCode() == KeyEvent.VK_D &&
                            rows[rows.length-1] < m_model.getActualRowCount()-1)
                {
                     //destination is after the next of last index
                     int dest = rows[rows.length-1]+2;

                     m_model.moveRow(rows[0], rows[rows.length-1], dest);

                     m_table.setRowSelectionInterval(dest-rows.length, dest-1);
                }
             }

          }
      }
      );

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);
      setResizable(true);
      pack(); 
   }


   /**
    * Replaces the current set of rules with the supplied set for editing.
    * @param rules an Iterator containing PSRule objects. May be <code>null</code>
    * or empty. Only the first call in each extension set is considered.
    *
    * @throws ClassNotFoundException if the class type passed to
    * PSCollection is not found.
    */
   public void onEdit(Iterator rules) throws ClassNotFoundException
   {
      PSCollection rulesColl = PSRuleEditorTableModel.createNewCollection();
      if(null != rules)
      {
         while(rules.hasNext())
         {
            rulesColl.add(rules.next());
         }
      }
      m_model.loadFromRules(rulesColl);
      m_rules = rulesColl;
      m_cancelHit = false;

   }

   /**
    * Validates user entered data in table and displays appropriate error
    * messages to the user in the case of invalid data. Saves the data in
    * this object and disposes the dialog. User can extract this data
    * {@link #getRules()} or {@link #getRulesIterator()} after the control
    * is returned to user from this dialog by checking {@link #wasCancelHit()}
    * or not.
    */
   public void onOk()
   {
      stopTableEditor(m_table);

      // Validate the table data
      PSTableCellValidationError error = m_model.validate();
      if ( null != error )
      {
         ErrorDialogs.showErrorMessage(
            this,error.getErrorText(),
               getResourceString( "error"));

         m_table.getSelectionModel().clearSelection();
         m_table.editCellAt( error.getErrorRow(), error.getErrorCol());
         Component comp = m_table.getEditorComponent();
         if(null != comp)
            comp.requestFocus();
         return;
      }

      try
      {
         // Store the data in m_rules
         PSCollection rules = PSRuleEditorTableModel.createNewCollection();
         Iterator it =  m_model.getData();
         while(it.hasNext())
         {
            rules.add(it.next());
         }
         m_rules = rules;
         super.onOk();
      }
      catch(ClassNotFoundException e)
      {
        ErrorDialogs.showErrorMessage(
            this,e.getMessage(),
               getResourceString( "ValidationErrorTitle"));
      }
   }

   /**
    * Sets the cancelHit flag and calls the super classes onCancel method.
    */
   public void onCancel()
   {
      m_cancelHit = true;
      super.onCancel();
   }

   /**
    * Returns an Iterator of the collection of rules
    * @return iterator containing PSRule objects. Never <code>null</code>,
    * may be empty.
    */
   public Iterator getRulesIterator()
   {
      return m_rules.iterator();
   }

   /**
    * Returns an PSCollection of the collection of rules
    * @return PSCollection containing PSRule objects. Never <code>null</code>,
    * may be empty.
    */
   public PSCollection getRules()
   {
      return m_rules;
   }

   /**
    * Indicates whether or not the cancel button was hit
    * @return <code>true</code> if cancel was hit, else <code>false</code>.
    */
   public boolean wasCancelHit()
   {
      return m_cancelHit;
   }
    
   /**
    * Overridden to avoid obfuscation errors if this is called from an inner
    * class. See {@link PSDialog#getResourceString(String)} for details.
    */
   protected String getResourceString(String key)
   {
      return super.getResourceString(key);
   }

   /**
    * The table cell editor that supports the diffent views in the rule
    * column.
    */
   private class PSRuleEditorTableCellEditor extends AbstractCellEditor
     implements TableCellEditor, ItemListener, ActionListener
   {
      /**
       *  Constructs this editor and initializes all of its components
       */
      public PSRuleEditorTableCellEditor(PSUdfSet udfSet)
      {
         m_conditionalEditor = new TextFieldButtonEditor(this);
         m_udfSelectionEditor = new JComboBox();
         m_udfEditor = new TextFieldButtonEditor(this);
         m_udfSet = udfSet;
         Iterator udfs = m_udfSet.getAllUdfs().iterator();
         while (udfs.hasNext())
         {
            m_udfSelectionEditor.addItem(
               new OSExtensionCall((IPSExtensionDef) udfs.next(), null));
         }

         m_udfSelectionEditor.addItemListener(this);

         m_extEditButton = new UTBrowseButton();

         m_extensionEditor = new JPanel();
         m_extensionEditor.setLayout(new BorderLayout());
         m_extensionEditor.add(m_udfSelectionEditor, BorderLayout.CENTER);
         m_extensionEditor.add(m_extEditButton, BorderLayout.EAST);
      }
      
      /**
       * Will display the Extension editor dialog if the user selects
       * a new item from the extension comboBox.
       * @param e the ItemEvent passed in
       */
      public void itemStateChanged(ItemEvent e)
      {
         if (e.getSource() == m_udfSelectionEditor)
         {
            if (e.getStateChange() == ItemEvent.SELECTED && 
               m_udfSelectionEditor.isShowing())
            {
               OSExtensionCall extCall = (OSExtensionCall) e.getItem();
               if (m_value != null && (m_value instanceof OSExtensionCall))
                  extCall = (OSExtensionCall) m_value;
      
               if (extCall.getExtensionDef().getRuntimeParameterNames().
                  hasNext())
               {
                  FormulaPropertyDialog udfDlg = new FormulaPropertyDialog(
                     PSRuleEditorDialog.this, m_udfSet, null, null);
                  udfDlg.center();
                  udfDlg.onEdit(extCall, true, false);
                  udfDlg.setVisible(true);
                  if (!udfDlg.isCancelHit())
                     m_value = udfDlg.getUdfCall();
      
                  stopCellEditing();
               }
            }
         }
         else
         {
            stopCellEditing();
         }
      }
      
      /**
       * When an action aevent is captured stop cell editing.
       * @param e the ActionEvent passed in
       */
      public void actionPerformed(ActionEvent e)
      {
         stopCellEditing();
      }

      /**
       * This is where we determine if a mouse double click occurred on a table
       * cell.
       * @param event the EventObject captured.
       * @return <code>true</code> if cell is Editable, else <code>false</code>.
       */
      public boolean isCellEditable(EventObject event)
      {
         if (event instanceof MouseEvent)
         {
            MouseEvent e = (MouseEvent)event;
            int clickCount = 2;
            
            return e.getClickCount() >= clickCount;
         }
         
         return false;
      }

      // implements interface method
      public Object getCellEditorValue()
      {
         Object data = null;

         if (m_curEditorComponent == m_conditionalEditor)
         {
            data = m_conditionalEditor.getValue();
         }
         else if (m_curEditorComponent == m_udfEditor)
         {
            data = m_udfEditor.getValue();
         }
         else if (m_curEditorComponent == m_udfSelectionEditor)
         {
            data = m_value;
         }

         return data;
      }

      /**
       * This method clears all components then return the appropriate editor
       * component for the indicted row and column.
       * See {@link super.getTableCellEditorComponent(JTable, Object, boolean,
       *    int, int)}
       * @param table the table that we are editing
       * @param value the value of the selected cell
       * @param isSelected <code>true</code> if the this component
       *    is selected
       * @param row the cell row index
       * @param column the cells column index
       * @return the editor component
       */
      public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected, int row, int column)
      {
         // clear component fields
         m_conditionalEditor.setValue(null);
         m_udfSelectionEditor.setSelectedItem(null);

         m_value = value;

         // Get the type value for this row
         String ruleType = (String) table.getValueAt(row,
            PSRuleEditorTableModel.TYPE_COL_INDEX);
            
         // Default type to conditional if not set
         if (null == ruleType || ruleType.length() == 0)
         {
            ruleType = getResourceString(
               PSRuleEditorTableModel.RULE_TYPE_CONDITIONAL);
            table.setValueAt(ruleType, row, 
               PSRuleEditorTableModel.TYPE_COL_INDEX);
         }

         if (ruleType.equalsIgnoreCase(getResourceString(
            PSRuleEditorTableModel.RULE_TYPE_CONDITIONAL)))
         {
            m_curEditorComponent = m_conditionalEditor;
            if (null != m_value)
               m_conditionalEditor.setValue(m_value);
         }
         else
         {
            if (m_value == null)
            {
               m_curEditorComponent = m_udfSelectionEditor;
               m_udfSelectionEditor.setSelectedItem(m_value);
            }
            else
            {
               m_curEditorComponent = m_udfEditor;
               m_udfEditor.setValue(m_value);
            }
         }

         return m_curEditorComponent;
      }

      /**
       * Return current value object
       * @return the current value object. May be <code>null</code>.
       */
      public Object getValue()
      {
         return m_value;
      }

      /**
       * The text field editor to edit string values, initialized in the
       * constructor and never <code>null</code> after that. Its display text
       * gets set as per the cell value that this is used for.
       */
      private TextFieldButtonEditor m_conditionalEditor;

      /**
       * The combo-box field editor to edit extensions, initialized in the
       * constructor and never <code>null</code> after that.
       */
      private JComboBox m_udfSelectionEditor;
      private TextFieldButtonEditor m_udfEditor;

      /**
       * The button to edit extensions, initialized in
       * the constructor and never <code>null</code> after that. The extension
       * call that is being edited is stored in this button action and used as
       * value to edit or save.
       */
      private UTBrowseButton m_extEditButton;

      /**
       * The panel to edit extensions or effects, initialized in the constructor
       * and never <code>null</code> or modified after that.
       */
      private JPanel m_extensionEditor;

      /**
       * The component that is used to edit the current cell being edited is
       * stored in this and is used later to give back the editor value when the
       * editing is stopped.
       */
      private Component m_curEditorComponent = null;

      /**
      * Udf set to retrieve a set of UDF extensions. Initialized in the
      * constructor, never <code>null</code> after that.
      */
      private PSUdfSet m_udfSet = null;

      /**
       *  The value for this cell.
       *  May be <code>null</code>.
       */
      private Object m_value = null;
   }

   /**
    * The class which is used as a component for cell editing. This
    * is actually a panel with a text field and a button.
    */
   private class TextFieldButtonEditor extends UTEditorComponent
      implements ActionListener
   {
   
      /**
       * Constructor
       * Creates a new TextFieldButtonEditor component that is contains
       * a textfield and button.
       *
       * @param editor a reference to the table cell editor. Should not
       * be <code>null</code>.
       */
      private TextFieldButtonEditor(PSRuleEditorTableCellEditor editor)
      {
         m_editor = editor;
         m_textField = new JTextField();
         m_textField.setEditable(false);
         m_textField.setBackground(Color.white);
         m_button = new UTBrowseButton();
   
         setLayout(new BorderLayout());
         add(m_textField, BorderLayout.CENTER);
         add(m_button, BorderLayout.EAST);
   
         m_textField.addActionListener(this);
         m_button.addActionListener(this);
      }
   
      /**
       * Returns the editor component for this editor
       * @return JComponent that is the editor component.
       */
      public JComponent getEditorComponent()
      {
         return m_textField;
      }
   
      /**
       * Sets the text field text string for this editor
       * @param text the string passed in.
       */
      public void setText(String text)
      {
         m_textField.setText(text);
      }
   
      /**
       * Returns the text in the text field for this editor
       * @return String of text from text field
       */
      public String getText()
      {
         return m_textField.getText();
      }
   
      /**
       * Adds the passed in listener to the the component on whose action
       * the cell editing to be stopped.
       *
       * @param listener the action listener, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if <code>l</code> is <code>null</code>
       */
      public void addActionListener(ActionListener listener)
      {
         if (null == listener)
            throw new IllegalArgumentException("Listener must not be null.");
         m_button.addActionListener(listener);
      }
   
      /**
       * Invokes the conditional editor dialog when the conditional button
       * is hit.
       * @param e the Action event caught
       */
      public void actionPerformed(ActionEvent e)
      {
         if (e.getSource() == m_button)
         {
            Object cellValue = m_editor.getValue();
            if (cellValue instanceof OSExtensionCall)
            {
               OSExtensionCall extCall = (OSExtensionCall) cellValue;
               if (extCall.getExtensionDef().getRuntimeParameterNames().
                  hasNext())
               {
                  FormulaPropertyDialog udfDlg = new FormulaPropertyDialog(
                     PSRuleEditorDialog.this, m_udfSet, null, null);
                  udfDlg.center();
                  udfDlg.onEdit(extCall, true, false);
                  udfDlg.setVisible(true);
                  if (!udfDlg.isCancelHit())
                     setValue(udfDlg.getUdfCall());
               }
            }
            else
            {
               ConditionalPropertyDialog dlg = new ConditionalPropertyDialog();
               PSCollection conditionals = null;
               if (cellValue instanceof PSCollection)
                  conditionals = (PSCollection) cellValue;
               dlg.center();
               dlg.onEdit(conditionals, null, null);
               ((UTTableModel) dlg.getTable().getModel()).setMinRows(15);
               dlg.setVisible(true);
               PSConditionalSet conds = (null != dlg.getConditionals()) ? 
                  new PSConditionalSet(dlg.getConditionals()) : 
                  new PSConditionalSet();
               setValue(conds);
            }
            
            m_editor.stopCellEditing();
         }
      }
   
      /**
       * Get the value for this component. May be <code>null</code>.
       */
      public Object getValue()
      {
         return m_value;
      }
   
      /**
       * Set the value for this component
       * @param value the value. May be <code>null</code>.
       */
      public void setValue(Object value)
      {
         m_value = value;
         if (null != m_value)
         {
            setText(m_value.toString());
         }
         else
         {
            setText("");
         }
      }
   
      /**
       *  The text field component. Instantiated in the constructor
       *  never <code>null</code> after that.
       */
      private JTextField m_textField = null;
   
      /**
       *  The button component. Instantiated in the constructor
       *  never <code>null</code> after that.
       */
      private UTBrowseButton m_button = null;
   
      /**
       * The value object. Set in {@link #setValue(Object)}.
       * May be <code>null</code>.
       */
      private Object m_value;
   
      /**
       * The cell editor that owns this component. Set in ctor, never
       * <code>null</code> after that.
       */
      private PSRuleEditorTableCellEditor m_editor = null;
   }

   /**
    *  The rule editors table model. Never <code>null</code>.
    */
   private PSRuleEditorTableModel m_model = new PSRuleEditorTableModel(null);

   /**
    *  The rule editors table. Never <code>null</code>.
    */
   private UTJTable m_table = new UTJTable(m_model);

   /**
    * The standard command panel, initialized in {@link #createCommandPanel()},
    * never <code>null</code> after that.
    */
   private UTStandardCommandPanel m_commandPanel = null;

   /**
    * The PSCollection of PSRule objects.  Initialized in constructor.
    * Never <code>null</code>.
    */
   private PSCollection m_rules = null;

   /**
    * Indicates that the on cancel button was pressed
    */
   private boolean m_cancelHit = false;

   /**
    * Udf set to retrieve a set of UDF extensions. Initialized in the
    * constructor, never <code>null</code> after that.
    */
   private PSUdfSet m_udfSet = null;


}

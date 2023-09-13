/******************************************************************************
 * [ PSConditionalExitPanel.java ]
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplyWhen;
import com.percussion.design.objectstore.PSConditionalExit;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A panel used to edit conditional exits.
 */
public class PSConditionalExitPanel extends JPanel 
   implements KeyListener, ItemListener, ListSelectionListener
{
   /**
    * Constructs a new panel for the supplied parameters..
    */
   public PSConditionalExitPanel(JDialog parent, Collection exitDefinitions,
      Collection conditionalExits, int maxErrorsToStop)
   {
      this(parent, exitDefinitions, conditionalExits, maxErrorsToStop, 
         null, null);
   }

   /**
    * Constucts a new panel for the supplied parameters.
    * 
    * @param parent the parent for this panel, used as the parent for all
    *    dialogs used within this panel, not <code>null</code>.
    * @param exitDefinitions a collection of exit definitions, may be
    *    <code>null</code> or empty. 
    * @param conditionalExits a collection of conditional exit to initialize
    *    this panel with, may be <code>null</code> or empty.
    * @param maxErrorsToStop the maximal number of errors allowed, must be
    *    > 0 to enable the maximal number of errors panel.
    * @param preferredTablePanelSize the preferred size for the extensions
    *    panel, may be <code>null</code> in which case the default is used.
    * @param preferredDescriptionPanelSize the preferred size for the 
    *    description panel, may be <code>null</code> in which case the 
    *    default is used.
    */
   public PSConditionalExitPanel(JDialog parent, Collection exitDefinitions,
      Collection conditionalExits, int maxErrorsToStop,
      Dimension preferredTablePanelSize, 
      Dimension preferredDescriptionPanelSize)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent cannot be null");
      m_parent = parent;
      
      if (preferredTablePanelSize != null)
         m_preferredTablePanelSize = preferredTablePanelSize;
      
      if (preferredDescriptionPanelSize != null)
         m_preferredDescriptionPanelSize = preferredDescriptionPanelSize;
      
      initPanel(maxErrorsToStop);
      initData(exitDefinitions, conditionalExits, maxErrorsToStop);
   }
   
   /**
    * @see KeyListener#keyPressed(KeyEvent)
    */
   public void keyPressed(KeyEvent event)
   {
      // noop
   }

   /**
    * @see KeyListener#keyReleased(KeyEvent)
    */
   public void keyReleased(KeyEvent event)
   {
      switch (event.getKeyCode())
      {
         case KeyEvent.VK_ESCAPE:
            m_table.clearSelection();
            event.consume();
            break;
            
         case KeyEvent.VK_DELETE:
            onDelete();
            break;
      }
   }

   /**
    * @see KeyListener#keyTyped(KeyEvent)
    */
   public void keyTyped(KeyEvent event)
   {
      // noop
   }
   
   /**
    * Handles change events for the table cell editor used in the extensions
    * column.
    *
    * {@link ItemListener#itemStateChanged(ItemEvent)}
    */
   public void itemStateChanged(ItemEvent event)
   {
      if (event.getSource() == m_extensionsEditor)
      {
         JComboBox source = (JComboBox) event.getSource();
         if (event.getStateChange() == ItemEvent.SELECTED && source.isShowing())
         {
            Object item = event.getItem();
            if (item instanceof PSDataContainer)
            {
               PSDataContainer container = (PSDataContainer) item;
            
               OSExitCallSet callSet = new OSExitCallSet();
               callSet.setExtension(container.getExtensionDef(), 
                  (String) container.getExtensionDef().getInterfaces().next());
                  
               PSDataContainer data = new PSDataContainer(
                  container.getExtensionDef(), callSet);                  
               if (onEditExtension(data))
               {
                  source.getAction().putValue(EXTENSIONS, data);
                  source.setSelectedIndex(0);

                  onValueChanged();
            
                  addEmptyRow();
               }
            }
         }
      }
   }
   
   /**
    * Updates the description if the table selection changed.
    * 
    * @see javax.swing.event.ListSelectionListener#valueChanged(
    *    javax.swing.event.ListSelectionEvent)
    */
   public void valueChanged(ListSelectionEvent event)
   {
      if (!event.getValueIsAdjusting())
         onValueChanged();
   }
   
   /**
    * Method to retrieve all conditional exits currently defined in this
    * panel.
    * 
    * @return a collection of <code>PSConditionalExit</code> objects, never
    *    <code>null</code>, may be empty.
    */
   public PSCollection getConditionalExits()
   {
      PSCollection exits = new PSCollection(PSConditionalExit.class);
      
      int rows = m_table.getRowCount();
      int columns = m_table.getColumnCount();
      for (int row=0; row<rows; row++)
      {
         PSConditionalExit exit = null;
         for (int column=0; column<columns; column++)
         {
            Object cell = m_table.getValueAt(row, column);
            if (cell instanceof PSDataContainer)
            {
               PSDataContainer container = (PSDataContainer) cell;
               exit = new PSConditionalExit(container.mi_extensionCalls);
               
            }
            else if (cell instanceof ArrayList)
            {
               PSApplyWhen condition = new PSApplyWhen();
               condition.addAll((ArrayList) cell);
               
               exit.setCondition(condition);
            }
         }

         if (exit != null)
            exits.add(exit);
      }
      
      return exits;
   }
   
   /**
    * Get the maximal number of errors before to stop an operation. Throws
    * an <code>IllegalStateException</code> exception if this is used but the 
    * panel was constructed without requesting this field to be visible. Also 
    * throws an <code>IllegalStateException</code> exception if the user input 
    * cannot be parsed into an integer and was not
    * called before.
    * 
    * @return the maximal number of errors allowed, always > 0.
    */
   public int getMaxErrorsToStop()
   {
      int maxErrorsToStop = -1;
      
      if (m_maxErrors == null)
         throw new IllegalStateException(
            "maxErrorsToStop must be requested at constuction time");
            
      if (m_maxErrors.getText().trim().length() > 0)
      {
         try
         {
            maxErrorsToStop = Integer.parseInt(m_maxErrors.getText());
         }
         catch (NumberFormatException e)
         {
            throw new IllegalStateException(
               "validateData() should be called before this");
         }
      }
      
      return maxErrorsToStop;
   }
   
   /**
    * Validates all panel data and displays an appropriate error message if
    * data is invalid.
    * 
    * @return <code>true</code> if all data is valid, <code>false</code>
    *    otherwise.
    */
   public boolean validateData()
   {
      if (m_maxErrors != null && m_maxErrors.getText().trim().length() > 0)
      {
         int maxErrors = -1;
         try
         {
            maxErrors = Integer.parseInt(m_maxErrors.getText());
         }
         catch (NumberFormatException e)
         {
            PSDlgUtil.showErrorDialog(
               getResource("error.msg.invalidnumber"), 
               getResource("error.title"));
               
            return false;
         }
         
         if (maxErrors <= 0)
         {
            PSDlgUtil.showErrorDialog(
               getResource("error.msg.invalidnumber"), 
               getResource("error.title"));
               
            return false; 
         }
      }

      return true;
   }
   
   /**
    * Call this method to update the description text area with the info of
    * the selected table row.
    */
   private void onValueChanged()
   {
      int row = m_table.getSelectedRow();
      
      if (row >= 0)
      {
         Object value = m_table.getValueAt(m_table.getSelectedRow(), 
            EXTENSIONS_COLUMN);
         if (value instanceof PSDataContainer)
         {
            PSDataContainer data = (PSDataContainer) value;
            m_description.setText(data.getExtensionDef().getInitParameter(
               IPSExtensionDef.INIT_PARAM_DESCRIPTION));
          }
         else
            m_description.setText("");
      }
      else
         m_description.setText("");
   }

   /**
    * Call this method to edit an extensions parameter values for the supplied 
    * data. The editor is only started if the extension definition specifies
    * parameters.
    * 
    * @param data the extension data, may be <code>null</code>.
    * @return <code>true</code> if no edit was required or the editor was left
    *    using the OK button, <code>false</code> otherwise.
    */
   private boolean onEditExtension(Object data)
   {
      boolean ok = false;
      
      if (data instanceof PSDataContainer)
      {
         PSDataContainer container = (PSDataContainer) data;
         OSExitCallSet exits = container.getExtensionCalls();
         if (exits != null)
         {
            IPSExtensionDef exitDefinition = container.getExtensionDef();
            if (exitDefinition.getRuntimeParameterNames().hasNext())
            {
               JavaExitsPropertyDialog dlg = new JavaExitsPropertyDialog(
                  m_parent, exits, false, 
                  getResource("exits.parameter.dialog.title"));
               dlg.setVisible(true);
               
               ok = dlg.wasOkExit(); 
            }
            else
               ok = true;
         }
      }
      
      if (ok)
         m_modified = true;
      
      return ok;
   }
   
   /**
    * Add an empty row to the table. The data elements are initialized to
    * and <code>null</code>.
    */
   private void addEmptyRow()
   {
      DefaultTableModel model = (DefaultTableModel) m_table.getModel();
      
      String[] rowData = { getResource("exits.item.default"), null };
      model.addRow(rowData);
   }
   
   /**
    * Initialize the panels UI.
    * 
    * @param maxErrorsToStop an integer > 0 to enable this field, <= 0 to
    *    disable it.
    */
   protected void initPanel(int maxErrorsToStop)
   {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      
      String[] columnNames = 
      {
         getResource("table.column.extensions"),
         getResource("table.column.conditionals")
      };
      
      add(createTablePanel(columnNames, 0));
      addEmptyRow();
      add(Box.createVerticalStrut(10));
      add(Box.createVerticalGlue());
      add(createDescriptionPanel());
      if (maxErrorsToStop > 0)
      {
         add(Box.createVerticalStrut(10));
         add(Box.createVerticalGlue());
         add(createMaxErrorsPanel());
      }
   }
   
   /**
    * Initialize the panel with the supplied data.
    * 
    * @param exitDefinitions all extension definitions allowed for this 
    *    panel, may be <code>null</code>.
    * @param conditionalExits all conditional exits with which to initialize
    *    the panel table, may be <code>null</code>.
    * @param maxErrorsToStop the maximal number of errors to initialize the
    *    panel with.
    */
   protected void initData(Collection exitDefinitions, 
      Collection conditionalExits, int maxErrorsToStop)
   {
      if (exitDefinitions != null)
      {
         Iterator exitWalker = exitDefinitions.iterator();
         m_extensionsEditor.addItem(getResource("exits.item.default"));
         while (exitWalker.hasNext())
         {
            IPSExtensionDef extensionDef = (IPSExtensionDef) exitWalker.next();
            m_extensionsEditor.addItem(new PSDataContainer(extensionDef, null));
         }
      }
      
      if (conditionalExits != null)
      {
         int rowIndex = 0;
         Iterator entries = conditionalExits.iterator();
         while (entries.hasNext())
         {
            PSConditionalExit entry = (PSConditionalExit) entries.next();
            
            OSExtensionCall call = new OSExtensionCall(
               (PSExtensionCall) entry.getRules().get(0));
               
            OSExitCallSet callSet = new OSExitCallSet();
            callSet.add(call, 
               (String) call.getExtensionDef().getInterfaces().next());
            
            PSDataContainer container = new PSDataContainer(
               call.getExtensionDef(), callSet);
               
            List conditions = new ArrayList();
            if (entry.getCondition() != null)
               conditions = new ArrayList(entry.getCondition());
               
            Object[] rowData =
            {
               container,
               conditions
            };
               
            DefaultTableModel model = (DefaultTableModel) m_table.getModel();
            model.insertRow(rowIndex++, rowData);
         }
         
         if (m_maxErrors != null)
            m_maxErrors.setText(Integer.toString(maxErrorsToStop));
      }
   }
   
   /**
    * Creates a panel with a table view to show all currently selected
    * extentsions and conditions.
    * 
    * @param columnNames an array of <code>String</code> objects with all
    *    column names in the order from left to right, not <code>null</code>
    *    or empty.
    * @param rows the number of empty rows to be initialized.
    * @return the table panel, never <code>null</code>.
    */
   protected JPanel createTablePanel(Object[] columnNames, int rows)
   {
      if (columnNames == null)
         throw new IllegalArgumentException("columnNames cannot be null");
         
      if (columnNames.length == 0)
         throw new IllegalArgumentException("columnNames cannot be empty");
                  
      DefaultTableModel model = new DefaultTableModel(columnNames, rows);
      
      m_table = new JTable(model);
      m_table.getSelectionModel().addListSelectionListener(this);
      m_table.setCellSelectionEnabled(false);
      m_table.setColumnSelectionAllowed(false);
      m_table.setRowSelectionAllowed(true);
      m_table.addKeyListener(this);
      
      for (int i=0; i<m_table.getColumnCount(); i++)
         m_table.getColumnModel().getColumn(i).setCellRenderer(
            new PSTableCellRenderer());
            
      m_extensionsEditor.addItemListener(this);    
      PSComboBoxTableCellEditor extensionsEditor = 
         new PSComboBoxTableCellEditor(m_extensionsEditor);
      extensionsEditor.setClickCountToStart(2);         
      TableColumn extensionsColumn = m_table.getColumnModel().getColumn(
         EXTENSIONS_COLUMN);
      extensionsColumn.setCellEditor(extensionsEditor);

      PSButtonTableCellEditor conditionalsEditor = new PSButtonTableCellEditor(
         m_conditionalsEditor);
      conditionalsEditor.setClickCountToStart(2);         
      TableColumn conditionalsColumn = m_table.getColumnModel().getColumn(
         CONDITIONALS_COLUMN);
      conditionalsColumn.setCellEditor(conditionalsEditor);
      conditionalsColumn.setPreferredWidth(20);
      conditionalsColumn.setMinWidth(20);
      conditionalsColumn.setMaxWidth(20);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      panel.add(new JScrollPane(m_table), BorderLayout.CENTER);
      panel.setPreferredSize(m_preferredTablePanelSize);
      
      return panel;
   }
   
   /**
    * Create the description panel.
    * 
    * @return the newly created description panel, never <code>null</code>.
    */
   protected JPanel createDescriptionPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());

      m_description.setLineWrap(true);
      m_description.setEditable(false);
      m_description.setBackground(panel.getBackground());
      m_description.setForeground(panel.getForeground());
      m_description.setLineWrap(true);
      m_description.setWrapStyleWord(true);

      JScrollPane scroll = new JScrollPane(m_description);
      scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
      scroll.setBorder(
         BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(getResource("border.description")),
            BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
      scroll.setPreferredSize(m_preferredDescriptionPanelSize);

      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      panel.add(scroll, BorderLayout.CENTER);

      return panel;
   }
   
   /**
    * Create the panel to edit the maximum number of errors before stopping
    * the action.
    * 
    * @return the newly created panel, never <code>null</code>.
    */
   protected JPanel createMaxErrorsPanel()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      m_maxErrors = new JTextField();
      Dimension fixedSize = new Dimension(40, 
         m_maxErrors.getPreferredSize().height);
      m_maxErrors.setPreferredSize(fixedSize);
      m_maxErrors.setMinimumSize(fixedSize);
      m_maxErrors.setMaximumSize(fixedSize);
         
      panel.add(new JLabel(getResource("ctrl.maxerrors")));
      panel.add(m_maxErrors);
         
      return panel;
   }
   
   /**
    * Get the requested resource string.
    * 
    * @param key the resource key, assumed not <code>null</code>.
    * @return the found resource as <code>String</code>, 
    *    never <code>null</code>.
    * @throws MissingResourceException if the requested resource cannot be
    *    found.
    */
   private String getResource(String key) throws MissingResourceException
   {
      return ms_resources.getString(key);
   }
   
   /**
    * Delete all currently selected table rows.
    */
   private void onDelete()
   {
      int[] rows = m_table.getSelectedRows();
      
      m_table.clearSelection();
      for (int i=rows.length-1; i>=0; i--)
      {
         int row = rows[i];
         m_table.getCellEditor(row, EXTENSIONS_COLUMN).stopCellEditing();
         
         DefaultTableModel model = (DefaultTableModel) m_table.getModel();
         Object value = model.getValueAt(row, EXTENSIONS_COLUMN);
         if (value instanceof PSDataContainer)
            model.removeRow(row);
      }
         
      m_modified = true;
   }

   /**
    * Overwrites the <code>DefaultTableCellRenderer</code> to support rendering
    * the conditional columns used for conditional exits.
    */
   protected class PSTableCellRenderer extends DefaultTableCellRenderer
   {
      /**
       * Construct a new conditional table cell renderer.
       */
      public PSTableCellRenderer()
      {
         mi_conditionsIcon = new ImageIcon(getClass().getResource(
            getResource("gif.conditionals")));
         mi_noConditionsIcon = new ImageIcon(getClass().getResource(
            getResource("gif.noconditionals")));
      }
   
      /**
       * Overridden for 2 reasons: 
       * <ol>
       *    <li>To render the exit conditions column using a 
       * <code>JButton</code> with an icon set depending on whether there are
       * conditions of not.</li>
       *    <li>To make the focused cell have the same background color as the
       *    other selected cells.</li>
       *    <li></li>
       * <ol>
       * <p>Implementation note: The code at the beginning of this method was
       * taken from the base class and only changed to make the cell w/ the
       * focus and selection be the same color as a selected cell w/o the 
       * focus.
       * 
       * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, 
       *    boolean, boolean, int, int)
       */
      public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
      {
         if (isSelected) 
         {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
         }
         else 
         {
            super.setForeground((mi_unselectedForeground != null) 
                  ? mi_unselectedForeground : table.getForeground());
            super.setBackground((mi_unselectedBackground != null) 
                  ? mi_unselectedBackground : table.getBackground());
         }
         
         setFont(table.getFont());
         
         if (hasFocus) 
         {
            setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
            if (table.isCellEditable(row, column)) 
            {
               if (isSelected)
               {
                  super.setForeground(table.getSelectionForeground());
                  super.setBackground(table.getSelectionBackground());
               }
               else
               {
                  super.setForeground( 
                        UIManager.getColor("Table.focusCellForeground") );
                  super.setBackground( 
                        UIManager.getColor("Table.focusCellBackground") );
               }
            }
         } 
         else 
         {
            setBorder(noFocusBorder);
         }
         setValue(value); 

         Component renderer = this;         
         if (column == CONDITIONALS_COLUMN)
         {
            Object data = table.getValueAt(row, EXTENSIONS_COLUMN);
            if (data instanceof PSDataContainer)
            {
               renderer = new JButton();
               if (value != null)
               {
                  List conditions = (List) value;
               
                  if (conditions.isEmpty())
                     ((JButton) renderer).setIcon(mi_noConditionsIcon);
                  else
                     ((JButton) renderer).setIcon(mi_conditionsIcon);
               }
               else
                  ((JButton) renderer).setIcon(mi_noConditionsIcon);
            }
         }
      
         return renderer;
      }
      
      /**
       * Overridden to remember the background color, which gets changed
       * for selection. If not available, the encompassing table's
       * background color will be used.
       *
       * @param c May be <code>null</code>.
       */
      public void setForeground(Color c) 
      {
         super.setForeground(c); 
         mi_unselectedForeground = c; 
      }
     
      /**
       * Overridden to remember the background color, which gets changed
       * for selection. If not available, the encompassing table's
       * background color will be used.
       *
       * @param c May be <code>null</code>.
       */
      public void setBackground(Color c) 
      {
         super.setBackground(c); 
         mi_unselectedBackground = c; 
      }

      /**
       * Stores the original color used for the foreground. We keep this around
       * because when this component is selected or focused, the color is
       * changed. May be <code>null</code>, which is the default value.
       */
      private Color mi_unselectedForeground = null;

      /**
       * Stores the original color used for the foreground. We keep this around
       * because when this component is selected or focused, the color is
       * changed. May be <code>null</code>, which is the default value.
       */
      private Color mi_unselectedBackground = null;

      /**
       * The icon used to represent that the cell has some conditions, 
       * initialized in constructor, never <code>null</code> or changed after 
       * that.
       */
      private ImageIcon mi_conditionsIcon = null;

      /**
       * The icon used to represent that the cell has no conditions, 
       * initialized in constructor, never <code>null</code> or changed after 
       * that.
       */
      private ImageIcon mi_noConditionsIcon = null;
   }
   
   /**
    * Overwrites the <code>DefaultCellEditor</code> to support an action and
    * special {@link #isCellEditable(EventObject)} handling.
    */
   protected class PSComboBoxTableCellEditor extends DefaultCellEditor 
   {
      /**
       * Constructs the editor with a no-op action to store the extension 
       * information.
       * 
       * @see DefaultCellEditor#DefaultCellEditor(JComboBox)}
       */
      public PSComboBoxTableCellEditor(JComboBox editorComponent)
      {
         super(editorComponent);
         
         AbstractAction extensionsAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent event)
            {
               // noop
            }
         };
         
         mi_editorComponent = editorComponent;
         mi_editorComponent.setAction(extensionsAction);
      }

      /**
       * Overwritten to determine if the cell is editable and start the
       * editor if it is.
       * 
       * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
       */
      public boolean isCellEditable(EventObject event)
      {
         if (event instanceof MouseEvent)
         {
            MouseEvent evt = (MouseEvent) event;
         
            if (evt.getSource() instanceof JTable)
            {
               JTable table = (JTable) evt.getSource();
               int row = table.rowAtPoint(evt.getPoint());
               Object value = table.getValueAt(row, EXTENSIONS_COLUMN);
               if (value instanceof PSDataContainer && 
                  evt.getClickCount() >= getClickCountToStart())
               {
                  PSDataContainer container = (PSDataContainer) value;
                  onEditExtension(container);
                                
                  return false;
               }
            }
         }

         return super.isCellEditable(event);
      }
      
      /**
       * Overwritten to use the value from the editor component action.
       * 
       * @see javax.swing.CellEditor#getCellEditorValue()
       */
      public Object getCellEditorValue()
      {
         return mi_editorComponent.getAction().getValue(EXTENSIONS);
      }

      /**
       * Overwritten to use the value from the editor component action.
       * 
       * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(
       *    javax.swing.JTable, java.lang.Object, boolean, int, int)
       */
      public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected, int row, int column)
      {
         mi_editorComponent.getAction().putValue(EXTENSIONS, value);
            
         return mi_editorComponent;
      }

      /**
       * The editor component, initialized while constructed, never 
       * <code>null</code> or changed after that.
       */
      private JComboBox mi_editorComponent = null;
   }

   /**
    * Overwrites the <code>AbstractCellEditor</code> to support an action and
    * special {@link #isCellEditable(EventObject)} handling.
    */
   protected class PSButtonTableCellEditor extends AbstractCellEditor 
      implements TableCellEditor
   {
      /**
       * Constructs a new cell editor for the supplied button.
       * 
       * @param editorComponent the editor component, not <code>null</code>.
       */
      public PSButtonTableCellEditor(JButton editorComponent)
      {
         if (editorComponent == null)
            throw new IllegalArgumentException(
               "editorComponent cannot be null");
               
         mi_editorComponent = editorComponent;
                        
         AbstractAction conditionalsAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent event)
            {
              List rules = (List) getValue(CONDITIONALS);

               if (rules == null)
                  throw new IllegalStateException("no conditions found");

               try
               {
                  PSRuleEditorDialog rulesDlg = new PSRuleEditorDialog(
                     m_parent);
                  rulesDlg.center();
                  rulesDlg.onEdit(rules.iterator());
                  rulesDlg.setVisible(true);
                  
                  if (rulesDlg.isOk())
                  {
                     rules.clear();
                     Iterator rulesWalker = rulesDlg.getRulesIterator();
                     while(rulesWalker.hasNext())
                        rules.add(rulesWalker.next());
                        
                     m_modified = true;
                  }
               }
               catch (ClassNotFoundException e)
               {
                  PSDlgUtil.showError(e, false, getResource("error.title"));
               }
               finally
               {
                  TableCellEditor editor = m_table.getColumnModel().getColumn(
                     CONDITIONALS_COLUMN).getCellEditor();
                  editor.stopCellEditing();
               }

               putValue(CONDITIONALS, rules);
            }
         };
         mi_editorComponent.setAction(conditionalsAction);
      }
      
      /**
       * Overwritten to use the value from the editor component action.
       * 
       * @see javax.swing.CellEditor#getCellEditorValue()
       */
      public Object getCellEditorValue()
      {
         return mi_editorComponent.getAction().getValue(CONDITIONALS);
      }

      /**
       * Overwritten to use the value from the editor component action.
       * 
       * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(
       *    javax.swing.JTable, java.lang.Object, boolean, int, int)
       */
      public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected, int row, int column)
      {
         Object localValue = value;
         if (localValue == null)
            localValue = new ArrayList();
            
         mi_editorComponent.getAction().putValue(CONDITIONALS, localValue);
         
         return mi_editorComponent;
      }
      
      /**
       * Get the number of clicks required to set cell into edit mode.
       * 
       * @return the number of cclicks requried to set cell into edit mode, 
       *    always >= 1.
       */
      public int getClickCount()
      {
         return mi_clickCount;
      }
      
      /**
       * Set the number of clicks requiredd to put cell into edit mode.
       * 
       * @param count the number of clicks required to put the cell into
       *    edit mode, set to 1 if a count of <= 0 is supplied.
       */
      public void setClickCountToStart(int count)
      {
         if (count < 1)
            count = 1;
            
         mi_clickCount = count;
      }
      
      /**
       * The cell is editable if a valid extension is available in the 
       * extensions column of the same row.
       * 
       * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
       */
      public boolean isCellEditable(EventObject event)
      {
         if (event instanceof MouseEvent)
         {
            MouseEvent evt = (MouseEvent) event;
            
            if (evt.getSource() instanceof JTable)
            {
               JTable table = (JTable) evt.getSource();
               Object value = table.getValueAt(table.rowAtPoint(evt.getPoint()), 
                  EXTENSIONS_COLUMN);
               if (value == null)
                  return false;
            }
            
            return evt.getClickCount() >= getClickCount();
         }

         return false;
      }

      /**
       * The editor component, initialized while constructed, never 
       * <code>null</code> or changedd after that.
       */
      private JButton mi_editorComponent = null;
      
      /**
       * The number of clicks required to put this cell into edit mode, always
       * >= 1. Can be modified through {@link #setClickCountToStart(int)}.
       */
      private int mi_clickCount = 1;
   }
   
   /**
    * A container to store all required data for the extensions column.
    */
   protected class PSDataContainer
   {
      /**
       * Constructor to initialize all data elements.
       * 
       * @param extensionDef the extension definition to initialize with, 
       *    not <code>null</code>.
       * @param extensionCalls the extension calls to initialized with, 
       *    may be <code>null</code>.
       */
      public PSDataContainer(IPSExtensionDef extensionDef, 
         OSExitCallSet extensionCalls)
      {
         setExtensionDef(extensionDef);
         setExtensionCalls(extensionCalls);
      }
      
      /**
       * Get the extension definition.
       * 
       * @return the extension definition, may be <code>null</code>.
       */
      public IPSExtensionDef getExtensionDef()
      {
         return mi_extensionDef;
      }
      
      /**
       * Set a new extension definition.
       * 
       * @param extensionDef the new extension definition, not 
       *    <code>null</code>.
       */
      public void setExtensionDef(IPSExtensionDef extensionDef)
      {
         if (extensionDef == null)
            throw new IllegalArgumentException("extensionDef cannot be null");
            
         mi_extensionDef = extensionDef;
      }
      
      /**
       * Get the extension calls.
       * 
       * @return the extension calls, may be <code>null</code>.
       */
      public OSExitCallSet getExtensionCalls()
      {
         return mi_extensionCalls;
      }
      
      /**
       * Set new extension calls.
       * 
       * @param extensionCalls the new extension calls, may be 
       *    <code>null</code>.
       */
      public void setExtensionCalls(OSExitCallSet extensionCalls)
      {
         mi_extensionCalls = extensionCalls;
      }
      
      /**
       * Overridden to show the extension name until one has been selected and
       * the extension name with runtime parameters otherwise.
       * 
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         if (getExtensionCalls() != null)
         {
            OSExtensionCall call = (OSExtensionCall) getExtensionCalls().get(0);
            if (call != null)
               return call.toString();
         }
            
         return getExtensionDef().toString();
      }

      /**
       * Storage for the extension definition. Initialized to <code>null</code>,
       * may be changed through {@link #setExtensionDef(IPSExtensionDef)}.
       */
      private IPSExtensionDef mi_extensionDef = null;

      /**
       * Storage for all extension calls. Initialized to <code>null</code>,
       * may be changed through {@link #setExtensionCalls(OSExitCallSet)}.
       */
      private OSExitCallSet mi_extensionCalls = null;
   }
    
   /**
    * The constant used as key to store the data object in the action attached
    * to the extensions combo box editor.
    */
   protected static final String EXTENSIONS = "extentsions";
   
   /**
    * The constant used as key to store the data object in the action attached
    * to the conditionals editor button.
    */
   protected static final String CONDITIONALS = "conditionals";
   
   /**
    * The index of the column that shows the extension.
    */
   protected static final int EXTENSIONS_COLUMN = 0;
   
   /**
    * The index of the column that shows the conditionals.
    */
   protected static final int CONDITIONALS_COLUMN = 1;
  
   /**
    * The resource bundle used to lookup all required renderer resources.
    * Initialized in constructor, never <code>null</code> or changed after that.
    */
   protected static ResourceBundle ms_resources = null;
   static
   {
      ms_resources = ResourceBundle.getBundle(
         PSConditionalExitPanel.class.getName() + "Resources", 
            Locale.getDefault());
   }
   
   /**
    * The parent dialog which owns this panel, initialized in constructor,
    * never <code>null</code> after that.
    */
   protected JDialog m_parent = null; 
   
   /**
    * The table to show all selected extensions and conditions. Initialized 
    * in {@link #createTablePanel(Object[], int)}, never <code>null</code>
    * after that.
    */
   protected JTable m_table = null;

   /**
    * The description panel which shows the description of the extension of 
    * the currently selecetd row. Initialized in 
    * {@link #createDescriptionPanel()}, never <code>null</code> after that.
    */   
   protected JTextArea m_description = new JTextArea();
   
   /**
    * A flag indicating whether or not the current panel was modified.
    */
   protected boolean m_modified = false;
   
   /**
    * The table editor used to edit extensions, initialized in 
    * {@link #initData(Collection, Collection, int)}, never <code>null</code> or
    * changed after that.
    */
   private JComboBox m_extensionsEditor = new JComboBox();
   
   /**
    * The table editor used to edit conditionals, initialized in 
    * {@link #createTablePanel(Object[], int)}, never <code>null</code> or
    * changed after that.
    */
   private JButton m_conditionalsEditor = new JButton();
   
   /**
    * The text field through which the maximal number of errors are edited
    * Initialized in {@linkcreateMaxErrorsPanel()}, may be <code>null</code>
    * if this field was not requested at consturction time.
    */
   private JTextField m_maxErrors = null;
   
   /**
    * The preferred size used for the table panel. Initialized with the default
    * size, may be overridden through constructor. Never <code>null</code>.
    */
   private Dimension m_preferredTablePanelSize = new Dimension(480, 120);
   
   /**
    * The preferred size used for the description panel. Initialized with the 
    * default size, may be overridden through constructor. Never 
    * <code>null</code>.
    */
   private Dimension m_preferredDescriptionPanelSize = new Dimension(480, 90);
}

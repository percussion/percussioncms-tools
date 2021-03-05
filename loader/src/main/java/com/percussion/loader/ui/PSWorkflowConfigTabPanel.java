/* *****************************************************************************
 *
 * [ PSWorkflowConfigTabPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSEntry;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSWorkflowDef;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/**
 * This class defines the workflow definition for an extractor. It is typically
 * used as one of the sub tab panels within the main configuration panel.
 */
public class PSWorkflowConfigTabPanel extends PSAbstractExtractorConfigTabPanel
   implements ItemListener
{
   /**
    * Constructs a new <code>PSWorkflowConfigTabPanel</code> object
    */
   public PSWorkflowConfigTabPanel()
   {
     init();
   }


   /**
    * Initializes the gui components for this panel
    */
   private void init()
   {
     if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      // Create border
      Border b = BorderFactory.createEmptyBorder(10, 10, 10, 10 );

      PSPropertyPanel bodyPanel = new PSPropertyPanel();
      bodyPanel.setAlignmentX(RIGHT_ALIGNMENT);
      bodyPanel.setLabelAlignment(LEFT_ALIGNMENT);
      bodyPanel.setBorder(b);

      // Workflow field
      m_workflowComboField = new JComboBox();
      m_workflowComboField.addItemListener(this);
      JComponent[] j1 = {m_workflowComboField};
      bodyPanel.addPropertyRow(ms_res.getString("field.label.workflow"), j1);

      // Transition performed on field
      m_performedOnComboField = new JComboBox();
      m_performedOnComboField.addItemListener(this);
      JComponent[] j2 = {m_performedOnComboField};
      bodyPanel.addPropertyRow(
         ms_res.getString("field.label.trans.performed.on"), j2);


      // Transitions table panel
      m_transitionComboEditor = new JComboBox();
      m_cellEditor = new DefaultCellEditor(m_transitionComboEditor);
      m_cellEditor.setClickCountToStart(2);
      initializeTableModels();
      JPanel tablePanel = new JPanel(new BorderLayout());
      tablePanel.setBorder(b);
      tablePanel.setPreferredSize(new Dimension(100, 200));
      tablePanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
      Vector columnNames = new Vector(1);
      columnNames.add(ms_res.getString("field.label.transitions"));
      m_transTable = new WorkflowTable(columnNames);
      m_transTable.addKeyListener( new KeyAdapter()
      {
         // Removes selected row if delete key is hit
         public void keyReleased(KeyEvent event)
         {
           if(event.getKeyCode() == event.VK_DELETE)
           {
             DefaultTableModel model =
                (DefaultTableModel)m_transTable.getModel();
             m_cellEditor.stopCellEditing();
             int row = m_transTable.getSelectedRow();
             if(row < 0 || row >= model.getRowCount())
                return;
             model.removeRow(row);
           }

           if(event.getKeyCode() == event.VK_ENTER)
           {
               m_cellEditor.stopCellEditing();
           }
         }
      });
      JScrollPane scrollPane = new JScrollPane(m_transTable);
      tablePanel.add(scrollPane, BorderLayout.CENTER);
      add(bodyPanel);
      add(tablePanel);

   }

   /**
    * Initializes all table models
    */
   private void initializeTableModels()
   {
      String columnName = ms_res.getString("field.label.transitions");
      m_transInputTableModel = new DefaultTableModel();
      m_transInputTableModel.addColumn(columnName);
      addMinimumRows(m_transInputTableModel);
      m_transPreTableModel = new DefaultTableModel();
      m_transPreTableModel.addColumn(columnName);
      addMinimumRows(m_transPreTableModel);
      m_transPostTableModel = new DefaultTableModel();
      m_transPostTableModel.addColumn(columnName);
      addMinimumRows(m_transPostTableModel);
   }

   // implements IPSExtractorConfigTabPanel interface method
   public void load(PSExtractorConfigContext configCtx)
     throws PSLoaderException
   {
      if(null == configCtx)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");
      m_configCtx = configCtx;
      m_isLoading = true;

      try
      {
         // Load workflow combo box
         Iterator entries = configCtx.getWorkflows();
         m_workflowComboField.removeAllItems();
         while(entries.hasNext())
            m_workflowComboField.addItem(entries.next());
         if(configCtx.getWorkflow() != null)
            m_workflowComboField.setSelectedItem(configCtx.getWorkflow());

         // Load all transitions list
         Iterator trans =
            configCtx.getTransitions(
               (PSEntry)m_workflowComboField.getSelectedItem());
         m_allTransitions.clear();
         while(trans.hasNext())
         {
            PSEntry entry = (PSEntry)trans.next();
            m_allTransitions.add(new PSEntryWrapper(entry));
         }

         // Load transition performed on combo box
         Iterator groups = getTransitionGroups().iterator();
         m_performedOnComboField.removeAllItems();
         while(groups.hasNext())
            m_performedOnComboField.addItem(groups.next());
         m_performedOnComboField.setSelectedIndex(0);

         // Load each table model with selections
         loadTableModelSelections(m_transInputTableModel,
            PSWorkflowDef.TRANS_INSERT);
         loadTableModelSelections(m_transPreTableModel,
            PSWorkflowDef.TRANS_PREUPDATE);
         loadTableModelSelections(m_transPostTableModel,
            PSWorkflowDef.TRANS_POSTUPDATE);

         loadTableModel();
      }
      finally
      {
         m_isLoading = false;
      }

   }

   /**
    * Loads the proper model into the table based on the selection
    * in the "transition performed on" combo box.
    */
   private void loadTableModel()
   {
       TransitionGroup group =
          (TransitionGroup)m_performedOnComboField.getSelectedItem();
       switch(group.getValue())
       {
          case PSWorkflowDef.TRANS_INSERT:
             m_transTable.setModel(m_transInputTableModel);
             return;
          case PSWorkflowDef.TRANS_PREUPDATE:
             m_transTable.setModel(m_transPreTableModel);
             return;
          case PSWorkflowDef.TRANS_POSTUPDATE:
             m_transTable.setModel(m_transPostTableModel);
             return;

       }
   }

   /**
    * Loads selections into a table model
    * @param model the model to load. Never <code>null</code>.
    * @param transition the transition group
    */
   private void loadTableModelSelections(DefaultTableModel model, int transition)
   {
      if(null == model)
         throw new IllegalArgumentException("Model cannot be null.");

      model.getDataVector().clear();

      Iterator it = m_configCtx.getTransitions(transition);
      while(it.hasNext())
      {
         Vector row = new Vector(1);
         row.add(new PSEntryWrapper((PSEntry)it.next()));
         model.addRow(row);
      }
      addMinimumRows(model);
   }

   /**
    * Adds empty rows to ensure table has minimum required rows
    *
    * @param model the table model to which rows will be added.
    * Never <code>null</code>.
    */
   private void addMinimumRows(DefaultTableModel model)
   {
       if(null == model)
          throw new IllegalArgumentException("Model cannot be null.");

       for(int i = model.getRowCount(); i < MIN_ROWS; i++)
       model.addRow(new Vector(1));
   }

   // implements IPSExtractorConfigTabPanel interface method
   public void save(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      if(!validateContent())
         return;
      // update workflow
      config.setWorkflow((PSEntry)m_workflowComboField.getSelectedItem());

      // update transitions
      saveTransitions(config, m_transInputTableModel,
         PSWorkflowDef.TRANS_INSERT);
      saveTransitions(config, m_transPreTableModel,
         PSWorkflowDef.TRANS_PREUPDATE);
      saveTransitions(config, m_transPostTableModel,
         PSWorkflowDef.TRANS_POSTUPDATE);

   }

   /**
    * Saves the transitions from the table model into the config
    *
    * @param config the extractor configuration context,
    *    cannot be <code>null</code>.
    * @param model the transitions table model, cannot be <code>null</code>.
    * @param transition the transition group, assume it is one of the
    *    <code>PSWorkflowDef.TRANS_XXX</code> values.
    */
   private void saveTransitions(PSExtractorConfigContext config,
      DefaultTableModel model,
      int transition)
   {
      if(null == config)
         throw new IllegalArgumentException("Config context cannot be null.");
      if(null == model)
         throw new IllegalArgumentException("Model cannot be null.");

      List trans = new ArrayList();
      Enumeration e = model.getDataVector().elements();
      while(e.hasMoreElements())
      {
         Vector row = (Vector)e.nextElement();
         if(!row.isEmpty())
         {
            PSEntryWrapper entry = (PSEntryWrapper)row.get(0);
            if(null != entry)
               trans.add(entry.getEntry());
         }
      }
      config.setTransitions(trans, transition);

   }

   // implements IPSExtractorConfigTabPanel interface method
   public void reset(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      initializeTableModels();
      super.reset(config);


   }

   /**
    * Returns list of transition groups.
    *
    * @return list Never <code>null</code>, at least one entry.
    */
    private List getTransitionGroups()
    {
       List trans = new ArrayList();
       trans.add(new TransitionGroup(ms_res.getString("trans.group.insert"),
          PSWorkflowDef.TRANS_INSERT));
       trans.add(new TransitionGroup(ms_res.getString("trans.group.pre"),
          PSWorkflowDef.TRANS_PREUPDATE));
       trans.add(new TransitionGroup(ms_res.getString("trans.group.post"),
          PSWorkflowDef.TRANS_POSTUPDATE));
       return trans;
    }

    // implement ItemListener inteface method
    public void itemStateChanged(ItemEvent event)
    {
       // ignore change events during load
       if(m_isLoading)
          return;
       Object source = event.getSource();
       if(source == m_workflowComboField)
       {
          m_configCtx.setWorkflow((PSEntry)m_workflowComboField.getSelectedItem());
          try
          {
             reset(m_configCtx);
          }
          catch(PSLoaderException e)
          {
             ErrorDialogs.showErrorDialog(PSWorkflowConfigTabPanel.this,
                e.getMessage(),
                ms_res.getString("err.title.remoteexception"),
                JOptionPane.ERROR_MESSAGE);
          }
       }
       if(source == m_performedOnComboField)
       {
          loadTableModel();
       }

    }

   /**
    * Convienience inner class to represent a transition group
    */
   private class TransitionGroup
   {
       /**
        * Constructs a new Transition group
        *
        * @param name the group name. Assume it is not <code>null</code> or
        *    empty.
        *
        * @param value The transition group code. Assume it is one of the
        *     <code>PSWorkflowDef.TRANS_XXX</code> values.
        */
       TransitionGroup(String name, int value)
       {
          if(null == name)
             throw new IllegalArgumentException("Group name cannot be null.");

          m_name = name;
          m_value = value;
       }

       /**
        * Returns the groups name
        *
        * @return name, never <code>null</code>
        */
       public String getName()
       {
         return m_name;
       }

       /**
        * Returns the transition group code
        *
        * @return group code. It is one of the <code>PSWorkflowDef.TRANS_XXX
        *    </code> values.
        */
       public int getValue()
       {
         return m_value;
       }

       /**
        * Returns the groups name as the string representation of this object.
        *
        * @return The name, never <code>null</code>.
        */
       public String toString()
       {
          return getName();
       }

       /**
        * The name of the group, init by ctor, never <code>null</code> after
        * that.
        */
       private String m_name;

       /**
        * The transition group code. Init by ctor. It is one of the
        * <code>PSWorkflowDef.TRANS_XXX</code> values.
        */
       private int m_value;

   }

   /**
    * PSEntry wrapper to override the toString method
    */
   private class PSEntryWrapper
   {
      /**
       * Constructs a new <code>PSEntryWrapper</code> object.
       *
       * @param entry the <code>PSEntry</code> object to wrap,
       *    never <code>null</code>.
       */
      PSEntryWrapper(PSEntry entry)
      {
         if(null == entry)
            throw new IllegalArgumentException("Entry cannot be null");

         m_entry = entry;
      }

      /**
       * Returns the <code>PSEntry</code> object.
       *
       * @return the entry object, never <code>null</code>.
       */
      public PSEntry getEntry()
      {
         return m_entry;
      }

      // Override <code>toString()</code>
      public String toString()
      {
         return m_entry.toString() + " ("+ m_entry.getValue() + ")";
      }

      /**
       * The original object of the wrapper class. Initialized by ctor, never
       * <code>null</code> after that.
       */
      private PSEntry m_entry;
   }


   /**
    * Table model to represent the workflow transitions
    */
   private class WorkflowTable extends JTable
   {

      /**
       * Constructs a new table
       *
       * @param columns A list of column names in <code>String</code> objects.
       *    Assume it is not <code>null</code> or empty.
       */
      WorkflowTable(Vector columns)
      {
         super(new Vector(), columns);
      }

      /**
       * Overrides the tables method to refresh the editor
       * components selections and then returns the appropriate
       * cell editor.
       * @param row the index of the row to be edited.
       * @param col the index of the column to be edited.
       * @return the cell editor, never <code>null</code>.
       */
      public TableCellEditor getCellEditor(int row, int col)
      {
         m_transitionComboEditor.removeAllItems();
         Iterator it = getAvailableTransitions();
         while(it.hasNext())
            m_transitionComboEditor.addItem(it.next());
         return m_cellEditor;
      }

      /**
       * Returns an iterator of non selected transitions
       * for this table model.
       * @return iterator of <code>PSEntryWrapper</code> objects
       * , never <code>null</code>, may be empty.
       */
      private Iterator getAvailableTransitions()
      {
         List availTrans = new ArrayList();
         DefaultTableModel model = (DefaultTableModel)getModel();
         List selectedTrans = new ArrayList();
         for(int i = 0; i < model.getRowCount(); i++)
         {
            Object obj = model.getValueAt(i, 0);
            if(null != obj && obj instanceof PSEntryWrapper)
               selectedTrans.add(obj);
         }

         Iterator it = m_allTransitions.iterator();
         while(it.hasNext())
         {
            PSEntryWrapper entry = (PSEntryWrapper)it.next();
            if(!selectedTrans.contains(entry))
               availTrans.add(entry);
         }
         return availTrans.iterator();
      }

   }


   /**
    * Workflow combo box field. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private JComboBox m_workflowComboField;

   /**
    * Transition performed on combo box field. Initialized in {@link #init()},
    * never <code>null</code> after that.
    */
   private JComboBox m_performedOnComboField;

   /**
    * Transitions table. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private WorkflowTable m_transTable;

   /**
    * Transitions table combo editor. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private JComboBox m_transitionComboEditor;

   /**
    * The cell editor for the transition table. Initialized int
    * {@link #init()}, never <code>null</code> after that.
    */
   private DefaultCellEditor m_cellEditor;

   /**
    * Transitions input table model. Initialized in
    * {@link #initializeTableModels()}, never <code>null</code> after that.
    */
   private DefaultTableModel m_transInputTableModel;

   /**
    * Transitions pre-update table model. Initialized in
    * {@link #initializeTableModels()}, never <code>null</code> after that.
    */
   private DefaultTableModel m_transPreTableModel;

   /**
    * Transitions post_update table model. Initialized in
    * {@link #initializeTableModels()}, never <code>null</code> after that.
    */
   private DefaultTableModel m_transPostTableModel;

   /**
    * This extractor's configuration context. Initialized in <code>load<code>
    * never <code>null</code> after that.
    */
   private PSExtractorConfigContext m_configCtx;

   /**
    * List of all transitions for this workflow. Never <code>null</code>,
    * may be empty.
    */
   private List m_allTransitions = new ArrayList();

   /**
    * Flag indicating that the panel is currently loading.
    */
   private boolean m_isLoading = false;

   /**
    * Resource bundle for this class. Initialized once in {@link #init()},
    * never <code>null</code> after that.
    */
   protected static ResourceBundle ms_res;

   private static final int MIN_ROWS = 30;



}

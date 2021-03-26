/******************************************************************************
 *
 * [ CatalogerConfigDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.StringConstraint;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.services.security.data.PSCatalogerConfig.ConfigTypes;
import com.percussion.guitools.PSPropertyPanel;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Dialog to allow creating and editing cataloger configurations.
 */
public class CatalogerConfigDialog extends PSDialog
{
   /**
    * Default serializer id
    */
   private static final long serialVersionUID = 1L;

   /**
    * @param parent The parent frame, may be <code>null</code>.
    * @param config The config to edit, may be <code>null</code> if creating
    * a new one.
    * @param type The type of cataloger, used when creating a new cataloger,
    * may not be <code>null</code> and must match the supplied config if it is
    * not <code>null</code>.
    */
   public CatalogerConfigDialog(Frame parent, PSCatalogerConfig config, 
      ConfigTypes type)
   {
      super(parent);

      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      if (config != null && !config.getConfigType().equals(type))
         throw new IllegalArgumentException(
            "config type must match supplied type");

      
      m_config = config;
      m_type = type;
      ms_res = getResources();
      
      initDialog();
      if (m_config != null)
         initData();      
   }
   
   /**
    * Initializes all dialog controls
    */
   private void initDialog()
   {
      setTitle(ms_res.getString("title"));
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      getContentPane().add(panel);
      
      m_name = new JTextField();
      m_className = new JTextField();
      m_description = new JTextField();
      
      PSPropertyPanel propPanel = new PSPropertyPanel();
      propPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
           
      propPanel.addPropertyRow(ms_res.getString("name"), m_name, 
         ms_res.getString("name.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("className"), m_className, 
         ms_res.getString("className.mn").charAt(0));
      propPanel.addPropertyRow(ms_res.getString("desc"), m_description, 
         ms_res.getString("desc.mn").charAt(0));
      
      panel.add(propPanel, BorderLayout.NORTH);
      
      JPanel propsPanel = createPropsPanel();
      panel.add(propsPanel, BorderLayout.CENTER);
      
      UTStandardCommandPanel commandPanel = new UTStandardCommandPanel(this, "", 
         SwingConstants.HORIZONTAL)
      {
         private static final long serialVersionUID = 1L;

         @Override
         public void onOk()
        {
            CatalogerConfigDialog.this.onOk();
          }
      };
      getRootPane().setDefaultButton(commandPanel.getOkButton());
      
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
      cmdPanel.add(commandPanel, BorderLayout.EAST);      
      panel.add(cmdPanel, BorderLayout.SOUTH);
      
      initValidation();
      
      setPreferredSize(new Dimension(400, 350));
      pack();
      setResizable(true);
      center();
   }

   /**
    * Create the panel containing the properties panel
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createPropsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      
      Border border = BorderFactory.createCompoundBorder(
         BorderFactory.createEmptyBorder(5, 5, 5, 5),
         BorderFactory.createTitledBorder(ms_res.getString("props")));      
      panel.setBorder(border);
      
      m_props = new JTable();
      DefaultTableModel tableModel = new DefaultTableModel();
      tableModel.setColumnIdentifiers(new String[]
      {
         ms_res.getString("propName"), 
         ms_res.getString("propVal")
      });

      tableModel.setRowCount(20);
      
      m_props.setModel(tableModel);
      m_props.getTableHeader().setReorderingAllowed(false);
      m_props.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_props.setIntercellSpacing(new Dimension(0, 0));
      m_props.setRowSelectionAllowed(false);
      m_props.setColumnSelectionAllowed(false);
      m_props.setRequestFocusEnabled(false);
      
      JScrollPane pane = new JScrollPane(m_props);
      panel.add(pane, BorderLayout.CENTER);
      
      return panel;
   }

   /**
    * Fills in the UI controls with values from the config supplied during
    * construction.
    */
   private void initData()
   {
      m_name.setText(m_config.getName());
      m_className.setText(m_config.getClassName());
      m_description.setText(m_config.getDescription());
      
      DefaultTableModel model = (DefaultTableModel) m_props.getModel();
      int rowCount = model.getRowCount();
      int onRow = 0;
      for (Map.Entry<String, String> entry : 
         m_config.getProperties().entrySet())
      {
         if (onRow < rowCount)
         {
            model.setValueAt(entry.getKey(), onRow, NAME_COL);
            model.setValueAt(entry.getValue(), onRow, VALUE_COL);
         }
         else
         {
            model.addRow(new String[] {entry.getKey(), entry.getValue()});
         }
         onRow++;
      }
      
      // ensure at least 5 empty rows
      int required = onRow + 5;
      if (required > rowCount)
         model.setRowCount(onRow + 5);
   }

   /**
    * Initializes the validation framework.
    */
   private void initValidation()
   {
      List<Component> comps = new ArrayList<Component>();
      List<ValidationConstraint> constraints = 
         new ArrayList<ValidationConstraint>();
      StringConstraint nonEmpty = new StringConstraint();
      
      comps.add(m_name);
      constraints.add(nonEmpty);
      comps.add(m_className);
      constraints.add(nonEmpty);
      
      Component[] c = comps.toArray(new Component[comps.size()]);
      ValidationConstraint[] v = constraints.toArray(
         new ValidationConstraint[constraints.size()]);
      setValidationFramework(c, v);
   }
   
   /**
    * Handle the ok action
    */
   @Override
   public void onOk()
   {
      stopTableEditor(m_props);
      
      if (!activateValidation())
         return;
      
      // validate props
      if (!validateProps())
         return;
      
      String name = m_name.getText().trim();
      String className = m_className.getText().trim();
      String desc = m_description.getText();
      Map<String, String> props = new HashMap<String, String>();
      
      TableModel model = m_props.getModel();
      for (int i = 0; i < model.getRowCount(); i++)
      {
         String propName = (String) model.getValueAt(i, NAME_COL);
         // skip empty rows
         if (StringUtils.isBlank(propName))
            continue;
         
         props.put(propName.trim(), 
            ((String) model.getValueAt(i, VALUE_COL)).trim());
      }
      
      if (m_config == null)
      {
         m_config = new PSCatalogerConfig(name, m_type, className, desc, props);
      }
      else
      {
         m_config.setName(name);
         m_config.setClassName(className);
         m_config.setDescription(desc);
         m_config.setProperties(props);
      }
      
      m_bModified = true;
      setVisible(false);
      dispose();
   }
   
   /**
    * Validates that all entries in the properties table consist of name/value
    * pairs.  If a problem is found, an error message is displayed.
    * 
    * @return <code>true</code> if validation succeeds, <code>false</code> if
    * not.
    */
   private boolean validateProps()
   {
      boolean result = true;
      
      TableModel model = m_props.getModel(); 
      int rows = model.getRowCount();
      
      for (int i = 0; i < rows; i++)
      {
         if (StringUtils.isBlank((String)model.getValueAt(i, NAME_COL)) && 
            !StringUtils.isBlank((String)model.getValueAt(i, VALUE_COL)))
         {
            JOptionPane.showMessageDialog(this, ms_res.getString(
               "props.val.msg"), "props.val.title", JOptionPane.ERROR_MESSAGE);
            result = false;
         }
      }
      
      return result;
   }

   /**
    * Get the new/edited config.
    * 
    * @return The config, may be <code>null</code> if creating a new config and
    * {@link #onOk()} has not been successfully executed.
    */
   public PSCatalogerConfig getConfig()
   {
      return m_config;
   }
   
   /**
    * Accessor function to check whether role is modified or not.
    *
    * @return The modified flag.
    **/
   public boolean isModified()
   {
      return m_bModified;
   }
   
   /**
    * Text field for the cataloger name, never <code>null</code> after ctor.
    */
   private JTextField m_name;

   /**
    * Text field for the cataloger class name, never <code>null</code> after 
    * ctor.
    */
   private JTextField m_className;

   /**
    * Text field for the cataloger description, never <code>null</code> after 
    * ctor.
    */
   private JTextField m_description;
   
   /**
    * Table to define the cataloger properties, never <code>null</code> after
    * construction.
    */
   private JTable m_props;
   
   /**
    * The flag to indicate whether role is modified or not, initialized to
    * <code>false</code> and set to <code>true</code> when OK button is clicked.
    **/
   private boolean m_bModified = false;
   
   /**
    * Config supplied during construction, may be <code>null</code>.
    */
   private PSCatalogerConfig m_config = null;
   
   /**
    * The type of config supplied during ctor, never <code>null</code> after 
    * that.
    */
   private ConfigTypes m_type;
   
   /** Dialog resource strings, initialized in constructor. **/
   private static ResourceBundle ms_res = null;
   
   /**
    * Constant for the column index in the {@link #m_props} table for the 
    * property name.
    */
   private static final int NAME_COL = 0;

   /**
    * Constant for the column index in the {@link #m_props} table for the 
    * property value.
    */
   private static final int VALUE_COL = 1;
}


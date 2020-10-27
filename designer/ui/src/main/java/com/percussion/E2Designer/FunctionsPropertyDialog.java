/* *****************************************************************************
 *
 * [ FunctionsPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSFunctionParamValue;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.PSDatabaseFunctionDef;
import com.percussion.extension.PSDatabaseFunctionDefParam;
import com.percussion.extension.PSDatabaseFunctionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

/**
 * This class provides a dialog box for editing database functions.
 */
public class FunctionsPropertyDialog extends PSDialogAPI
   implements IPSCellEditorDialog
{

   /**
    * Constructs the functions editor dialog using the specified parent
    * dialog.
    *
    * @param dialog the parent dialog, may not be <code>null</code>
    */
   public FunctionsPropertyDialog(JDialog dialog)
   {
      super(dialog);
      initDialog();
   }

   /**
    * Constructs the functions editor dialog using the specified parent
    * frame.
    *
    * @param frame the parent frame, may not be <code>null</code>
    */
   public FunctionsPropertyDialog(JFrame frame)
   {
      super(frame);
      initDialog();
   }

   /**
    * Initializes the dialog by creating the panels and controls of this dialog
    */
   private void initDialog()
   {
      getContentPane().setLayout(new BorderLayout());
      setSize(450, 500);

      String title = getResources().getString("title");
      setTitle(title);

      // Creates the command panel with OK, Cancel and help buttons
      UTStandardCommandPanel buttonPanel = new UTStandardCommandPanel(
         this, "", SwingConstants.HORIZONTAL)
      {
        public void onOk()
        {
            FunctionsPropertyDialog.this.onOk();
        }
      };

      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(buttonPanel, BorderLayout.EAST);
      
      getRootPane().setDefaultButton(buttonPanel.getOkButton());

      // Creates the parameter panel with the table of parameter values
      ValueSelectorDialogHelper vsHelper =
         new ValueSelectorDialogHelper(m_backendTank, null);
      Vector vDataTypes = vsHelper.getDataTypes();
      m_valueSelDlg = new ValueSelectorDialog(this, vDataTypes, null);
      ValueSelectorCellEditor editor =
         new ValueSelectorCellEditor(m_valueSelDlg);
      m_parameterPanel = new UTParameterPanel(editor, false);
      m_parameterPanel.setDataTypesForValueSelector(vDataTypes);
      
      // layout the panels
      String strFuncLabel = getResources().getString("functionsLabel");
      char mn = getResources().getString("functionsLabel.mn").charAt(0);
      JLabel funcLabel = new JLabel(strFuncLabel);
      funcLabel.setDisplayedMnemonic(mn);
      funcLabel.setDisplayedMnemonicIndex(strFuncLabel.indexOf(mn));
      funcLabel.setLabelFor(m_functions);
      JPanel functionPanel = new JPanel(new BorderLayout());
      functionPanel.setBorder(new EmptyBorder(8,5,5,5));
      functionPanel.add(funcLabel, BorderLayout.WEST);
      functionPanel.add(m_functions, BorderLayout.CENTER);

      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BorderLayout());
      topPanel.add(functionPanel, BorderLayout.WEST);

      getContentPane().add(topPanel, BorderLayout.NORTH);
      getContentPane().add(m_parameterPanel, BorderLayout.CENTER);
      getContentPane().add(cmdPanel, BorderLayout.SOUTH);

      center();
      setResizable(true);
      m_functions.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onFunctionSelectionChanged();
         }
      });

   }

   /**
    * Initializes the data in this dialog box.
    * <p>
    * Sets the back end data tank. This is used to get the database
    * driver needed for obtaining the function definitions from the database
    * function manager.
    * <p>
    * Obtains the database function definitions for this driver and sets the
    * name of the functions in the combo box.
    *
    * @param backendTank the back end data tank, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>backendTank</code> is
    * <code>null</code>
    */
   public void initialize(OSBackendDatatank backendTank)
   {
      if (backendTank == null)
         throw new IllegalArgumentException(
            "back end data tank may not be null");

      m_backendTank = backendTank;
      ValueSelectorDialogHelper vsHelper =
         new ValueSelectorDialogHelper(m_backendTank, null);
      m_valueSelDlg.refresh(vsHelper.getDataTypes());
      DTBackendColumn backendCol = new DTBackendColumn(m_backendTank);
      m_valueSelDlg.setDefaultType(backendCol);

      m_dbFuncDefs.clear();
      m_functions.removeAllItems();

      String datasource = null;
      Iterator itTables = m_backendTank.getTables().iterator();
      if (itTables.hasNext())
      {
         PSBackEndTable table = (PSBackEndTable)itTables.next();
         datasource = table.getDataSource();
         if (datasource == null)
            datasource = "";

         datasource = datasource.trim();
         Iterator itFuncDefs =
            CatalogDatabaseFunctions.getDatabaseFunctionDefs(
               null, datasource, false);
         while (itFuncDefs.hasNext())
         {
            PSDatabaseFunctionDef funcDef =
               (PSDatabaseFunctionDef)itFuncDefs.next();
            String funcName = funcDef.getName();
            if ((funcDef.getType() ==
               PSDatabaseFunctionManager.FUNCTION_TYPE_USER)
               || (!m_dbFuncDefs.containsKey(funcName)))
            {
               m_dbFuncDefs.put(funcName, funcDef);
            }
         }         
      }
      
      Iterator itFuncNames = new TreeSet(m_dbFuncDefs.keySet()).iterator();
      while (itFuncNames.hasNext())
         m_functions.addItem((String)itFuncNames.next());
   }

   /**
    * Creates the function call using the database function set in the combo
    * box and the parameter name and value set in the parameter panel.
    *
    * @return the function call created using this dialog, may be
    * <code>null</code> if no function name is selected
    */
   private PSFunctionCall createFunctionCall()
   {
      PSFunctionCall funcCall = null;

      int index = m_functions.getSelectedIndex();
      if ((index > -1) && (index < m_dbFuncDefs.size()))
      {
         String funcName = (String)m_functions.getSelectedItem();
         IPSReplacementValue[] values =
            m_parameterPanel.getReplacementValues();
         PSFunctionParamValue params[] =
            new PSFunctionParamValue[values.length];

         for (int i = 0; i < values.length; i++)
            params[i] = new PSFunctionParamValue(values[i]);

         funcCall = new PSFunctionCall(funcName, params, null, null);
      }
      return funcCall;
   }

   /**
    * Returns the replacement value edited/created using this dialog.
    *
    * @return If a function name is selected then returns a function call
    * (<code>PSFunctionCall</code>), else returns an empty text literal
    * (<code>PSTextLiteral</code>). Never <code>null</code>.
    */
   public Object getData()
   {
      IPSReplacementValue value = m_funcCall;
      if (value == null)
         value = new PSTextLiteral("");
      return value;
   }

   /**
    * Sets the function call to be edited using this dialog.
    *
    * @param value the function call to edit using this dialog, may be
    * <code>null</code>
    */
   public void setData(Object value)
   {
      if (value instanceof PSFunctionCall)
      {
         m_funcCall = (PSFunctionCall)value;
         updateFunctionComboxBox(m_funcCall.getName());
         updateParameterPanel();
      }
      else if (m_functions.getItemCount() > 0)
      {
         m_functions.setSelectedIndex(0);
      }
   }

   /**
    * Handles the database function selection change event.
    * Obtains the name of the currently selected function and creates a
    * function call by creating empty text literals for all the parameters.
    */
   void onFunctionSelectionChanged()
   {
      int index = m_functions.getSelectedIndex();
      if ((index > -1) && (index < m_dbFuncDefs.size()))
      {
         String funcName = (String)m_functions.getSelectedItem();

         if ((m_funcCall == null) ||
            (!m_funcCall.getName().equalsIgnoreCase(funcName)))
         {
            PSDatabaseFunctionDef funcDef =
               (PSDatabaseFunctionDef)m_dbFuncDefs.get(funcName);

            int reqParamsCount = funcDef.getParamsSize();
            PSFunctionParamValue params[] =
               new PSFunctionParamValue[reqParamsCount];

            for (int i = 0; i < reqParamsCount; i++)
               params[i] = new PSFunctionParamValue(new PSTextLiteral(""));

            m_funcCall = new PSFunctionCall(funcName, params, null, null);
         }
      }
      else
      {
         m_funcCall = null;
      }
      updateParameterPanel();
   }

   /**
    * Selects the specified function in the combox box.
    *
    * @param funcName the function to select in the combo box, may be
    * <code>null</code> or empty in which the no function is selected.
    */
   private void updateFunctionComboxBox(String funcName)
   {
      if ((funcName == null) || (funcName.trim().length() < 1))
         m_functions.setSelectedIndex(-1);
      else
         m_functions.setSelectedItem(funcName);
   }

   /**
    * Updates the parameter panel with data from the function call being edited
    */
   private void updateParameterPanel()
   {
      UTParameterPanelTableModel tableModel = m_parameterPanel.getTableModel();

      //tableModel.clearTableEntries();
      while (tableModel.getRowCount() > 0)
         tableModel.deleteRow(0);

      if (m_funcCall == null)
         return;

      PSDatabaseFunctionDef funcDef =
         (PSDatabaseFunctionDef)m_dbFuncDefs.get(m_funcCall.getName());
      PSFunctionParamValue[] params = m_funcCall.getParamValues();
      for (int i = 0; i < params.length; i++)
      {
         PSDatabaseFunctionDefParam funcDefParam = funcDef.getParamAtIndex(i);
         PSFunctionParamValue param = params[i];
         IPSReplacementValue value = param.getValue();
         m_parameterPanel.appendParameter(
            funcDefParam.getName(), value, funcDefParam.getDescription());
      }
   }

   /**
    * Handles the OK button click action. Updates the value of the function
    * call object using the current values set in the dialog. Invokes the OK
    * button click action listeners and makes this dialog invisible.
    */
   public void onOk()
   {
      setData(createFunctionCall());
      fireOk();
      setVisible(false);
   }

   /**
    * Inform all OK listeners that the OK button was clicked.
    */
   protected void fireOk()
   {
      ActionEvent event = new ActionEvent(this, 0, "OK");
      Iterator it = m_okListeners.iterator();
      while (it.hasNext())
         ((ActionListener)it.next()).actionPerformed(event);
   }

   /**
    * Inform all Cancel listeners that the Cancel button was clicked.
    */
   protected void fireCancel()
   {
      ActionEvent event = new ActionEvent(this, 0, "Cancel");
      Iterator it = m_cancelListeners.iterator();
      while (it.hasNext())
         ((ActionListener)it.next()).actionPerformed(event);
   }

   /**
    * See {@link IPSCellEditorDialog#addOkListener(ActionListener)} for details
    */
   public void addOkListener(ActionListener listener)
   {
      m_okListeners.add(listener);
   }

   /**
    * See {@link IPSCellEditorDialog#removeOkListener(ActionListener)} for
    * details.
    */
   public void removeOkListener(ActionListener listener)
   {
      m_okListeners.remove(listener);
   }

   /**
    * See {@link IPSCellEditorDialog#addCancelListener(ActionListener)} for
    * details.
    */
   public void addCancelListener(ActionListener listener)
   {
      m_cancelListeners.add(listener);
   }

   /**
    * See {@link IPSCellEditorDialog#removeCancelListener(ActionListener)} for
    * details.
    */
   public void removeCancelListener(ActionListener listener)
   {
      m_cancelListeners.remove(listener);
   }

   /**
    * See {@link PSDialogAPI#reset(ActionListener)} for
    * details.
    */
   public void reset()
   {
   }

   /**
    * See {@link PSDialogAPI#isValidModel(Object)} for
    * details.
    */
   public boolean isValidModel(Object model)
   {
      return (model instanceof PSFunctionCall);
   }

   /**
    * Data tank containing back end tables, used for obtaining the database
    * driver for obtaining the function definitions from the database function
    * manager. Initialized to <code>null</code>, set in the
    * <code>initialize()</code> method, never <code>null</code> or modified
    * after that.
    */
   private OSBackendDatatank m_backendTank = null;

   /**
    * Combo Box containing the database functions. Initialized to empty
    * combox box, items added in <code>initialize()</code> method
    */
   private JComboBox m_functions = new JComboBox();

   /**
    * map containing function name (<code>String</code>) as key, and function
    * definition (<code>PSDatabaseFunctionDef</code>) as value,
    * initialized to empty map, modified in the <code>initialize()</code>
    * method
    */
   private Map m_dbFuncDefs = new HashMap();

   /**
    * the function call which will be edited using this dialog box,
    * modified in the <code>setData()</code> method, may be <code>null</code>
    * (if a new function call is being added)
    */
   private PSFunctionCall m_funcCall = null;

   /**
    * panel containing function paramaters name and value, initialized in the
    * <code>initDialog()</code> method, never <code>null</code> or
    * modified after initialization
    */
   private UTParameterPanel m_parameterPanel = null;

   /**
    * The value selector dialog displayed when the user edits the function
    * parameter value, initialized in the ctor, never <code>null</code> after
    * initialization
    */
   private ValueSelectorDialog m_valueSelDlg;

   /**
    * list containing the listeners to be notified when the OK button is
    * clicked, initialized to empty list, modified in the
    * <code>addOkListener()</code> method, never <code>null</code>,
    * may be empty
    */
   protected transient List m_okListeners = new ArrayList();

   /**
    * list containing the listeners to be notified when the CANCEL button is
    * clicked, initialized to empty list, modified in the
    * <code>addOkListener()</code> method, never <code>null</code>,
    * may be empty
    */
   protected transient List m_cancelListeners = new ArrayList();

}



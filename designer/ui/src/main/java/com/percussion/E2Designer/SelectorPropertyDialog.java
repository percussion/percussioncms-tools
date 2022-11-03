/* *****************************************************************************
 *
 * [ SelectorPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.util.PSCollection;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * The column mapper property dialog.
 */
////////////////////////////////////////////////////////////////////////////////
public class SelectorPropertyDialog extends PSEditorDialog
{
   /**
   * Construct the default mapper property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public SelectorPropertyDialog()
   {
   super();
   initDialog();
   }

    public SelectorPropertyDialog(Window parent)
    {
        super(parent);
        initDialog();
    }

   /**
   * Construct the default mapper property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public SelectorPropertyDialog(JFrame parent, OSDataSelector selector)
   {
   super(parent);
    this.setLocationRelativeTo(parent);

    m_selector = selector;
   initDialog();
   }

/** This method changes the dynamic panel to display the WHERE view.
*/
  protected void swapToWhereView()
  {
    m_dynamicPanel.remove(m_selectViewPanel);
    m_dynamicPanel.add(m_whereViewPanel);
    m_whereViewPanel.revalidate();
    m_whereViewPanel.repaint();
  }

/** This method changes the dynamic panel to display the SELECT view.
*/
  protected void swapToSelectView()
  {
    m_dynamicPanel.remove(m_whereViewPanel);
    m_dynamicPanel.add(m_selectViewPanel);
    m_selectViewPanel.revalidate();
    m_selectViewPanel.repaint();
  }

   /**
    * Creates the data required to create the cell editor to display the
    * popup menu and display the corresponding dialog when the menu item
    * is selected.
    *
    * @param cellDlg the value selector dialog to display when the
    * "Single Value" menu action is selected, assumed not <code>null</code>
    *
    * @return an array containg two items. The first item is the cell editor
    * data for the "Function" menu item and the "Funtion Properties" dialog.
    * The second item is the cell editor data for the "Single Value" menu item
    * and the <code>ValueSelectorDialog</code> that is displayed when this
    * menu item is selected.
    */
   private UTPopupMenuCellEditorData[] createPopMenuCellEditorData(
      IPSCellEditorDialog cellDlg)
   {
      UTPopupMenuCellEditorData[] cellEditorData =
         new UTPopupMenuCellEditorData[2];

      // Function menu
      String strFunc = E2Designer.getResources().getString("menuFunction");
      JMenuItem menuItemFunc = new JMenuItem(strFunc, KeyEvent.VK_F);
      cellEditorData[0] = new UTPopupMenuCellEditorData(
         menuItemFunc, m_functionsDialog);

      // Single Value menu - variable column
      String strValSingle =
         E2Designer.getResources().getString("menuSingleValue");
      JMenuItem menuValFunc = new JMenuItem(strValSingle, KeyEvent.VK_S);
      cellEditorData[1] = new UTPopupMenuCellEditorData(
         menuValFunc, cellDlg);

      return cellEditorData;
   }

   /**
   * Create the WHERE clause selector table to edit the query keys.
   *
   * @param      table               the table model used
    * @return   JScrollPane      the table view, a scrollable pane
   */
  //////////////////////////////////////////////////////////////////////////////
   private JScrollPane createTableView(WhereClauseTableModel table)
   {
      m_functionsDialog = new FunctionsPropertyDialog(this);

      ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
            (OSBackendDatatank) null, null);

      m_columnDialog = new ValueSelectorDialog(this, h.getDataTypes(), null);
      m_xmlDialog = new ValueSelectorDialog(this, h.getDataTypes(), null);
      m_xmlDialog.setDefaultType(new DTTextLiteral());

      UTPopupMenuCellEditorData[] colCellEditorData =
         createPopMenuCellEditorData(m_columnDialog);
      m_columnEditor = new UTPopupMenuCellEditor(colCellEditorData);

      UTPopupMenuCellEditorData[] xmlCellEditorData =
         createPopMenuCellEditorData(m_xmlDialog);
      m_xmlEditor = new UTPopupMenuCellEditor(xmlCellEditorData);

      // define the cell editors
      m_tableView.getColumn( WhereClauseTableModel.VARIABLE_COL_NAME ).
         setCellEditor(m_columnEditor);
      m_tableView.getColumn( WhereClauseTableModel.VARIABLE_COL_NAME ).
         setPreferredWidth(130);
      m_tableView.getColumn( WhereClauseTableModel.OPERATOR_COL_NAME ).
         setCellEditor(new DefaultCellEditor(new UTOperatorComboBox()));
      m_tableView.getColumn( WhereClauseTableModel.OPERATOR_COL_NAME ).
         setPreferredWidth(50);
      m_tableView.getColumn( WhereClauseTableModel.VALUE_COL_NAME ).
         setCellEditor(m_xmlEditor);
      m_tableView.getColumn( WhereClauseTableModel.VALUE_COL_NAME ).
         setPreferredWidth(140);
      m_tableView.getColumn( WhereClauseTableModel.BOOL_COL_NAME ).
         setCellEditor(new DefaultCellEditor(new UTBooleanComboBox()));
      m_tableView.getColumn( WhereClauseTableModel.BOOL_COL_NAME ).
         setPreferredWidth(30);
      m_tableView.getColumn( WhereClauseTableModel.OMIT_COL_NAME ).
         setCellRenderer(new UTCheckBoxCellRenderer());
      m_tableView.getColumn( WhereClauseTableModel.OMIT_COL_NAME ).
         setCellEditor(new UTCheckBoxCellEditor());
      m_tableView.getColumn( WhereClauseTableModel.OMIT_COL_NAME ).
         setPreferredWidth(65);

      // set operator cell renderer
      m_tableView.getColumn( WhereClauseTableModel.OPERATOR_COL_NAME ).setCellRenderer(new UTOperatorComboBoxRenderer());

    JScrollPane pane = new JScrollPane(m_tableView,
                                                       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      return pane;
  }

   /**
   * Create the where clause view panel
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  private void createWhereViewPanel()
  {
      m_whereViewPanel.setBorder(new TitledBorder(getResources().getString("queryKeys")));

      // create "create indexes for keys" button
      JPanel indexPanel = new JPanel(new BorderLayout());
      indexPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
      //m_indexButton = new UTFixedButton(getResources().getString("index"));
    //m_indexButton.setEnabled(false);
    indexPanel.add(m_distinctBox, BorderLayout.CENTER);
      //indexPanel.add(m_indexButton, BorderLayout.CENTER);

      m_whereViewPanel.add(createTableView(m_table), BorderLayout.CENTER);
      m_whereViewPanel.add(indexPanel, BorderLayout.SOUTH);
  }

   /**
   * Create the select statement view panel
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  private void createSelectViewPanel()
  {
    m_select.setLineWrap(true);
    m_select.setWrapStyleWord(true);
    m_select.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.gray, Color.darkGray));

      m_selectViewPanel.setBorder(new TitledBorder(getResources().getString("selectStatement")));
      m_selectViewPanel.add(new JScrollPane( m_select ), "Center");
   }

   /**
    * Create criteria selection panel
    *
    * @return   JPanel      the criteria selection view panel
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createSelectionCriteriaPanel()
   {
      JPanel panel = new JPanel(new GridLayout(0, 1));
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));

      // create selection criteria radio buttons
      m_useWhereClause.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            swapToWhereView();
         }
      });
      m_useWhereClause.setMnemonic(getResources().getString("mn_where").charAt(0));
      m_useWhereClause.setActionCommand(getResources().getString("where"));

      m_useFlexibleQuery.setEnabled(true);
      m_useFlexibleQuery.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            swapToSelectView();
         }
      });
      m_useFlexibleQuery.setMnemonic(getResources().getString("mn_flexible").charAt(0));
      m_useFlexibleQuery.setActionCommand(getResources().getString("flexible"));

      m_selectionCriteria.add(m_useWhereClause);
      m_selectionCriteria.add(m_useFlexibleQuery);

      panel.add(m_useWhereClause);
      panel.add(m_useFlexibleQuery);

      return panel;
   }

   /**
    * Create the selector view panel
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  private void createViewPanel()
  {
      m_dynamicPanel.setBorder(new TitledBorder(getResources().getString("selectCriteria")));

      m_dynamicPanel.add(createSelectionCriteriaPanel(), "North");
      createWhereViewPanel();
      createSelectViewPanel();

      //m_dynamicPanel.add(m_selectViewPanel, "Center");
      m_dynamicPanel.add(m_whereViewPanel, "Center");
  }

   /**
   * Create command panel.
   *
   * @return   JPanel      the guess command panel
   */
  //////////////////////////////////////////////////////////////////////////////
   private JPanel createCommandPanel()
   {
      m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
         // implement onOk action
         public void onOk()
         {
            SelectorPropertyDialog.this.onOk();
         }
      };

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(m_commandPanel, BorderLayout.EAST);

      return panel;
   }

  /**
   * Perform build query functionality.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  private void onBuildQuery()
  {
   Frame frame = (Frame) this.getParent();

      System.out.println("Calling Build Query dialog...");
    m_flexDialog = new FlexibleQueryBuilderDialog(m_backendTank);
    m_flexDialog.setVisible(true);

    if (!m_flexDialog.isCancel())
      m_select.setText(m_flexDialog.getWhereClauses());

    m_flexDialog.dispose();
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
      createViewPanel();
      panel.add(m_dynamicPanel, "Center");
      panel.add(createCommandPanel(), "South");
      setResizable(true);

   // set the default button
    getRootPane().setDefaultButton(m_commandPanel.getOkButton());

   getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel);
    this.setSize(DIALOG_SIZE);
    m_distinctBox.setMnemonic(getResources().getString("mn_distinct").charAt(0));
    // initialize validation constraints
//    m_validatedComponents[0] = this;
//      m_validationConstraints[0] = new SelectorConstraint();
//      setValidationFramework(m_validatedComponents, m_validationConstraints);
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
               if(m_tableView.isEditing())
                  m_tableView.getCellEditor().stopCellEditing();
         }
      });
   }

   /**
   * Get the where clause table
   *
   * @return   The table model for the WHERE clauses.
   */
  //////////////////////////////////////////////////////////////////////////////
  public WhereClauseTableModel getWhereClauseTable()
   {
      return m_table;
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
         e.printStackTrace();
      }

      return m_res;
   }

/** @returns String Gets the text from SELECT statement text area.
*/
  public String getSelectStatement()
  {
    return m_select.getText();
  }

/** @param stmt Sets text into the SELECT statement text area.
*/
  public void setSelectStatement(String stmt)
  {
    m_select.setText(stmt);
  }

   //////////////////////////////////////////////////////////////////////////////
   // implementation of IEditor
   public boolean onEdit(UIFigure figure, final Object data)
   {
      try
      {
         UTPipeNavigator navigator = new UTPipeNavigator();
         UIFigure backend = navigator.getBackendTank(figure);
           UIFigure page = navigator.getPageTank(figure);

         ValueSelectorDialogHelper vsColumnHelper = null;
         ValueSelectorDialogHelper vsXmlHelper = null;

         if (backend != null && backend.getData() instanceof OSBackendDatatank)
         {
            OSBackendDatatank backendTank =
               (OSBackendDatatank)backend.getData();
            m_backendTank = backendTank;

            // in case BackEndTank contains no tables, display error dialog only
            PSCollection tables = m_backendTank.getTables();
            if (null == tables || 0 == tables.size())
            {
               JOptionPane.showMessageDialog(
                  E2Designer.getApp().getMainFrame(),
                  E2Designer.getResources().getString(
                     "ErrorNoBackEndTableForSelector"),
                  E2Designer.getResources().getString("OpErrorTitle"),
                  JOptionPane.ERROR_MESSAGE);

               return false;
            }

            vsColumnHelper = new ValueSelectorDialogHelper(backendTank, null);
         }
         else
         {
            m_backendTank = null;
            vsColumnHelper = new ValueSelectorDialogHelper(
                  (OSBackendDatatank) null, null);
         }

         m_columnDialog.refresh(vsColumnHelper.getDataTypes());

         DTBackendColumn backendCol = null;
         if (m_backendTank != null)
         {
            m_functionsDialog.initialize(m_backendTank);
            backendCol = new DTBackendColumn(m_backendTank);
            m_columnDialog.setDefaultType(backendCol);
         }

         if(page != null && page.getData() instanceof OSPageDatatank)
         {
            OSPageDatatank pageTank = (OSPageDatatank)page.getData();
            vsXmlHelper = new ValueSelectorDialogHelper(null, pageTank);
         }
         else
         {
           vsXmlHelper = new ValueSelectorDialogHelper(
               (OSBackendDatatank) null, null);
         }

         m_xmlDialog.refresh(vsXmlHelper.getDataTypes());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      if (figure.getData() instanceof OSDataSelector)
      {
         m_selector = (OSDataSelector) figure.getData();

         if (m_selector.isSelectByWhereClause())
         {
            m_selectionCriteria.setSelected(m_useWhereClause.getModel(), true);
            swapToWhereView();
         }
         else if (m_selector.isSelectByNativeStatement())
         {
            m_selectionCriteria.setSelected(m_useFlexibleQuery.getModel(), true);
            swapToSelectView();
         }

         m_select.setText(m_selector.getNativeStatement());
         m_table.loadFromConditionals( m_selector.getWhereClauses());

         m_distinctBox.setSelected(m_selector.isSelectUnique());

         m_table.appendRow(128 - m_table.getRowCount());

         this.center();
         this.setVisible(true);
      }
      else
         throw new IllegalArgumentException("OSDataSelector expected!");

      return m_modified;
   }


/** Handles ok button action. Overrides PSDialog onOk() method implementation.
*/
   public void onOk()
   {
      /* End the cell editing mode before validation. If the user presses the
         OK button before Enter, the contents of the editor will be recognized. */
      if ( m_tableView.isEditing())
         m_tableView.getCellEditor().stopCellEditing();

      if (activateValidation())
      {
         try
         {
            /* do conditionals first, since they have their own validation
                  TODOph: Move this code into a validation constraint (in the 5 different
                  places that it is used). To do this properly, the constraint framework
                  needs to be modified. The ValidationConstraint should pop the message
                  and do what's needed to activate the bad field. This code would be
                  a ConditionalConstraint, which takes a JTable (from which the model
                  can be obtained). */
            ConditionalValidationError error = m_table.validate();
            if ( null != error )
            {
               JOptionPane.showMessageDialog(this, error.getErrorText(),
                     getResources().getString( "ValidationErrorTitle" ), JOptionPane.WARNING_MESSAGE);
               m_tableView.getSelectionModel().clearSelection();
               m_tableView.editCellAt( error.getErrorRow(), error.getErrorCol());
               m_tableView.getEditorComponent().requestFocus();
               return;
            }

            m_selector.setWhereClauses( m_table.saveToConditionals( m_selector.getWhereClauses()));

            m_modified = true;

            if (m_selectionCriteria.isSelected(m_useWhereClause.getModel()))
            {
               m_selector.setSelectByWhereClause();
            }
            else
            {
               m_selector.setSelectByNativeStatement();
            }

            m_selector.setSelectUnique(m_distinctBox.isSelected());

        if (m_selector.isSelectByNativeStatement() && m_select.getText().trim().equals(""))
        {
          JOptionPane.showMessageDialog( this,
                                         getResources().getString("noSelectError"),
                                         E2Designer.getResources().getString("OpErrorTitle"),
                                         JOptionPane.ERROR_MESSAGE );

          throw new Exception();
        }
        else
        {
              m_selector.setNativeStatement(m_select.getText());
          dispose();
        }
         }
      catch (Exception e)
      {
        // do nothing since dialog can only exit via the dispose() call.
        //e.printStackTrace();
      }

    }
  }

   //////////////////////////////////////////////////////////////////////////////
  /**
   * the selector data
   */
  OSDataSelector m_selector = null;
  OSBackendDatatank m_backendTank = null;
  /**
   * the view panels
   */
  JPanel m_dynamicPanel = new JPanel(new BorderLayout());
  JPanel m_whereViewPanel = new JPanel(new BorderLayout());
  JPanel m_selectViewPanel = new JPanel(new BorderLayout());

  /**
   * the radion buttons to switch the view
   */
  ButtonGroup m_selectionCriteria = new ButtonGroup();
  JRadioButton m_useWhereClause = new JRadioButton(getResources().getString("where"));
  JRadioButton m_useFlexibleQuery = new JRadioButton(getResources().getString("flexible"));
  /**
   * the where clause table
   */
   WhereClauseTableModel m_table = new WhereClauseTableModel();
  UTJTable m_tableView = new UTJTable(m_table);

  /**
   * cell editor for the variable of the where clause, initialized in the ctor,
   * never <code>null</code> after initialization
   */
  UTPopupMenuCellEditor m_columnEditor = null;

  /**
   * cell editor for the value of the where clause, initialized in the ctor,
   * never <code>null</code> after initialization
   */
  UTPopupMenuCellEditor m_xmlEditor = null;

  /**
   * The dialog displayed when the user selects the "Function" menu item from
   * the popup menu. Initialized in the ctor, never <code>null</code> after
   * initialization.
   */
  FunctionsPropertyDialog m_functionsDialog = null;

  /**
   * The dialog displayed when the user selects the "Single Value" menu item
   * from the popup menu when the variable column is clicked. Initialized in
   * the ctor, never <code>null</code> after initialization.
   */
  ValueSelectorDialog m_columnDialog = null;

  /**
   * The dialog displayed when the user selects the "Single Value" menu item
   * from the popup menu when the value column is clicked. Initialized in
   * the ctor, never <code>null</code> after initialization.
   */
  ValueSelectorDialog m_xmlDialog = null;

  /**
   * The FlexibleQueryBuildDialog associated with the "Build Query" button.
   */
  FlexibleQueryBuilderDialog m_flexDialog = null;
  /**
   * the selector formula editor
   */
   JTextArea m_select = new JTextArea();

  /**
   * create key index
   */
  UTFixedButton m_indexButton = null;

  /**
   * switches the make distinct mode on/off.
   */
  JCheckBox m_distinctBox = new JCheckBox(getResources().getString("distinct"));

  /**
   * this flag will be set if any data within this dialog was modified
   */
  private boolean m_modified = false;
   /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel = null;
   /**
   * the dialog size
   */
  private final static Dimension DIALOG_SIZE = new Dimension(610, 303);

   /**
   * the validation framework variables
   */
   //////////////////////////////////////////////////////////////////////////////
  private static final int NUM_COMPONENTS_VALIDATED = 1;
  private final Component m_validatedComponents[] = new Component[NUM_COMPONENTS_VALIDATED];
  private final ValidationConstraint m_validationConstraints[] = new ValidationConstraint[NUM_COMPONENTS_VALIDATED];
}

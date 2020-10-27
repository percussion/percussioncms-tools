/*[ BinaryResourceDialog.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.util.PSCollection;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Vector;

/** Configures the BinaryResource object (BlobDataSet).  The user will have to
  * manually enter the following data in order to fetch the image from the
  * database.
   * <UL>
  * <LI>Table Alias    (required for More button)
  * <LI>Driver name    (required for More button)
  * <LI>Server name    (required for More button)
  * <LI>Database name
  * <LI>Table name     (required for More button)
  * <LI>Column name to select
  * <LI>A set of WHERE clauses as the keys to the query
  * <LI>And the image MIME type (usually gif or jpeg)
  * </UL>
*/

public class BinaryResourceDialog extends PSEditorDialog
{
//
// CONSTRUCTORS
//

  public BinaryResourceDialog()
  {
    initDialog();
    initListeners();
    setResizable( true );
  }


//
// PUBLIC METHODS
//

   /** Actions performed by clicking the more button.  Validates &quot;Table info
    * &quot; Fields by running a catalog on the information provided.  If any
    * error occurs, a warning will be given to the user for entering invalid
    * info.  If the catalog succeeds, the Query Keys table is enabled, and
    * dialog is expanded to display it.
    */
   public void onMore()
   {
      try
      {
         if (!verifyTableInfoData( m_aliasField.getText(),
            m_datasourceCombo.getSelectedDatasource(),
            m_tableField.getText()))
         {
            throw new Exception();
         }
      }
      catch (Exception e)
      {
         // if code gets here, then the data in "Table info" is invalid.  show
         // a warning dialog then do nothing.
         JOptionPane.showMessageDialog( this,
            getResources().getString("tableinfoerror"),
            getResources().getString("error"),
            JOptionPane.OK_OPTION );

         return;
      }

      // if code gets here, then the data is valid.  Enable the QueryKeys table
      // and disables the textfields (except Column textField)
      enableQueryKeyTable(true);

      // gathering data to set "Column name" column in querykeys table to use
      // DTBackEndColumn
      OSBackendDatatank backendTank = null;
      try
      {
         OSBackendTable table = new OSBackendTable(m_aliasField.getText());
         table.setDataSource(m_datasourceCombo.getSelectedDatasource());
         table.setTable(m_tableField.getText());

         if (null != table)
         {
            Vector v = CatalogBackendColumns.getCatalog( table, false );
            if(v != null)
            {
               m_colCatalogId++;

               m_columnBox.removeAllItems(); // clear all previous items
               for (int i = 0; i < v.size(); i++)
               {
                  m_columnBox.addItem(v.elementAt(i));
               }
            }
         }

         PSCollection collection = new PSCollection(
            "com.percussion.design.objectstore.PSBackEndTable");
         collection.add(table);

         backendTank = new OSBackendDatatank();
         backendTank.setTables(collection);
      }
      catch (Exception e)
      {
         // this should never happen
         e.printStackTrace();
      }

      ValueSelectorDialogHelper vsColumnHelper =
         new ValueSelectorDialogHelper(backendTank, null);
      m_columnDialog.refresh(vsColumnHelper.getDataTypes());

      m_queryKeyPanel.setVisible(true);
      m_mimeTypePanel.setVisible(true);
      setSize(FULL_SIZE);
      center();
      m_moreButton.setEnabled(false);
   }


/** Actions performed by clicking &quot;OK&quot;. Saves all entered information
  * to their appropriate objects.
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
            // do conditionals first, since they have their own validation
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

            m_pipe.getDataSelector().setWhereClauses(m_table.saveToConditionals( m_pipe.getDataSelector().getWhereClauses()));
            m_dataset.setRequestor(m_requestor);

            ////////////////////////////////////////////////////////////////////////
            // setting back end table values
            //
            if (null == m_beTable)
            {
               m_beTable = new OSBackendTable();
               m_beTable.setAlias(m_aliasField.getText());
            }
            else
            {
               m_beTable.setAlias(m_aliasField.getText());
            }

            m_beTable.setDataSource(m_datasourceCombo.getSelectedDatasource());
            m_beTable.setTable(m_tableField.getText());

            ////////////////////////////////////////////////////////////////////////
            // creating BackEndTank properties and creating the pipe too
            //
            PSCollection tableCollection = new PSCollection(
               "com.percussion.design.objectstore.PSBackEndTable");
            tableCollection.add(m_beTable);
            m_tank.setTables(tableCollection);
            m_pipe.setBackEndDataTank(m_tank);

            ////////////////////////////////////////////////////////////////////////
            // setting back end column value
            //
            String columnName = "";
            columnName = m_columnBox.getSelectedItem().toString();

            if (null == m_column)
            {
               m_column = new PSBackEndColumn(m_beTable, columnName);
            }
            else
            {
               m_column.setTable(m_beTable);
               m_column.setColumn(columnName);
            }

            ////////////////////////////////////////////////////////////////////////
            // creating DataMapper
            //
            PSDataMapping mapping = new PSDataMapping("IMAGE", m_column);
            OSDataMapper mapper = new OSDataMapper();
            mapper.add(mapping);

            // set MIME type info
            PSBackEndColumn col = null;
            if ( m_useColumnRadio.isSelected())
            {
               String colName = (String) m_columnName.getSelectedItem();
               // is the column name fully qualified?
               String tableName = m_beTable.getTable();
               if ( colName.startsWith( tableName + "." ))
                  // strip off the table before creating the column object
                  colName = colName.substring( tableName.length() + 1 );
               col = new PSBackEndColumn( m_beTable, colName );
            }
            m_requestor.setOutputMimeType( col );

            m_pipe.setDataMapper(mapper);

            // done creating all the needed stuff! Setting Pipe to DataSet
            m_dataset.setPipe(m_pipe);

         }
         catch (ClassNotFoundException e )
         {
        // failed when creating new collection for WHERE clauses
            PSDlgUtil.showError( e );
         }
         catch (IllegalArgumentException ex )
         {
        // failed when creating new collection for WHERE clauses
            PSDlgUtil.showError( ex );
         }

         dispose();
      }
   }


   /**
    * Implementation of PSEditorDialog. TODOph: Don't set the passed in data
    * object until the user presses Ok. etc...
    */
   public boolean onEdit(UIFigure figure, Object data)
   {
      m_moreButton.setEnabled(true);
      if (figure.getData() instanceof OSBinaryDataset)
      {
         m_isTableValid = false;
         m_dataset = (OSBinaryDataset) figure.getData();

         // ////////////////////////////////////////////////////////////////////////
         // checking for existing pipe, backendDatatank, and backendTables;
         // setting up BackendColumn Selection for ValueSelectorDialog of the
         // table.
         //
         m_pipe = (OSQueryPipe) m_dataset.getPipe();
         if (null != m_pipe)
         {
            m_tank = (OSBackendDatatank) m_pipe.getBackEndDataTank();
            if (null != m_tank)
            {
               m_beTable = (OSBackendTable) m_tank.getTables().get(0);

               // checking if the Table info data is correct
               m_isTableValid = verifyTableInfoData(m_beTable.getAlias(),
                  m_beTable.getDataSource() == null ? "" : m_beTable
                     .getDataSource(), m_beTable.getTable());
            }
            else
            {
               m_tank = new OSBackendDatatank();
            }
         }
         else
         {
            // unless someone removed these from the dataset, this should never
            // be necessary
            try
            {
               m_pipe = new OSQueryPipe();

               // create a tank with an empty table for "Column name" column for
               // first time entry/creation
               OSBackendTable table = new OSBackendTable();
               PSCollection tableCollection = new PSCollection(
                  "com.percussion.design.objectstore.PSBackEndTable");
               tableCollection.add(table);

               m_tank = new OSBackendDatatank();
               m_tank.setTables(tableCollection);
            }
            catch (IllegalArgumentException e)
            {
               e.printStackTrace();
            }
            catch (ClassNotFoundException ex)
            {
               ex.printStackTrace();
            }

         }

         // if Table info data fields are valid, enable the Query Key table;
         enableQueryKeyTable(m_isTableValid);

         // if Table info data fields are valid, change "Column name" column
         if (m_isTableValid)
         {
            ValueSelectorDialogHelper vsColumnHelper = new ValueSelectorDialogHelper(
               m_tank, null);
            m_columnDialog.refresh(vsColumnHelper.getDataTypes());

            m_queryKeyPanel.setVisible(true);
            m_mimeTypePanel.setVisible(true);
            m_moreButton.setEnabled(false);
            setSize(FULL_SIZE);
            center();
         }

         // ////////////////////////////////////////////////////////////////////////
         // getting OSBackendTable properties
         //
         if (null != m_beTable)
         {
            // setting queried data into the textfields
            m_aliasField.setText(m_beTable.getAlias());
            m_datasourceCombo.setSelectedDatasource(m_beTable.getDataSource());
            m_tableField.setText(m_beTable.getTable());

            // disabling/enabling the textfields and their labels depending if
            // the
            // data provided in "Table info" is incorrect/correct.
            // also disables/enables the QueryKeys table.
            setReadOnly(m_beTable.isReadOnly());
         }

         // ////////////////////////////////////////////////////////////////////////
         // getting OSRequestor properties
         //
         m_requestor = (OSRequestor) m_dataset.getRequestor();

         if (null == m_requestor)
         {
            try
            {
               OSRequestor requestor = new OSRequestor();
               m_dataset.setRequestor(requestor);
            }
            catch (IllegalArgumentException e)
            {
               e.printStackTrace();
            }
         }

         // ////////////////////////////////////////////////////////////////////////
         // * Looking for existing DataMappings *
         // If mapping exists, get the BackEndColumn from the mapping to fill in
         // the column textfield.
         // Otherwise, do nothing.
         if (null != m_pipe)
         {
            if (null == m_pipe.getDataMapper())
            {
               try
               {
                  m_pipe.setDataMapper(new OSDataMapper());
               }
               catch (IllegalArgumentException e)
               {
                  e.printStackTrace();
               }
            }

            if (0 < m_pipe.getDataMapper().size())
               m_column = (PSBackEndColumn) ((PSDataMapping) m_pipe
                  .getDataMapper().get(0)).getBackEndMapping();

            if (null != m_beTable)
            {
               Vector v = new Vector(5);
               v = m_beTable.getColumns();

               m_columnBox.removeAllItems(); // clear all previous items
               for (int i = 0; i < v.size(); i++)
               {
                  m_columnBox.addItem(v.elementAt(i));
               }
            }
         }

         if (null != m_column)
         {
            for (int i = 0; i < m_columnBox.getItemCount(); i++)
            {
               if (m_columnBox.getItemAt(i).toString().equals(
                  m_column.getColumn()))
               {
                  m_columnBox.setSelectedIndex(i);
                  break;
               }
            }
         }
         else
         {
            if (0 < m_columnBox.getItemCount())
            {
               m_columnBox.setSelectedIndex(0);
               try
               {
                  m_column = new PSBackEndColumn(m_beTable, m_columnBox
                     .getSelectedItem().toString());
               }
               catch (IllegalArgumentException e)
               {
                  e.printStackTrace();
               }
            }
         }

         // ////////////////////////////////////////////////////////////////////////
         // saving dialog table and converting table data into PSWhereClauses
         // * DataSelector is created now if it does not exist
         if (null == m_pipe.getDataSelector())
            m_pipe.setDataSelector(new OSDataSelector());

         OSDataSelector selector = (OSDataSelector) m_pipe.getDataSelector();
         if (null == selector)
         {
            selector = new OSDataSelector();
            m_pipe.setDataSelector(selector);
         }
         m_table.loadFromConditionals(selector.getWhereClauses());
         m_table.appendRow(128 - m_table.getRowCount());

         IPSReplacementValue col = m_requestor.getOutputMimeType();
         m_useDefaultRadio.setSelected(true);
         m_useColumnRadio.setSelected(null != col);
         if (null != col)
            m_columnName.setSelectedItem(col.getValueDisplayText());

         initGraying();

         setVisible(true);
      }

      return true;
   }

   public WhereClauseTableModel getClauseTable()
   {
      return m_table;
   }

//
// PRIVATE METHODS
//

   /**
    * Takes the text strings in the driver, server, database, and table fields
    * for verification. If the catalog returns an entry that matches the table
    * name, a <CODE>true</CODE> is returned to resemble verification success.
    * 
    * @param datasource The datasource name
    * @param table Table name
    * @param column Column name
    * 
    * @returns boolean <CODE>true</CODE> = verification passed; <CODE>false</CODE> =
    * verification failed.
    */
   private boolean verifyTableInfoData(String alias, String datasource,
      String table, String column)
   {
      if (alias.equals(""))
         return false;
      if (datasource == null)
         return false;
      if (table.equals(""))
         return false;
      if (column.equals(""))
         return false;

      return true;
   }

   private boolean verifyTableInfoData(String alias, String datasource,
      String table)
   {
      if (alias.equals(""))
         return false;
      if (datasource == null)
         return false;
      if (table.equals(""))
         return false;

      return true;
   }

   /**
    * Enables/disables all the Data Fields.
    * 
    * @param disable <CODE>true</CODE> disables the fields.
    */
   private void setReadOnly(boolean enable)
   {
      enable = (!enable);

      // alias should always be enabled
      m_aliasField.setEnabled(true);
      m_aliasLabel.setEnabled(true);

      m_datasourceCombo.setEnabled(enable);
      m_tableField.setEnabled(enable);

      m_datasourceLabel.setEnabled(enable);
      m_tableLabel.setEnabled(enable);
   }

   /**
    * Enables/Disables the Query Keys table by setting the table editors&apos;
    * ClickCountToStart from 1 to MAX_VALUE of Integer.
    * 
    * @param enable <CODE>true</CODE> enables the table.
    */
   private void enableQueryKeyTable(boolean enable)
   {
      int i = Integer.MAX_VALUE;
      if (enable)
      {
         i = 1;
      }

      m_columnEditor.setClickCountToStart(i);
      m_opEditor.setClickCountToStart(i);
      m_xmlEditor.setClickCountToStart(i);
      m_boolEditor.setClickCountToStart(i);
   }


   /**
    * Creates the OK/cancel/help buttons panel.
    */
   private JPanel createCommandPanel()
   {
      m_commandPanel = new UTStandardCommandPanel(this, "",
         SwingConstants.VERTICAL)
      {
         public void onOk()
         {
            BinaryResourceDialog.this.onOk();
         }

         public void onCancel()
         {
            BinaryResourceDialog.this.onCancel();
         }
      };
      m_commandPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      return m_commandPanel;
   }

   /**
    * Creates the &quot;Table Info&quot; panel.
    */
   private JPanel createTableInfoPanel()
   {
      createLabels();

      JPanel aliasPanel = new JPanel();
      aliasPanel.setLayout(new BoxLayout(aliasPanel, BoxLayout.X_AXIS));
      aliasPanel.add(m_aliasLabel);
      aliasPanel.add(Box.createHorizontalStrut(4));
      aliasPanel.add(m_aliasField);

      JPanel datasourcePanel = new JPanel();
      datasourcePanel
         .setLayout(new BoxLayout(datasourcePanel, BoxLayout.X_AXIS));
      datasourcePanel.add(m_datasourceLabel);
      datasourcePanel.add(Box.createHorizontalStrut(4));
      datasourcePanel.add(m_datasourceCombo);
      
      JPanel tablePanel = new JPanel();
      tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.X_AXIS));
      tablePanel.add(m_tableLabel);
      tablePanel.add(Box.createHorizontalStrut(4));
      tablePanel.add(m_tableField);

      m_columnPanel = new JPanel();
      m_columnPanel.setLayout(new BoxLayout(m_columnPanel, BoxLayout.X_AXIS));
      m_columnPanel.add(m_columnLabel);
      m_columnPanel.add(Box.createHorizontalStrut(4));
      m_columnPanel.add(m_columnBox);

      JPanel innerPanel = new JPanel();
      innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
      innerPanel.setBorder(new EmptyBorder(0, 0, 6, 6));
      innerPanel.add(aliasPanel);
      innerPanel.add(Box.createVerticalStrut(4));
      innerPanel.add(datasourcePanel);
      innerPanel.add(Box.createVerticalStrut(4));
      innerPanel.add(tablePanel);
      innerPanel.add(Box.createVerticalStrut(4));
      innerPanel.add(m_columnPanel);
      innerPanel.add(Box.createVerticalGlue());

      JPanel tableInfoPanel = new JPanel(new BorderLayout());
      tableInfoPanel.setBorder(new TitledBorder(new EtchedBorder(
         EtchedBorder.LOWERED), getResources().getString("tableinfo")));
      tableInfoPanel.add(innerPanel, BorderLayout.CENTER);

      return tableInfoPanel;
   }

/** Creates the &quot;Query Keys&quot; panel.
*/
  private JPanel createQueryKeyPanel()
  {
      // initializing QueryKeyPanel
      JPanel queryKeyPanel = new JPanel();
      queryKeyPanel.setLayout(new BoxLayout(queryKeyPanel, BoxLayout.Y_AXIS ));
      Border tb = BorderFactory.createTitledBorder(
         new EtchedBorder( EtchedBorder.LOWERED ),
         getResources().getString( "querykeys" ));
      // leave more space between the titled border and the components
      Border eb = BorderFactory.createEmptyBorder( 0, 5, 5, 5 );
      queryKeyPanel.setBorder( BorderFactory.createCompoundBorder( tb, eb ));
      queryKeyPanel.add(createQueryKeyTable(m_table));

      return queryKeyPanel;
   }

   private JScrollPane createQueryKeyTable(WhereClauseTableModel table)
   {
      ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
         (OSBackendDatatank) null, null);
      m_columnDialog = new ValueSelectorDialog(this, h.getDataTypes(), null);
      m_columnEditor = new ValueSelectorCellEditor(m_columnDialog);
      m_columnEditor.setClickCountToStart(1);

      m_xmlDialog = new ValueSelectorDialog(this, h.getDataTypes(), null);

      m_xmlEditor = new ValueSelectorCellEditor(m_xmlDialog, null);

      m_xmlEditor.setClickCountToStart(1);

      // define the cell editors
      m_tableView.getColumn(WhereClauseTableModel.VARIABLE_COL_NAME)
         .setCellEditor(m_columnEditor);
      m_tableView.getColumn(WhereClauseTableModel.VARIABLE_COL_NAME)
         .setPreferredWidth(150);
      m_tableView.getColumn(WhereClauseTableModel.OPERATOR_COL_NAME)
         .setCellEditor(m_opEditor);
      m_tableView.getColumn(WhereClauseTableModel.OPERATOR_COL_NAME)
         .setPreferredWidth(50);
      m_tableView.getColumn(WhereClauseTableModel.VALUE_COL_NAME)
         .setCellEditor(m_xmlEditor);
      m_tableView.getColumn(WhereClauseTableModel.VALUE_COL_NAME)
         .setPreferredWidth(150);
      m_tableView.getColumn(WhereClauseTableModel.BOOL_COL_NAME).setCellEditor(
         m_boolEditor);
      m_tableView.getColumn(WhereClauseTableModel.BOOL_COL_NAME)
         .setPreferredWidth(30);
      m_tableView.getColumn(WhereClauseTableModel.OMIT_COL_NAME).setCellEditor(
         new UTCheckBoxCellEditor());
      m_tableView.getColumn(WhereClauseTableModel.OMIT_COL_NAME)
         .setCellRenderer(new UTCheckBoxCellRenderer());
      m_tableView.getColumn(WhereClauseTableModel.OMIT_COL_NAME)
         .setPreferredWidth(30);

      // set operator cell renderer
      m_tableView.getColumn(WhereClauseTableModel.OPERATOR_COL_NAME)
         .setCellRenderer(new UTOperatorComboBoxRenderer());

      // do not allow column reordering
      m_tableView.setPreferredScrollableViewportSize(new Dimension(450, 160));
      enableQueryKeyTable(false); // default

      JScrollPane pane = new JScrollPane(m_tableView,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      return pane;
   }

  private JPanel createTopRightPanel()
  {
    m_moreButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onMore();
      }
    });

    JPanel Bpanel = new JPanel();
    Bpanel.add(m_moreButton);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(createCommandPanel(), BorderLayout.NORTH);
    panel.add(Bpanel, BorderLayout.SOUTH);

    return panel;
  }

/** Initializes the labels in TableInfoPanel.
*/
  private void createLabels()
  {
    m_aliasLabel = new JLabel(getResources().getString("alias"), SwingConstants.RIGHT);
    m_aliasLabel.setPreferredSize(LABEL_SIZE);
    m_aliasLabel.setMaximumSize(m_aliasLabel.getPreferredSize());
    m_aliasLabel.setMinimumSize(m_aliasLabel.getPreferredSize());

    m_datasourceLabel = new JLabel(getResources().getString("datasource"), 
       SwingConstants.RIGHT);
    m_datasourceLabel.setPreferredSize(LABEL_SIZE);
    m_datasourceLabel.setMaximumSize(m_datasourceLabel.getPreferredSize());
    m_datasourceLabel.setMinimumSize(m_datasourceLabel.getPreferredSize());

    m_tableLabel = new JLabel(getResources().getString("table"), SwingConstants.RIGHT);
    m_tableLabel.setPreferredSize(LABEL_SIZE);
    m_tableLabel.setMaximumSize(m_tableLabel.getPreferredSize());
    m_tableLabel.setMinimumSize(m_tableLabel.getPreferredSize());

    m_columnLabel = new JLabel(getResources().getString("column"), SwingConstants.RIGHT);
    m_columnLabel.setPreferredSize(LABEL_SIZE);
    m_columnLabel.setMaximumSize(m_columnLabel.getPreferredSize());
    m_columnLabel.setMinimumSize(m_columnLabel.getPreferredSize());
  }

/** Default initalizer for the constructors.
*/
   private void initDialog()
   {
      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
      topPanel.add(createTableInfoPanel());
      topPanel.add(createTopRightPanel());

      // initialize both the main and more panels
      m_mainPanel = new JPanel();
      m_mainPanel.setLayout(new BoxLayout(m_mainPanel, BoxLayout.Y_AXIS));
      m_mainPanel.add(topPanel);

      m_queryKeyPanel = createQueryKeyPanel();
      m_queryKeyPanel.setVisible(false);
      m_mimeTypePanel = createMimeTypePanel();
      m_mimeTypePanel.setVisible(false);
      m_mainPanel.add(Box.createVerticalStrut(5));
      m_mainPanel.add(m_queryKeyPanel);
      m_mainPanel.add(Box.createVerticalStrut(5));
      m_mainPanel.add(m_mimeTypePanel);


      // if More button loses focus, give focus back to OK button
      m_moreButton.addFocusListener(new FocusAdapter()
      {
         public void focusLost(FocusEvent e)
         {
            getRootPane().setDefaultButton(m_commandPanel.getOkButton());
         }
      });


      ArrayList comps = new ArrayList();
      ArrayList constraints = new ArrayList();
      ValidationConstraint nonEmpty = new StringConstraint();
      comps.add( m_aliasField );
      constraints.add( new StringConstraint(StringConstraint.NO_SPECIAL_CHAR));
      comps.add( m_tableField );
      constraints.add( nonEmpty );
      comps.add( m_columnBox );
      constraints.add( nonEmpty );
      comps.add( m_columnName );
      constraints.add( new ConditionalValidator( m_useColumnRadio, nonEmpty ));

      int size = constraints.size();
      m_componentArray = new Component[size];
      m_constraintArray = new ValidationConstraint[size];
      comps.toArray( m_componentArray );
      constraints.toArray( m_constraintArray );

      setValidationFramework(m_componentArray, m_constraintArray);

      getContentPane().add(m_mainPanel);
      setSize(NO_TABLE_SIZE);
      center();
   }


   /**
    * Builds all the controls that are used to gather information about the
    * MIME type to use when returning the content for this request.
    *
    * @return A panel that contains the controls and a grouping, titled border.
    */
   private JPanel createMimeTypePanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ));
      String title = getResources().getString("MimeTypeGroupTitle");
      panel.setBorder( BorderFactory.createTitledBorder(
         new EtchedBorder(EtchedBorder.LOWERED), title ));

      // create controls we couldn't create statically
      m_useDefaultRadio = new JRadioButton(
         getResources().getString("DefaultRadioLabel"));
      m_useColumnRadio = new JRadioButton(
         getResources().getString("UseColumnRadioLabel"));
      /* We save the label in a local variable because we need to be able to
         gray it out dynamically. */
      m_columnNameLabel = new UTFixedLabel(
         getResources().getString("MimeColumnChoiceLabel"), JLabel.RIGHT );

      ButtonGroup grp = new ButtonGroup();
      grp.add( m_useDefaultRadio );
      grp.add( m_useColumnRadio );

      Box row = Box.createHorizontalBox();
      row.add( m_useDefaultRadio );
      row.add( Box.createHorizontalGlue());
      panel.add( row );

      row = Box.createHorizontalBox();
      row.add( m_useColumnRadio );
      row.add( Box.createHorizontalGlue());
      panel.add( Box.createVerticalStrut(5));
      panel.add( row );

      row = Box.createHorizontalBox();
      row.add( Box.createHorizontalStrut(20));
      row.add( m_columnNameLabel );
      row.add( Box.createHorizontalStrut(5));
      row.add( m_columnName );
      row.add( Box.createHorizontalGlue());
      panel.add( Box.createVerticalStrut(5));
      panel.add( row );

      return panel;
   }


   /**
    * Adds listeners to all controls that need them for whatever reason. Two
    * common reasons are graying of controls based on another control and
    * dynamically cataloging drop lists.
    */
   private void initListeners()
   {
      m_useColumnRadio.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent evt )
         {
            m_columnName.setEnabled( true );
            m_columnNameLabel.setEnabled( true );
         }
      });

      m_useDefaultRadio.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent evt )
         {
            m_columnName.setEnabled( false );
            m_columnNameLabel.setEnabled( false );
         }
      });

      m_columnName.getEditor().getEditorComponent().addFocusListener(
         new FocusAdapter()
         {
            public void focusGained( FocusEvent evt )
            {
               if ( m_colCatalogCache != m_colCatalogId )
               {
                  //System.out.println( "Filling drop list for m_columnName" );
                  int ct = m_columnBox.getItemCount();
                  for ( int i = 0; i < ct; ++i )
                  {
                     m_columnName.addItem( m_columnBox.getItemAt(i));
                  }
                  m_colCatalogCache = m_colCatalogId;
               }
            }
         });
   }


   /**
    * Checks all controls that might possibly be dynamically grayed and grays
    * them out if they should be. This method should be only be called when
    * the dialog is first created or reset. After that, listeners added to the
    * controls will take care of graying.
    *
    * @see #initListeners
    */
   private void initGraying()
   {
      m_columnName.setEnabled( m_useColumnRadio.isSelected());
   }


//
// MEMBER VARIABLES
//

  // items for TableInfoPanel
  private UTFixedTextField m_aliasField = new UTFixedTextField("", FIELD_SIZE);
  private UTFixedTextField m_tableField = new UTFixedTextField("", FIELD_SIZE);
  private DatasourceComboBox m_datasourceCombo = new DatasourceComboBox(
     FIELD_SIZE);
  private PSComboBox m_columnBox = new PSComboBox(FIELD_SIZE);

  private JPanel m_columnPanel;

  private JLabel m_aliasLabel, m_datasourceLabel, m_tableLabel, m_columnLabel;

  private UTFixedButton m_moreButton = new UTFixedButton(getResources().getString("more"));

  private JPanel m_mainPanel;

  /**
   * The panel containing the query key table.  Initialized in the ctor, not
   * visible until the more button is clicked or the resource is edited with
   * all top panel fields filled in.  Never <code>null</code> after it is
   * intialized.
   */
  private JPanel m_queryKeyPanel;

  /**
   * The panel containing the query key table.  Initialized in the ctor, not
   * visible until the more button is clicked or the resource is edited with
   * all top panel fields filled in.  Never <code>null</code> after it is
   * intialized.
   */
  private JPanel m_mimeTypePanel;

  private boolean m_isTableValid = false;
  /**
   * the where clause table
   */
   WhereClauseTableModel m_table = new WhereClauseTableModel();
  UTJTable m_tableView = new UTJTable(m_table);

  private ValueSelectorDialog m_columnDialog;
  private ValueSelectorDialog m_xmlDialog;

  private ValueSelectorCellEditor m_columnEditor;
  private ValueSelectorCellEditor m_xmlEditor;
  private DefaultCellEditor m_opEditor = new DefaultCellEditor(new UTOperatorComboBox());
  private DefaultCellEditor m_boolEditor = new DefaultCellEditor(new UTBooleanComboBox());

   /**
    * This is used as a signal to the <code>m_columnName</code> list to know
    * if it needs to update it's drop list. This value is incremented each
    * time the main column field is cataloged. Each time the MIME type col
    * field gets focus, it checks to see if a catalog has occurred since the
    * last time it was activated. If it has, it gets all names from the main
    * column list and puts them in its own list. <code>m_colCatalogCache</code>
    * is used when checking for a difference.
    */
   private int m_colCatalogId = 0;

   /**
    * Used to determine when to reset the drop list on the <code>m_colunnName
    * </code> combobox. This value is compared to <code>m_colCatalogId</code>.
    * If different, the current list is cleared and all values in <code>
    * m_columnBox</code> are copied to <code>m_columnName</code>.
    */
   private int m_colCatalogCache = m_colCatalogId-1;

   /**
    * One of the radio buttons used to select the source for the MIME type
    * descriptor. If selected, the MIME type comes from the resource.
    */
   private JRadioButton m_useDefaultRadio;
   /**
    * One of the radio buttons used to select the source for the MIME type
    * descriptor. If selected, the MIME type is set dynamically set from the
    * specified column.
    */
   private JRadioButton m_useColumnRadio;

   /**
    * The name of the column to extract the MIME Content-type HTTP header
    * value from. Grayed out unless the <code>m_useColumnRadio</code> is
    * selected.
    */
   private UTFixedHeightComboBox m_columnName = new UTFixedHeightComboBox();

   /**
    * The label for the <code>m_columnName</code> control. Normally, we don't
    * keep local copies of labels, but we do it now so we can dynamically gray
    * it.
    */
   private UTFixedLabel m_columnNameLabel;

  /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel;

   private OSBinaryDataset m_dataset;

   // indices used for constraints setup
  private static int ALIAS_FIELD_INDEX = 0;
  private static int TABLE_FIELD_INDEX = 1;
  private static int COLUMN_FIELD_INDEX = 2;

   private static int NUMBER_OF_COMPONENTS_TO_VALIDATE = 3;
  // Validation members
   private ValidationConstraint[] m_constraintArray = new ValidationConstraint[NUMBER_OF_COMPONENTS_TO_VALIDATE];
   private Component[] m_componentArray = new Component[NUMBER_OF_COMPONENTS_TO_VALIDATE];

  // Server objects
   private OSQueryPipe m_pipe;
   private OSBackendDatatank m_tank;
   private OSBackendTable m_beTable;
  private OSRequestor m_requestor;
  private PSBackEndColumn m_column;
  private PSCollection m_whereClauses;

  private UIConnectionPoint m_cp;

  //Constants
  private static final Dimension LABEL_SIZE = new Dimension(80, 22);
  private static final Dimension FIELD_SIZE = new Dimension(200, 22);
  private static final Dimension FULL_SIZE = new Dimension(501, 520);
  private static final Dimension NO_TABLE_SIZE = new Dimension(471, 255);
}

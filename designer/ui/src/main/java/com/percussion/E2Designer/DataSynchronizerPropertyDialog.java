/*[ DataSynchronizerPropertyDialog.java ]**************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSUpdateColumn;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * This class is for editing the properties of the DataSynchronizer object.
 */
public class DataSynchronizerPropertyDialog extends PSEditorDialog
{

   /**
    *
    * Default constructor with no parameters
    */
   public DataSynchronizerPropertyDialog()
   {
      super();
      initDialog();
   }

   /**
   *
   *Construct the DataSynchronizerProperty dialog with the OSDataSynchronizer object
    */
   public DataSynchronizerPropertyDialog(
      JFrame parent,
      OSDataSynchronizer sync)
   {
      super(parent);
      this.setLocationRelativeTo(parent);

      m_sync = sync;
      initDialog();
   }

   /**
    * This method is called when a data object needs to be visually edited.
    *
    * @returns if the dialog is modal, returns <code>true</code> if the state of
    * the object was changed by the user, <code>false</code> otherwise. If the editor is not
    * modal, the return value is undefined.
    *
    * @param figure the screen object that contains the data that this editor
    * knows how to deal with. The actual data object can be obtained from
    * the figure by calling getData() on it.
    *
    * @param data the data object of the frame containing the figure supplied
    * as the first parameter. This object should not be modified.
    *
    * @throws IllegalArgumentException if Data is not an instance of the type
    * required by the editor implementing this interface
    *
    * @see IEditor
   */
   public boolean onEdit(UIFigure figure, final Object data)
   {
      if (figure == null)
      {
         throw new IllegalArgumentException("Passed in UIFigure object is null");
      }

      if (figure.getData() instanceof OSDataSynchronizer)
      {
         m_sync = (OSDataSynchronizer) figure.getData();
      }
      else
         throw new IllegalArgumentException("UIFigure's getData did not return an OSDataSynchronizer object");

      m_figure = figure;

      // find and initialize the member m_osBackendDatatank with the OSBackendDatatank that this DataSynchronizer is attached to
      try
      {
         UTPipeNavigator navigator = new UTPipeNavigator();
         UIFigure figBackEndDataTank = navigator.getBackendTank(figure);
         if (figBackEndDataTank != null)
         {
            m_osBackendDatatank =
               (OSBackendDatatank) figBackEndDataTank.getData();
            if (m_osBackendDatatank == null)
            {
               JOptionPane.showMessageDialog(
                  this,
                  getResources().getString("NO_BACKEND_DATATANK"),
                  getResources().getString("WARNING"),
                  JOptionPane.WARNING_MESSAGE);
               throw new IllegalArgumentException("OSBackendDatatank object is null");
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      initDialogFields();
      center();
      setVisible(true);

      return m_bModified;

   }

   /**
     * Handler for OK button clicked event.
     */
   public void onOk()
   {
      m_bModified = true;
      //    System.out.println("OK button clicked");

      if ((jCheckBoxDelete.isSelected() || jCheckBoxUpdate.isSelected())
         && getKeyColumnNames().isEmpty())
      {
         // popup warning message if either delete or update are checked and no
         // key column selected.
         int selection =
            JOptionPane.showConfirmDialog(
               this,
               getResources().getString("NO_KEY_COLUMNS_SELECTED"),
               getResources().getString("WARNING"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE);

         // return if user selects no
         if (selection == JOptionPane.NO_OPTION)
            return;
      }

      stopTableCellEditing();

      m_sync.setInsertingAllowed(jCheckBoxCreate.isSelected());
      m_sync.setDeletingAllowed(jCheckBoxDelete.isSelected());
      m_sync.setUpdatingAllowed(jCheckBoxUpdate.isSelected());

      Collection c = getKeyColumnNames();
      Iterator iter = c.iterator();
      // get the key column names and check against the Mapper columns
      while(iter.hasNext())
      {
         String strKeyColName = (String) iter.next();
         if (!isColumnPresent(strKeyColName, m_mapperColumns))
         {
            // popup warning message if column NOT found in mapper's column list
            JOptionPane.showMessageDialog(
               this,
               strKeyColName
                  + "  "
                  + getResources().getString("COLUMN_NOT_IN_MAPPER"),
               getResources().getString("WARNING"),
               JOptionPane.WARNING_MESSAGE);
         }
      }

      try
      {
         // the combination of all columns in vUpdateColumns and vKeyColumns
         // form the collection of Update Columns to be set in the PSDataSynchronizer object
         Vector vUpdateColumns = getUpdateColumnsInMapperThatAreNotKey();
         Iterator vKeyColumns = getKeyColumnsFromTable().iterator();
         PSCollection psCollUpdateColumns = new PSCollection(PSUpdateColumn.class);

         for (int i = 0; i < vUpdateColumns.size(); i++)
         {
            PSBackEndColumn psBECol =
               (PSBackEndColumn) vUpdateColumns.elementAt(i);
            PSUpdateColumn psUC = new PSUpdateColumn(psBECol, false);
            //FOR V1 only due to no Integrity: Since mutually exclusive, if isUpdateable, cannot be isKey
            psUC.setUpdateable(true);
            psCollUpdateColumns.add(psUC);
         }
         
         while(vKeyColumns.hasNext())
         {
            PSBackEndColumn psBECol =
               (PSBackEndColumn) vKeyColumns.next();
            PSUpdateColumn psUC = new PSUpdateColumn(psBECol, true);
            psUC.setUpdateable(false);
            // FOR V1 only due to no Integrity: Since mutually exclusive, if isKey, cannot be isUpdateable
            psCollUpdateColumns.add(psUC);
         }

         m_sync.setUpdateColumns(psCollUpdateColumns);

      }
      catch (IllegalArgumentException e)
      {
         PSDlgUtil.showError(e);
      }

      dispose();
   }

   /**
    * Handler for Cancel button clicked event.
    */
   public void onCancel()
   {
      super.close(1, true);
   }

   /**
    * Initializes the dialog by creating the controls of the dialog and initializes listeners.
    */
   private void initDialog()
   {
      this.getContentPane().setLayout(null);
      this.setSize(420, 325);

      createControls();
      createKeyColumnTable();
      initListeners();

      getRootPane().setDefaultButton(jButtonOK);
   }

   /**
    * Creates the controls of the dialog.
    */
   private void createControls()
   {
      jCheckBoxCreate = new JCheckBox();
      jCheckBoxCreate.setText(getResources().getString("ALLOW_CREATES"));
      jCheckBoxCreate.setActionCommand(
         getResources().getString("ALLOW_CREATES"));
      jCheckBoxCreate.setBounds(15, 10, 150, 20);
      getContentPane().add(jCheckBoxCreate);

      jCheckBoxDelete = new JCheckBox();
      jCheckBoxDelete.setText(getResources().getString("ALLOW_DELETES"));
      jCheckBoxDelete.setActionCommand(
         getResources().getString("ALLOW_DELETES"));
      jCheckBoxDelete.setBounds(15, 35, 150, 20);
      getContentPane().add(jCheckBoxDelete);

      jCheckBoxUpdate = new JCheckBox();
      jCheckBoxUpdate.setText(getResources().getString("ALLOW_UPDATES"));
      jCheckBoxUpdate.setActionCommand(
         getResources().getString("ALLOW_UPDATES"));
      jCheckBoxUpdate.setBounds(15, 60, 150, 20);
      getContentPane().add(jCheckBoxUpdate);

      jPanelTableBorder = new JPanel();
      jPanelTableBorder.setBorder(
         new TitledBorder(
            new EtchedBorder(),
            getResources().getString("KEY_COLUMNS")));
      jPanelTableBorder.setLayout(null);
      jPanelTableBorder.setBounds(15, 90, 300, 195);
      getContentPane().add(jPanelTableBorder);

      jScrollPane = new JScrollPane();
      jScrollPane.setOpaque(true);
      jScrollPane.setBounds(12, 25, 276, 130);
      jPanelTableBorder.add(jScrollPane);

      jButtonCheckKey = new JButton();
      jButtonCheckKey.setText(getResources().getString("CHECK_KEY"));
      jButtonCheckKey.setBounds(35, 165, 230, 22);
      jPanelTableBorder.add(jButtonCheckKey);

      jPanelStdCmd = new JPanel();
      jPanelStdCmd.setLayout(null);
      jPanelStdCmd.setBounds(320, 0, 90, 125);
      getContentPane().add(jPanelStdCmd);

      jButtonOK = new JButton();
      jButtonOK.setText(getResources().getString("OK"));
      jButtonOK.setBounds(2, 10, 80, 22);
      jPanelStdCmd.add(jButtonOK);

      jButtonCancel = new JButton();
      jButtonCancel.setText(getResources().getString("CANCEL"));
      jButtonCancel.setBounds(2, 38, 80, 22);
      jPanelStdCmd.add(jButtonCancel);

      jButtonHelp = new JButton();
      jButtonHelp.setText(getResources().getString("HELP"));
      jButtonHelp.setBounds(2, 66, 80, 22);
      jPanelStdCmd.add(jButtonHelp);

      // Comment out the code for Integrity checking
      // and Data Synchronization - caching is not being supported for V1

      /*
            jButtonCreateIndex = new JButton();
            jButtonCreateIndex.setText(getResources().getString( "CREATE_INDEX" ));
            jButtonCreateIndex.setBounds(40,162,220,22);
            jPanelTableBorder.add(jButtonCreateIndex);
      
            jPanelIntegrity = new JPanel();
            jPanelIntegrity.setLayout(null);
          jPanelIntegrity.setBorder(new TitledBorder(new EtchedBorder(), "Integrity checking"));
            jPanelIntegrity.setBounds(10,252,390,260);
            getContentPane().add(jPanelIntegrity);
      
            jList1 = new JList();
            jList1.setBounds(36,58,120,86);
            jList1.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.black, Color.gray));
            jPanelIntegrity.add(jList1);
      
            jPanelIntegrityType = new JPanel();
            jPanelIntegrityType.setLayout(null);
            jPanelIntegrityType.setBorder(new TitledBorder(new EtchedBorder(), "Integrity type"));
            jPanelIntegrityType.setBounds(168,50,210,97);
            jPanelIntegrity.add(jPanelIntegrityType);
      
            jRadioButtonTimestamp = new JRadioButton();
            jRadioButtonTimestamp.setText("Timestamp");
            jRadioButtonTimestamp.setBounds(12,18,100,16);
            jPanelIntegrityType.add(jRadioButtonTimestamp);
      
            jComboBoxColName = new JComboBox();
            jComboBoxColName.setBounds(30,50,170,22);
            jPanelIntegrityType.add(jComboBoxColName);
      
            jRadioButtonShadowfields = new JRadioButton();
            jRadioButtonShadowfields.setText("Shadow fields");
            jRadioButtonShadowfields.setBounds(12,75,149,16);
            jPanelIntegrityType.add(jRadioButtonShadowfields);
      
            jLabelColumnName = new JLabel();
            jLabelColumnName.setText("Column name");
            jLabelColumnName.setBounds(30,34,100,16);
            jPanelIntegrityType.add(jLabelColumnName);
      
            jRadioButtonDisable = new JRadioButton();
            jRadioButtonDisable.setText("Disable");
            jRadioButtonDisable.setBounds(20,17,100,18);
            jPanelIntegrity.add(jRadioButtonDisable);
      
            jRadioButtonEnable = new JRadioButton();
            jRadioButtonEnable.setText("Enable");
            jRadioButtonEnable.setBounds(20,35,100,18);
            jPanelIntegrity.add(jRadioButtonEnable);
      
            jPanelIntegrityData = new JPanel();
            jPanelIntegrityData.setLayout(null);
            jPanelIntegrityData.setBorder(new TitledBorder(new EtchedBorder(), "Integrity data storage"));
            jPanelIntegrityData.setBounds(36,186,257,64);
            jPanelIntegrity.add(jPanelIntegrityData);
      
            jRadioButtonSendData = new JRadioButton();
            jRadioButtonSendData.setText("Send data to client");
            jRadioButtonSendData.setBounds(24,18,180,17);
            jPanelIntegrityData.add(jRadioButtonSendData);
      
            jRadioButtonStoreData = new JRadioButton();
            jRadioButtonStoreData.setText("Store data in User's session");
            jRadioButtonStoreData.setBounds(24,38,180,17);
            jPanelIntegrityData.add(jRadioButtonStoreData);
      
            jComboBoxQueryPipes = new JComboBox();
            jComboBoxQueryPipes.setBounds(170,156,205,22);
            jPanelIntegrity.add(jComboBoxQueryPipes);
      
            jLabelRef = new JLabel();
            jLabelRef.setText("Reference query pipe");
            jLabelRef.setBounds(45,150,120,18);
            jPanelIntegrity.add(jLabelRef);
      
            jLabelShadow = new JLabel();
            jLabelShadow.setText("for shadows:");
            jLabelShadow.setBounds(45,164,120,18);
            jLabelShadow.setHorizontalAlignment(SwingConstants.RIGHT);
            jPanelIntegrity.add(jLabelShadow);
      
            jButtonCaching = new JButton();
            jButtonCaching.setText("Caching");
            jButtonCaching.setBounds(320,156,80,22);
            getContentPane().add(jButtonCaching); */

      //}}

   }

   /**
    * Creates the Table used for Key Columns for updating.
    */
   private void createKeyColumnTable()
   {
      String[] header = { /*getResources().getString( "UPDATE" ),*/
         getResources().getString("COLUMN_NAME")};

      jTableKeyColumns = new UTJTable(0, 1);
      DefaultTableModel dtm;
      dtm = (DefaultTableModel) jTableKeyColumns.getModel();
      dtm.setColumnIdentifiers(header);
      jTableKeyColumns.setRowSelectionAllowed(true);
      jTableKeyColumns.setColumnSelectionAllowed(false);
      jTableKeyColumns.getTableHeader().setReorderingAllowed(false);

      //todo: uncomment the following for V2 when Integrity checking is enabled
      /*    TableColumn updateColumn = jTableKeyColumns.getColumn(getResources().getString( "UPDATE" ));
          updateColumn.setPreferredWidth(55);
          updateColumn.setMaxWidth(55);
          updateColumn.setResizable(false);
            updateColumn.setCellEditor(new UTCheckBoxCellEditor());
          updateColumn.setCellRenderer(new UTCheckBoxCellRenderer());*/

      jComboBoxColumnEditor = new JComboBox();
      DefaultCellEditor dce = new DefaultCellEditor(jComboBoxColumnEditor);
      TableColumn columnNameColumn =
         jTableKeyColumns.getColumn(getResources().getString("COLUMN_NAME"));
      dce.setClickCountToStart(2);
      columnNameColumn.setCellEditor(dce);

      jScrollPane.getViewport().add(jTableKeyColumns);

   }

   /**
    * Initializes the listeners associated with the controls for this dialog.
    */
   private void initListeners()
   {

      ButtonListener buttonListener = new ButtonListener();
      TableRowSelectionListener lsl = new TableRowSelectionListener();
      KeyPressedListener kl = new KeyPressedListener();

      jButtonCheckKey.addActionListener(buttonListener);
      jButtonOK.addActionListener(buttonListener);
      jButtonCancel.addActionListener(buttonListener);
      jButtonHelp.addActionListener(buttonListener);

      jTableKeyColumns.getSelectionModel().addListSelectionListener(lsl);
      jTableKeyColumns.addKeyListener(kl);

   }

   /**
    * Initializes the dialog values from the OSDataSynchronizer object and the OSBackendDatatank object
    * that were initialized in onEdit method.
    */
   private void initDialogFields()
   {
      jCheckBoxCreate.setSelected(m_sync.isInsertingAllowed());
      // creating and inserting the same
      jCheckBoxDelete.setSelected(m_sync.isDeletingAllowed());
      jCheckBoxUpdate.setSelected(m_sync.isUpdatingAllowed());

      //get alist of all the columns from the BackendDatatank
      m_allColumns = getAllColumns();
      
      // get column objects from mapper
      m_mapperColumns = getMapperColumns();

      if (m_allColumns != null)
      {
         Iterator iter = m_allColumns.iterator();
         while(iter.hasNext())
         {
            PSBackEndColumn col = (PSBackEndColumn) iter.next();
            String colName = col.getValueDisplayText();
            //          System.out.println("Column from datatank ="+colName);

            jComboBoxColumnEditor.addItem(colName);
         }

         DefaultTableModel dtm;
         dtm = (DefaultTableModel) jTableKeyColumns.getModel();

         PSCollection coll = m_sync.getUpdateColumns();
         if (coll != null && coll.size() > 0)
         {
            for (int i = 0; i < coll.size(); i++)
            {
               PSUpdateColumn updateCol = (PSUpdateColumn) coll.get(i);              
               String display = updateCol.getColumn().getValueDisplayText();
               Collection columns = m_allColumns;
               boolean bColumnExists =
                  columns.contains(updateCol.getColumn());
               if (!bColumnExists)
               {
                  // popup message if column is not found in all columns list
                  // remove from the collection in PSDataSynchronizer object
                  JOptionPane.showMessageDialog(
                     this,
                     display
                        + "  "
                        + getResources().getString("REMOVING_COLUMN"));
                  continue;
               }

               boolean bUpdateable = updateCol.isUpdateable();
               boolean bIsKey = updateCol.isKey();
               if (bIsKey)
               {
                  Object[] data = { /*new Boolean(bUpdateable), */
                     display };
                  dtm.addRow(data);
               }
            } //end for
         } //end if
         int iRowCount = jTableKeyColumns.getRowCount();
         if (iRowCount < 7) // 7 rows can be displayed in the space provided
            addEmptyRowsToTable(7 - iRowCount);

      } //end if
   }

   /**
    * Calls the getColumns method on the OSBackendDatatank object and gets the columns.
    * The names are returned in alias.column format.
    *
    *@returns a vector of column names in alias.column format
    */
   private Collection getAllColumns()
   {
      Collection rval = new ArrayList();
      
      if (m_osBackendDatatank != null)
      {
         Enumeration e = m_osBackendDatatank.getBackendColumns();
         
         while (e.hasMoreElements())
         {
            Object element = (Object) e.nextElement();
            rval.add(element);
         }
      }
      else
      {
         JOptionPane.showMessageDialog(
            this,
            getResources().getString("NO_BACKEND_DATATANK"),
            getResources().getString("WARNING"),
            JOptionPane.WARNING_MESSAGE);
      }
      
      return rval;
   }

   /**
    * @returns a vector containing columns
    * that have been selected in the Mapper.
    *
    */
   private Vector getMapperColumns()
   {
      Vector vCol = new Vector();

      UTPipeNavigator navigator = new UTPipeNavigator();
      UIFigure figMapper = navigator.getMapper(m_figure);
      if (figMapper != null)
      {
         OSDataMapper osDataMapper = (OSDataMapper) figMapper.getData();
         if (osDataMapper != null)
         {
            for (int i = 0; i < osDataMapper.size(); i++)
            {
               PSDataMapping mappingObject =
                  (PSDataMapping) osDataMapper.get(i);
               if (mappingObject.getBackEndMapping()
                  instanceof PSBackEndColumn)
               {
                  PSBackEndColumn col =
                     (PSBackEndColumn) mappingObject.getBackEndMapping();
                  String alias = col.getTable().getAlias();
                  String colName = col.getColumn();
                  // Note that this is a display format
                  vCol.add(col);
               }
            }
         }
      }
      return vCol;

   }

   /**
    * Compared passed in column string against the vector of 
    * column objects.
    * @param col A column string that uses the format defined in 
    * PSBackEndColumn&apos;s getValueDisplayText method.
    * @param vColNames A vector of <code>PSBackEndColumn</code> objects
    * @return true if the given column finds a match
    */
   private boolean isColumnPresent(String colstr, Collection vColNames)
   {
      boolean bPresent = false;
      if (vColNames != null)
      {
         Iterator iter = vColNames.iterator();
         while(iter.hasNext())
         {
            PSBackEndColumn testcol = (PSBackEndColumn) iter.next();
            if (testcol.getValueDisplayText().equals(colstr))
            {
               bPresent = true;
               break;
            }
         }
      }
      return bPresent;
   }   

   /**
    * Inner class implements ActionListener interface.
    */
   class ButtonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         JButton button = (JButton) e.getSource();
         if (button == jButtonOK)
            onOk();
         else if (button == jButtonCancel)
            onCancel();
         else if (button == jButtonHelp)
            onHelp();
         else if (button == jButtonCheckKey)
            onCheckKeyUniqueness();
      }
   }

   /**
    * Inner class to listen for a key pressed event.
    */
   class KeyPressedListener extends KeyAdapter
   {
      public void keyReleased(KeyEvent e)
      {
         if (e.getKeyCode() == e.VK_DELETE)
         {
            onDeleteRows();
         }
      }
   }

   /** Inner class to implement ListSelectionListener for handling the table selection changes.
    */
   class TableRowSelectionListener implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
         rowSelectionChanged();
      }
   }

   /**
    *
    * On row selection change checks if the last row is selected, if it is the last row,
    * adds an additional row.
    */
   private void rowSelectionChanged()
   {
      int i = jTableKeyColumns.getSelectedRow();

      if (i == (jTableKeyColumns.getRowCount() - 1)) // last row selected
         addEmptyRowsToTable(1);
      
      if (i >= 0)
      {
         jTableKeyColumns.setRowSelectionInterval(i, i);
      }
   }

   /**
    *
    * Adds an additional row to table.
    */
   private void addEmptyRowsToTable(int number)
   {
      //add two rows
      Object[] data = { /*new Boolean(false),*/
         "" };
      for (int i = 0; i < number; i++)
          ((DefaultTableModel) jTableKeyColumns.getModel()).addRow(data);
   }

   /**
    *
    * Deletes the selected rows from the table.
    */
   private void onDeleteRows()
   {
      //    System.out.println("in onDeleteRows");

      stopTableCellEditing();

      int iMin = jTableKeyColumns.getSelectionModel().getMinSelectionIndex();
      int iMax = jTableKeyColumns.getSelectionModel().getMaxSelectionIndex();
      //    System.out.println("Min selected row = "+iMin+"   Max sel row = "+iMax);
      if (iMin >= 0 && iMax >= 0)
      {
         int iRowsToRemove = iMax - iMin + 1;
         if (iRowsToRemove < 1)
            return;
         for (int i = 0; i < iRowsToRemove; i++)
         {
            ((DefaultTableModel) jTableKeyColumns.getModel()).removeRow(iMin);
         }
         int iRowCount = jTableKeyColumns.getRowCount();
         if (iRowCount < 7) // 7 rows can be displayed in the space provided
            addEmptyRowsToTable(7 - iRowCount);

      }
      jTableKeyColumns.repaint();

   }

   private void stopTableCellEditing()
   {
      int iCol = jTableKeyColumns.getEditingColumn();
      if (iCol >= 0)
      {
         int iRow = jTableKeyColumns.getEditingRow();
         if (iRow >= 0)
         {
            ((DefaultCellEditor) jTableKeyColumns.getCellEditor(iRow, iCol))
               .stopCellEditing();
         }
      }

   }

   /**
    * Returns a Collection of PSBackEndColumn objects that are listed in the Key Column Table.
    */
   private Collection getKeyColumnsFromTable() throws IllegalArgumentException
   {
      int iRowCount = jTableKeyColumns.getRowCount();
      if (iRowCount <= 0)
         return null;
      Collection vCols = new ArrayList();

      TableColumn columnNameColumn =
         jTableKeyColumns.getColumn(getResources().getString("COLUMN_NAME"));
      int iColNameIndex = columnNameColumn.getModelIndex();

      for (int i = 0; i < iRowCount; i++)
      {
         String formattedName =
            (String) jTableKeyColumns.getValueAt(i, iColNameIndex);
         if (formattedName == null || formattedName.equals(""))
            continue;
         PSBackEndColumn beCol = getBackendColumn(formattedName);
         if (beCol != null)
            vCols.add(beCol);
      }
      return vCols;
   }

   /**
    * Returns a Collection of columns names (formatted) that are the key columns.
    */
   private Collection getKeyColumnNames()
   {
      int iRowCount = jTableKeyColumns.getRowCount();
      if (iRowCount <= 0)
         return null;
      Collection vCols = new ArrayList();

      TableColumn columnNameColumn =
         jTableKeyColumns.getColumn(getResources().getString("COLUMN_NAME"));
      int iColNameIndex = columnNameColumn.getModelIndex();

      for (int i = 0; i < iRowCount; i++)
      {
         String formattedColumnName =
            (String) jTableKeyColumns.getValueAt(i, iColNameIndex);
         if (formattedColumnName == null || formattedColumnName.equals(""))
            continue;
         vCols.add(formattedColumnName);
      }
      return vCols;
   }


   /**
    * Calls @link com.percussion.design.objectstore.PSBackEndColumn#findColumnFromDisplay(String, PSCollection)
    * to create a new PSBackEndColumn with the information in the passed in string. 
    * @param formattedName A string in the format produced by
    * @link #getValueDisplayText()
    * @return <code>null</code> on error or a <code>PSBackEndColumn</code>
    */
   private PSBackEndColumn getBackendColumn(String formattedName)
   {
      if (m_osBackendDatatank == null)
         return null;
         
      return PSBackEndColumn.findColumnFromDisplay(formattedName, m_osBackendDatatank.getTables());
   }

   /**
    * Returns a Vector of <code>PSBackEndColumn</code> objects. These are 
    * columns that are present in the mapper but are not key columns. 
    * (NOTE: that these come from the Mapper)
    * 
    * @return a Vector of <code>PSBackEndColumn</code> objects 
    */
   private Vector getUpdateColumnsInMapperThatAreNotKey()
   {
      Vector vCols = new Vector();

      Collection keyCols = getKeyColumnsFromTable();

      Iterator iter = m_mapperColumns.iterator();
      while(iter.hasNext())
      {
         PSBackEndColumn col = (PSBackEndColumn) iter.next();
         PSBackEndColumn col1 = col;
         Collection columns = m_allColumns;
         //skip if not in all columns list m_vAllColumns
         if (!columns.contains(col1))
            continue;

         // add to update column list only if it is not Key Column
         if (!keyCols.contains(col))
         {
            vCols.add(col);
         }
      }
      return vCols;
   }

   /**
    * Performs a check using the PSCataloger object and request type of UniqueKey to see if the
    * selected keys are in the predefined unique indices. Brings up a message dialog showing the
    * result of the check. Checks all tables individually.
    */
   private void onCheckKeyUniqueness()
   {

      Collection selectedKeyColumns = getKeyColumnsFromTable();

      if (selectedKeyColumns == null)
         throw new IllegalArgumentException("getKeyColumnNames returned null vector");

      if (selectedKeyColumns.isEmpty())
      {
         JOptionPane.showMessageDialog(
            this,
            getResources().getString("SELECT_KEY_COLUMNS"));
         return;
      }
      
      Set aliases = new HashSet();
      Iterator iter = selectedKeyColumns.iterator();
      while(iter.hasNext())
      {
         PSBackEndColumn col = (PSBackEndColumn) iter.next();
         // Use a set to create a unique collection of columns
         aliases.add(col.getTable().getAlias());
      }

      iter = aliases.iterator();
      while(iter.hasNext())
      {
         checkKeyUniquenessForTable((String) iter.next());
      }
   }

   /**
    * Performs a check using the PSCataloger object and request type of 
    * UniqueKey to see if the selected keys are in the predefined unique 
    * indexes. Brings up a message dialog showing the result of the check.
    *
    * @param alias - the alias of the table for which to check for key uniqueness.
    */
   private void checkKeyUniquenessForTable(String alias)
   {
      boolean bUnique = false;
      Vector vSelectedKeyColumns = new Vector();

      Collection cols = getKeyColumnsFromTable();
      Iterator iter = cols.iterator();
      while(iter.hasNext())
      {
         PSBackEndColumn beColumn = (PSBackEndColumn) iter.next();
         String strAlias = beColumn.getTable().getAlias();
         if (strAlias.equals(alias))
         {
            String strColumn = beColumn.getColumn();
            vSelectedKeyColumns.add(strColumn);
         }
      }

      OSBackendTable beTable =
         (OSBackendTable) m_osBackendDatatank.getBackEndTable(alias);
      Vector vUniqueKeys = CatalogIndices.getCatalog(beTable, false);

      if (null == vUniqueKeys
         || (null != vUniqueKeys && vUniqueKeys.isEmpty()))
      {
         JOptionPane.showMessageDialog(
            this,
            getResources().getString("NO_PREDEFINED_INDICES"));
         return;
      }

      // now need to walk the vector of unique key indexes and check if there is a match
      for (int i = 0; i < vUniqueKeys.size(); i++)
      {
         Vector vUKeys = (Vector) vUniqueKeys.elementAt(i);
         if (vUKeys == null)
            continue;
         if (vUKeys.size() != vSelectedKeyColumns.size())
            continue;
         else
         {
            int count = 0;
            for (int j = 0; j < vUKeys.size(); j++)
            {
               if (((String) vUKeys.elementAt(j))
                  .equals((String) vSelectedKeyColumns.elementAt(j)))
                  count++;
               else
                  break;
            }
            if (count == vUKeys.size())
            {
               bUnique = true;
               break;
            }
         }
      } //end for loop

      String endOfMsg =
         "\n" + getResources().getString("FOR_TABLE_ALIAS") + "  " + alias;

      if (bUnique)
         JOptionPane.showMessageDialog(
            this,
            getResources().getString("SUCCESS") + endOfMsg);
      else
         JOptionPane.showMessageDialog(
            this,
            getResources().getString("FAILURE") + endOfMsg);
   }

   /**
    * Main method for testing.
    */
   /*
      static public void main(String args[])
      {
         final JFrame frame = new JFrame("Test DataSynchronizerProperty Dialog");
         frame.addWindowListener(new BasicWindowMonitor());
         try
         {
            String strLnFClass = UIManager.getSystemLookAndFeelClassName();
            LookAndFeel lnf = (LookAndFeel) Class.forName(strLnFClass).newInstance();
            UIManager.setLookAndFeel( lnf );
   
            JButton startButton = new JButton("Open Dialog");
            frame.getContentPane().add(startButton);
            startButton.addActionListener(new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  DataSynchronizerPropertyDialog dialog = new DataSynchronizerPropertyDialog(frame, new OSDataSynchronizer());
                  dialog.setLocationRelativeTo(frame);
                  dialog.setVisible(true);
               }
            });
   
            frame.setSize(640, 480);
            frame.setVisible(true);
   
          }
          catch (Exception e)
          { System.out.println(e); }
   
      }
   */

   //storage

   /**
    * a reference to the UIFigure object passed in onEdit.
    */
   private UIFigure m_figure = null;

   /**
    * gets filled by the getData() method of the figure passed in onEdit method.
    */
   OSDataSynchronizer m_sync = null;

   /**
    * gets set to true if the user clicke on the OK button when the dialog data is saved.
    * in the Figure's data object.
    */
   boolean m_bModified = false;

   /**
    * The OSBackendDatatank that the synchronizer is connected to.
    */
   OSBackendDatatank m_osBackendDatatank = null;

   /**
    * For cataloging to determine if the selected key columns are unique.
    */
   PSCataloger m_psCataloger = null;

   /**
    * The Collection of all columns from the OSBackendDatatank object.
    */
   Collection m_allColumns = null;

   /**
    * The Collection of backend columns in the Mapper.
    */
   Collection m_mapperColumns = null;

   /**
    * Swing controls used in the user interface
    */
   JComboBox   jComboBoxColumnEditor;
   JCheckBox   jCheckBoxCreate;
   JCheckBox   jCheckBoxDelete;
   JCheckBox   jCheckBoxUpdate;
   JScrollPane jScrollPane;
   UTJTable    jTableKeyColumns;
   JButton     jButtonCheckKey;
   JButton     jButtonCreateIndex;
   JPanel      jPanelTableBorder;
   JPanel      jPanelStdCmd;
   JButton     jButtonOK;
   JButton     jButtonCancel;
   JButton     jButtonHelp;
}

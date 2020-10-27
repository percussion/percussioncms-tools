/*[ MultiTableDatatankPropertyDialog.java ]************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer;

import com.percussion.EditableListBox.EditableListBox;
import com.percussion.EditableListBox.EditableListBoxEditor;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/** 
 * Editor for BackendDatank object when the tank is connected to an update pipe.
 * The editor has an editable list box that lists all tables in the tank and a 
 * Table info panel that shows the table info such as table name, driver, server,
 * database, owner, and alias.
 * 
 */
public class  MultiTableDatatankPropertyDialog extends PSEditorDialog
{

/** 
 *
 * Default constructor with no parameters
 */
   public MultiTableDatatankPropertyDialog()
   {
     super();
     initDialog();
   }

/** Default initalizer for the constructors.
*/
  private void initDialog()
  {
    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
      leftPanel.setBorder(new EmptyBorder(10,10,10,10));
    leftPanel.add(createTableListPanel());
      leftPanel.add(Box.createVerticalStrut(10));
    leftPanel.add(createTableInfoPanel());

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(leftPanel, BorderLayout.CENTER);
    mainPanel.add(createCommandPanel(), BorderLayout.EAST);

      for (int i = 0; i < CONSTRAINTS; i++)
      {
         m_constraintArray[i] = new StringConstraint();
      }

      m_componentArray[0] = m_tfAlias;
      m_componentArray[1] = m_cbDatasource;
      m_componentArray[2] = m_tfTable;

      setValidationFramework(m_componentArray, m_constraintArray);

    getContentPane().add(mainPanel);
    setSize(450, 440);
      setResizable(true);
    center();
  }

   /**
    * Creates the &quot;Table Info&quot; panel.
    */
   private JPanel createTableInfoPanel()
   {
      JPanel innerPanel = new JPanel();
      innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
      innerPanel.setBorder(new EmptyBorder(0, 0, 6, 6));
      innerPanel.add(createFieldPanel(m_tfAlias, "alias"));
      innerPanel.add(createFieldPanel(m_cbDatasource, "datasource"));
      innerPanel.add(createFieldPanel(m_tfTable, "table"));

      JPanel tableInfoPanel = new JPanel();
      tableInfoPanel.setBorder(new TitledBorder(new EtchedBorder(
         EtchedBorder.LOWERED), getResources().getString("tableInfo")));
      tableInfoPanel.add(innerPanel);

      return tableInfoPanel;
   }

   /**
    * Creates a panel with a label and a JTextField.
    * 
    * @param field the field to create the panel for
    * @param labelResourceID the resource ID of the label associated with the
    * text field
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createFieldPanel(JComponent field,
      String labelResourceID)
   {
      if (field instanceof UTFixedTextField)
      {
         ((UTFixedTextField)field).setText("");
         ((UTFixedTextField)field).setEditable(false);
      }
      field.setEnabled(false);
      field.setBackground(Color.lightGray);
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      panel.add(new JLabel(getResources().getString(labelResourceID)));
      panel.add(field);
      return panel;
   }

      
/**
 * Creates the table panel with the Editable list box that shows the list of
 * tables.
 * 
 * @return The panel, never <code>null</code>. 
 */
   private JPanel createTableListPanel()
   {
      JPanel panelTables = new JPanel();
      panelTables.setLayout(new BorderLayout());
      panelTables.setPreferredSize(new Dimension(300, 250));

      m_listTables = new EditableListBox(getResources().getString("tables"),
         m_beTankDialog, null, null, EditableListBox.TEXTFIELD,
         EditableListBox.BROWSEBUTTON);
      m_listTables.getSelectionModel().setSelectionMode(
         ListSelectionModel.SINGLE_SELECTION);
      ((EditableListBoxEditor) m_listTables.getCellEditor())
         .setClickCountToStart(Integer.MAX_VALUE); // disabling editing
      m_listTables.getRightButton().addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_listTables.deleteRows();
         }
      });

      m_buttonInsert = m_listTables.getLeftButton();
      m_buttonDelete = m_listTables.getRightButton();
      m_buttonInsert.addActionListener(new ButtonListener());
      m_buttonDelete.addActionListener(new ButtonListener());
      m_listTables.getSelectionModel().addListSelectionListener(
         new ListItemListener());
      panelTables.add(m_listTables);

      return panelTables;
   }

   /**
    * Checks for if the new alias is unique.
    * 
    * @param newAlias - the newAlias to check for uniqueness
    * @param vTables - the vector of Table objects to check the alias against
    * 
    * @return <code>true</code> if it is unique, <code>false</code> if not.
    */
   private boolean isAliasUnique(String newAlias, Vector vTables)
   {
      boolean bFound = false;
      if (vTables != null)
      {
         for (int i = 0; i < vTables.size(); i++)
         {
            if (newAlias.equals(((Table) vTables.get(i)).getAlias()))
            {
               bFound = true;
               break;
            }
         }
      }

      if (bFound)
      {
         JOptionPane.showMessageDialog(this, getResources().getString(
            "aliasMustBeUnique"), getResources().getString("uniqueTitle"),
            JOptionPane.ERROR_MESSAGE);
         return false; // it is not unique
      }
      return true; // it is unique
   }

   /**
    * Creates the command panel with OK, Cancel and Help buttons.
    * 
    */
   private JPanel createCommandPanel()
   {
      m_commandPanel = new UTStandardCommandPanel(this, "",
         SwingConstants.VERTICAL)
      {
         public void onOk()
         {
            m_curIndex = m_listTables.getSelectionModel()
               .getMinSelectionIndex();

            if (m_curIndex >= 0)
            {
               if (!activateValidation())
                  return; // error, stop the save!
               saveCurrentSelection();
            }

            try
            {
               PSCollection collTables = new PSCollection(
                  "com.percussion.design.objectstore.PSBackEndTable");
               Vector v = getTables();
               for (int i = 0; i < v.size(); i++)
               {
                  OSBackendTable table = (OSBackendTable) v.get(i);
                  collTables.add(table);
               }
               m_beDatatank.setTables(collTables);
            }
            catch (Exception e)
            {
               // e.printStackTrace();
               PSDlgUtil.showErrorDialog(
                  getResources().getString("saveBETableException") + ": "
                     + e.toString(),
                  getResources().getString("saveBETableExceptionTitle"));
               return; // error, stop the save
            }

            m_bModified = true;
            dispose();
         }
      };
      m_commandPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());
      return m_commandPanel;
   }

   /**
    * Implementation of IEditor interface.
    * 
    * @see IEditor
    */
   public boolean onEdit(UIFigure figure, final Object data)
   {
      if (figure == null)
      {
         System.out.println("...figure not valid");
         return false;
      }

      if (figure.getData() instanceof OSBackendDatatank)
      {
         m_beDatatank = (OSBackendDatatank) figure.getData();
         initTableList(m_beDatatank);
         this.center();
         this.setVisible(true);
      }
      else
         throw new IllegalArgumentException(getResources().getString(
            "dataTypeError"));

      return m_bModified;
   }

   /** 
    * Inner class implements ListSelectionListener interface.
    */
   class ListItemListener implements ListSelectionListener
   {
    public void valueChanged( ListSelectionEvent e )
    {        
         onListSelectionChanged();
    }
   }

  /**
    * Inner class to implement ActionListener interface for handling button events.
    */
   class ButtonListener implements ActionListener
   {
      public void actionPerformed( ActionEvent e )
      {
         JButton button = (JButton)e.getSource();
         if(button == m_buttonInsert)
            onInsertButtonClicked();
         else if (button == m_buttonDelete)
            onDeleteButtonClicked();

      }
   }



   /**
    *   Handler for member list selection change.
    *
    */
   private void onListSelectionChanged()
   {
//      System.out.println("List selection changed");
      if(m_listTables.getItemCount() <=0 )
         return;

      int prevIndex = m_curIndex;
      m_curIndex =  m_listTables.getSelectionModel().getMinSelectionIndex();


      if(m_bDeleting)
      {
         if(m_curIndex >= 0 && m_curIndex < m_listTables.getItemCount())
         {
            OSBackendTable table = (OSBackendTable)m_listTables.getRowValue(m_curIndex);
            updateDialog(table);
         }
         m_bDeleting = false;
         return;
      }
      if(prevIndex == m_curIndex)
         return;

      savePrevSelectionData(prevIndex);
      m_curIndex =  m_listTables.getSelectionModel().getMinSelectionIndex();

      if(m_curIndex < 0 || m_curIndex >= m_listTables.getItemCount())
      {
         clearTextFields();
         return;
      }

      Object o = m_listTables.getRowValue(m_curIndex);
      if(o instanceof OSBackendTable)
      {   
         OSBackendTable table = (OSBackendTable)o;
         if(table != null)
            updateDialog(table);
      }

   }
      /**
    *   Handler for insert button clicked.
    *   
    */
   private void onInsertButtonClicked()
   {
      int prevIndex = m_curIndex;
      m_listTables.getCellEditor().cancelCellEditing();

      savePrevSelectionData(prevIndex);
      try
      {
         Table tableData = new Table();
      tableData.setReadOnly(false);
         m_beTankDialog = new BackendTankPropertyDialog(this);
         m_beTankDialog.setTables(getTables());
         UIFigure fake = new UIFigure("Fake", tableData,   "fake", 0);

         if(m_beTankDialog.onEdit(fake,tableData))
         {
            OSBackendTable table = m_beTankDialog.getData();
            m_listTables.addRowValue(tableData);
         }
         else //user clicked cancel
         {
            m_curIndex = m_listTables.getSelectionModel().getMinSelectionIndex();
         }
         repaint();

         m_beTankDialog.dispose();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }

   }


   /**
    *   Handler for delete button clicked.
    *   
    */
   private void onDeleteButtonClicked()
   {
      
//      System.out.println("delete button clicked");
      m_curIndex = m_listTables.getSelectionModel().getMinSelectionIndex();
      if(m_curIndex < 0 || m_curIndex >= m_listTables.getItemCount())
         return;
      m_bDeleting = true;
      clearTextFields();

   }

   /**
    *   returns a vector of Table objects in the Editable list box.
    *   
    */
   private Vector getTables()
   {
      Vector v = new Vector();
      int count = m_listTables.getItemCount();
      for(int i=0; i<count; i++)
      {
         v.add(m_listTables.getRowValue(i));
      }
      return v;
   }

   /**
    *   returns a vector of Table objects in the Editable list box except for the 
    * the one at the specified index.
    *   
    */
   private Vector getTablesExceptAt(int index)
   {
      Vector v = new Vector();
      int count = m_listTables.getItemCount();
      for(int i=0; i<count; i++)
      {
         if(i == index)
            continue;      // skip the table at index
         v.add(m_listTables.getRowValue(i));
      }
      return v;

   }


   /**
    *   Initializes the table list with the collection of tables from
    * the backend datatank.
    *   
    */
   private void initTableList(OSBackendDatatank tank)
   {
//      System.out.println(" Tank object ="+tank);
      PSCollection collTables = tank.getTables();
      if(collTables != null)
      {
         for(int i=0; i<collTables.size(); i++)
         {
            OSBackendTable osTable = (OSBackendTable) collTables.get(i);
            Table table = null;
            try
            {
               table = new Table(osTable);
            }
            catch(PSIllegalArgumentException e)
            {
               e.printStackTrace();
            }
            
            m_listTables.addRowValue(table);
         }   
      }
      if(m_listTables.getItemCount() > 0)
      {
         m_listTables.getSelectionModel().setSelectionInterval(0,0);
         m_curIndex = 0;
         Object o = m_listTables.getRowValue(0);
         updateDialog((OSBackendTable)o);
      }

   }

   /**
    *   Saves data from the text fields into the the currently selected table in the 
    * Editable list box.
    *   
    */
   private void saveCurrentSelection()
   {
      m_curIndex = m_listTables.getSelectionModel().getMinSelectionIndex();
      savePrevSelectionData(m_curIndex);
   }

   /**
    *   Saves data from the text fields into the the table in the 
    * Editable list box at the specified index. Will not save if
    * the index is less than 0 or greater than or equal to the row
    * count in the editable list box.
    *   
    */
   private void savePrevSelectionData(int index)
   {
//      System.out.println("saving data for index "+index);
//      System.out.println("m_bDeleting ="+m_bDeleting);

      if(index < 0 || index >= m_listTables.getItemCount())
         return;
      String defaultText = E2Designer.getResources().getString( "Dummy" );

      Object o = null;
      Table data = null;
      String strAlias = m_tfAlias.getText();
      if(strAlias.equals("")  || strAlias.equals(defaultText) )
      {
         JOptionPane.showMessageDialog(this, getResources().getString("enterAnAlias"));
         m_tfAlias.requestFocus();
//         m_listTables.getSelectionModel().setSelectionInterval(index,index);
         return;
      }

      Vector v = getTablesExceptAt(index);
      if(!isAliasUnique(strAlias, v))
      {
//         m_listTables.getSelectionModel().setSelectionInterval(index,index);
         return;
      }
      o = m_listTables.getRowValue(index);
      if(o instanceof Table)
      {
         data = (Table)o;
         if(activateValidation())
         {
            saveTextFieldData(data);
         }
         else
         {
//            m_listTables.getSelectionModel().setSelectionInterval(index,index);
            return;
         }
      }
   }

   /**
    * Saves data from the text fields into the the currently passed in table
    * object.
    * 
    * @param table - the table in which the data is to be saved into.
    */
   private void saveTextFieldData(OSBackendTable table)
   {
      if (table == null)
         return;
      if (table.isReadOnly())
      {
         try
         {
            table.setAlias(m_tfAlias.getText());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      else
      {
         try
         {

            table.setAlias(m_tfAlias.getText());
            table.setDataSource(m_cbDatasource.getSelectedDatasource());
            table.setTable(m_tfTable.getText());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

      }
   }

   /**
    *   Updates the text fields in the dialog with the data from the passed in table.
    *   
    */
   private void updateDialog(OSBackendTable table)
   {
    m_tfAlias.setEnabled(true);
    m_tfAlias.setEditable(true);
    m_tfAlias.setBackground(Color.white);

      if (table.isReadOnly())
      {
         m_cbDatasource.setEnabled(false);
         m_cbDatasource.setEditable(false);
         m_cbDatasource.setBackground(Color.lightGray);

         m_tfTable.setEnabled(false);
         m_tfTable.setEditable(false);
         m_tfTable.setBackground(Color.lightGray);
      }
      else
      {
         m_cbDatasource.setEnabled(true);
         m_cbDatasource.setEditable(true);
         m_cbDatasource.setBackground(Color.white);

         m_tfTable.setEnabled(true);
         m_tfTable.setEditable(true);
         m_tfTable.setBackground(Color.white);
      }

      clearTextFields();

      if (table.getAlias() != null)
         m_tfAlias.setText(table.getAlias());

      m_cbDatasource.setSelectedDatasource(table.getDataSource());

      if (table.getTable() != null)
         m_tfTable.setText(table.getTable());
   }

   /**
    *   Clears the text fields of the dialog.
    *   
    */
   private void clearTextFields()
   {
      m_tfAlias.setText("");
      m_tfTable.setText("");
   }


   
   /**
    * Inner class that extends from the OSBackendTable object. Has a toString
    * method to display the alias in the Editable list box. The Table objects
    * are stored in the Editable list box.
    * 
    */
   class Table extends OSBackendTable
   {
      public Table() throws PSIllegalArgumentException
      {
         super();
      }

      public Table(OSBackendTable table) throws PSIllegalArgumentException
      {
         super(table);
      }

      public String toString()
      {
         return getAlias();
      }
   }
   
//
// MEMBER VARIABLES
//

  // items for TableInfoPanel
  private UTFixedTextField m_tfAlias = new UTFixedTextField("", FIELD_SIZE);
  private DatasourceComboBox m_cbDatasource = new DatasourceComboBox(
     FIELD_SIZE);
  private UTFixedTextField m_tfTable = new UTFixedTextField("", FIELD_SIZE);
  
  
  /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel;

   private BackendTankPropertyDialog m_beTankDialog = null;

   // editable list box related
   private EditableListBox m_listTables = null;
   private JButton m_buttonInsert;
   private JButton m_buttonDelete;
   private int m_curIndex = -1;
   private boolean m_bDeleting = false;

   // set to true onOK
   private boolean m_bModified = false;


   // Validation members
   static private int CONSTRAINTS = 3;

   private ValidationConstraint[] m_constraintArray = new ValidationConstraint[CONSTRAINTS];
   private Component[] m_componentArray = new Component[CONSTRAINTS];

   // Server objects
   private OSBackendDatatank m_beDatatank;

   //Constants
   private static final Dimension FIELD_SIZE = new Dimension(200, 22);
}

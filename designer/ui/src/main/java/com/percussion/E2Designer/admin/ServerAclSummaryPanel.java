/******************************************************************************
 *
 * [ ServerAclSummaryPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer.admin;


import com.percussion.E2Designer.PSComboBox;
import com.percussion.E2Designer.UTCheckBoxCellRenderer;
import com.percussion.E2Designer.UTReadOnlyTableCellEditor;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

/** Provides a summary of server access control lists. The access control list
* consists of a PSAcl object that has a collection of PSAclEntry objects.
*/

public class ServerAclSummaryPanel extends JPanel implements ITabDataHelper
{
//
// CONSTRUCTORS
//
/**
 *   @version   1.1 1999/5/18
 *
 *  Constructor for the panel that takes the server configuration.
 *   @param   config The server configuration from the objectstore.
 *
 */
  public ServerAclSummaryPanel(Frame parent, ServerConfiguration config)
  {
    m_config = config;
    m_parent = parent;
    this.setBorder(new EmptyBorder(5, 5, 5, 5));
    init();
  }

//
// PUBLIC METHODS
//

/** Uses the vector data at index to return a row of 6 columns (name, security provider,
   * design access, data access, create access, and delete access.
  *
  * @param entry The PSAclEntry object in the collection of entries in the PSAcl
   * member m_acl. The data is to be converted to a row for the table to display.
  *
  * @returns Object[] The row of data as an array of Objects[6].
*/
  public Object[] convertDataToRow(PSAclEntry entry)
  {
    Object[] rowData = new Object[6];

    rowData[0] = entry.getName();

      boolean bDesignAccess = false;
      boolean bAdminAccess = false;
      boolean bDataAccess = false;
      boolean bCreateAccess = false;
      boolean bDeleteAccess = false;

      int iAccess = entry.getAccessLevel();
      if((iAccess & PSAclEntry.SACE_ACCESS_DESIGN) == PSAclEntry.SACE_ACCESS_DESIGN)
         bDesignAccess = true;
      if((iAccess & PSAclEntry.SACE_ACCESS_DATA) == PSAclEntry.SACE_ACCESS_DATA)
         bDataAccess = true;
      if((iAccess & PSAclEntry.SACE_CREATE_APPLICATIONS) == PSAclEntry.SACE_CREATE_APPLICATIONS)
         bCreateAccess = true;
      if((iAccess & PSAclEntry.SACE_DELETE_APPLICATIONS) == PSAclEntry.SACE_DELETE_APPLICATIONS)
         bDeleteAccess = true;
      if((iAccess & PSAclEntry.SACE_ADMINISTER_SERVER) == PSAclEntry.SACE_ADMINISTER_SERVER)
         bAdminAccess = true;

      rowData[1] = bDesignAccess;
      rowData[2] = bAdminAccess;
      rowData[3] = bDataAccess;
      rowData[4] = bCreateAccess;
      rowData[5] = bDeleteAccess;

    return rowData;
  }

/*
  public static void main(String[] args)
  {
    JFrame frame = new JFrame();
    ServerAclSummaryPanel panel = null;

    try
    {
      String strLnFClass = UIManager.getSystemLookAndFeelClassName();
      LookAndFeel lnf = (LookAndFeel) Class.forName(strLnFClass).newInstance();
      UIManager.setLookAndFeel( lnf );

      panel = new ServerAclSummaryPanel();
    }
    catch (Exception e)
    { e.printStackTrace(); }

    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });
    frame.getContentPane().add(panel);
    frame.setSize(300, 300);
    frame.setVisible(true);
  }
*/

//
// PRIVATE METHODS
//

/** Initial population of the table display of SecurityProvider data stored
  * in the Vector.  This is called once by the init() method.  This method also
  * uses populateRow(int) often to dissect the Vector data into table row
  * format.
  *
  * @returns Object[][] The 2-dimen array used for the construction of the
  * display table.
  *
  * @see #convertDataToRow(PSAclEntry)
*/
  private Object[][] initializeTableData()
  {
    m_acl = m_config.getServerAcl();
      m_entries = m_acl.getEntries();

    Object[][] storage = new Object[m_entries.size()][6];
    Object[] rowStore = new Object[6];

    for (int i = 0; i < m_entries.size(); i++)
    {
      rowStore = convertDataToRow((PSAclEntry)m_entries.get(i));

      storage[i][0] = rowStore[0];
      storage[i][1] = rowStore[1];
      storage[i][2] = rowStore[2];
      storage[i][3] = rowStore[3];
      storage[i][4] = rowStore[4];
      storage[i][5] = rowStore[5];
    }

    return storage;
  }

/** Constructors defaults to this initialization method for all this
  * panel&apos;s doing.
*/
  private void init()
  {
    String[] tableHeaders = {sm_res.getString("entry"),
                             sm_res.getString("designAccess"),
                             sm_res.getString("adminAccess"),
                             sm_res.getString("dataAccess"),
                             sm_res.getString("createAccess"),
                             sm_res.getString("deleteAccess")};

    m_dataModel = new DefaultTableModel(initializeTableData(), tableHeaders);

    m_table = new JTable(m_dataModel);
    m_table.setIntercellSpacing(new Dimension(0,0));
    m_table.setRowSelectionAllowed(true);
    m_table.setColumnSelectionAllowed(false);
    m_table.setShowVerticalLines(false);
    m_table.setShowGrid(false);
    m_table.setRequestFocusEnabled(false);
      m_table.getTableHeader().setReorderingAllowed(false);

      UTCheckBoxCellRenderer renderer = new UTCheckBoxCellRenderer();
      UTReadOnlyTableCellEditor editor = new UTReadOnlyTableCellEditor();

      for(int i=1; i<6; i++)
      {
         m_table.getColumnModel().getColumn(i).setCellRenderer(renderer);
      }
      for(int i=0; i<6; i++)
         m_table.getColumnModel().getColumn(i).setCellEditor(editor);

      // adding table row-editing capability
      m_table.addMouseListener(new MouseAdapter()
      {
         // need this for double clicking on row to edit
         public void mouseClicked(MouseEvent e)
         {
            if (0 > e.getY() || m_table.getPreferredSize().height < e.getY())
               return;  // ignore

            if (2 == e.getClickCount())
            {
               int iRow = e.getY() / m_table.getRowHeight();

               m_table.getSelectionModel().setSelectionInterval( iRow, iRow );
               onEdit();
            }
         }
      });

    // adding ListSelectionListener
    m_table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        if (e.getValueIsAdjusting())
          return;

        int min = 0;
        int max = m_table.getRowCount();
        int selectedCount = 0;

        for (int i = min; i < max; i++)
        {
          // if an index is selected...
          if (((DefaultListSelectionModel)e.getSource()).isSelectedIndex(i))
          {
            selectedCount++;
          }
        }

        // I do not want to allow users to edit ACL entries when there are
        // more than one row selected.
        if (1 == selectedCount)
          m_editButton.setEnabled(true);
        else
          m_editButton.setEnabled(false);
      }
    });


      JScrollPane scrollPane = new JScrollPane(m_table);
      if(m_table.getParent() != null)
         m_table.getParent().setBackground(Color.white);

      JPanel basePane = new JPanel(new BorderLayout());
      basePane.add(scrollPane, BorderLayout.CENTER);
      basePane.setBackground(Color.white);

      setLayout(new BorderLayout());
      add(basePane, BorderLayout.CENTER);
      add(createBottomPanel(), BorderLayout.SOUTH);


      m_allowDetailedMsg.getModel().addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            m_config.setAllowDetailedAuthenticationMessages(
               ((ButtonModel) e.getSource()).isSelected());
         }
      });
      m_allowDetailedMsg.setDisplayedMnemonicIndex(0);
      m_allowDetailedMsg.setMnemonic('A');
      m_allowDetailedMsg.getModel().setSelected(
         m_config.allowDetailedAuthenticationMessages());

      //set the selection for the multiple group combo box
      if(m_acl.isAccessForMultiMembershipMaximum())
         m_comboMultiGroup.setSelectedItem(sm_res.getString("max") );
      else if(m_acl.isAccessForMultiMembershipMergedMaximum())
         m_comboMultiGroup.setSelectedItem(sm_res.getString("mergemax") );
      else if(m_acl.isAccessForMultiMembershipMergedMinimum())
         m_comboMultiGroup.setSelectedItem(sm_res.getString("mergemin") );
      else if(m_acl.isAccessForMultiMembershipMinimum())
         m_comboMultiGroup.setSelectedItem(sm_res.getString("min") );
   }


   /**
    * Creates a panel that has the multi-memebership label and control and
    * the control buttons.
    *
    * @return The panel that contains the label, combobox and 3 buttons.
    */
   private JPanel createBottomPanel()
   {
      JPanel main = new JPanel();
      main.setLayout(new BoxLayout( main, BoxLayout.Y_AXIS ));

      Box multiGroup = Box.createHorizontalBox();
      m_comboMultiGroup = createComboBox();
      JLabel multiLabel = new JLabel( "If multiple group matches found:"/*sm_res.getString( "multiGroupLabel" )*/);
      multiLabel.setDisplayedMnemonic('p');
      multiLabel.setLabelFor(m_comboMultiGroup);
      
      multiGroup.add(multiLabel);
      multiGroup.add( Box.createHorizontalStrut(5));
      
      
      m_comboMultiGroup.addItemListener(new ItemListener()
      {
         public void itemStateChanged( ItemEvent e )
         {
            {
               if (e.getStateChange()== ItemEvent.SELECTED)
                  onMultiGroupItemChanged();
            }
         }
      });
      multiGroup.add( m_comboMultiGroup );
      main.add( Box.createVerticalStrut(4));
      main.add( multiGroup );
      main.add( Box.createVerticalStrut(4));

      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
//      bottomPanel.setBorder(new EmptyBorder(4,0,0,0));
      bottomPanel.add( m_allowDetailedMsg );
      bottomPanel.add(Box.createHorizontalStrut(5));
      bottomPanel.add( Box.createHorizontalGlue());
      JPanel buttonPanel = new JPanel(new BorderLayout());
      buttonPanel.add(createButtonPanel(), BorderLayout.EAST);
      bottomPanel.add(buttonPanel);

      main.add( bottomPanel );
      return main;
  }


/** Creates the button panel with three buttons, Edit, Delete and New.
  *
  * @returns JPanel The panel that contains the 3 buttons.
*/
   private JPanel createButtonPanel()
   {
    m_deleteButton = new JButton(sm_res.getString("delete"));
    m_deleteButton.setMnemonic(sm_res.getString("delete").charAt(0));
    m_deleteButton.setPreferredSize(new Dimension(70, 22));
    m_deleteButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
//        System.out.println("delete!");
            onDelete();
      }
    });

    m_editButton = new JButton(sm_res.getString("edit"));
    m_editButton.setMnemonic(sm_res.getString("edit").charAt(0));
    m_editButton.setPreferredSize(new Dimension(70, 22));
    m_editButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
//        System.out.println("edit!");
            onEdit();
      }
    });

    m_newButton = new JButton(sm_res.getString("new"));
    m_newButton.setMnemonic(sm_res.getString("new").charAt(0));
    m_newButton.setPreferredSize(new Dimension(70, 22));
    m_newButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
//        System.out.println("new!");
            onNew();
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.add(m_editButton);
    buttonPanel.add(m_deleteButton);
    buttonPanel.add(m_newButton);

    return buttonPanel;
   }

/** Creates the combo box for specifying access in case of multiple group matches.
 * The possible access are also added to the combo box.
*/
   private PSComboBox createComboBox()
   {
      PSComboBox comboBox = new PSComboBox();
      comboBox.addItem(sm_res.getString("mergemax"));
      comboBox.addItem(sm_res.getString("mergemin"));
      comboBox.addItem(sm_res.getString("max"));
      comboBox.addItem(sm_res.getString("min"));

      return comboBox;
   }

/** Creates the label panel in the bottom panel.
  *
  * @returns JPanel The panel that contains the label.
*/
/*   private JPanel createLabelPanel()
   {
      m_labelMultiGroup = new JLabel(sm_res.getString("multipleGroup"), SwingConstants.RIGHT);
      m_labelMatchesFound = new JLabel(sm_res.getString("matchesFound"), SwingConstants.RIGHT);

      m_labelMultiGroup.setPreferredSize(new Dimension(140, 15));
      m_labelMatchesFound.setPreferredSize(new Dimension(140, 15));

    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
      labelPanel.add(m_labelMultiGroup);
      labelPanel.add(m_labelMatchesFound);

      return labelPanel;
   }
*/
/**
   * Handler for item selection change in the combo box.
   */
   private void onMultiGroupItemChanged()
   {
      String strSelection = ((String)m_comboMultiGroup.getSelectedItem());

      if(strSelection.equals(sm_res.getString("max")))
         m_acl.setAccessForMultiMembershipMaximum();
      else if(strSelection.equals(sm_res.getString("mergemax")))
         m_acl.setAccessForMultiMembershipMergedMaximum();
      else if(strSelection.equals(sm_res.getString("mergemin")))
         m_acl.setAccessForMultiMembershipMergedMinimum();
      else if(strSelection.equals(sm_res.getString("min")))
         m_acl.setAccessForMultiMembershipMinimum();

   }



/**
   * Handler for Edit button clicked. Brings up the AclEntryDetailsDialog to edit the
   * properties. Also updates the entry in the collection PSCollection of
   * PSAaclEntry objects (m_entries).
   */
   private void onEdit()
   {
      int iMin = m_table.getSelectionModel().getMinSelectionIndex();
      int iMax = m_table.getSelectionModel().getMaxSelectionIndex();
      if(iMin < 0 || iMax < 0 || iMin != iMax)
         return;

      String strEntry = (String)m_table.getValueAt(iMin, 0);
      int indexInCollection = 0;
      for(int i=0; i<m_entries.size(); i++)
      {
         PSAclEntry entry = (PSAclEntry)m_entries.get(i);
         String strEntryName = entry.getName();
         if(strEntry.equals(strEntryName))
         {
            m_curEntry = entry;
            indexInCollection = i;
            break;
         }
      }

      AclEntryDetailsDialog d = new AclEntryDetailsDialog(m_parent, true, m_curEntry, m_config );
      d.setVisible(true);
      if(d != null && d.getEntry() != null)
      {
         m_curEntry= d.getEntry();
         ((DefaultTableModel)m_dataModel).insertRow(iMin, convertDataToRow(m_curEntry));
         ((DefaultTableModel)m_dataModel).removeRow(iMin+1);
         m_table.repaint();
         m_table.getSelectionModel().setSelectionInterval(iMin, iMin);
         m_entries.set(indexInCollection, m_curEntry);
      m_bModified = true;
      }
   }

/**
   * Handler for delete button clicked. Removes the selected rows. Also removes the selected
   * items from the PSCollection of PSAaclEntry objects (m_entries).
   */
   private void onDelete()
   {
      int iMin = m_table.getSelectionModel().getMinSelectionIndex();
      int iMax = m_table.getSelectionModel().getMaxSelectionIndex();
      if(iMin >= 0 && iMax >= 0)
      {
         int iRowsToRemove = iMax-iMin+1;
         if(iRowsToRemove < 1)
            return;
         for(int i=0; i<iRowsToRemove; i++)
         {
            String strName = (String)m_table.getValueAt(iMin, 0);
            removeEntryFromCollection(strName);
            ((DefaultTableModel)m_table.getModel()).removeRow(iMin);
         }
      }
    m_bModified = true;
   }

/**
   * Handler for New button clicked. Brings up the AclEntryDetailsDialog to fill in the
   * properties of the new PSAclEntry object. Adds a new row. Also adds the entry to
   * the PSCollection of PSAaclEntry objects (m_entries).
   */
   private void onNew()
   {
      PSAclEntry entry = null;
      try
      {
         entry= new PSAclEntry("newEntry", PSAclEntry.ACE_TYPE_GROUP);
      }
      catch(IllegalArgumentException e)
      {
         e.printStackTrace();
      }

      AclEntryDetailsDialog d = new AclEntryDetailsDialog(m_parent, false, entry, m_config );
      d.setVisible(true);
      if(d != null && d.getEntry() != null)
      {
         m_curEntry= d.getEntry();
         ((DefaultTableModel)m_dataModel).addRow(convertDataToRow(m_curEntry));
         m_entries.add(m_curEntry);
      m_bModified = true;
      }
   }

/**
   * Removes the entry with the passed in name from the collection.
   *
   * @param strName The name of the PSAclEntry object to be removed from the
   * collection of PSAaclEntry objects (m_entries).
   */
   private void removeEntryFromCollection(String strName)
   {
      if(m_entries == null)
       return;

      for(int i=0; i<m_entries.size(); i++)
      {
         PSAclEntry entry = (PSAclEntry)m_entries.get(i);
         String strEntryName = entry.getName();
         if(strName.equals(strEntryName))
         {
            m_entries.remove(m_entries.get(i));
            break;
         }
      }

   }

/**
   * updates the PSAcl object in the ServerConfiguration object passed in to the constructor of this tab.
   *
  * Implements ITabDataHelper interface.
  *
  * @returns boolean <CODE>true</CODE> = data is ok; will not return
  * <CODE>false</CODE>.
   */
   public boolean saveTabData()
   {
    if (!m_bModified)
      return false;

    System.out.println("saving ServerAclSummary tab data");
    m_config.setServerAcl(m_acl);
    m_bModified = false;
    return true;
   }

/** Does not need validation. Thus does nothing.
*/
  public boolean validateTabData()
  {
    return true;
  }

//
// MEMBER VARIABLES
//

   private ServerConfiguration m_config = null;
   private Frame m_parent = null;

   private JButton m_editButton, m_deleteButton, m_newButton;
   private JTable  m_table;
   private TableModel m_dataModel;

   private PSAcl m_acl = null;
   private PSCollection m_entries = null;
   private PSAclEntry m_curEntry = null;

   private PSComboBox m_comboMultiGroup = null;

   private boolean m_bModified = false;

   /**
    * A checkbox to enable/disable returning information about the
    * security providers used while authenticating a login. If checked, the
    * detailed message is allowed, otherwise, a generic 'authentication
    * failed' msg is returned.
    */
   private JCheckBox m_allowDetailedMsg =
      new JCheckBox( "Allow detailed messages" /*sm_res.getString( "detailedMsgLabel" )*/);


   private static ResourceBundle sm_res = PSServerAdminApplet.getResources();
}


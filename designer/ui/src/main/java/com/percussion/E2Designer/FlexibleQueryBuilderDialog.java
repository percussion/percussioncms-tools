/******************************************************************************
 *
 * [ FlexibleQueryBuilderDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSConditional;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.Vector;

/** This dialog creates the numerous WHERE clauses that will be written to the
  * SELECT Statment TextArea of SelectorPropertyDialog.
  * <P>
  * <B>New WHERE clause construction:</B>
  * <UL>
  * <LI>The dialog will take all available SQL columns and list them in a
  * <I>uneditable</I> ComboBox (named &quot;SQL column&quot;).
  * <LI>A VALUE TextField (named &quot;Value&quot;) will allow user to enter
  * literals or variables (variable names must start with a :).  Variables can
  * be DragNDropped into the TextField from the VARIABLES list.
  * <LI>An <I>editable</I> ComboBox (named &quot;Relationship&quot;) lists
  * all the SQL operators and allows Variables here as well.
  * <LI>A CheckBox (named &quot;Omit from query when NULL&quot) will
  * automatically remove this clause from the query if any Variables used is
  * NULL.
  * <LI>Lastly, a List (named &quot;Variables&quot;) that supplies all variables
  * available to use in a WHERE clause.  This includes server variables and
  * input parameters.
  * </UL>
  * <P>
  * <B>WHERE clauses display:</B>
  * <P>
  * There will be a List (or Table) display of all the created/existing WHERE
  * clauses to be converted to the SELECT Statement in SelectorPropertyDialog.
  * The list entries of this list can be added/modified/Removed by using the
  * buttons of the appropriate names.  On clicking the &quot;OK&quot; buttons,
  * data in this list display gets converted to a SELECT text, then returned
  * to SelectorPropertyDialog.
*/

public class FlexibleQueryBuilderDialog extends PSDialog
                                        implements DragSourceListener,
                                                   DropTargetListener,
                                                   DragGestureListener
{
//
// CONSTRUCTORS
//

/** @param tank An OSBackendDatatank object for filling SQL column box.  It can
  * be null.
*/
  public FlexibleQueryBuilderDialog(OSBackendDatatank tank)
  {
    super();

    init(tank);
  }

  public FlexibleQueryBuilderDialog(JFrame frame, OSBackendDatatank tank)
  {
    super(frame);

    init(tank);
  }

//
// PUBLIC METHODS
//

/** @returns String The where clause portion of the select statement created by
  * this dialog.
*/
  public String getWhereClauses()
  {
    return m_whereClauses;
  }

/** @returns boolean true = this dialog was cancelled.
*/
  public boolean isCancel()
  {
    return m_isCancel;
  }

/** A verification method to check if the newly created WHERE clause already
  * exists in the WHERE clause list.
*/
  public boolean doesClauseAlreadyExist(WhereClauseItem item)
  {
    for (int i = 0; i < m_whereList.getRowCount(); i++)
    {
      if (item.equals(convertClauseRowToClauseItem(i)));
        return true;
    }
    return false;
  }

/** Used to convert WhereClauseItem objects from SelectorPropertyDialog to be
  * to usable, table rows of WHERE clauses(array of objects).
  *
  * @param item A WhereClauseItem object that represents this row.
  * @returns Object[] The row to be placed in the m_whereList table.
*/
  public Object[] convertClauseItemToClauseRow(WhereClauseItem item)
  {
    Object[] array = new Object[4];

    array[0] = item.getColumnName();
    array[1] = item.getOperator();
    array[2] = item.getValue();

    if (item.isOr())
      array[3] = "OR";
    else
      array[3] = "AND";

    return array;
  }

/** Used to convert a row of a WHERE clause to a WhereClauseItem.  Then passed
  * to SelectorPropertyDialog to be displayed.
  *
  * @param index The chosen WHERE clause to be converted.
  * @returns WhereClauseItem The wrapper object storing the WHERE clause data.
*/
  public WhereClauseItem convertClauseRowToClauseItem(int index)
  {
    String columnName = m_whereList.getValueAt(index, 0).toString();

    String op = m_whereList.getValueAt(index, 1).toString();

    String value = m_whereList.getValueAt(index, 2).toString();

    boolean isOr;
    if (m_whereList.getValueAt(index, 3).toString().equals("OR"))
      isOr = true;
    else
      isOr = false;

    return new WhereClauseItem(columnName, op, value, isOr);
  }


/////////////////////////////////////
// DragSourceListener implementations
/////////////////////////////////////

/**
*/
  public void dragEnter(DragSourceDragEvent evt)
  {}

/**
*/
  public void dragOver(DragSourceDragEvent evt)
  {

  }


  public void dropActionChanged(DragSourceDragEvent evt)
  {

  }

  public void dragExit(DragSourceEvent evt)
  {

  }

  public void dragDropEnd(DragSourceDropEvent evt)
  {
    System.out.println("dragDropEnd!!!");
  }

/////////////////////////////////////
// DropTargetListener implementations
/////////////////////////////////////

/**
*/
  public void dragEnter(DropTargetDragEvent evt)
  {
    System.out.println("dragEnter!");

    Point pt = evt.getLocation();
    Component target = evt.getDropTargetContext().getComponent();
    JComponent component = (JComponent)m_relationshipBox.getEditor().getEditorComponent();

    if (target.equals(m_valueField))
    {
      if (isInTarget(m_valueField, pt))
      {
        System.out.println("Target is m_valueField!");
        evt.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      }
    }
    else if (target.equals(component))
    {

      if (isInTarget(component, pt))
      {
        System.out.println("Target is m_relationshipBox!");
        evt.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      }
    }
    else
    {
      System.out.println("Drag rejected! (from dragEnter)");
      evt.rejectDrag();
    }

  }

/**
*/
  public void dragOver(DropTargetDragEvent evt)
  {
    //System.out.println("dragOver!");
  }


  public void dropActionChanged(DropTargetDragEvent evt)
  {
    System.out.println("drag action Changed!");
  }

  public void drop(DropTargetDropEvent evt)
  {
    System.out.println("dropped!!!");
    Point pt = evt.getLocation();
    Component target = evt.getDropTargetContext().getComponent();

    try
    {
      if (target == m_dndValueTarget.getComponent())
      {
        System.out.println("Target is valueField!");
        if (isInTarget(m_valueField, pt))
          evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      }
      else if (target == m_dndRelationTarget.getComponent())
      {
        System.out.println("Target is relationBox!");
        if (isInTarget(m_relationshipBox, pt))
          evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      }
      else
      {
        evt.rejectDrop();
        return;
      }

      if (target == m_dndValueTarget.getComponent())
      {
        // if m_relationshipBox selected either IN, NOT IN, BETWEEN, or NOT
        // BETWEEN, the dnd will test to see if the text currently in m_valueField
        // has a <CODE>,</CODE> (Comma) at the end.  If there is, simply add the
        // dnd string to it.  Otherwise, add a comma before the dnd string.
        String relation = m_relationshipBox.getSelectedItem().toString();
        if (relation.equals(PSConditional.OPTYPE_BETWEEN) || relation.equals(PSConditional.OPTYPE_NOTBETWEEN) || relation.equals(PSConditional.OPTYPE_IN) || relation.equals(PSConditional.OPTYPE_NOTIN))
        {
          String s = (String)evt.getTransferable().getTransferData(DataFlavor.stringFlavor);

          String value = m_valueField.getText().trim();

          if (value.length() - 1 == value.lastIndexOf(","))
            m_valueField.setText(m_valueField.getText() + " :" + s);
          else if (value.length() > 0)
            m_valueField.setText(m_valueField.getText() + ", :" + s);
          else
            m_valueField.setText(":" + s);
        }
        else
        {
          String s = (String)evt.getTransferable().getTransferData(DataFlavor.stringFlavor);

          m_valueField.setText(":" + s);
        }
      }
      else if (target == m_dndRelationTarget.getComponent())
      {
        String s = ":" + (String) evt.getTransferable().getTransferData(DataFlavor.stringFlavor);

        for (int i = 0; i < m_relationshipBox.getItemCount(); i++)
        {
          // check m_relationshipBox to see if the String matches any existing
          // item in the ComboBox.
          if (s.equals(m_relationshipBox.getItemAt(i).toString()))
          {
            m_relationshipBox.setSelectedIndex(i);
            return;
          }
        }
        // Otherwise, add this String to the ComboBox list and select it.
        m_relationshipBox.addItem(s);
        m_relationshipBox.setSelectedIndex(m_relationshipBox.getItemCount() - 1);
      }

      evt.dropComplete(true);
    }
    catch (Exception e)
    {
      evt.dropComplete(false);
      e.printStackTrace();
    }
  }

  public void dragExit(DropTargetEvent evt)
  {
    System.out.println("dragExit!");
  }

//////////////////////////////////////
// DragGestureListener implementations
//////////////////////////////////////


  public void dragGestureRecognized(DragGestureEvent evt)
  {
    System.out.println("Drag gesture recognized!");

    // converts the String from m_varList to a Transferable object
    StringSelection ss = new StringSelection(m_varList.getSelectedValue().toString());

    evt.startDrag(DragSource.DefaultCopyNoDrop, ss, this);
  }

//////////////////////////////////////


  public static void main(String[] args)
  {
    try
    {
         final JFrame frame = new JFrame("Test FlexibleQueryBuilderDialog");
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         frame.addWindowListener(new BasicWindowMonitor());

         JButton startButton = new JButton("Open Dialog");
         frame.getContentPane().add(startButton);
         startButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
          JDialog dialog = new FlexibleQueryBuilderDialog(frame, null);//, new OSDataSelector());
          dialog.setVisible(true);
            }
         });

         frame.setSize(640, 480);
         frame.setVisible(true);
    }
    catch (Exception ex)
    {
       ex.printStackTrace();
    }
  }

//
// PRIVATE METHODS
//

/** Provides additional validation to m_valueField specific to this dialog.
  *
  * @returns boolean true = m_valueField is valid.
*/
  private boolean isValueFieldValid()
  {
    String opType = m_relationshipBox.getSelectedItem().toString();

    // If m_relationshipBox selected BETWEEN or NOT BETWEEN, check
    // m_valueField.
    if (opType.equals(PSConditional.OPTYPE_BETWEEN) || opType.equals(PSConditional.OPTYPE_NOTBETWEEN))
      if (!checkValueForBETWEENConformance())
      {
        JOptionPane.showMessageDialog(null,
                                      getResources().getString("invalidbetween"),
                                      getResources().getString("warning"),
                                      JOptionPane.OK_OPTION);
        m_valueField.requestFocus();
        return false;
      }

    // If m_relationshipBox selected IN or NOT IN, check m_valueField.
    if (opType.equals(PSConditional.OPTYPE_IN) || opType.equals(PSConditional.OPTYPE_NOTIN))
      if (!checkValueForINConformance())
      {
        JOptionPane.showMessageDialog(null,
                                      getResources().getString("invalidin"),
                                      getResources().getString("warning"),
                                      JOptionPane.OK_OPTION);
        m_valueField.requestFocus();
        return false;
      }

    return true;
  }


/** Checkes the text in m_valueField to make sure there are 2 variables/literals,
  * separated by a <CODE>,</CODE> (Comma).
  *
  * @returns boolean true = text in m_valueField does conform to BETWEEN
  * requirements.
*/
  private boolean checkValueForBETWEENConformance()
  {
    StringTokenizer tester = new StringTokenizer(m_valueField.getText(), ",");
    String temp = null;
    int counter = 0;

    // making sure each token has no space inside.
    while (tester.hasMoreTokens())
    {
      temp = tester.nextToken();

      if (-1 != temp.trim().indexOf(" "))
        return false;

      counter++;
    }

    if (m_valueField.getText().trim().endsWith(","))
      return false;

    return counter == 2;
  }

/**
*/
  private boolean checkValueForINConformance()
  {
    StringTokenizer tester = new StringTokenizer(m_valueField.getText(), ",");
    String temp = null;
    int counter = 0;

    // making sure each token has no space inside.
    while (tester.hasMoreTokens())
    {
      temp = tester.nextToken();


      if (-1 != temp.trim().indexOf(" "))
        return false;

      counter++;
    }

    if (m_valueField.getText().trim().endsWith(","))
      return false;

    return counter >= 0;
  }



/** Takes the data in WHERE table and convert it to a String.
*/
  private void convertClauseTableToString()
  {
    String statement = new String();

    for (int i = 0; i < m_whereList.getRowCount(); i++)
    {
      statement = statement + m_whereList.getValueAt(i, 0) + " ";
      statement = statement + m_whereList.getValueAt(i, 1) + " ";
      statement = statement + m_whereList.getValueAt(i, 2) + " ";
      statement = statement + m_whereList.getValueAt(i, 3) + "\n";
    }
    m_whereClauses = statement;
  }


/** Used to find if the Point pt is within the bounds of the target JComponent.
*/
  private boolean isInTarget(JComponent target, Point pt)
  {
    Rectangle bound = null;
    bound = target.getBounds(bound);
    Point location = target.getLocation();

    int width = bound.width;
    int height = bound.height;

    System.out.println("Click point: (" + pt.x + ", " + pt.y + ")");
    System.out.println("Component top left: (" + location.x + ", " + 0 + ")");
    System.out.println("Component bottom right: (" + width + ", " + height + ")");

    if ((pt.x >= location.x) && (pt.x < width))
      if ((pt.y >= 0) && (pt.y < height))
        return true;

    return false;
  }

/** Takes the data in New WHERE panel components and convert them to a row of
  * data in the WHERE table.
  *
  * @param rowIndex Which clause row from the WHERE table to get the clause
  * data.
*/
  private void convertClauseToComponents(int rowIndex)
  {
    // Checking if no row is selected or the whereList table is empty, warn user
    // and ignore this method call.
    if (0 > m_whereList.getSelectedRow() || 0 == m_whereList.getRowCount())
    {
      JOptionPane.showMessageDialog(null,
                                    getResources().getString("select"),
                                    getResources().getString("warning"),
                                    JOptionPane.OK_OPTION);
      return;
    }

    // searching m_sqlColumnBox for a matching item to select
    String columnName = m_whereList.getValueAt(rowIndex, 0).toString();

    for (int i = 0; i < m_sqlColumnBox.getItemCount(); i++)
    {
      if (m_sqlColumnBox.getItemAt(i).equals(columnName))
      {
        m_sqlColumnBox.setSelectedIndex(i);
        break;
      }
    }

    // search m_relationshipBox for a match to select; if non exists, add new
    // entry to the comboBox list of m_relationshipBox.
    String relationType = m_whereList.getValueAt(rowIndex, 1).toString();
    boolean match = false;

    for (int i = 0; i < m_relationshipBox.getItemCount(); i++)
    {
      if (m_relationshipBox.getItemAt(i).equals(relationType))
      {
        m_relationshipBox.setSelectedIndex(i);
        match = true;
        break;
      }  
    }
    if (match == false)
      m_relationshipBox.addItem(m_whereList.getValueAt(rowIndex, 1));

    // replacing the text in m_valueField with the selected row's "value".
    m_valueField.setText(m_whereList.getValueAt(rowIndex, 2).toString());

    // setting the bool value to represent the selected WHERE clause.
    
    if (m_whereList.getValueAt(rowIndex, 3).equals("AND"))
      m_boolBox.setSelectedIndex(0);
    else
      m_boolBox.setSelectedIndex(1);
  }

/** Takes the data in WHERE table, then convert and set the data into New WHERE
  * panel components.
  *
  * @returns array Returns an row of Objects contained by an array of Objects.
*/
  private Object[] convertComponentsToClause()
  {
    Object[] array = new Object[4];

    array[0] = m_sqlColumnBox.getSelectedItem();
    array[1] = m_relationshipBox.getSelectedItem();
    array[2] = m_valueField.getText();
    array[3] = m_boolBox.getSelectedItem();

    return array;
  }


/** Constructors delegates work to this method.
*/
  private void init(OSBackendDatatank tank)
  {
    Vector columnData = null;

    if (tank != null)
      columnData = tank.getColumns();

    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    leftPanel.add(createNewClausePanel(columnData));
    leftPanel.add(Box.createVerticalStrut(6));
    leftPanel.add(createBoolPanel());
    //leftPanel.add(Box.createVerticalStrut(6));
    leftPanel.add(createWherePanel());

    JPanel mainPanel = new JPanel();
    mainPanel.setBorder(new EmptyBorder(0,6,0,6));
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
    mainPanel.add(leftPanel);
    mainPanel.add(Box.createHorizontalStrut(6));
    mainPanel.add(createVarListPanel());


    JPanel commandPanel = new JPanel();
    commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));
    commandPanel.add(Box.createHorizontalGlue());
    commandPanel.add(createCommandPanel());
    commandPanel.add(Box.createHorizontalGlue());

    // creating Drag & Drop functionality
    m_dndVarSource = new DragSource();
    m_dndRecognizer = m_dndVarSource.createDefaultDragGestureRecognizer(m_varList,
         DnDConstants.ACTION_COPY_OR_MOVE, this);

    m_dndRelationTarget = new DropTarget(m_relationshipBox.getEditor().getEditorComponent(),
                                         DnDConstants.ACTION_COPY_OR_MOVE, this);

    m_dndValueTarget = new DropTarget(m_valueField,
                                      DnDConstants.ACTION_COPY_OR_MOVE, this);

    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    this.getContentPane().add(commandPanel,BorderLayout.SOUTH);

    this.setSize(650, 400);
    this.center();
    //this.setVisible(true);

  }

/** Initializes the table holding all the where clauses.
*/
  private JTable createWhereTable(TableModel dataModel)
  {
    if (dataModel == null)
      dataModel = new DefaultTableModel(0, 4);

    JTable table = new JTable(dataModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setIntercellSpacing(new Dimension(0,0));
    table.setRowSelectionAllowed(true);
    table.setColumnSelectionAllowed(false);
    table.setCellSelectionEnabled(false);
    table.setShowVerticalLines(false);
    table.setShowGrid(false);
    table.setRequestFocusEnabled(false);
    table.setTableHeader(new JTableHeader());

    DefaultCellEditor editor = new DefaultCellEditor(new JTextField());


    // disabling the table cell editor
    //table.setCellEditor(new WhereTableCellEditor());
    table.setCellEditor(editor);

    // setting column identifiers
    String[] columnNames = {"bool", "sql", "relation", "value"};
    ((DefaultTableModel)dataModel).setColumnIdentifiers(columnNames);

    /*
    // implementing mouse double-click behavior
    table.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() >= 2)
        {
          System.out.println("Inside double clicked!");

          convertClauseToComponents(m_whereList.getSelectedRow());
        }
      }
    });
    */


    // setting the column widths
    table.setPreferredSize(new Dimension(390, 200));

    int tableWidth = table.getPreferredSize().width;

    table.getColumn("bool").setPreferredWidth(30);
    table.getColumn("sql").setPreferredWidth(140);
    table.getColumn("relation").setPreferredWidth(80);
    table.getColumn("value").setPreferredWidth(100);

    return table;
  }

/** Creates the list of Variables.
*/
  private JList createVarList()
  {
    // need to catalog for variables...
    String[] data = {"great", "good", "average", "below_average", "unacceptable"};

    JList list = new JList(data);

    return list;
  }

/** @returns JPanel Creates the control buttons, ok, cancel, and help.
*/
  private JPanel createCommandPanel()
  {
    JPanel commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
    {
      public void onOk()
      {
        System.out.println("OK button pressed!");
        m_isCancel = false;

        // todo: if this dialog was created by "Native Query", to this. Else
        // ("WHERE clause") to else.
        if (true)
        {
          convertClauseTableToString();
          FlexibleQueryBuilderDialog.this.setVisible(false);
        }
        else
        {

        }
      }

      public void onCancel()
      {
        m_isCancel = true;
        FlexibleQueryBuilderDialog.this.setVisible(false);
      }

      public void onHelp()
      {
        System.out.println("Converting clause to components!");
        convertClauseToComponents(m_whereList.getSelectedRow());
      }

    };

    return commandPanel;
  }

/** @returns JPanel Creates the boolBox between the New WHERE Clause panel and
  * the WHERE clause panel.
*/
  private JPanel createBoolPanel()
  {
    String[] conditionals = {"AND", "OR"};
    m_boolBox = new PSComboBox(conditionals);
    m_boolBox.setEditable(false);
    m_boolBox.setPreferredSize(new Dimension(60, 24));
    m_boolBox.setMaximumSize(m_boolBox.getPreferredSize());
    m_boolBox.setMinimumSize(m_boolBox.getPreferredSize());


    JPanel boolPanel = new JPanel();
    boolPanel.setLayout(new BoxLayout(boolPanel, BoxLayout.X_AXIS));
    boolPanel.add(new JLabel(getResources().getString("bool")));
    boolPanel.add(Box.createHorizontalStrut(3));
    boolPanel.add(m_boolBox);
    boolPanel.add(Box.createHorizontalGlue());
    boolPanel.setBorder(new EmptyBorder(0,6,0,0));


    return boolPanel;
  }

/** @returns JPanel Creates the Variable list.
*/
  private JPanel createVarListPanel()
  {
    m_varList = createVarList();
    JScrollPane scrollPane = new JScrollPane(m_varList);


    JPanel varListLabel = new JPanel();
    varListLabel.setLayout(new BoxLayout(varListLabel, BoxLayout.X_AXIS));
    varListLabel.add(new JLabel(getResources().getString("var")));
    varListLabel.add(Box.createHorizontalGlue());

    JPanel varListPanel = new JPanel();
    varListPanel.setPreferredSize(new Dimension(130, 400));
    varListPanel.setBorder(new EmptyBorder(6,0,0,0));
    varListPanel.setLayout(new BoxLayout(varListPanel, BoxLayout.Y_AXIS));
    varListPanel.add(varListLabel);
    varListPanel.add(scrollPane);

    return varListPanel;
  }

/** @returns JPanel Creates the panel holding the New WHERE clause construction
  * components.
*/
  private JPanel createNewClausePanel(Vector columnData)
  {
    /////////////////////////////////////////
    // setting up the compnents of this panel
    /////////////////////////////////////////

    String[] relations = {
                          PSConditional.OPTYPE_EQUALS,
                          PSConditional.OPTYPE_NOTEQUALS,
                          PSConditional.OPTYPE_GREATERTHAN,
                          PSConditional.OPTYPE_GREATERTHANOREQUALS,
                          PSConditional.OPTYPE_LESSTHAN,
                          PSConditional.OPTYPE_LESSTHANOREQUALS,
                          PSConditional.OPTYPE_ISNULL,
                          PSConditional.OPTYPE_ISNOTNULL,
                          PSConditional.OPTYPE_LIKE,
                          PSConditional.OPTYPE_NOTLIKE,
                          PSConditional.OPTYPE_BETWEEN,
                          PSConditional.OPTYPE_NOTBETWEEN,
                          PSConditional.OPTYPE_IN,
                          PSConditional.OPTYPE_NOTIN
                         };


    if (columnData == null)
      m_sqlColumnBox = new PSComboBox();
    else
      m_sqlColumnBox = new PSComboBox(columnData);
    m_sqlColumnBox.setEditable(false);
    m_sqlColumnBox.setPreferredSize(new Dimension(260, 24));
    m_sqlColumnBox.setMaximumSize(m_sqlColumnBox.getPreferredSize());
    m_sqlColumnBox.setMinimumSize(m_sqlColumnBox.getPreferredSize());


    m_relationshipBox = new PSComboBox(relations);
    m_relationshipBox.setEditable(true);
    m_relationshipBox.setMaximumSize(m_relationshipBox.getPreferredSize());
    m_relationshipBox.setMinimumSize(m_relationshipBox.getPreferredSize());


    m_valueField = new JTextField();


    /*
    m_omitBox = new JCheckBox(getResources().getString("omit"));
    m_omitBox.setEnabled(false);
    */

    //////////////////////////////////////////////////////////
    // creating layout design for the components of this panel
    //////////////////////////////////////////////////////////

    JPanel sqlColumnLabel = new JPanel();
    sqlColumnLabel.setLayout(new BoxLayout(sqlColumnLabel, BoxLayout.X_AXIS));
    sqlColumnLabel.add(new JLabel(getResources().getString("sql")));
    sqlColumnLabel.add(Box.createHorizontalGlue());

    JPanel sqlColumnPanel = new JPanel();
    sqlColumnPanel.setLayout(new BoxLayout(sqlColumnPanel, BoxLayout.Y_AXIS));
    sqlColumnPanel.add(sqlColumnLabel);
    sqlColumnPanel.add(m_sqlColumnBox);


    JPanel relationshipLabel = new JPanel();
    relationshipLabel.setLayout(new BoxLayout(relationshipLabel, BoxLayout.X_AXIS));
    relationshipLabel.add(new JLabel(getResources().getString("relationship")));
    relationshipLabel.add(Box.createHorizontalGlue());

    JPanel relationshipPanel = new JPanel();
    relationshipPanel.setLayout(new BoxLayout(relationshipPanel, BoxLayout.Y_AXIS));
    relationshipPanel.add(relationshipLabel);
    relationshipPanel.add(m_relationshipBox);


    JPanel valueLabel = new JPanel();
    valueLabel.setLayout(new BoxLayout(valueLabel, BoxLayout.X_AXIS));
    valueLabel.add(new JLabel(getResources().getString("value")));
    valueLabel.add(Box.createHorizontalGlue());

    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.Y_AXIS));
    valuePanel.add(valueLabel);
    valuePanel.add(m_valueField);


    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
    topPanel.add(sqlColumnPanel);
    topPanel.add(Box.createHorizontalStrut(3));
    topPanel.add(relationshipPanel);


    /*
    JPanel innerPanel = new JPanel();
    innerPanel.setBorder(new EmptyBorder(0,0,0,0));
    innerPanel.add(m_omitBox);
    JPanel omitBoxPanel = new JPanel();
    omitBoxPanel.setLayout(new BoxLayout(omitBoxPanel, BoxLayout.X_AXIS));
    omitBoxPanel.add(innerPanel);
    omitBoxPanel.add(Box.createHorizontalGlue());
    */


    JPanel newClausePanel = new JPanel();
    newClausePanel.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                                                 getResources().getString("new")),
                                                new EmptyBorder(0,0,6,92)));
    newClausePanel.setLayout(new BoxLayout(newClausePanel, BoxLayout.Y_AXIS));
    newClausePanel.add(topPanel);
    newClausePanel.add(Box.createVerticalStrut(3));
    newClausePanel.add(valuePanel);
    //newClausePanel.add(Box.createVerticalStrut(3));
    //newClausePanel.add(omitBoxPanel);

    return newClausePanel;
  }

/** @returns JPanel Creates the panel holding the WHERE clause table and its
  * 3 modification buttons.
*/
  private JPanel createWherePanel()
  {
    // adding m_valueField and m_relationshipBox to validationFramework
    ValidationConstraint[] constraints = new ValidationConstraint[2];
    constraints[0] = new StringConstraint();
    constraints[1] = new StringConstraint();

    Component[] components = new Component[2];
    components[0] = m_valueField;
    components[1] = m_relationshipBox;

    setValidationFramework(components, constraints);

    m_addButton = new UTFixedButton(getResources().getString("add"));
    m_addButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        // added empty field validation (prevents empty string to be entered)
        if (activateValidation() && isValueFieldValid())
        {
          /*
          boolean isOr = false;
          if (m_boolBox.getSelectedItem().toString().equals("OR"))
            isOr = true;
          else
            isOr = false;

          WhereClauseItem item = new WhereClauseItem(m_sqlColumnBox.getSelectedItem().toString(),
                                                     m_relationshipBox.getSelectedItem().toString(),
                                                     m_valueField.getText(),
                                                     isOr);

          if (!doesClauseAlreadyExist(item))
          {
          */

            ((DefaultTableModel)m_whereList.getModel()).addRow(convertComponentsToClause());

            int index = ((DefaultTableModel)m_whereList.getModel()).getRowCount() - 1;

            m_whereList.getSelectionModel().setSelectionInterval(index, index);

            m_whereList.repaint();

          /*
          }
          else
          {
            System.out.println("add a warning dialog");
            // todo: need to add a warning JOptionPane.
          }
          */
        }
      }
    });

    m_modifyButton = new UTFixedButton(getResources().getString("modify"));
    m_modifyButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        // added empty field validation (prevents empty string to be entered)
        if (activateValidation() && isValueFieldValid())
        {
          int index = m_whereList.getSelectedRow();

          if (!(index < 0 || index >= ((DefaultTableModel)m_whereList.getModel()).getRowCount()))
          {
            ((DefaultTableModel)m_whereList.getModel()).removeRow(index);
            ((DefaultTableModel)m_whereList.getModel()).insertRow(index, convertComponentsToClause());

            m_whereList.getSelectionModel().setSelectionInterval(index, index);

            m_whereList.repaint();
          }
        }
      }
    });

    m_removeButton = new UTFixedButton(getResources().getString("remove"));
    m_removeButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {

        int index = m_whereList.getSelectedRow();

        if (!(index < 0 || index >= ((DefaultTableModel)m_whereList.getModel()).getRowCount()))
        {
          ((DefaultTableModel)m_whereList.getModel()).removeRow(index);

          if (index >= ((DefaultTableModel)m_whereList.getModel()).getRowCount())
          {
            index = ((DefaultTableModel)m_whereList.getModel()).getRowCount() - 1;

            m_whereList.getSelectionModel().setSelectionInterval(index, index);
          }
          else if (0 < ((DefaultTableModel)m_whereList.getModel()).getRowCount())
            m_whereList.getSelectionModel().setSelectionInterval(index, index);

          m_whereList.repaint();
        }
      }
    });


    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(new EmptyBorder(0,6,0,6));
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    buttonPanel.add(m_addButton);
    buttonPanel.add(Box.createVerticalStrut(4));
    buttonPanel.add(m_modifyButton);
    buttonPanel.add(Box.createVerticalStrut(4));
    buttonPanel.add(m_removeButton);
    buttonPanel.add(Box.createVerticalGlue());

    // creating member data, the where clauses table, then setting it to a
    // JScrollPane.
    m_whereList = createWhereTable(null);
    JPanel whitePanel = new JPanel(new BorderLayout()); // just to make BG white
    whitePanel.setBackground(Color.white);
    whitePanel.add(m_whereList, BorderLayout.CENTER);
    JScrollPane scrollPane = new JScrollPane(whitePanel);

    JPanel wherePanel = new JPanel();
    wherePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                          getResources().getString("where")));
    wherePanel.setLayout(new BoxLayout(wherePanel, BoxLayout.X_AXIS));
    wherePanel.add(scrollPane);
    wherePanel.add(buttonPanel);

    return wherePanel;
  }




//
// MEMBER VARIABLES
//

  private UTFixedButton m_addButton, m_modifyButton, m_removeButton;
  private PSComboBox    m_sqlColumnBox, m_relationshipBox, m_boolBox;
  private JCheckBox     m_omitBox;
  private JTextField    m_valueField;
  private JList         m_varList;
  private JTable        m_whereList;

  private String        m_whereClauses = null;
  private boolean       m_isCancel = false;

  // drag and drop members
   private String m_draggingString = null;
   private DropTarget m_dndRelationTarget = null;
    private DropTarget m_dndValueTarget = null;
  private DragSource m_dndVarSource = null;
  private DragGestureRecognizer m_dndRecognizer = null;
   private boolean m_bAcceptDrag = false;
}

 

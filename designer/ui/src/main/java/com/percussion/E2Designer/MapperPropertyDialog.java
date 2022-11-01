/******************************************************************************
 *
 * [ MapperPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.ImageListControl.ImageListItem;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * The column mapper property dialog.
 */
public class MapperPropertyDialog extends PSEditorDialog implements
   MouseListener, ActionListener
{
   /**
    * Construct the default mapper property dialog.
    */
   public MapperPropertyDialog()
   {
      super();
      initDialog();
   }

   public MapperPropertyDialog(Window parent)
   {
      super(parent);
      initDialog();
   }

   /**
    * Construct the default mapper property dialog.
    */
   public MapperPropertyDialog(JFrame parent, OSDataMapper mapper)
   {
      super(parent);
      setLocationRelativeTo(parent);
      m_mapper = mapper;
      initDialog();
   }


   /**
    * Create the XML table view. This scoll pane contains the XML tree on its
    * left hand side and the back-end table on its right hand side.
    *
    * @return the table view, a scrollable pane, never <code>null</code>.
    */
   private JScrollPane createMappingTableView()
   {
      try
      {
         // need this for the context menu
         m_mappingView.addMouseListener(this);
         m_mappingView.getTableHeader().addMouseListener(this);

         // create the context menu
         m_contextMenu.add(m_append);
         m_append.addActionListener(this);
         m_contextMenu.add(m_insert);
         m_insert.addActionListener(this);
         m_contextMenu.add(m_delete);
         m_delete.addActionListener(this);

         // hide group column
         m_mappingView.removeColumn(m_mappingView.getColumn(
            m_mappingTable.getResources().getString("group")));

         // set renderers
         ConditionalCellRenderer condRenderer = new ConditionalCellRenderer();
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "conditionals")).setCellRenderer(condRenderer);
         MapperCellRenderer mapperRenderer = new MapperCellRenderer();
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "backend")).setCellRenderer(mapperRenderer);
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "xml")).setCellRenderer(mapperRenderer);

         // set editors
         setEditors();

         // initialize predefined column sizes
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "backend")).setPreferredWidth(200);
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "xml")).setPreferredWidth(200);
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "conditionals")).setPreferredWidth(20);
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "conditionals")).setMaxWidth(20);
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "conditionals")).setMinWidth(20);

         // create the scroll pane and add all its contents
         m_pane = new JScrollPane(m_mappingView);
         
         return m_pane;
      }
      catch (ClassNotFoundException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }

   /**
    * Create and initialize all cell editors.
    */
   private void setEditors()
   {
      try
      {
         ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
            (OSBackendDatatank) null, null);
         m_udfDialog = new FormulaPropertyDialog();

         m_backendDialog = new ValueSelectorDialog(this, h.getDataTypes(), 
            null);
         m_backendEditor = new MapperCellEditor(m_backendDialog, m_udfDialog);
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "backend")).setCellEditor(m_backendEditor);

         m_xmlDialog = new ValueSelectorDialog(this, h.getDataTypes(), null);
         m_xmlEditor = new MapperCellEditor(m_xmlDialog, m_udfDialog);
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "xml")).setCellEditor(m_xmlEditor);

         m_condDialog = new ConditionalPropertyDialog();
         m_condEditor = new ConditionalCellEditor(m_condDialog);
         m_mappingView.getColumn(m_mappingTable.getResources().getString(
            "conditionals")).setCellEditor(m_condEditor);
      }
      catch (ClassNotFoundException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }

   /**
    * Create the mappers view panel.
    *
    * @return the view panel, never <code>null</code>.
    */
   private JPanel createViewPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());

      // create mapping view table
      panel.add(createMappingTableView(), "Center");

      return panel;
   }

   /**
    * Initialize the XML browser panel.
    *
    * @param tank the page datatank, assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void initXmlBrowser(OSPageDatatank tank)
   {
      Vector selections = new Vector();
      selections.addElement(new ImageListItem(m_pageIcon, 
         tank.getSchemaSource().toString(), tank));
      
      // add UDF's to the selectable items
      if (!editingQuery())
         initDatatypes(selections);

      m_xmlBrowser = new MapBrowser(selections, m_udfSet, m_bIsQuery);

      MapBrowserTree tree = m_xmlBrowser.getTree();
      tree.setDTDRepeatAttributesReadOnly();
   }

   /**
    * Initialize all valid selections.
    * 
    * @param selections the selections to which we add all valid datatypes, 
    *    assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void initDatatypes(Vector selections)
   {
      selections.addElement(new ImageListItem(m_udfIcon, E2Designer
         .getResources().getString("predefinedUdfs"), m_udfSet));

      // THESE TWO TYPES ARE NOW SUPPORTED FOR BACKEND SOURCE
      Vector cgiVariables = CatalogCgiVariables.getCatalog(false);
      if (cgiVariables.size() != 0)
      {
         selections.addElement(new ImageListItem(m_cgiIcon, E2Designer
            .getResources().getString("cgiVariables"), cgiVariables));
      }

      Vector userContext = CatalogUserContext.getCatalog(false);
      if (userContext.size() != 0)
      {
         selections.addElement(new ImageListItem(m_userContextIcon, E2Designer
            .getResources().getString("userContext"), userContext));
      }

      Vector cookieVariables = CatalogCookie.getCatalog(false);
      if (cookieVariables.size() != 0)
      {
         selections.addElement(new ImageListItem(m_cgiIcon, E2Designer
            .getResources().getString("cookieVariables"), cookieVariables));
      }
   }

   /**
    * Initialize the backend browser panel.
    * 
    * @param tank the backend datatank, assumed not <code>null</code>.
    * @returns <code>true</code> if successfully initalized <code>false</code>
    *    if the collection of tables is <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   private boolean initBackendBrowser(OSBackendDatatank tank)
   {
      Vector selections = new Vector();
      PSCollection tables = tank.getTables();

      if (null == tables || 0 == tables.size())
         return false;

      for (int i = 0, n = tables.size(); i < n; i++)
      {
         OSBackendTable table = (OSBackendTable) tables.get(i);
         selections.addElement(new ImageListItem(m_backendIcon, 
            table.getAlias(), table));
      }

      // add UDF's to the selectable items
      if (editingQuery())
         initDatatypes(selections);

      m_backendBrowser = new MapBrowser(selections, m_udfSet, m_bIsQuery);
      MapBrowserTree tree = m_backendBrowser.getTree();
      tree.setDTDRepeatAttributesReadOnly();
      
      return true;
   }

   /**
    * Implementation for TableModelListener to handle table model changes for
    * all table views.
    * 
    * @param e the table model event, not used.
    */
   @SuppressWarnings("unused")
   public void tableChanged(TableModelEvent e)
   {
      m_mappingView.repaint();
   }


  /**
   * The add button takes the selected items on both sides and adds them to 
   * the table.
   */
   private void onAdd()
   {
      MapBrowserTreeNode backendNode = m_backendBrowser.getSelectedNode();
      MapBrowserTreeNode xmlNode = m_xmlBrowser.getSelectedNode();
      if (backendNode != null && xmlNode != null)
         addNodesToTable(backendNode, xmlNode);
   }

  /**
   * Add the provided nodes to the mapping table.
   *
   * @param backendNode the backend node to be added, may be <code>null</code>.
   * @param xmlNode the XML node to be added, may be <code>null</code>.
   */
   public void addNodesToTable(MapBrowserTreeNode backendNode,
      MapBrowserTreeNode xmlNode)
   {
      if (backendNode == null || xmlNode == null)
         return;

      // look for first empty row
      for (int iRow = 0; iRow < m_mappingView.getRowCount(); ++iRow)
      {
         if (m_mappingView.getValueAt(iRow, 0) != null
            && m_mappingView.getValueAt(iRow, 0).toString().length() == 0
            && m_mappingView.getValueAt(iRow, 1) != null
            && m_mappingView.getValueAt(iRow, 1).toString().length() == 0)
         {
            int iBackend = 1;
            int iXML = 0;

            if (m_bIsQuery.booleanValue())
            {
               iBackend = 0;
               iXML = 1;
            }

            m_mappingView.setNode(backendNode, iRow, iBackend);
            m_mappingView.setNode(xmlNode, iRow, iXML);
            m_mappingView.repaint();

            break;
         }
      }
   }

   /**
    * Guess all XML to back-end mappings. Maps the XML page to EXACT matches
    * found in the backend table.
    */
   private void onGuess()
   {
      OSDataMapper mapper = new OSDataMapper(m_mapper);
      mapper.clear();
      mapper.guessMapping(m_backendTank, m_pageTank, true, true);

      while (m_mappingTable.getRowCount() > 0)
         m_mappingTable.deleteRow(0);

      m_mappingTable.loadFromMapper(mapper);// , serverUdfs);
      m_mappingTable.appendRow(128 - m_mappingTable.getRowCount());
      m_mappingTable.fireTableDataChanged();
   }


   /**
    * Clear all XML to back-end mappings.
    */
   protected void onClear()
   {
      m_mappingTable.clearColumn(m_mappingTable.findColumn(
         m_mappingTable.getResources().getString("backend")));
      m_mappingTable.clearColumn(m_mappingTable.findColumn(
         m_mappingTable.getResources().getString("xml")));
      m_mappingTable.clearColumn(m_mappingTable.findColumn(
         m_mappingTable.getResources().getString("conditionals")));
      m_mappingTable.clearColumn(m_mappingTable.findColumn(
         m_mappingTable.getResources().getString("group")));
   }

   /**
    * Removes one row of XML to back-end mappings.
    */
   protected void onRemove()
   {
      int col = m_mappingView.getSelectedColumn();
      int row = m_mappingView.getSelectedRow();
      
      // making sure that a row is selected before deleting it. -1 means no row
      // is selected. Code will do nothing if the value is -1.
      if (-1 != col && -1 != row)
      {
         m_mappingView.getCellEditor(row, col).stopCellEditing();
         m_mappingTable.deleteRow(row);
      }
   }


  /**
   * Create the command panel.
   *
   * @return the command panel, never <code>null</code>.
   */
   private JPanel createCommandPanel()
   {
      m_commandPanel = new UTStandardCommandPanel(this, "",
         SwingConstants.HORIZONTAL)
      {
         @Override
         public void onOk()
         {
            MapperPropertyDialog.this.onOk();
         }

         @Override
         public void onCancel()
         {
            MapperPropertyDialog.this.onCancel();
         }
      };

      // m_guessButton.setIcon(this.m_warningIcon);
      m_guessButton.addActionListener(new ActionListener()
      {
         @SuppressWarnings("unused")
         public void actionPerformed(ActionEvent event)
         {
            boolean isTableEmpty = true;
            for (int i = 0; i < m_mappingTable.getRowCount(); i++)
            {
               if (!m_mappingTable.getValueAt(i, MapperTableModel.BACKEND)
                  .toString().equals("")
                  && !m_mappingTable.getValueAt(i, MapperTableModel.XML)
                     .toString().equals(""))
               {
                  isTableEmpty = false;
                  break;
               }
            }

            // if table is empty, no need to display warning message; just guess
            if (!isTableEmpty)
            {
               int response = JOptionPane.showConfirmDialog(
                  MapperPropertyDialog.this, getResources().getString(
                     "clearwarning"), E2Designer.getResources().getString(
                     "ConfirmOperation"), JOptionPane.YES_NO_OPTION,
                  JOptionPane.WARNING_MESSAGE);
               if (JOptionPane.YES_OPTION == response)
                  onGuess();
            }
            else
               onGuess();
         }
      });

      m_addButton.addActionListener(new ActionListener()
      {
         @SuppressWarnings("unused")
         public void actionPerformed(ActionEvent event)
         {
            onAdd();
         }
      });

      // m_clearButton.setIcon(this.m_warningIcon);
      m_clearButton.addActionListener(new ActionListener()
      {
         @SuppressWarnings("unused")
         public void actionPerformed(ActionEvent event)
         {
            boolean isTableEmpty = true;
            for (int i = 0; i < m_mappingTable.getRowCount(); i++)
            {
               if (!m_mappingTable.getValueAt(i, MapperTableModel.BACKEND)
                  .toString().equals("")
                  && !m_mappingTable.getValueAt(i, MapperTableModel.XML)
                     .toString().equals(""))
               {
                  isTableEmpty = false;
                  break;
               }
            }

            // if table is not empty, warning user first; else, do nothing
            if (!isTableEmpty)
            {
               int response = JOptionPane.showConfirmDialog(
                  MapperPropertyDialog.this, getResources().getString(
                     "clearwarning"), E2Designer.getResources().getString(
                     "ConfirmOperation"), JOptionPane.YES_NO_OPTION,
                  JOptionPane.WARNING_MESSAGE);
               if (JOptionPane.YES_OPTION == response)
                  onClear();
            }
         }
      });

      m_removeButton.addActionListener(new ActionListener()
      {
         @SuppressWarnings("unused")
         public void actionPerformed(ActionEvent e)
         {
            onRemove();
         }
      });

      m_guessButton.setMnemonic(getResources().getString("guess.mn").charAt(0));
      m_removeButton.setMnemonic(getResources().getString(
         "remove.mn").charAt(0));
      m_clearButton.setMnemonic(getResources().getString("clear.mn").charAt(0));
      m_addButton.setMnemonic(getResources().getString("add.mn").charAt(0));
      m_emptyXmlBox.setMnemonic(getResources().getString(
         "emptyXml.mn").charAt(0));

      JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
      p1.add(m_addButton);
      p1.add(m_removeButton);

      JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pRight.add(m_guessButton);
      pRight.add(m_clearButton);

      m_pBottom = new JPanel();
      m_pBottom.setLayout(new BorderLayout());
      m_pBottom.add(m_emptyXmlBox, BorderLayout.WEST);

      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(m_commandPanel, BorderLayout.EAST);
      m_pBottom.add(cmdPanel, BorderLayout.EAST);

      JPanel p2 = new JPanel(new BorderLayout());
      p2.add(p1, "Center");
      p2.add(pRight, "East");
      p2.add(m_pBottom, "South");

      return p2;
   }

   /**
    * Initialize the dialogs GUI elements with its data.
    */
   private void initDialog()
   {
      setResizable(true);
      m_currentEditor = this;

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(createViewPanel(), "Center");
      panel.add(createCommandPanel(), "South");

      // set the default button
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel, "Center");
      Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit()
         .getScreenSize());
      getRootPane().setPreferredSize(
         new Dimension((screenSize.width / 5) * 4,
            (screenSize.height / 5) * 4));

      pack();
   }

   /**
    * Handles key released events.
    */
   public void addKeyEnterHandler()
   {
      addKeyListener(new KeyAdapter()
      {
         @Override
         public void keyReleased(KeyEvent e)
         {
            if (e.getKeyCode() == e.VK_ENTER)
               if (m_mappingView.isEditing())
                  m_mappingView.getCellEditor().stopCellEditing();
         }
      });
   }

   // see MouseListener
   public void mousePressed(MouseEvent e)
   {
      showContextMenu(e);
   }

   // see MouseListener
   public void mouseClicked(MouseEvent e)
   {
      showContextMenu(e);
   }

   // see MouseListener
   @SuppressWarnings("unused")
   public void mouseEntered(MouseEvent e)
   {
      // noop
   }

   // see MouseListener
   @SuppressWarnings("unused")
   public void mouseExited(MouseEvent e)
   {
      // noop
   }

   // see MouseListener
   public void mouseReleased(MouseEvent e)
   {
      showContextMenu(e);
   }

  /**
   * Show the context menu according to event source and row selection.
   *
   * @param e the mouse event, assumed not <code>null</code>.
   */
   private void showContextMenu(MouseEvent e)
   {
      if (e.isPopupTrigger())
      {
         if ((m_mappingView.getSelectedRow() == -1)
            || (e.getSource() instanceof JTableHeader))
         {
            m_insert.setEnabled(false);
            m_delete.setEnabled(false);
         }
         else
         {
            m_insert.setEnabled(true);
            m_delete.setEnabled(true);
         }

         m_contextMenu.show(m_mappingView, e.getX(), e.getY());
      }
   }

   // see ActionListener
   public void actionPerformed(ActionEvent e)
   {
      if (e.getActionCommand().equals(getResources().getString("append")))
         onAppend();
      else if (e.getActionCommand().equals(getResources().getString("insert")))
         onInsert();
      else if (e.getActionCommand().equals(getResources().getString("delete")))
         onDelete();
   }

   /**
    * Append a new row to the table.
    */
   private void onAppend()
   {
      m_mappingTable.appendRow();
   }

  /**
   * Insert a new row above the current selected.
   */
   private void onInsert()
   {
      m_mappingTable.insertRow(m_mappingView.getSelectedRow());
   }


   /**
    * Delete selected row.
    */
   private void onDelete()
   {
      int iSelectedRow = m_mappingView.getSelectedRow();
      
      // making sure that a row is selected before deleting it. -1 means no row
      // is selected. Code will do nothing if the value is -1.
      if (-1 != iSelectedRow)
           m_mappingTable.deleteRow(iSelectedRow);
   }

   // see IEditor
   public boolean onEdit(UIFigure figure, final Object data)
   {
      try
      {
         if (data instanceof PSApplication)
            m_app = (PSApplication) data;
         else
            throw new IllegalArgumentException("PSApplication expected!");

         if (figure.getData() instanceof OSDataMapper)
         {
            m_mapper = (OSDataMapper) figure.getData();
            m_udfSet = ((OSApplication) m_app).getUdfSet();
            m_mappingView.setUdfSet(m_udfSet);

            UTPipeNavigator navigator = new UTPipeNavigator();
            UIFigure pipeFigure = navigator.getPipe(figure);
            UIFigure backend = navigator.getBackendTank(figure);
            UIFigure page = navigator.getPageTank(figure);

            if (null == pipeFigure)
            {
               JOptionPane.showMessageDialog(
                  E2Designer.getApp().getMainFrame(), getResources().getString(
                     "errornopipe"), E2Designer.getResources().getString(
                     "OpErrorTitle"), JOptionPane.ERROR_MESSAGE);

               return false;
            }
            if (null == backend)
            {
               JOptionPane.showMessageDialog(
                  E2Designer.getApp().getMainFrame(), getResources().getString(
                     "errornobackendtable"), E2Designer.getResources()
                     .getString("OpErrorTitle"), JOptionPane.ERROR_MESSAGE);

               return false;
            }

            if (pipeFigure != null
               && pipeFigure.getData() instanceof PSQueryPipe)
            {
               m_bIsQuery = Boolean.TRUE;
            }
            else
            {
               m_bIsQuery = Boolean.FALSE;
               m_pBottom.remove(m_emptyXmlBox);
            }

            if (backend != null
               && backend.getData() instanceof OSBackendDatatank)
            {
               m_backendTank = (OSBackendDatatank) backend.getData();

               if (!initBackendBrowser(m_backendTank))
               {
                  JOptionPane.showMessageDialog(E2Designer.getApp()
                     .getMainFrame(), getResources().getString(
                     "errornobackendtable"), E2Designer.getResources()
                     .getString("OpErrorTitle"), JOptionPane.ERROR_MESSAGE);

                  return false;
               }

               int type = m_bIsQuery.booleanValue() ? 
                  ValueSelectorDialogHelper.MAPPER_SOURCE_BACKEND : 
                     ValueSelectorDialogHelper.MAPPER_TARGET;
               ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
                  m_backendTank, null, type);
               m_backendDialog.refresh(h.getDataTypes());

               // update backend editor's default datatype
               DTTextLiteral dtLiteral = new DTTextLiteral();
               m_backendDialog.setDefaultType(dtLiteral);
               m_backendEditor.setDefaultType(dtLiteral);

               m_mappingTable.setBackendTank(m_backendTank);
            }
            else
            {
               m_mappingTable.setBackendTank(null);
               m_udfDialog.refreshDialog(m_udfSet);
               m_mappingView.refreshUdfDialog(m_udfSet);
            }

            if (page != null && page.getData() instanceof OSPageDatatank)
            {
               m_pageTank = (OSPageDatatank) page.getData();
               int type = m_bIsQuery.booleanValue() ? 
                  ValueSelectorDialogHelper.MAPPER_TARGET : 
                     ValueSelectorDialogHelper.MAPPER_SOURCE_XML;
               ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
                  null, m_pageTank, type);
               m_xmlDialog.refresh(h.getDataTypes());

               // update xml editor's default datatype
               DTXMLField xmlField = new DTXMLField(m_pageTank.getColumns());
               m_xmlDialog.setDefaultType(xmlField);
               m_xmlEditor.setDefaultType(xmlField);

               initXmlBrowser(m_pageTank);
               m_mappingTable.setPageTank(m_pageTank);
            }
            else
               m_mappingTable.setPageTank(null);

            if (pipeFigure != null && pipeFigure.getData() instanceof PSPipe)
            {
               PSPipe pipe = (PSPipe) pipeFigure.getData();

               // alternating the xml and backend columns depending on the
               // type of the pipe.
               if (pipe instanceof PSQueryPipe)
               {
                  int xmlIndex = m_mappingView.getColumn(
                     m_mappingTable.getResources().getString("xml"))
                     .getModelIndex();
                  int backendIndex = m_mappingView.getColumn(
                     m_mappingTable.getResources().getString("backend"))
                     .getModelIndex();

                  m_mappingView.moveColumn(backendIndex, xmlIndex);

                  if (m_backendBrowser != null)
                     getContentPane().add(m_backendBrowser, "West");

                  if (m_xmlBrowser != null)
                     getContentPane().add(m_xmlBrowser, "East");

                  m_udfDialog.refreshDialog(m_udfSet, null, m_backendTank);
                  m_mappingView.refreshUdfDialog(m_udfSet, null, m_backendTank);

                  pack();
               }
               else if (pipe instanceof PSUpdatePipe)
               {
                  if (m_backendBrowser != null)
                     getContentPane().add(m_backendBrowser, "East");

                  if (m_xmlBrowser != null)
                     getContentPane().add(m_xmlBrowser, "West");

                  // removing "Conditionals" column in the update mapper
                  // table
                  TableModel model = m_mappingView.getModel();
                  String id = ((MapperTableModel) model).getResources()
                     .getString("conditionals");
                  m_mappingView.removeColumn(m_mappingView.getColumn(id));

                  m_udfDialog.refreshDialog(m_udfSet, null, m_pageTank);
                  m_mappingView.refreshUdfDialog(m_udfSet, null, m_pageTank);

                  pack();
               }
            }

            m_emptyXmlBox.setSelected(m_mapper.allowsEmptyDocReturn());
            m_mappingTable.loadFromMapper(m_mapper);
            m_mappingTable.appendRow(128 - m_mappingTable.getRowCount());
            center();
            setVisible(true);
         }
         else
            throw new IllegalArgumentException("OSDataMapper expected!");
      }
      finally
      {
         /*
          * Fix for Memory Leak: need to always clean these up no matter how the
          * dialog is exited to be sure this dialog is garbage collected.
          */
         m_udfDialog.removeCancelListener(m_xmlEditor);
         m_udfDialog.removeOkListener(m_xmlEditor);
         m_udfDialog.removeCancelListener(m_backendEditor);
         m_udfDialog.removeOkListener(m_backendEditor);

         m_xmlDialog.removeOkListener(m_xmlEditor);
         m_xmlDialog.removeCancelListener(m_xmlEditor);
         m_backendDialog.removeOkListener(m_backendEditor);
         m_backendDialog.removeCancelListener(m_backendEditor);

         m_currentEditor = null;
         m_mappingView.setUdfSet(null);

         /*
          * Fix for Memory Leak: need to clean this up so ConditionalDialog and
          * ConditionalCellEditors are not leaked even if the
          * MapperPropertyDialog is not leaking
          */
         m_condDialog.freeResources();

      }
      
      return m_modified;
   }


   /**
    * Handles the cancel button action.
    */
   @Override
   public void onCancel()
   {
      m_udfSet.undoApplicationUdfRemove();
      dispose();
   }

   /**
    * First we create a validation mapper from the mapping table. This is
    * checked for multiple mapped XML fields. If there are multiple mapped
    * XML fields, an error message showing all multiple mapped XML fields is
    * popping up, informing the user that this is not allowed. After hitting
    * OK in the error dialog we return to this dialog so the user can correct
    * it.
    * Once we passed all validations we store all settings to the real mapper.
    */
   @SuppressWarnings("unchecked")
   @Override
   public void onOk()
   {
      try
      {
         OSDataMapper validationMapper = new OSDataMapper();
         m_mappingTable.saveToMapper(validationMapper);

         // first check for duplicate XML mappings with conditionals
         // applicable for query resources only
         if (m_bIsQuery.booleanValue())
         {
            boolean bDuplicateError = false;
            StringBuffer duplicateErrorMessage = new StringBuffer(
               getResources().getString("duplicateXmlMappings"));

            HashMap testForDuplicates = new HashMap();
            ArrayList duplicates = new ArrayList();

            for (int i = 0; i < validationMapper.size(); i++)
            {
               PSDataMapping mapping = (PSDataMapping) validationMapper.get(i);
               String xmlMapping = mapping.getXmlField();

               if (testForDuplicates.containsKey(xmlMapping))
               {
                  if (!duplicates.contains(xmlMapping))
                  {
                     ArrayList conditionals = (ArrayList) testForDuplicates
                        .get(xmlMapping);
                     if (conditionals.contains(mapping.getConditionals()))
                     {
                        duplicateErrorMessage.append("\n  - " + xmlMapping);
                        duplicates.add(xmlMapping);
                        bDuplicateError = true;
                     }
                     else
                     {
                        conditionals.add(mapping.getConditionals());
                        testForDuplicates.put(xmlMapping, conditionals);
                     }
                  }
               }
               else
               {
                  ArrayList conditionals = new ArrayList();
                  conditionals.add(mapping.getConditionals());

                  testForDuplicates.put(xmlMapping, conditionals);
               }
            }

            if (bDuplicateError)
            {
               // report error and return
               JOptionPane.showMessageDialog(
                  E2Designer.getApp().getMainFrame(), duplicateErrorMessage
                     .toString(), E2Designer.getResources().getString(
                     "OpErrorTitle"), JOptionPane.ERROR_MESSAGE);
               
               return;
            }
         }
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
         return;
      }

      m_modified = true;
      m_mappingTable.saveToMapper(m_mapper);
      m_mapper.setAllowEmptyDocReturn( m_emptyXmlBox.isSelected());

      dispose();
   }

   /**
    * @return <code>true</code> if we are editing a query pipe,
    *    <code>false</code> if editing an update pipe.
    */
   public boolean editingQuery()
   {
      return (m_bIsQuery.booleanValue());
   }

   /**
    * @return a reference to the table model used in the middle of the dialog.
    *
    * @see MapperTableModel
    */
   public TableModel getMapperTable()
   {
      return m_mappingTable;
   }

   /**
    * A convenience method to get the currently instantiated Mapper Dialog.
    */
   static public MapperPropertyDialog getCurrentEditor()
   {
      return (m_currentEditor);
   }

   /** The Mapper dialog data object. */
   OSDataMapper m_mapper = null;

   /** the row context menu. */
   JPopupMenu m_contextMenu = new JPopupMenu();

   /** The menu item that represents append. */
   JMenuItem m_append = new JMenuItem(getResources().getString("append"));

   /** The menu item that represents insert. */
   JMenuItem m_insert = new JMenuItem(getResources().getString("insert"));

   /** The menu item that represents delete. */
   JMenuItem m_delete = new JMenuItem(getResources().getString("delete"));

   /** The XML - backend mapping table model. */
   MapperTableModel m_mappingTable = new MapperTableModel();

   /** The XML - backend mapping table. */
   MapBrowserTable m_mappingView = new MapBrowserTable(m_mappingTable);

   /** The XML - backend mapping table scrollPane. */
   JScrollPane m_pane = null;

   /** Page tank icon. */
   ImageIcon m_pageIcon = new ImageIcon(getClass().getResource(
      E2Designer.getResources().getString("gif_PageDatatank_32")));

   /** The XML browser. */
   MapBrowser m_xmlBrowser = null;

   /** The backend tank icon. */
   ImageIcon m_backendIcon = new ImageIcon(getClass().getResource(
      E2Designer.getResources().getString("gif_BEDatatank_32")));

   /** The backend browser. */
   MapBrowser m_backendBrowser = null;

   /** Form icon. */
   ImageIcon m_formFieldIcon = new ImageIcon(getClass().getResource(
      E2Designer.getResources().getString("gif_FormFields")));

   /** UDF icon. */
   ImageIcon m_udfIcon = new ImageIcon(getClass().getResource(
      E2Designer.getResources().getString("gif_Udfs")));

   /** CGI icon. */
   ImageIcon m_cgiIcon = new ImageIcon(getClass().getResource(
      E2Designer.getResources().getString("gif_Cgis")));

   /** UserContext icon. */
   ImageIcon m_userContextIcon = new ImageIcon(getClass().getResource(
      E2Designer.getResources().getString("gif_UserContext")));

   /** The conditional cell editor. */
   ConditionalCellEditor m_condEditor = null;

   /** The conditional cell dialog. */
   ConditionalPropertyDialog m_condDialog = null;

   /** The UDF param assignment dialog. */
   FormulaPropertyDialog m_udfDialog = null;

   /** Value selector dialog for the backend tank. */
   ValueSelectorDialog m_backendDialog = null;

   /** Value selector dialog for the page tank. */
   ValueSelectorDialog m_xmlDialog = null;

   /** The backend mapper cell editor. */
   MapperCellEditor m_backendEditor = null;

   /** The xml mapper cell editor. */
   MapperCellEditor m_xmlEditor = null;

   /** The guess button. Guesses mappings. */
   UTFixedButton m_guessButton = new UTFixedButton(getResources().getString(
      "guess"));

   /** The add button. Adds new backend - xml pair. */
   UTFixedButton m_addButton = new UTFixedButton(getResources()
      .getString("add"));

   /** The clear all backend entries button. */
   UTFixedButton m_clearButton = new UTFixedButton(getResources().getString(
      "clear"));

   /** The row removal button. removes selected row. */
   UTFixedButton m_removeButton = new UTFixedButton(getResources().getString(
      "remove"));

   /** Allows empty XML returned for an empty result set. */
   JCheckBox m_emptyXmlBox = new JCheckBox(getResources().getString("emptyXml"));

   /**
    * The bottom panel that contains the command panel and empty XML check box.
    */
   JPanel m_pBottom = null;

   /** The standard command panel. */
   private UTStandardCommandPanel m_commandPanel = null;

   /** This flag will be set if any data within this dialog was modified. */
   private boolean m_modified = false;

   /** Keep track of what type of pipe we are editing. */
   private Boolean m_bIsQuery = Boolean.FALSE;

   /** A reference to self. */
   private static MapperPropertyDialog m_currentEditor = null;

   /** UDF sets wrapper object. */
   private PSUdfSet m_udfSet = null;

   /** The object store objects. */
   transient PSApplication m_app = null;

   // keep this around for the current edit session
   private transient OSBackendDatatank m_backendTank = null;

   // keep this around for the current edit session
   private transient OSPageDatatank m_pageTank = null;
}


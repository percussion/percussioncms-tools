/* *****************************************************************************
 *
 * [ ResultPagerPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSSortedColumn;
import com.percussion.util.PSCollection;
import com.percussion.guitools.PSEditTablePanel;
import com.percussion.guitools.PSJTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The result pager property dialog.
 */
public class ResultPagerPropertyDialog extends PSEditorDialog
{
   /**
    * Handle specific extra accelerators on the table
    */
   private class PagerKeyListener implements KeyListener
   {
      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
       */
      public void keyTyped(KeyEvent event)
      {
         // Ignore   
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
       */
      public void keyPressed(KeyEvent arg0)
      {
         // Ignore
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
       */
      public void keyReleased(KeyEvent event)
      {
         switch (event.getKeyCode())
         {
            case KeyEvent.VK_ESCAPE :
               m_editTable.removeEditor(); // Pop down the editor
               break;
            case KeyEvent.VK_INSERT :
               try
               {
                  m_editPanel.addRow();
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e.getMessage());
               }
               break;
            case KeyEvent.VK_DELETE :
               m_editPanel.removeRow();
               break;
            case KeyEvent.VK_ENTER :
               event.consume(); // Stop processing here
               break;
            default :
               // Do nothing
         }
      }
   }

   /**
    * Handle specific extra accelerators on the JComboBox
    */
   protected class ColumnEditorKeyListener implements KeyListener
   {
      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
       */
      public void keyTyped(KeyEvent event)
      {
         // Ignore   
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
       */
      public void keyPressed(KeyEvent arg0)
      {
         // Ignore
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
       */
      public void keyReleased(KeyEvent event)
      {
         switch (event.getKeyCode())
         {
            case KeyEvent.VK_ESCAPE :
               m_editTable.removeEditor(); // Pop down the editor
               break;
            case KeyEvent.VK_ENTER:
               m_editTable.removeEditor();
               event.consume();
               break;
            case KeyEvent.VK_DOWN:
               if ((event.getModifiers() & KeyEvent.ALT_MASK ) != 0)
               {
                  Component comp = event.getComponent();
                  
                  if (comp == m_columnList ||
                     (comp == m_editTable && 
                        m_editTable.getSelectedColumn() == 1))
                  {
                     m_columnList.showPopup();
                     m_columnList.requestFocus();
                  }
               }
               break;
            default :
               // Do nothing
         }
      }
   }

   /**
    * Construct the default result pager property dialog.
    *
    */
   public ResultPagerPropertyDialog()
   {
      super();
      initDialog();
   }

   public ResultPagerPropertyDialog(Window parent)
   {
      super(parent);
      initDialog();
   }

   private void setMnemonicOnLabel(UTSpinTextField txtFld, String resId)
   {  
      JLabel lbl = txtFld.getLabel();
      String lblStr = getResources().getString(resId);
      char mn = getResources().getString(resId + ".mn").charAt(0);
      lbl.setLabelFor(txtFld);
      lbl.setDisplayedMnemonic(mn);
      lbl.setDisplayedMnemonicIndex(lblStr.indexOf(mn));
   }
   /**
    * Create spin controls panel.
    *
    * @return the completed panel, with no border
   **/
   private JPanel createSpinControlsPanel()
   {
      JPanel panel = new JPanel(new GridLayout(0, 1));

      JPanel panelMaxRows = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      m_spinTextFieldMaxRows =
         new UTSpinTextField(
            getResources().getString("maxRowsPerPage"),
            new Integer(20),
            new Integer(-1),
         // 0 or -1 means unlimited
   new Integer(Integer.MAX_VALUE));
      m_spinTextFieldMaxRows.setDirectEditingAllowed(true);
      m_spinTextFieldMaxRows.setNumericDataOnly(false);
      panelMaxRows.add(m_spinTextFieldMaxRows);
      panel.add(panelMaxRows);
      setMnemonicOnLabel( m_spinTextFieldMaxRows,  "maxRowsPerPage");

      JPanel panelMaxPages = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      m_spinTextFieldMaxPages =
         new UTSpinTextField(
            getResources().getString("maxPages"),
            new Integer(5),
            new Integer(-1),
         // 0 or -1 means unlimited
   new Integer(Integer.MAX_VALUE));
      m_spinTextFieldMaxPages.setDirectEditingAllowed(true);
      m_spinTextFieldMaxPages.setNumericDataOnly(false);
      panelMaxPages.add(m_spinTextFieldMaxPages);
      panel.add(panelMaxPages);
      setMnemonicOnLabel( m_spinTextFieldMaxPages,  "maxPages");
      
      JPanel panelMaxPageLinks = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      m_spinTextFieldMaxPageLinks =
         new UTSpinTextField(
            getResources().getString("maxPageLinks"),
            new Integer(10),
            new Integer(-1),
            // 0 or -1 means unlimited
            new Integer(Integer.MAX_VALUE));
      
      setMnemonicOnLabel( m_spinTextFieldMaxPageLinks,  "maxPageLinks");
      
      m_spinTextFieldMaxPageLinks.setDirectEditingAllowed(true);
      m_spinTextFieldMaxPageLinks.setNumericDataOnly(false);
      panelMaxPageLinks.add(m_spinTextFieldMaxPageLinks);
      panel.add(panelMaxPageLinks);

      JPanel spinPanel = new JPanel(new BorderLayout());
      spinPanel.add(panel, BorderLayout.WEST);
      JPanel dummyPanel = new JPanel();
      spinPanel.add(dummyPanel, BorderLayout.CENTER);
      return spinPanel;
   }

   /**
    * Creates a panel that contains the list of sort keys and associated
    * controls
    *
    * @return the panel, with no border.
    **/
   private JPanel createSortKeyPanel()
   {
      // this panel contains the up/down button and its text
      JPanel jPanelMove = new JPanel();
      jPanelMove.setLayout(new BoxLayout(jPanelMove, BoxLayout.Y_AXIS));
      jPanelMove.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

      m_moveUpButton =
         new JButton(
            new ImageIcon(
               getClass().getResource(
                  getResources().getString("gif_upButton"))));
      m_moveUpButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_editPanel.moveSelectedUp();
         }
      });
      jPanelMove.add(m_moveUpButton);

      JLabel jLabelMove = new JLabel();
      jLabelMove.setText(getResources().getString("move"));
      jPanelMove.add(jLabelMove);

      m_moveDownButton =
         new JButton(
            new ImageIcon(
               getClass().getResource(
                  getResources().getString("gif_downButton"))));
      m_moveDownButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_editPanel.moveSelectedDown();
         }
      });
      jPanelMove.add(m_moveDownButton);

      /* create editable list box, which contains PSSortedColumn objects.
         The columns will be added in onEdit. */

      // we don't pass in any dropdown list items now, they will be added later
      // Pass in current data model to allow JTable to extract information about 
      // column count and such

      // create column headers
      m_header[ResultPagerTableModel.COLUMN_BACKEND_COLUMN_NAME] =
         getResources().getString("header.key");
      m_header[ResultPagerTableModel.COLUMN_SORT_ORDER] =
         getResources().getString("header.order");
      m_header[ResultPagerTableModel.COLUMN_ROW_SELECT_TAB] = "*";
      // Create initial panel with a dummy table model. This will never
      // be shown in the GUI and acts only as a placeholder. See onEdit for
      // the real model
      m_editPanel =
         new PSEditTablePanel(
            new ResultPagerTableModel(m_header), 
               ResultPagerTableModel.COLUMN_BACKEND_COLUMN_NAME);
      m_editTable = m_editPanel.getTable();
      m_editTable.addSelectionListener(new ListSelectionListener()
      {
         // used to update enabled state of the move buttons
         public void valueChanged(ListSelectionEvent e)
         {
            onSortKeyListSelectionChanged();
         }
      });
      m_editTable.setPreferredSize(new Dimension(325, 120));

      m_editPanel.setPreferredSize(new Dimension(360, 200));
      m_editPanel.addKeyListener(new PagerKeyListener());

      // Setup editors for table              
      // set allowable values for column editor
      KeyListener coledit = new ColumnEditorKeyListener();
      m_columnList = new JComboBox();
      m_columnList.addKeyListener(coledit);
      m_editTable.addKeyListener(coledit);

      // Add arrows panel defined earlier to editPanel
      m_editPanel.add(jPanelMove, BorderLayout.EAST);
      return m_editPanel;
   }

   /**
    * Initialize the dialogs GUI elements with its data.
    *
    */
   private void initDialog()
   {
      setResizable(true);

      JPanel main = new JPanel(new BorderLayout());
      main.setBorder(new EmptyBorder(5, 5, 5, 5));

      m_commandPanel =
         new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
            // implement onOk action
         public void onOk()
         {
            ResultPagerPropertyDialog.this.onOk();
         }
      };
      m_commandPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 0));
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(m_commandPanel, BorderLayout.EAST);
      
      JPanel panel1 = new JPanel(new BorderLayout());

      JPanel spinPanel = createSpinControlsPanel();
      spinPanel.setBorder(
         BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            getResources().getString("PagerGroupTitle")));
      panel1.add(spinPanel);
      //panel1.add(m_commandPanel, BorderLayout.EAST);
      main.add(panel1, BorderLayout.NORTH);

      JPanel sortPanel = createSortKeyPanel();
      sortPanel.setBorder(
         BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            getResources().getString("SortKeysGroupTitle")));
      main.add(sortPanel, BorderLayout.CENTER);
      main.add(cmdPanel, BorderLayout.SOUTH);

      // set the default button
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(main);
      pack();

      // initialize validation constraints
      m_validatedComponents[0] = m_spinTextFieldMaxRows;
      m_validatedComponents[1] = m_spinTextFieldMaxPages;
      m_validatedComponents[2] = m_spinTextFieldMaxPageLinks;

      m_validationConstraints[0] = m_spinTextFieldMaxRows;
      m_validationConstraints[1] = m_spinTextFieldMaxPages;
      m_validationConstraints[2] = m_spinTextFieldMaxPageLinks;
      setValidationFramework(m_validatedComponents, m_validationConstraints);

      setSortKeyButtonState();
   }

   public boolean onEdit(UIFigure figure, final Object data)
   {
      try
      {
         if (figure.getData() instanceof OSResultPager)
         {
            m_resultPager = (OSResultPager) figure.getData();

            // initialize the data
            int iMaxRows = m_resultPager.getMaxRowsPerPage();
            int iMaxPages = m_resultPager.getMaxPages();
            int iMaxPageLinks = m_resultPager.getMaxPageLinks();
            m_spinTextFieldMaxRows.setValue(iMaxRows);
            m_spinTextFieldMaxPages.setValue(iMaxPages);
            m_spinTextFieldMaxPageLinks.setValue(iMaxPageLinks);

            UTPipeNavigator navigator = new UTPipeNavigator();
            UIFigure tankFigure = navigator.getBackendTank(figure);
            if (null != tankFigure)
            {
               OSBackendDatatank tank =
                  (OSBackendDatatank) tankFigure.getData();

               // set list box entries                    
               Enumeration cols = tank.getBackendColumns();
               PSBackEndColumn first = null;
               PSCollection knownColumns = 
                  new PSCollection(PSBackEndColumn.class);
               
               for (int i = 0; cols.hasMoreElements(); ++i)
               {
                  PSBackEndColumn column = (PSBackEndColumn) cols.nextElement();
                  if (first == null)
                  {
                     first = column;
                  }
                  m_columnList.insertItemAt(
                     ResultPagerTableModel.formatColumnName(column), i);
                     
                  knownColumns.add(column);
               }

               // set list entries
               PSCollection sortedKeys = m_resultPager.getSortedColumns();
               if (sortedKeys == null)
               {
                  sortedKeys = new PSCollection(PSSortedColumn.class);
               }
               
               ResultPagerTableModel model = 
                  new ResultPagerTableModel(sortedKeys, 
                     m_header, knownColumns);

               m_editTable.setModel(model);
               m_editTable.setColumnEditor(
                  ResultPagerTableModel.COLUMN_BACKEND_COLUMN_NAME,
                  m_columnList);
               m_editTable.setColumnWidth(
                  ResultPagerTableModel.COLUMN_SORT_ORDER,
                  70);
               m_editTable.setColumnWidth(ResultPagerTableModel.COLUMN_ROW_SELECT_TAB, 20);
            }

            this.center();
            this.setVisible(true);
            return true;
         }
         else
            throw new IllegalArgumentException("OSResultPager expected!");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return false;
   }

   /**
    * Handles ok button action. Overrides PSDialog onOk() method implementation.
    * Validates the entries in the dialog. If they all validate correctly,
    * data is transferred to the data object passed in the onEdit method,
    * otherwise, the dialog remains visible, allowing the user to correct her
    * mistakes (a message is displayed indicating the problem).
   **/
   public void onOk()
   {

      if (activateValidation())
      {
         try
         {
            // Get the sorted keys from the model
            ResultPagerTableModel model =
               (ResultPagerTableModel) m_editTable.getModel();

            PSCollection sortedKeys = model.getRows();

            // Validate and remove any empty keys from collection that
            // may be present due to an add that was not completed
            for (Iterator iter = sortedKeys.iterator(); iter.hasNext();)
            {
               PSSortedColumn col = (PSSortedColumn) iter.next();
               String cname = col.getColumn();
               if (cname == null || cname.trim().length() == 0)
               {
                  sortedKeys.remove(col);
               }
            }

            m_resultPager.setSortedColumns(sortedKeys);
            m_resultPager.setMaxRowsPerPage(
               m_spinTextFieldMaxRows.getData().intValue());
            m_resultPager.setMaxPages(
               m_spinTextFieldMaxPages.getData().intValue());
            m_resultPager.setMaxPageLinks(
               m_spinTextFieldMaxPageLinks.getData().intValue());

            dispose();
         }
         catch (ClassCastException e)
         {
            PSDlgUtil.showError(e);
         }
      }
   }

   /**
    * Handler for list selection changed events on the sort key list.
   **/
   private void onSortKeyListSelectionChanged()
   {
      setSortKeyButtonState();
   }

   /**
    * Sets the up/down buttons for the Sort key list to enabled/disabled
    * depending on the currently selected item in the list.
   **/
   private void setSortKeyButtonState()
   {
      int itemIndex = m_editTable.getSelectionModel().getMinSelectionIndex();
      int items = m_editTable.getRowCount();

      m_moveDownButton.setEnabled(itemIndex >= 0 && itemIndex < items - 1);
      m_moveUpButton.setEnabled(itemIndex < items && itemIndex > 0);
   }

   /**
   * Test the dialog.
   *
   */
   /**  public static void main(String[] args)
     {
      try
      {
            final JFrame frame = new JFrame("Test Dialog");
            UIManager.setLookAndFeel(
               new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
            frame.addWindowListener(new BasicWindowMonitor());
   
            JButton startButton = new JButton("Open Dialog");
            frame.getContentPane().add(startButton);
            startButton.addActionListener(new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
           JDialog dialog = new ResultPagerPropertyDialog();
                  dialog.setVisible(true);
               }
            });
   
            frame.setSize(100, 100);
            frame.setVisible(true);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
     }
   **/

   /**
   * Added for testing reasons only.
   *
   */
   private ResourceBundle m_res = null;
   protected ResourceBundle getResources()
   {
      try
      {
         if (m_res == null)
            m_res =
               ResourceBundle.getBundle(
                  getClass().getName() + "Resources",
                  Locale.getDefault());
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }

      return m_res;
   }

   /**
    * The result pager object
    */
   private OSResultPager m_resultPager = null;

   /**
    * The spin control for the max rows per page
    */
   UTSpinTextField m_spinTextFieldMaxRows = null;

   /**
    * The spin control for the max pages
    */
   UTSpinTextField m_spinTextFieldMaxPages = null;

   /**
    * The spin control for the max page links
    */
   UTSpinTextField m_spinTextFieldMaxPageLinks = null;

   /**
   * the standard command panel
   */
   private UTStandardCommandPanel m_commandPanel = null;

   /**
    * statics to name the columns in the table
    */
   private static final String SORT_ORDER = "SortingOrder";
   private static final String SORT_COL = "SortingColumn";
   
   /**
    * Some default values to use for the table model. Will be
    * overridden in the onEdit method, so these should never really
    * be used.
    */
   private static final String DEFAULT_TABLE = "CONTENTSTATUS";
   private static final String DEFAULT_COLUMN = "CONTENTID";

   /**
   * the validation framework variables
   */
   private static final int NUM_COMPONENTS_VALIDATED = 3;
   private final Component m_validatedComponents[] =
      new Component[NUM_COMPONENTS_VALIDATED];
   private final ValidationConstraint m_validationConstraints[] =
      new ValidationConstraint[NUM_COMPONENTS_VALIDATED];
   /**
    * Button used to move a hilighted entry down in the sorted keys list.
    **/
   private JButton m_moveDownButton = null;
   /**
    * Button used to move a hilighted entry up in the sorted keys list.
    **/
   private JButton m_moveUpButton = null;
   /**
    * The control that contains the list of sorted columns. This is
    * initialized in createSortKeyPanel (called by the constructor)
    * and is never updated.
    **/
   private PSEditTablePanel m_editPanel = null;
   /**
    * The table contained within the m_editPanel.
    * This is initialized in createSortKeyPanel (called by the constructor)
    * and is never updated.    
    */
   private PSJTable m_editTable = null;

   /**
    * The list of backend columns to display. This is initialized in
    * createSortKeyPanel (called by the constructor), 
    * but is recreated on every page display. It is
    * never <code>null</code> after initialization.
    */
   private JComboBox m_columnList = null;

   /**
    * Table headers 
    */
   private String m_header[] = new String[3];

}

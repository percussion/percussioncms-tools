/******************************************************************************
 *
 * [ DisplayFormatColumnPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.PSDFColumns;
import com.percussion.cms.objectstore.PSDbComponentList;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.client.PSLightWeightField;
import com.percussion.search.ui.PSFieldSelectionEditorDialog;
import com.percussion.guitools.PSDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;


/**
 * Panel displaying 'Categories' and 'Columns' table for a given display format.
 * The 'Categories' table is disabled if 'Folders' is checked in {@link
 * DisplayFormatColumnTable}. Both the tables allow single row selection
 */
public class DisplayFormatColumnPanel extends ParentPanel
  implements ActionListener
{
   /**
    * Constructs the display format column panel.
    *
    * @param parent the container of this panel.  Must not be <code>null</code>.
    */
   public DisplayFormatColumnPanel(DisplayFormatTabbedPanel parent)
   {
      if(parent == null)
         throw new IllegalArgumentException("parent must not be null");

      m_parentPanel = parent;
      m_supportWidth = FeatureSet.isFeatureSupported(FeatureSet.FEATURE_FTS);

      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);
      add(createCategoryTablePanel());

      //create inter table up/down button.
      JPanel pane = new JPanel();
      pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
      m_catToCol = new JButton(ms_res.getString("btn.column"),
         new ImageIcon(getClass().getResource("images/down.gif")));
      m_catToCol.addActionListener(new MoveRowListener(false));
      m_catToCol.setMnemonic(ms_res.getString("btn.column.mn").charAt(0));
      
      m_colToCat = new JButton(ms_res.getString("btn.category"),
         new ImageIcon(getClass().getResource("images/up.gif")));
      m_colToCat.addActionListener(new MoveRowListener(true));
      m_colToCat.setMnemonic(ms_res.getString("btn.category.mn").charAt(0));
      pane.add(m_catToCol);
      pane.add(Box.createHorizontalStrut(10));
      pane.add(m_colToCat);

      add(Box.createVerticalStrut(10));
      add(pane);
      add(createColumnTablePanel());
      setButtonState();
   }

   /**
    * Create a panel to hold to sort column and direction combo boxes
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createSortPanel()
   {
      // create the panel 
      JPanel sortPanel = new JPanel();
      sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.X_AXIS));
      sortPanel.setBorder(PSDialog.createGroupBorder( ms_res.getString(
         "sortCols.border.title")));
      
      // add the combo boxes
      m_sortColCombo = new JComboBox();
      sortPanel.add(m_sortColCombo);
      sortPanel.add(Box.createHorizontalStrut(15));
      m_sortDirCombo = new JComboBox(prepareComboData());
      sortPanel.add(m_sortDirCombo);

      // add listener to model to update combo box contents
      return sortPanel;
   }

   public void enableCategoriesTable(boolean enable)
   {
      //stopEditing(m_categoriesTable);
      m_categoriesTable.setEnabled(enable);
      if (!enable)
      {
         DefaultTableModel model =
            (DefaultTableModel)m_categoriesTable.getModel();
         Vector vec = model.getDataVector();
         move(false, vec);
         model.setNumRows(0);
      }
   }

   /**
    * Creates panel containing 'Categories' table and buttons for shifting rows
    * up and down.
    *
    * @return, never <code>null</code>.
    */
   private JPanel createCategoryTablePanel()
   {

      JPanel catPanel = new JPanel();
      catPanel.setLayout(new BoxLayout(catPanel, BoxLayout.X_AXIS));
 
      Border b = PSDialog.createGroupBorder(
         ms_res.getString("category.table.border"));

      JPanel userParamPane = new JPanel();
      userParamPane.setLayout(new BorderLayout());
      DefaultTableModel model = new DefaultTableModel(
         new Object[]
         {
             ms_res.getString("col.label"), ms_res.getString("col.field")
         }, 0);
      JScrollPane jsp = null;
      m_categoriesTable = new JTable(model)
      {
         public boolean isCellEditable(int row, int col)
         {
          return col == 2;
         }
      };
      ListSelectionModel rowSM = m_categoriesTable.getSelectionModel();
      rowSM.addListSelectionListener(new RowSelectionListener(0));
      m_categoriesTable.setSelectionMode(
           ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

      jsp = new JScrollPane(m_categoriesTable);
      jsp.getViewport().setBackground(m_categoriesTable.getBackground());
      userParamPane.add(jsp, BorderLayout.CENTER);

      JToolBar tb = new JToolBar();

      //to be replaced by 'up' graphics
      m_categoryUp = new JButton(
         new ImageIcon(getClass().getResource("images/up.gif")));
      m_categoryUp.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            move(m_categoriesTable, true);
            setButtonState(0);
         }
      });
      m_categoryDwn = new JButton(
        new ImageIcon(getClass().getResource("images/down.gif")));
      m_categoryDwn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            move(m_categoriesTable, false);
            setButtonState(0);
         }
      });

      // remove button:
      m_categoryDel = new JButton(
         new ImageIcon(getClass().getResource("images/delete.gif")));
      m_categoryDel.setPreferredSize(m_categoryDwn.getPreferredSize());
      m_categoryDel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            removeSelectedRow(m_categoriesTable);
            radioButtonsManager(false);
            setButtonState();
         }
      });

      tb.add(m_categoryUp);
      tb.add(Box.createRigidArea(new Dimension(0, 10)));
      tb.add(m_categoryDwn);
      tb.add(Box.createRigidArea(new Dimension(0, 10)));
      tb.add(m_categoryDel);
      tb.setOrientation(JToolBar.VERTICAL);

      catPanel.add(userParamPane);
      catPanel.add(Box.createHorizontalStrut(15));
      catPanel.add(tb);
      catPanel.setBorder(b);
      setButtonState(0);

      return catPanel;
   }

   /**
    * Creates panel containing 'Columns' table and buttons for shifting rows
    * up and down.
    *
    * @return, never <code>null</code>.
    */
   private JPanel createColumnTablePanel()
   {
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));

      JPanel catPanel = new JPanel();
      catPanel.setLayout(new BoxLayout(catPanel, BoxLayout.X_AXIS));

      JToolBar tb = new JToolBar();

      //up button
      m_columnUp = new JButton(
         new ImageIcon(getClass().getResource("images/up.gif")));
      m_columnUp.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            move(m_columnsTable, true);
            setButtonState(1);
         }
      });

      //down button:
      m_columnDwn = new JButton(
         new ImageIcon(getClass().getResource("images/down.gif")));
      m_columnDwn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            move(m_columnsTable, false);
            setButtonState(1);
         }
      });

      // remove button:
      m_columnDel = new JButton(
         new ImageIcon(getClass().getResource("images/delete.gif")));
      m_columnDel.setPreferredSize(m_columnDwn.getPreferredSize());
      m_columnDel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            removeSelectedRow(m_columnsTable);
            radioButtonsManager(false);
            setButtonState();
         }
      });

      tb.add(m_columnUp);
      m_columnUp.setActionCommand(COLUP);
      tb.add(Box.createRigidArea(new Dimension(0, 10)));
      tb.add(m_columnDwn);
      tb.add(Box.createRigidArea(new Dimension(0, 10)));
      tb.add(m_columnDel);

      tb.setOrientation(JToolBar.VERTICAL);
      Border b = PSDialog.createGroupBorder( ms_res.getString(
         "column.table.border"));
      JPanel userParamPane = new JPanel();
      userParamPane.setLayout(new BorderLayout());

      Object[] cols = new Object[(m_supportWidth) ? 3 : 2];
      cols[LABEL_COLUMN] = ms_res.getString("col.label");
      cols[FIELD_COLUMN] = ms_res.getString("col.field");
      String widthCol = ms_res.getString("col.width");
      if (m_supportWidth)
         cols[WIDTH_COLUMN] = widthCol; 
      DefaultTableModel model = new DefaultTableModel(cols, 0);
      JScrollPane jsp = null;
      m_columnsTable = new JTable(model)
      {
         public boolean isCellEditable(int row, int col)
         {
            return col == WIDTH_COLUMN;
         }
      };

      if (m_supportWidth)
      {
         TableColumn col = m_columnsTable.getColumn(widthCol);
         col.setPreferredWidth(15); 
         col.setCellRenderer(new DefaultTableCellRenderer());
         final JTextField widthField = new JTextField();
         col.setCellEditor(new DefaultCellEditor(widthField)
         {
            public boolean stopCellEditing()
            {
               String strWidth = widthField.getText();
               if (strWidth.trim().length() > 0)
               {
                  int width = -1;
                  try
                  {
                     width = Integer.parseInt(strWidth);
                  }
                  catch (NumberFormatException e)
                  {
                     // non-numeric value
                  }
                  if (width <= 0)
                  {
                     PSDlgUtil.showErrorDialog(
                        ms_res.getString("col.width.invalid.msg"),
                        ms_res.getString("col.width.invalid.title"));
                     return false;
                  }
               }

               return super.stopCellEditing();
            }
         });
      }
      
      ListSelectionModel rowSM =  m_columnsTable.getSelectionModel();
      rowSM.addListSelectionListener(new RowSelectionListener(1));
      m_columnsTable.setSelectionMode(
         ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      jsp = new JScrollPane(m_columnsTable);
      jsp.getViewport().setBackground(m_columnsTable.getBackground());
      userParamPane.add(jsp, BorderLayout.CENTER);
      catPanel.add(userParamPane);
      catPanel.add(Box.createHorizontalStrut(15));
      catPanel.add(tb);
      catPanel.setBorder(b);
      setButtonState(1);

      m_columnCustomize = new JButton(ms_res.getString("btn.customize"));
      m_columnCustomize.setMnemonic(ms_res.getString("btn.customize.mn").charAt(0));
      m_columnCustomize.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            PSDialog dialog =
               (PSDialog)SwingUtilities.getWindowAncestor(
               DisplayFormatColumnPanel.this);

            // save the order of the tables:
            //TODO: This is called so that the object is updated with the
            // correct order and the editor dialog will have the correct order.
            // However save should not be called internally by the panel.
            // When there is time this should be refactored.
            persistDataToObject();

            PSFieldSelectionEditorDialog columnFsDlg =
               new PSFieldSelectionEditorDialog(dialog, m_dspFormat,
                  m_parentPanel.getCataloger());

            columnFsDlg.setVisible(true);

            if (columnFsDlg.isOk())
            {
               // if not valid set both flags to false and let
               // the refreshTables turn the appropriate one on:
               if(!m_dspFormat.isValidForRelatedContent())
               {
                  m_hasContentTypeIdColumn = false;
                  m_hasVariantNameColumn = false;
               }
               refreshTables();
               radioButtonsManager(true);
            }
         }
      });
      mainPane.add(catPanel);      

      // add sys_title note
      Font font = new Font("Arial", Font.PLAIN, 12);
      JTextArea titleMsg = new JTextArea(ms_res.getString("note") + " " + 
         ms_res.getString("systitle.del.msg"));
      titleMsg.setFont(font);
      titleMsg.setEditable(false);
      titleMsg.setBackground(getBackground());
      titleMsg.setLineWrap(true);
      titleMsg.setWrapStyleWord(true);
      titleMsg.setBorder(new EmptyBorder(5, 10, 5, 10));
      mainPane.add(Box.createVerticalStrut(5));
      mainPane.add(titleMsg);
      
      // add sort column panel
      mainPane.add(Box.createVerticalStrut(10));
      mainPane.add(createSortPanel());      
      
      JPanel buttonBoxPanel = new JPanel();
      buttonBoxPanel.setLayout(new BoxLayout(buttonBoxPanel, BoxLayout.X_AXIS));
      buttonBoxPanel.add(m_columnCustomize);

      JPanel chkBoxPanel = new JPanel();
      chkBoxPanel.setLayout(new BoxLayout(chkBoxPanel, BoxLayout.Y_AXIS));
      chkBoxPanel.setBorder(PSDialog.createGroupBorder(ms_res.getString(
         "checkboxpanel.border.msg")));

      m_RelatedContentSearchRadio = new JRadioButton(ms_res.getString(
         "radiobtn.label.relatedcontentsearch"));
      m_RelatedContentSearchRadio.setMnemonic(
            ms_res.getString("radiobtn.label.relatedcontentsearch.mn").charAt(0));
      m_FoldersRadio  = new JRadioButton(ms_res.getString(
         "radiobtn.label.folders"));
      m_FoldersRadio.setMnemonic(
            ms_res.getString("radiobtn.label.folders.mn").charAt(0));
      m_NoneRadio  = new JRadioButton(ms_res.getString(
         "radiobtn.label.none"));
      m_NoneRadio.setMnemonic(
            ms_res.getString("radiobtn.label.none.mn").charAt(0));


      m_RelatedContentSearchRadio.setSelected(false);
      m_FoldersRadio.setSelected(false);
      m_NoneRadio.setSelected(true); // selected be default

      ButtonGroup bg = new ButtonGroup();
      bg.add(m_RelatedContentSearchRadio);
      bg.add(m_FoldersRadio);
      bg.add(m_NoneRadio);

      chkBoxPanel.add(m_FoldersRadio);
      chkBoxPanel.add(m_RelatedContentSearchRadio);
      chkBoxPanel.add(m_NoneRadio);

      m_FoldersRadio.addActionListener(this);
      m_RelatedContentSearchRadio.addActionListener(this);
      m_NoneRadio.addActionListener(this);

      buttonBoxPanel.add(Box.createHorizontalStrut(10));
      buttonBoxPanel.add(chkBoxPanel);

      mainPane.add(Box.createVerticalStrut(10));
      mainPane.add(buttonBoxPanel);
      return mainPane;
   }


   // see interface for description
   public void actionPerformed(ActionEvent e)
   {
      ValidateRadioButtons(false);

      if (m_FoldersRadio.isSelected())
         m_selectedButton = FOLDERS_BUTTON;
      else if (m_RelatedContentSearchRadio.isSelected())
         m_selectedButton = RELATED_CONTENT_SEARCH_BUTTON;
      else
         m_selectedButton = NONE_BUTTON;
   }

   /**
    * This method is called by the radiobuttons to validate the following:
    * If the folder is selected:
    *
    * 1. The category table must be empty, if not, an error dialog will be
    * presented, and the folder checkbox will be set to unchecked.
    *
    * 2. If selected column to category button will need to be disabled, this
    * will call setButtonState do disable that button.
    *
    * If the related content search checkbox is selected, the following rules
    * apply:
    *
    * 1. The sys_contentid field will be added to the columns table if
    *    it does not already exist.
    *
    * 2. This method will create a sys_variant display column and add it to
    *    the columns.  It should be noted that this is the only place where
    *    this field is created in the workbench.
    *
    * 3. If either the sys_contentid or hte sys_variant field get deleted
    *    this will be called to set the selected to <code>false</code>.
    *
    * This method faciliates those rules.
    *
    * @param fromCustomized (hack) If called when returning from the
    *    customize columns dialog, then this should be <code>true</code>,
    *    otherwise it should be <code>false</code>. If <code>false</code>,
    *    a message is displayed to the user if the checkbox has been unchecked
    *    before the associated columns are removed.
    */
   private void ValidateRadioButtons(boolean fromCustomized)
   {
      //suppress eclipse warning
      if (fromCustomized);
      
      // remember previous selected radio button
      int prevSelectedButton = m_selectedButton;
      
      // process any pending deletes since we may be adding columns and if we've
      // deleted a column and then re-add it here without updating the object
      // first, you end up doing an insert without a doing a delete and that
      // ends up giving us a primary key violation on save.
      persistDataToObject();
      
      // validate the folder is selected
      if (m_FoldersRadio.isSelected())
      {
         if (m_categoriesTable.getRowCount() != 0)
         {
            int choice =
               PSDlgUtil.showConfirmDialog(
                  ms_res.getString("folder.msg.hascategories"),
                  ms_res.getString("folder.title.hascategories"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION)
            {
               m_categoriesTable.selectAll();
               moveTableFields(false);
            }
            else // set back to what it was
               {
               
               if (prevSelectedButton == NONE_BUTTON 
                   || prevSelectedButton == FOLDERS_BUTTON)
               {
                  m_NoneRadio.setSelected(true);
               }
               else if (prevSelectedButton == RELATED_CONTENT_SEARCH_BUTTON)
               {
                  m_RelatedContentSearchRadio.setSelected(true);
               }
            }
         }
      }
      
      if (m_RelatedContentSearchRadio.isSelected())
      {
         // does it have variant name column:
         if (!hasColumn(PSDisplayFormat.COL_VARIANTID))
         {
            PSDlgUtil.showInfoDialog(
                  ms_res.getString("relatedcontent.msg.addvariantcol"),
                  ms_res.getString("relatedcontent.title.addvariantcol"));
            m_variantColumn = createDispCol(PSDisplayFormat.COL_VARIANTID,
                  "Variant");
            addToColumns(m_variantColumn);
         }
         if (!m_hasContentTypeIdColumn)
         {
            PSDlgUtil.showInfoDialog(
                  ms_res.getString("relatedcontent.msg.addctypecol"),
                  ms_res.getString("relatedcontent.title.addctypecol"));
            m_contentTypeIdColumn = createDispCol(
                  PSDisplayFormat.COL_CONTENTTYPEID, null);
            addToColumns(m_contentTypeIdColumn);
         }
      }
      else
      {
         // if there is a variant name column
         // this means variant name was selected:
         // hack alert, last minute hack: if fromCustomized dialog
         // don't prompt and just process:
         
         if (m_hasVariantNameColumn && m_hasContentTypeIdColumn)
         {
            int choice =
               PSDlgUtil.showConfirmDialog(
                  ms_res.getString("relatedcontent.msg.removecol"),
                  ms_res.getString("relatedcontent.title.removecol"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION)
            {
               removeRowByFieldValue(PSDisplayFormat.COL_CONTENTTYPEID);
               removeFromColumns(m_contentTypeIdColumn);
            }
         }
         
         removeRowByFieldValue(PSDisplayFormat.COL_VARIANTID);
         removeFromColumns(m_variantColumn);
         m_hasVariantNameColumn = false;
      }

      setButtonState();
   }


   // see interface for desc
   public boolean validateData()
   {
      // validate that we have at least one column
      if(m_columnsTable.getRowCount() == 0)
      {
         PSDlgUtil.showErrorDialog(
            ms_res.getString("relatedcontent.msg.missingcol"),
            ms_res.getString("relatedcontent.title.missingcol"));

         m_columnsTable.requestFocus();
         return false;
      }

      return true;
   }

   /**
    * There should not be a variantid without a content typeid and the
    * related content checkbox should not be selected without a variantid.
    * These conditions can be set from other dialogs or buttons, so we
    * need a common place to check to be sure we maintain the correct state.
    */
   private void radioButtonsManager(boolean fromCustomized)
   {
      ValidateRadioButtons(fromCustomized);
   }


   /**
    * A convenience method searches through both tables for the specified
    * internalName. If it finds it, it calls
    * {@link #locateInternalNameRowIndex(JTable, String)
    * locateInternalNameRowIndex} with the appropriate table.
    *
    * @param internalName for which to search. Assumed not <code>null</code>
    *    or empty.
    */
   private void removeRowByFieldValue(String internalName)
   {
      JTable[] tables = {m_columnsTable, m_categoriesTable};
      boolean found = false;
      for (int i=0; i < tables.length && !found; i++)
      {
         int varIndex = locateInternalNameRowIndex(tables[i], internalName);
         if(varIndex > -1)
         {
            // not concerned if user has anything selected:
            tables[i].setRowSelectionInterval(varIndex, varIndex);
            removeSelectedRow(tables[i]);
            tables[i].clearSelection();
         }
      }
   }
   
   /**
    * Moves a row from one table to another based on field name.  If the field
    * is not found in the source table, nothing happens.
    * 
    * @param internalName The internal name of the field, assumed not 
    * <code>null</code> or empty.
    * @param isUp <code>true</code> to move from columns to categories, 
    * <code>false</code> to move from categories to columns.
    * @param append <code>true</code> to append the row to the target table, 
    * <code>false</code> to insert it as the first row.
    * @return <code>true</code> if the field was found and moved, 
    * <code>false</code> if not.
    */
   private boolean moveRowByFieldValue(String internalName, boolean isUp, 
      boolean append)
   {
      boolean moved = false;
      
      JTable source = isUp ? m_columnsTable : m_categoriesTable;
      JTable dest = isUp ? m_categoriesTable : m_columnsTable;
      
      int row = locateInternalNameRowIndex(source, internalName);
      if (row != -1)
      {
         DefaultTableModel srcDtm =((DefaultTableModel)source.getModel());
         DefaultTableModel tgtDtm =((DefaultTableModel)dest.getModel());
         Vector rowData = (Vector)srcDtm.getDataVector().get(row);
         srcDtm.removeRow(row);
         if (append)
            tgtDtm.addRow(rowData);
         else
            tgtDtm.insertRow(0, rowData);
            
         moved = true;         
         updateSortCombos();         
      }
      
      return moved;
   }


   /**
    * Checks the categorized and column tables for the presence of the
    * specified field name.
    *
    * @param internalName Assumed not <code>null</code>.
    *
    * @return <code>true</code> if either table contains a row whose
    *    FIELD_COLUMN column contains a value that matches fieldName using a
    *    case insensitive compare.
    */
   private boolean hasColumn(String internalName)
   {
      if (locateInternalNameRowIndex(m_categoriesTable, internalName) >= 0)
         return true;
      if (locateInternalNameRowIndex(m_columnsTable, internalName) >= 0)
         return true;

      return false;
   }


   /**
    * Returns the row index at which the specified internal name is in the
    * specified table.
    *
    * @param tableToSearch the table in which to search. Assumed not
    *    <code>null</code>.
    *
    * @param internalName assumed not <code>null</code> or empty.
    *
    * @return <code>-1</code> if internalName cannot be found in specified
    *    table, otherwise it's the index of the row whose FIELD_COLUMN column
    *    contains a matching internal name.
    */
   private int locateInternalNameRowIndex(JTable tableToSearch,
      String internalName)
   {
      int index = -1;
      DefaultTableModel dtm = ((DefaultTableModel)tableToSearch.getModel());
      for(int i=0; i < dtm.getRowCount(); i++)
      {
         if(((String)dtm.getValueAt(i, FIELD_COLUMN)).equalsIgnoreCase(
            internalName))
         {
            index = i;
            break;
         }
      }

      return index;
   }

   /**
    * Calls {@link #addToColumns(PSDisplayColumn, boolean) 
    * addToColumns(dispCol, true)} 
    */
   private void addToColumns(PSDisplayColumn dispCol)
   {
      addToColumns(dispCol, true);
   }
   
   /**
    * Adds a display column to the component list, always appends to the end.
    * Created for the handling of the <code>VARIANT_NAME_FIELD</code>, and
    * the related content search checkboxes.
    *
    * @param displayCol assumed not <code>null</code>
    * @param append <code>true</code> to append the column to the end of the 
    * list, <code>false</code> to insert as the first column.
    */
   private void addToColumns(PSDisplayColumn dispCol, boolean append)
   {
      PSDbComponentList items = m_dspFormat.getColumnContainer();
      if (append)
      {      
         items.add(dispCol);
         setTableData(prepTableData(dispCol), false);
      }
      else
      {
         items.add(0, dispCol);
         setTableData(prepTableData(dispCol), false, false);
      }
      

      // remove from delete list:
      m_deleteList.remove(dispCol.getSource());
   }

   /**
    * Removes a display column from the component list.
    * Created for the handling of the <code>VARIANT_NAME_FIELD</code>, and
    * the related content search checkboxes, since these are added separately
    * from the customize dialog.
    *
    * @param displayCol assumed not <code>null</code>
    */
   private void removeFromColumns(PSDisplayColumn dispCol)
   {
      PSDbComponentList items = m_dspFormat.getColumnContainer();
      items.remove(dispCol);
   }

   /**
    * This refreshes the category and the column tables' data.
    */
   private void refreshTables()
   {
      // first get the current category values:
      List categoryData =
         ((DefaultTableModel)m_categoriesTable.getModel()).getDataVector();
      Iterator it = categoryData.iterator();
      List<String> categoryFields = new ArrayList<String>();

      // make list of field names in the category table:
      while(it.hasNext())
      {
         categoryFields.add((String)((Vector)it.next()).get(FIELD_COLUMN));
      }

      // get current sort selections
      DisplayColumn sortCol = (DisplayColumn)m_sortColCombo.getSelectedItem();
      boolean ascending = m_sortDirCombo.getSelectedIndex() == 0;

      // clear table models:
      clearTableModel(m_categoriesTable);
      clearTableModel(m_columnsTable);

      // now go through all of the columns in the correct order:
      it = m_dspFormat.getColumns();
      PSDisplayColumn dispCol = null;
      while(it.hasNext())
      {
         dispCol = (PSDisplayColumn)it.next();

         // if in the cat list add to cat table
         boolean isCategory = categoryFields.contains(dispCol.getSource());
         setTableData(prepTableData(dispCol), isCategory);
      }

      setButtonState();
      updateSortCombos(sortCol.getName(), ascending);
   }

   /**
    * Loads this panel with the specified data.
    *
    * @param data the data to be loaded into the tables.  Only loads the
    * data if it is an instance of <code>PSDisplayFormat</code>, otherwise
    * it will be ignored.  Must not be <code>null</code>.
    */
   public void load(Object data)
   {
      if(data == null)
         throw new IllegalArgumentException("data must not be null");

      if (data instanceof PSDisplayFormat)
      {
         clearTableModel(m_columnsTable);
         clearTableModel(m_categoriesTable);
         m_hasContentTypeIdColumn = false;
         m_hasVariantNameColumn = false;
         m_hasSysTitleColumn = false;

         m_dspFormat = (PSDisplayFormat)data;
         PSDFColumns dispCols = m_dspFormat.getColumnContainer();

         Iterator itr = dispCols.iterator();
         PSDisplayColumn dsCol = null;
         
         while(itr.hasNext())
         {
            dsCol = (PSDisplayColumn)itr.next();
            setTableData(prepTableData(dsCol), dsCol.isCategorized());
         }
         
         String sortCol = m_dspFormat.getPropertyValue(
            PSDisplayFormat.PROP_SORT_COLUMN);         
         boolean ascending = m_dspFormat.doesPropertyHaveValue(
            PSDisplayFormat.PROP_SORT_DIRECTION, 
            PSDisplayFormat.SORT_ASCENDING);
         
         // take care the radio buttons, which are non-persist data
         // always reset to none, the default at the beginning
         
         // if not a new display format set the appropriate values.
         // checking all display formats must have a column even if they're not
         // persisted to the database
         if(dispCols.size() > 0)
         {
            if (m_dspFormat.isValidForRelatedContent())
               m_RelatedContentSearchRadio.setSelected(true);
            else if (m_dspFormat.isValidForFolder())
               m_FoldersRadio.setSelected(true);
            else
               m_NoneRadio.setSelected(true);
         }
         else
         {
            
            m_NoneRadio.setSelected(true);
         }
         
         setOrderedLists();
         
         // add sys_title as first column if missing
         if (!m_hasSysTitleColumn)
            addToColumns(createDispCol(
               PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD, null), false);
         else
         {
            // see if it's been categorized.  If so, move it.
            boolean moved = moveRowByFieldValue(
               PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD, false, false);
            if (moved)
            {
               PSDlgUtil.showErrorDialog(ms_res.getString("systitle.cat.msg"),
                     ms_res.getString("systitle.cat.title"));
            }
         }         
         
         updateSortCombos(sortCol, ascending);
         m_isLoaded = true;
      }
   }

   /**
    * Creates a display column and adds it to the columns table.
    *
    * @param internalName the name fot he field to be added.  Assumed not
    * <code>null</code>.
    *
    * @param labelName, this method will first check with the cataloger to see
    * if a display column can be made, if it cannot it will attempt to create
    * one directly, the labelName will be used in that case. May be
    * <code>null</code>.
    *
    * @return the newly created display column.  Never <code>null</code>.
    */
   private PSDisplayColumn createDispCol(String internalName,
      String labelName)
   {
      PSDisplayColumn defCol = null;

      // check the cataloger first to see if the values are there:
      PSLightWeightField temp = (PSLightWeightField)
         m_parentPanel.getCataloger().getSystemMap().get(
            internalName);

      if(temp == null)
         defCol = new PSDisplayColumn(PSDisplayFormat.COL_VARIANTID, labelName,
            PSDisplayColumn.GROUPING_FLAT, null, null, true);

      else
         defCol = new PSDisplayColumn(temp.getInternalName(),
            temp.getDisplayName(),
            PSDisplayColumn.GROUPING_FLAT, null, null, true);

      return defCol;
   }

   /**
    * In order to more easily track ordering changes in the category and
    * column tables, we make copies of the original models for these two
    * tables. When we save the data, we then use these copies to determine
    * how the ordering has changed. We do this to minimize changes sent to
    * the database. This method must be called whenever new data is loaded.
    */
   private void setOrderedLists()
   {
      m_categoryOrderList = (Vector)((DefaultTableModel)
         m_categoriesTable.getModel()).getDataVector().clone();

      m_columnOrderList = (Vector)((DefaultTableModel)
         m_columnsTable.getModel()).getDataVector().clone();

      setButtonState();
   }

   /**
    * Has this class been loaded with data? To ensure that this object
    * has been loaded with data the {@link #load(Object) load(Object)}
    * must be called.
    *
    * @return <code>true</code> if it has, otherwise <code>false</code>.
    */
   public boolean isLoaded()
   {
      return m_isLoaded;
   }

   /**
    * This returns the total number of entries in the category table.
    *
    * @return see description above.
    */
   int getCategoryCount()
   {
      return m_categoriesTable.getRowCount();
   }

  /**
    * Removes the selected rows in the specified table.
    *
    * @param table, that table from which the selected rows will be removed.
    * Assumed not <code>null</code>. 
    */
   private void removeSelectedRow(JTable table)
   {
      DefaultTableModel dtm = ((DefaultTableModel)table.getModel());
      ListSelectionModel lsm = table.getSelectionModel();

      String theFieldColValue = "";
      Vector data = dtm.getDataVector();
      int minIndex = lsm.getMinSelectionIndex();
      int maxIndex = lsm.getMaxSelectionIndex();

      for (int i = maxIndex; i >= minIndex; i--)
      {
         if (lsm.isSelectedIndex(i))
         {
            theFieldColValue = (String)dtm.getValueAt(i, FIELD_COLUMN);
            if(theFieldColValue.equalsIgnoreCase(PSDisplayFormat.COL_VARIANTID))
               m_hasVariantNameColumn = false;

            else if(theFieldColValue.equalsIgnoreCase(PSDisplayFormat.COL_CONTENTTYPEID))
               m_hasContentTypeIdColumn = false;

            else if(theFieldColValue.equalsIgnoreCase(
               PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD))
            {
               // show message
               PSDlgUtil.showWarningDialog(ms_res.getString("systitle.del.msg"),
                     ms_res.getString("systitle.del.title"));
               continue;
            }
            

            m_deleteList.add(theFieldColValue);
            data.removeElementAt(i);
         }
      }

      dtm.fireTableRowsDeleted(minIndex, maxIndex);
      updateSortCombos();
   }

   /**
    * Clears all rows from specified table.
    *
    * @param table assumed not <code>null</code>.
    */
   private void clearTableModel(JTable table)
   {
      DefaultTableModel model = (DefaultTableModel)table.getModel();
      int lastRow = model.getRowCount();

      Vector v = model.getDataVector();
      v.clear();

      model.fireTableRowsDeleted(0, lastRow);
   }

   /**
    * Calls {@link #setTableData(Vector, boolean, boolean) 
    * setTableData(data, categorized, true)}
    */
   private void setTableData(Vector data, boolean categorized)
   {
      setTableData(data, categorized, true);
   }

   /**
    * Populates the appropriate table with the specified data.  The if
    * cateorized is <code>true</code> then the categories table will
    * be populated, otherwise the columns table will be populated.
    *
    * @param data assumed not <code>null</code>.
    * @param categorized is this for the categorized table, then should be
    * <code>true</code> otherwise <code>false</code>.
    * @param append <code>true</code> to append a new row, <code>false</code> to
    * insert the new row as the first row.
    */
   private void setTableData(Vector data, boolean categorized, boolean append)
   {
      DefaultTableModel model = null;
      if (!categorized)
      {
         model = (DefaultTableModel)m_columnsTable.getModel();
         if (append)
            model.addRow(data);
         else         
            model.insertRow(0, data);         
      }
      else if (m_categoriesTable.isEnabled())
      {
         model = (DefaultTableModel)m_categoriesTable.getModel();
         if (append)
            model.addRow(data);
         else
            model.insertRow(0, data);
      }
   }

   /**
    * Prepares the data for use in the table from the specified display
    * column.
    *
    * @param col assumed not <code>null</code>.
    *
    * @return a vector for use in either of the tables. Never <code>null</code>.
    */
   private Vector prepTableData(PSDisplayColumn col)
   {
      String sourceName = col.getSource();
      Vector vec = new Vector();
      vec.add(col.getDisplayName());
      vec.add(sourceName);
      
      if (m_supportWidth)
      {
         int width = col.getWidth();
         if (width != -1)      
            vec.add(String.valueOf(width));
      }

      if (sourceName.equalsIgnoreCase(PSDisplayFormat.COL_CONTENTTYPEID)
            || sourceName.equalsIgnoreCase(PSDisplayFormat.COL_CONTENTTYPENAME))
      {
         m_hasContentTypeIdColumn = true;
      }
      else if (sourceName.equalsIgnoreCase(PSDisplayFormat.COL_VARIANTID)
            || sourceName.equalsIgnoreCase(PSDisplayFormat.COL_VARIANTNAME))
      {
         m_hasVariantNameColumn = true;
      }
      else if(sourceName.equalsIgnoreCase(
         PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD))
         m_hasSysTitleColumn = true;

      return vec;
   }

   /**
    * Save the data in the model used for display to the underlying data
    * structure?
    *
    * @return <code>false</code> if this is called before the
    * {@link #load(Object) load(Object)}, otherwise <code>true</code>.
    */
   public boolean save()
   {
      if(isLoaded())
      {
         if(!validateData())
            return false;

         persistDataToObject();
      }

      return isLoaded();
   }

   /**
    * Persists the model values to the <code>PSDisplayFormat</code> object.
    */
   private void persistDataToObject()
   {  
      updateData(PSDisplayColumn.GROUPING_CATEGORY,
            (DefaultTableModel)m_categoriesTable.getModel());
      updateData(PSDisplayColumn.GROUPING_FLAT,
            (DefaultTableModel)m_columnsTable.getModel());

      DisplayColumn sortCol = (DisplayColumn)m_sortColCombo.getSelectedItem();
      int dir = m_sortDirCombo.getSelectedIndex();

      m_dspFormat.removeProperty(PSDisplayFormat.PROP_SORT_COLUMN);
      m_dspFormat.removeProperty(PSDisplayFormat.PROP_SORT_DIRECTION);
      m_dspFormat.setProperty(PSDisplayFormat.PROP_SORT_COLUMN, 
         sortCol.getName());
      m_dspFormat.setProperty(PSDisplayFormat.PROP_SORT_DIRECTION, dir == 1 ? 
         PSDisplayFormat.SORT_DESCENDING : PSDisplayFormat.SORT_ASCENDING);


      // This needs to be called to handle 'apply'.  Apply
      // does not call <code>load</code> so the lists will never match and
      // redundant requests will be made to the server.
      setOrderedLists();
   }

   /**
    * Has the order changed in a model from the original order that loaded
    * that model?
    *
    * @param type Assumed to be one of the GROUPING_xxx values. This specifies
    *    which model has been provided so the correct original list can be used.
    *
    * @param model to which the vector will be compared.  Assumed not
    * <code>null</code>.
    *
    * @return <code>true</code> if the orders between the model and the
    * vector are different, otherwise <code>false</code>.
    */
   private boolean hasOrderChanged(int type, DefaultTableModel model)
   {
      List origVector = null;

      if(type == PSDisplayColumn.GROUPING_CATEGORY)
         origVector = m_categoryOrderList;
      else
         origVector = m_columnOrderList;

      // check for object reference position:
      boolean hasChanged = false;

      for(int i = 0; i < model.getDataVector().size() && !hasChanged; i++)
      {
         if(origVector.size() <= i ||
            origVector.get(i) != model.getDataVector().elementAt(i))
         {
            hasChanged = true;
         }
      }

      return hasChanged;
   }

   /**
    * Updates the underlying data structure based on the table model.
    *
    * @param type the display column type, assumed not <code>null</code>.
    *
    * @param model the model from which data will update the display columns,
    * assumed not <code>null</code>.
    */
   private void updateData(int type, DefaultTableModel model)
   {
      // get all columns:
      PSDFColumns dispCols = m_dspFormat.getColumnContainer();
      Iterator itr = dispCols.iterator();
      Map colMap = new HashMap();
      PSDisplayColumn dsCol = null;
      String source = null;

      // iterate through the columns
      // create a map using the column: getSource() as the key
      // the col as the value.
      while(itr.hasNext())
      {
         // for each column,
         dsCol = (PSDisplayColumn)itr.next();
         colMap.put(dsCol.getSource(), dsCol);
      }

      // loop through delete list
      // and remove from cols:
      itr = m_deleteList.iterator();
      while(itr.hasNext())
      {
          dsCol = (PSDisplayColumn)colMap.get((String)itr.next());
          dispCols.remove(dsCol);
      }

      // clear list:
      m_deleteList.clear();

      /* This is here because the column list in the object is actually
         composed of 2 lists that are integrated during save. To eliminate
         unnecessary updates to the db, we don't change the column positions
         unless they are different from when they started editing this col.*/
      boolean orderChanged = hasOrderChanged(type, model);

      for(int k = 0; k < model.getRowCount(); k++)
      {
         source = (String)model.getValueAt(k, FIELD_COLUMN);
         dsCol = (PSDisplayColumn)colMap.get(source);
         dsCol.setGroupingType(type);
         
         if (m_supportWidth && type == PSDisplayColumn.GROUPING_FLAT)
         {
            int width = -1;
            String strWidth = (String)model.getValueAt(k, WIDTH_COLUMN);
            if (strWidth != null && strWidth.trim().length() > 0)
            {
               // we've already validated it's numeric
               width = Integer.parseInt(strWidth);            
            }
            dsCol.setWidth(width);
         }
 
         if (orderChanged)
            dispCols.move(dsCol.getPosition(), k);
      }
   }

  /**
   * Moves the rows from <code>m_availableTable</code> to <code>m_selectedTable
   * </code> when <code>m_downTableBtn</code> is pressed and vice versa if
   * <code>m_upTableBtn</code>.
   */
   private class MoveRowListener implements ActionListener
   {
      /**
       * Constructs the object.
       *
       * @param up, see description  for this class.
       */
      public MoveRowListener(boolean up)
      {
         m_isUp = up;
      }

      /**
       * Implements the interface.
       *
       * @param e, provided by the event handling mechanism of swing, never
       * <code>null</code>.
       */
      public void actionPerformed(ActionEvent e)
      {
         moveTableFields(m_isUp);
      }

      /**
        * Initialized in the ctor, see the description for the class.
        */
      private boolean m_isUp;
   }

   /**
    * This moves the selected items from one table to the other.  This method
    * was originally part of the MoveRowListener innerclass, however it
    * didn't need to be so it was moved out.  However, because of its
    * origin there are some leftovers that should be changed when there is
    * time.  The <code>isUp</code> is one of the leftovers that should be
    * refactored.
    *
    * @param isUp if <code>true</code> this moves the selected rows from
    * the columns table up to the category table, if <code>false</code> it
    * moves the selected rows from the category table to the columsn table.
    */
   private void moveTableFields(boolean isUp)
   {
      ListSelectionModel lsm = null;
      DefaultTableModel dtm = null;

      if (!isUp)
      {
         dtm = ((DefaultTableModel)m_categoriesTable.getModel());
         lsm = m_categoriesTable.getSelectionModel();
      }
      else
      {
         dtm = ((DefaultTableModel)m_columnsTable.getModel());
         lsm = m_columnsTable.getSelectionModel();
      }

      if (!lsm.isSelectionEmpty())
      {
         Vector dataVec = dtm.getDataVector();
         Vector rowsRemovedVec = new Vector();
         int minIndex = lsm.getMinSelectionIndex();
         int maxIndex = lsm.getMaxSelectionIndex();
         int titleIndex = -1;
         for (int i = minIndex; i <= maxIndex; i++)
         {
            if (lsm.isSelectedIndex(i))
            {
               if (isUp)
               {
                  String fieldName = (String)dtm.getValueAt(i, FIELD_COLUMN);
                  if(fieldName.equalsIgnoreCase(
                     PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD))
                  {
                     // show message
                     titleIndex = i;
                     PSDlgUtil.showWarningDialog(
                           ms_res.getString("systitle.move.msg"),
                           ms_res.getString("systitle.move.title"));
                     continue;
                  }
               }                  
               
               rowsRemovedVec.add(dataVec.get(i));
            }
         }

         int k = 0;
         for (int i = minIndex; i <= maxIndex; i++)
         {
            if (lsm.isSelectedIndex(i - k) && i != titleIndex)
            {
               dtm.removeRow(i - k);
               k++;
            }
         }

         if (!rowsRemovedVec.isEmpty())
            move(isUp, rowsRemovedVec);

         ((DefaultTableModel)
            m_categoriesTable.getModel()).fireTableDataChanged();
         ((DefaultTableModel)
            m_columnsTable.getModel()).fireTableDataChanged();

         setButtonState();
      }
   }

   /**
    * See description for MoveRowListener.
    *
    * @param up, if <code>true</code> then rows move up else down.
    *
    * @param rows, vector of row data being up or down, assumed to be not <code>
    * null</code>.
    */
   private void move(boolean up, Vector rows)
   {
      DefaultTableModel dtm = null;
      if (!up)
      {
         dtm = ((DefaultTableModel)m_columnsTable.getModel());
      }
      else
      {
         dtm = ((DefaultTableModel)m_categoriesTable.getModel());
      }

      Iterator itr = rows.iterator();
      while(itr.hasNext())
         dtm.addRow((Vector)itr.next());
      
      updateSortCombos();
   }

   /**
    * Gets the data for the combo box.
    *
    * @return, vector containing the combo box data, never <code>null</code> or
    * empty.
    */
   private Vector prepareComboData()
   {
      Vector vec = new Vector();
      vec.add(ms_res.getString("combo.ascending"));
      vec.add(ms_res.getString("combo.descending"));

      return vec;
   }

   /**
    * Listener responding to row selection in the table. Based on the row
    * selection up and down button state is set using {@link #setButtonState(
    * JTable)}.
    */
   private class RowSelectionListener implements ListSelectionListener
   {
      /**
       * Constructs this object.
       *
       * @param i, specifies the table, if 0 then it's 'Categories' table, if 1
       * then it's 'Columns' table.
       */
      public RowSelectionListener(int i)
      {
         m_table = i;
      }

      /**
       * Implementing the interface. {@link #setButtonState(JTable)} is called
       * when a row is selected in the table specified.
       */
      public void valueChanged(ListSelectionEvent e)
      {
         if (e.getValueIsAdjusting())
            return;
         ListSelectionModel lsm = (ListSelectionModel)e.getSource();
         if (!lsm.isSelectionEmpty())
         {
            setButtonState();
            setButtonState( m_table);
         }
      }

      /**
       * Index specifying the table. If 0 it's 'Categories' table, if 1 then
       * it's 'Columns' table
       */
      int m_table;
   }
   
   /**
    * Data object for the sort column combo that is displayed using the column's
    * Label, and can provide the columns internal name.
    */
   private class DisplayColumn
   {
      /**
       * Construct this object from its member data.
       * 
       * @param name The internal name identifying the column, may not be 
       * <code>null</code> or empty.
       * @param label The label used to display the column, may not be
       * <code>null</code> or empty.
       */
      public DisplayColumn(String name, String label)
      {
         if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("name may not be null or empty");

         if (label == null || label.trim().length() == 0)
            throw new IllegalArgumentException(
               "label may not be null or empty");

         m_name = name;
         m_label = label;
      }
      
      /**
       * Get the name of this column.
       * 
       * @return The name, never <code>null</code> or empty.
       */
      public String getName()
      {
         return m_name;         
      }
      
      /**
       * Get the label used to display this column.
       * 
       * @return The label, never <code>null</code> or empty.
       */
      public String getLabel()
      {
         return m_label;
      }
      
      /**
       * Returns the string representation of this column.
       * 
       * @return The label, never <code>null</code> or empty.
       */
      public String toString()
      {
         return m_label;
      }
      
      // see base class
      public boolean equals(Object obj)
      {
         boolean isEqual = true;
         
         if (!(obj instanceof DisplayColumn))
            isEqual = false;
         else
         {
            DisplayColumn other = (DisplayColumn)obj;
            if (!m_name.equals(other.m_name))
               isEqual = false;
            else if (!m_label.equals(other.m_label))
               isEqual = false;
         }
         
         return isEqual;
      }
      
      // see base class
      public int hashCode()
      {
         return (m_name + m_label).hashCode();
      }
      
      /**
       *  The name of the column, never <code>null</code> or empty or modified
       * after construction.
       */
      private String m_name;
      
      /**
       *  The label of the column, never <code>null</code> or empty or modified
       * after construction.
       */
      private String m_label;

   }

   /**
     * Moves the rows up and down in <code>m_selectedTable</code> table.
     *
     * @param up, if <code>true</code> the rows move up else down.
     */
   private void move(JTable table, boolean up)
   {
      DefaultTableModel dtm = ((DefaultTableModel)table.getModel());
      ListSelectionModel lsm = table.getSelectionModel();
      Vector data = dtm.getDataVector();
      int minIndex = lsm.getMinSelectionIndex();
      int maxIndex = lsm.getMaxSelectionIndex();
      if (up)
      {
         for (int i = minIndex; i <= maxIndex; i++)
         {
            if (lsm.isSelectedIndex(i))
            {
               Object aRow = data.elementAt(i);
               
               data.removeElementAt(i);
               data.insertElementAt(aRow, i - 1);
            }
         }
      }
      else
      {
         for (int i = maxIndex; i >= minIndex; i--)
         {
            if (lsm.isSelectedIndex(i))
            {
               Object aRow = data.elementAt(i);
               data.removeElementAt(i);
               data.insertElementAt(aRow, i + 1);
            }
         }
      }

      dtm.fireTableDataChanged();
      minIndex = up ? minIndex - 1: minIndex + 1;
      maxIndex = up ? maxIndex - 1: maxIndex + 1;
      table.setRowSelectionInterval(minIndex, maxIndex);

      setButtonState();
      updateSortCombos();
   }

   /* @todo the set buttons state methods should really be one method.
   this should be fixed.
   */
   private void setButtonState()
   {
      if (!m_categoriesTable.isEnabled())
      {
         m_catToCol.setEnabled(false);
         m_colToCat.setEnabled(false);
      }
      else
      {
         ListSelectionModel lsm = m_categoriesTable.getSelectionModel();
         int rowCount = m_categoriesTable.getModel().getRowCount();
         if (rowCount == 0 || lsm.isSelectionEmpty())
         {
            m_catToCol.setEnabled(false);
         }
         else
         {
            m_catToCol.setEnabled(true);
         }

         rowCount = m_columnsTable.getModel().getRowCount();
         if (m_FoldersRadio.isSelected() || rowCount == 0 ||
            m_columnsTable.getSelectionModel().isSelectionEmpty())
         {
            m_colToCat.setEnabled(false);
         }
         else if(!m_FoldersRadio.isSelected())
         {
            m_colToCat.setEnabled(true);
         }
      }

      setButtonState(0);
      setButtonState(1);
   }

   /**
    * Sets the button state in the table specified. If the top most row is
    * selected down button is enabled and up button is disabled. If bottom most
    * row is selected then up button is enabled and down button is disabled.
    * Foe any other rows both the buttons are enabled.
    *
    * @param table,  index specifying the table. If 0 it's 'Categories' table,
    * if 1 then it's 'Columns' table
    */
   private void setButtonState(int index)
   {
      int rowCount = 0;
      ListSelectionModel lsm = null;
      if (index ==0 )
      {
      rowCount = m_categoriesTable.getModel().getRowCount();
      lsm = m_categoriesTable.getSelectionModel();

         if (rowCount == 0 || lsm.isSelectionEmpty())
         {
           m_categoryUp.setEnabled(false);
           m_categoryDwn.setEnabled(false);
           m_categoryDel.setEnabled(false);
         }
         else
         {
            m_categoryDel.setEnabled(true);
            if (lsm.isSelectedIndex(0))
               m_categoryUp.setEnabled(false);
            else
               m_categoryUp.setEnabled(true);
            if (rowCount != 0)
            {
            if (lsm.isSelectedIndex(rowCount - 1))
               m_categoryDwn.setEnabled(false);
            else
               m_categoryDwn.setEnabled(true);
            }
         }
      }
      else
      {
         rowCount = m_columnsTable.getModel().getRowCount();
         lsm = m_columnsTable.getSelectionModel();


         if (rowCount == 0 || lsm.isSelectionEmpty())
         {
            m_columnUp.setEnabled(false);
            m_columnDwn.setEnabled(false);
            m_columnDel.setEnabled(false);
         }
         else
         {
            m_columnDel.setEnabled(true);

            if (lsm.isSelectedIndex(0))
               m_columnUp.setEnabled(false);
            else
               m_columnUp.setEnabled(true);
            if (rowCount != 0)
            {
            if (lsm.isSelectedIndex(rowCount - 1))
               m_columnDwn.setEnabled(false);
            else
              m_columnDwn.setEnabled(true);
            }
         }
      }
   }
   
   /**
    * Calls {@link #updateSortCombos(String, boolean) 
    * updateSortCombos(null, true)}
    */
   private void updateSortCombos()
   {
      updateSortCombos(null, true);
   }
   
   /**
    * Updates the state of the sort field and direction combo boxes.  Resets the
    * combo box entries with those of the columns table.  Attempts to maintain
    * the current selections if they are still valid unless a new column 
    * selection is specified.  If the specified column or current selection are
    * not valid, the first field in the list is selected, and the sort is set
    * to ascending by default.
    * 
    * @param col The column to select.  Is only selected if found in the list of
    * fields defined by the columns table.  May be <code>null</code> or empty to
    * attempt to maintain the current selection. 
    * @param ascending <code>true</code> to sort ascending, <code>false</code> 
    * to sort descending.  Ignored if the sort field combo is not being set to
    * the field specified by the <code>col</code> parameter.
    */
   private void updateSortCombos(String col, boolean ascending)
   {

      DefaultTableModel dtm = 
         (DefaultTableModel)m_columnsTable.getModel();
      
      // get current selection if there is one
      DisplayColumn selectedCol = 
         (DisplayColumn)m_sortColCombo.getSelectedItem();            
   
      // repopulate
      m_sortColCombo.removeAllItems();
      boolean hasSelectedCol = false;      
      int specifiedColIndex = -1;
      
      for (int i = 0; i < dtm.getRowCount(); i++)
      {
         String name = (String)dtm.getValueAt(i, FIELD_COLUMN);
         String label = (String)dtm.getValueAt(i, LABEL_COLUMN);
         m_sortColCombo.addItem(new DisplayColumn(name, label));
         if (!hasSelectedCol && selectedCol != null && 
            selectedCol.getName().equals(name))
         {
            hasSelectedCol = true;
         }
         
         if (specifiedColIndex == -1 && col != null && name.equals(col))
            specifiedColIndex = i;
      }
   
      // select if possible
      if (specifiedColIndex != -1)
      {
         m_sortColCombo.setSelectedIndex(specifiedColIndex);         
         m_sortDirCombo.setSelectedIndex(ascending ? 0 : 1);         
      }
      else if (selectedCol != null && hasSelectedCol)
         m_sortColCombo.setSelectedItem(selectedCol);               
      else
         m_sortDirCombo.setSelectedIndex(0);
   }

   private PSDisplayFormat m_dspFormat;
   private JButton m_catToCol;
   private JButton m_colToCat;

   /**
    * When this dialog box was originally done the customize dialog handled
    * creating all of the columns.  The design that was used was to pass the
    * actual psobjects back and forth between the 2 dialogs and to write
    * and rewrite the order, insertions and deletions to and from the lists both
    * display and the list that will be used to persist the data back to the
    * database.
    *
    * A new requirement came where we needed to validate that certain columns
    * are present for certain behavior to take place.  This was added as
    * checkboxes on this panel.  This now exposed the poor design previously
    * described and now we have to keep track of insertions, ordering and
    * deletions in 2 lists in 2 dialog boxes.  These fields along with other
    * methods are hacks to  accomodate this.  When there is time this
    * dialog as well as the customize dialogs implementation should be gutted
    * and replaced with a proper design.
    */
   private boolean m_hasVariantNameColumn = false;
   private boolean m_hasSysTitleColumn = false;
   private boolean m_hasContentTypeIdColumn = false;

   /**
    * The button that launches the customize dialog.  Initialized in
    * {@link createColumnPanel() createColumnPanel()}, never <code>null</code>
    * after that, and is invariant.
    */
   private JButton m_columnCustomize;

   /**
    * Initialized in the contstructor, never <code>null</code> after that and
    * reference is invariant.
    */
   private DisplayFormatTabbedPanel m_parentPanel = null;

   /**
    * 'Categories' table with 3 columns - Field, Label, Sort Order. Intialized
    * in  {@#createCategoryTablePanel()}, never <code>null</code> or modified
    * after that.
    */
   private JTable m_categoriesTable;

   /**
    * 'Columns' table with 3 columns - Field, Label, Sort Order. Intialized
    * in  {@#createColumnTablePanel()}, never <code>null</code> or modified
    * after that.
    */
   private JTable m_columnsTable;

   /**
    * Button to move rows up in 'Categories' table. If the top most row has been
    * selected then it's disabled else always enabled. Intialized in  {@#
    * createCategoryTablePanel()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_categoryUp;

   /**
    * Button to move rows down in 'Categories' table. If the bottom most row has
    * been selected then it's disabled else always enabled. Intialized in  {@#
    * createCategoryTablePanel()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_categoryDwn;

   /**
    * Button to remove rows in the 'Categories' table.  Intialized in  {@#
    * createCategoryTablePanel()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_categoryDel;

   /**
    * Button to move rows up in 'Columns' table. If the top most row has been
    * selected then it's disabled else always enabled. Intialized in  {@#
    * createColumnTablePanel()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_columnUp;

   /**
    * Button to move rows down in 'Columns' table. If the bottom most row has
    * been selected then it's disabled else always enabled. Intialized in  {@#
    * createColumnTablePanel()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_columnDwn;

   /**
    * Button to remove rows in the 'Columns' table.  Intialized in  {@#
    * createColumnTablePanel()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_columnDel;

   /**
    * Holds a list of names  (<code>FIELD_COLUMN</code>)
    * values for deletion from the display columns list.  This is never
    * <code>null</code>, may be empty, and invariant (always the same
    * reference).
    */
   private Collection m_deleteList = new HashSet();

   /**
    * These fields hold the order of the columns each time the
    * {@link #setOrderedLists() setOrderedLists()} is called.
    * Never <code>null</code> after that method is called, may be empty and
    * will be reset if the aforementioned method is again called.
    */
   private List m_categoryOrderList = null;
   private List m_columnOrderList = null;

   /**
    * Set once {@link #load(Object) load(Object)} has been called.  Will be
    * <code>true</code> after that.
    *
    * @see #isLoaded() for more details.
    */
   private boolean m_isLoaded = false;

   /**
    * RelatedContentSearch Radio button is selected the content type and variant must be present
    * either as column or category.
    */
   private JRadioButton m_RelatedContentSearchRadio;

   /**
    * Folders Radio button is selected, no categories are allowed, category fields will be
    * moved to the columns table
    */
   private JRadioButton m_FoldersRadio;

   /**
    * None Radio button is selected. Only default columns are in the columns table.
    */
   private JRadioButton m_NoneRadio;

   private static final int NONE_BUTTON = 0;
   private static final int FOLDERS_BUTTON = 1;
   private static final int RELATED_CONTENT_SEARCH_BUTTON = 2;

   private int m_selectedButton = 0;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;

   /**
    * The display column for the variantname. This is initialized when
    * {@link #validateRelatedContentCheckbox() validateRelatedContentCheckbox()}
    * is called, this may be to <code>null</code> if the related content
    * search checkbox is deselected.
    */
   private PSDisplayColumn m_variantColumn = null;
   
   /**
    * The display column for the content type id. This is initialized when
    * {@link #validateRelatedContentCheckbox() validateRelatedContentCheckbox()}
    * is called, this may be to <code>null</code> if the related content
    * search checkbox is deselected.
    */
   private PSDisplayColumn m_contentTypeIdColumn = null;

   /**
    * Indicates if column widths are supported by the server.  
    * <code>true</code> if they are, <code>false</code> otherwise.  Set during
    * ctor, never modified after that.
    */
   private boolean m_supportWidth = false;
   
   /**
    * Combo box used to specify the initial sort column.  Initialized during
    * construction, contents are modified by 
    * {@link #updateSortCombos(String, boolean)}.  Never <code>null</code> after
    * construction.
    */
   private JComboBox m_sortColCombo = null;

   /**
    * Combo box used to specify the initial sort direction.  Initialized during
    * construction, contents are modified by 
    * {@link #updateSortCombos(String, boolean)}.  Never <code>null</code> after
    * construction.
    */
   private JComboBox m_sortDirCombo = null;

   private static final String COLUP = "colup";
   
   // column indices in the tables:
   private static final int LABEL_COLUMN = 0;
   private static final int FIELD_COLUMN = 1;
   private static final int WIDTH_COLUMN = 2;
}

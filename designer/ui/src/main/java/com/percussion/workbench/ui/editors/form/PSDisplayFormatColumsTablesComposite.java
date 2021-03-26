/******************************************************************************
 *
 * [ PSDisplayFormatColumsTablesComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.PSDFColumns;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.search.ui.PSFieldSelectionEditorDialog;
import com.percussion.workbench.ui.FeatureSet;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonFactory;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSEditorUtil;
import com.percussion.workbench.ui.legacy.AwtSwtModalDialogBridge;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.guitools.IPSValueChangedListener;
import com.percussion.guitools.PSValueChangedEvent;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Composite that holds the display format column tables and
 * the action buttons that operate on these tables.
 */
public class PSDisplayFormatColumsTablesComposite extends Composite
   implements IPSDesignerObjectUpdater, IPSUiConstants
{   
   
   /**
    * Create the composite
    * @param parent
    * @param style
    */
   public PSDisplayFormatColumsTablesComposite(Composite parent, int style,
      PSEditorBase editor)
   {
      super(parent, style);
      setLayout(new FormLayout());
      
      m_supportWidth = FeatureSet.isFeatureSupported(FeatureSet.FEATURE_FTS);
      
      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null."); //$NON-NLS-1$
      m_editor = editor;
      m_displayFormat = (PSDisplayFormat)editor.m_data;

      // Create all controls first so that we can attach to any control
      // when laying out the controls
      m_categoriesLabel = new Label(this, SWT.NONE);
      m_columnsLabel = new Label(this, SWT.NONE);
      m_categoryTable = new PSSortableTable(this, new ColumnTableLabelProvider(),
         new DummyNewRowProvider(), SWT.NONE,
         PSSortableTable.DELETE_ALLOWED);
      m_categoryTable.addColumn(
         "PSDisplayFormatColumsTablesComposite.categories.label.label",  //$NON-NLS-1$
         PSSortableTable.NONE,
         new ColumnWeightData(10,100, true), null, SWT.LEFT);
      m_categoryTable.addColumn(
         "PSDisplayFormatColumsTablesComposite.categories.field.label",  //$NON-NLS-1$
         PSSortableTable.NONE,
         new ColumnWeightData(10,100, true), null, SWT.LEFT);
      m_categoryTable.getTable().addFocusListener(new DeselectorFocusListener());
      m_categoryTable.getTable().addSelectionListener(
         new HandleButtonEnablingSelectionListener());
      
      m_buttonComp = createButtonsComposite();
      m_columnTable = new PSSortableTable(this, new ColumnTableLabelProvider(),
         new DummyNewRowProvider(), SWT.NONE,
         PSSortableTable.DELETE_ALLOWED);
      m_columnTable.setCellModifier(new ColumnTableCellModifier(m_columnTable));
      m_columnTable.addColumn(
         "PSDisplayFormatColumsTablesComposite.tableColumn.label.label", //$NON-NLS-1$ 
         PSSortableTable.NONE, 
         new ColumnWeightData(10,100, true), null, SWT.LEFT);
      m_columnTable.addColumn(
         "PSDisplayFormatColumsTablesComposite.tableColumn.field.label", //$NON-NLS-1$ 
         PSSortableTable.NONE, 
         new ColumnWeightData(10,100, true), null, SWT.LEFT);
      if(m_supportWidth)
      {
         CellEditor cEditor = new TextCellEditor(m_columnTable.getTable());
         Text textControl = (Text)cEditor.getControl();
         textControl.addModifyListener(new ModifyListener()
               {
                  /* (non-Javadoc)
                   * @see org.eclipse.swt.events.ModifyListener#modifyText(
                   * org.eclipse.swt.events.ModifyEvent)
                   */
                  public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
                  {
                     fireSelectionEvent();                     
                  }
                  
               });
         m_columnTable.addColumn(
            "PSDisplayFormatColumsTablesComposite.tableColumn.width.label", //$NON-NLS-1$ 
            PSSortableTable.NONE,
            new ColumnWeightData(5,60, true), 
            cEditor, SWT.LEFT);
      }
      m_columnTable.getTable().addFocusListener(new DeselectorFocusListener());
      m_columnTable.getTable().addSelectionListener(
         new HandleButtonEnablingSelectionListener());
      
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(m_categoryTable, 0, SWT.LEFT);
      m_categoriesLabel.setLayoutData(formData);
      m_categoriesLabel.setText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.categories.label")); //$NON-NLS-1$

      
      final FormData formData_1 = new FormData();      
      formData_1.top = new FormAttachment(0, 0);
      formData_1.left = new FormAttachment(m_columnTable, 0, SWT.LEFT);
      m_columnsLabel.setLayoutData(formData_1);
      m_columnsLabel.setText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.columns.label")); //$NON-NLS-1$
      
      // layout category table
      final FormData formData_3 = new FormData();
      formData_3.right = new FormAttachment(40, 0);
      formData_3.bottom = new FormAttachment(m_buttonComp, 0, SWT.BOTTOM);
      formData_3.left = new FormAttachment(0, 0);
      formData_3.top = new FormAttachment(m_categoriesLabel, 0, SWT.BOTTOM);
      m_categoryTable.setLayoutData(formData_3);
      
      
      // layout buttons
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(m_columnTable, -5, SWT.LEFT);
      formData_4.top = new FormAttachment(m_categoryTable, 0, SWT.TOP);
      formData_4.left = new FormAttachment(m_categoryTable, 5, SWT.RIGHT);
      m_buttonComp.setLayoutData(formData_4);
      
      // layout category table
      final FormData formData_2 = new FormData();
      formData_2.left = new FormAttachment(60, 0);
      formData_2.right = new FormAttachment(100, 0);
      formData_2.bottom = new FormAttachment(m_buttonComp, 0, SWT.BOTTOM);
      formData_2.top = new FormAttachment(m_categoryTable, 0, SWT.TOP);
      m_columnTable.setLayoutData(formData_2);      

      m_addColumnsButton = new Button(this, SWT.NONE);
      m_addColumnsButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(
            @SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            onAddColumns(new Runnable()
            {
               //validates that the cols are ok for folders
               public void run()
               {
                  PSDisplayFormatEditor dfe = (PSDisplayFormatEditor) m_editor;
                  dfe.validateFolderColumns();
               }
            });
         }
      });
      final FormData formData_5 = new FormData();
      formData_5.right = new FormAttachment(m_columnTable, 0, SWT.RIGHT);
      formData_5.top = new FormAttachment(m_columnTable, 5, SWT.BOTTOM);
      m_addColumnsButton.setLayoutData(formData_5);
      m_addColumnsButton.setText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.addColumns.label")); //$NON-NLS-1$      
   }   

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }
   
   private Composite createButtonsComposite()
   {
      Composite comp = new Composite(this, SWT.NONE);      
      comp.setLayout(new FormLayout());

      Composite dummy = new Composite(comp, SWT.NONE);
      
      
      m_upButton = PSButtonFactory.createUpButton(comp);
      m_upButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            onUp();
         }         
      });
      final FormData formData_5 = new FormData();      
      formData_5.top = new FormAttachment(0, 0);
      formData_5.left = new FormAttachment(dummy, 0, SWT.CENTER);
      formData_5.width = BUTTON_WIDTH;
      formData_5.height = BUTTON_HEIGHT;
      m_upButton.setLayoutData(formData_5);
      m_upButton.setToolTipText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.moveUp.tooltip")); //$NON-NLS-1$

      m_downButton = PSButtonFactory.createDownButton(comp);
      m_downButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            onDown();
         }
      });
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(m_upButton, 2, SWT.BOTTOM);
      formData_6.left = new FormAttachment(dummy, 0, SWT.CENTER);
      formData_6.width = BUTTON_WIDTH;
      formData_6.height = BUTTON_HEIGHT;
      m_downButton.setLayoutData(formData_6);
      m_downButton.setToolTipText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.moveDown.tooltip")); //$NON-NLS-1$

      m_deleteButton = PSButtonFactory.createDeleteButton(comp);
      m_deleteButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            onDelete();
         }
      });
      final FormData formData_7 = new FormData();
      formData_7.top = new FormAttachment(m_downButton, 2, SWT.BOTTOM);      
      formData_7.left = new FormAttachment(dummy, 0, SWT.CENTER);
      formData_7.width = BUTTON_WIDTH;
      formData_7.height = BUTTON_HEIGHT;
      m_deleteButton.setLayoutData(formData_7);
      m_deleteButton.setToolTipText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.delete.tooltip")); //$NON-NLS-1$

      m_addButton = PSButtonFactory.createRightButton(comp);
      m_addButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            onAdd();
         }
      });
      final FormData formData_8 = new FormData();
      formData_8.top = new FormAttachment(m_deleteButton, 4, SWT.BOTTOM);
      formData_8.left = new FormAttachment(dummy, 0, SWT.CENTER);
      formData_8.width = BUTTON_WIDTH;
      formData_8.height = BUTTON_HEIGHT;
      m_addButton.setLayoutData(formData_8);
      m_addButton.setToolTipText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.moveToColumns.tooltip")); //$NON-NLS-1$

      m_removeButton = PSButtonFactory.createLeftButton(comp);
      m_removeButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            onRemove();
         }
      });
      final FormData formData_9 = new FormData();
      formData_9.top = new FormAttachment(m_addButton, 2, SWT.BOTTOM);
      formData_9.left = new FormAttachment(dummy, 0, SWT.CENTER);
      formData_9.width = BUTTON_WIDTH;
      formData_9.height = BUTTON_HEIGHT;
      m_removeButton.setLayoutData(formData_9);
      m_removeButton.setToolTipText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.moveToCategories.tooltip")); //$NON-NLS-1$

      m_addAllButton = PSButtonFactory.createDoubleRightButton(comp);
      m_addAllButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            onAddAll();
         }
      });
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(m_removeButton, 2, SWT.BOTTOM);
      formData_10.left = new FormAttachment(dummy, 0, SWT.CENTER);
      formData_10.width = BUTTON_WIDTH;
      formData_10.height = BUTTON_HEIGHT;
      m_addAllButton.setLayoutData(formData_10);
      m_addAllButton.setToolTipText(PSMessages.getString(
         "PSDisplayFormatColumsTablesComposite.moveAllCatsToCols.tooltip")); //$NON-NLS-1$
      
      final FormData formData_dummy = new FormData();      
      formData_dummy.top = new FormAttachment(0, 0);
      formData_dummy.left = new FormAttachment(0, 0);
      formData_dummy.right = new FormAttachment(100, 0);
      formData_dummy.height = 0;
      dummy.setLayoutData(formData_dummy);
      
      return comp;
   }
   
   /**
    * Adds a selection listener to this control. The selection listener
    * will be notified of selection events on this control and any nested
    * control.
    * @param listener cannot be <code>null</code>.
    */
   public void addSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null."); //$NON-NLS-1$
      if(!m_selectionListeners.contains(listener))
         m_selectionListeners.add(listener);
   }
   
   /**
    * Removes the specified selection listener
    * @param listener cannot be <code>null</code>.
    */
   public void removeSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null."); //$NON-NLS-1$
      if(m_selectionListeners.contains(listener))
         m_selectionListeners.remove(listener);
   }
   
   /**
    * Called when a selection event occurs on the control or
    * any of its nested controls.
    */
   protected void fireSelectionEvent()
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;     
      SelectionEvent event = new SelectionEvent(e);
      for(SelectionListener listener : m_selectionListeners)
      {
        listener.widgetSelected(event);
      }
   }
   
   /**
    * Handles setting the table buttons enabled or disabled based
    * on certain criteria.
    */
   protected void handleButtonsEnable()
   {
      PSDisplayFormatEditor editor = (PSDisplayFormatEditor)m_editor;
      PSRadioAndCheckBoxes usedWith = editor.m_usedWithButtons;
      Table catTable = m_categoryTable.getTable();
      Table colTable = m_columnTable.getTable();
      int catTableSelectCount = catTable.getSelectionCount();
      int colTableSelectCount = colTable.getSelectionCount();
      
      //Is sys_title selected in the columns table?
      boolean bSysTitleSelected = false;
      
      StructuredSelection selection = 
         (StructuredSelection)m_columnTable.getTableViewer().getSelection();
      Iterator it = selection.iterator();
      while(it.hasNext())
      {
         PSDisplayColumn col = (PSDisplayColumn)it.next();
         if("sys_title".equals(col.getSource()))
         {
            bSysTitleSelected = true;
            break;
         }
      }
      //Up, Down and Delete buttons
      if(catTableSelectCount > 0)
      {
         m_upButton.setEnabled(catTableSelectCount == 1 &&
            catTable.getSelectionIndex() > 0);
         m_downButton.setEnabled(catTableSelectCount == 1 &&
            catTable.getSelectionIndex() < (catTable.getItemCount() - 1));
         m_deleteButton.setEnabled(canBeDeleted(
            catTable.getSelectionIndices(), m_categoryTable));
      }
      else if(colTableSelectCount > 0)
      {
         m_upButton.setEnabled(colTableSelectCount == 1 &&
            colTable.getSelectionIndex() > 0);
         m_downButton.setEnabled(colTableSelectCount == 1 &&
            colTable.getSelectionIndex() < (colTable.getItemCount() - 1));
         m_deleteButton.setEnabled(canBeDeleted(
            colTable.getSelectionIndices(), m_columnTable));
      }
      else
      {
         m_upButton.setEnabled(false);
         m_downButton.setEnabled(false);
         m_deleteButton.setEnabled(false);
      }
      
      // Add button
      m_addButton.setEnabled(catTableSelectCount > 0);
      
      // Add All button
      m_addAllButton.setEnabled(catTable.getItemCount() > 0);
      
      // Remove button
      m_removeButton.setEnabled(
         usedWith.getSelectedIndex() != editor.FOLDERS_BUTTON &&
         colTableSelectCount > 0 && !bSysTitleSelected);
     
   }
   
   /**
    * Utility method to determine if the selected table items are allowed
    * to be deleted.
    * @param index
    * @param table
    * @return <code>true</code> if the items can be deleted
    */
   private boolean canBeDeleted(int[] index, PSSortableTable table)
   {
      List values = table.getValues();
      for(int idx : index)
      {
         PSDisplayColumn col = (PSDisplayColumn)values.get(idx);
         int sIndex = ((PSDisplayFormatEditor)m_editor).
            m_usedWithButtons.getSelectedIndex();
         if(sIndex == PSDisplayFormatEditor.RELATED_BUTTON)
         {
            if(col.getSource().equalsIgnoreCase(PSDisplayFormat.COL_VARIANTID) || 
               col.getSource().equalsIgnoreCase(PSDisplayFormat.COL_CONTENTTYPEID))
               return false;
         }
         if(col.getSource().equalsIgnoreCase(
            PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD))
            return false;
      }
      return true;
   }
   
   
   
   /**
    * Updates the categories and columns table so they reflect the
    * contents of the display format that this editor is editing.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   protected void updateTables()
   {
      Iterator<PSDisplayColumn> it = 
         (Iterator<PSDisplayColumn>)m_displayFormat.getColumns();
      List<PSDisplayColumn> columns = new ArrayList<PSDisplayColumn>();
      List<PSDisplayColumn> categories = new ArrayList<PSDisplayColumn>();
      while(it.hasNext())
      {
         PSDisplayColumn col = it.next();
         if(col.isCategorized())
         {
            categories.add(col);
         }
         else
         {
            columns.add(col);
         }
      }
      m_categoryTable.setValues(categories);
      m_columnTable.setValues(columns);
      setSortColumnControl();
      handleButtonsEnable();
      fireSelectionEvent();
   }
   
   /**
    * Sets the values in the sort column combo viewer and attempts
    * to set the value from the display format if it still exists in
    * the available list of columns or if not then set sys_title as
    * the default.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void setSortColumnControl()
   {
      PSDisplayFormat df = (PSDisplayFormat)m_editor.m_data;
      ComboViewer combo = 
         ((PSDisplayFormatEditor)m_editor).m_sortColumnComboViewer;
      String sortCol = StringUtils.defaultString(
         df.getSortedColumnName());
      //First load combo with available columns
      List<String> available = new ArrayList<String>();
      for(PSDisplayColumn col : 
         (List<PSDisplayColumn>)m_columnTable.getValues())
      {
         available.add(col.getSource());
      }
      boolean alreadyBlocked = m_editor.m_blockControlListeners;
      // Block listeners so that the dirty flag does not get set
      if(!alreadyBlocked)
         m_editor.blockRegisteredControlListeners(true);
      combo.setInput(available);
      if(available.contains(sortCol))
      {
         // Selection still exists in available list so set
         // the selection first then unblock the listeners
         combo.setSelection(new StructuredSelection(sortCol));
         m_editor.blockRegisteredControlListeners(alreadyBlocked);
      }
      else
      {
         // Selection does not exist in available list so unblock
         // the listeners first then select sys_title as the sort
         // field
         m_editor.blockRegisteredControlListeners(alreadyBlocked);
         combo.setSelection(new StructuredSelection(
            PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD));
      }
      
   }
   
   
   /**
    * Brings up the search field editor dialog to allow selection of new
    * columns.
    * 
    * @param postAction The <code>execute</code> method will be called after
    * the dialog has been dismissed if the dialog's <code>isOK</code> method
    * returns <code>true</code>. It will be called in the context of the SWT
    * UI thread.
    * 
    * @return <code>true</code> if new columns were added or deleted,
    * <code>false</code> otherwise.
    */
   private boolean onAddColumns(final Runnable postAction)
   {
      final Boolean[] results = new Boolean[1];
      BusyIndicator.showWhile(getDisplay(), new Runnable()
      {
         public void run()
         {
            final AwtSwtModalDialogBridge bridge = new AwtSwtModalDialogBridge(
                  PSDisplayFormatColumsTablesComposite.this.getShell());
            final PSContentEditorFieldCataloger fieldCatalog =
                  PSEditorUtil.getCEFieldCatalog(true);
            
            SwingUtilities.invokeLater(new Runnable()
            {
               @SuppressWarnings("synthetic-access")//$NON-NLS-1$
               public void run()
               {
                  try
                  {
                     final PSFieldSelectionEditorDialog dlg =
                           new PSFieldSelectionEditorDialog((Frame) null,
                                 m_displayFormat, fieldCatalog, true);
                     dlg.addValueChangedListener(new ValueChangedListener(
                           PSDisplayFormatColumsTablesComposite.this));
                     bridge.registerModalSwingDialog(dlg);
                     dlg.setVisible(true);
                     if (dlg.isOk())
                     {
                        getDisplay().asyncExec(new Runnable()
                        {
                           public void run()
                           {                  
                              postAction.run();
                           }            
                        });
                     }
                     
                  }
                  catch (Exception e)
                  {
                     PSDlgUtil.showError(e);
                  }
               }
            });

         }
      });
      if (results[0] != null)
         return results[0].booleanValue();
      
      return false;
   }
   
  
   /**
    * Moves all items from the category table to the column table.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   protected void onAddAll()
   {
      List<PSDisplayColumn> all = 
         (List<PSDisplayColumn>)m_categoryTable.getValues();
      PSDFColumns columns = m_displayFormat.getColumnContainer();
      if(!all.isEmpty())
      {
         for(PSDisplayColumn col : all)
         {
            col.setGroupingType(PSDisplayColumn.GROUPING_FLAT);
            columns.remove(col);
            columns.add(col);
         }
         updateTables();
      }      
      
   }

   /**
    * Moves selected items from the column table to the category
    * table.
    */
   protected void onRemove()
   {
      int[] selections = m_columnTable.getTable().getSelectionIndices();
      if(selections.length > 0)
      {
         moveColumnsToTable(selections, false);   
      }       
   }

   /**
    * Moves selected items from the category table to the
    * column table.
    */
   protected void onAdd()
   {
      int[] selections = m_categoryTable.getTable().getSelectionIndices();
      if(selections.length > 0)
      {
         moveColumnsToTable(selections, true);   
      }      
   }

   /**
    * Deletes selected items from the table with the selection if
    * the deletion is allowed.
    */
   protected void onDelete()
   {
      Table catTable = m_categoryTable.getTable();
      Table colTable = m_columnTable.getTable();
      int catTableSelectCount = catTable.getSelectionCount();
      int colTableSelectCount = colTable.getSelectionCount();
      PSDFColumns columns = m_displayFormat.getColumnContainer();
            
      if(catTableSelectCount > 0 && canBeDeleted(
         catTable.getSelectionIndices(), m_categoryTable))
      {
        int[] index = catTable.getSelectionIndices();        
        for(int idx : index)
        {
           PSDisplayColumn col = 
              (PSDisplayColumn)catTable.getItem(idx).getData();
           columns.remove(col);
        }
        updateTables();
      }
      else if(colTableSelectCount > 0 && canBeDeleted(
         colTable.getSelectionIndices(), m_columnTable))
      {
         int[] index = colTable.getSelectionIndices();        
         for(int idx : index)
         {
            PSDisplayColumn col = 
               (PSDisplayColumn)colTable.getItem(idx).getData();
            columns.remove(col);
         }
         updateTables();
      }
      
   }
   
   /**
    * Moves the selected item down one in the selected item's table
    */
   protected void onDown()
   {      
      int catTableSelectCount = m_categoryTable.getTable().getSelectionCount();
      int colTableSelectCount = m_columnTable.getTable().getSelectionCount();
      if(catTableSelectCount == 1)
      {
         moveUpDown(m_categoryTable, false);
      }
      else if (colTableSelectCount == 1)
      {
         moveUpDown(m_columnTable, false);
      }
   }

   /**
    * Moves the selected item up one in the selected item's table
    */
   protected void onUp()
   {
      int catTableSelectCount = m_categoryTable.getTable().getSelectionCount();
      int colTableSelectCount = m_columnTable.getTable().getSelectionCount();
      if(catTableSelectCount == 1)
      {
         moveUpDown(m_categoryTable, true);
      }
      else if (colTableSelectCount == 1)
      {
         moveUpDown(m_columnTable, true);
      }
   }
   
   /**
    * Utility method to move items up and down in the category and
    * column tables.
    * @param table assumed not <code>null</code>.
    * @param moveUp if <code>true</code> then we are moving
    * the item in an upward direction.
    */
   private void moveUpDown(PSSortableTable table, boolean moveUp)
   {
      PSDFColumns columns = m_displayFormat.getColumnContainer();
      if(table.getTable().getSelectionCount() != 1)
         return;
      int index = table.getTable().getSelectionIndex();
      // Get the column
      PSDisplayColumn col = 
         (PSDisplayColumn)table.getTable().getItem(index).getData();
      PSDisplayColumn moveToCol = null;
      if(moveUp)
      {
         if(index == 0)
            return;         
         // Get the column just above the selected one
         moveToCol = 
            (PSDisplayColumn)table.getTable().getItem(index - 1).getData();         
      }
      else
      {
         if(index == (table.getTable().getItemCount() - 1))
            return;                
         // Get the column just below the selected one
         moveToCol = 
            (PSDisplayColumn)table.getTable().getItem(index + 1).getData();         
      }
      // Get the move to location in the columns container
      int pos = columns.indexOf(moveToCol);
      // Remove the column to be moved
      columns.remove(col);
      // Insert the column at the above position
      columns.add(pos, col);
      updateTables();
   }
   
   /**
    * 
    * @param selections
    * @param toColumns
    */
   private void moveColumnsToTable(int[] selections, boolean toColumns)
   {
      PSDFColumns columns = m_displayFormat.getColumnContainer();
      if(toColumns)
      {
         for(int index : selections)
         {
            PSDisplayColumn col = 
               (PSDisplayColumn)m_categoryTable.getValues().get(index);
            col.setGroupingType(PSDisplayColumn.GROUPING_FLAT);
            columns.remove(col);
            columns.add(col);
         }
      }
      else
      {
         for(int index : selections)
         {
            PSDisplayColumn col = 
               (PSDisplayColumn)m_columnTable.getValues().get(index);
            if(col.getSource().equalsIgnoreCase(
               PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD))
            {
               String msg = ""; //$NON-NLS-1$
               new PSErrorDialog(getShell(), msg).open();
            }
            else
            {
               col.setGroupingType(PSDisplayColumn.GROUPING_CATEGORY);
               col.setWidth(-1); // set width to default
               columns.remove(col);
               columns.add(col);
            }
         }
      }
      updateTables();
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unused")  //$NON-NLS-1$
   public void updateDesignerObject(Object designObject, Object control)
   {
      // no-op, as object is updated as user does actions      
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   @SuppressWarnings("unused")  //$NON-NLS-1$
   public void loadControlValues(Object designObject)
   {      
      updateTables();
   }
   
   /**
    * @return item count in category table
    */
   public int getCategoryCount()
   {
      return m_categoryTable.getValues().size();
   }
   
   /**
    * @return item count in column table
    */
   public int getColumnCount()
   {
      return m_columnTable.getValues().size();
   }
   
  

   /**
    * A dummy new row object provider that does nothing.
    */ 
   class DummyNewRowProvider implements IPSNewRowObjectProvider
   {

      public Object newInstance()
      {
         return null;
      }

      public boolean isEmpty(@SuppressWarnings("unused") Object obj) //$NON-NLS-1$
      {
         return false;
      }      
   }
   
   /**
    * Label provider for both tables
    */
   class ColumnTableLabelProvider extends PSAbstractTableLabelProvider
   {

      public String getColumnText(Object element, int columnIndex)
      {
         PSDisplayColumn column = (PSDisplayColumn)element;
         switch(columnIndex)
         {
            case 0:
               return StringUtils.defaultString(column.getDisplayName());
            case 1:
               return StringUtils.defaultString(column.getSource());
            case 2:
               return StringUtils.defaultString(
                  column.getWidth() > -1 ? String.valueOf(column.getWidth()) : ""); //$NON-NLS-1$
         }
         return ""; //$NON-NLS-1$
      }
      
   }
   
   @SuppressWarnings("synthetic-access") //$NON-NLS-1$
   class ColumnTableCellModifier implements ICellModifier
   {

      protected ColumnTableCellModifier(PSSortableTable table)
      {         
         mi_table = table;  
      }
      
      public boolean canModify(@SuppressWarnings("unused") Object element, //$NON-NLS-1$
         String property)
      {
         int index = mi_table.getColumnIndex(property);
         if(m_supportWidth && index == 2)
         {
            return true;
         }
         return false;
      }

      public Object getValue(Object element, String property)
      {
         int index = mi_table.getColumnIndex(property);
         if(index == 2)
         {
            PSDisplayColumn col = (PSDisplayColumn)element;
            return col.getWidth() > 0 ? String.valueOf(col.getWidth()) : ""; //$NON-NLS-1$
         }
         return ""; //$NON-NLS-1$
      }

      public void modify(Object element, String property, Object value)
      {
         int index = mi_table.getColumnIndex(property);
         if(index != 2)
            return;
         if(element instanceof Item)
            element = ((Item)element).getData();
         PSDisplayColumn col = (PSDisplayColumn)element;
         if(value == null)
         {
            col.setWidth(-1);
         }
         else
         {
            try
            {
               int width = Integer.parseInt((String)value);
               col.setWidth(width > 0 ? width : -1);
            }
            catch(NumberFormatException nfe)
            {
               col.setWidth(-1);
            }
         }
         mi_table.refreshTable();
         
      }
      
      private PSSortableTable mi_table;
      
   }
   
   
   /**
    * Value change listener to listen for changes coming from
    * the search editor dialog and to then update the tables
    * when a change occurs.
    */
   class ValueChangedListener implements IPSValueChangedListener
   {
      ValueChangedListener(PSDisplayFormatColumsTablesComposite comp)
      {
         mi_comp = comp;
      }

     
      public void valueChanged(@SuppressWarnings("unused") PSValueChangedEvent e) //$NON-NLS-1$
      {
         Display display = mi_comp.getDisplay();
         display.asyncExec(new Runnable()
            {
               @SuppressWarnings("synthetic-access") //$NON-NLS-1$
               public void run()
               {                  
                  updateTables();
               }            
            });
      }
      
      private PSDisplayFormatColumsTablesComposite mi_comp; 
      
   }
   
   class DeselectorFocusListener extends FocusAdapter
   {
      /* 
       * @see org.eclipse.swt.events.FocusAdapter#focusGained(
       * org.eclipse.swt.events.FocusEvent)
       */
      @Override
      public void focusGained(FocusEvent e)
      {
         Object source = e.getSource();
         Table catTable = m_categoryTable.getTable();
         Table colTable = m_columnTable.getTable();
         if(source == catTable)
         {
            colTable.deselectAll();
         }
         else if(source == colTable )
         {
            catTable.deselectAll();
         }
      }     
      
   }
   
   class HandleButtonEnablingSelectionListener extends SelectionAdapter
   {

      /* 
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
       * org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
      {
         handleButtonsEnable();
      }
      
   }
   
   private Button m_addColumnsButton;
   private Button m_addAllButton;
   private Button m_removeButton;
   private Button m_addButton;
   private Button m_deleteButton;
   private Button m_downButton;
   private Button m_upButton;
   private Composite m_buttonComp;
   private Label m_columnsLabel;
   private Label m_categoriesLabel;
   private PSSortableTable m_categoryTable;
   private PSSortableTable m_columnTable;
   
   /**
    * List of all selection listeners registered to this control.
    */
   private List<SelectionListener> m_selectionListeners = 
      new ArrayList<SelectionListener>();
   
   
   
   private PSEditorBase m_editor;
   private PSDisplayFormat m_displayFormat;
   private boolean m_supportWidth;
   
  
   
  
   

}

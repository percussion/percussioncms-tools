/******************************************************************************
 *
 * [ PSSortableTable.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.layouts.IPSTableLayoutListener;
import com.percussion.workbench.ui.layouts.PSAutoResizeTableLayout;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_HEIGHT;
import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_WIDTH;

/**
 * The sortable table should really be known as the general table as
 * it has many features. It handles Inserting, deleting and moving of
 * rows. It handles moving the cell cursor via keyboard or mouse.
 * It allows columns to be sorted. It can even display buttons and context
 * menus for the allowed operations.
 */
public class PSSortableTable extends Composite implements ISelectionProvider
{
   /**
    * Ctor
    * @param parent
    * @param labelProvider
    * @param rowObjectProvider
    * @param style
    * @param options
    */
   public PSSortableTable(
      Composite parent,
      ITableLabelProvider labelProvider,
      IPSNewRowObjectProvider rowObjectProvider,      
      int style,
      int options)
   {
      super(parent, SWT.NONE);
      if(labelProvider == null)
         throw new IllegalArgumentException("Label provider must be provided");
      if(rowObjectProvider == null)
         throw new IllegalArgumentException(
            "New row object provider provider must be provided");
      setLayout(new FormLayout());
      m_rowObjectProvider = rowObjectProvider;
      m_options = options;
      
      final boolean showAll = (options & SHOW_ALL) != 0;
      final boolean showUp = (options & SHOW_MOVE_UP) != 0;
      final boolean showDn = (options & SHOW_MOVE_DOWN) != 0;
      final boolean showIns = (options & SHOW_INSERT) != 0;
      final boolean showDel = (options & SHOW_DELETE) != 0;
      final boolean allowDelete = (options & DELETE_ALLOWED) != 0;
      final boolean allowInsert = isInsertAllowed();
      final boolean hideHeader = (options & HIDE_HEADER) != 0;
      final boolean hideLines = (options & HIDE_LINES) != 0;
      final boolean useCursor = (options & SURPRESS_TABLE_CURSOR) == 0;
      final boolean allowManualSort = (options & SURPRESS_MANUAL_SORT) == 0;
      
      loadImages();
      
      Control lastControl = null;
      if(style == SWT.NONE)
         style = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER;
      
      // Add the table viewer first so it gets first focus on first tab
      m_tableViewer = new TableViewer(
         this, style);
      
            
      if((showAll || showUp) && allowManualSort)
      {
         m_moveUpButton = PSButtonFactory.createUpButton(this);         
         registerButton(m_moveUpButton, TYPE_MOVE_UP);
         final FormData formData_1 = new FormData();
         formData_1.width = BUTTON_WIDTH;
         formData_1.height = BUTTON_HEIGHT;
         formData_1.right = new FormAttachment(100, 0);
         formData_1.top = new FormAttachment(0, 0);
         m_moveUpButton.setLayoutData(formData_1);
         m_moveUpButton.setToolTipText(
            PSMessages.getString(
               "PSSortableTableComposite.label.moveup")); //$NON-NLS-1$
         lastControl = m_moveUpButton;
      }

      if((showAll || showDn) && allowManualSort)
      {
         m_moveDownButton = PSButtonFactory.createDownButton(this);         
         registerButton(m_moveDownButton, TYPE_MOVE_DN);
         final FormData formData_2 = new FormData();
         formData_2.width = BUTTON_WIDTH;
         formData_2.height = BUTTON_HEIGHT;
         if(lastControl == null)
         {
            formData_2.top = new FormAttachment(0,0);
         }
         else
         {
            formData_2.top = 
               new FormAttachment(lastControl, 
                  BUTTON_VSPACE_OFFSET, SWT.BOTTOM);
         }
         formData_2.right = new FormAttachment(100, 0);
         m_moveDownButton.setLayoutData(formData_2);
         m_moveDownButton.setToolTipText(
            PSMessages.getString(
               "PSSortableTableComposite.label.movedown")); //$NON-NLS-1$
         lastControl = m_moveDownButton;
      }
      
      if((showAll || showIns) && allowInsert)
      {
         m_insertButton = PSButtonFactory.createAddButton(this);
         registerButton(m_insertButton, TYPE_INSERT);
         final FormData formData_3 = new FormData();
         formData_3.width = BUTTON_WIDTH;
         formData_3.height = BUTTON_HEIGHT;
         formData_3.right = new FormAttachment(100, 0);
         if(lastControl == null)
         {
            formData_3.top = new FormAttachment(0,0);
         }
         else
         {
            formData_3.top = 
               new FormAttachment(lastControl, 
                  BUTTON_VSPACE_OFFSET, SWT.BOTTOM);
         }
         m_insertButton.setLayoutData(formData_3);
         
         m_insertButton.setToolTipText(
            PSMessages.getString(
               "PSSortableTableComposite.label.insert")); //$NON-NLS-1$
              
         lastControl = m_insertButton;
      }
      
      if((showAll || showDel) && allowDelete)
      {
         m_deleteButton = PSButtonFactory.createDeleteButton(this); 
         registerButton(m_deleteButton, TYPE_DELETE);
         final FormData formData_4 = new FormData();
         formData_4.width = BUTTON_WIDTH;
         formData_4.height = BUTTON_HEIGHT;
         formData_4.right = new FormAttachment(100, 0);
         if(lastControl == null)
         {
            formData_4.top = new FormAttachment(0,0);
         }
         else
         {
            formData_4.top = 
               new FormAttachment(lastControl, 
                  BUTTON_VSPACE_OFFSET, SWT.BOTTOM);
         }
         m_deleteButton.setLayoutData(formData_4);
         m_deleteButton.setToolTipText(
            PSMessages.getString(
               "PSSortableTableComposite.label.delete")); //$NON-NLS-1$
         lastControl = m_deleteButton;
      }
      
      m_tableViewer.setContentProvider(new PSDefaultContentProvider());
      m_tableViewer.setLabelProvider(labelProvider);
      m_tableViewer.setSorter(new DefaultSorter());
      m_tableViewer.setInput(new ArrayList());
      m_table = m_tableViewer.getTable();
            
      // Add listener to disable up, down, insert buttons on
      // multi-select
      m_table.addSelectionListener(new SelectionAdapter() {
         @Override
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
         {            
            m_lastRowSelectionFired = !(m_table.getSelectionCount() == 1 &&
               ((m_table.getSelectionIndex() + 1) == m_table.getItemCount()));
            if(isTableCursorInUse())
            {
               if(m_cursor.getColumn() != m_lastColumnSelected)
               {
                  fireColumnSelectionEvent();
                  m_lastColumnSelected = m_cursor.getColumn();
               }
               //Sync selection between table and cursor if single selection
               if(m_table.getSelectionCount() == 1)
               {
                  int cRow = m_table.indexOf(m_cursor.getRow());
                  int tRow = m_table.getSelectionIndex();
                  if(cRow != tRow)
                  {
                     m_cursor.setSelection(tRow,
                        Math.max(0, m_cursor.getColumn()));
                  }
               }
            }
            updateButtonStatus(null);
            fireSelectionEvent();
         }
      });
     
     
      m_table.addKeyListener(new KeyAdapter()
         {               
         @Override
         public void keyPressed(KeyEvent e)
         {               
            handleTableKeyPress(e);
         }             
         
         });
      
      // Handle inserting on click
      if(allowInsert)
      {
         m_table.addListener(SWT.MouseUp, new Listener()
            {
            @SuppressWarnings("synthetic-access")
            public void handleEvent(Event event)
            {               
               if(event.button > 1)
                  return;
               Rectangle bounds = PSSortableTable.this.getActiveBounds();
               boolean inActiveArea = bounds.contains(event.x, event.y);
               //Only insert if not in the active row area
               if(!inActiveArea)
               {                  
                  doInsert(true);
               }
            }            
            }); 
      }
      
      // Add mouse listener to enable edit mode upon double click
      m_table.addMouseListener(new MouseAdapter()
         {
        
         /* 
          * @see org.eclipse.swt.events.MouseAdapter#mouseDown(
          * org.eclipse.swt.events.MouseEvent)
          */
         @SuppressWarnings("synthetic-access")
         @Override
         public void mouseDown(MouseEvent e)
         {
            if(e.button > 1)
               return;
            // Capture the mouse down time so we can simulate
            // double click in the table cursor if
            // appropriate.
            if(getActiveBounds().contains(e.x, e.y))
               m_tableMouseDownTime = new Date().getTime();
         }             
         
         
         });
     
     
      
      // Use the auto resize layout
      PSAutoResizeTableLayout tableLayout = 
         new PSAutoResizeTableLayout(m_table);
      m_table.setLayout(tableLayout);
      m_table.setLinesVisible(!hideLines);
      m_table.setHeaderVisible(!hideHeader);
      if(useCursor)
      {
         // Add table cursor to handle navigating the table via
         // the keyboard      
         m_cursor = new TableCursor(m_table, SWT.NONE);
         
         m_cursor.addSelectionListener(new SelectionAdapter()
            {
            
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access")
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               
               if(m_cursor.getColumn() != m_lastColumnSelected)
               {
                  fireColumnSelectionEvent();
                  m_lastColumnSelected = m_cursor.getColumn();
               }
               int currentSelection = m_table.indexOf(m_cursor.getRow());
               // We need to know if a last row selection was fired or not. We
               // use this knowledge as a sort of hack to figure out if we should append
               // a new row to the table.
               m_lastRowSelectionFired = 
                  ((currentSelection + 1) == m_table.getItemCount());
               updateButtonStatus(currentSelection);
            }
            
            });
         
         // Add mouse listener to enable edit mode upon double click
         m_cursor.addMouseListener(new MouseAdapter()
            {
            
            /* 
             * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(
             * org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseDoubleClick(@SuppressWarnings("unused") MouseEvent e)
            {               
               final int col = m_cursor.getColumn();
               CellEditor cEditor = getCellEditor(col);
               if(cEditor != null && cEditor instanceof CheckboxCellEditor)
                  return;
               editCurrentColumn();
            }

            /* 
             * @see org.eclipse.swt.events.MouseAdapter#mouseDown(
             * org.eclipse.swt.events.MouseEvent)
             */
            @SuppressWarnings("synthetic-access")
            @Override
            public void mouseDown(MouseEvent e)
            {
               if(e.button > 1)
                  return;
               final int col = m_cursor.getColumn();
               CellEditor cEditor = getCellEditor(col);
               boolean forceEdit = 
                  (cEditor != null && cEditor instanceof CheckboxCellEditor);
               // We simulate double click here as the
               // table gets the first click then the cursor
               // is moved then it gets the second click
               long now = new Date().getTime();
               if(forceEdit ||(now - m_tableMouseDownTime) < 250)
               {
                  editCurrentColumn();
               }
            }
           
            
            });
         // Hide the TableCursor when the user hits the "CTRL" or "SHIFT" key.
         // This alows the user to select multiple items in the table.
         m_cursor.addKeyListener(new KeyAdapter()
            {
               @Override
               @SuppressWarnings("synthetic-access")
               public void keyPressed(KeyEvent e)
               {
                  handleTableCursorKeyPress(e);               
               }            
            
            });
         
         m_cursor.addFocusListener(new FocusAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.FocusAdapter#focusGained(
                * org.eclipse.swt.events.FocusEvent)
                */
               @Override
               public void focusGained(@SuppressWarnings("unused") FocusEvent e)
               {
                  fireFocusEvent(true);
               }

               /* 
                * @see org.eclipse.swt.events.FocusAdapter#focusLost(
                * org.eclipse.swt.events.FocusEvent)
                */
               @Override
               public void focusLost(@SuppressWarnings("unused") FocusEvent e)
               {
                  fireFocusEvent(false);
               }
               
            });
      }
      
      // Show the TableCursor when the user releases the "SHIFT" or "CTRL" key.
      // This signals the end of the multiple selection task.
      m_table.addKeyListener(new KeyAdapter() {
         @Override
         public void keyReleased(KeyEvent e) {
            if (e.keyCode == SWT.CONTROL && (e.stateMask & SWT.SHIFT) != 0) return;
            if (e.keyCode == SWT.SHIFT && (e.stateMask & SWT.CONTROL) != 0) return;
            if (e.keyCode != SWT.CONTROL && (e.stateMask & SWT.CONTROL) != 0) return;
            if (e.keyCode != SWT.SHIFT && (e.stateMask & SWT.SHIFT) != 0) return;
            
            if(!getValues().isEmpty())
            {
               TableItem[] selection = m_table.getSelection();
               TableItem row = (selection.length == 0) 
                  ? m_table.getItem(m_table.getTopIndex()) : selection[0];
               m_table.showItem(row);
               if(m_cursor != null)
                  m_cursor.setSelection(row, m_cursor.getColumn());
            }
            if(m_cursor != null)
            {
               m_cursor.setVisible(true);
               m_cursor.setFocus();
            }
         }
      });
      
      final FormData formData = new FormData();
      formData.bottom = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      if(lastControl == null)
      {
         formData.right = new FormAttachment(100, 0);
      }
      else
      {
         formData.right = new FormAttachment(lastControl, -TABLE_BUTTON_HSPACE,
            SWT.LEFT);
      }
      formData.left = new FormAttachment(0, 0);
      m_table.setLayoutData(formData);
      // Add focus listener so that tabbing works correctly
      // with cursor
      m_table.addFocusListener(new FocusAdapter()
         {       
         
         /* 
          * @see org.eclipse.swt.events.FocusAdapter#focusLost(
          * org.eclipse.swt.events.FocusEvent)
          */
         @SuppressWarnings("synthetic-access")
         @Override
         public void focusLost(@SuppressWarnings("unused") FocusEvent e)
         {
            if(!isTableCursorInUse())
               fireFocusEvent(false);
         }
         
         /* 
          * @see org.eclipse.swt.events.FocusAdapter#focusGained(
          * org.eclipse.swt.events.FocusEvent)
          */
         @SuppressWarnings("synthetic-access")
         @Override
         public void focusGained(@SuppressWarnings("unused") FocusEvent e)
         {
            if (getValues().isEmpty())
            {
               if (isInsertAllowed())
               {
                  doInsert(true);
                  if(m_cursor != null)
                  {
                     m_cursor.setSelection(0, 0);
                     m_cursor.setFocus();
                  }
               }
            }
            else if(m_cursor != null)
            {
               // set selection later, otherwise we risk to interfere with
               // processing of the mouse click which triggered this focus event 
               maybeMakeLaterDefaultSelection();
            }            
            if(!isTableCursorInUse())
               fireFocusEvent(true);
         }
         });
      createContextMenu();
      updateButtonStatus(null);
   }

   /**
    * After some delay asynchroniously selects table line 0 if
    * there is no selection yet and table is empty.
    * This is done to work around situation when making the selection
    * right away interferes with processing of later events.
    */
   private void maybeMakeLaterDefaultSelection()
   {
      new Thread(new Runnable()
      {
         public void run()
         {
            try
            {
               Thread.sleep(10);
            }
            catch (InterruptedException ignore)
            {
            }
            getDisplay().asyncExec(new Runnable()
            {
               public void run()
               {
                  if(m_table.getSelectionCount() == 0
                        && m_table.getItemCount() > 0)
                     m_cursor.setSelection(0, 0);
                  m_cursor.setFocus();
               }
            });
         }
      }).start();
   }

   /**
    * Initializes table images.
    */
   private void loadImages()
   {
      {
         final ImageDescriptor desc =
            PSUiUtils.getImageDescriptorFromIconsFolder(
               IPSUiConstants.IMAGE_TRIANGLE_UP);
         if (desc != null)
         {
            m_ascendingImage = desc.createImage();
         }
      }
      {
         final ImageDescriptor desc = 
            PSUiUtils.getImageDescriptorFromIconsFolder(
               IPSUiConstants.IMAGE_TRIANGLE_DOWN);
         if (desc != null)
         {
            m_descendingImage = desc.createImage();
         }
      }
   }
   
   /**
    * Creates image from shared descriptor.
    * @param descriptorName image id from {@link ISharedImages}.
    * Assumed not <code>null</code> or empty.
    */
   protected Image createImageFromSharedDescriptor(final String descriptorName)
   {
      assert StringUtils.isNotBlank(descriptorName);
      return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
            descriptorName).createImage();
   }

   /**
    * Edits the currently selected row and column as indicated in
    * the table cursor.
    */
   protected void editCurrentColumn()
   {
      if (!canModifySelected())
      {
         return;
      }

      final int col = m_cursor.getColumn();
      final TableItem row = m_cursor.getRow();
      getCellEditor(col).addListener(
            new TableCursorCellEditorListener(getCellEditor(col), row, col));
      m_cursor.setVisible(false);
      m_tableViewer.editElement(row.getData(), col);
   }
   
   /**
    * Returns <code>true</code> if currently selected cell is modifiable.
    */
   private boolean canModifySelected()
   {
      ICellModifier modifier = m_tableViewer.getCellModifier();
      if (m_cursor == null || modifier == null)
      {
         return false;
      }

      final int col = m_cursor.getColumn();
      final TableItem row = m_cursor.getRow();
      return getCellEditor(col) != null
            && modifier.canModify(row.getData(), getColumnProperty(col));
   }
   
   /**
    * Utility method that indicates that the table cursor is
    * in use.
    * @return <code>true</code> if in use.
    */
   protected boolean isTableCursorInUse()
   {
      if(m_cursor == null)
         return false;
      return m_cursor.isVisible();
   }
   
   
   /**
    * Helper method that handles the various key presses events
    * for the table.
    * @param e the key event, cannot be <code>null</code>
    */
   protected void handleTableKeyPress(KeyEvent e)
   {
      final boolean allowDelete = (m_options & DELETE_ALLOWED) != 0;
      
      // Handle delete key when table cursor is not visible
      if(e.character == SWT.DEL && !isTableCursorInUse())
      {                  
         if(allowDelete)
            doDelete();
      }
      // Handle ctrl-A
      else if(((e.stateMask & SWT.CONTROL) != 0 || 
         (e.stateMask & SWT.CTRL) != 0) &&
         (e.keyCode == 97))
      {
         m_table.selectAll();
         updateButtonStatus(null);
      }
      // Handle ctrl-I
      else if(((e.stateMask & SWT.CONTROL) != 0 || 
         (e.stateMask & SWT.CTRL) != 0) &&
         (e.keyCode == 105))
      {
         doInsert(false);
      }
      // Handle ctrl-D
      else if(((e.stateMask & SWT.CONTROL) != 0 || 
         (e.stateMask & SWT.CTRL) != 0) &&
         (e.keyCode == 100))
      {
         doMoveDown();
      }
      // Handle ctrl-U
      else if(((e.stateMask & SWT.CONTROL) != 0 || 
         (e.stateMask & SWT.CTRL) != 0) &&
         (e.keyCode == 117))
      {
         doMoveUp();
      }            
   }
   
   /**
    * Helper method that handles the various key presses events
    * for the table cursor.
    * @param e the key event, cannot be <code>null</code>
    */
   protected void handleTableCursorKeyPress(KeyEvent e)
   {
      final boolean allowDelete = (m_options & DELETE_ALLOWED) != 0;
      if (e.keyCode == SWT.CTRL || 
         e.keyCode == SWT.SHIFT || 
         (e.stateMask & SWT.CONTROL) != 0 || 
         (e.stateMask & SWT.SHIFT) != 0)
      {
         m_cursor.setVisible(false);
      }            
      // Edit cell when F2 key is hit
      else if(e.keyCode == SWT.F2)
      {
         List input = (List)m_tableViewer.getInput();
         if(input.isEmpty() || !canModifySelected())
         {
            return;
         }
         final TableItem row = m_cursor.getRow();
         final int col = m_cursor.getColumn();
         CellEditor cEditor = getCellEditor(col);
         m_cursor.setVisible(false);
         cEditor.addListener(new TableCursorCellEditorListener(cEditor, row, col));
         m_tableViewer.editElement(row.getData(), col);
      }
      // Handle Enter key
      else if(e.keyCode == ENTER_KEY)
      {
         scrollToNextColumn();
      }            
      //  Handle delete key
      else if(allowDelete && e.character == SWT.DEL)
      {
         TableItem row = m_cursor.getRow();
         if(row != null)
         {
            m_table.deselectAll();
            m_table.select(m_table.indexOf(row));
            doDelete();
            if(m_table.getItemCount() > 0)
            {
               m_cursor.setFocus();
            }
         }
      }
      else if(e.keyCode == SWT.ARROW_DOWN)
      {
         int current = m_table.indexOf(m_cursor.getRow());
         m_cursor.setVisible(false);
         if(current < m_table.getItemCount())
         {
            m_table.deselectAll();
            m_table.select(current);
            fireSelectionEvent();
         }
         if((current + 1) == m_table.getItemCount() &&
            !m_lastRowSelectionFired)
         {
            if(isInsertAllowed())
            {
               doInsert(true);
               m_table.deselectAll();
               m_table.select(current + 1);
               int newRow = m_table.getItemCount() - 1;
               m_cursor.setSelection(newRow, 0);
               fireSelectionEvent();
            }
         }
         m_lastRowSelectionFired = false;
         
      }
      else if(e.keyCode == SWT.ARROW_UP)
      {
         int current = m_table.indexOf(m_cursor.getRow());
         m_cursor.setVisible(false);
         
         if(current >= 0)
         {
            m_table.deselectAll();
            m_table.select(current);
            fireSelectionEvent();
         }
      }      
   }
   
   /**
    * Adds a new column to the table. This method also gets some meta data
    * that is used for layout, style and other things.
    * @param nameKey thekey for the name for the column, cannot be <code>null</code> or
    * empty. The name is then retrieved from the psmessages.properties file if
    * it exists, else the key is used for the name.
    * @param options various options can be set that change sorting behaviour,
    * column options available:
    * <p>
    * <pre>
    *   <table border="1">
    *      <tr><td><b>Option</b></td><td><b>Description</b></td></tr>
    *      <tr><td>IS_SORTABLE</td><td>Column can be sorted</td></tr>
    *      <tr><td>IS_NUMERIC</td><td>Column should be sorted by numeric</td></tr>
    *   </table>
    * </pre>
    * </p> 
    * @param layoutData layout data to help decide on the width for this column.
    * This can be a <code>ColumnPixelData</code> object or a 
    * <code>ColumnWeightData</code> object.
    * @param cellEditor the <code>CellEditor</code> to be used to edit items
    * in this column. To use this the <code>CellModifer</code> for this table 
    * viewer must allow editing for this column. May be <code>null</code>.
    * @param style The style for the created column, can be one of the 
    * following:
    * <p>
    * <pre>
    *    <li>SWT.CENTER</li>
    *    <li>SWT.LEFT</li>
    *    <li>SWT.RIGHT</li>
    * </pre>
    * </p>
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void addColumn(
      String nameKey, int options, ColumnLayoutData layoutData, 
      CellEditor cellEditor, int style)
   {
      PSAutoResizeTableLayout layout = 
         (PSAutoResizeTableLayout)m_table.getLayout();
      
      TableColumn tc = new TableColumn(m_table, style);
      tc.setText(PSMessages.getString(nameKey));
      layout.addColumnData(layoutData);
      m_tableColProps = (String[]) ArrayUtils.add(m_tableColProps, PSMessages.getString(nameKey));
      m_tableColPropsKeys = (String[]) ArrayUtils.add(m_tableColPropsKeys, nameKey);
      m_tableViewer.setColumnProperties(m_tableColProps);
      
      m_cellEditors = (CellEditor[])ArrayUtils.add(m_cellEditors, cellEditor);
      m_tableViewer.setCellEditors(m_cellEditors);
      m_colOptions.add(options);
      
      tc.addSelectionListener(new SelectionAdapter()
         {
            @Override
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void widgetSelected(SelectionEvent event)
            {
               TableColumn col = (TableColumn)event.getSource();
               DefaultSorter sorter = (DefaultSorter)m_tableViewer.getSorter();
               sorter.doSort(ArrayUtils.indexOf(m_tableColProps, col.getText()));
               refreshTable();
            }
         });            
   }
   
   /**
    * Forces a column to sort if it is allowed to 
    * sort. 
    * @param col the column index, must be a valid column index.
    * @param ascending if <code>true</code> then the sort will
    * be ascending, else if <code>false</code> then descending.
    */
   public void sortColumn(int col, boolean ascending)
   {
      if(col > getColumnCount() - 1)
         throw new IllegalArgumentException("Invalid column index.");
      DefaultSorter sorter = (DefaultSorter)m_tableViewer.getSorter();
      sorter.doSort(col, ascending);
      refreshTable();
   }
   
   /**
    * @return the number of columns in this table.
    */
   public int getColumnCount()
   {
      return m_colOptions.size();
   }
      
   /**
    * Sets the row values for this table. The values will appear in the table
    * in the same order as the list passed in.
    * @param values
    */
   @SuppressWarnings("unchecked")
   public void setValues(List values)
   {
      if(values == null)
         throw new IllegalArgumentException("values cannot be null.");
      m_tableViewer.setInput(new ArrayList(values));
   }
   
   /**
    * Retrieves the row values in the order they appear in the
    * table. 
    * @return only returns rows considered not empty by the associated
    * row object provider.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public List getValues()
   {
      List results = new ArrayList();
      if(m_tableViewer.getInput()!=null){
         for(Object obj : (List)m_tableViewer.getInput())
         {
            if(!m_rowObjectProvider.isEmpty(obj))
               results.add(obj);
         }
      }

      return results;   
   }  
   
   /**
    * Retrieves the row values in the order they appear in the
    * table including empty rows. In general this should only be used 
    * for special cases where empty rows are desired. 
    * @return all rows empty or not.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public List getAllValues()
   {
      return (List)m_tableViewer.getInput();   
   }  
   
   /**
    * Returns the current column width ratios. This information
    * is usually used for saving the table's column width state.
    * @return the ratios
    */
   public float[] getColumnWidthRatios()
   {
      PSAutoResizeTableLayout layout = 
         (PSAutoResizeTableLayout)m_table.getLayout();
      return layout.getColumnWidthRatios();
   }
   
   /**
    * Sets column width ratios of the table.
    * @param ratios cannot be <code>null</code> and
    * there must be an entry for each column.
    */
   public void setColumnWidthRatios(float[] ratios)
   {
      PSAutoResizeTableLayout layout = 
         (PSAutoResizeTableLayout)m_table.getLayout();
      layout.setColumnWidthRatios(ratios);
   }
   
   /**
    * Sets the cell modifier for this table
    * @param modifier the cell modifier, cannot be <code>null</code>.
    */
   public void setCellModifier(ICellModifier modifier)
   {
      if(modifier == null)
         throw new IllegalArgumentException(
            "Cell modifier cannot be null."); //$NON-NLS-1$
      m_tableViewer.setCellModifier(new CellModifierWrapper(modifier));      
   }
   
   
   /**
    * Moves the selected row up one row if it is not already
    * the first row. All other rows are shifted accordingly.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void doMoveUp()
   {
      if((m_options & SURPRESS_MANUAL_SORT) != 0)
         return;
     
      int count = m_table.getSelectionCount();
      if(count == 0 || (count > 1 && !isSelectionContiguous()))
         return;
      if(!preMoveUp())
         return;
      List data = (List)m_tableViewer.getInput();
      List items = new ArrayList(count);
      int[] selections = m_table.getSelectionIndices();
      if(selections[0] == 0)
         return;
      int index = selections[0] - 1;
      for(int i = count - 1; i >= 0; i--)
      {
         items.add(data.remove(selections[i]));
      }      
      for(int i = 0; i < count; i++)
      {
         data.add(index + i, items.get((count - 1) - i));
      }      
      refreshTable();
      if(m_table.getItemCount() > 0 && m_cursor != null)
         m_cursor.setSelection(m_table.getSelectionIndices()[0],
            m_cursor.getColumn());
      fireTableModifiedEvent();
   }
   
   /**
    * Moves the selected row down one row if it is not already
    * the last row. All other rows are shifted accordingly.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void doMoveDown()
   {
      if((m_options & SURPRESS_MANUAL_SORT) != 0)
         return;
      if(!preMoveDown())
         return;
      int count = m_table.getSelectionCount();
      if(count == 0 || (count > 1 && !isSelectionContiguous()))
         return;      
      List data = (List)m_tableViewer.getInput();
      List items = new ArrayList(count);
      int[] selections = m_table.getSelectionIndices();
      if(selections[count - 1] == data.size() - 1)
         return;
      int index = selections[0] + 1;
      for(int i = count - 1; i >= 0; i--)
      {
         items.add(data.remove(selections[i]));
      }      
      for(int i = 0; i < count; i++)
      {
         data.add(index + i, items.get((count - 1) - i));
      }      
      refreshTable();
      if(m_table.getItemCount() > 0 && m_cursor != null)
         m_cursor.setSelection(m_table.getSelectionIndices()[0],
            m_cursor.getColumn());
      fireTableModifiedEvent();
   }
   
   /**
    * Handles the insertion of a new table row in the table.
    * <p>
    * Behaviour:
    * <ul>
    * <li>If table is empty or no selection or the selection is this last
    * row in the table, then a new row is added to the end of the table</li>
    * <li>Only one empty row can be added to the end of the table</li>
    * <li>If the selection is on a row other then the last row, then the
    * selected row will be pushed down and the new row inserted in it's
    * place</li>
    * <li>The inserted row is always selected.</li>
    * </ul>
    * </p>
    * @param appendOnly if set to <code>true</code>, then the insert will
    * only be appended to the table and not inserted no matter what is 
    * selected.    
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void doInsert(boolean appendOnly) 
   {      
      if((m_options & INSERT_ALLOWED) == 0 || !m_insertButtonEnabled)
         return;      
      //if multi selection then it must be contiguous
      if(m_table.getSelectionCount() > 1 && !isSelectionContiguous())
         return;
      if(!preInsert())
         return;
      int selection = m_table.getSelectionIndices().length == 0
         ? -1
         : m_table.getSelectionIndices()[0];
      List data = (List) m_tableViewer.getInput();
      if(data == null)
      {
         data = new ArrayList();
         m_tableViewer.setInput(data);
      }
      Object newRow = m_rowObjectProvider.newInstance();
     
      final int newRowIdx;
      if(appendOnly || selection == -1 || ((selection + 1) == data.size()))
      {
         // Only allow one empty entry to be added to the end
         // of the list
         if(data.size() > 0)
         {
            if(m_rowObjectProvider.isEmpty(data.get(data.size() - 1)))
               return;
         }
         // Just append new entry to end of list
         data.add(newRow);
         newRowIdx = data.size() - 1;
      }
      else
      {
         // insert item at index
         data.add(selection, newRow);
         newRowIdx = selection;
      }
      
      refreshTable();         
      m_table.deselectAll();
      m_table.select(newRowIdx);
      if(isTableCursorInUse())
      {
         m_cursor.setVisible(false);
         m_cursor.setVisible(true);
         m_cursor.setSelection(newRowIdx, 0);
      }
      fireTableModifiedEvent();
      m_table.forceFocus();
      postInsert(newRow);
   }
   
   /**
    * Handles the deletion of all selected rows.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void doDelete()
   {
      if((m_options & DELETE_ALLOWED) == 0 || !m_deleteButtonEnabled)
         return;
      if(!preDelete())
         return;
      int selection[] = m_table.getSelectionIndices();
      if(selection.length == 0)
         return;
      int firstSelection = selection[0];
      List data = (List)m_tableViewer.getInput();
      //Reverse the array
      ArrayUtils.reverse(selection);
      for(int sel : selection)
      {
         data.remove(sel);
      }      
      refreshTable();      
      if(m_cursor != null)
      {
         int col = m_cursor.getColumn();
         // force a refresh on the cursor value
         m_cursor.setVisible(false);
         m_cursor.setVisible(true);
         if(m_table.getItemCount() > 0)
            m_cursor.setSelection(
               Math.min(m_table.getItemCount() - 1, firstSelection), col);
      }
      
      fireTableModifiedEvent();
   }
   
   /**
    * Allows a hook to do some action just before delete takes
    * place.
    * @return if <code>true</code> then allow the delete to occur
    */
   protected boolean preDelete()
   {
      return true;
   }
   
   /**
    * Allows a hook to do some action just before insert takes
    * place.
    * @return if <code>true</code> then allow the insert to occur
    */
   protected boolean preInsert()
   {
      return true;
   }
   
   /**
    * Allows a hook to some action just after insert takes place.
    * Only fires if something was actually inserted.
    * @param newObject the new object instance created during
    * the insert. 
    */
   protected void postInsert(Object newObject)
   {
      // no-op 
   }
   
   /**
    * Allows a hook to do some action just before move up takes
    * place.
    * @return if <code>true</code> then allow the move up to occur
    */
   protected boolean preMoveUp()
   {
      return true;
   }
   
   /**
    * Allows a hook to do some action just before move down takes
    * place.
    * @return if <code>true</code> then allow the move down to occur
    */
   protected boolean preMoveDown()
   {
      return true;
   }
   
   /**
    * Helper method to verify that the selection is contiguous.
    * @return <code>true</code> if selection is contiguous.
    */
   protected boolean isSelectionContiguous()
   {
      int[] selections = m_table.getSelectionIndices();
      if(selections.length == 0)
         return false;
      if(selections.length == 1)
         return true;
      int lastSelection = selections[0];
      for(int i = 1; i < selections.length; i++)
      {
         if((selections[i] - lastSelection) > 1)
            return false;
         lastSelection = selections[i];
      }
      return true;
      
   }
   
   /**
    * Refreshes the table viewer
    */
   public void refreshTable()
   {
      m_tableViewer.refresh();
   }
   
   /** 
    * @return the table contained in this composite. Never <code>null</code>.
    */
   public Table getTable()
   {
      return m_table;
   }
   
   /**
    * @return delete button from this composite. May be <code>null</code>, if
    * the table is created without delete button.
    */
   public Button getDeleteButton()
   {
      return m_deleteButton;
   }
   
   /** 
    * @return the table viewer contained in this composite. 
    * Never <code>null</code>.
    */
   public TableViewer getTableViewer()
   {
      return m_tableViewer;
   }
   
   /** 
    * Retrieves the property of the column by its index.
    * @param index the index of the column to retrieve the property
    * from.
    * @return the columns property.
    */
   public String getColumnProperty(int index)
   {
      if(index < 0 || index >= m_tableColProps.length)
         throw new IndexOutOfBoundsException();
      return m_tableColProps[index];
   }
   
   /** 
    * Retrieves the property name key of the column by its index.
    * @param index the index of the column to retrieve the property key
    * from.
    * @return the columns property name key.
    */
   public String getColumnPropertyKey(int index)
   {
      if(index < 0 || index >= m_tableColPropsKeys.length)
         throw new IndexOutOfBoundsException();
      return this.m_tableColPropsKeys[index];
   }
   
   /**
    * Retrieves the index of the column by its property.
    * @param property the column property, cannot be <code>null</code>
    * or empty.
    * @return the index or -1 if not found.
    */
   public int getColumnIndex(String property)
   {
      if(StringUtils.isBlank(property))
         throw new IllegalArgumentException(
            "property cannot be null or empty."); //$NON-NLS-1$
      return ArrayUtils.indexOf(m_tableColProps, property);
   }
   
   /**
    * Retrieves the cell editor for the specified column
    * @param index the column
    * @return the cell editor or <code>null</code> if not found.
    */
   public CellEditor getCellEditor(int index)
   {
      if( index < 0 || index >= m_cellEditors.length)
         throw new IndexOutOfBoundsException();
      return m_cellEditors[index];
   }
   
   /**
    * @return currently selected column index or -1 if none selected.
    */
   public int getSelectedColumn()
   {
      if(m_cursor != null && m_cursor.getVisible())
      {
         return m_cursor.getColumn();
      }
      return -1;
   }
   
   /**
    * Registers a button to be used for a specific action. This is most
    * useful for not displaying the buttons that are built into the control
    * , button  instead use external buttons to control those functions.
    * The buttons will also be enabled and disabled following the same rules
    * as the built in buttons. 
    * @param button the button to be registered, cannot be <code>null</code>.
    * @param type the type of button (action) that the passed in button
    * represents. The supported types are: 
    * <code>TYPE_MOVE_UP, TYPE_MOVE_DN, TYPE_INSERT, TYPE_DELETE</code>
    */
   @SuppressWarnings("synthetic-access")
   public void registerButton(Button button, int type)
   {
      if(button == null)
         throw new IllegalArgumentException("button cannot be null.");
      SelectionListener sl = null;
      switch(type)
      {
         case TYPE_MOVE_UP:
            sl = new SelectionAdapter()
            {              
               @Override
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  doMoveUp();
                  updateButtonStatus(null);
               }            
            };
            break;
         case TYPE_MOVE_DN:
            sl = new SelectionAdapter()
            {              
               @Override
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  doMoveDown();
                  updateButtonStatus(null);
               }            
            };
            break;
         case TYPE_INSERT:
            sl = new SelectionAdapter()
            {              
               @Override
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  doInsert(false);
                  updateButtonStatus(null);
               }            
            };
            break;
         case TYPE_DELETE:
            sl = new SelectionAdapter()
            {              
               @Override
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  doDelete();
                  updateButtonStatus(null);
               }            
            };
            break;
         default:
            throw new IllegalArgumentException("Unknown type.");
      }
      button.addSelectionListener(sl);
      Object[] obj = new Object[]{type, sl};
      m_buttonRegistry.put(button, obj);
      updateButtonStatus(null);
   }
   
   /**
    * Unregisters a button that was previously registered.
    * @param button the button to be unregistered, cannot be <code>null</code>.
    */
   public void unregisterButton(Button button)
   {
      if(button == null)
         throw new IllegalArgumentException("button cannot be null.");
      if(!m_buttonRegistry.containsKey(button))
         return;
      Object[] obj = m_buttonRegistry.get(button);
      button.removeSelectionListener((SelectionListener)obj[1]);
      m_buttonRegistry.remove(button);
   }
   
   /**
    * Add a table modified listener
    * @param listener cannot be <code>null</code>.
    */
   public void addTableModifiedListener(IPSTableModifiedListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_tableModListeners.contains(listener))
         m_tableModListeners.add(listener);
   }
   
   /**
    * Remove a table modified listener
    * @param listener cannot be <code>null</code>.
    */
   public void removeTableModifiedListener(IPSTableModifiedListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_tableModListeners.contains(listener))
         m_tableModListeners.remove(listener);
   }
   
   /**
    * Add a table column selection listener
    * @param listener cannot be <code>null</code>.
    */
   public void addTableColumnSelectionListener(
      IPSTableColumnSelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_columnSelectionListeners.contains(listener))
         m_columnSelectionListeners.add(listener);
   }
   
   /**
    * Remove a table column selection listener
    * @param listener cannot be <code>null</code>.
    */
   public void removeTableColumnSelectionListener(
      IPSTableColumnSelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_columnSelectionListeners.contains(listener))
         m_columnSelectionListeners.remove(listener);
   }
   
   /**
    * Adds the specified table layout listener
    * @param listener cannot be <code>null</code>.
    */
   public void addTableLayoutListener(IPSTableLayoutListener listener)
   {
      PSAutoResizeTableLayout layout = 
         (PSAutoResizeTableLayout)m_table.getLayout();
      layout.addTableLayoutListener(listener);
   }
   
   /**
    * Removes the specified table layout listener
    * @param listener cannot be <code>null</code>.
    */
   public void removeTableLayoutListener(IPSTableLayoutListener listener)
   {
      PSAutoResizeTableLayout layout = 
         (PSAutoResizeTableLayout)m_table.getLayout();
      layout.removeTableLayoutListener(listener);
   }
   
   /**
    * Notifies all registered table modified listeners of
    * a table modified event.
    */
   protected void fireTableModifiedEvent()
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;     
      PSTableEvent event = new PSTableEvent(e);
      for(IPSTableModifiedListener listener : m_tableModListeners)
      {
        listener.tableModified(event);
      }
      
   }
   
   /**
    * Notifies all registered table column selection listeners of
    * a table column selection event.
    */
   protected void fireColumnSelectionEvent()
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;     
      PSTableEvent event = new PSTableEvent(e);
      for(IPSTableColumnSelectionListener listener : m_columnSelectionListeners)
      {
        listener.columnSelected(event);
      }
      
   }
   
   /**
    * Handles scrolling the cursor to the next available column,
    * wrapping if needed and inserting a new row if needed.
    */
   private void scrollToNextColumn()
   {
      if(!isTableCursorInUse())
         return;
      int maxCol = m_table.getColumnCount() - 1;
      int cursorCol = m_cursor.getColumn();
      if(cursorCol < maxCol)
      {
         m_cursor.setSelection(m_cursor.getRow(), cursorCol + 1);
      }
      else
      {
         int row = m_table.indexOf(m_cursor.getRow());
         int lastRow = m_table.getItemCount() - 1;
         if(row < lastRow)
         {
            m_cursor.setSelection(row + 1, 0);
         }
         else
         {
            doInsert(true);
         }
      }
   }
   
   /**
    * Creates the tables context menu that will display insert, delete,
    * move up, and move down options.
    */
   protected void createContextMenu()
   {
      final boolean allowDelete = (m_options & DELETE_ALLOWED) != 0;
      final boolean allowInsert = isInsertAllowed();
      final boolean allowManualSort = (m_options & SURPRESS_MANUAL_SORT) == 0;
      final boolean suppressContext = (m_options & SURPRESS_CONTEXT_MENU) != 0;
      if(suppressContext)
         return;
      boolean hasMenuItem = false;
      Menu menu = new Menu(m_table);
      if(allowInsert)
      {
         m_insertMenuItem = new MenuItem(menu, SWT.NONE);
         m_insertMenuItem.setText("Insert");
         m_insertMenuItem.addSelectionListener(new SelectionAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
                * org.eclipse.swt.events.SelectionEvent)
                */
               @Override
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  doInsert(false);
               }
               
            });
         hasMenuItem = true;
      }
      
      if(allowDelete)
      {
         m_deleteMenuItem = new MenuItem(menu, SWT.NONE);
         m_deleteMenuItem.setText("Delete");
         m_deleteMenuItem.setImage(
               createImageFromSharedDescriptor(ISharedImages.IMG_TOOL_DELETE));
         m_deleteMenuItem.addSelectionListener(new SelectionAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
                * org.eclipse.swt.events.SelectionEvent)
                */
               @Override
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  doDelete();
               }
               
            });
         hasMenuItem = true;
      }
      
      if(allowManualSort)
      {
         m_upMenuItem = new MenuItem(menu, SWT.NONE);
         m_upMenuItem.setText("Move Up");
         m_upMenuItem.addSelectionListener(new SelectionAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
                * org.eclipse.swt.events.SelectionEvent)
                */
               @Override
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  doMoveUp();
               }
               
            });
         m_downMenuItem = new MenuItem(menu, SWT.NONE);
         m_downMenuItem.setText("Move Down");
         m_downMenuItem.addSelectionListener(new SelectionAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
                * org.eclipse.swt.events.SelectionEvent)
                */
               @Override
               public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
               {
                  doMoveDown();
               }
               
            });
         hasMenuItem = true;
      }
      if(hasMenuItem)
      {
         m_table.setMenu(menu);
         if(m_cursor != null)
            m_cursor.setMenu(menu);
      }
   }
  
   /* 
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   @Override
   public void dispose()
   {
      if(m_ascendingImage != null)
         m_ascendingImage.dispose();
      if(m_descendingImage != null)
         m_descendingImage.dispose();
      super.dispose();
   }

   /* 
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   @Override
   protected void checkSubclass()
   {
      //no-op
   }   
   
   /**
    * Updates buttons as to whether or not they should be enabled.
    * @param currentSelection the current selection index, if
    * <code>null</code> then the table is queried for all selections.
    */
   private void updateButtonStatus(Integer currentSelection)
   {
      if(m_table == null)
         return;
      List<Integer> selections = new ArrayList<Integer>(); 
      if(currentSelection != null)
      {
         selections.add(currentSelection);
      }
      else
      {
         if(m_table.getSelectionCount() > 0)
         {
            for(int index : m_table.getSelectionIndices())
               selections.add(index);
         }
      }      
      
      for(Button key : m_buttonRegistry.keySet())
      {
         Object[] obj = m_buttonRegistry.get(key);
         boolean enable = false;
         switch(((Integer)obj[0]).intValue())
         {
            case TYPE_MOVE_UP:
               enable = isSelectionContiguous() 
               && selections.get(0) > 0;
               key.setEnabled(enable);
               if(m_upMenuItem != null)
               m_upMenuItem.setEnabled(enable);
               break;
            case TYPE_MOVE_DN:
               enable = isSelectionContiguous() 
               && selections.get(0) < 
               (m_table.getItemCount() - 1);
               key.setEnabled(enable);
               if(m_downMenuItem != null)
                  m_downMenuItem.setEnabled(enable);
               break;
            case TYPE_INSERT:
               enable = (selections.size() == 0 ||
               isSelectionContiguous()) && m_insertButtonEnabled;
               key.setEnabled(enable);
               if(m_insertMenuItem != null)
                  m_insertMenuItem.setEnabled(enable);
               break;
            case TYPE_DELETE:
               enable = selections.size() > 0 && m_deleteButtonEnabled;
               key.setEnabled(enable);
               if(m_deleteMenuItem != null)
                  m_deleteMenuItem.setEnabled(enable);
               break;
         }
         
      }
      
   }
   
   /* 
    * @see org.eclipse.jface.viewers.ISelectionProvider#
    * addSelectionChangedListener(org.eclipse.jface.viewers.
    * ISelectionChangedListener)
    */
   public void addSelectionChangedListener(ISelectionChangedListener listener)
   {
      m_tableViewer.addSelectionChangedListener(listener);      
   }

   /* 
    * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
    */
   public ISelection getSelection()
   {
      return m_tableViewer.getSelection();
   }

   /* 
    * @see org.eclipse.jface.viewers.ISelectionProvider#
    * removeSelectionChangedListener(org.eclipse.jface.viewers.
    * ISelectionChangedListener)
    */
   public void removeSelectionChangedListener(ISelectionChangedListener listener)
   {
      m_tableViewer.removeSelectionChangedListener(listener);         
   }

   /* 
    * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(
    * org.eclipse.jface.viewers.ISelection)
    */
   public void setSelection(ISelection selection)
   {
      m_tableViewer.setSelection(selection);         
   } 
   
   /**
    * Add a selection listener to be notified when a selection
    * event occurs.
    * @param listener cannot be <code>null</code>.
    */
   public void addSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
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
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(m_selectionListeners.contains(listener))
         m_selectionListeners.remove(listener);
   }
   
   /**
    * Add a focus listener to be notified when a focus
    * event occurs.
    * @param listener cannot be <code>null</code>.
    */
   @Override
   public void addFocusListener(FocusListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(!m_focusListeners.contains(listener))
         m_focusListeners.add(listener);
   }
   
   /**
    * Removes the specified selection listener
    * @param listener cannot be <code>null</code>.
    */
   @Override
   public void removeFocusListener(FocusListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(m_focusListeners.contains(listener))
         m_focusListeners.remove(listener);
   }
   
   /**
    * Fires a <code>FocusEvent</code> for all registered 
    * <code>FocusListeners</code>.
    */
   private void fireFocusEvent(boolean gained)
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;     
      FocusEvent event = new FocusEvent(e);
      for(FocusListener listener : m_focusListeners)
      {
        if(gained)
           listener.focusGained(event);
        else
           listener.focusLost(event);
      }
   }
   
   /**
    * Fires a <code>SelectionEvent</code> for all registered 
    * <code>SelectionListeners</code>.
    */
   private void fireSelectionEvent()
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
   
   /* 
    * @see org.eclipse.swt.widgets.Control#addHelpListener(
    * org.eclipse.swt.events.HelpListener)
    */
   @Override
   public void addHelpListener(HelpListener listener)
   {
      m_table.addHelpListener(listener);
      if(m_cursor != null)
         m_cursor.addHelpListener(listener);
   }

   /* 
    * @see org.eclipse.swt.widgets.Control#removeHelpListener(
    * org.eclipse.swt.events.HelpListener)
    */
   @Override
   public void removeHelpListener(HelpListener listener)
   {
      m_table.removeHelpListener(listener);
      if(m_cursor != null)
         m_cursor.removeHelpListener(listener);
   }

   /**
    * Sets the insert button and context option enabled
    * @param enable flag indicating that the button should
    * be enabled.
    */
   public void setDeleteEnabled(boolean enable)
   {
      m_deleteButtonEnabled = enable;
   }
   
   /**
    * Sets the insert button and context option enabled
    * @param enable flag indicating that the button should
    * be enabled.
    */
   public void setInsertEnabled(boolean enable)
   {
      m_insertButtonEnabled = enable;
   }
   
   /**
    * Helper method to retrieve the active row bounds, that is
    * the total area including gridlines from the top left side
    * of the first row to the bottom right side of the last row
    * @return the bounds, never <code>null</code>.
    */
   protected Rectangle getActiveBounds()
   {
      int count = m_table.getItemCount();
      if( count == 0)
         return new Rectangle(0, 0, 0, 0);
      // Get first item
      TableItem firstItem = m_table.getItem(0);
      // Calculate total height
      int totalHeight = 0;
      for(TableItem item : m_table.getItems())
         totalHeight += item.getBounds(0).height;
      if(m_table.getLinesVisible())
      {
         int lineWidth = m_table.getGridLineWidth();
         totalHeight += ((count + 1) * lineWidth);
      }
      // Calculate total width
      int totalWidth = 0;
      int colCount = m_table.getColumnCount();
      for(int i = 0; i < colCount; i++)
         totalWidth += firstItem.getBounds(i).width;
      
      return new Rectangle(firstItem.getBounds(0).x,
         firstItem.getBounds(0).y, totalWidth, totalHeight);
   }
   
   /**
    * Returns <code>true</code> if options specify that inserting new
    * records is allowed.
    */
   protected boolean isInsertAllowed()
   {
      return (m_options & INSERT_ALLOWED) != 0;
   }

   /**
    * The default sorter for this table. This will sort alternating 
    * ascending and descending. It uses the label provider to get the
    * text values for each row\col.
    */
   class DefaultSorter extends ViewerSorter
   {

      @SuppressWarnings("synthetic-access")
      public void doSort(int col)
      {
         int options = (Integer)m_colOptions.get(col);
         if((options & IS_SORTABLE) == 0)
            return;
         mi_doSortCalled = true;
         if(col == mi_lastColumn)
         {
            mi_direction = 1 - mi_direction;
         }
         else
         {
            mi_lastColumn = col;
            mi_direction = ASCENDING;
         }            
      }
      
      @SuppressWarnings("synthetic-access")
      public void doSort(int col, boolean ascending)
      {
         int options = (Integer)m_colOptions.get(col);
         if((options & IS_SORTABLE) == 0)
            return;
         mi_doSortCalled = true;
         mi_lastColumn = col;
         mi_direction = ascending ? ASCENDING : DESCENDING;
                    
      }
            
      /* 
       * @see org.eclipse.jface.viewers.ViewerSorter#compare(
       * org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      @Override
      public int compare(Viewer viewer, Object e1, Object e2)
      {         
         boolean obj1_isEmpty = m_rowObjectProvider.isEmpty(e1);
         boolean obj2_isEmpty = m_rowObjectProvider.isEmpty(e2);
         
         if(obj1_isEmpty && obj2_isEmpty)
            return 0;
         if(obj1_isEmpty && !obj2_isEmpty)
            return 1;
         if(!obj1_isEmpty && obj2_isEmpty)
            return -1;
         mi_doSortCalled = false;
         int result = 0;
                  
         TableViewer tv = (TableViewer)viewer;
         int options = (Integer)m_colOptions.get(mi_lastColumn);
         boolean isNumeric = (options & IS_NUMERIC) != 0;        
         ITableLabelProvider provider = (ITableLabelProvider)tv.getLabelProvider();
         String str1 = provider.getColumnText(e1, mi_lastColumn);
         String str2 = provider.getColumnText(e2, mi_lastColumn);
         if(isNumeric)
         {
            //Compare by numeric
            Double d1 = Double.parseDouble(str1);
            Double d2 = Double.parseDouble(str2);
            if(d1 < d2)
               result = 1;
            else if(d1 == d2)
              result = 0;
            else if(d1 > d2)
               result = -1;               
         }         
         else
         {
            // Compare by string
            result = collator.compare(str1, str2);
         }
         if(mi_direction == DESCENDING)
            result = - result; // reverse for descending
         return result;
      }
      
      /* 
       * @see org.eclipse.jface.viewers.ViewerSorter#sort(
       * org.eclipse.jface.viewers.Viewer, java.lang.Object[])
       */
      @SuppressWarnings("unchecked")
      @Override
      public void sort(Viewer viewer, Object[] elements)
      {
         if(!mi_doSortCalled)
            return; // Only run sort if doSort was called first.        
         super.sort(viewer, elements); 
         // Need to keep model in sync 
         TableViewer tv = (TableViewer)viewer;
         List values = (List)tv.getInput();
         if(values == null)
            return;
         values.clear();
         for(Object element : elements)
            values.add(element);
         // Set sort image
         /*
          * This is not working correctly it is putting
          * space in the first column before each entry.
          * Looks like a bug in the setImage method
          * 
         int count = 0;
         for(TableColumn col : m_table.getColumns())
         {
            if(count == mi_lastColumn)
            {
               col.setImage(mi_direction == ASCENDING 
                  ? m_ascendingImage : m_descendingImage);
            }
            else
            {
               col.setImage(null); // Clear image
            }
            count++;
         }
         */
         fireTableModifiedEvent();         
        
      }     
      
      
      private int mi_lastColumn;
      private int mi_direction;
      private boolean mi_doSortCalled;
      
      private static final int ASCENDING = 0;
      private static final int DESCENDING = 1;      
      
   }
   
   
   class TableCursorCellEditorListener implements ICellEditorListener
   {
      

      TableCursorCellEditorListener(CellEditor cEditor, TableItem row, int col)
      {
         mi_cEditor = cEditor;
         mi_row = row;
         mi_col = col;
         
      }
      
      public void applyEditorValue()
      {
        // Remove this listener
         mi_cEditor.removeListener(this);
         m_applyCellValueHit = true;         
         m_table.showItem(mi_row);
         m_cursor.setVisible(true);
         scrollToNextColumn();         
         m_cursor.setFocus();         
      }

      public void cancelEditor()
      {
        //Remove this listener
         mi_cEditor.removeListener(this);        
                               
      }

      public void editorValueChanged(
         @SuppressWarnings("unused") boolean oldValidState,
         @SuppressWarnings("unused") boolean newValidState)
      {
        // no-op
      }
      
      private CellEditor mi_cEditor;
      private TableItem mi_row;
      @SuppressWarnings("unused")
      private int mi_col;
   }
   
   /**
    * Wrapper around the cell modifier to be able to fire a modification
    * event after the modification takes place.
    */
   class CellModifierWrapper implements ICellModifier
   {

      CellModifierWrapper(ICellModifier modifier)
      {
         mi_modifier = modifier;       
      }
      
      public boolean canModify(Object element, String property)
      {
         return mi_modifier.canModify(element, property);
      }

      public Object getValue(Object element, String property)
      {
         return mi_modifier.getValue(element, property);
      }

      public void modify(Object element, String property, Object value)
      {
         mi_modifier.modify(element, property, value);
         CellEditor cEditor = getCellEditor(getColumnIndex(property));
         boolean forceEvent = 
            (cEditor != null && cEditor instanceof CheckboxCellEditor);
         if(m_applyCellValueHit || forceEvent)
         {
            fireTableModifiedEvent();
            m_applyCellValueHit = false;
         }
      }
      
      private ICellModifier mi_modifier;
      
   }
      
   
   /**
    * Options indicating which buttons will be displayed. Can be
    * one or more of the following:
    * <p>
    * <pre>
    *    <li>SHOW_ALL</li>
    *    <li>SHOW_MOVE_UP</li>
    *    <li>SHOW_MOVE_DOWN</li>
    *    <li>SHOW_INSERT</li>
    *    <li>SHOW_DELETE</li>
    * </pre>
    * <p>
    */
   @SuppressWarnings("unused")
   private int m_options = -1;
   
   /**
    * The move up button, initialized in the ctor. Never
    * <code>null</code> after that.
    */
   protected Button m_moveUpButton;
   
   /**
    * The move down button, initialized in the ctor. Never
    * <code>null</code> after that.
    */
   protected Button m_moveDownButton;
   
   /**
    * The insert button, initialized in the ctor. Never
    * <code>null</code> after that.
    */
   protected Button m_insertButton;
   
   /**
    * The delete button, initialized in the ctor. Never
    * <code>null</code> after that.
    */
   protected Button m_deleteButton;
   
   /**
    * The table in the table viewer, initialized in the ctor after the table
    * viewer is created. Never <code>null</code> after that.
    */
   protected Table m_table;
   
   /**
    * The table viewer for this control, initialized in the ctor. Never
    * <code>null</code> after that.
    */
   protected TableViewer m_tableViewer;
   
   /**
    * The column properties array for the table. Used for
    * cell editing. Properties are added by {@link #addColumn(
    * String, int, ColumnLayoutData, CellEditor, int)}.
    */
   private String[] m_tableColProps = new String[]{};
   
   /**
    * The column properties key array for the table.
    */
   private String[] m_tableColPropsKeys = new String[]{};
      
   /**
    * The cell editors array for the table. Used for
    * cell editing. Properties are added by {@link #addColumn(
    * String, int, ColumnLayoutData, CellEditor, int)}.
    */
   private CellEditor[] m_cellEditors = new CellEditor[]{};
   
   private IPSNewRowObjectProvider m_rowObjectProvider;
   
   /**
    * List of options for each table column
    */
   private List m_colOptions = new ArrayList();
   
   
   /**
    * Button registry
    */
   private Map<Button, Object[]> m_buttonRegistry = 
      new HashMap<Button, Object[]>();
      
   /**
    * List of all registered table modified listeners
    */ 
   private List<IPSTableModifiedListener> m_tableModListeners = 
      new ArrayList<IPSTableModifiedListener>();
   
   /**
    * List of all registered column selection listeners
    */ 
   private List<IPSTableColumnSelectionListener> m_columnSelectionListeners = 
      new ArrayList<IPSTableColumnSelectionListener>();
   
   /**
    * List of selection listeners
    */
   private java.util.List<SelectionListener> m_selectionListeners = 
      new ArrayList<SelectionListener>();
   
   /**
    * List of focus listeners
    */
   private java.util.List<FocusListener> m_focusListeners = 
      new ArrayList<FocusListener>();
   
   /** 
    * The table cursor to handle keyboard events and
    * single cell selection for the table.
    */
   protected TableCursor m_cursor;
   
   /**
    * Last fired row selection
    */
   private boolean m_lastRowSelectionFired;
   
   /**
    * Indicates the last slected column
    */
   private int m_lastColumnSelected = -1;
   
   // Context Menu items
   private MenuItem m_insertMenuItem;
   private MenuItem m_deleteMenuItem;
   private MenuItem m_upMenuItem;
   private MenuItem m_downMenuItem;
   
   // Images for columns
   private Image m_ascendingImage;
   private Image m_descendingImage;
   
   /**
    * Variable that holds the time the last mousedown event
    * in the tables active area.
    */
   private long m_tableMouseDownTime;
   
   /**
    * Flag indicating that the apply cell value method was hit
    * for the cell currecntly being edited
    */
   private boolean m_applyCellValueHit = false;
   
   /**
    * Flag indicating that insert button and context option
    * can be enabled
    */
   private boolean m_insertButtonEnabled = true;
   
   /**
    * Flag indicating that delete button and context option
    * can be enabled
    */
   private boolean m_deleteButtonEnabled = true;
   
   /**
    * A place holder for options, when no options are
    * desired.
    */
   public static final int NONE = 1 << 1;
   
   /**
    * Column option that indicates that a column can be
    * sorted.
    */
   public static final int IS_SORTABLE = 1 << 2;
   
   /**
    * Column options that indicates that a column should be
    * sorted numerically.
    */
   public static final int IS_NUMERIC = 1 << 3;
   
   /**
    * Option to hide table headers
    */
   public static final int HIDE_HEADER = 1 << 4;
   
   /**
    * Option to hide table grid lines
    */
   public static final int HIDE_LINES = 1 << 5; 
      
   /**
    * Option to show all of the buttons for this control.
    */
   public static final int SHOW_ALL = 1 << 6;
   
   /**
    * Option to show the move up button for this control.
    */
   public static final int SHOW_MOVE_UP = 1 << 7;
   
   /**
    * Option to show the move down button for this control.
    */
   public static final int SHOW_MOVE_DOWN = 1 << 8;
   
   /**
    * Option to show the insert button for this control.
    */
   public static final int SHOW_INSERT = 1 << 9;
   
   /**
    * Option to show the delete button for this control.
    */
   public static final int SHOW_DELETE = 1 << 10;
   
   /**
    * Option that will allow deletion of rows from this table.
    */
   public static final int DELETE_ALLOWED = 1 << 11;
   
   /**
    * Option that will allow insertion of rows into this table.
    */
   public static final int INSERT_ALLOWED = 1 << 12;
   
   /**
    * Option that surpress the table cursor from being used. This
    * is only useful when cell editing is not needed.
    */
   public static final int SURPRESS_TABLE_CURSOR = 1 << 13;
   
   /**
    * Option that to allow manual sorting of a table via a key press
    * or API. This does not include sorting with a sorter.
    */
   public static final int SURPRESS_MANUAL_SORT = 1 << 14;
   
   /**
    * Option that to supress the context menu from appearing
    */
   public static final int SURPRESS_CONTEXT_MENU = 1 << 15;
      
   
   /**
    * Move up button type
    */
   public static final int TYPE_MOVE_UP = 0;
   
   /**
    * Move down button type
    */
   public static final int TYPE_MOVE_DN = 1;
   
   /**
    * Insert button type
    */
   public static final int TYPE_INSERT = 2;
   
   /**
    * Delete button type
    */
   public static final int TYPE_DELETE = 3;
   
   /**
    * Distance between table and buttons.
    */
   public static final int TABLE_BUTTON_HSPACE = 10;

   /**
    * Enter key code.
    */
   protected static final int ENTER_KEY = 13;
}

/******************************************************************************
 *
 * [ PSTemplateBindingsPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.PSModelException;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateBinding;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonFactory;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.dialog.PSTemplateBindingPropertiesDialog;
import com.percussion.workbench.ui.util.PSCompletionProvider;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;

import java.util.ArrayList;
import java.util.List;

import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_HEIGHT;
import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_WIDTH;
import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.EDITOR_TOP_OFFSET;

/**
 * Allows user to specify bindings of variables to expressions.
 * 
 * @author Andriy Palamarchuk
 */
public class PSTemplateBindingsPage extends Composite
{
   /**
    * Creates new bindings page.
    * @param editor the editor this page will be created on.
    * Not <code>null</code>.
    */
   public PSTemplateBindingsPage(Composite parent, int style,
         PSEditorBase editor) throws PSModelException
   {
      super(parent, style);
      if (editor == null)
      {
         throw new IllegalArgumentException("Editor can't be null");
      }
      m_bindingClipboardLoader = new PSBindingClipboardLoader(getDisplay());
      setLayout(new FormLayout());

      // left pane
      final Composite leftPane = createLeftPane(this);
      final Label variablesLabel = createBindingsLabel(leftPane);
      m_bindingsTable = createBindingsTable(leftPane, variablesLabel);
      editor.registerControl(BINDINGS_LABEL, m_bindingsTable,
            new IPSControlValueValidator[] {new PSNotNullValueValidator()});
      m_completionProvider = new PSCompletionProvider();
   }

   /**
    * Convenience method to get string resource identified by key.
    */
   private static String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }

   /**
    * Creates a label above bindings table.
    */
   private Label createBindingsLabel(final Composite container)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(BINDINGS_LABEL + ':');
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      label.setLayoutData(formData);
      return label;
   }

   /**
    * Table to show bindings
    */
   private PSBindingsTable createBindingsTable(final Composite container,
         final Control previous)
   {
      IPSNewRowObjectProvider rowObjectProvider = new IPSNewRowObjectProvider()
      {
         public Object newInstance()
         {
            return new PSTemplateBinding();
         }

         public boolean isEmpty(Object o)
         {
            if (!(o instanceof IPSTemplateBinding))
            {
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of IPSTemplateBinding."); //$NON-NLS-1$
            }
            final IPSTemplateBinding binding = (IPSTemplateBinding) o;
            return StringUtils.isBlank(binding.getVariable())
                  && StringUtils.isBlank(binding.getExpression());
         }
      };
      final ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         public String getColumnText(Object element, int columnIndex)
         {
            final IPSTemplateBinding binding = (IPSTemplateBinding) element;
            switch (columnIndex)
            {
               case VAR_COL :
                  return StringUtils.defaultString(binding.getVariable());
               case EXPRESSION_COL :
                  return StringUtils.defaultString(binding.getExpression());
               default :
                  throw new AssertionError();
            }
         }
      };
      final PSBindingsTable bindingsTable = new PSBindingsTable(container,
            labelProvider, rowObjectProvider, SWT.NONE,
            PSSortableTable.SHOW_MOVE_UP | PSSortableTable.SHOW_MOVE_DOWN
            | PSSortableTable.SHOW_INSERT | PSSortableTable.SHOW_DELETE
            | PSSortableTable.INSERT_ALLOWED | PSSortableTable.DELETE_ALLOWED);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, -BUTTON_BORDER_HSPACE);
      formData.top = new FormAttachment(previous, 0, 0);
      formData.bottom = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      bindingsTable.setLayoutData(formData);

      specifyBindingsTableColumns(bindingsTable);
      return bindingsTable;
   }

   /**
    * Defines columns in the provided bindings table.
    */
   private void specifyBindingsTableColumns(final PSSortableTable bindingsTable)
   {
      bindingsTable.addColumn("PSTemplateBindingsPage.tableColumn.variable",    //$NON-NLS-1$
            PSSortableTable.NONE, new ColumnWeightData(5), null, SWT.LEFT);
      bindingsTable.addColumn("PSTemplateBindingsPage.tableColumn.expression",  //$NON-NLS-1$
            PSSortableTable.NONE, new ColumnWeightData(10), null, SWT.LEFT);
   }

   /**
    * Creates the left pane. This pane contains the bindings table and the
    * expression editor.
    */
   private Composite createLeftPane(Composite container)
   {
      final Composite pane = new Composite(container, SWT.NONE);
      pane.setLayout(new FormLayout());

      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, EDITOR_TOP_OFFSET);
      formData.bottom = new FormAttachment(100, 0);
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, 0);
      pane.setLayoutData(formData);
      return pane;
   }

   /**
    * Load bindings data to controls from the template.
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      final List<PSTemplateBinding> bindings =
            new ArrayList<PSTemplateBinding>();
      for (final IPSTemplateBinding binding : template.getBindings())
      {
         bindings.add((PSTemplateBinding) ((PSTemplateBinding) binding)
               .clone());
      }
      m_bindingsTable.setValues(bindings);
   }

   /**
    * Updates template with the provided bindings.
    */
   @SuppressWarnings("unchecked")
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      m_notSavedBindings.clear();
      template.getBindings().clear();
      List<PSTemplateBinding> bindings = m_bindingsTable.getValues();
      int order = 1;
      for (final PSTemplateBinding binding : bindings)
      {
         binding.setExecutionOrder(order++);
         if (StringUtils.isBlank(binding.getExpression()))
         {
            m_notSavedBindings.add(binding.getVariable());
         }
         else
         {
            template.addBinding(binding);
         }
      }
   }

   /**
    * Copies bindings currently selected in the bindings table to clipboard.
    * Does nothing if there is no bindings selected.
    */
   void copyBindingsToClipboard()
   {
      m_bindingClipboardLoader.copyBindings(getSelectedBindings());
   }

   /**
    * Pastes bindings from clipboard into bindings table.
    * Does nothing if the clipboard is empty or clipboard content is not 
    * recognized to be bindings. Invalid binding data is ignored during the
    * operation.
    */
   @SuppressWarnings("unchecked")
   void pasteBindingsFromClipboard()
   {
      m_bindingsTable.pasteBindingsToTable();
   }
   
   /**
    * Indicates whether clipboard contains valid bindings.
    * @return <code>true</code> if clipboard contains data recognized as
    * valid bindings.
    * @see PSBindingClipboardLoader#clipboardContainsBindings()
    */
   boolean clipboardContainsBindings()
   {
      return m_bindingClipboardLoader.clipboardContainsBindings();
   }

   /**
    * Binding currently selected in the table. Table must have a selection.
    */
   PSTemplateBinding getSelectedBinding()
   {
      return (PSTemplateBinding) getSelectedBindingTableItem().getData();
   }

   /**
    * Bindings currently selected in the table. Table must have a selection.
    */
   private List<PSTemplateBinding> getSelectedBindings()
   {
      final List<PSTemplateBinding> bindings =
            new ArrayList<PSTemplateBinding>(); 
      final Table table = m_bindingsTable.getTable();
      for (int selectionIdx : table.getSelectionIndices())
      {
         final TableItem selection = table.getItem(selectionIdx);
         bindings.add((PSTemplateBinding) selection.getData());
      }
      return bindings;
   }

   /**
    * Bindings which are not saved because they don't have values. 
    */
   public List<String> getNotSavedBindings()
   {
      return m_notSavedBindings;
   }

   /**
    * Currently selected bindings table item. Table must have a selection.
    */
   private TableItem getSelectedBindingTableItem()
   {
      final Table table = m_bindingsTable.getTable();
      return table.getItem(table.getSelectionIndices()[0]);
   }

   /**
    * Returns <code>true</code> if the table has valid selection.
    */
   boolean hasValidBindingsSelection()
   {
      return m_bindingsTable.getTable().getSelectionIndex() != -1;
   }

   /**
    * Makes sure the bindings have expressions.
    */
   private class PSNotNullValueValidator implements IPSControlValueValidator
   {
      @SuppressWarnings("unchecked")
      public String validate(@SuppressWarnings("unused") PSControlInfo controlInfo)
      {
         List<PSTemplateBinding> bindings = m_bindingsTable.getValues();
         for (final PSTemplateBinding binding : bindings)
         {
            if (StringUtils.isBlank(binding.getExpression()))
            {
               return PSMessages.getString(
                     "PSTemplateBindingsPage.error.noValue",  //$NON-NLS-1$ 
                     new Object[]{binding.getVariable()});
            }
         }
         return null;
      }
   }
   
   /**
    * Adds more functionality needed for bindings page.
    * Shows dialog to edit table items, disables the table cursor.
    * In absence of table cursor correctly handles insertions, key bindings,
    * editing. Supports copy-and-paste of bindings.
    */
   private class PSBindingsTable extends PSSortableTable
   {
      /**
       * Creates new bindings table. Passes all the arguments to super
       * constructor disabling table cursor.
       * @see PSSortableTable#PSSortableTable(Composite, ITableLabelProvider,
       * IPSNewRowObjectProvider, int, int)
       */
      public PSBindingsTable(Composite parent, ITableLabelProvider labelProvider,
            IPSNewRowObjectProvider rowObjectProvider, int style, int options)
      {
         super(parent, labelProvider, rowObjectProvider, style,
               options | PSSortableTable.SURPRESS_TABLE_CURSOR);

         // edit on double click
         m_table.addMouseListener(new MouseAdapter()
               {
                  @Override
                  public void mouseDoubleClick(MouseEvent e)
                  {
                     if (getActiveBounds().contains(e.x, e.y))
                     {
                        editBinding(getSelectedBinding());
                     }
                  }
               });

         final Button editButton = PSButtonFactory.createEditButton(this);
         {
            final FormData formData = new FormData();
            formData.width = BUTTON_WIDTH;
            formData.height = BUTTON_HEIGHT;
            formData.right = new FormAttachment(100, 0);
            formData.top =  new FormAttachment(
                  m_deleteButton, BUTTON_VSPACE_OFFSET, SWT.BOTTOM);
            editButton.setLayoutData(formData);
         }
         
         editButton.setToolTipText(
               getMessage("PSTemplateBindingsPage.label.edit"));      //$NON-NLS-1$
         editButton.addSelectionListener(new SelectionAdapter()
               {
            @Override
            @SuppressWarnings("unused")
            public void widgetSelected(SelectionEvent e)
            {
               doEdit();
            }
         });
      }
      
      /**
       * Edits binding and refreshes the bindings table if necessary.
       *
       * @param binding the binding to edit. Assumed not <code>null</code>.
       */
      private void editBinding(final PSTemplateBinding binding)
      {
         final PSTemplateBindingPropertiesDialog dlg =
               new PSTemplateBindingPropertiesDialog(getShell(),
                     m_completionProvider.getVarCompletions(),
                     m_completionProvider.getMethodCompletions(), binding);
         dlg.open();
         if (dlg.getReturnCode() == PSTemplateBindingPropertiesDialog.OK)
         {
            refreshTable();         
            fireTableModifiedEvent();
         }
      }

      /**
       * Adds logic to invoke editing on 'F2', navigation on 'Enter',
       * maintaining empty row at the end of the table.
       * @see com.percussion.workbench.ui.controls.PSSortableTable#handleTableKeyPress(org.eclipse.swt.events.KeyEvent)
       */
      @Override
      protected void handleTableKeyPress(KeyEvent e)
      {
         super.handleTableKeyPress(e);
         if (e.keyCode == SWT.F2)
         {
            if (hasValidBindingsSelection())
            {
               editBinding(getSelectedBinding());
            }
         }
         else if(e.keyCode == ENTER_KEY)
         {
            final int row = m_table.getSelectionIndex();
            if (row == -1)
            {
               return;
            }
            final int lastRow = m_table.getItemCount() - 1;
            if(row < lastRow)
            {
               m_table.setSelection(row + 1);
            }
            else
            {
               maybeAppendEmptyRow();
            }
         }
         else if(e.keyCode == SWT.ARROW_DOWN)
         {
            // automatically add an empty record
            final int current = m_table.getSelectionIndex();
            final int count = m_table.getItemCount();
            if (count == current + 1)
            {
               maybeAppendEmptyRow();
            }
         }
      }

      /**
       * If necessary adds one more row to the end of the table.
       */
      private void maybeAppendEmptyRow()
      {
         if (isInsertAllowed())
         {
            final int current = m_table.getSelectionIndex();
            final int count = m_table.getItemCount();
            doInsert(true);
            if (count < m_table.getItemCount())
            {
               // successfully inserted
               m_table.deselectAll();
               m_table.select(current + 1);
            }
         }
      }

      /**
       * In addition to base class functionality shows an editor dialog if
       * parameter <code>appendOnly</code> is <code>false</code> and 
       * insert operation was successful. 
       * @see com.percussion.workbench.ui.controls.PSSortableTable#doInsert(boolean)
       */
      @Override
      public void doInsert(boolean appendOnly)
      {
         final int count = m_table.getItemCount();
         super.doInsert(appendOnly);
         if (!appendOnly && count < m_table.getItemCount())
         {
            editBinding(getSelectedBinding());
         }
      }

      /**
       * Handles editing of the selected row in the table.
       * <p>
       * <ul>
       * Behaviour:
       * <li>If table is empty then a record for new row is added to the end
       * of the table and is open for editing</li>
       * <li>Only one empty row can be added to the end of the table</li>
       * <li>If the selection is on a row then the selected row is open for
       * editing</li>
       * <li>Nothing happens if table is not empty and no rows are selected.</li>
       * </ul>
       * </p>
       */
      private void doEdit()
      {
         if (m_table.getItemCount() == 0)
         {
            maybeAppendEmptyRow();
         }
         if (hasValidBindingsSelection())
         {
            editBinding(getSelectedBinding());
         }
      }

      /**
       * Reads bindings from clipboard and inserts them into the table.
       * Does nothing if there is no valid bindings in clipboard.
       * @see PSTemplateBindingsPage#pasteBindingsFromClipboard()
       */
      @SuppressWarnings("unchecked")
      private void pasteBindingsToTable()
      {
         final List<PSTemplateBinding> bindings =
               m_bindingClipboardLoader.pasteBindings();
         if (bindings == null || bindings.isEmpty())
         {
            return;
         }

         final Table table = m_bindingsTable.getTable();
         final int insertIdx = table.getSelectionIndices().length == 0
               ? getAllValues().size()
               : table.getSelectionIndices()[0];   
         getAllValues().addAll(insertIdx, bindings);
         refreshTable();
         fireTableModifiedEvent();
      }

      /**
       * Adds Edit menu item to the standard sorted table menu. 
       */
      @Override
      protected void createContextMenu()
      {
         super.createContextMenu();
         final Menu tableMenu = m_table.getMenu();
         
         final MenuItem editMenuItem = new MenuItem(tableMenu, SWT.NONE);
         editMenuItem.setText(getMessage("PSTemplateBindingsPage.label.edit"));
         editMenuItem.addSelectionListener(new SelectionAdapter()
         {
            @Override
            @SuppressWarnings("unused")
            public void widgetSelected(SelectionEvent e)
            {
               doEdit();
            }
         });
         
         new MenuItem(tableMenu, SWT.SEPARATOR);

         createCopyMenuItem(tableMenu);
         createPasteMenuItem(tableMenu);
      }

      /**
       * Creates menu item to copy bindings into clipboard.
       * @param tableMenu the menu to add the menu item to.
       * Assumed not <code>null</code>.
       */
      private void createCopyMenuItem(final Menu tableMenu)
      {
         final MenuItem copyMenuItem = new MenuItem(tableMenu, SWT.NONE);
         copyMenuItem.setText(getMessage("PSTemplateBindingsPage.label.copy"));
         copyMenuItem.setImage(
               createImageFromSharedDescriptor(ISharedImages.IMG_TOOL_COPY));
         copyMenuItem.addSelectionListener(new SelectionAdapter()
         {
            @Override
            @SuppressWarnings("unused")
            public void widgetSelected(SelectionEvent e)
            {
               copyBindingsToClipboard();
            }
         });
         tableMenu.addMenuListener(new MenuAdapter()
         {
            @Override
            public void menuShown(@SuppressWarnings("unused") MenuEvent e)
            {
               copyMenuItem.setEnabled(hasValidBindingsSelection());
            }
         });
      }
      
      /**
       * Creates menu item to paste bindings from clipboard.
       * @param tableMenu the menu to add the menu item to.
       * Assumed not <code>null</code>.
       */
      private void createPasteMenuItem(final Menu tableMenu)
      {
         final MenuItem pasteMenuItem = new MenuItem(tableMenu, SWT.NONE);
         pasteMenuItem.setText(
               getMessage("PSTemplateBindingsPage.label.paste"));
         pasteMenuItem.setImage(
               createImageFromSharedDescriptor(ISharedImages.IMG_TOOL_PASTE));
         pasteMenuItem.addSelectionListener(new SelectionAdapter()
         {
            @Override
            public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
            {
               pasteBindingsToTable();
            }
         });
         tableMenu.addMenuListener(new MenuAdapter()
         {
            @Override
            public void menuShown(@SuppressWarnings("unused") MenuEvent e)
            {
               pasteMenuItem.setEnabled(clipboardContainsBindings());
            }
         });
      }
   }

   /**
    * Distance in pixels between buttons of the variables table
    * {@link #m_bindingsTable} and the border of the page.
    */
   private static final int BUTTON_BORDER_HSPACE = 
         PSSortableTable.TABLE_BUTTON_HSPACE * 2;

   /**
    * Bindings label text.
    */
   private static final String BINDINGS_LABEL =
         getMessage("PSTemplateBindingsPage.label.variables"); //$NON-NLS-1$
   
   /**
    * Index of variable column in bindings table.
    */
   private static final int VAR_COL = 0;

   /**
    * Index of expression column in bindings table.
    */
   private static final int EXPRESSION_COL = 1;

   /**
    * Table to manipulate bindings.
    */
   private PSBindingsTable m_bindingsTable;

   /**
    * Not updated bindings due to them not having values.
    */
   private final List<String> m_notSavedBindings = new ArrayList<String>();
   
   /**
    * Loads template bindings to, reads from clipboard.
    * Never <code>null</code>.
    */
   private final PSBindingClipboardLoader m_bindingClipboardLoader;

   /**
    * Manages variable, method, field completions. Initialized in constructor.
    * Never <code>null</code> after that.
    */
   private final PSCompletionProvider m_completionProvider;
}

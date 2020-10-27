/******************************************************************************
*
* [ PSExtensionDataDialog.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Dialog used to edit an extension's custom init parameters
 */
public class PSExtensionDataDialog extends PSDialog
{
   public PSExtensionDataDialog(Shell parentShell, IPSExtensionDef def)
   {
      super(parentShell);
      setShellStyle(getShellStyle() | SWT.RESIZE);
      m_def = def;
   }

   /* 
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      final Composite comp = (Composite)super.createDialogArea(parent);
      comp.setLayout(new FormLayout());
      
      m_table = createTable(comp);
      final FormData formData_1 = new FormData();
      formData_1.height = 160;
      formData_1.right = new FormAttachment(100, -5);
      formData_1.top = new FormAttachment(0, 10);
      formData_1.left = new FormAttachment(0, 5);
      m_table.setLayoutData(formData_1);
      
      Label descLabel = new Label(comp, SWT.NONE);
      descLabel.setText(PSMessages.getString("PSExtensionDataDialog.description.label")); //$NON-NLS-1$
      final FormData formData_2 = new FormData();
      formData_2.left = new FormAttachment(m_table, 0, SWT.LEFT);
      formData_2.top = new FormAttachment(m_table, 10, SWT.BOTTOM);
      descLabel.setLayoutData(formData_2);
     
      m_descriptionText = new Text(comp, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(descLabel, 0, SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_table, 0, SWT.LEFT);
      formData_3.right = new FormAttachment(m_table, 0, SWT.RIGHT);
      formData_3.height = 70;
      m_descriptionText.setLayoutData(formData_3);
      
      loadDialog();
      return comp;
   }
   
   /**
    * Helper method to create the data table
    * @param comp the parent composite, assumed not <code>null</code>.
    * @return the newly created table, never <code>null</code>.
    */
   private PSSortableTable createTable(Composite comp)
   {
      IPSNewRowObjectProvider objectProvider = new IPSNewRowObjectProvider()
      {

         public Object newInstance()
         {
            return new PSPair<String, String>();
         }

         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public boolean isEmpty(Object obj)
         {
            PSPair<String, String> pair = (PSPair<String, String>)obj;
            if(StringUtils.isBlank(pair.getFirst()) || 
               StringUtils.isBlank(pair.getSecond()))
               return true;
            return false;
         }
         
      };
      
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {

         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public String getColumnText(Object element,
            @SuppressWarnings("unused") int columnIndex) //$NON-NLS-1$
         {
            if(element == null)
               return ""; //$NON-NLS-1$
            PSPair<String, String> pair = (PSPair<String, String>)element;
            String result = columnIndex == 0 ? pair.getFirst(): pair.getSecond();
            return StringUtils.defaultString(result);
         }
         
      };
      
      PSSortableTable table = new PSSortableTable(comp,
         labelProvider, objectProvider, 
         SWT.NONE,
         PSSortableTable.SURPRESS_MANUAL_SORT |
         PSSortableTable.DELETE_ALLOWED |
         PSSortableTable.INSERT_ALLOWED);
      table.setCellModifier(new DataCellModifier(table));
      CellEditor nameEditor = new PSComboBoxCellEditor(
         table.getTable(), new String[0]);
      CellEditor valueEditor = new TextCellEditor(table.getTable());
      
      table.addColumn(PSMessages.getString("PSExtensionDataDialog.col.name.label"), //$NON-NLS-1$
         PSSortableTable.NONE,  
         new ColumnWeightData(10,100, true), nameEditor, SWT.LEFT);
      
      table.addColumn(PSMessages.getString("PSExtensionDataDialog.col.value.label"), //$NON-NLS-1$
         PSSortableTable.NONE,  
         new ColumnWeightData(10,100, true), valueEditor, SWT.LEFT);
      
      table.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(
            @SuppressWarnings("unused") SelectionEvent e)
         {
            showDescription();
         }
      });
     
      return table;
   }
   
   /**
    * Displays description for the selection row if a description
    * exists.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   protected void showDescription()
   {
      if(m_table.getTable().getSelectionCount() == 1)
      {
         StructuredSelection selection = 
            (StructuredSelection)m_table.getTableViewer().getSelection();
         PSPair<String, String> pair = 
            (PSPair<String, String>)selection.getFirstElement();
         String desc = StringUtils.defaultString(
            ms_param_desc.get(pair.getFirst()));
         m_descriptionText.setText(desc);
      }
      else
      {
         m_descriptionText.setText(""); //$NON-NLS-1$
      }
   }

   /* 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   @Override
   protected void okPressed()
   {
      List<PSPair<String, String>> mergedList = 
         new ArrayList<PSPair<String, String>>();
      mergedList.addAll(m_table.getValues());
      mergedList.addAll(m_hiddenParams);      
      if(!removeAssemblerParams(mergedList))
         return;
      clearInitParams();
      for(PSPair<String, String> pair : mergedList)
      {
         ((PSExtensionDef)m_def).setInitParameter(
            pair.getFirst(), pair.getSecond());
      }
      super.okPressed();
   }
   
   /**
    * Remove assembler params if this extension does not have an
    * assembler interface. Also warns user that the params will be
    * deleted.
    * @param params
    * @return <code>true</code> if user wants to continue with
    * ok procedure and params were removed. 
    */
   private boolean removeAssemblerParams(List<PSPair<String, String>> params)
   {
      List<PSPair<String, String>> removeList = 
         new ArrayList<PSPair<String, String>>();
      StringBuilder interfaces = new StringBuilder();
      for(PSPair<String, String> pair : params)
      {
         if(ms_assembly_params.contains(pair.getFirst()))
         {
            removeList.add(pair);
            interfaces.append(pair.getFirst());
            interfaces.append("\n"); //$NON-NLS-1$
         }
      }
      if(!(!hasAssemblyInterface(m_def) && !removeList.isEmpty()))
         return true;
      // Ask user if they want to continue
      Object[] args = new Object[]{interfaces.toString()};
      String msg = 
         PSMessages.getString("PSExtensionDataDialog.removingParams.message", args); //$NON-NLS-1$
      boolean bOK = MessageDialog.openQuestion(
         getShell(),
         PSMessages.getString("PSExtensionDataDialog.removingParams.title"), msg); //$NON-NLS-1$
      if(bOK)
      {
         for(PSPair<String, String> pair : removeList)
         {
            params.remove(pair);
         }
         return true;
      }
      return false;
   }
   
   /**
    * Clear all init parameters in the extension def
    */
   private void clearInitParams()
   {
      Iterator it = m_def.getInitParameterNames();
      List<String> removeList = new ArrayList<String>();
      while(it.hasNext())    
         removeList.add((String)it.next());
      for(String key : removeList)
         ((PSExtensionDef)m_def).setInitParameter(key, null);
   }

   /* 
    * @see org.eclipse.jface.window.Window#getInitialSize()
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(700, 350);
   }
   
   /**
    * Loads name cell editor choices, only unused choices should
    * be shown.
    * @element the object for the row to be modified.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void loadNameCellChoices(Object element)
   {
      List<String> used = new ArrayList<String>();
      List<String> choices = new ArrayList<String>();
      String currentSel = ""; //$NON-NLS-1$
      if(element != null)
         currentSel = ((PSPair<String, String>)element).getFirst();
      // Create 
      for(PSPair<String, String> pair : 
         (List<PSPair<String, String>>)m_table.getValues())
      {
         if(!pair.getFirst().equals(currentSel))
            used.add(pair.getFirst());
      }
      // create the list of choices
      for(String choice : getNameCellChoices())
      {
         if(!used.contains(choice))
            choices.add(choice);
      }
      Collections.sort(choices);
      //Set choices in cell editor
      PSComboBoxCellEditor editor = 
         (PSComboBoxCellEditor)m_table.getCellEditor(0);
      editor.setItems(choices.toArray(new String[choices.size()]));
   }
   
   /**
    * Used by {@link #loadNameCellChoices(Object)}. Items in
    * the returned list will be used for choices in the tables
    * name column cell editor.
    * @return the list of choices. Never <code>null</code>, may
    * be empty.
    */
   private List<String> getNameCellChoices()
   {
      List<String> choices = new ArrayList<String>();      
      if(hasAssemblyInterface(m_def))
         choices.addAll(ms_assembly_params);
      return choices;
   }
   
   /**
    * Indicates that this extension uses the assembly 
    * interface.
    * @return <code>true</code> if the assembly interface is
    * being used.
    */
   public boolean hasAssemblyInterface(IPSExtensionDef def)
   {
      Iterator it = def.getInterfaces();
      while(it.hasNext())
      {
         String current = (String)it.next();
         if(ASSEMBLY_INTERFACE.equals(current))
            return true;
      }
      return false;
   }
   
   /**
    * Load the parameters into the table
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void loadDialog()
   {
      List<PSPair<String, String>> params = 
         new ArrayList<PSPair<String, String>>();
      Iterator it = m_def.getInitParameterNames();
      while(it.hasNext())
      {
         String key = (String)it.next();
         if(!ms_filterList.contains(key))
            params.add(new PSPair(key, m_def.getInitParameter(key)));
         else
            m_hiddenParams.add(new PSPair(key, m_def.getInitParameter(key)));
      }
      m_table.setValues(params);
      if(m_table.getTable().getItemCount() > 0)
         m_table.getTable().select(0);
      showDescription();
   }   
   
   class DataCellModifier implements ICellModifier
   {
      
      DataCellModifier(PSSortableTable viewer)
      {
         mi_viewer = viewer;
      }

      public boolean canModify(@SuppressWarnings("unused") Object element, //$NON-NLS-1$
         @SuppressWarnings("unused") String property) //$NON-NLS-1$
      {
         int col = m_table.getColumnIndex(property);
         if(col == 0)
            loadNameCellChoices(element);
         return true;
      }

      @SuppressWarnings("unchecked") //$NON-NLS-1$
      public Object getValue(Object element, String property)
      {
         int col = m_table.getColumnIndex(property);
         PSPair<String, String> rowVal = 
            (PSPair<String, String>)element;
         if(col == 0)
            return StringUtils.defaultString(rowVal.getFirst());
         else if(col == 1)
            return StringUtils.defaultString(rowVal.getSecond());
         else
         return null;
      }

      @SuppressWarnings("unchecked") //$NON-NLS-1$
      public void modify(Object element, String property, Object value)
      {
         int col = m_table.getColumnIndex(property);
         if(value == null)
            return;
         TableItem item = (TableItem)element;
         
         PSPair<String, String> rowVal = 
            (PSPair<String, String>)item.getData();         
         if(col == 0)
            rowVal.setFirst(StringUtils.defaultString((String)value));
         else if(col == 1)
            rowVal.setSecond(StringUtils.defaultString((String)value)); 
         mi_viewer.refreshTable();
         
      }
      
      private PSSortableTable mi_viewer;
      
   }   
   
   
   /* 
    * @see org.eclipse.jface.window.Window#configureShell(
    * org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString("PSExtensionDataDialog.title")); //$NON-NLS-1$
   }

   private IPSExtensionDef m_def;
   
   private PSSortableTable m_table;
   private Text m_descriptionText;
   
   /**
    * List of params that have been hidden from the user but still
    * need to be maintained so they can be added to the list of all
    * init params when ok is hit.
    */
   private List<PSPair<String, String>> m_hiddenParams = 
      new ArrayList<PSPair<String, String>>();
   
   /**
    * Constant for the assembly interface name.
    */
   public static final String ASSEMBLY_INTERFACE = 
      "com.percussion.services.assembly.IPSAssembler"; //$NON-NLS-1$
   
   /**
    * List of known assembly init parameters.
    */
   public static final List<String> ms_assembly_params = new ArrayList<String>();
   static
   {
      ms_assembly_params.add(IPSExtensionDef.INIT_PARAM_ASSEMBLY_AUTO_RELOAD);
      ms_assembly_params.add(IPSExtensionDef.INIT_PARAM_ASSEMBLY_FILE_SUFFIX);
      ms_assembly_params.add(IPSExtensionDef.INIT_PARAM_ASSEMBLY_LIBRARIES);
   }
   
   /**
    * List of descriptions for known init parameters
    */
   private static final Map<String, String> ms_param_desc = new HashMap<String, String>();
   static
   {
      ms_param_desc.put(IPSExtensionDef.INIT_PARAM_ASSEMBLY_AUTO_RELOAD,
         PSMessages.getString("PSExtensionDataDialog.autoReload.description")); //$NON-NLS-1$
      ms_param_desc.put(IPSExtensionDef.INIT_PARAM_ASSEMBLY_FILE_SUFFIX,
         PSMessages.getString("PSExtensionDataDialog.fileSuffix.description")); //$NON-NLS-1$
      ms_param_desc.put(IPSExtensionDef.INIT_PARAM_ASSEMBLY_LIBRARIES,
         PSMessages.getString("PSExtensionDataDialog.library.description")); //$NON-NLS-1$
   }
   
   /**
    * List of Init parameters that should not appear in the table
    */
   private static final List<String> ms_filterList = new ArrayList<String>();
   static
   {
      ms_filterList.add("className"); //$NON-NLS-1$
      ms_filterList.add("com.percussion.user.description"); //$NON-NLS-1$
      ms_filterList.add("com.percussion.extension.version"); //$NON-NLS-1$
      ms_filterList.add("com.percussion.extension.reentrant"); //$NON-NLS-1$
   }
}

/******************************************************************************
 *
 * [ PSExtensionParamsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.E2Designer.DTTextLiteral;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.controls.PSValueSelectorCellEditor;
import com.percussion.workbench.ui.layouts.PSAutoResizeTableLayout;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Dialog used to edit extension parameters
 */
public class PSExtensionParamsDialog extends PSDialog
{
   
   /**
    * Ctor that calls:
    * {@link #PSExtensionParamsDialog(Shell, IPSExtensionDef, Map,
    *  int)}
    */
   public PSExtensionParamsDialog(
      Shell parent, IPSExtensionDef def, Map<String, IPSReplacementValue> values)
   {
      this(parent, def, values, MODE_NORMAL);
   }

   /**
    * Ctor
    * @param parent the parent shell, may be <code>null</code>
    * @param def the extension def cannot be <code>null</code>.
    * @param values the values map, this will be filled in when
    * ok is pressed, cannot be <code>null</code> but may be empty.
    * @param mode the mode indicating how the editor will appear,
    * one of the MODE_xxx constants.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public PSExtensionParamsDialog(
      Shell parent, IPSExtensionDef def, Map<String, IPSReplacementValue> values, 
      int mode)
   {
      super(parent);
      setShellStyle(getShellStyle() | SWT.RESIZE);
      if(def == null)
         throw new IllegalArgumentException("def cannot be null."); //$NON-NLS-1$
      if(values == null)
         throw new IllegalArgumentException("values cannot be null."); //$NON-NLS-1$
      m_mode = mode;
      // Retrieve and load all the runtime params
      Iterator it = def.getRuntimeParameterNames();
      Map<String, PSPair<String, IPSReplacementValue>> paramValues = 
         new HashMap<String, PSPair<String, IPSReplacementValue>>();
      PSPair pair = null;
      while(it.hasNext())
      {
         IPSExtensionParamDef param = 
            def.getRuntimeParameter((String)it.next());
         m_paramDefs.add(param);         
         pair = new PSPair<String, IPSReplacementValue>(param.getName(), null);
         paramValues.put(param.getName(), pair);
         m_values.add(pair);
      }
      // Set values for all params
      for(String key : values.keySet())
      {
         if(paramValues.containsKey(key))
         {
            pair = paramValues.get(key);
            pair.setSecond(values.get(key));
         }
         else
         {
            throw new IllegalArgumentException(
               "Invalid value. The parameter does not exist in the corresponding " + //$NON-NLS-1$
               "extension definition."); //$NON-NLS-1$
         }
      }
            
   }      

   /* 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @SuppressWarnings("unchecked")
   @Override
   protected void okPressed()
   {
      m_values = m_paramTable.getValues();
      super.okPressed();
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
      
      final Label parametersLabel = new Label(comp, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 20);
      formData.left = new FormAttachment(0, 10);
      parametersLabel.setLayoutData(formData);
      parametersLabel.setText(PSMessages.getString(
         "PSExtensionParamsDialog.parameters.label")); //$NON-NLS-1$
     
      ITableLabelProvider labelProvider =  new PSAbstractTableLabelProvider()
         {
            
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            public String getColumnText(Object element, int columnIndex)
            {
               if(element == null)
                  return ""; //$NON-NLS-1$
               PSPair<String, IPSReplacementValue> row = 
                  (PSPair<String, IPSReplacementValue>)element;
               switch(columnIndex)
               {
                  case 0:
                     return row.getFirst();
                  case 1:                     
                     if(row.getSecond() == null )
                        return "";
                     return row.getSecond().toString();
               }
               return null;
            }
         
         };
        
      IPSNewRowObjectProvider objectProvider = new IPSNewRowObjectProvider()
      {
         public Object newInstance()
         {
            return null;
         }

         public boolean isEmpty(@SuppressWarnings("unused") Object obj)
         {
            return false;
         }         
      };   
      m_paramTable = new PSSortableTable(comp, labelProvider,
         objectProvider, SWT.NONE,         
         PSSortableTable.SURPRESS_MANUAL_SORT);
      
      ICellModifier cModifier = m_mode == MODE_TEXT_LITERAL_ONLY
      ? new ExtParamsLiteralOnlyCellModifier(m_paramTable) 
         : new ExtParamsCellModifier(m_paramTable);
      m_paramTable.setCellModifier(cModifier);
      m_Table = m_paramTable.getTable();      
      final FormData formData_1 = new FormData();
      formData_1.height = 120;
      formData_1.right = new FormAttachment(98, 0);
      formData_1.top = new FormAttachment(parametersLabel, 0, SWT.BOTTOM);
      formData_1.left = new FormAttachment(0, 10);
      m_paramTable.setLayoutData(formData_1);
      m_paramTable.addSelectionListener(new SelectionAdapter()
         {

            @Override
            @SuppressWarnings({"synthetic-access"}) //$NON-NLS-1$
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               // Show description based on table selection
               int selection = m_Table.getSelectionIndex();
               if(selection == -1)
                  return;
               String desc = (m_paramDefs.get(selection)).getDescription();
               m_Text.setText(desc);               
            }
         
         });
      
      m_paramTable.addColumn(COL_NAME, PSSortableTable.NONE,
         new ColumnWeightData(10,100, true), null, SWT.LEFT);
            
      CellEditor cellEditor = null;
      if(m_mode == MODE_TEXT_LITERAL_ONLY)
      {
         cellEditor = 
            new TextCellEditor(m_Table);
      }
      else
      {
         cellEditor = 
            new PSValueSelectorCellEditor(m_Table,
               m_mode == MODE_LITERALS_ONLY);
      }
      
      m_paramTable.addColumn(COL_VALUE, PSSortableTable.NONE,
         new ColumnWeightData(10,100, true), cellEditor, SWT.LEFT);      
     
      
      m_paramTable.setValues(m_values);
      
      PSAutoResizeTableLayout tableLayout = 
         new PSAutoResizeTableLayout(m_Table);
      m_Table.setLayout(tableLayout);
      tableLayout.addColumnData(new ColumnWeightData(10,80, true));
      tableLayout.addColumnData(new ColumnWeightData(10,80, true)); 

      final Label parameterDescriptionLabel = new Label(comp, SWT.NONE);
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(m_paramTable, 10, SWT.BOTTOM);
      formData_2.left = new FormAttachment(0, 10);
      parameterDescriptionLabel.setLayoutData(formData_2);
      parameterDescriptionLabel.setText(PSMessages.getString(
         "PSExtensionParamsDialog.parameterDescription.label")); //$NON-NLS-1$

      m_Text = new Text(comp, SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
      m_Text.setForeground(
         getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
      m_Text.setBackground(
         getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
      final FormData formData_3 = new FormData();
      formData_3.height = 80;
      formData_3.right = new FormAttachment(98, 0);
      formData_3.top = new FormAttachment(parameterDescriptionLabel, 0, SWT.BOTTOM);
      formData_3.left = new FormAttachment(0, 10);
      m_Text.setLayoutData(formData_3);
      
      if(! m_paramTable.getValues().isEmpty())
      {
         String desc = (m_paramDefs.get(0)).getDescription();
         m_Text.setText(desc);
      }
      
      return comp;
   } 
   
   /* 
    * @see org.eclipse.jface.window.Window
    * #configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
     super.configureShell(newShell);
     newShell.setText(PSMessages.getString(
        "PSExtensionParamsDialog.title")); //$NON-NLS-1$
   }
   
   /**
    * Returns the param values that were set
    * @return never <code>null</code> may be empty.
    */
   public List<PSPair> getParamValues()
   {
      return m_values;
   }
   
   /**
    * Cell modifier for literal only and normal mode, created to work
    * with a PSValueSelectorCellEditor
    */
   class ExtParamsCellModifier implements ICellModifier
   {

      ExtParamsCellModifier(PSSortableTable viewer)
      {
         mi_viewer = viewer;
      }
      
      @SuppressWarnings("synthetic-access")
      public boolean canModify(@SuppressWarnings("unused") Object element,
         String property)
      {
         if(COL_NAME.equals(property))
            return false;
         return true;
      }

      @SuppressWarnings({"unchecked","synthetic-access"})
      public Object getValue(Object element, String property)
      {
         PSPair<String, IPSReplacementValue> rowVal = 
            (PSPair<String, IPSReplacementValue>)element;
         if(COL_NAME.equals(property))
            return rowVal.getFirst();
         else if(COL_VALUE.equals(property))
            return rowVal.getSecond();
         else
         return null;
      }

      @SuppressWarnings({"unchecked","synthetic-access"})
      public void modify(Object element, String property, Object value)
      {
         if(value == null)
            return;
         TableItem item = (TableItem)element;
         
         PSPair<String, IPSReplacementValue> rowVal = 
            (PSPair<String, IPSReplacementValue>)item.getData();         
         if(COL_NAME.equals(property))
            return;
         else if(COL_VALUE.equals(property))
            rowVal.setSecond((IPSReplacementValue)value); 
         mi_viewer.refreshTable();
      }
      
      PSSortableTable mi_viewer;
      
   }
   
   /**
    * Cell modifier for text literal only mode, created to work
    * with a TextCellEditor
    */
   class ExtParamsLiteralOnlyCellModifier implements ICellModifier
   {

      ExtParamsLiteralOnlyCellModifier(PSSortableTable viewer)
      {
         mi_viewer = viewer;
      }
      
      @SuppressWarnings("synthetic-access")
      public boolean canModify(@SuppressWarnings("unused") Object element,
         String property)
      {
         if(COL_NAME.equals(property))
            return false;
         return true;
      }

      @SuppressWarnings({"unchecked","synthetic-access"})
      public Object getValue(Object element, String property)
      {
         PSPair<String, IPSReplacementValue> rowVal = 
            (PSPair<String, IPSReplacementValue>)element;
         if(COL_NAME.equals(property))
            return rowVal.getFirst();
         else if(COL_VALUE.equals(property))
         {
            if(rowVal.getSecond() == null)
               return "";
            return rowVal.getSecond().getValueText();
         }
         else
         return null;
      }

      @SuppressWarnings({"unchecked","synthetic-access"})
      public void modify(Object element, String property, Object value)
      {
         if(value == null)
            return;
         TableItem item = (TableItem)element;
         
         PSPair<String, IPSReplacementValue> rowVal = 
            (PSPair<String, IPSReplacementValue>)item.getData();         
         if(COL_NAME.equals(property))
            return;
         else if(COL_VALUE.equals(property))
         {
            IPSReplacementValue val = null;
            DTTextLiteral dt = new DTTextLiteral();
            if(StringUtils.isNotBlank((String)value))
               val = (IPSReplacementValue)dt.create((String)value);
            rowVal.setSecond(val); 
         }
         mi_viewer.refreshTable();
      }
      
      PSSortableTable mi_viewer;
      
   }
   
   /* 
    * @see org.eclipse.jface.window.Window#getInitialSize()
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(700, 350);
   }  
  

   private Text m_Text;
   private Table m_Table;
   private PSSortableTable m_paramTable;
   private List<IPSExtensionParamDef> m_paramDefs = 
      new ArrayList<IPSExtensionParamDef>();   
   private List<PSPair> m_values = new ArrayList<PSPair>();
   
   /**
    * The mode the editor should use, one of the
    * MODE_xxx constants.
    */
   private int m_mode;
   
   private static final String COL_NAME = PSMessages.getString(
      "PSExtensionParamsDialog.col.name.lable"); //$NON-NLS-1$
   private static final String COL_VALUE = PSMessages.getString(
      "PSExtensionParamsDialog.col.value.label"); //$NON-NLS-1$
   
   
   
   /**
    * In this mode a value selector cell editor will be present
    * with all available types.
    */
   public static final int MODE_NORMAL = 0;
   
   /**
    * In this mode a value selector cell editor will be present
    * with only literal types.
    */
   public static final int MODE_LITERALS_ONLY = 1;
   
   /**
    * In this no value selector cell editor will be present, instead
    * a text cell editor will be used.
    */
   public static final int MODE_TEXT_LITERAL_ONLY = 2;
   
  
  
}

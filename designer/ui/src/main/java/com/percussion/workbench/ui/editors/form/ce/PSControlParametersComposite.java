/******************************************************************************
 *
 * [ PSControlParametersComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.E2Designer.DTTextLiteral;
import com.percussion.E2Designer.FormulaPropertyDialog;
import com.percussion.E2Designer.IDataTypeInfo;
import com.percussion.E2Designer.OSBackendDatatank;
import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.E2Designer.PSUdfSet;
import com.percussion.E2Designer.URLRequestUDFDialog;
import com.percussion.E2Designer.ValueSelectorDialogHelper;
import com.percussion.client.PSCoreFactory;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSControlParameter;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.util.PSCollection;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonedTextBoxCellEditor;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.dialog.PSValueSelectorDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Control parameter table composite. Creates a table with two columns. The
 * first column shows the dropdown list of control parameter names and second
 * column shows the parameter values. The parameter value is a replacement value
 * and is editable if it is not an extension. The extensions and other values
 * can be edited by openeing appropriate dialogs from popup menus.
 * 
 */
public class PSControlParametersComposite extends Composite
      implements
         IPSUiConstants
{

   /**
    * Constructor for the composite. Creates the controls and adds selection
    * listenrs.
    * 
    * @param parent The parent composite for this composite.
    * @param style The style that needs to be applied for this composite.
    * @param meta The control meta of the control for which the parameters needs
    *           to be displayed.
    */
   public PSControlParametersComposite(Composite parent, int style,
         PSControlMeta meta)
   {
      super(parent, style);
      if(meta == null)
         throw new IllegalArgumentException("meta must not be null"); //$NON-NLS-1$
      setLayout(new FormLayout());
      final Label parametersLabel = new Label(this, SWT.WRAP);
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(100, -10);
      formData_4.top = new FormAttachment(0, 0);
      formData_4.left = new FormAttachment(0, 10);
      parametersLabel.setLayoutData(formData_4);
      parametersLabel.setText(PSMessages.getString(
            "PSControlParametersComposite.label.Parameters")); //$NON-NLS-1$

      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public String getColumnText(Object element, int columnIndex)
         {
            if (element == null)
               return ""; //$NON-NLS-1$
            PSPair<String, IPSReplacementValue> row = 
               (PSPair<String, IPSReplacementValue>) element;
            switch (columnIndex)
            {
               case 0 :
                  return row.getFirst();
               case 1 :
                  if (row.getSecond() == null)
                     return ""; //$NON-NLS-1$
                  return row.getSecond().getValueDisplayText();
            }
            return null;
         }

      };
      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
      {
         public Object newInstance()
         {
            return new PSPair("", new PSTextLiteral("")); //$NON-NLS-1$
         }

         public boolean isEmpty(Object obj)
         {
            if (!(obj instanceof PSPair))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of PSPair."); //$NON-NLS-1$
            PSPair param = (PSPair) obj;
            boolean empty = false;
            if (StringUtils.isBlank((String) param.getFirst()))
               empty = true;
            else if (StringUtils.isBlank(((IPSReplacementValue) param
                  .getSecond()).getValueText()))
               empty = true;
            return empty;
         }
      };

      m_propstableComp = new PSSortableTable(this, labelProvider,
            newRowProvider, SWT.NONE, PSSortableTable.NONE
                  | PSSortableTable.INSERT_ALLOWED
                  | PSSortableTable.DELETE_ALLOWED);
      m_propstableComp
            .setCellModifier(new ParamsCellModifier(m_propstableComp));
      m_propstableComp.addSelectionListener(new SelectionAdapter(){
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if(m_propstableComp.getTable().getSelectionIndex() == -1)
               return;
            TableItem[] item = m_propstableComp.getTable().getSelection();
            final PSPair<String, IPSReplacementValue> pair = 
               (PSPair<String, IPSReplacementValue>) item[0].getData();
            final IPSReplacementValue val = pair.getSecond();
            if(val instanceof PSExtensionCall)
               m_valueText.setEditable(false);
            else
               m_valueText.setEditable(true);
         }
      });
      final FormData formData_4a = new FormData();
      formData_4a.left = new FormAttachment(parametersLabel, 0, SWT.LEFT);
      formData_4a.right = new FormAttachment(100, -10);
      formData_4a.height = 200;
      formData_4a.top = new FormAttachment(parametersLabel,
            COMBO_VSPACE_OFFSET, SWT.BOTTOM);
      m_propstableComp.setLayoutData(formData_4a);

      m_nameEditor = new PSComboBoxCellEditor(m_propstableComp.getTable(),
            new String[0], SWT.READ_ONLY);
      m_propstableComp.addColumn(COL_NAME, PSSortableTable.NONE, 
            new ColumnWeightData(10, 100), m_nameEditor, SWT.LEFT);
      setParamNamesList(meta);
      
      m_valueEditor = new PSButtonedTextBoxCellEditor(m_propstableComp
            .getTable(), SWT.NONE);
      m_valueText = (Text) m_valueEditor.getTextControl();
      Button valueButton = m_valueEditor.getButton();
      valueButton.addSelectionListener(new SelectionAdapter()
      {
         //Opens a URL request dialog
         public void widgetSelected(SelectionEvent e)
         {
            Menu menu = new Menu(getShell(), SWT.POP_UP);
            MenuItem link = new MenuItem(menu, SWT.NONE);
            link.setText(PSMessages.getString(
                  "PSControlParametersComposite.menu.label.link")); //$NON-NLS-1$
            link.addSelectionListener(new SelectionAdapter()
            {
               @Override
               public void widgetSelected(SelectionEvent e1)
               {
                  final Display display = getShell().getDisplay();
                  TableItem[] item = m_propstableComp.getTable().getSelection();
                  final PSPair<String, IPSReplacementValue> pair = 
                     (PSPair<String, IPSReplacementValue>) item[0].getData();
                  final IPSReplacementValue val = pair.getSecond();
                  SwingUtilities.invokeLater(new Runnable()
                  {
                     @SuppressWarnings("synthetic-access") //$NON-NLS-1$
                     public void run()
                     {
                        URLRequestUDFDialog dlg = new URLRequestUDFDialog();
                        if (val != null && val instanceof PSExtensionCall)
                        {
                           PSExtensionCall udf = (PSExtensionCall) val;
                           String canonicalName = udf.getExtensionRef()
                                 .getFQN();
                           if ((canonicalName.equals(MAKE_ABS_LINK))
                                 || (canonicalName.equals(MAKE_INT_LINK)))
                           {
                              dlg.setData(udf);
                           }
                        }
                        dlg.setVisible(true);
                        OSExtensionCall result = (OSExtensionCall) dlg
                              .getData();
                        dlg.dispose();
                        if (result != null)
                        {
                           final IPSReplacementValue newVal = 
                              (PSExtensionCall) result;
                           display.asyncExec(new Runnable()
                           {
                              public void run()
                              {
                                 pair.setSecond(newVal);
                                 m_valueText.setEditable(false);
                                 m_propstableComp.refreshTable();
                              }
                           });
                        }
                     }
                  });
               }
            });
            MenuItem udf = new MenuItem(menu, SWT.NONE);
            udf.setText(PSMessages.getString(
                  "PSControlParametersComposite.menu.label.udf")); //$NON-NLS-1$
            //Opens a UDF function dialog
            udf.addSelectionListener(new SelectionAdapter()
            {
               @Override
               public void widgetSelected(SelectionEvent e1)
               {
                  final Display display = getShell().getDisplay();
                  TableItem[] item = m_propstableComp.getTable().getSelection();
                  final PSPair<String, IPSReplacementValue> pair = 
                     (PSPair<String, IPSReplacementValue>) item[0]
                        .getData();
                  final IPSReplacementValue val = pair.getSecond();
                  SwingUtilities.invokeLater(new Runnable()
                  {
                     @SuppressWarnings("synthetic-access") //$NON-NLS-1$
                     public void run()
                     {
                        PSUdfSet udfSet = new PSUdfSet("ALL", PSCoreFactory //$NON-NLS-1$
                              .getInstance().getDesignerConnection());
                        FormulaPropertyDialog dlg = new FormulaPropertyDialog(
                              (Frame) null, udfSet, null, null);
                        if (val != null && val instanceof PSExtensionCall)
                        {
                           OSExtensionCall osCall = new OSExtensionCall(
                                 (PSExtensionCall) val);
                           dlg.setData(osCall);
                        }
                        dlg.setVisible(true);
                        OSExtensionCall result = (OSExtensionCall) dlg
                              .getData();
                        dlg.dispose();
                        if (result != null)
                        {
                           final IPSReplacementValue newVal = 
                              (PSExtensionCall) result;
                           display.asyncExec(new Runnable()
                           {
                              public void run()
                              {
                                 pair.setSecond(newVal);
                                 m_valueText.setEditable(false);
                                 m_propstableComp.refreshTable();
                              }
                           });
                        }
                     }
                  });
               }
            });
            MenuItem ov = new MenuItem(menu, SWT.NONE);
            ov.setText(PSMessages.getString(
                  "PSControlParametersComposite.menu.label.othervalue")); //$NON-NLS-1$
            ov.addSelectionListener(new SelectionAdapter()
            {
               public void widgetSelected(SelectionEvent e)
               {
                  // Open value selector dialog
                  final ValueSelectorDialogHelper helper = 
                     new ValueSelectorDialogHelper(
                           (OSBackendDatatank) null, null);
                  TableItem[] item = m_propstableComp.getTable().getSelection();
                  PSPair<String, IPSReplacementValue> pair = 
                     (PSPair<String, IPSReplacementValue>) item[0].getData();
                  IPSReplacementValue val = pair.getSecond();
                  List<IDataTypeInfo> types = new ArrayList();
                  Enumeration en = helper.getDataTypes().elements();
                  while (en.hasMoreElements())
                  {
                     IDataTypeInfo info = (IDataTypeInfo) en.nextElement();
                     types.add(info);
                  }
                  PSValueSelectorDialog dialog = new PSValueSelectorDialog(
                        m_propstableComp.getTable().getShell(), types, null,
                        val);

                  int status = dialog.open();
                  if (status == Dialog.OK)
                  {
                     IPSReplacementValue repVal = (IPSReplacementValue) dialog
                           .getValue();
                     pair.setSecond(repVal);
                     m_valueText.setText(repVal.getValueText());
                     m_valueText.setEditable(true);
                     m_propstableComp.refreshTable();
                  }
               };
            });
            menu.setVisible(true);
         }
      });
      m_propstableComp.addColumn(COL_VALUE, PSSortableTable.NONE,
            new ColumnWeightData(10, 100), m_valueEditor, SWT.LEFT);

      final Button deleteButton = new Button(this, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.width = BUTTON_WIDTH;
      formData_5.right = new FormAttachment(100, -10);
      formData_5.top = new FormAttachment(m_propstableComp,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      deleteButton.setLayoutData(formData_5);
      deleteButton.setText(PSMessages.getString(
            "PSControlParametersComposite.button.label.delete"));
      deleteButton.addSelectionListener(new SelectionAdapter()
      {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void widgetSelected(SelectionEvent e)
         {
            m_propstableComp.doDelete();
         }
      });
   }

   /**
    * Sets the parameters data. Creates a list of objects of PSPair from the
    * supplied iterator of PSParam objects and sets it as input to the params
    * table.
    * 
    * @param params Iterator of PSParam objects, If not throws
    *           IllegalArgumentException.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void setParamsData(Iterator params)
   {
      List<PSPair> pairs = new ArrayList<PSPair>();
      while (params != null && params.hasNext())
      {
         Object obj = params.next();
         if (!(obj instanceof PSParam))
            throw new IllegalArgumentException(
                  "Passed in params iterator is not of PSParam object type."); //$NON-NLS-1$
         PSParam param = (PSParam) obj;
         pairs.add(new PSPair(param.getName(), param.getValue()));
      }
      m_propstableComp.setValues(pairs);
   }

   /**
    * Gets the data from the table and creates a collection of the PSParam
    * objects and returns.
    * 
    * @return Collections of control parameters. May be empty but never
    *         <code>null</code>.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public PSCollection getParamsData()
   {
      PSCollection parameters = new PSCollection(PSParam.class);
      List<PSPair> pairs = m_propstableComp.getValues();
      for (PSPair pair : pairs)
      {
         parameters.add(new PSParam((String) pair.getFirst(),
               (IPSReplacementValue) pair.getSecond()));
      }
      return parameters;
   }

   /**
    * Sets the input to the parameter names drop down box
    * @param meta Control meta object, assumed not <code>null</code>.
    */
   private void setParamNamesList(PSControlMeta meta)
   {
      List<PSControlParameter> ctrlParams = meta.getParams();
      String[] params = new String[ctrlParams.size()];
      for (int i = 0; i < ctrlParams.size(); i++)
      {
         params[i] = ((PSControlParameter) ctrlParams.get(i)).getName();
      }
      m_nameEditor.setItems(params);
   }

   /**
    * Cell modifier for for the parameters table.
    */
   class ParamsCellModifier implements ICellModifier
   {

      ParamsCellModifier(PSSortableTable viewer)
      {
         mi_viewer = viewer;
      }

      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public boolean canModify(@SuppressWarnings("unused") //$NON-NLS-1$
      Object element, String property)
      {
         return true;
      }

      @SuppressWarnings(
      {"unchecked", "synthetic-access"}) //$NON-NLS-1$ //$NON-NLS-2$
      public Object getValue(Object element, String property)
      {
         PSPair<String, IPSReplacementValue> rowVal = 
            (PSPair<String, IPSReplacementValue>) element;
         if (COL_NAME.equals(property))
            return rowVal.getFirst();
         else if (COL_VALUE.equals(property))
         {
            if (rowVal.getSecond() != null)
               return rowVal.getSecond().getValueText();
            else
               return ""; //$NON-NLS-1$
         }
         else
            return null;
      }

      @SuppressWarnings(
      {"unchecked", "synthetic-access"}) //$NON-NLS-1$ //$NON-NLS-2$
      public void modify(Object element, String property, Object value)
      {
         if (value == null)
            return;
         TableItem item = (TableItem) element;

         PSPair<String, IPSReplacementValue> rowVal = 
            (PSPair<String, IPSReplacementValue>) item
               .getData();
         if (COL_NAME.equals(property))
            rowVal.setFirst((String) value);
         else if (COL_VALUE.equals(property))
         {
            if (rowVal.getSecond() instanceof PSExtensionCall)
               return;
            Object repValue = setDataTypeValue(rowVal.getSecond(), value);
            rowVal.setSecond((IPSReplacementValue) repValue);
         }
         mi_viewer.refreshTable();
      }

      PSSortableTable mi_viewer;
   }

   /**
    * Sets the value of the underlying data type object, i.e the replacement
    * value.
    * 
    * @param repValue, The replacement value for which the new value needs to be
    *           set. If <code>null</code> text literal replacement value will
    *           be created.
    * @param value If <code>null</code> or empty a text literal replacement
    *           value is created with empty value and returned.
    */
   private Object setDataTypeValue(Object repValue, Object value)
   {
      Object dt = null;
      if (value instanceof String)
      {
         if (repValue == null || (StringUtils.isBlank((String) value)))
            repValue = new DTTextLiteral().create(""); //$NON-NLS-1$
         IDataTypeInfo temp = convertToDTObject(repValue);
         dt = temp.create((String) value);
      }
      else
      {
         dt = value;
      }
      return dt;
   }

   /**
    * Helper method to get the appropriate designer data type object from the
    * specified replacement value server object
    * 
    * @param obj assumed not <code>null</code>.
    * @return the data type object, Never <code>null</code> if not found.
    */
   private IDataTypeInfo convertToDTObject(Object obj)
   {
      String thePackage = "com.percussion.E2Designer."; //$NON-NLS-1$
      String oldclassname = obj.getClass().getName();
      oldclassname = oldclassname.substring(oldclassname.lastIndexOf('.') + 1);
      if (!oldclassname.startsWith("PS")) //$NON-NLS-1$
         return null;
      String newclassname = thePackage + "DT" + oldclassname.substring(2); //$NON-NLS-1$
      try
      {
         Class dtClass = Class.forName(newclassname);
         return (IDataTypeInfo) dtClass.newInstance();
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
         throw new RuntimeException(e);

      }
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   public void dispose()
   {
      super.dispose();
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   protected void checkSubclass()
   {
   }

   /**
    * Sortable table component to display and edit the parameters. Initialized
    * in ctor and never <code>null</code> after that.
    */
   private PSSortableTable m_propstableComp;

   /**
    * Cell editor for the name column.
    */
   private PSComboBoxCellEditor m_nameEditor;

   /**
    * Cell editor for value column
    */
   private PSButtonedTextBoxCellEditor m_valueEditor;

   /**
    * Constant for the column name.
    */
   private static final String COL_NAME = PSMessages.getString(
         "PSControlParametersComposite.column.label.name"); //$NON-NLS-1$

   /**
    * Constant for the column value.
    */
   private static final String COL_VALUE = PSMessages.getString(
         "PSControlParametersComposite.column.label.value"); //$NON-NLS-1$

   /**
    * The text box of the value column cell editor.
    */
   private Text m_valueText;

   /** 
    * Name of the UDF that creates internal links 
    */
   protected static final String MAKE_INT_LINK = 
      "Java/global/percussion/generic/sys_MakeIntLink"; //$NON-NLS-1$

   /** 
    * Name of the UDF that creates external links 
    */
   protected static final String MAKE_ABS_LINK = 
      "Java/global/percussion/generic/sys_MakeAbsLink"; //$NON-NLS-1$

}

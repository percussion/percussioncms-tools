/******************************************************************************
 *
 * [ PSRelTypeFieldOverrideTableHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.OSExitCallSet;
import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.PSRuleEditorDialog;
import com.percussion.E2Designer.browser.PSCloningFieldOverridesTableModel;
import com.percussion.E2Designer.browser.PSRelationshipEditorDialog;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.design.objectstore.PSCloneOverrideField;
import com.percussion.design.objectstore.PSCloneOverrideFieldList;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRule;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.util.PSCollection;
import com.percussion.util.PSRemoteRequester;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonCellEditor;
import com.percussion.workbench.ui.controls.PSButtonedComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import static com.percussion.E2Designer.browser.PSCloningFieldOverridesTableModel.COL_COND;
import static com.percussion.E2Designer.browser.PSCloningFieldOverridesTableModel.COL_FIELD;
import static com.percussion.E2Designer.browser.PSCloningFieldOverridesTableModel.COL_UDF;
import static com.percussion.E2Designer.browser.PSCloningFieldOverridesTableModel.getExtensionDescription;
import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_WIDTH;
import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.THREE_FORTH;

/**
 * Clone field overrides management.
 *
 * @author Andriy Palamarchuk
 */
public class PSRelTypeFieldOverrideTableHelper
{
   /**
    * Initializes the UI.
    * @param editor is used to register the controls managed by the helper, for
    * sending notifications.
    */
   public void initUI(Composite container, Control previous,
         PSEditorBase editor)
   {
      initUdfs();
      final Label fieldTableLabel = createFieldTableLabel(container, previous);
      m_fieldOverrideTable = createFieldTable(container, fieldTableLabel);
      createDescriptionUI(container, m_fieldOverrideTable);
      editor.registerControl(
            fieldTableLabel.getText(), m_fieldOverrideTable, null);
      m_editor = editor;
   }

   /**
    * Initializes {@link #m_udfs}.
    *
    */
   private void initUdfs()
   {
      try
      {
         final IPSCmsModel model =
            PSCoreFactory.getInstance().getModel(PSObjectTypes.EXTENSION);
         final Collection<IPSReference> refs = model.catalog();
         final IPSReference[] refsArray =
               new ArrayList<IPSReference>(refs).toArray(new IPSReference[0]);
         final Object[] objects = model.load(refsArray, false, false);
         for (final Object o : objects)
         {
            final IPSExtensionDef exit = (IPSExtensionDef) o;
            if (exit.implementsInterface(OSExitCallSet.EXT_TYPE_UDF))
            {
               m_udfs.add(exit);
            }
         }
         Collections.sort(m_udfs, new Comparator<IPSExtensionDef>()
               {
                  public int compare(IPSExtensionDef o1, IPSExtensionDef o2)
                  {
                     return o1.getRef().getExtensionName().toLowerCase()
                           .compareTo(
                           o2.getRef().getExtensionName().toLowerCase());
                  }
               });
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
      catch (PSMultiOperationException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Creates the label description UI.
    * @param previousControl usually the properties table. 
    */
   private void createDescriptionUI(
         Composite container, Control previousControl)
   {
      final Label propDescLabel =
            createPropDescLabel(container, previousControl);
      m_fieldDescText = createDescriptionText(container, propDescLabel);
   }
   
   /**
    * Expression editor title label.
    */
   private Label createPropDescLabel(Composite container,
         Control previousControl)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(DESC_LABEL + ':');

      final FormData formData = new FormData();
      formData.top =
         new FormAttachment(previousControl, TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      formData.left = new FormAttachment(0, 0);
      label.setLayoutData(formData);
      return label;
   }
   
   /**
    * Creates field description text control. 
    */
   private Text createDescriptionText(Composite container, Label previousControl)
   {
      final Text text = new Text(container,
            SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, -FIELD_TABLE_BUTTON_SPACE);
      formData.top = new FormAttachment(previousControl, 0, SWT.BOTTOM);
      formData.bottom = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      text.setLayoutData(formData);
      
      return text;
   }
   
   /**
    * Creates field override table.
    */
   private PSSortableTable createFieldTable(Composite container,
         Control previous)
   {
      IPSNewRowObjectProvider rowObjectProvider = new IPSNewRowObjectProvider()
      {
         public Object newInstance()
         {
            final Vector<Object> empty = new Vector<Object>();
            empty.add("");                                                      //$NON-NLS-1$
            empty.add(null);
            empty.add(new PSCollection(PSRule.class));
            return empty; 
         }

         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public boolean isEmpty(Object o)
         {
            if (!(o instanceof Vector))
            {
               throw new IllegalArgumentException(
                     "The passed in object must be override data vector.");     //$NON-NLS-1$
            }
            return StringUtils.isBlank(
                  getOverrideName((Vector<Object>) o));
         }
      };
      final ITableLabelProvider labelProvider =
            new PSAbstractTableLabelProvider()
      {
         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public String getColumnText(Object element, int columnIndex)
         {
            final Vector<Object> rowData = (Vector<Object>) element;
            switch (columnIndex)
            {
               case COL_FIELD:
                  return getOverrideName(rowData);
               case COL_UDF:
                  final OSExtensionCall udf = getUDF(rowData);
                  return udf == null ? "" : udf.toString();                     //$NON-NLS-1$
               case COL_COND:
                  return "C";                                                   //$NON-NLS-1$
               default:
                  throw new AssertionError();
            }
         }
      };
      final PSSortableTable table =
            new PSSortableTable(container, labelProvider, rowObjectProvider,
                  SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI,
                  PSSortableTable.SURPRESS_MANUAL_SORT
                  | PSSortableTable.DELETE_ALLOWED
                  | PSSortableTable.INSERT_ALLOWED
                  | PSSortableTable.SHOW_DELETE);

      specifyTableLayoutData(table, previous);
      specifyTableColumns(table);
      
      // Add listener to fill the description field upon row selection
      table.getTable().addSelectionListener(new SelectionAdapter()
         {
            @Override
            @SuppressWarnings("unused")
            public void widgetSelected(SelectionEvent e)
            {
               onFieldOverrideSelected();
            }
         });
      table.setCellModifier(new CellModifier(table));
      return table;
   }
   
   /**
    * Sets table layout.
    */
   private void specifyTableLayoutData(final PSSortableTable table, Control previous)
   {
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right =
            new FormAttachment(100, -PSSortableTable.TABLE_BUTTON_HSPACE);
      formData.top = new FormAttachment(previous, 0, 0);
      formData.bottom = new FormAttachment(THREE_FORTH, 0);
      table.setLayoutData(formData);
   }

   /**
    * Defines columns in the provided field overrides table.
    */
   private void specifyTableColumns(PSSortableTable table)
   {
      {
         try
         {
            m_ceSystemFields = getSystemFields();
         }
         catch (PSCmsException e)
         {
            throw new RuntimeException(e);
         }
         final String[] systemFieldsArray =
               m_ceSystemFields.toArray(new String[0]);
         final CellEditor cellEditor = new ComboBoxCellEditor(
               table.getTable(), systemFieldsArray, SWT.READ_ONLY);
         table.addColumn(getMessage("column.field"),                          //$NON-NLS-1$
               PSSortableTable.IS_SORTABLE,
               new ColumnWeightData(8), cellEditor, SWT.LEFT);
      }
      {
         final PSButtonedComboBoxCellEditor cellEditor =
               new PSButtonedComboBoxCellEditor(
                     table.getTable(), m_udfs.toArray(), SWT.READ_ONLY)
         {
            @SuppressWarnings("unchecked")
            @Override
            protected void doSetValue(Object value)
            {
               super.doSetValue(value);
               getButton().setEnabled(value != null);
            }
         };
         cellEditor.setLabelProvider(new LabelProvider()
               {
                  @Override
                  public String getText(Object element)
                  {
                     final IPSExtensionDef udf = (IPSExtensionDef) element;
                     return udf == null ? "" : udf.getRef().getExtensionName(); //$NON-NLS-1$
                  }
               });
         cellEditor.getCombo().addSelectionListener(new SelectionAdapter()
               {
                  @Override
                  public void widgetSelected(SelectionEvent e)
                  {
                     cellEditor.getButton().setEnabled(true);

                     final CCombo combo = (CCombo) e.getSource();
                     final IPSExtensionDef udfDef =
                           m_udfs.get(combo.getSelectionIndex());
                     final Vector<Object> rowData = getSelectedOverrideData();
                     final OSExtensionCall oldUdf = getUDF(rowData);

                     if (oldUdf != null
                           && udfDef.equals(oldUdf.getExtensionDef()))
                     {
                        return;
                     }
                     asyncCreateUdf(rowData, udfDef);
                     cellEditor.deactivate();
                  }
               });
         cellEditor.getButton().addSelectionListener(new SelectionAdapter()
               {
                  @Override
                  @SuppressWarnings("unused")
                  public void widgetSelected(SelectionEvent e)
                  {
                     final Vector<Object> rowData = getSelectedOverrideData();
                     final OSExtensionCall udf = getUDF(rowData);
                     if (udf == null)
                     {
                        // has not been created yet
                        // user should select extension from the dropdown first
                        return;
                     }
                     asyncEditUdf(rowData, udf);
                     cellEditor.deactivate();
                  }
               });
         table.addColumn(getMessage("column.udf"), PSSortableTable.NONE,      //$NON-NLS-1$
               new ColumnWeightData(8, false), cellEditor, SWT.CENTER);
      }

      {
         final PSButtonCellEditor cellEditor =
            new PSButtonCellEditor(table.getTable(), SWT.NONE);
         cellEditor.getButton().addSelectionListener(new SelectionAdapter()
               {
                  @Override
                  @SuppressWarnings("unused")
                  public void widgetSelected(SelectionEvent e)
                  {
                     final PSCollection rules =
                           getConditions(getSelectedOverrideData());
                     SwingUtilities.invokeLater(new Runnable()
                           {
                              public void run()
                              {
                                 editRules(rules, false);
                                 asyncRefreshFieldOverrideTable();
                              }
                           });
                  }
               });
         table.addColumn(getMessage("column.condition"), PSSortableTable.NONE,       //$NON-NLS-1$
               new ColumnWeightData(1, false), cellEditor, SWT.LEFT);
      }
   }

   /**
    * Loads system fields. Code is borrowed from PSRelationshipEditorDialog.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private List<String> getSystemFields() throws PSCmsException
   {
      //get CE system fields
      //T O D O (Andriy: was in borrowed code):
      //currently this field list all search fields which need to be
      //filtered to rstrict to writable fields.
      PSRemoteRequester appReq =
         PSCoreFactory.getInstance().getRemoteRequester();
      PSRemoteCataloger remCatlg = new PSRemoteCataloger(appReq);

      PSContentEditorFieldCataloger fieldCatlgObj =
         new PSContentEditorFieldCataloger(remCatlg, null,
            IPSFieldCataloger.FLAG_INCLUDE_RESULTONLY);

      final List<String> systemFields = new ArrayList<String>(
            fieldCatlgObj.getSystemMap().keySet());
      Collections.sort(systemFields);
      return systemFields;
   }

   /**
    * Creates a label above cloning fields overrides table. 
    */
   private Label createFieldTableLabel(Composite container, Control previous)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(getMessage("label.table") + ':');                           //$NON-NLS-1$
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.top = new FormAttachment(previous, 0, SWT.BOTTOM);
      label.setLayoutData(formData);
      return label;
   }
   
   /**
    * Load controls with the relationship type values.
    */
   public void loadControlValues(final PSRelationshipConfig relType)
   {
      final Vector<Vector<Object>> fieldOverrideData =
            loadCloningFieldOverrideData(relType);
      
      m_fieldOverrideTable.setValues(fieldOverrideData);
      final boolean hasOverrides = !fieldOverrideData.isEmpty();
      if (hasOverrides)
      {
         m_fieldOverrideTable.getTable().select(0);
         onFieldOverrideSelected();
      }
      else
      {
         m_fieldDescText.setText(""); //$NON-NLS-1$
      }
      m_fieldOverrideTable.refreshTable();
   }
   
   /**
    * Called when selection is changed in field override table.
    */
   private void onFieldOverrideSelected()
   {
      final Table table = m_fieldOverrideTable.getTable();
      if (table.getSelectionCount() == 0)
      {
         m_fieldDescText.setText(""); //$NON-NLS-1$                  
      }
      else
      {
         final Vector<Object> row = getSelectedOverrideData();
         m_fieldDescText.setText(getExtensionDescription(getUDF(row)));
      }
   }
   
   /**
    * Extracts the UDF data.
    */
   private OSExtensionCall getUDF(Vector<Object> rowData)
   {
      return (OSExtensionCall) rowData.get(COL_UDF);
   }

   /**
    * Saves the UDF data.
    */
   private void setUDF(Vector<Object> rowData, OSExtensionCall udf)
   {
      rowData.set(COL_UDF, udf);
   }

   /**
    * Extracts override field name.
    */
   private String getOverrideName(Vector<Object> rowData)
   {
      return (String) rowData.get(COL_FIELD);
   }

   /**
    * Saves new field value.
    */
   private void setOverrideName(Vector<Object> rowData, String name)
   {
      rowData.set(COL_FIELD, name);
   }
   
   /**
    * Conditions.
    */
   private PSCollection getConditions(Vector<Object> rowData)
   {
      return (PSCollection) rowData.get(COL_COND);
   }

   /**
    * Override field data corresponding to currently selected item in the table.
    * Table must have a selection.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private Vector<Object> getSelectedOverrideData()
   {
      return (Vector<Object>) getSelectedOverrideTableItem().getData();
   }

   /**
    * Currently selected override table item.
    * Table must have a selection.
    */
   private TableItem getSelectedOverrideTableItem()
   {
      final Table table = m_fieldOverrideTable.getTable();
      return table.getItem(table.getSelectionIndices()[0]);
   }

   /**
    * Loads shallow/deep cloning-related data. 
    */
   private Vector<Vector<Object>> loadCloningFieldOverrideData(
         final PSRelationshipConfig relType)
   {
      PSCloneOverrideFieldList overrideFields = relType
            .getCloneOverrideFieldList();
      if (overrideFields == null)
         return new Vector<Vector<Object>>();

      return PSCloningFieldOverridesTableModel.buildCloningFieldOverridesData(
            overrideFields.iterator());
   }

   /**
    * Updates relationship type with the controls selection.
    */
   public void updateRelType(PSRelationshipConfig relType)
   {
      relType.setCloneOverrideFieldList(getDataForUpdate());
   }
   
   /**
    * Generates cloning fields overrides to update relationship type.
    * Borrowed from {@link PSCloningFieldOverridesTableModel#getData()}.
    * Had to borrow, not reuse because the original version accesses cells. 
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private PSCloneOverrideFieldList getDataForUpdate()
   {
      PSCloneOverrideFieldList cloneOverrides = new PSCloneOverrideFieldList();
      for (final Object rowDataObj : m_fieldOverrideTable.getValues())
      {
         final Vector<Object> rowData = (Vector<Object>) rowDataObj;  

         final PSCloneOverrideField cloneOverride =
            new PSCloneOverrideField(getOverrideName(rowData), getUDF(rowData));
         cloneOverride.setRules(getConditions(rowData));
         cloneOverrides.add(cloneOverride);
      }
      
      return cloneOverrides;
   }

   /**
    * Edits list of rules.
    * Copied from legacy code in
    * {@link com.percussion.E2Designer.browser.PSRelationshipEditorDialog}.
    * @param editCloningConditionals should be <code>true</code> if is called
    * for editing cloning conditionals, such as "shallow" or "deep" cloning
    * options and <code>false</code> if called for field overrides.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void editRules(List rules, boolean editCloningConditionals)
   {
      try
      {
         /*
          * Pull out the first rule if this is the cloning view
          * as this is the rule that determines if this property
          * is checked or not and should not be editable other then
          * by checking or unchecking.
          */
         PSRule firstConditional = null;

         if (editCloningConditionals && !rules.isEmpty())
         {
            firstConditional = (PSRule) rules.get(0);
            rules.remove(0);
         }
         
         PSRuleEditorDialog ruleDlg = new PSRuleEditorDialog((JFrame) null);
         E2Designer.getApp().getMainFrame().registerDialog(ruleDlg);
         ruleDlg.center();
         ruleDlg.onEdit(rules.iterator());
         ruleDlg.setVisible(true);
         rules.clear();
         // Put the removed conditional back to the top of
         // the list if necessary
         if (firstConditional != null)
         {
            rules.add(firstConditional);
         }
         Iterator it = ruleDlg.getRulesIterator();
         while(it.hasNext())
         {
            final PSRule rule = (PSRule) it.next();
            rules.add(rule);
         }

      }
      catch (ClassNotFoundException e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Have to edit asynchroniously because use existing Swing UI. 
    */
   private void asyncEditUdf(final Vector<Object> rowData,
         final OSExtensionCall udf)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            final OSExtensionCall editedUdf =
               PSRelationshipEditorDialog.editExitCall(udf);
            if (editedUdf != null)
            {
               setUDF(rowData, editedUdf);
            }
            asyncRefreshFieldOverrideTable();
         }
      });
   }

   /**
    * Have to create asynchroniously because use existing Swing UI. 
    */
   private void asyncCreateUdf(final Vector<Object> rowData,
         final IPSExtensionDef udfDef)
   {
      SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  final OSExtensionCall editedUdf =
                     PSRelationshipEditorDialog.createExitCall(udfDef);
                  if (editedUdf != null)
                  {
                     setUDF(rowData, editedUdf);
                  }
                  asyncRefreshFieldOverrideTable();
               }
            });
   }
   
   /**
    * Refreshes {@link #m_fieldOverrideTable} asyncroniously after update.
    * Also notifies the editor about the update.
    * Should be called from processing in other threads than SWT thread.
    */
   private void asyncRefreshFieldOverrideTable()
   {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            m_fieldOverrideTable.refreshTable();
            m_editor.onControlModified(m_fieldOverrideTable, false);
         }
      });
   }

   /**
    * Convenience method to get message.
    */
   private static String getMessage(final String key)
   {
      return PSMessages.getString("PSRelTypeFieldOverrideTableHelper." + key);  //$NON-NLS-1$
   }

   /**
    * Column index for given column name.
    */
   private int getColumnIndex(String propertyName)
   {
      final int idx = m_fieldOverrideTable.getColumnIndex(propertyName);
      if (idx == -1)
      {
         throw new IllegalArgumentException(
               "Unrecognized column name: " + propertyName);   //$NON-NLS-1$
      }
      return idx;
   }

   /**
    * Cell modifier for the properties table
    */
   private class CellModifier implements ICellModifier
   {
      CellModifier(PSSortableTable tableComp)
      {
         mi_tableComp = tableComp;
      }

      /**
       * Returns <code>true</code> only for value field.
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
       */
      public boolean canModify(@SuppressWarnings("unused") Object element,
            String propertyName)
      {
         switch (getColumnIndex(propertyName))
         {
            case COL_FIELD:
               return true;
            case COL_UDF:
               return true;
            case COL_COND:
               return true;
            default:
               throw new IllegalArgumentException(
                     "Unrecognized column " + getColumnIndex(propertyName)); //$NON-NLS-1$
         }
      }

      // see base
      @SuppressWarnings("unchecked") //$NON-NLS-1$
      public Object getValue(Object element, String propertyName)
      {
         final int column = getColumnIndex(propertyName);
         final Vector<Object> rowData = (Vector<Object>) element;
         switch (column)
         {
            case COL_FIELD:
               final String name = getOverrideName(rowData);
               return m_ceSystemFields.indexOf(name);
            case COL_UDF:
               final OSExtensionCall udf = getUDF(rowData);
               return udf == null ? null : udf.getExtensionDef();
            case COL_COND:
               return getConditions(rowData);
            default:
               throw new IllegalArgumentException(
                     "Unrecognized column " + column); //$NON-NLS-1$
         }
      }

      // see base
      @SuppressWarnings("unchecked") //$NON-NLS-1$
      public void modify(Object element, String propertyName, Object value)
      {
         final int column = getColumnIndex(propertyName);
         final TableItem item = (TableItem) element;
         final Vector<Object> rowData = (Vector<Object>) item.getData();
         switch (column)
         {
            case COL_FIELD:
               final int idx = (Integer) value;
               if (idx == -1)
               {
                  break;
               }
               final String name = m_ceSystemFields.get(idx);
               setOverrideName(rowData, name);
               break;
            case COL_UDF:
               // handled in the combo listener because the editing dialog
               // should be triggered on selection and this is too late here
               if (value == null)
               {
                  setUDF(rowData, null);
                  break;
               }
               break;
            case COL_COND:
               // handled in the combo listener because Swing legacy UI is used
               // for editing and it must run asynchroniously.
               break;
            default:
               throw new AssertionError("Unrecognized column " + column); //$NON-NLS-1$
         }
         mi_tableComp.refreshTable();
      }

      private PSSortableTable mi_tableComp;
   }

   /**
    * Label text for UDF description
    */
   public static final String DESC_LABEL = getMessage("label.description");     //$NON-NLS-1$

   /**
    * Space taken by table buttons.
    */
   public final static int FIELD_TABLE_BUTTON_SPACE =
         PSSortableTable.TABLE_BUTTON_HSPACE * 2 + BUTTON_WIDTH;

   /**
    * Text field to view field description.
    */
   private Text m_fieldDescText;

   /**
    * Field overrides table
    */
   private PSSortableTable m_fieldOverrideTable;
   
   /**
    * Contains an ordered list of CE system fields. Initilized by
    * the ctor and never <code>null</code> after that.
    */
   private List<String> m_ceSystemFields;
   
   /**
    * The list of UDFs (<code>IPSUdfProcessor</code>) available to
    * associate with a relationship, initialized in the constructor and never
    * <code>null</code> or modified after that. May be empty.
    */
   private List<IPSExtensionDef> m_udfs = new ArrayList<IPSExtensionDef>();
   
   /**
    * Editor owning UI created by this class.
    */
   private PSEditorBase m_editor;
}

/******************************************************************************
 *
 * [ PSCEFieldsCommonComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.catalogers.PSSqlCataloger;
import com.percussion.client.objectstore.IPSCETableColumnActions;
import com.percussion.client.objectstore.PSUiContentEditorSharedDef;
import com.percussion.client.objectstore.PSUiItemDefinition;
import com.percussion.cms.objectstore.PSFieldDefinition;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSChoices;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSControlDependencyMap;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDependency;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonedComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.form.PSContentTypeEditor;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import com.percussion.workbench.ui.editors.form.PSSharedDefEditor;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A common composite to hold the fields table and the field and fieldset
 * properties composites.
 */
public class PSCEFieldsCommonComposite extends Composite
      implements
         IPSUiConstants,
         SelectionListener,
         IPSDesignerObjectUpdater
{

   /**
    * Constructor
    */
   public PSCEFieldsCommonComposite(Composite parent, int style,
         PSEditorBase editor, String tableLabel, int editorType,
         String fieldSetName)
   {
      super(parent, style);
      if (!PSContentEditorDefinition.isValidEditorType(editorType))
      {
         throw new IllegalArgumentException("editorType is invalid"); //$NON-NLS-1$
      }
      m_editorType = editorType;
      m_tableLabel = tableLabel;
      m_editor = editor;
      m_fieldSetName = fieldSetName;
      m_fieldData = new ArrayList<PSFieldTableRowDataObject>();
      if (m_editorType == PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR
            || m_editorType == PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
      {
         PSItemDefinition itemDef = (PSItemDefinition) m_editor
               .getDesignerObject();
         PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
               .getContentEditor().getPipe();
         m_ctrlDepMap = pipe.getControlDependencyMap();
      }      
      setLayout(new FormLayout());
      createControls(parent);
   }

   /*
    * Creates the controls of this composite
    */
   private void createControls(Composite parent)
   {
      m_fieldTableLabel = new Label(this, SWT.WRAP);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      m_fieldTableLabel.setLayoutData(formData);
      m_fieldTableLabel.setText(m_tableLabel);

      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         public String getColumnText(Object element, int columnIndex)
         {
            PSFieldTableRowDataObject rowData = 
               (PSFieldTableRowDataObject) element;
            switch (columnIndex)
            {
               case 0 :
                  return StringUtils.defaultString(rowData.getName());
               case 1 :
                  return StringUtils.defaultString(rowData.getLabelText());
               case 2 :
                  return StringUtils.defaultString(rowData.getControlName());
               case 3 :
               {
                  //don't show the text until there is a non-empty row 
                  if (rowData.getFieldDefinition() == null)
                     return "";
                  //default to complex child, which has no field
                  int type = PSField.TYPE_LOCAL;
                  if (rowData.getField() != null)
                  {
                     type = rowData.getField().getType();
                  }
                  return getLabelForFieldTypeId(type);
               }
            }
            return ""; // should never get here //$NON-NLS-1$
         }
      };

      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
      {
         @SuppressWarnings("synthetic-access")//$NON-NLS-1$
         public Object newInstance()
         {
            PSFieldTableRowDataObject newRow = new PSFieldTableRowDataObject();
            showPropertyComposites(false, false);
            return newRow;
         }

         public boolean isEmpty(Object obj)
         {
            if (!(obj instanceof PSFieldTableRowDataObject))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of PSFieldTableRowDataObject."); //$NON-NLS-1$
            PSFieldTableRowDataObject fieldNSet = 
               (PSFieldTableRowDataObject) obj;
            return fieldNSet.isEmpty();
         }
      };

      int tableOptions = PSSortableTable.INSERT_ALLOWED;
      // System fields are not deletable.
      if (m_editorType != PSContentEditorDefinition.SYSTEMDEF_EDITOR)
         tableOptions = tableOptions | PSSortableTable.DELETE_ALLOWED;

      if (m_editorType == PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
         tableOptions = tableOptions | PSSortableTable.SHOW_ALL;
      else if (m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
         tableOptions = tableOptions | PSSortableTable.SHOW_DELETE;
      else
         tableOptions = tableOptions | PSSortableTable.NONE;

      m_fieldsTable = new CeSortableTable(this, labelProvider, newRowProvider,
            SWT.NONE, tableOptions);

      m_fieldsTable.setCellModifier(new CellModifier(m_fieldsTable));
      final FormData formData_1 = new FormData();
      formData_1.left = new FormAttachment(m_fieldTableLabel, 0, SWT.LEFT);
      formData_1.right = new FormAttachment(100, 0);
      formData_1.bottom = new FormAttachment(50, 0);
      formData_1.top = new FormAttachment(m_fieldTableLabel,
            COMBO_VSPACE_OFFSET, SWT.BOTTOM);
      m_fieldsTable.setLayoutData(formData_1);
      m_table = m_fieldsTable.getTable();
      // Create the cell editors

      // Name column editor
      m_nameCellEditor = new TextCellEditor(m_fieldsTable.getTable(), SWT.NONE);
      m_fieldsTable
            .addColumn(
                  PSMessages.getString(
                        "PSCEFieldsCommonComposite.tablecolumn.name"), 
                        PSSortableTable.NONE, //$NON-NLS-1$
                  new ColumnWeightData(10, 100), m_nameCellEditor, SWT.LEFT);

      // Label column editor
      m_labelCellEditor = 
         new TextCellEditor(m_fieldsTable.getTable(), SWT.NONE);

      m_fieldsTable
            .addColumn(
                  PSMessages.getString(
                        "PSCEFieldsCommonComposite.tablecolumn.label"), 
                        PSSortableTable.NONE, //$NON-NLS-1$
                  new ColumnWeightData(7, 40), m_labelCellEditor, SWT.LEFT);

      // Control column editor
      try
      {
         m_ctrlCellEditor = new PSButtonedComboBoxCellEditor(m_fieldsTable
               .getTable(), PSContentEditorDefinition
               .getControlNames(PSContentEditorDefinition.ALL_CONTROLS),
               SWT.READ_ONLY);
      }
      catch (PSModelException e1)
      {
         PSWorkbenchPlugin
               .handleException(
                     "Control List Generation", //$NON-NLS-1$
                     PSMessages.getString(
                           "PSCEFieldsCommonComposite.error.controllist.title"), //$NON-NLS-1$
                     PSMessages.getString(
                           "PSCEFieldsCommonComposite.error.controllist.message")
                           , e1); //$NON-NLS-1$
      }

      final CCombo ctrlCombo = m_ctrlCellEditor.getCombo();
      ctrlCombo.addSelectionListener(this);
      m_ctrlCellEditor.getButton().addSelectionListener(this);

      m_fieldsTable
            .addColumn(
                  PSMessages.getString(
                        "PSCEFieldsCommonComposite.tablecolumn.control"), //$NON-NLS-1$
                        PSSortableTable.NONE, 
                  new ColumnWeightData(10, 100), m_ctrlCellEditor, SWT.LEFT);

      if (m_editorType == PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR)
      {
         m_fieldsTable
            .addColumn(
                  PSMessages.getString(
                        "PSCEFieldsCommonComposite.tablecolumn.source"), //$NON-NLS-1$
                        PSSortableTable.NONE, 
                  new ColumnWeightData(4, 10), null, SWT.LEFT);
      }
      
      // Add listener to fill the value box upon row selection
      m_fieldsTable.addSelectionListener(this);
      if (m_editorType == PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR)
         m_fieldsTable.addSelectionListener((SelectionListener) parent);
      m_editor
            .registerControl(PSMessages.getString(
                  "PSCEFieldsCommonComposite.table.name"), m_fieldsTable, null); //$NON-NLS-1$

      final FormData formData_2 = new FormData();
      formData_2.left = new FormAttachment(m_fieldTableLabel, 0, SWT.LEFT);
      formData_2.right = new FormAttachment(100, 0);
      formData_2.bottom = new FormAttachment(100, 0);
      formData_2.top = new FormAttachment(m_fieldsTable, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);

      m_staticFieldLabel = new Label(this, SWT.WRAP);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_fieldsTable, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_3.left = new FormAttachment(0, 0);
      formData_3.right = new FormAttachment(100, 0);
      m_staticFieldLabel.setLayoutData(formData_3);
      m_staticFieldLabel.setText(PSMessages
            .getString("PSCEFieldsCommonComposite.staticField.label"));
      m_staticFieldLabel.setVisible(false);
      
      m_fieldSetProperties = new PSFieldSetPropertiesComposite(this, SWT.NONE,
            m_editorType, m_editor);
      m_fieldSetProperties.setLayoutData(formData_2);

      m_fieldProperties = new PSFieldPropertiesComposite(this, SWT.NONE,
            m_editorType, m_editor);
      m_fieldProperties.setLayoutData(formData_2);

      showPropertyComposites(false, false);
   }

   /**
    * Maps the supplied type to a message key, then retrieves the associated
    * value. The returned string is suitable for displaying to users.
    * 
    * @param type One of the values returned by the {@link PSField#getType()}.
    * Any other value will be treated as if it was {@link PSField#TYPE_LOCAL}.
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getLabelForFieldTypeId(int type)
   {
      String labelKey;
      switch (type)
      {
         case PSField.TYPE_SYSTEM:
            labelKey = "PSCEFieldsCommonComposite.fieldType.system";
            break;
         case PSField.TYPE_SHARED:
            labelKey = "PSCEFieldsCommonComposite.fieldType.shared";
            break;
         case PSField.TYPE_LOCAL:
         default:
            labelKey = "PSCEFieldsCommonComposite.fieldType.local";
            break;
      }
      return PSMessages.getString(labelKey);
   }
   
   @Override
   public void dispose()
   {
      super.dispose();
   }

   /**
    * Disables Eclipse subclassing check.
    */
   @Override
   protected void checkSubclass()
   {
   }

   /**
    * Cell modifier for the fields and fieldsets table
    */
   class CellModifier implements ICellModifier
   {

      CellModifier(PSSortableTable comp)
      {
         mi_tableComp = comp;
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(
       *      java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public boolean canModify(Object element, String property)
      {
         PSFieldTableRowDataObject rowData = 
            (PSFieldTableRowDataObject) element;
         // Return false for new rows and if the editor type is systemdef as we
         // are not supporting adding new fields to system def through UI
         if (rowData.getFieldDefinition() == null
               && m_editorType == PSContentEditorDefinition.SYSTEMDEF_EDITOR)
            return false;
         // Return false for new rows and if the editor type is shareddef
         // and if it has a only one field and it is a simple child field
         // as we do not support a simple child field and other fields in
         // a single shared group.
         if (rowData.getFieldDefinition() == null
               && m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
         {
            PSFieldTableRowDataObject fRowData = 
               (PSFieldTableRowDataObject) m_table.getItem(0).getData();
            if (!fRowData.isEmpty())
            {
               try
               {
                  PSControlMeta ctrl = PSContentEditorDefinition
                        .getControl(fRowData.getControlName());
                  if (ctrl != null && ctrl.getDimension()
                        .equals(PSControlMeta.ARRAY_DIMENSION))
                  {
                     return false;
                  }
               }
               catch (PSModelException e)
               {
                  // This should not happen otherwise we would have come this
                  // far.
                  throw new RuntimeException(e);
               }
            }
         }

         // Return false if it is control column and control is table control.
         if (rowData.getFieldDefinition() != null && rowData.isFieldSet()
               && m_fieldsTable.getColumnIndex(property) == 2)
            return false;

         return true;
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       *      java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public Object getValue(Object element, String property)
      {
         int col = m_fieldsTable.getColumnIndex(property);
         PSFieldTableRowDataObject field = (PSFieldTableRowDataObject) element;
         switch (col)
         {
            case 0 :
               return field.getName();
            case 1 :
               return field.getLabelText();
            case 2 :
               return field.getControlName();
         }
         return ""; //$NON-NLS-1$
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#modify( java.lang.Object,
       *      java.lang.String, java.lang.Object)
       */
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void modify(Object element, String property, Object value)
      {
         int col = m_fieldsTable.getColumnIndex(property);
         TableItem item = (TableItem) element;
         PSFieldTableRowDataObject rowData = (PSFieldTableRowDataObject) item
               .getData();
         if (rowData.getFieldDefinition() == null && col != 0)
            return;

         String val = (String) value;
         switch (col)
         {
            case 0 :
               if (rowData.getFieldDefinition() == null)
               {
                  if (validateFieldName(val, null))
                  {
                     PSUISet uiSet = null;
                     try
                     {
                        uiSet = PSContentEditorDefinition.getDefaultUISet(val,
                              m_editorType);
                     }
                     catch (PSModelException e)
                     {
                        String title = PSMessages.getString(
                              "PSCEFieldsCommonComposite.error.defaultuiset.title"); //$NON-NLS-1$
                        String msg = PSMessages.getString(
                              "PSCEFieldsCommonComposite.error.defaultuiset.message"); //$NON-NLS-1$
                        PSWorkbenchPlugin.handleException(
                              "CE default UISet", title, msg, e); //$NON-NLS-1$
                     }
                     PSField fld = PSContentEditorDefinition.getDefaultField(
                           val, m_editorType);
                     PSDisplayMapping mapping = new PSDisplayMapping(val, uiSet);
                     // Check whether any unused column exists in the table with
                     // the name of the field.
                     IPSBackEndMapping loc = getMatchingLocator(val);
                     if (loc != null)
                     {
                        String reuseDbColumnQTitle = PSMessages.getString(
                              "PSCEFieldsCommonComposite.error.reusedatabasecolumn.title"); //$NON-NLS-1$
                        String reuseDbColumnQMsg = PSMessages.getString(
                              "PSCEFieldsCommonComposite.error.reusedatabasecolumn.message"); //$NON-NLS-1$
                        if (MessageDialog.openQuestion(getShell(),
                              reuseDbColumnQTitle, reuseDbColumnQMsg))
                        {
                           try
                           {
                              String error = PSContentEditorDefinition
                                    .updateFieldWithBackendColumn(
                                          (PSBackEndColumn) loc, fld, mapping);
                              if (error != null)
                              {
                                 // This should not happen in general
                                 String dbColumnAttachErrTitle = 
                                    PSMessages.getString(
                                       "PSCEFieldsCommonComposite.error.reusedatabasecolumnfailed.title"); //$NON-NLS-1$
                                 String dbColumnAttachErrMsg = 
                                    PSMessages.getString(
                                          "PSCEFieldsCommonComposite.error.reusedatabasecolumnfailed.message"); //$NON-NLS-1$
                                 MessageDialog.openInformation(getShell(),
                                       dbColumnAttachErrTitle,
                                       dbColumnAttachErrMsg + error);
                              }
                           }
                           catch (Exception e)
                           {
                              String eTitle = PSMessages.getString(
                                    "PSCEFieldsCommonComposite.error.reusedbcolumnexception.title"); //$NON-NLS-1$
                              String eMsg = PSMessages.getString(
                                    "PSCEFieldsCommonComposite.error.reusedbcolumnexception.message"); //$NON-NLS-1$
                              PSWorkbenchPlugin.handleException(
                                    "Re-use database column", eTitle, eMsg, e); //$NON-NLS-1$
                           }
                        }
                     }
                     PSFieldDefinition fieldDef = new PSFieldDefinition(fld,
                           mapping);
                     rowData.setFieldDefinition(fieldDef);
                     PSContentEditorDefinition.setMaxLengthParam(rowData
                           .getControlRef(), fld.getDataFormat());
                     m_labelCellEditor.getControl().setEnabled(true);
                     m_ctrlCellEditor.getControl().setEnabled(true);
                     showPropertyComposites(true, false);
                     int index = m_table.getSelectionIndices()[0];
                     m_fieldProperties.loadData(rowData, index);
                     
                  }
               }
               else
               {
                  String oldname = rowData.getName();
                  if (!val.equals(oldname) && validateFieldName(val, oldname))
                  {
                     rowData.setName(val);
                     rowData.getDisplayMapping().setFieldRef(val);
                     // If it is a child field then
                     // Rename the child page
                     // Rename the displaymapper
                     // Update the designer object
                     if (rowData.isFieldSet())
                     {
                        PSDisplayMapper chMapper = rowData.getDisplayMapping()
                              .getDisplayMapper();
                        chMapper.setFieldSetRef(val);
                        ((PSContentTypeEditor) m_editor).renameChildPage(
                              oldname, val);
                        updateDesignerObject(m_editor.getDesignerObject(),
                              m_fieldsTable);
                     }
                  }
               }
               break;
            case 1 :
               rowData.setLabelText(val);
               if (!rowData.isFieldSet())
                  m_fieldProperties.updateMnemonic(val, rowData
                        .getDisplayMapping().getUISet());
               break;
            case 2 :
               rowData.setControlName(val);
               break;
         }
         mi_tableComp.refreshTable();
      }

      private PSSortableTable mi_tableComp;
   }

   /**
    * Validates the supplied string for field name. Checks whether the string is
    * valid name or not then checks whether a field exists with this name or
    * not.
    * 
    * @param newName The name to be validated.
    * @param oldName Old field name.
    * 
    * @return <code>true</code> if it is a valid name and no other field
    *         exists with this name otherwise <code>false</code>. If the new
    *         name and old name differ by just case returns <code>true</code>.
    */
   private boolean validateFieldName(String newName, String oldName)
   {
      String errMsg = "";
      String errTitle = PSMessages.getString(
            "PSCEFieldsCommonComposite.error.invalidfieldname.title"); //$NON-NLS-1$

      // Check for empty field. 
      // "newName" will be empty whenever user clicks in field, 
      // but does not add a new field name.
      if (StringUtils.isEmpty(newName))
      {
         return false;
      }

      // Validate the name (check for whitespace, invalid chars, etc)
      // (This test should be done before the test for 
      //  old-and-new-names-same-except-for-case (due to Hibernate issue
      //  with case of first two chars. See comment in "validateFieldName").
      String validationError = PSContentEditorDefinition
            .validateFieldName(newName);
      if (validationError != null)
      {
         MessageDialog.openError(getShell(), errTitle, validationError);
         return false;
      }

      // Do not need to validate further if new and old names 
      // are the same or if they differ only by case.
      if (newName.equalsIgnoreCase(oldName))
      {
         return true;
      }

      // Check to see if field name already exists.
      // Note special handling for creating shared fields:
      // (Field Set names cannot be the same as their field names)
      // 1) check the local Fields for a name clash
      // 2) check the Field Set name for a clash with the field name
      boolean fieldExists = false;
      if (m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
      {
         String [] stringArr = getAllFieldNames();
         if ( PSContentEditorDefinition.getItemIndex(stringArr, newName) != -1 ) 
         {
            fieldExists = true;
            errMsg = PSMessages.getString(
               "PSCEFieldsCommonComposite.error.duplicatefieldname.message"); //$NON-NLS-1$
         }
         else
         {
            if (m_fieldSetName.equalsIgnoreCase(newName))
            {
               fieldExists = true;
               errMsg = PSMessages.getString(
                  "PSCEFieldsCommonComposite.error.duplicatefieldandsetname.message"); //$NON-NLS-1$
            }
         }
      }
      else  // not in shared definition editor
      {
         if (((PSContentTypeEditor) m_editor).doesFieldExist(newName))
         {
            fieldExists = true; 
            errMsg = PSMessages.getString(
               "PSCEFieldsCommonComposite.error.duplicatefieldname.message"); //$NON-NLS-1$
            
         }
      }

      // Its an error if the field exists, flag error and return false.
      if (fieldExists)
      {
         MessageDialog.openError(getShell(), errTitle, errMsg);
         return false;
      }

      return true;
   }

   /**
    * Gets all the names of the fields. If the editor type is systemdef or
    * shareddef then loops through all the rows and returns the filenames. If
    * the editor is localdef then it has to return the field names from parent
    * and child editors. Calls parents method to get the field names.
    * 
    * @return String[] may be <code>empty </code>, but never null.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public String[] getAllFieldNames()
   {
      String[] fnames = new String[0];
      if (m_editorType == PSContentEditorDefinition.SYSTEMDEF_EDITOR
            || m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
      {
         List<PSFieldTableRowDataObject> rows = m_fieldsTable.getValues();
         fnames = new String[rows.size()];
         for (int i = 0; i < fnames.length; i++)
         {
            fnames[i] = rows.get(i).getName();
         }
      }
      else if (m_editorType == PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR
            || m_editorType == PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
      {
         fnames = ((PSContentTypeEditor) m_editor).getAllFieldNames();
      }
      return fnames;
   }

   /**
    * Sets the field set name
    */
   public void setFieldSetName(String fieldSetName)
   {
      if (StringUtils.isBlank(fieldSetName))
         throw new IllegalArgumentException("fieldSetName can not be null"); //$NON-NLS-1$
      m_fieldSetName = fieldSetName;
      String tabletitle = PSMessages.getString(
            "PSCEFieldsCommonComposite.childtable.name") + " " + fieldSetName; //$NON-NLS-1$ //$NON-NLS-2$
      m_fieldTableLabel.setText(tabletitle);
   }

   /**
    * Gets the PSSortableTable
    */
   public PSSortableTable getTableComp()
   {
      return m_fieldsTable;
   }

   /**
    * Convenient method to show or hide field and fieldset properties
    * composites.
    * 
    * @param fieldOption <code>true</code> shows the field properties
    *           composite otherwise hides.
    * @param fieldSetOption <code>true</code> shows the fieldset properties
    *           composite otherwise hides.
    */
   protected void showPropertyComposites(boolean fieldOption,
         boolean fieldSetOption)
   {
      m_fieldProperties.setVisible(fieldOption);
      m_fieldSetProperties.setVisible(fieldSetOption);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_fieldsTable)
      {
         if (m_table.getSelectionIndices().length != 1)
         {
            showPropertyComposites(false, false);
            return;
         }
         int index = m_table.getSelectionIndices()[0];
         IStructuredSelection sel = (IStructuredSelection) m_fieldsTable
               .getSelection();
         PSFieldTableRowDataObject rowData = (PSFieldTableRowDataObject) sel
               .getFirstElement();
         m_nameCellEditor.getControl().setEnabled(true);
         m_labelCellEditor.getControl().setEnabled(true);
         m_ctrlCellEditor.getControl().setEnabled(true);

         if (rowData.isEmpty())
         {
            m_nameCellEditor.getControl().setEnabled(true);
            m_labelCellEditor.getControl().setEnabled(false);
            m_ctrlCellEditor.getControl().setEnabled(false);
            showPropertyComposites(false, false);
            return;
         }
         // If it is a static field disable properties and show the static field
         // label
         if(isStaticField(rowData))
         {
            m_nameCellEditor.getControl().setEnabled(false);
            m_labelCellEditor.getControl().setEnabled(false);
            m_ctrlCellEditor.getControl().setEnabled(false);
            showPropertyComposites(false, false);
            m_staticFieldLabel.setVisible(true);
            return;
         }
         else
         {
            m_staticFieldLabel.setVisible(false);
         }
         
         // Check whether it is a field or field set
         if (rowData.isFieldSet())
         {
            updateControlCellEditor(
                  PSContentEditorDefinition.TABLE_DIM_CONTROLS, rowData
                        .getControlName());
            showPropertyComposites(false, true);
            m_fieldSetProperties.loadData(rowData, index);
         }
         else
         {
            int options = 0;
            /*
             * Update the controls combo box If the field has a data locator
             * then set the controls based on the controls dimension as the
             * field can not be changed from single dimension to array dimension
             * and vice versa. This makes managing the field much easier.
             */
            if (rowData.getField().getLocator() != null)
            {
               try
               {
                  PSControlMeta meta = PSContentEditorDefinition
                        .getControl(rowData.getControlName());
                  if (meta.getDimension().equals(PSControlMeta.ARRAY_DIMENSION))
                     options = PSContentEditorDefinition.ARRAY_DIM_CONTROLS;
                  else
                     options = PSContentEditorDefinition.SINGLE_DIM_CONTROLS;
               }
               catch (PSModelException e1)
               {
                  // This should not happen as we fix the controls before
                  // opening the editor. Incase happens.
                  PSWorkbenchPlugin
                        .handleException(
                              "Controls Catalog", //$NON-NLS-1$
                              PSMessages.getString(
                                    "PSCEFieldsCommonComposite.error.catalogcontrols.title"), //$NON-NLS-1$
                              PSMessages.getString(
                                    "PSCEFieldsCommonComposite.error.catalogcontrols.message"), //$NON-NLS-1$
                              e1);
                  return;
               }
            }
            else
            {
               options = PSContentEditorDefinition.SINGLE_DIM_CONTROLS;
               // If it is parent editor or shared editor with only one row add
               // array diemsnion controls also
               if (m_editorType == 
                  PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR
                     || (m_editorType == 
                        PSContentEditorDefinition.SHAREDDEF_EDITOR 
                           && m_fieldsTable.getValues().size() <= 1))
               {
                  options = options
                        | PSContentEditorDefinition.ARRAY_DIM_CONTROLS;
               }
            }

            updateControlCellEditor(options, rowData.getControlName());
            m_fieldProperties.setVisible(true);
            m_fieldSetProperties.setVisible(false);
            m_fieldProperties.loadData(rowData, index);
            // if the filed is shared or system then make name non editable
            int type = rowData.getField().getType();
            if (type == PSField.TYPE_SYSTEM
                  || (m_editorType == 
                     PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR 
                     && type == PSField.TYPE_SHARED))
            {
               m_nameCellEditor.getControl().setEnabled(false);
            }
            else
            {
               m_nameCellEditor.getControl().setEnabled(true);
            }
         }
      }
      else if (e.getSource() == m_ctrlCellEditor.getButton())
      {
         int index = m_table.getSelectionIndices()[0];
         IStructuredSelection sel = (IStructuredSelection) m_fieldsTable
               .getSelection();
         final PSFieldTableRowDataObject rowData =
               (PSFieldTableRowDataObject) sel.getFirstElement();
         List<PSFieldTableRowDataObject> rows = m_fieldsTable.getValues();
         PSFieldDefinition fieldDefCopy = PSFieldDefinition.deepCopy(rowData
               .getFieldDefinition());
         PSFieldTableRowDataObject rowDataCopy = new PSFieldTableRowDataObject(
               fieldDefCopy);
         PSControlPropertiesDialog dlg = new PSControlPropertiesDialog(
               getShell(), rowDataCopy, m_editorType);
         int result = dlg.open();
         if (result == Dialog.OK)
         {
            rows.set(index, rowDataCopy);
            m_fieldsTable.setValues(rows);
            m_fieldsTable.refreshTable();
         }
      }
      else if (e.getSource() == m_ctrlCellEditor.getCombo())
      {
         CCombo ctrlCombo = m_ctrlCellEditor.getCombo();
         IStructuredSelection sel = (IStructuredSelection) m_fieldsTable
               .getSelection();
         PSFieldTableRowDataObject rowData = (PSFieldTableRowDataObject) sel
               .getFirstElement();
         PSControlRef oldCtrlRef = rowData.getControlRef();
         String oldCtrl = rowData.getControlName();
         String[] items = ctrlCombo.getItems();
         String newCtrl = items[ctrlCombo.getSelectionIndex()];
         if (!oldCtrl.equals(newCtrl))
         {
            // When user changes the control we need to change the datatype and
            // format to defaults for all local fields, shared fields in shared
            // defs.
            // If the field has a data locator then we need
            // to ask the user about the possible dataloss and revert back to
            // old control in case of cancel.
            PSField fld = rowData.getField();
            if (fld != null && (fld.isLocalField() || 
                  (fld.isSharedField() && 
                        m_editorType == 
                           PSContentEditorDefinition.SHAREDDEF_EDITOR)))
            {
               List<String> dataList = PSContentEditorDefinition
               .getDefaultDataTypeAndFormat(newCtrl);
               String newType = dataList.get(0);
               String newFormat = dataList.get(1);
               if(fld.getLocator() != null)
               {
                  if (!fld.getDataType().equals(newType))
                  {
                     if(!MessageDialog.openConfirm(getShell(),PSMessages.getString(
                     "PSFieldDataTypeAndFormatComposite.error.possibledataloss.title"), 
                     PSMessages.getString(
                           "PSFieldDataTypeAndFormatComposite.error.possibledataloss.datatype.message")))
                     {
                        ctrlCombo.select(PSContentEditorDefinition
                              .getItemIndex(items, oldCtrl));
                        return;
                     }
                     else
                     {
                        fld.setDataType(newType);
                        fld.setDataFormat(newFormat);
                        setColumnModificationActions(fld,
                              IPSCETableColumnActions.COLUMN_ACTION_ALTER);
                     }
                  }
                  else if (fld.getDataType().equals("text")
                        && newType.equals("text"))
                  {
                     // If old and new types are text.
                     // change the format only if the old format is less than
                     // new default format.
                     String oldFormat = fld.getDataFormat();
                     boolean change = false;
                     if (oldFormat == null)
                        change = true;
                     else if(oldFormat.equals("max"))
                        change = false;
                     else if(newFormat.equals("max"))
                        change = true;
                     else if(!StringUtils.isNumeric(oldFormat))
                        change = true;
                     else if(Integer.parseInt(oldFormat) < Integer
                                 .parseInt(newFormat))
                        change = true;
                     if(change)
                     {
                        fld.setDataFormat(newFormat);
                        setColumnModificationActions(fld,
                              IPSCETableColumnActions.COLUMN_ACTION_ALTER);
                     }
                  }

               }
               else
               {
                  fld.setDataType(newType);
                  fld.setDataFormat(newFormat);
               }
            }
            rowData.setControlName(newCtrl);
            PSContentEditorDefinition.setDefaultInlineLinkProperties(rowData
                  .getField(), newCtrl);
            PSContentEditorDefinition.preserveControlProperties(oldCtrlRef,
                  rowData.getControlRef());
            PSContentEditorDefinition.setMaxLengthParam(rowData
                  .getControlRef(), fld.getDataFormat());
            resetChoices(rowData);
            setControlDependencies(rowData.getFieldDefinition());
            m_fieldProperties.loadData(rowData,m_table.getSelectionIndex());
            m_fieldsTable.refreshTable();
         }
      }
      m_editor
            .updateDesignerObject(m_editor.getDesignerObject(), m_fieldsTable);
      m_editor.setDirty();
   }

   /**
    * Convenient method to check whether given field refresented by a table row
    * is static or not. If the field is a local field and has a locator other
    * than backend column then it is treated as static field.
    * 
    * @param rowData may be <code>null</code> or empty.
    * @return <code>true</code> if field is static otherwise
    *         <code>false</code>.
    */
   private boolean isStaticField(PSFieldTableRowDataObject rowData)
   {
      if (rowData == null || rowData.isEmpty())
         return false;
      PSField fd = rowData.getField();
      if (fd != null && fd.isLocalField() && fd.getLocator() != null
            && !(fd.getLocator() instanceof PSBackEndColumn))
         return true;

      return false;
   }
   /**
    * Resets the control choices based on the control. This is intended to be
    * called when the control changes. If the control is Single Dimension then
    * sets the choices to null. If it is of Array Dimension sets it to the first
    * available keyword lookup.
    * 
    * @param rowData The data object representing the fields row. The choices
    *           are updated directly on this object.
    */
   private void resetChoices(PSFieldTableRowDataObject rowData)
   {
      String ctrlName = rowData.getControlName();
      try
      {
         PSControlMeta meta = PSContentEditorDefinition.getControl(ctrlName);
         if (meta.getDimension().equals(PSControlMeta.ARRAY_DIMENSION))
         {
            List<IPSReference> kwList = PSCoreUtils.catalog(
                  PSObjectTypes.KEYWORD, false);
            PSChoices ch = new PSChoices(((int) kwList.get(0).getId()
                  .longValue()));
            rowData.getDisplayMapping().getUISet().setChoices(ch);
         }
         else
         {
            rowData.getDisplayMapping().getUISet().setChoices(null);
         }
      }
      catch (PSModelException e)
      {
         // This should not happen as we fix the controls before
         // opening the editor. Incase happens.
         PSWorkbenchPlugin
               .handleException(
                     "Catalog Error", //$NON-NLS-1$
                     PSMessages.getString(
                           "PSCEFieldsCommonComposite.error.catalog.title"), //$NON-NLS-1$
                     PSMessages.getString(
                           "PSCEFieldsCommonComposite.error.catalog.message"), 
                           e); //$NON-NLS-1$
         return;
      }
   }

   /**
    * Sets the control dependencies for the supplied PSFieldDefinition object.
    * If the field definition is null or field set then does nothing.
    * 
    * @param fieldDefinition Object of PSFieldDefinition for which the
    */
   protected void setControlDependencies(PSFieldDefinition fieldDefinition)
   {
      if (fieldDefinition == null || fieldDefinition.isFieldSet())
         return;
      if (m_editorType == PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR
            || m_editorType == PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
      {
         PSControlRef ref = fieldDefinition.getMapping().getUISet()
               .getControl();
         if (ref != null)
         {
            PSControlMeta meta = null;
            try
            {
               meta = PSContentEditorDefinition.getControl(ref.getName());
               if (meta == null)
               {
                  // This should not happen as we fix all the controls while
                  // opening the editor.
                  Object[] args =
                  {ref.getName()};
                  String errMsg = MessageFormat
                        .format(PSMessages.getString(
                              "PSCEFieldsCommonComposite.error.missingcontrol.message"), //$NON-NLS-1$
                              args);
                  MessageDialog
                        .openError(
                              getShell(),
                              PSMessages.getString(
                                    "PSCEFieldsCommonComposite.error.missingcontrol.title"), errMsg); //$NON-NLS-1$
                  return;
               }
               if (meta.getDependencies().isEmpty())
               {
                  fieldDefinition.setCtrlDependencies(null);
                  return;
               }
               if (ref.getId() == 0)
                  ref.setId(PSContentEditorDefinition.getUniqueId());
               List<PSDependency> deps = m_ctrlDepMap.getControlDependencies(
                     fieldDefinition.getMapping(), meta);
               if (!deps.isEmpty())
               {
                  for (PSDependency dep : deps)
                  {
                     dep.setId(PSContentEditorDefinition.getUniqueId());
                  }
                  fieldDefinition.setCtrlDependencies(deps);
               }
               else
               {
                  fieldDefinition.setCtrlDependencies(null);
               }
            }
            catch (PSModelException e)
            {
               // This should not happen as we fix the controls before
               // opening the editor. Incase happens.
               PSWorkbenchPlugin
                     .handleException(
                           "Controls Catalog", //$NON-NLS-1$
                           PSMessages.getString(
                                 "PSCEFieldsCommonComposite.error.controlcatalog.title"), //$NON-NLS-1$
                           PSMessages.getString(
                                 "PSCEFieldsCommonComposite.error.controlcatalog.message"), //$NON-NLS-1$
                           e);
               return;
            }
         }
      }
   }

   /**
    * Call this method only when dealing with the Shared Field editor.
    * 
    * @param fieldSetName Name of the fieldset need to be created.
    */
   public void createSharedFieldSet(String fieldSetName)
   {
      if (!(m_editor instanceof PSSharedDefEditor))
      {
         MessageDialog
               .openError(
                     getShell(),
                     PSMessages.getString(
                           "PSCEFieldsCommonComposite.warn.title.invalidoperation.sharedfieldset"), //$NON-NLS-1$
                     PSMessages.getString(
                           "PSCEFieldsCommonComposite.warn.message.invalidoperation.sharedfieldset")); //$NON-NLS-1$
      }
      ((PSSharedDefEditor) m_editor).addNewFieldSetPage(fieldSetName);
   }

   public void widgetDefaultSelected(
         @SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object,
    *      java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      if (control == m_fieldsTable)
      {
         PSDisplayMapper parentMapper = null;
         PSFieldSet parentFieldSet = null;
         PSFieldSet chFieldSet = null;
         PSDisplayMapper chMapper = null;
         if (m_editorType == PSContentEditorDefinition.SYSTEMDEF_EDITOR)
         {
            PSContentEditorSystemDef sysDef = 
               (PSContentEditorSystemDef) designObject;
            parentMapper = sysDef.getUIDefinition().getDisplayMapper();
            parentFieldSet = sysDef.getFieldSet();
         }
         else if (m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
         {
            PSContentEditorSharedDef shDef = 
               (PSContentEditorSharedDef) designObject;
            Iterator iter = shDef.getFieldGroups();
            while (iter.hasNext())
            {
               PSSharedFieldGroup group = (PSSharedFieldGroup) iter.next();
               if (group.getName().equals(m_fieldSetName))
               {
                  parentFieldSet = group.getFieldSet();
                  if (parentFieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
                     m_simpleChildWarningShown = true;
                  parentFieldSet.setType(
                        PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
                  parentMapper = group.getUIDefinition().getDisplayMapper();
                  
                  /* check for the special case of the fieldset containing a
                   * single, multi-valued field. In this case, the fs must have
                   * its type reset to simpleChild.
                   */
                  try
                  {
                     if (parentMapper.size() == 1)
                     {
                        PSDisplayMapping fsMapping = 
                           (PSDisplayMapping) parentMapper.get(0);
                        PSControlRef controlRef = fsMapping.getUISet()
                              .getControl();
                        if (controlRef != null)
                        {
                           PSControlMeta ctrl = PSContentEditorDefinition
                                 .getControl(controlRef.getName());
                           if (ctrl != null && ctrl.getDimension()
                              .equals(PSControlMeta.ARRAY_DIMENSION))
                           {
                              parentFieldSet
                                    .setType(PSFieldSet.TYPE_SIMPLE_CHILD);
                              if (!m_simpleChildWarningShown)
                              {
                                 String title = PSMessages.getString(
                                       "PSCEFieldsCommonComposite.simpleChildInfo.title");
                                 String msg = PSMessages.getString(
                                       "PSCEFieldsCommonComposite.simpleChildInfo.msg");
                                 PSUiUtils.displayWarningMessage(title, msg);
                              }
                           }
                        }
                     }
                  }
                  catch (PSModelException e)
                  {
                     //this should never happen
                     throw new RuntimeException(e.getLocalizedMessage());
                  }
                  break;
               }
            }
         }
         else
         {
            PSItemDefinition itemDef = (PSItemDefinition) designObject;
            PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
                  .getContentEditor().getPipe();
            PSContentEditorMapper ceMapper = pipe.getMapper();
            PSUIDefinition uiDef = ceMapper.getUIDefinition();
            parentMapper = uiDef.getDisplayMapper();
            parentFieldSet = ceMapper.getFieldSet();
         }
         // If it is a child field editor we need to work on child field set and
         // child mapper
         if (m_editorType == PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
         {
            boolean found = false;
            Iterator mappings = parentMapper.iterator();
            while (mappings.hasNext())
            {
               PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
               chMapper = mapping.getDisplayMapper();
               if (chMapper != null
                     && chMapper.getFieldSetRef().equals(m_fieldSetName))
               {
                  chFieldSet = (PSFieldSet) parentFieldSet.get(m_fieldSetName);
                  if (chFieldSet != null
                        && chFieldSet.getType() == 
                           PSFieldSet.TYPE_COMPLEX_CHILD)
                  {
                     found = true;
                     break;
                  }
               }
            }
            if (!found)
            {
               return;
            }

         }
         if (m_editorType == PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR
               || m_editorType == PSContentEditorDefinition.SYSTEMDEF_EDITOR
               || m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
         {
            updateData(parentFieldSet, parentMapper);
         }
         else if (m_editorType == 
            PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
         {
            updateData(chFieldSet, chMapper);
         }
         //Update the icon info if it is local parent editor
         if (m_editorType == PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR)
            ((PSContentTypeEditor) m_editor).getPropsTab()
                  .setIconControlValues();
      }
   }

   /**
    * Saves model data to the passed in field set and mapper. Clears and adds
    * all fields/fieldsets and mappings in this model data to the fieldset and
    * mapper respectively.
    * 
    * @param fieldset the set to which all fields should be set to, may not be
    *           <code>null</code>.
    * @param mapper the mapper to which all mappings should be set to, may not
    *           be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * 
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   private void updateData(PSFieldSet fieldset, PSDisplayMapper mapper)
   {
      if (fieldset == null)
         throw new IllegalArgumentException("fieldset can not be null"); //$NON-NLS-1$

      if (mapper == null)
         throw new IllegalArgumentException("mapper can not be null"); //$NON-NLS-1$

      String errTitle = PSMessages
            .getString("PSCEFieldsCommonComposite.error.updatedata.title"); //$NON-NLS-1$
      String errMsg = PSMessages
            .getString("PSCEFieldsCommonComposite.error.updatedata.message"); //$NON-NLS-1$
      /*
       * Create new field set instead of clearing passed one and save to that,
       * so that we will have access to simple and multi property simple child's
       * field sets.
       */
      PSFieldSet saveSet = new PSFieldSet(fieldset.getName());
      // clear all existing mappings
      mapper.clear();
      List<PSFieldTableRowDataObject> rows = m_fieldsTable.getValues();
      for (int i = 0; i < rows.size(); i++)
      {
         PSFieldTableRowDataObject data = rows.get(i);
         if (data == null || data.isEmpty())
            continue;
         
         if (data.isFieldSet())
         {
            saveSet.add(data.getFieldSet());
            mapper.add(data.getDisplayMapping());
            continue;
         }
         
         PSField field = data.getField();
         String name = field.getSubmitName();
         PSUISet uiSet = data.getDisplayMapping().getUISet();
         PSDisplayMapping mapping = new PSDisplayMapping(name, uiSet);

         String controlType = null;
         PSControlRef control = uiSet.getControl();
         if (control != null)
         {
            try
            {
               controlType = PSContentEditorDefinition
                     .getControlType(control.getName());
            }
            catch (PSModelException e)
            {
               PSWorkbenchPlugin.handleException(
                     "CE Update Data", errTitle, errMsg, e); //$NON-NLS-1$
            }
         }
         boolean isSimpleChild = controlType != null
            && controlType.equals(PSControlMeta.ARRAY_DIMENSION);
         
         if (m_editorType == PSContentEditorDefinition.SYSTEMDEF_EDITOR
               || m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
         {
            saveSet.add(field);
            if (isSimpleChild)
               modifyMapping(fieldset, name, mapping);

            mapper.add(mapping);
         }
         else
         {
            /*
             * Steps to identify field to fieldset relationship. Case 1.
             * local field which does not have a backend locator (new field)
             * (We don't support new multiproperty simple child fields) a) If
             * the control for this field mapping is a simple child field
             * control (control of type array dimension), then we create
             * simple child fieldset and add that to the provided fieldset.
             * b) If the control for this field mapping is a control of
             * single dimension, then we add that field to the provided
             * fieldset. Case 2. local field which has a backend locator
             * (existing field) a)These fields can be fields of
             * parent/complex child, simple child or multiproperty simple
             * child. So we have to identify the fieldset to which this field
             * belongs to. Identifies the fieldset by the fieldset id and
             * update that fieldset with modified field. Case 3. System field
             * a) adds to the provided fieldset. Case 4. Shared field a) If
             * the field belongs to shared simple or multiproperty simple
             * child fieldset, we have to add that shared fieldset with only
             * included fields. b) If the field belongs to complex child,
             * then it should add to the provided fieldset which is a shared
             * complex child fieldset. Steps to add mappings. Case 1. If the
             * mapping refers to a simple child field, then we have to add a
             * fake display mapper to that mapping and that mapping should
             * refer to name of the simple child fieldset to which this field
             * belongs to. The resulting mapping should be added to this
             * mapper. Case 2. All other cases, the mapping refers to the
             * field.
             */

            // If the field is a simple child field, we have to add a fake
            // display mapper and add a simple child fieldset to this
            // fieldset.

            PSFieldSet childSet = null;
            if (field.isLocalField())
            {
               // simple child field
               if (isSimpleChild)
               {
                  // need to name the fieldset different from field
                  childSet = new PSUIFieldSet(name + SIMPLE_FIELDSET_SUFFIX);
                  childSet.setType(PSFieldSet.TYPE_SIMPLE_CHILD);
                  childSet.add(field);
                  saveSet.add(childSet);
               }
               // field in this fieldset
               else
               {
                  saveSet.add(field);
               }
            }
            else if (field.isSharedField())
            {
               try
               {
                  
                  String groupName = (String)field.getUserProperty(
                        PSField.SHARED_GROUP_FIELDSET_USER_PROP);
                  assert(StringUtils.isNotBlank(groupName));
                  // If any of the fields this fieldset are already added
                  // then
                  // we add this field to that set, otherwise we get the
                  // fieldset
                  // from shared def and add this field alone and set to the
                  // fieldset.
                  if (saveSet.contains(groupName))
                  {
                     childSet = (PSFieldSet) saveSet.get(groupName);
                  }
                  else
                  {
                     childSet = new PSFieldSet(PSContentEditorDefinition
                           .getSharedFieldSet(groupName));
                     childSet.removeAll();
                  }
                  childSet.add(field);
                  saveSet.add(childSet);
               }
               catch (Exception e)
               {
                  PSWorkbenchPlugin.handleException(
                        "CE Update Data", errTitle, errMsg, e); //$NON-NLS-1$
               }
            }
            else
            {
               saveSet.add(field);
            }

            // Add a fake display mapper if a simple child
            if (isSimpleChild)
            {
               modifyMapping(childSet, name, mapping);
            }
            // Now add the mapping to the mapper
            mapper.add(mapping);
         }
      }

      
      // true effectively means get all fields that are not shown in the UI
      Iterator fields = fieldset.getAll(true);
      while (fields.hasNext())
      {
         Object test = fields.next();
         if (test instanceof PSField)
         {
            PSField field = (PSField) test;
            if (!saveSet.contains(field.getSubmitName()))
               saveSet.add(field);
         }
      }

      // clear all fields in original field set and copy from saved set
      fieldset.removeAll();
      Iterator fieldIter = saveSet.getEveryField();
      while (fieldIter.hasNext())
      {
         Object o = fieldIter.next();
         if (o instanceof PSField)
            fieldset.add((PSField) o);
         else
            fieldset.add((PSFieldSet) o);
      }
   }

   /**
    * Builds the correct structure of mappings and mappers in the supplied
    * mapping for a simple child.
    * 
    * @param childFieldSet Assumed to be a child of TYPE_SIMPLE_CHILD. If it is
    * not, an exception is thrown.
    * 
    * @param childFieldName The name of the field contained by the supplied
    * fieldset. Assumed not blank.
    * 
    * @param fsMapping The mapping to update for a simple child. Assumed not
    * <code>null</code>.
    * 
    * @throws IllegalStateException If the supplied field set is
    * <code>null</code> or its type is not simple child.
    */
   private void modifyMapping(PSFieldSet childFieldSet, String childFieldName,
         PSDisplayMapping fsMapping)
   {
      if (childFieldSet == null
            || childFieldSet.getType() != PSFieldSet.TYPE_SIMPLE_CHILD)
      {
         throw new IllegalStateException(
               "Found simple child field control for a field <" //$NON-NLS-1$
                     + childFieldName + "> that is not a simple child field"); //$NON-NLS-1$
      }

      PSDisplayMapper childMapper = new PSDisplayMapper(childFieldSet
            .getName());
      fsMapping.setFieldRef(childFieldSet.getName());
      fsMapping.setDisplayMapper(childMapper);
      PSDisplayMapping childMapping = new PSDisplayMapping(childFieldName,
            new PSUISet());
      childMapper.add(childMapping);
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      // Get the field definition objects and keep a list
      List<PSFieldDefinition> fdefs = new ArrayList<PSFieldDefinition>();
      if (m_editorType == PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR)
      {
         PSItemDefinition itemDef = (PSItemDefinition) designObject;
         PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
               .getContentEditor().getPipe();
         m_ctrlDepMap = pipe.getControlDependencyMap();
         PSContentEditorMapper mapper = pipe.getMapper();
         PSUIDefinition def = mapper.getUIDefinition();
         PSDisplayMapper dmapper = def.getDisplayMapper();
         PSFieldSet fieldSet = mapper.getFieldSet();
         setDBTableName(fieldSet);
         fdefs = PSContentEditorDefinition.getFieldDefinitions(dmapper,
               fieldSet);
      }
      else if (m_editorType == PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
      {
         PSItemDefinition itemDef = (PSItemDefinition) designObject;
         PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
               .getContentEditor().getPipe();
         m_ctrlDepMap = pipe.getControlDependencyMap();
         fdefs = PSContentEditorDefinition.getChildFieldDefinitions(itemDef,
               m_fieldSetName);
         PSContentEditorMapper mapper = pipe.getMapper();
         PSFieldSet fieldSet = mapper.getFieldSet();
         PSFieldSet chSet = (PSFieldSet) fieldSet.get(m_fieldSetName);
         setDBTableName(chSet);
      }
      else if (m_editorType == PSContentEditorDefinition.SYSTEMDEF_EDITOR)
      {
         PSContentEditorSystemDef sysDef = 
            (PSContentEditorSystemDef) designObject;
         PSDisplayMapper dmapper = sysDef.getUIDefinition().getDisplayMapper();
         PSFieldSet fieldSet = sysDef.getFieldSet();
         fdefs = PSContentEditorDefinition.getFieldDefinitions(dmapper,
               fieldSet);
         setDBTableName(fieldSet);
      }
      else if (m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
      {
         PSContentEditorSharedDef shDef = 
            (PSContentEditorSharedDef) designObject;
         Iterator iter = shDef.getFieldGroups();
         while (iter.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup) iter.next();
            if (group.getName().equals(m_fieldSetName))
            {
               fdefs = PSContentEditorDefinition
                     .getSharedFieldDefinitions(group);
               setDBTableName(group.getFieldSet());
               break;
            }
         }
      }
      m_fieldData.clear();

      for (int i = 0; i < fdefs.size(); i++)
      {
         PSFieldDefinition fdef = fdefs.get(i);
         setControlDependencies(fdef);
         m_fieldData.add(new PSFieldTableRowDataObject(fdef));
      }
      m_fieldsTable.setValues(m_fieldData);
   }

   /**
    * Sets m_dbTableName the database table name represented by the field set.
    * Walks through the fields to find the table name. If not found sets it to
    * <code>null</code>.
    * 
    * @param set The PSFieldSet object from which the table name is picked to
    *           set. If the set is <code>null</code> the table name is set to
    *           <code>null</code>.
    */
   public void setDBTableName(PSFieldSet set)
   {
      if (set == null)
         m_dbTableName = null;
      Iterator fields = set.getAll();

      String tableAlias = null;
      while (fields.hasNext())
      {
         Object field = fields.next();
         if (field instanceof PSFieldSet)
            continue;
         else
         {
            PSField osfield = (PSField) field;
            if ((m_editorType == 
               PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR || 
               m_editorType == PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR)
                  && osfield.isLocalField())
            {
               /*
                * Get table alias for this field set from existing fields(fields
                * which have backend mapping.
                */
               if (osfield.getLocator() instanceof PSBackEndColumn)
               {
                  tableAlias = ((PSBackEndColumn) osfield.getLocator())
                        .getTable().getAlias();
                  break;
               }
            }
            else if (m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR
                  && osfield.isSharedField())
            {
               /*
                * Get table alias for this field set from existing fields(fields
                * which have backend mapping.
                */
               if (osfield.getLocator() instanceof PSBackEndColumn)
               {
                  tableAlias = ((PSBackEndColumn) osfield.getLocator())
                        .getTable().getAlias();
                  break;
               }
            }
         }
      }
      m_dbTableName = tableAlias;
   }

   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public String[] getSingleDimensionFieldNames()
   {
      List<String> fields = new ArrayList<String>();
      List<PSFieldTableRowDataObject> rows = m_fieldsTable.getValues();
      for (PSFieldTableRowDataObject obj : rows)
      {
         if (!(obj.isEmpty() && obj.isFieldSet()))
         {
            PSControlMeta meta;
            try
            {
               meta = PSContentEditorDefinition
                     .getControl(obj.getControlName());
               if (meta.getDimension().equals(PSControlMeta.SINGLE_DIMENSION))
                  fields.add(obj.getName());
            }
            catch (PSModelException e)
            {
               String errTitle = PSMessages.getString(
                     "PSCEFieldsCommonComposite.error.controlinfo.title"); //$NON-NLS-1$
               String errMsg = PSMessages.getString(
                     "PSCEFieldsCommonComposite.error.controlinfo.message"); //$NON-NLS-1$
               PSWorkbenchPlugin.handleException(
                     "CE getControl Data", errTitle, errMsg, e); //$NON-NLS-1$
            }
         }
      }
      return fields.toArray(new String[fields.size()]);
   }

   /**
    * Sets the column modification action, if the field has a datalocator.
    * 
    * @param field Object of PSField, must not be <code>null</code>.
    * @param action int indicating the value must be one of the supported action
    *           from IPSCETableColumnActions.
    */
   public void setColumnModificationActions(PSField field, int action)
   {
      if (field == null)
      {
         throw new IllegalArgumentException("field must not be null"); //$NON-NLS-1$
      }
      // Simply return if the filed does not have a datalocator.
      if (field.getLocator() == null)
         return;
      if (!(action == IPSCETableColumnActions.COLUMN_ACTION_ALTER || action 
            == IPSCETableColumnActions.COLUMN_ACTION_DELETE))
      {
         throw new IllegalArgumentException(
               "unsupported database column action is supplied."); //$NON-NLS-1$
      }
      if (m_editorType == PSContentEditorDefinition.SYSTEMDEF_EDITOR)
      {
         // We do not change the system def columns through UI.
      }
      else if (m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR)
      {
         PSUiContentEditorSharedDef sharedDef = 
            (PSUiContentEditorSharedDef) m_editor
               .getDesignerObject();
         sharedDef.addColumnAction(field, action);
      }
      else
      {
         PSUiItemDefinition itemDef = (PSUiItemDefinition) m_editor
               .getDesignerObject();
         itemDef.addColumnAction(field, action);
      }
   }

   /**
    * Determines if all local fields with valid locators have been selected.
    * 
    * @return <code>true</code> if all local fields with valid locators have
    * been selected, <code>false</code> otherwise.
    */
   public boolean allLocalFieldsWithLocatorSelected()
   {
      TableItem[] selitems = m_table.getSelection();
      
      // gather selected local field names with valid locators
      Set<String> selLocalFieldNames = getLocalFieldsWithLocator(selitems);
   
      // gather all local field names with valid locators
      Set<String> tableLocalFieldNames = getLocalFieldsWithLocator(
            m_table.getItems());
      
      int selFieldsSize = selLocalFieldNames.size();
      if (selFieldsSize > 0 && selFieldsSize == tableLocalFieldNames.size())
         return true;
      
      return false;
   }
   
   /**
    * If the backend table represented by the fields table has an unused column
    * that matches the supplied name then creates an object of PSBackEndColumn
    * corresponding to that column and returns it.
    * 
    * @return IPSBackEndMapping corresponding to the unused column or
    *         <code>null</code> if database table name is blank or no unused
    *         column exists with the supplied name.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   private IPSBackEndMapping getMatchingLocator(String name)
   {
      if (StringUtils.isBlank(name) || StringUtils.isBlank(m_dbTableName))
         return null;
      IPSBackEndMapping loc = null;
      // If the column name exists in
      // Get the column names for the table
      PSSqlCataloger cat = new PSSqlCataloger(null, m_dbTableName);
      PSDesignerConnection conn = PSCoreFactory.getInstance()
            .getDesignerConnection();
      cat.setConnectionInfo(conn);
      try
      {
         Collection<String> cols = cat.getCatalog();
         Map<String, IPSBackEndMapping> usedCols = 
            new HashMap<String, IPSBackEndMapping>();
         List<PSFieldTableRowDataObject> rows = m_fieldsTable.getValues();
         for (PSFieldTableRowDataObject row : rows)
         {
            if (row.isFieldSet())
               continue;
            PSField fld = row.getField();
            if (fld.isLocalField() && fld.getLocator() != null
                  && fld.getLocator() instanceof PSBackEndColumn)
            {
               String colName = ((PSBackEndColumn) fld.getLocator())
                     .getColumn();
               if (!StringUtils.isBlank(colName))
                  usedCols.put(colName, fld.getLocator());
            }

         }
         cols.removeAll(usedCols.keySet());
         for (String col : cols)
         {
            if (col.equalsIgnoreCase(name))
            {
               loc = new PSBackEndColumn(new PSBackEndTable(m_dbTableName), col);
               break;
            }
         }
      }
      catch (Exception e)
      {
         String title = PSMessages.getString(
               "PSCEFieldsCommonComposite.error.catalogcolumnsfailed.title"); //$NON-NLS-1$
         String msg = PSMessages.getString(
               "PSCEFieldsCommonComposite.error.catalogcolumnsfailed.message"); //$NON-NLS-1$
         PSWorkbenchPlugin.handleException(
               "Table column catalogger", title, msg, e); //$NON-NLS-1$
      }
      return loc;
   }

   /**
    * Convenient method to update the control name list
    */
   private void updateControlCellEditor(int options, String ctrlName)
   {
      try
      {
         String[] items = PSContentEditorDefinition.getControlNames(options);
         m_ctrlCellEditor.setItems(items);
         m_ctrlCellEditor.setValue(ctrlName);
      }
      catch (PSModelException e)
      {
         String errTitle = "Failed to get the control information"; //$NON-NLS-1$
         String errMsg = "The following error occured catalogging the controls"; //$NON-NLS-1$
         PSWorkbenchPlugin.handleException(
               "CE getControl Data", errTitle, errMsg, e); //$NON-NLS-1$
      }
   }

   /**
    * Retrieves local fields from the supplied table items which have valid
    * locators.
    * 
    * @param items Array of table items whose data is assumed to consist of
    * {@link PSFieldTableRowDataObject} objects.
    * 
    * @return Set of names corresponding to local fields with valid locators,  
    * never <code>null</code>.
    */
   private Set<String> getLocalFieldsWithLocator(TableItem[] items)
   {
      Set<String> fields = new HashSet<String>();
      
      for (TableItem item : items)
      {
         PSFieldTableRowDataObject row = (PSFieldTableRowDataObject) item
               .getData();
         if (row == null || row.isEmpty() || row.isFieldSet())
            continue;
         
         PSField field = row.getField();
         if (field == null || !field.isLocalField())
            continue;
          
         if (field.getLocator() != null)
            fields.add(field.getSubmitName());
      }
      
      return fields;
   }
   
   protected class CeSortableTable extends PSSortableTable
   {

      public CeSortableTable(Composite parent,
            ITableLabelProvider labelProvider,
            IPSNewRowObjectProvider rowObjectProvider, int style, int options)
      {
         super(parent, labelProvider, rowObjectProvider, style, options);
      }

      /**
       * Overwrite the pre delete method to show warning messages while
       * deleting the fields.
       */
      @Override
      protected boolean preDelete()
      {
         if (allLocalFieldsWithLocatorSelected())   
         {
            // don't allow deletion of all local fields with valid locators,
            // otherwise, a new table will be created for the content type on
            // save (RX-14754)
            String fieldDelTitle = PSMessages
                  .getString("PSCEFieldsCommonComposite.warn.title.localfielddeletion"); //$NON-NLS-1$
            String fieldDelMsg = PSMessages
                  .getString("PSCEFieldsCommonComposite.warn.message.localfielddeletion"); //$NON-NLS-1$
            MessageBox msgBox = new MessageBox(getShell(), SWT.ICON_WARNING
                  | SWT.OK);
            msgBox.setMessage(fieldDelMsg);
            msgBox.setText(fieldDelTitle);
            msgBox.open();
            
            return false;
         }
         
         TableItem[] selitems = m_table.getSelection();
         int userResponse = 0;
                 
         for (TableItem item : selitems)
         {
            PSFieldTableRowDataObject row = (PSFieldTableRowDataObject) item
                  .getData();
            if (row == null || row.isEmpty() || row.isFieldSet())
               continue;
            PSField fld = row.getField();
            if (fld.getLocator() != null
                  && (m_editorType == PSContentEditorDefinition.SHAREDDEF_EDITOR
                        || fld.isLocalField()))
            {
               if (userResponse == 0)
               {
                  String colDelTitle = PSMessages.getString(
                        "PSCEFieldsCommonComposite.warn.title.confirmstoragedeletion"); //$NON-NLS-1$
                  String colDelMsg = PSMessages.getString(
                        "PSCEFieldsCommonComposite.warn.message.confirmstoragedeletion"); //$NON-NLS-1$
                  MessageBox msgBox = new MessageBox(getShell(),
                        SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
                  msgBox.setMessage(colDelMsg);
                  msgBox.setText(colDelTitle);
                  userResponse = msgBox.open();
               }
               if (userResponse == SWT.NO)
                  break;
               else if (userResponse == SWT.CANCEL)
                  return false;

               setColumnModificationActions(fld,
                     IPSCETableColumnActions.COLUMN_ACTION_DELETE);
            }
         }
         showPropertyComposites(false, false);
         return true;
      }
   }

   /**
    * The choice list table, initialized in the ctor, never <code>null</code>
    * after that.
    */
   private Label m_fieldTableLabel;

   private Label m_staticFieldLabel;

   private PSSortableTable m_fieldsTable;

   private Table m_table;

   private PSFieldPropertiesComposite m_fieldProperties;

   private PSFieldSetPropertiesComposite m_fieldSetProperties;

   private int m_editorType;

   private String m_tableLabel;

   private List<PSFieldTableRowDataObject> m_fieldData;

   public static final String CHILD_TABLE_CONTROLNAME = "sys_Table"; //$NON-NLS-1$

   private CellEditor m_nameCellEditor;

   private CellEditor m_labelCellEditor;

   private PSButtonedComboBoxCellEditor m_ctrlCellEditor;

   private PSEditorBase m_editor;

   private String m_fieldSetName;

   /**
    * Suffix to add to simple child field name to create the parent fieldset
    * name that is unique from child.
    */
   private static final String SIMPLE_FIELDSET_SUFFIX = "_set"; //$NON-NLS-1$

   /**
    * Map of control dependencies. Set in loadcontrol values. Will be
    * <code>null</code> for System and Shared def editors
    */
   private PSControlDependencyMap m_ctrlDepMap;

   /**
    * Name of the database table representing this field set. May be
    * <code>null</code>. This is set while loading the control values 
    * see{@link #loadControlValues(Object)}.
    * For newly created content types this will be set after saving the content
    * type in {@link 
    * PSContentTypeEditor#doSave(org.eclipse.core.runtime.IProgressMonitor)}.
    */
   private String m_dbTableName = null;
   
   /**
    * This flag is used so we only show the warning message about simple 
    * children once. This message tells the user that they cannot enter more 
    * fields if they have made the first one a multi-value field. Defaults
    * to <code>false</code>.
    */
   private boolean m_simpleChildWarningShown = false;
}

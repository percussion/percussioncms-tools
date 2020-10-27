/******************************************************************************
 *
 * [ PSContentTypeMainTab.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.PSModelException;
import com.percussion.cms.objectstore.PSFieldDefinition;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSButtonFactory;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.form.PSContentTypeEditor;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class renders the content type main tab. Creates a system and shared
 * field composite, adds the buttons and then adds the fields table.
 */
public class PSContentTypeMainTab extends Composite
      implements
         SelectionListener,
         IPSUiConstants,
         IPSDesignerObjectUpdater
{

   public PSContentTypeMainTab(Composite parent, int style,
         PSEditorBase editor) {
      super(parent, style);
      m_editor = editor;
      setLayout(new FormLayout());
      m_sysNShFieldTree = 
         new PSSysAndSharedFieldTreeComposite(this, SWT.BOTTOM);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      formData.bottom = new FormAttachment(100, 0);
      m_sysNShFieldTree.setLayoutData(formData);

      m_buttonsComposite = new Composite(this, SWT.NONE);
      m_buttonsComposite.setLayout(new FormLayout());
      final FormData formData_1 = new FormData();
      formData_1.width = 50;
      formData_1.top = new FormAttachment(m_sysNShFieldTree, 0, SWT.TOP);
      formData_1.bottom = new FormAttachment(50, 0);
      formData_1.left = new FormAttachment(m_sysNShFieldTree,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_buttonsComposite.setLayoutData(formData_1);

      m_moveUpButton = PSButtonFactory.createUpButton(m_buttonsComposite);
      m_moveUpButton.setLayoutData(createFormData(null, 2 * LABEL_VSPACE_OFFSET));
      m_moveUpButton.setToolTipText(PSMessages.getString("PSContentTypeMainTab.button.toottip.moveup")); //$NON-NLS-1$

      m_moveDnButton = PSButtonFactory.createDownButton(m_buttonsComposite);
      m_moveDnButton.setLayoutData(createFormData(m_moveUpButton,
            BUTTON_VSPACE_OFFSET));
      m_moveDnButton.setToolTipText(PSMessages.getString("PSContentTypeMainTab.button.toottip.movedn")); //$NON-NLS-1$

      m_insertButton = PSButtonFactory.createAddButton(m_buttonsComposite);
      m_insertButton.setLayoutData(createFormData(m_moveDnButton,
            BUTTON_VSPACE_OFFSET));
      m_insertButton.setToolTipText(PSMessages.getString("PSContentTypeMainTab.button.toottip.insert")); //$NON-NLS-1$

      m_deleteButton = PSButtonFactory.createDeleteButton(m_buttonsComposite);
      m_deleteButton.setLayoutData(createFormData(m_insertButton,
            BUTTON_VSPACE_OFFSET));
      m_deleteButton.setToolTipText(PSMessages.getString("PSContentTypeMainTab.button.toottip.delete")); //$NON-NLS-1$
      m_deleteButton.addSelectionListener(this);
      
      m_addChildButton = PSButtonFactory.createAddChildButton(m_buttonsComposite);
      m_addChildButton.setLayoutData(createFormData(m_deleteButton,
            BUTTON_VSPACE_OFFSET));
      m_addChildButton.setToolTipText(PSMessages.getString("PSContentTypeMainTab.button.toottip.addchild")); //$NON-NLS-1$

      // Add and remove systema nd shared field buttons.
      m_addFieldButton = PSButtonFactory.createRightButton(m_buttonsComposite);
      m_addFieldButton.setLayoutData(createFormData(m_addChildButton,
            2 * LABEL_VSPACE_OFFSET));
      m_addFieldButton.setToolTipText(PSMessages.getString("PSContentTypeMainTab.button.toottip.addfields")); //$NON-NLS-1$

      //******************************/
      m_fieldsComp = new PSCEFieldsCommonComposite(this, SWT.NONE,editor,
            PSMessages.getString("PSContentTypeMainTab.table.label.fieldsnsets"), PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR,null); //$NON-NLS-1$
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(0, 0);
      formData_10.left = new FormAttachment(m_buttonsComposite,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_10.bottom = new FormAttachment(100, 0);
      formData_10.right = new FormAttachment(100, 0);
      m_fieldsComp.setLayoutData(formData_10);
      registerButtons(editor);
   }

   /**
    * Registers the buttons used in this composite. The table related buttons are
    * registered with the table and other buttons with the editor.
    * @param editor Object of base editor assumed not <code>null</code>.
    */
   private void registerButtons(PSEditorBase editor)
   {
      PSSortableTable table = m_fieldsComp.getTableComp();
      table.registerButton(m_moveUpButton,PSSortableTable.TYPE_MOVE_UP);
      table.registerButton(m_moveDnButton,PSSortableTable.TYPE_MOVE_DN);
      table.registerButton(m_insertButton,PSSortableTable.TYPE_INSERT);
      table.registerButton(m_deleteButton,PSSortableTable.TYPE_DELETE);
      editor.registerControl(m_addChildButton.getToolTipText(),m_addChildButton,null);
      editor.registerControl(m_addFieldButton.getToolTipText(),m_addFieldButton,null);
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
   }

   /**
    * Convenient method to create the FormData object for the buttons layout.
    * 
    * @param prev Object of previous control, if <code>null</code> alligned to
    *           top with the given offset otherwise aligned to the previous
    *           control with the given offset.
    * @param topOffset The offset that needs to be given to the top.
    * @return FormData object for the buttons, never <code>null</code>.
    */
   private FormData createFormData(Control prev, int topOffset)
   {
      final FormData formData = new FormData();
      if (prev != null)
         formData.top = new FormAttachment(prev, topOffset, SWT.BOTTOM);
      else
         formData.top = new FormAttachment(0, topOffset);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.height = 21;
      return formData;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void widgetSelected(SelectionEvent e)
   {
      if(e.getSource() == m_fieldsComp.getTableComp())
      {
         @SuppressWarnings("unused")  //$NON-NLS-1$
         Table table = m_fieldsComp.getTableComp().getTable();
         TableItem[] items = table.getSelection();
         boolean disableButtons = false;
         for(TableItem item:items)
         {
            PSFieldTableRowDataObject rowData = (PSFieldTableRowDataObject)item.getData();
            if(rowData != null && rowData.getField() != null && rowData.getField().isSystemMandatory())
            {
               disableButtons = true;
               break;
            }
         }
         m_deleteButton.setEnabled(!disableButtons);
      }
      else if (e.getSource() == m_deleteButton)
      {
         /*
          * The following code handles the deletion of the child table rows.
          * Other rows are handled by the preDelete method of sortable table.
          * See {@link PSCEFieldsCommonComposite#CeSortableTable#preDelete()}.
          * Loops through all the selected rows and for each child table row
          * that has mappings asks the user whether he wants to continue with
          * the deletion of child table. If user selects no then that row is
          * removed from the selection.
          * 
          * It also removes the system mandatory fields if any from the
          * selection.
          */
         TableItem[] selitems = m_fieldsComp.getTableComp().getTable()
               .getSelection();
         List<TableItem> remSel = new ArrayList<TableItem>();
         for (TableItem item : selitems)
         {
            PSFieldTableRowDataObject row = (PSFieldTableRowDataObject) item.getData();
            if(row == null)
               continue;
            
            if(row.isFieldSet())
            {
               PSFieldSet fset = row.getFieldSet();
               if(fset.getAllFields().length > 0)
               {
                  String msg = MessageFormat
                        .format(
                              PSMessages.getString("PSContentTypeMainTab.warn.msg.deletechildtablerow"), //$NON-NLS-1$
                              row.getName());
                  String title = PSMessages.getString("PSContentTypeMainTab.warn.title.deletechildtablerow"); //$NON-NLS-1$
                  if(MessageDialog.openQuestion(getShell(),title,msg) &&
                        !m_fieldsComp.allLocalFieldsWithLocatorSelected())
                  {
                     deleteChild(row.getName());
                  }
                  else
                  {
                     remSel.add(item);
                  }
               }
               else
               {
                  deleteChild(row.getName());
               }
            }
            else
            {
               PSField fld = row.getField();
               if(fld.isSystemMandatory())
                  remSel.add(item);
            }
         }
         if (!remSel.isEmpty())
         {
            List<TableItem> newList = new ArrayList(Arrays.asList(selitems));
            newList.removeAll(remSel);
            m_fieldsComp.getTableComp().getTable().setSelection(
                  newList.toArray(new TableItem[0]));
         }
      }
   }

   /**
    * Convenient method to check for the duplicate field definition objects in
    * the list Checks only the field name.
    * 
    * @return <code>true</code> if it has duplicates otherwise<code>false</code>;
    */
   private boolean hasDuplicates(List<PSFieldDefinition> fieldDefs)
   {
      Set<String> tmp = new HashSet<String>();
      for (int i = 0; i < fieldDefs.size(); i++)
      {
         tmp.add(fieldDefs.get(i).getField().getSubmitName());
      }
      return tmp.size() == fieldDefs.size() ? false : true;
   }

   /**
    * Convenient method to check for the shared or system field conflicts with the
    * local fields checks only the field name.
    * 
    * @return <code>List</code> of conflict names, may be empty, but never 
    * <code>null</code>
    */
   private List<String> getLocalFieldConflicts(List<PSFieldDefinition> fieldDefs)
   {
      List<PSFieldTableRowDataObject> rows = m_fieldsComp.getTableComp().getValues();
      List<String> existingFieldNames = new ArrayList<String>();
      List<String> conflictNames = new ArrayList<String>();

      // Build a list of names of the existing fields
      for(int i=0;i<rows.size();i++)
      {
         existingFieldNames.add(rows.get(i).getName());
      }

      // Check the new field's name against all the existing field names.
      for(int i=0; i<fieldDefs.size();i++)
      {
         PSField newField = fieldDefs.get(i).getField(); 

         String newFieldName = newField.getSubmitName();
         if(existingFieldNames.indexOf(newFieldName) != -1)
         {
            conflictNames.add(newFieldName);
         }
         
         // If the new field is a Shared field, check the new field's Shared
         // Group name against the existing field's names.
         if (newField.getType() == PSField.TYPE_SHARED)
         {
            String sharedGroupFieldSetName = 
               (String) newField.getUserProperty("sharedgroupfieldset");
            assert( sharedGroupFieldSetName != null);
            
            if(existingFieldNames.indexOf(sharedGroupFieldSetName) != -1)
            {
               String combinedName = sharedGroupFieldSetName + ":" + newFieldName;
               conflictNames.add(combinedName);
            }
         
         } 

      }
      return conflictNames;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(
         @SuppressWarnings("unused") SelectionEvent e)
   {
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSItemDefinition itemDef = (PSItemDefinition)designObject;
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
      .getContentEditor().getPipe();
      PSContentEditorMapper ceMapper = pipe.getMapper();
      PSUIDefinition uiDef = ceMapper.getUIDefinition();
      PSDisplayMapper parentMapper = uiDef.getDisplayMapper();
      PSFieldSet parentFieldSet = ceMapper.getFieldSet();
      
      if(control == m_addChildButton)
      {
         addChildField(parentFieldSet,parentMapper);
         m_fieldsComp.updateDesignerObject(designObject,m_fieldsComp.getTableComp());
      }
      else if (control == m_addFieldButton)
      {
         processFieldNames:
         {
            // Get the selections
            List<PSFieldDefinition> fds = m_sysNShFieldTree.getSelectedFieldDefs();
            // Check for duplicates
            if (hasDuplicates(fds))
            {
               String errMsg = PSMessages.getString(
                  "PSContentTypeMainTab.error.msg.duplicatefieldselection"); //$NON-NLS-1$
               MessageDialog.openError(getShell(),PSMessages.getString(
                  "PSContentTypeMainTab.error.title.duplicatefieldselection"),
                  errMsg); //$NON-NLS-1$
               break processFieldNames;
            }

            // Check for the name conflicts with local fields.
            List<String> conflicts = getLocalFieldConflicts(fds);
            if (conflicts.size() > 0)
            {
                String errMsg = PSMessages.getString(
                   "PSContentTypeMainTab.error.msg.nameconflict") 
                   + conflicts.toString(); //$NON-NLS-1$
                MessageDialog.openError(getShell(),PSMessages.getString(
                   "PSContentTypeMainTab.error.title.nameconflicts"), errMsg); //$NON-NLS-1$
                break processFieldNames;
            }

           // We are all set, move the fields
           // Create the list of PSFieldTableRowDataObjects from  
           // the PSFieldDefinition list, and add them.
           final List<PSFieldTableRowDataObject> newrows =
                 new ArrayList<PSFieldTableRowDataObject>();
           for (PSFieldDefinition fdef : fds)
           {
              m_fieldsComp.setControlDependencies(fdef);
              newrows.add(new PSFieldTableRowDataObject(fdef));
           }
           List<PSFieldTableRowDataObject> rows = m_fieldsComp
                 .getTableComp().getValues();
           rows.addAll(newrows);
           
           m_fieldsComp.getTableComp().setValues(rows);
           m_fieldsComp.getTableComp().refreshTable();
           addSystemOrSharedFields(parentFieldSet,parentMapper,newrows);
         } //processFieldNames

         m_fieldsComp.updateDesignerObject(designObject,m_fieldsComp.getTableComp());
      } 
      else if(control == m_fieldsComp.getTableComp())
      {
         m_fieldsComp.updateDesignerObject(designObject,m_fieldsComp.getTableComp());
      } //end: "if (control=....)"
   }

   
   /**
    * Adds the system or shared field rows to the supplied parent fieldset and
    * display mapper.
    * 
    * @param parentFieldSet Parent field set assumed not <code>null</code>.
    * @param parentMapper Parent display mapper assumed not <code>null</code>.
    * @param rows List of PSFieldTableRowDataObject representing the shared or
    *           system fields.
    */
   private void addSystemOrSharedFields(PSFieldSet parentFieldSet,
         PSDisplayMapper parentMapper, List<PSFieldTableRowDataObject> rows)
   {
      for (PSFieldTableRowDataObject row : rows)
      {
         PSField fld = row.getField();
         if (fld.isSystemField())
         {
            parentFieldSet.add(fld);
            parentMapper.add(row.getDisplayMapping());
         }
         else
         {
            try
            {
               String groupName = 
                  (String)fld.getUserProperty(
                        PSField.SHARED_GROUP_FIELDSET_USER_PROP);
               assert(StringUtils.isNotBlank(groupName));
               // If any of the fields this fieldset are already added then
               // we add this field to that set, otherwise we get the fieldset
               // from shared def and add this field alone and set to the
               // fieldset.
               PSFieldSet childSet = null;
               if (parentFieldSet.contains(groupName))
               {
                  childSet = (PSFieldSet) parentFieldSet.get(groupName);
               }
               else
               {
                  childSet = new PSFieldSet(PSContentEditorDefinition
                        .getSharedFieldSet(groupName));
                  childSet.removeAll();
               }
               childSet.add(fld);
               parentFieldSet.add(childSet);
               parentMapper.add(row.getDisplayMapping());
            }
            catch (Exception e)
            {
               String title = PSMessages
               .getString("PSContentTypeMainTab.error.title.failedtoupdatedata"); //$NON-NLS-1$
               String msg1 = PSMessages
               .getString("PSContentTypeMainTab.error.msg.failetoupdatedata"); //$NON-NLS-1$
               PSWorkbenchPlugin.handleException(
                     "CE Update Data", title, msg1, e); //$NON-NLS-1$
            }
         }
      }
   }
   
   /**
    * Calls parent's deleteChildPage method to delete a local def child field. If
    * the editor type is not PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR, then
    * does nothing.
    * 
    * @param fieldSetName name of the child field set must not be null or empty.
    */
   private void deleteChild(String fieldSetName)
   {
      if (fieldSetName == null || fieldSetName.length() == 0)
      {
         throw new IllegalArgumentException(
               "fieldSetName must not be null or empty"); //$NON-NLS-1$
      }
      PSContentTypeEditor editor = (PSContentTypeEditor) m_editor;
      editor.removeChildPage(fieldSetName);
   }
   
   /**
    * Adds the child field at the end.
    * @param itemDef object of PSItemDefinition assumed not <code>null</code>.
    */
   private void addChildField(PSFieldSet parentFieldSet,
         PSDisplayMapper parentMapper)
   {
      // Fix the rowData We need a new uiset and the field becomes
      // fieldset
      String[] fnames = new String[0];
      fnames = ((PSContentTypeEditor) m_editor).getAllFieldNames();
      int count=1;
      for (int i = 0; i < fnames.length; i++)
      {
         String fname = StringUtils.upperCase(fnames[i]);
         if(fname.startsWith("CHILD")) //$NON-NLS-1$
         {
            int number = 0;
            try
            {
               number = Integer.parseInt(fname.substring(5));
               count = count == number?count+1:count>number?count:number+1;
            }
            catch(NumberFormatException e)
            {
               continue;
            }
         }
      }
      String name = "Child" + count; //$NON-NLS-1$
      PSUISet uiSet = null;
      try
      {
         uiSet = PSContentEditorDefinition.getDefaultUISet(name,
               PSContentEditorDefinition.PARENT_CHILD_FIELD);
      }
      catch (PSModelException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      PSDisplayMapping mapping = new PSDisplayMapping(name, uiSet);
      mapping.setDisplayMapper(new PSDisplayMapper(name));
      PSFieldDefinition fieldDef = new PSFieldDefinition(
            PSContentEditorDefinition.getDefaultFieldSet(name),
            mapping);
      PSFieldTableRowDataObject rowData = new PSFieldTableRowDataObject(fieldDef);
      List<PSFieldTableRowDataObject> values = m_fieldsComp.getTableComp().getValues();
      values.add(rowData);
      m_fieldsComp.getTableComp().setValues(values);
      m_fieldsComp.getTableComp().refreshTable();
      createChild(name);
      parentFieldSet.add(fieldDef.getFieldset());
      parentMapper.add(mapping);
   }

   /**
    * Calls parent's addChildPage method to create a local def child field.  If
    * the editor type is not PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR, then
    * does nothing.
    * 
    * @param fieldSetName name of the child field set must not be null or empty.
    */
   private void createChild(String fieldSetName)
   {
      if (fieldSetName == null || fieldSetName.length() == 0)
      {
         throw new IllegalArgumentException(
               "fieldSetName must not be null or empty"); //$NON-NLS-1$
      }
      PSContentTypeEditor editor = (PSContentTypeEditor) m_editor;
      editor.addChildPage(fieldSetName);
   }

   /**
    * Convenient method to check whether the supplied rows have a
    * systemMandatory fields
    */
   private boolean hasSystemMandaoryFields(List<PSFieldTableRowDataObject> rows)
   {
      boolean result = false; 
      for(PSFieldTableRowDataObject row : rows)
      {
         if(row.isFieldSet())
            continue;
         PSField fld = row.getField();
         if(fld.isSystemMandatory())
         {
            result = true;
            break;
         }
      }
      return result;
   }
   
   /**
    * Gets the sortable table that represents the fields.
    */
   public PSSortableTable getSortableTable()
   {
      return m_fieldsComp.getTableComp();
   }
   
   public void setDBTableName(PSFieldSet fieldSet)
   {
      m_fieldsComp.setDBTableName(fieldSet);
      
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      m_fieldsComp.loadControlValues(designObject);
   }

   /**
    * Composite to hold move and insert buttons
    */
   private Composite m_buttonsComposite;

   /**
    * Fields common composite control
    */
   private PSCEFieldsCommonComposite m_fieldsComp;

   /**
    * Button to system or shared fields.
    */
   private Button m_addFieldButton;

   /**
    * Button to move selected table row/s up.
    */
   private Button m_moveUpButton;

   /**
    * Button to move selected table row/s down.
    */
   private Button m_moveDnButton;

   /**
    * Button to insert a row at selection.
    */
   private Button m_insertButton;

   /**
    * Button to add child table.
    */
   private Button m_addChildButton;

   /**
    * Button to delete the selected rows.
    */
   private Button m_deleteButton;

   /**
    * system and shared tree composite. Initialized in ctor and never <code>null</code> afetr that.
    */
   private PSSysAndSharedFieldTreeComposite m_sysNShFieldTree;
   
   /**
    * The base editor for the content type.
    */
   private PSEditorBase m_editor;

}

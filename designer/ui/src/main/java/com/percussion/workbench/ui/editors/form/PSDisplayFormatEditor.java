/******************************************************************************
 *
 * [ PSDisplayFormatEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSDFColumns;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.client.PSLightWeightField;
import com.percussion.search.ui.PSFieldSelectionEditorDialog;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import com.percussion.workbench.ui.editors.common.PSEditorUtil;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides a single paned UI for modifying a display format design object.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSDisplayFormatEditor extends PSEditorBase
   implements IPSUiConstants
{
   
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * isValidReference(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if(ref == null)
         return false; // Should never happen
      if(ref.getObjectType().getPrimaryType() == 
         PSObjectTypes.UI_DISPLAY_FORMAT)
         return true;
      return false;
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * createControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());

      m_nameLabelDesc = new PSNameLabelDesc(comp, SWT.NONE,
         "Display Format", //$NON-NLS-1$
         -1,
         PSNameLabelDesc.SHOW_ALL |
         PSNameLabelDesc.LAYOUT_SIDE |
         PSNameLabelDesc.NAME_READ_ONLY |
         PSNameLabelDesc.LABEL_USES_NAME_PREFIX, this);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      m_nameLabelDesc.setLayoutData(formData);
      
      m_usedWithButtons = new PSRadioAndCheckBoxes(comp,
         PSMessages.getString("PSDisplayFormatEditor.willBeUsedWith.label"), //$NON-NLS-1$
         SWT.RADIO | SWT.VERTICAL | SWT.SEPARATOR);
      
      m_usedWithButtons.addEntry(PSMessages.getString(
         "PSDisplayFormatEditor.usedWith.relContent.choice")); //$NON-NLS-1$
      m_usedWithButtons.addEntry(PSMessages.getString(
         "PSDisplayFormatEditor.usedWith.other.label")); //$NON-NLS-1$
      m_usedWithButtons.addEntry(PSMessages.getString(
         "PSDisplayFormatEditor.usedWith.folders")); //$NON-NLS-1$
      m_usedWithButtons.layoutControls();
      registerControlHelpOnly("PSDisplayFormatEditor.willBeUsedWith.label",
         m_usedWithButtons);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(m_nameLabelDesc, 15, SWT.BOTTOM);
      formData_1.left = new FormAttachment(0, 0);
      formData_1.right = new FormAttachment(50, -20);
      m_usedWithButtons.setLayoutData(formData_1);
      m_usedWithButtons.addSelectionListener(new SelectionAdapter()
      {
         /* 
          * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
          * org.eclipse.swt.events.SelectionEvent)
          */
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
         {
            validateRadioButtons();
            m_lastSelectedUsedFor = m_usedWithButtons.getSelectedIndex();
         }
      });
      
      Label extraText = new Label(comp, SWT.LEFT);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_usedWithButtons, 0, SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_usedWithButtons, 40, SWT.LEFT);
      extraText.setLayoutData(formData_3);
      extraText.setText(PSMessages.getString(
         "PSDisplayFormatEditor.noCatAllowed.label"));       //$NON-NLS-1$

      m_sortColumnComp = createSortColumnComposite(comp);
      final FormData formData_2 = new FormData();      
      formData_2.right = new FormAttachment(100, 0);
      formData_2.top = new FormAttachment(m_usedWithButtons, 0, SWT.TOP);
      formData_2.left = new FormAttachment(50, 0);
      m_sortColumnComp.setLayoutData(formData_2);
     
      m_columnsComp = new PSDisplayFormatColumsTablesComposite(comp, SWT.NONE, this);
      final FormData formData_4 = new FormData();      
      formData_4.right = new FormAttachment(100, 0);
      formData_4.top = new FormAttachment(extraText, 15, SWT.TOP);
      formData_4.left = new FormAttachment(0, 0);
      m_columnsComp.setLayoutData(formData_4);
      registerControl(
         "PSDisplayFormatEditor.columnsTable.label", //$NON-NLS-1$
         m_columnsComp,
         null);

      
   }
      
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSDisplayFormat displayFormat = (PSDisplayFormat)designObject;
      
      if(control == m_nameLabelDesc.getLabelText())
      {
         displayFormat.setDisplayName(
            m_nameLabelDesc.getLabelText().getText());
      }
      else if(control == m_nameLabelDesc.getDescriptionText())
      {
         displayFormat.setDescription(
            m_nameLabelDesc.getDescriptionText().getText());
      }
      else if(control == m_sortColumnCombo)
      {
         StructuredSelection selection = 
            (StructuredSelection)m_sortColumnComboViewer.getSelection();
                  
         displayFormat.setProperty(PSDisplayFormat.PROP_SORT_COLUMN,
            (String)selection.getFirstElement());
      }
      else if(control == m_ascendingButton || control == m_descendingButton)
      {
         String direction =  m_descendingButton.getSelection() 
         ? PSDisplayFormat.SORT_DESCENDING
            : PSDisplayFormat.SORT_ASCENDING;
         displayFormat.setProperty(
            PSDisplayFormat.PROP_SORT_DIRECTION, direction);
      }
      else
      {
         m_columnsComp.updateDesignerObject(designObject, control);
      }
      
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSDisplayFormat displayFormat = (PSDisplayFormat)designObject;
      
      // Set name
      ((Label)m_nameLabelDesc.getNameText()).setText(
         StringUtils.defaultString(displayFormat.getInternalName()));
      // Set Label
      m_nameLabelDesc.getLabelText().setText(
         StringUtils.defaultString(displayFormat.getDisplayName()));
      // Set Description
      m_nameLabelDesc.getDescriptionText().setText(
         StringUtils.defaultString(displayFormat.getDescription()));
              
      // Set sort direction
      boolean descending = displayFormat.doesPropertyHaveValue(
         PSDisplayFormat.PROP_SORT_DIRECTION, 
         PSDisplayFormat.SORT_DESCENDING);
      if(descending)
      {
         m_descendingButton.setSelection(true);
      }
      else
      {
         m_ascendingButton.setSelection(true);
      }
      
      // Set radio buttons
      if(displayFormat.getColumnContainer().size() > 0)
      {
         if (displayFormat.isValidForRelatedContent())
            m_usedWithButtons.setSelection(RELATED_BUTTON);
         else if (displayFormat.isValidForFolder())
            m_usedWithButtons.setSelection(FOLDERS_BUTTON);
         else
            m_usedWithButtons.setSelection(OTHER_BUTTON);
      }
      else
      {         
         m_usedWithButtons.setSelection(OTHER_BUTTON);
      }
      
      // add sys_title as first column if missing
      if (!hasColumn(COL_TYPE_TITLE))
      {
         PSDFColumns columns = displayFormat.getColumnContainer();
         columns.add(createDispCol(
            PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD, null));
      }
      else
      {
         // see if it's been categorized.  If so, move it.
         boolean moved = false;         
         Iterator it = displayFormat.getColumns();
         while(it.hasNext())
         {
            PSDisplayColumn col = (PSDisplayColumn)it.next();
            String sourceName = col.getSource();
            if(sourceName.equalsIgnoreCase(
               PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD))
            {
               moved = col.isCategorized();
               col.setGroupingType(PSDisplayColumn.GROUPING_FLAT);
            }
         }
         if (moved)
         {
            //TODO Add popup dialog here
            //PSDlgUtil.showErrorDialog(ms_res.getString("systitle.cat.msg"),
                  //ms_res.getString("systitle.cat.title"));
         }
      }
      m_columnsComp.loadControlValues(m_data);
      
   }
   
   /**
    * Creates a display column.
    *
    * @param internalName the name fot he field to be added.  Assumed not
    * <code>null</code>.
    *
    * @param labelName , this method will first check with the cataloger to see
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
      PSLightWeightField temp = PSEditorUtil.getCEFieldCatalog(true)
         .getSystemMap().get(internalName);

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
    * Checks to see if specified column type is present in the display format.
    *
    * @param columnType the column type to look for, currently only one of the 
    * following choices: <code>COL_TYPE_CONTENTTYPE, COL_TYPE_VARIANT, COL_TYPE_TITLE</code>
    *
    * @return <code>true</code> if the column exists.
    */
   protected boolean hasColumn(int columnType )
   {
      PSDisplayFormat displayFormat = (PSDisplayFormat)m_data;
      
      Iterator it = displayFormat.getColumns();
      while(it.hasNext())
      {
         PSDisplayColumn col = (PSDisplayColumn)it.next();
         String sourceName = col.getSource();
         if (columnType == COL_TYPE_CONTENTTYPE && 
            (sourceName.equalsIgnoreCase(PSDisplayFormat.COL_CONTENTTYPEID)
               || sourceName.equalsIgnoreCase(PSDisplayFormat.COL_CONTENTTYPENAME)))
         {
            return true;
         }
         else if (columnType == COL_TYPE_VARIANT &&
            (sourceName.equalsIgnoreCase(PSDisplayFormat.COL_VARIANTID)
               || sourceName.equalsIgnoreCase(PSDisplayFormat.COL_VARIANTNAME)))
         {
            return true;
         }
         else if(columnType == COL_TYPE_TITLE &&
            (sourceName.equalsIgnoreCase(
               PSFieldSelectionEditorDialog.CONTENT_TITLE_FIELD)))
            return true;
      }      
      
      return false;
   }
   
   /**
    * Creates the composite with the sort button and ascending/Descending
    * radio buttons.
    * @param parent
    * @return the compsite, never <code>null</code>.
    */
   private Composite createSortColumnComposite(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);      
      comp.setLayout(new FormLayout());

      m_sortColumnLabel = new Label(comp, SWT.NONE);
      final FormData formData_13 = new FormData();
      formData_13.top = new FormAttachment(0,
         LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      formData_13.left = new FormAttachment(0, 0);
      m_sortColumnLabel.setLayoutData(formData_13);
      m_sortColumnLabel.setText(PSMessages.getString(
         "PSDisplayFormatEditor.sortColumn.label")); //$NON-NLS-1$

      m_sortColumnComboViewer = new ComboViewer(comp, SWT.BORDER | SWT.READ_ONLY);
      m_sortColumnComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_sortColumnComboViewer.setSorter(new ViewerSorter());
      m_sortColumnCombo = m_sortColumnComboViewer.getCombo();
      final FormData formData_14 = new FormData();      
      formData_14.right = new FormAttachment(100, 0);
      formData_14.top = new FormAttachment(m_sortColumnLabel,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_14.left = new FormAttachment(m_sortColumnLabel, 5, SWT.RIGHT);
      m_sortColumnCombo.setLayoutData(formData_14);
      registerControl(
         "PSDisplayFormatEditor.sortColumn.label",
         m_sortColumnCombo,
         null);
      
      Composite radioGroup = new Composite(comp, SWT.NONE);
      radioGroup.setLayout(new FormLayout());
      final FormData formData_rg = new FormData();      
      formData_rg.top = new FormAttachment(m_sortColumnCombo, 5, SWT.BOTTOM);
      formData_rg.left = new FormAttachment(m_sortColumnCombo, 0, SWT.LEFT);
      formData_rg.right = new FormAttachment(100, 0);
      radioGroup.setLayoutData(formData_rg);

      m_ascendingButton = new Button(radioGroup, SWT.RADIO);
      final FormData formData_15 = new FormData();      
      formData_15.top = new FormAttachment(0, 0);
      formData_15.left = new FormAttachment(0, 0);
      m_ascendingButton.setLayoutData(formData_15);
      m_ascendingButton.setText(PSMessages.getString(
         "PSDisplayFormatEditor.sortOrder.ascending.choice")); //$NON-NLS-1$
      registerControl(
         "PSDisplayFormatEditor.sortOrder.ascending.choice",
         m_ascendingButton,
         null);

      m_descendingButton = new Button(radioGroup, SWT.RADIO);
      final FormData formData_16 = new FormData();      
      formData_16.top = new FormAttachment(0, 0);
      formData_16.left = new FormAttachment(m_ascendingButton, 10, SWT.RIGHT);
      m_descendingButton.setLayoutData(formData_16);
      m_descendingButton.setText(PSMessages.getString(
         "PSDisplayFormatEditor.sortOrder.descending.choice")); //$NON-NLS-1$
      registerControl(
         "PSDisplayFormatEditor.sortOrder.descending.choice",
         m_descendingButton,
         null);
      Control[] tabList = new Control[]{
         m_sortColumnCombo, radioGroup};
      comp.setTabList(tabList);
      return comp;
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
    */
   private void validateRadioButtons()
   {
      // validate the folder is selected
      if (m_usedWithButtons.getSelectedIndex() == FOLDERS_BUTTON)
      {
         if (m_columnsComp.getCategoryCount() > 0)
         {
            MessageBox msgBox = new MessageBox(getSite().getShell(),
               SWT.ICON_WARNING | SWT.YES | SWT.NO );
            msgBox.setText(PSMessages.getString(
               "PSDisplayFormatEditor.warning.moveCategories.title")); //$NON-NLS-1$
            msgBox.setMessage(PSMessages.getString(
               "PSDisplayFormatEditor.warning.moveCategories")); //$NON-NLS-1$
            int choice = msgBox.open();            
            if (choice == SWT.YES)
            {
               m_columnsComp.onAddAll();
            }
            else // set back to what it was
            {
               if (m_lastSelectedUsedFor == OTHER_BUTTON 
                   || m_lastSelectedUsedFor == FOLDERS_BUTTON)
               {
                  m_usedWithButtons.setSelection(OTHER_BUTTON);
               }
               else if (m_lastSelectedUsedFor == RELATED_BUTTON)
               {
                  m_usedWithButtons.setSelection(RELATED_BUTTON);
               }
            }
         }
         
         validateFolderColumns();
      }
      
      if (m_usedWithButtons.getSelectedIndex() == RELATED_BUTTON)
      {
         // does it have variant name column:
         if (!hasColumn(COL_TYPE_VARIANT))
         {
            MessageBox msgBox = new MessageBox(getSite().getShell(),
               SWT.ICON_INFORMATION | SWT.OK );
            msgBox.setText(PSMessages.getString(
               "PSDisplayFormatEditor.info.variantidRequired.title")); //$NON-NLS-1$
            msgBox.setMessage(PSMessages.getString(
               "PSDisplayFormatEditor.info.variantidRequired")); //$NON-NLS-1$
            msgBox.open();
            PSDisplayColumn variantCol = createDispCol(
               PSDisplayFormat.COL_VARIANTID, "Variant"); //$NON-NLS-1$
            addToColumns(variantCol);
         }
         if (!hasColumn(COL_TYPE_CONTENTTYPE))
         {
            MessageBox msgBox = new MessageBox(getSite().getShell(),
               SWT.ICON_INFORMATION | SWT.OK );
            msgBox.setText(PSMessages.getString(
               "PSDisplayFormatEditor.info.contenttypeidRequired.title")); //$NON-NLS-1$
            msgBox.setMessage(PSMessages.getString(
               "PSDisplayFormatEditor.info.contenttypeidRequired")); //$NON-NLS-1$
            msgBox.open();
            PSDisplayColumn contentTypeCol = createDispCol(
               PSDisplayFormat.COL_CONTENTTYPEID, null);
            addToColumns(contentTypeCol);
         }
      }
      else
      {
         // if there is a variant name column
         // this means variant name was selected:
         if (hasColumn(COL_TYPE_VARIANT) && hasColumn(COL_TYPE_CONTENTTYPE))
         {
            MessageBox msgBox = new MessageBox(getSite().getShell(),
               SWT.ICON_WARNING | SWT.YES | SWT.NO );
            msgBox.setText(PSMessages.getString(
               "PSDisplayFormatEditor.warning.removeContenttypeid.title")); //$NON-NLS-1$
            msgBox.setMessage(PSMessages.getString(
               "PSDisplayFormatEditor.warning.removeContenttypeid")); //$NON-NLS-1$
            int choice = msgBox.open();
            if (choice == SWT.YES)
            {
               removeField(PSDisplayFormat.COL_CONTENTTYPEID);
            }
         }
         
         removeField(PSDisplayFormat.COL_VARIANTID);
         m_columnsComp.updateTables();
      }
   }
   
   /**
    * Validates that all columns are valid to be used with folders. This
    * validation is only performed if the currently selected usage is set to
    * <code>FOLDERS_BUTTON</code>.
    * <p/>
    * If invalid colums are found the user will have the choice either to 
    * change the selected usage to <code>OTHER_BUTTON</code> or to remove all 
    * invalid columns.
    */
   public void validateFolderColumns()
   {
      if (m_usedWithButtons.getSelectedIndex() == FOLDERS_BUTTON)
      {
         if (m_columnsComp.getColumnCount() > 0)
         {
            PSDisplayFormat df = (PSDisplayFormat) m_data;
            if (!df.isValidForFolder())
            {
               MessageBox msgBox = new MessageBox(getSite().getShell(),
                  SWT.ICON_WARNING | SWT.YES | SWT.NO );
               msgBox.setText(PSMessages.getString(
                  "PSDisplayFormatEditor.warning.invalidFolderColumn.title"));
               msgBox.setMessage(PSMessages.getString(
                  "PSDisplayFormatEditor.warning.invalidFolderColumn", 
                  df.getInvalidFolderFieldNames()));
               int choice = msgBox.open();            
               if (choice == SWT.YES)
               {
                  m_usedWithButtons.setSelection(OTHER_BUTTON);
               }
               else
               {
                  df.removeInvalidFolderColums();
                  m_columnsComp.loadControlValues(m_data);
               }
            }
         }
      }
   }
   
   /**
    * Removes the specified field from the columns container.
    * @param name cannot be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   protected void removeField(String name)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty."); //$NON-NLS-1$
      PSDFColumns columns = ((PSDisplayFormat)m_data).getColumnContainer();
      List<PSDisplayColumn> removeList = new ArrayList<PSDisplayColumn>();
      Iterator<PSDisplayColumn> it = 
         (Iterator<PSDisplayColumn>)columns.iterator();
      while(it.hasNext())
      {
         PSDisplayColumn col = it.next();
         if(col.getSource().equals(name))
            removeList.add(col);
      }
      for(PSDisplayColumn col : removeList)
         columns.remove(col);
   }
   
   /**
    * Adds a column to the columns container that will end up
    * in the columns table. Updates tables.
    * @param displayColumn cannot be <code>null</code>.
    */
   protected void addToColumns(PSDisplayColumn displayColumn)
   {
      if(displayColumn == null)
         throw new IllegalArgumentException("displayColumn cannot be null."); //$NON-NLS-1$
      PSDFColumns columns = ((PSDisplayFormat)m_data).getColumnContainer();
      displayColumn.setGroupingType(PSDisplayColumn.GROUPING_FLAT);
      columns.add(displayColumn);
      m_columnsComp.updateTables();
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpHintKey(com.percussion.workbench.ui.util.PSControlInfo)
    */
   @Override
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      if(m_helpHintKeyHelper == null)
      {
         m_helpHintKeyHelper = new PSHelpHintKeyHelper(new String[]
         {
            PSNameLabelDesc.DESC_TEXT_KEY,
               "description",
            PSNameLabelDesc.LABEL_TEXT_KEY,
               "label",
            "PSDisplayFormatColumsTablesComposite.categories.label.label",
               "",
            "PSDisplayFormatColumsTablesComposite.categories.field.label",
               "",
            "PSDisplayFormatColumsTablesComposite.tableColumn.label.label",
               "",
            "PSDisplayFormatColumsTablesComposite.tableColumn.field.label",
               "",
            "PSDisplayFormatColumsTablesComposite.tableColumn.width.label",
               "",
            "PSDisplayFormatEditor.willBeUsedWith.label",
               "will_be_used_with",
            "PSDisplayFormatEditor.sortColumn.label",
               "sort_column",
            "PSDisplayFormatEditor.sortOrder.ascending.choice",
               "sort_column",
            "PSDisplayFormatEditor.sortOrder.descending.choice",
               "sort_column"   
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;   
   
   // Controls
   private PSNameLabelDesc m_nameLabelDesc;
   protected PSRadioAndCheckBoxes m_usedWithButtons;
   private Button m_descendingButton;
   private Button m_ascendingButton;
   private Combo m_sortColumnCombo;
   protected ComboViewer m_sortColumnComboViewer;
   private Label m_sortColumnLabel;
   private Composite m_sortColumnComp;
   private PSDisplayFormatColumsTablesComposite m_columnsComp;
   
   // Radio button constants
   protected static final int RELATED_BUTTON = 0;
   protected static final int OTHER_BUTTON = 1;
   protected static final int FOLDERS_BUTTON = 2;
   
   /**
    * Holds the last selected value for the used for radio
    * button set.
    */
   private int m_lastSelectedUsedFor = OTHER_BUTTON;
   
   public static final int COL_TYPE_VARIANT = 0;
   public static final int COL_TYPE_CONTENTTYPE = 1;
   public static final int COL_TYPE_TITLE = 2;
   
   
   

}

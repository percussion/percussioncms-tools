/******************************************************************************
*
* [ PSCTypeCopyNamesDialog.java ]
*
* COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import com.percussion.workbench.ui.validators.PSControlValueTextIdValidator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog used to get new names for content types during a copy operation. The
 * dialog presents a 2 column table w/ existing names on the left and new names
 * on the right. All new names must be unique among the new names and all
 * supplied names.
 */
public class PSCTypeCopyNamesDialog extends PSDialog
{

  /**
    * Construct a new dialog
    * 
    * @param parentShell reference to the parent shell, may be <code>null</code>.
    * @param originalNames original content type names. Cannot be
    * <code>null</code>.
    * @param disallowedNames The user will not be allowed to specify a new name
    * that matches any name within this list, case-insensitive. A copy of the
    * supplied list is made.
    */
   public PSCTypeCopyNamesDialog(Shell parentShell, String[] originalNames,
         Collection<String> disallowedNames)
   {
      super(parentShell);
      if(originalNames == null)
         throw new IllegalArgumentException("originalNames cannot be null."); //$NON-NLS-1$
      m_originalNames = originalNames;
      for (String name : disallowedNames)
         m_disallowedNames.add(name.toLowerCase());
   }

   /**
    * Returns a list of all new names, supplying <code>null<code> if
    * no name was specified for the copy.
    * @return array of new names, may be <code>null</code> if ok not
    * pressed.
    */
   public String[] getCopyNames()
   {      
      return m_copyNames;
   }
   
   /* 
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FormLayout());

      m_msgLabel = new Label(container, SWT.WRAP);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, -5);
      formData.top = new FormAttachment(0, 5);
      formData.left = new FormAttachment(0, 5);
      m_msgLabel.setLayoutData(formData);
      m_msgLabel.setText(PSMessages.getString("PSCTypeCopyNamesDialog.message")); //$NON-NLS-1$

      m_table = createTable(container);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(m_msgLabel, -10, SWT.RIGHT);
      formData_1.top = new FormAttachment(m_msgLabel, 10, SWT.BOTTOM);
      formData_1.left = new FormAttachment(m_msgLabel, 10, SWT.LEFT);
      formData_1.height = 100;
      m_table.setLayoutData(formData_1);
      
      //load table rows
      List<PSPair<String, String>> rows = 
         new ArrayList<PSPair<String, String>>(); 
      for(String name : m_originalNames)
      {
         rows.add(new PSPair<String, String>(name, "")); //$NON-NLS-1$
      }
      m_table.setValues(rows);
      return container;
   }   
   
   /* 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   @Override
   protected void okPressed()
   {
      List<String> results = new ArrayList<String>();
      for(PSPair<String, String> row : 
         (List<PSPair<String, String>>)m_table.getValues())
      {
         String value = row.getSecond();
         if (!validateName( row.getFirst(), value, true))
            return;
         if(StringUtils.isBlank(value))
            value = null;
         results.add(value);
      }
      m_copyNames = results.toArray(new String[]{});
      super.okPressed();
   }


   /**
    * Helper method to create the table and all of its supporting
    * classes.
    * @param parent assumed not <code>null</code>.
    * @return the table, never <code>null</code>.
    */
   private PSSortableTable createTable(Composite parent)
   {      
      IPSNewRowObjectProvider objectProvider = new IPSNewRowObjectProvider()
      {

         public Object newInstance()
         {
            return null;
         }

         public boolean isEmpty(@SuppressWarnings("unused") Object obj) //$NON-NLS-1$
         {
            return false;
         }
         
      };
      
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {

         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public String getColumnText(Object element, int columnIndex)
         {
            PSPair<String, String> row = (PSPair)element;
            if(columnIndex == 0)
               return StringUtils.defaultString(row.getFirst());
            return StringUtils.defaultString(row.getSecond());
         }
      
      };
         
      ICellModifier modifier = new ICellModifier()
      {

         public boolean canModify(@SuppressWarnings("unused") Object element, //$NON-NLS-1$
            String property)
         {
            if(m_table.getColumnIndex(property) == 1)
               return true;
            return false;
         }

         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public Object getValue(Object element,
            @SuppressWarnings("unused") String property) //$NON-NLS-1$
         {
            System.out.println("CellEditor:getValue");
            PSPair<String, String> row = (PSPair)element;
            return StringUtils.defaultString(row.getSecond());
         }

         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public void modify(Object element,
            @SuppressWarnings("unused") String property, Object value) //$NON-NLS-1$
         {
            TableItem item = (TableItem)element;               
            PSPair<String, String> row = 
               (PSPair<String, String>)item.getData(); 
            String name = ((String)value).trim();
            if (!row.getSecond().equalsIgnoreCase(name))
               validateName(row.getFirst(), name, false);
            row.setSecond(name);
            m_table.refreshTable();
         }
      
      };
      final PSSortableTable table = new PSSortableTable(parent,
         labelProvider, objectProvider, SWT.NONE,
         PSSortableTable.SURPRESS_CONTEXT_MENU |
         PSSortableTable.SURPRESS_MANUAL_SORT)
      {
         
      };
      table.setCellModifier(modifier);
      table.addColumn("PSCTypeCopyNamesDialog.col.originalName.label", //$NON-NLS-1$
         PSSortableTable.NONE, //$NON-NLS-1$
         new ColumnWeightData(10, 120), null, SWT.LEFT);
      
      final CellEditor ce = new TextCellEditor(table.getTable());
      table.addColumn("PSCTypeCopyNamesDialog.col.copyName.label", //$NON-NLS-1$
      PSSortableTable.NONE, //$NON-NLS-1$
         new ColumnWeightData(10, 120), ce, SWT.LEFT);
      
      return table;
   }
   
   /**
    * Checks if the supplied name is acceptable. If it is not, a warning/error
    * message is displayed to the user. Verifies that the supplied name is
    * either blank, is unique among the disallowed names and all new names other
    * than the one supplied, and valid characters.
    * 
    * @param originalName The name in column 1 of this row. Used to determine
    * which row is being edited.
    * @param name The name to validate. Any value allowed. A blank name is
    * valid.
    * @param useErrorIcon A flag to control whether the icon is a warning or
    * error.
    * 
    * @return <code>true</code> if the name is blank or valid,
    * <code>false</code> otherwise.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private boolean validateName(String originalName, String name, 
         boolean useErrorIcon)
   {
      if (StringUtils.isBlank(name))
         return true;
      Set<String> disallowedNames = new HashSet<String>();
      disallowedNames.addAll(m_disallowedNames);
      //we know the names in column 1 are unique
      for(PSPair<String, String> row : 
         (Collection<PSPair<String, String>>) m_table.getValues())
      {
         if (!originalName.equalsIgnoreCase(row.getFirst()))
            disallowedNames.add(row.getSecond());
      }
      String errorMsg = PSUiUtils.validateObjectName(name,
            disallowedNames, false);
      
      // name is a valid id
      {
         final String msg = getIdValidator().validateId(name,
               PSMessages.getString(
                     "PSCTypeCopyNamesDialog.col.copyName.label"));
         if (msg != null)
         {
            if (errorMsg != null)
            {
               errorMsg += "\n" + msg;
            }
            else
            {
               errorMsg = msg;
            }
         }
      }

      boolean valid = true;
      if (name != null && errorMsg != null)
      {
         valid = false;
         String title = MessageFormat.format(
               PSMessages.getString(
                     "PSCTypeCopyNamesDialog.validation.error.title"),
               name);
         if (useErrorIcon)
         {
            MessageDialog.openError(PSUiUtils.getShell(), title, errorMsg);
         }
         else
         {
            MessageDialog.openWarning(PSUiUtils.getShell(), title, errorMsg);
         }
      }
      return valid;
   }

   /**
    * Convenience method to access
    * {@link PSControlValidatorFactory#getIdValidator()}.
    * @return the id validator. Never <code>null</code>.
    */
   private PSControlValueTextIdValidator getIdValidator()
   {
      return PSControlValidatorFactory.getInstance()
            .getContentTypeNameValidator();
   }
   
   /**
    * Return the initial size of the dialog
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(400, 215);
   }
   
   /* 
    * @see org.eclipse.jface.window.Window#configureShell(
    * org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString("PSCTypeCopyNamesDialog.title")); //$NON-NLS-1$
   }
   
   /**
    * Array of original content type names, initialized in
    * ctor;
    */
   private String[] m_originalNames;
   
   /**
    * Stores the names that are not allowed as new names. Never
    * <code>null</code>, may be empty.
    */
   private Collection<String> m_disallowedNames = new HashSet<String>();
   
   /**
    * Array of content type new names for copying, initialized in
    * {@link #okPressed()};
    */
   private String[] m_copyNames;
   
   // Controls
   private Label m_msgLabel;
   private PSSortableTable m_table;
}

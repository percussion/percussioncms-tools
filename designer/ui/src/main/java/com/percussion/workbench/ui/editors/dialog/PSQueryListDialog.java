/******************************************************************************
*
* [ PSQueryListDialog.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.workbench.ui.PSMessages;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A tiny dialog that is made up of a message and a drop down
 * list to allow a single selection from a list of passed in strings.
 * use {@link #getSelectionIndex()} to get the index of the selected item
 * or use {@link #getSelection()} to get the actual string.
 */
public class PSQueryListDialog extends PSDialog
{
    
   /**
    * Create the dialog
    * @param parentShell
    * @param titleKey the key to the title string that is contained in the
    * psmessages.properties file. Cannot be <code>null</code> or empty.
    * @param msgKey the key to the message string that is contained in the
    * psmessages.properties file. Cannot be <code>null</code> or empty.
    * @param labelKey the key to the label string that is contained in the
    * psmessages.properties file. Cannot be <code>null</code> or empty.
    * @param values the string values for the drop down selection list.
    */
   public PSQueryListDialog(Shell parentShell, String titleKey,
      String msgKey, String labelKey, java.util.List<String> values)
   {
      super(parentShell);
      setBlockOnOpen(true);
      if(StringUtils.isBlank(titleKey))
         throw new IllegalArgumentException(
            "titleKey cannot be null or empty.");
      if(StringUtils.isBlank(labelKey))
         throw new IllegalArgumentException(
            "labelKey cannot be null or empty.");
      if(StringUtils.isBlank(msgKey))
         throw new IllegalArgumentException(
            "msgKey cannot be null or empty.");
      if(values == null)
         throw new IllegalArgumentException("values cannot be null.");
      
      m_titleKey = titleKey;
      m_msgKey = msgKey;
      m_labelKey = labelKey;
      m_listValues = values;
   }
   
   /**
    * @return the index of the selected list item or -1 if
    * no selection.
    */
   public int getSelectionIndex()
   {
      return m_selectedIndex;
   }
   
   /**
    * @return the selected string or <code>null</code> if no
    * selection.
    */
   public String getSelection()
   {
      return m_selectedItem;
   }

   /**
    * Create contents of the dialog
    * @param parent
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FormLayout());

      m_msgLabel = new Label(container, SWT.WRAP);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, -10);
      formData.top = new FormAttachment(0, 10);
      formData.left = new FormAttachment(0, 10);
      m_msgLabel.setLayoutData(formData);
      m_msgLabel.setText(PSMessages.getString(m_msgKey));

      m_listLabel = new Label(container, SWT.WRAP);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(m_msgLabel, 0, SWT.RIGHT);
      formData_1.top = new FormAttachment(m_msgLabel, 10, SWT.DEFAULT);
      formData_1.left = new FormAttachment(m_msgLabel, 0, SWT.LEFT);
      m_listLabel.setLayoutData(formData_1);
      m_listLabel.setText(PSMessages.getString(m_labelKey));

      m_listCombo = new Combo(container, SWT.READ_ONLY);
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(m_listLabel, 0, SWT.RIGHT);
      formData_2.top = new FormAttachment(m_listLabel, 0, SWT.BOTTOM);
      formData_2.left = new FormAttachment(m_listLabel, 0, SWT.LEFT);
      m_listCombo.setLayoutData(formData_2);
      String[] vals = new String[m_listValues.size()];
      m_listValues.toArray(vals);
      m_listCombo.setItems(vals);
      if(m_listCombo.getItemCount() > 0)
         m_listCombo.select(0);
      //
      return container;
   }

   /**
    * Create contents of the button bar
    * @param parent
    */
   @Override
   protected void createButtonsForButtonBar(Composite parent)
   {
      createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
         true);
      createButton(parent, IDialogConstants.CANCEL_ID,
         IDialogConstants.CANCEL_LABEL, false);
   }  
   

   /* 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      m_selectedIndex = m_listCombo.getSelectionIndex();
      m_selectedItem = m_selectedIndex == -1 
         ? null : m_listCombo.getItem(m_selectedIndex);      
      super.okPressed();
   }
   
   /* 
    * @see org.eclipse.jface.window.Window#configureShell(
    * org.eclipse.swt.widgets.Shell)
    */
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString(m_titleKey));
   }

   /**
    * Return the initial size of the dialog
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(340, 195);
   }
   
   /**
    * The title text key, initialized in ctor.
    */
   private String m_titleKey;
   
   /**
    * The message text key, initialized in ctor.
    */
   private String m_msgKey;
   
   /**
    * The label text key, initialized in ctor.
    */
   private String m_labelKey;
   
   /**
    * The list values, initialized in the ctor
    */
   private java.util.List<String> m_listValues;
   
   /**
    * The selected index, set on ok
    */
   private int m_selectedIndex;
   
   /**
    * The selected item, set on ok
    */
   private String m_selectedItem;
   
   // Controls
   private Combo m_listCombo;
   private Label m_listLabel;
   private Label m_msgLabel;
  
}

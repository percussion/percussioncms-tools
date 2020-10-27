/*******************************************************************************
 *
 * [ PSTextInputDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.workbench.ui.security;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple text input dialog box currently specific to user name input but can
 * be used for other purposes. Does not do any validadtion but provides a method
 * that a derived class can override to validate the unput.
 */
public class PSTextInputDialog extends Dialog implements IPSUiConstants
{
   /**
    * @param parentShell
    * @param dlgTitle
    */
   protected PSTextInputDialog(Shell parentShell, String dlgTitle)
   {
      super(parentShell);
      if (dlgTitle == null || dlgTitle.length() == 0)
      {
         throw new IllegalArgumentException(
            "dlgTitle must not be null or empty");
      }
      m_title = dlgTitle;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite mainComp = new Composite(parent, SWT.NONE);
      mainComp.setLayout(new FormLayout());
      FormData fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(0, 0);
      mainComp.setLayoutData(fd);

      Label label = new Label(mainComp, SWT.RIGHT);
      final FormData formData_A = new FormData();
      formData_A.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData_A.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      formData_A.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      label.setLayoutData(formData_A);
      label.setText(PSMessages.getString("PSTextInputDialog.label.userName")); //$NON-NLS-1$

      m_text = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
      final FormData formData_B = new FormData();
      formData_B.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData_B.top = new FormAttachment(label,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_B.left = new FormAttachment(label, LABEL_HSPACE_OFFSET,
         SWT.RIGHT);
      m_text.setLayoutData(formData_B);

      GridData data = new GridData(GridData.FILL_HORIZONTAL
         | GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
      mainComp.setLayoutData(data);
      return mainComp;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(m_title);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      if (isInputValid(m_text.getText()))
         return;
      m_value = m_text.getText();
      super.okPressed();
   }

   /**
    * Default implementation of validating the name. No validation is done here.
    * Derived class must implement to validate.
    * 
    * @param text text that was entered in the control, not <code>null</code>
    * may be empty.
    * @return <code>true</code> if validation succeeds <code>false</code>
    * otherwise.
    */
   protected boolean isInputValid(@SuppressWarnings("unused")
   String text)
   {
      return true;
   }

   /**
    * Get the text value entered by the user. Assumes this is called after
    * pressing OK button.
    * 
    * @return value entered by the user, may be empy but not <code>null</code>.
    */
   public String getTextValue()
   {
      return m_value;
   }

   /**
    * Title for the dialog box initialized in the ctor, never <code>null</code>.
    */
   private String m_title;

   /**
    * Text control
    */
   protected Text m_text = null;

   /**
    * @see #getTextValue()
    */
   protected String m_value = null;

}

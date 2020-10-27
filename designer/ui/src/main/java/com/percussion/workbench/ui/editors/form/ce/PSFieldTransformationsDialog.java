/******************************************************************************
 *
 * [ PSFieldTransformationsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Dialog for creating or updating filed input and output transformations
 */
public class PSFieldTransformationsDialog extends PSDialog implements IPSUiConstants
{

   /**
    * Ctor
    * @param parentShell The parent shell, must not be <code>null</code>.
    * @param rowData The PSFieldTableRowDataObject representing the field row
    * in the content editor. Must not be <code>null</code>.
    */
   public PSFieldTransformationsDialog(Shell parentShell,
         PSFieldTableRowDataObject rowData) {
      super(parentShell);
      if (parentShell == null)
      {
         throw new IllegalArgumentException("parentShell must not be null"); //$NON-NLS-1$
      }
      if (rowData == null)
      {
         throw new IllegalArgumentException("fieldData must not be null"); //$NON-NLS-1$
      }
      m_rowData = rowData;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {

      Composite m_transformComp = new Composite(parent, SWT.NONE);
      m_transformComp.setLayout(new FormLayout());
      
      m_mainTabFolder = new TabFolder(m_transformComp, SWT.TOP);
      m_mainTabFolder.addSelectionListener(new SelectionAdapter()
         {

            /* @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               if(m_inputTransformsComp != null)
                  m_inputTransformsComp.getFieldRulesComposite().checkForRuleChanges();
               if(m_outputTransformsComp != null)
                  m_outputTransformsComp.getFieldRulesComposite().checkForRuleChanges();
            }
            
         });
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.bottom = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      m_mainTabFolder.setLayoutData(formData);

      m_inputTransformsTab = new TabItem(m_mainTabFolder, SWT.NONE);
      m_inputTransformsTab.setText(PSMessages.getString("PSFieldTransformationsDialog.tab.title.input")); //$NON-NLS-1$
      m_inputTransformsComp = new PSTransformationRulesTab(m_mainTabFolder,
            SWT.NONE, m_rowData, true);
      m_inputTransformsTab.setControl(m_inputTransformsComp);

      m_outputTransformsTab = new TabItem(m_mainTabFolder, SWT.NONE);
      m_outputTransformsTab.setText(PSMessages.getString("PSFieldTransformationsDialog.tab.title.output")); //$NON-NLS-1$
      m_outputTransformsComp = new PSTransformationRulesTab(m_mainTabFolder,
            SWT.NONE, m_rowData, false);
      m_outputTransformsTab.setControl(m_outputTransformsComp);

      GridData data = new GridData(GridData.FILL_HORIZONTAL
            | GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
      m_transformComp.setLayoutData(data);
      return m_transformComp;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
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
      m_inputTransformsComp.getFieldRulesComposite().checkForRuleChanges();
      m_outputTransformsComp.getFieldRulesComposite().checkForRuleChanges();
      if(!(m_inputTransformsComp.updateData() && m_outputTransformsComp.updateData()))
         return;
      super.okPressed();
   }

   /*
    * @see org.eclipse.jface.window.Window
    *      #configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString("PSFieldTransformationsDialog.dialog.title.fieldtransform")); //$NON-NLS-1$
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.window.Window#getInitialSize()
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(600, 600);
   }

   //Controls
   private PSFieldTableRowDataObject m_rowData;
   private TabFolder m_mainTabFolder;
   private TabItem m_inputTransformsTab;
   private PSTransformationRulesTab m_inputTransformsComp;
   private PSTransformationRulesTab m_outputTransformsComp;
   private TabItem m_outputTransformsTab;

}

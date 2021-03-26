/******************************************************************************
 *
 * [ PSAddDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.dialogs;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog that provides a user interface to add/remove items from the list
 * of currently selected items.
 */
public class PSAddDialog extends PSDialog
   implements IPSUiConstants
{  
   
   /**
    * Create the dialog
    * @param parentShell the parent shell
    * @param title the title for this dialog, never <code>null</code>, may be
    * empty.
    * @param availLabel the label of the available items column, never
    * <code>null</code>, may be empty. 
    * @param selectLabel the label of the selected items column, never
    * <code>null</code>, may be empty.
    * @param availItems the list of available items, not <code>null</code>,
    * may be empty.
    * @param selectItems the list of selected items, not <code>null</code>,
    * may be empty.
    * @param mode string that indicates what this dialog is used for so
    * that the correct help can be launched. Cannot be <code>null<code> or
    * empty.
    */
   public PSAddDialog(Shell parentShell, String title, String availLabel, String selectLabel,
         java.util.List availItems, java.util.List selectItems, String mode)
   {
      super(parentShell);
      setBlockOnOpen(true);
      if(title == null)
         throw new IllegalArgumentException("title cannot be null."); //$NON-NLS-1$
      if(availLabel == null)
         throw new IllegalArgumentException("availLabel cannot be null."); //$NON-NLS-1$
      if(selectLabel == null)
         throw new IllegalArgumentException("selectLabel cannot be null."); //$NON-NLS-1$     
      if(availItems == null)
         throw new IllegalArgumentException("availItems cannot be null."); //$NON-NLS-1$
      if(selectItems == null)
         throw new IllegalArgumentException("selectItems cannot be null."); //$NON-NLS-1$
      if(StringUtils.isBlank(mode))
         throw new IllegalArgumentException("mode cannot be null or empty."); //$NON-NLS-1$
            
      m_title = title;
      m_availLabel = availLabel;
      m_selectLabel = selectLabel;
      m_availItems = availItems;
      m_selectItems = selectItems;
      m_mode = mode;
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

      m_slush = new PSSlushBucket(container, SWT.NONE, m_availLabel,
            m_selectLabel, m_availItems, m_selectItems,
            new PSReferenceLabelProvider());
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      
      final Label filterLabel = new Label(container, SWT.NONE);
      filterLabel.setText(
            PSMessages.getString("common.label.filter") + ':'); //$NON-NLS-1$
      {
         final FormData formData_1 = new FormData();      
         formData_1.left = new FormAttachment(m_slush, 0, SWT.LEFT);
         formData_1.bottom = new FormAttachment(100, 0);
         filterLabel.setLayoutData(formData_1);
      }
      
      formData.bottom = 
         new FormAttachment(filterLabel,
               -LABEL_HSPACE_OFFSET - LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,
               SWT.TOP);
         
      m_slush.setLayoutData(formData);
      m_slush.setValues(m_availItems, m_selectItems);
      
      final Text text = new Text(container, SWT.BORDER);
      {
         final FormData formData_2 = new FormData();      
         formData_2.right = new FormAttachment(ONE_THIRD, 0);
         formData_2.top = new FormAttachment(filterLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         formData_2.left = new FormAttachment(filterLabel, LABEL_HSPACE_OFFSET, SWT.DEFAULT);
         text.setLayoutData(formData_2);
      }
      
      text.addModifyListener(new ModifyListener() {
         @SuppressWarnings("synthetic-access")
         public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
         {
            m_slush.filterAvailableList(text.getText());
         }
      });
           
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
      
   /**
    * Return the initial size of the dialog
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(500, 500);
   }
   
   /* 
    * @see org.eclipse.jface.window.Window#configureShell(
    * org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(m_title); //$NON-NLS-1$
   }   
   
   /* 
    * @see com.percussion.workbench.ui.editors.dialog.PSDialog#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {      
      return super.getHelpKey(control) + "_" + m_mode;
   }

   /**
    * Returns the current selections of this dialog
    * 
    * @return a list of the currently selected items, never <code>null</code>,
    * may be empty.
    */
   public java.util.List getSelections()
   {
      return m_slush.getSelections();
   }
   
   //Controls
   private PSSlushBucket m_slush;
      
   /**
    * The title of this dialog.  Never, <code>null</code>, may be empty.
    */
   private String m_title = "";
   
   /**
    * Available items column label. Initialized in ctor, never <code>
    * null</code> after that.
    */  
   private String m_availLabel;
   
   /**
    * Selected items column label. Initialized in ctor, never <code>
    * null</code> after that.
    */  
   private String m_selectLabel;
   
   /**
    * Available items list. Initialized in ctor, never <code>
    * null</code> after that.
    */  
   private java.util.List m_availItems;
   
   /**
    * Selected items list. Initialized in ctor, never <code>
    * null</code> after that.
    */  
   private java.util.List m_selectItems;
   
   /**
    * String that indicates what mode this dialog is used for
    * so that the correct help can be displayed. Initialized in ctor,
    * never <code>null</code> after that.
    */
   private String m_mode;
   
     

}

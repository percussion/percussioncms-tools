/******************************************************************************
 *
 * [ PSConvertTemplatesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.handlers.PSContentTypeAssociateTemplateNodeHandler;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;

/**
 * Dialog that asks user how to handle the moving of local
 * templates to a target content type.
 */
public class PSConvertTemplatesDialog extends Dialog
{
   /**
    * Create the dialog
    * @param parentShell
    * @param names A list of the names applicable to this operation. They will
    * be placed in the message to the user. Never <code>null</code> or empty.
    */
   public PSConvertTemplatesDialog(Shell parentShell, String names)
   {
      super(parentShell);
      if (StringUtils.isBlank(names))
      {
         throw new IllegalArgumentException("names cannot be null or empty");  
      }
      setShellStyle(getShellStyle() | SWT.RESIZE);
      setBlockOnOpen(true);
      m_names = names;
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
      formData.top = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      // to make dialog resize itself correctly vertically with wrap on Windows
      formData.width = getShell().getDisplay().getBounds().width / 2;
      m_msgLabel.setLayoutData(formData);
      m_msgLabel.setText(PSMessages.getString(
            "PSConvertTemplatesDialog.message", new Object[] {m_names})); //$NON-NLS-1$

      m_moveButton = new PSWrappedTextRadioButton(container);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(m_msgLabel, 20, SWT.BOTTOM);
      formData_1.left = new FormAttachment(0, 50);
      formData_1.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      m_moveButton.setLayoutData(formData_1);
      m_moveButton.setText(PSMessages.getString(
            "PSConvertTemplatesDialog.move.choice")); //$NON-NLS-1$

      m_convertButton = new PSWrappedTextRadioButton(container);
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(m_moveButton, 10, SWT.BOTTOM);
      formData_2.left = new FormAttachment(0, 50);
      formData_2.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);

      m_convertButton.setLayoutData(formData_2);
      m_convertButton.setText(PSMessages.getString(
            "PSConvertTemplatesDialog.convert.choice")); //$NON-NLS-1$

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
    * Returns the choice selected by the user. Will be CANCEL if
    * the cancel button was pressed.
    * @return Never <code>null</code>.
    */
   public PSContentTypeAssociateTemplateNodeHandler.LocalTemplateProcessChoice
      getValue()
   {
      return m_choice;
   }   

   /* 
    * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
    */
   @Override
   protected void cancelPressed()
   {
      m_choice = PSContentTypeAssociateTemplateNodeHandler.
         LocalTemplateProcessChoice.CANCEL;
      super.cancelPressed();
   }

   /* 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      if(m_moveButton.getButton().getSelection())
      {
         m_choice = PSContentTypeAssociateTemplateNodeHandler.
         LocalTemplateProcessChoice.MOVE;
      }
      else
      {
         m_choice = PSContentTypeAssociateTemplateNodeHandler.
         LocalTemplateProcessChoice.CONVERT;
      }
      super.okPressed();
   }

   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString("PSConvertTemplatesDialog.title")); //$NON-NLS-1$
   }
   
   public class PSWrappedTextRadioButton extends Composite
   {

      public PSWrappedTextRadioButton(Composite parent)
      {
         super(parent, SWT.NONE);
         setLayout(new FormLayout());
         init();
      }
      
      private void init()
      {
         mi_button = new Button(this, SWT.RADIO);
         final FormData formdataButton = new FormData();
         formdataButton.top = new FormAttachment(0, 0);
         formdataButton.left = new FormAttachment(0, 0);
         mi_button.setLayoutData(formdataButton);
         mi_button.addSelectionListener(new SelectionAdapter()
            {
               @SuppressWarnings("synthetic-access")
               @Override
               public void widgetSelected(
                     @SuppressWarnings("unused") SelectionEvent e)
               {
                  if(mi_button.getSelection())
                     handleSelection();
               }
               
            });

         mi_label = new Label(this, SWT.WRAP);
         final FormData formdataLabel = new FormData();
         formdataLabel.top = new FormAttachment(0, 0);
         formdataLabel.left = new FormAttachment(mi_button, 5, SWT.RIGHT);
         formdataLabel.right = new FormAttachment(100, 0);
         mi_label.setLayoutData(formdataLabel);
         mi_label.addMouseListener(new MouseAdapter()
            {
               /* 
                * @see org.eclipse.swt.events.MouseAdapter#mouseDown(
                * org.eclipse.swt.events.MouseEvent)
                */
               @SuppressWarnings("synthetic-access") //$NON-NLS-1$
               @Override
               public void mouseDown(@SuppressWarnings("unused") MouseEvent e)
               {
                  mi_button.setSelection(true);
                  handleSelection();
               }
            });
      }
      
      public void setText(String text)
      {
         mi_label.setText(text);
      }
      
      @Override
      public void setToolTipText(String text)
      {
        mi_label.setToolTipText(text);
        mi_button.setToolTipText(text);
      }      
      
      
      public Button getButton()
      {
         return mi_button;
      } 
      
      private void handleSelection()
      {
         Composite parent = this.getParent();
         Control[] children = parent.getChildren();
         for(Control child : children)
         {
            if (child == this)
               continue;
            if(child instanceof Button || child instanceof PSWrappedTextRadioButton)
            {
               Button temp = null;
               if(child instanceof Button)
               {
                  temp = (Button)child;
                  if((temp.getStyle() & SWT.RADIO) == 0)
                     return;
                  temp.setSelection(false);
               }
               else
               {
                  ((PSWrappedTextRadioButton)child).getButton().setSelection(false);
               }
               
            }
         }
      }
      
      private Button mi_button;
      private Label mi_label;
   }
   
   private PSWrappedTextRadioButton m_convertButton;
   private PSWrappedTextRadioButton m_moveButton;
   private Label m_msgLabel;
   private PSContentTypeAssociateTemplateNodeHandler.
      LocalTemplateProcessChoice m_choice;
   
   /**
    * See ctor for description. Set in ctor, then never <code>null</code>, 
    * empty or modified.
    */
   private String m_names;
}

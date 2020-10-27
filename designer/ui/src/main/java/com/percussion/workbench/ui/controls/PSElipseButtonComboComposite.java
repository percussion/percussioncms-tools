/******************************************************************************
 *
 * [ PSElipseButtonComboComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.workbench.ui.IPSUiConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * A composite that is comprised of a <code>ComboViewer</code> and
 * an elipse <code>Button</code> on the right side of the combo.
 */
public class PSElipseButtonComboComposite extends Composite 
   implements IPSUiConstants
{

   public PSElipseButtonComboComposite(Composite parent, int style)
   {
      super(parent, SWT.NONE);
      setLayout(new FormLayout());
      int comboStyle = 
         ((style & SWT.READ_ONLY) != 0) ? SWT.READ_ONLY : SWT.BORDER;
      m_comboViewer = new ComboViewer(this, comboStyle);
      m_button = new Button(this, SWT.NONE);
      
      final FormData formData_1 = new FormData();
      formData_1.height = 21;
      formData_1.width = 21;
      formData_1.right = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(0, 0);
      m_button.setLayoutData(formData_1);
      m_button.setText("...");
      
      
      final Combo combo = m_comboViewer.getCombo();
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      formData.right = 
         new FormAttachment(m_button, -BUTTON_HSPACE_OFFSET, SWT.LEFT);
      combo.setLayoutData(formData);           
      //
   }   

   /**
    * @return Returns the button.
    */
   public Button getButton()
   {
      return m_button;
   }

   /**
    * @return Returns the comboViewer.
    */
   public ComboViewer getComboViewer()
   {
      return m_comboViewer;
   }

   public void dispose()
   {
      super.dispose();
   }

   protected void checkSubclass()
   {
   }
   
   private Button m_button;
   private ComboViewer m_comboViewer;

}

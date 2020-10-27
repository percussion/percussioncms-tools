/******************************************************************************
*
* [ PSButtonCellEditor.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.swtdesigner.SWTResourceManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PSButtonCellEditor extends CellEditor
{


   public PSButtonCellEditor(Composite parent, int style) {
      super(parent, style);
      //
   }

   public void dispose()
   {
      super.dispose();
   }

   protected void checkSubclass()
   {
   }

   @Override
   protected Control createControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());

      m_button = new Button(comp, SWT.NONE);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(0, 0);
      formData_1.left = new FormAttachment(0,0);
      formData_1.right = new FormAttachment(100,0);
      formData_1.bottom = new FormAttachment(100,0);
      m_button.setLayoutData(formData_1);
      m_button.setText("C");
      return comp;
   }
   
   public void setButtonFont(boolean bold)
   {
      //Create a dummy button
      Button btn = new Button((Composite)this.getControl(), SWT.NONE);
      if(bold)
         m_button.setFont(SWTResourceManager.getBoldFont(btn.getFont()));
      else
         m_button.setFont(btn.getFont());
      //dispose the dummy button
      btn.dispose();
   }
   public Button getButton()
   {
      return m_button;
   }
   @Override
   protected Object doGetValue()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected void doSetFocus()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void doSetValue(Object value)
   {
      // TODO Auto-generated method stub
      
   }
   private Button m_button;

}

/******************************************************************************
 *
 * [ ExampleMultiPageEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValueRequiredValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ExampleMultiPageEditor extends PSMultiPageEditorBase
{
   public ExampleMultiPageEditor()
   {
      super();
   }

   @Override
   protected void createPages()
   {
      createTestPage1();
      createTestPage2();
   }
   
   protected void createTestPage1()
   {
      Composite comp = new Composite(getContainer(), SWT.NONE);
      comp.setLayout(new FormLayout());
      int index = addPage(comp);
      IPSControlValueValidator required = new PSControlValueRequiredValidator();
      
      final Label nameLabel = new Label(comp, SWT.NONE);
      nameLabel.setAlignment(SWT.RIGHT);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(25, 0);
      formData.top = new FormAttachment(0, 10);
      formData.left = new FormAttachment(0, 0);
      nameLabel.setLayoutData(formData);
      nameLabel.setText("Name");

      m_Text = new Text(comp, SWT.BORDER);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(80, 0);
      formData_1.top = new FormAttachment(nameLabel, 0, SWT.TOP);
      formData_1.left = new FormAttachment(nameLabel, 5, SWT.RIGHT);
      m_Text.setLayoutData(formData_1);
      registerControl(nameLabel.getText(), m_Text, 
         new IPSControlValueValidator[]{required}, 1);
      setPageText(index, "Page 1");
      setPageTitle(index, "Editing Page 1");
   }
   
   protected void createTestPage2()
   {
      
      Composite comp = new Composite(getContainer(), SWT.NONE);
      comp.setLayout(new FormLayout());
      int index = addPage(comp);
      IPSControlValueValidator required = new PSControlValueRequiredValidator();
      final Label nameLabel = new Label(comp, SWT.NONE);
      nameLabel.setAlignment(SWT.RIGHT);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(25, 0);
      formData.top = new FormAttachment(0, 15);
      formData.left = new FormAttachment(0, 5);
      nameLabel.setLayoutData(formData);
      nameLabel.setText("Foobar");

      m_Text2 = new Text(comp, SWT.BORDER);
      final FormData formData_2 = new FormData();
      formData_2.left = new FormAttachment(0, 135);
      formData_2.bottom = new FormAttachment(0, 35);
      formData_2.right = new FormAttachment(0, 211);
      m_Text2.setLayoutData(formData_2);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(80, 0);
      formData_1.top = new FormAttachment(nameLabel, 0, SWT.TOP);
      formData_1.left = new FormAttachment(nameLabel, 5, SWT.RIGHT);
      m_Text2.setLayoutData(formData_1);
      registerControl(nameLabel.getText(), m_Text2, 
         new IPSControlValueValidator[]{required}, 2);
      
      setPageText(index, "Page 2");
      setPageTitle(index, "Editing Page 2");
   }

   @Override
   public boolean isValidReference(@SuppressWarnings("unused") IPSReference ref)
   {
      return true;
   }
   
   public void loadControlValues(@SuppressWarnings("unused") Object designObject)
   {
   }

  
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(
         @SuppressWarnings("unused") Object designObject,
         @SuppressWarnings("unused") Object control)
   {
   }
   
   private Text m_Text;
   private Text m_Text2;
}

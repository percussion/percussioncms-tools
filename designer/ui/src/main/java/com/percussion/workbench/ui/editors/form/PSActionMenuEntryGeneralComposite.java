/******************************************************************************
 *
 * [ PSActionMenuEntryGeneralComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSActionMenuGeneralCommonComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class PSActionMenuEntryGeneralComposite extends Composite
   implements IPSDesignerObjectUpdater
{
   
   /**
    * Create the composite
    * @param parent
    * @param style
    */
   public PSActionMenuEntryGeneralComposite(
      Composite parent, int style, PSEditorBase editor, int page)
   {
      super(parent, style);
      
      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null."); //$NON-NLS-1$
      setLayout(new FormLayout());
      
      m_editor = editor;
      
     //This composite takes care of registering its own controls
      m_commonComp = new PSNameLabelDesc(this, SWT.NONE,
         PSMessages.getString("PSActionMenuEntryGeneralComposite.menuEntry.label"), //$NON-NLS-1$
         -1, 
         PSNameLabelDesc.SHOW_ALL |
         PSNameLabelDesc.LAYOUT_SIDE |
         PSNameLabelDesc.NAME_READ_ONLY |
         PSNameLabelDesc.LABEL_USES_NAME_PREFIX, m_editor);
      final FormData formData = new FormData();      
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      m_commonComp.setLayoutData(formData);       
           
      // This composite takes care of registering its own controls
      m_accelComp = 
         new PSActionMenuGeneralCommonComposite(this, SWT.NONE, editor, page);
      final FormData formData_2 = new FormData();      
      formData_2.right = new FormAttachment(100, 0);
      formData_2.top = new FormAttachment(m_commonComp, 30, SWT.BOTTOM);
      formData_2.left = new FormAttachment(0, 0);
      m_accelComp.setLayoutData(formData_2);
      
     // This composite takes care of registering its own controls
      m_optionsComp = 
         new PSActionMenuEntryOptionsComposite(this, SWT.NONE, editor, page);
      final FormData formData_3 = new FormData();      
      formData_3.right = new FormAttachment(100, 0);
      formData_3.top = new FormAttachment(m_accelComp, 30, SWT.BOTTOM);
      formData_3.left = new FormAttachment(0, 0);
      m_optionsComp.setLayoutData(formData_3);
      
      //
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSAction action = (PSAction)designObject;
      
      if(control == m_commonComp.getLabelText())
      {
         action.setLabel(m_commonComp.getLabelText().getText());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         action.setDescription(m_commonComp.getDescriptionText().getText());
      }      
      else
      {
         m_accelComp.updateDesignerObject(designObject, control);
         m_optionsComp.updateDesignerObject(designObject, control);
      }
      
   }
   
   /**
    * Loads the intial control values.
    */
   public void loadControlValues(Object designObject)
   {
      PSAction action = (PSAction)designObject;
      
      // set name
      ((Label)m_commonComp.getNameText()).setText(action.getName());
      // set label
      m_commonComp.getLabelText().setText(action.getLabel());
      // set description
      m_commonComp.getDescriptionText().setText(action.getDescription());
      // set values in accelComp
      m_accelComp.loadControlValues(designObject);
      // set values in options composite
      m_optionsComp.loadControlValues(designObject);
   }
   
   //Controls
   private PSNameLabelDesc m_commonComp;   
   private PSActionMenuGeneralCommonComposite m_accelComp;
   private PSActionMenuEntryOptionsComposite m_optionsComp;
   
   @SuppressWarnings("unused") //$NON-NLS-1$
   private PSEditorBase m_editor;

}

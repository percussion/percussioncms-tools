/******************************************************************************
*
* [ PSSystemDefEditor.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.editors.form.ce.PSCEFieldsCommonComposite;
import com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class PSSystemDefEditor extends PSEditorBase
{
   //See base class
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if(ref == null)
         return false; // Should never happen
      if(ref.getObjectType().getPrimaryType() == PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG)
         return true;
      return false;
   }

   //See base class
   @Override
   public void createControl(Composite comp)
   {
      Composite  mainComp = new Composite(comp, SWT.NONE);
      mainComp.setLayout(new FormLayout());
      
      m_fieldsComp = new PSCEFieldsCommonComposite(mainComp, SWT.NONE,this,
            "Fields", PSContentEditorDefinition.SYSTEMDEF_EDITOR,"System Fields");
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(0, 0);
      formData_10.left = new FormAttachment(15,0);
      formData_10.bottom = new FormAttachment(100, 0);
      formData_10.right = new FormAttachment(90, 0);
      m_fieldsComp.setLayoutData(formData_10);

   }
   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      m_fieldsComp.updateDesignerObject(designObject,control);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      m_fieldsComp.loadControlValues(designObject);
   }

   /**
    * Fields common composite control
    */
   private PSCEFieldsCommonComposite m_fieldsComp;

}

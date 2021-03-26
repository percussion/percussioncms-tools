/******************************************************************************
 *
 * [ PSSharedDefFieldSetTab.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class PSSharedDefFieldSetTab extends Composite
      implements
         IPSDesignerObjectUpdater
{

   /**
    * CTor
    * @param parent may be <code>null</code>.
    * @param style the style options
    * @param editor cannot be <code>null</code>.
    * @param fieldSetName cannot be <code>null</code> or empty.
    */
   public PSSharedDefFieldSetTab(Composite parent, int style,
         PSEditorBase editor, String fieldSetName) {
      super(parent, style);
      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null.");
      if(StringUtils.isBlank(fieldSetName))
         throw new IllegalArgumentException("fieldSetName cannot be null or empty.");
      setLayout(new FormLayout());
      m_fieldsComp = new PSCEFieldsCommonComposite(this, SWT.NONE, editor,
            "Fields", PSContentEditorDefinition.SHAREDDEF_EDITOR,
            fieldSetName);
      m_name = fieldSetName;
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(0, 0);
      formData_10.left = new FormAttachment(15, 0);
      formData_10.bottom = new FormAttachment(100, 0);
      formData_10.right = new FormAttachment(90, 0);
      m_fieldsComp.setLayoutData(formData_10);
   }

   public void updateDesignerObject(Object designObject, Object control)
   {
      m_fieldsComp.updateDesignerObject(designObject, control);
   }

   public void loadControlValues(Object designObject)
   {
      m_fieldsComp.loadControlValues(designObject);
   }
   
   /**
    * @return the name of the field set that this tab represents.
    */
   public String getFieldSetName()
   {
      return m_name;
   }

   /**
    * Fields common composite control
    */
   private PSCEFieldsCommonComposite m_fieldsComp;
   
   /**
    * Name of the field set. Initialized in ctor. Never <code>null</code>
    * or empty after that.
    */
   private String m_name;

}

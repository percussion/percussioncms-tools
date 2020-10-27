/******************************************************************************
 *
 * [ PSKeywordPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.services.content.data.PSKeyword;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:54 PM
 */
public class PSKeywordPropertiesPage extends PSWizardPageBase 
   implements IPSUiConstants
{

   public PSKeywordPropertiesPage()
   {
      super(PSMessages.getString("PSKeywordPropertiesPage.pagename"), //$NON-NLS-1$
         PSMessages.getString("PSKeywordPropertiesPage.title"), null); //$NON-NLS-1$   }
   }
   
   /* 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(
    * org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {
      final Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      
       m_commonComp = new PSNameLabelDesc(comp, SWT.NONE,
          PSMessages.getString("PSKeywordPropertiesPage.namePrefix"),  //$NON-NLS-1$
         WIZARD_LABEL_NUMERATOR,
         PSNameLabelDesc.SHOW_NAME | PSNameLabelDesc.SHOW_DESC, this);
      final FormData formData_14 = new FormData();
      formData_14.left = new FormAttachment(0, 0);
      formData_14.right = 
         new FormAttachment(WIZARD_VALUE_NUMERATOR, -COMMON_BORDER_OFFSET);
      formData_14.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      m_commonComp.setLayoutData(formData_14);
      m_commonComp.getDescriptionText().setTextLimit(255);
      
      
      setControl(comp);
   }
  
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSKeyword keyword = (PSKeyword)designObject;
      if(control == m_commonComp.getNameText())
      {
         keyword.setLabel(((Text)m_commonComp.getNameText()).getText());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         keyword.setDescription(m_commonComp.getDescriptionText().getText());
      }
      
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpHintKey(com.percussion.workbench.ui.util.PSControlInfo)
    */
   @Override
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      if(m_helpHintKeyHelper == null)
      {
         m_helpHintKeyHelper = new PSHelpHintKeyHelper(new String[]
         {
            PSNameLabelDesc.DESC_TEXT_KEY,
               "description",
            PSNameLabelDesc.NAME_TEXT_KEY,
               "keyword_name"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
   
   private PSNameLabelDesc m_commonComp;
}

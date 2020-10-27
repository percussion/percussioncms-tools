/******************************************************************************
*
* [ PSItemFilterPropertiesPage.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.services.filter.data.PSItemFilter;
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

/**
 * Wizard page to create Item Filters.
 */
public class PSItemFilterPropertiesPage extends PSWizardPageBase implements IPSUiConstants
{

   /**
    * Constructor to create a new item filter proeprty page.
    */
   public PSItemFilterPropertiesPage() 
   {
      super(PSMessages.getString("PSItemFilterPropertiesPage.label.wizard.title"), PSMessages.getString("PSItemFilterPropertiesPage.label.wizard.message"), null); //$NON-NLS-1$ //$NON-NLS-2$
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {
      final Composite mainComp = new Composite(parent, SWT.NONE);
      mainComp.setLayout(new FormLayout());
      final FormData fd = new FormData();
      fd.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      fd.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      fd.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      mainComp.setLayoutData(fd);

      m_nameLabelComp = new PSNameLabelDesc(mainComp, SWT.NONE,
         PSMessages.getString("PSItemFilterPropertiesPage.label.filter.name"),  //$NON-NLS-1$
            WIZARD_LABEL_NUMERATOR,
            PSNameLabelDesc.SHOW_NAME | PSNameLabelDesc.SHOW_DESC, this);

      final FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(100, 0);
      fd1.top = new FormAttachment(0, 0);
      m_nameLabelComp.setLayoutData(fd1);
      
      setControl(mainComp);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSItemFilter filter = (PSItemFilter) designObject;
      if(control == m_nameLabelComp.getDescriptionText())
      {
         filter.setDescription(m_nameLabelComp.getDescriptionText().getText());
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
               "filter_name"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   /*
    * Controls for this wizard page
    */
   private PSNameLabelDesc m_nameLabelComp;
}

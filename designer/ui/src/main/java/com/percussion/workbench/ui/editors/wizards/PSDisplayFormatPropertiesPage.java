/******************************************************************************
 *
 * [ PSDisplayFormatPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSSelectCommunitiesComposite;
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
 * @created 03-Sep-2005 4:43:50 PM
 */
public class PSDisplayFormatPropertiesPage extends PSWizardPageBase
implements IPSUiConstants
{

   public PSDisplayFormatPropertiesPage()
   {
      super(PSMessages.getString("PSDisplayFormatPropertiesPage.title"), PSMessages.getString("PSDisplayFormatPropertiesPage.subtitle"), null); //$NON-NLS-1$ //$NON-NLS-2$
   }

   
   /* 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(
    * org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      
      m_commonComp = new PSNameLabelDesc(comp,SWT.NONE, PSMessages.getString("PSDisplayFormatPropertiesPage.name"), //$NON-NLS-1$
         WIZARD_LABEL_NUMERATOR, 
         PSNameLabelDesc.SHOW_ALL, this);
      final FormData formdata_1 = new FormData();
      formdata_1.top = new FormAttachment(0, 0);
      formdata_1.left = new FormAttachment(0, 0);
      formdata_1.right = new FormAttachment(100, 0);
      m_commonComp.setLayoutData(formdata_1);
      
      m_communityComp = new PSSelectCommunitiesComposite(comp, SWT.NONE);
      final FormData formdata_2 = new FormData();
      formdata_2.top = new FormAttachment(m_commonComp, 10, SWT.BOTTOM);
      formdata_2.left = new FormAttachment(0, 0);
      formdata_2.right = new FormAttachment(100, 0);
      m_communityComp.setLayoutData(formdata_2);
      registerControl(
         "PSSelectCommunitiesComposite.label.visible.communities",
         m_communityComp.getSlushControl(),
         null,
         PSControlInfo.TYPE_COMMUNITY);
      registerControlHelpOnly("common.label.filter",
         m_communityComp.getFilterTextControl());
      
      setControl(comp);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSDisplayFormat displayFormat = (PSDisplayFormat)designObject;
      if(control == m_commonComp.getNameText())
      {
         displayFormat.setInternalName(
            ((Text)m_commonComp.getNameText()).getText());
      }
      else if(control == m_commonComp.getLabelText())
      {
         displayFormat.setDisplayName(m_commonComp.getLabelText().getText());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         displayFormat.setDescription(
            m_commonComp.getDescriptionText().getText());
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
            PSNameLabelDesc.LABEL_TEXT_KEY,
               "label",
            PSNameLabelDesc.NAME_TEXT_KEY,
               "display_format_name",
            "PSSelectCommunitiesComposite.label.visible.communities",
               "visible_to_these_communities",
            "common.label.filter",
               "filter"
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
   private PSSelectCommunitiesComposite m_communityComp;

}

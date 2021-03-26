/******************************************************************************
 *
 * [ PSMenuActionEntryPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSActionMenuWizardCommonComposite;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:56 PM
 */
public class PSActionMenuEntryPropertiesPage extends PSWizardPageBase
{

   public PSActionMenuEntryPropertiesPage()
   {
     super(PSMessages.getString(
        "PSActionMenuEntryPropertiesPage.pagename"),  //$NON-NLS-1$
        PSMessages.getString(
           "PSActionMenuEntryPropertiesPage.title"), null);  //$NON-NLS-1$
   }

  
   /* 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(
    * org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {
       m_comp = new PSActionMenuWizardCommonComposite(parent, this, true);
       setControl((Control)m_comp);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      m_comp.updateDesignerObject(designObject, control);      
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
               "menu_entry_name"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   private IPSDesignerObjectUpdater m_comp;
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

}

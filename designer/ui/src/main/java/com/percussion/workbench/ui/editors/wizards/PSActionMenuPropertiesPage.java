/******************************************************************************
 *
 * [ PSMenuActionPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.common.PSActionMenuWizardCommonComposite;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.widgets.Composite;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:56 PM
 */
public class PSActionMenuPropertiesPage extends PSWizardPageBase
{

   public PSActionMenuPropertiesPage()
   {
     super("New Menu", "Create New Menu", null);
   }

  
   /* 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(
    * org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {
       m_comp = new PSActionMenuWizardCommonComposite(parent, this, false);
       setControl(m_comp);
   }

   /**
    * Checks which radio button is selected for the menu type.
    * 
    * @return <code>true</code> if the dynamic menu radio is chosen,
    * <code>false</code> otherwise.
    */
   public boolean isCascadingMenu()
   {
      return m_comp.isCascadingMenu();
   }

   /**
    * Convenience method that calls {@link #isCascadingMenu()} and inverts it.
    * 
    * @return !<code>isCascadingMenu()</code>. 
    */
   public boolean isDynamicMenu()
   {
      return !isCascadingMenu();
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
               "menu_name",
            "PSMenuActionWizardCommonComposite.menuType.label",
               "menu_type"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
   
   private PSActionMenuWizardCommonComposite m_comp;

}

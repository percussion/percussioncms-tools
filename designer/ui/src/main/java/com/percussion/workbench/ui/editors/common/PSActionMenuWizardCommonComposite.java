/******************************************************************************
 *
 * [ PSActionMenuWizardCommonComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.common;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import com.percussion.workbench.ui.editors.wizards.PSWizardPageBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class PSActionMenuWizardCommonComposite extends Composite
   implements IPSDesignerObjectUpdater, IPSUiConstants
{
   /**
    * Create the composite
    * @param parent
    * @param page enclosing wizard page, cannot be <code>null</code>.
    * @param isMenuEntry flag indicating this is a menu entry
    */
   public PSActionMenuWizardCommonComposite(
      Composite parent, PSWizardPageBase page, boolean isMenuEntry)
   {
      super(parent, SWT.NONE);
      if(page == null)
         throw new IllegalArgumentException("page cannot be null.");
      setLayout(new FormLayout());
                  
      String namePrefix = isMenuEntry 
         ? PSMessages.getString(
            "PSMenuActionWizardCommonComposite.label.menu.entry") //$NON-NLS-1$
            : PSMessages.getString(
               "PSMenuActionWizardCommonComposite.label.menu");  //$NON-NLS-1$
      // This composite takes care of registering its own controls
      m_commonComp = new PSNameLabelDesc(
         this, SWT.NONE, namePrefix, WIZARD_LABEL_NUMERATOR,
         m_commonComp.SHOW_ALL, page);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      formData.left = new FormAttachment(0, 0);
      m_commonComp.setLayoutData(formData);      
      
      if(!isMenuEntry)
      {
         m_menuTypeButtons = new PSRadioAndCheckBoxes(this,
            PSMessages.getString(
               "PSMenuActionWizardCommonComposite.menuType.label"), //$NON-NLS-1$
            SWT.HORIZONTAL | SWT.RADIO | SWT.SEPARATOR);
         m_menuTypeButtons.addEntry(
            PSMessages.getString(
               "PSMenuActionWizardCommonComposite.type.cascadingMenu.label")); //$NON-NLS-1$
         m_menuTypeButtons.addEntry(
            PSMessages.getString(
               "PSMenuActionWizardCommonComposite.type.dynamicMenu.label")); //$NON-NLS-1$
         m_menuTypeButtons.layoutControls();
         m_menuTypeButtons.setSelection(0); // default
         final FormData formData_1 = new FormData();
         formData_1.right = new FormAttachment(100, 0);
         formData_1.left = new FormAttachment(0, 0);
         formData_1.top = new FormAttachment(m_commonComp, 30, SWT.BOTTOM);
         m_menuTypeButtons.setLayoutData(formData_1);
         page.registerControl(
            "PSMenuActionWizardCommonComposite.menuType.label", //$NON-NLS-1$
            m_menuTypeButtons,
            null);
      }
     
      //
   }

   /**
    * Determines which radio button is selected for the menu type.
    * 
    * @return <code>true</code> if the cascading menu radio is selected,
    * <code>false</code> otherwise.
    */
   public boolean isCascadingMenu()
   {
      int selection = m_menuTypeButtons.getSelectedIndex();
      return selection == 0;      
   }
   
   @Override
   public void dispose()
   {
      super.dispose();
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSAction action = (PSAction)designObject;
      
      if(control == m_commonComp.getNameText())
      {
         action.setName(((Text)m_commonComp.getNameText()).getText());
      }
      else if(control == m_commonComp.getLabelText())
      {
         action.setLabel(m_commonComp.getLabelText().getText());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         action.setDescription(m_commonComp.getDescriptionText().getText());
      }
      else if(control == m_menuTypeButtons)
      {
         action.setMenuType(PSAction.TYPE_MENU);
         action.setMenuDynamic(!isCascadingMenu());
      }
      
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(@SuppressWarnings("unused") Object designObject)
   {
      // no-op      
   }
   
   //Controls
   private PSNameLabelDesc m_commonComp;
   private PSRadioAndCheckBoxes m_menuTypeButtons;
}

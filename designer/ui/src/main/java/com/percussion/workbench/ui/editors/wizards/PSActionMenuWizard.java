/******************************************************************************
 *
 * [ PSMenuActionWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSMessages;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:56 PM
 */
public class PSActionMenuWizard extends PSWizardBase 
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSActionMenuWizard()
   {
      super(PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
            PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER));
   }

   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSActionMenuWizard.title")); //$NON-NLS-1$
      addPage(new PSActionMenuPropertiesPage());
   }

   /**
    * @inheritDoc
    * Need to handle secondary type.
    */
   @Override
   protected PSObjectType getObjectTypeForRef()
   {
      Enum secondary = null;
      for(IWizardPage page : getPages())
      {
         if(page instanceof PSActionMenuPropertiesPage)
         {
            PSActionMenuPropertiesPage actionPage = (PSActionMenuPropertiesPage) page;
            secondary = actionPage.isCascadingMenu() 
                  ? PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER
                  : PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER; 
         }
      }
      return PSObjectTypeFactory.getType(
            (IPSPrimaryObjectType) getObjectType().getPrimaryType(),
            secondary);
   }
}

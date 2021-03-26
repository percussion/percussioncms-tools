/******************************************************************************
 *
 * [ PSSearchWizard.java ]
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
 * @created 03-Sep-2005 4:43:59 PM
 */
public class PSSearchWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSSearchWizard()
   {
      super(PSObjectTypeFactory.getType(PSObjectTypes.UI_SEARCH,
            PSObjectTypes.SearchSubTypes.STANDARD));
   }

   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSSearchWizard.label.new.search")); //$NON-NLS-1$
      addPage(new PSViewPropertiesPage(false, 
         PSMessages.getString("PSSearchWizard.label.pagename"), //$NON-NLS-1$
         PSMessages.getString("PSSearchWizard.label.title"), null));  //$NON-NLS-1$
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
         if(page instanceof PSViewPropertiesPage)
         {
            PSViewPropertiesPage viewPage = (PSViewPropertiesPage) page;
            secondary = viewPage.isStandard() ? PSObjectTypes.SearchSubTypes.STANDARD
                  : PSObjectTypes.SearchSubTypes.CUSTOM; 
         }
      }
      return PSObjectTypeFactory.getType(
            (IPSPrimaryObjectType) getObjectType().getPrimaryType(),
            secondary);
   }
}

/******************************************************************************
 *
 * [ PSViewWizard.java ]
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
 * @created 03-Sep-2005 4:44:05 PM
 */
public class PSViewWizard extends PSWizardBase 
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSViewWizard()
   {
      super(PSObjectTypeFactory.getType(PSObjectTypes.UI_VIEW,
            PSObjectTypes.SearchSubTypes.STANDARD));
   }

   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSViewWizard.label.new.view")); //$NON-NLS-1$
      addPage(new PSViewPropertiesPage(true, 
         PSMessages.getString("PSViewWizard.label.pagename"), //$NON-NLS-1$
         PSMessages.getString("PSViewWizard.label.title"), null));  //$NON-NLS-1$
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

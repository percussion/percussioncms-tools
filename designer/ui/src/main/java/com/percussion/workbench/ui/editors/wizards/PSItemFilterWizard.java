/******************************************************************************
*
* [ PSItemFilterWizard.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSMessages;

public class PSItemFilterWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSItemFilterWizard()
   {
      super(new PSObjectType(PSObjectTypes.ITEM_FILTER));
   }

   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSItemFilterWizard.title")); //$NON-NLS-1$
      PSItemFilterPropertiesPage page = new PSItemFilterPropertiesPage();
      addPage(page);
   }
}

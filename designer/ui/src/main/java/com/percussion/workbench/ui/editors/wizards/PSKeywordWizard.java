/******************************************************************************
 *
 * [ PSKeywordWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSMessages;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:54 PM
 */
public class PSKeywordWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSKeywordWizard()
   {
      super(new PSObjectType(PSObjectTypes.KEYWORD));
   }

   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(
         PSMessages.getString("PSKeywordWizard.label.new.keyword")); //$NON-NLS-1$
      addPage(new PSKeywordPropertiesPage());
      addPage(new PSKeywordChoiceListPage());
   }
}

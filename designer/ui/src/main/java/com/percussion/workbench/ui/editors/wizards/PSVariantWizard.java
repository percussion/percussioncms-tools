/******************************************************************************
 *
 * [ PSVariantWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSMessages;

/**
 * Wizard creating new template.
 *
 * @author Andriy Palamarchuk
 */
public class PSVariantWizard extends PSWizardBase 
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSVariantWizard()
   {
      super(PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
            PSObjectTypes.TemplateSubTypes.VARIANT));
   }

   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSVariantWizard.title")); //$NON-NLS-1$
      addPage(new PSVariantPropertiesPage());
   }
}

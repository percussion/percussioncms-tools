/******************************************************************************
 *
 * [ PSExtensionRegistrationWizard.java ]
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

/**
 * Wizard registering an extension. 
 *
 * @author Andriy Palamarchuk
 */
public class PSExtensionRegistrationWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSExtensionRegistrationWizard()
   {
      super(new PSObjectType(PSObjectTypes.EXTENSION));
   }
  
   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSExtensionRegistrationWizard.title")); //$NON-NLS-1$
      m_propertiesPage = new PSExtensionRegistrationPropertiesPage();
      addPage(m_propertiesPage);
   }

   /**
    * Standard behavior is redefined because extension name is composed from
    * many components, not just name field.
    */
   @Override
   protected String findObjectName()
   {
      return m_propertiesPage.findObjectName();
   }
   
   /**
    * The extension properties page.
    */
   private PSExtensionRegistrationPropertiesPage m_propertiesPage;
}

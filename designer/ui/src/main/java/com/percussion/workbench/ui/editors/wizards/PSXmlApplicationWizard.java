/******************************************************************************
 *
 * [ PSXmlApplicationWizard.java ]
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
 * Wizard creating an application.
 */
public class PSXmlApplicationWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSXmlApplicationWizard()
   {
      //sub-type doesn't matter
      super(new PSObjectType(PSObjectTypes.XML_APPLICATION,
            PSObjectTypes.XmlApplicationSubTypes.USER));
   }

   // see base class
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSXmlApplicationWizard.title")); //$NON-NLS-1$
      addPage(new PSXmlApplicationPropertiesPage());
   }
}

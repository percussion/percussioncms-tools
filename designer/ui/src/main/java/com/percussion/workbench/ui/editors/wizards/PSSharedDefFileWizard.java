/******************************************************************************
 *
 * [ PSSharedDefFileWizard.java ]
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
import org.apache.commons.lang.StringUtils;

public class PSSharedDefFileWizard extends PSWizardBase
{

   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSSharedDefFileWizard() 
   {
      super(new PSObjectType(PSObjectTypes.SHARED_FIELDS));
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSSharedDefFileWizard.title")); //$NON-NLS-1$
      m_propsPage = new PSSharedFieldPropertiesPage();
      addPage(m_propsPage);
   }
   
   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.wizard.IWizard#performFinish()
    */
   @Override
   public boolean performFinish()
   {
      String fileName = m_propsPage.getFileNameText().getText();
      if(!StringUtils.lowerCase(fileName).endsWith(XML_EXTENSION))
         m_propsPage.getFileNameText().setText(fileName + XML_EXTENSION);
      return super.performFinish();
   }
   
   /**
    * Properties page variable. Initialized in the addPages method.
    */
   private PSSharedFieldPropertiesPage m_propsPage;

   /**
    * Constant for xml extension for shred def file.
    */
   public static final String XML_EXTENSION = ".xml";  //$NON-NLS-1$
}

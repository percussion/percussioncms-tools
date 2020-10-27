/*******************************************************************************
 *
 * [ PSTemplateExportWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectTypes;

import static com.percussion.workbench.ui.editors.wizards.PSTemplateImportWizard.TEMPLATE_SUFFIX;

/**
 * Exports templates to selected directory.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateExportWizard extends PSExportWizardBase
{
   // see base
   @Override
   protected PSObjectTypes getPrimaryType()
   {
      return PSObjectTypes.TEMPLATE;
   }

   @Override
   protected String getFileExtension()
   {
      return TEMPLATE_SUFFIX;
   }

   @Override
   protected String getPageImage()
   {
      return "template.gif";
   }

   @Override
   protected String getMessagePrefix()
   {
      return "PSTemplateExportWizard.";
   }
}

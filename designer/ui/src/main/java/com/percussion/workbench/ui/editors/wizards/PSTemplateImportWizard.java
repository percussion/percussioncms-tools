/******************************************************************************
 *
 * [ PSTemplateImportWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectTypes;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Imports templates from files.
 * 
 * @author Andriy Palamarchuk
 * @author Doug Rand
 */
public class PSTemplateImportWizard extends PSImportWizardBase
{
   // see base class
   @Override
   protected void fromXml(Object o, String xml)
         throws IOException, SAXException, PSUnknownNodeTypeException,
         PSInvalidXmlException
   {
      final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) o;
      final Integer version = template.getVersion();
      template.setVersion(null);

      super.fromXml(o, xml);

      template.setVersion(null);
      template.setVersion(version);
   }

   // see base class
   @Override
   protected Object createTemp()
   {
      return new PSUiAssemblyTemplate();
   }

   // see base class
   @Override
   protected PSObjectTypes getPrimaryType()
   {
      return PSObjectTypes.TEMPLATE;
   }

   // see base class
   @Override
   protected String getMessagePrefix()
   {
      return "PSTemplateImportWizard.";
   }

   // see base class
   @Override
   protected String getPageImage()
   {
      return "template.gif";
   }

   // see base class
   @Override
   protected String getFileExtension()
   {
      return TEMPLATE_SUFFIX;
   }

   /**
    * Template file extension.
    */
   public static final String TEMPLATE_SUFFIX = ".template";
}

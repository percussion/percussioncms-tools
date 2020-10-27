/*******************************************************************************
 *
 * [ PSContentTypeExportWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static com.percussion.workbench.ui.editors.wizards.PSContentTypeImportWizard.CONTENT_TYPE_SUFFIX;

/**
 * Exports templates to selected directory.
 *
 * @author Andriy Palamarchuk
 */
public class PSContentTypeExportWizard extends PSExportWizardBase
{
   /**
    * Calls base <code>toXml(Object)</code> for item def after system fields are
    * demerged from it.
    * @see PSExportWizardBase#toXml(Object)
    */
   @Override
   protected String toXml(Object o) throws SAXException
   {
      final PSItemDefinition itemDef = (PSItemDefinition) o;
      // save demerged clone
      final PSItemDefinition clone = (PSItemDefinition) itemDef.clone();
      try
      {
         PSContentEditorDefinition.demergeFields(clone);
      }
      catch (Exception e)
      {
         throw new SAXException(e);
      }
      final Document doc = PSXmlDocumentBuilder.createXmlDocument();
      return PSXmlDocumentBuilder.toString(
            clone.getContentEditor().toXml(doc));
   }

   // see base
   @Override
   protected String getFileExtension()
   {
      return CONTENT_TYPE_SUFFIX;
   }

   // see base
   @Override
   protected String getPageImage()
   {
      return "contentType.gif";
   }

   // see base
   @Override
   protected PSObjectTypes getPrimaryType()
   {
      return PSObjectTypes.CONTENT_TYPE;
   }

   // see base
   @Override
   protected String getMessagePrefix()
   {
      return "PSContentTypeExportWizard.";
   }
}
